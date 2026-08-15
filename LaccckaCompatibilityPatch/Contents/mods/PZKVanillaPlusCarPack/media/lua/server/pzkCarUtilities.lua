-- Initialize empty lists
local vehicleList = {}
local partList = {}

--///////////////////////////////////////////////////////////////////////////

pzkCarUtils = {}
pzkCarUtils.Create = {}
pzkCarUtils.Init = {}
pzkCarUtils.Update = {}

TrailerFieldOven = {}
TrailerFieldOven.ContainerAccess = {}
TrailerFieldOven.Use = {}


--///////////////////////////////////////////////////////////////////////////
function pzkCarUtils.Create.CreateWithDistribution(vehicle, part)
    local theContainer = part:getItemContainer()
    local newCarPartPart = VehicleUtils.createPartInventoryItem(part)
    if not part:getModData().PzkVanillaPlusCarPack then part:getModData().PzkVanillaPlusCarPack = {} end

    local skinID = vehicle:getSkin()
if skinID == 1 then
print("SkinID loaded: ", skinID)
else
print("other SkinID loaded")
end


end
--///////////////////////////////////////////////////////////////////////////

--/////////////////////////////Trunk Refrigerator by HAMMERMAN///////////////////////////////////////

function pzkCarUtils.Init.TrunkRefrigerator(vehicle, part)
    local theContainer = part:getItemContainer()
    if not part:getModData().PzkVanillaPlusCarPack then part:getModData().PzkVanillaPlusCarPack = {} end
    if part:getInventoryItem() and theContainer then
        if vehicle:getBatteryCharge() > 0.00010 then
            theContainer:setCustomTemperature(0.2)
        else        
            theContainer:setCustomTemperature(1.0)
        end
    end
    vehicle:transmitPartModData(part)
end
--///////////////////////////////////////////////////////////////////////////
function pzkCarUtils.Create.TrunkRefrigerator(vehicle, part)
    local theContainer = part:getItemContainer()
    local refrigeratorPart = VehicleUtils.createPartInventoryItem(part)
    if not part:getModData().PzkVanillaPlusCarPack then part:getModData().PzkVanillaPlusCarPack = {} end
    if part:getInventoryItem() and theContainer then
        theContainer:setType("fridge")
    end
end
--///////////////////////////////////////////////////////////////////////////
function pzkCarUtils.Update.TrunkRefrigerator(vehicle, part, elapsedMinutes)
print("Refrigerator Update script tick")
    local theContainer = part:getItemContainer()
    local inventoryItem = part:getInventoryItem()
    local currentTemp = theContainer:getTemprature()
    local batteryPart = vehicle:getBattery()
    local minTemp = 0.2
    local maxTemp = 1.0

    if not part:getModData().PzkVanillaPlusCarPack then part:getModData().PzkVanillaPlusCarPack = {} end
    if inventoryItem and theContainer then
if batteryPart then
        if part:getModData().PzkVanillaPlusCarPack and vehicle:getBatteryCharge() > 0.00010 then
            if currentTemp < minTemp then
                theContainer:setCustomTemperature(minTemp)
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

   else
   theContainer:setCustomTemperature(maxTemp)
   end

end

--///////////////////////////////////////////////////////////////////////////
function TrailerFieldOven.ContainerAccess.TrailerFieldOven(vehicle, part, chr)
print("CarFieldOven ContainerAccess script loaded")
		-- Standing outside the vehicle.
		if vehicle:isInArea(part:getArea(), chr) then 
		
		return true
	        end
end


--Field Oven by HAMMERMAN
--///////////////////////////////////////////////////////////////////////////
function pzkCarUtils.Init.CFOven(vehicle, part)
print("CarFieldOven Init script loaded")
   local theContainer = part:getItemContainer()
   part:getModData().fuel = part:getModData().fuel or 0
   part:getModData().isLit = part:getModData().isLit or false
   part:getModData().burnTime = part:getModData().burnTime or 0

    if not part:getModData().PzkVanillaPlusCarPack then part:getModData().PzkVanillaPlusCarPack = {} end
    if part:getInventoryItem() and theContainer then
        if part:getModData().isLit then
            theContainer:setCustomTemperature(2.0)
        else        
            theContainer:setCustomTemperature(1.0)
        end
    end
	pzkaddVehicleAndParts(vehicle, part)

    vehicle:transmitPartModData(part)
end
--///////////////////////////////////////////////////////////////////////////
function pzkCarUtils.Create.CFOven(vehicle, part)
print("CarFieldOven Create script loaded")
    local theContainer = part:getItemContainer()
    local ovenPart = VehicleUtils.createPartInventoryItem(part)
    if not part:getModData().PzkVanillaPlusCarPack then part:getModData().PzkVanillaPlusCarPack = {} end

    if part:getInventoryItem() and theContainer then
        theContainer:setType("CarFieldOven")
    end
end


