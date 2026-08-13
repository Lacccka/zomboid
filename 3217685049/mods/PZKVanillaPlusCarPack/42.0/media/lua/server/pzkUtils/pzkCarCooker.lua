CarOven = {}
CarOven.ContainerAccess = {}
CarOven.Create = {}
CarOven.Init = {}
CarOven.Update = {}
CarOven.Use = {}




function CarOven.ContainerAccess.CarOven(vehicle, part, chr)
	if chr:getVehicle() == vehicle then
		local seat = vehicle:getSeat(chr)
		-- Can the seated player reach the passenger seat?
		-- Only character in front seat can access it
		return seat == 1;
	elseif chr:getVehicle() then
		-- Can't reach from inside a different vehicle.
		return false
	else
		-- Standing outside the vehicle.
		if not vehicle:isInArea(part:getArea(), chr) then return false end
		local doorPart = vehicle:getPartById("DoorFrontRight")
		if doorPart and doorPart:getDoor() and not doorPart:getDoor():isOpen() then
			return false
		end
		return true
	end
end


--/////////////////////////////Oven by HAMMERMAN///////////////////////////////////////

function CarOven.Init.CarOven(vehicle, part)
    local theContainer = part:getItemContainer()
    if not part:getModData().PzkVanillaPlusCarPack then part:getModData().PzkVanillaPlusCarPack = {} end
    if part:getInventoryItem() and theContainer then
        if vehicle:getBatteryCharge() > 0.00010 then
            theContainer:setCustomTemperature(2.0)
        else        
            theContainer:setCustomTemperature(1.0)
        end
    end
    vehicle:transmitPartModData(part)
end
--///////////////////////////////////////////////////////////////////////////
function CarOven.Create.CarOven(vehicle, part)
    local theContainer = part:getItemContainer()
    local ovenPart = VehicleUtils.createPartInventoryItem(part)
    if not part:getModData().PzkVanillaPlusCarPack then part:getModData().PzkVanillaPlusCarPack = {} end
    if part:getInventoryItem() and theContainer then
        theContainer:setType("CarOven")
    end
end
--///////////////////////////////////////////////////////////////////////////
function CarOven.Update.CarOven(vehicle, part, elapsedMinutes)
print("CarOven Update script tick")
    local theContainer = part:getItemContainer()
    local inventoryItem = part:getInventoryItem()
    local currentTemp = theContainer:getTemprature()
    local batteryPart = vehicle:getBattery()
    local minTemp = 1.0
    local maxTemp = 2.0

    if not part:getModData().PzkVanillaPlusCarPack then part:getModData().PzkVanillaPlusCarPack = {} end
    if inventoryItem and theContainer then
        if part:getModData().PzkVanillaPlusCarPack and vehicle:getBatteryCharge() > 0.00010 then
            if currentTemp > maxTemp then
                theContainer:setCustomTemperature(maxTemp)
            elseif currentTemp < maxTemp then
                theContainer:setCustomTemperature(currentTemp + (0.02 * elapsedMinutes))
            end
                if not vehicle:isEngineRunning() and not theContainer:isEmpty() then
		VehicleUtils.chargeBattery(vehicle, -0.000025 * elapsedMinutes)
                end
        else
            if currentTemp > minTemp then
                theContainer:setCustomTemperature(currentTemp - (0.01 * elapsedMinutes))
            elseif currentTemp <= minTemp then
                theContainer:setCustomTemperature(minTemp)
            end
        end
    end






end