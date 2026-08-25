require "ISUI/ISPanel"
require "ISUI/ISPanelJoypad"
require "ISUI/ISButton"
require "ExtractionMode/Upgrades"
require "ExtractionMode/Generator"
require "ExtractionMode/Util"
require "ExtractionMode/Infection"
require "ExtractionMode/Localization"

ExtractionMode = ExtractionMode or {}
local Upgrades = ExtractionMode.Upgrades
local Generator = ExtractionMode.Generator
local Util = ExtractionMode.Util
local Infection = ExtractionMode.Infection
local Localization = ExtractionMode.Localization
local Panel = ISPanelJoypad:derive("ExtractionModeUpgradePanel")
local LAYOUT_VERSION = 6
local PANEL_WIDTH = 960
local HEADER_HEIGHT = 60
local GENERATOR_HEIGHT = 136
local UPGRADE_HEADER_HEIGHT = 24
local CATEGORY_HEIGHT = 42
local ROW_HEIGHT = 170
local ACTION_BUTTON_WIDTH = 132
local UPGRADE_START = HEADER_HEIGHT + GENERATOR_HEIGHT + UPGRADE_HEADER_HEIGHT + CATEGORY_HEIGHT
local LIST_WIDTH = PANEL_WIDTH - 24
local ACTION_BUTTON_X = LIST_WIDTH - ACTION_BUTTON_WIDTH - 12
local UPGRADE_TEXT_WIDTH = ACTION_BUTTON_X - 24
local DETAIL_TEXT_WIDTH = LIST_WIDTH - 52

local function localPlayer(playerNum)
    return getSpecificPlayer and getSpecificPlayer(playerNum or 0) or (getPlayer and getPlayer())
end

local function clientState(playerNum)
    if playerNum == nil and ExtractionMode.UpgradePanelInstance then
        playerNum = ExtractionMode.UpgradePanelInstance.playerNum
    end
    if ExtractionMode.Client and ExtractionMode.Client.stateFor then
        return ExtractionMode.Client.stateFor(playerNum or 0)
    end
    return ExtractionMode.ClientState or {}
end

local function raidReferenceState(data)
    return data.state == ExtractionMode.Config.STATE_RAID
        or data.state == ExtractionMode.Config.STATE_EXTRACTING
        or data.state == ExtractionMode.Config.STATE_BOARDING
end

local function send(playerNum, command, args)
    local player = localPlayer(playerNum)
    if player and ExtractionMode.Client and ExtractionMode.Client.sendCommand then
        ExtractionMode.Client.sendCommand(player, command, args or {})
    end
end

local function oneDecimal(value)
    return string.format("%.1f", tonumber(value) or 0)
end

