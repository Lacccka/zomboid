require "Compat/PPO_Compat"

-- Shared by every support file for a mod that pays Strength or Fitness XP of
-- its own while the character is exercising.
--
-- PPO proves a repetition by matching one token against one `Events.AddXP`
-- notification. A foreign award inside the same repetition arrives as a second
-- notification the token cannot use. The evidence queue holds two entries, an
-- arrival past that evicts the oldest and counts as excess, and three excesses
-- inside ten seconds close the session -- so a mod paying one extra award per
-- repetition silently dropped the character back to vanilla rates about four
-- repetitions into every set, with nothing in the log to say why.
--
-- The rule this file applies: a repetition can be proved once per direction.
-- While a direction already holds an unclaimed proof, another positive award in
-- that same direction is somebody else's and is not evidence. It is not
-- refused, hidden or reverted -- the character keeps every point of it -- it
-- simply does not get to speak for a repetition that is already proved.
--
-- Deliberately not gated on a declared mod. The rule is correct whatever pays
-- the extra award, and a whitelist would leave the next such mod broken in a
-- way that takes a live session to find. The folders beside this one record
-- which real mods it was written for.

PPO = PPO or {}
PPO.Compat = PPO.Compat or {}
PPO.Compat.Shared = PPO.Compat.Shared or {}
PPO.Compat.Shared.ForeignXp = PPO.Compat.Shared.ForeignXp or {}

local ForeignXp = PPO.Compat.Shared.ForeignXp

-- True when this award cannot be the repetition's own proof, because the
-- direction already holds one. An unreadable matcher answers false: the safe
-- direction is to let core decide, which is what happens without this file.
function ForeignXp.isEcho(matcher, playerKey, component)
    if PPO.RepetitionMatcher == nil
            or PPO.RepetitionMatcher.hasEvidence == nil then
        return false
    end
    local ok, pending = pcall(PPO.RepetitionMatcher.hasEvidence,
        matcher, playerKey, component)
    if not ok then return false end
    return pending == true
end
