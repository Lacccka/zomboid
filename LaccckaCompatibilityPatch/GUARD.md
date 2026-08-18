# LCC Compatibility Guard

`LCC/Guard.lua` is the failure-isolation layer for the compatibility patch.
Its job is to make an outdated LCC fix fail closed without hiding failures from
the original mod or Project Zomboid code.

## Runtime rules

- Give every compatibility fix a stable feature ID, for example `chimera.ghillie-extra-menu`.
- Use `Guard.safeRequire()` for dependencies whose path may move in a future build.
- Use `Guard.install()` to validate upstream symbols before replacing or wrapping them.
- Use `Guard.protect()` only around code owned by this patch. The first exception disables that feature for the rest of the session and logs one `DISABLED` line.
- Do not put the original/upstream function inside `Guard.protect()`. Upstream failures must remain visible in logs.
- Prefer wrappers and capability checks over full-file overrides.

Example:

```lua
local Guard = require "LCC/Guard"
local FEATURE = "example.inventory-hook"

Guard.safeRequire(FEATURE, "ISUI/SomeModule")
if not Guard.isEnabled(FEATURE) then return end

Guard.install {
    id = FEATURE,
    validate = function()
        return SomeModule and type(SomeModule.someMethod) == "function",
            "SomeModule.someMethod is unavailable"
    end,
    install = function()
        Guard.wrapBefore(FEATURE, SomeModule, "someMethod", function(self)
            -- LCC-only compatibility logic.
        end)
    end,
}
```

## Static contracts

`tools/compat_contracts.json` describes files where the patch depends on a
specific upstream implementation. `tools/audit_compat_contracts.py` verifies
the Git blob SHA and required source/patch fragments.

A `full-override` contract is intentionally strict: any upstream blob change
fails CI and requires manual review before the expected SHA is updated. This is
used for the highest-risk Bandits overrides.

To add a strict contract:

1. Add an entry with a unique `id`, `upstream_path`, `patch_path`, and current `expected_upstream_blob_sha`.
2. Add a few semantic `required_upstream_fragments` that describe the API/logic the patch depends on.
3. Add `required_patch_fragments` that prove the safety fix is still present.
4. Run `python3 tools/audit_compat_contracts.py`.
5. Only update the expected SHA after reviewing the upstream diff and confirming the LCC patch is still valid.

## Log format

Runtime guard messages are prefixed with:

```text
[LCC][Guard][OK][feature.id] installed
[LCC][Guard][WARN][feature.id] ...
[LCC][Guard][DISABLED][feature.id] ...
```

A disabled feature means that compatibility fix stopped executing for the
current session; it does not disable the entire LCC patch.
