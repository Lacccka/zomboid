require "ISUI/ISPanelJoypad"
require "PPO_TrainingPanelMath"

PPO = PPO or {}

local FONT_HGT_SMALL = getTextManager():getFontHeight(UIFont.Small)
local FONT_HGT_MEDIUM = getTextManager():getFontHeight(UIFont.Medium)
local UI_BORDER_SPACING = 10
local ROW_HEIGHT = FONT_HGT_SMALL + 6
-- Headers carry their own height because they carry their own font: reusing
-- ROW_HEIGHT for a Medium line clips its descenders.
local HEADER_HEIGHT = FONT_HGT_MEDIUM + 6
local BAR_HEIGHT = 10
local STACK_HEIGHT = 16
-- The gap between the widest label in a block and the bars that follow it.
local LABEL_GAP = 8
-- A bar narrower than this stops being readable as a proportion, so it sets the
-- floor: label plus bar per column, two columns, and the gaps around them.
local MIN_BAR_WIDTH = 120
local NOMINAL_LABEL_WIDTH = 110
local MIN_WIDTH = (NOMINAL_LABEL_WIDTH + MIN_BAR_WIDTH) * 2 + UI_BORDER_SPACING * 3

-- Named rather than numbered so the model and the palette cannot drift apart.
-- These are bar colors only. A label used to be tinted to match its bar, which
-- put six hues in a column of six lines and left the reader with no plain text
-- to rest on; the tint says nothing the bar beside it does not already say.
-- The two windows carry hues of their own rather than borrowing `fuel` and
-- `debt`, which is what they used to do: `fuel` is the food-and-water share
-- drawn a few rows below, and red is the state a stopped course leaves behind.
-- A hue that already means something else in this panel means that thing here
-- too.
local COLORS = {
    form = { r = 0.36, g = 0.68, b = 0.40 },
    protein = { r = 0.32, g = 0.55, b = 0.85 },
    creatine = { r = 0.45, g = 0.72, b = 0.90 },
    sleep = { r = 0.60, g = 0.47, b = 0.82 },
    fuel = { r = 0.88, g = 0.68, b = 0.32 },
    debt = { r = 0.82, g = 0.32, b = 0.32 },
    stimulant = { r = 0.86, g = 0.42, b = 0.74 },
    thermogenic = { r = 0.24, g = 0.76, b = 0.72 },
}
-- Three text weights and no more: white Medium opens a section, white Small
-- opens a block inside one, grey Small is every value the player reads off a
-- bar.
local HEADER_TEXT = { r = 1, g = 1, b = 1 }
local BLOCK_TEXT = { r = 1, g = 1, b = 1 }
local LABEL_TEXT = { r = 0.82, g = 0.82, b = 0.82 }
local CAPTION_TEXT = { r = 0.7, g = 0.7, b = 0.7 }
-- "Course" named the mechanic, not the thing in the player's hands, and the two
-- directions do not even hold the same thing: `PPO_ConsumeAuthority.lua:9` feeds
-- Strength from anabolic credit and Fitness from cardio credit, with no path
-- between them. One shared label would be wrong under one of the two columns.
local COURSE_KEY = {
    Strength = "IGUI_PPO_PanelAnabolics",
    Fitness = "IGUI_PPO_PanelCardio",
}
local TRACK = { r = 0.16, g = 0.16, b = 0.16 }
local DIM_ALPHA = 0.25
-- Every bar is framed in the same faint grey. Without it a fill sits directly
-- on the window background and its right edge is the only thing telling the
-- player where the bar ends, which reads as a shorter bar rather than an
-- emptier one.
local EDGE = { r = 0.5, g = 0.5, b = 0.5, a = 0.35 }
-- The rule that closes a group. Fainter than EDGE, because it separates blocks
-- rather than bounding a value, and a divider that competes with the bars is a
-- divider that has to be read.
local RULE = { r = 0.45, g = 0.45, b = 0.45, a = 0.35 }

