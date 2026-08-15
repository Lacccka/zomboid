HDCP_IVP_Helpers.noise(HDCP_IVP_Noises.SERVER_SCRIPT_LOADED)

local OnClientCommandFactory = require('event/HDCP_IVP_OnClientCommand')
local onClientCommand = OnClientCommandFactory.new()
Events.OnClientCommand.Add(onClientCommand.run)

local OnFillContainerFactory = require('event/HDCP_IVP_OnFillContainer')
local OnFillContainer = OnFillContainerFactory.new()
Events.OnFillContainer.Add(OnFillContainer.run)

local bagsAndContainers = BagsAndContainers
local clutterTables = ClutterTables
local proceduralDistributions = ProceduralDistributions
local vehicleDistributions = VehicleDistributions
local distributions = Distributions

local ToolsFactory = require('item/HDCP_IVP_Tools')
local tools = ToolsFactory.new({
    BagsAndContainers       = bagsAndContainers,
    ClutterTables           = clutterTables,
    ProceduralDistributions = proceduralDistributions,
    VehicleDistributions    = vehicleDistributions,
    Distributions           = distributions,
})
tools.include()

local MagazinesFactory = require('item/HDCP_IVP_Magazines')
local magazines = MagazinesFactory.new({
    BagsAndContainers       = bagsAndContainers,
    ClutterTables           = clutterTables,
    ProceduralDistributions = proceduralDistributions,
    VehicleDistributions    = vehicleDistributions,
    Distributions           = distributions,
})
magazines.include()

local AutomotiveSpraysFactory = require('item/HDCP_IVP_AutomotiveSprays')
local automotiveSprays = AutomotiveSpraysFactory.new({
    BagsAndContainers       = bagsAndContainers,
    ClutterTables           = clutterTables,
    ProceduralDistributions = proceduralDistributions,
    VehicleDistributions    = vehicleDistributions,
    Distributions           = distributions,
})
automotiveSprays.include()

local RecipesFactory = require('recipe/HDCP_IVP_Recipes')
local recipes = RecipesFactory.new()
HDCP_IVP_Recipes = {}
HDCP_IVP_Recipes.OnCreate = {}
function HDCP_IVP_Recipes.OnCreate.OpenBoxOfAutomotiveSprayPaint(recipe, character)
    recipes.openBoxOfAutomotiveSprayPaint(character)
end
function HDCP_IVP_Recipes.OnCreate.OpenBoxOfWarmAutomotiveSprayPaint(recipe, character)
    recipes.openBoxOfWarmAutomotiveSprayPaint(character)
end
function HDCP_IVP_Recipes.OnCreate.OpenBoxOfCoolAutomotiveSprayPaint(recipe, character)
    recipes.openBoxOfCoolAutomotiveSprayPaint(character)
end
function HDCP_IVP_Recipes.OnCreate.OpenBoxOfNeutralAutomotiveSprayPaint(recipe, character)
    recipes.openBoxOfNeutralAutomotiveSprayPaint(character)
end
