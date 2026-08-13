-- Lifestyle: Hobbies compatibility patch for Project Zomboid 42.20.
-- This file intentionally does not modify or copy the Workshop mod.

require "LSMoodleManager"

Lifestyle4220Compat = Lifestyle4220Compat or {}

local requiredMoodles = {
    "BladderNeed",
    "Embarrassed",
    "Gloomy",
    "TaughtSkill",
}

local function moodleNeedsRepair(moodle)
    return type(moodle) ~= "table"
        or moodle.Value == nil
        or moodle.Level == nil
        or moodle.Tiers == nil
        or moodle.Icon == nil
        or moodle.Alignment == nil
end

function Lifestyle4220Compat.ensurePlayerData(player)
    if not player or not player.getModData then return false end

    local data = player:getModData()
    local needsRepair = type(data.LSMoodles) ~= "table"

    if not needsRepair then
        for i = 1, #requiredMoodles do
            if moodleNeedsRepair(data.LSMoodles[requiredMoodles[i]]) then
                needsRepair = true
                break
            end
        end
    end

    if needsRepair and LSMoodleManager and LSMoodleManager.init then
        LSMoodleManager.init(player)
        data = player:getModData()
    end

    data.LSCooldowns = data.LSCooldowns or {}
    return type(data.LSMoodles) == "table"
end

local function ensureSpecificPlayer(playerIndex, player)
    Lifestyle4220Compat.ensurePlayerData(player or getSpecificPlayer(playerIndex or 0) or getPlayer())
end

local function ensureCurrentPlayer()
    Lifestyle4220Compat.ensurePlayerData(getPlayer())
end

-- A cheap per-frame guard is deliberate: Build 42 multiplayer can replace player
-- ModData between the slower minute/hour events used by Lifestyle.
local lastPlayer
local lastMoodles
local function guardPlayerData()
    local player = getPlayer()
    if not player then return end

    local data = player:getModData()
    if player ~= lastPlayer or data.LSMoodles ~= lastMoodles
        or type(data.LSMoodles) ~= "table" then
        Lifestyle4220Compat.ensurePlayerData(player)
        lastPlayer = player
        lastMoodles = player:getModData().LSMoodles
        return
    end

    -- Validate the entries that caused observed 42.20 exceptions. This does not
    -- rewrite valid saved values; LSMoodleManager.init only repairs bad entries.
    for i = 1, #requiredMoodles do
        if moodleNeedsRepair(data.LSMoodles[requiredMoodles[i]]) then
            Lifestyle4220Compat.ensurePlayerData(player)
            lastMoodles = player:getModData().LSMoodles
            return
        end
    end
end

Events.OnCreatePlayer.Add(ensureSpecificPlayer)
Events.OnGameStart.Add(ensureCurrentPlayer)
Events.OnConnected.Add(ensureCurrentPlayer)
Events.OnTick.Add(guardPlayerData)

-- Lifestyle 0.4.0 uses a fixed 31 px tooltip background and hard-coded text
-- baselines. Russian text and larger UI fonts exceed that height in Build 42.20.
-- Wrap the factory so every newly-created Lifestyle moodle receives a tooltip
-- renderer based on the actual font height.
if LSMoodleManager and LSMoodleManager.newType
    and not Lifestyle4220Compat.originalNewType then

    Lifestyle4220Compat.originalNewType = LSMoodleManager.newType

    LSMoodleManager.newType = function(player, moodleName)
        Lifestyle4220Compat.ensurePlayerData(player)
        local moodleUI = Lifestyle4220Compat.originalNewType(player, moodleName)
        if not moodleUI then return moodleUI end

        moodleUI.mouseOverMoodle = function(self, currentName, title, description)
            local data = player and player:getModData()
            if not data or type(data.LSMoodles) ~= "table"
                or not data.LSMoodles[currentName] then
                return
            end

            local hovered = self:isMouseOver()
            if not hovered then
                local mouseX, mouseY = getMouseX(), getMouseY()
                hovered = mouseX >= self:getX()
                    and mouseY >= self:getY()
                    and mouseX <= self:getX() + self:getWidth()
                    and mouseY <= self:getY() + self:getHeight()
            end

            if hovered then
                -- Vanilla moodles live in a separate Java UI. Moving directly
                -- onto a Lifestyle icon does not notify that UI that the mouse
                -- left its previous slot, so its last tooltip can remain visible.
                local playerNum = player and player:getPlayerNum() or 0
                local vanillaMoodles = UIManager and UIManager.getMoodleUI
                    and UIManager.getMoodleUI(playerNum)
                if vanillaMoodles and vanillaMoodles.onMouseMoveOutside then
                    vanillaMoodles:onMouseMoveOutside(0, 0)
                end

                title = title or ""
                description = description or ""

                -- Match zombie.ui.MoodlesUI.render() from Build 42.20:
                -- max text width + 12 px, two line-heights + 4 px,
                -- black at 0.6 alpha, white title and 0.8-gray description.
                local textManager = getTextManager()
                local font = UIFont.Small
                local lineHeight = textManager:getFontHeight(font)
                local textWidth = math.max(
                    textManager:MeasureStringX(font, title),
                    textManager:MeasureStringX(font, description)
                )
                local boxWidth = textWidth + 12
                local boxHeight = (lineHeight + 2) * 2
                local textY = 1
                if self:getHeight() > boxHeight then
                    textY = textY + math.floor((self:getHeight() - boxHeight) / 2)
                end

                self:drawRect(-10 - textWidth - 6, textY - 2, boxWidth, boxHeight, 0.6, 0, 0, 0)
                self:drawTextRight(title, -10, textY, 1, 1, 1, 1)
                self:drawTextRight(description, -10, textY + lineHeight, 0.8, 0.8, 0.8, 1)

                if currentName == "BeautyGood" or currentName == "BeautyNeg" then
                    if not LSMoodleManager.BUIInstance and LSBeautyScore then
                        LSMoodleManager.BUIInstance = LSBeautyScore:new(self, player, true)
                        LSMoodleManager.BUIInstance:initialise()
                        LSMoodleManager.BUIInstance:addToUIManager()
                        LSMoodleManager.BUIHovering = true
                    end
                end
            elseif LSMoodleManager.BUIInstance and LSMoodleManager.BUIHovering then
                LSMoodleManager.BUIInstance:close()
                LSMoodleManager.BUIInstance = false
                LSMoodleManager.BUIHovering = false
            end
        end

        return moodleUI
    end
end
