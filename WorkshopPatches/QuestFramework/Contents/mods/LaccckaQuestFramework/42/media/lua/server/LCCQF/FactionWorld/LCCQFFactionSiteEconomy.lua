-- Server-owned logical settlement economy derived from verified physical stock.
-- This layer never owns InventoryItem objects and never mutates ItemContainer state.
-- It translates faction reserve policy + logical population + observed stock into
-- stable economic metrics that jobs, traders and dynamic quests may consume later.
if isClient and isClient() and not (isServer and isServer()) then return {} end

require "LCCQF/LCCQFConstants"
require "LCCQF/Content/LCCQFSupplyCategoryDefinitions"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSitePopulation"
require "LCCQF/FactionWorld/LCCQFFactionSiteStock"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Categories = LCCQF.SupplyCategoryRegistry
local Sites = LCCQF.FactionSiteRegistry
local Population = LCCQF.FactionSitePopulation
local Stock = LCCQF.FactionSiteStock
local Economy = LCCQF.FactionSiteEconomy or {}

local function worldHours()
    if not getGameTime then return 0 end
    local gameTime = getGameTime()
    return gameTime and gameTime.getWorldAgeHours and (tonumber(gameTime:getWorldAgeHours()) or 0) or 0
end

local function livingPopulation(site)
    return #(Population.ListMembers(site) or {})
end

local function reservePolicy(definition)
    local operations = definition and definition.operationsProfile or nil
    return type(operations) == "table" and type(operations.supplies) == "table"
        and operations.supplies or {}
end

local function normalizeRevision(value)
    return math.max(0, math.floor(tonumber(value) or 0))
end

local function normalizeQuantity(categoryId, value)
    local number = math.max(0, tonumber(value) or 0)
    if Categories and Categories.NormalizeQuantity then
        return Categories.NormalizeQuantity(categoryId, number)
    end
    return number
end

local function targetQuantity(categoryId, value)
    local raw = math.max(0, tonumber(value) or 0)
    local semantics = Categories.GetQuantitySemantics and Categories.GetQuantitySemantics(categoryId) or nil
    if semantics and semantics.measureKind == "ITEM" and semantics.precision == 0 and semantics.splittable ~= true then
        return math.max(0, math.ceil(raw))
    end
    return normalizeQuantity(categoryId, raw)
end

local function statusFor(categoryId, available, target)
    available = normalizeQuantity(categoryId, available)
    target = normalizeQuantity(categoryId, target)
    if target <= 0 then return "UNTRACKED" end
    if available <= 0 then return "CRITICAL" end
    if available < target then return "LOW" end
    if available > target then return "SURPLUS" end
    return "ADEQUATE"
end

local function coverageRatio(categoryId, available, target)
    available = normalizeQuantity(categoryId, available)
    target = normalizeQuantity(categoryId, target)
    if target <= 0 then return 1 end
    return available / target
end

local function scalarEqual(a, b)
    if type(a) ~= "table" or type(b) ~= "table" then return a == b end
    for key, value in pairs(a) do
        if type(value) ~= "table" and b[key] ~= value then return false end
    end
    for key, value in pairs(b) do
        if type(value) ~= "table" and a[key] ~= value then return false end
    end
    return true
end

local function categoryMapEqual(a, b)
    a = type(a) == "table" and a or {}
    b = type(b) == "table" and b or {}
    for categoryId, row in pairs(a) do
        if type(row) ~= "table" or type(b[categoryId]) ~= "table" or not scalarEqual(row, b[categoryId]) then
            return false
        end
    end
    for categoryId in pairs(b) do
        if a[categoryId] == nil then return false end
    end
    return true
end

