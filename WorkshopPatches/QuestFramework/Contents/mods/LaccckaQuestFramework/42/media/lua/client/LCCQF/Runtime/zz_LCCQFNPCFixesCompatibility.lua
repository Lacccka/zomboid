require "LCCQF/LCCQFConstants"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local EXPECTED_NPCFIXES_SEAM = "loadstring-free-predicate-bridge-v2"
local reported = false

local function log(message)
    print(C.LOG_PREFIX .. "[COMPAT:NPCFIXES] " .. tostring(message))
end

local function checkNPCFixesCompatibility()
    if reported then return end
    reported = true

    local actual = LCC_NPCFIXES_BANDITUPDATE_SHIM
    if actual == EXPECTED_NPCFIXES_SEAM then
        log("NPCFixes scheduling seam compatible expected=" .. EXPECTED_NPCFIXES_SEAM
            .. " actual=" .. tostring(actual)
            .. " runtimeTransform=false"
            .. " nonCombatScheduling=available")
        return
    end

    log("WARNING stale-or-missing NPCFixes scheduling seam expected=" .. EXPECTED_NPCFIXES_SEAM
        .. " actual=" .. tostring(actual)
        .. " nonCombatScheduling=unavailable"
        .. " action=update-LaccckaB4220NPCFixes-client-content")
end

Events.OnGameStart.Add(checkNPCFixesCompatibility)

return true
