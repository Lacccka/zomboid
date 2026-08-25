pcall(require, "PZAPI/ModOptions")

LCC_GridPaneUXOptions = LCC_GridPaneUXOptions or {}
local GridPaneUXOptions = LCC_GridPaneUXOptions

local SECTION_ID = "LCCGridInventorySort"
local DEFAULT_SCROLL_SPEED = 3
local MIN_SCROLL_SPEED = 1
local MAX_SCROLL_SPEED = 8
local NONE_KEY = Keyboard and Keyboard.KEY_NONE or 0

GridPaneUXOptions.cache = GridPaneUXOptions.cache or {
    scrollSpeed = DEFAULT_SCROLL_SPEED,
    previousContainerKey = NONE_KEY,
    nextContainerKey = NONE_KEY,
}
local cache = GridPaneUXOptions.cache

local function clampNumber(value, default, minimum, maximum)
    local number = tonumber(value) or default
    number = math.floor(number + 0.5)
    if number < minimum then number = minimum end
    if number > maximum then number = maximum end
    return number
end

local function split(line, separator)
    local parts = {}
    for part in (line .. separator):gmatch("(.-)" .. separator) do
        table.insert(parts, part)
    end
    return parts
end

local function applyIni()
    local ok, reader = pcall(function() return getFileReader("ModOptions.ini", true) end)
    if not ok or not reader then return end

    local okRead, err = pcall(function()
        while true do
            local line = reader:readLine()
            if line == nil then break end
            local parts = split(line, "|")
            if parts[2] == SECTION_ID and parts[3] and parts[4] then
                local value = parts[4]:gsub("\r$", ""):gsub("%s+$", "")
                if parts[3] == "scrollSpeed" then
                    cache.scrollSpeed = clampNumber(value, DEFAULT_SCROLL_SPEED, MIN_SCROLL_SPEED, MAX_SCROLL_SPEED)
                elseif parts[3] == "previousContainerKey" then
                    cache.previousContainerKey = math.max(0, tonumber(value) or NONE_KEY)
                elseif parts[3] == "nextContainerKey" then
                    cache.nextContainerKey = math.max(0, tonumber(value) or NONE_KEY)
                end
            end
        end
    end)
    reader:close()
    if not okRead then
        print("[LCC GridSort] failed to read pane options: " .. tostring(err))
    end
end

local function syncFromSection(section)
    if not section then return end

    local speed = section:getOption("scrollSpeed")
    local previous = section:getOption("previousContainerKey")
    local next = section:getOption("nextContainerKey")
    local function currentKey(option)
        -- Mod keybind controls are not GameOption entries. MainOptions updates
        -- keyTextElement.keyCode first and copies it to option.key only later,
        -- during PZAPI.ModOptions:save(), after section.apply has run.
        if option and option.element and option.element.keyCode ~= nil then
            return option.element.keyCode
        end
        return option and option:getValue() or NONE_KEY
    end
    if speed then
        cache.scrollSpeed = clampNumber(speed:getValue(), DEFAULT_SCROLL_SPEED, MIN_SCROLL_SPEED, MAX_SCROLL_SPEED)
    end
    if previous then cache.previousContainerKey = math.max(0, tonumber(currentKey(previous)) or NONE_KEY) end
    if next then cache.nextContainerKey = math.max(0, tonumber(currentKey(next)) or NONE_KEY) end
end

local function registerOptions()
    local modOptions = PZAPI and PZAPI.ModOptions
    if not (modOptions and modOptions.create and modOptions.getOptions) then return false end

    local existing = modOptions:getOptions(SECTION_ID)
    if existing then
        GridPaneUXOptions.section = existing
        return true
    end

    local section = modOptions:create(SECTION_ID, getText("UI_LCC_GridPaneUX_OptionsSection"))
    section:addTitle(getText("UI_LCC_GridPaneUX_NavigationTitle"))
    section:addDescription("UI_LCC_GridPaneUX_NavigationDescription")
    section:addSlider(
        "scrollSpeed",
        getText("UI_LCC_GridPaneUX_ScrollSpeed"),
        MIN_SCROLL_SPEED,
        MAX_SCROLL_SPEED,
        1,
        cache.scrollSpeed,
        getText("UI_LCC_GridPaneUX_ScrollSpeed_Tooltip")
    )
    section:addKeyBind(
        "previousContainerKey",
        getText("UI_LCC_GridPaneUX_PreviousContainer"),
        cache.previousContainerKey,
        getText("UI_LCC_GridPaneUX_PreviousContainer_Tooltip")
    )
    section:addKeyBind(
        "nextContainerKey",
        getText("UI_LCC_GridPaneUX_NextContainer"),
        cache.nextContainerKey,
        getText("UI_LCC_GridPaneUX_NextContainer_Tooltip")
    )
    section.apply = function()
        syncFromSection(section)
    end

    GridPaneUXOptions.section = section
    return true
end

local function tryRegister()
    applyIni()
    if not GridPaneUXOptions.registered then
        GridPaneUXOptions.registered = registerOptions()
    end
end

function GridPaneUXOptions.getScrollSpeed()
    return clampNumber(cache.scrollSpeed, DEFAULT_SCROLL_SPEED, MIN_SCROLL_SPEED, MAX_SCROLL_SPEED)
end

function GridPaneUXOptions.getPreviousContainerKey()
    return math.max(0, tonumber(cache.previousContainerKey) or NONE_KEY)
end

function GridPaneUXOptions.getNextContainerKey()
    return math.max(0, tonumber(cache.nextContainerKey) or NONE_KEY)
end

if not GridPaneUXOptions.eventsRegistered then
    GridPaneUXOptions.eventsRegistered = true
    Events.OnGameBoot.Add(tryRegister)
    Events.OnMainMenuEnter.Add(tryRegister)
    Events.OnGameStart.Add(tryRegister)
end

tryRegister()
return GridPaneUXOptions
