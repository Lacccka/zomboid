
-- ====================================================================
-- oZumbiAnalitico : 
-- 1. youtube ; https://www.youtube.com/@oZumbiAnalitico
-- 2. steam workshop ; https://steamcommunity.com/id/ozumbianalitico/myworkshopfiles/
-- 3. tiktok ; https://www.tiktok.com/@ozumbianalitico
-- 4. X ; https://twitter.com/ozumbianalitico
-- ====================================================================
-- File [ 7_in_game_ui.lua ] : Graphical User Interface Utilities

-- Inventory [ .../7_in_game_ui.lua ]
-- 1. UserInterface.texture_path ; "media/textures/"
-- ---------------- Variables | Functions --------------------
-- 1. UserInterface.setOnClick(interface_object, callback)
-- 2. UserInterface.setImage(texture_path, texture_object)
-- 3. UserInterface.FUserInterface o (ui,ui_object_name, ui_index, uis)
-- 4. UserInterface.setOnClickHotbar(inner_function) o (clickedSlotIndex, item)
-- 5. UserInterface.setOnMap( interface_object )
-- 6. UserInterface.getScreenXYfromWorldXYZ(x,y, [ z ])
-- 7. UserInterface.FHudButton(inner_function) o (ui)
-- 8. UserInterface.isOffScreen(x,y, [ tol ])
-- 9. UserInterface.verticalNext(target, interface_object, [ pad ]) 
-- 10. UserInterface.horizontalNext(target, interface_objet, [ pad ])
-- 11. UserInterface.verticalOrdering(table_uis,[ pad ])
-- 12. UserInterface.horizontalOrdering(table_uis,[ pad ])
-- 13. UserInterface.floatingButton( name, texture_path, [ texture_object, x,y, width, height ])
-- 14. UserInterface.window(name,[ x,y,width,height ])
-- 15. UserInterface.childButton(parent, name, text, callback, [ x, y, width, height ])
-- 16. UserInterface.childMultilineEntry(parent, name, [ x, y, width, height ])
-- 17. UserInterface.childSlider(parent, name, callback , [ x, y, width, height, default_value ] ) o (_newValue, parent, name)
-- 18. UserInterface.childLabel(parent, [ text, x, y, font, R, G, B, A ])
-- XX. UserInterface.childTickBox(parent,[ x,y,w,h ])
-- 19. UserInterface.CInGame(inner_function) o (screen_width, screen_height, CORE)
-- 20. UserInterface.CWindow(window_name, inner_function, [ window ]) o (widgets, window, screen_width, screen_height)
-- ... It's a context which create a new empty window and fill utilities as arguments in inner_function
-- ui_reference <- widgets:Button(name, text, callback, [ x, y, width, height ])
-- ui_reference <- widgets:Slider(name, callback, [ x, y, width, height, default_value ])
-- ui_reference <- widgets:MultiEntry(name, text, [ x, y, width, height ])
-- ui_reference <- widgets:Label([ text, x, y, font, R, G, B, A ])
-- --------------------------- LIB | VANILLA --------------------------
-- 1. UI:setVisible(boolean)
-- 2. UI:setY() ; UI:getY()
-- 3. UI:setEditable(boolean)
-- 4. getTexture(file_path)
-- 5. UI:getHeight() ; UI:getWidth()
-- 6. SLIDER:getCurrentValue()
-- 7. UI:setResizable(boolean)
-- 8. local HOTBAR = getPlayerHotbar(playernum)
-- 9. 
-- local PARENT_DIR = 
-- local UserInterface = require(PARENT_DIR.."7_in_game_ui")

local UserInterface = {}
UserInterface.texture_path = "media/textures/"

--
-- ====================================================================
-- Requires

require "ISUI/ISPanel"
require "ISUI/ISButton"
require "ISUI/ISLayoutManager"
require "RadioCom/ISUIRadio/ISSliderPanel"

--
-- ====================================================================
-- Utilities

function UserInterface.setOnClick(interface_object, callback) -- [testing]
    interface_object.onclick = callback
end

-- UserInterface.setImage(texture_path, texture_object)
function UserInterface.setImage(interface_object, texture_path, texture_object) -- [testing]
    if texture_object then 
        interface_object:setImage(texture_object)
    elseif texture_path then 
        interface_object:setImage( getTexture( texture_path ) )
    end
