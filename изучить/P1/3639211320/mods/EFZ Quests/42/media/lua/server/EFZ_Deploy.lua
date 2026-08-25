if not (isServer and isServer()) then
    return
end

local config = require "EFZ_DeployConfig"
require "EFZ_DeployExtraction"
require "EFZ_DeployZombieClear"

if not EFZ then
    EFZ = {}
end

EFZ.DeployServer = EFZ.DeployServer or {}
local Server = EFZ.DeployServer

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
local DeployInterface = EFZ.Deploy
local ZombieClear = EFZ and EFZ.DeployZombieClear or nil

local function isIsoPlayer(obj)
    return obj ~= nil and instanceof(obj, "IsoPlayer")
end

local function getPlayerUsername(playerObj)
    if playerObj and playerObj.getUsername then
        return playerObj:getUsername(false, false)
    end
    return nil
end

local function buildDeployActiveKeyFromOnlineID(onlineID)
    return "oid:" .. tostring(onlineID)
end

local function getDeployActiveKey(playerObj)
    if not isIsoPlayer(playerObj) then
        return nil
    end
    if playerObj.getOnlineID then
        local onlineID = playerObj:getOnlineID()
        if onlineID ~= nil then
            return buildDeployActiveKeyFromOnlineID(onlineID)
        end
    end
    if playerObj.getPlayerNum then
        return "pnum:" .. tostring(playerObj:getPlayerNum())
    end
    return nil
end

local function isDeployActiveForPlayer(playerObj)
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
    local key = getDeployActiveKey(playerObj)
    if not key then
        return false
    end
    EFZ.IsDeployActive[key] = active and true or false
    return true
end

local function resolvePlayer(obj)
    if isIsoPlayer(obj) then
        return obj
    end
    if type(obj) == "table" then
        if isIsoPlayer(obj.player) then
            return obj.player
        end
        if obj.playerNum ~= nil and type(getSpecificPlayer) == "function" then
            local candidate = getSpecificPlayer(obj.playerNum)
            if isIsoPlayer(candidate) then
                return candidate
            end
        end
        if obj.onlineID ~= nil then
            local onlinePlayers = getOnlinePlayers and getOnlinePlayers() or nil
            if onlinePlayers then
                for i = 0, onlinePlayers:size() - 1 do
                    local candidate = onlinePlayers:get(i)
                    if candidate and candidate:getOnlineID() == obj.onlineID then
                        return candidate
                    end
                end
            end
        end
    end
    if isIsoPlayer(_G.player) then
        return _G.player
    end
    return nil
end

function EFZ.AssignDeployActive()
    local target = resolvePlayer(player)
    if not target and type(getSpecificPlayer) == "function" then
        target = getSpecificPlayer(0)
    end
    Server.setDeployActive(true, target)
    return true
end

local function syncDeployActive(targetPlayer, active)
    local args = {
        active = active and true or false,
        targetOnlineID = nil,
    }
    if isIsoPlayer(targetPlayer) then
        args.targetOnlineID = targetPlayer:getOnlineID()
        if targetPlayer.getPlayerNum then
            args.playerNum = targetPlayer:getPlayerNum()
        end
    end
    sendServerCommand("EFZ", "SyncDeployActive", args)
end

function Server.setDeployActive(active, playerObj)
    local value = active and true or false
    local resolved = resolvePlayer(playerObj)
    if resolved then
        setDeployActiveForPlayer(resolved, value)
        syncDeployActive(resolved, value)
    else
        -- 대상이 없으면 기본값(전체 기본)을 갱신하는 것으로 처리 (구버전 호환/관리자 용도)
        EFZ.IsDeployActiveDefault = value
        syncDeployActive(nil, value)
    end
end

function Server.beginDeployDialogue(destinationId, playerObj)
    if not destinationId then
        return
    end
    local resolved = resolvePlayer(playerObj)
    if not resolved then
        return
    end
    if not isDeployActiveForPlayer(resolved) then
        syncDeployActive(resolved, false)
        return
    end
    local args = {
        destinationId = destinationId,
        targetOnlineID = resolved:getOnlineID(),
        force = true,
    }
    if resolved.getPlayerNum then
        args.playerNum = resolved:getPlayerNum()
    end
    sendServerCommand("EFZ", "BeginDeployDialogue", args)
end

DeployInterface.setDeployActive = Server.setDeployActive
DeployInterface.beginDeployDialogue = Server.beginDeployDialogue

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

