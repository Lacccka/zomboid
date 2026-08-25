require "ISUI/ISPanel"
require "ISUI/ISButton"
require "ExtractionMode/Config"
require "ExtractionMode/Util"
require "ExtractionMode/Localization"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local Util = ExtractionMode.Util
local Localization = ExtractionMode.Localization
local HUD = ISPanel:derive("ExtractionModeHUD")
local EXPANDED_WIDTH = 500
local EXPANDED_HEIGHT = 186
local MINIMIZED_WIDTH = 210
local MINIMIZED_HEIGHT = 36
local MINIMAP_GAP = 48
local MINIMAP_BOTTOM_INSET = 14
local FALLBACK_MINIMAP_SIZE = 270
local SCREEN_MARGIN = 10

local function state()
    return ExtractionMode.ClientState or {}
end

local function localPlayer()
    return getSpecificPlayer and getSpecificPlayer(0) or (getPlayer and getPlayer())
end

local function positionLeftOfMiniMap(miniMap, width, height, screenWidth, screenHeight)
    local x = screenWidth - width - FALLBACK_MINIMAP_SIZE - MINIMAP_GAP - SCREEN_MARGIN
    local y = screenHeight - height - MINIMAP_BOTTOM_INSET - SCREEN_MARGIN
    if miniMap then
        x = miniMap:getX() - width - MINIMAP_GAP
        y = miniMap:getBottom() - height - MINIMAP_BOTTOM_INSET
    end
    x = math.max(0, math.min(math.max(0, screenWidth - width), math.floor(x)))
    y = math.max(0, math.min(math.max(0, screenHeight - height), math.floor(y)))
    return x, y
end

local function selectedTownName(data)
    if data == nil or data.selectedTownKey == nil then return nil end
    return Config.townDisplayName(data.selectedTownKey, data.selectedTownName)
end

local function hasFlare(player)
    return ExtractionMode.Client and ExtractionMode.Client.findFlare(player) ~= nil
end

local function currentSite(player)
    if player == nil then return nil end
    local square = player:getCurrentSquare()
    if square == nil or not square:isOutside() then return nil end
    for _, site in ipairs(state().extractionSites or {}) do
        if Util.playerNear(player, site, tonumber(site.radius) or 12) then return site end
    end
    return nil
end

local function nearestSiteDistance(player)
    if player == nil then return nil, nil end
    local nearest = nil
    local nearestDistance = nil
    for _, site in ipairs(state().extractionSites or {}) do
        local centerDistance = math.sqrt(Util.distanceSquaredXY(
            { x = player:getX(), y = player:getY() }, site))
        local distanceToZone = math.max(0, centerDistance - (tonumber(site.radius) or 12))
        if nearestDistance == nil or distanceToZone < nearestDistance then
            nearest = site
            nearestDistance = distanceToZone
        end
    end
    return nearest, nearestDistance
end

local function ropeDistance(player)
    local rope = state().extractionRope
    if player == nil or rope == nil then return nil end
    return math.sqrt(Util.distanceSquaredXY({ x = player:getX(), y = player:getY() }, rope))
end

local function atExtractionRope(player)
    local rope = state().extractionRope
    return rope ~= nil and player ~= nil
        and Util.playerNear(player, rope, tonumber(rope.radius) or 3)
end

local function hordeText(data)
    if data.hordeSpawned and data.hordeEventType == "BANDITS" then
        return Localization.get("IGUI_ExtractionMode_BanditForceActive", "BANDIT FORCE: ACTIVE")
    end
    if data.hordeSpawned then return Localization.get("IGUI_ExtractionMode_HordeActive", "HORDE: ACTIVE") end
    if data.hordeWindowLabel == nil then
        return Localization.get("IGUI_ExtractionMode_HordeUnknown", "HORDE: TIMING UNKNOWN")
    end
    return Localization.get("IGUI_ExtractionMode_HordeExpected", "HORDE EXPECTED: SOMETIME AFTER %1",
        tostring(data.hordeWindowLabel))
end

