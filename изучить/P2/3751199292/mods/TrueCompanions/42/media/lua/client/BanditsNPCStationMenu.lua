--
-- True Companions - Workstation world context menu (client)
--
-- Right-clicking a placed station tile shows "<Station> (Tier N)" with the next
-- Upgrade step and a deliberate Remove (whole group). Vanilla teardown options are
-- stripped so the station isn't dismantled/picked up by accident.
--

local S = BanditsNPC.Stations

-- context-menu callbacks: onSelect(target=player, param1, param2, ...)
function S._ctxUpgrade(player, stationType, info)
    local ok, msg = S.BeginUpgrade(player, stationType, { groupId = info.groupId, tier = info.tier, origin = info.origin })
    if msg then pcall(function() player:Say(msg) end) end
end

function S._ctxRemove(player, info)
    S.RemoveGroup(info.groupId, info.origin.x, info.origin.y, info.origin.z)
    pcall(function() player:Say(BanditsNPC.T("UI_BN_Ws_Removed", "Workstation removed.")) end)
end

-- disable vanilla "pick up / disassemble" options so the station can't be torn down by accident
-- ONLY KEYS THAT EXIST (v0.75.36). This listed NINE and SEVEN OF THEM ARE NOT REAL --
-- checked every one against the shipped EN translations: ContextMenu_DisassembleObject,
-- _GrabHandful, _PickUpFurniture, _Pickup, _Pick_up, _MoveFurniture and _PlaceFurniture
-- do not exist in B42. getText() echoes a missing key back, so each produced a kill entry
-- matching an option named "ContextMenu_Pickup", which nothing is. The list LOOKED
-- thorough and protected against two things.
--
-- KNOWN GAP, stated rather than papered over: furniture pick-up and placement in B42 go
-- through the MOVEABLES system, whose menu entries are not built from a
-- ContextMenu_* key this approach can match. So a determined player can still move a
-- workstation via Moveables. Closing that needs a different hook (the moveable cursor
-- itself), not more strings -- and inventing plausible-looking keys here is exactly what
-- produced the false sense of coverage in the first place.
local TEARDOWN_KEYS = {
    "ContextMenu_Dismantle", "ContextMenu_Disassemble",
}
-- A KEY THAT NO LONGER EXISTS MUST NOT FAIL SILENTLY (v0.75.36). getText() ECHOES THE KEY
-- BACK when the translation is missing, so a renamed or removed vanilla key produced
-- kill["ContextMenu_Pickup"] -- which matches no option, protects nothing, and looks
-- exactly like working code. That is how a station becomes accidentally dismantlable
-- after a PZ update, with nothing in the log. getTextOrNull returns nil instead of
-- echoing, so a dead key is detectable; report it once and carry on protecting with the
-- keys that still resolve.
local warnedTeardownKeys = false

