local NPC_TradingItemTables = {}

NPC_TradingItemTables.acceptableItems = {
    ["Base.Mugl"] = {
        itemType = "Base.Mugl",
        displayName = "Coffee Mug",
        acceptQuantity = 1,
        takeAll = true,
        value = 30
    },
    ["Base.MugWhite"] = {
        itemType = "Base.MugWhite",
        displayName = "Coffee Mug",
        acceptQuantity = 1,
        takeAll = true,
        value = 30
    },
    ["Base.MugSpiffo"] = {
        itemType = "Base.MugSpiffo",
        displayName = "Spiffo Mug",
        acceptQuantity = 1,
        takeAll = true,
        value = 65
    },
    ["Base.ClayBowl"] = {
        itemType = "Base.ClayBowl",
        displayName = "Clay Bowl",
        acceptQuantity = 1,
        takeAll = true,
        value = 15
    },
    ["Base.CopperCup"] = {
        itemType = "Base.CopperCup",
        displayName = "Copper Cup",
        acceptQuantity = 1,
        takeAll = true,
        value = 32
    },
    ["Base.ClayMug"] = {
        itemType = "Base.ClayMug",
        displayName = "Coffee Mug",
        acceptQuantity = 1,
        takeAll = true,
        value = 30
    },

    ["Base.GoldCup"] = {
        itemType = "Base.GoldCup",
        displayName = "Gold Mug",
        acceptQuantity = 1,
        takeAll = true,
        value = 320
    },
    ["Base.SilverCup"] = {
        itemType = "Base.SilverCup",
        displayName = "Silver Mug",
        acceptQuantity = 1,
        takeAll = true,
        value = 125
    },
    ["Base.CeramicTeacup"] = {
        itemType = "Base.CeramicTeacup",
        displayName = "Teacup",
        acceptQuantity = 1,
        takeAll = true,
        value = 16
    },
    ["Base.Teacup"] = {
        itemType = "Base.Teacup",
        displayName = "Teacup",
        acceptQuantity = 1,
        takeAll = true,
        value = 16
    },
    ["Base.Whisk"] = {
        itemType = "Base.Whisk",
        displayName = "Wisk",
        acceptQuantity = 1,
        takeAll = true,
        value = 15
    },
    ["Base.CheeseGrater"] = {
        itemType = "Base.CheeseGrater",
        displayName = "Cheese Grater",
        acceptQuantity = 1,
        takeAll = true,
        value = 20
    },
    ["Base.Kettle_Copper"] = {
        itemType = "Base.Kettle_Copper",
        displayName = "Kettle",
        acceptQuantity = 1,
        takeAll = true,
        value = 26
    },
    ["Base.TestMug"] = {
        itemType = "Base.TestMug",
        displayName = "Coffee Mug",
        acceptQuantity = 1,
        takeAll = true,
        value = 35
    },
    ["Base.Kettle"] = {
        itemType = "Base.Kettle",
        displayName = "Kettle",
        acceptQuantity = 1,
        takeAll = true,
        value = 26
    },
    ["Base.Pan"] = {
        itemType = "Base.BakingPan",
        displayName = "Pan",
        acceptQuantity = 1,
        takeAll = true,
        value = 24
    },
    ["Base.BakingTray"] = {
        itemType = "Base.BakingTray",
        displayName = "Pan",
        acceptQuantity = 1,
        takeAll = true,
        value = 24
    },
    ["Base.Saucepan"] = {
        itemType = "Base.Saucepan",
        displayName = "Saucepan",
        acceptQuantity = 1,
        takeAll = true,
        value = 24
    },
    ["Base.Strainer"] = {
        itemType = "Base.Strainer",
        displayName = "Strainer",
        acceptQuantity = 1,
        takeAll = true,
        value = 18
    },
    ["Base.SaucepanCopper"] = {
        itemType = "Base.SaucepanCopper",
        displayName = "Saucepan",
        acceptQuantity = 1,
        takeAll = true,
        value = 24
    },
    ["Base.GridlePan"] = {
        itemType = "Base.GridlePan",
        displayName = "Griddle Pan",
        acceptQuantity = 1,
        takeAll = true,
        value = 22
    },
    ["Base.RoastingPan"] = {
        itemType = "Base.RoastingPan",
        displayName = "Roasting Pan",
        acceptQuantity = 1,
        takeAll = true,
        value = 25
    },
    ["Base.Flask"] = {
        itemType = "Base.Flask",
        displayName = "Flask",
        acceptQuantity = 1,
        takeAll = true,
        value = 15
    },
    ["Base.Cologne"] = {
        itemType = "Base.Cologne",
        displayName = "Cologne",
        acceptQuantity = 1,
        takeAll = true,
        value = 15
    },
    ["Base.DrinkingGlass"] = {
        itemType = "Base.DrinkingGlass",
        displayName = "Drinking Glass",
        acceptQuantity = 1,
        takeAll = true,
        value = 14
    },
    ["Base.GlassChampagne"] = {
        itemType = "Base.GlassChampagne",
        displayName = "Champagne Glass",
        acceptQuantity = 1,
        takeAll = true,
        value = 17
    },
    ["Base.GlassTumbler"] = {
        itemType = "Base.Tumbler",
        displayName = "Glass Tumbler",
        acceptQuantity = 1,
        takeAll = true,
        value = 19
    },
    ["Base.Pop2Empty"] = {
        itemType = "Base.Pop2Empty",
        displayName = "Pop Can",
        acceptQuantity = 1,
        takeAll = true,
        value = 12
    },
    ["Base.Pop3Empty"] = {
        itemType = "Base.Pop3Empty",
        displayName = "Pop Can",
        acceptQuantity = 1,
        takeAll = true,
        value = 12
    },
    ["Base.TinCanEmpty"] = {
        itemType = "Base.TinCanEmpty",
        displayName = "Empty Can",
        acceptQuantity = 1,
        takeAll = true,
        value = 16
    },
    ["Base.WaterRationCanEmpty"] = {
        itemType = "Base.WaterRationCanEmpty",
        displayName = "Empty Can",
        acceptQuantity = 1,
        takeAll = true,
        value = 17
    },
    ["Base.BeerCanEmpty"] = {
        itemType = "Base.BeerCanEmpty",
        displayName = "Empty Can",
        acceptQuantity = 1,
        takeAll = true,
        value = 13
    },
    ["Base.BeerEmpty"] = {
        itemType = "Base.BeerEmpty",
        displayName = "Empty Can",
        acceptQuantity = 1,
        takeAll = true,
        value = 13
    },
    ["Base.PopEmpty"] = {
        itemType = "Base.PopEmpty",
        displayName = "Empty Can",
        acceptQuantity = 1,
        takeAll = true,
        value = 12
    },
    ["Base.ClayPlate"] = {
        itemType = "Base.ClayPlate",
        displayName = "Plate",
        acceptQuantity = 1,
        takeAll = true,
        value = 17
    },
    ["Base.ClayMugUnfired"] = {
        itemType = "Base.ClayMugUnfired",
        displayName = "Unfired Mug",
        acceptQuantity = 1,
        takeAll = true,
        value = 25
    },
    ["Base.ClayBowlUnfired"] = {
        itemType = "Base.ClayBowlUnfired",
        displayName = "Unfired Bowl",
        acceptQuantity = 1,
        takeAll = true,
        value = 15
    },
    ["Base.ClayPlateUnfired"] = {
        itemType = "Base.ClayPlateUnfired",
        displayName = "Unfired Plate",
        acceptQuantity = 1,
        takeAll = true,
        value = 20
    },
    ["Base.CeramicTeacupUnfired"] = {
        itemType = "Base.CeramicTeacupUnfired",
        displayName = "Unfired Teacup",
        acceptQuantity = 1,
        takeAll = true,
        value = 13
    },
    ["Base.ClayMugGlazedUnfired"] = {
        itemType = "Base.ClayMugGlazedUnfired",
        displayName = "Glazed Unfired Mug",
        acceptQuantity = 1,
        takeAll = true,
        value = 27
    },
    ["Base.PlateGlazedUnfired"] = {
        itemType = "Base.PlateGlazedUnfired",
        displayName = "Glazed Unfired Plate",
        acceptQuantity = 1,
        takeAll = true,
        value = 20
    },
    ["Base.CeramicTeacupGlazedUnfired"] = {
        itemType = "Base.CeramicTeacupGlazedUnfired",
        displayName = "Glazed Unfired Teacup",
        acceptQuantity = 1,
        takeAll = true,
        value = 16
    },
    ["Base.Bowl"] = {
        itemType = "Base.Bowl",
        displayName = "Bowl",
        acceptQuantity = 1,
        takeAll = true,
        value = 12
    },
    ["Base.Plate"] = {
        itemType = "Base.Plate",
        displayName = "Plate",
        acceptQuantity = 1,
        takeAll = true,
        value = 22
    },




    ["Base.BreadKnife"] = {
        itemType = "Base.BreadKnife",
        displayName = "Bread Knife",
        acceptQuantity = 1,
        takeAll = true,
        value = 12
    },
    ["Base.ButterKnife"] = {
        itemType = "Base.ButterKnife",
        displayName = "Butter Knife",
        acceptQuantity = 1,
        takeAll = true,
        value = 12
    },
    ["Base.KnifeFillet"] = {
        itemType = "Base.KnifeFillet",
        displayName = "Fillet Knife",
        acceptQuantity = 1,
        takeAll = true,
        value = 15
    },
    ["Base.ButterKnife_Gold"] = {
        itemType = "Base.ButterKnife_Gold",
        displayName = "Gold Butter Knife",
        acceptQuantity = 1,
        takeAll = true,
        value = 100
    },
    ["Base.KitchenKnife"] = {
        itemType = "Base.KitchenKnife",
        displayName = "Kitchen Knife",
        acceptQuantity = 1,
        takeAll = true,
        value = 17
    },
    ["Base.KitchenKnife_Forged"] = {
        itemType = "Base.KitchenKnife_Forged",
        displayName = "Kitchen Knife",
        acceptQuantity = 1,
        takeAll = true,
        value = 17
    },
    ["Base.ParingKnife"] = {
        itemType = "Base.ParingKnife",
        displayName = "Bowl",
        acceptQuantity = 1,
        takeAll = true,
        value = 13
    },
    ["Base.PizzaCutter"] = {
        itemType = "Base.PizzaCutter",
        displayName = "Pizza Cutter",
        acceptQuantity = 1,
        takeAll = true,
        value = 17
    },
    ["Base.SteakKnife"] = {
        itemType = "Base.SteakKnife",
        displayName = "Steak Knife",
        acceptQuantity = 1,
        takeAll = true,
        value = 20
    },
    ["Base.SushiKnife"] = {
        itemType = "Base.SushiKnife",
        displayName = "Sushi Knife",
        acceptQuantity = 1,
        takeAll = true,
        value = 20
    },
    ["Base.ButterKnife_Silver"] = {
        itemType = "Base.ButterKnife_Silver",
        displayName = "Silver Butter Knife",
        acceptQuantity = 1,
        takeAll = true,
        value = 40
    },
    ["Base.PlasticKnife"] = {
        itemType = "Base.PlasticKnife",
        displayName = "Plastic Knife",
        acceptQuantity = 1,
        takeAll = true,
        value = 12
    },
    ["Base.ForkForged"] = {
        itemType = "Base.ForkForged",
        displayName = "Forged Fork",
        acceptQuantity = 1,
        takeAll = true,
        value = 16
    },
    ["Base.SpoonForged"] = {
        itemType = "Base.SpoonForged",
        displayName = "Forged SpoonForged",
        acceptQuantity = 1,
        takeAll = true,
        value = 16
    },
    ["Base.Fork"] = {
        itemType = "Base.Fork",
        displayName = "Fork",
        acceptQuantity = 1,
        takeAll = true,
        value = 12
    },
    ["Base.Fork_Gold"] = {
        itemType = "Base.Fork_Gold",
        displayName = "Gold Fork",
        acceptQuantity = 1,
        takeAll = true,
        value = 150
    },

    ["Base.Spoon_Gold"] = {
        itemType = "Base.Spoon_Gold",
        displayName = "Gold SpoonForged",
        acceptQuantity = 1,
        takeAll = true,
        value = 150
    },
    ["Base.PlasticFork"] = {
        itemType = "Base.PlasticFork",
        displayName = "Plastic Fork",
        acceptQuantity = 1,
        takeAll = true,
        value = 12
    },
    ["Base.PlasticSpoon"] = {
        itemType = "Base.PlasticSpoon",
        displayName = "Plastic Spoon",
        acceptQuantity = 1,
        takeAll = true,
        value = 12
    },
    ["Base.Fork_Silver"] = {
        itemType = "Base.Fork_Silver",
        displayName = "Silver Fork",
        acceptQuantity = 1,
        takeAll = true,
        value = 60
    },

    ["Base.Spon_Silver"] = {
        itemType = "Base.Spoon_Silver",
        displayName = "Silver Spoon",
        acceptQuantity = 1,
        takeAll = true,
        value = 60
    },
    ["Base.Spatula"] = {
        itemType = "Base.Spatula",
        displayName = "Spatula",
        acceptQuantity = 1,
        takeAll = true,
        value = 20
    },
    ["Base.Spoon"] = {
        itemType = "Base.Spoon",
        displayName = "Spoon",
        acceptQuantity = 1,
        takeAll = true,
        value = 12
    },
    ["Base.CarvingFork"] = {
        itemType = "Base.Carving Fork",
        displayName = "Carving Fork",
        acceptQuantity = 1,
        takeAll = true,
        value = 15
    },

    ["Base.IcePick"] = {
        itemType = "Base.SteakKnife",
        displayName = "Steak Knife",
        acceptQuantity = 1,
        takeAll = true,
        value = 16
    },
    ["Base.KitchenTongs"] = {
        itemType = "Base.KitchenTongs",
        displayName = "Tongs",
        acceptQuantity = 1,
        takeAll = true,
        value = 26
    },
    ["Base.Ladle"] = {
        itemType = "Base.Ladle",
        displayName = "Ladle",
        acceptQuantity = 1,
        takeAll = true,
        value = 16
    },
    ["Base.MuffinTray"] = {
        itemType = "Base.MuffinTray",
        displayName = "Muffin Tray",
        acceptQuantity = 1,
        takeAll = true,
        value = 20
    },
}

