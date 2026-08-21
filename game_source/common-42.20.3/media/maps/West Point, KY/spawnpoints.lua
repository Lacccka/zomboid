function SpawnPoints()
    local poor_houses = {
        { posX = 11308, posY = 6671, posZ = 0 },
        { posX = 11218, posY = 6796, posZ = 0 },
        { posX = 10936, posY = 6645, posZ = 0 },
        { posX = 11536, posY = 6934, posZ = 0 },
        { posX = 12023, posY = 6980, posZ = 0 },
        { posX = 11936, posY = 6749, posZ = 0 },
        { posX = 11735, posY = 6691, posZ = 0 },
        { posX = 10955, posY = 6964, posZ = 0 },
        { posX = 11913, posY = 7070, posZ = 0 },
        { posX = 11967, posY = 6749, posZ = 0 }
    }
    local medium_houses = {
        { posX = 11945, posY = 7049, posZ = 0 },
        { posX = 11417, posY = 6877, posZ = 0 },
        { posX = 11835, posY = 6993, posZ = 0 },
        { posX = 11187, posY = 6733, posZ = 0 },
        { posX = 10933, posY = 6728, posZ = 0 }
    }
    local rich_houses = {
        { posX = 11182, posY = 6860, posZ = 0 },
        { posX = 11967, posY = 7079, posZ = 0 },
        { posX = 11767, posY = 6673, posZ = 0 }
    }
    local doctor_houses = {
        { posX = 11531, posY = 6972, posZ = 0 },
    }
    local fire_station = {
        { posX = 12275, posY = 7032, posZ = 0 },
    }
return {
    chef = mergeTable(poor_houses, medium_houses, rich_houses),
    constructionworker = poor_houses,
    doctor = mergeTable(medium_houses, rich_houses, doctor_houses),
    fireofficer = mergeTable(poor_houses, fire_station),
    nurse = poor_houses,
    parkranger = poor_houses,
    policeofficer = mergeTable(poor_houses, medium_houses),
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
