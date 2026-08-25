local MUGGY_ZoneDefinitions = {}

MUGGY_ZoneDefinitions.spawnZones = {
    {id = "MUGGY_ZONE_MULD1", minX = 11473, minY = 8819, maxX = 11462, maxY = 8802, enabled = true},
    {id = "MUGGY_ZONE_MULD2", minX = 10635, minY = 9386, maxX = 10624, maxY = 9375, enabled = true},
    {id = "MUGGY_ZONE_MULD3", minX = 10622, minY = 9505, maxX = 10627, maxY = 9509, enabled = true},
    {id = "MUGGY_ZONE_MULD4", minX = 10618, minY = 9654, maxX = 10606, maxY = 9637, enabled = true},
    {id = "MUGGY_ZONE_MULD5", minX = 10621, minY = 9896, maxX = 10607, maxY = 9870, enabled = true},
    {id = "MUGGY_ZONE_MULD6", minX = 10765, minY = 10370, maxX = 10747, maxY = 10344, enabled = true},
    {id = "MUGGY_ZONE_MULD7", minX = 10756, minY = 10542, maxX = 10767, maxY = 10554, enabled = true},
    {id = "MUGGY_ZONE_MULD8", minX = 10839, minY = 10020, maxX = 10852, maxY = 10032, enabled = true},

    {id = "MUGGY_ZONE_RW1", minX = 8362, minY = 10632, maxX = 8326, maxY = 11595, enabled = true},
    {id = "MUGGY_ZONE_RW2", minX = 8150, minY = 11490, maxX = 8128, maxY = 11467, enabled = true},
    {id = "MUGGY_ZONE_RW3", minX = 8080, minY = 11355, maxX = 8065, maxY = 11336, enabled = true},
    {id = "MUGGY_ZONE_RW4", minX = 8083, minY = 11466, maxX = 8069, maxY = 11446, enabled = true},
    {id = "MUGGY_ZONE_RW5", minX = 8094, minY = 11434, maxX = 8083, maxY = 11506, enabled = true},
    {id = "MUGGY_ZONE_RW6", minX = 8029, minY = 11490, maxX = 8015, maxY = 11423, enabled = true},

    {id = "MUGGY_ZONE_RVS1", minX = 6591, minY = 5223, maxX = 6598, maxY = 5228, enabled = true},
    {id = "MUGGY_ZONE_RVS2", minX = 6501, minY = 5230, maxX = 6486, maxY = 5218, enabled = true},
    {id = "MUGGY_ZONE_RVS3", minX = 6465, minY = 5215, maxX = 6481, maxY = 5228, enabled = true},
    {id = "MUGGY_ZONE_RVS4", minX = 6409, minY = 5259, maxX = 6397, maxY = 5248, enabled = true},
    {id = "MUGGY_ZONE_RVS5", minX = 6181, minY = 5367, maxX = 6195, maxY = 5299, enabled = true},
    {id = "MUGGY_ZONE_RVS6", minX = 6134, minY = 5314, maxX = 6115, maxY = 5218, enabled = true},
    {id = "MUGGY_ZONE_RVS7", minX = 5970, minY = 5433, maxX = 5959, maxY = 5414, enabled = true},
    {id = "MUGGY_ZONE_RVS8", minX = 5960, minY = 5266, maxX = 5953, maxY = 5259, enabled = true},

    {id = "MUGGY_ZONE_WP1", minX = 12082, minY = 7085, maxX = 12075, maxY = 7068, enabled = true},
    {id = "MUGGY_ZONE_WP2", minX = 12149, minY = 7042, maxX = 12132, maxY = 7014, enabled = true},
    {id = "MUGGY_ZONE_WP3", minX = 12047, minY = 6927, maxX = 12007, maxY = 6912, enabled = true},
    {id = "MUGGY_ZONE_WP4", minX = 11969, minY = 6874, maxX = 11959, maxY = 6887, enabled = true},

    {id = "MUGGY_ZONE_WP5", minX = 11492, minY = 6931, maxX = 11928, maxY = 6907, enabled = true},
    {id = "MUGGY_ZONE_WP6", minX = 11911, minY = 6858, maxX = 11902, maxY = 6844, enabled = true},
    {id = "MUGGY_ZONE_WP7", minX = 11987, minY = 6820, maxX = 11971, maxY = 6805, enabled = true},
    {id = "MUGGY_ZONE_WP8", minX = 11668, minY = 7090, maxX = 11654, maxY = 7081, enabled = true},
}

MUGGY_ZoneDefinitions.spawnSettings = {
    animalsPerSpawn = 1,
    checkInterval = 0.167,
    primarySpawn = {
        enabled = true,
        onceEverGlobally = true,
        disableOtherZonesAfterSpawn = true
    },
    contingentSpawn = {
        enabled = true,
        daysAfterPrimary = 7,
        hoursAfterPrimary = 168,
        failureRate = 0.25,
        allowOnlyOne = true
    }
}

function MUGGY_ZoneDefinitions.isPlayerInZone(playerX, playerY, zone)
    local actualMinX = math.min(zone.minX, zone.maxX)
    local actualMaxX = math.max(zone.minX, zone.maxX)
    local actualMinY = math.min(zone.minY, zone.maxY)
    local actualMaxY = math.max(zone.minY, zone.maxY)

    return playerX >= actualMinX and playerX <= actualMaxX and
           playerY >= actualMinY and playerY <= actualMaxY
end

function MUGGY_ZoneDefinitions.getApproxDistanceToZone(playerX, playerY, zone)
    local actualMinX = math.min(zone.minX, zone.maxX)
    local actualMaxX = math.max(zone.minX, zone.maxX)
    local actualMinY = math.min(zone.minY, zone.maxY)
    local actualMaxY = math.max(zone.minY, zone.maxY)

    local centerX = (actualMinX + actualMaxX) / 2
    local centerY = (actualMinY + actualMaxY) / 2
    return math.abs(playerX - centerX) + math.abs(playerY - centerY)
end

function MUGGY_ZoneDefinitions.getZoneById(zoneId)
    for _, zone in ipairs(MUGGY_ZoneDefinitions.spawnZones) do
        if zone.id == zoneId then
            return zone
        end
    end
    return nil
end

function MUGGY_ZoneDefinitions.getEnabledZones()
    local enabledZones = {}
    for _, zone in ipairs(MUGGY_ZoneDefinitions.spawnZones) do
        if zone.enabled then
            table.insert(enabledZones, zone)
        end
    end
    return enabledZones
end

return MUGGY_ZoneDefinitions
