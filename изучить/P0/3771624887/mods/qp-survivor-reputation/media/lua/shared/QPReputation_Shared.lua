QPReputation = QPReputation or {}

QPReputation.Paths = { "community", "hunter", "explorer", "medic", "mechanic", "builder" }

function QPReputation.isValidPath(path)
    if not path then return false end
    path = string.lower(tostring(path))
    for _, value in ipairs(QPReputation.Paths) do
        if value == path and QPReputation.Config.Paths[value] ~= false then return true end
    end
    return false
end

function QPReputation.clampPoints(value)
    value = math.floor(tonumber(value) or 0)
    if value < 0 then return 0 end
    return value
end

function QPReputation.getLevel(points)
    points = QPReputation.clampPoints(points)
    local level = 0
    for i, threshold in ipairs(QPReputation.Config.Thresholds) do
        if points >= threshold then level = i - 1 else break end
    end
    return math.min(level, #QPReputation.Config.Thresholds - 1)
end

function QPReputation.getTitle(level)
    level = math.max(0, math.min(tonumber(level) or 0, #QPReputation.Config.GenericTitles - 1))
    return QPReputation.Config.GenericTitles[level + 1] or getText("UI_QPSR_Unknown")
end

function QPReputation.getNextThreshold(level)
    local index = (tonumber(level) or 0) + 2
    return QPReputation.Config.Thresholds[index]
end
