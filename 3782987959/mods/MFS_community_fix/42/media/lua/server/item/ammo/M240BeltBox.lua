require 'Items/ProceduralDistributions'
require "Items/ItemPicker"

local distributions = {
    "GunStoreShelf",
    "PlankStashGun",
    "FirearmWeapons",
    "ArmyStorageGuns",
    "GunStoreCounter",
    "PoliceStorageGuns",
    "PawnShopGunsSpecial",
    "GunStoreDisplayCase",
    "GarageFirearms",
    "DrugLabGuns",
    "GunStoreAmmunition",
    "ArmyStorageAmmunition",
    "ArmySurplusCases",
    "LockerArmyBedroom",
    "LockerArmyBedroomHome",
    "ArmySurplusAmmoBoxes",
    "PoliceStorageAmmunition",
    "PrisonArmoryShotguns",
}

for _, distribution in ipairs(distributions) do
    table.insert(ProceduralDistributions["list"][distribution].items, "Base.M240BeltBox")
    table.insert(ProceduralDistributions["list"][distribution].items, 0.6)
end
