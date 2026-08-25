local AccessLevelUtils = require("ElyonLib/PlayerUtils/AccessLevelUtils")

local Shared = {}

Shared.VERSION = 1
Shared.MOD_ID = "InteractiveNPCs"
Shared.MODULE = "InteractiveNPCs"
Shared.DATA_DIR = "InteractiveNPCs"
Shared.INDEX_FILE = Shared.DATA_DIR .. "/index.json"
Shared.PRESETS_FILE = Shared.DATA_DIR .. "/avatar-presets.json"
Shared.OBJECT_MODDATA_KEY = "InteractiveNPCs_id"

Shared.COMMANDS = {
	REQUEST_REGISTRY = "RequestRegistry",
	RECEIVE_REGISTRY = "ReceiveRegistry",
	REQUEST_DIALOGUE = "RequestDialogue",
	RECEIVE_DIALOGUE = "ReceiveDialogue",
	INTERACTION_DENIED = "InteractionDenied",
	ADMIN_REQUEST = "AdminRequest",
	ADMIN_RECEIVE = "AdminReceive",
	ADMIN_SAVE = "AdminSave",
	ADMIN_DELETE = "AdminDelete",
	ADMIN_MARK_OBJECT = "AdminMarkObject",
	ADMIN_UNMARK_OBJECT = "AdminUnmarkObject",
	ADMIN_SAVE_PRESET = "AdminSavePreset",
	ADMIN_DELETE_PRESET = "AdminDeletePreset",
	NPCS_UPDATED = "NpcsUpdated",
}

Shared.LIMITS = {
	NAME_MAX = 64,
	TEXT_MAX = 4096,
	NODE_MAX = 256,
	RESPONSE_MAX = 12,
	PORTRAIT_MAX = 160,
	SOUND_MAX = 96,
	CONDITION_MAX = 128,
	MAKEUP_MAX = 16,
	OUTFIT_WEAR_MAX = 64,
}

Shared.AVATAR_MODE = {
	NONE = "none",
	PORTRAIT = "portrait",
	OUTFIT = "outfit",
	PRESET = "preset",
}

--- how `avatar.portrait` is interpreted when mode is Portrait (UI texture path vs tile/item sprite name).
Shared.PORTRAIT_SOURCE = {
	UI_IMAGE = "ui",
	SPRITE = "sprite",
}

local function trim(value)
	return tostring(value or ""):match("^%s*(.-)%s*$") or ""
end

