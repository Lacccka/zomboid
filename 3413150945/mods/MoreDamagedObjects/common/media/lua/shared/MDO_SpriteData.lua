-- From "More Damaged Objects [B42]" mod -- Author = carlesturo

-- ------------------------------------------------------------------------------------------------

-- **************** MORE DAMAGED OBJECTS - SPRITE DATA - FLOOR SPRITES ****************

local BrokenGlassSprites = {
    "brokenglass_1_0", "brokenglass_1_1", "brokenglass_1_2", "brokenglass_1_3"
}

local BrokenWhiteWoodSprites = {
    "fencing_damaged_01_124", "fencing_damaged_01_125", "fencing_damaged_01_126", "fencing_damaged_01_127"
}

local BrokenWireSprites = {
    "fencing_damaged_01_140", "fencing_damaged_01_141", "fencing_damaged_01_142", "fencing_damaged_01_143"
}

local BrokenBrownWoodSprites = {
    "fencing_damaged_01_136", "fencing_damaged_01_137", "fencing_damaged_01_138", "fencing_damaged_01_139"
}

local BrokenCarpentrySprites = {
    "fencing_damaged_01_168", "fencing_damaged_01_169", "fencing_damaged_01_170", "fencing_damaged_01_171"
}

local BrokenNewB42WoodSprites = {
    "ct_more_damaged_objects_04_80", "ct_more_damaged_objects_04_81", "ct_more_damaged_objects_04_82", "ct_more_damaged_objects_04_83"
}

local BrokenBurnedSprites = {
    "ct_more_damaged_objects_04_84", "ct_more_damaged_objects_04_85", "ct_more_damaged_objects_04_86", "ct_more_damaged_objects_04_87"
}

local BrokenWhiteWoodSprites2 = {
    "ct_more_damaged_objects_05_20", "ct_more_damaged_objects_05_21", "ct_more_damaged_objects_05_22", "ct_more_damaged_objects_05_23"
}

local BrokenPoleSprites = {
    "ct_more_damaged_objects_05_60", "ct_more_damaged_objects_05_61", "ct_more_damaged_objects_05_62", "ct_more_damaged_objects_05_63"
}

-- ------------------------------------------------------------------------------------------------

-- **************** MORE DAMAGED OBJECTS - SPRITE DATA - STREET LIGHTS TALL ****************

