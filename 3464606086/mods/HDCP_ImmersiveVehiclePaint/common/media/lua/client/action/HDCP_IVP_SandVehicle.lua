local HDCP_IVP_SandVehicle = {}

function HDCP_IVP_SandVehicle.new(deps)
    local Constants        = deps and deps.Constants or require('HDCP_IVP_Constants')
    local BaseTimedAction  = deps and deps.ISBaseTimedAction or ISBaseTimedAction
    local VehicleMechanics = deps and deps.ISVehicleMechanics or ISVehicleMechanics
    local Queue            = deps and deps.ISTimedActionQueue or ISTimedActionQueue
    local PathFind         = deps and deps.ISPathFindAction or ISPathFindAction

    local function getNextAvailableSurfaceAreaFrom(surfaceAreas, surfaceArea)
        local found = nil

        for i, surface in ipairs(surfaceAreas) do
            if surfaceArea == surface then
                found = surfaceAreas[i + 1]
                break
            end
        end

        if found then return found end

        return surfaceAreas[1]
    end

    local function calculateTaskTime(task, surfaceArea)
        if VehicleMechanics.cheat then return 1 end

        local randomTimeVariation = ZombRand(task.MIN, task.MAX)

        local result = surfaceArea / 1.5 * randomTimeVariation * 30

        return math.ceil(result)
    end

    ---@class SandVehicleAction : ISBaseTimedAction
    ---@field vehicle BaseVehicle
    ---@field counter number
    ---@field totalAreas number
    ---@field requiredUses number
    ---@field surface string
    local module = BaseTimedAction:derive('SandVehicleAction')

    ---@param self SandVehicleAction
    function module:new(player, vehicle, surfaceArea, counter)
        local data = vehicle:getModData().IVP

        local o    = BaseTimedAction.new(self, player)

        setmetatable(o, self)
        self.__index   = self

        o.stopOnWalk   = true
        o.stopOnRun    = true
        o.character    = player
        o.maxTime      = calculateTaskTime(
            Constants.TASK_DURATION.SAND, data.surfaceAreaSizes[surfaceArea]
        )

        ---@cast o SandVehicleAction
        o.vehicle      = vehicle
        o.counter      = counter or 1
        o.totalAreas   = data.totalAreas
        o.requiredUses = data.requiredPrimerUses
        o.surface      = getNextAvailableSurfaceAreaFrom(
            data.surfaceAreas, surfaceArea
        )

        return o
    end

    function module:isValid()
        return self.vehicle and not self.vehicle:isRemovedFromWorld()
    end

    function module:waitToStart()
        self.character:faceThisObject(self.vehicle)

        return self.character:shouldBeTurning()
    end

    function module:start()
        addSound(
            self.character,
            self.character:getX(),
            self.character:getY(),
            self.character:getZ(),
            Constants.SOUND.RADIUS,
            Constants.SOUND.VOLUME
        )

        self.sound = getSoundManager():PlayWorldSound(
            'ImmersiveVehiclePaint_SandingSound',
            false,
            self.character:getSquare(),
            0,
            Constants.SOUND.RADIUS,
            1,
            false
        )

        self:setActionAnim("VehicleWash")
        self:setOverrideHandModels(nil, nil)
    end

    function module:perform()
        if self.sound and self.sound:isPlaying() then
            self.sound:stop()
        end

        if self.counter == self.totalAreas then
            sendClientCommand(self.character, "vehicle", "setRust", {
                vehicle = self.vehicle:getId(),
                rust = 0.0
            })

            sendClientCommand(self.character, "vehicle", 'setHSV', {
                vehicle = self.vehicle:getId(),
                h = 0.56,
                s = 0.3,
                v = 0.6
            })
            self.vehicle:getModData().IVP['isSanded'] = true
            self.vehicle:transmitModData()
        else
            self.counter = self.counter + 1

            Queue.addAfter(self, module:new(
                self.character, self.vehicle, self.surface, self.counter
            ))

            Queue.addAfter(self, PathFind:pathToVehicleArea(
                self.character, self.vehicle, self.surface
            ))
        end

        BaseTimedAction.perform(self)
    end

    function module:stop()
        if self.sound then self.sound:stop() end

        BaseTimedAction.stop(self)
    end

    return module
end

return HDCP_IVP_SandVehicle
