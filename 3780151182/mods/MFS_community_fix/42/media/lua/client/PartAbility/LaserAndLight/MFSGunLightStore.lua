-- MFS community fix: durable store for gun-light switch state and battery.
--
-- WHY THIS EXISTS - THE MULTIPLAYER modData WIPE
-- ----------------------------------------------
-- In multiplayer the weapon's ENTIRE modData table is replaced 2-3 frames after
-- every shot, and again on reload. Proven by probe. The item is NOT replaced -
-- its ID and its Lua object identity both stay the same - but every key in the
-- table disappears at once:
--
--   FIRE    id=1277067068 on=true  batt=99.9 stamp=S2  cone=true  ammo=29
--   CHANGE (+2 frames)  on: true -> nil, battery: 99.9 -> nil, stamp: S2 -> S3
--
-- The decisive detail is "stamp". That key was written by the diagnostic probe
-- and nothing in MFS or the game knows it exists, yet it vanished with the
-- others. So this is not something clearing our keys; the whole table is being
-- swapped, almost certainly by the engine syncing the item after the ammo count
-- changes. A reload does it too - the probe caught stamp S33 -> S34 with ammo
-- going 0 -> 30 and no shot in between, which is why swapping an empty magazine
-- also killed the light.
--
-- The symptom was a silent light-off: our handler read on=nil, correctly
-- concluded the switch was off and cleared the torch. No halo text appeared
-- because toggleLight() never ran. The control layer was behaving properly on
-- top of storage that was being erased underneath it.
--
-- Battery was equally broken in MP and nobody had noticed: it reset to 100 on
-- every re-toggle, so drain could never accumulate.
--
-- Single-player is unaffected - item modData is stable there - which is why
-- every SP test passed, including firing on single and full auto.
--
-- THE FIX - AND THE CORRECTION THAT FOLLOWED IT
-- ---------------------------------------------
-- First attempt (RC7E) moved state to PLAYER modData, on the reasoning that it
-- is not touched by item synchronisation. That was HALF right. It does survive
-- the item wipe - but player modData is ALSO server-authoritative, and MP
-- testing showed the server pushing an older snapshot down over the client's
-- newer value. See the SESSION CACHE comment further down for the log evidence.
--
-- So the real rule is: on an MP client, modData of ANY kind - item or player -
-- cannot be trusted as live state. It is a persistence medium, not a variable.
--
-- Current design (RC7E-b): an in-memory session table is authoritative. modData
-- is read exactly ONCE per weapon per session to seed it, and written on every
-- mutation so a save has something to store. It is never read again, so a
-- pushback cannot revert anything the player can see.
--
-- RESIDUAL LIMITATION, ACCEPTED KNOWINGLY
-- Persistence across a relog is only as good as whatever the server last
-- saved, so a charge may come back slightly stale after reconnecting. Fully
-- correct persistence needs the client-command / server / ACK pattern that
-- MFSPartOffsetSync.lua and MFSMagazineSyncFix.lua already use - which exists
-- in this project for exactly this reason. That is the right long-term answer
-- and was judged too heavy for a light switch in this round.
--
-- Entries are keyed by item ID, in both the session table and modData:
--
--   player:getModData().MFSGunLightState = {
--       ["1277067068"] = { on = true, battery = 99.8, warned = nil, touched = 41 },
--   }
--
-- WHAT THIS DOES NOT SOLVE
-- ------------------------
-- The wipe itself. It still destroys every other MFS key on the weapon on every
-- shot in MP - LaserBatteryReamin, NowLightSet, MFSOriginalSwingSound, and the
-- GunPos part offsets that MFSPartOffsetPersistence re-applies.
--
-- No currently observed defect is attributed to that. A lead was raised
-- connecting it to MP issues 1/2/6 in MFS_PATCH_5_0_RC1_MP_DIAGNOSIS.txt
-- ("held weapon in a wrong pose") and WITHDRAWN: the operator confirms those
-- were fixed long ago. That document is from RC1 and its open-issue list is
-- stale by seven releases - do not mine it for defects without checking first.
--
-- KEY STABILITY
-- Item IDs are assigned by the game and saved. If one ever proved unstable
-- across sessions the entry would simply not be found and the light would
-- default to off - which is the pre-existing behaviour, so the failure mode is
-- a no-op rather than a regression.