NPC_TradingItemTables.rewardItems = {
    {
        itemType = "Base.AluminumFragments",
        displayName = "Aluminum Fragments",
        weight = 6,
        minQuantity = 1,
        maxQuantity = 5,
        value = 6
    },
    {
        itemType = "Base.IronPiece",
        displayName = "Iron Piece",
        weight = 3,
        minQuantity = 1,
        maxQuantity = 7,
        value = 3
    },
    {
        itemType = "Base.AluminumScrap",
        displayName = "Aluminum Scrap",
        weight = 8,
        minQuantity = 1,
        maxQuantity = 7,
        value = 8
    },
    {
        itemType = "Base.IronScrap",
        displayName = "Iron Scrap",
        weight = 10,
        minQuantity = 1,
        maxQuantity = 6,
        value = 10
    },
    {
        itemType = "Base.ScrapMetal",
        displayName = "Scrap Metal",
        weight = 10,
        minQuantity = 1,
        maxQuantity = 5,
        value = 10
    },
    {
        itemType = "Base.SteelPiece",
        displayName = "Steel Piece",
        weight = 6,
        minQuantity = 1,
        maxQuantity = 8,
        value = 6
    },
    {
        itemType = "Base.BrassScrap",
        displayName = "Brass Scrap",
        weight = 12,
        minQuantity = 1,
        maxQuantity = 4,
        value = 12
    },
    {
        itemType = "Base.SmallSheetMetal",
        displayName = "Small Steel Sheet",
        weight = 45,
        minQuantity = 1,
        maxQuantity = 3,
        value = 45
    },
    {
        itemType = "Base.SteelScrap",
        displayName = "Steel Scrap",
        weight = 16,
        minQuantity = 1,
        maxQuantity = 5,
        value = 16
    },
    {
        itemType = "Base.IronChunk",
        displayName = "Iron Chunk",
        weight = 50,
        minQuantity = 1,
        maxQuantity = 3,
        value = 50
    },
    {
        itemType = "Base.CopperScrap",
        displayName = "Copper Scrap",
        weight = 25,
        minQuantity = 1,
        maxQuantity = 5,
        value = 25
    },
    {
        itemType = "Base.SheetMetal",
        displayName = "Steel Sheet",
        weight = 70,
        minQuantity = 1,
        maxQuantity = 2,
        value = 70
    },
    {
        itemType = "Base.SteelChunk",
        displayName = "Steel Chunk",
        weight = 75,
        minQuantity = 1,
        maxQuantity = 2,
        value = 75
    },
    {
        itemType = "Base.Katana_Shard",
        displayName = "Katana Shard",
        weight = 70,
        minQuantity = 1,
        maxQuantity = 1,
        value = 70
    },
    {
        itemType = "Base.SpearLongHead",
        displayName = "Long Metal Spearhead",
        weight = 150,
        minQuantity = 1,
        maxQuantity = 1,
        value = 150
    },
    {
        itemType = "Base.GoldScrap",
        displayName = "Gold Scrap",
        weight = 25,
        minQuantity = 1,
        maxQuantity = 1,
        value = 25
    },
    {
        itemType = "Base.GoldBar",
        displayName = "Gold Ingot",
        weight = 1500,
        minQuantity = 1,
        maxQuantity = 1,
        value = 1500
    },
}