function S.StripTeardownOptions(context)
    if not (context and context.options) then return end
    local kill = {}
    local dead = {}
    for _, key in ipairs(TEARDOWN_KEYS) do
        local t
        pcall(function() t = getTextOrNull(key) end)
        if type(t) == "string" and t ~= "" and t ~= key then
            kill[t] = true
        else
            dead[#dead + 1] = key
        end
    end
    if #dead > 0 and not warnedTeardownKeys then
        warnedTeardownKeys = true
        print("[BanditsNPC] WARNING: these vanilla context-menu keys no longer resolve, so"
            .. " workstations are NOT protected from those options: "
            .. table.concat(dead, ", ") .. ". Please report this line.")
    end
    for _, opt in ipairs(context.options) do
        if opt and opt.name and kill[opt.name] then opt.notAvailable = true end
    end
end

local function findWorkstation(worldobjects)
    if not worldobjects then return nil end
    for i = 1, #worldobjects do
        local o = worldobjects[i]
        if o and o.getModData and o:getModData().npcWorkstation then return o end
    end
    return nil
end

local function onFillWorldObjectContextMenu(playerNum, context, worldobjects, test)
    if test then return end
    local player = getSpecificPlayer(playerNum)
    if not player then return end
    local ws = findWorkstation(worldobjects)
    if not ws then return end

    local md = ws:getModData()
    local stationType = md.npcWorkstation
    local tier = md.npcWorkstationTier or 1
    local groupId = md.npcWorkstationGroup
    local sq = ws:getSquare()
    if not sq then return end
    local origin = { x = sq:getX(), y = sq:getY(), z = sq:getZ() }
    local label = S.Label(stationType)

    S.StripTeardownOptions(context)

    local title = context:addOption(BanditsNPC.TF("UI_BN_Ws_TierHdr", "%1 (Tier %2)", label, tier), nil, nil)
    title.notAvailable = true

    local up = S.NextUpgrade(stationType, tier)
    if up then
        local okSkill, sm = S.MeetsSkill(player, up.skill)
        local okMat, mm = S.HasMaterials(player, up.materials)
        local hasHammer = S.HasHammer(player)
        local verb = (up.kind == "add") and BanditsNPC.T("UI_BN_Ws_Add", "Add") or BanditsNPC.T("UI_BN_Ws_Upgrade", "Upgrade")
        local opt = context:addOption(BanditsNPC.TF("UI_BN_Ws_UpgradeTo", "%1 -> Tier %2 (%3)", verb, up.toTier, up.piece.label),
            player, S._ctxUpgrade, stationType, { groupId = groupId, tier = tier, origin = origin })
        local tip = ISToolTip:new()
        tip:setName(BanditsNPC.TF("UI_BN_Ws_VerbTip", "%1: %2", verb, up.piece.label))
        local addIntro = up.piece.onBench and BanditsNPC.T("UI_BN_Ws_AddBench", "Place it on the station's bench (or beside it).") or BanditsNPC.T("UI_BN_Ws_AddBeside", "Place a new piece next to the station.")
        local txt = (up.kind == "add") and addIntro or BanditsNPC.T("UI_BN_Ws_Rebuild", "Rebuild this piece in place (walk over + hammer).")
        txt = txt .. " <LINE> " .. BanditsNPC.TF("UI_BN_Ws_Materials", "Materials: %1", S.MaterialText(up.materials))
        local sk = S.SkillText(up.skill)
        txt = txt .. " <LINE> " .. BanditsNPC.TF("UI_BN_Ws_Skill", "Skill: %1", (sk ~= "" and sk or BanditsNPC.T("UI_BN_Ws_None", "none")))
        txt = txt .. " <LINE> " .. BanditsNPC.T("UI_BN_Ws_NeedsHammer", "Needs a hammer.")
        if up.power then txt = txt .. " <LINE> " .. BanditsNPC.T("UI_BN_Ws_NeedsPower", "Needs power to operate (grid power or a running generator).") end
        if not okSkill then txt = txt .. " <LINE> <RGB:1,0.4,0.4> " .. BanditsNPC.TF("UI_BN_Ws_NeedSkill", "Need %1", sm) end
        if not okMat then txt = txt .. " <LINE> <RGB:1,0.4,0.4> " .. BanditsNPC.TF("UI_BN_Ws_Missing", "Missing: %1", mm) end
        if not hasHammer then txt = txt .. " <LINE> <RGB:1,0.4,0.4> " .. BanditsNPC.T("UI_BN_Ws_NeedHammer", "Need a hammer.") end
        tip.description = txt
        opt.toolTip = tip
        if not (okSkill and okMat and hasHammer) then opt.notAvailable = true end
    else
        local maxed = context:addOption(BanditsNPC.TF("UI_BN_Ws_Maxed", "Fully upgraded (Tier %1)", tier), nil, nil)
        maxed.notAvailable = true
    end

    context:addOption(BanditsNPC.T("UI_BN_RemoveWorkstation", "Remove Workstation"), player, S._ctxRemove, { groupId = groupId, origin = origin })
end

Events.OnFillWorldObjectContextMenu.Add(onFillWorldObjectContextMenu)
