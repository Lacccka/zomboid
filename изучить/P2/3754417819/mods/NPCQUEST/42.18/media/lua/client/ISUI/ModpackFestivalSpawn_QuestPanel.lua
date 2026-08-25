require "ISUI/ISPanel"
require "ISUI/ISButton"

ModpackFestivalQuestPanel = ModpackFestivalQuestPanel or {}

local function ensureQuestPanelClass()
    if ModpackFestivalQuestPanel.__mpfClassReady then
        return true
    end
    if not ISPanel or not ISPanel.derive then
        return false
    end
    local methods = ModpackFestivalQuestPanel
    ModpackFestivalQuestPanel = ISPanel:derive("ModpackFestivalQuestPanel")
    for k, v in pairs(methods) do
        if type(v) == "function" and ModpackFestivalQuestPanel[k] == nil then
            ModpackFestivalQuestPanel[k] = v
        end
    end
    ModpackFestivalQuestPanel.__mpfClassReady = true
    return true
end

if not ISPanel or not ISPanel.derive then
    ModpackFestivalQuestPanel = {}
elseif not ModpackFestivalQuestPanel.__mpfClassReady then
    ModpackFestivalQuestPanel = ISPanel:derive("ModpackFestivalQuestPanel")
    ModpackFestivalQuestPanel.__mpfClassReady = true
end

local function getFontHeightSafe(font)
    if getTextManager then
        local tm = getTextManager()
        if tm and tm.getFontHeight then
            return tm:getFontHeight(font)
        end
    end
    return 16
end

local FONT_HGT_SMALL = getFontHeightSafe(UIFont.Small)
local FONT_HGT_MEDIUM = getFontHeightSafe(UIFont.Medium)
local FONT_HGT_DIST = math.max(1, math.floor(FONT_HGT_MEDIUM * 2 / 3))
local DIST_FONT = UIFont.Small
local PANEL_W = 300
local PANEL_H_EXPANDED = 120
local PAD = 8
local BTN_SIZE = FONT_HGT_SMALL + 4
local GLOW_DURATION_MS = 5000
local DEFAULT_BORDER = { r = 0.45, g = 0.35, b = 0.2, a = 1 }
local NAV_COMPASS_FILL = { r = 0.08, g = 0.08, b = 0.1, a = 0.85 }
local NAV_COMPASS_RING = { r = 0.55, g = 0.45, b = 0.28, a = 0.9 }
local NAV_ARROW_FILL = { r = 0.98, g = 0.88, b = 0.62, a = 1 }
local NAV_DIST_COLOR = { r = 0.9, g = 0.88, b = 0.84, a = 1 }
local DESC_CHARS_PER_LINE = 38
local DEFAULT_Y_SCREEN_FRACTION = 0.62

local function questShowsDistance(quest)
    return ModpackFestivalQuests
        and ModpackFestivalQuests.questHasNavigationTarget
        and ModpackFestivalQuests.questHasNavigationTarget(quest)
end

