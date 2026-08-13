
--***********************************************************
--**                    THE INDIE STONE                    **
--***********************************************************
require "Vehicle/ISVehiclePartMenu"
require "Vehicles/TimedActions/RS_TakePropaneFromTank"

function ISVehiclePartMenu.getPropaneTankNotFull(playerObj, typeToItem)

	local equipped = playerObj:getPrimaryHandItem()
	if equipped and equipped:getType() == "PropaneTank" and equipped:getCurrentUsesFloat() < 1.0 then
		return equipped
	end
	if typeToItem["Base.PropaneTank"] then
		local gasCan = nil
		local usedDelta = -1
		for _,item in ipairs(typeToItem["Base.PropaneTank"]) do
			if item:getCurrentUsesFloat() < 1.0 and item:getCurrentUsesFloat() > usedDelta then
				gasCan = item
				usedDelta = gasCan:getCurrentUsesFloat()
			end
		end
		if gasCan then return gasCan end
	end
	return nil
end

local tank = ScriptManager.instance:getItem("Base.PropaneTank")
tank:DoParam("KeepOnDeplete = TRUE")
tank:DoParam("StaticModel = PropaneTank")

function ISVehiclePartMenu.onTakePropane(playerObj, part)
	if playerObj:getVehicle() then
		ISVehicleMenu.onExit(playerObj)
	end
	local typeToItem = VehicleUtils.getItems(playerObj:getPlayerNum())
	local item = ISVehiclePartMenu.getPropaneTankNotFull(playerObj, typeToItem)
	if item then
		ISVehiclePartMenu.toPlayerInventory(playerObj, item)
		ISTimedActionQueue.add(ISPathFindAction:pathToVehicleArea(playerObj, part:getVehicle(), part:getArea()))
		ISInventoryPaneContextMenu.equipWeapon(item, false, false, playerObj:getPlayerNum())
		--ISTimedActionQueue.add(FuelTruck_TakePropaneFromVehicle:new(playerObj, part, item, 50))
		--ISTimedActionQueue.add(ISTakeGasolineFromVehicle:new(playerObj, part, item, 50))
		ISTimedActionQueue.add(ISTakePropaneActionFromTank:new(playerObj, part, item, 50))
	end
end
