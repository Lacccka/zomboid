local HDCP_IVP_IsPlayerInside = {}

function HDCP_IVP_IsPlayerInside.new()
    local module = {}

    module.isSatisfiedBy = function(context)
        if not context.player:isOutside() then
            return true, nil
        end

        return false, {
            code = "GO_TO_GARAGE"
        }
    end

    return module
end

return HDCP_IVP_IsPlayerInside