end

-- UserInterface.FUserInterface o (ui,ui_object_name, ui_index, uis)
function UserInterface.FUserInterface(inner_function) -- [testing]
    local uis = UIManager.getUI()
    if uis:size()==0 then return nil end
    for i=0, uis:size() - 1 do
        local current_ui = uis:get(i)
        local current_ui_object_name = tostring(current_ui)
        local current_ui_index = i
        local ret_in = inner_function(current_ui,current_ui_object_name, current_ui_index, uis)
        if ret_in == nil then 
        else 
            if ret_in == true then 
                break
            else 
                return ret_in
            end
        end
    end
end

-- UserInterface.setOnClickHotbar(inner_function) o (clickedSlotIndex, item)
function UserInterface.setOnClickHotbar(inner_function) -- [testing]
    function ISHotbar:onMouseUp(x, y)
        local counta = 1;
        local clickedSlot = self:getSlotIndexAt(x, y);
        if ISMouseDrag.dragging then
            local slot = self.availableSlot[clickedSlot];
            local dragging = ISInventoryPane.getActualItems(ISMouseDrag.dragging);
            for i,v in ipairs(dragging) do
                if (v ~= self.attachedItems[clickedSlot]) and self:canBeAttached(slot, v) then
                    local weapon = v;
                    self:attachItem(weapon, slot.def.attachments[weapon:getAttachmentType()], clickedSlot, slot.def, true);
                    break;
                end
            end
        else 
            inner_function(clickedSlot, self.attachedItems[clickedSlot])
        end
    end
end

function UserInterface.setOnMap( interface_object ) -- [testing]
    local old_update = interface_object.update
    interface_object:setVisible(false)
    interface_object:setAlwaysOnTop(true)
    function interface_object:update() 
        old_update(self)
        if ISWorldMap_instance then 
            if ISWorldMap_instance:isVisible() then
                self:setVisible(true)
            else 
                self:setVisible(false)
            end
        end
    end
end

-- UserInterface.getScreenXYfromWorldXYZ(x,y, [ z ])
function UserInterface.getScreenXYfromWorldXYZ(x,y,z) -- [testing]
    if not z then z = 0 end
    local X = IsoUtils.XToScreen(x,y,z,0) - IsoCamera.getOffX()
    local Y = IsoUtils.YToScreen(x,y,z,0) - IsoCamera.getOffY()
    X = X / getCore():getZoom(0)
    Y = Y / getCore():getZoom(0)
    return X,Y
end

function UserInterface.FHudButton(inner_function) -- [testing]
    local UI = nil 
    local ret = nil
    UI = ISEquippedItem.instance.healthBtn
    ret = inner_function(UI)
    if ret == nil then 
    else 
        if ret == true then 
            return 
        else
            return ret
        end
    end
    --
    UI = ISEquippedItem.instance.invBtn
    ret = inner_function(UI)
    if ret == nil then 
    else 
        if ret == true then 
            return 
        else
            return ret
        end
    end
    --
    UI = ISEquippedItem.instance.craftingBtn
    ret = inner_function(UI)
    if ret == nil then 
    else 
        if ret == true then 
            return 
        else
            return ret
        end
    end
    --
    UI = ISEquippedItem.instance.mapBtn
    ret = inner_function(UI)
    if ret == nil then 
    else 
        if ret == true then 
            return 
        else
            return ret
        end
    end
    --
    UI = ISEquippedItem.instance.searchBtn
    ret = inner_function(UI)
    if ret == nil then 
    else 
        if ret == true then 
            return 
        else
            return ret
        end
    end
    --
    UI = ISEquippedItem.instance.movableBtn
    ret = inner_function(UI)
    if ret == nil then 
    else 
        if ret == true then 
            return 
        else
            return ret
        end
    end
    --
    UI = ISEquippedItem.instance.debugBtn
    ret = inner_function(UI)
    if ret == nil then 
    else 
        if ret == true then 
            return 
        else
            return ret
        end
    end
    --
    UI = ISEquippedItem.instance.clientBtn
    ret = inner_function(UI)
    if ret == nil then 
    else 
        if ret == true then 
            return 
        else
            return ret
        end
    end
    --
    UI = ISEquippedItem.instance.adminBtn
    ret = inner_function(UI)
    if ret == nil then 
    else 
        if ret == true then 
            return 
        else
            return ret
        end
    end
