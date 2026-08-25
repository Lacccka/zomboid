--
-- True Companions - Zone drawing panel
--
-- The small floating bar that appears while a zone cursor is armed. It is what
-- makes multi-piece areas possible: without an explicit Finish there is no way to
-- tell "I let go of the mouse" from "I am done", so letting go would have to end
-- the job and an L-shaped base could never be drawn.
--
-- Sits top-centre, clear of the world where you are dragging.
--

require "ISUI/ISPanel"
require "ISUI/ISButton"

BanditsNPCZonePanel = ISPanel:derive("BanditsNPCZonePanel")

local FONT_HGT = getTextManager():getFontHeight(UIFont.Small)
local PAD = 10
local BTN_H = FONT_HGT + 10

function BanditsNPCZonePanel:createChildren()
    local y = PAD + FONT_HGT + 6 + FONT_HGT + PAD
    local bw = (self.width - PAD * 2 - 12) / 3

    self.finishBtn = ISButton:new(PAD, y, bw, BTN_H,
        BanditsNPC.T("UI_BN_Zone_Finish", "Finish"), self, self.onFinish)
    self.finishBtn:initialise(); self.finishBtn:instantiate(); self:addChild(self.finishBtn)

    self.undoBtn = ISButton:new(PAD + bw + 6, y, bw, BTN_H,
        BanditsNPC.T("UI_BN_Zone_Undo", "Undo piece"), self, self.onUndo)
    self.undoBtn:initialise(); self.undoBtn:instantiate(); self:addChild(self.undoBtn)

    self.cancelBtn = ISButton:new(PAD + (bw + 6) * 2, y, bw, BTN_H,
        BanditsNPC.T("UI_BN_Cancel", "Cancel"), self, self.onCancel)
    self.cancelBtn:initialise(); self.cancelBtn:instantiate(); self:addChild(self.cancelBtn)

    self:refresh()
end

function BanditsNPCZonePanel:refresh()
    local n = (self.cursor and self.cursor.parts and #self.cursor.parts) or 0
    if self.finishBtn then self.finishBtn:setEnable(n > 0) end
    if self.undoBtn then self.undoBtn:setEnable(n > 0) end
end

function BanditsNPCZonePanel:onFinish()
    if self.cursor then self.cursor:finish() end
end

function BanditsNPCZonePanel:onUndo()
    if self.cursor then self.cursor:undoPiece() end
    self:refresh()
end

function BanditsNPCZonePanel:onCancel()
    local pnum = self.cursor and self.cursor.player or 0
    BanditsNPCZoneCursor.Cancel(pnum)
end

function BanditsNPCZonePanel:close()
    self:removeFromUIManager()
    if BanditsNPCZonePanel.instance == self then BanditsNPCZonePanel.instance = nil end
end

function BanditsNPCZonePanel:prerender()
    -- The cursor can be cleared by ESC or by another mod taking the drag. If it has
    -- gone, this panel is driving nothing and must not linger over the world.
    local drag = getCell():getDrag(self.cursor and self.cursor.player or 0)
    if drag ~= self.cursor then self:close(); return end

    ISPanel.prerender(self)
    self:refresh()

    local Zc = BanditsNPC.Zones
    local kindName = Zc.KindName(self.cursor.kind)
    local col = Zc.COLOR[self.cursor.kind] or Zc.COLOR.base

    -- colour chip so the bar matches what is being painted on the ground
    self:drawRect(PAD, PAD + 2, 10, 10, 0.95, col.r, col.g, col.b)
    self:drawText(BanditsNPC.TF("UI_BN_Zone_Drawing", "Marking out: %1", kindName),
                  PAD + 18, PAD, 1, 1, 1, 1, UIFont.Small)

    local n = #self.cursor.parts
    local hint
    if n == 0 then
        hint = BanditsNPC.T("UI_BN_Zone_HintFirst",
            "Drag on the ground to mark out a piece.")
    else
        hint = BanditsNPC.TF("UI_BN_Zone_HintMore",
            "%1 piece(s). Drag more to cover odd shapes, then Finish.", n)
    end
    self:drawText(hint, PAD, PAD + FONT_HGT + 6, 0.8, 0.8, 0.8, 1, UIFont.Small)
end

function BanditsNPCZonePanel:new(cursor)
    local w = 420
    local h = PAD + FONT_HGT + 6 + FONT_HGT + PAD + BTN_H + PAD
    local x = (getCore():getScreenWidth() - w) / 2
    local o = ISPanel.new(self, x, 60, w, h)
    o.cursor = cursor
    o.backgroundColor = { r = 0.07, g = 0.07, b = 0.08, a = 0.92 }
    o.borderColor = { r = 0.45, g = 0.45, b = 0.5, a = 1 }
    o.moveWithMouse = true
    return o
end

function BanditsNPCZonePanel.OpenFor(cursor)
    if BanditsNPCZonePanel.instance then BanditsNPCZonePanel.instance:close() end
    local p = BanditsNPCZonePanel:new(cursor)
    p:initialise()
    p:addToUIManager()
    BanditsNPCZonePanel.instance = p
    return p
end
