require "TimedActions/ISBaseTimedAction"

PZKISTakePropaneFromTank = ISBaseTimedAction:derive("ISTakeGasolineFromVehicle")

function PZKISTakePropaneFromTank:isValid()
	return true;
end

function PZKISTakePropaneFromTank:waitToStart()
	self.character:faceThisObject(self.vehicle)
	return self.character:shouldBeTurning()
end

function PZKISTakePropaneFromTank:update()
	self.character:faceThisObject(self.vehicle)
	self.item:setJobDelta(self:getJobDelta())
	self.item:setJobType(getText("ContextMenu_Fill") .. self.item:getName())
	local litres = self.tankStart + (self.tankTarget - self.tankStart) * self:getJobDelta()
	litres = math.floor(litres + 0.5)
	if litres ~= self.amountSent then
		local args = { vehicle = self.vehicle:getId(), part = self.part:getId(), amount = litres }
		sendClientCommand(self.character, 'vehicle', 'setContainerContentAmount', args)
		self.amountSent = litres
	end
	
	local litresTaken = self.tankStart - litres
	
	local usedDelta = self.startUsedDelta + litresTaken * self.item:getCurrentUsesFloat ()
	self.item:setUsedDelta(usedDelta);
	
    self.character:setMetabolicTarget(Metabolics.HeavyDomestic);
end

function PZKISTakePropaneFromTank:start()
	self.partData = self.part:getModData()

	if self.item:getType() == "PropaneTank" and self.item:getCurrentUsesFloat () < 1 then 
		
		-- we create the item which contain our water - Tread --
		local wasPrimary = self.character:getPrimaryHandItem() == self.item
		local wasSecondary = self.character:getSecondaryHandItem() == self.item
		local oldItem = self.item
		local newItemType = oldItem:getReplaceOnUseOn()
		newItemType = string.sub(newItemType,13)
		newItemType = oldItem:getModule() .. "." .. newItemType;
		local newItem = InventoryItemFactory.CreateItem(newItemType,0)
		newItem:setCondition(oldItem:getCondition())
		newItem:setFavorite(oldItem:isFavorite())
		oldItem = nil		
		self.character:getInventory():DoRemoveItem(self.item)
		self.item = self.character:getInventory():AddItem(newItem)
		self.item:setUsedDelta(0)

		if wasPrimary then
			self.character:setPrimaryHandItem(self.item)
		end
		if wasSecondary then
			self.character:setSecondaryHandItem(self.item)
		end
	end		
	
	self.tankStart = self.part:getContainerContentAmount()
	self.item:setBeingFilled(true)
	self.startUsedDelta = self.item:getCurrentUsesFloat ()
	self.itemCapacity = math.floor(1.0 / self.item:getCurrentUsesFloat () + 0.0001)
	
	self.itemAvSpace = self.itemCapacity - self.item:getCurrentUses()
	self.waterUnit = math.min(self.itemAvSpace, self.tankStart)
	self.endUsedDelta = math.min(self.startUsedDelta + self.waterUnit * self.item:getCurrentUsesFloat (), 1.0)
	
	local take = math.min(self.waterUnit, self.tankStart)
	self.tankTarget = self.tankStart - take

	--print('Item Capacity=' .. self.itemCapacity ..'take=' .. take .. ' Av Space=' .. self.itemAvSpace)
	self.amountSent = math.ceil(self.tankStart)
	self.action:setTime((self.waterUnit * 15) + 30)

	self:setActionAnim("fill_container_tap")
	self:setOverrideHandModels(nil, self.item:getStaticModel())
	
	self.sound = self.character:playSound("GetWaterFromTap")
    addSound(self.character, self.character:getX(), self.character:getY(), self.character:getZ(), 10, 1)	
	
end

---stopSound
function PZKISTakePropaneFromTank:stopSound()
    if self.sound and self.character:getEmitter():isPlaying(self.sound) then
        self.character:stopOrTriggerSound(self.sound);
    end
end

function PZKISTakePropaneFromTank:stop()
	self:stopSound()
	self.item:setJobDelta(0)
	self.item:setBeingFilled(false)
	local currentDelta = self.item:getCurrentUsesFloat ()
	
	if currentDelta <= 0 then -- makes bottle empty again if not filled with any water units
		self.item:Use()
	elseif  currentDelta < 1 and currentDelta > (1 - self.item:getCurrentUsesFloat ()) then
		self.item:setUsedDelta(1);
	end 
	
	ISBaseTimedAction.stop(self)
end

function PZKISTakePropaneFromTank:perform()
	self:stopSound()
	self.item:setJobDelta(0)
	self.item:setBeingFilled(false)
	local currentDelta = self.item:getCurrentUsesFloat ()
	
	if currentDelta <= 0 then -- makes bottle empty again if not filled with any water units
		self.item:Use()
	elseif  currentDelta < 1 and currentDelta > (1 - self.item:getCurrentUsesFloat ()) then
		self.item:setUsedDelta(1);
	end
	
	ISBaseTimedAction.perform(self)
end

function PZKISTakePropaneFromTank:new(character, part, item, time, filter)
	local o = {}
	setmetatable(o, self)
	self.__index = self
	o.character = character
	o.vehicle = part:getVehicle()
	o.part = part
	o.item = item
	o.maxTime = time
	o.filter = filter
	return o
end
