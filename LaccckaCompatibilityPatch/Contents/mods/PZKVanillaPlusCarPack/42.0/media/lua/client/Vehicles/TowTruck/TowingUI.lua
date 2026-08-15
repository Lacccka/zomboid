if not getActivatedMods():contains("STowTruck")  and not getActivatedMods():contains("TowingCar")  then
if not TowTruckMod then TowTruckMod = {} end
if not TowTruckMod.UI then TowTruckMod.UI = {} end

---------------------------------------------------------------------------
--- UI functions
---------------------------------------------------------------------------

function TowTruckMod.UI.addHookOptionToMenu(playerObj, vehicle)
	local menu = getPlayerRadialMenu(playerObj:getPlayerNum())
	if menu == nil then return end

	local flippedVehicle = TowTruckMod.Utils.getUpsideDownVehicle(vehicle)
	if #flippedVehicle == 1 then
		local centerHook = TowTruckMod.Utils.getCenterHook(vehicle, flippedVehicle[1])
		menu:addSlice(centerHook[1].name, centerHook[1].texture, centerHook[1].func, playerObj, centerHook[1].towingVehicle, centerHook[1].towedVehicle, centerHook[1].towingPoint, centerHook[1].towedPoint)
		return
	end

	local vehicles = TowTruckMod.Utils.getAviableVehicles(vehicle)
	if #vehicles == 1 then
		local hookTypeVariants = TowTruckMod.Utils.getHookTypeVariants(vehicle, vehicles[1])
		menu:addSlice(hookTypeVariants[1].name, hookTypeVariants[1].texture, hookTypeVariants[1].func, playerObj, hookTypeVariants[1].towingVehicle, hookTypeVariants[1].towedVehicle, hookTypeVariants[1].towingPoint, hookTypeVariants[1].towedPoint)	
	end
end

function TowTruckMod.UI.addUnhookOptionToMenu(playerObj, vehicle)
	local menu = getPlayerRadialMenu(playerObj:getPlayerNum())
	if menu == nil then return end
   
	local towedVehicle = vehicle
	if vehicle:getVehicleTowing() then
		towedVehicle = vehicle:getVehicleTowing()
	end
   
    local attachment = vehicle:getTowAttachmentOther()  
	local detachFunc
	
	if TowTruckMod.Utils.isTrailer(towedVehicle) then
		return
	end	
		
	detachFunc = TowTruckMod.Hook.detachTowTruckAction
	-- Add towhook height options.
	if attachment ~= "flipNode" then
		menu:addSlice(getText("UI_Text_Towing_heightmenu"), getTexture("media/textures/arrows.png"), TowTruckMod.UI.showChooseTowHeightMenu, playerObj, vehicle, towedVehicle)
	end
	menu:addSlice(getText("UI_Text_Towing_detach", ISVehicleMenu.getVehicleDisplayName(towedVehicle)), getTexture("media/textures/untow_car_icon.png"), detachFunc, playerObj, vehicle, attachment)
end

