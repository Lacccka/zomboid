if not EFZ then EFZ = {} end

-- ============================================================
-- 오버레이 마커 (월드맵/미니맵 렌더) + ModData 영구 저장
-- ============================================================

EFZ.OverlayMarkers = EFZ.OverlayMarkers or {}
EFZ._OverlayHooked = EFZ._OverlayHooked or false

local function normalizeColor(color)
    if type(color) ~= "table" then
        return { r = 1, g = 1, b = 1, a = 1 }
    end
    local function clamp(v, fallback)
        if type(v) ~= "number" then
            return fallback
        end
        if v < 0 then return 0 end
        if v > 1 then return 1 end
        return v
    end
    return {
        r = clamp(color.r, 1),
        g = clamp(color.g, 1),
        b = clamp(color.b, 1),
        a = clamp(color.a, 1),
    }
end

local function resolveOverlayTexture(idOrPath)
    if not idOrPath or idOrPath == "" then
        return getTexture("media/ui/previoustarget.png")
    end
    local tex = getTexture(idOrPath)
    if tex then return tex end
    tex = getTexture("media/ui/" .. idOrPath)
    if tex then return tex end
    tex = getTexture("media/ui/worldmap/" .. idOrPath)
    return tex
end

local function drawOverlay(self, api, marker)
    if not api or not marker or not marker.x or not marker.y then
        return
    end
    local texture = resolveOverlayTexture(marker.texture or "previoustarget.png")
    if not texture then
        return
    end
    local uiX = api.worldToUIX and api:worldToUIX(marker.x, marker.y) or nil
    local uiY = api.worldToUIY and api:worldToUIY(marker.x, marker.y) or nil
    if not uiX or not uiY then
        return
    end
    local worldScale = api.getWorldScale and api:getWorldScale() or 1.0
    local scale = math.max(1, worldScale)
    local baseSize = marker.size or 20
    local size = baseSize * scale
    local half = size / 2
    local color = normalizeColor(marker.color)
    self:setStencilRect(0, 0, self.width, self.height)
    self:drawTextureScaled(texture, uiX - half, uiY - half, size, size, color.a or 1, color.r or 1, color.g or 1, color.b or 1)
    self:clearStencilRect()
end

function EFZ.RenderOverlayMarkers(self)
    if not self or not EFZ.OverlayMarkers then
        return
    end
    local javaObject = self.javaObject
    local api = javaObject and javaObject.getAPI and javaObject:getAPI() or nil
    if not api then
        return
    end
    for _, marker in pairs(EFZ.OverlayMarkers) do
        drawOverlay(self, api, marker)
    end
end

local function persistOverlayMarker(id, marker)
    local modData = ModData.getOrCreate("EFZ_OverlayMarkers")
    modData[id] = marker
end

local function removePersistedOverlayMarker(id)
    local modData = ModData.getOrCreate("EFZ_OverlayMarkers")
    modData[id] = nil
end

function EFZ.AddOverlayMarker(id, x, y, opts)
    opts = opts or {}
    local marker = {
        x = x,
        y = y,
        texture = opts.texture or opts.textureName or "previoustarget.png",
        size = opts.size or 20,
        color = normalizeColor(opts.color),
    }
    EFZ.OverlayMarkers[id] = marker
    persistOverlayMarker(id, marker)
end

function EFZ.RemoveOverlayMarker(id)
    EFZ.OverlayMarkers[id] = nil
    removePersistedOverlayMarker(id)
end

-- 기존 호출 호환성: AddMapMarker/RemoveMapMarker 를 오버레이 버전으로 위임
function EFZ.AddMapMarker(id, x, y, textureName)
    return EFZ.AddOverlayMarker(id, x, y, { texture = textureName })
end
function EFZ.RemoveMapMarker(id)
    return EFZ.RemoveOverlayMarker(id)
end

local function loadPersistedOverlays()
    local modData = ModData.getOrCreate("EFZ_OverlayMarkers")
    for id, data in pairs(modData) do
        if data and data.x and data.y then
            EFZ.OverlayMarkers[id] = {
                x = data.x,
                y = data.y,
                texture = data.texture or data.textureName or "previoustarget.png",
                size = data.size or 20,
                color = normalizeColor(data.color),
            }
        end
    end
end

local function installOverlayHooks()
    if EFZ._OverlayHooked then
        return
    end
    if ISWorldMap and ISWorldMap.render then
        local original = ISWorldMap.render
        ISWorldMap.render = function(self, ...)
            local result = nil
            if original then
                result = original(self, ...)
            end
            EFZ.RenderOverlayMarkers(self)
            return result
        end
    end
    if ISMiniMapInner and ISMiniMapInner.render then
        local originalMini = ISMiniMapInner.render
        ISMiniMapInner.render = function(self, ...)
            local result = nil
            if originalMini then
                result = originalMini(self, ...)
            end
            EFZ.RenderOverlayMarkers(self)
            return result
        end
    end
    EFZ._OverlayHooked = true
end

-- OnGameStart 시점에 훅 설치 및 저장된 오버레이 복원
Events.OnGameStart.Add(function()
    loadPersistedOverlays()
    installOverlayHooks()
end)
