require "ISUI/ISPanel"

NPC_DialogueUI = ISPanel:derive("NPC_DialogueUI")

local NPC_DialogueEngine = require("DialogueFramework/Dialogue/NPC_DialogueEngine")
local NPC_DialogueInputHandler = require("DialogueFramework/UI/NPC_DialogueInputHandler")
local NPC_DialogueConfig = require("DialogueFramework/Dialogue/NPC_DialogueConfig")

local TIER_SMALL_WIDTH = 400
local TIER_SMALL_HEIGHT = 140

local TIER_MEDIUM_WIDTH = 600
local TIER_MEDIUM_HEIGHT = 180

local TIER_LARGE_WIDTH = 800

local TIER_CONFIG = {
    [1] = {
        width = 850,
        height = 155,
        maxResponses = 3,
        boxWidth = 770,
        boxHeight = 45,
        boxX = 40,
        boxYPositions = {12, 61, 110}
    },
    [2] = {
        width = 950,
        height = 245,
        maxResponses = 5,
        boxWidth = 870,
        boxHeight = 40,
        boxX = 40,
        boxYPositions = {20, 66, 112, 158, 204}
    },
    [3] = {
        width = 1100,
        height = 400,
        maxResponses = 9,
        boxWidth = 1020,
        boxHeight = 38,
        boxX = 40,
        boxYPositions = {18, 61, 104, 147, 190, 233, 276, 319, 362}
    }
}

local COLOR_BOX_OUTLINE = {r=1.0, g=0.9, b=0.4, a=0.8}

local UI_STATE = {
    NPC_RESPONSE = "npc_response",
    STANDARD = "standard",
    FADING_OUT = "fading_out",
    FADING_IN = "fading_in"
}

local COLOR_NPC_NAME = {r=1.0, g=0.59, b=0.2, a=1.0}
local COLOR_NPC_TEXT = {r=1.0, g=0.78, b=0.39, a=1.0}
local COLOR_OPTION_SELECTED = {r=1.0, g=0.9, b=0.4, a=1.0}
local COLOR_OPTION_UNSELECTED = {r=0.6, g=0.6, b=0.6, a=1.0}
local COLOR_ARROW = {r=1.0, g=0.9, b=0.4, a=1.0}

