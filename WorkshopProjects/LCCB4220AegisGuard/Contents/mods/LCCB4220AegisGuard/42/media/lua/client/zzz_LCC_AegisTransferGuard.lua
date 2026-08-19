local Guard = require "LCC/Guard"
local FEATURE = "aegis.inventory-transfer"

Guard.safeRequire(FEATURE, "TimedActions/ISInventoryTransferAction")
if not Guard.isEnabled(FEATURE) then return end

Guard.install {
    id = FEATURE,
    validate = function()
        if type(ISInventoryTransferAction) ~= "table" then
            return false, "ISInventoryTransferAction is unavailable"
        end
        if type(ISInventoryTransferAction.isValid) ~= "function" then
            return false, "ISInventoryTransferAction.isValid is unavailable"
        end
        return true
    end,
    install = function()
        if ISInventoryTransferAction.__LCCAegisTransferGuard then return end

        local originalIsValid = ISInventoryTransferAction.isValid

        function ISInventoryTransferAction:isValid()
            if Guard.isEnabled(FEATURE) then
                local ok, invalid = Guard.protect(FEATURE, "nil-container precheck", function()
                    return not self or not self.item or not self.srcContainer or not self.destContainer
                end)
                if ok and invalid then
                    return false
                end
            end

            -- Upstream/Aegis errors remain visible: only our precheck is guarded.
            return originalIsValid(self)
        end

        ISInventoryTransferAction.__LCCAegisTransferGuard = true
    end,
}
