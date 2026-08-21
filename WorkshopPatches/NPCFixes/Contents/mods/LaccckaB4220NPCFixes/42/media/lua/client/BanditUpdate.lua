-- Lacccka B42 NPC Fixes: source-clean Bandits 42.20 pursuit shim.
--
-- This file intentionally contains no upstream Bandits implementation. Because it
-- occupies the BanditUpdate.lua module path, it reads the installed Bandits2
-- source directly from that mod, applies three exact B42.20 fingerprints, then
-- compiles and executes the transformed source. If the upstream fingerprints no
-- longer match, the original source is executed unchanged and a loud warning is
-- emitted instead of guessing against a new Bandits version.

local MOD_ID = "Bandits2"
local SOURCE_PATH = "media/lua/client/BanditUpdate.lua"
local MARKER = "source-clean-coordinate-pursuit-v1"
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

local CLOSE_RELATION = [[                    if zombie and bandit then
                        zombie:spotted(bandit, true)
                        zombie:addAggro(bandit, 1)
                        zombie:setTarget(bandit)
                        zombie:setAttackedBy(bandit)]]

local CLOSE_LOCATION = [[                    if zombie and bandit then
                        LCCPathZombieToBanditLocation(zombie, banditCached)]]

local source, readErr = readUpstreamSource()
if not source then
    error("[LCC][NPCFixes][BanditUpdateShim][FATAL] " .. tostring(readErr))
end

local patched = source
local reasons = {}

local nextSource, reason = replacePlainOnce(patched, HELPER_ANCHOR, HELPER_REPLACEMENT, "helper-anchor")
if nextSource then patched = nextSource else reasons[#reasons + 1] = reason end

if #reasons == 0 then
    nextSource, reason = replacePlainOnce(patched, FAR_CHARACTER_PATH, FAR_LOCATION_PATH, "far-character-path")
    if nextSource then patched = nextSource else reasons[#reasons + 1] = reason end
end

if #reasons == 0 then
    nextSource, reason = replacePlainOnce(patched, CLOSE_RELATION, CLOSE_LOCATION, "close-character-relation")
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
    .. " mode=" .. mode .. " source=Bandits2 runtimeTransform=true bundledUpstream=false")
