--
-- Bandits NPC - Companions Overhaul - Item list model + renderer (30 Jul)
--
-- ONE model and ONE row renderer for every item list the mod shows: the Give picker, the
-- companion's carried list on the Items tab, and the two-pane Items window. Those were
-- three independent flat `getAllEvalRecurse` dumps, which produced three separate faults
-- reported from the 30 Jul debug playtest:
--
--   1. The player's OWN EQUIPPED GEAR was offered up for handing over. The author gave
--      away the bag off their own back; the companion put it on and the player model kept
--      drawing it. (getAllEvalRecurse walks the player's inventory, and worn clothing,
--      hand items and a worn bag all LIVE in that inventory -- being "equipped" is a
--      separate reference held by WornItems / the hand slots / AttachedItems.)
--   2. No icons -- every row was bare text, unlike the vanilla inventory and unlike our
--      own carried list, which already drew them.
--   3. No way to tell a loose item from one buried three bags deep. A flat recursive dump
--      lists the contents of every container as if they were all lying in your hands.
--
-- What this file provides:
--   Build(container, opts) -> flat row list that CARRIES its nesting metadata
--   Fill(listbox, rows)    -> push rows into an ISScrollingListBox
--   DrawRow(...)           -> icon + tree guides + weight, shared by all lists
--   EquipKind(player, it)  -> why an item is unavailable ("hand" / "worn" / "hotbar")
--
-- The actual unequip-before-transfer fix lives with the other actions, in
-- BanditsNPCInteract.lua (BanditsNPC.Interact.TakeFromPlayer).
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.ItemList = BanditsNPC.ItemList or {}
local IL = BanditsNPC.ItemList

IL.INDENT = 12      -- px of extra indent per nesting level
IL.ICON = 16        -- icon box (square)

-- Row height that always fits both the icon and the current font. The UI-scale option can
-- push SMALL_FONT up to UIFont.Large, and the old hardcoded 22 clipped the text there.
function IL.RowHeight(font)
    local fh = getTextManager():getFontHeight(font or UIFont.Small)
    return math.max(IL.ICON + 6, fh + 6)
end

-- ===== equipped / worn detection =====

-- Why can this item not be handed over? "hand" | "worn" | "hotbar" | nil.
-- Engine calls only; all five are proven present on IsoGameCharacter in 42.20 (javap), and
-- the pair `isEquipped() or isEquippedClothing()` is vanilla's own test -- ISTradingUI.lua
-- InventoryList:addInventoryItemToGroup uses exactly that to keep your gear out of a trade.
-- isEquipped() is the one that catches a BAG ON THE BACK: a bag is neither a hand item nor
-- Clothing, which is precisely why a worn-items-only filter let it through.
function IL.EquipKind(player, item)
    if not (player and item) then return nil end
    local kind
    pcall(function()
        if player:isPrimaryHandItem(item) or player:isSecondaryHandItem(item) then
            kind = "hand"
        elseif player:isEquippedClothing(item) or player:isEquipped(item) then
            kind = "worn"
        elseif player:isAttachedItem(item) then
            kind = "hotbar"
        end
    end)
    return kind
end

function IL.EquipLabel(kind)
    if kind == "hand"   then return BanditsNPC.T("UI_BN_Inv_InHands", "in hands") end
    if kind == "worn"   then return BanditsNPC.T("UI_BN_Inv_Worn", "worn") end
    if kind == "hotbar" then return BanditsNPC.T("UI_BN_Inv_Holstered", "holstered") end
    return nil
end

-- The ItemContainer inside a bag/holdall, or nil for anything that is not a container.
-- getInventory() exists ONLY on InventoryContainer, never on InventoryItem (javap), and a
-- missing-method call is a Java dispatch error that pcall does NOT contain -- so gate on
-- BOTH the type test and the method's existence before touching it.
function IL.ContainerOf(item)
    if not item then return nil end
    local isC = false
    pcall(function() isC = (item.IsInventoryContainer and item:IsInventoryContainer()) == true end)
    if not isC then return nil end
    if item.getInventory == nil then return nil end
    local inv
    pcall(function() inv = item:getInventory() end)
    return inv
end

-- ===== model =====

local function passesFilter(item, opts)
    local hidden = false
    pcall(function() hidden = (item.isHidden and item:isHidden()) == true end)
    if hidden then return false end
    if not opts.filter then return true end
    local ok, r = pcall(opts.filter, item)
    return (ok and r) and true or false
end

-- containers last, then alphabetical: a bag and everything indented under it then reads as
-- one block at the bottom of its level instead of being scattered through the loose items
local function sortNodes(a, b)
    local ac, bc = (a.kids ~= nil), (b.kids ~= nil)
    if ac ~= bc then return bc end
    return (a.name or "") < (b.name or "")
end

