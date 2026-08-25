require("ISUI/ISCollapsableWindow")
require("ISUI/ISRichTextPanel")
require("ISUI/ISButton")
require("ISUI/ISUI3DModel")
require("ISUI/ISLabel")

local Shared = require("InteractiveNPCs/Shared")
local Client = require("InteractiveNPCs/Client")
local AvatarUtils = require("InteractiveNPCs/AvatarUtils")
local Theme = require("ElyonLib/UI/Theme/Theme")
local Layout = require("ElyonLib/UI/Layout/LayoutUtils")
local TextUtils = require("ElyonLib/TextUtils/TextUtils")
local UIHelpers = require("InteractiveNPCs/UI/UIHelpers")

require("ISUI/ISPanel")

local OptionsScrollPanel = ISPanel:derive("InteractiveNPCsDialogueOptionsScroll")

function OptionsScrollPanel:setYScroll(y)
	ISPanel.setYScroll(self, y)
	if self.optionsContent then
		self.optionsContent:setY(self:getYScroll())
	end
end

function OptionsScrollPanel:prerender()
	self.doRepaintStencil = true
	if self.vscroll then
		self.vscroll.doSetStencil = true
		self.vscroll.doRepaintStencil = true
	end
	UIHelpers.syncScrollingListScrollbar(self)
	ISPanel.prerender(self)
	self:setStencilRect(0, 0, self:getWidth(), self:getHeight())
end

function OptionsScrollPanel:render()
	ISPanel.render(self)
	self:clearStencilRect()
	if self.doRepaintStencil then
		self:repaintStencilRect(0, 0, self:getWidth(), self:getHeight())
	end
end

local function optionsInnerWidth(scroll)
	if scroll.getScrollAreaWidth then
		local w = scroll:getScrollAreaWidth()
		if type(w) == "number" and w > 0 then
			return w
		end
	end
	local full = scroll:getWidth()
	if scroll.vscroll and scroll.vscroll.getIsVisible and scroll.vscroll:getIsVisible() then
		return math.max(40, full - scroll.vscroll:getWidth())
	end
	return full
end

local function resolvePortraitTexture(avatar)
	if not avatar or avatar.portrait == "" then
		return nil
	end
	local path = avatar.portrait
	local kind = Shared.normalizePortraitSource(avatar.portraitSource)
	if getTexture then
		local tex = getTexture(path)
		if tex then
			return tex
		end
		if kind == Shared.PORTRAIT_SOURCE.SPRITE then
			tex = getTexture(path .. ".png")
			if tex then
				return tex
			end
		end
	end
	return nil
end

local function getTexturePixelSize(tex)
	if not tex then
		return nil, nil
	end
	if tex.getWidth and tex.getHeight then
		local w = tex:getWidth()
		local h = tex:getHeight()
		if type(w) == "number" and type(h) == "number" and w > 0 and h > 0 then
			return w, h
		end
	end
	return nil, nil
end

local T = Theme.colors

local C = {
	PAD = 10,
	BTN_H = 30,
	MIN_W = 600,
	MIN_H = 450,
	DEF_W = 700,
	DEF_H = 550,
	AVATAR_W = 250,
	AVATAR_NAME_PAD = 6,
	AVATAR_COLUMN_BOTTOM_PAD = 10,
}

local DialogueUI = ISCollapsableWindow:derive("InteractiveNPCsDialogueUI")
DialogueUI.instance = nil

local function escapeRich(text)
	text = tostring(text or "")
	text = text:gsub("<", "&lt;")
	return text
end

function DialogueUI:new(x, y, w, h, npc)
	local o = ISCollapsableWindow.new(self, x, y, w, h)
	local nm = npc and tostring(npc.name or ""):match("^%s*(.-)%s*$") or ""
	o.title = nm ~= "" and nm or " "
	o.npc = npc
	o.resizable = true
	o.moveWithMouse = true
	o.minimumWidth = C.MIN_W
	o.minimumHeight = C.MIN_H
	o.borderColor = Theme.copy(T.border)
	o.backgroundColor = Theme.copy(T.background)
	o.currentNodeId = nil
	o.history = {}
	o.visibleNpcText = ""
	o.fullNpcText = ""
	o.typeStartMs = 0
	o.responseButtons = {}
	return o
