--local swp = require "Trd_spawnPoint"
local FINISH = false;
local DayCount = 0;

local function sign(num)
  if num >= 0 then
      return 1;
    else return -1;
    end
  end

local function Trd_new()
    randspawnTime = ZombRand(9, 21);
    chance = ZombRand(1, 100);
    clientSpawn = 1;
    needOneitem = 1;
    --getspawnChance = 100;
    markers = nil;
    getitem = nil;
    viewTrade = nil;
    detailview2 = nil;
    usespawnerview = nil;
    soundplay = false;
    starttimer = 0;
    delay = 1;
    playhey = nil;
    playM = nil;
    playTalk = nil;
    playspawnhunter = nil;
    playUs = nil;
    map = nil;
    salefood = 0;
    trashfood = 0;
    spawnZx = 0;
    spawnZy = 0;
    z = nil;
    zitem = nil;
    rtime = 0;
    timerr = 0;
    findwalkie = false;
    helper = false;
    zom = nil;
    CHECK_START = false;
    createItems = {};
end  
  
local function Trd_talking()
  if findwalkie and delay < 6 then
    if not playTalk:isPlaying() then
        playTalk = getSoundManager():PlaySound("talking",false,0);
        getSoundManager():PlayAsMusic("talking",playTalk,false,0);
        playTalk:setVolume(1);
    end
    local px = getPlayer():getX();
    local py = getPlayer():getY();
    local zx = spawnZx;
    local zy = spawnZy;
    local distance = IsoUtils.DistanceTo(px, py, zx, zy);
    local only = false;
    local direction = "";
  
    if py > zy then
       direction = getText("IGUI_PlayerText_TRD1");
    end
    if py < zy then
       direction = getText("IGUI_PlayerText_TRD2");
    end
    if px-21 < zx and px+21 > zx then
       only = true
    end
    if py-21 < zy and py+21 > zy then
       direction = "";
    end
    if px < zx and not only then
       direction = direction .. getText("IGUI_PlayerText_TRD3");
    end
    if px > zx and not only then
       direction = direction .. getText("IGUI_PlayerText_TRD4");
    end
    if direction == "" then
       direction = getText("IGUI_PlayerText_TRD5");
    end
  
      local talkingList = {
      getText("IGUI_PlayerText_TRDTalk1"),
      getText("IGUI_PlayerText_TRDTalk2"),
      getText("IGUI_PlayerText_TRDTalk3"),
      getText("IGUI_PlayerText_TRDTalk4") .. tostring(round(getPlayer():getX(),0)) .. "," .. tostring(round(getPlayer():getY(),0)),
      getText("IGUI_PlayerText_TRDTalk5") .. tostring(round(distance,0)) .. getText("IGUI_PlayerText_TRDTalk6") .. direction
      };
    if getPlayer():getSayLine() == nil then
       getPlayer():Say(talkingList[delay]);
       delay = delay + 1;
       if delay > 5 then 
         playTalk:stop(); 
         playUs = getSoundManager():PlaySound("LastOfUs",false,0);
         getSoundManager():PlayAsMusic("LastOfUs",playUs,false,0);
         playUs:setVolume(0.5);
       end
    end
  end
end


local function Trd_reset()
  if z ~= nil then
    if isClient() then
      SendCommandToServer(string.format("/removezombies -x %d -y %d -z %d -radius %d", z:getX(), z:getY(), z:getZ(), 1))
      getSandboxOptions():getOptionByName("TradeHunter.getHunterSurvive"):setValue(2);
      getSandboxOptions():sendToServer();
      getSandboxOptions():getOptionByName("TradeHunter.setTarget"):setValue("");
      getSandboxOptions():sendToServer();
    else
      z:removeFromWorld();
      z:removeFromSquare();
    end
  end
    if viewTrade ~= nil then viewTrade:clear(); end
      Trd_new();
      FINISH  = true;
