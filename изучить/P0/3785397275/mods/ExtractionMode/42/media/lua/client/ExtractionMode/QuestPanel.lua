require "ISUI/ISPanel"
require "ISUI/ISPanelJoypad"
require "ISUI/ISButton"
require "ExtractionMode/Config"
require "ExtractionMode/Infection"
require "ExtractionMode/Quests"
require "ExtractionMode/Barters"
require "ExtractionMode/Localization"
require "ExtractionMode/Util"

ExtractionMode = ExtractionMode or {}

local Config = ExtractionMode.Config
local Infection = ExtractionMode.Infection
local Quests = ExtractionMode.Quests
local Barters = ExtractionMode.Barters
local Localization = ExtractionMode.Localization
local Util = ExtractionMode.Util
local Panel = ISPanelJoypad:derive("ExtractionModeQuestPanel")
local LAYOUT_VERSION = 12
local PANEL_WIDTH = 980
local HEADER_HEIGHT = 80
local LIST_WIDTH = PANEL_WIDTH - 24
local ARCHIVED_ROW_HEIGHT = 72
local COLLAPSED_ROW_HEIGHT = 234
local EXPANDED_ROW_HEIGHT = 360
local BUTTON_WIDTH = 126
local BUTTON_GAP = 8
local TURN_IN_X = LIST_WIDTH - BUTTON_WIDTH - 16
local BARTER_BUTTON_X = LIST_WIDTH - BUTTON_WIDTH - 32
local BARTER_CONTACT_BUTTON_X = LIST_WIDTH - 58
local DETAILS_X = TURN_IN_X - BUTTON_WIDTH - BUTTON_GAP
local TEXT_WIDTH = DETAILS_X - 24
local CONTACT_HEADER_HEIGHT = 68
local BARTER_ROW_HEIGHT = 72

local function localPlayer(playerNum)
    return getSpecificPlayer and getSpecificPlayer(playerNum or 0) or (getPlayer and getPlayer())
end

local function definitionHasRaidVisitObjective(definition)
    for _, objective in ipairs((definition and definition.objectives) or {}) do
        if objective.type == "raid_visit" then return true end
    end
    return false
end

local function clientState(playerNum)
    if playerNum == nil and ExtractionMode.QuestPanelInstance then
        playerNum = ExtractionMode.QuestPanelInstance.playerNum
    end
    if ExtractionMode.Client and ExtractionMode.Client.stateFor then
        return ExtractionMode.Client.stateFor(playerNum or 0)
    end
    return ExtractionMode.ClientState or {}
end

local function raidReferenceState(data)
    return data.state == Config.STATE_RAID or data.state == Config.STATE_EXTRACTING
        or data.state == Config.STATE_BOARDING
end

local function send(playerNum, command, args)
    local player = localPlayer(playerNum)
    if player and ExtractionMode.Client and ExtractionMode.Client.sendCommand then
        ExtractionMode.Client.sendCommand(player, command, args or {})
    end
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

local QuestList = ISPanel:derive("ExtractionModeQuestList")

function QuestList:prerender()
    self:setStencilRect(0, 0, self.width, self.height)
    ISPanel.prerender(self)
end

local BarterList = ISPanel:derive("ExtractionModeBarterList")

function BarterList:prerender()
    self:setStencilRect(0, 0, self.width, self.height)
    ISPanel.prerender(self)
end

function BarterList:render()
    ISPanel.render(self)
    if self.owner then self.owner:renderBarterRows(self) end
    self:clearStencilRect()
end

function BarterList:onMouseWheel(delta)
    self:setYScroll(self:getYScroll() - delta * 42)
    return true
end

function BarterList:new(x, y, width, height, owner)
    local object = ISPanel:new(x, y, width, height)
    setmetatable(object, self)
    self.__index = self
    object.owner = owner
    object.background = false
    object.borderColor = { r = 0, g = 0, b = 0, a = 0 }
    object.moveWithMouse = false
    return object
end

function QuestList:render()
    ISPanel.render(self)
    if self.owner then self.owner:renderQuestRows(self) end
    self:clearStencilRect()
end

function QuestList:onMouseWheel(delta)
    self:setYScroll(self:getYScroll() - delta * 42)
    return true
end

function QuestList:new(x, y, width, height, owner)
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

function Panel:onTurnIn(button)
    if button and button.questId then send(self.playerNum, "CompleteQuest", { questId = button.questId }) end
end

function Panel:onBarter(button)
    if button and button.barterId then send(self.playerNum, "CompleteBarter", { barterId = button.barterId }) end
end

function Panel:onToggleBarterContact(button)
    if button == nil or button.contactId == nil then return end
    local collapsed = ExtractionMode.CollapsedBarterContacts
    collapsed[button.contactId] = collapsed[button.contactId] ~= true
    self:layoutBarterRows(false)
    self:autoGenerateJoypadButtonsLists()
end