local function buildSnapshot(site, definition)
    local stock = type(site.stock) == "table" and site.stock or nil
    if not stock or stock.complete ~= true then return nil, "verified stock unavailable" end

    local population = livingPopulation(site)
    local rows = {}
    for supplyId, policy in pairs(reservePolicy(definition)) do
        local categoryId = tostring(policy.category or supplyId)
        if not Categories.IsRegistered(categoryId) then
            return nil, "unregistered supply category: " .. categoryId
        end
        local semantics = Categories.GetQuantitySemantics(categoryId)
        if not semantics then return nil, "quantity semantics unavailable: " .. categoryId end

        local perResident = math.max(0, tonumber(policy.minimumPerResident) or 0)
        local reserve = math.max(0, tonumber(policy.reserve) or 0)
        local target = targetQuantity(categoryId, (population * perResident) + reserve)
        local available = normalizeQuantity(categoryId, Stock.GetCategoryQuantity(site, categoryId))
        local deficit = normalizeQuantity(categoryId, math.max(0, target - available))
        local surplus = normalizeQuantity(categoryId, math.max(0, available - target))
        rows[supplyId] = {
            supplyId = tostring(supplyId),
            category = categoryId,
            unitKind = semantics.unitKind,
            precision = semantics.precision,
            splittable = semantics.splittable == true,
            available = available,
            target = target,
            deficit = deficit,
            surplus = surplus,
            coverage = coverageRatio(categoryId, available, target),
            status = statusFor(categoryId, available, target),
            minimumPerResident = perResident,
            reserve = reserve,
        }
    end

    return {
        schemaVersion = 2,
        livingPopulation = population,
        sourceStockRevision = normalizeRevision(stock.revision),
        sourceStockWorldHours = tonumber(stock.verifiedWorldHours) or tonumber(stock.scannedWorldHours) or 0,
        categories = rows,
    }
end

function Economy.Ensure(site)
    if type(site) ~= "table" then return nil end
    if type(site.economy) ~= "table" then
        site.economy = {
            schemaVersion = 2,
            revision = 0,
            categories = {},
        }
    end
    site.economy.schemaVersion = 2
    site.economy.revision = normalizeRevision(site.economy.revision)
    site.economy.categories = type(site.economy.categories) == "table" and site.economy.categories or {}
    return site.economy
end

function Economy.Refresh(site, definition)
    if type(site) ~= "table" or type(definition) ~= "table" then
        return false, "invalid economy input"
    end
    local nextSnapshot, err = buildSnapshot(site, definition)
    if not nextSnapshot then return false, err end

    local economy = Economy.Ensure(site)
    local changed = normalizeRevision(economy.livingPopulation) ~= nextSnapshot.livingPopulation
        or normalizeRevision(economy.sourceStockRevision) ~= nextSnapshot.sourceStockRevision
        or not categoryMapEqual(economy.categories, nextSnapshot.categories)

    if changed then
        economy.livingPopulation = nextSnapshot.livingPopulation
        economy.sourceStockRevision = nextSnapshot.sourceStockRevision
        economy.sourceStockWorldHours = nextSnapshot.sourceStockWorldHours
        economy.categories = nextSnapshot.categories
        economy.revision = normalizeRevision(economy.revision) + 1
        economy.changedWorldHours = worldHours()
        Sites.MarkDirty(site.siteId, "settlement economy snapshot changed")
    end
    economy.lastEvaluatedWorldHours = worldHours()
    return true, changed
end

function Economy.GetSupply(siteOrId, supplyId)
    local site = type(siteOrId) == "table" and siteOrId or Sites.GetSite(siteOrId)
    local rows = site and site.economy and site.economy.categories or nil
    local row = type(rows) == "table" and rows[supplyId] or nil
    return type(row) == "table" and row or nil
end

function Economy.ListSupplies(siteOrId)
    local site = type(siteOrId) == "table" and siteOrId or Sites.GetSite(siteOrId)
    local out = {}
    for _, row in pairs(site and site.economy and site.economy.categories or {}) do
        if type(row) == "table" then out[#out + 1] = row end
    end
    table.sort(out, function(a, b) return tostring(a.supplyId) < tostring(b.supplyId) end)
    return out
end

LCCQF.FactionSiteEconomy = Economy
return Economy
