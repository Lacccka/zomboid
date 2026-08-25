
-- ====================================================================
-- oZumbiAnalitico : 
-- 1. youtube ; https://www.youtube.com/@oZumbiAnalitico
-- 2. steam workshop ; https://steamcommunity.com/id/ozumbianalitico/myworkshopfiles/
-- 3. email ; ericyuta@gmail.com
-- ====================================================================
-- File [ F_mod_options.lua ] : Utilities for mod options mod.

local F_ModOptions = {}

-- Inventory [ .../F_mod_options.lua ]
-- ---------------------- VARIABLES | FUNCTIONS -----------------------
-- 1. F_ModOptions.new(mod_shortname, mod_id)
-- 2. F_ModOptions.add(settings_table, option_name, default_value, [ display_name ])
-- 3. F_ModOptions.getOptionIndex(settings_table, name)
-- 4. F_ModOptions.CDeclare(mod_shortname, mod_id, inner_function, [ settings_table ] ) o (Check,Dropdown,Define,RadialButton)
-- ... option_name <- Check(option_name, display_name, default_value)
-- ... option_name <- Dropdown(option_name, display_name, default_value)
-- ... table_names <- RadialButton(table_names, default_index)
-- ... Define(inner_function) o (Check, Dropdown, RadialButton)
-- ... ... Check(option_name, inner_function) o (opt)
-- ... ... Dropdown(option_name, inner_function, default_value) o (opt)
-- ... ... RadialButton(table_names, inner_function) o (opt)
-- ---------------------------- LIB | MOD -----------------------------
-- 1. opt.tooltip ; stores the tooltip text
-- 2. opt[index] ; stores the display string of an dropdown option
-- --------------------------
-- 1. opt:onUpdate(val)
-- 2. opt:set(val)
-- 3. opt:OnApplyInGame(val)
-- local PARENT_DIR = 
-- local F_ModOptions = require(PARENT_DIR.."F_mod_options")

--
-- ====================================================================
--

function F_ModOptions.new(mod_shortname, mod_id) -- [ok]
    local settings = {}
    settings.mod_id = mod_id
    settings.mod_shortname = mod_shortname
    settings.options = {} -- option values
    settings.names = {} -- display names
    function settings:get(name_id) -- [testing] 
        return self.options[name_id]
    end
    return settings
end

-- F_ModOptions.add(settings_table, option_name, default_value, [ display_name ])
function F_ModOptions.add(settings_table, option_name, default_value, display_name) -- [ok]
    if not display_name then display_name = option_name end
    settings_table.options[option_name] = default_value
    settings_table.names[option_name] = display_name
    return option_name
end

function F_ModOptions.getOptionIndex(settings_table, name) -- [ok]
    return settings_table.options[name]
end

-- F_ModOptions.CDeclare(mod_shortname, mod_id, inner_function, [ settings_table ] ) o (Check,Dropdown,Define, RadialButton, settings_table)
-- ... option_name <- Check(option_name, display_name, default_value)
-- ... option_name <- Dropdown(option_name, display_name, default_value)
-- ... table_names <- RadialButton(table_names, default_index)
-- ... Define(inner_function) o (Check, Dropdown, RadialButton)
-- ... ... Check(option_name, inner_function) o (opt)
-- ... ... Dropdown(option_name, inner_function, default_value) o (opt)
-- ... ... RadialButton(table_names, inner_function) o (opt)
function F_ModOptions.CDeclare(mod_shortname,mod_id,inner_function,settings_table) -- [ok]
    if not settings_table then settings_table = F_ModOptions.new(mod_shortname, mod_id) end
    local function Check(option_name, display_name, default_value) -- [ok]
        return F_ModOptions.add(settings_table, option_name, default_value, display_name)
    end
    local function Dropdown(option_name, display_name, default_value) -- [ok]
        return F_ModOptions.add(settings_table, option_name, default_value, display_name)
    end
    local function RadialButton(table_names, default_index) -- [testing]
        for i, name in ipairs(table_names) do 
            if i== default_index then 
                Check(name, name,true) 
            else
                Check(name, name,false)
            end
        end
        return table_names
    end
    local function Define(inner_function_2) -- [ok]
        if ModOptions and ModOptions.getInstance then 
            local SETTINGS = ModOptions:getInstance(settings_table)
            local function Check2(option_name, inner_function_3) -- [ok]
                local opt = SETTINGS:getData(option_name) 
                inner_function_3(opt)
            end
            local function Dropdown2(option_name, inner_function_3, default_value) -- [ok]
                local opt = SETTINGS:getData(option_name) 
                inner_function_3(opt)
                opt[default_value] = opt[default_value].." (default) "
            end
            local function RadialButton2(table_names, inner_function_3) -- [testing]
                local opt = nil
                for i, name in ipairs(table_names) do 
                    opt = SETTINGS:getData(name) 
                    function opt:onUpdate(val) 
                        if val == true then 
                            for j, name_2 in ipairs(table_names) do 
                                opt2 = SETTINGS:getData(name_2) 
                                if opt2 ~= self then 
                                    opt2:set(false)
                                end
                            end
                        end
                    end
                    if inner_function_3 then inner_function_3(opt) end
                end
            end
            inner_function_2(Check2, Dropdown2, RadialButton2)
        end
    end
    inner_function(Check, Dropdown, Define, RadialButton, settings_table)
    return settings_table
end

--[[ Example ; CDeclare
local SETTINGS = F_ModOptions.CDeclare("Modding Laboratory", "modding_laboratory", function(Check,Drop,Define) 
    Check("My_Check_Option","My Check Option",true)
    Drop("My_Drop_Option", "My Drop Option",5)
    Define( function(C,D) 
        C("My_Check_Option", function(opt) 
            opt.tooltip = "This is an example of check option"
        end )
        D("My_Drop_Option", function(opt) 
            opt.tooltip = "This is an example of dropdown option"
            for i = 1,100 do 
                opt[i] = tostring(i).." value "
            end
        end, 5)
    end )
end )

Events.EveryOneMinute.Add( function() 
    local PLAYER = getSpecificPlayer(0)
    if not PLAYER then return nil end
    PLAYER:Say( tostring( SETTINGS:get("My_Check_Option") ) )
    PLAYER:Say( tostring( SETTINGS:get("My_Drop_Option") ) )
end )
--]]

--[[ Example ; RadialButton
F_ModOptions.CDeclare("Modding Laboratory", "modding_laboratory", function(Check, Drop, Def, Radial) 
    local radial_names = Radial({ "option 1","option 2","option 3" }, 2)
    Def( function(Check, Drop, Radial) 
        Radial( radial_names )
    end )
end )
--]]

--
-- ====================================================================
--

return F_ModOptions













