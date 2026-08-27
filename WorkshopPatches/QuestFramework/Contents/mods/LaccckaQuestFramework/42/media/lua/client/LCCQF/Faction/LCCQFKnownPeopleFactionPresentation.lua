require "ISUI/ISButton"
require "LCCQF/LCCQFConstants"
require "LCCQF/Knowledge/LCCQFKnownPeopleClientState"
require "LCCQF/Faction/LCCQFKnownFactionsClientState"
require "LCCQF/UI/LCCQFHub"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Hub = LCCQFHub
local KnownPeople = LCCQF.KnownPeopleClientState
local KnownFactions = LCCQF.KnownFactionsClientState
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

local function factionName(faction)
    if not faction then return localize("IGUI_LCCQF_Hub_UnknownFaction", "Unknown faction") end
    return localize(faction.displayNameKey, tostring(faction.factionId or "Faction"))
end

local function install()
    if type(LCCQFKnownPeoplePage) ~= "table"
        or type(LCCQFKnownPeoplePage.createChildren) ~= "function"
        or type(LCCQFKnownPeoplePage.updateDetail) ~= "function"
    then
        return false
    end
    if LCCQFKnownPeoplePage.__LCCQFFactionPresentation then return true end

    local originalCreateChildren = LCCQFKnownPeoplePage.createChildren
    LCCQFKnownPeoplePage.createChildren = function(self)
        originalCreateChildren(self)
        if not self.info then return end

        local buttonHeight = 28
        local gap = 8
        local reserved = buttonHeight + gap + 8
        self.info:setHeight(math.max(90, self.portraitHeight - reserved))

        self.factionLinkButton = ISButton:new(
            self.infoX,
            self.detailTop + self.portraitHeight - buttonHeight - 8,
            self.infoWidth,
            buttonHeight,
            "",
            self,
            LCCQFKnownPeoplePage.onFactionLink
        )
        self.factionLinkButton:initialise()
        self.factionLinkButton:instantiate()
        self.factionLinkButton:setVisible(false)
        self:addChild(self.factionLinkButton)
    end

    function LCCQFKnownPeoplePage:onFactionLink()
        local person = self.selectedNpcId and KnownPeople.Get(self.selectedNpcId) or nil
        local factionId = person and person.faction and person.faction.factionId or nil
        if factionId and KnownFactions.Get(factionId) and Hub.OpenFaction then
            Hub.OpenFaction(factionId)
        end
    end

    local originalUpdateDetail = LCCQFKnownPeoplePage.updateDetail
    LCCQFKnownPeoplePage.updateDetail = function(self)
        originalUpdateDetail(self)
        if not self.factionLinkButton then return end

        local person = self.selectedNpcId and KnownPeople.Get(self.selectedNpcId) or nil
        local factionId = person and person.faction and person.faction.factionId or nil
        local faction = factionId and KnownFactions.Get(factionId) or nil
        if not faction then
            self.factionLinkButton:setVisible(false)
            return
        end

        self.factionLinkButton:setTitle(
            localize("IGUI_LCCQF_Hub_Person_Faction", "Faction")
                .. ": " .. factionName(faction) .. " >"
        )
        self.factionLinkButton:setVisible(true)
    end

    LCCQFKnownPeoplePage.__LCCQFFactionPresentation = true
    print(C.LOG_PREFIX .. "[FACTION:CLIENT] Known People faction cross-navigation installed")
    return true
end

install()
return true
