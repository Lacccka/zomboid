--
-- True Companions - shared UI style and widgets (v0.77.0)
--
-- ===========================================================================
-- WHY THIS FILE EXISTS
-- ===========================================================================
--
-- Every colour in the mod's UI used to be written at the point it was drawn, which is how
-- the panels drifted apart: the dialog window's green, the roster's slightly different
-- green, and a third one in the beacon window that nobody meant to be different. The brief
-- for the rework asks for one table, referenced everywhere, and that is the single most
-- useful thing in it -- restyling then becomes editing this file rather than grepping for
-- floats.
--
-- NO GREEN. Vanilla Project Zomboid has no green chrome anywhere; the only saturated accent
-- in its own UI is the list-selection orange, and that is what ACTIVE uses here. The old
-- green highlights read as "modded" at a glance, which is exactly what a UI layered onto
-- someone else's game should not do.
--
-- PZ colours are 0..1 floats, not 0..255. Getting that wrong produces pure white, which is
-- worth stating because it has happened in this project before.
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.UI = BanditsNPC.UI or {}
local U = BanditsNPC.UI

-- ---------------------------------------------------------------------------
-- the palette
-- ---------------------------------------------------------------------------

U.C = {
    panelBg      = { r = 0.00, g = 0.00, b = 0.00, a = 0.87 },
    windowBorder = { r = 0.42, g = 0.42, b = 0.37, a = 1.00 },
    hairline     = { r = 0.29, g = 0.29, b = 0.25, a = 1.00 },

    btnBorder    = { r = 0.48, g = 0.48, b = 0.42, a = 1.00 },
    btnBg        = { r = 0.00, g = 0.00, b = 0.00, a = 0.45 },
    btnText      = { r = 0.86, g = 0.86, b = 0.82, a = 1.00 },

    -- the vanilla list-selection orange; the ONLY saturated accent in the mod
    activeBorder = { r = 0.70, g = 0.35, b = 0.15, a = 1.00 },
    activeBg     = { r = 0.70, g = 0.35, b = 0.15, a = 0.34 },
    activeText   = { r = 0.95, g = 0.87, b = 0.78, a = 1.00 },

    emphBorder   = { r = 0.72, g = 0.72, b = 0.66, a = 1.00 },   -- primary action only
    emphBg       = { r = 1.00, g = 1.00, b = 1.00, a = 0.11 },
    emphText     = { r = 1.00, g = 1.00, b = 1.00, a = 1.00 },

    disBorder    = { r = 0.29, g = 0.29, b = 0.26, a = 1.00 },
    disText      = { r = 0.43, g = 0.43, b = 0.39, a = 1.00 },

    dangerBorder = { r = 0.54, g = 0.29, b = 0.27, a = 1.00 },   -- Dismiss only
    dangerText   = { r = 0.85, g = 0.60, b = 0.58, a = 1.00 },

    label        = { r = 0.71, g = 0.71, b = 0.64, a = 1.00 },
    muted        = { r = 0.54, g = 0.54, b = 0.49, a = 1.00 },
    warn         = { r = 0.84, g = 0.67, b = 0.31, a = 1.00 },
    good         = { r = 0.62, g = 0.72, b = 0.52, a = 1.00 },   -- health fill, desaturated
    -- 0.075, not the brief's 0.035. On the routine grid the banding at 0.035 was invisible
    -- against the container behind it -- the alternation might as well not have been there.
    -- Still well short of a visible stripe; it only has to separate one row from the next.
    rowAlt       = { r = 1.00, g = 1.00, b = 1.00, a = 0.075 },
    rowSel       = { r = 0.70, g = 0.35, b = 0.15, a = 0.30 },
}

-- ---------------------------------------------------------------------------
-- metrics
-- ---------------------------------------------------------------------------
--
-- Scaled through BanditsNPC.UIScaleValues so the sandbox UI-scale option reaches the new
-- chrome too; the raw numbers below are the 1.0 values from the brief.
U.M = {
    btnH = 22, btnPadX = 6, gap = 5, sectionGap = 11,
    rail = 170, footer = 34, scrollGutter = 5, pad = 10,
}

