if not getActivatedMods():contains("STowTruck")  and not getActivatedMods():contains("TowingCar")  then
---------------------------------------------------------------------------
--- Utils functions
---------------------------------------------------------------------------

if not TowTruckMod then TowTruckMod = {} end
if not TowTruckMod.Utils then TowTruckMod.Utils = {} end

TowTruckMod.Utils.tempVector1 = Vector3f.new()
TowTruckMod.Utils.tempVector2 = Vector3f.new()
TowTruckMod.Utils.tempVector3 = Vector3f.new()
TowTruckMod.Utils.tempVector4 = Vector3f.new()

function TowTruckMod.Utils.isTrailer(vehicle)
	return string.match(string.lower(vehicle:getScript():getName()), "trailer")
end

function TowTruckMod.Utils.isTowTruck(vehicle)
	return vehicle:attachmentExist("hook")
end

function TowTruckMod.Utils.getAviableVehicles(mainVehicle)
	local vehicles = {}
	local square = mainVehicle:getSquare()
	if square == nil then return vehicles end

	for y=square:getY() - 10, square:getY() + 10 do
		for x=square:getX() - 10, square:getX() + 10 do
			local square2 = getCell():getGridSquare(x, y, square:getZ())
			if square2 then
				for i=1, square2:getMovingObjects():size() do
					local obj = square2:getMovingObjects():get(i-1)
					if obj~= nil 
							and instanceof(obj, "BaseVehicle") 
							and obj ~= mainVehicle
							and #(TowTruckMod.Utils.getHookTypeVariants(mainVehicle, obj)) ~= 0
							then							
						table.insert(vehicles, obj)
					end
				end
			end
		end
	end
	return vehicles
end

--- Return a table with hookType options for vehicles.
function TowTruckMod.Utils.getHookTypeVariants(vehicleA, vehicleB)
	local hookTypeVariants = {}

	if vehicleA:getVehicleTowing() or vehicleA:getVehicleTowedBy()
			or vehicleB:getVehicleTowing() or vehicleB:getVehicleTowedBy() then 
		return hookTypeVariants 
	end

	local p1, p2 = TowTruckMod.Utils.getTowingAttachPoints(vehicleA, vehicleB)
	
	if TowTruckMod.Utils.isTrailer(vehicleB) then
		return hookTypeVariants
	end

	if TowTruckMod.Utils.isTowTruck(vehicleA) and p1 or p2 then
		local hookType = {}
			hookType.name = getText("UI_Text_Towing_attach") .. "\n".. ISVehicleMenu.getVehicleDisplayName(vehicleB) .. "\n" .. getText("UI_Text_Towing_byHook")
			hookType.func = TowTruckMod.Hook.attachTowTruckAction
			hookType.towingVehicle = vehicleA
			hookType.towedVehicle = vehicleB
			hookType.towingPoint = p1
			hookType.towedPoint = p2					
			hookType.texture = getTexture("media/textures/tow_car_icon.png")
		table.insert(hookTypeVariants, hookType)
	end

	return hookTypeVariants
end

function TowTruckMod.Utils.getTowingAttachPoints(vehicleA, vehicleB)
	if vehicleA:canAttachTrailer(vehicleB, "hook", "trailer") then
		return "hook", "trailer"
	end
	if vehicleA:canAttachTrailer(vehicleB, "hook", "trailerfront") then
		return "hook", "trailerfront"
	end
end

-- Is the vehicle upside down?

function TowTruckMod.Utils.isUpsideDownVehicle(vehicle)
   local topPos = vehicle:getWorldPos(0, 1, 0, TowTruckMod.Utils.tempVector1)
   local centerPos = vehicle:getWorldPos(0, 0, 0, TowTruckMod.Utils.tempVector2)

   return topPos:z() < (centerPos:z() + 0.3)
end

