--*********************************************************** 
--**			PZK FORGE - Peter Hammerman				   ** 
--***********************************************************
require "Vehicles/ISUI/ISVehicleMenu"
require "ISBaseTimedAction"
local old_ISVehicleMenu_FillPartMenu = ISVehicleMenu.FillPartMenu

VehicleUnrust_ISVehicleMenu = VehicleUnrust_ISVehicleMenu or {}


ISUnrustVehicleAction = ISBaseTimedAction:derive("ISUnrustVehicleAction")

function ISUnrustVehicleAction:isValid()
    if not self.character or not self.vehicle then return false end
    if not self.solvent or not self.sheets then return false end
    if not self.vehicle.getRust then return false end
    if self.vehicle:getRust() == 0 then return false end
    return true
end

function ISUnrustVehicleAction:waitToStart()

    self.character:faceThisObject(self.vehicle)
    return self.character:shouldBeTurning()
end

function ISUnrustVehicleAction:start()

    if self.solvent and self.solvent.getUsedDelta then
        self.itemStart = self.solvent:getUsedDelta() or 1.0
    else
        self.itemStart = 1.0
    end

    self.itemTarget = math.max(0, self.itemStart - 0.1)

    self:setActionAnim("refuelgascan")

    if self.setLoopedAction then
        self:setLoopedAction(true)
    end

    local primaryModel = (self.solvent and self.solvent.getStaticModel) and self.solvent:getStaticModel() or nil
    local secondaryModel = (self.sheets  and self.sheets.getStaticModel)  and self.sheets:getStaticModel()  or nil
    if self.setOverrideHandModels then
        self:setOverrideHandModels(primaryModel, secondaryModel)
    end

    self.character:faceThisObject(self.vehicle)

end

function ISUnrustVehicleAction:update()
    if self.vehicle then
        self.character:faceThisObject(self.vehicle)
    end


    if self.solvent and self.solvent.setJobDelta then
        self.solvent:setJobDelta(self:getJobDelta())
    end


    if self.character then
        self.character:setMetabolicTarget(Metabolics.LightWork)
    end
end

function ISUnrustVehicleAction:stop()

    if self.solvent and self.solvent.setJobDelta then
        self.solvent:setJobDelta(0)
    end


    ISBaseTimedAction.stop(self)
end

function ISUnrustVehicleAction:perform()
    local char = self.character
    local solvent = self.solvent
    local sheets = self.sheets
    local vehicle = self.vehicle


    if solvent and solvent.setJobDelta then solvent:setJobDelta(0) end


    if solvent then
        local inv = char:getInventory()
        local inPrimary = (char:getPrimaryHandItem() == solvent)
        local inSecondary = (char:getSecondaryHandItem() == solvent)

        local cont = solvent:getContainer()
        if cont then
            cont:Remove(solvent)
        else
            if inv and inv:contains(solvent) then inv:Remove(solvent) end
        end

        local bottle = nil
		if solvent.getFullType == "Base.pzkRusteeze" or solvent.getFullType == "Base.pzkRustSolvent" then
		
			if inv then bottle = inv:AddItem("Base.pzkRustSolventBottle") end
			
		elseif solvent.getFullType == "Base.Pop2" then
			if inv then bottle = inv:AddItem("Base.Pop2Empty") end
		end

        if inPrimary and bottle and char.setPrimaryHandItem then
            if inv and inv:contains(bottle) then
                char:setPrimaryHandItem(bottle)
            else
                char:setPrimaryHandItem(bottle)
            end
        elseif inSecondary and bottle and char.setSecondaryHandItem then
            if inv and inv:contains(bottle) then
                char:setSecondaryHandItem(bottle)
            else
                char:setSecondaryHandItem(bottle)
            end
        end
    end


    if sheets then
        local inv = char:getInventory()
        local inPrimary = (char:getPrimaryHandItem() == sheets)
        local inSecondary = (char:getSecondaryHandItem() == sheets)

        local cont = sheets:getContainer()
        if cont then
            cont:Remove(sheets)
        else
            if inv and inv:contains(sheets) then inv:Remove(sheets) end
        end

        local dirtyItem = nil
        if inv then dirtyItem = inv:AddItem("Base.RippedSheetsDirty") end

        if inPrimary and dirtyItem and char.setPrimaryHandItem then
            if inv and inv:contains(dirtyItem) then
                char:setPrimaryHandItem(dirtyItem)
            else
                char:setPrimaryHandItem(dirtyItem)
            end
        elseif inSecondary and dirtyItem and char.setSecondaryHandItem then
            if inv and inv:contains(dirtyItem) then
                char:setSecondaryHandItem(dirtyItem)
            else
                char:setSecondaryHandItem(dirtyItem)
            end
        end
        -- otherwise dirty remains in inventory
    end

    if vehicle and vehicle.setRust then
        vehicle:setRust(0)
    end


    ISBaseTimedAction.perform(self)
