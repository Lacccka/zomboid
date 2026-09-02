require "PPO_Num"
require "PPO_MoodlePresenter"
require "PPO_ClientRuntime"

PPO = PPO or {}
PPO.MoodleHUD = PPO.MoodleHUD or { Instances = {} }

local MoodleHUD = PPO.MoodleHUD

local STANDARD_SIZES = { 32, 48, 64, 80, 96, 128 }
local VANILLA_MOODLES = {
    "ENDURANCE", "ANGRY", "TIRED", "HUNGRY", "PANIC", "SICK",
    "BORED", "UNHAPPY", "STRESS", "THIRST", "PAIN", "WET",
    "HAS_A_COLD", "INJURED", "DRUNK", "UNCOMFORTABLE",
    "NOXIOUS_SMELL", "HYPOTHERMIA", "HYPERTHERMIA", "WINDCHILL",
    "HEAVY_LOAD", "FOOD_EATEN",
}

local Num = PPO.Num

local function guardedNumber(call, fallback)
    local ok, value = pcall(call)
    if not ok then return fallback end
    return Num.finite(value, fallback)
end

local function viewport(playerNum)
    local left = guardedNumber(function()
        return getPlayerScreenLeft(playerNum)
    end, 0)
    local top = guardedNumber(function()
        return getPlayerScreenTop(playerNum)
    end, 0)
    local width = math.max(0, guardedNumber(function()
        return getPlayerScreenWidth(playerNum)
    end, 0))
    local height = math.max(0, guardedNumber(function()
        return getPlayerScreenHeight(playerNum)
    end, 0))
    return { left = left, top = top, width = width, height = height }
end

function MoodleHUD.textureMetrics(option, fontSizeOption)
    local selected = Num.finite(option, 0)
    if selected >= 1 and selected <= #STANDARD_SIZES
            and selected == math.floor(selected) then
        local size = STANDARD_SIZES[selected]
        return {
            sourceSize = size,
            size = size,
            gap = 10,
            step = size + 10,
        }
    end
    if selected == 7 then
        local fontOption = Num.finite(fontSizeOption, 0)
        if fontOption >= 1 and fontOption <= #STANDARD_SIZES
                and fontOption == math.floor(fontOption) then
            local size = STANDARD_SIZES[fontOption]
            return { sourceSize = size, size = size, gap = 10,
                step = size + 10 }
        end
    end
    return { sourceSize = 32, size = 32, gap = 10, step = 42 }
end

function MoodleHUD.visibleVanillaCount(character)
    if character == nil or type(character.getMoodles) ~= "function" then
        return 0
    end
    local ok, moodles = pcall(character.getMoodles, character)
    if not ok or moodles == nil
            or type(moodles.getMoodleLevel) ~= "function" then
        return 0
    end

    local count = 0
    for _, name in ipairs(VANILLA_MOODLES) do
        local moodleType = MoodleType ~= nil and MoodleType[name] or nil
        if moodleType ~= nil then
            local readOK, level = pcall(moodles.getMoodleLevel,
                moodles, moodleType)
            level = readOK and Num.finite(level, 0) or 0
            if level ~= 0
                    and (name ~= "FOOD_EATEN" or level >= 3) then
                count = count + 1
            end
        end
    end
    return count
end

local function activeCustomRecord(record)
    if type(record) ~= "table" then return false end
    if record.active == true or record.visible == true then return true end
    for _, name in ipairs({ "Level", "level", "Value", "value" }) do
        local value = Num.finite(record[name], 0)
        if value ~= 0 then return true end
    end
    return false
end

local function activeCustomCount(records)
    if type(records) ~= "table" then return 0 end
    local count = 0
    for _, record in pairs(records) do
        if activeCustomRecord(record) then count = count + 1 end
    end
    return count
end

