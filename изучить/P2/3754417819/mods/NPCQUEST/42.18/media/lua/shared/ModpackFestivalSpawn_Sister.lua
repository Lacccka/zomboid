-- Sister data + Bandits2 companion glue.

ModpackFestivalSister = ModpackFestivalSister or {}

local MOD_ID = "ModpackFestivalSpawn"

ModpackFestivalSister.DEFAULT_SISTER_NAME = "Alyssa"
ModpackFestivalSister.CLAN_ID = "1783e8de-e11a-49c2-9e96-851190aa072b"
ModpackFestivalSister.BANDIT_ID = "1b8a2810-0d14-4ea7-8c85-65b2ba089ab3"
ModpackFestivalSister.PROGRAM_NAME = "ModpackCompanion"
ModpackFestivalSister.SPAWN_X = 13595
ModpackFestivalSister.SPAWN_Y = 1292
ModpackFestivalSister.SPAWN_Z = 0
ModpackFestivalSister.FULL_HEALTH = 50.0
ModpackFestivalSister.FOLLOW_SPEED_MULT = 1.50   -- base 1.25 × 1.2
ModpackFestivalSister.MOVEMENT_SPEED = 1.05       -- base 0.875 × 1.2
ModpackFestivalSister.ANIMATION_SPEED_MULT = 1.50 -- base 1.25 × 1.2
ModpackFestivalSister.WALK_ANIM_SPEED = 1.56      -- base 1.30 × 1.2; Bandits default is 1.04
ModpackFestivalSister.RUN_ANIM_SPEED = 1.05       -- base 0.875 × 1.2
ModpackFestivalSister.LIMP_ANIM_SPEED = 1.20      -- base 1.00 × 1.2; Bandits default is 0.80
ModpackFestivalSister.INVENTORY_CAPACITY = 20
ModpackFestivalSister.UNLIMITED_STAMINA_VALUE = 10

-- Items added to Alyssa's physical inventory on first spawn (no prior snapshot).
-- Add/remove entries here to change her default loadout.
ModpackFestivalSister.DEFAULT_SPAWN_ITEMS = {
    { fullType = "Base.BaseballBat", count = 1, slot = "primary" },
    { fullType = "Base.KitchenKnife", count = 1 },
}
ModpackFestivalSister.DEFAULT_EQUIPPED_WEAPON = "Base.BaseballBat"

function ModpackFestivalSister.getState()
    local md = ModData.getOrCreate(MOD_ID)
    md.sister = md.sister or {}
    return md.sister
end

function ModpackFestivalSister.parseForenameFromBuildString(buildString)
    if not buildString or buildString == "" then
        return nil
    end
    local namePart = tostring(buildString):match("name=([^;]+)")
    if not namePart then
        return nil
    end
    local forename = namePart:match("^([^|]*)")
    if forename and forename ~= "" then
        return forename
    end
    return nil
end

function ModpackFestivalSister.storeSisterAppearance(appearance, buildString)
    local md = ModpackFestivalSister.getState()
    md.sisterAppearanceData = appearance
    md.sisterBuildString = buildString

    local forename = appearance and appearance.forename
    if (not forename or forename == "") and buildString then
        forename = ModpackFestivalSister.parseForenameFromBuildString(buildString)
    end
    if forename and forename ~= "" then
        md.sisterForename = forename
        if ModpackFestivalQuests and ModpackFestivalQuests.rememberSisterForename then
            ModpackFestivalQuests.rememberSisterForename(forename)
        end
    end
end

function ModpackFestivalSister.getSisterAppearanceData()
    return ModpackFestivalSister.getState().sisterAppearanceData
end

function ModpackFestivalSister.getSisterForename()
    local md = ModpackFestivalSister.getState()
    if md.sisterForename and md.sisterForename ~= "" then
        return md.sisterForename
    end
    if md.sisterBuildString then
        local fromBuild = ModpackFestivalSister.parseForenameFromBuildString(md.sisterBuildString)
        if fromBuild then
            md.sisterForename = fromBuild
            return fromBuild
        end
    end
    local appearance = md.sisterAppearanceData
    if appearance and appearance.forename and appearance.forename ~= "" then
        md.sisterForename = appearance.forename
        return appearance.forename
    end
    return ModpackFestivalSister.DEFAULT_SISTER_NAME
end

function ModpackFestivalSister.markScriptedSpeechActive(durationMs)
    local now = getTimestampMs and getTimestampMs() or 0
    local untilMs = now + (durationMs or 12000)
    ModpackFestivalSister.scriptedSpeechUntilMs = math.max(
        ModpackFestivalSister.scriptedSpeechUntilMs or 0,
        untilMs
    )
end

function ModpackFestivalSister.isScriptedSpeechActive()
    local now = getTimestampMs and getTimestampMs() or 0
    return (ModpackFestivalSister.scriptedSpeechUntilMs or 0) > now
end

function ModpackFestivalSister.sayAsSister(line, scripted)
    if not line or line == "" then
        return false
    end
    if scripted then
        ModpackFestivalSister.markScriptedSpeechActive(12000)
    end
    local sister = ModpackFestivalSister.findSisterBandit()
    if sister and sister.addLineChatElement then
        sister:addLineChatElement(line, 0.9, 0.85, 1.0)
        return true
    end
    return false
end

function ModpackFestivalSister.shouldUseFollowAi()
    return true
end

function ModpackFestivalSister.isSisterWorldSearchSuspended()
    return false
end

local function getBrain(bandit)
    if not bandit or not bandit.getModData then
        return nil
    end
    if BanditBrain and BanditBrain.Get then
        local ok, brain = pcall(BanditBrain.Get, bandit)
        if ok and brain then
            return brain
        end
    end
    local md = bandit:getModData()
    return md and md.brain
end

local function syncBrain(bandit, brain)
    if not bandit or not brain then
        return
    end
    if BanditBrain and BanditBrain.Update then
        pcall(BanditBrain.Update, bandit, brain)
    elseif bandit.getModData then
        bandit:getModData().brain = brain
    end
    if bandit.transmitModData then
        pcall(function() bandit:transmitModData() end)
    end
