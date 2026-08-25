require("ISUI/ISCollapsableWindow")
require("ISUI/ISTextEntryBox")
require("ISUI/ISButton")
require("ISUI/ISComboBox")
require("ISUI/ISTickBox")
require("ISUI/ISUI3DModel")
require("ISUI/ISPanel")

local Shared = require("InteractiveNPCs/Shared")
local AvatarUtils = require("InteractiveNPCs/AvatarUtils")
local Client = require("InteractiveNPCs/Client")
local Theme = require("ElyonLib/UI/Theme/Theme")
local Layout = require("ElyonLib/UI/Layout/LayoutUtils")
local TextUtils = require("ElyonLib/TextUtils/TextUtils")
local UIHelpers = require("InteractiveNPCs/UI/UIHelpers")
local STScrollingListBox = require("InteractiveNPCs/UI/STScrollingListBox")
local OutfitCombo = require("InteractiveNPCs/UI/OutfitCombo")

local ScrollClipPanel = ISPanel:derive("InteractiveNPCsAvatarScrollClip")

function ScrollClipPanel:prerender()
	ISPanel.prerender(self)
	self:setStencilRect(0, 0, self:getWidth(), self:getHeight())
end

function ScrollClipPanel:render()
	ISPanel.render(self)
	self:clearStencilRect()
	if self.doRepaintStencil then
		self:repaintStencilRect(0, 0, self:getWidth(), self:getHeight())
	end
end

local T = Theme.colors

local C = {
	PAD = 8,
	BTN_H = 22,
	LBL_H = 16,
	FIELD_H = 22,
	MIN_W = 820,
	MIN_H = 500,
	DEF_W = 920,
	DEF_H = 580,
	LIST_W = 200,
	PREVIEW_W = 220,
}

local BODY_LOCATIONS = {
	"Hat",
	"FullHat",
	"Mask",
	"MaskEyes",
	"MaskFull",
	"Eyes",
	"Shirt",
	"Tshirt",
	"TankTop",
	"Sweater",
	"Jacket",
	"Jacket_Bulky",
	"TorsoExtra",
	"TorsoExtraVest",
	"Hands",
	"Pants",
	"Skirt",
	"Dress",
	"Boilersuit",
	"FullSuit",
	"Shoes",
	"Back",
	"Neck",
	"Scarf",
}

local SKIN_TONE_COUNT = 5

local function listRowPayload(row)
	if type(row) == "table" and row.item ~= nil then
		return row.item
	end
	return row
end

local AvatarPresetUI = ISCollapsableWindow:derive("InteractiveNPCsAvatarPresetUI")
AvatarPresetUI.instance = nil

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

local function makeCombo(parent, target, onChange)
	local combo = ISComboBox:new(0, 0, 100, C.FIELD_H, target, onChange)
	combo:initialise()
	combo:instantiate()
	Theme.applyComboStyle(combo)
	parent:addChild(combo)
	return combo
end

local SlotList = STScrollingListBox:derive("InteractiveNPCsAvatarSlotList")
function SlotList:doDrawItem(y, item, alt)
	if item.index == self.selected then
		self:drawRect(0, y, self.width, item.height, T.selected.a, T.selected.r, T.selected.g, T.selected.b)
	elseif alt then
		self:drawRect(0, y, self.width, item.height, T.listAlt.a, T.listAlt.r, T.listAlt.g, T.listAlt.b)
	end
	local slot = item.item
	local split = math.floor(self.width * 0.38)
	local t1 = TextUtils.trimToWidth(UIFont.Small, tostring(slot.bodyLocation or ""), split - 10)
	local t2 = TextUtils.trimToWidth(UIFont.Small, tostring(slot.fullType or ""), self.width - split - 10)
	self:drawText(t1, 6, y + 4, T.text.r, T.text.g, T.text.b, 1, UIFont.Small)
	self:drawText(t2, split, y + 4, T.textDim.r, T.textDim.g, T.textDim.b, 1, UIFont.Small)
	return y + item.height
end

