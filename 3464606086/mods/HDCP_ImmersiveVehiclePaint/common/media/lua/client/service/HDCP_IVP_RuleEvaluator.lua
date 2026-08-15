local HDCP_IVP_RuleEvaluator = {}

local tableInsert = table.insert

function HDCP_IVP_RuleEvaluator.evaluate(rules, context)
    local result = {
        available = true,
        failures = {},
        context = context
    }

    for _, rule in ipairs(rules) do
        local ok, failure = rule:evaluate(context)

        if not ok then
            result.available = false

            tableInsert(result.failures, failure)
        end
    end

    return result
end

return HDCP_IVP_RuleEvaluator
