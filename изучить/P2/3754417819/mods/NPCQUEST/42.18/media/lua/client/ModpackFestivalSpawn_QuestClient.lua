-- Client quest progress + HUD panel.

if not ModpackFestivalQuests then
    print("[ModpackFestivalSpawn] QuestClient: shared quest module missing")
    return
end

local MOD_ID = ModpackFestivalQuests.MOD_ID
local tickDelay = 0
local lastCompleteFlash = 0
local pendingPlayerSay = nil
local pendingSayTicks = 0
local pendingSisterSayQueue = {}
local pendingSisterSayTicks = 0
local SISTER_SCRIPTED_LINE_SPACING_TICKS = (ModpackFestivalTick and ModpackFestivalTick.sec)
    and ModpackFestivalTick.sec(3) or 180
local SISTER_SCRIPTED_SPEECH_GUARD_MS = 15000
local NAV_SCAN_INTERVAL_MS = 5000
local lastNavScanMs = 0

local function showEndOfQuestPopup()
    pcall(function()
        local W, H = 540, 460
        local sw = getCore():getScreenWidth()
        local sh = getCore():getScreenHeight()
        local px = math.floor((sw - W) / 2)
        local py = math.floor((sh - H) / 2)

        -- Build text rows before creating the panel so render() can close over them
        local rows = {
            { text = "END OF QUEST SYSTEM TEST",                                    font = UIFont.Large,  r=1,    g=1,    b=1,    pad=10 },
            { text = "Survival from here is up to you.",                            font = UIFont.Medium, r=0.72, g=0.72, b=0.72, pad=14 },
            { text = "--- divider ---",                                              divider = true,                               pad=10 },
            { text = "WHAT YOU CAN DO WITH YOUR SISTER:",                           font = UIFont.Medium, r=1,    g=1,    b=1,    pad=6  },
            { text = "- Click on her to open her inventory and swap items.",        font = UIFont.Small,  r=0.85, g=0.85, b=0.85, pad=2  },
            { text = "- Yell a CALL OVER to move her to your position.",            font = UIFont.Small,  r=0.85, g=0.85, b=0.85, pad=2  },
            { text = "- She will follow you, fight enemies, and idle at your side.",font = UIFont.Small,  r=0.85, g=0.85, b=0.85, pad=2  },
            { text = "- Getting hit causes stress and sadness to the player.",      font = UIFont.Small,  r=0.85, g=0.85, b=0.85, pad=12 },
            { text = "IMMUNITY:",                                                   font = UIFont.Medium, r=1,    g=1,    b=1,    pad=4  },
            { text = "You and your sister cannot turn from bites or scratches.",    font = UIFont.Small,  r=0.85, g=0.85, b=0.85, pad=2  },
            { text = "You can still bleed out, get sick, and die from other causes.",font= UIFont.Small,  r=0.85, g=0.85, b=0.85, pad=12 },
            { text = "WARNING:",                                                    font = UIFont.Medium, r=1,    g=0.7,  b=0.2,  pad=4  },
            { text = "Exiting with items in her inventory may cause them to despawn.",font=UIFont.Small,  r=0.85, g=0.85, b=0.85, pad=0  },
        }

        local panel = ISPanel:new(px, py, W, H)
        panel.backgroundColor = { r = 0.05, g = 0.05, b = 0.05, a = 0.97 }
        panel.borderColor     = { r = 0.55, g = 0.55, b = 0.55, a = 1 }
        panel:initialise()
        panel:addToUIManager()
        panel:setAlwaysOnTop(true)
        panel.onRightMouseUp = function() end

        -- pause: try each B42 mechanism until one works (singleplayer only)
        pcall(function() UIManager.setShowPause(true) end)
        pcall(function() getCore():setTimeLapse(0) end)
        pcall(function() setGameSpeed(0) end)

        -- draw all text in render() so coords are panel-local
        panel.render = function(self)
            ISPanel.render(self)
            local cx = 0
            local cy = 14
            local pad = 24
            for _, row in ipairs(rows) do
                if row.divider then
                    self:drawRect(pad, cy + 4, W - pad * 2, 1, 1, 0.38, 0.38, 0.38)
                else
                    local font = row.font or UIFont.Small
                    local tw = getTextManager():MeasureStringX(font, row.text)
                    -- centre Large/Medium headers, left-align Small body
                    if font == UIFont.Large or font == UIFont.Medium then
                        cx = math.floor((W - tw) / 2)
                    else
                        cx = pad
                    end
                    self:drawText(row.text, cx, cy, row.r, row.g, row.b, 1, font)
                end
                local lineH = getTextManager():getFontHeight(row.font or UIFont.Small)
                cy = cy + lineH + (row.pad or 4)
            end
        end

        local btnW, btnH = 140, 34
        local btn = ISButton:new(math.floor((W - btnW) / 2), H - btnH - 16, btnW, btnH, "Got it", panel, function(self)
            self:setVisible(false)
        end)
        btn.backgroundColor          = { r = 0.12, g = 0.12, b = 0.12, a = 1 }
        btn.backgroundColorMouseOver = { r = 0.22, g = 0.22, b = 0.22, a = 1 }
        btn.borderColor              = { r = 0.55, g = 0.55, b = 0.55, a = 1 }
        btn:initialise()
        panel:addChild(btn)
    end)
