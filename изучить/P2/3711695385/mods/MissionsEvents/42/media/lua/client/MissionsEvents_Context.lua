require "TimedActions/ISTimedActionQueue"
require "ISUI/ISContextMenu"

MissionsEvents = MissionsEvents or {}
MissionsEvents_Context = {}

local function isPager(item)
    if not item then return false end
    if not instanceof(item, "InventoryItem") then return false end

    local fullType = item:getFullType()
    return string.find(fullType, "Pager") ~= nil
end

function MissionsEvents_Context.onFillInventoryObjectContextMenu(player, context, items)

    local cfg = SandboxVars and SandboxVars.MissionsEvents
    if not cfg or cfg.EnableMod == false then
        return
    end

    local playerObj = getSpecificPlayer(player)
    if not playerObj then return end

    local pager = nil

    for _, v in ipairs(items) do
        local item = v

        if not instanceof(v, "InventoryItem") then
            item = v.items and v.items[1]
        end

        if isPager(item) then
            pager = item
            break
        end
    end

    if not pager then return end

	local option = context:addOption(
		getText("IGUI_MissionsEvents_PagerMenu"),
		playerObj,
		MissionsEvents_Context.onUsePager,
		pager
	)

    local icon = getTexture("media/ui/emotes/yes.png")
    if icon then
        option.iconTexture = icon
    end
end

function MissionsEvents_Context.onUsePager(player, pager)
    if not pager then return end

    ISTimedActionQueue.add(MissionsEvents_PagerAction:new(player, pager))
end

Events.OnFillInventoryObjectContextMenu.Add(
    MissionsEvents_Context.onFillInventoryObjectContextMenu
)