MFSGunLightStore = MFSGunLightStore or {}

local Store = MFSGunLightStore

Store.VERSION = "1.1.0"

-- Lives on the PLAYER, not the item. This is the whole point.
Store.ROOT_KEY = "MFSGunLightState"

Store.DEFAULT_BATTERY = 100

-- Cap on remembered weapons, so a long-lived character cannot accumulate an
-- unbounded table. Least-recently-touched entries are dropped first.
Store.MAX_ENTRIES = 64

-- Pre-RC7E saves kept the charge here, on the item. Read once when an entry is
-- first created so existing single-player characters do not lose their battery
-- level. Never written back - see the header.
Store.LEGACY_BATTERY_KEY = "LightBatteryReamin"

local function log(message)
    print("[MFSGunLightStore] " .. tostring(message))
end

local function try(fn, fallback)
    local ok, value = pcall(fn)
    if ok then
        return value
    end
    return fallback
end

-- Monotonic, so pruning does not depend on the game clock.
local touchCounter = 0

local function root(playerObj)
    if not playerObj then
        return nil
    end

    local modData = try(function() return playerObj:getModData() end, nil)
    if type(modData) ~= "table" then
        return nil
    end

    local table_ = modData[Store.ROOT_KEY]
    if type(table_) ~= "table" then
        table_ = {}
        modData[Store.ROOT_KEY] = table_
    end
    return table_
end

-- String keys, because modData is serialised and numeric keys do not survive
-- the round trip reliably.
local function keyFor(weapon)
    local id = try(function() return weapon:getID() end, nil)
    if id == nil then
        return nil
    end
    return tostring(id)
end

local function prune(entries)
    local count = 0
    for _ in pairs(entries) do
        count = count + 1
    end
    if count <= Store.MAX_ENTRIES then
        return
    end

    while count > Store.MAX_ENTRIES do
        local oldestKey, oldestTouch = nil, nil
        for key, entry in pairs(entries) do
            local touched = (type(entry) == "table" and entry.touched) or 0
            if oldestTouch == nil or touched < oldestTouch then
                oldestKey, oldestTouch = key, touched
            end
        end
        if not oldestKey then
            return
        end
        entries[oldestKey] = nil
        count = count - 1
    end
end

-- SESSION CACHE - THE AUTHORITATIVE COPY. See the header note below.
--
-- RC7E-b: player modData turned out to be server-authoritative too. Storing
-- here and reading back from modData produced two distinct failures in the MP
-- test log:
--
--     on: true -> false, battery: 97.5 -> 100   entry gone, recreated default
--     battery: 49   -> 97.5                     restored to a STALE value
--     on: false -> true, battery: 22.2 -> 49    restored to a STALE value
--
-- The server holds a periodically saved snapshot of the player's modData and
-- pushes it down over the client's newer value. Where it had no snapshot the
-- entry simply vanished and was recreated at the defaults.
--
-- So modData - item OR player - cannot be READ as live state on an MP client.
-- The fix is to read it exactly once per weapon per session to seed, and treat
-- this in-memory table as the truth from then on. A pushback still overwrites
-- the modData copy; we just never look at it again, so it cannot revert
-- anything the player can see.
--
-- Not indexed into modData, so nothing the engine syncs can reach it.
Store._session = Store._session or {}

local function playerKey(playerObj)
    return try(function() return playerObj:getPlayerNum() end, 0) or 0
end

