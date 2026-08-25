
-- ====================================================================
-- oZumbiAnalitico : 
-- 1. youtube ; https://www.youtube.com/@oZumbiAnalitico
-- 2. steam workshop ; https://steamcommunity.com/id/ozumbianalitico/myworkshopfiles/
-- 3. tiktok ; https://www.tiktok.com/@ozumbianalitico
-- 4. x ; https://twitter.com/ozumbianalitico
-- ====================================================================

--
local TOOLBOX_DIR = "Toolbox_PZNSIBS/"
local TOOLBOX_PZNS_DIR = "ToolboxPZNS_PZNSIBS/"

--
-- ====================================================================
-- Requires

-- pzns
local PZNS_WorldUtils = require("02_mod_utils/PZNS_WorldUtils")
local PZNS_UtilsNPCs = require("02_mod_utils/PZNS_UtilsNPCs");
local PZNS_UtilsDataNPCs = require("02_mod_utils/PZNS_UtilsDataNPCs")
local PZNS_PresetsSpeeches = require("03_mod_core/PZNS_PresetsSpeeches");
local PZNS_NPCsManager = require("04_data_management/PZNS_NPCsManager")
local PZNS_NPCGroupsManager = require("04_data_management/PZNS_NPCGroupsManager")
local PZNS_UtilsZones = require("02_mod_utils/PZNS_UtilsZones")
local PZNS_UtilsDataZones = require("02_mod_utils/PZNS_UtilsDataZones")
local PZNS_NPCZonesManager = require("04_data_management/PZNS_NPCZonesManager")
-- toolbox
local RadialMenu = require(TOOLBOX_DIR.."3_radial_menu")
local UserInterface = require(TOOLBOX_DIR.."7_in_game_ui")
local Lib = require(TOOLBOX_DIR.."10_libs")
local Check = require(TOOLBOX_DIR.."11_checkers")
-- pzns toolbox
local ForEach = require(TOOLBOX_PZNS_DIR.."ForEachLib_VANILLA")
-- local 

if not Lib.check( { 
    PZNS_WorldUtils,
    PZNS_UtilsNPCs,
    PZNS_UtilsDataNPCs,
    PZNS_PresetsSpeeches,
    PZNS_NPCsManager,
    PZNS_NPCGroupsManager
} ) then return nil end

--
-- ====================================================================
-- Buttons Construction

-- buttons file-variables
local button_inventory = nil
local button_config = nil
local button_invite = nil
local button_melee = nil
local button_ranged = nil
local button_follow = nil
local button_check_health = nil
local button_guard = nil
local button_home_config = nil
local button_close = nil
local button_move_here = nil
local button_select_job = nil
-- buttons texture file-variables
local texture_button_inventory = nil
local texture_button_config = nil
local texture_button_invite = nil
local texture_button_melee = nil
local texture_button_ranged = nil
local texture_button_follow = nil
local texture_button_check_health = nil
local texture_button_guard = nil
local texture_button_home_config = nil
local texture_button_close = nil
local texture_button_move_here = nil 
local texture_button_select_job = nil
--
local buttons_table = nil
-- 
local button_side_size = 35
-- 
UserInterface.CInGame( function(w,h,CORE) 
    -- texture object construction
    texture_button_inventory = getTexture(UserInterface.texture_path.."NPC_inventory.png")
    texture_button_config = getTexture(UserInterface.texture_path.."NPC_settings.png")
    texture_button_invite = getTexture(UserInterface.texture_path.."NPC_Invite.png")
    texture_button_melee = getTexture(UserInterface.texture_path.."NPC_meleIcon.png")
    texture_button_ranged = getTexture(UserInterface.texture_path.."NPC_gunIcon.png")
    texture_button_follow = getTexture(UserInterface.texture_path.."NPC_Walk.png")
    texture_button_check_health = getTexture(UserInterface.texture_path.."NPC_check_health.png")
    texture_button_guard = getTexture(UserInterface.texture_path.."NPC_Guard.png")
    texture_button_home_config = getTexture(UserInterface.texture_path.."NPC_base.png")
    texture_button_close = getTexture(UserInterface.texture_path.."NPC_close_button_menu.png")
    texture_button_move_here = getTexture(UserInterface.texture_path.."NPC_Stay.png")
    texture_button_select_job = getTexture(UserInterface.texture_path.."NPC_select_job.png")
    -- buttons construction
    button_inventory = UserInterface.floatingButton("button_inventory",nil, texture_button_inventory,0,0,button_side_size,button_side_size )
    button_config = UserInterface.floatingButton("button_config",nil, texture_button_config,0,0,button_side_size,button_side_size )
    button_invite = UserInterface.floatingButton("button_invite",nil, texture_button_invite,0,0,button_side_size,button_side_size )
    button_melee = UserInterface.floatingButton("button_melee",nil, texture_button_melee,0,0,button_side_size,button_side_size )
    button_ranged = UserInterface.floatingButton("button_ranged",nil, texture_button_ranged,0,0,button_side_size,button_side_size )
    button_follow = UserInterface.floatingButton("button_follow",nil, texture_button_follow,0,0,button_side_size,button_side_size )
    button_check_health = UserInterface.floatingButton("button_check_health",nil, texture_button_check_health,0,0,button_side_size,button_side_size )
    button_guard = UserInterface.floatingButton("button_guard",nil, texture_button_guard,0,0,button_side_size,button_side_size )
    button_home_config = UserInterface.floatingButton("button_home_config",nil, texture_button_home_config,0,0,button_side_size,button_side_size )
    button_close = UserInterface.floatingButton("button_close",nil, texture_button_close,0,0,button_side_size,button_side_size)
    button_move_here = UserInterface.floatingButton("button_move_here",nil, texture_button_move_here,0,0,button_side_size,button_side_size)
    button_select_job = UserInterface.floatingButton("button_select_job",nil, texture_button_select_job,0,0,button_side_size,button_side_size)
    -- invisible at start
    button_inventory:setVisible(false)
    button_config:setVisible(false)
    button_invite:setVisible(false)
    button_melee:setVisible(false)
    button_ranged:setVisible(false)
    button_follow:setVisible(false)
    button_check_health:setVisible(false)
    button_guard:setVisible(false)
    button_home_config:setVisible(false)
    button_close:setVisible(false)
    button_move_here:setVisible(false)
    button_select_job:setVisible(false)
    -- set background transparency
    button_inventory.backgroundColor.a = 0
    button_config.backgroundColor.a = 0
    button_invite.backgroundColor.a = 0
    button_melee.backgroundColor.a = 0
    button_ranged.backgroundColor.a = 0
    button_follow.backgroundColor.a = 0
    button_check_health.backgroundColor.a = 0
    button_guard.backgroundColor.a = 0
    button_home_config.backgroundColor.a = 0
    button_close.backgroundColor.a = 0
    button_move_here.backgroundColor.a = 0
    button_select_job.backgroundColor.a = 0
    -- position
    local X = PVPButton:getX() + PVPButton:getWidth()/2 - button_side_size/2
    -- position || y
    buttons_table = { 
        PVPButton,
        button_home_config,
        button_check_health,
        button_config,
        button_select_job,
        button_melee,
        button_ranged,
        button_inventory,
        button_guard,
        button_move_here,
        button_follow,
        button_invite,
        button_close
    }
    --
    -- UserInterface.verticalOrdering( buttons_table, nil, nil, true)
    -- position || x
    button_inventory:setX(X)
    button_config:setX(X)
    button_invite:setX(X)
    button_melee:setX(X)
    button_ranged:setX(X)
    button_follow:setX(X)
    button_check_health:setX(X)
    button_guard:setX(X)
    button_home_config:setX(X)
    button_close:setX(X)
    button_move_here:setX(X)
    button_select_job:setX(X)
end ) -- o (screen_width, screen_height, CORE)

