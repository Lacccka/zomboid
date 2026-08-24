local GridSortState = {}

GridSortState.MODULE = "LCCGridInventorySort"
GridSortState.COMMANDS = {
    SORT_REQUEST = "SortRequest",
    PAGE_MOVE = "PageMove",
    PAGE_REORDER = "PageReorder",
    PAGE_CLEAR = "PageClear",
    SYNC_LAYOUT = "SyncLayout",
    SYNC_ITEM = "SyncItem",
    REJECT_LAYOUT = "RejectLayout",
}

GridSortState.MAX_PAGES = 32

local HASH_MOD = 2147483647
local HASH_MUL = 131

local function isGridItem(item)
    if not item then return false end
    if item.isHidden and item:isHidden() then return false end
    if item.isEquipped and item:isEquipped() then return false end
    if item.getAttachedSlot and item:getAttachedSlot() ~= -1 then return false end
    return true
end

function GridSortState.isGridItem(item)
    return isGridItem(item)
end

function GridSortState.collectItems(container)
    local out = {}
    if not container or not container.getItems then return out end
    local items = container:getItems()
    for i = 0, items:size() - 1 do
        local item = items:get(i)
        if isGridItem(item) then
            table.insert(out, item)
        end
    end
    table.sort(out, function(a, b)
        local ai, bi = tonumber(a:getID()), tonumber(b:getID())
        if ai and bi then return ai < bi end
        return tostring(a:getID()) < tostring(b:getID())
    end)
    return out
end

local function feedHash(h, text)
    text = tostring(text or "")
    for i = 1, #text do
        h = (h * HASH_MUL + string.byte(text, i)) % HASH_MOD
    end
    return h
end

function GridSortState.layoutHash(container)
    local h = 17
    local items = GridSortState.collectItems(container)
    for _, item in ipairs(items) do
        local md = item.getModData and item:getModData() or nil
        local page = md and tonumber(md.gridPage) or 1
        local x = md and tonumber(md.gridX) or 0
        local y = md and tonumber(md.gridY) or 0
        local rot = md and md.gridRot and 1 or 0
        h = feedHash(h, item:getID())
        h = feedHash(h, ":")
        h = feedHash(h, page)
        h = feedHash(h, ",")
        h = feedHash(h, x)
        h = feedHash(h, ",")
        h = feedHash(h, y)
        h = feedHash(h, ",")
        h = feedHash(h, rot)
        h = feedHash(h, ";")
    end
    return tostring(#items) .. ":" .. tostring(h)
end

function GridSortState.snapshot(container)
    local out = {}
    for _, item in ipairs(GridSortState.collectItems(container)) do
        local md = item.getModData and item:getModData() or nil
        table.insert(out, {
            itemId = item:getID(),
            x = md and tonumber(md.gridX) or nil,
            y = md and tonumber(md.gridY) or nil,
            rotated = md and md.gridRot and true or false,
            page = md and tonumber(md.gridPage) or 1,
            gridContainer = md and md.gridContainer or nil,
            manual = md and md.gridManual and true or false,
        })
    end
    return out
end

return GridSortState
