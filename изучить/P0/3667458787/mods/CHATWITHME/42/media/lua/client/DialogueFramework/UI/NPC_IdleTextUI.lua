require "ISUI/ISPanel"

local NPC_IdleTextUI = ISPanel:derive("NPC_IdleTextUI")

local COLOR_NPC_TEXT = {r = 0.9, g = 0.9, b = 0.9, a = 1.0}
local COLOR_BACKGROUND = {r = 0.0, g = 0.0, b = 0.0, a = 0.8}
local COLOR_BORDER = {r = 0.4, g = 0.4, b = 0.4, a = 1.0}

function NPC_IdleTextUI:new(x, y, width, height, npcText, displayDuration, fadeInDuration, fadeOutDuration)
    local o = ISPanel.new(self, x, y, width, height)

    o.npcText = npcText or ""
    o.displayDuration = displayDuration or 3.0
    o.fadeInDuration = fadeInDuration or 0.3
    o.fadeOutDuration = fadeOutDuration or 0.3

    o.fadeAlpha = 0.0
    o.fadeState = "FADING_IN"
    o.stateStartTime = getTimestampMs() / 1000.0

    o.backgroundColor = COLOR_BACKGROUND
    o.borderColor = COLOR_BORDER

    o:setAlwaysOnTop(true)

    return o
end

function NPC_IdleTextUI:initialise()
    ISPanel.initialise(self)
end

function NPC_IdleTextUI:update()
    ISPanel.update(self)

    local currentTime = getTimestampMs() / 1000.0
    local elapsed = currentTime - self.stateStartTime

    if self.fadeState == "FADING_IN" then
        self.fadeAlpha = math.min(1.0, elapsed / self.fadeInDuration)

        if elapsed >= self.fadeInDuration then
            self.fadeState = "DISPLAYING"
            self.stateStartTime = currentTime
        end

    elseif self.fadeState == "DISPLAYING" then
        self.fadeAlpha = 1.0

        if elapsed >= self.displayDuration then
            self.fadeState = "FADING_OUT"
            self.stateStartTime = currentTime
        end

    elseif self.fadeState == "FADING_OUT" then
        self.fadeAlpha = math.max(0.0, 1.0 - (elapsed / self.fadeOutDuration))

        if elapsed >= self.fadeOutDuration then
            self.fadeState = "COMPLETE"
            self:removeFromUIManager()
        end
    end
end

function NPC_IdleTextUI:render()
    ISPanel.render(self)

    self:drawRect(
        0, 0, self.width, self.height,
        self.backgroundColor.a * self.fadeAlpha,
        self.backgroundColor.r,
        self.backgroundColor.g,
        self.backgroundColor.b
    )

    self:drawRectBorder(
        0, 0, self.width, self.height,
        self.borderColor.a * self.fadeAlpha,
        self.borderColor.r,
        self.borderColor.g,
        self.borderColor.b
    )

    self:renderNPCText()
end

function NPC_IdleTextUI:renderNPCText()
    if not self.npcText or self.npcText == "" then
        return
    end

    local textX = 15
    local textY = 15
    local textWidth = self.width - 30

    local wrappedText = self:wrapText(self.npcText, textWidth, UIFont.Medium)

    for i, line in ipairs(wrappedText) do
        local lineY = textY + ((i - 1) * 16)
        self:drawText(
            line,
            textX,
            lineY,
            COLOR_NPC_TEXT.r,
            COLOR_NPC_TEXT.g,
            COLOR_NPC_TEXT.b,
            COLOR_NPC_TEXT.a * self.fadeAlpha,
            UIFont.Medium
        )
    end
end

function NPC_IdleTextUI:wrapText(text, maxWidth, font)
    local wrappedLines = {}
    local words = {}

    for word in string.gmatch(text, "%S+") do
        table.insert(words, word)
    end

    local currentLine = ""

    for _, word in ipairs(words) do
        local testLine = currentLine == "" and word or (currentLine .. " " .. word)
        local testWidth = getTextManager():MeasureStringX(font, testLine)

        if testWidth <= maxWidth then
            currentLine = testLine
        else
            if currentLine ~= "" then
                table.insert(wrappedLines, currentLine)
            end
            currentLine = word
        end
    end

    if currentLine ~= "" then
        table.insert(wrappedLines, currentLine)
    end

    return wrappedLines
end

return NPC_IdleTextUI
