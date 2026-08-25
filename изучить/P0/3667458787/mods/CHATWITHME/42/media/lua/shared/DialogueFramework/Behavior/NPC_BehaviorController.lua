local NPC_BehaviorController = {}

local NPC_BehaviorConfig = require("DialogueFramework/Behavior/NPC_BehaviorConfig")
local NPC_BehaviorDefinitionRegistry = require("DialogueFramework/Behavior/NPC_BehaviorDefinitionRegistry")
local NPC_BehaviorNPCRegistry = require("DialogueFramework/Behavior/NPC_BehaviorNPCRegistry")

function NPC_BehaviorController.queueBehavior(npc, behaviorID, params, options)
    if not NPC_BehaviorNPCRegistry.isValidNPC(npc) then
        return false, "Invalid NPC"
    end

    NPC_BehaviorNPCRegistry.registerNPC(npc)

    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
    if not npcID then
        return false, "Failed to get NPC ID"
    end

    if not NPC_BehaviorDefinitionRegistry.isRegistered(behaviorID) then
        return false, "Behavior not registered: " .. tostring(behaviorID)
    end

    local behaviorDef = NPC_BehaviorDefinitionRegistry.get(behaviorID)

    local queue = NPC_BehaviorNPCRegistry.getQueue(npcID) or {}

    if #queue >= NPC_BehaviorConfig.QUEUE.MAX_BEHAVIORS_PER_NPC then
        return false, "Behavior queue full for NPC"
    end

    if not NPC_BehaviorConfig.QUEUE.ALLOW_DUPLICATE_BEHAVIORS then
        for _, existing in ipairs(queue) do
            if existing.behaviorID == behaviorID and existing.status == NPC_BehaviorConfig.STATUS.QUEUED then
                return false, "Duplicate behavior already queued"
            end
        end
    end

    options = options or {}
    local behaviorEntry = {
        behaviorID = behaviorID,
        params = params or {},
        priority = options.priority or behaviorDef.priority or NPC_BehaviorConfig.PRIORITY.MEDIUM,
        status = NPC_BehaviorConfig.STATUS.QUEUED,
        queuedTime = getTimestampMs() / 1000.0,
        startTime = nil,
        completionTime = nil,
        isAsync = options.isAsync or false,
        asyncHandle = nil,
        chainedFrom = options.chainedFrom or nil,
        nextBehavior = options.nextBehavior or nil,
        onComplete = options.onComplete or nil,
        onFailed = options.onFailed or nil
    }

    table.insert(queue, behaviorEntry)

    NPC_BehaviorNPCRegistry.setQueue(npcID, queue)

    NPC_BehaviorController.processQueue(npc)

    return true, behaviorEntry
end

function NPC_BehaviorController.processQueue(npc)
    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
    if not npcID then return false end

    local queue = NPC_BehaviorNPCRegistry.getQueue(npcID)
    if not queue or #queue == 0 then return false end

    for _, behavior in ipairs(queue) do
        if behavior.status == NPC_BehaviorConfig.STATUS.EXECUTING then
            return false
        end
    end

    local highestPriority = nil
    local highestPriorityIndex = nil

    for i, behavior in ipairs(queue) do
        if behavior.status == NPC_BehaviorConfig.STATUS.QUEUED then
            if not highestPriority or behavior.priority > highestPriority.priority then
                highestPriority = behavior
                highestPriorityIndex = i
            end
        end
    end

    if not highestPriority then
        return false
    end

    local success, result = NPC_BehaviorController.executeBehavior(npc, highestPriority)

    if success then
        highestPriority.status = NPC_BehaviorConfig.STATUS.EXECUTING
        highestPriority.startTime = getTimestampMs() / 1000.0
    else
        highestPriority.status = NPC_BehaviorConfig.STATUS.FAILED
        highestPriority.completionTime = getTimestampMs() / 1000.0
        highestPriority.failureReason = result
    end

    NPC_BehaviorNPCRegistry.setQueue(npcID, queue)

    return success, result
end

