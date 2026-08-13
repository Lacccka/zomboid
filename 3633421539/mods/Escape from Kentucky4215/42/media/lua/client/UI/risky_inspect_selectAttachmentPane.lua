-- SELECT ATTACHMENT PANE
selectAttachmentPane = ISPanel:derive("selectAttachmentPane")
function selectAttachmentPane:new(x, y, category, ClipType, AttackModeType, SkinType)
    local o = {}
    o = ISPanel:new(x, y, 40 * 5 + 20, 126);
    setmetatable(o, self)
    self.__index = self
    o.ClipType = ClipType
    o.AttackModeType = AttackModeType
    o.SkinType = SkinType
    o.category = category
    o.backgroundColor = {
        r = 0,
        g = 0,
        b = 0,
        a = 1
    };
    o.borderColor = {
        r = 0.9,
        g = 0.9,
        b = 0.9,
        a = 0.7
    };
    o.currentPrimaryItem = getPlayer():getPrimaryHandItem()
    o.elements = {}
    if (riskyShowPotentialAttachment) then
        o.potentialAttachment = {}
        local items = getAllItems();
        for i = 0, items:size() - 1 do
            local item = items:get(i);
            if item and not item:getObsolete() and not item:isHidden() and item:getItemType() and item:getItemType():toString() == "base:weaponpart" then
                o.potentialAttachment[item:getFullName()] = 1
            end
        end
    end
    return o
end
function selectAttachmentPane:prerender()
    self:setStencilRect(0, 0, self.width, self.height);
    self:drawRect(-self:getXScroll(), -self:getYScroll(), self.width, self.height, self.backgroundColor.a,
        self.backgroundColor.r, self.backgroundColor.g, self.backgroundColor.b);
end
function selectAttachmentPane:render()
    self:clearStencilRect();
    self:drawRectBorder(-self:getXScroll(), -self:getYScroll(), self.width, self.height, self.borderColor.a,
        self.borderColor.r, self.borderColor.g, self.borderColor.b);
end
function selectAttachmentPane:createChildren()
    self:addScrollBars(false)
    self:setScrollWithParent(false)
    self:setScrollChildren(true)
end
function selectAttachmentPane:onMouseWheel(del)
    self:setYScroll(self:getYScroll() - (del * 42))
    return true;
