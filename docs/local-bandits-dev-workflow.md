# Local Bandits development workflow (Build 42.20)

The repository contains a complete Bandits development working copy at:

`WorkshopPatches/Bandits-LCC-Dev`

That folder is the authoritative source for local testing. It keeps the real `id=Bandits2` and may contain the current controlled upstream experiment. The user copies this ready working copy into `%UserProfile%\Zomboid\mods\Bandits-LCC-Dev` for testing.

## Preferred manual workflow

After repository changes:

```powershell
cd C:\zomboid
git pull
```

Delete the previous local test copy and copy the whole folder:

```text
C:\zomboid\WorkshopPatches\Bandits-LCC-Dev
        |
        | complete folder replacement
        v
C:\Users\user\Zomboid\mods\Bandits-LCC-Dev
        |
        | id=Bandits2
        v
Project Zomboid client + dedicated server
```

Do not keep another active local/Steam copy with `id=Bandits2`. Steam Workshop item `3268487204` is intentionally unsubscribed/excluded from this test setup, while `Bandits2` remains in `Mods=`.

For deterministic development loading, both client and dedicated server should use:

```text
-modfolders mods,workshop,steam
```

## Current AttackState experiment

The ready repository working copy currently contains:

```text
upstream-coordinate-pursuit-v2
```

Runtime proof:

```text
[LCC][BanditsAttackPoC][INIT] upstream-coordinate-pursuit-v2 active; character pursuit and vanilla target bridge disabled
```

The matching `NPCCombatExperimental` build must also be refreshed. Its guard should report:

```text
[LCC][BanditsAttackGuard][UPSTREAM_POC_ACTIVE] marker=upstream-coordinate-pursuit-v2 mode=observe-only v3Disconnect=false targetProtection=false
```

If either marker is missing, do not interpret the combat test: the client is not running the intended paired experiment.

## Optional setup script

Manual copying is the normal workflow. `tools/Setup-LocalBanditsDev.ps1` remains available when a deterministic mirror/update of the local folder is useful:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\Setup-LocalBanditsDev.ps1
```

By default it mirrors the **already-prepared** `WorkshopPatches\Bandits-LCC-Dev` working copy. It does not apply another source transformation after copying. This keeps the local file identical to the version reviewed and committed in the repository.

The script also:

1. validates `id=Bandits2`;
2. mirrors with `robocopy /MIR`;
3. verifies the current PoC marker in the copied `BanditUpdate.lua`;
4. checks that `Mods=` contains `Bandits2`;
5. removes Workshop item `3268487204` from `WorkshopItems=` if necessary;
6. scans `Zomboid\mods` and `Zomboid\Workshop` for a second `id=Bandits2` and refuses an ambiguous setup;
7. writes `.lcc-local-bandits-dev` metadata into the destination.

To mirror the clean repository snapshot instead of the prepared experimental working copy:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\Setup-LocalBanditsDev.ps1 -NoAttackPoC
```

That mode uses:

`3268487204\mods\Bandits`

as the source.

## Reproducibility / revert tool

`tools/Apply-BanditsAttackBridgePoC.ps1` is not required for the normal manual test workflow. It exists to reproduce or revert the current exact-block transformation against an audited clean `BanditUpdate.lua`.

Current transformation:

- removes the active `spotted/addAggro/setTarget/setAttackedBy` bridge;
- removes both active `pathToCharacter(bandit)` pursuit calls from `UpdateZombies()`;
- replaces pursuit with coordinate-only `pathToLocationF(x, y, z)`;
- sets marker `upstream-coordinate-pursuit-v2`.

The script refuses mixed or unknown source states rather than patching them heuristically.

## Client/server consistency

With `DoLuaChecksum=true`, the client and dedicated server must resolve `Bandits2` and the LCC patches from matching content. On the single Windows test machine, both processes can use the same `%UserProfile%\Zomboid\mods` tree. Remote clients need the same modified Bandits/LCC files.

Use a full client and dedicated-server restart after replacing the working copy. For causal experiments, prefer freshly spawned Bandits and fresh zombies so stale network target state from a previous PoC cannot contaminate results.

## Publication boundary

`WorkshopPatches/Bandits-LCC-Dev` is the controlled development copy. It is not part of the `NPCCombatExperimental/Contents` Workshop payload. Once the correct behavior is proven, the intended final compatibility solution remains an LCC-authored interception or a minimal upstream Bandits change rather than shipping the complete upstream source inside the public compatibility patch.
