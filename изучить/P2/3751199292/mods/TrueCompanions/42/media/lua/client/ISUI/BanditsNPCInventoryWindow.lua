--
-- Bandits NPC - Companions Overhaul - Companion inventory (Phase 5)
--
-- A two-pane give/take window between the player and a recruited companion's
-- real entity inventory (zombie:getInventory()). Items given persist on the
-- NPC and drop with their corpse on death. No values/currency here -- buying
-- and selling is reserved for hand-placed trader NPCs later.
--

require "ISUI/ISCollapsableWindow"
require "ISUI/ISScrollingListBox"
require "ISUI/ISButton"
require "ISUI/BanditsNPCItemList"

BanditsNPCInventoryWindow = ISCollapsableWindow:derive("BanditsNPCInventoryWindow")

local FONT_HGT = getTextManager():getFontHeight(UIFont.Small)
local PAD = 10
local BTN_H = FONT_HGT + 8

-- `player` set = the item is coming OFF the player, so release it from their body first
-- (worn items, hand slots and the hotbar all keep references that survive a bare Remove
-- and leave the gear painted on the model). Coming off the companion needs none of that:
-- a bandit's outfit is painted from brain.clothing, not from real worn items.
-- Would this item fit? Both panes DISPLAY "used / max" (see weightStr below), and
-- nothing enforced it -- so a companion could be loaded to 210.0 / 30.0 and the window
-- would cheerfully say so. Displaying a limit implies it means something (v0.75.30).
-- The capacity rule lives on ItemList now (IL.Fits) so the Gear tab's whole-stack
-- transfers apply exactly the same test; this is the alias the rest of the file uses.
local function fits(item, target)
    return BanditsNPC.ItemList.Fits(item, target)
end

-- Returns true if the item actually moved.
local function moveItem(item, target, player)
    if not fits(item, target) then return false end
    if player then
        BanditsNPC.Interact.TakeFromPlayer(player, item)
    else
        local from = item:getContainer()
        if from then from:Remove(item) end
    end
    target:addItem(item)
    return true
end

function BanditsNPCInventoryWindow:createChildren()
    ISCollapsableWindow.createChildren(self)

    local top = self:titleBarHeight() + PAD
    local colW = (self.width - PAD * 3) / 2
    local leftX = PAD
    local rightX = PAD * 2 + colW
    local listY = top + FONT_HGT * 2 + 8
    local listH = self.height - listY - (PAD + BTN_H + PAD)

    self.leftX, self.rightX, self.colW, self.headerY = leftX, rightX, colW, top

    local win = self
    local rowH = BanditsNPC.ItemList.RowHeight(UIFont.Small)

    self.npcList = ISScrollingListBox:new(leftX, listY, colW, listH)
    self.npcList:initialise()
    self.npcList.itemheight = rowH
    self.npcList.font = UIFont.Small
    self.npcList:setOnMouseDownFunction(self, self.onSelectNpc)
    self:addChild(self.npcList)
    self.npcList.doDrawItem = function(lb, yy, entry, alt)
        return BanditsNPC.ItemList.DrawRow(lb, yy, entry, alt,
            { font = UIFont.Small, selected = win.selNpc })
    end

    self.playerList = ISScrollingListBox:new(rightX, listY, colW, listH)
    self.playerList:initialise()
    self.playerList.itemheight = rowH
    self.playerList.font = UIFont.Small
    self.playerList:setOnMouseDownFunction(self, self.onSelectPlayer)
    self:addChild(self.playerList)
    self.playerList.doDrawItem = function(lb, yy, entry, alt)
        return BanditsNPC.ItemList.DrawRow(lb, yy, entry, alt,
            { font = UIFont.Small, selected = win.selPlayer })
    end

    local by = self.height - PAD - BTN_H
    self.takeBtn = ISButton:new(leftX, by, colW, BTN_H, BanditsNPC.T("UI_BN_TakeSelected", "<< Take selected"), self, self.onTake)
    self.takeBtn:initialise(); self.takeBtn:instantiate(); self:addChild(self.takeBtn)

    self.giveBtn = ISButton:new(rightX, by, colW, BTN_H, BanditsNPC.T("UI_BN_GiveSelected", "Give selected >>"), self, self.onGive)
    self.giveBtn:initialise(); self.giveBtn:instantiate(); self:addChild(self.giveBtn)

    local closeBtn = ISButton:new(self.width - PAD - 70, self:titleBarHeight() + 1, 70, self:titleBarHeight() - 2, BanditsNPC.T("UI_BN_Close", "Close"), self, self.onClose)
    closeBtn:initialise(); closeBtn:instantiate(); self:addChild(closeBtn)

    self:refresh()
end

