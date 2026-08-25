require "ExtractionMode/Config"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local BookXP = {}
local lastChecks = {}

-- XPSystem_SkillBook.lua lives in vanilla's server-only Lua tree in Build 42.
-- Requiring it from this shared module produces a warning on every client and
-- can fail during the server's early shared-file pass. Resolve the handful of
-- literature-facing names through the engine's shared perk registry instead.
local perkAliases = {
    Carpentry = "Woodwork",
    FirstAid = "Doctor",
    Foraging = "PlantScavenging",
}
local perkTranslationAliases = {
    FirstAid = "Doctor",
}

local function perkForSkill(trainedName)
    local name = tostring(trainedName or "")
    if name == "" or Perks == nil or Perks.FromString == nil then return nil end
    local ok, perk = pcall(function()
        return Perks.FromString(perkAliases[name] or name)
    end)
    if not ok or perk == nil or (Perks.None ~= nil and perk == Perks.None) then return nil end
    return perk
end

local function completedSkillBooks(player)
    local inventory = player and player:getInventory()
    if inventory == nil then return nil end
    return inventory:getAllEvalRecurse(function(item)
        if item == nil or not instanceof(item, "Literature") then return false end
        local pages = tonumber(item:getNumberOfPages()) or 0
        local perk = perkForSkill(item:getSkillTrained())
        return pages > 0 and perk ~= nil
            and (tonumber(player:getAlreadyReadPages(item:getFullType())) or 0) >= pages
    end)
end

function BookXP.processPlayer(player, nowSecond)
    if player == nil or player:isDead() or Config.value("SkillBooksGrantXP") == false then return nil end
    local username = tostring(player:getUsername() or player:getDisplayName() or "")
    nowSecond = tonumber(nowSecond) or 0
    if nowSecond - (tonumber(lastChecks[username]) or -10) < 3 then return nil end
    lastChecks[username] = nowSecond

    local books = completedSkillBooks(player)
    if books == nil or books:isEmpty() then return nil end
    local modData = player:getModData()
    modData.ExtractionModeBookXP = modData.ExtractionModeBookXP or {}
    local awarded = modData.ExtractionModeBookXP
    local rewards = {}

    for index = 0, books:size() - 1 do
        local item = books:get(index)
        local fullType = item and item:getFullType()
        if fullType and awarded[fullType] ~= true then
            -- Mark first so duplicate copies of the same volume cannot award twice
            -- during one inventory scan or after reconnecting.
            awarded[fullType] = true
            local trainedName = item:getSkillTrained()
            local maximumLevel = math.max(0, math.min(10,
                math.floor(tonumber(item:getMaxLevelTrained()) or 0)))
            local perk = perkForSkill(trainedName)
            if perk and maximumLevel > 0 then
                local currentXP = tonumber(player:getXp():getXP(perk)) or 0
                local targetXP = tonumber(perk:getTotalXpForLevel(maximumLevel)) or currentXP
                local amount = math.max(0, targetXP - currentXP)
                if amount > 0 then
                    -- Direct training XP deliberately ignores the multiplier the
                    -- same book grants, otherwise the reward would exceed its tier.
                    player:getXp():AddXP(perk, amount, false, false, false, false)
                    rewards[#rewards + 1] = {
                        skill = tostring(trainedName or "Skill"),
                        skillKey = "IGUI_perks_"
                            .. tostring(perkTranslationAliases[tostring(trainedName or "")]
                                or trainedName or "Skill"),
                        amount = math.floor(amount * 10 + 0.5) / 10,
                        level = maximumLevel,
                    }
                end
            end
        end
    end

    if #rewards > 0 then pcall(function() player:transmitModData() end) end
    return rewards
end

ExtractionMode.BookXP = BookXP
return BookXP
