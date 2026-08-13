if getActivatedMods():contains("StandardizedVehicleUpgradesCore") or getActivatedMods():contains("StandardizedVehicleUpgrades3Core") then

require 'Items/ProceduralDistributions'

-- This code allows for the magazine to spawn in the world.
-- Replace the item name to match the one in the script file!!!

table.insert(ProceduralDistributions.list["BookstoreBooks"].items, "Autotsar.ATA_PZK_Models_TuningMag");
table.insert(ProceduralDistributions.list["BookstoreBooks"].items, 0.5);
table.insert(ProceduralDistributions.list["CrateMagazines"].items, "Autotsar.ATA_PZK_Models_TuningMag");
table.insert(ProceduralDistributions.list["CrateMagazines"].items, 0.5);
table.insert(ProceduralDistributions.list["CrateMechanics"].items, "Autotsar.ATA_PZK_Models_TuningMag");
table.insert(ProceduralDistributions.list["CrateMechanics"].items, 0.5);
table.insert(ProceduralDistributions.list["LibraryBooks"].items, "Autotsar.ATA_PZK_Models_TuningMag");
table.insert(ProceduralDistributions.list["LibraryBooks"].items, 0.5);
table.insert(ProceduralDistributions.list["LivingRoomShelf"].items, "Autotsar.ATA_PZK_Models_TuningMag");
table.insert(ProceduralDistributions.list["LivingRoomShelf"].items, 0.1);
table.insert(ProceduralDistributions.list["LivingRoomShelfNoTapes"].items, "Autotsar.ATA_PZK_Models_TuningMag");
table.insert(ProceduralDistributions.list["LivingRoomShelfNoTapes"].items, 0.1);
table.insert(ProceduralDistributions.list["MagazineRackMixed"].items, "Autotsar.ATA_PZK_Models_TuningMag");
table.insert(ProceduralDistributions.list["MagazineRackMixed"].items, 0.5);
table.insert(ProceduralDistributions.list["MechanicShelfBooks"].items, "Autotsar.ATA_PZK_Models_TuningMag");
table.insert(ProceduralDistributions.list["MechanicShelfBooks"].items, 0.5);
table.insert(ProceduralDistributions.list["MechanicShelfBooks"].junk.items, "Autotsar.ATA_PZK_Models_TuningMag");
table.insert(ProceduralDistributions.list["MechanicShelfBooks"].junk.items, 0.5);
table.insert(ProceduralDistributions.list["PostOfficeMagazines"].items, "Autotsar.ATA_PZK_Models_TuningMag");
table.insert(ProceduralDistributions.list["PostOfficeMagazines"].items, 0.5);
table.insert(ProceduralDistributions.list["ShelfGeneric"].items, "Autotsar.ATA_PZK_Models_TuningMag");
table.insert(ProceduralDistributions.list["ShelfGeneric"].items, 0.1);
table.insert(ProceduralDistributions.list["GarageMetalwork"].items, "Autotsar.ATA_PZK_Models_TuningMag");
table.insert(ProceduralDistributions.list["GarageMetalwork"].items, 0.1);
table.insert(ProceduralDistributions.list["StoreShelfMechanics"].items, "Autotsar.ATA_PZK_Models_TuningMag");
table.insert(ProceduralDistributions.list["StoreShelfMechanics"].items, 0.5);
table.insert(ProceduralDistributions.list["ToolStoreBooks"].items, "Autotsar.ATA_PZK_Models_TuningMag");
table.insert(ProceduralDistributions.list["ToolStoreBooks"].items, 0.5);
table.insert(ProceduralDistributions.list["BookstoreMisc"].items, "Autotsar.ATA_PZK_Models_TuningMag");
table.insert(ProceduralDistributions.list["BookstoreMisc"].items, 0.5);
table.insert(ProceduralDistributions.list["CampingStoreBooks"].items, "Autotsar.ATA_PZK_Models_TuningMag");
table.insert(ProceduralDistributions.list["CampingStoreBooks"].items, 0.5);
table.insert(ProceduralDistributions.list["JanitorMisc"].items, "Autotsar.ATA_PZK_Models_TuningMag");
table.insert(ProceduralDistributions.list["JanitorMisc"].items, 0.5);

end