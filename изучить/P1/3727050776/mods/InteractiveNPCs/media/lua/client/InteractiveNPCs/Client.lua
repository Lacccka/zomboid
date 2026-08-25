local Shared = require("InteractiveNPCs/Shared")
local Persistence = require("InteractiveNPCs/Persistence")
local WorldObjectUtils = require("InteractiveNPCs/WorldObjectUtils")
local AccessLevelUtils = require("ElyonLib/PlayerUtils/AccessLevelUtils")

local Client = {}
Client.registry = {}
Client.adminNpcs = {}
Client.avatarPresets = {}
Client.uiRef = nil
Client.dialogueUiRef = nil
Client.avatarPresetUiRef = nil
Client.pendingObjectRef = nil

local MODULE = Shared.MODULE
local COMMANDS = Shared.COMMANDS

local function offline()
	return AccessLevelUtils.isSinglePlayer()
end

local function playerObj()
	return getPlayer and getPlayer() or getSpecificPlayer and getSpecificPlayer(0) or nil
end

local function send(command, args)
	sendClientCommand(MODULE, command, args or {})
end

local function loadOffline()
	return Persistence.loadNpcs()
end

local function loadOfflinePresets()
	return Persistence.loadAvatarPresets()
end

local function saveOffline(npcs)
	return Persistence.saveNpcs(npcs)
end

local function saveOfflinePresets(presets)
	return Persistence.saveAvatarPresets(presets)
end

local function findIndex(npcs, id)
	for i = 1, #(npcs or {}) do
		if npcs[i].id == id then
			return i
		end
	end
	return nil
end

local function findByObjectRef(npcs, ref)
	local key = Shared.objectKey(ref)
	for i = 1, #(npcs or {}) do
		local npc = npcs[i]
		if npc.objectRef and npc.objectRef.key == key then
			return npc
		end
	end
	return nil
end

local function refreshAdminUi()
	if Client.uiRef and Client.uiRef.onAdminNpcsReceived then
		Client.uiRef:onAdminNpcsReceived(Client.adminNpcs)
	end
	if Client.avatarPresetUiRef and Client.avatarPresetUiRef.refreshPresetList then
		Client.avatarPresetUiRef:refreshPresetList()
	end
end

local function refreshRegistryUi()
	if Client.uiRef and Client.uiRef.onRegistryReceived then
		Client.uiRef:onRegistryReceived(Client.registry)
	end
end

local function setOfflineCopies(all)
	Client.adminNpcs = all
	Client.avatarPresets = loadOfflinePresets()
	Client.registry = Shared.publicRegistry(all)
	refreshAdminUi()
	refreshRegistryUi()
end

function Client.requestRegistry()
	if offline() then
		setOfflineCopies(loadOffline())
		return
	end
	send(COMMANDS.REQUEST_REGISTRY)
end

function Client.requestAdmin()
	if offline() then
		setOfflineCopies(loadOffline())
		return
	end
	send(COMMANDS.ADMIN_REQUEST)
end

function Client.requestDialogue(npcOrRef)
	if offline() then
		local all = loadOffline()
		local npc = type(npcOrRef) == "table" and npcOrRef.id and all[findIndex(all, npcOrRef.id) or -1]
			or findByObjectRef(all, npcOrRef)
		if not npc then
			return
		end
		local p = playerObj()
		local dist = WorldObjectUtils.distanceToPlayer(npc.objectRef, p)
		if dist > (tonumber(npc.settings and npc.settings.interactionDistance) or 2.5) then
			Client.onInteractionDenied({ reason = "range" })
			return
		end
		if npc.avatar and npc.avatar.mode == Shared.AVATAR_MODE.PRESET then
			for i = 1, #Client.avatarPresets do
				if Client.avatarPresets[i].id == npc.avatar.presetId then
					npc.avatar.preset = Client.avatarPresets[i]
					break
				end
			end
		end
		Client.onDialogueReceived({ npc = npc })
		return
	end
	if type(npcOrRef) == "table" and npcOrRef.id then
		send(COMMANDS.REQUEST_DIALOGUE, { id = npcOrRef.id })
	else
		send(COMMANDS.REQUEST_DIALOGUE, { objectRef = npcOrRef })
	end
end