end

  local function Trd_spawnHunter(x, y)
   if markers ~= nil then markers:clear(); end
   if isClient() then 
    if clientSpawn == 1 then
     sendClientCommand(getPlayer(), "Trd_module", "spawnHunter", {zx = x, zy = y});
     clientSpawn = 0;
    end
    getSandboxOptions():getOptionByName("TradeHunter.getHunterSurvive"):setValue(1);
    getSandboxOptions():sendToServer();
    local zs = getCell():getZombieList()
    local sz = zs:size()
     for i = 0, sz - 1 do
       local zz = zs:get(i)
       local outfit = zz:getOutfitName();
       if outfit == "HockeyPsycho" then
         z = zz;
         break;
       end
     end
  --[[
     if clientSpawn == 1 then
     SendCommandToServer(string.format("/createhorde2 -x %d -y %d -z %d -count %d -radius %d -crawler %s -isFallOnFront %s -isFakeDead %s -knockedDown %s -health %s -outfit %s ", x, y, 0, 1, 1, "false", "false", "false", "false", "10", "HockeyPsycho"))
     clientSpawn = 0;
     end]]
   else 
    local zA = addZombiesInOutfit(x,y,0,1,"HockeyPsycho",0,false,false,false,false,10);
    z = zA:get(0);
   end
   if z ~= nil then
     local outfit = z:getOutfitName();
     if outfit ~= "HockeyPsycho" then
        z:dressInNamedOutfit("HockeyPsycho");
     end
     getPlayer():Say(outfit);
     zitem = z:getInventory():AddItem("HuntingKnife")
     z:setTarget(z);
     z:playSound("over-here")
   end
   --getPlayer():Say("SPAWN HUNTER!");
   if playhey ~= nil then playhey:stop(); end
  end
  
  local function Trd_OnClientCommand(module, command, player, args)
    if module ~= "Trd_module" then return end
    if command == "explode" then
      local sq = player:getCurrentSquare();
      local item = InventoryItemFactory.CreateItem("Base.PipeBomb")
      local trap = IsoTrap.new(item, getCell(), sq)
         trap:setExplosionPower(100);
         trap:setExplosionRange(5);
         trap:setFireRange(5);
         trap:setFirePower(100);
         trap:setNoiseRange(100);
         trap:setExtraDamage(100);
       sq:AddTileObject(trap)	
    end
    if command == "spawnHunter" then
      addZombiesInOutfit(args["zx"],args["zy"],0,1,"HockeyPsycho",0,false,false,false,false,10);
    end
  end
  
  Events.OnClientCommand.Add(Trd_OnClientCommand)

  local function Trd_hitHunter(zombie)
    local suicidebombing = getSandboxOptions():getOptionByName("TradeHunter.EnableSuicide"):getValue();
    if z ~= nil and zombie == z then
      --z:setGodMod(true);
      if suicidebombing then
       z:playSound("stranger");
       z:addLineChatElement(getText("IGUI_PlayerText_TRDkill"),204,0,0)
       z:setX(getPlayer():getX());
       z:setY(getPlayer():getY());
       local sq = getPlayer():getCurrentSquare();
       --local item = InventoryItemFactory.CreateItem("Base.PipeBomb")
       local item = z:getInventory():AddItem("Base.PipeBomb")
       local trap = IsoTrap.new(item, getCell(), sq)
         trap:setExplosionPower(100);
         trap:setExplosionRange(5);
         trap:setFireRange(5);
         trap:setFirePower(100);
         trap:setNoiseRange(100);
         trap:setExtraDamage(100);
       sq:AddTileObject(trap)	
       if isClient() then sendClientCommand(getPlayer(), "Trd_module", "explode", nil); end
       else 
        z:addLineChatElement(getText("IGUI_PlayerText_TRDdie"),204,0,0)
        local getDayCycle = getSandboxOptions():getOptionByName("TradeHunter.SpawnHunterDay"):getValue();
        local randDayCycle = tonumber(getDayCycle) + ZombRand(1, 3);
        getSandboxOptions():getOptionByName("TradeHunter.SpawnHunterDay"):setValue(randDayCycle);
        getSandboxOptions():sendToServer();
      end

      if playUs ~= nil then playUs:stop(); end
      if usespawnerview ~= nil and usespawnerview:getIsVisible() then
        usespawnerview:setVisible(false);
        usespawnerview:removeFromUIManager();
      end
      if viewTrade ~= nil then viewTrade:clear(); end
      --Trd_reset();
      if isClient() then
       getSandboxOptions():getOptionByName("TradeHunter.getHunterSurvive"):setValue(2);
       getSandboxOptions():sendToServer();
       getSandboxOptions():getOptionByName("TradeHunter.setTarget"):setValue("");
       getSandboxOptions():sendToServer();
      end
      Trd_new();
      FINISH = true;
    end
  end
  
  local function Trd_angryHunter()
    
    if trashfood > 2 then
      z:setPrimaryHandItem(zitem) 
      z:addLineChatElement(getText("IGUI_PlayerText_TRDjoke"),204,0,0)
      getPlayer():playSound("splat2")
      randattack = ZombRand(1,3);
      z:setX(getPlayer():getX());
      z:setY(getPlayer():getY());
      for i=1, randattack do
        local bodyParts = getPlayer():getBodyDamage():getBodyParts();
        local randi = ZombRand(1, BodyPartType.ToIndex(BodyPartType.MAX));
        local bodyPart = bodyParts:get(randi);
        bodyPart:setDeepWounded(true);
      end
      Trd_reset();
      trashfood = 0;
      playUs:stop();
     end
  end
  
  local function Trd_helpHunter2(zombie, character, bodyPartType, handWeapon)
   
   if z ~= nil and zombie ~= z then
    helper = true;
    zom = zombie;
   end
   if z ~= nil and z == zombie then
    local rand = ZombRand(1,3)
    if not z:isDead() then z:playSound("ahh" .. rand); end
       --z:setTarget(z);
      --z:clearAggroList();
      --z:setForceEatingAnimation(false);
   end
  end
  Events.OnHitZombie.Add(Trd_helpHunter2)
  
  local function Trd_helpHunter()
    
    if z ~= nil and helper then
      if usespawnerview ~= nil then
       z:setPrimaryHandItem(zitem) 
       local zs = getCell():getZombieList()
       local sz = zs:size()
        for i = 0, sz - 1 do
         local zz = zs:get(i)
         local distance = IsoUtils.DistanceTo(z:getX(), z:getY(), zz:getX(), zz:getY());
         if distance < 6 and zz ~= z then
           z:setUseless(false);
           z:setX(zz:getX());
           z:setY(zz:getY());
           --z:setForceEatingAnimation(true);
           --z:playSound("splat2")
           zz:applyDamageFromVehicle(10,10);
           z:pathToCharacter(getPlayer());
         end
        end
       else
       local distance = IsoUtils.DistanceTo(z:getX(), z:getY(), getPlayer():getX(), getPlayer():getY());
       if distance < 7 then
       local backVecXX = getPlayer():getX() + (getPlayer():getX() - zom:getX());
       local backVecYY = getPlayer():getY() + (getPlayer():getY() - zom:getY());
       local backVecX = sign(getPlayer():getX() - zom:getX())*3;
       local backVecY = sign(getPlayer():getY() - zom:getY())*3;
       local backVecXXX = backVecXX + backVecX;
       local backVecYYY = backVecYY + backVecY;
       local dis = IsoUtils.DistanceTo(z:getX(), z:getY(), backVecXXX, backVecYYY);
       --getWorldMarkers():addDirectionArrow(getPlayer(), backVecXXX, backVecYYY, getPlayer():getZ(), "arrow", 255, 0, 0, 0.5);
       --local vec2 = Vector2.new(backVecXXX, backVecYYY)
       if dis > 6 then
       z:setX(backVecXXX);
       z:setY(backVecYYY);
       z:setSitAgainstWall(true)
       end
       --z:Move(vec2)
       --getSandboxOptions():set("ZombieLore.Speed", 1);
       --z:makeInactive(true)
       --z:makeInactive(false)
     end
   end
      helper = false;
   end
  end
  
  local function Trd_cureHunter()
    
    if timerr > 17 then
    --getSandboxOptions():set("ZombieLore.Speed", 1);
    --z:makeInactive(true)
    --z:makeInactive(false)
    --local vec2 = Vector2.new(getPlayer():getX(), getPlayer():getY());
    --z:Move(vec2);
    z:setUseless(false);
    z:pathToCharacter(getPlayer());
    local item = z:getInventory():AddItem("AlcoholBandage")
    local bodyParts = getPlayer():getBodyDamage():getBodyParts()
    for i=1, bodyParts:size() do
        local bP = bodyParts:get(i-1)
        bP:RestoreToFullHealth();
        --local apply = ISApplyBandage:new(getPlayer(), getPlayer(), item, bP, true)
        --apply.maxTime = 10;
        --ISTimedActionQueue.add(apply)
        if bP:getStiffness() > 0 then
            bP:setStiffness(0)
            getPlayer():getFitness():removeStiffnessValue(BodyPartType.ToString(bP:getType()))
        end
     end
     if usespawnerview ~= nil and usespawnerview:getIsVisible() then
      usespawnerview:setVisible(false);
      usespawnerview:removeFromUIManager();
     end
     Trd_reset();
     if playUs ~= nil then playUs:stop(); end
    else z:addLineChatElement(getText("IGUI_PlayerText_TRDnotCure"),204,0,0)
    end
  end
  
  local function Trd_updateHunter()
    if usespawnerview == nil then
     local cal = Calendar.getInstance();
     cal:setTimeInMillis(Calendar.getInstance():getTimeInMillis() - starttimer)
     --local sec = cal2:get(Calendar.SECOND);
     local min = cal:get(Calendar.MINUTE);
     local getStay = getSandboxOptions():getOptionByName("TradeHunter.SpawnHunterStayTime"):getValue();
     if min >= getStay then
       if playUs ~= nil then playUs:stop(); end
       if playhey ~= nil then playhey:stop(); end
       if markers ~= nil then markers:clear(); end
       Trd_reset();
     end
    end
    if z ~= nil then
      if usespawnerview ~= nil then
       local cal2 = Calendar.getInstance();
       cal2:setTimeInMillis(Calendar.getInstance():getTimeInMillis() - rtime)
       local min = cal2:get(Calendar.MINUTE);
       timerr = 20 - min;
       usespawnerview.description = getText("UI_rtime2") .. tostring(timerr) .. getText("UI_rtime3")
       if timerr == 0 then
         usespawnerview.setVisible(false);
         usespawnerview:removeFromUIManager();
         Trd_reset();
         Events.OnTick.Remove(Trd_updateHunter);
       end
      end 
  
      if getPlayer():isSitOnGround() then
        z:setSitAgainstWall(true)
      end
      if playUs ~= nil and not playUs:isPlaying() then
        playUs = getSoundManager():PlaySound("LastOfUs",false,0);
        getSoundManager():PlayAsMusic("LastOfUs",playUs,false,0);
        playUs:setVolume(0.5);
      end
  
      local obj = TextDrawObject.new();
      if getPlayer():CanSee(z) then
       local sx = IsoUtils.XToScreen(z:getX(), z:getY(), z:getZ(), 0);
       local sy = IsoUtils.YToScreen(z:getX(), z:getY(), z:getZ(), 0);
       sx = sx - IsoCamera.getOffX() - z:getOffsetX();
       sy = sy - IsoCamera.getOffY() - z:getOffsetY();
       sy = sy - 180;
       sx = sx / getCore():getZoom(0)
       sy = sy / getCore():getZoom(0)
       obj:setDefaultColors(153, 0, 0)
       obj:setOutlineColors(153, 0, 0)
       obj:ReadString(UIFont.Medium, "Hunter", -1)
       obj:AddBatchedDraw(sx, sy, true)
      end

      
      --getSandboxOptions():set("ZombieLore.Memory", 4);

      z:setUseless(true);
      z:setCanWalk(true);
      z:clearAggroList();
      Trd_angryHunter()
      Trd_helpHunter()

      local target = getSandboxOptions():getOptionByName("TradeHunter.setTarget"):getValue();
      local getHunterSurvive = getSandboxOptions():getOptionByName("TradeHunter.getHunterSurvive"):getValue();
      if getHunterSurvive == 2 and isClient() and target ~= getPlayer():getUsername() then
        if playUs ~= nil then playUs:stop(); end
         Trd_reset();
      end
    end
  end
  
 local function Trd_start()

    local target = getSandboxOptions():getOptionByName("TradeHunter.setTarget"):getValue();
    local serverplayerszx = getSandboxOptions():getOptionByName("TradeHunter.SpawnHunterX"):getValue();
    local serverplayerszy = getSandboxOptions():getOptionByName("TradeHunter.SpawnHunterY"):getValue();
    randspawnTime = getSandboxOptions():getOptionByName("TradeHunter.SpawnHunterTime"):getValue(); --향후 삭제 필요
    if not isClient() then randspawnTime = getSandboxOptions():getOptionByName("TradeHunter.SpawnHunterTime"):getValue(); end

    if not FINISH then
      if target == getPlayer():getUsername() then
         if spawnZx == 0 and spawnZy == 0 then
           local px = getPlayer():getX();
           local py = getPlayer():getY();
           spawnZx = px + ZombRand(-300, 300);
           spawnZy = py + ZombRand(-300, 300);
           getSandboxOptions():getOptionByName("TradeHunter.SpawnHunterX"):setValue(spawnZx);
           getSandboxOptions():sendToServer();
           getSandboxOptions():getOptionByName("TradeHunter.SpawnHunterY"):setValue(spawnZy);
           getSandboxOptions():sendToServer();
         end
         if serverplayerszx ~= spawnZx and serverplayerszy ~= spawnZy then
           getSandboxOptions():getOptionByName("TradeHunter.SpawnHunterX"):setValue(spawnZx);
           getSandboxOptions():sendToServer();
           getSandboxOptions():getOptionByName("TradeHunter.SpawnHunterY"):setValue(spawnZy);
           getSandboxOptions():sendToServer();
           getSandboxOptions():getOptionByName("TradeHunter.setTarget"):setValue(getPlayer():getUsername());
           getSandboxOptions():sendToServer();
           --getSandboxOptions():getOptionByName("TradeHunter.SpawnHunterTime"):setValue(randspawnTime);
           --getSandboxOptions():sendToServer();
         end
      end

      if target ~= getPlayer():getUsername() and isClient() then
        --getPlayer():Say("my name : " .. tostring(getPlayer():getUsername()) .. " and " .. tostring(target))
        --randspawnTime = getSandboxOptions():getOptionByName("TradeHunter.SpawnHunterTime"):getValue();
        spawnZx = getSandboxOptions():getOptionByName("TradeHunter.SpawnHunterX"):getValue();
        spawnZy = getSandboxOptions():getOptionByName("TradeHunter.SpawnHunterY"):getValue();
      end
   end

    if randspawnTime == getGameTime():getHour() then
      CHECK_START = true;
      randspawnTime = -1;
    end

    if FINISH then
      Events.OnTick.Remove(Trd_start);
      CHECK_START = false;
      FINISH = false;
    end

    if CHECK_START then
       Trd_talking()
       Trd_updateHunter()
       if not FINISH then
         if (viewTrade ~= nil and detailview2 ~= nil) and viewTrade:toView():getIsVisible() and detailview2:getIsVisible() then
           detailview2:destroy();
         end
       
         local distance = IsoUtils.DistanceTo(getPlayer():getX(), getPlayer():getY(), spawnZx, spawnZy);
       
         if distance < 8 and z == nil then
           if getPlayer():getUsername() == target or not isClient() then
             Trd_spawnHunter(spawnZx, spawnZy);
           end
           if getPlayer():getUsername() ~= target and isClient() then
             local zs = getCell():getZombieList()
             local sz = zs:size()
             for i = 0, sz - 1 do
               local zz = zs:get(i)
               local outfit = zz:getOutfitName();
               if outfit == "HockeyPsycho" then
                 z = zz;
                 if playhey ~= nil then playhey:stop(); end
                 break;
               end
             end
           end
         end
  
         if ISWorldMap_instance ~= nil and ISWorldMap_instance:isVisible() and findwalkie and map == nil then
           --player:Say(tostring(player:getX() .. "," .. tostring(player:getY())))
           map = ISWorldMap_instance.mapAPI;
           markers = map:getMarkersAPI()
           markers:addGridSquareMarker(spawnZx, spawnZy, 1, 204, 0, 0, 0.5)
           --local map = WorldMapMarkersV1.new(ISWorldMap);
         end
      
         if not soundplay then
           playM = getSoundManager():PlaySound("radio",false,0);
           getSoundManager():PlayAsMusic("radio",playM,false,0);
           --addSound(nil, z:getX(), z:getY(), z:getZ(), 100, 100)
           playM:setVolume(1);
           soundplay = true;
         end
  
         if playM ~= nil and not playM:isPlaying() then
           --getWorldMarkers():addPlayerHomingPoint(getPlayer(), z:getX(), z:getY())
           if playTalk == nil then
             playTalk = false;
             for i=4,5 do
               if getPlayer():getInventory():containsTypeRecurse("WalkieTalkie" .. tostring(i)) then
                 --player:Say("find walkie!")
                 findwalkie = true;
                 break;
               end
             end
             --local getGametimeToMinutes = getGameTime():getHour()*60 + getGameTime():getMinutes();
             if findwalkie then
               playTalk = getSoundManager():PlaySound("talking",false,0);
               getSoundManager():PlayAsMusic("talking",playTalk,false,0);
               playTalk:setVolume(1);
             end
           end
        
           if distance < 100 and z == nil and playTalk ~= nil then    
             if playhey == nil then
               playhey = getSoundManager():PlaySound("hey",false,0);
               getSoundManager():PlayAsMusic("hey",playhey,false,0);
               addSound(nil, spawnZx, spawnZy, 0, 100, 100)
             end
  
             if playhey ~= nil and not playhey:isPlaying() then
               playhey = getSoundManager():PlaySound("hey",false,0);
               getSoundManager():PlayAsMusic("hey",playhey,false,0);
               addSound(nil, spawnZx, spawnZy, 0, 100, 100)
               playhey:setVolume(0.1 + ((100-distance)*0.01));
             end
           end
         end 
       end
    end
