if isClient() then return end

local PZKZones = {}

function PZKZones.addZone(zone, ztype, x, y, z, w, h, dir, modID)
    if not zone then return end


    if ztype == "ParkingStall" then
        getWorld():registerVehiclesZone(zone, ztype, x, y, z, w, h, {Direction = dir, FaceDirection = true})
    else
        getWorld():registerZone(zone, ztype, x, y, z, w, h)
    end
	getWorld():checkVehiclesZones();
end

return PZKZones