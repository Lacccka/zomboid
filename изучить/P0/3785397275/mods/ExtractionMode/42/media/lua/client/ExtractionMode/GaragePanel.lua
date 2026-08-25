require "ISUI/ISPanel"
require "ISUI/ISButton"
require "ISUI/ISScrollingListBox"
require "ISUI/ISTextEntryBox"
require "ISUI/ISModalDialog"
require "Vehicles/ISUI/ISUI3DScene"

ExtractionMode = ExtractionMode or {}
local Panel = ISPanel:derive("ExtractionModeGaragePanel")

local function state()
    return ExtractionMode.ClientState or {}
end

local function localPlayer()
    return getSpecificPlayer and getSpecificPlayer(0) or (getPlayer and getPlayer())
end

local function send(command, args)
    local player = localPlayer()
    if player and ExtractionMode.Client and ExtractionMode.Client.sendCommand then
        ExtractionMode.Client.sendCommand(player, command, args or {})
    end
end

local function hsvToRgb(h, s, v)
    h = math.max(0, math.min(1, tonumber(h) or 0))
    s = math.max(0, math.min(1, tonumber(s) or 0))
    v = math.max(0, math.min(1, tonumber(v) or 0.5))
    if s <= 0 then return v, v, v end
    local sector = (h * 6) % 6
    local index = math.floor(sector)
    local fraction = sector - index
    local p = v * (1 - s)
    local q = v * (1 - s * fraction)
    local t = v * (1 - s * (1 - fraction))
    if index == 0 then return v, t, p end
    if index == 1 then return q, v, p end
    if index == 2 then return p, v, t end
    if index == 3 then return p, q, v end
    if index == 4 then return t, p, v end
    return v, p, q
end

local function modelLabel(record)
    if record == nil then return "Unknown Vehicle" end
    local key = tostring(record.modelKey or "")
    local translated = key ~= "" and getTextOrNull and getTextOrNull("IGUI_VehicleName" .. key) or nil
    return translated or key ~= "" and key or tostring(record.scriptName or "Unknown Vehicle")
end

local function recordLabel(record)
    local label = nil
    if record and record.customName == true and tostring(record.name or "") ~= "" then
        label = tostring(record.name)
    else
        label = modelLabel(record)
    end
    local ordinal = math.max(1, math.floor(tonumber(record and record.nameOrdinal) or 1))
    if ordinal > 1 then
        label = label .. " " .. tostring(ordinal)
    end
    return label
end

local function garageSignature(records)
    local values = {}
    for _, record in ipairs(records or {}) do
        values[#values + 1] = table.concat({ tostring(record.id), tostring(record.name),
            tostring(record.fuel), tostring(record.batteryCharge),
            tostring(record.engineCondition), tostring(record.colorHue),
            tostring(record.colorSaturation), tostring(record.colorValue),
            tostring(record.transactionPending) }, ":")
    end
    return table.concat(values, "|")
end

function Panel:selectedRecord()
    local entry = self.vehicleList.items[self.vehicleList.selected]
    return entry and entry.item or nil
end

function Panel:close()
    self:setVisible(false)
    self:removeFromUIManager()
    if ExtractionMode.GaragePanelInstance == self then ExtractionMode.GaragePanelInstance = nil end
end

function Panel:onClose()
    self:close()
end

function Panel:refreshList(force)
    local records = state().garageVehicles or {}
    local signature = garageSignature(records)
    if not force and signature == self.garageSignature then return end
    local selected = self:selectedRecord()
    local selectedId = selected and tostring(selected.id) or self.selectedGarageId
    self.vehicleList:clear()
    local selectedIndex = 1
    for index, record in ipairs(records) do
        self.vehicleList:addItem(recordLabel(record), record)
        if selectedId and tostring(record.id) == selectedId then selectedIndex = index end
    end
    self.vehicleList.selected = #records > 0 and selectedIndex or 0
    self.garageSignature = signature
    self.selectedGarageId = nil
    self:refreshSelection(true)
