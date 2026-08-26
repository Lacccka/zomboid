require "ISUI/ISPanel"
require "ISUI/ISButton"
require "ISUI/ISRichTextPanel"
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
    local active, archived = {}, {}
    for _, quest in ipairs(QuestState.ListAll()) do
        if quest.giverNpcId == npcId then
            if quest.state == "active" then active[#active + 1] = quest
            else archived[#archived + 1] = quest end
        end
    end
    return active, archived
end

local function removeRegisteredPage(pageId)
    for index = #Hub.pages, 1, -1 do
        if Hub.pages[index].id == pageId then table.remove(Hub.pages, index) end
    end
end

LCCQFKnownPeoplePage = ISPanel:derive("LCCQFKnownPeoplePage")

function LCCQFKnownPeoplePage:new(x, y, width, height)
    local o = ISPanel:new(x, y, width, height)
    setmetatable(o, self)
    self.__index = self
    o.background = false
    o.personButtons = {}
    o.questButtons = {}
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

    self.info = ISRichTextPanel:new(detailX + 230, 58, self.width - detailX - 246, 245)
    self.info:initialise()
    self.info:instantiate()
    self.info.autosetheight = false
    self.info.background = false
    self.info.borderColor = { r = 0, g = 0, b = 0, a = 0 }
    self:addChild(self.info)

    self:refresh(true)
end

function LCCQFKnownPeoplePage:clearButtons(buttons)
    for _, button in ipairs(buttons) do self:removeChild(button) end
    return {}
end

function LCCQFKnownPeoplePage:onPersonPressed(button)
    if button and button.npcId then self:selectPerson(button.npcId) end
end

function LCCQFKnownPeoplePage:onRelatedQuestPressed(button)
    if button and button.instanceId and Hub.OpenQuest then Hub.OpenQuest(button.instanceId) end
end

function LCCQFKnownPeoplePage:selectPerson(npcId)
    if not KnownPeople.Get(npcId) then return false end
    self.selectedNpcId = npcId
    Hub.selectedPersonId = npcId
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
    self.questButtons = self:clearButtons(self.questButtons)
    local view = self.selectedNpcId and KnownPeople.Get(self.selectedNpcId) or nil
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
            lines[#lines + 1] = "<H2>" .. localize(fact.titleKey, "") .. "</H2>"
            lines[#lines + 1] = localize(fact.textKey, "")
            lines[#lines + 1] = ""
        end
    end

    self.info.text = table.concat(lines, "\n")
    self.info:paginate()
    self.info:setYScroll(0)

    local active, archived = questsForPerson(view.npcId)
    local detailX = self.listWidth + 28
    local y = 332
    local function addQuestSection(labelKey, fallback, quests, maxCount)
        if #quests == 0 then return end
        local label = ISButton:new(detailX, y, 210, 28,
            localize(labelKey, fallback) .. " (" .. tostring(#quests) .. ")", nil, nil)
        label:initialise(); label:instantiate(); label.enable = false
        self:addChild(label); self.questButtons[#self.questButtons + 1] = label
        y = y + 32
        for index = 1, math.min(#quests, maxCount) do
            local quest = quests[index]
            local button = ISButton:new(detailX, y, self.width - detailX - 14, 30,
                questTitle(quest) .. "  >", self, LCCQFKnownPeoplePage.onRelatedQuestPressed)
            button:initialise(); button:instantiate(); button.instanceId = quest.instanceId
            self:addChild(button); self.questButtons[#self.questButtons + 1] = button
            y = y + 34
        end
    end

    addQuestSection("IGUI_LCCQF_Hub_Person_ActiveQuests", "Active assignments", active, 2)
    addQuestSection("IGUI_LCCQF_Hub_Person_CompletedQuests", "Completed assignments", archived, 2)
end

function LCCQFKnownPeoplePage:refresh(force)
    local knowledgeRevision = KnownPeople.GetRevision()
    local questRevision = QuestState.GetRevision()
    if not force and knowledgeRevision == self.lastKnowledgeRevision and questRevision == self.lastQuestRevision then return end
    self.lastKnowledgeRevision = knowledgeRevision
    self.lastQuestRevision = questRevision

    self.personButtons = self:clearButtons(self.personButtons)
    local people = KnownPeople.ListAll()
    if self.selectedNpcId and not KnownPeople.Get(self.selectedNpcId) then self.selectedNpcId = nil end
    if not self.selectedNpcId and people[1] then self.selectedNpcId = people[1].npcId end
    Hub.selectedPersonId = self.selectedNpcId

    local y = 58
    local maxVisible = math.max(1, math.floor((self.height - y - 20) / 40))
    for index = 1, math.min(#people, maxVisible) do
        local view = people[index]
        local button = ISButton:new(12, y, self.listWidth - 24, 32,
            personName(view), self, LCCQFKnownPeoplePage.onPersonPressed)
        button:initialise(); button:instantiate(); button.npcId = view.npcId
        self:addChild(button); self.personButtons[#self.personButtons + 1] = button
        y = y + 40
    end

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
        if not self.portraitHasCharacter then
            self:drawRectBorder(self.listWidth + 28, 58, 210, 250, 0.65, 0.45, 0.45, 0.45)
            self:drawTextCentre(localize("IGUI_LCCQF_Hub_Portrait_Unavailable", "Portrait unavailable while NPC is not loaded"),
                self.listWidth + 133, 174, 0.7, 0.7, 0.7, 1, UIFont.Small)
        end
    else
        self:drawText(localize("IGUI_LCCQF_Hub_NoKnownPeople", "You do not know anyone yet."),
            self.listWidth + 28, 58, 0.8, 0.8, 0.8, 1, UIFont.Medium)
    end
end

LCCQFQuestJournalPage = ISPanel:derive("LCCQFQuestJournalPage")

function LCCQFQuestJournalPage:new(x, y, width, height)
    local o = ISPanel:new(x, y, width, height)
    setmetatable(o, self)
    self.__index = self
    o.background = false
    o.questButtons = {}
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
    self.detail = ISRichTextPanel:new(self.listWidth + 28, 60,
        self.width - self.listWidth - 44, self.height - 112)
    self.detail:initialise(); self.detail:instantiate()
    self.detail.autosetheight = false
    self.detail.background = false
    self.detail.borderColor = { r = 0, g = 0, b = 0, a = 0 }
    self:addChild(self.detail)

    self.giverButton = ISButton:new(self.listWidth + 28, self.height - 42,
        self.width - self.listWidth - 44, 30, "", self, LCCQFQuestJournalPage.onGiverPressed)
    self.giverButton:initialise(); self.giverButton:instantiate(); self.giverButton:setVisible(false)
    self:addChild(self.giverButton)

    self:refresh(true)
end

function LCCQFQuestJournalPage:clearQuestButtons()
    for _, button in ipairs(self.questButtons) do self:removeChild(button) end
    self.questButtons = {}
end

function LCCQFQuestJournalPage:onQuestPressed(button)
    if button and button.instanceId then self:selectQuest(button.instanceId) end
end

function LCCQFQuestJournalPage:onCompletedToggle()
    self.showCompleted = not self.showCompleted
    self:refresh(true)
end

function LCCQFQuestJournalPage:onGiverPressed()
    local quest = self.selectedQuestId and QuestState.Get(self.selectedQuestId) or nil
    if quest and quest.giverNpcId and KnownPeople.Get(quest.giverNpcId) and Hub.OpenPerson then
        Hub.OpenPerson(quest.giverNpcId)
    end
end

function LCCQFQuestJournalPage:selectQuest(instanceId)
    if not QuestState.Get(instanceId) then return false end
    self.selectedQuestId = instanceId
    Hub.selectedQuestId = instanceId
    self:updateDetail()
    return true
end

function LCCQFQuestJournalPage:updateDetail()
    local view = self.selectedQuestId and QuestState.Get(self.selectedQuestId) or nil
    if not view then
        self.detail.text = localize("IGUI_LCCQF_Hub_NoQuests", "There are no quests yet.")
        self.detail:paginate(); self.detail:setYScroll(0)
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
        if objective.state == "completed" then prefix = "[x] "
        elseif objective.state == "active" then prefix = "[>] " end
        lines[#lines + 1] = prefix .. localize(objective.titleKey, tostring(objective.id or "Objective"))
            .. objectiveProgressSuffix(objective)
    end
    if view.marker and view.state == "active" then
        lines[#lines + 1] = ""
        lines[#lines + 1] = localize("IGUI_LCCQF_Hub_MapMarkerActive", "The active objective is marked on the world map.")
    end

    self.detail.text = table.concat(lines, "\n")
    self.detail:paginate(); self.detail:setYScroll(0)

    local giver = view.giverNpcId and KnownPeople.Get(view.giverNpcId) or nil
    if giver then
        self.giverButton:setTitle(localize("IGUI_LCCQF_Hub_Quest_Giver", "Quest giver")
            .. ": " .. personName(giver) .. "  >")
        self.giverButton:setVisible(true)
    else
        self.giverButton:setVisible(false)
    end
end

function LCCQFQuestJournalPage:refresh(force)
    local revision = QuestState.GetRevision()
    if not force and revision == self.lastRevision then return end
    self.lastRevision = revision
    self:clearQuestButtons()

    local active, archived = {}, {}
    for _, quest in ipairs(QuestState.ListAll()) do
        if quest.state == "active" then active[#active + 1] = quest
        else archived[#archived + 1] = quest end
    end

    if self.selectedQuestId and not QuestState.Get(self.selectedQuestId) then self.selectedQuestId = nil end
    if not self.selectedQuestId and active[1] then self.selectedQuestId = active[1].instanceId end
    if not self.selectedQuestId and archived[1] then self.selectedQuestId = archived[1].instanceId end
    Hub.selectedQuestId = self.selectedQuestId

    local y = 54
    local activeLimit = math.max(1, math.floor((self.height * 0.56 - y) / 38))
    for index = 1, math.min(#active, activeLimit) do
        local quest = active[index]
        local button = ISButton:new(12, y, self.listWidth - 24, 30,
            questTitle(quest), self, LCCQFQuestJournalPage.onQuestPressed)
        button:initialise(); button:instantiate(); button.instanceId = quest.instanceId
        self:addChild(button); self.questButtons[#self.questButtons + 1] = button
        y = y + 38
    end

    local archiveY = math.max(y + 8, math.floor(self.height * 0.58))
    local toggle = ISButton:new(12, archiveY, self.listWidth - 24, 30,
        (self.showCompleted and "- " or "+ ") .. localize("IGUI_LCCQF_Hub_CompletedQuests", "Completed")
            .. " (" .. tostring(#archived) .. ")", self, LCCQFQuestJournalPage.onCompletedToggle)
    toggle:initialise(); toggle:instantiate(); self:addChild(toggle); self.questButtons[#self.questButtons + 1] = toggle

    if self.showCompleted then
        local cy = archiveY + 38
        local maxArchived = math.max(1, math.floor((self.height - cy - 14) / 36))
        for index = 1, math.min(#archived, maxArchived) do
            local quest = archived[index]
            local button = ISButton:new(12, cy, self.listWidth - 24, 28,
                questTitle(quest), self, LCCQFQuestJournalPage.onQuestPressed)
            button:initialise(); button:instantiate(); button.instanceId = quest.instanceId
            self:addChild(button); self.questButtons[#self.questButtons + 1] = button
            cy = cy + 36
        end
    end

    self:updateDetail()
end

function LCCQFQuestJournalPage:update()
    ISPanel.update(self)
    self:refresh(false)
end

function LCCQFQuestJournalPage:prerender()
    ISPanel.prerender(self)
    self:drawText(localize("IGUI_LCCQF_Hub_ActiveQuests", "Active quests"), 12, 18, 1, 1, 1, 1, UIFont.Medium)
    self:drawRect(self.listWidth, 12, 1, self.height - 24, 0.35, 0.6, 0.6, 0.6)
end

function Hub.OpenPerson(npcId)
    if not KnownPeople.Get(npcId) then return false end
    Hub.selectedPersonId = npcId
    if not Hub.window then Hub.Toggle() end
    if not Hub.window then return false end
    Hub.window:setVisible(true); Hub.window:bringToTop(); Hub.window:switchPage("known_people")
    local page = Hub.window.pagePanels.known_people
    if page and page.selectPerson then page:selectPerson(npcId) end
    return true
end

function Hub.OpenQuest(instanceId)
    if not QuestState.Get(instanceId) then return false end
    Hub.selectedQuestId = instanceId
    if not Hub.window then Hub.Toggle() end
    if not Hub.window then return false end
    Hub.window:setVisible(true); Hub.window:bringToTop(); Hub.window:switchPage("quests")
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
