--[[
    MFS Weapon Native-Part Tooltip
    Release-oriented version.

    Keeps the existing/vanilla weapon tooltip intact.
    While Alt is held on a ranged HandWeapon, appends:
      - Item ID
      - Native Parts count
      - Each installed WeaponPart with display name, part type, and full item ID

    Load this file AFTER other ISToolTipInv.render overrides.
]]

local previous_ISToolTipInv_render = ISToolTipInv.render

-- Visual constants
local CLR_YELLOW = {1.0, 1.0, 0.8}
local CLR_CYAN   = {0.6, 0.85, 1.0}
local CLR_GREY   = {0.72, 0.72, 0.72}
local CLR_DIM    = {0.58, 0.58, 0.58}
local CLR_WHITE  = {1.0, 1.0, 1.0}

local function isAltDown()
    return isKeyDown(Keyboard.KEY_LMENU) or isKeyDown(Keyboard.KEY_RMENU)
end

local function addLabelValueLine(lines, label, value, labelColor, valueColor)
    labelColor = labelColor or CLR_YELLOW
    valueColor = valueColor or CLR_CYAN

    table.insert(lines, {
        type = "labelvalue",
        label = label .. ": ",
        value = tostring(value),
        labelR = labelColor[1],
        labelG = labelColor[2],
        labelB = labelColor[3],
        valueR = valueColor[1],
        valueG = valueColor[2],
        valueB = valueColor[3]
    })
end

local function addSeparatorLine(lines)
    table.insert(lines, { type = "separator" })
end

local function buildNativeWeaponInfo(item)
    local lines = {}

    -- Primary technical information
    addLabelValueLine(lines, "Item ID", item:getFullType(), CLR_YELLOW, CLR_CYAN)

    local parts = nil
    if item.getAllWeaponParts then
        parts = item:getAllWeaponParts()
    end

    local partCount = parts and parts:size() or 0
    addLabelValueLine(lines, "Native Parts", partCount, CLR_YELLOW, CLR_WHITE)

    -- Separate the summary from the installed-part listing.
    if partCount > 0 then
        addSeparatorLine(lines)
    end

    if parts then
        for i = 0, parts:size() - 1 do
            local part = parts:get(i)

            if part then
                local partType = ""
                if part.getPartType then
                    partType = tostring(part:getPartType() or "")
                end

                local fullType = tostring(part:getFullType())
                local displayName = tostring(part:getDisplayName())

                local label = "Part " .. tostring(i + 1)
                local value

                if partType ~= "" then
                    value = displayName .. " [" .. partType .. "] - " .. fullType
                else
                    value = displayName .. " - " .. fullType
                end

                -- Part-number labels are dimmer; the useful technical string remains cyan.
                addLabelValueLine(lines, label, value, CLR_GREY, CLR_CYAN)
            end
        end
    end

    -- Debug helpers retained for future troubleshooting.
    -- print("[MFS Tooltip] Weapon:", item:getFullType())
    -- print("[MFS Tooltip] Native Parts:", partCount)
    -- if parts then
    --     for i = 0, parts:size() - 1 do
    --         local part = parts:get(i)
    --         if part then
    --             print(
    --                 "[MFS Tooltip] Part",
    --                 i + 1,
    --                 part:getDisplayName(),
    --                 part.getPartType and part:getPartType() or "",
    --                 part:getFullType()
    --             )
    --         end
    --     end
    -- end

    return lines
end

local function appendLinesBelowTooltip(self, lines)
    if not lines or #lines == 0 then
        return
    end

    -- Keep the same font as the current tooltip for visual consistency.
    local font = ISToolTip.GetFont()
    local tm = getTextManager()

    local padding = 6
    local gap = 4
    local lineHeight = tm:getFontHeight(font)

    local maxWidth = 0
    local totalHeight = 0

    for _, line in ipairs(lines) do
        if line.type == "labelvalue" then
            local width = tm:MeasureStringX(font, line.label .. line.value)
            if width > maxWidth then
                maxWidth = width
            end
            totalHeight = totalHeight + lineHeight

        elseif line.type == "separator" then
            totalHeight = totalHeight + 5
        end
    end

    local oldWidth = self:getWidth()
    local oldHeight = self:getHeight()

    local extraHeight = totalHeight + (padding * 2)
    local newWidth = math.max(oldWidth, maxWidth + (padding * 2) + 2)
    local newHeight = oldHeight + gap + extraHeight

    self:setWidth(newWidth)
    self:setHeight(newHeight)

    local y = oldHeight + gap

    -- Match vanilla-style tooltip background/border.
    self:drawRect(0, y, newWidth, extraHeight, 0.9, 0, 0, 0)
    self:drawRectBorder(0, y, newWidth, extraHeight, 1, 0.4, 0.4, 0.4)

    local yPos = y + padding

    for _, line in ipairs(lines) do
        if line.type == "labelvalue" then
            local labelWidth = tm:MeasureStringX(font, line.label)

            self:drawText(
                line.label,
                padding,
                yPos,
                line.labelR,
                line.labelG,
                line.labelB,
                1,
                font
            )

            self:drawText(
                line.value,
                padding + labelWidth,
                yPos,
                line.valueR,
                line.valueG,
                line.valueB,
                1,
                font
            )

            yPos = yPos + lineHeight

        elseif line.type == "separator" then
            local lineY = yPos + 2
            self:drawRect(
                padding,
                lineY,
                newWidth - (padding * 2),
                1,
                1,
                CLR_DIM[1],
                CLR_DIM[2],
                CLR_DIM[3]
            )
            yPos = yPos + 5
        end
    end
end

function ISToolTipInv:render()
    -- Preserve whichever tooltip renderer was installed before this file.
    previous_ISToolTipInv_render(self)

    -- Only append the technical section for ranged weapons while Alt is held.
    if self.item
            and instanceof(self.item, "HandWeapon")
            and self.item:isRanged()
            and isAltDown()
            and (not ISContextMenu.instance or not ISContextMenu.instance.visibleCheck) then

        local lines = buildNativeWeaponInfo(self.item)
        appendLinesBelowTooltip(self, lines)
    end
end