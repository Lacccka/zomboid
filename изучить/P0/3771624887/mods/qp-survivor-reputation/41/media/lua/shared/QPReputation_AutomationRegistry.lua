require "QPReputation_Config"

QPReputation.AutomationRegistry =
    QPReputation.AutomationRegistry or {}

local R = QPReputation.AutomationRegistry

R.SchemaVersion = 2
R.Order = {
    "hunter",
    "community",
    "explorer",
    "medic",
    "mechanic",
    "builder",
}

R.Definitions = {
    hunter = {
        id = "hunter",
        path = "hunter",
        implemented = true,
        enabledByDefault = true,
        stateSchemaVersion = 1,
    },
    community = {
        id = "community",
        path = "community",
        implemented = true,
        enabledByDefault = false,
        stateSchemaVersion = 1,
        eventDriven = true,
    },
    explorer = {
        id = "explorer",
        path = "explorer",
        implemented = false,
        enabledByDefault = false,
        stateSchemaVersion = 1,
    },
    medic = {
        id = "medic",
        path = "medic",
        implemented = false,
        enabledByDefault = false,
        stateSchemaVersion = 1,
    },
    mechanic = {
        id = "mechanic",
        path = "mechanic",
        implemented = false,
        enabledByDefault = false,
        stateSchemaVersion = 1,
    },
    builder = {
        id = "builder",
        path = "builder",
        implemented = false,
        enabledByDefault = false,
        stateSchemaVersion = 1,
    },
}

local function automationConfig()
    return QPReputation.Config.Automation or {}
end

local function configuredPath(pathId)
    local paths = automationConfig().Paths or {}
    return paths[pathId] or {}
end

function R.normalizeId(pathId)
    return string.lower(tostring(pathId or ""))
end

function R.get(pathId)
    return R.Definitions[R.normalizeId(pathId)]
end

function R.list()
    local rows = {}

    for _, pathId in ipairs(R.Order) do
        local definition = R.Definitions[pathId]

        if definition then
            table.insert(rows, definition)
        end
    end

    return rows
end

function R.isImplemented(pathId)
    local definition = R.get(pathId)
    return definition ~= nil
        and definition.implemented == true
end

function R.defaultEnabled(pathId)
    pathId = R.normalizeId(pathId)

    local definition = R.get(pathId)

    if not definition or definition.implemented ~= true then
        return false
    end

    local configured = configuredPath(pathId)

    if configured.Enabled ~= nil then
        return configured.Enabled == true
    end

    if pathId == "hunter" then
        local hunter = automationConfig().Hunter or {}

        if hunter.Enabled ~= nil then
            return hunter.Enabled == true
        end
    end

    return definition.enabledByDefault == true
end

function R.defaultPathSettings()
    local settings = {}

    for _, definition in ipairs(R.list()) do
        settings[definition.id] = {
            schemaVersion = 1,
            implemented = definition.implemented == true,
            enabled = R.defaultEnabled(definition.id),
        }
    end

    return settings
end

function R.status(settings)
    settings = settings or {}

    local result = {
        registryVersion = R.SchemaVersion,
        registeredCount = 0,
        implementedCount = 0,
        activeCount = 0,
    }

    local pathSettings = settings.paths or {}
    local masterEnabled = settings.enabled == true

    for _, definition in ipairs(R.list()) do
        result.registeredCount = result.registeredCount + 1

        if definition.implemented == true then
            result.implementedCount =
                result.implementedCount + 1

            local row = pathSettings[definition.id] or {}

            if masterEnabled and row.enabled == true then
                result.activeCount = result.activeCount + 1
            end
        end
    end

    return result
end
