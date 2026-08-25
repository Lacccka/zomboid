local Shared = require("InteractiveNPCs/Shared")

local AvatarUtils = {}

local function trim(value)
	return tostring(value or ""):match("^%s*(.-)%s*$") or ""
end

--- locations to strip before replaying pinned clothing (deterministic outfit).
local BODY_LOCATIONS_CLEAR_ORDER = {
	"Hat",
	"FullHat",
	"Mask",
	"MaskEyes",
	"MaskFull",
	"Eyes",
	"Neck",
	"Scarf",
	"TankTop",
	"Tshirt",
	"BathRobe",
	"Shirt",
	"Sweater",
	"Jacket",
	"Jacket_Bulky",
	"TorsoExtra",
	"TorsoExtraVest",
	"Hands",
	"Pants",
	"ShortsShort",
	"Skirt",
	"Dress",
	"Boilersuit",
	"FullSuit",
	"Shoes",
	"Socks",
	"Underwear",
	"UnderwearBottom",
	"UnderwearExtra1",
	"Back",
}

local function defaultHairModel(isFemale)
	if getAllHairStyles then
		local hs = getAllHairStyles(isFemale == true)
		if hs and hs.size and hs:size() > 0 then
			local id = hs:get(0)
			if id and tostring(id) ~= "" then
				return tostring(id)
			end
		end
	end
	return isFemale and "Long" or "Short"
end

local function clothingBodyLocation(item)
	if not item or not item.getBodyLocation then
		return nil
	end
	local loc = item:getBodyLocation()
	if not loc then
		return nil
	end
	if type(loc) == "string" then
		return loc
	end
	if loc.getType then
		local t = loc:getType()
		if t and t ~= "" then
			return t
		end
	end
	return tostring(loc)
end

local function registerWorn(byLoc, item)
	if not item then
		return
	end
	if item.isDestroyed and item:isDestroyed() then
		return
	end
	local loc = clothingBodyLocation(item)
	if not loc or loc == "" then
		return
	end
	local ft = item.getFullType and item:getFullType()
	if not ft or ft == "" then
		return
	end
	if item.IsClothing then
		if not item:IsClothing() then
			return
		end
	end
	byLoc[trim(tostring(loc))] = trim(tostring(ft))
end