function BanditsNPCInventoryWindow:refresh()
    local player = getSpecificPlayer(0)
    if not player or not self.zombie then self:onClose(); return end

    -- Both panes are nested trees now (bags shown as group rows with their contents
    -- indented beneath), and the player side withholds equipped gear -- the old
    -- worn-items-only skip set missed the bag on your back, hand items and hotbar
    -- attachments, all of which live in the player's inventory like anything else.
    local npcInv = self.zombie:getInventory()
    BanditsNPC.ItemList.Fill(self.npcList, BanditsNPC.ItemList.Build(npcInv, {}))
    BanditsNPC.ItemList.Fill(self.playerList, BanditsNPC.ItemList.Build(player:getInventory(), {
        player = player,
        hideEquipped = true,
    }))

    self.selNpc = nil
    self.selPlayer = nil
end

function BanditsNPCInventoryWindow:onSelectNpc(item) self.selNpc = item end
function BanditsNPCInventoryWindow:onSelectPlayer(item) self.selPlayer = item end

-- Move the WHOLE row. Identical items collapse to one stacked row now, so moving a single
-- item per press would need five presses to shift "Screws x5" and would look, for the first
-- four of them, as though nothing had happened. Stops at the first item that will not fit
-- and says so, rather than silently moving part of the stack without comment.
local function moveStack(lb, item, target, player)
    local stack = BanditsNPC.ItemList.StackOf(lb, item)
    local moved, blocked = 0, false
    for _, s in ipairs(stack) do
        if moveItem(s, target, player) then moved = moved + 1 else blocked = true; break end
    end
    return moved, blocked
end

function BanditsNPCInventoryWindow:onTake()
    if not self.selNpc then return end
    local player = getSpecificPlayer(0)
    local moved, blocked = moveStack(self.npcList, self.selNpc, player:getInventory())
    if moved == 0 then
        self.msg = BanditsNPC.T("UI_BN_Inv_TooHeavy", "That won't fit.")
        return
    end
    self.msg = blocked and BanditsNPC.T("UI_BN_Inv_PartialFit", "Only part of that fits.") or nil
    self:refresh()
end

function BanditsNPCInventoryWindow:onGive()
    if not self.selPlayer then return end
    local moved, blocked = moveStack(self.playerList, self.selPlayer, self.zombie:getInventory(), getSpecificPlayer(0))
    if moved == 0 then
        self.msg = BanditsNPC.T("UI_BN_Inv_TooHeavy", "That won't fit.")
        return
    end
    self.msg = blocked and BanditsNPC.T("UI_BN_Inv_PartialFit", "Only part of that fits.") or nil
    self:refresh()
end

function BanditsNPCInventoryWindow:onClose()
    self:removeFromUIManager()
    if BanditsNPCInventoryWindow.instance == self then
        BanditsNPCInventoryWindow.instance = nil
    end
end

local function weightStr(container)
    local ok, cur = pcall(function() return container:getCapacityWeight() end)
    local ok2, max = pcall(function() return container:getMaxWeight() end)
    if ok and ok2 and cur and max then
        return string.format("%.1f / %.1f", cur, max)
    end
    return ""
end

function BanditsNPCInventoryWindow:prerender()
    ISCollapsableWindow.prerender(self)
    local npcInv = self.zombie and self.zombie:getInventory()
    local player = getSpecificPlayer(0)

    self:drawText(BanditsNPC.T("UI_BN_Inv_Their", "Their belongings"), self.leftX, self.headerY, 1, 1, 1, 1, UIFont.Small)
    self:drawText(BanditsNPC.T("UI_BN_Inv_Yours", "Your inventory"), self.rightX, self.headerY, 1, 1, 1, 1, UIFont.Small)

    if npcInv then
        self:drawText(weightStr(npcInv), self.leftX, self.headerY + FONT_HGT + 2, 0.8, 0.8, 0.8, 1, UIFont.Small)
    end
    if player then
        self:drawText(weightStr(player:getInventory()), self.rightX, self.headerY + FONT_HGT + 2, 0.8, 0.8, 0.8, 1, UIFont.Small)
    end
    -- A REFUSAL MUST BE VISIBLE (v0.75.30). moveItem can now decline a transfer that
    -- would exceed the shown limit, and a button that silently does nothing is exactly
    -- the failure this audit spent its length removing -- so say why, next to the two
    -- weights the decision was made from.
    if self.msg then
        self:drawText(self.msg, self.leftX, self.height - 26, 0.95, 0.75, 0.4, 1, UIFont.Small)
    end
end

function BanditsNPCInventoryWindow:new(zombie)
    local w, h = 580, 480
    local x = (getCore():getScreenWidth() - w) / 2
    local y = (getCore():getScreenHeight() - h) / 2
    local o = ISCollapsableWindow.new(self, x, y, w, h)
    o.zombie = zombie
    local brain = BanditBrain.Get(zombie)
    o.title = BanditsNPC.T("UI_BN_Title_Items", "Items") .. " - " .. ((brain and brain.fullname) or BanditsNPC.T("UI_BN_Companion", "Companion"))
    o.resizable = false
    return o
end

function BanditsNPCInventoryWindow.OpenFor(zombie)
    if BanditsNPCInventoryWindow.instance then
        BanditsNPCInventoryWindow.instance:onClose()
    end
    local win = BanditsNPCInventoryWindow:new(zombie)
    win:initialise()
    win:addToUIManager()
    BanditsNPCInventoryWindow.instance = win
    return win
end
