require "ISUI/ISUIElement"
require "ISUI/ISPanel"
require "ISUI/ISInventoryPaneContextMenu"

-- 로드 자체가 안 되는지(경로/모드 적용/설치 문제)부터 확인하기 위한 1회 로그
if _G.__EFZ_TETRIS_SSR_PATCH_FILE_LOADED then
    return
end
_G.__EFZ_TETRIS_SSR_PATCH_FILE_LOADED = true
print("[EFZ_Tetris_SSR_Patch] Loaded EFZ_Tetris_SSR_Patch.lua")

local function tryRequire(path)
    local ok, mod = pcall(require, path)
    if ok then
        return mod
    end
    return nil
end

local function isInventoryTetrisActive()
    local mods = getActivatedMods()
    return mods and mods:contains("INVENTORY_TETRIS")
end

local function isSsrQuestsActive()
    local mods = getActivatedMods()
    return mods and (mods:contains("ssr-quests") or mods:contains("ssr_quests") or mods:contains("SSR_QUESTS"))
end

local ItemGridContainerUI = nil
local OPT = nil

local function debugPrint(str)
    print("[EFZ_Tetris_SSR_Patch] " .. str)
end

-- SSR Quest System(QItemSpawner)은 다른 모드에서 늦게 로드될 수 있으므로
-- "패치 적용"과 "아이템 표시"를 분리해서, QItemSpawner가 없어도 패치는 먼저 적용되도록 한다.

local SSROverlayRenderer = ISUIElement:derive("SSROverlayRenderer")

function SSROverlayRenderer:new(x, y, width, height, containerUi)
    local o = ISUIElement:new(x, y, width, height)
    setmetatable(o, self)
    self.__index = self
    o.containerUi = containerUi
    o.items = {}
    o.iconSize = 32
    o.padding = 4
    o.headerHeight = 20 -- 헤더 텍스트 공간
    return o
end

function SSROverlayRenderer:initialise()
    ISUIElement.initialise(self)
end

function SSROverlayRenderer:prerender()
    if #self.items == 0 then return end
    
    -- 배경 그리기 (약간의 금색 틴트가 들어간 어두운 배경)
    self:drawRect(0, 0, self.width, self.height, 0.8, 0.1, 0.1, 0)
    -- 테두리 강조 (노란색/금색)
    self:drawRectBorder(0, 0, self.width, self.height, 0.8, 1, 0.8, 0)
    
    -- "QUEST ITEMS" 헤더 텍스트
    self:drawText("QUEST ITEMS", self.padding, 2, 1, 0.8, 0, 1, UIFont.Small)
    
    local x = self.padding
    local y = self.padding + self.headerHeight
    
    for _, item in ipairs(self.items) do
        local tex = item:getTex()
        if tex then
            self:drawTextureScaled(tex, x, y, self.iconSize, self.iconSize, 1, 1, 1, 1)
        end
        
        x = x + self.iconSize + self.padding
        if x > self.width - self.iconSize then
            x = self.padding
            y = y + self.iconSize + self.padding
        end
    end
end

function SSROverlayRenderer:onMouseDown(x, y)
    return true -- 이벤트 전파 차단
end

function SSROverlayRenderer:onMouseUp(x, y)
    local item = self:findItemAt(x, y)
    if item then
        -- 클릭 시 획득 로직은 더블클릭이나 우클릭 메뉴에서 처리
    end
    return true
end

function SSROverlayRenderer:onRightMouseUp(x, y)
    local item = self:findItemAt(x, y)
    if item and self.containerUi then
        local playerNum = self.containerUi.playerNum or 0
        local mx = getMouseX()
        local my = getMouseY()
        local context = ISContextMenu.get(playerNum, mx, my)

        context:addOption(getText("ContextMenu_Grab"), self.containerUi, function()
            if self.containerUi and self.containerUi._efz_grabSsrQuestItem then
                self.containerUi:_efz_grabSsrQuestItem(item)
            end
        end)

        -- SSR 퀘스트 아이템 정보 표시(디버그용)
        context:addOption(item:getName(), nil, nil)
    end
    return true