end

function DialogueUI:initialise()
	ISCollapsableWindow.initialise(self)
	local pad = C.PAD
	local th = self:titleBarHeight()

	self.avatarPanel = ISPanel:new(pad, th + pad, C.AVATAR_W, self.height - th - pad * 2)
	self.avatarPanel:initialise()
	self.avatarPanel.backgroundColor = Theme.copy(T.panelDark)
	self.avatarPanel.borderColor = Theme.copy(T.borderDim)
	self:addChild(self.avatarPanel)

	self.avatarNameLabel = ISLabel:new(0, 0, 20, "", T.text.r, T.text.g, T.text.b, 1, UIFont.Medium, false)
	self.avatarNameLabel:initialise()
	self.avatarPanel:addChild(self.avatarNameLabel)

	self.historyPanel = ISRichTextPanel:new(0, 0, 100, 100)
	self.historyPanel:initialise()
	self.historyPanel.autosetheight = false
	self.historyPanel:addScrollBars()
	self.historyPanel.vscroll:setVisible(true)
	self.historyPanel.defaultFont = UIFont.Medium
	self.historyPanel.marginTop = 10
	self.historyPanel.marginBottom = 22
	self.historyPanel.marginLeft = 10
	self.historyPanel.marginRight = 24
	self.historyPanel.backgroundColor = Theme.copy(T.panel)
	self.historyPanel.borderColor = Theme.copy(T.border)
	self.historyPanel.clip = true
	self:addChild(self.historyPanel)

	self.optionsScroll = OptionsScrollPanel:new(0, 0, 100, 100)
	self.optionsScroll:initialise()
	self.optionsScroll.doRepaintStencil = true

	self.optionsContent = ISPanel:new(0, 0, 100, 100)
	self.optionsContent:initialise()
	self.optionsContent.background = false
	self.optionsContent.backgroundColor = { r = 0, g = 0, b = 0, a = 0 }
	self.optionsContent.borderColor = { r = 0, g = 0, b = 0, a = 0 }
	self.optionsScroll:addChild(self.optionsContent)
	self.optionsScroll.optionsContent = self.optionsContent

	self.optionsScroll:addScrollBars()
	if self.optionsScroll.vscroll then
		self.optionsScroll.vscroll.doSetStencil = true
		self.optionsScroll.vscroll.doRepaintStencil = true
	end
	Theme.applyListStyle(self.optionsScroll)
	self.optionsScroll.drawBorder = true

	self:addChild(self.optionsScroll)
	function self.optionsScroll:onMouseWheel(del)
		if self:getScrollHeight() > self:getHeight() then
			self:setYScroll(self:getYScroll() - del * 24)
			return true
		end
		return false
	end

	self:enterNode(self.npc.dialogue and self.npc.dialogue.root or "start", true)
	self:layoutChildren()
end

function DialogueUI:resizeBottomInset()
	if self.resizable and self.resizeWidget and self.resizeWidget:getIsVisible() then
		return self:resizeWidgetHeight() + C.PAD
	end
	return C.PAD
end

