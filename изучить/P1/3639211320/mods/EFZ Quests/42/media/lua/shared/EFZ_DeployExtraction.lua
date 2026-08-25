local config = require "EFZ_DeployConfig"
require "EFZ_Teleport"

if not EFZ then
    EFZ = {}
end

EFZ.DeployExtraction = EFZ.DeployExtraction or {}
local Extraction = EFZ.DeployExtraction

local function getBodyDamage(playerObj)
    if not playerObj or not playerObj.getBodyDamage then
        return nil
    end
    return playerObj:getBodyDamage()
end

function Extraction.cureInfection(playerObj)
    local bodyDamage = getBodyDamage(playerObj)
    if not bodyDamage then
        return false
    end

    if bodyDamage.setInfected then
        bodyDamage:setInfected(false)
    end
    if bodyDamage.setInfectionMortalityDuration then
        bodyDamage:setInfectionMortalityDuration(-1)
    end
    if bodyDamage.setInfectionTime then
        bodyDamage:setInfectionTime(-1)
    end
    if bodyDamage.setInfectionGrowthRate then
        bodyDamage:setInfectionGrowthRate(0)
    end
    if bodyDamage.setIsFakeInfected then
        bodyDamage:setIsFakeInfected(false)
    end

    local bodyParts = bodyDamage.getBodyParts and bodyDamage:getBodyParts() or nil
    if bodyParts then
        for i = bodyParts:size() - 1, 0, -1 do
            local bodyPart = bodyParts:get(i)
            if bodyPart and bodyPart.SetInfected then
                bodyPart:SetInfected(false)
            end
        end
    end

    local stats = playerObj:getStats()
    stats:set(CharacterStat.ZOMBIE_INFECTION, 0)
    stats:set(CharacterStat.ZOMBIE_FEVER, 0)

    return true
end

function Extraction.applyCompletionState(playerObj)
    if not playerObj then
        return false
    end

    Extraction.cureInfection(playerObj)
    return true
end

function Extraction.completeExtraction(playerObj, options)
    if not playerObj then
        return false
    end

    options = options or {}
    local destination = options.destination or config.extraction
    local callback = options.callback
    local useLocalTeleport = options.useLocalTeleport == true

    local function finish(success)
        if success ~= false then
            Extraction.applyCompletionState(playerObj)
        end
        if callback then
            callback(success ~= false)
        end
        return success ~= false
    end

    if destination and useLocalTeleport and EFZ.Elevator and EFZ.Elevator.requestTeleportToDestination then
        return EFZ.Elevator.requestTeleportToDestination(playerObj, destination, {
            callback = function(success)
                finish(success)
            end,
        })
    end

    if destination and useLocalTeleport and EFZ.Teleport and EFZ.Teleport.requestLocalTeleport then
        return EFZ.Teleport.requestLocalTeleport(playerObj, destination, function(success)
            finish(success)
        end)
    end

    if destination and EFZ.Teleport and EFZ.Teleport.teleportPlayer then
        return finish(EFZ.Teleport.teleportPlayer(playerObj, destination))
    end

    return finish(true)
end

local function hasActiveDeployState(playerObj)
    local deploy = EFZ and EFZ.Deploy
    if not deploy or not deploy.activePlayers then
        return false
    end
    local state = deploy.activePlayers[playerObj:getPlayerNum()]
    return state ~= nil
end

local function hasDeployModData(playerObj)
    local modData = playerObj:getModData()
    local deployData = modData and modData.EFZDeploy
    return deployData and deployData.active == true
end

function Extraction.isPlayerDeploying(playerObj)
    if not playerObj then
        return false
    end
    return hasActiveDeployState(playerObj) or hasDeployModData(playerObj)
end

function Extraction.isPlayerInfected(playerObj)
    local bodyDamage = getBodyDamage(playerObj)
    if not bodyDamage then
        return false
    end
    return bodyDamage:isInfected() or bodyDamage:IsInfected()
end

function Extraction.cureOutsideDeploy(playerObj)
    if not playerObj or playerObj:isDead() then
        return false
    end
    if Extraction.isPlayerDeploying(playerObj) then
        return false
    end
    if not Extraction.isPlayerInfected(playerObj) then
        return false
    end
    return Extraction.cureInfection(playerObj)
end

local function onPlayerUpdate(playerObj)
    Extraction.cureOutsideDeploy(playerObj)
end

if Events and Events.OnPlayerUpdate then
    Events.OnPlayerUpdate.Add(onPlayerUpdate)
end
