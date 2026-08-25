-- Festival modpack: Apocalypse + Custom Sandbox only; custom mode descriptions.
-- Patch only after the main menu exists (never during the initial "Loading Lua" pass).

if isServer() then return end

local MOD_ID = "ModpackFestivalSpawn"
local APOCALYPSE_DESC = "UI_ModpackFestival_ApocalypseDesc"
local SANDBOX_DESC = "UI_ModpackFestival_SandboxDesc"
local NPC_QUEST_PRESET_LABEL = "NPC&QUEST"
local NPC_QUEST_PRESET_FILE = "NPCQUEST"
local UI_BORDER_SPACING = 10
local MAX_CARD_WIDTH = 520

local allowedModes = nil

local function modeAllowed(mode)
    return mode and allowedModes and allowedModes[mode] == true
end

local function isRealModeEntry(entry)
    return entry and entry.mode and modeAllowed(entry.mode)
end

local function captureVanillaGameModeData()
    if NewGameScreen and NewGameScreen._ModpackFestivalVanillaGameModeData then
        return true
    end
    if not NewGameScreen or not NewGameScreen.defaultGameModeData then
        return false
    end
    local vanilla = {}
    for i = 1, #NewGameScreen.defaultGameModeData do
        local entry = NewGameScreen.defaultGameModeData[i]
        if entry and entry.mode then
            local copy = {}
            for k, v in pairs(entry) do
                copy[k] = v
            end
            vanilla[#vanilla + 1] = copy
        end
    end
    if #vanilla < 1 then
        return false
    end
    NewGameScreen._ModpackFestivalVanillaGameModeData = vanilla
    return true
end

local function buildAllowedModes()
    if allowedModes then return allowedModes end
    allowedModes = {}
    if GameMode and GameMode.APOCALYPSE and GameMode.SANDBOX then
        allowedModes[GameMode.APOCALYPSE:toString()] = true
        allowedModes[GameMode.SANDBOX:toString()] = true
    else
        allowedModes.Apocalypse = true
        allowedModes.Sandbox = true
    end
    return allowedModes
end

local function buildFestivalGameModeData()
    if not captureVanillaGameModeData() then
        return nil
    end
    buildAllowedModes()
    local apocalypseMode = GameMode and GameMode.APOCALYPSE and GameMode.APOCALYPSE:toString() or "Apocalypse"
    local sandboxMode = GameMode and GameMode.SANDBOX and GameMode.SANDBOX:toString() or "Sandbox"
    local vanilla = NewGameScreen._ModpackFestivalVanillaGameModeData
    local list = {}
    for i = 1, #vanilla do
        local entry = vanilla[i]
        if entry.mode and modeAllowed(entry.mode) then
            local copy = {}
            for k, v in pairs(entry) do
                copy[k] = v
            end
            if entry.mode == apocalypseMode then
                copy.title = "NPC&QUEST"
                copy.desc = APOCALYPSE_DESC
            elseif entry.mode == sandboxMode then
                copy.desc = SANDBOX_DESC
            end
            list[#list + 1] = copy
        end
    end
    return list
end

local function isApocalypseMode(mode)
    local apocalypseMode = GameMode and GameMode.APOCALYPSE and GameMode.APOCALYPSE:toString() or "Apocalypse"
    return mode == apocalypseMode or mode == "Apocalypse"
end

local function applyNpcQuestPresetOverride()
    local options = getSandboxOptions and getSandboxOptions() or nil
    if not options or not options.getOptionByName or not options.set then
        return false
    end

    local okPreset, presetTable = pcall(function()
        return require("Sandbox/" .. NPC_QUEST_PRESET_FILE)
    end)
    if not okPreset or type(presetTable) ~= "table" then
        return false
    end

    local function applyTable(prefix, tbl)
        for k, v in pairs(tbl) do
            local key = tostring(k)
            local optionName = (prefix and (prefix .. "." .. key)) or key
            if type(v) == "table" then
                applyTable(optionName, v)
            else
                local opt = options:getOptionByName(optionName)
                if opt then
                    options:set(optionName, v)
                end
            end
        end
    end

    applyTable(nil, presetTable)
    if options.toLua then
        options:toLua()
    end
    if getWorld and getWorld().setPreset then
        getWorld():setPreset(NPC_QUEST_PRESET_FILE)
    end
    return true
end

local function applyGameModeList(screen)
    if not screen then return end
    local data = buildFestivalGameModeData()
    if not data then return end
    screen.gameModeData = data
    screen.dataShift = 0
    screen.inChallengesView = false
end

local function hideModePanel(panel)
    if not panel then return end
    panel:setData(nil)
    panel:setVisible(false)
    panel:setSelected(false)
end

local function festivalUpdatePanels(self)
    applyGameModeList(self)
    local modes = self.gameModeData or {}

    for i = 1, #self.panels do
        local panel = self.panels[i]
        local data = modes[i]
        if data and isRealModeEntry(data) then
            panel:setData(data)
            panel:setVisible(true)
        else
            hideModePanel(panel)
        end
    end

    if self.selectedItem then
        local sel = self.selectedItem
        if not sel.data or not modeAllowed(sel.data.mode) or not sel:isVisible() then
            self.selectedItem = nil
            for _, panel in ipairs(self.panels) do
                if panel:isVisible() and panel.data and modeAllowed(panel.data.mode) then
                    self.selectedItem = panel
                    panel:setSelected(true)
                    break
                end
            end
        end
    end

    if not self.selectedItem then
        for _, panel in ipairs(self.panels) do
            if panel:isVisible() and panel.data and modeAllowed(panel.data.mode) then
                self.selectedItem = panel
                panel:setSelected(true)
                break
            end
        end
    end
end

local function relayoutVisibleModePanels(self)
    if not self.panels or not self.viewDimensions or not self.backButton then
        return
    end
    local viewY = self.viewDimensions.y
    local previewH = self.viewDimensions.previewHeight
    if viewY == nil or previewH == nil or not self.width or self.width <= 0 then
        return
    end
    local backY = self.backButton:getY()
    if backY == nil then
        return
    end

    local visible = {}
    for i = 1, #self.panels do
        local panel = self.panels[i]
        if panel and panel:isVisible() and panel.data and modeAllowed(panel.data.mode) then
            visible[#visible + 1] = panel
        end
    end
    local count = #visible
    if count == 0 then
        return
    end

    local marginUnderVideo = 6
    local y = viewY + previewH + UI_BORDER_SPACING + marginUnderVideo
    local h = backY - y - UI_BORDER_SPACING
    if not h or h < 32 then
        return
    end

    local gap = UI_BORDER_SPACING
    local slotW = (self.width - gap * (count + 1)) / count
    if slotW > MAX_CARD_WIDTH then
        slotW = MAX_CARD_WIDTH
    end
    if slotW < 32 then
        return
    end

    local rowW = count * slotW + (count - 1) * gap
    local startX = (self.width - rowW) / 2

    for i = 1, count do
        local panel = visible[i]
        panel:setX(startX + (i - 1) * (slotW + gap))
        panel:setY(y)
        panel:setWidth(slotW)
        panel:setHeight(h)
        if panel.updateView then
            panel:updateView()
        end
    end
end

local function festivalOnItemClick(self, item, x, y)
    if not item or not item.data or not modeAllowed(item.data.mode) then
        return
    end
    return NewGameScreen._ModpackFestivalOrigOnItemClick(self, item, x, y)
end

local function patchNewGameScreen()
    if not NewGameScreen then
        return false
    end
    if NewGameScreen._ModpackFestivalNewGamePatched then
        if MainScreen and MainScreen.instance and MainScreen.instance.soloScreen then
            local screen = MainScreen.instance.soloScreen
            festivalUpdatePanels(screen)
            pcall(relayoutVisibleModePanels, screen)
        end
        return true
    end
    if not captureVanillaGameModeData() then
        return false
    end

    NewGameScreen._ModpackFestivalNewGamePatched = true

    NewGameScreen._ModpackFestivalOrigNew = NewGameScreen.new
    NewGameScreen.new = function(x, y, width, height)
        local o = NewGameScreen._ModpackFestivalOrigNew(x, y, width, height)
        applyGameModeList(o)
        return o
    end

    NewGameScreen._ModpackFestivalOrigClickPlay = NewGameScreen.clickPlay
    NewGameScreen.clickPlay = function(self)
        local selectedMode = self.selectedItem and self.selectedItem.data and self.selectedItem.data.mode or nil
        if self.selectedItem and self.selectedItem.data and self.selectedItem.data.mode then
            if not modeAllowed(self.selectedItem.data.mode) then
                return
            end
        end
        local ret = NewGameScreen._ModpackFestivalOrigClickPlay(self)
        if isApocalypseMode(selectedMode) then
            if applyNpcQuestPresetOverride() then
                print("[" .. MOD_ID .. "] Apocalypse tile overridden to sandbox preset: " .. NPC_QUEST_PRESET_LABEL)
            else
                print("[" .. MOD_ID .. "] WARN: missing sandbox preset '" .. NPC_QUEST_PRESET_LABEL .. "'")
            end
        end
        return ret
    end

    NewGameScreen.clickChallenges = function(self)
        -- Challenges removed from this modpack flow.
    end

    NewGameScreen._ModpackFestivalOrigOnItemClick = NewGameScreen.onItemClick
    NewGameScreen.onItemClick = festivalOnItemClick

    NewGameScreen._ModpackFestivalOrigUpdatePanels = NewGameScreen.updatePanels
    NewGameScreen.updatePanels = function(self)
        festivalUpdatePanels(self)
        pcall(relayoutVisibleModePanels, self)
    end

    NewGameScreen._ModpackFestivalOrigOnResolutionChange = NewGameScreen.onResolutionChange
    NewGameScreen.onResolutionChange = function(self)
        NewGameScreen._ModpackFestivalOrigOnResolutionChange(self)
        pcall(relayoutVisibleModePanels, self)
    end

    NewGameScreen._ModpackFestivalOrigSetVisible = NewGameScreen.setVisible
    NewGameScreen.setVisible = function(self, visible, joypadData)
        NewGameScreen._ModpackFestivalOrigSetVisible(self, visible, joypadData)
        if visible then
            festivalUpdatePanels(self)
            pcall(relayoutVisibleModePanels, self)
            if self.updatePreview then
                self:updatePreview()
            end
        end
    end

    if MainScreen and MainScreen.instance and MainScreen.instance.soloScreen then
        local screen = MainScreen.instance.soloScreen
        festivalUpdatePanels(screen)
        pcall(relayoutVisibleModePanels, screen)
    end

    print("[" .. MOD_ID .. "] new game screen: Apocalypse + Custom Sandbox only")
    return true
end

Events.OnMainMenuEnter.Add(patchNewGameScreen)