local StreetLightTallStreetsAndTrafficLight = {
    baseSprite = "ct_more_damaged_objects_02_8",
    aboveSpriteReplacements = {
        ["lighting_outdoor_01_8"] = "ct_more_damaged_objects_02_9",
        ["lighting_outdoor_01_9"] = "ct_more_damaged_objects_02_12",
        ["lighting_outdoor_01_10"] = "ct_more_damaged_objects_02_17",
        ["lighting_outdoor_01_11"] = "ct_more_damaged_objects_02_20",
        ["lighting_outdoor_01_12"] = "ct_more_damaged_objects_02_24",
        ["lighting_outdoor_01_13"] = "ct_more_damaged_objects_02_26",
        ["lighting_outdoor_01_14"] = "ct_more_damaged_objects_02_27",
        ["lighting_outdoor_01_15"] = "ct_more_damaged_objects_02_30",
        ["lighting_outdoor_01_20"] = "ct_more_damaged_objects_02_33",
        ["lighting_outdoor_01_21"] = "ct_more_damaged_objects_02_35",
        ["lighting_outdoor_01_22"] = "ct_more_damaged_objects_02_38",
        ["lighting_outdoor_01_23"] = "ct_more_damaged_objects_02_40",
		["street_decoration_01_48"] = "ct_more_damaged_objects_02_120",
		["street_decoration_01_49"] = "ct_more_damaged_objects_02_121",
		["street_decoration_01_50"] = "ct_more_damaged_objects_02_123",
		["street_decoration_01_51"] = "ct_more_damaged_objects_02_124",
    },
    baseSpriteReplacement = {
        ["ct_more_damaged_objects_02_11"] = {
            "ct_more_damaged_objects_02_12", "ct_more_damaged_objects_02_26", "ct_more_damaged_objects_02_40"
        },
        ["ct_more_damaged_objects_02_18"] = {
            "ct_more_damaged_objects_02_17", "ct_more_damaged_objects_02_24", "ct_more_damaged_objects_02_38"
        },
        ["ct_more_damaged_objects_02_21"] = {
            "ct_more_damaged_objects_02_20", "ct_more_damaged_objects_02_30", "ct_more_damaged_objects_02_35"
        }
    },
	additionalSprites = {
		["ct_more_damaged_objects_02_9"] = {
			{dx = 0, dy = -1, sprite = "ct_more_damaged_objects_02_10"}
		},
		["ct_more_damaged_objects_02_12"] = {
			{dx = 1, dy = 0, sprite = "ct_more_damaged_objects_02_13"}
		},
		["ct_more_damaged_objects_02_17"] = {
			{dx = 0, dy = 1, sprite = "ct_more_damaged_objects_02_16"}
		},
		["ct_more_damaged_objects_02_20"] = {
			{dx = -1, dy = 0, sprite = "ct_more_damaged_objects_02_19"}
		},
		["ct_more_damaged_objects_02_24"] = {
			{dx = 0, dy = 1, sprite = "ct_more_damaged_objects_02_25"}
		},
		["ct_more_damaged_objects_02_27"] = {
			{dx = 0, dy = -1, sprite = "ct_more_damaged_objects_02_28"}
		},
		["ct_more_damaged_objects_02_30"] = {
			{dx = -1, dy = 0, sprite = "ct_more_damaged_objects_02_31"}
		},
		["ct_more_damaged_objects_02_33"] = {
			{dx = 0, dy = -1, sprite = "ct_more_damaged_objects_02_34"}
		},
		["ct_more_damaged_objects_02_35"] = {
			{dx = -1, dy = 0, sprite = "ct_more_damaged_objects_02_36"}
		},
		["ct_more_damaged_objects_02_40"] = {
			{dx = 1, dy = 0, sprite = "ct_more_damaged_objects_02_41"}
		},
		["ct_more_damaged_objects_02_121"] = {
			{dx = 0, dy = -1, sprite = "ct_more_damaged_objects_02_122"}
		},
		["ct_more_damaged_objects_02_124"] = {
			{dx = 0, dy = 1, sprite = "ct_more_damaged_objects_02_125"}
		}
	},
    brokenGlassSprites = BrokenGlassSprites,
	brokenglassPositions = {
		["ct_more_damaged_objects_02_9"] = {{dx = 0, dy = -1}},
		["ct_more_damaged_objects_02_12"] = {{dx = 1, dy = 0}},
		["ct_more_damaged_objects_02_17"] = {{dx = 0, dy = 1}},
		["ct_more_damaged_objects_02_20"] = {{dx = -1, dy = 0}},	
		["ct_more_damaged_objects_02_24"] = {{dx = -1, dy = 0}},
		["ct_more_damaged_objects_02_26"] = {{dx = 0, dy = 1}},
		["ct_more_damaged_objects_02_27"] = {{dx = 1, dy = 0}},
		["ct_more_damaged_objects_02_30"] = {{dx = 0, dy = -1}},
		["ct_more_damaged_objects_02_33"] = {{dx = -1, dy = 0}},
		["ct_more_damaged_objects_02_35"] = {{dx = 0, dy = 1}},
		["ct_more_damaged_objects_02_38"] = {{dx = 1, dy = 0}},
		["ct_more_damaged_objects_02_40"] = {{dx = 0, dy = -1}},
	}
}

local StreetLightTallParkingLots = {
    baseSprite = "ct_more_damaged_objects_02_48",
    aboveSpriteReplacements = {
        ["lighting_outdoor_01_18"] = "ct_more_damaged_objects_02_49",
        ["lighting_outdoor_01_19"] = "ct_more_damaged_objects_02_57",
    },
    brokenGlassSprites = BrokenGlassSprites,
    brokenglassPositions = {
        ["ct_more_damaged_objects_02_49"] = {
            {dx = 0, dy = -1},
            {dx = 0, dy = 1},
        },
        ["ct_more_damaged_objects_02_57"] = {
            {dx = 1, dy = 0},
            {dx = -1, dy = 0},
        },
    }
}

local StreetLightTallNewB42 = {
    baseSprite = "ct_more_damaged_objects_02_23",
    aboveSpriteReplacements = {
        ["lighting_outdoor_01_5"] = "ct_more_damaged_objects_02_15",
    },
}

-- ------------------------------------------------------------------------------------------------

-- **************** MORE DAMAGED OBJECTS - SPRITE DATA - STREET LIGHTS SHORT ****************