end

function Panel:refreshSelection(force)
    local record = self:selectedRecord()
    local garageId = record and tostring(record.id) or nil
    local active = state().activeHideoutVehicle
    local transition = state().garageTransition
    local controlSignature = table.concat({ tostring(garageId),
        tostring(active and active.vehicleId), tostring(active and active.owner),
        tostring(active and active.name), tostring(active and active.driverUsername),
        tostring(active and active.driverGarageOwner),
        tostring(active and active.inactive), tostring(active and active.occupied),
        tostring(active and active.storing), tostring(active and active.raidReserved),
        tostring(transition and transition.busy),
        tostring(transition and transition.removalPending),
        tostring(transition and transition.swapPending) }, ":")
    if not force and controlSignature == self.controlSignature then return end
    self.controlSignature = controlSignature
    self.selectedGarageId = garageId
    self.nameEntry:setText(record and recordLabel(record) or "")
    self.nameEntry:setEditable(record ~= nil)
    self.saveNameButton.enable = record ~= nil
    self.deleteButton.enable = record ~= nil and record.transactionPending ~= true
        and transition == nil
        and not (active ~= nil and active.storing == true)
    self.deleteButton:setTooltip(transition ~= nil
        and "Wait for the current vehicle return or swap to finish before deleting a vehicle."
        or active ~= nil and active.storing == true
        and "Wait for the current vehicle return or swap to finish before deleting a vehicle."
        or record ~= nil and record.transactionPending == true
        and "This vehicle is still completing its extraction transaction."
        or record ~= nil
        and "Permanently delete the selected stored vehicle and all cargo inside it. Active vehicles must be returned first."
        or "Select a stored vehicle to delete. Return an active vehicle before deleting that vehicle.")

    local ownsActive = active ~= nil
        and tostring(active.owner or "") == tostring(state().garageOwner or "")
    local activeBlocked = active ~= nil
        and (active.storing == true or active.raidReserved == true)
    local driverOwner = active and tostring(active.driverGarageOwner or "") or ""
    local driverName = active and tostring(active.driverUsername or "") or ""
    local driverIsDifferent = driverOwner ~= ""
        and driverOwner ~= tostring(state().garageOwner or "")
    self.transferButton.enable = ownsActive and not activeBlocked and driverIsDifferent
    if active == nil then
        self.transferButton:setTooltip("There is no active hideout vehicle to transfer.")
    elseif not ownsActive then
        self.transferButton:setTooltip("Only the active vehicle's owner can transfer it.")
    elseif active.storing == true then
        self.transferButton:setTooltip("The active vehicle is already being returned.")
    elseif active.raidReserved == true then
        self.transferButton:setTooltip("The active vehicle is reserved for raid deployment.")
    elseif driverOwner == "" then
        self.transferButton:setTooltip("Another player must sit in the driver's seat before ownership can be transferred.")
    elseif not driverIsDifferent then
        self.transferButton:setTooltip("The current driver already shares your garage ownership.")
    else
        self.transferButton:setTooltip("Transfer this active vehicle to current driver "
            .. (driverName ~= "" and driverName or driverOwner) .. ".")
    end
    if active == nil then
        self.deployButton:setTitle(transition ~= nil and "PLEASE WAIT" or "SPAWN VEHICLE")
        self.deployButton.enable = transition == nil and record ~= nil
            and record.transactionPending ~= true
        self.deployButton:setTooltip(transition ~= nil
            and "The previous active vehicle is still despawning. No vehicle can be deployed until the garage transition finishes."
            or record ~= nil and record.transactionPending == true
            and "This vehicle is still completing its extraction transaction."
            or record ~= nil
            and "Deploy the selected vehicle into the hideout garage bay."
            or "Select a stored vehicle to deploy.")
        self.storeButton.enable = false
        self.storeButton:setTooltip("There is no active hideout vehicle to return.")
    else
        self.deployButton:setTitle("SWAP VEHICLE")
        local blocked = active.storing == true or active.raidReserved == true
            or (not ownsActive and active.inactive ~= true)
        self.deployButton.enable = record ~= nil and record.transactionPending ~= true and not blocked
        if record ~= nil and record.transactionPending == true then
            self.deployButton:setTooltip("This vehicle is still completing its extraction transaction.")
        elseif record == nil then
            self.deployButton:setTooltip("Select one of your stored vehicles to swap in.")
        elseif active.storing == true then
            self.deployButton:setTooltip("The active vehicle is already being returned to its owner's garage.")
        elseif active.raidReserved == true then
            self.deployButton:setTooltip("The active vehicle is reserved for a raid deployment and cannot be swapped.")
        elseif not ownsActive and active.occupied == true then
            self.deployButton:setTooltip("Another player's active vehicle is occupied and cannot be swapped.")
        elseif not ownsActive and active.inactive ~= true then
            self.deployButton:setTooltip("Another player's vehicle can only be swapped after its inactivity timer expires.")
        else
            self.deployButton:setTooltip(ownsActive
                and "Return your active vehicle and deploy the selected vehicle."
                or "Return the inactive vehicle to its owner and deploy your selected vehicle.")
        end
        self.storeButton.enable = ownsActive and active.storing ~= true
            and active.raidReserved ~= true
        if not ownsActive then
            self.storeButton:setTooltip("Only the active vehicle's owner can return it manually.")
        elseif active.storing == true then
            self.storeButton:setTooltip("Your active vehicle is already being returned.")
        elseif active.raidReserved == true then
            self.storeButton:setTooltip("Your vehicle is reserved for raid deployment and cannot be returned.")
        else
            self.storeButton:setTooltip("Save the active vehicle and all current cargo back into your garage.")
        end
    end

    if record ~= nil and self.vehicleScene and self.vehicleScene.javaObject then
        pcall(function()
            self.vehicleScene.javaObject:fromLua2("setObjectVisible", "garageVehicle", true)
            self.vehicleScene.javaObject:fromLua2("setVehicleScript", "garageVehicle",
                tostring(record.scriptName or ""))
        end)
        local r, g, b = hsvToRgb(record.colorHue, record.colorSaturation, record.colorValue)
        self.vehicleScene.backgroundColor = { r = r, g = g, b = b, a = 0.92 }
        self.vehicleScene.borderColor = { r = math.min(1, r + 0.25),
            g = math.min(1, g + 0.25), b = math.min(1, b + 0.25), a = 1 }
    else
        pcall(function()
            self.vehicleScene.javaObject:fromLua2("setObjectVisible", "garageVehicle", false)
        end)
        self.vehicleScene.backgroundColor = { r = 0.08, g = 0.09, b = 0.10, a = 1 }
        self.vehicleScene.borderColor = { r = 0.35, g = 0.35, b = 0.35, a = 1 }
    end
