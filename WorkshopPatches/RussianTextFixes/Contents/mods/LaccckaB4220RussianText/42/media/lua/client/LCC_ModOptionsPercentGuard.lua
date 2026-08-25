-- Build 42.20.x compatibility guard for PZAPI Mod Options.
-- MainOptions:addModOptionsPanel() calls getText() on option names/tooltips even
-- when a mod has already passed resolved display text. A literal '%' in that
-- display text can therefore reach Java Formatter as a conversion marker and
-- raise UnknownFormatConversionException (for example, "30%)").
--
-- Keep this patch deliberately narrow: only strings that MainOptions feeds
-- through getText() again are sanitized. Combo-box display values and general
-- descriptions are left untouched because MainOptions renders those directly.

LCCRussianTextPercentGuard = LCCRussianTextPercentGuard or {}
local state = LCCRussianTextPercentGuard
local VERSION = "1.1.2"

local function log(message)
    print("[LCC][RussianText][ModOptionsPercentGuard] " .. tostring(message))
end

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
                -- Preserve Java positional placeholders such as %1$s. Plain
                -- percentages such as 30% never enter this branch because the
                -- character after '%' is not a digit.
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
    if type(owner) ~= "table" then return 0 end

    local value = owner[field]
    local sanitized, changed = escapeUnsafePercents(value)
    if not changed then return 0 end

    owner[field] = sanitized
    log("sanitized field=" .. tostring(field) .. " scope=" .. tostring(scope))
    return 1
end

local function sanitizeOptions(options)
    if type(options) ~= "table" then return 0 end

    local panelScope = options.id or options.modOptionsID or "panel"
    local changed = sanitizeField(options, "name", panelScope)
    changed = changed + sanitizeField(options, "tooltip", panelScope)

    local entries = options.options
    if type(entries) ~= "table" then return changed end

    for index, option in pairs(entries) do
        if type(option) == "table" then
            local scope = option.id or option.name or index
            changed = changed + sanitizeField(option, "name", scope)
            changed = changed + sanitizeField(option, "tooltip", scope)

            if option.type == "multipletickbox" and type(option.values) == "table" then
                for valueIndex, entry in pairs(option.values) do
                    if type(entry) == "table" then
                        changed = changed + sanitizeField(entry, "name", tostring(scope) .. ":" .. tostring(valueIndex))
                        changed = changed + sanitizeField(entry, "tooltip", tostring(scope) .. ":" .. tostring(valueIndex))
                    end
                end
            end
        end
    end

    return changed
end

local function install()
    if state.installed then return true end

    if not MainOptions or not MainOptions.addModOptionsPanel then
        pcall(require, "OptionScreens/MainOptions")
    end
    if not MainOptions or not MainOptions.addModOptionsPanel then
        pcall(require, "ISUI/MainOptions")
    end
    if not MainOptions or not MainOptions.addModOptionsPanel then
        state.lastInstallResult = "MainOptions unavailable"
        return false
    end

    local original = MainOptions.addModOptionsPanel
    MainOptions.addModOptionsPanel = function(self, options, ...)
        local changed = sanitizeOptions(options)
        if changed > 0 then
            log("sanitized=" .. tostring(changed))
        end
        return original(self, options, ...)
    end

    state.installed = true
    state.version = VERSION
    state.originalAddModOptionsPanel = original
    state.lastInstallResult = "installed"
    log("installed version=" .. VERSION)
    return true
end

-- This load marker is intentionally unconditional. If it is absent from a
-- failing client log, the Workshop copy is older than this compatibility fix.
log("loaded version=" .. VERSION)
install()
if Events and Events.OnGameBoot then
    Events.OnGameBoot.Add(install)
end
