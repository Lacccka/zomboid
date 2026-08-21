# Project Zomboid Build 42.20.3 — Java decompilation QA

## Scope

This report audits the Project Zomboid-owned (`zombie.*`) methods that CFR 0.152 reported as either `Unable to fully structure code` or a hard decompilation exception.

Audited JAR SHA-256:

`bda809fb49004a07dbfc560d059c0ee58d0643ab0f33b53351b13bd62f1d8227`

The hash matches the Build 42.20.3 client and dedicated-server JAR already recorded by the source snapshot.

## Method

The original `.class` files were inspected with the JDK disassembler (`javap -p -c -l -s`). This provides the actual JVM instruction stream and retained debug metadata rather than another inferred Java source representation.

The audit records:

- bytecode instructions and control-flow branches;
- switch instructions and exception-table entries;
- original `LineNumberTable` ranges;
- retained `LocalVariableTable` names where present;
- SHA-256 of each original `.class` file.

Bytecode is treated as ground truth for behavior. Decompiled Java remains a convenience/reference representation.

## Inventory

| Method | Current snapshot source | Instructions | Branches | Switches | Exception handlers | Original source lines |
|---|---:|---:|---:|---:|---:|---:|
| `zombie.CollisionManager.resolveContactsInternal` | CFR partial | 646 | 42 | 0 | 0 | 231-373 |
| `zombie.CombatManager.pressedAttack` | CFR partial | 611 | 93 | 0 | 0 | 3116-3308 |
| `zombie.characters.IsoGameCharacter.updateUserName` | CFR partial | 367 | 56 | 0 | 0 | 7342-7405 |
| `zombie.characters.IsoPlayer.getUsername` | CFR partial | 99 | 19 | 0 | 0 | 6989-7012 |
| `zombie.core.Core.loadOptions_OLD` | CFR partial | 1942 | 283 | 0 | 4 | 1112-1467 |
| `zombie.gameStates.ChooseGameInfo.readModInfoAux` | Vineflower fallback | 620 | 74 | 2 | 28 | 179-339 |
| `zombie.inventory.CompressIdenticalItems.areItemsIdentical` | Vineflower fallback | 182 | 22 | 0 | 7 | 130-204 |
| `zombie.inventory.ItemPickerJava.doRollItemInternal` | CFR partial | 773 | 142 | 0 | 0 | 1249-1490 |
| `zombie.inventory.ItemPickerJava.rollContainerItemInternal` | CFR partial | 599 | 94 | 0 | 0 | 1512-1712 |
| `zombie.iso.Helicopter.update` | CFR partial | 481 | 48 | 1 | 0 | 116-256 |
| `zombie.iso.IsoGridSquare.renderMinusFloor` | CFR partial | 1679 | 306 | 0 | 0 | 8969-9346 |
| `zombie.iso.fboRenderChunk.FBORenderCell.calculateObjectTargetAlpha` | CFR partial | 111 | 25 | 0 | 0 | 2149-2168 |
| `zombie.pathfind.PolygonalMap2.findPath` | CFR partial | 1787 | 190 | 0 | 8 | 568-791 |
| `zombie.randomizedWorld.randomizedBuilding.RBTrashed.trashHouse` | CFR partial | 1067 | 195 | 1 | 0 | 112-386 |

## Verified Vineflower fallbacks

### `ChooseGameInfo.readModInfoAux(String)`

**Status: verified, high confidence.**

CFR failed completely on this method. The current repository version was produced by Vineflower 1.12.0. The fallback was checked against the original Build 42.20.3 bytecode.

The bytecode confirms the reconstruction's important control flow and operations:

