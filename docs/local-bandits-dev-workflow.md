# Local Bandits development workflow (Build 42.20)

This workflow makes the Bandits source used by local Windows testing deterministic while `NPCCombatExperimental` remains a separate source-clean compatibility patch.

## Goal

The live test must use exactly this chain:

```text
repository/3268487204/mods/Bandits
        |
        | mirror + optional local PoC
        v
C:\Users\<user>\Zomboid\mods\Bandits-LCC-Dev
        |
        | id=Bandits2
        v
Project Zomboid client + dedicated server
```

The Steam Workshop item `3268487204` is excluded from the test server's `WorkshopItems=` line, while `Bandits2` remains in `Mods=`.

## Why `-modfolders` is required

Build 42 can discover the same Mod ID from multiple sources. For this development workflow both client and server must be launched with:

```text
-modfolders mods,workshop,steam
```

This puts `%UserProfile%\Zomboid\mods` ahead of the local Workshop staging folder and Steam Workshop source. Without this argument a subscribed Steam copy with the same `id=Bandits2` may be selected instead of `Bandits-LCC-Dev`.

## One-command setup

From the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\Setup-LocalBanditsDev.ps1
```

Defaults:

- repository source: `3268487204\mods\Bandits`;
- local destination: `%UserProfile%\Zomboid\mods\Bandits-LCC-Dev`;
- server config: `%UserProfile%\Zomboid\Server\servertest.ini`;
- current AttackState upstream PoC: enabled.

The script:

1. validates that the repository B42.20 mod declares `id=Bandits2`;
2. mirrors the complete Bandits mod into `Zomboid\mods\Bandits-LCC-Dev` using `robocopy /MIR`;
3. applies `Apply-BanditsAttackBridgePoC.ps1` only to the local mirrored `BanditUpdate.lua`;
4. checks that `Mods=` already contains `Bandits2`;
5. removes Workshop item `3268487204` from `WorkshopItems=` and creates `servertest.ini.lcc-local-bandits.bak` before the first change;
6. scans `Zomboid\mods` and `Zomboid\Workshop` for another `id=Bandits2` and refuses an ambiguous local setup;
7. writes `.lcc-local-bandits-dev` into the destination with source/PoC metadata.

Every rerun refreshes the local Bandits copy from the repository first and then reapplies the current PoC. This prevents old experimental edits from accumulating between tests.

## Setup without the AttackState PoC

To test the repository Bandits snapshot unchanged while still keeping deterministic local loading:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\Setup-LocalBanditsDev.ps1 -NoAttackPoC
```

## Custom Zomboid directory or server ini

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\Setup-LocalBanditsDev.ps1 `
  -ZomboidHome "D:\PZ-Test\Zomboid" `
  -ServerIni "D:\PZ-Test\Zomboid\Server\servertest.ini"
```

Use `-SkipServerIni` only when the config is intentionally managed elsewhere. In that case manually guarantee:

```ini
Mods=...;Bandits2;...
WorkshopItems=...        # must NOT contain 3268487204
```

## Runtime verification

For the current upstream AttackState PoC, a correct client load must print:

```text
[LCC][BanditsAttackPoC][INIT] upstream-pursuit-v1 active; vanilla spotted/addAggro/setTarget/setAttackedBy bridge disabled
```

`NPCCombatExperimental` must then also report:

```text
[LCC][BanditsAttackGuard][UPSTREAM_POC_ACTIVE] marker=upstream-pursuit-v1 mode=observe-only v3Disconnect=false targetProtection=false
```

If the first marker is missing, do not interpret the combat test: the client did not load the prepared local `BanditUpdate.lua`.

## Client/server consistency

With `DoLuaChecksum=true`, the client and dedicated server must resolve `Bandits2` and the LCC patches from matching local content. On a single Windows test machine this workflow uses the same `%UserProfile%\Zomboid\mods` tree for both processes. Remote clients cannot join this local-only configuration unless they are given the same local Bandits and LCC mod copies.

## What remains source-clean

`WorkshopPatches/NPCCombatExperimental` still does not bundle `BanditUpdate.lua` or another complete Bandits source file. The direct source modification exists only in the controlled local development copy produced by the setup script. Once the correct behavior is proven, the reference change should be reproduced with an LCC-authored interception or an upstream hook instead of publishing modified third-party source.
