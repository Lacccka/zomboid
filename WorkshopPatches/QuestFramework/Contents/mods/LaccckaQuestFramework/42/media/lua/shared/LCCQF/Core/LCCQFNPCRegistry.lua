require "LCCQF/LCCQFConstants"

LCCQF = LCCQF or {}

local Registry = LCCQF.NPCRegistry or {}
local definitions = Registry.definitions or {}

local function isIdentifier(value)
    return type(value) == "string"
        and value ~= ""
        and #value <= LCCQF.Constants.MAX_IDENTIFIER_LENGTH
end

function Registry.Register(definition)
    if type(definition) ~= "table" then
        return false, "definition must be a table"
    end
    if not isIdentifier(definition.npcId) then
        return false, "invalid npcId"
    end
    if not isIdentifier(definition.displayName) then
        return false, "invalid displayName"
    end
    if not isIdentifier(definition.dialogueId) then
        return false, "invalid dialogueId"
    end
    if type(definition.runtime) ~= "table" or not isIdentifier(definition.runtime.adapter) then
        return false, "invalid runtime adapter"
    end
    if definitions[definition.npcId] then
        return false, "duplicate npcId"
    end

    definitions[definition.npcId] = definition
    return true
end

function Registry.Get(npcId)
    if not isIdentifier(npcId) then return nil end
    return definitions[npcId]
end

function Registry.IsRegistered(npcId)
    return Registry.Get(npcId) ~= nil
end

function Registry.GetDefinitions()
    return definitions
end

Registry.definitions = definitions
LCCQF.NPCRegistry = Registry

-- Temporary compatibility alias for code/content outside the new core boundary.
function LCCQF.GetNPCDefinition(npcId)
    return Registry.Get(npcId)
end

return Registry