end

function ISUnrustVehicleAction:new(character, vehicle, solvent, sheets, time)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.character = character
    o.vehicle = vehicle
    o.solvent = solvent
    o.sheets = sheets
    o.maxTime = time or 200
    o.stopOnWalk = true
    o.stopOnRun = true
    return o
end


-- ---------------------------------------------------------------------------

local function getItemFullType(item)
    if not item then return "" end
    if item.getFullType then
        return item:getFullType()
    end
    if item.getModule and item.getType then
        return item:getModule() .. "." .. item:getType()
    end
    if item.getType then
        return item:getType()
    end
    return ""
end


local function findItemByFullType(playerObj, wantedFullType)
    if not playerObj or not wantedFullType or wantedFullType == "" then return nil end

    -- hands
    local it = playerObj:getPrimaryHandItem()
    if it and getItemFullType(it) == wantedFullType then return it end
    it = playerObj:getSecondaryHandItem()
    if it and getItemFullType(it) == wantedFullType then return it end

    -- inventory
    local inv = playerObj:getInventory()
    if inv and inv.getItems then
        local items = inv:getItems()
        for i = 0, items:size() - 1 do
            local item = items:get(i)
            if item and getItemFullType(item) == wantedFullType then
                return item
            end
        end
    end

    -- worn items
    if playerObj.getWornItems and playerObj:getWornItems() then
        local wornContainer = playerObj:getWornItems()
        if wornContainer and wornContainer.getItems then
            local worn = wornContainer:getItems()
            for i = 0, worn:size() - 1 do
                local item = worn:get(i)
                if item and getItemFullType(item) == wantedFullType then
                    return item
                end
            end
        end
    end

    return nil
end

local function findSolventAndSheets(playerObj)
    if not playerObj then return nil, nil end
    local sheets = findItemByFullType(playerObj, "Base.RippedSheets")


    local solvent = findItemByFullType(playerObj, "Base.Rusteeze")
        or findItemByFullType(playerObj, "Base.pzkRusteeze")
        or findItemByFullType(playerObj, "Base.pzkRustSolvent")
		or findItemByFullType(playerObj, "Base.Pop2")

    return solvent, sheets
end


function ISVehicleMenu.FillPartMenu(playerIndex, context, slice, vehicle)
    local playerObj = getSpecificPlayer(playerIndex)
    if not playerObj or not vehicle then
        old_ISVehicleMenu_FillPartMenu(playerIndex, context, slice, vehicle)
        return
    end

    -- distance guard
  --  if playerObj:DistToProper(vehicle) >= 8 then
   --     old_ISVehicleMenu_FillPartMenu(playerIndex, context, slice, vehicle)
   --     return
  --  end


    if vehicle.getRust and vehicle:getRust() > 0 then

        local solventItem, sheetsItem = findSolventAndSheets(playerObj)
        if solventItem and sheetsItem then
            local label = "Remove Rust"
            local iconPath = "media/ui/vehicles/PZK_Remove_Rust.png"
            if slice then
                slice:addSlice(label, getTexture(iconPath), function(player, veh, sol, sh)

                    ISTimedActionQueue.add(ISUnrustVehicleAction:new(playerObj, vehicle, solventItem, sheetsItem, 400))
                end, playerObj, vehicle, solventItem, sheetsItem)
            else
                local option = context:addOption(label, playerObj, function(player, veh, sol, sh)
                    ISTimedActionQueue.add(ISUnrustVehicleAction:new(playerObj, vehicle, solventItem, sheetsItem, 400))
                end, playerObj, vehicle, solventItem, sheetsItem)
                local tooltip = ISToolTip:new()
                tooltip:setName(label)
                tooltip.description = "Use Rust Solvent and Ripped Sheets to remove rust from this vehicle." -- to be localized
                tooltip.maxLineWidth = 512
                option.toolTip = tooltip
            end
        end
    end


    old_ISVehicleMenu_FillPartMenu(playerIndex, context, slice, vehicle)
end