end

local function getPlayerId(player)
    if player and BanditUtils and BanditUtils.GetCharacterID then
        local ok, id = pcall(BanditUtils.GetCharacterID, player)
        if ok then
            return id
        end
    end
    return nil
end

function ModpackFestivalSister.isSisterBrain(brain)
    if not brain then
        return false
    end
    if brain.modpackFestivalSister == true then
        return true
    end
    return brain.cid == ModpackFestivalSister.CLAN_ID or brain.bid == ModpackFestivalSister.BANDIT_ID
end

function ModpackFestivalSister.markSisterModData(bandit)
    if not bandit or not bandit.getModData then
        return false
    end
    local md = bandit:getModData()
    if not md then
        return false
    end
    md.modpackFestivalSister = true
    md.modpackFestivalSisterProtected = true
    md.modpackFestivalSisterClanId = ModpackFestivalSister.CLAN_ID
    md.modpackFestivalSisterBanditId = ModpackFestivalSister.BANDIT_ID
    if bandit.transmitModData then
        pcall(function() bandit:transmitModData() end)
    end
    return true
end

function ModpackFestivalSister.hasSisterModData(bandit)
    if not bandit or not bandit.getModData then
        return false
    end
    local md = bandit:getModData()
    return md and (md.modpackFestivalSister == true
        or md.modpackFestivalSisterProtected == true
        or md.modpackFestivalSisterClanId == ModpackFestivalSister.CLAN_ID
        or md.modpackFestivalSisterBanditId == ModpackFestivalSister.BANDIT_ID)
end

function ModpackFestivalSister.isSisterBandit(bandit)
    if not bandit or not bandit.getVariableBoolean then
        return false
    end
    local ok, isBandit = pcall(function()
        return bandit:getVariableBoolean("Bandit")
    end)
    if not ok or not isBandit then
        return ModpackFestivalSister.hasSisterModData(bandit)
    end
    return ModpackFestivalSister.isSisterBrain(getBrain(bandit))
        or ModpackFestivalSister.hasSisterModData(bandit)
end

function ModpackFestivalSister.findSisterBandit()
    if not BanditZombie then
        return nil
    end
    if BanditZombie.Cache then
        for _, bandit in pairs(BanditZombie.Cache) do
            if ModpackFestivalSister.isSisterBandit(bandit)
                and not (bandit.isDead and bandit:isDead()) then
                return bandit
            end
        end
    end
    if BanditZombie.CacheLightB and BanditZombie.GetInstanceById then
        for id, light in pairs(BanditZombie.CacheLightB) do
            if light and ModpackFestivalSister.isSisterBrain(light.brain) then
                local bandit = BanditZombie.GetInstanceById(id)
                if bandit and not (bandit.isDead and bandit:isDead()) then
                    return bandit
                end
            end
        end
    end
    return nil
end

function ModpackFestivalSister.applyStoredAppearanceToBrain(brain)
    if not brain then
        return
    end
    local appearance = ModpackFestivalSister.getSisterAppearanceData()
    if appearance then
        brain.female = true
        brain.skin = appearance.skin or brain.skin
        brain.hairType = appearance.hairType or brain.hairType
        brain.hairColor = appearance.hairColor or brain.hairColor
        if appearance.clothing then
            brain.clothing = appearance.clothing
        end
        if appearance.tint then
            brain.tint = appearance.tint
        end
    end
    brain.fullname = ModpackFestivalSister.getSisterForename()
end

local function instanceItem(itemType)
    if not itemType or itemType == "" then
        return nil
    end
    if BanditCompatibility and BanditCompatibility.InstanceItem then
        local ok, item = pcall(BanditCompatibility.InstanceItem, itemType)
        if ok and item then
            return item
        end
    end
    if InventoryItemFactory and InventoryItemFactory.CreateItem then
        local ok, item = pcall(InventoryItemFactory.CreateItem, itemType)
        if ok and item then
            return item
        end
    end
    return nil
end

local function colorFromPackedRgb(packed)
    if not packed then
        return nil
    end
    if BanditUtils and BanditUtils.dec2rgb then
        local ok, color = pcall(BanditUtils.dec2rgb, packed)
        if ok and color then
            return color
        end
    end
    local r = math.floor(packed / 65536) % 256
    local g = math.floor(packed / 256) % 256
    local b = packed % 256
    return { r = r / 255, g = g / 255, b = b / 255 }
end

local function immutableColorFromRgb(color)
    if not color or not ImmutableColor then
        return nil
    end
    return ImmutableColor.new(color.r or 1, color.g or 1, color.b or 1, color.a or 1)
end

local function getAppearanceSignature(appearance)
    if not appearance then
        return ""
    end
    local parts = {
        tostring(appearance.skin or ""),
        tostring(appearance.hairModel or appearance.hairType or ""),
        tostring(appearance.hairColorRgb and appearance.hairColorRgb.r or appearance.hairColor or ""),
    }
    local keys = {}
    for bodyLocation in pairs(appearance.clothing or {}) do
        table.insert(keys, bodyLocation)
    end
    table.sort(keys)
    for _, bodyLocation in ipairs(keys) do
        table.insert(parts, bodyLocation .. "=" .. tostring(appearance.clothing[bodyLocation])
            .. ":" .. tostring(appearance.tint and appearance.tint[bodyLocation] or ""))
    end
    return table.concat(parts, "|")
end

local function hasItemVisuals(bandit)
    if not bandit or not bandit.getItemVisuals then
        return false
    end
    local ok, result = pcall(function()
        local itemVisuals = bandit:getItemVisuals()
        return itemVisuals and itemVisuals.size and itemVisuals:size() > 0
    end)
    return ok and result == true
end

