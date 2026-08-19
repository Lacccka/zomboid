# LCC B42.20 Vehicle Integration Bridge

Status: **READY_FOR_UNLISTED_TEST**.

Unofficial independent compatibility patch for PZK VLC by PZK Forge and its SVU/Tsar integration paths. Original vehicle mods remain separate dependencies; this project does not repack PZK source, vehicles, models, textures, sounds, or other assets.

The runtime files are LCC-authored compatibility shims only: old vehicle UI paths are redirected to `Vehicles/ISUI/ISVehiclePartMenu`, root `ISBaseTimedAction` resolves to `TimedActions/ISBaseTimedAction`, the old PZK zones path resolves to `pzkUtils/pzkZonesFunction`, and the SVU support root redirects to the installed support module.

Before public visibility, test water-tank/vehicle menus, PZK zones, SVU integration, and dedicated-server startup with the original dependencies installed separately.
