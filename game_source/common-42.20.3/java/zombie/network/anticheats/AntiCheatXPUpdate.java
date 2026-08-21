/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.anticheats;

import zombie.SandboxOptions;
import zombie.characters.IsoPlayer;
import zombie.characters.skills.PerkFactory;
import zombie.characters.traits.CharacterTraits;
import zombie.core.raknet.UdpConnection;
import zombie.debug.DebugType;
import zombie.network.anticheats.AbstractAntiCheat;
import zombie.scripting.objects.CharacterTrait;

public class AntiCheatXPUpdate
extends AbstractAntiCheat {
    private static final float MIN_XP_MULTIPLIER = 1.0f;
    private static final int XP_BOOST_OPTION_1 = 1;
    private static final int XP_BOOST_OPTION_2 = 2;
    private static final int XP_BOOST_OPTION_3 = 3;
    private static final float XP_BOOST_VALUE_1 = 1.0f;
    private static final float XP_BOOST_VALUE_2 = 1.33f;
    private static final float XP_BOOST_VALUE_3 = 1.66f;
    private static final float XP_BOOST_DEFAULT_VALUE = 0.25f;
    private static final float XP_BOOST_TRAIT = 1.3f;

    private static float getMaxPerkXpBoostMultiplier(IsoPlayer player, PerkFactory.Perk perk) {
        int newXpBoost = player.getXp().getPerkBoost(perk);
        int oldXpBoost = player.getNetworkCharacterAI().setXpBoost(perk, newXpBoost);
        int maxXpBoost = Math.max(newXpBoost, oldXpBoost);
        float maxXpBoostMultiplier = switch (maxXpBoost) {
            case 1 -> 1.0f;
            case 2 -> 1.33f;
            case 3 -> 1.66f;
            default -> 0.25f;
        };
        CharacterTraits characterTraits = player.getCharacterTraits();
        if (characterTraits.get(CharacterTrait.FAST_LEARNER) && perk.getParent() != PerkFactory.Perks.PhysicalCategory) {
            maxXpBoostMultiplier *= 1.3f;
        }
        if (characterTraits.get(CharacterTrait.CRAFTY) && perk.getParent() == PerkFactory.Perks.Crafting) {
            maxXpBoostMultiplier *= 1.3f;
        }
        return maxXpBoostMultiplier;
    }

    private static double getMaxPerkXpMultiplier(IsoPlayer player, PerkFactory.Perk perk) {
        float newXpMultiplier = Math.max(1.0f, player.getXp().getMultiplier(perk));
        float oldXpMultiplier = Math.max(1.0f, player.getNetworkCharacterAI().setXpMultiplier(perk, newXpMultiplier));
        float maxXpMultiplier = Math.max(newXpMultiplier, oldXpMultiplier);
        if (SandboxOptions.instance.multipliersConfig.xpMultiplierGlobalToggle.getValue()) {
            return (double)maxXpMultiplier * Math.max(1.0, SandboxOptions.instance.multipliersConfig.xpMultiplierGlobal.getValue());
        }
        SandboxOptions.SandboxOption multiplierSandboxOption = SandboxOptions.instance.getOptionByName("MultiplierConfig." + perk.getName());
        if (multiplierSandboxOption != null) {
            return (double)maxXpMultiplier * Math.max(1.0, Double.parseDouble(multiplierSandboxOption.asConfigOption().getValueAsString()));
        }
        return maxXpMultiplier;
    }

    private static boolean isPerkXpGrowthRateTooHigh(IsoPlayer player, PerkFactory.Perk perk) {
        float maxPerkXpBoostMultiplier;
        double maxPerkXpMultiplier;
        double maxXpDelta;
        float oldXp;
        float newXp = player.getXp().getXP(perk);
        float xpDelta = newXp - (oldXp = player.getNetworkCharacterAI().setXp(perk, newXp));
        if ((double)xpDelta > (maxXpDelta = 1000.0 * (maxPerkXpMultiplier = AntiCheatXPUpdate.getMaxPerkXpMultiplier(player, perk)) * (double)(maxPerkXpBoostMultiplier = AntiCheatXPUpdate.getMaxPerkXpBoostMultiplier(player, perk)))) {
            DebugType.Multiplayer.error("perk %s xp growth is too high: xp=%f max-xp=%f multiplier=%f boost=%f", perk.getName(), Float.valueOf(xpDelta), maxXpDelta, maxPerkXpMultiplier, Float.valueOf(maxPerkXpBoostMultiplier));
            return true;
        }
        return false;
    }

    private static boolean isXpGrowthRateTooHigh(IsoPlayer player) {
        if (!player.getNetworkCharacterAI().isXpCheckerIntervalPassed()) {
            return false;
        }
        for (PerkFactory.Perk perk : PerkFactory.PerkList) {
            if (!AntiCheatXPUpdate.isPerkXpGrowthRateTooHigh(player, perk)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean update(UdpConnection connection) {
        super.update(connection);
        if (!this.antiCheat.isEnabled()) {
            return true;
        }
        for (IsoPlayer player : connection.players) {
            if (player == null || !AntiCheatXPUpdate.isXpGrowthRateTooHigh(player)) continue;
            return false;
        }
        return true;
    }
}

