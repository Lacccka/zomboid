require "LCCQF/LCCQFConstants"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local EXPECTED_BANDITUPDATE_SHIM = "source-clean-coordinate-pursuit-v4"
local reported = false

local function log(message)
    print(C.LOG_PREFIX .. "[COMPAT:NPCFIXES] " .. tostring(message))
end

local function checkNPCFixesCompatibility()
    if reported then return end
    reported = true

    local actual = LCC_NPCFIXES_BANDITUPDATE_SHIM
    if actual == EXPECTED_BANDITUPDATE_SHIM then
        log("BanditUpdate seam compatible expected=" .. EXPECTED_BANDITUPDATE_SHIM
            .. " actual=" .. tostring(actual)
            .. " nonCombatScheduling=available")
        return
    end

    log("WARNING stale-or-missing BanditUpdate seam expected=" .. EXPECTED_BANDITUPDATE_SHIM
        .. " actual=" .. tostring(actual)
        .. " nonCombatScheduling=unavailable"
        .. " action=update-LaccckaB4220NPCFixes-client-content")
end

Events.OnGameStart.Add(checkNPCFixesCompatibility)

return true
