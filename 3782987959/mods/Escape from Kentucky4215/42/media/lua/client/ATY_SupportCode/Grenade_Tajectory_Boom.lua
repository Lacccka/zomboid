Grenade_Tajectory = Grenade_Tajectory or {}
Grenade_Tajectory = Grenade_Tajectory or {}
Grenade_Tajectory.table = {}
Grenade_Tajectory.boomtable = {}
Grenade_Tajectory.aimcursor = nil

local sampleBomb = nil

function Grenade_Tajectory.itemremove(worlditem)
    if worlditem == nil then
        return
    end
    -- worlditem:getWorldItem():getSquare():transmitRemoveItemFromSquare(worlditem:getWorldItem())
    worlditem:getWorldItem():removeFromSquare()
end
function Grenade_Tajectory.mathfloor(number)
    return number - math.floor(number)
end
function Grenade_Tajectory.additemsfx(square, itemname, x, y, z)
    if square:getZ() > 7 then
        return
    end
    local iteminv = instanceItem(itemname)
    local itemin = IsoWorldInventoryObject.new(iteminv, square, Grenade_Tajectory.mathfloor(x),
        Grenade_Tajectory.mathfloor(y), Grenade_Tajectory.mathfloor(z));
    iteminv:setWorldItem(itemin)
    square:getWorldObjects():add(itemin)
    square:getObjects():add(itemin)
    local chunk = square:getChunk()
    if chunk then
        square:getChunk():recalcHashCodeObjects()
    else
        return
    end
    return iteminv
end
function Grenade_Tajectory.twotable(table2)
    local table1 = {}
    for i, k in pairs(table2) do
        table1[i] = table2[i]
    end
    -- print(table1)
    return table1
end
function Grenade_Tajectory.checkiswallordoor(square, angle, postion, postion2, nosfx)
    local objects = square:getObjects()
    if objects then
        for i = 1, objects:size() do
            local locobject = objects:get(i - 1)
            local sprite = locobject:getSprite()
            if sprite then
                local Properties = sprite:getProperties()
                if Properties then
                    if instanceof(locobject, "IsoWindow") and not locobject:isSmashed() then
                        if nosfx then
                            return true
                        end
                        locobject:setSmashed(true)
                        getSoundManager():PlayWorldSoundWav("SmashWindow", square, 0.5, 2, 0.5, true);
                        return true
                    end
                    local intdel = 0.25
                    if Properties:Is(IsoFlagType.WallNW) then
                        if postion[2] < square:getY() + intdel and postion[1] < square:getX() + intdel then
                            if nosfx then
                                return true
                            end
                            getSoundManager():PlayWorldSoundWav("BreakObject", square, 0.5, 2, 0.5, true);
                            return true
                        end
                    elseif Properties:Is(IsoFlagType.WallN) or
                        (Properties:Is(IsoFlagType.doorN) and not locobject:IsOpen()) then
                        if postion[2] < square:getY() + intdel then
                            if nosfx then
                                return true
                            end
                            getSoundManager():PlayWorldSoundWav("BreakObject", square, 0.5, 2, 0.5, true);
                            return true
                        end
                    elseif Properties:Is(IsoFlagType.WallW) or
                        (Properties:Is(IsoFlagType.doorW) and not locobject:IsOpen()) then
                        if postion[1] < square:getX() + intdel then
                            if nosfx then
                                return true
                            end
                            getSoundManager():PlayWorldSoundWav("BreakObject", square, 0.5, 2, 0.5, true);
                            return true
                        end
                    end
                end
            end
        end
    end
end
function Grenade_Tajectory.boomontick()
    local tablenow = Grenade_Tajectory.boomtable
    for kt, vt in pairs(tablenow) do
        print(vt[7])
        for kz, vz in pairs(vt[12]) do
            Grenade_Tajectory.itemremove(vt[12][vt[3] - vt[13]])
        end
        if vt[3] > vt[2] + vt[13] then
            tablenow[kt] = nil
            break
        end
        if vt[3] == 1 and vt[7] == 0 then
            local itemornone = Grenade_Tajectory.additemsfx(vt[5], vt[1] .. tostring(vt[3]), vt[4][1], vt[4][2],
                vt[4][3])
            table.insert(vt[12], itemornone)
            vt[3] = vt[3] + 1
        elseif vt[7] > vt[6] and vt[3] <= vt[2] then
            vt[7] = 0
            local itemornone = Grenade_Tajectory.additemsfx(vt[5], vt[1] .. tostring(vt[3]), vt[4][1], vt[4][2],
                vt[4][3])
            table.insert(vt[12], itemornone)
            vt[3] = vt[3] + 1
        elseif vt[7] > vt[6] then
            vt[7] = 0
            vt[3] = vt[3] + 1
        end
        vt[7] = vt[7] + getGameTime():getMultiplier()

    end
