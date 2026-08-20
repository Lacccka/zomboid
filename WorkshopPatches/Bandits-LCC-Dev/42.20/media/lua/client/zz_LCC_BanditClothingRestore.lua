-- LCC controlled PoC for the B42.20 Bandits reconnect clothing bug.
--
-- Bandit.ApplyVisuals() rebuilds brain.clothing as ItemVisual objects and clears
-- real WornItems. Persistent Bandits can therefore retain the clothing metadata
-- and visuals while losing the actual wearable state after reconnect. This
-- wrapper runs after upstream ApplyVisuals and materializes only missing real
-- WornItems. It does NOT add them to the inventory or death-spawn queue; vanilla
-- corpse creation can then copy the real worn state without duplicating the
-- upstream death-item queue.
if isServer() then return end

local MARKER = "real-worn-reconnect-v1"
LCC_BANDITS_CLOTHING_RESTORE = MARKER

if type(Bandit) ~= "table" or type(Bandit.ApplyVisuals) ~= "function" then
    print("[LCC][BanditsClothingPoC][DISABLED] Bandit.ApplyVisuals unavailable")
    return
end
if type(BanditCompatibility) ~= "table" or type(BanditCompatibility.InstanceItem) ~= "function" then
    print("[LCC][BanditsClothingPoC][DISABLED] BanditCompatibility.InstanceItem unavailable")
    return
end

local originalApplyVisuals = Bandit.ApplyVisuals
local stateByBandit = setmetatable({}, { __mode = "k" })
local stats = {
    applyCalls = 0,
    entitiesRestored = 0,
    slotsRestored = 0,
    itemsCreated = 0,
    cachedRewears = 0,
    conflicts = 0,
    bagRestored = 0,
}

local function fullType(item)
    if not item then return nil end
    local ok, value = pcall(function() return item:getFullType() end)
    if ok and value then return tostring(value) end
    return nil
end

local function characterId(character, brain)
    if brain and brain.id ~= nil then return tostring(brain.id) end
    if character and BanditUtils and type(BanditUtils.GetCharacterID) == "function" then
        local ok, value = pcall(BanditUtils.GetCharacterID, character)
        if ok and value ~= nil then return tostring(value) end
    end
    return "nil"
end

local function wornSize(bandit)
    local worn = bandit and bandit:getWornItems() or nil
    return worn and worn:size() or -1
end

local function expectedClothingCount(brain)
    if not brain or type(brain.clothing) ~= "table" then return 0 end
    local count = 0
    for _ in pairs(brain.clothing) do count = count + 1 end
    return count
end

local function applyTint(item, brain, bodyLocation)
    if not item or not brain or type(brain.tint) ~= "table" then return end
    local packed = brain.tint[bodyLocation]
    if packed == nil or not BanditUtils or type(BanditUtils.dec2rgb) ~= "function" then return end

    pcall(function()
        local visual = item:getVisual()
        if not visual then return end
        local color = BanditUtils.dec2rgb(packed)
        visual:setTint(ImmutableColor.new(color.r, color.g, color.b, 1))
    end)
end

local function itemState(bandit)
    local state = stateByBandit[bandit]
    if not state then
        state = {
            items = {},
            reportedRestore = false,
            reportedConflicts = {},
        }
        stateByBandit[bandit] = state
    end
    return state
end

local function createRealItem(itemType, brain, bodyLocation)
    local item = BanditCompatibility.InstanceItem(itemType)
    if not item then return nil end

    local md = item:getModData()
    if md then
        md.LCC_BanditsRealClothing = MARKER
        md.preserve = true
    end
    applyTint(item, brain, bodyLocation)
    stats.itemsCreated = stats.itemsCreated + 1
    return item
end

