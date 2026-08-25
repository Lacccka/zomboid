require("ISUI/ISScrollingListBox")

local UIHelpers = require("InteractiveNPCs/UI/UIHelpers")

local STScrollingListBox = ISScrollingListBox:derive("InteractiveNPCsSTScrollingListBox")

function STScrollingListBox:prerender()
	self.doRepaintStencil = true
	if self.vscroll then
		self.vscroll.doSetStencil = true
		self.vscroll.doRepaintStencil = true
	end
	UIHelpers.syncScrollingListScrollbar(self)
	ISScrollingListBox.prerender(self)
end

return STScrollingListBox
