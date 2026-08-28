-- Build 42 can execute this top-level bootstrap in an MP client Lua environment during
-- world/global-mod-data initialization. Faction world state is server authority only.
-- Keep this guard before every server-domain require so a client cannot install faction
-- discovery/reward bridges or the autonomous site allocator into its local Lua state.
if isClient and isClient() and not (isServer and isServer()) then
    return false
end

require "LCCQF/Faction/zz_LCCQFFactionDiscoveryBridge"
require "LCCQF/Faction/zz_LCCQFFactionRelationshipBridge"
require "LCCQF/FactionWorld/zz_LCCQFFactionSiteAllocator"

print("[LCCQF][SERVER] faction bootstrap loaded discovery=true relationships=true siteAllocator=dry-run")

return true
