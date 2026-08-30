require "LCCQF/LCCQFConstants"

LCCQF = LCCQF or {}

local Registry = LCCQF.NPCRegistry or {}
local definitions = Registry.definitions or {}

local function isIdentifier(value)
    return type(value) == "string"
        and value ~= ""
        and #value <= LCCQF.Constants.MAX_IDENTIFIER_LENGTH
end

local function validateDefinition(definition)
    if type(definition) ~= "table" then return false, "definition must be a table" end
    if not isIdentifier(definition.npcId) then return false, "invalid npcId" end
    if not isIdentifier(definition.displayNameKey) then return false, "invalid displayNameKey" end
    if not isIdentifier(definition.dialogueId) then return false, "invalid dialogueId" end
    if type(definition.runtime) ~= "table" or not isIdentifier(definition.runtime.adapter) then
        return false, "invalid runtime adapter"
    end
    return true
end

function Registry.Register(definition)
    local ok, errorText = validateDefinition(definition)
    if not ok then return false, errorText end
    if definitions[definition.npcId] then return false, "duplicate npcId" end
    definitions[definition.npcId] = definition
    return true
end

-- Generated server definitions are still full logical NPC definitions, but unlike authored
-- content they may be refreshed when a faction member moves between sites/roles. Authored
-- definitions can never be overwritten by this path.
function Registry.RegisterGenerated(definition)
    local ok, errorText = validateDefinition(definition)
    if not ok then return false, errorText end

    local existing = definitions[definition.npcId]
    if existing and existing.generated ~= true then return false, "npcId belongs to authored definition" end
    definition.generated = true
    definitions[definition.npcId] = definition
    return true
end

-- Public client projections are deliberately smaller than logical server definitions.
-- They exist only so nearby materialized generated NPCs can use the common interaction UI.
function Registry.ApplyPublicDefinition(publicDefinition)
    if type(publicDefinition) ~= "table" then return false, "invalid public definition" end
    if not isIdentifier(publicDefinition.npcId)
        or not isIdentifier(publicDefinition.displayNameKey)
        or not isIdentifier(publicDefinition.runtimeAdapter)
    then
        return false, "invalid public definition"
    end

    local existing = definitions[publicDefinition.npcId]
    if existing and existing.generated ~= true and existing.publicProjection ~= true then
        -- Authored content already has richer local data; never replace it with a projection.
        return true
    end

    definitions[publicDefinition.npcId] = {
        npcId = publicDefinition.npcId,
        displayNameKey = publicDefinition.displayNameKey,
        dialogueId = "lccq_public_server_dialogue",
        interactive = publicDefinition.interactive ~= false,
        generated = true,
        publicProjection = true,
        runtime = { adapter = publicDefinition.runtimeAdapter },
    }
    return true
end

function Registry.MakePublicDefinition(npcId)
    local definition = Registry.Get(npcId)
    if not definition then return nil end
    return {
        npcId = definition.npcId,
        displayNameKey = definition.displayNameKey,
        interactive = definition.interactive ~= false,
        runtimeAdapter = definition.runtime and definition.runtime.adapter or nil,
    }
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