local function sessionTable(playerObj)
    local key = playerKey(playerObj)
    local cache = Store._session[key]
    if type(cache) ~= "table" then
        cache = {}
        Store._session[key] = cache
    end
    return cache
end

-- Push the live value back into modData so a save has something to write. Best
-- effort only: in MP the server may clobber it moments later, which is fine
-- because it is never read again this session.
local function persist(playerObj, key, entry)
    local entries = root(playerObj)
    if not entries then
        return
    end
    entries[key] = {
        on = entry.on,
        battery = entry.battery,
        warned = entry.warned,
        touched = entry.touched
    }
    prune(entries)
end

-- Returns the entry for this weapon, creating it if needed, or nil if the
-- player or item cannot be resolved.
function Store.entry(playerObj, weapon)
    if not weapon then
        return nil
    end

    local key = keyFor(weapon)
    if not key then
        return nil
    end

    local cache = sessionTable(playerObj)
    local entry = cache[key]

    if type(entry) ~= "table" then
        entry = {on = false, battery = Store.DEFAULT_BATTERY}

        -- SEED, ONCE. This is the only read of modData for this weapon in this
        -- session. Prefer a saved player-modData entry, then the pre-RC7E item
        -- key, then the defaults.
        local entries = root(playerObj)
        local saved = entries and entries[key] or nil
        if type(saved) == "table" then
            if type(saved.battery) == "number" then
                entry.battery = saved.battery
            end
            entry.on = (saved.on == true)
            entry.warned = (saved.warned == true) or nil
        else
            local legacy = try(function()
                return weapon:getModData()[Store.LEGACY_BATTERY_KEY]
            end, nil)
            if type(legacy) == "number" then
                entry.battery = legacy
            end
        end

        cache[key] = entry
        prune(cache)
    end

    touchCounter = touchCounter + 1
    entry.touched = touchCounter

    return entry
end

-- Called after every mutation. Keeps the modData copy roughly current for
-- persistence without ever depending on it.
local function commit(playerObj, weapon, entry)
    local key = keyFor(weapon)
    if key and entry then
        persist(playerObj, key, entry)
    end
end

function Store.isOn(playerObj, weapon)
    local entry = Store.entry(playerObj, weapon)
    return entry ~= nil and entry.on == true
end

function Store.setOn(playerObj, weapon, value)
    local entry = Store.entry(playerObj, weapon)
    if entry then
        entry.on = (value == true)
        commit(playerObj, weapon, entry)
    end
end

function Store.getBattery(playerObj, weapon)
    local entry = Store.entry(playerObj, weapon)
    if not entry then
        return Store.DEFAULT_BATTERY
    end
    if type(entry.battery) ~= "number" then
        entry.battery = Store.DEFAULT_BATTERY
    end
    return entry.battery
end

function Store.setBattery(playerObj, weapon, value)
    local entry = Store.entry(playerObj, weapon)
    if not entry then
        return
    end
    if type(value) ~= "number" then
        return
    end
    if value < 0 then
        value = 0
    end
    if value > Store.DEFAULT_BATTERY then
        value = Store.DEFAULT_BATTERY
    end
    entry.battery = math.floor((value * 10) + 0.5) / 10
    commit(playerObj, weapon, entry)
end

-- One-shot latch for the low-battery warning, so it does not repeat every
-- in-game minute. Kept here rather than on the item for the same reason as
-- everything else in this file.
function Store.isWarned(playerObj, weapon)
    local entry = Store.entry(playerObj, weapon)
    return entry ~= nil and entry.warned == true
end

function Store.setWarned(playerObj, weapon, value)
    local entry = Store.entry(playerObj, weapon)
    if entry then
        entry.warned = (value == true) or nil
        commit(playerObj, weapon, entry)
    end
end

log("version " .. Store.VERSION .. " loaded; live state in session cache, " ..
    "seeded once from and persisted to player modData key " .. Store.ROOT_KEY)