local StreetLightShortFlatHeadDark = {
    baseSprite = "ct_more_damaged_objects_02_0",
	spriteLeft = "ct_more_damaged_objects_02_1",
    spriteRight = "ct_more_damaged_objects_02_0",
    brokenGlassSprites = BrokenGlassSprites,
}

local StreetLightShortFlatRoundHead = {
    baseSprite = "ct_more_damaged_objects_02_2",
	spriteLeft = "ct_more_damaged_objects_02_3",
    spriteRight = "ct_more_damaged_objects_02_2",
    brokenGlassSprites = BrokenGlassSprites,
}

local StreetLightShortFlatHeadLight = {
    baseSprite = "ct_more_damaged_objects_02_4",
	spriteLeft = "ct_more_damaged_objects_02_5",
    spriteRight = "ct_more_damaged_objects_02_4",
    brokenGlassSprites = BrokenGlassSprites,
}

local StreetLightShortFlatNewB42 = {
    baseSprite = "ct_more_damaged_objects_02_6",
	spriteLeft = "ct_more_damaged_objects_02_7",
    spriteRight = "ct_more_damaged_objects_02_6",
}

local StreetLightShortFlatOldB41 = {
    baseSprite = "ct_more_damaged_objects_01_0",
	spriteLeft = "ct_more_damaged_objects_01_7",
    spriteRight = "ct_more_damaged_objects_01_0",
    brokenGlassSprites = BrokenGlassSprites,
}

-- ------------------------------------------------------------------------------------------------

-- **************** MORE DAMAGED OBJECTS - SPRITE DATA - WALLS AND FENCES ****************

local BrownDividedGlassWall = {
    baseSpriteWestWall = "ct_more_damaged_objects_03_0",
	newSpriteWestWall = "ct_more_damaged_objects_03_4",
	baseSpriteNorthWall = "ct_more_damaged_objects_03_1",
	newSpriteNorthWall = "ct_more_damaged_objects_03_5",
	baseSpriteSouthEastCorner = "ct_more_damaged_objects_03_3",
	newSpriteSouthEastCorner = "ct_more_damaged_objects_03_7",
    brokenGlassSprites = BrokenGlassSprites,
}

local BlackFullWindowWall = {
    baseSpriteWestWall = "ct_more_damaged_objects_03_8",
	newSpriteWestWall = "ct_more_damaged_objects_03_12",
	baseSpriteNorthWall = "ct_more_damaged_objects_03_9",
	newSpriteNorthWall = "ct_more_damaged_objects_03_13",
	baseSpriteNorthWestCorner = "ct_more_damaged_objects_03_10",
	newSpriteNorthWestCorner = "ct_more_damaged_objects_03_14",
	baseSpriteSouthEastCorner = "ct_more_damaged_objects_03_11",
	newSpriteSouthEastCorner = "ct_more_damaged_objects_03_15",
    brokenGlassSprites = BrokenGlassSprites,
}

local BlackDividedGlassWall = {
    baseSpriteWestWall = "ct_more_damaged_objects_03_16",
	newSpriteWestWall = "ct_more_damaged_objects_03_20",
	baseSpriteNorthWall = "ct_more_damaged_objects_03_17",
	newSpriteNorthWall = "ct_more_damaged_objects_03_21",
	baseSpriteNorthWestCorner = "ct_more_damaged_objects_03_18",
	newSpriteNorthWestCorner = "ct_more_damaged_objects_03_22",
	baseSpriteSouthEastCorner = "ct_more_damaged_objects_03_19",
	newSpriteSouthEastCorner = "ct_more_damaged_objects_03_23",
    brokenGlassSprites = BrokenGlassSprites,
}

local BrownFullWindowWall = {
    baseSpriteWestWall = "ct_more_damaged_objects_03_24",
	newSpriteWestWall = "ct_more_damaged_objects_03_28",
	baseSpriteNorthWall = "ct_more_damaged_objects_03_25",
	newSpriteNorthWall = "ct_more_damaged_objects_03_29",
	baseSpriteNorthWestCorner = "ct_more_damaged_objects_03_26",
	newSpriteNorthWestCorner = "ct_more_damaged_objects_03_30",
	baseSpriteSouthEastCorner = "ct_more_damaged_objects_03_27",
	newSpriteSouthEastCorner = "ct_more_damaged_objects_03_31",
    brokenGlassSprites = BrokenGlassSprites,
}