PPO.TrainingPanel = ISPanelJoypad:derive("PPO_TrainingPanel")
local TrainingPanel = PPO.TrainingPanel

local function percent(value)
    return tostring(math.floor(value * 100 + 0.5)) .. "%"
end

local function color(name)
    return COLORS[name] or COLORS.form
end

local function shareCaption(row)
    return getText(row.key, percent(row.value))
end

-- A window is a duration, so the number beside it is time left rather than a
-- percentage.
local function windowCaption(row)
    local remaining = getText("IGUI_PPO_WindowNone")
    if row.timeKind == "Hours" then
        remaining = getText("IGUI_PPO_WindowHours", tostring(row.hoursRemaining))
    elseif row.timeKind == "UnderHour" then
        remaining = getText("IGUI_PPO_WindowUnderHour")
    end
    return getText(row.key, remaining)
end

-- Pairs each row with the string that will actually be drawn, because the label
-- column is measured off those strings and a column measured off anything else
-- is a column the longest label runs out of.
local function labelled(rows, caption)
    local out = {}
    for _, row in ipairs(rows) do
        table.insert(out, { row = row, caption = caption(row) })
    end
    return out
end

-- Measured, never assumed. `LABEL_WIDTH = 110` was assumed, and
-- "термогеник: не принят" is wider than that in Russian, so the label ran under
-- its own bar; with eleven languages shipped, no fixed number is the longest
-- string in all of them. The bar keeps MIN_BAR_WIDTH, so a label that cannot fit
-- is the one thing that gives way.
local function labelColumn(entries, width)
    local widest = 0
    for _, entry in ipairs(entries) do
        local measured = getTextManager():MeasureStringX(UIFont.Small,
            entry.caption)
        if type(measured) == "number" and measured > widest then
            widest = measured
        end
    end
    local column = widest + LABEL_GAP
    local ceiling = width - MIN_BAR_WIDTH
    if column > ceiling then column = ceiling end
    if column < 0 then column = 0 end
    return column
end

-- Always drawn after the fill it frames, never before: a border laid down first
-- is painted over by the very bar it was meant to outline.
function TrainingPanel:edge(x, y, width, height)
    self:drawRectBorder(x, y, width, height, EDGE.a, EDGE.r, EDGE.g, EDGE.b)
end

function TrainingPanel:rule(x, y, width)
    self:drawRect(x, y, width, 1, RULE.a, RULE.r, RULE.g, RULE.b)
end

function TrainingPanel:drawTrack(x, y, width)
    self:drawRect(x, y, width, BAR_HEIGHT, 1, TRACK.r, TRACK.g, TRACK.b)
end

function TrainingPanel:drawHeader(x, y, text)
    self:drawText(text, x, y, HEADER_TEXT.r, HEADER_TEXT.g, HEADER_TEXT.b, 1,
        UIFont.Medium)
    return y + HEADER_HEIGHT
end

-- One labelled row: name on the left, bar in the middle, the number folded into
-- the name on the left. Shares and windows differ only in that number, so they
-- differ only in which caption function built the string.
function TrainingPanel:drawRow(x, y, width, entry, labelWidth)
    local row = entry.row
    local c = color(row.color)
    self:drawText(entry.caption, x, y, LABEL_TEXT.r, LABEL_TEXT.g, LABEL_TEXT.b,
        1, UIFont.Small)
    local barX = x + labelWidth
    local barWidth = width - labelWidth
    local barY = y + (ROW_HEIGHT - BAR_HEIGHT) / 2
    self:drawTrack(barX, barY, barWidth)
    self:drawRect(barX, barY, barWidth * row.value, BAR_HEIGHT, 1,
        c.r, c.g, c.b)
    self:edge(barX, barY, barWidth, BAR_HEIGHT)
    return y + ROW_HEIGHT
end

