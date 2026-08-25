require "ISUI/ISCollapsableWindow"
require "ISUI/ISLabel"
require "ISUI/ISButton"
require "ISUI/ISToolTip"

MissionsEvents_UI = ISCollapsableWindow:derive("MissionsEvents_UI")
MissionsEvents_UI.instance = nil

MissionsEvents_UI.state = MissionsEvents_UI.state or {
    lastStart = 0,
    cooldown = 0
}

-- =========================
-- CREATE UI
-- =========================
function MissionsEvents_UI:createChildren()
    ISCollapsableWindow.createChildren(self)

    self.resizable = false

    local x = 10
    local y = 30

    -- =========================
    -- M1
    -- =========================
    self.m1Label = ISLabel:new(
        x, y, 20,
        getText("IGUI_M1_Title") .. ": " .. getText("IGUI_M1_Objective"),
        1,1,1,1, UIFont.Small, true
    )
    self.m1Label:initialise()
    self:addChild(self.m1Label)

    y = y + 20

    self.m1Status = ISLabel:new(x, y, 20, "", 1,1,1,1, UIFont.Small, true)
    self.m1Status:initialise()
    self:addChild(self.m1Status)

    y = y + 20

    self.m1Button = ISButton:new(
        x, y, 120, 25,
        getText("IGUI_M1_Activate"),
        self,
        MissionsEvents_UI.onMission1
    )
    self.m1Button:initialise()
    self:addChild(self.m1Button)

    -- =========================
    -- SEPARADOR
    -- =========================
    y = y + 40

    -- =========================
    -- M2
    -- =========================
    self.m2Label = ISLabel:new(
        x, y, 20,
        getText("IGUI_M2_Title") .. ": " .. getText("IGUI_M2_Objective"),
        1,1,1,1, UIFont.Small, true
    )
    self.m2Label:initialise()
    self:addChild(self.m2Label)

    y = y + 20

    self.m2Status = ISLabel:new(x, y, 20, "", 1,1,1,1, UIFont.Small, true)
    self.m2Status:initialise()
    self:addChild(self.m2Status)

    -- Tooltip
    self.m2Status:setTooltip("...")
    self.m2Status.tooltipUI = nil

    y = y + 20

    self.m2Button = ISButton:new(
        x, y, 120, 25,
        "Activate",
        self,
        MissionsEvents_UI.onMission2
    )
    self.m2Button:initialise()
    self:addChild(self.m2Button)
end

-- =========================
-- M1 BUTTON
-- =========================
function MissionsEvents_UI:onMission1()

    local player = getPlayer()
    if not player then return end

    if MissionsEvents and MissionsEvents.M2 then
        local data = MissionsEvents.M2.getUIData(player)
        if data and data.active then
            return
        end
    end

    local state = MissionsEvents_UI.state or {}
    MissionsEvents_UI.state = state

    local now = getGameTime():getWorldAgeHours() * 60
    local cfg = SandboxVars.MissionsEvents
    local cooldown = (cfg and cfg.M1_CooldownHours or 2) * 60

    state.lastStart = state.lastStart or 0

    if (now - state.lastStart) < cooldown then
        return
    end

    if MissionsEvents and MissionsEvents.startM1 then
        MissionsEvents.startM1()
    end

    state.lastStart = now
    state.cooldown = cooldown
end

-- =========================
-- M2 BUTTON
-- =========================
function MissionsEvents_UI:onMission2()

    local player = getPlayer()
    if not player then return end

    local data = MissionsEvents.M2.getUIData(player)
    if not data then return end

    -- bloquear si M1 activo
    if MissionsEvents and MissionsEvents.M1 then
        local m1 = MissionsEvents.M1.getUIData(player)
        if m1 and m1.active then return end
    end

    if not data.active then
        sendClientCommand("MissionsEvents", "StartM2", {})
    else
        sendClientCommand("MissionsEvents", "ConfirmM2", {})
    end
end