- cached `Mod.read` / `Mod.valid` fast path;
- version-specific `mod.info`, then common `mod.info` fallback;
- `InputStreamReader` + `BufferedReader` read loop;
- handling for `name`, `poster`, `description`, `require`, `incompatible`, `loadModAfter`, `loadModBefore`, `id`, `author`, `modversion`, `icon`, `category`, `url`, `pack`, `tiledef`, `versionMax`, and `versionMin`;
- `NumberFormatException` path for `tiledef`;
- version parsing exception paths;
- try-with-resources cleanup including `Throwable.addSuppressed`;
- `Translator.readModTranslation(mod)` followed by `mod.valid = true`;
- outer exception logging and `null` return.

The class retains local-variable names such as `nameStr`, `texName`, `posterName`, `pack`, `flags`, `tileDefName`, `fileNumber`, and `versionStr`, and those names align with the Vineflower output.

**Decision:** retain the Vineflower class as the canonical Java reference.

### `CompressIdenticalItems.areItemsIdentical(...)`

**Status: verified, high confidence.**

CFR failed completely on this method. The current Vineflower output matches the original bytecode, including the difficult retry/finally structure.

Confirmed behavior:

- inventory containers compare identical only when both nested containers are empty;
- item attributes are compared when both exist, with asymmetric/null attributes rejected;
- byte-data buffers are compared;
- `item2.id` is saved and temporarily set to `0`;
- item serialization is compared by exact length and byte contents;
- `BufferOverflowException` grows the comparison buffer and retries;
- the exception table guarantees restoration of `item2.id` on normal return and exceptional exit.

Retained local-variable names (`container1`, `container2`, `byteData1`, `byteData2`, `item1Start`, `item1End`, `item2Start`, `item2End`, `offset`, `itemID2`, `ex`) also align with the current Vineflower output.

**Decision:** retain the Vineflower class as the canonical Java reference.

## CFR partial methods that can be reconstructed cleanly from bytecode

The following two methods are small enough that CFR's synthetic labels can be removed without guessing. Their bytecode control flow and local-variable metadata are unambiguous.

### `IsoPlayer.getUsername(Boolean, Boolean)`

A clean behavior-equivalent reconstruction is:

```java
public String getUsername(Boolean canShowFirstname, Boolean canShowDisguisedName) {
    String nameStr = this.username;
    if (canShowDisguisedName) {
        this.updateDisguisedState();
        IsoGameCharacter isoGameCharacter = IsoCamera.getCameraCharacter();
        boolean bViewerIsAdmin = GameClient.client
                && isoGameCharacter instanceof IsoPlayer player
                && player.role.hasCapability(Capability.CanSeePlayersStats);

        if (this.isDisguised() && !bViewerIsAdmin) {
            nameStr = ServerOptions.getInstance().hideDisguisedUserName.getValue()
                    ? ""
                    : Translator.getText("IGUI_Disguised_Player_Name");
        } else if (canShowFirstname
                && GameClient.client
                && ServerOptions.instance.showFirstAndLastName.getValue()) {
            nameStr = this.getDescriptor().getForename() + " " + this.getDescriptor().getSurname();
            if (ServerOptions.instance.displayUserName.getValue()) {
                nameStr = nameStr + " (" + this.username + ")";
            }
        }
    } else if (canShowFirstname
            && GameClient.client
            && ServerOptions.instance.showFirstAndLastName.getValue()) {
        nameStr = this.getDescriptor().getForename() + " " + this.getDescriptor().getSurname();
        if (ServerOptions.instance.displayUserName.getValue()) {
            nameStr = nameStr + " (" + this.username + ")";
        }
    }
    return nameStr;
}
```

The two string-concatenation recipes were also resolved from the class bootstrap table: first/last name uses `"<forename> <surname>"`; username display uses `"<name> (<username>)"`.

**Decision:** safe clean-reconstruction candidate.

### `FBORenderCell.calculateObjectTargetAlpha(IsoObject)`

CFR currently emits synthetic `block6`, `** GOTO`, `lbl-1000`, `v0` and `v1` artifacts. The actual bytecode is a straightforward condition tree:

