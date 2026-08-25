require "ISUI/ISPanel"
require "ISUI/ISButton"
require "ISUI/ISComboBox"
require "ISUI/ISTickBox"
require "ISUI/ISTextEntryBox"
require "ISUI/ISResizeWidget"
require "QPReputation_Client"

-- QPSR_V041_RC37_BUILD42_MEASURED_TOOL_WINDOWS
-- QPSR_V041_RC35_BUILD42_UI_SPACING_AND_PLACEMENT
QPReputation.ProfileUI = ISPanel:derive("QPReputationProfileUI")
local UI = QPReputation.ProfileUI

local PATH_LABELS = {
    community = "COMMUNITY", hunter = "HUNTER", explorer = "EXPLORER",
    medic = "MEDIC", mechanic = "MECHANIC", builder = "BUILDER"
}

local PATH_COLORS = {
    community = {0.35, 0.78, 0.42}, hunter = {0.86, 0.34, 0.30},
    explorer = {0.32, 0.64, 0.88}, medic = {0.82, 0.88, 0.92},
    mechanic = {0.94, 0.64, 0.24}, builder = {0.68, 0.50, 0.30}
}

local PATH_TRANSLATION_KEYS = {
    community = "UI_QPSR_Community",
    hunter = "UI_QPSR_Hunter",
    explorer = "UI_QPSR_Explorer",
    medic = "UI_QPSR_Medic",
    mechanic = "UI_QPSR_Mechanic",
    builder = "UI_QPSR_Builder"
}

local function tr(key)
    return getText(key)
end

-- QPSR_ADMIN_UI_WORDING_FIX_V1
local function trOr(key, fallback)
    local value = getText(key)

    if value == nil
        or value == ""
        or value == key then
        return tostring(fallback or key)
    end

    return value
end

local function pathLabel(path)
    local key = PATH_TRANSLATION_KEYS[path]
    return key and tr(key) or tostring(path or "")
end

local function textWidth(font, value)
    return getTextManager():MeasureStringX(font, tostring(value or ""))
end

-- QPSR_V041_RC31_BUILD41_FONT_METRIC_REFLOW
local function fontHeight(font, fallback)
    local manager = getTextManager()

    if manager and manager.getFontHeight then
        local value = tonumber(manager:getFontHeight(font))

        if value and value > 0 then
            return value
        end
    end

    return tonumber(fallback) or 18
end

local function addResizeWidget(window)
    window.resizeWidget = ISResizeWidget:new(
        window.width - 12,
        window.height - 12,
        12,
        12,
        window
    )
    window.resizeWidget:initialise()
    window.resizeWidget:setAnchorLeft(false)
    window.resizeWidget:setAnchorRight(true)
    window.resizeWidget:setAnchorTop(false)
    window.resizeWidget:setAnchorBottom(true)
    window:addChild(window.resizeWidget)
end

local function enforceMinimumSize(window, minWidth, minHeight)
    if window.width < minWidth then
        window:setWidth(minWidth)
    end
    if window.height < minHeight then
        window:setHeight(minHeight)
    end
end

local function setControlEnabled(control, enabled)
    if not control then return end

    if control.setEnable then
        control:setEnable(enabled)
    else
        control.enable = enabled
        control.disabled = not enabled
    end
end

local function setTextEntryEditable(control, enabled)
    if not control then return end

    if control.setEditable then
        control:setEditable(enabled)
    else
        control.editable = enabled
        control.enable = enabled
        control.disabled = not enabled
    end
end

local cachedLocalClockCorrection = nil

local function localClockCorrection()
    if cachedLocalClockCorrection ~= nil then
        return cachedLocalClockCorrection
    end

    if getTimestampMs == nil then
        cachedLocalClockCorrection = 0
        return cachedLocalClockCorrection
    end

    local realEpoch = math.floor(
        (tonumber(getTimestampMs()) or 0) / 1000
    )
    local luaEpoch = tonumber(os.time()) or realEpoch
    local correction = realEpoch - luaEpoch

    -- Ignore impossible values if either clock API is unavailable
    -- or temporarily returns an invalid value.
    if math.abs(correction) > (18 * 60 * 60) then
        cachedLocalClockCorrection = 0
        return cachedLocalClockCorrection
    end

    -- Time-zone offsets are minute-based. Rounding removes the
    -- one-second render drift while preserving half-hour and
    -- 45-minute time zones.
    if correction >= 0 then
        correction = math.floor((correction + 30) / 60) * 60
    else
        correction = math.ceil((correction - 30) / 60) * 60
    end

    cachedLocalClockCorrection = correction
    return cachedLocalClockCorrection
end

local function formatTime(value)
    local stamp = tonumber(value)
    if not stamp then return "" end

    -- Project Zomboid's Lua clock may be represented as a UTC wall
    -- clock instead of a true epoch value. Comparing it with the
    -- engine timestamp provides the current local/DST correction
    -- without hardcoding a time zone.
    stamp = stamp + localClockCorrection()

    return os.date("%d/%m/%Y %H:%M", stamp)
end

local function profileTotalPoints(profile)
    local total = 0
    for _, path in ipairs(QPReputation.Paths) do
        if QPReputation.Config.Paths[path] ~= false then
            local row = profile.reputation and profile.reputation[path]
            total = total + (tonumber(row and row.points) or 0)
        end
    end
    return total
end

-- QPSR_PROFILE_HISTORY_QOL_V036
-- QPSR_HISTORY_CONTEXT_LABEL_HOTFIX_V036
-- QPSR_HISTORY_SMART_METADATA_LABEL_HOTFIX_V036
-- QPSR_AUTOMATION_REGISTRY_UI_V040
-- QPSR_COMMUNITY_AUTOMATION_UI_V041
-- QPSR_COMMUNITY_LOCAL_TEST_OVERRIDE_UI_HOTFIX_V041
-- QPSR_COMMUNITY_SUPPLY_ONLY_UI_HOTFIX_V041
-- QPSR_V041_RELEASE_CANDIDATE_CLEANUP
-- QPSR_V041_GLANCE_UI_POLISH_RC2
-- QPSR_V041_RC21_WORDING_SPACING_HOTFIX
-- QPSR_V041_RC212_COMMUNITY_METRIC_SPACING_HOTFIX
-- QPSR_V041_RC3_BUILD41_UI_REDESIGN
-- QPSR_V041_RC31_BUILD41_FONT_METRIC_REFLOW
local function profileLifetimePoints(profile)
    local total = 0

    for _, path in ipairs(QPReputation.Paths) do
        if QPReputation.Config.Paths[path] ~= false then
            local row = profile.reputation
                and profile.reputation[path]

            total = total
                + (tonumber(
                    row and row.lifetimePoints
                ) or 0)
        end
    end

    return total
end

local function strongestPath(profile)
    local bestPath = nil
    local bestPoints = -1

    for _, path in ipairs(QPReputation.Paths) do
        if QPReputation.Config.Paths[path] ~= false then
            local row = profile.reputation
                and profile.reputation[path]

            local points = tonumber(
                row and row.points
            ) or 0

            if points > bestPoints then
                bestPath = path
                bestPoints = points
            end
        end
    end

    return bestPath, math.max(0, bestPoints)
end

local function pointsRemaining(points, level)
    local nextThreshold =
        QPReputation.getNextThreshold(level)

    if not nextThreshold then
        return nil
    end

    return math.max(
        0,
        nextThreshold - (tonumber(points) or 0)
    )
end

local function historySourceKey(entry)
    local sourceType = string.lower(
        tostring(entry and entry.sourceType or "")
    )

    local reason = string.lower(
        tostring(entry and entry.reason or "")
    )

    local actor = string.lower(
        tostring(entry and entry.actor or "")
    )

    if entry and entry.automation == true
        or string.find(
            sourceType,
            "automation",
            1,
            true
        )
        or string.find(
            sourceType,
            "milestone",
            1,
            true
        ) then
        return "automation"
    end

    if string.find(
        sourceType,
        "contract",
        1,
        true
    )
        or string.find(
            reason,
            "contract completed",
            1,
            true
        ) then
        return "contract"
    end

    if string.find(
        sourceType,
        "admin",
        1,
        true
    )
        or string.find(
            reason,
            "admin adjustment",
            1,
            true
        )
        or (
            sourceType == ""
            and actor ~= ""
            and actor ~= "system"
            and actor ~= "integration"
            and actor ~= "automation"
        ) then
        return "admin"
    end

    return "other"
end

local function historySourceLabel(entry)
    local source = historySourceKey(entry)

    if source == "contract" then
        return trOr(
            "UI_QPSR_SourceContract",
            "Contract"
        )
    end

    if source == "automation" then
        return trOr(
            "UI_QPSR_SourceAutomation",
            "Automation"
        )
    end

    if source == "admin" then
        return trOr(
            "UI_QPSR_SourceAdmin",
            "Admin"
        )
    end

    return trOr(
        "UI_QPSR_SourceOther",
        "Other"
    )
end

local function historySourceColor(entry)
    local source = historySourceKey(entry)

    if source == "contract" then
        return 0.40, 0.78, 0.55
    end

    if source == "automation" then
        return 0.92, 0.66, 0.28
    end

    if source == "admin" then
        return 0.72, 0.56, 0.90
    end

    return 0.62, 0.66, 0.70
end

local function historyProgressText(entry)
    local progress = tonumber(
        entry and entry.progress
    )

    local target = tonumber(
        entry and entry.target
    )

    if progress == nil then
        return ""
    end

    local value = trOr(
        "UI_QPSR_Progress",
        "Progress"
    ) .. ": " .. tostring(progress)

    if target ~= nil then
        value = value .. " / " .. tostring(target)
    end

    return value
end

local function historyDisplayReason(entry)
    local reason = tostring(
        entry and entry.reason or ""
    )

    local sourceType = string.lower(
        tostring(entry and entry.sourceType or "")
    )

    if sourceType == "community_supply_request"
        and string.find(
            reason,
            "Automatic Community: Supply Request completed",
            1,
            true
        ) == 1
    then
        return "Automatic Community: Supply Request completed"
    end

    return reason
end

local function fitText(text, maxWidth, font)
    text = tostring(text or "")
    if maxWidth <= 0 then return "" end

    local manager = getTextManager()
    if manager:MeasureStringX(font, text) <= maxWidth then
        return text
    end

    local suffix = "..."
    local suffixWidth = manager:MeasureStringX(font, suffix)
    local low, high = 0, string.len(text)
    local best = ""

    while low <= high do
        local middle = math.floor((low + high) / 2)
        local candidate = string.sub(text, 1, middle)
        if manager:MeasureStringX(font, candidate) + suffixWidth <= maxWidth then
            best = candidate
            low = middle + 1
        else
            high = middle - 1
        end
    end

    return best .. suffix
end

local function historyHeader(entry)
    local change = tonumber(entry and entry.change) or 0
    local sign = change >= 0 and "+" or ""
    local path = tostring(
        entry and entry.path or ""
    )

    local translatedPath = path ~= ""
        and pathLabel(path)
        or path

    return sign
        .. tostring(change)
        .. " "
        .. string.upper(translatedPath)
end

QPReputation.HistoryUI = ISPanel:derive("QPReputationHistoryUI")
local HistoryUI = QPReputation.HistoryUI

