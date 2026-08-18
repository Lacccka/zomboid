-- local old_render = ISPlace3DItemCursor.render

-- function ISPlace3DItemCursor:render(x, y, z, square)
--     local item = self.items[1]
--     if instanceof(item, "HandWeapon") then
--         if item:isRanged() then
--             local modData = item:getModData().weaponpart
--             for k, v in pairs(modData) do
--                 local RealPart = item:getWeaponPart(k, true)
--                 if not RealPart then
--                     local Part = instanceItem(v)
--                     if Part and instanceof(Part, "WeaponPart") then
--                         item:setWeaponPart(k, Part, true, true)
--                     end
--                 end
--             end

--         end
--     end
--     old_render(self, x, y, z, square)

-- end

-- MFS update adding safeguard

if isServer() then return end

require "BuildingObjects/ISPlace3DItemCursor"

local old_render = ISPlace3DItemCursor.render

function ISPlace3DItemCursor:render(x, y, z, square)
    local item = self.items and self.items[1]

    if item and instanceof(item, "HandWeapon") and item:isRanged() then
        local modData = item:getModData().weaponpart

        if type(modData) == "table" then
            for k, v in pairs(modData) do
                local RealPart = item:getWeaponPart(k, true)
                if not RealPart then
                    local Part = instanceItem(v)
                    if Part and instanceof(Part, "WeaponPart") then
                        item:setWeaponPart(k, Part, true, true)
                    end
                end
            end
        end
    end

    old_render(self, x, y, z, square)
end
