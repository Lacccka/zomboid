-- Filters spawn regions after they are loaded (B42).
local ALLOWED_FILE = "media/maps/The Bunker/spawnpoints.lua"

local function OnSpawnRegionsLoaded(regions)
    -- Remove everything except our allowed spawnpoints file
    for i = #regions, 1, -1 do
        local r = regions[i]
        if not (r and r.file == ALLOWED_FILE) then
            table.remove(regions, i)
        end
    end
end

Events.OnSpawnRegionsLoaded.Add(OnSpawnRegionsLoaded)