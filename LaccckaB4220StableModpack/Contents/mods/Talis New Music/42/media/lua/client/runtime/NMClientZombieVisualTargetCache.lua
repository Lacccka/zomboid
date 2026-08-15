NMClientZombieVisualTargetCache = NMClientZombieVisualTargetCache or {}
NMClientZombieVisualTargetCache._tick = NMClientZombieVisualTargetCache._tick or 0
NMClientZombieVisualTargetCache._revision = NMClientZombieVisualTargetCache._revision or 0
NMClientZombieVisualTargetCache._ttlTicks = NMClientZombieVisualTargetCache._ttlTicks or 0
NMClientZombieVisualTargetCache._receivedTick = NMClientZombieVisualTargetCache._receivedTick or 0
NMClientZombieVisualTargetCache._hasSnapshot = NMClientZombieVisualTargetCache._hasSnapshot or false
NMClientZombieVisualTargetCache._targetCount = NMClientZombieVisualTargetCache._targetCount or 0
NMClientZombieVisualTargetCache._staleLoggedRevision = NMClientZombieVisualTargetCache._staleLoggedRevision or 0
NMClientZombieVisualTargetCache._staleLoggedAge = NMClientZombieVisualTargetCache._staleLoggedAge or 0
NMClientZombieVisualTargetCache._targets = NMClientZombieVisualTargetCache._targets or {}

local function shouldLog()
    return NMCore and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("zombie_visual") == true
end

local function logSummary(tag, detail)
    if not shouldLog() then
        return
    end
    print("[NewMusic] [ZombieProof] " .. tostring(tag or "") .. " " .. tostring(detail or ""))
end

local function clearTargets()
    NMClientZombieVisualTargetCache._targets = {}
    NMClientZombieVisualTargetCache._targetCount = 0
end

local function resetStaleSnapshotLogging()
    NMClientZombieVisualTargetCache._staleLoggedRevision = 0
    NMClientZombieVisualTargetCache._staleLoggedAge = 0
end

local function isMPClientRuntime()
    return NMCore and NMCore.isMPClientRuntime and NMCore.isMPClientRuntime() == true
end

local function isSPLocalRuntime()
    return NMCore and NMCore.getRuntimeAuthorityMode and NMCore.getRuntimeAuthorityMode() == "sp_local"
end

local function hasAuthoritativeSnapshot()
    return (tonumber(NMClientZombieVisualTargetCache._revision) or 0) > 0 and NMClientZombieVisualTargetCache._hasSnapshot == true
end

local function getStampedProofModData(zombie)
    local key = NMZombieVisualTargetContract and NMZombieVisualTargetContract.ModDataKey or "nmZombieWalkmanProof"
    local root = zombie and zombie.getModData and zombie:getModData() or nil
    local md = root and root[key] or nil
    if type(md) ~= "table" then
        return nil
    end
    return md
end

local function getSPStampedDecision(zombie)
    if not isSPLocalRuntime() then
        return nil
    end
    local md = getStampedProofModData(zombie)
    if type(md) ~= "table" then
        return nil
    end
    local wantedSource = tostring(NMZombieVisualTargetContract and NMZombieVisualTargetContract.SelectionSource or "server_ledger")
    local selectionSource = tostring(md.selectionSource or "")
    if selectionSource ~= wantedSource then
        return nil
    end
    local stampedZombieId = tostring(md.selectionZombieId or "")
    local zombieId = tostring(NMZombieVisualTargetContract and NMZombieVisualTargetContract.getZombieId and NMZombieVisualTargetContract.getZombieId(zombie) or zombie or "")
    if stampedZombieId == "" or zombieId == "" or stampedZombieId ~= zombieId then
        return nil
    end
    local status = tostring(md.status or "")
    local selected = status == "attached"
    return {
        authoritative = true,
        selected = selected,
        state = selected and "selected" or "excluded",
        source = "sp_stamp",
        selectionEpoch = tonumber(md.selectionEpoch) or 0,
        zombieId = zombieId,
        variantId = tostring(md.variantId or ""),
        fullType = tostring(md.fullType or ""),
        attachmentLocation = tostring(md.attachmentLocation or ""),
        modelAttachmentName = tostring(md.modelAttachmentName or "")
    }
end

local function buildMPSnapshotDecision(zombie)
    local zombieId = NMZombieVisualTargetContract and NMZombieVisualTargetContract.getZombieId and NMZombieVisualTargetContract.getZombieId(zombie) or tostring(zombie or "")
    local snapshotRecord = NMClientZombieVisualTargetCache._targets[tostring(zombieId or "")]
    local selected = type(snapshotRecord) == "table"
    return {
        authoritative = hasAuthoritativeSnapshot(),
        selected = selected,
        state = selected and "selected" or "excluded",
        source = "mp_snapshot",
        zombieId = tostring(zombieId or ""),
        variantId = tostring(snapshotRecord and snapshotRecord.variantId or ""),
        fullType = tostring(snapshotRecord and snapshotRecord.fullType or ""),
        attachmentLocation = tostring(snapshotRecord and snapshotRecord.attachmentLocation or ""),
        modelAttachmentName = tostring(snapshotRecord and snapshotRecord.modelAttachmentName or "")
    }
end