--
-- ====================================================================
-- Config Window Construction - [ok]

-- List of Sandbox Global Options to be modified in-game
-- 1. CompanionFollowRange 
-- 2. CompanionRunRange 
-- 3. CompanionIdleTicks 

-- UserInterface.CWindow(window_name, inner_function, [ window ]) o (widgets, window, screen_width, screen_height)
-- ... It's a context which create a new empty window and fill utilities as arguments in inner_function
-- ui_reference <- widgets:Button(name, text, callback, [ x, y, width, height ])
-- ui_reference <- widgets:Slider(name, callback, [ x, y, width, height, default_value ])
-- ui_reference <- widgets:MultiEntry(name, text, [ x, y, width, height ])
-- ui_reference <- widgets:Label([ text, x, y, font, R, G, B, A ])

local CompanionConfig_window = nil
UserInterface.CWindow("Companion Configuration", function(widgets, window, screen_width, screen_height)
    local max_min_CompanionFollowRange = {30,1}
    local max_min_CompanionRunRange = {30,1}
    local max_min_CompanionIdleTicks = {10000, 500}
    local PLAYER = getSpecificPlayer(0)
    -- CompanionFollowRange
    local label_follow_range = widgets:Label("Companion Follow Range",0,20)
    local slider_follow_range = widgets:Slider("slider_follow_range", function(value) 
        CompanionFollowRange = math.max( max_min_CompanionFollowRange[2], value/100*max_min_CompanionFollowRange[1] )
        PLAYER:Say( "CompanionFollowRange: "..tostring( CompanionFollowRange ) )
    end, nil,nil,nil,nil, CompanionFollowRange*100/max_min_CompanionFollowRange[1] )
    -- CompanionRunRange
    local label_run_range = widgets:Label("Companion Run Range")
    local slider_run_range = widgets:Slider("slider_run_range", function(value) 
        CompanionRunRange = math.max( max_min_CompanionRunRange[2], value/100*max_min_CompanionRunRange[1] )
        PLAYER:Say( "CompanionRunRange: "..tostring( CompanionRunRange ) )
    end, nil,nil,nil,nil, CompanionRunRange*100/max_min_CompanionRunRange[1] )
    -- CompanionIdleTicks
    local label_idle_ticks = widgets:Label("Companion Idle Ticks")
    local slider_idle_ticks = widgets:Slider("slider_idle_ticks", function(value) 
        CompanionIdleTicks = math.max( max_min_CompanionIdleTicks[2], value/100*max_min_CompanionIdleTicks[1] )
        PLAYER:Say( "CompanionIdleTicks: "..tostring( CompanionIdleTicks ) )
    end, nil,nil,nil,nil,  CompanionIdleTicks*100/max_min_CompanionIdleTicks[1] )
    --
    UserInterface.verticalOrdering( { 
        label_follow_range,
        slider_follow_range,
        label_run_range,
        slider_run_range,
        label_idle_ticks,
        slider_idle_ticks
    } )
    --
    window:setVisible(false)
    window:setWidth(200)
    window:setHeight(200)
    CompanionConfig_window = window
end )