local function wrapText(text, font, maximumWidth)
    local lines = {}
    local current = ""
    local manager = getTextManager and getTextManager()
    if manager == nil then return { tostring(text or "") } end
    for word in tostring(text or ""):gmatch("%S+") do
        local candidate = current == "" and word or (current .. " " .. word)
        if current ~= "" and manager:MeasureStringX(font, candidate) > maximumWidth then
            lines[#lines + 1] = current
            current = word
        else
            current = candidate
        end
    end
    if current ~= "" then lines[#lines + 1] = current end
    if #lines == 0 then lines[1] = "" end
    return lines
end

local function drawRequirementProgress(canvas, prefix, parts, x, y, maximumWidth, maximumLines)
    local manager = getTextManager and getTextManager()
    if manager == nil then return end
    local font = UIFont.Small
    local line = 1
    local cursorX = x
    local right = x + maximumWidth

    local function width(text)
        return manager:MeasureStringX(font, tostring(text or ""))
    end

    local function draw(text, r, g, b)
        canvas:drawText(text, cursorX, y + (line - 1) * 18, r, g, b, 1, font)
        cursorX = cursorX + width(text)
    end

    draw(prefix, 0.82, 0.82, 0.82)
    for index, part in ipairs(parts or {}) do
        local separator = index > 1 and "   |   " or ""
        local segmentWidth = width(separator) + width(part.text)
        if cursorX > x and cursorX + segmentWidth > right then
            line = line + 1
            cursorX = x
            separator = ""
        end
        if line > maximumLines then return end
        if separator ~= "" then draw(separator, 0.58, 0.58, 0.58) end
        if part.met == true then
            draw(part.text, 0.96, 0.72, 0.18)
        else
            draw(part.text, 0.95, 0.45, 0.30)
        end
    end
end

local UpgradeList = ISPanel:derive("ExtractionModeUpgradeList")

function UpgradeList:prerender()
    self:setStencilRect(0, 0, self.width, self.height)
    ISPanel.prerender(self)
end

function UpgradeList:render()
    ISPanel.render(self)
    if self.owner then self.owner:renderUpgradeRows(self) end
    self:clearStencilRect()
end

function UpgradeList:onMouseWheel(delta)
    self:setYScroll(self:getYScroll() - delta * 42)
    return true
end

function UpgradeList:new(x, y, width, height, owner)
    local object = ISPanel:new(x, y, width, height)
    setmetatable(object, self)
    self.__index = self
    object.owner = owner
    object.background = false
    object.borderColor = { r = 0, g = 0, b = 0, a = 0 }
    object.moveWithMouse = false
    return object
end

function Panel:onClose()
    local returnToController = self.returnToController == true
    local playerNum = self.playerNum or 0
    self.returnToController = false
    if self.joyfocus and setJoypadFocus then setJoypadFocus(self.playerNum or 0, nil) end
    self:setVisible(false)
    if returnToController and ExtractionMode.openControllerPanel then
        ExtractionMode.openControllerPanel(playerNum)
    end
end

function Panel:onInstall(button)
    if button and button.upgradeId then send(self.playerNum, "InstallUpgrade", { upgradeId = button.upgradeId }) end
end

function Panel:onDebugComplete(button)
    if button and button.upgradeId then send(self.playerNum, "DebugCompleteUpgrade", { upgradeId = button.upgradeId }) end
end

function Panel:onCategory(button)
    if button and button.categoryId then
        self.activeCategory = button.categoryId
        self:layoutCategory()
        self:autoGenerateJoypadButtonsLists()
    end
end

function Panel:onAddFuel()
    send(self.playerNum, "AddGeneratorFuel", {})
end

function Panel:onDebugFillGenerator()
    send(self.playerNum, "DebugFillGenerator", {})
end

function Panel:onToggleGenerator()
    local generator = clientState().generator or {}
    send(self.playerNum, "SetGeneratorRunning", { running = generator.running ~= true })
end

function Panel:createChildren()
    ISPanelJoypad.createChildren(self)
    self.installButtons = {}
    self.debugButtons = {}
    self.categoryButtons = {}
    self.activeCategory = self.activeCategory or "utilities"

    self.addFuelButton = ISButton:new(20, HEADER_HEIGHT + 91, 170, 30,
        Localization.get("IGUI_ExtractionMode_AddGasoline", "ADD GASOLINE"), self, Panel.onAddFuel)
    self.addFuelButton:initialise()
    self.addFuelButton:instantiate()
    self:addChild(self.addFuelButton)

    self.debugFillFuelButton = ISButton:new(380, HEADER_HEIGHT + 91, 170, 30,
        Localization.get("IGUI_ExtractionMode_DebugFillTank", "DEBUG: FILL TANK"), self, Panel.onDebugFillGenerator)
    self.debugFillFuelButton:initialise()
    self.debugFillFuelButton:instantiate()
    self.debugFillFuelButton:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_DebugFillTank",
        "DEBUG: Fill the generator tank without consuming carried gasoline."))
    self:addChild(self.debugFillFuelButton)

    self.generatorToggleButton = ISButton:new(200, HEADER_HEIGHT + 91, 170, 30,
        Localization.get("IGUI_ExtractionMode_StartGenerator", "START GENERATOR"), self, Panel.onToggleGenerator)
    self.generatorToggleButton:initialise()
    self.generatorToggleButton:instantiate()
    self:addChild(self.generatorToggleButton)

    local categories = Upgrades.categories()
    local categoryWidth = math.floor((PANEL_WIDTH - 24) / #categories)
    for index, category in ipairs(categories) do
        local button = ISButton:new(12 + (index - 1) * categoryWidth,
            HEADER_HEIGHT + GENERATOR_HEIGHT + UPGRADE_HEADER_HEIGHT,
            categoryWidth, 32, category.label, self, Panel.onCategory)
        button:initialise()
        button:instantiate()
        button.categoryId = category.id
        self:addChild(button)
        self.categoryButtons[category.id] = button
    end

    self.upgradeList = UpgradeList:new(12, UPGRADE_START, LIST_WIDTH,
        self.height - UPGRADE_START - 8, self)
    self.upgradeList:initialise()
    self.upgradeList:instantiate()
    self.upgradeList:setScrollChildren(true)
    self.upgradeList:addScrollBars(false)
    self.upgradeList.vscroll.doSetStencil = true
    self:addChild(self.upgradeList)

    for _, definition in ipairs(Upgrades.definitions()) do
        local button = ISButton:new(ACTION_BUTTON_X, 12, ACTION_BUTTON_WIDTH, 30,
            Localization.get("IGUI_ExtractionMode_Install", "INSTALL"), self, Panel.onInstall)
        button:initialise()
        button:instantiate()
        button.upgradeId = definition.id
        self.upgradeList:addChild(button)
        self.installButtons[definition.id] = button

        local debugButton = ISButton:new(ACTION_BUTTON_X, 50, ACTION_BUTTON_WIDTH, 30,
            Localization.get("IGUI_ExtractionMode_AutoComplete", "AUTO COMPLETE"), self, Panel.onDebugComplete)
        debugButton:initialise()
        debugButton:instantiate()
        debugButton.upgradeId = definition.id
        debugButton:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_AutoCompleteUpgrade",
            "DEBUG: Install this upgrade without prerequisites, skills, or materials."))
        self.upgradeList:addChild(debugButton)
        self.debugButtons[definition.id] = debugButton
    end

    self.closeButton = ISButton:new(PANEL_WIDTH - 38, 8, 28, 28, "X", self, Panel.onClose)
    self.closeButton:initialise()
    self.closeButton:instantiate()
    self:addChild(self.closeButton)
    self:applyModeLayout()
    self:layoutCategory()
    self:autoGenerateJoypadButtonsLists()
