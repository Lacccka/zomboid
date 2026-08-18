-- MFS installs this UI override from a server directory. On a dedicated
-- server the cursor class does not exist. This later-mod file replaces it.
if isServer() then return end

local Guard = require "LCC/Guard"
local FEATURE = "mfs.place-3d-weapon-parts"

local function patchRender(self)
    local item = self and self.items and self.items[1]
    if not item or not instanceof(item, "HandWeapon") or not item:isRanged() then
        return
    end

    local itemModData = item:getModData()
    local modData = itemModData and itemModData.weaponpart
    if type(modData) ~= "table" then return end

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

if type(ISPlace3DItemCursor) ~= "table" or type(ISPlace3DItemCursor.render) ~= "function" then
    Guard.disable(FEATURE, "ISPlace3DItemCursor.render is unavailable")
    return
end

Guard.install {
    id = FEATURE,
    validate = function()
        return type(ISPlace3DItemCursor.render) == "function", "ISPlace3DItemCursor.render is unavailable"
    end,
    install = function()
        if ISPlace3DItemCursor.__LCCWeaponPartRenderFix then return end

        local oldRender = ISPlace3DItemCursor.render

        function ISPlace3DItemCursor:render(x, y, z, square)
            if Guard.isEnabled(FEATURE) then
                Guard.protect(FEATURE, "weapon-part render prehook", patchRender, self)
            end

            -- Do not hide failures from the real cursor renderer.
            return oldRender(self, x, y, z, square)
        end

        ISPlace3DItemCursor.__LCCWeaponPartRenderFix = true
    end,
}
