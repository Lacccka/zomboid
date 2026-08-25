--
-- Bandits NPC - Companions Overhaul - Dialogue Window
--
-- Opened by the "Talk to NPC" key / right-click. Three sections:
--   HEADER (full width): name, status, Health/needs/affinity bars.
--   TABS:   Story / Orders / Quest / Skills / Items / Work / Anim.
--   FOOTER: Close (left), Talk + Recruit/Dismiss (right) -- always visible.
-- Conversation itself is the separate frameless Talk overlay.
--

require "ISUI/ISCollapsableWindow"
require "ISUI/ISButton"
require "ISUI/ISScrollingListBox"
require "ISUI/BanditsNPCGiveWindow"
require "ISUI/BanditsNPCItemList"
require "ISUI/BanditsNPCTalkWindow"
require "ISUI/BanditsNPCScheduleWindow"

BanditsNPCDialogWindow = ISCollapsableWindow:derive("BanditsNPCDialogWindow")

-- UI scaling (sandbox option BanditsNPC.UIScale). The window, fonts and spacing scale
-- by this so the UI can be enlarged on high-res screens. Sandbox vars aren't ready at
-- mod load, so these are (re)computed when a window opens (bnApplyScale, called from
-- OpenFor) -- reopen the window after changing the option.
local SCALE = 1
local SMALL_FONT = UIFont.Small
local MED_FONT = UIFont.Medium
local FONT_HGT = getTextManager():getFontHeight(UIFont.Small)
local FONT_MED = getTextManager():getFontHeight(UIFont.Medium)
local PAD = 10
local BTN_H = FONT_HGT + 8
local function sc(v) return math.floor(v * (SCALE or 1)) end

-- FIX(TASK-020): position one widget, tolerating nil. `layout()` runs from
-- `createChildren` while some children still do not exist and from `onResize` when they
-- all do, so every call site would otherwise need its own `if self.x then` guard --
-- twenty-eight of them, and the one somebody forgets is a nil-index crash on drag.
local function place(c, x, y, w, h)
    if not c then return end
    c:setX(x); c:setY(y)
    if w then c:setWidth(w) end
    if h then c:setHeight(h) end
    -- FIX(TASK-020): **A SCROLLING LIST'S SCROLLBAR DOES NOT FOLLOW ITS PARENT ON THE LUA
    -- SIDE, AND THE LIST'S OWN STENCIL IS COMPUTED FROM IT.**
    --
    -- `ISScrollBar:instantiate` fixes `self.x = self.parent.width - 16` and
    -- `self.height = self.parent.height` ONCE, at creation
    -- (refs/pz-lua/.../ISScrollBar.lua:268-280). `ISScrollingListBox:instantiate` calls
    -- `addScrollBars()` (`:63`), which runs from `addChild` -- and TASK-020 builds every
    -- list at `0,0,0,0`, so the bar was created with **x = -16 and height = 0**. Java
    -- anchors then stretch its JAVA rect with the parent, but the LUA `x` stays -16
    -- forever, and `ISScrollingListBox:prerender` reads exactly that field:
    --     `if self:isVScrollBarVisible() then stencilX2 = self.vscroll.x + 3 end` (`:495`)
    -- so the stencil collapses to a negative width and the rows are clipped away while
    -- remaining fully clickable -- hit-testing does not consult the stencil.
    --
    -- **THAT ALSO EXPLAINS THE "SNAPS IN PAST A THRESHOLD" BEHAVIOUR**, which is not a
    -- negative width being clamped: `isVScrollBarVisible()` is
    -- `vscroll:getHeight() < getScrollHeight()`, so once the window is tall enough that
    -- every row fits, the branch above is skipped, `stencilX2` stays `self.width`, and the
    -- list snaps to correct. A list that always needs to scroll (Anim) never gets there
    -- and is blank at every size.
    --
    -- Two lines, and they are vanilla's own idiom for this -- ISCraftInventoryPanel.lua:
    -- 120-121, ISWidgetRecipeListPanel.lua:70-71, ISInventoryPane.lua:1786. Guarded on
    -- `c.vscroll` so the same helper still serves the plain buttons, which have none.
    if c.vscroll then
        c.vscroll:setX(c:getWidth() - c.vscroll:getWidth())
        c.vscroll:setHeight(c:getHeight())
    end
end
-- Thresholds and maths live in BanditsNPC.UIScaleValues so this window and the roster
-- panel cannot drift apart; the locals stay here because every line below uses them.
local function bnApplyScale()
    SCALE, SMALL_FONT, MED_FONT, FONT_HGT, FONT_MED, PAD, BTN_H =
        BanditsNPC.UIScaleValues()
end

-- id = STABLE layout/lookup key (English, never translated -- EQUIP_LAYOUT is
-- keyed by it); label = the translated caption shown under the box. Keeping these
-- separate is the fix for "slots disappear when a language is applied": the old
-- code looked EQUIP_LAYOUT up by the translated label, which no longer matched.
local EQUIP_SLOTS = {
    {id="Head",    label=BanditsNPC.T("UI_BN_Body_Head", "Head"),    locs={"FullHat","FullHelmet","Hat","Head","Wig","Hood","Scarf","Neck"}},
    {id="Face",    label=BanditsNPC.T("UI_BN_Body_Face", "Face"),    locs={"MaskFull","MaskEyes","Mask","Eyes"}},
    {id="Torso",   label=BanditsNPC.T("UI_BN_Body_Torso", "Torso"),   locs={"FullSuit","Jacket","BathRobe","Sweater","Shirt","ShortSleeveShirt","Tshirt","TankTop","UnderwearTop"}},
    {id="Vest",    label=BanditsNPC.T("UI_BN_Body_Vest", "Vest"),    locs={"TorsoExtraVestBullet","TorsoExtraVest","Webbing","TorsoRig","TorsoExtra"}},
    {id="Hands",   label=BanditsNPC.T("UI_BN_Body_Hands", "Hands"),   locs={"Hands","HandsLeft","HandsRight"}},
    {id="Belt",    label=BanditsNPC.T("UI_BN_Body_Belt", "Belt"),    locs={"Belt","BeltExtra","FannyPackFront","FannyPackBack"}},
    {id="Legs",    label=BanditsNPC.T("UI_BN_Body_Legs", "Legs"),    locs={"Pants","LongSkirt","Skirt","ShortPants","LowerBody","UnderwearBottom"}},
    {id="Feet",    label=BanditsNPC.T("UI_BN_Body_Feet", "Feet"),    locs={"Shoes","Socks"}},
    {id="Back",    label=BanditsNPC.T("UI_BN_Body_Back", "Back"),    locs={"Back","LowerBack"}},
    {id="Jewelry", label=BanditsNPC.T("UI_BN_Body_Jewelry", "Jewelry"), locs={"Necklace","Necklace_Long"}},
}

-- ===========================================================================
-- CLOTHING THE SLOT LISTS HAVE NEVER HEARD OF (v0.77.13)
-- ===========================================================================
--
-- "Certain clothes aren't detected by your interface" (F242, 13 Aug). EQUIP_SLOTS is a
-- fixed list of body locations, so a garment in ANY location not spelled out above is
-- simply not drawn -- she is visibly wearing it and the Gear tab says the slot is empty.
-- That covers most modded clothing, which invents its own locations, and a fair number of
-- vanilla ones nobody thought to list (Apron, Dress, AmmoStrap, SweaterHat...).
--
-- Our own dressing code already handles this correctly -- ApplyClothingVisuals walks
-- Bandits' ordered locations and THEN "any MODDED/custom locations its list doesn't know"
-- (BanditsNPCInteract.lua) -- so the mod puts the garment on her and only the window
-- cannot see it. Matching the display to the behaviour, rather than growing the lists
-- forever and still missing the next mod:
--
--   1. exact location match, in each slot's own priority order (unchanged);
--   2. anything left over goes to the anatomically nearest slot THAT IS STILL EMPTY,
--      by substring -- so a "PlateCarrierVest" lands on Vest and a "WinterHat" on Head.
--
-- Order matters in the hint list: "SweaterHat" must meet "Hat" before "Sweater", so the
-- headwear hints come first. Two unknown locations competing for the same empty slot is
-- resolved arbitrarily (pairs order) -- one of them shows, which beats neither showing.
local LOC_SLOT = {}     -- filled below, once EQUIP_SLOTS exists
local EQUIP_HINTS = {
    { "Hat", "Head" },      { "Helmet", "Head" },   { "Hood", "Head" },
    { "Head", "Head" },     { "Ear", "Head" },      { "Wig", "Head" },
    { "Mask", "Face" },     { "Eye", "Face" },      { "Nose", "Face" },
    { "Vest", "Vest" },     { "Rig", "Vest" },      { "Webbing", "Vest" },
    { "Strap", "Vest" },    { "Holster", "Vest" },
    { "Belt", "Belt" },     { "Pack", "Belt" },
    { "Glove", "Hands" },   { "Hand", "Hands" },
    { "Shoe", "Feet" },     { "Sock", "Feet" },     { "Foot", "Feet" },
    { "Back", "Back" },
    { "Necklace", "Jewelry" }, { "Ring", "Jewelry" }, { "Finger", "Jewelry" },
    { "Neck", "Jewelry" },
    { "Pant", "Legs" },     { "Skirt", "Legs" },    { "Leg", "Legs" },
    { "Torso", "Torso" },   { "Jacket", "Torso" },  { "Shirt", "Torso" },
    { "Sweater", "Torso" }, { "Suit", "Torso" },    { "Dress", "Torso" },
    { "Apron", "Torso" },   { "Coat", "Torso" },
}

local function slotForLoc(loc)
    local known = LOC_SLOT[loc]
    if known then return known end
    for _, h in ipairs(EQUIP_HINTS) do
        if loc:find(h[1], 1, true) then return h[2] end
    end
    return "Torso"
end

-- ANATOMICAL, not a 3-column grid (v0.77.2, from the brief): head and face on top, then
-- RHand / Torso / LHand, Back / Belt / Vest, Jewelry / Hands, Legs, Feet. The old layout
-- put Back beside Head and both hands down the left edge, which read as a list of slots
-- rather than as a person. Columns are 0/1/2 at a 34px pitch, rows at 40 (26 for the box,
-- the rest for the caption under it).
for _, s in ipairs(EQUIP_SLOTS) do
    for _, l in ipairs(s.locs) do LOC_SLOT[l] = s.id end
end

local EQUIP_BOX = 26
local EQUIP_LAYOUT = {
                              Head    = {x=44,  y=0},   Face    = {x=88,  y=0},
    RHand   = {x=0,   y=40},  Torso   = {x=44,  y=40},  LHand   = {x=88,  y=40},
    Back    = {x=0,   y=80},  Belt    = {x=44,  y=80},  Vest    = {x=88,  y=80},
    Jewelry = {x=0,   y=120},                           Hands   = {x=88,  y=120},
                              Legs    = {x=44,  y=160},
                              Feet    = {x=44,  y=200},
}

-- Performable "bump-type" animations (the only anims a hijacked-zombie bandit can
-- play). Grouped for readability; all are names the engine/Bandits recognise.
local ANIMS = {
    -- idle / gestures
    "Loot", "LootLow", "Forage", "ShiftWeight", "Shrug", "PullAtCollar", "ChewNails",
    "Smoke", "Cough", "Sneeze", "WipeBrow", "WipeHead", "Exhausted", "Surrender",
    -- sitting / resting
    "Sit", "SitAction", "SitMaking", "SitRubHands", "Sleep", "Eat", "Faint", "GetUp",
    -- work / tools (closest we get to "using a tool")
    "RemoveBarricadeCrowbarHigh", "RemoveBarricadeCrowbarMid", "BlowtorchHigh", "Refuel",
    "DigShovel", "Rake", "PourWateringCan", "FillBucket", "ChopTree", "WindowSmash",
    -- medic (bandaging). NO PLAIN "Bandage" (v0.77.14): Bandits ships seven bandage nodes
    -- and every one names a body part -- there is no node whose condition is "Bandage", so
    -- picking it set a bump type nothing could match and left the companion wedged in
    -- BumpedState with no animation to end and therefore no exit. One entry in a list of
    -- forty, and it was a freeze button.
    "BandageHead", "BandageUpperBody", "BandageLowerBody",
    "BandageLeftArm", "BandageRightArm", "BandageLeftLeg", "BandageRightLeg",
}

local function buildExpertiseNames()
    local names = {}
    if Bandit and Bandit.Expertise then
        for name, id in pairs(Bandit.Expertise) do names[id] = name end
    end
    return names
end

local function itemName(fullType)
    local ok, name = pcall(function()
        local it = BanditCompatibility.InstanceItem(fullType)
        return it and it:getName()
    end)
    return (ok and name) or fullType
end

function BanditsNPCDialogWindow:isRecruited()
    -- "mine" = recruited by the local player (matches id OR username, so it survives a
    -- reconnect that changes the online id). See BanditsNPC.IsMine.
    return (BanditsNPC.IsMine and BanditsNPC.IsMine(self.brain)) or false
end

-- Recruited, but by a DIFFERENT player. Used to hide the recruit/dismiss controls so a
-- player can't claim or release someone else's companion in MP.
function BanditsNPCDialogWindow:isOwnedByOther()
    local brain = self.brain
    if not (brain and brain.recruited and brain.master) then return false end
    return not self:isRecruited()
end

-- ===== equipped panel (Items tab) =====
-- Cached item icon for a type string. Bandit clothing is NOT in getWornItems()
-- (Bandits clears it and paints the outfit on as visuals), so we read icons from
-- the item types stored in brain.clothing (bodyLocation -> itemType).
function BanditsNPCDialogWindow:iconTex(itemType)
    if not itemType then return nil end
    self._texCache = self._texCache or {}
    if self._texCache[itemType] == nil then
        local tex = false
        pcall(function()
            local it = BanditCompatibility.InstanceItem(itemType)
            if it then tex = it:getTex() end
        end)
        self._texCache[itemType] = tex
    end
    return self._texCache[itemType] or nil
end

-- The outfit doll. Since v0.77.2 it is the ONLY worn UI -- the "Worn layers" list that used
-- to sit beside it saying the same thing is gone -- so a FILLED SLOT IS CLICKABLE and takes
-- that layer off, which is what the brief asks and what freed the column beside it for the
-- two-panel transfer.
function BanditsNPCDialogWindow:drawEquippedPanel(ox, oy)
    local U = BanditsNPC.UI
    local hits = self._hits
    local zombie = self.zombie
    local clothing = (self.brain and self.brain.clothing) or {}   -- bodyLocation -> itemType
    local weapons = self.brain and self.brain.weapons
    -- id positions the box (EQUIP_LAYOUT), label is the translated caption
    local function box(id, label, tex, has, kind, tip, data)
        local pos = EQUIP_LAYOUT[id]
        if not pos then return end
        local bx, by = ox + U.S(pos.x), oy + U.S(pos.y)
        local bs = U.S(EQUIP_BOX)
        U.Fill(self, bx, by, bs, bs, { r = 0, g = 0, b = 0, a = 0.45 })
        -- Filled reads brighter than empty; an empty slot should recede rather than compete
        -- for attention with the twelve things that are actually on her.
        U.Border(self, bx, by, bs, bs, has and U.C.btnBorder or U.C.disBorder)
        if tex then
            pcall(function() self:drawTextureScaled(tex, bx + 2, by + 2, bs - 4, bs - 4, 1, 1, 1, 1) end)
        end
        local lc = has and U.C.label or U.C.disText
        self:drawTextCentre(label or id, bx + bs / 2, by + bs + 1, lc.r, lc.g, lc.b, 1, SMALL_FONT)
        if has and kind then
            local hit = U.Hit(hits, bx, by, bs, bs, kind, tip)
            if hit then hit.worn = data end
        end
    end

    local takeOffTip = BanditsNPC.T("UI_BN_Tooltip_TakeOffSlot", "Click to take this off.")

    -- clothing slots, from brain.clothing
    --
    -- Resolved in two passes so an unrecognised location still lands somewhere -- see the
    -- note over EQUIP_HINTS. Known locations claim their slot first, so a modded garment
    -- can only ever fill a box that would otherwise have been drawn empty; it can never
    -- displace something the lists do know about.
    local worn = {}
    for _, slot in ipairs(EQUIP_SLOTS) do
        for _, loc in ipairs(slot.locs) do
            if clothing[loc] then worn[slot.id] = { loc = loc, type = clothing[loc] }; break end
        end
    end
    for loc, itype in pairs(clothing) do
        if type(loc) == "string" and type(itype) == "string" and not LOC_SLOT[loc] then
            local id = slotForLoc(loc)
            if id and not worn[id] then worn[id] = { loc = loc, type = itype } end
        end
    end

    for _, slot in ipairs(EQUIP_SLOTS) do
        local w = worn[slot.id]
        local itype, iloc = w and w.type, w and w.loc
        -- THE BAG LIVES ON brain.bag, not brain.clothing. It had no slot of its own and
        -- would otherwise be the one thing on her you could not take off from here.
        local isBag = false
        if slot.id == "Back" and not itype then
            local bagType = self.brain and self.brain.bag and self.brain.bag.name
            if bagType then itype, iloc, isBag = bagType, "__bag", true end
        end
        -- The payload is exactly the row shape ctxTakeOff already takes, so clicking a slot
        -- reaches the same code path the old worn list did -- including RestoreItemLook,
        -- which is what stops a borrowed shirt coming back a different colour.
        box(slot.id, slot.label, itype and self:iconTex(itype) or nil, itype ~= nil,
            itype and "takeoff" or nil, takeOffTip,
            itype and { loc = iloc, type = itype, bag = isBag or nil } or nil)
    end

    -- weapons: prefer the live hand item, fall back to the assigned weapon type
    local function handBox(id, label, liveItem, fallbackType, kind, tip)
        local tex, has = nil, false
        if liveItem then pcall(function() tex = liveItem:getTex() end); has = true end
        if not tex and fallbackType and fallbackType ~= "Base.BareHands" then
            tex = self:iconTex(fallbackType); has = true
        end
        box(id, label, tex, has, kind, tip)
    end
    -- RHand: live item, else her long gun (v0.42 gun slots), else melee
    local rFallback = weapons and ((type(weapons.primary) == "table" and weapons.primary.name) or weapons.melee)
    local lFallback = weapons and (type(weapons.secondary) == "table" and weapons.secondary.name)
    handBox("RHand", BanditsNPC.T("UI_BN_Body_RHand", "RHand"), zombie:getPrimaryHandItem(), rFallback,
            "takeweapon", BanditsNPC.T("UI_BN_Tooltip_TakeWeapon", "Click to take their weapon"))
    handBox("LHand", BanditsNPC.T("UI_BN_Body_LHand", "LHand"), zombie:getSecondaryHandItem(), lFallback)
end

-- ===== build =====
-- ===========================================================================
-- FIX(TASK-020): EVERY COORDINATE IN THIS WINDOW, IN ONE PLACE, RE-RUNNABLE.
-- ===========================================================================
--
-- This is the geometry that used to sit inline in `createChildren`. **The arithmetic is
-- unchanged** -- it was already derived from `self.width` and `self.contentBottom`; what
-- it lacked was any way to run a second time. `createChildren` runs once, so a window
-- that could be dragged would have kept its opening rectangle forever.
--
-- IT IS SAFE TO CALL BEFORE THE CHILDREN EXIST. `place()` ignores nil, so the call at the
-- end of `createChildren` positions whatever has been built by then and the call from
-- `onResize` positions everything.
--
-- NOT HERE, DELIBERATELY:
--   * `self.prodQty` -- state. Resizing must not reset a part-entered craft quantity.
--   * the tab buttons -- `drawRail` already sets their rectangles every frame, because
--     the vitals block above them changes height with recruitment (:2802-2806).
--   * `self._hits` -- rebuilt every frame by `prerender`; a resize needs nothing.
function BanditsNPCDialogWindow:layout()
    local RAIL = sc(BanditsNPC.UI.M.rail)
    self.railW = RAIL
    local top = self:titleBarHeight() + sc(12)
    self.railNameY   = top
    self.railStatusY = top + FONT_MED + 2

    self.contentY = top
    self.rightX = RAIL + sc(14)
    -- FIX(TASK-020): RESERVE THE RESIZE STRIP, WHICH IS ONLY NOW A LIVE CONTROL.
    -- `ISCollapsableWindow` builds a full-width grab strip along the bottom edge and a
    -- corner grip, both `setVisible(self.resizable)` (refs/pz-lua/.../:34-49) -- so until
    -- this window became resizable they were invisible AND inert and nothing had to clear
    -- them. `resizeWidgetHeight()` is `(BUTTON_HGT/2)+2` (:302), about 12.5px, and the
    -- footer row used to end at exactly `height - sc(12)`. **That is INSIDE the strip:
    -- the bottom pixel row of Close, Dismiss and Talk would have started a window resize
    -- instead of pressing the button.** Subtracting it is the same idiom RosterPanel
    -- already uses for its list floor (BanditsNPCRosterPanel.lua:446).
    -- Floored: resizeWidgetHeight() is (BUTTON_HGT/2)+2 and is a HALF-PIXEL at most
    -- font sizes, and every container and button below is measured off this.
    self.footerY = math.floor(self.height - sc(12) - self:resizeWidgetHeight() - BTN_H)
    -- Clear of the footer container's own top edge, which sits 6px above footerY.
    self.contentBottom = self.footerY - sc(16)
    local rightX = self.rightX

    -- ===== FOOTER =====
    local fpad = sc(14)
    local bw = sc(96)
    place(self.closeBtn, fpad, self.footerY, bw, BTN_H)
    local ldX = fpad + bw + sc(6)
    local ldW = sc(120)
    place(self.dismissBtn, ldX, self.footerY, ldW, BTN_H)
    place(self.recruitBtn, ldX, self.footerY, ldW, BTN_H)
    local talkW = sc(96)
    local talkX = self.width - fpad - talkW
    place(self.talkOpenBtn, talkX, self.footerY, talkW, BTN_H)
    local cleanW = sc(96)
    local cleanX = talkX - sc(6) - cleanW
    place(self.cleanBtn, cleanX, self.footerY, cleanW, BTN_H)
    local sitW = sc(150)
    local sitX = cleanX - sc(6) - sitW
    place(self.helpUpBtn, sitX, self.footerY, sitW, BTN_H)
    place(self.treatBtn, sitX, self.footerY, sitW, BTN_H)

    -- ===== GEAR =====
    local gearTop = self.contentY + FONT_HGT * 2 + sc(10)   -- header + capacity bar
    local gearBtnY = self.contentBottom - BTN_H - sc(6)
    local listH = (gearBtnY - sc(8)) - gearTop
    local dollW = sc(142)
    local arrowW = sc(34)
    self.gearDollX = rightX
    local colX = rightX + dollW + sc(10)
    local colW = math.floor((self.width - sc(14) - colX - arrowW - sc(12)) / 2)
    self.invX = colX
    self.playerX = colX + colW + arrowW + sc(12)
    self.gearColW = colW
    self.gearListTop = gearTop
    self.gearListH = listH
    place(self.invList, colX, gearTop, colW, listH)
    place(self.playerList, self.playerX, gearTop, colW, listH)
    local aX = colX + colW + sc(6)
    local aY = gearTop + sc(40)
    place(self.giveArrowBtn, aX, aY, arrowW, BTN_H)
    place(self.takeArrowBtn, aX, aY + BTN_H + sc(4), arrowW, BTN_H)
    place(self.takeAllBtn, aX, aY + (BTN_H + sc(4)) * 2, arrowW, BTN_H)
    local halfW = math.floor((colW - sc(4)) / 2)
    place(self.wearBtn, colX, gearBtnY, halfW, BTN_H)
    place(self.equipWpnBtn, colX + halfW + sc(4), gearBtnY, halfW, BTN_H)
    place(self.makeWearBtn, self.playerX, gearBtnY, halfW, BTN_H)
    place(self.armHerBtn, self.playerX + halfW + sc(4), gearBtnY, halfW, BTN_H)

    -- ===== JOB =====
    local jobTop = self.contentY
    local bannerH = BTN_H + sc(10)
    self.jobBannerY = jobTop
    self.jobColW = math.floor(((self.width - PAD - rightX) - sc(10)) * 0.56)
    self.jobRightX = rightX + self.jobColW + sc(10)
    local wsBtnW = sc(76)
    local wsY = jobTop + sc(5)
    local wsX = self.width - sc(14) - sc(8) - wsBtnW * 2 - sc(4)
    place(self.buildWsBtn, wsX, wsY, wsBtnW, BTN_H)
    place(self.selectWsBtn, wsX + wsBtnW + sc(4), wsY, wsBtnW, BTN_H)
    place(self.clearWsBtn, wsX - wsBtnW - sc(4), wsY, wsBtnW, BTN_H)
    local recListY = jobTop + bannerH + FONT_HGT + sc(6)
    self.recListY = recListY
    local jobBottom = self.contentBottom - sc(20)
    self.jobBottom = jobBottom
    self.recipeListH = jobBottom - recListY
    place(self.recipeList, rightX, recListY, self.jobColW, self.recipeListH)
    local jrW = self.width - sc(14) - self.jobRightX
    self.jobRightW = jrW
    local inPad = sc(8)
    local prodY = jobBottom - inPad - BTN_H
    place(self.produceBtn, self.jobRightX, prodY, jrW, BTN_H)
    local qtyY = prodY - BTN_H - sc(6)
    self.qtyY = qtyY
    local stepW = sc(28)
    place(self.qtyMinusBtn, self.jobRightX, qtyY, stepW, BTN_H)
    place(self.qtyPlusBtn, self.jobRightX + stepW + sc(44), qtyY, stepW, BTN_H)
    self.qtyNumX = self.jobRightX + stepW + sc(22)   -- centre of the gap between - and +
    place(self.profCombo, self.jobRightX, jobBottom + sc(4), jrW, BTN_H)

    -- ===== ANIM =====
    local animBtnY = self.contentBottom - BTN_H - sc(6)
    place(self.animList, rightX, self.contentY, sc(250), (animBtnY - sc(8)) - self.contentY)
    place(self.playAnimBtn, rightX, animBtnY, sc(150), BTN_H)
    place(self.stopAnimBtn, rightX + sc(156), animBtnY, sc(90), BTN_H)

    self.laidOutW, self.laidOutH = self.width, self.height
