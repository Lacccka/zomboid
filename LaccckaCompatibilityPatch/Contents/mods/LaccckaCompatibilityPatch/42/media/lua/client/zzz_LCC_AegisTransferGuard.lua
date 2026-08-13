require "TimedActions/ISInventoryTransferAction"

-- Copy vanilla's nil-container guard before Aegis repeats the Java check.
local aegisIsValid = ISInventoryTransferAction.isValid

function ISInventoryTransferAction:isValid()
    if not self.item or not self.srcContainer or not self.destContainer then
        return false
    end
    return aegisIsValid(self)
end

