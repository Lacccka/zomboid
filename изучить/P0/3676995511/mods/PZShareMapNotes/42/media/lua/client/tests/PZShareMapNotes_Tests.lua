require("PZShareMapNotes_Shared")

-- Diagnostic tests for the "city names duplicated / pre-discovery / editable"
-- bugs reported on Workshop. These tests do NOT modify game state — they open
-- the world map, dump every symbol the mod can see, and (in the third test)
-- call shareNewSymbols so we can observe exactly which symbols the mod marks
-- as shared. Output is logged to console.txt with a [PZShareMapNotes-Test]
-- prefix; grep for `PZShareMapNotes-Test` to read results.

-- Town center coordinates pulled from vanilla StashDescriptions/*StashDesc.lua.
local TOWNS = {
    Riverside  = { x = 6800,  y = 5400  },
    Muldraugh  = { x = 10663, y = 9764  },
    WestPoint  = { x = 10941, y = 6726  },
}

local LOG = "[PZShareMapNotes-Test]"

local function safeCall(fn, default)
    local ok, val = pcall(fn)
    if ok then return val end
    return default
end

local function describeSymbol(sym, idx)
    local kind     = "?"
    local label    = "?"
    if safeCall(function() return sym:isText() end, false) then
        kind  = "Note"
        label = safeCall(function() return sym:getTranslatedText() end, "(no-text)")
    elseif safeCall(function() return sym:isTexture() end, false) then
        kind  = "Symbol"
        label = safeCall(function() return sym:getSymbolID() end, "(no-id)")
    else
        kind = "Mystery"
    end
    label = tostring(label):gsub("\n", "\\n"):gsub("\r", "")

    local author    = safeCall(function() return sym:getAuthor() end, "(none)")
    local isLocal   = safeCall(function() return sym:isAuthorLocalPlayer() end, "?")
    local canModify = safeCall(function() return sym:canClientModify() end, "?")
    local isShared  = safeCall(function() return sym:isShared() end, "?")

    return string.format(
        "%s   [%d] %-7s text=%q author=%q local=%s canMod=%s shared=%s",
        LOG, idx, kind, label, tostring(author),
        tostring(isLocal), tostring(canModify), tostring(isShared))
end

local function dumpSymbols(label)
    local api = PZShareMapNotes.getSymbolsAPI and PZShareMapNotes.getSymbolsAPI() or nil
    if not api then
        print(LOG .. " " .. label .. ": symbolsAPI is nil (map UI not initialised yet)")
        return 0, 0, 0
    end
    local count = safeCall(function() return api:getSymbolCount() end, 0)
    print(string.format("%s === %s === count=%d", LOG, label, count))

    local unsharedLocal, unsharedNonLocal = 0, 0
    for i = 0, count - 1 do
        local sym = safeCall(function() return api:getSymbolByIndex(i) end, nil)
        if sym then
            print(describeSymbol(sym, i))
            local isShared  = safeCall(function() return sym:isShared() end, true)
            local isLocal   = safeCall(function() return sym:isAuthorLocalPlayer() end, false)
            local canModify = safeCall(function() return sym:canClientModify() end, false)
            if not isShared and canModify then
                if isLocal then unsharedLocal = unsharedLocal + 1
                else unsharedNonLocal = unsharedNonLocal + 1 end
            end
        end
    end
    print(string.format("%s   summary: unshared+canMod local=%d, non-local=%d",
        LOG, unsharedLocal, unsharedNonLocal))
    return count, unsharedLocal, unsharedNonLocal
end

local function closeMap()
    if ISWorldMap_instance then
        pcall(function() ISWorldMap_instance:setVisible(false) end)
        pcall(function() ISWorldMap_instance:removeFromUIManager() end)
        ISWorldMap_instance = nil
    end
end

local function openMapAt(player, x, y)
    closeMap()
    if ISWorldMap and ISWorldMap.ShowWorldMap then
        local pNum = 0  -- local player on this client
        if player and player.getIndex then
            pNum = safeCall(function() return player:getIndex() end, 0)
        end
        ISWorldMap.ShowWorldMap(pNum, x, y, 18.0)
    end
end

_PZTestRegistrations = _PZTestRegistrations or {}
table.insert(_PZTestRegistrations, function()
    return {
        -- ---------------------------------------------------------------
        -- Test 1 — Baseline: open the map at the player's spawn location
        -- and dump everything the symbol API contains. Establishes what's
        -- in the symbol set with no teleport / no town-area exposure.
        -- ---------------------------------------------------------------
        {
            name = "ShareMapNotes: T1 baseline symbol dump at spawn",
            setup = function(player)
                openMapAt(player, player:getX(), player:getY())
            end,
            setupWaitFrames = 120,
            run = function(player)
                dumpSymbols("T1 baseline @ spawn (" ..
                    math.floor(player:getX()) .. "," ..
                    math.floor(player:getY()) .. ")")
                closeMap()
            end,
        },

        -- ---------------------------------------------------------------
        -- Test 2 — Teleport to Riverside, open map, dump. If new entries
        -- appear here that weren't in T1, those are discovery-driven
        -- symbols the mod could be (incorrectly) auto-sharing.
        -- ---------------------------------------------------------------
        {
            name = "ShareMapNotes: T2 dump after teleport to Riverside",
            setup = function(player)
                PZTest.teleport(player, TOWNS.Riverside.x, TOWNS.Riverside.y, 0)
            end,
            setupWaitFrames = 180,  -- 6s for teleport + chunk load
            run = function(player)
                openMapAt(player, TOWNS.Riverside.x, TOWNS.Riverside.y)
            end,
            waitFrames = 120,  -- 4s for map UI + symbol layer to populate
            assert = function(player)
                local _, _, nonLocal = dumpSymbols("T2 Riverside pre-share")
                closeMap()
                -- Sanity: after a teleport into a town the symbol set
                -- should be non-empty (otherwise this test isn't telling
                -- us anything useful and we should investigate).
                PZTest.assertNotNil(nonLocal,
                    "expected symbol set readable after Riverside teleport")
            end,
        },

        -- ---------------------------------------------------------------
        -- Test 3 — At Riverside, call PZShareMapNotes.shareNewSymbols()
        -- and dump again. Anything that flips from shared=false to
        -- shared=true is what the mod is broadcasting. If any of those
        -- have local=false, that's our smoking gun.
        -- ---------------------------------------------------------------
        {
            name = "ShareMapNotes: T3 observe shareNewSymbols at Riverside",
            sandbox = {
                -- Ensure auto-share is on so shareNewSymbols actually does
                -- something. AutoShareSymbols=1 is "Everyone".
                ["PZShareMapNotes.AutoShareSymbols"] = 1,
            },
            setup = function(player)
                PZTest.teleport(player, TOWNS.Riverside.x, TOWNS.Riverside.y, 0)
            end,
            setupWaitFrames = 180,
            run = function(player)
                openMapAt(player, TOWNS.Riverside.x, TOWNS.Riverside.y)
            end,
            waitFrames = 120,
            assert = function(player)
                local _, preLocal, preNonLocal = dumpSymbols("T3 Riverside pre-share")
                PZTest.assertNotNil(PZShareMapNotes.shareNewSymbols,
                    "PZShareMapNotes.shareNewSymbols must be exposed for tests")
                pcall(function() PZShareMapNotes.shareNewSymbols() end)
                local _, postLocal, postNonLocal = dumpSymbols("T3 Riverside post-share")

                print(string.format(
                    "%s T3 delta: unshared+canMod local %d->%d, non-local %d->%d",
                    LOG, preLocal, postLocal, preNonLocal, postNonLocal))
                closeMap()
                -- Bug signature: any unshared non-local-author symbols
                -- existed before the call AND got marked shared by the mod
                -- (i.e. postNonLocal < preNonLocal). Asserting equality
                -- means: while the bug is live this test is RED; once the
                -- fix lands and the mod stops touching non-local symbols,
                -- it goes GREEN.
                PZTest.assertEqual(postNonLocal, preNonLocal,
                    "shareNewSymbols must not mark non-local-author " ..
                    "symbols (e.g. town labels) as shared")
            end,
        },

        -- ---------------------------------------------------------------
        -- Test 4 — Regression check for the legitimate sharing path.
        -- Captures the symbol count, programmatically adds a Note, then
        -- calls shareSymbolsAddedSince(oldCount) — the same flow the
        -- patched addSymbol/onNoteAdded handlers use. Asserts the new
        -- symbol becomes shared, proving the index-based filter passes
        -- user-placed content through.
        --
        -- Note (sic): isAuthorLocalPlayer() returns false on a freshly-
        -- created symbol because author isn't assigned client-side — the
        -- mod intentionally does NOT filter on it in the production path
        -- for this reason. setSharing() itself doesn't check author, so
        -- the share completes regardless.
        --
        -- Side-effect: if this test passes, a shared "PZSHAREMAPNOTES_TEST_NOTE"
        -- ends up in the server's shared symbol layer at the test
        -- coordinates. Run /removemapsymbolsforuser <hostname> to clear
        -- if it accumulates across many test runs.
        -- ---------------------------------------------------------------
        {
            name = "ShareMapNotes: T4 user-placed note gets shared",
            sandbox = {
                ["PZShareMapNotes.AutoShareSymbols"] = 1,
            },
            setup = function(player)
                openMapAt(player, player:getX(), player:getY())
            end,
            setupWaitFrames = 120,
            run = function(player)
                local api = PZShareMapNotes.getSymbolsAPI()
                PZTest.assertNotNil(api, "symbolsAPI must be available")

                local oldCount = safeCall(function() return api:getSymbolCount() end, 0)
                local layerID = safeCall(function()
                    return api:getDefaultTextLayerID()
                end, "text-place")
                local sym = api:addUntranslatedText(
                    "PZSHAREMAPNOTES_TEST_NOTE",
                    layerID, player:getX(), player:getY())
                pcall(function() sym:setRGBA(1, 0, 0, 1) end)
                pcall(function() sym:setAnchor(0.5, 0.5) end)
                pcall(function() sym:setScale(1.0) end)

                local preLocal   = safeCall(function() return sym:isAuthorLocalPlayer() end, "?")
                local preCanMod  = safeCall(function() return sym:canClientModify() end, "?")
                local preShared  = safeCall(function() return sym:isShared() end, "?")
                print(string.format(
                    "%s T4 pre-share: oldCount=%d local=%s canMod=%s shared=%s",
                    LOG, oldCount, tostring(preLocal), tostring(preCanMod), tostring(preShared)))

                PZTest.assertNotNil(PZShareMapNotes.shareSymbolsAddedSince,
                    "PZShareMapNotes.shareSymbolsAddedSince must be exposed")
                pcall(function()
                    PZShareMapNotes.shareSymbolsAddedSince(oldCount)
                end)

                _G._PZSMN_test4_sym = sym
            end,
            waitFrames = 240,  -- 8s for share round-trip in MP
            assert = function(player)
                local sym = _G._PZSMN_test4_sym
                PZTest.assertNotNil(sym, "test note reference should persist")

                local isShared = safeCall(function() return sym:isShared() end, false)
                local canModify = safeCall(function() return sym:canClientModify() end, false)
                local isLocal = safeCall(function() return sym:isAuthorLocalPlayer() end, false)
                print(string.format(
                    "%s T4 post-share: local=%s canMod=%s shared=%s",
                    LOG, tostring(isLocal), tostring(canModify), tostring(isShared)))

                _G._PZSMN_test4_sym = nil
                closeMap()

                PZTest.assertTrue(isShared,
                    "shareSymbolsAddedSince must mark the newly-added note as shared")
            end,
        },

        -- ---------------------------------------------------------------
        -- Test 5 — Shared note deletion routes through sendRemoveSymbol.
        --
        -- The mod patches ISWorldMapSymbolTool_RemoveAnnotation so that
        -- when a shared note is being deleted, we send the removal to
        -- the server (matching vanilla's symbol-icon path) instead of
        -- vanilla's local-only removeSymbolByIndex.
        --
        -- Behavioral test: the patched shared branch is async (server
        -- round-trip), the unshared/fallthrough branch is synchronous
        -- (immediate local removal). So if our patch ran correctly, the
        -- symbol count must be UNCHANGED immediately after the call.
        -- If the count dropped by 1, vanilla's local path ran — bug.
        -- ---------------------------------------------------------------
        {
            name = "ShareMapNotes: T5 shared note delete routes to server",
            sandbox = {
                ["PZShareMapNotes.AutoShareSymbols"] = 1,
            },
            setup = function(player)
                openMapAt(player, player:getX(), player:getY())
            end,
            setupWaitFrames = 120,
            run = function(player)
                local api = PZShareMapNotes.getSymbolsAPI()
                PZTest.assertNotNil(api, "symbolsAPI must be available")
                PZTest.assertNotNil(ISWorldMapSymbolTool_RemoveAnnotation,
                    "ISWorldMapSymbolTool_RemoveAnnotation must be loaded")

                local layerID = safeCall(function()
                    return api:getDefaultTextLayerID()
                end, "text-place")

                local oldCount = safeCall(function() return api:getSymbolCount() end, 0)
                local marker = "PZSMN_T5_" .. tostring(getTimestampMs())
                local sym = api:addUntranslatedText(
                    marker, layerID, player:getX(), player:getY())
                pcall(function() sym:setRGBA(1, 0, 0, 1) end)
                pcall(function() sym:setAnchor(0.5, 0.5) end)
                pcall(function() sym:setScale(1.0) end)

                pcall(function()
                    PZShareMapNotes.shareSymbolsAddedSince(oldCount)
                end)

                _G._PZSMN_test5 = { sym = sym, marker = marker }
            end,
            waitFrames = 240,  -- 8s for share round-trip
            assert = function(player)
                local ctx = _G._PZSMN_test5
                PZTest.assertNotNil(ctx, "test 5 context must persist")

                local api = PZShareMapNotes.getSymbolsAPI()
                local sym = ctx.sym
                local marker = ctx.marker
                local isShared = safeCall(function() return sym:isShared() end, false)
                PZTest.assertTrue(isShared,
                    "test 5 precondition: note must be shared before delete")

                -- Locate the note by its unique text marker. We can't compare
                -- Java userdata refs with `==` because Kahlua may wrap the same
                -- Java object in fresh proxies on each bridge crossing.
                local countBefore = safeCall(function() return api:getSymbolCount() end, 0)
                local foundIdx = nil
                for i = 0, countBefore - 1 do
                    local s = safeCall(function() return api:getSymbolByIndex(i) end, nil)
                    if s and safeCall(function() return s:getTranslatedText() end, nil) == marker then
                        foundIdx = i
                        break
                    end
                end
                PZTest.assertNotNil(foundIdx,
                    "note should still be locatable by marker before delete")

                local fakeSelf = {
                    symbolsAPI = api,
                    symbolsUI  = { mouseOverNote = foundIdx },
                }
                local result = ISWorldMapSymbolTool_RemoveAnnotation.removeAnnotation(fakeSelf)

                local countAfter = safeCall(function() return api:getSymbolCount() end, 0)
                print(string.format(
                    "%s T5 delete: shared=%s idx=%d countBefore=%d countAfter=%d result=%s",
                    LOG, tostring(isShared), foundIdx, countBefore, countAfter, tostring(result)))

                _G._PZSMN_test5 = nil
                closeMap()

                PZTest.assertTrue(result,
                    "patched removeAnnotation must return true for shared notes")
                -- The shared branch is async: server confirms removal later.
                -- Vanilla's local-only path is synchronous (would drop count
                -- immediately). Equal count proves the patched branch ran.
                PZTest.assertEqual(countAfter, countBefore,
                    "shared note delete must NOT remove the symbol locally — " ..
                    "server confirmation drives the visual update")
            end,
        },

        -- ---------------------------------------------------------------
        -- Test 6 — Unshared note deletion falls through to vanilla.
        --
        -- A locally-authored note that hasn't been shared yet must take
        -- the original removeAnnotation path (synchronous local removal).
        -- Asserts the symbol count drops by exactly 1 immediately after,
        -- proving vanilla's removeSymbolByIndex ran rather than our
        -- server-routed branch.
        -- ---------------------------------------------------------------
        {
            name = "ShareMapNotes: T6 unshared note delete falls through to vanilla",
            -- Note: this test bypasses ISWorldMapSymbolTool_AddNote (calls
            -- api:addUntranslatedText directly), so SymbolTracker's auto-share
            -- patch never fires regardless of AutoShareSymbols sandbox value.
            -- The created note is guaranteed unshared at the moment of delete.
            setup = function(player)
                openMapAt(player, player:getX(), player:getY())
            end,
            setupWaitFrames = 120,
            run = function(player)
                local api = PZShareMapNotes.getSymbolsAPI()
                PZTest.assertNotNil(api, "symbolsAPI must be available")
                PZTest.assertNotNil(ISWorldMapSymbolTool_RemoveAnnotation,
                    "ISWorldMapSymbolTool_RemoveAnnotation must be loaded")

                local layerID = safeCall(function()
                    return api:getDefaultTextLayerID()
                end, "text-place")

                local countInitial = safeCall(function() return api:getSymbolCount() end, 0)
                local sym = api:addUntranslatedText(
                    "PZSMN_T6_" .. tostring(getTimestampMs()),
                    layerID, player:getX(), player:getY())
                pcall(function() sym:setRGBA(0, 1, 0, 1) end)
                pcall(function() sym:setAnchor(0.5, 0.5) end)
                pcall(function() sym:setScale(1.0) end)

                local countWithNote = safeCall(function() return api:getSymbolCount() end, 0)
                PZTest.assertEqual(countWithNote, countInitial + 1,
                    "addUntranslatedText must add exactly one symbol")

                local isShared = safeCall(function() return sym:isShared() end, true)
                PZTest.assertEqual(isShared, false,
                    "test 6 precondition: new note must NOT be shared yet")

                local fakeSelf = {
                    symbolsAPI = api,
                    symbolsUI  = { mouseOverNote = countInitial },  -- index of the new note
                }
                local result = ISWorldMapSymbolTool_RemoveAnnotation.removeAnnotation(fakeSelf)

                local countAfter = safeCall(function() return api:getSymbolCount() end, 0)
                print(string.format(
                    "%s T6 delete: shared=%s initial=%d withNote=%d after=%d result=%s",
                    LOG, tostring(isShared), countInitial, countWithNote, countAfter, tostring(result)))

                closeMap()

                PZTest.assertTrue(result,
                    "vanilla removeAnnotation returns true when mouseOverNote is set")
                -- Vanilla's local removeSymbolByIndex is synchronous — count
                -- must drop back to initial (the added note is gone).
                PZTest.assertEqual(countAfter, countInitial,
                    "unshared note delete must remove locally (back to initial count)")
            end,
        },

        -- ---------------------------------------------------------------
        -- Test 7 — Engine API surface guard for the sharing-mode filters.
        --
        -- The server called SafeHouse.getSafehouses() for months. That
        -- method has never existed in B42 (it is getSafehouseList()), so
        -- Safehouse sharing mode threw "Object tried to call nil" on every
        -- broadcast: no strokes synced, and share/remove died mid-mutation
        -- without persisting.
        --
        -- It stayed invisible because the visibility check short-circuits
        -- on "viewer == author", so a solo Host & Play session never
        -- reaches the throwing branch. A nil static costs nothing to
        -- assert directly, so do that instead of hoping a second player
        -- happens to be connected during testing.
        -- ---------------------------------------------------------------
        {
            name = "ShareMapNotes: T7 faction/safehouse engine APIs resolve",
            run = function(player)
                PZTest.assertNotNil(SafeHouse,
                    "SafeHouse class must be exposed to Lua")
                PZTest.assertNotNil(SafeHouse.getSafehouseList,
                    "SafeHouse.getSafehouseList must exist")
                -- Wrapped: depending on Kahlua's class metatable, looking up
                -- an unknown static may return nil or raise. Either outcome
                -- means "not usable", which is what we're asserting.
                PZTest.assertNil(
                    safeCall(function() return SafeHouse.getSafehouses end, nil),
                    "SafeHouse.getSafehouses does not exist in B42 — " ..
                    "the mod must call getSafehouseList()")

                PZTest.assertNotNil(Faction,
                    "Faction class must be exposed to Lua")
                PZTest.assertNotNil(Faction.getFactions,
                    "Faction.getFactions must exist")

                -- Both must return iterable Java lists even when empty.
                local safehouses = SafeHouse.getSafehouseList()
                PZTest.assertNotNil(safehouses,
                    "getSafehouseList() must return a list")
                local factions = Faction.getFactions()
                PZTest.assertNotNil(factions,
                    "getFactions() must return a list")

                local shCount = safeCall(function() return safehouses:size() end, -1)
                local fCount  = safeCall(function() return factions:size() end, -1)
                PZTest.assertGte(shCount, 0, "safehouse list must be sizeable")
                PZTest.assertGte(fCount, 0, "faction list must be sizeable")

                -- Owner-awareness: getPlayers() excludes the owner on both
                -- types, so the mod must also test isOwner()/getOwner().
                -- Verify those accessors exist on any live instance.
                if shCount > 0 then
                    local sh = safehouses:get(0)
                    PZTest.assertNotNil(safeCall(function() return sh:getOwner() end, nil),
                        "SafeHouse:getOwner() must be callable — owners are " ..
                        "NOT in getPlayers()")
                end
                if fCount > 0 then
                    local f = factions:get(0)
                    PZTest.assertNotNil(safeCall(function() return f:isOwner("") end, nil),
                        "Faction:isOwner() must be callable — owners are " ..
                        "NOT in getPlayers()")
                end

                print(string.format("%s T7: safehouses=%d factions=%d",
                    LOG, shCount, fCount))
            end,
        },

        -- ---------------------------------------------------------------
        -- Test 8 — Faction lookup finds you when you OWN the faction.
        --
        -- This is the one part of the faction fix a SINGLE player can
        -- verify. The visibility filter short-circuits on
        -- "viewer == author", so solo play never reaches the group lookup
        -- through normal gameplay — but the lookup is exactly where the
        -- bug was: Faction:isMember() scans the members list only, and the
        -- owner is never in it, so getPlayerFaction() returned nil for
        -- the faction leader. Their drawings then reached nobody.
        --
        -- Unlike the offline harness, this runs the real Kahlua bridge
        -- calls (Faction.getFactions / isOwner / isMember).
        --
        -- SKIPS (does not fail) if you don't own a faction — create one
        -- via the User panel and re-run to get real coverage.
        -- ---------------------------------------------------------------
        {
            name = "ShareMapNotes: T8 faction lookup finds the faction you own",
            run = function(player)
                local lookup = PZShareMapNotes._debugGetPlayerFaction
                if not lookup then
                    print(LOG .. " T8 SKIPPED — server Lua not loaded on this" ..
                        " client (expected on a remote dedicated-server client)")
                    return
                end

                local username = player:getUsername()
                local factions = Faction.getFactions()
                local owned = nil
                for i = 0, safeCall(function() return factions:size() end, 0) - 1 do
                    local f = safeCall(function() return factions:get(i) end, nil)
                    if f and safeCall(function() return f:isOwner(username) end, false) then
                        owned = f
                        break
                    end
                end

                if not owned then
                    print(LOG .. " T8 SKIPPED — " .. tostring(username) ..
                        " does not own a faction. Create one (User panel >" ..
                        " Faction) and re-run for real coverage.")
                    return
                end

                local found = safeCall(function() return lookup(username) end, nil)
                print(string.format("%s T8: owner=%s lookup=%s",
                    LOG, tostring(username), tostring(found ~= nil)))
                PZTest.assertNotNil(found,
                    "getPlayerFaction must find the faction you OWN — owners " ..
                    "are not in getPlayers(), which is the bug this guards")
            end,
        },

        -- ---------------------------------------------------------------
        -- Test 9 — Safehouse lookup finds the safehouse you own.
        --
        -- Same solo-verifiable idea as T8, and it covers BOTH safehouse
        -- bugs at once: the old code called SafeHouse.getSafehouses(),
        -- which does not exist (it is getSafehouseList()), so this lookup
        -- threw outright; and even once renamed it would still have
        -- missed the owner, who is not in getPlayers().
        --
        -- SKIPS if you don't own a safehouse — claim one and re-run.
        -- ---------------------------------------------------------------
        {
            name = "ShareMapNotes: T9 safehouse lookup finds the safehouse you own",
            run = function(player)
                local lookup = PZShareMapNotes._debugGetPlayerSafehouse
                if not lookup then
                    print(LOG .. " T9 SKIPPED — server Lua not loaded on this" ..
                        " client (expected on a remote dedicated-server client)")
                    return
                end

                local username = player:getUsername()
                local list = SafeHouse.getSafehouseList()
                local owned = nil
                for i = 0, safeCall(function() return list:size() end, 0) - 1 do
                    local sh = safeCall(function() return list:get(i) end, nil)
                    if sh and tostring(safeCall(function() return sh:getOwner() end, nil)) == username then
                        owned = sh
                        break
                    end
                end

                if not owned then
                    print(LOG .. " T9 SKIPPED — " .. tostring(username) ..
                        " does not own a safehouse. Claim one and re-run for" ..
                        " real coverage.")
                    return
                end

                local found = safeCall(function() return lookup(username) end, nil)
                print(string.format("%s T9: owner=%s lookup=%s",
                    LOG, tostring(username), tostring(found ~= nil)))
                PZTest.assertNotNil(found,
                    "getPlayerSafehouse must find the safehouse you OWN — the " ..
                    "old code called the nonexistent SafeHouse.getSafehouses() " ..
                    "and threw here")
            end,
        },
    }
end)