local function applyClothingSlot(bandit, appearance, bodyLocation, itemType)
    local item = instanceItem(itemType)
    if not item then
        return
    end
    local packedTint = appearance.tint and appearance.tint[bodyLocation]
    local tint = immutableColorFromRgb(colorFromPackedRgb(packedTint))

    if ItemVisual and bandit.getItemVisuals then
        local itemVisual = ItemVisual.new()
        if itemVisual.setItemType then
            itemVisual:setItemType(itemType)
        end
        if itemVisual.setClothingItemName then
            itemVisual:setClothingItemName(itemType)
        end
        if tint and itemVisual.setTint then
            itemVisual:setTint(tint)
        end
        bandit:getItemVisuals():add(itemVisual)
    end

    if tint and item.getVisual then
        local visual = item:getVisual()
        if visual and visual.setTint then
            visual:setTint(tint)
        end
    end
    -- intentionally NOT calling setWornItem — ItemVisual handles the visual,
    -- and setWornItem would block the player from taking the item out of inventory
end

function ModpackFestivalSister.applyStoredAppearanceToBandit(bandit, force)
    if not bandit then
        return false
    end
    local appearance = ModpackFestivalSister.getSisterAppearanceData()
    if not appearance then
        return false
    end
    local signature = getAppearanceSignature(appearance)
    local md = bandit.getModData and bandit:getModData() or nil
    if not force
        and md and md.modpackFestivalSisterAppearanceSig == signature
        and hasItemVisuals(bandit) then
        return true
    end

    local clothing = appearance.clothing or {}

    local ok = pcall(function()
        local human = bandit.getHumanVisual and bandit:getHumanVisual()
        if human then
            if appearance.skin and human.setSkinTextureIndex then
                human:setSkinTextureIndex(math.max(0, appearance.skin - 1))
            end
            if appearance.hairModel and appearance.hairModel ~= "" and human.setHairModel then
                human:setHairModel(appearance.hairModel)
            elseif appearance.hairType and Bandit and Bandit.GetHairStyle and human.setHairModel then
                human:setHairModel(Bandit.GetHairStyle(true, appearance.hairType))
            end
            if appearance.hairColorRgb and human.setHairColor then
                local color = immutableColorFromRgb(appearance.hairColorRgb)
                if color then
                    human:setHairColor(color)
                end
            elseif appearance.hairColor and Bandit and Bandit.GetHairColor and human.setHairColor then
                local c = Bandit.GetHairColor(appearance.hairColor)
                if c then
                    human:setHairColor(ImmutableColor.new(c.r, c.g, c.b, 1))
                end
            end
        end

        if bandit.getWornItems then
            bandit:getWornItems():clear()
        end
        if bandit.getItemVisuals then
            bandit:getItemVisuals():clear()
        end

        local applied = {}
        if BanditCompatibility and BanditCompatibility.GetBodyLocationsOrdered then
            for _, bodyLocation in pairs(BanditCompatibility.GetBodyLocationsOrdered()) do
                local itemType = clothing[bodyLocation]
                if itemType then
                    applyClothingSlot(bandit, appearance, bodyLocation, itemType)
                    applied[bodyLocation] = true
                end
            end
        end
        for bodyLocation, itemType in pairs(clothing) do
            if not applied[bodyLocation] then
                applyClothingSlot(bandit, appearance, bodyLocation, itemType)
            end
        end

        if bandit.resetModel then
            bandit:resetModel()
        end
        if bandit.resetModelNextFrame then
            bandit:resetModelNextFrame()
        end
        if md then
            md.modpackFestivalSisterAppearanceSig = signature
        end
    end)
    return ok == true
end

function ModpackFestivalSister.protectSister(bandit)
    if not ModpackFestivalSister.isSisterBandit(bandit) then
        return false
    end
    ModpackFestivalSister.markSisterModData(bandit)
    pcall(function()
        if bandit.setNoDamage then
            bandit:setNoDamage(true)
        end
    end)
    pcall(function()
        if bandit.setInvincible then
            bandit:setInvincible(true)
        end
    end)
    pcall(function()
        if bandit.setHealth then
            bandit:setHealth(ModpackFestivalSister.FULL_HEALTH)
        end
    end)
    pcall(function()
        if bandit.setNoTeeth then
            bandit:setNoTeeth(true)
        end
    end)
    pcall(function()
        if bandit.setReanim then
            bandit:setReanim(false)
        end
    end)
    -- clear Bandits2 death-drop list so she doesn't spawn water bottles / extra ammo on respawn
    pcall(function()
        if bandit.clearItemsToSpawnAtDeath then
            bandit:clearItemsToSpawnAtDeath()
        end
    end)
    local brain = getBrain(bandit)
    if brain then
        brain.health = ModpackFestivalSister.FULL_HEALTH
        brain.modpackFestivalSister = true
        brain.immortal = true
        brain.accuracyBoost = 8  -- max; gives virtual x8 scope + best hit-chance calc
        brain.rnd = brain.rnd or {}
        brain.rnd[2] = 0  -- zero out individual aim/fire time variance
        syncBrain(bandit, brain)
    end
    ModpackFestivalSister.applyStoredAppearanceToBandit(bandit)
    ModpackFestivalSister.applyFollowSpeed(bandit)
    ModpackFestivalSister.applyAnimationSpeed(bandit)
    ModpackFestivalSister.applyInventoryCapacity(bandit)
    ModpackFestivalSister.applyUnlimitedStamina(bandit)
    return true
end

function ModpackFestivalSister.applyFollowSpeed(bandit)
    if not ModpackFestivalSister.isSisterBandit(bandit) then
        return false
    end
    pcall(function()
        if bandit.setVariable then
            bandit:setVariable("MovementSpeed", ModpackFestivalSister.MOVEMENT_SPEED)
        end
    end)
    pcall(function()
        if bandit.setSpeedMod then
            bandit:setSpeedMod(ModpackFestivalSister.FOLLOW_SPEED_MULT)
        end
    end)
    return true
end

