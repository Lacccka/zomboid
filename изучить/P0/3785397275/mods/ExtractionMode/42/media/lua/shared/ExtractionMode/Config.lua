require "ExtractionMode/Localization"

ExtractionMode = ExtractionMode or {}

local Config = {
    VERSION = 63,
    DATA_KEY = "ExtractionMode.State",
    BACKUP_DATA_KEY = "ExtractionMode.ProgressionBackup",
    GARAGE_BACKUP_DATA_KEY = "ExtractionMode.GarageBackup",
    PLAYER_BACKUP_KEY = "ExtractionModeProgressionBackup",
    COOP_WELCOME_VERSION = 1,
    COMMAND_MODULE = "ExtractionMode",
    FLARE_FULL_TYPE = "ExtractionMode.ExtractionFlareGun",
    FLARE_NORMAL_SOUND_RADIUS = 50,
    EXTRACTION_PROXIMITY_REVEAL_DISTANCE = 50,
    HELICOPTER_ACTIVE_APPROACH_SECONDS = 30,
    CAMPAIGN_HELICOPTER_ACTIVE_APPROACH_SECONDS = 60,
    CAMPAIGN_HANDOFF_SECONDS = 180,
    CAMPAIGN_HANDOFF_POINT = { x = 13617, y = 1344, z = 5, radius = 4 },
    CAMPAIGN_ZOMBIE_ATTRACTION_RADIUS = 150,
    CAMPAIGN_ZOMBIE_REPATH_INTERVAL_MS = 5000,
    CAMPAIGN_ROOF_CLEAR_RADIUS = 85,
    CAMPAIGN_FADE_TO_BLACK_MS = 5000,
    CAMPAIGN_EPILOGUE_AFTER_BLACK_MS = 25000,
    TAMED_ANIMAL_EXTRACTION_RADIUS = 50,
    GROUND_EXTRACTION_SECONDS = 5,
    INFECTION_CURE_TYPE = "ExtractionMode.InfectionCure",
    HIDEOUT_CELL_X = 90,
    HIDEOUT_CELL_Y = 75,
    MAP_CELL_SIZE = 256,
    -- The authored garage reaches about sixteen tiles from HideoutX/Y. Existing
    -- saves may still contain the old radius of 14, so enforce this minimum at
    -- runtime as well as raising the sandbox default.
    HIDEOUT_MINIMUM_RADIUS = 20,
    -- The hideout occupies its own map cell. Cover that complete cell so map
    -- revisions, annexes, and rooms separated into another IsoBuilding cannot
    -- fall outside the pseudo-generator's electrical range.
    HIDEOUT_POWER_RADIUS = 256,
    -- One authored garage bay is reserved for the personal vehicle prototype.
    -- Keep this relative to HideoutX/Y so servers that relocate the hideout do
    -- not leave the vehicle behind at the bundled map's absolute coordinates.
    -- Deployment point is two tiles northwest of authored garage marker
    -- (139,140) in cell (90,75): world (23177,19338).
    HIDEOUT_VEHICLE_OFFSET_X = 14,
    HIDEOUT_VEHICLE_OFFSET_Y = -2,
    -- Vehicle footprint clearance: authored cell tiles (136,136)-(138,140),
    -- inclusive. Players in this rectangle move to authored tile (134,137).
    HIDEOUT_VEHICLE_UNSAFE_MIN_OFFSET_X = 13,
    HIDEOUT_VEHICLE_UNSAFE_MIN_OFFSET_Y = -4,
    HIDEOUT_VEHICLE_UNSAFE_MAX_OFFSET_X = 15,
    HIDEOUT_VEHICLE_UNSAFE_MAX_OFFSET_Y = 0,
    HIDEOUT_VEHICLE_CLEAR_OFFSET_X = 11,
    HIDEOUT_VEHICLE_CLEAR_OFFSET_Y = -3,
    -- Garage control floor tile (134,135) in the authored hideout cell:
    -- world (23174,19335) with the bundled hideout coordinates.
    HIDEOUT_GARAGE_CONTROL_OFFSET_X = 11,
    HIDEOUT_GARAGE_CONTROL_OFFSET_Y = -5,
    -- Authored garage sentinel at cell tile (134,134). The progression door
    -- must not be installed unless this map object is present, which prevents
    -- the door from appearing by itself in saves generated before the annex.
    HIDEOUT_GARAGE_MARKER_OFFSET_X = 11,
    HIDEOUT_GARAGE_MARKER_OFFSET_Y = -6,
    HIDEOUT_GARAGE_MARKER_SPRITE = "security_01_0",
    -- The garage access door spans cell tiles (131,137) and (132,137).
    -- IsoDoor is owned by the east tile (132,137): world (23172,19337)
    -- for the bundled hideout map.
    HIDEOUT_GARAGE_DOOR_OFFSET_X = 9,
    HIDEOUT_GARAGE_DOOR_OFFSET_Y = -3,
    -- The requested visible open-door tile is fixtures_doors_01_55. The
    -- closed sprite is only used while constructing the IsoDoor; the authority
    -- immediately assigns the requested open sprite and opens a new door.
    HIDEOUT_GARAGE_DOOR_CLOSED_SPRITE = "fixtures_doors_01_52",
    -- The garage entrance occupies a west-facing edge. Sprite 55 is the
    -- north-facing open variant and renders closed-looking and offset here.
    HIDEOUT_GARAGE_DOOR_OPEN_SPRITE = "fixtures_doors_01_54",
    -- This edge separates (131,137) from (132,137), so it is the west edge of
    -- the owning (132,137) square rather than its north edge.
    HIDEOUT_GARAGE_DOOR_NORTH = false,
    -- Protected garage work area: authored cell tiles (134,134)-(139,142),
    -- inclusive. Offsets keep the area aligned if HideoutX/Y is relocated.
    HIDEOUT_GARAGE_PROTECTED_MIN_OFFSET_X = 11,
    HIDEOUT_GARAGE_PROTECTED_MIN_OFFSET_Y = -6,
    HIDEOUT_GARAGE_PROTECTED_MAX_OFFSET_X = 16,
    HIDEOUT_GARAGE_PROTECTED_MAX_OFFSET_Y = 2,
    -- Vehicle heading is rotation around PZ's vertical Y axis. Zero faces
    -- world +Y, which appears toward the bottom-left of the isometric screen.
    HIDEOUT_VEHICLE_ANGLE_Y = 0,
    STATE_HIDEOUT = "HIDEOUT",
    STATE_COUNTDOWN = "COUNTDOWN",
    STATE_TRANSIT = "TRANSIT",
    STATE_RAID = "RAID",
    STATE_EXTRACTING = "EXTRACTING",
    STATE_BOARDING = "BOARDING",
}
local Localization = ExtractionMode.Localization

