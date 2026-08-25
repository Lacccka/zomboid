-- ============================================================
-- PagerMod_MapPing.lua
-- Drops a temporary marker on the in-game world map at an SOS
-- sender's location, then removes it again a few seconds later.
-- Uses the vanilla map-symbols API (the same one behind the map
-- screen's own "add symbol" tool), reusing a built-in icon so no
-- new art asset is needed.
--
-- IMPORTANT: this API (javaObject:getAPIv3():getSymbolsAPIv2())
-- only exists in Build 42. Build 41's world map is a different
-- (v1) API with no getSymbolsAPIv2(), so on B41 the call throws.
-- We therefore PROBE the API once and, if it isn't there, switch
-- the whole feature off — otherwise the throw repeats every render
-- frame and floods the debug log (KahluaUtil.fail loop), which is
-- exactly what happened on live B41 servers.
-- ============================================================

require "PagerMod_Client"

local PING_SECONDS = 15
local PING_SYMBOL = "Exclamation"

-- Pending pings not yet turned into a real map symbol (added lazily, the next
-- time the map screen renders) and active ones already on the map awaiting
-- removal once their time is up.
PagerMod._sosPings = PagerMod._sosPings or {}
PagerMod._sosPingSymbols = PagerMod._sosPingSymbols or {}

-- Tri-state capability latch for the map-symbols API on this build:
--   nil   = not probed yet
--   true  = usable (Build 42)
--   false = unavailable -> feature disabled for the session (Build 41, etc.)
-- Once false, we never touch the map API again, so no repeated errors.
local symbolsUsable = nil

-- Queue a temporary map marker at (x, y). Safe to call whether or not the map
-- screen is open: the marker is added the next time it renders, as long as it
-- hasn't already expired by then; if it has, it's just dropped.
function PagerMod.addSOSMapPing(x, y, seconds)
    if symbolsUsable == false then return end
    x, y = tonumber(x), tonumber(y)
    if not x or not y then return end
    table.insert(PagerMod._sosPings, { x = x, y = y, expire = getTimestampMs() + (seconds or PING_SECONDS) * 1000 })
end

-- Resolve the current map's v2 symbols API, or nil. This is the ONE call that
-- can throw on an unsupported build, so it's the only thing we probe. A hard
-- failure latches symbolsUsable = false (disable for good); a nil mapAPI is
-- treated as transient (map not ready yet) and left un-latched so a later frame
-- can retry without disabling the feature.
local function resolveSymbolsAPI(mapUI)
    if not mapUI or not mapUI.mapAPI then return nil end
    local ok, api = pcall(function() return mapUI.mapAPI:getSymbolsAPIv2() end)
    if not ok or not api then
        symbolsUsable = false
        return nil
    end
    symbolsUsable = true
    return api
end

-- Hooked into ISWorldMap:render (below): runs every frame the map screen is
-- open. Promotes pending pings into on-map symbols and removes expired ones.
local function pumpSOSPings(mapUI)
    if symbolsUsable == false then
        -- Feature disabled on this build: drop anything queued and never retry.
        PagerMod._sosPings = {}
        PagerMod._sosPingSymbols = {}
        return
    end
    if #PagerMod._sosPings == 0 and #PagerMod._sosPingSymbols == 0 then return end

    local symbolsAPI = resolveSymbolsAPI(mapUI)
    if not symbolsAPI then
        -- If the probe latched the feature off, clear the queue so the top-of-
        -- function guard silently short-circuits every future frame. If it was
        -- only a transient nil mapAPI, leave the queue for a later frame.
        if symbolsUsable == false then
            PagerMod._sosPings = {}
            PagerMod._sosPingSymbols = {}
        end
        return
    end

    local now = getTimestampMs()

    -- Promote pending pings to on-map symbols (skip any already expired).
    if #PagerMod._sosPings > 0 then
        local pending = PagerMod._sosPings
        PagerMod._sosPings = {}
        for _, ping in ipairs(pending) do
            if ping.expire > now then
                local addOk, symbol = pcall(function()
                    local s = symbolsAPI:addTexture(PING_SYMBOL, ping.x, ping.y)
                    s:setRGBA(1.0, 0.35, 0.2, 1.0)
                    s:setAnchor(0.5, 0.5)
                    s:setScale(1.0)
                    return s
                end)
                if addOk and symbol then
                    table.insert(PagerMod._sosPingSymbols, { symbol = symbol, expire = ping.expire })
                end
            end
        end
    end

    -- Remove expired symbols.
    for i = #PagerMod._sosPingSymbols, 1, -1 do
        local active = PagerMod._sosPingSymbols[i]
        if active.expire <= now then
            pcall(function() symbolsAPI:removeSymbol(active.symbol) end)
            table.remove(PagerMod._sosPingSymbols, i)
        end
    end
end

if ISWorldMap then
    local origRender = ISWorldMap.render
    function ISWorldMap:render()
        origRender(self)
        pcall(pumpSOSPings, self)
    end

    -- Map symbols added via addTexture are SAVED to disk (persistent map
    -- annotations). If the player closes the map before a ping expires, the
    -- per-frame removal above never runs, so remove whatever is still active on
    -- close — otherwise a stray marker could persist on their map for good.
    local origClose = ISWorldMap.close
    if origClose then
        function ISWorldMap:close()
            if symbolsUsable == true and #PagerMod._sosPingSymbols > 0 and self.mapAPI then
                pcall(function()
                    local api = self.mapAPI:getSymbolsAPIv2()
                    for i = 1, #PagerMod._sosPingSymbols do
                        api:removeSymbol(PagerMod._sosPingSymbols[i].symbol)
                    end
                end)
            end
            PagerMod._sosPingSymbols = {}
            origClose(self)
        end
    end
end
