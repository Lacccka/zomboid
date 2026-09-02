local GridSortState = {}

GridSortState.MODULE = "LCCGridInventorySort"
GridSortState.COMMANDS = {
    SORT_PREPARE = "SortPrepare",
    SORT_TOKEN = "SortToken",
    SORT_REQUEST = "SortRequest",
    SYNC_LAYOUT = "SyncLayout",
    REJECT_LAYOUT = "RejectLayout",
}

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

function GridSortState.isPlayerRootContainer(container)
    if not container then return false end
    if container.getContainingItem and container:getContainingItem() then return false end
    local parent = container.getParent and container:getParent()
    return parent and instanceof and instanceof(parent, "IsoPlayer") or false
end

function GridSortState.collectItems(container)
    local out = {}
    if not container or not container.getItems then return out end
    local items = container:getItems()
    for i = 0, items:size() - 1 do
        local item = items:get(i)
        if isGridItem(item) then table.insert(out, item) end
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

-- Server-issued CAS revision: membership is always included; only explicit
-- manual/server-committed coordinates participate in the layout revision.
-- Client auto-fit is intentionally excluded because it is presentation state.
function GridSortState.authorityHash(container)
    local h = 17
    local items = GridSortState.collectItems(container)
    for _, item in ipairs(items) do
        local md = item.getModData and item:getModData() or nil
        local manual = md and md.gridManual and true or false
        h = feedHash(h, item:getID())
        h = feedHash(h, ":")
        if manual then
            h = feedHash(h, "M,")
            h = feedHash(h, md and tonumber(md.gridX) or 0)
            h = feedHash(h, ",")
            h = feedHash(h, md and tonumber(md.gridY) or 0)
            h = feedHash(h, ",")
            h = feedHash(h, md and md.gridRot and 1 or 0)
        else
            h = feedHash(h, "A")
        end
        h = feedHash(h, ";")
    end
    return tostring(#items) .. ":" .. tostring(h)
end

function GridSortState.layoutHash(container)
    local h = 17
    local items = GridSortState.collectItems(container)
    for _, item in ipairs(items) do
        local md = item.getModData and item:getModData() or nil
        h = feedHash(h, item:getID())
        h = feedHash(h, ":")
        h = feedHash(h, md and tonumber(md.gridX) or 0)
        h = feedHash(h, ",")
        h = feedHash(h, md and tonumber(md.gridY) or 0)
        h = feedHash(h, ",")
        h = feedHash(h, md and md.gridRot and 1 or 0)
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
            gridContainer = md and md.gridContainer or nil,
            manual = md and md.gridManual and true or false,
        })
    end
    return out
end

return GridSortState
