-- B42.19+ moved the recipe-magazine callback to ItemCodeOnCreate.
-- Keep old item scripts working without replacing a callback restored upstream.
SpecialLootSpawns = SpecialLootSpawns or {}

if not SpecialLootSpawns.OnCreateRecipeMagazine then
    SpecialLootSpawns.OnCreateRecipeMagazine = function(...)
        if ItemCodeOnCreate and ItemCodeOnCreate.onCreateRecipeMagazine then
            return ItemCodeOnCreate.onCreateRecipeMagazine(...)
        end
    end
end

return SpecialLootSpawns