end

-- UserInterface.isOffScreen(x,y, [ tol ])
function UserInterface.isOffScreen(x,y, tol) -- [testing]
    if not tol then tol = 10 end
    local CORE = getCore()
    local w = CORE:getScreenWidth()
    local h = CORE:getScreenHeight()
    return (x<=tol or x>=w-tol or y<=tol or y>=h-tol)
end

-- UserInterface.verticalNext(target, interface_object, [ pad ]) 
function UserInterface.verticalNext(target, interface_object, pad) -- [ok]
    if not pad then pad = 10 end
    -- interface_object:setX( target:getX() )
    interface_object:setY( target:getY() + target:getHeight() + pad )
end

function UserInterface.verticalPrev(target, interface_object, pad) -- [testing]
    if not pad then pad = 10 end
    -- interface_object:setX( target:getX() )
    interface_object:setY( target:getY() - interface_object:getHeight() - pad )
end

-- UserInterface.horizontalNext(target, interface_objet, [ pad ])
function UserInterface.horizontalNext(target, interface_object, pad) -- [ok]
    if not pad then pad = 10 end
    -- interface_object:setY( target:getY() )
    interface_object:setX( target:getX() + target:getWidth() + pad )
end

-- UserInterface.horizontalNext(target, interface_objet, [ pad ])
function UserInterface.horizontalPrev(target, interface_object, pad) -- [testing]
    if not pad then pad = 10 end
    -- interface_object:setY( target:getY() )
    interface_object:setX( target:getX() - interface_object:getWidth() - pad )
end

-- UserInterface.verticalOrdering(table_uis,[ pad, is_array, is_reversed ])
function UserInterface.verticalOrdering(table_uis, pad, is_array, is_reversed) -- [ok]
    -- optional arguments
    if not is_reversed then is_reversed = false end
    if not is_array then is_array = true end
    if not pad then pad = 10 end
    --
    local previous_ui = nil
    if is_array then
        for i,ui in ipairs(table_uis) do
            if previous_ui ~= nil then 
                if not is_reversed then 
                    ui:setY( previous_ui:getY() + previous_ui:getHeight() + pad)
                else 
                    ui:setY( previous_ui:getY() - ui:getHeight() - pad)
                end
            end
            previous_ui = ui
        end
    else 
        for name,ui in pairs(table_uis) do
            if previous_ui ~= nil then 
                if not is_reversed then
                    ui:setY( previous_ui:getY() + previous_ui:getHeight() + pad)
                else 
                    ui:setY( previous_ui:getY() - ui:getHeight() - pad)
                end
            end
            previous_ui = ui
        end
    end
end

-- ... onlyVisible uis from the table
-- ... the first ui should be visible
function UserInterface.verticalOrderingVisible(table_uis, pad, is_array, is_reversed) -- [testing]
    if #table_uis == 0 then return nil end
    if not table_uis[1]:isVisible() then return nil end
    -- optional arguments
    if not is_reversed then is_reversed = false end
    if not is_array then is_array = true end
    if not pad then pad = 10 end
    --
    local previous_ui = nil
    if is_array then
        for i,ui in ipairs(table_uis) do
            if ui:isVisible() then 
                if previous_ui ~= nil then 
                    if not is_reversed then 
                        ui:setY( previous_ui:getY() + previous_ui:getHeight() + pad)
                    else 
                        ui:setY( previous_ui:getY() - ui:getHeight() - pad)
                    end
                end
                previous_ui = ui
            end
        end
    else 
        for name,ui in pairs(table_uis) do
            if ui:isVisible() then
                if previous_ui ~= nil then 
                    if not is_reversed then
                        ui:setY( previous_ui:getY() + previous_ui:getHeight() + pad)
                    else 
                        ui:setY( previous_ui:getY() - ui:getHeight() - pad)
                    end
                end
                previous_ui = ui
            end
        end
    end
end