function NPC_DialogueUI:new(player, session)
    local screenWidth = getCore():getScreenWidth()
    local screenHeight = getCore():getScreenHeight()

    local rootNode = session.sessionDef.nodes[session.currentNode]
    local nodeType = NPC_DialogueConfig.getNodeType(rootNode)

    local width, height, x, y
    local responseTier = nil

    if nodeType == NPC_DialogueConfig.NODE_TYPES.NPC_RESPONSE then
        local npcText = rootNode.npcText or ""

        local function countWrappedLines(text, maxWidth)
            if text == "" then
                return 1
            end

            local words = {}
            for word in text:gmatch("%S+") do
                table.insert(words, word)
            end

            local currentLine = ""
            local lineCount = 0

            for i, word in ipairs(words) do
                local testLine = currentLine == "" and word or (currentLine .. " " .. word)
                local lineWidth = getTextManager():MeasureStringX(UIFont.Medium, testLine)

                if lineWidth <= maxWidth then
                    currentLine = testLine
                else
                    if currentLine ~= "" then
                        lineCount = lineCount + 1
                    end
                    currentLine = word
                end
            end

            if currentLine ~= "" then
                lineCount = lineCount + 1
            end

            return lineCount
        end

        local smallLines = countWrappedLines(npcText, TIER_SMALL_WIDTH - 60)
        if smallLines <= 2 then
            width = TIER_SMALL_WIDTH
            height = TIER_SMALL_HEIGHT
        else
            local mediumLines = countWrappedLines(npcText, TIER_MEDIUM_WIDTH - 60)
            if mediumLines <= 4 then
                width = TIER_MEDIUM_WIDTH
                height = TIER_MEDIUM_HEIGHT
            else
                width = TIER_LARGE_WIDTH
                local largeLines = countWrappedLines(npcText, TIER_LARGE_WIDTH - 60)
                height = 50 + (largeLines * 16) + 60
            end
        end

        x = (screenWidth - width) / 2
        y = screenHeight - height - 80
    else
        local optionCount = rootNode.options and #rootNode.options or 0

        if optionCount <= 3 then
            responseTier = 1
        elseif optionCount <= 5 then
            responseTier = 2
        else
            responseTier = 3
        end

        local tierConfig = TIER_CONFIG[responseTier]
        width = tierConfig.width
        height = tierConfig.height
        x = (screenWidth - width) / 2
        y = screenHeight - height - 80
    end

    local o = ISPanel:new(x, y, width, height)
    setmetatable(o, self)
    self.__index = self

    o.player = player
    o.session = session
    o.sessionDef = session.sessionDef
    o.selectedOptionIndex = 1
    o.availableOptions = {}
    o.optionHitboxes = {}
    o.dialogueStartTime = getTimestampMs() / 1000.0

    o.responseTier = responseTier
    o.responseBoxes = {}
    o.hoveredBoxIndex = nil

    o.lastClickTime = 0
    o.clickCount = 0

    o.npcResponseTexture = getTexture("media/ui/npcresponseui.png")
    o.standardTexture = getTexture("media/ui/maindialogueui.png")

    if nodeType == NPC_DialogueConfig.NODE_TYPES.NPC_RESPONSE then
        o.uiState = UI_STATE.NPC_RESPONSE
    else
        o.uiState = UI_STATE.STANDARD
    end

    o.currentUIState = o.uiState
    o.targetUIState = nil
    o.fadeAlpha = 1.0
    o.fadeTimer = 0.0
    o.responseTimer = 0.0

    o:setVisible(true)
    o:setAlwaysOnTop(true)

    return o
end

function NPC_DialogueUI:initialise()
    ISPanel.initialise(self)

    self:setAlwaysOnTop(true)

    if self.uiState == UI_STATE.STANDARD then
        self:updateAvailableOptions()
    end

    NPC_DialogueInputHandler.setActiveUI(self)
end

function NPC_DialogueUI:update()
    ISPanel.update(self)

    local deltaTime = 1.0 / 60.0

    if self.uiState == UI_STATE.NPC_RESPONSE then
        self:updateNPCResponseState(deltaTime)
    elseif self.uiState == UI_STATE.FADING_OUT then
        self:updateFadeOut(deltaTime)
    elseif self.uiState == UI_STATE.FADING_IN then
        self:updateFadeIn(deltaTime)
    end
end

function NPC_DialogueUI:updateNPCResponseState(deltaTime)
    self.responseTimer = self.responseTimer + deltaTime

    local currentNode = NPC_DialogueEngine.getCurrentNode(self.player)
    if not currentNode then
        return
    end

    local displayDuration = currentNode.displayDuration or NPC_DialogueConfig.getDefaultDisplayDuration()

    if self.responseTimer >= displayDuration then
        self:advanceFromNPCResponse()
    end
end

function NPC_DialogueUI:updateFadeOut(deltaTime)
    self.fadeTimer = self.fadeTimer + deltaTime
    self.fadeAlpha = 1.0 - (self.fadeTimer / NPC_DialogueConfig.TIMING.FADE_DURATION)

    if self.fadeAlpha <= 0.0 then
        self.fadeAlpha = 0.0
        self:onFadeOutComplete()
    end
end

function NPC_DialogueUI:updateFadeIn(deltaTime)
    self.fadeTimer = self.fadeTimer + deltaTime
    self.fadeAlpha = self.fadeTimer / NPC_DialogueConfig.TIMING.FADE_DURATION

    if self.fadeAlpha >= 1.0 then
        self.fadeAlpha = 1.0
        self:onFadeInComplete()
    end
end

