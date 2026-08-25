require("ISUI/ISCollapsableWindow")
require("ISUI/ISPanel")
local UIHelpers = require("InteractiveNPCs/UI/UIHelpers")
local STScrollingListBox = require("InteractiveNPCs/UI/STScrollingListBox")
require("ISUI/ISTextEntryBox")
require("ISUI/ISButton")
require("ISUI/ISComboBox")
require("ISUI/ISTickBox")
require("ISUI/ISModalDialog")

local JSON = require("ElyonLib/FileUtils/JSON")
local Shared = require("InteractiveNPCs/Shared")
local Client = require("InteractiveNPCs/Client")
local AvatarUtils = require("InteractiveNPCs/AvatarUtils")
local Theme = require("ElyonLib/UI/Theme/Theme")
local Layout = require("ElyonLib/UI/Layout/LayoutUtils")
local TextUtils = require("ElyonLib/TextUtils/TextUtils")
local OutfitCombo = require("InteractiveNPCs/UI/OutfitCombo")

local T = Theme.colors

local C = {
	PAD = 8,
	BTN_H = 22,
	FOOTER_BTN_H = 28,
	FOOTER_ROW_GAP = 6,
	LIST_ABOVE_FOOTER_PAD = 8,
	LBL_H = 16,
	FIELD_H = 22,
	MIN_W = 950,
	MIN_H = 650,
	DEF_W = 950,
	DEF_H = 650,
	LIST_W = 220,
	NODE_W = 220,
	NODE_BTN_H = 26,
	NODE_RESP_GAP = 12,
}

local SKIN_TONE_COUNT = 5

local FormScrollPanel = ISPanel:derive("InteractiveNPCsAdminFormScroll")
function FormScrollPanel:prerender()
	ISPanel.prerender(self)
	self:setStencilRect(0, 0, self:getWidth(), self:getHeight())
end

function FormScrollPanel:render()
	ISPanel.render(self)
	self:clearStencilRect()
	if self.doRepaintStencil then
		self:repaintStencilRect(0, 0, self:getWidth(), self:getHeight())
	end
end

local AdminUI = ISCollapsableWindow:derive("InteractiveNPCsAdminUI")
AdminUI.instance = nil

local function makeLabel(parent, text)
	local label = ISLabel:new(0, 0, C.LBL_H, text, T.textMuted.r, T.textMuted.g, T.textMuted.b, 1, UIFont.Small, true)
	label:initialise()
	parent:addChild(label)
	return label
end

local function makeField(parent, multiline)
	local field = ISTextEntryBox:new("", 0, 0, 80, C.FIELD_H)
	field:initialise()
	field:instantiate()
	if multiline then
		field:setMultipleLine(true)
	end
	Theme.applyFieldStyle(field)
	parent:addChild(field)
	return field
end

local function makeButton(parent, title, target, fn, variant)
	local button = ISButton:new(0, 0, 80, C.BTN_H, title, target, fn)
	button:initialise()
	button:instantiate()
	Theme.applyButtonStyle(button, variant)
	parent:addChild(button)
	return button
end

local function makeNodeButton(parent, title, target, fn, variant)
	local button = ISButton:new(0, 0, 80, C.NODE_BTN_H, title, target, fn)
	button:initialise()
	button:instantiate()
	Theme.applyButtonStyle(button, variant)
	parent:addChild(button)
	return button
end

--- ISScrollingListBox passes the list row; payload is in `row.item`.
local function listRowPayload(row)
	if type(row) == "table" and row.item ~= nil then
		return row.item
	end
	return row
end