-- UserInterface.horizontalOrdering(table_uis,[ pad, is_array, is_reversed ])
function UserInterface.horizontalOrdering(table_uis, pad, is_array, is_reversed) -- [ok]
    -- optional arguments
    if not is_reversed then is_reversed = false end
    if not is_array then is_array = true end
    if not pad then pad = 10 end
    --
    local previous_ui = nil
    if is_array then 
        for i,ui in ipairs(table_uis) do
            if previous_ui ~= nil then 
                if not is_reversed then 
                    ui:setX( previous_ui:getX() + previous_ui:getWidth() + pad )
                else 
                    ui:setX( previous_ui:getX() - ui:getWidth() - pad )
                end
            end
            previous_ui = ui
        end
    else 
        for name,ui in pairs(table_uis) do
            if previous_ui ~= nil then 
                if not is_reversed then 
                    ui:setX( previous_ui:getX() + previous_ui:getWidth() + pad )
                else 
                    ui:setX( previous_ui:getX() - ui:getWidth() - pad )
                end
            end
            previous_ui = ui
        end
    end
end

-- ... onlyVisible uis from the table
-- ... the first ui should be visible
function UserInterface.horizontalOrderingVisible(table_uis, pad, is_array, is_reversed) -- [testing]
    if #table_uis == 0 then return nil end
    if not table_uis[1]:isVisible() then return nil end
    -- optional arguments
    if not is_reversed then is_reversed = false end
    if not is_array then is_array = true end
    if not pad then pad = 10 end
    --
    local previous_ui = nil
    if is_array then 
        for i,ui in ipairs(table_uis) do
            if ui:isVisible() then 
                if previous_ui ~= nil then 
                    if not is_reversed then 
                        ui:setX( previous_ui:getX() + previous_ui:getWidth() + pad )
                    else 
                        ui:setX( previous_ui:getX() - ui:getWidth() - pad )
                    end
                end
                previous_ui = ui
            end
        end
    else 
        for name,ui in pairs(table_uis) do
            if ui:isVisible() then 
                if previous_ui ~= nil then 
                    if not is_reversed then 
                        ui:setX( previous_ui:getX() + previous_ui:getWidth() + pad )
                    else 
                        ui:setX( previous_ui:getX() - ui:getWidth() - pad )
                    end
                end
                previous_ui = ui
            end
        end
    end
end

-- UserInterface.positionTranslate(table_uis, [ dx, dy, is_array ])
function UserInterface.positionTranslate(table_uis, dx, dy, is_array) -- [testing]
    if not dx then dx = 0 end
    if not dy then dy = 0 end
    if not is_array then is_array = true end
    if is_array then 
        for i, ui in ipairs(table_uis) do 
            ui:setX( ui:getX() + dx )
            ui:setY( ui:getY() + dy )
        end
    else 
        for name, ui in pairs(table_uis) do 
            ui:setX( ui:getX() + dx )
            ui:setY( ui:getY() + dy )
        end
    end
end

-- UserInterface.getSumHeight(table_uis, [ is_array, pad ]) 
function UserInterface.getSumHeight(table_uis, is_array, pad) -- [testing]
    if not is_array then is_array = true end
    if not pad then pad = 0 end
    local h = pad
    if is_array then
        for i,ui in ipairs(table_uis) do 
            h = h + ui:getHeight()+pad
        end
    else
        for name,ui in pairs(table_uis) do 
            h = h + ui:getHeight()+pad
        end
    end
end

-- UserInterface.getSumWidth(table_uis, [ is_array, pad ])  
function UserInterface.getSumWidth(table_uis, is_array, pad) -- [testing]
    if not is_array then is_array = true end
    if not pad then pad = 0 end
    local w = pad
    if is_array then
        for i,ui in ipairs(table_uis) do 
            w = w + ui:getWidth()+pad
        end
    else
        for name,ui in pairs(table_uis) do 
            w = w + ui:getWidth()+pad
        end
    end
end

--
-- ====================================================================
-- Graphical User Interfaces

-- UserInterface.floatingButton( name, texture_path, [ texture_object, x,y, width, height ])
function UserInterface.floatingButton(name,texture_path, texture_object,x,y,width,height ) -- [ok]
    -- optional arguments
    if not x then x = getCore():getScreenWidth() - 100 end
    if not y then y = getCore():getScreenHeight() - 50 end
    if not width then width = 25 end
    if not height then height = 25 end
    local Texture = nil do 
        if texture_object then 
            Texture = texture_object
        else 
            Texture = getTexture(texture_path)
        end
    end
    -- object construction
    local BUTTON_PROTOTYPE = ISButton:derive(name)
    local BUTTON = BUTTON_PROTOTYPE:new(x, y, width, height, "", nil, nil)
    BUTTON:setImage(Texture)
    BUTTON:setVisible(true)
    BUTTON:setEnable(true)
    BUTTON:addToUIManager()
    return BUTTON
