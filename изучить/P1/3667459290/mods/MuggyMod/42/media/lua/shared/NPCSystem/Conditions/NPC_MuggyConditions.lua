local NPC_MuggyConditions = {}

NPC_MuggyConditions.TRADEABLE_ITEMS = {
    "Base.Mug",
    "Base.Mugl",
    "Base.MugWhite",
    "Base.MugRed",
    "Base.MugBlue",
    "Base.MugSpiffo",
    "Base.MugTeal",
    "Base.ClayMug",
    "Base.TestMug",
    "Base.ClayMugUnfired",
    "Base.ClayMugGlazedUnfired",
    "Base.Cup",
    "Base.CopperCup",
    "Base.GoldCup",
    "Base.SilverCup",
    "Base.Bowl",
    "Base.BowlCeramic",
    "Base.ClayBowl",
    "Base.ClayBowlUnfired",
    "Base.Plate",
    "Base.PlateCeramic",
    "Base.ClayPlate",
    "Base.ClayPlateUnfired",
    "Base.PlateGlazedUnfired",
    "Base.CeramicTeacup",
    "Base.Teacup",
    "Base.CeramicTeacupUnfired",
    "Base.CeramicTeacupGlazedUnfired",
    "Base.Whisk",
    "Base.CheeseGrater",
    "Base.Kettle",
    "Base.Kettle_Copper",
    "Base.Pan",
    "Base.BakingTray",
    "Base.Saucepan",
    "Base.SaucepanCopper",
    "Base.Strainer",
    "Base.GridlePan",
    "Base.RoastingPan",
    "Base.Flask",
    "Base.Cologne",
    "Base.DrinkingGlass",
    "Base.GlassChampagne",
    "Base.GlassTumbler",
    "Base.Pop2Empty",
    "Base.Pop3Empty",
    "Base.PopEmpty",
    "Base.TinCanEmpty",
    "Base.WaterRationCanEmpty",
    "Base.BeerCanEmpty",
    "Base.BeerEmpty",
    "Base.BreadKnife",
    "Base.ButterKnife",
    "Base.ButterKnife_Gold",
    "Base.ButterKnife_Silver",
    "Base.KnifeFillet",
    "Base.KitchenKnife",
    "Base.KitchenKnife_Forged",
    "Base.ParingKnife",
    "Base.PizzaCutter",
    "Base.SteakKnife",
    "Base.SushiKnife",
    "Base.PlasticKnife",
    "Base.Fork",
    "Base.ForkForged",
    "Base.Fork_Gold",
    "Base.Fork_Silver",
    "Base.PlasticFork",
    "Base.CarvingFork",
    "Base.Spoon",
    "Base.SpoonForged",
    "Base.Spoon_Gold",
    "Base.Spon_Silver",
    "Base.PlasticSpoon",
    "Base.Spatula",
    "Base.IcePick",
    "Base.KitchenTongs",
    "Base.Ladle",
    "Base.MuffinTray",
}

function NPC_MuggyConditions.hasAnyTradeableItem(player)
    if not player then
        return false
    end

    local inventory = player:getInventory()
    if not inventory then
        return false
    end

    for _, itemType in ipairs(NPC_MuggyConditions.TRADEABLE_ITEMS) do
        local item = inventory:getFirstTypeRecurse(itemType)
        if item then
            return true
        end
    end

    return false
end

function NPC_MuggyConditions.yes_mugs(player, npc)
    return NPC_MuggyConditions.hasAnyTradeableItem(player)
end

function NPC_MuggyConditions.no_mugs(player, npc)
    return not NPC_MuggyConditions.hasAnyTradeableItem(player)
end

function NPC_MuggyConditions.has_module(player, npc)
    local NPC_DialogueConditions = require("DialogueFramework/Dialogue/NPC_DialogueConditions")
    return NPC_DialogueConditions.hasModDataFlag(player, "muggy_hasmodule")
end

function NPC_MuggyConditions.no_module(player, npc)
    local NPC_DialogueConditions = require("DialogueFramework/Dialogue/NPC_DialogueConditions")
    return not NPC_DialogueConditions.hasModDataFlag(player, "muggy_hasmodule")
end

function NPC_MuggyConditions.can_receive_daily_gift(player, npc)
    local NPC_GiftingCooldownManager = require("DialogueFramework/Gifting/NPC_GiftingCooldownManager")
    return NPC_GiftingCooldownManager.canReceiveGift(player, "muggy")
end

function NPC_MuggyConditions.gift_on_cooldown(player, npc)
    return not NPC_MuggyConditions.can_receive_daily_gift(player, npc)
end

return NPC_MuggyConditions
