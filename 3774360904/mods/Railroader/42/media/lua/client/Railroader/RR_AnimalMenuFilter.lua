--***********************************************************************
-- Railroader / RR_AnimalMenuFilter  -- hide the vanilla animal context menu on our animals
--
-- The loco (rr_loco) and its collider segments (rr_collider) are REAL IsoAnimals,
-- so right-clicking the locomotive pops the vanilla livestock menu -- Feed, Water,
-- Kill, Pet, Info, Attach, and every debug entry -- built per-animal by
-- AnimalContextMenu.doMenu (see ISUI/Animal/ISAnimalContextMenu.lua), fed from the
-- OnClickedAnimalForContext event. That menu is meaningless (and immersion-breaking)
-- on a diesel engine.
--
-- doMenu is the ONE funnel every world-object animal submenu passes through, so we
-- wrap it and bail out for any animal whose type carries our "rr_" prefix (rr_loco,
-- rr_collider). No header, no submenu, nothing gets added -- the loco simply has no
-- animal section in its right-click menu. Everything NOT ours falls through to the
-- original untouched.
--***********************************************************************

print("[Railroader] RR_AnimalMenuFilter.lua: loading...")

local OWN_PREFIX = "rr_"   -- ours: rr_loco (visual), rr_truck (bogies), rr_collider (segments)

local function isOurs(animal)
    if not animal then return false end
    local t
    pcall(function() t = animal:getAnimalType() end)
    return t ~= nil and string.sub(t, 1, #OWN_PREFIX) == OWN_PREFIX
end

if AnimalContextMenu and AnimalContextMenu.doMenu then
    local origDoMenu = AnimalContextMenu.doMenu
    AnimalContextMenu.doMenu = function(player, context, animal, test)
        if isOurs(animal) then
            -- Add NOTHING as a rule -- except the entries that belong on the locomotive
            -- itself. This is the hook with the best reach we have on the body: the engine
            -- collects clicked animals from the 3x3 around the clicked square, and the hull is
            -- lined with rr_collider segments, so a click anywhere along the body lands on one
            -- -- where the world-object path only sees the ground square the cursor projects
            -- onto (and the hull is drawn ~4.5 tiles tall).
            --
            --   * "Climb into the cab" / "Leave the cab" (RR_BoardMenu) -- the way in for a
            --     player who is not on a keyboard, and the obvious thing to look for when you
            --     right-click a locomotive.
            --   * "Put it back on the rails" (RR_RerailMenu) -- silent unless the game runs
            --     with `-debug` AND the animal is part of a derailed loco.
            if RR and RR.BoardMenu and RR.BoardMenu.addForAnimal then
                pcall(function() RR.BoardMenu.addForAnimal(player, context, animal, test) end)
            end
            if RR and RR.RerailMenu and RR.RerailMenu.addForAnimal then
                pcall(function() RR.RerailMenu.addForAnimal(player, context, animal, test) end)
            end
            return
        end
        return origDoMenu(player, context, animal, test)
    end
else
    print("[Railroader] RR_AnimalMenuFilter: AnimalContextMenu.doMenu missing -- vanilla menu NOT filtered")
end