end

function UserInterface.textFloatingButton(name,x,y,width,height ) -- [testing]
    -- optional arguments
    if not x then x = getCore():getScreenWidth() - 100 end
    if not y then y = getCore():getScreenHeight() - 50 end
    if not width then width = 25 end
    if not height then height = 25 end
    -- object construction
    -- local BUTTON_PROTOTYPE = ISButton:derive(name)
    local BUTTON = ISButton:new(x, y, width, height, name, nil, nil)
    BUTTON:setVisible(true)
    BUTTON:setEnable(true)
    BUTTON:addToUIManager()
    return BUTTON
end

-- UserInterface.window(name,[ x,y,width,height ])
function UserInterface.window(name,x,y,width,height) -- [testing]
    if not x then x = getCore():getScreenWidth()/2 end
    if not y then y = getCore():getScreenHeight()/2 end
    if not width then width = 200 end
    if not height then height = 200 end
    local PANEL = ISCollapsableWindowJoypad:new(x,y,width ,height)
    PANEL.title = name
    PANEL:setVisible(false)
    PANEL:addToUIManager()
    return PANEL
end

local FONT_HGT_SMALL = getTextManager():getFontHeight(UIFont.Small)
-- UserInterface.childButton(parent, name, text, callback, [ x, y, width, height ])
function UserInterface.childButton(parent, name, text, callback, x, y, width, height) -- [testing]
    --
    local bottom = parent:getHeight()
    local btnWid = 60
    local btnHgt = math.max(FONT_HGT_SMALL + 3 * 2, 25)
    --
    if not x then x = 10 end
    if not y then y = bottom - btnHgt - 10 end
    if not width then width = btnWid end
    if not height then height = btnHgt end
    --
    parent[name] = ISButton:new(x, y, width, height, text, parent, callback)
    parent[name].internal = name;
    parent[name]:initialise();
    parent[name]:instantiate();
    parent[name].borderColor = {r=1, g=1, b=1, a=0.1};
    parent:addChild(parent[name])
    return parent[name]
end

-- UserInterface.childMultilineEntry(parent, name, [ x, y, width, height ])
function UserInterface.childMultilineEntry(parent, name, text,x, y, width, height) -- [testing]
    -- optional arguments
    if not x then x = 5 end
    if not y then y = 25 end
    if not width then width = parent:getWidth()*0.8 end
    if not height then height = parent:getHeight()*0.6 end
    if not text then text = "" end
    --
    parent[name] = ISTextEntryBox:new(text,x,y,width,height)
    parent[name]:initialise()
    parent[name]:instantiate()
    parent[name]:setMultipleLine(true)
    parent[name].javaObject:setMaxLines(10)
    parent[name].javaObject:setMaxTextLength(500)
    parent:addChild( parent[name] )
    parent[name]:setEditable(true) 
    return parent[name]
end

-- UserInterface.childSlider(parent, name, callback , [ x, y, width, height, default_value ] ) o (_newValue, parent, name)
function UserInterface.childSlider(parent, name, callback, x, y, width, height, default_value) -- [testing]
    if not x then x = 5 end
    if not y then y = 25 end
    if not width then width = parent:getWidth()*0.95 end
    if not height then height = 20 end
    if not default_value then default_value = 0.0 end
    parent[name] = ISSliderPanel:new( x, y, width, height, parent, nil )
    parent[name]:initialise()
    parent[name]:instantiate()
    parent[name].valueLabel = true
    parent[name]:setValues(0.0, 100.0, 0.1, 1.0)
	parent[name]:setCurrentValue(default_value, true)
    local old = parent[name].doOnValueChange
    parent[name].doOnValueChange = function( self, _newValue )
        old(self, _newValue)
        return callback(_newValue, parent, name)
    end
    parent:addChild(parent[name])
    return parent[name]
end

-- UserInterface.childTickBox(parent,[ x,y,w,h ])
function UserInterface.childTickBox(parent,x,y,w,h) -- [X]
    -- optional arguments
    if not x then x = 20 end
    if not y then y = 10 end
    if not w then w = 20 end
    if not h then h = 20 end
    -- 
	local tickBox = ISTickBox:new(x, y, w, h, "HELLO?")
	tickBox.choicesColor = {r=1, g=1, b=1, a=1}
	tickBox:initialise()
	parent:addChild(tickBox)
	--self.mainPanel:insertNewLineOfButtons(tickBox)
    --self.addY = self.addY + tickBox:getHeight() + 6
	return tickBox