function HUD:onAction()
    local player = localPlayer()
    if player == nil or ExtractionMode.Client == nil then return end
    local data = state()
    if data.state == Config.STATE_HIDEOUT and data.selectedTownKey == nil then
        ExtractionMode.openTownPicker()
    elseif data.state == Config.STATE_HIDEOUT or data.state == Config.STATE_COUNTDOWN then
        if data.canJoinFactionRaid == true then
            ExtractionMode.Client.sendCommand(player, "JoinFactionRaid", {})
        elseif data.canReady ~= false then
            ExtractionMode.Client.sendCommand(player, "SetReady", { ready = data.selfReady ~= true })
        end
    elseif data.state == Config.STATE_RAID then
        if ExtractionMode.Client.campaignQuestActive(data)
            and ExtractionMode.Client.atCampaignHandoff(player, data)
            and data.campaignHandoffActive ~= true then
            if ExtractionMode.Client.findVaccineSample(player) then
                ExtractionMode.Client.sendCommand(player, "StartCampaignHandoff", {})
            else
                ExtractionMode.Client.showMessage(Localization.get(
                    "IGUI_ExtractionMode_Error_VaccineSampleRequired",
                    "Carry the Vaccine Sample onto the helipad before signaling."), true)
            end
        elseif currentSite(player) and hasFlare(player) then
            if ExtractionMode.Client.flareEquipped(player) then
                ExtractionMode.Client.showMessage(Localization.get("IGUI_ExtractionMode_FireFlareHint",
                    "Hold Aim, then press Attack to fire the extraction flare."), false)
            else
                ExtractionMode.Client.equipFlare(player)
            end
        end
    elseif data.state == Config.STATE_BOARDING and atExtractionRope(player)
        and data.boardingPendingSelf ~= true then
        if player:getVehicle() ~= nil then
            ExtractionMode.Client.showMessage(Localization.get("IGUI_ExtractionMode_Error_BoardVehicle",
                "Exit the vehicle before boarding the extraction helicopter."), true)
        else
            ExtractionMode.Client.boardExtraction(player)
        end
    end
end

function HUD:onOptOut()
    local player = localPlayer()
    if player and ExtractionMode.Client then
        ExtractionMode.Client.sendCommand(player, "SetOptOut", {
            optedOut = state().selfOptedOut ~= true,
        })
    end
end

function HUD:onTown()
    if ExtractionMode.toggleTownPicker then ExtractionMode.toggleTownPicker() end
end

function HUD:onUpgrades()
    if ExtractionMode.toggleUpgradePanel then ExtractionMode.toggleUpgradePanel() end
end

function HUD:onQuests()
    if ExtractionMode.toggleQuestPanel then ExtractionMode.toggleQuestPanel() end
end

function HUD:onDebug()
    if ExtractionMode.openDebugPanel then ExtractionMode.openDebugPanel() end
end

function HUD:onToggleMinimized()
    self.minimized = not self.minimized
    ExtractionMode.HUDMinimized = self.minimized
    if self.minimized then
        self:setWidth(MINIMIZED_WIDTH)
        self:setHeight(MINIMIZED_HEIGHT)
    else
        self:setWidth(EXPANDED_WIDTH)
        self:setHeight(EXPANDED_HEIGHT)
    end
    self.toggle:setX(self.width - 30)
    self.toggle:setTitle(self.minimized and "+" or "-")
    self.action:setX(12)
    self.action:setY(EXPANDED_HEIGHT - 37)
    self.action:setWidth(EXPANDED_WIDTH - 24)
    self.optOutAction:setY(EXPANDED_HEIGHT - 37)
    self.townButton:setX(12)
    self.townButton:setY(EXPANDED_HEIGHT - 101)
    self.townButton:setWidth(EXPANDED_WIDTH - 24)
    self.upgradeButton:setX(12)
    self.upgradeButton:setY(EXPANDED_HEIGHT - 69)
    self.upgradeButton:setWidth(math.floor((EXPANDED_WIDTH - 30) / 2))
    self.questButton:setX(18 + math.floor((EXPANDED_WIDTH - 30) / 2))
    self.questButton:setY(EXPANDED_HEIGHT - 69)
    self.questButton:setWidth(math.floor((EXPANDED_WIDTH - 30) / 2))
