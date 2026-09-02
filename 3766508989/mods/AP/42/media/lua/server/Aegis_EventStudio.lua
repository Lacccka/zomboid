-- Event studio: admins compose named event chains (announce delay plus
-- ordered steps) that run against the spot the admin stood on when the
-- chain was triggered. The registry lives under Aegis/Events/, execution
-- goes through the AegisWorldOps table in Aegis_Server.lua.
if isClient() then return end

require "Aegis_Store"
require "Aegis_Roles"
require "Aegis_Log"
require "Aegis_Moderation"

local EVENTS_FILE = AegisStore.ROOT .. "/Events/events.txt"
local MAX_EVENTS = 40
local MAX_STEPS = 20
local MAX_NAME = 48
local MAX_TEXT = 120
local MAX_ANNOUNCE = 300

-- step type -> param -> { min, max, default }; announce carries free text
local STEP_TYPES = {
    horde = {
        count = { 1, 200, 30 }, dist = { 0, 120, 0 }, radius = { 1, 20, 8 },
        sprint = { 0, 1, 0 }, crawl = { 0, 1, 0 }, lure = { 0, 15, 5 },
    },
    storm = { hours = { 1, 96, 8 } },
    tropical = { hours = { 1, 96, 8 } },
    blizzard = { hours = { 1, 96, 8 } },
    rain = { on = { 0, 1, 1 }, intensity = { 10, 100, 60 } },
    thunder = {},
    gunshot = {},
    firework = {},
    noise = { radius = { 10, 500, 100 } },
    announce = { text = true },
    wait = { seconds = { 5, 600, 30 } },
}

local events = nil
local eventsIncomplete = false
local runs = {}
local runCount = 0
local tickAt = 0

local function splitPipe(line)
    local parts = {}
    for field in string.gmatch(line .. "|", "([^|]*)|") do
        table.insert(parts, field)
    end
    return parts
end

local function idOk(id)
    return type(id) == "string" and id ~= "" and #id <= 24 and id:match("^[a-z0-9]+$") ~= nil
end

local function cleanName(s)
    if type(s) ~= "string" then return "" end
    return s:gsub("[%c|]", " "):match("^%s*(.-)%s*$"):sub(1, MAX_NAME)
end

-- announce text shares the line format and the k=v field, so the
-- separators have to go entirely
local function cleanText(s)
    if type(s) ~= "string" then return "" end
    return s:gsub("[%c|;]", " "):sub(1, MAX_TEXT)
end

local function clampAnnounce(v)
    v = math.floor(tonumber(v) or 0)
    if v < 0 then v = 0 end
    if v > MAX_ANNOUNCE then v = MAX_ANNOUNCE end
    return v
end

local function parseParams(field)
    local params = {}
    if type(field) ~= "string" then return params end
    for pair in string.gmatch(field .. ";", "([^;]*);") do
        local k, v = pair:match("^([^=]+)=(.*)$")
        if k then params[k] = v end
    end
    return params
end

local function sanitizeStep(stype, params)
    local spec = STEP_TYPES[stype]
    if not spec or type(params) ~= "table" then return nil end
    local step = { type = stype }
    for key, range in pairs(spec) do
        if key == "text" then
            step.text = cleanText(params.text)
        else
            local v = math.floor(tonumber(params[key]) or range[3])
            if v < range[1] then v = range[1] end
            if v > range[2] then v = range[2] end
            step[key] = v
        end
    end
    return step
end

local function stepField(step)
    local keys = {}
    for key in pairs(STEP_TYPES[step.type]) do
        table.insert(keys, key)
    end
    table.sort(keys)
    local parts = {}
    for _, key in ipairs(keys) do
        table.insert(parts, key .. "=" .. tostring(step[key]))
    end
    return table.concat(parts, ";")
end

local function stepCopy(step)
    local copy = {}
    for k, v in pairs(step) do
        copy[k] = v
    end
    return copy
end

local function eventCount()
    local n = 0
    for _ in pairs(events) do
        n = n + 1
    end
    return n
end

