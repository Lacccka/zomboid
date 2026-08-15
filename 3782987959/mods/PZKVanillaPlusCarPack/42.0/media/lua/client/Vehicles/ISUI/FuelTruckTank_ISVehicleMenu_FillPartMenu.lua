
require "Vehicles/ISUI/ISVehicleMenu"
require "Vehicles/ISUI/ISVehiclePartMenu"

local function isMod(mod_Name)
	local mods = getActivatedMods();
	for i=0, mods:size()-1, 1 do
		if mods:get(i) == mod_Name then
			return true;
		end
	end
	return false;
end

if isMod("Siphoning Needs Hoses") then
	require "Vehicles/ISUI/SiphonHose_ISVehicleMenu"
end

local old_ISVehicleMenu_getNearbyFuelPump = ISVehiclePartMenu.getNearbyFuelPump

function ISVehiclePartMenu.getNearbyFuelPump(vehicle)
	--old_ISVehicleMenu_getNearbyFuelPump(vehicle)

	local part
	local areaCenter
	local square
	local square2
	local obj

	part = vehicle:getPartById("GasTank")
	if part then
		areaCenter = vehicle:getAreaCenter(part:getArea())
		if areaCenter then
			square = getCell():getGridSquare(areaCenter:getX(), areaCenter:getY(), vehicle:getZ())
			if square then
				for dy=-2,2 do
					for dx=-2,2 do
						-- TODO: check line-of-sight between 2 squares
						square2 = getCell():getGridSquare(square:getX() + dx, square:getY() + dy, square:getZ())
						if not square2 or not square2:getObjects() then
							return nil;
						end
						for i=0, square2:getObjects():size()-1 do
							obj = square2:getObjects():get(i);
							if obj:getPipedFuelAmount() > 0 then
								return obj
							end
						end
					end
				end
			end
		end
	end

	part = vehicle:getPartById("500FuelTruckTank")
	if part then
		areaCenter = vehicle:getAreaCenter(part:getArea())
		if areaCenter then
			square = getCell():getGridSquare(areaCenter:getX(), areaCenter:getY(), vehicle:getZ())
			if square then
				for dy=-2,2 do
					for dx=-2,2 do
						-- TODO: check line-of-sight between 2 squares
						square2 = getCell():getGridSquare(square:getX() + dx, square:getY() + dy, square:getZ())
						if not square2 or not square2:getObjects() then
							return nil;
						end
						for i=0, square2:getObjects():size()-1 do
							obj = square2:getObjects():get(i);
							if obj:getPipedFuelAmount() > 0 then
								return obj
							end
						end
					end
				end
			end
		end
	end

	part = vehicle:getPartById("pzk250FuelTruckTank")
	if part then
		areaCenter = vehicle:getAreaCenter(part:getArea())
		if areaCenter then
			square = getCell():getGridSquare(areaCenter:getX(), areaCenter:getY(), vehicle:getZ())
			if square then
				for dy=-2,2 do
					for dx=-2,2 do
						-- TODO: check line-of-sight between 2 squares
						square2 = getCell():getGridSquare(square:getX() + dx, square:getY() + dy, square:getZ())
						if not square2 or not square2:getObjects() then
							return nil;
						end
						for i=0, square2:getObjects():size()-1 do
							obj = square2:getObjects():get(i);
							if obj:getPipedFuelAmount() > 0 then
								return obj
							end
						end
					end
				end
			end
		end
	end

	part = vehicle:getPartById("pzk1000FuelTruckTank")
	if part then
		areaCenter = vehicle:getAreaCenter(part:getArea())
		if areaCenter then
			square = getCell():getGridSquare(areaCenter:getX(), areaCenter:getY(), vehicle:getZ())
			if square then
				for dy=-2,2 do
					for dx=-2,2 do
						-- TODO: check line-of-sight between 2 squares
						square2 = getCell():getGridSquare(square:getX() + dx, square:getY() + dy, square:getZ())
						if not square2 or not square2:getObjects() then
							return nil;
						end
						for i=0, square2:getObjects():size()-1 do
							obj = square2:getObjects():get(i);
							if obj:getPipedFuelAmount() > 0 then
								return obj
							end
						end
					end
				end
			end
		end
	end
