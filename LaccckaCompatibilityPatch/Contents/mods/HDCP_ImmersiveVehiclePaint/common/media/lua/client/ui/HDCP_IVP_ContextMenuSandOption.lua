local OnSandVehicleFactory           = require('event/HDCP_IVP_OnSandVehicle')
local CommonRulesFactory             = require('rule/HDCP_IVP_CommonRules')
local RuleEvaluator                  = require('service/HDCP_IVP_RuleEvaluator')
local TooltipRendererFactory         = require('service/HDCP_IVP_TooltipRenderer')

local HDCP_IVP_ContextMenuSandOption = {}

function HDCP_IVP_ContextMenuSandOption.new(deps)
    local module            = {}

    local OnSandVehicle     = deps and deps.OnSandVehicle or OnSandVehicleFactory.new()
    local CommonRules       = deps and deps.CommonRules or CommonRulesFactory.new()
    local ObjectContextMenu = deps and deps.ISWorldObjectContextMenu or ISWorldObjectContextMenu
    local RendererFactory   = deps and deps.TooltipRendererFactory or TooltipRendererFactory

    module.add              = function(contextMenu, player, vehicle)
        local option = contextMenu:addOption(
            getText("ContextMenu_IVP_Sand"), player, OnSandVehicle.handle, vehicle
        )

        local rules = CommonRules.buildSandRules()

        local context = { player = player, vehicle = vehicle }

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

return HDCP_IVP_ContextMenuSandOption
