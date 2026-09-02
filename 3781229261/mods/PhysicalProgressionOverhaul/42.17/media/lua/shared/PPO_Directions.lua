-- The mod's most basic domain fact: there are two directions, they are called
-- Strength and Fitness, and each answers to one vanilla perk. Nine modules held
-- the list, four resolved the perk by hand and four validated a name by hand --
-- thirteen places that had to agree about a fact with two values in it.
--
-- Nothing here reads state or config. A third direction would be a design
-- decision, not an edit to this file, but if one ever happened this is the file
-- that says so.

PPO = PPO or {}
PPO.Directions = PPO.Directions or {}

local Directions = PPO.Directions

-- A fresh table every call, which is the same contract PPO.Config's resolvers
-- give: a module holding this as its own constant cannot have it mutated out
-- from under nine other modules. Callers bind it once at load, exactly as they
-- did when each of them wrote the literal out.
function Directions.order()
    return { "Strength", "Fitness" }
end

-- Which direction a vanilla perk belongs to, or nil for any other perk. `Perks`
-- is absent in some loaders, so it is checked rather than assumed: an
-- unreadable perk table means "not one of ours", which is the safe answer.
function Directions.forPerk(perk)
    if Perks == nil then return nil end
    if perk == Perks.Strength then return "Strength" end
    if perk == Perks.Fitness then return "Fitness" end
    return nil
end

-- Whether a name is one of the two. Callers use this to refuse a direction they
-- were handed rather than to discover one.
function Directions.supported(name)
    return name == "Strength" or name == "Fitness"
end

return Directions
