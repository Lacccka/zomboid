-- MFS B42.20 Redux - HANDGUN GRIP OFFSET  (vanilla-body hand position)
--
-- DRAFT - NOT YET VERIFIED IN GAME. See "THE ONE UNVERIFIED ASSUMPTION" below.
-- Companion note: 42/MFS_HANDGUN_GRIP_OFFSET_NOTES.txt
--
-- THE PROBLEM
--   Players on the VANILLA body report the held pistol sitting slightly behind
--   the hand. Players on the VS body replacer never saw it. The VS body is
--   physically SMALLER than both vanilla bodies - confirmed by the operator via
--   overlapping bodies on the character creation screen; the vanilla hand is
--   visibly larger and has no defined fingers.
--
--   Every MFS pistol mesh was authored with its origin calibrated against that
--   smaller VS hand. With no `attachment Bip01_Prop1` block in the weapon model
--   script the engine uses offset 0,0,0 - i.e. the raw mesh origin - so every
--   vanilla-body player inherits the VS calibration.
--
-- THE LEVER  (operator-confirmed, 2026-08-24)
--   Adding this to a weapon model script MOVES THE HELD GUN:
--       attachment Bip01_Prop1
--       {
--           offset = 0.0 0.03 0.0,
--           rotate = 0.0 0.0 0.0,
--       }
--   Tested on M9A4. +0.03 on Y reads as "forward" and looks roughly correct on
--   the vanilla MALE body. On the VS female body the same value is too far
--   forward, which is expected - that body is what 0.0 was calibrated for.
--
--   This is the same mechanism RC2-2 used for the off-hand: `Bip01_Prop2` was
--   added to 39 pistols to fix the secondary-hand pose. Prop1 is the primary
--   hand, Prop2 the secondary (AWCWF_RenderPart.lua:214 hardcodes Prop1 for
--   held-weapon parts).
--
-- WHY THIS IS LUA AND NOT 39 SCRIPT EDITS
--   The correction depends on WHICH BODY MESH THE PLAYER HAS INSTALLED. A value
--   baked into the scripts is the same for everyone by construction, so it can
--   only ever be right for one population. Nothing in the Lua API reports which
--   body mesh is loaded, so it cannot be auto-detected either - it has to be a
--   client setting. That makes a runtime write the natural home, and it also
--   means:
--     * zero diff against upstream in the 39 gun scripts
--     * the value is adjustable live instead of one game restart per iteration
--     * "VS body" is simply 0.0, so existing VS users see NO change at all
--
-- SCOPE - HANDGUNS ONLY, DELIBERATELY
--   Rifles are NOT touched. Nobody has reported rifle grip position, and the
--   RC6 grip-mass finding (Handgun 39/39 toward -H, Rifle 125/129 toward +H)
--   says pistol meshes are authored ~180 degrees flipped relative to rifles -
--   so the same +Y could push a rifle the WRONG WAY. If rifles are ever taken
--   on, treat them as a separate sweep with their own sign and magnitude.
--
--   `getSwingAnim() == "Handgun"` is an EXACT match for the intended set:
--   verified 39/39 against the 39 scripts carrying Bip01_Prop2, and 0/132
--   against SwingAnim = Rifle. No hardcoded weapon list is needed.
--
-- THE ONE UNVERIFIED ASSUMPTION
--   Adding a ModelAttachment at RUNTIME is proven for weapon PARTS
--   (MFSPartOffsetPersistence.lua does exactly this). It is NOT yet proven for
--   the weapon BODY. If the held gun does not move after this file runs, the
--   engine only honours Bip01_Prop1 when it is present in the script at load
--   time, and the fallback is:
--       add `attachment Bip01_Prop1 { offset = 0.0 0.0 0.0, ... }` to the 39
--       handgun scripts as an inert placeholder, and let this file adjust it.
--   Same user-facing result, bigger diff. Test before assuming.
--
-- MULTIPLAYER
--   Client-side rendering only. No modData, no transmission, nothing the server
--   executes (this file lives under lua/client). No checksum exposure: every
--   client ships identical files and only the in-memory value differs.
--   Known cosmetic limit, accepted by the operator: ModelScript attachments are
--   shared per MODEL, not per character, so on your screen every rendered
--   character uses YOUR value. A remote player on a different body renders
--   slightly off for you and correct for themselves. Same class of limitation
--   already documented in MFSPartOffsetPersistence.lua.
--   Split-screen co-op with one male and one female player shares one model
--   too. Ignorable.

