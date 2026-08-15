require "Items/ProceduralDistributions"

local explosives_Distributions = {
    Bag_AmmoBox_Grenades = {
        rolls = 6,
        items = {
            "Explosives.Mk2Grenade", 20.0,
            "Explosives.M26Grenade", 15.0,
            "Explosives.M67Grenade", 10.0,
            "Explosives.M14TH3Grenade", 5.0,
            "Explosives.M84Flashbang", 15.0,
        },
        junk = { rolls = 1, items = {} },
    },

    Bag_ProtectiveCaseBulkyGrenades = {
        rolls = 16,
        items = {
            "Explosives.Mk2Grenade", 20.0,
            "Explosives.M26Grenade", 15.0,
            "Explosives.M67Grenade", 10.0,
            "Explosives.M14TH3Grenade", 5.0,
            "Explosives.M84Flashbang", 15.0,
        },
        junk = { rolls = 1, items = {} },
    },

    Bag_ProtectiveCaseSmallMilitary_Grenades = {
        rolls = 4,
        items = {
            "Explosives.Mk2Grenade", 20.0,
            "Explosives.M26Grenade", 15.0,
            "Explosives.M67Grenade", 10.0,
            "Explosives.M14TH3Grenade", 5.0,
            "Explosives.M84Flashbang", 15.0,
        },
        junk = { rolls = 1, items = {} },
    },
}