end

function BanditsNPCDialogWindow:createChildren()
    ISCollapsableWindow.createChildren(self)

    -- ===================================================================
    -- RAIL / CONTENT / FOOTER (v0.77.0)
    -- ===================================================================
    --
    -- The old layout put the name, the vitals and a horizontal row of seven tabs across
    -- the TOP, and then let each tab own everything below. Two problems with that, and the
    -- second is a real bug rather than a matter of taste:
    --
    --   * seven tabs is more than the content justifies -- Skills held three lines of text
    --     and Quest held four;
    --   * the Items tab replaced the whole content area INCLUDING the tab row, so once a
    --     player opened it there was no way back to any other tab. That is the "some tabs
    --     have no way back" bug, and moving the tabs into a rail that no tab can draw over
    --     is the fix, not a workaround.
    --
    -- So: a fixed left RAIL owns the portrait, name, vitals and the tab list and is drawn
    -- by the window itself; CONTENT is the only thing a tab may touch; the FOOTER is
    -- always present. Content is anchored on self.rightX, which every control already used,
    -- so moving the whole content column right of the rail is this one assignment.
    -- Geometry is measured from the three CONTAINERS drawn in prerender, not from the window
    -- edge: content that starts at the window's padding sits on its own frame.
    -- FIX(TASK-020): GEOMETRY LIVES IN layout(), WHICH RUNS AGAIN ON EVERY RESIZE.
    -- `createChildren` runs ONCE, so anything computed here would be frozen at the
    -- opening size. Everything below constructs widgets at 0,0,0,0 and `layout()` at
    -- the end of this function gives them their rectangles -- ONE source for every
    -- coordinate, which is the only way the drawn content and the real ISButtons cannot
    -- drift apart at a size nobody tested.
    -- STATE, NOT GEOMETRY, AND IT MUST NOT MOVE INTO layout(). Resizing the window would
    -- otherwise reset the player's chosen production quantity back to 1 mid-craft.
    self.prodQty = 1

    -- ===== TABS: SEVEN BECOME FOUR =====
    -- Story+Quest -> Profile, Skills+Work -> Job, Items -> Gear. The internal section ids
    -- are UNCHANGED and each tab simply shows a SET of them (see self.shows) -- so all the
    -- existing per-section content and its handlers keep working exactly as they did, and
    -- this is a regrouping rather than a rewrite of everything underneath.
    --
    -- Anim is a developer tool and is only offered in debug, per the author's ask.
    self.tabButtons = {}
    local tabs = {
        {id="profile", label=BanditsNPC.T("UI_BN_Tab_Profile", "Profile")},
        {id="orders",  label=BanditsNPC.T("UI_BN_Tab_Orders", "Orders")},
        {id="job",     label=BanditsNPC.T("UI_BN_Tab_Job", "Job")},
        {id="gear",    label=BanditsNPC.T("UI_BN_Tab_Gear", "Gear")},
    }
    if BanditsNPC.UI.DebugOn() then
        tabs[#tabs + 1] = {id="anim", label=BanditsNPC.T("UI_BN_Tab_Anim", "Anim")}
    end
    self.tabY = 0            -- kept: older code reads it; the rail positions tabs itself
    -- FIX(TASK-020): CONSTRUCTED AT 0,0,0,0. **`drawRail`'s tab loop OWNS THIS RECTANGLE
    -- ENTIRELY -- all four of x, y, width and height -- and `layout()` deliberately does
    -- not touch it.** The tab row is the one part of this window whose position is genuine
    -- per-frame state rather than per-resize state: `y` accumulates down from the vitals
    -- block, which changes height with recruitment and with debug mode. Duplicating any
    -- of it here would be a second source for a coordinate that legitimately moves.
    --
    -- WHAT WAS HERE BEFORE, AND THE LESSON, because this cost a test cycle: the old code
    -- positioned these buttons from `self.railTabsY`, **a field read here and written
    -- nowhere in the file** -- so it was always 0 and every position it produced was
    -- overwritten on the first frame. It was dead. But it also passed `BTN_H` as the
    -- constructor's height, and THAT was live, because `drawRail` set only three of the
    -- four components. Deleting the dead arithmetic took the live height with it and the
    -- whole rail broke: zero-height buttons are unclickable, paint no chrome, and centre
    -- their label off a height of 0. **A line can be half dead. Check every value it
    -- produces, not just the one that looks wrong.**
    for _, t in ipairs(tabs) do
        local b = ISButton:new(0, 0, 0, 0, t.label, self, self.onTab)
        b.internal = t.id
        b:initialise(); b:instantiate(); self:addChild(b)
        self.tabButtons[t.id] = b
    end
    self.tabOrder = tabs

    -- ===== FOOTER buttons (always present) =====
    --
    -- LEFT: Close, then Dismiss beside it. RIGHT: Clean up, then Talk in the corner. That
    -- is the arrangement in the author's mock-ups, and it groups by consequence rather than
    -- by frequency -- the two that end things on one side, the two you use on the other.
    -- Dismiss used to sit in the far-right corner, one pixel-width from Talk.
    -- FIX(TASK-020): `closeBtn` WAS A LOCAL AND IS NOW `self.closeBtn`. layout() has to
    -- be able to reach every widget it positions, and a local dies with this function.
    self.closeBtn = ISButton:new(0, 0, 0, 0, BanditsNPC.T("UI_BN_Close", "Close"), self, self.onClose)
    self.closeBtn:initialise(); self.closeBtn:instantiate(); self:addChild(self.closeBtn)

    -- Dismiss and "Ask to join" are the same slot: a survivor is one or the other.
    self.dismissBtn = ISButton:new(0, 0, 0, 0, BanditsNPC.T("UI_BN_Dismiss", "Dismiss"), self, self.onDismiss)
    self.dismissBtn:initialise(); self.dismissBtn:instantiate(); self:addChild(self.dismissBtn)
    self.recruitBtn = ISButton:new(0, 0, 0, 0, BanditsNPC.T("UI_BN_AskToJoin", "Ask to join"), self, self.onRecruit)
    self.recruitBtn:initialise(); self.recruitBtn:instantiate(); self:addChild(self.recruitBtn)

    self.talkOpenBtn = ISButton:new(0, 0, 0, 0, BanditsNPC.T("UI_BN_Talk", "Talk"), self, self.onOpenTalk)
    self.talkOpenBtn:initialise(); self.talkOpenBtn:instantiate(); self:addChild(self.talkOpenBtn)

    -- "Clean up": wash off combat blood/dirt and pull out stuck weapon visuals
    self.cleanBtn = ISButton:new(0, 0, 0, 0, BanditsNPC.T("UI_BN_CleanUp", "Clean up"), self, self.onCleanUp)
    self.cleanBtn:initialise(); self.cleanBtn:instantiate(); self:addChild(self.cleanBtn)

    -- The two situational ones share the space left of Clean up. Neither is ever shown at
    -- the same time as the other: one needs her down, the other needs her up and hurt.
    self.helpUpBtn = ISButton:new(0, 0, 0, 0, BanditsNPC.T("UI_BN_HelpUp", "Help up"), self, self.onHelpUp)
    self.helpUpBtn:initialise(); self.helpUpBtn:instantiate(); self:addChild(self.helpUpBtn)
    self.treatBtn = ISButton:new(0, 0, 0, 0, BanditsNPC.T("UI_BN_TreatWounds", "Treat wounds"), self, self.onTreat)
    self.treatBtn:initialise(); self.treatBtn:instantiate(); self:addChild(self.treatBtn)

    -- ===== Orders tab (v0.77.1: DRAWN, not built from ISButtons) =====
    --
    -- Movement, Distance and Stance are "pick exactly one" and the brief asks for them to
    -- look like it -- one welded segmented strip per group. ISButton cannot do that: it
    -- always draws its own full border and its own background, so five of them in a row is
    -- five separate boxes with five gaps, which is most of why the old Orders tab looked
    -- ragged. So these three groups, the schedule block and the routine-spot rows are drawn
    -- by render() through the shared widgets and register their rectangles in self._hits.
    --
    -- That list is the SAME one the controller navigator reads (BanditsNPCJoypad's
    -- targets() checks panel._hits), so the mouse and the pad cannot end up disagreeing
    -- about what is pressable -- the rule the roster panel already follows. Their tooltips
    -- come along too, through U.Tip, rather than being quietly dropped with the buttons.
    -- `need` is the routine this spot serves, and only the six that HAVE one carry it: it is
    -- what the debug plus on the row calls Routine.Force with. Weapon and Meds are storage
    -- she visits on her own terms, not needs with a meter, so those rows get no plus.
    self.spotDefs = {
        {key="bed", label=BanditsNPC.T("UI_BN_Spot_Bed", "Bed"), need="fatigue"},
        {key="chair", label=BanditsNPC.T("UI_BN_Spot_Chair", "Chair"), need="boredom"},
        {key="tv", label=BanditsNPC.T("UI_BN_Spot_TV", "TV"), need="boredom"},
        {key="food", label=BanditsNPC.T("UI_BN_Spot_Food", "Food container"), need="hunger"},
        {key="reading", label=BanditsNPC.T("UI_BN_Spot_Reading", "Books cont."), need="boredom"},
        {key="weapon", label=BanditsNPC.T("UI_BN_Spot_Weapon", "Weapon cont.")},
        {key="healing", label=BanditsNPC.T("UI_BN_Spot_Healing", "Meds cont.")},
        {key="bathroom", label=BanditsNPC.T("UI_BN_Spot_Bathroom", "Bathroom"), need="hygiene"},
    }
    for _, d in ipairs(self.spotDefs) do
        if d.key == "bed" then
            d.tip = BanditsNPC.T("UI_BN_Tooltip_Bed", "Bed furniture only (red highlight = not a bed).\nPress R while placing to rotate the way they'll lie (blue tile).")
        elseif d.key == "chair" or d.key == "tv" then
            d.tip = BanditsNPC.T("UI_BN_Tooltip_Seat", "Pick the tile they'll sit on (any seat, modded too).\nPress R while placing to rotate the way they'll face (blue tile).")
        elseif d.key == "bathroom" then
            d.tip = BanditsNPC.T("UI_BN_Tooltip_Bathroom", "Needs a water source on the tile (sink, bath, toilet, barrel...).")
        else
            d.tip = BanditsNPC.T("UI_BN_Tooltip_Container", "Needs a container on the tile (crate, fridge, shelf...).")
        end
    end
    self._hits = {}

    -- (The two debug BUTTON ROWS are gone, v0.77.6, at the author's ask. "Debug - raise a
    -- need" was four buttons naming the needs a second time, and "Debug - send to spot now"
    -- was six naming the spots a second time in a different order -- neither could say which
    -- spots were even assigned, and together they took a third of the tab. Both are green
    -- pluses on the rows they act on now: beside each vitals bar in the rail, and beside
    -- each routine spot. Same two actions, no duplicated lists, and nothing at all when
    -- debug is off.)

    -- (The "Daily Schedule..." button is gone: the schedule is now a block ON this tab, per
    -- the brief. BanditsNPCScheduleWindow itself still exists and still works -- it is what
    -- the debug "Test now" preview lives in -- it is just no longer the only way in.)

    -- ===== Gear tab (v0.77.2: TWO-PANEL TRANSFER) =====
    --
    -- Left: the outfit doll -- and it is now the ONLY worn UI. The old middle column was a
    -- "Worn layers" list that duplicated the doll beside it, so half the tab was spent
    -- saying the same thing twice while giving her an item meant opening a whole separate
    -- window. Clicking a filled slot takes that layer off (the brief's rule), which frees
    -- the middle column for HER carried items and the right for YOURS, side by side with
    -- the transfer arrows between them.
    local dlg = self
    -- FIX(TASK-020): the gear geometry -- gearTop, gearBtnY, listH, dollW, colX, colW --
    -- moved to layout(). All six were already derived from self.width and
    -- self.contentBottom; they simply have to be derived AGAIN after a drag.

    -- HER carried items
    self.invList = ISScrollingListBox:new(0, 0, 0, 0)
    self.invList:initialise()
    self.invList.itemheight = BanditsNPC.ItemList.RowHeight(SMALL_FONT)
    self.invList.font = SMALL_FONT
    self.invList.drawBorder = true
    self.invList:setOnMouseDownFunction(self, self.onSelectInvItem)
    self:addChild(self.invList)
    -- shared renderer: icon + NESTING GUIDES + weight. The rows used to be a flat
    -- getAllEvalRecurse dump, so a knife lying in her hands' reach and a knife three bags
    -- deep looked identical.
    self.invList.doDrawItem = function(lb, yy, entry, alt)
        return BanditsNPC.ItemList.DrawRow(lb, yy, entry, alt,
            { font = SMALL_FONT, selected = dlg.invSelected })
    end
    self.invList.onMouseDouble = function(lb) dlg:onTakeItem() end
    self.invList.onRightMouseDown = function(lb, x, y)
        pcall(function()
            local row = lb.rowAt and lb:rowAt(x, y)
            local e = (row and row >= 1 and lb.items) and lb.items[row] or nil
            -- ignore a group/header row: it has no item, and blanking a good selection
            -- because the click landed on a bag label would be a trap
            if e and e.item then
                dlg.invSelected = e.item
                dlg:showInvContext(dlg.invSelected, lb:getAbsoluteX() + x, lb:getAbsoluteY() + y)
            end
        end)
    end

    -- YOUR carried items -- the half of the transfer that used to be a separate window
    self.playerList = ISScrollingListBox:new(0, 0, 0, 0)
    self.playerList:initialise()
    self.playerList.itemheight = BanditsNPC.ItemList.RowHeight(SMALL_FONT)
    self.playerList.font = SMALL_FONT
    self.playerList.drawBorder = true
    self.playerList:setOnMouseDownFunction(self, self.onSelectPlayerItem)
    self:addChild(self.playerList)
    self.playerList.doDrawItem = function(lb, yy, entry, alt)
        return BanditsNPC.ItemList.DrawRow(lb, yy, entry, alt,
            { font = SMALL_FONT, selected = dlg.playerSelected })
    end
    self.playerList.onMouseDouble = function(lb) dlg:onGiveSelected() end
    self.playerList.onRightMouseDown = function(lb, x, y)
        pcall(function()
            local row = lb.rowAt and lb:rowAt(x, y)
            local e = (row and row >= 1 and lb.items) and lb.items[row] or nil
            if e and e.item then
                dlg.playerSelected = e.item
                dlg:showPlayerContext(dlg.playerSelected, lb:getAbsoluteX() + x, lb:getAbsoluteY() + y)
            end
        end)
    end

    -- transfer gutter. Left arrow moves toward HER list, right arrow toward yours -- the
    -- arrow points at where the item lands.
    self.giveArrowBtn = ISButton:new(0, 0, 0, 0, "<-", self, self.onGiveSelected)
    self.giveArrowBtn:initialise(); self.giveArrowBtn:instantiate(); self:addChild(self.giveArrowBtn)
    self.giveArrowBtn:setTooltip(BanditsNPC.T("UI_BN_Tooltip_GiveArrow", "Give the selected item to them"))
    self.takeArrowBtn = ISButton:new(0, 0, 0, 0, "->", self, self.onTakeItem)
    self.takeArrowBtn:initialise(); self.takeArrowBtn:instantiate(); self:addChild(self.takeArrowBtn)
    self.takeArrowBtn:setTooltip(BanditsNPC.T("UI_BN_Tooltip_TakeArrow", "Take the selected item from them"))
    self.takeAllBtn = ISButton:new(0, 0, 0, 0, BanditsNPC.T("UI_BN_All", "All"), self, self.onTakeAll)
    self.takeAllBtn:initialise(); self.takeAllBtn:instantiate(); self:addChild(self.takeAllBtn)
    self.takeAllBtn:setTooltip(BanditsNPC.T("UI_BN_Tooltip_TakeAll", "Take everything they are carrying"))

    -- action rows under each list
    self.wearBtn = ISButton:new(0, 0, 0, 0, BanditsNPC.T("UI_BN_Wear", "Wear"), self, self.onWearClothing)
    self.wearBtn:initialise(); self.wearBtn:instantiate(); self:addChild(self.wearBtn)
    self.equipWpnBtn = ISButton:new(0, 0, 0, 0, BanditsNPC.T("UI_BN_Equip", "Equip"), self, self.onEquipWeapon)
    self.equipWpnBtn:initialise(); self.equipWpnBtn:instantiate(); self:addChild(self.equipWpnBtn)
    -- Give-and-use in one press. Two steps for something the player always wants as one:
    -- handing over a jacket and then finding the Wear button is not a decision anyone makes
    -- separately.
    self.makeWearBtn = ISButton:new(0, 0, 0, 0, BanditsNPC.T("UI_BN_MakeWear", "Make wear"), self, self.onMakeWear)
    self.makeWearBtn:initialise(); self.makeWearBtn:instantiate(); self:addChild(self.makeWearBtn)
    self.armHerBtn = ISButton:new(0, 0, 0, 0, BanditsNPC.T("UI_BN_ArmThem", "Arm them"), self, self.onArmThem)
    self.armHerBtn:initialise(); self.armHerBtn:instantiate(); self:addChild(self.armHerBtn)

    -- ===== Quest tab =====
    -- FONT_HGT + 8, not + 4. The "Quest" heading is drawn at contentY and is FONT_HGT
    -- tall, so at +4 the button's top edge cut into the text under it (author, 4 Aug:
    -- "the Quest text overlaps with the label above"). renderQuest below starts the
    -- description from this button's BOTTOM for the same reason.
    -- (No "Complete quest" button: Turn in is drawn INSIDE the quest card on the Profile
    -- tab and is DISABLED until the progress bar is full. It used to sit above the quest
    -- text and be live from the moment the quest appeared, which is the wrong way round.)

    -- ===== Job tab (v0.77.2) =====
    --
    -- Two columns under a full-width station banner: the recipe list and its stepper on the
    -- left, the selected recipe's REQUIREMENTS on the right. The banner replaces the
    -- detached right-hand column of three workstation buttons -- Build and Select now sit
    -- inside the warning that explains why you would press them.
    -- FIX(TASK-020): jobColW, jobRightX, wsX, recListY, jobBottom, recipeListH, jrW,
    -- prodY and qtyY all moved to layout(). Same arithmetic, run again after a drag.
    self.buildWsBtn = ISButton:new(0, 0, 0, 0, BanditsNPC.T("UI_BN_Build", "Build"), self, self.onBuildWorkstation)
    self.buildWsBtn:initialise(); self.buildWsBtn:instantiate(); self:addChild(self.buildWsBtn)
    self.selectWsBtn = ISButton:new(0, 0, 0, 0, BanditsNPC.T("UI_BN_Select", "Select"), self, self.onSelectWorkstation)
    self.selectWsBtn:initialise(); self.selectWsBtn:instantiate(); self:addChild(self.selectWsBtn)
    -- Clear only appears once a station is actually assigned, so it never sits there as a
    -- third button with nothing to clear.
    self.clearWsBtn = ISButton:new(0, 0, 0, 0, BanditsNPC.T("UI_BN_Clear2", "Clear"), self, self.onClearWorkstation)
    self.clearWsBtn:initialise(); self.clearWsBtn:instantiate(); self:addChild(self.clearWsBtn)

    -- The recipe list fills the LEFT column to the bottom. Everything about the selected
    -- recipe -- requirements, output, the stepper and Produce -- lives in the right-hand
    -- container, which is how the author's mock-up reads: pick on the left, decide on the
    -- right. v0.77.3 put the stepper and Produce under the list instead, where they had to
    -- share the column's height with it and pushed the status line past the footer.
    -- BOTH COLUMNS START AND END ON THE SAME LINES (author ask). The recipe list and the
    -- requirements container are one row of two panels, so their tops and floors match; the
    -- section captions sit above them both.
    self.recipeList = ISScrollingListBox:new(0, 0, 0, 0)
    self.recipeList:initialise()
    -- Tall enough for an item ICON, like every other list in the mod: a bare-text recipe
    -- list was the one place the player could not tell at a glance what came out.
    self.recipeList.itemheight = BanditsNPC.ItemList.RowHeight(SMALL_FONT)
    self.recipeList.font = SMALL_FONT
    self.recipeList.drawBorder = true
    self.recipeList:setOnMouseDownFunction(self, self.onSelectRecipe)
    self:addChild(self.recipeList)
    -- Gated recipes are SHOWN and greyed rather than filtered out: "tiers currently mean
    -- nothing to the player" was in the brief, and a recipe you cannot see teaches nothing
    -- about why upgrading the station is worth it.
    self.recipeList.doDrawItem = function(lb, yy, entry, alt)
        return dlg:drawRecipeRow(lb, yy, entry, alt)
    end

    -- INSIDE the right container, on its floor: Produce along the bottom with the stepper
    -- directly above it, which is how the author's mock reads. v0.77.6 hung both BELOW the
    -- container instead, so the panel looked like it ended and then two loose controls
    -- followed it.
    self.produceBtn = ISButton:new(0, 0, 0, 0, BanditsNPC.T("UI_BN_Produce", "Produce"), self, self.onProduceInline)
    self.produceBtn:initialise(); self.produceBtn:instantiate(); self:addChild(self.produceBtn)

    self.qtyMinusBtn = ISButton:new(0, 0, 0, 0, "-", self, self.onProdQtyMinus)
    self.qtyMinusBtn:initialise(); self.qtyMinusBtn:instantiate(); self:addChild(self.qtyMinusBtn)
    self.qtyPlusBtn = ISButton:new(0, 0, 0, 0, "+", self, self.onProdQtyPlus)
    self.qtyPlusBtn:initialise(); self.qtyPlusBtn:instantiate(); self:addChild(self.qtyPlusBtn)

    -- [DEBUG] profession changer (debug/admin only) so every trade can be tested without
    -- spawning a pile of NPCs to roll them all. UNDER the right container (author ask) --
    -- above it, it pushed the requirements down and read as part of them.
    self.profCombo = ISComboBox:new(0, 0, 0, 0, self, self.onChangeProfession)
    self.profCombo:initialise(); self.profCombo:instantiate()
    if BanditsNPC.ProfessionNames then
        for _, nm in ipairs(BanditsNPC.ProfessionNames) do self.profCombo:addOption(nm) end
    end
    self:addChild(self.profCombo)

    -- ===== Anim tab =====
    -- Play and Stop sit ABOVE the footer, inside the content area. They were placed at
    -- footerY, which is the footer's own row -- they drew straight over Dismiss.
    self.animList = ISScrollingListBox:new(0, 0, 0, 0)
    self.animList:initialise()
    self.animList.itemheight = FONT_HGT + 6
    self.animList.font = SMALL_FONT
    self.animList.drawBorder = true
    self.animList:setOnMouseDownFunction(self, self.onSelectAnim)
    self:addChild(self.animList)
    self.playAnimBtn = ISButton:new(0, 0, 0, 0, BanditsNPC.T("UI_BN_PlayAnim", "Play (pick tile)"), self, self.onPlayAnim)
    self.playAnimBtn:initialise(); self.playAnimBtn:instantiate(); self:addChild(self.playAnimBtn)
    self.stopAnimBtn = ISButton:new(0, 0, 0, 0, BanditsNPC.T("UI_BN_StopAnim", "Stop"), self, self.onStopAnim)
    self.stopAnimBtn:initialise(); self.stopAnimBtn:instantiate(); self:addChild(self.stopAnimBtn)

    -- FIX(TASK-020): every widget now exists, so give them all their rectangles. Runs
    -- here rather than per-section so there is exactly one ordering to reason about.
    self:layout()

    -- ONE LOOK FOR THE WHOLE WINDOW (v0.77.2). Half this panel is now drawn through the
    -- shared widgets and half is still real ISButtons -- the ones that need a tooltip, a
    -- combo box beside them, or keyboard focus. Left alone the two halves looked like two
    -- different mods, so every remaining button is repainted with the same palette. Only
    -- ISButton's own colour fields are touched; behaviour is untouched.
    local U0 = BanditsNPC.UI
    for _, c in ipairs(self:getChildrenInOrder() or {}) do
        if c.onclick ~= nil then U0.StyleButton(c) end
    end
    U0.StyleButton(self.talkOpenBtn, "emph")
    U0.StyleButton(self.dismissBtn, "danger")
    U0.StyleButton(self.produceBtn, "emph")
    -- Help up is the one button that only exists because something has gone wrong; the
    -- loop above just repainted its old red flat, and it should keep reading as an alert.
    -- Treat wounds and Clean up lose their green and blue with no argument -- vanilla PZ
    -- has neither, which is the brief's whole point about chrome.
    U0.StyleButton(self.helpUpBtn, "danger")
    -- The tab rail draws its own active highlight behind these, so the buttons themselves
    -- must not paint a competing box over it.
    for _, b in pairs(self.tabButtons or {}) do
        b.backgroundColor = { r = 0, g = 0, b = 0, a = 0 }
        b.borderColor = { r = 0, g = 0, b = 0, a = 0 }
    end

    self.expertiseNames = buildExpertiseNames()
    if BanditsNPC.Backstory then BanditsNPC.Backstory.Ensure(self.zombie) end
    if BanditsNPC.Recruit then BanditsNPC.Recruit.Ensure(self.zombie) end
    if BanditsNPC.Talk then BanditsNPC.Talk.EnsureMet(self.zombie) end

    -- "profile", not "story". These are TAB ids; "story" is a CONTENT GROUP id, and passing
    -- one where the other belongs left self.section unmatched by TAB_SHOWS -- so shows()
    -- answered false for every group, the content area opened blank and no tab was lit.
    self:selectSection("profile")
end

-- WHICH CONTENT GROUPS A TAB SHOWS (v0.77.0).
--
-- The four tabs are views over the seven content groups that already existed. Keeping the
-- old group ids means every `self:shows("prod")` test below still asks the same question it
-- always did -- it just asks it through here, so one table decides the whole consolidation
-- and there is no second place for the two to disagree.
local TAB_SHOWS = {
    profile = { story = true, quest = true },
    orders  = { orders = true },
    job     = { skills = true, prod = true },
    gear    = { trade = true },
    anim    = { anim = true },
}

function BanditsNPCDialogWindow:shows(group)
    local set = TAB_SHOWS[self.section or "profile"]
    return (set and set[group]) and true or false
end

function BanditsNPCDialogWindow:selectSection(id)
    self.section = id
    self:refresh()
end

-- ===== throttled memo for the two expensive per-frame lookups (29 Jul audit, Q3) =====
-- render() is called EVERY FRAME, and two of its lookups are heavy:
--   * Production.FindStation walks a 17x17 box of grid squares and every object on each
--     (~289 getGridSquare calls per call -- roughly 17k a second at 60fps) and it ran
--     unconditionally the whole time the Work tab was open, whether or not anything moved;
--   * Quests.Progress does TWO full getAllEvalRecurse passes, recursing every container
--     in the player's inventory AND the companion's bag, every frame the Quest tab is open.
-- Both are now memoised for MEMO_MS -- the same trick the roster panel already uses
-- (_nextCollectMs). Correctness comes from refresh() dropping the memo: every player
-- ACTION funnels through refresh(), so anything the player actually does still shows up
-- immediately. The cache only ever absorbs repeat draws of a frame nothing changed in.
-- A nil result is cached deliberately -- "no station nearby" is the case that costs the
-- full scan, so it is the one most worth not repeating.
local MEMO_MS = 250

function BanditsNPCDialogWindow:memo(key, fn)
    local now = getTimestampMs()
    self._memo = self._memo or {}
    local e = self._memo[key]
    if e and now < e.expires then return e.a, e.b end
    local a, b = fn()
    self._memo[key] = { expires = now + MEMO_MS, a = a, b = b }
    return a, b
end

function BanditsNPCDialogWindow:refresh()
    self._memo = nil   -- a player action just happened: never draw stale derived data
    self.brain = BanditBrain.Get(self.zombie)
    if not self.brain then self:onClose(); return end

    local recruited = self:isRecruited()
    local ownedByOther = self:isOwnedByOther()
    -- footer
    -- Talk works on your own companion and on wild/recruitable NPCs, but NOT on another
    -- player's companion (talking changes its affinity/story -- that's the owner's to do).
    self.talkOpenBtn:setVisible(not ownedByOther)
    -- only offer "Ask to join" for a companion nobody owns; never for another player's
    self.recruitBtn:setVisible(not recruited and not ownedByOther)
    -- Dismissing releases a companion for good, so it is the owner's call -- it sat
    -- next to two buttons that already tested ownedByOther and did not test it itself
    -- (audit Cluster A). Interact.Dismiss refuses regardless now; this stops the button
    -- offering an action that would silently do nothing.
    self.dismissBtn:setVisible(recruited and not ownedByOther)
    self.helpUpBtn:setVisible((recruited and self.brain and self.brain.downed) and true or false)
    -- "Treat wounds": an up (not downed) companion who is hurt below full health
    local hurt = false
    pcall(function()
        if recruited and self.brain and not self.brain.downed then
            local maxh = (self.brain.health and self.brain.health > 0) and self.brain.health or 2.0
            hurt = self.zombie:getHealth() < maxh - 0.05
        end
    end)
    self.treatBtn:setVisible(hurt and true or false)
    self.cleanBtn:setVisible((recruited and not ownedByOther and self.brain and not self.brain.downed) and true or false)

    -- Orders. The movement/distance/stance strips, the schedule block and the routine-spot
    -- rows are drawn in render() and have no widgets to show or hide; only the two debug
    -- rows are still real buttons.
    local showOrders = (self:shows("orders") and recruited)
    -- The two debug rows follow the Orders tab AND the debug gate: a normal playthrough
    -- should not be offered "+Hunger" or "send her to the bathroom now" (author ask).
    -- The schedule block is drawn from brain.schedule and render must not create it, so
    -- the one call that can is made here: once per refresh, on the tab that needs it.
    if showOrders and BanditsNPC.Schedule then BanditsNPC.Schedule.Ensure(self.brain) end
    if recruited and BanditsNPC.Needs then
        BanditsNPC.Needs.Update(self.zombie)
        self.brain = BanditBrain.Get(self.zombie)
    end

    -- Gear
    local showItems = (self:shows("trade") and recruited)
    self.invList:setVisible(showItems)
    self.playerList:setVisible(showItems)
    self.giveArrowBtn:setVisible(showItems)
    self.takeArrowBtn:setVisible(showItems)
    self.takeAllBtn:setVisible(showItems)
    self.equipWpnBtn:setVisible(showItems)
    self.wearBtn:setVisible(showItems)
    self.makeWearBtn:setVisible(showItems)
    self.armHerBtn:setVisible(showItems)
    if showItems then
        -- nested tree, not a flat recursive dump: her own bags now show as group rows with
        -- their contents indented under them. Nothing of hers is "equipped" in the engine
        -- sense (a bandit's outfit lives in brain.clothing and is drawn on the doll), so
        -- no equip filter here.
        BanditsNPC.ItemList.Fill(self.invList,
            BanditsNPC.ItemList.Build(self.zombie:getInventory(), {}))
        -- YOUR side. hideEquipped is deliberately OFF and the player is passed instead, so
        -- what you are wearing still LISTS but reads "worn" -- handing over the coat off
        -- your back is a legitimate thing to do and hiding it would just look like a bug.
        local player = getSpecificPlayer(0)
        if player then
            BanditsNPC.ItemList.Fill(self.playerList,
                BanditsNPC.ItemList.Build(player:getInventory(), { player = player }))
        end
    end

    if recruited and BanditsNPC.Quests then
        BanditsNPC.Quests.Ensure(self.zombie)
        if BanditsNPC.Production then BanditsNPC.Production.Update(self.zombie) end
        self.brain = BanditBrain.Get(self.zombie)
    end
    -- (the quest card and its Turn in are drawn on the Profile tab; nothing to show here)

    -- Work
    local stationType = nil
    if recruited and BanditsNPC.Production then stationType = BanditsNPC.Production.GetStationType(self.brain) end
    self.prodStationType = stationType
    -- Build button shows for ANY trade that maps to a station (incl. the new trades
    -- that don't have recipes yet); select/clear stay tied to producible stations.
    local buildType = nil
    if recruited and BanditsNPC.Stations then buildType = BanditsNPC.Stations.GetType(self.brain) end
    self.buildWsType = buildType
    self.buildWsBtn:setVisible((self:shows("prod") and buildType ~= nil) and true or false)
    -- [DEBUG] profession dropdown: visible to debug/admin on the Work tab; reflects
    -- the companion's current trade and changes it on selection.
    local debugTools = (isDebugEnabled and isDebugEnabled()) or (isAdmin and isAdmin())
    local showProf = (debugTools and self:shows("prod") and recruited) and true or false
    self.profCombo:setVisible(showProf)
    if showProf and self.brain then
        local curId
        if self.brain.exp then
            for _, id in ipairs(self.brain.exp) do
                if BanditsNPC.ProfessionSet and BanditsNPC.ProfessionSet[id] then curId = id; break end
            end
        end
        if curId and Bandit and Bandit.Expertise and BanditsNPC.ProfessionNames then
            for i, nm in ipairs(BanditsNPC.ProfessionNames) do
                if Bandit.Expertise[nm] == curId then self.profCombo.selected = i; break end
            end
        end
    end
    local showProd = (self:shows("prod") and stationType ~= nil) and true or false
    self.selectWsBtn:setVisible(showProd)
    self.clearWsBtn:setVisible(showProd and self.brain.workstation ~= nil and true or false)
    self.recipeList:setVisible(showProd)
    self.qtyMinusBtn:setVisible(showProd)
    self.qtyPlusBtn:setVisible(showProd)
    self.produceBtn:setVisible(showProd)
    if showProd then
        self.recipeList:clear()
        local nearbyTier = 0
        local stObj = self:memo("station", function()
            return BanditsNPC.Production.FindStation(self.zombie, stationType)
        end)
        if stObj then pcall(function() nearbyTier = stObj:getModData().npcWorkstationTier or 1 end) end
        self.prodNearbyTier = nearbyTier
        -- EVERY recipe, gated ones included. They used to be filtered out entirely, so a
        -- tier-3 station looked identical to a tier-1 one except that more rows appeared
        -- from nowhere -- there was no way to learn that upgrading unlocks anything. They
        -- are listed greyed with their tier requirement instead (see drawRecipeRow).
        for _, r in ipairs(BanditsNPC.Production.Recipes[stationType] or {}) do
            local mt = r.minTier or 1
            local locked = (nearbyTier > 0 and mt > nearbyTier)
            local e = self.recipeList:addItem(BanditsNPC.Production.RecipeName(r.name), r)
            e.bnLocked = locked
            e.bnTier = mt
        end
        -- A selection left over from another station (or a recipe that just got gated) must
        -- not keep driving the requirements panel and the Produce button.
        if self.prodSelected then
            local stillThere = false
            for _, it in ipairs(self.recipeList.items or {}) do
                if it.item == self.prodSelected and not it.bnLocked then stillThere = true; break end
            end
            if not stillThere then self.prodSelected = nil end
        end
        -- Clamp the quantity to what the ingredients actually allow, so the stepper cannot
        -- be left showing a number that Produce will refuse.
        if self.prodSelected then
            local maxq = BanditsNPC.Production.MaxQty(getSpecificPlayer(0), self.prodSelected)
            self.prodMaxQty = maxq
            if maxq > 0 and (self.prodQty or 1) > maxq then self.prodQty = maxq end
        else
            self.prodMaxQty = 0
        end
    end

    -- Anim (owner-only: a non-owner must not be able to puppet someone else's companion)
    local showAnim = (self:shows("anim") and recruited)
    self.animList:setVisible(showAnim)
    self.playAnimBtn:setVisible(showAnim)
    self.stopAnimBtn:setVisible(showAnim)
    if showAnim and not self.animPopulated then
        for _, a in ipairs(ANIMS) do self.animList:addItem(a, a) end
        self.animPopulated = true
    end
end

-- ===== handlers =====
function BanditsNPCDialogWindow:onTab(button) self:selectSection(button.internal) end

function BanditsNPCDialogWindow:onOpenTalk()
    local z = self.zombie
    self:onClose()
    BanditsNPCTalkWindow.OpenFor(z)
end

function BanditsNPCDialogWindow:onRecruit()
    local zombie = self.zombie
    local req = BanditsNPC.Recruit.Ensure(zombie)
    if not req or req.type == "none" then
        self.recruitMsg = BanditsNPC.Interact.RecruitFree(zombie)
        self:refresh(); return
    end
    local def = req.def or {}
    local dlg = self
    BanditsNPCGiveWindow.Open({
        title = BanditsNPC.TF("UI_BN_GiveTo", "Give to %1", ((self.brain and self.brain.fullname) or BanditsNPC.T("UI_BN_Survivor", "Survivor"))),
        instruction = def.prompt or BanditsNPC.T("UI_BN_ChooseItemGive", "Choose an item to give."),
        filter = BanditsNPC.Interact.REQ_PREDICATE[req.type],
        -- The recruitment gift DOES offer gear off your own body, unlike the open-ended
        -- Items-tab give. The survivor asked for one specific thing and the filter has
        -- already narrowed the list to it; refusing the axe in your hands or the water
        -- bottle on your belt would just block recruitment with no way forward. Safe since
        -- FulfillWith hands over via Interact.TakeFromPlayer, which unequips properly, and
        -- equipped rows are labelled so nothing goes unknowingly.
        hideEquipped = false,
        confirmLabel = BanditsNPC.T("UI_BN_GiveRecruit", "Give & Recruit"),
        onConfirm = function(item)
            dlg.recruitMsg = BanditsNPC.Interact.FulfillWith(dlg.zombie, item)
            dlg:refresh()
        end,
    })
end
function BanditsNPCDialogWindow:onDismiss()
    -- Confirmation dialog: testers dismissed companions expecting that to "start their
    -- routines" and lost them -- a dismissed NPC ignores orders/spots, wanders off, and
    -- leaves the restore roster. Routines run automatically while she stays recruited.
    local name = (self.brain and self.brain.fullname) or BanditsNPC.T("UI_BN_ThisCompanion", "this companion")
    local w, h = 380, 170
    local x = getCore():getScreenWidth() / 2 - w / 2
    local y = getCore():getScreenHeight() / 2 - h / 2
    local text = BanditsNPC.TF("UI_BN_DismissConfirm", "Dismiss %1?\n\nThey will stop taking orders, stop using their assigned spots,\nwander off on their own and no longer be protected.\n\n(Routines and schedules run AUTOMATICALLY while they stay\nrecruited - you never need to dismiss them for that.)", name)
    local modal = ISModalDialog:new(x, y, w, h, text, true, self, BanditsNPCDialogWindow.onConfirmDismiss)
    modal:initialise()
    modal:addToUIManager()
end

function BanditsNPCDialogWindow:onConfirmDismiss(button)
    if button.internal == "YES" then
        BanditsNPC.Interact.Dismiss(self.zombie)
        self:refresh()
    end
end

function BanditsNPCDialogWindow:onHelpUp()
    if BanditsNPC.Combat and BanditsNPC.Combat.HelpUp then BanditsNPC.Combat.HelpUp(self.zombie) end
    self:refresh()
end

function BanditsNPCDialogWindow:onTreat()
    if BanditsNPC.Interact and BanditsNPC.Interact.Treat then
        self.recruitMsg = BanditsNPC.Interact.Treat(self.zombie)
    end
    self:refresh()
end

function BanditsNPCDialogWindow:onCleanUp()
    if BanditsNPC.Interact and BanditsNPC.Interact.CleanUp then
        self.recruitMsg = BanditsNPC.Interact.CleanUp(self.zombie)
    end
    self:refresh()
end

function BanditsNPCDialogWindow:onOpenSchedule() BanditsNPCScheduleWindow.OpenFor(self.zombie) end

function BanditsNPCDialogWindow:onSelectAnim(item) self.selectedAnim = item end
function BanditsNPCDialogWindow:onPlayAnim()
    if not self.selectedAnim then return end
    local z, a = self.zombie, self.selectedAnim
    self:onClose()
    BanditsNPC.Interact.PlayAnimAt(z, a)
end
function BanditsNPCDialogWindow:onStopAnim()
    if BanditsNPC.Interact.StopAnim then BanditsNPC.Interact.StopAnim(self.zombie) end
    self.animMsg = BanditsNPC.T("UI_BN_Msg_Stopped", "Stopped.")
    self:refresh()
end

function BanditsNPCDialogWindow:onOrder(button)
    if BanditsNPC.Schedule then BanditsNPC.Schedule.Disable(self.zombie) end
    local prog = button.internal
    if prog == "NPCStay" then
        self:onClose(); BanditsNPC.Interact.OrderStay(self.zombie)
    elseif prog == "NPCGuard" then
        self:onClose(); BanditsNPC.Interact.OrderGuard(self.zombie)
    elseif prog == "__relax" then
        self:onClose(); BanditsNPC.Interact.OrderRelax(self.zombie)
    elseif prog == "__hide" then
        BanditsNPC.Interact.OrderHide(self.zombie); self:refresh()
    else
        BanditsNPC.Interact.SetOrder(self.zombie, prog); self:refresh()
    end
end

function BanditsNPCDialogWindow:onStance(button)
    BanditsNPC.Interact.SetStance(self.zombie, button.internal); self:refresh()
end

function BanditsNPCDialogWindow:onToggleFirearms()
    if BanditsNPC.Interact.SetFirearmsEnabled then
        local noGuns = self.brain and self.brain.npcNoGuns
        BanditsNPC.Interact.SetFirearmsEnabled(self.zombie, noGuns and true or false)
        self.brain = BanditBrain.Get(self.zombie)
    end
    self:refresh()
end

function BanditsNPCDialogWindow:onSetSpot(button)
    local key = button.internal
    -- if already assigned, this button clears it; otherwise pick a tile
    local spots = self.brain and self.brain.spots
    if spots and spots[key] then
        BanditsNPC.Interact.ClearSpot(self.zombie, key)
        self:refresh()
        return
    end
    local label = key
    for _, d in ipairs(self.spotDefs) do if d.key == key then label = d.label; break end end
    self:onClose()
    BanditsNPC.Interact.BeginSetSpot(self.zombie, key, label)
end

function BanditsNPCDialogWindow:onRaiseNeed(button)
    if BanditsNPC.Needs then BanditsNPC.Needs.Add(self.zombie, button.internal, 35) end
    self:refresh()
end

function BanditsNPCDialogWindow:onRoutineCmd(button)
    local d = button.internal
    local ok = BanditsNPC.Routine and BanditsNPC.Routine.Force(self.zombie, d.need, d.spot)
    self.ordersMsg = ok and BanditsNPC.TF("UI_BN_Msg_OnIt", "On it: %1.", d.l)
        or BanditsNPC.TF("UI_BN_Msg_AssignSpot", "Assign the '%1' spot first.", d.spot)
    self:refresh()
end

function BanditsNPCDialogWindow:onCompleteQuest()
    local ok, msg = BanditsNPC.Quests.Complete(self.zombie)
    self.questMsg = msg; self:refresh()
end

-- ===========================================================================
-- DRAWN-CONTROL DISPATCH (v0.77.1)
-- ===========================================================================
--
-- The Profile and Orders tabs are drawn rather than assembled from ISButtons (see the note
-- in createChildren), so their clicks arrive here. One list, self._hits, rebuilt every
-- frame by render and read by BOTH the mouse and the controller navigator -- the two cannot
-- drift apart into disagreeing about what is pressable.
--
-- A consumed click must NOT fall through to ISCollapsableWindow.onMouseDown, which starts
-- dragging the window from wherever you pressed; pressing Guard would otherwise drag the
-- panel across the screen.
function BanditsNPCDialogWindow:hitAt(x, y)
    for _, h in ipairs(self._hits or {}) do
        if x >= h.x and x < h.x + h.w and y >= h.y and y < h.y + h.h then return h end
    end
    return nil
end

-- Press and release, not press alone. ISCollapsableWindow makes the whole window draggable
-- from wherever you press, so firing on the DOWN edge means dragging the panel by its
-- Movement strip also issues the order you started the drag on. Consuming the press over a
-- control both suppresses the drag there and lets the release decide, which is what every
-- other button in the game does.
function BanditsNPCDialogWindow:onMouseDown(x, y)
    self._pressed = nil
    if self:getIsVisible() and y > self:titleBarHeight() then
        local h = self:hitAt(x, y)
        if h then
            -- EVERY hit fires on the RELEASE, including the two that open a menu.
            --
            -- v0.77.6 opened those two on the PRESS, reasoning that a dropdown should feel
            -- like a dropdown -- and it made them dead: the schedule activity buttons and
            -- the doll's take-off slots did nothing at all. ISContextMenu hides itself from
            -- onMouseDownOutside and from NOTHING ELSE (it has no onMouseUpOutside and its
            -- onMouseDown is an empty function), so a menu built during a mouse-DOWN is
            -- created into the one event that dismisses it, and vanishes the same frame.
            -- Building it on the release is provably safe for exactly the same reason.
            self._pressed = h
            self:bringToTop()
            return true
        end
    end
    return ISCollapsableWindow.onMouseDown(self, x, y)
end

function BanditsNPCDialogWindow:onMouseUp(x, y)
    local p = self._pressed
    self._pressed = nil
    if p then
        -- Only if the release is still inside the rectangle the press started in: sliding
        -- off a control has always been the way to change your mind about pressing it.
        if x >= p.x and x < p.x + p.w and y >= p.y and y < p.y + p.h then
            self:onHit(p)
        end
        return true
    end
    return ISCollapsableWindow.onMouseUp(self, x, y)
end

-- A press that leaves the window entirely must not stay armed for the next click.
function BanditsNPCDialogWindow:onMouseUpOutside(x, y)
    self._pressed = nil
    return ISCollapsableWindow.onMouseUpOutside(self, x, y)
end

-- The wheel scrolls the quest strip when the pointer is over it. Returns true only when it
-- actually consumed the scroll, so the wheel keeps working normally everywhere else.
function BanditsNPCDialogWindow:onMouseWheel(del)
    local r = self._questRegion
    if r and (r.max or 0) > 0 then
        local mx, my = self:getMouseX(), self:getMouseY()
        if mx >= r.x and mx < r.x + r.w and my >= r.y and my < r.y + r.h then
            self.questScroll = math.max(0, math.min(r.max, (self.questScroll or 0) + del * getTextManager():getFontHeight(SMALL_FONT) * 2))
            return true
        end
    end
    return false
end

-- `fromPad` = activated by the controller rather than the mouse. It matters for the two
-- controls that answer a click with an ISContextMenu: the menu is a mouse-driven element
-- this window has no joypad focus over, so a pad press would open something the pad then
-- could not reach or dismiss. Those two do the direct thing instead (author's rule:
-- everything must be at least reachable through controller input).
function BanditsNPCDialogWindow:onHit(h, fromPad)
    if not h or not h.kind then return end
    -- "order:NPCStay" -> base "order", arg "NPCStay". One shape for every drawn control.
    local base, arg = string.match(h.kind, "^([^:]+):?(.*)$")
    if base == "order" then
        self:onOrder({ internal = arg })
    elseif base == "dist" then
        -- Only meaningful while following; the strip greys out then, and this agrees.
        local prog = self.brain and self.brain.program and self.brain.program.name
        if prog == "NPCCompanion" then
            pcall(function() BanditsNPC.SetFollowDist(self.zombie, arg) end)
            self:refresh()
        end
    elseif base == "stance" then
        self:onStance({ internal = arg })
    elseif base == "guns" then
        self:onToggleFirearms()
    elseif base == "spot" then
        self:onSetSpot({ internal = arg })
    elseif base == "quest" then
        -- arg is the card index; with one quest in the model it is always 1, and the day
        -- there are several this is where the right one gets picked.
        self:onCompleteQuest()
    elseif base == "schedOn" then
        local sch = self.brain and BanditsNPC.Schedule.Ensure(self.brain)
        BanditsNPC.Schedule.SetEnabled(self.zombie, not (sch and sch.enabled))
        self:refresh()
    elseif base == "schedAct" then
        if fromPad then
            -- No menu on a pad: step to the next activity. Cycling is a worse mouse
            -- interaction (four presses to reach "sleep", four more if you overshoot) but
            -- it is the only one a D-pad can drive without a focusable menu.
            self:cycleScheduleType(tonumber(arg))
        else
            self:onScheduleActivity(tonumber(arg), h)
        end
    elseif base == "schedLoc" then
        self:onScheduleLocation(tonumber(arg))
    elseif base == "takeoff" then
        -- The same two destinations the worn list's right-click menu offered. Into your bag
        -- or into hers is a real choice and clicking the slot should not silently pick one.
        -- On a pad there is no menu to choose in, so it takes the commoner of the two.
        if h.worn then
            if fromPad then
                self:ctxTakeOff(h.worn, false)
            else
                self:showWornContext(h.worn, self:getAbsoluteX() + h.x, self:getAbsoluteY() + h.y + h.h)
            end
        end
    elseif base == "takeweapon" then
        self:onTakeWeapon()
    elseif base == "dbgneed" then
        self:onRaiseNeed({ internal = arg })
    elseif base == "dbgspot" then
        for _, d in ipairs(self.spotDefs) do
            if d.key == arg and d.need then
                self:onRoutineCmd({ internal = { need = d.need, spot = d.key, l = d.label } })
                break
            end
        end
    end
end

-- A MENU, not a cycle. The old editor cycled the activity on every click, so reaching
-- "sleep" from "follow" meant four presses and overshooting meant four more.
function BanditsNPCDialogWindow:onScheduleActivity(idx, h)
    if not idx then return end
    local ctx = ISContextMenu.get(0, self:getAbsoluteX() + h.x, self:getAbsoluteY() + h.y + h.h)
    for _, t in ipairs(BanditsNPC.Schedule.TYPES) do
        -- The label is resolved HERE and not baked into SCHEDULE_TYPES, so a language pack
        -- that loads after this file still reaches it.
        ctx:addOption(BanditsNPC.T("UI_BN_Type_" .. t[1], t[2]), self,
                      BanditsNPCDialogWindow.ctxScheduleType, idx, t[1])
    end
end

function BanditsNPCDialogWindow:ctxScheduleType(idx, t)
    BanditsNPC.Schedule.SetType(self.zombie, idx, t)
    self:refresh()
end

-- The controller's route to the same setting. Both paths read Schedule.TYPES, so the menu
-- and the cycle can never offer different activities.
function BanditsNPCDialogWindow:cycleScheduleType(idx)
    if not idx then return end
    local brain = self.brain
    local sch = brain and BanditsNPC.Schedule.Ensure(brain)
    local cur = (sch and sch.blocks[idx] and sch.blocks[idx].type) or "follow"
    self:ctxScheduleType(idx, BanditsNPC.Schedule.NextType(cur))
end

function BanditsNPCDialogWindow:onScheduleLocation(idx)
    if not idx then return end
    local brain = self.brain
    local sch = brain and BanditsNPC.Schedule.Ensure(brain)
    local t = (sch and sch.blocks[idx] and sch.blocks[idx].type) or "follow"
    if t == "follow" or t == "sleep" then return end
    -- Relax defers to the beacon when there is one; only a companion with no base area
    -- needs to be told where to settle, which is the same rule Interact.OrderRelax uses.
    if t == "relax" then
        local hasBase = false
        pcall(function()
            -- self.zombie, not `z`: the local `z` is declared BELOW this block, so naming
            -- it here would read a nil GLOBAL. That forward-reference has cost this project
            -- three shipped crashes and it is not getting a fourth.
            local site = BanditsNPC.Base and BanditsNPC.Base.SiteFor(self.zombie)
            hasBase = (site and BanditsNPC.Base.Zone(site)) and true or false
        end)
        if hasBase then return end
    end
    -- Guard is a patrol between two tiles, so it asks for two picks; the rest take one.
    local count = (t == "guard") and 2 or 1
    local z = self.zombie
    self:onClose()
    BanditsNPCTileCursor.Begin(getSpecificPlayer(0):getPlayerNum(), count, function(picks)
        BanditsNPC.Schedule.SetLocation(z, idx, picks)
    -- FIX(TASK-010): same destination path as the ScheduleWindow picker -- these
    -- two are separate entry points to Schedule.SetLocation and BOTH need the
    -- validator. Fixing one and not the other leaves the bug reachable.
    end, BanditsNPC.Base and BanditsNPC.Base.STAND_OPTS)
end

function BanditsNPCDialogWindow:onSelectWorkstation()
    local z = self.zombie
    self:onClose()
    if BanditsNPC.Production then BanditsNPC.Production.BeginSelectWorkstation(z) end
end
function BanditsNPCDialogWindow:onClearWorkstation()
    BanditsNPC.Interact.ClearWorkstation(self.zombie)
    self.prodMsg = BanditsNPC.T("UI_BN_Msg_WsCleared", "Workstation cleared.")
    self:refresh()
end

-- Build Workstation menu. If no station of this trade is nearby -> Build (Tier 1). If
-- one exists -> the single next Upgrade step (Add or Replace), clearly separated from a
-- "build a separate new one" option so the two can't be confused. Options gated by
-- skill + materials + hammer, with a tooltip explaining any shortfall.
function BanditsNPCDialogWindow:onBuildWorkstation()
    local st = self.buildWsType
    if not (st and BanditsNPC.Stations) then return end
    local player = getSpecificPlayer(0)
    if not player then return end
    local S = BanditsNPC.Stations
    local px, py, pz = math.floor(player:getX()), math.floor(player:getY()), player:getZ()
    local menu = ISContextMenu.get(player:getPlayerNum(), getMouseX(), getMouseY())

    -- tooltip for a piece/cost
    local function costTip(name, intro, materials, skill, power, okSkill, sm, okMat, mm, hasHammer)
        local tip = ISToolTip:new()
        tip:setName(name)
        local txt = intro
        txt = txt .. " <LINE> " .. BanditsNPC.TF("UI_BN_Ws_Materials", "Materials: %1", S.MaterialText(materials))
        local sk = S.SkillText(skill)
        txt = txt .. " <LINE> " .. BanditsNPC.TF("UI_BN_Ws_Skill", "Skill: %1", (sk ~= "" and sk or BanditsNPC.T("UI_BN_Ws_None", "none")))
        txt = txt .. " <LINE> " .. BanditsNPC.T("UI_BN_Ws_NeedsHammer", "Needs a hammer.")
        if power then txt = txt .. " <LINE> " .. BanditsNPC.T("UI_BN_Ws_NeedsPower", "Needs power to operate (grid power or a running generator).") end
        if not okSkill then txt = txt .. " <LINE> <RGB:1,0.4,0.4> " .. BanditsNPC.TF("UI_BN_Ws_NeedSkill", "Need %1", sm) end
        if not okMat then txt = txt .. " <LINE> <RGB:1,0.4,0.4> " .. BanditsNPC.TF("UI_BN_Ws_Missing", "Missing: %1", mm) end
        if not hasHammer then txt = txt .. " <LINE> <RGB:1,0.4,0.4> " .. BanditsNPC.T("UI_BN_Ws_NeedHammer", "Need a hammer.") end
        tip.description = txt
        return tip
    end

    local function tier1Option(label)
        local parts = S.TierParts(st, 1)
        local piece = parts and parts[1] and S.Pieces[parts[1].piece]
        local okSkill, sm = S.MeetsSkill(player, piece and piece.skill)
        local okMat, mm = S.HasMaterials(player, piece and piece.materials)
        local hasHammer = S.HasHammer(player)
        local opt = menu:addOption(label, self, BanditsNPCDialogWindow.onBuildTier1, st)
        opt.toolTip = costTip(BanditsNPC.TF("UI_BN_Ws_BuildTip", "Build: %1", (piece and piece.label or S.Label(st))),
            BanditsNPC.T("UI_BN_Ws_PlaceIntro", "Place it (rotate with R), then build it with your hammer."),
            piece and piece.materials, piece and piece.skill, false, okSkill, sm, okMat, mm, hasHammer)
        if not (okSkill and okMat and hasHammer) then opt.notAvailable = true end
    end

    local station = S.GetNearbyStation(st, px, py, pz, 20)
    if not station then
        tier1Option(BanditsNPC.TF("UI_BN_Ws_BuildTier1", "Build %1 (Tier 1)", S.Label(st)))
    else
        local hdr = menu:addOption(BanditsNPC.TF("UI_BN_Ws_UpgradeHdr", "--- Upgrade existing (Tier %1) ---", station.tier), nil, nil)
        hdr.notAvailable = true
        local up = S.NextUpgrade(st, station.tier)
        if up then
            local okSkill, sm = S.MeetsSkill(player, up.skill)
            local okMat, mm = S.HasMaterials(player, up.materials)
            local hasHammer = S.HasHammer(player)
            local verb = (up.kind == "add") and BanditsNPC.T("UI_BN_Ws_Add", "Add") or BanditsNPC.T("UI_BN_Ws_Upgrade", "Upgrade")
            local opt = menu:addOption(BanditsNPC.TF("UI_BN_Ws_UpgradeTo", "%1 -> Tier %2 (%3)", verb, up.toTier, up.piece.label),
                self, BanditsNPCDialogWindow.onUpgradeNearby, st)
            local addIntro = up.piece.onBench and BanditsNPC.T("UI_BN_Ws_AddBench", "Place it on the station's bench (or beside it).") or BanditsNPC.T("UI_BN_Ws_AddBeside", "Place a new piece next to the station.")
            opt.toolTip = costTip(BanditsNPC.TF("UI_BN_Ws_VerbTip", "%1: %2", verb, up.piece.label),
                (up.kind == "add") and addIntro or BanditsNPC.T("UI_BN_Ws_Rebuild", "Rebuild this piece in place (walk over + hammer)."),
                up.materials, up.skill, up.power, okSkill, sm, okMat, mm, hasHammer)
            if not (okSkill and okMat and hasHammer) then opt.notAvailable = true end
        else
            local maxed = menu:addOption(BanditsNPC.TF("UI_BN_Ws_Maxed", "Fully upgraded (Tier %1)", station.tier), nil, nil)
            maxed.notAvailable = true
        end
        local sep = menu:addOption(BanditsNPC.T("UI_BN_Ws_SepNew", "--- Build a separate new one ---"), nil, nil)
        sep.notAvailable = true
        tier1Option(BanditsNPC.TF("UI_BN_Ws_BuildNew", "Build new %1 (Tier 1)", S.Label(st)))
    end

    menu:bringToTop()
end

function BanditsNPCDialogWindow:onBuildTier1(st)
    self:onClose()
    local player = getSpecificPlayer(0)
    if player and BanditsNPC.Stations then BanditsNPC.Stations.BeginBuildTier1(player, st) end
end

function BanditsNPCDialogWindow:onUpgradeNearby(st)
    self:onClose()
    local player = getSpecificPlayer(0)
    if not (player and BanditsNPC.Stations) then return end
    local px, py, pz = math.floor(player:getX()), math.floor(player:getY()), player:getZ()
    local station = BanditsNPC.Stations.GetNearbyStation(st, px, py, pz, 20)
    if not station then return end
    local ok, msg = BanditsNPC.Stations.BeginUpgrade(player, st, station)
    if msg then pcall(function() player:Say(msg) end) end
end

-- [DEBUG] change the companion's profession from the Work-tab dropdown.
function BanditsNPCDialogWindow:onChangeProfession(combo)
    if not ((isDebugEnabled and isDebugEnabled()) or (isAdmin and isAdmin())) then return end
    -- options were added as plain strings, so read the text via the combo API (NOT
    -- combo.options[..].text, which is nil for string options -- that was the bug that
    -- made this dropdown do nothing).
    local nm = combo and combo:getOptionText(combo.selected)
    local id = nm and Bandit and Bandit.Expertise and Bandit.Expertise[nm]
    if not id then return end
    local brain = BanditBrain.Get(self.zombie)
    if not brain then return end
    brain.exp = brain.exp or {0, 0, 0}
    brain.exp[1] = id
    brain.npcProfRolled = true
    BanditBrain.Update(self.zombie, brain)
    if Bandit.ForceSyncPart then
        Bandit.ForceSyncPart(self.zombie, { id = brain.id, exp = brain.exp, npcProfRolled = true })
    end
    self.brain = brain
    self.prodMsg = BanditsNPC.TF("UI_BN_Msg_ProfSet", "[DEBUG] Profession set to %1.", nm)
    self:refresh()
end

-- A gated recipe is listed so the player can see what a better station would unlock, but
-- selecting it would put a requirements panel and a live Produce button behind something
-- that cannot be made. The click is refused and the reason is said out loud.
function BanditsNPCDialogWindow:onSelectRecipe(recipe)
    if recipe then
        for _, it in ipairs(self.recipeList.items or {}) do
            if it.item == recipe and it.bnLocked then
                self.prodMsg = BanditsNPC.TF("UI_BN_Msg_RecipeLocked",
                    "That needs a tier-%1 station. This one is tier %2.",
                    (recipe.minTier or 1), (self.prodNearbyTier or 1))
                return
            end
        end
    end
    self.prodSelected = recipe
    self.prodQty = 1
    self.prodMsg = nil
    self:refresh()
end
function BanditsNPCDialogWindow:onProdQtyMinus() self.prodQty = math.max(1, (self.prodQty or 1) - 1) end
-- Capped at what the ingredients allow, not at a flat 20: a stepper that climbs past what
-- you can make only sets up a Produce that refuses.
function BanditsNPCDialogWindow:onProdQtyPlus()
    local cap = math.min(20, math.max(1, self.prodMaxQty or 20))
    self.prodQty = math.min(cap, (self.prodQty or 1) + 1)
end

-- Recipe row: name, then either the output count it yields or the tier it is waiting for.
function BanditsNPCDialogWindow:drawRecipeRow(lb, yy, entry, alt)
    local U = BanditsNPC.UI
    local h = lb.itemheight
    local r = entry.item
    if r and r == self.prodSelected then
        lb:drawRect(0, yy, lb:getWidth(), h, U.C.rowSel.a, U.C.rowSel.r, U.C.rowSel.g, U.C.rowSel.b)
    elseif alt then
        lb:drawRect(0, yy, lb:getWidth(), h, U.C.rowAlt.a, U.C.rowAlt.r, U.C.rowAlt.g, U.C.rowAlt.b)
    end
    local tc = entry.bnLocked and U.C.disText or U.C.btnText
    if r == self.prodSelected then tc = U.C.activeText end

    -- THE ICON OF WHAT IT MAKES (author ask). Every other list in the mod draws item icons
    -- and this one was bare text, so a recipe list read as a menu of words. iconTex caches
    -- per type, so this is a table lookup after the first frame.
    local ICON = BanditsNPC.ItemList.ICON
    local tx = U.S(6)
    if r and r.output then
        local tex = self:iconTex(r.output)
        if tex then
            pcall(function()
                lb:drawTextureScaled(tex, tx, yy + math.floor((h - ICON) / 2), ICON, ICON,
                                     entry.bnLocked and 0.45 or 1, 1, 1, 1)
            end)
            tx = tx + ICON + U.S(5)
        end
    end
    local fh = getTextManager():getFontHeight(SMALL_FONT)
    lb:drawText(entry.text or "", tx, yy + math.floor((h - fh) / 2), tc.r, tc.g, tc.b, 1, SMALL_FONT)

    local right, rc
    if entry.bnLocked then
        right = BanditsNPC.TF("UI_BN_NeedsTier", "Tier %1", entry.bnTier or 1)
        rc = U.C.warn
    elseif r then
        right = "\195\151" .. tostring((r.outCount or 1) * (r == self.prodSelected and (self.prodQty or 1) or 1))
        rc = U.C.muted
    end
    if right then
        local rw = getTextManager():MeasureStringX(SMALL_FONT, right)
        lb:drawText(right, lb:getWidth() - U.S(10) - rw, yy + U.S(4), rc.r, rc.g, rc.b, 1, SMALL_FONT)
    end
    return yy + h
end
function BanditsNPCDialogWindow:onProduceInline()
    if not self.prodSelected then self.prodMsg = BanditsNPC.T("UI_BN_Msg_PickRecipe", "Pick a recipe first."); return end
    local ok, msg = BanditsNPC.Production.Start(self.zombie, self.prodStationType, self.prodSelected, self.prodQty or 1)
    self.prodMsg = msg; self:refresh()
end

function BanditsNPCDialogWindow:onSelectInvItem(item) self.invSelected = item end

-- ===== item actions (used by the buttons, double-click AND the right-click menus) =====

function BanditsNPCDialogWindow:ctxEquip(it)
    if not it then return end
    local isWpn = false; pcall(function() isWpn = it.IsWeapon and it:IsWeapon() end)
    if not isWpn then self.itemsMsg = BanditsNPC.T("UI_BN_Msg_NotWeapon", "That isn't a weapon."); self:refresh(); return end
    local brain = BanditBrain and BanditBrain.Get(self.zombie)
    local oldMelee = brain and brain.weapons and brain.weapons.melee
    local ok, slot = BanditsNPC.Interact.EquipWeapon(self.zombie, it:getFullType(), it)
    if ok then
        -- the carried item BECOMES her wielded weapon: consume it from the bag
        local from = it:getContainer(); if from then from:Remove(it) end
        if slot == "melee" then
            -- give the melee she was holding back into her bag (no dupes, nothing lost)
            if oldMelee and oldMelee ~= "" and oldMelee ~= "Base.BareHands" and oldMelee ~= it:getFullType() then
                pcall(function() self.zombie:getInventory():AddItem(oldMelee) end)
            end
            self.itemsMsg = BanditsNPC.TF("UI_BN_Msg_NowWielding", "Now wielding %1.", it:getName())
        else
            -- a gun: the melee stays as backup; ammo comes from what you give them
            self.itemsMsg = BanditsNPC.TF("UI_BN_Msg_NowCarryingGun", "Now carrying %1 -- give them magazines, rounds or ammo boxes and they'll load them.", it:getName())
        end
        self.invSelected = nil
    elseif slot == "unsupported-firearm" then
        -- ranged, but its magazine/ammo type couldn't be resolved (common for modded guns).
        -- Say so instead of silently parking it in the melee slot, where the engine would
        -- SWING it and play its gunshot as a swing sound.
        self.itemsMsg = BanditsNPC.T("UI_BN_Msg_UnsupportedGun",
            "They can't use that firearm -- its ammo type isn't readable. Give them a melee weapon instead.")
    else
        self.itemsMsg = BanditsNPC.T("UI_BN_Msg_CantEquip", "They can't equip that.")
    end
    self:refresh()
end

function BanditsNPCDialogWindow:onEquipWeapon()
    local it = self.invSelected
    if not it then self.itemsMsg = BanditsNPC.T("UI_BN_Msg_SelectWeapon", "Select a weapon in the list first."); self:refresh(); return end
    self:ctxEquip(it)
end
function BanditsNPCDialogWindow:onTakeWeapon()
    local ok = BanditsNPC.Interact.TakeWeapon(self.zombie)
    self.itemsMsg = ok and BanditsNPC.T("UI_BN_Msg_TookWeapon", "Took their weapon.") or BanditsNPC.T("UI_BN_Msg_NoWeaponHeld", "They aren't holding a weapon.")
    self:refresh()
end
function BanditsNPCDialogWindow:ctxWear(it)
    if not it then return end
    local isCloth = false; pcall(function() isCloth = it.IsClothing and it:IsClothing() end)
    local isBag = BanditsNPC.Interact.IsBag and BanditsNPC.Interact.IsBag(it)
    -- BAGS ARE NOT CLOTHING. InventoryContainer extends InventoryItem directly, so
    -- IsClothing() is false for every backpack and this gate used to refuse them outright
    -- ("That isn't a clothing item") -- which is why giving a companion a bag was impossible
    -- even though Bandits paints one from brain.bag. Route containers to the bag slot.
    if isBag then
        local nm = it:getName()
        -- record THIS bag's look before it is consumed, or she gets painted with a re-rolled
        -- variant of its type instead of the one you handed her
        BanditsNPC.Interact.CaptureItemLook(it, self.brain)
        if BanditsNPC.Interact.WearBag(self.zombie, it:getFullType()) then
            -- The real bag item is CONSUMED here: Bandits paints a worn bag from
            -- brain.bag.name, which holds a TYPE, not an item. So anything inside the bag
            -- would be destroyed with it -- give her a full backpack and the contents were
            -- gone. Tip them into her own inventory first. (Snapshot the list before
            -- removing: mutating a container while iterating it skips entries.)
            local moved = 0
            local inv = BanditsNPC.ItemList.ContainerOf(it)
            if inv then
                local kids = {}
                pcall(function()
                    local list = inv:getItems()
                    for i = 0, list:size() - 1 do table.insert(kids, list:get(i)) end
                end)
                for _, k in ipairs(kids) do
                    pcall(function()
                        inv:Remove(k)
                        self.zombie:getInventory():addItem(k)
                        moved = moved + 1
                    end)
                end
            end
            local from = it:getContainer(); if from then from:Remove(it) end
            self.invSelected = nil
            self.itemsMsg = BanditsNPC.TF("UI_BN_Msg_NowWearingBag", "Now carrying %1 on their back.", nm)
            if moved > 0 then
                self.itemsMsg = self.itemsMsg .. "  "
                    .. BanditsNPC.TF("UI_BN_Msg_BagEmptied", "Moved %1 items out of it first.", moved)
            end
        else
            self.itemsMsg = BanditsNPC.T("UI_BN_Msg_CantWear", "They can't wear that.")
        end
        self:refresh(); return
    end
    if not isCloth then self.itemsMsg = BanditsNPC.T("UI_BN_Msg_NotClothing", "That isn't a clothing item."); self:refresh(); return end
    -- B42: getBodyLocation() returns an OBJECT, not a string -- normalize to the canonical
    -- Bandits key BEFORE doing anything with it (raw objects broke rendering, threw on
    -- concat, and the item was consumed anyway = "wear deleted my clothes" Workshop bug).
    -- LocationOf applies vanilla's own resolution order and also covers garments whose
    -- location lives on canBeEquipped() instead of getBodyLocation() (some modded clothing).
    local loc = BanditsNPC.Interact.LocationOf(it)
    if not loc then self.itemsMsg = BanditsNPC.T("UI_BN_Msg_UnknownLoc", "Can't tell where that's worn."); self:refresh(); return end
    local nm = it:getName()
    -- same as the bag branch: capture the real garment's colour/variant before it is consumed
    BanditsNPC.Interact.CaptureItemLook(it, self.brain)
    if BanditsNPC.Interact.WearClothing(self.zombie, it:getFullType(), loc) then
        -- consume the real item she was carrying ONLY once it's actually worn
        local from = it:getContainer(); if from then from:Remove(it) end
        self.invSelected = nil
        local locName = BanditsNPC.T("UI_BN_Loc_" .. tostring(loc), tostring(loc))
        self.itemsMsg = BanditsNPC.TF("UI_BN_Msg_NowWearing", "Now wearing %1  (%2).", nm, locName)
    else
        self.itemsMsg = BanditsNPC.T("UI_BN_Msg_CantWear", "They can't wear that.")
    end
    self:refresh()
end

function BanditsNPCDialogWindow:onWearClothing()
    local it = self.invSelected
    if not it then self.itemsMsg = BanditsNPC.T("UI_BN_Msg_SelectClothing", "Select a clothing item in the carried list first."); self:refresh(); return end
    self:ctxWear(it)
end

function BanditsNPCDialogWindow:onSelectPlayerItem(item) self.playerSelected = item end

-- ===== the two-panel transfer (v0.77.2) =====
--
-- Hand `item` over and return it as it now exists in HER inventory, or nil. TakeFromPlayer
-- rather than a bare Remove: an item still referenced by your worn items or hand slots
-- stays painted on your model after it changes owner.
function BanditsNPCDialogWindow:giveToCompanion(item)
    if not item then return nil end
    local player = getSpecificPlayer(0)
    if not player then return nil end
    -- Refuse rather than overload. One press moves a whole stack now, so an unchecked give
    -- is not "one more item" but twenty, and a companion buried under her own carry weight
    -- walks at a crawl for reasons the player never sees.
    if not BanditsNPC.ItemList.Fits(item, self.zombie:getInventory()) then return nil end
    BanditsNPC.Interact.TakeFromPlayer(player, item)
    self.zombie:getInventory():addItem(item)
    -- given mags/rounds become her reload supply immediately (see SyncAmmo)
    if BanditsNPC.Interact and BanditsNPC.Interact.SyncAmmo then
        pcall(function() BanditsNPC.Interact.SyncAmmo(self.zombie) end)
    end
    return item
end

-- Give the WHOLE row. A row that reads "Screws x5" is five screws; handing over one of
-- them and leaving four identical-looking ones behind reads as the button not working.
-- "Give one" is on the right-click menu for when that is really what you meant.
function BanditsNPCDialogWindow:onGiveSelected()
    local it = self.playerSelected
    if not it then
        self.itemsMsg = BanditsNPC.T("UI_BN_Msg_SelectYours", "Select something you are carrying first.")
        self:refresh(); return
    end
    local stack = BanditsNPC.ItemList.StackOf(self.playerList, it)
    local nm = itemName(it:getFullType())
    local n = 0
    for _, s in ipairs(stack) do
        if self:giveToCompanion(s) then n = n + 1 else break end
    end
    -- Say which of the three things happened. "Gave them 3 of 8" is the one that matters:
    -- a partial transfer that reported success would look like items vanishing.
    if n == 0 then
        self.itemsMsg = BanditsNPC.T("UI_BN_Msg_TheyreFull", "They can't carry any more.")
    elseif n < #stack then
        self.itemsMsg = BanditsNPC.TF("UI_BN_Msg_GavePartial", "Gave them %1 of %2 \195\151 %3.", n, #stack, nm)
    elseif n > 1 then
        self.itemsMsg = BanditsNPC.TF("UI_BN_Msg_GaveN", "Gave them %1 \195\151 %2.", nm, n)
    else
        self.itemsMsg = BanditsNPC.TF("UI_BN_Msg_Gave", "Gave them %1.", nm)
    end
    self.playerSelected = nil
    self:refresh()
end

function BanditsNPCDialogWindow:ctxGiveOne(item)
    if not item then return end
    local nm = itemName(item:getFullType())
    self.itemsMsg = self:giveToCompanion(item)
        and BanditsNPC.TF("UI_BN_Msg_Gave", "Gave them %1.", nm)
        or BanditsNPC.T("UI_BN_Msg_TheyreFull", "They can't carry any more.")
    self.playerSelected = nil
    self:refresh()
end

function BanditsNPCDialogWindow:showPlayerContext(it, absX, absY)
    if not it then return end
    local ctx = ISContextMenu.get(0, absX, absY)
    local stack = BanditsNPC.ItemList.StackOf(self.playerList, it)
    if #stack > 1 then
        ctx:addOption(BanditsNPC.TF("UI_BN_Ctx_GiveAll", "Give all %1", #stack), self, BanditsNPCDialogWindow.onGiveSelected)
        ctx:addOption(BanditsNPC.T("UI_BN_Ctx_GiveOne", "Give one"), self, BanditsNPCDialogWindow.ctxGiveOne, it)
    else
        ctx:addOption(BanditsNPC.T("UI_BN_Give", "Give"), self, BanditsNPCDialogWindow.ctxGiveOne, it)
    end
    ctx:addOption(BanditsNPC.T("UI_BN_MakeWear", "Make wear"), self, BanditsNPCDialogWindow.onMakeWear)
    ctx:addOption(BanditsNPC.T("UI_BN_ArmThem", "Arm them"), self, BanditsNPCDialogWindow.onArmThem)
end

-- Give AND put on, in one press. Nobody hands over a jacket and then separately decides
-- whether it should be worn.
function BanditsNPCDialogWindow:onMakeWear()
    local it = self.playerSelected
    if not it then
        self.itemsMsg = BanditsNPC.T("UI_BN_Msg_SelectYours", "Select something you are carrying first.")
        self:refresh(); return
    end
    if not self:giveToCompanion(it) then
        self.itemsMsg = BanditsNPC.T("UI_BN_Msg_TheyreFull", "They can't carry any more.")
        self:refresh(); return
    end
    self.playerSelected = nil
    self:ctxWear(it)     -- ctxWear refreshes and writes its own message
end

function BanditsNPCDialogWindow:onArmThem()
    local it = self.playerSelected
    if not it then
        self.itemsMsg = BanditsNPC.T("UI_BN_Msg_SelectYours", "Select something you are carrying first.")
        self:refresh(); return
    end
    if not self:giveToCompanion(it) then
        self.itemsMsg = BanditsNPC.T("UI_BN_Msg_TheyreFull", "They can't carry any more.")
        self:refresh(); return
    end
    self.playerSelected = nil
    self:ctxEquip(it)
end

-- ===== right-click context menus (player-inventory-style) =====

function BanditsNPCDialogWindow:showInvContext(it, absX, absY)
    if not it then return end
    local ctx = ISContextMenu.get(0, absX, absY)
    local isCloth = false; pcall(function() isCloth = it.IsClothing and it:IsClothing() end)
    local isWpn = false; pcall(function() isWpn = it.IsWeapon and it:IsWeapon() end)
    -- bags are containers, not Clothing -- offer Wear for both or backpacks get no option
    local isBag = BanditsNPC.Interact.IsBag and BanditsNPC.Interact.IsBag(it)
    if isCloth or isBag then ctx:addOption(BanditsNPC.T("UI_BN_Ctx_Wear", "Wear"), self, BanditsNPCDialogWindow.ctxWear, it) end
    if isWpn then ctx:addOption(BanditsNPC.T("UI_BN_Ctx_EquipWeapon", "Equip as weapon"), self, BanditsNPCDialogWindow.ctxEquip, it) end
    local stack = BanditsNPC.ItemList.StackOf(self.invList, it)
    if #stack > 1 then
        ctx:addOption(BanditsNPC.TF("UI_BN_Ctx_TakeAllN", "Take all %1", #stack), self, BanditsNPCDialogWindow.ctxTake, it)
        ctx:addOption(BanditsNPC.T("UI_BN_Ctx_TakeOne", "Take one"), self, BanditsNPCDialogWindow.ctxTakeOne, it)
    else
        ctx:addOption(BanditsNPC.T("UI_BN_Ctx_Take", "Take"), self, BanditsNPCDialogWindow.ctxTake, it)
    end
end

function BanditsNPCDialogWindow:showWornContext(d, absX, absY)
    if not d then return end
    local ctx = ISContextMenu.get(0, absX, absY)
    ctx:addOption(BanditsNPC.T("UI_BN_Ctx_TakeOffToYou", "Take off - into your inventory"), self, BanditsNPCDialogWindow.ctxTakeOffToPlayer, d)
    ctx:addOption(BanditsNPC.T("UI_BN_Ctx_TakeOffToBag", "Take off - into their bag"), self, BanditsNPCDialogWindow.ctxTakeOffToHer, d)
end

function BanditsNPCDialogWindow:ctxTakeOff(d, toHerBag)
    if not d then return end
    -- Read the appearance record BEFORE the removal: RemoveClothing/RemoveBag redress her,
    -- and brain.clothing holds TYPE STRINGS, so the garment that comes back is a brand-new
    -- item the engine re-rolls. Without this, lending her your shirt and taking it back
    -- returned a DIFFERENT-COLOURED shirt -- which is the likeliest thing behind "I think my
    -- shirt's colour changed". RestoreItemLook stamps the saved look onto the new item.
    local brain = self.brain
    -- strip the visual FIRST; only a successful removal spawns the garment (no dupes)
    local removed
    if d.bag then removed = (BanditsNPC.Interact.RemoveBag(self.zombie) ~= nil)
    else removed = BanditsNPC.Interact.RemoveClothing(self.zombie, d.loc) end
    if removed then
        if toHerBag then
            pcall(function()
                BanditsNPC.Interact.RestoreItemLook(
                    self.zombie:getInventory():AddItem(d.type), brain, d.type)
            end)
            self.itemsMsg = BanditsNPC.TF("UI_BN_Msg_TookOffToBag", "Took off %1  (put in their bag).", itemName(d.type))
        else
            local player = getSpecificPlayer(0)
            if player then
                pcall(function()
                    BanditsNPC.Interact.RestoreItemLook(
                        player:getInventory():AddItem(d.type), brain, d.type)
                end)
            end
            self.itemsMsg = BanditsNPC.TF("UI_BN_Msg_TookOffToYou", "Took off %1  (now in your bag).", itemName(d.type))
        end
    else
        self.itemsMsg = BanditsNPC.T("UI_BN_Msg_CantTakeOff", "Couldn't take that off.")
    end
    self:refresh()
end

function BanditsNPCDialogWindow:ctxTakeOffToHer(d) self:ctxTakeOff(d, true) end
function BanditsNPCDialogWindow:ctxTakeOffToPlayer(d) self:ctxTakeOff(d, false) end

-- Takes the WHOLE row: a stacked row stands for every item behind it. ctxTakeOne is the
-- escape hatch, on the right-click menu.
function BanditsNPCDialogWindow:ctxTake(item)
    if not item then return end
    for _, s in ipairs(BanditsNPC.ItemList.StackOf(self.invList, item)) do
        self:ctxTakeOne(s, true)
    end
    self.invSelected = nil; self:refresh()
end

function BanditsNPCDialogWindow:ctxTakeOne(item, quiet)
    if not item then return end
    local from = item:getContainer(); if from then from:Remove(item) end
    getSpecificPlayer(0):getInventory():addItem(item)
    if not quiet then self.invSelected = nil; self:refresh() end
end

function BanditsNPCDialogWindow:onTakeItem()
    if not self.invSelected then return end
    self:ctxTake(self.invSelected)
end
function BanditsNPCDialogWindow:onTakeAll()
    local list = ArrayList.new()
    self.zombie:getInventory():getAllEvalRecurse(function() return true end, list)
    local pinv = getSpecificPlayer(0):getInventory()
    for i = 0, list:size() - 1 do
        local it = list:get(i)
        local from = it:getContainer(); if from then from:Remove(it) end
        pinv:addItem(it)
    end
    self.invSelected = nil; self:refresh()
end
-- (onGiveItem is gone: giving used to open a whole separate window to pick from your own
-- inventory, which is now the right-hand panel of this tab. BanditsNPCGiveWindow itself
-- stays -- the recruit flow still uses it to ask for one specific item.)

function BanditsNPCDialogWindow:onClose()
    -- Hand the controller back BEFORE anything else runs: a window that closes while
    -- holding joypad focus takes the pad away from the whole game.
    if BanditsNPC.Joypad then BanditsNPC.Joypad.Release() end
    if BanditsNPC.Portrait then BanditsNPC.Portrait.Hide() end
    self:removeFromUIManager()
    if BanditsNPCDialogWindow.instance == self then BanditsNPCDialogWindow.instance = nil end
end

-- ===== render =====
local function programLabel(name)
    return BanditsNPC.ProgramLabel(name)   -- shared, translated (BanditsNPC.lua)
end

-- ===========================================================================
-- PROFILE TAB (v0.77.1)
-- ===========================================================================
--
-- Relationship, traits, background, the quest card, then a strip of facts. Every number
-- here comes from BanditsNPC.ProfileData, which answers with nil and a reason where the mod
-- genuinely does not know -- so the gaps show as one consistent TODO line rather than as
-- plausible-looking invented values.
--
-- The mock-up's big portrait is deliberately NOT here. There is exactly one ISUI3DModel in
-- the mod (BanditsNPCPortrait keeps a single instance on purpose -- an earlier attempt with
-- one per window crashed on FBO accumulation), it now lives in the rail where it shows on
-- every tab, and a second frame here would either fight it for the renderer or draw an
-- empty box. An empty box is what the author reported last build.
function BanditsNPCDialogWindow:drawProfileTab(x, y, w, brain, recruited)
    local U = BanditsNPC.UI
    local D = BanditsNPC.ProfileData
    local sgap = U.S(U.M.sectionGap)
    local tm = getTextManager()

    -- ---- Mood and age, one line at the top (author ask) -------------------
    -- Two facts about her as a person, above everything that is about the two of you.
    if recruited then
        local mood = D.Mood(brain)
        local age = D.Age(brain)
        local dash = BanditsNPC.T("UI_BN_Unknown", "\226\128\148")
        self:drawText(BanditsNPC.T("UI_BN_Mood", "Mood"), x, y,
                      U.C.label.r, U.C.label.g, U.C.label.b, 1, SMALL_FONT)
        local mw = tm:MeasureStringX(SMALL_FONT, BanditsNPC.T("UI_BN_Mood", "Mood"))
        self:drawText(mood or dash, x + mw + U.S(8), y, 1, 1, 1, 1, SMALL_FONT)
        local aLbl = BanditsNPC.T("UI_BN_Fact_Age", "Age")
        local aVal = age and tostring(age) or dash
        local avw = tm:MeasureStringX(SMALL_FONT, aVal)
        local alw = tm:MeasureStringX(SMALL_FONT, aLbl)
        self:drawText(aLbl, x + w - avw - U.S(8) - alw, y,
                      U.C.label.r, U.C.label.g, U.C.label.b, 1, SMALL_FONT)
        self:drawText(aVal, x + w - avw, y, 1, 1, 1, 1, SMALL_FONT)
        y = y + FONT_HGT + U.S(6)
    end

    -- ---- Relationship ---------------------------------------------------
    if recruited then
        local rel = D.Relationship(brain)
        y = U.Header(self, x, y, w, BanditsNPC.T("UI_BN_Relationship", "Relationship"), rel.label)
        local trackY = y
        y = U.Track(self, x, y, w, (rel.max > 0) and (rel.value / rel.max) or 0,
                    math.floor(rel.value) .. " / " .. rel.max)

        -- THE LADDER IS A GRID, not a sentence (author ask). Each tier name sits at the
        -- position on the bar where that tier actually STARTS, with a tick down to it, so
        -- the bar answers "how far to Friendly" by looking at it. Strung together with
        -- middots the five names said nothing about where the thresholds were.
        --
        -- The names are the mod's OWN five tiers, and the positions are Affinity.Tiers --
        -- the same table GetTier reads, so the labels cannot drift off the thresholds.
        local capW = tm:MeasureStringX(SMALL_FONT, math.floor(rel.value) .. " / " .. rel.max) + U.S(8)
        local barW = w - capW
        local tiers = (BanditsNPC.Affinity and BanditsNPC.Affinity.Tiers) or { 0, 25, 50, 75, 100 }
        local curTier = rel.tier or 0
        for i = 1, math.min(#tiers, #rel.ladder) do
            local frac = (rel.max > 0) and (tiers[i] / rel.max) or 0
            local tickX = x + math.floor(barW * frac)
            if i > 1 then U.Fill(self, math.min(tickX, x + barW - 1), trackY, 1, U.S(11), U.C.hairline) end
            local name = rel.ladder[i] or ""
            local nw = tm:MeasureStringX(SMALL_FONT, name)
            -- Clamped inside the bar: the first label would hang off the left edge and the
            -- last off the right if each were simply centred on its tick.
            local lx = math.max(x, math.min(tickX - math.floor(nw / 2), x + barW - nw))
            local c = (i - 1 == curTier) and U.C.activeText or U.C.muted
            self:drawText(name, lx, y, c.r, c.g, c.b, 1, SMALL_FONT)
        end
        y = y + FONT_HGT + U.S(3)
        local nx = D.NextTier(brain)
        local hint
        if not nx then
            hint = BanditsNPC.T("UI_BN_TierTop", "This is the highest tier.")
        elseif nx.slower > 0 then
            -- A single % here, not %%: Fill returns early on the NUMBERED path and never
            -- unescapes %%, so a doubled sign would print literally.
            hint = BanditsNPC.TF("UI_BN_TierNextSlower", "At %1 (%2) their needs rise %3% more slowly.",
                                 nx.label, nx.at, nx.slower)
        else
            hint = BanditsNPC.TF("UI_BN_TierNextPlain", "Next tier: %1 at %2.", nx.label, nx.at)
        end
        y = self:drawTextWrapped(hint, x, y, w) + sgap
    end

    -- ---- Traits ---------------------------------------------------------
    -- TODO(BanditsNPC.ProfileData.Traits): the mock lists traits with mechanical effects
    -- ("Wary - bond gains halved below 5"). The backstory generator has flavour lines, not
    -- trait objects, so there is nothing to enumerate. D.Traits returns nil and its reason.
    local traits, why = D.Traits()
    y = U.Header(self, x, y, w, BanditsNPC.T("UI_BN_Traits", "Traits"))
    if traits then
        for _, t in ipairs(traits) do
            self:drawText(t.name or "", x, y, U.C.btnText.r, U.C.btnText.g, U.C.btnText.b, 1, SMALL_FONT)
            if t.effect then
                local ew = tm:MeasureStringX(SMALL_FONT, t.effect)
                self:drawText(t.effect, x + w - ew, y, U.C.muted.r, U.C.muted.g, U.C.muted.b, 1, SMALL_FONT)
            end
            y = y + FONT_HGT + U.S(2)
        end
    else
        y = self:drawTextWrapped(why or "", x, y, w)
    end
    y = y + sgap

    -- ---- Background -----------------------------------------------------
    y = U.Header(self, x, y, w, BanditsNPC.T("UI_BN_Background", "Background"))
    local revealed = brain.storyRevealed or 0
    local parts = brain.storyParts or {}
    if revealed <= 0 then
        y = self:drawTextWrapped(BanditsNPC.T("UI_BN_Story_Unknown", "You don't know much about them yet. Press Talk to learn more."), x, y, w)
    else
        if brain.occupationName then
            self:drawText(BanditsNPC.TF("UI_BN_Story_BeforeOutbreak", "Before the outbreak: %1", brain.occupationName),
                          x, y, U.C.label.r, U.C.label.g, U.C.label.b, 1, SMALL_FONT)
            y = y + FONT_HGT + U.S(4)
        end
        for i = 1, math.min(revealed, #parts) do
            y = self:drawTextWrapped(parts[i], x, y, w) + U.S(3)
        end
    end
    y = y + sgap

    -- ---- Quests ----------------------------------------------------------
    -- A SEPARATE THING, with air above it (author ask): the background paragraph runs to a
    -- ragged edge and without the gap the quest heading read as its last line.
    if recruited then
        y = self:drawQuestRegion(x, y + U.S(8), w, brain)
    elseif self.recruitMsg then
        self:drawTextWrapped(self.recruitMsg, x, y, w)
    end

    -- ---- Facts strip ----------------------------------------------------
    -- Bottom-anchored so it is a footer to the tab rather than something that slides
    -- around with the length of her backstory.
    if recruited then
        local dash = BanditsNPC.T("UI_BN_Unknown", "\226\128\148")
        local metDay, together = D.MetDay(brain), D.DaysTogether(brain)
        local age = D.Age(brain)
        local facts = {
            { BanditsNPC.T("UI_BN_Fact_Met", "Met"), metDay and BanditsNPC.TF("UI_BN_DayN", "day %1", metDay) or dash },
            { BanditsNPC.T("UI_BN_Fact_Together", "Together"), together and BanditsNPC.TF("UI_BN_NDays", "%1 days", together) or dash },
            { BanditsNPC.T("UI_BN_Fact_Kills", "Kills"), tostring(D.Kills(brain)) },
            { BanditsNPC.T("UI_BN_Fact_Age", "Age"), age and tostring(age) or dash },
        }
        local fy = self.contentBottom - FONT_HGT
        U.Fill(self, x, fy - U.S(6), w, 1, U.C.hairline)
        local cw = math.floor(w / #facts)
        for i, f in ipairs(facts) do
            local s = f[1] .. "  \194\183  " .. f[2]
            self:drawText(U.Fit(s, cw - U.S(6)), x + (i - 1) * cw, fy,
                          U.C.muted.r, U.C.muted.g, U.C.muted.b, 1, SMALL_FONT)
        end
    end
    return y
end

-- The quest REGION: a heading, then a scrollable strip of cards between there and the facts
-- line at the bottom of the tab.
--
-- The model yields at most one quest today (brain.quest is a single table or false), so the
-- strip is written to take a LIST and scroll when the cards outgrow the space, rather than
-- to the single card that happens to fit right now. When a second quest can exist, it lands
-- in the list and nothing here changes. The scrollbar and the clipping only appear when
-- there is genuinely something below the fold -- an always-on scrollbar over one short card
-- is the thing that looks wrong.
function BanditsNPCDialogWindow:drawQuestRegion(x, y, w, brain)
    local U = BanditsNPC.UI
    -- One place builds the list, so "how many quests are there" has a single answer.
    local quests = {}
    if brain.quest and brain.quest ~= false then quests[1] = brain.quest end

    y = U.Header(self, x, y, w, BanditsNPC.T("UI_BN_Tab_Quest", "Quest"),
                 (#quests > 0) and BanditsNPC.T("UI_BN_QuestActive", "Active") or nil)
    -- The facts strip owns the last line of the tab; the region stops short of it.
    local bottom = self.contentBottom - FONT_HGT - U.S(10)
    local regionH = bottom - y
    if regionH < U.S(40) then return y end

    if #quests == 0 then
        self:drawTextWrapped(BanditsNPC.T("UI_BN_NoQuest", "Nothing right now -- check back in a few days."), x, y, w)
        if self.questMsg then self:drawTextWrapped(self.questMsg, x, y + FONT_HGT + U.S(6), w) end
        return y + FONT_HGT
    end

    -- Measure first: whether it scrolls decides whether a gutter is reserved, and the cards
    -- have to be laid out in the width they will actually get.
    local gutter = U.S(U.M.scrollGutter) + U.S(3)
    local totalH = 0
    for _, q in ipairs(quests) do totalH = totalH + self:questCardHeight(q, w) + U.S(6) end
    local scrolls = totalH > regionH
    local cardW = scrolls and (w - gutter) or w

    if scrolls then
        totalH = 0
        for _, q in ipairs(quests) do totalH = totalH + self:questCardHeight(q, cardW) + U.S(6) end
        local maxScroll = math.max(0, totalH - regionH)
        self.questScroll = math.max(0, math.min(self.questScroll or 0, maxScroll))
        self:setStencilRect(x, y, w, regionH)
    else
        self.questScroll = 0
    end

    local cy = y - (self.questScroll or 0)
    self._questRegion = { x = x, y = y, w = w, h = regionH, max = math.max(0, totalH - regionH) }
    for i, q in ipairs(quests) do
        cy = self:drawQuestCard(x, cy, cardW, brain, q, i) + U.S(6)
    end
    if scrolls then
        self:clearStencilRect()
        -- Thumb sized to the fraction on screen, the same shape U.List draws.
        local th = math.max(U.S(16), math.floor(regionH * regionH / totalH))
        local frac = (self._questRegion.max > 0) and ((self.questScroll or 0) / self._questRegion.max) or 0
        U.Fill(self, x + w - gutter + U.S(3), y + math.floor((regionH - th) * frac),
               U.S(U.M.scrollGutter) - 1, th, U.C.btnBorder)
    end
    return y + regionH
end

-- How tall the card for `q` needs to be. Shared by the measure pass and the draw so the
-- bordered box can never come out a different size from what goes in it.
function BanditsNPCDialogWindow:questCardHeight(q, w)
    local U = BanditsNPC.UI
    local pad = U.S(8)
    local quoteH = self:measureWrapped("\"" .. (q.desc or "") .. "\"", w - pad * 2)
    return pad + FONT_HGT + U.S(5) + quoteH + U.S(6) + U.S(U.M.btnH) + pad
end

-- One quest card. Order is the brief's: heading and reward, then the quote, then the
-- progress bar with n / total, then Turn in -- DISABLED until the bar is full.
function BanditsNPCDialogWindow:drawQuestCard(x, y, w, brain, q, idx)
    local U = BanditsNPC.UI
    local hits = self._hits
    local pad = U.S(8)
    local tm = getTextManager()
    if not q then return y end

    local curp, total = self:memo("questProgress", function()
        return BanditsNPC.Quests.Progress(brain, getSpecificPlayer(0), self.zombie)
    end)
    local done = (total > 0 and curp >= total)

    local quote = "\"" .. (q.desc or "") .. "\""
    local innerW = w - pad * 2
    local boxH = self:questCardHeight(q, w)
    U.Fill(self, x, y, w, boxH, { r = 0, g = 0, b = 0, a = 0.25 })
    U.Border(self, x, y, w, boxH, U.C.hairline)

    local iy = y + pad
    -- A quest has no title of its own in the data -- Generate stores type, desc and reward
    -- and nothing else -- so the heading is the type, which is true, rather than a name
    -- invented for the card.
    local kind = (q.type == "hunt") and BanditsNPC.T("UI_BN_QuestType_hunt", "Hunt")
                                     or BanditsNPC.T("UI_BN_QuestType_fetch", "Fetch")
    self:drawText(kind, x + pad, iy, 1, 1, 1, 1, SMALL_FONT)
    local rew = BanditsNPC.TF("UI_BN_RewardShort", "Reward +%1 %2",
                              (q.reward or 0), BanditsNPC.Affinity.GetLabel(brain))
    local rw = tm:MeasureStringX(SMALL_FONT, rew)
    self:drawText(rew, x + w - pad - rw, iy, U.C.label.r, U.C.label.g, U.C.label.b, 1, SMALL_FONT)
    iy = iy + FONT_HGT + U.S(5)

    iy = self:drawTextWrapped(quote, x + pad, iy, innerW) + U.S(6)

    local btnW = U.S(90)
    U.Track(self, x + pad, iy + U.S(4), innerW - btnW - U.S(10),
            (total > 0) and (curp / total) or 0, curp .. " / " .. total)
    U.Button(self, x + w - pad - btnW, iy, btnW, BanditsNPC.T("UI_BN_TurnIn", "Turn in"),
             done and "emph" or "disabled", hits, "quest:" .. (idx or 1))
    return y + boxH
end

-- ===========================================================================
-- ORDERS TAB (v0.77.1)
-- ===========================================================================
--
-- Movement, Distance and Stance as welded segmented strips, then the daily schedule as a
-- block on this tab rather than behind a "Daily Schedule..." button, then the routine spots
-- as rows that SAY WHAT IS ASSIGNED. The old grid was eight identical "Set Bed" buttons and
-- there was no way to tell which spots you had already done.
--
-- Everything here registers into self._hits; see the note in createChildren for why these
-- are drawn rather than built from ISButton.
local SPOT_COLS = 2

function BanditsNPCDialogWindow:drawOrdersTab(x, y, w, brain)
    local U = BanditsNPC.UI
    local hits = self._hits
    local gap = U.S(U.M.gap)
    local sgap = U.S(U.M.sectionGap)

    -- ---- Movement -------------------------------------------------------
    -- Hide is Stay plus a passive stance (Interact.OrderHide), so it has no program of its
    -- own; reading it back means testing both. Doing it this way keeps the rule the brief
    -- asks for -- never leave a radio group with no visible selection.
    local prog = (brain.program and brain.program.name) or "NPCCompanion"
    local stance = brain.npcStance or "defensive"
    local moveKey = "NPCCompanion"
    if prog == "NPCStay" then moveKey = (stance == "passive") and "__hide" or "NPCStay"
    elseif prog == "NPCGuard" then moveKey = "NPCGuard"
    elseif prog == "NPCRelax" then moveKey = "__relax" end

    -- A manual order switches the schedule off (onOrder calls Schedule.Disable), so the
    -- honest note here is which of the two is in charge -- not a countdown to a boundary
    -- the code does not run to.
    local sch = brain.schedule
    local note = (sch and sch.enabled)
        and BanditsNPC.T("UI_BN_SchedRunning", "the schedule is driving this")
        or nil
    -- SLIGHTLY squarer than the 22px default, and no more (author, second pass: the medium
    -- font in a 30px box was "too big"). PZ offers Small then Medium and nothing between,
    -- so the text stays Small and only the box grows -- 26px is the whole adjustment.
    local STRIP_H = math.max(U.S(26), FONT_HGT + U.S(10))
    local STRIP_F = SMALL_FONT
    y = U.Header(self, x, y, w, BanditsNPC.T("UI_BN_Lbl_Movement", "Movement"), note)
    y = U.Segmented(self, x, y, w, {
        { key = "NPCCompanion", label = BanditsNPC.T("UI_BN_Order_Follow", "Follow") },
        { key = "NPCStay",      label = BanditsNPC.T("UI_BN_Order_Stay", "Stay") },
        { key = "NPCGuard",     label = BanditsNPC.T("UI_BN_Order_Guard", "Guard") },
        { key = "__relax",      label = BanditsNPC.T("UI_BN_Order_Relax", "Relax") },
        { key = "__hide",       label = BanditsNPC.T("UI_BN_Order_Hide", "Hide") },
    }, moveKey, hits, "order:", STRIP_F, STRIP_H)
    -- Movement and Distance are two different questions and were welded into one block of
    -- fifty pixels; the gap says the second one is about the first rather than more of it.
    y = y + gap

    -- Distance only means anything while she is actually following, so off Follow the strip
    -- greys out rather than vanishing -- a control that disappears leaves the player
    -- wondering what they did wrong.
    local following = (moveKey == "NPCCompanion")
    local fd = "near"
    pcall(function() fd = BanditsNPC.GetFollowDist(self.zombie) or "near" end)
    y = U.Segmented(self, x, y, w, {
        { key = "near", label = BanditsNPC.T("UI_BN_Dist_Near", "Near"),   disabled = not following },
        { key = "mid",  label = BanditsNPC.T("UI_BN_Dist_Mid", "Middle"),  disabled = not following },
        { key = "far",  label = BanditsNPC.T("UI_BN_Dist_Far", "Far"),     disabled = not following },
    -- The current choice stays lit even while the strip is disabled: it is still her
    -- setting, it just is not adjustable until she is following again.
    }, fd, hits, "dist:", STRIP_F, STRIP_H)
    y = y + sgap

    -- ---- Stance ---------------------------------------------------------
    y = U.Header(self, x, y, w, BanditsNPC.T("UI_BN_Lbl_Stance", "Stance"))
    local gunsW = U.S(120)
    U.Segmented(self, x, y, w - gunsW - gap, {
        { key = "passive",    label = BanditsNPC.T("UI_BN_Stance_Passive", "Passive") },
        { key = "defensive",  label = BanditsNPC.T("UI_BN_Stance_Defensive", "Defensive") },
        { key = "aggressive", label = BanditsNPC.T("UI_BN_Stance_Aggressive", "Aggressive") },
    }, stance, hits, "stance:", STRIP_F, STRIP_H)
    local gunsOn = not brain.npcNoGuns
    y = U.Button(self, x + w - gunsW, y, gunsW,
        gunsOn and BanditsNPC.T("UI_BN_FirearmsShortOn", "Firearms on")
               or BanditsNPC.T("UI_BN_FirearmsShortOff", "Firearms off"),
        gunsOn and "active" or nil, hits, "guns", STRIP_F, STRIP_H)
    local gh = hits[#hits]
    if gh and gh.kind == "guns" then
        gh.tip = BanditsNPC.T("UI_BN_Tooltip_Firearms", "ON: they fight with their gun whenever they have ammo for it.\nOFF: their guns are safely stashed (nothing is lost) and they use their melee weapon only.")
    end
    y = y + sgap

    -- ---- Daily schedule -------------------------------------------------
    y = self:drawScheduleBlock(x, y, w, brain)
    y = y + sgap

    -- ---- Routine spots --------------------------------------------------
    y = U.Header(self, x, y, w, BanditsNPC.T("UI_BN_Lbl_RoutineSpots", "Routine spots"),
                 BanditsNPC.T("UI_BN_SpotsNote", "used automatically while recruited"))
    local spots = brain.spots or {}
    local rowH = U.RowH()
    local rowGap = U.S(3)          -- author ask: the rows were touching
    local pitch = rowH + rowGap
    local colW = math.floor((w - gap) / SPOT_COLS)
    local tm = getTextManager()
    local dbg = BanditsNPC.UI.DebugOn()
    -- In debug each row grows a green plus AFTER its label ("Bed +") that sends her to that
    -- spot NOW. That replaces the "Debug - send to spot now" row of six buttons, which named
    -- the spots a second time in a different order and could not say which were assigned.
    for i, d in ipairs(self.spotDefs) do
        local col = (i - 1) % SPOT_COLS
        local rowi = math.floor((i - 1) / SPOT_COLS)
        local rx = x + col * (colW + gap)
        local ry = y + rowi * pitch
        if rowi % 2 == 1 then U.Fill(self, rx, ry, colW, rowH, U.C.rowAlt) end
        local set = spots[d.key] ~= nil
        local act = set and BanditsNPC.T("UI_BN_SpotSet", "Set \195\151")
                        or BanditsNPC.T("UI_BN_SpotAssign", "Assign")
        local aw = tm:MeasureStringX(SMALL_FONT, act)

        local label = U.Fit(d.label, colW - aw - U.S(30))
        self:drawText(label, rx + U.S(4), ry + U.S(3),
                      U.C.btnText.r, U.C.btnText.g, U.C.btnText.b, 1, SMALL_FONT)
        -- The plus is registered FIRST so it wins the overlap with the row behind it:
        -- hitAt takes the earliest match.
        if dbg and d.need then
            U.DebugPlus(self, rx + U.S(8) + tm:MeasureStringX(SMALL_FONT, label), ry + U.S(3),
                        hits, "dbgspot:" .. d.key,
                        BanditsNPC.T("UI_BN_Tooltip_DbgSendNow", "[DEBUG] send them there now"))
        end
        -- The row itself is the control: assigned rows read "Set" and clear on click,
        -- unassigned ones read "Assign" in muted text and start the tile picker.
        local c = set and U.C.activeText or U.C.muted
        self:drawText(act, rx + colW - U.S(4) - aw, ry + U.S(3), c.r, c.g, c.b, 1, SMALL_FONT)
        U.Hit(hits, rx, ry, colW, rowH, "spot:" .. d.key,
              set and BanditsNPC.T("UI_BN_Tooltip_ClearSpot", "Click to clear this spot.") or d.tip)
    end
    -- Trailing air under the last row (author ask): the grid ended flush against whatever
    -- came next.
    return y + math.ceil(#self.spotDefs / SPOT_COLS) * pitch + U.S(8)
end

-- The schedule block. Three rows over BanditsNPC.Schedule's existing model -- this reads and
-- writes brain.schedule through Schedule.SetEnabled / SetType / SetLocation and adds no
-- state of its own, so the AI hook that applies the active block (Schedule.Apply, called
-- from the movement programs) already works and needed no change.
function BanditsNPCDialogWindow:drawScheduleBlock(x, y, w, brain)
    local U = BanditsNPC.UI
    local hits = self._hits
    local gap = U.S(U.M.gap)
    local btnH = U.S(U.M.btnH)
    local pad = U.S(8)

    local hour, minute = 0, 0
    pcall(function() hour = getGameTime():getHour(); minute = getGameTime():getMinutes() end)
    local nowBlock = BanditsNPC.Schedule.BlockForHour(hour)
    -- READ ONLY. This used to call Schedule.Ensure, which writes brain.schedule -- a model
    -- mutation running sixty times a second from inside render, on a table that in
    -- multiplayer is also being replaced by syncs. Ensure is called once from refresh()
    -- instead, and this falls back to an empty view if it somehow has not been.
    local sch = brain.schedule or { enabled = false, blocks = {} }
    local blocks = sch.blocks or {}

    y = U.Header(self, x, y, w, BanditsNPC.T("UI_BN_DailySchedule", "Daily schedule"),
                 string.format("%02d:%02d", math.floor(hour), math.floor(minute)))

    -- Tall enough for the two-line time label ("Morning" over "6am-2pm"); the controls are
    -- centred against it. One line would have to be truncated -- BlockLabel is a single
    -- "Morning (6am-2pm)" string and it does not fit a column narrow enough to leave the
    -- activity and location buttons usable widths.
    local rowH = math.max(btnH, 2 * FONT_HGT) + U.S(8)
    local boxH = pad + btnH + U.S(6) + 3 * rowH + pad
    U.Fill(self, x, y, w, boxH, { r = 0, g = 0, b = 0, a = 0.25 })
    U.Border(self, x, y, w, boxH, U.C.hairline)

    local iy = y + pad
    local togW = U.S(110)
    U.Button(self, x + pad, iy, togW,
        sch.enabled and BanditsNPC.T("UI_BN_ScheduleOn", "Schedule: on")
                     or BanditsNPC.T("UI_BN_ScheduleOff", "Schedule: off"),
        sch.enabled and "active" or nil, hits, "schedOn")
    self:drawText(BanditsNPC.T("UI_BN_ScheduleHint", "Blocks run when no manual order is active"),
                  x + pad + togW + gap, iy + U.S(4),
                  U.C.muted.r, U.C.muted.g, U.C.muted.b, 1, SMALL_FONT)
    iy = iy + btnH + U.S(6)

    -- Time label column, then the activity button, then the location button. Widths are
    -- fixed rather than fitted so the three rows line up as a table.
    local labW = U.S(84)
    local ctlW = math.floor((w - pad * 2 - labW - gap * 2) / 2)
    for i = 1, 3 do
        local b = blocks[i] or {}
        local t = b.type or "follow"
        local ry = iy + (i - 1) * rowH
        -- The block containing the current hour is lit, so "which one is running" is
        -- answerable at a glance instead of by reading the clock and doing the arithmetic.
        if i == nowBlock then
            U.Fill(self, x + 1, ry, w - 2, rowH, { r = 0.70, g = 0.35, b = 0.15, a = 0.22 })
        end
        -- "Morning (6am-2pm)" split over two lines; anything BlockLabel returns without
        -- the bracketed range just draws as one.
        local full = BanditsNPC.Schedule.BlockLabel(i) or ""
        local n1, n2 = string.match(full, "^(.-)%s*%((.-)%)$")
        local nameC = sch.enabled and U.C.btnText or U.C.disText
        self:drawText(U.Fit(n1 or full, labW - U.S(4)), x + pad, ry + U.S(3),
                      nameC.r, nameC.g, nameC.b, 1, SMALL_FONT)
        if n2 then
            self:drawText(U.Fit(n2, labW - U.S(4)), x + pad, ry + U.S(3) + FONT_HGT,
                          U.C.muted.r, U.C.muted.g, U.C.muted.b, 1, SMALL_FONT)
        end

        -- Controls centred against the two-line label rather than sitting on its first line.
        local cy = ry + math.floor((rowH - btnH) / 2)
        local ax = x + pad + labW
        local tLabel = BanditsNPC.T("UI_BN_Type_" .. t, t)
        -- "dim", not "disabled" -- the brief says an off schedule greys its rows but keeps
        -- them EDITABLE, and shipping them disabled made them unclickable, so the only way
        -- to set a block up was to switch the schedule on first and back off after. That is
        -- the "schedule buttons are not clickable" the author hit.
        -- NOT `sch.enabled and nil or "dim"`. That is Lua's oldest trap: with enabled TRUE
        -- it evaluates `true and nil` -> nil, then `nil or "dim"` -> "dim", so the ternary
        -- always yields the second branch and the rows were permanently greyed no matter
        -- what the toggle said. An `and nil` arm can never work; this has to be an if.
        local actState = nil
        if not sch.enabled then actState = "dim" end
        U.Button(self, ax, cy, ctlW, tLabel, actState, hits, "schedAct:" .. i)

        -- The location control is only meaningful for the activities that take a tile.
        -- Follow never does; sleep uses her Bed spot from the routine list below, which is
        -- a different control, so saying so is more useful than a dead "Set location".
        local lx = ax + ctlW + gap
        local lLabel, lState
        local hasBase = false
        pcall(function()
            local site = BanditsNPC.Base and BanditsNPC.Base.SiteFor(self.zombie)
            hasBase = (site and BanditsNPC.Base.Zone(site)) and true or false
        end)
        if t == "follow" then
            lLabel, lState = BanditsNPC.T("UI_BN_NotNeeded", "\226\128\148 not needed"), "disabled"
        elseif t == "sleep" then
            -- Both routes count, and the panel has to say which one it found or "set a bed"
            -- is a lie to anyone whose beacon already scanned one.
            local ownBed = brain.spots and brain.spots.bed
            local baseBed = BanditsNPC.Base and BanditsNPC.Base.Has(brain, "bed", self.zombie)
            if ownBed then lLabel = BanditsNPC.T("UI_BN_UsesBedSpot", "Uses their Bed spot")
            elseif baseBed then lLabel = BanditsNPC.T("UI_BN_UsesBaseBed", "A bed in your base")
            else lLabel = BanditsNPC.T("UI_BN_NeedBedSpot", "! Set a Bed spot first") end
            lState = "disabled"
        elseif t == "relax" then
            -- RELAX FOLLOWS THE BEACON (author ask). With a base area drawn, NPCRelax
            -- settles her somewhere inside it on its own -- Interact.OrderRelax has always
            -- worked that way -- so asking for a tile is asking a question that already has
            -- an answer. Only a companion with no base gets the tile picker.
            if hasBase and not b.pos then
                lLabel, lState = BanditsNPC.T("UI_BN_UsesYourBase", "Anywhere in your base"), "disabled"
            elseif b.pos then
                lLabel = string.format("%d, %d", b.pos.x, b.pos.y)
                lState = (not sch.enabled) and "dim" or nil
            else
                lLabel = BanditsNPC.T("UI_BN_NotSet", "Not set")
                lState = (not sch.enabled) and "dim" or nil
            end
        elseif t == "guard" then
            lLabel = (b.a and b.b) and BanditsNPC.T("UI_BN_TwoPointsSet", "2 points set")
                                   or BanditsNPC.T("UI_BN_NotSet", "Not set")
            lState = (not sch.enabled) and "dim" or nil
        else
            lLabel = b.pos and string.format("%d, %d", b.pos.x, b.pos.y)
                            or BanditsNPC.T("UI_BN_NotSet", "Not set")
            lState = (not sch.enabled) and "dim" or nil
        end
        U.Button(self, lx, cy, ctlW, U.Fit(lLabel, ctlW - U.S(8)), lState, hits, "schedLoc:" .. i)
    end
    return y + boxH
end

-- ===========================================================================
-- JOB TAB (v0.77.2)
-- ===========================================================================
--
-- A station banner across the top with Build and Select INSIDE it -- the brief's point,
-- and a good one: the three workstation buttons used to sit in a detached column on the
-- right with nothing explaining why you would press any of them. Then the recipe list on
-- the left and the selected recipe's requirements on the right.
function BanditsNPCDialogWindow:drawJobTab(x, y, w, brain, recruited)
    local U = BanditsNPC.UI
    local sgap = U.S(U.M.sectionGap)
    local tm = getTextManager()

    if not recruited then
        self:drawText(BanditsNPC.T("UI_BN_RecruitForWork", "Recruit this survivor to put them to work."),
                      x, y, 0.8, 0.8, 0.8, 1, SMALL_FONT)
        return
    end

    -- ---- station banner -------------------------------------------------
    local st = self.prodStationType
    local bannerH = BTN_H + U.S(10)
    if st then
        -- same memo key refresh() populated, so on the common path this is a table lookup
        -- rather than a 289-square scan
        local station = self:memo("station", function()
            return BanditsNPC.Production.FindStation(self.zombie, st)
        end)
        local label = BanditsNPC.Production.StationLabel[st] or st
        local msg, warn
        if station then
            local tier = 1
            pcall(function() tier = station:getModData().npcWorkstationTier or 1 end)
            local needsPower = BanditsNPC.Stations and BanditsNPC.Stations.TierNeedsPower
                               and BanditsNPC.Stations.TierNeedsPower(st, tier)
            local unpowered = needsPower and BanditsNPC.Stations.HasPower
                              and not BanditsNPC.Stations.HasPower(station:getSquare())
            if unpowered then
                msg = BanditsNPC.TF("UI_BN_Banner_NoPower", "%1 (tier %2) -- NO POWER", label, tier)
                warn = true
            else
                msg = BanditsNPC.TF("UI_BN_Banner_Ready", "%1 (tier %2) -- ready", label, tier)
            end
        else
            msg = BanditsNPC.TF("UI_BN_Banner_None", "No %1 nearby", label)
            warn = true
        end
        -- A warning tint only when something is actually wrong; a permanently amber banner
        -- stops meaning anything.
        U.Fill(self, x, y, w, bannerH, warn and { r = 0.55, g = 0.42, b = 0.12, a = 0.22 }
                                            or { r = 1, g = 1, b = 1, a = 0.05 })
        U.Border(self, x, y, w, bannerH, warn and U.C.warn or U.C.hairline)
        local mc = warn and U.C.warn or U.C.btnText
        self:drawText(U.Fit(msg, w - U.S(200)), x + U.S(8), y + U.S(8), mc.r, mc.g, mc.b, 1, SMALL_FONT)
    end
    local top = y + bannerH + U.S(4)

    if not st then
        self:drawText(BanditsNPC.T("UI_BN_NoProdSkill", "This survivor has no production skill."),
                      x, y, 0.8, 0.8, 0.8, 1, SMALL_FONT)
        return
    end

    -- ---- two panels, two captions, ONE LINE (author ask) -----------------
    -- "Can produce" over the list and the recipe's name over the requirements, on the same
    -- baseline, with both boxes starting and ending together underneath. The right caption
    -- used to be inside its own box, which put the two panels a header out of step.
    local rx2, rw2 = self.jobRightX, self.jobRightW
    local r = self.prodSelected
    U.Header(self, x, top, self.jobColW, BanditsNPC.T("UI_BN_CanProduce", "Can produce"))
    U.Header(self, rx2, top, rw2,
             r and U.Fit(BanditsNPC.Production.RecipeName(r.name), rw2)
               or BanditsNPC.T("UI_BN_Requirements", "Requirements"))

    local boxTop = self.recListY
    local boxBot = self.jobBottom
    U.Container(self, rx2 - U.S(6), boxTop, rw2 + U.S(12), boxBot - boxTop)
    local ry2 = boxTop + U.S(8)
    local inner = rw2

    local haveT = self.prodNearbyTier or 0
    if not r then
        self:drawText(BanditsNPC.T("UI_BN_PickRecipeHint", "Pick a recipe on the left."),
                      rx2, ry2, U.C.muted.r, U.C.muted.g, U.C.muted.b, 1, SMALL_FONT)
    else
        self:drawText(BanditsNPC.T("UI_BN_Needs", "Needs"), rx2, ry2,
                      U.C.label.r, U.C.label.g, U.C.label.b, 1, SMALL_FONT)
        ry2 = ry2 + FONT_HGT + U.S(3)
        -- have / need per ingredient, from the SAME count HasInputs and Start use, so the
        -- panel and the check that actually runs can never disagree.
        -- Each ingredient gets ITS OWN ICON, the same as the recipe rows opposite: a
        -- shopping list of bare words is the slowest possible way to check a bag against it.
        local ICON = BanditsNPC.ItemList.ICON
        local rowH = math.max(ICON, FONT_HGT) + U.S(3)
        for _, ing in ipairs(BanditsNPC.Production.InputStatus(getSpecificPlayer(0), r, self.prodQty or 1)) do
            local s = ing.have .. " / " .. ing.need
            local sw = tm:MeasureStringX(SMALL_FONT, s)
            local ty = ry2 + math.floor((rowH - FONT_HGT) / 2)
            local nx2 = rx2
            local tex = self:iconTex(ing.type)
            if tex then
                pcall(function()
                    self:drawTextureScaled(tex, rx2, ry2 + math.floor((rowH - ICON) / 2),
                                           ICON, ICON, ing.ok and 1 or 0.6, 1, 1, 1)
                end)
                nx2 = rx2 + ICON + U.S(4)
            end
            -- The name is CLIPPED to the space the count is not using. Unclipped it ran
            -- straight under the numbers and the whole panel became unreadable.
            self:drawText(U.Fit(itemName(ing.type), rx2 + inner - sw - U.S(8) - nx2), nx2, ty,
                          U.C.btnText.r, U.C.btnText.g, U.C.btnText.b, 1, SMALL_FONT)
            local c = ing.ok and U.C.good or U.C.warn
            self:drawText(s, rx2 + inner - sw, ty, c.r, c.g, c.b, 1, SMALL_FONT)
            ry2 = ry2 + rowH
        end
        -- The tier gate, in the same have / need shape. It is the one requirement the
        -- player cannot fix by looting, so saying it plainly is the whole point of listing
        -- gated recipes at all.
        local needT = r.minTier or 1
        if needT > 1 or haveT > 0 then
            local s = (haveT > 0 and haveT or "-") .. " / " .. needT
            local c = (haveT > 0 and haveT >= needT) and U.C.good or U.C.warn
            local sw = tm:MeasureStringX(SMALL_FONT, s)
            self:drawText(U.Fit(BanditsNPC.T("UI_BN_StationTier", "Station tier"), inner - sw - U.S(10)),
                          rx2, ry2, U.C.btnText.r, U.C.btnText.g, U.C.btnText.b, 1, SMALL_FONT)
            self:drawText(s, rx2 + inner - sw, ry2, c.r, c.g, c.b, 1, SMALL_FONT)
            ry2 = ry2 + FONT_HGT + U.S(2)
        end
        ry2 = ry2 + U.S(6)

        -- Makes / time, as one right-aligned pair each, same grid as Needs above.
        local function pair(label, value, c)
            local vw = tm:MeasureStringX(SMALL_FONT, value)
            self:drawText(U.Fit(label, inner - vw - U.S(10)), rx2, ry2,
                          U.C.label.r, U.C.label.g, U.C.label.b, 1, SMALL_FONT)
            c = c or U.C.btnText
            self:drawText(value, rx2 + inner - vw, ry2, c.r, c.g, c.b, 1, SMALL_FONT)
            ry2 = ry2 + FONT_HGT + U.S(2)
        end
        pair(BanditsNPC.T("UI_BN_Makes", "Makes"),
             U.Fit(((r.outCount or 1) * (self.prodQty or 1)) .. "\195\151 " .. itemName(r.output), math.floor(inner * 0.62)))
        -- Hours are what the job actually schedules (Start multiplies by TimeMult and the
        -- tier's speed), so the estimate here is read off the same three factors.
        local speed = (BanditsNPC.Production.TierSpeed and BanditsNPC.Production.TierSpeed[math.max(1, haveT)]) or 1
        local hrs = (r.time or 1) * (self.prodQty or 1) * (BanditsNPC.Production.TimeMult or 1) * speed
        pair(BanditsNPC.T("UI_BN_TimeEach", "Time"),
             BanditsNPC.TF("UI_BN_AboutHours", "~%1 h", string.format("%.1f", hrs)), U.C.muted)
    end

    -- ---- skills & stats, at the FOOT of the container --------------------
    -- The mock-up puts perk BARS in the rail. The rail is fixed by design (that is the fix
    -- for tabs stranding the player) and D.PerkLevels has nothing to draw anyway: a
    -- companion is an IsoZombie, getPerkLevel exists but nothing ever sets a perk, so every
    -- bar would read zero. What IS real goes here, and the gap says so.
    --
    -- Bottom-anchored inside the box so it cannot collide with a long ingredient list.
    local skills = {}
    if brain.exp then
        for _, id in pairs(brain.exp) do
            if id and id > 0 and self.expertiseNames[id] then table.insert(skills, self.expertiseNames[id]) end
        end
    end
    -- Above the stepper, which now sits inside this container too.
    local sy = self.qtyY - U.S(8) - FONT_HGT * 2 - U.S(2)
    if sy > ry2 + U.S(6) then
        U.Fill(self, rx2, sy - U.S(6), inner, 1, U.C.hairline)
        self:drawText(U.Fit(BanditsNPC.TF("UI_BN_ExpertiseList", "Expertise: %1",
                (#skills > 0 and table.concat(skills, ", ") or BanditsNPC.T("UI_BN_None", "None"))), inner),
            rx2, sy, U.C.muted.r, U.C.muted.g, U.C.muted.b, 1, SMALL_FONT)
        self:drawText(U.Fit(BanditsNPC.TF("UI_BN_HealthMax", "Health (max): %1",
                (brain.health and string.format("%.1f", brain.health) or "-")), inner),
            rx2, sy + FONT_HGT + U.S(2), U.C.muted.r, U.C.muted.g, U.C.muted.b, 1, SMALL_FONT)
    end

    -- ---- the stepper row -------------------------------------------------
    -- The number sits BETWEEN the two step buttons; "Qty:" used to be drawn on top of it.
    local qtyTxt = tostring(self.prodQty or 1)
    self:drawText(qtyTxt, self.qtyNumX - math.floor(tm:MeasureStringX(SMALL_FONT, qtyTxt) / 2),
                  self.qtyY + U.S(5), 1, 1, 1, 1, SMALL_FONT)
    if r then
        local mx = BanditsNPC.TF("UI_BN_MaxN", "max %1", self.prodMaxQty or 0)
        local mw = tm:MeasureStringX(SMALL_FONT, mx)
        self:drawText(mx, rx2 + rw2 - mw, self.qtyY + U.S(5),
                      U.C.muted.r, U.C.muted.g, U.C.muted.b, 1, SMALL_FONT)
    end

    -- ---- status / messages, on the line under the recipe list ------------
    local pstatus = BanditsNPC.Production.StatusText(brain)
    local msgY = self.jobBottom + U.S(6)
    if pstatus then
        self:drawText(U.Fit(pstatus, self.jobColW), x, msgY, U.C.good.r, U.C.good.g, U.C.good.b, 1, SMALL_FONT)
    elseif self.prodMsg then
        self:drawText(U.Fit(self.prodMsg, self.jobColW), x, msgY,
                      U.C.warn.r, U.C.warn.g, U.C.warn.b, 1, SMALL_FONT)
    end
end

-- ===========================================================================
-- GEAR TAB (v0.77.2)
-- ===========================================================================
--
-- Doll and protection on the left, her carried items and yours side by side. The two lists
-- are ISScrollingListBoxes and draw themselves; this draws their headers, their capacity
-- bars and the outfit doll.
function BanditsNPCDialogWindow:drawGearTab(x, y, w, brain)
    local U = BanditsNPC.UI
    local D = BanditsNPC.ProfileData
    local tm = getTextManager()

    -- ---- the doll -------------------------------------------------------
    U.Header(self, self.gearDollX, y, U.S(134), BanditsNPC.T("UI_BN_Worn", "Worn"))
    local dollY = y + FONT_HGT + U.S(6)
    self:drawEquippedPanel(self.gearDollX, dollY)

    -- ---- protection, off what she is actually wearing --------------------
    local py = dollY + U.S(240) + U.S(4)
    py = U.Header(self, self.gearDollX, py, U.S(134), BanditsNPC.T("UI_BN_Protection", "Protection"))
    local prot = D.Protection(self.zombie, brain)
    local function protRow(label, value, colour)
        self:drawText(label, self.gearDollX, py, U.C.label.r, U.C.label.g, U.C.label.b, 1, SMALL_FONT)
        local vw = tm:MeasureStringX(SMALL_FONT, value)
        local c = colour or U.C.btnText
        self:drawText(value, self.gearDollX + U.S(134) - vw, py, c.r, c.g, c.b, 1, SMALL_FONT)
        py = py + FONT_HGT + U.S(2)
    end
    if prot.any then
        -- Bite and scratch are the BEST single garment's cover, not a sum: two 30% items on
        -- different limbs are not 60% protection anywhere. See ProfileData.Protection.
        local function pct(v) return tostring(math.floor((v or 0) * 100 + 0.5)) .. "%" end
        local function band(v) return (v >= 0.5) and U.C.good or ((v >= 0.2) and U.C.warn or U.C.dangerText) end
        protRow(BanditsNPC.T("UI_BN_Prot_Bite", "Bite"), pct(prot.bite), band(prot.bite or 0))
        protRow(BanditsNPC.T("UI_BN_Prot_Scratch", "Scratch"), pct(prot.scratch), band(prot.scratch or 0))
        protRow(BanditsNPC.T("UI_BN_Prot_Insul", "Insul."), D.InsulWord(prot.insul))
        protRow(BanditsNPC.T("UI_BN_Prot_Weight", "Weight"), string.format("%.1f", prot.weight or 0))
    else
        self:drawText(BanditsNPC.T("UI_BN_Prot_Nothing", "wearing nothing"), self.gearDollX, py,
                      U.C.muted.r, U.C.muted.g, U.C.muted.b, 1, SMALL_FONT)
        py = py + FONT_HGT + U.S(2)
    end

    -- ---- the two carried columns ----------------------------------------
    -- A BAR, not just the numbers. "3.1 / 8.0" needs reading and comparing; a bar that
    -- ambers near the cap answers "is she about to be overloaded" at a glance, which is the
    -- only question anyone asks of that figure.
    local function column(cx, title, inv)
        local cw, mw = 0, 0
        pcall(function() cw = inv:getCapacityWeight(); mw = inv:getMaxWeight() end)
        local txt = string.format("%.1f / %.1f", cw, mw)
        self:drawText(U.Fit(title, self.gearColW - tm:MeasureStringX(SMALL_FONT, txt) - U.S(8)),
                      cx, y, 1, 1, 1, 1, SMALL_FONT)
        local frac = (mw > 0) and (cw / mw) or 0
        local tw = tm:MeasureStringX(SMALL_FONT, txt)
        local c = (frac >= 1) and U.C.dangerText or ((frac >= 0.85) and U.C.warn or U.C.muted)
        self:drawText(txt, cx + self.gearColW - tw, y, c.r, c.g, c.b, 1, SMALL_FONT)
        local barC = (frac >= 1) and { r = 0.78, g = 0.34, b = 0.30, a = 1 }
                     or ((frac >= 0.85) and U.C.warn or U.C.btnBorder)
        U.Track(self, cx, y + FONT_HGT + U.S(2), self.gearColW, frac, nil, barC)
    end
    column(self.invX, brain.fullname or BanditsNPC.T("UI_BN_Companion", "Companion"),
           self.zombie:getInventory())
    local player = getSpecificPlayer(0)
    if player then
        column(self.playerX, BanditsNPC.T("UI_BN_YouCarry", "You carry"), player:getInventory())
    end

    -- An empty list must SAY it is empty; blank space is indistinguishable from broken.
    local function emptyNote(lb, cx, text)
        if lb and lb.items and #lb.items == 0 then
            local tw = tm:MeasureStringX(SMALL_FONT, text)
            self:drawText(text, cx + math.floor((self.gearColW - tw) / 2),
                          self.gearListTop + math.floor(self.gearListH / 2),
                          U.C.muted.r, U.C.muted.g, U.C.muted.b, 1, SMALL_FONT)
        end
    end
    emptyNote(self.invList, self.invX, BanditsNPC.T("UI_BN_CarryNothing", "Carrying nothing"))
    emptyNote(self.playerList, self.playerX, BanditsNPC.T("UI_BN_CarryNothing", "Carrying nothing"))

    -- The message goes in the DOLL column, under Protection. Across the full width it ran
    -- straight through the Wear / Equip / Make wear row sitting on the same line; this is
    -- the one column with empty space below its content.
    if self.itemsMsg then
        self:drawTextWrapped(self.itemsMsg, self.gearDollX, py + U.S(6), U.S(138))
    end
end

-- ===========================================================================
-- PRERENDER: THE FRAME AND THE RAIL (v0.77.6)
-- ===========================================================================
--
-- THIS IS WHERE BACKGROUNDS BELONG, and getting it wrong is what made the author's whole
-- window look "greyed out / almost unrecognisable". PZ renders a window's CHILDREN between
-- prerender() and render(), which is exactly why every vanilla panel paints its background
-- in prerender (ISPanel:prerender is four lines and does nothing else). v0.77.1 painted the
-- opaque panel background at the top of RENDER instead -- so an 87%-black rectangle went
-- down over every ISButton, both list boxes and the combo, every frame. The tell was in the
-- author's own report: the outfit doll was lit and the inventory list beside it was dark,
-- and the doll is drawn by this file while the list is a child.
--
-- So: frame and rail here, content foreground in render(), and the joypad marker last of
-- all -- it was in prerender too, which drew the selection ring UNDER the button it marks.
-- FIX(TASK-020): re-lay-out on a drag. Copied from BanditsNPCBeaconWindow:352-368,
-- which is the pattern that already works in this mod.
function BanditsNPCDialogWindow:onResize()
    ISCollapsableWindow.onResize(self)
    self:layout()
end

function BanditsNPCDialogWindow:prerender()
    ISCollapsableWindow.prerender(self)
    -- BELT AND BRACES, and BeaconWindow carries the same note for the same reason: it is
    -- not obvious from the Lua side that dragging ISResizeWidget always reaches onResize
    -- -- the widget calls setWidth/setHeight, which push to the java object, and the
    -- callback comes back from there. A stale layout after a drag would leave the footer
    -- buttons floating in the middle of the window, so the size is also checked once a
    -- frame here, which cannot miss whatever route the resize took.
    if self.laidOutW ~= self.width or self.laidOutH ~= self.height then
        self:layout()
    end
    local brain = self.brain
    if not brain then return end
    local U = BanditsNPC.UI

    -- Rebuilt once per frame, before anything registers into it. Stale rectangles are how a
    -- drawn UI lets you press a control that is no longer on screen.
    self._hits = {}

    local top = self:titleBarHeight()
    local RAIL = self.railW or U.S(U.M.rail)
    U.Fill(self, 1, top, self.width - 2, self.height - top - 1, U.C.panelBg)

    -- THREE CONTAINERS, as the mock-ups draw them: the rail, the content area and the
    -- footer are each their own bordered box rather than three regions of one field.
    local fx, fw = U.S(6), self.width - U.S(12)
    local footTop = self.footerY - U.S(6)
    U.Container(self, fx, top + U.S(4), RAIL - U.S(10), footTop - top - U.S(10))
    U.Container(self, RAIL + U.S(2), top + U.S(4), self.width - RAIL - U.S(8), footTop - top - U.S(10))
    U.Container(self, fx, footTop, fw, self.height - footTop - U.S(6))

    self:drawRail(brain, RAIL)
end

-- The rail: portrait, name, subtitle, vitals, then the tab list. Drawn by the window and
-- never by a tab, which is the fix for the Items tab stranding the player.
function BanditsNPCDialogWindow:drawRail(brain, RAIL)
    local U = BanditsNPC.UI
    local recruited = self:isRecruited()
    local rx, rw = U.S(14), RAIL - U.S(28)

    -- THE PORTRAIT LIVES IN THE RAIL (v0.77.1). The first build drew an empty frame here
    -- and rendered her in the Profile content instead -- "the corner top left box is not
    -- showing the npc", and quite right. There is only ONE 3D portrait renderer, so it
    -- cannot be in both places; the rail is the better of the two because the rail is
    -- present on every tab, so you can see who you are giving orders to while you do it.
    local pw = U.S(52)
    U.Fill(self, rx, self.railNameY, pw, pw, { r = 0, g = 0, b = 0, a = 0.45 })
    local gotPortrait = false
    if BanditsNPC.Portrait then
        -- Inset by one pixel: the model is a CHILD and draws after this whole rail, so at
        -- the full 52 it would cover the frame drawn round it two lines below.
        gotPortrait = BanditsNPC.Portrait.Show(self, self.zombie, rx + 1, self.railNameY + 1, pw - 2, pw - 2)
    end
    if not gotPortrait then
        -- The 3D render is a sandbox option and can be off. Say so quietly rather than
        -- leaving a black hole the player reads as a broken panel.
        local ph = BanditsNPC.T("UI_BN_NoPortrait", "no portrait")
        self:drawText(ph, rx + U.S(6), self.railNameY + U.S(14),
                      U.C.disText.r, U.C.disText.g, U.C.disText.b, 1, SMALL_FONT)
    end
    U.Border(self, rx, self.railNameY, pw, pw, U.C.hairline)

    local nx = rx + pw + U.S(8)
    self:drawText(brain.fullname or "?", nx, self.railNameY, 1, 1, 1, 1, MED_FONT)

    -- Subtitle over two lines: what she is, then how she is set up. One long line was
    -- being clipped by the rail.
    local sex = brain.female and BanditsNPC.T("UI_BN_Female", "Female") or BanditsNPC.T("UI_BN_Male", "Male")
    local line1, line2
    if recruited and brain.downed then
        line1 = BanditsNPC.T("UI_BN_Status_Downed", "KNOCKED DOWN")
        line2 = BanditsNPC.T("UI_BN_HelpUpHint", "use Help up below")
    elseif recruited then
        local stanceName = ({
            passive    = BanditsNPC.T("UI_BN_Stance_Passive", "Passive"),
            defensive  = BanditsNPC.T("UI_BN_Stance_Defensive", "Defensive"),
            aggressive = BanditsNPC.T("UI_BN_Stance_Aggressive", "Aggressive"),
        })[brain.npcStance or "defensive"] or (brain.npcStance or "defensive")
        -- The profession name comes from the id->name table this window already builds
        -- (buildExpertiseNames); there is no module-level accessor and inventing one here
        -- would be a second source of truth for the same string.
        local exp = (self.expertiseNames and brain.exp and self.expertiseNames[brain.exp]) or sex
        line1 = exp .. "  " .. (BanditsNPC.Affinity and BanditsNPC.Affinity.GetTierLabel(brain) or "")
        line2 = programLabel(brain.program and brain.program.name) .. "  " .. stanceName
    else
        line1 = sex
        line2 = BanditsNPC.T("UI_BN_Status_NotRecruited", "Not recruited")
    end
    -- FIT, DO NOT OVERFLOW (v0.77.1). drawText does not clip, so "Following  Defensive"
    -- ran under the rail divider and touched the content -- the missing padding the author
    -- spotted. The available width is what is left beside the portrait, less a margin.
    local subW = RAIL - (nx - 0) - U.S(8)
    self:drawText(U.Fit(line1, subW), nx, self.railStatusY,
                  U.C.label.r, U.C.label.g, U.C.label.b, 1, SMALL_FONT)
    self:drawText(U.Fit(line2, subW), nx, self.railStatusY + FONT_HGT + 1,
                  U.C.muted.r, U.C.muted.g, U.C.muted.b, 1, SMALL_FONT)

    local y = self.railNameY + pw + U.S(8)

    local cur = 0; pcall(function() cur = self.zombie:getHealth() end)
    local maxh = (brain.health and brain.health > 0) and brain.health or 1
    if cur > maxh and cur <= 100 then maxh = 100 end
    local hpPct = math.max(0, math.min(100, (cur / maxh) * 100))

    if recruited then
        y = U.Header(self, rx, y, rw, BanditsNPC.T("UI_BN_Vitals", "Vitals"))
        local n = (BanditsNPC.Needs and BanditsNPC.Needs.Get(brain)) or {}
        -- In debug each of the four raisable needs grows a green plus, replacing the row of
        -- "+Hunger / +Fatigue / ..." buttons that used to name them a second time on the
        -- Orders tab. The bar it acts on is right there, so the effect is visible in place.
        local dbg = U.DebugOn()
        local barW = dbg and (rw - U.S(12)) or rw
        local function need(key, label, value, rising)
            local ny = U.StatBar(self, rx, y, barW, label, value, 100, rising)
            if dbg and key then
                U.DebugPlus(self, rx + rw - U.S(7), y, self._hits, "dbgneed:" .. key,
                            BanditsNPC.T("UI_BN_Tooltip_DbgRaise", "[DEBUG] raise this need"))
            end
            y = ny
        end
        -- HEALTH FILLS AS IT IS GOOD; THE NEEDS FILL AS PRESSURE RISES. The old header
        -- drew both with the same colour ramp, so a full Hunger bar looked like good news.
        need(nil, BanditsNPC.T("UI_BN_Need_Health", "Health"), hpPct, false)
        need("hunger",  BanditsNPC.T("UI_BN_Need_Hunger", "Hunger"), n.hunger, true)
        need("fatigue", BanditsNPC.T("UI_BN_Need_Fatigue", "Fatigue"), n.fatigue, true)
        need("boredom", BanditsNPC.T("UI_BN_Need_Boredom", "Boredom"), n.boredom, true)
        need("hygiene", BanditsNPC.T("UI_BN_Need_Hygiene", "Hygiene"), n.hygiene, true)
        local affLabel = (BanditsNPC.Affinity and BanditsNPC.Affinity.GetLabel(brain))
                         or BanditsNPC.T("UI_BN_Bond", "Bond")
        local affVal = (BanditsNPC.Affinity and BanditsNPC.Affinity.Get(brain)) or 0
        need(nil, affLabel, affVal, false)
    else
        y = U.Header(self, rx, y, rw, BanditsNPC.T("UI_BN_Vitals", "Vitals"))
        y = U.StatBar(self, rx, y, rw, BanditsNPC.T("UI_BN_Need_Health", "Health"), hpPct, 100, false)
        self:drawText(BanditsNPC.T("UI_BN_RecruitToSeeNeeds", "(recruit to see needs & bond)"),
                      rx, y + U.S(2), U.C.muted.r, U.C.muted.g, U.C.muted.b, 1, SMALL_FONT)
        y = y + FONT_HGT + U.S(4)
    end

    -- Tab list. Positioned here rather than in createChildren because the vitals block
    -- above changes height with recruitment; the buttons follow it.
    --
    -- A FULL SECTION GAP, not a nudge (v0.77.1). Six pixels put "Panels" hard against the
    -- last vitals bar and the two blocks read as one; sectionGap is the same distance every
    -- other block in the mod uses to say "this is a different thing".
    y = y + U.S(U.M.sectionGap)
    y = U.Header(self, rx, y, rw, BanditsNPC.T("UI_BN_Panels", "Panels"))
    local tabX, tabW = U.S(10), RAIL - U.S(22)
    for _, t in ipairs(self.tabOrder or {}) do
        local b = self.tabButtons[t.id]
        if b then
            -- LEFT-ALIGNED (author ask). Centred labels in a 150px column read as a stack of
            -- unrelated words; flush left they read as a list, which is what they are.
            b.titleLeft = true
            -- FIX(TASK-020): **setHeight IS PART OF THIS LINE NOW, AND ITS ABSENCE WAS
            -- THE WHOLE RAIL BUG.** This loop has always owned x, y and width -- it must,
            -- because `y` accumulates from the vitals block above, which changes height
            -- with recruitment and with debug mode, so the tab row genuinely moves from
            -- frame to frame and cannot live in `layout()`. What it never owned was the
            -- HEIGHT, because the buttons used to be constructed with `BTN_H` and nothing
            -- ever changed it. TASK-020 moved every widget to `ISButton:new(0, 0, 0, 0)`
            -- and this was the one widget `layout()` does not touch -- so the height went
            -- to 0 and stayed there. All three symptoms are that single missing value:
            --   * UNCLICKABLE -- the hit rectangle was `width x 0`;
            --   * NO BUTTON CHROME -- `drawRect(0, 0, width, 0)` paints nothing
            --     (refs/pz-lua/.../ISButton.lua:128, :141);
            --   * HIGHLIGHT OFF BY ABOUT A ROW -- ISButton centres its title at
            --     `(self.height / 2) - (textHeight / 2)` (ISButton.lua:252), which at
            --     height 0 draws the LABEL half a font-height ABOVE the button's own y,
            --     while the highlight below still spans a full `BTN_H` from that same y.
            --     The two origins that parted company were not the highlight and the
            --     button rect -- they share `y` -- but the button rect and the button's
            --     own vertical text centring, which is computed from the height.
            -- ALL FOUR COMPONENTS ARE SET HERE SO THERE IS ONE OWNER OF THIS RECTANGLE.
            -- Leaving three here and one at the construction site is exactly the split
            -- that produced this bug; do not put the height back in the constructor.
            b:setX(tabX + U.S(6)); b:setY(y); b:setWidth(tabW - U.S(6)); b:setHeight(BTN_H)
            -- The active tab is marked with an orange left edge and a lifted background --
            -- the vanilla list-selection idiom, and readable without relying on colour. The
            -- marker sits OUTSIDE the button so the flush-left label never touches it.
            if self.section == t.id then
                U.Fill(self, tabX, y, tabW, BTN_H, U.C.activeBg)
                U.Fill(self, tabX, y, U.S(3), BTN_H, U.C.activeBorder)
            end
            y = y + BTN_H + U.S(3)
        end
    end
end

-- ===========================================================================
-- RENDER: THE CONTENT FOREGROUND
-- ===========================================================================
--
-- Everything here draws OVER the children, which is right for text and bars laid around the
-- list boxes and wrong for anything that should sit behind one -- the frame and the rail are
-- in prerender for exactly that reason.
function BanditsNPCDialogWindow:render()
    ISCollapsableWindow.render(self)
    local brain = self.brain
    if not brain then return end
    local U = BanditsNPC.UI
    local recruited = self:isRecruited()

    -- another player's companion: explain why there's no "Ask to join" button
    if self:isOwnedByOther() then
        local owner = (brain.masterName and brain.masterName ~= "" and brain.masterName) or BanditsNPC.T("UI_BN_AnotherPlayer", "another player")
        self:drawText(BanditsNPC.TF("UI_BN_CompanionOf", "Companion of %1", owner),
                      self.rightX, self.footerY + 4, U.C.muted.r, U.C.muted.g, U.C.muted.b, 1, SMALL_FONT)
    end

    -- ===== tab content =====
    local x, y = self.rightX, self.contentY

    if self:shows("story") then
        self:drawProfileTab(x, y, self.width - x - PAD, brain, recruited)
    elseif self:shows("skills") then
        -- Job absorbs Skills AND Work, and this is an if/elseif chain, so the two must be
        -- ONE branch -- exactly the trap that made the quest card unreachable on Profile.
        self:drawJobTab(x, y, self.width - x - PAD, brain, recruited)
    elseif self:shows("orders") then
        if recruited then
            local endY = self:drawOrdersTab(x, y, self.width - x - PAD, brain)
            -- The debug pluses write here when they fire. Under the content, which the tab
            -- now reports the bottom of, rather than at a constant that drifted past the
            -- footer the moment the layout above it changed.
            if self.ordersMsg then
                self:drawText(U.Fit(self.ordersMsg, self.width - x - PAD), x, endY + U.S(6),
                              U.C.warn.r, U.C.warn.g, U.C.warn.b, 1, SMALL_FONT)
            end
        else
            self:drawText(BanditsNPC.T("UI_BN_RecruitForOrders", "Recruit this survivor first to give orders."), x, y, 0.8, 0.8, 0.8, 1, SMALL_FONT)
        end
    elseif self:shows("trade") then
        if recruited then
            self:drawGearTab(x, y, self.width - x - PAD, brain)
        else
            self:drawText(BanditsNPC.T("UI_BN_ManageItemsRecruited", "You can only manage items for a recruited companion."), x, y, 0.8, 0.8, 0.8, 1, SMALL_FONT)
        end
    -- (The quest and prod branches are gone: their content is part of drawProfileTab and
    -- drawJobTab. Neither could ever have drawn once Profile absorbed Quest and Job
    -- absorbed Skills -- this is an if/elseif chain and each tab shows TWO groups, so the
    -- first test matched and the second was unreachable. That is why the quest card and the
    -- whole production readout were invisible in v0.77.0 and v0.77.1.)
    elseif self:shows("anim") then
        local dx = x + 260
        self:drawText(BanditsNPC.T("UI_BN_AnimPlayer", "Animation Player"), dx, y, 1, 1, 1, 1, SMALL_FONT)
        self:drawText(BanditsNPC.TF("UI_BN_AnimSelected", "Selected: %1", (self.selectedAnim or "-")), dx, y + FONT_HGT + 6, 0.9, 0.95, 0.8, 1, SMALL_FONT)
        self:drawTextWrapped(BanditsNPC.T("UI_BN_AnimHelp", "Pick an animation, then Play to choose a tile for them to perform it. Stop ends it early."),
            dx, y + (FONT_HGT + 6) * 2, self.width - dx - PAD)
        if self.animMsg then self:drawText(self.animMsg, dx, y + (FONT_HGT + 6) * 4, 0.95, 0.95, 0.6, 1, SMALL_FONT) end
    end

    -- LAST, over everything: the controller marker, then the hover tooltip.
    --
    -- The marker was in prerender, which drew the selection ring UNDER the button it was
    -- marking -- the same ordering fault as the background. The tooltip has to be last of
    -- all or the tab content draws through it.
    if BanditsNPC.Joypad then BanditsNPC.Joypad.DrawSelection(self) end
    if self:isMouseOver() then
        local h = self:hitAt(self:getMouseX(), self:getMouseY())
        if h and h.tip then U.Tip(self, self:getMouseX(), self:getMouseY(), h.tip) end
    end
end

function BanditsNPCDialogWindow:drawTextWrapped(text, x, y, maxWidth)
    -- breaking is shared (BanditsNPC.WrapLines); this keeps its own contract -- it RETURNS
    -- the y it finished at and advances past the final line, which callers rely on
    local lines = BanditsNPC.WrapLines(text, SMALL_FONT, maxWidth)
    for i = 1, #lines do
        self:drawText(lines[i], x, y, 0.9, 0.9, 0.9, 1, SMALL_FONT)
        y = y + FONT_HGT + 2
    end
    return y
end

-- How tall drawTextWrapped WOULD be. The quest card is a bordered box drawn before its own
-- contents, so it has to know the wrapped height of the quote up front; measuring with the
-- same WrapLines the drawing uses is the only way the two cannot disagree.
function BanditsNPCDialogWindow:measureWrapped(text, maxWidth)
    return #BanditsNPC.WrapLines(text, SMALL_FONT, maxWidth) * (FONT_HGT + 2)
end

function BanditsNPCDialogWindow:new(zombie)
    -- 900x560 per the rework brief: the rail takes 170 of the width, so the old 800 left
    -- the content column narrower than it was before the restructure.
    local w, h = sc(900), sc(560)
    local x = (getCore():getScreenWidth() - w) / 2
    local y = (getCore():getScreenHeight() - h) / 2
    local o = ISCollapsableWindow.new(self, x, y, w, h)
    o.zombie = zombie
    o.brain = BanditBrain.Get(zombie)
    o.myPid = BanditUtils.GetCharacterID(getSpecificPlayer(0))
    o.title = (o.brain and o.brain.fullname) or BanditsNPC.T("UI_BN_Survivor", "Survivor")
    -- ===========================================================================
    -- FIX(TASK-020): RESIZEABLE, WITH A FLOOR UNDER IT.
    -- ===========================================================================
    --
    -- `ISResizeWidget:resize` clamps against these two fields before it does anything
    -- else (refs/pz-lua/.../ISResizeWidget.lua:12-23), so a minimum size is two
    -- assignments rather than any code of ours.
    --
    -- **WIDTH sc(760). The Gear tab binds it and nothing else comes close.** That tab is
    -- rightX(184) + doll(142) + 10 + two item lists + arrows(34) + 12 + 14; holding the
    -- lists at a usable ~150 each needs 696, and 760 leaves margin. At 760 the Job tab is
    -- comfortable -- jobColW ~= 311, and its three 76px workstation buttons start at 582,
    -- well right of jobRightX ~= 505.
    --
    -- **HEIGHT sc(520) AGAINST AN OPENING HEIGHT OF sc(560), AND THE SMALL DIFFERENCE IS
    -- THE WHOLE DESIGN.** The tab content is DRAWN, not children, so it cannot scroll the
    -- way BeaconWindow's body does -- on a window shorter than its tallest tab it would
    -- simply paint over the footer. Setting the floor at what the tallest tab needs
    -- removes that failure mode at no cost, and the alternative (teaching four drawn tabs
    -- to scroll) was considered and explicitly rejected as disproportionate.
    --
    -- WHAT SETS THE FLOOR, so a future change knows what it is spending: the RAIL, not
    -- the tabs. Worst case is recruited with debug on -- portrait sc(52) + sc(8), the
    -- Vitals header (FONT_HGT + 4), SIX stat bars (FONT_HGT + 2 each: health, four needs,
    -- bond), a section gap sc(11), the Panels header, then FIVE tab buttons
    -- (BTN_H + sc(3) each). That totals roughly 13 x FONT_HGT + 146 below `top`, and the
    -- window must also carry its title bar, the sc(12) inset, the footer row, its
    -- container and now the reserved resize strip -- about 424px at scale 1, against an
    -- opening height of 560. **If a stat bar, a need or a tab is ever ADDED, this number
    -- goes up.**
    --
    -- **sc(540) IS DELIBERATELY CONSERVATIVE AND IS THE ONE NUMBER HERE THAT IS NOT
    -- DERIVED.** The rail is measured above; the four tabs are not, because Orders (three
    -- strips, a schedule block and eight routine-spot rows) and Gear (a ten-slot doll) are
    -- drawn text whose height cannot be read off the source as cleanly. 540 gives 20px of
    -- shrink, which is safe by inspection. **Lower it only after opening all four tabs at
    -- the floor and confirming nothing paints over the footer** -- that is a one-line
    -- change, and it is the right way round: an overflow ships as a visual defect to
    -- 27,000 people, a floor 20px too high ships as nothing at all.
    o.resizable = true
    o.minimumWidth  = sc(760)
    o.minimumHeight = sc(540)
    o.section = "profile"
    return o
end

function BanditsNPCDialogWindow.OpenFor(zombie)
    bnApplyScale()   -- pick up the current UIScale sandbox option before laying out
    -- rebuild her model from the CURRENT visual state: the 3D portrait renders the
    -- model as of its last rebuild, so accumulated combat blood/gore didn't show until
    -- something else reset it ("clean portrait on a gory companion" report, 11 Jul)
    pcall(function() zombie:resetModelNextFrame() end)
    if BanditsNPCDialogWindow.instance then BanditsNPCDialogWindow.instance:onClose() end
    local win = BanditsNPCDialogWindow:new(zombie)
    win:initialise()
    win:addToUIManager()
    BanditsNPCDialogWindow.instance = win

    -- CONTROLLER (v0.76.2). This window is 31 real ISButtons rather than drawn
    -- rectangles, so it publishes getJoypadTargets below and the navigator walks those.
    -- Only runs when a pad is actually connected; if any of it fails the joypad module
    -- disables itself and this stays a mouse window.
    pcall(function()
        local Jp = BanditsNPC.Joypad
        if not Jp then return end
        Jp.Install(BanditsNPCDialogWindow)
        if Jp.Present(0) then
            win.joySel = 1
            Jp.Grab(win, 0)
        end
    end)
    return win
end

-- The controls a controller may move between. Recomputed per call rather than cached
-- because tabs swap whole rows in and out, and a stale list would let the D-pad press
-- something that is no longer there.
--
-- TWO SOURCES SINCE v0.77.1, and both are needed: the footer, the tab rail and the Job/Gear
-- tabs are still real ISButtons, while Profile and Orders are drawn rectangles in _hits.
-- Listing only one of them would leave half the window unreachable by pad, which is the
-- opposite of what the author asked for ("everything must be at least reachable through
-- controller input").
function BanditsNPCDialogWindow:getJoypadTargets()
    if not BanditsNPC.Joypad then return {} end
    local out = BanditsNPC.Joypad.ButtonTargets(self) or {}
    local win = self
    for _, h in ipairs(self._hits or {}) do
        out[#out + 1] = { x = h.x, y = h.y, w = h.w, h = h.h,
                          go = function() win:onHit(h, true) end }   -- true = from the pad
    end
    table.sort(out, function(a, b)
        if math.abs(a.y - b.y) > 4 then return a.y < b.y end
        return a.x < b.x
    end)
    return out
end
