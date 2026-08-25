--
-- Bandits NPC - Companions Overhaul - Spawn Window (Phase 1)
--
-- A standalone window opened from the ground right-click menu. Left column
-- lists all Bandit Maker groups (clans), the middle column lists the NPCs in
-- the selected group, and the right side shows a live 3D preview of the
-- selected NPC plus their details. "Spawn" places exactly one NPC.
--
-- The 3D preview is built from the NPC's saved profile (skin/hair/beard/
-- clothing) into a SurvivorDesc and rendered with ISUI3DModel -- the same
-- engine widget the character-creation screen uses.
--

require "ISUI/ISCollapsableWindow"
require "ISUI/ISScrollingListBox"
require "ISUI/ISButton"
require "ISUI/ISComboBox"
require "ISUI/ISUI3DModel"

BanditsNPCSpawnWindow = ISCollapsableWindow:derive("BanditsNPCSpawnWindow")

local FONT_HGT = getTextManager():getFontHeight(UIFont.Small)
local PAD = 10

-- Reverse map: expertise id -> readable name (Bandit.Expertise is name->id).
local function buildExpertiseNames()
    local names = {}
    if Bandit and Bandit.Expertise then
        for name, id in pairs(Bandit.Expertise) do
            names[id] = name
        end
    end
    return names
end

function BanditsNPCSpawnWindow:setSpawnTarget(x, y, z)
    self.spawnX, self.spawnY, self.spawnZ = x, y, z
end

function BanditsNPCSpawnWindow:createChildren()
    ISCollapsableWindow.createChildren(self)

    local headerY = self:titleBarHeight() + 4
    local top = headerY + FONT_HGT + 4
    local bottomBarH = 40
    local listH = self.height - top - bottomBarH - PAD

    -- Groups list (left)
    self.groupList = ISScrollingListBox:new(PAD, top, 200, listH)
    self.groupList:initialise()
    self.groupList.itemheight = FONT_HGT + 6
    self.groupList.font = UIFont.Small
    self.groupList:setOnMouseDownFunction(self, self.onGroupSelected)
    self:addChild(self.groupList)

    -- NPC list (middle)
    self.npcList = ISScrollingListBox:new(220, top, 200, listH)
    self.npcList:initialise()
    self.npcList.itemheight = FONT_HGT + 6
    self.npcList.font = UIFont.Small
    self.npcList:setOnMouseDownFunction(self, self.onNpcSelected)
    self:addChild(self.npcList)

    -- (3D preview removed: ISUI3DModel leaks render-buffers/FBOs, which caused
    --  world-render crashes when windows are opened repeatedly.)

    -- Details area is drawn in render().
    self.detailsX = 440
    self.detailsY = top
    self.detailLines = {}

    -- Bottom bar: program selector + action buttons
    local by = self.height - bottomBarH + (bottomBarH - (FONT_HGT + 8)) / 2
    local btnH = FONT_HGT + 8

    self.programCombo = ISComboBox:new(70, by, 180, btnH, self, nil)
    self.programCombo:initialise()
    self.programCombo:addOptionWithData(BanditsNPC.T("UI_BN_Spawn_Companion", "Companion (follows you)"), "NPCCompanion")
    self.programCombo:addOptionWithData(BanditsNPC.T("UI_BN_Spawn_Neutral", "Neutral wanderer"), "NPCNeutral")
    self.programCombo:addOptionWithData(BanditsNPC.T("UI_BN_Spawn_Hostile", "Hostile bandit"), "Bandit")
    self.programCombo.selected = 2   -- default Neutral so you recruit via dialogue
    self:addChild(self.programCombo)

    local closeBtn = ISButton:new(self.width - PAD - 80, by, 80, btnH, BanditsNPC.T("UI_BN_Close", "Close"), self, self.onClose)
    closeBtn:initialise(); closeBtn:instantiate()
    self:addChild(closeBtn)

    self.spawnBtn = ISButton:new(closeBtn.x - PAD - 90, by, 90, btnH, BanditsNPC.T("UI_BN_Spawn", "Spawn"), self, self.onSpawn)
    self.spawnBtn:initialise(); self.spawnBtn:instantiate()
    self:addChild(self.spawnBtn)

    self.randomBtn = ISButton:new(self.spawnBtn.x - PAD - 90, by, 90, btnH, BanditsNPC.T("UI_BN_Random", "Random"), self, self.onRandom)
    self.randomBtn:initialise(); self.randomBtn:instantiate()
    self:addChild(self.randomBtn)

    self.expertiseNames = buildExpertiseNames()

    self:refreshGroups()
end

function BanditsNPCSpawnWindow:refreshGroups()
    self.groupList:clear()
    self.npcList:clear()
    self.detailLines = {}
    self.selectedCid = nil
    self.selectedBid = nil
    self.selectedBandit = nil

    if not BanditCustom then return end
    BanditCustom.Load()

    local clans = BanditCustom.ClanGetAllSorted()
    for cid, clan in pairs(clans) do
        local name = (clan.general and clan.general.name) or cid
        self.groupList:addItem(name, {cid = cid, clan = clan})
    end
end

function BanditsNPCSpawnWindow:onGroupSelected(item)
    if not item then return end
    self.selectedCid = item.cid
    self.selectedBid = nil
    self.selectedBandit = nil
    self.detailLines = {BanditsNPC.T("UI_BN_Spawn_SelectHint", "Select an NPC, or press Random/Spawn for a random member.")}

    self.npcList:clear()
    local bandits = BanditCustom.GetFromClanSorted(item.cid)
    for bid, bandit in pairs(bandits) do
        local name = (bandit.general and bandit.general.name) or bid
        self.npcList:addItem(name, {bid = bid, bandit = bandit})
    end