function Panel:onSelectTab(button)
    if button == nil or button.tabId == nil or self.activeTab == button.tabId then return end
    self.activeTab = button.tabId
    self.questList:setVisible(self.activeTab == "quests")
    self.barterList:setVisible(self.activeTab == "barter")
    self.questTab.enable = self.activeTab ~= "quests"
    self.barterTab.enable = self.activeTab ~= "barter"
    if self.activeTab == "barter" then self:layoutBarterRows(true) end
    self:autoGenerateJoypadButtonsLists()
end

function Panel:onDebugComplete(button)
    if button and button.questId then send(self.playerNum, "DebugCompleteQuest", { questId = button.questId }) end
end

function Panel:onToggleDetails(button)
    if button == nil or button.questId == nil then return end
    if self.expandedQuestId == button.questId then
        self.expandedQuestId = nil
    else
        self.expandedQuestId = button.questId
    end
    self:layoutRows(false)
    self:autoGenerateJoypadButtonsLists()
end

function Panel:rowHeight(definition)
    if self.expandedQuestId == definition.id then
        local completed = Quests.isCompleted(clientState().quests, definition.id)
        local briefingLines = wrapText(Quests.flavorText(definition, completed),
            UIFont.Small, LIST_WIDTH - 62)
        return math.max(EXPANDED_ROW_HEIGHT,
            COLLAPSED_ROW_HEIGHT + 54 + #briefingLines * 17)
    end
    if self.oldCompletedQuestIds and self.oldCompletedQuestIds[definition.id] == true then
        return ARCHIVED_ROW_HEIGHT
    end
    return COLLAPSED_ROW_HEIGHT
end

function Panel:acquiredDefinitions()
    local definitions = Quests.acquiredDefinitions(clientState().quests)
    if self.readOnlyRaid == true then
        local unfinished = {}
        for _, definition in ipairs(definitions) do
            if not Quests.isCompleted(clientState().quests, definition.id) then
                unfinished[#unfinished + 1] = definition
            end
        end
        definitions = unfinished
    end
    if self.questOrder == nil then return definitions end

    local definitionsById = {}
    for _, definition in ipairs(definitions) do
        definitionsById[definition.id] = definition
    end

    local ordered = {}
    local archived = {}
    local included = {}
    for _, questId in ipairs(self.questOrder) do
        local definition = definitionsById[questId]
        if definition ~= nil then
            if self.oldCompletedQuestIds and self.oldCompletedQuestIds[questId] == true then
                archived[#archived + 1] = definition
            else
                ordered[#ordered + 1] = definition
            end
            included[questId] = true
        end
    end
    -- Newly unlocked quests and newly completed quests remain above the archived
    -- completions captured when this menu was opened.
    for _, definition in ipairs(definitions) do
        if not included[definition.id] then ordered[#ordered + 1] = definition end
    end
    for _, definition in ipairs(archived) do ordered[#ordered + 1] = definition end
    return ordered
end

function Panel:refreshQuestOrder(resetScroll)
    local state = clientState()
    local definitions = Quests.acquiredDefinitions(state.quests)
    self.oldCompletedQuestIds = {}
    local ranked = {}
    for index, definition in ipairs(definitions) do
        local completed = Quests.isCompleted(state.quests, definition.id)
        if completed then self.oldCompletedQuestIds[definition.id] = true end
        ranked[#ranked + 1] = {
            definition = definition,
            completed = completed,
            index = index,
        }
    end
    table.sort(ranked, function(left, right)
        if left.completed ~= right.completed then return not left.completed end
        return left.index < right.index
    end)

    self.questOrder = {}
    for _, entry in ipairs(ranked) do
        self.questOrder[#self.questOrder + 1] = entry.definition.id
    end
    if self.expandedQuestId and self.oldCompletedQuestIds[self.expandedQuestId] == true then
        self.expandedQuestId = nil
    end
    self.lastLayoutKey = nil
    if self.questList then self:layoutRows(resetScroll == true) end
end

function Panel:layoutRows(resetScroll)
    local acquired = {}
    local y = 0
    local previousScroll = self.questList and self.questList:getYScroll() or 0
    for _, definition in ipairs(self:acquiredDefinitions()) do
        acquired[definition.id] = true
        local archived = self.oldCompletedQuestIds
            and self.oldCompletedQuestIds[definition.id] == true
            and self.expandedQuestId ~= definition.id
        local turnIn = self.turnInButtons and self.turnInButtons[definition.id]
        local details = self.detailButtons and self.detailButtons[definition.id]
        local debugButton = self.debugButtons and self.debugButtons[definition.id]
        if turnIn then
            turnIn:setVisible(not archived and self.readOnlyRaid ~= true)
            turnIn:setY(y + 12)
        end
        if details then
            details:setVisible(true)
            details:setX((archived or self.readOnlyRaid == true) and TURN_IN_X or DETAILS_X)
            details:setY(y + 12)
            details:setTitle(self.expandedQuestId == definition.id
                and Localization.get("IGUI_ExtractionMode_Collapse", "COLLAPSE")
                or (archived and Localization.get("IGUI_ExtractionMode_Expand", "EXPAND")
                    or Localization.get("IGUI_ExtractionMode_Details", "DETAILS")))
        end
        if debugButton then
            debugButton:setVisible(self.readOnlyRaid ~= true
                and clientState().debugEnabled == true and not archived)
            debugButton:setY(y + 50)
        end
        y = y + self:rowHeight(definition)
    end
    for _, definition in ipairs(Quests.definitions()) do
        if not acquired[definition.id] then
            if self.turnInButtons and self.turnInButtons[definition.id] then
                self.turnInButtons[definition.id]:setVisible(false)
            end
            if self.detailButtons and self.detailButtons[definition.id] then
                self.detailButtons[definition.id]:setVisible(false)
            end
            if self.debugButtons and self.debugButtons[definition.id] then
                self.debugButtons[definition.id]:setVisible(false)
            end
        end
    end
    if self.questList then
        local scrollHeight = math.max(self.questList.height, y)
        self.questList:setScrollHeight(scrollHeight)
        if resetScroll then
            self.questList:setYScroll(0)
        else
            local minimumScroll = math.min(0, self.questList.height - scrollHeight)
            self.questList:setYScroll(math.max(minimumScroll, math.min(0, previousScroll)))
        end
    end
end

function Panel:unlockedBarters()
    return Barters.unlockedDefinitions(clientState().contactTrust)
end

function Panel:barterLayoutEntries()
    local entries = {}
    local y = 0
    local previousContact = nil
    local contactCollapsed = false
    for _, definition in ipairs(self:unlockedBarters()) do
        if definition.contactId ~= previousContact then
            contactCollapsed = ExtractionMode.CollapsedBarterContacts[definition.contactId] == true
            entries[#entries + 1] = {
                kind = "contact",
                contactId = definition.contactId,
                collapsed = contactCollapsed,
                y = y,
            }
            y = y + CONTACT_HEADER_HEIGHT
            previousContact = definition.contactId
        end
        if not contactCollapsed then
            entries[#entries + 1] = {
                kind = "barter",
                definition = definition,
                y = y,
            }
            y = y + BARTER_ROW_HEIGHT
        end
    end
    return entries, y
end

function Panel:layoutBarterRows(resetScroll)
    if self.barterList == nil then return end
    local visible = {}
    local visibleContacts = {}
    local previousScroll = self.barterList:getYScroll()
    local entries, height = self:barterLayoutEntries()
    self.barterEntries = entries
    for _, entry in ipairs(entries) do
        if entry.kind == "contact" then
            visibleContacts[entry.contactId] = true
            local button = self.barterContactButtons and self.barterContactButtons[entry.contactId]
            if button then
                button:setVisible(true)
                button:setY(entry.y + 14)
                button:setTitle(entry.collapsed and "+" or "-")
            end
        elseif entry.kind == "barter" then
            local definition = entry.definition
            visible[definition.id] = true
            local button = self.barterButtons and self.barterButtons[definition.id]
            if button then
                button:setVisible(true)
                button:setY(entry.y + 12)
            end
        end
    end
    for _, definition in ipairs(Barters.definitions()) do
        if not visible[definition.id] and self.barterButtons and self.barterButtons[definition.id] then
            self.barterButtons[definition.id]:setVisible(false)
        end
    end
    for contactId, button in pairs(self.barterContactButtons or {}) do
        if not visibleContacts[contactId] then button:setVisible(false) end
    end
    local scrollHeight = math.max(self.barterList.height, height)
    self.barterList:setScrollHeight(scrollHeight)
    if resetScroll then
        self.barterList:setYScroll(0)
    else
        local minimumScroll = math.min(0, self.barterList.height - scrollHeight)
        self.barterList:setYScroll(math.max(minimumScroll, math.min(0, previousScroll)))
    end
end

function Panel:createChildren()
    ISPanelJoypad.createChildren(self)
    self.turnInButtons = {}
    self.detailButtons = {}
    self.debugButtons = {}
    self.barterButtons = {}
    self.barterContactButtons = {}

    self.questList = QuestList:new(12, HEADER_HEIGHT, LIST_WIDTH,
        self.height - HEADER_HEIGHT - 8, self)
    self.questList:initialise()
    self.questList:instantiate()
    self.questList:setScrollChildren(true)
    self.questList:addScrollBars(false)
    self.questList.vscroll.doSetStencil = true
    self:addChild(self.questList)

    self.barterList = BarterList:new(12, HEADER_HEIGHT, LIST_WIDTH,
        self.height - HEADER_HEIGHT - 8, self)
    self.barterList:initialise()
    self.barterList:instantiate()
    self.barterList:setScrollChildren(true)
    self.barterList:addScrollBars(false)
    self.barterList.vscroll.doSetStencil = true
    self:addChild(self.barterList)

    for _, definition in ipairs(Quests.definitions()) do
        local turnIn = ISButton:new(TURN_IN_X, 12, BUTTON_WIDTH, 30,
            Localization.get("IGUI_ExtractionMode_TurnIn", "TURN IN"), self, Panel.onTurnIn)
        turnIn:initialise()
        turnIn:instantiate()
        turnIn.questId = definition.id
        self.questList:addChild(turnIn)
        self.turnInButtons[definition.id] = turnIn

        local details = ISButton:new(DETAILS_X, 12, BUTTON_WIDTH, 30,
            Localization.get("IGUI_ExtractionMode_Details", "DETAILS"), self, Panel.onToggleDetails)
        details:initialise()
        details:instantiate()
        details.questId = definition.id
        details:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_QuestDetails",
            "Expand or collapse this quest's full briefing."))
        self.questList:addChild(details)
        self.detailButtons[definition.id] = details

        local debugButton = ISButton:new(TURN_IN_X, 50, BUTTON_WIDTH, 30,
            Localization.get("IGUI_ExtractionMode_AutoComplete", "AUTO COMPLETE"), self, Panel.onDebugComplete)
        debugButton:initialise()
        debugButton:instantiate()
        debugButton.questId = definition.id
        debugButton:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_AutoCompleteQuest",
            "DEBUG: Complete this quest without consuming items or completing objectives."))
        self.questList:addChild(debugButton)
        self.debugButtons[definition.id] = debugButton
    end

    for _, definition in ipairs(Barters.definitions()) do
        local trade = ISButton:new(BARTER_BUTTON_X, 12, BUTTON_WIDTH, 30,
            Localization.get("IGUI_ExtractionMode_Trade", "TRADE"), self, Panel.onBarter)
        trade:initialise()
        trade:instantiate()
        trade.barterId = definition.id
        self.barterList:addChild(trade)
        self.barterButtons[definition.id] = trade
    end

    local contactButtonsCreated = {}
    for _, definition in ipairs(Barters.definitions()) do
        local contactId = definition.contactId
        if not contactButtonsCreated[contactId] then
            local toggle = ISButton:new(BARTER_CONTACT_BUTTON_X, 14, 30, 30, "-",
                self, Panel.onToggleBarterContact)
            toggle:initialise()
            toggle:instantiate()
            toggle.contactId = contactId
            toggle:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_ToggleBarterContact",
                "Expand or collapse this trader's barter list."))
            self.barterList:addChild(toggle)
            self.barterContactButtons[contactId] = toggle
            contactButtonsCreated[contactId] = true
        end
    end

    local tabWidth = 180
    local tabGap = 8
    local tabX = math.floor((PANEL_WIDTH - tabWidth * 2 - tabGap) / 2)
    self.questTab = ISButton:new(tabX, 42, tabWidth, 28,
        Localization.get("IGUI_ExtractionMode_Quests", "QUESTS"), self, Panel.onSelectTab)
    self.questTab:initialise()
    self.questTab:instantiate()
    self.questTab.tabId = "quests"
    self:addChild(self.questTab)

    self.barterTab = ISButton:new(tabX + tabWidth + tabGap, 42, tabWidth, 28,
        Localization.get("IGUI_ExtractionMode_BarterTrade", "BARTER TRADE"), self, Panel.onSelectTab)
    self.barterTab:initialise()
    self.barterTab:instantiate()
    self.barterTab.tabId = "barter"
    self:addChild(self.barterTab)

    self.closeButton = ISButton:new(PANEL_WIDTH - 38, 8, 28, 28, "X", self, Panel.onClose)
    self.closeButton:initialise()
    self.closeButton:instantiate()
    self:addChild(self.closeButton)
    self:layoutRows(false)
    self:layoutBarterRows(false)
    self.activeTab = self.activeTab or "quests"
    self.questList:setVisible(self.activeTab == "quests")
    self.barterList:setVisible(self.activeTab == "barter")
    self.questTab.enable = self.activeTab ~= "quests"
    self.barterTab.enable = self.activeTab ~= "barter"
    self.questTab:setVisible(self.readOnlyRaid ~= true)
    self.barterTab:setVisible(self.readOnlyRaid ~= true)
    self:autoGenerateJoypadButtonsLists()
end

function Panel:refreshInventoryCounts()
    local now = Util.nowMs()
    if self.lastCountRefresh and now - self.lastCountRefresh < 500 then return end
    self.lastCountRefresh = now
    self.requirementCounts = {}
    self.requirementsAvailable = {}
    self.barterRequirementCounts = {}
    self.barterRequirementsAvailable = {}
    local player = localPlayer(self.playerNum)
    local inventory = player and player:getInventory()
    local state = clientState()
    for _, definition in ipairs(self:acquiredDefinitions()) do
        local counts = {}
        local completed = Quests.isCompleted(state.quests, definition.id)
        local available = inventory ~= nil and not completed
        if not completed then
            for index, requirement in ipairs(definition.requirements or {}) do
                counts[index] = Quests.requirementCount(inventory, requirement)
                if counts[index] < (tonumber(requirement.amount) or 0) then available = false end
            end
        end
        self.requirementCounts[definition.id] = counts
        self.requirementsAvailable[definition.id] = available
    end
    for _, definition in ipairs(self:unlockedBarters()) do
        local counts = {}
        local available = inventory ~= nil
        for index, requirement in ipairs(definition.offered or {}) do
            counts[index] = Barters.requirementCount(inventory, requirement)
            if counts[index] < (tonumber(requirement.amount) or 0) then available = false end
        end
        self.barterRequirementCounts[definition.id] = counts
        self.barterRequirementsAvailable[definition.id] = available
    end
end

function Panel:ensureVisible()
    if not self.joyfocus then return end
    local children = self:getVisibleChildren(self.joypadIndexY)
    local child = children[self.joypadIndex]
    local list = nil
    if child and (child.parent == self.questList or child.parent == self.barterList) then
        list = child.parent
    end
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
        self.activeTab = "quests"
        self.questList:setVisible(true)
        self.barterList:setVisible(false)
        self.questTab:setVisible(not referenceMode)
        self.barterTab:setVisible(not referenceMode)
        self.lastLayoutKey = nil
        self:layoutRows(false)
    end
    self:refreshInventoryCounts()
    local state = clientState()
    local player = localPlayer(self.playerNum)
    local inHideout = state.state == Config.STATE_HIDEOUT and Infection.playerInsideHideout(player)
    local acquired = self:acquiredDefinitions()
    local unlockedBarters = self:unlockedBarters()
    local layoutKey = tostring(self.expandedQuestId or "") .. ":" .. tostring(#acquired)
        .. ":R" .. tostring(self.readOnlyRaid == true)
        .. ":" .. tostring(state.debugEnabled == true) .. ":B" .. tostring(#unlockedBarters)
    for _, definition in ipairs(acquired) do layoutKey = layoutKey .. ":" .. definition.id end
    for _, definition in ipairs(unlockedBarters) do layoutKey = layoutKey .. ":" .. definition.id end
    if layoutKey ~= self.lastLayoutKey then
        self.lastLayoutKey = layoutKey
        self:layoutRows(false)
        self:layoutBarterRows(false)
        self:autoGenerateJoypadButtonsLists()
    end

    for _, definition in ipairs(acquired) do
        local completed = Quests.isCompleted(state.quests, definition.id)
        local skillsMet = Quests.skillRequirementsMet(player, definition)
        local objectivesMet = Quests.objectivesMet(state.questObjectives, definition)
        local raidSuccessMet = not definitionHasRaidVisitObjective(definition)
            or state.raidVisitSuccessful == true
        local available = inHideout and not completed and skillsMet and objectivesMet and raidSuccessMet
            and self.requirementsAvailable[definition.id] == true
        local button = self.turnInButtons[definition.id]
        local debugButton = self.debugButtons[definition.id]
        button:setTitle(completed and Localization.get("IGUI_ExtractionMode_Completed", "COMPLETED")
            or Localization.get("IGUI_ExtractionMode_TurnIn", "TURN IN"))
        button.enable = available
        debugButton.enable = self.readOnlyRaid ~= true and state.debugEnabled == true and not completed
        if completed then
            button:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_QuestCompleted",
                "This quest has already been completed for your current quest group."))
        elseif not skillsMet then
            button:setTooltip(Localization.get("IGUI_ExtractionMode_Requires", "Requires: %1",
                table.concat(Quests.missingSkillNames(player, definition), ", ")))
        elseif not objectivesMet then
            button:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_ObjectivesIncomplete",
                "Complete every quest objective before turning this in."))
        elseif not raidSuccessMet then
            button:setTooltip(Localization.get("IGUI_ExtractionMode_Error_QuestRaidSuccessRequired",
                "Successfully extract from the raid where those locations were checked before turning in %1.",
                Quests.name(definition)))
        elseif not inHideout then
            button:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_QuestHideoutOnly",
                "Quest materials can only be turned in while idle inside the hideout."))
        elseif not available then
            button:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_QuestItemsMissing",
                "Carry every required item in your inventory or nested bags."))
        else
            button:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_CompleteQuest",
                "Consume the required items and complete this quest for your current quest group."))
        end
    end
    for _, definition in ipairs(unlockedBarters) do
        local available = inHideout and self.barterRequirementsAvailable[definition.id] == true
        local button = self.barterButtons[definition.id]
        button.enable = available
        if not inHideout then
            button:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_BarterHideoutOnly",
                "Trades can only be completed while idle inside the hideout."))
        elseif not available then
            button:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_BarterItemsMissing",
                "Carry every offered item in your inventory or nested bags."))
        else
            button:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_CompleteBarter",
                "Exchange the offered items. Trust unlocks this trade and is not consumed."))
        end
    end
    ISPanel.prerender(self)
