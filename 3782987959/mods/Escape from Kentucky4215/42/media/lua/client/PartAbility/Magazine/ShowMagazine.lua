local function showMagazine(playerObj)
    local weapon = playerObj:getPrimaryHandItem()
    if weapon and weapon:IsWeapon() and weapon:isRanged() then
        local WeaponType = weapon:getType()
        local Loaded = weapon:isContainsClip()
        local Type = weapon:getMagazineType()

        local MagPart, MagPart2, magItem1, magItem2
        if AWCWF_MagazineTypeToPart[Type] then
            MagPart = AWCWF_MagazineTypeToPart[Type]
            magItem1 = ScriptManager.instance:getItem(MagPart)
        else
            MagPart = "Gunpart.Clip_" .. WeaponType
            magItem1 = ScriptManager.instance:getItem(MagPart)
            MagPart2 = "Base.Clip_" .. WeaponType
            magItem2 = ScriptManager.instance:getItem(MagPart2)
        end
        if magItem1 or magItem2 then

            local magItem
            if magItem1 then
                magItem = instanceItem(magItem1)
            elseif magItem2 then
                magItem = instanceItem(magItem2)
            end
            local clip = weapon:getWeaponPart("Clip")
            if Loaded and magItem then
                if not clip or clip:getType() ~= magItem:getType() then
                    weapon:setWeaponPart("Clip", magItem)

                end
            else
                local RealClip = weapon:getWeaponPart("Clip")
                if clip or RealClip then
                    weapon:clearWeaponPart("Clip")

                end
            end
        end
    end
end
Events.OnPlayerUpdate.Add(showMagazine)
