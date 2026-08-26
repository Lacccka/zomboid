require "LCCQF/Quest/Objectives/LCCQFObjectiveItemUtils"

LCCQF = LCCQF or {}
LCCQF.QuestObjectives = LCCQF.QuestObjectives or {}

local ItemUtils = LCCQF.QuestObjectives.ItemUtils
local Fetch = {}

local function validFullType(value)
    return type(value) == "string" and value ~= "" and #value <= 128
end

function Fetch.Create(spec)
    if type(spec) ~= "table" or not validFullType(spec.itemFullType) then
        return nil, "invalid Fetch objective"
    end

    local required = math.max(1, math.min(100, math.floor(tonumber(spec.required) or 1)))
    return {
        id = spec.id,
        type = "Fetch",
        titleKey = spec.titleKey,
        state = "pending",
        itemFullType = spec.itemFullType,
        required = required,
        progress = 0,
    }
end

function Fetch.ValidatePersisted(objective)
    return type(objective) == "table"
        and validFullType(objective.itemFullType)
        and tonumber(objective.required) ~= nil
        and tonumber(objective.progress) ~= nil
end

function Fetch.EvaluateTick(player, objective)
    if not player or not objective or objective.state ~= "active" then return false, false end
    local count = math.min(objective.required, ItemUtils.Count(player, objective.itemFullType))
    local changed = tonumber(objective.progress) ~= count
    objective.progress = count
    return count >= objective.required, changed, "fetch_items"
end

function Fetch.MakeProgressView(objective)
    return tonumber(objective and objective.progress) or 0,
        tonumber(objective and objective.required) or 1
end

LCCQF.QuestObjectives.Fetch = Fetch

return Fetch