function DialogueUI:layoutChildren()
	local pad = C.PAD
	local th = self:titleBarHeight()
	local bottom = self:resizeBottomInset()
	local contentH = self.height - th - pad - bottom
	local avatarW = math.min(C.AVATAR_W, math.max(250, math.floor(self.width * 0.33)))
	Layout.setBounds(self.avatarPanel, pad, th + pad, avatarW, contentH)

	local fhName = getTextManager():getFontHeight(UIFont.Medium)
	self._nameBandH = fhName + C.AVATAR_NAME_PAD
	local modelW = avatarW - pad * 2
	local modelTop = self._nameBandH
	local modelH = math.max(120, contentH - modelTop - C.AVATAR_COLUMN_BOTTOM_PAD)
	if self.model then
		Layout.setBounds(self.model, pad * 2, th + pad + modelTop, modelW, modelH)
	end

	if self.avatarNameLabel then
		local nm = TextUtils.trimToWidth(UIFont.Medium, tostring(self.npc.name or ""), avatarW - pad * 2)
		local tm = getTextManager()
		local tw = tm:MeasureStringX(UIFont.Medium, nm)
		local lx = math.max(0, math.floor((avatarW - tw) / 2))
		self.avatarNameLabel:setX(lx)
		self.avatarNameLabel:setY(0)
		self.avatarNameLabel:setWidth(math.max(1, tw))
		self.avatarNameLabel:setHeight(self._nameBandH)
		self.avatarNameLabel:setName(nm)
		self.avatarNameLabel:setVisible(nm ~= "")
	end

	local rightX = pad + avatarW + pad
	local rightW = self.width - rightX - pad
	local optionsH = math.min(220, math.max(90, math.floor(contentH * 0.38)))
	local historyH = contentH - optionsH - pad
	Layout.setBounds(self.historyPanel, rightX, th + pad, rightW, historyH)
	Layout.setBounds(
		self.optionsScroll,
		rightX,
		self.historyPanel:getY() + self.historyPanel:getHeight() + pad,
		rightW,
		optionsH
	)

	self:layoutResponses()
	self.historyPanel:paginate()
	self:syncHistoryScrollbar()
end

function DialogueUI:scrollHistoryToBottom()
	local p = self.historyPanel
	if not p or not p.setYScroll then
		return
	end
	local viewH = p:getHeight()
	local sh = p:getScrollHeight()
	local overflow = sh - viewH
	if overflow <= 0 then
		p:setYScroll(0)
	else
		p:setYScroll(-overflow)
	end
end

function DialogueUI:syncHistoryScrollbar()
	local p = self.historyPanel
	if not p or not p.vscroll then
		return
	end
	local h = p:getHeight()
	local w = p:getWidth()
	local vs = p.vscroll
	vs:setHeight(h)
	vs:setY(0)
	vs:setX(w - vs:getWidth())
	if vs.recalcSize then
		vs:recalcSize()
	end
	if vs.setVisible then
		vs:setVisible(true)
	end
	p:paginate()
	self:scrollHistoryToBottom()
end

function DialogueUI:onResize()
	ISCollapsableWindow.onResize(self)
	self:layoutChildren()
end

--- draw portrait / placeholder after children so the avatar panel background does not cover it.
function DialogueUI:drawAvatarColumnForeground()
	if not self.avatarPanel or not self.npc then
		return
	end
	local mode = self.npc.avatar and self.npc.avatar.mode or Shared.AVATAR_MODE.NONE
	local ax = self.avatarPanel:getX()
	local ay = self.avatarPanel:getY()
	local aw = self.avatarPanel:getWidth()
	local ah = self.avatarPanel:getHeight()
	local nameBand = self._nameBandH or (getTextManager():getFontHeight(UIFont.Medium) + C.AVATAR_NAME_PAD)

	if mode == Shared.AVATAR_MODE.PORTRAIT and self.portraitTexture then
		local top = nameBand + C.PAD
		local bottomPad = C.AVATAR_COLUMN_BOTTOM_PAD
		local maxW = math.max(1, aw - C.PAD * 2)
		local maxH = math.max(1, ah - top - bottomPad)
		local tw, th = getTexturePixelSize(self.portraitTexture)
		local drawW, drawH, px, py
		if tw and th then
			local scale = math.min(maxW / tw, maxH / th)
			drawW = tw * scale
			drawH = th * scale
		else
			local size = math.min(maxW, maxH)
			size = math.max(48, size)
			drawW, drawH = size, size
		end
		px = ax + (aw - drawW) / 2
		py = ay + top + (maxH - drawH) / 2
		self:drawTextureScaled(self.portraitTexture, px, py, drawW, drawH, 1, 1, 1, 1)
	elseif mode == Shared.AVATAR_MODE.NONE then
		local below = ay + nameBand + math.max(0, (ah - nameBand) / 2 - 10)
		self:drawTextCentre("NPC", ax + aw / 2, below, T.textMuted.r, T.textMuted.g, T.textMuted.b, 1, UIFont.Large)
	end
end

function DialogueUI:render()
	ISCollapsableWindow.render(self)
	self:drawAvatarColumnForeground()
end

