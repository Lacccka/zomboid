--
-- Bandits NPC - Companions Overhaul - Give Item picker
--
-- A small window that lets the player choose a SPECIFIC item from their own
-- inventory (optionally filtered) and hand it over -- so recruitment never
-- silently eats an item you wanted to keep. Reusable: the eventual trade UI
-- can open it with no filter.
--
-- Open with:
--   BanditsNPCGiveWindow.Open({
--       title = "...", instruction = "...",
--       filter = function(item) return bool end,   -- nil = any item
--       confirmLabel = "Give",
--       onConfirm = function(item) ... end,         -- chosen item
--   })
--

require "ISUI/ISCollapsableWindow"
require "ISUI/ISScrollingListBox"
require "ISUI/ISButton"
require "ISUI/BanditsNPCItemList"

BanditsNPCGiveWindow = ISCollapsableWindow:derive("BanditsNPCGiveWindow")

local FONT_HGT = getTextManager():getFontHeight(UIFont.Small)
local PAD = 10
local BTN_H = FONT_HGT + 8

function BanditsNPCGiveWindow:createChildren()
    ISCollapsableWindow.createChildren(self)

    local top = self:titleBarHeight() + PAD
    self.instructionBottom = top + (FONT_HGT + 2) * 3 + 6

    -- selected-item slot (drawn in render)
    self.slotX = PAD
    self.slotY = self.instructionBottom
    self.slotW = self.width - PAD * 2
    self.slotH = 44

    -- one dim line explaining the absence of your own gear, so "where is my backpack?"
    -- never has to be guessed at
    self.noteY = self.slotY + self.slotH + 5

    -- list of eligible items
    local listY = self.noteY + FONT_HGT + 5
    local listH = self.height - listY - (PAD + BTN_H + PAD)
    self.itemList = ISScrollingListBox:new(PAD, listY, self.width - PAD * 2, listH)
    self.itemList:initialise()
    self.itemList.itemheight = BanditsNPC.ItemList.RowHeight(UIFont.Small)
    self.itemList.font = UIFont.Small
    self.itemList:setOnMouseDownFunction(self, self.onSelectItem)
    self:addChild(self.itemList)
    local win = self
    self.itemList.doDrawItem = function(lb, yy, entry, alt)
        return BanditsNPC.ItemList.DrawRow(lb, yy, entry, alt,
            { font = UIFont.Small, selected = win.selected })
    end

    local by = self.height - PAD - BTN_H
    self.giveBtn = ISButton:new(PAD, by, 150, BTN_H, self.opts.confirmLabel or BanditsNPC.T("UI_BN_Give", "Give"), self, self.onGive)
    self.giveBtn:initialise(); self.giveBtn:instantiate()
    self:addChild(self.giveBtn)

    local cancelBtn = ISButton:new(self.width - PAD - 90, by, 90, BTN_H, BanditsNPC.T("UI_BN_Cancel", "Cancel"), self, self.onCancel)
    cancelBtn:initialise(); cancelBtn:instantiate()
    self:addChild(cancelBtn)

    self:populate()
end

-- The list used to be a flat getAllEvalRecurse dump of the player's inventory, which
-- offered up WORN CLOTHING, HAND ITEMS and the BAG ON YOUR BACK as if they were loose
-- (they all physically live in that inventory -- see BanditsNPCItemList.EquipKind), and
-- showed the contents of every bag at the same level as the bag itself. Now it is a
-- nested tree, and whether your own gear is offered depends on the flow:
--
--   opts.hideEquipped = true (DEFAULT, the Items-tab "hand her whatever you like" give)
--       -- browsing everything you own must not put the shirt off your back one click from
--          being given away by accident. Your worn bag is still shown as an unselectable
--          group header, because withholding it must not hide its contents too.
--   opts.hideEquipped = false (the RECRUITMENT gift)
--       -- the NPC asked for one specific thing and the filter has already narrowed the
--          list to it. Refusing the axe in your hands there is just a dead end, and it is
--          safe now: onConfirm goes through Interact.TakeFromPlayer, which unequips the
--          vanilla way before the item changes owner. Equipped rows are labelled
--          "worn" / "in hands" / "holstered" so nothing is handed over unknowingly.
function BanditsNPCGiveWindow:populate()
    self.selected = nil
    self.hideEquipped = (self.opts.hideEquipped ~= false)

    local player = getSpecificPlayer(0)
    if not player then self.itemList:clear(); return end

    -- keep the scroll position across a re-populate: the Items tab gives in a loop
    -- (stayOpen) and snapping back to the top after every single item was maddening
    local scroll = self.itemList.getYScroll and self.itemList:getYScroll() or nil

    local rows = BanditsNPC.ItemList.Build(player:getInventory(), {
        player = player,
        filter = self.opts.filter,
        hideEquipped = self.hideEquipped,
    })
    BanditsNPC.ItemList.Fill(self.itemList, rows)

    -- One dim line saying which rule is in force. In the withholding case it only earns the
    -- line if something actually WAS withheld, so a naked character sees no note.
    self.noteText = nil
    if self.hideEquipped then
        local withheld = false
        for _, r in ipairs(rows) do
            if r.header then withheld = true; break end
        end
        if not withheld then
            local worn = player:getWornItems()
            if worn and worn:size() > 0 then withheld = true end
        end
        if withheld then
            self.noteText = BanditsNPC.T("UI_BN_Give_EquippedHeld",
                "Worn and equipped gear is not offered.")
        end
    else
        self.noteText = BanditsNPC.T("UI_BN_Give_EquippedOffered",
            "You can give worn gear too. It comes off by itself.")
    end

    if scroll and self.itemList.setYScroll then
        pcall(function() self.itemList:setYScroll(scroll) end)
    end
