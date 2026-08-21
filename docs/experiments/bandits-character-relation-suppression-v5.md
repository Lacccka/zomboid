# Bandits character-relation suppression v5

## Build 42.20.3 Java confirmation

The captured runtime under `game_source/common-42.20.3` confirms the exact failure mechanism behind the historical Bandits crash.

In `zombie/ai/states/AttackState.java`, `triggerPlayerReaction(IsoZombie zombie)` contains a branch that checks only whether the target uses worn items and then performs an unconditional player cast:

```java
if (zombie.getTarget() != null && zombie.getTarget().isUsingWornItems()) {
    ...
    IsoPlayer player = (IsoPlayer)zombie.getTarget();
    ...
}
```

A Bandit is Java `IsoZombie` with human/worn semantics. Therefore allowing an ordinary zombie to enter vanilla AttackState with a Bandit target is structurally unsafe; this is not merely a timing bug.

This validates the v4 architecture: ordinary zombies may pursue Bandit **coordinates**, but a Bandit must not be installed as their vanilla character target/attacker relation.

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

while Bandits still generated hundreds of gunshot coordinate alerts and dozens of melee/gun damage events. This strongly confirms that `ZAShoot` target creation plus `setAttackedBy(Bandit)` retaliation seams were the relevant relationship sources.

## Why v5 removes post-hit coordinate retaliation

The same captured runtime exposes a separate `LungeState` hazard.

`zombie/ai/states/LungeState.java` can calculate:

```java
zombie.vectorToTarget.x = zombie.getTarget().getX() - zombie.getX();
zombie.vectorToTarget.y = zombie.getTarget().getY() - zombie.getY();
zombie.vectorToTarget.normalize();
```

The normalization is not guarded against a zero vector at that point.

The v4 wrapper cleared the unsafe Bandit relationship and then immediately called:

```lua
victim:pathToLocationF(attacker:getX(), attacker:getY(), attacker:getZ())
```

After a melee hit the two characters can already be overlapping or extremely close. The latest runtime test contained one `Forward Direction cannot be zero length vector` / `LungeState` failure close to a melee sanitation event.

v5 therefore removes that exact-coordinate retaliation path entirely.

## Current behavior

`ZAShoot.lua` still uses coordinate-only response to the **shot location** instead of `spottedNew/addAggro/setTarget`.

`zzzz_LCC_BanditRelationshipSuppression.lua` still wraps:

- `ZombieActions.Smack.onWorking`;
- `BanditUtils.Hit`.

After a confirmed Bandit-caused hit against an ordinary zombie it now only:

1. clears a Bandit target if one somehow exists;
2. clears `attackedBy(Bandit)`;
3. leaves movement to the normal `BanditUpdate` coordinate pursuit and gunshot sound-coordinate path.

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

Summary adds:

```text
retaliationPathsSuppressed=...
```

and the legacy `coordinateResponses` counter should remain zero for v5 sanitation.

## Test criteria

The next runtime test should preserve the v4 safety result:

```text
ENTRY_TARGET=0
traceSafetyDisconnects=0
attackStateObserved=0
ClassCastException=0
NetworkZombieMind=0
```

while also checking:

- `meleeDamageEvents > 0`;
- `gunDamageEvents > 0`;
- `attackedByClears > 0` when those seams execute;
- `retaliationPathsSuppressed > 0`;
- no `LungeState` zero-vector exception;
- ordinary zombies still discover/pursue nearby Bandits through the normal coordinate-only update path;
- the still-unresolved custom `Bite/BiteLow` transition is tested separately and is not solved by restoring a vanilla Bandit target.

The v5 change narrows the combat fix further: relationship sanitation is responsible only for removing unsafe Java character relations, not for steering.