local MakeupAppliedList = STScrollingListBox:derive("InteractiveNPCsMakeupAppliedList")
function MakeupAppliedList:doDrawItem(y, item, alt)
	if item.index == self.selected then
		self:drawRect(0, y, self.width, item.height, T.selected.a, T.selected.r, T.selected.g, T.selected.b)
	elseif alt then
		self:drawRect(0, y, self.width, item.height, T.listAlt.a, T.listAlt.r, T.listAlt.g, T.listAlt.b)
	end
	local ft = item.item
	local disp = ft
	local si = ScriptManager and ScriptManager.instance and ScriptManager.instance:FindItem(ft)
	if si then
		disp = si:getDisplayName()
	end
	self:drawText(
		TextUtils.trimToWidth(UIFont.Small, disp, self.width - 12),
		6,
		y + 3,
		T.text.r,
		T.text.g,
		T.text.b,
		1,
		UIFont.Small
	)
	return y + item.height
end

local PresetList = STScrollingListBox:derive("InteractiveNPCsAvatarPresetList")
function PresetList:doDrawItem(y, item, alt)
	if item.index == self.selected then
		self:drawRect(0, y, self.width, item.height, T.selected.a, T.selected.r, T.selected.g, T.selected.b)
	elseif alt then
		self:drawRect(0, y, self.width, item.height, T.listAlt.a, T.listAlt.r, T.listAlt.g, T.listAlt.b)
	end
	local name = item.text or tostring(item.item and (item.item.name or item.item.id) or "")
	local text = TextUtils.trimToWidth(UIFont.Small, name, self.width - 14)
	local fh = getTextManager():getFontHeight(UIFont.Small)
	local ty = y + (item.height - fh) / 2
	self:drawText(text, 8, ty, T.text.r, T.text.g, T.text.b, 1, UIFont.Small)
	return y + item.height
end

function AvatarPresetUI:new(x, y, w, h)
	local o = ISCollapsableWindow.new(self, x, y, w, h)
	o.title = getText("IGUI_INPC_AvatarPresets")
	o.resizable = true
	o.moveWithMouse = true
	o.minimumWidth = C.MIN_W
	o.minimumHeight = C.MIN_H
	o.borderColor = Theme.copy(T.border)
	o.backgroundColor = Theme.copy(T.background)
	o.selectedPreset = nil
	o._rgbSig = ""
	o._rgbTick = 0
	return o
end