end

function HUD:createChildren()
    ISPanel.createChildren(self)
    self.toggle = ISButton:new(self.width - 30, 6, 22, 22, "-", self, HUD.onToggleMinimized)
    self.toggle:initialise()
    self.toggle:instantiate()
    self.toggle:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_ToggleHUD",
        "Collapse or expand raid information"))
    self:addChild(self.toggle)

    self.debugButton = ISButton:new(8, 6, 100, 22,
        Localization.get("IGUI_ExtractionMode_DebugTools", "DEBUG TOOLS"), self, HUD.onDebug)
    self.debugButton:initialise()
    self.debugButton:instantiate()
    self.debugButton:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_DebugTools",
        "Open server-authoritative Extraction Mode test actions"))
    self:addChild(self.debugButton)

    self.townButton = ISButton:new(12, EXPANDED_HEIGHT - 101, EXPANDED_WIDTH - 24, 27,
        Localization.get("IGUI_ExtractionMode_ChooseRaidDestination", "CHOOSE RAID DESTINATION"), self, HUD.onTown)
    self.townButton:initialise()
    self.townButton:instantiate()
    self:addChild(self.townButton)

    local secondaryButtonWidth = math.floor((EXPANDED_WIDTH - 30) / 2)
    self.upgradeButton = ISButton:new(12, EXPANDED_HEIGHT - 69, secondaryButtonWidth, 27,
        Localization.get("IGUI_ExtractionMode_HideoutUpgrades", "HIDEOUT UPGRADES"), self, HUD.onUpgrades)
    self.upgradeButton:initialise()
    self.upgradeButton:instantiate()
    self.upgradeButton:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_HideoutUpgrades",
        "Install permanent shared hideout improvements"))
    self:addChild(self.upgradeButton)

    self.questButton = ISButton:new(18 + secondaryButtonWidth, EXPANDED_HEIGHT - 69,
        secondaryButtonWidth, 27, Localization.get("IGUI_ExtractionMode_Contacts", "CONTACTS"), self, HUD.onQuests)
    self.questButton:initialise()
    self.questButton:instantiate()
    self.questButton:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_Contacts",
        "Review contact quests and repeatable barter trades"))
    self:addChild(self.questButton)

    self.action = ISButton:new(12, EXPANDED_HEIGHT - 37, EXPANDED_WIDTH - 24, 27,
        Localization.get("IGUI_ExtractionMode_Ready", "READY"), self, HUD.onAction)
    self.action:initialise()
    self.action:instantiate()
    self:addChild(self.action)

    self.optOutAction = ISButton:new(12, EXPANDED_HEIGHT - 37, EXPANDED_WIDTH - 24, 27,
        Localization.get("IGUI_ExtractionMode_OptOut", "OPT OUT"), self, HUD.onOptOut)
    self.optOutAction:initialise()
    self.optOutAction:instantiate()
    self:addChild(self.optOutAction)
end

