
-- ====================================================================
-- oZumbiAnalitico : 
-- 1. youtube ; https://www.youtube.com/@oZumbiAnalitico
-- 2. steam workshop ; https://steamcommunity.com/id/ozumbianalitico/myworkshopfiles/
-- 3. email ; ericyuta@gmail.com
-- ====================================================================
-- File [ 10_libs.lua ] : Utilities to import libraries

local Lib = {}

-- Inventory [ .../10_libs.lua ]
-- 1. Lib.check( package_table ) 
-- --------------------------- LIB | VANILLA --------------------------
-- local PARENT_DIR = 
-- local Lib = require(PARENT_DIR.."10_libs")

-- 
-- ====================================================================
-- 

function Lib.check( package_table ) -- [testing]
    local b_ret = true
    for i,v in ipairs(package_table) do 
        if not v then
            b_ret = false 
            Events.OnGameStart.Add( function() 
                local PLAYER = getSpecificPlayer(0)
                Events.EveryOneMinute.Add( function() 
                    PLAYER:Say( "Missing Requirements" )
                end )
            end )
            break 
        end
    end
    return b_ret
end

--[[ Example 
local PACKAGE_1 = require(PATH1)
local PACKAGE_2 = require(PATH2)
if not Lib.check( {PACKAGE_1, PACKAGE_2} ) then return nil end
-- Things of your mod after this
--]]

-- 
-- ====================================================================
--

return Lib 






