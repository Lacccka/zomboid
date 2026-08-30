-- Build 42 can execute this top-level bootstrap in an MP client Lua environment during
-- world/global-mod-data initialization. Faction world state is server authority only.
if isClient and isClient() and not (isServer and isServer()) then return false end

require "LCCQF/Faction/zz_LCCQFFactionDiscoveryBridge"
require "LCCQF/Faction/zz_LCCQFFactionRelationshipBridge"
require "LCCQF/FactionWorld/zz_LCCQFFactionSiteAllocator"
require "LCCQF/FactionWorld/zz_LCCQFFactionSiteValidationService"
require "LCCQF/Runtime/LCCQFBanditsFactionSiteMaterializer"
require "LCCQF/Runtime/LCCQFBanditsFactionSiteLifecycle"
require "LCCQF/Runtime/LCCQFBanditsFactionRelocationRetirement"
require "LCCQF/Runtime/LCCQFBanditsFactionOperationsProjection"
require "LCCQF/FactionWorld/LCCQFFactionNPCBridge"

-- Relocation runs before materialization so a validated replacement can inherit the
-- old logical population before provider spawn begins.
require "LCCQF/FactionWorld/zz_LCCQFFactionSiteRelocationService"
-- Operations assignment runs before physical materialization. The projection service
-- runs afterwards, so newly spawned provider actors receive already-persisted duty.
require "LCCQF/FactionWorld/zz_LCCQFFactionSiteOperationsService"
require "LCCQF/FactionWorld/zz_LCCQFFactionSiteMaterializationService"
require "LCCQF/FactionWorld/zz_LCCQFFactionSiteLifecycleService"
require "LCCQF/FactionWorld/zz_LCCQFFactionSiteOperationsProjectionService"
-- Generated logical NPC definitions exist before this projection. This provider-only
-- service exposes only physically materialized residents to the common interaction runtime.
require "LCCQF/Runtime/LCCQFBanditsFactionNPCProjection"
require "LCCQF/FactionWorld/zz_LCCQFFactionSitePopulationMaintenance"
require "LCCQF/FactionWorld/zz_LCCQFFactionSiteHealthService"
require "LCCQF/FactionWorld/LCCQFFactionSiteDebugServer"

print("[LCCQF][SERVER] faction bootstrap loaded discovery=true relationships=true siteAllocator=location-only siteValidation=resources population=logical lifecycle=reconcile+virtualize+rematerialize maintenance=replacements relocation=identity-preserving health=loaded-world operations=jobs+schedules+needs+signals generatedNPCs=public-projection materializer=Bandits guard=duty-aware debug=privileged")
return true
