require "ISUI/ISPanel"
require "ISUI/ISButton"
require "ISUI/ISRichTextPanel"
require "ISUI/ISScrollingListBox"
require "ISUI/ISUI3DModel"
require "LCCQF/LCCQFConstants"
require "LCCQF/Core/LCCQFNPCRuntime"
require "LCCQF/Knowledge/LCCQFKnownPeopleClientState"
require "LCCQF/Quest/LCCQFQuestClientState"
require "LCCQF/UI/LCCQFHub"

local C = LCCQF.Constants
local Hub = LCCQFHub
local KnownPeople = LCCQF.KnownPeopleClientState
local QuestState = LCCQF.QuestClientState
local Runtime = LCCQF.NPCRuntime
local TRANSLATION_PREFIX = "IGUI_LCCQF_"

local function localize(key, fallback)
    if type(key) ~= "string" or #key > C.MAX_IDENTIFIER_LENGTH
        or string.sub(key, 1, #TRANSLATION_PREFIX) ~= TRANSLATION_PREFIX
    then
        return fallback or "-"
    end
    local value = getText(key)
    if not value or value == key then return fallback or key end
    return value
end

local function personName(view)
    if not view then return localize("IGUI_LCCQF_Hub_UnknownPerson", "Unknown person") end
    local name = localize(view.displayNameKey, tostring(view.npcId or "NPC"))
    if type(view.aliasKey) == "string" then
        local alias = localize(view.aliasKey, "")
        if alias ~= "" then name = name .. " \"" .. alias .. "\"" end
    end
    return name
end

local function questTitle(view)
    return localize(view and view.titleKey, tostring(view and view.questId or "Quest"))
end

local function questStateText(state)
    if state == "active" then return localize("IGUI_LCCQF_Hub_State_Active", "Active") end
    if state == "completed" then return localize("IGUI_LCCQF_Hub_State_Completed", "Completed") end
    if state == "failed" then return localize("IGUI_LCCQF_Hub_State_Failed", "Failed") end
    if state == "cancelled" then return localize("IGUI_LCCQF_Hub_State_Cancelled", "Cancelled") end
    return tostring(state or "-")
end

local function objectiveProgressSuffix(objective)
    local required = math.max(1, math.floor(tonumber(objective and objective.required) or 1))
    if required <= 1 then return "" end
    local progress = math.max(0, math.min(required, math.floor(tonumber(objective.progress) or 0)))
    return " (" .. tostring(progress) .. "/" .. tostring(required) .. ")"
end

local function questsForPerson(npcId)
    local active, completed = {}, {}
    for _, quest in ipairs(QuestState.ListAll()) do
        if quest.giverNpcId == npcId then
            if quest.state == "active" then
                active[#active + 1] = quest
            elseif quest.state == "completed" then
                completed[#completed + 1] = quest
            end
        end
    end
    return active, completed
end

local function removeRegisteredPage(pageId)
    for index = #Hub.pages, 1, -1 do
        if Hub.pages[index].id == pageId then table.remove(Hub.pages, index) end
    end
end

local function selectListItemByField(list, field, value)
    if not list or value == nil then return false end
    for index, row in ipairs(list.items or {}) do
        local item = row.item
        if type(item) == "table" and item[field] == value then
            list.selected = index
            list:ensureVisible(index)
            return true
        end
    end
    return false
end

LCCQFKnownPeoplePage = ISPanel:derive("LCCQFKnownPeoplePage")

function LCCQFKnownPeoplePage:new(x, y, width, height)
    local o = ISPanel:new(x, y, width, height)
    setmetatable(o, self)
    self.__index = self
    o.background = false
    o.listWidth = math.min(270, math.floor(width * 0.33))
    o.lastKnowledgeRevision = -1
    o.lastQuestRevision = -1
    o.selectedNpcId = Hub.selectedPersonId
    o.nextPortraitRefreshMs = 0
    o.portraitHasCharacter = false
    return o
end

function LCCQFKnownPeoplePage:initialise()
    ISPanel.initialise(self)
end

function LCCQFKnownPeoplePage:createChildren()
    local detailX = self.listWidth + 28

    self.personList = ISScrollingListBox:new(12, 52, self.listWidth - 24, self.height - 64)
    self.personList:initialise()
    self.personList:instantiate()
    self.personList:setFont(UIFont.Small, 7)
    self.personList.drawBorder = true
    self.personList:setOnMouseDownFunction(self, LCCQFKnownPeoplePage.onPersonSelected)
    self:addChild(self.personList)

    self.portrait = ISUI3DModel:new(detailX, 58, 210, 250)
    self.portrait:initialise()
    self.portrait:instantiate()
    self.portrait.backgroundColor = { r = 0, g = 0, b = 0, a = 0 }
    self.portrait.borderColor = { r = 0, g = 0, b = 0, a = 0 }
    self.portrait:setState("idle")
    self.portrait:setDirection(IsoDirections.S)
    self.portrait:setIsometric(false)
    self.portrait:setDoRandomExtAnimations(true)
    self:addChild(self.portrait)

    self.info = ISRichTextPanel:new(detailX + 230, 58, self.width - detailX - 246, 250)
    self.info:initialise()
    self.info:instantiate()
    self.info.autosetheight = false
    self.info.background = false
    self.info.borderColor = { r = 0, g = 0, b = 0, a = 0 }
    self:addChild(self.info)

    self.relatedQuestList = ISScrollingListBox:new(
        detailX,
        344,
        self.width - detailX - 14,
        math.max(74, self.height - 356)
    )
    self.relatedQuestList:initialise()
    self.relatedQuestList:instantiate()
    self.relatedQuestList:setFont(UIFont.Small, 5)
    self.relatedQuestList.drawBorder = true
    self.relatedQuestList:setOnMouseDownFunction(self, LCCQFKnownPeoplePage.onRelatedQuestSelected)
    self:addChild(self.relatedQuestList)

    self:refresh(true)
end

function LCCQFKnownPeoplePage:onPersonSelected(view)
    if type(view) == "table" and view.npcId then self:selectPerson(view.npcId) end
end

function LCCQFKnownPeoplePage:onRelatedQuestSelected(quest)
    if type(quest) == "table" and quest.instanceId and Hub.OpenQuest then
        Hub.OpenQuest(quest.instanceId)
    end
end

function LCCQFKnownPeoplePage:selectPerson(npcId)
    if not KnownPeople.Get(npcId) then return false end
    self.selectedNpcId = npcId
    Hub.selectedPersonId = npcId
    selectListItemByField(self.personList, "npcId", npcId)
    self.nextPortraitRefreshMs = 0
    self:updateDetail()
    return true
end

function LCCQFKnownPeoplePage:applyPortrait(view)
    local entity = view and Runtime.ResolveClientEntity(view.npcId) or nil
    if entity then
        local ok = pcall(function() self.portrait:setCharacter(entity) end)
        self.portraitHasCharacter = ok
        self.portrait:setVisible(ok)
        local portrait = view.portrait or {}
        if tonumber(portrait.zoom) then self.portrait:setZoom(tonumber(portrait.zoom)) end
        if tonumber(portrait.yOffset) then self.portrait:setYOffset(tonumber(portrait.yOffset)) end
        if tonumber(portrait.xOffset) then self.portrait:setXOffset(tonumber(portrait.xOffset)) end
    else
        self.portraitHasCharacter = false
        self.portrait:setVisible(false)
    end
end

function LCCQFKnownPeoplePage:updateDetail()
    local view = self.selectedNpcId and KnownPeople.Get(self.selectedNpcId) or nil
    self.relatedQuestList:clear()

    if not view then
        self.info.text = localize("IGUI_LCCQF_Hub_NoPersonSelected", "No person selected")
        self.info:paginate()
        self.portrait:setVisible(false)
        self.portraitHasCharacter = false
        return
    end

    self:applyPortrait(view)

    local lines = {
        "<H2>" .. personName(view) .. "</H2>",
        "",
        localize(view.summaryKey, localize("IGUI_LCCQF_Hub_KnownPerson_DefaultSummary", "Known person")),
        "",
        "<H2>" .. localize("IGUI_LCCQF_Hub_Person_History", "Known history") .. "</H2>",
    }

    if #(view.facts or {}) == 0 then
        lines[#lines + 1] = localize("IGUI_LCCQF_Hub_Person_NoHistory", "You have not learned anything else yet.")
    else
        for _, fact in ipairs(view.facts or {}) do
            local title = localize(fact.titleKey, "")
            if title ~= "" then lines[#lines + 1] = "<H2>" .. title .. "</H2>" end
            lines[#lines + 1] = localize(fact.textKey, "")
            lines[#lines + 1] = ""
        end
    end

    self.info.text = table.concat(lines, "\n")
    self.info:paginate()
    self.info:setYScroll(0)

    local active, completed = questsForPerson(view.npcId)
    for _, quest in ipairs(active) do
        self.relatedQuestList:addItem(
            "[" .. localize("IGUI_LCCQF_Hub_State_Active", "Active") .. "] " .. questTitle(quest),
            quest
        )
    end
    for _, quest in ipairs(completed) do
        self.relatedQuestList:addItem(
            "[" .. localize("IGUI_LCCQF_Hub_State_Completed", "Completed") .. "] " .. questTitle(quest),
            quest
        )
    end
end

function LCCQFKnownPeoplePage:refresh(force)
    local knowledgeRevision = KnownPeople.GetRevision()
    local questRevision = QuestState.GetRevision()
    if not force and knowledgeRevision == self.lastKnowledgeRevision and questRevision == self.lastQuestRevision then return end
    self.lastKnowledgeRevision = knowledgeRevision
    self.lastQuestRevision = questRevision

    local people = KnownPeople.ListAll()
    if self.selectedNpcId and not KnownPeople.Get(self.selectedNpcId) then self.selectedNpcId = nil end
    if not self.selectedNpcId and people[1] then self.selectedNpcId = people[1].npcId end
    Hub.selectedPersonId = self.selectedNpcId

    self.personList:clear()
    for _, view in ipairs(people) do
        self.personList:addItem(personName(view), view)
    end
    if self.selectedNpcId then selectListItemByField(self.personList, "npcId", self.selectedNpcId) end

    self:updateDetail()
end

function LCCQFKnownPeoplePage:update()
    ISPanel.update(self)
    self:refresh(false)
    local now = getTimestampMs()
    if self.selectedNpcId and now >= self.nextPortraitRefreshMs then
        self.nextPortraitRefreshMs = now + 1000
        local view = KnownPeople.Get(self.selectedNpcId)
        local before = self.portraitHasCharacter
        self:applyPortrait(view)
        if before ~= self.portraitHasCharacter then self:updateDetail() end
    end
end

function LCCQFKnownPeoplePage:prerender()
    ISPanel.prerender(self)
    self:drawText(localize("IGUI_LCCQF_Hub_KnownPeople", "Known people"), 12, 18, 1, 1, 1, 1, UIFont.Medium)
    self:drawRect(self.listWidth, 12, 1, self.height - 24, 0.35, 0.6, 0.6, 0.6)

    if self.selectedNpcId then
        local view = KnownPeople.Get(self.selectedNpcId)
        self:drawText(personName(view), self.listWidth + 28, 18, 1, 1, 1, 1, UIFont.Medium)
        self:drawText(
            localize("IGUI_LCCQF_Hub_Person_Quests", "Assignments"),
            self.listWidth + 28,
            318,
            1, 1, 1, 1,
            UIFont.Medium
        )
        if not self.portraitHasCharacter then
            self:drawRectBorder(self.listWidth + 28, 58, 210, 250, 0.65, 0.45, 0.45, 0.45)
            self:drawTextCentre(
                localize("IGUI_LCCQF_Hub_Portrait_Unavailable", "Portrait unavailable while NPC is not loaded"),
                self.listWidth + 133,
                174,
                0.7, 0.7, 0.7, 1,
                UIFont.Small
            )
        end
    else
        self:drawText(
            localize("IGUI_LCCQF_Hub_NoKnownPeople", "You do not know anyone yet."),
            self.listWidth + 28,
            58,
            0.8, 0.8, 0.8, 1,
            UIFont.Medium
        )
    end
end

LCCQFQuestJournalPage = ISPanel:derive("LCCQFQuestJournalPage")

function LCCQFQuestJournalPage:new(x, y, width, height)
    local o = ISPanel:new(x, y, width, height)
    setmetatable(o, self)
    self.__index = self
    o.background = false
    o.listWidth = math.min(300, math.floor(width * 0.37))
    o.lastRevision = -1
    o.selectedQuestId = Hub.selectedQuestId
    o.showCompleted = false
    return o
end

function LCCQFQuestJournalPage:initialise()
    ISPanel.initialise(self)
end

function LCCQFQuestJournalPage:createChildren()
    self.activeList = ISScrollingListBox:new(12, 54, self.listWidth - 24, 200)
    self.activeList:initialise()
    self.activeList:instantiate()
    self.activeList:setFont(UIFont.Small, 7)
    self.activeList.drawBorder = true
    self.activeList:setOnMouseDownFunction(self, LCCQFQuestJournalPage.onQuestSelected)
    self:addChild(self.activeList)

    self.completedToggle = ISButton:new(
        12, 270, self.listWidth - 24, 30,
        "", self, LCCQFQuestJournalPage.onCompletedToggle
    )
    self.completedToggle:initialise()
    self.completedToggle:instantiate()
    self:addChild(self.completedToggle)

    self.completedList = ISScrollingListBox:new(12, 308, self.listWidth - 24, 120)
    self.completedList:initialise()
    self.completedList:instantiate()
    self.completedList:setFont(UIFont.Small, 6)
    self.completedList.drawBorder = true
    self.completedList:setOnMouseDownFunction(self, LCCQFQuestJournalPage.onQuestSelected)
    self:addChild(self.completedList)

    self.detail = ISRichTextPanel:new(
        self.listWidth + 28,
        60,
        self.width - self.listWidth - 44,
        self.height - 112
    )
    self.detail:initialise()
    self.detail:instantiate()
    self.detail.autosetheight = false
    self.detail.background = false
    self.detail.borderColor = { r = 0, g = 0, b = 0, a = 0 }
    self:addChild(self.detail)

    self.giverButton = ISButton:new(
        self.listWidth + 28,
        self.height - 42,
        self.width - self.listWidth - 44,
        30,
        "",
        self,
        LCCQFQuestJournalPage.onGiverPressed
    )
    self.giverButton:initialise()
    self.giverButton:instantiate()
    self.giverButton:setVisible(false)
    self:addChild(self.giverButton)

    self:layoutLists()
    self:refresh(true)
end

function LCCQFQuestJournalPage:layoutLists()
    local top = 54
    if self.showCompleted then
        local activeHeight = math.max(110, math.floor((self.height - 98) * 0.52))
        self.activeList:setY(top)
        self.activeList:setHeight(activeHeight)
        self.completedToggle:setY(top + activeHeight + 8)
        self.completedList:setY(self.completedToggle:getBottom() + 8)
        self.completedList:setHeight(math.max(64, self.height - self.completedList:getY() - 12))
        self.completedList:setVisible(true)
    else
        self.activeList:setY(top)
        self.activeList:setHeight(math.max(130, self.height - top - 54))
        self.completedToggle:setY(self.height - 42)
        self.completedList:setVisible(false)
    end
end

function LCCQFQuestJournalPage:onQuestSelected(quest)
    if type(quest) == "table" and quest.instanceId then self:selectQuest(quest.instanceId) end
end

function LCCQFQuestJournalPage:onCompletedToggle()
    self.showCompleted = not self.showCompleted
    self:layoutLists()
    self:updateCompletedToggleTitle()
end

function LCCQFQuestJournalPage:onGiverPressed()
    local quest = self.selectedQuestId and QuestState.Get(self.selectedQuestId) or nil
    if quest and quest.giverNpcId and KnownPeople.Get(quest.giverNpcId) and Hub.OpenPerson then
        Hub.OpenPerson(quest.giverNpcId)
    end
end

function LCCQFQuestJournalPage:selectQuest(instanceId)
    local quest = QuestState.Get(instanceId)
    if not quest then return false end
    self.selectedQuestId = instanceId
    Hub.selectedQuestId = instanceId

    if quest.state == "completed" and not self.showCompleted then
        self.showCompleted = true
        self:layoutLists()
        self:updateCompletedToggleTitle()
    end

    selectListItemByField(self.activeList, "instanceId", instanceId)
    selectListItemByField(self.completedList, "instanceId", instanceId)
    self:updateDetail()
    return true
end

function LCCQFQuestJournalPage:updateCompletedToggleTitle()
    local completedCount = #(self.completedList.items or {})
    self.completedToggle:setTitle(
        (self.showCompleted and "- " or "+ ")
        .. localize("IGUI_LCCQF_Hub_CompletedQuests", "Completed quests")
        .. " (" .. tostring(completedCount) .. ")"
    )
end

function LCCQFQuestJournalPage:updateDetail()
    local view = self.selectedQuestId and QuestState.Get(self.selectedQuestId) or nil
    if not view then
        self.detail.text = localize("IGUI_LCCQF_Hub_NoQuests", "There are no quests yet.")
        self.detail:paginate()
        self.detail:setYScroll(0)
        self.giverButton:setVisible(false)
        return
    end

    local lines = {
        "<H1>" .. questTitle(view) .. "</H1>",
        "",
        localize(view.descriptionKey, ""),
        "",
        localize("IGUI_LCCQF_Hub_Status", "Status") .. ": " .. questStateText(view.state),
        "",
        "<H2>" .. localize("IGUI_LCCQF_Hub_Objectives", "Objectives") .. "</H2>",
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
        lines[#lines + 1] = localize(
            "IGUI_LCCQF_Hub_MapMarkerActive",
            "The active objective is marked on the world map."
        )
    end

    self.detail.text = table.concat(lines, "\n")
    self.detail:paginate()
    self.detail:setYScroll(0)

    local giver = view.giverNpcId and KnownPeople.Get(view.giverNpcId) or nil
    if giver then
        self.giverButton:setTitle(
            localize("IGUI_LCCQF_Hub_Quest_Giver", "Quest giver")
            .. ": " .. personName(giver) .. "  >"
        )
        self.giverButton:setVisible(true)
    else
        self.giverButton:setVisible(false)
    end
end

function LCCQFQuestJournalPage:refresh(force)
    local revision = QuestState.GetRevision()
    if not force and revision == self.lastRevision then return end
    self.lastRevision = revision

    local active, completed = {}, {}
    for _, quest in ipairs(QuestState.ListAll()) do
        if quest.state == "active" then
            active[#active + 1] = quest
        elseif quest.state == "completed" then
            completed[#completed + 1] = quest
        end
    end

    if self.selectedQuestId and not QuestState.Get(self.selectedQuestId) then self.selectedQuestId = nil end
    if not self.selectedQuestId and active[1] then self.selectedQuestId = active[1].instanceId end
    if not self.selectedQuestId and completed[1] then self.selectedQuestId = completed[1].instanceId end
    Hub.selectedQuestId = self.selectedQuestId

    self.activeList:clear()
    for _, quest in ipairs(active) do self.activeList:addItem(questTitle(quest), quest) end

    self.completedList:clear()
    for _, quest in ipairs(completed) do self.completedList:addItem(questTitle(quest), quest) end

    self:updateCompletedToggleTitle()
    if self.selectedQuestId then
        selectListItemByField(self.activeList, "instanceId", self.selectedQuestId)
        selectListItemByField(self.completedList, "instanceId", self.selectedQuestId)
    end
    self:updateDetail()
end

function LCCQFQuestJournalPage:update()
    ISPanel.update(self)
    self:refresh(false)
end

function LCCQFQuestJournalPage:prerender()
    ISPanel.prerender(self)
    self:drawText(
        localize("IGUI_LCCQF_Hub_ActiveQuests", "Active quests"),
        12, 18, 1, 1, 1, 1, UIFont.Medium
    )
    self:drawRect(self.listWidth, 12, 1, self.height - 24, 0.35, 0.6, 0.6, 0.6)
end

function Hub.OpenPerson(npcId)
    if not KnownPeople.Get(npcId) then return false end
    Hub.selectedPersonId = npcId
    if not Hub.window then Hub.Toggle() end
    if not Hub.window then return false end
    Hub.window:setVisible(true)
    Hub.window:bringToTop()
    Hub.window:switchPage("known_people")
    local page = Hub.window.pagePanels.known_people
    if page and page.selectPerson then page:selectPerson(npcId) end
    return true
end

function Hub.OpenQuest(instanceId)
    if not QuestState.Get(instanceId) then return false end
    Hub.selectedQuestId = instanceId
    if not Hub.window then Hub.Toggle() end
    if not Hub.window then return false end
    Hub.window:setVisible(true)
    Hub.window:bringToTop()
    Hub.window:switchPage("quests")
    local page = Hub.window.pagePanels.quests
    if page and page.selectQuest then page:selectQuest(instanceId) end
    return true
end

removeRegisteredPage("quests")
removeRegisteredPage("known_people")

Hub.RegisterPage({
    id = "known_people",
    labelKey = "IGUI_LCCQF_Hub_Tab_KnownPeople",
    create = function(window, x, y, width, height)
        return LCCQFKnownPeoplePage:new(x, y, width, height)
    end,
})

Hub.RegisterPage({
    id = "quests",
    labelKey = "IGUI_LCCQF_Hub_Tab_Quests",
    create = function(window, x, y, width, height)
        return LCCQFQuestJournalPage:new(x, y, width, height)
    end,
})

return true
