




local group = BodyLocations.getGroup("Human")
for i = 1, #ChimeraRegistries.BodyLocations do
    group:getOrCreateLocation(ChimeraRegistries.BodyLocations[i])
end

