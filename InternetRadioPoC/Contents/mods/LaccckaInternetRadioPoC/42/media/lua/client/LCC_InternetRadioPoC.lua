-- Internet Vehicle Radio - WIVK-FM proof-of-concept result for PZ B42.20.2.
--
-- The public Workshop Lua environment cannot open an HTTP/AAC stream:
--   * vehicle emitters resolve strings as local GameSound names;
--   * luajava is not exposed to multiplayer clients, so fmod.javafmod cannot
--     be reached from a normal Workshop mod.
--
-- Keep this client file deliberately inert. Earlier PoC versions registered an
-- OnTick vehicle scan even after the audio backend failed to initialize. That
-- produced a nil-call error every 15 ticks without any chance of playing audio.

local MOD_TAG = "[LCC Internet Radio PoC]"

local function log(message)
    print(MOD_TAG .. " " .. tostring(message))
end

Events.OnGameStart.Add(function()
    log("0.3.1 loaded in safe-disabled mode")
    log("B42.20.2 multiplayer exposes no Workshop-only HTTP/AAC playback API; no tick handler was registered")
end)