end

function Panel:renderQuestRows(canvas)
    local state = clientState()
    local player = localPlayer(self.playerNum)
    local y = 0
    local definitions = self:acquiredDefinitions()
    if #definitions == 0 then
        canvas:drawTextCentre(Localization.get("IGUI_ExtractionMode_NoActiveQuests",
                "NO ACTIVE QUESTS"), (LIST_WIDTH - 14) / 2, 36,
            0.7, 0.7, 0.7, 1, UIFont.Medium)
        return
    end
    for _, definition in ipairs(definitions) do
        local completed = Quests.isCompleted(state.quests, definition.id)
        local rowHeight = self:rowHeight(definition)
        local archived = self.oldCompletedQuestIds
            and self.oldCompletedQuestIds[definition.id] == true
            and self.expandedQuestId ~= definition.id
        canvas:drawRect(0, y, LIST_WIDTH - 14, rowHeight - 8, 0.34, 0.08, 0.09, 0.10)
        canvas:drawRectBorder(0, y, LIST_WIDTH - 14, rowHeight - 8, 0.55, 0.55, 0.42, 0.20)
        canvas:drawText(Quests.name(definition), 12, y + 12, completed and 0.38 or 1,
            completed and 0.82 or 1, completed and 0.42 or 1, 1, UIFont.Medium)

        if archived then
            canvas:drawText(Localization.get("IGUI_ExtractionMode_Completed", "COMPLETED"),
                12, y + 40, 0.38, 0.88, 0.46, 1, UIFont.Small)
        else
        canvas:drawText(Localization.get("IGUI_ExtractionMode_ContactLabel", "CONTACT: %1",
                string.upper(Quests.contactDisplayName(definition.contactId))),
            12, y + 40, 0.96, 0.72, 0.18, 1, UIFont.Small)

        local descriptionLines = wrapText(Quests.description(definition), UIFont.Small, TEXT_WIDTH)
        for lineIndex = 1, math.min(2, #descriptionLines) do
            canvas:drawText(descriptionLines[lineIndex], 12, y + 62 + (lineIndex - 1) * 18,
                0.82, 0.82, 0.82, 1, UIFont.Small)
        end

        local detailY = y + 106
        if completed then
            canvas:drawText(Localization.get("IGUI_ExtractionMode_QuestCompletedNewAssignments",
                    "COMPLETED - New assignments may now be available"), 12, detailY,
                0.38, 0.88, 0.46, 1, UIFont.Small)
            detailY = detailY + 24
        else
            for _, objective in ipairs(definition.objectives or {}) do
                local amount = math.max(0, math.floor(tonumber(objective.amount) or 0))
                local count = Quests.objectiveCount(state.questObjectives, definition, objective)
                local met = count >= amount
                canvas:drawText(Localization.get("IGUI_ExtractionMode_ObjectiveProgress",
                        "Objective - %1: %2/%3", Quests.label(objective),
                        tostring(math.min(count, amount)), tostring(amount)), 12, detailY,
                    met and 0.35 or 0.95, met and 0.9 or 0.45,
                    met and 0.4 or 0.3, 1, UIFont.Small)
                detailY = detailY + 20
            end
            if definitionHasRaidVisitObjective(definition) then
                local secured = state.raidVisitSuccessful == true
                canvas:drawText(Localization.get("IGUI_ExtractionMode_ObjectiveProgress",
                        "Objective - %1: %2/%3",
                        Localization.get("IGUI_ExtractionMode_RaidSuccessObjective",
                            "Raid successfully extracted"), secured and "1" or "0", "1"),
                    12, detailY, secured and 0.35 or 0.95, secured and 0.9 or 0.45,
                    secured and 0.4 or 0.3, 1, UIFont.Small)
                detailY = detailY + 20
            end

            local counts = self.requirementCounts and self.requirementCounts[definition.id] or {}
            local itemParts = {}
            local itemsMet = true
            for index, requirement in ipairs(definition.requirements or {}) do
                local amount = math.max(0, math.floor(tonumber(requirement.amount) or 0))
                local count = tonumber(counts[index]) or 0
                if count < amount then itemsMet = false end
                itemParts[#itemParts + 1] = Quests.label(requirement) .. ": "
                    .. tostring(math.min(count, amount)) .. "/" .. tostring(amount)
            end
            if #itemParts > 0 then
                local itemLines = wrapText(Localization.get("IGUI_ExtractionMode_Items",
                    "Items: %1", table.concat(itemParts, "   |   ")), UIFont.Small,
                    LIST_WIDTH - 38)
                for lineIndex = 1, math.min(2, #itemLines) do
                    canvas:drawText(itemLines[lineIndex], 12, detailY + (lineIndex - 1) * 18,
                        itemsMet and 0.35 or 0.95, itemsMet and 0.9 or 0.45,
                        itemsMet and 0.4 or 0.3, 1, UIFont.Small)
                end
                detailY = detailY + math.min(2, #itemLines) * 18 + 2
            end

            local skillParts = {}
            local skillsMet = true
            for _, requirement in ipairs(definition.skillRequirements or {}) do
                local required = math.max(0, math.floor(tonumber(requirement.level) or 0))
                local current = Quests.skillLevel(player, requirement)
                if current < required then skillsMet = false end
                skillParts[#skillParts + 1] = Quests.label(requirement) .. ": "
                    .. tostring(current) .. "/" .. tostring(required)
            end
            if #skillParts > 0 then
                canvas:drawText(Localization.get("IGUI_ExtractionMode_Skills", "Skills: %1",
                    table.concat(skillParts, "   |   ")), 12, detailY,
                    skillsMet and 0.35 or 0.95, skillsMet and 0.9 or 0.45,
                    skillsMet and 0.4 or 0.3, 1, UIFont.Small)
                detailY = detailY + 20
            end
        end

        local rewardParts = {}
        for _, reward in ipairs(definition.rewards or {}) do
            if reward.type == "trust" then
                local current = math.max(0, tonumber(state.contactTrust
                    and state.contactTrust[reward.contactId]) or 0)
                local text = Localization.get("IGUI_ExtractionMode_TrustReward",
                    "+%1 Trust with %2", tostring(math.max(0, tonumber(reward.amount) or 0)),
                    Quests.contactDisplayName(reward.contactId))
                if completed then
                    text = text .. Localization.get("IGUI_ExtractionMode_CurrentTrustSuffix",
                        "  |  Current Trust: %1", tostring(current))
                end
                rewardParts[#rewardParts + 1] = text
            elseif reward.type == "item" then
                rewardParts[#rewardParts + 1] = tostring(math.max(0,
                    math.floor(tonumber(reward.amount) or 0))) .. "x "
                    .. (reward.label and Quests.label(reward) or tostring(reward.fullType
                        or Localization.get("IGUI_ExtractionMode_Item", "Item")))
            elseif reward.type == "location" then
                rewardParts[#rewardParts + 1] = reward.label and Quests.label(reward)
                    or Localization.get("IGUI_ExtractionMode_UnlockRaidLocation",
                        "Unlock Raid Location: %1",
                        Config.townDisplayName(reward.townKey, tostring(reward.townKey or "")))
            elseif reward.type == "campaign" then
                rewardParts[#rewardParts + 1] = reward.label and Quests.label(reward)
                    or Localization.get("IGUI_ExtractionMode_CompleteCampaign", "Complete the campaign")
            end
        end
        if #rewardParts > 0 then
            local rewardLines = wrapText(Localization.get("IGUI_ExtractionMode_Reward",
                "Reward: %1", table.concat(rewardParts, "   |   ")),
                UIFont.Small, LIST_WIDTH - 38)
            for lineIndex = 1, math.min(2, #rewardLines) do
                canvas:drawText(rewardLines[lineIndex], 12, detailY + (lineIndex - 1) * 18,
                    0.45, 0.72, 1, 1, UIFont.Small)
            end
        end

        if self.expandedQuestId == definition.id then
            local boxY = y + COLLAPSED_ROW_HEIGHT - 12
            local boxHeight = rowHeight - COLLAPSED_ROW_HEIGHT - 10
            canvas:drawRect(12, boxY, LIST_WIDTH - 38, boxHeight, 0.55, 0.025, 0.03, 0.035)
            canvas:drawRectBorder(12, boxY, LIST_WIDTH - 38, boxHeight, 0.6, 0.52, 0.42, 0.20)
            canvas:drawText(completed and Localization.get("IGUI_ExtractionMode_Completion", "COMPLETION")
                    or Localization.get("IGUI_ExtractionMode_QuestBriefing", "QUEST BRIEFING"), 22, boxY + 9,
                0.96, 0.72, 0.18, 1, UIFont.Small)
            local flavorLines = wrapText(Quests.flavorText(definition, completed),
                UIFont.Small, LIST_WIDTH - 62)
            for lineIndex = 1, #flavorLines do
                canvas:drawText(flavorLines[lineIndex], 22, boxY + 32 + (lineIndex - 1) * 17,
                    0.78, 0.78, 0.78, 1, UIFont.Small)
            end
        end
        end
        y = y + rowHeight
    end
end

function Panel:renderBarterRows(canvas)
    local state = clientState()
    local entries = self.barterEntries or {}
    if #entries == 0 then
        canvas:drawTextCentre(Localization.get("IGUI_ExtractionMode_NoBartersUnlocked",
                "NO BARTER TRADES UNLOCKED"), (LIST_WIDTH - 14) / 2, 36,
            0.7, 0.7, 0.7, 1, UIFont.Medium)
        canvas:drawTextCentre(Localization.get("IGUI_ExtractionMode_NoBartersHint",
                "Complete contact quests to earn trust and reveal repeatable trades."),
            (LIST_WIDTH - 14) / 2, 68, 0.55, 0.55, 0.55, 1, UIFont.Small)
        return
    end

    for _, entry in ipairs(entries) do
        local y = entry.y
        if entry.kind == "contact" then
            local trust = math.max(0, tonumber(state.contactTrust
                and state.contactTrust[entry.contactId]) or 0)
            canvas:drawRect(0, y, LIST_WIDTH - 14, CONTACT_HEADER_HEIGHT - 6,
                0.5, 0.055, 0.065, 0.075)
            canvas:drawRectBorder(0, y, LIST_WIDTH - 14, CONTACT_HEADER_HEIGHT - 6,
                0.65, 0.82, 0.58, 0.16)
            canvas:drawText(string.upper(Quests.contactDisplayName(entry.contactId)), 12, y + 10,
                0.96, 0.72, 0.18, 1, UIFont.Medium)
            canvas:drawText(Localization.get("IGUI_ExtractionMode_TrustAndFocus", "TRUST: %1  |  %2",
                    tostring(trust), Quests.contactFocus(entry.contactId)),
                12, y + 38, 0.72, 0.82, 0.9, 1, UIFont.Small)
        elseif entry.kind == "barter" then
            local definition = entry.definition
            local counts = self.barterRequirementCounts
                and self.barterRequirementCounts[definition.id] or {}
            canvas:drawRect(8, y, LIST_WIDTH - 30, BARTER_ROW_HEIGHT - 8,
                0.34, 0.08, 0.09, 0.10)
            canvas:drawRectBorder(8, y, LIST_WIDTH - 30, BARTER_ROW_HEIGHT - 8,
                0.45, 0.46, 0.38, 0.22)

            local offerParts = {}
            for index, item in ipairs(definition.offered or {}) do
                local amount = math.max(0, math.floor(tonumber(item.amount) or 0))
                local count = math.max(0, math.floor(tonumber(counts[index]) or 0))
                offerParts[#offerParts + 1] = tostring(amount) .. "x " .. Barters.label(item)
                    .. " (" .. tostring(math.min(count, amount)) .. "/" .. tostring(amount) .. ")"
            end
            local receiveParts = {}
            for _, item in ipairs(definition.received or {}) do
                receiveParts[#receiveParts + 1] = tostring(math.max(0,
                    math.floor(tonumber(item.amount) or 0))) .. "x " .. Barters.label(item)
            end
            canvas:drawText(Localization.get("IGUI_ExtractionMode_Give", "GIVE: %1",
                    table.concat(offerParts, "  |  ")), 20, y + 12,
                0.92, 0.62, 0.32, 1, UIFont.Small)
            canvas:drawText(Localization.get("IGUI_ExtractionMode_Receive", "RECEIVE: %1",
                    table.concat(receiveParts, "  |  ")), 20, y + 39,
                0.38, 0.88, 0.48, 1, UIFont.Small)
        end
    end
end

function Panel:render()
    ISPanel.render(self)
    self:drawTextCentre(self.readOnlyRaid == true
            and Localization.get("IGUI_ExtractionMode_QuestReference", "QUEST REFERENCE")
            or Localization.get("IGUI_ExtractionMode_Contacts", "CONTACTS"), PANEL_WIDTH / 2, 10,
        0.96, 0.72, 0.18, 1, UIFont.Medium)
    if self.readOnlyRaid == true then
        self:drawTextCentre(Localization.get("IGUI_ExtractionMode_RaidReferenceReadOnly",
                "READ ONLY DURING RAID - Return to the hideout to turn in quests."),
            PANEL_WIDTH / 2, 42, 0.8, 0.8, 0.8, 1, UIFont.Small)
    end
end

function Panel:new(playerNum)
    playerNum = tonumber(playerNum) or 0
    local screenLeft = getPlayerScreenLeft(playerNum)
    local screenTop = getPlayerScreenTop(playerNum)
    local screenWidth = getPlayerScreenWidth(playerNum)
    local screenHeight = getPlayerScreenHeight(playerNum)
    local height = math.min(HEADER_HEIGHT + 680, screenHeight - 20)
    height = math.max(HEADER_HEIGHT + 240, height)
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
    object.activeTab = "quests"
    object.readOnlyRaid = raidReferenceState(clientState(playerNum))
    object.layoutVersion = LAYOUT_VERSION
    return object
end

-- This table intentionally lives only in the client Lua session. It survives
-- closing and recreating the Contacts panel, but a UI/game reload resets every
-- trader section to expanded without touching save or server data.
ExtractionMode.CollapsedBarterContacts = ExtractionMode.CollapsedBarterContacts or {}

function ExtractionMode.openQuestPanel(playerNum, returnToController)
    playerNum = tonumber(playerNum) or 0
    local panel = ExtractionMode.QuestPanelInstance
    if panel ~= nil and (panel.layoutVersion ~= LAYOUT_VERSION or panel.playerNum ~= playerNum) then
        if panel.joyfocus and setJoypadFocus then setJoypadFocus(panel.playerNum or 0, nil) end
        panel:setVisible(false)
        panel:removeFromUIManager()
        ExtractionMode.QuestPanelInstance = nil
        panel = nil
    end
    if panel == nil then
        panel = Panel:new(playerNum)
        panel:initialise()
        panel:addToUIManager()
        panel:setAlwaysOnTop(true)
        ExtractionMode.QuestPanelInstance = panel
    end
    panel:refreshQuestOrder(true)
    panel:layoutBarterRows(true)
    panel:setVisible(true)
    panel:bringToTop()
    panel.returnToController = returnToController == true
    panel:clearISButtonB()
    if panel.returnToController then panel:setISButtonForB(panel.closeButton) end
    panel:autoGenerateJoypadButtonsLists()
    if getJoypadData and getJoypadData(playerNum) then setJoypadFocus(playerNum, panel) end
    return panel
end

function ExtractionMode.toggleQuestPanel(playerNum)
    local panel = ExtractionMode.QuestPanelInstance
    if panel ~= nil and panel:isVisible() then
        panel:onClose()
        return nil
    end
    return ExtractionMode.openQuestPanel(playerNum)
end

ExtractionMode.QuestPanel = Panel
return Panel