function ModpackFestivalSister.applyAnimationSpeed(bandit)
    if not ModpackFestivalSister.isSisterBandit(bandit) then
        return false
    end
    pcall(function()
        if bandit.setVariable then
            bandit:setVariable("WalkSpeed", ModpackFestivalSister.WALK_ANIM_SPEED)
            bandit:setVariable("RunSpeed", ModpackFestivalSister.RUN_ANIM_SPEED)
            bandit:setVariable("LimpSpeed", ModpackFestivalSister.LIMP_ANIM_SPEED)
        end
    end)
    return true
end

function ModpackFestivalSister.applyInventoryCapacity(bandit)
    if not ModpackFestivalSister.isSisterBandit(bandit) then
        return false
    end
    if not bandit.getInventory then
        return false
    end
    local inv = bandit:getInventory()
    if not inv then
        return false
    end
    pcall(function()
        if inv.setCapacity then
            inv:setCapacity(ModpackFestivalSister.INVENTORY_CAPACITY)
        end
    end)
    pcall(function()
        if inv.setDrawDirty then
            inv:setDrawDirty(true)
        end
    end)
    return true
end

function ModpackFestivalSister.applyUnlimitedStamina(bandit)
    if not ModpackFestivalSister.isSisterBandit(bandit) then
        return false
    end
    local stamina = ModpackFestivalSister.UNLIMITED_STAMINA_VALUE
    local brain = getBrain(bandit)
    if brain then
        brain.endurance = stamina
        brain.stamina = stamina
        brain.fatigue = 0
        brain.tired = false
        brain.exhausted = false
        syncBrain(bandit, brain)
    end
    pcall(function()
        if bandit.setVariable then
            bandit:setVariable("Endurance", tostring(stamina))
            bandit:setVariable("Stamina", tostring(stamina))
            bandit:setVariable("Fatigue", "0")
        end
    end)
    pcall(function()
        if bandit.setFatigue then
            bandit:setFatigue(0)
        end
    end)
    return true
end

local function isMeleeCombatItem(item)
    if not item then
        return false
    end
    if instanceof then
        local ok, result = pcall(function()
            return instanceof(item, "HandWeapon")
        end)
        if ok and result == true then
            if item.isAimedFirearm then
                local firearmOk, isFirearm = pcall(function()
                    return item:isAimedFirearm()
                end)
                if firearmOk and isFirearm then
                    return false
                end
            end
            return true
        end
    end
    if item.getConditionMax and item.getMaxDamage then
        local ok, maxDamage = pcall(function()
            return item:getMaxDamage()
        end)
        return ok and (tonumber(maxDamage) or 0) > 0
    end
    return false
end

local function makeEmptyWeaponSlot()
    return {
        bulletsLeft = 0,
        magCount = 0,
        ammoCount = 0,
    }
end

local function sanitizeBanditWeaponSlots(weapons)
    if not weapons then
        return
    end
    if type(weapons.primary) ~= "table" then
        weapons.primary = makeEmptyWeaponSlot()
    else
        weapons.primary.bulletsLeft = tonumber(weapons.primary.bulletsLeft) or 0
        weapons.primary.magCount = tonumber(weapons.primary.magCount) or 0
        weapons.primary.ammoCount = tonumber(weapons.primary.ammoCount) or 0
    end
    -- secondary slot intentionally unused — sister holds one weapon at a time
    weapons.secondary = makeEmptyWeaponSlot()
end

ModpackFestivalSister.sanitizeWeaponSlots = sanitizeBanditWeaponSlots

local function getWeaponType(item)
    if not item or not item.IsWeapon or not item:IsWeapon() or not WeaponType then
        return nil
    end
    local ok, weaponType = pcall(function()
        return WeaponType.getWeaponType(item)
    end)
    if ok then
        return weaponType
    end
    return nil
end

local function getFirearmSlot(item)
    local weaponType = getWeaponType(item)
    if not weaponType then
        return nil
    end
    -- all firearms go to primary — no secondary slot
    if (WeaponType.FIREARM and weaponType == WeaponType.FIREARM)
        or (WeaponType.HANDGUN and weaponType == WeaponType.HANDGUN) then
        return "primary"
    end
    return nil
end

local function isTwoHandedWeapon(item)
    if not item then return false end
    local ok, result = pcall(function()
        if item.isTwoHandWeapon and item:isTwoHandWeapon() then return true end
        if item.getRequiresEquippedBothHands and item:getRequiresEquippedBothHands() then return true end
        return false
    end)
    return ok and result == true
end

local function inventoryItems(inv)
    local items = inv and inv.getItems and inv:getItems() or nil
    if not items or not items.size then
        return nil
    end
    return items
end

local function removeInventoryItem(inv, item)
    if not inv or not item then
        return
    end
    pcall(function()
        inv:Remove(item)
    end)
    pcall(function()
        if inv.removeItemOnServer then
            inv:removeItemOnServer(item)
        end
    end)
end

-- 1 real bullet lasts this many Bandits shots before inventory item is consumed
local AMMO_SHOT_MULTIPLIER = 4

local function getCurrentAmmoCount(item)
    if not item or not item.getCurrentAmmoCount then
        return 0
    end
    local ok, count = pcall(function()
        return item:getCurrentAmmoCount()
    end)
    if ok then
        return tonumber(count) or 0
    end
    return 0
end

local function consumeInventoryItemsByType(inv, fullType, predicate)
    local items = inventoryItems(inv)
    if not items or not fullType then
        return 0
    end
    local toRemove = {}
    for i = 0, items:size() - 1 do
        local invItem = items:get(i)
        local invType = invItem and invItem.getFullType and invItem:getFullType() or nil
        if invType == fullType and (not predicate or predicate(invItem)) then
            table.insert(toRemove, invItem)
        end
    end
    for _, invItem in ipairs(toRemove) do
        removeInventoryItem(inv, invItem)
    end
    return #toRemove
end