-- BaseVehicle:setAngles consumes degrees. These names describe the direction
-- on PZ's isometric screen rather than cardinal world directions.
Config.VEHICLE_HEADING = {
    DOWN_LEFT = 0,
    DOWN_RIGHT = 90,
    UP_RIGHT = 180,
    UP_LEFT = -90,
}

function Config.hideoutCellBounds()
    local minimumX = Config.HIDEOUT_CELL_X * Config.MAP_CELL_SIZE
    local minimumY = Config.HIDEOUT_CELL_Y * Config.MAP_CELL_SIZE
    return {
        minX = minimumX,
        minY = minimumY,
        maxXExclusive = minimumX + Config.MAP_CELL_SIZE,
        maxYExclusive = minimumY + Config.MAP_CELL_SIZE,
    }
end

local defaults = {
    HideoutX = 23163, HideoutY = 19340, HideoutZ = 0, HideoutRadius = 20,
    HideoutInfectionCapPercent = 50,
    HideoutClutterCount = 50,
    HideoutCandleCount = 12,
    SkillBooksGrantXP = true,
    RequireUpgradeSkills = true,
    GrantStarterM9Kit = true,
    GrantStarterNightstick = true,
    GeneratorTankCapacityLiters = 40,
    GeneratorFuelPerDay = 4,
    GeneratorEmptyHideoutMultiplier = 0.25,
    GeneratorTransferLiters = 5,
    RaidSpawnX = 10620, RaidSpawnY = 9280, RaidSpawnZ = 0,
    Extraction1X = 10668, Extraction1Y = 9312,
    Extraction2X = 10852, Extraction2Y = 9464,
    Extraction3X = 10752, Extraction3Y = 9576,
    ExtractionRadius = 12,
    ReadyCountdownSeconds = 10,
    VehicleRaidBaseFuelLiters = 4,
    HideoutVehicleInactivityMinutes = 5,
    HotwireExtractedVehicles = false,
    ExtractionHealing = 1,
    RaidDeathHandling = 1,
    HordeDelayMinimumHours = 8,
    HordeDelayMaximumHours = 11,
    HordeSize = 100,
    HordeSpawnRadius = 38,
    AmbientSprinterPercentWeek2 = 1,
    HordeSprinterPercent = 5,
    HelicopterArrivalSeconds = 90,
    CommArrayHelicopterArrivalSeconds = 60,
    EasierExtractions = false,
    ExtractionHordePerPlayer = 5,
    ExtractionHordeSpawnRadius = 35,
    EnableBanditsIntegration = true,
    SuppressBanditsAutonomousRaids = true,
    BanditAttackWindowChancePercent = 15,
    BanditAttackWindowMinimumHours = 1,
    BanditAttackWindowMaximumHours = 2,
    BanditGroupMinimum = 2,
    BanditGroupMaximum = 5,
    BanditSpawnRadius = 55,
    BanditHordeReplacementChancePercent = 20,
    BanditHordeGroupMinimum = 8,
    BanditHordeGroupMaximum = 12,
    BoardingWindowSeconds = 30,
    BoardingInteractionRadius = 3,
    InsertionClearRadius = 35,
    IntelInsertionClearBonus = 20,
    IntelHordeDelayBonusHours = 3,
    BaseDailyTownChoices = 3,
    CommArrayDailyTownChoices = 5,
    VentilationSleepBonusPercent = 20,
    HideoutReadingDurationReductionPercent = 25,
    MedicalHealingBonusPercent = 20,
    MedicalInfectionPreventionPercent = 20,
    HideoutHeatingRadius = 30,
    HideoutHeatingTemperature = 35,
    RoutePointMinimumDistance = 120,
    RoutePointClearanceRadius = 6,
    TransitFadeSeconds = 1,
    DebugLogging = false,
}

