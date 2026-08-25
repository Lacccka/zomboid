AnimalDefinitions.stages["securitron"] = {
    stages = {
        ["babycup"] = {
            ageToGrow = 2 * 0,
            nextStage = "tea",
            nextStageMale = "coffee",
            minWeight = 0.1,
            maxWeight = 0.25
        },
        ["tea"] = {
            minWeight = 0.25,
            maxWeight = 0.5
        },
        ["coffee"] = {
            minWeight = 0.25,
            maxWeight = 0.5
        }
    }
}

AnimalDefinitions.genome["securitron"] = {
    genes = {
        ["maxSize"] = "maxSize",
        ["meatRatio"] = "meatRatio",
        ["maxWeight"] = "maxWeight",
        ["lifeExpectancy"] = "lifeExpectancy",
        ["resistance"] = "resistance",
        ["strength"] = "strength",
        ["hungerResistance"] = "hungerResistance",
        ["thirstResistance"] = "thirstResistance",
        ["aggressiveness"] = "aggressiveness",
        ["ageToGrow"] = "ageToGrow",
        ["fertility"] = "fertility",
        ["stress"] = "stress",
        ["swiftness"] = "swiftness",
        ["endurance"] = "endurance",
        ["haulingCapacity"] = "haulingCapacity"
    }
}

AnimalDefinitions.breeds["securitron"] = {
    breeds = {
        ["muggy"] = {
            name = "muggy",
            texture = "MuggyMod/PFO_MUGGY",
            textureMale = "MuggyMod/PFO_MUGGY",
            rottenTexture = "MuggyMod/PFO_MUGGY",
            textureBaby = "MuggyMod/PFO_MUGGY",
            invIconMale = "Item_DeerMale_Dead",
            invIconFemale = "Item_DeerFemale_Dead",
            invIconBaby = "Item_DeerFawn_Dead",
            invIconMaleDead = "Item_DeerMale_Dead",
            invIconFemaleDead = "Item_DeerFemale_Dead",
            invIconBabyDead = "Item_DeerFawn_Dead",
            canBePicked = false,
        }
    }
}


AnimalDefinitions.animals["babycup"] = {
    breeds = copyTable(AnimalDefinitions.breeds["securitron"].breeds);
    stages = AnimalDefinitions.stages["securitron"].stages;
    genes = AnimalDefinitions.genome["securitron"].genes;
    bodyModel = "MuggyMod.MuggyMug";
    bodyModelSkel = "MuggyMod.MuggyMug";
    modelscript = "MuggyMod.MuggyMug";
    textureSkeleton = "MuggyMod.MuggyMug";
    textureSkeletonBloody = "MuggyMod.MuggyMug";
    bodyModelSkelNoHead = "MuggyMod.MuggyMug";
    ropeBone = "Bip01_Neck";
    animset = "turkey",
    group = "securitron";
    animalSize = 0.1;
    shadoww = 0.3;
    shadowfm = 0.3;
    shadowbm = 0.3;
    wanderMul = 250;
    minSize = 0.35;
    maxSize = 0.75;
    hungerMultiplier = 0.0001;
    thirstMultiplier = 0.0002;
    minBlood = 200;
    maxBlood = 600;
    idleEmoteChance = 700;
    trailerBaseSize = 100;
    idleTypeNbr = 2;
    collisionSize = 1;
    baseEncumbrance = 2;
    corpseSize = 1;
    minWeight = 2;
    maxWeight = 4;
    spottingDist = 15;
    attackDist = 2;
    distToEat = 1;
    needMom = false;
    canBePicked = false;
    canBeKilledWithoutWeapon = false;
    sitRandomly = false;
    canClimbStairs = true;
    wild = false;
    canBeAlerted = true;
    canBeDomesticated = false;
    canThump = false;
    eatGrass = true;
    dontAttackOtherMale = true;
    knockdownAttack = false;
    attackIfStressed = false;
    attackBack = false;
    canBePet = false;
    canBeTransported = false;
    stressAboveGround = false;
    stressUnderRain = false;
    canClimbFences = true;
    collidable = true;
    eatTypeTrough = "AnimalFeed,Grass,Hay,Vegetables,Fruits,Seeds,Nuts,Nut,Insect";
}