local FullWindowWall = {
    baseSpriteWestWall = "ct_more_damaged_objects_03_32",
	newSpriteWestWall = "ct_more_damaged_objects_03_36",
	baseSpriteNorthWall = "ct_more_damaged_objects_03_33",
	newSpriteNorthWall = "ct_more_damaged_objects_03_37",
	baseSpriteNorthWestCorner = "ct_more_damaged_objects_03_34",
	newSpriteNorthWestCorner = "ct_more_damaged_objects_03_38",
	baseSpriteSouthEastCorner = "ct_more_damaged_objects_03_35",
	newSpriteSouthEastCorner = "ct_more_damaged_objects_03_39",
    brokenGlassSprites = BrokenGlassSprites,
}

local WhiteWall1 = {
    baseSpriteWestWall = "ct_more_damaged_objects_03_48",
	newSpriteWestWall = "ct_more_damaged_objects_03_52",
	baseSpriteNorthWall = "ct_more_damaged_objects_03_40",
	newSpriteNorthWall = "ct_more_damaged_objects_03_44",
    brokenGlassSprites = BrokenGlassSprites,
    brokenWhiteWoodSprites2 = BrokenWhiteWoodSprites2,
}

local WhiteWall2 = {
    baseSpriteWestWall = "ct_more_damaged_objects_03_49",
	newSpriteWestWall = "ct_more_damaged_objects_03_53",
	baseSpriteNorthWall = "ct_more_damaged_objects_03_41",
	newSpriteNorthWall = "ct_more_damaged_objects_03_45",
    brokenGlassSprites = BrokenGlassSprites,
    brokenWhiteWoodSprites2 = BrokenWhiteWoodSprites2,
}

local WhiteWall3 = {
    baseSpriteWestWall = "ct_more_damaged_objects_03_50",
	newSpriteWestWall = "ct_more_damaged_objects_03_54",
	baseSpriteNorthWall = "ct_more_damaged_objects_03_42",
	newSpriteNorthWall = "ct_more_damaged_objects_03_46",
    brokenGlassSprites = BrokenGlassSprites,
    brokenWhiteWoodSprites2 = BrokenWhiteWoodSprites2,
}

local WhiteWall4 = {
    baseSpriteWestWall = "ct_more_damaged_objects_03_51",
	newSpriteWestWall = "ct_more_damaged_objects_03_55",
	baseSpriteNorthWall = "ct_more_damaged_objects_03_43",
	newSpriteNorthWall = "ct_more_damaged_objects_03_47",
    brokenGlassSprites = BrokenGlassSprites,
	brokenWhiteWoodSprites2 = BrokenWhiteWoodSprites2,
}

local GreenesWall1 = {
    baseSpriteWestWall = "ct_more_damaged_objects_03_56",
	newSpriteWestWall = "ct_more_damaged_objects_03_60",
	baseSpriteNorthWall = "ct_more_damaged_objects_03_57",
	newSpriteNorthWall = "ct_more_damaged_objects_03_61",
	baseSpriteSouthEastCorner = "ct_more_damaged_objects_03_59",
	newSpriteSouthEastCorner = "ct_more_damaged_objects_03_63",
    brokenGlassSprites = BrokenGlassSprites,
}

local GreenesWall2 = {
    baseSpriteWestWall = "ct_more_damaged_objects_03_64",
	newSpriteWestWall = "ct_more_damaged_objects_03_68",
	baseSpriteNorthWall = "ct_more_damaged_objects_03_66",
	newSpriteNorthWall = "ct_more_damaged_objects_03_70",
    brokenGlassSprites = BrokenGlassSprites,
}

local GreenesWall3 = {
    baseSpriteWestWall = "ct_more_damaged_objects_03_65",
	newSpriteWestWall = "ct_more_damaged_objects_03_69",
	baseSpriteNorthWall = "ct_more_damaged_objects_03_67",
	newSpriteNorthWall = "ct_more_damaged_objects_03_71",
    brokenGlassSprites = BrokenGlassSprites,
}

local SeahorseWall1 = {
    baseSpriteWestWall = "ct_more_damaged_objects_03_80",
	newSpriteWestWall = "ct_more_damaged_objects_03_84",
	baseSpriteNorthWall = "ct_more_damaged_objects_03_72",
	newSpriteNorthWall = "ct_more_damaged_objects_03_76",
    brokenGlassSprites = BrokenGlassSprites,
    brokenCarpentrySprites = BrokenCarpentrySprites,
}