end

function Panel:applyModeLayout()
    local offset = self.readOnlyRaid == true and -GENERATOR_HEIGHT or 0
    local categoryY = HEADER_HEIGHT + GENERATOR_HEIGHT + UPGRADE_HEADER_HEIGHT + offset
    local listY = UPGRADE_START + offset
    for _, button in pairs(self.categoryButtons or {}) do button:setY(categoryY) end
    if self.upgradeList then
        self.upgradeList:setY(listY)
        self.upgradeList:setHeight(self.height - listY - 8)
        if self.upgradeList.vscroll then
            self.upgradeList.vscroll:setHeight(self.upgradeList.height)
        end
    end
end

function Panel:layoutCategory()
    local visibleIndex = 0
    local state = clientState()
    for _, definition in ipairs(Upgrades.definitions()) do
        local installed = Upgrades.isInstalled(state.upgrades, definition.id)
        local visible = definition.category == self.activeCategory
            and (self.readOnlyRaid ~= true or not installed)
        local button = self.installButtons and self.installButtons[definition.id]
        local debugButton = self.debugButtons and self.debugButtons[definition.id]
        if button then
            button:setVisible(visible and self.readOnlyRaid ~= true)
            if visible then
                button:setY(visibleIndex * ROW_HEIGHT + 12)
                visibleIndex = visibleIndex + 1
            end
        end
        if debugButton then
            debugButton:setVisible(visible and self.readOnlyRaid ~= true
                and state.debugEnabled == true)
            if visible then debugButton:setY((visibleIndex - 1) * ROW_HEIGHT + 50) end
        end
    end
    if self.upgradeList then
        self.upgradeList:setScrollHeight(math.max(self.upgradeList.height, visibleIndex * ROW_HEIGHT))
        self.upgradeList:setYScroll(0)
    end
    for categoryId, button in pairs(self.categoryButtons or {}) do
        button.backgroundColor = categoryId == self.activeCategory
            and { r = 0.35, g = 0.24, b = 0.06, a = 1 }
            or { r = 0.10, g = 0.10, b = 0.10, a = 1 }
    end
end

function Panel:refreshInventoryCounts()
    local now = Util.nowMs()
    if self.lastCountRefresh and now - self.lastCountRefresh < 500 then return end
    self.lastCountRefresh = now
    self.requirementCounts = {}
    self.requirementsAvailable = {}
    local player = localPlayer(self.playerNum)
    local inventory = player and player:getInventory()
    self.gasolineAvailable = Generator.availableGasoline(inventory)
    for _, definition in ipairs(Upgrades.definitions()) do
        local counts = {}
        local available = inventory ~= nil
        for index, requirement in ipairs(definition.requirements or {}) do
            counts[index] = Upgrades.requirementCount(inventory, requirement)
            if counts[index] < (tonumber(requirement.amount) or 0) then available = false end
        end
        self.requirementCounts[definition.id] = counts
        self.requirementsAvailable[definition.id] = available
    end
