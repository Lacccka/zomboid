-- Lacccka B42 NPC Fixes: source-clean Bandits 42.20 pursuit/non-combat shim.
--
-- This file intentionally contains no upstream Bandits implementation. Because it
-- occupies the BanditUpdate.lua module path, it reads the installed Bandits2
-- source directly from that mod, applies exact B42.20 seams, then compiles and
-- executes the transformed source. Known upstream formatting revisions are
-- whitelisted explicitly; unknown fingerprints bypass the patch instead of being
-- transformed heuristically.

local MOD_ID = "Bandits2"
local SOURCE_PATH = "media/lua/client/BanditUpdate.lua"
local MARKER = "source-clean-coordinate-pursuit-v4"
LCC_NPCFIXES_BANDITUPDATE_SHIM = MARKER

local function readUpstreamSource()
    if type(getModFileReader) ~= "function" then
        return nil, "getModFileReader unavailable"
    end

    local reader = getModFileReader(MOD_ID, SOURCE_PATH, false)
    if not reader then
        return nil, "Bandits2 BanditUpdate.lua unavailable"
    end

    local lines = {}
    while true do
        local line = reader:readLine()
        if line == nil then break end
        lines[#lines + 1] = line
    end
    pcall(function() reader:close() end)
    return table.concat(lines, "\n") .. "\n"
end

local function replacePlainOnce(source, needle, replacement, label)
    local first, last = string.find(source, needle, 1, true)
    if not first then
        return nil, label .. " fingerprint missing"
    end
    if string.find(source, needle, last + 1, true) then
        return nil, label .. " fingerprint is not unique"
    end
    return string.sub(source, 1, first - 1) .. replacement .. string.sub(source, last + 1)
end

local function replacePlainOnceAny(source, variants, replacement, label)
    local matchedFirst, matchedLast, matchedVariant = nil, nil, nil

    for index, needle in ipairs(variants) do
        local first, last = string.find(source, needle, 1, true)
        if first then
            if string.find(source, needle, last + 1, true) then
                return nil, label .. " fingerprint variant " .. tostring(index) .. " is not unique"
            end
            if matchedVariant ~= nil then
                return nil, label .. " multiple fingerprint variants matched"
            end
            matchedFirst, matchedLast, matchedVariant = first, last, index
        end
    end

    if matchedVariant == nil then
        return nil, label .. " fingerprint missing"
    end

    return string.sub(source, 1, matchedFirst - 1)
        .. replacement
        .. string.sub(source, matchedLast + 1), nil, matchedVariant
end

-- Runtime adapters may mark a Bandits brain non-combat. The current Quest
-- Framework provider also identifies its essential giver by program name, which
-- exists in the very first cluster snapshot before the physical NPC materializes.
-- This decision must happen inside BanditUpdate: generic combat and zombie-victim
-- selection run before custom ZombiePrograms.
local NON_COMBAT_HELPER_ANCHOR = [[local iter3 = 0]]
local NON_COMBAT_HELPER_REPLACEMENT = [[local iter3 = 0

local function LCCIsNonCombatBandit(bandit)
    if not bandit then return false end
    local brain = BanditBrain.Get(bandit)
    if type(brain) ~= "table" then return false end
    if brain.lccqNonCombat == true then return true end
    local program = brain.program
    return type(program) == "table" and program.name == "LCCQFQuestGiver"
end]]

local HELPER_ANCHOR = [[-- table of bandits being attacked by zombies
local biteTab = {}

-- manages zombie behavior towards bandits]]

local HELPER_REPLACEMENT = [[-- table of bandits being attacked by zombies
local biteTab = {}

-- LCC B42.20 coordinate-only pursuit. PathFindBehavior2.setData() cancels an
-- in-flight request, so an already-aligned Goal.Location must not be reissued on
-- every OnZombieUpdate. Never construct Goal.Character -> Bandit here.
local LCC_PURSUIT_ALIGN_DIST2 = 0.5625 -- 0.75 tile
local LCC_PURSUIT_IDLE_RETRY_MS = 750
local lccPursuitIdleRetryAt = setmetatable({}, { __mode = "k" })

local function LCCPathZombieToBanditLocation(zombie, banditCached)
    if not zombie or not banditCached then return end
    if not BanditUtils.IsController(zombie) then return end

    local pfb = zombie:getPathFindBehavior2()
    if pfb and not pfb:getIsCancelled() and pfb:isGoalLocation() then
        local dx = pfb:getTargetX() - banditCached.x
        local dy = pfb:getTargetY() - banditCached.y
        local dz = math.abs(pfb:getTargetZ() - banditCached.z)
        local aligned = dz < 0.5 and (dx * dx + dy * dy) <= LCC_PURSUIT_ALIGN_DIST2

        if aligned then
            if zombie:getActionStateName() ~= "idle" then
                return
            end

            local now = getTimestampMs()
            local lastRetry = lccPursuitIdleRetryAt[zombie] or 0
            if now - lastRetry < LCC_PURSUIT_IDLE_RETRY_MS then
                return
            end
            lccPursuitIdleRetryAt[zombie] = now
        end
    end

    zombie:pathToLocationF(banditCached.x, banditCached.y, banditCached.z)
end

-- manages zombie behavior towards bandits]]

local FAR_CHARACTER_PATH = [[                zombie:pathToCharacter(bandit)]]
local FAR_LOCATION_PATH = [[                LCCPathZombieToBanditLocation(zombie, banditCached)]]

-- Bandits 2026-08-26 retained the same unsafe relationship block but changed
-- whitespace in the condition. Keep both known exact forms instead of weakening
-- the transformer to whitespace-insensitive matching.
local CLOSE_RELATION_VARIANTS = {
[[                    if zombie and bandit then
                        zombie:spotted(bandit, true)
                        zombie:addAggro(bandit, 1)
                        zombie:setTarget(bandit)
                        zombie:setAttackedBy(bandit)]],
[[                    if zombie and bandit  then
                        zombie:spotted(bandit, true)
                        zombie:addAggro(bandit, 1)
                        zombie:setTarget(bandit)
                        zombie:setAttackedBy(bandit)]],
}

local CLOSE_LOCATION = [[                    if zombie and bandit then
                        LCCPathZombieToBanditLocation(zombie, banditCached)]]

-- Do not let normal zombies discover a non-combat Bandits NPC as prey. Keep the
-- entry in CacheLightB because BanditUpdate uses cache presence to decide whether
-- the physical Bandit itself is active and may execute its custom program.
local VICTIM_SELECTION_UNSAFE = [[    for _, bandit in pairs(banditList) do
        local dist2 = ((bandit.x - zx) * (bandit.x - zx)) + ((bandit.y - zy) * (bandit.y - zy))
        if dist2 < dist2max then
            dist2max = dist2
            banditCached = bandit
        end
    end]]

local VICTIM_SELECTION_FILTERED = [[    for _, bandit in pairs(banditList) do
        local candidate = BanditZombie.Cache[bandit.id]
        if candidate and not LCCIsNonCombatBandit(candidate) then
            local dist2 = ((bandit.x - zx) * (bandit.x - zx)) + ((bandit.y - zy) * (bandit.y - zy))
            if dist2 < dist2max then
                dist2max = dist2
                banditCached = bandit
            end
        end
    end]]

-- Bandits generic combat runs before custom ZombiePrograms. A non-combat NPC
-- must never enter ManageCombat, otherwise the "enemies >= friendlies + 2"
-- branch generates a Run escape task even when brain.stationary is true.
local COMBAT_DISPATCH_UNSAFE = [[    -- MANAGE MELEE / SHOOTING TASKS
    if #tasks == 0  then
        -- local ts = getTimestampMs()
        local combatTasks = ManageCombat(bandit)]]

local COMBAT_DISPATCH_FILTERED = [[    -- MANAGE MELEE / SHOOTING TASKS
    if #tasks == 0 and not LCCIsNonCombatBandit(bandit) then
        -- local ts = getTimestampMs()
        local combatTasks = ManageCombat(bandit)]]

-- Collision handling can also create movement/climb/breach tasks. Non-combat
-- presentation NPCs leave those decisions to their custom program.
local COLLISION_DISPATCH_UNSAFE = [[    -- MANAGE COLLISION TASKS
    if #tasks == 0 then
        -- local ts = getTimestampMs()
        local colissionTasks = ManageCollisions(bandit)]]

local COLLISION_DISPATCH_FILTERED = [[    -- MANAGE COLLISION TASKS
    if #tasks == 0 and not LCCIsNonCombatBandit(bandit) then
        -- local ts = getTimestampMs()
        local colissionTasks = ManageCollisions(bandit)]]

-- Ignore Bandits-specific hit/friendly-fire reactions for non-combat NPCs.
-- Native targetability is separately controlled by the provider/runtime layer.
local HIT_DISPATCH_UNSAFE = [[    if not zombie:getVariableBoolean("Bandit") then return end

    local bandit = zombie]]

local HIT_DISPATCH_FILTERED = [[    if not zombie:getVariableBoolean("Bandit") then return end
    if LCCIsNonCombatBandit(zombie) then return end

    local bandit = zombie]]

-- Bandits2 passes brain.key to InventoryItem.setKeyId(int). Reject non-numeric
-- values defensively so legacy integrations cannot crash the death cleanup.
local DEATH_KEY_UNSAFE = [[        if brain.key and ZombRand(3) == 1 then
            local item = BanditCompatibility.InstanceItem("Base.Key1")
            item:setKeyId(brain.key)]]

local DEATH_KEY_TYPED = [[        if type(brain.key) == "number" and ZombRand(3) == 1 then
            local item = BanditCompatibility.InstanceItem("Base.Key1")
            item:setKeyId(brain.key)]]

local source, readErr = readUpstreamSource()
if not source then
    error("[LCC][NPCFixes][BanditUpdateShim][FATAL] " .. tostring(readErr))
end

local patched = source
local reasons = {}
local closeVariant = nil

local nextSource, reason = replacePlainOnce(
    patched,
    NON_COMBAT_HELPER_ANCHOR,
    NON_COMBAT_HELPER_REPLACEMENT,
    "non-combat-helper"
)
if nextSource then patched = nextSource else reasons[#reasons + 1] = reason end

if #reasons == 0 then
    nextSource, reason = replacePlainOnce(patched, HELPER_ANCHOR, HELPER_REPLACEMENT, "helper-anchor")
    if nextSource then patched = nextSource else reasons[#reasons + 1] = reason end
end

if #reasons == 0 then
    nextSource, reason = replacePlainOnce(
        patched,
        VICTIM_SELECTION_UNSAFE,
        VICTIM_SELECTION_FILTERED,
        "non-combat-victim-selection"
    )
    if nextSource then patched = nextSource else reasons[#reasons + 1] = reason end
end

if #reasons == 0 then
    nextSource, reason = replacePlainOnce(
        patched,
        COMBAT_DISPATCH_UNSAFE,
        COMBAT_DISPATCH_FILTERED,
        "non-combat-combat-dispatch"
    )
    if nextSource then patched = nextSource else reasons[#reasons + 1] = reason end
end

if #reasons == 0 then
    nextSource, reason = replacePlainOnce(
        patched,
        COLLISION_DISPATCH_UNSAFE,
        COLLISION_DISPATCH_FILTERED,
        "non-combat-collision-dispatch"
    )
    if nextSource then patched = nextSource else reasons[#reasons + 1] = reason end
end

if #reasons == 0 then
    nextSource, reason = replacePlainOnce(
        patched,
        HIT_DISPATCH_UNSAFE,
        HIT_DISPATCH_FILTERED,
        "non-combat-hit-dispatch"
    )
    if nextSource then patched = nextSource else reasons[#reasons + 1] = reason end
end

if #reasons == 0 then
    nextSource, reason = replacePlainOnce(patched, FAR_CHARACTER_PATH, FAR_LOCATION_PATH, "far-character-path")
    if nextSource then patched = nextSource else reasons[#reasons + 1] = reason end
end

if #reasons == 0 then
    nextSource, reason, closeVariant = replacePlainOnceAny(
        patched,
        CLOSE_RELATION_VARIANTS,
        CLOSE_LOCATION,
        "close-character-relation"
    )
    if nextSource then patched = nextSource else reasons[#reasons + 1] = reason end
end

if #reasons == 0 then
    nextSource, reason = replacePlainOnce(patched, DEATH_KEY_UNSAFE, DEATH_KEY_TYPED, "typed-death-key")
    if nextSource then patched = nextSource else reasons[#reasons + 1] = reason end
end

local mode = "PATCHED"
local sourceToRun = patched
if #reasons > 0 then
    mode = "BYPASS_FINGERPRINT"
    sourceToRun = source
    print("[LCC][NPCFixes][BanditUpdateShim][WARN] marker=" .. MARKER
        .. " mode=" .. mode .. " reason=" .. table.concat(reasons, "; ")
        .. " upstream=unchanged")
end

if type(loadstring) ~= "function" then
    error("[LCC][NPCFixes][BanditUpdateShim][FATAL] loadstring unavailable")
end

local chunk, compileErr = loadstring(sourceToRun)
if not chunk and mode == "PATCHED" then
    mode = "BYPASS_COMPILE"
    print("[LCC][NPCFixes][BanditUpdateShim][WARN] marker=" .. MARKER
        .. " mode=" .. mode .. " patchedCompileError=" .. tostring(compileErr)
        .. " upstream=unchanged")
    chunk, compileErr = loadstring(source)
end
if not chunk then
    error("[LCC][NPCFixes][BanditUpdateShim][FATAL] compile failed: " .. tostring(compileErr))
end

local ok, runtimeErr = pcall(chunk)
if not ok then
    error("[LCC][NPCFixes][BanditUpdateShim][FATAL] execution failed after mode="
        .. mode .. ": " .. tostring(runtimeErr))
end

print("[LCC][NPCFixes][BanditUpdateShim][BOOT] marker=" .. MARKER
    .. " mode=" .. mode
    .. " closeVariant=" .. tostring(closeVariant or "n/a")
    .. " nonCombatProgram=LCCQFQuestGiver"
    .. " source=Bandits2 runtimeTransform=true bundledUpstream=false")