function HistoryUI:initialise()
    ISPanel.initialise(self)

    self.pathFilterValues = {"all"}
    self.sourceFilterValues = {
        "all",
        "contract",
        "automation",
        "admin",
        "other"
    }

    self.pathCombo = ISComboBox:new(
        18,
        92,
        280,
        32,
        self,
        HistoryUI.onFilterChanged
    )

    self.pathCombo:initialise()
    self.pathCombo:addOption(
        trOr("UI_QPSR_AllPaths", "All paths")
    )

    for _, path in ipairs(QPReputation.Paths) do
        self.pathCombo:addOption(pathLabel(path))
        table.insert(self.pathFilterValues, path)
    end

    self.pathCombo.selected = 1
    self:addChild(self.pathCombo)

    self.sourceCombo = ISComboBox:new(
        314,
        92,
        280,
        32,
        self,
        HistoryUI.onFilterChanged
    )

    self.sourceCombo:initialise()
    self.sourceCombo:addOption(
        trOr("UI_QPSR_AllSources", "All sources")
    )

    self.sourceCombo:addOption(
        trOr("UI_QPSR_SourceContract", "Contract")
    )

    self.sourceCombo:addOption(
        trOr(
            "UI_QPSR_SourceAutomation",
            "Automation"
        )
    )

    self.sourceCombo:addOption(
        trOr("UI_QPSR_SourceAdmin", "Admin")
    )

    self.sourceCombo:addOption(
        trOr("UI_QPSR_SourceOther", "Other")
    )

    self.sourceCombo.selected = 1
    self:addChild(self.sourceCombo)

    self.closeButton = ISButton:new(
        self.width - 132,
        self.height - 46,
        114,
        34,
        tr("UI_QPSR_Close"),
        self,
        HistoryUI.onClose
    )

    self.closeButton:initialise()
    self.closeButton.font = UIFont.Medium
    self.pathCombo.font = UIFont.Medium
    self.sourceCombo.font = UIFont.Medium
    self:addChild(self.closeButton)

    addResizeWidget(self)
end

function HistoryUI:onClose()
    self:setVisible(false)
    self:removeFromUIManager()
end

function HistoryUI:onFilterChanged()
    self.scrollY = 0
end

function HistoryUI:getFilteredHistory()
    local profile = QPReputation.Client.profile
    local history = profile and profile.history or {}
    local pathIndex = tonumber(
        self.pathCombo and self.pathCombo.selected
    ) or 1

    local sourceIndex = tonumber(
        self.sourceCombo and self.sourceCombo.selected
    ) or 1

    local pathFilter =
        self.pathFilterValues[pathIndex]
        or "all"

    local sourceFilter =
        self.sourceFilterValues[sourceIndex]
        or "all"

    local filtered = {}

    for _, entry in ipairs(history) do
        local pathMatches =
            pathFilter == "all"
            or tostring(entry.path or "")
                == pathFilter

        local sourceMatches =
            sourceFilter == "all"
            or historySourceKey(entry)
                == sourceFilter

        if pathMatches and sourceMatches then
            table.insert(filtered, entry)
        end
    end

    return filtered, #history
end

local function QPSR_B42_historyRowMetrics()
    local smallLineH = fontHeight(UIFont.Small, 14)
    local rowHeight = math.max(
        58,
        (smallLineH * 3) + 14
    )

    return rowHeight, rowHeight + 6
end

function HistoryUI:onMouseWheel(del)
    local history = self:getFilteredHistory()
    local count = #history
    local smallLineH = fontHeight(UIFont.Small, 14)
    local mediumLineH = fontHeight(UIFont.Medium, 18)
    local largeLineH = fontHeight(UIFont.Large, 24)
    local controlH = math.max(28, mediumLineH + 8)
    local headerH = math.max(42, largeLineH + 16)
    local filterLabelY = headerH + 6
    local filterY = filterLabelY + smallLineH + 3
    local viewportY = filterY + controlH + 8
    local footerH = math.max(42, smallLineH + 20)
    local visibleHeight = math.max(
        110,
        self.height - viewportY - footerH
    )

    local _, rowStep = QPSR_B42_historyRowMetrics()
    local contentHeight = count * rowStep
    local maxScroll = math.max(
        0,
        contentHeight - visibleHeight
    )

    self.scrollY = math.max(
        0,
        math.min(
            maxScroll,
            (self.scrollY or 0) - (del * rowStep)
        )
    )

    return true
end