--///////////////////////////////////////////////////////////////////////////
-- Update TrailerFieldOven
function pzkCarUtils.Update.CFOven(vehicle, part, elapsedMinutes)
print("CarFieldOven Update script tick")
    local theContainer = part:getItemContainer()
    local inventoryItem = part:getInventoryItem()
    local currentTemp = theContainer:getTemperature()
    local minTemp = 1.0
    local maxTemp = 2.0
    local ovenPart = vehicle:getPartById("CarFieldOven")
    if not part:getModData().PzkVanillaPlusCarPack then part:getModData().PzkVanillaPlusCarPack = {} end


        if part:getModData().isLit then
            theContainer:setCustomTemperature(maxTemp)
            print("stove is lit")

            if part:getModData().burnTime > 0 then
                part:getModData().burnTime = part:getModData().burnTime - elapsedMinutes
                part:getModData().fuel = part:getModData().burnTime
            else
                part:getModData().isLit = false
                part:getModData().burnTime = 0
                part:getModData().fuel = part:getModData().burnTime
                theContainer:setCustomTemperature(minTemp)
                print("run out of wood fuel")
            end
        else
            theContainer:setCustomTemperature(minTemp)
            print("stove not lit")
        end


end



--///////////////////////////FORCED////////////////////////////////////

-- Force Update TrailerFieldOven
function pzkOvenForceUpdate(vehicle, part)
print("CarFieldOven Force Update script tick")

    local minTemp = 1.0
    local maxTemp = 2.0
    local theContainer = part:getItemContainer()

    if not part:getModData().PzkVanillaPlusCarPack then part:getModData().PzkVanillaPlusCarPack = {} end


        if part:getModData().isLit then
            theContainer:setCustomTemperature(maxTemp)
            print("stove is lit")

            if part:getModData().burnTime > 0 then
                part:getModData().burnTime = part:getModData().burnTime - 1
                part:getModData().fuel = part:getModData().burnTime
            else
                part:getModData().isLit = false
                part:getModData().burnTime = 0
                part:getModData().fuel = part:getModData().burnTime
                theContainer:setCustomTemperature(minTemp)
                print("run out of wood fuel")
            end
        else
            theContainer:setCustomTemperature(minTemp)
            print("stove not lit")
        end


end



--///////////////////////////////////////////////////////////////////////////

function TrailerFieldOven.checkFuel(player, vehicle)
print("CarFieldOven Check fuel function")
    print("checkFuel called with player:", player, "vehicle:", vehicle)
    local ovenPart = vehicle:getPartById("CarFieldOven")

    if not ovenPart then
  --      player:Say("This vehicle doesn't have an oven part.")
    print("ovenPart not found, variable is nil")
        return

    else
    print("ovenPart found:", ovenPart)

    end

    local currentFuel = ovenPart:getModData().fuel or 0
    local currentBurn = ovenPart:getModData().burnTime or 0
    local currentLit =  ovenPart:getModData().isLit or false
    print(currentFuel, currentBurn, currentLit)
end
--///////////////////////////////////////////////////////////////////////////

-- Add Logs to Oven
function TrailerFieldOven.addLogs(player, vehicle, logsCount)
print("CarFieldOven add logs function")
    print("addLogs called with player:", player, "vehicle:", vehicle, "logsCount:", logsCount)
    local ovenPart = vehicle:getPartById("CarFieldOven")

    if not ovenPart then
  --      player:Say("This vehicle doesn't have an oven part.")
    print("ovenPart not found, variable is nil")
        return

    else
    print("ovenPart found:", ovenPart)

    end

    if logsCount <= 0 then
--        player:Say("You need to add at least one log.")
        return
    end

    local currentFuel = ovenPart:getModData().fuel or 0
    ovenPart:getModData().fuel = currentFuel + (logsCount * 10)
--    player:Say("Added " .. logsCount .. " logs to the oven.")
    print("Added " .. logsCount .. " logs to the oven.")
end

--///////////////////////////////////////////////////////////////////////////
-- Light Fire in Oven
function TrailerFieldOven.lightFire(player, vehicle)
print("CarFieldOven light fire function")
    print("lightFire called with player:", player, "vehicle:", vehicle)
    local ovenPart = vehicle:getPartById("CarFieldOven")
    local theContainer = ovenPart:getItemContainer()
    if not ovenPart then
    print("This vehicle doesn't have an oven part.")
        return
    end

    if ovenPart:getModData().fuel <= 0 then
    print("No fuel to light the oven.")
        return
    end

    ovenPart:getModData().isLit = true
    theContainer:setCustomTemperature(2.0)
    ovenPart:getModData().burnTime = ovenPart:getModData().fuel
    print("The oven is now lit.",ovenPart:getModData().fuel, ovenPart:getModData().burnTime, ovenPart:getModData().isLit )
end

