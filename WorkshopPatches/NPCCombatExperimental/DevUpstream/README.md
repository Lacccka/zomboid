# NPCCombatExperimental upstream working copy

This directory is development-only and is deliberately outside `Contents`.

`Bandits/42.20/media/lua/client/BanditUpdate.lua` is the manually deployable working copy of the upstream Bandits B42.20 client file used for controlled compatibility experiments. It is not part of the source-clean Workshop package.

Manual target on the test machine:

`<Bandits mod>/42.20/media/lua/client/BanditUpdate.lua`

For the current experiment the working copy must contain marker `LCC_BANDITS_ATTACK_BRIDGE_POC = "upstream-pursuit-v1"` and replace the close-range `spotted/addAggro/setTarget/setAttackedBy` bridge with `zombie:pathToCharacter(bandit)`.

After each update of upstream Bandits, refresh this working copy from the new 42.20 source before carrying compatibility changes forward.