end
  
local function Trd_init()
  DayCount = DayCount + 1;
  getSandboxOptions():getOptionByName("TradeHunter.getHunterSurvive"):setValue(2);
  getSandboxOptions():sendToServer();
  local getDayCycle = getSandboxOptions():getOptionByName("TradeHunter.SpawnHunterDay"):getValue();
  if DayCount == getDayCycle then
    Trd_new();
    local getspawnChance = getSandboxOptions():getOptionByName("TradeHunter.SpawnHunterChance"):getValue();
    local targetplayer = getSandboxOptions():getOptionByName("TradeHunter.setTarget"):getValue();
    if targetplayer == "" and isClient() then 
      getSandboxOptions():getOptionByName("TradeHunter.setTarget"):setValue(getPlayer():getUsername());
      getSandboxOptions():sendToServer();
    end
    if getPlayer():getUsername() == targetplayer or not isClient() then
    getSandboxOptions():getOptionByName("TradeHunter.SpawnHunterTime"):setValue(randspawnTime);
    getSandboxOptions():sendToServer();
    end
    if chance <= getspawnChance then
       player = getPlayer();
       if starttimer == 0 then starttimer = Calendar.getInstance():getTimeInMillis(); end

       if getPlayer():getUsername() == targetplayer or not isClient() then
         --getPlayer():setZombieKills(300);
         --getPlayer():setGodMod(true);
         local px = getPlayer():getX();
         local py = getPlayer():getY();
         spawnZx = px + ZombRand(-300, 300);
         spawnZy = py + ZombRand(-300, 300);
         getSandboxOptions():getOptionByName("TradeHunter.SpawnHunterX"):setValue(spawnZx);
         getSandboxOptions():sendToServer();
         getSandboxOptions():getOptionByName("TradeHunter.SpawnHunterY"):setValue(spawnZy);
         getSandboxOptions():sendToServer();
       end

       Events.OnTick.Add(Trd_start);
    end
    DayCount = 0;
  end
