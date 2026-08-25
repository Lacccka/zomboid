require "ISUI/ISPanelJoypad"

SF_MissionPanel = ISPanelJoypad:derive("SF_MissionPanel");

SF_MissionPanel.Category1Label = nil;
SF_MissionPanel.Category2Label = nil;

local FONT_HGT_SMALL = getTextManager():getFontHeight(UIFont.Small);
local FONT_HGT_MEDIUM = getTextManager():getFontHeight(UIFont.Medium);

SF_MissionPanel.Commands = {};

--------------------------------------------------------------------------------------------------------
-- Local functions, usually used to check certain items' conditions

local function predicateBigFish(item)
	local fullname =  item:getName():gsub(" ", "");
	print("SOUL QUEST SYSTEM - Fish name was: " .. fullname);
	local nameTable = luautils.split(fullname, "-");
	if not nameTable[2] then return false end
	local lengthstr = luautils.split(nameTable[2], "c")[1];
	print("SOUL QUEST SYSTEM - Fish length was: " .. lengthstr);
	local length = tonumber(lengthstr);
	return item:isFresh() and length and length >= 50
end

local function predicateCondition(item, condition)
	local percent = condition / 100;
	local matchcondition = item:getConditionMax() * percent;
	return instanceof(item, "HandWeapon") and item:getCondition() >= matchcondition
end

local function predicateFreshFood(item)
	return item:isFresh()
end

local function predicateFullDrainable(item)
	return item:getUsedDelta() == 1
end

--------------------------------------------------------------------------------------------------------
-- Commands

function SF_MissionPanel.Commands.actionevent(condition, commandslist)
	local player = getPlayer();
	local convertedcondition = condition:gsub(":", ";");
	local checkcondition = luautils.split(convertedcondition, ";");
	if checkcondition[1] == "killzombies" then
		local currentkills = player:getZombieKills();
		local goal = currentkills + tonumber(checkcondition[2]);
		convertedcondition = checkcondition[1] .. ";" .. tostring(goal);
	end
	local convertedlist = commandslist:gsub(":", ";");
	table.insert(player:getModData().missionProgress.ActionEvent, {condition = convertedcondition, commands = convertedlist});
	SF_MissionPanel.instance.needsBackup = true;
end

function SF_MissionPanel.Commands.additem(item, quantity)
	local inv = getPlayer():getInventory();
	inv:AddItems(item, quantity);
end

function SF_MissionPanel.Commands.addmannequin(squaretag)
	if SFQuest_Database.MannequinPool[squaretag] then
		SFQuest_Database.MannequinPool[squaretag].removed = nil;
	end
end

function SF_MissionPanel.Commands.clickevent(squareaddress, actiondata, commands)
	local player = getPlayer();
	local squareTable = luautils.split(squareaddress, ":");
	local convertedaction = actiondata:gsub(":", ";");
	local convertedlist = commands:gsub(":", ";");	
	table.insert(player:getModData().missionProgress.ClickEvent, {square = squareTable[1], address = squareTable[2], actiondata = convertedaction, commands = convertedlist});
	SF_MissionPanel.instance.needsBackup = true;
end

function SF_MissionPanel.Commands.playersay(entry)
	local player = getPlayer();
	local text = getText(entry);
	player:Say(text);
end

