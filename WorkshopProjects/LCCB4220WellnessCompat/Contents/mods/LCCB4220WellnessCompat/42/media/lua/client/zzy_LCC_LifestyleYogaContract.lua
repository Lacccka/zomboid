-- LCC B42.20 Wellness Compatibility
-- Runtime contract check for the split-owned Yoga CustomPerk declaration.
--
-- B42 CustomPerk keeps its parent as an ID until the custom-perk pipeline
-- resolves it. We intentionally let that normal pipeline finish, then verify
-- that our UI-only Yoga proxy exists under Lifestyle. If the contract changes
-- after a game/mod update, disable only the LCC Yoga UI feature.

local Guard = require "LCC/Guard"
local FEATURE = "lifestyle.yoga-progress-ui"

local function getPerkId(perk)
    if not perk then return nil end
    if perk.getId then
        return perk:getId()
    end
    return nil
end

local function validateYogaContract()
    if not Guard.isEnabled(FEATURE) then return end

    if not Perks or not Perks.Yoga then
        Guard.disable(FEATURE, "LCC Yoga CustomPerk was not registered by the B42 custom-perk pipeline")
        return
    end

    local yoga = Perks.Yoga
    if not yoga.getParent then
        Guard.disable(FEATURE, "LCC Yoga CustomPerk does not expose getParent()")
        return
    end

    local parent = yoga:getParent()
    if not parent then
        Guard.disable(FEATURE, "LCC Yoga CustomPerk parent was not resolved")
        return
    end

    local parentId = getPerkId(parent)
    local parentMatches = parentId == "Lifestyle"
    if not parentMatches and Perks.Lifestyle then
        parentMatches = parent == Perks.Lifestyle
    end

    if not parentMatches then
        Guard.disable(
            FEATURE,
            "LCC Yoga CustomPerk resolved to unexpected parent: " .. tostring(parentId or parent)
        )
        return
    end

    print("[LCC][Wellness] Yoga CustomPerk contract OK: parent=Lifestyle")
end

if Events and Events.OnGameStart then
    Events.OnGameStart.Add(validateYogaContract)
else
    validateYogaContract()
end
