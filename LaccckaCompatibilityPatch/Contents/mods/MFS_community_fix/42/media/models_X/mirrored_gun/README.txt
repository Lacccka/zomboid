Mirrored Held-Gun Assets and Conversion Guide
==============================================

Purpose
-------
Project Zomboid's vanilla firearm meshes are normally X-mirrored in their source
files. The character-held rendering path reflects them into real-world
handedness. Many MFS meshes were authored in normal real-world/Blender
handedness, so the held renderer reflects them the wrong way.

Use this treatment only for an asymmetric gun whose source/GUI model is correct
but whose character-held receiver, feed, ejection port, or controls are proven
to be mirrored. Do not apply it merely because an attachment moves toward the
opposite side of the screen; compare the attachment with the gun's geometry.

Current files
-------------
- M240_cat_Held.x
- URG_S_cat_Held.x

URG_S_cat_Held.x is shared by the standard and drum URG-S item variants. The
drum is a separate attached magazine model, so a second full gun mesh is not
needed.

How to create a corrected held mesh
-----------------------------------
1. Back up the working patch or keep a known-good overlay archive.
2. Identify the original mesh referenced by the gun's normal model script.
3. Copy that `.x` file into this `mirrored_gun` folder with a `_Held.x` suffix.
4. At the top of the copied file, find the first `Frame Root` and its first
   `FrameTransformMatrix` row. Change only its first value from +1 to -1:

       Original:  1.000000, 0.000000, 0.000000, 0.000000,
       Held:     -1.000000, 0.000000, 0.000000, 0.000000,

   Do not replace every `1.000000` in the file. Do not alter child frames,
   vertices, normals, UVs, materials, or animation data.
5. Compare the original and held files. The intended conversion should show
   exactly that one root-matrix value changed.

How to wire the held model
--------------------------
1. Keep the gun's original model definition unchanged for source/GUI use.
2. Add a second model definition such as `ExampleGun_Held`.
3. Point its mesh to `mirrored_gun/ExampleGun_Held` and reuse the original
   texture, scale, cullface setting, and every attachment block exactly.
4. Set the firearm item's `WeaponSprite` to `ExampleGun_Held`.
5. Set `WorldStaticModel` explicitly to the original normal model unless a
   separately tested ground model is required.
6. Ensure the inspection GUI selects the original model. Add the held-to-GUI
   alias to `AWCWF_InspectionModelMap` in
   `media/lua/shared/Gun_Vars/Weapon_Ability/AWCWF_Inspection_Model_Map.lua`.
   The shared table currently covers M240 and both URG-S variants; the generic
   inspection UI reads the table without firearm-specific conditions.
7. If magazine and drum item variants use the same base gun mesh, create two
   model-script aliases but reuse one `_Held.x` file.

WHERE THE CODE ACTUALLY SELECTS THE MODEL  (added RC7)
-------------------------------------------------------

The workflow above works because of three separate selection points. Two are
data, one is Lua. If a corrected held mesh ever "does nothing", check these in
order.

1. HELD MODEL - selected by data only, no Lua involved.

       WeaponSprite = ExampleGun_Held,     in the firearm's item block

   The engine renders the weapon in the character's hands from the model script
   named by WeaponSprite. Pointing it at the mirrored alias is the entire held
   fix. There is no code to change and nothing registers the alias anywhere.

2. INSPECTION GUI - redirected back to the unmirrored mesh by Lua.

   Table:    media/lua/shared/Gun_Vars/Weapon_Ability/AWCWF_Inspection_Model_Map.lua
   Consumer: media/lua/client/UI/risky_inspect_core.lua   (line 505)

       local inspectionSprite = (AWCWF_InspectionModelMap and
                                 AWCWF_InspectionModelMap[sprite]) or sprite

   That single line is the whole mechanism. The GUI takes the weapon's
   WeaponSprite, looks it up in the table, and uses the mapped name if one
   exists, otherwise the sprite itself. So an entry is only needed for guns that
   HAVE a mirrored held mesh:

       AWCWF_InspectionModelMap.ExampleGun_Held = "ExampleGun"

   Add entries to the table, never firearm-specific conditions in the UI code.

3. GROUND / DROPPED MODEL - selected by data only.

       WorldStaticModel = ExampleGun_Ground,    in the firearm's item block

   If this line is absent, the dropped item falls back to WeaponSprite - i.e.
   the mirrored held mesh.


IMPORTANT: THE ROOT-MATRIX MIRROR DOES NOT AFFECT THE GROUND MODEL
-------------------------------------------------------------------

Established during RC7 and worth understanding before using this guide:

    held weapon / inspection GUI / Blender  ->  render frames x vertices
    dropped item (ground model)             ->  render vertices only

The `+1 -> -1` root-matrix edit described above changes a FRAME. The held path
composes the frame chain, so the mirror applies there. The ground path ignores
the frame chain entirely, so the mirror has NO EFFECT on a dropped weapon.

That is very likely the real reason the separate `_Ground` model aliases with
`attachment world` transforms had to be invented at all - not because the ground
model was missing, but because the mirror silently did not reach it.

