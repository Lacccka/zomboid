require "ISUI/ISScrollingListBox"
require "LCCQF/LCCQFConstants"
require "LCCQF/Knowledge/LCCQFKnownPeopleClientState"
require "LCCQF/UI/LCCQFHub"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Hub = LCCQFHub
local KnownPeople = LCCQF.KnownPeopleClientState
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

local function membersForFaction(factionId)
    local result = {}
    for _, person in ipairs(KnownPeople.ListAll()) do
        if type(person.faction) == "table" and person.faction.factionId == factionId then
            result[#result + 1] = person
        end
    end
    table.sort(result, function(a, b)
        return personName(a) < personName(b)
    end)
    return result
end

local function install()
    if type(LCCQFKnownFactionsPage) ~= "table"
        or type(LCCQFKnownFactionsPage.createChildren) ~= "function"
        or type(LCCQFKnownFactionsPage.updateDetail) ~= "function"
        or type(LCCQFKnownFactionsPage.update) ~= "function"
    then
        return false
    end
    if LCCQFKnownFactionsPage.__LCCQFKnownMembersPresentation then return true end

    local originalCreateChildren = LCCQFKnownFactionsPage.createChildren
    LCCQFKnownFactionsPage.createChildren = function(self)
        originalCreateChildren(self)
        self.lastKnownPeopleRevision = -1
        self.knownMembersTitleY = self.height - 150
        if self.detail then
            self.detail:setHeight(math.max(130, self.knownMembersTitleY - 58 - 10))
        end

        self.knownMembersList = ISScrollingListBox:new(
            self.detailX,
            self.knownMembersTitleY + 28,
            self.detailWidth,
            math.max(72, self.height - self.knownMembersTitleY - 40)
        )
        self.knownMembersList:initialise()
        self.knownMembersList:instantiate()
        self.knownMembersList:setFont(UIFont.Small, 6)
        self.knownMembersList.drawBorder = true
        self.knownMembersList:setOnMouseDownFunction(self, LCCQFKnownFactionsPage.onKnownMemberSelected)
        self:addChild(self.knownMembersList)
        self:refreshKnownMembers()
    end

    function LCCQFKnownFactionsPage:onKnownMemberSelected(person)
        if type(person) == "table" and person.npcId and Hub.OpenPerson then
            Hub.OpenPerson(person.npcId)
        end
    end

    function LCCQFKnownFactionsPage:refreshKnownMembers()
        if not self.knownMembersList then return end
        self.knownMembersList:clear()
        if not self.selectedFactionId then return end
        for _, person in ipairs(membersForFaction(self.selectedFactionId)) do
            self.knownMembersList:addItem(personName(person), person)
        end
    end

    local originalUpdateDetail = LCCQFKnownFactionsPage.updateDetail
    LCCQFKnownFactionsPage.updateDetail = function(self)
        originalUpdateDetail(self)
        self:refreshKnownMembers()
    end

    local originalUpdate = LCCQFKnownFactionsPage.update
    LCCQFKnownFactionsPage.update = function(self)
        originalUpdate(self)
        local revision = KnownPeople.GetRevision()
        if revision ~= self.lastKnownPeopleRevision then
            self.lastKnownPeopleRevision = revision
            self:refreshKnownMembers()
        end
    end

    local originalPrerender = LCCQFKnownFactionsPage.prerender
    LCCQFKnownFactionsPage.prerender = function(self)
        originalPrerender(self)
        if not self.knownMembersTitleY then return end
        self:drawText(
            localize("IGUI_LCCQF_Hub_Faction_KnownMembers", "Known members"),
            self.detailX,
            self.knownMembersTitleY,
            1, 1, 1, 1,
            UIFont.Medium
        )
        if self.selectedFactionId and #membersForFaction(self.selectedFactionId) == 0 then
            self:drawText(
                localize("IGUI_LCCQF_Hub_Faction_NoKnownMembers", "You do not personally know any members yet."),
                self.detailX + 4,
                self.knownMembersTitleY + 31,
                0.75, 0.75, 0.75, 1,
                UIFont.Small
            )
        end
    end

    LCCQFKnownFactionsPage.__LCCQFKnownMembersPresentation = true
    print(C.LOG_PREFIX .. "[FACTION:CLIENT] known faction member cross-navigation installed")
    return true
end

install()
return true
