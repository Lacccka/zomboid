NMMediaWorldTextureResolver = NMMediaWorldTextureResolver or {}

local pathCache = {}
local textureCache = {}

local function norm(value)
    return tostring(value or "")
end

local function findScriptItem(fullType)
    local ft = norm(fullType)
    if ft == "" then
        return nil
    end
    return ScriptManager and ScriptManager.instance and ScriptManager.instance.FindItem
        and ScriptManager.instance:FindItem(ft) or nil
end

local function orderedCandidates(fullType)
    local out = {}
    local seen = {}

    local function add(value)
        local key = norm(value)
        if key == "" or seen[key] then
            return
        end
        seen[key] = true
        out[#out + 1] = key
    end

    add(fullType)

    if NMMediaContract and NMMediaContract.resolveContainerMediaBinding then
        add(NMMediaContract.resolveContainerMediaBinding(fullType))
    end

    if NMMediaContract and NMMediaContract.resolveMediaCanonical then
        add(NMMediaContract.resolveMediaCanonical(fullType))
    end

    return out
end

local function resolveWorldStaticModelToken(fullType)
    local item = findScriptItem(fullType)
    if not item then
        return ""
    end

    if item.getWorldStaticModel then
        local ok, token = pcall(item.getWorldStaticModel, item)
        if ok and tostring(token or "") ~= "" then
            return tostring(token)
        end
    end

    if item.getStaticModel then
        local ok, token = pcall(item.getStaticModel, item)
        if ok and tostring(token or "") ~= "" then
            return tostring(token)
        end
    end

    return ""
end

local function toTexturePath(textureToken)
    local token = norm(textureToken)
    if token == "" then
        return nil
    end
    token = token:gsub("\\", "/")
    if string.match(token, "^media/textures/.+%.png$") then
        return token
    end
    if string.match(token, "^media/textures/") then
        return token .. ".png"
    end
    return "media/textures/" .. token .. ".png"
end

local function findModelScript(token)
    local modelToken = norm(token)
    if modelToken == "" then
        return nil
    end
    local sm = ScriptManager and ScriptManager.instance or nil
    if not sm then
        return nil
    end

    local candidates = {}
    local seen = {}
    local function addCandidate(value)
        local key = norm(value)
        if key == "" or seen[key] then
            return
        end
        seen[key] = true
        candidates[#candidates + 1] = key
    end

    addCandidate(modelToken)
    addCandidate(modelToken:match("([^%.]+)$"))

    local methods = {
        "getModelScript",
        "GetModelScript",
        "FindModelScript",
        "getModel",
        "GetModel",
    }

    for i = 1, #candidates do
        local candidate = candidates[i]
        for j = 1, #methods do
            local methodName = methods[j]
            local fn = sm[methodName]
            if type(fn) == "function" then
                local ok, modelScript = pcall(fn, sm, candidate)
                if ok and modelScript then
                    return modelScript
                end
            end
        end
    end

    return nil
end

local function resolveModelScriptTexturePath(token)
    local modelScript = findModelScript(token)
    if not modelScript then
        return nil
    end

    local methods = {
        "getTextureName",
        "GetTextureName",
        "getTexture",
        "GetTexture",
    }

    for i = 1, #methods do
        local methodName = methods[i]
        local fn = modelScript[methodName]
        if type(fn) == "function" then
            local ok, textureToken = pcall(fn, modelScript)
            local texturePath = ok and toTexturePath(textureToken) or nil
            if texturePath then
                return texturePath
            end
        end
    end

    return nil
end

local function cassettePathFromWorldModelToken(token)
    local modelToken = norm(token)
    if modelToken == "" then
        return nil
    end

    local cassetteNumber = string.match(modelToken, "^NewMusic%.Cassette(%d+)$")
    if cassetteNumber then
        local idx = tonumber(cassetteNumber)
        if idx and idx >= 1 and idx <= 99 then
            return string.format("media/textures/WorldItems/Cassette/World_NM_Cassette%02d.png", idx)
        end
    end

    return nil
end

local function cdPathFromWorldModelToken(token)
    local modelToken = norm(token)
    if modelToken == "" then
        return nil
    end

    if modelToken == "NewMusic.CD" then
        return "media/textures/WorldItems/CD/World_NM_CD.png"
    end

    local modelTexturePath = resolveModelScriptTexturePath(modelToken)
    if modelTexturePath then
        return modelTexturePath
    end

    if string.match(modelToken, "CD$") and not string.match(modelToken, "CDCover") then
        return "media/textures/WorldItems/CD/World_NM_CD.png"
    end

    return nil
end

local function cassettePathFromIconToken(fullType)
    local item = findScriptItem(fullType)
    if not item or not item.getIcon then
        return nil
    end

    local ok, iconToken = pcall(item.getIcon, item)
    if not ok then
        return nil
    end

    local icon = norm(iconToken)
    if icon == "" then
        return nil
    end

    local cassetteNumber = string.match(icon, "^TCTape(%d+)$")
    if cassetteNumber then
        local idx = tonumber(cassetteNumber)
        if idx and idx >= 1 and idx <= 99 then
            return string.format("media/textures/WorldItems/Cassette/World_NM_Cassette%02d.png", idx)
        end
    end

    cassetteNumber = string.match(icon, "^NM_Cassette0*(%d+)$")
    if cassetteNumber then
        local idx = tonumber(cassetteNumber)
        if idx and idx >= 1 and idx <= 99 then
            return string.format("media/textures/WorldItems/Cassette/World_NM_Cassette%02d.png", idx)
        end
    end

    local suffix = string.match(icon, "^NM_Cassette_(.+)$")
    if suffix and suffix ~= "" then
        return "media/textures/WorldItems/Cassette/World_NM_Cassette_" .. suffix .. ".png"
    end

    return nil
end

function NMMediaWorldTextureResolver.resolvePath(fullType)
    local ft = norm(fullType)
    if ft == "" then
        return nil
    end

    local cached = pathCache[ft]
    if cached ~= nil then
        return cached or nil
    end

    local path = nil
    local candidates = orderedCandidates(ft)
    for i = 1, #candidates do
        local candidate = candidates[i]
        local modelToken = resolveWorldStaticModelToken(candidate)
        path = cassettePathFromWorldModelToken(modelToken)
            or cdPathFromWorldModelToken(modelToken)
            or cassettePathFromIconToken(candidate)
        if path then
            break
        end
    end

    pathCache[ft] = path or false
    return path
end

function NMMediaWorldTextureResolver.resolveTexture(fullType)
    local path = NMMediaWorldTextureResolver.resolvePath(fullType)
    if not path or not getTexture then
        return nil, path
    end

    local cached = textureCache[path]
    if cached ~= nil then
        return cached or nil, path
    end

    local tex = getTexture(path)
    textureCache[path] = tex or false
    return tex, path
end