local SeahorseWall2 = {
    baseSpriteWestWall = "ct_more_damaged_objects_03_81",
	newSpriteWestWall = "ct_more_damaged_objects_03_85",
	baseSpriteNorthWall = "ct_more_damaged_objects_03_73",
	newSpriteNorthWall = "ct_more_damaged_objects_03_77",
    brokenGlassSprites = BrokenGlassSprites,
    brokenCarpentrySprites = BrokenCarpentrySprites,
}

local SeahorseWall3 = {
    baseSpriteWestWall = "ct_more_damaged_objects_03_82",
	newSpriteWestWall = "ct_more_damaged_objects_03_86",
	baseSpriteNorthWall = "ct_more_damaged_objects_03_74",
	newSpriteNorthWall = "ct_more_damaged_objects_03_78",
    brokenGlassSprites = BrokenGlassSprites,
    brokenCarpentrySprites = BrokenCarpentrySprites,
}
-- ------------------------------------------------------------------------------------------------
local WhiteWoodFence = {
    baseSpriteWestWall = "fencing_damaged_01_12",
	newSpriteWestWall = "ct_more_damaged_objects_04_9",
	baseSpriteNorthWall = "fencing_damaged_01_9",
	newSpriteNorthWall = "ct_more_damaged_objects_04_8",
	baseSpriteNorthWestCorner = "fencing_damaged_01_113",
	newSpriteNorthWestCorner = "ct_more_damaged_objects_04_10",
	baseSpriteSouthEastCorner = "ct_more_damaged_objects_04_11",
	newSpriteSouthEastCorner = "ct_more_damaged_objects_04_12",
    brokenWhiteWoodSprites = BrokenWhiteWoodSprites,
}

local BarbedFence = {
    baseSpriteWestWall = "ct_more_damaged_objects_04_16",
	newSpriteWestWall = "ct_more_damaged_objects_04_20",
	baseSpriteNorthWall = "ct_more_damaged_objects_04_17",
	newSpriteNorthWall = "ct_more_damaged_objects_04_21",
	baseSpriteNorthWestCorner = "ct_more_damaged_objects_04_18",
	newSpriteNorthWestCorner = "ct_more_damaged_objects_04_22",
	baseSpriteSouthEastCorner = "ct_more_damaged_objects_04_19",
	newSpriteSouthEastCorner = "ct_more_damaged_objects_04_23",
}

local BarbedFence2 = {
    baseSpriteWestWall = "ct_more_damaged_objects_04_24",
	newSpriteWestWall = "ct_more_damaged_objects_04_26",
	baseSpriteNorthWall = "ct_more_damaged_objects_04_25",
	newSpriteNorthWall = "ct_more_damaged_objects_04_27",
}

local WireFence = {
    baseSpriteWestWall = "fencing_damaged_01_36",
	newSpriteWestWall = "ct_more_damaged_objects_04_33",
	baseSpriteNorthWall = "fencing_damaged_01_33",
	newSpriteNorthWall = "ct_more_damaged_objects_04_32",
	baseSpriteNorthWestCorner = "fencing_damaged_01_116",
	newSpriteNorthWestCorner = "ct_more_damaged_objects_04_34",
	baseSpriteSouthEastCorner = "ct_more_damaged_objects_04_35",
	newSpriteSouthEastCorner = "ct_more_damaged_objects_04_36",
	brokenWireSprites = BrokenWireSprites,
}

local WireFence2 = {
    baseSpriteWestWall = "fencing_damaged_01_44",
	newSpriteWestWall = "ct_more_damaged_objects_04_38",
	baseSpriteNorthWall = "fencing_damaged_01_41",
	newSpriteNorthWall = "ct_more_damaged_objects_04_37",
	brokenWireSprites = BrokenWireSprites,
}

local BrownWoodFence = {
    baseSpriteWestWall = "fencing_damaged_01_52",
	newSpriteWestWall = "ct_more_damaged_objects_04_41",
	baseSpriteNorthWall = "fencing_damaged_01_49",
	newSpriteNorthWall = "ct_more_damaged_objects_04_40",
	baseSpriteNorthWestCorner = "fencing_damaged_01_117",
	newSpriteNorthWestCorner = "ct_more_damaged_objects_04_42",
	baseSpriteSouthEastCorner = "ct_more_damaged_objects_04_43",
	newSpriteSouthEastCorner = "ct_more_damaged_objects_04_44",
    brokenBrownWoodSprites = BrokenBrownWoodSprites,
}