end


local old_ISVehicleMenu_FillPartMenu = ISVehicleMenu.FillPartMenu

function ISVehicleMenu.FillPartMenu(playerIndex, context, slice, vehicle)
	--local what = IsoObjectPicker():PickVehicle(2,2)
	
	-- print("Player Index: ".. tostring(playerIndex))
	-- print("Context: ".. tostring(context))
	-- print("Slice: " ..tostring(slice))
	-- print("Vehicle: " ..tostring(vehicle))
	
	
	local playerObj = getSpecificPlayer(playerIndex);
	local typeToItem = VehicleUtils.getItems(playerIndex)
	
	local fuel_truck_source = FindVehicleGas(playerObj, vehicle)
	
	
	for i=1,vehicle:getPartCount() do
		local part = vehicle:getPartByIndex(i-1)		
		if part:isContainer() and part:getContainerContentType() == "Gasoline Storage" then
			if typeToItem["Base.PetrolCan"] and part:getContainerContentAmount() <= part:getContainerCapacity() then
				if slice then
					slice:addSlice(getText("Add Gasoline To Gasoline Storage Tank"), getTexture("Item_Petrol"), ISVehiclePartMenu.onAddGasoline, playerObj, part)
				else
					context:addOption(getText("Add Gasoline To Gasoline Storage Tank"), playerObj,ISVehiclePartMenu.onAddGasoline, part)
				end
			end
			if ISVehiclePartMenu.getGasCanNotFull(playerObj, typeToItem) and part:getContainerContentAmount() > 0 then
				if slice then
					slice:addSlice(getText("Take Gasoline From Gasoline Storage Tank"), getTexture("Item_Petrol"), ISVehiclePartMenu.onTakeGasoline, playerObj, part)
				else
					context:addOption(getText("Take Gasoline From Gasoline Storage Tank"), playerObj, ISVehiclePartMenu.onTakeGasoline, part)
				end
			end
			
			local fuelStation = ISVehiclePartMenu.getNearbyFuelPump(vehicle)
			if fuelStation then
				local square = fuelStation:getSquare();
				if square and ((SandboxVars.AllowExteriorGenerator and square:haveElectricity()) or (SandboxVars.ElecShutModifier > -1 and GameTime:getInstance():getNightsSurvived() < SandboxVars.ElecShutModifier)) then
					if square and part:getContainerContentAmount() < part:getContainerCapacity() then
						if slice then
							slice:addSlice(getText("Fill Gasoline Storage Tank From Pump"), getTexture("media/ui/vehicles/Fuel_Pump_Tanker.png"), ISVehiclePartMenu.onPumpGasolinePZK, playerObj, part, fuelStation)
						else
							context:addOption(getText("Fill Gasoline Storage Tank From Pump"), playerObj, ISVehiclePartMenu.onPumpGasolinePZK, part, fuelStation)
						end
					end
				end
			end
			
			-- local square = ISVehiclePartMenu.getNearbyFuelPump(vehicle)
			-- if square and ((SandboxVars.AllowExteriorGenerator and square:haveElectricity()) or (SandboxVars.ElecShutModifier > -1 and GameTime:getInstance():getNightsSurvived() < SandboxVars.ElecShutModifier)) then
				-- if square and part:getContainerContentAmount() < part:getContainerCapacity() then
					-- if slice then
						-- slice:addSlice(getText("Fill Gasoline Storage Tank From Pump"), getTexture("Item_Petrol"), ISVehiclePartMenu.onPumpGasolinePZK, playerObj, part)
					-- else
						-- context:addOption(getText("Fill Gasoline Storage Tank From Pump"), playerObj, ISVehiclePartMenu.onPumpGasolinePZK, part)
					-- end
				-- end
			-- end
			
			--local square = ISVehiclePartMenu.getNearbyFuelPump(vehicle)
			if fuel_truck_source and fuel_truck_source:getContainerContentAmount() > 0 and part:getContainerContentAmount() < part:getContainerCapacity() then
				--if square and part:getContainerContentAmount() < part:getContainerCapacity() then
					if slice then
						slice:addSlice(getText("Fill Gasoline Storage Tank From Fuel Truck"), getTexture("Item_Petrol"), ISVehiclePartMenu.onPumpGasolineFromTruck, playerObj, part, fuel_truck_source)
					else
						context:addOption(getText("Fill Gasoline Storage Tank From Fuel Truck"), playerObj, ISVehiclePartMenu.onPumpGasolineFromTruck, part, fuel_truck_source)
					end
				--end
			end			
		end	

		if not vehicle:isEngineStarted() and part:isContainer() and part:getContainerContentType() == "Gasoline" then
			print("Room")
			
			
			--local square = ISVehiclePartMenu.getNearbyFuelPump(vehicle)
			if fuel_truck_source and fuel_truck_source:getContainerContentAmount() > 0 and part:getContainerContentAmount() < part:getContainerCapacity() then
				--if square and part:getContainerContentAmount() < part:getContainerCapacity() then
					if slice then
						slice:addSlice(getText("Add Gasoline From Fuel Truck"), getTexture("Item_Petrol"), ISVehiclePartMenu.onPumpGasolineFromTruck, playerObj, part, fuel_truck_source)
					else
						context:addOption(getText("Add Gasoline From Fuel Truck"), playerObj, ISVehiclePartMenu.onPumpGasolineFromTruck, part, fuel_truck_source)
					end
				--end
			end			
		end


		
	end
	old_ISVehicleMenu_FillPartMenu(playerIndex, context, slice, vehicle)