function NPC_DialogueUI:advanceFromNPCResponse()
    local currentNode = NPC_DialogueEngine.getCurrentNode(self.player)
    if not currentNode then
        return
    end

    local nextNodeID = NPC_DialogueEngine.resolveNextNode(
        currentNode,
        self.player,
        self.session.npcEntity
    )

    if not nextNodeID then
        self:close()
        return
    end

    if nextNodeID == NPC_DialogueConfig.SPECIAL_NODES.EXIT then
        self:close()
        return
    end

    print("[advanceFromNPCResponse] Calling advanceToNode with nextNodeID: " .. tostring(nextNodeID))
    local success = NPC_DialogueEngine.advanceToNode(self.player, nextNodeID)
    print("[advanceFromNPCResponse] advanceToNode returned: " .. tostring(success))

    if not success then
        print("[advanceFromNPCResponse] advanceToNode FAILED - closing session")
        self:close()
        return
    end

    print("[advanceFromNPCResponse] Getting current node after advancement...")
    local nextNode = NPC_DialogueEngine.getCurrentNode(self.player)
    print("[advanceFromNPCResponse] nextNode: " .. tostring(nextNode))

    if not nextNode then
        print("[advanceFromNPCResponse] ERROR: getCurrentNode returned nil after successful advanceToNode!")
        return
    end

    print("[advanceFromNPCResponse] Getting node type...")
    local nextNodeType = NPC_DialogueConfig.getNodeType(nextNode)
    print("[advanceFromNPCResponse] nextNodeType: " .. tostring(nextNodeType))
    print("[advanceFromNPCResponse] NPC_RESPONSE constant: " .. tostring(NPC_DialogueConfig.NODE_TYPES.NPC_RESPONSE))
    print("[advanceFromNPCResponse] STANDARD constant: " .. tostring(NPC_DialogueConfig.NODE_TYPES.STANDARD))
    print("[advanceFromNPCResponse] NPC_TRADING constant: " .. tostring(NPC_DialogueConfig.NODE_TYPES.NPC_TRADING))

    if nextNodeType == NPC_DialogueConfig.NODE_TYPES.NPC_RESPONSE then
        print("[advanceFromNPCResponse] Handler: NPC_RESPONSE")
        self:resetResponseTimer()
        self:transitionToNPCResponseUI()
    elseif nextNodeType == NPC_DialogueConfig.NODE_TYPES.STANDARD then
        print("[advanceFromNPCResponse] Handler: STANDARD")
        if self.session and self.session.npcEntity then
            if isClient() then
                sendClientCommand("NPCDialogue", "StartApproach", {
                    npcID = self.session.npcID,
                    playerID = self.player:getOnlineID()
                })
            else
                local NPC_Behavior_Talking = require("DialogueFramework/Behavior/Behaviors/NPC_Behavior_Talking")
                NPC_Behavior_Talking.startApproach(self.session.npcEntity, self.player)
            end
        end

        self:startFadeTransition(UI_STATE.STANDARD)
    elseif nextNodeType == NPC_DialogueConfig.NODE_TYPES.NPC_TRADING then
        print("[advanceFromNPCResponse] Handler: NPC_TRADING - MATCHED!")
        print("[advanceFromNPCResponse] Hiding dialogue UI...")
        self:setVisible(false)
        self:removeFromUIManager()
        print("[advanceFromNPCResponse] Calling openTradingUI...")
        NPC_DialogueEngine.openTradingUI(self.player, self.session.npcEntity, self.session, nextNode)
        print("[advanceFromNPCResponse] openTradingUI call completed")
    elseif nextNodeType == NPC_DialogueConfig.NODE_TYPES.NPC_INFO then
        print("[advanceFromNPCResponse] Handler: NPC_INFO - MATCHED!")
        print("[advanceFromNPCResponse] Hiding dialogue UI...")
        self:setVisible(false)
        self:removeFromUIManager()
        print("[advanceFromNPCResponse] Calling openInfoUI...")
        NPC_DialogueEngine.openInfoUI(self.player, self.session.npcEntity, self.session, nextNode)
        print("[advanceFromNPCResponse] openInfoUI call completed")
    else
        print("[advanceFromNPCResponse] ERROR: Unknown nodeType - no handler matched!")
    end
    print("[advanceFromNPCResponse] Function complete")
