require "TimedActions/ISBaseTimedAction"

EFZ_OpenFloorPlanAction = ISBaseTimedAction:derive("EFZ_OpenFloorPlanAction")

local PLAN_CONFIG = {
    ["EFZ.LivingSpaceFloorPlanUpper"] = {
        jobTextKey = "ContextMenu_Open_LivingSpaceFloorPlanUpper",
    },
    ["EFZ.LivingSpaceFloorPlanLower"] = {
        jobTextKey = "ContextMenu_Open_LivingSpaceFloorPlanLower",
    },
}

local function getFullTypeSafe(item)
    if not item or not item.getFullType then
        return nil
    end
    local ok, res = pcall(function()
        return item:getFullType()
    end)
    if not ok then
        return nil
    end
    return res
end

local function getPlanConfig(item)
    local fullType = getFullTypeSafe(item)
    if not fullType then
        return nil
    end
    return PLAN_CONFIG[fullType]
end

local function removeItemFromItsContainer(character, item)
    if not item then
        return false
    end

    if character and character.removeFromHands then
        pcall(function()
            character:removeFromHands(item)
        end)
    end

    local container = item.getContainer and item:getContainer() or nil
    if container and container.Remove then
        container:Remove(item)
        if sendRemoveItemFromContainer then
            sendRemoveItemFromContainer(container, item)
        end
        return true
    end

    local inv = character and character.getInventory and character:getInventory() or nil
    if inv and inv.Remove then
        inv:Remove(item)
        if sendRemoveItemFromContainer then
            sendRemoveItemFromContainer(inv, item)
        end
        return true
    end

    return false
end

function EFZ_OpenFloorPlanAction:isValid()
    if not self.character or not self.item then
        return false
    end

    -- Don't allow reading while driving/moving.
    local vehicle = self.character.getVehicle and self.character:getVehicle() or nil
    if vehicle and vehicle.isDriver and vehicle:isDriver(self.character) then
        if vehicle.isEngineRunning and vehicle.getSpeed2D then
            return (not vehicle:isEngineRunning()) or vehicle:getSpeed2D() == 0
        end
    end

    local inv = self.character.getInventory and self.character:getInventory() or nil
    if not (inv and inv.contains and inv:contains(self.item) == true) then
        return false
    end

    return getPlanConfig(self.item) ~= nil
end

function EFZ_OpenFloorPlanAction:getDuration()
    -- B42+: duration is calculated on server to prevent cheating.
    return 200
end

function EFZ_OpenFloorPlanAction:start()
    if self.item and self.item.setJobType then
        local job = self.jobType
        if not job and getText then
            local cfg = getPlanConfig(self.item)
            local key = cfg and cfg.jobTextKey
            if key then
                local ok, text = pcall(getText, key)
                if ok and type(text) == "string" and text ~= key then
                    job = text
                end
            end
        end
        self.item:setJobType(job or "Read")
        self.item:setJobDelta(0.0)
    end

    self:setAnimVariable("ReadType", self.readType or "book")
    self:setActionAnim(CharacterActionAnims.Read)
    self:setOverrideHandModels(nil, self.item)

    if self.character and self.character.setReading then
        self.character:setReading(true)
    end
    if self.character and self.character.reportEvent then
        self.character:reportEvent("EventRead")
    end

    if self.openSound and self.openSound ~= "" and self.character and self.character.playSound then
        self.character:playSound(self.openSound)
    end
end

function EFZ_OpenFloorPlanAction:update()
    if self.item and self.item.setJobDelta then
        self.item:setJobDelta(self:getJobDelta())
    end
end

function EFZ_OpenFloorPlanAction:stop()
    if self.character and self.character.setReading then
        self.character:setReading(false)
    end
    if self.item and self.item.setJobDelta then
        self.item:setJobDelta(0.0)
    end

    if self.closeSound and self.closeSound ~= "" and self.character and self.character.playSound then
        self.character:playSound(self.closeSound)
    end

    ISBaseTimedAction.stop(self)
end

-- Client-only: animations/sounds. No world/item manipulation here.
function EFZ_OpenFloorPlanAction:perform()
    if self.character and self.character.setReading then
        self.character:setReading(false)
    end
    if self.item and self.item.setJobDelta then
        self.item:setJobDelta(0.0)
    end

    if self.closeSound and self.closeSound ~= "" and self.character and self.character.playSound then
        self.character:playSound(self.closeSound)
    end

    -- MP: action 완료 시 서버에 요청 -> 서버가 전체 클라에 브로드캐스트
    if isClient and isClient() and sendClientCommand then
        local fullType = getFullTypeSafe(self.item)
        local itemId = nil
        if self.item and self.item.getID then
            local ok, res = pcall(function()
                return self.item:getID()
            end)
            if ok then
                itemId = tonumber(res) or nil
            end
        end
        if fullType then
            sendClientCommand("EFZ", "RequestOpenFloorPlan", { fullType = fullType, itemId = itemId })
        end
    end

    ISBaseTimedAction.perform(self)
end

-- Server-only (and singleplayer): open the space + consume the item.
function EFZ_OpenFloorPlanAction:complete()
    -- MP 서버에서는 complete()에서 월드/아이템을 직접 처리하지 않는다.
    -- (클라->서버 커맨드 처리에서 아이템 제거 + 서버->클라 브로드캐스트로 개방)
    if isServer and isServer() then
        return true
    end

    -- 싱글플레이: "개방 상태 저장" 후 아이템 소비, 그리고 (로드되어 있으면) 즉시 적용 시도
    local fullType = getFullTypeSafe(self.item)
    if not fullType then
        return true
    end

    pcall(require, "EFZ_FloorPlan_Shared")
    if EFZ and EFZ.FloorPlan and EFZ.FloorPlan.MarkOpened then
        EFZ.FloorPlan.MarkOpened(fullType)
    end

    -- 설계도는 개방 여부와 무관하게 소비(청크가 아직 로드되지 않았어도 이후 로드시 적용됨)
    removeItemFromItsContainer(self.character, self.item)

    if EFZ and EFZ.FloorPlan and EFZ.FloorPlan.OpenByFullType then
        EFZ.FloorPlan.OpenByFullType(fullType)
    end

    return true
end

function EFZ_OpenFloorPlanAction:new(character, item)
    local o = ISBaseTimedAction.new(self, character)
    o.item = item

    o.readType = "book"
    o.jobType = nil
    o.openSound = "OpenBook"
    o.closeSound = "CloseBook"

    o.stopOnWalk = true
    o.stopOnRun = true
    o.useProgressBar = true

    o.maxTime = o:getDuration()
    return o
end


