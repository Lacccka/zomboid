local JSON = require("ElyonLib/FileUtils/JSON")
local Shared = require("InteractiveNPCs/Shared")

local Persistence = {}

local function path(name)
	return Shared.DATA_DIR .. "/" .. name
end

local function npcPath(npcId)
	return path("npcs/" .. Shared.safeFileId(npcId) .. "/npc.json")
end

local function readJsonQuiet(filePath)
	local reader = getFileReader(filePath, false)
	if not reader then
		return nil
	end
	local lines = {}
	local line = reader:readLine()
	while line do
		lines[#lines + 1] = line
		line = reader:readLine()
	end
	reader:close()
	local content = table.concat(lines, "\n")
	if content == "" then
		return nil
	end
	return JSON.parse(content)
end

local function writeJsonQuiet(filePath, data)
	local content = JSON.stringify(data)
	if not content then
		return false
	end
	local writer = getFileWriter(filePath, true, false)
	if not writer then
		return false
	end
	writer:write(content)
	writer:close()
	return true
end

function Persistence.normalizeNpc(npc)
	local ok, err, normalized = Shared.validateNpc(npc)
	if not ok then
		return nil, err
	end
	return normalized
end

function Persistence.loadNpcs()
	local index = readJsonQuiet(Shared.INDEX_FILE)
	if type(index) ~= "table" or type(index.npcIds) ~= "table" then
		return {}
	end
	local npcs = {}
	for i = 1, #index.npcIds do
		local npc = readJsonQuiet(npcPath(Shared.coerceStoredString(index.npcIds[i], true)))
		local normalized = Persistence.normalizeNpc(npc)
		if normalized then
			npcs[#npcs + 1] = normalized
		end
	end
	return npcs
end

function Persistence.loadAvatarPresets()
	local data = readJsonQuiet(Shared.PRESETS_FILE)
	if type(data) == "table" and type(data.presets) == "table" then
		return Shared.normalizeAvatarPresets(data.presets)
	end
	return {}
end

function Persistence.saveAvatarPresets(presets)
	return writeJsonQuiet(Shared.PRESETS_FILE, {
		version = Shared.VERSION,
		presets = Shared.normalizeAvatarPresets(presets),
	})
end

function Persistence.saveNpcs(npcs)
	npcs = type(npcs) == "table" and npcs or {}
	local ids = {}
	local ok = true
	for i = 1, #npcs do
		local normalized = Persistence.normalizeNpc(npcs[i])
		if normalized then
			npcs[i] = normalized
			ids[#ids + 1] = Shared.coerceStoredString(normalized.id, true)
			ok = writeJsonQuiet(npcPath(normalized.id), normalized) and ok
		end
	end
	ok = writeJsonQuiet(Shared.INDEX_FILE, { version = Shared.VERSION, npcIds = ids }) and ok
	return ok
end

return Persistence
