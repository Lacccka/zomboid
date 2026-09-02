require "PPO_Num"

-- How a state report names the character it belongs to, and how the client
-- decides which loaded character a report is for. The two have to read the seam
-- the same way or a report is silently delivered to nobody, so the reading lives
-- in one place rather than once per network side.
--
-- Both return nil rather than a sentinel when the seam is absent or refuses:
-- `PPO.StateReport` omits the field and `PPO.ClientRuntime` falls through to the
-- next rule, which is what an unreadable identity should cost.

PPO = PPO or {}
PPO.Identity = PPO.Identity or {}

local Identity = PPO.Identity
local Num = PPO.Num

-- Vanilla returns -1 for a character with no network identity, which is every
-- character in single player.
function Identity.onlineID(character)
    if character == nil or type(character.getOnlineID) ~= "function" then
        return nil
    end
    local ok, value = pcall(character.getOnlineID, character)
    if not ok then return nil end
    value = Num.finite(value, -1)
    if value < 0 then return nil end
    return value
end

function Identity.username(character)
    if character == nil or type(character.getUsername) ~= "function" then
        return nil
    end
    local ok, value = pcall(character.getUsername, character)
    if not ok or type(value) ~= "string" or value == "" then return nil end
    return value
end

return Identity
