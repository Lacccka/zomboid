--
-- True Companions - the trace (v0.75.64)
--
-- ===========================================================================
-- WHY THIS FILE EXISTS
-- ===========================================================================
--
-- Five builds in a row have tried to fix the same three symptoms -- she freezes and
-- teleports, she wrecks her own plots, she never waters -- and five times the fix came
-- out of READING the code and inferring what must have happened. Each miss cost the
-- author a real test session.
--
-- The method was the problem, not the effort. Every symptom is TEMPORAL: "she plowed
-- three tiles and then for hours did nothing", "as soon as she stood at the container
-- she started freezing". Nothing about a static read can say what she was doing during
-- those hours. And the mod produces no evidence to read instead: the 7 Aug session log
-- carries NINE lines from this mod in 4,233, every one of them a one-shot notice. The
-- Guardian flushes her whole task queue at nine seconds and teleports her at twenty
-- with no log line at all -- so every teleport the author has ever reported has left
-- exactly zero trace.
--
-- The v0.75.63 water diagnostics proved it. They printed NOTHING. Both live inside the
-- `kind == "water"` branch after arrival, so silence means she never reached the pour,
-- and the real failure is somewhere upstream that had no instrumentation whatsoever.
-- A diagnostic that only fires at the end of the path it is diagnosing cannot tell you
-- the path was never taken.
--
-- So: trace first, read the trace, then fix ONCE.
--
-- ===========================================================================
-- WHAT MAKES THIS DIFFERENT FROM `print`
-- ===========================================================================
--
-- Everything a companion does hangs off Events.OnZombieUpdate FOR HER, which is roughly
-- sixty firings a second. A naive per-tick print is not a log, it is a denial of service
-- against the person who has to read it -- and it is exactly why nobody instruments this
-- path. Three things make it usable:
--
--   * IT GOES TO ITS OWN FILE. `D:\Zomboid\TrueCompanions_trace.txt`, not the DebugLog,
--     which is already 4,000 lines of vanilla weather and tile noise. One file to send.
--
--   * IT IS BUFFERED. Lines land in a Lua table and are written every two seconds on
--     the tick clock. getFileWriter opens, writes and closes -- doing that per line at
--     60Hz would cost more than the bug.
--
--   * IT COLLAPSES REPEATS. Tr.Change writes only when the text actually changes and
--     tags the swallowed run with `xN`. A thousand identical dispatches become one line
--     saying x1000 -- AND THAT NUMBER IS THE DIAGNOSIS. A loop is invisible in a stream
--     of identical lines and unmissable as a repeat count.
--
-- Every line carries BOTH clocks: real seconds since the trace opened, and the world-age
-- hour. "Hours of nothing" is a claim about game time; "she froze for two minutes" is a
-- claim about real time. The two coming apart is itself one of the open hypotheses
-- (tick starvation), so neither clock alone can test it.
--
-- OFF BY DEFAULT. A shipped build pays one boolean read per call site and nothing else:
-- every probe is written as `if Tr.On() then ... end` so the string concatenation
-- inside never runs when the option is off.
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.Trace = BanditsNPC.Trace or {}
local Tr = BanditsNPC.Trace

local FILE = "TrueCompanions_trace.txt"

-- A session that goes wrong at 3am unattended must not fill the disk. At the volumes
-- this is designed for (a heartbeat a second plus change-only records) an hour of play
-- is a few thousand lines, so a quarter of a million is "something is broken", not
-- "a long session".
local LINE_CAP = 250000

local FLUSH_MS = 2000

local buf, bufN = {}, 0
local written = 0
local capped = false
local startedAt = nil
local opened = false

-- ---------------------------------------------------------------------------
-- the gate
-- ---------------------------------------------------------------------------

-- Cached per tick rather than per call: a single dispatch asks this a dozen times and
-- BanditsNPC.Opt goes through a pcall and a ModData lookup.
local gateVal, gateAt = false, -1

function Tr.On()
    local now = (getTimestampMs and getTimestampMs()) or 0
    if now - gateAt < 500 then return gateVal end
    gateAt = now
    gateVal = false
    pcall(function()
        gateVal = BanditsNPC.Opt("DbgTrace", false) and true or false
    end)
    return gateVal
end

-- ---------------------------------------------------------------------------
-- the writer
-- ---------------------------------------------------------------------------

local function nowMs()
    return (getTimestampMs and getTimestampMs()) or 0
end

-- Real seconds since the first traced line, to one decimal. Deliberately NOT the wall
-- clock: a relative number is what you subtract to answer "how long was she frozen".
local function stamp()
    local now = nowMs()
    if not startedAt then startedAt = now end
    return string.format("%08.1f", (now - startedAt) / 1000)
