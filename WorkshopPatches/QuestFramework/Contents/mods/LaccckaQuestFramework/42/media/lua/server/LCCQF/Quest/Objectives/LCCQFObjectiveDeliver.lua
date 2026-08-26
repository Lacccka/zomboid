require "LCCQF/Quest/Objectives/LCCQFObjectiveItemUtils"

LCCQF = LCCQF or {}
LCCQF.QuestObjectives = LCCQF.QuestObjectives or {}

local ItemUtils = LCCQF.QuestObjectives.ItemUtils
local Deliver = {}

local function validString(value, maxLength)
    return type(value) == "string" and value ~= "" and #value <= (maxLength or 128)
end

function Deliver.Create(spec, context)
    if type(spec) ~= "table" or not validString(spec.itemFullType, 128) then
        return nil, "invalid Deliver objective"
    end

    local npcId = spec.npcId or (context and context.giverNpcId)
    if not validString(npcId, 96) then return nil, "Deliver npcId unavailable" end

    local required = math.max(1, math.min(100, math.floor(tonumber(spec.required) or 1)))
    return {
        id = spec.id,
        type = "Deliver",
        titleKey = spec.titleKey,
        state = "pending",
        npcId = npcId,
        itemFullType = spec.itemFullType,
        required = required,
        progress = 0,
    }
end

function Deliver.ValidatePersisted(objective)
    return type(objective) == "table"
        and validString(objective.npcId, 96)
        and validString(objective.itemFullType, 128)
        and tonumber(objective.required) ~= nil
        and tonumber(objective.progress) ~= nil
end

function Deliver.EvaluateTick(player, objective)
    if not player or not objective or objective.state ~= "active" then return false, false end
    local count = math.min(objective.required, ItemUtils.Count(player, objective.itemFullType))
    local changed = tonumber(objective.progress) ~= count
    objective.progress = count
    return false, changed
end

function Deliver.EvaluateTalk(player, objective, npcId)
    if not player or not objective or objective.state ~= "active" then return false, false end
    if tostring(objective.npcId or "") ~= tostring(npcId or "") then return false, false end

    local available = ItemUtils.Count(player, objective.itemFullType)
    local shown = math.min(objective.required, available)
    local changed = tonumber(objective.progress) ~= shown
    objective.progress = shown
    if available < objective.required then return false, changed end

    local removed = ItemUtils.Remove(player, objective.itemFullType, objective.required)
    if not removed then return false, changed end

    objective.progress = objective.required
    return true, true, "deliver_items"
end

function Deliver.MakeProgressView(objective)
    return tonumber(objective and objective.progress) or 0,
        tonumber(objective and objective.required) or 1
end

LCCQF.QuestObjectives.Deliver = Deliver

return Deliver
