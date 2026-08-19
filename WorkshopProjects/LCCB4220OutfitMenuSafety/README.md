# LCC B42.20 Outfit Menu Safety

Status: **READY_FOR_UNLISTED_TEST**.

Unofficial independent compatibility patch for `Federal Ranger's [Chimera]` by EtherealShigure. The original `Federal_Rangers_Chimera` Workshop mod remains a required separate dependency; this project contains no Chimera source, clothing assets, models, textures, or sounds.

`zzz_LCC_ChimeraGhillieFix.lua` is an LCC-authored runtime wrapper around the installed game's `ISInventoryPaneContextMenu.doClothingItemExtraMenu` path. It normalizes the affected ghillie extra-menu arrays and otherwise leaves original behavior authoritative.

Before public visibility, test both affected ghillie types, normal clothing context menus, and client/server startup with the original Chimera item installed separately.
