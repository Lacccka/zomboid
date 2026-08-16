local HDCP_IVP_HasPaintSpray = {}

function HDCP_IVP_HasPaintSpray.new()
    local module = {}

    module.isSatisfiedBy = function(context)
        if #context.paintOptions > 0 then
            return true, nil
        end

        return false, {
            code = "NO_PAINT"
        }
    end

    return module
end

return HDCP_IVP_HasPaintSpray
