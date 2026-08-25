-- QP Survivor Contracts
-- v1.1.4 Reputation Integration TC - optional dependency and reward polish

require "ISUI/ISPanel"
require "ISUI/ISButton"
require "ISUI/ISTextEntryBox"
require "ISUI/ISScrollingListBox"
require "ISUI/ISComboBox"
require "QPSurvivorContracts/QPSC_I18N"
require "QPSurvivorContracts/QPSC_FlexibleLayout"

QPSC_TrackedUI = QPSC_TrackedUI or {}
QPSC_TrackedUI.itemCatalog = QPSC_TrackedUI.itemCatalog or nil
QPSC_TrackedUI.itemCatalogBuild =
    QPSC_TrackedUI.itemCatalogBuild or nil
QPSC_TrackedUI.window = QPSC_TrackedUI.window or nil

local QPSC_TUI_REPUTATION_DEFINITIONS = {
    {key="", labelKey="UI_QPSC_None", label="None"},
    {key="community", label="Community"},
    {key="hunter", label="Hunter"},
    {key="explorer", label="Explorer"},
    {key="medic", label="Medic"},
    {key="mechanic", label="Mechanic"},
    {key="builder", label="Builder"}
}

local function QPSC_TUI_reputationLabel(definition)
    if definition == nil then return "" end
    if definition.labelKey ~= nil and definition.labelKey ~= "" then
        return QPSC_I18N.getText(definition.labelKey)
    end
    return tostring(definition.label or "")
end

local function QPSC_TUI_reputationAvailable()
    return QPReputation ~= nil
        and QPReputation.Client ~= nil
end

local function QPSC_TUI_trim(value)
    return tostring(value or "")
        :gsub("^%s+", "")
        :gsub("%s+$", "")
end

local function QPSC_TUI_limit(value, maximum)
    local text = tostring(value or "")
    local limit = tonumber(maximum) or 200

    if string.len(text) > limit then
        return string.sub(text, 1, limit)
    end

    return text
end

local function QPSC_TUI_scriptValue(item, methodName)
    if item == nil or item[methodName] == nil then
        return ""
    end

    local ok, value = pcall(function()
        return item[methodName](item)
    end)

    if not ok or value == nil then return "" end
    return tostring(value)
end

local function QPSC_TUI_makeCatalogEntry(scriptItem)
    if scriptItem == nil then return nil end

    local fullType = QPSC_TUI_scriptValue(
        scriptItem,
        "getFullName"
    )
    local displayName = QPSC_TUI_scriptValue(
        scriptItem,
        "getDisplayName"
    )
    local typeName = QPSC_TUI_scriptValue(
        scriptItem,
        "getName"
    )

    if fullType == "" or displayName == "" then
        return nil
    end

    if scriptItem.getObsolete ~= nil then
        local ok, obsolete = pcall(function()
            return scriptItem:getObsolete()
        end)

        if ok and obsolete == true then return nil end
    end

    if scriptItem.isHidden ~= nil then
        local ok, hidden = pcall(function()
            return scriptItem:isHidden()
        end)

        if ok and hidden == true then return nil end
    end

    return {
        fullType = fullType,
        displayName = displayName,
        typeName = typeName,
        searchText = string.lower(
            displayName
                .. " "
                .. fullType
                .. " "
                .. typeName
        )
    }
end

local function QPSC_TUI_getAllScriptItems()
    local ok, result = pcall(function()
        if getScriptManager ~= nil then
            return getScriptManager():getAllItems()
        end

        if getAllItems ~= nil then
            return getAllItems()
        end

        return nil
    end)

    if not ok then return nil end
    return result
end

local function QPSC_TUI_startCatalog()
    if QPSC_TrackedUI.itemCatalog ~= nil then
        return true
    end

    if QPSC_TrackedUI.itemCatalogBuild ~= nil then
        return false
    end

    local source = QPSC_TUI_getAllScriptItems()

    if source == nil then
        QPSC_TrackedUI.itemCatalog = {}
        return true
    end

    QPSC_TrackedUI.itemCatalogBuild = {
        source = source,
        index = 0,
        total = source:size(),
        catalog = {},
        seen = {}
    }

    return false
end