end

function SSROverlayRenderer:onMouseMove(dx, dy)
    self.mouseOverItem = self:findItemAt(self:getMouseX(), self:getMouseY())
    return true
end

function SSROverlayRenderer:onMouseMoveOutside(dx, dy)
    self.mouseOverItem = nil
end

function SSROverlayRenderer:findItemAt(x, y)
    if #self.items == 0 then return nil end
    
    -- 헤더 영역 제외
    if y < self.headerHeight then return nil end
    
    local effectiveY = y - self.headerHeight
    local col = math.floor((x - self.padding) / (self.iconSize + self.padding))
    local row = math.floor((effectiveY - self.padding) / (self.iconSize + self.padding))
    
    local itemsPerRow = math.floor((self.width - self.padding) / (self.iconSize + self.padding))
    local index = row * itemsPerRow + col + 1
    
    if index >= 1 and index <= #self.items then
        local itemX = (col * (self.iconSize + self.padding)) + self.padding
        local itemY = (row * (self.iconSize + self.padding)) + self.padding + self.headerHeight
        
        if x >= itemX and x < itemX + self.iconSize and y >= itemY and y < itemY + self.iconSize then
            return self.items[index]
        end
    end
    return nil
end

function SSROverlayRenderer:onMouseDoubleClick(x, y)
    local item = self:findItemAt(x, y)
    if item then
        debugPrint("Double clicked SSR item: " .. tostring(item:getName()))
        if self.containerUi and self.containerUi._efz_grabSsrQuestItem then
            self.containerUi:_efz_grabSsrQuestItem(item)
        end
    end
end

function SSROverlayRenderer:update()
    if self:isMouseOver() then
        local item = self:findItemAt(self:getMouseX(), self:getMouseY())
        if item and self.containerUi.inventoryPane and self.containerUi.inventoryPane.doTooltipForItem then
            self.containerUi.inventoryPane:doTooltipForItem(item)
        end
    end
end

