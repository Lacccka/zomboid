require("PZShareMapNotes_Shared")

local MOD_ID = PZShareMapNotes.MOD_ID

--- Button labels and state strings. Resolved through getText() so the map UI
--- follows each player's own client locale.
---
--- Keys MUST start with `IGUI_` and live in shared/Translate/<LOCALE>/IG_UI.json.
--- Translator.getTextInternal dispatches on the key PREFIX to a fixed set of
--- maps, each loaded from a hardcoded filename (Translator.java BY_NAME) —
--- there is no per-mod translation file. A key with an unrecognised prefix
--- misses every map and getText() returns the key string itself, which is how
--- this shipped once showing literal "PZShareMapNotes_DrawingsDisplay" on the
--- buttons. `IGUI_` is the correct prefix for UI text.
local function L(key)
    return getText("IGUI_PZShareMapNotes_" .. key)
end

-- Toggle state for rendering shared strokes
local showShared = true

-- Shared strokes received from the server, keyed by stroke ID
local sharedStrokes = {}
-- Toggle state for draw mode
local drawModeActive = false
-- Whether we are currently drawing (mouse is down in draw mode)
local isDrawing = false
-- Current stroke being drawn (array of {x, y} world-coordinate points)
local currentStrokePoints = {}
-- Last recorded point (for minimum distance filtering)
local lastPointX = nil
local lastPointY = nil
-- Whether stroke sync has been requested
local strokeSyncRequested = false
-- Batched stroke sync tracking
local pendingStrokeBatches = {}
local expectedStrokeBatches = 0
-- Strokes drawn by the local player (for undo), ordered list of IDs
local localStrokeIds = {}
-- Toggle state for erase mode
local eraseModeActive = false

-- Line thickness
local DRAW_THICKNESS = PZShareMapNotes.DRAW_LINE_THICKNESS
-- Current draw color (set when entering draw mode or starting a stroke)
local currentDrawColor = nil
-- Last canDraw check result and timestamp (avoid checking inventory every frame)
local lastCanDrawCheck = false
local lastCanDrawTime = 0
local CAN_DRAW_CHECK_INTERVAL = 500
-- Last canErase check result and timestamp
local lastCanEraseCheck = false
local lastCanEraseTime = 0

--- Get sandbox option value with fallback.
local function getSandboxOption(name, default)
    if SandboxVars and SandboxVars.PZShareMapNotes and SandboxVars.PZShareMapNotes[name] ~= nil then
        return SandboxVars.PZShareMapNotes[name]
    end
    return default
end

-- Reference to the ISWorldMap instance (set in createChildren patch)
local mapInstance = nil