function DialogueUI:createModel()
	if self.model then
		return self.model
	end

	local pad = C.PAD
	local th = self:titleBarHeight()
	local avatarW = self.avatarPanel and self.avatarPanel:getWidth() or C.AVATAR_W
	local contentH = self.avatarPanel and self.avatarPanel:getHeight() or 260
	local nameBand = self._nameBandH or (getTextManager():getFontHeight(UIFont.Medium) + C.AVATAR_NAME_PAD)
	local modelH = math.max(120, contentH - nameBand - C.AVATAR_COLUMN_BOTTOM_PAD)
	local model = ISUI3DModel:new(pad * 2, th + pad + nameBand, avatarW - pad * 2, modelH)
	self:addChild(model)
	model:setVisible(false)
	model:setState("idle")
	model:setDirection(IsoDirections.S)
	model:setIsometric(false)
	model:setZoom(15)
	model:setYOffset(-0.8)
	model:setXOffset(0)
	self.model = model
	return model
end

function DialogueUI:hideModel()
	if self.model then
		self.model:setVisible(false)
	end
	self.modelReady = false
end

function DialogueUI:makeSurvivorDesc()
	return AvatarUtils.makeSurvivorDesc(self.npc.avatar)
end

function DialogueUI:tryShowModelWithDesc(desc)
	if not desc then
		return false
	end

	local model = self:createModel()
	model:setSurvivorDesc(desc)
	model:setState("idle")
	model:setDirection(IsoDirections.S)
	model:setIsometric(false)
	model:render()
	model:setVisible(true)
	self.modelReady = true
	return true
end

function DialogueUI:tryShowPlayerModel()
	local player = getPlayer and getPlayer() or nil
	if not player then
		return false
	end

	local model = self:createModel()
	model:setCharacter(player)
	model:setState("idle")
	model:setDirection(IsoDirections.S)
	model:setIsometric(false)
	model:render()
	model:setVisible(true)
	self.modelReady = true
	return true
end

function DialogueUI:configureAvatar()
	local mode = self.npc.avatar and self.npc.avatar.mode or Shared.AVATAR_MODE.NONE
	self.portraitTexture = nil
	self:hideModel()
	if mode == Shared.AVATAR_MODE.PORTRAIT and self.npc.avatar and self.npc.avatar.portrait ~= "" then
		self.portraitTexture = resolvePortraitTexture(self.npc.avatar)
	elseif mode == Shared.AVATAR_MODE.OUTFIT or mode == Shared.AVATAR_MODE.PRESET then
		self:tryShowModelWithDesc(self:makeSurvivorDesc())
	end
	self:layoutChildren()
end

function DialogueUI:getNode(nodeId)
	local tree = self.npc.dialogue or {}
	local nodes = tree.nodes or {}
	return nodes[nodeId] or nodes[tree.root] or nodes.start
end

