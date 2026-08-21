# Bandits character-relation suppression v6

## Runtime basis

Test archive:

```text
ZomboidLogs_2026-08-21_14-46-44.zip
```

Build: Project Zomboid 42.20.3.

The run contained three client sessions/reconnects with the v5 relation suppression active.

Across all three sessions the important AttackState safety signals remained clean:

```text
targetLeaks=0
traceSafetyDisconnects=0
attackStateObserved=0
ClassCastException=0
AttackState.triggerPlayerReaction=0
Forward Direction cannot be zero length vector=0
```

The coordinate-only architecture therefore continues to prevent the historical
`IsoZombie -> IsoPlayer` AttackState cast failure without restoring a vanilla
Bandit character target.

## Custom Bite is compatible with target=nil

The new close-range trace disproved the earlier assumption that custom bites had
stopped completely.

Session 1 ended with:

```text
near08Samples=2181
biteArmedTransitions=24
bumpedTransitions=21
bumpedBiteSamples=1206
```

Session 2 ended with:

```text
near08Samples=328
biteArmedTransitions=9
bumpedTransitions=7
bumpedBiteSamples=254
```

Detailed records show successful transitions such as:

```text
state=walktoward bump=Bite target=nil
->
state=bumped bump=Bite target=nil
```

Therefore `Bite/BiteLow -> bumped` does not require a vanilla Bandit target.
The old `BanditsDiag customBiteStart=0` metric is not reliable for this path.

The first trace also over-counted `eligibleNotArmedSamples`: every detailed
sample emitted for that category in this run was `state=onground`, while
`BanditUpdate.UpdateZombies()` returns early for `onground` before the bite
arming branch. `close-range-bite-trace-v2` now mirrors those early state/prone
exits before declaring a sample truly eligible.

A second observer, `bite-outcome-trace-v1`, now follows the exposed `zid +
Bite/BiteLow + bumped` window and measures Bandit health delta. This will verify
whether the upstream tick-14 `bandit:Hit(...)` actually fires.

## Remaining NetworkZombieMind seam

v5 still produced exactly two instances of:

```text
NetworkZombieMind: goal character is not set
```

They occurred during heavy Bandit gun-hit retaliation sanitation, while all Lua
`zombie:getTarget()` diagnostics remained zero.

The decompiled B42.20.3 `zombie.characters.NetworkZombieMind.set()` explains
this precisely. It checks `PathFindBehavior2` independently of
`IsoZombie.target`:

```java
else if (pfb.isGoalCharacter()) {
    IsoGameCharacter character = pfb.getTargetChar();
    if (character instanceof IsoPlayer) {
        ...
    } else {
        packet.pfb.goal = PathFindBehavior2.Goal.None;
        DebugType.Multiplayer.error("NetworkZombieMind: goal character is not set");
    }
}
```

Thus `setTarget(nil)` and `setAttackedBy(nil)` are insufficient when a stale
`PathFindBehavior2.Goal.Character -> Bandit` survives internally.

## v6 change

Marker:

```text
character-relation-suppression-v6
```

After Bandit melee/gun damage sanitation v6 now checks:

```lua
pfb:isGoalCharacter()
pfb:getTargetChar()
```

If and only if the PFB character target is a Bandit `IsoZombie`, it calls:

```lua
pfb:cancel()
```

It does **not** issue a replacement path to the attacker's exact coordinate.
Normal BanditUpdate coordinate pursuit remains responsible for movement.

New counters:

```text
pfbCharacterGoalChecks
pfbCharacterGoalCancels
```

Expected next-run result:

```text
pfbCharacterGoalCancels > 0   # when the stale seam occurs
NetworkZombieMind = 0
targetLeaks = 0
attackStateObserved = 0
ClassCastException = 0
LungeState zero-vector = 0
```

## Next acceptance test

Use mixed melee and gun Bandits against a large ordinary-zombie group and keep
the client connected long enough for multiple controller/path updates.

Required markers:

```text
[LCC][BanditsRelationPoC][BOOT] marker=character-relation-suppression-v6
[LCC][BanditsRelationPoC][SUMMARY]
[LCC][BanditsBiteTrace][BOOT] marker=close-range-bite-trace-v2
[LCC][BanditsBiteOutcome][BOOT] marker=bite-outcome-trace-v1
```

The desired final state is no unsafe Java character relationship, no
`NetworkZombieMind` error, and independently confirmed custom bite damage.
