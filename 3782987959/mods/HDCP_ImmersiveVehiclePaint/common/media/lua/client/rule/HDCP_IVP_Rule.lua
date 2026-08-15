local HDCP_IVP_Rule = {}

HDCP_IVP_Rule.__index = HDCP_IVP_Rule

function HDCP_IVP_Rule:new(specification)
    return setmetatable({
        specification = specification,
    }, self)
end

function HDCP_IVP_Rule:evaluate(context)
    return self.specification.isSatisfiedBy(context)
end

return HDCP_IVP_Rule
