QP Supply Requests v0.6.6

Optional QP Survivor Reputation integration
- When QP Survivor Reputation v0.4.1 Community automation is installed and enabled, every survivor with a positive frozen final contribution receives the configured fixed Supply Request completion reward.
- Rewards occur only after permanent server-confirmed completion.
- The request creator is not rewarded for fulfilling their own request.
- Stable request and participant source IDs prevent reconnect, restart, and retry duplicates.
- QP Survivor Reputation is optional. All Supply Request behavior remains unchanged without it.

QP Supply Requests v0.6.5

Create shared supply requests on world containers in Project Zomboid Build 41 and Build 42.

Highlights
- Add up to 20 distinct item lines to one supply request.
- Set a separate target quantity for every requested item.
- Track progress and every player contribution per requested item.
- Active contribution totals are net: withdrawing a credited item removes one contribution credit before completion.
- Re-depositing the same physical item restores one credit instead of inflating the total.
- Protected withdrawals are rejected during timed-action validation, before the multiplayer item transaction starts.
- The existing transfer-stage block remains as a second client safeguard, with server-side protected-intent rejection for defense in depth.
- Multiplayer deposits and withdrawals register the exact InventoryItem ID before each vanilla transaction starts.
- The server credits an item only after that same ID appears in the attached request container.
- Canceled transfers receive no contribution credit, while mixed-item batches retain every contributor.
- The request completes only when every item line reaches its target.
- Excess deliveries are not credited beyond each target.
- Completed requests never reopen when supplies are removed.
- Optional administrator donation protection blocks withdrawals while collecting.
- Protected completed requests remain locked until a current administrator chooses Release Supplies.
- Existing v0.5.x single-item requests migrate automatically as one-line requests.
- In multiplayer, only players with current administrator access can create, remove, or release requests.
- Request creator names remain visible for accountability but do not grant management permissions.
- Localized UI: English, French, Russian, Simplified Chinese, Brazilian Portuguese, and Spanish.

Server setup
WorkshopItems=3766282536
Mods=QPSupplyRequests

Local test Mod ID
QPSupplyRequestsLocalTest
