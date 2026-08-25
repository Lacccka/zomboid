local NPC_GuardConfig = {}

NPC_GuardConfig.ZONE_SCAN = {
    MAX_RENDER_DISTANCE = 100,
    EXTENDED_RANGE_DISTANCE = 200,

    TIMED_REFRESH = {
        MIN_MINUTES = 10,
        MAX_MINUTES = 30
    },

    MOVEMENT_THRESHOLD = 5,
    ZONE_CHECK_COOLDOWN = 3
}

NPC_GuardConfig.BOUNDARY_CHECK = {
    THROTTLE_COOLDOWN = 1.0,
    CHECK_ON_MOVEMENT = true
}

NPC_GuardConfig.ZOMBIE_DETECTION = {
    PLAYER_IN_ZONE_MIN = 1,
    PLAYER_IN_ZONE_MAX = 2,

    PLAYER_NEAR_ZONE_MINUTES = 5,
    PLAYER_NEAR_ZONE_DISTANCE = 50,

    PLAYER_FAR_ZONE_MINUTES = 10,
    PLAYER_FAR_ZONE_DISTANCE = 100,

    PLAYER_BEYOND_RENDER_DORMANT = true
}

NPC_GuardConfig.ATTACK = {
    MUGGY = {
        damageRange = {
            min = 0.5,
            max = 3.0
        },
        attackRange = 6.0,
        multiHit = 1,
        attackSpeed = {
            baseMin = 1.5,
            baseMax = 3.0,
            modifier = 1.0
        },
        targetTypes = {"IsoZombie"},
        animationName = "attack",
        animationEstimatedDuration = 1.0
    }
}

NPC_GuardConfig.IDLE_CHILLING = {
    INTERVAL_MIN = 1,
    INTERVAL_MAX = 4,

    DISTANCE_MIN = 2,
    DISTANCE_MAX = 7
}

NPC_GuardConfig.COMMANDS = {
    GUARD_SYSTEM = {
        MODULE = "NPCGuardSystem_v1",
        ACTIVATE = "ActivateGuarding_v1",
        REFRESH = "RefreshGuarding_v1",
        UPDATE_BOUNDARY = "UpdateBoundary_v1",
        NPC_EXITED_ZONE = "NPCExitedZone_v1",
        NPC_RETURNED_ZONE = "NPCReturnedToZone_v1"
    },

    ATTACK_SYSTEM = {
        MODULE = "NPCAttackSystem_v1",
        INITIATE = "InitiateAttack_v1",
        EXECUTE = "ExecuteAttack_v1",
        TARGET_ELIMINATED = "TargetEliminated_v1",
        ALL_CLEARED = "AllTargetsCleared_v1",
        SET_ATTACKING_STATE = "SetAttackingState_v1"
    },

    IDLE_SYSTEM = {
        MODULE = "NPCIdleSystem_v1",
        ACTIVATE = "ActivateIdleChilling_v1",
        EXECUTE_MOVE = "ExecuteIdleMove_v1",
        DEACTIVATE = "DeactivateIdleChilling_v1"
    },

    SCAN_SYSTEM = {
        MODULE = "NPCZoneScanSystem_v1",
        ACTIVATE = "ActivateScan_v1",
        NPC_DETECTED = "NPCDetected_v1",
        SCAN_DORMANT = "ScanDormant_v1",
        REFRESH = "RefreshScan_v1"
    }
}

NPC_GuardConfig.PRIORITIES = {
    ATTACKING = 7,
    GUARDING = 5,
    IDLE_CHILLING = 3
}

return NPC_GuardConfig
