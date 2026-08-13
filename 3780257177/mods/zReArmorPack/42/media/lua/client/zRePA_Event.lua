local oldWearClothing = ISWearClothing.complete
local oldCreateItemNew = ISClothingExtraAction.createItemNew

LuaEventManager.AddEvent("zReOnClothingEquipped")

function ISWearClothing:complete()
    local result = oldWearClothing(self)
    LuaEventManager.triggerEvent("zReOnClothingEquipped", self.item)
    return result
end

function ISClothingExtraAction:createItemNew(item, newItem)
    local result = oldCreateItemNew(self, item, newItem)
    LuaEventManager.triggerEvent("zReOnClothingEquipped", newItem)
    return result
end