NPC_TradingItemTables.acceptableItems_System2 = {
    ["Base.Mug"] = {
        itemType = "Base.Mug",
        displayName = "Coffee Mug",
        value = 5,
        stackable = true
    },
    ["Base.Bowl"] = {
        itemType = "Base.Bowl",
        displayName = "Bowl",
        value = 8,
        stackable = true
    },
    ["Base.Plate"] = {
        itemType = "Base.Plate",
        displayName = "Plate",
        value = 6,
        stackable = true
    },
    ["Base.Fork"] = {
        itemType = "Base.Fork",
        displayName = "Fork",
        value = 3,
        stackable = true
    }
}

NPC_TradingItemTables.rewardItems_System2 = {
    {
        itemType = "Base.Plasticbag",
        displayName = "Plastic Bag",
        value = 10,
        quantity = 1,
        iconTexture = nil
    },
    {
        itemType = "Base.ScrapMetal",
        displayName = "Scrap Metal",
        value = 25,
        quantity = 1,
        iconTexture = nil
    },
    {
        itemType = "Base.ElectronicsScrap",
        displayName = "Electronics Scrap",
        value = 50,
        quantity = 1,
        iconTexture = nil
    },
    {
        itemType = "Base.WeldingRods",
        displayName = "Welding Rods",
        value = 75,
        quantity = 5,
        iconTexture = nil
    }
}

