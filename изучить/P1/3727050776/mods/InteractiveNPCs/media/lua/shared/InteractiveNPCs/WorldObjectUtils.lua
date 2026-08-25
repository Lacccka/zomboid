local Shared = require("InteractiveNPCs/Shared")

local WorldObjectUtils = {}

local function getObjects(square)
	if not square or not square.getObjects then
		return nil
	end
	return square:getObjects()
end

function WorldObjectUtils.getSpriteName(object)
	if object and object.getSprite and object:getSprite() and object:getSprite().getName then
		return object:getSprite():getName() or ""
	end
	return ""
end

function WorldObjectUtils.findObject(ref)
	if type(ref) ~= "table" then
		return nil
	end
	local x = tonumber(ref.x)
	local y = tonumber(ref.y)
	local z = tonumber(ref.z) or 0
	if not x or not y or not getCell then
		return nil
	end
	local square = getCell():getGridSquare(x, y, z)
	local objects = getObjects(square)
	if not objects then
		return nil
	end

	local wantedIndex = tonumber(ref.objectIndex)
	if wantedIndex and wantedIndex >= 0 and wantedIndex < objects:size() then
		local object = objects:get(wantedIndex)
		if
			object
			and (not ref.spriteName or ref.spriteName == "" or WorldObjectUtils.getSpriteName(object) == ref.spriteName)
		then
			return object
		end
	end

	local wantedKey = Shared.objectKey(ref)
	for i = 0, objects:size() - 1 do
		local object = objects:get(i)
		local objectRef = Shared.makeObjectRef(object)
		if objectRef and objectRef.key == wantedKey then
			return object
		end
	end

	for i = 0, objects:size() - 1 do
		local object = objects:get(i)
		if object and WorldObjectUtils.getSpriteName(object) == tostring(ref.spriteName or "") then
			return object
		end
	end

	return nil
end

function WorldObjectUtils.markObject(ref, npcId)
	local object = WorldObjectUtils.findObject(ref)
	if not object or not object.getModData then
		return false
	end
	object:getModData()[Shared.OBJECT_MODDATA_KEY] = npcId
	if object.transmitModData then
		object:transmitModData()
	end
	return true
end

function WorldObjectUtils.unmarkObject(ref)
	local object = WorldObjectUtils.findObject(ref)
	if not object or not object.getModData then
		return false
	end
	object:getModData()[Shared.OBJECT_MODDATA_KEY] = nil
	if object.transmitModData then
		object:transmitModData()
	end
	return true
end

function WorldObjectUtils.distanceToPlayer(ref, player)
	local object = WorldObjectUtils.findObject(ref)
	if object and object.getSquare and object:getSquare() and player and player.getSquare then
		return object:getSquare():DistToProper(player)
	end
	if type(ref) == "table" and player and player.getX then
		local dx = (tonumber(ref.x) or 0) - player:getX()
		local dy = (tonumber(ref.y) or 0) - player:getY()
		return math.sqrt(dx * dx + dy * dy)
	end
	return 9999
end

function WorldObjectUtils.readMannequinDescriptor(ref)
	local object = WorldObjectUtils.findObject(ref)
	if not object then
		return nil
	end

	--- Build 41 mannequin internals are Java-side and vary by object type/mod.
	--- This helper intentionally returns only conservative, serializable hints
	--- unless a verified accessor exists in the runtime.
	local info = {
		isMannequin = instanceof and instanceof(object, "IsoMannequin") or false,
		spriteName = WorldObjectUtils.getSpriteName(object),
	}
	if object.getMannequinScriptName then
		info.scriptName = object:getMannequinScriptName()
	end
	return info
end

return WorldObjectUtils
