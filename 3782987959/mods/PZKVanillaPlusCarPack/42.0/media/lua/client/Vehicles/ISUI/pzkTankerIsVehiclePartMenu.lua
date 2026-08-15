--***********************************************************
--**                    THE INDIE STONE                    **
--***********************************************************

PZKISVehiclePartMenu = {}

require "Vehicle/ISVehiclePartMenu"
require "Vehicles/TimedActions/RS_TakeFuelFromTank"

local function predicateNotBroken(item)
	return not item:isBroken()
end

local function predicatePetrol(item)
	return item:getFluidContainer() and item:getFluidContainer():contains(Fluid.Petrol)
end

local function predicateEmptyPetrol(item)
	return item:getFluidContainer() and item:getFluidContainer():isEmpty()
end

local function predicatePetrolNotFull(item)
	return item:getFluidContainer() and item:getFluidContainer():contains(Fluid.Petrol) and item:getFluidContainer():getFreeCapacity() > 0
end

function PZKISVehiclePartMenu.getGasCanNotEmpty(playerObj, typeToItem)
	-- Prefer an equipped PetrolCan, then the emptiest PetrolCan.
	local equipped = playerObj:getPrimaryHandItem()
	if equipped and predicatePetrol(equipped) then
		return equipped
	end
	local inv = playerObj:getInventory()
	if inv:containsEvalRecurse(predicatePetrol) then
		local allPetrol = inv:getAllEvalRecurse(predicatePetrol)
		local gasCan = nil
		local amount = - 1
        for j=1,allPetrol:size() do
            local item = allPetrol:get(j-1)
			if item:getFluidContainer():getAmount() > 0 and ( item:getFluidContainer():getAmount() > amount ) then
				gasCan = item
				amount = gasCan:getFluidContainer():getAmount();
			end
		end
		if gasCan then return gasCan end
	end
	return nil
end

function PZKISVehiclePartMenu.getGasCanNotFull(playerObj, typeToItem)
	-- Prefer an equipped PetrolCanEmpty/PetrolCan, then the fullest PetrolCan, then any PetrolCanEmpty.
	local equipped = playerObj:getPrimaryHandItem()
	if equipped and predicatePetrolNotFull(equipped) then
		return equipped
	end
	if equipped and predicateEmptyPetrol(equipped) then
		return equipped
	end
	local inv = playerObj:getInventory()
	if inv:containsEvalRecurse(predicatePetrolNotFull) then
		local allPetrol = inv:getAllEvalRecurse(predicatePetrolNotFull)
		local gasCan = nil
		-- local usedDelta = -1
		local amount = -1
		for j=1,allPetrol:size() do
			local item = allPetrol:get(j-1)
			if item:getFluidContainer():getAmount() > amount then
				gasCan = item
				amount = gasCan:getFluidContainer():getAmount();
			end
		end
		if gasCan then return gasCan end
	end
	if inv:containsEvalRecurse(predicateEmptyPetrol) then
		return inv:getFirstEvalRecurse(predicateEmptyPetrol)
	end
	return nil
end

function PZKISVehiclePartMenu.onTakeGasoline(playerObj, part)
	if playerObj:getVehicle() then
		ISVehicleMenu.onExit(playerObj)
	end
	local typeToItem,tagToItem = VehicleUtils.getItems(playerObj:getPlayerNum())
	local item = PZKISVehiclePartMenu.getGasCanNotFull(playerObj, typeToItem)
	if item then
		ISVehiclePartMenu.toPlayerInventory(playerObj, item)
		ISTimedActionQueue.add(ISPathFindAction:pathToVehicleArea(playerObj, part:getVehicle(), part:getArea()))
		ISInventoryPaneContextMenu.equipWeapon(item, false, false, playerObj:getPlayerNum())
		ISTimedActionQueue.add(ISTakeFuelActionFromTank:new(playerObj, part, item, 50))
	end
end