-- Build 42.20.x compatibility guard for PZAPI Mod Options.
--
-- Vanilla MainOptions:addModOptionsPanel() takes no panel argument. It calls
-- PZAPI.ModOptions:load(), iterates PZAPI.ModOptions.Data, and then feeds panel
-- and option display strings through getText(). Mods may already have resolved
-- those strings, so a literal '%' can reach Java Formatter as a conversion
-- marker and raise UnknownFormatConversionException (for example, "30%)").
--
-- Keep this guard narrow: sanitize only fields which vanilla MainOptions sends
-- through getText() again. Descriptions and combobox values are rendered through
-- different paths and are intentionally left alone.

LCCRussianTextPercentGuard = LCCRussianTextPercentGuard or {}
local state = LCCRussianTextPercentGuard
local VERSION = "1.1.3"

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
                -- Already escaped for java.util.Formatter.
                out[#out + 1] = "%%"
                i = i + 2
            elseif nextCh ~= "" and string.match(nextCh, "%d") then
                -- Preserve positional placeholders such as %1$s.
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

local function sanitizeField(owner, field)
    if type(owner) ~= "table" then return 0 end

    local sanitized, changed = escapeUnsafePercents(owner[field])
    if not changed then return 0 end

    owner[field] = sanitized
    return 1
end

local function sanitizePanel(options)
    if type(options) ~= "table" then return 0 end

    local changed = sanitizeField(options, "name")
    local entries = options.data
    if type(entries) ~= "table" then return changed end

    for _, option in ipairs(entries) do
        if type(option) == "table" then
            changed = changed + sanitizeField(option, "name")
            changed = changed + sanitizeField(option, "tooltip")

            if option.type == "multipletickbox" and type(option.values) == "table" then
                for _, entry in ipairs(option.values) do
                    if type(entry) == "table" then
                        changed = changed + sanitizeField(entry, "name")
                        changed = changed + sanitizeField(entry, "tooltip")
                    end
                end
            end
        end
    end

    return changed
end

local function sanitizeModOptionsData()
    if not PZAPI or not PZAPI.ModOptions or type(PZAPI.ModOptions.Data) ~= "table" then
        return 0
    end

    local changed = 0
    for _, options in ipairs(PZAPI.ModOptions.Data) do
        changed = changed + sanitizePanel(options)
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
    MainOptions.addModOptionsPanel = function(self, ...)
        -- Match B42.20's real control flow. load() only restores option values
        -- from ModOptions.ini; it does not rebuild Data, so preloading here is
        -- safe and lets us sanitize the exact tables the original will render.
        if PZAPI and PZAPI.ModOptions and PZAPI.ModOptions.load then
            local ok, err = pcall(function()
                PZAPI.ModOptions:load()
            end)
            if not ok then
                log("preload failed error=" .. tostring(err))
            end
        end

        local changed = sanitizeModOptionsData()
        if changed > 0 then
            log("sanitized count=" .. tostring(changed))
        end

        return original(self, ...)
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