-- ONE SOURCE, and it must be the one the windows already lay themselves out with.
--
-- This used to read BanditsNPC.Opt("UIScale"), which consults the runtime override store
-- BEFORE the sandbox var, while BanditsNPC.UIScaleValues -- what the dialog window's own
-- sc() is built from -- reads the sandbox var alone. UIScale is not currently override-able
-- so the two always agreed in practice, but half of a window laid out at one scale and half
-- at another is the kind of fault that only shows up after someone adds the option, and by
-- then it looks like a layout bug rather than a units bug.
function U.Scale()
    local s = 1
    pcall(function()
        local v = BanditsNPC.UIScaleValues()
        if type(v) == "number" and v > 0 then s = v end
    end)
    return s
end

function U.S(v)
    return math.floor((v or 0) * U.Scale())
end

-- ---------------------------------------------------------------------------
-- primitives
-- ---------------------------------------------------------------------------

function U.Fill(p, x, y, w, h, c)
    p:drawRect(x, y, w, h, c.a, c.r, c.g, c.b)
end

function U.Border(p, x, y, w, h, c)
    p:drawRectBorder(x, y, w, h, c.a, c.r, c.g, c.b)
end

-- A section header is a label with a hairline filling the rest of the row, and an optional
-- right-aligned note ("15:20 - evening", "overrides schedule until 10pm"). Consistency here
-- is most of what makes the panels look like one product: same colour, same gap, always.
function U.Header(p, x, y, w, text, note, font)
    font = font or UIFont.Small
    local tm = getTextManager()
    local tw = tm:MeasureStringX(font, text)
    local fh = tm:getFontHeight(font)
    p:drawText(text, x, y, U.C.label.r, U.C.label.g, U.C.label.b, 1, font)
    local lineY = y + math.floor(fh / 2)
    local lx = x + tw + U.S(6)
    local rx = x + w
    if note and note ~= "" then
        local nw = tm:MeasureStringX(font, note)
        rx = x + w - nw - U.S(6)
        p:drawText(note, x + w - nw, y, U.C.muted.r, U.C.muted.g, U.C.muted.b, 1, font)
    end
    if rx > lx then U.Fill(p, lx, lineY, rx - lx, 1, U.C.hairline) end
    return y + fh + U.S(4)
end

-- A labelled bar: label, track, right-aligned value. `rising` flips the meaning -- health
-- fills as it is GOOD, the five needs fill as pressure RISES, and using one colour for both
-- was actively misleading on the old header.
function U.StatBar(p, x, y, w, label, value, maxv, rising, labelW)
    local tm = getTextManager()
    local fh = tm:getFontHeight(UIFont.Small)
    labelW = labelW or U.S(48)
    local v = math.max(0, math.min(maxv or 100, value or 0))
    local frac = (maxv and maxv > 0) and (v / maxv) or 0

    p:drawText(label, x, y, U.C.label.r, U.C.label.g, U.C.label.b, 1, UIFont.Small)
    local valTxt = tostring(math.floor(v))
    local vw = tm:MeasureStringX(UIFont.Small, valTxt)
    local barX = x + labelW
    local barW = w - labelW - vw - U.S(6)
    local barY = y + math.floor((fh - U.S(8)) / 2)
    local barH = U.S(8)

    U.Fill(p, barX, barY, barW, barH, { r = 0, g = 0, b = 0, a = 0.5 })
    local c
    if rising then
        -- pressure: calm -> amber -> red as it climbs
        if frac > 0.75 then c = { r = 0.78, g = 0.34, b = 0.30, a = 1 }
        elseif frac > 0.45 then c = U.C.warn
        else c = { r = 0.55, g = 0.52, b = 0.36, a = 1 } end
    else
        if frac < 0.30 then c = { r = 0.78, g = 0.34, b = 0.30, a = 1 }
        elseif frac < 0.60 then c = U.C.warn
        else c = U.C.good end
    end
    if barW > 0 then U.Fill(p, barX, barY, math.floor(barW * frac), barH, c) end
    U.Border(p, barX, barY, barW, barH, U.C.hairline)
    p:drawText(valTxt, x + w - vw, y, U.C.btnText.r, U.C.btnText.g, U.C.btnText.b, 1, UIFont.Small)
    return y + fh + U.S(2)