end

function Panel:onSaveName()
    local record = self:selectedRecord()
    if record == nil then return end
    send("GarageRename", { garageId = record.id, name = self.nameEntry:getText() })
end

function Panel:onDeploy()
    local record = self:selectedRecord()
    if record ~= nil then send("GarageDeploy", { garageId = record.id }) end
end

function Panel:onStoreActive()
    send("GarageStoreActive", {})
end

function Panel:onTransferConfirmed(button, expectedDriverUsername)
    if button and button.internal == "YES" then
        send("GarageTransferActiveToDriver", {
            expectedDriverUsername = expectedDriverUsername,
        })
    end
end

function Panel:onTransferToDriver()
    local active = state().activeHideoutVehicle
    if active == nil or tostring(active.driverUsername or "") == "" then return end
    local message = "Transfer ownership of '" .. tostring(active.name or active.scriptName or "Vehicle")
        .. "' to current driver " .. tostring(active.driverUsername) .. "?\n"
        .. "They will become the active owner, and the vehicle will return to their personal garage when stored."
    local modal = ISModalDialog:new(0, 0, 470, 160, message, true,
        self, Panel.onTransferConfirmed, nil, active.driverUsername)
    modal:initialise()
    modal:setCapture(true)
    modal:setAlwaysOnTop(true)
    modal:addToUIManager()