end

function BanditsNPCGiveWindow:onSelectItem(item)
    self.selected = item
end

function BanditsNPCGiveWindow:onGive()
    if not self.selected then return end
    local cb = self.opts.onConfirm
    local item = self.selected
    if self.opts.stayOpen then
        -- multi-give mode (Items tab): hand it over and refresh the list so the next
        -- item can be given without reopening the window every time
        if cb then cb(item) end
        self:populate()
    else
        self:onCancel()          -- close first (single-give flows, e.g. recruiting)
        if cb then cb(item) end
    end
end

function BanditsNPCGiveWindow:onCancel()
    self:removeFromUIManager()
    if BanditsNPCGiveWindow.instance == self then
        BanditsNPCGiveWindow.instance = nil
    end
end

function BanditsNPCGiveWindow:prerender()
    ISCollapsableWindow.prerender(self)
    -- slot background
    self:drawRect(self.slotX, self.slotY, self.slotW, self.slotH, 0.4, 0, 0, 0)
    self:drawRectBorder(self.slotX, self.slotY, self.slotW, self.slotH, 1, 0.5, 0.5, 0.5)
end

function BanditsNPCGiveWindow:render()
    ISCollapsableWindow.render(self)

    local top = self:titleBarHeight() + PAD
    if self.opts.instruction then
        self:drawTextWrapped(self.opts.instruction, PAD, top, self.width - PAD * 2)
    end

    -- slot contents
    local sx, sy = self.slotX + 6, self.slotY + (self.slotH - 32) / 2
    if self.selected then
        local drew = false
        pcall(function()
            local tex = self.selected:getTexture()
            if tex then
                self:drawTextureScaled(tex, sx, sy, 32, 32, 1, 1, 1, 1)
                drew = true
            end
        end)
        local tx = drew and (sx + 40) or sx
        self:drawText(self.selected:getName(), tx, self.slotY + (self.slotH - FONT_HGT) / 2, 1, 1, 1, 1, UIFont.Small)
    else
        self:drawText(BanditsNPC.T("UI_BN_Give_ClickItem", "Click an item below to place it here."), sx, self.slotY + (self.slotH - FONT_HGT) / 2, 0.7, 0.7, 0.7, 1, UIFont.Small)
    end

    if self.noteText then
        self:drawText(self.noteText, PAD + 2, self.noteY, 0.62, 0.66, 0.72, 1, UIFont.Small)
    end

    if self.itemList and self.itemList.items and #self.itemList.items == 0 then
        self:drawText(BanditsNPC.T("UI_BN_Give_NothingSuitable", "You have nothing suitable to give."), PAD + 4, self.itemList.y + 4, 0.8, 0.6, 0.6, 1, UIFont.Small)
    end
end

-- simple word-wrap
function BanditsNPCGiveWindow:drawTextWrapped(text, x, y, maxWidth)
    -- breaking is shared (BanditsNPC.WrapLines); the drawing and the y contract stay here
    local lines = BanditsNPC.WrapLines(text, UIFont.Small, maxWidth)
    for i = 1, #lines do
        self:drawText(lines[i], x, y, 0.9, 0.9, 0.9, 1, UIFont.Small)
        if i < #lines then y = y + FONT_HGT + 2 end
    end
end

function BanditsNPCGiveWindow:new(opts)
    -- taller than the old 440: the rows now carry icons and the list lost a line to the
    -- "worn gear is not offered" note
    local w, h = 400, 480
    local x = (getCore():getScreenWidth() - w) / 2
    local y = (getCore():getScreenHeight() - h) / 2
    local o = ISCollapsableWindow.new(self, x, y, w, h)
    o.opts = opts or {}
    o.title = o.opts.title or BanditsNPC.T("UI_BN_Give_Title", "Give Item")
    o.resizable = false
    return o
end

function BanditsNPCGiveWindow.Open(opts)
    if BanditsNPCGiveWindow.instance then
        BanditsNPCGiveWindow.instance:onCancel()
    end
    local win = BanditsNPCGiveWindow:new(opts)
    win:initialise()
    win:addToUIManager()
    BanditsNPCGiveWindow.instance = win
    return win
end