local BrownWoodFence2 = {
    baseSpriteWestWall = "fencing_damaged_01_60",
	newSpriteWestWall = "ct_more_damaged_objects_04_46",
	baseSpriteNorthWall = "fencing_damaged_01_57",
	newSpriteNorthWall = "ct_more_damaged_objects_04_45",
    brokenBrownWoodSprites = BrokenBrownWoodSprites,
}

local CarpentryFence1 = {
    baseSpriteWestWall = "fencing_damaged_01_148",
	newSpriteWestWall = "ct_more_damaged_objects_04_49",
	baseSpriteNorthWall = "fencing_damaged_01_145",
	newSpriteNorthWall = "ct_more_damaged_objects_04_48",
	baseSpriteNorthWestCorner = "ct_more_damaged_objects_04_56",
	newSpriteNorthWestCorner = "ct_more_damaged_objects_04_57",
	baseSpriteSouthEastCorner = "ct_more_damaged_objects_04_54",
	newSpriteSouthEastCorner = "ct_more_damaged_objects_04_55",
    brokenCarpentrySprites = BrokenCarpentrySprites,
}

local CarpentryFence2 = {
    baseSpriteWestWall = "fencing_damaged_01_156",
	newSpriteWestWall = "ct_more_damaged_objects_04_51",
	baseSpriteNorthWall = "fencing_damaged_01_153",
	newSpriteNorthWall = "ct_more_damaged_objects_04_50",
	baseSpriteNorthWestCorner = "ct_more_damaged_objects_04_58",
	newSpriteNorthWestCorner = "ct_more_damaged_objects_04_59",
	baseSpriteSouthEastCorner = "ct_more_damaged_objects_04_54",
	newSpriteSouthEastCorner = "ct_more_damaged_objects_04_55",
    brokenCarpentrySprites = BrokenCarpentrySprites,
}

local CarpentryFence3 = {
    baseSpriteWestWall = "fencing_damaged_01_164",
	newSpriteWestWall = "ct_more_damaged_objects_04_53",
	baseSpriteNorthWall = "fencing_damaged_01_161",
	newSpriteNorthWall = "ct_more_damaged_objects_04_52",
	baseSpriteNorthWestCorner = "ct_more_damaged_objects_04_60",
	newSpriteNorthWestCorner = "ct_more_damaged_objects_04_61",
	baseSpriteSouthEastCorner = "ct_more_damaged_objects_04_54",
	newSpriteSouthEastCorner = "ct_more_damaged_objects_04_55",
    brokenCarpentrySprites = BrokenCarpentrySprites,
}

local NewB42WoodFence = {
    baseSpriteWestWall = "ct_more_damaged_objects_04_73",
	newSpriteWestWall = "ct_more_damaged_objects_04_77",
	baseSpriteNorthWall = "ct_more_damaged_objects_04_72",
	newSpriteNorthWall = "ct_more_damaged_objects_04_76",
	baseSpriteNorthWestCorner = "ct_more_damaged_objects_04_74",
	newSpriteNorthWestCorner = "ct_more_damaged_objects_04_78",
	baseSpriteSouthEastCorner = "ct_more_damaged_objects_04_75",
	newSpriteSouthEastCorner = "ct_more_damaged_objects_04_79",
	brokenNewB42WoodSprites = BrokenNewB42WoodSprites,
}

local BurnedFence = {
    baseSpriteWestWall = "ct_more_damaged_objects_04_88",
	newSpriteWestWall = "ct_more_damaged_objects_04_92",
	baseSpriteNorthWall = "ct_more_damaged_objects_04_89",
	newSpriteNorthWall = "ct_more_damaged_objects_04_93",
	baseSpriteNorthWestCorner = "ct_more_damaged_objects_04_90",
	newSpriteNorthWestCorner = "ct_more_damaged_objects_04_94",
	baseSpriteSouthEastCorner = "ct_more_damaged_objects_04_91",
	newSpriteSouthEastCorner = "ct_more_damaged_objects_04_95",
	brokenBurnedSprites = BrokenBurnedSprites,
}

