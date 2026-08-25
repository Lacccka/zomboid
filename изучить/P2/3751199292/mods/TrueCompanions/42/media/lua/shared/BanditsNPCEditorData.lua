--
-- Bandits NPC - Companions Overhaul - Editor data (persistence)
--
-- The NPC Core Editor authors content (recipes first; dialogue/quests later)
-- and saves it to files in the Zomboid folder, so it applies across all saves
-- (like Bandits Creator's bandits.txt/clans.txt). At boot, if a file exists it
-- REPLACES the hardcoded defaults, so the editor is the source of truth once used.
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.Editor = BanditsNPC.Editor or {}
BanditsNPC.Editor.RECIPE_FILE = "BanditsNPC_recipes.txt"

-- ===== recipes =====

function BanditsNPC.Editor.SerializeRecipes()
    local out = {}
    for station, list in pairs(BanditsNPC.Production.Recipes or {}) do
        for _, r in ipairs(list) do
            table.insert(out, "[recipe]")
            table.insert(out, "station=" .. station)
            table.insert(out, "name=" .. (r.name or ""))
            table.insert(out, "output=" .. (r.output or ""))
            table.insert(out, "outCount=" .. tostring(r.outCount or 1))
            table.insert(out, "time=" .. tostring(r.time or 1))
            -- minTier MUST round-trip: LoadRecipes REPLACES the whole catalogue, so a
            -- field missing here is destroyed on the next boot. Omitting it reset every
            -- recipe to tier 1 the first time an author pressed Save -- silently deleting
            -- the entire station-upgrade progression (29 Jul audit, B3).
            table.insert(out, "minTier=" .. tostring(r.minTier or 1))
            if r.inputs then
                for t, c in pairs(r.inputs) do table.insert(out, "input=" .. t .. ":" .. tostring(c)) end
            end
            table.insert(out, "")
        end
    end
    return table.concat(out, "\n")
end

function BanditsNPC.Editor.SaveRecipes()
    local w = getFileWriter(BanditsNPC.Editor.RECIPE_FILE, true, false)
    if not w then return false end
    w:write(BanditsNPC.Editor.SerializeRecipes())
    w:close()
    return true
end

function BanditsNPC.Editor.LoadRecipes()
    local r = getFileReader(BanditsNPC.Editor.RECIPE_FILE, false)
    if not r then return false end

    local grouped = {}
    local cur = nil
    local line = r:readLine()
    while line ~= nil do
        line = line:gsub("[\r\n]", "")
        if line == "[recipe]" then
            cur = { inputs = {} }
        elseif cur and line ~= "" then
            local k, v = line:match("^(%w+)=(.*)$")
            if k == "station" then
                cur.station = v
                grouped[v] = grouped[v] or {}
                table.insert(grouped[v], cur)
            elseif k == "name" then cur.name = v
            elseif k == "output" then cur.output = v
            elseif k == "outCount" then cur.outCount = tonumber(v) or 1
            elseif k == "time" then cur.time = tonumber(v) or 1
            elseif k == "minTier" then cur.minTier = tonumber(v) or 1
            elseif k == "input" then
                local it, cnt = v:match("^(.+):(%d+)$")
                if it then cur.inputs[it] = tonumber(cnt) end
            end
        end
        line = r:readLine()
    end
    r:close()

    local hasGrouped = false   -- PZ's Lua has no global `next`; test with pairs
    for _ in pairs(grouped) do hasGrouped = true; break end
    if hasGrouped and BanditsNPC.Production then
        BanditsNPC.Production.Recipes = grouped
    end
    return true
end

-- delete the saved file (revert to hardcoded defaults on next boot)
function BanditsNPC.Editor.DeleteRecipesFile()
    -- overwrite with empty so LoadRecipes finds nothing usable; user can also
    -- delete the file manually. (PZ has no direct file-delete from Lua.)
    local w = getFileWriter(BanditsNPC.Editor.RECIPE_FILE, true, false)
    if w then w:write(""); w:close() end
end

-- ===== dialogue topics =====

BanditsNPC.Editor.DIALOGUE_FILE = "BanditsNPC_dialogue.txt"

-- teach encode/decode: {kind="xp",perk,amount} <-> "xp:Doctor:80", trait/recipe too
function BanditsNPC.Editor.EncTeach(te)
    if not (te and te.kind) then return "" end
    if te.kind == "xp" then return "xp:" .. (te.perk or "") .. ":" .. tostring(te.amount or 0) end
    if te.kind == "trait" then return "trait:" .. (te.trait or "") end
    if te.kind == "recipe" then return "recipe:" .. (te.recipe or "") end
    return ""
end
function BanditsNPC.Editor.DecTeach(v)
    if not v or v == "" then return nil end
    local kind = v:match("^(%w+):")
    if kind == "xp" then
        local perk, amt = v:match("^xp:([^:]*):?(.*)$")
        return { kind = "xp", perk = perk, amount = tonumber(amt) or 0 }
    elseif kind == "trait" then return { kind = "trait", trait = v:match("^trait:(.*)$") }
    elseif kind == "recipe" then return { kind = "recipe", recipe = v:match("^recipe:(.*)$") } end
    return nil
end

-- Response encoding for the editor / save files. Generic variants are joined by
-- " | "; profession-specific answers are written as "Exp: text" segments where
-- Exp is a Bandit.Expertise name (Medic, Recon, Cook, ...).
--   topic.response    = string | { variants }
--   topic.responseExp = { ExpName = string | { variants } }
function BanditsNPC.Editor.EncResp(topic)
    local parts = {}
    local r = topic.response
    if type(r) == "table" then for _, v in ipairs(r) do table.insert(parts, v) end
    elseif r and r ~= "" then table.insert(parts, r) end
    if topic.responseExp then
        for expName, resp in pairs(topic.responseExp) do
            if type(resp) == "table" then
                for _, v in ipairs(resp) do table.insert(parts, expName .. ": " .. v) end
            elseif resp and resp ~= "" then table.insert(parts, expName .. ": " .. resp) end
        end
    end
    return table.concat(parts, " | ")
end

-- Returns response, responseExp.
function BanditsNPC.Editor.DecResp(v)
    local generic, byExp = {}, {}
    for seg in ((v or "") .. " | "):gmatch("(.-) | ") do
        if seg ~= "" then
            local exp, text = seg:match("^(%w+):%s+(.*)$")
            if exp and Bandit and Bandit.Expertise and Bandit.Expertise[exp] then
                byExp[exp] = byExp[exp] or {}
                table.insert(byExp[exp], text)
            else
                table.insert(generic, seg)
            end
        end
    end
    local response
    if #generic > 1 then response = generic elseif #generic == 1 then response = generic[1] else response = "" end
    local responseExp
    for exp, arr in pairs(byExp) do
        responseExp = responseExp or {}
        responseExp[exp] = (#arr == 1) and arr[1] or arr
    end
    return response, responseExp
end

function BanditsNPC.Editor.SerializeDialogue()
    local out = {}
    for _, t in ipairs((BanditsNPC.Talk and BanditsNPC.Talk.Topics) or {}) do
        table.insert(out, "[topic]")
        table.insert(out, "id=" .. (t.id or ""))
        table.insert(out, "text=" .. (t.text or ""))
        table.insert(out, "response=" .. BanditsNPC.Editor.EncResp(t))
        table.insert(out, "affinity=" .. tostring(t.affinity or 0))
        table.insert(out, "cooldown=" .. tostring(t.cooldown or 0))
        table.insert(out, "minAffinity=" .. tostring(t.minAffinity or 0))
        table.insert(out, "minDaysKnown=" .. tostring(t.minDaysKnown or 0))
        table.insert(out, "affectionOnly=" .. (t.affectionOnly and "true" or "false"))
        table.insert(out, "platonicOnly=" .. (t.platonicOnly and "true" or "false"))
        -- tri-state: "" = no track switch, "true"/"false" = switch to that track
        table.insert(out, "setRomance=" .. (t.setRomance == true and "true" or (t.setRomance == false and "false" or "")))
        table.insert(out, "needsUnrevealed=" .. (t.needsUnrevealed and "true" or "false"))
        table.insert(out, "reveals=" .. (t.reveals and "true" or "false"))
        table.insert(out, "parent=" .. (t.parent or ""))
        table.insert(out, "category=" .. (t.isCategory and "true" or "false"))
        table.insert(out, "reqPerk=" .. ((t.reqPerk and t.reqPerk.perk) or ""))
        table.insert(out, "reqLevel=" .. tostring((t.reqPerk and t.reqPerk.level) or 0))
        table.insert(out, "reqTrait=" .. (t.reqTrait or ""))
        table.insert(out, "reqNpcExp=" .. (t.reqNpcExp or ""))
        table.insert(out, "reqRival=" .. (t.reqRival and "true" or "false"))
        table.insert(out, "teach=" .. BanditsNPC.Editor.EncTeach(t.teach))
        table.insert(out, "")
    end
    return table.concat(out, "\n")
end

function BanditsNPC.Editor.SaveDialogue()
    local w = getFileWriter(BanditsNPC.Editor.DIALOGUE_FILE, true, false)
    if not w then return false end
    w:write(BanditsNPC.Editor.SerializeDialogue()); w:close(); return true
end

function BanditsNPC.Editor.LoadDialogue()
    local r = getFileReader(BanditsNPC.Editor.DIALOGUE_FILE, false)
    if not r then return false end
    local topics, cur = {}, nil
    local line = r:readLine()
    while line ~= nil do
        line = line:gsub("[\r\n]", "")
        if line == "[topic]" then cur = {}; table.insert(topics, cur)
        elseif cur and line ~= "" then
            local k, v = line:match("^(%w+)=(.*)$")
            if k == "id" then cur.id = v
            elseif k == "text" then cur.text = v
            elseif k == "response" then cur.response, cur.responseExp = BanditsNPC.Editor.DecResp(v)
            elseif k == "affinity" then cur.affinity = tonumber(v) or 0
            elseif k == "cooldown" then cur.cooldown = tonumber(v) or 0
            elseif k == "minAffinity" then cur.minAffinity = tonumber(v)
            elseif k == "minDaysKnown" then cur.minDaysKnown = tonumber(v)
            elseif k == "affectionOnly" then cur.affectionOnly = (v == "true")
            elseif k == "platonicOnly" then cur.platonicOnly = (v == "true")
            elseif k == "setRomance" then
                if v == "true" then cur.setRomance = true elseif v == "false" then cur.setRomance = false end
            elseif k == "needsUnrevealed" then cur.needsUnrevealed = (v == "true")
            elseif k == "reveals" then cur.reveals = (v == "true")
            elseif k == "parent" then cur.parent = (v ~= "" and v) or nil
            elseif k == "category" then cur.isCategory = (v == "true")
            elseif k == "reqPerk" then cur._reqPerk = v
            elseif k == "reqLevel" then cur._reqLevel = tonumber(v) or 0
            elseif k == "reqTrait" then cur.reqTrait = (v ~= "" and v) or nil
            elseif k == "reqNpcExp" then cur.reqNpcExp = (v ~= "" and v) or nil
            elseif k == "reqRival" then cur.reqRival = (v == "true")
            elseif k == "teach" then cur.teach = BanditsNPC.Editor.DecTeach(v)
            end
        end
        line = r:readLine()
    end
    r:close()
    -- assemble reqPerk from the streamed perk/level keys
    for _, t in ipairs(topics) do
        if t._reqPerk and t._reqPerk ~= "" and (t._reqLevel or 0) > 0 then
            t.reqPerk = { perk = t._reqPerk, level = t._reqLevel }
        end
        t._reqPerk, t._reqLevel = nil, nil
    end
    if #topics > 0 and BanditsNPC.Talk then BanditsNPC.Talk.Topics = topics end
    return true
end

-- ===== quest templates =====

BanditsNPC.Editor.QUEST_FILE = "BanditsNPC_quests.txt"

function BanditsNPC.Editor.SerializeQuests()
    local out = {}
    for _, q in ipairs((BanditsNPC.Quests and BanditsNPC.Quests.Templates) or {}) do
        table.insert(out, "[quest]")
        table.insert(out, "type=" .. (q.type or "fetch"))
        table.insert(out, "itemType=" .. (q.itemType or ""))
        -- tkey and itemTypes MUST round-trip (LoadQuests replaces the whole template
        -- list). Dropping them cost two regressions at once: without tkey every quest
        -- falls back to English forever regardless of language pack, and without
        -- itemTypes the variant lists die -- "cans of soda" goes back to accepting only
        -- Base.Pop and refusing Pop2/Pop3/PopBottle/SodaCan, which is verbatim the
        -- 11 Jul "can not turn in items that were asked for" report (29 Jul audit, B4).
        table.insert(out, "tkey=" .. (q.tkey or ""))
        table.insert(out, "itemTypes=" .. ((q.itemTypes and table.concat(q.itemTypes, ",")) or ""))
        table.insert(out, "name=" .. (q.name or ""))
        table.insert(out, "min=" .. tostring(q.min or 1))
        table.insert(out, "max=" .. tostring(q.max or 1))
        table.insert(out, "reward=" .. tostring(q.reward or 10))
        table.insert(out, "desc=" .. (q.desc or ""))
        table.insert(out, "success=" .. (q.success or ""))
        table.insert(out, "minAffinity=" .. tostring(q.minAffinity or 0))
        table.insert(out, "npcExp=" .. (q.npcExp or ""))
        table.insert(out, "reqPersonality=" .. (q.reqPersonality or ""))
        table.insert(out, "rewardItem=" .. ((q.rewardItem and ((q.rewardItem.type or "") .. ":" .. tostring(q.rewardItem.count or 1))) or ""))
        table.insert(out, "rewardXp=" .. ((q.rewardXp and ((q.rewardXp.perk or "") .. ":" .. tostring(q.rewardXp.amount or 0))) or ""))
        table.insert(out, "")
    end
    return table.concat(out, "\n")
end

function BanditsNPC.Editor.SaveQuests()
    local w = getFileWriter(BanditsNPC.Editor.QUEST_FILE, true, false)
    if not w then return false end
    w:write(BanditsNPC.Editor.SerializeQuests()); w:close(); return true
end

function BanditsNPC.Editor.LoadQuests()
    local r = getFileReader(BanditsNPC.Editor.QUEST_FILE, false)
    if not r then return false end
    local list, cur = {}, nil
    local line = r:readLine()
    while line ~= nil do
        line = line:gsub("[\r\n]", "")
        if line == "[quest]" then cur = {}; table.insert(list, cur)
        elseif cur and line ~= "" then
            local k, v = line:match("^(%w+)=(.*)$")
            if k == "type" then cur.type = v
            elseif k == "itemType" then cur.itemType = v
            elseif k == "tkey" then cur.tkey = (v ~= "" and v) or nil
            elseif k == "itemTypes" then
                if v ~= "" then
                    local t = {}
                    for s in v:gmatch("[^,]+") do t[#t + 1] = s:match("^%s*(.-)%s*$") end
                    cur.itemTypes = (#t > 0) and t or nil
                end
            elseif k == "name" then cur.name = v
            elseif k == "min" then cur.min = tonumber(v) or 1
            elseif k == "max" then cur.max = tonumber(v) or 1
            elseif k == "reward" then cur.reward = tonumber(v) or 10
            elseif k == "desc" then cur.desc = (v ~= "" and v) or nil
            elseif k == "success" then cur.success = (v ~= "" and v) or nil
            elseif k == "minAffinity" then local n = tonumber(v) or 0; cur.minAffinity = n > 0 and n or nil
            elseif k == "npcExp" then cur.npcExp = (v ~= "" and v) or nil
            elseif k == "reqPersonality" then cur.reqPersonality = (v ~= "" and v) or nil
            elseif k == "rewardItem" then
                if v ~= "" then local t, c = v:match("^([^:]*):?(.*)$"); cur.rewardItem = { type = t, count = tonumber(c) or 1 } end
            elseif k == "rewardXp" then
                if v ~= "" then local p, a = v:match("^([^:]*):?(.*)$"); cur.rewardXp = { perk = p, amount = tonumber(a) or 0 } end
            end
        end
        line = r:readLine()
    end
    r:close()
    if #list > 0 and BanditsNPC.Quests then BanditsNPC.Quests.Templates = list end
    return true
end

-- ===== wild-spawn clan selection =====
-- Which clans the wild friendly spawner draws from (picked in the editor's
-- Spawning tab). Stored as clan IDs so a clan rename can't break it.
BanditsNPC.SpawnConfig = BanditsNPC.SpawnConfig or { clans = {} }   -- clans = { [cid] = true }
BanditsNPC.Editor.SPAWN_FILE = "BanditsNPC_spawn.txt"

-- THE WRITE ITSELF. Runs where the file matters: single player, or the SERVER after it
-- has checked the caller's access level.
function BanditsNPC.Editor.ApplySpawnConfig(clans)
    if type(clans) == "table" then
        local clean = {}
        for cid, on in pairs(clans) do
            -- the payload crosses the wire, so take only what this file can represent:
            -- string ids, flagged on. Anything else would be written back out verbatim.
            if on and type(cid) == "string" and cid ~= "" and not cid:find("[\r\n]") then
                clean[cid] = true
            end
        end
        BanditsNPC.SpawnConfig = BanditsNPC.SpawnConfig or {}
        BanditsNPC.SpawnConfig.clans = clean
    end
    local w = getFileWriter(BanditsNPC.Editor.SPAWN_FILE, true, false)
    if not w then return false end
    local out = {}
    for cid, on in pairs(BanditsNPC.SpawnConfig.clans or {}) do
        if on then table.insert(out, "clan=" .. cid) end
    end
    w:write(table.concat(out, "\n")); w:close()
    return true
end

-- WHICH CLANS THE WILD SPAWNER DRAWS FROM. The one editor catalogue whose consumer is
-- SERVER-SIDE (server/BanditsNPCWildSpawn.lua:98 reads BanditsNPC.SpawnConfig.clans), while
-- the tab that sets it is client UI. Every catalogue here is loaded per-machine from that
-- machine's own Zomboid folder at boot, so on a dedicated server the admin's ticks landed in
-- a file the server never reads: the tab appeared to work -- the selection persisted and
-- reloaded for the admin -- and changed nothing about who actually spawned (audit Cluster B).
--
-- Same shape as BanditsNPC.SetOpt: in MP the client asks, the server checks access level and
-- performs the write. In SP there is no server to ask and the direct path is correct.
function BanditsNPC.Editor.SaveSpawnConfig()
    if isClient and isClient() then
        local p = getSpecificPlayer(0)
        if not p then return false end
        sendClientCommand(p, 'BanditsNPC', 'SetSpawnConfig',
                          { clans = BanditsNPC.SpawnConfig and BanditsNPC.SpawnConfig.clans or {} })
        return true
    end
    return BanditsNPC.Editor.ApplySpawnConfig(nil)
end

function BanditsNPC.Editor.LoadSpawnConfig()
    local r = getFileReader(BanditsNPC.Editor.SPAWN_FILE, false)
    if not r then return false end
    local clans = {}
    local line = r:readLine()
    while line ~= nil do
        line = line:gsub("[\r\n]", "")
        local cid = line:match("^clan=(.+)$")
        if cid and cid ~= "" then clans[cid] = true end
        line = r:readLine()
    end
    r:close()
    BanditsNPC.SpawnConfig.clans = clans
    return true
end

local function onBoot()
    if BanditsNPC.Production then BanditsNPC.Editor.LoadRecipes() end
    BanditsNPC.Editor.LoadDialogue()
    BanditsNPC.Editor.LoadQuests()
    BanditsNPC.Editor.LoadSpawnConfig()
end
Events.OnGameBoot.Add(onBoot)