function TowTruckMod.UI.showChooseTowHeightMenu(playerObj, towingVehicle, towedVehicle)
	local playerIndex = playerObj:getPlayerNum()
	local menu = getPlayerRadialMenu(playerIndex)
	menu:clear()

	local modData = towingVehicle:getModData()
	modData.towHeight = modData.towHeight or -1
	towingVehicle:transmitModData()
	local towHeight = modData.towHeight
	if towHeight < 0 then
		menu:addSlice(getText("UI_Text_Towing_heightup"), getTexture("media/textures/arrow_up.png"), TowTruckMod.Hook.increaseTowHeight, playerObj, towingVehicle, towedVehicle, 0.0)
	elseif towHeight == 0 then
		menu:addSlice(getText("UI_Text_Towing_heightup"), getTexture("media/textures/arrow_up.png"), TowTruckMod.Hook.increaseTowHeight, playerObj, towingVehicle, towedVehicle, 0.25)
		menu:addSlice(getText("UI_Text_Towing_heightmin"), getTexture("media/textures/arrow_down_gray.png"), TowTruckMod.Hook.fakeFunc, playerObj, towingVehicle, towedVehicle)
	elseif towHeight == 1 then
		menu:addSlice(getText("UI_Text_Towing_heightup"), getTexture("media/textures/arrow_up.png"), TowTruckMod.Hook.increaseTowHeight, playerObj, towingVehicle, towedVehicle, 0.25)
		menu:addSlice(getText("UI_Text_Towing_heightdown"), getTexture("media/textures/arrow_down.png"), TowTruckMod.Hook.decreaseTowHeight, playerObj, towingVehicle, towedVehicle)
	elseif towHeight == 2 then
		menu:addSlice(getText("UI_Text_Towing_heightup"), getTexture("media/textures/arrow_up.png"), TowTruckMod.Hook.increaseTowHeight, playerObj, towingVehicle, towedVehicle, 0.25)
		menu:addSlice(getText("UI_Text_Towing_heightdown"), getTexture("media/textures/arrow_down.png"), TowTruckMod.Hook.decreaseTowHeight, playerObj, towingVehicle, towedVehicle)		
	elseif towHeight == 3 then
		menu:addSlice(getText("UI_Text_Towing_heightmax"), getTexture("media/textures/arrow_up_gray.png"), TowTruckMod.Hook.fakeFunc, playerObj, towingVehicle, towedVehicle)
		menu:addSlice(getText("UI_Text_Towing_heightdown"), getTexture("media/textures/arrow_down.png"), TowTruckMod.Hook.decreaseTowHeight, playerObj, towingVehicle, towedVehicle)
	end

	menu:setX(getPlayerScreenLeft(playerIndex) + getPlayerScreenWidth(playerIndex) / 2 - menu:getWidth() / 2)
	menu:setY(getPlayerScreenTop(playerIndex) + getPlayerScreenHeight(playerIndex) / 2 - menu:getHeight() / 2)
	menu:addToUIManager()
	if JoypadState.players[playerObj:getPlayerNum()+1] then
		menu:setHideWhenButtonReleased(Joypad.DPadUp)
		setJoypadFocus(playerObj:getPlayerNum(), menu)
		playerObj:setJoypadIgnoreAimUntilCentered(true)
	end	
end

function TowTruckMod.UI.removeDefaultTrailerOptions(playerObj)
   local menu = getPlayerRadialMenu(playerObj:getPlayerNum())
   if menu == nil then return end

   local tmpSlices = menu.slices
   menu:clear()
   for _, slice in ipairs(tmpSlices) do
      if slice.command[1] ~= ISVehicleMenu.onAttachTrailer and slice.command[1] ~= ISVehicleMenu.onDetachTrailer then
         menu:addSlice(slice.text, slice.texture, slice.command[1], 
                        slice.command[2], slice.command[3], slice.command[4], slice.command[5], slice.command[6], slice.command[7])
      end
   end
end

---------------------------------------------------------------------------
--- Mod compability
---------------------------------------------------------------------------

if getActivatedMods():contains("vehicle_additions") then
   require('Vehicles/ISUI/Oven_Mattress_RadialMenu')
   require('Vehicles/ISUI/FuelTruckTank_ISVehicleMenu_FillPartMenu')
end

---------------------------------------------------------------------------
--- Attach to default menu method
---------------------------------------------------------------------------

--- Save default function for wrap it.
if TowTruckMod.UI.defaultShowRadialMenu == nil then
   TowTruckMod.UI.defaultShowRadialMenu = ISVehicleMenu.showRadialMenu
end
 
 --- Wrap default fuction.
function ISVehicleMenu.showRadialMenu(playerObj)
	TowTruckMod.UI.defaultShowRadialMenu(playerObj)
		
	local vehicle = ISVehicleMenu.getVehicleToInteractWith(playerObj)
	if vehicle ~= nil then -- Make sure there is a vehicle to interact with.		
		-- Only give towtruck the hook options.
		if TowTruckMod.Utils.isTowTruck(vehicle) then
			--if not TowTruckMod.Utils.isTrailer(vehicle) then
				TowTruckMod.UI.removeDefaultTrailerOptions(playerObj)
			--end	
			
			if vehicle:getVehicleTowing() then
				TowTruckMod.UI.addUnhookOptionToMenu(playerObj, vehicle)
			else
				TowTruckMod.UI.addHookOptionToMenu(playerObj, vehicle)
			end
		end
	end
end
end