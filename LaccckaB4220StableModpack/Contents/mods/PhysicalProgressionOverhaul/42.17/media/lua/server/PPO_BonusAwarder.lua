require "PPO_BonusMath"
require "PPO_ExerciseState"

PPO = PPO or {}
PPO.BonusAwarder = PPO.BonusAwarder or {}

local Awarder = PPO.BonusAwarder

function Awarder.new(issueXp)
    return {
        issueXp = issueXp,
        internalDepth = 0,
    }
end

function Awarder.isInternal(awarder)
    return awarder ~= nil and awarder.internalDepth > 0
end

-- B42.17 refuses Fitness XP outright through `Nutrition.canAddFitnessXp()`:
-- from Fitness level 6 for an emaciated, obese or very underweight character,
-- and from level 9 for any weight trouble. The refusal is a silent `return` at
-- the top of `AddXP`, so issuing into it raises nothing and reports nothing.
-- PPO has to ask the same question itself, or it charges load minutes and
-- stimulus for a repetition the game discarded. Strength is never gated this
-- way; its nutrition term is a multiplier, not a veto.
--
-- An unreadable seam answers "accepted", because the alternative silently
-- stops all Fitness progression on any character shape this cannot inspect.
function Awarder.vanillaAccepts(character, perk)
    if Perks == nil or perk ~= Perks.Fitness then return true end
    if character == nil or character.getNutrition == nil then return true end
    local read, nutrition = pcall(character.getNutrition, character)
    if not read or nutrition == nil
            or nutrition.canAddFitnessXp == nil then
        return true
    end
    local asked, allowed = pcall(nutrition.canAddFitnessXp, nutrition)
    if not asked then return true end
    return allowed ~= false
end

-- The factor `AddXP` applies to Strength before it reads anything the mod owns,
-- read straight from `zombie.characters.IsoGameCharacter$XP.AddXP` offsets
-- 63-195: `if (50 < proteins && proteins < 300) amount *= 1.5f;` followed by
-- `if (proteins < -300) amount *= 0.7f;`, both guarded by Strength and by the
-- character being a player. Fitness carries no such factor; its nutrition term
-- is the veto `vanillaAccepts` asks about instead.
--
-- An unreadable seam answers 1, which absorbs nothing and leaves today's award
-- untouched. That is the safe direction: the worst case is the hidden bonus the
-- mod has always paid, not XP taken away from a character this cannot inspect.
function Awarder.vanillaFactor(character, perk)
    if Perks == nil or perk ~= Perks.Strength then return 1 end
    if character == nil or character.getNutrition == nil then return 1 end
    local read, nutrition = pcall(character.getNutrition, character)
    if not read or nutrition == nil or nutrition.getProteins == nil then
        return 1
    end
    local asked, proteins = pcall(nutrition.getProteins, nutrition)
    if not asked or type(proteins) ~= "number" or proteins ~= proteins then
        return 1
    end

    if proteins > 50 and proteins < 300 then return 1.5 end
    if proteins < -300 then return 0.7 end
    return 1
end

function Awarder.award(awarder, character, token)
    if awarder == nil or character == nil or type(token) ~= "table"
            or token.awarded then
        return { ok = false, close = false, token = token }
    end

    local awards = {}
    for _, component in ipairs({ "Strength", "Fitness" }) do
        -- A direction vanilla refuses costs nothing: no award, and no stimulus
        -- for work the game did not record.
        local accepted = Awarder.vanillaAccepts(character, Perks[component])
        if not accepted and token.stimulus ~= nil then
            token.stimulus[component] = nil
        end
        local rawXp = token.rawXp and token.rawXp[component]
        local capped = token.capped and token.capped[component]
        local fullMultiplier = token.fullMultiplier
            and token.fullMultiplier[component]
        local bonusReturn = token.bonusReturn and token.bonusReturn[component]
        -- The factor is always passed. Kahlua leaves an unpassed parameter
        -- holding whatever the caller's stack had in that slot, so omitting it
        -- would hand `rawBonus` garbage instead of a default.
        local amount = PPO.BonusMath.rawBonus(
            rawXp, fullMultiplier, bonusReturn, capped,
            Awarder.vanillaFactor(character, Perks[component]))
        if amount > 0 and accepted then
            table.insert(awards, {
                perk = Perks[component],
                amount = amount,
            })
        end
    end

    awarder.internalDepth = awarder.internalDepth + 1
    local issued = true
    for _, award in ipairs(awards) do
        local ok = pcall(
            awarder.issueXp, character, award.perk, award.amount)
        if not ok then
            issued = false
            break
        end
    end
    awarder.internalDepth = math.max(0, awarder.internalDepth - 1)

    if not issued then
        return { ok = false, close = true, token = token }
    end
    if not PPO.ExerciseState.applyAcceptedRepeat(character, token.stimulus) then
        return { ok = false, close = true, token = token }
    end

    token.awarded = true
    return { ok = true, close = false, token = token }
end
