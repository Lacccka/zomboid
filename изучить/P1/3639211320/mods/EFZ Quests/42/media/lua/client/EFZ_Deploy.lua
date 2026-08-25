require "ISUI/ISModalDialog"
require "ISUI/Maps/ISWorldMap"
require "ISUI/Maps/ISMap"
require "ISUI/Maps/ISMiniMap"
require "MannequinFolks"

local config = require "EFZ_DeployConfig"
require "EFZ_DeployZombieClear"

if not EFZ then
    EFZ = {}
end

require "EFZ_Teleport"
require "EFZ_DeployExtraction"

EFZ.Deploy = EFZ.Deploy or {}
-- Deploy 활성 상태는 플레이어별로 관리한다.
-- 구버전에서는 EFZ.IsDeployActive가 boolean(전역) 이었으므로 마이그레이션/기본값을 유지한다.
EFZ.IsDeployActiveDefault = EFZ.IsDeployActiveDefault == true
if type(EFZ.IsDeployActive) ~= "table" then
    if type(EFZ.IsDeployActive) == "boolean" then
        EFZ.IsDeployActiveDefault = EFZ.IsDeployActive == true
    end
    EFZ.IsDeployActive = {}
end
local Deploy = EFZ.Deploy
local ZombieClear = EFZ and EFZ.DeployZombieClear or nil

Deploy.config = config
Deploy.destinations = config.destinations
Deploy.activePlayers = Deploy.activePlayers or {}
Deploy.activePlayerCount = 0
for _ in pairs(Deploy.activePlayers) do
    Deploy.activePlayerCount = Deploy.activePlayerCount + 1
end
Deploy.highlightedFloors = Deploy.highlightedFloors or {}
Deploy._worldMapHooked = Deploy._worldMapHooked or false
Deploy._highlightTickerAdded = Deploy._highlightTickerAdded or false
Deploy._highlightColorInfo = Deploy._highlightColorInfo or nil
Deploy._mapOverlayTexture = Deploy._mapOverlayTexture or nil
Deploy._flareSpritePath = Deploy._flareSpritePath or nil
Deploy._flareLoadHookAdded = Deploy._flareLoadHookAdded or false
Deploy._flareTickerAdded = Deploy._flareTickerAdded or false
Deploy.pendingDestinationId = Deploy.pendingDestinationId or nil
Deploy.pendingDialog = Deploy.pendingDialog or nil
Deploy.syncedZombieClearJobs = Deploy.syncedZombieClearJobs or {}
Deploy._syncedZombieClearTickerAdded = Deploy._syncedZombieClearTickerAdded or false
Deploy.pendingMultiplayerDeployTeleports = Deploy.pendingMultiplayerDeployTeleports or {}
Deploy._multiplayerDeployTickerAdded = Deploy._multiplayerDeployTickerAdded or false

local collectTargetFloors
local applyTargetHighlight
local clearTargetHighlight
local getMapOverlayTexture
local renderMapOverlayTargets
local resolveFlareSprite
local addExtractionFlareMarkers
local clearExtractionFlareMarkers
local tryAddFlareMarkerForTarget
local onGridSquareLoaded
local processPendingFlareTargets
local ensurePendingFlareTicker
local persistStateForPlayer
local restorePersistentStateForPlayer
local syncPersistentStateForPlayer
local ensureInfectionState
local processInfectionTimer
local processMultiplayerDeployTeleports
local updateCountdownMessage

local infectionConfig = {
    minMinutes = 12 * 60,
    maxMinutes = 24 * 60,
    persistIntervalMinutes = 10,
}

local function randomInfectionDelayMinutes()
    local minMinutes = infectionConfig.minMinutes or (18 * 60)
    local maxMinutes = infectionConfig.maxMinutes or (48 * 60)
    if maxMinutes < minMinutes then
        maxMinutes = minMinutes
    end
    local range = maxMinutes - minMinutes
    return (ZombRand(range + 1) + minMinutes)
end

local function getCurrentWorldAgeMinutes()
    return math.floor(getGameTime():getWorldAgeHours() * 60)
end

local function EFZDeployTriggerWorldMapRefresh()
end

local function EFZDeployHasActivePlayers()
    return (Deploy.activePlayerCount or 0) > 0
end

-- Playsquad integration:
-- - While Deploy is in progress (after teleport ~ before Extraction): psc_SetForcePaused(false)
-- - Before Deploy starts / after Extraction completes: psc_SetForcePaused(true)
-- If the Playsquad mod is absent, psc_SetForcePaused is not defined, so do nothing.
local function EFZDeploySetPlaysquadForcePaused(forcePaused)
    if type(psc_SetForcePaused) ~= "function" then
        return
    end
    local desired = forcePaused == true
    if Deploy._playsquadLastForcePaused == desired then
        return
    end
    psc_SetForcePaused(desired)
    Deploy._playsquadLastForcePaused = desired
end

local function EFZDeployRefreshPlaysquadForcePaused()
    -- During gameplay, only active Deploy should unpause Playsquad forced pause.
    EFZDeploySetPlaysquadForcePaused(not EFZDeployHasActivePlayers())
end

local function isIsoPlayer(obj)
    return obj ~= nil and instanceof(obj, "IsoPlayer")
end

local function getPlayerUsername(playerObj)
    if playerObj and playerObj.getUsername then
        return playerObj:getUsername(false, false)
    end
    return nil
end

local function resolveLocalPlayer(obj)
    if isIsoPlayer(obj) then
        return obj
    end

    if type(obj) == "table" then
        if isIsoPlayer(obj.player) then
            return obj.player
        end
        if obj.username ~= nil and type(getSpecificPlayer) == "function" then
            local activeCount = type(getNumActivePlayers) == "function" and getNumActivePlayers() or 0
            for idx = 0, activeCount - 1 do
                local candidate = getSpecificPlayer(idx)
                if isIsoPlayer(candidate) and getPlayerUsername(candidate) == obj.username then
                    return candidate
                end
            end
        end
        if obj.playerNum ~= nil and type(getSpecificPlayer) == "function" then
            local candidate = getSpecificPlayer(obj.playerNum)
            if isIsoPlayer(candidate) then
                return candidate
            end
        end
        if obj.onlineID ~= nil and type(getSpecificPlayer) == "function" then
            local activeCount = type(getNumActivePlayers) == "function" and getNumActivePlayers() or 0
            for idx = 0, activeCount - 1 do
                local candidate = getSpecificPlayer(idx)
                if isIsoPlayer(candidate) and candidate:getOnlineID() == obj.onlineID then
                    return candidate
                end
            end
        end
    end

    if isIsoPlayer(_G.player) then
        return _G.player
    end

    if type(getPlayer) == "function" then
        local localPlayer = getPlayer()
        if isIsoPlayer(localPlayer) then
            return localPlayer
        end
    end

    if type(getSpecificPlayer) == "function" then
        local activeCount = type(getNumActivePlayers) == "function" and getNumActivePlayers() or 0
        if activeCount > 0 then
            for idx = 0, activeCount - 1 do
                local candidate = getSpecificPlayer(idx)
                if isIsoPlayer(candidate) then
                    return candidate
                end
            end
        else
            local candidate = getSpecificPlayer(0)
            if isIsoPlayer(candidate) then
                return candidate
            end
        end
    end

    return nil
end

local function getDestinationById(destinationId)
    if not destinationId or not config.destinations then
        return nil
    end
    for _, destination in ipairs(config.destinations) do
        if destination.id == destinationId then
            return destination
        end
    end
    return nil
end

local function clonePoint(point)
    if not point then
        return nil
    end
    return { x = point.x, y = point.y, z = point.z or 0 }
end

local function resolveText(key, fallback)
    if key and key ~= "" and getText then
        local text = getText(key)
        if text and text ~= "" then
            return text
        end
    end
    return fallback
end

local function getWorldMapSymbolsAPI()
    if not ISWorldMap_instance then
        return nil
    end
    local mapAPI = ISWorldMap_instance.mapAPI
    if not mapAPI then
        return nil
    end
    local getter = mapAPI.getSymbolsAPI
    if type(getter) ~= "function" then
        return nil
    end
    local ok, symbolsAPI = pcall(getter, mapAPI)
    if not ok or not symbolsAPI then
        return nil
    end
    return symbolsAPI
end

local function buildDeployActiveKeyFromOnlineID(onlineID)
    return "oid:" .. tostring(onlineID)
end

local function getDeployActiveKey(playerObj)
    if not isIsoPlayer(playerObj) then
        return nil
    end
    -- MP 클라에서는 onlineID가 안정적이므로 onlineID 기반으로 관리한다.
    if isClient and isClient() and playerObj.getOnlineID then
        local onlineID = playerObj:getOnlineID()
        if onlineID ~= nil then
            return buildDeployActiveKeyFromOnlineID(onlineID)
        end
    end
    -- SP/로컬 멀티는 playerNum 기반
    if playerObj.getPlayerNum then
        return "pnum:" .. tostring(playerObj:getPlayerNum())
    end
    return nil
