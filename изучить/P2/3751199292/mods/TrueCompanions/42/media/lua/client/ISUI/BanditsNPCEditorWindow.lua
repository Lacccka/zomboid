--
-- Bandits NPC - Companions Overhaul - NPC Core Editor
--
-- One consolidated authoring tool (own window, right-click opened like Bandits
-- Creator). Tabs: Recipes / Dialogue / Quests. Edits the live tables and
-- "Save to file" persists each across saves.
--

require "ISUI/ISCollapsableWindow"
require "ISUI/ISButton"
require "ISUI/ISComboBox"
require "ISUI/ISTextEntryBox"
require "ISUI/ISScrollingListBox"

BanditsNPCEditorWindow = ISCollapsableWindow:derive("BanditsNPCEditorWindow")

local FONT_HGT = getTextManager():getFontHeight(UIFont.Small)
local PAD = 12
local BTN_H = FONT_HGT + 8
local STATIONS = { "cooking", "chemistry", "mechanical", "electronics" }

-- field labels per tab (drives how many entry boxes are shown)
local FIELDS = {
    recipes  = { "Name", "Output item (Base.X)", "Output count", "Time (hours)", "Inputs (Type:Count, ...)" },
    dialogue = { "Player line / category label", "NPC response  ( ' | ' between variants;  'Recon: text' for a profession answer )", "Params: id,parent,category,reqPerk,reqLevel,reqTrait,reqNpcExp,reqRival,teach,affinity,cooldown,minAffinity,minDaysKnown,affectionOnly,platonicOnly,setRomance,reveals,needsUnrevealed" },
    quests   = { "Quest text (the ask; use {count})", "Completion line", "Params: type,itemType,name,min,max,reward,minAffinity,npcExp,reqPersonality,rewardItem,rewardXp" },
    spawn    = {},
}

local function serializeInputs(inputs)
    local p = {}
    if inputs then for t, c in pairs(inputs) do table.insert(p, t .. ":" .. tostring(c)) end end
    return table.concat(p, ", ")
end
local function parseInputs(str)
    local r = {}
    for chunk in string.gmatch(str or "", "[^,]+") do
        local t, c = chunk:match("^%s*(.-)%s*:%s*(%d+)%s*$")
        if t and t ~= "" then r[t] = tonumber(c) end
    end
    return r
end
local function parseKV(str)
    local m = {}
    for chunk in string.gmatch(str or "", "[^,]+") do
        local k, v = chunk:match("^%s*(%w+)%s*=%s*(.-)%s*$")
        if k then m[k] = v end
    end
    return m
end
local function num(v, d) return tonumber(v) or d end
local function dlgParams(t)
    local rp = t.reqPerk or {}
    return string.format("id=%s, parent=%s, category=%s, reqPerk=%s, reqLevel=%s, reqTrait=%s, reqNpcExp=%s, reqRival=%s, teach=%s, affinity=%s, cooldown=%s, minAffinity=%s, minDaysKnown=%s, affectionOnly=%s, platonicOnly=%s, setRomance=%s, reveals=%s, needsUnrevealed=%s",
        tostring(t.id or ""), tostring(t.parent or ""), tostring(t.isCategory and true or false),
        tostring(rp.perk or ""), tostring(rp.level or 0), tostring(t.reqTrait or ""),
        tostring(t.reqNpcExp or ""), tostring(t.reqRival and true or false), tostring(BanditsNPC.Editor.EncTeach(t.teach)),
        tostring(t.affinity or 0), tostring(t.cooldown or 0), tostring(t.minAffinity or 0), tostring(t.minDaysKnown or 0),
        tostring(t.affectionOnly and true or false), tostring(t.platonicOnly and true or false),
        t.setRomance == true and "true" or (t.setRomance == false and "false" or ""),
        tostring(t.reveals and true or false), tostring(t.needsUnrevealed and true or false))
end
local function questParams(q)
    local ri = q.rewardItem and ((q.rewardItem.type or "") .. ":" .. tostring(q.rewardItem.count or 1)) or ""
    local rx = q.rewardXp and ((q.rewardXp.perk or "") .. ":" .. tostring(q.rewardXp.amount or 0)) or ""
    return string.format("type=%s, itemType=%s, name=%s, min=%s, max=%s, reward=%s, minAffinity=%s, npcExp=%s, reqPersonality=%s, rewardItem=%s, rewardXp=%s",
        q.type or "fetch", q.itemType or "", q.name or "", tostring(q.min or 1), tostring(q.max or 1), tostring(q.reward or 10),
        tostring(q.minAffinity or 0), tostring(q.npcExp or ""), tostring(q.reqPersonality or ""), ri, rx)
end