end

function BanditsNPCSpawnWindow:onNpcSelected(item)
    if not item then return end
    self.selectedBid = item.bid
    self.selectedBandit = item.bandit
    self:updateDetails(item.bandit)
end

function BanditsNPCSpawnWindow:updateDetails(bandit)
    local lines = {}
    local g = bandit.general or {}
    table.insert(lines, BanditsNPC.TF("UI_BN_Spawn_Name", "Name: %1", tostring(g.name or "?")))
    table.insert(lines, BanditsNPC.TF("UI_BN_Spawn_Sex", "Sex: %1", (g.female and BanditsNPC.T("UI_BN_Female", "Female") or BanditsNPC.T("UI_BN_Male", "Male"))))
    table.insert(lines, BanditsNPC.TF("UI_BN_Spawn_HealthStr", "Health: %1  Str: %2", tostring(g.health or "-"), tostring(g.strength or "-")))
    table.insert(lines, BanditsNPC.TF("UI_BN_Spawn_EndSight", "Endurance: %1  Sight: %2", tostring(g.endurance or "-"), tostring(g.sight or "-")))

    local skills = {}
    for _, key in ipairs({"exp1", "exp2", "exp3"}) do
        local id = g[key]
        if id and id > 0 and self.expertiseNames[id] then
            table.insert(skills, self.expertiseNames[id])
        end
    end
    table.insert(lines, BanditsNPC.TF("UI_BN_Spawn_Skills", "Skills: %1", (#skills > 0 and table.concat(skills, ", ") or BanditsNPC.T("UI_BN_None", "None"))))

    if bandit.weapons then
        local w = bandit.weapons
        local wt = w.primary or w.melee
        table.insert(lines, BanditsNPC.TF("UI_BN_Spawn_Weapon", "Weapon: %1", tostring(wt or BanditsNPC.T("UI_BN_Spawn_Unarmed", "Unarmed"))))
    end

    self.detailLines = lines
end

function BanditsNPCSpawnWindow:onSpawn()
    if not self.selectedCid then
        self.detailLines = {BanditsNPC.T("UI_BN_Spawn_PickGroup", "Pick a group first.")}
        return
    end
    local program = self.programCombo:getOptionData(self.programCombo.selected)
    BanditsNPC.Spawn.SendSpawn(self.selectedCid, self.selectedBid, program,
                              self.spawnX, self.spawnY, self.spawnZ)
end

function BanditsNPCSpawnWindow:onRandom()
    local cid = BanditsNPC.Spawn.PickRandomCompanionClan()
    if not cid then
        self.detailLines = {BanditsNPC.T("UI_BN_Spawn_NoGroups", "No groups available.")}
        return
    end
    local program = self.programCombo:getOptionData(self.programCombo.selected)
    BanditsNPC.Spawn.SendSpawn(cid, nil, program, self.spawnX, self.spawnY, self.spawnZ)
end

function BanditsNPCSpawnWindow:onClose()
    self:removeFromUIManager()
    if BanditsNPCSpawnWindow.instance == self then
        BanditsNPCSpawnWindow.instance = nil
    end
end

function BanditsNPCSpawnWindow:prerender()
    ISCollapsableWindow.prerender(self)
    local headerY = self:titleBarHeight() + 4
    self:drawText(BanditsNPC.T("UI_BN_Spawn_Groups", "Groups"), self.groupList.x, headerY, 1, 1, 1, 1, UIFont.Small)
    self:drawText(BanditsNPC.T("UI_BN_Spawn_NPCs", "NPCs"), self.npcList.x, headerY, 1, 1, 1, 1, UIFont.Small)
    self:drawText(BanditsNPC.T("UI_BN_Spawn_Details", "Details"), self.detailsX, headerY, 1, 1, 1, 1, UIFont.Small)
    self:drawText(BanditsNPC.T("UI_BN_Spawn_SpawnAs", "Spawn as:"), PAD, self.programCombo.y + 4, 1, 1, 1, 1, UIFont.Small)
end

function BanditsNPCSpawnWindow:render()
    ISCollapsableWindow.render(self)
    if self.detailLines then
        local y = self.detailsY
        for _, line in ipairs(self.detailLines) do
            self:drawText(line, self.detailsX, y, 0.9, 0.9, 0.9, 1, UIFont.Small)
            y = y + FONT_HGT + 4
        end
    end
end

function BanditsNPCSpawnWindow:new()
    local w, h = 780, 500
    local x = (getCore():getScreenWidth() - w) / 2
    local y = (getCore():getScreenHeight() - h) / 2
    local o = ISCollapsableWindow.new(self, x, y, w, h)
    o.title = BanditsNPC.T("UI_BN_Title_Spawn", "Spawn NPC")
    o.resizable = false
    return o
end

-- Opens (or re-opens) the spawn window targeting the given square.
function BanditsNPCSpawnWindow.OpenAt(square)
    if BanditsNPCSpawnWindow.instance then
        BanditsNPCSpawnWindow.instance:onClose()
    end
    local win = BanditsNPCSpawnWindow:new()
    win:initialise()
    win:addToUIManager()
    if square then
        win:setSpawnTarget(square:getX(), square:getY(), square:getZ())
    end
    BanditsNPCSpawnWindow.instance = win
    return win
end
