-- Build 42.20.x compatibility guard for PZAPI Mod Options.
-- MainOptions:addModOptionsPanel() calls getText() on option names/tooltips even
-- when a mod has already passed resolved display text. A literal '%' in that
-- display text can therefore reach Java Formatter as a conversion marker and
-- raise UnknownFormatConversionException (for example, "30%)").
--
-- Keep this patch deliberately narrow: only strings that MainOptions will feed
-- through getText() again are sanitized. Combo-box display values and
-- descriptions are left untouched because MainOptions renders those directly.

LCCRussianTextPercentGuard = LCCRussianTextPercentGuard or {}
local state = LCCRussianTextPercentGuard

local function escapeUnsafePercents(value)
    if type(value) ~= "string" or not string.find(value, "%", 1, true) then
        return value, false
    end

    local out = {}
    local changed = false
    local i = 1
    local length = #value

    while i <= length do
        local ch = string.sub(value, i, i)
        if ch ~= "%" then
            out[#out + 1] = ch
            i = i + 1
        else
            local nextCh = string.sub(value, i + 1, i + 1)
            if nextCh == "%" then
                out[#out + 1] = "%%"
                i = i + 2
            elseif nextCh ~= "" and string.match(nextCh, "%d") then
                out[#out + 1] = "%"
                i = i + 1
                while i <= length and string.match(string.sub(value, i, i), "%d") do
                    out[#out + 1] = string.sub(value, i, i)
                    i = i + 1
                end
            else
                out[#out + 1] = "%%"
                changed = true
                i = i + 1
            end
        end
    end

    return table.concat(out), changed
end

local function sanitizeField(owner, field, scope)
    if type(owner) ~= "table" then
        return 0
    end

    local value = owner[field]
    local sanitized, changed = escapeUnsafePercents(value)
    if not changed then
        return 0
    end

    owner[field] = sanitized
    print(string.format("[LCC][RussianText][ModOptionsPercentGuard] sanitized field=%s scope=%s", tostring(field), tostring(scope)))
    return 1
end

local function sanitizeOptions(options)
    if type(options) ~= "table" then
        return 0
    end

    local changed = sanitizeField(options, "name", options.id or options.modOptionsID or "panel")
    local entries = options.options
    if type(entries) ~= "table" then
        return changed
    end

    for index, option in pairs(entries) do
        if type(option) == "table" then
            local scope = option.id or option.name or index
            changed = changed + sanitizeField(option, "name", scope)
            changed = changed + sanitizeField(option, "tooltip", scope)

            if option.type == "multipletickbox" and type(option.values) == "table" then
                for valueIndex, entry in pairs(option.values) do
                    changed = changed + sanitizeField(entry, "name", tostring(scope) .. ":" .. tostring(valueIndex))
                end
            end
        end
    end

    return changed
end

local function install()
    if state.installed then
        return true
    end

    if not MainOptions or not MainOptions.addModOptionsPanel then
        pcall(require, "OptionScreens/MainOptions")
    end
    if not MainOptions or not MainOptions.addModOptionsPanel then
        pcall(require, "ISUI/MainOptions")
    end
    if not MainOptions or not MainOptions.addModOptionsPanel then
        return false
    end

    local original = MainOptions.addModOptionsPanel
    MainOptions.addModOptionsPanel = function(self, options, ...)
        local changed = sanitizeOptions(options)
        if changed > 0 then
            print(string.format("[LCC][RussianText][ModOptionsPercentGuard] sanitized=%d", changed))
        end
        return original(self, options, ...)
    end

    state.installed = true
    state.originalAddModOptionsPanel = original
    print("[LCC][RussianText][ModOptionsPercentGuard] installed")
    return true
end

install()
if Events and Events.OnGameBoot then
    Events.OnGameBoot.Add(install)
end
