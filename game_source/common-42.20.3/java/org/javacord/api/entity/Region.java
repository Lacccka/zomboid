/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity;

import org.javacord.api.entity.Nameable;

public enum Region implements Nameable
{
    AMSTERDAM("amsterdam", "Amsterdam", false),
    BRAZIL("brazil", "Brazil", false),
    EUROPE("europe", "Europe", false),
    EU_WEST("eu-west", "EU West", false),
    EU_CENTRAL("eu-central", "EU Central", false),
    FRANKFURT("frankfurt", "Frankfurt", false),
    HONG_KONG("hongkong", "Hong Kong", false),
    JAPAN("japan", "Japan", false),
    LONDON("london", "London", false),
    RUSSIA("russia", "Russia", false),
    INDIA("india", "India", false),
    SINGAPORE("singapore", "Singapore", false),
    SYDNEY("sydney", "Sydney", false),
    US_EAST("us-east", "US East", false),
    US_WEST("us-west", "US West", false),
    US_CENTRAL("us-central", "US Central", false),
    US_SOUTH("us-south", "US South", false),
    VIP_AMSTERDAM("vip-amsterdam", "Amsterdam (VIP)", true),
    VIP_BRAZIL("vip-brazil", "Brazil (VIP)", true),
    VIP_EU_WEST("vip-eu-west", "EU West (VIP)", true),
    VIP_EU_CENTRAL("vip-eu-central", "EU Central (VIP)", true),
    VIP_FRANKFURT("vip-frankfurt", "Frankfurt (VIP)", true),
    VIP_LONDON("vip-london", "London (VIP)", true),
    VIP_SINGAPORE("vip-singapore", "Singapore (VIP)", true),
    VIP_SYDNEY("vip-sydney", "Sydney (VIP)", true),
    VIP_US_EAST("vip-us-east", "US East (VIP)", true),
    VIP_US_WEST("vip-us-west", "US West (VIP)", true),
    VIP_US_CENTRAL("vip-us-central", "US Central (VIP)", true),
    VIP_US_SOUTH("vip-us-south", "US South (VIP)", true),
    UNKNOWN("us-west", "Unknown", false);

    private final String key;
    private final String name;
    private final boolean vip;

    private Region(String key, String name, boolean vip) {
        this.key = key;
        this.name = name;
        this.vip = vip;
    }

    public String getKey() {
        return this.key;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public boolean isVip() {
        return this.vip;
    }

    public static Region getRegionByKey(String key) {
        for (Region region : Region.values()) {
            if (!region.getKey().equalsIgnoreCase(key) || region == UNKNOWN) continue;
            return region;
        }
        return UNKNOWN;
    }
}

