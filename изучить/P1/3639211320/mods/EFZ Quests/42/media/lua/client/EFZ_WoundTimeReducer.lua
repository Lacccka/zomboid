if not EFZ then
    EFZ = {}
end

local function safeRequire(path)
    local ok, err = pcall(require, path)
    if not ok then
        print("[EFZ_WoundTimeReducer] require failed: " .. tostring(path) .. " -> " .. tostring(err))
    end
    return ok
end

safeRequire("TimedActions/ISTimedActionQueue")
safeRequire("TimedActions/EFZ_WoundTimeReducerAction")
safeRequire("ISUI/ISInventoryPane")
safeRequire("ISUI/ISContextMenu")
safeRequire("ISUI/ISInventoryPaneContextMenu")
safeRequire("XpSystem/ISUI/ISHealthPanel")

local ITEM_FULLTYPE = "EFZ.WoundTimeReducer"
local REDUCTION_FRACTION = 0.5
local MIN_REDUCTION = 30
local ACTION_TIME = 15 * 60

local function tr(key, fallback)
    if type(getText) == "function" and type(key) == "string" then
        local ok, res = pcall(getText, key)
        if ok and type(res) == "string" and res ~= key then
            return res
        end
    end
    return fallback or key
end

local TIME_FIELDS = {
    { get = "getScratchTime", set = "setScratchTime" },
    { get = "getCutTime", set = "setCutTime" },
    { get = "getBurnTime", set = "setBurnTime" },
    { get = "getDeepWoundTime", set = "setDeepWoundTime" },
    { get = "getStitchTime", set = "setStitchTime" },
    { get = "getFractureTime", set = "setFractureTime" },
    -- Some builds expose bite time as a timer; keep it optional.
    { get = "getBiteTime", set = "setBiteTime" },
}

local function safeCall(obj, fnName, ...)
    if not obj or not fnName then
        return false, nil
    end
    local fn = obj[fnName]
    if not fn then
        return false, nil
    end
    return pcall(fn, obj, ...)
end

local function safeGetNumber(obj, fnName)
    local ok, res = safeCall(obj, fnName)
    if not ok then
        return 0
    end
    local n = tonumber(res) or 0
    if n ~= n then
        return 0
    end
    return n
end

local function computeReducedValue(value)
    if value <= 0 then
        return value
    end
    local reduceBy = math.max(value * REDUCTION_FRACTION, MIN_REDUCTION)
    local newValue = value - reduceBy
    if newValue < 0 then
        newValue = 0
    end
    return newValue
end

local function applyReductionToBodyPart(bodyPart)
    if not bodyPart then
        return false
    end

    local changed = false
    for _, field in ipairs(TIME_FIELDS) do
        local cur = safeGetNumber(bodyPart, field.get)
        if cur > 0 then
            local newVal = computeReducedValue(cur)
            if newVal < cur then
                safeCall(bodyPart, field.set, newVal)
                changed = true
            end
        end
    end

    return changed
end

local function getBodyPartTypeString(bodyPart)
    local ok, res = safeCall(bodyPart, "getType")
    if not ok or res == nil then
        return nil
    end
    return tostring(res)
end

local function findBodyPartByTypeString(playerObj, typeStr)
    if not playerObj or not typeStr then
        return nil
    end

    local bd = playerObj.getBodyDamage and playerObj:getBodyDamage() or nil
    if not bd or not bd.getBodyParts then
        return nil
    end

    local list = bd:getBodyParts()
    if not list then
        return nil
    end

    for i = 0, list:size() - 1 do
        local bp = list:get(i)
        local bpTypeStr = getBodyPartTypeString(bp)
        if bpTypeStr == typeStr then
            return bp
        end
    end

    return nil
end

local function hasReducibleTime(bodyPart)
    if not bodyPart then
        return false
    end
    for _, field in ipairs(TIME_FIELDS) do
        if safeGetNumber(bodyPart, field.get) > 0 then
            return true
        end
    end

    -- Bleeding / wound infection (bacterial) should also count as "treatable" for menu visibility.
    if safeGetNumber(bodyPart, "getBleedingTime") > 0 then
        return true
    end
    do
        local ok, res = safeCall(bodyPart, "IsInfected")
        if ok and res == true then
            return true
        end
    end
    do
        local ok, res = safeCall(bodyPart, "isInfected")
        if ok and res == true then
            return true
        end
    end
    if safeGetNumber(bodyPart, "getWoundInfectionLevel") > 0 then
        return true
    end
    if safeGetNumber(bodyPart, "getInfectionLevel") > 0 then
        return true
    end

    return false
end

local function getBodyPartDisplayName(typeStr)
    if not typeStr then
        return "?"
    end
    return tr("IGUI_EFZ_BodyPart_" .. tostring(typeStr), tostring(typeStr))
end