end

-- ---------------------------------------------------------------------------
-- segmented control
-- ---------------------------------------------------------------------------
--
-- Movement, Distance and Stance are all "pick exactly one", and the brief is right that
-- they should look like it: one welded strip, buttons butting at x+w with no gap, only the
-- leftmost drawing a left border. Equal widths on a fixed grid, never sized to the label --
-- ragged rows are most of why the old Orders tab looked unfinished.
--
-- Returns the hit list so the caller can feed it to its own click handler; this widget
-- draws and measures, it does not own state.
--
-- `font` and `height` are optional. The Orders strips pass the larger font and a taller box
-- so the segments read as squarish buttons rather than long thin slots -- at the default
-- 22px with Small text a five-segment strip across the content column is 130x22 per segment,
-- which is a letterbox.
function U.Segmented(p, x, y, w, items, activeKey, hits, kindPrefix, font, height)
    local n = #items
    if n == 0 then return y end
    font = font or UIFont.Small
    local h = height or U.S(U.M.btnH)
    local tm = getTextManager()
    local fh = tm:getFontHeight(font)
    -- Integer division with the remainder spread over the first segments, so the strip ends
    -- exactly on x+w instead of leaving a one-pixel gap that reads as a rendering fault.
    local base = math.floor(w / n)
    local extra = w - base * n
    local cx = x
    for i, it in ipairs(items) do
        local sw = base + (i <= extra and 1 or 0)
        local on = (it.key == activeKey)
        local dis = it.disabled
        U.Fill(p, cx, y, sw, h, on and U.C.activeBg or U.C.btnBg)
        -- Only the first segment draws its left edge; the rest inherit their neighbour's.
        local bc = dis and U.C.disBorder or (on and U.C.activeBorder or U.C.btnBorder)
        U.Fill(p, cx, y, 1, h, bc)
        U.Fill(p, cx, y, sw, 1, bc)
        U.Fill(p, cx, y + h - 1, sw, 1, bc)
        if i == n then U.Fill(p, cx + sw - 1, y, 1, h, bc) end
        local tc = dis and U.C.disText or (on and U.C.activeText or U.C.btnText)
        local tw = tm:MeasureStringX(font, it.label)
        p:drawText(it.label, cx + math.floor((sw - tw) / 2), y + math.floor((h - fh) / 2),
                   tc.r, tc.g, tc.b, 1, font)
        if hits and not dis then
            hits[#hits + 1] = { x = cx, y = y, w = sw, h = h,
                                kind = (kindPrefix or "") .. it.key }
        end
        cx = cx + sw
    end
    return y + h
end

