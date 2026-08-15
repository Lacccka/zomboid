
require "Hotbar/ISHotbarAttachDefinition"
if not ISHotbarAttachDefinition then
    return
end



--[[
Kept here for repairs.

local SmallBeltLeft = {
	type = "SmallBeltLeft",
	name = "Belt Left", -- Name shown in the slot icon
	animset = "belt left",
	attachments = { -- list of possible item category and their modelAttachement group, the item category is defined in the item script
		Knife = "Belt Left Upside", -- defined in AttachedLocations.lua
		Hammer = "Belt Left",
		HammerRotated = "Belt Rotated Left",
		Nightstick = "Nightstick Left",
		Screwdriver  = "Belt Left Screwdriver",
		Wrench = "Wrench Left",
		MeatCleaver = "MeatCleaver Belt Left",
		Walkie = "Walkie Belt Left",
	},
}
table.insert(ISHotbarAttachDefinition, SmallBeltLeft);
--]]


------------------- yes -------------------
local OperatorHeadGearMount = { -- Bob_Equip_HolsterLeft_1Hand  Bob_Equip_HolsterLeft_1Hand_Out
	type = "OperatorHeadGearMount",
	name = "Flashlight Mount", -- Name shown in the slot icon 
	animset = "back", -- Animation name of when the item is stored or retreived.
	attachments = { -- the itemscript Provided or Atype ID
	MilitaryFlashlight = "Flashlight Mount",
	Flashlight = "Flashlight Mount",
	HandTorch = "Flashlight Mount",
	Torch = "Flashlight Mount",		
	Torchb = "Flashlight Mount",		
	TorchSmall = "Flashlight Mount",		
	HandTorchSmall = "Flashlight Mount",		
	HandTorchBig = "Flashlight Mount",
	PenLight = "Flashlight Mount",
	OldFlashlight = "Flashlight Mount", --Better Flashlights compatibility	
	},
}
table.insert(ISHotbarAttachDefinition, OperatorHeadGearMount);

local OperatorHeadGearMountAlt = { -- Bob_Equip_HolsterLeft_1Hand  Bob_Equip_HolsterLeft_1Hand_Out
	type = "OperatorHeadGearMountAlt",
	name = "Operator HeadGear Mount Alt", -- Name shown in the slot icon 
	animset = "back", -- Animation name of when the item is stored or retreived.
	attachments = {
	TacOpFlashlight  = "Tac Op Flashlight", -- the itemscript Provided or Atype ID
	},
}
table.insert(ISHotbarAttachDefinition, OperatorHeadGearMountAlt);

local StanagOperatorMolleStorage = {
	type = "StanagOperatorMolleStorage",-- Name shown in the slot icon
	name = "Stanag Operator Molle Storage", 
	animset = "back", -- Animation name 
	attachments = {
	GunMagazine = "StanagOperatorMolleStorage",
	Walkie = "StanagOperatorMolleStorage",
	MolleStorage = "StanagOperatorMolleStorage",
	},
}
table.insert(ISHotbarAttachDefinition, StanagOperatorMolleStorage);



local StanagOperatorMolleStorageMiddle = {
	type = "StanagOperatorMolleStorageMiddle",-- Name shown in the slot icon
	name = "Stanag Operator Molle Storage Middle", 
	animset = "back", -- Animation name 
	attachments = {
	GunMagazine = "StanagOperatorMolleStorageMiddle",
	Walkie = "StanagOperatorMolleStorageMiddle",
	MolleStorage = "StanagOperatorMolleStorageMiddle",
	},
}
table.insert(ISHotbarAttachDefinition, StanagOperatorMolleStorageMiddle);

local StanagOperatorMolleStorageRight = {
	type = "StanagOperatorMolleStorageRight",-- Name shown in the slot icon
	name = "Stanag Operator Molle Storage Right", 
	animset = "back", -- Animation name 
	attachments = {
	GunMagazine = "StanagOperatorMolleStorageRight",
	Walkie = "StanagOperatorMolleStorageRight",
	MolleStorage = "StanagOperatorMolleStorageRight",
	},
}
table.insert(ISHotbarAttachDefinition, StanagOperatorMolleStorageRight);













-- Molle Vest addon

local ARsetOperatorMolleStorage = {
	type = "ARsetOperatorMolleStorage",-- Name shown in the slot icon
	name = "ARset Operator Molle Storage", 
	animset = "back", -- Animation name 
	attachments = {
	GunMagazine = "ARsetOperatorMolleStorage",
	Walkie = "ARsetOperatorMolleStorage",
	ARsetOperatorMolleStorage = "ARsetOperatorMolleStorage",
	ARsetOperatorMolleStorageRight = "ARsetOperatorMolleStorage",
	ARsetOperatorMolleStorageMiddle = "ARsetOperatorMolleStorage",
	},
}
table.insert(ISHotbarAttachDefinition, ARsetOperatorMolleStorage);


