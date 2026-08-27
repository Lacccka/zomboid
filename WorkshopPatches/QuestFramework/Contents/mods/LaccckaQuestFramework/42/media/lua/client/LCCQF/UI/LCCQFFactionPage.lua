require "ISUI/ISPanel"
require "ISUI/ISRichTextPanel"
require "ISUI/ISScrollingListBox"
require "LCCQF/LCCQFConstants"
require "LCCQF/Faction/LCCQFKnownFactionsClientState"
require "LCCQF/UI/LCCQFHub"

local C = LCCQF.Constants
local Hub = LCCQFHub
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

local function factionName(view)
    if not view then return localize("IGUI_LCCQF_Hub_UnknownFaction", "Unknown faction") end
    return localize(view.displayNameKey, tostring(view.factionId or "Faction"))
end

local function removeRegisteredPage(pageId)
    for index = #Hub.pages, 1, -1 do
        if Hub.pages[index].id == pageId then table.remove(Hub.pages, index) end
    end
end

local function selectByFactionId(list, factionId)
    if not list or not factionId then return false end
    for index, row in ipairs(list.items or {}) do
        local item = row.item
        if type(item) == "table" and item.factionId == factionId then
            list.selected = index
            list:ensureVisible(index)
            return true
        end
    end
    return false
end

LCCQFKnownFactionsPage = ISPanel:derive("LCCQFKnownFactionsPage")

function LCCQFKnownFactionsPage:new(x, y, width, height)
    local o = ISPanel:new(x, y, width, height)
    setmetatable(o, self)
    self.__index = self
    o.background = false
    o.listWidth = math.min(280, math.max(230, math.floor(width * 0.28)))
    o.detailX = o.listWidth + 30
    o.detailWidth = width - o.detailX - 12
    o.lastRevision = -1
    o.selectedFactionId = Hub.selectedFactionId
    return o
end

function LCCQFKnownFactionsPage:initialise()
    ISPanel.initialise(self)
end

function LCCQFKnownFactionsPage:createChildren()
    self.factionList = ISScrollingListBox:new(12, 54, self.listWidth - 24, self.height - 66)
    self.factionList:initialise()
    self.factionList:instantiate()
    self.factionList:setFont(UIFont.Small, 7)
    self.factionList.drawBorder = true
    self.factionList:setOnMouseDownFunction(self, LCCQFKnownFactionsPage.onFactionSelected)
    self:addChild(self.factionList)

    self.detail = ISRichTextPanel:new(
        self.detailX,
        58,
        self.detailWidth,
        self.height - 70
    )
    self.detail:initialise()
    self.detail:instantiate()
    self.detail.autosetheight = false
    self.detail.background = true
    self.detail.clip = true
    self.detail.defaultFont = UIFont.NewSmall
    self.detail.marginLeft = 16
    self.detail.marginRight = 16
    self.detail.marginTop = 14
    self.detail.marginBottom = 14
    self:addChild(self.detail)

    self:refresh(true)
end

function LCCQFKnownFactionsPage:onFactionSelected(view)
    if type(view) == "table" and view.factionId then self:selectFaction(view.factionId) end
end

function LCCQFKnownFactionsPage:selectFaction(factionId)
    if not KnownFactions.Get(factionId) then return false end
    self.selectedFactionId = factionId
    Hub.selectedFactionId = factionId
    selectByFactionId(self.factionList, factionId)
    self:updateDetail()
    return true
end

function LCCQFKnownFactionsPage:updateDetail()
    local view = self.selectedFactionId and KnownFactions.Get(self.selectedFactionId) or nil
    if not view then
        self.detail.text = localize("IGUI_LCCQF_Hub_NoFactionSelected", "No faction selected")
        self.detail:paginate()
        self.detail:setYScroll(0)
        return
    end

    local lines = {
        localize(view.summaryKey, localize("IGUI_LCCQF_Hub_KnownFaction_DefaultSummary", "Known faction")),
        "",
        "<H2>" .. localize("IGUI_LCCQF_Hub_Faction_History", "Known information") .. "</H2>",
    }

    if #(view.facts or {}) == 0 then
        lines[#lines + 1] = localize(
            "IGUI_LCCQF_Hub_Faction_NoHistory",
            "You have not learned anything else about this faction yet."
        )
    else
        for _, fact in ipairs(view.facts or {}) do
            local title = localize(fact.titleKey, "")
            if title ~= "" then lines[#lines + 1] = title end
            local text = localize(fact.textKey, "")
            if text ~= "" then lines[#lines + 1] = text end
            lines[#lines + 1] = ""
        end
    end

    self.detail.text = table.concat(lines, "\n")
    self.detail:paginate()
    self.detail:setYScroll(0)
end

function LCCQFKnownFactionsPage:refresh(force)
    local revision = KnownFactions.GetRevision()
    if not force and revision == self.lastRevision then return end
    self.lastRevision = revision

    local factions = KnownFactions.ListAll()
    if self.selectedFactionId and not KnownFactions.Get(self.selectedFactionId) then
        self.selectedFactionId = nil
    end
    if not self.selectedFactionId and factions[1] then
        self.selectedFactionId = factions[1].factionId
    end
    Hub.selectedFactionId = self.selectedFactionId

    self.factionList:clear()
    for _, view in ipairs(factions) do
        self.factionList:addItem(factionName(view), view)
    end
    if self.selectedFactionId then selectByFactionId(self.factionList, self.selectedFactionId) end
    self:updateDetail()
end

function LCCQFKnownFactionsPage:update()
    ISPanel.update(self)
    self:refresh(false)
end

function LCCQFKnownFactionsPage:prerender()
    ISPanel.prerender(self)
    self:drawText(
        localize("IGUI_LCCQF_Hub_KnownFactions", "Known factions"),
        12, 18, 1, 1, 1, 1, UIFont.Medium
    )

    if self.selectedFactionId then
        local view = KnownFactions.Get(self.selectedFactionId)
        self:drawText(factionName(view), self.detailX, 18, 1, 1, 1, 1, UIFont.Medium)
    elseif #(KnownFactions.ListAll()) == 0 then
        self:drawText(
            localize("IGUI_LCCQF_Hub_NoKnownFactions", "You have not discovered any factions yet."),
            self.detailX, 58, 0.8, 0.8, 0.8, 1, UIFont.Medium
        )
    end
end

function Hub.OpenFaction(factionId)
    if not KnownFactions.Get(factionId) then return false end
    Hub.selectedFactionId = factionId
    if not Hub.window then Hub.Toggle() end
    if not Hub.window then return false end
    Hub.window:setVisible(true)
    Hub.window:bringToTop()
    Hub.window:switchPage("factions")
    local page = Hub.window.pagePanels.factions
    if page and page.selectFaction then page:selectFaction(factionId) end
    return true
end

removeRegisteredPage("factions")
Hub.RegisterPage({
    id = "factions",
    labelKey = "IGUI_LCCQF_Hub_Tab_Factions",
    create = function(window, x, y, width, height)
        return LCCQFKnownFactionsPage:new(x, y, width, height)
    end,
})

return true