end

function NPC_DialogueUI:resetResponseTimer()
    self.responseTimer = 0.0
end

function NPC_DialogueUI:startFadeTransition(targetState)
    self.targetUIState = targetState
    self.uiState = UI_STATE.FADING_OUT
    self.fadeTimer = 0.0
    self.fadeAlpha = 1.0
end

function NPC_DialogueUI:onFadeOutComplete()
    self.uiState = UI_STATE.FADING_IN
    self.fadeTimer = 0.0
    self.fadeAlpha = 0.0
    self.currentUIState = self.targetUIState

    if self.targetUIState == UI_STATE.NPC_RESPONSE then
        self:transitionToNPCResponseUI()
    end
end

function NPC_DialogueUI:onFadeInComplete()
    self.uiState = self.targetUIState
    self.fadeAlpha = 1.0

    if self.uiState == UI_STATE.NPC_RESPONSE then
        self:resetResponseTimer()

        local currentNode = NPC_DialogueEngine.getCurrentNode(self.player)
        if currentNode and currentNode.soundFile then
            local NPC_SoundManager = require("DialogueFramework/Sound/NPC_SoundManager")
            if self.session and self.session.npcEntity and self.session.sessionID then
                local soundHandle = NPC_SoundManager.playDialogueSound(
                    self.session.npcEntity,
                    currentNode.soundFile,
                    self.session.sessionID
                )
                self.session.currentSoundHandle = soundHandle
            end
        end
    elseif self.uiState == UI_STATE.STANDARD then
        self:updateAvailableOptions()
        self:transitionToStandardUI()
    end
end

function NPC_DialogueUI:transitionToStandardUI()
    if not self.responseTier then
        return
    end

    local tierConfig = TIER_CONFIG[self.responseTier]
    if not tierConfig then
        return
    end

    local screenWidth = getCore():getScreenWidth()
    local screenHeight = getCore():getScreenHeight()

    self:setWidth(tierConfig.width)
    self:setHeight(tierConfig.height)

    local x = (screenWidth - tierConfig.width) / 2
    local y = screenHeight - tierConfig.height - 80
    self:setX(x)
    self:setY(y)
end

function NPC_DialogueUI:transitionToNPCResponseUI()
    local currentNode = NPC_DialogueEngine.getCurrentNode(self.player)
    if not currentNode then
        return
    end

    local npcText = currentNode.npcText or ""
    local screenWidth = getCore():getScreenWidth()
    local screenHeight = getCore():getScreenHeight()

    local function countWrappedLines(text, maxWidth)
        if text == "" then
            return 1
        end

        local words = {}
        for word in text:gmatch("%S+") do
            table.insert(words, word)
        end

        local currentLine = ""
        local lineCount = 0

        for i, word in ipairs(words) do
            local testLine = currentLine == "" and word or (currentLine .. " " .. word)
            local lineWidth = getTextManager():MeasureStringX(UIFont.Medium, testLine)

            if lineWidth <= maxWidth then
                currentLine = testLine
            else
                if currentLine ~= "" then
                    lineCount = lineCount + 1
                end
                currentLine = word
            end
        end

        if currentLine ~= "" then
            lineCount = lineCount + 1
        end

        return lineCount
    end

    local testWidth = TIER_MEDIUM_WIDTH - 60
    local tierTestLines = countWrappedLines(npcText, testWidth)

    local width, height
    if tierTestLines <= 2 then
        width = TIER_SMALL_WIDTH
        height = TIER_SMALL_HEIGHT
    elseif tierTestLines <= 4 then
        width = TIER_MEDIUM_WIDTH
        height = TIER_MEDIUM_HEIGHT
    else
        width = TIER_LARGE_WIDTH
        local actualLines = countWrappedLines(npcText, width - 60)
        height = 50 + (actualLines * 16) + 60
    end

    self:setWidth(width)
    self:setHeight(height)

    local x = (screenWidth - width) / 2
    local y = screenHeight - height - 80
    self:setX(x)
    self:setY(y)