function Client.adminSave(npc)
	if offline() then
		local ok, err, normalized = Shared.validateNpc(npc)
		if not ok then
			return false, err
		end
		local all = loadOffline()
		local idx = findIndex(all, normalized.id)
		if idx then
			normalized.createdAtTs = all[idx].createdAtTs or normalized.createdAtTs
			all[idx] = normalized
		else
			all[#all + 1] = normalized
		end
		if normalized.objectRef then
			WorldObjectUtils.markObject(normalized.objectRef, normalized.id)
		end
		if saveOffline(all) then
			setOfflineCopies(all)
		end
		return true
	end
	send(COMMANDS.ADMIN_SAVE, npc)
	return true
end

function Client.adminDelete(id)
	if offline() then
		local all = loadOffline()
		local idx = findIndex(all, id)
		if idx then
			if all[idx].objectRef then
				WorldObjectUtils.unmarkObject(all[idx].objectRef)
			end
			table.remove(all, idx)
			if saveOffline(all) then
				setOfflineCopies(all)
			end
		end
		return
	end
	send(COMMANDS.ADMIN_DELETE, { id = id })
end

function Client.adminMarkObject(objectRef)
	Client.pendingObjectRef = objectRef
	if offline() then
		local all = loadOffline()
		local existing = findByObjectRef(all, objectRef)
		if not existing then
			all[#all + 1] = Shared.makeNpc({ objectRef = objectRef, name = "Interactive NPC", avatar = { mode = Shared.AVATAR_MODE.NONE } })
			WorldObjectUtils.markObject(objectRef, all[#all].id)
		end
		if saveOffline(all) then
			setOfflineCopies(all)
		end
		return
	end
	send(COMMANDS.ADMIN_MARK_OBJECT, { objectRef = objectRef, avatarMode = Shared.AVATAR_MODE.NONE })
end

function Client.adminUnmarkObject(id)
	if offline() then
		local all = loadOffline()
		local idx = findIndex(all, id)
		if idx then
			WorldObjectUtils.unmarkObject(all[idx].objectRef)
			all[idx].objectRef = nil
			if saveOffline(all) then
				setOfflineCopies(all)
			end
		end
		return
	end
	send(COMMANDS.ADMIN_UNMARK_OBJECT, { id = id })
end

function Client.adminSaveAvatarPreset(preset)
	if offline() then
		local normalized = Shared.normalizeAvatarPreset(preset)
		local presets = loadOfflinePresets()
		local idx = findIndex(presets, normalized.id)
		if idx then
			presets[idx] = normalized
		else
			presets[#presets + 1] = normalized
		end
		if saveOfflinePresets(presets) then
			Client.avatarPresets = presets
			refreshAdminUi()
		end
		return
	end
	send(COMMANDS.ADMIN_SAVE_PRESET, preset)
end

function Client.adminDeleteAvatarPreset(id)
	if offline() then
		local presets = loadOfflinePresets()
		local idx = findIndex(presets, id)
		if idx then
			table.remove(presets, idx)
			saveOfflinePresets(presets)
		end
		local all = loadOffline()
		for i = 1, #all do
			local avatar = all[i].avatar
			if avatar and avatar.presetId == id then
				avatar.presetId = ""
				if avatar.mode == Shared.AVATAR_MODE.PRESET then
					avatar.mode = Shared.AVATAR_MODE.NONE
				end
			end
		end
		saveOffline(all)
		Client.avatarPresets = presets
		setOfflineCopies(all)
		return
	end
	send(COMMANDS.ADMIN_DELETE_PRESET, { id = id })
end

function Client.findRegistryByObject(object)
	local ref = Shared.makeObjectRef(object)
	local key = Shared.objectKey(ref)
	for i = 1, #(Client.registry or {}) do
		local npc = Client.registry[i]
		if npc.objectRef and npc.objectRef.key == key then
			return npc, ref
		end
	end
	if object and object.getModData then
		local id = object:getModData()[Shared.OBJECT_MODDATA_KEY]
		if id then
			for i = 1, #(Client.registry or {}) do
				if Client.registry[i].id == id then
					return Client.registry[i], ref
				end
			end
		end
	end
	return nil, ref
end

function Client.onDialogueReceived(args)
	if type(args) ~= "table" or type(args.npc) ~= "table" then
		return
	end
	local DialogueUI = require("InteractiveNPCs/UI/DialogueUI")
	DialogueUI.open(args.npc)
end

function Client.onInteractionDenied(args)
	local reason = args and args.reason
	local msg = reason == "range" and getText("IGUI_INPC_OutOfRange") or getText("IGUI_INPC_Denied")
	local modal = ISModalDialog:new(0, 0, 300, 80, msg, false, nil, nil)
	modal:initialise()
	modal:addToUIManager()
end

local function onServerCommand(module, command, args)
	if module ~= MODULE then
		return
	end
	if command == COMMANDS.RECEIVE_REGISTRY or command == COMMANDS.NPCS_UPDATED then
		Client.registry = type(args) == "table" and type(args.npcs) == "table" and args.npcs or {}
		refreshRegistryUi()
	elseif command == COMMANDS.ADMIN_RECEIVE then
		Client.adminNpcs = type(args) == "table" and type(args.npcs) == "table" and args.npcs or {}
		Client.avatarPresets = type(args) == "table" and type(args.avatarPresets) == "table" and args.avatarPresets or {}
		refreshAdminUi()
	elseif command == COMMANDS.RECEIVE_DIALOGUE then
		Client.onDialogueReceived(args)
	elseif command == COMMANDS.INTERACTION_DENIED then
		Client.onInteractionDenied(args)
	end
end

local function onFirstTick()
	Client.requestRegistry()
	Events.OnTick.Remove(onFirstTick)
end

if not isServer() then
	Events.OnServerCommand.Add(onServerCommand)
	Events.OnTick.Add(onFirstTick)
end

return Client