function AvatarPresetUI:initialise()
	ISCollapsableWindow.initialise(self)

	self.presetList = PresetList:new(C.PAD, self:titleBarHeight() + C.PAD, C.LIST_W, 200)
	self.presetList:initialise()
	self.presetList.itemheight = 32
	self.presetList.target = self
	self.presetList.onmousedown = AvatarPresetUI.onPresetSelected
	Theme.applyListStyle(self.presetList)
	self.presetList.drawBorder = true
	self:addChild(self.presetList)

	self.newBtn = makeButton(self, getText("IGUI_INPC_New"), self, AvatarPresetUI.onNew, "primary")
	self.saveBtn = makeButton(self, getText("IGUI_INPC_Save"), self, AvatarPresetUI.onSave, "success")
	self.deleteBtn = makeButton(self, getText("IGUI_INPC_Delete"), self, AvatarPresetUI.onDelete, "danger")

	-- vanilla scroll + stencil repaint so nested widgets clip inside the viewport.
	self.formPane = ScrollClipPanel:new(C.PAD + C.LIST_W + C.PAD, self:titleBarHeight() + C.PAD, 400, 300)
	self.formPane:initialise()
	self.formPane:noBackground()
	self.formPane.borderColor = { r = 0, g = 0, b = 0, a = 0 }
	self.formPane.doRepaintStencil = true
	self.formPane:addScrollBars()
	if self.formPane.vscroll then
		self.formPane.vscroll.doSetStencil = true
		self.formPane.vscroll.doRepaintStencil = true
	end
	self:addChild(self.formPane)
	function self.formPane:onMouseWheel(del)
		if self:getScrollHeight() > self:getHeight() then
			self:setYScroll(self:getYScroll() - del * 24)
			return true
		end
		return false
	end

	local sp = self.formPane

	self.labels = {}
	self.labels.name = makeLabel(sp, getText("IGUI_INPC_FieldName"))
	self.nameField = makeField(sp)
	self.femaleTick = ISTickBox:new(0, 0, 200, C.FIELD_H, "", self, AvatarPresetUI.onFemaleChanged)
	self.femaleTick:initialise()
	self.femaleTick:instantiate()
	self.femaleTick:addOption(getText("IGUI_INPC_FieldFemale"))
	Theme.applyTickBoxStyle(self.femaleTick)
	sp:addChild(self.femaleTick)

	self.labels.outfit = makeLabel(sp, getText("IGUI_INPC_FieldBaseOutfit"))
	self.outfitCombo = makeCombo(sp, self, AvatarPresetUI.onComboLivePreview)
	OutfitCombo.populate(self.outfitCombo)

	self.labels.skinTone = makeLabel(sp, getText("UI_SkinColor"))
	self.skinCombo = makeCombo(sp, self, AvatarPresetUI.onComboLivePreview)
	for i = 1, SKIN_TONE_COUNT do
		self.skinCombo:addOptionWithData(getText("UI_SkinColor") .. " (" .. tostring(i) .. ")", i - 1)
	end

	self.labels.hairStyle = makeLabel(sp, getText("IGUI_INPC_FieldHairStyle"))
	self.hairCombo = makeCombo(sp, self, AvatarPresetUI.onComboLivePreview)
	self.labels.beardStyle = makeLabel(sp, getText("IGUI_INPC_FieldBeardStyle"))
	self.beardCombo = makeCombo(sp, self, AvatarPresetUI.onComboLivePreview)

	self.labels.hairRgb = makeLabel(sp, getText("IGUI_INPC_FieldHairRgb"))
	self.hairRgbField = makeField(sp)
	self.labels.beardRgb = makeLabel(sp, getText("IGUI_INPC_FieldBeardRgb"))
	self.beardRgbField = makeField(sp)

	self.labels.makeupCat = makeLabel(sp, getText("IGUI_INPC_MakeupCategory"))
	self.makeupCategoryCombo = makeCombo(sp, self, AvatarPresetUI.onMakeupCategoryCombo)
	self.labels.makeupPick = makeLabel(sp, getText("IGUI_INPC_MakeupPick"))
	self.makeupItemCombo = makeCombo(sp, self, AvatarPresetUI.onComboLivePreview)
	self.makeupAddBtn = makeButton(sp, getText("IGUI_INPC_MakeupAdd"), self, AvatarPresetUI.onMakeupAdd, "primary")
	self.makeupRemoveBtn =
		makeButton(sp, getText("IGUI_INPC_MakeupRemove"), self, AvatarPresetUI.onMakeupRemove, "danger")

	self.labels.makeupApplied = makeLabel(sp, getText("IGUI_INPC_MakeupApplied"))
	self.makeupAppliedList = MakeupAppliedList:new(0, 0, 100, 72)
	self.makeupAppliedList:initialise()
	self.makeupAppliedList.itemheight = 22
	self.makeupAppliedList.drawBorder = true
	Theme.applyListStyle(self.makeupAppliedList)
	sp:addChild(self.makeupAppliedList)

	self.labels.location = makeLabel(sp, getText("IGUI_INPC_FieldBodyLocation"))
	self.locationCombo = makeCombo(sp, self, AvatarPresetUI.onLocationComboChanged)
	for i = 1, #BODY_LOCATIONS do
		self.locationCombo:addOptionWithData(BODY_LOCATIONS[i], BODY_LOCATIONS[i])
	end

	self.labels.item = makeLabel(sp, getText("IGUI_INPC_FieldClothingItem"))
	self.itemCombo = makeCombo(sp, self, AvatarPresetUI.onComboLivePreview)

	self.slotList = SlotList:new(0, 0, 100, 100)
	self.slotList:initialise()
	self.slotList.itemheight = 26
	self.slotList.target = self
	self.slotList.onmousedown = AvatarPresetUI.onSlotSelected
	Theme.applyListStyle(self.slotList)
	self.slotList.drawBorder = true
	sp:addChild(self.slotList)

	self.addSlotBtn = makeButton(sp, getText("IGUI_INPC_AddSlot"), self, AvatarPresetUI.onAddSlot, "primary")
	self.removeSlotBtn = makeButton(sp, getText("IGUI_INPC_RemoveSlot"), self, AvatarPresetUI.onRemoveSlot, "danger")
	self.previewBtn = makeButton(sp, getText("IGUI_INPC_Preview"), self, AvatarPresetUI.onPreview)

	self.previewPanel = ISPanel:new(0, 0, C.PREVIEW_W, 240)
	self.previewPanel:initialise()
	self.previewPanel.backgroundColor = Theme.copy(T.panelDark)
	self.previewPanel.borderColor = Theme.copy(T.borderDim)
	self:addChild(self.previewPanel)
	self.model = ISUI3DModel:new(0, 0, C.PREVIEW_W - C.PAD * 2, 220)
	self.model:initialise()
	self.model:instantiate()
	self.model:setVisible(false)
	self.model:setState("idle")
	self.model:setDirection(IsoDirections.S)
	self.model:setIsometric(false)
	self.model:setZoom(15)
	self.model:setYOffset(-0.8)
	self.previewPanel:addChild(self.model)

	self:initMakeupCategories()
	self:refreshPresetList()
	self:onBodyLocationChanged()
	self:layoutChildren()
