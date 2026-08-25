require("PZShareMapNotes_Shared")

-- Reference to the symbolsAPI (set when ISWorldMap is created)
local symbolsAPI = nil
-- Whether patches have been applied
local patched = false

--- Store a reference to the symbolsAPI from the map UI.
--- Called from the createChildren patch in PZShareMapNotes_Client.lua.
function PZShareMapNotes.setSymbolsAPI(api)
    symbolsAPI = api
end

--- Expose the current symbolsAPI for tests / diagnostics. Returns nil if the
--- map UI hasn't been opened yet this session.
function PZShareMapNotes.getSymbolsAPI()
    return symbolsAPI
end

--- Get sandbox option value with fallback.
local function getSandboxOption(name, default)
    if SandboxVars and SandboxVars.PZShareMapNotes and SandboxVars.PZShareMapNotes[name] ~= nil then
        return SandboxVars.PZShareMapNotes[name]
    end
    return default
end

--- Build a sharing config table from the AutoShareSymbols sandbox option.
--- Returns nil if auto-share is disabled.
local function getSharingConfig()
    local option = getSandboxOption("AutoShareSymbols", 1)
    if option == 1 then
        return { everyone = true }
    elseif option == 2 then
        return { faction = true }
    elseif option == 3 then
        return { safehouse = true }
    end
    return nil
end

--- Share symbols added since `oldCount`. Used by the addSymbol/onNoteAdded
--- patches: they capture the symbol count before invoking the vanilla
--- method, and after, we share only the entries at indices >= oldCount —
--- i.e. symbols that did not exist before the user's action.
---
--- Index-based filtering (rather than `isAuthorLocalPlayer`) is what makes
--- this safe: the world's 63 baked Knox County labels live at the start
--- of the symbol list and are never re-added by user actions, so a
--- delta-from-oldCount window can never include them. `isAuthorLocalPlayer`
--- can't be used here because it returns false for symbols that have only
--- just been created client-side and not yet round-tripped to the server.
local function shareSymbolsAddedSince(oldCount)
    if not symbolsAPI then return end
    if not isClient() then return end

    local config = getSharingConfig()
    if not config then return end

    local newCount = symbolsAPI:getSymbolCount()
    if newCount <= oldCount then return end

    -- Collect first, then share — setSharing() removes the symbol from the
    -- internal local-layer list, which would invalidate later indices.
    local toShare = {}
    for i = oldCount, newCount - 1 do
        local sym = symbolsAPI:getSymbolByIndex(i)
        if sym and sym:canClientModify() and not sym:isShared() then
            table.insert(toShare, sym)
        end
    end
    for _, sym in ipairs(toShare) do
        sym:setSharing(config)
    end
end

--- Diagnostic / test helper: share any local-player-authored symbols in
--- the entire current symbol set. NOT used by production code — exposed
--- so tests can deliberately try to share things and verify the filter
--- excludes world-baked labels (T3 in PZShareMapNotes_Tests.lua).
local function shareNewSymbols()
    if not symbolsAPI then return end
    if not isClient() then return end

    local config = getSharingConfig()
    if not config then return end

    local toShare = {}
    local count = symbolsAPI:getSymbolCount()
    for i = 0, count - 1 do
        local sym = symbolsAPI:getSymbolByIndex(i)
        if sym
            and sym:canClientModify()
            and not sym:isShared()
            and sym:isAuthorLocalPlayer()
        then
            table.insert(toShare, sym)
        end
    end
    for _, sym in ipairs(toShare) do
        sym:setSharing(config)
    end
end

--- Patch vanilla symbol creation methods to auto-share immediately.
local function patchSymbolCreation()
    if patched then return end
    if not ISWorldMapSymbolTool_AddSymbol or not ISWorldMapSymbolTool_AddNote then return end
    patched = true

    local origAddSymbol = ISWorldMapSymbolTool_AddSymbol.addSymbol
    ISWorldMapSymbolTool_AddSymbol.addSymbol = function(self, x, y)
        local oldCount = symbolsAPI and symbolsAPI:getSymbolCount() or 0
        origAddSymbol(self, x, y)
        shareSymbolsAddedSince(oldCount)
    end

    local origOnNoteAdded = ISWorldMapSymbolTool_AddNote.onNoteAdded
    ISWorldMapSymbolTool_AddNote.onNoteAdded = function(self, button, playerNum)
        local oldCount = symbolsAPI and symbolsAPI:getSymbolCount() or 0
        origOnNoteAdded(self, button, playerNum)
        shareSymbolsAddedSince(oldCount)
    end

    print("[PZShareMapNotes] Symbol creation patches applied.")
end

--- Reset tracker state on disconnect.
local function onMainMenuEnter()
    symbolsAPI = nil
    -- Re-apply patches when classes are available
    patchSymbolCreation()
end

Events.OnMainMenuEnter.Add(onMainMenuEnter)
-- NOTE: bulk-sharing on OnGameStart was removed — sharing now only happens
-- through the addSymbol/onNoteAdded patches, where we know the symbol was
-- just created by user action.

-- Try patching immediately in case classes are already loaded
patchSymbolCreation()

-- Expose for tests / diagnostics
PZShareMapNotes.shareNewSymbols = shareNewSymbols
PZShareMapNotes.shareSymbolsAddedSince = shareSymbolsAddedSince

print("[PZShareMapNotes] SymbolTracker module loaded.")