end

function NPC_DialogueUI:skipNPCResponse()
    if self.uiState ~= UI_STATE.NPC_RESPONSE then
        return
    end

    if self.responseTimer < NPC_DialogueConfig.TIMING.MIN_RESPONSE_DURATION then
        return
    end

    self:advanceFromNPCResponse()
end

function NPC_DialogueUI:getUIState()
    return self.uiState
end

function NPC_DialogueUI:updateAvailableOptions()
    self.availableOptions = NPC_DialogueEngine.getAvailableOptions(self.player)

    if #self.availableOptions > 0 then
        if self.selectedOptionIndex > #self.availableOptions then
            self.selectedOptionIndex = #self.availableOptions
        end
        if self.selectedOptionIndex < 1 then
            self.selectedOptionIndex = 1
        end
    else
        self.selectedOptionIndex = 1
    end

    self:updateOptionHitboxes()
    self:generateResponseBoxes()
end

function NPC_DialogueUI:generateResponseBoxes()
    self.responseBoxes = {}

    if self.uiState ~= UI_STATE.STANDARD then
        return
    end

    if not self.responseTier then
        local optionCount = #self.availableOptions
        if optionCount == 0 then
            return
        end

        if optionCount <= 3 then
            self.responseTier = 1
        elseif optionCount <= 5 then
            self.responseTier = 2
        else
            self.responseTier = 3
        end
    end

    local tierConfig = TIER_CONFIG[self.responseTier]
    if not tierConfig then
        return
    end

    local optionCount = #self.availableOptions
    if optionCount == 0 then
        return
    end

    for i = 1, tierConfig.maxResponses do
        local hasResponse = i <= optionCount
        local box = {
            x = tierConfig.boxX,
            y = tierConfig.boxYPositions[i],
            width = tierConfig.boxWidth,
            height = tierConfig.boxHeight,
            hasResponse = hasResponse,
            responseIndex = hasResponse and i or nil
        }
        table.insert(self.responseBoxes, box)
    end
end

function NPC_DialogueUI:render()
    if self.uiState == UI_STATE.NPC_RESPONSE or (self.uiState == UI_STATE.FADING_OUT and self.currentUIState == UI_STATE.NPC_RESPONSE) then
        self:renderNPCResponseTexture()
    elseif self.uiState == UI_STATE.STANDARD or (self.uiState == UI_STATE.FADING_IN and self.targetUIState == UI_STATE.STANDARD) then
        self:renderMainDialogueTexture()
    end

    self:renderNPCName()

    if self.uiState == UI_STATE.NPC_RESPONSE or (self.uiState == UI_STATE.FADING_OUT and self.currentUIState == UI_STATE.NPC_RESPONSE) then
        self:renderNPCResponse()
    end

    if self.uiState == UI_STATE.STANDARD or (self.uiState == UI_STATE.FADING_IN and self.targetUIState == UI_STATE.STANDARD) then
        self:renderPlayerOptions()
    end

    if self.uiState == UI_STATE.NPC_RESPONSE and self.responseTimer >= NPC_DialogueConfig.TIMING.MIN_RESPONSE_DURATION then
        self:renderSkipHint()
    end
end

function NPC_DialogueUI:renderNPCResponseTexture()
    if self.npcResponseTexture then
        self:drawTextureScaled(self.npcResponseTexture, 0, 0, self.width, self.height, self.fadeAlpha, 1.0, 1.0, 1.0)
    end
end

function NPC_DialogueUI:renderMainDialogueTexture()
    if self.standardTexture then
        self:drawTextureScaled(self.standardTexture, 0, 0, self.width, self.height, self.fadeAlpha, 1.0, 1.0, 1.0)
    end
end