function NPC_BehaviorController.executeBehavior(npc, behaviorEntry)
    local behaviorDef = NPC_BehaviorDefinitionRegistry.get(behaviorEntry.behaviorID)
    if not behaviorDef then
        return false, "Behavior definition not found"
    end

    if behaviorEntry.isAsync and behaviorDef.supportsAsync then
        return NPC_BehaviorController.executeAsync(npc, behaviorEntry, behaviorDef)
    else
        return NPC_BehaviorController.executeSync(npc, behaviorEntry, behaviorDef)
    end
end

function NPC_BehaviorController.executeSync(npc, behaviorEntry, behaviorDef)
    if behaviorDef.requiresServer then
        if isClient() and not isServer() then
            local NPC_BehaviorExecutor = require("DialogueFramework/Behavior/NPC_BehaviorExecutor")
            return NPC_BehaviorExecutor.sendToServer(npc, behaviorEntry)
        else
            return NPC_BehaviorController.executeLocal(npc, behaviorEntry, behaviorDef)
        end
    else
        return NPC_BehaviorController.executeLocal(npc, behaviorEntry, behaviorDef)
    end
end

function NPC_BehaviorController.executeLocal(npc, behaviorEntry, behaviorDef)
    local success, behaviorModule = pcall(function()
        return require(behaviorDef.moduleFile)
    end)

    if not success or not behaviorModule then
        return false, "Failed to load behavior module"
    end

    if not behaviorModule.execute then
        return false, "Behavior module missing execute function"
    end

    local execSuccess, execResult = pcall(function()
        return behaviorModule.execute(npc, behaviorEntry.params, getPlayer())
    end)

    if execSuccess and execResult then
        return true, execResult
    else
        NPC_BehaviorController.onBehaviorFailed(npc, behaviorEntry, execResult)
        return false, execResult
    end
end

function NPC_BehaviorController.executeAsync(npc, behaviorEntry, behaviorDef)
    behaviorEntry.status = NPC_BehaviorConfig.STATUS.EXECUTING

    return true, "Async execution started"
end

function NPC_BehaviorController.onBehaviorComplete(npc, behaviorEntry, result)
    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
    if not npcID then return end

    local queue = NPC_BehaviorNPCRegistry.getQueue(npcID)
    if not queue then return end

    for i, queuedBehavior in ipairs(queue) do
        if queuedBehavior == behaviorEntry then
            queuedBehavior.status = NPC_BehaviorConfig.STATUS.COMPLETED
            queuedBehavior.completionTime = getTimestampMs() / 1000.0
            queuedBehavior.result = result

            if queuedBehavior.onComplete then
                pcall(queuedBehavior.onComplete, npc, result)
            end

            if NPC_BehaviorConfig.CHAINING.ENABLED and queuedBehavior.nextBehavior then
                NPC_BehaviorController.queueBehavior(npc, queuedBehavior.nextBehavior, {}, {
                    chainedFrom = queuedBehavior.behaviorID
                })
            end

            if NPC_BehaviorConfig.CLEANUP.ON_BEHAVIOR_COMPLETION then
                table.remove(queue, i)
            end

            break
        end
    end

    NPC_BehaviorNPCRegistry.setQueue(npcID, queue)

    NPC_BehaviorController.processQueue(npc)
end

function NPC_BehaviorController.onBehaviorFailed(npc, behaviorEntry, reason)
    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
    if not npcID then return end

    local queue = NPC_BehaviorNPCRegistry.getQueue(npcID)
    if not queue then return end

    for i, queuedBehavior in ipairs(queue) do
        if queuedBehavior == behaviorEntry then
            queuedBehavior.status = NPC_BehaviorConfig.STATUS.FAILED
            queuedBehavior.completionTime = getTimestampMs() / 1000.0
            queuedBehavior.failureReason = reason

            if queuedBehavior.onFailed then
                pcall(queuedBehavior.onFailed, npc, reason)
            end

            if NPC_BehaviorConfig.CHAINING.ABORT_ON_FAILURE then
                queuedBehavior.nextBehavior = nil
            end

            break
        end
    end

    NPC_BehaviorNPCRegistry.setQueue(npcID, queue)

    NPC_BehaviorController.processQueue(npc)
