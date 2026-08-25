require "ISUI/ISPanel"
require "ISUI/ISButton"
require "ISUI/ISRichTextPanel"
require "LCCQF/LCCQFConstants"

LCCQFDialoguePanel = ISPanel:derive("LCCQFDialoguePanel")
LCCQFDialoguePanel.instance = nil

function LCCQFDialoguePanel:new(npcName, sessionId, onChoice, onClose)
    local width = math.min(700, getCore():getScreenWidth() - 80)
    local height = math.min(430, getCore():getScreenHeight() - 80)
    local x = math.floor((getCore():getScreenWidth() - width) / 2)
    local y = math.floor((getCore():getScreenHeight() - height) / 2)

    local o = ISPanel:new(x, y, width, height)
    setmetatable(o, self)
    self.__index = self

    o.npcName = npcName or "NPC"
    o.sessionId = sessionId
    o.onChoiceCallback = onChoice
    o.onCloseCallback = onClose
    o.moveWithMouse = true
    o.backgroundColor = { r = 0.05, g = 0.05, b = 0.05, a = 0.94 }
    o.borderColor = { r = 0.55, g = 0.55, b = 0.55, a = 1.0 }
    o.choiceButtons = {}
    o.closed = false

    return o
end

function LCCQFDialoguePanel:initialise()
    ISPanel.initialise(self)
end

function LCCQFDialoguePanel:createChildren()
    local margin = 24
    local closeW = 32

    self.closeButton = ISButton:new(self.width - margin - closeW, 14, closeW, 28, "X", self, LCCQFDialoguePanel.onClosePressed)
    self.closeButton:initialise()
    self.closeButton:instantiate()
    self:addChild(self.closeButton)

    self.body = ISRichTextPanel:new(margin, 58, self.width - margin * 2, self.height - 170)
    self.body:initialise()
    self.body:instantiate()
    self.body.autosetheight = false
    self.body.background = false
    self.body.borderColor = { r = 0, g = 0, b = 0, a = 0 }
    self.body.marginLeft = 0
    self.body.marginRight = 0
    self.body.marginTop = 0
    self.body.marginBottom = 0
    self:addChild(self.body)

    local buttonY = self.height - 96
    local buttonGap = 10
    local buttonWidth = math.floor((self.width - margin * 2 - buttonGap * 2) / 3)

    for i = 1, 3 do
        local x = margin + (i - 1) * (buttonWidth + buttonGap)
        local button = ISButton:new(x, buttonY, buttonWidth, 44, "", self, LCCQFDialoguePanel.onChoicePressed)
        button:initialise()
        button:instantiate()
        button.choiceId = nil
        button:setVisible(false)
        self:addChild(button)
        self.choiceButtons[i] = button
    end
end

function LCCQFDialoguePanel:prerender()
    ISPanel.prerender(self)
    self:drawText(self.npcName, 24, 20, 1, 1, 1, 1, UIFont.Medium)
end

function LCCQFDialoguePanel:setChoicesEnabled(enabled)
    for _, button in ipairs(self.choiceButtons) do
        button.enable = enabled and button.choiceId ~= nil
    end
end

function LCCQFDialoguePanel:updateState(state)
    if not state or tostring(state.sessionId) ~= tostring(self.sessionId) then return false end

    self.npcName = state.npcName or self.npcName
    self.body.text = tostring(state.text or "")
    self.body:paginate()
    self.body:setYScroll(0)

    local choices = state.choices or {}
    for i = 1, #self.choiceButtons do
        local button = self.choiceButtons[i]
        local choice = choices[i]
        if choice and type(choice.choiceId) == "string" then
            button.choiceId = choice.choiceId
            button:setTitle(tostring(choice.text or "..."))
            button.enable = true
            button:setVisible(true)
        else
            button.choiceId = nil
            button.enable = false
            button:setVisible(false)
        end
    end

    return true
end

function LCCQFDialoguePanel:onChoicePressed(button)
    if not button or not button.choiceId or not self.onChoiceCallback then return end
    self:setChoicesEnabled(false)
    self.onChoiceCallback(self.sessionId, button.choiceId)
end

function LCCQFDialoguePanel:onClosePressed()
    self:close(true)
end

function LCCQFDialoguePanel:close(notifyServer)
    if self.closed then return end
    self.closed = true

    if notifyServer and self.onCloseCallback then
        self.onCloseCallback(self.sessionId)
    end

    self:setVisible(false)
    self:removeFromUIManager()
    if LCCQFDialoguePanel.instance == self then
        LCCQFDialoguePanel.instance = nil
    end
end

function LCCQFDialoguePanel.open(state, onChoice, onClose)
    if not state or not state.sessionId then return nil end

    local current = LCCQFDialoguePanel.instance
    if current and tostring(current.sessionId) == tostring(state.sessionId) then
        current:updateState(state)
        return current
    end
    if current then current:close(true) end

    local panel = LCCQFDialoguePanel:new(state.npcName, state.sessionId, onChoice, onClose)
    panel:initialise()
    panel:addToUIManager()
    panel:setAlwaysOnTop(true)
    panel:updateState(state)
    LCCQFDialoguePanel.instance = panel
    return panel
end

return LCCQFDialoguePanel