```java
private float calculateObjectTargetAlpha(IsoObject object) {
    int playerIndex = IsoCamera.frameState.playerIndex;
    ObjectRenderLayer renderLayer = object.getRenderInfo(playerIndex).layer;
    IsoObjectType t = IsoObjectType.MAX;
    if (object.sprite != null) {
        t = object.sprite.getTileType();
    }

    if (renderLayer == ObjectRenderLayer.MinusFloor
            || renderLayer == ObjectRenderLayer.MinusFloorSE
            || renderLayer == ObjectRenderLayer.Translucent
            || renderLayer == ObjectRenderLayer.TranslucentSE) {
        boolean isOpenDoor = object instanceof IsoDoor door && door.isOpen()
                || object instanceof IsoThumpable isoThumpable && isoThumpable.open;
        if (isOpenDoor
                && object.getProperties() != null
                && !object.getProperties().has(IsoPropertyType.GARAGE_DOOR)) {
            return 0.6f;
        }

        boolean isWestDoorOrWall = t == IsoObjectType.doorFrW
                || t == IsoObjectType.doorW
                || object.sprite != null && object.sprite.cutW;
        boolean isNorthDoorOrWall = t == IsoObjectType.doorFrN
                || t == IsoObjectType.doorN
                || object.sprite != null && object.sprite.cutN;
        if (isWestDoorOrWall || isNorthDoorOrWall) {
            return this.calculateObjectTargetAlpha_DoorOrWall(object);
        }
        return this.calculateObjectTargetAlpha_NotDoorOrWall(object);
    }
    return 1.0f;
}
```

The retained local variables include `playerIndex`, `renderLayer`, `t`, `isOpenDoor`, `door`, `isoThumpable`, `isWestDoorOrWall`, and `isNorthDoorOrWall`.

**Decision:** safe clean-reconstruction candidate.

## Remaining CFR partial methods

The other ten CFR-partial methods are deliberately **not manually rewritten**. Their bytecode is too large or control-flow-heavy for a trustworthy hand reconstruction when no independent whole-method decompiler result is available in the audit environment.

High-risk examples include:

- `Core.loadOptions_OLD`: 1,942 instructions, 283 branches, 4 exception handlers;
- `IsoGridSquare.renderMinusFloor`: 1,679 instructions, 306 branches;
- `PolygonalMap2.findPath`: 1,787 instructions, 190 branches, 8 exception handlers;
- `ItemPickerJava.doRollItemInternal`: 773 instructions, 142 branches;
- `CombatManager.pressedAttack`: 611 instructions, 93 branches and visible CFR `GOTO` artifacts.

For these methods, the existing CFR output is useful for navigation but must not be treated as authoritative structured Java. If a compatibility fix depends on one of them, verify the relevant path against the original bytecode (and preferably an independent decompiler output) before drawing conclusions.

## Project relevance priority

For compatibility-patch investigations, prioritize additional independent decompilation of:

1. `ItemPickerJava.doRollItemInternal` / `rollContainerItemInternal` — loot and container generation;
2. `PolygonalMap2.findPath` — pathfinding and movement;
3. `CombatManager.pressedAttack` — combat and network-sensitive attack state;
4. `IsoPlayer.getUsername` / `IsoGameCharacter.updateUserName` — multiplayer identity/display logic;
5. `CollisionManager.resolveContactsInternal` — collision behavior.

Rendering-only methods are lower priority for dedicated-server compatibility unless a client crash points directly at them.

## Confidence rules for future investigations

Use the snapshot with these confidence levels:

- **High confidence:** normal CFR methods with no warning, plus the two bytecode-verified Vineflower fallbacks.
- **Bytecode-verified candidate:** the two clean reconstructions documented above.
- **Conditional confidence:** methods marked `CFR partial`; inspect bytecode before using subtle control flow as evidence.
- **Ground truth:** original `.class` bytecode from the JAR whose SHA-256 matches this report.

Do not infer behavior solely from CFR synthetic labels such as `blockNN`, `** GOTO`, `lbl-*`, or temporary variables like `v0`/`v1`.
