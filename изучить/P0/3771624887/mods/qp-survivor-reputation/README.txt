QP Survivor Reputation v0.4.1

Persistent, server-authoritative survivor reputation for Project Zomboid Build 41 and Build 42.

v0.4.1 Community Automation - QP Supply Requests
--------------------------------------------------
Community automation now supports completed QP Supply Requests through a server-confirmed integration.

Production defaults:
- Community automation is disabled until an administrator enables it.
- Each eligible completed Supply Request grants +5 Community Reputation by default.
- The shared Community automation daily cap is 25 points by default.
- Self-created Supply Requests never grant automatic Reputation.
- Only survivors with a positive frozen final contribution qualify.
- The reward is fixed per completion and never scales per transferred item.

Every award uses a stable event source ID and the persistent Reputation award ledger. Reopening requests, reconnecting, restarting the server, or retrying the completion event cannot duplicate the award. Offline survivor usernames are supported through persistent Reputation profiles.

QP Survivor Tasks is not an automatic Reputation source. Its manual completion workflow cannot prove that the requested work happened and could otherwise be farmed.

QP Survivor Contracts remains separate. Contracts already support explicit Primary and Secondary Reputation rewards configured by administrators, so v0.4.1 does not add a second automatic Contract reward.

Administration:
View Reputation -> Admin Editor -> Automation Settings

The Automation Settings window provides:
- Master automatic Reputation switch
- Hunter automation controls
- Community automation switch
- QP Supply Request reward toggle and point value
- Shared Community daily point cap
- Registry diagnostics

Build 41 and Build 42 now share the current Profile, History, Admin Editor, Automation Settings, networking, and translation interface.

Compatibility and safety:
- Existing Reputation profiles, points, lifetime points, history, Hunter progress, and award ledgers are preserved.
- Save-data key remains QPReputation_v1.
- Existing worlds receive Community automation disabled unless an administrator enables it.
- Self-created reward testing controls were removed from the release build.
- Internal Supply Request event IDs remain in the private duplicate-protection ledger but are hidden from player-facing history.

v0.4.0 Generic Automation Foundation
--------------------------------------
The automation backend uses a versioned registry and generic server-side path handlers. Hunter behavior, baselines, milestones, source IDs, daily limits, and suspicious-jump protection remain compatible.

Community is the second implemented path. Explorer, Medic, Mechanic, and Builder remain registered but disabled and unimplemented until their own tested releases.

v0.3.6 Profile and History Quality of Life
-------------------------------------------
Profiles display lifetime Reputation, strongest path, and points remaining. Full History supports path and source filters plus stored actor, progress, and target metadata.

v0.3.5 Profile UI Polish
-------------------------
Responsive profile layout, measured columns, structured automation status, improved recent activity, and translated footer controls.

v0.4.1 RC2 UI polish
The profile now uses a compact two-column reputation grid, separate Hunter and Community automation summaries, and cleaner recent-activity rows. Automation Settings are grouped into clear sections for faster administration. This is a presentation-only change.

v0.4.1 RC2.1 UI wording and spacing hotfix
------------------------------------------------
- Replaced Hunter-specific status wording with generic Enabled and Disabled badges.
- Renamed automation cards to Hunter Automation and Community Automation.
- Simplified Master, Hunter, and Community checkboxes to Enabled.
- Moved the server-authoritative explanation onto its own full-width line.
- Increased the default and minimum Automation Settings size for translated labels.
- Presentation only; reputation logic, rewards, profiles, history, and save data are unchanged.