end

local FONT_HGT_SMALL = getTextManager():getFontHeight(UIFont.Small)
local FONT_HGT_MEDIUM = getTextManager():getFontHeight(UIFont.Medium)
local FONT_HGT_LARGE = getTextManager():getFontHeight(UIFont.Large)

-- UserInterface.childLabel(parent, [ text, x, y, font, R, G, B, A ])
function UserInterface.childLabel(parent, text, x, y, font, R, G, B, A) -- [testing]
    -- optional args
    if not R then R = 1 end
    if not G then G = 1 end
    if not B then B = 1 end
    if not A then A = 1 end
    if not font then 
        font = UIFont.Small 
    elseif type(font)=="string" then 
        if font=="small" then 
            font = UIFont.Small
        elseif font=="medium" then 
            font = UIFont.Medium
        elseif font=="large" then 
            font = UIFont.Large 
        else 
            font = UIFont.Small
        end
    end
    if not x then x = 10 end
    if not y then y = 25 end
    if not text then text = "Label" end
    --
    if not font_height then font_height = getTextManager():getFontHeight( font ) end
    -- 
    local label = ISLabel:new(x, y, font_height, text, R, G, B, A, font, false)
    label:setX( label:getX()+label:getWidth()/2 )
	label:initialise()
	parent:addChild(label)
    return label
end

function UserInterface.childCheckBox(parent, name, x, y, width, height) -- [...]
    if not width then width = 20 end
    if not height then height = 20 end
    local tick = ISTickBox:new(x, y, width, height, name, nil, nil)
    tick:initialise()
    --
    
    --
    parent:addChild(tick)
    return tick
end

--
-- ====================================================================
-- Contexts

-- UserInterface.CInGame(inner_function) o (screen_width, screen_height, CORE)
-- ... basic context based using OnGameStart event
function UserInterface.CInGame(inner_function) -- [ok]
    local CORE = getCore()
    local screen_width = CORE:getScreenWidth()
    local screen_height = CORE:getScreenHeight()
    Events.OnGameStart.Add( function()
        inner_function(screen_width, screen_height, CORE)
    end )
end

-- UserInterface.CWindow(window_name, inner_function, [ window ]) o (widgets, window, screen_width, screen_height)
-- ... It's a context which create a new empty window and fill utilities as arguments in inner_function
-- ui_reference <- widgets:Button(name, text, callback, [ x, y, width, height ])
-- ui_reference <- widgets:Slider(name, callback, [ x, y, width, height, default_value ])
-- ui_reference <- widgets:MultiEntry(name, text, [ x, y, width, height ])
-- ui_reference <- widgets:Label([ text, x, y, font, R, G, B, A ])
function UserInterface.CWindow(window_name, inner_function, window) -- [~]
    Events.OnGameStart.Add( function()
        if not window then window = UserInterface.window(window_name) end
        local CORE = getCore()
        local screen_width = CORE:getScreenWidth()
        local screen_height = CORE:getScreenHeight()
        -- 
        local widgets = {}
        widgets.root = window 
        function widgets:Button(name, text, callback, x, y, width, height) -- [ok]
            return UserInterface.childButton(self.root, name, text, callback, x, y, width, height)
        end
        function widgets:Slider(name, callback, x, y, width, height, default_value) -- [ok]
            return UserInterface.childSlider(self.root, name, callback, x, y, width, height, default_value)
        end
        function widgets:MultiEntry(name, text,x, y, width, height) -- [ok]
            return UserInterface.childMultilineEntry(self.root, name, text,x, y, width, height)
        end
        function widgets:Label(text, x, y, font, R, G, B, A) -- [testing]
            return UserInterface.childLabel(self.root, text, x, y, font, R, G, B, A)
        end
        --
        inner_function(widgets, window, screen_width, screen_height)
    end )
end

