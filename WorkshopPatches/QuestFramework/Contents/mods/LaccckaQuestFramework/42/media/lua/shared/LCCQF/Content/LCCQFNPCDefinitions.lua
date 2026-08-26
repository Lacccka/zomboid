require "LCCQF/LCCQFConstants"
require "LCCQF/Core/LCCQFNPCRegistry"

local C = LCCQF.Constants

local ok, err = LCCQF.NPCRegistry.Register({
    npcId = C.TEST_NPC_ID,
    displayNameKey = "IGUI_LCCQF_NPC_Alexey",
    dialogueId = "test_alexey",
    stationary = true,
    runtime = {
        adapter = "Bandits",
        profileId = C.TEST_NPC_BID,
        clanId = C.TEST_NPC_CID,
        program = "LCCQFQuestGiver",
    },
})

if not ok and err ~= "duplicate npcId" then
    print(C.LOG_PREFIX .. "[REGISTRY] test NPC registration failed: " .. tostring(err))
end

return LCCQF.NPCRegistry
