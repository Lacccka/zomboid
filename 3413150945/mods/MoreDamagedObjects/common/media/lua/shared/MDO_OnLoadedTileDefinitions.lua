-- From "More Damaged Objects [B42]" mod -- Author = carlesturo

-- ------------------------------------------------------------------------------------------------

-- **************** MORE DAMAGED OBJECTS - ON LOADED TILE DEFINITIONS ****************

Events.OnLoadedTileDefinitions.Add(function(manager)

    local vehicleDamageSprites = {
        "crafted_04_122",
        "crafted_04_123",
        "crafted_04_124",
        "crafted_04_125",
        "crafted_04_126",
        "crafted_04_127",
    }

    for _, sprite in ipairs(vehicleDamageSprites) do
        local props = manager:getSprite(sprite):getProperties()
        props:set("HitByCar", "")
        props:set("MinimumCarSpeedDmg", "10")
        props:CreateKeySet()
    end


    local noSolidTransSprites = {
        "location_business_machinery_01_0",
        "location_business_machinery_01_2",
        "location_business_machinery_01_11",
        "location_business_machinery_01_13",
        "location_business_office_generic_01_48",
        "location_business_office_generic_01_49",
        "location_business_office_generic_01_56",
        "location_business_office_generic_01_57",
        "location_business_office_generic_01_58",
        "location_business_office_generic_01_59",
        "location_business_office_generic_01_60",
        "location_business_office_generic_01_61",
    }

    for _, sprite in ipairs(noSolidTransSprites) do
        local props = manager:getSprite(sprite):getProperties()
        props:unset(IsoFlagType.solidtrans)
        props:CreateKeySet()
    end

end)