local PoleFence = {
    baseSpriteWestWall = "ct_more_damaged_objects_05_64",
	newSpriteWestWall = "ct_more_damaged_objects_05_68",
	baseSpriteNorthWall = "ct_more_damaged_objects_05_65",
	newSpriteNorthWall = "ct_more_damaged_objects_05_69",
	baseSpriteNorthWestCorner = "ct_more_damaged_objects_05_66",
	newSpriteNorthWestCorner = "ct_more_damaged_objects_05_70",
	baseSpriteSouthEastCorner = "ct_more_damaged_objects_05_67",
	newSpriteSouthEastCorner = "ct_more_damaged_objects_05_71",
	brokenPoleSprites = BrokenPoleSprites,
}
-- ------------------------------------------------------------------------------------------------
local BrownWoodClosedAndOpenFenceGate = {
    baseSpriteWestWall = "ct_more_damaged_objects_05_8",
	newSpriteWestWall = "ct_more_damaged_objects_05_9",
	baseSpriteNorthWall = "ct_more_damaged_objects_05_10",
	newSpriteNorthWall = "ct_more_damaged_objects_05_11",
    brokenBrownWoodSprites = BrokenBrownWoodSprites,
}

local WhiteWoodClosedAndOpenFenceGate = {
    baseSpriteWestWall = "ct_more_damaged_objects_05_16",
	newSpriteWestWall = "ct_more_damaged_objects_05_17",
	baseSpriteNorthWall = "ct_more_damaged_objects_05_18",
	newSpriteNorthWall = "ct_more_damaged_objects_05_19",
    brokenWhiteWoodSprites2 = BrokenWhiteWoodSprites2,
}

local WireClosedAndOpenFenceGate = {
    baseSpriteWestWall = "ct_more_damaged_objects_05_32",
	newSpriteWestWall = "ct_more_damaged_objects_05_33",
	baseSpriteNorthWall = "ct_more_damaged_objects_05_34",
	newSpriteNorthWall = "ct_more_damaged_objects_05_35",
	brokenWireSprites = BrokenWireSprites,
}

local PoleClosedAndOpenFenceGate = {
    baseSpriteWestWall = "ct_more_damaged_objects_05_56",
	newSpriteWestWall = "ct_more_damaged_objects_05_57",
	baseSpriteNorthWall = "ct_more_damaged_objects_05_58",
	newSpriteNorthWall = "ct_more_damaged_objects_05_59",
	brokenPoleSprites = BrokenPoleSprites,
}

-- ------------------------------------------------------------------------------------------------

-- **************** MORE DAMAGED OBJECTS - SPRITE DATA - BUSHES ****************

local Bushes = {
    baseSpriteBush = "ct_more_damaged_objects_01_8",
}

-- ------------------------------------------------------------------------------------------------

-- **************** MORE DAMAGED OBJECTS - SPRITE DATA - STANDPIPE ****************

local Standpipe = {
    baseSpriteStandpipe = "ct_more_damaged_objects_06_44",
	newSpriteStandpipe = "ct_more_damaged_objects_06_45",
	baseSpriteWaterJet = "ct_more_damaged_objects_06_16",
	brokenSpriteStandpipe = "ct_more_damaged_objects_06_46",
    animationSprites = {
		"ct_more_damaged_objects_06_16",
        "ct_more_damaged_objects_06_17",
		"ct_more_damaged_objects_06_18",
        "ct_more_damaged_objects_06_19",
        "ct_more_damaged_objects_06_20",
		"ct_more_damaged_objects_06_21",
		"ct_more_damaged_objects_06_22",
		"ct_more_damaged_objects_06_23",
		"ct_more_damaged_objects_06_40",
		"ct_more_damaged_objects_06_41",
		"ct_more_damaged_objects_06_42",
		"ct_more_damaged_objects_06_43"
    },
	baseSpriteAboveWaterJet = "ct_more_damaged_objects_06_8",
    aboveAnimationSprites = {
		"ct_more_damaged_objects_06_8",
        "ct_more_damaged_objects_06_9",
		"ct_more_damaged_objects_06_10",
        "ct_more_damaged_objects_06_11",
        "ct_more_damaged_objects_06_12",
		"ct_more_damaged_objects_06_13",
		"ct_more_damaged_objects_06_14",
		"ct_more_damaged_objects_06_15",
		"ct_more_damaged_objects_06_32",
		"ct_more_damaged_objects_06_33",
		"ct_more_damaged_objects_06_34",
		"ct_more_damaged_objects_06_35"
    },
	baseSpriteAbove2WaterJet = "ct_more_damaged_objects_06_0",
    above2AnimationSprites = {
		"ct_more_damaged_objects_06_0",
        "ct_more_damaged_objects_06_1",
		"ct_more_damaged_objects_06_2",
        "ct_more_damaged_objects_06_3",
        "ct_more_damaged_objects_06_4",
		"ct_more_damaged_objects_06_5",
		"ct_more_damaged_objects_06_6",
		"ct_more_damaged_objects_06_7",
		"ct_more_damaged_objects_06_24",
		"ct_more_damaged_objects_06_25",
		"ct_more_damaged_objects_06_26",
		"ct_more_damaged_objects_06_27"
    }
}

