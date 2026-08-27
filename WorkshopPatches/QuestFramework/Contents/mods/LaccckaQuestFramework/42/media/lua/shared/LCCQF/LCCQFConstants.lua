LCCQF = LCCQF or {}

local Constants = {}

Constants.MODULE = "LaccckaQuestFramework"
Constants.VERSION = "0.3.4"
Constants.LOG_PREFIX = "[LCCQF]"

Constants.INTERACTION_RANGE = 3.25
Constants.SERVER_INTERACTION_RANGE = 4.0
Constants.RUNTIME_RESOLVE_PADDING = 1.0
Constants.RUNTIME_RECONCILE_INTERVAL_MS = 250
Constants.QUEST_UPDATE_INTERVAL_MS = 250
Constants.REQUEST_COOLDOWN_MS = 150
Constants.SESSION_TIMEOUT_MS = 15 * 60 * 1000
Constants.COMMAND_HISTORY_TIMEOUT_MS = 30 * 60 * 1000
Constants.MAX_IDENTIFIER_LENGTH = 96
Constants.MAX_DIALOGUE_CHOICES = 3

Constants.PERSISTENCE_TAG = "LCCQF_Persistence"
Constants.PERSISTENCE_SCHEMA_VERSION = 1
Constants.QUEST_PERSISTENCE_SCHEMA_VERSION = 1
Constants.KNOWLEDGE_PERSISTENCE_SCHEMA_VERSION = 1
Constants.RELATIONSHIP_PERSISTENCE_SCHEMA_VERSION = 1
Constants.FACTION_KNOWLEDGE_PERSISTENCE_SCHEMA_VERSION = 1
Constants.FACTION_RELATIONSHIP_PERSISTENCE_SCHEMA_VERSION = 1
Constants.CHARACTER_ID_MODDATA_KEY = "lccqCharacterId"

Constants.COMMAND = {
    REQUEST_DIALOGUE = "RequestDialogue",
    CHOOSE_DIALOGUE = "ChooseDialogue",
    CLOSE_DIALOGUE = "CloseDialogue",
    SPAWN_TEST_NPC = "SpawnTestNPC",
    REQUEST_RUNTIME_BINDINGS = "RequestRuntimeBindings",
    RUNTIME_BINDINGS = "RuntimeBindings",
    RUNTIME_BINDING_UPSERT = "RuntimeBindingUpsert",
    RUNTIME_BINDING_REMOVE = "RuntimeBindingRemove",
    REQUEST_QUESTS = "RequestQuests",
    QUESTS = "Quests",
    QUEST_UPSERT = "QuestUpsert",
    QUEST_EVENT = "QuestEvent",
    REQUEST_KNOWLEDGE = "RequestKnowledge",
    KNOWLEDGE = "Knowledge",
    KNOWN_PERSON_UPSERT = "KnownPersonUpsert",
    REQUEST_FACTIONS = "RequestFactions",
    FACTIONS = "Factions",
    KNOWN_FACTION_UPSERT = "KnownFactionUpsert",
    DIALOGUE_STATE = "DialogueState",
    DIALOGUE_CLOSED = "DialogueClosed",
    STATUS = "Status",
}

Constants.TEST_NPC_ID = "lccq_test_npc_01"
Constants.TEST_NPC_BID = "e6b19154-2cf4-4ef9-9ad5-1ac8c85a0001"
Constants.TEST_NPC_CID = "c2c9d2b8-7a21-4e66-a8d9-1cfa01caa001"
Constants.TEST_FACTION_ID = "lccq_checkpoint_survivors"
Constants.TEST_QUEST_ID = "lccq_test_checkpoint"
Constants.TEST_QUEST_2_ID = "lccq_test_supply_run"

LCCQF.Constants = Constants

return Constants