function NPC_DialogueUI:getCurrentTexture()
    if self.uiState == UI_STATE.NPC_RESPONSE or (self.uiState == UI_STATE.FADING_OUT and self.currentUIState == UI_STATE.NPC_RESPONSE) then
        return self.npcResponseTexture or self.standardTexture
    elseif self.uiState == UI_STATE.STANDARD or (self.uiState == UI_STATE.FADING_IN and self.targetUIState == UI_STATE.STANDARD) then
        return self.standardTexture
    else
        if self.targetUIState == UI_STATE.STANDARD then
            return self.standardTexture
        else
            return self.npcResponseTexture or self.standardTexture
        end
    end
end

function NPC_DialogueUI:renderNPCName()
    local npcName = self.sessionDef.displayName or "NPC"

    local letterSpacing = 10
    local stretchedWidth = 0

    for i = 1, #npcName do
        local char = npcName:sub(i, i)
        local charWidth = getTextManager():MeasureStringX(UIFont.Large, char)
        stretchedWidth = stretchedWidth + charWidth + letterSpacing
    end

    local nameX = self.width - stretchedWidth - 5
    local nameY = 10

    if self.uiState == UI_STATE.STANDARD or (self.uiState == UI_STATE.FADING_IN and self.targetUIState == UI_STATE.STANDARD) then
        nameY = -25
    end

    local currentX = nameX
    for i = 1, #npcName do
        local char = npcName:sub(i, i)

        local offsets = {
            {-1, -1}, {0, -1}, {1, -1},
            {-1, 0}, {1, 0},
            {-1, 1}, {0, 1}, {1, 1}
        }

        for _, offset in ipairs(offsets) do
            self:drawText(
                char,
                currentX + offset[1],
                nameY + offset[2],
                0.0, 0.0, 0.0,
                self.fadeAlpha,
                UIFont.Large
            )
        end

        self:drawText(
            char,
            currentX,
            nameY,
            COLOR_NPC_NAME.r,
            COLOR_NPC_NAME.g,
            COLOR_NPC_NAME.b,
            COLOR_NPC_NAME.a * self.fadeAlpha,
            UIFont.Large
        )

        local charWidth = getTextManager():MeasureStringX(UIFont.Large, char)
        currentX = currentX + charWidth + letterSpacing
    end
end

function NPC_DialogueUI:renderNPCResponse()
    local currentNode = NPC_DialogueEngine.getCurrentNode(self.player)
    if not currentNode then
        return
    end

    local npcText = currentNode.npcText or ""

    local responseY = 50
    local textWidth = self.width - 60
    local textX = 30

    local wrappedText = self:wrapText(npcText, textWidth, UIFont.Medium)

    for i, line in ipairs(wrappedText) do
        local lineY = responseY + ((i - 1) * 16)
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

function NPC_DialogueUI:renderPlayerOptions()
    if #self.availableOptions == 0 or #self.responseBoxes == 0 then
        return
    end

    for i, box in ipairs(self.responseBoxes) do
        if box.hasResponse then
            local responseIndex = box.responseIndex
            local option = self.availableOptions[responseIndex]

            if option then
                local isSelected = responseIndex == self.selectedOptionIndex
                local isHovered = i == self.hoveredBoxIndex

                if isSelected or isHovered then
                    self:renderBoxOutline(box, isSelected)
                end

                local arrowX = box.x + 5
                local textX = box.x + 25
                local textY = box.y + 15

                local color = isSelected and COLOR_OPTION_SELECTED or COLOR_OPTION_UNSELECTED

                if isSelected then
                    self:drawText(
                        "►",
                        arrowX,
                        textY,
                        COLOR_ARROW.r,
                        COLOR_ARROW.g,
                        COLOR_ARROW.b,
                        COLOR_ARROW.a * self.fadeAlpha,
                        UIFont.Large
                    )
                end

                local playerText = option.playerText or ""
                local maxTextWidth = box.width - 35

                local wrappedLines = self:wrapText(playerText, maxTextWidth, UIFont.Large)
                for lineIndex, line in ipairs(wrappedLines) do
                    if lineIndex > 2 then
                        break
                    end

                    local lineY = textY + ((lineIndex - 1) * 18)
                    self:drawText(
                        line,
                        textX,
                        lineY,
                        color.r,
                        color.g,
                        color.b,
                        color.a * self.fadeAlpha,
                        UIFont.Large
                    )
                end
            end
        end
    end