MFSHandgunGripOffset = MFSHandgunGripOffset or {}
local Grip = MFSHandgunGripOffset

Grip.DEBUG = false

-- Must match AWCWF_Mod_Options.lua.
Grip.OPTION_PAGE = "AWCWF_42_Patch"
Grip.OPT_OFFSET = "handgun_grip_offset"
Grip.OPT_REPLACER = "handgun_grip_body_replacer"

-- Operator-tuned on M9A4 against the vanilla male body. Vanilla FEMALE is not
-- yet checked - if it wants a different number this becomes two values.
Grip.DEFAULT_OFFSET = 0.03

-- The bone the primary-hand weapon hangs from.
Grip.BONE = "Bip01_Prop1"

-- spriteKey -> {x, y, z} as the model script had it BEFORE we touched it.
-- Currently always 0,0,0 because no gun script defines Prop1, but captured
-- properly so a future script-side value is added to rather than clobbered.
local baseline = {}

-- spriteKey -> last Y we wrote, so a no-op change never forces a model rebuild.
local lastWritten = {}

local function dbg(msg)
    if Grip.DEBUG then
        print("[MFSHandgunGrip] " .. tostring(msg))
    end
end

-- ---------------------------------------------------------------- options ---

local function options()
    if not PZAPI or not PZAPI.ModOptions then
        return nil
    end
    local ok, opts = pcall(function()
        return PZAPI.ModOptions:getOptions(Grip.OPTION_PAGE)
    end)
    if ok then
        return opts
    end
    return nil
end

local function optionValue(id, fallback)
    local opts = options()
    if not opts then
        return fallback
    end
    local ok, value = pcall(function()
        local opt = opts:getOption(id)
        if not opt then
            return nil
        end
        return opt:getValue()
    end)
    if ok and value ~= nil then
        return value
    end
    return fallback
end

function Grip.configuredOffset()
    local v = optionValue(Grip.OPT_OFFSET, Grip.DEFAULT_OFFSET)
    if type(v) ~= "number" then
        return Grip.DEFAULT_OFFSET
    end
    return v
end

function Grip.isReplacerInstalled()
    return optionValue(Grip.OPT_REPLACER, false) and true or false
end

-- The discriminator is SEX x REPLACER, not just replacer. Body replacers are
-- typically female-only, so a player who installed one still has the VANILLA
-- male body on a male character and still needs the correction:
--
--                    male char    female char
--   no replacer       0.03          0.03
--   replacer on       0.03          0.00
--
-- Sex alone cannot decide this - vanilla-female and VS-female are both "female"
-- and want opposite values. That is exactly why it must be a declared setting.
function Grip.effectiveOffset(player)
    local base = Grip.configuredOffset()
    if not player then
        return base
    end
    local ok, female = pcall(function()
        return player:isFemale()
    end)
    if ok and female and Grip.isReplacerInstalled() then
        return 0.0
    end
    return base
end

-- ------------------------------------------------------------------ apply ---

local function spriteKeyFor(weapon)
    local ok, key = pcall(function()
        local module = weapon.getModule and weapon:getModule() or "Base"
        return (module or "Base") .. "." .. tostring(weapon:getWeaponSprite())
    end)
    if ok and key then
        return key
    end
    return nil
end

local function isHandgun(weapon)
    if not weapon or not instanceof(weapon, "HandWeapon") then
        return false
    end
    local ok, anim = pcall(function()
        return weapon:getSwingAnim()
    end)
    return ok and anim == "Handgun"
end