function HUD:prerender()
    if self.awaitingMiniMapPosition then
        -- In multiplayer the Extraction Mode HUD can be created before vanilla
        -- finishes creating the minimap. Keep checking until it exists, but
        -- abandon the automatic move if the player has already dragged the HUD.
        if self:getX() ~= self.initialFallbackX or self:getY() ~= self.initialFallbackY then
            self.awaitingMiniMapPosition = false
        else
            local playerData = getPlayerData and getPlayerData(0)
            local miniMap = playerData and playerData.miniMap
            if miniMap then
                local x, y = positionLeftOfMiniMap(miniMap, self.width, self.height,
                    getCore():getScreenWidth(), getCore():getScreenHeight())
                self:setX(x)
                self:setY(y)
                self.awaitingMiniMapPosition = false
            end
        end
    end

    local data = state()
    local player = localPlayer()
    -- Keep the panel in the UI render cycle while multiplayer state is being
    -- synchronized. Calling setVisible(false) here is a one-way trap because a
    -- hidden ISPanel stops receiving prerender and cannot reveal itself later.
    local visible = player ~= nil
    self:setVisible(visible)
    if not visible then return end

    self.toggle:setTitle(self.minimized and "+" or "-")
    if self.minimized then
        self.action:setVisible(false)
        self.optOutAction:setVisible(false)
        self.townButton:setVisible(false)
        self.upgradeButton:setVisible(false)
        self.questButton:setVisible(false)
        self.debugButton:setVisible(false)
        ISPanel.prerender(self)
        return
    end

    self.debugButton:setVisible(data.debugEnabled == true)

    local buttonVisible = false
    local optOutVisible = false
    local townVisible = false
    local upgradeVisible = false
    local questVisible = false
    local raidReference = data.isParticipant == true and (data.state == Config.STATE_RAID
        or data.state == Config.STATE_EXTRACTING or data.state == Config.STATE_BOARDING)
    if raidReference then
        upgradeVisible = true
        questVisible = true
        self.upgradeButton:setTitle(Localization.get("IGUI_ExtractionMode_UpgradeReference",
            "UPGRADE REFERENCE"))
        self.upgradeButton:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_UpgradeReference",
            "Review unfinished hideout upgrades and their requirements. Read-only during raids."))
        self.questButton:setTitle(Localization.get("IGUI_ExtractionMode_QuestReference",
            "QUEST REFERENCE"))
        self.questButton:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_QuestReference",
            "Review active quests, requirements, objectives, and full briefings. Read-only during raids."))
    else
        self.upgradeButton:setTitle(Localization.get("IGUI_ExtractionMode_HideoutUpgrades",
            "HIDEOUT UPGRADES"))
        self.upgradeButton:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_HideoutUpgrades",
            "Install permanent shared hideout improvements"))
        self.questButton:setTitle(Localization.get("IGUI_ExtractionMode_Contacts", "CONTACTS"))
        self.questButton:setTooltip(Localization.get("IGUI_ExtractionMode_Tooltip_Contacts",
            "Review contact quests and repeatable barter trades"))
    end
    if data.state == nil then
        line1 = Localization.get("IGUI_ExtractionMode_Connecting", "CONNECTING TO EXTRACTION AUTHORITY...")
        line2 = Localization.get("IGUI_ExtractionMode_Synchronizing",
            "Synchronizing shared hideout and raid state.")
    elseif data.isParticipant ~= true and (data.state == Config.STATE_RAID
        or data.state == Config.STATE_EXTRACTING or data.state == Config.STATE_BOARDING) then
        -- Extracted survivors and late joiners both remain safely in the
        -- hideout until the current participants finish the raid.
        if data.lateJoinPendingSelf == true then
            buttonVisible = true
            self.action:setTitle(Localization.get("IGUI_ExtractionMode_JoiningRaidButton",
                "JOINING RAID: %1s", tostring(data.lateJoinSeconds or 0)))
            self.action.enable = false
        end
    elseif data.state == Config.STATE_HIDEOUT then
        townVisible = true
        upgradeVisible = true
        questVisible = true
        self.townButton:setTitle(data.selectedTownKey
            and Localization.get("IGUI_ExtractionMode_DestinationChange", "DESTINATION: %1  |  CHANGE",
                selectedTownName(data))
            or Localization.get("IGUI_ExtractionMode_ChooseRaidDestination", "CHOOSE RAID DESTINATION"))
        self.townButton.enable = true
        buttonVisible = data.selectedTownKey ~= nil
        self.action:setTitle(data.canJoinFactionRaid == true
            and Localization.get("IGUI_ExtractionMode_JoinRaid", "JOIN RAID")
            or (data.vehicleInsertionActive == true and data.vehicleInsertionHasDriver ~= true
                and Localization.get("IGUI_ExtractionMode_DriverRequired", "DRIVER REQUIRED")
            or (data.canReady == false
                and Localization.get("IGUI_ExtractionMode_EnterInsertionVehicle", "ENTER INSERTION VEHICLE")
            or (data.selfReady
                and Localization.get("IGUI_ExtractionMode_CancelReady", "CANCEL READY")
                or Localization.get("IGUI_ExtractionMode_ReadyForRaid", "READY FOR RAID")))))
        self.action.enable = data.canJoinFactionRaid == true or data.canReady ~= false
        optOutVisible = data.canOptOut == true and data.canJoinFactionRaid ~= true
    elseif data.state == Config.STATE_COUNTDOWN then
        buttonVisible = true
        self.action:setTitle(data.vehicleInsertionActive == true and data.vehicleInsertionHasDriver ~= true
            and Localization.get("IGUI_ExtractionMode_DriverRequired", "DRIVER REQUIRED")
            or (data.canReady == false
            and Localization.get("IGUI_ExtractionMode_EnterInsertionVehicle", "ENTER INSERTION VEHICLE")
            or (data.selfReady
            and Localization.get("IGUI_ExtractionMode_CancelReady", "CANCEL READY")
            or Localization.get("IGUI_ExtractionMode_ReadyForRaid", "READY FOR RAID"))))
        self.action.enable = data.canReady ~= false
        optOutVisible = data.canOptOut == true
    elseif data.state == Config.STATE_RAID then
        local atHandoff = ExtractionMode.Client.campaignQuestActive(data)
            and ExtractionMode.Client.atCampaignHandoff(player, data)
        if atHandoff then
            buttonVisible = true
            self.action:setTitle(data.campaignHandoffActive
                and Localization.get("IGUI_ExtractionMode_VaccineHelicopterInboundButton",
                    "VACCINE HELICOPTER INBOUND: %1s", tostring(data.campaignHandoffSeconds or 0))
                or Localization.get("IGUI_ExtractionMode_SignalVaccineHelicopter",
                    "SIGNAL VACCINE HELICOPTER"))
            self.action.enable = data.campaignHandoffActive ~= true
                and ExtractionMode.Client.findVaccineSample(player) ~= nil
        else
            local site = currentSite(player)
            if site then
                buttonVisible = true
                if hasFlare(player) and ExtractionMode.Client.flareEquipped(player) then
                    self.action:setTitle(Localization.get("IGUI_ExtractionMode_AimFireFlare",
                        "AIM + FIRE FLARE AT E%1", tostring(site.id)))
                elseif hasFlare(player) then
                    self.action:setTitle(Localization.get("IGUI_ExtractionMode_EquipFlareGun",
                        "EQUIP FLARE GUN FOR E%1", tostring(site.id)))
                else
                    self.action:setTitle(Localization.get("IGUI_ExtractionMode_PickUpEmergencyFlare",
                        "PICK UP EMERGENCY FLARE AT E%1", tostring(site.id)))
                end
                self.action.enable = hasFlare(player)
            end
        end
    elseif data.state == Config.STATE_BOARDING and atExtractionRope(player) then
        buttonVisible = true
        self.action:setTitle(player:getVehicle() ~= nil
            and Localization.get("IGUI_ExtractionMode_ExitVehicleToBoard", "EXIT VEHICLE TO BOARD")
            or (data.boardingPendingSelf
                and Localization.get("IGUI_ExtractionMode_BoardingHelicopter", "BOARDING EXTRACTION HELICOPTER...")
                or Localization.get("IGUI_ExtractionMode_BoardHelicopter", "BOARD EXTRACTION HELICOPTER")))
        self.action.enable = data.boardingPendingSelf ~= true and player:getVehicle() == nil
    end
    if optOutVisible then
        local gap = 6
        local width = math.floor((EXPANDED_WIDTH - 24 - gap) / 2)
        self.action:setWidth(width)
        self.optOutAction:setX(12 + width + gap)
        self.optOutAction:setWidth(EXPANDED_WIDTH - 24 - gap - width)
        self.optOutAction:setTitle(data.selfOptedOut == true
            and Localization.get("IGUI_ExtractionMode_CancelOptOut", "CANCEL OPT OUT")
            or Localization.get("IGUI_ExtractionMode_OptOut", "OPT OUT"))
        self.optOutAction.enable = true
    else
        self.action:setX(12)
        self.action:setWidth(EXPANDED_WIDTH - 24)
    end
    self.action:setVisible(buttonVisible)
    self.optOutAction:setVisible(optOutVisible)
    self.townButton:setVisible(townVisible)
    self.upgradeButton:setVisible(upgradeVisible)
    self.questButton:setVisible(questVisible)

    ISPanel.prerender(self)