function MoodleHUD.visibleCustomCount(character)
    if character == nil or type(character.getModData) ~= "function" then
        return 0
    end
    local ok, count = pcall(function()
        local data = character:getModData()
        if type(data) ~= "table" then return 0 end
        local managerMoodles = nil
        if type(data.MoodleManager) == "table" then
            managerMoodles = data.MoodleManager.moodles
        end
        return activeCustomCount(managerMoodles)
            + activeCustomCount(data.LSMoodles)
            + activeCustomCount(data.Moodles)
    end)
    if not ok then return 0 end
    return count
end

local function currentMetrics()
    local option = guardedNumber(function()
        return getCore():getOptionMoodleSize()
    end, nil)
    local fontSizeOption = guardedNumber(function()
        return getCore():getOptionFontSizeReal()
    end, nil)
    return MoodleHUD.textureMetrics(option, fontSizeOption)
end

local function currentLanguageCode()
    local ok, code = pcall(function()
        local language = Translator.getLanguage()
        return language:name()
    end)
    if ok and type(code) == "string" and code ~= "" then return code end
    return "EN"
end

function MoodleHUD.slotLayout(playerNum, character, records)
    local area = viewport(playerNum)
    local metrics = currentMetrics()
    local priorSlots = MoodleHUD.visibleVanillaCount(character)
        + MoodleHUD.visibleCustomCount(character)
    local x = area.left + area.width - metrics.size - 10
    local recordCount = 0
    if type(records) == "table" then
        for _ in ipairs(records) do recordCount = recordCount + 1 end
    end
    local height = math.max(metrics.size, recordCount * metrics.step)
    local desiredY = area.top + 120 + priorSlots * metrics.step
    local maximumY = area.top + area.height - height
    local y = math.max(area.top, math.min(desiredY, maximumY))
    local rects = {}
    if type(records) == "table" then
        for _, record in ipairs(records) do
            table.insert(rects, {
                x = x,
                y = y + (#rects * metrics.step),
                width = metrics.size,
                height = metrics.size,
                record = record,
            })
        end
    end
    return {
        x = x, y = y, width = metrics.size,
        height = height,
        metrics = metrics, rects = rects, viewport = area,
    }
end

function MoodleHUD.tooltipBounds(playerNum, iconRect, width, height)
    local area = viewport(playerNum)
    local resolvedWidth = math.min(area.width,
        math.max(0, Num.finite(width, 0)))
    local resolvedHeight = math.min(area.height,
        math.max(0, Num.finite(height, 0)))
    local desiredX = Num.finite(iconRect and iconRect.x, area.left)
        - resolvedWidth - 6
    local desiredY = Num.finite(iconRect and iconRect.y, area.top)
    local maximumX = area.left + area.width - resolvedWidth
    local maximumY = area.top + area.height - resolvedHeight
    return {
        x = math.max(area.left, math.min(desiredX, maximumX)),
        y = math.max(area.top, math.min(desiredY, maximumY)),
        width = resolvedWidth,
        height = resolvedHeight,
    }
end

local Container = ISUIElement:derive("PPO_MoodleHUDContainer")

MoodleHUD.MissingTextureLogged = MoodleHUD.MissingTextureLogged or {}

local function texture(path)
    local ok, resolved = pcall(getTexture, path)
    if ok and resolved ~= nil then return resolved end
    if not MoodleHUD.MissingTextureLogged[path] then
        MoodleHUD.MissingTextureLogged[path] = true
        print("PPO moodle texture unavailable: " .. tostring(path))
    end
    return nil
end

local function colourComponents(alignment, severity)
    local grayRed, grayGreen, grayBlue = 0.5, 0.5, 0.5
    if Color ~= nil and Color.gray ~= nil then
        grayRed = guardedNumber(function()
            return Color.gray:getRedFloat()
        end, grayRed)
        grayGreen = guardedNumber(function()
            return Color.gray:getGreenFloat()
        end, grayGreen)
        grayBlue = guardedNumber(function()
            return Color.gray:getBlueFloat()
        end, grayBlue)
    end
    local ok, selected = pcall(function()
        local core = getCore()
        if alignment == "Good" then
            return core:getGoodHighlitedColor()
        end
        return core:getBadHighlitedColor()
    end)
    local red, green, blue = grayRed, grayGreen, grayBlue
    if ok and selected ~= nil then
        red = guardedNumber(function() return selected:getR() end, red)
        green = guardedNumber(function() return selected:getG() end, green)
        blue = guardedNumber(function() return selected:getB() end, blue)
    end
    local share = math.max(0, math.min(1,
        Num.finite(severity, 1) / 3))
    local inverse = 1 - share
    return grayRed * inverse + red * share,
        grayGreen * inverse + green * share,
        grayBlue * inverse + blue * share
end

local function textWidth(value)
    return guardedNumber(function()
        return getTextManager():MeasureStringX(UIFont.Small, value)
    end, string.len(tostring(value)) * 7)
end

local function wrappedLines(value, maximumWidth)
    local text = tostring(value or "")
    if textWidth(text) <= maximumWidth then return { text } end
    local lines = {}
    local current = ""
    for word in string.gmatch(text, "%S+") do
        local candidate = current == "" and word or current .. " " .. word
        if current ~= "" and textWidth(candidate) > maximumWidth then
            table.insert(lines, current)
            current = word
        else
            current = candidate
        end
    end
    if current ~= "" then table.insert(lines, current) end
    if #lines == 0 then table.insert(lines, text) end
    return lines
end

local function hovered(rect)
    local x = guardedNumber(getMouseX, -1)
    local y = guardedNumber(getMouseY, -1)
    return x >= rect.x and x <= rect.x + rect.width
        and y >= rect.y and y <= rect.y + rect.height
end

function Container:new(playerNum, character)
    local instance = ISUIElement.new(self, 0, 0, 0, 0)
    instance.playerNum = playerNum
    instance.character = character
    instance.records = {}
    instance.rects = {}
    instance.severityByID = {}
    instance.wiggleFrames = 0
    instance.lastWiggleOffset = 0
    return instance
end

function Container:instantiate()
    ISUIElement.instantiate(self)
    self.javaObject:setConsumeMouseEvents(false)
end

function Container:updateLayout()
    local layout = MoodleHUD.slotLayout(self.playerNum, self.character,
        self.records)
    self.rects = layout.rects
    self:setX(layout.x)
    self:setY(layout.y)
    self:setWidth(layout.width)
    self:setHeight(layout.height)
end

function Container:refresh(payload)
    local records = PPO.MoodlePresenter.records(payload, currentLanguageCode())
    local changed = false
    local nextSeverity = {}
    for _, record in ipairs(records) do
        nextSeverity[record.id] = record.severity
        local previous = self.severityByID[record.id]
        if previous ~= nil and previous ~= record.severity then
            changed = true
        end
    end
    self.records = records
    self.severityByID = nextSeverity
    if changed then self.wiggleFrames = 3 end
    self:updateLayout()
end

function Container:drawTooltip(rect, record)
    local title = getText(record.titleKey)
    local description = getText(record.descriptionKey)
    local lines = { title }
    for _, line in ipairs(wrappedLines(description, 300)) do
        table.insert(lines, line)
    end
    if record.timeKey ~= nil then
        local time = nil
        if record.timeValue ~= nil then
            time = getText(record.timeKey, record.timeValue)
        else
            time = getText(record.timeKey)
        end
        for _, line in ipairs(wrappedLines(time, 300)) do
            table.insert(lines, line)
        end
    end

    local lineHeight = guardedNumber(function()
        return getTextManager():getFontHeight(UIFont.Small)
    end, 16)
    local width = 16
    for _, line in ipairs(lines) do
        width = math.max(width, textWidth(line) + 16)
    end
    width = math.min(320, width)
    local height = #lines * lineHeight + 12
    local bounds = MoodleHUD.tooltipBounds(self.playerNum, rect, width, height)
    local localX = bounds.x - self:getX()
    local localY = bounds.y - self:getY()
    self:drawRect(localX, localY, bounds.width, bounds.height,
        0.75, 0, 0, 0)
    for index, line in ipairs(lines) do
        local alpha = index == 1 and 1 or 0.85
        self:drawText(line, localX + 8,
            localY + 5 + (index - 1) * lineHeight,
            1, 1, 1, alpha, UIFont.Small)
    end
end

function Container:render()
    self:updateLayout()
    local wiggle = 0
    if self.wiggleFrames > 0 then
        if self.wiggleFrames == 3 then wiggle = 2 end
        if self.wiggleFrames == 2 then wiggle = -2 end
        self.wiggleFrames = self.wiggleFrames - 1
    end
    self.lastWiggleOffset = wiggle

    local hoveredRect = nil
    -- Read once per frame rather than once per icon: the moodle size is a
    -- whole-HUD option and every record resolves it to the same answer.
    local sourceSize = currentMetrics().sourceSize
    local base = "media/ui/Moodles/" .. tostring(sourceSize) .. "/"
    for _, rect in ipairs(self.rects) do
        local record = rect.record
        local size = rect.width
        local icon = texture(base .. tostring(record.icon))
        if icon ~= nil then
            local solid = texture(base .. "_Moodles_BGsolid.png")
            local outline = texture(base .. "_Moodles_BGoutline.png")
            if solid ~= nil and outline ~= nil then
                local red, green, blue = colourComponents(
                    record.alignment, record.severity)
                local x = rect.x - self:getX() + wiggle
                local y = rect.y - self:getY()
                self:drawTextureScaled(solid, x, y, size, size,
                    1, red, green, blue)
                self:drawTextureScaled(outline, x, y, size, size, 1)
                self:drawTextureScaled(icon, x, y, size, size, 1, 1, 1, 1)
                if hovered(rect) then
                    hoveredRect = rect
                end
            end
        end
    end
    if hoveredRect ~= nil then
        self:drawTooltip(hoveredRect, hoveredRect.record)
    end
end

function MoodleHUD.create(playerNum, character)
    local existing = MoodleHUD.Instances[playerNum]
    if existing ~= nil and existing.character == character then
        return existing
    end
    if existing ~= nil then
        MoodleHUD.remove(playerNum)
    end
    local instance = Container:new(playerNum, character)
    instance:initialise()
    instance:addToUIManager()
    MoodleHUD.Instances[playerNum] = instance
    local state = PPO.ClientRuntime.state(character)
    if state ~= nil then instance:refresh(state) end
    return instance
end

function MoodleHUD.remove(playerNum)
    local instance = MoodleHUD.Instances[playerNum]
    if instance == nil then return false end
    instance.records = {}
    instance.rects = {}
    instance:removeFromUIManager()
    MoodleHUD.Instances[playerNum] = nil
    return true
end

function MoodleHUD.refresh(character, payload)
    for _, instance in pairs(MoodleHUD.Instances) do
        if instance.character == character then
            instance:refresh(payload)
            return true
        end
    end
    return false
end

MoodleHUD.Container = Container

local function onCreatePlayer(playerNum, character)
    MoodleHUD.create(playerNum, character)
end

local function onPlayerDeath(character)
    local playerNum = nil
    for index, instance in pairs(MoodleHUD.Instances) do
        if instance.character == character then playerNum = index break end
    end
    PPO.ClientRuntime.reset(character)
    if playerNum ~= nil then MoodleHUD.remove(playerNum) end
end

if not MoodleHUD.LifecycleInstalled and Events ~= nil then
    if Events.OnCreatePlayer ~= nil then
        Events.OnCreatePlayer.Add(onCreatePlayer)
    end
    if Events.OnPlayerDeath ~= nil then
        Events.OnPlayerDeath.Add(onPlayerDeath)
    end
    MoodleHUD.LifecycleInstalled = true
end

PPO.ClientRuntime.onStateChanged = MoodleHUD.refresh