local function injectLoot()
    local mult = SandboxVars.Explosives and SandboxVars.Explosives.LootMultiplier or 1.0

    -- Add mines to prot cases
    -- Bag_AmmoBox_Grenades
    table.insert(explosives_Distributions.Bag_AmmoBox_Grenades.items, "Explosives.M14Mine")
    table.insert(explosives_Distributions.Bag_AmmoBox_Grenades.items, 5.0)
    table.insert(explosives_Distributions.Bag_AmmoBox_Grenades.items, "Explosives.M18a1Claymore")
    table.insert(explosives_Distributions.Bag_AmmoBox_Grenades.items, 3.0)
    -- Bag_ProtectiveCaseBulkyGrenades
    table.insert(explosives_Distributions.Bag_ProtectiveCaseBulkyGrenades.items, "Explosives.M14Mine")
    table.insert(explosives_Distributions.Bag_ProtectiveCaseBulkyGrenades.items, 10.0)
    table.insert(explosives_Distributions.Bag_ProtectiveCaseBulkyGrenades.items, "Explosives.M18a1Claymore")
    table.insert(explosives_Distributions.Bag_ProtectiveCaseBulkyGrenades.items, 5.0)
    -- Bag_ProtectiveCaseSmallMilitary_Grenades
    table.insert(explosives_Distributions.Bag_ProtectiveCaseSmallMilitary_Grenades.items, "Explosives.M14Mine")
    table.insert(explosives_Distributions.Bag_ProtectiveCaseSmallMilitary_Grenades.items, 3.0)
    table.insert(explosives_Distributions.Bag_ProtectiveCaseSmallMilitary_Grenades.items, "Explosives.M18a1Claymore")
    table.insert(explosives_Distributions.Bag_ProtectiveCaseSmallMilitary_Grenades.items, 2.0)

    table.insert(Distributions, 2, explosives_Distributions)

    local bagsAndContainers = {
        
    }
    
    local outfitDistributions = {
        Bag_ALICE_BeltSus_Green = BagsAndContainers.ALICE_BeltSus,
        Bag_ALICE_BeltSus_Camo = BagsAndContainers.ALICE_BeltSus,
    }
    table.insert(Distributions, 2, outfitDistributions)

    local containersLow = {
        "GunStoreAccessories",
        "PoliceEvidence",
        "GarageFirearms"
    }
    local containersMedium = {
        "PoliceArmory",
        "SurvivalGear",
        "FirearmWeapons_Mid",
        "GunStoreAmmunition",
    }
    local containersHigh = {
        "ArmyStorageAmmunition",
        "ArmyStorageGuns",
        "FirearmWeapons_Late"
    }
    local containersBoxes = {
        "ArmySurplusAmmoBoxes",
        "ArmyStorageAmmunition",
        "ArmyStorageGuns",
        "FirearmWeapons_Late"
    }
    local containersClothing = {
        "ArmySurplusAmmoBoxes",
        "ArmyStorageAmmunition",
        "FirearmWeapons_Mid",
        "FirearmWeapons_Late",
        "ArmyStorageGuns",
        "PoliceArmory",
        "SurvivalGear",
    }

    local containersLiterature = {
        "ArmySurplusLiterature", 2.0,
        "GunStoreMagazineRack", 1.0,
        "ArmyStorageElectronics", 1.0,
        "CrateMagazines", 0.5,
        "PrisonCellRandom", 0.1,
        "PrisonCellRandomClassy", 0.1,
    }

    local itemsLow = {
        {"Explosives.Mk2Grenade", 0.1},
        {"Explosives.M26Grenade", 0.1},
        {"Explosives.M67Grenade", 0.05},
        {"Explosives.M14TH3Grenade", 0.05},
        {"Explosives.M84Flashbang", 0.1},
    }
    local itemsMedium = {
        {"Explosives.Mk2Grenade", 1.0},
        {"Explosives.M26Grenade", 1.0},
        {"Explosives.M67Grenade", 0.5},
        {"Explosives.M14TH3Grenade", 0.5},
        {"Explosives.M84Flashbang", 1.0},
    }
    local itemsHigh = {
        {"Explosives.Mk2Grenade", 5.0},
        {"Explosives.M26Grenade", 5.0},
        {"Explosives.M67Grenade", 3.0},
        {"Explosives.M14TH3Grenade", 3.0},
        {"Explosives.M84Flashbang", 5.0},
    }
    local itemsBoxes = {
        {"Explosives.Bag_ProtectiveCaseBulkyGrenades", 2.0},
        {"Explosives.Bag_AmmoBox_Grenades", 3.0},
        {"Explosives.Bag_ProtectiveCaseSmallMilitary_Grenades", 3.0},
    }
    local itemsClothing = {
        {"Explosives.GrenadeBandolier", 2.5},
    }
    local itemsLiterature = {
        {"Explosives.Magazine_MilitaryExplosives", 1.0},
    }

    table.insert(itemsMedium, {"Explosives.M14Mine", 1.0})
    table.insert(itemsMedium, {"Explosives.M18a1Claymore", 0.5})
    table.insert(itemsHigh, {"Explosives.M14Mine", 3.0})
    table.insert(itemsHigh, {"Explosives.M18a1Claymore", 2.0})

    local function inject(containers, items)
        for _, containerName in ipairs(containers) do
            local containerData = ProceduralDistributions.list[containerName]
            if containerData and containerData.items then
                for _, item in ipairs(items) do
                    table.insert(containerData.items, item[1])
                    table.insert(containerData.items, item[2] * mult)
                end
            end
        end
    end

    -- containersWeighted is a flat {name, weight, name, weight, ...} list -
    -- each container gets its own weight multiplier instead of sharing one
    local function injectWeighted(containersWeighted, items)
        for i = 1, #containersWeighted, 2 do
            local containerName = containersWeighted[i]
            local containerWeight = containersWeighted[i + 1]
            local containerData = ProceduralDistributions.list[containerName]
            if containerData and containerData.items then
                for _, item in ipairs(items) do
                    table.insert(containerData.items, item[1])
                    table.insert(containerData.items, item[2] * containerWeight)
                end
            end
        end
    end

    inject(containersLow, itemsLow)
    inject(containersMedium, itemsMedium)
    inject(containersHigh, itemsHigh)
    inject(containersBoxes, itemsBoxes)
    inject(containersClothing, itemsClothing)
    injectWeighted(containersLiterature, itemsLiterature)

end

Events.OnPreDistributionMerge.Add(function()
    injectLoot()
end)