end

function HUD:render()
    ISPanel.render(self)
    if self.minimized then
        self:drawText(Localization.get("IGUI_ExtractionMode_Title", "EXTRACTION MODE"),
            10, 10, 0.96, 0.72, 0.18, 1, UIFont.Small)
        return
    end
    local data = state()
    local title = Localization.get("IGUI_ExtractionMode_Title", "EXTRACTION MODE")
    local line1 = ""
    local line2 = ""

    if data.isParticipant == true and data.groundExtractionPendingSelf == true then
        if data.groundExtractionPhaseSelf == "FADING" then
            line1 = Localization.get("IGUI_ExtractionMode_GroundExtractionFading",
                "EXTRACTING FROM RAID...")
            line2 = Localization.get("IGUI_ExtractionMode_GroundExtractionStandBy",
                "Leaving the raid area. Stand by...")
        else
            line1 = Localization.get("IGUI_ExtractionMode_GroundExtractionCountdown",
                "LEAVING RAID AREA: %1s", tostring(data.groundExtractionSeconds or 0))
            line2 = Localization.get("IGUI_ExtractionMode_GroundExtractionCancelHint",
                "Return inside the marked boundary to cancel extraction.")
        end
    elseif data.isParticipant ~= true and (data.state == Config.STATE_RAID
        or data.state == Config.STATE_EXTRACTING or data.state == Config.STATE_BOARDING) then
        if data.lateJoinPendingSelf == true then
            line1 = Localization.get("IGUI_ExtractionMode_JoiningRaidCountdown",
                "JOINING FACTION RAID IN %1s", tostring(data.lateJoinSeconds or 0))
            line2 = data.lateJoinPhaseSelf == "ARRIVING"
                and Localization.get("IGUI_ExtractionMode_InsertionProgress", "INSERTION IN PROGRESS")
                or Localization.get("IGUI_ExtractionMode_JoiningRaidStayInside",
                    "Remain inside the hideout until insertion.")
        elseif data.hasExtracted == true then
            line1 = Localization.get("IGUI_ExtractionMode_ExtractedSafe", "EXTRACTED  |  SAFE IN HIDEOUT")
            line2 = Localization.get("IGUI_ExtractionMode_RaidStillActive",
                "The current raid remains active for survivors still in the field.")
        else
            line1 = Localization.get("IGUI_ExtractionMode_RaidInProgressSafe",
                "RAID IN PROGRESS  |  SAFE IN HIDEOUT")
            line2 = Localization.get("IGUI_ExtractionMode_NotDeployed",
                "Not deployed in this raid. Wait for active survivors to return.")
        end
    elseif data.state == Config.STATE_HIDEOUT then
        line1 = Localization.get("IGUI_ExtractionMode_HideoutDestination", "HIDEOUT  |  DESTINATION: %1",
            selectedTownName(data) or Localization.get("IGUI_ExtractionMode_NotSelected", "NOT SELECTED"))
        if data.selectedTownKey then
            if data.canJoinFactionRaid == true then
                line2 = Localization.get("IGUI_ExtractionMode_ActiveFactionRaidSelected",
                    "Your faction is raiding this town. Join them or choose another destination.")
            elseif data.selfOptedOut == true then
                line2 = Localization.get("IGUI_ExtractionMode_OptedOutStatus",
                    "OPTED OUT  |  You will remain in the hideout when this team deploys.")
            elseif data.vehicleInsertionActive == true and data.vehicleInsertionHasDriver ~= true then
                line2 = Localization.get("IGUI_ExtractionMode_DriverRequiredHint",
                    "A driver must occupy the driver seat before anyone can ready.")
            elseif data.vehicleInsertionActive == true and data.vehicleInsertionMember ~= true then
                line2 = Localization.get("IGUI_ExtractionMode_EnterInsertionVehicleHint",
                    "Enter the selected vehicle to join its insertion roster.")
            elseif data.vehicleInsertionActive == true then
                line2 = Localization.get("IGUI_ExtractionMode_VehicleReadyFuel",
                    "VEHICLE READY %1/%2  |  FUEL %3/%4 L", tostring(data.readyCount or 0),
                    tostring(data.requiredCount or 0),
                    string.format("%.2f", tonumber(data.vehicleInsertionFuel) or 0),
                    string.format("%.2f", tonumber(data.vehicleInsertionFuelRequired) or 0))
            else
                line2 = Localization.get("IGUI_ExtractionMode_ReadyCountDeploy",
                    "READY %1/%2  |  Unready teammates may opt out.", tostring(data.readyCount or 0),
                    tostring(data.requiredCount or 0))
            end
        else
            line2 = Localization.get("IGUI_ExtractionMode_ChooseTownHint",
                "Choose a town to prepare a randomized insertion route.")
        end
    elseif data.state == Config.STATE_COUNTDOWN then
        line1 = Localization.get("IGUI_ExtractionMode_DeployingIn", "DEPLOYING TO %1 IN %2s",
            selectedTownName(data) or Localization.get("IGUI_ExtractionMode_Raid", "RAID"),
            tostring(data.countdownSeconds or 0))
        line2 = data.vehicleInsertionActive == true
            and Localization.get("IGUI_ExtractionMode_VehicleReadyFuel",
                "VEHICLE READY %1/%2  |  FUEL %3/%4 L", tostring(data.readyCount or 0),
                tostring(data.requiredCount or 0),
                string.format("%.2f", tonumber(data.vehicleInsertionFuel) or 0),
                string.format("%.2f", tonumber(data.vehicleInsertionFuelRequired) or 0))
            or Localization.get("IGUI_ExtractionMode_ReadyCount", "READY %1/%2",
                tostring(data.readyCount or 0), tostring(data.requiredCount or 0))
    elseif data.state == Config.STATE_TRANSIT then
        line1 = Localization.get("IGUI_ExtractionMode_InsertionProgress", "INSERTION IN PROGRESS  |  %1",
            selectedTownName(data) or Localization.get("IGUI_ExtractionMode_RaidZone", "RAID ZONE"))
        line2 = Localization.get("IGUI_ExtractionMode_StandBy", "Stand by...")
    elseif data.state == Config.STATE_RAID then
        if data.campaignHandoffActive then
            line1 = Localization.get("IGUI_ExtractionMode_VaccineHandoffCountdown",
                "VACCINE HELICOPTER INBOUND: %1s", tostring(data.campaignHandoffSeconds or 0))
            line2 = Localization.get("IGUI_ExtractionMode_HoldMallRoof",
                "The Vaccine Sample carrier must remain on the helipad until the pilot arrives.")
        else
            line1 = Localization.get("IGUI_ExtractionMode_RaidActive", "RAID ACTIVE  |  %1", hordeText(data))
            local site = currentSite(localPlayer())
            if ExtractionMode.Client.campaignQuestActive(data)
                and ExtractionMode.Client.atCampaignHandoff(localPlayer(), data) then
                line2 = ExtractionMode.Client.findVaccineSample(localPlayer())
                    and Localization.get("IGUI_ExtractionMode_AtVaccineHelipad",
                        "AT HELIPAD  |  Signal the vaccine helicopter when ready.")
                    or Localization.get("IGUI_ExtractionMode_NeedVaccineAtHelipad",
                        "AT HELIPAD  |  A Vaccine Sample is required.")
            elseif site then
            if hasFlare(localPlayer()) then
                line2 = Localization.get("IGUI_ExtractionMode_InsideExtractWithFlare",
                    "INSIDE E%1  |  Equip the flare gun, hold Aim, then press Attack.", tostring(site.id))
            else
                line2 = Localization.get("IGUI_ExtractionMode_InsideExtractFindFlare",
                    "INSIDE E%1  |  Find the emergency flare near the site center.", tostring(site.id))
            end
            else
                local nearest, distance = nearestSiteDistance(localPlayer())
                if nearest and distance and distance <= Config.EXTRACTION_PROXIMITY_REVEAL_DISTANCE then
                    line2 = Localization.get("IGUI_ExtractionMode_ExtractionNearby",
                        "EXTRACTION NEARBY  |  E%1 is about %2 tiles away.", tostring(nearest.id),
                        tostring(math.max(1, math.ceil(distance))))
                else
                    line2 = ""
                end
            end
        end
    elseif data.state == Config.STATE_EXTRACTING then
        line1 = Localization.get("IGUI_ExtractionMode_HelicopterInbound", "HELICOPTER INBOUND: %1s",
            tostring(data.extractionSeconds or 0))
        line2 = Localization.get("IGUI_ExtractionMode_MoveToExtraction",
            "Move to E%1. A boarding line will deploy on arrival.", tostring(data.activeExtraction or "?"))
    elseif data.state == Config.STATE_BOARDING then
        line1 = Localization.get("IGUI_ExtractionMode_LineDeployed", "EXTRACTION LINE DEPLOYED: %1s",
            tostring(data.boardingSeconds or 0))
        if data.boardingPendingSelf then
            line2 = Localization.get("IGUI_ExtractionMode_BoardingShort", "BOARDING HELICOPTER...")
        elseif atExtractionRope(localPlayer()) then
            line2 = Localization.get("IGUI_ExtractionMode_AtExtractionLine",
                "AT EXTRACTION LINE  |  Board now before the helicopter departs.")
        else
            local distance = ropeDistance(localPlayer())
            line2 = distance and Localization.get("IGUI_ExtractionMode_ReachLineDistance",
                "REACH THE EXTRACTION LINE  |  About %1 tiles away.",
                tostring(math.max(1, math.ceil(distance))))
                or Localization.get("IGUI_ExtractionMode_ReachLine",
                    "Reach the marked extraction line and board individually.")
        end
    end

    self:drawTextCentre(title, self.width / 2, 10, 0.96, 0.72, 0.18, 1, UIFont.Medium)
    self:drawTextCentre(line1, self.width / 2, 37, 1, 1, 1, 1, UIFont.Small)
    self:drawTextCentre(line2, self.width / 2, 58, 0.82, 0.82, 0.82, 1, UIFont.Small)