--///////////////////////////////////////////////////////////////////////////
-- Put Out Fire in Oven
function TrailerFieldOven.putOutFire(player, vehicle)
print("CarFieldOven put out fire function")
    print("putOutFire called with player:", player, "vehicle:", vehicle)
    local ovenPart = vehicle:getPartById("CarFieldOven")
    local theContainer = ovenPart:getItemContainer()
    if not ovenPart then
    print("This vehicle doesn't have an oven part.")
        return
    end

    if not ovenPart:getModData().isLit then
    print("The oven is not lit.")
        return
    end

    ovenPart:getModData().isLit = false
    ovenPart:getModData().burnTime = 0
    theContainer:setCustomTemperature(1.0)
    print("The oven fire has been put out.")
end

--///////////////////////////////////////////////////////////////////////////
--Clean oven fireplace (reset)
function TrailerFieldOven.resetFuel(player, vehicle)
print("CarFieldOven clean fireplace function")
    print("resetFuel called with player:", player, "vehicle:", vehicle)

    local ovenPart = vehicle:getPartById("CarFieldOven")
    if not ovenPart then
    print("This vehicle doesn't have an oven part.")
        return
    end

    ovenPart:getModData().isLit = false
    ovenPart:getModData().burnTime = 0
    ovenPart:getModData().fuel = 0
    print("The fireplace cleaned.")
end


--///////////////////////////////////////////////////////////////////////////

-- Add Logs, Light Fire, and Put Out Fire to Context Menu
local function createVehicleContextMenu(player, context, worldobjects, test)
    local vehicle = nil
    
    if test and instanceof(test, "BaseVehicle") then
        vehicle = test
    else
        for _, obj in ipairs(worldobjects) do
            if instanceof(obj, "IsoObject") and obj:getSquare() and obj:getSquare():getVehicleContainer() then
                vehicle = obj:getSquare():getVehicleContainer()
                break
            end
        end
    end


    if vehicle then
        local ovenPart = vehicle:getPartById("CarFieldOven")
        if ovenPart then

            context:addOption("Add Logs to Oven", player, function() TrailerFieldOven.addLogs(player, vehicle, 5) end)

	if not ovenPart:getModData().isLit then
            context:addOption("Light Oven Fire", player, function() TrailerFieldOven.lightFire(player, vehicle) end)
	end

	if ovenPart:getModData().isLit then
            context:addOption("Put Out Oven Fire", player, function() TrailerFieldOven.putOutFire(player, vehicle) end)
	end

            context:addOption("Check fuel", player, function() TrailerFieldOven.checkFuel(player, vehicle) end)

	if not ovenPart:getModData().isLit then
            context:addOption("Clean fireplace", player, function() TrailerFieldOven.resetFuel(player, vehicle) end)
	end


        end
    end
end


--///////////////////////////////////////////////////////////////////////////
Events.OnFillWorldObjectContextMenu.Add(createVehicleContextMenu)


--///////////////////////////////////////////////////////////////////////////
local function updateCarFieldOvenParts(vehicle, part)
           -- print("Update tick: ", vehicle," : ",part)
	    pzkOvenForceUpdate(vehicle, part)
end




--///////////////////////////ADD///////////////////////////////////////

function pzkaddVehicleAndParts(vehicle, part)
    table.insert(vehicleList, vehicle)
    table.insert(partList, part)
print("Added: ", vehicle," : ",part)
end




--////////////////////////CHECK//////////////////////////////////////////

-- Function to check if a vehicle exists in the list
function pzkvehicleExists(vehicle)

    for i, v in ipairs(vehicleList) do
        if v == vehicle then
            updateCarFieldOvenParts(vehicleList[i], partList[i])
	    return true
        end
    end
	pzkRemoveVehicle(vehicle)
    return false
end

--///////////////////////REMOVE/////////////////////////////////////////
-- Function to remove a vehicle from the lists
function pzkRemoveVehicle(vehicle)
    for i, v in ipairs(vehicleList) do
        if v == vehicle then
            table.remove(vehicleList, i)
            table.remove(partList, i) -- Remove corresponding parts
            print("Removed: ", vehicle," : ",part)
            return
        end
    end
end


function pzkPrintAllTables(k)
   for i, index in ipairs(vehicleList) do
   print("Vehicle list: ", i, " : ", vehicleList[i], partList[i])
   end
end


--////////////////////////////////////////////////////////////////

-- Variables for tick counting
local tickCounter = 0
local updateInterval = 400  -- Update every 90 ticks (approximately every 2 seconds)


--//////////////////////TICK CLOCK///////////////////////////////
--[[

Events.OnTick.Add(function()
    local gameSpeed = getGameTime():getTrueMultiplier()

    tickCounter = tickCounter + 1


local speed = updateInterval / gameSpeed


    if tickCounter >= speed then

 --   pzkPrintAllTables(1)

   for i, vehicle in ipairs(vehicleList) do
        pzkvehicleExists(vehicle)
    end

        tickCounter = 0
    end
end)

--]]