function TrainingPanel:drawRows(x, y, width, entries)
    local labelWidth = labelColumn(entries, width)
    for _, entry in ipairs(entries) do
        y = self:drawRow(x, y, width, entry, labelWidth)
    end
    return y
end

-- Each segment lights inside its own slot, so the dark remainder of a slot is
-- exactly the headroom that share still has.
function TrainingPanel:drawStack(x, y, width, direction)
    self:drawText(getText("IGUI_PPO_PanelFill", percent(direction.fill)),
        x, y, LABEL_TEXT.r, LABEL_TEXT.g, LABEL_TEXT.b, 1, UIFont.Small)
    local barY = y + ROW_HEIGHT
    local offset = 0
    for _, segment in ipairs(direction.segments) do
        local c = color(segment.color)
        local slot = width * segment.width
        self:drawRect(x + offset, barY, slot, STACK_HEIGHT, DIM_ALPHA,
            c.r, c.g, c.b)
        self:drawRect(x + offset, barY, slot * segment.filled, STACK_HEIGHT, 1,
            c.r, c.g, c.b)
        -- Per slot rather than around the stack alone: the boundary between two
        -- shares is exactly where a player needs to see one share end.
        self:edge(x + offset, barY, slot, STACK_HEIGHT)
        offset = offset + slot
    end
    self:drawRectBorder(x, barY, width, STACK_HEIGHT, 0.5, 0.5, 0.5, 0.5)
    return barY + STACK_HEIGHT + UI_BORDER_SPACING
end

-- Ghost is what was swallowed, solid is what is felt, red is the debt a
-- stopped course left behind.
function TrainingPanel:drawCourse(x, y, width, course, key)
    self:drawText(getText(key or COURSE_KEY.Strength), x, y, BLOCK_TEXT.r,
        BLOCK_TEXT.g, BLOCK_TEXT.b, 1, UIFont.Small)
    local barY = y + ROW_HEIGHT
    self:drawTrack(x, barY, width)
    local c = color("form")
    self:drawRect(x, barY, width * course.level, BAR_HEIGHT, DIM_ALPHA,
        c.r, c.g, c.b)
    self:drawRect(x, barY, width * course.active, BAR_HEIGHT, 1, c.r, c.g, c.b)
    local caption = getText("IGUI_PPO_PanelCourseTaken", percent(course.level))
        .. "  " .. getText("IGUI_PPO_PanelCourseActive",
            percent(course.active))
    local captionR, captionG, captionB =
        CAPTION_TEXT.r, CAPTION_TEXT.g, CAPTION_TEXT.b
    -- The debt replaces the caption rather than crowding it: a stopped course
    -- has nothing left to report about what is taken or in effect. This is the
    -- one coloured string left in the panel, because red here is a state and
    -- not a category tint.
    if course.withdrawal > 0 then
        local debt = COLORS.debt
        local debtWidth = width * course.withdrawal
        self:drawRect(x + width - debtWidth, barY, debtWidth, BAR_HEIGHT, 1,
            debt.r, debt.g, debt.b)
        caption = getText("IGUI_PPO_Withdrawal", percent(course.withdrawal))
        captionR, captionG, captionB = debt.r, debt.g, debt.b
    end
    self:edge(x, barY, width, BAR_HEIGHT)
    self:drawText(caption, x, barY + BAR_HEIGHT + 2, captionR, captionG,
        captionB, 1, UIFont.Small)
    return barY + BAR_HEIGHT + ROW_HEIGHT
end

-- Vanilla already owns a localized name for every perk, so the panel borrows it
-- instead of shipping two more strings in eleven files.
local function perkName(name)
    local ok, resolved = pcall(function()
        return PerkFactory.getPerk(Perks[name]):getName()
    end)
    if ok and type(resolved) == "string" and resolved ~= "" then
        return resolved
    end
    return name
end