end

function NPC_DialogueUI:renderBoxOutline(box, isSelected)
    local alpha = isSelected and 1.0 or COLOR_BOX_OUTLINE.a
    local lineWidth = isSelected and 2 or 2

    self:drawRectBorder(
        box.x,
        box.y,
        box.width,
        box.height,
        alpha * self.fadeAlpha,
        COLOR_BOX_OUTLINE.r,
        COLOR_BOX_OUTLINE.g,
        COLOR_BOX_OUTLINE.b
    )
end

function NPC_DialogueUI:renderSkipHint()
    local hintText = "Press [E] to continue..."
    local textWidth = getTextManager():MeasureStringX(UIFont.Small, hintText)

    local hintX = (self.width - textWidth) / 2
    local hintY = self.height - 30

    self:drawText(
        hintText,
        hintX,
        hintY,
        0.8, 0.8, 0.8, self.fadeAlpha * 0.7,
        UIFont.Small
    )
end

function NPC_DialogueUI:wrapText(text, maxWidth, font)
    local lines = {}
    local words = {}

    for word in text:gmatch("%S+") do
        table.insert(words, word)
    end

    local currentLine = ""
    local testFont = font or UIFont.Medium

    for i, word in ipairs(words) do
        local testLine = currentLine == "" and word or (currentLine .. " " .. word)
        local lineWidth = getTextManager():MeasureStringX(testFont, testLine)

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

function NPC_DialogueUI:selectPreviousOption()
    if self.selectedOptionIndex > 1 then
        self.selectedOptionIndex = self.selectedOptionIndex - 1
    end
end

function NPC_DialogueUI:selectNextOption()
    if self.selectedOptionIndex < #self.availableOptions then
        self.selectedOptionIndex = self.selectedOptionIndex + 1
    end
end

function NPC_DialogueUI:confirmSelection()
    if #self.availableOptions == 0 then
        return
    end

    local selectedOption = self.availableOptions[self.selectedOptionIndex]
    if not selectedOption then
        return
    end

    local success, result = NPC_DialogueEngine.selectOption(self.player, selectedOption.id)

    if success then
        if result == "EXIT" then
            self:close()
        else
            local currentNode = NPC_DialogueEngine.getCurrentNode(self.player)
            if not currentNode then
                self:close()
                return
            end

            local nodeType = NPC_DialogueConfig.getNodeType(currentNode)

            if nodeType == NPC_DialogueConfig.NODE_TYPES.NPC_RESPONSE then
                local NPC_SoundManager = require("DialogueFramework/Sound/NPC_SoundManager")
                if self.session and self.session.npcEntity and self.session.sessionID then
                    NPC_SoundManager.stopDialogueSound(self.session.npcEntity, self.session.sessionID)
                end
                self:startFadeTransition(UI_STATE.NPC_RESPONSE)
            elseif nodeType == NPC_DialogueConfig.NODE_TYPES.STANDARD then
                self:updateAvailableOptions()
            end
        end
    end
end

function NPC_DialogueUI:getDialogueDuration()
    local currentTime = getTimestampMs() / 1000.0
    return currentTime - self.dialogueStartTime
end

function NPC_DialogueUI:cancelDialogue()
    self:close()
end

function NPC_DialogueUI:updateOptionHitboxes()
    self.optionHitboxes = {}

    if not self.responseTier or self.uiState ~= UI_STATE.STANDARD then
        return
    end

    for i, box in ipairs(self.responseBoxes) do
        if box.hasResponse then
            self.optionHitboxes[i] = {
                x = box.x,
                y = box.y,
                width = box.width,
                height = box.height,
                optionIndex = box.responseIndex,
                active = true
            }
        end
    end
