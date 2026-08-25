ExtractionMode = ExtractionMode or {}

local Localization = {}

local function translated(key, arguments)
    if key == nil or key == "" or getText == nil then return nil end
    local resolvedArguments = {}
    for index, argument in ipairs(arguments or {}) do
        if type(argument) == "table" and argument.key then
            local nested = nil
            pcall(function() nested = getText(tostring(argument.key)) end)
            resolvedArguments[index] = nested and tostring(nested) ~= tostring(argument.key)
                and tostring(nested) or tostring(argument.fallback or argument.key)
        else
            resolvedArguments[index] = argument
        end
    end
    local ok, value = pcall(function()
        return getText(tostring(key), unpack(resolvedArguments))
    end)
    if not ok or value == nil or tostring(value) == tostring(key) then return nil end
    return tostring(value)
end

-- Resolve a translation key locally and retain the supplied English text as a
-- compatibility fallback. This lets servers and older clients safely exchange
-- keyed messages without forcing the server's own language on every player.
function Localization.get(key, fallback, ...)
    return translated(key, { ... }) or tostring(fallback or key or "")
end

function Localization.field(definition, field)
    if definition == nil then return "" end
    return Localization.get(definition[tostring(field) .. "Key"], definition[field])
end

function Localization.resolveMessage(payload)
    payload = payload or {}
    return translated(payload.messageKey, payload.messageArgs)
        or tostring(payload.message or payload.messageKey or "")
end

ExtractionMode.Localization = Localization
return Localization