-- Server-authoritative zombie clearing right after deploy teleport.
local DeployZombieClearJobs = {}
local DeployZombieClearTickerAdded = false
local DEPLOY_CLEAR_DEFAULT_REMAINING = 60
local DEPLOY_CLEAR_DEFAULT_INTERVAL = 10

local function resolveDeployZombieClearRadius()
    if ZombieClear and type(ZombieClear.resolveRadius) == "function" then
        return ZombieClear.resolveRadius(config and config.deployZombieClearRadius or 0)
    end
    return 0
end

local function clearNearbyZombiesAtPoint(point, radius)
    if not ZombieClear or type(ZombieClear.clearAtPoint) ~= "function" then
        return 0, 0
    end
    return ZombieClear.clearAtPoint(point, radius)
end

local function syncDeployZombieClear(point, radius, remaining, interval)
    local sendPoint = clonePoint(point)
    if not sendPoint then
        return
    end
    sendServerCommand("EFZ", "SyncDeployZombieClear", {
        point = sendPoint,
        radius = radius,
        remaining = remaining,
        interval = interval,
    })
end

local function processDeployZombieClearJobs()
    for key, job in pairs(DeployZombieClearJobs) do
        job.delay = (job.delay or 0) - 1
        if job.delay <= 0 then
            job.delay = job.interval or DEPLOY_CLEAR_DEFAULT_INTERVAL
            local _, loadedSquares = clearNearbyZombiesAtPoint(job.point, job.radius)
            -- 스트리밍/청크 로딩 전에는 gridSquare가 nil로 떨어질 수 있어 로딩된 스퀘어가 하나라도 생겼을 때부터 remaining을 줄인다.
            if loadedSquares and loadedSquares > 0 then
                job.remaining = (job.remaining or 0) - 1
                if job.remaining <= 0 then
                    DeployZombieClearJobs[key] = nil
                end
            end
        end
    end
end

local function ensureDeployZombieClearTicker()
    if DeployZombieClearTickerAdded then
        return
    end
    if not Events or not Events.OnTick or type(Events.OnTick.Add) ~= "function" then
        return
    end
    Events.OnTick.Add(processDeployZombieClearJobs)
    DeployZombieClearTickerAdded = true
end

local function scheduleDeployZombieClear(point, radius)
    if not point then
        return
    end
    local normalizedPoint = nil
    if ZombieClear and type(ZombieClear.normalizePoint) == "function" then
        normalizedPoint = ZombieClear.normalizePoint(point)
    else
        normalizedPoint = clonePoint(point)
    end
    if not normalizedPoint then
        return
    end
    radius = tonumber(radius) or 0
    if ZombieClear and type(ZombieClear.resolveRadius) == "function" then
        radius = ZombieClear.resolveRadius(radius)
    end
    if radius <= 0 then
        return
    end

    local x = tonumber(normalizedPoint.x) or 0
    local y = tonumber(normalizedPoint.y) or 0
    local z = math.floor(tonumber(normalizedPoint.z) or 0)
    local key = string.format("%d:%d:%d:%d", math.floor(x), math.floor(y), z, math.floor(radius))
    local remaining = DEPLOY_CLEAR_DEFAULT_REMAINING
    local interval = DEPLOY_CLEAR_DEFAULT_INTERVAL

    local job = DeployZombieClearJobs[key]
    if job then
        job.point = { x = x, y = y, z = z }
        job.radius = radius
        job.remaining = math.max(job.remaining or 0, remaining)
        job.interval = job.interval or interval
        job.delay = 0
    else
        DeployZombieClearJobs[key] = {
            point = { x = x, y = y, z = z },
            radius = radius,
            remaining = remaining,
            interval = interval,
            delay = 0,
        }
    end

    ensureDeployZombieClearTicker()
    syncDeployZombieClear({ x = x, y = y, z = z }, radius, remaining, interval)
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

