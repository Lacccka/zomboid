require "ISUI/ISPanel"
require "ISUI/ISButton"
require "ISUI/ISRichTextPanel"
require "LCCQF/LCCQFConstants"
require "LCCQF/LCCQFContent"

LCCQFDialoguePanel = ISPanel:derive("LCCQFDialoguePanel")
LCCQFDialoguePanel.instance = nil

function LCCQFDialoguePanel:new(npcName, dialogueId, sessionId)
    local width = math.min(700, getCore():getScreenWidth() - 80)
    local height = math.min(430, getCore():getScreenHeight() - 80)
    local x = math.floor((getCore():getScreenWidth() - width) / 2)
    local y = math.floor((getCore():getScreenHeight() - height) / 2)

    local o = ISPanel:new(x, y, width, height)
    setmetatable(o, self)
    self.__index = self

    o.npcName = npcName or "NPC"
    o.dialogueId = dialogueId
    o.sessionId = sessionId
    o.currentNodeId = nil
    o.moveWithMouse = true
    o.backgroundColor = { r = 0.05, g = 0.05, b = 0.05, a = 0.94 }
    o.borderColor = { r = 0.55, g = 0.55, b = 0.55, a = 1.0 }
    o.choiceButtons = {}

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
        button.choiceIndex = i
        button:setVisible(false)
        self:addChild(button)
        self.choiceButtons[i] = button
    end

    local dialogue = LCCQF.GetDialogue(self.dialogueId)
    if dialogue then
        self:showNode(dialogue.start)
    else
        self.body.text = "Диалог не найден: " .. tostring(self.dialogueId)
        self.body:paginate()
    end
end

function LCCQFDialoguePanel:prerender()
    ISPanel.prerender(self)
    self:drawText(self.npcName, 24, 20, 1, 1, 1, 1, UIFont.Medium)
end

function LCCQFDialoguePanel:getCurrentNode()
    local dialogue = LCCQF.GetDialogue(self.dialogueId)
    if not dialogue or not dialogue.nodes then return nil end
    return dialogue.nodes[self.currentNodeId]
end

function LCCQFDialoguePanel:showNode(nodeId)
    local dialogue = LCCQF.GetDialogue(self.dialogueId)
    if not dialogue or not dialogue.nodes then return end

    local node = dialogue.nodes[nodeId]
    if not node then return end

    self.currentNodeId = nodeId
    self.body.text = node.text or ""
    self.body:paginate()
    if self.body.setYScroll then
        self.body:setYScroll(0)
    end

    local choices = node.choices or {}
    for i = 1, #self.choiceButtons do
        local button = self.choiceButtons[i]
        local choice = choices[i]
        if choice then
            button:setTitle(choice.text or "...")
            button:setVisible(true)
        else
            button:setVisible(false)
        end
    end
end

function LCCQFDialoguePanel:onChoicePressed(button)
    local node = self:getCurrentNode()
    if not node then return end

    local choice = node.choices and node.choices[button.choiceIndex]
    if not choice then return end

    if choice.close then
        self:close()
        return
    end

    if choice.next then
        self:showNode(choice.next)
    end
end

function LCCQFDialoguePanel:onClosePressed()
    self:close()
end

function LCCQFDialoguePanel:close()
    if self.sessionId and isClient() then
        local player = getSpecificPlayer(0)
        if player then
            sendClientCommand(player, LCCQF.Constants.MODULE, "CloseDialogue", { sessionId = self.sessionId })
        end
    end

    self:setVisible(false)
    self:removeFromUIManager()

    if LCCQFDialoguePanel.instance == self then
        LCCQFDialoguePanel.instance = nil
    end
end

function LCCQFDialoguePanel.open(npcName, dialogueId, sessionId)
    if LCCQFDialoguePanel.instance then
        LCCQFDialoguePanel.instance:close()
    end

    local panel = LCCQFDialoguePanel:new(npcName, dialogueId, sessionId)
    panel:initialise()
    panel:addToUIManager()
    panel:setAlwaysOnTop(true)
    LCCQFDialoguePanel.instance = panel
    return panel
end

return LCCQFDialoguePanel