end

local function addHaloText(player, msg)
    if not player or not msg or not HaloTextHelper or not HaloTextHelper.addText then
        return false
    end
    local color = HaloTextHelper.getColorGreen and HaloTextHelper.getColorGreen()
        or (getCore() and getCore():getGoodHighlitedColor())
    local ok = pcall(function()
        HaloTextHelper.addText(player, msg, "[br/]", color)
    end)
    if not ok then
        ok = pcall(function()
            HaloTextHelper.addText(player, msg, color)
        end)
    end
    return ok
end

local function getPlayer()
    return getSpecificPlayer(0)
end

local function formatQuestSpeech(text, player)
    if not text then return text end
    if ModpackFestivalQuests and ModpackFestivalQuests.formatQuestText then
        return ModpackFestivalQuests.formatQuestText(text, player)
    end
    return text
end

local function sayQuestLine(player, text)
    if not player or not text then return false end
    if player.Say then
        player:Say(text)
        return true
    end
    return false
end

local function queuePlayerSay(player, text, delayTicks, questId)
    if not player or not text then return end
    pendingPlayerSay = {
        player = player,
        text = formatQuestSpeech(text, player),
        questId = questId,
    }
    pendingSayTicks = delayTicks or 20
end

local function processPendingSay()
    if not pendingPlayerSay then return end
    pendingSayTicks = pendingSayTicks - 1
    if pendingSayTicks > 0 then return end
    local pending = pendingPlayerSay
    pendingPlayerSay = nil
    if sayQuestLine(pending.player, pending.text) and pending.questId then
        ModpackFestivalQuests.markStartLineSpoken(pending.questId)
    end
end

