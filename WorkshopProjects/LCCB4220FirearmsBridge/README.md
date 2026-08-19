# LCC B42.20 Firearms Placement Bridge

Status: **RETIRED_UPSTREAM_FIXED**.

This split item is no longer an active Workshop project for the current Build 42.20 stack. The installed `MFS_community_fix` snapshot already contains the guarded 3D-placement renderer fix: it exits on server, requires `BuildingObjects/ISPlace3DItemCursor`, validates `self.items` / `weaponpart`, and restores missing `WeaponPart` instances before delegating to the original renderer.

Keeping a second LCC renderer wrapper would only double-wrap behavior already provided by the required dependency. The historical LCC implementation remains in this directory for regression/reference comparison, but there is intentionally no active `workshop.txt`.

If a future upstream release regresses this behavior, re-open the bridge only after reproducing the regression and adding a focused contract test.