local function deriveId(name)
    local base = name:lower():gsub("[^a-z0-9]", ""):sub(1, 20)
    if base == "" then base = "event" end
    if not events[base] then return base end
    local n = 2
    while events[base .. tostring(n)] do
        n = n + 1
    end
    return base .. tostring(n)
end

-- full rewrite, refused after an incomplete read: rewriting on top of a
-- partial load would silently drop unread events
local function saveEvents()
    if eventsIncomplete then
        print("[Aegis] Event registry write-protected, change not saved")
        return false
    end
    local ids = {}
    for id in pairs(events) do
        table.insert(ids, id)
    end
    table.sort(ids)
    -- the header line marks the registry as initialised: a file an admin
    -- emptied on purpose must not grow the starter templates back
    local lines = { "A|1" }
    for _, id in ipairs(ids) do
        local ev = events[id]
        table.insert(lines, "E|" .. ev.id .. "|" .. ev.name .. "|" .. tostring(ev.announceSec))
        for i = 1, #ev.steps do
            table.insert(lines, "S|" .. ev.id .. "|" .. tostring(i) .. "|" .. ev.steps[i].type
                .. "|" .. stepField(ev.steps[i]))
        end
    end
    local content = table.concat(lines, "\n")
    if #lines > 0 then content = content .. "\n" end
    return AegisStore.write(EVENTS_FILE, content)
end

-- the former director combos as editable presets. The chopper flyover of
-- heli and airdrop was client bound (/chopper has no server trigger) and
-- is approximated by a wide noise pull; firestorm loses the onFire flag
-- the step format does not carry
local function seedDefaults()
    local defs = {
        { id = "stormshow", name = "Storm Show", steps = {
            { type = "storm", hours = 8 },
            { type = "thunder" }, { type = "thunder" }, { type = "thunder" },
            { type = "thunder" }, { type = "thunder" }, { type = "thunder" },
        } },
        { id = "siege", name = "Siege", steps = {
            { type = "horde", count = 40, dist = 60, radius = 10, lure = 5 },
            { type = "horde", count = 40, dist = 72, radius = 10, lure = 5 },
            { type = "horde", count = 40, dist = 84, radius = 10, lure = 5 },
            { type = "gunshot" },
        } },
        { id = "helialert", name = "Heli Alert", steps = {
            { type = "noise", radius = 400 },
            { type = "horde", count = 15, dist = 80, radius = 8 },
        } },
        { id = "airdrop", name = "Airdrop", steps = {
            { type = "noise", radius = 400 },
            { type = "horde", count = 20, dist = 0, radius = 10 },
        } },
        { id = "firestorm", name = "Firestorm", steps = {
            { type = "storm", hours = 6 },
            { type = "thunder" }, { type = "thunder" },
            { type = "thunder" }, { type = "thunder" },
            { type = "horde", count = 15, dist = 50, radius = 8, lure = 5 },
            { type = "horde", count = 15, dist = 50, radius = 8, lure = 5 },
            { type = "horde", count = 15, dist = 50, radius = 8, lure = 5 },
        } },
        { id = "ambush", name = "Crawler Ambush", steps = {
            { type = "rain", on = 1, intensity = 60 },
            { type = "horde", count = 25, dist = 0, radius = 6, crawl = 1 },
        } },
    }
    for _, def in ipairs(defs) do
        local ev = { id = def.id, name = def.name, announceSec = 0, steps = {} }
        for i = 1, #def.steps do
            local step = sanitizeStep(def.steps[i].type, def.steps[i])
            if step then table.insert(ev.steps, step) end
        end
        events[def.id] = ev
    end
end

