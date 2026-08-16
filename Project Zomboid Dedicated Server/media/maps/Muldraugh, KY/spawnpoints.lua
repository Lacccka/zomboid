function SpawnPoints()
    local poor_houses = {
        { posX = 10770, posY = 10271, posZ = 0 },
        { posX = 10637, posY = 10267, posZ = 0 },
        { posX = 10720, posY = 10195, posZ = 0 },
        { posX = 10997, posY = 9699, posZ = 0 },
        { posX = 10819, posY = 9437, posZ = 0 },
        { posX = 10695, posY = 9383, posZ = 0 },
        { posX = 10776, posY = 9764, posZ = 0 },
        { posX = 10911, posY = 10037, posZ = 0 },
        { posX = 10721, posY = 10630, posZ = 0 },
        { posX = 10718, posY = 9989, posZ = 0 },
    }
    local medium_houses = {
        { posX = 11018, posY = 9419, posZ = 0 },
        { posX = 10656, posY = 10137, posZ = 0 },
        { posX = 10810, posY = 10037, posZ = 0 },
        { posX = 10820, posY = 9419, posZ = 0 },
        { posX = 10654, posY = 9371, posZ = 0 },
    }
    local rich_houses = {
        { posX = 10715, posY = 9532, posZ = 0 },
        { posX = 10919, posY = 10137, posZ = 0 },
        { posX = 10754, posY = 10214, posZ = 0 },
    }
    local doctor_houses = {
        { posX = 10878, posY = 10021, posZ = 0 },
        { posX = 10863, posY = 10042, posZ = 0 },
    }
    local police_station = {
        { posX = 10637, posY = 10418, posZ = 0 },
    }
    return {
        chef = mergeTable(poor_houses, medium_houses, rich_houses),
        constructionworker = poor_houses,
        doctor = mergeTable(medium_houses, rich_houses, doctor_houses),
        fireofficer = poor_houses,
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
