-- QP Survivor Contracts
-- v1.1.0 Reputation Integration TC + resizable multi-objective editor

require "ISUI/ISPanel"
require "ISUI/ISButton"
require "ISUI/ISTextEntryBox"
require "ISUI/ISScrollingListBox"
require "ISUI/ISComboBox"
require "QPSurvivorContracts/QPSC_I18N"
require "QPSurvivorContracts/QPSC_FlexibleLayout"

QPSC_MultiObjectiveUI = QPSC_MultiObjectiveUI or {}
QPSC_MultiObjectiveUI.window = QPSC_MultiObjectiveUI.window or nil
QPSC_MultiObjectiveUI.detailsWindow = QPSC_MultiObjectiveUI.detailsWindow or nil
QPSC_MultiObjectiveUI.itemCatalog = QPSC_MultiObjectiveUI.itemCatalog or nil
QPSC_MultiObjectiveUI.catalogBuild = QPSC_MultiObjectiveUI.catalogBuild or nil

local MAX_OBJECTIVES = 5
local MIN_WIDTH = 960
local MIN_HEIGHT = 820
local RESIZE_GRIP = 30

local CATEGORY_DEFS = {
    {key="NONE", label="UI_QPSC_CategoryNone"},
    {key="FUEL", label="UI_QPSC_CategoryFuel"},
    {key="FOOD", label="UI_QPSC_CategoryFood"},
    {key="MECHANIC", label="UI_QPSC_CategoryMechanic"},
    {key="MEDICAL", label="UI_QPSC_CategoryMedical"},
    {key="CONSTRUCTION", label="UI_QPSC_CategoryConstruction"},
    {key="DELIVERY", label="UI_QPSC_CategoryDelivery"},
    {key="DANGER", label="UI_QPSC_CategoryDanger"}
}

local DIFFICULTY_DEFS = {
    {key="UNRATED", label="UI_QPSC_DifficultyUnrated"},
    {key="EASY", label="UI_QPSC_DifficultyEasy"},
    {key="MEDIUM", label="UI_QPSC_DifficultyMedium"},
    {key="HARD", label="UI_QPSC_DifficultyHard"}
}

local MODE_DEFS = {
    {key="INDIVIDUAL", label="UI_QPSC_CompletionModeIndividual"},
    {key="GLOBAL", label="UI_QPSC_CompletionModeGlobal"},
    {key="SHARED_TEAM", label="UI_QPSC_CompletionModeSharedTeam"}
}


local REPUTATION_DEFS = {
    {key="", labelKey="UI_QPSC_None", label="None"},
    {key="community", label="Community"},
    {key="hunter", label="Hunter"},
    {key="explorer", label="Explorer"},
    {key="medic", label="Medic"},
    {key="mechanic", label="Mechanic"},
    {key="builder", label="Builder"}
}

local function reputationLabel(definition)
    if definition == nil then return "" end
    if definition.labelKey ~= nil and definition.labelKey ~= "" then
        return QPSC_I18N.getText(definition.labelKey)
    end
    return tostring(definition.label or "")
end

local function QPSC_MO_reputationAvailable()
    return QPReputation ~= nil
        and QPReputation.Client ~= nil
end

local OBJECTIVE_DEFS = {
    {key="DELIVERY", label="UI_QPSC_ObjectiveDelivery"},
    {key="KILL", label="UI_QPSC_ObjectiveKill"},
    {key="LOCATION", label="UI_QPSC_ObjectiveLocation"}
}

local function trim(value)
    return tostring(value or ""):gsub("^%s+", ""):gsub("%s+$", "")
end

local function limit(value, maximum)
    local text = tostring(value or "")
    if string.len(text) > maximum then return string.sub(text, 1, maximum) end
    return text
end

local objectiveIdSequence = 0

local function objectiveIdSuffix()
    objectiveIdSequence = objectiveIdSequence + 1

    local stamp = nil

    if getTimestampMs ~= nil then
        local ok, value = pcall(getTimestampMs)

        if ok and value ~= nil then
            stamp = tonumber(value)
        end
    end

    if stamp == nil and getGameTime ~= nil then
        local ok, gameTime = pcall(getGameTime)

        if ok and gameTime ~= nil
            and gameTime.getWorldAgeHours ~= nil then
            local ageOk, age = pcall(function()
                return gameTime:getWorldAgeHours()
            end)

            if ageOk and age ~= nil then
                stamp = math.floor((tonumber(age) or 0) * 3600000)
            end
        end
    end

    stamp = math.floor(tonumber(stamp) or objectiveIdSequence)

    return tostring(stamp) .. "-" .. tostring(objectiveIdSequence)
end

local function scriptValue(item, methodName)
    if item == nil or item[methodName] == nil then return "" end
    local ok, value = pcall(function() return item[methodName](item) end)
    return ok and value ~= nil and tostring(value) or ""
end

local function makeCatalogEntry(scriptItem)
    if scriptItem == nil then return nil end
    local fullType = scriptValue(scriptItem, "getFullName")
    local displayName = scriptValue(scriptItem, "getDisplayName")
    local typeName = scriptValue(scriptItem, "getName")
    if fullType == "" or displayName == "" then return nil end
    if scriptItem.getObsolete ~= nil then
        local ok, value = pcall(function() return scriptItem:getObsolete() end)
        if ok and value == true then return nil end
    end
    if scriptItem.isHidden ~= nil then
        local ok, value = pcall(function() return scriptItem:isHidden() end)
        if ok and value == true then return nil end
    end
    return {
        fullType = fullType,
        displayName = displayName,
        typeName = typeName,
        searchText = string.lower(displayName .. " " .. fullType .. " " .. typeName)
    }
end

local function startCatalog()
    if QPSC_MultiObjectiveUI.itemCatalog ~= nil
        or QPSC_MultiObjectiveUI.catalogBuild ~= nil then return end
    local manager = getScriptManager and getScriptManager() or nil
    if manager == nil then QPSC_MultiObjectiveUI.itemCatalog = {}; return end
    local items = nil
    if manager.getAllItems ~= nil then
        local ok, value = pcall(function() return manager:getAllItems() end)
        if ok then items = value end
    end
    if items == nil then QPSC_MultiObjectiveUI.itemCatalog = {}; return end
    QPSC_MultiObjectiveUI.catalogBuild = {items=items, index=0, result={}}
end

local function stepCatalog(maximum)
    local build = QPSC_MultiObjectiveUI.catalogBuild
    if build == nil then return end
    local size = build.items:size()
    local stop = math.min(size - 1, build.index + (maximum or 250) - 1)
    for index = build.index, stop do
        local entry = makeCatalogEntry(build.items:get(index))
        if entry ~= nil then table.insert(build.result, entry) end
    end
    build.index = stop + 1
    if build.index >= size then
        table.sort(build.result, function(a,b)
            return string.lower(a.displayName) < string.lower(b.displayName)
        end)
        QPSC_MultiObjectiveUI.itemCatalog = build.result
        QPSC_MultiObjectiveUI.catalogBuild = nil
    end
end