end

local function gameHour()
    local h
    pcall(function()
        if getGameTime then h = getGameTime():getWorldAgeHours() end
    end)
    if type(h) ~= "number" then return "?" end
    return string.format("%.4f", h)
end

-- Who the line is about. Companions are hijacked zombies with no useful tostring, and a
-- raw id is unreadable in a log you are scanning by eye -- so it is the name where there
-- is one, and the short id otherwise.
local function who(bandit)
    if bandit == nil then return "-" end
    if type(bandit) == "string" then return bandit end
    local n
    pcall(function()
        local brain = BanditBrain.Get(bandit)
        if brain then n = brain.fullname or brain.name end
        if not n and bandit.getModData then
            local md = bandit:getModData()
            n = md and md.tcName
        end
    end)
    if type(n) ~= "string" or n == "" then return "npc" end
    -- one word is enough to tell two companions apart and keeps the columns aligned
    local first = string.match(n, "^(%S+)")
    return first or n
end

-- Stable per-companion key for the change/heartbeat tables. The brain uid is the only
-- identity that survives a save/load; the table address does not.
local function keyOf(bandit)
    if bandit == nil then return "-" end
    if type(bandit) == "string" then return bandit end
    local uid
    pcall(function()
        local brain = BanditBrain.Get(bandit)
        uid = brain and brain.npcUid
    end)
    if uid then return tostring(uid) end
    local id
    pcall(function() id = bandit:getOnlineID() end)
    return tostring(id or bandit)
end

local function push(line)
    if capped then return end
    if written >= LINE_CAP then
        capped = true
        bufN = bufN + 1
        buf[bufN] = "[TC] ---- LINE CAP REACHED (" .. LINE_CAP .. "), tracing stopped ----"
        return
    end
    written = written + 1
    bufN = bufN + 1
    buf[bufN] = line
end

-- Write the buffer out and empty it. Append mode, so a crash keeps everything written
-- so far -- which is the case that matters, because the interesting sessions are the
-- ones that end badly.
function Tr.Flush()
    if bufN == 0 then return end
    local lines = buf
    buf, bufN = {}, 0
    pcall(function()
        -- getFileWriter(path, createIfMissing, append) -- ISLayoutManager.lua:172 and
        -- ISEntityUI.lua:683 are the vanilla precedents for the three-argument form.
        -- The FIRST open of a session truncates so a trace is one session, not a pile
        -- of them; every later open appends.
        local w = getFileWriter(FILE, true, opened)
        if not w then return end
        opened = true
        for i = 1, #lines do
            w:write(lines[i] .. "\r\n")
        end
        w:close()
    end)
end

-- ---------------------------------------------------------------------------
-- the four ways to write a line
-- ---------------------------------------------------------------------------

-- Unconditional. For events that genuinely happen once: a teleport, a plot destroyed,
-- a phase change. Never put this on anything that can run per tick.
function Tr.Line(bandit, chan, text)
    if not Tr.On() then return end
    push("[TC] " .. stamp() .. "s h=" .. gameHour() .. " "
        .. string.format("%-10s", who(bandit)) .. " "
        .. string.format("%-6s", tostring(chan)) .. " " .. tostring(text))
end

-- CHANGE-ONLY, WITH A REPEAT COUNT. The workhorse.
--
-- Writes only when `text` differs from the last text written for this (companion, key).
-- When it finally does differ, the line that was being repeated is closed out with the
-- number of times it was swallowed. A dispatch loop -- the single most common failure
-- shape in this mod -- shows up as one line ending `x8412`, which is unmissable, where
-- 8,412 identical lines are unreadable.
local lastText, lastCount = {}, {}

function Tr.Change(bandit, chan, key, text)
    if not Tr.On() then return end
    local k = keyOf(bandit) .. "|" .. tostring(key)
    text = tostring(text)
    if lastText[k] == text then
        lastCount[k] = (lastCount[k] or 1) + 1
        return
    end
    -- close out the run that just ended
    if lastText[k] ~= nil and (lastCount[k] or 1) > 1 then
        push("[TC] " .. stamp() .. "s h=" .. gameHour() .. " "
            .. string.format("%-10s", who(bandit)) .. " "
            .. string.format("%-6s", tostring(chan)) .. " ^ repeated x" .. lastCount[k])
    end
    lastText[k] = text
    lastCount[k] = 1
    Tr.Line(bandit, chan, text)
end

-- Throttled. For a heartbeat that should appear at a steady rate whether or not it
-- changed, because its VALUE over time is the measurement (tick rate, position).
local lastEvery = {}