function TrainingPanel:drawDirection(x, y, width, direction)
    local title = perkName(direction.name)
    if direction.hasMultiplier then
        title = title .. "   x" .. string.format("%.2f", direction.multiplier)
            .. " " .. getText("IGUI_PPO_PanelCeiling",
                string.format("%.2f", direction.ceiling))
    end
    y = self:drawHeader(x, y, title)

    y = self:drawStack(x, y, width, direction)
    y = self:drawRows(x, y, width, labelled(direction.rows, shareCaption))

    self:rule(x, y + UI_BORDER_SPACING / 2, width)
    return self:drawCourse(x, y + UI_BORDER_SPACING, width,
        direction.course, COURSE_KEY[direction.name])
end

-- The character window has no idea how tall any of its tabs is. Every vanilla
-- view answers that question from its own render and walks the answer up to the
-- window (ISCharacterProtection.lua:117, ISCharacterScreen.lua:108); a view that
-- stays silent simply inherits whatever size the previously open tab left, which
-- is what this panel used to do.
function TrainingPanel:fitWindow(height)
    -- Width only ever holds its ground: the layout is fluid and reads
    -- `self.width`, so the floor is what the two columns need to stay legible,
    -- never a number measured off the content.
    self:setWidthAndParentWidth(math.max(MIN_WIDTH, self.width))
    self:setHeightAndParentHeight(height)
end

function TrainingPanel:render()
    ISPanelJoypad.render(self)

    local player = getSpecificPlayer(self.playerNum)
    local model = nil
    if player ~= nil then
        model = PPO.TrainingPanelMath.model(PPO.ClientRuntime.state(player))
    end
    if model == nil then
        self:drawText(getText("IGUI_PPO_PanelNoData"), UI_BORDER_SPACING,
            UI_BORDER_SPACING, CAPTION_TEXT.r, CAPTION_TEXT.g, CAPTION_TEXT.b,
            1, UIFont.Small)
        self:fitWindow(UI_BORDER_SPACING * 2 + ROW_HEIGHT)
        return
    end

    local x = UI_BORDER_SPACING
    local y = UI_BORDER_SPACING
    local full = self.width - UI_BORDER_SPACING * 2

    -- The windows lead, because what is in effect right now outranks the level
    -- a reservoir happens to sit at. They are a section of their own and not
    -- the head of the nutrition list: a window bar is time left and reads in
    -- hours, a share bar is a level and reads in percent. Drawn as one block
    -- they also shared one measured label column, so the wider of the two set
    -- the indent for the other.
    y = self:drawHeader(x, y, getText("IGUI_PPO_PanelEffects"))
    y = self:drawRows(x, y, full, labelled(model.windows, windowCaption))

    self:rule(x, y + UI_BORDER_SPACING / 2, full)
    y = y + UI_BORDER_SPACING

    y = self:drawHeader(x, y, getText("IGUI_PPO_PanelNutrition"))
    y = self:drawRows(x, y, full, labelled(model.shared, shareCaption))

    self:rule(x, y + UI_BORDER_SPACING / 2, full)
    y = y + UI_BORDER_SPACING
    local columnWidth = (full - UI_BORDER_SPACING) / 2
    local columnY = y
    local bottom = y
    for index, direction in ipairs(model.directions) do
        local columnX = x + (index - 1) * (columnWidth + UI_BORDER_SPACING)
        local reached = self:drawDirection(columnX, columnY, columnWidth,
            direction)
        if reached > bottom then bottom = reached end
    end
    self:fitWindow(bottom + UI_BORDER_SPACING)
end

function TrainingPanel:new(x, y, width, height, playerNum)
    local o = ISPanelJoypad:new(x, y, width, height)
    setmetatable(o, self)
    self.__index = self
    o.playerNum = playerNum
    o.backgroundColor = { r = 0, g = 0, b = 0, a = 0.0 }
    o.borderColor = { r = 0, g = 0, b = 0, a = 0.0 }
    return o
end

return TrainingPanel
