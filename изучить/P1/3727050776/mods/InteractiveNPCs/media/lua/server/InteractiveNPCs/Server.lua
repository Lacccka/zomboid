local Shared = require("InteractiveNPCs/Shared")
local Persistence = require("InteractiveNPCs/Persistence")
local WorldObjectUtils = require("InteractiveNPCs/WorldObjectUtils")
local AccessLevelUtils = require("ElyonLib/PlayerUtils/AccessLevelUtils")
local Logger = require("ElyonLib/Core/Logger"):new("InteractiveNPCs", "1.0.0")

local Server = {}
local MODULE = Shared.MODULE
local COMMANDS = Shared.COMMANDS

Server.npcs = {}
Server.avatarPresets = {}

local function loadNpcs()
	Server.npcs = Persistence.loadNpcs()
	Server.avatarPresets = Persistence.loadAvatarPresets()
	Logger:info("Loaded %d NPC definitions.", #Server.npcs)
	if #Server.npcs > 0 then
		Persistence.saveNpcs(Server.npcs)
	end
end

local function saveNpcs()
	local ok = Persistence.saveNpcs(Server.npcs)
	if ok then
		Logger:info("Saved %d NPC definitions.", #Server.npcs)
	else
		Logger:error("Failed to save NPC definitions.")
	end
	return ok
end

local function findNpcIndex(id)
	for i = 1, #Server.npcs do
		if Server.npcs[i].id == id then
			return i
		end
	end
	return nil
end

local function findPresetIndex(id)
	for i = 1, #Server.avatarPresets do
		if Server.avatarPresets[i].id == id then
			return i
		end
	end
	return nil
end

local function findPreset(id)
	local idx = findPresetIndex(id)
	return idx and Server.avatarPresets[idx] or nil
end

local function findNpcByObjectRef(ref)
	local key = Shared.objectKey(ref)
	if not key then
		return nil
	end
	for i = 1, #Server.npcs do
		local npc = Server.npcs[i]
		if npc.objectRef and npc.objectRef.key == key then
			return npc
		end
	end
	return nil
end

local function sendRegistry(player)
	sendServerCommand(player, MODULE, COMMANDS.RECEIVE_REGISTRY, { npcs = Shared.publicRegistry(Server.npcs) })
end

local function broadcastRegistry()
	sendServerCommand(MODULE, COMMANDS.NPCS_UPDATED, { npcs = Shared.publicRegistry(Server.npcs) })
end

local function sendAdminData(player)
	sendServerCommand(player, MODULE, COMMANDS.ADMIN_RECEIVE, { npcs = Server.npcs, avatarPresets = Server.avatarPresets })
end

local function deny(player, reason)
	sendServerCommand(player, MODULE, COMMANDS.INTERACTION_DENIED, { reason = reason or "denied" })
end

local requireAdmin

local function canInteract(player, npc)
	if not npc or npc.enabled == false or npc.visible == false then
		return false, "disabled"
	end
	local required = npc.settings and npc.settings.minimumAccessLevel or "None"
	if required ~= "None" and not AccessLevelUtils.isPlayerAtLeast(nil, required, player) then
		return false, "permission"
	end
	local dist = WorldObjectUtils.distanceToPlayer(npc.objectRef, player)
	local maxDist = tonumber(npc.settings and npc.settings.interactionDistance) or 2.5
	if dist > maxDist then
		return false, "range"
	end
	return true
end

local function handleRequestRegistry(player)
	sendRegistry(player)
end

local function handleRequestDialogue(player, args)
	if type(args) ~= "table" then
		return
	end
	local npc = args.id and Server.npcs[findNpcIndex(args.id) or -1] or findNpcByObjectRef(args.objectRef)
	local ok, reason = canInteract(player, npc)
	if not ok then
		deny(player, reason)
		return
	end
	local payloadNpc = npc
	if npc.avatar and npc.avatar.mode == Shared.AVATAR_MODE.PRESET then
		payloadNpc = Shared.makeNpc(npc)
		payloadNpc.avatar.preset = findPreset(npc.avatar.presetId)
	end
	sendServerCommand(player, MODULE, COMMANDS.RECEIVE_DIALOGUE, {
		npc = payloadNpc,
	})
end

local function handleAdminSavePreset(player, args)
	if not requireAdmin(player, COMMANDS.ADMIN_SAVE_PRESET) then
		return
	end
	local preset = Shared.normalizeAvatarPreset(args)
	local idx = findPresetIndex(preset.id)
	if idx then
		Server.avatarPresets[idx] = preset
	else
		Server.avatarPresets[#Server.avatarPresets + 1] = preset
	end
	Persistence.saveAvatarPresets(Server.avatarPresets)
	sendAdminData(player)
end

local function handleAdminDeletePreset(player, args)
	if not requireAdmin(player, COMMANDS.ADMIN_DELETE_PRESET) then
		return
	end
	if type(args) ~= "table" or type(args.id) ~= "string" then
		return
	end
	local idx = findPresetIndex(args.id)
	if not idx then
		return
	end
	table.remove(Server.avatarPresets, idx)
	for i = 1, #Server.npcs do
		local avatar = Server.npcs[i].avatar
		if avatar and avatar.presetId == args.id then
			avatar.presetId = ""
			if avatar.mode == Shared.AVATAR_MODE.PRESET then
				avatar.mode = Shared.AVATAR_MODE.NONE
			end
		end
	end
	Persistence.saveAvatarPresets(Server.avatarPresets)
	saveNpcs()
	broadcastRegistry()
	sendAdminData(player)
end

function requireAdmin(player, command)
	if AccessLevelUtils.hasAdminAccess(player) then
		return true
	end
	Logger:warning("Non-admin '%s' attempted %s", tostring(player and player:getUsername() or "?"), tostring(command))
	return false
end

local function handleAdminRequest(player)
	if not requireAdmin(player, COMMANDS.ADMIN_REQUEST) then
		return
	end
	sendAdminData(player)
end

local function handleAdminSave(player, args)
	if not requireAdmin(player, COMMANDS.ADMIN_SAVE) then
		return
	end
	local ok, err, npc = Shared.validateNpc(args)
	if not ok then
		Logger:warning("AdminSave rejected: %s", tostring(err))
		return
	end
	local idx = findNpcIndex(npc.id)
	if idx then
		npc.createdAtTs = Server.npcs[idx].createdAtTs or npc.createdAtTs
		Server.npcs[idx] = npc
	else
		Server.npcs[#Server.npcs + 1] = npc
	end
	if npc.objectRef then
		WorldObjectUtils.markObject(npc.objectRef, npc.id)
	end
	saveNpcs()
	broadcastRegistry()
	sendAdminData(player)
end

local function handleAdminDelete(player, args)
	if not requireAdmin(player, COMMANDS.ADMIN_DELETE) then
		return
	end
	if type(args) ~= "table" or type(args.id) ~= "string" then
		return
	end
	local idx = findNpcIndex(args.id)
	if not idx then
		return
	end
	local npc = Server.npcs[idx]
	if npc.objectRef then
		WorldObjectUtils.unmarkObject(npc.objectRef)
	end
	table.remove(Server.npcs, idx)
	saveNpcs()
	broadcastRegistry()
	sendAdminData(player)
end

local function handleAdminMarkObject(player, args)
	if not requireAdmin(player, COMMANDS.ADMIN_MARK_OBJECT) then
		return
	end
	if type(args) ~= "table" or type(args.objectRef) ~= "table" then
		return
	end
	local existing = findNpcByObjectRef(args.objectRef)
	local npc = existing or Shared.makeNpc({
		name = args.name or "Interactive NPC",
		objectRef = args.objectRef,
		avatar = { mode = args.avatarMode or Shared.AVATAR_MODE.NONE },
		greeting = "Hello.",
	})
	npc.objectRef = args.objectRef
	npc.objectRef.key = Shared.objectKey(args.objectRef)
	WorldObjectUtils.markObject(npc.objectRef, npc.id)
	if not existing then
		Server.npcs[#Server.npcs + 1] = npc
	end
	saveNpcs()
	broadcastRegistry()
	sendAdminData(player)
end

local function handleAdminUnmarkObject(player, args)
	if not requireAdmin(player, COMMANDS.ADMIN_UNMARK_OBJECT) then
		return
	end
	if type(args) ~= "table" or type(args.id) ~= "string" then
		return
	end
	local idx = findNpcIndex(args.id)
	if not idx then
		return
	end
	if Server.npcs[idx].objectRef then
		WorldObjectUtils.unmarkObject(Server.npcs[idx].objectRef)
	end
	Server.npcs[idx].objectRef = nil
	saveNpcs()
	broadcastRegistry()
	sendAdminData(player)
end

function Server.onClientCommand(module, command, player, args)
	if module ~= MODULE then
		return
	end
	if command == COMMANDS.REQUEST_REGISTRY then
		handleRequestRegistry(player)
	elseif command == COMMANDS.REQUEST_DIALOGUE then
		handleRequestDialogue(player, args)
	elseif command == COMMANDS.ADMIN_REQUEST then
		handleAdminRequest(player)
	elseif command == COMMANDS.ADMIN_SAVE then
		handleAdminSave(player, args)
	elseif command == COMMANDS.ADMIN_DELETE then
		handleAdminDelete(player, args)
	elseif command == COMMANDS.ADMIN_MARK_OBJECT then
		handleAdminMarkObject(player, args)
	elseif command == COMMANDS.ADMIN_UNMARK_OBJECT then
		handleAdminUnmarkObject(player, args)
	elseif command == COMMANDS.ADMIN_SAVE_PRESET then
		handleAdminSavePreset(player, args)
	elseif command == COMMANDS.ADMIN_DELETE_PRESET then
		handleAdminDeletePreset(player, args)
	end
end

function Server.init()
	loadNpcs()
	Events.OnClientCommand.Add(Server.onClientCommand)
	Logger:info("Server initialized.")
end

if isServer() then
	Events.OnServerStarted.Add(Server.init)
end

return Server
