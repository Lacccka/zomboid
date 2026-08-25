local NPC_BehaviorConfig = {}

NPC_BehaviorConfig.QUEUE = {
    MAX_BEHAVIORS_PER_NPC = 20,
    ALLOW_DUPLICATE_BEHAVIORS = false
}

NPC_BehaviorConfig.PRIORITY = {
    TALKING = 15,
    CRITICAL = 10,
    HIGH = 7,
    MEDIUM = 5,
    LOW = 3,
    MINIMAL = 1
}

NPC_BehaviorConfig.STATUS = {
    QUEUED = "queued",
    EXECUTING = "executing",
    COMPLETED = "completed",
    FAILED = "failed",
    CANCELLED = "cancelled"
}

NPC_BehaviorConfig.ASYNC = {
    ENABLED = true,
    DEFAULT_TIMEOUT = 30.0,
    CALLBACK_DELAY = 0.5
}

NPC_BehaviorConfig.CLEANUP = {
    ON_DIALOGUE_END = true,
    ON_BEHAVIOR_COMPLETION = false,
    ON_EXPLICIT_CALL = true,

    TIMED_CLEANUP = {
        ENABLED = true,
        COMPLETED_AFTER_SECONDS = 60.0,
        FAILED_AFTER_SECONDS = 30.0,
        CANCELLED_AFTER_SECONDS = 10.0
    }
}

NPC_BehaviorConfig.CHAINING = {
    ENABLED = true,
    MAX_CHAIN_DEPTH = 10,
    ABORT_ON_FAILURE = true
}

return NPC_BehaviorConfig
