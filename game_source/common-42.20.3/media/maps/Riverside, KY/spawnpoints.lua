function SpawnPoints()
    local poor_houses = {
        { posX = 5739, posY = 5258, posZ = 0 },
        { posX = 5832, posY = 5233, posZ = 0 },
        { posX = 6021, posY = 5364, posZ = 0 },
        { posX = 6076, posY = 5375, posZ = 0 },
        { posX = 6117, posY = 5473, posZ = 0 },
        { posX = 6167, posY = 5412, posZ = 0 },
        { posX = 6443, posY = 5562, posZ = 0 },
        { posX = 6408, posY = 5498, posZ = 0 },
        { posX = 7342, posY = 5981, posZ = 0 },
        { posX = 7396, posY = 6017, posZ = 0 },
    }
    local medium_houses = {
        { posX = 5814, posY = 5233, posZ = 0 },
        { posX = 6081, posY = 5344, posZ = 0 },
        { posX = 6817, posY = 5259, posZ = 0 },
        { posX = 6067, posY = 5457, posZ = 0 },
        { posX = 6502, posY = 5517, posZ = 0 },
    }
    local rich_houses = {
        { posX = 6762, posY = 5372, posZ = 0 },
        { posX = 6327, posY = 5412, posZ = 0 },
        { posX = 6726, posY = 5514, posZ = 0 },
    }
    local doctor_houses = {
    {
        posX = 6658, posY = 5247, posZ = 1 },
    }
    local police_station = {
    {
        posX = 6119, posY = 5257, posZ = 0 },
    }
    local fire_station = {
    {
        posX = 6081, posY = 5255, posZ = 1 },
    }
    return {
        chef = mergeTable(poor_houses, medium_houses, rich_houses),
        constructionworker = poor_houses,
        doctor = mergeTable(medium_houses, rich_houses, doctor_houses),
        fireofficer = mergeTable(poor_houses, fire_station),
        nurse = poor_houses,
        parkranger = poor_houses,
        policeofficer = mergeTable(poor_houses, medium_houses, police_station),
        repairman = poor_houses,
        securityguard = poor_houses,
        unemployed = poor_houses,
        burglar = poor_houses,
        burgerflipper = poor_houses,
        carpenter = poor_houses,
        electrician = poor_houses,
        engineer = mergeTable(medium_houses, rich_houses),
        farmer = poor_houses,
        fisherman = poor_houses,
        fitnessInstructor = poor_houses,
        lumberjack = poor_houses,
        mechanics = poor_houses,
        metalworker = poor_houses,
        rancher = poor_houses,
        repairman = poor_houses,
        smither = poor_houses,
        tailor = poor_houses,
        veteran = poor_houses,
    }
end
