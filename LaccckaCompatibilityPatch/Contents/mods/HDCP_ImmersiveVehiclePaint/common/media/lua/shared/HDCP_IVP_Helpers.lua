HDCP_IVP_Helpers = HDCP_IVP_Helpers or {}

local SERVER_DEBUG = HDCP_IVP_Constants.SERVER_DEBUG

local MOD_ID = HDCP_IVP_Constants.MOD_ID

local isClientDebug = function()
    return getDebug()
end

local isServerDebug = function()
    return isServer() and SERVER_DEBUG
end

HDCP_IVP_Helpers.noise = function(msg)
    if isClientDebug() or isServerDebug() then
        print(MOD_ID .. ": " .. msg)
    end
end

return HDCP_IVP_Helpers