function BanditsNPCEditorWindow:createChildren()
    ISCollapsableWindow.createChildren(self)
    local top = self:titleBarHeight() + PAD
    self.contentY = top + BTN_H + 10

    self.tabButtons = {}
    local tabs = { {id="recipes", label="Recipes"}, {id="dialogue", label="Dialogue"}, {id="quests", label="Quests"}, {id="spawn", label="Spawning"} }
    local tx = PAD
    for _, t in ipairs(tabs) do
        local b = ISButton:new(tx, top, 90, BTN_H, t.label, self, self.onTab)
        b.internal = t.id
        b:initialise(); b:instantiate(); self:addChild(b)
        self.tabButtons[t.id] = b
        tx = tx + 94
    end

    self.stationCombo = ISComboBox:new(PAD, self.contentY, 200, BTN_H, self, self.onStationChange)
    self.stationCombo:initialise()
    for _, s in ipairs(STATIONS) do self.stationCombo:addOptionWithData(s, s) end
    self.stationCombo.selected = 1
    self:addChild(self.stationCombo)

    local listY = self.contentY + BTN_H + 8
    self.list = ISScrollingListBox:new(PAD, listY, 260, self.height - listY - PAD - BTN_H - 8)
    self.list:initialise()
    self.list.itemheight = FONT_HGT + 6
    self.list.font = UIFont.Small
    self.list:setOnMouseDownFunction(self, self.onSelectItem)
    self:addChild(self.list)

    -- 5 reusable entry boxes (used per tab)
    local fx = 300
    local fw = self.width - fx - PAD
    self.formX = fx
    self.fieldY = {}
    self.entries = {}
    for i = 1, 5 do
        local ly = self.contentY + (i - 1) * 46
        self.fieldY[i] = ly
        local e = ISTextEntryBox:new("", fx, ly + FONT_HGT + 2, fw, BTN_H)
        e:initialise(); e:instantiate(); self:addChild(e)
        self.entries[i] = e
    end

    local by = self.height - PAD - BTN_H
    self.newBtn = ISButton:new(PAD, by, 70, BTN_H, "New", self, self.onNew)
    self.applyBtn = ISButton:new(PAD + 76, by, 70, BTN_H, "Apply", self, self.onApply)
    self.deleteBtn = ISButton:new(PAD + 152, by, 70, BTN_H, "Delete", self, self.onDelete)
    self.saveBtn = ISButton:new(PAD + 228, by, 110, BTN_H, "Save to file", self, self.onSaveFile)
    local closeBtn = ISButton:new(self.width - PAD - 80, by, 80, BTN_H, "Close", self, self.onClose)
    for _, b in ipairs({self.newBtn, self.applyBtn, self.deleteBtn, self.saveBtn, closeBtn}) do
        b:initialise(); b:instantiate(); self:addChild(b)
    end

    self.section = "recipes"
    self:refresh()
end

function BanditsNPCEditorWindow:currentStation()
    return self.stationCombo:getOptionData(self.stationCombo.selected) or "cooking"
end

function BanditsNPCEditorWindow:clanList()
    if self._clanList then return self._clanList end
    local out = {}
    pcall(function()
        if BanditCustom and BanditCustom.ClanGetAll then
            for cid, clan in pairs(BanditCustom.ClanGetAll()) do
                table.insert(out, { cid = cid, name = (clan.general and clan.general.name) or cid })
            end
            table.sort(out, function(a, b) return (a.name or "") < (b.name or "") end)
        end
    end)
    self._clanList = out
    return out
end

function BanditsNPCEditorWindow:getList()
    if self.section == "recipes" then
        BanditsNPC.Production.Recipes[self:currentStation()] = BanditsNPC.Production.Recipes[self:currentStation()] or {}
        return BanditsNPC.Production.Recipes[self:currentStation()]
    elseif self.section == "dialogue" then
        return BanditsNPC.Talk.Topics
    elseif self.section == "spawn" then
        return self:clanList()
    else
        return BanditsNPC.Quests.Templates
    end
end

function BanditsNPCEditorWindow:itemLabel(item)
    if self.section == "recipes" then return item.name or "?"
    elseif self.section == "dialogue" then
        local txt = item.text or item.id or "?"
        if item.isCategory then return "[CAT] " .. txt
        elseif item.parent and item.parent ~= "" then return "      - " .. txt
        else return txt end
    elseif self.section == "spawn" then
        local on = BanditsNPC.SpawnConfig.clans[item.cid]
        return (on and "[X]  " or "[  ]  ") .. (item.name or item.cid)
    else return (item.type or "?") .. " - " .. (item.name or item.itemType or "?") end
end

function BanditsNPCEditorWindow:onTab(button) self.section = button.internal; self.selItem = nil; self:refresh() end
function BanditsNPCEditorWindow:onStationChange() self.selItem = nil; self:refresh() end

