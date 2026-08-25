local UIHelpers = {}

function UIHelpers.syncScrollingListScrollbar(list)
	if not list or not list.vscroll then
		return
	end
	local h = list:getHeight()
	list.vscroll:setHeight(h)
	list.vscroll:setY(0)
	list.vscroll:setX(list:getWidth() - list.vscroll:getWidth())
	if list.vscroll.recalcSize then
		list.vscroll:recalcSize()
	end
end

return UIHelpers