end
Events.EveryDays.Add(Trd_init)
--Events.OnGameStart.Add(Trd_init)
  
  local function Trd_tradeitem(dum, button, items)
      if button.internal == nil then return end
      if button.internal == "EXIT" and detailview2 ~= nil then
        detailview2:destroy();
      end
  end
  
  local function Trd_trade()
      local player = getPlayer();
      local rand = ZombRand(1,7);
      rand = 7;
      player:Say("Trade!");
      local x = getPlayerScreenLeft(player:getPlayerNum()) + (getPlayerScreenWidth(player:getPlayerNum()) - 500) / 2
      local y = getPlayerScreenTop(player:getPlayerNum()) + (getPlayerScreenHeight(player:getPlayerNum()) - 500) / 2
      viewTrade = TradeUI:new(x, y, 500, 530, getText("UI_trade"), nil, Trd_tradeitem, player:getPlayerNum(), rand);
      viewTrade:initialise();
      viewTrade:addToUIManager();
  end
  
  local function Trd_saleFood(dum, button, item)
    --getPlayer():Say("find food button!")
    if salefood > 0 then
      local player = getPlayer();
      local befkill = player:getZombieKills();
      local item = player:getInventory():FindAndReturn(getitem:getType());
      if item ~= nil then
      player:setZombieKills(befkill + salefood);
      player:getInventory():Remove(item);
      viewTrade.text = getText("UI_food1") .. tostring(salefood)
      viewTrade.Tip.description = player:getZombieKills() .. "C";
      trashfood = 0;
      --player:playSound("YUMMY");
      player:playSound("thank-you");
      end
     else viewTrade.text = getText("UI_food2")
    end
    detailview2:destroy();
  end
  
  local function Trd_sale()
    --if viewTrade.detailview ~= nil then viewTrade.detailview:destroy(); end
    local dview = viewTrade:toView();
    local tview = viewTrade:toView2();
    if dview ~= nil then dview:destroy(); end
    if tview ~= nil then 
      tview:setVisible(false); 
      tview:removeFromUIManager();
    end
    if detailview2 ~= nil then detailview2:destroy(); end
    --getPlayer():Say(tostring(getitem:getIcon()))
    local player = getPlayer();
    local iitem = player:getInventory():FindAndReturn(getitem:getType());
    --local item = iitem:getScriptItem();
    local Evaluation = "";
    salefood = 0;
  
    if iitem ~= nil then
    --음식 감정 알고리즘--
    if iitem:getHungerChange() > 0 and iitem:getThirstChange() > 0 and iitem:getUnhappyChange() > 0 then
      Evaluation = getText("UI_eatfood1")
      trashfood = trashfood + 1;
      if iitem:getBoredomChange() > 0 or iitem:getEnduranceChange() > 0 or iitem:getStressChange() > 0 then
        Evaluation = Evaluation .. "\n" .. getText("UI_eatfood2")
     end
    end
    if iitem:getHungerChange() < 0 and (iitem:getThirstChange() > 0 or iitem:getUnhappyChange() > 0) then Evaluation = getText("UI_eatfood10") end
    if iitem:getHungerChange() < 0 and (iitem:getThirstChange() <= 0 or iitem:getUnhappyChange() <= 0) then
      if (iitem:getThirstChange() == 0 and iitem:getUnhappyChange() < 0) or (iitem:getThirstChange() < 0 and iitem:getUnhappyChange() == 0) then
       Evaluation = getText("UI_eatfood11")
       salefood = ZombRand(1,5);
      end
    end
    if iitem:getHungerChange() < 0 and iitem:getThirstChange() == 0 and iitem:getUnhappyChange() == 0 then
       Evaluation = getText("UI_eatfood11")
       salefood = ZombRand(1,5);
    end
    if iitem:getHungerChange() < 0 and iitem:getThirstChange() < 0 and iitem:getUnhappyChange() < 0 then
      local transHunger = iitem:getHungerChange()*-100;
      getPlayer():playSound("buy-it")
      --player:Say(tostring(transHunger))
      if transHunger > 100 then 
        Evaluation = getText("UI_eatfood3")
        Evaluation = Evaluation .. "\n" .. getText("UI_eatfood4")
        salefood = 100;
      else 
        Evaluation = getText("UI_eatfood5")
        salefood = ZombRand(1,transHunger);
        if not iitem:isCooked() then
        Evaluation = getText("UI_eatfood6")
        salefood = ZombRand(1,10);
        end
      end
    end
    if iitem:isbDangerousUncooked() and not iitem:isCooked() and not iitem:isBurnt() then
      Evaluation = getText("UI_eatfood7")
      salefood = 0;
    end
    if iitem:isBurnt() then
      Evaluation = Evaluation .. "\n" .. getText("UI_eatfood8")
    end
    if iitem:isBadCold() and not iitem:isBurnt() then
      Evaluation = Evaluation .. "\n" .. getText("UI_eatfood9")
    end
    end
  
    --getPlayer():Say(item:getDisplayName())
    --local texture = getitem:getIconsForTexture():get(0);
    local x = getPlayerScreenLeft(player:getPlayerNum()) + (getPlayerScreenWidth(player:getPlayerNum()) - 500) / 2
    local y = getPlayerScreenTop(player:getPlayerNum()) + (getPlayerScreenHeight(player:getPlayerNum()) - 500) / 2
    detailview2 = DetailsUI:new(x + 500, y, 350, 530, "[" .. iitem:getName() .. "]" .. "\n\n" .. Evaluation, nil, Trd_saleFood, getPlayer():getPlayerNum(), iitem, 2, "")
    detailview2:initialise();
    detailview2:addToUIManager();
  end
  
  local function Trd_UseSpawnHunter()
    local plInv = getPlayer():getInventory();
    
    if plInv:containsTypeRecurse("tradeHunter.SpawnHunter") and needOneitem == 1 then
      local iitem = plInv:FindAndReturn("tradeHunter.SpawnHunter");
      plInv:DoRemoveItem(iitem);
      needOneitem = 0;
    end

    if usespawnerview == nil then
      usespawnerview = ISToolTip:new();
      usespawnerview:initialise();
      usespawnerview:addToUIManager();
      usespawnerview:setOwner(self);
      usespawnerview:setX(140);
      usespawnerview:setY(0);
      usespawnerview.followMouse = false;
     --toolTip:setName(getItem[selected]:getDisplayName())
      usespawnerview.description = getText("UI_rtime1")
      local titem = InventoryItemFactory.CreateItem("tradeHunter.SpawnHunter");
      local tex = titem:getTex():getName();
      usespawnerview:setTexture(tex);
      usespawnerview:doLayout();
      rtime = Calendar.getInstance():getTimeInMillis();
    end
    if z ~= nil then
     z:setX(getPlayer():getX())
     z:setY(getPlayer():getY()+3)
    else 
    if playspawnhunter == nil then
      playspawnhunter = getSoundManager():PlaySound("radio",false,0);
      getSoundManager():PlayAsMusic("radio",playspawnhunter,false,0);
      playspawnhunter:setVolume(1);
      Events.OnTick.Add(Trd_UseSpawnHunter);
    end
    if not playspawnhunter:isPlaying() then 
      Trd_spawnHunter(getPlayer():getX(),getPlayer():getY()+3)
      if z ~= nil then
      Events.OnTick.Add(Trd_updateHunter);
      starttimer = Calendar.getInstance():getTimeInMillis();
      trashfood = 0;
      playspawnhunter = nil;
      Events.OnTick.Remove(Trd_UseSpawnHunter);
      end
    end
   end
  end

  local user = "";
  local function Trd_OnScoreboardUpdate(usernames, displayNames, steamIDs)
    user = usernames;
  end
  Events.OnScoreboardUpdate.Add(Trd_OnScoreboardUpdate)


  local function Trd_playAnim()
    local invitem = nil;
    plInv = getPlayer():getInventory();
    if not plInv:containsTypeRecurse("HuntingKnife") then
     invitem = getPlayer():getInventory():AddItem("HuntingKnife")
    else invitem = plInv:FindAndReturn("HuntingKnife");
    end
    local item = getScriptManager():FindItem("HuntingKnife");
    --getPlayer():setPrimaryHandItem(invitem)
    --EquipAction:start();
    if getPlayer():isEquipped(invitem) then 
      --for i=1,user:size() do
      --  local username = user:get(i-1)
      --getSandboxOptions():getOptionByName("TradeHunter.getHunterSurvive"):setValue(1);
      
      getPlayer():Say("server name : " .. getServerName())
      getPlayer():Say("save name : " .. tostring(getWorld():getWorld()))
      if z ~= nil then getPlayer():Say("get zombie : " .. tostring(z:getOutfitName())) end
      getPlayer():Say("zx,zy : " .. tostring(spawnZx) .. "," .. tostring(spawnZy))
      getPlayer():Say("find target : " .. tostring(getSandboxOptions():getOptionByName("TradeHunter.setTarget"):getValue()))
     -- Trd_init();
      --end
      getPlayer():setGodMod(true)
      --if TradeUI.list ~= nil then TradeUI:clear(); end
      Trd_new();
      Trd_spawnHunter(getPlayer():getX(), getPlayer():getY());
      Events.OnTick.Add(Trd_updateHunter);
    end
  end
  
  local function Trd_EditItems()
    local modal = ItemsListUI:new(50, 200, 850, 650, getPlayer())
    modal:initialise();
    modal:addToUIManager();
  end
  
  local function Trd_contxtmenuSet(player, context, worldObjects)
     --context:addOption("Anim", worldObjects,Trd_playAnim)
     --context:addOption(getText("UI_trade"), worldObjects,Trd_trade) 
     local getEdit = getSandboxOptions():getOptionByName("TradeHunter.setItemsList"):getValue();
      if getEdit == 1 and (isAdmin() or not isClient()) then
        context:addOption("EDIT ITEMSLIST", worldObjects,Trd_EditItems)
      end
      if z ~= nil then
        if not z:isDead() then 
         local distance = IsoUtils.DistanceTo(getPlayer():getX(), getPlayer():getY(), z:getX(), z:getY());
         if distance < 6 then
           --getPlayer():Say("find!");     
           getPlayer():playEmote("wavehi")
           --getWorldMarkers():removeAllHomingPoints(getPlayer());
           if usespawnerview == nil then 
            context:addOption(getText("UI_trade"), worldObjects,Trd_trade) 
            z:addLineChatElement(getText("IGUI_PlayerText_TRDWelcome"),204,0,0)
            z:playSound("welcome");
           elseif usespawnerview:getIsVisible() then
            context:addOption(getText("UI_cure"), worldObjects,Trd_cureHunter)
            z:addLineChatElement(getText("IGUI_PlayerText_TRDHelp"),204,0,0)
            z:playSound("need");
           end
           z:setUseless(false);
           z:setSitAgainstWall(false);
           --local vec2 = Vector2.new(getPlayer():getX(), getPlayer():getY());
           --z:Move(vec2);
           --getSandboxOptions():set("ZombieLore.Speed", 1);
           --z:makeInactive(true)
           z:pathToCharacter(getPlayer());
           --z:makeInactive(false)
           --getSandboxOptions():set("ZombieLore.Speed", 2);
           --z:setWalkType("WTSprint");
           --getSandboxOptions():set("ZombieLore.Cognition", 1)
           --z:DoZombieStats()
           if z:isEquipped(zitem) then 
             --ISTimedActionQueue.add(ISUnequipAction:new(z, zitem, 10));  
             z:setPrimaryHandItem(nil);
           end
         end
       end
     end
  end

  function Trd_InvContextMenu(plyer, context, items)
    local player = getSpecificPlayer(plyer);
    items = ISInventoryPane.getActualItems(items)
    for _, item in ipairs(items) do
        if viewTrade ~= nil and item:getDisplayCategory() == "Food" and viewTrade:getIsVisible() then
            local option = nil
                option = context:addOption(getText("UI_sale"), player, Trd_sale, item); 
                getitem = item;  
        end
        if item:getType() == "SpawnHunter" then
          --player:Say(item:getType());
          local option = nil
          option = context:addOption(getText("UI_help"), player, Trd_UseSpawnHunter, item); 
          needOneitem = 1;
        end
    end
  end  
  
  Events.OnZombieDead.Add(Trd_hitHunter);
  Events.OnFillInventoryObjectContextMenu.Add(Trd_InvContextMenu);
  Events.OnFillWorldObjectContextMenu.Add(Trd_contxtmenuSet)
  --Events.OnTick.Add(Trd_talking);
  --Events.OnTick.Add(Trd_updateHunter);
  