local function countInventoryItemsByType(inv, fullType, predicate)
    local items = inventoryItems(inv)
    if not items or not fullType then return 0 end
    local count = 0
    for i = 0, items:size() - 1 do
        local invItem = items:get(i)
        local invType = invItem and invItem.getFullType and invItem:getFullType() or nil
        if invType == fullType and (not predicate or predicate(invItem)) then
            count = count + 1
        end
    end
    return count
end

local function removeInventoryItemsByTypeCount(inv, fullType, n, predicate)
    local items = inventoryItems(inv)
    if not items or not fullType or n <= 0 then return 0 end
    local toRemove = {}
    for i = 0, items:size() - 1 do
        local invItem = items:get(i)
        local invType = invItem and invItem.getFullType and invItem:getFullType() or nil
        if invType == fullType and (not predicate or predicate(invItem)) then
            table.insert(toRemove, invItem)
            if #toRemove >= n then break end
        end
    end
    for _, invItem in ipairs(toRemove) do
        removeInventoryItem(inv, invItem)
    end
    return #toRemove
end

local function usesExternalMagazine(item)
    if not item then
        return false
    end
    if BanditCompatibility and BanditCompatibility.UsesExternalMagazine then
        local ok, result = pcall(BanditCompatibility.UsesExternalMagazine, item)
        if ok then
            return result == true
        end
    end
    local ok, magazineType = pcall(function()
        return item.getMagazineType and item:getMagazineType() or nil
    end)
    return ok and magazineType ~= nil and tostring(magazineType) ~= ""
end

local function makeFirearmWeaponRecord(item, inv, consumeAmmo, includeLoadedAmmo)
    local fullType = item and item.getFullType and item:getFullType() or nil
    if not fullType or fullType == "" then
        return nil
    end

    local record = {
        name = BanditCompatibility and BanditCompatibility.GetLegacyItem
            and BanditCompatibility.GetLegacyItem(fullType)
            or fullType,
        racked = false,
        bulletsLeft = includeLoadedAmmo and getCurrentAmmoCount(item) or 0,
    }

    if usesExternalMagazine(item) then
        local magazineType = item.getMagazineType and item:getMagazineType() or nil
        if not magazineType or magazineType == "" then
            return nil
        end
        local magSize = item.getMaxAmmo and item:getMaxAmmo() or 0
        record.type = "mag"
        record.clipIn = record.bulletsLeft > 0
        record.magName = tostring(magazineType)
        record.magSize = math.max(1, tonumber(magSize) or 1)
        record.magCount = 0
        if inv then
            local magInvCount = countInventoryItemsByType(inv, record.magName, function(magazine)
                return getCurrentAmmoCount(magazine) > 0
            end)
            local ammoType = item.getAmmoType and item:getAmmoType() or nil
            if ammoType and ammoType.getItemKey then
                ammoType = ammoType:getItemKey()
            end
            local looseMagEquiv = 0
            if ammoType and ammoType ~= "" then
                local looseRounds = countInventoryItemsByType(inv, tostring(ammoType))
                if looseRounds > 0 then
                    looseMagEquiv = math.max(1, math.floor(looseRounds / record.magSize))
                end
            end
            local totalMags = magInvCount + looseMagEquiv
            record.magCount = totalMags * AMMO_SHOT_MULTIPLIER
            record.virtualMax = record.magCount
        end
    else
        local ammoType = item.getAmmoType and item:getAmmoType() or nil
        if ammoType and ammoType.getItemKey then
            ammoType = ammoType:getItemKey()
        end
        if not ammoType or ammoType == "" then
            return nil
        end
        local ammoSize = item.getMaxAmmo and item:getMaxAmmo() or 0
        record.type = "nomag"
        record.ammoName = tostring(ammoType)
        record.ammoSize = math.max(1, tonumber(ammoSize) or 1)
        local invAmmo = inv and countInventoryItemsByType(inv, record.ammoName) or 0
        record.ammoCount = invAmmo * AMMO_SHOT_MULTIPLIER
        record.virtualMax = record.ammoCount
    end

    return record
end

local function mergeAmmoIntoWeaponRecord(existing, added)
    if not existing or not added or existing.name ~= added.name or existing.type ~= added.type then
        return added or existing
    end
    existing.bulletsLeft = tonumber(existing.bulletsLeft) or 0
    if existing.type == "mag" then
        existing.magCount = (tonumber(existing.magCount) or 0) + (tonumber(added.magCount) or 0)
        existing.magName = existing.magName or added.magName
        existing.magSize = existing.magSize or added.magSize
        if existing.clipIn == nil then
            existing.clipIn = existing.bulletsLeft > 0
        end
    elseif existing.type == "nomag" then
        existing.ammoCount = (tonumber(existing.ammoCount) or 0) + (tonumber(added.ammoCount) or 0)
        existing.ammoName = existing.ammoName or added.ammoName
        existing.ammoSize = existing.ammoSize or added.ammoSize
    end
    return existing
end