--
-- ====================================================================
-- Home Config Window Construction - [testing]
        
-- Local Inventory:
-- 1. PZNS_UtilsZones.PZNS_SetGroupZoneBoundary(groupID, zoneType, boundaries)
-- 2. local zoneType = "ZoneHome"
-- 3. local playerGroupID = "Player" .. tostring(defaultID) .. "Group"
-- 4. PZNS_UtilsZones.PZNS_ShowGroupZoneSquares(groupID, zoneKey)
-- 5. PZNS_UtilsZones.PZNS_HideGroupZoneSquares()
-- 6. PZNS_UtilsZones.PZNS_GetGroupZoneBoundary(groupID, zoneType)

local home_area_unit = 100
--
local function AreaInSquares(x1,x2,y1,y2) -- [ok]
    if not x2 then -- x1 is a zone table object
        return math.abs(x1.zoneBoundaryX1-x1.zoneBoundaryX2)*math.abs(x1.zoneBoundaryY1-x1.zoneBoundaryY2)
    end
    return math.abs(x1-x2)*math.abs(y1-y2)
end
local function CalculateMaxHomeZoneArea() -- [testing]
    local player_object = getSpecificPlayer(0)
    if not player_object then return home_area_unit*5, home_area_unit*5, 0 end
    local bonus_woodwork = Check.level(Perks.Woodwork, player_object)
    if not bonus_woodwork then bonus_woodwork = 0 end
    local bonus_metalwork = Check.level(Perks.MetalWelding, player_object)
    if not bonus_metalwork then bonus_metalwork = 0 end
    return home_area_unit*(5+bonus_woodwork+bonus_metalwork), home_area_unit*5, home_area_unit*(bonus_woodwork+bonus_metalwork)