local function splitLinesByNewline(text)
    local out = {}
    if not text or text == "" then
        out[1] = ""
        return out
    end
    for line in tostring(text):gmatch("([^\n]*)\n?") do
        if line == nil then break end
        if line == "" and #out > 0 and out[#out] == "" then
            break
        end
        out[#out + 1] = line
    end
    if #out == 0 then out[1] = "" end
    return out
end

local function wrapLineToWidth(line, maxWidth, font)
    if not line or line == "" then return { "" } end
    local tm = getTextManager()
    if tm:MeasureStringX(font, line) <= maxWidth then
        return { line }
    end

    local words = {}
    for w in tostring(line):gmatch("%S+") do
        words[#words + 1] = w
    end
    if #words == 0 then return { "" } end

    local out = {}
    local cur = words[1]
    for i = 2, #words do
        local candidate = cur .. " " .. words[i]
        if tm:MeasureStringX(font, candidate) <= maxWidth then
            cur = candidate
        else
            out[#out + 1] = cur
            cur = words[i]
        end
    end
    out[#out + 1] = cur
    return out
end

local function wrapTextToWidth(text, maxWidth, font)
    local rawLines = splitLinesByNewline(text)
    local out = {}
    for i = 1, #rawLines do
        local wrapped = wrapLineToWidth(rawLines[i], maxWidth, font)
        for j = 1, #wrapped do
            out[#out + 1] = wrapped[j]
        end
    end
    if #out == 0 then out[1] = "" end
    return out
end

local function formatDistTiles(distTiles)
    if distTiles == nil or distTiles >= 9000 then return nil end
    return math.max(0, math.floor(distTiles + 0.5))
end

local function formatQuestUiText(text)
    if not text or text == "" then return text end
    local player = getSpecificPlayer and getSpecificPlayer(0) or nil
    if ModpackFestivalQuests and ModpackFestivalQuests.formatQuestText then
        return ModpackFestivalQuests.formatQuestText(text, player)
    end
    return text
end

local COMPASS_W = 30
local COMPASS_H = 30
local ARROW_TEXT_GAP = 8
local DIST_ROW_EXTRA = 2

local function rotateLocalPoint(cx, cy, lx, ly, rotRad)
    local c = math.cos(rotRad)
    local s = math.sin(rotRad)
    return cx + lx * c - ly * s, cy + lx * s + ly * c
end

local function drawQuestNavArrow(panel, cx, cy, rotRad, size, color)
    if rotRad == nil or not panel then return end

    local r, g, b, a = color.r, color.g, color.b, color.a
    local wing = size * 0.55
    local base = size * 0.36

    local tipX, tipY = rotateLocalPoint(cx, cy, 0, -size, rotRad)
    local leftX, leftY = rotateLocalPoint(cx, cy, -wing, base, rotRad)
    local rightX, rightY = rotateLocalPoint(cx, cy, wing, base, rotRad)
    local tailX, tailY = rotateLocalPoint(cx, cy, 0, base * 0.55, rotRad)

    if panel.drawPolygon then
        panel:drawPolygon(nil, tipX, tipY, leftX, leftY, rightX, rightY, tailX, tailY, r, g, b, a)
    end
    if panel.drawLine2 then
        local lr, lg, lb = r * 0.65, g * 0.65, b * 0.65
        panel:drawLine2(tipX, tipY, leftX, leftY, a, lr, lg, lb)
        panel:drawLine2(tipX, tipY, rightX, rightY, a, lr, lg, lb)
        panel:drawLine2(leftX, leftY, rightX, rightY, a * 0.85, lr, lg, lb)
    end
end

local function drawCompassChrome(panel, boxX, boxY, boxW, boxH)
    local cx = boxX + boxW * 0.5
    local cy = boxY + boxH * 0.5
    local inset = 2

    panel:drawRect(boxX, boxY, boxW, boxH, NAV_COMPASS_FILL.a,
        NAV_COMPASS_FILL.r, NAV_COMPASS_FILL.g, NAV_COMPASS_FILL.b)
    panel:drawRectBorder(boxX, boxY, boxW, boxH, NAV_COMPASS_RING.a,
        NAV_COMPASS_RING.r, NAV_COMPASS_RING.g, NAV_COMPASS_RING.b)

    local innerX = boxX + inset
    local innerY = boxY + inset
    local innerW = boxW - inset * 2
    local innerH = boxH - inset * 2
    panel:drawRectBorder(innerX, innerY, innerW, innerH, 0.35,
        NAV_COMPASS_RING.r * 0.6, NAV_COMPASS_RING.g * 0.6, NAV_COMPASS_RING.b * 0.6)

    if panel.drawRect then
        panel:drawRect(cx - 1, cy - 1, 2, 2, 0.55, 0.4, 0.35, 0.3)
    end

    return cx, cy
end

function ModpackFestivalQuestPanel:getDistanceRowHeight()
    if not self:showsDistanceLine() then
        return 0
    end
    return math.max(FONT_HGT_DIST, COMPASS_H) + DIST_ROW_EXTRA
end

local function getDefaultPanelXY()
    local sw = getCore():getScreenWidth()
    local sh = getCore():getScreenHeight()
    local x = math.floor((sw - PANEL_W) / 2)
    local y = math.floor(sh * DEFAULT_Y_SCREEN_FRACTION)
    return x, y
end

function ModpackFestivalQuestPanel:getHeaderHeight()
    return PAD * 2 + FONT_HGT_SMALL
end

function ModpackFestivalQuestPanel:showsDistanceLine()
    return self.quest
        and questShowsDistance(self.quest)
        and formatDistTiles(self.distTiles) ~= nil
end

function ModpackFestivalQuestPanel:getMinimizedHeight()
    local h = self:getHeaderHeight()
    if self:showsDistanceLine() then
        h = h + self:getDistanceRowHeight()
    end
    return h
end

function ModpackFestivalQuestPanel:layoutChrome()
    if not self.btnMinimize then return end
    self.btnMinimize:setX(self.width - BTN_SIZE - PAD)
    self.btnMinimize:setY(math.max(PAD, math.floor((self:getHeaderHeight() - BTN_SIZE) / 2)))
end

function ModpackFestivalQuestPanel:saveLayout()
    if ModpackFestivalQuests and ModpackFestivalQuests.saveUiLayout then
        ModpackFestivalQuests.saveUiLayout(self:getX(), self:getY(), self.minimized)
    end
end

function ModpackFestivalQuestPanel:loadLayout()
    if not ModpackFestivalQuests or not ModpackFestivalQuests.getUiState then return end
    local ui = ModpackFestivalQuests.getUiState()
    if ui.x and ui.y then
        self:setX(ui.x)
        self:setY(ui.y)
    end
    if ui.minimized then
        self:setMinimized(true, true)
    end
end

function ModpackFestivalQuestPanel:setMinimized(min, silent)
    self.minimized = min == true
    if self.minimized then
        if not self.expandedHeight or self.expandedHeight < self:getHeaderHeight() then
            self.expandedHeight = PANEL_H_EXPANDED
        end
        self:setHeight(self:getMinimizedHeight())
        self.btnMinimize:setTitle("+")
    else
        self:recalcExpandedHeight()
        self.btnMinimize:setTitle("-")
    end
    self:layoutChrome()
    if not silent then
        self:saveLayout()
    end
end

function ModpackFestivalQuestPanel:initialise()
    ISPanel.initialise(self)

    self.btnMinimize = ISButton:new(self.width - BTN_SIZE - PAD, PAD, BTN_SIZE, BTN_SIZE, "-", self, ModpackFestivalQuestPanel.onClick)
    self.btnMinimize.internal = "MINIMIZE"
    self.btnMinimize:initialise()
    self.btnMinimize:instantiate()
    self.btnMinimize.borderColor = { r = 0.5, g = 0.4, b = 0.25, a = 0.8 }
    self.btnMinimize.backgroundColor = { r = 0.15, g = 0.12, b = 0.08, a = 0.9 }
    self.btnMinimize.backgroundColorMouseOver = { r = 0.25, g = 0.2, b = 0.12, a = 1 }
    self:addChild(self.btnMinimize)
    self:layoutChrome()
    self:loadLayout()
end

function ModpackFestivalQuestPanel:onClick(button)
    if button.internal == "MINIMIZE" then
        self:setMinimized(not self.minimized)
    end
end

function ModpackFestivalQuestPanel:onMouseUp(x, y)
    local wasMoving = self.moving
    ISPanel.onMouseUp(self, x, y)
    if self.moveWithMouse and wasMoving then
        self:saveLayout()
    end
end

function ModpackFestivalQuestPanel:onMouseUpOutside(x, y)
    local wasMoving = self.moving
    ISPanel.onMouseUpOutside(self, x, y)
    if self.moveWithMouse and wasMoving then
        self:saveLayout()
    end
end

function ModpackFestivalQuestPanel:recalcExpandedHeight()
    local h = self:getHeaderHeight() + PAD
    if self.quest then
        if questShowsDistance(self.quest) and formatDistTiles(self.distTiles) then
            h = h + self:getDistanceRowHeight() + 2
        end
        local maxWidth = self.width - PAD * 2
        local descLines = wrapTextToWidth(self.quest.description or "", maxWidth, UIFont.Small)
        h = h + #descLines * (FONT_HGT_SMALL + 2)
        if self.quest.hideTimer ~= true and self.remainingSec and self.remainingSec > 0 then
            h = h + FONT_HGT_SMALL + 2
        end
    else
        h = h + FONT_HGT_SMALL
    end
    h = h + PAD
    self.expandedHeight = math.max(PANEL_H_EXPANDED, h)
    if self.minimized then
        self:setHeight(self:getMinimizedHeight())
    else
        self:setHeight(self.expandedHeight)
    end
end

function ModpackFestivalQuestPanel:setQuest(quest, distTiles, remainingSec, navBearingRad)
    self.quest = quest
    self.distTiles = distTiles
    self.remainingSec = remainingSec
    self.navBearingRad = navBearingRad
    self:recalcExpandedHeight()
end

function ModpackFestivalQuestPanel:isGlowing()
    return self.glowUntil and getTimestampMs() < self.glowUntil
end

function ModpackFestivalQuestPanel:startGlow()
    self.glowUntil = getTimestampMs() + GLOW_DURATION_MS
    self:setAlwaysOnTop(true)
    self.alwaysOnTopGlow = true
end

function ModpackFestivalQuestPanel:stopGlow()
    self.glowUntil = nil
    if self.alwaysOnTopGlow then
        self:setAlwaysOnTop(false)
        self.alwaysOnTopGlow = false
    end
    self.borderColor = DEFAULT_BORDER
end

function ModpackFestivalQuestPanel:drawGlow()
    if not self:isGlowing() then
        self:stopGlow()
        return
    end

    local now = getTimestampMs()
    local fade = (self.glowUntil - now) / GLOW_DURATION_MS
    local pulse = 0.5 + 0.5 * math.sin(now / 90)
    local pad = 5 + math.floor(pulse * 3)
    local glowA = (0.3 + 0.5 * pulse) * math.max(0.2, fade)

    self:drawRect(-pad, -pad, self.width + pad * 2, self.height + pad * 2, glowA * 0.4, 1, 0.78, 0.18)
    self:drawRectBorder(-pad, -pad, self.width + pad * 2, self.height + pad * 2, glowA, 1, 0.9, 0.35)
    self.borderColor = {
        r = 0.45 + 0.5 * pulse,
        g = 0.35 + 0.45 * pulse,
        b = 0.15,
        a = 1,
    }
end

function ModpackFestivalQuestPanel:drawDistanceRow(y, dist)
    local rowH = self:getDistanceRowHeight()
    local boxX = PAD
    local boxY = y + math.floor((rowH - COMPASS_H) / 2)

    local cx, cy = drawCompassChrome(self, boxX, boxY, COMPASS_W, COMPASS_H)
    cy = cy + 1
    local arrowSize = COMPASS_H * 0.32

    if self.navBearingRad ~= nil then
        drawQuestNavArrow(self, cx, cy, self.navBearingRad, arrowSize, NAV_ARROW_FILL)
    else
        self:drawTextCentre("?", cx, cy - FONT_HGT_SMALL * 0.5,
            NAV_ARROW_FILL.r, NAV_ARROW_FILL.g, NAV_ARROW_FILL.b, NAV_ARROW_FILL.a, UIFont.Small)
    end

    local textX = boxX + COMPASS_W + ARROW_TEXT_GAP
    local textY = y + math.floor((rowH - FONT_HGT_DIST) / 2)
    local distNum = tostring(dist)
    self:drawText(distNum, textX, textY, NAV_DIST_COLOR.a,
        NAV_DIST_COLOR.r, NAV_DIST_COLOR.g, NAV_DIST_COLOR.b, DIST_FONT)
    local numW = getTextManager():MeasureStringX(DIST_FONT, distNum)
    local suffix = " tiles away"
    self:drawText(suffix, textX + numW, textY, NAV_DIST_COLOR.a * 0.82,
        NAV_DIST_COLOR.r * 0.88, NAV_DIST_COLOR.g * 0.88, NAV_DIST_COLOR.b * 0.88, DIST_FONT)
end

function ModpackFestivalQuestPanel:alertNewQuest(quest, player)
    local panel = ModpackFestivalQuestPanel.getOrCreate()
    if not panel then
        return
    end
    panel:setMinimized(false, true)
    panel:startGlow()
    panel:setVisible(true)

    local title = formatQuestUiText(quest and quest.title or "New objective")
    local msg = "New quest: " .. title

    local haloOk = false
    if player and HaloTextHelper and HaloTextHelper.addText then
        local color = HaloTextHelper.getColorGreen and HaloTextHelper.getColorGreen()
            or (getCore() and getCore():getGoodHighlitedColor())
        haloOk = pcall(function()
            HaloTextHelper.addText(player, msg, "[br/]", color)
        end)
        if not haloOk then
            haloOk = pcall(function()
                HaloTextHelper.addText(player, msg, color)
            end)
        end
    end
    if not haloOk and player and player.Say then
        player:Say(msg)
    end

    if player then
        local sq = player:getSquare()
        if sq then
            pcall(function()
                local emitter = getWorld():getFreeEmitter(player:getX(), player:getY(), player:getZ())
                if emitter then
                    emitter:playSound("VehicleUnlock")
                    emitter:setVolumeAll(0.9)
                end
            end)
        end
    end
end

function ModpackFestivalQuestPanel:prerender()
    self:drawGlow()

    self.backgroundColor.a = 0.72
    self:drawRect(0, 0, self.width, self.height, self.backgroundColor.a, self.backgroundColor.r, self.backgroundColor.g, self.backgroundColor.b)
    self:drawRectBorder(0, 0, self.width, self.height, self.borderColor.a, self.borderColor.r, self.borderColor.g, self.borderColor.b)

    local headerRight = self.width - BTN_SIZE - PAD * 2
    local headerLabel = "Objectives"
    if self.quest and self.quest.title then
        headerLabel = formatQuestUiText(self.quest.title)
    elseif not self.quest then
        headerLabel = "Complete"
    end
    if self:isGlowing() then
        self:drawText(headerLabel, PAD, PAD, 1, 1, 0.88, 0.35, UIFont.Small)
    else
        self:drawText(headerLabel, PAD, PAD, 1, 0.92, 0.55, 0.2, UIFont.Small)
    end
    if not self.minimized then
        self:drawText("drag to move", headerRight - 70, PAD + 1, 1, 0.55, 0.55, 0.55, UIFont.Small)
    end

    if self.minimized then
        if self:showsDistanceLine() then
            local dist = formatDistTiles(self.distTiles)
            self:drawDistanceRow(self:getHeaderHeight(), dist)
        end
        return
    end

    local y = self:getHeaderHeight()

    if self.quest then
        local dist = formatDistTiles(self.distTiles)
        if questShowsDistance(self.quest) and dist ~= nil then
            self:drawDistanceRow(y, dist)
            y = y + self:getDistanceRowHeight() + 2
        end

        local desc = formatQuestUiText(self.quest.description or "")
        local maxWidth = self.width - PAD * 2
        local lines = wrapTextToWidth(desc, maxWidth, UIFont.Small)
        for i = 1, #lines do
            self:drawText(lines[i], PAD, y, 1, 0.85, 0.85, 0.85, UIFont.Small)
            y = y + (FONT_HGT_SMALL + 2)
        end

        if self.quest.hideTimer ~= true and self.remainingSec and self.remainingSec > 0 then
            local timeLine = string.format("%d seconds left", self.remainingSec)
            self:drawText(timeLine, PAD, y, 1, 0.75, 0.9, 0.65, UIFont.Small)
        end
    else
        self:drawText("All objectives complete.", PAD, y, 1, 0.75, 1, 0.75, UIFont.Small)
    end
end

function ModpackFestivalQuestPanel:new()
    local x, y = getDefaultPanelXY()
    local o = ISPanel:new(x, y, PANEL_W, PANEL_H_EXPANDED)
    setmetatable(o, self)
    self.__index = self
    o.borderColor = DEFAULT_BORDER
    o.glowUntil = nil
    o.alwaysOnTopGlow = false
    o.backgroundColor = { r = 0.05, g = 0.05, b = 0.05, a = 0.8 }
    o.anchorLeft = false
    o.anchorTop = false
    o.anchorRight = false
    o.anchorBottom = false
    o.moveWithMouse = true
    o:setVisible(true)
    o:setAlwaysOnTop(false)
    o.quest = nil
    o.distTiles = nil
    o.navBearingRad = nil
    o.minimized = false
    o.expandedHeight = PANEL_H_EXPANDED
    return o
end

function ModpackFestivalQuestPanel.getOrCreate()
    if not ensureQuestPanelClass() then
        return nil
    end
    if ModpackFestivalQuestPanel.instance then
        return ModpackFestivalQuestPanel.instance
    end

    local panel = ModpackFestivalQuestPanel:new()
    panel:initialise()
    panel:addToUIManager()
    ModpackFestivalQuestPanel.instance = panel
    return panel
end

function ModpackFestivalQuestPanel.hide()
    if ModpackFestivalQuestPanel.instance then
        ModpackFestivalQuestPanel.instance:setVisible(false)
    end
end

function ModpackFestivalQuestPanel.show(quest, distTiles, remainingSec, navBearingRad)
    local panel = ModpackFestivalQuestPanel.getOrCreate()
    if not panel then
        return
    end
    panel:setQuest(quest, distTiles, remainingSec, navBearingRad)
    panel:setVisible(true)
end

Events.OnGameStart.Add(function()
    ensureQuestPanelClass()
end)
Events.OnCreatePlayer.Add(function()
    ensureQuestPanelClass()
end)
