local HDCP_IVP_TooltipDescriptionBuilder = {}

function HDCP_IVP_TooltipDescriptionBuilder.new()
    local description = ""

    local module = {}

    local function addText(format, text)
        description = description .. string.format(format, text)
    end

    function module.addEmptyLine()
        addText("%s", "\n\n")
    end

    function module.addLineBreak()
        addText("%s", "\n")
    end

    function module.addWhiteText(text, format)
        format = format or " %s"
        addText(" <RGB:1,1,1>" .. format, text)
    end

    function module.addRedText(text, format)
        format = format or " %s"
        addText(" <RGB:1,0,0>" .. format, text)
    end

    function module.addYellowText(text, format)
        format = format or " %s"
        addText(" <RGB:1,1,0>" .. format, text)
    end

    function module.get()
        return description
    end

    return module
end

return HDCP_IVP_TooltipDescriptionBuilder
