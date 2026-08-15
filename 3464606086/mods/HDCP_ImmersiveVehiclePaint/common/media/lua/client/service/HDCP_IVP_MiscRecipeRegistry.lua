-- The magazine teaches a recipe with no craftRecipe behind it, so ISLiteratureUI's
-- recipe tab falls back to getRecipeIcon(), which looks the name up in
-- ISLiteratureUI.miscRecipes. Vanilla hardcodes its own entries there and mods are
-- expected to add their own; with no entry the lookup returns nil and the row renders
-- with no icon.
--
-- The icon field is handed to getTexture(). Vanilla passes bare names for textures that
-- live in its .pack archives (Item_Wrench) and a full path for loose files
-- (media/ui/Traits/trait_herbalist.png). Ours are loose PNGs, so we keep whichever form
-- the engine actually resolves.
local HDCP_IVP_MiscRecipeRegistry = {}

function HDCP_IVP_MiscRecipeRegistry.new(deps)
    local Constants  = deps and deps.Constants or require('HDCP_IVP_Constants')
    local registry   = deps and deps.Registry or ISLiteratureUI.miscRecipes
    local resolve    = deps and deps.Resolve or getTexture

    local ICON       = 'Item_CherryAutomotiveSprayPaint'

    local candidates = {
        ICON,
        'media/textures/' .. ICON .. '.png',
    }

    local module     = {}

    module.register  = function()
        for _, candidate in ipairs(candidates) do
            if resolve(candidate) then
                registry[Constants.RECIPE] = {
                    tooltip = 'Tooltip_IVP_Recipe_AutomotivePainting',
                    icon    = candidate,
                }

                return candidate
            end
        end
    end

    return module
end

return HDCP_IVP_MiscRecipeRegistry