local towns = {
    louisville = {
        key = "louisville", name = "Louisville", size = "Major City",
        raidBounds = { minX = 11600, minY = 900, maxX = 14450, maxY = 3860 },
        -- Louisville is large enough that unrestricted random route points can put
        -- insertion and extraction on opposite sides of the city.
        maximumRouteDistance = 1250,
        points = {
            -- Build 42.20 outdoor roads, lots, and neighborhood approaches. The
            -- former northern anchors were in the Ohio River, while several of
            -- the old southern/eastern anchors were isolated in woods or fields.
            { x = 12070, y = 2588, z = 0 }, { x = 12298, y = 1801, z = 0 },
            { x = 13090, y = 1390, z = 0 }, { x = 13520, y = 1494, z = 0 },
            { x = 13602, y = 1904, z = 0 }, { x = 13929, y = 2658, z = 0 },
            { x = 13977, y = 3150, z = 0 }, { x = 13420, y = 3198, z = 0 },
            { x = 12840, y = 3313, z = 0 }, { x = 12400, y = 3000, z = 0 },
            { x = 12605, y = 2407, z = 0 }, { x = 13234, y = 2676, z = 0 },
        },
        vehicleInsertionPoints = {
            { x = 12512, y = 3845, z = 0, angleY = Config.VEHICLE_HEADING.UP_RIGHT },
            { x = 14182, y = 3444, z = 0, angleY = Config.VEHICLE_HEADING.UP_LEFT },
            { x = 13651, y = 3713, z = 0, angleY = Config.VEHICLE_HEADING.UP_RIGHT },
        },
    },
    muldraugh = {
        key = "muldraugh", name = "Muldraugh", size = "Main Town",
        raidBounds = { minX = 10100, minY = 8800, maxX = 11500, maxY = 11100 },
        maximumRouteDistance = 850,
        points = {
            -- Build 42.20 road and parking-lot anchors. Keep these on explicit
            -- blends_street tiles so players never insert into grass or fields.
            { x = 10620, y = 9280, z = 0 }, { x = 11006, y = 9397, z = 0 },
            { x = 10926, y = 9814, z = 0 }, { x = 10751, y = 10613, z = 0 },
            { x = 10594, y = 10496, z = 0 }, { x = 10594, y = 9767, z = 0 },
            { x = 10817, y = 9461, z = 0 }, { x = 10750, y = 10189, z = 0 },
        },
        vehicleInsertionPoints = {
            { x = 10200, y = 9789, z = 0, angleY = Config.VEHICLE_HEADING.DOWN_RIGHT },
            { x = 10591, y = 8935, z = 0, angleY = Config.VEHICLE_HEADING.DOWN_LEFT },
            { x = 10591, y = 10950, z = 0, angleY = Config.VEHICLE_HEADING.UP_RIGHT },
        },
    },
    riverside = {
        key = "riverside", name = "Riverside", size = "Main Town",
        raidBounds = { minX = 5250, minY = 4800, maxX = 7320, maxY = 6200 },
        maximumRouteDistance = 500,
        points = {
            -- The river occupies much of Riverside's northern edge. These three
            -- anchors use known outdoor parking areas instead of the old water tiles.
            { x = 6096, y = 5307, z = 0 }, { x = 6463, y = 5287, z = 0 },
            { x = 6814, y = 5273, z = 0 }, { x = 6840, y = 5540, z = 0 },
            { x = 6480, y = 5630, z = 0 }, { x = 6094, y = 5483, z = 0 },
            { x = 6300, y = 5450, z = 0 }, { x = 6600, y = 5450, z = 0 },
        },
        vehicleInsertionPoints = {
            { x = 7299, y = 5699, z = 0, angleY = Config.VEHICLE_HEADING.UP_RIGHT },
            { x = 6771, y = 5631, z = 0, angleY = Config.VEHICLE_HEADING.UP_LEFT },
            { x = 5300, y = 5836, z = 0, angleY = Config.VEHICLE_HEADING.DOWN_RIGHT },
        },
    },
    rosewood = {
        key = "rosewood", name = "Rosewood", size = "Main Town",
        raidBounds = { minX = 7500, minY = 10700, maxX = 8900, maxY = 12500 },
        maximumRouteDistance = 550,
        points = {
            -- Build 42.20 road and parking-lot anchors. The previous outer
            -- anchors included grass, open ground, and a wooded natural tile.
            { x = 8192, y = 11208, z = 0 }, { x = 8449, y = 11422, z = 0 },
            { x = 8453, y = 11644, z = 0 }, { x = 8448, y = 12050, z = 0 },
            { x = 8025, y = 11890, z = 0 }, { x = 7913, y = 11386, z = 0 },
            { x = 8241, y = 11470, z = 0 }, { x = 8180, y = 11776, z = 0 },
        },
        -- Dedicated road approaches used only when a raid inserts a vehicle.
        -- Each heading points the automatic rolling arrival along the road.
        vehicleInsertionPoints = {
            { x = 7800, y = 11205, z = 0, angleY = Config.VEHICLE_HEADING.DOWN_RIGHT },
            { x = 8100, y = 10800, z = 0, angleY = Config.VEHICLE_HEADING.DOWN_LEFT },
            { x = 8700, y = 11200, z = 0, angleY = Config.VEHICLE_HEADING.UP_LEFT },
            { x = 8325, y = 11980, z = 0, angleY = Config.VEHICLE_HEADING.UP_LEFT },
            { x = 8032, y = 11982, z = 0, angleY = Config.VEHICLE_HEADING.DOWN_RIGHT },
        },
    },
    west_point = {
        key = "west_point", name = "West Point", size = "Main Town",
        raidBounds = { minX = 10800, minY = 6100, maxX = 12800, maxY = 7900 },
        maximumRouteDistance = 600,
        points = {
            { x = 11540, y = 6740, z = 0 }, { x = 11844, y = 6655, z = 0 },
            { x = 12322, y = 6957, z = 0 }, { x = 12047, y = 7382, z = 0 },
            { x = 11685, y = 7122, z = 0 }, { x = 11287, y = 6954, z = 0 },
            { x = 11769, y = 6921, z = 0 }, { x = 12007, y = 7171, z = 0 },
        },
        -- Provisional Build 42.20 road-edge points pending an in-game audit.
        vehicleInsertionPoints = {
            { x = 10800, y = 7043, z = 0, angleY = Config.VEHICLE_HEADING.DOWN_RIGHT },
            { x = 12519, y = 6628, z = 0, angleY = Config.VEHICLE_HEADING.UP_LEFT },
            { x = 12480, y = 7216, z = 0, angleY = Config.VEHICLE_HEADING.UP_LEFT },
        },
    },
    march_ridge = {
        key = "march_ridge", name = "March Ridge", size = "Large Town",
        raidBounds = { minX = 9300, minY = 11900, maxX = 11000, maxY = 13650 },
        maximumRouteDistance = 500,
        points = {
            { x = 9996, y = 12690, z = 0 }, { x = 10360, y = 12436, z = 0 },
            { x = 10509, y = 12802, z = 0 }, { x = 10339, y = 12833, z = 0 },
            { x = 9969, y = 13143, z = 0 }, { x = 9800, y = 12851, z = 0 },
            { x = 10151, y = 12675, z = 0 }, { x = 10151, y = 12829, z = 0 },
        },
        -- Provisional audited street anchors pending an in-game vehicle pass.
        vehicleInsertionPoints = {
            { x = 9836, y = 12326, z = 0, angleY = Config.VEHICLE_HEADING.DOWN_RIGHT },
            { x = 10591, y = 12684, z = 0, angleY = Config.VEHICLE_HEADING.UP_RIGHT },
            { x = 10590, y = 12132, z = 0, angleY = Config.VEHICLE_HEADING.DOWN_LEFT },
        },
    },
    brandenburg = {
        key = "brandenburg", name = "Brandenburg", size = "Small City",
        raidBounds = { minX = 1000, minY = 5250, maxX = 3350, maxY = 7100 },
        points = {
            -- Build 42.20 street anchors. The former northern, eastern, southern,
            -- and western points were between 47 and 78 tiles from a mapped road.
            { x = 1547, y = 5904, z = 0 }, { x = 2162, y = 5842, z = 0 },
            { x = 2840, y = 5855, z = 0 }, { x = 2741, y = 6354, z = 0 },
            { x = 2300, y = 6491, z = 0 }, { x = 1794, y = 6398, z = 0 },
        },
        -- Provisional audited street anchors pending an in-game vehicle pass.
        vehicleInsertionPoints = {
            { x = 1138, y = 6249, z = 0, angleY = Config.VEHICLE_HEADING.UP_RIGHT },
            { x = 1974, y = 6948, z = 0, angleY = Config.VEHICLE_HEADING.UP_RIGHT },
            { x = 2916, y = 6149, z = 0, angleY = Config.VEHICLE_HEADING.UP_LEFT },
        },
    },
    ekron = {
        key = "ekron", name = "Ekron", size = "Small Town",
        raidBounds = { minX = 50, minY = 8800, maxX = 1330, maxY = 10550 },
        points = {
            { x = 537, y = 9484, z = 0 }, { x = 590, y = 9310, z = 0 },
            { x = 840, y = 9490, z = 0 }, { x = 820, y = 9830, z = 0 },
            { x = 510, y = 9880, z = 0 }, { x = 250, y = 9720, z = 0 },
        },
        -- Provisional Build 42.20 road-edge points pending an in-game audit.
        vehicleInsertionPoints = {
            { x = 70, y = 9895, z = 0, angleY = Config.VEHICLE_HEADING.DOWN_RIGHT },
            { x = 1317, y = 9895, z = 0, angleY = Config.VEHICLE_HEADING.UP_LEFT },
            { x = 538, y = 9295, z = 0, angleY = Config.VEHICLE_HEADING.DOWN_LEFT },
        },
    },
    fallas_lake = {
        key = "fallas_lake", name = "Fallas Lake", size = "Small Town",
        raidBounds = { minX = 6500, minY = 7650, maxX = 8000, maxY = 9100 },
        points = {
            { x = 7055, y = 8207, z = 0 }, { x = 7425, y = 8152, z = 0 },
            { x = 7444, y = 8299, z = 0 }, { x = 7440, y = 8560, z = 0 },
            { x = 7121, y = 8557, z = 0 }, { x = 7018, y = 8457, z = 0 },
        },
        -- Provisional Build 42.20 mapped-road points pending an in-game audit.
        vehicleInsertionPoints = {
            { x = 6673, y = 8154, z = 0, angleY = Config.VEHICLE_HEADING.DOWN_RIGHT },
            { x = 7003, y = 7673, z = 0, angleY = Config.VEHICLE_HEADING.DOWN_LEFT },
            { x = 7931, y = 8562, z = 0, angleY = Config.VEHICLE_HEADING.UP_LEFT },
        },
    },
    irvington = {
        key = "irvington", name = "Irvington", size = "Large Town",
        raidBounds = { minX = 1400, minY = 13350, maxX = 3900, maxY = 15050 },
        points = {
            -- Build 42.20 paved road approaches around the town center.
            { x = 2010, y = 14028, z = 0 }, { x = 2217, y = 13872, z = 0 },
            { x = 2816, y = 13916, z = 0 }, { x = 2861, y = 14516, z = 0 },
            { x = 2351, y = 14538, z = 0 }, { x = 1925, y = 14402, z = 0 },
        },
        -- Provisional Build 42.20 road-edge points pending an in-game audit.
        vehicleInsertionPoints = {
            { x = 2008, y = 13463, z = 0, angleY = Config.VEHICLE_HEADING.DOWN_LEFT },
            { x = 3709, y = 14500, z = 0, angleY = Config.VEHICLE_HEADING.UP_RIGHT },
            { x = 1424, y = 14837, z = 0, angleY = Config.VEHICLE_HEADING.DOWN_LEFT },
        },
    },
    knox_penitentiary = {
        key = "knox_penitentiary", name = "Knox Penitentiary", size = "Secure Facility",
        raidBounds = { minX = 7000, minY = 11200, maxX = 8200, maxY = 12500 },
        unlockQuestId = "court_with_death",
        maximumRouteDistance = 500,
        points = {
            -- Four outdoor approaches around the prison complex.
            { x = 7721, y = 11810, z = 0 }, { x = 7728, y = 11939, z = 0 },
            { x = 7468, y = 11915, z = 0 }, { x = 7493, y = 11800, z = 0 },
        },
        -- Knox Penitentiary intentionally has one vehicle insertion route.
        vehicleInsertionPoints = {
            { x = 7895, y = 11886, z = 0, angleY = Config.VEHICLE_HEADING.UP_LEFT },
        },
    },
    louisville_checkpoint = {
        key = "louisville_checkpoint", name = "Louisville Checkpoint", size = "Military Checkpoint",
        raidBounds = { minX = 12000, minY = 3900, maxX = 13000, maxY = 4850 },
        unlockQuestId = "evacuation_triage",
        minimumExtractionSites = 1,
        maximumExtractionSites = 1,
        points = {
            -- This narrow destination intentionally uses one approach on each
            -- side of the checkpoint: one insertion and one extraction.
            { x = 12522, y = 4051, z = 0 }, { x = 12513, y = 4523, z = 0 },
        },
        -- Provisional approaches from either side of the checkpoint.
        vehicleInsertionPoints = {
            { x = 12510, y = 3913, z = 0, angleY = Config.VEHICLE_HEADING.DOWN_LEFT },
            { x = 12514, y = 4725, z = 0, angleY = Config.VEHICLE_HEADING.UP_RIGHT },
        },
    },
    grand_ohio_mall = {
        key = "grand_ohio_mall", name = "Grand Ohio Mall", size = "Shopping Mall",
        raidBounds = { minX = 12900, minY = 700, maxX = 14300, maxY = 2000 },
        unlockQuestId = "the_grand_prize",
        maximumRouteDistance = 500,
        points = {
            -- Four outdoor approaches in the mall's surrounding roads and lots.
            { x = 13520, y = 1494, z = 0 }, { x = 13392, y = 1340, z = 0 },
            { x = 13572, y = 1168, z = 0 }, { x = 13810, y = 1350, z = 0 },
        },
        -- Provisional mall road and parking-lot approaches pending an in-game audit.
        vehicleInsertionPoints = {
            { x = 13391, y = 1386, z = 0, angleY = Config.VEHICLE_HEADING.DOWN_RIGHT },
            { x = 13644, y = 1665, z = 0, angleY = Config.VEHICLE_HEADING.UP_RIGHT },
        },
    },
}

