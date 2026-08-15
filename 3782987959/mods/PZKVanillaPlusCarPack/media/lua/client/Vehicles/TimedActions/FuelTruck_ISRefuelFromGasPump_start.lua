ISRefuelFromGasPumpPZK = ISBaseTimedAction:derive("ISRefuelFromGasPump")

function ISRefuelFromGasPumpPZK:isValid()
    return true
end

function ISRefuelFromGasPumpPZK:waitToStart()
    self.character:faceThisObject(self.vehicle)
    return self.character:shouldBeTurning()
end

function ISRefuelFromGasPumpPZK:update()
    local litres = self.tankStart + (self.tankTarget - self.tankStart) * self:getJobDelta()
    litres = math.floor(litres)
    if litres ~= self.amountSent then
        local args = { vehicle = self.vehicle:getId(), part = self.part:getId(), amount = litres }
        sendClientCommand(self.character, 'vehicle', 'setContainerContentAmount', args)
        self.amountSent = litres

    end

    local pumpUnits = self.pumpStart + (self.pumpTarget - self.pumpStart) * self:getJobDelta()
    pumpUnits = math.ceil(pumpUnits)
    self.fuelStation:setPipedFuelAmount(pumpUnits);

    self.character:setMetabolicTarget(Metabolics.HeavyDomestic);
end


	function ISRefuelFromGasPumpPZK:start()
		self.tankStart = self.part:getContainerContentAmount()
		-- Pumps start with 100 units of fuel.  8 pump units = 1 PetrolCan according to ISTakeFuel.
		--self.pumpStart = tonumber(self.square:getProperties():Val("fuelAmount"))
		--print("GAS IN PUMP: " .. tostring(self.pumpStart))
		self.pumpStart = 1000
		local pumpLitresAvail = self.pumpStart * (Vehicles.JerryCanLitres / 8)
		local tankLitresFree = self.part:getContainerCapacity() - self.tankStart
		local takeLitres = math.min(tankLitresFree, pumpLitresAvail)
		self.tankTarget = self.tankStart + takeLitres
		self.pumpTarget = self.pumpStart - takeLitres / (Vehicles.JerryCanLitres / 8)
		self.amountSent = self.tankStart

		self.action:setTime(takeLitres * 50)

		self:setActionAnim("fill_container_tap")
		self:setOverrideHandModels(nil, nil)

   		 self.sound = self.character:playSound("GeneratorLoop")
 	   self.sound2 = self.character:playSound("GetWaterFromTapMetalBig")
 	   addSound(self.character, self.character:getX(), self.character:getY(), self.character:getZ(), 10, 1)
	end

---stopSound
function ISRefuelFromGasPumpPZK:stopSound()
    if self.sound and self.character:getEmitter():isPlaying(self.sound) then
        self.character:stopOrTriggerSound(self.sound);
        self.character:stopOrTriggerSound(self.sound2);
    end
end

function ISRefuelFromGasPumpPZK:stop()
    ISBaseTimedAction.stop(self)
    self:stopSound()

end

function ISRefuelFromGasPumpPZK:perform()
    -- needed to remove from queue / start next.
    ISBaseTimedAction.perform(self)
    self:stopSound()
end

function ISRefuelFromGasPumpPZK:new(character, part, fuelStation, time)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.character = character
    o.vehicle = part:getVehicle()
    o.part = part
    o.fuelStation = fuelStation;
    o.maxTime = math.max(time, 50)
    return o
end