function DialogueUI:historyRich()
	local lines = {}
	for i = 1, #self.history do
		local item = self.history[i]
		if item.speaker == "player" then
			lines[#lines + 1] = string.format(" <RGB:0.62,0.88,0.60> %s <TEXT> <LINE> ", escapeRich(item.text))
		else
			lines[#lines + 1] = string.format(" <RGB:0.90,0.90,0.90> %s <TEXT> <LINE> ", escapeRich(item.text))
		end
	end
	if self.visibleNpcText ~= "" then
		lines[#lines + 1] = string.format(" <RGB:0.90,0.90,0.90> %s <TEXT> ", escapeRich(self.visibleNpcText))
	end
	if #lines == 0 then
		return " " .. getText("IGUI_INPC_NoDialogue")
	end
	return table.concat(lines, "")
end

function DialogueUI:setHistoryText()
	self.historyPanel.text = self:historyRich()
	self.historyPanel:paginate()
	self:syncHistoryScrollbar()
end

function DialogueUI:enterNode(nodeId, first)
	local node = self:getNode(nodeId)
	if not node then
		self:close()
		return
	end
	self.currentNodeId = node.id
	self.fullNpcText = tostring(node.npcText or "")
	self.visibleNpcText = ""
	self.responses = node.responses or {}
	self:rebuildResponses()
	self:setHistoryText()
end

function DialogueUI:rebuildResponses()
	self.optionsContent:clearChildren()
	self.responseButtons = {}
	self.optionsScroll:setYScroll(0)
	local responses = self.responses or {}
	for i = 1, #responses do
		local response = responses[i]
		local btn = ISButton:new(0, 0, 100, C.BTN_H, response.text or "...", self, DialogueUI.onResponse)
		btn:initialise()
		btn:instantiate()
		btn.response = response
		Theme.applyButtonStyle(btn)
		self.optionsContent:addChild(btn)
		self.responseButtons[#self.responseButtons + 1] = btn
	end
	self:layoutResponses()
end

function DialogueUI:layoutResponses()
	if not self.optionsScroll or not self.optionsContent then
		return
	end
	local pad = 6
	local lastContentH = 40
	for _ = 1, 2 do
		local inner = optionsInnerWidth(self.optionsScroll)
		self.optionsContent:setWidth(math.max(1, inner))
		local w = math.max(40, inner - pad * 2)
		local y = pad
		for i = 1, #self.responseButtons do
			local btn = self.responseButtons[i]
			Layout.setBounds(btn, pad, y, w, C.BTN_H)
			y = y + C.BTN_H + pad
		end
		lastContentH = y + pad
		local viewH = self.optionsScroll:getHeight()
		self.optionsContent:setHeight(math.max(1, lastContentH))
		self.optionsScroll:setScrollHeight(math.max(viewH, lastContentH))
		UIHelpers.syncScrollingListScrollbar(self.optionsScroll)
		if self.optionsScroll.javaObject then
			self.optionsScroll:setScrollChildren(false)
		end
		self.optionsScroll:onResize()
	end
	if self.optionsContent then
		self.optionsContent:setY(self.optionsScroll:getYScroll())
	end
end

function DialogueUI:onResponse(button)
	local response = button and button.response
	if not response then
		return
	end
	if self.fullNpcText ~= "" then
		self.history[#self.history + 1] = { speaker = "npc", text = self.fullNpcText }
	end
	local playerText = response.playerText ~= "" and response.playerText or response.text
	self.history[#self.history + 1] = { speaker = "player", text = playerText }
	if
		self.npc.settings
		and self.npc.settings.responseSound
		and self.npc.settings.responseSound ~= ""
	then
		getSoundManager():playUISound(self.npc.settings.responseSound)
	end
	if response.action == "close" then
		self:close()
	elseif response.action == "back" then
		self:enterNode(self.npc.dialogue and self.npc.dialogue.root or "start")
	elseif response.target and response.target ~= "" then
		self:enterNode(response.target)
	else
		self:close()
	end
end

function DialogueUI:update()
	ISCollapsableWindow.update(self)
	local settings = self.npc.settings or {}
	if settings.typewriter == false then
		if self.visibleNpcText ~= self.fullNpcText then
			self.visibleNpcText = self.fullNpcText
			self:setHistoryText()
		end
		return
	end
	local speed = tonumber(settings.typewriterSpeed) or 45
	local current = #self.visibleNpcText
	if current < #self.fullNpcText then
		local add = math.max(1, math.floor(speed / 20))
		self.visibleNpcText = self.fullNpcText:sub(1, math.min(#self.fullNpcText, current + add))
		self:setHistoryText()
	end
end

function DialogueUI:close()
	ISCollapsableWindow.close(self)
	if DialogueUI.instance == self then
		DialogueUI.instance = nil
	end
	Client.dialogueUiRef = nil
end

function DialogueUI.open(npc)
	if DialogueUI.instance then
		DialogueUI.instance:close()
	end
	local x, y, w, h = Layout.defaultWindowGeometry(C.DEF_W, C.DEF_H, C.MIN_W, C.MIN_H, 24)
	local ui = DialogueUI:new(x, y, w, h, npc)
	DialogueUI.instance = ui
	Client.dialogueUiRef = ui
	ui:initialise()
	ui:configureAvatar()
	ui:layoutChildren()
	ui:addToUIManager()
	if npc.settings and npc.settings.openSound and npc.settings.openSound ~= "" then
		getSoundManager():playUISound(npc.settings.openSound)
	end
	return ui
end

return DialogueUI