function BanditsNPCEditorWindow:refresh()
    self.stationCombo:setVisible(self.section == "recipes")
    local nFields = #(FIELDS[self.section] or {})
    for i = 1, 5 do self.entries[i]:setVisible(i <= nFields) end

    -- New/Apply/Delete don't apply to the Spawning tab (it's a clan checklist)
    local editable = (self.section ~= "spawn")
    self.newBtn:setVisible(editable)
    self.applyBtn:setVisible(editable)
    self.deleteBtn:setVisible(editable)

    self.list:clear()
    for _, it in ipairs(self:getList() or {}) do self.list:addItem(self:itemLabel(it), it) end
end

function BanditsNPCEditorWindow:onSelectItem(item)
    self.selItem = item
    if not item then return end
    if self.section == "spawn" then
        BanditsNPC.SpawnConfig.clans[item.cid] = (not BanditsNPC.SpawnConfig.clans[item.cid]) or nil
        self.msg = "Toggled. Use 'Save to file' to keep your selection."
        self:refresh()
        return
    end
    local e = self.entries
    if self.section == "recipes" then
        e[1]:setText(item.name or ""); e[2]:setText(item.output or "")
        e[3]:setText(tostring(item.outCount or 1)); e[4]:setText(tostring(item.time or 1))
        e[5]:setText(serializeInputs(item.inputs))
    elseif self.section == "dialogue" then
        e[1]:setText(item.text or ""); e[2]:setText(BanditsNPC.Editor.EncResp(item)); e[3]:setText(dlgParams(item))
    else
        e[1]:setText(item.desc or ""); e[2]:setText(item.success or ""); e[3]:setText(questParams(item))
    end
end

