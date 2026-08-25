QPReputation = QPReputation or {}

-- QPSR_V041_RELEASE_CANDIDATE_CLEANUP

QPReputation.Config = {
    Enabled = true,
    Debug = false,
    DataKey = "QPReputation_v1",
    MaxHistoryEntries = 100,
    ShowExactPoints = true,
    EnableNotifications = true,
    EnableLevelUpNotifications = true,
    AllowAdminChatCommands = true,

    Paths = {
        community = true,
        hunter = true,
        explorer = true,
        medic = true,
        mechanic = true,
        builder = true,
    },

    Automation = {
        Enabled = true,
        RegistryVersion = 2,
        ScanEveryMinutes = 1,
        MaxKillsPerScan = 50,

        -- Generic automation registry. Hunter and Community are implemented.
        -- Community remains disabled until an administrator enables it.
        -- Every future path remains disabled until its own tested release.
        Paths = {
            hunter = {
                Enabled = true,
                Implemented = true,
            },
            community = {
                Enabled = false,
                Implemented = true,
            },
            explorer = {
                Enabled = false,
                Implemented = false,
            },
            medic = {
                Enabled = false,
                Implemented = false,
            },
            mechanic = {
                Enabled = false,
                Implemented = false,
            },
            builder = {
                Enabled = false,
                Implemented = false,
            },
        },

        Community = {
            -- Event-driven integrations. Existing worlds remain disabled
            -- until an administrator enables Community automation.
            Enabled = false,
            DailyPointCap = 25,

            SupplyRequestsEnabled = true,
            SupplyRequestPoints = 5,

            -- Survivor Tasks integration was removed from v0.4.1.
            -- Production rule: self-created Supply Requests are not rewarded.
            RequireDifferentCreator = true,

        },

        Hunter = {
            Enabled = true,

            -- Set false for production thresholds.
            UseTestMilestones = false,

            -- Progress starts when QPSR first observes the survivor.
            -- Existing lifetime kills are recorded as the baseline and are
            -- never rewarded retroactively.
            CountFromFirstSeen = true,

            Milestones = {
                { kills = 100, points = 10 },
                { kills = 250, points = 20 },
                { kills = 500, points = 40 },
                { kills = 1000, points = 75 },
            },

            TestMilestones = {
                { kills = 1, points = 10 },
                { kills = 3, points = 20 },
                { kills = 5, points = 40 },
                { kills = 10, points = 75 },
            },

            DailyPointCap = 145,
        },
    },

    Thresholds = { 0, 100, 250, 500, 1000, 2000, 4000, 7500, 12500, 20000, 35000 },

    GenericTitles = {
        getText("UI_QPSR_Unknown"), getText("UI_QPSR_Initiate"), getText("UI_QPSR_Recognized"), getText("UI_QPSR_Proven"), getText("UI_QPSR_Trusted"),
        getText("UI_QPSR_Respected"), getText("UI_QPSR_Veteran"), getText("UI_QPSR_Elite"), getText("UI_QPSR_Renowned"), getText("UI_QPSR_Legendary"), getText("UI_QPSR_Icon")
    }
}