local function loadEvents()
    if events then return end
    events = {}
    eventsIncomplete = false
    local lines, truncated = AegisStore.readLines(EVENTS_FILE, 2000)
    if lines == nil or truncated then eventsIncomplete = true end
    local count = 0
    local pending = {}
    for _, line in ipairs(lines or {}) do
        local parts = splitPipe(line)
        if parts[1] == "E" then
            local id = parts[2]
            if idOk(id) and not events[id] and count < MAX_EVENTS then
                events[id] = {
                    id = id,
                    name = cleanName(parts[3]),
                    announceSec = clampAnnounce(parts[4]),
                    steps = {},
                }
                count = count + 1
            end
        elseif parts[1] == "S" then
            local id, idx = parts[2], tonumber(parts[3])
            if idx and idOk(id) then
                local step = sanitizeStep(parts[4], parseParams(parts[5]))
                if step then
                    if not pending[id] then pending[id] = {} end
                    table.insert(pending[id], { idx = idx, step = step })
                end
            end
        end
    end
    -- step lines are re-ordered by index, the sorted file interleaves
    -- 1, 10, 11 ... 2 lexicographically
    for id, list in pairs(pending) do
        local ev = events[id]
        if ev then
            table.sort(list, function(a, b) return a.idx < b.idx end)
            for i = 1, #list do
                if #ev.steps < MAX_STEPS then
                    table.insert(ev.steps, list[i].step)
                end
            end
        end
    end
    -- only a file with no lines at all is a first run; the header line
    -- keeps an emptied registry empty
    if not eventsIncomplete and #(lines or {}) == 0 then
        seedDefaults()
        saveEvents()
    end
end

local function toClient(player, command, args)
    if isServer() then
        sendServerCommand(player, AegisShared.MODULE, command, args)
    else
        triggerEvent("OnServerCommand", AegisShared.MODULE, command, args)
    end
end

local function deny(player)
    toClient(player, "denied", { area = "events" })
end

local function listPayload()
    local ids = {}
    for id in pairs(events) do
        table.insert(ids, id)
    end
    table.sort(ids)
    local list = {}
    for _, id in ipairs(ids) do
        local ev = events[id]
        local steps = {}
        for i = 1, #ev.steps do
            table.insert(steps, stepCopy(ev.steps[i]))
        end
        table.insert(list, { id = ev.id, name = ev.name, announceSec = ev.announceSec, steps = steps })
    end
    return list
end

local function pushList(player)
    toClient(player, "studioList", { events = listPayload() })
end

local function execStep(step, anchor)
    local t = step.type
    if t == "horde" then
        AegisWorldOps.horde({
            x = anchor.x, y = anchor.y, z = anchor.z,
            count = step.count, radius = step.radius, dist = step.dist,
            sprinter = step.sprint == 1, crawler = step.crawl == 1,
            lureMinutes = step.lure,
        })
    elseif t == "storm" then
        AegisWorldOps.storm({ hours = step.hours })
    elseif t == "tropical" then
        AegisWorldOps.tropical({ hours = step.hours })
    elseif t == "blizzard" then
        AegisWorldOps.blizzard({ hours = step.hours })
    elseif t == "rain" then
        if step.on == 1 then
            AegisWorldOps.rainOn({ intensity = step.intensity })
        else
            AegisWorldOps.rainOff()
        end
    elseif t == "thunder" then
        AegisWorldOps.thunder({ x = anchor.x, y = anchor.y })
    elseif t == "gunshot" then
        AegisWorldOps.gunshot()
    elseif t == "firework" then
        AegisWorldOps.firework({ x = anchor.x, y = anchor.y })
    elseif t == "noise" then
        AegisWorldOps.noise({ x = anchor.x, y = anchor.y, z = anchor.z, radius = step.radius })
    elseif t == "announce" then
        AegisWorldOps.announce({ text = step.text })
    end
end

