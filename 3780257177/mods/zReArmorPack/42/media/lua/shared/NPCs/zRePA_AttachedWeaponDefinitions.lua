require "Definitions/AttachedWeaponDefinitions"

	AttachedWeaponDefinitions.zReMilitary_Weapon = {
		chance = 75,
		outfit = {"zRe_Military_4"},
		weaponLocation = {"Rifle On Back with Bag"},
		bloodLocations = nil,
		addHoles = false,
		daySurvived = 0,
		weapons = {
			"Base.HuntingRifle",
		},
	}
	AttachedWeaponDefinitions.attachedWeaponCustomOutfit.zRe_Military_4 = {
		chance = 75;
		maxitem = 1;
		weapons = {
			AttachedWeaponDefinitions.zReMilitary_Weapon,
		},
	}


	AttachedWeaponDefinitions.zReEXO_Weapon = {
		chance = 75,
		outfit = {"zRe_Military_exo1", "zRe_Military_exo2", "zRe_Military_exo3"},
		weaponLocation = {"Rifle On Back with Bag"},
		bloodLocations = nil,
		addHoles = false,
		daySurvived = 0,
		weapons = {
			"Base.Shotgun",
		},
	}
	AttachedWeaponDefinitions.attachedWeaponCustomOutfit.zRe_Military_exo1 = {
		chance = 75;
		maxitem = 1;
		weapons = {
			AttachedWeaponDefinitions.zReEXO_Weapon,
		},
	}
	AttachedWeaponDefinitions.attachedWeaponCustomOutfit.zRe_Military_exo2 = {
		chance = 75;
		maxitem = 1;
		weapons = {
			AttachedWeaponDefinitions.zReEXO_Weapon,
		},
	}
	AttachedWeaponDefinitions.attachedWeaponCustomOutfit.zRe_Military_exo3 = {
		chance = 75;
		maxitem = 1;
		weapons = {
			AttachedWeaponDefinitions.zReEXO_Weapon,
		},
	}




	AttachedWeaponDefinitions.zReTractor_Weapon = {
		chance = 75,
		outfit = {"zRe_Military_Tractor"},
		weaponLocation = {"Rifle On Back with Bag"},
		bloodLocations = nil,
		addHoles = false,
		daySurvived = 0,
		weapons = {
			"Base.AssaultRifle",
		},
	}
	AttachedWeaponDefinitions.attachedWeaponCustomOutfit.zRe_Military_Tractor = {
		chance = 75;
		maxitem = 1;
		weapons = {
			AttachedWeaponDefinitions.zReTractor_Weapon,
		},
	}