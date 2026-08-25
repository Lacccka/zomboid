-- Copyright (c) 2026 ReapBone. All rights reserved.

BClan = BClan or {}

BClan.Config = {
    ServerName = "BONE > Project Zomboid",
    DefaultLanguage = "TR",
    DataKey = "BClan_Data_v1",
    NetworkModule = "BClan",
    Version = 3,

    MinNameLength = 3,
    MaxNameLength = 15,
    MinTagLength = 2,
    MaxTagLength = 4,

    MaxLevel = 10,
    BaseMemberLimit = 5,
    MembersPerLevel = 2,
    ZombieKillXP = 6,
    SurvivalHourXP = 1,
    BaseLevelXP = 650,
    MaxLevelBarXP = 10000,
    ProtectionRefreshTicks = 60,
    AutoSaveMinutes = 15,
}

function BClan.memberLimit(level)
    level = math.max(1, math.min(BClan.Config.MaxLevel, tonumber(level) or 1))
    return BClan.Config.BaseMemberLimit + ((level - 1) * BClan.Config.MembersPerLevel)
end

function BClan.xpForLevel(level)
    level = math.max(1, math.min(BClan.Config.MaxLevel, tonumber(level) or 1))
    if level >= BClan.Config.MaxLevel then
        return 0
    end
    return math.floor(BClan.Config.BaseLevelXP * (level ^ 1.35))
end

function BClan.trim(value)
    if value == nil then return "" end
    return tostring(value):gsub("^%s+", ""):gsub("%s+$", "")
end

function BClan.isValidName(value)
    value = BClan.trim(value)
    local length = #value
    if length < BClan.Config.MinNameLength or length > BClan.Config.MaxNameLength then
        return false
    end
    return value:match("^[%w%s_%-]+$") ~= nil
end

function BClan.isValidTag(value)
    value = BClan.trim(value):upper()
    local length = #value
    if length < BClan.Config.MinTagLength or length > BClan.Config.MaxTagLength then
        return false
    end
    return value:match("^[%w]+$") ~= nil
end

function BClan.safeUsername(value)
    value = BClan.trim(value)
    if #value < 1 or #value > 64 then return nil end
    return value
end