AnimalDefinitions.animals["coffee"] = {
    breeds = copyTable(AnimalDefinitions.breeds["securitron"].breeds);
    stages = AnimalDefinitions.stages["securitron"].stages;
    genes = AnimalDefinitions.genome["securitron"].genes;
    minAge = AnimalDefinitions.stages["securitron"].stages["babycup"].ageToGrow;
    bodyModel = "MuggyMod.MuggyMug";
    bodyModelSkel = "MuggyMod.MuggyMug";
    textureSkeleton = "MuggyMod.MuggyMug";
    textureSkeletonBloody = "MuggyMod.MuggyMug";
    bodyModelSkelNoHead = "MuggyMod.MuggyMug";
    modelscript = "MuggyMod.MuggyMug";
    bodyModelHeadless = "MuggyMod.MuggyMug";
    textureSkinned = "MuggyMod/MuggyMug";
    ropeBone = "Bip01_Neck";
    animset = "turkey";
    group = "securitron";
    babyType = "babycup";
    mate = "tea";
    maxAgeGeriatric = 19 * 30;
    shadoww = 0.3;
    shadowfm = 0.3;
    shadowbm = 0.3;
    minSize = 0.35;
    maxSize = 0.75;
    animalSize = 0.3;
    hungerMultiplier = 0.0001;
    thirstMultiplier = 0.0002;
    corpseSize = 1;
    collisionSize = 1;
    baseEncumbrance = 3;
    minWeight = 1;
    maxWeight = 3;
    minBlood = 800;
    maxBlood = 2500;
    trailerBaseSize = 300;
    distToEat = 1;
    minAgeForBaby = 10;
    wanderMul = 200;
    idleTypeNbr = 2;
    idleEmoteChance = 700;
    spottingDist = 19;
    attackDist = 2;
    male = true;
    canBePicked = false;
    canBeKilledWithoutWeapon = false;
    sitRandomly = false;
    canClimbStairs = true;
    wild = false;
    canBeAlerted = true;
    canBeDomesticated = false;
    canThump = false;
    eatGrass = true;
    dontAttackOtherMale = true;
    knockdownAttack = false;
    attackIfStressed = false;
    attackBack = false;
    canBePet = false;
    canBeTransported = false;
    stressAboveGround = false;
    stressUnderRain = false;
    canClimbFences = true;
    collidable = true;
    eatTypeTrough = "AnimalFeed,Grass,Hay,Vegetables,Fruits,Seeds,Nuts,Nut,Insect";
}

AnimalDefinitions.animals["tea"] = {
    breeds = copyTable(AnimalDefinitions.breeds["securitron"].breeds);
    stages = AnimalDefinitions.stages["securitron"].stages;
    genes = AnimalDefinitions.genome["securitron"].genes;
    minAge = AnimalDefinitions.stages["securitron"].stages["babycup"].ageToGrow;
    bodyModel = "MuggyMod.MuggyMug";
    bodyModelSkel = "MuggyMod.MuggyMug";
    textureSkeleton = "MuggyMod.MuggyMug";
    textureSkeletonBloody = "MuggyMod.MuggyMug";
    bodyModelSkelNoHead = "MuggyMod.MuggyMug";
    modelscript = "MuggyMod.MuggyMug";
    bodyModelHeadless = "MuggyMod.MuggyMug";
    textureSkinned = "MuggyMod/MuggyMug";
    ropeBone = "Bip01_Neck";
    animset = "turkey";
    group = "securitron";
    babyType = "babycup";
    mate = "coffee";
    maxAgeGeriatric = 19 * 30;
    shadoww = 0.3;
    shadowfm = 0.3;
    shadowbm = 0.3;
    minSize = 0.35;
    maxSize = 0.75;
    animalSize = 0.3;
    hungerMultiplier = 0.0001;
    thirstMultiplier = 0.0002;
    corpseSize = 1;
    collisionSize = 1;
    baseEncumbrance = 3;
    minWeight = 1;
    maxWeight = 3;
    minBlood = 800;
    maxBlood = 2500;
    trailerBaseSize = 300;
    distToEat = 1;
    minAgeForBaby = 10;
    wanderMul = 200;
    idleTypeNbr = 2;
    idleEmoteChance = 700;
    spottingDist = 19;
    attackDist = 2;
    female = true;
    canBePicked = false;
    canBeKilledWithoutWeapon = false;
    sitRandomly = false;
    canClimbStairs = true;
    wild = false;
    canBeAlerted = true;
    canBeDomesticated = false;
    canThump = false;
    eatGrass = true;
    dontAttackOtherMale = true;
    knockdownAttack = false;
    attackIfStressed = false;
    attackBack = false;
    canBePet = false;
    canBeTransported = false;
    stressAboveGround = false;
    stressUnderRain = false;
    canClimbFences = true;
    collidable = true;
    eatTypeTrough = "AnimalFeed,Grass,Hay,Vegetables,Fruits,Seeds,Nuts,Nut,Insect";
}