-- ------------------------------------------------------------------------------------------------

-- **************** MORE DAMAGED OBJECTS - SPRITE DATA ****************

local StreetLight = {
    TallStreetsAndTrafficLight = StreetLightTallStreetsAndTrafficLight,
    TallParkingLots = StreetLightTallParkingLots,
	TallNewB42 = StreetLightTallNewB42,
	ShortFlatHeadDark = StreetLightShortFlatHeadDark,
	ShortFlatRoundHead = StreetLightShortFlatRoundHead,
	ShortFlatHeadLight = StreetLightShortFlatHeadLight,
	ShortFlatNewB42 = StreetLightShortFlatNewB42,
	ShortFlatOldB41 = StreetLightShortFlatOldB41
}

local WallsAndFences = {
	BrownDivided = BrownDividedGlassWall,
	BlackFull = BlackFullWindowWall,
	BlackDivided = BlackDividedGlassWall,
	BrownFull = BrownFullWindowWall,
	Full = FullWindowWall,
	WhiteWall1 = WhiteWall1,
	WhiteWall2 = WhiteWall2,
	WhiteWall3 = WhiteWall3,
	WhiteWall4 = WhiteWall4,
	GreenesWall1 = GreenesWall1,
	GreenesWall2 = GreenesWall2,
	GreenesWall3 = GreenesWall3,
	SeahorseWall1 = SeahorseWall1,
	SeahorseWall2 = SeahorseWall2,
	SeahorseWall3 = SeahorseWall3,
	WhiteFence = WhiteWoodFence,
	BarbedFence = BarbedFence,
	BarbedFence2 = BarbedFence2,
	WireFence = WireFence,
	WireFence2 = WireFence2,
	BrownFence = BrownWoodFence,
	BrownFence2 = BrownWoodFence2,
	CarpentryFence1 = CarpentryFence1,
	CarpentryFence2 = CarpentryFence2,
	CarpentryFence3 = CarpentryFence3,
	NewB42WoodFence = NewB42WoodFence,
	BurnedFence = BurnedFence,
	PoleFence = PoleFence,
	BrownClosedAndOpenFenceGate = BrownWoodClosedAndOpenFenceGate,
	WhiteClosedAndOpenFenceGate = WhiteWoodClosedAndOpenFenceGate,
	WireClosedAndOpenFenceGate = WireClosedAndOpenFenceGate,
	PoleClosedAndOpenFenceGate = PoleClosedAndOpenFenceGate
}

local function getSpriteDataByBaseSprite(baseSprite)
    for _, streetLight in pairs(StreetLight) do
        if streetLight.baseSprite == baseSprite then
            return streetLight
        end
    end
    for _, wallData in pairs(WallsAndFences) do
        for key, value in pairs(wallData) do
            if value == baseSprite then
                return wallData
            end
        end
    end
	if Bushes.baseSpriteBush == baseSprite then
		return Bushes
	end
    if Standpipe.baseSpriteStandpipe == baseSprite then
        return Standpipe
    end
    return nil
end

local function getSpriteDataFromNewStandpipeSprite(spriteName)
    if Standpipe.newSpriteStandpipe == spriteName then
        return Standpipe
    end
    return nil
end

return {
    StreetLight = StreetLight,
	WallsAndFences = WallsAndFences,
	Bushes = Bushes,
	Standpipe = Standpipe,
    getSpriteDataByBaseSprite = getSpriteDataByBaseSprite,
	getSpriteDataFromNewStandpipeSprite = getSpriteDataFromNewStandpipeSprite
}

-- ------------------------------------------------------------------------------------------------