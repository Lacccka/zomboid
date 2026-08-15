local GetVehiclePartsFactory     = require('service/HDCP_IVP_GetVehicleParts')
local DecimalToFractionFormatter = require('service/HDCP_IVP_DecimalToFractionFormatter')
local TooltipDescriptionBuilder  = require('service/HDCP_IVP_TooltipDescriptionBuilder')

local HDCP_IVP_TooltipRenderer   = {}

function HDCP_IVP_TooltipRenderer.new(deps)
    local module = {}

    local GetVehicleParts = deps and deps.GetVehicleParts or GetVehiclePartsFactory.new()
    local builder = deps and deps.TooltipDescriptionBuilder or TooltipDescriptionBuilder.new()

    local function addMissingSandingBlockSection(context)
        builder.addEmptyLine()
        builder.addWhiteText(getText("Tooltip_IVP_NecessaryTools"))
        builder.addLineBreak()
        builder.addRedText(getItemNameFromFullType(context.itemType))
    end

    local function addMissingMaterialSection(context)
        local delta = context.sprayDelta

        local availableCans = context.remainingUses / (1 / delta)
        local requiredCans = context.requiredUses / (1 / delta)

        builder.addEmptyLine()
        builder.addWhiteText(getText("Tooltip_IVP_NecessaryMaterials"))
        builder.addLineBreak()
        builder.addWhiteText(getItemNameFromFullType(context.itemType))

        builder.addEmptyLine()
        builder.addWhiteText(getText("Tooltip_IVP_NeededMaterial"), " %s <SPACE>")
        builder.addYellowText(DecimalToFractionFormatter.format(requiredCans))

        builder.addLineBreak()
        builder.addWhiteText(getText("Tooltip_IVP_AvailableMaterial"), " %s <SPACE>")
        builder.addRedText(DecimalToFractionFormatter.format(availableCans))
    end

    local function addMissingPaintSection()
        builder.addEmptyLine()
        builder.addWhiteText(getText("Tooltip_IVP_NecessaryMaterials"))
        builder.addLineBreak()
        builder.addRedText(getText("Tooltip_IVP_AnyAutomotiveSprayPaint"))
    end

    local function addMissingPPESection(context)
        builder.addEmptyLine()
        builder.addWhiteText(getText("Tooltip_IVP_NeedsPPE"))

        local i = 1

        for _, kit in pairs(context.kits) do
            local j = 1

            for _, kitItem in pairs(kit) do
                local itemName = getItemNameFromFullType(kitItem)

                if j == 1 then
                    builder.addLineBreak()

                    if i > 1 then
                        builder.addWhiteText(getText("Tooltip_IVP_or"), " %s <SPACE>")
                    end

                    builder.addRedText(itemName)
                else
                    builder.addWhiteText(getText("Tooltip_IVP_and"), " <SPACE> %s <SPACE>")
                    builder.addRedText(itemName)
                end

                j = j + 1
            end

            i = i + 1
        end
    end

    local function addBadConditionSection(context)
        local bodyworkRequirement = context.bodyworkRequirement

        builder.addEmptyLine()
        builder.addWhiteText(getText("Tooltip_IVP_NeedsBodywork"))

        local vehicleParts = GetVehicleParts.get(context.vehicle)

        for _, vehiclePart in ipairs(vehicleParts) do
            if vehiclePart.condition < bodyworkRequirement then
                builder.addLineBreak()
                builder.addWhiteText(vehiclePart.name, " %s <SPACE>")
                builder.addRedText(vehiclePart.condition, " (%s%%)")
            end
        end

        builder.addLineBreak()
        builder.addYellowText(getText("Tooltip_IVP_BodyworkInformation", bodyworkRequirement))
    end

    local hasAdicionalRequirementsHeader = false

    local function addAdditionalRequirementsHeader()
        if hasAdicionalRequirementsHeader then return end

        builder.addEmptyLine()
        builder.addWhiteText(getText("Tooltip_IVP_AdditionalRequirements"))

        hasAdicionalRequirementsHeader = true
    end

    local function addGoToGarageSection()
        addAdditionalRequirementsHeader()

        builder.addLineBreak()
        builder.addRedText(getText("Tooltip_IVP_GoToGarage"))
    end

    local function addWashVehicleSection()
        addAdditionalRequirementsHeader()

        builder.addLineBreak()
        builder.addRedText(getText("Tooltip_IVP_WashVehicle"))
    end

    local function applyFailure(failure)
        local failures = {
            NO_TOOL      = addMissingSandingBlockSection,
            NO_MATERIAL  = addMissingMaterialSection,
            NO_PAINT     = addMissingPaintSection,
            NO_PPE       = addMissingPPESection,
            FIX_BODYWORK = addBadConditionSection,
            GO_TO_GARAGE = addGoToGarageSection,
            WASH_VEHICLE = addWashVehicleSection,
        }

        if failures[failure.code] then
            failures[failure.code](failure)
        end
    end

    function module.build(result)
        builder.addWhiteText(getText("Tooltip_IVP_Requirements"))

        for _, failure in ipairs(result.failures) do
            applyFailure(failure)
        end
    end

    function module.get()
        return builder.get()
    end

    return module
end

return HDCP_IVP_TooltipRenderer