local function handleDeployRequest(destinationId, requestPlayer)
    local destination = getDestinationById(destinationId)
    if not destination then
        return
    end

    -- DeployActive 여부와 상관 없이 deployZone 내부에서 요청한 경우 처리한다.
    if requestPlayer and (not isIsoPlayer(requestPlayer) or requestPlayer:isDead() or (not isPlayerInDeployZone(requestPlayer))) then
        if requestPlayer and requestPlayer.getX then
            print(string.format("[EFZ Deploy] Server rejected deploy request: requester outside deploy zone at %.2f,%.2f,%.2f.",
                requestPlayer:getX(), requestPlayer:getY(), requestPlayer:getZ()))
        else
            print("[EFZ Deploy] Server rejected deploy request: invalid requester.")
        end
        return
    end

    local onlinePlayers = getOnlinePlayers()
    if not onlinePlayers then
        return
    end

    local deployPoints = destination.deployPoints or {}
    if #deployPoints == 0 then
        return
    end
    local targetPoints = destination.targetPoints or {}
    local hasTargetPoints = #targetPoints > 0

    local payload = {
        players = {},
        destinationId = destination.id,
    }

    local sharedSpawn = clonePoint(deployPoints[ZombRand(#deployPoints) + 1])
    if not sharedSpawn then
        return
    end
    local sharedTargets = nil
    if hasTargetPoints then
        sharedTargets = {}
        local seenTargets = {}
        for _, point in ipairs(targetPoints) do
            if point and point.x and point.y then
                local clone = clonePoint(point)
                local key = string.format("%d:%d:%d", math.floor(clone.x or 0), math.floor(clone.y or 0), math.floor(clone.z or 0))
                if not seenTargets[key] then
                    seenTargets[key] = true
                    sharedTargets[#sharedTargets + 1] = clone
                end
            end
        end
        if #sharedTargets == 0 then
            sharedTargets = nil
        end
    end

    for i = 0, onlinePlayers:size() - 1 do
        local playerObj = onlinePlayers:get(i)
        -- DeployActive 여부와 상관 없이 deployZone 내부 플레이어는 모두 Deploy 대상으로 포함한다.
        if playerObj and not playerObj:isDead() and isPlayerInDeployZone(playerObj) then
            local entryTargets = nil
            if sharedTargets then
                entryTargets = {}
                for _, point in ipairs(sharedTargets) do
                    entryTargets[#entryTargets + 1] = clonePoint(point)
                end
            end
            payload.players[#payload.players + 1] = {
                onlineID = playerObj:getOnlineID(),
                username = getPlayerUsername(playerObj),
                playerNum = playerObj.getPlayerNum and playerObj:getPlayerNum() or nil,
                destination = clonePoint(sharedSpawn),
                target = entryTargets and entryTargets[1] and clonePoint(entryTargets[1]) or nil,
                targets = entryTargets,
                destinationId = destination.id,
            }
        end
    end

    if #payload.players > 0 then
        local clearRadius = resolveDeployZombieClearRadius()
        if clearRadius > 0 then
            -- 스트리밍/스폰 타이밍으로 인해 즉시 제거가 누락될 수 있어,
            -- 일정 시간 동안 반복 시도하는 잡을 등록한다.
            scheduleDeployZombieClear(sharedSpawn, clearRadius)
        end
        print(string.format("[EFZ Deploy] Sending SyncDeploy for %d player(s) to %s,%s,%s.",
            #payload.players,
            tostring(sharedSpawn.x),
            tostring(sharedSpawn.y),
            tostring(sharedSpawn.z)))
        sendServerCommand("EFZ", "SyncDeploy", payload)
    else
        print("[EFZ Deploy] Server did not find any online player inside deploy zone.")
    end
end

local function triggerDeploy(destinationId)
    local target = resolvePlayer(player)
    if not target and type(getSpecificPlayer) == "function" then
        target = getSpecificPlayer(0)
    end
    if not target then
        print("[EFZ Deploy] Cannot start deploy: no target player for " .. tostring(destinationId) .. ".")
        return false
    end
    handleDeployRequest(destinationId, target)
    return true
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

local function onClientCommand(module, command, playerObj, args)
    if module ~= "EFZ" then
        return
    end
    if command == "RequestDeploy" then
        local destinationId = args and args.destinationId
        handleDeployRequest(destinationId, playerObj)
        return
    elseif command == "CompleteDeployExtraction" then
        if playerObj and (not playerObj:isDead()) then
            EFZ.DeployExtraction.applyCompletionState(playerObj)
        end
        return
    elseif command == "SetDeployActive" then
        local active = args and args.active == true
        Server.setDeployActive(active, playerObj)
        return
    end
end

local function onCreatePlayer(_, playerObj)
    if isIsoPlayer(playerObj) then
        -- 접속/생성 시점에 해당 플레이어의 활성 상태를 동기화한다.
        syncDeployActive(playerObj, isDeployActiveForPlayer(playerObj))
    end
end

Events.OnCreatePlayer.Add(onCreatePlayer)
Events.OnClientCommand.Add(onClientCommand)