end
--
local HomeConfig_window = nil
UserInterface.CWindow("Base Quick Set", function(widgets, window, screen_width, screen_height)
    local PLAYER = getSpecificPlayer(0)
    if not PLAYER then return nil end
    local groupID = "Player" .. tostring(0) .. "Group"
    local zoneKey = "ZoneHome"
    --
    local radius = 10
    local north_delta = 0 
    local south_delta = 0 
    local left_delta = 0 
    local right_delta = 0 
    local X = PLAYER:getX()
    local Y = PLAYER:getY()
    local home_size = nil
    local home_max_size = nil
    local home_max_size_base = nil
    local home_max_size_bonus = nil
    local b_zone_visible = false
    local label_home_size_value = nil
	local label_max_size_value = nil
    --
    local function CalculateHomeZoneArea() -- [ok]
        local activeZones = PZNS_UtilsDataZones.PZNS_GetCreateActiveZonesModData()
        local currentZone = activeZones[ "Player" .. tostring(0) .. "Group" .. "_" .. "ZoneHome" ]
        if not currentZone then return 0 end
        home_size = AreaInSquares(currentZone)
        if label_home_size_value then label_home_size_value.name = tostring(home_size) end
        home_max_size, home_max_size_base,home_max_size_bonus = CalculateMaxHomeZoneArea()
		if label_max_size_value then label_max_size_value.name = tostring(home_max_size_base).."+("..tostring(home_max_size_bonus)..")" end
    end    
    CalculateHomeZoneArea()
    --
    local button_show_zone = widgets:Button("button_show_zone", "Show", function()
        if b_zone_visible then return nil end
		PZNS_UtilsZones.PZNS_ShowGroupZoneSquares(groupID, zoneKey)
        b_zone_visible = true
    end, nil, 25, nil, nil)
        local button_hide_zone = widgets:Button("button_hide_zone", "Hide", function() 
		PZNS_UtilsZones.PZNS_HideGroupZoneSquares()
        b_zone_visible = false
    end, nil, 25, nil, nil )
    --
    local label_home_size = widgets:Label("Area (Squares): ")
        label_home_size_value = widgets:Label(tostring(home_size))
            local label_max_size = widgets:Label("Max.: ")
                label_max_size_value = widgets:Label(tostring(home_max_size_base).."+("..tostring(home_max_size_bonus)..")")
	-- 
    local label_center = widgets:Label("Centered on Player") --, 0,20)
    local label_radius = widgets:Label("Radius: ")
        local label_radius_value = widgets:Label(tostring(radius))
    --
    local button_set = widgets:Button("button_set", "Center", function() 
		if not PZNS_UtilsZones.PZNS_CheckGroupWorkZoneExists(groupID, zoneKey) then
			PZNS_NPCZonesManager.createZone(groupID, zoneKey)
		end
        X = PLAYER:getX()
        Y = PLAYER:getY()
		PZNS_UtilsZones.PZNS_SetGroupZoneBoundary( groupID , zoneKey, { 
			X-radius-left_delta, -- x1, left
			X+radius+right_delta, -- x2, right
			Y-radius-north_delta, -- y1, north
			Y+radius+south_delta, -- y2, south
			PLAYER:getZ() -- z
		} )
        CalculateHomeZoneArea()
        PZNS_UtilsZones.PZNS_HideGroupZoneSquares()
        b_zone_visible = false
        if not b_zone_visible then 
            PZNS_UtilsZones.PZNS_ShowGroupZoneSquares(groupID, zoneKey) 
            b_zone_visible = true
        end
    end )
        --
        local button_decrease_rad = widgets:Button("button_decrease_rad", "--radius", function() 
            PZNS_UtilsZones.PZNS_HideGroupZoneSquares()
            b_zone_visible = false
            radius = radius - 1
            label_radius_value.name = tostring(radius)
            PZNS_UtilsZones.PZNS_SetGroupZoneBoundary( groupID , zoneKey, { 
                X-radius-left_delta, -- x1, left
                X+radius+right_delta, -- x2, right
                Y-radius-north_delta, -- y1, north
                Y+radius+south_delta, -- y2, south
                PLAYER:getZ() -- z
            } )
            CalculateHomeZoneArea()
            if not b_zone_visible then 
                PZNS_UtilsZones.PZNS_ShowGroupZoneSquares(groupID, zoneKey) 
                b_zone_visible = true
            end
        end)
        local button_increase_rad = widgets:Button("button_increase_rad", " ++radius", function() 
            PZNS_UtilsZones.PZNS_HideGroupZoneSquares()
            b_zone_visible = false
            if AreaInSquares(
                X-(radius+5)-left_delta, 
                X+(radius+5)+right_delta,
                Y-(radius+5)-north_delta,
                Y+(radius+5)+south_delta
            ) <= home_max_size then radius = radius+5 end
            label_radius_value.name = tostring(radius)
            PZNS_UtilsZones.PZNS_SetGroupZoneBoundary( groupID , zoneKey, { 
                X-radius-left_delta, -- x1, left
                X+radius+right_delta, -- x2, right
                Y-radius-north_delta, -- y1, north
                Y+radius+south_delta, -- y2, south
                PLAYER:getZ() -- z
            } )
            CalculateHomeZoneArea()
            if not b_zone_visible then 
                PZNS_UtilsZones.PZNS_ShowGroupZoneSquares(groupID, zoneKey) 
                b_zone_visible = true
            end
        end)
    -- north
    local label_north_side = widgets:Label("North:  ")
        local label_north_side_value = widgets:Label(tostring(north_delta))
    local button_decrease_north_side = widgets:Button("button_decrease_north_side", "--north", function()
        PZNS_UtilsZones.PZNS_HideGroupZoneSquares()
        b_zone_visible = false
        north_delta = north_delta -1
        label_north_side_value.name = tostring(north_delta)
        PZNS_UtilsZones.PZNS_SetGroupZoneBoundary( groupID , zoneKey, { 
            X-radius-left_delta, -- x1, left
            X+radius+right_delta, -- x2, right
            Y-radius-north_delta, -- y1, north
            Y+radius+south_delta, -- y2, south
            PLAYER:getZ() -- z
        } )
        CalculateHomeZoneArea()
        if not b_zone_visible then 
            PZNS_UtilsZones.PZNS_ShowGroupZoneSquares(groupID, zoneKey) 
            b_zone_visible = true
        end
    end)
        --
        local button_increase_north_side = widgets:Button("button_increase_north_side", " ++north", function()         
            PZNS_UtilsZones.PZNS_HideGroupZoneSquares()
            b_zone_visible = false
            if AreaInSquares(
                X-radius-left_delta,
                X+radius+right_delta,
                Y-radius-(north_delta+1), 
                Y+radius+south_delta
            ) <= home_max_size then north_delta = north_delta+1 end
            label_north_side_value.name = tostring(north_delta)
            PZNS_UtilsZones.PZNS_SetGroupZoneBoundary( groupID , zoneKey, { 
                X-radius-left_delta, -- x1, left
                X+radius+right_delta, -- x2, right
                Y-radius-north_delta, -- y1, north
                Y+radius+south_delta, -- y2, south
                PLAYER:getZ() -- z
            } )
            CalculateHomeZoneArea()
            if not b_zone_visible then 
                PZNS_UtilsZones.PZNS_ShowGroupZoneSquares(groupID, zoneKey) 
                b_zone_visible = true
            end
        end)
    -- south
    local label_south_side = widgets:Label("South:  ")
        local label_south_side_value = widgets:Label(tostring(south_delta))
    local button_decrease_south_side = widgets:Button("button_decrease_south_side", "--south", function() 
        PZNS_UtilsZones.PZNS_HideGroupZoneSquares()
        b_zone_visible = false
        south_delta = south_delta -1
        label_south_side_value.name = tostring(south_delta)
        PZNS_UtilsZones.PZNS_SetGroupZoneBoundary( groupID , zoneKey, { 
            X-radius-left_delta, -- x1, left
            X+radius+right_delta, -- x2, right
            Y-radius-north_delta, -- y1, north
            Y+radius+south_delta, -- y2, south
            PLAYER:getZ() -- z
        } )
        CalculateHomeZoneArea()
        if not b_zone_visible then 
            PZNS_UtilsZones.PZNS_ShowGroupZoneSquares(groupID, zoneKey) 
            b_zone_visible = true
        end
    end)
        --
        local button_increase_south_side = widgets:Button("button_increase_south_side", " ++south", function() 
            PZNS_UtilsZones.PZNS_HideGroupZoneSquares()
            b_zone_visible = false
            if AreaInSquares(
                X-radius-left_delta,
                X+radius+right_delta,
                Y-radius-north_delta, 
                Y+radius+(south_delta+1)
            ) <= home_max_size then south_delta = south_delta+1 end
            label_south_side_value.name = tostring(south_delta)
            PZNS_UtilsZones.PZNS_SetGroupZoneBoundary( groupID , zoneKey, { 
                X-radius-left_delta, -- x1, left
                X+radius+right_delta, -- x2, right
                Y-radius-north_delta, -- y1, north
                Y+radius+south_delta, -- y2, south
                PLAYER:getZ() -- z
            } )
            CalculateHomeZoneArea()
            if not b_zone_visible then 
                PZNS_UtilsZones.PZNS_ShowGroupZoneSquares(groupID, zoneKey) 
                b_zone_visible = true
            end
        end)
    -- right
    local label_right_side = widgets:Label("Right:  ")
        local label_right_side_value = widgets:Label(tostring(right_delta))
    local button_decrease_right_side = widgets:Button("button_decrease_right_side", "--right", function() 
        PZNS_UtilsZones.PZNS_HideGroupZoneSquares()
        b_zone_visible = false
        right_delta = right_delta -1
        label_right_side_value.name = tostring(right_delta)
        PZNS_UtilsZones.PZNS_SetGroupZoneBoundary( groupID , zoneKey, { 
            X-radius-left_delta, -- x1, left
            X+radius+right_delta, -- x2, right
            Y-radius-north_delta, -- y1, north
            Y+radius+south_delta, -- y2, south
            PLAYER:getZ() -- z
        } )
        CalculateHomeZoneArea()
        if not b_zone_visible then 
            PZNS_UtilsZones.PZNS_ShowGroupZoneSquares(groupID, zoneKey) 
            b_zone_visible = true
        end
    end)
        --
        local button_increase_right_side = widgets:Button("button_increase_right_side", " ++right", function()         
            PZNS_UtilsZones.PZNS_HideGroupZoneSquares()
            b_zone_visible = false
            if AreaInSquares(
                X-radius-left_delta,
                X+radius+(right_delta+1),
                Y-radius-north_delta, 
                Y+radius+south_delta
            ) <= home_max_size then right_delta = right_delta+1 end
            label_right_side_value.name = tostring(right_delta)
            PZNS_UtilsZones.PZNS_SetGroupZoneBoundary( groupID , zoneKey, { 
                X-radius-left_delta, -- x1, left
                X+radius+right_delta, -- x2, right
                Y-radius-north_delta, -- y1, north
                Y+radius+south_delta, -- y2, south
                PLAYER:getZ() -- z
            } )
            CalculateHomeZoneArea()
            if not b_zone_visible then 
                PZNS_UtilsZones.PZNS_ShowGroupZoneSquares(groupID, zoneKey) 
                b_zone_visible = true
            end
        end)
    
    -- left
    local label_left_side = widgets:Label("Left:   ")
        local label_left_side_value = widgets:Label(tostring(left_delta))
    local button_decrease_left_side = widgets:Button("button_decrease_left_side", "--left", function() 
        PZNS_UtilsZones.PZNS_HideGroupZoneSquares()
        b_zone_visible = false
        left_delta = left_delta -1
        label_left_side_value.name = tostring(left_delta)
        PZNS_UtilsZones.PZNS_SetGroupZoneBoundary( groupID , zoneKey, { 
            X-radius-left_delta, -- x1, left
            X+radius+right_delta, -- x2, right
            Y-radius-north_delta, -- y1, north
            Y+radius+south_delta, -- y2, south
            PLAYER:getZ() -- z
        } )
        CalculateHomeZoneArea()
        if not b_zone_visible then 
            PZNS_UtilsZones.PZNS_ShowGroupZoneSquares(groupID, zoneKey) 
            b_zone_visible = true
        end
    end)
        --
        local button_increase_left_side = widgets:Button("button_increase_left_side", " ++left", function()         
            PZNS_UtilsZones.PZNS_HideGroupZoneSquares()
            b_zone_visible = false
            if AreaInSquares(
                X-radius-(left_delta+1),
                X+radius+right_delta,
                Y-radius-north_delta, 
                Y+radius+south_delta
            ) <= home_max_size then left_delta = left_delta+1 end
            label_left_side_value.name = tostring(left_delta)
            PZNS_UtilsZones.PZNS_SetGroupZoneBoundary( groupID , zoneKey, { 
                X-radius-left_delta, -- x1, left
                X+radius+right_delta, -- x2, right
                Y-radius-north_delta, -- y1, north
                Y+radius+south_delta, -- y2, south
                PLAYER:getZ() -- z
            } )
            CalculateHomeZoneArea()
            if not b_zone_visible then 
                PZNS_UtilsZones.PZNS_ShowGroupZoneSquares(groupID, zoneKey) 
                b_zone_visible = true
            end
        end)
    
    -- UserInterface.CWindow || set ordering
    UserInterface.verticalOrdering({ 
            button_show_zone, -- button_hide_zone
            label_home_size, -- label_home_size_value, label_max_size, label_max_size_value
            label_center, -- button_set
            label_radius, -- label_radius_value, button_set, button_decrease_rad, button_increase_rad
            label_north_side, -- label_north_side_value, button_decrease_north_side, button_increase_north_side
            label_south_side, -- label_south_side_value, button_decrease_south_side, button_increase_south_side
            label_left_side, -- label_left_side_value, button_decrease_left_side, button_increase_left_side
            label_right_side, -- label_right_side_value, button_decrease_right_side, button_increase_right_side
        })
        UserInterface.horizontalNext(button_show_zone, button_hide_zone)
        -- label_home_size_value, label_max_size, label_max_size_value
        UserInterface.verticalNext(button_show_zone, label_home_size_value)
        UserInterface.verticalNext(button_show_zone, label_max_size)
        UserInterface.verticalNext(button_show_zone, label_max_size_value)
        UserInterface.horizontalOrdering({ label_home_size, label_home_size_value, label_max_size, label_max_size_value })
        -- button_set
        UserInterface.verticalNext(label_home_size, button_set)
        UserInterface.horizontalNext(label_center, button_set)
        -- label_radius_value, button_set, button_decrease_rad, button_increase_rad
        UserInterface.verticalNext(label_center, label_radius_value)
        UserInterface.verticalNext(label_center, button_set)
        UserInterface.verticalNext(label_center, button_decrease_rad)
        UserInterface.verticalNext(label_center, button_increase_rad)
        UserInterface.horizontalOrdering({ label_radius, label_radius_value, button_set, button_decrease_rad, button_increase_rad })
        -- label_north_side_value, button_decrease_north_side, button_increase_north_side
        UserInterface.verticalNext(label_radius, label_north_side_value)
        UserInterface.verticalNext(label_radius, button_decrease_north_side)
        UserInterface.verticalNext(label_radius, button_increase_north_side)
        UserInterface.horizontalOrdering({ label_north_side, label_north_side_value, button_decrease_north_side, button_increase_north_side })
        -- label_south_side_value, button_decrease_south_side, button_increase_south_side
        UserInterface.verticalNext(label_north_side, label_south_side_value)
        UserInterface.verticalNext(label_north_side, button_decrease_south_side)
        UserInterface.verticalNext(label_north_side, button_increase_south_side)
        UserInterface.horizontalOrdering( {label_south_side, label_south_side_value, button_decrease_south_side, button_increase_south_side} )
        -- label_left_side_value, button_decrease_left_side, button_increase_left_side
        UserInterface.verticalNext(label_south_side, label_left_side_value)
        UserInterface.verticalNext(label_south_side, button_decrease_left_side)
        UserInterface.verticalNext(label_south_side, button_increase_left_side)
        UserInterface.horizontalOrdering({ label_left_side, label_left_side_value, button_decrease_left_side, button_increase_left_side })
        -- label_right_side_value, button_decrease_right_side, button_increase_right_side
        UserInterface.verticalNext(label_left_side, label_right_side_value)
        UserInterface.verticalNext(label_left_side, button_decrease_right_side)
        UserInterface.verticalNext(label_left_side, button_increase_right_side)
        UserInterface.horizontalOrdering({ label_right_side, label_right_side_value, button_decrease_right_side, button_increase_right_side })
    -- UserInterface.CWindow || set ordering | set other windows props
    window:setVisible(false)
    window:setWidth(280)
    window:setHeight(250)
    HomeConfig_window = window
