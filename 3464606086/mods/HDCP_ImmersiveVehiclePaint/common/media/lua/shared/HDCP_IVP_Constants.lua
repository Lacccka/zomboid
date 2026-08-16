HDCP_IVP_Constants = HDCP_IVP_Constants or {}
HDCP_IVP_Constants.MOD_ID = "HDCP ImmersiveVehiclePaint"
HDCP_IVP_Constants.MOD_VERSION = "2.1.0"
HDCP_IVP_Constants.SERVER_DEBUG = true
-- must match LearnedRecipes in the magazine script and the Recipes translation key
HDCP_IVP_Constants.RECIPE = "AutomotivePainting"
HDCP_IVP_Constants.SPRAY_DELTA = 0.125
HDCP_IVP_Constants.BODYWORK_REQUIREMENT = 60
HDCP_IVP_Constants.SOUND = {
    RADIUS = 60,
    VOLUME = 100
}
HDCP_IVP_Constants.TASK_DURATION = {
    SAND  = { MIN = 13, MAX = 15 },
    PRIME = { MIN = 08, MAX = 10 },
    PAINT = { MIN = 10, MAX = 12 },
}
HDCP_IVP_Constants.ALLOWED_VEHICLES = {
    -- Standard
    { id = "Base.CarNormal" },
    { id = "Base.CarStationWagon" },
    { id = "Base.CarStationWagon2" },
    { id = "Base.OffRoad" },
    { id = "Base.SmallCar" },
    { id = "Base.SmallCar02" },
    -- Heavy-Duty
    { id = "Base.PickUpTruck" },
    { id = "Base.PickUpVan" },
    { id = "Base.StepVan" },
    { id = "Base.SUV" },
    { id = "Base.Van" },
    { id = "Base.VanSeats" },
    -- Sports
    { id = "Base.CarLuxury" },
    { id = "Base.ModernCar" },
    { id = "Base.ModernCar02" },
    { id = "Base.SportsCar" }
}
HDCP_IVP_Constants.VEHICLE_SURFACES = {
    "Engine",
    "TireFrontLeft",
    "DoorFrontLeft",
    "DoorMiddleLeft",
    "DoorRearLeft",
    "TireRearLeft",
    "TruckBed",
    "TireRearRight",
    "DoorRearRight",
    "DoorMiddleRight",
    "DoorFrontRight",
    "TireFrontRight",
}
HDCP_IVP_Constants.ITEMS = {
    MAGAZINE                = "ImmersiveVehiclePaint.AutomotivePaintingMag",
    SANDING_BLOCK           = "ImmersiveVehiclePaint.SandingBlock",
    AUTOMOTIVE_PRIMER_SPRAY = "ImmersiveVehiclePaint.AutomotivePrimerSpray",
    EMPTY_PAINT_CAN         = "ImmersiveVehiclePaint.EmptyAutomotivePaintSprayCan",
    EMPTY_PRIMER_CAN        = "ImmersiveVehiclePaint.EmptyAutomotivePrimerSprayCan",
    -- kept defined so boxes sitting in existing saves still load and open; it no
    -- longer spawns, the colour-themed boxes below replaced it in the loot tables
    SPRAY_PAINT_BOX         = "ImmersiveVehiclePaint.BoxOfAutomotiveSprayPaint",
    SPRAY_PAINT_BOX_WARM    = "ImmersiveVehiclePaint.BoxOfWarmAutomotiveSprayPaint",
    SPRAY_PAINT_BOX_COOL    = "ImmersiveVehiclePaint.BoxOfCoolAutomotiveSprayPaint",
    SPRAY_PAINT_BOX_NEUTRAL = "ImmersiveVehiclePaint.BoxOfNeutralAutomotiveSprayPaint",
    USED_SPRAY_PAINT_DUMMY  = "ImmersiveVehiclePaint.UsedAutomotiveSprayPaintDummy",
    USED_SPRAY_PRIMER_DUMMY = "ImmersiveVehiclePaint.UsedAutomotivePrimerSprayDummy",
    NEW_SPRAY_PAINT_DUMMY   = "ImmersiveVehiclePaint.NewAutomotiveSprayPaintDummy",
    NEW_SPRAY_PRIMER_DUMMY  = "ImmersiveVehiclePaint.NewAutomotivePrimerSprayDummy",
    SPRAY_PAINT             = {
        {
            type = "ImmersiveVehiclePaint.WhiteAutomotiveSprayPaint",
            group = "neutral",
            color = { h = 0, s = 0, v = 100 }
        },
        {
            type = "ImmersiveVehiclePaint.PearlWhiteAutomotiveSprayPaint",
            group = "neutral",
            color = { h = 45, s = 36, v = 96 }
        },
        {
            type = "ImmersiveVehiclePaint.SilverAutomotiveSprayPaint",
            group = "neutral",
            color = { h = 214, s = 8, v = 83 }
        },
        {
            type = "ImmersiveVehiclePaint.GraphiteAutomotiveSprayPaint",
            group = "neutral",
            color = { h = 235, s = 48, v = 30 }
        },
        {
            type = "ImmersiveVehiclePaint.BlackAutomotiveSprayPaint",
            group = "neutral",
            color = { h = 0, s = 0, v = 10 }
        },
        {
            type = "ImmersiveVehiclePaint.MustardAutomotiveSprayPaint",
            group = "warm",
            color = { h = 40, s = 95, v = 89 }
        },
        {
            type = "ImmersiveVehiclePaint.TangerineAutomotiveSprayPaint",
            group = "warm",
            color = { h = 25, s = 100, v = 89 }
        },
        {
            type = "ImmersiveVehiclePaint.CherryAutomotiveSprayPaint",
            group = "warm",
            color = { h = 0, s = 100, v = 72 }
        },
        {
            type = "ImmersiveVehiclePaint.BurgundyAutomotiveSprayPaint",
            group = "warm",
            color = { h = 351, s = 100, v = 48 }
        },
        {
            type = "ImmersiveVehiclePaint.HotPinkAutomotiveSprayPaint",
            group = "warm",
            color = { h = 330, s = 94, v = 98 }
        },
        {
            type = "ImmersiveVehiclePaint.AmethystAutomotiveSprayPaint",
            group = "cool",
            color = { h = 270, s = 88, v = 89 }
        },
        {
            type = "ImmersiveVehiclePaint.VioletAutomotiveSprayPaint",
            group = "cool",
            color = { h = 274, s = 98, v = 66 }
        },
        {
            type = "ImmersiveVehiclePaint.SapphireAutomotiveSprayPaint",
            group = "cool",
            color = { h = 216, s = 99, v = 73 }
        },
        {
            type = "ImmersiveVehiclePaint.SkyBlueAutomotiveSprayPaint",
            group = "cool",
            color = { h = 200, s = 92, v = 88 }
        },
        {
            type = "ImmersiveVehiclePaint.LimeGreenAutomotiveSprayPaint",
            group = "cool",
            color = { h = 120, s = 100, v = 100 }
        },
        {
            type = "ImmersiveVehiclePaint.HunterGreenAutomotiveSprayPaint",
            group = "cool",
            color = { h = 117, s = 88, v = 25 }
        },
        {
            type = "ImmersiveVehiclePaint.HazelAutomotiveSprayPaint",
            group = "warm",
            color = { h = 46, s = 80, v = 78 }
        },
        {
            type = "ImmersiveVehiclePaint.CoffeeAutomotiveSprayPaint",
            group = "warm",
            color = { h = 28, s = 99, v = 38 }
        },
    },
    PPE_KITS                = {
        {
            SAFETY_GOGGLES = "Base.Glasses_SafetyGoggles",
            DUST_MASK = "Base.Hat_DustMask"
        },
        {
            SAFETY_GOGGLES = "Base.Glasses_SafetyGoggles",
            HALF_MASK_RESPIRATOR = "Base.Hat_BuildersRespirator"
        },
        {
            GAS_MASK = "Base.Hat_GasMask"
        },
        {
            NBC_MASK = "Base.Hat_NBCmask"
        },
        {
            IMPROVISED_GAS_MASK = "Base.Hat_ImprovisedGasMask"
        },
    },
}
HDCP_IVP_Constants.DROP_RATE = {
    COMMON    = 20,
    UNCOMMON  = 8,
    RARE      = 4,
    EPIC      = 2,
    LEGENDARY = 1,
    MYTHIC    = 0.5,
    RELIC     = 0.1,
    DIVINE    = 0.01,
    GODLIKE   = 0.005
}
return HDCP_IVP_Constants
