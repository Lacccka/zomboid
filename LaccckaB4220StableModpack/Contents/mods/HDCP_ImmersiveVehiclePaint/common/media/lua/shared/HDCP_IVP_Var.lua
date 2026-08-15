HDCP_IVP_Var = {}

HDCP_IVP_Var.dump = function(o, indent)
    indent = indent or ""

    if type(o) ~= "table" then
        print(indent .. tostring(o))
        return
    end

    print(indent .. "{")

    for k, v in pairs(o) do
        io.write(indent .. "  [" .. tostring(k) .. "] = ")

        if type(v) == "table" then
            HDCP_IVP_Var.dump(v, indent .. "  ")
        else
            print(tostring(v))
        end
    end

    print(indent .. "}")
end

return HDCP_IVP_Var
