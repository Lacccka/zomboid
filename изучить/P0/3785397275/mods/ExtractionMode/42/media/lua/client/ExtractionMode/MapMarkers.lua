require "ISUI/ISPanel"
require "ISUI/Maps/ISWorldMap"
require "ExtractionMode/Config"
require "ExtractionMode/Localization"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local Overlay = ISPanel:derive("ExtractionModeMapOverlay")
local Localization = ExtractionMode.Localization

function Overlay:prerender()
    local data = ExtractionMode.ClientState or {}
    if self.map == nil or not self.map:isVisible() or data.isParticipant ~= true or
        (data.state ~= Config.STATE_RAID and data.state ~= Config.STATE_EXTRACTING
            and data.state ~= Config.STATE_BOARDING) then
        self:setVisible(false)
        if self.parent then self.parent:removeChild(self) end
        if ExtractionMode.MapOverlayInstance == self then ExtractionMode.MapOverlayInstance = nil end
        return
    end
    self:setWidth(self.map:getWidth())
    self:setHeight(self.map:getHeight())
end

function Overlay:render()
    local data = ExtractionMode.ClientState or {}
    if data.isParticipant ~= true then return end
    local bounds = data.raidBounds
    if bounds then
        local x1 = self.map.mapAPI:worldToUIX(bounds.minX, bounds.minY)
        local y1 = self.map.mapAPI:worldToUIY(bounds.minX, bounds.minY)
        local x2 = self.map.mapAPI:worldToUIX(bounds.maxX, bounds.minY)
        local y2 = self.map.mapAPI:worldToUIY(bounds.maxX, bounds.minY)
        local x3 = self.map.mapAPI:worldToUIX(bounds.maxX, bounds.maxY)
        local y3 = self.map.mapAPI:worldToUIY(bounds.maxX, bounds.maxY)
        local x4 = self.map.mapAPI:worldToUIX(bounds.minX, bounds.maxY)
        local y4 = self.map.mapAPI:worldToUIY(bounds.minX, bounds.maxY)
        local player = getPlayer and getPlayer()
        local outside = player and not Config.pointInsideRaidBounds(data.selectedTownKey, {
            x = player:getX(), y = player:getY(),
        })
        local red, green, blue = outside and 0.95 or 0.96,
            outside and 0.18 or 0.72, outside and 0.12 or 0.18
        self:drawLine(nil, x1, y1, x2, y2, 2, 0.9, red, green, blue)
        self:drawLine(nil, x2, y2, x3, y3, 2, 0.9, red, green, blue)
        self:drawLine(nil, x3, y3, x4, y4, 2, 0.9, red, green, blue)
        self:drawLine(nil, x4, y4, x1, y1, 2, 0.9, red, green, blue)
        local labelX, labelY = x1, y1
        if y2 < labelY then labelX, labelY = x2, y2 end
        if y3 < labelY then labelX, labelY = x3, y3 end
        if y4 < labelY then labelX, labelY = x4, y4 end
        self:drawText(Localization.get("IGUI_ExtractionMode_RaidBoundary", "RAID BOUNDARY"),
            labelX + 6, labelY + 4, red, green, blue, 1, UIFont.Small)
    end
    for _, site in ipairs(data.extractionSites or {}) do
        local x = self.map.mapAPI:worldToUIX(site.x, site.y)
        local y = self.map.mapAPI:worldToUIY(site.x, site.y)
        local active = tonumber(data.activeExtraction) == tonumber(site.id)
        -- A solid near-black badge stays legible over roads, grass, buildings,
        -- and the yellow-beige palette used by the vanilla world map.
        self:drawRect(x - 16, y - 16, 32, 32, 0.94, 0.02, 0.02, 0.02)
        self:drawRectBorder(x - 17, y - 17, 34, 34, 1, active and 0.9 or 1,
            active and 0.2 or 1, active and 0.15 or 1)
        self:drawTextCentre(Localization.get("IGUI_ExtractionMode_ExtractionMarker", "E%1",
            tostring(site.id)), x, y - 7, 1, 1, 1, 1, UIFont.Small)
    end
end

function Overlay:new(map)
    local object = ISPanel:new(0, 0, map:getWidth(), map:getHeight())
    setmetatable(object, self)
    self.__index = self
    object.map = map
    object.background = false
    object.border = false
    object.moveWithMouse = false
    return object
end

local function attachOverlay()
    local data = ExtractionMode.ClientState or {}
    if data.isParticipant ~= true then return end
    if data.state ~= Config.STATE_RAID and data.state ~= Config.STATE_EXTRACTING
        and data.state ~= Config.STATE_BOARDING then return end
    if ExtractionMode.MapOverlayInstance then return end
    if ISWorldMap_instance and ISWorldMap_instance:isVisible() then
        local overlay = Overlay:new(ISWorldMap_instance)
        overlay:initialise()
        -- This panel only draws raid markers. If it consumes mouse events it
        -- covers the entire world map and blocks its buttons, panning, and zoom.
        overlay:setWantMouseEvents(false)
        ISWorldMap_instance:addChild(overlay)
        overlay:setVisible(true)
        ExtractionMode.MapOverlayInstance = overlay
    end
end

Events.OnTick.Add(attachOverlay)
ExtractionMode.MapOverlay = Overlay
return Overlay
