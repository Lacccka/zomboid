# Russian localization runtime loading — 2026-08-25

Status: **pending fresh in-game acceptance**

Build: Project Zomboid 42.20.3

Patch: `LaccckaB4220RussianText` 1.1.3

## Fresh failure evidence

The 1.1.2 Workshop update was confirmed active by client logs:

- `[LCC][RussianText][ModOptionsPercentGuard] loaded version=1.1.2`
- `[LCC][RussianText][ModOptionsPercentGuard] installed version=1.1.2`

Despite that, each translation initialization still produced the same 12 Immersive Vehicle Paint percentage formatter warnings, the Explosives fire-damage tooltip formatter warning, and `UnknownFormatConversionException: Conversion = ')'` while vanilla `MainOptions:addModOptionsPanel()` rendered mod options.

This disproved the stale-Workshop hypothesis.

## Confirmed structural defects

### 1. `LCC_*_Sandbox.json` files are not Translator categories

Build 42.20.3 `Translator.tryFillMapFromFile()` resolves exactly:

`media/lua/shared/Translate/<language>/<category>.json`

and `loadFiles()` requests canonical category names from `BY_NAME`, including `Sandbox`, `Tooltip`, `UI`, and so on.

Therefore files such as:

- `LCC_ImmersiveVehiclePaint_Sandbox.json`
- `LCC_Explosives_Sandbox.json`

are useful as source fragments/audit inputs, but they are **not loaded by the vanilla Translator as Sandbox dictionaries**. The escaped `%%` values added there could never replace the live target strings.

1.1.3 adds the affected keys to a canonical runtime file:

`common/media/lua/shared/Translate/RU/Sandbox.json`

The common file is loaded before this mod's version directory. The version `Sandbox.json` does not redefine these target keys, so the formatter-safe values remain active.

### 2. The 1.1.2 ModOptions guard wrapped the wrong B42.20 API shape

Vanilla 42.20.3 defines:

`function MainOptions:addModOptionsPanel()`

with no panel argument. It calls `PZAPI.ModOptions:load()` and iterates `PZAPI.ModOptions.Data`; each panel stores entries under `options.data`.

The 1.1.2 guard incorrectly expected an `options` argument and `options.options`, so it could install successfully while sanitizing zero fields.

1.1.3 now:

- wraps the real no-argument function shape;
- preloads `PZAPI.ModOptions` values;
- walks `PZAPI.ModOptions.Data` and each panel's `data` entries;
- escapes unsafe literal `%` in panel/option names, tooltips and multi-tick labels before vanilla calls `getText()` again;
- leaves descriptions and combobox display values alone because vanilla renders them through different paths.

## Acceptance criteria

A fresh Russian-client run must show:

- guard `loaded version=1.1.3`;
- guard `installed version=1.1.3`;
- zero formatter warnings for the 12 `Sandbox_ImmersiveVehiclePaint_*SpawnRate_option*` keys;
- zero formatter warning for `Sandbox_ExplosivesOptions_FireDamageMultiplier_tooltip`;
- zero `UnknownFormatConversionException: Conversion = ')'` from `MainOptions:addModOptionsPanel()`.

Do not restore this issue to `docs/final-reports` until those conditions are confirmed in-game.