local function getReducibleBodyParts(playerObj)
    local parts = {}

    local bd = playerObj and playerObj.getBodyDamage and playerObj:getBodyDamage() or nil
    if not bd or not bd.getBodyParts then
        return parts
    end

    local list = bd:getBodyParts()
    if not list then
        return parts
    end

    for i = 0, list:size() - 1 do
        local bp = list:get(i)
        if bp and hasReducibleTime(bp) then
            local typeStr = getBodyPartTypeString(bp)
            if typeStr then
                parts[#parts + 1] = {
                    bodyPart = bp,
                    typeStr = typeStr,
                    name = getBodyPartDisplayName(typeStr),
                }
            end
        end
    end

    return parts
end

local function findOneItemInInventory(playerObj)
    local inv = playerObj and playerObj.getInventory and playerObj:getInventory() or nil
    if not inv then
        return nil
    end

    local function getInnerContainer(item)
        if not item then
            return nil
        end

        if item.getInventory then
            local ok, res = pcall(function()
                return item:getInventory()
            end)
            if ok and res and res.getItems then
                return res
            end
        end

        if item.getItemContainer then
            local ok, res = pcall(function()
                return item:getItemContainer()
            end)
            if ok and res and res.getItems then
                return res
            end
        end

        return nil
    end

    local function findInContainer(container, visited)
        if not container or not container.getItems then
            return nil
        end

        visited = visited or {}
        if visited[container] then
            return nil
        end
        visited[container] = true

        local items = container:getItems()
        if not items then
            return nil
        end

        for i = 0, items:size() - 1 do
            local it = items:get(i)
            if it and it.getFullType and it:getFullType() == ITEM_FULLTYPE then
                return it
            end

            local inner = getInnerContainer(it)
            if inner then
                local found = findInContainer(inner, visited)
                if found then
                    return found
                end
            end
        end

        return nil
    end

    return findInContainer(inv, {})
end

local function getBodyPartMenuName(bodyPart)
    if bodyPart and BodyPartType and BodyPartType.getDisplayName and bodyPart.getType then
        local ok, t = pcall(function()
            return bodyPart:getType()
        end)
        if ok and t ~= nil then
            local ok2, name = pcall(BodyPartType.getDisplayName, t)
            if ok2 and name then
                return name
            end
        end
    end
    local typeStr = getBodyPartTypeString(bodyPart)
    return getBodyPartDisplayName(typeStr)
end