--[[ Example ; CWindow 
UserInterface.CWindow("My Example Window", function(wdg, window) 
    -- Slider
    local label_sld = wdg:Label("My Slider")
    local sld = wdg:Slider("My Slider", function(value) 
        getSpecificPlayer(0):Say(tostring(value))
    end )
    UserInterface.verticalNext( label_sld, sld )
    -- Multiline Entry
    local label_ent = wdg:Label("My Entry")
    UserInterface.verticalNext( sld, label_ent )
    local ent = wdg:MultiEntry("My Text Entry")
    UserInterface.verticalNext( label_ent, ent )
    -- Button 
    local btn = wdg:Button("My Button", "Close", function() 
        window:setVisible(false)
    end )
    UserInterface.verticalNext( ent, btn )
    local btn_print = wdg:Button("My Button Print", "Say Hello", function() 
        getSpecificPlayer(0):Say("Hello")
    end )
    UserInterface.verticalNext( ent, btn_print )
    UserInterface.horizontalNext( btn, btn_print )
    -- size
    window:setHeight( window:getHeight()+70 )
    window:setWidth( window:getWidth()+10 )
    window:setResizable(false)
    -- key press to show the window
    Events.OnKeyPressed.Add( function(key) 
        if key ~= Keyboard.KEY_1 then return nil end
        getSpecificPlayer(0):Say("This is My Window")
        window:setVisible(true)
    end )
end )
--]]

--[[ Example ; CWindow, verticalOrdering, horizontalOrdering
UserInterface.CWindow("My Example Window", function(wdg, window) 
    -- Slider
    local label_sld = wdg:Label("My Slider")
    local sld = wdg:Slider("My Slider", function(value) 
        getSpecificPlayer(0):Say(tostring(value))
    end )
    -- Multiline Entry
    local label_ent = wdg:Label("My Entry")
    local ent = wdg:MultiEntry("My Text Entry")
    -- Button 
    local btn_bye = wdg:Button("Button Bye", "Bye", function() 
        getSpecificPlayer(0):Say("Bye")
        window:setVisible(false)
    end )
    local btn_hello = wdg:Button("Button Hello", "Hello", function() 
        getSpecificPlayer(0):Say("Hello")
    end )
    -- position and size
    UserInterface.verticalOrdering( { label_sld, sld, label_ent, ent, btn_bye } )
    UserInterface.verticalNext( ent, btn_hello )
    UserInterface.horizontalOrdering( { btn_bye, btn_hello } )
    window:setHeight( window:getHeight()+70 )
    window:setWidth( window:getWidth()+10 )
    window:setResizable(false)
    -- key press to show the window
    Events.OnKeyPressed.Add( function(key) 
        if key ~= Keyboard.KEY_1 then return nil end
        getSpecificPlayer(0):Say("This is My Window")
        window:setVisible(true)
    end )
end )
--]]

--
-- ====================================================================
-- Interaction Buttons