function Tr.Every(bandit, chan, key, ms, text)
    if not Tr.On() then return end
    local k = keyOf(bandit) .. "|" .. tostring(key)
    local now = nowMs()
    if now - (lastEvery[k] or -1e9) < (ms or 1000) then return end
    lastEvery[k] = now
    Tr.Line(bandit, chan, text)
end

-- ---------------------------------------------------------------------------
-- breadcrumbs
-- ---------------------------------------------------------------------------
--
-- The route she ACTUALLY walked, which is the only way to settle two open questions:
-- whether the window router is being used and works, and whether she is stepping on her
-- own crops. Kept as a small ring per companion and only written out when something
-- goes wrong, so the normal case costs a table store per square change.

local CRUMBS = 40
local crumb = {}

function Tr.Crumb(bandit, sq)
    if not Tr.On() or not sq then return end
    local k = keyOf(bandit)
    local c = crumb[k]
    if not c then c = { n = 0 }; crumb[k] = c end
    local s
    pcall(function() s = sq:getX() .. "," .. sq:getY() .. "," .. sq:getZ() end)
    if not s then return end
    if c[((c.n - 1) % CRUMBS) + 1] == s then return end   -- same square, not a step
    c.n = c.n + 1
    c[((c.n - 1) % CRUMBS) + 1] = s
end

function Tr.Dump(bandit, why)
    if not Tr.On() then return end
    local k = keyOf(bandit)
    local c = crumb[k]
    if not c or c.n == 0 then
        Tr.Line(bandit, "PATH", "crumbs(" .. tostring(why) .. "): none")
        return
    end
    local out, first = {}, math.max(1, c.n - CRUMBS + 1)
    for i = first, c.n do
        out[#out + 1] = c[((i - 1) % CRUMBS) + 1]
    end
    Tr.Line(bandit, "PATH", "crumbs(" .. tostring(why) .. ", " .. #out .. "): "
        .. table.concat(out, " > "))
end

-- ---------------------------------------------------------------------------
-- small formatters, so every probe reads the same way
-- ---------------------------------------------------------------------------

function Tr.B(v)
    if v == nil then return "?" end
    return v and "1" or "0"
end

function Tr.N(v, dp)
    if type(v) ~= "number" then return "?" end
    return string.format("%." .. tostring(dp or 2) .. "f", v)
end

function Tr.Sq(sq)
    if not sq then return "nil" end
    local s
    pcall(function() s = sq:getX() .. "," .. sq:getY() .. "," .. sq:getZ() end)
    return s or "?"
end

function Tr.Pos(p)
    if not p then return "nil" end
    return tostring(p.x) .. "," .. tostring(p.y) .. "," .. tostring(p.z or 0)
end

-- ---------------------------------------------------------------------------
-- lifecycle
-- ---------------------------------------------------------------------------

local nextFlush = 0

-- Announced ONCE, in the ORDINARY log -- so a player who turned the option on and then
-- forgot has a breadcrumb telling them where the file went, and so a session that was
-- meant to be traced but was not is obvious from the DebugLog alone.
local announced = false

local function onTick()
    if not Tr.On() then return end

    if not announced then
        announced = true
        -- The path is <Zomboid>\Lua\, not <Zomboid>\ -- getFileWriter resolves relative to
        -- the Lua subfolder, which is where the 7 Aug trace actually landed. Named exactly
        -- so nobody has to go looking for it again.
        print("[BanditsNPC] DEBUG TRACE IS ON. Writing to <Zomboid folder>\\Lua\\" .. FILE
            .. " -- it costs performance and grows a file, so turn 'Debug trace' off in"
            .. " the sandbox options when you are done.")
        Tr.Line(nil, "OPEN", "True Companions trace opened -- mod version "
            .. tostring(BanditsNPC.version or "?"))
    end

    local now = nowMs()
    if now < nextFlush then return end
    nextFlush = now + FLUSH_MS
    Tr.Flush()
end

Events.OnTick.Remove(onTick)
Events.OnTick.Add(onTick)

-- Best effort on the way out. OnPlayerDeath is not the only way a session ends, but a
-- two-second buffer means at worst two seconds of trace is lost on a hard exit, and the
-- flush above has already written everything before that.
local function onEnd()
    pcall(function() Tr.Flush() end)
end

if Events.OnPlayerDeath then
    Events.OnPlayerDeath.Remove(onEnd)
    Events.OnPlayerDeath.Add(onEnd)
end
if Events.OnPostSave then
    Events.OnPostSave.Remove(onEnd)
    Events.OnPostSave.Add(onEnd)
end
