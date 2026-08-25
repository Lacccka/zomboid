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

function TalkToNPC.EvaluateTalk(objective, npcId)
    if not objective or objective.state ~= "active" then return false end
    if type(npcId) ~= "string" then return false end
    return tostring(objective.npcId) == tostring(npcId)
end

LCCQF.QuestObjectives.TalkToNPC = TalkToNPC

return TalkToNPC
