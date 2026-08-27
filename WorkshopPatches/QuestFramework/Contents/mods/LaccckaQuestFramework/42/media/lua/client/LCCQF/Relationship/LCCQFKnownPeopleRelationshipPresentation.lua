require "LCCQF/LCCQFConstants"
require "LCCQF/Knowledge/LCCQFKnownPeopleClientState"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
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

local function score(value)
    return tostring(math.floor(tonumber(value) or 0))
end

local function tierText(tier)
    local keys = {
        neutral = "IGUI_LCCQF_Hub_Relation_Tier_Neutral",
        friendly = "IGUI_LCCQF_Hub_Relation_Tier_Friendly",
        trusted = "IGUI_LCCQF_Hub_Relation_Tier_Trusted",
        wary = "IGUI_LCCQF_Hub_Relation_Tier_Wary",
        hostile = "IGUI_LCCQF_Hub_Relation_Tier_Hostile",
    }
    local key = keys[tostring(tier or "neutral")] or keys.neutral
    return localize(key, tostring(tier or "neutral"))
end

local function install()
    if type(LCCQFKnownPeoplePage) ~= "table" or type(LCCQFKnownPeoplePage.updateDetail) ~= "function" then
        return false
    end
    if LCCQFKnownPeoplePage.__LCCQFRelationshipPresentation then return true end

    local originalUpdateDetail = LCCQFKnownPeoplePage.updateDetail
    LCCQFKnownPeoplePage.updateDetail = function(self)
        originalUpdateDetail(self)

        local view = self.selectedNpcId and KnownPeople.Get(self.selectedNpcId) or nil
        local relationship = view and view.relationship or nil
        if type(relationship) ~= "table" or not self.info then return end

        local lines = {
            self.info.text or "",
            "",
            "<H2>" .. localize("IGUI_LCCQF_Hub_Person_Relationship", "Relationship") .. "</H2>",
            localize("IGUI_LCCQF_Hub_Relation_Status", "Status") .. ": " .. tierText(relationship.tier),
            localize("IGUI_LCCQF_Hub_Relation_Trust", "Trust") .. ": " .. score(relationship.trust),
            localize("IGUI_LCCQF_Hub_Relation_Reputation", "Reputation") .. ": " .. score(relationship.reputation),
            localize("IGUI_LCCQF_Hub_Relation_Hostility", "Hostility") .. ": " .. score(relationship.hostility),
        }

        self.info.text = table.concat(lines, "\n")
        self.info:paginate()
        self.info:setYScroll(0)
    end

    LCCQFKnownPeoplePage.__LCCQFRelationshipPresentation = true
    print(C.LOG_PREFIX .. "[RELATIONSHIP:CLIENT] Known People relationship presentation installed")
    return true
end

install()

return true
