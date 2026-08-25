LCCQF = LCCQF or {}

local Constants = {}

Constants.MODULE = "LaccckaQuestFramework"
Constants.VERSION = "0.1.0"
Constants.LOG_PREFIX = "[LCCQF]"

Constants.INTERACTION_RANGE = 3.25
Constants.SERVER_INTERACTION_RANGE = 4.0

Constants.TEST_NPC_KEY = "lccq_test_npc_01"
Constants.TEST_NPC_BID = "e6b19154-2cf4-4ef9-9ad5-1ac8c85a0001"
Constants.TEST_NPC_CID = "c2c9d2b8-7a21-4e66-a8d9-1cfa01caa001"

Constants.NPCS = {
    [Constants.TEST_NPC_KEY] = {
        key = Constants.TEST_NPC_KEY,
        bid = Constants.TEST_NPC_BID,
        displayName = "Алексей",
        dialogueId = "test_alexey",
        stationary = true,
    },
}

LCCQF.Constants = Constants

function LCCQF.GetNPCDefinition(npcKey)
    return Constants.NPCS[npcKey]
end

return Constants