function HistoryUI:prerender()
    -- QPSR_V041_RC37_BUILD42_HISTORY_MEASURED_REFLOW
    enforceMinimumSize(self, 620, 420)

    local smallLineH = fontHeight(UIFont.Small, 14)
    local mediumLineH = fontHeight(UIFont.Medium, 18)
    local largeLineH = fontHeight(UIFont.Large, 24)
    local controlH = math.max(28, mediumLineH + 8)
    local margin = 12
    local gap = 10
    local headerH = math.max(42, largeLineH + 16)
    local filterLabelY = headerH + 6
    local filterY = filterLabelY + smallLineH + 3
    local viewportY = filterY + controlH + 8
    local footerH = math.max(42, smallLineH + 20)

    local filtersW = self.width - (margin * 2)
    local comboW = math.floor(
        (filtersW - gap) / 2
    )
    local sourceX = margin + comboW + gap

    self.pathCombo:setX(margin)
    self.pathCombo:setY(filterY)
    self.pathCombo:setWidth(comboW)
    self.pathCombo:setHeight(controlH)

    self.sourceCombo:setX(sourceX)
    self.sourceCombo:setY(filterY)
    self.sourceCombo:setWidth(comboW)
    self.sourceCombo:setHeight(controlH)

    local closeWidth = math.max(
        90,
        textWidth(
            UIFont.Small,
            tr("UI_QPSR_Close")
        ) + 28
    )

    self.closeButton:setWidth(closeWidth)
    self.closeButton:setHeight(math.max(28, mediumLineH + 8))
    self.closeButton:setX(
        self.width - closeWidth - margin
    )
    self.closeButton:setY(
        self.height - self.closeButton.height - 9
    )

    ISPanel.prerender(self)

    self:drawRect(
        0, 0, self.width, headerH,
        0.94, 0.07, 0.07, 0.07
    )

    self:drawText(
        tr("UI_QPSR_HistoryTitle"),
        14,
        math.max(8, math.floor((headerH - largeLineH) / 2)),
        1, 1, 1, 1,
        UIFont.Large
    )

    self:drawText(
        trOr("UI_QPSR_HistoryPathFilter", "Path"),
        margin,
        filterLabelY,
        0.68, 0.72, 0.76, 1,
        UIFont.Small
    )

    self:drawText(
        trOr("UI_QPSR_HistorySourceFilter", "Source"),
        sourceX,
        filterLabelY,
        0.68, 0.72, 0.76, 1,
        UIFont.Small
    )

    local history, totalCount =
        self:getFilteredHistory()

    local viewportX = margin
    local viewportW = self.width - (margin * 2)
    local viewportH = math.max(
        110,
        self.height - viewportY - footerH
    )

    self:drawRect(
        viewportX,
        viewportY,
        viewportW,
        viewportH,
        0.36,
        0.04, 0.04, 0.04
    )

    self:drawRectBorder(
        viewportX,
        viewportY,
        viewportW,
        viewportH,
        0.55,
        0.32, 0.32, 0.32
    )

    self:setStencilRect(
        viewportX,
        viewportY,
        viewportW,
        viewportH
    )

    local rowHeight, rowStep =
        QPSR_B42_historyRowMetrics()

    if #history == 0 then
        local emptyText = totalCount > 0
            and trOr(
                "UI_QPSR_NoMatchingActivity",
                "No activity matches the selected filters."
            )
            or tr("UI_QPSR_NoActivity")

        self:drawText(
            emptyText,
            viewportX + 12,
            viewportY + 12,
            0.68, 0.68, 0.68, 1,
            UIFont.Small
        )
    else
        local y = viewportY + 6 - (self.scrollY or 0)

        for _, entry in ipairs(history) do
            if y + rowHeight >= viewportY
                and y <= viewportY + viewportH then
                local color =
                    PATH_COLORS[
                        tostring(entry.path or "")
                    ]
                    or {0.72, 0.72, 0.72}

                self:drawRect(
                    viewportX + 6,
                    y,
                    viewportW - 12,
                    rowHeight,
                    0.42,
                    0.10, 0.10, 0.10
                )

                self:drawRectBorder(
                    viewportX + 6,
                    y,
                    viewportW - 12,
                    rowHeight,
                    0.40,
                    0.30, 0.30, 0.30
                )

                local innerX = viewportX + 14
                local innerRight = viewportX + viewportW - 14
                local line1Y = y + 5
                local line2Y = line1Y + smallLineH + 2
                local line3Y = line2Y + smallLineH + 2
                local date = formatTime(entry.time)
                local dateWidth = textWidth(
                    UIFont.Small,
                    date
                )

                self:drawText(
                    fitText(
                        historyHeader(entry),
                        math.max(
                            90,
                            viewportW - dateWidth - 44
                        ),
                        UIFont.Small
                    ),
                    innerX,
                    line1Y,
                    color[1],
                    color[2],
                    color[3],
                    1,
                    UIFont.Small
                )

                self:drawTextRight(
                    date,
                    innerRight,
                    line1Y,
                    0.62, 0.66, 0.70, 1,
                    UIFont.Small
                )

                local sourceLabel =
                    "[" .. historySourceLabel(entry) .. "]"
                local sr, sg, sb =
                    historySourceColor(entry)
                local sourceWidth =
                    textWidth(UIFont.Small, sourceLabel) + 9

                self:drawText(
                    sourceLabel,
                    innerX,
                    line2Y,
                    sr, sg, sb, 1,
                    UIFont.Small
                )

                self:drawText(
                    fitText(
                        historyDisplayReason(entry),
                        math.max(
                            80,
                            viewportW - sourceWidth - 40
                        ),
                        UIFont.Small
                    ),
                    innerX + sourceWidth,
                    line2Y,
                    0.82, 0.82, 0.82, 1,
                    UIFont.Small
                )

                local metadata = {}
                local actor = tostring(entry.actor or "")

                if actor ~= "" then
                    local actorLabelKey = "UI_QPSR_By"
                    local actorLabelFallback = "By"
                    local sourceKey = historySourceKey(entry)
                    local normalizedActor = string.lower(actor)
                    local isContractContext = false

                    if sourceKey == "contract" then
                        local knownContexts = {
                            ["delivery"] = true,
                            ["zombie hunt"] = true,
                            ["location"] = true,
                            ["reach location"] = true,
                            ["manual"] = true,
                            ["custom"] = true
                        }

                        isContractContext =
                            knownContexts[normalizedActor] == true
                            or string.find(
                                normalizedActor,
                                "multi-objective ",
                                1,
                                true
                            ) == 1
                            or string.find(
                                normalizedActor,
                                "multi objective ",
                                1,
                                true
                            ) == 1
                    end

                    if isContractContext then
                        actorLabelKey = "UI_QPSR_Context"
                        actorLabelFallback = "Context"
                    end

                    table.insert(
                        metadata,
                        trOr(actorLabelKey, actorLabelFallback)
                            .. ": "
                            .. actor
                    )
                end

                local progressText =
                    historyProgressText(entry)

                if progressText ~= "" then
                    table.insert(metadata, progressText)
                end

                if #metadata > 0 then
                    self:drawText(
                        fitText(
                            table.concat(metadata, "   |   "),
                            viewportW - 34,
                            UIFont.Small
                        ),
                        innerX,
                        line3Y,
                        0.58, 0.62, 0.66, 1,
                        UIFont.Small
                    )
                end
            end

            y = y + rowStep
        end
    end

    self:clearStencilRect()

    local footerTextY =
        self.height - smallLineH - 13

    if #history > 0 then
        self:drawText(
            tr("UI_QPSR_MouseWheel"),
            margin + 2,
            footerTextY,
            0.55, 0.58, 0.62, 1,
            UIFont.Small
        )
    end

    self:drawTextRight(
        tostring(#history)
            .. " / "
            .. tostring(totalCount),
        self.closeButton.x - 12,
        footerTextY,
        0.52, 0.56, 0.60, 1,
        UIFont.Small
    )
end

function HistoryUI:new(x, y, width, height)
    local o = ISPanel.new(
        self,
        x,
        y,
        width,
        height
    )

    o.backgroundColor = {
        r=0.025,
        g=0.025,
        b=0.025,
        a=0.97
    }

    o.borderColor = {
        r=0.45,
        g=0.45,
        b=0.45,
        a=1
    }

    o.moveWithMouse = true
    o.scrollY = 0
    return o
end

-- QPSR_V041_RC37_BUILD42_NATURAL_TOOL_WINDOWS
local function QPSR_B42_windowRect(kind)
    local screenW = getCore():getScreenWidth()
    local screenH = getCore():getScreenHeight()
    local gutter = 12
    local gap = 12

    if screenW >= 1700 and screenH >= 1100 then
        local usableW = screenW - (gutter * 2) - gap
        local usableH = screenH - (gutter * 2) - gap

        local leftW = math.floor(usableW * 0.59)
        local rightW = usableW - leftW
        local profileH = math.floor(usableH * 0.64)
        local historyH = usableH - profileH
        local rightX = gutter + leftW + gap

        local adminH = 380
        local settingsH = 680
        local settingsY = gutter + adminH + gap

        if settingsY + settingsH > screenH - gutter then
            settingsH = screenH - gutter - settingsY
        end

        if kind == "profile" then
            return gutter, gutter, leftW, profileH
        elseif kind == "history" then
            return gutter,
                gutter + profileH + gap,
                leftW,
                historyH
        elseif kind == "admin" then
            return rightX, gutter, rightW, adminH
        elseif kind == "settings" then
            return rightX,
                settingsY,
                rightW,
                math.max(620, settingsH)
        end
    end

    local width
    local height

    if kind == "profile" then
        width = math.min(1120, math.max(880, screenW - 70))
        height = math.min(820, math.max(640, screenH - 70))
    elseif kind == "history" then
        width = math.min(920, math.max(620, screenW - 140))
        height = math.min(640, math.max(420, screenH - 160))
    elseif kind == "admin" then
        width = math.min(700, math.max(560, screenW - 180))
        height = 380
    else
        width = math.min(900, math.max(760, screenW - 120))
        height = math.min(680, math.max(640, screenH - 100))
    end

    local x = math.max(
        gutter,
        math.floor((screenW - width) / 2)
    )

    local y = math.max(
        gutter,
        math.floor((screenH - height) / 2)
    )

    if kind == "history" then
        x = math.max(gutter, x - 24)
        y = math.min(screenH - height - gutter, y + 24)
    elseif kind == "admin" then
        x = math.min(screenW - width - gutter, x + 36)
        y = math.max(gutter, y - 36)
    elseif kind == "settings" then
        x = math.min(screenW - width - gutter, x + 48)
        y = math.min(screenH - height - gutter, y + 48)
    end

    return x, y, width, height
end

function QPReputation.openHistory()
    local x, y, width, height =
        QPSR_B42_windowRect("history")

    local ui = HistoryUI:new(
        x,
        y,
        width,
        height
    )

    ui:initialise()
    ui:addToUIManager()
    ui:setVisible(true)
end

function UI:initialise()
    ISPanel.initialise(self)

    self.historyButton = ISButton:new(18, self.height - 48, 154, 34, tr("UI_QPSR_ViewAllHistory"), self, UI.onHistory)
    self.historyButton:initialise()
    self.historyButton.font = UIFont.Medium
    self:addChild(self.historyButton)

    self.closeButton = ISButton:new(self.width - 132, self.height - 48, 114, 34, tr("UI_QPSR_Close"), self, UI.onClose)
    self.closeButton:initialise()
    self.closeButton.font = UIFont.Medium
    self:addChild(self.closeButton)

    local player = getPlayer()
    local access = string.lower(
        tostring(
            player and player:getAccessLevel() or ""
        )
    )

    if access == "admin" or access == "moderator" or access == "overseer" then
        self.adminButton = ISButton:new(182, self.height - 48, 148, 34, tr("UI_QPSR_AdminEditor"), self, UI.onAdmin)
        self.adminButton:initialise()
        self.adminButton.font = UIFont.Medium
        self:addChild(self.adminButton)
    end

    addResizeWidget(self)
end

function UI:onClose()
    QPReputation.Client.removeProfileListener(self)
    self:setVisible(false)
    self:removeFromUIManager()
end

function UI:onAdmin()
    QPReputation.openAdminEditor()
end

function UI:onHistory()
    QPReputation.openHistory()
end

function UI:prerender()
    -- QPSR_V041_RC35_BUILD42_RESPONSIVE_DESKTOP
    -- Build 42 uses measured two-line cards and a three-row overview.
    -- The window is designed for the docked desktop layout.
    enforceMinimumSize(self, 880, 640)

    local mediumLineH = math.max(
        18,
        fontHeight(UIFont.Medium, 18)
    )
    local smallLineH = math.max(
        14,
        fontHeight(UIFont.Small, 14)
    )
    local mediumStep = mediumLineH + 3
    local smallStep = smallLineH + 2

    local margin = 12
    local footerH = 38
    local footerY = self.height - footerH
    local contentW = self.width - (margin * 2)

    local historyWidth = math.max(
        122,
        textWidth(
            UIFont.Medium,
            tr("UI_QPSR_ViewAllHistory")
        ) + 24
    )

    self.historyButton:setWidth(historyWidth)
    self.historyButton:setX(margin)
    self.historyButton:setY(footerY + 2)

    local nextFooterX =
        self.historyButton.x
        + self.historyButton.width
        + 7

    if self.adminButton then
        local adminWidth = math.max(
            116,
            textWidth(
                UIFont.Medium,
                tr("UI_QPSR_AdminEditor")
            ) + 24
        )

        self.adminButton:setWidth(adminWidth)
        self.adminButton:setX(nextFooterX)
        self.adminButton:setY(footerY + 2)
    end

    local closeWidth = math.max(
        84,
        textWidth(
            UIFont.Medium,
            tr("UI_QPSR_Close")
        ) + 26
    )

    self.closeButton:setWidth(closeWidth)
    self.closeButton:setX(
        self.width - margin - closeWidth
    )
    self.closeButton:setY(footerY + 2)

    ISPanel.prerender(self)

    local function drawCard(x, y, width, height, alpha)
        self:drawRect(
            x,
            y,
            width,
            height,
            alpha or 0.38,
            0.055, 0.055, 0.055
        )

        self:drawRectBorder(
            x,
            y,
            width,
            height,
            0.52,
            0.30, 0.30, 0.30
        )
    end

    local function drawCompactStatusBadge(
        xRight,
        y,
        enabled
    )
        local label = enabled
            and trOr(
                "UI_QPSR_AutomationEnabled",
                "Enabled"
            )
            or trOr(
                "UI_QPSR_AutomationDisabled",
                "Disabled"
            )

        local width = math.max(
            64,
            textWidth(
                UIFont.Small,
                label
            ) + 16
        )

        local x = xRight - width
        local badgeH = smallLineH + 5

        self:drawRect(
            x,
            y,
            width,
            badgeH,
            0.66,
            enabled and 0.12 or 0.18,
            enabled and 0.28 or 0.18,
            enabled and 0.14 or 0.18
        )

        self:drawRectBorder(
            x,
            y,
            width,
            badgeH,
            0.70,
            enabled and 0.34 or 0.42,
            enabled and 0.66 or 0.42,
            enabled and 0.38 or 0.42
        )

        self:drawText(
            label,
            x + 7,
            y + 2,
            enabled and 0.54 or 0.66,
            enabled and 0.86 or 0.66,
            enabled and 0.58 or 0.66,
            1,
            UIFont.Small
        )
    end

    local function drawInlineMetric(
        x,
        y,
        width,
        label,
        value,
        r,
        g,
        b
    )
        local text =
            tostring(label or "")
            .. ": "
            .. tostring(value or "")

        self:drawText(
            fitText(
                text,
                width,
                UIFont.Small
            ),
            x,
            y,
            r or 0.72,
            g or 0.76,
            b or 0.80,
            1,
            UIFont.Small
        )
    end

    local headerH = 34

    self:drawRect(
        0,
        0,
        self.width,
        headerH,
        0.94,
        0.07, 0.07, 0.07
    )

    self:drawText(
        tr("UI_QPSR_ProfileTitle"),
        12,
        7,
        1, 1, 1, 1,
        UIFont.Medium
    )

    local profile = QPReputation.Client.profile

    if not profile then
        self:drawText(
            tr("UI_QPSR_LoadingProfile"),
            margin,
            headerH + 10,
            1, 1, 1, 1,
            UIFont.Medium
        )
        return
    end

    local totalPoints = profileTotalPoints(profile)
    local lifetimePoints =
        profileLifetimePoints(profile)

    local overall = QPReputation.getLevel(totalPoints)
    local nextOverall =
        QPReputation.getNextThreshold(overall)

    local overallRemaining =
        pointsRemaining(totalPoints, overall)

    local bestPath, bestPathPoints =
        strongestPath(profile)

    local summaryX = margin
    local summaryY = headerH + 7
    local summaryW = contentW
    local summaryTitleH = smallLineH
    local summaryValueH = mediumLineH
    local summaryDetailH = smallLineH
    local summaryRowGap = 1
    local summaryPaddingTop = 6
    local summaryPaddingBottom = 6

    local summaryH = math.max(
        64,
        summaryPaddingTop
            + summaryTitleH
            + summaryRowGap
            + summaryValueH
            + summaryRowGap
            + summaryDetailH
            + summaryPaddingBottom
    )

    drawCard(
        summaryX,
        summaryY,
        summaryW,
        summaryH,
        0.42
    )

    local identityW = math.max(
        290,
        math.floor(summaryW * 0.35)
    )

    local identityRight =
        summaryX + identityW

    self:drawRect(
        identityRight,
        summaryY + 7,
        1,
        summaryH - 14,
        0.56,
        0.32, 0.32, 0.32
    )

    local username = tostring(
        profile.username
        or tr("UI_QPSR_UnknownSurvivor")
    )

    local identitySecondLine =
        tr("UI_QPSR_Overall")
        .. ": "
        .. tr("UI_QPSR_Level")
        .. " "
        .. tostring(overall)
        .. " - "
        .. QPReputation.getTitle(overall)
        .. "   |   "
        .. trOr(
            "UI_QPSR_StrongestPath",
            "Strongest path"
        )
        .. ": "
        .. string.upper(
            bestPath
            and pathLabel(bestPath)
            or "-"
        )
        .. " ("
        .. tostring(bestPathPoints)
        .. ")"

    self:drawText(
        fitText(
            username,
            identityW - 18,
            UIFont.Medium
        ),
        summaryX + 9,
        summaryY + 7,
        1, 1, 1, 1,
        UIFont.Medium
    )

    self:drawText(
        fitText(
            identitySecondLine,
            identityW - 18,
            UIFont.Small
        ),
        summaryX + 9,
        summaryY + 8 + mediumLineH,
        0.68, 0.76, 0.82, 1,
        UIFont.Small
    )

    local metricAreaX = identityRight + 10
    local metricAreaW =
        summaryW - identityW - 18
    local metricGap = 8
    local metricW = math.floor(
        (metricAreaW - (metricGap * 2)) / 3
    )

    local summaryTitleY =
        summaryY + summaryPaddingTop

    local summaryValueY =
        summaryTitleY
        + summaryTitleH
        + summaryRowGap

    local summaryDetailY =
        summaryValueY
        + summaryValueH
        + summaryRowGap

    local function drawSummaryMetric(
        x,
        label,
        value,
        detail,
        r,
        g,
        b
    )
        self:drawText(
            fitText(
                label,
                metricW,
                UIFont.Small
            ),
            x,
            summaryTitleY,
            0.55, 0.62, 0.68, 1,
            UIFont.Small
        )

        self:drawText(
            fitText(
                value,
                metricW,
                UIFont.Medium
            ),
            x,
            summaryValueY,
            r or 0.90,
            g or 0.90,
            b or 0.90,
            1,
            UIFont.Medium
        )

        if detail and detail ~= "" then
            self:drawText(
                fitText(
                    detail,
                    metricW,
                    UIFont.Small
                ),
                x,
                summaryDetailY,
                0.62, 0.68, 0.74, 1,
                UIFont.Small
            )
        end
    end

    drawSummaryMetric(
        metricAreaX,
        tr("UI_QPSR_TotalPoints"),
        tostring(totalPoints),
        tr("UI_QPSR_Overall"),
        0.88, 0.92, 0.96
    )

    drawSummaryMetric(
        metricAreaX + metricW + metricGap,
        trOr(
            "UI_QPSR_LifetimePoints",
            "Lifetime points"
        ),
        tostring(lifetimePoints),
        trOr(
            "UI_QPSR_AllPaths",
            "All paths"
        ),
        0.68, 0.80, 0.90
    )

    local nextDetail = nextOverall
        and (
            trOr(
                "UI_QPSR_PointsRemaining",
                "Points remaining"
            )
            .. ": "
            .. tostring(overallRemaining or 0)
        )
        or ""

    drawSummaryMetric(
        metricAreaX
            + (metricW * 2)
            + (metricGap * 2),
        tr("UI_QPSR_NextOverallRank"),
        nextOverall
            and tostring(nextOverall)
            or tr("UI_QPSR_Max"),
        nextDetail,
        0.88, 0.82, 0.56
    )

    local pathTop = summaryY + summaryH + 7
    local pathGap = 6
    local pathCardW = math.floor(
        (contentW - pathGap) / 2
    )
    local pathCardH = math.max(
        46,
        (mediumLineH * 2) + 12
    )
    local pathRowGap = 5

    for index, path in ipairs(QPReputation.Paths) do
        local column = (index - 1) % 2
        local rowIndex = math.floor(
            (index - 1) / 2
        )

        local cardX =
            margin
            + column * (pathCardW + pathGap)

        local cardY =
            pathTop
            + rowIndex
                * (pathCardH + pathRowGap)

        local row = profile.reputation
            and profile.reputation[path]
            or {
                points = 0,
                lifetimePoints = 0
            }

        local points = tonumber(row.points) or 0
        local lifetime = tonumber(
            row.lifetimePoints
        ) or 0

        local level =
            QPReputation.getLevel(points)

        local nextThreshold =
            QPReputation.getNextThreshold(level)

        local remaining =
            pointsRemaining(points, level)

        local currentThreshold =
            QPReputation.Config.Thresholds[
                level + 1
            ] or 0

        local progress = 1

        if nextThreshold then
            local span = math.max(
                1,
                nextThreshold - currentThreshold
            )

            progress = math.max(
                0,
                math.min(
                    1,
                    (points - currentThreshold) / span
                )
            )
        end

        local color =
            PATH_COLORS[path]
            or {0.7, 0.7, 0.7}

        local pointText = nextThreshold
            and (
                tostring(points)
                .. " / "
                .. tostring(nextThreshold)
            )
            or (
                tostring(points)
                .. " / "
                .. tr("UI_QPSR_Max")
            )

        local titleText =
            string.upper(pathLabel(path))
            .. "   |   "
            .. tr("UI_QPSR_Level")
            .. " "
            .. tostring(level)
            .. " - "
            .. QPReputation.getTitle(level)

        local detailLeft =
            trOr(
                "UI_QPSR_LifetimePoints",
                "Lifetime points"
            )
            .. ": "
            .. tostring(lifetime)

        local detailRight = remaining
            and (
                trOr(
                    "UI_QPSR_PointsRemaining",
                    "Points remaining"
                )
                .. ": "
                .. tostring(remaining)
            )
            or tr("UI_QPSR_Max")

        drawCard(
            cardX,
            cardY,
            pathCardW,
            pathCardH,
            0.36
        )

        self:drawRect(
            cardX,
            cardY,
            3,
            pathCardH,
            0.92,
            color[1],
            color[2],
            color[3]
        )

        self:drawText(
            fitText(
                titleText,
                pathCardW - 116,
                UIFont.Medium
            ),
            cardX + 8,
            cardY + 5,
            color[1],
            color[2],
            color[3],
            1,
            UIFont.Medium
        )

        self:drawTextRight(
            pointText,
            cardX + pathCardW - 7,
            cardY + 5,
            0.90, 0.90, 0.90, 1,
            UIFont.Medium
        )

        self:drawText(
            fitText(
                detailLeft,
                math.floor(pathCardW * 0.54),
                UIFont.Small
            ),
            cardX + 8,
            cardY + 6 + mediumLineH,
            0.58, 0.66, 0.72, 1,
            UIFont.Small
        )

        self:drawTextRight(
            fitText(
                detailRight,
                math.floor(pathCardW * 0.42),
                UIFont.Small
            ),
            cardX + pathCardW - 7,
            cardY + 6 + mediumLineH,
            0.58, 0.66, 0.72, 1,
            UIFont.Small
        )

        local barX = cardX + 8
        local barH = 4
        local barY =
            cardY + pathCardH - barH - 3
        local barW = pathCardW - 16

        self:drawRect(
            barX,
            barY,
            barW,
            barH,
            0.78,
            0.035, 0.035, 0.035
        )

        self:drawRect(
            barX,
            barY,
            math.floor(barW * progress),
            barH,
            0.94,
            color[1],
            color[2],
            color[3]
        )
    end

    local pathsBottom =
        pathTop
        + (pathCardH * 3)
        + (pathRowGap * 2)

    local automationTitleY =
        pathsBottom + 6

    self:drawText(
        getText("UI_QPSR_AutomationStatus"),
        margin,
        automationTitleY,
        1, 1, 1, 1,
        UIFont.Medium
    )

    local automationY =
        automationTitleY + mediumLineH + 3

    local automationGap = 6
    local automationW = math.floor(
        (contentW - automationGap) / 2
    )

    local automationH = math.max(
        82,
        mediumLineH
            + (smallLineH * 4)
            + 18
    )

    local serverSettings =
        QPReputation.Client.automationSettings
        or {}

    local settingsPaths =
        serverSettings.paths
        or {}

    local hunterSettings =
        settingsPaths.hunter
        or {}

    local communitySettings =
        settingsPaths.community
        or {}

    local masterEnabled =
        serverSettings.enabled

    if masterEnabled == nil then
        masterEnabled =
            QPReputation.Config.Automation
            and QPReputation.Config.Automation.Enabled == true
    end

    local hunterFlag =
        serverSettings.hunterEnabled

    if hunterFlag == nil then
        hunterFlag = hunterSettings.enabled
    end

    if hunterFlag == nil then
        hunterFlag =
            QPReputation.Config.Automation
            and QPReputation.Config.Automation.Hunter
            and QPReputation.Config.Automation.Hunter.Enabled == true
    end

    local communityFlag =
        serverSettings.communityEnabled

    if communityFlag == nil then
        communityFlag = communitySettings.enabled
    end

    if communityFlag == nil then
        communityFlag =
            QPReputation.Config.Automation
            and QPReputation.Config.Automation.Community
            and QPReputation.Config.Automation.Community.Enabled == true
    end

    local hunterEnabled =
        masterEnabled == true
        and hunterFlag == true

    local communityEnabled =
        masterEnabled == true
        and communityFlag == true

    local hunterAutomation =
        profile.automation
        and profile.automation.hunter
        or nil

    local communityAutomation =
        profile.automation
        and profile.automation.community
        or nil

    local hunterCardX = margin
    local communityCardX =
        margin + automationW + automationGap

    drawCard(
        hunterCardX,
        automationY,
        automationW,
        automationH,
        0.38
    )

    drawCard(
        communityCardX,
        automationY,
        automationW,
        automationH,
        0.38
    )

    self:drawText(
        trOr(
            "UI_QPSR_HunterAutomation",
            "Hunter Automation"
        ),
        hunterCardX + 8,
        automationY + 5,
        0.92, 0.92, 0.92, 1,
        UIFont.Medium
    )

    drawCompactStatusBadge(
        hunterCardX + automationW - 7,
        automationY + 4,
        hunterEnabled
    )

    local preset = tonumber(
        serverSettings.preset
        or hunterSettings.preset
    )

    if preset == nil then
        preset =
            QPReputation.Config.Automation
            and QPReputation.Config.Automation.Hunter
            and QPReputation.Config.Automation.Hunter
                .UseTestMilestones == true
            and 2
            or 1
    end

    local presetText = preset == 2
        and trOr(
            "UI_QPSR_AutomationPresetFastTesting",
            "Fast testing milestones"
        )
        or trOr(
            "UI_QPSR_AutomationPresetProduction",
            "Production milestones"
        )

    self:drawText(
        fitText(
            presetText,
            automationW - 16,
            UIFont.Small
        ),
        hunterCardX + 8,
        automationY + 6 + mediumLineH,
        preset == 2 and 0.90 or 0.56,
        preset == 2 and 0.66 or 0.78,
        preset == 2 and 0.28 or 0.60,
        1,
        UIFont.Small
    )

    local tracked = tonumber(
        hunterAutomation
        and hunterAutomation.trackedKills
    ) or 0

    local hunterDaily = tonumber(
        hunterAutomation
        and hunterAutomation.daily
        and hunterAutomation.daily.points
    ) or 0

    local nextMilestoneText

    if hunterAutomation
        and hunterAutomation.nextMilestone
    then
        nextMilestoneText =
            tostring(
                hunterAutomation.nextMilestone
            )
            .. " (+"
            .. tostring(
                hunterAutomation.nextReward or 0
            )
            .. ")"
    else
        nextMilestoneText =
            tr("UI_QPSR_AllMilestones")
    end

    local scanInterval =
        tonumber(serverSettings.scanInterval)
        or tonumber(
            QPReputation.Config.Automation
                .ScanEveryMinutes
        )
        or 1

    local hunterCap =
        tonumber(
            serverSettings.dailyCap
            or hunterSettings.dailyCap
        )
        or 0

    local maxJump =
        tonumber(
            serverSettings.maxKillsPerScan
            or hunterSettings.maxProgressPerScan
        )
        or 50

    local hunterMetricY =
        automationY + 8 + mediumLineH + smallStep

    local metricGap = 5
    local metricW = math.floor(
        (automationW - 16 - (metricGap * 2)) / 3
    )

    drawInlineMetric(
        hunterCardX + 8,
        hunterMetricY,
        metricW,
        tr("UI_QPSR_TrackedKills"),
        tracked,
        0.72, 0.78, 0.82
    )

    drawInlineMetric(
        hunterCardX + 8 + metricW + metricGap,
        hunterMetricY,
        metricW,
        tr("UI_QPSR_NextMilestone"),
        nextMilestoneText,
        0.88, 0.82, 0.56
    )

    drawInlineMetric(
        hunterCardX
            + 8
            + (metricW * 2)
            + (metricGap * 2),
        hunterMetricY,
        metricW,
        tr("UI_QPSR_DailyAutomation"),
        hunterDaily,
        0.66, 0.80, 0.88
    )

    local hunterFooter =
        tr("UI_QPSR_AutomationScan")
        .. ": "
        .. tostring(scanInterval)
        .. "m   |   "
        .. tr("UI_QPSR_AutomationDailyCap")
        .. ": "
        .. tostring(hunterCap)
        .. "   |   "
        .. tr("UI_QPSR_AutomationMaxJump")
        .. ": "
        .. tostring(maxJump)

    self:drawText(
        fitText(
            hunterFooter,
            automationW - 16,
            UIFont.Small
        ),
        hunterCardX + 8,
        automationY + automationH - smallLineH - 5,
        0.56, 0.62, 0.68, 1,
        UIFont.Small
    )

    self:drawText(
        trOr(
            "UI_QPSR_CommunityAutomation",
            "Community Automation"
        ),
        communityCardX + 8,
        automationY + 5,
        0.92, 0.92, 0.92, 1,
        UIFont.Medium
    )

    drawCompactStatusBadge(
        communityCardX + automationW - 7,
        automationY + 4,
        communityEnabled
    )

    local supplyEnabled =
        serverSettings.supplyRequestsEnabled

    if supplyEnabled == nil then
        supplyEnabled =
            communitySettings.supplyRequestsEnabled
    end

    if supplyEnabled == nil then
        supplyEnabled =
            QPReputation.Config.Automation
            and QPReputation.Config.Automation.Community
            and QPReputation.Config.Automation.Community
                .SupplyRequestsEnabled ~= false
    end

    local supplyPoints = tonumber(
        serverSettings.supplyRequestPoints
        or communitySettings.supplyRequestPoints
    )

    if supplyPoints == nil then
        supplyPoints =
            tonumber(
                QPReputation.Config.Automation
                and QPReputation.Config.Automation.Community
                and QPReputation.Config.Automation.Community
                    .SupplyRequestPoints
            )
            or 5
    end

    local communityCap = tonumber(
        serverSettings.communityDailyCap
        or communitySettings.dailyCap
    )

    if communityCap == nil then
        communityCap =
            tonumber(
                QPReputation.Config.Automation
                and QPReputation.Config.Automation.Community
                and QPReputation.Config.Automation.Community
                    .DailyPointCap
            )
            or 25
    end

    local communityDaily = tonumber(
        communityAutomation
        and communityAutomation.daily
        and communityAutomation.daily.points
    ) or 0

    self:drawText(
        fitText(
            trOr(
                "UI_QPSR_CommunitySupplyRequests",
                "Supply Request completion points"
            ),
            automationW - 16,
            UIFont.Small
        ),
        communityCardX + 8,
        automationY + 6 + mediumLineH,
        supplyEnabled and 0.52 or 0.66,
        supplyEnabled and 0.80 or 0.66,
        supplyEnabled and 0.58 or 0.66,
        1,
        UIFont.Small
    )

    local communityMetricY =
        automationY + 8 + mediumLineH + smallStep

    drawInlineMetric(
        communityCardX + 8,
        communityMetricY,
        metricW,
        trOr(
            "UI_QPSR_Points",
            "Points"
        ),
        "+" .. tostring(supplyPoints),
        0.52, 0.84, 0.58
    )

    drawInlineMetric(
        communityCardX + 8 + metricW + metricGap,
        communityMetricY,
        metricW,
        tr("UI_QPSR_DailyAutomation"),
        communityDaily,
        0.66, 0.80, 0.88
    )

    drawInlineMetric(
        communityCardX
            + 8
            + (metricW * 2)
            + (metricGap * 2),
        communityMetricY,
        metricW,
        trOr(
            "UI_QPSR_AutomationDailyCap",
            "Daily cap"
        ),
        communityCap,
        0.88, 0.82, 0.56
    )

    self:drawText(
        fitText(
            trOr(
                "UI_QPSR_CommunitySelfCreatedBlocked",
                "Self-created Supply Requests are not rewarded."
            ),
            automationW - 16,
            UIFont.Small
        ),
        communityCardX + 8,
        automationY + automationH - smallLineH - 5,
        0.56, 0.62, 0.68, 1,
        UIFont.Small
    )

    local historyTitleY =
        automationY + automationH + 6

    self:drawText(
        tr("UI_QPSR_RecentActivity"),
        margin,
        historyTitleY,
        1, 1, 1, 1,
        UIFont.Medium
    )

    local recentX = margin
    local recentY =
        historyTitleY + mediumLineH + 3
    local recentW = contentW
    local recentH = math.max(
        44,
        footerY - recentY - 5
    )

    drawCard(
        recentX,
        recentY,
        recentW,
        recentH,
        0.32
    )

    self:setStencilRect(
        recentX,
        recentY,
        recentW,
        recentH
    )

    local history = profile.history or {}

    if #history == 0 then
        self:drawText(
            tr("UI_QPSR_NoActivity"),
            recentX + 8,
            recentY + 8,
            0.65, 0.65, 0.65, 1,
            UIFont.Small
        )
    else
        local rowHeight =
            (smallLineH * 2) + 9

        local visible = math.max(
            1,
            math.min(
                5,
                math.floor(
                    (recentH - 6) / rowHeight
                )
            )
        )

        local rowY = recentY + 3

        for i = 1, visible do
            local entry = history[i]

            if entry then
                local pathColor =
                    PATH_COLORS[
                        tostring(entry.path or "")
                    ]
                    or {0.72, 0.72, 0.72}

                local date = formatTime(entry.time)
                local dateWidth = textWidth(
                    UIFont.Small,
                    date
                )

                self:drawRect(
                    recentX + 4,
                    rowY,
                    recentW - 8,
                    rowHeight - 2,
                    i % 2 == 0 and 0.20 or 0.26,
                    0.07, 0.07, 0.07
                )

                self:drawText(
                    fitText(
                        historyHeader(entry),
                        math.max(
                            120,
                            recentW - dateWidth - 24
                        ),
                        UIFont.Small
                    ),
                    recentX + 8,
                    rowY + 2,
                    pathColor[1],
                    pathColor[2],
                    pathColor[3],
                    1,
                    UIFont.Small
                )

                self:drawTextRight(
                    date,
                    recentX + recentW - 8,
                    rowY + 2,
                    0.58, 0.62, 0.66, 1,
                    UIFont.Small
                )

                local sourceLabel =
                    historySourceLabel(entry)

                local sourceR, sourceG, sourceB =
                    historySourceColor(entry)

                local sourceText =
                    "["
                    .. sourceLabel
                    .. "]"

                local sourceWidth = textWidth(
                    UIFont.Small,
                    sourceText
                )

                local progressText =
                    historyProgressText(entry)

                local progressWidth = progressText ~= ""
                    and textWidth(
                        UIFont.Small,
                        progressText
                    )
                    or 0

                local secondY =
                    rowY + 3 + smallLineH

                self:drawText(
                    sourceText,
                    recentX + 8,
                    secondY,
                    sourceR,
                    sourceG,
                    sourceB,
                    1,
                    UIFont.Small
                )

                local reasonX =
                    recentX + 12 + sourceWidth

                local reasonWidth = math.max(
                    60,
                    recentW
                        - (reasonX - recentX)
                        - progressWidth
                        - 18
                )

                self:drawText(
                    fitText(
                        historyDisplayReason(entry),
                        reasonWidth,
                        UIFont.Small
                    ),
                    reasonX,
                    secondY,
                    0.80, 0.82, 0.84, 1,
                    UIFont.Small
                )

                if progressText ~= "" then
                    self:drawTextRight(
                        progressText,
                        recentX + recentW - 8,
                        secondY,
                        0.62, 0.68, 0.74, 1,
                        UIFont.Small
                    )
                end

                rowY = rowY + rowHeight
            end
        end
    end

    self:clearStencilRect()
end

function UI:new(x, y, width, height)
    local o = ISPanel.new(self, x, y, width, height)
    o.backgroundColor = {r=0.025, g=0.025, b=0.025, a=0.96}
    o.borderColor = {r=0.45, g=0.45, b=0.45, a=1}
    o.moveWithMouse = true
    return o
end

QPReputation.AdminUI = ISPanel:derive("QPReputationAdminUI")
local AdminUI = QPReputation.AdminUI

-- QPSR_SERVER_PLAYER_EDITOR_UI_V1
local function QPSR_trimAdminUsername(value)
    local username = tostring(value or "")

    username = string.gsub(
        username,
        "^%s+",
        ""
    )

    username = string.gsub(
        username,
        "%s+$",
        ""
    )

    return username
end

function AdminUI:initialise()
    ISPanel.initialise(self)

    self.onlinePlayerValues = {}

    self.playerCombo = ISComboBox:new(
        18,
        96,
        330,
        25,
        self,
        AdminUI.onPlayerSelected
    )

    self.playerCombo:initialise()
    self.playerCombo.font = UIFont.Medium
    self:addChild(self.playerCombo)

    self.refreshPlayersButton = ISButton:new(
        360,
        96,
        150,
        25,
        trOr("UI_QPSR_RefreshPlayers", "Refresh list"),
        self,
        AdminUI.onRefreshPlayers
    )

    self.refreshPlayersButton:initialise()
    self.refreshPlayersButton.font = UIFont.Medium
    self:addChild(self.refreshPlayersButton)

    self.usernameEntry = ISTextEntryBox:new(
        "",
        18,
        152,
        330,
        25
    )

    self.usernameEntry:initialise()
    self.usernameEntry.font = UIFont.Medium
    self:addChild(self.usernameEntry)

    self.loadProfileButton = ISButton:new(
        360,
        152,
        150,
        25,
        trOr("UI_QPSR_LoadProfile", "Load player"),
        self,
        AdminUI.onLoadProfile
    )

    self.loadProfileButton:initialise()
    self.loadProfileButton.font = UIFont.Medium
    self:addChild(self.loadProfileButton)

    self.pathValues = {}

    self.pathCombo = ISComboBox:new(
        18,
        208,
        330,
        25,
        self
    )

    self.pathCombo:initialise()
    self.pathCombo.font = UIFont.Medium

    for _, path in ipairs(QPReputation.Paths) do
        self.pathCombo:addOption(
            pathLabel(path)
        )

        table.insert(
            self.pathValues,
            path
        )
    end

    self:addChild(self.pathCombo)

    self.pointsEntry = ISTextEntryBox:new(
        "100",
        360,
        208,
        150,
        25
    )

    self.pointsEntry:initialise()
    self.pointsEntry.font = UIFont.Medium
    self:addChild(self.pointsEntry)

    self.reasonEntry = ISTextEntryBox:new(
        tr("UI_QPSR_AdminAdjustment"),
        18,
        264,
        492,
        25
    )

    self.reasonEntry:initialise()
    self.reasonEntry.font = UIFont.Medium
    self:addChild(self.reasonEntry)

    self.addButton = ISButton:new(
        18,
        310,
        100,
        28,
        tr("UI_QPSR_Add"),
        self,
        AdminUI.onAdd
    )

    self.addButton:initialise()
    self.addButton.font = UIFont.Medium
    self:addChild(self.addButton)

    self.setButton = ISButton:new(
        128,
        310,
        100,
        28,
        tr("UI_QPSR_Set"),
        self,
        AdminUI.onSet
    )

    self.setButton:initialise()
    self.setButton.font = UIFont.Medium
    self:addChild(self.setButton)

    self.resetButton = ISButton:new(
        238,
        310,
        132,
        28,
        tr("UI_QPSR_ResetPath"),
        self,
        AdminUI.onReset
    )

    self.resetButton:initialise()
    self.resetButton.font = UIFont.Medium
    self:addChild(self.resetButton)

    self.settingsButton = ISButton:new(
        18,
        364,
        200,
        28,
        getText("UI_QPSR_AutomationSettings"),
        self,
        AdminUI.onAutomationSettings
    )

    self.settingsButton:initialise()
    self.settingsButton.font = UIFont.Medium
    self:addChild(self.settingsButton)

    self.closeButton = ISButton:new(
        410,
        364,
        100,
        28,
        tr("UI_QPSR_Close"),
        self,
        AdminUI.onClose
    )

    self.closeButton:initialise()
    self.closeButton.font = UIFont.Medium
    self:addChild(self.closeButton)

    addResizeWidget(self)

    self:refreshOnlinePlayers(nil)
end

function AdminUI:setActionsEnabled(enabled)
    self.addButton:setEnable(enabled)
    self.setButton:setEnable(enabled)
    self.resetButton:setEnable(enabled)
end

function AdminUI:refreshOnlinePlayers(
    preferredUsername
)
    preferredUsername =
        QPSR_trimAdminUsername(
            preferredUsername
        )

    if preferredUsername == "" then
        preferredUsername =
            QPSR_trimAdminUsername(
                self.usernameEntry
                and self.usernameEntry:getText()
                or ""
            )
    end

    self.onlinePlayerValues = {}

    if self.playerCombo.clear then
        self.playerCombo:clear()
    else
        self.playerCombo.options = {}
    end

    local names = {}
    local seen = {}

    local okPlayers, players = pcall(function()
        return getOnlinePlayers()
    end)

    if okPlayers and players ~= nil then
        for index = 0, players:size() - 1 do
            local candidate =
                players:get(index)

            if candidate ~= nil then
                local okName, username =
                    pcall(function()
                        return candidate:getUsername()
                    end)

                username =
                    okName
                    and QPSR_trimAdminUsername(username)
                    or ""

                local normalized =
                    string.lower(username)

                if username ~= ""
                    and seen[normalized] ~= true then
                    seen[normalized] = true
                    table.insert(names, username)
                end
            end
        end
    end

    local localPlayer = getPlayer()

    if localPlayer ~= nil then
        local okName, username =
            pcall(function()
                return localPlayer:getUsername()
            end)

        username =
            okName
            and QPSR_trimAdminUsername(username)
            or ""

        local normalized =
            string.lower(username)

        if username ~= ""
            and seen[normalized] ~= true then
            seen[normalized] = true
            table.insert(names, username)
        end
    end

    table.sort(
        names,
        function(left, right)
            return string.lower(left)
                < string.lower(right)
        end
    )

    local selectedIndex = 0

    for index, username in ipairs(names) do
        self.playerCombo:addOption(username)

        table.insert(
            self.onlinePlayerValues,
            username
        )

        if preferredUsername ~= ""
            and string.lower(username)
                == string.lower(preferredUsername) then
            selectedIndex = index
        end
    end

    if #names == 0 then
        self.playerCombo:addOption(
            trOr("UI_QPSR_NoOnlinePlayers", "No online players")
        )

        table.insert(
            self.onlinePlayerValues,
            ""
        )

        self.playerCombo.selected = 1
        return
    end

    if selectedIndex <= 0 then
        selectedIndex = 1
    end

    self.playerCombo.selected =
        selectedIndex

    local selectedUsername =
        self.onlinePlayerValues[selectedIndex]

    if preferredUsername == ""
        and selectedUsername then
        self.usernameEntry:setText(
            selectedUsername
        )
    elseif preferredUsername ~= "" then
        self.usernameEntry:setText(
            preferredUsername
        )
    end
end

function AdminUI:selectInitialTarget()
    local profile =
        QPReputation.Client.profile

    local username =
        profile
        and profile.username
        or ""

    username =
        QPSR_trimAdminUsername(username)

    self:refreshOnlinePlayers(username)

    if username ~= "" then
        self.usernameEntry:setText(username)
        self:requestUsername(username)
    end
end

function AdminUI:requestUsername(username)
    username =
        QPSR_trimAdminUsername(username)

    if username == "" then
        return false
    end

    self.usernameEntry:setText(username)
    self:setActionsEnabled(false)

    return QPReputation.Client
        .requestAdminProfile(username)
end

function AdminUI:onPlayerSelected()
    local selected =
        tonumber(self.playerCombo.selected)
        or 0

    local username =
        self.onlinePlayerValues[selected]

    username =
        QPSR_trimAdminUsername(username)

    if username == "" then
        return
    end

    self.usernameEntry:setText(username)
    self:requestUsername(username)
end

function AdminUI:onRefreshPlayers()
    local preferred =
        QPSR_trimAdminUsername(
            self.usernameEntry:getText()
        )

    self:refreshOnlinePlayers(preferred)

    if preferred ~= "" then
        self:requestUsername(preferred)
    end
end

function AdminUI:onLoadProfile()
    self:requestUsername(
        self.usernameEntry:getText()
    )
end

function AdminUI:onAdminProfileUpdated(
    profile,
    response
)
    if profile and profile.username then
        self.usernameEntry:setText(
            tostring(profile.username)
        )
    elseif response
        and response.requestedUsername then
        self.usernameEntry:setText(
            tostring(
                response.requestedUsername
            )
        )
    end

    self:setActionsEnabled(true)
end

function AdminUI:prerender()
    -- QPSR_V041_RC37_BUILD42_ADMIN_MEASURED_REFLOW
    enforceMinimumSize(self, 560, 360)

    local smallLineH = fontHeight(UIFont.Small, 14)
    local mediumLineH = fontHeight(UIFont.Medium, 18)
    local largeLineH = fontHeight(UIFont.Large, 24)
    local controlH = math.max(28, mediumLineH + 8)
    local buttonH = math.max(28, mediumLineH + 8)
    local left = 14
    local right = 14
    local gap = 10
    local rowGap = 8
    local labelGap = 3
    local contentWidth = self.width - left - right

    local titleY = 8
    local selectedY = titleY + largeLineH + 1
    local onlineLabelY = selectedY + smallLineH + 8
    local onlineFieldY = onlineLabelY + smallLineH + labelGap
    local usernameLabelY = onlineFieldY + controlH + rowGap
    local usernameFieldY = usernameLabelY + smallLineH + labelGap
    local pathLabelY = usernameFieldY + controlH + rowGap
    local pathFieldY = pathLabelY + smallLineH + labelGap
    local reasonLabelY = pathFieldY + controlH + rowGap
    local reasonFieldY = reasonLabelY + smallLineH + labelGap
    local actionsY = reasonFieldY + controlH + 10
    local footerY = actionsY + buttonH + 12
    local requiredH = footerY + buttonH + 12

    if self.height < requiredH then
        self:setHeight(requiredH)
    end

    local sideButtonWidth = math.max(
        126,
        math.floor(contentWidth * 0.25)
    )

    local mainWidth =
        contentWidth - sideButtonWidth - gap

    self.playerCombo:setX(left)
    self.playerCombo:setY(onlineFieldY)
    self.playerCombo:setWidth(mainWidth)
    self.playerCombo:setHeight(controlH)

    self.refreshPlayersButton:setX(
        left + mainWidth + gap
    )
    self.refreshPlayersButton:setY(onlineFieldY)
    self.refreshPlayersButton:setWidth(sideButtonWidth)
    self.refreshPlayersButton:setHeight(controlH)

    self.usernameEntry:setX(left)
    self.usernameEntry:setY(usernameFieldY)
    self.usernameEntry:setWidth(mainWidth)
    self.usernameEntry:setHeight(controlH)

    self.loadProfileButton:setX(
        left + mainWidth + gap
    )
    self.loadProfileButton:setY(usernameFieldY)
    self.loadProfileButton:setWidth(sideButtonWidth)
    self.loadProfileButton:setHeight(controlH)

    local pointsWidth = math.max(
        104,
        math.floor(contentWidth * 0.24)
    )
    local pathWidth = contentWidth - pointsWidth - gap

    self.pathCombo:setX(left)
    self.pathCombo:setY(pathFieldY)
    self.pathCombo:setWidth(pathWidth)
    self.pathCombo:setHeight(controlH)

    self.pointsEntry:setX(left + pathWidth + gap)
    self.pointsEntry:setY(pathFieldY)
    self.pointsEntry:setWidth(pointsWidth)
    self.pointsEntry:setHeight(controlH)

    self.reasonEntry:setX(left)
    self.reasonEntry:setY(reasonFieldY)
    self.reasonEntry:setWidth(contentWidth)
    self.reasonEntry:setHeight(controlH)

    local addWidth = math.max(
        84,
        textWidth(UIFont.Small, tr("UI_QPSR_Add")) + 24
    )
    local setWidth = math.max(
        84,
        textWidth(UIFont.Small, tr("UI_QPSR_Set")) + 24
    )
    local resetWidth = math.max(
        112,
        textWidth(UIFont.Small, tr("UI_QPSR_ResetPath")) + 24
    )
    local buttonGap = 8

    self.addButton:setX(left)
    self.addButton:setY(actionsY)
    self.addButton:setWidth(addWidth)
    self.addButton:setHeight(buttonH)

    self.setButton:setX(left + addWidth + buttonGap)
    self.setButton:setY(actionsY)
    self.setButton:setWidth(setWidth)
    self.setButton:setHeight(buttonH)

    self.resetButton:setX(
        left + addWidth + buttonGap + setWidth + buttonGap
    )
    self.resetButton:setY(actionsY)
    self.resetButton:setWidth(resetWidth)
    self.resetButton:setHeight(buttonH)

    local settingsWidth = math.max(
        176,
        textWidth(
            UIFont.Small,
            getText("UI_QPSR_AutomationSettings")
        ) + 28
    )
    local closeWidth = math.max(
        90,
        textWidth(UIFont.Small, tr("UI_QPSR_Close")) + 28
    )

    self.settingsButton:setX(left)
    self.settingsButton:setY(footerY)
    self.settingsButton:setWidth(settingsWidth)
    self.settingsButton:setHeight(buttonH)

    self.closeButton:setX(self.width - right - closeWidth)
    self.closeButton:setY(footerY)
    self.closeButton:setWidth(closeWidth)
    self.closeButton:setHeight(buttonH)

    ISPanel.prerender(self)

    self:drawRect(
        0, 0, self.width, selectedY + smallLineH + 6,
        0.72, 0.05, 0.05, 0.05
    )

    local pending =
        QPReputation.Client.adminRequestPending == true
        or QPReputation.Client
            .adminProfileRequestPending == true

    local enteredUsername =
        QPSR_trimAdminUsername(
            self.usernameEntry:getText()
        )

    self:setActionsEnabled(
        not pending
        and enteredUsername ~= ""
    )

    self:drawText(
        tr("UI_QPSR_AdminTitle"),
        left,
        titleY,
        1, 1, 1, 1,
        UIFont.Large
    )

    local profile = QPReputation.Client.adminProfile
    local displayedUsername =
        profile and profile.username or enteredUsername

    if displayedUsername == "" then
        displayedUsername = tr("UI_QPSR_Unknown")
    end

    self:drawText(
        trOr("UI_QPSR_SelectedPlayer", "Selected player")
            .. ": "
            .. tostring(displayedUsername),
        left,
        selectedY,
        0.8, 0.8, 0.8, 1,
        UIFont.Small
    )

    self:drawText(
        trOr("UI_QPSR_OnlinePlayer", "Online players"),
        left,
        onlineLabelY,
        0.7, 0.7, 0.7, 1,
        UIFont.Small
    )

    self:drawText(
        trOr(
            "UI_QPSR_ExactUsername",
            "Username (online or offline)"
        ),
        left,
        usernameLabelY,
        0.7, 0.7, 0.7, 1,
        UIFont.Small
    )

    self:drawText(
        tr("UI_QPSR_Path"),
        left,
        pathLabelY,
        0.7, 0.7, 0.7, 1,
        UIFont.Small
    )

    self:drawText(
        tr("UI_QPSR_Points"),
        left + pathWidth + gap,
        pathLabelY,
        0.7, 0.7, 0.7, 1,
        UIFont.Small
    )

    self:drawText(
        tr("UI_QPSR_Reason"),
        left,
        reasonLabelY,
        0.7, 0.7, 0.7, 1,
        UIFont.Small
    )
end

function AdminUI:getValues()
    local selected =
        tonumber(self.pathCombo.selected)
        or 1

    local path =
        self.pathValues[selected]

    local username =
        QPSR_trimAdminUsername(
            self.usernameEntry:getText()
        )

    return
        username ~= "" and username or nil,
        path,
        tonumber(
            self.pointsEntry:getText()
        ) or 0,
        self.reasonEntry:getText() or ""
end

function AdminUI:onAdd()
    local username, path, points, reason =
        self:getValues()

    if username
        and QPReputation.Client.adminAdd(
            username,
            path,
            points,
            reason
        ) then
        self:setActionsEnabled(false)
    end
end

function AdminUI:onSet()
    local username, path, points, reason =
        self:getValues()

    if username
        and QPReputation.Client.adminSet(
            username,
            path,
            points,
            reason
        ) then
        self:setActionsEnabled(false)
    end
end

function AdminUI:onReset()
    local username, path, _, reason =
        self:getValues()

    if username
        and QPReputation.Client.adminReset(
            username,
            path,
            reason
        ) then
        self:setActionsEnabled(false)
    end
end

function AdminUI:onAutomationSettings()
    QPReputation.openAutomationSettings()
end

function AdminUI:onClose()
    QPReputation.Client
        .removeAdminProfileListener(self)

    self:setVisible(false)
    self:removeFromUIManager()
end

function AdminUI:new(
    x,
    y,
    width,
    height
)
    local o = ISPanel.new(
        self,
        x,
        y,
        width,
        height
    )

    o.backgroundColor = {
        r = 0.03,
        g = 0.03,
        b = 0.03,
        a = 0.96
    }

    o.borderColor = {
        r = 0.5,
        g = 0.5,
        b = 0.5,
        a = 1
    }

    o.moveWithMouse = true
    return o
end

QPReputation.AutomationSettingsUI = ISPanel:derive(
    "QPReputationAutomationSettingsUI"
)
local SettingsUI = QPReputation.AutomationSettingsUI

function SettingsUI:initialise()
    ISPanel.initialise(self)

    self.masterTick = ISTickBox:new(20, 78, 360, 30, "", self)
    self.masterTick:initialise()
    self.masterTick.font = UIFont.Medium
    self.masterTick:addOption(
        trOr(
            "UI_QPSR_AutomationEnabled",
            "Enabled"
        )
    )
    self:addChild(self.masterTick)

    self.hunterTick = ISTickBox:new(20, 114, 360, 30, "", self)
    self.hunterTick:initialise()
    self.hunterTick.font = UIFont.Medium
    self.hunterTick:addOption(
        trOr(
            "UI_QPSR_AutomationEnabled",
            "Enabled"
        )
    )
    self:addChild(self.hunterTick)

    self.presetCombo = ISComboBox:new(20, 176, 410, 32, self)
    self.presetCombo:initialise()
    self.presetCombo.font = UIFont.Medium
    self.presetCombo:addOption(
        getText("UI_QPSR_PresetProduction")
    )
    self.presetCombo:addOption(
        getText("UI_QPSR_PresetTesting")
    )
    self:addChild(self.presetCombo)

    self.scanEntry = ISTextEntryBox:new("1", 250, 226, 220, 32)
    self.scanEntry:initialise()
    self.scanEntry.font = UIFont.Medium
    self:addChild(self.scanEntry)

    self.capEntry = ISTextEntryBox:new("145", 250, 270, 220, 32)
    self.capEntry:initialise()
    self.capEntry.font = UIFont.Medium
    self:addChild(self.capEntry)

    self.jumpEntry = ISTextEntryBox:new("50", 250, 314, 220, 32)
    self.jumpEntry:initialise()
    self.jumpEntry.font = UIFont.Medium
    self:addChild(self.jumpEntry)

    self.communityTick = ISTickBox:new(
        20,
        390,
        500,
        24,
        "",
        self
    )
    self.communityTick:initialise()
    self.communityTick.font = UIFont.Medium
    self.communityTick:addOption(
        trOr(
            "UI_QPSR_AutomationEnabled",
            "Enabled"
        )
    )
    self:addChild(self.communityTick)

    self.supplyTick = ISTickBox:new(
        20,
        430,
        440,
        24,
        "",
        self
    )
    self.supplyTick:initialise()
    self.supplyTick.font = UIFont.Medium
    self.supplyTick:addOption(
        trOr(
            "UI_QPSR_CommunitySupplyRequests",
            "Supply Request completion points"
        )
    )
    self:addChild(self.supplyTick)

    self.supplyPointsEntry =
        ISTextEntryBox:new("5", 510, 426, 180, 32)
    self.supplyPointsEntry:initialise()
    self.supplyPointsEntry.font = UIFont.Medium
    self:addChild(self.supplyPointsEntry)

    self.communityCapEntry =
        ISTextEntryBox:new("25", 510, 468, 180, 32)
    self.communityCapEntry:initialise()
    self.communityCapEntry.font = UIFont.Medium
    self:addChild(self.communityCapEntry)

    self.applyButton = ISButton:new(
        20,
        self.height - 46,
        105,
        28,
        getText("UI_QPSR_Apply"),
        self,
        SettingsUI.onApply
    )
    self.applyButton:initialise()
    self.applyButton.font = UIFont.Medium
    self:addChild(self.applyButton)

    self.defaultsButton = ISButton:new(
        135,
        self.height - 46,
        160,
        28,
        getText("UI_QPSR_RestoreDefaults"),
        self,
        SettingsUI.onDefaults
    )
    self.defaultsButton:initialise()
    self.defaultsButton.font = UIFont.Medium
    self:addChild(self.defaultsButton)

    self.closeButton = ISButton:new(
        self.width - 120,
        self.height - 46,
        100,
        28,
        getText("UI_QPSR_Close"),
        self,
        SettingsUI.onClose
    )
    self.closeButton:initialise()
    self.closeButton.font = UIFont.Medium
    self:addChild(self.closeButton)

    addResizeWidget(self)
    self:setControlsEnabled(false)
end

function SettingsUI:setControlsEnabled(enabled)
    setControlEnabled(self.masterTick, enabled)
    setControlEnabled(self.hunterTick, enabled)
    setControlEnabled(self.presetCombo, enabled)
    setTextEntryEditable(self.scanEntry, enabled)
    setTextEntryEditable(self.capEntry, enabled)
    setTextEntryEditable(self.jumpEntry, enabled)
    setControlEnabled(self.communityTick, enabled)
    setControlEnabled(self.supplyTick, enabled)
    setTextEntryEditable(self.supplyPointsEntry, enabled)
    setTextEntryEditable(self.communityCapEntry, enabled)
    setControlEnabled(self.applyButton, enabled)
    setControlEnabled(self.defaultsButton, enabled)
end

function SettingsUI:loadSettings(settings, response)
    if not settings then return end

    self.settings = settings

    local hunterSettings =
        settings.paths
        and settings.paths.hunter
        or {}

    local communitySettings =
        settings.paths
        and settings.paths.community
        or {}

    local hunterEnabled =
        settings.hunterEnabled

    if hunterEnabled == nil then
        hunterEnabled = hunterSettings.enabled
    end

    local preset =
        settings.preset
        or hunterSettings.preset
        or 2

    local dailyCap =
        settings.dailyCap
        or hunterSettings.dailyCap
        or 145

    local maximumProgress =
        settings.maxKillsPerScan
        or hunterSettings.maxProgressPerScan
        or 50

    self.masterTick:setSelected(
        1,
        settings.enabled == true
    )
    self.hunterTick:setSelected(
        1,
        hunterEnabled == true
    )
    self.presetCombo.selected = tonumber(preset) or 2
    self.scanEntry:setText(tostring(settings.scanInterval or 1))
    self.capEntry:setText(tostring(dailyCap))
    self.jumpEntry:setText(
        tostring(maximumProgress)
    )

    local communityEnabled =
        settings.communityEnabled

    if communityEnabled == nil then
        communityEnabled = communitySettings.enabled
    end

    local supplyEnabled =
        settings.supplyRequestsEnabled

    if supplyEnabled == nil then
        supplyEnabled =
            communitySettings.supplyRequestsEnabled
    end

    local supplyPoints =
        settings.supplyRequestPoints
        or communitySettings.supplyRequestPoints
        or 5

    local communityCap =
        settings.communityDailyCap
        or communitySettings.dailyCap
        or 25

    self.communityTick:setSelected(
        1,
        communityEnabled == true
    )
    self.supplyTick:setSelected(
        1,
        supplyEnabled ~= false
    )
    self.supplyPointsEntry:setText(
        tostring(supplyPoints)
    )
    self.communityCapEntry:setText(
        tostring(communityCap)
    )
    self.registryVersion =
        tonumber(settings.registryVersion) or 0
    self.registeredCount =
        tonumber(settings.registeredCount) or 0
    self.implementedCount =
        tonumber(settings.implementedCount) or 0
    self.activeCount =
        tonumber(settings.activeCount) or 0

    self.statusText = nil
    if response and response.saved then
        self.statusText = getText("UI_QPSR_SettingsSaved")
    elseif response and response.reset then
        self.statusText = getText("UI_QPSR_SettingsReset")
    end

    self:setControlsEnabled(true)
end

function SettingsUI:prerender()
    -- QPSR_V041_RC37_BUILD42_SETTINGS_MEASURED_REFLOW
    enforceMinimumSize(self, 760, 640)

    local smallLineH = fontHeight(UIFont.Small, 14)
    local mediumLineH = fontHeight(UIFont.Medium, 18)
    local largeLineH = fontHeight(UIFont.Large, 24)
    local controlH = math.max(28, mediumLineH + 8)
    local tickH = math.max(24, mediumLineH + 5)
    local buttonH = math.max(28, mediumLineH + 8)
    local left = 14
    local right = 14
    local innerPad = 10
    local sectionGap = 8
    local contentWidth = self.width - left - right
    local headerH = math.max(44, largeLineH + 18)

    local masterY = headerH + 8
    local masterTitleY = masterY + 7
    local masterHelpY = masterTitleY + mediumLineH + 1
    local masterTickY = masterHelpY + smallLineH + 5
    local masterH =
        (masterTickY - masterY) + tickH + 8

    local hunterY = masterY + masterH + sectionGap
    local hunterTitleY = hunterY + 7
    local hunterTickY = hunterTitleY + mediumLineH + 3
    local presetLabelY = hunterTickY + tickH + 4
    local presetY = presetLabelY + smallLineH + 3
    local firstHunterRowY = presetY + controlH + 9
    local hunterRowGap = 7
    local hunterH =
        (firstHunterRowY - hunterY)
        + (controlH * 3)
        + (hunterRowGap * 2)
        + 9

    local communityY = hunterY + hunterH + sectionGap
    local communityTitleY = communityY + 7
    local communityTickY = communityTitleY + mediumLineH + 3
    local supplyRowY = communityTickY + tickH + 5
    local capRowY = supplyRowY + controlH + 7
    local communityNoteY = capRowY + controlH + 5
    local communityH =
        (communityNoteY - communityY)
        + smallLineH
        + 8

    local registryY = communityY + communityH + sectionGap
    local registryTitleY = registryY + 7
    local registryTextY = registryTitleY + mediumLineH + 2
    local registryH =
        (registryTextY - registryY)
        + smallLineH
        + 8

    local footerY = registryY + registryH + 10
    local requiredH = footerY + buttonH + 12

    if self.height < requiredH then
        self:setHeight(requiredH)
    end

    local function drawSection(x, y, width, height)
        self:drawRect(
            x, y, width, height,
            0.38,
            0.055, 0.055, 0.055
        )

        self:drawRectBorder(
            x, y, width, height,
            0.52,
            0.30, 0.30, 0.30
        )
    end

    local labelWidth = math.max(
        230,
        math.floor(contentWidth * 0.44)
    )
    local fieldX = left + labelWidth + 18
    local fieldWidth = math.max(
        160,
        contentWidth - labelWidth - 30
    )

    self.masterTick:setX(left + innerPad)
    self.masterTick:setY(masterTickY)
    self.masterTick:setWidth(contentWidth - (innerPad * 2))
    self.masterTick:setHeight(tickH)

    self.hunterTick:setX(left + innerPad)
    self.hunterTick:setY(hunterTickY)
    self.hunterTick:setWidth(contentWidth - (innerPad * 2))
    self.hunterTick:setHeight(tickH)

    self.presetCombo:setX(left + innerPad)
    self.presetCombo:setY(presetY)
    self.presetCombo:setWidth(contentWidth - (innerPad * 2))
    self.presetCombo:setHeight(controlH)

    self.scanEntry:setX(fieldX)
    self.scanEntry:setY(firstHunterRowY)
    self.scanEntry:setWidth(fieldWidth)
    self.scanEntry:setHeight(controlH)

    self.capEntry:setX(fieldX)
    self.capEntry:setY(firstHunterRowY + controlH + hunterRowGap)
    self.capEntry:setWidth(fieldWidth)
    self.capEntry:setHeight(controlH)

    self.jumpEntry:setX(fieldX)
    self.jumpEntry:setY(
        firstHunterRowY + ((controlH + hunterRowGap) * 2)
    )
    self.jumpEntry:setWidth(fieldWidth)
    self.jumpEntry:setHeight(controlH)

    self.communityTick:setX(left + innerPad)
    self.communityTick:setY(communityTickY)
    self.communityTick:setWidth(contentWidth - (innerPad * 2))
    self.communityTick:setHeight(tickH)

    self.supplyTick:setX(left + innerPad)
    self.supplyTick:setY(supplyRowY)
    self.supplyTick:setWidth(labelWidth - innerPad)
    self.supplyTick:setHeight(controlH)

    self.supplyPointsEntry:setX(fieldX)
    self.supplyPointsEntry:setY(supplyRowY)
    self.supplyPointsEntry:setWidth(fieldWidth)
    self.supplyPointsEntry:setHeight(controlH)

    self.communityCapEntry:setX(fieldX)
    self.communityCapEntry:setY(capRowY)
    self.communityCapEntry:setWidth(fieldWidth)
    self.communityCapEntry:setHeight(controlH)

    local applyWidth = math.max(
        92,
        textWidth(UIFont.Small, getText("UI_QPSR_Apply")) + 28
    )
    local defaultsWidth = math.max(
        150,
        textWidth(
            UIFont.Small,
            getText("UI_QPSR_RestoreDefaults")
        ) + 28
    )
    local closeWidth = math.max(
        90,
        textWidth(UIFont.Small, getText("UI_QPSR_Close")) + 28
    )

    self.applyButton:setX(left)
    self.applyButton:setY(footerY)
    self.applyButton:setWidth(applyWidth)
    self.applyButton:setHeight(buttonH)

    self.defaultsButton:setX(left + applyWidth + 9)
    self.defaultsButton:setY(footerY)
    self.defaultsButton:setWidth(defaultsWidth)
    self.defaultsButton:setHeight(buttonH)

    self.closeButton:setX(self.width - right - closeWidth)
    self.closeButton:setY(footerY)
    self.closeButton:setWidth(closeWidth)
    self.closeButton:setHeight(buttonH)

    ISPanel.prerender(self)

    self:drawRect(
        0, 0, self.width, headerH,
        0.94,
        0.07, 0.07, 0.07
    )

    self:drawText(
        getText("UI_QPSR_AutomationSettings"),
        left,
        math.max(8, math.floor((headerH - largeLineH) / 2)),
        1, 1, 1, 1,
        UIFont.Large
    )

    drawSection(left, masterY, contentWidth, masterH)

    self:drawText(
        trOr("UI_QPSR_AutomationMaster", "Master Automation"),
        left + innerPad,
        masterTitleY,
        0.92, 0.92, 0.92, 1,
        UIFont.Medium
    )

    self:drawText(
        fitText(
            getText("UI_QPSR_ServerAuthoritative"),
            contentWidth - (innerPad * 2),
            UIFont.Small
        ),
        left + innerPad,
        masterHelpY,
        0.58, 0.64, 0.70, 1,
        UIFont.Small
    )

    drawSection(left, hunterY, contentWidth, hunterH)

    self:drawText(
        trOr("UI_QPSR_HunterAutomation", "Hunter Automation"),
        left + innerPad,
        hunterTitleY,
        0.92, 0.92, 0.92, 1,
        UIFont.Medium
    )

    self:drawText(
        getText("UI_QPSR_MilestonePreset"),
        left + innerPad,
        presetLabelY,
        0.62, 0.68, 0.74, 1,
        UIFont.Small
    )

    local scanY = firstHunterRowY
    local capY = firstHunterRowY + controlH + hunterRowGap
    local jumpY = firstHunterRowY + ((controlH + hunterRowGap) * 2)
    local labelYOffset = math.max(
        0,
        math.floor((controlH - smallLineH) / 2)
    )

    self:drawText(
        getText("UI_QPSR_ScanIntervalMinutes"),
        left + innerPad,
        scanY + labelYOffset,
        0.75, 0.75, 0.75, 1,
        UIFont.Small
    )

    self:drawText(
        getText("UI_QPSR_DailyPointCap"),
        left + innerPad,
        capY + labelYOffset,
        0.75, 0.75, 0.75, 1,
        UIFont.Small
    )

    self:drawText(
        getText("UI_QPSR_MaxKillsPerScan"),
        left + innerPad,
        jumpY + labelYOffset,
        0.75, 0.75, 0.75, 1,
        UIFont.Small
    )

    drawSection(left, communityY, contentWidth, communityH)

    self:drawText(
        trOr("UI_QPSR_CommunityAutomation", "Community Automation"),
        left + innerPad,
        communityTitleY,
        0.92, 0.92, 0.92, 1,
        UIFont.Medium
    )

    self:drawText(
        trOr(
            "UI_QPSR_CommunityDailyCap",
            "Community daily point cap"
        ),
        left + innerPad,
        capRowY + labelYOffset,
        0.75, 0.75, 0.75, 1,
        UIFont.Small
    )

    self:drawText(
        fitText(
            trOr(
                "UI_QPSR_CommunitySelfCreatedBlocked",
                "Self-created Supply Requests are not rewarded."
            ),
            contentWidth - (innerPad * 2),
            UIFont.Small
        ),
        left + innerPad,
        communityNoteY,
        0.58, 0.66, 0.72, 1,
        UIFont.Small
    )

    drawSection(left, registryY, contentWidth, registryH)

    self:drawText(
        trOr("UI_QPSR_AutomationRegistry", "Automation registry"),
        left + innerPad,
        registryTitleY,
        0.92, 0.92, 0.92, 1,
        UIFont.Medium
    )

    if self.settings and self.registryVersion > 0 then
        local registryText =
            "v"
            .. tostring(self.registryVersion)
            .. "   |   "
            .. trOr("UI_QPSR_Registered", "Registered")
            .. ": "
            .. tostring(self.registeredCount)
            .. "   |   "
            .. trOr("UI_QPSR_Implemented", "Implemented")
            .. ": "
            .. tostring(self.implementedCount)
            .. "   |   "
            .. trOr("UI_QPSR_Active", "Active")
            .. ": "
            .. tostring(self.activeCount)

        self:drawText(
            fitText(
                registryText,
                contentWidth - (innerPad * 2),
                UIFont.Small
            ),
            left + innerPad,
            registryTextY,
            0.58, 0.74, 0.88, 1,
            UIFont.Small
        )
    elseif not self.settings then
        self:drawText(
            getText("UI_QPSR_SettingsLoading"),
            left + innerPad,
            registryTextY,
            0.72, 0.72, 0.72, 1,
            UIFont.Small
        )
    end

    if self.statusText then
        self:drawTextRight(
            fitText(
                self.statusText,
                math.floor(contentWidth * 0.42),
                UIFont.Small
            ),
            left + contentWidth - innerPad,
            registryTextY,
            0.48, 0.82, 0.52, 1,
            UIFont.Small
        )
    end
end

function SettingsUI:onApply()
    local hunterEnabled =
        self.hunterTick:isSelected(1)
    local preset =
        tonumber(self.presetCombo.selected) or 2
    local dailyCap =
        tonumber(self.capEntry:getText()) or 0
    local maximumProgress =
        tonumber(self.jumpEntry:getText()) or 50
    local communityEnabled =
        self.communityTick:isSelected(1)
    local supplyEnabled =
        self.supplyTick:isSelected(1)
    local supplyPoints =
        tonumber(self.supplyPointsEntry:getText()) or 5
    local communityCap =
        tonumber(self.communityCapEntry:getText()) or 25
    local settings = {
        enabled = self.masterTick:isSelected(1),
        hunterEnabled = hunterEnabled,
        preset = preset,
        scanInterval =
            tonumber(self.scanEntry:getText()) or 1,
        dailyCap = dailyCap,
        maxKillsPerScan = maximumProgress,
        communityEnabled = communityEnabled,
        communityDailyCap = communityCap,
        supplyRequestsEnabled = supplyEnabled,
        supplyRequestPoints = supplyPoints,
        paths = {
            hunter = {
                enabled = hunterEnabled,
                preset = preset,
                dailyCap = dailyCap,
                maxProgressPerScan =
                    maximumProgress,
            },
            community = {
                enabled = communityEnabled,
                dailyCap = communityCap,
                supplyRequestsEnabled = supplyEnabled,
                supplyRequestPoints = supplyPoints,
            },
        },
    }

    self:setControlsEnabled(false)
    QPReputation.Client.saveAutomationSettings(settings)
end

function SettingsUI:onDefaults()
    self:setControlsEnabled(false)
    QPReputation.Client.resetAutomationSettings()
end

function SettingsUI:onClose()
    QPReputation.Client.removeSettingsListener(self)
    self:setVisible(false)
    self:removeFromUIManager()
end

function SettingsUI:new(x, y, width, height)
    local o = ISPanel.new(self, x, y, width, height)
    o.backgroundColor = {
        r = 0.025,
        g = 0.025,
        b = 0.025,
        a = 0.97
    }
    o.borderColor = {
        r = 0.48,
        g = 0.48,
        b = 0.48,
        a = 1
    }
    o.moveWithMouse = true
    return o
end

function QPReputation.openAutomationSettings()
    local x, y, width, height =
        QPSR_B42_windowRect("settings")

    local ui = SettingsUI:new(
        x,
        y,
        width,
        height
    )

    ui:initialise()
    QPReputation.Client.addSettingsListener(
        ui,
        SettingsUI.loadSettings
    )
    ui:addToUIManager()
    ui:setVisible(true)

    if QPReputation.Client.automationSettings then
        ui:loadSettings(
            QPReputation.Client.automationSettings
        )
    end

    QPReputation.Client.requestAutomationSettings()
end

function QPReputation.openAdminEditor()
    local x, y, width, height =
        QPSR_B42_windowRect("admin")

    local titleWidth = textWidth(
        UIFont.Large,
        tr("UI_QPSR_AdminTitle")
    )

    local refreshWidth = textWidth(
        UIFont.Small,
        trOr("UI_QPSR_RefreshPlayers", "Refresh list")
    )

    width = math.max(
        width,
        520,
        titleWidth + 36,
        refreshWidth + 360
    )

    local screenW = getCore():getScreenWidth()
    if x + width > screenW - 16 then
        width = math.max(520, screenW - x - 16)
    end

    local ui = AdminUI:new(
        x,
        y,
        width,
        height
    )

    ui:initialise()

    QPReputation.Client
        .addAdminProfileListener(
            ui,
            AdminUI.onAdminProfileUpdated
        )

    ui:addToUIManager()
    ui:setVisible(true)
    ui:selectInitialTarget()
end

function QPReputation.openProfile()
    QPReputation.Client.requestProfile()

    local x, y, width, height =
        QPSR_B42_windowRect("profile")

    local ui = UI:new(
        x,
        y,
        width,
        height
    )

    ui:initialise()

    QPReputation.Client.addProfileListener(
        ui,
        function(owner, profile)
        end
    )

    ui:addToUIManager()
    ui:setVisible(true)
end

local function addContextMenu(playerIndex, context, worldobjects, test)
    if test then return true end
    context:addOption(tr("UI_QPSR_ViewReputation"), nil, QPReputation.openProfile)
end

Events.OnFillWorldObjectContextMenu.Add(addContextMenu)