end

function HUD:new()
    local minimized = ExtractionMode.HUDMinimized == true
    local width = minimized and MINIMIZED_WIDTH or EXPANDED_WIDTH
    local height = minimized and MINIMIZED_HEIGHT or EXPANDED_HEIGHT
    local screenWidth = getCore():getScreenWidth()
    local screenHeight = getCore():getScreenHeight()
    local playerData = getPlayerData and getPlayerData(0)
    local miniMap = playerData and playerData.miniMap
    -- Reserve the normal minimap footprint even when multiplayer startup has
    -- not created it yet. This prevents the first frame from covering the map.
    local x, y = positionLeftOfMiniMap(miniMap, width, height, screenWidth, screenHeight)
    local object = ISPanel:new(x, y, width, height)
    setmetatable(object, self)
    self.__index = self
    object.background = true
    object.backgroundColor = { r = 0.03, g = 0.035, b = 0.04, a = 0.82 }
    object.borderColor = { r = 0.82, g = 0.58, b = 0.16, a = 0.8 }
    object.moveWithMouse = true
    object.minimized = minimized
    object.awaitingMiniMapPosition = miniMap == nil
    object.initialFallbackX = x
    object.initialFallbackY = y
    return object
end

function ExtractionMode.createHUD()
    if ExtractionMode.HUDInstance then return ExtractionMode.HUDInstance end
    local hud = HUD:new()
    hud:initialise()
    hud:addToUIManager()
    hud:setAlwaysOnTop(true)
    ExtractionMode.HUDInstance = hud
    return hud
end

ExtractionMode.HUD = HUD
return HUD
