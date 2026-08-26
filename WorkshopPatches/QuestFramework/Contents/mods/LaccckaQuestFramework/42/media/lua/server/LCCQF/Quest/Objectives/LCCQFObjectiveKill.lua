LCCQF = LCCQF or {}
LCCQF.QuestObjectives = LCCQF.QuestObjectives or {}

local Kill = {}

function Kill.Create(spec)
    if type(spec) ~= "table" then return nil, "invalid Kill objective" end

    local required = math.max(1, math.min(1000, math.floor(tonumber(spec.required) or 1)))
    return {
        id = spec.id,
        type = "Kill",
        titleKey = spec.titleKey,
        state = "pending",
        required = required,
        progress = 0,
    }
end

function Kill.ValidatePersisted(objective)
    return type(objective) == "table"
        and tonumber(objective.required) ~= nil
        and tonumber(objective.progress) ~= nil
end

function Kill.EvaluateZombieDeath(player, objective, zombie)
    if not player or not objective or objective.state ~= "active" or not zombie then
        return false, false
    end

    local killer = zombie.getAttackedBy and zombie:getAttackedBy() or nil
    if killer ~= player then return false, false end

    local progress = math.min(objective.required, (tonumber(objective.progress) or 0) + 1)
    objective.progress = progress
    return progress >= objective.required, true, "kill_zombie"
end

function Kill.MakeProgressView(objective)
    return tonumber(objective and objective.progress) or 0,
        tonumber(objective and objective.required) or 1
end

LCCQF.QuestObjectives.Kill = Kill

return Kill
