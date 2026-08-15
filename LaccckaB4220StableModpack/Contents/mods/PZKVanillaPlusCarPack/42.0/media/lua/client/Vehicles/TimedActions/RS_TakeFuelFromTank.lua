if not getActivatedMods():contains("RS_WaterCistern")  then
---------------------Code by Tread ----- (Trealak on Steam) ---------------------------------
-- inspired by FuelAPI, Fuel Dispenser and Coco Liquid Overhaul by Konijima, Fuel Trailers and Trucks by Filibuster Rhymes and TMC (Tsar's Modding Company) ----------

require "TimedActions/ISBaseTimedAction"

ISTakeFuelActionFromTank = ISBaseTimedAction:derive("ISTakeGasolineFromVehicle")

function ISTakeFuelActionFromTank:isValid()
	return true;
end

function ISTakeFuelActionFromTank:waitToStart()
	self.character:faceThisObject(self.vehicle)
	return self.character:shouldBeTurning()
end

function ISTakeFuelActionFromTank:update()
	self.character:faceThisObject(self.vehicle)
	self.item:setJobDelta(self:getJobDelta())
	self.item:setJobType(getText("ContextMenu_Fill") .. self.item:getName())
	local litres = self.tankStart + (self.tankTarget - self.tankStart) -- * self:getJobDelta()
	litres = math.floor(litres + 0.5)
	local litresTaken = self.tankStart - self.endUsedDelta
	if litresTaken then -- ~= self.amountSent then
		local args = { vehicle = self.vehicle:getId(), part = self.part:getId(), amount = litresTaken }
		sendClientCommand(self.character, 'vehicle', 'setContainerContentAmount', args)
		self.amountSent = litres
	end
	
	local usedDelta = self.startUsedDelta + litresTaken * self.item:getCurrentUsesFloat()
	--self.item:setCurrentUsesFloat(usedDelta);
	
    self.character:setMetabolicTarget(Metabolics.HeavyDomestic);
end

function ISTakeFuelActionFromTank:start()
	self.partData = self.part:getModData()

	--[[
	if self.item:canStoreFuel() and not self.item:isFuelSource() then -- replace empty item with matching "Fuel source" item - Tread 
		
		-- we create the item which contain our Fuel - Tread --
		local wasPrimary = self.character:getPrimaryHandItem() == self.item
		local wasSecondary = self.character:getSecondaryHandItem() == self.item
		local oldItem = self.item
		--local newItemType = oldItem:getReplaceOnUseOn()
		--newItemType = string.sub(tostring(newItemType),13)
		--newItemType = oldItem:getModule() .. "." .. newItemType;
		--local newItem = instanceItem(tostring(newItemType))
		local newItem = oldItem
		newItem:setCondition(oldItem:getCondition())
		newItem:setFavorite(oldItem:isFavorite())
		oldItem = nil		
		self.character:getInventory():DoRemoveItem(self.item)
		self.item = self.character:getInventory():AddItem(newItem)
		--self.item:setCurrentUsesFloat(0)

		if wasPrimary then
			self.character:setPrimaryHandItem(self.item)
		end
		if wasSecondary then
			self.character:setSecondaryHandItem(self.item)
		end
	end		
	]]--

	self.tankStart = self.part:getContainerContentAmount()
	self.item:setBeingFilled(true)
	self.startUsedDelta = self.item:getCurrentUsesFloat()
	self.itemCapacity = math.floor(1.0 / self.item:getCurrentUsesFloat() + 0.0001)
	
	self.itemAvSpace = self.itemCapacity - self.item:getCurrentUses()
	self.FuelUnit = math.min(self.itemAvSpace, self.tankStart)
	self.endUsedDelta = math.min(self.startUsedDelta + self.FuelUnit * self.item:getCurrentUsesFloat(), 1.0)
	
	local take = math.min(self.FuelUnit, self.tankStart)
	self.tankTarget = self.tankStart - take

	--print('Item Capacity=' .. self.itemCapacity ..'take=' .. take .. ' Av Space=' .. self.itemAvSpace)
	self.amountSent = math.ceil(self.tankStart)
	self.action:setTime((self.FuelUnit * 15) + 30)

	self:setActionAnim("fill_container_tap")
	self:setOverrideHandModels(nil, self.item:getStaticModel())
	
	self.sound = self.character:playSound("GetFuelFromTap")
    addSound(self.character, self.character:getX(), self.character:getY(), self.character:getZ(), 10, 1)	

	fluidContainer:addFluid("Petrol", self.endUsedDelta)
end

---stopSound
function ISTakeFuelActionFromTank:stopSound()
    if self.sound and self.character:getEmitter():isPlaying(self.sound) then
        self.character:stopOrTriggerSound(self.sound);
    end
end

function ISTakeFuelActionFromTank:stop()
	self:stopSound()
	self.item:setJobDelta(0)
	self.item:setBeingFilled(false)
	local currentDelta = self.item:getCurrentUsesFloat()
	
	if currentDelta <= 0 then -- makes bottle empty again if not filled with any Fuel units
		self.item:Use()
	elseif  currentDelta < 1 and currentDelta > (1 - self.item:getCurrentUsesFloat()) then
		self.item:setCurrentUsesFloat(1);
	end 
	
	------------------------------------------------------
	
	ISBaseTimedAction.stop(self)
end

function ISTakeFuelActionFromTank:perform()
	self:stopSound()
	self.item:setJobDelta(0)
	self.item:setBeingFilled(false)
	local currentDelta = self.item:getCurrentUsesFloat()
	
	if currentDelta <= 0 then -- makes bottle empty again if not filled with any Fuel units
		self.item:Use()
	elseif  currentDelta < 1 and currentDelta > (1 - self.item:getCurrentUsesFloat()) then
		self.item:setCurrentUsesFloat(1);
	end
	
	------------------------------------------------------
	
	
--	self.tankTarget = math.floor(self.tankTarget)
	
--	local args = { vehicle = self.vehicle:getId(), part = self.part:getId(), amount = self.tankTarget }
--	sendClientCommand(self.character, 'vehicle', 'setContainerContentAmount', args)
--	print('take fluid level=' .. self.part:getContainerContentAmount() .. ' usedDelta=' .. self.item:getCurrentUsesFloat())
	
	-- needed to remove from queue / start next.
	ISBaseTimedAction.perform(self)
end

function ISTakeFuelActionFromTank:new(character, part, item, time)
	local o = {}
	setmetatable(o, self)
	self.__index = self
	o.character = character
	o.vehicle = part:getVehicle()
	o.part = part
	o.item = item
	o.maxTime = time
	return o
end
end