function Shared.tableKeys(t)
	local ks = {}
	if type(t) ~= "table" then
		return ks
	end
	for k in pairs(t) do
		ks[#ks + 1] = k
	end
	table.sort(ks, function(a, b)
		return tostring(a) < tostring(b)
	end)
	return ks
end

local function timestamp()
	return string.format("%.0f", os.time())
end

function Shared.nowUnix()
	return timestamp()
end

function Shared.coerceStoredString(value, allowEmpty)
	local v = value
	if v == nil then
		return allowEmpty and "" or nil
	end
	if type(v) == "number" then
		if v ~= v then
			return allowEmpty and "" or nil
		end
		if math.floor(v) == v and math.abs(v) < 1e15 then
			return string.format("%.0f", v)
		end
		return trim(tostring(v))
	end
	local s = trim(tostring(v))
	if s == "" then
		return allowEmpty and "" or nil
	end
	local n = tonumber(s)
	if n and math.floor(n) == n and s:lower():find("e", 1, true) then
		return string.format("%.0f", n)
	end
	return s
end

function Shared.generateId(prefix)
	local rand = ZombRand and ZombRand(100000, 999999) or math.random(100000, 999999)
	return tostring(prefix or "npc") .. "_" .. timestamp() .. "_" .. tostring(rand)
end

function Shared.safeFileId(id)
	local s = trim(id)
	s = s:gsub("[%c%z]", "")
	s = s:gsub('[\\/:%*%?"<>|%[%]]', "_")
	if s == "" or s == "." or s == ".." then
		return "_unknown"
	end
	return #s > 96 and s:sub(1, 96) or s
end

function Shared.objectKey(ref)
	if type(ref) ~= "table" then
		return nil
	end
	local x = tonumber(ref.x)
	local y = tonumber(ref.y)
	local z = tonumber(ref.z) or 0
	if not x or not y then
		return nil
	end
	local sprite = trim(ref.spriteName)
	local index = tonumber(ref.objectIndex) or 0
	return table.concat(
		{ tostring(math.floor(x)), tostring(math.floor(y)), tostring(math.floor(z)), sprite, tostring(index) },
		"|"
	)
end

function Shared.makeObjectRef(object)
	if not object or not object.getSquare then
		return nil
	end
	local square = object:getSquare()
	if not square then
		return nil
	end
	local spriteName = ""
	if object.getSprite and object:getSprite() and object:getSprite().getName then
		spriteName = object:getSprite():getName() or ""
	end

	local objectIndex = 0
	local objects = square:getObjects()
	if objects then
		for i = 0, objects:size() - 1 do
			if objects:get(i) == object then
				objectIndex = i
				break
			end
		end
	end

	return {
		x = square:getX(),
		y = square:getY(),
		z = square:getZ(),
		spriteName = spriteName,
		objectIndex = objectIndex,
		key = Shared.objectKey({
			x = square:getX(),
			y = square:getY(),
			z = square:getZ(),
			spriteName = spriteName,
			objectIndex = objectIndex,
		}),
	}
end

function Shared.makeDefaultTree(greeting)
	local startId = "start"
	return {
		root = startId,
		nodes = {
			[startId] = {
				id = startId,
				npcText = trim(greeting) ~= "" and trim(greeting) or "Hello.",
				delay = 0,
				typewriter = true,
				responses = {
					{ id = "bye", text = "Goodbye.", action = "close" },
				},
			},
		},
	}
end

function Shared.makeNpc(data)
	data = type(data) == "table" and data or {}
	local now = Shared.nowUnix()
	local name = trim(data.name)
	if name == "" then
		name = "Interactive NPC"
	end
	local objectRef = type(data.objectRef) == "table" and data.objectRef or nil
	if objectRef and not objectRef.key then
		objectRef.key = Shared.objectKey(objectRef)
	end
	local idStr = Shared.coerceStoredString(data.id, true)
	local owe = Shared.normalizeOutfitWear(data.avatar and data.avatar.outfitWear)
	return {
		id = idStr ~= "" and idStr or Shared.generateId("npc"),
		version = Shared.VERSION,
		name = name,
		enabled = data.enabled ~= false,
		visible = data.visible ~= false,
		objectRef = objectRef,
		avatar = {
			mode = Shared.normalizeAvatarMode(data.avatar and data.avatar.mode or data.avatarMode),
			portrait = trim(data.avatar and data.avatar.portrait or data.portrait),
			portraitSource = Shared.normalizePortraitSource(data.avatar and data.avatar.portraitSource),
			outfit = trim(data.avatar and data.avatar.outfit or data.outfit),
			presetId = Shared.coerceStoredString(data.avatar and data.avatar.presetId or data.presetId, true),
			female = data.avatar and data.avatar.female == true or data.female == true,
			appearance = Shared.normalizeAppearance(data.avatar and data.avatar.appearance),
			outfitWear = #owe > 0 and owe or nil,
		},
		theme = type(data.theme) == "table" and data.theme or {},
		settings = {
			interactionDistance = tonumber(
				data.settings and data.settings.interactionDistance or data.interactionDistance
			) or 2.5,
			minimumAccessLevel = AccessLevelUtils.normalize(data.settings and data.settings.minimumAccessLevel)
				or "None",
			typewriter = not (data.settings and data.settings.typewriter == false),
			typewriterSpeed = tonumber(data.settings and data.settings.typewriterSpeed) or 45,
			openSound = trim(data.settings and data.settings.openSound),
			responseSound = trim(data.settings and data.settings.responseSound),
		},
		dialogue = Shared.normalizeTree(data.dialogue or Shared.makeDefaultTree(data.greeting)),
		createdAtTs = Shared.coerceStoredString(data.createdAtTs, true) ~= ""
				and Shared.coerceStoredString(data.createdAtTs, true)
			or now,
		updatedAtTs = Shared.coerceStoredString(now, true),
	}
end

function Shared.formatRgb255FromUnit(r, g, b)
	if r == nil or g == nil or b == nil then
		return ""
	end
	local function c(x)
		local n = tonumber(x)
		if not n then
			return 0
		end
		return math.max(0, math.min(255, math.floor(n * 255 + 0.5)))
	end
	return string.format("%d %d %d", c(r), c(g), c(b))
end

--- parses UI text as three 0–255 integers; returns internal 0–1 floats or nil.
function Shared.parseAppearanceRgbInput(text)
	if type(text) ~= "string" or trim(text) == "" then
		return nil, nil, nil
	end
	local r, g, b
	local n = 0
	for w in string.gmatch(text, "[%d%-]+") do
		n = n + 1
		local v = tonumber(w)
		if n == 1 then
			r = v
		elseif n == 2 then
			g = v
		elseif n == 3 then
			b = v
			break
		end
	end
	if r == nil or g == nil or b == nil then
		return nil, nil, nil
	end
	r = math.max(0, math.min(255, math.floor(r + 0.5))) / 255
	g = math.max(0, math.min(255, math.floor(g + 0.5))) / 255
	b = math.max(0, math.min(255, math.floor(b + 0.5))) / 255
	return r, g, b
end

function Shared.splitCommaList(str)
	local out = {}
	if type(str) ~= "string" then
		return out
	end
	for part in string.gmatch(str, "[^,\n]+") do
		local p = trim(part)
		if p ~= "" then
			out[#out + 1] = p
		end
	end
	return out
end

function Shared.normalizeMakeupList(raw)
	if raw == nil then
		return {}
	end
	if type(raw) == "table" then
		local out = {}
		for i = 1, #raw do
			local ft = trim(tostring(raw[i] or ""))
			if ft ~= "" and #ft <= 96 then
				out[#out + 1] = ft
			end
			if #out >= Shared.LIMITS.MAKEUP_MAX then
				break
			end
		end
		return out
	end
	if type(raw) == "string" then
		return Shared.normalizeMakeupList(Shared.splitCommaList(raw))
	end
	return {}
end

function Shared.normalizeOutfitWear(raw)
	if type(raw) ~= "table" then
		return {}
	end
	local out = {}
	local maxSlots = tonumber(Shared.LIMITS and Shared.LIMITS.OUTFIT_WEAR_MAX) or 64
	for i = 1, #raw do
		local slot = raw[i]
		if type(slot) == "table" then
			local bl = trim(tostring(slot.bodyLocation or ""))
			local ft = trim(tostring(slot.fullType or ""))
			if bl ~= "" and ft ~= "" then
				out[#out + 1] = {
					bodyLocation = bl:sub(1, 48),
					fullType = ft:sub(1, 128),
				}
			end
		end
		if #out >= maxSlots then
			break
		end
	end
	return out
end

function Shared.normalizeAppearance(app)
	app = type(app) == "table" and app or {}
	local function strField(key)
		local v = app[key]
		if v == nil then
			return nil
		end
		return trim(tostring(v)):sub(1, 64)
	end
	local skin = tonumber(app.skinTextureIndex)
	local out = {
		skinTextureIndex = skin,
		hairModel = strField("hairModel"),
		beardModel = strField("beardModel"),
		hairR = tonumber(app.hairR),
		hairG = tonumber(app.hairG),
		hairB = tonumber(app.hairB),
		beardR = tonumber(app.beardR),
		beardG = tonumber(app.beardG),
		beardB = tonumber(app.beardB),
		makeupFullTypes = Shared.normalizeMakeupList(app.makeupFullTypes),
	}
	do
		local bm = out.beardModel
		if bm then
			local l = bm:lower()
			if l == "none" or l == "-" then
				out.beardModel = ""
			else
				local noneTxt = ""
				if getText then
					noneTxt = trim(getText("IGUI_Beard_None"))
				end
				if noneTxt ~= "" and bm == noneTxt then
					out.beardModel = ""
				end
			end
		end
	end
	return out
end

function Shared.mergeAppearances(presetApp, npcApp)
	local p = Shared.normalizeAppearance(presetApp)
	local n = Shared.normalizeAppearance(npcApp)
	local out = {}
	out.skinTextureIndex = n.skinTextureIndex ~= nil and n.skinTextureIndex or p.skinTextureIndex
	out.hairModel = n.hairModel ~= nil and n.hairModel or p.hairModel
	out.beardModel = n.beardModel ~= nil and n.beardModel or p.beardModel
	out.hairR = n.hairR ~= nil and n.hairR or p.hairR
	out.hairG = n.hairG ~= nil and n.hairG or p.hairG
	out.hairB = n.hairB ~= nil and n.hairB or p.hairB
	out.beardR = n.beardR ~= nil and n.beardR or p.beardR
	out.beardG = n.beardG ~= nil and n.beardG or p.beardG
	out.beardB = n.beardB ~= nil and n.beardB or p.beardB
	if n.makeupFullTypes and #n.makeupFullTypes > 0 then
		out.makeupFullTypes = n.makeupFullTypes
	else
		out.makeupFullTypes = p.makeupFullTypes or {}
	end
	return out
end

function Shared.normalizePortraitSource(raw)
	raw = trim(tostring(raw or "")):lower()
	if raw == Shared.PORTRAIT_SOURCE.SPRITE or raw == "sprite" then
		return Shared.PORTRAIT_SOURCE.SPRITE
	end
	return Shared.PORTRAIT_SOURCE.UI_IMAGE
end

function Shared.normalizeAvatarMode(mode)
	mode = trim(mode):lower()
	if mode == Shared.AVATAR_MODE.PORTRAIT then
		return Shared.AVATAR_MODE.PORTRAIT
	end
	if mode == Shared.AVATAR_MODE.OUTFIT then
		return Shared.AVATAR_MODE.OUTFIT
	end
	if mode == Shared.AVATAR_MODE.PRESET then
		return Shared.AVATAR_MODE.PRESET
	end
	return Shared.AVATAR_MODE.NONE
end

function Shared.normalizeAvatarPreset(preset)
	preset = type(preset) == "table" and preset or {}
	local slots = {}
	local rawSlots = type(preset.slots) == "table" and preset.slots or {}
	for i = 1, #rawSlots do
		local slot = rawSlots[i]
		if type(slot) == "table" and trim(slot.bodyLocation) ~= "" and trim(slot.fullType) ~= "" then
			slots[#slots + 1] = {
				bodyLocation = trim(slot.bodyLocation):sub(1, 48),
				fullType = trim(slot.fullType):sub(1, 96),
			}
		end
	end
	local name = trim(preset.name)
	if name == "" then
		name = "Avatar Preset"
	end
	local pid = Shared.coerceStoredString(preset.id, true)
	return {
		id = pid ~= "" and pid or Shared.generateId("avatar"),
		name = name:sub(1, Shared.LIMITS.NAME_MAX),
		female = preset.female == true,
		baseOutfit = trim(preset.baseOutfit):sub(1, 96),
		slots = slots,
		appearance = Shared.normalizeAppearance(preset.appearance),
		updatedAtTs = Shared.coerceStoredString(preset.updatedAtTs, true) ~= ""
				and Shared.coerceStoredString(preset.updatedAtTs, true)
			or Shared.nowUnix(),
	}
end

function Shared.normalizeAvatarPresets(presets)
	local out = {}
	for i = 1, #(type(presets) == "table" and presets or {}) do
		out[#out + 1] = Shared.normalizeAvatarPreset(presets[i])
	end
	return out
end

function Shared.normalizeResponse(response, fallbackIndex)
	if type(response) ~= "table" then
		return nil
	end
	local text = trim(response.text)
	if text == "" then
		text = "Continue"
	end
	local rid = Shared.coerceStoredString(response.id, true)
	return {
		id = rid ~= "" and rid or ("response_" .. tostring(fallbackIndex or 1)),
		text = text:sub(1, Shared.LIMITS.TEXT_MAX),
		target = trim(response.target),
		action = trim(response.action) ~= "" and trim(response.action) or nil,
		condition = trim(response.condition):sub(1, Shared.LIMITS.CONDITION_MAX),
		delay = tonumber(response.delay) or 0,
		playerText = trim(response.playerText):sub(1, Shared.LIMITS.TEXT_MAX),
	}
end

function Shared.normalizeNode(node, id)
	if type(node) ~= "table" then
		return nil
	end
	local nodeId = Shared.coerceStoredString(node.id, true)
	if nodeId == "" then
		nodeId = Shared.coerceStoredString(id, true) or ""
	end
	if nodeId == "" then
		return nil
	end
	local responses = {}
	local rawResponses = type(node.responses) == "table" and node.responses or {}
	local maxResponses = math.min(#rawResponses, Shared.LIMITS.RESPONSE_MAX)
	for i = 1, maxResponses do
		local response = Shared.normalizeResponse(rawResponses[i], i)
		if response then
			responses[#responses + 1] = response
		end
	end
	local nt = trim(node.npcText)
	if nt == "" then
		nt = " "
	end
	return {
		id = nodeId,
		npcText = nt:sub(1, Shared.LIMITS.TEXT_MAX),
		delay = tonumber(node.delay) or 0,
		typewriter = node.typewriter ~= false,
		condition = trim(node.condition):sub(1, Shared.LIMITS.CONDITION_MAX),
		responses = responses,
	}
end

function Shared.normalizeTree(tree)
	tree = type(tree) == "table" and tree or Shared.makeDefaultTree()
	local nodes = {}
	local count = 0
	if type(tree.nodes) == "table" then
		local nk = Shared.tableKeys(tree.nodes)
		for i = 1, #nk do
			if count >= Shared.LIMITS.NODE_MAX then
				break
			end
			local id = nk[i]
			local normalized = Shared.normalizeNode(tree.nodes[id], id)
			if normalized then
				nodes[normalized.id] = normalized
				count = count + 1
			end
		end
	end
	if not nodes.start then
		nodes.start = Shared.normalizeNode({
			id = "start",
			npcText = "Hello.",
			responses = { { text = "Goodbye.", action = "close" } },
		})
	end
	local root = trim(tree.root)
	if root == "" or not nodes[root] then
		root = "start"
	end
	return { root = root, nodes = nodes }
end

function Shared.validateNpc(data)
	if type(data) ~= "table" then
		return false, getText("IGUI_INPC_ErrInvalidData")
	end
	local npc = Shared.makeNpc(data)
	if trim(npc.name) == "" then
		return false, getText("IGUI_INPC_ErrNameRequired")
	end
	if #npc.name > Shared.LIMITS.NAME_MAX then
		return false, getText("IGUI_INPC_ErrNameTooLong")
	end
	if npc.avatar.portrait ~= "" and #npc.avatar.portrait > Shared.LIMITS.PORTRAIT_MAX then
		return false, getText("IGUI_INPC_ErrPortraitTooLong")
	end
	return true, nil, npc
end

function Shared.publicNpc(npc)
	if type(npc) ~= "table" then
		return nil
	end
	return {
		id = Shared.coerceStoredString(npc.id, true) or npc.id,
		name = npc.name,
		enabled = npc.enabled,
		visible = npc.visible,
		objectRef = npc.objectRef,
		avatar = npc.avatar,
		theme = npc.theme,
		settings = npc.settings,
		updatedAtTs = Shared.coerceStoredString(npc.updatedAtTs, true) or npc.updatedAtTs,
	}
end

function Shared.publicRegistry(npcs)
	local out = {}
	local n = 0
	for i = 1, #(npcs or {}) do
		local npc = npcs[i]
		if npc and npc.enabled and npc.visible then
			n = n + 1
			out[n] = Shared.publicNpc(npc)
		end
	end
	return out
end

function Shared.isSinglePlayer()
	return AccessLevelUtils.isSinglePlayer()
end

return Shared