-- A single button drawn in the same language as the strip, for the odd control that is not
-- part of a radio group (Firearms, Turn in, Produce).
--
-- STATES: nil | "active" | "emph" | "danger" | "disabled" | "dim".
-- "dim" and "disabled" look identical and differ only in whether the control still takes a
-- click. The schedule rows need dim: the brief says an off schedule greys its three rows but
-- keeps them "visible and editable", and shipping them as "disabled" made them unclickable,
-- so the only way to set a block up was to switch the schedule on first.
function U.Button(p, x, y, w, label, state, hits, kind, font, height)
    font = font or UIFont.Small
    local h = height or U.S(U.M.btnH)
    local tm = getTextManager()
    local fh = tm:getFontHeight(font)
    local bg, bc, tc = U.C.btnBg, U.C.btnBorder, U.C.btnText
    if state == "active" then bg, bc, tc = U.C.activeBg, U.C.activeBorder, U.C.activeText
    elseif state == "emph" then bg, bc, tc = U.C.emphBg, U.C.emphBorder, U.C.emphText
    elseif state == "danger" then bg, bc, tc = U.C.btnBg, U.C.dangerBorder, U.C.dangerText
    elseif state == "disabled" or state == "dim" then bg, bc, tc = U.C.btnBg, U.C.disBorder, U.C.disText end
    U.Fill(p, x, y, w, h, bg)
    U.Border(p, x, y, w, h, bc)
    local tw = tm:MeasureStringX(font, label)
    p:drawText(label, x + math.floor((w - tw) / 2), y + math.floor((h - fh) / 2),
               tc.r, tc.g, tc.b, 1, font)
    -- A DISABLED control stays visible and unclickable rather than vanishing: a button that
    -- disappears when it cannot be used leaves the player wondering what they did wrong.
    if hits and state ~= "disabled" then
        hits[#hits + 1] = { x = x, y = y, w = w, h = h, kind = kind }
    end
    return y + h
end

-- A framed region: the rail, the content area and the footer are each one of these. The
-- mock-ups draw the window as three boxes rather than one field of text, and that reading
-- is most of what makes it look built rather than printed.
function U.Container(p, x, y, w, h)
    U.Fill(p, x, y, w, h, { r = 0, g = 0, b = 0, a = 0.35 })
    U.Border(p, x, y, w, h, U.C.hairline)
end

-- The debug affordance: a green "+" AFTER the label it belongs to ("Bed +"), drawn as plain
-- text with no box round it. The boxed version read as a control in its own right, which is
-- the opposite of what a debug afterthought on someone else's row should look like.
--
-- Green is deliberately the one green thing in the mod -- the brief bans it from CHROME, and
-- that is exactly why it is legible here: nothing that only exists with debug enabled should
-- be mistakable for a real control.
U.DEBUG_GREEN = { r = 0.42, g = 0.82, b = 0.38, a = 1 }

function U.DebugPlus(p, x, y, hits, kind, tip)
    local c = U.DEBUG_GREEN
    p:drawText("+", x, y, c.r, c.g, c.b, 1, UIFont.Small)
    local w = getTextManager():MeasureStringX(UIFont.Small, "+")
    -- A four-pixel skirt: the glyph alone is a hard target on a row this size.
    U.Hit(hits, x - U.S(4), y - U.S(3), w + U.S(8), getTextManager():getFontHeight(UIFont.Small) + U.S(6), kind, tip)
    return w
end

-- Paint a REAL ISButton in the same language as U.Button. Not every control can be a drawn
-- rectangle -- the ones that need a tooltip, keyboard focus or a vanilla combo box beside
-- them are still ISButtons -- and a window that mixes the two has to make them agree or it
-- looks like two different mods. Only ISButton's own colour fields are touched, so a button
-- styled here behaves exactly as it did before.
function U.StyleButton(b, state)
    if not b then return end
    local bg, bc, tc = U.C.btnBg, U.C.btnBorder, U.C.btnText
    if state == "active" then bg, bc, tc = U.C.activeBg, U.C.activeBorder, U.C.activeText
    elseif state == "emph" then bg, bc, tc = U.C.emphBg, U.C.emphBorder, U.C.emphText
    elseif state == "danger" then bg, bc, tc = U.C.btnBg, U.C.dangerBorder, U.C.dangerText end
    b.backgroundColor = { r = bg.r, g = bg.g, b = bg.b, a = bg.a }
    b.backgroundColorMouseOver = { r = 1, g = 1, b = 1, a = 0.14 }
    b.borderColor = { r = bc.r, g = bc.g, b = bc.b, a = bc.a }
    b.textColor = { r = tc.r, g = tc.g, b = tc.b, a = tc.a }
    return b
end

-- ---------------------------------------------------------------------------
-- list box
-- ---------------------------------------------------------------------------
--
-- Bordered, alternating rows, a scroll gutter reserved whether or not it is needed (so the
-- content does not shift the first time a list grows), and a centred muted line when empty.
-- An empty list that renders as blank space is indistinguishable from a broken one.
function U.List(p, x, y, w, h, rows, selIdx, emptyText, hits, kindPrefix, scroll)
    U.Fill(p, x, y, w, h, { r = 0, g = 0, b = 0, a = 0.35 })
    U.Border(p, x, y, w, h, U.C.hairline)
    local tm = getTextManager()
    local fh = tm:getFontHeight(UIFont.Small)
    local rowH = fh + U.S(6)
    local gut = U.S(U.M.scrollGutter)

    if not rows or #rows == 0 then
        local t = emptyText or ""
        local tw = tm:MeasureStringX(UIFont.Small, t)
        p:drawText(t, x + math.floor((w - tw) / 2), y + math.floor((h - fh) / 2),
                   U.C.muted.r, U.C.muted.g, U.C.muted.b, 1, UIFont.Small)
        return
    end

    p:setStencilRect(x + 1, y + 1, w - 2, h - 2)
    local top = y + 1 - (scroll or 0)
    for i, r in ipairs(rows) do
        local ry = top + (i - 1) * rowH
        if ry + rowH > y and ry < y + h then
            if i == selIdx then
                U.Fill(p, x + 1, ry, w - 2 - gut, rowH, U.C.rowSel)
            elseif i % 2 == 0 then
                U.Fill(p, x + 1, ry, w - 2 - gut, rowH, U.C.rowAlt)
            end
            local tc = r.dim and U.C.disText or U.C.btnText
            if i == selIdx then tc = U.C.activeText end
            p:drawText(r.label or "", x + U.S(6), ry + U.S(3), tc.r, tc.g, tc.b, 1, UIFont.Small)
            if r.right and r.right ~= "" then
                local rw = tm:MeasureStringX(UIFont.Small, r.right)
                local rc = r.rightDim and U.C.muted or tc
                p:drawText(r.right, x + w - gut - U.S(6) - rw, ry + U.S(3),
                           rc.r, rc.g, rc.b, 1, UIFont.Small)
            end
            if hits and not r.dim then
                hits[#hits + 1] = { x = x, y = ry, w = w - gut, h = rowH,
                                    kind = (kindPrefix or "row"), index = i }
            end
        end
    end
    p:clearStencilRect()

    -- Scroll thumb, drawn only when there is something to scroll to.
    local contentH = #rows * rowH
    if contentH > h - 2 then
        local th = math.max(U.S(16), math.floor((h - 2) * (h - 2) / contentH))
        local maxScroll = contentH - (h - 2)
        local frac = maxScroll > 0 and math.min(1, (scroll or 0) / maxScroll) or 0
        local ty = y + 1 + math.floor((h - 2 - th) * frac)
        U.Fill(p, x + w - gut, ty, gut - 1, th, U.C.btnBorder)
    end
end

-- Truncate to fit a width, with an ellipsis. The rail is 170px wide and several of the
-- strings that go in it ("Following  Defensive", a long companion name, a profession plus
-- a tier label) are simply longer than that -- drawText does not clip, so without this they
-- run under the rail's own divider and out into the content, which is exactly what the
-- author spotted on the first build.
function U.Fit(text, w, font)
    font = font or UIFont.Small
    local tm = getTextManager()
    if not text or text == "" then return "" end
    if tm:MeasureStringX(font, text) <= w then return text end
    local ell = "..."
    local ew = tm:MeasureStringX(font, ell)
    local out = text
    while #out > 1 and tm:MeasureStringX(font, out) + ew > w do
        out = string.sub(out, 1, #out - 1)
    end
    return out .. ell
end

function U.RowH()
    return getTextManager():getFontHeight(UIFont.Small) + U.S(6)
end

-- ---------------------------------------------------------------------------
-- hover tooltip for the drawn controls
-- ---------------------------------------------------------------------------
--
-- ISButton carries its own tooltip; a drawn rectangle does not. The routine-spot rows are
-- where this matters -- "needs a container on the tile" is the difference between assigning
-- a Food spot and pointing at an empty patch of kitchen floor -- so replacing those buttons
-- with rows had to bring their hints along rather than quietly drop them.
--
-- Drawn LAST in the frame, by the panel, so it sits over everything. Flips to the other side
-- of the cursor near an edge instead of running off the panel.
function U.Tip(p, mx, my, text)
    if not text or text == "" then return end
    local tm = getTextManager()
    local fh = tm:getFontHeight(UIFont.Small)
    local lines = {}
    for s in string.gmatch(text .. "\n", "([^\n]*)\n") do lines[#lines + 1] = s end
    local tw = 0
    for _, s in ipairs(lines) do tw = math.max(tw, tm:MeasureStringX(UIFont.Small, s)) end
    local pad = U.S(6)
    local bw, bh = tw + pad * 2, #lines * fh + pad * 2
    local bx, by = mx + U.S(14), my + U.S(14)
    if bx + bw > p:getWidth() then bx = math.max(2, p:getWidth() - bw - 2) end
    if by + bh > p:getHeight() then by = math.max(2, my - bh - U.S(6)) end
    U.Fill(p, bx, by, bw, bh, { r = 0, g = 0, b = 0, a = 0.94 })
    U.Border(p, bx, by, bw, bh, U.C.btnBorder)
    for i, s in ipairs(lines) do
        p:drawText(s, bx + pad, by + pad + (i - 1) * fh,
                   U.C.btnText.r, U.C.btnText.g, U.C.btnText.b, 1, UIFont.Small)
    end
end

-- Register a clickable rectangle. Everything drawn by hand goes through here so the mouse
-- dispatch and the controller navigator read one list and can never disagree about what is
-- pressable -- the rule the roster panel already follows.
-- Returns the hit so a caller with a payload (which body location, which item) can hang it
-- on the table rather than encoding it into the kind string and parsing it back out.
function U.Hit(hits, x, y, w, h, kind, tip)
    if not hits then return nil end
    local hit = { x = x, y = y, w = w, h = h, kind = kind, tip = tip }
    hits[#hits + 1] = hit
    return hit
end

-- A plain progress track with an optional right-aligned caption ("3 / 10"). Used by the
-- quest card and the relationship ladder, which want the bar without StatBar's label column.
function U.Track(p, x, y, w, frac, caption, col)
    local tm = getTextManager()
    local fh = tm:getFontHeight(UIFont.Small)
    local cw = (caption and caption ~= "") and (tm:MeasureStringX(UIFont.Small, caption) + U.S(8)) or 0
    local barW = w - cw
    local barH = U.S(8)
    local barY = y + math.floor((fh - barH) / 2)
    frac = math.max(0, math.min(1, frac or 0))
    U.Fill(p, x, barY, barW, barH, { r = 0, g = 0, b = 0, a = 0.5 })
    if barW > 0 and frac > 0 then
        U.Fill(p, x, barY, math.floor(barW * frac), barH, col or U.C.activeBorder)
    end
    U.Border(p, x, barY, barW, barH, U.C.hairline)
    if cw > 0 then
        p:drawText(caption, x + w - cw + U.S(8), y,
                   U.C.btnText.r, U.C.btnText.g, U.C.btnText.b, 1, UIFont.Small)
    end
    return y + fh + U.S(2)
end

-- ---------------------------------------------------------------------------
-- debug gate
-- ---------------------------------------------------------------------------
--
-- One question, one place. The Anim tab and every "Debug - ..." block are hidden behind
-- this; the author asked for them gone from normal play and there is no reason for a
-- second opinion about what "debug" means.
function U.DebugOn()
    local on = false
    pcall(function()
        on = (isDebugEnabled and isDebugEnabled()) or (isAdmin and isAdmin()) or false
    end)
    return on and true or false
end