local function orderedNodes(tree)
	local out = {}
	local nodeMap = (tree and tree.nodes) or {}
	local ids = Shared.tableKeys(nodeMap)
	for i = 1, #ids do
		out[#out + 1] = nodeMap[ids[i]]
	end
	table.sort(out, function(a, b)
		if a.id == (tree and tree.root) then
			return true
		end
		if b.id == (tree and tree.root) then
			return false
		end
		return tostring(a.id) < tostring(b.id)
	end)
	return out
end

local function trimUi(s)
	return tostring(s or ""):match("^%s*(.-)%s*$") or ""
end

local NpcList = STScrollingListBox:derive("InteractiveNPCsAdminNpcList")
function NpcList:doDrawItem(y, item, alt)
	local npc = item.item
	if item.index == self.selected then
		self:drawRect(0, y, self.width, item.height, T.selected.a, T.selected.r, T.selected.g, T.selected.b)
	elseif alt then
		self:drawRect(0, y, self.width, item.height, T.listAlt.a, T.listAlt.r, T.listAlt.g, T.listAlt.b)
	end
	local status = npc.enabled and T.success or T.danger
	self:drawRect(0, y, 4, item.height, status.a, status.r, status.g, status.b)
	local fh = getTextManager():getFontHeight(UIFont.Small)
	local gap = 2
	local block = fh * 2 + gap
	local y0 = y + math.max(0, (item.height - block) / 2)
	local text = TextUtils.trimToWidth(UIFont.Small, tostring(npc.name or ""), self.width - 18)
	self:drawText(text, 10, y0, T.text.r, T.text.g, T.text.b, 1, UIFont.Small)
	local ref = npc.objectRef and npc.objectRef.key or "unbound"
	self:drawText(
		TextUtils.trimToWidth(UIFont.Small, ref, self.width - 18),
		10,
		y0 + fh + gap,
		T.textDim.r,
		T.textDim.g,
		T.textDim.b,
		1,
		UIFont.Small
	)
	return y + item.height
end

local NodeList = STScrollingListBox:derive("InteractiveNPCsAdminNodeList")
function NodeList:doDrawItem(y, item, alt)
	local node = item.item
	if item.index == self.selected then
		self:drawRect(0, y, self.width, item.height, T.selected.a, T.selected.r, T.selected.g, T.selected.b)
	elseif alt then
		self:drawRect(0, y, self.width, item.height, T.listAlt.a, T.listAlt.r, T.listAlt.g, T.listAlt.b)
	end
	local id = tostring(node and node.id or "")
	local text = TextUtils.trimToWidth(UIFont.Small, id, self.width - 14)
	local fh = getTextManager():getFontHeight(UIFont.Small)
	local ty = y + (item.height - fh) / 2
	self:drawText(text, 8, ty, T.text.r, T.text.g, T.text.b, 1, UIFont.Small)
	return y + item.height
end

local ResponseList = STScrollingListBox:derive("InteractiveNPCsAdminResponseList")
function ResponseList:doDrawItem(y, item, alt)
	local resp = item.item
	if item.index == self.selected then
		self:drawRect(0, y, self.width, item.height, T.selected.a, T.selected.r, T.selected.g, T.selected.b)
	elseif alt then
		self:drawRect(0, y, self.width, item.height, T.listAlt.a, T.listAlt.r, T.listAlt.g, T.listAlt.b)
	end
	local txt = tostring(resp and resp.text or "")
	local margin = 10
	local dotW = 14
	local maxW = self.width - margin * 2 - dotW - 14
	maxW = math.max(40, maxW)
	local trimmed = TextUtils.trimToWidth(UIFont.Small, txt, maxW)
	local text = trimmed
	if trimmed ~= txt then
		text = TextUtils.trimToWidth(UIFont.Small, txt, maxW - dotW) .. "..."
	end
	local fh = getTextManager():getFontHeight(UIFont.Small)
	local ty = y + (item.height - fh) / 2
	self:drawText(text, 8, ty, T.text.r, T.text.g, T.text.b, 1, UIFont.Small)
	return y + item.height
end

function AdminUI:new(x, y, w, h, pendingObjectRef)
	local o = ISCollapsableWindow.new(self, x, y, w, h)
	o.title = getText("IGUI_INPC_ManageTitle")
	o.resizable = true
	o.moveWithMouse = true
	o.minimumWidth = C.MIN_W
	o.minimumHeight = C.MIN_H
	o.borderColor = Theme.copy(T.border)
	o.backgroundColor = Theme.copy(T.background)
	o.selectedNpc = nil
	o.selectedNodeId = nil
	o.selectedResponseIndex = nil
	o.pendingObjectRef = pendingObjectRef
	o._forceSelectFirstOnNpcs = false
	return o
end

function AdminUI:initialise()
	ISCollapsableWindow.initialise(self)

	self.npcList = NpcList:new(C.PAD, self:titleBarHeight() + C.PAD, C.LIST_W, 200)
	self.npcList:initialise()
	self.npcList.itemheight = 48
	self.npcList.target = self
	self.npcList.onmousedown = AdminUI.onNpcSelected
	Theme.applyListStyle(self.npcList)
	self.npcList.drawBorder = true
	self:addChild(self.npcList)

	self.newBtn = makeButton(self, getText("IGUI_INPC_New"), self, AdminUI.onNew, "primary")
	self.saveBtn = makeButton(self, getText("IGUI_INPC_Save"), self, AdminUI.onSave, "success")
	self.deleteBtn = makeButton(self, getText("IGUI_INPC_Delete"), self, AdminUI.onDelete, "danger")
	self.refreshBtn = makeButton(self, getText("IGUI_INPC_Refresh"), self, AdminUI.onRefresh)
	self.teleportBtn = makeButton(self, getText("IGUI_INPC_TeleportNpc"), self, AdminUI.onTeleportToNpc, "primary")

	self.formScroll = FormScrollPanel:new(0, 0, 100, 100)
	self.formScroll:initialise()
	self.formScroll:noBackground()
	self.formScroll.borderColor = { r = 0, g = 0, b = 0, a = 0 }
	self.formScroll.doRepaintStencil = true
	self.formScroll:addScrollBars()
	if self.formScroll.vscroll then
		self.formScroll.vscroll.doSetStencil = true
		self.formScroll.vscroll.doRepaintStencil = true
	end
	self:addChild(self.formScroll)
	function self.formScroll:onMouseWheel(del)
		if self:getScrollHeight() > self:getHeight() then
			self:setYScroll(self:getYScroll() - del * 24)
			return true
		end
		return false
	end

	local sp = self.formScroll

	self.labels = {}
	self.labels.name = makeLabel(sp, getText("IGUI_INPC_FieldName"))
	self.nameField = makeField(sp)
	self.enabledTick = ISTickBox:new(0, 0, 140, C.FIELD_H, "", self, nil)
	self.enabledTick:initialise()
	self.enabledTick:instantiate()
	self.enabledTick:addOption(getText("IGUI_INPC_FieldEnabled"))
	Theme.applyTickBoxStyle(self.enabledTick)
	sp:addChild(self.enabledTick)
	self.visibleTick = ISTickBox:new(0, 0, 140, C.FIELD_H, "", self, nil)
	self.visibleTick:initialise()
	self.visibleTick:instantiate()
	self.visibleTick:addOption(getText("IGUI_INPC_FieldVisible"))
	Theme.applyTickBoxStyle(self.visibleTick)
	sp:addChild(self.visibleTick)

	self.labels.distance = makeLabel(sp, getText("IGUI_INPC_FieldDistance"))
	self.distanceField = makeField(sp)
	self.labels.access = makeLabel(sp, getText("IGUI_INPC_FieldMinAccess"))
	self.accessCombo = ISComboBox:new(0, 0, 100, C.FIELD_H, self, nil)
	self.accessCombo:initialise()
	self.accessCombo:instantiate()
	for i = 1, #require("ElyonLib/PlayerUtils/AccessLevelUtils").ORDER do
		local lvl = require("ElyonLib/PlayerUtils/AccessLevelUtils").ORDER[i]
		self.accessCombo:addOptionWithData(lvl, lvl)
	end
	Theme.applyComboStyle(self.accessCombo)
	sp:addChild(self.accessCombo)

	self.labels.avatar = makeLabel(sp, getText("IGUI_INPC_FieldAvatarMode"))
	self.avatarCombo = ISComboBox:new(0, 0, 100, C.FIELD_H, self, AdminUI.onAvatarModeChanged)
	self.avatarCombo:initialise()
	self.avatarCombo:instantiate()
	self.avatarCombo:addOptionWithData(getText("IGUI_INPC_ModeNone"), Shared.AVATAR_MODE.NONE)
	self.avatarCombo:addOptionWithData(getText("IGUI_INPC_ModePortrait"), Shared.AVATAR_MODE.PORTRAIT)
	self.avatarCombo:addOptionWithData(getText("IGUI_INPC_ModeOutfit"), Shared.AVATAR_MODE.OUTFIT)
	self.avatarCombo:addOptionWithData(getText("IGUI_INPC_ModePreset"), Shared.AVATAR_MODE.PRESET)
	Theme.applyComboStyle(self.avatarCombo)
	sp:addChild(self.avatarCombo)
	self.labels.preset = makeLabel(sp, getText("IGUI_INPC_FieldAvatarPreset"))
	self.presetCombo = ISComboBox:new(0, 0, 100, C.FIELD_H, self, nil)
	self.presetCombo:initialise()
	self.presetCombo:instantiate()
	Theme.applyComboStyle(self.presetCombo)
	sp:addChild(self.presetCombo)
	self.presetBtn = makeButton(sp, getText("IGUI_INPC_AvatarPresets"), self, AdminUI.onAvatarPresets, "primary")
	self.labels.portraitKind = makeLabel(sp, getText("IGUI_INPC_FieldPortraitKind"))
	self.portraitKindCombo = ISComboBox:new(0, 0, 100, C.FIELD_H, self, nil)
	self.portraitKindCombo:initialise()
	self.portraitKindCombo:instantiate()
	self.portraitKindCombo:addOptionWithData(getText("IGUI_INPC_PortraitKindUi"), Shared.PORTRAIT_SOURCE.UI_IMAGE)
	self.portraitKindCombo:addOptionWithData(getText("IGUI_INPC_PortraitKindSprite"), Shared.PORTRAIT_SOURCE.SPRITE)
	Theme.applyComboStyle(self.portraitKindCombo)
	sp:addChild(self.portraitKindCombo)
	self.labels.portrait = makeLabel(sp, getText("IGUI_INPC_FieldPortrait"))
	self.portraitField = makeField(sp)
	self.labels.outfit = makeLabel(sp, getText("IGUI_INPC_FieldOutfit"))
	self.outfitCombo = ISComboBox:new(0, 0, 100, C.FIELD_H, self, nil)
	self.outfitCombo:initialise()
	self.outfitCombo:instantiate()
	Theme.applyComboStyle(self.outfitCombo)
	sp:addChild(self.outfitCombo)
	OutfitCombo.populate(self.outfitCombo)
	self.femaleTick = ISTickBox:new(0, 0, 200, C.FIELD_H, "", self, AdminUI.onFemaleChanged)
	self.femaleTick:initialise()
	self.femaleTick:instantiate()
	self.femaleTick:addOption(getText("IGUI_INPC_FieldFemale"))
	Theme.applyTickBoxStyle(self.femaleTick)
	sp:addChild(self.femaleTick)

	self.labels.skinTone = makeLabel(sp, getText("UI_SkinColor"))
	self.skinCombo = ISComboBox:new(0, 0, 100, C.FIELD_H, self, nil)
	self.skinCombo:initialise()
	self.skinCombo:instantiate()
	for i = 1, SKIN_TONE_COUNT do
		self.skinCombo:addOptionWithData(getText("UI_SkinColor") .. " (" .. tostring(i) .. ")", i - 1)
	end
	Theme.applyComboStyle(self.skinCombo)
	sp:addChild(self.skinCombo)
	self.labels.hairStyle = makeLabel(sp, getText("IGUI_INPC_FieldHairStyle"))
	self.hairCombo = ISComboBox:new(0, 0, 100, C.FIELD_H, self, nil)
	self.hairCombo:initialise()
	self.hairCombo:instantiate()
	Theme.applyComboStyle(self.hairCombo)
	sp:addChild(self.hairCombo)
	self.labels.beardStyle = makeLabel(sp, getText("IGUI_INPC_FieldBeardStyle"))
	self.beardCombo = ISComboBox:new(0, 0, 100, C.FIELD_H, self, nil)
	self.beardCombo:initialise()
	self.beardCombo:instantiate()
	Theme.applyComboStyle(self.beardCombo)
	sp:addChild(self.beardCombo)
	self.labels.hairRgb = makeLabel(sp, getText("IGUI_INPC_FieldHairRgb"))
	self.hairRgbField = makeField(sp)
	self.labels.beardRgb = makeLabel(sp, getText("IGUI_INPC_FieldBeardRgb"))
	self.beardRgbField = makeField(sp)

	self.typewriterTick = ISTickBox:new(0, 0, 160, C.FIELD_H, "", self, nil)
	self.typewriterTick:initialise()
	self.typewriterTick:instantiate()
	self.typewriterTick:addOption(getText("IGUI_INPC_FieldTypewriter"))
	Theme.applyTickBoxStyle(self.typewriterTick)
	sp:addChild(self.typewriterTick)
	self.labels.speed = makeLabel(sp, getText("IGUI_INPC_FieldTypeSpeed"))
	self.speedField = makeField(sp)
	self.labels.openSound = makeLabel(sp, getText("IGUI_INPC_FieldOpenSound"))
	self.openSoundField = makeField(sp)
	self.labels.responseSound = makeLabel(sp, getText("IGUI_INPC_FieldResponseSound"))
	self.responseSoundField = makeField(sp)

	self.nodeList = NodeList:new(0, 0, C.NODE_W, 120)
	self.nodeList:initialise()
	self.nodeList.itemheight = 30
	self.nodeList.target = self
	self.nodeList.onmousedown = AdminUI.onNodeSelected
	Theme.applyListStyle(self.nodeList)
	self.nodeList.drawBorder = true
	self:addChild(self.nodeList)
	self.addNodeBtn = makeNodeButton(self, getText("IGUI_INPC_AddNode"), self, AdminUI.onAddNode, "primary")
	self.removeNodeBtn = makeNodeButton(self, getText("IGUI_INPC_RemoveNode"), self, AdminUI.onRemoveNode, "danger")

	self.labels.nodeText = makeLabel(self, getText("IGUI_INPC_FieldNodeText"))
	self.nodeTextField = makeField(self, true)

	self.responseList = ResponseList:new(0, 0, 100, 80)
	self.responseList:initialise()
	self.responseList.itemheight = 30
	self.responseList.target = self
	self.responseList.onmousedown = AdminUI.onResponseSelected
	Theme.applyListStyle(self.responseList)
	self.responseList.drawBorder = true
	self:addChild(self.responseList)
	self.addResponseBtn = makeButton(self, getText("IGUI_INPC_AddResponse"), self, AdminUI.onAddResponse, "primary")
	self.removeResponseBtn =
		makeButton(self, getText("IGUI_INPC_RemoveResponse"), self, AdminUI.onRemoveResponse, "danger")

	self.labels.responseText = makeLabel(self, getText("IGUI_INPC_FieldResponseText"))
	self.responseTextField = makeField(self)
	self.labels.playerText = makeLabel(self, getText("IGUI_INPC_FieldPlayerText"))
	self.playerTextField = makeField(self)
	self.labels.target = makeLabel(self, getText("IGUI_INPC_FieldTarget"))
	self.targetCombo = ISComboBox:new(0, 0, 100, C.FIELD_H, self, nil)
	self.targetCombo:initialise()
	self.targetCombo:instantiate()
	Theme.applyComboStyle(self.targetCombo)
	self:addChild(self.targetCombo)
	self.labels.action = makeLabel(self, getText("IGUI_INPC_FieldAction"))
	self.actionCombo = ISComboBox:new(0, 0, 100, C.FIELD_H, self, nil)
	self.actionCombo:initialise()
	self.actionCombo:instantiate()
	self.actionCombo:addOptionWithData(getText("IGUI_INPC_ActionNone"), "")
	self.actionCombo:addOptionWithData(getText("IGUI_INPC_ActionClose"), "close")
	self.actionCombo:addOptionWithData(getText("IGUI_INPC_ActionBack"), "back")
	Theme.applyComboStyle(self.actionCombo)
	self:addChild(self.actionCombo)

	self.resetTreeBtn = makeButton(self, getText("IGUI_INPC_ResetTree"), self, AdminUI.onResetTree)
	self.exportBtn = makeButton(self, getText("IGUI_INPC_ExportTree"), self, AdminUI.onExportTree)
	self.importBtn = makeButton(self, getText("IGUI_INPC_ImportTree"), self, AdminUI.onImportTree, "warning")
	self.labels.json = makeLabel(self, getText("IGUI_INPC_FieldJson"))
	self.jsonField = makeField(self, true)

	self:rebuildOutfitHairBeardCombos()
	self:applyTooltips()
	self:layoutChildren()
	Client.uiRef = self
	Client.requestAdmin()
end

function AdminUI:layoutFormScroll()
	local sp = self.formScroll
	if not sp then
		return
	end
	local pad = C.PAD
	local inner = sp.getScrollAreaWidth and sp:getScrollAreaWidth() or sp.width
	local w = math.max(120, inner - pad * 2)
	local y = pad

	local function place(label, control, h)
		if not label or not label.getIsVisible or not label:getIsVisible() then
			return
		end
		Layout.setBounds(label, pad, y, w, C.LBL_H)
		y = y + C.LBL_H + 2
		if control then
			Layout.setBounds(control, pad, y, w, h or C.FIELD_H)
			y = y + (h or C.FIELD_H) + pad
		end
	end

	local function placeRowSingle(control, h)
		if not control or not control.getIsVisible or not control:getIsVisible() then
			return
		end
		Layout.setBounds(control, pad, y, w, h or C.FIELD_H)
		y = y + (h or C.FIELD_H) + pad
	end

	place(self.labels.name, self.nameField)
	if self.enabledTick:getIsVisible() or self.visibleTick:getIsVisible() then
		Layout.setBounds(self.enabledTick, pad, y, 120, C.FIELD_H)
		Layout.setBounds(self.visibleTick, pad + 130, y, 120, C.FIELD_H)
		y = y + C.FIELD_H + pad
	end
	place(self.labels.distance, self.distanceField)
	place(self.labels.access, self.accessCombo)
	place(self.labels.avatar, self.avatarCombo)
	place(self.labels.preset, self.presetCombo)
	if self.presetBtn:getIsVisible() then
		Layout.setBounds(self.presetBtn, pad, y, w, C.BTN_H)
		y = y + C.BTN_H + pad
	end
	place(self.labels.portraitKind, self.portraitKindCombo)
	place(self.labels.portrait, self.portraitField)
	place(self.labels.outfit, self.outfitCombo)
	placeRowSingle(self.femaleTick, C.FIELD_H)
	place(self.labels.skinTone, self.skinCombo)
	place(self.labels.hairStyle, self.hairCombo)
	place(self.labels.beardStyle, self.beardCombo)
	place(self.labels.hairRgb, self.hairRgbField)
	place(self.labels.beardRgb, self.beardRgbField)

	if self.typewriterTick:getIsVisible() then
		Layout.setBounds(self.typewriterTick, pad, y, w, C.FIELD_H)
		y = y + C.FIELD_H + pad
	end
	place(self.labels.speed, self.speedField)
	place(self.labels.openSound, self.openSoundField)
	place(self.labels.responseSound, self.responseSoundField)

	sp:setScrollHeight(y + pad)
	UIHelpers.syncScrollingListScrollbar(sp)
end

function AdminUI:resizeBottomInset()
	if self.resizable and self.resizeWidget and self.resizeWidget:getIsVisible() then
		return self:resizeWidgetHeight() + C.PAD
	end
	return C.PAD
end

function AdminUI:setCombo(combo, data)
	for i = 1, #combo.options do
		if combo:getOptionData(i) == data then
			combo.selected = i
			return
		end
	end
	combo.selected = 1
end

function AdminUI:getCombo(combo)
	return combo:getOptionData(combo.selected)
end

function AdminUI:setComboOptionData(combo, data)
	if not combo then
		return
	end
	if data == nil then
		combo.selected = combo.options and #combo.options > 0 and 1 or 0
		return
	end
	for i = 1, #(combo.options or {}) do
		if combo:getOptionData(i) == data then
			combo.selected = i
			return
		end
	end
	combo.selected = combo.options and #combo.options > 0 and 1 or 0
end

function AdminUI:rebuildOutfitHairBeardCombos()
	local isFemale = self.femaleTick:isSelected(1)
	self.hairCombo.options = {}
	self.hairCombo.selected = 0
	if getAllHairStyles then
		local hairStyles = getAllHairStyles(isFemale)
		if hairStyles and hairStyles.size then
			for i = 1, hairStyles:size() do
				local styleId = hairStyles:get(i - 1)
				local label = styleId
				if label == "" then
					label = getText("IGUI_Hair_Bald")
				else
					label = getText("IGUI_Hair_" .. tostring(styleId))
				end
				self.hairCombo:addOptionWithData(label, tostring(styleId))
			end
		end
	end
	self.beardCombo.options = {}
	self.beardCombo.selected = 0
	if not isFemale and getAllBeardStyles then
		local beardStyles = getAllBeardStyles()
		if beardStyles and beardStyles.size then
			for i = 1, beardStyles:size() do
				local bid = beardStyles:get(i - 1)
				local label = bid
				if label == "" then
					label = getText("IGUI_Beard_None")
				else
					label = getText("IGUI_Beard_" .. tostring(bid))
				end
				self.beardCombo:addOptionWithData(label, tostring(bid))
			end
		end
	end
	self:refreshOutfitAppearanceVisibility()
end

function AdminUI:refreshOutfitAppearanceVisibility()
	local mode = self.avatarCombo and self:getCombo(self.avatarCombo) or Shared.AVATAR_MODE.NONE
	if mode ~= Shared.AVATAR_MODE.OUTFIT then
		self.labels.beardStyle:setVisible(false)
		self.beardCombo:setVisible(false)
		self.labels.beardRgb:setVisible(false)
		self.beardRgbField:setVisible(false)
		return
	end
	local showBeard = not self.femaleTick:isSelected(1)
	self.labels.beardStyle:setVisible(showBeard)
	self.beardCombo:setVisible(showBeard)
	self.labels.beardRgb:setVisible(showBeard)
	self.beardRgbField:setVisible(showBeard)
end

function AdminUI.onFemaleChanged(target)
	if not target then
		return
	end
	if target.rebuildOutfitHairBeardCombos then
		target:rebuildOutfitHairBeardCombos()
	end
	if target.layoutChildren then
		target:layoutChildren()
	end
end

function AdminUI:refreshAvatarFieldVisibility()
	local mode = self.avatarCombo and self:getCombo(self.avatarCombo) or Shared.AVATAR_MODE.NONE
	local showPreset = mode == Shared.AVATAR_MODE.PRESET
	local showPortrait = mode == Shared.AVATAR_MODE.PORTRAIT
	local showOutfit = mode == Shared.AVATAR_MODE.OUTFIT
	local showFemale = showOutfit

	self.labels.preset:setVisible(showPreset)
	self.presetCombo:setVisible(showPreset)
	self.presetBtn:setVisible(showPreset)
	self.labels.portrait:setVisible(showPortrait)
	self.portraitKindCombo:setVisible(showPortrait)
	self.labels.portraitKind:setVisible(showPortrait)
	self.portraitField:setVisible(showPortrait)
	self.labels.outfit:setVisible(showOutfit)
	self.outfitCombo:setVisible(showOutfit)

	self.femaleTick:setVisible(showFemale)

	local showApp = showOutfit
	self.labels.skinTone:setVisible(showApp)
	self.skinCombo:setVisible(showApp)
	self.labels.hairStyle:setVisible(showApp)
	self.hairCombo:setVisible(showApp)
	self.labels.hairRgb:setVisible(showApp)
	self.hairRgbField:setVisible(showApp)
	self:refreshOutfitAppearanceVisibility()
end

function AdminUI:applyTooltips()
	local function tip(w, text)
		if w and w.setTooltip then
			w:setTooltip(text)
		end
	end
	tip(self.labels.name, getText("IGUI_Tooltip_FieldName"))
	tip(self.nameField, getText("IGUI_Tooltip_FieldName_Detail"))
	tip(self.enabledTick, getText("IGUI_Tooltip_FieldEnabled_Detail"))
	tip(self.visibleTick, getText("IGUI_Tooltip_FieldVisible_Detail"))
	tip(self.labels.distance, getText("IGUI_Tooltip_FieldDistance"))
	tip(self.distanceField, getText("IGUI_Tooltip_FieldDistance_Detail"))
	tip(self.labels.access, getText("IGUI_Tooltip_FieldMinAccess"))
	tip(self.accessCombo, getText("IGUI_Tooltip_FieldMinAccess_Detail"))
	tip(self.labels.avatar, getText("IGUI_Tooltip_FieldAvatarMode"))
	tip(self.avatarCombo, getText("IGUI_Tooltip_FieldAvatarMode_Detail"))
	tip(self.labels.preset, getText("IGUI_Tooltip_FieldAvatarPreset"))
	tip(self.presetCombo, getText("IGUI_Tooltip_FieldAvatarPreset_Detail"))
	tip(self.presetBtn, getText("IGUI_Tooltip_PresetManager"))
	tip(self.labels.portraitKind, getText("IGUI_Tooltip_FieldPortraitKind"))
	tip(self.portraitKindCombo, getText("IGUI_Tooltip_FieldPortraitKind_Detail"))
	tip(self.labels.portrait, getText("IGUI_Tooltip_FieldPortrait"))
	tip(self.portraitField, getText("IGUI_Tooltip_FieldPortrait_Detail"))
	tip(self.labels.outfit, getText("IGUI_Tooltip_FieldOutfit"))
	tip(self.outfitCombo, getText("IGUI_Tooltip_FieldOutfit_Detail"))
	tip(self.femaleTick, getText("IGUI_Tooltip_FieldFemale_Detail"))
	tip(self.labels.skinTone, getText("IGUI_Tooltip_FieldSkinIndex"))
	tip(self.skinCombo, getText("IGUI_Tooltip_FieldSkinIndex_Detail"))
	tip(self.labels.hairStyle, getText("IGUI_Tooltip_FieldHairStyle"))
	tip(self.hairCombo, getText("IGUI_Tooltip_FieldHairStyle_Detail"))
	tip(self.labels.beardStyle, getText("IGUI_Tooltip_FieldBeardStyle"))
	tip(self.beardCombo, getText("IGUI_Tooltip_FieldBeardStyle_Detail"))
	tip(self.labels.hairRgb, getText("IGUI_Tooltip_FieldHairRgb"))
	tip(self.hairRgbField, getText("IGUI_Tooltip_FieldHairRgb_Detail"))
	tip(self.labels.beardRgb, getText("IGUI_Tooltip_FieldBeardRgb"))
	tip(self.beardRgbField, getText("IGUI_Tooltip_FieldBeardRgb_Detail"))
	tip(self.typewriterTick, getText("IGUI_Tooltip_FieldTypewriter_Detail"))
	tip(self.labels.speed, getText("IGUI_Tooltip_FieldTypeSpeed"))
	tip(self.speedField, getText("IGUI_Tooltip_FieldTypeSpeed_Detail"))
	tip(self.labels.openSound, getText("IGUI_Tooltip_FieldOpenSound"))
	tip(self.openSoundField, getText("IGUI_Tooltip_FieldOpenSound_Detail"))
	tip(self.labels.responseSound, getText("IGUI_Tooltip_FieldResponseSound"))
	tip(self.responseSoundField, getText("IGUI_Tooltip_FieldResponseSound_Detail"))
	tip(self.labels.nodeText, getText("IGUI_Tooltip_FieldNodeText"))
	tip(self.nodeTextField, getText("IGUI_Tooltip_FieldNodeText_Detail"))
	tip(self.labels.responseText, getText("IGUI_Tooltip_FieldResponseText"))
	tip(self.responseTextField, getText("IGUI_Tooltip_FieldResponseText_Detail"))
	tip(self.labels.playerText, getText("IGUI_Tooltip_FieldPlayerText"))
	tip(self.playerTextField, getText("IGUI_Tooltip_FieldPlayerText_Detail"))
	tip(self.labels.target, getText("IGUI_Tooltip_FieldTarget"))
	tip(self.targetCombo, getText("IGUI_Tooltip_FieldTarget_Detail"))
	tip(self.labels.action, getText("IGUI_Tooltip_FieldAction"))
	tip(self.actionCombo, getText("IGUI_Tooltip_FieldAction_Detail"))
	tip(self.labels.json, getText("IGUI_Tooltip_FieldJson"))
	tip(self.jsonField, getText("IGUI_Tooltip_FieldJson_Detail"))
	tip(self.newBtn, getText("IGUI_Tooltip_BtnNew"))
	tip(self.saveBtn, getText("IGUI_Tooltip_BtnSave"))
	tip(self.deleteBtn, getText("IGUI_Tooltip_BtnDelete"))
	tip(self.refreshBtn, getText("IGUI_Tooltip_BtnRefresh"))
	tip(self.teleportBtn, getText("IGUI_Tooltip_TeleportNpc"))
	tip(self.resetTreeBtn, getText("IGUI_Tooltip_ResetTree"))
	tip(self.exportBtn, getText("IGUI_Tooltip_ExportTree"))
	tip(self.importBtn, getText("IGUI_Tooltip_ImportTree"))
	tip(self.addNodeBtn, getText("IGUI_Tooltip_AddNode"))
	tip(self.removeNodeBtn, getText("IGUI_Tooltip_RemoveNode"))
	tip(self.addResponseBtn, getText("IGUI_Tooltip_AddResponse"))
	tip(self.removeResponseBtn, getText("IGUI_Tooltip_RemoveResponse"))
	tip(self.npcList, getText("IGUI_Tooltip_NpcList"))
	tip(self.nodeList, getText("IGUI_Tooltip_NodeList"))
	tip(self.responseList, getText("IGUI_Tooltip_ResponseList"))
	tip(self.formScroll, getText("IGUI_Tooltip_FormScroll"))
end

function AdminUI.onAvatarModeChanged(target)
	if target and target.refreshAvatarFieldVisibility then
		target:refreshAvatarFieldVisibility()
		if
			target.avatarCombo
			and target.getCombo
			and target:getCombo(target.avatarCombo) == Shared.AVATAR_MODE.OUTFIT
			and target.rebuildOutfitHairBeardCombos
		then
			target:rebuildOutfitHairBeardCombos()
		end
		target:layoutChildren()
	end
end

function AdminUI:onAdminNpcsReceived(npcs)
	self:rebuildPresetCombo()
	self.npcList:clear()
	for i = 1, #(npcs or {}) do
		self.npcList:addItem(npcs[i].name or "", npcs[i])
	end
	local matchedObject = false
	if self.pendingObjectRef then
		local key = Shared.objectKey(self.pendingObjectRef)
		for i = 1, #(npcs or {}) do
			if npcs[i].objectRef and npcs[i].objectRef.key == key then
				self.npcList.selected = i
				self:loadNpc(npcs[i])
				self.pendingObjectRef = nil
				matchedObject = true
				break
			end
		end
	end
	if matchedObject then
		self._forceSelectFirstOnNpcs = false
		return
	end
	if self._forceSelectFirstOnNpcs and npcs and #npcs > 0 then
		self._forceSelectFirstOnNpcs = false
		self.npcList.selected = 1
		self:loadNpc(npcs[1])
		return
	end
	if not self.selectedNpc and npcs and #npcs > 0 then
		self.npcList.selected = 1
		self:loadNpc(npcs[1])
	end
end

function AdminUI:rebuildPresetCombo(selectedId)
	if not self.presetCombo then
		return
	end
	selectedId = selectedId or (self.selectedNpc and self.selectedNpc.avatar and self.selectedNpc.avatar.presetId) or ""
	self.presetCombo.options = {}
	self.presetCombo.selected = 0
	self.presetCombo:addOptionWithData(getText("IGUI_INPC_TargetNone"), "")
	for i = 1, #(Client.avatarPresets or {}) do
		local preset = Client.avatarPresets[i]
		self.presetCombo:addOptionWithData(preset.name or preset.id, preset.id)
	end
	self:setCombo(self.presetCombo, selectedId)
end

function AdminUI:loadNpc(npcOrRow)
	self:saveCurrentNode()
	self:saveCurrentResponse()
	local npc = listRowPayload(npcOrRow)
	self.selectedNpc = npc
	self.selectedNodeId = nil
	self.selectedResponseIndex = nil
	self.nameField:setText(npc.name or "")
	self.enabledTick:setSelected(1, npc.enabled ~= false)
	self.visibleTick:setSelected(1, npc.visible ~= false)
	self.distanceField:setText(tostring((npc.settings and npc.settings.interactionDistance) or 2.5))
	self:setCombo(self.accessCombo, (npc.settings and npc.settings.minimumAccessLevel) or "None")
	self:setCombo(self.avatarCombo, (npc.avatar and npc.avatar.mode) or Shared.AVATAR_MODE.NONE)
	self:rebuildPresetCombo((npc.avatar and npc.avatar.presetId) or "")
	self:setCombo(self.portraitKindCombo, Shared.normalizePortraitSource(npc.avatar and npc.avatar.portraitSource))
	self.portraitField:setText((npc.avatar and npc.avatar.portrait) or "")
	OutfitCombo.ensureUnknownOutfitOption(self.outfitCombo, (npc.avatar and npc.avatar.outfit) or "")
	OutfitCombo.selectByOutfitName(self.outfitCombo, (npc.avatar and npc.avatar.outfit) or "")
	local mode = npc.avatar and npc.avatar.mode or Shared.AVATAR_MODE.NONE
	if mode == Shared.AVATAR_MODE.PRESET then
		self.femaleTick:setSelected(1, false)
	else
		self.femaleTick:setSelected(1, npc.avatar and npc.avatar.female == true)
	end
	if mode == Shared.AVATAR_MODE.OUTFIT then
		local ap = Shared.normalizeAppearance(npc.avatar and npc.avatar.appearance or {})
		local skinIdx = ap.skinTextureIndex
		if skinIdx == nil then
			skinIdx = 0
		end
		self:setComboOptionData(self.skinCombo, tonumber(skinIdx))
		if ap.hairR ~= nil and ap.hairG ~= nil and ap.hairB ~= nil then
			self.hairRgbField:setText(Shared.formatRgb255FromUnit(ap.hairR, ap.hairG, ap.hairB))
		else
			self.hairRgbField:setText("")
		end
		if ap.beardR ~= nil and ap.beardG ~= nil and ap.beardB ~= nil then
			self.beardRgbField:setText(Shared.formatRgb255FromUnit(ap.beardR, ap.beardG, ap.beardB))
		else
			self.beardRgbField:setText("")
		end
		self:rebuildOutfitHairBeardCombos()
		self:setComboOptionData(self.hairCombo, ap.hairModel)
		if not self.femaleTick:isSelected(1) then
			self:setComboOptionData(self.beardCombo, ap.beardModel)
		end
	else
		self:setComboOptionData(self.skinCombo, 0)
		self.hairRgbField:setText("")
		self.beardRgbField:setText("")
		self:rebuildOutfitHairBeardCombos()
	end
	self:refreshAvatarFieldVisibility()
	self.typewriterTick:setSelected(1, not (npc.settings and npc.settings.typewriter == false))
	self.speedField:setText(tostring((npc.settings and npc.settings.typewriterSpeed) or 45))
	self.openSoundField:setText((npc.settings and npc.settings.openSound) or "")
	self.responseSoundField:setText((npc.settings and npc.settings.responseSound) or "")
	self:rebuildNodeList()
	self:layoutChildren()
end

function AdminUI.onNpcSelected(list, row)
	if list and list.loadNpc then
		list:loadNpc(row)
	end
end

function AdminUI:currentTree()
	if not self.selectedNpc then
		return nil
	end
	self.selectedNpc.dialogue = Shared.normalizeTree(self.selectedNpc.dialogue)
	return self.selectedNpc.dialogue
end

function AdminUI:currentNode()
	local tree = self:currentTree()
	return tree and tree.nodes[self.selectedNodeId] or nil
end

function AdminUI:currentResponse()
	local node = self:currentNode()
	return node and node.responses and node.responses[self.selectedResponseIndex] or nil
end

function AdminUI:rebuildNodeList()
	self.nodeList:clear()
	local tree = self:currentTree()
	if not tree then
		return
	end
	local nodes = orderedNodes(tree)
	for i = 1, #nodes do
		self.nodeList:addItem(nodes[i].id, nodes[i])
	end
	if #nodes > 0 then
		self.nodeList.selected = 1
		self:loadNode(nodes[1])
	end
end

function AdminUI:loadNode(nodeOrRow)
	local node = listRowPayload(nodeOrRow)
	local nid = node and node.id or nil
	if nid ~= self.selectedNodeId then
		self:saveCurrentNode()
		self:saveCurrentResponse()
	end
	self.selectedNodeId = nid
	self.selectedResponseIndex = nil
	local tree = self:currentTree()
	local live = (tree and nid and tree.nodes[nid]) or node
	self.nodeTextField:setText(live and live.npcText or "")
	self:rebuildTargetCombo()
	self:rebuildResponseList()
end

function AdminUI:rebuildTargetCombo(selectedTarget)
	if not self.targetCombo then
		return
	end
	selectedTarget = selectedTarget or (self:currentResponse() and self:currentResponse().target) or ""
	self.targetCombo.options = {}
	self.targetCombo.selected = 0
	self.targetCombo:addOptionWithData(getText("IGUI_INPC_TargetNone"), "")
	local tree = self:currentTree()
	local nodes = orderedNodes(tree)
	for i = 1, #nodes do
		self.targetCombo:addOptionWithData(nodes[i].id, nodes[i].id)
	end
	self:setCombo(self.targetCombo, selectedTarget)
end

function AdminUI.onNodeSelected(list, row)
	if list and list.loadNode then
		list:loadNode(row)
	end
end

function AdminUI:rebuildResponseList()
	self.responseList:clear()
	local node = self:currentNode()
	for i = 1, #(node and node.responses or {}) do
		self.responseList:addItem(node.responses[i].text or "", node.responses[i])
	end
	if node and #node.responses > 0 then
		self.responseList.selected = 1
		self:loadResponse(1)
	else
		self:loadResponse(nil)
	end
end

function AdminUI:loadResponse(index)
	self.selectedResponseIndex = index
	local r = self:currentResponse()
	self.responseTextField:setText(r and r.text or "")
	self.playerTextField:setText(r and r.playerText or "")
	self:rebuildTargetCombo(r and r.target or "")
	self:setCombo(self.actionCombo, r and r.action or "")
end

function AdminUI.onResponseSelected(list, response)
	if list and list.saveCurrentResponse and list.saveCurrentNode and list.loadResponse then
		list:saveCurrentNode()
		list:saveCurrentResponse()
		list:loadResponse(list.responseList and list.responseList.selected or nil)
	end
end

function AdminUI:saveCurrentNode()
	local node = self:currentNode()
	if node then
		node.npcText = self.nodeTextField:getText()
	end
end

function AdminUI:saveCurrentResponse()
	local r = self:currentResponse()
	if r then
		r.text = self.responseTextField:getText()
		r.playerText = self.playerTextField:getText()
		r.target = self:getCombo(self.targetCombo)
		r.action = self:getCombo(self.actionCombo)
		if r.action == "" then
			r.action = nil
		end
	end
end

function AdminUI:collectNpc()
	if not self.selectedNpc then
		self.selectedNpc = Shared.makeNpc({ objectRef = self.pendingObjectRef })
	end
	self:saveCurrentNode()
	self:saveCurrentResponse()
	local npc = self.selectedNpc
	npc.name = self.nameField:getText()
	npc.enabled = self.enabledTick:isSelected(1)
	npc.visible = self.visibleTick:isSelected(1)
	local mode = self:getCombo(self.avatarCombo)
	local femaleForNpc = mode ~= Shared.AVATAR_MODE.PRESET and self.femaleTick:isSelected(1)
	local appearance = Shared.normalizeAppearance({})
	local prevAvatar = self.selectedNpc and self.selectedNpc.avatar or {}
	local prevOutfit = trimUi(tostring(prevAvatar.outfit or ""))
	local selectedOutfit = trimUi(tostring(OutfitCombo.getSelectedOutfitName(self.outfitCombo)))
	local keptOutfitWear = nil
	if
		mode == Shared.AVATAR_MODE.OUTFIT
		and prevOutfit ~= ""
		and prevOutfit == selectedOutfit
		and type(prevAvatar.outfitWear) == "table"
		and prevAvatar.female == femaleForNpc
	then
		keptOutfitWear = prevAvatar.outfitWear
	end
	if mode == Shared.AVATAR_MODE.OUTFIT then
		local hr, hg, hb = Shared.parseAppearanceRgbInput(self.hairRgbField:getText())
		local br, bg, bb = Shared.parseAppearanceRgbInput(self.beardRgbField:getText())
		local beardVal = nil
		if not self.femaleTick:isSelected(1) then
			beardVal = self:getCombo(self.beardCombo)
		end
		appearance = Shared.normalizeAppearance({
			skinTextureIndex = self:getCombo(self.skinCombo),
			hairModel = self:getCombo(self.hairCombo),
			beardModel = beardVal,
			hairR = hr,
			hairG = hg,
			hairB = hb,
			beardR = br,
			beardG = bg,
			beardB = bb,
		})
	end
	npc.avatar = {
		mode = mode,
		portrait = self.portraitField:getText(),
		portraitSource = mode == Shared.AVATAR_MODE.PORTRAIT and self:getCombo(self.portraitKindCombo)
			or Shared.PORTRAIT_SOURCE.UI_IMAGE,
		outfit = OutfitCombo.getSelectedOutfitName(self.outfitCombo),
		presetId = self:getCombo(self.presetCombo),
		female = femaleForNpc,
		appearance = appearance,
		outfitWear = mode == Shared.AVATAR_MODE.OUTFIT and keptOutfitWear or nil,
	}
	if mode == Shared.AVATAR_MODE.OUTFIT and trimUi(tostring(npc.avatar.outfit or "")) ~= "" then
		AvatarUtils.ensureOutfitWearSnapshot(npc.avatar)
	end
	npc.settings = {
		interactionDistance = tonumber(self.distanceField:getText()) or 2.5,
		minimumAccessLevel = self:getCombo(self.accessCombo),
		typewriter = self.typewriterTick:isSelected(1),
		typewriterSpeed = tonumber(self.speedField:getText()) or 45,
		openSound = self.openSoundField:getText(),
		responseSound = self.responseSoundField:getText(),
	}
	npc.dialogue = Shared.normalizeTree(npc.dialogue)
	return npc
end

function AdminUI:onNew()
	self.selectedNpc = Shared.makeNpc({ objectRef = self.pendingObjectRef, name = getText("IGUI_INPC_DefaultName") })
	self:loadNpc(self.selectedNpc)
end

function AdminUI:onSave()
	local npc = self:collectNpc()
	local ok, err = Shared.validateNpc(npc)
	if not ok then
		self:showMessage(err)
		return
	end
	Client.adminSave(npc)
end

function AdminUI:onDelete()
	if not self.selectedNpc then
		self:showMessage(getText("IGUI_INPC_ErrSelectNpc"))
		return
	end
	local modal = ISModalDialog:new(
		0,
		0,
		320,
		90,
		"Delete " .. tostring(self.selectedNpc.name) .. "?",
		true,
		self,
		AdminUI.onConfirmDelete
	)
	modal:initialise()
	modal:addToUIManager()
end

function AdminUI.onConfirmDelete(target, button)
	if button.internal == "YES" and target.selectedNpc then
		Client.adminDelete(target.selectedNpc.id)
		target.selectedNpc = nil
		target:clearFields()
	end
end

function AdminUI:onRefresh()
	Client.requestAdmin()
end

function AdminUI:onAvatarPresets()
	local AvatarPresetUI = require("InteractiveNPCs/UI/AvatarPresetUI")
	AvatarPresetUI.open()
end

function AdminUI:onAddNode()
	local tree = self:currentTree()
	if not tree then
		return
	end
	local id = "node_" .. tostring(#orderedNodes(tree) + 1)
	while tree.nodes[id] do
		id = id .. "_"
	end
	tree.nodes[id] = { id = id, npcText = "New dialogue.", responses = { { text = "Back", action = "back" } } }
	self:rebuildNodeList()
end

function AdminUI:onRemoveNode()
	local tree = self:currentTree()
	if not tree or not self.selectedNodeId or self.selectedNodeId == tree.root then
		return
	end
	local deletedId = self.selectedNodeId
	tree.nodes[self.selectedNodeId] = nil
	local nodeIds = Shared.tableKeys(tree.nodes)
	for ni = 1, #nodeIds do
		local node = tree.nodes[nodeIds[ni]]
		local resps = node.responses or {}
		for ri = 1, #resps do
			local response = resps[ri]
			if response.target == deletedId then
				response.target = ""
				if not response.action or response.action == "" then
					response.action = "back"
				end
			end
		end
	end
	self.selectedNodeId = nil
	self:rebuildNodeList()
end

function AdminUI:onAddResponse()
	local node = self:currentNode()
	if not node then
		self:showMessage(getText("IGUI_INPC_ErrSelectNode"))
		return
	end
	node.responses = node.responses or {}
	node.responses[#node.responses + 1] = { id = Shared.generateId("response"), text = "Continue", target = "" }
	self:rebuildResponseList()
	self.responseList.selected = #node.responses
	self:loadResponse(#node.responses)
end

function AdminUI:onRemoveResponse()
	local node = self:currentNode()
	if not node or not self.selectedResponseIndex then
		return
	end
	table.remove(node.responses, self.selectedResponseIndex)
	self.selectedResponseIndex = nil
	self:rebuildResponseList()
end

function AdminUI:onResetTree()
	if not self.selectedNpc then
		return
	end
	self.selectedNpc.dialogue = Shared.makeDefaultTree("Hello.")
	self:rebuildNodeList()
end

function AdminUI:onExportTree()
	if not self.selectedNpc then
		return
	end
	self:saveCurrentNode()
	self:saveCurrentResponse()
	local content = JSON.stringify(self.selectedNpc.dialogue)
	if content then
		self.jsonField:setText(content)
	end
end

function AdminUI:onImportTree()
	if not self.selectedNpc then
		return
	end
	local text = self.jsonField:getText()
	if not text or trimUi(text) == "" then
		self:showMessage(getText("IGUI_INPC_ErrJsonEmpty"))
		return
	end
	local data = JSON.parse(text)
	if type(data) ~= "table" then
		self:showMessage(getText("IGUI_INPC_ErrJsonInvalid"))
		return
	end
	self.selectedNpc.dialogue = Shared.normalizeTree(data)
	local canon = JSON.stringify(self.selectedNpc.dialogue)
	if canon then
		self.jsonField:setText(canon)
	end
	self:rebuildNodeList()
end

function AdminUI:clearFields()
	self.nameField:setText("")
	self.nodeList:clear()
	self.responseList:clear()
	self.nodeTextField:setText("")
	self.responseTextField:setText("")
	self.playerTextField:setText("")
	if self.targetCombo then
		self:rebuildTargetCombo("")
	end
	self.jsonField:setText("")
end

function AdminUI:showMessage(msg)
	local modal = ISModalDialog:new(0, 0, 340, 90, msg or "", false, self, nil)
	modal:initialise()
	modal:addToUIManager()
end

function AdminUI:onResize()
	ISCollapsableWindow.onResize(self)
	self:layoutChildren()
end

function AdminUI:update()
	ISCollapsableWindow.update(self)
	local idx = self.selectedResponseIndex
	if not idx or idx < 1 or not self.responseList or not self.responseTextField then
		return
	end
	if not self.responseList.selected or self.responseList.selected ~= idx then
		return
	end
	local items = self.responseList.items
	if not items then
		return
	end
	local row = items[idx]
	if not row then
		return
	end
	local t = self.responseTextField:getText()
	if row.text == t then
		return
	end
	row.text = t
	local resp = row.item
	if resp and type(resp) == "table" then
		resp.text = t
	end
end

function AdminUI:onTeleportToNpc()
	local npc = self.selectedNpc
	if not npc then
		self:showMessage(getText("IGUI_INPC_ErrSelectNpc"))
		return
	end
	local ref = npc.objectRef
	if type(ref) ~= "table" or ref.x == nil or ref.y == nil then
		self:showMessage(getText("IGUI_INPC_ErrTeleportNoBound"))
		return
	end
	local WorldUtils = require("ElyonLib/WorldUtils/WorldUtils")
	local sq = WorldUtils.getSquareFromWorldCoords(ref.x, ref.y, ref.z)
	if not sq then
		self:showMessage(getText("IGUI_INPC_ErrTeleportNoSquare"))
		return
	end
	local player = getPlayer()
	if not player then
		return
	end
	WorldUtils.teleportPlayerToSquare(player, sq)
end

function AdminUI:layoutChildren()
	self:refreshAvatarFieldVisibility()
	local pad = C.PAD
	local th = self:titleBarHeight()
	local bottom = self:resizeBottomInset()
	local top = th + pad
	local fh = C.FOOTER_BTN_H
	local fgap = C.FOOTER_ROW_GAP
	local footerTotalH = fh * 2 + fgap + pad
	local footerTop = self.height - bottom - footerTotalH
	local listGap = C.LIST_ABOVE_FOOTER_PAD
	local listH = footerTop - top - listGap
	Layout.setBounds(self.npcList, pad, top, C.LIST_W, math.max(80, listH))

	local bw3 = math.floor((C.LIST_W - pad * 4) / 3)
	local row1y = footerTop
	Layout.setBounds(self.newBtn, pad, row1y, bw3, fh)
	Layout.setBounds(self.saveBtn, pad + bw3 + pad, row1y, bw3, fh)
	Layout.setBounds(self.deleteBtn, pad + (bw3 + pad) * 2, row1y, bw3, fh)

	local bw2 = math.floor((C.LIST_W - pad * 3) / 2)
	local row2y = footerTop + fh + fgap
	Layout.setBounds(self.refreshBtn, pad, row2y, bw2, fh)
	Layout.setBounds(self.teleportBtn, pad + bw2 + pad, row2y, bw2, fh)

	local formX = pad + C.LIST_W + pad
	local formW = math.max(260, math.floor((self.width - formX - C.NODE_W - pad * 4) * 0.48))
	local treeX = formX + formW + pad

	local formScrollH = footerTop - top
	Layout.setBounds(self.formScroll, formX, top, formW, formScrollH)
	if self.formScroll.javaObject then
		self.formScroll:setScrollChildren(true)
	end
	self.formScroll:onResize()
	self:layoutFormScroll()
	self.formScroll:onResize()
	self:layoutFormScroll()
	UIHelpers.syncScrollingListScrollbar(self.formScroll)

	local treeTop = top
	local nodeH = math.max(100, math.floor((footerTop - treeTop - pad * 3) * 0.28))
	local nodeBtnStackH = C.NODE_BTN_H * 2 + pad
	Layout.setBounds(self.nodeList, treeX, treeTop, C.NODE_W, nodeH)
	local nbY = treeTop + nodeH + pad
	Layout.setBounds(self.addNodeBtn, treeX, nbY, C.NODE_W, C.NODE_BTN_H)
	Layout.setBounds(self.removeNodeBtn, treeX, nbY + C.NODE_BTN_H + pad, C.NODE_W, C.NODE_BTN_H)

	local editX = treeX + C.NODE_W + pad
	local editW = self.width - editX - pad
	local ey = treeTop
	Layout.setBounds(self.labels.nodeText, editX, ey, editW, C.LBL_H)
	ey = ey + C.LBL_H + 2
	local nodeTextH = nodeH + nodeBtnStackH + pad - C.LBL_H - 2
	Layout.setBounds(self.nodeTextField, editX, ey, editW, nodeTextH)
	local nodeEditBottom = ey + nodeTextH + pad

	local respTop = treeTop + nodeH + nodeBtnStackH + pad * 2 + C.NODE_RESP_GAP
	local respH = math.max(80, math.floor((footerTop - respTop - pad * 2) * 0.34))
	Layout.setBounds(self.responseList, treeX, respTop, C.NODE_W, respH)
	Layout.setBounds(self.addResponseBtn, treeX, respTop + respH + pad, math.floor((C.NODE_W - pad) / 2), C.NODE_BTN_H)
	Layout.setBounds(
		self.removeResponseBtn,
		treeX + math.floor((C.NODE_W - pad) / 2) + pad,
		respTop + respH + pad,
		math.floor((C.NODE_W - pad) / 2),
		C.NODE_BTN_H
	)

	local ry = respTop
	local function rfield(label, control)
		Layout.setBounds(label, editX, ry, editW, C.LBL_H)
		ry = ry + C.LBL_H + 2
		Layout.setBounds(control, editX, ry, editW, C.FIELD_H)
		ry = ry + C.FIELD_H + pad
	end
	rfield(self.labels.responseText, self.responseTextField)
	rfield(self.labels.playerText, self.playerTextField)
	rfield(self.labels.target, self.targetCombo)
	rfield(self.labels.action, self.actionCombo)

	local jsonTop = math.max(ry + pad, nodeEditBottom, respTop + respH + C.NODE_BTN_H + pad * 2)
	if jsonTop + 80 > footerTop then
		jsonTop = math.max(treeTop, footerTop - 120)
	end
	local smallW = math.floor((editW - pad * 2) / 3)
	Layout.setBounds(self.resetTreeBtn, editX, jsonTop, smallW, C.BTN_H)
	Layout.setBounds(self.exportBtn, editX + smallW + pad, jsonTop, smallW, C.BTN_H)
	Layout.setBounds(self.importBtn, editX + (smallW + pad) * 2, jsonTop, smallW, C.BTN_H)
	jsonTop = jsonTop + C.BTN_H + pad
	Layout.setBounds(self.labels.json, editX, jsonTop, editW, C.LBL_H)
	jsonTop = jsonTop + C.LBL_H + 2
	Layout.setBounds(self.jsonField, editX, jsonTop, editW, math.max(44, footerTop - jsonTop))
	UIHelpers.syncScrollingListScrollbar(self.npcList)
	UIHelpers.syncScrollingListScrollbar(self.nodeList)
	UIHelpers.syncScrollingListScrollbar(self.responseList)
	UIHelpers.syncScrollingListScrollbar(self.formScroll)
end

function AdminUI:close()
	ISCollapsableWindow.close(self)
	if AdminUI.instance == self then
		AdminUI.instance = nil
	end
	Client.uiRef = nil
end

function AdminUI.open(pendingObjectRef)
	if AdminUI.instance then
		if pendingObjectRef ~= nil then
			AdminUI.instance.pendingObjectRef = pendingObjectRef
			AdminUI.instance._forceSelectFirstOnNpcs = false
		else
			AdminUI.instance.pendingObjectRef = nil
			AdminUI.instance._forceSelectFirstOnNpcs = true
		end
		AdminUI.instance:bringToTop()
		if pendingObjectRef then
			Client.adminMarkObject(pendingObjectRef)
		end
		Client.requestAdmin()
		return AdminUI.instance
	end
	local x, y, w, h = Layout.defaultWindowGeometry(C.DEF_W, C.DEF_H, C.MIN_W, C.MIN_H, 24)
	local ui = AdminUI:new(x, y, w, h, pendingObjectRef)
	AdminUI.instance = ui
	ui._forceSelectFirstOnNpcs = pendingObjectRef == nil
	ui:initialise()
	ui:addToUIManager()
	if pendingObjectRef then
		Client.adminMarkObject(pendingObjectRef)
	end
	return ui
end

return AdminUI
