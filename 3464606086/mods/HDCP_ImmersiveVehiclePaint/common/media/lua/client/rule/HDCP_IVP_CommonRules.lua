local Rule                            = require('rule/HDCP_IVP_Rule')
local HasSandingBlockFactory          = require('specification/HDCP_IVP_HasSandingBlock')
local HasEnoughMaterialFactory        = require('specification/HDCP_IVP_HasEnoughMaterial')
local HasPaintSprayFactory            = require('specification/HDCP_IVP_HasPaintSpray')
local PlayerHasAnyKitFactory          = require('specification/HDCP_IVP_PlayerHasAnyKit')
local IsVehicleInGoodConditionFactory = require('specification/HDCP_IVP_IsVehicleInGoodCondition')
local IsPlayerInsideFactory           = require('specification/HDCP_IVP_IsPlayerInside')
local IsVehicleCleanFactory           = require('specification/HDCP_IVP_IsVehicleClean')

local HDCP_IVP_CommonRules            = {}

function HDCP_IVP_CommonRules.new(deps)
    local PlayerHasAnyKit          = deps and deps.PlayerHasAnyKit or PlayerHasAnyKitFactory.new()
    local IsVehicleInGoodCondition = deps and deps.IsVehicleInGoodCondition or IsVehicleInGoodConditionFactory.new()
    local IsPlayerInsideSpec       = deps and deps.IsPlayerInside or IsPlayerInsideFactory.new()
    local IsVehicleCleanSpec       = deps and deps.IsVehicleClean or IsVehicleCleanFactory.new()

    local module                   = {}

    local function withSpecificRule(specificRule, commonRules)
        local rules = { specificRule }

        for i = 1, #commonRules do
            rules[#rules + 1] = commonRules[i]
        end

        return rules
    end

    local function buildCommon()
        return {
            Rule:new(PlayerHasAnyKit),
            Rule:new(IsVehicleInGoodCondition),
            Rule:new(IsPlayerInsideSpec),
            Rule:new(IsVehicleCleanSpec)
        }
    end

    function module.buildSandRules()
        local hasSandingBlock = deps and deps.HasSandingBlock or HasSandingBlockFactory.new()

        local specificRule = Rule:new(hasSandingBlock)

        return withSpecificRule(specificRule, buildCommon())
    end

    function module.buildPrimeRules()
        local hasEnoughMaterial = deps and deps.HasEnoughMaterial or HasEnoughMaterialFactory.new()

        local specificRule = Rule:new(hasEnoughMaterial)

        return withSpecificRule(specificRule, buildCommon())
    end

    function module.buildPaintRules()
        local hasPaintSpray = deps and deps.HasPaintSpray or HasPaintSprayFactory.new()

        local specificRule = Rule:new(hasPaintSpray)

        return withSpecificRule(specificRule, buildCommon())
    end

    function module.buildSubPaintRules()
        local hasEnoughMaterial = deps and deps.HasEnoughMaterial or HasEnoughMaterialFactory.new()

        return {
            Rule:new(hasEnoughMaterial)
        }
    end

    return module
end

return HDCP_IVP_CommonRules
