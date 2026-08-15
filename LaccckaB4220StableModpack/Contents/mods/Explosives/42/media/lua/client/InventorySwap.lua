Events.OnFillContainer.Add(function(roomName, containerType, container)
    if not container then return end

    -- Skip ItemPickerContainer previews (used for backpack icon rendering, NPC inventories, etc.)
    -- getItems() throws on this container type; check the type string first to avoid the exception entirely.
    if tostring(container):find("ItemPickerContainer") then return end

    local items = container:getItems()
    for i = items:size() - 1, 0, -1 do
        local item = items:get(i)
        if item:getType() == "GrenadeBandolierFull" then
            container:Remove(item)
            container:AddItem("Explosives.GrenadeBandolier")

            container:AddItem("Explosives.M67Grenade")
            container:AddItem("Explosives.M67Grenade")
            container:AddItem("Explosives.M67Grenade")

            for extra = 1, 2 do
                if ZombRand(100) < 50 then
                    local roll = ZombRand(100)
                    if roll < 50 then
                        container:AddItem("Explosives.M26Grenade")
                    elseif roll < 80 then
                        container:AddItem("Explosives.Mk2Grenade")
                    else
                        container:AddItem("Explosives.M14TH3Grenade")
                    end
                end
            end
        end
    end
end)