local coffee_sounds = {
	death = { name = "muggy_blank", slot = "voice", priority = 100 },
	fallover = { name = "muggy_blank" },
	idle = { name = "muggy_blank", intervalMin = 5, intervalMax = 55, slot = "voice" },
	pain = { name = "muggy_blank", slot = "voice", priority = 50 },
	pick_up = { name = "muggy_blank", slot = "voice", priority = 1 },
	pick_up_corpse = { name = "muggy_blank" },
	put_down = { name = "muggy_blank", slot = "voice", priority = 1 },
	put_down_corpse = { name = "muggy_blank" },
	run = { name = "muggy_blank" },
	stressed = { name = "muggy_blank", intervalMin = 5, intervalMax = 10, slot = "voice" },
	walkloop = { name = "muggy_blank", slot = "walkloop" },
}
AnimalDefinitions.animals["coffee"].breeds["muggy"].sounds = coffee_sounds

local tea_sounds = {
	death = { name = "muggy_blank", slot = "voice", priority = 100 },
	fallover = { name = "muggy_blank" },
	idle = { name = "muggy_blank", intervalMin = 5, intervalMax = 55, slot = "voice" },
	pain = { name = "muggy_blank", slot = "voice", priority = 50 },
	pick_up = { name = "muggy_blank", slot = "voice", priority = 1 },
	pick_up_corpse = { name = "muggy_blank" },
	put_down = { name = "muggy_blank", slot = "voice", priority = 1 },
	put_down_corpse = { name = "muggy_blank" },
	run = { name = "muggy_blank" },
	stressed = { name = "muggy_blank", intervalMin = 5, intervalMax = 10, slot = "voice" },
	walkloop = { name = "muggy_blank", slot = "walkloop" },
}
AnimalDefinitions.animals["tea"].breeds["muggy"].sounds = tea_sounds

local babycup_sounds = {
	death = { name = "muggy_blank", slot = "voice", priority = 100 },
	fallover = { name = "muggy_blank" },
	idle = { name = "muggy_blank", intervalMin = 5, intervalMax = 55, slot = "voice" },
	pain = { name = "muggy_blank", slot = "voice", priority = 50 },
	pick_up = { name = "muggy_blank", slot = "voice", priority = 1 },
	pick_up_corpse = { name = "muggy_blank" },
	put_down = { name = "muggy_blank", slot = "voice", priority = 1 },
	put_down_corpse = { name = "muggy_blank" },
	run = { name = "muggy_blank" },
	stressed = { name = "muggy_blank", intervalMin = 3, intervalMax = 8, slot = "voice" },
	walkloop = { name = "muggy_blank", slot = "walkloop" },
}
AnimalDefinitions.animals["babycup"].breeds["muggy"].sounds = babycup_sounds

local AVATAR_DEFINITION = {
    zoom = 3,
    xoffset = 0,
    yoffset = -1,
    avatarWidth = 180,
    avatarDir = IsoDirections.SE,
    trailerDir = IsoDirections.SW,
    trailerZoom = 3,
    trailerXoffset = 0,
    trailerYoffset = 0,
    hook = true,
    butcherHookZoom = 3,
    butcherHookXoffset = 0,
    butcherHookYoffset = 0,
    animalPositionSize = 0.6,
    animalPositionX = 0,
    animalPositionY = 0.5,
    animalPositionZ = 0.7
}

AnimalAvatarDefinition["coffee"] = AVATAR_DEFINITION
AnimalAvatarDefinition["tea"] = AVATAR_DEFINITION
AnimalAvatarDefinition["babycup"] = AVATAR_DEFINITION
