-- QP Survivor Contracts
-- Shared flexible layout helpers for translated/resizable UIs.

QPSC_FlexibleLayout = QPSC_FlexibleLayout or {}

local FL = QPSC_FlexibleLayout

function FL.clamp(value, minimum, maximum)
    local number = tonumber(value) or 0
    local low = tonumber(minimum) or number
    local high = tonumber(maximum) or number

    if number < low then return low end
    if number > high then return high end
    return number
end

function FL.measure(text, font)
    local value = tostring(text or "")
    local selectedFont = font or UIFont.Small

    if getTextManager ~= nil then
        local ok, width = pcall(function()
            return getTextManager():MeasureStringX(
                selectedFont,
                value
            )
        end)

        if ok and width ~= nil then
            return math.max(0, tonumber(width) or 0)
        end
    end

    return string.len(value) * 8
end

local function utf8SafePrefix(value, length)
    local text = tostring(value or "")
    local limit = math.max(0, math.min(#text, tonumber(length) or 0))

    while limit > 0 do
        local byte = string.byte(text, limit)

        if byte == nil or byte < 128 or byte >= 192 then
            break
        end

        limit = limit - 1
    end

    return string.sub(text, 1, limit)
end

function FL.ellipsize(text, font, maximumWidth)
    local value = tostring(text or "")
    local width = math.max(0, tonumber(maximumWidth) or 0)

    if width <= 0 or FL.measure(value, font) <= width then
        return value
    end

    local suffix = "..."
    local low = 0
    local high = #value
    local best = ""

    while low <= high do
        local middle = math.floor((low + high) / 2)
        local candidate = utf8SafePrefix(value, middle) .. suffix

        if FL.measure(candidate, font) <= width then
            best = candidate
            low = middle + 1
        else
            high = middle - 1
        end
    end

    return best ~= "" and best or suffix
end

function FL.columns(left, right, weights, gap, minimumWidths)
    local x1 = tonumber(left) or 0
    local x2 = math.max(x1, tonumber(right) or x1)
    local spacing = math.max(0, tonumber(gap) or 0)
    local count = #weights
    local available = math.max(0, x2 - x1 - spacing * math.max(0, count - 1))
    local totalWeight = 0

    for _, weight in ipairs(weights) do
        totalWeight = totalWeight + math.max(0.01, tonumber(weight) or 1)
    end

    local columns = {}
    local currentX = x1
    local used = 0

    for index, weight in ipairs(weights) do
        local width

        if index == count then
            width = math.max(1, available - used)
        else
            width = math.floor(
                available * math.max(0.01, tonumber(weight) or 1)
                    / totalWeight
            )
        end

        local minimum = minimumWidths and tonumber(minimumWidths[index]) or 1
        width = math.max(minimum or 1, width)

        columns[index] = {x=currentX, width=width}
        currentX = currentX + width + spacing
        used = used + width
    end

    local overflow = currentX - spacing - x2

    if overflow > 0 then
        for index = count, 1, -1 do
            local minimum = minimumWidths and tonumber(minimumWidths[index]) or 1
            local reducible = math.max(0, columns[index].width - (minimum or 1))
            local reduction = math.min(reducible, overflow)
            columns[index].width = columns[index].width - reduction
            overflow = overflow - reduction

            if overflow <= 0 then break end
        end

        currentX = x1
        for index = 1, count do
            columns[index].x = currentX
            currentX = currentX + columns[index].width + spacing
        end
    end

    return columns
end

function FL.buttonWidth(button, minimum, maximum, padding)
    local title = button and button.title or ""
    local desired = FL.measure(title, UIFont.Small) + (tonumber(padding) or 28)
    return FL.clamp(desired, minimum or 90, maximum or desired)
end

function FL.planButtons(buttons, availableWidth, gap, minimumWidth, maximumWidth)
    local spacing = math.max(0, tonumber(gap) or 0)
    local available = math.max(1, tonumber(availableWidth) or 1)
    local minimum = math.max(40, tonumber(minimumWidth) or 90)
    local maximum = math.max(minimum, tonumber(maximumWidth) or available)
    local rows = {}
    local row = {items={}, width=0}

    for _, button in ipairs(buttons or {}) do
        if button ~= nil then
            local desired = FL.buttonWidth(button, minimum, maximum, 28)
            local added = desired

            if #row.items > 0 then
                added = added + spacing
            end

            if #row.items > 0 and row.width + added > available then
                rows[#rows + 1] = row
                row = {items={}, width=0}
                added = desired
            end

            row.items[#row.items + 1] = {
                button=button,
                width=math.min(desired, available)
            }
            row.width = row.width + added
        end
    end

    if #row.items > 0 then
        rows[#rows + 1] = row
    end

    if #rows == 0 then
        rows[1] = {items={}, width=0}
    end

    return {
        rows=rows,
        rowHeight=28,
        gap=spacing,
        height=(#rows * 28) + (math.max(0, #rows - 1) * 6)
    }
end

function FL.applyButtonPlan(plan, left, top)
    if plan == nil then return end

    local y = tonumber(top) or 0

    for _, row in ipairs(plan.rows or {}) do
        local x = tonumber(left) or 0

        for _, item in ipairs(row.items or {}) do
            item.button:setX(x)
            item.button:setY(y)
            item.button:setWidth(item.width)
            x = x + item.width + (plan.gap or 0)
        end

        y = y + (plan.rowHeight or 28) + 6
    end
end
