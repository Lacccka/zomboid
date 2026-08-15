# Lacccka B42.20 Stable Modpack

Publication template for a new Project Zomboid Workshop item.

## Publish as a new item

1. Ensure Git LFS has restored the real binary files: `git lfs pull`.
2. Copy the entire `LaccckaB4220StableModpack` directory into `Zomboid/Workshop/`.
3. Open Project Zomboid's Workshop publishing screen.
4. Create a new Workshop item from this directory. The current `workshop.txt` deliberately uses `id=0`.
5. After Steam assigns the new Workshop ID, keep that generated ID in `workshop.txt` and commit it to the repository.
6. Replace the old source Workshop IDs in the server's `WorkshopItems=` with the new single ID.
7. Keep all internal IDs in `Mods=`, with `LaccckaB4220Compat` last.

The old Workshop item `3782987959` is not modified by publishing from this directory.