end
function selectAttachmentPane:update()
    if self and self:getIsVisible() then
        if (self.currentPrimaryItem ~= getPlayer():getPrimaryHandItem() or riskyInspectWindow == nil) then
            self:close()
        end
        if self.itemCap ~= getPlayer():getInventory():getItems():size() or self.itemWeight ~=
            getPlayer():getInventory():getCapacityWeight() then
            self.itemCap = getPlayer():getInventory():getItems():size()
            self.itemWeight = getPlayer():getInventory():getCapacityWeight()
            if (#self.elements ~= 0) then
                for i = 1, #self.elements do
                    self:removeChild(self.elements[i])
                end
            end
            self:renderInventory()
        end
    end
end
function selectAttachmentPane:renderInventory()
    local weapon = getPlayer():getPrimaryHandItem()
    if getPlayer():getPrimaryHandItem() ~= nil and getPlayer():getPrimaryHandItem():IsWeapon() then
        local weaponParts = getPlayer():getInventory():getItemsFromCategory("WeaponPart");
        local alreadyDoneList = {};
        local itemNum = 0
        local rowCount = -1
        for i = 0, weaponParts:size() - 1 do
            local part = weaponParts:get(i);
            if part:getMountOn():contains(weapon:getFullType()) and not alreadyDoneList[part:getName()] then
                if (part:getPartType() == self.category) then
                    alreadyDoneList[part:getName()] = true;
                    if (math.fmod(itemNum, 5) == 0) then
                        rowCount = rowCount + 1
                    end
                    local x = 2 + 41 * math.fmod(itemNum, 5)
                    local y = 2 + 41 * rowCount
                    if riskyShowPotentialAttachment then
                        self.potentialAttachment[part:getFullType()] = nil
                    end
                    local item = addAttachmentButton:new(x, y, 40, 40, part, weapon, true, "WeaponPart")
                    table.insert(self.elements, item)
                    item:bringToTop()
                    self:addChild(item)
                    itemNum = itemNum + 1
                end
            end
        end
        if self.ClipType == "ClipType" then
            if AWCWF_WeaponMagazineType[weapon:getType()] then
                for j = 1, #AWCWF_WeaponMagazineType[weapon:getType()] do
                    local TempPart = instanceItem(AWCWF_WeaponMagazineType[weapon:getType()][j]);
                    if not alreadyDoneList[TempPart] then
                        if (math.fmod(itemNum, 5) == 0) then
                            rowCount = rowCount + 1
                        end
                        local x = 2 + 41 * math.fmod(itemNum, 5)
                        local y = 2 + 41 * rowCount
                        item = addAttachmentButton:new(x, y, 40, 40, TempPart, weapon, true, "ClipType")
                        item:bringToTop()
                        self:addChild(item)
                        x = x + 41
                    end
                end
            end
        end
        if self.SkinType == "Skin" then
            if CatWeaponSkin[weapon:getType()] then
                local SkinTableNow = CatWeaponSkin[weapon:getType()]
                for j = 1, #SkinTableNow do
                    if (math.fmod(itemNum, 5) == 0) then
                        rowCount = rowCount + 1
                    end
                    local x = 2 + 41 * math.fmod(itemNum, 5)
                    local y = 2 + 41 * rowCount
                    local SkinItem = instanceItem("Gunpart." .. SkinTableNow[j]);
                    if getPlayer():isRecipeKnown(SkinTableNow[j]) or j == 1 then
                        item = addAttachmentButton:new(x, y, 40, 40, SkinItem, weapon, true, "Skin")
                    else
                        item = addAttachmentButton:new(x, y, 40, 40, SkinItem, weapon, false, "Skin")
                    end
                    item:bringToTop()
                    self:addChild(item)
                    x = x + 41
                end
            end
        end
        if self.AttackModeType == "WeaponAttackType" then
            if AWCWF_WeaponAttackType[weapon:getType()] then
                local AttackTableNow = AWCWF_WeaponAttackType[weapon:getType()]
                for j = 1, #AttackTableNow do
                    local AttackTypeItem = instanceItem(AttackTableNow[j]);
                    local needflag = false
                    local CreatFlag = true
                    if string.find(weapon:getType(), "Brynhild") then
                        needflag = true
                    end
                    if needflag then
                        if not getPlayer():getInventory():contains(AttackTableNow[j]) then
                            CreatFlag = false
                        end
                    end
                    if (math.fmod(itemNum, 5) == 0) then
                        rowCount = rowCount + 1
                    end
                    local x = 2 + 41 * math.fmod(itemNum, 5)
                    local y = 2 + 41 * rowCount
                    if AttackTypeItem and CreatFlag then
                        item = addAttachmentButton:new(x, y, 40, 40, AttackTypeItem, weapon, true, "WeaponAttackType")
                        item:bringToTop()
                        self:addChild(item)
                        x = x + 41
                    end
                end
            end
        end
        if riskyShowPotentialAttachment then
            for k, v in pairs(self.potentialAttachment) do
                local potentialPart = instanceItem(k)
                if potentialPart:getMountOn():contains(weapon:getFullType()) and potentialPart:getPartType() ==
                    self.category then
                    if (math.fmod(itemNum, 5) == 0) then
                        rowCount = rowCount + 1
                    end
                    local x = 2 + 41 * math.fmod(itemNum, 5)
                    local y = 2 + 41 * rowCount
                    local item = addAttachmentButton:new(x, y, 40, 40, potentialPart, weapon, false, "WeaponPart")
                    table.insert(self.elements, item)
                    item:bringToTop()
                    self:addChild(item)
                    itemNum = itemNum + 1
                end
            end
        end
        self:setScrollHeight(42 * (rowCount + 1))
        if (self:getHeight() >= self:getScrollHeight()) then
            self:setWidth(40 * 5 + 8)
        end
    end
end
function selectAttachmentPane:close()
    self:setVisible(false)
end
function selectAttachmentPane:onMouseDownOutside(x, y)
    if self:getIsVisible() and not self.vscroll:isMouseOver() then
        self:close()
    end
end
-- SELECT MAGAZINE PANE
