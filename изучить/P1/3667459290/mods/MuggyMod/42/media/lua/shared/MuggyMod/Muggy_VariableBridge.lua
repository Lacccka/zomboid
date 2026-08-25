local Muggy_VariableBridge = {}

function Muggy_VariableBridge.setMuggyAttacking(npc, isAttacking)
    if not npc then
        return false
    end

    if not npc.setVariable then
        return false
    end

    local success, error = pcall(function()
        npc:setVariable("muggyisattacking", isAttacking)
    end)

    if not success then
        return false
    end

    return true
end

function Muggy_VariableBridge.getMuggyAttacking(npc)
    if not npc then
        return false
    end

    if not npc.getVariable then
        return false
    end

    local success, result = pcall(function()
        return npc:getVariable("muggyisattacking")
    end)

    if not success then
        return false
    end

    return result or false
end

function Muggy_VariableBridge.initializeMuggyVariables(npc)
    if not npc then
        return false
    end

    if not npc.setVariable then
        return false
    end

    local success, error = pcall(function()
        npc:setVariable("muggyisattacking", false)
    end)

    if not success then
        return false
    end

    return true
end

return Muggy_VariableBridge
