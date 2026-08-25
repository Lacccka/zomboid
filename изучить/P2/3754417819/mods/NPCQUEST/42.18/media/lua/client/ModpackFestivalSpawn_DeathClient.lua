-- Festival run: death ends the save — only quit to main menu (no new character / quit to desktop).

if isServer() then return end

local MOD_ID = "ModpackFestivalSpawn"

local ALLOW_TITLE_FRAGMENTS = {
    "QUIT TO MENU",
    "MAIN MENU",
    "EXIT TO MENU",
    "RETURN TO MENU",
}

local BLOCK_TITLE_FRAGMENTS = {
    "CONTINUE",
    "NEW CHARACTER",
    "NEW PLAYER",
    "QUIT TO DESKTOP",
    "DESKTOP",
    "RESPAWN",
}

local HIDE_BUTTON_FIELDS = {
    "buttonNewPlayer",
    "buttonRespawn",
    "continueButton",
    "quitDesktopButton",
    "buttonQuitDesktop",
    "buttonExitDesktop",
    "buttonQuitToDesktop",
    "desktopButton",
    "newPlayerButton",
}

local function upperTitle(title)
    if not title then return "" end
    return string.upper(tostring(title))
end

local function isAllowedMenuButton(title)
    local t = upperTitle(title)
    if t == "" then return false end
    for i = 1, #ALLOW_TITLE_FRAGMENTS do
        if string.find(t, ALLOW_TITLE_FRAGMENTS[i], 1, true) then
            return true
        end
    end
    return false
end

local function isBlockedDeathButton(title)
    local t = upperTitle(title)
    if t == "" then return false end
    if isAllowedMenuButton(t) then
        return false
    end
    for i = 1, #BLOCK_TITLE_FRAGMENTS do
        if string.find(t, BLOCK_TITLE_FRAGMENTS[i], 1, true) then
            return true
        end
    end
    return false
end

local function disableUiElement(el)
    if not el then return end
    pcall(function() el:setVisible(false) end)
    pcall(function() el:setEnable(false) end)
    pcall(function() el:setEnabled(false) end)
    pcall(function()
        if el.setOnClick then
            el:setOnClick(nil)
        end
    end)
    pcall(function()
        if el.onclick then
            el.onclick = nil
        end
    end)
end

local function getButtonTitle(btn)
    if not btn then return nil end
    if btn.getTitle then
        local ok, title = pcall(function() return btn:getTitle() end)
        if ok and title then return title end
    end
    if btn.title then return btn.title end
    return nil
end

local function shouldHideDeathButton(btn)
    local title = getButtonTitle(btn)
    if isBlockedDeathButton(title) then
        return true
    end
    if btn.internal then
        local internal = upperTitle(btn.internal)
        if string.find(internal, "DESKTOP", 1, true)
            or string.find(internal, "NEW", 1, true)
            or string.find(internal, "CONTINUE", 1, true)
            or string.find(internal, "RESPAWN", 1, true) then
            return true
        end
    end
    return false
end

local function hideKnownFields(panel)
    if not panel then return end
    for i = 1, #HIDE_BUTTON_FIELDS do
        disableUiElement(panel[HIDE_BUTTON_FIELDS[i]])
    end
end

local function forEachChild(panel, fn)
    if not panel or not fn then return end
    if panel.getChildren then
        local ok, kids = pcall(function() return panel:getChildren() end)
        if ok and kids and kids.size then
            for i = 0, kids:size() - 1 do
                fn(kids:get(i))
            end
            return
        end
    end
    if panel.children then
        for _, child in ipairs(panel.children) do
            fn(child)
        end
    end
end

local function hideDeathButtonsRecursive(panel)
    if not panel then return end
    hideKnownFields(panel)

    forEachChild(panel, function(child)
        if not child then return end
        local isButton = false
        if ISButton and child.Type == "ISButton" then
            isButton = true
        elseif instanceof then
            local ok, result = pcall(function() return instanceof(child, "ISButton") end)
            isButton = ok and result
        end
        if isButton and shouldHideDeathButton(child) then
            disableUiElement(child)
        end
        hideDeathButtonsRecursive(child)
    end)
end

local function isPostDeathPanel(el)
    if not el then return false end
    if ISPostDeathUI and el.Type == "ISPostDeathUI" then
        return true
    end
    if instanceof then
        local ok, result = pcall(function() return instanceof(el, "ISPostDeathUI") end)
        if ok and result then return true end
    end
    local typeName = nil
    if el.getType then
        local okType, gotType = pcall(function() return el:getType() end)
        if okType then
            typeName = gotType
        end
    end
    if typeName and string.find(string.upper(tostring(typeName)), "POSTDEATH", 1, true) then
        return true
    end
    return false
end

local function patchPostDeathUiClass()
    if not ISPostDeathUI or ISPostDeathUI._ModpackFestivalDeathPatched then
        return ISPostDeathUI ~= nil and ISPostDeathUI._ModpackFestivalDeathPatched == true
    end

    local origCreate = ISPostDeathUI.createChildren
    ISPostDeathUI.createChildren = function(self)
        if origCreate then
            origCreate(self)
        end
        hideDeathButtonsRecursive(self)
    end

    local blockMethods = {
        "onNewPlayer",
        "onContinue",
        "onQuitDesktop",
        "onExitDesktop",
        "onRespawn",
    }
    for i = 1, #blockMethods do
        local name = blockMethods[i]
        if ISPostDeathUI[name] then
            ISPostDeathUI[name] = function() end
        end
    end

    ISPostDeathUI._ModpackFestivalDeathPatched = true
    return true
end

local function tryRequirePostDeathUi()
    pcall(function() require "ISUI/ISPostDeathUI" end)
end

local function scanDeathUi()
    if not UIManager or not UIManager.getUI then return end
    local ui = UIManager.getUI()
    if not ui or not ui.size then return end
    for i = 0, ui:size() - 1 do
        local el = ui:get(i)
        if isPostDeathPanel(el) then
            hideDeathButtonsRecursive(el)
        end
    end
end

local tick = 0

local function onTick()
    tick = tick + 1
    if ModpackFestivalTick.every(tick, ModpackFestivalTick.MAINT) then
        tryRequirePostDeathUi()
        patchPostDeathUiClass()
    end

    local player = getSpecificPlayer(0)
    if not player or not player:isDead() then return end

    scanDeathUi()
end

tryRequirePostDeathUi()
patchPostDeathUiClass()

Events.OnGameBoot.Add(function()
    tryRequirePostDeathUi()
    patchPostDeathUiClass()
end)
Events.OnTick.Add(onTick)

print("[" .. MOD_ID .. "] permadeath death screen client loaded")