function ModpackFestivalSister.syncEquippedFirearmAmmo(bandit)
    if not ModpackFestivalSister.isSisterBandit(bandit) or not bandit.getInventory then
        return false
    end
    local brain = getBrain(bandit)
    local inv = bandit:getInventory()
    if not brain or not inv then
        return false
    end
    brain.weapons = brain.weapons or {}
    sanitizeBanditWeaponSlots(brain.weapons)

    local changed = false
    for _, slotName in ipairs({ "primary" }) do
        local weapon = brain.weapons[slotName]
        if weapon and weapon.name then
            local prevVirtual = tonumber(weapon.virtualMax) or 0
            local currentBrain = 0
            local invCount = 0
            local validSlot = false
            if weapon.type == "nomag" and weapon.ammoName then
                currentBrain = tonumber(weapon.ammoCount) or 0
                invCount = countInventoryItemsByType(inv, weapon.ammoName)
                validSlot = true
            elseif weapon.type == "mag" and weapon.magName then
                currentBrain = tonumber(weapon.magCount) or 0
                invCount = countInventoryItemsByType(inv, weapon.magName, function(m)
                    return getCurrentAmmoCount(m) > 0
                end)
                validSlot = true
            end

            if validSlot then
                local newVirtualMax = invCount * AMMO_SHOT_MULTIPLIER

                if newVirtualMax > prevVirtual then
                    local delta = newVirtualMax - prevVirtual
                    if weapon.type == "nomag" then
                        weapon.ammoCount = currentBrain + delta
                    else
                        weapon.magCount = currentBrain + delta
                    end
                    weapon.virtualMax = newVirtualMax
                    changed = true
                elseif currentBrain < prevVirtual then
                    local shotsFired = prevVirtual - currentBrain
                    local bulletsToRemove = math.floor(shotsFired / AMMO_SHOT_MULTIPLIER)
                    if bulletsToRemove > 0 then
                        if weapon.type == "nomag" then
                            removeInventoryItemsByTypeCount(inv, weapon.ammoName, bulletsToRemove)
                        else
                            removeInventoryItemsByTypeCount(inv, weapon.magName, bulletsToRemove,
                                function(m) return getCurrentAmmoCount(m) > 0 end)
                        end
                        local newInvCount = invCount - bulletsToRemove
                        weapon.virtualMax = math.max(0, newInvCount) * AMMO_SHOT_MULTIPLIER
                    else
                        weapon.virtualMax = newVirtualMax
                    end
                    changed = true
                elseif newVirtualMax < prevVirtual then
                    if weapon.type == "nomag" then
                        weapon.ammoCount = math.min(currentBrain, newVirtualMax)
                    else
                        weapon.magCount = math.min(currentBrain, newVirtualMax)
                    end
                    weapon.virtualMax = newVirtualMax
                    changed = true
                end
            end
        end
    end
    if changed then
        syncBrain(bandit, brain)
    end
    return changed
end

function ModpackFestivalSister.equipInventoryItem(bandit, item, slot)
    if not ModpackFestivalSister.isSisterBandit(bandit) or not item or not slot then
        return false
    end
    local inv = bandit.getInventory and bandit:getInventory() or nil
    if not inv then
        return false
    end
    local container = item.getContainer and item:getContainer() or nil
    if container and container ~= inv then
        return false
    end

    local fullType = item.getFullType and item:getFullType() or nil
    local firearmSlot = getFirearmSlot(item)
    local equipped = false
    if firearmSlot and slot ~= "wear" then
        -- clear both physical hands before equipping gun so sister never dual-wields
        pcall(function()
            if bandit.setPrimaryHandItem then bandit:setPrimaryHandItem(nil) end
            if bandit.setSecondaryHandItem then bandit:setSecondaryHandItem(nil) end
        end)
        pcall(function()
            if Bandit and Bandit.SetHands and fullType then
                Bandit.SetHands(bandit, fullType)
                equipped = true
            elseif bandit.setPrimaryHandItem then
                bandit:setPrimaryHandItem(item)
                equipped = true
            end
        end)
    elseif slot == "primary" or slot == "both" then
        local twoHanded = isTwoHandedWeapon(item)
        pcall(function()
            if bandit.setPrimaryHandItem then
                bandit:setPrimaryHandItem(item)
                equipped = true
            end
        end)
        if twoHanded then
            pcall(function()
                if bandit.setSecondaryHandItem then
                    bandit:setSecondaryHandItem(item)
                end
            end)
        else
            -- single-handed: clear secondary hand
            pcall(function()
                if bandit.setSecondaryHandItem then
                    bandit:setSecondaryHandItem(nil)
                end
            end)
        end
    end
    if slot == "wear" then
        pcall(function()
            local bodyLocation = item.getBodyLocation and item:getBodyLocation() or nil
            if bodyLocation and bandit.setWornItem then
                bandit:setWornItem(bodyLocation, item)
                equipped = true
            end
        end)
        pcall(function()
            if bandit.resetModelNextFrame then
                bandit:resetModelNextFrame()
            end
        end)
    end

    local brain = getBrain(bandit)
    if brain and fullType and fullType ~= "" then
        brain.weapons = brain.weapons or {}
        sanitizeBanditWeaponSlots(brain.weapons)
        if firearmSlot then
            local weaponRecord = makeFirearmWeaponRecord(item, inv, true, true)
            if weaponRecord then
                brain.weapons[firearmSlot] = weaponRecord
            end
            -- clear melee so sister doesn't switch back to it
            brain.weapons.melee = "Base.BareHands"
        elseif (slot == "primary" or slot == "both") and isMeleeCombatItem(item) then
            brain.weapons.melee = fullType
            -- clear firearm so companion program doesn't override with gun animations
            brain.weapons.primary = makeEmptyWeaponSlot()
            brain.weapons.secondary = makeEmptyWeaponSlot()
        elseif brain.weapons.melee == fullType then
            brain.weapons.melee = "Base.BareHands"
        end
        -- remember which weapon the player last equipped so we can restore it on respawn
        brain.modpackFestivalEquippedWeapon = fullType
        syncBrain(bandit, brain)
    end
    pcall(function()
        if bandit.transmitModData then
            bandit:transmitModData()
        end
    end)
    pcall(function()
        if inv.setDrawDirty then
            inv:setDrawDirty(true)
        end
    end)
    return equipped
end

local function compactInventoryEntries(entries)
    local counts = {}
    for _, entry in pairs(entries or {}) do
        local fullType = entry and entry.fullType
        local count = tonumber(entry and entry.count) or 1
        if fullType and fullType ~= "" and count > 0 then
            counts[fullType] = (counts[fullType] or 0) + count
        end
    end

    local compact = {}
    for fullType, count in pairs(counts) do
        table.insert(compact, { fullType = fullType, count = count })
    end
    table.sort(compact, function(a, b)
        return tostring(a.fullType) < tostring(b.fullType)
    end)
    return compact
end

function ModpackFestivalSister.inventoryEntriesSignature(entries)
    local compact = compactInventoryEntries(entries)
    local parts = {}
    for _, entry in ipairs(compact) do
        table.insert(parts, tostring(entry.fullType) .. "x" .. tostring(entry.count or 0))
    end
    return table.concat(parts, "|")