-- STACKING (v0.77.4). Five screws used to be five rows, one under the other, which is not
-- how the base game's inventory reads and made a looted bag unusable to scroll. Identical
-- items now collapse to one row carrying a count, exactly as vanilla does.
--
-- Grouped by full type AND by equip state, so the shirt on your back never merges into the
-- pile of spares -- its row says "worn" and that has to stay true of every item behind it.
-- CONTAINERS ARE NEVER MERGED: two bags with different contents are two different things,
-- and each one is the label for the rows indented under it.
local function stackKey(node)
    if node.kids then return nil end
    local ft
    pcall(function() ft = node.item:getFullType() end)
    if not ft then return nil end
    return ft .. "|" .. tostring(node.equip) .. "|" .. tostring(node.header)
end

local function mergeStacks(nodes)
    local out, byKey = {}, {}
    for _, n in ipairs(nodes) do
        local k = stackKey(n)
        local first = k and byKey[k]
        if first then
            first.items[#first.items + 1] = n.item
            first.stack = #first.items
        else
            if k then byKey[k] = n end
            out[#out + 1] = n
        end
    end
    return out
end

local function collect(container, depth, opts, out)
    if container == nil then return end
    local items
    pcall(function() items = container:getItems() end)
    if not items then return end

    local nodes = {}
    for i = 0, items:size() - 1 do
        local it = items:get(i)
        if it then
            -- `item` stays the FIRST of the stack so every existing selection handler and
            -- context menu keeps working unchanged; `items` is the whole stack for the
            -- callers that move it.
            local node = { item = it, items = { it }, stack = 1, depth = depth }
            pcall(function() node.name = it:getName() end)
            node.name = node.name or "?"
            node.equip = IL.EquipKind(opts.player, it)
            node.pass = passesFilter(it, opts)
            -- Gear ON THE BODY is withheld. Something merely STOWED in a hotbar slot is
            -- still yours to hand over, so it stays selectable and is only labelled --
            -- TakeFromPlayer detaches it properly, so handing it over is safe.
            if opts.hideEquipped and (node.equip == "hand" or node.equip == "worn") then
                node.pass = false
            end

            local inv = (depth < (opts.maxDepth or 4)) and IL.ContainerOf(it) or nil
            if inv then
                node.kids = {}
                collect(inv, depth + 1, opts, node.kids)
            end

            -- A container you may NOT hand over still earns a row: it is the label that
            -- says what its contents are inside. Drop it only when it is also empty.
            if node.pass or (node.kids and #node.kids > 0) then
                node.header = not node.pass
                table.insert(nodes, node)
            end
        end
    end

    nodes = mergeStacks(nodes)
    table.sort(nodes, sortNodes)
    for i, n in ipairs(nodes) do
        n.last = (i == #nodes)
        table.insert(out, n)
        if n.kids then
            n.count = 0
            for _, k in ipairs(n.kids) do
                if k.depth == depth + 1 then
                    k.parentName = n.name
                    n.count = n.count + 1
                end
                table.insert(out, k)
            end
        end
    end
end

-- Build the row list for a container.
--   opts.player       owner of the container; enables equipped/worn detection
--   opts.filter       predicate(item) -> bool; nil = everything
--   opts.hideEquipped true -> equipped items are not offered (see the header rule above)
--   opts.maxDepth     nesting stop (default 4)
function IL.Build(container, opts)
    opts = opts or {}
    local rows = {}
    collect(container, 0, opts, rows)

    -- Vertical continuation lines. guides[l] = "an ancestor at level l still has siblings
    -- below this row", so its line has to be drawn through this row. Children always
    -- immediately follow their parent in the flat list, so a single stack is enough.
    local stack = {}
    for _, r in ipairs(rows) do
        r.guides = {}
        for l = 0, r.depth - 1 do r.guides[l] = stack[l] end
        stack[r.depth] = not r.last
    end
    return rows
end

-- Push rows into an ISScrollingListBox. `entry.item` stays the raw InventoryItem so every
-- existing selection handler and context menu keeps working unchanged; the nesting metadata
-- rides along on `entry.bnRow`. A HEADER row (your own worn bag, shown only so its contents
-- read as nested) gets item = nil, which makes it unselectable: ISScrollingListBox hands
-- `items[selected].item` to the click handler, so nil arrives and nothing is selected.
function IL.Fill(lb, rows)
    lb:clear()
    for _, r in ipairs(rows) do
        local label = r.name
        -- Two different counts, two different marks, deliberately: "x5" is how many of THIS
        -- item the row stands for, "(5)" is how many things are inside a bag. One notation
        -- for both would make a backpack holding five things look like five backpacks.
        if r.stack and r.stack > 1 then label = label .. "  \195\151" .. r.stack end
        if r.count and r.count > 0 then label = label .. "  (" .. r.count .. ")" end
        local suffix = IL.EquipLabel(r.equip)   -- "worn" / "in hands" / "holstered"
        if suffix then label = label .. "  -  " .. suffix end
        local tip
        if r.parentName then
            tip = BanditsNPC.TF("UI_BN_Inv_Inside", "Inside: %1", r.parentName)
        end
        local e = lb:addItem(label, (not r.header) and r.item or nil, tip)
        e.bnRow = r
    end
end

-- Every item a displayed row stands for. A selection handler only ever receives the ONE
-- InventoryItem the row was built around, so anything that MOVES a row has to ask this
-- instead -- otherwise taking "Screws x5" hands over one screw and leaves four behind
-- looking identical, which reads as the button not working.
--
-- Falls back to the single item, so a list that predates stacking still behaves.
function IL.StackOf(lb, item)
    if not item then return {} end
    if lb and lb.items then
        for _, e in ipairs(lb.items) do
            if e.item == item and e.bnRow and e.bnRow.items then return e.bnRow.items end
        end
    end
    return { item }
end

-- Will `item` still fit in `container`? Hoisted out of BanditsNPCInventoryWindow in v0.77.5
-- so the Gear tab's transfers use the same rule: once a press moves a WHOLE STACK, an
-- unchecked transfer stops being "one more item" and becomes twenty, which can bury a
-- companion under her own carry weight in a single click.
--
-- If the capacity cannot be read, ALLOW the move: refusing on a failed probe would block a
-- legitimate transfer for no visible reason, which is worse than an over-filled bag.
function IL.Fits(item, container)
    if not (item and container) then return true end
    local ok, room = pcall(function()
        local w = (item.getActualWeight and item:getActualWeight()) or 0
        return (container:getCapacityWeight() + w) <= container:getMaxWeight()
    end)
    if not ok then return true end
    return room and true or false
end

-- ===== renderer =====

-- Shared row renderer for every list. o = { font=, selected=<InventoryItem>, showWeight= }
function IL.DrawRow(lb, yy, entry, alt, o)
    o = o or {}
    local h = entry.height or lb.itemheight or 22
    -- offscreen cull, same guard vanilla's own doDrawItem uses: a full backpack tree can
    -- be a few hundred rows and only ~15 of them are ever visible
    if (yy + lb:getYScroll() + h < 0) or (yy + lb:getYScroll() >= lb.height) then
        return yy + h
    end

    local row = entry.bnRow
    local it = entry.item or (row and row.item)
    local depth = (row and row.depth) or 0
    local font = o.font or UIFont.Small
    local fh = getTextManager():getFontHeight(font)
    local ty = yy + math.floor((h - fh) / 2)

    if alt then lb:drawRect(0, yy, lb:getWidth(), h, 0.4, 0.13, 0.13, 0.13) end
    -- a container gets a band, so "everything indented below this is inside it" reads at a
    -- glance; an unavailable one is additionally dimmed by the text colour further down
    if row and row.kids then lb:drawRect(0, yy, lb:getWidth(), h, 0.20, 0.30, 0.42, 0.55) end
    if it ~= nil and o.selected ~= nil and it == o.selected then
        lb:drawRect(0, yy, lb:getWidth(), h, 0.35, 0.55, 0.45, 0.2)
    end

    -- Nesting guides, drawn as thin rects rather than box-drawing glyphs: the game fonts
    -- have no box-drawing characters and would render blanks.
    local half = math.floor(IL.INDENT / 2)
    local mid = yy + math.floor(h / 2)
    if row and row.guides then
        for l = 0, depth - 1 do
            if row.guides[l] then
                lb:drawRect(5 + l * IL.INDENT + half, yy, 1, h, 0.45, 0.62, 0.70, 0.80)
            end
        end
    end
    if depth > 0 then
        local gx = 5 + (depth - 1) * IL.INDENT + half
        lb:drawRect(gx, yy, 1, (row and row.last) and (mid - yy) or h, 0.55, 0.62, 0.70, 0.80)
        lb:drawRect(gx, mid, half + 2, 1, 0.55, 0.62, 0.70, 0.80)
    end

    local ix = 6 + depth * IL.INDENT
    local drew = false
    if it then
        pcall(function()
            local tex = it.getTex and it:getTex()
            if tex then
                lb:drawTextureScaled(tex, ix, yy + math.floor((h - IL.ICON) / 2), IL.ICON, IL.ICON, 1, 1, 1, 1)
                drew = true
            end
        end)
    end
    local tx = ix + (drew and (IL.ICON + 5) or 2)

    local dim = (row and row.header) and 0.62 or 1
    lb:drawText(entry.text or "?", tx, ty, dim, dim, dim, 1, font)

    if o.showWeight ~= false and it and not (row and row.header) then
        -- The WHOLE stack's weight. One row standing for five screws that reported the
        -- weight of one would be the only number on the tab that did not add up to the
        -- capacity bar above it.
        local wt = 0
        pcall(function()
            if row and row.items and #row.items > 1 then
                for _, s in ipairs(row.items) do wt = wt + (s:getWeight() or 0) end
            else
                wt = it:getWeight() or 0
            end
        end)
        lb:drawTextRight(string.format("%.1f", wt), lb:getWidth() - 6, ty, 0.75, 0.75, 0.75, 1, font)
    end
    return yy + h
end