function BanditsNPCEditorWindow:onNew()
    if self.section == "spawn" then return end
    local list = self:getList()
    local item
    if self.section == "recipes" then
        -- Base.StewBowl, not Base.Stew: the latter DOES NOT EXIST in B42 and was the
        -- mod's own cautionary tale (a recipe that ate its inputs and paid out nothing,
        -- silently, because the id was wrong) -- reintroduced here as the default for
        -- every new recipe an author creates. v0.75.7, audit Cluster D.
        item = { name = "New Recipe", output = "Base.StewBowl", outCount = 1, time = 1, inputs = {} }
    elseif self.section == "dialogue" then
        item = { id = "custom" .. (#list + 1), text = "New line", response = "\"...\"", affinity = 1, cooldown = 6 }
    else
        item = { type = "fetch", itemType = "Base.TinnedSoup", name = "cans of soup", min = 1, max = 2, reward = 10,
                 desc = "Could you bring me {count} cans of soup?", success = "\"Thank you. Really.\"" }
    end
    table.insert(list, item)
    self.selItem = item
    self:refresh()
    self:onSelectItem(item)
    self.msg = "Added. Edit then Apply, then Save to file."
end

function BanditsNPCEditorWindow:onApply()
    if self.section == "spawn" then return end
    if not self.selItem then self.msg = "Select or create an entry first."; return end
    local e, it = self.entries, self.selItem
    if self.section == "recipes" then
        it.name = e[1]:getText(); it.output = e[2]:getText()
        it.outCount = num(e[3]:getText(), 1); it.time = num(e[4]:getText(), 1)
        it.inputs = parseInputs(e[5]:getText())
    elseif self.section == "dialogue" then
        it.text = e[1]:getText()
        it.response, it.responseExp = BanditsNPC.Editor.DecResp(e[2]:getText())
        local m = parseKV(e[3]:getText())
        if m.id and m.id ~= "" then it.id = m.id end
        it.affinity = num(m.affinity, 0); it.cooldown = num(m.cooldown, 0)
        local ma = num(m.minAffinity, 0); it.minAffinity = ma > 0 and ma or nil
        local md = num(m.minDaysKnown, 0); it.minDaysKnown = md > 0 and md or nil
        it.affectionOnly = (m.affectionOnly == "true")
        it.platonicOnly = (m.platonicOnly == "true")
        -- tri-state: empty = no track switch
        if m.setRomance == "true" then it.setRomance = true
        elseif m.setRomance == "false" then it.setRomance = false
        else it.setRomance = nil end
        it.reveals = (m.reveals == "true")
        it.needsUnrevealed = (m.needsUnrevealed == "true")
        it.parent = (m.parent and m.parent ~= "") and m.parent or nil
        it.isCategory = (m.category == "true")
        local rpName, rpLvl = m.reqPerk, num(m.reqLevel, 0)
        it.reqPerk = (rpName and rpName ~= "" and rpLvl > 0) and { perk = rpName, level = rpLvl } or nil
        it.reqTrait = (m.reqTrait and m.reqTrait ~= "") and m.reqTrait or nil
        it.reqNpcExp = (m.reqNpcExp and m.reqNpcExp ~= "") and m.reqNpcExp or nil
        it.reqRival = (m.reqRival == "true")
        it.teach = BanditsNPC.Editor.DecTeach(m.teach)
    else
        it.desc = e[1]:getText(); it.success = e[2]:getText()
        local m = parseKV(e[3]:getText())
        it.type = m.type or "fetch"; it.itemType = m.itemType or ""; it.name = m.name or ""
        it.min = num(m.min, 1); it.max = num(m.max, 1); it.reward = num(m.reward, 10)
        local qma = num(m.minAffinity, 0); it.minAffinity = qma > 0 and qma or nil
        it.npcExp = (m.npcExp and m.npcExp ~= "") and m.npcExp or nil
        it.reqPersonality = (m.reqPersonality and m.reqPersonality ~= "") and m.reqPersonality or nil
        if m.rewardItem and m.rewardItem ~= "" then local t, c = m.rewardItem:match("^([^:]*):?(.*)$"); it.rewardItem = { type = t, count = num(c, 1) } else it.rewardItem = nil end
        if m.rewardXp and m.rewardXp ~= "" then local p, a = m.rewardXp:match("^([^:]*):?(.*)$"); it.rewardXp = { perk = p, amount = num(a, 0) } else it.rewardXp = nil end
    end
    self.msg = "Applied. Use 'Save to file' to keep it."
    self:refresh()
end

function BanditsNPCEditorWindow:onDelete()
    if self.section == "spawn" then return end
    if not self.selItem then return end
    local list = self:getList()
    for i, it in ipairs(list) do if it == self.selItem then table.remove(list, i); break end end
    self.selItem = nil
    -- Say so. Delete was the ONLY action here with neither a confirmation nor a message,
    -- while the far-less-destructive beacon dismantle gets a modal -- so the destructive
    -- one was also the quietest. It is recoverable until "Save to file", and that is
    -- exactly the thing the user needed telling (v0.75.9, audit Cluster G).
    self.msg = "Removed. Use 'Save to file' to keep it, or reopen the editor to discard."
    self:refresh()
end

function BanditsNPCEditorWindow:onSaveFile()
    local ok
    if self.section == "recipes" then ok = BanditsNPC.Editor.SaveRecipes()
    elseif self.section == "dialogue" then ok = BanditsNPC.Editor.SaveDialogue()
    elseif self.section == "spawn" then ok = BanditsNPC.Editor.SaveSpawnConfig()
    else ok = BanditsNPC.Editor.SaveQuests() end
    self.msg = ok and "Saved to file." or "Save failed."
end

function BanditsNPCEditorWindow:onClose()
    self:removeFromUIManager()
    if BanditsNPCEditorWindow.instance == self then BanditsNPCEditorWindow.instance = nil end
end

function BanditsNPCEditorWindow:render()
    ISCollapsableWindow.render(self)
    if self.section == "spawn" then
        self:drawText("Tick the clans the friendly wild-spawner should draw from, then 'Save to file'.",
            self.formX, self.contentY, 0.85, 0.85, 0.85, 1, UIFont.Small)
        self:drawText("Click a clan to toggle it. None ticked = use any friendly/companion clan.",
            self.formX, self.contentY + FONT_HGT + 4, 0.7, 0.7, 0.7, 1, UIFont.Small)
        self:drawText("Group size per spawn follows each clan's own Group Size setting.",
            self.formX, self.contentY + (FONT_HGT + 4) * 2, 0.7, 0.7, 0.7, 1, UIFont.Small)
    else
        local labels = FIELDS[self.section] or {}
        for i, lab in ipairs(labels) do
            self:drawText(lab, self.formX, self.fieldY[i], 0.8, 0.8, 0.8, 1, UIFont.Small)
        end
    end
    if self.msg then
        self:drawText(self.msg, self.formX, self.height - PAD - BTN_H - FONT_HGT - 6, 0.95, 0.95, 0.6, 1, UIFont.Small)
    end
end

function BanditsNPCEditorWindow:new()
    local w, h = 860, 560
    local x = (getCore():getScreenWidth() - w) / 2
    local y = (getCore():getScreenHeight() - h) / 2
    local o = ISCollapsableWindow.new(self, x, y, w, h)
    o.title = "NPC Core Editor"
    o.resizable = false
    return o
end

function BanditsNPCEditorWindow.OpenIt()
    if BanditsNPCEditorWindow.instance then BanditsNPCEditorWindow.instance:onClose() end
    local win = BanditsNPCEditorWindow:new()
    win:initialise()
    win:addToUIManager()
    BanditsNPCEditorWindow.instance = win
    return win
end
