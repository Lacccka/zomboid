require 'Items/ProceduralDistributions'


table.insert(ProceduralDistributions.list.CampingLockers.items, "Base.MuggyHolotape");
table.insert(ProceduralDistributions.list.CampingLockers.items, 0.01);
table.insert(ProceduralDistributions.list.CampingStoreBooks.items, "Base.MuggyHolotape");
table.insert(ProceduralDistributions.list.CampingStoreBooks.items, 0.4);


table.insert(ProceduralDistributions.list.CrateBooks.items, "Base.MuggyHolotape");
table.insert(ProceduralDistributions.list.CrateBooks.items, 0.01);
table.insert(ProceduralDistributions.list.GunStoreLiterature.items, "Base.MuggyHolotape");
table.insert(ProceduralDistributions.list.GunStoreLiterature.items, 0.4);


table.insert(ProceduralDistributions.list.ArmyStorageOutfit.items, "Base.MuggyHolotape");
table.insert(ProceduralDistributions.list.ArmyStorageOutfit.items, 0.01);
table.insert(ProceduralDistributions.list.ArmySurplusTools.items, "Base.MuggyHolotape");
table.insert(ProceduralDistributions.list.ArmySurplusTools.items, 0.4);


table.insert(ProceduralDistributions.list.ArmyStorageOutfit.items, "Base.MuggyHolotape");
table.insert(ProceduralDistributions.list.ArmyStorageOutfit.items, 0.01);
table.insert(ProceduralDistributions.list.ArmySurplusTools.items, "Base.MuggyHolotape");
table.insert(ProceduralDistributions.list.ArmySurplusTools.items, 0.4);


table.insert(ProceduralDistributions.list.ArmyStorageOutfit.items, "Base.MuggyHolotape");
table.insert(ProceduralDistributions.list.ArmyStorageOutfit.items, 0.01);
table.insert(ProceduralDistributions.list.ArmySurplusTools.items, "Base.MuggyHolotape");
table.insert(ProceduralDistributions.list.ArmySurplusTools.items, 0.04);


table.insert(ProceduralDistributions.list.ArmyStorageOutfit.items, "Base.MuggyHolotape");
table.insert(ProceduralDistributions.list.ArmyStorageOutfit.items, 0.01);
table.insert(ProceduralDistributions.list.ArmySurplusTools.items, "Base.MuggyHolotape");
table.insert(ProceduralDistributions.list.ArmySurplusTools.items, 0.04);


table.insert(ProceduralDistributions.list.CampingStoreTools.items, "Base.MuggyHolotape");
table.insert(ProceduralDistributions.list.CampingStoreTools.items, 0.01);
table.insert(ProceduralDistributions.list.CampingStoreGear.items, "Base.MuggyHolotape");
table.insert(ProceduralDistributions.list.CampingStoreGear.items, 0.04);


table.insert(ProceduralDistributions.list.GiftStoreToys.items, "Base.MuggyHolotape");
table.insert(ProceduralDistributions.list.GiftStoreToys.items, 0.01);
table.insert(ProceduralDistributions.list.GigamartToys.items, "Base.MuggyHolotape");
table.insert(ProceduralDistributions.list.GigamartToys.items, 0.04);














local items = {
    "Base.MuggyHolotape",
}
function muggylootdistro(zombie)
    if (ZombRand(1000) <= 25) then
            local randomItem = items[ZombRand(1, #items)]
            local item = instanceItem(randomItem)
            local inv = zombie:getInventory()
            inv:getInventory():AddItem(item)
            sendAddItemToContainer(inv, item)
    end
end