local townOrder = {
    "louisville", "muldraugh", "riverside", "rosewood", "west_point",
    "march_ridge", "brandenburg", "ekron", "fallas_lake", "irvington",
    "knox_penitentiary", "louisville_checkpoint", "grand_ohio_mall",
}

function Config.town(key)
    return towns[tostring(key or "")]
end

function Config.raidBounds(key)
    local town = Config.town(key)
    return town and town.raidBounds or nil
end

function Config.pointInsideRaidBounds(key, point)
    local bounds = Config.raidBounds(key)
    local x = point and tonumber(point.x)
    local y = point and tonumber(point.y)
    return bounds ~= nil and x ~= nil and y ~= nil
        and x >= bounds.minX and x <= bounds.maxX
        and y >= bounds.minY and y <= bounds.maxY
end

function Config.townDisplayName(key, fallback)
    local town = Config.town(key)
    return Localization.get("IGUI_ExtractionMode_Town_" .. tostring(key or ""),
        fallback or (town and town.name) or tostring(key or ""))
end

function Config.townSize(key, fallback)
    local town = Config.town(key)
    return Localization.get("IGUI_ExtractionMode_TownSize_" .. tostring(key or ""),
        fallback or (town and town.size) or "Town")
end

function Config.townKeys()
    local result = {}
    for _, key in ipairs(townOrder) do result[#result + 1] = key end
    return result
end

function Config.townSummaries(keys)
    local result = {}
    for _, key in ipairs(keys or townOrder) do
        local town = towns[key]
        if town then result[#result + 1] = { key = town.key, name = town.name, size = town.size } end
    end
    return result
end

local legacyPoints = {
    Hideout = { x = 10770, y = 10271 },
    RaidSpawn = { x = 10695, y = 9383 },
    Extraction1 = { x = 10654, y = 9371 },
    Extraction2 = { x = 10820, y = 9419 },
    Extraction3 = { x = 10715, y = 9532 },
}

function Config.value(key)
    local section = SandboxVars and SandboxVars.ExtractionMode
    local value = section and section[key]
    if value == nil then return defaults[key] end
    return value
end

-- Keep the shot audible to players while controlling only the world-sound
-- radius used by the zombie hearing system. Apply this on both authority and
-- owning client because firearm attacks can be simulated on either side.
function Config.applyExtractionFlareNoise(item)
    if item == nil then return false end
    local isFlare = false
    pcall(function() isFlare = item:getFullType() == Config.FLARE_FULL_TYPE end)
    if not isFlare then return false end
    local radius = Config.value("EasierExtractions") == true
        and 0 or Config.FLARE_NORMAL_SOUND_RADIUS
    return pcall(function() item:setSoundRadius(radius) end)
end

function Config.point(prefix)
    return {
        x = tonumber(Config.value(prefix .. "X")) or 0,
        y = tonumber(Config.value(prefix .. "Y")) or 0,
        z = tonumber(Config.value(prefix .. "Z")) or 0,
    }
end

function Config.hideout()
    local point = Config.point("Hideout")
    local legacy = legacyPoints.Hideout
    if point.x == legacy.x and point.y == legacy.y then
        point.x, point.y = defaults.HideoutX, defaults.HideoutY
    end
    point.radius = math.max(Config.HIDEOUT_MINIMUM_RADIUS,
        tonumber(Config.value("HideoutRadius")) or defaults.HideoutRadius)
    return point
end

function Config.hideoutVehicleSpawn()
    local hideout = Config.hideout()
    return {
        x = hideout.x + Config.HIDEOUT_VEHICLE_OFFSET_X,
        y = hideout.y + Config.HIDEOUT_VEHICLE_OFFSET_Y,
        z = hideout.z,
        angleY = Config.HIDEOUT_VEHICLE_ANGLE_Y,
    }
end

function Config.hideoutVehicleUnsafeBounds()
    local hideout = Config.hideout()
    return {
        minX = math.floor(hideout.x + Config.HIDEOUT_VEHICLE_UNSAFE_MIN_OFFSET_X),
        minY = math.floor(hideout.y + Config.HIDEOUT_VEHICLE_UNSAFE_MIN_OFFSET_Y),
        maxX = math.floor(hideout.x + Config.HIDEOUT_VEHICLE_UNSAFE_MAX_OFFSET_X),
        maxY = math.floor(hideout.y + Config.HIDEOUT_VEHICLE_UNSAFE_MAX_OFFSET_Y),
        z = math.floor(tonumber(hideout.z) or 0),
    }
end

function Config.hideoutVehiclePlayerClearPoint()
    local hideout = Config.hideout()
    return {
        x = math.floor(hideout.x + Config.HIDEOUT_VEHICLE_CLEAR_OFFSET_X),
        y = math.floor(hideout.y + Config.HIDEOUT_VEHICLE_CLEAR_OFFSET_Y),
        z = math.floor(tonumber(hideout.z) or 0),
    }
end

function Config.hideoutGarageControl()
    local hideout = Config.hideout()
    return {
        x = hideout.x + Config.HIDEOUT_GARAGE_CONTROL_OFFSET_X,
        y = hideout.y + Config.HIDEOUT_GARAGE_CONTROL_OFFSET_Y,
        z = hideout.z,
    }
end

function Config.hideoutGarageMarker()
    local hideout = Config.hideout()
    return {
        x = hideout.x + Config.HIDEOUT_GARAGE_MARKER_OFFSET_X,
        y = hideout.y + Config.HIDEOUT_GARAGE_MARKER_OFFSET_Y,
        z = hideout.z,
    }
end

function Config.hideoutGarageDoor()
    local hideout = Config.hideout()
    return {
        x = hideout.x + Config.HIDEOUT_GARAGE_DOOR_OFFSET_X,
        y = hideout.y + Config.HIDEOUT_GARAGE_DOOR_OFFSET_Y,
        z = hideout.z,
    }
end

function Config.isHideoutGarageDoorSquare(square)
    if square == nil then return false end
    local point = Config.hideoutGarageDoor()
    return square:getX() == math.floor(point.x)
        and square:getY() == math.floor(point.y)
        and square:getZ() == math.floor(point.z)
end

function Config.hideoutGarageProtectedBounds()
    local hideout = Config.hideout()
    return {
        minX = math.floor(hideout.x + Config.HIDEOUT_GARAGE_PROTECTED_MIN_OFFSET_X),
        minY = math.floor(hideout.y + Config.HIDEOUT_GARAGE_PROTECTED_MIN_OFFSET_Y),
        maxX = math.floor(hideout.x + Config.HIDEOUT_GARAGE_PROTECTED_MAX_OFFSET_X),
        maxY = math.floor(hideout.y + Config.HIDEOUT_GARAGE_PROTECTED_MAX_OFFSET_Y),
        z = math.floor(tonumber(hideout.z) or 0),
    }
end

function Config.isHideoutGarageProtectedSquare(square)
    if square == nil then return false end
    local bounds = Config.hideoutGarageProtectedBounds()
    return square:getZ() == bounds.z
        and square:getX() >= bounds.minX and square:getX() <= bounds.maxX
        and square:getY() >= bounds.minY and square:getY() <= bounds.maxY
end

function Config.raidSpawn()
    local point = Config.point("RaidSpawn")
    local legacy = legacyPoints.RaidSpawn
    if point.x == legacy.x and point.y == legacy.y then
        point.x, point.y = defaults.RaidSpawnX, defaults.RaidSpawnY
    end
    return point
end

function Config.extractionSites()
    local z = tonumber(Config.value("RaidSpawnZ")) or 0
    local radius = tonumber(Config.value("ExtractionRadius")) or defaults.ExtractionRadius
    local sites = {}
    for index = 1, 3 do
        local prefix = "Extraction" .. index
        local x = tonumber(Config.value(prefix .. "X")) or 0
        local y = tonumber(Config.value(prefix .. "Y")) or 0
        local legacy = legacyPoints[prefix]
        if legacy and x == legacy.x and y == legacy.y then
            x, y = defaults[prefix .. "X"], defaults[prefix .. "Y"]
        end
        sites[index] = {
            id = index,
            x = x,
            y = y,
            z = z,
            radius = radius,
        }
    end
    return sites
end

ExtractionMode.Config = Config
return Config