local ARsetOperatorMolleStorageRight = {
	type = "ARsetOperatorMolleStorageRight",-- Name shown in the slot icon
	name = "ARset Operator Molle Storage Right", 
	animset = "back", -- Animation name 
	attachments = {
	GunMagazine = "ARsetOperatorMolleStorageRight",
	Walkie = "ARsetOperatorMolleStorageRight",
	MolleStorage = "ARsetOperatorMolleStorageRight",
	ARsetOperatorMolleStorage = "ARsetOperatorMolleStorage",
	ARsetOperatorMolleStorageRight = "ARsetOperatorMolleStorage",
	ARsetOperatorMolleStorageMiddle = "ARsetOperatorMolleStorage",
	},
}
table.insert(ISHotbarAttachDefinition, ARsetOperatorMolleStorageRight);


local ARsetOperatorMolleStorageMiddle = {
	type = "ARsetOperatorMolleStorageMiddle",-- Name shown in the slot icon
	name = "ARset Operator Molle Storage Middle", 
	animset = "back", -- Animation name 
	attachments = {
	GunMagazine = "ARsetOperatorMolleStorageMiddle",
	Walkie = "ARsetOperatorMolleStorageMiddle",
	MolleStorage = "ARsetOperatorMolleStorageMiddle",
	ARsetOperatorMolleStorage = "ARsetOperatorMolleStorage",
	ARsetOperatorMolleStorageRight = "ARsetOperatorMolleStorage",
	ARsetOperatorMolleStorageMiddle = "ARsetOperatorMolleStorage",
	},
}
table.insert(ISHotbarAttachDefinition, ARsetOperatorMolleStorageMiddle);



-- ARset end.




local TacOpFlashlight = {
	type = "TacOpFlashlight", -- Operator_GLXHeadGear_Mount Old.
	name = "Tac Op Flashlight", -- Name shown in the slot icon 
	animset = "back", -- Animation name of when the item is stored or retreived.
	attachments = {
	TacOpFlashlight  = "Tac Op Flashlight",
	Tac_Op_Flashlight  = "Tac_Op_Flashlight", -- the itemscript Provided or Atype ID
	},
}
table.insert(ISHotbarAttachDefinition, TacOpFlashlight);

local StanagMolleStorage = {
	type = "StanagMolleStorage",-- Name shown in the slot icon
	name = "Stanag Molle Storage", 
	animset = "back", -- Animation name 
	attachments = {
	GunMagazine = "StanagMolleStorage",
	Walkie = "StanagMolleStorage",
	MolleStorage = "StanagMolleStorage",
	},
}
table.insert(ISHotbarAttachDefinition, StanagMolleStorage);

local StanagMolleStorageMiddle = {
	type = "StanagMolleStorageMiddle",-- Name shown in the slot icon
	name = "Stanag Molle Storage Middle", 
	animset = "back", -- Animation name 
	attachments = {
	GunMagazine = "StanagMolleStorageMiddle",
	Walkie = "StanagMolleStorageMiddle",
	MolleStorage = "StanagMolleStorageMiddle",
	},
}
table.insert(ISHotbarAttachDefinition, StanagMolleStorageMiddle);

local StanagMolleStorageRight = {
	type = "StanagMolleStorageRight",-- Name shown in the slot icon
	name = "Stanag Molle Storage Right", 
	animset = "back", -- Animation name 
	attachments = {
	MolleStorage = "StanagMolleStorageRight",
	GunMagazine = "StanagMolleStorageRight",
	Walkie = "StanagMolleStorageRight",
	},
}
table.insert(ISHotbarAttachDefinition, StanagMolleStorageRight);

local OperatorVestMain = {
	type = "OperatorVestMain",-- Name shown in the slot icon
	name = "PRC Falcon Radio Slot", 
	animset = "back", -- Animation name 
	attachments = {
		Screwdriver = "OperatorVestMain",
		Knife = "OperatorVestMain",
		Walkie = "OperatorVestMain",
		OperatorVestMain = "OperatorVestMain",
	},
}
table.insert(ISHotbarAttachDefinition, OperatorVestMain);

