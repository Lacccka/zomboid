require "ISUI/ISPanel"

NPC_InfoUI = ISPanel:derive("NPC_InfoUI")

local COLOR_BOX_OUTLINE = {r=1.0, g=0.9, b=0.4, a=0.8}
local COLOR_OPTION_SELECTED = {r=1.0, g=0.9, b=0.4, a=1.0}
local COLOR_OPTION_UNSELECTED = {r=0.6, g=0.6, b=0.6, a=1.0}
local COLOR_HEADER = {r=1.0, g=0.59, b=0.2, a=1.0}
local COLOR_BODY = {r=1.0, g=0.78, b=0.39, a=1.0}

function NPC_InfoUI:new(player, session, npc, infoNode)
    local WINDOW_WIDTH = 600
    local WINDOW_HEIGHT = 300

    local screenWidth = getCore():getScreenWidth()
    local screenHeight = getCore():getScreenHeight()

    local x = (screenWidth - WINDOW_WIDTH) / 2
    local y = (screenHeight - WINDOW_HEIGHT) / 2

    local o = ISPanel:new(x, y, WINDOW_WIDTH, WINDOW_HEIGHT)
    setmetatable(o, self)
    self.__index = self

    o.player = player
    o.session = session
    o.npc = npc
    o.infoNode = infoNode

    o.infoTexture = getTexture("media/ui/maindialogueui.png")

    o.headerText = infoNode.headerText or "Information"
    o.bodyText = infoNode.bodyText or infoNode.npcText or ""

    o.okBox = {
        x = 50,
        y = WINDOW_HEIGHT - 80,
        width = WINDOW_WIDTH - 100,
        height = 50
    }

    o.isOKHovered = false

    o:setVisible(true)
    o:setAlwaysOnTop(true)

    return o
end

function NPC_InfoUI:initialise()
    ISPanel.initialise(self)
end

function NPC_InfoUI:render()
    if self.infoTexture then
        self:drawTextureScaled(self.infoTexture, 0, 0, self.width, self.height, 1.0, 1.0, 1.0, 1.0)
    end

    local headerX = self.width / 2
    local headerY = 20
    self:drawTextCentre(
        self.headerText,
        headerX,
        headerY,
        COLOR_HEADER.r,
        COLOR_HEADER.g,
        COLOR_HEADER.b,
        COLOR_HEADER.a,
        UIFont.Massive
    )

    local bodyX = 30
    local bodyY = 90
    local maxWidth = self.width - 60

    local wrappedLines = self:wrapText(self.bodyText, maxWidth, UIFont.Large)
    for i, line in ipairs(wrappedLines) do
        local lineY = bodyY + ((i - 1) * 22)
        self:drawText(
            line,
            bodyX,
            lineY,
            COLOR_BODY.r,
            COLOR_BODY.g,
            COLOR_BODY.b,
            COLOR_BODY.a,
            UIFont.Large
        )
    end

    local isHovered = self.isOKHovered

    if isHovered then
        self:drawRectBorder(
            self.okBox.x,
            self.okBox.y,
            self.okBox.width,
            self.okBox.height,
            COLOR_BOX_OUTLINE.a,
            COLOR_BOX_OUTLINE.r,
            COLOR_BOX_OUTLINE.g,
            COLOR_BOX_OUTLINE.b
        )
    end

    local okTextColor = isHovered and COLOR_OPTION_SELECTED or COLOR_OPTION_UNSELECTED
    local okTextX = self.okBox.x + (self.okBox.width / 2)
    local okTextY = self.okBox.y + 15

    self:drawTextCentre(
        "OK",
        okTextX,
        okTextY,
        okTextColor.r,
        okTextColor.g,
        okTextColor.b,
        okTextColor.a,
        UIFont.Large
    )
end

function NPC_InfoUI:wrapText(text, maxWidth, font)
    local words = {}
    for word in text:gmatch("%S+") do
        table.insert(words, word)
    end

    local lines = {}
    local currentLine = ""

    for i, word in ipairs(words) do
        local testLine = currentLine == "" and word or (currentLine .. " " .. word)
        local lineWidth = getTextManager():MeasureStringX(font, testLine)

        if lineWidth <= maxWidth then
            currentLine = testLine
        else
            if currentLine ~= "" then
                table.insert(lines, currentLine)
            end
            currentLine = word
        end
    end

    if currentLine ~= "" then
        table.insert(lines, currentLine)
    end

    return lines
end

function NPC_InfoUI:onMouseMove(dx, dy)
    if not self:getIsVisible() then
        self.isOKHovered = false
        return
    end

    local mouseX = self:getMouseX()
    local mouseY = self:getMouseY()

    if not mouseX or not mouseY then
        self.isOKHovered = false
        return
    end

    if mouseX >= self.okBox.x and mouseX <= (self.okBox.x + self.okBox.width) and
       mouseY >= self.okBox.y and mouseY <= (self.okBox.y + self.okBox.height) then
        self.isOKHovered = true
    else
        self.isOKHovered = false
    end
end

function NPC_InfoUI:onMouseDown(x, y)
    if not self:getIsVisible() then
        return false
    end

    local mouseX = self:getMouseX()
    local mouseY = self:getMouseY()

    if not mouseX or not mouseY then
        return false
    end

    if mouseX >= self.okBox.x and mouseX <= (self.okBox.x + self.okBox.width) and
       mouseY >= self.okBox.y and mouseY <= (self.okBox.y + self.okBox.height) then
        self:close()

        local NPC_DialogueEngine = require("DialogueFramework/Dialogue/NPC_DialogueEngine")
        NPC_DialogueEngine.endSession(self.player)

        return true
    end

    return false
end

function NPC_InfoUI:close()
    local NPC_InteractionCoordinator = require("DialogueFramework/Interaction/NPC_InteractionCoordinator")

    NPC_InteractionCoordinator.setInteractionActive(false)

    self:setVisible(false)
    self:removeFromUIManager()
end

return NPC_InfoUI
