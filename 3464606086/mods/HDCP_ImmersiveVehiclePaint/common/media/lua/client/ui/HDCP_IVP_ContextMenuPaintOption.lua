local OnPaintVehicleFactory           = require('event/HDCP_IVP_OnPaintVehicle')
local CommonRulesFactory              = require('rule/HDCP_IVP_CommonRules')
local RuleEvaluator                   = require('service/HDCP_IVP_RuleEvaluator')
local TooltipRendererFactory          = require('service/HDCP_IVP_TooltipRenderer')
local GetItemUsesRemainingFactory     = require('service/HDCP_IVP_GetItemUsesRemaining')

local HDCP_IVP_ContextMenuPaintOption = {}

local tableInsert                     = table.insert

function HDCP_IVP_ContextMenuPaintOption.new(deps)
    local module            = {}

    local Constants         = deps and deps.Constants or require('HDCP_IVP_Constants')
    local ItemUsesRemaining = deps and deps.GetItemUsesRemaining or GetItemUsesRemainingFactory.new()
    local OnPaintVehicle    = deps and deps.OnPaintVehicle or OnPaintVehicleFactory.new()
    local CommonRules       = deps and deps.CommonRules or CommonRulesFactory.new()
    local ObjectContextMenu = deps and deps.ISWorldObjectContextMenu or ISWorldObjectContextMenu
    local ContextMenu       = deps and deps.ISContextMenu or ISContextMenu
    local RendererFactory   = deps and deps.TooltipRendererFactory or TooltipRendererFactory

    local function addPaintOption(contextMenu, context, item, requiredUses)
        local optionPaint = contextMenu:addOption(
            getText("ContextMenu_IVP_" .. item.type),
            context.player,
            OnPaintVehicle.handle,
            context.vehicle,
            item
        )

        local rules = CommonRules.buildSubPaintRules()

        local subContext = {
            player       = context.player,
            requiredUses = requiredUses,
            itemType     = item.type
        }

        local result = RuleEvaluator.evaluate(rules, subContext)

        if not result.available then
            local renderer = RendererFactory.new()
            renderer.build(result)

            local tooltip = ObjectContextMenu.addToolTip()
            tooltip.description = renderer.get()

            optionPaint.notAvailable = true
            optionPaint.toolTip = tooltip
        end
    end

    local function addPaintOptions(contextMenu, context)
        local requiredUses = context.vehicle:getModData().IVP.requiredPaintUses

        for _, entry in pairs(context.paintOptions) do
            addPaintOption(contextMenu, context, entry.item, requiredUses)
        end
    end

    local function getAvailablePaintOptions(player)
        local availablePaintOptions = {}

        for _, entry in ipairs(Constants.ITEMS.SPRAY_PAINT) do
            local usesRemaining = ItemUsesRemaining.get(player, entry.type)

            if usesRemaining > 0 then
                tableInsert(availablePaintOptions, {
                    item = entry,
                    uses = usesRemaining
                })
            end
        end

        return availablePaintOptions
    end

    module.add = function(contextMenu, player, vehicle)
        local option = contextMenu:addOption(getText("ContextMenu_IVP_Paint"), nil, nil)

        local availablePaintOptions = getAvailablePaintOptions(player)

        local rules = CommonRules.buildPaintRules()

        local context = {
            player       = player,
            vehicle      = vehicle,
            paintOptions = availablePaintOptions
        }

        local result = RuleEvaluator.evaluate(rules, context)

        if not result.available then
            local renderer = RendererFactory.new()
            renderer.build(result)

            local tooltip = ObjectContextMenu.addToolTip()
            tooltip.description = renderer.get()

            option.notAvailable = true
            option.toolTip = tooltip
        else
            local subMenuOptions = ContextMenu:getNew(contextMenu)
            contextMenu:addSubMenu(option, subMenuOptions)

            addPaintOptions(subMenuOptions, context)
        end
    end

    return module
end

return HDCP_IVP_ContextMenuPaintOption