local function queueSisterSay(text, delayTicks)
    if not text or text == "" then return end
    if not ModpackFestivalSister or not ModpackFestivalSister.sayAsSister then return end
    table.insert(pendingSisterSayQueue, text)
    if delayTicks and delayTicks > 0 and (#pendingSisterSayQueue == 1) then
        pendingSisterSayTicks = delayTicks
    end
    if ModpackFestivalSister.markScriptedSpeechActive then
        ModpackFestivalSister.markScriptedSpeechActive(SISTER_SCRIPTED_SPEECH_GUARD_MS)
    end
end

local function processPendingSisterSay()
    if #pendingSisterSayQueue == 0 then return end
    pendingSisterSayTicks = pendingSisterSayTicks - 1
    if pendingSisterSayTicks > 0 then return end

    local line = table.remove(pendingSisterSayQueue, 1)
    if ModpackFestivalSister and ModpackFestivalSister.sayAsSister then
        ModpackFestivalSister.sayAsSister(line, true)
    end
    pendingSisterSayTicks = SISTER_SCRIPTED_LINE_SPACING_TICKS
end

local function getPlayerForename(player)
    if not player then return "Hey" end
    local forename = nil
    pcall(function()
        if player.getDescriptor then
            local desc = player:getDescriptor()
            if desc and desc.getForename then
                forename = desc:getForename()
            end
        end
    end)
    if forename and forename ~= "" then
        return forename
    end
    pcall(function()
        if player.getUsername then
            local uname = player:getUsername()
            if uname and uname ~= "" then
                forename = uname
            end
        end
    end)
    return (forename and forename ~= "") and forename or "Hey"
end

local function formatSisterNameCallout(player)
    local name = getPlayerForename(player)
    name = string.upper(name or "HEY")
    if name == "" then
        name = "HEY"
    end
    if not name:match("!$") then
        name = name .. "!"
    end
    return name
end

local function showCompleteMessage(player, quest)
    local msg = quest and quest.completeMessage or "Quest complete."
    if ModpackFestivalQuests.formatQuestText then
        msg = ModpackFestivalQuests.formatQuestText(msg, player)
    end
    if sayQuestLine(player, msg) then
        return
    end
    addHaloText(player, msg)
end

local function speakQuestStart(player, quest, delayTicks)
    if not quest or not quest.id or not quest.startMessage then return end
    if ModpackFestivalQuests.hasSpokenStartLine(quest.id) then return end
    queuePlayerSay(player, quest.startMessage, delayTicks or 20, quest.id)
end

local function announceQuestStart(player, quest)
    if not quest or not quest.id then return end
    speakQuestStart(player, quest, 20)
    ModpackFestivalQuestPanel.alertNewQuest(quest, player)
end

local function syncQuestCompleteToServer(player, questId)
    if not player or not questId or not sendClientCommand then return end
    sendClientCommand(player, MOD_ID, "QuestCompleted", { questId = questId })
end

local function syncQuestStartToServer(player, questId)
    if not player or not questId or not sendClientCommand then return end
    sendClientCommand(player, MOD_ID, "QuestStarted", {
        questId = questId,
        x = math.floor(player:getX()),
        y = math.floor(player:getY()),
        z = player:getZ() or 0,
    })
end

local function getQuestNavBearing(player, quest)
    if not player or not quest or not ModpackFestivalQuests.getQuestArrowRotationRad then
        return nil
    end
    if not ModpackFestivalQuests.questHasNavigationTarget(quest) then
        return nil
    end
    return ModpackFestivalQuests.getQuestArrowRotationRad(player, quest)
end

local function refreshQuestNavigation(player, quest)
    if not player or not quest or not ModpackFestivalQuests.questHasNavigationTarget(quest) then
        return
    end
    local panel = ModpackFestivalQuestPanel.instance
    if not panel or not panel:isVisible() then
        return
    end

    local dist = ModpackFestivalQuests.distToQuestTarget(player, quest)
    if dist and dist < 9000 then
        panel.distTiles = dist
    end
    panel.navBearingRad = getQuestNavBearing(player, quest)
end

local function maybeRefreshQuestNavigation(player, quest, force)
    if not player or not quest then return end
    if not ModpackFestivalQuests.questHasNavigationTarget(quest) then return end

    local now = getTimestampMs and getTimestampMs() or 0
    if force or (now - lastNavScanMs) >= NAV_SCAN_INTERVAL_MS then
        lastNavScanMs = now
        refreshQuestNavigation(player, quest)
    end
end

local function updateQuestPanel(player, quest, announceNew)
    if not quest then
        ModpackFestivalQuestPanel.hide()
        lastNavScanMs = 0
        return
    end

    local dist = nil
    local navBearing = nil
    if ModpackFestivalQuests.questHasNavigationTarget(quest) then
        dist = ModpackFestivalQuests.distToQuestTarget(player, quest)
        if dist and dist >= 9000 then
            dist = nil
        end
        navBearing = getQuestNavBearing(player, quest)
        lastNavScanMs = getTimestampMs and getTimestampMs() or 0
    else
        lastNavScanMs = 0
    end
    local remainingSec = nil
    if quest.type == "timed" and not quest.hideTimer then
        remainingSec = ModpackFestivalQuests.getTimedQuestRemainingSec(quest)
    end
    ModpackFestivalQuestPanel.show(quest, dist, remainingSec, navBearing)
    if announceNew then
        announceQuestStart(player, quest)
        syncQuestStartToServer(player, quest.id)
    end
end

local function onQuestTick()
    tickDelay = tickDelay + 1

    if pendingPlayerSay then
        processPendingSay()
    end
    processPendingSisterSay()

    local needsUi = ModpackFestivalTick.every(tickDelay, ModpackFestivalTick.UI)
    local needsGame = ModpackFestivalTick.every(tickDelay, ModpackFestivalTick.GAME)
    if not needsUi and not needsGame then
        return
    end

    local player = getPlayer()
    if not player or not player:getSquare() then return end

    if needsUi then
        local questIdEarly = ModpackFestivalQuests.getActiveQuestId()
        if questIdEarly and ModpackFestivalQuestPanel.instance
            and ModpackFestivalQuestPanel.instance:isVisible() then
            local questEarly = ModpackFestivalQuests.getDisplayQuest(questIdEarly, player)
            maybeRefreshQuestNavigation(player, questEarly, false)
        end
    end

    if not needsGame then return end

    ModpackFestivalQuests.ensureQuestLineStarted(player)

    local questId = ModpackFestivalQuests.getActiveQuestId()
    if not questId then
        updateQuestPanel(player, nil)
        return
    end

    local questDef = ModpackFestivalQuests.getDefinition(questId)
    if not questDef then return end
    local quest = ModpackFestivalQuests.getDisplayQuest(questId, player)

    if quest.startMessage and not ModpackFestivalQuests.hasSpokenStartLine(questId) then
        speakQuestStart(player, questDef, 15)
    end

    if questId == "get_to_car" then
        local cell = getCell()
        local vehicle = ModpackFestivalQuests.findFestivalVehicle(cell)
        if vehicle then
            ModpackFestivalQuests.setVehicleLocation(vehicle:getX(), vehicle:getY(), vehicle:getZ())
        end
    end

    updateQuestPanel(player, quest, false)

    if not ModpackFestivalQuests.isQuestComplete(player, questDef) then
        return
    end

    local now = getTimestampMs()
    if now - lastCompleteFlash < 1500 then
        return
    end
    lastCompleteFlash = now

    local completedId = questId
    ModpackFestivalQuests.completeQuest(completedId)
    syncQuestCompleteToServer(player, completedId)

    if completedId == "get_home" then
        if quest.completePlayerSay then
            queuePlayerSay(player, quest.completePlayerSay, 12)
        end
        -- slight delay so quest flash/dialogue finishes first
        local function doPopup()
            showEndOfQuestPopup()
        end
        local ticksUntil = 180
        local ticksSoFar = 0
        local function waitThenPopup()
            ticksSoFar = ticksSoFar + 1
            if ticksSoFar >= ticksUntil then
                Events.OnTick.Remove(waitThenPopup)
                doPopup()
            end
        end
        Events.OnTick.Add(waitThenPopup)
    elseif completedId == "meet_sister" then
        queueSisterSay(formatSisterNameCallout(player), 1)
        queueSisterSay("There's something wrong with them. There's something really wrong with them")
        queueSisterSay("Come on, Get us out of here!")
    elseif completedId == "find_sister" and quest.mallArrivalSay then
        queuePlayerSay(player, quest.mallArrivalSay, 12)
    else
        if quest.completePlayerSay then
            queuePlayerSay(player, quest.completePlayerSay, 12)
        elseif quest.showCompleteMessage ~= false and quest.completeMessage then
            pcall(function()
                showCompleteMessage(player, quest)
            end)
        end
    end

    local nextQuest = ModpackFestivalQuests.getActiveQuest(player)
    if nextQuest then
        if completedId == "meet_sister" and nextQuest.id == "get_home" then
            speakQuestStart(player, nextQuest, 300)
            ModpackFestivalQuestPanel.alertNewQuest(nextQuest, player)
        else
            announceQuestStart(player, nextQuest)
        end
    else
        ModpackFestivalQuestPanel.hide()
    end

    updateQuestPanel(player, nextQuest, false)

    print("[" .. MOD_ID .. "] quest completed: " .. completedId)
end

local function onGameStart()
    local player = getPlayer()
    if player and ModpackFestivalQuests.getSisterForename then
        ModpackFestivalQuests.getSisterForename(player)
    end
    ModpackFestivalQuests.ensureQuestLineStarted(player)
    if player then
        local active = ModpackFestivalQuests.getActiveQuest(player)
        updateQuestPanel(player, active, true)
        if active and active.startMessage and not ModpackFestivalQuests.hasSpokenStartLine(active.id) then
            speakQuestStart(player, active, 30)
        end
    end
    print("[" .. MOD_ID .. "] quest tracker active")
end

Events.OnTick.Add(onQuestTick)
Events.OnGameStart.Add(onGameStart)