end

function Panel:ensureVisible()
    if not self.joyfocus then return end
    local children = self:getVisibleChildren(self.joypadIndexY)
    local child = children[self.joypadIndex]
    local list = child and child.parent == self.upgradeList and self.upgradeList or nil
    if list == nil then
        ISPanelJoypad.ensureVisible(self)
        return
    end
    local padding = 20
    local childY = child:getY()
    local scroll = list:getYScroll()
    if childY + scroll < padding then
        scroll = padding - childY
    elseif childY + child:getHeight() + scroll > list:getHeight() - padding then
        scroll = list:getHeight() - padding - childY - child:getHeight()
    end
    local minimum = math.min(0, list:getHeight() - list:getScrollHeight())
    list:setYScroll(math.max(minimum, math.min(0, scroll)))
end

function Panel:prerender()
    self:stayOnSplitScreen(self.playerNum or 0)
    local referenceMode = raidReferenceState(clientState())
    if self.readOnlyRaid ~= referenceMode then
        self.readOnlyRaid = referenceMode
        self:applyModeLayout()
        self:layoutCategory()
        self:autoGenerateJoypadButtonsLists()
    end
    self:refreshInventoryCounts()
    local state = clientState()
    local generator = state.generator or {}
    local player = localPlayer(self.playerNum)
    local inHideout = state.state == ExtractionMode.Config.STATE_HIDEOUT
        and Infection.playerInsideHideout(player)
    local fuel = tonumber(generator.fuel) or 0
    local capacity = tonumber(generator.capacity) or Generator.capacity()
    local tankHasRoom = fuel < capacity - 0.0001

    self.addFuelButton:setVisible(self.readOnlyRaid ~= true)
    self.addFuelButton.enable = inHideout and tankHasRoom and (self.gasolineAvailable or 0) > 0.0001
    self.addFuelButton:setTitle(Localization.get("IGUI_ExtractionMode_AddUpToLiters", "ADD UP TO %1 L",
        oneDecimal(generator.transferLiters or Generator.transferLimit())))
    self.addFuelButton:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_AddGasoline",
        "Drain pure gasoline from carried containers, including containers inside bags."))

    self.debugFillFuelButton:setVisible(self.readOnlyRaid ~= true and state.debugEnabled == true)
    self.debugFillFuelButton.enable = state.debugEnabled == true and inHideout and tankHasRoom

    self.generatorToggleButton:setTitle(generator.running == true
        and Localization.get("IGUI_ExtractionMode_StopGenerator", "STOP GENERATOR")
        or Localization.get("IGUI_ExtractionMode_StartGenerator", "START GENERATOR"))
    self.generatorToggleButton.enable = inHideout and (generator.running == true or fuel > 0.0001)
    self.generatorToggleButton:setVisible(self.readOnlyRaid ~= true)
    self.generatorToggleButton:setTooltip(generator.running == true
        and Localization.get("IGUI_ExtractionMode_Tooltip_StopGenerator",
            "Shut down electricity and water to conserve fuel.")
        or Localization.get("IGUI_ExtractionMode_Tooltip_StartGenerator",
            "Supply electricity and replenish piped water while gasoline remains."))

    for _, definition in ipairs(Upgrades.definitions()) do
        local installed = Upgrades.isInstalled(state.upgrades, definition.id)
        local prerequisitesMet = Upgrades.prerequisitesMet(state.upgrades, definition)
        local skillsMet = Upgrades.upgradeSkillRequirementsMet(player, definition)
        local available = inHideout and not installed and prerequisitesMet and skillsMet
            and self.requirementsAvailable[definition.id] == true
        local button = self.installButtons[definition.id]
        local debugButton = self.debugButtons[definition.id]
        button:setTitle(installed and Localization.get("IGUI_ExtractionMode_Installed", "INSTALLED")
            or Localization.get("IGUI_ExtractionMode_Install", "INSTALL"))
        button.enable = available
        button:setVisible(definition.category == self.activeCategory and self.readOnlyRaid ~= true)
        debugButton:setVisible(definition.category == self.activeCategory
            and self.readOnlyRaid ~= true and state.debugEnabled == true)
        debugButton.enable = state.debugEnabled == true and not installed
        if installed then
            button:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_UpgradeInstalled",
                "This upgrade is permanently installed for the shared hideout."))
        elseif not prerequisitesMet then
            button:setTooltip(Localization.get("IGUI_ExtractionMode_Requires", "Requires: %1", table.concat(
                Upgrades.missingPrerequisiteNames(state.upgrades, definition), ", ")))
        elseif not skillsMet then
            button:setTooltip(Localization.get("IGUI_ExtractionMode_Requires", "Requires: %1", table.concat(
                Upgrades.missingUpgradeSkillNames(player, definition), ", ")))
        elseif not inHideout then
            button:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_UpgradeHideoutOnly",
                "Upgrades can only be installed while the raid system is idle in the hideout."))
        elseif not available then
            button:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_UpgradeMaterialsMissing",
                "Carry all required materials in your inventory or nested bags."))
        else
            button:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_InstallUpgrade",
                "Consume the required materials and install this shared upgrade."))
        end
    end
    local navigationKey = tostring(self.readOnlyRaid == true) .. ":" .. tostring(state.debugEnabled == true)
    if self.navigationKey ~= navigationKey then
        self.navigationKey = navigationKey
        self:autoGenerateJoypadButtonsLists()
    end
    ISPanel.prerender(self)