local function itemRank(entry, search)
    local display = string.lower(entry.displayName or "")
    local fullType = string.lower(entry.fullType or "")
    if display == search or fullType == search then return 0 end
    if string.sub(display, 1, #search) == search then return 1 end
    if string.find(display, search, 1, true) then return 2 end
    if string.find(fullType, search, 1, true) then return 3 end
    if string.find(entry.searchText or "", search, 1, true) then return 4 end
    return nil
end

local function drawRow(list, y, item, alt)
    local height = list.itemheight
    if alt then list:drawRect(0, y, list.width, height - 1, 0.12, 0.18, 0.18, 0.18) end
    if list.selected == item.index then
        list:drawRect(0, y, list.width, height - 1, 0.35, 0.42, 0.28, 0.10)
        list:drawRectBorder(0, y, list.width, height - 1, 0.85, 0.78, 0.62, 0.25)
    end
    list:drawText(tostring(item.text or ""), 7, y + 3, 0.92, 0.92, 0.92, 1, UIFont.Small)
    return y + height
end

local function itemFromFields(fullType, displayName)
    fullType = tostring(fullType or "")
    if fullType == "" then return nil end
    return {fullType=fullType, displayName=tostring(displayName or fullType)}
end

-- QPSC_V130_MULTIPLE_REWARDS_V1
local QPSC_MO_MAX_REWARD_ITEMS = 5

local function QPSC_MO_rewardItemsFromContract(contract)
    local rewards = {}
    local rawRewards =
        type(contract and contract.rewardItems) == "table"
        and contract.rewardItems
        or nil

    if rawRewards ~= nil then
        for index = 1, math.min(
            #rawRewards,
            QPSC_MO_MAX_REWARD_ITEMS
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

local function QPSC_MO_rewardSummary(rewards)
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

local function QPSC_MO_addReward(
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

    if #(rewards or {}) >= QPSC_MO_MAX_REWARD_ITEMS then
        return false, "limit"
    end

    table.insert(rewards, {
        fullType = fullType,
        displayName = displayName,
        quantity = amount
    })

    return true, "added"
end

local function QPSC_MO_applyRewardArgs(args, rewards)
    local list = rewards or {}
    args.rewardCount = #list

    for index = 1, QPSC_MO_MAX_REWARD_ITEMS do
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

local function objectiveText(objective, index)
    local prefix = tostring(index) .. ". "
    if objective.type == "DELIVERY" then
        return prefix .. QPSC_I18N.getText("UI_QPSC_MultiDeliveryLabel", objective.target, objective.itemDisplayName or objective.itemFullType)
    elseif objective.type == "KILL" then
        return prefix .. QPSC_I18N.getText("UI_QPSC_MultiKillAreaLabel", objective.target, objective.radius)
    end
    return prefix .. QPSC_I18N.getText("UI_QPSC_MultiLocationAreaLabel", objective.radius)
end

QPSC_MultiObjectiveWindow = ISPanel:derive("QPSC_MultiObjectiveWindow")

function QPSC_MultiObjectiveWindow:new(x, y, width, height, player, categoryKey, editContract)
    local o = ISPanel:new(x, y, width, height)
    setmetatable(o, self); self.__index = self
    o.player = player
    o.categoryKey = tostring(categoryKey or "NONE")
    o.editContract = editContract
    o.isEditMode = editContract ~= nil
    o.reputationAvailable = QPSC_MO_reputationAvailable()
    o.objectives = {}
    o.editingObjectiveIndex = nil
    o.selectedObjectiveItem = nil
    o.rewardItems =
        QPSC_MO_rewardItemsFromContract(editContract)
    o.selectedRewardItem =
        o.rewardItems[1]
        and itemFromFields(
            o.rewardItems[1].fullType,
            o.rewardItems[1].displayName
        )
        or nil
    o.selectedBonusItem = nil
    o.lastSearch = nil
    o.searchDelay = 0
    o.objectivesLocked = o.isEditMode and #(editContract.participants or {}) > 0
    o.moveWithMouse = true
    o.resizing = false
    o.resizeGripSize = RESIZE_GRIP
    o.minWindowWidth = MIN_WIDTH
    o.minWindowHeight = MIN_HEIGHT
    o.lastLayoutWidth = nil
    o.lastLayoutHeight = nil
    o.backgroundColor = {r=0.025,g=0.025,b=0.025,a=0.98}
    o.borderColor = {r=0.82,g=0.66,b=0.24,a=0.95}
    return o
end

function QPSC_MultiObjectiveWindow:layoutControls()
    if self.titleEntry == nil then return end

    local FL = QPSC_FlexibleLayout
    local margin = 18
    local gap = 8
    local rightX = math.max(500, math.floor(self.width * 0.48))
    rightX = math.min(rightX, self.width - 360)

    local leftWidth = math.max(430, rightX - margin - 20)
    local rightWidth = math.max(330, self.width - rightX - margin)

    self.titleEntry:setWidth(self.width - (margin * 2))
    self.locationEntry:setWidth(self.width - (margin * 2))
    self.descriptionEntry:setWidth(self.width - (margin * 2))

    local topColumns = FL.columns(
        margin,
        self.width - margin,
        {0.24, 0.21, 0.27, 0.28},
        14,
        {150, 135, 180, 140}
    )
    self.categoryCombo:setX(topColumns[1].x)
    self.categoryCombo:setWidth(topColumns[1].width)
    self.difficultyCombo:setX(topColumns[2].x)
    self.difficultyCombo:setWidth(topColumns[2].width)
    self.modeCombo:setX(topColumns[3].x)
    self.modeCombo:setWidth(topColumns[3].width)
    self.timeEntry:setX(topColumns[4].x)
    self.timeEntry:setWidth(topColumns[4].width)

    local objectiveColumns = FL.columns(
        margin,
        self.width - margin,
        {0.24, 0.11, 0.11, 0.23, 0.31},
        12,
        {160, 80, 80, 150, 185}
    )
    self.objectiveCombo:setX(objectiveColumns[1].x)
    self.objectiveCombo:setWidth(objectiveColumns[1].width)
    self.targetEntry:setX(objectiveColumns[2].x)
    self.targetEntry:setWidth(objectiveColumns[2].width)
    self.radiusEntry:setX(objectiveColumns[3].x)
    self.radiusEntry:setWidth(objectiveColumns[3].width)
    self.addObjectiveButton:setX(objectiveColumns[4].x)
    self.addObjectiveButton:setWidth(objectiveColumns[4].width)
    self.usePositionButton:setX(objectiveColumns[5].x)
    self.usePositionButton:setWidth(objectiveColumns[5].width)

    self.layoutTopLabelX = {
        category=topColumns[1].x,
        difficulty=topColumns[2].x,
        completion=topColumns[3].x,
        time=topColumns[4].x,
        objective=objectiveColumns[1].x,
        target=objectiveColumns[2].x,
        radius=objectiveColumns[3].x
    }

    local actionButtonPlanLeft = FL.planButtons(
        {
            self.setObjectiveItemButton,
            self.setRewardButton,
            self.setBonusButton
        },
        leftWidth,
        gap,
        120,
        330
    )
    local rightActionColumnWidth = math.max(
        140,
        math.floor((rightWidth - gap) / 2)
    )
    local rightActionHeight = 28 * 2 + 6
    local actionHeight = math.max(
        actionButtonPlanLeft.height,
        rightActionHeight
    )

    local quantityWidth = FL.clamp(
        math.floor(leftWidth * 0.16),
        74,
        96
    )
    local pointsWidth = quantityWidth
    local clearRewardWidth = FL.buttonWidth(
        self.clearRewardButton,
        110,
        math.max(130, math.floor(leftWidth * 0.44)),
        28
    )
    local clearBonusWidth = FL.buttonWidth(
        self.clearBonusButton,
        110,
        math.max(130, math.floor(leftWidth * 0.44)),
        28
    )

    local rewardRowHeight = 58
    local bonusRowHeight = 58
    local pointsLabel = QPSC_I18N.getText("UI_QPSC_ReputationPoints")
    local unavailableSuffix = self.reputationAvailable
        and ""
        or (
            " (" .. QPSC_I18N.getText("UI_QPSC_ReputationUnavailable") .. ")"
        )
    local primaryLabel = QPSC_I18N.getText(
        "UI_QPSC_PrimaryReputationReward"
    ) .. unavailableSuffix
    local secondaryLabel = QPSC_I18N.getText(
        "UI_QPSC_SecondaryReputationReward"
    ) .. unavailableSuffix

    local function reputationRowHeight(labelText)
        local labelWidth = FL.measure(labelText, UIFont.Small)
        local pointsLabelWidth = FL.measure(pointsLabel, UIFont.Small)
        local pointsX = margin + leftWidth - pointsWidth
        local overlap = margin + labelWidth + 12
            > pointsX - pointsLabelWidth
        return overlap and 78 or 58, overlap
    end

    local primaryHeight, primaryWrapped = reputationRowHeight(primaryLabel)
    local secondaryHeight, secondaryWrapped = reputationRowHeight(secondaryLabel)
    local bottomSectionHeight = rewardRowHeight
        + 6 + bonusRowHeight
        + 6 + primaryHeight
        + 6 + secondaryHeight

    local bottomY = self.height - 38
    local createWidth = FL.buttonWidth(self.createButton, 220, 430, 34)
    local closeWidth = FL.buttonWidth(self.closeButton, 130, 220, 34)
    createWidth = math.min(
        createWidth,
        math.max(180, self.width - margin * 2 - closeWidth - 20)
    )
    self.createButton:setX(margin)
    self.createButton:setY(bottomY)
    self.createButton:setWidth(createWidth)
    self.closeButton:setX(self.width - margin - closeWidth)
    self.closeButton:setY(bottomY)
    self.closeButton:setWidth(closeWidth)

    local rewardStartY = bottomY - 10 - bottomSectionHeight
    local actionY = rewardStartY - 8 - actionHeight
    local listTop = 358
    local listHeight = math.max(24, actionY - listTop - 8)

    self.searchEntry:setWidth(leftWidth)
    self.itemList:setWidth(leftWidth)
    self.itemList:setHeight(listHeight)
    self.objectiveList:setX(rightX)
    self.objectiveList:setWidth(rightWidth)
    self.objectiveList:setHeight(listHeight)

    FL.applyButtonPlan(actionButtonPlanLeft, margin, actionY)

    self.editObjectiveButton:setX(rightX)
    self.editObjectiveButton:setY(actionY)
    self.editObjectiveButton:setWidth(rightActionColumnWidth)

    self.removeObjectiveButton:setX(rightX + rightActionColumnWidth + gap)
    self.removeObjectiveButton:setY(actionY)
    self.removeObjectiveButton:setWidth(rightActionColumnWidth)

    self.upButton:setX(rightX)
    self.upButton:setY(actionY + 34)
    self.upButton:setWidth(rightActionColumnWidth)

    self.downButton:setX(rightX + rightActionColumnWidth + gap)
    self.downButton:setY(actionY + 34)
    self.downButton:setWidth(rightActionColumnWidth)

    local rewardTop = rewardStartY
    local bonusTop = rewardTop + rewardRowHeight + 6
    local primaryTop = bonusTop + bonusRowHeight + 6
    local secondaryTop = primaryTop + primaryHeight + 6

    local quantityLabelWidth = FL.measure(
        QPSC_I18N.getText("UI_QPSC_Quantity"),
        UIFont.Small
    )
    local quantityEntryX = margin + quantityLabelWidth + 10
    local clearRewardX = quantityEntryX + quantityWidth + gap
    local clearBonusX = quantityEntryX + quantityWidth + gap

    self.rewardQtyEntry:setX(quantityEntryX)
    self.rewardQtyEntry:setY(rewardTop + 26)
    self.rewardQtyEntry:setWidth(quantityWidth)
    self.clearRewardButton:setX(clearRewardX)
    self.clearRewardButton:setY(rewardTop + 26)
    self.clearRewardButton:setWidth(
        math.min(clearRewardWidth, margin + leftWidth - clearRewardX)
    )

    self.bonusQtyEntry:setX(quantityEntryX)
    self.bonusQtyEntry:setY(bonusTop + 26)
    self.bonusQtyEntry:setWidth(quantityWidth)
    self.clearBonusButton:setX(clearBonusX)
    self.clearBonusButton:setY(bonusTop + 26)
    self.clearBonusButton:setWidth(
        math.min(clearBonusWidth, margin + leftWidth - clearBonusX)
    )

    local reputationPointsX = margin + leftWidth - pointsWidth
    local comboWidth = math.max(170, reputationPointsX - margin - 12)

    self.reputationCombo:setX(margin)
    self.reputationCombo:setY(primaryTop + (primaryWrapped and 46 or 26))
    self.reputationCombo:setWidth(comboWidth)
    self.reputationPointsEntry:setX(reputationPointsX)
    self.reputationPointsEntry:setY(primaryTop + (primaryWrapped and 46 or 26))
    self.reputationPointsEntry:setWidth(pointsWidth)

    self.secondaryReputationCombo:setX(margin)
    self.secondaryReputationCombo:setY(secondaryTop + (secondaryWrapped and 46 or 26))
    self.secondaryReputationCombo:setWidth(comboWidth)
    self.secondaryReputationPointsEntry:setX(reputationPointsX)
    self.secondaryReputationPointsEntry:setY(secondaryTop + (secondaryWrapped and 46 or 26))
    self.secondaryReputationPointsEntry:setWidth(pointsWidth)

    self.flexLayout = {
        rightX=rightX,
        leftWidth=leftWidth,
        rightWidth=rightWidth,
        rewardTop=rewardTop,
        bonusTop=bonusTop,
        primaryTop=primaryTop,
        secondaryTop=secondaryTop,
        primaryWrapped=primaryWrapped,
        secondaryWrapped=secondaryWrapped,
        pointsX=reputationPointsX,
        primaryLabel=primaryLabel,
        secondaryLabel=secondaryLabel,
        pointsLabel=pointsLabel,
        quantityLabelX=margin,
        quantityEntryX=quantityEntryX,
        actionY=actionY
    }

    self.layoutRightX = rightX
    self.layoutRewardStartY = rewardStartY
    self.layoutActionY = actionY
    self.lastLayoutWidth = self.width
    self.lastLayoutHeight = self.height
end

function QPSC_MultiObjectiveWindow:resizeBy(dx, dy)
    local screenWidth = getCore():getScreenWidth()
    local screenHeight = getCore():getScreenHeight()
    local currentX = tonumber(self:getX()) or 0
    local currentY = tonumber(self:getY()) or 0
    local minWidth = math.min(
        self.minWindowWidth or MIN_WIDTH,
        math.max(760, screenWidth - 20)
    )
    local minHeight = math.min(
        self.minWindowHeight or MIN_HEIGHT,
        math.max(660, screenHeight - 20)
    )
    local maxWidth = math.max(minWidth, screenWidth - currentX - 10)
    local maxHeight = math.max(minHeight, screenHeight - currentY - 10)
    local newWidth = math.max(
        minWidth,
        math.min(maxWidth, (tonumber(self.width) or minWidth) + (tonumber(dx) or 0))
    )
    local newHeight = math.max(
        minHeight,
        math.min(maxHeight, (tonumber(self.height) or minHeight) + (tonumber(dy) or 0))
    )

    self:setWidth(newWidth)
    self:setHeight(newHeight)
    self:layoutControls()
end

function QPSC_MultiObjectiveWindow:onMouseDown(x, y)
    local gripSize = tonumber(self.resizeGripSize) or RESIZE_GRIP

    if x >= self.width - gripSize
        and y >= self.height - gripSize then
        self.resizing = true

        if self.setCapture then self:setCapture(true) end

        return true
    end

    if ISPanel.onMouseDown then
        return ISPanel.onMouseDown(self, x, y)
    end

    return false
end

function QPSC_MultiObjectiveWindow:onMouseMove(dx, dy)
    if self.resizing then
        self:resizeBy(dx, dy)
        return true
    end

    if ISPanel.onMouseMove then
        return ISPanel.onMouseMove(self, dx, dy)
    end

    return false
end

function QPSC_MultiObjectiveWindow:onMouseMoveOutside(dx, dy)
    if self.resizing then
        self:resizeBy(dx, dy)
        return
    end

    if ISPanel.onMouseMoveOutside then
        ISPanel.onMouseMoveOutside(self, dx, dy)
    end
end

function QPSC_MultiObjectiveWindow:onMouseUp(x, y)
    if self.resizing then
        self.resizing = false

        if self.setCapture then self:setCapture(false) end

        return true
    end

    if ISPanel.onMouseUp then
        return ISPanel.onMouseUp(self, x, y)
    end

    return false
end

function QPSC_MultiObjectiveWindow:initialise()
    ISPanel.initialise(self)
    local margin = 18
    self.titleEntry = ISTextEntryBox:new("", margin, 62, self.width - 36, 26); self.titleEntry:initialise(); self.titleEntry:instantiate(); self:addChild(self.titleEntry)
    self.locationEntry = ISTextEntryBox:new("", margin, 112, self.width - 36, 26); self.locationEntry:initialise(); self.locationEntry:instantiate(); self:addChild(self.locationEntry)
    self.descriptionEntry = ISTextEntryBox:new("", margin, 162, self.width - 36, 26); self.descriptionEntry:initialise(); self.descriptionEntry:instantiate(); self:addChild(self.descriptionEntry)

    self.categoryCombo = ISComboBox:new(margin, 212, 190, 26, self, function() end); self.categoryCombo:initialise(); self.categoryCombo:instantiate()
    for _, d in ipairs(CATEGORY_DEFS) do self.categoryCombo:addOption(QPSC_I18N.getText(d.label)) end; self:addChild(self.categoryCombo)
    self.difficultyCombo = ISComboBox:new(224, 212, 170, 26, self, function() end); self.difficultyCombo:initialise(); self.difficultyCombo:instantiate()
    for _, d in ipairs(DIFFICULTY_DEFS) do self.difficultyCombo:addOption(QPSC_I18N.getText(d.label)) end; self:addChild(self.difficultyCombo)
    self.modeCombo = ISComboBox:new(410, 212, 210, 26, self, QPSC_MultiObjectiveWindow.onModeChanged); self.modeCombo:initialise(); self.modeCombo:instantiate()
    for _, d in ipairs(MODE_DEFS) do self.modeCombo:addOption(QPSC_I18N.getText(d.label)) end; self:addChild(self.modeCombo)
    self.timeEntry = ISTextEntryBox:new("", 636, 212, self.width - 654, 26); self.timeEntry:initialise(); self.timeEntry:instantiate(); self:addChild(self.timeEntry)

    self.objectiveCombo = ISComboBox:new(margin, 276, 180, 26, self, QPSC_MultiObjectiveWindow.onObjectiveTypeChanged); self.objectiveCombo:initialise(); self.objectiveCombo:instantiate()
    for _, d in ipairs(OBJECTIVE_DEFS) do self.objectiveCombo:addOption(QPSC_I18N.getText(d.label)) end; self:addChild(self.objectiveCombo)
    self.targetEntry = ISTextEntryBox:new("8", 214, 276, 85, 26); self.targetEntry:initialise(); self.targetEntry:instantiate(); self.targetEntry:setOnlyNumbers(true); self:addChild(self.targetEntry)
    self.radiusEntry = ISTextEntryBox:new("", 315, 276, 85, 26); self.radiusEntry:initialise(); self.radiusEntry:instantiate(); self.radiusEntry:setOnlyNumbers(true); self:addChild(self.radiusEntry)
    self.addObjectiveButton = ISButton:new(416, 276, 170, 26, QPSC_I18N.getText("UI_QPSC_AddObjective"), self, QPSC_MultiObjectiveWindow.onAddObjective); self.addObjectiveButton:initialise(); self.addObjectiveButton:instantiate(); self:addChild(self.addObjectiveButton)
    self.usePositionButton = ISButton:new(600, 276, 190, 26, QPSC_I18N.getText("UI_QPSC_UseCurrentPosition"), self, QPSC_MultiObjectiveWindow.onUseCurrentPosition); self.usePositionButton:initialise(); self.usePositionButton:instantiate(); self:addChild(self.usePositionButton)

    self.searchEntry = ISTextEntryBox:new("", margin, 326, 470, 26); self.searchEntry:initialise(); self.searchEntry:instantiate(); self:addChild(self.searchEntry)
    self.itemList = ISScrollingListBox:new(margin, 358, 470, 150); self.itemList:initialise(); self.itemList:instantiate(); self.itemList.itemheight=25; self.itemList.doDrawItem=drawRow; self:addChild(self.itemList)
    self.setObjectiveItemButton = ISButton:new(margin, 516, 150, 26, QPSC_I18N.getText("UI_QPSC_SetObjectiveItem"), self, QPSC_MultiObjectiveWindow.onSetObjectiveItem); self.setObjectiveItemButton:initialise(); self.setObjectiveItemButton:instantiate(); self:addChild(self.setObjectiveItemButton)
    self.setRewardButton = ISButton:new(178, 516, 140, 26, QPSC_I18N.getText("UI_QPSC_AddRewardItem"), self, QPSC_MultiObjectiveWindow.onSetReward); self.setRewardButton:initialise(); self.setRewardButton:instantiate(); self:addChild(self.setRewardButton)
    self.setBonusButton = ISButton:new(326, 516, 160, 26, QPSC_I18N.getText("UI_QPSC_SetFirstFinisherBonusItem"), self, QPSC_MultiObjectiveWindow.onSetBonus); self.setBonusButton:initialise(); self.setBonusButton:instantiate(); self:addChild(self.setBonusButton)

    self.objectiveList = ISScrollingListBox:new(510, 326, self.width - 528, 182); self.objectiveList:initialise(); self.objectiveList:instantiate(); self.objectiveList.itemheight=30; self.objectiveList.doDrawItem=drawRow; self:addChild(self.objectiveList)
    self.editObjectiveButton = ISButton:new(510, 516, 100, 26, QPSC_I18N.getText("UI_QPSC_Edit"), self, QPSC_MultiObjectiveWindow.onEditObjective); self.editObjectiveButton:initialise(); self.editObjectiveButton:instantiate(); self:addChild(self.editObjectiveButton)
    self.removeObjectiveButton = ISButton:new(618, 516, 105, 26, QPSC_I18N.getText("UI_QPSC_Remove"), self, QPSC_MultiObjectiveWindow.onRemoveObjective); self.removeObjectiveButton:initialise(); self.removeObjectiveButton:instantiate(); self:addChild(self.removeObjectiveButton)
    self.upButton = ISButton:new(731, 516, 80, 26, QPSC_I18N.getText("UI_QPSC_MoveUp"), self, QPSC_MultiObjectiveWindow.onMoveUp); self.upButton:initialise(); self.upButton:instantiate(); self:addChild(self.upButton)
    self.downButton = ISButton:new(819, 516, 90, 26, QPSC_I18N.getText("UI_QPSC_MoveDown"), self, QPSC_MultiObjectiveWindow.onMoveDown); self.downButton:initialise(); self.downButton:instantiate(); self:addChild(self.downButton)

    self.rewardQtyEntry = ISTextEntryBox:new("1", 200, 590, 75, 26); self.rewardQtyEntry:initialise(); self.rewardQtyEntry:instantiate(); self.rewardQtyEntry:setOnlyNumbers(true); self:addChild(self.rewardQtyEntry)
    self.clearRewardButton = ISButton:new(288, 590, 120, 26, QPSC_I18N.getText("UI_QPSC_ClearRewards"), self, function(self) self.rewardItems={}; self.selectedRewardItem=nil end); self.clearRewardButton:initialise(); self.clearRewardButton:instantiate(); self:addChild(self.clearRewardButton)
    self.bonusQtyEntry = ISTextEntryBox:new("1", 200, 642, 75, 26); self.bonusQtyEntry:initialise(); self.bonusQtyEntry:instantiate(); self.bonusQtyEntry:setOnlyNumbers(true); self:addChild(self.bonusQtyEntry)
    self.clearBonusButton = ISButton:new(288, 642, 120, 26, QPSC_I18N.getText("UI_QPSC_ClearFirstFinisherBonus"), self, function(self) self.selectedBonusItem=nil end); self.clearBonusButton:initialise(); self.clearBonusButton:instantiate(); self:addChild(self.clearBonusButton)

    self.reputationCombo = ISComboBox:new(18, 682, 220, 26, self, function() end); self.reputationCombo:initialise(); self.reputationCombo:instantiate()
    for _, definition in ipairs(REPUTATION_DEFS) do self.reputationCombo:addOption(reputationLabel(definition)) end; self:addChild(self.reputationCombo)
    self.reputationPointsEntry = ISTextEntryBox:new("0", 250, 682, 110, 26); self.reputationPointsEntry:initialise(); self.reputationPointsEntry:instantiate()
    if self.reputationPointsEntry.setOnlyNumbers then self.reputationPointsEntry:setOnlyNumbers(true) end
    self:addChild(self.reputationPointsEntry)

    self.secondaryReputationCombo = ISComboBox:new(18, 732, 220, 26, self, function() end); self.secondaryReputationCombo:initialise(); self.secondaryReputationCombo:instantiate()
    for _, definition in ipairs(REPUTATION_DEFS) do self.secondaryReputationCombo:addOption(reputationLabel(definition)) end; self:addChild(self.secondaryReputationCombo)
    self.secondaryReputationPointsEntry = ISTextEntryBox:new("0", 250, 732, 110, 26); self.secondaryReputationPointsEntry:initialise(); self.secondaryReputationPointsEntry:instantiate()
    if self.secondaryReputationPointsEntry.setOnlyNumbers then self.secondaryReputationPointsEntry:setOnlyNumbers(true) end
    self:addChild(self.secondaryReputationPointsEntry)

    self.createButton = ISButton:new(margin, self.height - 42, 280, 28, self.isEditMode and QPSC_I18N.getText("UI_QPSC_SaveChanges") or QPSC_I18N.getText("UI_QPSC_CreateMultiContract"), self, QPSC_MultiObjectiveWindow.onCreate); self.createButton:initialise(); self.createButton:instantiate(); self:addChild(self.createButton)
    self.closeButton = ISButton:new(self.width - 178, self.height - 42, 160, 28, QPSC_I18N.getText("UI_QPSC_Close"), self, QPSC_MultiObjectiveWindow.onClose); self.closeButton:initialise(); self.closeButton:instantiate(); self:addChild(self.closeButton)

    self.currentX = math.floor(tonumber(self.player:getX()) or 0)
    self.currentY = math.floor(tonumber(self.player:getY()) or 0)
    self.currentZ = math.floor(tonumber(self.player:getZ()) or 0)
    startCatalog()
    self:populate()
    self.reputationCombo.enable = self.reputationAvailable
    self.reputationPointsEntry.enable = self.reputationAvailable
    self.secondaryReputationCombo.enable = self.reputationAvailable
    self.secondaryReputationPointsEntry.enable = self.reputationAvailable
    if not self.reputationAvailable and not self.isEditMode then
        self.reputationCombo.selected = 1
        self.reputationPointsEntry:setText("0")
        self.secondaryReputationCombo.selected = 1
        self.secondaryReputationPointsEntry:setText("0")
    end
    self:onObjectiveTypeChanged()
    self:onModeChanged()
    self:refreshObjectiveList()
    self:layoutControls()
end

function QPSC_MultiObjectiveWindow:getDef(defs, combo)
    return defs[tonumber(combo.selected) or 1] or defs[1]
end

function QPSC_MultiObjectiveWindow:getSelectedCatalogItem()
    local index = tonumber(self.itemList.selected) or 0
    local row = self.itemList.items and self.itemList.items[index] or nil
    return row and row.item or nil
end

function QPSC_MultiObjectiveWindow:populate()
    local contract = self.editContract
    if contract == nil then
        for i,d in ipairs(CATEGORY_DEFS) do if d.key == self.categoryKey then self.categoryCombo.selected=i end end
        self.modeCombo.selected = 1; self.difficultyCombo.selected = 1; return
    end
    self.titleEntry:setText(tostring(contract.title or "")); self.locationEntry:setText(tostring(contract.location or "")); self.descriptionEntry:setText(tostring(contract.description or ""))
    local time = tonumber(contract.timeLimitHours) or 0; self.timeEntry:setText(time > 0 and tostring(time) or "")
    self.rewardItems =
        QPSC_MO_rewardItemsFromContract(contract)
    local firstReward = self.rewardItems[1]
    self.rewardQtyEntry:setText(
        tostring(
            firstReward
            and math.max(
                1,
                tonumber(firstReward.quantity) or 1
            )
            or 1
        )
    )
    self.bonusQtyEntry:setText(tostring(math.max(1, tonumber(contract.firstFinisherBonusQuantity) or 1)))
    self.selectedRewardItem =
        firstReward
        and itemFromFields(
            firstReward.fullType,
            firstReward.displayName
        )
        or nil
    self.selectedBonusItem=itemFromFields(contract.firstFinisherBonusItemFullType, contract.firstFinisherBonusItemDisplayName)
    local reputationPath=string.lower(tostring(contract.reputationPath or ""))
    self.reputationPointsEntry:setText(tostring(math.max(0,math.floor(tonumber(contract.reputationPoints) or 0))))
    for i,d in ipairs(REPUTATION_DEFS) do if d.key==reputationPath then self.reputationCombo.selected=i end end
    local secondaryReputationPath=string.lower(tostring(contract.secondaryReputationPath or ""))
    self.secondaryReputationPointsEntry:setText(tostring(math.max(0,math.floor(tonumber(contract.secondaryReputationPoints) or 0))))
    for i,d in ipairs(REPUTATION_DEFS) do if d.key==secondaryReputationPath then self.secondaryReputationCombo.selected=i end end
    for i,d in ipairs(CATEGORY_DEFS) do if d.key == tostring(contract.category or "NONE") then self.categoryCombo.selected=i end end
    for i,d in ipairs(DIFFICULTY_DEFS) do if d.key == tostring(contract.difficulty or "UNRATED") then self.difficultyCombo.selected=i end end
    for i,d in ipairs(MODE_DEFS) do if d.key == tostring(contract.completionMode or "INDIVIDUAL") then self.modeCombo.selected=i end end
    for i,objective in ipairs(contract.objectives or {}) do
        self.objectives[i] = {
            id=tostring(objective.id or ("OBJ-"..i)), type=tostring(objective.type or "KILL"), target=tonumber(objective.target) or 1,
            radius=tonumber(objective.radius) or 0, itemFullType=tostring(objective.itemFullType or ""), itemDisplayName=tostring(objective.itemDisplayName or ""),
            targetX=tonumber(objective.targetX) or 0, targetY=tonumber(objective.targetY) or 0, targetZ=tonumber(objective.targetZ) or 0
        }
    end
end

function QPSC_MultiObjectiveWindow:onUseCurrentPosition()
    self.currentX=math.floor(tonumber(self.player:getX()) or 0); self.currentY=math.floor(tonumber(self.player:getY()) or 0); self.currentZ=math.floor(tonumber(self.player:getZ()) or 0)
    self.player:Say(QPSC_I18N.getText("UI_QPSC_PositionCaptured", self.currentX, self.currentY, self.currentZ))
end

function QPSC_MultiObjectiveWindow:onObjectiveTypeChanged()
    local t=self:getDef(OBJECTIVE_DEFS,self.objectiveCombo).key
    self.targetEntry.enable=t~="LOCATION" and not self.objectivesLocked
    self.radiusEntry.enable=t~="DELIVERY" and not self.objectivesLocked
    self.setObjectiveItemButton.enable=t=="DELIVERY" and not self.objectivesLocked
    self.addObjectiveButton.enable=not self.objectivesLocked
    self.usePositionButton.enable=not self.objectivesLocked
    if t=="DELIVERY" then self.radiusEntry:setText("")
    elseif t=="KILL" and trim(self.radiusEntry:getText())=="" then self.radiusEntry:setText("100")
    elseif t=="LOCATION" then self.targetEntry:setText("1"); if trim(self.radiusEntry:getText())=="" then self.radiusEntry:setText("3") end end
end

function QPSC_MultiObjectiveWindow:onModeChanged()
    local shared=self:getDef(MODE_DEFS,self.modeCombo).key=="SHARED_TEAM"
    self.modeCombo.enable=not self.objectivesLocked
    self.setBonusButton.enable=not shared
    self.bonusQtyEntry.enable=not shared
    self.clearBonusButton.enable=not shared
    if shared then self.selectedBonusItem=nil end
end

function QPSC_MultiObjectiveWindow:onSetObjectiveItem() local item=self:getSelectedCatalogItem(); if item then self.selectedObjectiveItem=item end end
function QPSC_MultiObjectiveWindow:onSetReward()
    local item = self:getSelectedCatalogItem()
    if item == nil then return end

    local quantity =
        tonumber(self.rewardQtyEntry:getText()) or 0
    local ok, result = QPSC_MO_addReward(
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
        and itemFromFields(
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
            QPSC_MO_MAX_REWARD_ITEMS
        )
    )
end
function QPSC_MultiObjectiveWindow:onSetBonus() if self:getDef(MODE_DEFS,self.modeCombo).key~="SHARED_TEAM" then local item=self:getSelectedCatalogItem(); if item then self.selectedBonusItem=item end end end

function QPSC_MultiObjectiveWindow:onAddObjective()
    if self.objectivesLocked then return end
    local editIndex=self.editingObjectiveIndex
    if editIndex==nil and #self.objectives>=MAX_OBJECTIVES then self.player:Say(QPSC_I18N.getText("UI_QPSC_MessageMaximumObjectives",MAX_OBJECTIVES)); return end
    local t=self:getDef(OBJECTIVE_DEFS,self.objectiveCombo).key
    local target=tonumber(self.targetEntry:getText()) or 0; local radius=tonumber(self.radiusEntry:getText()) or 0
    if t=="DELIVERY" then
        if target<1 or target>10000 or self.selectedObjectiveItem==nil then self.player:Say(QPSC_I18N.getText("UI_QPSC_MessageInvalidObjective")); return end
        radius=0
    elseif t=="KILL" then
        if target<1 or target>10000 or radius<1 or radius>1000 then self.player:Say(QPSC_I18N.getText("UI_QPSC_MessageInvalidObjective")); return end
    else
        target=1; if radius<1 or radius>20 then self.player:Say(QPSC_I18N.getText("UI_QPSC_MessageInvalidRadius")); return end
    end
    local objective={
        id=editIndex and tostring(self.objectives[editIndex].id) or ("OBJ-"..tostring(#self.objectives+1).."-"..objectiveIdSuffix()),
        type=t,target=target,radius=radius,itemFullType=t=="DELIVERY" and self.selectedObjectiveItem.fullType or "",itemDisplayName=t=="DELIVERY" and self.selectedObjectiveItem.displayName or "",
        targetX=self.currentX,targetY=self.currentY,targetZ=self.currentZ
    }
    if editIndex then self.objectives[editIndex]=objective else table.insert(self.objectives,objective) end
    self.editingObjectiveIndex=nil; self.addObjectiveButton:setTitle(QPSC_I18N.getText("UI_QPSC_AddObjective")); self.selectedObjectiveItem=nil; self:refreshObjectiveList()
end

function QPSC_MultiObjectiveWindow:selectedObjectiveIndex()
    local selected=tonumber(self.objectiveList.selected) or 0
    return selected>=1 and selected<=#self.objectives and selected or nil
end

function QPSC_MultiObjectiveWindow:onEditObjective()
    if self.objectivesLocked then return end
    local i=self:selectedObjectiveIndex(); if not i then return end
    local o=self.objectives[i]; self.editingObjectiveIndex=i
    for index,d in ipairs(OBJECTIVE_DEFS) do if d.key==o.type then self.objectiveCombo.selected=index end end
    self.targetEntry:setText(tostring(o.target)); self.radiusEntry:setText(o.radius>0 and tostring(o.radius) or "")
    self.selectedObjectiveItem=itemFromFields(o.itemFullType,o.itemDisplayName); self.currentX=o.targetX; self.currentY=o.targetY; self.currentZ=o.targetZ
    self.addObjectiveButton:setTitle(QPSC_I18N.getText("UI_QPSC_UpdateObjective")); self:onObjectiveTypeChanged()
end

function QPSC_MultiObjectiveWindow:onRemoveObjective() if self.objectivesLocked then return end; local i=self:selectedObjectiveIndex(); if i then table.remove(self.objectives,i); self:refreshObjectiveList() end end
function QPSC_MultiObjectiveWindow:onMoveUp() if self.objectivesLocked then return end; local i=self:selectedObjectiveIndex(); if i and i>1 then self.objectives[i],self.objectives[i-1]=self.objectives[i-1],self.objectives[i]; self:refreshObjectiveList(); self.objectiveList.selected=i-1 end end
function QPSC_MultiObjectiveWindow:onMoveDown() if self.objectivesLocked then return end; local i=self:selectedObjectiveIndex(); if i and i<#self.objectives then self.objectives[i],self.objectives[i+1]=self.objectives[i+1],self.objectives[i]; self:refreshObjectiveList(); self.objectiveList.selected=i+1 end end

function QPSC_MultiObjectiveWindow:refreshObjectiveList()
    if self.objectiveList.clear then self.objectiveList:clear() else self.objectiveList.items={} end
    for i,o in ipairs(self.objectives) do self.objectiveList:addItem(objectiveText(o,i),o) end
end

function QPSC_MultiObjectiveWindow:refreshSearch()
    local search=string.lower(trim(self.searchEntry:getText()))
    if search==self.lastSearch then return end; self.lastSearch=search
    if self.itemList.clear then self.itemList:clear() else self.itemList.items={} end
    if #search<2 or QPSC_MultiObjectiveUI.itemCatalog==nil then return end
    local matches={}
    for _,entry in ipairs(QPSC_MultiObjectiveUI.itemCatalog) do local rank=itemRank(entry,search); if rank then table.insert(matches,{rank=rank,item=entry}) end end
    table.sort(matches,function(a,b) if a.rank~=b.rank then return a.rank<b.rank end return string.lower(a.item.displayName)<string.lower(b.item.displayName) end)
    for i=1,math.min(#matches,100) do local e=matches[i].item; self.itemList:addItem(e.displayName.." ("..e.fullType..")",e) end
end

function QPSC_MultiObjectiveWindow:update()
    ISPanel.update(self)

    if self.lastLayoutWidth ~= self.width
        or self.lastLayoutHeight ~= self.height then
        self:layoutControls()
    end

    if QPSC_MultiObjectiveUI.itemCatalog==nil then stepCatalog(250) end
    self:refreshSearch()
    self:onModeChanged()
end

function QPSC_MultiObjectiveWindow:onCreate()
    local title=limit(trim(self.titleEntry:getText()),120); if title=="" then self.player:Say(QPSC_I18N.getText("UI_QPSC_MessageTitleRequired")); return end
    if #self.objectives<2 or #self.objectives>MAX_OBJECTIVES then self.player:Say(QPSC_I18N.getText("UI_QPSC_MessageMultiNeedsObjectives")); return end
    local timeText=trim(self.timeEntry:getText()); local time=tonumber(timeText) or 0; if timeText~="" and time<=0 then self.player:Say(QPSC_I18N.getText("UI_QPSC_MessageInvalidTimeLimit")); return end
    local rewardItems=self.rewardItems or {}
    if #rewardItems>QPSC_MO_MAX_REWARD_ITEMS then self.player:Say(QPSC_I18N.getText("UI_QPSC_MessageRewardLimit")); return end
    for _,reward in ipairs(rewardItems) do local quantity=math.floor(tonumber(reward.quantity) or 0); if tostring(reward.fullType or "")=="" or quantity<1 or quantity>100 then self.player:Say(QPSC_I18N.getText("UI_QPSC_MessageInvalidReward")); return end end
    local mode=self:getDef(MODE_DEFS,self.modeCombo).key; local bonusQty=0
    if self.selectedBonusItem and mode~="SHARED_TEAM" then bonusQty=tonumber(self.bonusQtyEntry:getText()) or 0; if bonusQty<1 or bonusQty>100 then self.player:Say(QPSC_I18N.getText("UI_QPSC_MessageInvalidFirstFinisherBonus")); return end end
    local reputationDef=self:getDef(REPUTATION_DEFS,self.reputationCombo)
    local reputationPoints=math.floor(tonumber(self.reputationPointsEntry:getText()) or 0)
    local secondaryReputationDef=self:getDef(REPUTATION_DEFS,self.secondaryReputationCombo)
    local secondaryReputationPoints=math.floor(tonumber(self.secondaryReputationPointsEntry:getText()) or 0)
    if not self.reputationAvailable and not self.isEditMode then
        reputationDef=REPUTATION_DEFS[1]
        reputationPoints=0
        secondaryReputationDef=REPUTATION_DEFS[1]
        secondaryReputationPoints=0
    end
    if reputationDef.key=="" then reputationPoints=0
    elseif reputationPoints<1 or reputationPoints>100000 then self.player:Say(QPSC_I18N.getText("UI_QPSC_MessageInvalidReputationPoints")); return end
    if secondaryReputationDef.key=="" then secondaryReputationPoints=0
    elseif secondaryReputationPoints<1 or secondaryReputationPoints>100000 then self.player:Say(QPSC_I18N.getText("UI_QPSC_MessageInvalidReputationPoints")); return end
    if reputationDef.key~="" and secondaryReputationDef.key~="" and reputationDef.key==secondaryReputationDef.key then self.player:Say(QPSC_I18N.getText("UI_QPSC_MessageReputationPathsDiffer")); return end
    local args={contractId=self.editContract and self.editContract.id or nil,title=title,location=limit(trim(self.locationEntry:getText()),160),description=limit(trim(self.descriptionEntry:getText()),300),category=self:getDef(CATEGORY_DEFS,self.categoryCombo).key,difficulty=self:getDef(DIFFICULTY_DEFS,self.difficultyCombo).key,completionMode=mode,timeLimitText=timeText,timeLimitHours=time,reputationPath=reputationDef.key,reputationPoints=reputationPoints,secondaryReputationPath=secondaryReputationDef.key,secondaryReputationPoints=secondaryReputationPoints,firstFinisherBonusItemFullType=self.selectedBonusItem and self.selectedBonusItem.fullType or "",firstFinisherBonusItemDisplayName=self.selectedBonusItem and self.selectedBonusItem.displayName or "",firstFinisherBonusQuantity=bonusQty,objectiveCount=#self.objectives}
    QPSC_MO_applyRewardArgs(args,rewardItems)
    if args.location=="" then args.location=QPSC_I18N.getText("UI_QPSC_CurrentLocation") end; if args.description=="" then args.description=QPSC_I18N.getText("UI_QPSC_NoDescription") end
    for i,o in ipairs(self.objectives) do local p="objective"..i; args[p.."Id"]=o.id; args[p.."Type"]=o.type; args[p.."Target"]=o.target; args[p.."Radius"]=o.radius; args[p.."ItemFullType"]=o.itemFullType; args[p.."ItemDisplayName"]=o.itemDisplayName; args[p.."TargetX"]=o.targetX; args[p.."TargetY"]=o.targetY; args[p.."TargetZ"]=o.targetZ end
    if self.isEditMode then QPSC_Client.updateMultiObjectiveContract(self.player,args) else QPSC_Client.createMultiObjectiveContract(self.player,args) end
    self:onClose()
end

function QPSC_MultiObjectiveWindow:onClose() self:removeFromUIManager(); if QPSC_MultiObjectiveUI.window==self then QPSC_MultiObjectiveUI.window=nil end end

function QPSC_MultiObjectiveWindow:prerender()
    if self.lastLayoutWidth ~= self.width
        or self.lastLayoutHeight ~= self.height then
        self:layoutControls()
    end

    ISPanel.prerender(self)
    self:drawRect(0,0,self.width,self.height,0.96,0.025,0.025,0.025)
    self:drawRectBorder(0,0,self.width,self.height,0.95,0.82,0.66,0.24)
    self:drawText(
        self.isEditMode
            and QPSC_I18N.getText("UI_QPSC_EditMultiContractTitle")
            or QPSC_I18N.getText("UI_QPSC_CreateMultiContractTitle"),
        18,14,1,1,1,1,UIFont.Medium
    )

    local layout = self.flexLayout or {}
    local rightX = tonumber(layout.rightX) or tonumber(self.layoutRightX) or 510
    local rewardStartY = tonumber(layout.rewardTop) or tonumber(self.layoutRewardStartY) or 570
    local topLabelX = self.layoutTopLabelX or {}
    local labels={
        {"UI_QPSC_Title",18,40},
        {"UI_QPSC_Location",18,90},
        {"UI_QPSC_Description",18,140},
        {"UI_QPSC_Category",topLabelX.category or 18,190},
        {"UI_QPSC_Difficulty",topLabelX.difficulty or 224,190},
        {"UI_QPSC_CompletionMode",topLabelX.completion or 410,190},
        {"UI_QPSC_TimeLimit",topLabelX.time or 636,190},
        {"UI_QPSC_ObjectiveBuilder",topLabelX.objective or 18,252},
        {"UI_QPSC_Target",topLabelX.target or 214,252},
        {"UI_QPSC_Radius",topLabelX.radius or 315,252},
        {"UI_QPSC_SearchItems",18,306},
        {"UI_QPSC_Objectives",rightX,306}
    }

    for _,label in ipairs(labels) do
        self:drawText(
            QPSC_I18N.getText(label[1]),
            label[2],label[3],
            0.82,0.82,0.82,1,UIFont.Small
        )
    end

    local leftWidth = tonumber(layout.leftWidth) or 470
    local rewardTop = tonumber(layout.rewardTop) or rewardStartY
    local bonusTop = tonumber(layout.bonusTop) or (rewardTop + 64)
    local primaryTop = tonumber(layout.primaryTop) or (bonusTop + 64)
    local secondaryTop = tonumber(layout.secondaryTop) or (primaryTop + 64)
    local quantityLabelX = tonumber(layout.quantityLabelX) or 18

    local function drawItemHeader(labelKey, item, y, colorR, colorG, colorB)
        local label = QPSC_I18N.getText(labelKey) .. ":"
        local labelWidth = QPSC_FlexibleLayout.measure(label, UIFont.Small)
        local valueX = 18 + labelWidth + 10
        local valueWidth = math.max(80, 18 + leftWidth - valueX)
        local itemText = item
            and tostring(item.displayName or "")
            or QPSC_I18N.getText("UI_QPSC_None")

        self:drawText(label, 18, y, colorR, colorG, colorB, 1, UIFont.Small)
        self:drawText(
            QPSC_FlexibleLayout.ellipsize(
                itemText,
                UIFont.Small,
                valueWidth
            ),
            valueX,
            y,
            0.90,0.90,0.90,1,UIFont.Small
        )
    end

    drawItemHeader(
        "UI_QPSC_NormalRewards",
        {
            displayName =
                "("
                .. tostring(#(self.rewardItems or {}))
                .. "/"
                .. tostring(QPSC_MO_MAX_REWARD_ITEMS)
                .. ") "
                .. QPSC_MO_rewardSummary(
                    self.rewardItems
                )
        },
        rewardTop,
        0.72,1,0.72
    )
    self:drawText(
        QPSC_I18N.getText("UI_QPSC_Quantity"),
        quantityLabelX,rewardTop+30,0.85,0.85,0.85,1,UIFont.Small
    )

    drawItemHeader(
        "UI_QPSC_FirstFinisherBonus",
        self.selectedBonusItem,
        bonusTop,
        1,0.82,0.38
    )
    self:drawText(
        QPSC_I18N.getText("UI_QPSC_Quantity"),
        quantityLabelX,bonusTop+30,0.85,0.85,0.85,1,UIFont.Small
    )

    local reputationPointsLabelX = tonumber(layout.pointsX)
        or tonumber(self.layoutReputationPointsX)
        or 250
    local primaryPointsY = primaryTop + (layout.primaryWrapped and 22 or 0)
    local secondaryPointsY = secondaryTop + (layout.secondaryWrapped and 22 or 0)

    self:drawText(
        layout.primaryLabel or QPSC_I18N.getText(
            "UI_QPSC_PrimaryReputationReward"
        ),
        18,primaryTop,0.72,0.90,1,1,UIFont.Small
    )
    self:drawText(
        layout.pointsLabel or QPSC_I18N.getText(
            "UI_QPSC_ReputationPoints"
        ),
        reputationPointsLabelX,primaryPointsY,0.85,0.85,0.85,1,UIFont.Small
    )
    self:drawText(
        layout.secondaryLabel or QPSC_I18N.getText(
            "UI_QPSC_SecondaryReputationReward"
        ),
        18,secondaryTop,0.72,0.90,1,1,UIFont.Small
    )
    self:drawText(
        layout.pointsLabel or QPSC_I18N.getText(
            "UI_QPSC_ReputationPoints"
        ),
        reputationPointsLabelX,secondaryPointsY,0.85,0.85,0.85,1,UIFont.Small
    )
    self:drawText(
        QPSC_I18N.getText(
            "UI_QPSC_CapturedPosition",
            self.currentX or 0,
            self.currentY or 0,
            self.currentZ or 0
        ),
        rightX,rewardStartY,0.70,0.88,1,1,UIFont.Small
    )
    self:drawText(
        QPSC_I18N.getText(
            "UI_QPSC_ObjectiveCount",
            #self.objectives,
            MAX_OBJECTIVES
        ),
        rightX,rewardStartY+26,0.92,0.82,0.48,1,UIFont.Small
    )

    if self.objectivesLocked then
        self:drawText(
            QPSC_I18N.getText("UI_QPSC_MultiObjectivesLocked"),
            rightX,rewardStartY+52,0.95,0.68,0.30,1,UIFont.Small
        )
    end

    if self.selectedObjectiveItem then
        self:drawText(
            QPSC_I18N.getText("UI_QPSC_DeliveryObjectiveItem")..": "..
                self.selectedObjectiveItem.displayName,
            rightX,rewardStartY+78,0.75,0.90,1,1,UIFont.Small
        )
    end

    self:drawRect(self.width-22,self.height-5,18,1,0.85,0.82,0.66,0.24)
    self:drawRect(self.width-16,self.height-10,12,1,0.85,0.82,0.66,0.24)
    self:drawRect(self.width-10,self.height-15,6,1,0.85,0.82,0.66,0.24)
end

QPSC_MultiDetailsWindow = ISPanel:derive("QPSC_MultiDetailsWindow")
function QPSC_MultiDetailsWindow:new(x,y,w,h,player,contract) local o=ISPanel:new(x,y,w,h); setmetatable(o,self); self.__index=self; o.player=player; o.contract=contract; o.moveWithMouse=true; return o end
function QPSC_MultiDetailsWindow:initialise() ISPanel.initialise(self); self.closeButton=ISButton:new(self.width-130,self.height-40,110,26,QPSC_I18N.getText("UI_QPSC_Close"),self,QPSC_MultiDetailsWindow.onClose); self.closeButton:initialise(); self.closeButton:instantiate(); self:addChild(self.closeButton) end
function QPSC_MultiDetailsWindow:onClose() self:removeFromUIManager(); if QPSC_MultiObjectiveUI.detailsWindow==self then QPSC_MultiObjectiveUI.detailsWindow=nil end end
function QPSC_MultiDetailsWindow:prerender()
    ISPanel.prerender(self); self:drawRect(0,0,self.width,self.height,0.96,0.025,0.025,0.025); self:drawRectBorder(0,0,self.width,self.height,0.95,0.82,0.66,0.24)
    self:drawText(QPSC_I18N.getText("UI_QPSC_ObjectiveDetails").." — #"..tostring(self.contract.id or "?"),18,14,1,1,1,1,UIFont.Medium)
    local username=self.player and self.player.getUsername and tostring(self.player:getUsername()) or ""; local participant=nil
    for _,p in ipairs(self.contract.participants or {}) do if string.lower(tostring(p.username or ""))==string.lower(username) then participant=p; break end end
    local y=54
    for i,o in ipairs(self.contract.objectives or {}) do
        local progress=0; local id=tostring(o.id or "")
        if tostring(self.contract.completionMode or "") == "SHARED_TEAM" then progress=tonumber((self.contract.sharedObjectiveProgress or {})[id]) or 0
        elseif participant then progress=tonumber((participant.objectiveProgress or {})[id]) or 0 end
        local target=math.max(1,tonumber(o.target) or 1); local mark=progress>=target and "[X] " or "[ ] "
        self:drawText(mark..objectiveText(o,i),22,y,progress>=target and 0.55 or 0.9,progress>=target and 1 or 0.9,progress>=target and 0.55 or 0.9,1,UIFont.Small)
        self:drawText(tostring(progress).." / "..tostring(target),self.width-110,y,0.72,0.90,1,1,UIFont.Small); y=y+34
    end
    if tostring(self.contract.completionMode or "") == "SHARED_TEAM" and participant then
        local total=0; for _,v in pairs(participant.objectiveContributions or {}) do total=total+(tonumber(v) or 0) end
        self:drawText(
            QPSC_I18N.getText("UI_QPSC_YourContribution", total),
            22,
            y + 10,
            0.92,
            0.82,
            0.48,
            1,
            UIFont.Small
        )
    end
end

local function openEditor(player, categoryKey, contract)
    if QPSC_MultiObjectiveUI.window then QPSC_MultiObjectiveUI.window:removeFromUIManager() end
    local sw,sh=getCore():getScreenWidth(),getCore():getScreenHeight(); local w=math.min(1080,math.max(MIN_WIDTH,sw-60)); local h=math.min(900,math.max(MIN_HEIGHT,sh-60)); w=math.min(w,sw-10); h=math.min(h,sh-10)
    local window=QPSC_MultiObjectiveWindow:new((sw-w)/2,(sh-h)/2,w,h,player,categoryKey,contract); window:initialise(); window:addToUIManager(); QPSC_MultiObjectiveUI.window=window
end
function QPSC_MultiObjectiveUI.open(player, categoryKey) openEditor(player,categoryKey,nil) end
function QPSC_MultiObjectiveUI.openEdit(player, contract) openEditor(player,tostring(contract.category or "NONE"),contract) end
function QPSC_MultiObjectiveUI.openDetails(player, contract)
    if contract==nil or contract.multiObjective~=true then return end
    if QPSC_MultiObjectiveUI.detailsWindow then QPSC_MultiObjectiveUI.detailsWindow:removeFromUIManager() end
    local sw,sh=getCore():getScreenWidth(),getCore():getScreenHeight(); local w=760; local h=math.min(420,150+(#(contract.objectives or {})*38)); local window=QPSC_MultiDetailsWindow:new((sw-w)/2,(sh-h)/2,w,h,player,contract); window:initialise(); window:addToUIManager(); QPSC_MultiObjectiveUI.detailsWindow=window
end

return QPSC_MultiObjectiveUI