--- collect clothing currently on a SurvivorDesc after dressInNamedOutfit (for freezing outfit variants).
function AvatarUtils.collectWornClothingSlots(desc)
	local byLoc = {}

	if desc and desc.getWearables then
		local w = desc:getWearables()
		if w and w.size then
			for i = 0, w:size() - 1 do
				registerWorn(byLoc, w:get(i))
			end
		end
	end

	if desc and desc.getWornItems then
		local w = desc:getWornItems()
		if w and w.size then
			for i = 0, w:size() - 1 do
				registerWorn(byLoc, w:get(i))
			end
		end
	end

	if desc and desc.getInventory then
		local inv = desc:getInventory()
		if inv then
			local items = inv.getItems and inv:getItems()
			if items and items.size then
				for i = 0, items:size() - 1 do
					registerWorn(byLoc, items:get(i))
				end
			end
			local itemsAlt = inv.getItemsFromCategory and inv:getItemsFromCategory("Clothing")
			if itemsAlt and itemsAlt.size then
				for i = 0, itemsAlt:size() - 1 do
					registerWorn(byLoc, itemsAlt:get(i))
				end
			end
		end
	end

	local keys = Shared.tableKeys(byLoc)
	table.sort(keys)
	local slots = {}
	for i = 1, #keys do
		local loc = keys[i]
		local ft = byLoc[loc]
		if ft and ft ~= "" then
			slots[#slots + 1] = { bodyLocation = loc, fullType = ft }
		end
	end
	return slots
end

local function stripWornSlots(desc)
	if not desc or not desc.setWornItem then
		return
	end
	for i = 1, #BODY_LOCATIONS_CLEAR_ORDER do
		desc:setWornItem(BODY_LOCATIONS_CLEAR_ORDER[i], nil)
	end
end

function AvatarUtils.applyClothingSlots(desc, slots)
	if not desc or not desc.setWornItem or type(slots) ~= "table" then
		return
	end
	stripWornSlots(desc)
	for i = 1, #slots do
		local slot = slots[i]
		if
			slot
			and slot.bodyLocation
			and slot.fullType
			and InventoryItemFactory
			and InventoryItemFactory.CreateItem
		then
			desc:setWornItem(slot.bodyLocation, nil)
			local item = InventoryItemFactory.CreateItem(slot.fullType)
			if item then
				desc:setWornItem(slot.bodyLocation, item)
			end
		end
	end
end

local function applyPresetClothing(desc, preset)
	if not desc or type(preset) ~= "table" then
		return
	end
	if preset.baseOutfit and preset.baseOutfit ~= "" and desc.dressInNamedOutfit then
		desc:dressInNamedOutfit(preset.baseOutfit)
	end
	local slots = preset.slots or {}
	for i = 1, #slots do
		local slot = slots[i]
		if
			slot
			and slot.bodyLocation
			and slot.fullType
			and InventoryItemFactory
			and InventoryItemFactory.CreateItem
			and desc.setWornItem
		then
			desc:setWornItem(slot.bodyLocation, nil)
			local item = InventoryItemFactory.CreateItem(slot.fullType)
			if item then
				desc:setWornItem(slot.bodyLocation, item)
			end
		end
	end
end

function AvatarUtils.applyMakeupFromList(desc, fullTypes)
	if not desc or not desc.setWornItem or type(fullTypes) ~= "table" then
		return
	end
	for i = 1, #fullTypes do
		local ft = fullTypes[i]
		if ft and ft ~= "" and InventoryItemFactory and InventoryItemFactory.CreateItem then
			local item = InventoryItemFactory.CreateItem(ft)
			if item and item.getBodyLocation then
				local loc = item:getBodyLocation()
				if loc then
					desc:setWornItem(loc, item)
				end
			end
		end
	end
end

function AvatarUtils.applyHumanVisual(desc, app, isFemale)
	if not desc or not desc.getHumanVisual then
		return
	end
	local vis = desc:getHumanVisual()
	if not vis then
		return
	end
	app = type(app) == "table" and app or {}
	isFemale = isFemale == true

	local skin = tonumber(app.skinTextureIndex)
	if skin == nil then
		skin = 0
	end
	vis:setSkinTextureIndex(skin)

	local hair = app.hairModel
	if hair == nil or hair == "" then
		hair = defaultHairModel(isFemale)
	end
	vis:setHairModel(hair)

	if isFemale then
		vis:setBeardModel(nil)
	else
		local beard = app.beardModel
		if beard == nil or beard == "" then
			vis:setBeardModel("")
		else
			vis:setBeardModel(beard)
		end
	end

	local function applyRgb(r, g, b, applyFn)
		if r == nil or g == nil or b == nil then
			return
		end
		local c = ImmutableColor.new(r, g, b)
		applyFn(c)
	end

	applyRgb(app.hairR, app.hairG, app.hairB, function(c)
		vis:setHairColor(c)
		if vis.setNaturalHairColor then
			vis:setNaturalHairColor(c)
		end
	end)

	if not isFemale then
		applyRgb(app.beardR, app.beardG, app.beardB, function(c)
			vis:setBeardColor(c)
			if vis.setNaturalBeardColor then
				vis:setNaturalBeardColor(c)
			end
		end)
	end
end

--- after save in Admin UI: pin one concrete clothing roll for this outfit name (stable across dialogue opens).
function AvatarUtils.ensureOutfitWearSnapshot(avatar)
	if type(avatar) ~= "table" then
		return
	end
	if avatar.mode ~= Shared.AVATAR_MODE.OUTFIT then
		return
	end
	local outfitName = avatar.outfit or ""
	outfitName = trim(tostring(outfitName))
	if outfitName == "" then
		avatar.outfitWear = nil
		return
	end
	if #Shared.normalizeOutfitWear(avatar.outfitWear) > 0 then
		return
	end
	if not SurvivorFactory or not SurvivorFactory.CreateSurvivor then
		return
	end
	local desc = SurvivorFactory.CreateSurvivor()
	if not desc then
		return
	end
	if desc.setFemale then
		desc:setFemale(avatar.female == true)
	end
	if desc.dressInNamedOutfit then
		desc:dressInNamedOutfit(outfitName)
	end
	local collected = AvatarUtils.collectWornClothingSlots(desc)
	if #collected < 1 then
		avatar.outfitWear = nil
		return
	end
	avatar.outfitWear = Shared.normalizeOutfitWear(collected)
end

function AvatarUtils.makeSurvivorDesc(avatar)
	if not SurvivorFactory or not SurvivorFactory.CreateSurvivor then
		return nil
	end
	local desc = SurvivorFactory.CreateSurvivor()
	if not desc then
		return nil
	end

	avatar = type(avatar) == "table" and avatar or {}
	local isFemale = avatar.female == true
	if avatar.mode == Shared.AVATAR_MODE.PRESET and type(avatar.preset) == "table" then
		isFemale = avatar.preset.female == true
	end
	if desc.setFemale then
		desc:setFemale(isFemale)
	end

	local presetApp = {}
	local npcApp = Shared.normalizeAppearance(avatar.appearance)

	if avatar.mode == Shared.AVATAR_MODE.PRESET and type(avatar.preset) == "table" then
		presetApp = Shared.normalizeAppearance(avatar.preset.appearance)
		applyPresetClothing(desc, avatar.preset)
	elseif avatar.mode == Shared.AVATAR_MODE.OUTFIT then
		local wear = Shared.normalizeOutfitWear(avatar.outfitWear)
		local outfitName = trim(tostring(avatar.outfit or ""))
		if #wear > 0 then
			AvatarUtils.applyClothingSlots(desc, wear)
		elseif outfitName ~= "" and desc.dressInNamedOutfit then
			desc:dressInNamedOutfit(outfitName)
		end
	end

	local merged = Shared.mergeAppearances(presetApp, npcApp)
	AvatarUtils.applyMakeupFromList(desc, merged.makeupFullTypes)
	AvatarUtils.applyHumanVisual(desc, merged, isFemale)

	return desc
end

return AvatarUtils
