# NPCCombatExperimental upstream working copy

This directory is development-only and is deliberately outside `Contents`.

`Bandits/42.20/media/lua/client/BanditUpdate.lua` is the tracked upstream B42.20 baseline used for controlled compatibility experiments. It is intentionally kept byte-for-byte identical to the repository's current upstream Bandits snapshot before an experiment is materialized, so we never test against a reconstructed or partially copied source file.

## Prepare the current experimental file

From the repository root run:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\Apply-BanditsAttackBridgePoC.ps1
```

With no `-TargetFile`, the script now modifies this DevUpstream `BanditUpdate.lua` in the local checkout. For the current `upstream-pursuit-v1` experiment it adds the runtime marker and replaces only the audited close-range `spotted/addAggro/setTarget/setAttackedBy` bridge with `zombie:pathToCharacter(bandit)`.

The resulting file to transfer manually is:

`WorkshopPatches/NPCCombatExperimental/DevUpstream/Bandits/42.20/media/lua/client/BanditUpdate.lua`

Copy it over the actual test Bandits file at:

`<Bandits mod>/42.20/media/lua/client/BanditUpdate.lua`

To return the local working copy to its exact upstream baseline:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\Apply-BanditsAttackBridgePoC.ps1 -Revert
```

The same applicator can still patch an explicitly supplied live file with `-TargetFile` when needed.

## Publication boundary

`DevUpstream` is outside `Contents` and is not part of the source-clean Workshop package. Do not move this upstream file under `Contents/mods/LaccckaB4220NPCCombatExperimental`.

When upstream Bandits updates, refresh this baseline from the new 42.20 source first, then carry forward only the compatibility experiment that is still required.