end

function Panel:onDeleteConfirmed(button, garageId)
    if button and button.internal == "YES" and garageId then
        send("GarageDelete", { garageId = garageId })
    end
end

function Panel:onDelete()
    local record = self:selectedRecord()
    if record == nil then return end
    local message = "Delete '" .. recordLabel(record) .. "' from your garage?\n"
        .. "This is irreversible. The vehicle and every item stored inside it will be permanently deleted."
    local modal = ISModalDialog:new(0, 0, 430, 150, message, true,
        self, Panel.onDeleteConfirmed, nil, record.id)
    modal:initialise()
    modal:setCapture(true)
    modal:setAlwaysOnTop(true)
    modal:addToUIManager()
end

function Panel:createChildren()
    ISPanel.createChildren(self)

    self.vehicleList = ISScrollingListBox:new(16, 91, 286, 445)
    self.vehicleList:initialise()
    self.vehicleList:instantiate()
    self.vehicleList.font = UIFont.Small
    self.vehicleList.itemheight = 30
    self.vehicleList.drawBorder = true
    self:addChild(self.vehicleList)

    self.vehicleScene = ISUI3DScene:new(318, 91, 446, 279)
    self.vehicleScene:initialise()
    self.vehicleScene:instantiate()
    self.vehicleScene.javaObject:fromLua1("setDrawGrid", false)
    self.vehicleScene.javaObject:fromLua1("createVehicle", "garageVehicle")
    -- Present stored vehicles from above and across both visible sides, matching
    -- PZ's isometric world view instead of the scene widget's flat default.
    self.vehicleScene:setView("UserDefined")
    self.vehicleScene.javaObject:fromLua3("setViewRotation", 30.0, 45.0, 0.0)
    self.vehicleScene.javaObject:fromLua1("setZoom", 5)
    self:addChild(self.vehicleScene)

    self.nameEntry = ISTextEntryBox:new("", 318, 408, 326, 30)
    self.nameEntry:initialise()
    self.nameEntry:instantiate()
    self.nameEntry:setMaxTextLength(48)
    self:addChild(self.nameEntry)

    self.saveNameButton = ISButton:new(652, 408, 112, 30, "SAVE NAME", self, Panel.onSaveName)
    self.saveNameButton:initialise()
    self.saveNameButton:instantiate()
    self:addChild(self.saveNameButton)

    self.deployButton = ISButton:new(318, 494, 214, 34, "SPAWN VEHICLE", self, Panel.onDeploy)
    self.deployButton:initialise()
    self.deployButton:instantiate()
    self:addChild(self.deployButton)

    self.storeButton = ISButton:new(540, 494, 224, 34, "RETURN ACTIVE VEHICLE", self,
        Panel.onStoreActive)
    self.storeButton:initialise()
    self.storeButton:instantiate()
    self:addChild(self.storeButton)

    self.deleteButton = ISButton:new(318, 534, 446, 34, "DELETE STORED VEHICLE", self, Panel.onDelete)
    self.deleteButton:initialise()
    self.deleteButton:instantiate()
    self.deleteButton:setTooltip("Permanently remove the selected vehicle record after confirmation.")
    self:addChild(self.deleteButton)

    self.transferButton = ISButton:new(318, 574, 446, 34,
        "TRANSFER OWNERSHIP TO CURRENT DRIVER", self,
        Panel.onTransferToDriver)
    self.transferButton:initialise()
    self.transferButton:instantiate()
    self:addChild(self.transferButton)

    self.closeButton = ISButton:new(16, 660, 748, 30, "CLOSE", self, Panel.onClose)
    self.closeButton:initialise()
    self.closeButton:instantiate()
    self:addChild(self.closeButton)

    self:refreshList(true)
end

