local HDCP_IVP_DecimalToFractionFormatter = {}

local function gcd(a, b)
    return b == 0 and a or gcd(b, a % b)
end

local function decimalToFraction(decimal)
    local tolerance = 1e-9
    local numerator, denominator = 1, 1

    while math.abs(numerator / denominator - decimal) > tolerance do
        if numerator / denominator < decimal then
            numerator = numerator + 1
        else
            denominator = denominator + 1
            numerator = math.floor(decimal * denominator + 0.5)
        end
    end

    local divisor = gcd(numerator, denominator)
    numerator = numerator / divisor
    denominator = denominator / divisor

    return string.format("%i/%i", numerator, denominator)
end

function HDCP_IVP_DecimalToFractionFormatter.format(availableCans)
    local wholePart = math.floor(availableCans)

    local decimalPart = availableCans - wholePart

    if decimalPart == 0 then return wholePart end

    local fraction = decimalToFraction(decimalPart)

    if wholePart == 0 then return fraction end

    return string.format("%s %s %s", wholePart, getText("Tooltip_IVP_and"), fraction)
end

return HDCP_IVP_DecimalToFractionFormatter