end

local function isDeployActiveForPlayer(playerObj)
    if not EFZ or type(EFZ.IsDeployActive) ~= "table" then
        return false
    end
    local key = getDeployActiveKey(playerObj)
    if not key then
        return EFZ.IsDeployActiveDefault == true
    end
    local stored = EFZ.IsDeployActive[key]
    if stored == nil then
        return EFZ.IsDeployActiveDefault == true
    end
    return stored == true
end

local function setDeployActiveForPlayer(playerObj, active)
    if not EFZ or type(EFZ.IsDeployActive) ~= "table" then
        return false
    end
    local key = getDeployActiveKey(playerObj)
    if not key then
        return false
    end
    EFZ.IsDeployActive[key] = active and true or false
    return true
end

local function isDeployActive(playerObj)
    if playerObj == nil and type(getPlayer) == "function" then
        playerObj = getPlayer()
    end
    return isDeployActiveForPlayer(playerObj)
end

-- "권한(Deploy 활성화)"과 별개로, 실제로 Deploy(텔레포트) 진행 중인지 판정
-- 의도: Deploy 후 ~ Extraction 완료 전까지만 특정 기능(철거하기 등)을 허용
local function isDeployInProgress(playerObj)
    if not playerObj or not playerObj.getPlayerNum then
        return false
    end
    local playerNum = playerObj:getPlayerNum()
    local state = Deploy.activePlayers and Deploy.activePlayers[playerNum] or nil
    if state then
        return true
    end
    -- 로딩 타이밍으로 activePlayers가 아직 복구되지 않았어도, modData에 active가 남아있으면 진행 중으로 간주
    local modData = playerObj.getModData and playerObj:getModData() or nil
    local saved = modData and modData.EFZDeploy
    return saved and saved.active == true
end

-- 다른 클라이언트 스크립트에서도 Deploy 진행 여부를 확인할 수 있도록 노출한다.
-- (예: Mood 관리, UI 제한 등)
Deploy.isDeployInProgress = isDeployInProgress
EFZ.IsDeployInProgress = isDeployInProgress

local function isNoClip(playerObj)
    if not playerObj then
        return false
    end
    if playerObj.isNoClip then
        return playerObj:isNoClip()
    end
    return false
end

local function onFillWorldObjectContextMenu(playerNum, context, worldobjects, test)
    if test or not context or not context.removeOptionByName then
        return
    end

    local playerObj = getSpecificPlayer(playerNum)
    if not playerObj then
        return
    end

    if isNoClip(playerObj) then
        return
    end

    -- Deploy 진행 중인 동안에만 철거하기를 허용한다.
    if isDeployInProgress(playerObj) then
        return
    end

    local destroyText = getText and getText("ContextMenu_Destroy") or "Destroy"
    context:removeOptionByName(destroyText)
    if destroyText ~= "Destroy" then
        context:removeOptionByName("Destroy")
    end
end

Events.OnFillWorldObjectContextMenu.Add(onFillWorldObjectContextMenu)

local function installWorldMapHooks()
    if Deploy._worldMapHooked then
        return
    end
    if not ISWorldMap then
        return
    end

    local originalShowWorldMap = ISWorldMap.ShowWorldMap
    ISWorldMap.ShowWorldMap = function(playerNum)
        if originalShowWorldMap then
            originalShowWorldMap(playerNum)
        end
        EFZDeployTriggerWorldMapRefresh()
    end

    local originalWorldMapRender = ISWorldMap.render
    ISWorldMap.render = function(self, ...)
        local result = nil
        if originalWorldMapRender then
            result = originalWorldMapRender(self, ...)
        end
        renderMapOverlayTargets(self)
        return result
    end

    if ISMiniMapInner and ISMiniMapInner.render then
        local originalMiniMapRender = ISMiniMapInner.render
        ISMiniMapInner.render = function(self, ...)
            local result = nil
            if originalMiniMapRender then
                result = originalMiniMapRender(self, ...)
            end
            renderMapOverlayTargets(self)
            return result
        end
    end

    Deploy._worldMapHooked = true
end


local function isPilotCharacter(characterId)
    if type(characterId) ~= "string" then
        return false
    end
    return characterId:lower() == "pilot"
end

local function isPlayerInDeployZone(playerObj)
    if not playerObj then
        return false
    end
    local x = playerObj:getX()
    local y = playerObj:getY()
    local zone = config.deployZone
    return x >= zone.minX and x <= zone.maxX and y >= zone.minY and y <= zone.maxY
end

local function isPlayerInVehicle(playerObj)
    if not playerObj then
        return false
    end
    if playerObj.getVehicle then
        local ok, vehicle = pcall(function()
            return playerObj:getVehicle()
        end)
        return ok and vehicle ~= nil
    end
    return false
end

