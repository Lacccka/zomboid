if isClient() then return end

local PZKZones = {}

function PZKZones.addZone(zone, ztype, x, y, z, w, h, dir, modID)
    if not zone then return end

    modID = modID or "PzkVanillaPlusCarPack"

    local createdZones = ModData.getOrCreate(modID).createdZones
    if not createdZones then
        createdZones = {}
        ModData.getOrCreate(modID).createdZones = createdZones
    end

    if ztype ~= "ParkingStall" then
        local typeTable = createdZones[ztype] or {}
        createdZones[ztype] = typeTable

        local key = string.format("%d,%d,%d", x, y, z)
        if typeTable[key] then return end
        typeTable[key] = true
    end

    if ztype == "ParkingStall" then
        getWorld():registerVehiclesZone(zone, ztype, x, y, z, w, h, {
            Direction = dir, FaceDirection = true
        })
    else
        getWorld():registerZone(zone, ztype, x, y, z, w, h)
    end
end

return PZKZones