local function QPSC_TUI_stepCatalog(batchSize)
    if QPSC_TrackedUI.itemCatalog ~= nil then
        return true
    end

    QPSC_TUI_startCatalog()

    local build = QPSC_TrackedUI.itemCatalogBuild
    if build == nil then
        return QPSC_TrackedUI.itemCatalog ~= nil
    end

    local maximum = tonumber(batchSize) or 200
    local processed = 0

    while build.index < build.total
        and processed < maximum do
        local scriptItem = build.source:get(build.index)
        build.index = build.index + 1
        processed = processed + 1

        local entry = QPSC_TUI_makeCatalogEntry(scriptItem)

        if entry ~= nil and not build.seen[entry.fullType] then
            build.seen[entry.fullType] = true
            table.insert(build.catalog, entry)
        end
    end

    if build.index >= build.total then
        table.sort(build.catalog, function(left, right)
            local leftName = string.lower(
                tostring(left.displayName or "")
            )
            local rightName = string.lower(
                tostring(right.displayName or "")
            )

            if leftName == rightName then
                return tostring(left.fullType or "")
                    < tostring(right.fullType or "")
            end

            return leftName < rightName
        end)

        QPSC_TrackedUI.itemCatalog = build.catalog
        QPSC_TrackedUI.itemCatalogBuild = nil
        print(
            "[QPSC] Tracked item catalog ready: "
                .. tostring(#QPSC_TrackedUI.itemCatalog)
        )
        return true
    end

    return false
end

local function QPSC_TUI_itemRank(entry, searchLower)
    local display = string.lower(
        tostring(entry.displayName or "")
    )
    local fullType = string.lower(
        tostring(entry.fullType or "")
    )
    local typeName = string.lower(
        tostring(entry.typeName or "")
    )

    if display == searchLower then return 0 end
    if typeName == searchLower then return 1 end
    if fullType == searchLower then return 2 end

    if string.sub(display, 1, #searchLower)
        == searchLower then return 3 end
    if string.sub(typeName, 1, #searchLower)
        == searchLower then return 4 end
    if string.sub(fullType, 1, #searchLower)
        == searchLower then return 5 end

    if string.find(display, searchLower, 1, true) then
        return 6
    end
    if string.find(typeName, searchLower, 1, true) then
        return 7
    end
    if string.find(fullType, searchLower, 1, true) then
        return 8
    end

    return nil
end

local function QPSC_TUI_drawCatalogItem(list, y, row, alt)
    local height = tonumber(list.itemheight) or 26

    if alt then
        list:drawRect(
            0,
            y,
            list.width,
            height - 1,
            0.04,
            1,
            1,
            1
        )
    end

    if list.selected == row.index then
        list:drawRect(
            0,
            y,
            list.width,
            height - 1,
            0.32,
            0.45,
            0.27,
            0.12
        )
        list:drawRectBorder(
            0,
            y,
            list.width,
            height - 1,
            0.84,
            0.78,
            0.63,
            0.25
        )
    end

    list:drawText(
        tostring(row.text or ""),
        8,
        y + 3,
        0.9,
        0.9,
        0.9,
        1,
        UIFont.Small
    )

    return y + height
end

QPSC_TrackedContractWindow =
    ISPanel:derive("QPSC_TrackedContractWindow")

local QPSC_TUI_MIN_WIDTH = 860
local QPSC_TUI_MIN_HEIGHT = 840
local QPSC_TUI_RESIZE_GRIP = 34

local QPSC_TUI_CATEGORY_DEFINITIONS = {
    {key="NONE", labelKey="UI_QPSC_CategoryNone"},
    {key="FUEL", labelKey="UI_QPSC_CategoryFuel"},
    {key="FOOD", labelKey="UI_QPSC_CategoryFood"},
    {key="MECHANIC", labelKey="UI_QPSC_CategoryMechanic"},
    {key="MEDICAL", labelKey="UI_QPSC_CategoryMedical"},
    {key="CONSTRUCTION", labelKey="UI_QPSC_CategoryConstruction"},
    {key="DELIVERY", labelKey="UI_QPSC_CategoryDelivery"},
    {key="DANGER", labelKey="UI_QPSC_CategoryDanger"}
}

local QPSC_TUI_DIFFICULTY_DEFINITIONS = {
    {key="UNRATED", labelKey="UI_QPSC_DifficultyUnrated"},
    {key="EASY", labelKey="UI_QPSC_DifficultyEasy"},
    {key="MEDIUM", labelKey="UI_QPSC_DifficultyMedium"},
    {key="HARD", labelKey="UI_QPSC_DifficultyHard"}
}

local QPSC_TUI_COMPLETION_MODE_DEFINITIONS = {
    {key="INDIVIDUAL", labelKey="UI_QPSC_CompletionModeIndividual"},
    {key="GLOBAL", labelKey="UI_QPSC_CompletionModeGlobal"},
    {key="SHARED_TEAM", labelKey="UI_QPSC_CompletionModeSharedTeam"}
}

local function QPSC_TUI_normalizeCompletionMode(value)
    local mode = string.upper(tostring(value or "INDIVIDUAL"))

    if mode == "GLOBAL" or mode == "SHARED_TEAM" then
        return mode
    end

    return "INDIVIDUAL"
end

local function QPSC_TUI_normalizeDifficulty(value)
    local difficulty = string.upper(tostring(value or "UNRATED"))

    if difficulty == "EASY"
        or difficulty == "MEDIUM"
        or difficulty == "HARD" then
        return difficulty
    end

    return "UNRATED"
end

local function QPSC_TUI_setVisible(control, visible)
    if control == nil then return end

    if control.setVisible ~= nil then
        control:setVisible(visible == true)
    else
        control.visible = visible == true
    end
end

local function QPSC_TUI_noop()
end

local function QPSC_TUI_contractHasCompletedParticipant(contract)
    for _, participant in ipairs(
        contract and contract.participants or {}
    ) do
        if tostring(participant.status or "") == "Completed" then
            return true
        end
    end

    return false
end

local function QPSC_TUI_contractParticipantCount(contract)
    return #(contract and contract.participants or {})
end

local function QPSC_TUI_itemFromContract(fullType, displayName)
    fullType = tostring(fullType or "")
    displayName = tostring(displayName or "")

    if fullType == "" then return nil end
    if displayName == "" then displayName = fullType end

    return {
        fullType = fullType,
        displayName = displayName,
        typeName = fullType,
        searchText = string.lower(displayName .. " " .. fullType)
    }
end

-- QPSC_V130_MULTIPLE_REWARDS_V1
local QPSC_TUI_MAX_REWARD_ITEMS = 5

local function QPSC_TUI_rewardItemsFromContract(contract)
    local rewards = {}
    local rawRewards =
        type(contract and contract.rewardItems) == "table"
        and contract.rewardItems
        or nil

    if rawRewards ~= nil then
        for index = 1, math.min(
            #rawRewards,
            QPSC_TUI_MAX_REWARD_ITEMS
        ) do
            local raw = rawRewards[index] or {}
            local fullType = tostring(
                raw.fullType or raw.itemFullType or ""
            )
            local displayName = tostring(
                raw.displayName
                or raw.itemDisplayName
                or fullType
            )
            local quantity = math.max(
                0,
                math.floor(
                    tonumber(raw.quantity) or 0
                )
            )

            if fullType ~= "" and quantity > 0 then
                table.insert(rewards, {
                    fullType = fullType,
                    displayName = displayName,
                    quantity = quantity
                })
            end
        end
    end

    if #rewards == 0 and contract ~= nil then
        local fullType =
            tostring(contract.rewardItemFullType or "")
        local quantity = math.max(
            0,
            math.floor(
                tonumber(contract.rewardQuantity) or 0
            )
        )

        if fullType ~= "" and quantity > 0 then
            table.insert(rewards, {
                fullType = fullType,
                displayName = tostring(
                    contract.rewardItemDisplayName
                    or fullType
                ),
                quantity = quantity
            })
        end
    end

    return rewards
end

local function QPSC_TUI_rewardSummary(rewards)
    local parts = {}

    for _, reward in ipairs(rewards or {}) do
        table.insert(
            parts,
            tostring(reward.quantity)
                .. " x "
                .. tostring(
                    reward.displayName or reward.fullType
                )
        )
    end

    if #parts == 0 then
        return QPSC_I18N.getText("UI_QPSC_None")
    end

    return table.concat(parts, " + ")
end

local function QPSC_TUI_addRewardItem(
    rewards,
    item,
    quantity
)
    if item == nil then
        return false, "item"
    end

    local fullType = tostring(item.fullType or "")
    local displayName = tostring(
        item.displayName or fullType
    )
    local amount = math.floor(
        tonumber(quantity) or 0
    )

    if fullType == ""
        or amount < 1
        or amount > 100 then
        return false, "quantity"
    end

    for _, reward in ipairs(rewards or {}) do
        if tostring(reward.fullType or "") == fullType then
            reward.displayName = displayName
            reward.quantity = amount
            return true, "updated"
        end
    end

    if #(rewards or {}) >= QPSC_TUI_MAX_REWARD_ITEMS then
        return false, "limit"
    end

    table.insert(rewards, {
        fullType = fullType,
        displayName = displayName,
        quantity = amount
    })

    return true, "added"
end

local function QPSC_TUI_applyRewardArgs(args, rewards)
    local list = rewards or {}
    args.rewardCount = #list

    for index = 1, QPSC_TUI_MAX_REWARD_ITEMS do
        local reward = list[index]
        local prefix = "reward" .. tostring(index)

        args[prefix .. "ItemFullType"] =
            reward and tostring(reward.fullType or "") or ""
        args[prefix .. "ItemDisplayName"] =
            reward and tostring(
                reward.displayName
                or reward.fullType
                or ""
            ) or ""
        args[prefix .. "Quantity"] =
            reward and math.floor(
                tonumber(reward.quantity) or 0
            ) or 0
    end

    local first = list[1]
    args.rewardItemFullType =
        first and tostring(first.fullType or "") or ""
    args.rewardItemDisplayName =
        first and tostring(
            first.displayName
            or first.fullType
            or ""
        ) or ""
    args.rewardQuantity =
        first and math.floor(
            tonumber(first.quantity) or 0
        ) or 0

    return args
end

function QPSC_TrackedContractWindow:new(
    x,
    y,
    width,
    height,
    player,
    categoryKey,
    editContract,
    forceManual
)
    local o = ISPanel:new(x, y, width, height)
    setmetatable(o, self)
    self.__index = self

    o.player = player
    o.categoryKey = tostring(categoryKey or "NONE")
    o.editContract = editContract
    o.isEditMode = editContract ~= nil
    o.forceManual = forceManual == true
    o.reputationAvailable = QPSC_TUI_reputationAvailable()
    o.moveWithMouse = true
    o.backgroundColor = {r=0.03, g=0.03, b=0.03, a=0.97}
    o.borderColor = {r=0.80, g=0.66, b=0.24, a=0.95}
    o.objectiveTypes = o.forceManual
        and {"MANUAL"}
        or (
            o.isEditMode
            and {"MANUAL", "DELIVERY", "KILL", "LOCATION"}
            or {"DELIVERY", "KILL", "LOCATION"}
        )
    o.lastObjectiveType = nil
    o.killRadiusText = "100"
    o.locationRadiusText = "3"
    o.selectedObjectiveItem = nil
    o.rewardItems =
        QPSC_TUI_rewardItemsFromContract(editContract)
    o.selectedRewardItem =
        o.rewardItems[1]
        and QPSC_TUI_itemFromContract(
            o.rewardItems[1].fullType,
            o.rewardItems[1].displayName
        )
        or nil
    o.selectedFirstFinisherBonusItem = nil
    o.observedSearchText = ""
    o.lastSearchText = nil
    o.searchDelayFrames = 0
    o.matchCount = 0
    o.resizing = false
    o.resizeGripSize = QPSC_TUI_RESIZE_GRIP
    o.minWindowWidth = QPSC_TUI_MIN_WIDTH
    o.minWindowHeight = QPSC_TUI_MIN_HEIGHT
    o.lastLayoutWidth = nil
    o.lastLayoutHeight = nil
    o.objectiveTypeLocked = o.forceManual
        or (
            o.isEditMode
            and QPSC_TUI_contractParticipantCount(editContract) > 0
        )
    o.objectiveSettingsLocked = o.isEditMode
        and QPSC_TUI_contractHasCompletedParticipant(editContract)
    o.objectiveItemLocked = o.objectiveTypeLocked
    o.completionModeLocked = o.objectiveTypeLocked
    o.legacyRewardText = tostring(
        editContract and editContract.reward or ""
    )

    if o.isEditMode then
        o.selectedObjectiveItem = QPSC_TUI_itemFromContract(
            editContract.objectiveItemFullType,
            editContract.objectiveItemDisplayName
        )
        o.selectedFirstFinisherBonusItem = QPSC_TUI_itemFromContract(
            editContract.firstFinisherBonusItemFullType,
            editContract.firstFinisherBonusItemDisplayName
        )
    end

    return o
end

function QPSC_TrackedContractWindow:initialise()
    ISPanel.initialise(self)

    local margin = 20
    local fieldWidth = self.width - (margin * 2)

    self.titleEntry = ISTextEntryBox:new("", margin, 72, fieldWidth, 28)
    self.titleEntry:initialise()
    self.titleEntry:instantiate()
    self:addChild(self.titleEntry)

    self.locationEntry = ISTextEntryBox:new("", margin, 132, fieldWidth, 28)
    self.locationEntry:initialise()
    self.locationEntry:instantiate()
    self:addChild(self.locationEntry)

    self.descriptionEntry = ISTextEntryBox:new("", margin, 192, fieldWidth, 28)
    self.descriptionEntry:initialise()
    self.descriptionEntry:instantiate()
    self:addChild(self.descriptionEntry)

    self.categoryCombo = ISComboBox:new(
        margin, 252, 230, 28, self, QPSC_TUI_noop
    )
    self.categoryCombo:initialise()
    self.categoryCombo:instantiate()
    for _, definition in ipairs(QPSC_TUI_CATEGORY_DEFINITIONS) do
        self.categoryCombo:addOption(
            QPSC_I18N.getText(definition.labelKey)
        )
    end
    self:addChild(self.categoryCombo)

    self.difficultyCombo = ISComboBox:new(
        270, 252, 190, 28, self, QPSC_TUI_noop
    )
    self.difficultyCombo:initialise()
    self.difficultyCombo:instantiate()
    for _, definition in ipairs(QPSC_TUI_DIFFICULTY_DEFINITIONS) do
        self.difficultyCombo:addOption(
            QPSC_I18N.getText(definition.labelKey)
        )
    end
    self:addChild(self.difficultyCombo)

    self.completionModeCombo = ISComboBox:new(
        480, 252, 220, 28, self,
        QPSC_TrackedContractWindow.onCompletionModeChanged
    )
    self.completionModeCombo:initialise()
    self.completionModeCombo:instantiate()
    for _, definition in ipairs(
        QPSC_TUI_COMPLETION_MODE_DEFINITIONS
    ) do
        self.completionModeCombo:addOption(
            QPSC_I18N.getText(definition.labelKey)
        )
    end
    self.completionModeCombo.selected = 1
    self:addChild(self.completionModeCombo)

    self.objectiveCombo = ISComboBox:new(
        margin, 312, 235, 28, self,
        QPSC_TrackedContractWindow.onObjectiveChanged
    )
    self.objectiveCombo:initialise()
    self.objectiveCombo:instantiate()
    for _, objectiveType in ipairs(self.objectiveTypes) do
        if objectiveType == "MANUAL" then
            self.objectiveCombo:addOption(
                QPSC_I18N.getText("UI_QPSC_ObjectiveManual")
            )
        elseif objectiveType == "DELIVERY" then
            self.objectiveCombo:addOption(
                QPSC_I18N.getText("UI_QPSC_ObjectiveDelivery")
            )
        elseif objectiveType == "KILL" then
            self.objectiveCombo:addOption(
                QPSC_I18N.getText("UI_QPSC_ObjectiveKill")
            )
        else
            self.objectiveCombo:addOption(
                QPSC_I18N.getText("UI_QPSC_ObjectiveLocation")
            )
        end
    end
    self.objectiveCombo.selected = 1
    self:addChild(self.objectiveCombo)

    self.targetEntry = ISTextEntryBox:new("10", 275, 312, 115, 28)
    self.targetEntry:initialise()
    self.targetEntry:instantiate()
    self.targetEntry:setOnlyNumbers(true)
    self:addChild(self.targetEntry)

    self.radiusEntry = ISTextEntryBox:new("", 410, 312, 105, 28)
    self.radiusEntry:initialise()
    self.radiusEntry:instantiate()
    self.radiusEntry:setOnlyNumbers(true)
    self:addChild(self.radiusEntry)

    self.timeEntry = ISTextEntryBox:new(
        "", 535, 312, self.width - 555, 28
    )
    self.timeEntry:initialise()
    self.timeEntry:instantiate()
    self:addChild(self.timeEntry)

    self.searchEntry = ISTextEntryBox:new("", margin, 372, fieldWidth, 28)
    self.searchEntry:initialise()
    self.searchEntry:instantiate()
    self:addChild(self.searchEntry)

    self.itemList = ISScrollingListBox:new(
        margin, 406, fieldWidth, 110
    )
    self.itemList:initialise()
    self.itemList:instantiate()
    self.itemList.itemheight = 26
    self.itemList.font = UIFont.Small
    self.itemList.doDrawItem = QPSC_TUI_drawCatalogItem
    self.itemList.selected = 0
    self:addChild(self.itemList)

    self.setObjectiveButton = ISButton:new(
        margin, 540, 250, 28,
        QPSC_I18N.getText("UI_QPSC_SetObjectiveItem"),
        self, QPSC_TrackedContractWindow.onSetObjectiveItem
    )
    self.setObjectiveButton:initialise()
    self.setObjectiveButton:instantiate()
    self:addChild(self.setObjectiveButton)

    self.manualRewardEntry = ISTextEntryBox:new(
        "", margin, 540, fieldWidth, 28
    )
    self.manualRewardEntry:initialise()
    self.manualRewardEntry:instantiate()
    self:addChild(self.manualRewardEntry)
    QPSC_TUI_setVisible(self.manualRewardEntry, false)

    self.setRewardButton = ISButton:new(
        margin, 620, 250, 28,
        QPSC_I18N.getText("UI_QPSC_AddRewardItem"),
        self, QPSC_TrackedContractWindow.onSetRewardItem
    )
    self.setRewardButton:initialise()
    self.setRewardButton:instantiate()
    self:addChild(self.setRewardButton)

    self.clearRewardButton = ISButton:new(
        280, 620, 180, 28,
        QPSC_I18N.getText("UI_QPSC_ClearRewards"),
        self, QPSC_TrackedContractWindow.onClearReward
    )
    self.clearRewardButton:initialise()
    self.clearRewardButton:instantiate()
    self:addChild(self.clearRewardButton)

    self.rewardQuantityEntry = ISTextEntryBox:new(
        "1", self.width - 110, 620, 90, 28
    )
    self.rewardQuantityEntry:initialise()
    self.rewardQuantityEntry:instantiate()
    self.rewardQuantityEntry:setOnlyNumbers(true)
    self:addChild(self.rewardQuantityEntry)

    self.reputationCombo = ISComboBox:new(
        margin, 680, 260, 28, self, QPSC_TUI_noop
    )
    self.reputationCombo:initialise()
    self.reputationCombo:instantiate()
    for _, definition in ipairs(
        QPSC_TUI_REPUTATION_DEFINITIONS
    ) do
        self.reputationCombo:addOption(QPSC_TUI_reputationLabel(definition))
    end
    self.reputationCombo.selected = 1
    self:addChild(self.reputationCombo)

    self.reputationPointsEntry = ISTextEntryBox:new(
        "0", self.width - 150, 680, 126, 28
    )
    self.reputationPointsEntry:initialise()
    self.reputationPointsEntry:instantiate()
    self.reputationPointsEntry:setOnlyNumbers(true)
    self:addChild(self.reputationPointsEntry)

    self.secondaryReputationCombo = ISComboBox:new(
        margin, 714, 260, 28, self, QPSC_TUI_noop
    )
    self.secondaryReputationCombo:initialise()
    self.secondaryReputationCombo:instantiate()
    for _, definition in ipairs(
        QPSC_TUI_REPUTATION_DEFINITIONS
    ) do
        self.secondaryReputationCombo:addOption(
            QPSC_TUI_reputationLabel(definition)
        )
    end
    self.secondaryReputationCombo.selected = 1
    self:addChild(self.secondaryReputationCombo)

    self.secondaryReputationPointsEntry = ISTextEntryBox:new(
        "0", self.width - 150, 714, 126, 28
    )
    self.secondaryReputationPointsEntry:initialise()
    self.secondaryReputationPointsEntry:instantiate()
    self.secondaryReputationPointsEntry:setOnlyNumbers(true)
    self:addChild(self.secondaryReputationPointsEntry)

    self.setFirstFinisherBonusButton = ISButton:new(
        margin, 700, 250, 28,
        QPSC_I18N.getText("UI_QPSC_SetFirstFinisherBonusItem"),
        self, QPSC_TrackedContractWindow.onSetFirstFinisherBonusItem
    )
    self.setFirstFinisherBonusButton:initialise()
    self.setFirstFinisherBonusButton:instantiate()
    self:addChild(self.setFirstFinisherBonusButton)

    self.clearFirstFinisherBonusButton = ISButton:new(
        280, 700, 180, 28,
        QPSC_I18N.getText("UI_QPSC_ClearFirstFinisherBonus"),
        self, QPSC_TrackedContractWindow.onClearFirstFinisherBonus
    )
    self.clearFirstFinisherBonusButton:initialise()
    self.clearFirstFinisherBonusButton:instantiate()
    self:addChild(self.clearFirstFinisherBonusButton)

    self.firstFinisherBonusQuantityEntry = ISTextEntryBox:new(
        "1", self.width - 110, 700, 90, 28
    )
    self.firstFinisherBonusQuantityEntry:initialise()
    self.firstFinisherBonusQuantityEntry:instantiate()
    self.firstFinisherBonusQuantityEntry:setOnlyNumbers(true)
    self:addChild(self.firstFinisherBonusQuantityEntry)

    self.createButton = ISButton:new(
        margin, self.height - 42, 280, 28,
        self.isEditMode
            and QPSC_I18N.getText("UI_QPSC_SaveChanges")
            or (
                self.forceManual
                and QPSC_I18N.getText("UI_QPSC_CreateCustomContract")
                or QPSC_I18N.getText("UI_QPSC_CreateTracked")
            ),
        self, QPSC_TrackedContractWindow.onCreate
    )
    self.createButton:initialise()
    self.createButton:instantiate()
    self:addChild(self.createButton)

    self.cancelButton = ISButton:new(
        self.width - 200, self.height - 42, 180, 28,
        QPSC_I18N.getText("UI_QPSC_Close"),
        self, QPSC_TrackedContractWindow.onCancel
    )
    self.cancelButton:initialise()
    self.cancelButton:instantiate()
    self:addChild(self.cancelButton)

    QPSC_TUI_startCatalog()
    self:populateInitialValues()

    self.reputationCombo.enable = self.reputationAvailable
    self.reputationPointsEntry.enable = self.reputationAvailable
    self.secondaryReputationCombo.enable =
        self.reputationAvailable
    self.secondaryReputationPointsEntry.enable =
        self.reputationAvailable

    if not self.reputationAvailable and not self.isEditMode then
        self.reputationCombo.selected = 1
        self.reputationPointsEntry:setText("0")
        self.secondaryReputationCombo.selected = 1
        self.secondaryReputationPointsEntry:setText("0")
    end

    self:layoutControls()
    self:refreshItemList(true)
    self:onObjectiveChanged()
end

function QPSC_TrackedContractWindow:populateInitialValues()
    local contract = self.editContract

    if contract == nil then
        for index, definition in ipairs(QPSC_TUI_CATEGORY_DEFINITIONS) do
            if definition.key == self.categoryKey then
                self.categoryCombo.selected = index
                break
            end
        end
        self.difficultyCombo.selected = 1
        self.completionModeCombo.selected = 1
        return
    end

    self.titleEntry:setText(tostring(contract.title or ""))
    self.locationEntry:setText(tostring(contract.location or ""))
    self.descriptionEntry:setText(tostring(contract.description or ""))
    self.manualRewardEntry:setText(tostring(contract.reward or ""))
    self.targetEntry:setText(tostring(tonumber(contract.objectiveTarget) or 0))
    self.radiusEntry:setText(tostring(tonumber(contract.objectiveRadius) or 0))

    local timeLimit = tonumber(contract.timeLimitHours) or 0
    self.timeEntry:setText(timeLimit > 0 and tostring(timeLimit) or "")
    self.rewardQuantityEntry:setText(tostring(math.max(1, tonumber(contract.rewardQuantity) or 1)))
    self.firstFinisherBonusQuantityEntry:setText(tostring(math.max(1, tonumber(contract.firstFinisherBonusQuantity) or 1)))

    for index, definition in ipairs(QPSC_TUI_CATEGORY_DEFINITIONS) do
        if definition.key == tostring(contract.category or "NONE") then
            self.categoryCombo.selected = index
            break
        end
    end

    local difficulty = QPSC_TUI_normalizeDifficulty(contract.difficulty)
    for index, definition in ipairs(QPSC_TUI_DIFFICULTY_DEFINITIONS) do
        if definition.key == difficulty then
            self.difficultyCombo.selected = index
            break
        end
    end

    local completionMode = QPSC_TUI_normalizeCompletionMode(
        contract.completionMode
    )
    for index, definition in ipairs(
        QPSC_TUI_COMPLETION_MODE_DEFINITIONS
    ) do
        if definition.key == completionMode then
            self.completionModeCombo.selected = index
            break
        end
    end

    local objectiveType = string.upper(tostring(contract.objectiveType or "MANUAL"))
    for index, value in ipairs(self.objectiveTypes) do
        if value == objectiveType then
            self.objectiveCombo.selected = index
            break
        end
    end

    if objectiveType == "KILL" then
        self.killRadiusText = tostring(tonumber(contract.objectiveRadius) or 0)
    elseif objectiveType == "LOCATION" then
        self.locationRadiusText = tostring(tonumber(contract.objectiveRadius) or 3)
    end

    local reputationPath = string.lower(
        tostring(contract.reputationPath or "")
    )
    self.reputationPointsEntry:setText(tostring(
        math.max(
            0,
            math.floor(
                tonumber(contract.reputationPoints) or 0
            )
        )
    ))

    for index, definition in ipairs(
        QPSC_TUI_REPUTATION_DEFINITIONS
    ) do
        if definition.key == reputationPath then
            self.reputationCombo.selected = index
            break
        end
    end

    local secondaryReputationPath = string.lower(
        tostring(contract.secondaryReputationPath or "")
    )
    self.secondaryReputationPointsEntry:setText(
        tostring(
            math.max(
                0,
                math.floor(
                    tonumber(
                        contract.secondaryReputationPoints
                    ) or 0
                )
            )
        )
    )

    for index, definition in ipairs(
        QPSC_TUI_REPUTATION_DEFINITIONS
    ) do
        if definition.key == secondaryReputationPath then
            self.secondaryReputationCombo.selected = index
            break
        end
    end

    -- Prevent the first refresh from replacing an existing custom radius.
    self.lastObjectiveType = objectiveType
end

function QPSC_TrackedContractWindow:layoutControls()
    if self.titleEntry == nil then return end

    local FL = QPSC_FlexibleLayout
    local margin = 20
    local panelLeft = 16
    local panelInnerLeft = 24
    local panelRight = self.width - 16
    local fieldWidth = self.width - (margin * 2)
    local gap = 8
    local panelGap = 7
    local buttonHeight = 28
    local quantityWidth = FL.clamp(
        math.floor(self.width * 0.065),
        78,
        98
    )
    local quantityX = self.width - 24 - quantityWidth
    local panelContentRight = quantityX - 14

    self.titleEntry:setWidth(fieldWidth)
    self.locationEntry:setWidth(fieldWidth)
    self.descriptionEntry:setWidth(fieldWidth)
    self.searchEntry:setWidth(fieldWidth)

    local topColumns = FL.columns(
        margin,
        self.width - margin,
        {0.31, 0.27, 0.42},
        16,
        {150, 140, 190}
    )

    self.categoryCombo:setX(topColumns[1].x)
    self.categoryCombo:setWidth(topColumns[1].width)
    self.difficultyCombo:setX(topColumns[2].x)
    self.difficultyCombo:setWidth(topColumns[2].width)
    self.completionModeCombo:setX(topColumns[3].x)
    self.completionModeCombo:setWidth(topColumns[3].width)

    local objectiveColumns = FL.columns(
        margin,
        self.width - margin,
        {0.31, 0.13, 0.13, 0.43},
        14,
        {180, 88, 88, 160}
    )

    self.objectiveCombo:setX(objectiveColumns[1].x)
    self.objectiveCombo:setWidth(objectiveColumns[1].width)
    self.targetEntry:setX(objectiveColumns[2].x)
    self.targetEntry:setWidth(objectiveColumns[2].width)
    self.radiusEntry:setX(objectiveColumns[3].x)
    self.radiusEntry:setWidth(objectiveColumns[3].width)
    self.timeEntry:setX(objectiveColumns[4].x)
    self.timeEntry:setWidth(objectiveColumns[4].width)

    self.layoutTopLabelX = {
        category=topColumns[1].x,
        difficulty=topColumns[2].x,
        completion=topColumns[3].x,
        objective=objectiveColumns[1].x,
        target=objectiveColumns[2].x,
        radius=objectiveColumns[3].x,
        time=objectiveColumns[4].x
    }

    local function headerPlan(labelText, valueMinimumWidth)
        local labelWidth = FL.measure(labelText, UIFont.Small)
        local valueX = panelInnerLeft + labelWidth + 10
        local valueRight = panelContentRight
        local wrapped = valueX + (valueMinimumWidth or 80) > valueRight

        if wrapped then
            return {
                labelX=panelInnerLeft,
                labelY=8,
                valueX=panelInnerLeft,
                valueY=28,
                headerHeight=48,
                valueWidth=math.max(80, valueRight - panelInnerLeft)
            }
        end

        return {
            labelX=panelInnerLeft,
            labelY=8,
            valueX=valueX,
            valueY=8,
            headerHeight=30,
            valueWidth=math.max(80, valueRight - valueX)
        }
    end

    local objectiveType = self:getObjectiveType()
    local isManual = objectiveType == "MANUAL"
    local objectiveKey = isManual
        and "UI_QPSC_ManualRewardText"
        or objectiveType == "DELIVERY"
            and "UI_QPSC_DeliveryObjectiveItem"
            or "UI_QPSC_ObjectiveItemNotRequired"
    local objectiveLabel = QPSC_I18N.getText(objectiveKey)

    if isManual or objectiveType == "DELIVERY" then
        objectiveLabel = objectiveLabel .. ":"
    end

    local rewardLabel = QPSC_I18N.getText("UI_QPSC_NormalRewards") .. ":"
    local bonusLabel = QPSC_I18N.getText("UI_QPSC_FirstFinisherBonus") .. ":"

    local objectiveHeader = headerPlan(objectiveLabel, 100)
    local rewardHeader = headerPlan(rewardLabel, 100)
    local bonusHeader = headerPlan(bonusLabel, 100)

    local objectiveButtons = {}
    if not isManual then
        objectiveButtons = {self.setObjectiveButton}
    end

    local rewardPlan = FL.planButtons(
        {self.setRewardButton, self.clearRewardButton},
        panelContentRight - panelInnerLeft,
        gap,
        120,
        330
    )
    local objectivePlan = FL.planButtons(
        objectiveButtons,
        panelContentRight - panelInnerLeft,
        gap,
        140,
        360
    )
    local bonusButtons = {
        self.setFirstFinisherBonusButton,
        self.clearFirstFinisherBonusButton
    }
    local bonusPlan = FL.planButtons(
        bonusButtons,
        panelContentRight - panelInnerLeft,
        gap,
        120,
        390
    )

    local objectiveControlHeight = isManual and buttonHeight or objectivePlan.height
    local objectivePanelHeight = objectiveHeader.headerHeight + objectiveControlHeight + 10
    local rewardPanelHeight = rewardHeader.headerHeight + rewardPlan.height + 10
    local bonusPanelHeight = bonusHeader.headerHeight + bonusPlan.height + 10

    local unavailableSuffix = self.reputationAvailable
        and ""
        or (
            " (" .. QPSC_I18N.getText("UI_QPSC_ReputationUnavailable") .. ")"
        )
    local primaryLabel = QPSC_I18N.getText(
        "UI_QPSC_PrimaryReputationReward"
    ) .. unavailableSuffix .. ":"
    local secondaryLabel = QPSC_I18N.getText(
        "UI_QPSC_SecondaryReputationReward"
    ) .. unavailableSuffix .. ":"
    local pointsLabel = QPSC_I18N.getText("UI_QPSC_ReputationPoints")

    local function reputationRow(labelText)
        local labelWidth = FL.measure(labelText, UIFont.Small)
        local pointsWidth = FL.measure(pointsLabel, UIFont.Small)
        local overlap = panelInnerLeft + labelWidth + 14
            > quantityX - pointsWidth

        if overlap then
            return {
                labelY=0,
                pointsY=22,
                controlsY=42,
                height=76
            }
        end

        return {
            labelY=0,
            pointsY=0,
            controlsY=22,
            height=56
        }
    end

    local primaryRow = reputationRow(primaryLabel)
    local secondaryRow = reputationRow(secondaryLabel)
    local reputationPanelHeight = 8
        + primaryRow.height
        + 6
        + secondaryRow.height
        + 8

    local actionY = self.height - 38
    local createWidth = FL.buttonWidth(self.createButton, 220, 430, 34)
    local cancelWidth = FL.buttonWidth(self.cancelButton, 130, 220, 34)
    local maxCreateWidth = math.max(180, self.width - margin * 2 - cancelWidth - 20)
    createWidth = math.min(createWidth, maxCreateWidth)

    self.createButton:setX(margin)
    self.createButton:setY(actionY)
    self.createButton:setWidth(createWidth)
    self.cancelButton:setX(self.width - margin - cancelWidth)
    self.cancelButton:setY(actionY)
    self.cancelButton:setWidth(cancelWidth)

    local bottomPanelsY = actionY - 10
    local bonusPanelY = bottomPanelsY - bonusPanelHeight
    local reputationPanelY = bonusPanelY - panelGap - reputationPanelHeight
    local rewardPanelY = reputationPanelY - panelGap - rewardPanelHeight
    local objectivePanelY = rewardPanelY - panelGap - objectivePanelHeight
    local listY = 406
    local listHeight = math.max(12, objectivePanelY - listY - 10)

    self.itemList:setX(margin)
    self.itemList:setY(listY)
    self.itemList:setWidth(fieldWidth)
    self.itemList:setHeight(listHeight)

    local objectiveControlY = objectivePanelY + objectiveHeader.headerHeight
    if isManual then
        self.manualRewardEntry:setX(panelInnerLeft)
        self.manualRewardEntry:setY(objectiveControlY)
        self.manualRewardEntry:setWidth(panelRight - panelInnerLeft - 8)
    else
        FL.applyButtonPlan(objectivePlan, panelInnerLeft, objectiveControlY)
    end

    local rewardControlY = rewardPanelY + rewardHeader.headerHeight
    FL.applyButtonPlan(rewardPlan, panelInnerLeft, rewardControlY)
    self.rewardQuantityEntry:setX(quantityX)
    self.rewardQuantityEntry:setY(rewardControlY)
    self.rewardQuantityEntry:setWidth(quantityWidth)

    local primaryTop = reputationPanelY + 8
    local secondaryTop = primaryTop + primaryRow.height + 6
    local comboWidth = math.max(180, quantityX - panelInnerLeft - 14)

    self.reputationCombo:setX(panelInnerLeft)
    self.reputationCombo:setY(primaryTop + primaryRow.controlsY)
    self.reputationCombo:setWidth(comboWidth)
    self.reputationPointsEntry:setX(quantityX)
    self.reputationPointsEntry:setY(primaryTop + primaryRow.controlsY)
    self.reputationPointsEntry:setWidth(quantityWidth)

    self.secondaryReputationCombo:setX(panelInnerLeft)
    self.secondaryReputationCombo:setY(secondaryTop + secondaryRow.controlsY)
    self.secondaryReputationCombo:setWidth(comboWidth)
    self.secondaryReputationPointsEntry:setX(quantityX)
    self.secondaryReputationPointsEntry:setY(secondaryTop + secondaryRow.controlsY)
    self.secondaryReputationPointsEntry:setWidth(quantityWidth)

    local bonusControlY = bonusPanelY + bonusHeader.headerHeight
    FL.applyButtonPlan(bonusPlan, panelInnerLeft, bonusControlY)
    self.firstFinisherBonusQuantityEntry:setX(quantityX)
    self.firstFinisherBonusQuantityEntry:setY(bonusControlY)
    self.firstFinisherBonusQuantityEntry:setWidth(quantityWidth)

    self.flexLayout = {
        panelLeft=panelLeft,
        panelRight=panelRight,
        panelInnerLeft=panelInnerLeft,
        quantityX=quantityX,
        quantityWidth=quantityWidth,
        objectiveY=objectivePanelY,
        objectiveHeight=objectivePanelHeight,
        objectiveHeader=objectiveHeader,
        rewardY=rewardPanelY,
        rewardHeight=rewardPanelHeight,
        rewardHeader=rewardHeader,
        reputationY=reputationPanelY,
        reputationHeight=reputationPanelHeight,
        primaryTop=primaryTop,
        primaryRow=primaryRow,
        primaryLabel=primaryLabel,
        secondaryTop=secondaryTop,
        secondaryRow=secondaryRow,
        secondaryLabel=secondaryLabel,
        pointsLabel=pointsLabel,
        bonusY=bonusPanelY,
        bonusHeight=bonusPanelHeight,
        bonusHeader=bonusHeader
    }

    self.lastLayoutWidth = self.width
    self.lastLayoutHeight = self.height
end

function QPSC_TrackedContractWindow:resizeBy(dx, dy)
    local screenWidth = getCore():getScreenWidth()
    local screenHeight = getCore():getScreenHeight()
    local currentX = tonumber(self:getX()) or 0
    local currentY = tonumber(self:getY()) or 0
    local minWidth = math.min(self.minWindowWidth, math.max(700, screenWidth - 20))
    local minHeight = math.min(self.minWindowHeight, math.max(620, screenHeight - 20))
    local maxWidth = math.max(minWidth, screenWidth - currentX - 10)
    local maxHeight = math.max(minHeight, screenHeight - currentY - 10)

    self:setWidth(math.max(minWidth, math.min(maxWidth, self.width + (tonumber(dx) or 0))))
    self:setHeight(math.max(minHeight, math.min(maxHeight, self.height + (tonumber(dy) or 0))))
    self:layoutControls()
end

function QPSC_TrackedContractWindow:getObjectiveType()
    local index = tonumber(self.objectiveCombo.selected) or 1
    return self.objectiveTypes[index] or (self.isEditMode and "MANUAL" or "DELIVERY")
end

function QPSC_TrackedContractWindow:getCategoryKey()
    local index = tonumber(self.categoryCombo.selected) or 1
    local definition = QPSC_TUI_CATEGORY_DEFINITIONS[index]
    return definition and definition.key or "NONE"
end

function QPSC_TrackedContractWindow:getDifficultyKey()
    local index = tonumber(self.difficultyCombo.selected) or 1
    local definition = QPSC_TUI_DIFFICULTY_DEFINITIONS[index]
    return definition and definition.key or "UNRATED"
end

function QPSC_TrackedContractWindow:getCompletionModeKey()
    local index = tonumber(self.completionModeCombo.selected) or 1
    local definition = QPSC_TUI_COMPLETION_MODE_DEFINITIONS[index]
    return definition and definition.key or "INDIVIDUAL"
end

function QPSC_TrackedContractWindow:getReputationDefinition()
    local index = tonumber(self.reputationCombo.selected) or 1
    return QPSC_TUI_REPUTATION_DEFINITIONS[index]
        or QPSC_TUI_REPUTATION_DEFINITIONS[1]
end

function QPSC_TrackedContractWindow:getSecondaryReputationDefinition()
    local index = tonumber(
        self.secondaryReputationCombo.selected
    ) or 1

    return QPSC_TUI_REPUTATION_DEFINITIONS[index]
        or QPSC_TUI_REPUTATION_DEFINITIONS[1]
end

function QPSC_TrackedContractWindow:onCompletionModeChanged()
    local objectiveType = self:getObjectiveType()
    local completionMode = self:getCompletionModeKey()

    if completionMode == "SHARED_TEAM"
        and objectiveType ~= "KILL" then
        self.completionModeCombo.selected =
            objectiveType == "LOCATION" and 2 or 1

        if self.player ~= nil then
            self.player:Say(
                QPSC_I18N.getText(
                    "UI_QPSC_MessageSharedTeamKillOnly"
                )
            )
        end
    end

    self:onObjectiveChanged()
end

function QPSC_TrackedContractWindow:onObjectiveChanged()
    local objectiveType = self:getObjectiveType()
    local isManual = objectiveType == "MANUAL"
    local isDelivery = objectiveType == "DELIVERY"
    local isLocation = objectiveType == "LOCATION"
    local completionMode = self:getCompletionModeKey()

    if completionMode == "SHARED_TEAM"
        and objectiveType ~= "KILL" then
        self.completionModeCombo.selected =
            objectiveType == "LOCATION" and 2 or 1
        completionMode = self:getCompletionModeKey()
    end

    local isSharedTeam = completionMode == "SHARED_TEAM"

    self.objectiveCombo.enable = not self.objectiveTypeLocked
    self.completionModeCombo.enable = not self.completionModeLocked
    self.setObjectiveButton.enable = isDelivery and not self.objectiveItemLocked
    self.targetEntry.enable = not isManual and not isLocation and not self.objectiveSettingsLocked
    self.radiusEntry.enable = not isManual and not isDelivery and not self.objectiveSettingsLocked
    self.setFirstFinisherBonusButton.enable =
        not isSharedTeam
    self.clearFirstFinisherBonusButton.enable =
        not isSharedTeam
    self.firstFinisherBonusQuantityEntry.enable =
        not isSharedTeam

    if isSharedTeam then
        self.selectedFirstFinisherBonusItem = nil
        self.firstFinisherBonusQuantityEntry:setText("1")
        local sharedBonusHint = QPSC_I18N.getText(
            "UI_QPSC_SharedTeamNoFirstBonus"
        )
        self.setFirstFinisherBonusButton.tooltip = sharedBonusHint
        self.clearFirstFinisherBonusButton.tooltip = sharedBonusHint
        self.firstFinisherBonusQuantityEntry.tooltip = sharedBonusHint
    else
        self.setFirstFinisherBonusButton.tooltip = nil
        self.clearFirstFinisherBonusButton.tooltip = nil
        self.firstFinisherBonusQuantityEntry.tooltip = nil
    end

    QPSC_TUI_setVisible(self.setObjectiveButton, isDelivery)
    QPSC_TUI_setVisible(self.manualRewardEntry, isManual)
    QPSC_TUI_setVisible(self.setFirstFinisherBonusButton, true)
    QPSC_TUI_setVisible(self.clearFirstFinisherBonusButton, true)
    QPSC_TUI_setVisible(self.firstFinisherBonusQuantityEntry, true)

    if self.lastObjectiveType ~= objectiveType then
        local currentText = tostring(self.radiusEntry:getText() or "")

        if self.lastObjectiveType == "KILL" then
            self.killRadiusText = currentText
        elseif self.lastObjectiveType == "LOCATION" then
            self.locationRadiusText = currentText
        end

        if not self.isEditMode and not self.completionModeLocked then
            self.completionModeCombo.selected =
                (objectiveType == "KILL" or objectiveType == "LOCATION")
                and 2 or 1
        end

        if objectiveType == "KILL" then
            self.radiusEntry:setText(tostring(self.killRadiusText or "100"))
        elseif objectiveType == "LOCATION" then
            self.radiusEntry:setText(tostring(self.locationRadiusText or "3"))
            self.targetEntry:setText("1")
        elseif objectiveType == "MANUAL" then
            self.radiusEntry:setText("")
            self.targetEntry:setText("0")
        else
            self.radiusEntry:setText("")
        end

        self.lastObjectiveType = objectiveType
    end

    self:layoutControls()
end

function QPSC_TrackedContractWindow:getSelectedCatalogItem()
    local selected = tonumber(self.itemList.selected) or 0

    if selected < 1
        or self.itemList.items == nil
        or selected > #self.itemList.items then
        return nil
    end

    local row = self.itemList.items[selected]
    return row and row.item or nil
end

function QPSC_TrackedContractWindow:onSetObjectiveItem()
    if self:getObjectiveType() ~= "DELIVERY"
        or self.objectiveItemLocked then
        return
    end

    local item = self:getSelectedCatalogItem()
    if item ~= nil then self.selectedObjectiveItem = item end
end

function QPSC_TrackedContractWindow:onSetRewardItem()
    local item = self:getSelectedCatalogItem()

    if item == nil then
        return
    end

    local quantity = tonumber(
        self.rewardQuantityEntry:getText()
    ) or 0

    local ok, result = QPSC_TUI_addRewardItem(
        self.rewardItems,
        item,
        quantity
    )

    if not ok then
        if result == "limit" then
            self.player:Say(
                QPSC_I18N.getText(
                    "UI_QPSC_MessageRewardLimit"
                )
            )
        else
            self.player:Say(
                QPSC_I18N.getText(
                    "UI_QPSC_MessageInvalidReward"
                )
            )
        end
        return
    end

    self.selectedRewardItem =
        self.rewardItems[1]
        and QPSC_TUI_itemFromContract(
            self.rewardItems[1].fullType,
            self.rewardItems[1].displayName
        )
        or nil

    self.player:Say(
        QPSC_I18N.getText(
            "UI_QPSC_MessageRewardAdded",
            quantity,
            tostring(item.displayName or item.fullType),
            #self.rewardItems,
            QPSC_TUI_MAX_REWARD_ITEMS
        )
    )
end

function QPSC_TrackedContractWindow:onClearReward()
    self.rewardItems = {}
    self.selectedRewardItem = nil
end

-- QPSC_V122_MANUAL_FIRST_FINISHER_UI_FIX_V1
function QPSC_TrackedContractWindow:onSetFirstFinisherBonusItem()
    local item = self:getSelectedCatalogItem()
    if item ~= nil then
        self.selectedFirstFinisherBonusItem = item
    end
end

function QPSC_TrackedContractWindow:onClearFirstFinisherBonus()
    self.selectedFirstFinisherBonusItem = nil
end

function QPSC_TrackedContractWindow:clearResults()
    if self.itemList.clear ~= nil then
        self.itemList:clear()
    else
        self.itemList.items = {}
    end

    self.itemList.selected = 0
    self.matchCount = 0
end

function QPSC_TrackedContractWindow:refreshItemList(force)
    local searchText = QPSC_TUI_trim(self.searchEntry:getText())

    if not force and searchText ~= self.observedSearchText then
        self.observedSearchText = searchText
        self.searchDelayFrames = 8
        self.lastSearchText = nil
        self:clearResults()
        return
    end

    if not force and self.searchDelayFrames > 0 then
        self.searchDelayFrames = self.searchDelayFrames - 1
        return
    end

    if not force and searchText == self.lastSearchText then return end

    self.lastSearchText = searchText
    self:clearResults()

    if string.len(searchText) < 2 then return end
    if QPSC_TrackedUI.itemCatalog == nil then
        self.lastSearchText = nil
        return
    end

    local searchLower = string.lower(searchText)
    local matches = {}

    for _, entry in ipairs(QPSC_TrackedUI.itemCatalog) do
        local rank = QPSC_TUI_itemRank(entry, searchLower)

        if rank ~= nil then
            table.insert(matches, {rank=rank, item=entry})
        end
    end

    table.sort(matches, function(left, right)
        if left.rank ~= right.rank then return left.rank < right.rank end
        return string.lower(left.item.displayName)
            < string.lower(right.item.displayName)
    end)

    local maximum = math.min(#matches, 120)

    for index = 1, maximum do
        local entry = matches[index].item
        self.itemList:addItem(
            QPSC_I18N.getText(
                "UI_QPSC_ItemListEntry",
                entry.displayName,
                entry.fullType
            ),
            entry
        )
    end

    self.matchCount = maximum
end

function QPSC_TrackedContractWindow:update()
    ISPanel.update(self)

    if self.lastLayoutWidth ~= self.width
        or self.lastLayoutHeight ~= self.height then
        self:layoutControls()
    end

    if QPSC_TrackedUI.itemCatalog == nil then
        QPSC_TUI_stepCatalog(250)
    end

    self:refreshItemList(false)
    self:onObjectiveChanged()
end

function QPSC_TrackedContractWindow:onCreate()
    local objectiveType = self:getObjectiveType()
    local completionMode = self:getCompletionModeKey()

    if completionMode == "SHARED_TEAM"
        and objectiveType ~= "KILL" then
        self.player:Say(
            QPSC_I18N.getText(
                "UI_QPSC_MessageSharedTeamKillOnly"
            )
        )
        return
    end
    local title = QPSC_TUI_limit(QPSC_TUI_trim(self.titleEntry:getText()), 120)
    local location = QPSC_TUI_limit(QPSC_TUI_trim(self.locationEntry:getText()), 160)
    local description = QPSC_TUI_limit(QPSC_TUI_trim(self.descriptionEntry:getText()), 300)
    local target = tonumber(self.targetEntry:getText()) or 0
    local radius = tonumber(self.radiusEntry:getText()) or 0
    local timeLimitText = QPSC_TUI_trim(self.timeEntry:getText())
    local timeLimitHours = tonumber(timeLimitText) or 0

    if title == "" then
        self.player:Say(QPSC_I18N.getText("UI_QPSC_MessageTitleRequired"))
        return
    end

    if location == "" then location = QPSC_I18N.getText("UI_QPSC_CurrentLocation") end
    if description == "" then description = QPSC_I18N.getText("UI_QPSC_NoDescription") end

    if timeLimitText ~= "" and timeLimitHours <= 0 then
        self.player:Say(QPSC_I18N.getText("UI_QPSC_MessageInvalidTimeLimit"))
        return
    end

    local objectiveItem = self.selectedObjectiveItem

    if objectiveType == "MANUAL" then
        target = 0
        radius = 0
        objectiveItem = nil
    elseif objectiveType == "DELIVERY" then
        radius = 0

        if target < 1 or target > 10000 then
            self.player:Say(QPSC_I18N.getText("UI_QPSC_MessageInvalidObjective"))
            return
        end

        if objectiveItem == nil then
            self.player:Say(QPSC_I18N.getText("UI_QPSC_MessageSelectObjectiveItem"))
            return
        end
    elseif objectiveType == "KILL" then
        if target < 1 or target > 10000 then
            self.player:Say(QPSC_I18N.getText("UI_QPSC_MessageInvalidObjective"))
            return
        end

        local legacyUnrestrictedHunt =
            self.isEditMode
            and string.upper(tostring(self.editContract.objectiveType or "")) == "KILL"
            and (tonumber(self.editContract.objectiveRadius) or 0) <= 0
            and radius <= 0

        if not legacyUnrestrictedHunt
            and (radius < 1 or radius > 1000) then
            self.player:Say(QPSC_I18N.getText("UI_QPSC_MessageInvalidKillRadius"))
            return
        end
    else
        target = 1

        if radius < 1 or radius > 20 then
            self.player:Say(QPSC_I18N.getText("UI_QPSC_MessageInvalidRadius"))
            return
        end
    end

    local rewardItems = self.rewardItems or {}

    if #rewardItems > QPSC_TUI_MAX_REWARD_ITEMS then
        self.player:Say(
            QPSC_I18N.getText(
                "UI_QPSC_MessageRewardLimit"
            )
        )
        return
    end

    for _, reward in ipairs(rewardItems) do
        local quantity = math.floor(
            tonumber(reward.quantity) or 0
        )

        if tostring(reward.fullType or "") == ""
            or quantity < 1
            or quantity > 100 then
            self.player:Say(
                QPSC_I18N.getText(
                    "UI_QPSC_MessageInvalidReward"
                )
            )
            return
        end
    end

    local rewardText = self.legacyRewardText

    if objectiveType == "MANUAL" then
        rewardText = QPSC_TUI_limit(
            QPSC_TUI_trim(self.manualRewardEntry:getText()),
            160
        )
        if rewardText == "" then
            rewardText = "Manual reward"
        end
    elseif rewardText == "" then
        rewardText =
            QPSC_I18N.getText("UI_QPSC_None")
    end

    if #rewardItems > 0 then
        rewardText =
            QPSC_TUI_rewardSummary(rewardItems)
    elseif objectiveType ~= "MANUAL" then
        rewardText =
            QPSC_I18N.getText("UI_QPSC_None")
    end

    local firstFinisherBonusItem = self.selectedFirstFinisherBonusItem
    local firstFinisherBonusQuantity = 0

    if completionMode == "SHARED_TEAM" then
        firstFinisherBonusItem = nil
    elseif firstFinisherBonusItem ~= nil then
        firstFinisherBonusQuantity = tonumber(
            self.firstFinisherBonusQuantityEntry:getText()
        ) or 0

        if firstFinisherBonusQuantity < 1
            or firstFinisherBonusQuantity > 100 then
            self.player:Say(QPSC_I18N.getText("UI_QPSC_MessageInvalidFirstFinisherBonus"))
            return
        end
    end

    local reputationDefinition = self:getReputationDefinition()
    local reputationPoints = math.floor(
        tonumber(self.reputationPointsEntry:getText()) or 0
    )
    local secondaryReputationDefinition =
        self:getSecondaryReputationDefinition()
    local secondaryReputationPoints = math.floor(
        tonumber(
            self.secondaryReputationPointsEntry:getText()
        ) or 0
    )

    if not self.reputationAvailable and not self.isEditMode then
        reputationDefinition =
            QPSC_TUI_REPUTATION_DEFINITIONS[1]
        reputationPoints = 0
        secondaryReputationDefinition =
            QPSC_TUI_REPUTATION_DEFINITIONS[1]
        secondaryReputationPoints = 0
    end

    if reputationDefinition.key == "" then
        reputationPoints = 0
    elseif reputationPoints < 1 or reputationPoints > 100000 then
        self.player:Say(
            QPSC_I18N.getText(
                "UI_QPSC_MessageInvalidReputationPoints"
            )
        )
        return
    end

    if secondaryReputationDefinition.key == "" then
        secondaryReputationPoints = 0
    elseif secondaryReputationPoints < 1
        or secondaryReputationPoints > 100000 then
        self.player:Say(
            QPSC_I18N.getText(
                "UI_QPSC_MessageInvalidReputationPoints"
            )
        )
        return
    end

    if reputationDefinition.key ~= ""
        and secondaryReputationDefinition.key ~= ""
        and reputationDefinition.key
            == secondaryReputationDefinition.key then
        self.player:Say(
            QPSC_I18N.getText(
                "UI_QPSC_MessageReputationPathsDiffer"
            )
        )
        return
    end

    local args = {
        contractId = self.editContract and self.editContract.id or nil,
        title = title,
        category = self:getCategoryKey(),
        difficulty = self:getDifficultyKey(),
        completionMode = completionMode,
        location = location,
        description = description,
        reward = rewardText,
        timeLimitText = timeLimitText,
        timeLimitHours = timeLimitHours,
        objectiveType = objectiveType,
        objectiveTarget = target,
        objectiveRadius = radius,
        objectiveItemFullType = objectiveItem and objectiveItem.fullType or "",
        objectiveItemDisplayName = objectiveItem and objectiveItem.displayName or "",
        reputationPath = reputationDefinition.key,
        reputationPoints = reputationPoints,
        secondaryReputationPath =
            secondaryReputationDefinition.key,
        secondaryReputationPoints =
            secondaryReputationPoints,
        firstFinisherBonusItemFullType = firstFinisherBonusItem and firstFinisherBonusItem.fullType or "",
        firstFinisherBonusItemDisplayName = firstFinisherBonusItem and firstFinisherBonusItem.displayName or "",
        firstFinisherBonusQuantity = firstFinisherBonusQuantity
    }

    QPSC_TUI_applyRewardArgs(
        args,
        rewardItems
    )

    if self.isEditMode then
        QPSC_Client.updateContract(self.player, args)
    else
        QPSC_Client.createTrackedContract(self.player, args)
    end

    self:onCancel()
end

function QPSC_TrackedContractWindow:onMouseDown(x, y)
    local gripSize = tonumber(self.resizeGripSize) or QPSC_TUI_RESIZE_GRIP

    if x >= self.width - gripSize
        and y >= self.height - gripSize then
        self.resizing = true
        if self.setCapture then self:setCapture(true) end
        return true
    end

    return ISPanel.onMouseDown(self, x, y)
end

function QPSC_TrackedContractWindow:onMouseMove(dx, dy)
    if self.resizing then
        self:resizeBy(dx, dy)
        return true
    end

    return ISPanel.onMouseMove(self, dx, dy)
end

function QPSC_TrackedContractWindow:onMouseMoveOutside(dx, dy)
    if self.resizing then
        self:resizeBy(dx, dy)
        return true
    end

    if ISPanel.onMouseMoveOutside then
        return ISPanel.onMouseMoveOutside(self, dx, dy)
    end
end

function QPSC_TrackedContractWindow:onMouseUp(x, y)
    if self.resizing then
        self.resizing = false
        if self.setCapture then self:setCapture(false) end
        return true
    end

    return ISPanel.onMouseUp(self, x, y)
end

function QPSC_TrackedContractWindow:onMouseUpOutside(x, y)
    if self.resizing then
        self.resizing = false
        if self.setCapture then self:setCapture(false) end
        return true
    end

    if ISPanel.onMouseUpOutside then
        return ISPanel.onMouseUpOutside(self, x, y)
    end
end

function QPSC_TrackedContractWindow:onCancel()
    self:removeFromUIManager()

    if QPSC_TrackedUI.window == self then
        QPSC_TrackedUI.window = nil
    end
end

function QPSC_TrackedContractWindow:prerender()
    ISPanel.prerender(self)

    if self.lastLayoutWidth ~= self.width
        or self.lastLayoutHeight ~= self.height then
        self:layoutControls()
    end

    local layout = self.flexLayout or {}
    local objectivePanelY = tonumber(layout.objectiveY)
        or (self.height - 292)
    local objectivePanelHeight = tonumber(layout.objectiveHeight) or 66
    local rewardPanelY = tonumber(layout.rewardY)
        or (self.height - 276)
    local rewardPanelHeight = tonumber(layout.rewardHeight) or 66
    local reputationPanelY = tonumber(layout.reputationY)
        or (self.height - 204)
    local reputationPanelHeight = tonumber(layout.reputationHeight) or 126
    local bonusPanelY = tonumber(layout.bonusY)
        or (self.height - 132)
    local bonusPanelHeight = tonumber(layout.bonusHeight) or 66
    local quantityX = tonumber(layout.quantityX)
        or (self.width - 114)

    self:drawRect(
        0, 0, self.width, self.height,
        0.96, 0.025, 0.025, 0.025
    )
    self:drawRectBorder(
        0, 0, self.width, self.height,
        0.95, 0.80, 0.66, 0.24
    )

    self:drawText(
        self.isEditMode
            and QPSC_I18N.getText("UI_QPSC_EditContractTitle")
            or (
                self.forceManual
                and QPSC_I18N.getText("UI_QPSC_CreateCustomContract")
                or QPSC_I18N.getText("UI_QPSC_CreateTrackedTitle")
            ),
        20, 14, 1, 1, 1, 1, UIFont.Medium
    )

    local topLabelX = self.layoutTopLabelX or {}
    local labels = {
        {"UI_QPSC_Title", 48, 20},
        {"UI_QPSC_Location", 108, 20},
        {"UI_QPSC_Description", 168, 20},
        {"UI_QPSC_Category", 228, topLabelX.category or 20},
        {"UI_QPSC_Difficulty", 228, topLabelX.difficulty or 270},
        {"UI_QPSC_CompletionMode", 228, topLabelX.completion or 480},
        {"UI_QPSC_ObjectiveType", 288, topLabelX.objective or 20},
        {"UI_QPSC_Target", 288, topLabelX.target or 275},
        {"UI_QPSC_Radius", 288, topLabelX.radius or 410},
        {"UI_QPSC_TimeLimit", 288, topLabelX.time or 535},
        {"UI_QPSC_SearchItems", 348, 20}
    }

    for _, label in ipairs(labels) do
        self:drawText(
            QPSC_I18N.getText(label[1]),
            label[3], label[2],
            0.82, 0.82, 0.82, 1, UIFont.Small
        )
    end

    local function compactItem(item, maximumWidth)
        if item == nil then
            return QPSC_I18N.getText("UI_QPSC_None")
        end

        return QPSC_FlexibleLayout.ellipsize(
            tostring(item.displayName or ""),
            UIFont.Small,
            math.max(80, tonumber(maximumWidth) or 240)
        )
    end

    self:drawRect(
        16, objectivePanelY, self.width - 32, objectivePanelHeight,
        0.22, 0.08, 0.12, 0.16
    )
    self:drawRectBorder(
        16, objectivePanelY, self.width - 32, objectivePanelHeight,
        0.55, 0.30, 0.55, 0.75
    )

    local currentObjectiveType = self:getObjectiveType()
    local objectivePanelKey = currentObjectiveType == "MANUAL"
        and "UI_QPSC_ManualRewardText"
        or currentObjectiveType == "DELIVERY"
            and "UI_QPSC_DeliveryObjectiveItem"
            or "UI_QPSC_ObjectiveItemNotRequired"
    local objectivePanelLabel = QPSC_I18N.getText(objectivePanelKey)

    if currentObjectiveType == "MANUAL"
        or currentObjectiveType == "DELIVERY" then
        objectivePanelLabel = objectivePanelLabel .. ":"
    end

    local objectiveHeader = layout.objectiveHeader or {
        labelX=24,labelY=8,valueX=300,valueY=8,valueWidth=240
    }
    self:drawText(
        objectivePanelLabel,
        objectiveHeader.labelX,
        objectivePanelY + objectiveHeader.labelY,
        0.75, 0.90, 1, 1, UIFont.Small
    )

    if currentObjectiveType == "DELIVERY" then
        self:drawText(
            compactItem(
                self.selectedObjectiveItem,
                objectiveHeader.valueWidth
            ),
            objectiveHeader.valueX,
            objectivePanelY + objectiveHeader.valueY,
            0.90, 0.90, 0.90, 1, UIFont.Small
        )
    end

    self:drawRect(
        16, rewardPanelY, self.width - 32, rewardPanelHeight,
        0.22, 0.08, 0.16, 0.08
    )
    self:drawRectBorder(
        16, rewardPanelY, self.width - 32, rewardPanelHeight,
        0.55, 0.30, 0.75, 0.30
    )
    local rewardHeader = layout.rewardHeader or {
        labelX=24,labelY=8,valueX=300,valueY=8,valueWidth=240
    }
    self:drawText(
        QPSC_I18N.getText("UI_QPSC_NormalRewards")
            .. " ("
            .. tostring(#(self.rewardItems or {}))
            .. "/"
            .. tostring(QPSC_TUI_MAX_REWARD_ITEMS)
            .. "):",
        rewardHeader.labelX,
        rewardPanelY + rewardHeader.labelY,
        0.72, 1, 0.72, 1, UIFont.Small
    )
    self:drawText(
        QPSC_FlexibleLayout.ellipsize(
            QPSC_TUI_rewardSummary(
                self.rewardItems
            ),
            UIFont.Small,
            rewardHeader.valueWidth
        ),
        rewardHeader.valueX,
        rewardPanelY + rewardHeader.valueY,
        0.90, 0.90, 0.90, 1, UIFont.Small
    )
    self:drawText(
        QPSC_I18N.getText("UI_QPSC_Quantity"),
        quantityX,
        rewardPanelY + rewardHeader.labelY,
        0.88, 0.88, 0.88, 1, UIFont.Small
    )

    self:drawRect(
        16,
        reputationPanelY,
        self.width - 32,
        reputationPanelHeight,
        0.22,
        0.06,
        0.10,
        0.17
    )
    self:drawRectBorder(
        16,
        reputationPanelY,
        self.width - 32,
        reputationPanelHeight,
        0.60,
        0.34,
        0.58,
        0.90
    )

    local primaryRow = layout.primaryRow or {labelY=0,pointsY=0}
    local secondaryRow = layout.secondaryRow or {labelY=0,pointsY=0}
    local primaryTop = tonumber(layout.primaryTop) or (reputationPanelY + 8)
    local secondaryTop = tonumber(layout.secondaryTop) or (reputationPanelY + 64)

    self:drawText(
        layout.primaryLabel or QPSC_I18N.getText(
            "UI_QPSC_PrimaryReputationReward"
        ) .. ":",
        24,
        primaryTop + primaryRow.labelY,
        0.72, 0.78, 1, 1, UIFont.Small
    )
    self:drawText(
        layout.pointsLabel or QPSC_I18N.getText(
            "UI_QPSC_ReputationPoints"
        ),
        quantityX,
        primaryTop + primaryRow.pointsY,
        0.88, 0.88, 0.88, 1, UIFont.Small
    )
    self:drawText(
        layout.secondaryLabel or QPSC_I18N.getText(
            "UI_QPSC_SecondaryReputationReward"
        ) .. ":",
        24,
        secondaryTop + secondaryRow.labelY,
        0.72, 0.78, 1, 1, UIFont.Small
    )
    self:drawText(
        layout.pointsLabel or QPSC_I18N.getText(
            "UI_QPSC_ReputationPoints"
        ),
        quantityX,
        secondaryTop + secondaryRow.pointsY,
        0.88, 0.88, 0.88, 1, UIFont.Small
    )

    self:drawRect(
        16, bonusPanelY, self.width - 32, bonusPanelHeight,
        0.22, 0.18, 0.12, 0.04
    )
    self:drawRectBorder(
        16, bonusPanelY, self.width - 32, bonusPanelHeight,
        0.65, 0.95, 0.68, 0.20
    )
    local bonusHeader = layout.bonusHeader or {
        labelX=24,labelY=8,valueX=300,valueY=8,valueWidth=240
    }
    self:drawText(
        QPSC_I18N.getText("UI_QPSC_FirstFinisherBonus") .. ":",
        bonusHeader.labelX,
        bonusPanelY + bonusHeader.labelY,
        1, 0.82, 0.38, 1, UIFont.Small
    )
    self:drawText(
        compactItem(
            self.selectedFirstFinisherBonusItem,
            bonusHeader.valueWidth
        ),
        bonusHeader.valueX,
        bonusPanelY + bonusHeader.valueY,
        0.90, 0.90, 0.90, 1, UIFont.Small
    )
    self:drawText(
        QPSC_I18N.getText("UI_QPSC_Quantity"),
        quantityX,
        bonusPanelY + bonusHeader.labelY,
        0.88, 0.88, 0.88, 1, UIFont.Small
    )

    if self.isEditMode
        and (self.objectiveTypeLocked or self.objectiveSettingsLocked) then
        self:drawText(
            QPSC_I18N.getText("UI_QPSC_EditLockedHint"),
            24, objectivePanelY + objectivePanelHeight - 20,
            0.90, 0.72, 0.34, 1, UIFont.Small
        )
    end

    local itemListY = tonumber(self.itemList:getY()) or 406

    if QPSC_TrackedUI.itemCatalog == nil then
        self:drawText(
            QPSC_I18N.getText("UI_QPSC_LoadingItems"),
            24, itemListY + 8,
            0.90, 0.80, 0.45, 1, UIFont.Small
        )
    elseif string.len(
        QPSC_TUI_trim(self.searchEntry:getText())
    ) < 2 then
        self:drawText(
            QPSC_I18N.getText("UI_QPSC_SearchHint"),
            24, itemListY + 8,
            0.70, 0.70, 0.70, 1, UIFont.Small
        )
    end

    local gripRight = self.width - 8
    local gripBottom = self.height - 8

    for index = 0, 2 do
        local length = 6 + (index * 5)
        self:drawRect(
            gripRight - length,
            gripBottom - (index * 5),
            length,
            2,
            0.90,
            0.72,
            0.72,
            0.72
        )
    end
end

local function QPSC_TUI_openWindow(player, categoryKey, editContract, forceManual)
    if QPSC_TrackedUI.window ~= nil then
        QPSC_TrackedUI.window:removeFromUIManager()
        QPSC_TrackedUI.window = nil
    end

    local screenWidth = getCore():getScreenWidth()
    local screenHeight = getCore():getScreenHeight()
    local width = math.min(1040, math.max(QPSC_TUI_MIN_WIDTH, screenWidth - 80))
    local height = math.min(860, math.max(QPSC_TUI_MIN_HEIGHT, screenHeight - 80))
    width = math.min(width, screenWidth - 10)
    height = math.min(height, screenHeight - 10)

    local x = (screenWidth - width) / 2
    local y = (screenHeight - height) / 2

    local window = QPSC_TrackedContractWindow:new(
        x, y, width, height, player, categoryKey, editContract, forceManual
    )
    window:initialise()
    window:addToUIManager()
    QPSC_TrackedUI.window = window
end

function QPSC_TrackedUI.open(player, categoryKey)
    QPSC_TUI_openWindow(player, categoryKey, nil)
end

function QPSC_TrackedUI.openEdit(player, contract)
    if contract == nil then return end
    QPSC_TUI_openWindow(
        player,
        tostring(contract.category or "NONE"),
        contract,
        false
    )
end

function QPSC_TrackedUI.openCustom(player, categoryKey)
    QPSC_TUI_openWindow(
        player,
        tostring(categoryKey or "NONE"),
        nil,
        true
    )
end

return QPSC_TrackedUI