end )

--
-- ====================================================================
-- Adjacent Member Floating Buttons

local function PZNS_CheckDistToNPCInventory()
    if PZNS_ActiveInventoryNPC == nil then
        return;
    end
    local playerSurvivor = getSpecificPlayer(0);
    local npcIsoPlayer = PZNS_ActiveInventoryNPC.npcIsoPlayerObject;
    -- Cows: Check and reset the PZNS_ActiveInventoryNPC if the NPC is beyond 2 squares away.
    if (npcIsoPlayer) then
        local npcDistanceFromPlayer = PZNS_WorldUtils.PZNS_GetDistanceBetweenTwoObjects(playerSurvivor, npcIsoPlayer);
        --
        if (npcDistanceFromPlayer > 2) then
            PZNS_ActiveInventoryNPC = {};
            Events.OnPlayerMove.Remove(PZNS_CheckDistToNPCInventory);
        end
    end
end

local function openNPCInventory(mpPlayerID, npcSurvivor)
    if (npcSurvivor == nil) then
        return;
    end
    PZNS_NPCsManager.setActiveInventoryNPCBySurvivorID(npcSurvivor.survivorID);
    -- Cows: Force reload the container window.
    ISPlayerData[mpPlayerID + 1].lootInventory:refreshBackpacks();
    Events.OnPlayerMove.Add(PZNS_CheckDistToNPCInventory);
