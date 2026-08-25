-- ============================================================
-- PagerMod_Keybinds.lua
-- Rebindable hotkey (Options > Key Bindings > [Pager Network])
-- to open the pager you are carrying.
-- ============================================================

require "PagerMod_Shared"
require "PagerMod_Client"

local OPEN_LABEL  = "Open Pager"
local REPLY_LABEL = "Page Last Sender"

-- Register the bindings so they show up under Options > Key Bindings, in a
-- "[Pager Network]" section, where players can rebind or clear them. The
-- global keyBinding table is the engine's mechanism (vanilla MainOptions
-- reads it); entries whose value starts with "[" render as section headers.
-- Defaults: P opens the pager; "Page Last Sender" ships unbound (key 0).
local function ensureBind(entry)
    for _, kb in ipairs(keyBinding) do
        if kb.value == entry.value then return end -- already registered this session
    end
    table.insert(keyBinding, entry)
end

local function registerKeybinds()
    if not keyBinding then return end
    ensureBind({ value = "[Pager Network]" })
    ensureBind({ value = OPEN_LABEL,  key = Keyboard.KEY_P })
    ensureBind({ value = REPLY_LABEL, key = 0 })
end
registerKeybinds()

local function firstHeldPager()
    local pagers = PagerMod.getHeldPagers()
    for _, p in ipairs(pagers) do
        if p.number then return p.item end
    end
    return pagers[1] and pagers[1].item or nil
end

local function openCarriedPager()
    local target = firstHeldPager()
    if not target then return end

    if PagerMod.UI and PagerMod.UI:getIsVisible() then
        PagerMod.UI:close()
        return
    end
    if target:getModData().pagerNumber then
        PagerMod.OpenPagerUI(target)
    else
        -- Auto-activate, then open automatically once the number is assigned.
        PagerMod._openAfterAssign = true
        PagerMod.halo(getText("IGUI_PagerMod_Activating"))
        PagerMod.requestAssign(target, getText("IGUI_PagerMod_DefaultName"))
    end
end

local function pageLastSender()
    local target = firstHeldPager()
    if not target or not target:getModData().pagerNumber then
        PagerMod.halo(getText("IGUI_PagerMod_NeedActivate"), { r = 0.9, g = 0.7, b = 0.2 })
        return
    end
    if not PagerMod.lastSender then
        PagerMod.halo(getText("IGUI_PagerMod_NoLastSender"), { r = 0.9, g = 0.7, b = 0.2 })
        return
    end
    PagerMod.openComposeTo(target, PagerMod.lastSender)
end

local function onKeyPressed(key)
    if not getPlayer() then return end
    if key == 0 then return end
    if key == getCore():getKey(OPEN_LABEL) then
        openCarriedPager()
    elseif key == getCore():getKey(REPLY_LABEL) then
        pageLastSender()
    end
end

Events.OnKeyPressed.Add(onKeyPressed)

print("[PagerMod] Keybinds loaded")