local function buildUnknownDecision(zombie)
    return {
        authoritative = false,
        selected = false,
        state = isSPLocalRuntime() and "pending" or "unknown",
        source = "none",
        zombieId = tostring(NMZombieVisualTargetContract and NMZombieVisualTargetContract.getZombieId and NMZombieVisualTargetContract.getZombieId(zombie) or zombie or "")
    }
end

local function receiveTargetSnapshot(args)
    NMClientZombieVisualTargetCache._ttlTicks = tonumber(args and args.ttlTicks) or tonumber(NMZombieVisualTargetContract and NMZombieVisualTargetContract.ClientCacheTtlTicks) or 0
    NMClientZombieVisualTargetCache._receivedTick = tonumber(NMClientZombieVisualTargetCache._tick) or 0
    NMClientZombieVisualTargetCache._hasSnapshot = true
    resetStaleSnapshotLogging()
    clearTargets()
    local records = args and args.targetRecords or nil
    NMClientZombieVisualTargetCache._targets = NMZombieVisualTargetContract and NMZombieVisualTargetContract.buildTargetSnapshotLookup and NMZombieVisualTargetContract.buildTargetSnapshotLookup(records) or {}
    for _ in pairs(NMClientZombieVisualTargetCache._targets) do
        NMClientZombieVisualTargetCache._targetCount = (tonumber(NMClientZombieVisualTargetCache._targetCount) or 0) + 1
    end
end

function NMClientZombieVisualTargetCache.onServerCommand(command, args)
    if tostring(command or "") ~= tostring(NMZombieVisualTargetContract and NMZombieVisualTargetContract.NetCommand or "") then
        return false
    end
    local revision = tonumber(args and args.revision) or 0
    if revision < (tonumber(NMClientZombieVisualTargetCache._revision) or 0) then
        return true
    end
    NMClientZombieVisualTargetCache._revision = revision
    receiveTargetSnapshot(args)
    logSummary(
        "target_cache_update",
        string.format(
            "revision=%s targets=%s candidates=%s published=%s",
            tostring(revision),
            tostring(NMClientZombieVisualTargetCache._targetCount or 0),
            tostring(args and args.targetCandidates or 0),
            tostring(args and args.targetPublished or 0)
        )
    )
    return true
end

function NMClientZombieVisualTargetCache.onTick()
    NMClientZombieVisualTargetCache._tick = (tonumber(NMClientZombieVisualTargetCache._tick) or 0) + 1
    if not isMPClientRuntime() then
        return
    end
    local ttl = tonumber(NMClientZombieVisualTargetCache._ttlTicks) or 0
    if ttl <= 0 then
        return
    end
    local ageTicks = (tonumber(NMClientZombieVisualTargetCache._tick) or 0) - (tonumber(NMClientZombieVisualTargetCache._receivedTick) or 0)
    local overdueThreshold = ttl * 4
    if ageTicks < overdueThreshold then
        return
    end
    local revision = tonumber(NMClientZombieVisualTargetCache._revision) or 0
    if revision <= 0 then
        return
    end
    if revision ~= (tonumber(NMClientZombieVisualTargetCache._staleLoggedRevision) or 0) then
        NMClientZombieVisualTargetCache._staleLoggedRevision = revision
        NMClientZombieVisualTargetCache._staleLoggedAge = 0
    end
    local lastLoggedAge = tonumber(NMClientZombieVisualTargetCache._staleLoggedAge) or 0
    if ageTicks < (lastLoggedAge + overdueThreshold) then
        return
    end
    NMClientZombieVisualTargetCache._staleLoggedAge = ageTicks
    logSummary(
        "target_cache_overdue",
        string.format(
            "revision=%s targets=%s ageTicks=%s ttl=%s",
            tostring(revision),
            tostring(NMClientZombieVisualTargetCache._targetCount or 0),
            tostring(ageTicks),
            tostring(ttl)
        )
    )
end

function NMClientZombieVisualTargetCache.getZombieDecision(zombie)
    if isMPClientRuntime() then
        return buildMPSnapshotDecision(zombie)
    end
    local stampedDecision = getSPStampedDecision(zombie)
    if stampedDecision then
        return stampedDecision
    end
    return buildUnknownDecision(zombie)
end

function NMClientZombieVisualTargetCache.shouldRenderZombie(zombie)
    local decision = NMClientZombieVisualTargetCache.getZombieDecision and NMClientZombieVisualTargetCache.getZombieDecision(zombie) or nil
    if type(decision) ~= "table" then
        return false
    end
    if decision.authoritative ~= true then
        return false
    end
    return decision.selected == true
end

function NMClientZombieVisualTargetCache.isAuthoritativeZombie(zombie)
    local decision = NMClientZombieVisualTargetCache.getZombieDecision and NMClientZombieVisualTargetCache.getZombieDecision(zombie) or nil
    return type(decision) == "table" and decision.authoritative == true
end

function NMClientZombieVisualTargetCache.isAuthorityDrivenRuntime()
    if isMPClientRuntime() then
        return hasAuthoritativeSnapshot()
    end
    return isSPLocalRuntime()
end

function NMClientZombieVisualTargetCache.getZombieId(zombie)
    local zombieId = NMZombieVisualTargetContract and NMZombieVisualTargetContract.getZombieId and NMZombieVisualTargetContract.getZombieId(zombie) or tostring(zombie or "")
    return tostring(zombieId or "")
end

function NMClientZombieVisualTargetCache.getRevision()
    return tonumber(NMClientZombieVisualTargetCache._revision) or 0
end

return NMClientZombieVisualTargetCache