local OperatorSlingMain = {
	type = "OperatorSlingMain",-- Name shown in the slot icon
	name = "DevTac Firearm AttachPoint", 
	animset = "back", -- Animation name 
	attachments = {
		Rifle = "OperatorSlingMain",
		BigBlade = "OperatorSlingMain",
		BigBonk = "OperatorSlingMain",
		BigWeapon = "OperatorSlingMain",
		Shovel = "OperatorSlingMain",
	},
}
table.insert(ISHotbarAttachDefinition, OperatorSlingMain);





-- TacOp_Chest_Holster Operator_GLXHeadGear_Mount
local HolsterChimeraDeltaR = {
	type = "HolsterChimeraDeltaR",-- Name shown in the slot icon  
	name = "Holster Mount Delta R", 
	animset = "holster right", -- Animation name 
	attachments = {
		Holster = "HolsterChimeraDeltaR",
	},
}
table.insert(ISHotbarAttachDefinition, HolsterChimeraDeltaR);

-- TacOp_Chest_Holster Operator_GLXHeadGear_Mount
local HolsterChimeraDeltaL = {
	type = "HolsterChimeraDeltaL",-- Name shown in the slot icon  
	name = "Holster Mount Delta L", 
	animset = "holster left", -- Animation name 
	attachments = {
		Holster = "HolsterChimeraDeltaL",
	},
}
table.insert(ISHotbarAttachDefinition, HolsterChimeraDeltaL);





--[[


-- TacOp_Chest_Holster Operator_GLXHeadGear_Mount
local HolsterMountDeltaR = {
	type = "HolsterMountDeltaR",-- Name shown in the slot icon  
	name = "Holster Mount Delta R", 
	animset = "holster right", -- Animation name 
	attachments = {
		Holster = "HolsterMountDeltaR",
	},
}
table.insert(ISHotbarAttachDefinition, HolsterMountDeltaR);

-- TacOp_Chest_Holster Operator_GLXHeadGear_Mount
local HolsterMountDeltaL = {
	type = "HolsterMountDeltaL",-- Name shown in the slot icon  
	name = "Holster Mount Delta L", 
	animset = "holster left", -- Animation name 
	attachments = {
		Holster = "HolsterMountDeltaL",
	},
}
table.insert(ISHotbarAttachDefinition, HolsterMountDeltaL);

--]]

local OperatorHolsterSide = {
	type = "OperatorHolsterSide",
	name = "OperatorHolsterSide",
	animset = "holster right",
	attachments = {
		Holster = "Ash Holster Side",
	},
}
table.insert(ISHotbarAttachDefinition, OperatorHolsterSide);

local OperatorHolsterSide = {
	type = "OperatorHolsterSide",
	name = "OperatorHolsterSide",
	animset = "holster right",
	attachments = {
		Holster = "Operator Holster Side",
	},
}
table.insert(ISHotbarAttachDefinition, OperatorHolsterSide);






local KydexVestMain = {
	type = "KydexVestMain",-- Name shown in the slot icon
	name = "PRC Falcon Radio Slot", 
	animset = "back", -- Animation name 
	attachments = {
		Screwdriver = "KydexVestMain",
		Knife = "KydexVestMain",
		Walkie = "KydexVestMain",
		TacOpFlashlight = "KydexVestMain",
		KydexVestMain = "KydexVestMain",
	},
}
table.insert(ISHotbarAttachDefinition, KydexVestMain);

local KydexSlingMain = {
	type = "KydexSlingMain",-- Name shown in the slot icon
	name = "DevTac Firearm AttachPoint",
	animset = "back", -- Animation name 
	attachments = {
		Rifle = "KydexSlingMain",
		BigBlade = "KydexSlingMain",
		BigBonk = "KydexSlingMain",
		BigWeapon = "KydexSlingMain",
		Shovel = "KydexSlingMain",
	},
}
table.insert(ISHotbarAttachDefinition, KydexSlingMain);

local ChestMountDT = {
	type = "ChestMountDT",-- Name shown in the slot icon
	name = "Delta Chest Holster",
	animset = "back", -- Animation name 
	attachments = {
		Holster = "ChestMountDT",
	},
}
table.insert(ISHotbarAttachDefinition, ChestMountDT);



local AshHolsterside = {
	type = "AshHolsterside",
	name = "AshHolsterside",
	animset = "holster right",
	attachments = {
		Holster = "Ash Holster Side",
	},
}
table.insert(ISHotbarAttachDefinition, AshHolsterside);

local KydexHolsterSide = {
	type = "KydexHolsterSide",
	name = "KydexHolsterSide",
	animset = "holster right",
	attachments = {
		Holster = "Kydex Holster Side",
	},
}
table.insert(ISHotbarAttachDefinition, KydexHolsterSide);