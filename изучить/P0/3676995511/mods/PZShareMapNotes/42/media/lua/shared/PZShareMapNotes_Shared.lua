PZShareMapNotes = PZShareMapNotes or {}

PZShareMapNotes.MOD_ID = "PZShareMapNotes"

-- Drawing network command names
PZShareMapNotes.CMD_SHARE_STROKE             = "ShareStroke"
PZShareMapNotes.CMD_REMOVE_STROKE            = "RemoveStroke"
PZShareMapNotes.CMD_BROADCAST_STROKE_ADD     = "BroadcastStrokeAdd"
PZShareMapNotes.CMD_BROADCAST_STROKE_REMOVE  = "BroadcastStrokeRemove"
PZShareMapNotes.CMD_REQUEST_STROKE_SYNC      = "RequestStrokeSync"
PZShareMapNotes.CMD_FULL_STROKE_SYNC         = "FullStrokeSync"
PZShareMapNotes.CMD_FULL_STROKE_SYNC_BATCH   = "FullStrokeSyncBatch"

-- Pen types and their drawing colors (matches PZ's native ISWorldMapSymbols)
PZShareMapNotes.PEN_COLORS = {
    { item = "Pen",      r = 0.129, g = 0.129, b = 0.129 },
    { item = "Pencil",   r = 0.2,   g = 0.2,   b = 0.2   },
    { item = "RedPen",   r = 0.65,  g = 0.054, b = 0.054 },
    { item = "BluePen",  r = 0.156, g = 0.188, b = 0.49  },
    { item = "GreenPen", r = 0.06,  g = 0.39,  b = 0.17  },
}

-- Default draw color (orange) used when RequirePenToDraw is disabled
PZShareMapNotes.DEFAULT_DRAW_COLOR = { r = 1.0, g = 0.5, b = 0.0 }

-- Drawing limits
PZShareMapNotes.MAX_STROKES              = 200
PZShareMapNotes.MAX_STROKES_PER_PLAYER   = 30
PZShareMapNotes.MAX_POINTS_PER_STROKE    = 500
PZShareMapNotes.STROKE_SYNC_BATCH_SIZE   = 10
PZShareMapNotes.MIN_POINT_DISTANCE_SQ    = 4.0
PZShareMapNotes.DRAW_LINE_THICKNESS      = 2.0

--- Upper bound on the serialized point string the server will accept from a
--- client. A point serializes to at most ~"12345.678,12345.678;" so 32 chars
--- per point is a generous ceiling. The server checks this *before* parsing so
--- a hostile client can't make it build an arbitrarily large table only for
--- validateStroke to reject it afterwards.
PZShareMapNotes.MAX_POINT_DATA_CHARS     = PZShareMapNotes.MAX_POINTS_PER_STROKE * 32

--- True only in singleplayer, where neither GameClient.client nor
--- GameServer.server is set.
---
--- This matters because the two directions of a round trip do NOT behave the
--- same solo. sendClientCommand still works: LuaManager routes it to
--- SinglePlayerClient, which loops the packet through SinglePlayerServer and
--- fires OnClientCommand. But BOTH sendServerCommand overloads are wrapped in
--- `if (GameServer.server)`, and nothing forwards them to
--- SinglePlayerServer.sendServerCommand (that entry point is reachable only
--- from Java), so every reply is silently dropped. See deliverToLocalClient in
--- PZShareMapNotes_Server.lua.
--- @return boolean
function PZShareMapNotes.isSinglePlayer()
    return not isClient() and not isServer()
end

--- Copy a command payload the way the network would.
--- The singleplayer path hands a table straight from the server half to the
--- client half inside one Lua VM, with no serialization in between. Copying
--- keeps that boundary honest: client handlers mutate what they receive (they
--- swap pointData for a points array), and must not reach back into the
--- server's own stroke store to do it.
--- @param value any
--- @return any
function PZShareMapNotes.copyPayload(value)
    if type(value) ~= "table" then return value end
    local out = {}
    for k, v in pairs(value) do
        out[k] = PZShareMapNotes.copyPayload(v)
    end
    return out
end

--- Generate a unique stroke ID.
--- @param username string
--- @return string
function PZShareMapNotes.generateStrokeId(username)
    local ts = getTimestampMs()
    local rand = ZombRand(100000)
    return "stroke_" .. username .. "_" .. tostring(ts) .. "_" .. tostring(rand)
end

--- Serialize a points array into a flat string: "x1,y1;x2,y2;..."
--- @param points table Array of {x=number, y=number}
--- @return string
function PZShareMapNotes.serializePoints(points)
    local parts = {}
    for _, pt in ipairs(points) do
        table.insert(parts, pt.x .. "," .. pt.y)
    end
    return table.concat(parts, ";")
end

--- Deserialize a pointData string back into a points array: {{x=n, y=n}, ...}
--- Parsing stops one point past MAX_POINTS_PER_STROKE: that keeps the work
--- bounded for a malformed or hostile payload while still letting the server's
--- validateStroke see an over-length stroke and reject it (rather than
--- silently truncating it to the limit and accepting it).
--- @param pointData string
--- @return table
function PZShareMapNotes.deserializePoints(pointData)
    local points = {}
    local parseCap = PZShareMapNotes.MAX_POINTS_PER_STROKE + 1
    for pair in string.gmatch(pointData, "[^;]+") do
        if #points >= parseCap then break end
        local x, y = string.match(pair, "([^,]+),([^,]+)")
        if x and y then
            table.insert(points, { x = tonumber(x), y = tonumber(y) })
        end
    end
    return points
end
