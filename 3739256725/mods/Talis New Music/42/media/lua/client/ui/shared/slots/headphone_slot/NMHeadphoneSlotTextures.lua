_G.NMHeadphoneSlotEnv = _G.NMHeadphoneSlotEnv or {}
local env = _G.NMHeadphoneSlotEnv
if getmetatable(env) == nil then
    setmetatable(env, { __index = _G })
end
setfenv(1, env)

NMHeadphoneSlotTextures = NMHeadphoneSlotTextures or {}

function NMHeadphoneSlotTextures.resolveTextureByFullType(fullType)
    local ft = tostring(fullType or "")
    if ft == "" or not getTexture then
        return nil
    end
    local scriptItem = ScriptManager and ScriptManager.instance and ScriptManager.instance.FindItem
        and ScriptManager.instance:FindItem(ft) or nil
    if scriptItem and scriptItem.getIcon then
        local iconName = tostring(scriptItem:getIcon() or "")
        if iconName ~= "" then
            local tex = getTexture("Item_" .. iconName) or getTexture("media/textures/Item_" .. iconName .. ".png")
            if tex then
                return tex
            end
        end
    end
    local itemType = ft
    local dotPos = string.find(itemType, "%.")
    if dotPos then
        itemType = string.sub(itemType, dotPos + 1)
    end
    return getTexture("Item_" .. itemType) or getTexture("media/textures/Item_" .. itemType .. ".png")
end

function NMHeadphoneSlotTextures.resolveItemTextureForFullType(fullType)
    local key = tostring(fullType or "")
    if key == "" then
        return nil
    end
    if HEADPHONE_TEXTURE_CACHE[key] == nil then
        HEADPHONE_TEXTURE_CACHE[key] = NMHeadphoneSlotTextures.resolveTextureByFullType(key) or false
    end
    local tex = HEADPHONE_TEXTURE_CACHE[key]
    return tex ~= false and tex or nil
end

function NMHeadphoneSlotTextures.resolveInsertedHeadphoneTooltip(fullType)
    local resolvedType = tostring(fullType or "")
    if resolvedType == "" then
        return NMTranslations.ui("InsertHeadphones", "Insert Headphones")
    end
    if resolvedType == "Base.Earbuds" then
        local item = ScriptManager and ScriptManager.instance and ScriptManager.instance.FindItem
            and ScriptManager.instance:FindItem(resolvedType) or nil
        if item and item.getDisplayName then
            local ok, displayName = pcall(item.getDisplayName, item)
            if ok and tostring(displayName or "") ~= "" then
                return tostring(displayName)
            end
        end
        return "Earbuds"
    end
    return NMTranslations.ui("HeadphonesInserted", "Headphones Inserted")
end

function NMHeadphoneSlotTextures.drawEmptyHeadphoneVector(self)
    local icon = NMBatterySlotVectors.icons and NMBatterySlotVectors.icons.headphone_placeholder or nil
    local bounds = NMBatterySlotVectors.bounds and NMBatterySlotVectors.bounds.headphone_placeholder or nil
    if not (icon and bounds) then
        return false
    end
    if EMPTY_HEADPHONE_VECTOR_SHAPES == nil then
        EMPTY_HEADPHONE_VECTOR_SHAPES = {}
        for i = 1, #icon do
            EMPTY_HEADPHONE_VECTOR_SHAPES[i] = NMVectorDraw.prepareShape(icon[i], NMBatterySlotVectors.viewBox, bounds) or false
        end
    end
    local w = tonumber(self.width) or 0
    local h = tonumber(self.height) or 0
    local size = math.max(10, math.floor(math.min(w, h) * EMPTY_HEADPHONE_ICON_SCALE + 0.5))
    local left = math.floor((w - size) * 0.5 + 0.5)
    local top = math.floor((h - size) * 0.5 + 0.5)
    for i = 1, #EMPTY_HEADPHONE_VECTOR_SHAPES do
        local prepared = EMPTY_HEADPHONE_VECTOR_SHAPES[i]
        if prepared and prepared ~= false then
            NMVectorDraw.drawPreparedShape(self, prepared, EMPTY_SLOT_VECTOR_COLOR, left, top, size, size)
        end
    end
    return true
end

function NMHeadphoneSlotTextures.drawEmptyPlaceholder(self)
    if NMHeadphoneSlotTextures.drawEmptyHeadphoneVector(self) then
        return
    end

    if not EMPTY_PLACEHOLDER_HEADPHONE_TEXTURE then
        EMPTY_PLACEHOLDER_HEADPHONE_TEXTURE = getTexture and (
            getTexture("media/textures/UI/UI_NM_SlotEmpty_Headphones.png")
            or getTexture("UI/UI_NM_SlotEmpty_Headphones")
            or getTexture("UI_NM_SlotEmpty_Headphones")
            or getTexture("Item_NM_Headphones")
            or getTexture("media/textures/Item_NM_Headphones.png")
            or getTexture("Item_Headphones")
            or getTexture("media/textures/Item_Headphones.png")
        ) or nil
    end
    if EMPTY_PLACEHOLDER_HEADPHONE_TEXTURE then
        local tex = EMPTY_PLACEHOLDER_HEADPHONE_TEXTURE
        local texW = tex.getWidthOrig and tex:getWidthOrig() or 32
        local texH = tex.getHeightOrig and tex:getHeightOrig() or 32
        local maxW = self.width - 8
        local maxH = self.height - 8
        local scale = math.min(maxW / texW, maxH / texH)
        if scale > 1 then
            scale = 1
        end
        local drawW = texW * scale
        local drawH = texH * scale
        local dx = (self.width - drawW) / 2
        local dy = (self.height - drawH) / 2
        self:drawTextureScaled(tex, dx, dy, drawW, drawH, 0.55, 0.12, 0.12, 0.12)
        return
    end

    if not EMPTY_PLACEHOLDER_BATTERY_TEXTURE then
        EMPTY_PLACEHOLDER_BATTERY_TEXTURE = getTexture and (getTexture("Item_Battery") or getTexture("media/textures/Item_Battery.png")) or nil
    end
    local tex = EMPTY_PLACEHOLDER_BATTERY_TEXTURE
    if not tex then
        return
    end
    local texW = tex.getWidthOrig and tex:getWidthOrig() or 32
    local texH = tex.getHeightOrig and tex:getHeightOrig() or 32
    local maxW = self.width - 8
    local maxH = self.height - 8
    local scale = math.min(maxW / texW, maxH / texH)
    if scale > 1 then
        scale = 1
    end
    local drawW = texW * scale
    local drawH = texH * scale
    local dx = (self.width - drawW) / 2
    local dy = (self.height - drawH) / 2
    self:drawTextureScaled(tex, dx, dy, drawW, drawH, 0.85, 0.85, 0.85, 0.85)
end

return NMHeadphoneSlotTextures
