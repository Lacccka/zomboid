local OnPrimeVehicleFactory           = require('event/HDCP_IVP_OnPrimeVehicle')
local CommonRulesFactory              = require('rule/HDCP_IVP_CommonRules')
local RuleEvaluator                   = require('service/HDCP_IVP_RuleEvaluator')
local TooltipRendererFactory          = require('service/HDCP_IVP_TooltipRenderer')

local HDCP_IVP_ContextMenuPrimeOption = {}

function HDCP_IVP_ContextMenuPrimeOption.new(deps)
    local module            = {}

    local Constants         = deps and deps.Constants or require('HDCP_IVP_Constants')
    local OnPrimeVehicle    = deps and deps.OnPrimeVehicle or OnPrimeVehicleFactory.new()
    local CommonRules       = deps and deps.CommonRules or CommonRulesFactory.new()
    local ObjectContextMenu = deps and deps.ISWorldObjectContextMenu or ISWorldObjectContextMenu
    local RendererFactory   = deps and deps.TooltipRendererFactory or TooltipRendererFactory

    module.add              = function(contextMenu, player, vehicle)
        local option = contextMenu:addOption(
            getText("ContextMenu_IVP_Prime"), player, OnPrimeVehicle.handle, vehicle
        )

        local rules = CommonRules.buildPrimeRules()

        local context = {
            player       = player,
            vehicle      = vehicle,
            requiredUses = vehicle:getModData().IVP.requiredPrimerUses,
            itemType     = Constants.ITEMS.AUTOMOTIVE_PRIMER_SPRAY
        }

        local result = RuleEvaluator.evaluate(rules, context)

        if not result.available then
            local renderer = RendererFactory.new()
            renderer.build(result)

            local tooltip = ObjectContextMenu.addToolTip()
            tooltip.description = renderer.get()

            option.notAvailable = true
            option.toolTip = tooltip
        end
    end

    return module
end

return HDCP_IVP_ContextMenuPrimeOption