end

function Panel:renderGenerator(state)
    local generator = state.generator or {}
    local fuel = math.max(0, tonumber(generator.fuel) or 0)
    local capacity = math.max(1, tonumber(generator.capacity) or Generator.capacity())
    local ratio = math.max(0, math.min(1, fuel / capacity))
    local y = HEADER_HEIGHT

    self:drawRect(12, y, PANEL_WIDTH - 24, GENERATOR_HEIGHT - 8, 0.42, 0.06, 0.07, 0.08)
    self:drawRectBorder(12, y, PANEL_WIDTH - 24, GENERATOR_HEIGHT - 8, 0.65, 0.48, 0.38, 0.16)
    self:drawText(Localization.get("IGUI_ExtractionMode_HideoutGenerator", "HIDEOUT GENERATOR"),
        24, y + 10, 1, 0.78, 0.28, 1, UIFont.Medium)
    local status = generator.running == true
        and Localization.get("IGUI_ExtractionMode_GeneratorRunning", "RUNNING - UTILITIES ONLINE")
        or Localization.get("IGUI_ExtractionMode_GeneratorStopped", "STOPPED - UTILITIES OFFLINE")
    self:drawTextRight(status, PANEL_WIDTH - 24, y + 13, generator.running == true and 0.35 or 0.95,
        generator.running == true and 0.9 or 0.4, generator.running == true and 0.4 or 0.3, 1, UIFont.Small)

    self:drawRect(24, y + 41, PANEL_WIDTH - 48, 16, 0.65, 0.03, 0.03, 0.03)
    self:drawRect(25, y + 42, (PANEL_WIDTH - 50) * ratio, 14, 0.85, 0.72, 0.43, 0.12)
    self:drawRectBorder(24, y + 41, PANEL_WIDTH - 48, 16, 0.8, 0.55, 0.55, 0.55)
    self:drawText(Localization.get("IGUI_ExtractionMode_GeneratorTank",
            "Tank: %1 / %2 L    Carried gasoline: %3 L", oneDecimal(fuel), oneDecimal(capacity),
            oneDecimal(self.gasolineAvailable)),
        24, y + 62, 0.85, 0.85, 0.85, 1, UIFont.Small)

    local remainingHours = tonumber(generator.hoursRemaining) or 0
    local remainingText = remainingHours >= 24
        and Localization.get("IGUI_ExtractionMode_InGameDays", "%1 in-game days",
            oneDecimal(remainingHours / 24))
        or Localization.get("IGUI_ExtractionMode_InGameHours", "%1 in-game hours",
            oneDecimal(remainingHours))
    local standbyText = generator.standby == true
        and Localization.get("IGUI_ExtractionMode_StandbySuffix", " (standby)") or ""
    self:drawText(Localization.get("IGUI_ExtractionMode_GeneratorConsumption",
            "Consumption: %1 L/day%2    Estimated fuel: %3",
            oneDecimal(generator.fuelPerDay), standbyText, remainingText),
        390, y + 62, 0.72, 0.72, 0.72, 1, UIFont.Small)
