require "OptionScreens/MainScreen"

local hdLogoScale = 4
local hdLogoPath = "media/ui/logo_custom.png"
local MAX_WIDTH_FACTOR = 0.5
local UI_BORDER_SPACING = 10
local JOYPAD_TEX_SIZE = 32
local WARNING_FADE_SPEED = 1.5 / 60
local SECONDS_PER_FRAME = 1.0 / 60

local vanillaNew = MainScreen.new
local missingTextureWarningShown = false

local function logHdLogo(message)
    print("[EFZ Quests] hdlogo.lua: " .. message)
end

local function getLogoDrawMetrics(self)
    if self.logoTextureHD then
        return self.logoTextureHD, self.logoTextureHD:getWidth() / hdLogoScale, self.logoTextureHD:getHeight() / hdLogoScale
    end

    if not missingTextureWarningShown then
        logHdLogo("missing '" .. hdLogoPath .. "', using the vanilla logo")
        missingTextureWarningShown = true
    end

    return self.logoTexture, self.logoTexture:getWidth(), self.logoTexture:getHeight()
end

function MainScreen:new(inGame)
    local o = vanillaNew(self, inGame)

    useTextureFiltering(true)
    o.logoTextureHD = getTexture(hdLogoPath)
    useTextureFiltering(false)

    return o
end

function MainScreen:prerender()
    ISPanel.prerender(self)

    if self.inGame then
        self:drawRect(0, 0, self.width, self.height, 0.5, self.backgroundColor.r, self.backgroundColor.g, self.backgroundColor.b)
        if isQuitCooldown() then
            self.exitOption:setColor(0.5, 0.5, 0.5)
            self.quitToDesktopOption:setColor(0.5, 0.5, 0.5)
        else
            self.exitOption:setColor(1, 1, 1)
            self.quitToDesktopOption:setColor(1, 1, 1)
        end
    end

    if self.delay > 0 then
        if self.firstFrame then
            self.delay = self.delay - UIManager.getMillisSinceLastRender()
        else
            self.firstFrame = true
        end
    end

    self.time = self.time + (SECONDS_PER_FRAME * getGameTime():getMultiplier())

    local mainScreen = MainScreenState.getInstance()
    if mainScreen ~= nil and ISDemoPopup.instance == nil then
        local x = 50
        local y = 50
        local sw = getCore():getScreenWidth()
        local tex, logicalWidth, logicalHeight = getLogoDrawMetrics(self)
        local resdelta = math.min(self:calcLogoHeight() / logicalHeight, sw / 1920)
        local w = logicalWidth * resdelta
        local h = logicalHeight * resdelta

        x = x * (sw / 1920)
        y = y * (sw / 1920)

        self:drawTextureScaled(tex, x, y, w, h, 1 - (self.warningFade / self.warningFadeMax), 1, 1, 1.0)
        if getDebug() and getDebugOptions():getBoolean("UI.Render.Outline") then
            self:drawRectBorder(x, y, w, h, 1, 1, 1, 1)
        end

        self.warningFade = self.warningFade - (WARNING_FADE_SPEED * getGameTime():getMultiplier())
        if self.warningFade < 0 then
            self.warningFade = 0
        end

        local maxWidth = math.max(w * MAX_WIDTH_FACTOR, self.maxMenuItemWidth)
        for _, child in pairs(self.bottomPanel:getChildren()) do
            if child.Type == "ISLabel" then
                child:setWidth(maxWidth)
            end
        end

        self.bottomPanel:setWidth(maxWidth)
        self.bottomPanel:setX(math.max(UI_BORDER_SPACING * 2 + JOYPAD_TEX_SIZE + 1, x + (w - self.bottomPanel:getWidth()) / 2))
        self.bottomPanel:setY(x + h + 50 * (sw / 1920))
    end

    if isDemo() and not self.inGame then
        if self.bottomPanel:getIsVisible() then
            if not self.demoMessagePanel then
                self.demoMessagePanel = ISRichTextPanel:new(self.width / 2 - 800 / 2, 0, 800, 35 * 3)
                self.demoMessagePanel:setAnchorTop(false)
                self.demoMessagePanel:setAnchorBottom(true)
                self.demoMessagePanel.font = UIFont.Medium
                self:addChild(self.demoMessagePanel)
                self.demoMessagePanel.text = getText("UI_Demo_Welcome")
                self.demoMessagePanel:paginate()
            end

            self.demoMessagePanel:setX(self.width / 2 - self.demoMessagePanel:getWidth() / 2)
            self.demoMessagePanel:setY(self.bottomPanel:getY() - 24 - self.demoMessagePanel:getHeight())
        end

        if self.demoMessagePanel then
            self.demoMessagePanel:setVisible(self.bottomPanel:getIsVisible())
        end
    end
end