Consequences:

- Do not expect a corrected held mesh to fix the dropped weapon. It cannot.
- Blender will always show the mirrored file as correct, because Blender also
  composes the chain. Blender cannot reveal this class of problem.
- To make an orientation apply to BOTH paths it must live in the geometry, not
  in a frame. Use:

      tools/normalize_x_orientation.py --check <file.x>
      tools/normalize_x_orientation.py --fix   <file.x>

  which bakes the frame product into the vertices and normals and leaves the
  chain at identity. It refuses to touch uniform scales and mirrors, so it will
  NOT damage the `_Held.x` files in this folder.
- When authoring in Blender, apply all transforms (Ctrl+A -> All Transforms)
  before export. That leaves the chain at identity by construction and both
  render paths agree.


Ground-model starting workflow
------------------------------
If the corrected gun points upward or has the wrong handedness when dropped,
do not alter the held model. Add a separate `ExampleGun_Ground` model alias,
point it to the corrected `_Held` mesh, and start with the transform confirmed
for M240 and now used by both URG-S variants:

    attachment world
    {
        offset = 0.0 0.20 0.0,
        rotate = 90.0 0.0 0.0,
    }

Set the item's `WorldStaticModel` to this ground alias. The first 90-degree
rotation is confirmed for M240 and both URG-S variants, so it is the standard
starting workflow for firearm meshes using the same source-axis convention. It
is still a first test, not an automatic guarantee for every imported mesh.

Confirmed ground-pose examples:

- M240, resting top-side-up on its underside/ammunition-box area:

      offset = 0.0 0.20 0.0,
      rotate = 90.0 0.0 0.0,

- Standard and drum URG-S, rolled onto the rifle's side with extra clearance
  for the pistol grip:

      offset = 0.0 0.25 0.0,
      rotate = 90.0 0.0 90.0,

In this tested world-render path, the second rotation value changes ground
heading (the same kind of adjustment as rotating a placed item with `R`). The
third value rolls the already-horizontal weapon onto its side. Verify that a
new weapon lies flat, remains above the terrain, and has correct physical
handedness before accepting it. A magazine/drum variant can have its own ground
alias while sharing the same corrected mesh.

For these firearm meshes, local X is the side-to-side axis, local Y runs along
the weapon toward the muzzle, and local Z is its vertical/detail axis before
world placement. `rotate = 90.0 0.0 0.0` rotates 90 degrees around local X,
laying the Y-length of the weapon onto the ground plane. After that rotation,
the local Y direction also supplies world-height displacement; consequently
the middle offset value raises the laid-down model. If a grip is slightly
buried but orientation is otherwise correct, increase only that middle offset
in small steps and do not change the rotation.

Do not add optic-specific mount positions. Continue using each gun's existing
generic Scope, Canon, Stock, Light, Clip, and other attachment anchors.

Required in-game test
---------------------
Fully restart the game and spawn a fresh weapon. Check all of the following:

1. Original/Blender and inspection-GUI handedness remains correct.
2. Character-held feed, ejection port, receiver controls, and markings are
   physically correct.
3. Dropped model is visible, textured, sensibly oriented, and correctly handed.
4. Scope, muzzle device, stock, magazine/drum, light, and asymmetric side parts
   remain attached to the correct gun geometry.
5. Test X, Y, and Z adjustment relative to gun geometry, not just screen direction.
6. Test ammunition caliber, magazine capacity/model, reload, firing sound, and
   weapon switching.
7. Re-run the world-model and firearm-sound audits.

M240 implementation history (do not retune without a regression test)
---------------------------------------------------------------------
1. M240 originally used the normal `M240_cat.x` for every view. Its held belt
   feed/ejection handedness was wrong.
2. Script-level `invertX = true` did not correct the held model reliably.
3. `M240_cat_Held.x` was created by changing only the root X matrix from +1 to
   -1. `WeaponSprite = M240_cat_Held` corrected the held gun.
4. Several ground-only experiments failed: one stood upright; a baked rotated
   ground mesh lay upside down/mostly below terrain; other rotations pointed the
   weapon upward.
5. The confirmed ground model is `M240_cat_Ground`. It reuses the mirrored held
   mesh and has:

       attachment world
       {
           offset = 0.0 0.20 0.0,
           rotate = 90.0 0.0 0.0,
       }

6. That result lies flat, top-side up, with correct belt-feed handedness. Keep
   this ground transform unchanged unless a controlled regression proves a new
   value is necessary.

Separate M240 ammunition-box work
---------------------------------
The M240 functional ammunition conversion is independent of gun handedness. It
uses .308/7.62x51 ammunition and a dedicated 100-round belt box. Its cosmetic
box is the scaled MG338-style `Clip_M240_Box_cat.x`, not the old cylindrical
7.62x54R drum. Do not confuse that magazine model with `M240_cat_Held.x`.

Packaging
---------
Include this complete `mirrored_gun` folder plus every changed firearm script
and inspection selector file. Preserve original-relative paths so extracting
the overlay adds the held assets and overwrites the corresponding scripts.