end

function Panel:renderUpgradeRows(canvas)
    local state = clientState()
    local player = localPlayer(self.playerNum)
    local y = 0
    local definitions = {}
    for _, definition in ipairs(Upgrades.definitionsForCategory(self.activeCategory)) do
        if self.readOnlyRaid ~= true or not Upgrades.isInstalled(state.upgrades, definition.id) then
            definitions[#definitions + 1] = definition
        end
    end
    if #definitions == 0 then
        canvas:drawTextCentre(Localization.get("IGUI_ExtractionMode_NoUnfinishedUpgrades",
                "NO UNFINISHED UPGRADES IN THIS CATEGORY"), (LIST_WIDTH - 14) / 2, 36,
            0.7, 0.7, 0.7, 1, UIFont.Medium)
        return
    end
    for _, definition in ipairs(definitions) do
        local installed = Upgrades.isInstalled(state.upgrades, definition.id)
        local prerequisitesMet = Upgrades.prerequisitesMet(state.upgrades, definition)
        canvas:drawRect(0, y, LIST_WIDTH - 14, ROW_HEIGHT - 8, 0.34, 0.08, 0.09, 0.10)
        canvas:drawRectBorder(0, y, LIST_WIDTH - 14, ROW_HEIGHT - 8, 0.55, 0.55, 0.42, 0.20)
        canvas:drawText(Upgrades.name(definition), 12, y + 12, installed and 0.38 or 1,
            installed and 0.82 or 1, installed and 0.42 or 1, 1, UIFont.Medium)
        local descriptionWidth = self.readOnlyRaid == true and DETAIL_TEXT_WIDTH or UPGRADE_TEXT_WIDTH
        local descriptionLines = wrapText(Upgrades.description(definition), UIFont.Small, descriptionWidth)
        for lineIndex = 1, math.min(2, #descriptionLines) do
            canvas:drawText(descriptionLines[lineIndex], 12, y + 40 + (lineIndex - 1) * 18,
                0.82, 0.82, 0.82, 1, UIFont.Small)
        end

        -- Installed upgrades show only their active status. Their former material,
        -- prerequisite, and skill requirements are intentionally omitted.
        if installed then
            canvas:drawText(Localization.get("IGUI_ExtractionMode_UpgradeActive",
                    "INSTALLED - Active for the shared hideout"), 12, y + 92,
                0.38, 0.88, 0.46, 1, UIFont.Small)
        else
            local requirementParts = {}
            local counts = self.requirementCounts and self.requirementCounts[definition.id] or {}
            for index, requirement in ipairs(definition.requirements or {}) do
                local amount = math.max(0, math.floor(tonumber(requirement.amount) or 0))
                local count = tonumber(counts[index]) or 0
                requirementParts[#requirementParts + 1] = {
                    text = Upgrades.label(requirement) .. ": "
                        .. tostring(math.min(count, amount)) .. "/" .. tostring(amount),
                    met = count >= amount,
                }
            end
            local detailY = y + 82
            if not prerequisitesMet then
                canvas:drawText(Localization.get("IGUI_ExtractionMode_Prerequisite", "Prerequisite: %1",
                    table.concat(Upgrades.missingPrerequisiteNames(state.upgrades, definition), ", ")),
                    12, detailY, 0.95, 0.45, 0.3, 1, UIFont.Small)
                detailY = detailY + 20
            end

            drawRequirementProgress(canvas,
                Localization.get("IGUI_ExtractionMode_Materials", "Materials: %1", ""),
                requirementParts, 12, detailY, DETAIL_TEXT_WIDTH, 2)

            local skillText = {}
            local skillsMet = true
            for _, requirement in ipairs(definition.skillRequirements or {}) do
                local required = math.max(0, math.floor(tonumber(requirement.level) or 0))
                local current = Upgrades.skillLevel(player, requirement)
                if current < required then skillsMet = false end
                skillText[#skillText + 1] = Upgrades.label(requirement) .. ": "
                    .. tostring(current) .. "/" .. tostring(required)
            end
            canvas:drawText(Localization.get("IGUI_ExtractionMode_Skills", "Skills: %1",
                    table.concat(skillText, "   |   ")), 12, y + 138,
                skillsMet and 0.35 or 0.95, skillsMet and 0.9 or 0.45,
                skillsMet and 0.4 or 0.3, 1, UIFont.Small)
        end
        y = y + ROW_HEIGHT
    end
end

function Panel:render()
    ISPanel.render(self)
    self:drawTextCentre(Localization.get("IGUI_ExtractionMode_UpgradesTitle",
            "HIDEOUT UTILITIES & UPGRADES"), PANEL_WIDTH / 2, 12,
        0.96, 0.72, 0.18, 1, UIFont.Medium)
    self:drawTextCentre(self.readOnlyRaid == true
            and Localization.get("IGUI_ExtractionMode_UpgradeReferenceReadOnly",
                "READ ONLY DURING RAID - Carried counts update live; completed requirements turn gold.")
            or Localization.get("IGUI_ExtractionMode_UpgradesSubtitle",
                "Fuel and installation materials are taken from the contributing survivor."),
        PANEL_WIDTH / 2, 38, 0.8, 0.8, 0.8, 1, UIFont.Small)

    local state = clientState()
    if self.readOnlyRaid ~= true then self:renderGenerator(state) end
    self:drawText(Localization.get("IGUI_ExtractionMode_PermanentSharedUpgrades",
            "PERMANENT SHARED UPGRADES"), 18,
        HEADER_HEIGHT + (self.readOnlyRaid == true and 3 or GENERATOR_HEIGHT + 3),
        0.82, 0.82, 0.82, 1, UIFont.Small)
end

function Panel:new(playerNum)
    playerNum = tonumber(playerNum) or 0
    local screenLeft = getPlayerScreenLeft(playerNum)
    local screenTop = getPlayerScreenTop(playerNum)
    local screenWidth = getPlayerScreenWidth(playerNum)
    local screenHeight = getPlayerScreenHeight(playerNum)
    local height = math.min(UPGRADE_START + 480, screenHeight - 20)
    height = math.max(UPGRADE_START + 220, height)
    local x = math.floor(screenLeft + (screenWidth - PANEL_WIDTH) / 2)
    local y = math.max(screenTop + 10, math.floor(screenTop + (screenHeight - height) / 2))
    local object = ISPanelJoypad:new(x, y, PANEL_WIDTH, height)
    setmetatable(object, self)
    self.__index = self
    object.background = true
    object.backgroundColor = { r = 0.025, g = 0.03, b = 0.035, a = 0.96 }
    object.borderColor = { r = 0.82, g = 0.58, b = 0.16, a = 0.9 }
    object.moveWithMouse = true
    object.playerNum = playerNum
    object.readOnlyRaid = raidReferenceState(clientState(playerNum))
    object.layoutVersion = LAYOUT_VERSION
    return object
end

function ExtractionMode.openUpgradePanel(playerNum, returnToController)
    playerNum = tonumber(playerNum) or 0
    local panel = ExtractionMode.UpgradePanelInstance
    if panel ~= nil and (panel.layoutVersion ~= LAYOUT_VERSION or panel.playerNum ~= playerNum) then
        if panel.joyfocus and setJoypadFocus then setJoypadFocus(panel.playerNum or 0, nil) end
        panel:setVisible(false)
        panel:removeFromUIManager()
        ExtractionMode.UpgradePanelInstance = nil
        panel = nil
    end
    if panel == nil then
        panel = Panel:new(playerNum)
        panel:initialise()
        panel:addToUIManager()
        panel:setAlwaysOnTop(true)
        ExtractionMode.UpgradePanelInstance = panel
    end
    panel:setVisible(true)
    panel:bringToTop()
    panel.returnToController = returnToController == true
    panel:clearISButtonB()
    if panel.returnToController then panel:setISButtonForB(panel.closeButton) end
    panel:autoGenerateJoypadButtonsLists()
    if getJoypadData and getJoypadData(playerNum) then setJoypadFocus(playerNum, panel) end
    return panel
end

function ExtractionMode.toggleUpgradePanel(playerNum)
    local panel = ExtractionMode.UpgradePanelInstance
    if panel ~= nil and panel:isVisible() then
        panel:onClose()
        return nil
    end
    return ExtractionMode.openUpgradePanel(playerNum)
end

ExtractionMode.UpgradePanel = Panel
return Panel