-- UserInterface.CFloatingButtonMenu( get_pos_ref_interface_object, order_function, inner_function, [ X,Y, side_size ] ) o (Add, buttons_table) 
-- ... Add(name, texture)
-- ... the callback for the buttons and the activation of the menu must be set elsewhere
-- ... the order_function must be on of the functions verticalNext verticalPrev horizontalNext horizontalPrev
-- ... get_pos_ref_interface_object is a function to get an interface object reference
function UserInterface.CFloatingButtonMenu( get_pos_ref_interface_object, order_function, inner_function, getX,getY, side_size  ) -- [testing]
	local Interface = {}
	Interface.buttons = {} -- buttons table to be returned
	if not getX then getX = function() return 0 end end
	if not getY then getY = function() return 0 end end
	if not side_size then side_size = 45 end
	-- ... create the button objects using the inner_function content
    Events.OnGameStart.Add( function() 
		local X = getX()
		local Y = getY()
        local function Add(name, texture)  -- [testing] [verified](1)
            if type(texture) == "string" then
                Interface.buttons[name] = UserInterface.floatingButton(name,UserInterface.texture_path..texture, nil, X,Y,side_size,side_size)
            else 
                Interface.buttons[name] = UserInterface.floatingButton(name,nil, texture, X,Y,side_size,side_sizes)
            end
            Interface.buttons[name]:setVisible(false)
            Interface.buttons[name].backgroundColor.a = 0
            return Interface.buttons[name]
        end
        -- call activate once
        inner_function(Add, Interface.buttons)
        -- ... Add(name, callback, texture)
    end )
    -- ... set the position with the order_function
    function Interface:Position(pad) -- [testing] 
		local pos_ref = get_pos_ref_interface_object()
		if not pos_ref:isVisible() then return nil end
		-- optional arguments
		if not pad then pad = 10 end
		--
		local previous_ui = nil
		for name,ui in pairs(self.buttons) do
			if ui:isVisible() then
				if previous_ui ~= nil then 
					order_function(previous_ui, ui, pad)
				else
					order_function(pos_ref, ui, pad)
				end
				previous_ui = ui
			end
		end
    end
    -- ... hide all floating buttons 
    function Interface:Hide() -- [testing]
		for name, ui in pairs(self.buttons) do 
			ui:setVisible(false)
		end
    end
    -- ... get the button object by his name
    function Interface:Get(name) -- [testing]
		return self.buttons[name]
    end
    -- ... set the callback for the button by name
    function Interface:OnClick(name, callback, b_hide) -- [testing]
		if not b_hide then b_hide = true end
		UserInterface.setOnClick(self.buttons[name], function() 
			callback()
			if b_hide then self:Hide() end
		end )
    end
    -- Interface:CSetCallbacks( inner_function_2 ) o (OnClick, Hide)
    -- ... context to set all callbacks at same block
    function Interface:CSetCallbacks( inner_function_2 ) -- [testing]
		inner_function_2( 
			function(name, callback, b_hide) return self:OnClick(name, callback, b_hide) end, 
			function() return self:Hide() end 
		)
    end
    -- ... set the function to get the corresponding key for the event key pressed
    function Interface:setKeyCheck( check ) -- [testing]
		self.keyCheck = check
    end
    -- ... to be called on event OnKeyPressed
    -- ... this function should be overrided using OKeyPressed
    function Interface.keyCheck(key) return true end
    -- 
    function Interface:OnKeyPress(key) -- [testing]
		--local PLAYER = getSpecificPlayer(0) -- debug
		--PLAYER:Say("check key: "..tostring(key) ) -- debug
		if not self.keyCheck(key) then return nil end
		return true
    end
    -- ... in this context the visibility, positioning, and callback should be set
    function Interface:OKeyPressed( inner_function, keyCheck ) -- [testing]
		if keyCheck ~= nil then self:setKeyCheck( keyCheck ) end
		--
		local old_OnKeyPress = self.OnKeyPress
		self.OnKeyPress = function(this, key) 
			if old_OnKeyPress(self,key) == nil then return nil end
			return inner_function(
				function(name) return self:Get(name) end, -- Get the button object by name
				function(pad) return self:Position(pad) end, -- calculate and set the position
				function(inner_function_2) return self:CSetCallbacks( inner_function_2 ) end, -- Context for setting the callbacks
				function() return self:Hide() end, -- hide all buttons
				key
			)
		end
    end
    -- 
    function Interface:start() 
		Events.OnKeyPressed.Add( function(key) return self:OnKeyPress(key) end )
    end
    --
    return Interface
end

--
-- ====================================================================
--> Fixed Point Animation

function UserInterface.fixedPointPositioningAnimationX(interface_object, target_X, initial_X, num_iterations) -- [testing]
    if not num_iterations then num_iterations = 1 end
    local t = ( interface_object:getX() - target_X )/( initial_X - target_X )
    for i = 1,num_iterations do t = math.sin(t) end
    interface_object:setX( target_X*(1-t)+t*initial_X )
    -- X = target_X*(1-t)+t*initial_X
    -- X = target_X - target_X*t + initial_X*t
    -- X - target_X = (initial_X - target_X)*t
    -- (X - target_X)/(initial_X - target_X) = t
end

function UserInterface.fixedPointPositioningAnimationY(interface_object, target_Y, initial_Y, num_iterations) -- [testing]
    if not num_iterations then num_iterations = 1 end
    local t = ( interface_object:getY() - target_Y )/( initial_Y - target_Y )
    for i = 1,num_iterations do t = math.sin(t) end
    interface_object:setY( target_Y*(1-t)+t*initial_Y )
    -- X = target_X*(1-t)+t*initial_X
    -- X = target_X - target_X*t + initial_X*t
    -- X - target_X = (initial_X - target_X)*t
    -- (X - target_X)/(initial_X - target_X) = t
end

--
-- ====================================================================
-- 

return UserInterface




