end
function Grenade_Tajectory.boomsfx(sq, sfxName, sfxNum, ticktime)
    -- print(sq)
    local sfxname = sfxName or "Base.theMH_MkII_SFX"
    local sfxnum = sfxNum or 12
    local nowsfxnum = 1
    local sfxcount = 0
    local pos = {sq:getX(), sq:getY(), sq:getZ()}
    local square = sq
    local ticktime = ticktime or 3.5
    local func = function()
        return
    end
    local varz1, varz2, varz3
    local item = {}
    local offset = 3
    local tablesfx = {sfxname, ---1
    sfxnum, ---2
    nowsfxnum, ---3
    pos, ---4
    square, ---5
    ticktime, ---6
    sfxcount, ---7
    func, ---8
    varz1, ---9
    varz2, ---10
    varz3, ---11
    item, ---12
    offset ---13滞后
    }

    table.insert(Grenade_Tajectory.boomtable, tablesfx)
end
Grenade_Tajectory.damagedisplayer = {}
function Grenade_Tajectory.checkontick()
    Grenade_Tajectory.boomontick()
end
Events.OnTick.Add(Grenade_Tajectory.checkontick)

local list = {}

local function KillReachableSquaresZombie(square, range, Damage, ExplosionSound)
    if not square then
        return
    end
    if range == nil then
        range = 1
    end
    local cx = square:getX()
    local cy = square:getY()
    local cz = square:getZ()
    local dx, dy = 0, 0
    local currentSq = square
    for dy = 0 - range, range do
        for dx = 0 - range, range do
            local square = getCell():getGridSquare(cx + dx, cy + dy, cz)
            if square ~= currentSq and currentSq and currentSq:isBlockedTo(square) then
                square = nil -- 不读取被隔开的区块
            end
            if square then
                local Zombies = square:getMovingObjects()
                for i = 0, Zombies:size() - 1 do
                    local zombie = Zombies:get(i)
                    if instanceof(zombie, "IsoZombie") then
                        local distance = math.sqrt((dx * dx) + (dy * dy))
                        local scaledDamage = Damage * (math.abs((range - distance)) / range) * 2
                        local newHealth = zombie:getHealth() - scaledDamage
                        table.insert(list, zombie)
                        if newHealth < 0 then
                            newHealth = 0
                        end


                    end
                end
            end
        end
    end
    local sound = getSoundManager():PlayWorldSound(ExplosionSound, square, 0, 4, 1.0, false);
    sound:setVolume(0.7);
end


function Grenade_Tajectory.Boom(sq, GrenadeTypeInfo)
    KillReachableSquaresZombie(sq, GrenadeTypeInfo.ExplosionRange, GrenadeTypeInfo.ExplosionDamage,
        GrenadeTypeInfo.ExplosionSound)
    --Grenade_Tajectory.boomsfx(sq)

    triggerEvent("OnThrowableExplode", nil, sq)
end

local function OnKillingZombieRagdoll()
    for i,v in pairs(list) do
        if v == nil then return end

        if v:getModData()["RagdollTimer"] == nil then
            v:getModData()["RagdollTimer"] = 0
        else
            v:getModData()["RagdollTimer"] = v:getModData()["RagdollTimer"] + getGameTime():getTimeDelta()
        end

        --v:setSitAgainstWall(true)
        --v:setVariable("bMoving", false)
        --v:setMoving(false)

       if not v:isDead() then
           --v:setRagdoll(true)
            --if v:isSitAgainstWall() and not v:getVariableBoolean("bMoving", true) and v:getVariableBoolean("issitting", true) then
                v:Kill(getPlayer())
        --elseif v:getModData()["RagdollTimer"] > 3 then
            --v:becomeCorpse()
            --table.remove(list, i)
       end
   
    end
end

Events.OnTick.Add(OnKillingZombieRagdoll)