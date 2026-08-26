LCCQF = LCCQF or {}
LCCQF.QuestObjectives = LCCQF.QuestObjectives or {}

local TalkToNPC = {}

function TalkToNPC.Create(spec, context)
    if type(spec) ~= "table" then return nil, "invalid TalkToNPC objective" end

    local npcId = spec.npcId or (context and context.giverNpcId)
    if type(npcId) ~= "string" or npcId == "" then
        return nil, "TalkToNPC npcId unavailable"
    end

    return {
        id = spec.id,
        type = "TalkToNPC",
        titleKey = spec.titleKey,
        state = "pending",
        npcId = npcId,
    }
end

function TalkToNPC.ValidatePersisted(objective)
    return type(objective) == "table"
        and type(objective.npcId) == "string"
        and objective.npcId ~= ""
end

function TalkToNPC.EvaluateTalk(player, objective, npcId)
    if not objective or objective.state ~= "active" then return false, false end
    if type(npcId) ~= "string" then return false, false end
    return tostring(objective.npcId) == tostring(npcId), false, "talk_to_npc"
end

function TalkToNPC.MakeProgressView(objective)
    return objective and objective.state == "completed" and 1 or 0, 1
end

LCCQF.QuestObjectives.TalkToNPC = TalkToNPC

return TalkToNPC
