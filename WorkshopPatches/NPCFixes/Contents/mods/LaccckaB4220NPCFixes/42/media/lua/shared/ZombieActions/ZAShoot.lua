-- Lacccka B42 NPC Fixes: source-clean Bandits 42.20 gunshot-alert shim.
--
-- Reads the installed Bandits2 ZAShoot implementation, replaces the one unsafe
-- idle-zombie -> Bandit character relationship with a coordinate-only sound-origin
-- path, and executes the transformed upstream source. No upstream implementation
-- is stored in this patch.

local MOD_ID = "Bandits2"
local SOURCE_PATH = "media/lua/shared/ZombieActions/ZAShoot.lua"
local MARKER = "source-clean-gunshot-coordinate-alert-v1"
LCC_NPCFIXES_ZASHOOT_SHIM = MARKER

local function readUpstreamSource()
    if type(getModFileReader) ~= "function" then
        return nil, "getModFileReader unavailable"
    end

    local reader = getModFileReader(MOD_ID, SOURCE_PATH, false)
    if not reader then
        return nil, "Bandits2 ZAShoot.lua unavailable"
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

local UNSAFE_ALERT = [[                    zombie:spottedNew(shooter, true)
                    zombie:addAggro(shooter, 1)
                    zombie:setTarget(shooter)
                    -- zombie:pathToLocationF(sx, sy, sz)]]

local COORDINATE_ALERT = [[                    zombie:pathToLocationF(sx, sy, sz)]]

local source, readErr = readUpstreamSource()
if not source then
    error("[LCC][NPCFixes][ZAShootShim][FATAL] " .. tostring(readErr))
end

local patched, reason = replacePlainOnce(source, UNSAFE_ALERT, COORDINATE_ALERT, "idle-shot-character-alert")
local mode = "PATCHED"
local sourceToRun = patched
if not patched then
    mode = "BYPASS_FINGERPRINT"
    sourceToRun = source
    print("[LCC][NPCFixes][ZAShootShim][WARN] marker=" .. MARKER
        .. " mode=" .. mode .. " reason=" .. tostring(reason)
        .. " upstream=unchanged")
end

if type(loadstring) ~= "function" then
    error("[LCC][NPCFixes][ZAShootShim][FATAL] loadstring unavailable")
end

local chunk, compileErr = loadstring(sourceToRun)
if not chunk and mode == "PATCHED" then
    mode = "BYPASS_COMPILE"
    print("[LCC][NPCFixes][ZAShootShim][WARN] marker=" .. MARKER
        .. " mode=" .. mode .. " patchedCompileError=" .. tostring(compileErr)
        .. " upstream=unchanged")
    chunk, compileErr = loadstring(source)
end
if not chunk then
    error("[LCC][NPCFixes][ZAShootShim][FATAL] compile failed: " .. tostring(compileErr))
end

local ok, runtimeErr = pcall(chunk)
if not ok then
    error("[LCC][NPCFixes][ZAShootShim][FATAL] execution failed after mode="
        .. mode .. ": " .. tostring(runtimeErr))
end

print("[LCC][NPCFixes][ZAShootShim][BOOT] marker=" .. MARKER
    .. " mode=" .. mode .. " source=Bandits2 runtimeTransform=true bundledUpstream=false")
