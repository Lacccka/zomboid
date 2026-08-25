require "ISUI/ISPanel"
require "ISUI/ISButton"

if not EFZ then
    EFZ = {}
end

EFZ.PlaysquadPauseWarningPanel = ISPanel:derive("EFZ.PlaysquadPauseWarningPanel")
EFZ.PlaysquadPauseWarningPanel.instance = EFZ.PlaysquadPauseWarningPanel.instance or nil
EFZ.PlaysquadPauseWarningPanel.shown = EFZ.PlaysquadPauseWarningPanel.shown or false

local FONT = UIFont.Medium
local LINE_SPACING = 4
local PADDING_Y = 8
local BUTTON_WIDTH = 120
local BUTTON_HEIGHT = 28
local BUTTON_SPACING = 8
local BG_ALPHA = 0.75
local BORDER_ALPHA = 0.9

local function normalizeLineBreaks(text)
    text = tostring(text or "")
    text = text:gsub("\r\n", "\n")
    text = text:gsub("\r", "\n")
    text = text:gsub("<[Bb][Rr]%s*/?>", "\n")
    text = text:gsub("<LINE>", "\n")
    return text
end

local function splitLines(text)
    local out = {}
    text = normalizeLineBreaks(text)
    for line in (text .. "\n"):gmatch("(.-)\n") do
        if line ~= "" then
            out[#out + 1] = line
        end
    end
    if #out == 0 then
        out[1] = text
    end
    return out
end

local function computeTextHeight(lines)
    local totalHeight = 0
    local textManager = getTextManager()
    local fontHeight = textManager:getFontHeight(FONT)
    for i = 1, #lines do
        totalHeight = totalHeight + fontHeight
        if i < #lines then
            totalHeight = totalHeight + LINE_SPACING
        end
    end
    return totalHeight
end

function EFZ.PlaysquadPauseWarningPanel:refreshLayout()
    local text = getText("IGUI_EFZ_PlaysquadPauseNotice")

    self.lines = splitLines(text)

    local screenLeft = getPlayerScreenLeft(self.playerNum)
    local screenTop = getPlayerScreenTop(self.playerNum)
    local screenWidth = getPlayerScreenWidth(self.playerNum)

    local textHeight = computeTextHeight(self.lines)
    local panelHeight = textHeight + (PADDING_Y * 2) + BUTTON_HEIGHT + BUTTON_SPACING

    self:setX(screenLeft)
    self:setY(screenTop)
    self:setWidth(screenWidth)
    self:setHeight(panelHeight)

    if self.buttonOk then
        local buttonX = (screenWidth - BUTTON_WIDTH) / 2
        local buttonY = PADDING_Y + textHeight + BUTTON_SPACING
        self.buttonOk:setX(buttonX)
        self.buttonOk:setY(buttonY)
        self.buttonOk:setTitle(getText("UI_Ok"))
    end
end

function EFZ.PlaysquadPauseWarningPanel:prerender()
    ISPanel.prerender(self)
    self:refreshLayout()
    self:drawRect(0, 0, self.width, self.height, BG_ALPHA, 0, 0, 0)
    self:drawRectBorder(0, 0, self.width, self.height, BORDER_ALPHA, 1, 1, 1)
end

function EFZ.PlaysquadPauseWarningPanel:render()
    ISPanel.render(self)

    local textManager = getTextManager()
    local fontHeight = textManager:getFontHeight(FONT)
    local y = PADDING_Y

    if self.lines then
        for i = 1, #self.lines do
            self:drawTextCentre(self.lines[i], self.width / 2, y, 1, 1, 1, 1, FONT)
            y = y + fontHeight + LINE_SPACING
        end
    end
end

function EFZ.PlaysquadPauseWarningPanel:closePanel()
    self:setVisible(false)
    self:removeFromUIManager()
    if EFZ.PlaysquadPauseWarningPanel.instance == self then
        EFZ.PlaysquadPauseWarningPanel.instance = nil
    end
end

function EFZ.PlaysquadPauseWarningPanel:onConfirm()
    self:closePanel()
end

function EFZ.PlaysquadPauseWarningPanel:onMouseDown(_, _, _)
    return true
end

function EFZ.PlaysquadPauseWarningPanel:new(playerNum)
    local o = ISPanel:new(0, 0, 100, 10)
    setmetatable(o, self)
    self.__index = self

    o.background = false
    o.borderColor = { r = 1, g = 1, b = 1, a = BORDER_ALPHA }
    o.anchorLeft = true
    o.anchorTop = true
    o.anchorRight = true
    o.anchorBottom = false
    o.playerNum = playerNum or 0

    o:initialise()
    o:setAlwaysOnTop(true)

    local okText = getText("UI_Ok")
    o.buttonOk = ISButton:new(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, okText, o, EFZ.PlaysquadPauseWarningPanel.onConfirm)
    o.buttonOk:initialise()
    o:addChild(o.buttonOk)

    return o
end

function EFZ.PlaysquadPauseWarningPanel.show(playerNum)
    if EFZ.PlaysquadPauseWarningPanel.instance then
        local existing = EFZ.PlaysquadPauseWarningPanel.instance
        existing:setVisible(true)
        existing:addToUIManager()
        existing:bringToTop()
        return
    end

    local panel = EFZ.PlaysquadPauseWarningPanel:new(playerNum or 0)
    panel:addToUIManager()
    EFZ.PlaysquadPauseWarningPanel.instance = panel
end

local function tryShowWarning(playerObj)
    if EFZ.PlaysquadPauseWarningPanel.shown then
        return
    end
    if type(psc_SetForcePaused) ~= "function" then
        return
    end

    local target = playerObj or getPlayer()
    if not target then
        return
    end
    if type(EFZ.IsDeployInProgress) == "function" and EFZ.IsDeployInProgress(target) then
        return
    end

    EFZ.PlaysquadPauseWarningPanel.show(target:getPlayerNum())
    EFZ.PlaysquadPauseWarningPanel.shown = true
end

Events.OnGameStart.Add(tryShowWarning)
Events.OnCreatePlayer.Add(function(_, playerObj)
    if playerObj and playerObj:isLocalPlayer() then
        tryShowWarning(playerObj)
    end
end)
