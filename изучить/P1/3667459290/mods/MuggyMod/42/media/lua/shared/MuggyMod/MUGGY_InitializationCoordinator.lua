local MUGGY_InitializationCoordinator = {}

local MUGGY_EnvironmentDetector = require("MuggyMod/MUGGY_EnvironmentDetector")

local initializationState = {
    started = false,
    completed = false,
    sharedSystemsLoaded = false,
    clientSystemsLoaded = false,
    serverSystemsLoaded = false
}

local initializationResults = {
    shared = {},
    client = {},
    server = {}
}

local function initializeSystem(systemName, requirePath, category)
    print("[MUGGY_Init] [" .. string.upper(category) .. "] Loading " .. systemName .. "...")

    local success, systemModule = pcall(function()
        return require(requirePath)
    end)

    if not success then
        print("[MUGGY_Init] [" .. string.upper(category) .. "] ERROR: Failed to load " .. systemName)
        print("[MUGGY_Init] [" .. string.upper(category) .. "] Error: " .. tostring(systemModule))
        initializationResults[category][systemName] = {
            loaded = false,
            initialized = false,
            error = tostring(systemModule)
        }
        return false
    end

    print("[MUGGY_Init] [" .. string.upper(category) .. "] " .. systemName .. " module loaded")

    if not systemModule then
        print("[MUGGY_Init] [" .. string.upper(category) .. "] ERROR: " .. systemName .. " module is nil")
        initializationResults[category][systemName] = {
            loaded = false,
            initialized = false,
            error = "Module is nil"
        }
        return false
    end

    if not systemModule.initialize then
        print("[MUGGY_Init] [" .. string.upper(category) .. "] WARNING: " .. systemName .. " has no initialize() function")
        initializationResults[category][systemName] = {
            loaded = true,
            initialized = false,
            error = "No initialize() function"
        }
        return true
    end

    print("[MUGGY_Init] [" .. string.upper(category) .. "] Calling " .. systemName .. ".initialize()...")

    local initSuccess, initError = pcall(function()
        systemModule.initialize()
    end)

    if not initSuccess then
        print("[MUGGY_Init] [" .. string.upper(category) .. "] ERROR: " .. systemName .. " initialization failed")
        print("[MUGGY_Init] [" .. string.upper(category) .. "] Error: " .. tostring(initError))
        initializationResults[category][systemName] = {
            loaded = true,
            initialized = false,
            error = tostring(initError)
        }
        return false
    end

    print("[MUGGY_Init] [" .. string.upper(category) .. "] " .. systemName .. " initialized successfully")
    initializationResults[category][systemName] = {
        loaded = true,
        initialized = true,
        error = nil
    }
    return true
end

local function initializeSharedSystems()
    print("[MUGGY_Init] ========== INITIALIZING SHARED SYSTEMS ==========")

    local systems = {
        {name = "ZoneSpawningCore", path = "MuggyMod/MUGGY_ZoneSpawningCore"},
        {name = "GuardZoneScanner", path = "DialogueFramework/Behavior/NPC_GuardZoneScanner"}
    }

    local successCount = 0
    local failureCount = 0

    for _, system in ipairs(systems) do
        if initializeSystem(system.name, system.path, "shared") then
            successCount = successCount + 1
        else
            failureCount = failureCount + 1
        end
    end

    print("[MUGGY_Init] Shared systems complete: " .. successCount .. " succeeded, " .. failureCount .. " failed")
    initializationState.sharedSystemsLoaded = true
    return successCount > 0
end

local function initializeClientSystems()
    print("[MUGGY_Init] ========== INITIALIZING CLIENT SYSTEMS ==========")

    local systems = {
        {name = "ClientInit", path = "NPCSystem/MUGGY_ClientInit"}
    }

    local successCount = 0
    local failureCount = 0

    for _, system in ipairs(systems) do
        if initializeSystem(system.name, system.path, "client") then
            successCount = successCount + 1
        else
            failureCount = failureCount + 1
        end
    end

    print("[MUGGY_Init] Client systems complete: " .. successCount .. " succeeded, " .. failureCount .. " failed")
    initializationState.clientSystemsLoaded = true
    return true
end

local function initializeServerSystems()
    print("[MUGGY_Init] ========== INITIALIZING SERVER SYSTEMS ==========")

    local systems = {
        {name = "ZoneSpawningHandler", path = "NPCSystem/MUGGY_ZoneSpawningHandler"},
        {name = "ServerCommandHandler", path = "NPCSystem/MUGGY_ServerCommandHandler"}
    }

    local successCount = 0
    local failureCount = 0

    for _, system in ipairs(systems) do
        if initializeSystem(system.name, system.path, "server") then
            successCount = successCount + 1
        else
            failureCount = failureCount + 1
        end
    end

    print("[MUGGY_Init] Server systems complete: " .. successCount .. " succeeded, " .. failureCount .. " failed")
    initializationState.serverSystemsLoaded = true
    return true
end

local function onPlayerMove(player)
    if initializationState.started then
        return
    end

    initializationState.started = true

    print("[MUGGY_Init] ========== MUGGY NPC SYSTEM INITIALIZATION ==========")
    print("[MUGGY_Init] Triggered by OnPlayerMove")

    local envInfo = MUGGY_EnvironmentDetector.getEnvironmentInfo()
    print("[MUGGY_Init] Environment: " .. (envInfo.isSinglePlayer and "SP" or "MP"))

    initializeSharedSystems()

    if not isServer() or MUGGY_EnvironmentDetector.isSinglePlayer() then
        initializeClientSystems()
    end

    if isServer() or not isClient() or MUGGY_EnvironmentDetector.isSinglePlayer() then
        initializeServerSystems()
    end

    initializationState.completed = true
    print("[MUGGY_Init] ========== INITIALIZATION COMPLETE ==========")

    if Events and Events.OnPlayerMove then
        Events.OnPlayerMove.Remove(onPlayerMove)
    end
end

function MUGGY_InitializationCoordinator.initialize()
    if initializationState.started then
        return
    end

    print("[MUGGY_Init] Coordinator loaded, waiting for OnPlayerMove...")

    if Events and Events.OnPlayerMove then
        Events.OnPlayerMove.Add(onPlayerMove)
    else
        print("[MUGGY_Init] WARNING: OnPlayerMove event not available")
    end
end

function MUGGY_InitializationCoordinator.getStatus()
    return {
        started = initializationState.started,
        completed = initializationState.completed,
        sharedSystemsLoaded = initializationState.sharedSystemsLoaded,
        clientSystemsLoaded = initializationState.clientSystemsLoaded,
        serverSystemsLoaded = initializationState.serverSystemsLoaded,
        results = initializationResults
    }
end

MUGGY_InitializationCoordinator.initialize()

return MUGGY_InitializationCoordinator
