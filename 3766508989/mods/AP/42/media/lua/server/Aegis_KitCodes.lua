-- Kit codes: the Discord bot signs a code that unlocks one kit for one
-- claim. Same key and same signature as the booster codes, told apart by
-- the prefix. Two stores in one file: vouchers waiting to be claimed and
-- the signatures of codes already spent.
if isClient() then return end

require "Aegis_Boost"
require "Aegis_Kits"

AegisKitCodes = AegisKitCodes or {}

local FILE = AegisStore.ROOT .. "/Player/kitcodes.txt"
local MAX_CODE = 96
local MAX_KIT_TAG = 24
-- a code lives 30 minutes, so a spent signature only has to be remembered
-- long enough that no valid code can come back; a week is generous
local SPENT_KEEP = 7 * 24 * 3600
local MAX_LINES = 20000

local vouchers = {}   -- username -> { kitId -> true }
local spent = {}      -- mac12 -> epoch
local loaded = false
local incomplete = false

local function nameOk(name)
    return type(name) == "string" and name ~= "" and #name <= 48
        and not name:find("[%c|]")
end

-- line: V|username|kitId   or   S|mac12|epoch
local function load()
    if loaded then return end
    loaded = true
    vouchers, spent = {}, {}
    local lines, truncated = AegisStore.readLines(FILE, MAX_LINES)
    if truncated then
        incomplete = true
        print("[Aegis] Kit code store truncated, redemptions blocked")
        return
    end
    for _, line in ipairs(lines or {}) do
        local kind, a, b = line:match("^(%a)|([^|]*)|([^|]*)$")
        if kind == "V" and a ~= "" and b ~= "" then
            vouchers[a] = vouchers[a] or {}
            vouchers[a][b] = true
        elseif kind == "S" and a ~= "" then
            spent[a] = tonumber(b) or 0
        end
    end
end

-- rewrite drops spent signatures that can no longer match a live code, so
-- the file does not grow without end on a busy server
local function save()
    if incomplete then return false end
    local now = AegisShared.realTime()
    local out = {}
    for user, kits in pairs(vouchers) do
        for kitId in pairs(kits) do
            table.insert(out, "V|" .. user .. "|" .. kitId)
        end
    end
    for sig, epoch in pairs(spent) do
        if now - epoch < SPENT_KEEP then
            table.insert(out, "S|" .. sig .. "|" .. tostring(epoch))
        else
            spent[sig] = nil
        end
    end
    table.sort(out)
    local content = table.concat(out, "\n")
    if #out > 0 then content = content .. "\n" end
    return AegisStore.write(FILE, content) == true
end

-- ---------- code ----------

-- AEGK1.<discordId36>.<kitTag>.<expiryMinutes36>.<mac12>. The payload is
-- rebuilt from the parsed fields and the number field must be canonical,
-- so what gets signed here is byte identical to what the bot signed.
local function parseCode(code)
    if type(code) ~= "string" then return nil, "format" end
    local s = code:gsub("%s+", "")
    if s == "" or #s > MAX_CODE then return nil, "format" end
    s = s:upper()
    if s:find("[^0-9A-Z%.]") then return nil, "format" end
    local id, tag, expF, sig = s:match("^AEGK1%.([0-9A-Z]+)%.([0-9A-Z]+)%.([0-9A-Z]+)%.([0-9A-Z]+)$")
    if not id then return nil, "format" end
    if not AegisBoost.discordIdOk(id) then return nil, "format" end
    if #tag > MAX_KIT_TAG then return nil, "format" end
    if #sig ~= 12 then return nil, "format" end
    local expMin = AegisBoost.fromB36(expF)
    if not expMin then return nil, "format" end
    if AegisBoost.toB36(expMin) ~= expF then return nil, "format" end
    return {
        discordId = id,
        kitTag = tag,
        expiryEpoch = expMin * 60,
        payload = "AEGK1." .. id .. "." .. tag .. "." .. expF,
        sig = sig,
    }
end

-- the tag travels in A to Z and 0 to 9 only, kit ids and names do not, so
-- both sides are reduced to that alphabet before they are compared
local function normalise(s)
    return tostring(s or ""):upper():gsub("[^0-9A-Z]", "")
end

local function kitForTag(tag)
    local want = normalise(tag)
    if want == "" then return nil end
    local list = AegisKits.list and AegisKits.list() or {}
    for _, kit in ipairs(list) do
        if normalise(kit.id) == want then return kit end
    end
    for _, kit in ipairs(list) do
        if normalise(kit.name) == want then return kit end
    end
    return nil
end

-- ---------- public ----------

function AegisKitCodes.hasVoucher(userName, kitId)
    if not nameOk(userName) then return false end
    load()
    local v = vouchers[userName]
    return v ~= nil and v[kitId] == true
end

function AegisKitCodes.useVoucher(userName, kitId)
    if not AegisKitCodes.hasVoucher(userName, kitId) then return false end
    vouchers[userName][kitId] = nil
    -- no global next() on the dedicated server
    local empty = true
    for _ in pairs(vouchers[userName]) do
        empty = false
        break
    end
    if empty then vouchers[userName] = nil end
    save()
    AegisLog.write("Actions", "KitCodes", userName,
        "Kit voucher used by " .. userName .. ": " .. tostring(kitId))
    return true
end

-- true plus the kit name on success, otherwise false plus a reason:
-- nokey|format|sig|expired|nokit|used|held|error
function AegisKitCodes.redeem(player, code)
    local name = player and player:getUsername()
    if not nameOk(name) then return false, "error" end
    if not AegisBoost.keySet() then return false, "nokey" end
    load()
    if incomplete then return false, "error" end

    local parsed, reason = parseCode(code)
    if not parsed then return false, reason end
    if AegisBoost.sign(parsed.payload) ~= parsed.sig then return false, "sig" end
    if parsed.expiryEpoch <= AegisShared.realTime() then return false, "expired" end
    if spent[parsed.sig] then return false, "used" end

    local kit = kitForTag(parsed.kitTag)
    if not kit then return false, "nokit" end
    if AegisKitCodes.hasVoucher(name, kit.id) then return false, "held" end

    vouchers[name] = vouchers[name] or {}
    vouchers[name][kit.id] = true
    spent[parsed.sig] = AegisShared.realTime()
    if not save() then
        vouchers[name][kit.id] = nil
        spent[parsed.sig] = nil
        return false, "error"
    end
    AegisLog.write("Actions", "KitCodes", name,
        "Kit code redeemed by " .. name .. ": " .. kit.id .. " (discord " .. parsed.discordId .. ")")
    return true, kit.name
end

-- ---------- command ----------

local PlayerCommands = {}

PlayerCommands.kitCodeRedeem = function(player, args)
    if not player or not args then return end
    local ok, info = AegisKitCodes.redeem(player, tostring(args.code or ""))
    sendServerCommand(player, "AegisPlayer", "kitCodeResult", {
        ok = ok == true,
        reason = (not ok) and tostring(info or "error") or nil,
        kit = ok and tostring(info or "") or nil,
    })
    if ok and AegisPlayerPanel and AegisPlayerPanel.push then
        AegisPlayerPanel.push(player)
    end
end

Events.OnClientCommand.Add(function(module, command, player, args)
    if module ~= "AegisPlayer" then return end
    if AegisModeration and AegisModeration.isSuspended and AegisModeration.isSuspended(player) then return end
    if PlayerCommands[command] then PlayerCommands[command](player, args) end
end)