end

local function HideAll()
    button_inventory:setVisible(false)
    button_config:setVisible(false)
    button_invite:setVisible(false)
    button_melee:setVisible(false)
    button_ranged:setVisible(false)
    button_follow:setVisible(false)
    button_check_health:setVisible(false)
    button_guard:setVisible(false)
    button_home_config:setVisible(false)
    button_close:setVisible(false)
    button_move_here:setVisible(false)
    button_select_job:setVisible(false)
end

local function getFirstMelee(npcSurvivor)
    local isoplayer = npcSurvivor.npcIsoPlayerObject
    if not isoplayer then return nil end
    local function inner_function(item) -- (item, i, size, items, inventory, isoplayer, ret)
        if not item:IsWeapon() then return nil end
        if item:isRanged() then return nil end
        return item
    end
    return ForEach.inventoryItem(inner_function, isoplayer) 
end

local function getFirstRanged(npcSurvivor)
    local isoplayer = npcSurvivor.npcIsoPlayerObject
    if not isoplayer then return nil end
    local function inner_function(item) -- (item, i, size, items, inventory, isoplayer, ret)
        if not item:IsWeapon() then return nil end
        if not item:isRanged() then return nil end
        return item
    end
    return ForEach.inventoryItem(inner_function, isoplayer) 
end

