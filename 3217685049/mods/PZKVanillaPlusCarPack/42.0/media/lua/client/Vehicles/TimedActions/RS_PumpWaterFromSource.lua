if not getActivatedMods():contains("RS_WaterCistern")  then
---------------------Code by Tread ----- (Trealak on Steam) ---------------------------------
-- inspired by FuelAPI, Water Dispenser and Coco Liquid Overhaul by Konijima, Fuel Trailers and Trucks by Filibuster Rhymes and TMC (Tsar's Modding Company) ----------

require "TimedActions/ISBaseTimedAction"

ISPumpWaterFromSource = ISBaseTimedAction:derive("ISRefuelFromGasPump")

function ISPumpWaterFromSource:isValid()
	--return self.vehicle:isInArea(self.part:getArea(), self.character)
	return true;
end

function ISPumpWaterFromSource:waitToStart()
	self.character:faceThisObject(self.vehicle)
	return self.character:shouldBeTurning()
end

function ISPumpWaterFromSource:update()
    local delta = tonumber(self:getJobDelta()) or 0
    local take  = tonumber(self.takeLitres) or 0
    local start = tonumber(self.tankStart) or 0

    local litres = math.ceil(start + take * delta)

    local tankAmount = self.part:getContainerContentAmount() or 0
    local tankChanged = tankAmount ~= self.tankTarget

    -- SAFE water check
    local sourceHasWater = true

    if self.sourceIsWaterTile == 0 then
        sourceHasWater =
            self.waterStation
            and self.waterStation.getFluidAmount
            and self.waterStation:getFluidAmount() > 0
    end

    if tankChanged and sourceHasWater then
        local args = {
            vehicle = self.vehicle:getId(),
            part = self.part:getId(),
            amount = litres
        }

        sendClientCommand(self.character, 'vehicle', 'setContainerContentAmount', args)

        -- drain only non-natural sources
        if self.sourceIsWaterTile == 0 then
            local drain = self.pumpStart - self.takeLitres * self:getJobDelta()
            drain = math.floor(drain)

            local args2 = {
                x = self.waterStation:getX(),
                y = self.waterStation:getY(),
                z = self.waterStation:getZ(),
                index = self.waterStation:getObjectIndex(),
                amount = drain
            }

            sendClientCommand(self.character, 'object', 'setWaterAmount', args2)
        end
    end
end

function ISPumpWaterFromSource:start()
    self.partData = self.part:getModData()

    -- Determine if source is infinite natural water (river/lake)
    local isWaterTile = instanceof(self.waterStation, "IsoGridSquare")

    self.sourceIsWaterTile = isWaterTile and 1 or 0

    -- Tank / source amounts
    self.tankStart = tonumber(self.part:getContainerContentAmount()) or 0

    -- Natural water = infinite
    if isWaterTile then
        self.pumpStart = math.huge
    else
        self.pumpStart = (self.waterStation and self.waterStation.getFluidAmount and self.waterStation:getFluidAmount()) or 0
    end

    local capacity = tonumber(self.part:getContainerCapacity()) or 0
    local tankLitresFree = math.max(capacity - self.tankStart, 0)

    self.takeLitres = math.min(tankLitresFree, self.pumpStart)
    self.tankTarget = self.tankStart + self.takeLitres

    if isWaterTile then
        self.pumpTarget = self.pumpStart
    else
        self.pumpTarget = self.pumpStart - self.takeLitres
    end

    self.action:setTime(self.takeLitres * 10)

    self:setActionAnim("fill_container_tap")
    self:setOverrideHandModels(nil, nil)

    self.sound = self.character:playSound("GeneratorLoop")
    self.sound2 = self.character:playSound("GetWaterFromTapMetalBig")
    addSound(self.character, self.character:getX(), self.character:getY(), self.character:getZ(), 10, 1)

    -- Taint logic
    local tainted = true

    if not isWaterTile then
        if self.waterStation and self.waterStation.isTaintedWater then
            tainted = self.waterStation:isTaintedWater()
        end
    end

    if tainted then
        sendClientCommand(
            self.character,
            "RS_Server",
            "RS_TaintPartModDataServer",
            {
                vehicle = self.vehicle:getId(),
                part = self.part:getId()
            }
        )
    end
end

---stopSound
function ISPumpWaterFromSource:stopSound()
    if self.sound and self.character:getEmitter():isPlaying(self.sound) then
        self.character:stopOrTriggerSound(self.sound);
		self.character:stopOrTriggerSound(self.sound2);
    end
end

function ISPumpWaterFromSource:stop()
	self:stopSound()

	ISBaseTimedAction.stop(self)
end

function ISPumpWaterFromSource:perform()
	self:stopSound()
	
	-- needed to remove from queue / start next.
	ISBaseTimedAction.perform(self)
end

function ISPumpWaterFromSource:new(character, part, waterStation, time)
	local o = {}
	setmetatable(o, self)
	self.__index = self
	o.character = character
	o.vehicle = part:getVehicle()
	o.part = part
	o.waterStation = waterStation;
	--o.maxTime = math.max(time, 50)
	o.maxTime = time
	return o
end

end