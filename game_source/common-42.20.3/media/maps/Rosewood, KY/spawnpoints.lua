function SpawnPoints()
    local poor_houses = {
        { posX = 7976, posY = 11402, posZ = 0 },
        { posX = 7822, posY = 11286, posZ = 0 },
        { posX = 8035, posY = 11560, posZ = 0 },
        { posX = 8078, posY = 11547, posZ = 1 },
        { posX = 8037, posY = 11440, posZ = 1 },
        { posX = 8303, posY = 11689, posZ = 0 },
        { posX = 8495, posY = 11550, posZ = 0 },
        { posX = 7989, posY = 11755, posZ = 0 },
        { posX = 8114, posY = 12223, posZ = 0 },
        { posX = 8431, posY = 12135, posZ = 0 }
    }
    local medium_houses = {
        { posX = 7911, posY = 11409, posZ = 1 },
        { posX = 7995, posY = 11414, posZ = 0 },
        { posX = 8284, posY = 11721, posZ = 1 },
        { posX = 8446, posY = 11729, posZ = 0 },
        { posX = 8168, posY = 12394, posZ = 1 }
    }
    local rich_houses = {
        { posX = 8197, posY = 11557, posZ = 1 },
        { posX = 8469, posY = 11558, posZ = 1 },
        { posX = 8471, posY = 11891, posZ = 1 }
    }
    local fire_station = {
        { posX = 8137, posY = 11746, posZ = 1 }
    }
    local police_station = {
        { posX = 8066, posY = 11726, posZ = 0 }
    }
    return {
        chef = mergeTable(poor_houses, medium_houses, rich_houses),
        constructionworker = poor_houses,
        doctor = mergeTable(medium_houses, rich_houses),
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