-- 1. Assign the vehicle radial menu key to activate the search for nearby npcs
-- 2. Toggle Visibility if find any NPC adjacent
local buttons_table_select_job = {}
Events.OnKeyPressed.Add( function(key) 
    if key ~= getCore():getKey("VehicleRadialMenu") then return nil end
    -- ... continue if key is the same of vehicle radial menu
    -- get adjacent npc
    local PLAYER = getSpecificPlayer(0)
    if PLAYER:getVehicle() then return nil end
    -- ... continue if outside vehicle
    local Adj_NPC = nil
    RadialMenu.FAdjacentMovingObjects( function(object) 
        if object == PLAYER then return nil end -- continue
        if string.find( tostring(object), "IsoPlayer") then 
            Adj_NPC = object
            return true
        end
        return nil -- end of iteration
    end , PLAYER )
    -- if there is an adjacent npc them show the interaction buttons
    if not Adj_NPC and button_home_config:isVisible() then 
        -- set invisible
        HideAll()
    elseif not Adj_NPC and not button_home_config:isVisible() then
		HideAll()
		button_home_config:setVisible(true)
		button_config:setVisible(true)
		button_close:setVisible(true)
		UserInterface.verticalOrderingVisible( buttons_table, nil, nil, true)
		-- callback for button_home_config
		UserInterface.setOnClick(button_home_config, function() 
			if not HomeConfig_window then return nil end
			HomeConfig_window:setVisible(true)
			HideAll()
		end )		
		-- callback for button_config
		UserInterface.setOnClick(button_config, function() 
			if not CompanionConfig_window then return nil end
			CompanionConfig_window:setVisible(true)
			HideAll()
		end )	
		-- 
		UserInterface.setOnClick(button_close, HideAll )
	else
        local activeNPCs = PZNS_UtilsDataNPCs.PZNS_GetCreateActiveNPCsModData()
        local npcSurvivor = activeNPCs[Adj_NPC:getModData().survivorID]
        local playerID = "Player" .. tostring(PLAYER:getPlayerNum())
        local playerGroupID = playerID .. "Group"
        -- boolean variables
        local b_alive = Adj_NPC:isAlive()
        local b_alive_member = ( b_alive and npcSurvivor.groupID == playerGroupID )
        -- set visible
        button_inventory:setVisible(b_alive_member)
        button_config:setVisible(b_alive_member)
        button_invite:setVisible( 
            npcSurvivor.groupID ~= playerGroupID and 
            npcSurvivor.isRaider ~= true and 
            Adj_NPC:isAlive() and 
            npcSurvivor.affection > 30
            )
        button_melee:setVisible(b_alive_member)
        button_ranged:setVisible(b_alive_member)
        button_follow:setVisible(b_alive_member)
        button_check_health:setVisible(b_alive)
        button_guard:setVisible(b_alive_member)
        button_move_here:setVisible(b_alive_member)
        button_close:setVisible(true)
        button_select_job:setVisible(b_alive_member)
        -- positioning
        UserInterface.verticalOrderingVisible( buttons_table, nil, nil, true)
        -- setting callbacks 
        UserInterface.setOnClick(button_follow, function() 
            if (npcSurvivor.speechTable == nil) then
                PZNS_UtilsNPCs.PZNS_UseNPCSpeechTable(
                    npcSurvivor, PZNS_PresetsSpeeches.PZNS_OrderSpeechFollow, "Friendly"
                );
            elseif (npcSurvivor.speechTable.PZNS_OrderSpeechFollow) then
                PZNS_UtilsNPCs.PZNS_UseNPCSpeechTable(
                    npcSurvivor, npcSurvivor.speechTable.PZNS_OrderSpeechFollow, "Friendly"
                );
            else
                PZNS_UtilsNPCs.PZNS_UseNPCSpeechTable(
                    npcSurvivor, PZNS_PresetsSpeeches.PZNS_OrderSpeechFollow, "Friendly"
                );
            end
            PZNS_NPCOrderActions["FollowMe"](npcSurvivor, playerID)
            PZNS_UtilsNPCs.PZNS_SetNPCJob(npcSurvivor,"Companion")
            npcSurvivor.isHoldingInPlace = false
            PLAYER:Say("Follow Me")
            HideAll()
        end )
        UserInterface.setOnClick(button_melee, function() 
            if not Adj_NPC:getPrimaryHandItem():isRanged() then return nil end
            local n_weapon = getFirstMelee(npcSurvivor)
            if not n_weapon then return nil end
            ISTimedActionQueue.add(ISEquipWeaponAction:new(Adj_NPC, n_weapon, 10, true, true))
            PLAYER:Say("Change to Melee Weapon")
            HideAll()
        end )
        UserInterface.setOnClick(button_ranged, function() 
            if Adj_NPC:getPrimaryHandItem():isRanged() then return nil end
            local n_weapon = getFirstRanged(npcSurvivor)
            if not n_weapon then return nil end
            ISTimedActionQueue.add(ISEquipWeaponAction:new(Adj_NPC, n_weapon, 10, true, true))
            PLAYER:Say("Change to Ranged Weapon")
            HideAll()
        end )
        UserInterface.setOnClick(button_check_health, function()
			ISTimedActionQueue.clear(PLAYER)
            ISTimedActionQueue.add(ISMedicalCheckAction:new(PLAYER, Adj_NPC))
            HideAll()
        end )        
        UserInterface.setOnClick(button_inventory, function() 
            openNPCInventory(PLAYER:getPlayerNum(), npcSurvivor)
            HideAll()
        end )
        UserInterface.setOnClick(button_invite, function()
            -- Cows: Remove the npcSurvivor from its original group if it was in a group
            if (npcSurvivor.groupID ~= nil) then
                PZNS_NPCGroupsManager.removeNPCFromGroupBySurvivorID(
                    npcSurvivor.groupID, npcSurvivor.survivorID
                );
            end
            npcSurvivor.canSaveData = true;                                  -- Cows: This will allow the NPC to be saved.
            PZNS_UtilsNPCs.PZNS_SetNPCGroupID(npcSurvivor, playerGroupID);   -- Cows: Update the npcSurvivor groupID
            PZNS_NPCGroupsManager.addNPCToGroup(npcSurvivor, playerGroupID); -- Cows: Add the npcSurvivor to the group's moddata
            PZNS_UtilsDataNPCs.PZNS_SaveNPCData(npcSurvivor.survivorID, npcSurvivor);
            HideAll()
        end )
        UserInterface.setOnClick(button_guard, function() 
            if (npcSurvivor.speechTable == nil) then
                PZNS_UtilsNPCs.PZNS_UseNPCSpeechTable(
                    npcSurvivor, PZNS_PresetsSpeeches.PZNS_OrderSpeechHoldPosition, "Friendly"
                );
            elseif (npcSurvivor.speechTable.PZNS_OrderSpeechFollow) then
                PZNS_UtilsNPCs.PZNS_UseNPCSpeechTable(
                    npcSurvivor, npcSurvivor.speechTable.PZNS_OrderSpeechHoldPosition, "Friendly"
                );
            else
                PZNS_UtilsNPCs.PZNS_UseNPCSpeechTable(
                    npcSurvivor, PZNS_PresetsSpeeches.PZNS_OrderSpeechHoldPosition, "Friendly"
                );
            end
            PZNS_UtilsNPCs.PZNS_SetNPCJob(npcSurvivor,"Guard")
            PLAYER:Say("Guard the Base")
            HideAll()
        end )
		UserInterface.setOnClick(button_move_here, function() 
            npcSurvivor.isHoldingInPlace = true
            
            -- Local Inventory:
            -- 1. npcSurvivor.speechTable.PZNS_OrderConfirmed
            
            if (npcSurvivor.speechTable == nil) then
                PZNS_UtilsNPCs.PZNS_UseNPCSpeechTable(
                    npcSurvivor, PZNS_PresetsSpeeches.PZNS_OrderConfirmed, "Friendly"
                );
            elseif (npcSurvivor.speechTable.PZNS_OrderConfirmed) then
                PZNS_UtilsNPCs.PZNS_UseNPCSpeechTable(
                    npcSurvivor, npcSurvivor.speechTable.PZNS_OrderConfirmed, "Friendly"
                );
            else
                PZNS_UtilsNPCs.PZNS_UseNPCSpeechTable(
                    npcSurvivor, PZNS_PresetsSpeeches.PZNS_OrderConfirmed, "Friendly"
                );
            end
            
            local square = getSpecificPlayer(0):getCurrentSquare()
            local ad_square = getCell():getGridSquare( square:getX()+2 , square:getY()+2 , square:getZ() )
            --isoplayer:NPCSetRunning(true)
            PZNS_RunToSquareXYZ(npcSurvivor, ad_square:getX(), ad_square:getY(), ad_square:getZ() )
            PLAYER:Say("Hold this position")
            HideAll()
        end )
		UserInterface.setOnClick(button_select_job, function()
            
            local idx = 1
            if PZNS_JobsText then for jobKey, jobText in pairs(PZNS_JobsText) do 
            if 
                not string.find(jobKey, "Enemy") and 
                not string.find(jobKey, "Debug") and 
                not string.find(jobKey, "Remove") and
                not string.find(jobKey, "Wander") and
                jobKey ~= "Companion"
            then 
                if not buttons_table_select_job[idx] then 
                    buttons_table_select_job[idx] = UserInterface.textFloatingButton(jobText[2],70,70,nil,nil)
                end
                buttons_table_select_job[idx]:setVisible(true)
                if jobKey == npcSurvivor.jobName then 
                    buttons_table_select_job[idx].backgroundColor.r = 0
                    buttons_table_select_job[idx].backgroundColor.g = 0
                    buttons_table_select_job[idx].backgroundColor.b = 0.5
                else
                    buttons_table_select_job[idx].backgroundColor.r = 0
                    buttons_table_select_job[idx].backgroundColor.g = 0
                    buttons_table_select_job[idx].backgroundColor.b = 0 
                end
                UserInterface.setOnClick(buttons_table_select_job[idx], function() 
                    PZNS_UtilsNPCs.PZNS_SetNPCJob(npcSurvivor, jobKey)
                    for i,v in ipairs(buttons_table_select_job) do 
                        v:setVisible(false)
                    end
                    HideAll()
                end )
                idx = idx + 1
            end
            end end
            UserInterface.verticalOrdering(buttons_table_select_job)
            --
            HideAll()
        end )
        
		-- callback for button_config
		UserInterface.setOnClick(button_config, function() 
			if not CompanionConfig_window then return nil end
			CompanionConfig_window:setVisible(true)
			HideAll()
		end )
		--
		UserInterface.setOnClick(button_close, HideAll )
    end
end )

--
-- ====================================================================
-- 

























