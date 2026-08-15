local FLASH_DURATION = 3000 -- ms
local FLASH_RANGE = 10 -- tiles

local FlashbangUI = ISUIElement:derive("FlashbangUI")

function FlashbangUI:new()
    local o = ISUIElement.new(self, 0, 0, getCore():getScreenWidth(), getCore():getScreenHeight())
    o.active = false
    o.startTime = 0
    return o
end

function FlashbangUI:render()
    if not self.active then return end
    local elapsed = getTimestampMs() - self.startTime
    if elapsed >= FLASH_DURATION then
        self.active = false
        self:setVisible(false)
        return
    end
    local alpha = 1.0 - (elapsed / FLASH_DURATION)
    self:drawRect(0, 0, self:getWidth(), self:getHeight(), alpha, 1, 1, 1)
end

function FlashbangUI:trigger(intensity)
    self.active = true
    self.startTime = getTimestampMs() - (FLASH_DURATION * (1.0 - intensity))
    self:setVisible(true)
end

local flashUI = nil
local function getFlashUI()
    if not flashUI then
        flashUI = FlashbangUI:new()
        flashUI:initialise()
        flashUI:addToUIManager()
        flashUI:setVisible(false)
    end
    return flashUI
end

Events.OnThrowableExplode.Add(function(item, square)
    local isoItem = item:getItem()
    if isoItem == nil or isoItem:getType() ~= "M84Flashbang" then return end

    local range = 3
    local x = square:getX()
    local y = square:getY()
    local z = square:getZ()

    local zombies = getCell():getZombieList()
    for i=0, zombies:size()-1 do
        local zomb = zombies:get(i)
        if zomb:getZ() == z then
            local dx = math.abs(zomb:getX() - x)
            local dy = math.abs(zomb:getY() - y)
            if dx <= range and dy <= range then
                zomb:knockDown(true)
                zomb:clearAggroList()
                zomb:Wander()
            end
        end
    end

    local player = getPlayer()
    if player and player:getZ() == z then
        local dx = math.abs(player:getX() - x)
        local dy = math.abs(player:getY() - y)
        if dx <= FLASH_RANGE and dy <= FLASH_RANGE then
            local dist = math.sqrt(dx*dx + dy*dy)
            local intensity = 1.0 - (dist / FLASH_RANGE)
            getFlashUI():trigger(intensity)
            if dist <= 3 then
                getSoundManager():PlaySound("flashbang_close", false, 0)
            elseif dist <= 6 then
                getSoundManager():PlaySound("flashbang_mid", false, 0)
            elseif dist <= FLASH_RANGE then
                getSoundManager():PlaySound("flashbang_far", false, 0)
            end
        end
    end
end)