require "ExtractionMode/Config"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local marker = nil
local arrow = nil
local markerKey = nil

local function removeMarkers()
    if marker then marker:remove(); marker = nil end
    if arrow then arrow:remove(); arrow = nil end
    markerKey = nil
end

local function updateMarkers()
    local data = ExtractionMode.ClientState or {}
    local rope = data.extractionRope
    local player = getPlayer and getPlayer()
    if data.state ~= Config.STATE_BOARDING or data.isParticipant ~= true
        or rope == nil or player == nil then
        removeMarkers()
        return
    end

    local key = tostring(data.raidId) .. ":" .. tostring(data.activeExtraction)
    if markerKey == key and marker and arrow then return end
    removeMarkers()

    local square = getCell():getGridSquare(math.floor(rope.x), math.floor(rope.y), math.floor(rope.z or 0))
    if square == nil then return end
    local ok = pcall(function()
        marker = getWorldMarkers():addGridSquareMarker(square, 0.95, 0.72, 0.12, true, 1.25)
        marker:setScaleCircleTexture(true)
        arrow = getWorldMarkers():addDirectionArrow(player, rope.x, rope.y, rope.z or 0,
            "Item_Rope2", 0.95, 0.72, 0.12, 1.0)
    end)
    if ok then markerKey = key else removeMarkers() end
end

Events.OnTick.Add(updateMarkers)
Events.OnMainMenuEnter.Add(removeMarkers)

ExtractionMode.removeExtractionRopeMarker = removeMarkers
return {}
