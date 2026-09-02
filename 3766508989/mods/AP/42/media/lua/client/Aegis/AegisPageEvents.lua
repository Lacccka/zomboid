-- Event studio: reusable event scripts, list on the left, step editor on
-- the right. The server owns the registry, this page edits a working copy
-- and mirrors its list pushes. Runs anchor at the triggering admin.
require "Aegis/AegisWindow"
require "ISUI/ISScrollingListBox"
require "ISUI/ISTextEntryBox"
require "ISUI/ISComboBox"

AegisPageEvents = ISPanel:derive("AegisPageEvents")

local LIST_W = 230
local ROW_H = 44
local STEP_ROW_H = 26
local MAX_EVENTS = 40
local MAX_STEPS = 20
local LABEL_W = 96

local TYPES = { "horde", "storm", "tropical", "blizzard", "rain", "thunder", "gunshot", "firework", "noise", "announce", "wait" }

-- widget plan per step type: sliders, toggles and one text line, ranges
-- mirror the server side clamps
local PARAMS = {
    horde = {
        sliders = {
            { key = "count", label = "UI_Aegis_StudioP_count", min = 1, max = 200, step = 1, default = 40 },
            { key = "dist", label = "UI_Aegis_StudioP_dist", min = 0, max = 120, step = 5, default = 60 },
            { key = "radius", label = "UI_Aegis_StudioP_radius", min = 1, max = 20, step = 1, default = 10 },
            { key = "lure", label = "UI_Aegis_StudioP_lure", min = 0, max = 15, step = 1, default = 5 },
        },
        toggles = {
            { key = "sprint", label = "UI_Aegis_StudioP_sprint", default = 0 },
            { key = "crawl", label = "UI_Aegis_StudioP_crawl", default = 0 },
        },
    },
    storm = { sliders = { { key = "hours", label = "UI_Aegis_StudioP_hours", min = 1, max = 96, step = 1, default = 8, suffix = "h" } } },
    tropical = { sliders = { { key = "hours", label = "UI_Aegis_StudioP_hours", min = 1, max = 96, step = 1, default = 12, suffix = "h" } } },
    blizzard = { sliders = { { key = "hours", label = "UI_Aegis_StudioP_hours", min = 1, max = 96, step = 1, default = 12, suffix = "h" } } },
    rain = {
        sliders = { { key = "intensity", label = "UI_Aegis_StudioP_intensity", min = 10, max = 100, step = 5, default = 60, suffix = "%" } },
        toggles = { { key = "on", label = "UI_Aegis_StudioP_on", default = 1 } },
    },
    thunder = {},
    gunshot = {},
    firework = {},
    noise = { sliders = { { key = "radius", label = "UI_Aegis_StudioP_radius", min = 10, max = 500, step = 10, default = 100 } } },
    announce = { text = { key = "text", label = "UI_Aegis_StudioP_text" } },
    wait = { sliders = { { key = "seconds", label = "UI_Aegis_StudioP_seconds", min = 5, max = 600, step = 5, default = 30, suffix = "s" } } },
}

local function typeIndex(name)
    for i, t in ipairs(TYPES) do
        if t == name then return i end
    end
    return nil
end

local function typeLabel(name)
    return getText("UI_Aegis_StudioType_" .. name)
end

local function clampNum(v, def, min, max)
    v = tonumber(v)
    if not v then return def end
    v = math.floor(v)
    if v < min then v = min end
    if v > max then v = max end
    return v
end

