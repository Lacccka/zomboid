require "ISUI/ISPanel"
require "ISUI/ISButton"
require "ISUI/ISRichTextPanel"
require "LCCQF/LCCQFConstants"
require "LCCQF/Quest/LCCQFQuestClientState"

LCCQFHub = LCCQFHub or {}

local Hub = LCCQFHub
local C = LCCQF.Constants
local QuestClientState = LCCQF.QuestClientState
local TRANSLATION_PREFIX = "IGUI_LCCQF_"

Hub.pages = Hub.pages or {}
Hub.button = Hub.button or nil
Hub.window = Hub.window or nil
Hub.selectedQuestId = Hub.selectedQuestId or nil

local function localize(key, fallback)
    if type(key) ~= "string" or #key > C.MAX_IDENTIFIER_LENGTH
        or string.sub(key, 1, #TRANSLATION_PREFIX) ~= TRANSLATION_PREFIX
    then
        return fallback or "Quest Framework"
    end

    local value = getText(key)
    if not value or value == key then return fallback or key end
    return value
end

local function questStateText(state)
    if state == "active" then
        return localize("IGUI_LCCQF_Hub_State_Active", "Active")
    elseif state == "completed" then
        return localize("IGUI_LCCQF_Hub_State_Completed", "Completed")
    elseif state == "failed" then
        return localize("IGUI_LCCQF_Hub_State_Failed", "Failed")
    elseif state == "cancelled" then
        return localize("IGUI_LCCQF_Hub_State_Cancelled", "Cancelled")
    end
    return tostring(state or "-")
end

local function objectiveProgressSuffix(objective)
    local required = math.max(1, math.floor(tonumber(objective and objective.required) or 1))
    if required <= 1 then return "" end
    local progress = math.max(0, math.min(required, math.floor(tonumber(objective.progress) or 0)))
    return " (" .. tostring(progress) .. "/" .. tostring(required) .. ")"
end

function Hub.RegisterPage(definition)
    if type(definition) ~= "table" or type(definition.id) ~= "string"
        or type(definition.create) ~= "function"
    then
        return false
    end

    for _, current in ipairs(Hub.pages) do
        if current.id == definition.id then return false end
    end

    Hub.pages[#Hub.pages + 1] = definition
    return true
end

LCCQFQuestHubPage = ISPanel:derive("LCCQFQuestHubPage")

function LCCQFQuestHubPage:new(x, y, width, height)
    local o = ISPanel:new(x, y, width, height)
    setmetatable(o, self)
    self.__index = self
    o.background = false
    o.questButtons = {}
    o.lastRevision = -1
    o.listWidth = math.min(280, math.floor(width * 0.36))
    o.detailTitle = ""
    return o
end

function LCCQFQuestHubPage:initialise()
    ISPanel.initialise(self)
end

function LCCQFQuestHubPage:createChildren()
    self.detail = ISRichTextPanel:new(
        self.listWidth + 28,
        56,
        self.width - self.listWidth - 44,
        self.height - 74
    )
    self.detail:initialise()
    self.detail:instantiate()
    self.detail.autosetheight = false
    self.detail.background = false
    self.detail.borderColor = { r = 0, g = 0, b = 0, a = 0 }
    self.detail.marginLeft = 0
    self.detail.marginRight = 8
    self.detail.marginTop = 0
    self.detail.marginBottom = 0
    self:addChild(self.detail)

    self:refresh(true)
end

function LCCQFQuestHubPage:clearQuestButtons()
    for _, button in ipairs(self.questButtons) do
        self:removeChild(button)
    end
    self.questButtons = {}
end

function LCCQFQuestHubPage:selectQuest(instanceId)
    Hub.selectedQuestId = instanceId
    self:updateDetail()
end

function LCCQFQuestHubPage:onQuestPressed(button)
    if not button or not button.instanceId then return end
    self:selectQuest(button.instanceId)
end

function LCCQFQuestHubPage:updateDetail()
    local view = Hub.selectedQuestId and QuestClientState.Get(Hub.selectedQuestId) or nil
    if not view then
        self.detailTitle = localize("IGUI_LCCQF_Hub_NoQuestSelected", "No quest selected")
        self.detail.text = localize("IGUI_LCCQF_Hub_NoQuests", "There are no known quests yet.")
        self.detail:paginate()
        self.detail:setYScroll(0)
        return
    end

    self.detailTitle = localize(view.titleKey, tostring(view.questId or "Quest"))
    local lines = {
        localize(view.descriptionKey, ""),
        "",
        localize("IGUI_LCCQF_Hub_Status", "Status") .. ": " .. questStateText(view.state),
        "",
        localize("IGUI_LCCQF_Hub_Objectives", "Objectives") .. ":",
    }

    for _, objective in ipairs(view.objectives or {}) do
        local prefix = "[ ] "
        if objective.state == "completed" then
            prefix = "[x] "
        elseif objective.state == "active" then
            prefix = "[>] "
        end
        lines[#lines + 1] = prefix
            .. localize(objective.titleKey, tostring(objective.id or "Objective"))
            .. objectiveProgressSuffix(objective)
    end

    if view.marker and view.state == "active" then
        lines[#lines + 1] = ""
        lines[#lines + 1] = localize("IGUI_LCCQF_Hub_MapMarkerActive", "The active objective is marked on the world map.")
    end

    self.detail.text = table.concat(lines, "\n")
    self.detail:paginate()
    self.detail:setYScroll(0)
end

function LCCQFQuestHubPage:refresh(force)
    local revision = QuestClientState.GetRevision()
    if not force and revision == self.lastRevision then return end
    self.lastRevision = revision

    self:clearQuestButtons()
    local quests = QuestClientState.ListAll()

    if Hub.selectedQuestId and not QuestClientState.Get(Hub.selectedQuestId) then
        Hub.selectedQuestId = nil
    end
    if not Hub.selectedQuestId and quests[1] then
        Hub.selectedQuestId = quests[1].instanceId
    end

    local y = 52
    local maxVisible = math.max(1, math.floor((self.height - y - 20) / 42))
    for index = 1, math.min(#quests, maxVisible) do
        local view = quests[index]
        local title = localize(view.titleKey, tostring(view.questId or "Quest"))
        local button = ISButton:new(12, y, self.listWidth - 24, 34, title, self, LCCQFQuestHubPage.onQuestPressed)
        button:initialise()
        button:instantiate()
        button.instanceId = view.instanceId
        button.enable = true
        self:addChild(button)
        self.questButtons[#self.questButtons + 1] = button
        y = y + 42
    end

    self:updateDetail()
end

function LCCQFQuestHubPage:update()
    ISPanel.update(self)
    self:refresh(false)
end

function LCCQFQuestHubPage:prerender()
    ISPanel.prerender(self)
    self:drawText(localize("IGUI_LCCQF_Hub_QuestList", "Quests"), 12, 18, 1, 1, 1, 1, UIFont.Medium)
    self:drawRect(self.listWidth, 12, 1, self.height - 24, 0.35, 0.6, 0.6, 0.6)
    self:drawText(self.detailTitle or "", self.listWidth + 28, 18, 1, 1, 1, 1, UIFont.Medium)
end

LCCQFHubPanel = ISPanel:derive("LCCQFHubPanel")

function LCCQFHubPanel:new(x, y, width, height)
    local o = ISPanel:new(x, y, width, height)
    setmetatable(o, self)
    self.__index = self
    o.moveWithMouse = true
    o.backgroundColor = { r = 0.045, g = 0.045, b = 0.045, a = 0.96 }
    o.borderColor = { r = 0.55, g = 0.55, b = 0.55, a = 1.0 }
    o.pagePanels = {}
    o.tabButtons = {}
    o.activePageId = nil
    return o
end

function LCCQFHubPanel:initialise()
    ISPanel.initialise(self)
end

function LCCQFHubPanel:createChildren()
    self.closeButton = ISButton:new(self.width - 48, 14, 32, 28, "X", self, LCCQFHubPanel.onClosePressed)
    self.closeButton:initialise()
    self.closeButton:instantiate()
    self:addChild(self.closeButton)

    local x = 18
    for _, definition in ipairs(Hub.pages) do
        local title = localize(definition.labelKey, definition.id)
        local button = ISButton:new(x, 52, 132, 34, title, self, LCCQFHubPanel.onTabPressed)
        button:initialise()
        button:instantiate()
        button.pageId = definition.id
        self:addChild(button)
        self.tabButtons[#self.tabButtons + 1] = button
        x = x + 140
    end

    if Hub.pages[1] then self:switchPage(Hub.pages[1].id) end
end

function LCCQFHubPanel:onClosePressed()
    self:setVisible(false)
end

function LCCQFHubPanel:onTabPressed(button)
    if button and button.pageId then self:switchPage(button.pageId) end
end

function LCCQFHubPanel:switchPage(pageId)
    if self.activePageId == pageId then return end

    if self.activePageId and self.pagePanels[self.activePageId] then
        self.pagePanels[self.activePageId]:setVisible(false)
    end

    local page = self.pagePanels[pageId]
    if not page then
        local definition = nil
        for _, current in ipairs(Hub.pages) do
            if current.id == pageId then definition = current break end
        end
        if not definition then return end

        page = definition.create(self, 16, 96, self.width - 32, self.height - 112)
        page:initialise()
        page:instantiate()
        self:addChild(page)
        self.pagePanels[pageId] = page
    end

    page:setVisible(true)
    self.activePageId = pageId

    for _, button in ipairs(self.tabButtons) do
        button.enable = button.pageId ~= pageId
    end
end

function LCCQFHubPanel:prerender()
    ISPanel.prerender(self)
    self:drawText(localize("IGUI_LCCQF_Hub_Title", "Quest Framework"), 18, 18, 1, 1, 1, 1, UIFont.Large)
end

LCCQFHubButton = ISButton:derive("LCCQFHubButton")

function LCCQFHubButton:new()
    local width, height = 48, 42
    local x = getCore():getScreenWidth() - width - 16
    local y = 156
    local o = ISButton:new(x, y, width, height, "QF", nil, Hub.Toggle)
    setmetatable(o, self)
    self.__index = self
    return o
end

function LCCQFHubButton:prerender()
    self:setX(getCore():getScreenWidth() - self.width - 16)
    ISButton.prerender(self)
end

function Hub.Toggle()
    if Hub.window then
        local show = not Hub.window:isVisible()
        Hub.window:setVisible(show)
        if show then Hub.window:bringToTop() end
        return
    end

    local screenWidth = getCore():getScreenWidth()
    local screenHeight = getCore():getScreenHeight()
    local width = math.min(860, screenWidth - 80)
    local height = math.min(580, screenHeight - 80)
    local x = math.floor((screenWidth - width) / 2)
    local y = math.floor((screenHeight - height) / 2)

    local panel = LCCQFHubPanel:new(x, y, width, height)
    panel:initialise()
    panel:addToUIManager()
    panel:setAlwaysOnTop(true)
    Hub.window = panel
end

function Hub.EnsureButton()
    if Hub.button then return Hub.button end

    local button = LCCQFHubButton:new()
    button:initialise()
    button:instantiate()
    button:addToUIManager()
    button:setAlwaysOnTop(true)
    Hub.button = button
    return button
end

function Hub.Reset()
    if Hub.window then
        Hub.window:removeFromUIManager()
        Hub.window = nil
    end
    if Hub.button then
        Hub.button:removeFromUIManager()
        Hub.button = nil
    end
    Hub.selectedQuestId = nil
    Hub.EnsureButton()
end

Hub.RegisterPage({
    id = "quests",
    labelKey = "IGUI_LCCQF_Hub_Tab_Quests",
    create = function(window, x, y, width, height)
        return LCCQFQuestHubPage:new(x, y, width, height)
    end,
})

Events.OnGameStart.Add(Hub.Reset)

return Hub
