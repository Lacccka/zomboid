-- Client OnZombieUpdate spotted-target correction removed for MP performance.
-- Bandits2 compatibility is handled only by the shared Shoot.onComplete patch
-- in EFZ_BanditsSpottedTargetCompat.lua (event-driven, not per-zombie-per-tick).

local PATCH_TAG = "[EFZ_BanditsSpottedTarget_Client]"

if _G.__EFZ_BANDITS_SPOTTED_TARGET_COMPAT_CLIENT_FILE_LOADED then
    return
end
_G.__EFZ_BANDITS_SPOTTED_TARGET_COMPAT_CLIENT_FILE_LOADED = true
DebugLog.log(PATCH_TAG .. " Loaded EFZ_BanditsSpottedTargetCompat_Client.lua (OnZombieUpdate correction removed; shared Shoot patch only)")