local function getTreatableBodyParts(playerObj)
    local parts = {}

    local bd = playerObj and playerObj.getBodyDamage and playerObj:getBodyDamage() or nil
    if not bd or not bd.getBodyParts then
        return parts
    end

    local list = bd:getBodyParts()
    if not list then
        return parts
    end

    for i = 0, list:size() - 1 do
        local bp = list:get(i)
        if bp and hasReducibleTime(bp) then
            parts[#parts + 1] = bp
        end
    end

    return parts
end

local function queueUseOnBodyPart(item, playerObj, bodyPart)
    if not item or not playerObj or not bodyPart then
        return
    end

    if not hasReducibleTime(bodyPart) then
        return
    end

    local job = tr("ContextMenu_Use_WoundTimeReducer", "Use")
    if ISTimedActionQueue and ISTimedActionQueue.add and EFZ_WoundTimeReducerAction and EFZ_WoundTimeReducerAction.new then
        local partTypeStr = getBodyPartTypeString(bodyPart)
        if partTypeStr then
            ISTimedActionQueue.add(EFZ_WoundTimeReducerAction:new(playerObj, item, partTypeStr))
        end
    end
end

local function findKitInActualItems(actualItems)
    if type(actualItems) ~= "table" then
        return nil
    end
    for _, it in ipairs(actualItems) do
        if it and it.getFullType and it:getFullType() == ITEM_FULLTYPE then
            return it
        end
    end
    return nil
end

local function onUseFromInventory(items, bodyPart, playerNum)
    local playerObj = getSpecificPlayer and getSpecificPlayer(playerNum) or nil
    if not playerObj or playerObj:isDead() or not bodyPart then
        return
    end

    local actualItems = nil
    if ISInventoryPane and ISInventoryPane.getActualItems then
        actualItems = ISInventoryPane.getActualItems(items)
    end
    if type(actualItems) ~= "table" then
        actualItems = {}
    end

    local kit = findKitInActualItems(actualItems) or findOneItemInInventory(playerObj)
    if not kit then
        return
    end

    if ISInventoryPaneContextMenu and ISInventoryPaneContextMenu.transferIfNeeded then
        pcall(ISInventoryPaneContextMenu.transferIfNeeded, playerObj, kit)
    end

    queueUseOnBodyPart(kit, playerObj, bodyPart)
end

local function addWoundTimeReducerToInventoryContext(context, playerNum, items)
    if not context or not items then
        return
    end

    local playerObj = getSpecificPlayer and getSpecificPlayer(playerNum) or nil
    if not playerObj or playerObj:isDead() then
        return
    end

    local actualItems = nil
    if ISInventoryPane and ISInventoryPane.getActualItems then
        actualItems = ISInventoryPane.getActualItems(items)
    end
    if type(actualItems) ~= "table" then
        actualItems = {}
    end

    if not findKitInActualItems(actualItems) then
        return
    end

    local label = tr("ContextMenu_Use_WoundTimeReducer", "Use")
    local treatable = getTreatableBodyParts(playerObj)
    if #treatable == 0 then
        local noInjury = tr("IGUI_EFZ_WoundTimeReducer_NoInjury", "No Injury")
        local opt = context:addOption(label .. " (" .. noInjury .. ")", nil, nil)
        opt.notAvailable = true
        return
    end

    local option = context:addOption(label, nil, nil)
    local subMenu = context.getNew and context:getNew(context) or nil
    if subMenu and context.addSubMenu then
        context:addSubMenu(option, subMenu)
    end

    for _, bp in ipairs(treatable) do
        local partName = getBodyPartMenuName(bp)
        if subMenu and subMenu.addOption then
            subMenu:addOption(partName, items, onUseFromInventory, bp, playerNum)
        else
            context:addOption(partName, items, onUseFromInventory, bp, playerNum)
        end
    end
end

local function findKitForHealthPanel(panel)
    local found = nil

    local collector = {
        checkItem = function(self, item)
            if found then
                return
            end
            if item and item.getFullType and item:getFullType() == ITEM_FULLTYPE then
                found = item
            end
        end,
    }

    if panel and panel.checkItems then
        pcall(function()
            panel:checkItems({ collector })
        end)
    end

    if found then
        return found
    end

    local playerObj = panel and panel.character or (getPlayer and getPlayer() or nil)
    return playerObj and findOneItemInInventory(playerObj) or nil
end

local function addWoundTimeReducerToHealthContext(panel, context, bodyPart)
    if not panel or not context or not bodyPart then
        return
    end

    if panel.blockingMessage then
        return
    end

    if not hasReducibleTime(bodyPart) then
        return
    end

    local item = findKitForHealthPanel(panel)
    if not item then
        return
    end

    local patient = panel.character or (getPlayer and getPlayer() or nil)
    if not patient or (patient.isDead and patient:isDead()) then
        return
    end

    local label = tr("ContextMenu_Use_WoundTimeReducer", "Use")
    context:addOption(label, item, function(it)
        if ISInventoryPaneContextMenu and ISInventoryPaneContextMenu.transferIfNeeded then
            pcall(ISInventoryPaneContextMenu.transferIfNeeded, patient, it)
        end
        queueUseOnBodyPart(it, patient, bodyPart)
    end)

    if context.setVisible then
        context:setVisible(true)
    end
end

local function patchInventoryPaneContextMenu()
    if not ISInventoryPaneContextMenu or type(ISInventoryPaneContextMenu.createMenu) ~= "function" then
        return false
    end
    if ISInventoryPaneContextMenu.__EFZ_WTR_PatchedCreateMenu == true then
        return true
    end

    ISInventoryPaneContextMenu.__EFZ_WTR_PatchedCreateMenu = true
    local originalCreateMenu = ISInventoryPaneContextMenu.createMenu
    ISInventoryPaneContextMenu.createMenu = function(player, isInPlayerInventory, items, x, y, origin)
        local context = originalCreateMenu(player, isInPlayerInventory, items, x, y, origin)
        pcall(addWoundTimeReducerToInventoryContext, context, player, items)
        return context
    end

    print("[EFZ_WoundTimeReducer] Patched ISInventoryPaneContextMenu.createMenu")
    return true
end

local function patchHealthPanel()
    if not ISHealthPanel or type(ISHealthPanel.doBodyPartContextMenu) ~= "function" then
        return false
    end
    if ISHealthPanel.__EFZ_WTR_PatchedBodyPartMenu == true then
        return true
    end
    if not ISContextMenu or type(ISContextMenu.get) ~= "function" then
        return false
    end

    ISHealthPanel.__EFZ_WTR_PatchedBodyPartMenu = true
    local originalFn = ISHealthPanel.doBodyPartContextMenu
    ISHealthPanel.doBodyPartContextMenu = function(self, bodyPart, x, y)
        local captured = nil
        local originalGet = ISContextMenu.get

        ISContextMenu.get = function(...)
            local ctx = originalGet(...)
            captured = ctx
            return ctx
        end

        local ok, err = pcall(function()
            originalFn(self, bodyPart, x, y)
        end)

        ISContextMenu.get = originalGet

        if not ok then
            error(err)
        end

        addWoundTimeReducerToHealthContext(self, captured, bodyPart)
    end

    print("[EFZ_WoundTimeReducer] Patched ISHealthPanel.doBodyPartContextMenu")
    return true
end

local function applyPatches()
    safeRequire("ISUI/ISContextMenu")
    safeRequire("ISUI/ISInventoryPaneContextMenu")
    safeRequire("XpSystem/ISUI/ISHealthPanel")

    patchInventoryPaneContextMenu()
    patchHealthPanel()
end

applyPatches()
if Events and Events.OnGameBoot then
    Events.OnGameBoot.Add(applyPatches)
end
if Events and Events.OnGameStart then
    Events.OnGameStart.Add(applyPatches)
end


