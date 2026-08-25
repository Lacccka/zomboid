--
-- True Companions - companions close doors behind them
--
-- A follower opening every door in the base and leaving them all open is how a
-- cleared base fills up with zombies. She should shut the door.
--
-- HOW SHE IS KNOWN TO HAVE OPENED ONE: Bandits fires its own event for exactly this,
-- BanditNotifications.EVENT_TOGGLE_DOOR ("OnBanditDoorToggled"), with the bandit, the
-- object and its coordinates. Listening to that is far better than trying to infer it
-- from movement -- no guessing which door she used, and no sweep over doors she never
-- touched. The event is Bandits' own public notification, so this is additive: nothing
-- upstream is patched or replaced.
--
-- WHY ONLY INSIDE A BASE AREA. Closing doors out in the world would be actively bad --
-- she would shut doors the player is walking through, re-close doors deliberately
-- propped open, and pointlessly tidy a house nobody is coming back to. Inside a
-- defined base area it is unambiguously what you want, and it is the one place the
-- player has told us they care about. With no base drawn, nothing here does anything.
--
-- SHE HAS TO BE CLEAR OF IT FIRST. Closing the door on the tick she opens it would
-- shut it in her own face and, worse, in the player's if they were following her
-- through. The door is remembered and closed once she is at least two tiles away, so
-- it reads as her pulling it to behind her.
--

BanditsNPC = BanditsNPC or {}

local PENDING = {}          -- uid -> { x, y, z, idx, atMs }
local PENDING_N = 0         -- see anyPending(): PZ's Lua has no `next`
local CLEAR_DIST = 2.0      -- tiles she must be from the door before it shuts
local GIVE_UP_MS = 30000    -- forget a door she never walked away from

-- THERE IS NO GLOBAL `next` IN KAHLUA, PZ's Lua. `if not next(PENDING)` is the
-- idiomatic Lua emptiness test and it is a nil call here -- which raises
-- KahluaUtil.fail, and that propagates THROUGH the pcall wrapped round it. From a
-- per-frame OnPlayerUpdate handler that is an error box every frame that the player
-- cannot dismiss, which is exactly what v0.73 shipped.
--
-- PENDING is keyed by uid so # does not work on it either. The count is maintained by
-- the two functions that write the table, and nothing else may touch it directly.
local function setPending(uid, value)
    if PENDING[uid] == nil and value ~= nil then PENDING_N = PENDING_N + 1
    elseif PENDING[uid] ~= nil and value == nil then PENDING_N = PENDING_N - 1 end
    PENDING[uid] = value
end

local function anyPending()
    return PENDING_N > 0
end

local function uidOf(zombie)
    local b = BanditBrain and BanditBrain.Get(zombie)
    return b and (b.npcUid or b.id), b
end

-- Is this square inside a base area of any beacon?
local function insideABase(x, y, z)
    if not (BanditsNPC.Beacon and BanditsNPC.Beacon.AllSites and BanditsNPC.Zones) then return false end
    local ok, res = pcall(function()
        for _, site in ipairs(BanditsNPC.Beacon.AllSites()) do
            local base = BanditsNPC.Zones.Base(site)
            if base and BanditsNPC.Zones.Contains(base, x + 0.5, y + 0.5, z) then return true end
        end
        return false
    end)
    return ok and res == true
end

local function doorAt(info)
    local found
    pcall(function()
        local sq = getCell():getGridSquare(info.x, info.y, info.z)
        if not sq then return end
        local objs = sq:getObjects()
        -- By object INDEX where we have it: a doorway can hold a door and a barricade,
        -- and re-finding "the first door-ish object" could pick the wrong one.
        if info.idx and info.idx >= 0 and info.idx < objs:size() then
            local o = objs:get(info.idx)
            if o and (instanceof(o, "IsoDoor") or (instanceof(o, "IsoThumpable") and o:isDoor())) then
                found = o
                return
            end
        end
        for i = 0, objs:size() - 1 do
            local o = objs:get(i)
            if o and (instanceof(o, "IsoDoor") or (instanceof(o, "IsoThumpable") and o:isDoor())) then
                found = o
                return
            end
        end
    end)
    return found
end

-- ONLY SIMPLE, SINGLE-LEAF DOORS.
--
-- A double door and a garage door are ONE object spread over several squares, and
-- toggling one leaf of one leaves the engine unable to find all its parts -- which it
-- then complains about on every frame, forever. That froze the game on the first
-- session this code actually ran, so the rule now is: if there is any doubt that this
-- is one plain door on one square, leave it open. A door left open is a small
-- annoyance; a door that freezes the game is not a trade worth making.
--
-- getDoubleDoorIndex and getGarageDoorIndex are the tests Bandits itself uses to pick
-- its multi-tile toggle path (BanditUpdate.lua:780,802) and vanilla uses them too, so
-- they are proven. isObjectMultiSquare is the engine's own general question and is
-- asked as well, guarded separately because nothing in vanilla Lua calls it.
local function closable(door)
    local ok = false
    pcall(function()
        if IsoDoor.getDoubleDoorIndex(door) > -1 then return end
        if IsoDoor.getGarageDoorIndex(door) > -1 then return end
        ok = true
    end)
    if not ok then return false end
    -- A second, independent opinion. If this call is unavailable the pcall leaves
    -- `multi` false and we fall back to the two tests above rather than refusing
    -- everything.
    local multi = false
    pcall(function()
        if IsoObjectUtils and IsoObjectUtils.isObjectMultiSquare then
            multi = IsoObjectUtils.isObjectMultiSquare(door) and true or false
        end
    end)
    return not multi