-- =========================
-- UPDATE
-- =========================
function MissionsEvents_UI:update()

    local player = getPlayer()
    if not player then return end

    local now = getGameTime():getWorldAgeHours() * 60
    local cfg = SandboxVars.MissionsEvents

    -- =========================
    -- M1 UPDATE
    -- =========================
    local state = MissionsEvents_UI.state
    state.cooldown = state.cooldown or ((cfg and cfg.M1_CooldownHours or 2) * 60)

    local remaining = state.cooldown - (now - state.lastStart)
    local onCooldown = remaining > 0

    if not (cfg and cfg.M1_Enable ~= false) then
        self.m1Status:setName(getText("IGUI_M1_Disabled"))
        self.m1Button:setEnable(false)

    elseif onCooldown then
        local percent = math.min(1, (now - state.lastStart) / state.cooldown)
        self.m1Status:setName(getText("IGUI_M1_Cooldown") .. " (" .. math.floor(percent*100) .. "%)")
        self.m1Status:setColor(1,0,0,1)
        self.m1Button:setEnable(false)

    else
        self.m1Status:setName(getText("IGUI_M1_Available"))
        self.m1Status:setColor(0,1,0,1)
        self.m1Button:setEnable(true)
    end

	-- =========================
	-- M2 UPDATE
	-- =========================
	local data = MissionsEvents.M2.getUIData(player)
	
	if not data or not data.enabled then
		self.m2Status:setName(getText("IGUI_M2_Disabled"))
		self.m2Status:setColor(0.5,0.5,0.5,1)
		self.m2Button:setEnable(false)
		return
	end
	
	-- =========================
	-- COOLDOWN
	-- =========================
	local cooldown = (cfg and cfg.M2_CooldownHours or 2) * 60
	local lastStart = data.lastStart or 0
	
	local remaining = cooldown - (now - lastStart)
	local onCooldown = (not data.active) and remaining > 0
	
	if onCooldown then
	
		local percent = math.min(1, (now - lastStart) / cooldown)
	
		self.m2Status:setName(
			getText("IGUI_M2_Cooldown") .. " (" .. math.floor(percent * 100) .. "%)"
		)
		self.m2Status:setColor(1,0,0,1)
	
		self.m2Button:setEnable(false)
		return
	end
	
	-- =========================
	-- AVAILABLE
	-- =========================
	if not data.active then
		self.m2Status:setName(getText("IGUI_M2_Available"))
		self.m2Status:setColor(0,1,0,1)
		self.m2Button:setTitle(getText("IGUI_M2_Activate"))
		self.m2Button:setEnable(true)
		return
	end
	
	-- =========================
	-- STARTING
	-- =========================
	if not data.type then
		self.m2Status:setName(getText("IGUI_M2_Starting"))
		self.m2Status:setColor(1,1,0,1)
		self.m2Button:setEnable(false)
		return
	end
	
	-- =========================
	-- LOADING ITEMS
	-- =========================
	if not data.requiredItems then
		self.m2Status:setName(getText("IGUI_M2_Loading"))
		self.m2Status:setColor(1,1,0,1)
		self.m2Button:setEnable(false)
		return
	end
	
	-- =========================
	-- PROGRESS
	-- =========================
	local have, total, done = MissionsEvents.M2.getProgress(player, data.requiredItems)
	
	local typeName = (data.type == "carpentry" and getText("IGUI_M2_Carpentry"))
				  or (data.type == "electrician" and getText("IGUI_M2_Electrician"))
				  or getText("IGUI_M2_Unknown")
	
	self.m2Status:setName(typeName .. " " .. have .. "/" .. total)
	
	if done then
		self.m2Status:setColor(0,1,0,1)
		self.m2Button:setTitle(getText("IGUI_M2_Confirm"))
	else
		self.m2Status:setColor(1,0,0,1)
		self.m2Button:setTitle(getText("IGUI_M2_Activate"))
	end
	
	-- =========================
	-- TOOLTIP
	-- =========================
	local text = ""
	
	for item, amount in pairs(data.requiredItems) do
	
		local itemName = getItemName(item) or item
	
		text = text .. itemName .. " x" .. amount .. "\n"
	end
	
	self.m2Status:setTooltip(text)
	
end

-- =========================
-- OPEN
-- =========================
function MissionsEvents_UI.open(player)

    if MissionsEvents_UI.instance then
        MissionsEvents_UI.instance:close()
    end

    local ui = MissionsEvents_UI:new(200, 200, 280, 200)

    ui:initialise()
    ui:instantiate()
    ui:addToUIManager()

    ui.state = MissionsEvents_UI.state

    MissionsEvents_UI.instance = ui
end

-- =========================
-- CLOSE
-- =========================
function MissionsEvents_UI:close()
    ISCollapsableWindow.close(self)
    MissionsEvents_UI.instance = nil
end

-- =========================
-- CONSTRUCTOR
-- =========================
function MissionsEvents_UI:new(x, y, w, h)

    local o = ISCollapsableWindow:new(x, y, w, h)

    setmetatable(o, self)
    self.__index = self

    o.title = getText("IGUI_MissionsEvents_Title")
    o.resizable = false
    o.pin = true

    return o
end