end

function AvatarPresetUI:initMakeupCategories()
	if not MakeUpDefinitions or not MakeUpDefinitions.categories then
		return
	end
	self.makeupCategoryCombo.options = {}
	self.makeupCategoryCombo.selected = 0
	self.makeupCategoryCombo:addOptionWithData(getText("IGUI_SelectBodyLocation"), nil)
	local cats = {}
	local catMap = MakeUpDefinitions.categories
	local ck = Shared.tableKeys(catMap)
	for i = 1, #ck do
		cats[#cats + 1] = catMap[ck[i]]
	end
	table.sort(cats, function(a, b)
		return tostring(a.name) < tostring(b.name)
	end)
	for i = 1, #cats do
		local v = cats[i]
		self.makeupCategoryCombo:addOptionWithData(getText("MakeUpCategory_" .. v.name), v)
	end
	self.makeupCategoryCombo.selected = 1
	self:rebuildMakeupItemCombo()
end

function AvatarPresetUI.onLocationComboChanged(target)
	if target and target.onBodyLocationChanged then
		target:onBodyLocationChanged()
	end
end

function AvatarPresetUI.onComboLivePreview(target)
	if target and target.onPreview then
		target:onPreview()
	end
end

function AvatarPresetUI.onMakeupCategoryCombo(target)
	if target and target.rebuildMakeupItemCombo then
		target:rebuildMakeupItemCombo()
	end
end

function AvatarPresetUI:rebuildMakeupItemCombo()
	self.makeupItemCombo.options = {}
	self.makeupItemCombo.selected = 0
	self.makeupItemCombo:addOptionWithData(getText("IGUI_SelectMakeUp"), nil)
	local cat = self:getCombo(self.makeupCategoryCombo)
	if not cat or not MakeUpDefinitions or not MakeUpDefinitions.makeup then
		self.makeupItemCombo.selected = 1
		return
	end
	local makeupDefs = MakeUpDefinitions.makeup
	for i = 1, #makeupDefs do
		local def = makeupDefs[i]
		if def.category == cat.category then
			local name = getText("MakeUpType_" .. def.name)
			self.makeupItemCombo:addOptionWithData(name, def)
		end
	end
	self.makeupItemCombo.selected = 1
end

function AvatarPresetUI:getCombo(combo)
	return combo:getOptionData(combo.selected)
end

function AvatarPresetUI:setCombo(combo, data)
	for i = 1, #combo.options do
		if combo:getOptionData(i) == data then
			combo.selected = i
			return
		end
	end
	combo.selected = 1
end

function AvatarPresetUI:setComboOptionData(combo, data)
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

function AvatarPresetUI:rebuildHairBeardCombos()
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
	self:refreshBeardVisibility()
end

function AvatarPresetUI:refreshBeardVisibility()
	local show = not self.femaleTick:isSelected(1)
	self.labels.beardStyle:setVisible(show)
	self.beardCombo:setVisible(show)
	self.labels.beardRgb:setVisible(show)
	self.beardRgbField:setVisible(show)
end

function AvatarPresetUI.onFemaleChanged(target)
	if not target then
		return
	end
	target:rebuildHairBeardCombos()
	target:layoutChildren()
	target:onPreview()
end

function AvatarPresetUI:ensureAppearanceSlots()
	if not self.selectedPreset then
		self.selectedPreset = Shared.normalizeAvatarPreset({ name = getText("IGUI_INPC_NewAvatarPreset") })
	end
	self.selectedPreset.appearance = self.selectedPreset.appearance or Shared.normalizeAppearance({})
	if not self.selectedPreset.appearance.makeupFullTypes then
		self.selectedPreset.appearance.makeupFullTypes = {}
	end
	return self.selectedPreset.appearance.makeupFullTypes
end

function AvatarPresetUI:rebuildMakeupAppliedList()
	self.makeupAppliedList:clear()
	local list = self.selectedPreset
		and self.selectedPreset.appearance
		and self.selectedPreset.appearance.makeupFullTypes
		or {}
	for i = 1, #list do
		local ft = list[i]
		self.makeupAppliedList:addItem(ft, ft)
	end
end

function AvatarPresetUI:onMakeupAdd()
	local def = self:getCombo(self.makeupItemCombo)
	if not def or not def.item or not InventoryItemFactory then
		return
	end
	local item = InventoryItemFactory.CreateItem(def.item)
	if not item or not item.getBodyLocation then
		return
	end
	local loc = item:getBodyLocation()
	local slots = self:ensureAppearanceSlots()
	for idx = #slots, 1, -1 do
		local ex = InventoryItemFactory.CreateItem(slots[idx])
		if ex and ex:getBodyLocation() == loc then
			table.remove(slots, idx)
		end
	end
	slots[#slots + 1] = def.item
	self:rebuildMakeupAppliedList()
	self:onPreview()
end

function AvatarPresetUI:onMakeupRemove()
	if not self.makeupAppliedList.selected or self.makeupAppliedList.selected < 1 then
		return
	end
	local slots = self:ensureAppearanceSlots()
	table.remove(slots, self.makeupAppliedList.selected)
	self:rebuildMakeupAppliedList()
	self:onPreview()
end

function AvatarPresetUI:refreshPresetList()
	self.presetList:clear()
	for i = 1, #(Client.avatarPresets or {}) do
		local preset = Client.avatarPresets[i]
		self.presetList:addItem(preset.name or preset.id, preset)
	end
	-- no saved presets: behave like pressing New so the form and hair/beard combos are initialized.
	if #self.presetList.items < 1 then
		self.presetList.selected = 0
		self:onNew()
		return
	end
	local sel = self.presetList.selected
	local ok = type(sel) == "number" and sel >= 1 and sel <= #self.presetList.items
	if not ok then
		self:selectFirstPresetIfAny()
	end
end

function AvatarPresetUI:selectFirstPresetIfAny()
	if not self.presetList or not self.presetList.items or #self.presetList.items < 1 then
		return
	end
	self.presetList.selected = 1
	local first = self.presetList.items[1]
	local preset = first and first.item
	if preset then
		self:loadPreset(preset)
	end
end

function AvatarPresetUI:loadPreset(preset)
	self.selectedPreset = Shared.normalizeAvatarPreset(preset)
	self.nameField:setText(self.selectedPreset.name or "")
	self.femaleTick:setSelected(1, self.selectedPreset.female == true)
	OutfitCombo.ensureUnknownOutfitOption(self.outfitCombo, self.selectedPreset.baseOutfit or "")
	OutfitCombo.selectByOutfitName(self.outfitCombo, self.selectedPreset.baseOutfit or "")
	local ap = self.selectedPreset.appearance
	local skinIdx = ap and ap.skinTextureIndex
	if skinIdx == nil then
		skinIdx = 0
	end
	self:setComboOptionData(self.skinCombo, tonumber(skinIdx))
	if ap then
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
	else
		self.hairRgbField:setText("")
		self.beardRgbField:setText("")
	end
	self:rebuildHairBeardCombos()
	if ap then
		self:setComboOptionData(self.hairCombo, ap.hairModel)
		if not self.femaleTick:isSelected(1) then
			self:setComboOptionData(self.beardCombo, ap.beardModel)
		end
	end
	self:rebuildMakeupAppliedList()
	self:refreshBeardVisibility()
	self:rebuildSlotList()
	self:onPreview()
	self._rgbSig = self:_rgbSignature()
	self:layoutChildren()
end

function AvatarPresetUI:_rgbSignature()
	return tostring(self.hairRgbField:getText()) .. "|" .. tostring(self.beardRgbField:getText())
end

function AvatarPresetUI.onPresetSelected(target, row)
	if target and target.loadPreset then
		local preset = listRowPayload(row)
		if preset then
			target:loadPreset(preset)
		end
	end
end

function AvatarPresetUI:onNew()
	self:loadPreset(Shared.normalizeAvatarPreset({ name = getText("IGUI_INPC_NewAvatarPreset") }))
end

function AvatarPresetUI:collectPreset()
	if not self.selectedPreset then
		self.selectedPreset = Shared.normalizeAvatarPreset({ name = getText("IGUI_INPC_NewAvatarPreset") })
	end
	self.selectedPreset.name = self.nameField:getText()
	self.selectedPreset.female = self.femaleTick:isSelected(1)
	self.selectedPreset.baseOutfit = OutfitCombo.getSelectedOutfitName(self.outfitCombo)
	local hr, hg, hb = Shared.parseAppearanceRgbInput(self.hairRgbField:getText())
	local br, bg, bb = Shared.parseAppearanceRgbInput(self.beardRgbField:getText())
	local beardVal = nil
	if not self.femaleTick:isSelected(1) then
		beardVal = self:getCombo(self.beardCombo)
	end
	self.selectedPreset.appearance = Shared.normalizeAppearance({
		skinTextureIndex = self:getCombo(self.skinCombo),
		hairModel = self:getCombo(self.hairCombo),
		beardModel = beardVal,
		hairR = hr,
		hairG = hg,
		hairB = hb,
		beardR = br,
		beardG = bg,
		beardB = bb,
		makeupFullTypes = self:ensureAppearanceSlots(),
	})
	return Shared.normalizeAvatarPreset(self.selectedPreset)
end

function AvatarPresetUI:onSave()
	Client.adminSaveAvatarPreset(self:collectPreset())
end

function AvatarPresetUI:onDelete()
	if not self.selectedPreset then
		return
	end
	Client.adminDeleteAvatarPreset(self.selectedPreset.id)
	self:refreshPresetList()
end

function AvatarPresetUI:onBodyLocationChanged()
	local bodyLocation = self:getCombo(self.locationCombo)
	self.itemCombo.options = {}
	self.itemCombo.selected = 0
	self.itemCombo:addOptionWithData(getText("UI_characreation_clothing_none"), "")
	if bodyLocation and getAllItemsForBodyLocation then
		local items = getAllItemsForBodyLocation(bodyLocation)
		if type(items) == "table" then
			table.sort(items)
			for i = 1, #items do
				local fullType = items[i]
				local name = fullType
				local scriptItem = ScriptManager
					and ScriptManager.instance
					and ScriptManager.instance:FindItem(fullType)
				if scriptItem then
					name = scriptItem:getDisplayName()
				end
				self.itemCombo:addOptionWithData(name, fullType)
			end
		end
	end
	self.itemCombo.selected = 1
	self:onPreview()
end

function AvatarPresetUI:rebuildSlotList()
	self.slotList:clear()
	for i = 1, #(self.selectedPreset and self.selectedPreset.slots or {}) do
		local slot = self.selectedPreset.slots[i]
		self.slotList:addItem((slot.bodyLocation or "") .. " " .. (slot.fullType or ""), slot)
	end
end

function AvatarPresetUI:onAddSlot()
	if not self.selectedPreset then
		self:onNew()
	end
	local bodyLocation = self:getCombo(self.locationCombo)
	local fullType = self:getCombo(self.itemCombo)
	if not bodyLocation or bodyLocation == "" or not fullType or fullType == "" then
		return
	end
	self.selectedPreset.slots = self.selectedPreset.slots or {}
	for i = #self.selectedPreset.slots, 1, -1 do
		if self.selectedPreset.slots[i].bodyLocation == bodyLocation then
			table.remove(self.selectedPreset.slots, i)
		end
	end
	self.selectedPreset.slots[#self.selectedPreset.slots + 1] = { bodyLocation = bodyLocation, fullType = fullType }
	self:rebuildSlotList()
	self:onPreview()
end

function AvatarPresetUI:onRemoveSlot()
	if not self.selectedPreset or not self.slotList.selected or self.slotList.selected < 1 then
		return
	end
	table.remove(self.selectedPreset.slots, self.slotList.selected)
	self:rebuildSlotList()
	self:onPreview()
end

function AvatarPresetUI.onSlotSelected(target, row)
	local slot = listRowPayload(row)
	if not target or not slot then
		return
	end
	target:setCombo(target.locationCombo, slot.bodyLocation)
	target:onBodyLocationChanged()
	target:setCombo(target.itemCombo, slot.fullType)
end

function AvatarPresetUI:onPreview()
	local preset = self:collectPreset()
	local desc = AvatarUtils.makeSurvivorDesc({
		mode = Shared.AVATAR_MODE.PRESET,
		preset = preset,
		female = preset.female == true,
	})
	if not desc then
		self.model:setVisible(false)
		return
	end
	self.model:setSurvivorDesc(desc)
	self.model:setState("idle")
	self.model:setDirection(IsoDirections.S)
	self.model:setIsometric(false)
	self.model:render()
	self.model:setVisible(true)
end

function AvatarPresetUI:update()
	ISCollapsableWindow.update(self)
	self._rgbTick = self._rgbTick + 1
	if self._rgbTick >= 12 then
		self._rgbTick = 0
		local sig = self:_rgbSignature()
		if sig ~= self._rgbSig then
			self._rgbSig = sig
			self:onPreview()
		end
	end
end

function AvatarPresetUI:onResize()
	ISCollapsableWindow.onResize(self)
	self:layoutChildren()
end

function AvatarPresetUI:layoutScrollForm(scrollViewportH)
	local pane = self.formPane
	local pad = C.PAD
	local inner = pane:getScrollAreaWidth()
	local w = math.max(200, inner)
	local y = pad

	local function rowLabel(label)
		if not label:getIsVisible() then
			return
		end
		Layout.setBounds(label, pad, y, w - pad * 2, C.LBL_H)
		y = y + C.LBL_H + 2
	end

	local function rowControl(control, h)
		if not control:getIsVisible() then
			return
		end
		h = h or C.FIELD_H
		Layout.setBounds(control, pad, y, w - pad * 2, h)
		y = y + h + pad
	end

	rowLabel(self.labels.name)
	rowControl(self.nameField)
	rowControl(self.femaleTick, C.FIELD_H)
	rowLabel(self.labels.outfit)
	rowControl(self.outfitCombo)
	rowLabel(self.labels.skinTone)
	rowControl(self.skinCombo)
	rowLabel(self.labels.hairStyle)
	rowControl(self.hairCombo)
	if self.labels.beardStyle:getIsVisible() then
		rowLabel(self.labels.beardStyle)
		rowControl(self.beardCombo)
	end
	rowLabel(self.labels.hairRgb)
	rowControl(self.hairRgbField)
	if self.labels.beardRgb:getIsVisible() then
		rowLabel(self.labels.beardRgb)
		rowControl(self.beardRgbField)
	end
	rowLabel(self.labels.makeupCat)
	rowControl(self.makeupCategoryCombo)
	rowLabel(self.labels.makeupPick)
	rowControl(self.makeupItemCombo)
	local bw = math.floor((w - pad * 3) / 2)
	Layout.setBounds(self.makeupAddBtn, pad, y, bw, C.BTN_H)
	Layout.setBounds(self.makeupRemoveBtn, pad + bw + pad, y, bw, C.BTN_H)
	y = y + C.BTN_H + pad
	rowLabel(self.labels.makeupApplied)
	local makeupListH = 72
	Layout.setBounds(self.makeupAppliedList, pad, y, w - pad * 2, makeupListH)
	y = y + makeupListH + pad

	rowLabel(self.labels.location)
	rowControl(self.locationCombo)
	rowLabel(self.labels.item)
	rowControl(self.itemCombo)

	local slotH = math.max(96, math.min(220, scrollViewportH - y - C.BTN_H * 2 - pad * 8))
	Layout.setBounds(self.slotList, pad, y, w - pad * 2, slotH)
	y = y + slotH + pad
	local tw = math.floor((w - pad * 4) / 3)
	Layout.setBounds(self.addSlotBtn, pad, y, tw, C.BTN_H)
	Layout.setBounds(self.removeSlotBtn, pad + tw + pad, y, tw, C.BTN_H)
	Layout.setBounds(self.previewBtn, pad + (tw + pad) * 2, y, tw, C.BTN_H)
	y = y + C.BTN_H + pad * 2

	pane:setScrollHeight(y + pad)
	UIHelpers.syncScrollingListScrollbar(pane)
end

function AvatarPresetUI:layoutChildren()
	local pad = C.PAD
	local th = self:titleBarHeight()
	local top = th + pad
	local bottom = self.resizable
		and self.resizeWidget
		and self.resizeWidget:getIsVisible()
		and self:resizeWidgetHeight() + pad
		or pad
	local footerY = self.height - bottom - C.BTN_H
	Layout.setBounds(self.presetList, pad, top, C.LIST_W, footerY - top - pad)
	local bw = math.floor((C.LIST_W - pad * 2) / 3)
	Layout.setBounds(self.newBtn, pad, footerY, bw, C.BTN_H)
	Layout.setBounds(self.saveBtn, pad + bw + pad, footerY, bw, C.BTN_H)
	Layout.setBounds(self.deleteBtn, pad + (bw + pad) * 2, footerY, bw, C.BTN_H)

	local previewX = self.width - C.PREVIEW_W - pad
	local formX = pad + C.LIST_W + pad
	local formW = previewX - formX - pad
	local scrollH = footerY - top

	self:refreshBeardVisibility()
	Layout.setBounds(self.formPane, formX, top, formW, scrollH)
	if self.formPane.javaObject then
		self.formPane:setScrollChildren(true)
	end
	self.formPane:onResize()
	self:layoutScrollForm(scrollH)
	self.formPane:onResize()
	self:layoutScrollForm(scrollH)
	UIHelpers.syncScrollingListScrollbar(self.formPane)

	Layout.setBounds(self.previewPanel, previewX, top, C.PREVIEW_W, footerY - top)
	Layout.setBounds(
		self.model,
		pad,
		pad + 22,
		C.PREVIEW_W - pad * 2,
		math.max(180, self.previewPanel:getHeight() - pad * 3 - 22)
	)

	UIHelpers.syncScrollingListScrollbar(self.presetList)
	UIHelpers.syncScrollingListScrollbar(self.slotList)
	UIHelpers.syncScrollingListScrollbar(self.makeupAppliedList)
end

function AvatarPresetUI:close()
	ISCollapsableWindow.close(self)
	if AvatarPresetUI.instance == self then
		AvatarPresetUI.instance = nil
	end
	Client.avatarPresetUiRef = nil
end

function AvatarPresetUI.open()
	if AvatarPresetUI.instance then
		Client.avatarPresetUiRef = AvatarPresetUI.instance
		AvatarPresetUI.instance:refreshPresetList()
		AvatarPresetUI.instance:bringToTop()
		return AvatarPresetUI.instance
	end
	local x, y, w, h = Layout.defaultWindowGeometry(C.DEF_W, C.DEF_H, C.MIN_W, C.MIN_H, 40)
	local ui = AvatarPresetUI:new(x, y, w, h)
	AvatarPresetUI.instance = ui
	Client.avatarPresetUiRef = ui
	ui:initialise()
	ui:addToUIManager()
	return ui
end

return AvatarPresetUI
