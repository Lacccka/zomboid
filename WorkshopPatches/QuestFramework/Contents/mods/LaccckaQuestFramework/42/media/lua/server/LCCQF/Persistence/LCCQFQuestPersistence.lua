require "LCCQF/LCCQFConstants"
require "LCCQF/Quest/LCCQFQuestRegistry"
require "LCCQF/Quest/LCCQFQuestInstance"
require "LCCQF/Persistence/LCCQFCharacterIdentity"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local QuestRegistry = LCCQF.QuestRegistry
local QuestInstance = LCCQF.QuestInstance
local CharacterIdentity = LCCQF.CharacterIdentity
local QuestPersistence = LCCQF.QuestPersistence or {}
local normalizedCharacters = {}

local function log(message)
    print(C.LOG_PREFIX .. "[PERSISTENCE:SERVER] " .. tostring(message))
end

local function ensureStore(record)
    if not record then return nil end

    local store = record.quests
    if type(store) ~= "table" then
        store = {}
        record.quests = store
    end
    if tonumber(store.schemaVersion) == nil then
        store.schemaVersion = C.QUEST_PERSISTENCE_SCHEMA_VERSION
    end
    if type(store.byInstanceId) ~= "table" then
        store.byInstanceId = {}
    end
    if type(store.byQuestId) ~= "table" then
        store.byQuestId = {}
    end
    return store
end

local function normalizeStore(characterId, store)
    if normalizedCharacters[characterId] then return 0, 0 end

    local restored = 0
    local dropped = 0
    local canonicalByQuestId = {}

    for instanceId, rawInstance in pairs(store.byInstanceId) do
        local definition = type(rawInstance) == "table"
            and QuestRegistry.Get(rawInstance.questId)
            or nil

        local instance = nil
        if definition then
            instance = QuestInstance.Restore(rawInstance, definition, characterId)
        end

        if instance then
            local canonicalId = tostring(instance.id)
            if canonicalId ~= tostring(instanceId) then
                store.byInstanceId[instanceId] = nil
            end
            store.byInstanceId[canonicalId] = instance
            canonicalByQuestId[instance.questId] = canonicalId
            restored = restored + 1
        else
            store.byInstanceId[instanceId] = nil
            dropped = dropped + 1
        end
    end

    store.byQuestId = canonicalByQuestId
    store.schemaVersion = C.QUEST_PERSISTENCE_SCHEMA_VERSION
    normalizedCharacters[characterId] = true
    return restored, dropped
end

function QuestPersistence.Initialize()
    local ok, err = CharacterIdentity.Initialize()
    if not ok then return false, err end

    normalizedCharacters = {}
    log("initialized schemaVersion=" .. tostring(C.QUEST_PERSISTENCE_SCHEMA_VERSION))
    return true
end

function QuestPersistence.GetQuestStore(player, create)
    local record, characterId = CharacterIdentity.GetRecord(player, create ~= false)
    if not record or not characterId then return nil, nil, false end

    local store = ensureStore(record)
    if not store then return nil, characterId, false end

    local restored, dropped = normalizeStore(characterId, store)
    if restored > 0 or dropped > 0 then
        log("normalized characterId=" .. tostring(characterId)
            .. " restored=" .. tostring(restored)
            .. " dropped=" .. tostring(dropped))
    end

    return store, characterId, true
end

function QuestPersistence.Touch(characterId)
    return CharacterIdentity.Touch(characterId)
end

function QuestPersistence.RetireCharacter(player, reason)
    local changed, characterId = CharacterIdentity.Retire(player, reason)
    if changed and characterId then
        normalizedCharacters[characterId] = nil
    end
    return changed, characterId
end

function QuestPersistence.GetCharacterId(player)
    local characterId = CharacterIdentity.GetExistingCharacterId(player)
    if characterId then return characterId end
    return CharacterIdentity.Resolve(player)
end

LCCQF.QuestPersistence = QuestPersistence

return QuestPersistence