function NPC_TradingItemTables.isAcceptableItem(itemType, system)
    system = system or 1
    if system == 2 then
        return NPC_TradingItemTables.acceptableItems_System2[itemType] ~= nil
    else
        return NPC_TradingItemTables.acceptableItems[itemType] ~= nil
    end
end

function NPC_TradingItemTables.getAcceptableItems(system)
    system = system or 1
    if system == 2 then
        return NPC_TradingItemTables.acceptableItems_System2
    else
        return NPC_TradingItemTables.acceptableItems
    end
end

function NPC_TradingItemTables.selectRewardItem(totalValue)
    totalValue = totalValue or 0

    local affordableRewards = {}
    for _, item in ipairs(NPC_TradingItemTables.rewardItems) do
        if totalValue >= item.value then
            table.insert(affordableRewards, item)
        end
    end

    if #affordableRewards == 0 then
        return nil, 0
    end

    local totalWeight = 0
    for _, item in ipairs(affordableRewards) do
        totalWeight = totalWeight + item.weight
    end

    local roll = ZombRand(totalWeight)
    local currentWeight = 0

    for _, item in ipairs(affordableRewards) do
        currentWeight = currentWeight + item.weight
        if roll < currentWeight then
            local quantity = ZombRand(item.minQuantity, item.maxQuantity + 1)
            return item.itemType, quantity
        end
    end

    return nil, 0
