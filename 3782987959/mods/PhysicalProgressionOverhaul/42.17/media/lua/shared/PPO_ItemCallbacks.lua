PPO = PPO or {}

PPO_ItemCallbacks = PPO_ItemCallbacks or {}
PPO.ItemCallbacks = PPO_ItemCallbacks

local Callbacks = PPO_ItemCallbacks

function Callbacks.takeThermogenic(item, character)
    if item == nil or character == nil or CharacterStat == nil
            or CharacterStat.FATIGUE == nil then
        return false
    end
    if item.getFatigueChange == nil or character.getStats == nil then
        return false
    end
    local applied, result = pcall(function()
        local stats = character:getStats()
        if stats == nil or stats.get == nil or stats.set == nil then
            return false
        end
        local current = stats:get(CharacterStat.FATIGUE)
        local change = item:getFatigueChange()
        if type(current) ~= "number" or type(change) ~= "number" then
            return false
        end
        stats:set(CharacterStat.FATIGUE, current + change)
        return true
    end)
    return applied and result == true
end

return PPO_ItemCallbacks
