-- Extends the existing quest journal so terminal quest states remain visible in history.
-- The base journal already renders failed/cancelled state labels; this layer only changes
-- list bucketing and the collapsed terminal-history label.
require "LCCQF/UI/LCCQFRPGJournalPages"
require "LCCQF/Quest/LCCQFQuestClientState"

local Hub = LCCQFHub
local QuestState = LCCQF.QuestClientState
local Page = LCCQFQuestJournalPage

if not Page or Page.__LCCQFTerminalHistoryInstalled == true then return true end

local function localize(key, fallback)
    local value = getText(key)
    if not value or value == key then return fallback or key end
    return value
end

local function questTitle(view)
    return localize(view and view.titleKey, tostring(view and view.questId or "Quest"))
end

local function selectListItem(list, instanceId)
    if not list or not instanceId then return false end
    for index, row in ipairs(list.items or {}) do
        local item = row and row.item or nil
        if item and tostring(item.instanceId) == tostring(instanceId) then
            list.selected = index
            return true
        end
    end
    return false
end

local originalSelectQuest = Page.selectQuest

function Page:selectQuest(instanceId)
    local quest = QuestState.Get(instanceId)
    if quest and quest.state ~= "active" and not self.showCompleted then
        self.showCompleted = true
        self:layoutLists()
        self:updateCompletedToggleTitle()
    end
    return originalSelectQuest(self, instanceId)
end

function Page:updateCompletedToggleTitle()
    local terminalCount = #(self.completedList.items or {})
    self.completedToggle:setTitle(
        (self.showCompleted and "- " or "+ ")
        .. localize("IGUI_LCCQF_Hub_QuestHistory", "Quest history")
        .. " (" .. tostring(terminalCount) .. ")"
    )
end

function Page:refresh(force)
    local revision = QuestState.GetRevision()
    if not force and revision == self.lastRevision then return end
    self.lastRevision = revision

    local active, terminal = {}, {}
    for _, quest in ipairs(QuestState.ListAll()) do
        if quest.state == "active" then
            active[#active + 1] = quest
        else
            terminal[#terminal + 1] = quest
        end
    end

    if self.selectedQuestId and not QuestState.Get(self.selectedQuestId) then self.selectedQuestId = nil end
    if not self.selectedQuestId and active[1] then self.selectedQuestId = active[1].instanceId end
    if not self.selectedQuestId and terminal[1] then self.selectedQuestId = terminal[1].instanceId end
    Hub.selectedQuestId = self.selectedQuestId

    self.activeList:clear()
    for _, quest in ipairs(active) do self.activeList:addItem(questTitle(quest), quest) end

    self.completedList:clear()
    for _, quest in ipairs(terminal) do self.completedList:addItem(questTitle(quest), quest) end

    self:updateCompletedToggleTitle()
    if self.selectedQuestId then
        selectListItem(self.activeList, self.selectedQuestId)
        selectListItem(self.completedList, self.selectedQuestId)
    end
    self:updateDetail()
end

Page.__LCCQFTerminalHistoryInstalled = true
return true
