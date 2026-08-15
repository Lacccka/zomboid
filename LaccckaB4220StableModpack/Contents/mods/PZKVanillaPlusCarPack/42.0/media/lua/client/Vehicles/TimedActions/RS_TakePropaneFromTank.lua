if not getActivatedMods():contains("RS_WaterCistern") then
---------------------Code by Tread ----- (Trealak on Steam) ---------------------------------
-- inspired by FuelAPI, Propane Dispenser and Coco Liquid Overhaul by Konijima, Fuel Trailers and Trucks by Filibuster Rhymes and TMC (Tsar's Modding Company) ----------

require "TimedActions/ISBaseTimedAction"

ISTakePropaneActionFromTank = ISBaseTimedAction:derive("ISTakeGasolineFromVehicle")

function ISTakePropaneActionFromTank:isValid()
	return true;
end

function ISTakePropaneActionFromTank:waitToStart()
	self.character:faceThisObject(self.vehicle)
	return self.character:shouldBeTurning()
end

function ISTakePropaneActionFromTank:update()
	self.character:faceThisObject(self.vehicle)
	self.item:setJobDelta(self:getJobDelta())
	self.item:setJobType(getText("ContextMenu_Fill") .. self.item:getName())
--	self.itemCapacity = math.floor(1.0 / self.item:getCurrentUsesFloat() + 0.0001)
	local litres = self.tankStart + (self.tankTarget - self.tankStart) -- * self:getJobDelta()
	litres = math.floor(litres + 0.5)
	local litresTaken = self.tankStart - self.finalUsedDelta
	--local litresTaken = self.tankStart - 1.0
	if litresTaken then -- ~= self.amountSent then
		local args = { vehicle = self.vehicle:getId(), part = self.part:getId(), amount = litresTaken }
		sendClientCommand(self.character, 'vehicle', 'setContainerContentAmount', args)
		self.amountSent = litres
	end

	--local litresTaken = self.tankStart - litres
	--local usedDelta = self.startUsedDelta + litresTaken * self.item:getCurrentUsesFloat()

	--self.item:setCurrentUsesFloat(usedDelta);

	--self.item:setCurrentUsesFloat(math.max(0.01, self.itemCapacity));

	self.item:setCurrentUsesFloat(1.0);

    self.character:setMetabolicTarget(Metabolics.HeavyDomestic);
end

function ISTakePropaneActionFromTank:start()
	self.partData = self.part:getModData()

	self.tankStart = self.part:getContainerContentAmount()
	self.item:setBeingFilled(true)
	self.startUsedDelta = self.item:getCurrentUsesFloat()
	self.itemCapacity = math.floor(1.0 / self.item:getCurrentUsesFloat() + 0.0001)
	
	self.itemAvSpace = self.itemCapacity - self.item:getCurrentUsesFloat()
	self.PropaneUnit = math.min(self.itemAvSpace, self.tankStart)
	self.endUsedDelta = math.min(self.startUsedDelta + self.PropaneUnit * self.item:getCurrentUsesFloat(), 1.0)
	--self.finalUsedDelta = math.min(self.endUsedDelta, 1.0)
	--self.finalUsedDelta = 1.0
	self.finalUsedDelta = 1.0 - self.startUsedDelta
	print("itemAvSpace " .. tostring(self.itemAvSpace))
	print("PropaneUnit " .. tostring(self.PropaneUnit))
	print("endUsedDelta " .. tostring(self.endUsedDelta))
	print("startUsedDelta " .. tostring(self.startUsedDelta))
	print("itemCapacity " .. tostring(self.itemCapacity))	
	print("tankStart " .. tostring(self.tankStart))
	print("finalUsedDelta " .. tostring(self.finalUsedDelta))
	
	local take = math.min(self.PropaneUnit, self.tankStart)
	self.tankTarget = self.tankStart - take

	--print('Item Capacity=' .. self.itemCapacity ..'take=' .. take .. ' Av Space=' .. self.itemAvSpace)
	self.amountSent = math.ceil(self.tankStart)
	--self.action:setTime((self.PropaneUnit * 15) + 30)
	self.action:setTime(self.PropaneUnit)

	self:setActionAnim("fill_container_tap")
	self:setOverrideHandModels(nil, self.item:getStaticModel())
	
	self.sound = self.character:playSound("GetWaterFromTap")
    addSound(self.character, self.character:getX(), self.character:getY(), self.character:getZ(), 10, 1)	

	--local litresTaken = self.tankStart - litres
	--local usedDelta = self.startUsedDelta + litresTaken * self.item:getCurrentUsesFloat()

	--self.item:setCurrentUsesFloat(self.itemCapacity);

	--self.item:setCurrentUsesFloat(self.finalUsedDelta);

	--local fluidContainer = self.item:getFluidContainerFromSelfOrWorldItem()
	--fluidContainer:addFluid("Propane", self.itemCapacity)
	--self.item:setCurrentUsesFloat(usedDelta);
end

---stopSound
function ISTakePropaneActionFromTank:stopSound()
    if self.sound and self.character:getEmitter():isPlaying(self.sound) then
        self.character:stopOrTriggerSound(self.sound);
    end
end

function ISTakePropaneActionFromTank:stop()
	self:stopSound()
	self.item:setJobDelta(0)
	self.item:setBeingFilled(false)
	local currentDelta = self.item:getCurrentUsesFloat()
	
	if currentDelta < 0 then -- makes bottle empty again if not filled with any Propane units
		self.item:Use()
	elseif  currentDelta < 1 and currentDelta > (1 - self.item:getCurrentUsesFloat()) then
		self.item:setCurrentUsesFloat(1);
	end
	------------------------------------------------------
	
	ISBaseTimedAction.stop(self)
end

function ISTakePropaneActionFromTank:perform()
	self:stopSound()
	self.item:setJobDelta(0)
	self.item:setBeingFilled(false)
	local currentDelta = self.item:getCurrentUsesFloat()
	local containerCapacity = 1.0
	
	if currentDelta < 0 then -- makes bottle empty again if not filled with any Propane units
		self.item:Use()
	elseif  currentDelta < containerCapacity and currentDelta > (containerCapacity - self.item:getCurrentUsesFloat()) then
		self.item:setCurrentUsesFloat(containerCapacity);
	end

	-- needed to remove from queue / start next.
	ISBaseTimedAction.perform(self)
end

function ISTakePropaneActionFromTank:new(character, part, item, time)
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