--- Check if the player can draw (delegates to PZ's native canWrite check).
local function canDraw()
    if not getSandboxOption("RequirePenToDraw", true) then
        return true
    end
    if mapInstance and mapInstance.symbolsUI then
        return mapInstance.symbolsUI:canWrite()
    end
    return false
end

--- Check if the player can erase (requires Base.Eraser in inventory).
local function canErase()
    if not getSandboxOption("RequireEraserToErase", true) then return true end
    local player = getPlayer()
    if player then return player:getInventory():containsType("Eraser") end
    return false
end

--- Get the draw color from PZ's native pen selection UI.
--- Returns {r, g, b} or nil if no pen and pen is required.
local function getDrawColor()
    if not getSandboxOption("RequirePenToDraw", true) then
        return PZShareMapNotes.DEFAULT_DRAW_COLOR
    end

    -- Must have a pen before reading color (PZ always populates currentColor, even without a pen)
    if not canDraw() then return nil end

    -- Use the color the player selected in PZ's map symbols panel
    if mapInstance and mapInstance.symbolsUI and mapInstance.symbolsUI.currentColor then
        local color = mapInstance.symbolsUI.currentColor
        return { r = color:getR(), g = color:getG(), b = color:getB() }
    end

    return nil
end

--- Squared distance from point (px,py) to line segment (x1,y1)-(x2,y2).
local function pointToSegmentDistSq(px, py, x1, y1, x2, y2)
    local dx, dy = x2 - x1, y2 - y1
    local lenSq = dx * dx + dy * dy
    if lenSq == 0 then
        local ex, ey = px - x1, py - y1
        return ex * ex + ey * ey
    end
    local t = math.max(0, math.min(1, ((px - x1) * dx + (py - y1) * dy) / lenSq))
    local cx, cy = x1 + t * dx, y1 + t * dy
    local ex, ey = px - cx, py - cy
    return ex * ex + ey * ey
end

--- Hit test all shared strokes against a click position, returning the closest stroke ID.
local function hitTestStrokes(mapAPI, clickX, clickY)
    local worldX = mapAPI:uiToWorldX(clickX, clickY)
    local worldY = mapAPI:uiToWorldY(clickX, clickY)
    -- Scale hit radius with zoom: larger radius when zoomed out
    local zoom = mapAPI:getZoomF()
    local hitRadius = 3.0 + (zoom * 0.5)
    local hitRadiusSq = hitRadius * hitRadius

    local bestId = nil
    local bestDistSq = hitRadiusSq

    for id, stroke in pairs(sharedStrokes) do
        local pts = stroke.points
        if pts and #pts >= 2 then
            for i = 1, #pts - 1 do
                local dSq = pointToSegmentDistSq(worldX, worldY,
                    pts[i].x, pts[i].y, pts[i+1].x, pts[i+1].y)
                if dSq < bestDistSq then
                    bestDistSq = dSq
                    bestId = id
                end
            end
        end
    end
    return bestId
end

-- ============================================================
-- Stroke Network Handlers
-- ============================================================

--- Handle BroadcastStrokeAdd from server.
local function handleBroadcastStrokeAdd(args)
    if not args or not args.id then return end

    -- Deserialize pointData string into points array for rendering
    if args.pointData and not args.points then
        args.points = PZShareMapNotes.deserializePoints(args.pointData)
        args.pointData = nil
    end

    local player = getPlayer()
    if player and args.author == player:getUsername() then
        table.insert(localStrokeIds, args.id)
    end

    sharedStrokes[args.id] = args
    print("[PZShareMapNotes] Received shared stroke: " .. args.id .. " from " .. tostring(args.author))
end

--- Handle BroadcastStrokeRemove from server.
local function handleBroadcastStrokeRemove(args)
    if not args or not args.id then return end
    sharedStrokes[args.id] = nil
    for i, sid in ipairs(localStrokeIds) do
        if sid == args.id then
            table.remove(localStrokeIds, i)
            break
        end
    end
    print("[PZShareMapNotes] Shared stroke removed: " .. args.id)
end

--- Handle FullStrokeSync from server (all strokes in one payload).
local function handleFullStrokeSync(args)
    if not args or not args.strokes then return end

    sharedStrokes = {}
    localStrokeIds = {}

    local player = getPlayer()
    local username = player and player:getUsername() or ""

    for _, stroke in pairs(args.strokes) do
        -- Deserialize pointData string into points array for rendering
        if stroke.pointData and not stroke.points then
            stroke.points = PZShareMapNotes.deserializePoints(stroke.pointData)
            stroke.pointData = nil
        end
        sharedStrokes[stroke.id] = stroke
        if stroke.author == username then
            table.insert(localStrokeIds, stroke.id)
        end
    end

    local count = 0
    for _ in pairs(sharedStrokes) do count = count + 1 end
    print("[PZShareMapNotes] Stroke sync complete. " .. count .. " shared strokes loaded.")
end

--- Handle FullStrokeSyncBatch from server (batched stroke sync).
local function handleFullStrokeSyncBatch(args)
    if not args or not args.strokes or not args.batchIndex or not args.totalBatches then return end

    if args.batchIndex == 1 then
        sharedStrokes = {}
        localStrokeIds = {}
        pendingStrokeBatches = {}
        expectedStrokeBatches = args.totalBatches
    end

    pendingStrokeBatches[args.batchIndex] = true

    local player = getPlayer()
    local username = player and player:getUsername() or ""

    for _, stroke in pairs(args.strokes) do
        -- Deserialize pointData string into points array for rendering
        if stroke.pointData and not stroke.points then
            stroke.points = PZShareMapNotes.deserializePoints(stroke.pointData)
            stroke.pointData = nil
        end
        sharedStrokes[stroke.id] = stroke
        if stroke.author == username then
            table.insert(localStrokeIds, stroke.id)
        end
    end

    local receivedCount = 0
    for _ in pairs(pendingStrokeBatches) do receivedCount = receivedCount + 1 end

    if receivedCount >= expectedStrokeBatches then
        local count = 0
        for _ in pairs(sharedStrokes) do count = count + 1 end
        print("[PZShareMapNotes] Batched stroke sync complete (" .. expectedStrokeBatches .. " batches). " .. count .. " shared strokes loaded.")
        pendingStrokeBatches = {}
        expectedStrokeBatches = 0
    end
end

--- Remove the player's most recent stroke (undo).
local function undoLastStroke()
    if #localStrokeIds == 0 then return end

    local lastId = localStrokeIds[#localStrokeIds]
    local player = getPlayer()
    if not player then return end

    sendClientCommand(player, MOD_ID, PZShareMapNotes.CMD_REMOVE_STROKE, { id = lastId })
    print("[PZShareMapNotes] Undo stroke requested: " .. lastId)
end

--- Server command dispatcher.
local function onServerCommand(module, command, args)
    if module ~= MOD_ID then return end

    print("[PZShareMapNotes] Received server command: " .. tostring(command))

    if command == PZShareMapNotes.CMD_BROADCAST_STROKE_ADD then
        handleBroadcastStrokeAdd(args)
    elseif command == PZShareMapNotes.CMD_BROADCAST_STROKE_REMOVE then
        handleBroadcastStrokeRemove(args)
    elseif command == PZShareMapNotes.CMD_FULL_STROKE_SYNC then
        handleFullStrokeSync(args)
    elseif command == PZShareMapNotes.CMD_FULL_STROKE_SYNC_BATCH then
        handleFullStrokeSyncBatch(args)
    end
end

--- Singleplayer delivery point. The server half calls this in-process because
--- sendServerCommand cannot reach us solo (see deliverToLocalClient in
--- PZShareMapNotes_Server.lua). Mirrors exactly what Events.OnServerCommand
--- hands us in multiplayer.
function PZShareMapNotes.deliverLocalServerCommand(command, args)
    onServerCommand(MOD_ID, command, args)
end

--- Request stroke sync from server after a short delay.
--- We use OnTick instead of OnGameStart because sendClientCommand requires
--- GameClient.ingame == true, which is only set in IngameState.UpdateStuff()
--- right before OnTick fires. During OnGameStart, ingame is still false and
--- sendClientCommand silently routes to SinglePlayerClient (a no-op in MP).
--- We wait a few ticks to ensure the server has finished processing the
--- player connection before sending the sync request.
local strokeSyncDelayTicks = 0
local STROKE_SYNC_DELAY = 30  -- ~1 second at 30 FPS

local onTickRequestStrokeSync  -- forward declaration (add/remove reference it)

-- Whether onTickRequestStrokeSync is currently registered with Events.OnTick.
-- Without this the handler gets added twice — once at file load and once from
-- the first resetClientState() — which makes it tick twice per frame and
-- halves the settle delay above.
local strokeSyncTickRegistered = false

local function addStrokeSyncTick()
    -- Always restart the delay, even if still registered: a reconnect that
    -- interrupted a previous countdown should get the full settle window.
    strokeSyncDelayTicks = 0
    if strokeSyncTickRegistered then return end
    strokeSyncTickRegistered = true
    Events.OnTick.Add(onTickRequestStrokeSync)
end

local function removeStrokeSyncTick()
    if not strokeSyncTickRegistered then return end
    strokeSyncTickRegistered = false
    Events.OnTick.Remove(onTickRequestStrokeSync)
end

onTickRequestStrokeSync = function()
    if strokeSyncRequested then
        removeStrokeSyncTick()
        return
    end

    strokeSyncDelayTicks = strokeSyncDelayTicks + 1
    if strokeSyncDelayTicks < STROKE_SYNC_DELAY then return end

    -- No local player yet: keep waiting. The 3-arg sendClientCommand silently
    -- fails in Host & Play, so there is no useful fallback to take here.
    local player = getPlayer()
    if not player then return end

    strokeSyncRequested = true
    removeStrokeSyncTick()

    sendClientCommand(player, MOD_ID, PZShareMapNotes.CMD_REQUEST_STROKE_SYNC, {})
    print("[PZShareMapNotes] Stroke sync requested from server (tick " .. strokeSyncDelayTicks .. ").")
end

-- ============================================================
-- ISWorldMap Rendering Patch
-- ============================================================

local originalRender = nil

--- Patch ISWorldMap to render shared strokes after the normal render.
local function patchWorldMapRender()
    if not ISWorldMap or originalRender then return end

    originalRender = ISWorldMap.render

    ISWorldMap.render = function(self)
        -- Call original render
        originalRender(self)

        -- Don't render shared drawings if toggled off
        if not showShared then return end

        -- Get the map API for coordinate conversion
        local api = self.mapAPI
        if not api then return end

        -- ===== Render shared strokes =====
        local defaultColor = PZShareMapNotes.DEFAULT_DRAW_COLOR
        for id, stroke in pairs(sharedStrokes) do
            local pts = stroke.points
            if pts and #pts >= 2 then
                local sr = stroke.r or defaultColor.r
                local sg = stroke.g or defaultColor.g
                local sb = stroke.b or defaultColor.b
                for i = 1, #pts - 1 do
                    local x1 = self.mapAPI:worldToUIX(pts[i].x, pts[i].y)
                    local y1 = self.mapAPI:worldToUIY(pts[i].x, pts[i].y)
                    local x2 = self.mapAPI:worldToUIX(pts[i + 1].x, pts[i + 1].y)
                    local y2 = self.mapAPI:worldToUIY(pts[i + 1].x, pts[i + 1].y)

                    if (x1 > -100 and x1 < self:getWidth() + 100 and y1 > -100 and y1 < self:getHeight() + 100) or
                       (x2 > -100 and x2 < self:getWidth() + 100 and y2 > -100 and y2 < self:getHeight() + 100) then
                        self.javaObject:DrawLine(nil, x1, y1, x2, y2, DRAW_THICKNESS, sr, sg, sb, 0.9)
                    end
                end
            end
        end

        -- ===== Render current stroke being drawn (local preview) =====
        if isDrawing and currentDrawColor and #currentStrokePoints >= 2 then
            for i = 1, #currentStrokePoints - 1 do
                local x1 = self.mapAPI:worldToUIX(currentStrokePoints[i].x, currentStrokePoints[i].y)
                local y1 = self.mapAPI:worldToUIY(currentStrokePoints[i].x, currentStrokePoints[i].y)
                local x2 = self.mapAPI:worldToUIX(currentStrokePoints[i + 1].x, currentStrokePoints[i + 1].y)
                local y2 = self.mapAPI:worldToUIY(currentStrokePoints[i + 1].x, currentStrokePoints[i + 1].y)
                self.javaObject:DrawLine(nil, x1, y1, x2, y2, DRAW_THICKNESS,
                    currentDrawColor.r, currentDrawColor.g, currentDrawColor.b, 0.9)
            end
        end

        -- ===== Periodically update Draw button state based on pen availability =====
        if self.drawModeBtn and not drawModeActive then
            local now = getTimestampMs()
            if (now - lastCanDrawTime) >= CAN_DRAW_CHECK_INTERVAL then
                lastCanDrawTime = now
                lastCanDrawCheck = canDraw()
                if lastCanDrawCheck then
                    self.drawModeBtn:setTitle(L("DrawModeOff"))
                    self.drawModeBtn.borderColor = { r = 0.5, g = 0.5, b = 0.5, a = 0.8 }
                else
                    self.drawModeBtn:setTitle(L("NoPen"))
                    self.drawModeBtn.borderColor = { r = 0.4, g = 0.4, b = 0.4, a = 0.5 }
                end
            end
        end

        -- ===== Periodically update Erase button state based on eraser availability =====
        if self.eraseModeBtn and not eraseModeActive then
            local now = getTimestampMs()
            if (now - lastCanEraseTime) >= CAN_DRAW_CHECK_INTERVAL then
                lastCanEraseTime = now
                lastCanEraseCheck = canErase()
                if lastCanEraseCheck then
                    self.eraseModeBtn:setTitle(L("Erase"))
                    self.eraseModeBtn.borderColor = { r = 0.5, g = 0.5, b = 0.5, a = 0.8 }
                else
                    self.eraseModeBtn:setTitle(L("NoEraser"))
                    self.eraseModeBtn.borderColor = { r = 0.4, g = 0.4, b = 0.4, a = 0.5 }
                end
            end
        end
    end
end

-- ============================================================
-- ISWorldMap Toggle Button Patch
-- ============================================================

local originalCreateChildren = nil

--- Patch ISWorldMap:createChildren to add our toggle button.
local function patchWorldMapCreateChildren()
    if not ISWorldMap or originalCreateChildren then return end

    originalCreateChildren = ISWorldMap.createChildren

    ISWorldMap.createChildren = function(self)
        originalCreateChildren(self)

        -- Store reference to the map instance for draw color and canWrite checks
        mapInstance = self

        -- Provide the symbolsAPI to the tracker for polling player symbols
        local mapAPI = self.javaObject and self.javaObject:getAPIv3()
        if mapAPI then
            PZShareMapNotes.setSymbolsAPI(mapAPI:getSymbolsAPIv2())
        end

        -- Position buttons on the left side, vertically centered
        local btnH = 25
        local btnX = 10
        local btnY = math.floor(self:getHeight() / 2) - 40

        -- ===== Drawings Toggle Button =====
        local notesBtnW = 120
        self.sharedNotesBtn = ISButton:new(btnX, btnY, notesBtnW, btnH, L("DrawingsDisplay"), self, function(_, btn)
            showShared = not showShared
            if showShared then
                btn:setTitle(L("DrawingsDisplay"))
            else
                btn:setTitle(L("DrawingsOff"))
            end
        end)
        self.sharedNotesBtn:initialise()
        self.sharedNotesBtn:instantiate()
        self.sharedNotesBtn.borderColor = { r = 0.2, g = 0.8, b = 0.9, a = 0.8 }
        self:addChild(self.sharedNotesBtn)

        -- ===== Draw Mode Toggle Button =====
        local drawBtnW = 100
        local drawBtnY = btnY + btnH + 5

        self.drawModeBtn = ISButton:new(btnX, drawBtnY, drawBtnW, btnH, L("DrawModeOff"), self, function(_, btn)
            if not drawModeActive then
                local color = getDrawColor()
                if not color then
                    -- No pen in inventory and pen is required
                    btn:setTitle(L("NoPenAlert"))
                    btn.borderColor = { r = 0.8, g = 0.2, b = 0.2, a = 0.9 }
                    return
                end
                drawModeActive = true
                currentDrawColor = color
                btn:setTitle(L("DrawModeOn"))
                btn.borderColor = { r = color.r, g = color.g, b = color.b, a = 0.9 }
                -- Disable erase mode if active
                if eraseModeActive then
                    eraseModeActive = false
                    self.eraseModeBtn:setTitle(L("Erase"))
                    self.eraseModeBtn.borderColor = { r = 0.5, g = 0.5, b = 0.5, a = 0.8 }
                end
            else
                drawModeActive = false
                btn:setTitle(L("DrawModeOff"))
                btn.borderColor = { r = 0.5, g = 0.5, b = 0.5, a = 0.8 }
                isDrawing = false
                currentStrokePoints = {}
                currentDrawColor = nil
            end
        end)
        self.drawModeBtn:initialise()
        self.drawModeBtn:instantiate()
        self.drawModeBtn.borderColor = { r = 0.5, g = 0.5, b = 0.5, a = 0.8 }
        self:addChild(self.drawModeBtn)

        -- ===== Undo Button =====
        local undoBtnW = 60
        local undoBtnY = drawBtnY + btnH + 5

        self.undoStrokeBtn = ISButton:new(btnX, undoBtnY, undoBtnW, btnH, L("Undo"), self, function(_, btn)
            undoLastStroke()
        end)
        self.undoStrokeBtn:initialise()
        self.undoStrokeBtn:instantiate()
        self.undoStrokeBtn.borderColor = { r = 0.5, g = 0.5, b = 0.5, a = 0.8 }
        self:addChild(self.undoStrokeBtn)

        -- ===== Erase Mode Toggle Button =====
        local eraseBtnW = 90
        local eraseBtnY = undoBtnY + btnH + 5

        self.eraseModeBtn = ISButton:new(btnX, eraseBtnY, eraseBtnW, btnH, L("Erase"), self, function(_, btn)
            if not eraseModeActive then
                if not canErase() then
                    btn:setTitle(L("NoEraserAlert"))
                    btn.borderColor = { r = 0.8, g = 0.2, b = 0.2, a = 0.9 }
                    return
                end
                eraseModeActive = true
                btn:setTitle(L("EraseOn"))
                btn.borderColor = { r = 0.8, g = 0.2, b = 0.2, a = 0.9 }
                -- Disable draw mode if active
                if drawModeActive then
                    drawModeActive = false
                    isDrawing = false
                    currentStrokePoints = {}
                    currentDrawColor = nil
                    self.drawModeBtn:setTitle(L("DrawModeOff"))
                    self.drawModeBtn.borderColor = { r = 0.5, g = 0.5, b = 0.5, a = 0.8 }
                end
            else
                eraseModeActive = false
                btn:setTitle(L("Erase"))
                btn.borderColor = { r = 0.5, g = 0.5, b = 0.5, a = 0.8 }
            end
        end)
        self.eraseModeBtn:initialise()
        self.eraseModeBtn:instantiate()
        self.eraseModeBtn.borderColor = { r = 0.5, g = 0.5, b = 0.5, a = 0.8 }
        self:addChild(self.eraseModeBtn)
    end
end

-- ============================================================
-- ISWorldMap Mouse Input Patch (Draw Mode)
-- ============================================================

local originalOnMouseDown = nil
local originalOnMouseMove = nil
local originalOnMouseUp = nil

local function patchWorldMapMouseInput()
    if not ISWorldMap or originalOnMouseDown then return end

    originalOnMouseDown = ISWorldMap.onMouseDown
    originalOnMouseMove = ISWorldMap.onMouseMove
    originalOnMouseUp = ISWorldMap.onMouseUp

    ISWorldMap.onMouseDown = function(self, x, y)
        if eraseModeActive then
            -- Re-check eraser availability before erasing
            if not canErase() then
                eraseModeActive = false
                if self.eraseModeBtn then
                    self.eraseModeBtn:setTitle(L("NoEraserAlert"))
                    self.eraseModeBtn.borderColor = { r = 0.8, g = 0.2, b = 0.2, a = 0.9 }
                end
                return originalOnMouseDown(self, x, y)
            end

            local strokeId = hitTestStrokes(self.mapAPI, x, y)
            if strokeId then
                local stroke = sharedStrokes[strokeId]
                local player = getPlayer()
                if player and stroke then
                    -- Only allow erasing own strokes (server enforces this too)
                    if stroke.author == player:getUsername()
                        or tostring(player:getAccessLevel()) == "admin" then
                        sendClientCommand(player, MOD_ID, PZShareMapNotes.CMD_REMOVE_STROKE, { id = strokeId })
                    end
                end
                return true
            end
            -- Click missed all strokes — pass through to default behavior
            return originalOnMouseDown(self, x, y)
        end

        if drawModeActive then
            -- Re-check pen availability before starting a stroke
            local color = getDrawColor()
            if not color then
                drawModeActive = false
                if self.drawModeBtn then
                    self.drawModeBtn:setTitle(L("NoPenAlert"))
                    self.drawModeBtn.borderColor = { r = 0.8, g = 0.2, b = 0.2, a = 0.9 }
                end
                return originalOnMouseDown(self, x, y)
            end
            currentDrawColor = color

            isDrawing = true
            currentStrokePoints = {}

            local worldX = self.mapAPI:uiToWorldX(x, y)
            local worldY = self.mapAPI:uiToWorldY(x, y)

            table.insert(currentStrokePoints, { x = worldX, y = worldY })
            lastPointX = worldX
            lastPointY = worldY

            return true
        end

        return originalOnMouseDown(self, x, y)
    end

    ISWorldMap.onMouseMove = function(self, dx, dy)
        if drawModeActive and isDrawing then
            local mouseX = self:getMouseX()
            local mouseY = self:getMouseY()
            local worldX = self.mapAPI:uiToWorldX(mouseX, mouseY)
            local worldY = self.mapAPI:uiToWorldY(mouseX, mouseY)

            if lastPointX and lastPointY then
                local ddx = worldX - lastPointX
                local ddy = worldY - lastPointY
                local distSq = ddx * ddx + ddy * ddy
                if distSq < PZShareMapNotes.MIN_POINT_DISTANCE_SQ then
                    return true
                end
            end

            if #currentStrokePoints >= PZShareMapNotes.MAX_POINTS_PER_STROKE then
                return true
            end

            table.insert(currentStrokePoints, { x = worldX, y = worldY })
            lastPointX = worldX
            lastPointY = worldY

            return true
        end

        return originalOnMouseMove(self, dx, dy)
    end

    ISWorldMap.onMouseUp = function(self, x, y)
        if drawModeActive and isDrawing then
            isDrawing = false

            if #currentStrokePoints >= 2 then
                local player = getPlayer()
                if player and currentDrawColor then
                    local strokeData = {
                        pointData = PZShareMapNotes.serializePoints(currentStrokePoints),
                        r = currentDrawColor.r,
                        g = currentDrawColor.g,
                        b = currentDrawColor.b,
                    }
                    sendClientCommand(player, MOD_ID, PZShareMapNotes.CMD_SHARE_STROKE, strokeData)
                    print("[PZShareMapNotes] Stroke sent with " .. #currentStrokePoints .. " points.")
                end
            end

            currentStrokePoints = {}
            lastPointX = nil
            lastPointY = nil

            return true
        end

        return originalOnMouseUp(self, x, y)
    end
end

-- ============================================================
-- ISWorldMapSymbolTool_RemoveAnnotation Patch
-- ============================================================
-- Vanilla's removeAnnotation deletes notes via the client-only
-- removeSymbolByIndex, even when the note has been shared to the server.
-- Result: the user "deletes" the note but the server still holds the
-- canonical copy and re-pushes it on reconnect (Workshop bug report,
-- mod ID 3676995511). Vanilla's symbol-icon path 10 lines below already
-- handles shared deletes correctly via sendRemoveSymbol; this patch
-- promotes the note path to match.
--
-- Matches the vanilla shared-symbol branch exactly: no mouseOverNote
-- clear, no MapRemoveMarking sound. Server confirmation drives the
-- visual change; hover state clears naturally on the next mouse event.
-- Mirroring vanilla avoids optimistic feedback if the server drops the
-- packet.

local removeAnnotationPatched = false

local function patchRemoveAnnotation()
    if removeAnnotationPatched then return end
    if not ISWorldMapSymbolTool_RemoveAnnotation then return end
    removeAnnotationPatched = true

    local origRemoveAnnotation = ISWorldMapSymbolTool_RemoveAnnotation.removeAnnotation
    ISWorldMapSymbolTool_RemoveAnnotation.removeAnnotation = function(self)
        if self.symbolsUI and self.symbolsUI.mouseOverNote
            and self.symbolsAPI and isClient() then
            local note = self.symbolsAPI:getSymbolByIndex(self.symbolsUI.mouseOverNote)
            -- pcall around isShared(): it's a Java call. If it throws (symbol
            -- freed mid-frame, unexpected layer state), fall through to vanilla
            -- rather than leaving the user with an unresponsive delete button.
            local ok, shared = pcall(function() return note and note:isShared() end)
            if ok and shared then
                self.symbolsAPI:sendRemoveSymbol(note)
                return true
            end
        end
        return origRemoveAnnotation(self)
    end

    print("[PZShareMapNotes] RemoveAnnotation patch applied.")
end

-- ============================================================
-- Initialization
-- ============================================================

--- Reset all client state so a fresh sync is requested on next connect.
--- Called when returning to main menu (i.e. disconnecting from a server).
local function resetClientState()
    strokeSyncRequested = false
    sharedStrokes = {}
    localStrokeIds = {}
    pendingStrokeBatches = {}
    expectedStrokeBatches = 0
    drawModeActive = false
    isDrawing = false
    currentStrokePoints = {}
    currentDrawColor = nil
    lastPointX = nil
    lastPointY = nil
    mapInstance = nil
    -- Re-register OnTick so stroke sync fires on next connect
    addStrokeSyncTick()
    print("[PZShareMapNotes] Client state reset.")
end

--- Apply patches when the main menu is shown (ISWorldMap class is available).
local function onMainMenuEnter()
    resetClientState()
    patchWorldMapRender()
    patchWorldMapCreateChildren()
    patchWorldMapMouseInput()
    patchRemoveAnnotation()
end

Events.OnServerCommand.Add(onServerCommand)
addStrokeSyncTick()
Events.OnMainMenuEnter.Add(onMainMenuEnter)

-- Also try patching immediately in case ISWorldMap is already loaded
if ISWorldMap then
    patchWorldMapRender()
    patchWorldMapCreateChildren()
    patchWorldMapMouseInput()
end
patchRemoveAnnotation()

print("[PZShareMapNotes] Client module loaded.")