-- Returns true when the model was actually changed.
function Grip.applyToSprite(spriteKey, y)
    if not spriteKey then
        return false
    end
    if lastWritten[spriteKey] == y then
        return false
    end

    local ok, changed = pcall(function()
        local model = ScriptManager.instance:getModelScript(spriteKey)
        if not model then
            dbg("model script NOT FOUND: " .. spriteKey)
            return false
        end

        local attachment = model:getAttachmentById(Grip.BONE)
        if not attachment then
            attachment = ModelAttachment.new(Grip.BONE)
            model:addAttachment(attachment)
            -- Only zero the rotation on an attachment WE created. If a script
            -- ever ships its own Prop1 block with a meaningful rotate, leave it.
            attachment:getRotate():set(0, 0, 0)
        end

        if not baseline[spriteKey] then
            local o = attachment:getOffset()
            baseline[spriteKey] = { x = o:x(), y = o:y(), z = o:z() }
        end

        local b = baseline[spriteKey]
        attachment:getOffset():set(b.x, b.y + y, b.z)
        return true
    end)

    if ok and changed then
        lastWritten[spriteKey] = y
        dbg("applied " .. tostring(y) .. " to " .. spriteKey)
        return true
    end
    return false
end

-- Guaranteed path. The local player's held pistol is the only model that has to
-- be right, and this fires whenever it changes.
function Grip.applyToPlayer(player)
    player = player or getPlayer()
    if not player then
        return
    end

    local weapon = player:getPrimaryHandItem()
    if not isHandgun(weapon) then
        return
    end

    local y = Grip.effectiveOffset(player)
    if Grip.applyToSprite(spriteKeyFor(weapon), y) then
        pcall(function()
            player:resetEquippedHandsModels()
        end)
    end
end

-- InventoryItem:getModule() returns a STRING (AWCWF_RenderPart.lua:139 relies
-- on that and works). ScriptItem:getModule() returns a ScriptModule OBJECT, and
-- concatenating it throws:
--     __concat not defined for operands:
--     zombie.scripting.objects.ScriptModule@173eb3bf and .G17_cat_Drum
-- That killed the whole sweep on the first handgun reached. Resolve the name
-- properly and accept either form.
local function moduleNameOf(item)
    local ok, name = pcall(function()
        local m = item:getModule()
        if type(m) == "string" then
            return m
        end
        if m and m.getName then
            return m:getName()
        end
        return nil
    end)
    if ok and type(name) == "string" and name ~= "" then
        return name
    end
    return "Base"
end

-- Best-effort sweep so remote players and dropped/held models are also right
-- from the start. Every item is guarded individually: one awkward entry must
-- not abort the sweep the way the getModule() concat did. A total failure here
-- is still harmless - applyToPlayer covers the case that actually matters.
function Grip.applyAll(player)
    player = player or getPlayer()
    local y = Grip.effectiveOffset(player)
    local count = 0
    local failed = 0

    local ok = pcall(function()
        local items = getScriptManager():getAllItems()
        if not items then
            return
        end
        for i = 0, items:size() - 1 do
            local applied = pcall(function()
                local item = items:get(i)
                if not item then
                    return
                end
                local sprite = item:getWeaponSprite()
                local anim = item:getSwingAnim()
                if sprite and sprite ~= "" and anim == "Handgun" then
                    if Grip.applyToSprite(moduleNameOf(item) .. "." .. tostring(sprite), y) then
                        count = count + 1
                    end
                end
            end)
            if not applied then
                failed = failed + 1
            end
        end
    end)

    if not ok then
        dbg("enumeration unavailable - relying on the on-equip path only")
        return
    end
    dbg("swept " .. count .. " handgun models at offset " .. tostring(y) .. ", " .. failed .. " skipped")
end

-- Called by AWCWF_Mod_Options.lua when either setting changes.
function Grip.onOptionChanged()
    lastWritten = {}
    local player = getPlayer()
    Grip.applyAll(player)
    Grip.applyToPlayer(player)

    -- applyAll has already written the held weapon's sprite, so applyToPlayer
    -- sees an unchanged value and skips its own rebuild. Force one here or the
    -- new offset does not appear until the player re-equips.
    if player then
        pcall(function()
            player:resetEquippedHandsModels()
        end)
    end
end

-- ----------------------------------------------------------------- events ---

Events.OnGameStart.Add(function()
    Grip.applyAll()
    Grip.applyToPlayer()
end)

Events.OnEquipPrimary.Add(function(character, item)
    if character and character == getPlayer() then
        Grip.applyToPlayer(character)
    end
end)