function SF_MissionPanel.Commands.randomcodedworldfrompool(dailycode, tablename1, tablename2)
	local poolTable = SFQuest_Database.RandomEventPool[tablename1][tablename2];
	local random = ZombRand(1, #poolTable + 1);
	local randompick = poolTable[random];
	local entry = luautils.split(randompick, ";");
	SF_MissionPanel.instance:runCommand("unlockworldevent", entry[1], entry[2], entry[3], dailycode);	
end

function SF_MissionPanel.Commands.removeclickevent(address)
	local player = getPlayer();
	if not player:getModData().missionProgress.ClickEvent then return end
	for c=1,#player:getModData().missionProgress.ClickEvent do
		local event = player:getModData().missionProgress.ClickEvent[c];
		if event.address and event.address == address then
			table.remove(player:getModData().missionProgress.ClickEvent, c);
			SF_MissionPanel.instance.needsBackup = true;
			break;
		end
	end
end

function SF_MissionPanel.Commands.removemannequin(squaretag)
	if SFQuest_Database.MannequinPool[squaretag] then
		SFQuest_Database.MannequinPool[squaretag].removed = true;
	end
end

function SF_MissionPanel.Commands.revealobjective(guid, index)
	local quest = SF_MissionPanel.instance:getActiveQuest(guid);
	if quest and quest.objectives and  quest.objectives[index] then
		if quest.objectives[index].hidden then
			quest.objectives[index].hidden = nil;
			SF_MissionPanel.instance:setLists();
			SF_MissionPanel.instance.needsUpdate = true;
			SF_MissionPanel.instance.needsBackup = true;
		end
	end
end

function SF_MissionPanel.Commands.unlockdaily(daily)
	local player = getPlayer();
	if not player:getModData().missionProgress.DailyEvent then
		print("SOUL QUEST SYSTEM - " .. player:getUsername() .. " did not have a proper Daily Event table set.");
		return
	end
	local t0 = SF_MissionPanel:getStartingHour();
	local serverTime = getGameTime():getWorldAgeHours() + t0;
	local lastRoll = math.floor(serverTime / (24 * (daily.frequency or 1)));
	local dailyTable = { dailycode = daily.dailycode, condition = daily.condition, commands = daily.commands, days = lastRoll, frequency = daily.frequency };
	table.insert(player:getModData().missionProgress.DailyEvent, dailyTable);
	SF_MissionPanel.instance.needsBackup = true;
end

function SF_MissionPanel.Commands.unlockroom(squaretag, commandslist)
	local player = getPlayer();
	--print("SOUL QUEST SYSTEM - Unlocking a room event.");
	local convertedlist = commandslist:gsub(":", ";");
	if not player:getModData().missionProgress.Rooms[squaretag] then
		player:getModData().missionProgress.Rooms[squaretag] = {};
	end
	table.insert(player:getModData().missionProgress.Rooms[squaretag], convertedlist);
	SF_MissionPanel.instance.needsBackup = true;
end

function SF_MissionPanel.Commands.unlocktimer(guid)
	local player = getPlayer();
	if not player:getModData().missionProgress.Timers then return end
	local timer = SF_MissionPanel:getTimer(guid)
	local agedHours = getGameTime():getWorldAgeHours();
	local timeMod = timer.timermin;
	if not timer.timermin == timer.timermax then
		timeMod = ZombRandFloat(timermin, timermax);
	end
	local finalTime = agedHours + timeMod;
	local timeTable = {guid = timer.guid, command = timer.command, commands = timer.commands, sound = timer.sound, timer = finalTime};
	table.insert(player:getModData().missionProgress.Timers, timeTable);
	SF_MissionPanel.instance.needsBackup = true;
end

function SF_MissionPanel.Commands.unlockworldevent(identity, dialoguecode, questid, dailycode)
	local player = getPlayer();
	local worldevent = SF_MissionPanel.instance:getWorldInfo(identity);
	local squaretag = worldevent.square;
	local event = {identity = identity, dialoguecode = dialoguecode, quest = questid, dailycode = dailycode};
	player:getModData().missionProgress.WorldEvent[squaretag] = event;
	
	local squareTable = luautils.split(squaretag, "x");
	local x, y, z = tonumber(squareTable[1]), tonumber(squareTable[2]), tonumber(squareTable[3]);
	local square = getCell():getGridSquare(x, y, z);
	if square then
		local marker = getIsoMarkers():addIsoMarker({}, {"media/textures/Test_Marker.png"}, square, 1, 1, 1, false, false);
		marker:setDoAlpha(false);
		marker:setAlphaMin(0.8);
		marker:setAlpha(1.0);
		player:getModData().missionProgress.WorldEvent[squaretag].marker = marker;
		SF_MissionPanel.instance.needsBackup = true;
	end
end

function SF_MissionPanel.Commands.updateobjectivetext(guid, index, text)
	local quest = SF_MissionPanel.instance:getActiveQuest(guid);
	if quest and quest.objectives and  quest.objectives[index] then
		quest.objectives[index].text = text;
		SF_MissionPanel.instance.needsUpdate = true;
		SF_MissionPanel.instance.needsBackup = true;
	end
end


--------------------------------------------------------------------------------------------------------

local function round(num, numDecimalPlaces)
    local mult = 10^(numDecimalPlaces or 0)
    return math.floor(num * mult + 0.5) / mult
end

function SF_MissionPanel:initialise()
    ISPanelJoypad.initialise(self);
end

function SF_MissionPanel:createChildren()
    ISPanelJoypad.createChildren(self);

	local th = math.max(16, FONT_HGT_SMALL + 1);
		
	self.tabs = ISTabPanel:new(0, th, self.width, self.height - (th * 1.5))
	self.tabs:setAnchorRight(true)
	self.tabs:setAnchorTop(true)
	self.tabs:setEqualTabWidth(false)
	self:addChild(self.tabs)

	local hasModData;
	if self.player:getModData().missionProgress then
		hasModData = true;
	end
	
	local tempTitle1 = getText("IGUI_XP_Quests_QuestLog");
	local tempGreyed = false;
	if hasModData == true and self.player:getModData().missionProgress.Category1Label then
		tempTitle1 = getText(self.player:getModData().missionProgress.Category1Label);	
	end
	if hasModData == true and self.player:getModData().missionProgress.Category1Greyed then
		tempGreyed = true;
	end

	local btnHgt2 = math.max(FONT_HGT_SMALL, 20)
		
	local btnX = self.width - ((btnHgt2 + 4) * 2);
	local btnY = (self.tabs.height / 2) + 4;
	self.nextPage = ISButton:new(btnX, btnY, btnHgt2, btnHgt2, ">", self, SF_MissionPanel.onClick);
	self.nextPage.internal = "NEXTPAGE";
	self.nextPage:initialise();
	self.nextPage:instantiate();
	self.nextPage.borderColor = {r=1, g=1, b=1, a=0.1};
	self.nextPage:setVisible(false);
	self:addChild(self.nextPage);
	btnX = btnX - btnHgt2 - 2;
	
	self.previousPage = ISButton:new(btnX, btnY, btnHgt2, btnHgt2, "<", self, SF_MissionPanel.onClick);
	self.previousPage.internal = "PREVIOUSPAGE";
	self.previousPage:initialise();
	self.previousPage:instantiate();
	self.previousPage.borderColor = {r=1, g=1, b=1, a=0.1};
	self.previousPage:setEnable(false);
	self.previousPage:setVisible(false);
	self:addChild(self.previousPage);
	btnX = btnX - btnHgt2 - 2;
	
	local labelText = getText("IGUI_Pages") .. tostring(self.currentPage) .. "/" .. tostring(#self.lore or 1);
	local stringWidth = getTextManager():MeasureStringX(self.font, labelText);
	self.pageLabel = ISLabel:new (btnX, btnY, FONT_HGT_SMALL, labelText, 1, 1, 1, 1, self.font, false);
	self.pageLabel:initialise();
	self.pageLabel:instantiate();
	self.pageLabel:setVisible(false);
	self:addChild(self.pageLabel);
	
	self.titleLabel = ISLabel:new (10, btnY, FONT_HGT_SMALL, self.loretitle, 1, 1, 1, 1, self.font, true);
	self.titleLabel:initialise();
	self.titleLabel:instantiate();
	self.titleLabel:setVisible(false);
	self:addChild(self.titleLabel);
	
	self.richText = MapSpawnSelectInfoPanel:new(0, btnY + 12, self.width - 26,150);
	self.richText.autosetheight = false;
	self.richText.clip = true
	self.richText:initialise();
	self.richText.background = false;
	self.richText:setAnchorTop(true);
	self.richText:setAnchorLeft(true);
	self.richText:setVisible(false);
	self.richText.backgroundColor  = {r=0, g=0, b=0, a=0.5};
	self:addChild(self.richText);
	self.richText:addScrollBars();
	
	local listbox1 = SFQuest_MissionLists:new(0, 0, self.tabs.width, self.tabs.height - self.tabs.tabHeight, self.player, true)
	listbox1:setAnchorRight(true)
	listbox1:setAnchorTop(true)
	listbox1:setFont(UIFont.Small, 2)
	listbox1.itemheight = math.max(32, FONT_HGT_SMALL) + 2 * 2
	self.tabs:addView(tempTitle1, listbox1)
	self.listbox1 = listbox1


	local listbox2 = SFQuest_MissionLists:new(0, 0, self.tabs.width, self.tabs.height - self.tabs.tabHeight, self.player, false)
	listbox2:setAnchorRight(true)
	listbox2:setAnchorTop(true)
	listbox2:setFont(UIFont.Small, 2)
	listbox2.itemheight = math.max(32, FONT_HGT_SMALL) + 2 * 2
	local tabtitle2 = getText("IGUI_XP_Quests_ActiveQuests");
	self.tabs:addView(tabtitle2, listbox2)
	self.listbox2 = listbox2
	
	local factionbox = SFQuest_FactionLists:new(0, 0, self.tabs.width, self.tabs.height - self.tabs.tabHeight, self.player, false)
	factionbox:setAnchorRight(true)
	factionbox:setAnchorBottom(true)
	factionbox:setFont(UIFont.Small, 2)
	factionbox.itemheight = math.max(32, FONT_HGT_SMALL) + 2 * 2
	local tabtitle3 = getText("IGUI_XP_Factions");
	self.tabs:addView(tabtitle3, factionbox)
	self.factionbox = factionbox

	self:setLists()
end

function SF_MissionPanel:setLists()
	local cat1 = {}
	local cat2 = {}
	local faction = {}

	if self.player:getModData().missionProgress and self.player:getModData().missionProgress.Category1 then
		local cat1Table = self.player:getModData().missionProgress.Category1;
		if #cat1Table > 0 then
			for i=1,#cat1Table do
				local item = cat1Table[i]
				table.insert(cat1, item)
			end
		end
	end

	if self.player:getModData().missionProgress and self.player:getModData().missionProgress.Category2 then
		local cat2Table = self.player:getModData().missionProgress.Category2;
		if #cat2Table > 0 then
			for i=1,#cat2Table do
				local item = cat2Table[i]
				table.insert(cat2, item)
			end
		end
	end
	
	if self.player:getModData().missionProgress and self.player:getModData().missionProgress.Factions then
		local factTable = self.player:getModData().missionProgress.Factions;
		if #factTable > 0 then
			for i=1,#factTable do
				local item = factTable[i]
				table.insert(faction, item)
			end
		end
	end

	if self.listbox1 then
		self.listbox1:clear()
	for _,item in ipairs(cat1) do
		self.listbox1:addItem(item.name, item)
	end
	end

	if self.listbox2 then
	self.listbox2:clear()
	for _,item in ipairs(cat2) do
		self.listbox2:addItem(item.name, item)
	end
	end
	
	if self.factionbox then
	self.factionbox:clear()
		for _,item in ipairs(faction) do
			self.factionbox:addItem(item.name, item)
		end
	end
end

function SF_MissionPanel:onClick(button)
    if button.internal == "NEXTPAGE" then
        self.currentPage = self.currentPage + 1;
		local text = getText(self.lore[self.currentPage]);
		self.richText.text = text;
        if self.currentPage == #self.lore then
            self.nextPage:setEnable(false);
        else
            self.nextPage:setEnable(true);
        end
        self.previousPage:setEnable(true);
    elseif button.internal == "PREVIOUSPAGE" then
        self.currentPage = self.currentPage - 1;
		local text = getText(self.lore[self.currentPage]);
		self.richText.text = text;
        if self.currentPage == 1 then
            self.previousPage:setEnable(false);
        else
            self.previousPage:setEnable(true);
        end
        self.nextPage:setEnable(true);
    end

    if self.pageLabel then
        self.pageLabel.name = getText("IGUI_Pages") .. self.currentPage .. "/" ..(#self.lore or 1);
    end
end

-------------------------------------------------------------------------------------------------------------------------------------

function SF_MissionPanel:addQuestToCategory(quest, category, sound)
	if not category then return end;
	if self.player:getModData().missionProgress and self.player:getModData().missionProgress[category] then
		local timedQuest = quest;
		timedQuest.timetag = getGameTime():getWorldAgeHours();
		table.insert(self.player:getModData().missionProgress[category], timedQuest);
		if sound then
			self.player:getEmitter():playSound(sound);
			--self.player:    if player has a pager in inventory then we play the sound alarm
		end
		self.needsBackup = true;
	end
end

function SF_MissionPanel:backupData()
	local player = self.player or getPlayer();
	local data = player:getModData().missionProgress;
	if not data then
		print("Player had no quest data for the backup.");
		return
	end
	if isClient() then
		sendClientCommand(player, 'SFQuest', 'saveData', data);
	else
		SFQuest_Server.localBackup(player, data);
	end;
end

function SF_MissionPanel:checkItemQuantity(stringforcheck)
	local needsTable = luautils.split(stringforcheck, ";");
	local itemscript = needsTable[1];
	local quantity = tonumber(needsTable[2]) or 1;
	local carrying;
	local isTag;
	local predicateValue;
	if luautils.stringStarts(needsTable[1], "Tag#") then
		itemscript = luautils.split(itemscript, "#")[2];
		isTag = true;
		carrying = self.player:getInventory():getCountTag(itemscript);
	elseif luautils.stringStarts(needsTable[1], "TagPredicateBigFish#") then
		itemscript = luautils.split(itemscript, "#")[2];
		isTag = true;
		carrying = self.player:getInventory():getCountTagEval(itemscript, predicateBigFish);
	elseif luautils.stringStarts(needsTable[1], "TagPredicateCondition#") then
		itemscript = luautils.split(itemscript, "#")[2];
		isTag = true;
		predicateValue = tonumber(needsTable[3]);
		carrying = self.player:getInventory():getCountTagEvalArg(itemscript, predicateCondition, predicateValue);			
	elseif luautils.stringStarts(needsTable[1], "TagPredicateFreshFood#") then
		itemscript = luautils.split(itemscript, "#")[2];
		isTag = true;
		carrying = self.player:getInventory():getCountTagEval(itemscript, predicateFreshFood);	
	elseif luautils.stringStarts(needsTable[1], "TagPredicateFullDrainable#") then
		itemscript = luautils.split(itemscript, "#")[2];
		isTag = true;
		carrying = self.player:getInventory():getCountTagEval(itemscript, predicateFullDrainable);
	else
		carrying = self.player:getInventory():getNumberOfItem(itemscript, true, true);							
	end

	if quantity <= carrying then
		return true
	end
	return false
end

function SF_MissionPanel:checkObjectivesForCompletion(type, entry, newStatus)
	local status = newStatus or "Obtained";
	
	if self.player:getModData().missionProgress and self.player:getModData().missionProgress.Category2 then
		local currentTasks = self.player:getModData().missionProgress.Category2
		local done = false
		if #currentTasks > 0 then
			for i=1,#currentTasks do
				local task = currentTasks[i]
				if task.objectives and #task.objectives > 0 then
					for k=1,#task.objectives do
						if not task.objectives[k].status then
							if type == "item" and currentTasks[i].objectives[k].needsitem and currentTasks[i].objectives[k].needsitem == entry then
								local guid = currentTasks[i].guid;
								SF_MissionPanel.instance:updateObjective(guid, k, status)
							end
						end
					end
				end
			end	
		end		
	end
end

function SF_MissionPanel:checkQuestForCompletionByType(type, entry, newStatus)
	local status = newStatus or "Obtained";

	if self.player:getModData().missionProgress and self.player:getModData().missionProgress.Category2 then
		local currentTasks = self.player:getModData().missionProgress.Category2
		local done = false
		if #currentTasks > 0 then
			for i=1,#currentTasks do
				local task = currentTasks[i];
				-- If there is no item script then it is a generic check for all possible items.
				if entry == nil then
					if type == "item" and task.needsitem then
						local quantitycheck = SF_MissionPanel.instance:checkItemQuantity(task.needsitem)
						if quantitycheck then
							task.status = status;
							self.needsUpdate = true
							if status == "Obtained" and task.onobtained then
								local commandTable = luautils.split(task.onobtained, ";");
								SF_MissionPanel.instance:readCommandTable(commandTable);
							end
							if status == "Completed" then
								local guid = task.guid;
								SF_MissionPanel.instance:completeQuest(getPlayer(), guid);
							end		
						end
					end
					if type == "item" and task.objectives and #task.objectives > 0 then
						for o=1,#task.objectives do
							local objective = task.objectives[o];
							if objective.needsitem then
								local quantitycheck = SF_MissionPanel.instance:checkItemQuantity(objective.needsitem);
								if quantitycheck then
									local guid = task.guid;
									SF_MissionPanel.instance:updateObjective(guid, o, status)
									self.needsUpdate = true
								end
							
							end
						end
					end
				else
					if type == "item" and task.needsitem then
						local needsTable = luautils.split(task.needsitem, ";");
						local itemscript = needsTable[1];
						local quantity = tonumber(needsTable[2]) or 1;
						local isTag;
						if luautils.stringStarts(needsTable[1], "Tag#") then
							itemscript = luautils.split(itemscript, "#")[2];
							isTag = true;
						end
						if itemscript == entry then
							task.status = status;
							self.needsUpdate = true
							if status == "Completed" then
								local guid = task.guid;
								SF_MissionPanel.instance:completeQuest(getPlayer(), guid);
							end
						end
					end
				end
			end	
		end		
	end
end

function SF_MissionPanel:checkTaskForCompletion(guid)
	local player = self.player or getPlayer();
	if player:getModData().missionProgress and player:getModData().missionProgress.Category2 then
		local currentTasks = player:getModData().missionProgress.Category2
		local done = false
		if #currentTasks > 0 then
			for i=1,#currentTasks do
				local task = currentTasks[i];
				if task.guid and task.guid == guid then
					local completed = true;
					local deliveryindex;
					if task.objectives and #task.objectives > 0 then
						for o=1,#task.objectives do
							objective = currentTasks[i].objectives[o];
							if not objective.status then
								completed = false;			
							elseif objective.needsitem ~= nil and not objective.status == "Completed" then
								completed = false;
							elseif objective.blockscompletion == true and not objective.status == "Completed" then
								completed = false;
							elseif objective.deliverysquare then
								deliveryindex = o;
							end
						end
					end
					if completed == true and task.onobjectivescompleted then
						local commandsTable = luautils.split(task.onobjectivescompleted, ";");
						SF_MissionPanel.instance:readCommandTable(commandsTable);
					end
					if completed == true and not task.needsreport then
						if deliveryindex then
							player:getModData().missionProgress.Category2[i].objectives[deliveryindex].status = "Completed";
						end
						SF_MissionPanel:completeQuest(player, guid);
					end
					break
				end
			end
		end
	end
end

function SF_MissionPanel:completeQuest(player, guid)
	if player:getModData().missionProgress and player:getModData().missionProgress.Category2 then
		local currentTasks = player:getModData().missionProgress.Category2;
		local done = false
		if #currentTasks > 0 then
			for i=1,#currentTasks do
				local task = currentTasks[i];
				if task.guid and task.guid == guid then
					player:getModData().missionProgress.Category2[i].status = "Completed";
					if task.awardstask then
						SF_MissionPanel:unlockQuest(task.awardstask);
					end
					if task.awardsitem then
						if luautils.stringStarts(task.awardsitem, "Table:") then
							print("SOUL QUEST SYSTEM - REWARD ITEM STARTS WITH TABLE:");
						end
						local count = 1;
						local rewardTable = luautils.split(task.awardsitem, ";");
						local quantity = 1;
						if rewardTable[count + 1] then
							quantity = tonumber(rewardTable[count + 1]);
						end
						player:getInventory():AddItems(rewardTable[1], quantity);
						count = 3;
						while rewardTable[count] do
							quantity = tonumber(rewardTable[count + 1]);
							player:getInventory():AddItems(rewardTable[count], quantity);
							count = count + 2;
						end
					end
					if task.awardslore then
						if task.lore then
							table.insert(task.lore, task.awardslore);
						else
							task.lore = {task.awardslore};
						end
					end
					if task.awardsrep then
						local repvalues = luautils.split(task.awardsrep, ";");
						print("This task awards " .. repvalues[2] .. " reputation for " .. repvalues[1]);
						SF_MissionPanel.instance:awardReputation(repvalues[1], tonumber(repvalues[2]));
					end
					if task.awardsworld then
						local entry = luautils.split(task.awardsworld, ";");
						SF_MissionPanel.instance:runCommand("unlockworldevent", entry[1], entry[2], entry[3]);				
					end
					if task.completesound then
						getSoundManager():playUISound(task.completesound);
						--player:getEmitter():playSound(task.completesound);
					end
					if task.guid == self.expanded then
						self.expanded = nil;
						self.loretitle = nil;
						self.lore = {};
						self.currentPage = 1;
						self.titleLabel:setVisible(false);
						self.pageLabel:setVisible(false);
						self.nextPage:setVisible(false);
						self.previousPage:setVisible(false);
						self.richText:setVisible(false);
					end
					table.insert(player:getModData().missionProgress.Category1, task);
					table.remove(player:getModData().missionProgress.Category2, i);
					done = true;
					self.needsUpdate = true
					break
				end
			end
		end
		if #player:getModData().missionProgress.Delivery > 0 then
			for d=1,#player:getModData().missionProgress.Delivery do
				if player:getModData().missionProgress.Delivery[d] == guid then
					player:getModData().missionProgress.Delivery[d] = nil;
					break
				end
			end
		end
		self.needsBackup = true;
	end
end

function SF_MissionPanel:countActiveQuestsWithCode(dailycode)
	local player = getPlayer();
	if player:getModData().missionProgress and player:getModData().missionProgress.Category2 then
		local activeQuests = player:getModData().missionProgress.Category2;
		local count = 0;
		for q=1,#activeQuests do
			if activeQuests[q].dailycode and activeQuests[q].dailycode == dailycode then
				count = count + 1;
			end
		end
		return count
	end
	return 0
end

function SF_MissionPanel.DailyEventReroll()
	local player = getPlayer();
	if not player:getModData().missionProgress then return end
	if player:getModData().missionProgress.DailyEvent and #player:getModData().missionProgress.DailyEvent > 0 then
		--print("SOUL QUEST SYSTEM - Player has daily events to check.");
		local t0 = SF_MissionPanel:getStartingHour();
		local ageHours = getGameTime():getWorldAgeHours();
		local serverTime = ageHours + t0;
		for d = #player:getModData().missionProgress.DailyEvent,1,-1 do	
			local event = player:getModData().missionProgress.DailyEvent[d];
			local lastRoll = math.floor(serverTime / (24 * (event.frequency or 1)));
			if event.days ~= lastRoll then
				--print("SOUL QUEST SYSTEM - Time for rerolling this daily event.");
				if event.condition then
					local conditionTable = luautils.split(event.condition, ";");
					if conditionTable[1] == "notmaxedwithcode" then
						local dailycode = conditionTable[2];
						local maxed = tonumber(conditionTable[3]);
						local active = SF_MissionPanel.instance:countActiveQuestsWithCode(dailycode);
						if active < maxed then
							event.days = lastRoll;
							SF_MissionPanel.instance.needsBackup = true;
							print("SOUL QUEST SYSTEM - No active quests for daily code: " .. dailycode);
							if player:getModData().missionProgress.WorldEvent then
								SF_MissionPanel.instance:removeWorldEventsWithCode(dailycode);
							end
							local commandTable = luautils.split(event.commands, ";");
							SF_MissionPanel.instance:readCommandTable(commandTable);
						end
					end
				end
			end
		end
	end
end

function SF_MissionPanel.EveryTenMinutes()
	local player = getPlayer();
	if not player:getModData().missionProgress then return end
	
	if player:getModData().missionProgress.ActionEvent and #player:getModData().missionProgress.ActionEvent > 0 then
		local actionevent = player:getModData().missionProgress.ActionEvent;
		for a=#actionevent,1,-1 do
			if actionevent[a].condition then
				local condition = luautils.split(actionevent[a].condition, ";");
				if condition[1] == "enterroom" then
					local squareTable = luautils.split(condition[2], "x");
					local x, y, z = tonumber(squareTable[1]), tonumber(squareTable[2]), tonumber(squareTable[3]);
					local square = getCell():getGridSquare(x, y, z);
					if square then
						local room = square:getRoom();
						if player:getSquare():getRoom() == room then
							local commandTable = luautils.split(actionevent[a].commands, ";");
							SF_MissionPanel.instance:readCommandTable(commandTable);
							table.remove(actionevent, a);
						end
					end	
				elseif condition[1] == "enterdungeon" then
					local dungeonID = condition[2];
					if player:getModData().CurrentDungeon.dungeonId and player:getModData().CurrentDungeon.dungeonId == dungeonID then
						local commandTable = luautils.split(actionevent[a].commands, ";");
						SF_MissionPanel.instance:readCommandTable(commandTable);
						table.remove(actionevent, a);					
					end
				elseif condition[1] == "killzombies" then
					local currentkills = player:getZombieKills();
					local goal = tonumber(condition[2]);
					if currentkills >= goal then
							local commandTable = luautils.split(actionevent[a].commands, ";");
							SF_MissionPanel.instance:readCommandTable(commandTable);
							table.remove(actionevent, a);
					end
				elseif condition[1] == "readbook" then
					
				elseif condition[1] == "watchmedia" then
					local media = getZomboidRadio():getRecordedMedia():getMediaData(condition[2]);
					local watched = getZomboidRadio():getRecordedMedia():hasListenedToAll(player, media);
					if watched and actionevent[a].commands then
						local commandTable = luautils.split(actionevent[a].commands, ";");
						SF_MissionPanel.instance:readCommandTable(commandTable);
						table.remove(actionevent, a);
					end
				end
			end
		end
	end
	
	if player:getModData().missionProgress.Timers and #player:getModData().missionProgress.Timers > 0 then
		local timers = player:getModData().missionProgress.Timers;
		local ageHours = getGameTime():getWorldAgeHours();
		for i=#timers,1,-1 do
			if timers[i].timer and timers[i].timer < ageHours then
				if timers[i].command == "unlockQuest" then
					if timers[i].category then
						SF_MissionPanel.instance:addTaskToCategory(timers[i].guid, category, timers[i].sound);
					else
						SF_MissionPanel.instance:unlockQuest(timers[i].guid, timers[i].sound);
					end
				end
				if timers[i].commands then
					local commandTable = luautils.split(timers[i].commands, ";");
					SF_MissionPanel.instance:readCommandTable(commandTable);		
				end
				table.remove(player:getModData().missionProgress.Timers, i);
				SF_MissionPanel.instance.needsBackup = true;
			end
		end
	end

	if player:getModData().missionProgress.WorldEvent then
		for k2,v2 in pairs(player:getModData().missionProgress.WorldEvent) do
			if not v2.marker then
				local squareTable = luautils.split(k2, "x");
				local x, y, z = tonumber(squareTable[1]), tonumber(squareTable[2]), tonumber(squareTable[3]);
				local square = getCell():getGridSquare(x, y, z);
				if square then
					local marker = getIsoMarkers():addIsoMarker({}, {"media/textures/Test_Marker.png"}, square, 1, 1, 1, false, false);
					marker:setDoAlpha(false);
					marker:setAlphaMin(0.8);
					marker:setAlpha(1.0);
					v2.marker = marker;
				end
			end
		end
	end
	
	if SF_MissionPanel.instance.needsBackup == true then
		SF_MissionPanel.instance:backupData();
		SF_MissionPanel.instance.needsBackup = false;
	end
end

Events.EveryDays.Add(SF_MissionPanel.DailyEventReroll)
Events.EveryTenMinutes.Add(SF_MissionPanel.EveryTenMinutes)
Events.OnGameStart.Add(SF_MissionPanel.DailyEventReroll)

function SF_MissionPanel:getActiveQuest(guid)
	local player = self.player or getPlayer();
	if player:getModData().missionProgress and player:getModData().missionProgress.Category2 then
		local currentTasks = player:getModData().missionProgress.Category2
		local done = false
		if #currentTasks > 0 then
			for i=1,#currentTasks do
				local task = currentTasks[i]
				if currentTasks[i].guid and currentTasks[i].guid == guid then
					return player:getModData().missionProgress.Category2[i];
				end
			end
		end
		print("SOUL QUEST SYSTEM - No quest with guid: " .. guid .. " in the list of active quests.")
	end
	return nil
end

function SF_MissionPanel:getDailyEvent(dailycode)
	for i=1,#SFQuest_Database.DailyEventPool do
		if SFQuest_Database.DailyEventPool[i].dailycode and SFQuest_Database.DailyEventPool[i].dailycode == dailycode then
			return SFQuest_Database.DailyEventPool[i];
		end
	end
	print("SOUL QUEST SYSTEM - No daily event with dailycode: " .. dailycode .. " in the pool of daily events.")
	return nil
end

function SF_MissionPanel:getQuest(guid)
	for i=1,#SFQuest_Database.QuestPool do
		if SFQuest_Database.QuestPool[i].guid and SFQuest_Database.QuestPool[i].guid == guid then
			return SFQuest_Database.QuestPool[i];
		end
	end
	print("SOUL QUEST SYSTEM - No quest with guid: " .. guid .. " in the pool of quests.")
	return nil
end

function SF_MissionPanel:getStartingHour()
	local timeofday = getGameTime():getStartTimeOfDay();
	local modifier = 0;
	if timeofday >= 7 then
		modifier = 7;
	else
		modifier = -17; -- 19 e 22 pra 2AM e 5AM
	end
	return modifier
end

function SF_MissionPanel:getTimer(guid)
	for i=1,#SFQuest_Database.TimerPool do
		if SFQuest_Database.TimerPool[i].guid and SFQuest_Database.TimerPool[i].guid == guid then
			return SFQuest_Database.TimerPool[i];
		end
	end
	print("SOUL QUEST SYSTEM - No Timer with guid: " .. guid .. " in the pool of timers.")
	return nil
end

function SF_MissionPanel:removeCallFromList(guid)
	if self.player:getModData().missionProgress and self.player:getModData().missionProgress.Calls then
		local calls = self.player:getModData().missionProgress.Calls;
			if #calls > 0 then
				for i=1,#calls do
					if calls[i] == guid then
						table.remove(self.player:getModData().missionProgress.Calls, i);
						break
					end
				end
			end
	end
end

function SF_MissionPanel:returnAwardedItemsForEventWindow(awardeditem)
	local tab = luautils.split(awardeditem, ";");
	local scriptItem = getScriptManager():FindItem(tab[1])
	local itemName = scriptItem:getName();
	local finalstring = itemName
	if tab[2] then
		finalstring = itemName .. " X " .. tab[2];
		return finalstring;
	end
end

function SF_MissionPanel:unlockQuest(guid, overrideAwardsItem)
	local player = self.player or getPlayer();
	local quest = SF_MissionPanel:getQuest(guid);
	if player:getModData().missionProgress and player:getModData().missionProgress.Category2 then
		if guid then
			local hasTask = false
			local currentTasks = player:getModData().missionProgress.Category2
			if #currentTasks > 0 then
				for i=1,#currentTasks do
					if currentTasks[i].guid and currentTasks[i].guid == guid then
						hasTask = true
					end
				end
			end
			if quest.unique then --a unique quest will never be unlocked again, so we need to check the quest log too
				local questLog = player:getModData().missionProgress.Category1	
				if questLog and #questLog > 0 then
					for i=1,#questLog do
						if questLog[i].guid and questLog[i].guid == guid then
							hasTask = true
						end
					end
				end
			end
			if hasTask == true then
				print("SOUL QUEST SYSTEM - Player already had that quest unlocked.");
				return
			end
		end
		--quest should be unlocked for this player
		if quest.unlockedsound then
			getSoundManager():playUISound(quest.unlockedsound);
		end
		if quest.updates then
			local updatedQuest = SF_MissionPanel.instance:getActiveQuest(quest.updates);
			if updatedQuest then
				updatedQuest.awardsitem = quest.awardsitem or updatedQuest.awardsitem;
				updatedQuest.awardslore = quest.awardslore or updatedQuest.awardslore;
				updatedQuest.awardsrep = quest.awardsrep or updatedQuest.awardsrep;
				updatedQuest.awardstask = quest.awardstask or updatedQuest.awardstask;
				updatedQuest.awardsworld = quest.awardsworld or updatedQuest.awardsworld;
				updatedQuest.completesound = quest.completesound or updatedQuest.completesound;
				updatedQuest.needsitem = quest.needsitem or updatedQuest.needsitem;
				updatedQuest.onobtained = quest.onobtained or updatedQuest.onobtained;
				updatedQuest.text = quest.text or updatedQuest.text;
				updatedQuest.texture = quest.texture or updatedQuest.texture;
				updatedQuest.title = quest.title or updatedQuest.title;
				if (not updatedQuest.lore) and quest.lore then
					updatedQuest.lore = quest.lore;
				elseif updatedQuest.lore and quest.lore then
					for l=1, #quest.lore do
						local lore = quest.lore[l];
						table.insert(updatedQuest.lore, lore);
					end
				end
			else
				print("SOUL QUEST SYSTEM - Unlocked quest was an update to an existing quest that could not be found.");
			end
		else
			if quest.awardsitem and overrideAwardsItem then
				local newQuest = quest;
				newQuest.awardsitem = overrideAwardsItem;
				table.insert(player:getModData().missionProgress.Category2, newQuest);	
			elseif quest.awardsitem and luautils.stringStarts(quest.awardsitem, "Table:") then
				local tableKey = luautils.split(quest.awardsitem, ":")[2];
				local rewardTable = SFQuest_Database.RandomRewardItemPool[tableKey];
				local newQuest = quest;
				if rewardTable and #rewardTable > 0 then
					newQuest.awardsitem = rewardTable[ZombRand(1, #rewardTable + 1)]
				end
				table.insert(player:getModData().missionProgress.Category2, newQuest);			
			else
				table.insert(player:getModData().missionProgress.Category2, quest);	
			end
		end
		if quest.unlocks then
			local commandTable = luautils.split(quest.unlocks, ";");
			SF_MissionPanel.instance:readCommandTable(commandTable);
		end
		SF_MissionPanel.instance.needsUpdate = true;
		SF_MissionPanel.instance.needsBackup = true;
		return
	else
		print("Player did not have a list of current tasks. Unable to unlock a task.");
	end
end

function SF_MissionPanel:unlockQuestsFromPager()
	if self.player:getModData().missionProgress and self.player:getModData().missionProgress.CategoryPager then
		local pagerTasks = self.player:getModData().missionProgress.CategoryPager;
		if #pagerTasks > 0 then
			for i=1,#pagerTasks do
				if not pagerTasks[i].update then
					local task = pagerTasks[i];
					table.insert(self.player:getModData().missionProgress.Category2, task);
					table.remove(self.player:getModData().missionProgress.CategoryPager, i);
					if task.unlocks then
						local commandTable = luautils.split(task.unlocks, ";");
						SF_MissionPanel.instance:readCommandTable(commandTable);
					end
				else
					local update = pagerTasks[i];
					if update.unlocks then
						local commandTable = luautils.split(update.unlocks, ";");
						SF_MissionPanel.instance:readCommandTable(commandTable);
					end
					for t=1,#self.player:getModData().missionProgress.Category2 do
						local task = self.player:getModData().missionProgress.Category2[t]
						if task.guid == update.task then
							if update.index then --this is an update for one of the task's objectives
								self.player:getModData().missionProgress.Category2[t].objectives[update.index] = {};
								self.player:getModData().missionProgress.Category2[t].objectives[update.index].status = update.status;
								if update.blockscompletion then --if the objective was waiting for an update so the task can be completed, allow it to be completed then
									self.player:getModData().missionProgress.Category2[t].objectives[update.index].blockscompletion = update.blockscompletion;
								end
								if update.text then
									self.player:getModData().missionProgress.Category2[t].objectives[update.index].text = update.text;								
								end
							else -- this is an update for the task in general
								self.player:getModData().missionProgress.Category2[t].status = update.status;
								if update.text then
									self.player:getModData().missionProgress.Category2[t].text = update.text;								
								end
							end
						end
					end
					table.remove(self.player:getModData().missionProgress.CategoryPager, i);
				end
				self.needsUpdate = true;
				self.needsBackup = true;
			end
		else
			print("pager list did not contain any tasks, unable to search for provided guid.")
			return
		end
	else
		print("Player does not have a pager list.");
		return
	end
end

function SF_MissionPanel:updateLore(guid, lore)
	local player = self.player or getPlayer();
	if player:getModData().missionProgress and player:getModData().missionProgress.Category2 then
		local currentTasks = player:getModData().missionProgress.Category2;
		local done = false
		if #currentTasks > 0 then
			for i=1,#currentTasks do
				local task = currentTasks[i]
				if task.guid and task.guid == guid then
					if not currentTasks[i].lore then
						currentTasks[i].lore = {};
					end
					for l=1, #lore do
						table.insert(currentTasks[i].lore, lore[l])
					end
					self.needsUpdate = true;
					self.needsBackup = true;		
					break
				end
			end
		end
	end
end

function SF_MissionPanel:updateObjective(guid, index, status)
	local player = self.player or getPlayer();
	if player:getModData().missionProgress and player:getModData().missionProgress.Category2 then
		local currentTasks = player:getModData().missionProgress.Category2
		local done = false
		if #currentTasks > 0 then
			for i=1,#currentTasks do
				local task = currentTasks[i]
				if task.guid and task.guid == guid then
					if task.objectives and  task.objectives[index] then
						task.objectives[index].status = status;
						if status == "Failed" and task.objectives[index].onfailed then
							local commandTable = luautils.split(task.objectives[index].onfailed, ";");
							SF_MissionPanel.instance:readCommandTable(commandTable);
						elseif status == "Completed" and task.objectives[index].oncompleted then	
							local commandTable = luautils.split(task.objectives[index].oncompleted, ";");
							SF_MissionPanel.instance:readCommandTable(commandTable);
						elseif status == "Obtained" and task.objectives[index].onobtained then	
							local commandTable = luautils.split(task.objectives[index].onobtained, ";");
							SF_MissionPanel.instance:readCommandTable(commandTable);							
						end
						SF_MissionPanel:checkTaskForCompletion(guid)
						self.needsUpdate = true;
						self.needsBackup = true;
					end
				end
			end
		end
	end
end

function SF_MissionPanel:updateQuestStatus(guid, status)
	local player = self.player or getPlayer();
	if player:getModData().missionProgress and player:getModData().missionProgress.Category2 then
		local currentTasks = player:getModData().missionProgress.Category2
		local done = false
		if #currentTasks > 0 then
			for i=1,#currentTasks do
				local task = currentTasks[i]
				if task.guid and task.guid == guid then
					task.status = status;
					if status == "Failed" and task.onfailed then
						local commandTable = luautils.split(task.onfailed, ";");
						SF_MissionPanel.instance:readCommandTable(commandTable);
					elseif status == "Done" and task.ondone then	
						local commandTable = luautils.split(task.ondone, ";");
						SF_MissionPanel.instance:readCommandTable(commandTable);
					elseif status == "Obtained" and task.onobtained then	
						local commandTable = luautils.split(task.onobtained, ";");
						SF_MissionPanel.instance:readCommandTable(commandTable);							
					end			
					self.needsUpdate = true;
					self.needsBackup = true;			
				end
			end
		end
	end
end

function SF_MissionPanel:takeNeededItem(neededitem)
	local player = getPlayer();
	local needsTable = luautils.split(neededitem, ";");
	local itemscript = needsTable[1];
	local quantity = tonumber(needsTable[2]) or 1;
	local carrying;
	local items;
	local predicateValue;
	if luautils.stringStarts(needsTable[1], "Tag#") then
		itemscript = luautils.split(itemscript, "#")[2];
		carrying = player:getInventory():getCountTag(itemscript);
		if quantity <= carrying then
			items = player:getInventory():getSomeTag(itemscript, quantity);
		end
	elseif luautils.stringStarts(needsTable[1], "TagPredicateBigFish#") then
		itemscript = luautils.split(itemscript, "#")[2];
		carrying = player:getInventory():getCountTagEval(itemscript, predicateBigFish);
		if quantity <= carrying then
			items = player:getInventory():getSomeTagEvalRecurse(itemscript, predicateBigFish, quantity);
		end		
	elseif luautils.stringStarts(needsTable[1], "TagPredicateCondition#") then
		itemscript = luautils.split(itemscript, "#")[2];
		predicateValue = tonumber(needsTable[3]);
		carrying = player:getInventory():getCountTagEvalArg(itemscript, predicateCondition, predicateValue);
		if quantity <= carrying then
			items = player:getInventory():getSomeTagEvalArgRecurse(itemscript, predicateCondition, predicateValue, quantity);
		end	
	elseif luautils.stringStarts(needsTable[1], "TagPredicateFreshFood#") then	
		itemscript = luautils.split(itemscript, "#")[2];
		carrying = player:getInventory():getCountTagEval(itemscript, predicateFreshFood);
		if quantity <= carrying then
			items = player:getInventory():getSomeTagEvalRecurse(itemscript, predicateFreshFood, quantity);
		end	
	elseif luautils.stringStarts(needsTable[1], "TagPredicateFullDrainable#") then	
		itemscript = luautils.split(itemscript, "#")[2];
		carrying = player:getInventory():getCountTagEval(itemscript, predicateFullDrainable);
		if quantity <= carrying then
			items = player:getInventory():getSomeTagEvalRecurse(itemscript, predicateFullDrainable, quantity);
		end	
	else
		carrying = player:getInventory():getNumberOfItem(itemscript, true, true);
		if quantity <= carrying then
			items = player:getInventory():getSomeType(itemscript, quantity);
		end	
	end

	if items then
		for i=0, items:size()-1 do
			local item = items:get(i);
			player:getInventory():Remove(item);
		end
		return true
	end
	return nil					
end

--------------------------------------------------------------------------------------------------------
-- Faction-related functions

function SF_MissionPanel:awardReputation(faction, value)
	local player = self.player or getPlayer();
	if player:getModData().missionProgress and player:getModData().missionProgress.Factions then
		local factions = player:getModData().missionProgress.Factions;
		
		local facIndex;
		local currentRep;
		local currentTier;
		local newTier;
		local maxTier;
		
		if #factions > 0 then
			for j=1,#factions do
				if factions[j].factioncode and factions[j].factioncode == faction then
					facIndex = j;
					currentRep = factions[j].reputation;
					currentTier = factions[j].tierlevel;
					--print("SOUL QUEST SYSTEM - Player has a proper faction to increase reputation: " .. factions[j].factioncode .. " and reputation was " .. tostring(currentRep));
					break
				end
			end
		end
		if facIndex then
			for i=1,#SFQuest_Database.FactionPool do
				if SFQuest_Database.FactionPool[i].factioncode and SFQuest_Database.FactionPool[i].factioncode == faction then
					if SFQuest_Database.FactionPool[i].maxtier then maxTier = SFQuest_Database.FactionPool[i].maxtier end
					if currentTier == maxTier then
						player:getModData().missionProgress.Factions[facIndex].reputation = currentRep + value;
						if player:getModData().missionProgress.Factions[facIndex].reputation > player:getModData().missionProgress.Factions[facIndex].repmax then
							player:getModData().missionProgress.Factions[facIndex].reputation = player:getModData().missionProgress.Factions[facIndex].repmax;
						end
					else
						player:getModData().missionProgress.Factions[facIndex].reputation = currentRep + value;		
						if player:getModData().missionProgress.Factions[facIndex].reputation >= player:getModData().missionProgress.Factions[facIndex].repmax then
							newTier = currentTier + 1;
							player:getModData().missionProgress.Factions[facIndex].tierlevel = newTier;
							player:getModData().missionProgress.Factions[facIndex].reputation = player:getModData().missionProgress.Factions[facIndex].reputation - player:getModData().missionProgress.Factions[facIndex].repmax;
							if SFQuest_Database.FactionPool[i].tiers then
								local tier = SFQuest_Database.FactionPool[i].tiers[newTier];
								local newName = tier.tiername;
								local newMax = tier.minrep;
								local newColor = tier.barcolor;
								if tier.unlocks then
									local commandTable = luautils.split(tier.unlocks, ";");
									SF_MissionPanel.instance:readCommandTable(commandTable);
								end
								
								player:getModData().missionProgress.Factions[facIndex].tiername = newName;
								player:getModData().missionProgress.Factions[facIndex].repmax = newMax;
								player:getModData().missionProgress.Factions[facIndex].tiercolor = newColor;
							end
						end						
					end
				end
			end
			self.needsUpdate = true;
			self.needsBackup = true;	
		end
	end
end

function SF_MissionPanel:getColor(color)
	return SFQuest_Database.ColorPool[color];
end

function SF_MissionPanel:getReputationTier(faction, player)
	local player = player or getPlayer();
	if player:getModData().missionProgress and player:getModData().missionProgress.Factions then
		local factions = player:getModData().missionProgress.Factions;
		if #factions > 0 then
			for j=1,#factions do
				if factions[j].factioncode and factions[j].factioncode == faction then
					local tier = factions[j].tierlevel or 1;
					return tier;
				end
			end
		end
	end
	print("Player is missing data for the requested faction's tier. Returning 1 as the tier to avoid errors.");
	return 1;		
end

function SF_MissionPanel:getColorForFactionTier(faction, reputation)
	for i=1,#SFQuest_Database.FactionPool do
		if SFQuest_Database.FactionPool[i].factioncode and SFQuest_Database.FactionPool[i].factioncode == faction then
			print("Found the right faction, now seeking for the proper color!");
			if SFQuest_Database.FactionPool[i].tiers then
				local tiers = SFQuest_Database.FactionPool[i].tiers;
				if #tiers > 0 then
					for j=1,#tiers do
						if tiers[j].minrep and reputation > tiers[j].minrep then
							print("Tier's minimum reputation was " .. tostring(tiers[j].minrep) .. " so player has enough reputation for this tier.");
							local color = tiers[j].barcolor;
							print("Tier's color was " .. color);
							if color and SFQuest_Database.ColorPool[color] then
								print("Color " .. color .. " was found! Let's return it then.");
								local tab = SFQuest_Database.ColorPool[color];
								return tab;
							end
						end
					end
				end
			end
		end
	end
	return {1.0, 1.0, 1.0}
end

--------------------------------------------------------------------------------------------------------
-- World Event functions

function SF_MissionPanel:getDialogueInfo(code)
	for i=1,#SFQuest_Database.DialoguePool do
		if SFQuest_Database.DialoguePool[i].dialoguecode and SFQuest_Database.DialoguePool[i].dialoguecode == code then	
			return SFQuest_Database.DialoguePool[i];
		end
	end
	print("SOUL QUEST SYSTEM - Unable to find a Dialogue with dialoguecode: " .. code);
	return nil
end

function SF_MissionPanel:getWorldInfo(identity)
	for i=1,#SFQuest_Database.WorldPool do
		if SFQuest_Database.WorldPool[i].identity and SFQuest_Database.WorldPool[i].identity == identity then	
			return SFQuest_Database.WorldPool[i];
		end
	end
	print("SOUL QUEST SYSTEM - Unable to find a World Event with identity: " .. identity);
	return nil
end

-- Removes a Word Event from a player's list.
function SF_MissionPanel:removeWorldEvent(squaretag)
	local player = self.player or getPlayer();
	if player:getModData().missionProgress and player:getModData().missionProgress.WorldEvent then
		if player:getModData().missionProgress.WorldEvent[squaretag] and player:getModData().missionProgress.WorldEvent[squaretag].marker then
			player:getModData().missionProgress.WorldEvent[squaretag].marker:remove();
		end
		player:getModData().missionProgress.WorldEvent[squaretag] = nil;
	end
end

-- Searches for world events that match the dailycode and remove them
function SF_MissionPanel:removeWorldEventsWithCode(dailycode)
	local player = self.player or getPlayer();
	if player:getModData().missionProgress and player:getModData().missionProgress.WorldEvent then
		for k, v in ipairs(player:getModData().missionProgress.WorldEvent) do
			local event = v;
			if v.dailycode and v.dailycode == dailycode then
				SF_MissionPanel.instance:removeWorldEvent(k);
			end
		end
	end
end

--------------------------------------------------------------------------------------------------------
-- Commands functions

function SF_MissionPanel:readCommandTable(commandTable)
	local player = getPlayer();
	local count = 1;
	
	while commandTable[count] do
		if commandTable[count] == "actionevent" then
			SF_MissionPanel.instance:runCommand("actionevent", commandTable[count + 1], commandTable[count + 2]);
			count = count + 3;	
		elseif commandTable[count] == "additem" then
			SF_MissionPanel.instance:runCommand("additem", commandTable[count + 1], tonumber(commandTable[count + 2]));
			count = count + 3;	
		elseif commandTable[count] == "addmannequin" then
			SF_MissionPanel.instance:runCommand("addmannequin", commandTable[count + 1]);
			count = count + 2;			
		elseif commandTable[count] == "call" then
			local callguid = commandTable[count + 1];
			table.insert(player:getModData().missionProgress.Calls, callguid)	
			self.needsBackup = true;
			count = count + 2;
		elseif commandTable[count] == "clickevent" then
			SF_MissionPanel.instance:runCommand("clickevent", commandTable[count + 1], commandTable[count + 2], commandTable[count + 3]);	
			count = count + 4;	
		elseif commandTable[count] == "completequest" then
			local guid = commandTable[count + 1];
			SF_MissionPanel.instance:completeQuest(player, guid);
			count = count + 2;	
		elseif commandTable[count] == "delivery" then
			local guid = commandTable[count + 1];
			
			--if task.update then guid = task.task end;
			--player:getModData().missionProgress.Delivery[task.deliverysquare] = guid;
		
			self.needsBackup = true;
			count = count + 2;	
		elseif commandTable[count] == "lore" then
			SF_MissionPanel.instance:updateLore(commandTable[count + 1], {commandTable[count + 2]});
			count = count + 3;
		elseif commandTable[count] == "playersay" then
			SF_MissionPanel.instance:runCommand("playersay", commandTable[count + 1]);
			count = count + 2;
		elseif commandTable[count] == "quest" or commandTable[count] == "unlockquest" then
			SF_MissionPanel.instance:unlockQuest(commandTable[count + 1]);
			count = count + 2;	
		elseif commandTable[count] == "removeclickevent" then
			SF_MissionPanel.instance:runCommand("removeclickevent", commandTable[count + 1]);
			count = count + 2;	
		elseif commandTable[count] == "randomcodedworldfrompool" then
			SF_MissionPanel.instance:runCommand("randomcodedworldfrompool", commandTable[count + 1], commandTable[count + 2], commandTable[count + 3]);	 
			count = count + 4;
		elseif commandTable[count] == "removemannequin" then
			SF_MissionPanel.instance:runCommand("removemannequin", commandTable[count + 1]);
			count = count + 2;				
		elseif commandTable[count] == "revealobjective" then
			SF_MissionPanel.instance:runCommand("revealobjective", commandTable[count + 1], tonumber(commandTable[count + 2]));	
			count = count + 3;
		elseif commandTable[count] ==  "timer" or commandTable[count] ==  "unlocktimer" then
			SF_MissionPanel.instance:runCommand("unlocktimer", commandTable[count + 1]);
			count = count + 2;	
		elseif commandTable[count] == "updatequeststatus" then
			SF_MissionPanel.instance:updateQuestStatus(commandTable[count + 1], commandTable[count + 2]);
			count = count + 3;			
		elseif commandTable[count] == "updateobjective" then
			SF_MissionPanel.instance:updateObjective(commandTable[count + 1], tonumber(commandTable[count + 2]), commandTable[count + 3]);
			count = count + 4;
		elseif commandTable[count] == "updateobjectivetext" then
			SF_MissionPanel.instance:runCommand("updateobjectivetext", commandTable[count + 1], tonumber(commandTable[count + 2]),  commandTable[count + 3]);
			count = count + 4;		
		elseif commandTable[count] == "worldevent" or commandTable[count] == "unlockworldevent" then
			SF_MissionPanel.instance:runCommand("unlockworldevent", commandTable[count + 1], commandTable[count + 2], commandTable[count + 3]);	
			count = count + 4;
		else
			print("SOUL QUEST SYSTEM - Unrecognized command from a command table: " .. commandTable[count]);
			return
		end
	end
end

function SF_MissionPanel:runCommand(command, param1, param2, param3)
	if SF_MissionPanel.Commands[command] then
		SF_MissionPanel.Commands[command](param1, param2, param3)
	else
		print("SOUL QUEST SYSTEM - Unknown command: " .. command);
	end
end

---------------------------------------------------------------------------------------------------------
-- Mission Panel internal functions

function SF_MissionPanel:prerender()
    ISPanelJoypad.prerender(self);

	if self.needsUpdate == true then
		self:setLists();
		self.needsUpdate = false;
	end
end

function SF_MissionPanel:triggerUpdate()
	self.needsUpdate = true;
end

function SF_MissionPanel:render()
    ISPanelJoypad.render(self);

	if self.richText:getIsVisible() then
		self.richText:paginate();
	end
	
    self:setWidthAndParentWidth(400);
    self:setHeightAndParentHeight(self.height);
end

function SF_MissionPanel:update()
    ISPanelJoypad.update(self);
end

--[[
function SF_MissionPanel:setJoypadButtons()
    self:clearJoypadFocus()
    self.joypadButtonsY = {}
    self.joypadIndexY = 1
    self.joypadIndex = 1
    for _,ui in ipairs(self.viewButtons[self.currentViewID]) do
        self:insertNewLineOfButtons(ui)
    end
    self:insertNewLineOfButtons(self.toggleAdvBtn)
    self:restoreJoypadFocus()
end

function SF_MissionPanel:onGainJoypadFocus(joypadData)
    ISPanelJoypad.onGainJoypadFocus(self, joypadData)
    self:setJoypadButtons()
end

function SF_MissionPanel:onLoseJoypadFocus(joypadData)
    ISPanelJoypad.onLoseJoypadFocus(self, joypadData)
end
]]--
function SF_MissionPanel:onJoypadDown(button, joypadData)
    if button == Joypad.BButton then
        getPlayerInfoPanel(self.playerNum):toggleView(xpSystemText.clothingIns);
        setJoypadFocus(self.playerNum, nil);
        return;
    end
    if button == Joypad.LBumper then
        getPlayerInfoPanel(self.playerNum):onJoypadDown(button, joypadData);
        return;
    end
    if button == Joypad.RBumper then
        getPlayerInfoPanel(self.playerNum):onJoypadDown(button, joypadData);
        return;
    end
    ISPanelJoypad.onJoypadDown(self, button, joypadData);
end

function SF_MissionPanel:new(player, x, y, width, height)
    local o = {};
    o = ISPanelJoypad:new(x, y, width, height);
    o:noBackground();
    setmetatable(o, self);
    self.__index = self;
    o.player = player;
    o.playerNum = player:getPlayerNum();
    o.refreshNeeded = true;
	o.needsBackup = false;
	o.needsUpdate = false;
    o.bFemale = o.player:isFemale();
    o.borderColor = {r=0.4, g=0.4, b=0.4, a=1};
    o.backgroundColor = {r=0, g=0, b=0, a=0.8};
	o.expanded = nil;
	o.lore = {};
	o.loretitle = "???";
	o.currentPage = 1;
	SF_MissionPanel.instance = o;
    return o;
end