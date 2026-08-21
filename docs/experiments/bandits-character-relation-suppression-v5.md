# Bandits character-relation suppression v5

## Build 42.20.3 Java confirmation

The captured/decompiled runtime under `game_source/common-42.20.3` confirms the historical Bandits crash mechanism directly.

### AttackState is structurally unsafe for a Bandit target

In `zombie/ai/states/AttackState.java`, `triggerPlayerReaction()` obtains the zombie target as an `IsoGameCharacter`, performs distance/death/side checks, and then directly executes player-only casts:

```java
if (((IsoPlayer)targetChar).isDoShove() && isFront && !targetChar.isAimAtFloor()) {
    return;
}
if (((IsoPlayer)targetChar).isDoShove() && !isFront && !isBack && Rand.Next(100) > 75) {
    return;
}
```

There is no `IsoPlayer` type guard before these casts. A Bandit is a Java `IsoZombie`, so an ordinary zombie reaching vanilla `AttackState` with a Bandit target is structurally capable of producing the observed `IsoZombie cannot be cast to IsoPlayer` crash.

This validates the architecture: ordinary zombies may pursue Bandit **coordinates**, but a Bandit must not be installed as their vanilla target/attacker relationship.

## Runtime result of v4

Under real combat load, v4 produced:

```text
ENTRY_TARGET=0
targetLeaks=0
traceSafetyDisconnects=0
attackStateObserved=0
NetworkZombieMind=0
ClassCastException=0
AttackState.triggerPlayerReaction=0
```

while Bandits still generated hundreds of gunshot-coordinate alerts and dozens of melee/gun damage events. This strongly confirms that `ZAShoot` target creation plus `setAttackedBy(Bandit)` retaliation seams were the relevant relationship sources.

## Why v5 removes post-hit coordinate retaliation

The exact Build 42.20.3 `LungeState.execute()` does **not** recalculate its vector from the current target. Instead it consumes the existing `IsoZombie.vectorToTarget` and normalizes it without a zero-length guard:

```java
this.temp.x = zomb.vectorToTarget.x;
this.temp.y = zomb.vectorToTarget.y;
zomb.getZombieLungeSpeed();
this.temp.normalize();
zomb.setForwardDirection(this.temp);
```

The v4 sanitation wrapper cleared the unsafe Bandit relationship and immediately issued:

```lua
victim:pathToLocationF(attacker:getX(), attacker:getY(), attacker:getZ())
```

After a melee hit the two actors may already be overlapping or extremely close. The corresponding runtime contained one `Forward Direction cannot be zero length vector` / `LungeState` exception close to a melee sanitation event. The Java source does not prove that the v4 path call caused that exception, but it confirms that feeding a zero `vectorToTarget` into LungeState is unsafe.

v5 therefore removes the unnecessary exact-attacker-coordinate retaliation path from sanitation. This narrows responsibility rather than claiming a proven LungeState root cause.

## Current behavior

`ZAShoot.lua` uses coordinate-only response to the **shot location** instead of `spottedNew/addAggro/setTarget`.

`zzzz_LCC_BanditRelationshipSuppression.lua` wraps:

- `ZombieActions.Smack.onWorking`;
- `BanditUtils.Hit`.

After a confirmed Bandit-caused hit against an ordinary zombie it now only:

1. clears a Bandit target if one somehow exists;
2. clears `attackedBy(Bandit)`;
3. leaves steering to normal BanditUpdate coordinate pursuit and the shot-location response.

No exact attacker-coordinate path is issued by the sanitation wrapper.

## Marker

```text
[LCC][BanditsRelationPoC][BOOT]
marker=character-relation-suppression-v5
gunshotAlert=coordinate-only
meleeWrapped=true
gunHitWrapped=true
retaliationPath=disabled
```

Summary adds `retaliationPathsSuppressed`; legacy `coordinateResponses` should remain zero for v5 sanitation.

## Separate unresolved issue: custom Bite/BiteLow

The target-safety problem and custom zombie-to-Bandit bite behavior are now separate workstreams.

`BanditUpdate.lua` starts its custom bite only when the normal zombie reaches center distance `< 0.8`, has no wall to the Bandit and passes `isFacingObject(bandit, 0.3)`. It then sets `Bite/BiteLow` and expects the zombie to enter action state `bumped` before the manual damage tick executes.

The latest runtime showed `customBiteStart=0/customBiteEnd=0` despite very large coordinate-pursuit counts.

Decompiled `PathFindBehavior2` shows a `Goal.Location` path can continue until roughly `0.05` distance from its endpoint, so the missing bite cannot be explained simply by `pathToLocationF()` intentionally stopping at 0.8 tiles. The next experiment should trace close-range distance/facing/bump-state transitions rather than restore a character target.

## Test criteria

The next runtime should preserve:

```text
ENTRY_TARGET=0
traceSafetyDisconnects=0
attackStateObserved=0
ClassCastException=0
NetworkZombieMind=0
```

and verify:

- `meleeDamageEvents > 0`;
- `gunDamageEvents > 0`;
- `attackedByClears > 0` when those seams execute;
- `retaliationPathsSuppressed > 0`;
- no new `LungeState` zero-vector exception;
- ordinary zombies still discover/pursue nearby Bandits through coordinate-only logic;
- close-range bite tracing identifies whether the blocker is distance, wall/facing, bump type, or failure to enter `BumpedState`.

v5 remains intentionally narrow: relationship sanitation removes unsafe Java character relations and does not own steering or custom bite simulation.