end

function NPC_BehaviorController.cleanup(npc, strategy)
    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
    if not npcID then return false end

    local queue = NPC_BehaviorNPCRegistry.getQueue(npcID)
    if not queue then return false end

    strategy = strategy or "all_completed"

    local newQueue = {}
    local currentTime = getTimestampMs() / 1000.0

    for _, behavior in ipairs(queue) do
        local shouldKeep = false

        if strategy == "all_completed" then
            shouldKeep = behavior.status ~= NPC_BehaviorConfig.STATUS.COMPLETED

        elseif strategy == "timed" then
            if behavior.status == NPC_BehaviorConfig.STATUS.COMPLETED then
                local timeSinceCompletion = currentTime - (behavior.completionTime or currentTime)
                shouldKeep = timeSinceCompletion < NPC_BehaviorConfig.CLEANUP.TIMED_CLEANUP.COMPLETED_AFTER_SECONDS

            elseif behavior.status == NPC_BehaviorConfig.STATUS.FAILED then
                local timeSinceFailure = currentTime - (behavior.completionTime or currentTime)
                shouldKeep = timeSinceFailure < NPC_BehaviorConfig.CLEANUP.TIMED_CLEANUP.FAILED_AFTER_SECONDS

            elseif behavior.status == NPC_BehaviorConfig.STATUS.CANCELLED then
                shouldKeep = false

            else
                shouldKeep = true
            end

        elseif strategy == "all" then
            shouldKeep = false
        end

        if shouldKeep then
            table.insert(newQueue, behavior)
        end
    end

    NPC_BehaviorNPCRegistry.setQueue(npcID, newQueue)
    return true
end

function NPC_BehaviorController.onDialogueEnd(npc)
    if NPC_BehaviorConfig.CLEANUP.ON_DIALOGUE_END then
        NPC_BehaviorController.cleanup(npc, "all_completed")
    end
end

function NPC_BehaviorController.cleanupNPC(npc, strategy)
    if not NPC_BehaviorConfig.CLEANUP.ON_EXPLICIT_CALL then
        return false
    end

    return NPC_BehaviorController.cleanup(npc, strategy or "all_completed")
end

function NPC_BehaviorController.getActiveBehaviors(npc)
    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
    if not npcID then return {} end

    return NPC_BehaviorNPCRegistry.getQueue(npcID) or {}
end

function NPC_BehaviorController.getBehaviorStatus(npc, behaviorID)
    local behaviors = NPC_BehaviorController.getActiveBehaviors(npc)

    for _, behavior in ipairs(behaviors) do
        if behavior.behaviorID == behaviorID then
            return behavior.status
        end
    end

    return nil
end

function NPC_BehaviorController.cancelBehavior(npc, behaviorID)
    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
    if not npcID then return false end

    local queue = NPC_BehaviorNPCRegistry.getQueue(npcID)
    if not queue then return false end

    for _, behavior in ipairs(queue) do
        if behavior.behaviorID == behaviorID then
            if behavior.status == NPC_BehaviorConfig.STATUS.QUEUED then
                behavior.status = NPC_BehaviorConfig.STATUS.CANCELLED
                behavior.completionTime = getTimestampMs() / 1000.0
                return true
            end
        end
    end

    return false
end

function NPC_BehaviorController.completeBehaviorByID(npc, behaviorID, result)
    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
    if not npcID then return false end

    local queue = NPC_BehaviorNPCRegistry.getQueue(npcID)
    if not queue then return false end

    for _, behavior in ipairs(queue) do
        if behavior.behaviorID == behaviorID and behavior.status == NPC_BehaviorConfig.STATUS.EXECUTING then
            NPC_BehaviorController.onBehaviorComplete(npc, behavior, result)
            return true
        end
    end

    return false
end

return NPC_BehaviorController