end

local function isWearableClothing(item)
    if not item then return false end
    local ok, loc = pcall(function()
        return item.getBodyLocation and item:getBodyLocation()
    end)
    return ok and loc ~= nil and tostring(loc) ~= ""
end

function ModpackFestivalSister.serializeBanditInventory(bandit)
    if not bandit or not bandit.getInventory then
        return {}
    end
    local inv = bandit:getInventory()
    local items = inv and inv.getItems and inv:getItems() or nil
    if not items or not items.size then
        return {}
    end

    local entries = {}
    local weaponsSeen = {}  -- deduplicate: only one copy of each weapon type
    for i = 0, items:size() - 1 do
        local item = items:get(i)
        local fullType = item and item.getFullType and item:getFullType() or nil
        if fullType and fullType ~= "" then
            -- skip wearable clothing — visuals are handled by ItemVisual system
            if not isWearableClothing(item) then
                local isWeapon = item.getMaxDamage ~= nil or item.getSwingSound ~= nil
                if isWeapon then
                    if not weaponsSeen[fullType] then
                        weaponsSeen[fullType] = true
                        table.insert(entries, { fullType = fullType, count = 1 })
                    end
                else
                    table.insert(entries, { fullType = fullType, count = 1 })
                end
            end
        end
    end

    -- Ammo stays in physical inventory (virtual shot multiplier system).
    return compactInventoryEntries(entries)
end

local function getInventoryFile()
    local saveId = "default"
    pcall(function()
        local dir = getSaveDir and getSaveDir() or nil
        if dir then
            -- pull the last path component (the save folder name)
            local name = tostring(dir):match("[/\\]([^/\\]+)[/\\]?$")
            if name and name ~= "" then saveId = name end
        end
    end)
    return "ModpackFestivalSpawn_SisterInventory_" .. saveId .. ".txt"
end

function ModpackFestivalSister.saveInventoryToFile(compact, equippedWeapon)
    pcall(function()
        local writer = getFileWriter and getFileWriter(getInventoryFile(), false, false)
        if not writer then return end
        if equippedWeapon and equippedWeapon ~= "" then
            writer:write("equipped=" .. tostring(equippedWeapon) .. "\n")
        end
        for _, entry in ipairs(compact or {}) do
            if entry.fullType and entry.count and entry.count > 0 then
                writer:write(tostring(entry.fullType) .. "=" .. tostring(entry.count) .. "\n")
            end
        end
        writer:close()
    end)
end

function ModpackFestivalSister.loadInventoryFromFile()
    local entries = {}
    local equippedWeapon = nil
    pcall(function()
        local reader = getFileReader and getFileReader(getInventoryFile(), false)
        if not reader then return end
        local line = reader:readLine()
        while line do
            line = tostring(line):gsub("[\r\n]+", "")
            if line ~= "" then
                local key, val = line:match("^([^=]+)=(.+)$")
                if key == "equipped" then
                    equippedWeapon = val
                elseif key and val then
                    local count = tonumber(val) or 1
                    if count > 0 then
                        table.insert(entries, { fullType = key, count = count })
                    end
                end
            end
            line = reader:readLine()
        end
        reader:close()
    end)
    if #entries > 0 then
        return entries, equippedWeapon
    end
    return nil, nil
end