end

local function enabled()
    -- An off switch, on the Beacon's Settings tab. This feature froze the game once
    -- already; anything that can do that should be something the player can turn off
    -- without waiting for a patch.
    return (BanditsNPC.Opt and BanditsNPC.Opt("CompanionsCloseDoors", true)) ~= false
end

local function onDoorToggled(bandit, object, info)
    pcall(function()
        if not enabled() then return end
        if not (bandit and info and info.open) then return end
        if not (BanditsNPC.IsRecruitedCompanion and BanditsNPC.IsRecruitedCompanion(bandit)) then return end
        if not insideABase(info.x, info.y, info.z) then return end
        local uid = uidOf(bandit)
        if not uid then return end
        setPending(uid, { x = info.x, y = info.y, z = info.z, idx = info.idx,
                          atMs = (getTimestampMs and getTimestampMs()) or 0 })
    end)
end

local lastSweepMs = 0
local function sweep()
    pcall(function()
        local now = (getTimestampMs and getTimestampMs()) or 0
        if now - lastSweepMs < 700 then return end
        lastSweepMs = now
        if not anyPending() then return end

        -- Index the loaded companions once, rather than searching per pending door.
        local byUid = {}
        local zl = getCell() and getCell():getZombieList()
        if zl then
            for i = 0, zl:size() - 1 do
                local z = zl:get(i)
                if z and z:getVariableBoolean("Bandit") then
                    local u = uidOf(z)
                    if u then byUid[u] = z end
                end
            end
        end

        for uid, d in pairs(PENDING) do
            local z = byUid[uid]
            local done = false
            if now - d.atMs > GIVE_UP_MS then
                done = true                     -- she never left; stop tracking it
            elseif z then
                local dist = BanditUtils.DistTo(z:getX(), z:getY(), d.x + 0.5, d.y + 0.5)
                if dist >= CLEAR_DIST then
                    local door = doorAt(d)
                    if door and closable(door) then
                        pcall(function()
                            -- ToggleDoorSilent(), NOT ToggleDoor(character).
                            --
                            -- ToggleDoor TAKES AN IsoGameCharacter AND THEN USES IT AS A
                            -- PLAYER. IsoDoor.ToggleDoorActual:1611 calls
                            -- player.isLocalPlayer(), and for a zombie-bodied companion
                            -- that reference is null:
                            --   NullPointerException: Cannot invoke
                            --   "zombie.characters.IsoPlayer.isLocalPlayer()" because
                            --   "player" is null   (4 Aug log, BanditsNPCDoors.lua:193)
                            -- So door-closing threw on EVERY door from the day it shipped
                            -- and never once worked, which is exactly what the author saw.
                            -- The signature accepting the base class is the trap; nothing
                            -- about it says "players only".
                            --
                            -- ToggleDoorSilent does the entire job with no character at
                            -- all: barricade check, InvalidateSpecialObjectPaths, the LOS
                            -- cache, setRecalcLightTime, setOpen and the sprite swap
                            -- (javap -c). What it does not do is play the sound -- which
                            -- for a companion tidying up behind herself is no loss.
                            --
                            -- This is NOT the v0.74.1 mistake of hand-rolling the
                            -- sequence: the multi-tile guards in closable() stay, and a
                            -- double or garage door is still never touched.
                            if door:IsOpen() then
                                door:ToggleDoorSilent()
                                -- door:syncIsoObject(false, 0, nil, nil) -- an INSTANCE
                                -- method taking four args, exactly as vanilla calls it
                                -- (ISLockDoor.lua:56/:62/:69).
                                --
                                -- This was `if syncIsoObject then syncIsoObject(door) end`
                                -- -- a BARE GLOBAL that does not exist. Dumping
                                -- LuaManager$GlobalObject gives 730 globals and eleven
                                -- sync* functions, all for players/items/clothing; there
                                -- is none for IsoObjects. So the guard was false on every
                                -- pass, silently, and ToggleDoorSilent's work stayed
                                -- local: the door shut for whoever ran the sweep and
                                -- stayed open for the server and every other client.
                                -- Correct in SP, which is why it shipped (v0.75.7).
                                pcall(function() door:syncIsoObject(false, 0, nil, nil) end)
                            end
                        end)
                    end
                    done = true
                end
            else
                -- She streamed out. The door is somebody else's problem now.
                done = true
            end
            if done then setPending(uid, nil) end
        end
    end)
end

if Events.OnBanditDoorToggled then
    Events.OnBanditDoorToggled.Add(onDoorToggled)
end
if Events.OnPlayerUpdate then
    Events.OnPlayerUpdate.Add(sweep)
end