-- 120 char cap of the server side; kahlua strings count UTF-16 units,
-- only a dangling high surrogate can break at the cut
local function capText(s)
    s = tostring(s or ""):gsub("[%c|;]", " "):gsub("^%s+", ""):gsub("%s+$", "")
    if #s > 120 then
        s = s:sub(1, 120)
        local b = s:byte(#s)
        if b and b >= 55296 and b <= 56319 then s = s:sub(1, #s - 1) end
    end
    return s
end

-- server payloads pass through here, only known keys survive
local function copyStep(raw)
    if type(raw) ~= "table" or type(raw.type) ~= "string" then return nil end
    local spec = PARAMS[raw.type]
    if not spec then return nil end
    local step = { type = raw.type }
    for _, def in ipairs(spec.sliders or {}) do
        step[def.key] = clampNum(raw[def.key], def.default, def.min, def.max)
    end
    for _, def in ipairs(spec.toggles or {}) do
        step[def.key] = (raw[def.key] == 1 or raw[def.key] == true) and 1 or 0
    end
    if spec.text then
        step[spec.text.key] = capText(raw[spec.text.key])
    end
    return step
end

local function stepSummary(step)
    local t = step.type
    if t == "horde" then return step.count .. "x " .. step.dist .. "m" end
    if t == "storm" or t == "tropical" or t == "blizzard" then return step.hours .. "h" end
    if t == "rain" then return step.on == 1 and (step.intensity .. "%") or "0%" end
    if t == "noise" then return step.radius .. "m" end
    if t == "announce" then return step.text or "" end
    if t == "wait" then return step.seconds .. "s" end
    return ""
end

local function stepLine(i, step)
    local line = i .. ". " .. typeLabel(step.type)
    local extra = stepSummary(step)
    if extra ~= "" then line = line .. " - " .. extra end
    return line
end

local function stepCount(ev)
    if type(ev.steps) == "table" then return #ev.steps end
    return 0
end

function AegisPageEvents.create(window, x, y, w, h)
    local o = ISPanel:new(x, y, w, h)
    setmetatable(o, AegisPageEvents)
    AegisPageEvents.__index = AegisPageEvents
    o.background = false
    o.window = window
    o.editorX = 20 + LIST_W + 16
    o.colW = w - o.editorX - 20
    o.events = {}
    o.selectedId = nil
    o.editor = { id = nil, name = "", announceSec = 0, steps = {} }
    o.shownType = 1
    AegisPageEvents.instance = o
    return o
end

function AegisPageEvents:createChildren()
    local pad = 20
    local ex = self.editorX + 14
    local ew = self.colW - 28
    local half = math.floor((ew - 8) / 2)

    -- editor column first, its fixed walk decides the page height
    local ey = pad + 44
    self.nameEntry = ISTextEntryBox:new("", ex, ey, ew, 26)
    self.nameEntry:initialise()
    self.nameEntry:instantiate()
    self.nameEntry.font = UIFont.Small
    self.nameEntry:setMaxTextLength(40)
    self.nameEntry:setPlaceholderText(getText("UI_Aegis_StudioName"))
    self.nameEntry.backgroundColor = { r = Aegis.col.dark.r, g = Aegis.col.dark.g, b = Aegis.col.dark.b, a = 1 }
    self.nameEntry.borderColor = { r = Aegis.col.line.r, g = Aegis.col.line.g, b = Aegis.col.line.b, a = 1 }
    self:addChild(self.nameEntry)
    ey = ey + 36

    self.announceSlider = AegisSlider:new(ex + LABEL_W + 4, ey, ew - LABEL_W - 4, 24, self, nil)
    self.announceSlider:setValues(0, 300, 5, "s")
    self.announceSlider:setValue(0, true)
    self:addChild(self.announceSlider)
    ey = ey + 34

    self.stepsLabelY = ey
    ey = ey + 18
    self.stepList = ISScrollingListBox:new(ex, ey, ew, STEP_ROW_H * 7)
    self.stepList:initialise()
    self.stepList:instantiate()
    self.stepList.itemheight = STEP_ROW_H
    self.stepList.drawBorder = false
    self.stepList.backgroundColor = { r = 0, g = 0, b = 0, a = 0 }
    self.stepList.doDrawItem = AegisPageEvents.drawStepRow
    self.stepList:setOnMouseDownFunction(self, AegisPageEvents.onSelectStep)
    self:addChild(self.stepList)
    ey = ey + STEP_ROW_H * 7 + 10

    self.typeCombo = ISComboBox:new(ex, ey, ew, 26, self, nil)
    self.typeCombo:initialise()
    for _, t in ipairs(TYPES) do
        self.typeCombo:addOption(typeLabel(t))
    end
    self.typeCombo.selected = 1
    self:addChild(self.typeCombo)
    ey = ey + 34

    self.paramY = ey
    self.paramSliders = {}
    for i = 1, 4 do
        local s = AegisSlider:new(ex + LABEL_W + 4, ey, ew - LABEL_W - 4, 24, self, nil)
        self:addChild(s)
        self.paramSliders[i] = s
    end
    self.paramToggles = {}
    for i = 1, 2 do
        local t = AegisToggle:new(ex + (i - 1) * (half + 8), ey, half, 28, "", nil, self, nil)
        self:addChild(t)
        self.paramToggles[i] = t
    end
    self.paramText = ISTextEntryBox:new("", ex, ey, ew, 26)
    self.paramText:initialise()
    self.paramText:instantiate()
    self.paramText.font = UIFont.Small
    self.paramText:setMaxTextLength(120)
    self.paramText:setPlaceholderText(getText("UI_Aegis_StudioP_text"))
    self.paramText.backgroundColor = { r = Aegis.col.dark.r, g = Aegis.col.dark.g, b = Aegis.col.dark.b, a = 1 }
    self.paramText.borderColor = { r = Aegis.col.line.r, g = Aegis.col.line.g, b = Aegis.col.line.b, a = 1 }
    self:addChild(self.paramText)
    ey = ey + 5 * 32 + 6

    local upW = 48
    local aw = math.floor((ew - 2 * upW - 4 * 8) / 3)
    self.addBtn = AegisButton:new(ex, ey, aw, 30, getText("UI_Aegis_StudioAdd"), "plus", self, AegisPageEvents.onAddStep)
    self:addChild(self.addBtn)
    self.updateBtn = AegisButton:new(ex + aw + 8, ey, aw, 30, getText("UI_Aegis_StudioUpdate"), "refresh", self, AegisPageEvents.onUpdateStep)
    self:addChild(self.updateBtn)
    self.removeBtn = AegisButton:new(ex + (aw + 8) * 2, ey, aw, 30, getText("UI_Aegis_StudioRemove"), "minus", self, AegisPageEvents.onRemoveStep)
    self:addChild(self.removeBtn)
    self.upBtn = AegisButton:new(ex + (aw + 8) * 3, ey, upW, 30, getText("UI_Aegis_StudioUp"), nil, self, AegisPageEvents.onUpStep)
    self:addChild(self.upBtn)
    self.downBtn = AegisButton:new(ex + (aw + 8) * 3 + upW + 8, ey, upW, 30, getText("UI_Aegis_StudioDown"), nil, self, AegisPageEvents.onDownStep)
    self:addChild(self.downBtn)
    ey = ey + 38

    self.hintY = ey
    ey = ey + 22

    self.saveBtn = AegisButton:new(ex, ey, half, 36, getText("UI_Aegis_StudioSave"), "check", self, AegisPageEvents.onSave)
    self.saveBtn.style = "gold"
    self:addChild(self.saveBtn)
    self.runBtn = AegisButton:new(ex + half + 8, ey, ew - half - 8, 36, getText("UI_Aegis_StudioRun"), "bolt", self, AegisPageEvents.onRun)
    self:addChild(self.runBtn)
    ey = ey + 36

    -- fixed layout, the window scrolls the page when it is shorter
    -- (see AegisWindow.switchPage)
    self.designH = ey + 14 + pad

    -- event list column, anchored to the same bottom edge
    local lx = pad + 8
    local lw = LIST_W - 16
    local cardBottom = self.designH - pad
    self.surpriseBtn = AegisButton:new(lx, cardBottom - 14 - 32, lw, 32, getText("UI_Aegis_StudioSurprise"), "wand", self, AegisPageEvents.onSurprise)
    self.surpriseBtn.tooltip = getText("UI_Aegis_StudioSurpriseTip")
    self:addChild(self.surpriseBtn)
    local rowY = self.surpriseBtn.y - 40
    self.newBtn = AegisButton:new(lx, rowY, lw - 80, 32, getText("UI_Aegis_StudioNew"), "plus", self, AegisPageEvents.onNew)
    self.newBtn.style = "gold"
    self:addChild(self.newBtn)
    self.dupBtn = AegisButton:new(lx + lw - 72, rowY, 32, 32, nil, "items", self, AegisPageEvents.onDuplicate)
    self.dupBtn.tooltip = getText("UI_Aegis_StudioDuplicate")
    self:addChild(self.dupBtn)
    self.delBtn = AegisButton:new(lx + lw - 32, rowY, 32, 32, nil, "trash", self, AegisPageEvents.onDelete)
    self.delBtn.style = "danger"
    self.delBtn.tooltip = getText("UI_Aegis_StudioDelete")
    self:addChild(self.delBtn)

    self.list = ISScrollingListBox:new(lx, pad + 40, lw, rowY - 8 - (pad + 40))
    self.list:initialise()
    self.list:instantiate()
    self.list.itemheight = ROW_H
    self.list.drawBorder = false
    self.list.backgroundColor = { r = 0, g = 0, b = 0, a = 0 }
    self.list.doDrawItem = AegisPageEvents.drawEventRow
    self.list:setOnMouseDownFunction(self, AegisPageEvents.onSelectEvent)
    self:addChild(self.list)

    self:applyType(TYPES[1], nil)
end

-- ------------------------------------------------------------------
-- Step editor
-- ------------------------------------------------------------------

function AegisPageEvents:applyType(typeName, step)
    local spec = PARAMS[typeName]
    if not spec then return end
    local row = 0
    for i = 1, 4 do
        local s = self.paramSliders[i]
        local def = spec.sliders and spec.sliders[i] or nil
        if def then
            s:setValues(def.min, def.max, def.step, def.suffix or "")
            local v = def.default
            if step then v = clampNum(step[def.key], def.default, def.min, def.max) end
            s:setValue(v, true)
            s:setY(self.paramY + row * 32)
            s:setVisible(true)
            row = row + 1
        else
            s:setVisible(false)
        end
    end
    for i = 1, 2 do
        local t = self.paramToggles[i]
        local def = spec.toggles and spec.toggles[i] or nil
        if def then
            t.label = getText(def.label)
            t.tooltip = nil
            local v = def.default
            if step then v = (step[def.key] == 1 or step[def.key] == true) and 1 or 0 end
            t:setChecked(v == 1)
            t:setY(self.paramY + row * 32)
            t:setVisible(true)
        else
            t:setVisible(false)
        end
    end
    if spec.toggles and spec.toggles[1] then row = row + 1 end
    if spec.text then
        self.paramText:setText(step and step[spec.text.key] or "")
        self.paramText:setY(self.paramY + row * 32)
        self.paramText:setVisible(true)
    else
        self.paramText:setVisible(false)
    end
end

function AegisPageEvents:stepFromWidgets()
    local typeName = TYPES[self.typeCombo.selected or 1]
    local spec = PARAMS[typeName]
    local step = { type = typeName }
    for i, def in ipairs(spec.sliders or {}) do
        step[def.key] = self.paramSliders[i].value
    end
    for i, def in ipairs(spec.toggles or {}) do
        step[def.key] = self.paramToggles[i].checked and 1 or 0
    end
    if spec.text then
        step[spec.text.key] = capText(self.paramText:getInternalText())
    end
    return step
end

function AegisPageEvents:refreshSteps()
    local keep = self.stepList.selected or 0
    self.stepList:clear()
    for i, step in ipairs(self.editor.steps) do
        self.stepList:addItem(stepLine(i, step), step)
    end
    if keep > #self.editor.steps then keep = #self.editor.steps end
    if keep < 1 then keep = 0 end
    self.stepList.selected = keep
end

function AegisPageEvents:onSelectStep(step)
    if type(step) ~= "table" or type(step.type) ~= "string" then return end
    local idx = typeIndex(step.type)
    if not idx then return end
    self.typeCombo.selected = idx
    self.shownType = idx
    self:applyType(step.type, step)
end

function AegisPageEvents:onAddStep()
    if #self.editor.steps >= MAX_STEPS then return end
    table.insert(self.editor.steps, self:stepFromWidgets())
    self.stepList.selected = #self.editor.steps
    self:refreshSteps()
end

function AegisPageEvents:onUpdateStep()
    local i = self.stepList.selected or 0
    if i < 1 or i > #self.editor.steps then return end
    self.editor.steps[i] = self:stepFromWidgets()
    self:refreshSteps()
end

function AegisPageEvents:onRemoveStep()
    local i = self.stepList.selected or 0
    if i < 1 or i > #self.editor.steps then return end
    table.remove(self.editor.steps, i)
    self:refreshSteps()
end

function AegisPageEvents:onUpStep()
    local i = self.stepList.selected or 0
    if i < 2 or i > #self.editor.steps then return end
    local steps = self.editor.steps
    steps[i - 1], steps[i] = steps[i], steps[i - 1]
    self.stepList.selected = i - 1
    self:refreshSteps()
end

function AegisPageEvents:onDownStep()
    local i = self.stepList.selected or 0
    if i < 1 or i >= #self.editor.steps then return end
    local steps = self.editor.steps
    steps[i], steps[i + 1] = steps[i + 1], steps[i]
    self.stepList.selected = i + 1
    self:refreshSteps()
end

-- ------------------------------------------------------------------
-- Event list and registry commands
-- ------------------------------------------------------------------

function AegisPageEvents:setEvents(list)
    self.events = {}
    if type(list) == "table" then
        for i = 1, #list do
            local ev = list[i]
            if type(ev) == "table" and type(ev.id) == "string" then
                table.insert(self.events, ev)
            end
        end
    end
    local keep = self.selectedId
    self.list:clear()
    local sel = nil
    for i, ev in ipairs(self.events) do
        self.list:addItem(ev.name or ev.id, ev)
        if keep and ev.id == keep then sel = i end
    end
    -- a create has no id yet, the save ack or this name match adopts
    -- the one the server picked
    if not sel and self.pendingSaveName then
        for i, ev in ipairs(self.events) do
            if ev.name == self.pendingSaveName then sel = i end
        end
        self.pendingSaveName = nil
    end
    if sel then
        self.list.selected = sel
        self:onSelectEvent(self.events[sel])
    elseif keep then
        -- the selected event is gone, fall back to a fresh editor
        self:onNew()
    else
        self.list.selected = 0
    end
end

function AegisPageEvents:onSelectEvent(ev)
    if type(ev) ~= "table" or type(ev.id) ~= "string" then return end
    self.selectedId = ev.id
    self.pendingSaveName = nil
    self.editor = { id = ev.id, name = ev.name or ev.id, announceSec = tonumber(ev.announceSec) or 0, steps = {} }
    local raw = type(ev.steps) == "table" and ev.steps or {}
    for i = 1, #raw do
        if #self.editor.steps >= MAX_STEPS then break end
        local step = copyStep(raw[i])
        if step then table.insert(self.editor.steps, step) end
    end
    self.nameEntry:setText(self.editor.name)
    self.announceSlider:setValue(self.editor.announceSec, true)
    self.stepList.selected = 0
    self:refreshSteps()
end

function AegisPageEvents:onNew()
    self.selectedId = nil
    self.pendingSaveName = nil
    self.list.selected = 0
    self.editor = { id = nil, name = "", announceSec = 0, steps = {} }
    self.nameEntry:setText("")
    self.announceSlider:setValue(0, true)
    self.stepList.selected = 0
    self:refreshSteps()
end

-- unsaved copy of the current editor, the server hands out the new id
function AegisPageEvents:onDuplicate()
    if not self.editor.id then return end
    self.editor.id = nil
    self.selectedId = nil
    self.list.selected = 0
end

function AegisPageEvents:onDelete()
    if not self.selectedId then return end
    local name = self.editor.name
    if name == "" then name = self.selectedId end
    AegisConfirm.show(getText("UI_Aegis_StudioDelete"), name,
        getText("UI_Aegis_StudioDelete"), self, function(page)
            sendClientCommand(getPlayer(), AegisShared.MODULE, "studioDelete", { id = page.selectedId })
        end)
end

function AegisPageEvents:onSave()
    local name = (self.nameEntry:getInternalText() or ""):gsub("[%c|]", " "):gsub("^%s+", ""):gsub("%s+$", "")
    if name == "" or #self.editor.steps == 0 then return end
    self.editor.name = name
    if self.editor.id == nil then self.pendingSaveName = name end
    sendClientCommand(getPlayer(), AegisShared.MODULE, "studioSave", {
        event = {
            id = self.editor.id or "",
            name = name,
            announceSec = self.announceSlider.value,
            steps = self.editor.steps,
        },
    })
end

function AegisPageEvents:onRun()
    if not self.editor.id then return end
    sendClientCommand(getPlayer(), AegisShared.MODULE, "studioRun", { id = self.editor.id })
    Aegis.showToast(getText("UI_Aegis_StudioStarted"))
end

function AegisPageEvents:onSurprise()
    sendClientCommand(getPlayer(), AegisShared.MODULE, "studioSurprise", {})
    Aegis.showToast(getText("UI_Aegis_StudioStarted"))
end

function AegisPageEvents:onShow()
    sendClientCommand(getPlayer(), AegisShared.MODULE, "studioList", {})
end

-- ------------------------------------------------------------------
-- Rows
-- ------------------------------------------------------------------

function AegisPageEvents.drawEventRow(list, y, item, alt)
    local c = Aegis.col
    local sel = list.selected == item.index
    local hover = list.mouseoverselected == item.index
    if sel then
        Aegis.roundRect(list, 2, y + 1, list:getWidth() - 4, ROW_H - 2, 8, 1, c.card)
        Aegis.roundRect(list, 2, y + 8, 3, ROW_H - 16, 1, 1, c.gold)
    elseif hover then
        Aegis.roundRect(list, 2, y + 1, list:getWidth() - 4, ROW_H - 2, 8, 0.5, c.card)
    end
    local ev = item.item
    Aegis.text(list, Aegis.fitText(ev.name or ev.id, UIFont.Medium, list:getWidth() - 24), 12, y + 4,
        UIFont.Medium, sel and c.text or c.muted)
    Aegis.text(list, stepCount(ev) .. " " .. getText("UI_Aegis_StudioSteps"), 12,
        y + 6 + Aegis.fontH(UIFont.Medium), UIFont.Small, c.goldDim)
    -- announced events carry a dot, silent ones stay plain
    if (tonumber(ev.announceSec) or 0) > 0 then
        Aegis.roundRect(list, list:getWidth() - 18, y + math.floor(ROW_H / 2) - 4, 8, 8, 4, 1, c.gold)
    end
    return y + ROW_H
end

function AegisPageEvents.drawStepRow(list, y, item, alt)
    local c = Aegis.col
    local sel = list.selected == item.index
    local hover = list.mouseoverselected == item.index
    if sel then
        Aegis.roundRect(list, 2, y + 1, list:getWidth() - 4, STEP_ROW_H - 2, 8, 1, c.card)
        Aegis.roundRect(list, 2, y + 5, 3, STEP_ROW_H - 10, 1, 1, c.gold)
    elseif hover then
        Aegis.roundRect(list, 2, y + 1, list:getWidth() - 4, STEP_ROW_H - 2, 8, 0.5, c.card)
    end
    Aegis.text(list, Aegis.fitText(item.text, UIFont.Small, list:getWidth() - 20), 10,
        y + math.floor((STEP_ROW_H - Aegis.fontH(UIFont.Small)) / 2), UIFont.Small, sel and c.text or c.muted)
    return y + STEP_ROW_H
end

-- ------------------------------------------------------------------
-- Frame
-- ------------------------------------------------------------------

function AegisPageEvents:prerender()
    if not self.typeCombo then return end
    local c = Aegis.col
    local pad = 20
    local ex = self.editorX + 14

    -- the combo has no change callback, follow its selection here
    local sel = self.typeCombo.selected or 1
    if sel ~= self.shownType then
        self.shownType = sel
        self:applyType(TYPES[sel], nil)
    end

    Aegis.roundFrame(self, pad, pad, LIST_W, self.designH - pad * 2, 10, 1, c.line, c.panel)
    Aegis.icon(self, "bolt", pad + 14, pad + 12, 15, 1, c.gold)
    Aegis.text(self, getText("UI_Aegis_NavStudio"), pad + 36, pad + 10, UIFont.Medium, c.text)
    if #self.events == 0 then
        Aegis.textCentre(self, getText("UI_Aegis_StudioNone"), pad + math.floor(LIST_W / 2), pad + 70, UIFont.Small, c.muted)
    end

    local name = (self.nameEntry:getInternalText() or ""):gsub("^%s+", ""):gsub("%s+$", "")
    Aegis.roundFrame(self, self.editorX, pad, self.colW, self.designH - pad * 2, 10, 1, c.line, c.panel)
    Aegis.icon(self, "gear", self.editorX + 14, pad + 12, 15, 1, c.gold)
    Aegis.text(self, Aegis.fitText(name ~= "" and name or getText("UI_Aegis_StudioNew"), UIFont.Medium, self.colW - 50),
        self.editorX + 36, pad + 10, UIFont.Medium, c.text)

    Aegis.text(self, Aegis.fitText(getText("UI_Aegis_StudioAnnounce"), UIFont.Small, LABEL_W), ex,
        self.announceSlider.y + 5, UIFont.Small, c.muted)
    Aegis.text(self, getText("UI_Aegis_StudioSteps"), ex, self.stepsLabelY, UIFont.Small, c.muted)
    Aegis.textRight(self, #self.editor.steps .. "/" .. MAX_STEPS, self.editorX + self.colW - 14,
        self.stepsLabelY, UIFont.Small, c.muted)

    local spec = PARAMS[TYPES[sel]]
    if spec and spec.sliders then
        for i, def in ipairs(spec.sliders) do
            Aegis.text(self, Aegis.fitText(getText(def.label), UIFont.Small, LABEL_W),
                ex, self.paramSliders[i].y + 5, UIFont.Small, c.muted)
        end
    end

    Aegis.text(self, Aegis.fitText(getText("UI_Aegis_StudioAnchorHint"), UIFont.Small, self.colW - 28),
        ex, self.hintY, UIFont.Small, c.muted)

    local steps = self.editor.steps
    local srow = self.stepList.selected or 0
    local hasSel = srow >= 1 and srow <= #steps
    local canBuild = true
    if TYPES[sel] == "announce" then
        canBuild = capText(self.paramText:getInternalText()) ~= ""
    end
    self.addBtn:setEnabled(#steps < MAX_STEPS and canBuild)
    self.updateBtn:setEnabled(hasSel and canBuild)
    self.removeBtn:setEnabled(hasSel)
    self.upBtn:setEnabled(srow >= 2)
    self.downBtn:setEnabled(hasSel and srow < #steps)
    self.saveBtn:setEnabled(name ~= "" and #steps > 0
        and not (self.editor.id == nil and #self.events >= MAX_EVENTS))
    self.runBtn:setEnabled(self.editor.id ~= nil)
    self.dupBtn:setEnabled(self.editor.id ~= nil)
    self.delBtn:setEnabled(self.selectedId ~= nil)
    self.surpriseBtn:setEnabled(#self.events > 0)
end

-- server replies, in solo via the same event path
Events.OnServerCommand.Add(function(module, command, args)
    if module ~= AegisShared.MODULE then return end
    local page = AegisPageEvents.instance
    if not page then return end
    if command == "studioList" then
        page:setEvents(args and args.events or {})
    elseif command == "studioSave" then
        if args and args.ok == true then
            if type(args.id) == "string" then
                page.editor.id = args.id
                page.selectedId = args.id
                page.pendingSaveName = nil
            end
            Aegis.showToast(getText("UI_Aegis_StudioSaved"))
        end
    elseif command == "studioDelete" then
        if args and args.ok == true then
            Aegis.showToast(getText("UI_Aegis_StudioDeleted"))
        end
    end
end)

AegisWindow.registerPage({
    id = "events",
    icon = "bolt",
    label = "UI_Aegis_NavStudio",
    create = AegisPageEvents.create,
})