local function ensureSlot(bandit, brain, state, bodyLocation, itemType)
    if not bodyLocation or not itemType then return 0, 0 end

    local current = bandit:getWornItem(bodyLocation)
    if current then
        if fullType(current) == tostring(itemType) then
            state.items[bodyLocation] = current
            return 0, 0
        end

        if not state.reportedConflicts[bodyLocation] then
            state.reportedConflicts[bodyLocation] = true
            stats.conflicts = stats.conflicts + 1
            print(string.format(
                "[LCC][BanditsClothingPoC][SLOT_CONFLICT] id=%s location=%s expected=%s actual=%s intervention=false",
                characterId(bandit, brain),
                tostring(bodyLocation),
                tostring(itemType),
                tostring(fullType(current) or "<unknown>")
            ))
        end
        return 0, 1
    end

    local item = state.items[bodyLocation]
    local created = false
    if not item or fullType(item) ~= tostring(itemType) then
        item = createRealItem(itemType, brain, bodyLocation)
        if not item then return 0, 0 end
        state.items[bodyLocation] = item
        created = true
    else
        stats.cachedRewears = stats.cachedRewears + 1
        applyTint(item, brain, bodyLocation)
    end

    bandit:setWornItem(bodyLocation, item)
    stats.slotsRestored = stats.slotsRestored + 1
    return 1, created and 1 or 0
end

local function bagName(brain)
    if not brain or brain.bag == nil then return nil end
    if type(brain.bag) == "table" then return brain.bag.name end
    if type(brain.bag) == "string" then return brain.bag end
    return nil
end

local function ensureBag(bandit, brain, state)
    local itemType = bagName(brain)
    if not itemType then return 0 end

    local key = "__bag"
    local item = state.items[key]
    if not item or fullType(item) ~= tostring(itemType) then
        item = createRealItem(itemType, brain, nil)
        if not item then return 0 end
        state.items[key] = item
    end

    local okLocation, bodyLocation = pcall(function() return item:canBeEquipped() end)
    if not okLocation or not bodyLocation or tostring(bodyLocation) == "" then return 0 end

    local current = bandit:getWornItem(bodyLocation)
    if current then
        if current == item or fullType(current) == tostring(itemType) then
            state.items[key] = current
        end
        return 0
    end

    bandit:setWornItem(bodyLocation, item)
    stats.bagRestored = stats.bagRestored + 1
    return 1
end

Bandit.ApplyVisuals = function(bandit, brain)
    originalApplyVisuals(bandit, brain)
    stats.applyCalls = stats.applyCalls + 1

    if not bandit or not brain or type(brain.clothing) ~= "table" then return end
    if not bandit:isAlive() then return end

    local state = itemState(bandit)
    local beforeWorn = wornSize(bandit)
    local expected = expectedClothingCount(brain)
    local restored, created, conflicts = 0, 0, 0

    if BanditCompatibility.GetBodyLocationsOrdered then
        for _, bodyLocation in pairs(BanditCompatibility.GetBodyLocationsOrdered()) do
            local itemType = brain.clothing[bodyLocation]
            if itemType then
                local r, c = ensureSlot(bandit, brain, state, bodyLocation, itemType)
                restored = restored + r
                created = created + c
            end
        end
    else
        for bodyLocation, itemType in pairs(brain.clothing) do
            local r, c = ensureSlot(bandit, brain, state, bodyLocation, itemType)
            restored = restored + r
            created = created + c
        end
    end

    restored = restored + ensureBag(bandit, brain, state)
    local afterWorn = wornSize(bandit)

    if restored > 0 then
        bandit:resetModelNextFrame()
        stats.entitiesRestored = stats.entitiesRestored + (state.reportedRestore and 0 or 1)
        if not state.reportedRestore then
            state.reportedRestore = true
            print(string.format(
                "[LCC][BanditsClothingPoC][RESTORE] marker=%s id=%s beforeWorn=%d expectedClothing=%d restored=%d created=%d afterWorn=%d bag=%s",
                MARKER,
                characterId(bandit, brain),
                beforeWorn,
                expected,
                restored,
                created,
                afterWorn,
                tostring(bagName(brain) or "<none>")
            ))
        end
    end
end

print(string.format(
    "[LCC][BanditsClothingPoC][BOOT] marker=%s mode=materialize-missing-real-worn inventoryAdd=false",
    MARKER
))