-- 패치 적용 로직
local function applyPatch()
    -- 이벤트가 실제로 호출되는지 확인하기 위한 1회 로그
    if not _G.__EFZ_TETRIS_SSR_PATCH_APPLY_LOGGED then
        _G.__EFZ_TETRIS_SSR_PATCH_APPLY_LOGGED = true
        debugPrint("applyPatch() called. INVENTORY_TETRIS active=" .. tostring(isInventoryTetrisActive())
            .. " ssr-quests active=" .. tostring(isSsrQuestsActive())
            .. " QItemSpawner=" .. tostring(QItemSpawner ~= nil))
    end

    -- SSR_Quests의 QItemSpawner가 아직 로드되지 않은 환경도 있어서, 설치되어 있으면 한 번만 로드 시도.
    if isSsrQuestsActive() and not QItemSpawner and not _G.__EFZ_SSR_ITEMSPAWNER_REQUIRED then
        _G.__EFZ_SSR_ITEMSPAWNER_REQUIRED = true
        tryRequire("Scripting/ItemSpawner")
    end
    
    if not ItemGridContainerUI then
        -- b42.13 경로 우선, 구버전 경로는 fallback
        ItemGridContainerUI =
            tryRequire("InventoryTetris/UI/Container/ItemGridContainerUI") or
            tryRequire("InventoryTetris/ItemGrid/UI/Container/ItemGridContainerUI")
        if not ItemGridContainerUI then
            -- InventoryTetris가 로드되지 않았으면 조용히 종료 (getActivatedMods()는 의존성 로드 케이스에서 false일 수 있음)
            debugPrint("ItemGridContainerUI not found. InventoryTetris may not be loaded/enabled yet.")
            return
        end
    end

    if not OPT then
        OPT = tryRequire("InventoryTetris/Settings")
    end

    if ItemGridContainerUI.ssrPatchApplied then
        debugPrint("Patch already applied.")
        return
    end

    debugPrint("Applying ItemGridContainerUI hooks (b42 compatible)...")

    local function wipeArray(t)
        for i = #t, 1, -1 do
            t[i] = nil
        end
    end

    ---@return string[]|nil
    local function getQidsForContainerUi(containerUi)
        if not QItemSpawner or not QItemSpawner.containers then
            return nil
        end

        -- SSR_Quests의 refreshBackpacks() 로직과 동일하게:
        -- containerUi.inventory(아이콘에 매달린 inventory) -> parent:getContainer() 를 기준으로 type/x/y/z를 잡는다.
        local function getContainerSignature(inv)
            if not inv then
                return nil
            end
            local parent = inv.getParent and inv:getParent() or nil
            local container = (parent and parent.getContainer and parent:getContainer()) or inv

            local cParent = container.getParent and container:getParent() or parent
            local square = cParent and cParent.getSquare and cParent:getSquare() or nil
            if not square then
                return nil
            end

            return {
                type = container:getType(),
                x = square:getX(),
                y = square:getY(),
                z = square:getZ()
            }
        end

        local sig = getContainerSignature(containerUi.inventory)
        if not sig then
            return nil
        end

        -- 1) inventoryPane.qid가 있으면, 그 qid들 중 "현재 컨테이너"와 실제로 매칭되는 것만 걸러 사용
        if containerUi.inventoryPane and containerUi.inventoryPane.qid then
            local filtered = nil
            for i = 1, #containerUi.inventoryPane.qid do
                local qid = containerUi.inventoryPane.qid[i]
                local c = QItemSpawner.containers[qid]
                if c and c.type == sig.type and c.x == sig.x and c.y == sig.y and c.z == sig.z then
                    if not filtered then filtered = {} end
                    filtered[#filtered + 1] = qid
                end
            end
            if filtered and #filtered > 0 then
                return filtered
            end
        end

        -- 2) fallback: QItemSpawner.containers 전체에서 현재 컨테이너(type/x/y/z)와 매칭되는 qid를 직접 검색
        local qids = nil
        for key, value in pairs(QItemSpawner.containers) do
            if value and value.type == sig.type and value.x == sig.x and value.y == sig.y and value.z == sig.z then
                if not qids then qids = {} end
                qids[#qids + 1] = key
            end
        end
        return qids
    end

    local og_initialise = ItemGridContainerUI.initialise
    function ItemGridContainerUI:initialise()
        og_initialise(self)
        
        if not self.ssrRenderer then
            self.ssrRenderer = SSROverlayRenderer:new(0, 0, 100, 30, self)
            self.ssrRenderer:initialise()
            self.ssrRenderer:setVisible(false)
            self:addChild(self.ssrRenderer)
            debugPrint("Initialized SSROverlayRenderer for container")
        end

        -- SSR 아이템을 여러 qid에서 합쳐 표시해야 할 수 있어(SSR_Quests는 qid 배열을 쓴다)
        if not self._efz_ssrCombinedItems then
            self._efz_ssrCombinedItems = {}
        end
        self._efz_ssrSig = nil
    end

    -- InventoryTetris는 ISInventoryPaneContextMenu.onGrabItems를 "실제 컨테이너에 든 아이템" 기준으로 패치해서,
    -- SSR_Quests의 virtual 아이템(item:getContainer()==nil)은 Grab이 무시되는 경우가 있음.
    -- 그래서 SSR 쪽 로직(QItemSpawner/QItemFactory)로 직접 "획득" 처리한다.
    function ItemGridContainerUI:_efz_grabSsrQuestItem(item)
        if not item then return end

        -- SSR 모듈이 아직 로드 안 된 경우 대비
        if not QItemSpawner and isSsrQuestsActive() then
            tryRequire("Scripting/ItemSpawner")
        end
        if not QItemFactory and isSsrQuestsActive() then
            tryRequire("Communications/QItemFactory")
        end

        local md = item.getModData and item:getModData() or nil
        if not md or not md.virtual or not md.qid then
            -- 가상 아이템이 아니면 기존 grab 로직에 맡김(가능하면)
            if ISInventoryPaneContextMenu and ISInventoryPaneContextMenu.onGrabItems then
                ISInventoryPaneContextMenu.onGrabItems({ item }, self.playerNum or 0)
            end
            return
        end

        if not QItemSpawner or not QItemSpawner.containers then
            return
        end

        local qid = md.qid
        local c = QItemSpawner.containers[qid]
        if not c or not c.items then
            return
        end

        local playerNum = self.playerNum or 0
        local playerObj = getSpecificPlayer(playerNum) or getPlayer()
        if not playerObj then
            return
        end

        local pm = playerObj:getModData()
        if type(pm.ispwn) ~= "table" then
            pm.ispwn = {}
        end
        if type(pm.ispwn[qid]) ~= "table" then
            pm.ispwn[qid] = {}
        end

        -- 지급할 실제 아이템 목록(단일 아이템)
        local fullName = tostring(item:getModule()) .. "." .. tostring(item:getType())
        local list = {}
        if QItemFactory and QItemFactory.createEntry then
            list[1] = QItemFactory.createEntry(fullName, 1)
        else
            list[1] = { name = fullName, amount = 1 }
        end

        -- SSR의 컨테이너 items에서 제거 + 진행도(extdata) 반영
        local single = md.single == true
        for i = #c.items, 1, -1 do
            local it = c.items[i]
            if it and (single or it == item) then
                local imd = it:getModData()
                -- idx 기록(SSR trim 로직과 호환)
                local idx = (imd and imd.idx) or md.idx
                if idx ~= nil then
                    pm.ispwn[qid][#pm.ispwn[qid] + 1] = idx
                end
                if imd then
                    imd.virtual = false
                end
                table.remove(c.items, i)
                if not single then
                    break
                end
            end
        end
        if c.task then
            c.task.extdata = pm.ispwn[qid]
        end

        if SaveManager and SaveManager.onQuestDataChange then
            SaveManager.onQuestDataChange()
        end
        if SaveManager and SaveManager.save then
            SaveManager.save()
        end

        if QItemFactory and QItemFactory.request then
            QItemFactory.request("LootContainer", list)
        end

        -- UI 갱신
        self._efz_ssrSig = nil
        local gs = self._efz_lastGridScale or (OPT and OPT.SCALE) or 1
        local is = self._efz_lastInfoScale or (OPT and OPT.CONTAINER_INFO_SCALE) or 1
        self:applyScales(gs, is)
        if self.inventoryPane and self.inventoryPane.refreshContainer then
            self.inventoryPane:refreshContainer()
        end
    end

    local og_applyScales = ItemGridContainerUI.applyScales
    function ItemGridContainerUI:applyScales(gridScale, infoScale)
        -- prerender에서 다시 레이아웃을 요청할 때 사용할 마지막 스케일 저장
        self._efz_lastGridScale = gridScale
        self._efz_lastInfoScale = infoScale

        og_applyScales(self, gridScale, infoScale)
        
        if not self.ssrRenderer then return end

        -- b42.13: isCollapsed/gridRenderer가 아니라 isGridCollapsed/multiGridRenderer 구조
        if self.isGridCollapsed then
            -- 접힘 상태에서는 오버레이도 숨김
            if self.ssrRenderer:isVisible() then
                self.ssrRenderer:setVisible(false)
                self.ssrRenderer.items = {}
            end
            return
        end

        if not self.ssrRenderer:isVisible() then
            return
        end

        if not self.multiGridRenderer then
            return
        end

        -- 오버레이는 "그리드 영역" 위에만 표시 (infoRenderer 영역은 그대로 유지)
        local baseX = self.multiGridRenderer:getX()
        local baseY = self.multiGridRenderer:getY()
        local width = self.multiGridRenderer:getWidth()

        self.ssrRenderer:setX(baseX)
        self.ssrRenderer:setY(baseY)
        self.ssrRenderer:setWidth(width)

        local iconSize = self.ssrRenderer.iconSize or 32
        local padding = self.ssrRenderer.padding or 4
        local headerHeight = self.ssrRenderer.headerHeight or 20
        local itemsCount = (self.ssrRenderer.items and #self.ssrRenderer.items) or 0

        local itemsPerRow = math.floor((width - padding) / (iconSize + padding))
        if itemsPerRow < 1 then itemsPerRow = 1 end
        local rows = math.ceil(itemsCount / itemsPerRow)
        local height = rows * (iconSize + padding) + padding + headerHeight
        if height < (padding + headerHeight) then
            height = padding + headerHeight
        end
        self.ssrRenderer:setHeight(height)

        local delta = height + 5

        -- 그리드/오버플로우 렌더러를 아래로 밀어 공간 확보
        self.multiGridRenderer:setY(baseY + delta)
        if self.overflowRenderer then
            self.overflowRenderer:setY(baseY + delta)
        end

        -- 컨테이너 높이 재계산(스크롤/레이아웃 깨짐 방지)
        local infoBottom = 0
        if self.infoRenderer then
            infoBottom = self.infoRenderer:getY() + self.infoRenderer:getHeight()
        end
        local gridBottom = self.multiGridRenderer:getY() + self.multiGridRenderer:getHeight()
        local overflowBottom = 0
        if self.overflowRenderer then
            overflowBottom = self.overflowRenderer:getY() + self.overflowRenderer:getHeight()
        end
        self:setHeight(math.max(infoBottom, gridBottom, overflowBottom))

        -- Z-order 상단 보장
        self.ssrRenderer:bringToTop()
    end

    local og_prerender = ItemGridContainerUI.prerender
    function ItemGridContainerUI:prerender()
        og_prerender(self)
        
        if not self.ssrRenderer or not self.inventory then
            return
        end

        -- 접힘 상태면 표시하지 않음
        if self.isGridCollapsed then
            if self.ssrRenderer:isVisible() then
                self.ssrRenderer:setVisible(false)
                self.ssrRenderer.items = {}
            end
            return
        end

        local qids = getQidsForContainerUi(self)

        -- SSR 시스템이 아직 로드되지 않았거나, 해당 컨테이너에 퀘스트 아이템(qid)이 없으면 숨김
        if not qids or #qids == 0 then
            if self.ssrRenderer:isVisible() then
                self.ssrRenderer:setVisible(false)
                self.ssrRenderer.items = {}

                local gs = self._efz_lastGridScale or (OPT and OPT.SCALE) or 1
                local is = self._efz_lastInfoScale or (OPT and OPT.CONTAINER_INFO_SCALE) or 1
                self:applyScales(gs, is)
                if self.inventoryPane and self.inventoryPane.refreshContainer then
                    self.inventoryPane:refreshContainer()
                end
            end
            return
        end

        -- qid(여러 개 가능)들의 가상 아이템을 합쳐서 오버레이에 표시
        local combined = self._efz_ssrCombinedItems or {}
        wipeArray(combined)

        local parts = {}
        local total = 0
        for i = 1, #qids do
            local qid = qids[i]
            local c = QItemSpawner and QItemSpawner.containers and QItemSpawner.containers[qid]
            local items = c and c.items
            local count = (items and #items) or 0
            if count > 0 then
                parts[#parts + 1] = tostring(qid) .. ":" .. tostring(count)
                for j = 1, count do
                    total = total + 1
                    combined[total] = items[j]
                end
            end
        end

        local sig = (#parts > 0) and table.concat(parts, ";") or nil

        local wasVisible = self.ssrRenderer:isVisible()
        local changed = (self._efz_ssrSig ~= sig)

        if total > 0 then
            self.ssrRenderer.items = combined
            if not wasVisible then
                self.ssrRenderer:setVisible(true)
                changed = true
            end
        else
            if wasVisible then
                self.ssrRenderer:setVisible(false)
                self.ssrRenderer.items = {}
                changed = true
            end
        end

        self._efz_ssrSig = sig

        if changed then
            debugPrint("SSR overlay update: total=" .. tostring(total) .. " sig=" .. tostring(sig))
            local gs = self._efz_lastGridScale or (OPT and OPT.SCALE) or 1
            local is = self._efz_lastInfoScale or (OPT and OPT.CONTAINER_INFO_SCALE) or 1
            self:applyScales(gs, is)
            if self.inventoryPane and self.inventoryPane.refreshContainer then
                self.inventoryPane:refreshContainer()
            end
        end
    end

    -- InventoryTetris는 더블클릭을 자식에게 전달하지 않고 containerUi:onMouseDoubleClick로만 라우팅한다.
    -- 오버레이 위에서의 더블클릭/우클릭을 강제로 오버레이로 전달한다.
    local og_onMouseDoubleClick = ItemGridContainerUI.onMouseDoubleClick
    function ItemGridContainerUI:onMouseDoubleClick(x, y)
        if self.ssrRenderer and self.ssrRenderer:isVisible() and self.ssrRenderer:isMouseOver() then
            return self.ssrRenderer:onMouseDoubleClick(self.ssrRenderer:getMouseX(), self.ssrRenderer:getMouseY())
        end
        return og_onMouseDoubleClick(self, x, y)
    end

    local og_onRightMouseUp = ItemGridContainerUI.onRightMouseUp
    function ItemGridContainerUI:onRightMouseUp(x, y)
        if self.ssrRenderer and self.ssrRenderer:isVisible() and self.ssrRenderer:isMouseOver() then
            return self.ssrRenderer:onRightMouseUp(self.ssrRenderer:getMouseX(), self.ssrRenderer:getMouseY())
        end
        return og_onRightMouseUp(self, x, y)
    end

    -- 일부 환경에서는 우클릭이 ISInventoryPane 쪽에서 소비되어 child까지 안 내려오는 경우가 있어, 한 번 더 보강
    if ISInventoryPane and not _G.__EFZ_TETRIS_SSR_PATCHED_PANE_RIGHTCLICK then
        _G.__EFZ_TETRIS_SSR_PATCHED_PANE_RIGHTCLICK = true
        local og_pane_onRightMouseUp = ISInventoryPane.onRightMouseUp
        function ISInventoryPane:onRightMouseUp(x, y)
            -- InventoryTetris grid 모드에서만 처리
            if self.mode == "grid" and self.findContainerGridUiUnderMouse then
                local containerUi = self:findContainerGridUiUnderMouse()
                if containerUi and containerUi.ssrRenderer and containerUi.ssrRenderer:isVisible() and containerUi.ssrRenderer:isMouseOver() then
                    return containerUi.ssrRenderer:onRightMouseUp(containerUi.ssrRenderer:getMouseX(), containerUi.ssrRenderer:getMouseY())
                end
            end
            return og_pane_onRightMouseUp(self, x, y)
        end
    end
    
    ItemGridContainerUI.ssrPatchApplied = true
    debugPrint("Patch applied successfully.")
end

-- 로드 순서/타이밍 이슈 대비: 여러 이벤트에 걸어두되, ssrPatchApplied로 중복 적용은 방지됨
Events.OnGameBoot.Add(applyPatch)
Events.OnCreatePlayer.Add(applyPatch)
Events.OnGameStart.Add(applyPatch)