end

function NPC_DialogueUI:onMouseMove(dx, dy)
    if not self:getIsVisible() then
        self.hoveredBoxIndex = nil
        return
    end

    if self.uiState ~= UI_STATE.STANDARD then
        self.hoveredBoxIndex = nil
        return
    end

    if not self.responseBoxes or #self.responseBoxes == 0 then
        self.hoveredBoxIndex = nil
        return
    end

    local mouseX = self:getMouseX()
    local mouseY = self:getMouseY()

    if not mouseX or not mouseY then
        self.hoveredBoxIndex = nil
        return
    end

    self.hoveredBoxIndex = nil

    for i, box in ipairs(self.responseBoxes) do
        if box and box.hasResponse then
            if mouseX >= box.x and mouseX <= (box.x + box.width) and
               mouseY >= box.y and mouseY <= (box.y + box.height) then
                self.hoveredBoxIndex = i
                if box.responseIndex then
                    self.selectedOptionIndex = box.responseIndex
                end
                return
            end
        end
    end
end

function NPC_DialogueUI:onMouseDown(x, y)
    if not self:getIsVisible() then
        return false
    end

    if self.uiState == UI_STATE.NPC_RESPONSE then
        local currentTime = getTimestampMs() / 1000.0
        local timeSinceLastClick = currentTime - self.lastClickTime

        if timeSinceLastClick <= 2.0 then
            self.clickCount = self.clickCount + 1
        else
            self.clickCount = 1
        end

        self.lastClickTime = currentTime

        if self.clickCount >= 2 then
            self.clickCount = 0

            local NPC_SoundManager = require("DialogueFramework/Sound/NPC_SoundManager")
            if self.session and self.session.npcEntity and self.session.sessionID then
                NPC_SoundManager.stopDialogueSound(self.session.npcEntity, self.session.sessionID)
            end

            self:advanceFromNPCResponse()
        end

        return true
    end

    if self.uiState ~= UI_STATE.STANDARD then
        return false
    end

    if not self.responseBoxes or #self.responseBoxes == 0 then
        return false
    end

    local mouseX = self:getMouseX()
    local mouseY = self:getMouseY()

    if not mouseX or not mouseY then
        return false
    end

    for i, box in ipairs(self.responseBoxes) do
        if box and box.hasResponse then
            if mouseX >= box.x and mouseX <= (box.x + box.width) and
               mouseY >= box.y and mouseY <= (box.y + box.height) then
                if box.responseIndex then
                    self.selectedOptionIndex = box.responseIndex
                    self:confirmSelection()
                    return true
                end
            end
        end
    end

    return false
end

function NPC_DialogueUI:isMouseOver()
    return self:getIsVisible() and self.mouseOver
end

function NPC_DialogueUI:close()
    local NPC_InteractionCoordinator = require("DialogueFramework/Interaction/NPC_InteractionCoordinator")
    local NPC_IdleSoundSystem = require("DialogueFramework/Sound/NPC_IdleSoundSystem")
    local NPC_InteractionScanner = require("DialogueFramework/Interaction/NPC_InteractionScanner")

    NPC_DialogueInputHandler.clearActiveUI()
    NPC_DialogueEngine.endSession(self.player)

    ISTimedActionQueue.clear(self.player)

    NPC_InteractionCoordinator.setInteractionActive(false)

    NPC_InteractionScanner.updateScanTime()

    NPC_IdleSoundSystem.recordDialogueClose(self.player)

    self:setVisible(false)
    self:removeFromUIManager()
end

function NPC_DialogueUI:onResolutionChange(oldw, oldh, neww, newh)
    local screenWidth = getCore():getScreenWidth()
    local screenHeight = getCore():getScreenHeight()

    local newX = (screenWidth - self.width) / 2
    local newY = screenHeight - self.height - 80

    self:setX(newX)
    self:setY(newY)
end

return NPC_DialogueUI