function TowTruckMod.Utils.getUpsideDownVehicle(mainVehicle)
	local vehicles = {}
	local square = mainVehicle:getSquare()
	if square == nil then return vehicles end

	for y=square:getY() - 10, square:getY() + 10 do
		for x=square:getX() - 10, square:getX() + 10 do
			local square2 = getCell():getGridSquare(x, y, square:getZ())
			if square2 then
				for i=1, square2:getMovingObjects():size() do
					local obj = square2:getMovingObjects():get(i-1)
					if obj~= nil 
							and instanceof(obj, "BaseVehicle") 
							and obj ~= mainVehicle
							and TowTruckMod.Utils.isUpsideDownVehicle(obj)
							and #(TowTruckMod.Utils.getCenterHook(mainVehicle, obj)) ~= 0
							then							
						table.insert(vehicles, obj)
					end
				end
			end
		end
	end
	return vehicles
end

--- Return a table with hookType options for flipped vehicles.
function TowTruckMod.Utils.getCenterHook(vehicleA, vehicleB)
	local centerHook = {}

	if vehicleA:getVehicleTowing() or vehicleA:getVehicleTowedBy()
			or vehicleB:getVehicleTowing() or vehicleB:getVehicleTowedBy() then 
		return centerHook 
	end

	if TowTruckMod.Utils.isTowTruck(vehicleA) and (vehicleA:canAttachTrailer(vehicleB, "hook", "flipNode")) then
		local hookType = {}
			hookType.name = getText("UI_Text_Towing_flip") .. "\n".. ISVehicleMenu.getVehicleDisplayName(vehicleB) .. "\n" .. getText("UI_Text_Towing_byHook")
			hookType.func = TowTruckMod.Hook.flipVehicleAction
			hookType.towingVehicle = vehicleA
			hookType.towedVehicle = vehicleB
			hookType.texture = getTexture("media/textures/flipVehicle.png")
		table.insert(centerHook, hookType)
	end
	return centerHook
end

-----------------------------------------------------------	

function TowTruckMod.Utils.getTowableVehicleNear(square, ignoreVehicle, attachmentA, attachmentB)
	for y=square:getY() - 6,square:getY()+6 do
		for x=square:getX()-6,square:getX()+6 do
			local square2 = getCell():getGridSquare(x, y, square:getZ())
			if square2 then
				for i=1,square2:getMovingObjects():size() do
					local obj = square2:getMovingObjects():get(i-1)
					if instanceof(obj, "BaseVehicle") and obj ~= ignoreVehicle and ignoreVehicle:canAttachTrailer(obj, attachmentA, attachmentB) then
						return obj
					end
				end
			end
		end
	end
	return nil
end

-----------------------------------------------------------

--- Fix mods that add vehicles without tow attachments
local function fixTowAttachmentsForOtherVehicleMods()
	local scriptManager = getScriptManager()
	local vehicleScripts = scriptManager:getAllVehicleScripts()

	for i = 0, vehicleScripts:size()-1 do
		local script = vehicleScripts:get(i)
		local wheelCount = script:getWheelCount()

		local attachHeightOffset = -0.5
		if wheelCount > 0 then 
			attachHeightOffset = script:getWheel(0):getOffset():y() + 0.1
		end

		local trailerAttachment = script:getAttachmentById("trailer")
		if trailerAttachment == nil then
--			local attach = ModelAttachment.new("trailer")
--			attach:getOffset():set(0, attachHeightOffset, -script:getPhysicsChassisShape():z()/2 - 0.1)
--			attach:setZOffset(-1)

--			script:addAttachment(attach)
		end

		local trailerAttachment = script:getAttachmentById("trailerfront")
		if trailerAttachment == nil then
--			local attach = ModelAttachment.new("trailerfront")
--			attach:getOffset():set(0, attachHeightOffset, script:getPhysicsChassisShape():z()/2 + 0.1)
--			attach:setZOffset(1)

--			script:addAttachment(attach)
		end
			
		local trailerAttachment = script:getAttachmentById("flipNode")
		if trailerAttachment == nil then
			local attach = ModelAttachment.new("flipNode")
			attach:getOffset():set(0, attachHeightOffset, 0)
			attach:setZOffset(0)

			script:addAttachment(attach)	
		end
	end
end

Events.OnGameBoot.Add(fixTowAttachmentsForOtherVehicleMods)
end