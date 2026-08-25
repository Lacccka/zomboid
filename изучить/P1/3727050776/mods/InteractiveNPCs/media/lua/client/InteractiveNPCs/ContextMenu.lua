local Shared = require("InteractiveNPCs/Shared")
local Client = require("InteractiveNPCs/Client")
local AccessLevelUtils = require("ElyonLib/PlayerUtils/AccessLevelUtils")

local ContextMenu = {}

local function addCandidate(candidates, seen, object)
	if not object or seen[object] or not object.getSquare then
		return
	end
	local square = object:getSquare()
	if not square then
		return
	end
	seen[object] = true
	candidates[#candidates + 1] = object
end

local function collectCandidates(worldobjects)
	local candidates = {}
	local seen = {}

	local wo = worldobjects or {}
	for i = 1, #wo do
		local object = wo[i]
		addCandidate(candidates, seen, object)

		local square = object and object.getSquare and object:getSquare()
		local objects = square and square.getObjects and square:getObjects()
		if objects then
			for oj = 0, objects:size() - 1 do
				addCandidate(candidates, seen, objects:get(oj))
			end
		end
	end

	return candidates
end

local function getSpriteName(object)
	local sprite = object and object.getSprite and object:getSprite()
	if sprite and sprite.getName then
		local spriteName = sprite:getName()
		if spriteName and spriteName ~= "" then
			return spriteName
		end
	end
	if object and object.getTextureName then
		local textureName = object:getTextureName()
		if textureName and textureName ~= "" then
			return textureName
		end
	end
	return nil
end

local function getMoveableDisplayName(object)
	local sprite = object and object.getSprite and object:getSprite()
	local props = sprite and sprite.getProperties and sprite:getProperties()
	if props and props.Is and props.Val and props:Is("CustomName") then
		local name = props:Val("CustomName")
		if props:Is("GroupName") then
			name = props:Val("GroupName") .. " " .. name
		end
		if Translator and Translator.getMoveableDisplayName then
			local translated = Translator.getMoveableDisplayName(name)
			if translated and translated ~= "" then
				return translated
			end
		end
		return name
	end
	return nil
end

local function getObjectName(object, index)
	local displayName = getMoveableDisplayName(object)
	if displayName and displayName ~= "" then
		return displayName
	end
	if object and object.getName then
		local name = object:getName()
		if name and name ~= "" then
			return name
		end
	end
	local spriteName = getSpriteName(object)
	if spriteName then
		return spriteName
	end
	return getText("IGUI_INPC_ObjectFallback", tostring(index or "?"))
end

local function getObjectMenuLabel(object, index)
	local label = getObjectName(object, index)
	local ref = Shared.makeObjectRef(object)
	if ref and ref.objectIndex ~= nil then
		return getText("IGUI_INPC_ObjectWithSlot", label, tostring((tonumber(ref.objectIndex) or 0) + 1))
	end
	return label
end

local function getObjectIconTexture(object)
	local spriteName = getSpriteName(object)
	if not spriteName or not getTexture then
		return nil
	end
	return getTexture(spriteName)
end

local function makeObjectTooltip(option, object, label, npc)
	if not option or not object or not ISWorldObjectContextMenu or not ISWorldObjectContextMenu.addToolTip then
		return
	end

	local tooltip = ISWorldObjectContextMenu:addToolTip()
	tooltip:setName(label)

	local square = object:getSquare()
	local spriteName = getSpriteName(object)
	local details = {}
	if npc then
		details[#details + 1] = "<RGB:0.95,0.76,0.33> " .. getText("IGUI_INPC_TooltipNpc", npc.name or getText("IGUI_INPC_DefaultName"))
	end
	if square then
		details[#details + 1] = string.format(
			"<RGB:0.85,0.85,0.85> %s",
			getText("IGUI_INPC_TooltipSquare", square:getX(), square:getY(), square:getZ())
		)
	end
	local ref = Shared.makeObjectRef(object)
	if ref and ref.objectIndex ~= nil then
		details[#details + 1] = string.format(
			"<RGB:0.85,0.85,0.85> %s",
			getText("IGUI_INPC_TooltipObjectSlot", (tonumber(ref.objectIndex) or 0) + 1)
		)
	end
	if spriteName then
		details[#details + 1] = string.format("<RGB:0.85,0.85,0.85> %s", getText("IGUI_INPC_TooltipSprite", spriteName))
		if tooltip.setTexture then
			tooltip:setTexture(spriteName)
		end
	end

	tooltip.description = table.concat(details, " <LINE> ")
	option.toolTip = tooltip

	local texture = getObjectIconTexture(object)
	if texture then
		option.iconTexture = texture
	end
end

local function talkMenuLabel(entry, disambiguateCoords)
	local name = entry.npc.name or getText("IGUI_INPC_DefaultName")
	if disambiguateCoords then
		local sq = entry.object:getSquare()
		if sq then
			return getText("IGUI_INPC_TalkToNpcAt", name, sq:getX(), sq:getY(), sq:getZ())
		end
	end
	return getText("IGUI_INPC_TalkToNpc", name)
end

local function makeNpcTalkTooltip(option, object, npc)
	if not option or not object or not ISWorldObjectContextMenu or not ISWorldObjectContextMenu.addToolTip then
		return
	end
	local tooltip = ISWorldObjectContextMenu:addToolTip()
	tooltip:setName(npc.name or getText("IGUI_INPC_DefaultName"))
	local square = object:getSquare()
	local spriteName = getSpriteName(object)
	local details = {}
	if square then
		details[#details + 1] = string.format(
			"<RGB:0.85,0.85,0.85> %s",
			getText("IGUI_INPC_TooltipSquare", square:getX(), square:getY(), square:getZ())
		)
	end
	if spriteName then
		details[#details + 1] = string.format("<RGB:0.85,0.85,0.85> %s", getText("IGUI_INPC_TooltipSprite", spriteName))
		if tooltip.setTexture then
			tooltip:setTexture(spriteName)
		end
	end
	tooltip.description = table.concat(details, " <LINE> ")
	option.toolTip = tooltip
	local texture = getObjectIconTexture(object)
	if texture then
		option.iconTexture = texture
	end
end

local function addObjectSubMenu(parentMenu, parentOption)
	local subMenu = parentMenu:getNew(parentMenu)
	parentMenu:addSubMenu(parentOption, subMenu)
	return subMenu
end

local function getNpcEntries(candidates)
	local entries = {}
	for index = 1, #candidates do
		local object = candidates[index]
		local npc, ref = Client.findRegistryByObject(object)
		if npc then
			entries[#entries + 1] = {
				index = index,
				object = object,
				npc = npc,
				ref = ref,
				label = getObjectMenuLabel(object, index),
			}
		end
	end
	return entries
end

local function onTalk(npc, ref)
	Client.requestDialogue(npc or ref)
end

local function onCreateNpc(objectRef)
	local AdminUI = require("InteractiveNPCs/UI/AdminUI")
	AdminUI.open(objectRef)
end

local function onEditNpc(npc)
	local AdminUI = require("InteractiveNPCs/UI/AdminUI")
	AdminUI.open(npc and npc.objectRef)
end

local function onUnmark(npc)
	if npc then
		Client.adminUnmarkObject(npc.id)
	end
end

function ContextMenu.onFillWorldObjectContextMenu(playerNum, context, worldobjects, test)
	if test and ISWorldObjectContextMenu.Test then
		return true
	end
	if test then
		return ISWorldObjectContextMenu.setTest()
	end

	local playerObj = getSpecificPlayer(playerNum)
	if not playerObj or playerObj:getVehicle() then
		return
	end

	local candidates = collectCandidates(worldobjects)
	if #candidates == 0 then
		return
	end

	local npcEntries = getNpcEntries(candidates)
	local visibleNpcEntries = {}
	for i = 1, #npcEntries do
		local entry = npcEntries[i]
		if entry.npc.enabled and entry.npc.visible then
			visibleNpcEntries[#visibleNpcEntries + 1] = entry
		end
	end

	if #visibleNpcEntries == 1 then
		local entry = visibleNpcEntries[1]
		local option = context:addOptionOnTop(talkMenuLabel(entry, false), entry.npc, onTalk, entry.ref)
		makeNpcTalkTooltip(option, entry.object, entry.npc)
	elseif #visibleNpcEntries > 1 then
		local talkOption = context:addOptionOnTop(getText("IGUI_INPC_Talk"), nil, nil)
		local talkMenu = addObjectSubMenu(context, talkOption)
		for j = 1, #visibleNpcEntries do
			local entry = visibleNpcEntries[j]
			local option = talkMenu:addOption(talkMenuLabel(entry, true), entry.npc, onTalk, entry.ref)
			makeNpcTalkTooltip(option, entry.object, entry.npc)
		end
	end

	if AccessLevelUtils.hasAdminAccess(playerObj) then
		local adminOption = context:addOptionOnTop(getText("IGUI_INPC_AdminMenu"), worldobjects, nil)
		local subMenu = addObjectSubMenu(context, adminOption)

		if #npcEntries > 0 then
			local editOption = subMenu:addOption(getText("IGUI_INPC_EditNpc"), nil, nil)
			local editMenu = addObjectSubMenu(subMenu, editOption)
			local unmarkOption = subMenu:addOption(getText("IGUI_INPC_UnmarkObject"), nil, nil)
			local unmarkMenu = addObjectSubMenu(subMenu, unmarkOption)

			for j = 1, #npcEntries do
				local entry = npcEntries[j]
				local editLabel = getText("IGUI_INPC_EditNpcObject", entry.npc.name or getText("IGUI_INPC_DefaultName"), entry.label)
				local editEntryOption = editMenu:addOption(editLabel, entry.npc, onEditNpc)
				makeObjectTooltip(editEntryOption, entry.object, entry.label, entry.npc)

				local unmarkLabel = getText("IGUI_INPC_UnmarkObjectNamed", entry.label)
				local unmarkEntryOption = unmarkMenu:addOption(unmarkLabel, entry.npc, onUnmark)
				makeObjectTooltip(unmarkEntryOption, entry.object, entry.label, entry.npc)
			end
		end

		local createOption = subMenu:addOption(getText("IGUI_INPC_MarkObject"), nil, nil)
		local createMenu = addObjectSubMenu(subMenu, createOption)
		for index = 1, #candidates do
			local object = candidates[index]
			local npc, ref = Client.findRegistryByObject(object)
			local label = getObjectMenuLabel(object, index)
			if npc then
				local option = createMenu:addOption(
					getText("IGUI_INPC_AlreadyNpcObject", npc.name or getText("IGUI_INPC_DefaultName"), label),
					npc,
					onEditNpc
				)
				makeObjectTooltip(option, object, label, npc)
			else
				local option = createMenu:addOption(label, ref or Shared.makeObjectRef(object), onCreateNpc)
				makeObjectTooltip(option, object, label, nil)
			end
		end
	end
end

Events.OnFillWorldObjectContextMenu.Add(ContextMenu.onFillWorldObjectContextMenu)

return ContextMenu
