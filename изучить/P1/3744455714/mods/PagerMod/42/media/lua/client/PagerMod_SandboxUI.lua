-- ============================================================
-- PagerMod_SandboxUI.lua
-- Makes the "Preset" sandbox option live-seed the other PagerMod
-- controls in the options UI, the way the vanilla "Zombies" preset
-- live-updates the population sliders.
--
-- Both option screens (the new-game / host screen SandboxOptionsScreen
-- and the in-game admin ISServerSandboxOptionsUI) expose:
--   * self.controls[optionName] -> the UI control for each option
--   * self:onComboBoxSelected(combo, optionName) when a combo changes
-- We wrap that method so picking a preset writes its values straight
-- into the sibling controls. Presets only *seed* the controls here;
-- the individual options remain authoritative at runtime
-- (see shared/PagerMod_Shared.lua refreshConfig).
-- ============================================================

require "PagerMod_Shared"

-- Maps a PagerMod.Config key (as used in PagerMod.PRESETS) to the full sandbox
-- option name and how its control is read/written.
--   enum -> ISComboBox   (.selected is a 1-based index)
--   bool -> ISTickBox    (.selected[1] is a boolean)
--   int  -> ISTextEntryBox (:setText with a string)
local SEED_MAP = {
    maxMessages      = { opt = "PagerMod.MaxMessages",      kind = "int"  },
    messageMaxLength = { opt = "PagerMod.MessageMaxLength", kind = "int"  },
    locationSharing  = { opt = "PagerMod.LocationSharing",  kind = "enum" },
    readReceipts     = { opt = "PagerMod.ReadReceipts",     kind = "bool" },
    allowSOS         = { opt = "PagerMod.AllowSOS",         kind = "bool" },
    allowBlocking    = { opt = "PagerMod.AllowBlocking",    kind = "bool" },
    allowBroadcast   = { opt = "PagerMod.AllowBroadcast",   kind = "bool" },
    pagerMode        = { opt = "PagerMod.PagerMode",        kind = "enum" },
    signalMode       = { opt = "PagerMod.SignalMode",       kind = "enum" },
}

local function setControl(control, kind, value)
    if not control or value == nil then return end
    if kind == "bool" then
        control.selected[1] = value and true or false
    elseif kind == "enum" then
        control.selected = value
    elseif kind == "int" then
        control:setText(tostring(value))
    end
end

-- Seed every preset-controllable control for the chosen preset index.
-- Default (index 1) is intentionally a no-op: it leaves the controls untouched.
local function applyPreset(controls, presetIndex)
    if not controls then return end
    if presetIndex == PagerMod.Preset.DEFAULT then return end
    local preset = PagerMod.PRESETS[presetIndex]
    if not preset then return end

    local so = getSandboxOptions and getSandboxOptions()

    -- 1. Reset every key any preset can touch back to its sandbox default, so
    --    switching between presets always yields a clean, predictable state
    --    rather than a merge with the previously selected preset.
    if so then
        for _, m in pairs(SEED_MAP) do
            local option = so:getOptionByName(m.opt)
            if option then
                setControl(controls[m.opt], m.kind, option:getDefaultValue())
            end
        end
    end

    -- 2. Layer this preset's values on top.
    for key, value in pairs(preset) do
        local m = SEED_MAP[key]
        if m then setControl(controls[m.opt], m.kind, value) end
    end
end

-- Wrap a screen class's onComboBoxSelected so our preset combo seeds siblings.
local function hookScreen(klass)
    if not klass or klass.PagerMod_presetHook then return end
    klass.PagerMod_presetHook = true
    local original = klass.onComboBoxSelected
    function klass:onComboBoxSelected(combo, optionName)
        if original then original(self, combo, optionName) end
        if optionName == "PagerMod.Preset" and combo then
            applyPreset(self.controls, combo.selected)
        end
    end
end

local function installHooks()
    hookScreen(SandboxOptionsScreen)      -- new-game / host options
    hookScreen(ISServerSandboxOptionsUI)  -- in-game admin options
end

-- The screen classes are defined by the base game before mod client Lua loads,
-- so install immediately; re-run on boot as a safety net in case load order
-- ever changes.
installHooks()
Events.OnGameBoot.Add(installHooks)