function ModpackFestivalSister.storeInventorySnapshot(entries, equippedWeapon)
    local md = ModpackFestivalSister.getState()
    local compact = compactInventoryEntries(entries)
    local sig = ModpackFestivalSister.inventoryEntriesSignature(compact)
    local resolvedWeapon = equippedWeapon or (md.sisterInventoryLedger and md.sisterInventoryLedger.equippedWeapon)
    md.sisterInventoryLedger = {
        entries = compact,
        equippedWeapon = resolvedWeapon,
        signature = sig,
        savedAtMs = getTimestampMs and getTimestampMs() or 0,
    }
    if #compact > 0 then
        ModpackFestivalSister.saveInventoryToFile(compact, resolvedWeapon)
    end
    print("[ModpackFestivalSpawn][Sister] storeInventorySnapshot: " .. #compact .. " types, sig=" .. tostring(sig))
    return md.sisterInventoryLedger
end

function ModpackFestivalSister.captureInventoryFromBandit(bandit)
    if not ModpackFestivalSister.isSisterBandit(bandit) then
        return nil
    end
    local brain = getBrain(bandit)
    local equippedWeapon = brain and brain.modpackFestivalEquippedWeapon or nil
    local entries = ModpackFestivalSister.serializeBanditInventory(bandit)
    -- Don't overwrite a good snapshot with empty: headshots kill at C++ level before
    -- OnHitZombie fires, so inventory may already be cleared when we get here.
    -- The periodic sendSisterInventorySnapshot (onZombieUpdate) is the reliable source.
    if not entries or #entries == 0 then
        return nil
    end
    return ModpackFestivalSister.storeInventorySnapshot(entries, equippedWeapon)
end

function ModpackFestivalSister.getInventorySnapshot()
    -- ModData only — no file fallback here.
    -- The file is read exclusively by clientApplyInventoryFromFile on the client,
    -- guarded by a ModData ledger check, so stale files can't bleed into new games.
    local ledger = ModpackFestivalSister.getState().sisterInventoryLedger
    if ledger and ledger.entries and #ledger.entries > 0 then
        return ledger.entries
    end
    return nil
end

function ModpackFestivalSister.getEquippedWeaponSnapshot()
    local ledger = ModpackFestivalSister.getState().sisterInventoryLedger
    if ledger and ledger.equippedWeapon then
        return ledger.equippedWeapon
    end
    return nil
end

function ModpackFestivalSister.hasInventorySnapshot()
    return ModpackFestivalSister.getInventorySnapshot() ~= nil
end

local function clearInventory(inv)
    local items = inv and inv.getItems and inv:getItems() or nil
    if not items or not items.size then
        return
    end
    for i = items:size() - 1, 0, -1 do
        local item = items:get(i)
        if item then
            pcall(function()
                inv:Remove(item)
            end)
            pcall(function()
                if inv.removeItemOnServer then
                    inv:removeItemOnServer(item)
                end
            end)
        end
    end
end

function ModpackFestivalSister.applyInventorySnapshotToBandit(bandit, entries, clearFirst)
    if not ModpackFestivalSister.isSisterBandit(bandit) then
        return false
    end
    if not bandit.getInventory then
        return false
    end
    local inv = bandit:getInventory()
    if not inv then
        return false
    end
    ModpackFestivalSister.applyInventoryCapacity(bandit)

    -- strip any clothing or duplicate weapons that may have contaminated old snapshots
    local raw = entries or ModpackFestivalSister.getInventorySnapshot() or {}
    local cleaned = {}
    local weaponsSeen = {}
    for _, entry in ipairs(raw) do
        local item = entry.fullType and instanceItem(entry.fullType) or nil
        if item then
            if isWearableClothing(item) then
                -- drop it
            else
                local isWeapon = item.getMaxDamage ~= nil or item.getSwingSound ~= nil
                if isWeapon then
                    if not weaponsSeen[entry.fullType] then
                        weaponsSeen[entry.fullType] = true
                        table.insert(cleaned, entry)
                    end
                else
                    table.insert(cleaned, entry)
                end
            end
        else
            table.insert(cleaned, entry)
        end
    end
    local compact = compactInventoryEntries(cleaned)
    if clearFirst then
        clearInventory(inv)
    end

    local added = 0
    for _, entry in ipairs(compact) do
        for _ = 1, entry.count or 0 do
            local item = nil
            -- try string-based add first; fall back to factory-create then add
            pcall(function() item = inv:AddItem(entry.fullType) end)
            if not item then
                pcall(function()
                    if InventoryItemFactory and InventoryItemFactory.CreateItem then
                        local created = InventoryItemFactory.CreateItem(entry.fullType)
                        if created then
                            inv:AddItem(created)
                            item = created
                        end
                    end
                end)
            end
            if item then
                added = added + 1
                if sendAddItemToContainer then
                    pcall(sendAddItemToContainer, inv, item)
                end
            else
                print("[ModpackFestivalSpawn][Sister] AddItem failed for: " .. tostring(entry.fullType))
            end
        end
    end
    print("[ModpackFestivalSpawn][Sister] applyInventory: added=" .. added .. " of " .. #compact .. " types")

    pcall(function()
        if inv.setDrawDirty then
            inv:setDrawDirty(true)
        end
    end)
    pcall(function()
        if bandit.transmitModData then
            bandit:transmitModData()
        end
    end)
    -- re-equip the weapon the player had set before respawn; fall back to default on first spawn
    local equippedWeapon = ModpackFestivalSister.getEquippedWeaponSnapshot()
        or ModpackFestivalSister.DEFAULT_EQUIPPED_WEAPON
    if equippedWeapon then
        pcall(function()
            local items = inv.getItems and inv:getItems()
            if not items then return end
            for i = 0, items:size() - 1 do
                local it = items:get(i)
                if it and it.getFullType and it:getFullType() == equippedWeapon then
                    ModpackFestivalSister.equipInventoryItem(bandit, it, "primary")
                    break
                end
            end
        end)
    end

    ModpackFestivalSister.storeInventorySnapshot(compact, equippedWeapon)
    return added >= 0
end

function ModpackFestivalSister.enableFollowMode(bandit, player)
    if not ModpackFestivalSister.isSisterBandit(bandit) then
        return false
    end
    player = player or (getSpecificPlayer and getSpecificPlayer(0))
    local pid = getPlayerId(player)
    local brain = getBrain(bandit)
    local needsProgramSet = true
    if brain then
        brain.modpackFestivalSister = true
        brain.cid = ModpackFestivalSister.CLAN_ID
        brain.bid = ModpackFestivalSister.BANDIT_ID
        brain.weapons = brain.weapons or {}
        sanitizeBanditWeaponSlots(brain.weapons)
        brain.hostile = false
        brain.hostileP = false
        brain.loyal = true
        brain.master = pid or brain.master
        brain.pid = pid or brain.pid
        brain.program = brain.program or {}
        needsProgramSet = brain.program.name ~= ModpackFestivalSister.PROGRAM_NAME
        brain.program.name = ModpackFestivalSister.PROGRAM_NAME
        brain.program.stage = brain.program.stage or "Prepare"
        brain.programFallback = ModpackFestivalSister.PROGRAM_NAME
        ModpackFestivalSister.applyStoredAppearanceToBrain(brain)
        syncBrain(bandit, brain)
    end
    if needsProgramSet and Bandit and Bandit.SetProgram then
        pcall(Bandit.SetProgram, bandit, ModpackFestivalSister.PROGRAM_NAME, {})
    end
    if Bandit and Bandit.SetMaster and pid then
        pcall(Bandit.SetMaster, bandit, pid)
    end
    if Bandit and Bandit.SetHostileP then
        pcall(Bandit.SetHostileP, bandit, false)
    end
    if Bandit and Bandit.ApplyVisuals and brain then
        pcall(Bandit.ApplyVisuals, bandit, brain)
    end
    ModpackFestivalSister.applyStoredAppearanceToBandit(bandit, false)
    ModpackFestivalSister.protectSister(bandit)
    return true
end

function ModpackFestivalSister.requestSpawn(player)
    if not player or not sendClientCommand then
        return false
    end
    sendClientCommand(player, MOD_ID, "SisterSpawn", {})
    return true
end

function ModpackFestivalSister.requestCallOver(player)
    if not player or not sendClientCommand then
        return false
    end
    sendClientCommand(player, MOD_ID, "SisterCallOver", {})
    return true
end

print("[ModpackFestivalSpawn] sister companion helpers loaded")