end

function NPC_TradingItemTables.getItemValue(itemType, system)
    system = system or 1
    local items
    if system == 2 then
        items = NPC_TradingItemTables.acceptableItems_System2
    else
        items = NPC_TradingItemTables.acceptableItems
    end

    local itemData = items[itemType]
    if itemData then
        return itemData.value or 0
    end
    return 0
end

function NPC_TradingItemTables.calculateTotalValue(items, system)
    system = system or 2
    local total = 0
    for itemType, quantity in pairs(items) do
        local value = NPC_TradingItemTables.getItemValue(itemType, system)
        total = total + (value * quantity)
    end
    return total
end

function NPC_TradingItemTables.getAffordableRewards(currentValue)
    local affordable = {}
    for _, reward in ipairs(NPC_TradingItemTables.rewardItems_System2) do
        if currentValue >= reward.value then
            table.insert(affordable, reward)
        end
    end
    return affordable
end

function NPC_TradingItemTables.loadRewardTextures()
    for _, reward in ipairs(NPC_TradingItemTables.rewardItems_System2) do
        if not reward.iconTexture then
            local item = InventoryItemFactory.CreateItem(reward.itemType)
            if item then
                reward.iconTexture = item:getNormalTexture()
            end
        end
    end
end

return NPC_TradingItemTables
