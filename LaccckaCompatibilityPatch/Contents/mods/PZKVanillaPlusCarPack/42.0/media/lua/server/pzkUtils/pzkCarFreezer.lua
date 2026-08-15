CarFreezer = {}
CarFreezer.Create = {}
CarFreezer.Init = {}
CarFreezer.Update = {}


CarFreezer.ContainerAccess = {}
CarFreezer.Use = {}




function CarFreezer.ContainerAccess.CarFreezer(vehicle, part, chr)
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

--/////////////////////////////Trunk Refrigerator by HAMMERMAN///////////////////////////////////////

function CarFreezer.Init.Freezer(vehicle, part)
    local theContainer = part:getItemContainer()
    if not part:getModData().PzkVanillaPlusCarPack then part:getModData().PzkVanillaPlusCarPack = {} end
    if part:getInventoryItem() and theContainer then
        if vehicle:getBatteryCharge() > 0.00010 then
            theContainer:setCustomTemperature(0.2)



            local foodItems = part:getItemContainer():getItemsFromCategory("Food")
            for i=1, foodItems:size() do
                local item = foodItems:get(i-1)
                if item:canBeFrozen() then
                    item:setFreezingTime(100)
                    item:freeze()
                end
            end



        else        
            theContainer:setCustomTemperature(1.0)
        end
    end
    vehicle:transmitPartModData(part)
end
--///////////////////////////////////////////////////////////////////////////
function CarFreezer.Create.Freezer(vehicle, part)
    local theContainer = part:getItemContainer()
    local freezerPart = VehicleUtils.createPartInventoryItem(part)
    if not part:getModData().PzkVanillaPlusCarPack then part:getModData().PzkVanillaPlusCarPack = {} end
    if part:getInventoryItem() and theContainer then
        theContainer:setType("CarFreezer")
    end
end
--///////////////////////////////////////////////////////////////////////////
function CarFreezer.Update.Freezer(vehicle, part, elapsedMinutes)
print("Freezer Update script tick")
    local theContainer = part:getItemContainer()
    local inventoryItem = part:getInventoryItem()
    local currentTemp = theContainer:getTemprature()
    local batteryPart = vehicle:getBattery()
    local minTemp = 0.2
    local maxTemp = 1.0

    if not part:getModData().PzkVanillaPlusCarPack then part:getModData().PzkVanillaPlusCarPack = {} end
    if inventoryItem and theContainer then
        if part:getModData().PzkVanillaPlusCarPack and vehicle:getBatteryCharge() > 0.00010 then
            if currentTemp <= minTemp then
                theContainer:setCustomTemperature(minTemp)



                local foodItems = part:getItemContainer():getItemsFromCategory("Food")
                local newFreezerTable = {}
                for i=1, foodItems:size() do
                    local item = foodItems:get(i-1)
                    if item:canBeFrozen() then

                      	  if item:getFreezingTime() < 98  then
                         	   item:setFreezingTime(item:getFreezingTime() + (elapsedMinutes)/50 * 100.0)

                     	   else
                          	  item:freeze()
                        	  item:setFreezingTime(100)

                      	   end
		            end
                end
                  
		  
   


            elseif currentTemp > minTemp then
                theContainer:setCustomTemperature(currentTemp - (0.02 * elapsedMinutes))
            end
                if not vehicle:isEngineRunning() and not theContainer:isEmpty() then
		VehicleUtils.chargeBattery(vehicle, -0.000025 * elapsedMinutes)
                end
        else
            if currentTemp < maxTemp then
                theContainer:setCustomTemperature(currentTemp + (0.01 * elapsedMinutes))
            elseif currentTemp >= maxTemp then
                theContainer:setCustomTemperature(maxTemp)
            end
        end
    end
end