local function getActiveLocalPlayers()
    local players = {}
    local numPlayers = getNumActivePlayers()
    for idx = 0, numPlayers - 1 do
        local playerObj = getSpecificPlayer(idx)
        if playerObj and not playerObj:isDead() then
            players[#players + 1] = playerObj
        end
    end
    return players
end

local function getPlayersWithinDeployZone()
    local result = {}
    local players = getActiveLocalPlayers()
    for _, playerObj in ipairs(players) do
        -- DeployActive 여부와 상관 없이 deployZone 내부 플레이어는 모두 Deploy 대상으로 포함한다.
        if isPlayerInDeployZone(playerObj) then
            result[#result + 1] = playerObj
        end
    end
    return result
end

local function getBodyDamage(playerObj)
    return playerObj and playerObj.getBodyDamage and playerObj:getBodyDamage() or nil
end

local function isPlayerInfected(playerObj)
    local bodyDamage = getBodyDamage(playerObj)
    if not bodyDamage then
        return false
    end
    local checks = {
        bodyDamage.isInfected,
        bodyDamage.isIsInfected,
        bodyDamage.isFakeInfected,
        bodyDamage.isIsFakeInfected,
        bodyDamage.getIsFakeInfected,
    }
    for _, checker in ipairs(checks) do
        if type(checker) == "function" then
            local ok, result = pcall(checker, bodyDamage)
            if ok and result then
                return true
            end
        end
    end
    return false
end

local function applyZombieInfection(playerObj)
    local bodyDamage = getBodyDamage(playerObj)
    if not bodyDamage then
        return false
    end

    if bodyDamage.setIsFakeInfected then
        bodyDamage:setIsFakeInfected(false)
    end
    if bodyDamage.setFakeInfectionLevel then
        bodyDamage:setFakeInfectionLevel(0)
    end
    if bodyDamage.setInfected then
        bodyDamage:setInfected(true)
    end
    if bodyDamage.setIsInfected then
        bodyDamage:setIsInfected(true)
    end
    if bodyDamage.setInfectionLevel and (not bodyDamage.getInfectionLevel or bodyDamage:getInfectionLevel() < 0.1) then
        bodyDamage:setInfectionLevel(0.1)
    end
    local gameTime = GameTime and GameTime.getInstance and GameTime:getInstance() or nil
    if gameTime and gameTime.getWorldAgeHours and bodyDamage.setInfectionTime then
        bodyDamage:setInfectionTime(gameTime:getWorldAgeHours())
    end
    if bodyDamage.setInfectionMortalityDuration and (not bodyDamage.getInfectionMortalityDuration or bodyDamage:getInfectionMortalityDuration() < 0) then
        bodyDamage:setInfectionMortalityDuration(48)
    end
    return true
end

local function resetInfectionSchedule(state)
    if not state then
        return nil
    end
    state.infection = state.infection or {}
    state.infection.minutesRemaining = randomInfectionDelayMinutes()
    state.infection.wasInfected = false
    state.infection._persistAccumulator = 0
    return state.infection
end

ensureInfectionState = function(state)
    if not state then
        return nil
    end
    local infection = state.infection
    if not infection then
        infection = {}
        state.infection = infection
    end
    if infection.active == nil then
        infection.active = true
    end
    if infection.minutesRemaining == nil and infection.wasInfected ~= true then
        infection.minutesRemaining = randomInfectionDelayMinutes()
    end
    infection._persistAccumulator = infection._persistAccumulator or 0
    return infection
end

local function buildDeploymentPayloadForPlayers(players, destinationId)
    local destination = getDestinationById(destinationId)
    if not destination then
        return nil
    end

    local payload = {
        players = {},
        destinationId = destination.id,
    }
    local deployPoints = destination.deployPoints or {}
    if #deployPoints == 0 then
        return nil
    end
    local targetPoints = destination.targetPoints or {}
    local hasTargetPoints = #targetPoints > 0

    local sharedDeploy = deployPoints[ZombRand(#deployPoints) + 1]
    if not sharedDeploy then
        return nil
    end
    local sharedTargets = nil
    if hasTargetPoints then
        sharedTargets = {}
        for _, point in ipairs(targetPoints) do
            sharedTargets[#sharedTargets + 1] = clonePoint(point)
        end
    end

    for _, playerObj in ipairs(players) do
        local entryTargets = nil
        if sharedTargets then
            entryTargets = {}
            for _, point in ipairs(sharedTargets) do
                entryTargets[#entryTargets + 1] = clonePoint(point)
            end
        end
        payload.players[#payload.players + 1] = {
            playerNum = playerObj:getPlayerNum(),
            onlineID = playerObj:getOnlineID(),
            username = getPlayerUsername(playerObj),
            destination = clonePoint(sharedDeploy),
            target = entryTargets and entryTargets[1] and clonePoint(entryTargets[1]) or nil,
            targets = entryTargets,
            destinationId = destination.id,
        }
    end
    return payload
end

local SYNC_CLEAR_DEFAULT_REMAINING = 60
local SYNC_CLEAR_DEFAULT_INTERVAL = 10

local function resolveDeployZombieClearRadius()
    local radius = config and config.deployZombieClearRadius or 0
    if ZombieClear and type(ZombieClear.resolveRadius) == "function" then
        return ZombieClear.resolveRadius(radius)
    end
    radius = tonumber(radius) or 0
    if radius < 0 then
        radius = 0
    end
    return radius
end

local function normalizeZombieClearPoint(point)
    if ZombieClear and type(ZombieClear.normalizePoint) == "function" then
        return ZombieClear.normalizePoint(point)
    end
    if not point or point.x == nil or point.y == nil then
        return nil
    end
    return {
        x = tonumber(point.x) or 0,
        y = tonumber(point.y) or 0,
        z = math.floor(tonumber(point.z) or 0),
    }
end

local function clearNearbyZombies(playerObj, radius)
    if not playerObj then
        return 0
    end
    if not ZombieClear or type(ZombieClear.clearAroundPlayer) ~= "function" then
        return 0
    end
    local removed = ZombieClear.clearAroundPlayer(playerObj, radius)
    return tonumber(removed) or 0
end

local function clearNearbyZombiesAtPoint(point, radius)
    if not ZombieClear or type(ZombieClear.clearAtPoint) ~= "function" then
        return 0, 0
    end
    return ZombieClear.clearAtPoint(point, radius)
end

local function processSyncedZombieClearJobs()
    for key, job in pairs(Deploy.syncedZombieClearJobs) do
        job.delay = (job.delay or 0) - 1
        if job.delay <= 0 then
            job.delay = job.interval or SYNC_CLEAR_DEFAULT_INTERVAL
            local _, loadedSquares = clearNearbyZombiesAtPoint(job.point, job.radius)
            if loadedSquares and loadedSquares > 0 then
                job.remaining = (job.remaining or 0) - 1
                if job.remaining <= 0 then
                    Deploy.syncedZombieClearJobs[key] = nil
                end
            end
        end
    end
end

local function ensureSyncedZombieClearTicker()
    if Deploy._syncedZombieClearTickerAdded then
        return
    end
    if not Events or not Events.OnTick or type(Events.OnTick.Add) ~= "function" then
        return
    end
    Events.OnTick.Add(processSyncedZombieClearJobs)
    Deploy._syncedZombieClearTickerAdded = true
end

local function scheduleSyncedZombieClearJob(args)
    if not args then
        return
    end
    local point = normalizeZombieClearPoint(args.point)
    if not point then
        return
    end

    local radius = args.radius
    if radius == nil then
        radius = resolveDeployZombieClearRadius()
    end
    if ZombieClear and type(ZombieClear.resolveRadius) == "function" then
        radius = ZombieClear.resolveRadius(radius)
    else
        radius = tonumber(radius) or 0
        if radius < 0 then
            radius = 0
        end
    end
    if radius <= 0 then
        return
    end

    local remaining = math.floor(tonumber(args.remaining) or SYNC_CLEAR_DEFAULT_REMAINING)
    if remaining <= 0 then
        remaining = SYNC_CLEAR_DEFAULT_REMAINING
    end
    local interval = math.floor(tonumber(args.interval) or SYNC_CLEAR_DEFAULT_INTERVAL)
    if interval <= 0 then
        interval = SYNC_CLEAR_DEFAULT_INTERVAL
    end

    local key = string.format("%d:%d:%d:%d", math.floor(point.x), math.floor(point.y), point.z, math.floor(radius))
    local job = Deploy.syncedZombieClearJobs[key]
    if job then
        job.point = { x = point.x, y = point.y, z = point.z }
        job.radius = radius
        job.remaining = math.max(job.remaining or 0, remaining)
        job.interval = job.interval or interval
        job.delay = 0
    else
        Deploy.syncedZombieClearJobs[key] = {
            point = { x = point.x, y = point.y, z = point.z },
            radius = radius,
            remaining = remaining,
            interval = interval,
            delay = 0,
        }
    end

    ensureSyncedZombieClearTicker()
end

local function finishPlayerDeployment(playerObj, entry)
    local clearRadius = resolveDeployZombieClearRadius()

    if not isClient() then
        clearNearbyZombies(playerObj, clearRadius)
    end
    local playerNum = playerObj:getPlayerNum()

    local targets = {}
    local seenTargets = {}
    local function addTargetPoint(point)
        if not point or not point.x or not point.y then
            return
        end
        local key = string.format("%d:%d:%d", math.floor(point.x or 0), math.floor(point.y or 0), math.floor(point.z or 0))
        if seenTargets[key] then
            return
        end
        seenTargets[key] = true
        targets[#targets + 1] = clonePoint(point)
    end

    if type(entry.targets) == "table" then
        for _, point in ipairs(entry.targets) do
            addTargetPoint(point)
        end
    end

    if entry.target then
        addTargetPoint(entry.target)
    end

    if #targets == 0 and entry.destinationId then
        local destinationConfig = getDestinationById(entry.destinationId)
        if destinationConfig and type(destinationConfig.targetPoints) == "table" then
            for _, point in ipairs(destinationConfig.targetPoints) do
                addTargetPoint(point)
            end
        end
    end

    local existingState = Deploy.activePlayers[playerNum]
    if existingState then
        clearTargetHighlight(existingState)
    else
        Deploy.activePlayerCount = (Deploy.activePlayerCount or 0) + 1
    end

    Deploy.activePlayers[playerNum] = {
        origin = { x = playerObj:getX(), y = playerObj:getY(), z = playerObj:getZ() },
        startedWorldAgeMinutes = getCurrentWorldAgeMinutes(),
        ghostActive = false,
        highlightEnabled = true,
        countdown = nil,
        targets = targets,
        destinationId = entry.destinationId,
        playerOnlineID = playerObj.getOnlineID and playerObj:getOnlineID() or nil,
    }
    if not isClient() and clearRadius and clearRadius > 0 then
        -- 텔레포트 직후 스트리밍 로딩 타이밍 때문에 1회 제거만으로는 누락될 수 있어 짧은 시간 동안 여러 번 재시도한다.
        Deploy.activePlayers[playerNum]._zombieClearJob = {
            remaining = 30,
            interval = 10,
            delay = 0,
            radius = clearRadius,
        }
    end
    ensureInfectionState(Deploy.activePlayers[playerNum])
    -- applyTargetHighlight(Deploy.activePlayers[playerNum])
    addExtractionFlareMarkers(Deploy.activePlayers[playerNum])
    persistStateForPlayer(playerObj, Deploy.activePlayers[playerNum])
    EFZDeployRefreshPlaysquadForcePaused()
end

local function isPlayerAtDeployDestination(playerObj, destination)
    if EFZ.Teleport and EFZ.Teleport.isPlayerAtDestination then
        return EFZ.Teleport.isPlayerAtDestination(playerObj, destination)
    end
    if not playerObj or not destination then
        return false
    end
    return math.floor(playerObj:getX()) == math.floor(destination.x)
        and math.floor(playerObj:getY()) == math.floor(destination.y)
        and math.floor(playerObj:getZ()) == math.floor(destination.z or 0)
end

local function isLocalPlayerForDeployEntry(playerObj, entry)
    if not playerObj or not entry then
        return false
    end
    if entry.onlineID ~= nil and playerObj.getOnlineID and playerObj:getOnlineID() == entry.onlineID then
        return true
    end
    if entry.username ~= nil and getPlayerUsername(playerObj) == entry.username then
        return true
    end
    return false
end

local function getMultiplayerDeployTeleportKey(playerObj, entry)
    if entry and entry.onlineID ~= nil then
        return "oid:" .. tostring(entry.onlineID)
    end
    if playerObj and playerObj.getPlayerNum then
        return "pnum:" .. tostring(playerObj:getPlayerNum())
    end
    return nil
end

local function hasPendingMultiplayerDeployTeleports()
    for _, _ in pairs(Deploy.pendingMultiplayerDeployTeleports) do
        return true
    end
    return false
end

local function stopMultiplayerDeployTeleportTickerIfIdle()
    if not Deploy._multiplayerDeployTickerAdded then
        return
    end
    if hasPendingMultiplayerDeployTeleports() then
        return
    end
    if Events and Events.OnTick and processMultiplayerDeployTeleports then
        Events.OnTick.Remove(processMultiplayerDeployTeleports)
    end
    Deploy._multiplayerDeployTickerAdded = false
end

processMultiplayerDeployTeleports = function()
    local completed = {}
    for key, pending in pairs(Deploy.pendingMultiplayerDeployTeleports) do
        local playerObj = pending.playerObj or resolveLocalPlayer({ onlineID = pending.onlineID, playerNum = pending.playerNum })
        if not playerObj or (playerObj.isDead and playerObj:isDead()) then
            completed[#completed + 1] = key
        else
            pending.ticks = (pending.ticks or 0) + 1
            if isPlayerAtDeployDestination(playerObj, pending.destination) then
                completed[#completed + 1] = key
                finishPlayerDeployment(playerObj, pending.entry)
            elseif pending.ticks > 300 then
                print(string.format("[EFZ Deploy] Multiplayer deploy teleport timed out for %s at %.2f,%.2f,%.2f -> %d,%d,%d",
                    tostring(key),
                    playerObj:getX(), playerObj:getY(), playerObj:getZ(),
                    math.floor(pending.destination.x), math.floor(pending.destination.y), math.floor(pending.destination.z or 0)))
                completed[#completed + 1] = key
            end
        end
    end

    for i = 1, #completed do
        Deploy.pendingMultiplayerDeployTeleports[completed[i]] = nil
    end
    stopMultiplayerDeployTeleportTickerIfIdle()
end

local function ensureMultiplayerDeployTeleportTicker()
    if Deploy._multiplayerDeployTickerAdded then
        return
    end
    if not Events or not Events.OnTick or not processMultiplayerDeployTeleports then
        return
    end
    Events.OnTick.Add(processMultiplayerDeployTeleports)
    Deploy._multiplayerDeployTickerAdded = true
end

local function queueMultiplayerDeployTeleport(playerObj, entry)
    local key = getMultiplayerDeployTeleportKey(playerObj, entry)
    if not key then
        return false
    end
    if isPlayerAtDeployDestination(playerObj, entry.destination) then
        finishPlayerDeployment(playerObj, entry)
        return true
    end
    Deploy.pendingMultiplayerDeployTeleports[key] = {
        playerObj = playerObj,
        playerNum = playerObj.getPlayerNum and playerObj:getPlayerNum() or nil,
        onlineID = entry.onlineID,
        entry = entry,
        destination = entry.destination,
        ticks = 0,
    }
    ensureMultiplayerDeployTeleportTicker()
    return true
end

local function startPlayerDeployment(playerObj, entry)
    if not entry or not entry.destination then
        print("[EFZ Deploy] Cannot start player deployment: missing destination.")
        return
    end

    print(string.format("[EFZ Deploy] Starting deploy teleport for playerNum=%s onlineID=%s username=%s to %s,%s,%s.",
        tostring(playerObj.getPlayerNum and playerObj:getPlayerNum() or nil),
        tostring(playerObj.getOnlineID and playerObj:getOnlineID() or nil),
        tostring(getPlayerUsername(playerObj)),
        tostring(entry.destination.x),
        tostring(entry.destination.y),
        tostring(entry.destination.z)))

    if EFZ.Teleport and EFZ.Teleport.requestLocalTeleport then
        local queued = EFZ.Teleport.requestLocalTeleport(playerObj, entry.destination, function(success)
            if success then
                finishPlayerDeployment(playerObj, entry)
            else
                print("[EFZ Deploy] Local deploy teleport callback reported failure.")
            end
        end)
        if queued then
            return
        end
        print("[EFZ Deploy] Local teleport was not queued; falling back.")
    elseif EFZ.Elevator and EFZ.Elevator.requestTeleportToDestination then
        local queued = EFZ.Elevator.requestTeleportToDestination(playerObj, entry.destination, {
            callback = function(success)
                if success then
                    finishPlayerDeployment(playerObj, entry)
                else
                    print("[EFZ Deploy] Deploy teleport callback reported failure.")
                end
            end,
        })
        if queued then
            return
        end
        print("[EFZ Deploy] Elevator teleport was not queued; falling back.")
    end

    if EFZ.Teleport and EFZ.Teleport.teleportPlayer then
        if not EFZ.Teleport.teleportPlayer(playerObj, entry.destination) then
            print("[EFZ Deploy] Fallback deploy teleport returned false.")
        end
    end
    finishPlayerDeployment(playerObj, entry)
end

collectTargetFloors = function(targets)
    if type(targets) ~= "table" or #targets == 0 then
        return {}
    end
    local cell = getCell()
    if not cell then
        return {}
    end
    local radius = config.targetRadius or 0
    local floors = {}
    local seenFloors = {}
    for _, target in ipairs(targets) do
        if target and target.x and target.y then
            local minX = math.floor(target.x - radius)
            local maxX = math.floor(target.x + radius)
            local minY = math.floor(target.y - radius)
            local maxY = math.floor(target.y + radius)
            local z = math.floor(target.z or 0)
            for x = minX, maxX do
                for y = minY, maxY do
                    local square = cell:getGridSquare(x, y, z)
                    if square then
                        local floor = square:getFloor()
                        if floor and not seenFloors[floor] then
                            seenFloors[floor] = true
                            floors[#floors + 1] = floor
                        end
                    end
                end
            end
        end
    end
    return floors
end

local defaultHighlightFallback = { r = 0.2, g = 0.85, b = 0.2, a = 0.35 }

local function clampToUnit(value, fallback)
    if type(value) ~= "number" then
        return fallback
    end
    if value < 0 then
        return 0
    elseif value > 1 then
        return 1
    end
    return value
end

local function resolveHighlightColorInfo()
    if Deploy._highlightColorInfo then
        return Deploy._highlightColorInfo
    end

    local color = config.highlightColor
    if type(color) == "table" and ColorInfo and ColorInfo.new then
        local r = clampToUnit(color.r, defaultHighlightFallback.r)
        local g = clampToUnit(color.g, defaultHighlightFallback.g)
        local b = clampToUnit(color.b, defaultHighlightFallback.b)
        local a = clampToUnit(color.a, defaultHighlightFallback.a)
        Deploy._highlightColorInfo = ColorInfo.new(r, g, b, a)
        return Deploy._highlightColorInfo
    end

    Deploy._highlightColorInfo = getCore():getGoodHighlitedColor()
    return Deploy._highlightColorInfo
end

applyTargetHighlight = function(state)
    if not state or state.highlightEnabled == false then
        return
    end

    local floors = state.highlightFloors
    if not floors or #floors == 0 then
        local targets = state.targets
        if type(targets) ~= "table" or #targets == 0 then
            state.highlightFloors = nil
            state._highlightRegistered = nil
            return
        end
        floors = collectTargetFloors(targets)
        if not floors or #floors == 0 then
            state.highlightFloors = nil
            state._highlightRegistered = nil
            return
        end
        state.highlightFloors = floors
        state._highlightRegistered = false
    end

    if state._highlightRegistered ~= true then
        for _, floor in ipairs(floors) do
            if floor and floor.setHighlighted then
                Deploy.highlightedFloors[floor] = (Deploy.highlightedFloors[floor] or 0) + 1
            end
        end
        state._highlightRegistered = true
    end

    local colorInfo = resolveHighlightColorInfo()
    for _, floor in ipairs(floors) do
        if floor and floor.setHighlighted then
            if floor.setHighlightColor then
                floor:setHighlightColor(colorInfo)
            end
            floor:setHighlighted(true, false)
        end
    end
end

local function maintainTargetHighlights()
    if not EFZDeployHasActivePlayers() then
        return
    end
    for _, state in pairs(Deploy.activePlayers) do
        if state and state.highlightEnabled ~= false then
            -- applyTargetHighlight(state)
        end
    end
end

-- if not Deploy._highlightTickerAdded then
--     Events.OnTick.Add(maintainTargetHighlights)
--     Deploy._highlightTickerAdded = true
-- end

clearTargetHighlight = function(state)
    if not state or not state.highlightFloors then
        clearExtractionFlareMarkers(state)
        return
    end
    clearExtractionFlareMarkers(state)
    for idx = #state.highlightFloors, 1, -1 do
        local floor = state.highlightFloors[idx]
        if floor and Deploy.highlightedFloors[floor] then
            local remaining = Deploy.highlightedFloors[floor] - 1
            if remaining <= 0 then
                Deploy.highlightedFloors[floor] = nil
                if floor.setHighlighted then
                    floor:setHighlighted(false)
                end
            else
                Deploy.highlightedFloors[floor] = remaining
            end
        elseif floor and floor.setHighlighted then
            floor:setHighlighted(false)
        end
        state.highlightFloors[idx] = nil
    end
    state.highlightFloors = nil
    state._highlightRegistered = nil
end

resolveFlareSprite = function()
    if Deploy._flareSpritePath ~= nil then
        if Deploy._flareSpritePath == false then
            return nil
        end
        return Deploy._flareSpritePath
    end

    local spritePath = "media/ui/exitmarker.png"
    local texture = getTexture and getTexture(spritePath) or nil
    if not texture then
        Deploy._flareSpritePath = false
        return nil
    end

    Deploy._flareSpritePath = spritePath
    return spritePath
end

local function buildTargetKey(target)
    if not target then
        return nil
    end
    local x = math.floor(target.x or 0)
    local y = math.floor(target.y or 0)
    local z = math.floor(target.z or 0)
    return string.format("%d:%d:%d", x, y, z)
end

local function ensureFlareLoadHook()
    if Deploy._flareLoadHookAdded then
        return
    end
    if not Events or not Events.LoadGridsquare or type(Events.LoadGridsquare.Add) ~= "function" then
        return
    end
    Events.LoadGridsquare.Add(function(square)
        onGridSquareLoaded(square)
    end)
    Deploy._flareLoadHookAdded = true
end

ensurePendingFlareTicker = function()
    if Deploy._flareTickerAdded then
        return
    end
    if not Events or not Events.OnTick or type(Events.OnTick.Add) ~= "function" then
        return
    end
    Events.OnTick.Add(function()
        processPendingFlareTargets()
    end)
    Deploy._flareTickerAdded = true
end

local function clearPendingIfEmpty(pending)
    if not pending then
        return true
    end
    for k in pairs(pending) do
        return false
    end
    return true
end

local function ensurePendingTable(state)
    if not state.pendingFlareTargets then
        state.pendingFlareTargets = {}
    end
    return state.pendingFlareTargets
end

local function removePendingKey(state, key)
    if not state or not key then
        return
    end
    if state.pendingFlareTargets and state.pendingFlareTargets[key] then
        state.pendingFlareTargets[key] = nil
        if clearPendingIfEmpty(state.pendingFlareTargets) then
            state.pendingFlareTargets = nil
        end
    end
end

tryAddFlareMarkerForTarget = function(state, target)
    if not state or not target then
        return false
    end

    local key = buildTargetKey(target)
    if not key then
        return false
    end

    if state.flareMarkerIds and state.flareMarkerIds[key] then
        return true
    end

    state.flareMarkerIds = state.flareMarkerIds or {}
    state.flareMarkerIds[key] = state.flareMarkerIds[key] or false

    local cell = getCell()
    if not cell then
        return false
    end

    local x = math.floor(target.x or 0) + 4
    local y = math.floor(target.y or 0) + 4
    local z = math.max(0, math.floor(target.z or 0) - 1)
    local square = cell:getGridSquare(x, y, z)
    if not square then
        return false
    end

    local isoMarkers = getIsoMarkers and getIsoMarkers()
    if not isoMarkers then
        return false
    end

    local spritePath = resolveFlareSprite()
    if not spritePath then
        return false
    end

    -- PZ 빌드/브리지 차이로 addIsoMarker 시그니처가 달라 실패할 수 있어 안전하게 여러 형태로 시도한다.
    local markerId = nil
    local function captureMarkerResult(result)
        if markerId ~= nil then
            return
        end
        if type(result) == "number" then
            markerId = result
            return
        end
        if result then
            if result.getID then
                local ok, id = pcall(function()
                    return result:getID()
                end)
                if ok and id ~= nil then
                    markerId = id
                    return
                end
            end
            if result.getId then
                local ok, id = pcall(function()
                    return result:getId()
                end)
                if ok and id ~= nil then
                    markerId = id
                    return
                end
            end
        end
    end
    local function tryCall(fn)
        if markerId ~= nil then
            return
        end
        local ok, result = pcall(fn)
        if ok then
            captureMarkerResult(result)
        end
    end

    tryCall(function()
        return isoMarkers:addIsoMarker(spritePath, square, 0.5, 0.5, 1, 0.5)
    end)

    if markerId == nil then
        return false
    end

    state.flareMarkerIds[key] = markerId
    removePendingKey(state, key)
    return true
end

addExtractionFlareMarkers = function(state)
    if not state then
        return
    end

    clearExtractionFlareMarkers(state)

    local targets = state.targets
    if type(targets) ~= "table" or #targets == 0 then
        return
    end

    ensureFlareLoadHook()
    ensurePendingFlareTicker()

    for _, target in ipairs(targets) do
        if target and target.x and target.y then
            local key = buildTargetKey(target)
            if key then
                if not tryAddFlareMarkerForTarget(state, target) then
                    local pending = ensurePendingTable(state)
                    pending[key] = clonePoint(target)
                end
            end
        end
    end
end

clearExtractionFlareMarkers = function(state)
    if not state then
        return
    end

    local isoMarkers = getIsoMarkers and getIsoMarkers()
    if state.flareMarkerIds then
        for key, markerId in pairs(state.flareMarkerIds) do
            if markerId and isoMarkers then
                local marker = isoMarkers.getIsoMarker and isoMarkers:getIsoMarker(markerId) or nil
                if isoMarkers.removeIsoMarker then
                    isoMarkers:removeIsoMarker(markerId)
                end
                if marker and marker.removeTempSquareObjects then
                    marker:removeTempSquareObjects()
                end
            end
            state.flareMarkerIds[key] = nil
        end
        state.flareMarkerIds = nil
    end

    state.pendingFlareTargets = nil
end

onGridSquareLoaded = function(square)
    if not square then
        return
    end
    if not EFZDeployHasActivePlayers() then
        return
    end

    local x = square:getX()
    local y = square:getY()
    local z = square:getZ()
    if not x or not y or not z then
        return
    end

    local key = string.format("%d:%d:%d", math.floor(x), math.floor(y), math.floor(z))

    for _, state in pairs(Deploy.activePlayers) do
        local pending = state and state.pendingFlareTargets
        if pending then
            local target = pending[key]
            if target then
                if tryAddFlareMarkerForTarget(state, target) then
                    removePendingKey(state, key)
                end
            end
        end
    end
end

processPendingFlareTargets = function()
    if not EFZDeployHasActivePlayers() then
        return
    end
    for _, state in pairs(Deploy.activePlayers) do
        local pending = state and state.pendingFlareTargets
        if pending then
            for key, target in pairs(pending) do
                if target and tryAddFlareMarkerForTarget(state, target) then
                    removePendingKey(state, key)
                end
            end
        end
    end
end

local function cloneTargetListForPersistence(targets)
    if type(targets) ~= "table" or #targets == 0 then
        return nil
    end
    local result = {}
    for _, point in ipairs(targets) do
        if point then
            local clone = clonePoint(point)
            if clone then
                result[#result + 1] = clone
            end
        end
    end
    if #result == 0 then
        return nil
    end
    return result
end

persistStateForPlayer = function(playerObj, state)
    if not playerObj or not playerObj.getModData then
        return
    end
    local modData = playerObj:getModData()
    if not modData then
        return
    end
    if not state then
        modData.EFZDeploy = nil
    else
        local payload = modData.EFZDeploy or {}
        payload.active = true
        payload.destinationId = state.destinationId
        payload.ghostActive = false
        payload.highlightEnabled = state.highlightEnabled ~= false
        payload.playerNum = playerObj.getPlayerNum and playerObj:getPlayerNum() or nil
        payload.playerOnlineID = playerObj.getOnlineID and playerObj:getOnlineID() or nil
        payload.origin = state.origin and clonePoint(state.origin) or nil
        payload.startedWorldAgeMinutes = state.startedWorldAgeMinutes
        payload.targets = cloneTargetListForPersistence(state.targets)
        if state.countdown and type(state.countdown.remaining) == "number" and state.countdown.remaining > 0 then
            payload.countdown = { remaining = state.countdown.remaining }
        else
            payload.countdown = nil
        end
        if state.infection then
            payload.infection = {
                minutesRemaining = state.infection.minutesRemaining,
                wasInfected = state.infection.wasInfected == true,
            }
        else
            payload.infection = nil
        end
        modData.EFZDeploy = payload
    end
    if playerObj.transmitModData then
        playerObj:transmitModData()
    end
end

local function clearPersistedStateForPlayer(playerObj)
    if not playerObj then
        return
    end
    persistStateForPlayer(playerObj, nil)
end

syncPersistentStateForPlayer = function(playerNum)
    if not getSpecificPlayer then
        return
    end
    local playerObj = getSpecificPlayer(playerNum)
    if not playerObj then
        return
    end
    persistStateForPlayer(playerObj, Deploy.activePlayers[playerNum])
end

restorePersistentStateForPlayer = function(playerObj)
    if not playerObj or not playerObj.getModData then
        return
    end
    local modData = playerObj:getModData()
    local saved = modData and modData.EFZDeploy
    if not saved or saved.active ~= true then
        return
    end

    local playerNum = playerObj:getPlayerNum()
    local existingState = Deploy.activePlayers[playerNum]
    if existingState then
        clearTargetHighlight(existingState)
    else
        Deploy.activePlayerCount = (Deploy.activePlayerCount or 0) + 1
    end

    local state = {
        origin = saved.origin and clonePoint(saved.origin) or { x = playerObj:getX(), y = playerObj:getY(), z = playerObj:getZ() },
        startedWorldAgeMinutes = saved.startedWorldAgeMinutes or getCurrentWorldAgeMinutes(),
        ghostActive = false,
        highlightEnabled = saved.highlightEnabled ~= false,
        countdown = nil,
        targets = {},
        destinationId = saved.destinationId,
        playerOnlineID = saved.playerOnlineID or (playerObj.getOnlineID and playerObj:getOnlineID() or nil),
        infection = nil,
    }

    local savedTargets = saved.targets
    if type(savedTargets) == "table" then
        for _, point in ipairs(savedTargets) do
            if point then
                local cloned = clonePoint(point)
                if cloned then
                    state.targets[#state.targets + 1] = cloned
                end
            end
        end
    end

    if saved.countdown and type(saved.countdown.remaining) == "number" and saved.countdown.remaining > 0 then
        state.countdown = { remaining = saved.countdown.remaining }
    end

    if saved.infection and type(saved.infection) == "table" then
        state.infection = {
            minutesRemaining = saved.infection.minutesRemaining,
            wasInfected = saved.infection.wasInfected == true,
            _persistAccumulator = 0,
        }
    end

    ensureInfectionState(state)

    Deploy.activePlayers[playerNum] = state
    state.highlightFloors = nil
    state._highlightRegistered = nil

    -- if state.highlightEnabled ~= false then
    --     applyTargetHighlight(state)
    -- end
    addExtractionFlareMarkers(state)

    if state.countdown and state.countdown.remaining and state.countdown.remaining > 0 then
        updateCountdownMessage(state.countdown.remaining)
    end

    EFZDeployTriggerWorldMapRefresh()
    persistStateForPlayer(playerObj, state)
    EFZDeployRefreshPlaysquadForcePaused()
end

local function restoreAllPersistentStates()
    if type(getNumActivePlayers) ~= "function" or type(getSpecificPlayer) ~= "function" then
        return
    end
    local count = getNumActivePlayers()
    if type(count) ~= "number" or count <= 0 then
        return
    end
    for idx = 0, count - 1 do
        local playerObj = getSpecificPlayer(idx)
        if playerObj then
            restorePersistentStateForPlayer(playerObj)
        end
    end
end

local function resolveCountdownText(minutes)
    if not minutes or minutes <= 0 then
        return nil
    end
    if getText then
        local ok, text = pcall(getText, "IGUI_EFZ_CountdownMessage", minutes)
        if ok and text and text ~= "" and text ~= "IGUI_EFZ_CountdownMessage" then
            return text
        end
    end
    return string.format("Extraction in %dm", minutes)
end

updateCountdownMessage = function(minutes)
    local message = resolveCountdownText(minutes)
    if not message then
        return
    end
    for playerNum, state in pairs(Deploy.activePlayers) do
        local playerObj = getSpecificPlayer(playerNum)
        if playerObj and not playerObj:isDead() then
            playerObj:Say(message)
        end
    end
end

local function applyDeploymentPayload(payload)
    if not payload or not payload.players then
        return
    end

    local hasLocalPlayers = false
    local isMultiplayerClient = isClient and isClient()

    for _, entry in ipairs(payload.players) do
        local targetPlayer = nil

        if isMultiplayerClient then
            -- MP에서는 서버가 보내는 playerNum이 각 클라의 로컬 인덱스(보통 0)와 충돌할 수 있어
            -- 존 밖 플레이어도 잘못 매칭되어 텔레포트되는 문제가 생길 수 있다.
            -- 따라서 MP에서는 playerNum 대신 서버와 클라이언트가 공유하는 식별자로 로컬 플레이어를 매칭한다.
            for idx = 0, getNumActivePlayers() - 1 do
                local candidate = getSpecificPlayer(idx)
                if candidate and not candidate:isDead() then
                    if isLocalPlayerForDeployEntry(candidate, entry) then
                        targetPlayer = candidate
                        break
                    end
                end
            end
        else
            -- SP/로컬: playerNum이 신뢰 가능
            if entry.playerNum ~= nil then
                targetPlayer = getSpecificPlayer(entry.playerNum)
            end

            -- fallback: playerNum이 없거나 못 찾았을 때 onlineID로 시도
            if (not targetPlayer or targetPlayer:isDead()) and entry.onlineID ~= nil then
                for idx = 0, getNumActivePlayers() - 1 do
                    local candidate = getSpecificPlayer(idx)
                    if candidate and not candidate:isDead() and candidate:getOnlineID() == entry.onlineID then
                        targetPlayer = candidate
                        break
                    end
                end
            end
        end

        if (not targetPlayer or targetPlayer:isDead()) and entry.username ~= nil then
            for idx = 0, getNumActivePlayers() - 1 do
                local candidate = getSpecificPlayer(idx)
                if candidate and not candidate:isDead() and getPlayerUsername(candidate) == entry.username then
                    targetPlayer = candidate
                    break
                end
            end
        end

        if targetPlayer and not targetPlayer:isDead() and entry.destination then
            hasLocalPlayers = true
            startPlayerDeployment(targetPlayer, entry)
        else
            local destinationText = "nil"
            if entry.destination then
                destinationText = string.format("%s,%s,%s", tostring(entry.destination.x), tostring(entry.destination.y), tostring(entry.destination.z))
            end
            print(string.format("[EFZ Deploy] Ignored SyncDeploy entry: no matching local player or destination. onlineID=%s username=%s destination=%s",
                tostring(entry.onlineID), tostring(entry.username), destinationText))
        end
    end

    if hasLocalPlayers then
        EFZDeployTriggerWorldMapRefresh()
    else
        print("[EFZ Deploy] SyncDeploy contained no entry for a local player.")
    end
end

local function resolveOverlayColor()
    local color = config.worldMapSymbol and config.worldMapSymbol.color or {}
    local r = color.r or 1.0
    local g = color.g or 1.0
    local b = color.b or 1.0
    local a = color.a or 1.0
    return r, g, b, a
end

getMapOverlayTexture = function()
    if Deploy._mapOverlayTexture ~= nil then
        return Deploy._mapOverlayTexture
    end
    local textureId = config.worldMapSymbol and config.worldMapSymbol.texture
    local texture = nil
    if textureId then
        texture = getTexture(textureId)
        if not texture then
            texture = getTexture("media/ui/" .. textureId .. ".png")
        end
        if not texture then
            texture = getTexture("media/ui/worldmap/" .. textureId .. ".png")
        end
    end
    if not texture then
        texture = getTexture("media/ui/extractionpoint.png") or getTexture("media/ui/worldmap/extractionpoint.png")
    end
    Deploy._mapOverlayTexture = texture
    return texture
end

local function drawOverlayTexture(self, api, texture, x, y, r, g, b, a)
    if not api or not texture then
        return
    end
    local uiX = api:worldToUIX(x, y)
    local uiY = api:worldToUIY(x, y)
    if not uiX or not uiY then
        return
    end
    local worldScale = api.getWorldScale and api:getWorldScale() or 1.0
    local scale = math.max(1, worldScale)
    local baseSize = (config.worldMapSymbol and config.worldMapSymbol.size) or 20
    local size = baseSize * scale
    local half = size / 2
    self:setStencilRect(0, 0, self.width, self.height)
    self:drawTextureScaled(texture, uiX - half, uiY - half, size, size, a, r, g, b)
    self:clearStencilRect()
end

renderMapOverlayTargets = function(self)
    if not EFZDeployHasActivePlayers() then
        return
    end
    local javaObject = self.javaObject
    local api = javaObject and javaObject.getAPI and javaObject:getAPI()
    if not api then
        return
    end
    local texture = getMapOverlayTexture()
    if not texture then
        return
    end
    local r, g, b, a = resolveOverlayColor()
    for _, state in pairs(Deploy.activePlayers) do
        local targets = state and state.targets
        if type(targets) == "table" then
            for _, target in ipairs(targets) do
                if target and target.x and target.y then
                    drawOverlayTexture(self, api, texture, target.x, target.y, r, g, b, a)
                end
            end
        end
    end
end

installWorldMapHooks()

local function clearPlayerState(playerNum, playerObj)
    if not playerObj and getSpecificPlayer then
        playerObj = getSpecificPlayer(playerNum)
    end
    local hadState = Deploy.activePlayers[playerNum]
    if hadState then
        clearTargetHighlight(hadState)
        clearExtractionFlareMarkers(hadState)
        Deploy.activePlayers[playerNum] = nil
        if (Deploy.activePlayerCount or 0) > 0 then
            Deploy.activePlayerCount = Deploy.activePlayerCount - 1
        end
        -- 사망/강제 종료 등으로 Deploy 상태가 정리될 때 Playsquad 강제정지를 복구한다.
        EFZDeployRefreshPlaysquadForcePaused()
    end
    if playerObj then
        clearPersistedStateForPlayer(playerObj)
    end
    EFZDeployTriggerWorldMapRefresh()
end

function EFZ.CurePlayerInfection(target)
    local playerObj = resolveLocalPlayer and resolveLocalPlayer(target) or nil
    if not isIsoPlayer(playerObj) and type(getPlayer) == "function" then
        playerObj = getPlayer()
    end
    if not isIsoPlayer(playerObj) then
        return false
    end
    EFZ.DeployExtraction.cureInfection(playerObj)
    return true
end

processInfectionTimer = function(playerObj, state)
    if not playerObj or not state then
        return
    end

    local infection = ensureInfectionState(state)
    if not infection then
        return
    end
    if infection.active == false then
        return
    end

    local infected = isPlayerInfected(playerObj)
    if infected then
        if infection.wasInfected ~= true or infection.minutesRemaining ~= nil then
            infection.wasInfected = true
            infection.minutesRemaining = nil
            infection._persistAccumulator = 0
            persistStateForPlayer(playerObj, state)
        end
        return
    end

    if infection.wasInfected == true then
        resetInfectionSchedule(state)
        persistStateForPlayer(playerObj, state)
        return
    end

    if infection.minutesRemaining then
        infection.minutesRemaining = infection.minutesRemaining - 1
        infection._persistAccumulator = (infection._persistAccumulator or 0) + 1

        if infection.minutesRemaining <= 0 then
            if applyZombieInfection(playerObj) then
                infection.wasInfected = true
                infection.minutesRemaining = nil
                infection._persistAccumulator = 0
            else
                resetInfectionSchedule(state)
            end
            persistStateForPlayer(playerObj, state)
        elseif infection._persistAccumulator >= (infectionConfig.persistIntervalMinutes or 10) then
            infection._persistAccumulator = 0
            persistStateForPlayer(playerObj, state)
        end
    end
end

local function finalizeExtraction(playerObj, playerNum)
    local state = Deploy.activePlayers[playerNum]
    if state and state.infection then
        state.infection.active = false
        state.infection.minutesRemaining = nil
        state.infection._persistAccumulator = 0
    end
    clearPlayerState(playerNum, playerObj)
    if isClient() then
        sendClientCommand("EFZ", "CompleteDeployExtraction", { onlineID = playerObj:getOnlineID() })
    end
    EFZDeployRefreshPlaysquadForcePaused()
end

local function completeExtraction(playerObj, playerNum)
    if not playerObj then
        return
    end

    local state = Deploy.activePlayers[playerNum]
    if state and state._completingExtraction then
        return
    end
    if state then
        state._completingExtraction = true
    end

    local function onExtractionComplete(success)
        local currentState = Deploy.activePlayers[playerNum]
        if not success then
            if currentState then
                currentState._completingExtraction = false
            end
            return
        end
        finalizeExtraction(playerObj, playerNum)
    end

    local options = {
        destination = config.extraction,
        callback = onExtractionComplete,
    }
    if EFZ.Elevator and EFZ.Elevator.requestTeleportToDestination then
        options.useLocalTeleport = true
    elseif not isClient() and EFZ.Teleport and EFZ.Teleport.requestLocalTeleport then
        options.useLocalTeleport = true
    end

    EFZ.DeployExtraction.completeExtraction(playerObj, options)
end

function EFZ.ImmediateExtractForCurrentPlayer()
    local playerObj = getPlayer()
    local playerNum = playerObj:getPlayerNum()
    if playerObj and not playerObj:isDead() then
        completeExtraction(playerObj, playerNum)
    end
end


local function startCountdown(playerObj, playerNum)
    local state = Deploy.activePlayers[playerNum]
    if not state then
        return
    end
    if state.countdown then
        return
    end
    state.countdown = {
        remaining = config.countdownMinutes,
    }
    persistStateForPlayer(playerObj, state)
    updateCountdownMessage(state.countdown.remaining)
end

local function cancelCountdown(playerNum)
    local state = Deploy.activePlayers[playerNum]
    if not state or not state.countdown then
        return
    end
    state.countdown = nil
    syncPersistentStateForPlayer(playerNum)
end

local function isInsideTarget(playerObj, state)
    if not state then
        return false
    end
    local targets = state.targets
    if type(targets) ~= "table" or #targets == 0 then
        state.activeTargetIndex = nil
        return false
    end
    local px = playerObj:getX()
    local py = playerObj:getY()
    local radiusSq = (config.targetRadius + 0.5) * (config.targetRadius + 0.5)
    for idx, target in ipairs(targets) do
        if target and target.x and target.y then
            local dx = px - target.x
            local dy = py - target.y
            if (dx * dx + dy * dy) <= radiusSq then
                state.activeTargetIndex = idx
                return true
            end
        end
    end
    state.activeTargetIndex = nil
    return false
end

local function tryDisableGhost(playerObj, playerNum)
    local state = Deploy.activePlayers[playerNum]
    if not state or not state.ghostActive then
        return
    end
    state.ghostActive = false
    persistStateForPlayer(playerObj, state)
end

local function updateActivePlayers()
    for playerNum, state in pairs(Deploy.activePlayers) do
        local playerObj = getSpecificPlayer(playerNum)
        if playerObj and playerObj:isDead() then
            clearPlayerState(playerNum, playerObj)
        end
    end
end

local function onPlayerUpdate(playerObj)
    if not playerObj then
        return
    end
    EFZDeployRefreshPlaysquadForcePaused()
    local playerNum = playerObj:getPlayerNum()
    local state = Deploy.activePlayers[playerNum]
    if not state then
        local modData = playerObj.getModData and playerObj:getModData() or nil
        local saved = modData and modData.EFZDeploy
        if saved and saved.active == true then
            -- restore persisted deploy state when the player updates after loading in
            restorePersistentStateForPlayer(playerObj)
            state = Deploy.activePlayers[playerNum]
        end
        if not state then
            EFZ.DeployExtraction.cureOutsideDeploy(playerObj)
            return
        end
    end

    if not isClient() then
        local job = state._zombieClearJob
        if job then
            job.delay = (job.delay or 0) - 1
            if job.delay <= 0 then
                job.delay = job.interval or 10
                local radius = job.radius or ((config and config.deployZombieClearRadius) or 8)
                if radius and radius > 0 then
                    clearNearbyZombies(playerObj, radius)
                end
                job.remaining = (job.remaining or 0) - 1
                if job.remaining <= 0 then
                    state._zombieClearJob = nil
                end
            end
        end
    end

    tryDisableGhost(playerObj, playerNum)

    if state.countdown then
        -- 차량에 탑승하거나(초기화), 타겟 범위를 벗어나면 카운트다운을 취소한다.
        if isPlayerInVehicle(playerObj) or (not isInsideTarget(playerObj, state)) then
            cancelCountdown(playerNum)
        end
    else
        if isInsideTarget(playerObj, state) and (not isPlayerInVehicle(playerObj)) then
            startCountdown(playerObj, playerNum)
        end
    end
end

local function onEveryMinute()
    for playerNum, state in pairs(Deploy.activePlayers) do
        local playerObj = getSpecificPlayer(playerNum)
        if not state then
            -- no state to process
        elseif not playerObj then
            -- player not available; keep state for restoration
        elseif playerObj:isDead() then
            clearPlayerState(playerNum, playerObj)
        elseif state.countdown then
            -- 차량에 탑승하면 카운트다운을 초기화(취소)한다.
            if isPlayerInVehicle(playerObj) then
                cancelCountdown(playerNum)
            else
                state.countdown.remaining = state.countdown.remaining - 1
                if state.countdown.remaining <= 0 then
                    completeExtraction(playerObj, playerNum)
                else
                    persistStateForPlayer(playerObj, state)
                    updateCountdownMessage(state.countdown.remaining)
                end
            end
            processInfectionTimer(playerObj, state)
        else
            processInfectionTimer(playerObj, state)
        end
    end
end

local function requestDeployment(destinationId)
    local destination = getDestinationById(destinationId)
    if not destination then
        print("[EFZ Deploy] Cannot start deploy: unknown destination " .. tostring(destinationId) .. ".")
        return false
    end

    if isClient() then
        local hasLocalInZone = false
        local localPlayers = getActiveLocalPlayers()
        for _, playerObj in ipairs(localPlayers) do
            if isPlayerInDeployZone(playerObj) then
                hasLocalInZone = true
                break
            end
        end
        if not hasLocalInZone then
            local playerObj = getPlayer and getPlayer() or nil
            if playerObj then
                print(string.format("[EFZ Deploy] Cannot start deploy: no local player in deploy zone at %.2f,%.2f,%.2f.",
                    playerObj:getX(), playerObj:getY(), playerObj:getZ()))
            else
                print("[EFZ Deploy] Cannot start deploy: no local player in deploy zone.")
            end
            return false
        end
        EFZDeployRefreshPlaysquadForcePaused()
        sendClientCommand("EFZ", "RequestDeploy", { destinationId = destination.id })
    else
        local eligiblePlayers = getPlayersWithinDeployZone()
        if #eligiblePlayers == 0 then
            local playerObj = getPlayer and getPlayer() or nil
            if playerObj then
                print(string.format("[EFZ Deploy] Cannot start deploy: no eligible player in deploy zone at %.2f,%.2f,%.2f.",
                    playerObj:getX(), playerObj:getY(), playerObj:getZ()))
            else
                print("[EFZ Deploy] Cannot start deploy: no eligible player in deploy zone.")
            end
            return false
        end
        EFZDeployRefreshPlaysquadForcePaused()
        local payload = buildDeploymentPayloadForPlayers(eligiblePlayers, destination.id)
        if payload then
            applyDeploymentPayload(payload)
        else
            print("[EFZ Deploy] Cannot start deploy: failed to build deployment payload for " .. tostring(destination.id) .. ".")
            return false
        end
    end

    Deploy.pendingDestinationId = nil
    return true
end

local _createOption = MannequinFolks.createOption
MannequinFolks.createOption = function(context, characterId)
    if _createOption then
        _createOption(context, characterId)
    end
end

local function onConfirmationClick(_, button)
    local dialog = button and button.parent or nil
    local destinationId = dialog and dialog.destinationId or Deploy.pendingDestinationId

    if button and button.internal == "YES" then
        if destinationId then
            requestDeployment(destinationId)
        end
    else
        Deploy.pendingDestinationId = nil
    end

    if Deploy.pendingDialog and dialog == Deploy.pendingDialog then
        Deploy.pendingDialog = nil
    end
end

local function openDeploymentWarningDialog(destinationId)
    if not isDeployActive() then
        return
    end
    local destination = getDestinationById(destinationId)
    if not destination then
        return
    end

    if Deploy.pendingDialog then
        Deploy.pendingDialog:removeFromUIManager()
        Deploy.pendingDialog = nil
    end

    Deploy.pendingDestinationId = destination.id

    local messageParts = {}
    local destinationDescription = resolveText(destination.descriptionKey, destination.description)
    if destinationDescription and destinationDescription ~= "" then
        messageParts[#messageParts + 1] = destinationDescription
    end
    messageParts[#messageParts + 1] = getText("IGUI_EFZ_DeployWarning")
    local message = table.concat(messageParts, "\n\n")

    local panelWidth = 420
    local panelHeight = 220
    local dialog = ISModalDialog:new(0, 0, panelWidth, panelHeight, message, true, Deploy, onConfirmationClick, nil)
    dialog:initialise()
    dialog:addToUIManager()
    if dialog.centre then
        dialog:centre()
    end
    dialog.moveWithMouse = true

    local destinationName = resolveText(destination.nameKey, destination.name or destination.id or "")
    local titleText = string.format(getText("IGUI_EFZ_DeployTitle"), destinationName)
    if dialog.setTitle then
        dialog:setTitle(titleText)
    else
        dialog.title = titleText
    end

    if dialog.yes then
        dialog.yes:setTitle(getText("IGUI_EFZ_DeployYes"))
    end
    if dialog.no then
        dialog.no:setTitle(getText("IGUI_EFZ_DeployNo"))
    end
    dialog.destinationId = destination.id

    Deploy.pendingDialog = dialog
end

function Deploy.setDeployActive(active, options)
    options = options or {}
    local fromServer = options.fromServer == true
    local value = active and true or false

    -- 서버에서 브로드캐스트(대상 없음)로 내려온 경우: 플레이어별이 아니라 기본값만 갱신한다.
    if fromServer and options.targetOnlineID == nil and options.player == nil and options.playerNum == nil then
        EFZ.IsDeployActiveDefault = value
        return
    end

    -- 서버에서 특정 onlineID 대상으로 내려준 경우(로컬 플레이어 객체가 아직 없어도 반영 가능)
    if options.targetOnlineID ~= nil and EFZ and type(EFZ.IsDeployActive) == "table" then
        EFZ.IsDeployActive[buildDeployActiveKeyFromOnlineID(options.targetOnlineID)] = value
    end

    -- MP에서 서버 동기화 패킷은 모든 클라가 수신하므로,
    -- playerNum/getPlayer()로 로컬 플레이어를 갱신하면 다른 플레이어의 상태로 오염될 수 있다.
    -- onlineID 키만 갱신하고(위), 여기서 종료한다.
    if fromServer and options.targetOnlineID ~= nil and options.player == nil and options.playerNum == nil then
        return
    end

    local playerObj = nil
    if options.player and instanceof(options.player, "IsoPlayer") then
        playerObj = options.player
    elseif options.playerNum ~= nil and type(getSpecificPlayer) == "function" then
        local candidate = getSpecificPlayer(options.playerNum)
        if candidate and instanceof(candidate, "IsoPlayer") then
            playerObj = candidate
        end
    elseif type(getPlayer) == "function" then
        local candidate = getPlayer()
        if candidate and instanceof(candidate, "IsoPlayer") then
            playerObj = candidate
        end
    end

    if playerObj then
        setDeployActiveForPlayer(playerObj, value)
    elseif options.targetOnlineID == nil then
        -- 대상이 없는 브로드캐스트는 기본값 갱신으로 취급 (구버전 호환)
        EFZ.IsDeployActiveDefault = value
    end

    if fromServer then
        return
    end

    if isClient and isClient() and sendClientCommand then
        local payload = { active = value }
        if playerObj and playerObj.getPlayerNum then
            payload.playerNum = playerObj:getPlayerNum()
        end
        sendClientCommand("EFZ", "SetDeployActive", payload)
    end
end

function Deploy.beginDeployDialogue(destinationId, options)
    local skipActiveCheck = false
    if type(options) == "table" then
        skipActiveCheck = options.skipActiveCheck == true
    end
    if not destinationId or (not skipActiveCheck and not isDeployActive()) then
        return
    end
    local destination = getDestinationById(destinationId)
    if not destination then
        return
    end
    openDeploymentWarningDialog(destination.id)
end

function EFZ.AssignDeployActive()
    if not Deploy or type(Deploy.setDeployActive) ~= "function" then
        return false
    end
    Deploy.setDeployActive(true)
    return true
end

local function triggerDeploy(destinationId)
    if not Deploy or type(requestDeployment) ~= "function" then
        return false
    end
    return requestDeployment(destinationId) == true
end

function EFZ.TriggerDeployDialogueMuldraugh()
    return triggerDeploy("muldraugh")
end

function EFZ.TriggerDeployDialogueWestpoint()
    return triggerDeploy("westpoint")
end

function EFZ.TriggerDeployDialogueBlackwood()
    return triggerDeploy("blackwood")
end

function EFZ.TriggerDeployDialogueLVHarbor()
    return triggerDeploy("LVharbor")
end

function EFZ.TriggerDeployDialogueLouisvilleW()
    return triggerDeploy("louisvilleW")
end

function EFZ.TriggerDeployDialogueLouisvilleE()
    return triggerDeploy("louisvilleE")
end

function EFZ.TriggerDeployDialogueDeltaFacility()
    return triggerDeploy("deltaFacility")
end

local function onCreatePlayerRestore(_, playerObj)
    if not playerObj then
        return
    end
    restorePersistentStateForPlayer(playerObj)
end

Events.OnPlayerUpdate.Add(onPlayerUpdate)
Events.EveryOneMinute.Add(onEveryMinute)
Events.OnCreatePlayer.Add(onCreatePlayerRestore)
Events.OnGameStart.Add(function()
    updateActivePlayers()
    restoreAllPersistentStates()
    EFZDeployRefreshPlaysquadForcePaused()
    EFZDeployTriggerWorldMapRefresh()
end)

local function onServerCommand(module, command, args)
    if module ~= "EFZ" then
        return
    end
    if command == "SyncDeploy" then
        applyDeploymentPayload(args)
        return
    elseif command == "SyncDeployZombieClear" then
        scheduleSyncedZombieClearJob(args)
        return
    elseif command == "SyncDeployActive" then
        if args and args.active ~= nil then
            Deploy.setDeployActive(args.active, {
                fromServer = true,
                targetOnlineID = args.targetOnlineID,
            })
        end
        return
    elseif command == "BeginDeployDialogue" then
        if args and args.targetOnlineID ~= nil then
            local localPlayer = getPlayer()
            if not localPlayer or localPlayer:getOnlineID() ~= args.targetOnlineID then
                return
            end
        end
        if args and args.destinationId then
            Deploy.beginDeployDialogue(args.destinationId, { skipActiveCheck = args.force == true })
        end
        return
    end
end

Events.OnServerCommand.Add(onServerCommand)