end

function FindVehicleGas(playerObj, playerVehicle)
	print("TEST")
	local radius = 5
	local player = getPlayer()
	local cell = playerObj:getCell()
	local vehicleList = cell:getVehicles()
	if vehicleList then
        local iterator = vehicleList:iterator()
        if iterator then
            while iterator:hasNext() do
                local vehicle = iterator:next()
                if vehicle then
                    for i=1,vehicle:getPartCount() do
                        local part = vehicle:getPartByIndex(i-1)	
                        if part:isContainer() and part:getContainerContentType() == "Gasoline Storage" and part:getContainerContentAmount() > 0 and vehicle ~= playerVehicle then
                            --print("FUEL")
                            local square = vehicle:getSquare()
                                x = math.abs(vehicle:getX()-playerObj:getX())
                                y = math.abs(vehicle:getY()-playerObj:getY())
                                if x < radius and y < radius then
                                    return part
                                end
                        end
                    end
                end
            end
        end
    end
	return false
end

function ISVehiclePartMenu.onPumpGasolineFromTruck(playerObj, part, source_Tank)
	if playerObj:getVehicle() then
		ISVehicleMenu.onExit(playerObj)
	end
	local square = source_Tank:getVehicle():getSquare()
	if square then
		local action = ISPathFindAction:pathToVehicleArea(playerObj, part:getVehicle(), part:getArea())
		action:setOnFail(ISVehiclePartMenu.onPumpGasolinePathFail, playerObj)
		ISTimedActionQueue.add(action)
		ISTimedActionQueue.add(ISRefuelFromFuelTruck:new(playerObj, part, square, 100, source_Tank))
	end
end

 function ISVehiclePartMenu.onPumpGasolinePZK(playerObj, part, fuelStation)
	if not part then return nil end
	if not fuelStation then return nil end
	if playerObj:getVehicle() then
		ISVehicleMenu.onExit(playerObj)
	end
	local square = fuelStation:getSquare();
	if square then
		local action = ISPathFindAction:pathToVehicleArea(playerObj, part:getVehicle(), part:getArea())
		action:setOnFail(ISVehiclePartMenu.onPumpGasolinePathFail, playerObj)
		ISTimedActionQueue.add(action)
		ISTimedActionQueue.add(ISRefuelFromGasPumpPZK:new(playerObj, part, fuelStation, 100))
	end
  end
