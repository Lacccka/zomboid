-- MFS installs this UI override from a server directory. On a dedicated
-- server the cursor class does not exist. This later-mod file replaces it.
if isServer() then return end
if not ISPlace3DItemCursor or not ISPlace3DItemCursor.render then return end

local oldRender = ISPlace3DItemCursor.render

function ISPlace3DItemCursor:render(x, y, z, square)
    local item = self.items and self.items[1]
    if item and instanceof(item, "HandWeapon") and item:isRanged() then
        local modData = item:getModData().weaponpart
        if type(modData) == "table" then
            for slot, itemType in pairs(modData) do
                local realPart = item:getWeaponPart(slot, true)
                if not realPart then
                    local part = instanceItem(itemType)
                    if part and instanceof(part, "WeaponPart") then
                        item:setWeaponPart(slot, part, true, true)
                    end
                end
            end
        end
    end
    return oldRender(self, x, y, z, square)
end