local function startRun(player, ev)
    if #ev.steps == 0 then return false end
    local steps = {}
    for i = 1, #ev.steps do
        table.insert(steps, stepCopy(ev.steps[i]))
    end
    local now = AegisShared.realTime()
    local run = {
        steps = steps,
        idx = 1,
        nextAt = now,
        anchor = {
            x = math.floor(player:getX()),
            y = math.floor(player:getY()),
            z = math.floor(player:getZ()),
        },
        admin = player:getUsername(),
        name = ev.name,
    }
    if ev.announceSec > 0 then
        AegisWorldOps.announce({ text = ev.name .. " in " .. ev.announceSec .. "s" })
        run.nextAt = now + ev.announceSec
    end
    table.insert(runs, run)
    runCount = runCount + 1
    AegisLog.write("Actions", run.admin, "events",
        "Event started: " .. ev.name .. " (" .. #steps .. " steps)")
    return true
end

local Commands = {}

Commands.studioList = function(player, args)
    if not AegisRoles.canArea(player, "events") then deny(player) return end
    loadEvents()
    pushList(player)
end

Commands.studioSave = function(player, args)
    if not AegisRoles.canArea(player, "events") then deny(player) return end
    if not args or type(args.event) ~= "table" then return end
    loadEvents()
    local incoming = args.event
    local name = cleanName(incoming.name)
    if name == "" then return end
    local steps = {}
    if type(incoming.steps) == "table" then
        for i = 1, #incoming.steps do
            if #steps >= MAX_STEPS then break end
            local raw = incoming.steps[i]
            if type(raw) == "table" then
                local step = sanitizeStep(raw.type, raw)
                if step then table.insert(steps, step) end
            end
        end
    end
    if #steps == 0 then return end
    local id = incoming.id
    if not idOk(id) then id = nil end
    if not events[id or ""] and eventCount() >= MAX_EVENTS then
        toClient(player, "studioSave", { ok = false, reason = "full" })
        return
    end
    if not id then id = deriveId(name) end
    events[id] = { id = id, name = name, announceSec = clampAnnounce(incoming.announceSec), steps = steps }
    local ok = saveEvents()
    toClient(player, "studioSave", { ok = ok, id = id })
    if ok then
        AegisLog.write("Actions", player:getUsername(), "events", "Event saved: " .. name)
        pushList(player)
    end
end

Commands.studioDelete = function(player, args)
    if not AegisRoles.canArea(player, "events") then deny(player) return end
    if not args or not idOk(args.id) then return end
    loadEvents()
    local ev = events[args.id]
    if not ev then return end
    events[args.id] = nil
    local ok = saveEvents()
    toClient(player, "studioDelete", { ok = ok, id = args.id })
    if ok then
        AegisLog.write("Actions", player:getUsername(), "events", "Event deleted: " .. ev.name)
        pushList(player)
    end
end

Commands.studioRun = function(player, args)
    if not AegisRoles.canArea(player, "events") then deny(player) return end
    if not args or not idOk(args.id) then return end
    loadEvents()
    local ev = events[args.id]
    if not ev then return end
    if startRun(player, ev) then
        toClient(player, "studioRun", { ok = true, id = ev.id, name = ev.name })
    end
end

Commands.studioSurprise = function(player, args)
    if not AegisRoles.canArea(player, "events") then deny(player) return end
    loadEvents()
    local ids = {}
    for id, ev in pairs(events) do
        if #ev.steps > 0 then table.insert(ids, id) end
    end
    if #ids == 0 then return end
    local ev = events[ids[ZombRand(#ids) + 1]]
    if startRun(player, ev) then
        toClient(player, "studioSurprise", { ok = true, id = ev.id, name = ev.name })
    end
end

-- runs advance on real seconds, one step per second, only wait stretches
-- the gap. EveryOneMinute counts game time and races on short days
local function onTick()
    if runCount == 0 then return end
    local now = AegisShared.realTime()
    if now < tickAt then return end
    tickAt = now + 1
    for r = #runs, 1, -1 do
        local run = runs[r]
        if now >= run.nextAt then
            local step = run.steps[run.idx]
            run.idx = run.idx + 1
            if step.type == "wait" then
                run.nextAt = now + step.seconds
            else
                execStep(step, run.anchor)
                run.nextAt = now + 1
            end
            if run.idx > #run.steps then
                table.remove(runs, r)
                runCount = runCount - 1
            end
        end
    end
end

local function onClientCommand(module, command, player, args)
    if module ~= AegisShared.MODULE then return end
    if not Commands[command] then return end
    if AegisModeration.isSuspended(player) then return end
    Commands[command](player, args)
end

Events.OnClientCommand.Add(onClientCommand)
Events.OnTick.Add(onTick)