function Panel:prerender()
    self:refreshList(false)
    self:refreshSelection(false)
    ISPanel.prerender(self)
end

function Panel:render()
    ISPanel.render(self)
    self:drawTextCentre("PERSONAL GARAGE", self.width / 2, 10,
        0.96, 0.72, 0.18, 1, UIFont.Medium)
    self:drawTextCentre("Stored vehicles are personal. Only one vehicle may be active in the hideout.",
        self.width / 2, 34, 0.80, 0.80, 0.80, 1, UIFont.Small)
    local record = self:selectedRecord()
    local active = state().activeHideoutVehicle
    local transition = state().garageTransition
    if active ~= nil then
        local activeName = recordLabel(active)
        local ownerText = tostring(active.owner or "Unknown")
        local status = active.raidReserved == true and "Reserved for raid"
            or active.storing == true and "Returning to garage"
            or active.occupied == true and "Occupied"
            or active.inactive == true and "Inactive"
            or "Active"
        self:drawText("ACTIVE: " .. activeName .. " | Owner: " .. ownerText .. " | " .. status,
            16, 52, 0.72, 0.86, 1, 1, UIFont.Small)
    elseif transition ~= nil then
        self:drawText("GARAGE: Completing vehicle return...",
            16, 52, 0.96, 0.72, 0.18, 1, UIFont.Small)
    else
        self:drawText("ACTIVE: None", 16, 52, 0.58, 0.58, 0.58, 1, UIFont.Small)
    end
    self:drawText("SAVED VEHICLES", 16, 73, 0.96, 0.72, 0.18, 1, UIFont.Small)
    if record == nil then
        self:drawTextCentre("No vehicles saved", 541, 214, 0.85, 0.85, 0.85, 1, UIFont.Medium)
        return
    end
    self:drawText("VEHICLE NAME", 318, 386, 0.96, 0.72, 0.18, 1, UIFont.Small)
    self:drawText("Model: " .. modelLabel(record), 318, 448, 0.88, 0.88, 0.88, 1, UIFont.Small)
    local capacity = math.max(0, tonumber(record.fuelCapacity) or 0)
    local fuel = math.max(0, tonumber(record.fuel) or 0)
    local fuelText = capacity > 0 and string.format("Fuel: %.1f / %.1f L", fuel, capacity)
        or string.format("Fuel: %.1f L", fuel)
    self:drawText(fuelText, 318, 469, 0.88, 0.88, 0.88, 1, UIFont.Small)
    local batteryText = record.batteryPresent == true
        and string.format("Battery: %.1f%%", math.max(0, tonumber(record.batteryCharge) or 0))
        or "Battery: No Battery"
    self:drawText(batteryText, 530, 448, 0.88, 0.88, 0.88, 1, UIFont.Small)
    self:drawText("Engine condition: " .. tostring(math.floor(tonumber(record.engineCondition) or 0)) .. "%",
        530, 469, 0.88, 0.88, 0.88, 1, UIFont.Small)
end

function Panel:new()
    local width, height = 780, 706
    local x = math.floor((getCore():getScreenWidth() - width) / 2)
    local y = math.max(8, math.floor((getCore():getScreenHeight() - height) / 2))
    local object = ISPanel:new(x, y, width, height)
    setmetatable(object, self)
    self.__index = self
    object.background = true
    object.backgroundColor = { r = 0.025, g = 0.03, b = 0.035, a = 0.97 }
    object.borderColor = { r = 0.82, g = 0.32, b = 0.16, a = 0.9 }
    object.moveWithMouse = true
    return object
end

function ExtractionMode.openGaragePanel()
    if ExtractionMode.GaragePanelInstance then ExtractionMode.GaragePanelInstance:close() end
    local panel = Panel:new()
    panel:initialise()
    panel:addToUIManager()
    panel:setAlwaysOnTop(true)
    ExtractionMode.GaragePanelInstance = panel
    return panel
end

ExtractionMode.GaragePanel = Panel
return Panel
