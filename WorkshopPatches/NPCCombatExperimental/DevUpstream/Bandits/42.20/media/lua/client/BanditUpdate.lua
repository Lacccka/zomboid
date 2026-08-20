require "BanditZombie"

-- [LCC POC] Development-only working copy for controlled B42.20.3 testing.
-- This file lives outside Workshop Contents and must be manually copied over the
-- real Bandits 42.20 client BanditUpdate.lua when testing the current experiment.
LCC_BANDITS_ATTACK_BRIDGE_POC = "upstream-pursuit-v1"
print("[LCC][BanditsAttackPoC][INIT] upstream-pursuit-v1 active; vanilla spotted/addAggro/setTarget/setAttackedBy bridge disabled")

local sum1 = 0
local sum2 = 0
local sum3 = 0
local iter1 = 0
local iter2 = 0
local iter3 = 0

local function predicateRemovable(item)
    if not item:getModData().preserve and not instanceof(item, "Clothing") then
        return true
    end
end

local function predicateAll(item)
	return true
end

local function CalcSpottedScore(player, dist)
    if not instanceof(player, "IsoPlayer") then return end

    local square = player:getSquare()
    local spottedScore = square:getLightLevel(0)

    if player:isRunning() then spottedScore = spottedScore + 0.05 end
    if player:isSprinting() then spottedScore = spottedScore + 0.08 end

    if player:isSneaking() then
        spottedScore = spottedScore - 0.1
        local objects = square:getObjects()
        for i = 0, objects:size() - 1 do
            local object = objects:get(i)
            local props = object and object:getProperties()
            if props and props:has(IsoFlagType.vegitation) and props:has(IsoFlagType.canBeCut) then
                spottedScore = spottedScore - 0.15
                break
            end
        end
    end

    -- distance-based adjustment
    if dist <= 8 then
        spottedScore = spottedScore + (0.65 - (dist * 0.075))
    end

    return spottedScore
end

local function IsWindowClose(bandit)
    local bx = math.floor(bandit:getX())
    local by = math.floor(bandit:getY())
    local bz = bandit:getZ()
    local cell = getCell()

    for x=-1, 1 do
        for y=-1, 1 do
            local square = cell:getGridSquare(bx + x, by + y, bz)
            if square then
                local objects = square:getObjects()
                for i = 0, objects:size() - 1 do
                    local object = objects:get(i)
                    if object then
                        if instanceof(object, "IsoWindow") or instanceof(object, "IsoWindowFrame") or instanceof(object, "IsoThumpable") then
                            if object:canClimbThrough(bandit) then
                                return true
                            end
                        end
                    end
                end
            end
        end
    end
end

-- checks if the line of fire is clear from friendlies
local function IsShotClear (shooter, enemy)

    if true then return true end

    local cell = getCell()

    local x0 = math.floor(shooter:getX())
    local y0 = math.floor(shooter:getY())
    local x1 = math.floor(enemy:getX())
    local y1 = math.floor(enemy:getY())
    local z = enemy:getZ()

    local dx = math.abs(x1 - x0)
    local dy = math.abs(y1 - y0)
    local sx = (x0 < x1) and 1 or -1
    local sy = (y0 < y1) and 1 or -1
    local err = dx - dy

    local cx, cy, cz = x0, y0, z

    local brainShooter = BanditBrain.Get(shooter)

    local i = 0
    while true do

        -- last iteration
        local list = {}
        if cx == x1 and cy == y1 then
            for x = -2, 2 do
                for y = -2, 2 do
                    table.insert(list, {x = cx + x, y = cy + y, z=cz})
                end
            end
        else
            table.insert(list, {x=cx, y=cy, z=cz})
        end

        for _, c in pairs(list) do
            local square = cell:getGridSquare(c.x, c.y, c.z)
            if i > 1 and square then

                local chrs = square:getMovingObjects()
                for i=0, chrs:size()-1 do
                    local chr = chrs:get(i)
                    if instanceof(chr, "IsoPlayer") and not (brainShooter.hostile or brainShooter.hostileP) then
                        -- shooter:addLineChatElement("PLAYER IN LINE", 0.8, 0.8, 0.1)
                        return false
                    elseif instanceof(chr, "IsoZombie") then
                        local brainEnemy = BanditBrain.Get(chr)
                        if not BanditUtils.AreEnemies(brainEnemy, brainShooter) then
                        -- if brainEnemy and brainEnemy.clan and brainShooter.clan == brainEnemy.clan and (not brainShooter.hostile or brainEnemy.hostile) then
                            -- shooter:addLineChatElement("FRIENDLY IN LINE", 0.8, 0.8, 0.1)
                            return false
                        end
                    end
                end
            end
        end

        if cx == x1 and cy == y1 then break end
        local e2 = 2 * err
        if e2 > -dy then
            err = err - dy
            cx = cx + sx
        end
        if e2 < dx then
            err = err + dx
            cy = cy + sy
        end
        i = i + 1
    end

    return true
end

-- turns a zombie into a bandit
local function Banditize(zombie, brain)

    -- load brain
    BanditBrain.Update(zombie, brain)

    -- just in case
    zombie:setNoTeeth(true)

    -- used to determine if zombie is a bandit, can be used by other mods
    zombie:setVariable("Bandit", true)
    zombie:getModData().isDeadBandit = false
    zombie:setVariable("LimpSpeed", 0.80)
    zombie:setVariable("RunSpeed", 0.65 + ZombRandFloat(0, 0.15))
    zombie:setVariable("WalkSpeed", 1.04)

    -- bandit primary and secondary hand items
    zombie:setVariable("BanditPrimary", "")
    zombie:setVariable("BanditSecondary", "")

    -- bandit walking type defined in animations
    zombie:setWalkType("Walk")
    zombie:setVariable("BanditWalkType", "Walk")

    -- this shit here is important, removes black screen crashes
    -- with this var set, game engine skips testDefense function that
    -- wrongly refers to moodles, which zombie object does not have
    zombie:setVariable("ZombieHitReaction", "Chainsaw")

    -- prevents the bandit from being the target of a lunge attack
    zombie:setVariable("NoLungeTarget", true)

    -- stfu
    zombie:getEmitter():stopAll()

    zombie:setPrimaryHandItem(nil)
    zombie:setSecondaryHandItem(nil)
    zombie:resetEquippedHandsModels()
    zombie:clearAttachedItems()

    -- makes bandit unstuck after spawns
    zombie:setTurnAlertedValues(-5, 5)

    zombie:getModData().brainId = brain.id

    local desc = zombie:getDescriptor()
    -- local test = desc:getVoicePrefix()
    desc:setVoicePrefix("Bandit")

end

-- turns bandit into a zombie
local function Zombify(bandit)
    bandit:setNoTeeth(false)
    bandit:setUseless(false)
    bandit:setVariable("Bandit", false)
    bandit:setVariable("BanditPrimary", "")
    bandit:setVariable("BanditSecondary", "")
    bandit:setWalkType("2")
    bandit:setVariable("BanditWalkType", "")
    bandit:setPrimaryHandItem(nil)
    bandit:setSecondaryHandItem(nil)
    bandit:resetEquippedHandsModels()
    bandit:clearAttachedItems()
    bandit:getModData().brainId = nil
    BanditBrain.Remove(bandit)
end

-- updates bandit torches light
local TorchLightDefs = {
    {d=0, r=2, c={r = 1, g = 0.9, b = 0.8}},
    {d=2, r=4, c={r = 1, g = 0.9, b = 0.8}},
    {d=7, r=8, c={r = 1, g = 0.9, b = 0.7}},
    {d=11, r=12, c={r = 0.8, g = 0.8, b = 0.7}}
}

local function ManageTorch(bandit, brain)
    if not SandboxVars.Bandits.General_CarryTorches then return end
    if not brain.torch then return end

    local zx, zy, zz = math.floor(bandit:getX()), math.floor(bandit:getY()), math.floor(bandit:getZ())
    local vehicle = bandit:getVehicle()
    local cell = getCell()

    if vehicle then return end
    
    if bandit:isProne() then

    else
        local theta = bandit:getDirectionAngle() * 0.0174533

        for _, ld in ipairs(TorchLightDefs) do
            local lx = math.floor(zx + (ld.d * math.cos(theta)) + 0.5)
            local ly = math.floor(zy + (ld.d * math.sin(theta)) + 0.5)
            local lz = zz

            local ls = cell:getLightSourceAt(lx, ly, lz)
            if not ls then
                local ls = IsoLightSource.new(lx, ly, lz, ld.c.r, ld.c.g, ld.c.b, ld.r, 1)
                if ls then
                    cell:addLamppost(ls)
                end
            else
                cell:removeLamppost(ls)
            end
        end
    end
end

local function ManageChainsaw(bandit)
    if bandit:isPrimaryEquipped("AuthenticZClothing.Chainsaw") then
        local emitter = bandit:getEmitter()
        if not emitter:isPlaying("ChainsawIdle") then
            bandit:playSound("ChainsawIdle")
        end
    end
end

local function ManageOnFire(bandit)
    if bandit:isOnFire() then
        if not Bandit.HasTaskType(bandit, "Die") then
            Bandit.ClearTasks(bandit)
            Bandit.AddTask(bandit, {action="Die", lock=true, anim="Die", fire=true, time=250})
        end
        return
    end

    local cell = bandit:getCell()
    local bx, by, bz = bandit:getX(), bandit:getY(), bandit:getZ()

    if Bandit.HasActionTask(bandit) then return end

    for x = -2, 2 do
        for y = -2, 2 do
            local testSquare = cell:getGridSquare(bx + x, by + y, bz)
            if testSquare and testSquare:haveFire() then
                Bandit.ClearTasks(bandit)
                Bandit.AddTask(bandit, {action="Time", anim="Cough", time=200})
                return
            end
        end
    end
end

local function ManageSpeechCooldown(brain)
    if brain.speech and brain.speech > 0 then
        brain.speech = brain.speech - 0.01
        if brain.speech < 0 then brain.speech = 0 end
    end
end

local ClearTaskActionStates = {
    ["getup"] = true,
    ["getup-fromonback"] = true,
    ["getup-fromonfront"] = true,
    ["getup-fromsitting"] = true,
    ["staggerback"] = true,
    ["staggerback-knockeddown"] = true,
    ["onground"] = true,
    ["onground-ragdoll"] = true,
    ["onground-breathing"] = true,
    ["falldown"] = true,
    ["falldown-headleft"] = true,
    ["falldown-headleft-ragdoll"] = true,
    ["falldown-headright"] = true,
    ["falldown-headright-ragdoll"] = true,
    ["falldown-tophead"] = true,
    ["falldown-tophead-ragdoll"] = true,
    ["falldown-knifedead"] = true,
    ["falldown-knifedead-ragdoll"] = true,
    ["falldown-onknees"] = true,
    ["falldown-onknees-ragdoll"] = true,
    ["falldown-ragdoll"] = true,
    ["falldown-speardeath1"] = true,
    ["falldown-speardeath1-ragdoll"] = true,
    ["falldown-speardeath2"] = true,
    ["falldown-speardeath2-ragdoll"] = true,
    ["falldown-uppercut"] = true,
    ["falldown-uppercut-ragdoll"] = true,
    ["hitreaction"] = true,
    ["hitreaction-fencewindow"] = true,
    ["hitreaction-fencewindow-ragdoll"] = true,
    ["hitreaction-gettingup"] = true,
    ["hitreaction-hit"] = true,
    ["hitreaction-onfloor"] = true,
    ["hitreaction-ragdoll"] = true,
    ["hitreaction-shothead-bwd"] = true,
    ["hitreaction-shothead-bwd-ragdoll"] = true,
    ["hitreaction-shothead-fwd"] = true,
    ["hitreaction-shothead-fwd-ragdoll"] = true,
    ["hitreaction-shothead-fwd02"] = true,
    ["hitreaction-shothead-fwd02-ragdoll"] = true,
    ["knockeddown-headleft"] = true,
    ["knockeddown-headright"] = true,
    ["knockeddown-headtop"] = true,
    ["knockeddown-shotchestl"] = true,
    ["knockeddown-shotchestr"] = true,
    ["knockeddown-shotlegl"] = true,
    ["knockeddown-shotlegr"] = true,
    ["knockeddown-shotshoulderl"] = true,
    ["knockeddown-shotshoulderr"] = true,
    ["knockeddown-uppercut"] = true,
    ["staggerback-knockeddown-frombehind"] = true,
    ["staggerback-knockeddown-ragdoll"] = true,
    ["staggerback-ragdoll"] = true,
    ["vehiclecollision-bumped"] = true,
    ["vehiclecollision-falldown"] = true,
    ["vehiclecollision-onground"] = true,
    ["vehiclecollision-onground-dead"] = true,
    ["vehiclecollision-ragdoll"] = true,
    ["vehiclecollision-staggerback"] = true,
}

local function ManageActionState(bandit)
    local asn = bandit:getActionStateName()

    if asn == "turnalerted" then
        bandit:changeState(ZombieIdleState.instance())
        bandit:clearAggroList()
        bandit:setTarget(nil)
        return true
    elseif asn == "pathfind" then
        return true
    elseif asn == "lunge" then
        bandit:setUseless(true)
        bandit:clearAggroList()
        bandit:setTarget(nil)
        return true
    elseif ClearTaskActionStates[asn] then
        Bandit.ClearTasks(bandit)
        return false
    end

    bandit:setTarget(nil)
    bandit:setUseless(getWorld():getGameMode() ~= "Multiplayer" or Bandit.IsForceStationary(bandit))
    return true
end

local function ManageEndurance(bandit)
    if bandit:isMoving() then
        if bandit:getVariableString("BanditWalkType") == "Run" then
            local player = getSpecificPlayer(0)
            local px, py, pz = player:getX(), player:getY(), player:getZ()
            local zx, zy, zz = bandit:getX(), bandit:getY(), bandit:getZ()
            local dist = ((zx - px) * (zx - px)) + ((zy - py) * (zy - py))
            if pz == zz and dist < 9 then
                local volume = getSoundManager():getSoundVolume()
                local emitter = bandit:getEmitter()
                local sound = "ZSBreath_Male"
                if bandit:isFemale() then sound = "ZSBreath_Female" end
                if not emitter:isPlaying(sound) then
                    local id = emitter:playSound(sound)
                    emitter:setVolume(id, volume * 0.6)
                end
            end
        end
    end

    if not SandboxVars.Bandits.General_LimitedEndurance then
        return {}
    end

    local brain = BanditBrain.Get(bandit)
    if brain.endurance > 0 or Bandit.HasActionTask(bandit) then
        return {}
    end

    brain.endurance = 1
    local exhaustionTasks = {}
    local exhaustionTask = { action = "Time", anim = "Exhausted", time = 200, lock = true }
    for i = 1, 5 do
        exhaustionTasks[i] = exhaustionTask
    end
    return exhaustionTasks
end

local function ManageHealth(bandit)
    local tasks = {}

    if SandboxVars.Bandits.General_BleedOut then
        local healing = false
        local health = bandit:getHealth()
        if health < 0.7 then
            local zx, zy = bandit:getX(), bandit:getY()
            if ZombRand(16) == 0 then
                local bx = zx - 0.5 + ZombRandFloat(0.1, 0.9)
                local by = zy - 0.5 + ZombRandFloat(0.1, 0.9)
                local chunk = bandit:getChunk()
                if chunk then 
                    chunk:addBloodSplat(bx, by, 0, ZombRand(20))
                end
            end
            bandit:setHealth(health - 0.00005)
        end
    end

    if SandboxVars.Bandits.General_Infection then
        local brain = BanditBrain.Get(bandit)
        if brain.infection and brain.infection > 0 then
            Bandit.UpdateInfection(bandit, 0.001)
            if brain.infection >= 100 then
                Bandit.ClearTasks(bandit)
                local task = {action="Zombify", anim="Faint", lock=true, time=200}
                table.insert(tasks, task)
            end
        end
    end
    return tasks
end

local function RemoveWindowFromPathing (bandit, square)
    if true then return end
end

local function ManageCollisions(bandit)
    local collided = bandit:isCollidedWithDoor() or bandit:isCollidedThisFrame() or bandit:isCollided()
    if not collided then return {} end

    local tasks = {}
    local task = Bandit.GetTask(bandit)
    if not task then return {} end
    if not (task.action == "Move" or task.action == "GoTo") then return {} end
    
    local dir = bandit:getDirectionAngle()
    local bx = math.floor(bandit:getX())
    local by = math.floor(bandit:getY())
    local bz = bandit:getZ()
    local sx, sy
    if (dir >= -180 and dir < -45) or (dir >= 135 and dir <= 180) then
        sx, sy = bx, by
    elseif dir >= -45 and dir < 45 then
        sx, sy = bx + 1, by
    elseif dir >= 45 and dir < 135 then
        sx, sy = bx, by + 1
    end
   
    if not sx or not sy then return {} end

    local cell = getCell()
    local square = cell:getGridSquare(sx, sy, bz)
    if square then
        local brain = BanditBrain.Get(bandit)
        local weapons = brain.weapons
        local objects = square:getObjects()
        for i = 0, objects:size() - 1 do
            local object = objects:get(i)
            if object then
                local properties = object:getProperties()
                if properties then
                    local lowFence = properties:get("FenceTypeLow")
                    local hoppable = object:isHoppable()
                    if lowFence or hoppable then
                        if bandit:isFacingObject(object, 0.5) then
                            bandit:changeState(ClimbOverFenceState.instance())
                            bandit:setBumpType("ClimbFenceEnd")
                        else
                            bandit:faceThisObject(object)
                        end
                        return tasks
                    end

                    local tallFence = properties:get("FenceTypeHigh")
                    local tallHoppable = object:isTallHoppable()
                    if tallFence or tallHoppable then
                        bandit:setCollidable(false)
                        if bandit:isFacingObject(object, 0.5) then
                            bandit:setBumpType("ClimbFenceTall")
                        else
                            bandit:faceThisObject(object)
                        end
                        return tasks
                    end

                    if instanceof(object, "IsoWindow") then
                        if bandit:isFacingObject(object, 0.5) then
                            if object:isBarricaded() then
                                if brain.hostile then
                                    local barricade = object:getBarricadeOnSameSquare()
                                    if not barricade then barricade = object:getBarricadeOnOppositeSquare() end
                                    local fx, fy
                                    if barricade then
                                        if properties:has(IsoFlagType.WindowN) then
                                            fx = barricade:getX()
                                            fy = barricade:getY() - 0.5
                                        else
                                            fx = barricade:getX() - 0.5
                                            fy = barricade:getY()
                                        end
                                    else
                                        barricade = object:getBarricadeOnOppositeSquare()
                                        if properties:has(IsoFlagType.WindowN) then
                                            fx = barricade:getX()
                                            fy = barricade:getY() + 0.5
                                        else
                                            fx = barricade:getX() + 0.5
                                            fy = barricade:getY()
                                        end
                                    end

                                    if SandboxVars.Bandits.General_RemoveBarricade and Bandit.HasExpertise(bandit, Bandit.Expertise.Breaker) then
                                        if barricade:isMetal() or barricade:isMetalBar() then
                                            if not bandit:isPrimaryEquipped("Bandits.PropaneTorch") then
                                                local stasks = BanditPrograms.Weapon.Switch(bandit, "Bandits.PropaneTorch")
                                                for _, t in pairs(stasks) do table.insert(tasks, t) end
                                            end
                                            if not BanditBrain.HasTaskType(brain, "UnbarricadeMetal") then
                                                local task = {action="UnbarricadeMetal", anim="BlowtorchHigh", time=500, fx=fx, fy=fy, x=object:getSquare():getX(), y=object:getSquare():getY(), z=object:getSquare():getZ(), idx=object:getObjectIndex()}
                                                table.insert(tasks, task)
                                            end
                                            return tasks
                                        else
                                            anim = "RemoveBarricadeCrowbarMid"
                                            local planks = barricade:getNumPlanks()
                                            if planks == 2 or planks == 4 then anim = "RemoveBarricadeCrowbarHigh" end
                                            if not bandit:isPrimaryEquipped("Base.Crowbar") then
                                                local stasks = BanditPrograms.Weapon.Switch(bandit, "Base.Crowbar")
                                                for _, t in pairs(stasks) do table.insert(tasks, t) end
                                            end
                                            if not BanditBrain.HasTaskType(brain, "Unbarricade") then
                                                local task = {action="Unbarricade", anim=anim, time=300, fx=fx, fy=fy, x=object:getSquare():getX(), y=object:getSquare():getY(), z=object:getSquare():getZ(), idx=object:getObjectIndex()}
                                                table.insert(tasks, task)
                                            end
                                            return tasks
                                        end
                                    else
                                        if not bandit:isPrimaryEquipped(weapons.melee) then
                                            local stasks = BanditPrograms.Weapon.Switch(bandit, weapons.melee)
                                            for _, t in pairs(stasks) do table.insert(tasks, t) end
                                        end
                                        if not BanditBrain.HasTaskType(brain, "Destroy") then
                                            local task = {action="Destroy", anim="ChopTree", x=object:getSquare():getX(), y=object:getSquare():getY(), z=object:getSquare():getZ(), idx=object:getObjectIndex()}
                                            table.insert(tasks, task)
                                        end
                                        return tasks
                                    end
                                else
                                    RemoveWindowFromPathing(bandit, square)
                                end
                            elseif not object:IsOpen() and not object:isSmashed() and not BanditBrain.HasTaskType(brain, "SmashWindow") then
                                if SandboxVars.Bandits.General_SmashWindow and (brain.hostile or brain.demolish) then
                                    local task = {action="SmashWindow", anim="WindowSmash", time=25, x=object:getSquare():getX(), y=object:getSquare():getY(), z=object:getSquare():getZ()}
                                    table.insert(tasks, task)
                                    return tasks
                                elseif not object:isPermaLocked() and not BanditBrain.HasTaskType(brain, "OpenWindow") then
                                    local task = {action="OpenWindow", anim="WindowOpen", time=25, x=object:getSquare():getX(), y=object:getSquare():getY(), z=object:getSquare():getZ()}
                                    table.insert(tasks, task)
                                    return tasks
                                end
                            elseif object:canClimbThrough(bandit) then
                                ClimbThroughWindowState.instance():setParams(bandit, object)
                                bandit:changeState(ClimbThroughWindowState.instance())
                                bandit:setBumpType("ClimbWindow")
                                return tasks      
                            end
                        end
                    end

                    if instanceof(object, "IsoDoor") or (instanceof(object, 'IsoThumpable') and object:isDoor() == true) then
                        if bandit:isFacingObject(object, 0.5) then
                            if object:isBarricaded() then
                                local barricade = object:getBarricadeOnSameSquare()
                                local fx, fy
                                if barricade then
                                    if properties:has(IsoFlagType.doorN) then
                                        fx = barricade:getX()
                                        fy = barricade:getY() - 1
                                    else
                                        fx = barricade:getX() - 1
                                        fy = barricade:getY()
                                    end
                                else
                                    barricade = object:getBarricadeOnOppositeSquare()
                                    if properties:has(IsoFlagType.doorN) then
                                        fx = barricade:getX()
                                        fy = barricade:getY() + 1
                                    else
                                        fx = barricade:getX() + 1
                                        fy = barricade:getY()
                                    end
                                end
                                local sameSide = barricade:getSquare():getX() == bandit:getSquare():getX() and barricade:getSquare():getY() == bandit:getSquare():getY()

                                if SandboxVars.Bandits.General_RemoveBarricade and Bandit.HasExpertise(bandit, Bandit.Expertise.Breaker) and sameSide then
                                    anim = "RemoveBarricadeCrowbarMid"
                                    local planks = barricade:getNumPlanks()
                                    if planks == 2 or planks == 4 then anim = "RemoveBarricadeCrowbarHigh" end
                                    if not bandit:isPrimaryEquipped("Base.Crowbar") then
                                        local stasks = BanditPrograms.Weapon.Switch(bandit, "Base.Crowbar")
                                        for _, t in pairs(stasks) do table.insert(tasks, t) end
                                    end
                                    Bandit.Say(bandit, "BREACH")
                                    if not BanditBrain.HasTaskType(brain, "Unbarricade") then
                                        local task = {action="Unbarricade", anim=anim, time=300, fx=fx, fy=fy, x=object:getSquare():getX(), y=object:getSquare():getY(), z=object:getSquare():getZ(), idx=object:getObjectIndex()}
                                        table.insert(tasks, task)
                                    end
                                    return tasks
                                else
                                    if not bandit:isPrimaryEquipped(weapons.melee) then
                                        local stasks = BanditPrograms.Weapon.Switch(bandit, weapons.melee)
                                        for _, t in pairs(stasks) do table.insert(tasks, t) end
                                    end
                                    Bandit.Say(bandit, "BREACH")
                                    if not BanditBrain.HasTaskType(brain, "Destroy") then
                                        local task = {action="Destroy", anim="ChopTree", x=object:getSquare():getX(), y=object:getSquare():getY(), z=object:getSquare():getZ(), idx=object:getObjectIndex()}
                                        table.insert(tasks, task)
                                    end
                                    return tasks
                                end
                            elseif not object:IsOpen() then
                                if IsoDoor.getDoubleDoorIndex(object) > -1 then
                                    if object:isLocked() or object:isLockedByKey() or object:isObstructed() then
                                        if bandit:isPrimaryEquipped(weapons.melee) then
                                            if not BanditBrain.HasTaskType(brain, "Destroy") then
                                                local task = {action="Destroy", anim="ChopTree", x=object:getSquare():getX(), y=object:getSquare():getY(), z=object:getSquare():getZ(), idx=object:getObjectIndex()}
                                                table.insert(tasks, task)
                                            end
                                            return tasks
                                        else
                                            local stasks = BanditPrograms.Weapon.Switch(bandit, weapons.melee)
                                            for _, t in pairs(stasks) do table.insert(tasks, t) end
                                            return tasks
                                        end
                                    else
                                        IsoDoor.toggleDoubleDoor(object, true)
                                        BanditNotifications.DoorToggled(bandit, object, true)
                                        local doorSound = properties:has("DoorSound") and properties:get("DoorSound") or "WoodDoor"
                                        doorSound = doorSound .. "Open"
                                        bandit:playSound(doorSound)
                                    end
                                elseif IsoDoor.getGarageDoorIndex(object) > -1 then
                                    local exterior = bandit:getCurrentSquare():has(IsoFlagType.exterior)
                                    if brain.hostile and (object:isLocked() or object:isLockedByKey() or object:getModData().CustomLock or object:isObstructed()) then
                                        if bandit:isPrimaryEquipped(weapons.melee) then
                                            Bandit.Say(bandit, "BREACH")
                                            if not BanditBrain.HasTaskType(brain, "Destroy") then
                                                local task = {action="Destroy", anim="ChopTree", x=object:getSquare():getX(), y=object:getSquare():getY(), z=object:getSquare():getZ(), idx=object:getObjectIndex()}
                                                table.insert(tasks, task)
                                            end
                                            return tasks
                                        else
                                            local stasks = BanditPrograms.Weapon.Switch(bandit, weapons.melee)
                                            for _, t in pairs(stasks) do table.insert(tasks, t) end
                                            return tasks
                                        end
                                    else
                                        IsoDoor.toggleGarageDoor(object, true)
                                        BanditNotifications.DoorToggled(bandit, object, true)
                                        local doorSound = properties:has("DoorSound") and properties:get("DoorSound") or "WoodDoor"
                                        doorSound = doorSound .. "Open"
                                        bandit:playSound(doorSound)
                                    end
                                else
                                    if ((object:isLocked() or object:isLockedByKey()) and (not bandit:getCurrentSquare():getRoom() or object:getProperties():has("forceLocked"))) or object:isObstructed() then
                                        if bandit:isPrimaryEquipped(weapons.melee) then
                                            Bandit.Say(bandit, "BREACH")
                                            if not BanditBrain.HasTaskType(brain, "Unbarricade") then
                                                local task = {action="Destroy", anim="ChopTree", x=object:getSquare():getX(), y=object:getSquare():getY(), z=object:getSquare():getZ(), idx=object:getObjectIndex()}
                                                table.insert(tasks, task)
                                            end
                                            return tasks
                                        else
                                            local stasks = BanditPrograms.Weapon.Switch(bandit, weapons.melee)
                                            for _, t in pairs(stasks) do table.insert(tasks, t) end
                                            return tasks
                                        end
                                    else
                                        object:DirtySlice()
                                        IsoGridSquare.RecalcLightTime = -1.0
                                        square:InvalidateSpecialObjectPaths()
                                        object:ToggleDoorSilent()
                                        square:RecalcProperties()
                                        object:syncIsoObject(false, 1, nil, nil)
                                        LuaEventManager.triggerEvent("OnContainerUpdate")
                                        if BanditCompatibility.GetGameVersion() >= 42 then
                                            object:invalidateRenderChunkLevel(FBORenderChunk.DIRTY_OBJECT_MODIFY)
                                        end
                                        BanditNotifications.DoorToggled(bandit, object, true)
                                        local doorSound = properties:has("DoorSound") and properties:get("DoorSound") or "WoodDoor"
                                        doorSound = doorSound .. "Open"
                                        bandit:playSound(doorSound)
                                    end
                                end
                            end
                        else
                            bandit:faceThisObject(object)
                        end
                    end

                    if SandboxVars.Bandits.General_DestroyThumpable and instanceof(object, "IsoThumpable") and not properties:get("FenceTypeLow") and brain.hostile then
                        local isWallTo = bandit:getSquare():isSomethingTo(object:getSquare())
                        if not isWallTo then
                            if bandit:isPrimaryEquipped(weapons.melee) then
                                local task = {action="Destroy", anim="ChopTree", x=object:getSquare():getX(), y=object:getSquare():getY(), z=object:getSquare():getZ(), soundEnd=object:getThumpSound(), time=80}
                                table.insert(tasks, task)
                            else
                                local stasks = BanditPrograms.Weapon.Switch(bandit, weapons.melee)
                                for _, t in pairs(stasks) do table.insert(tasks, t) end
                                return tasks
                            end
                        end
                    end
                end
            end
        end
    end

    return tasks
end

local function ManageCombat(bandit)
    if bandit:isCrawling() then return {} end 
    if Bandit.IsSleeping(bandit) then return {} end

    local tasks = {}
    local zx, zy, zz = bandit:getX(), bandit:getY(), bandit:getZ()
    local brain = BanditBrain.Get(bandit)
    local weapons = brain.weapons
    local isOutOfAmmo = BanditBrain.IsOutOfAmmo(brain)
    local isNeedPrimary = BanditBrain.NeedResupplySlot(brain, "primary")
    local isNeedSecondary = BanditBrain.NeedResupplySlot(brain, "secondary")
    local isBareHands = BanditBrain.IsBareHands(brain)
    local isOutside = bandit:getSquare():isOutside()

    local bestDist = 40
    local enemyCharacter, switchTo
    local healing, reload, resupply = false, false, false
    local combat, switch, firing, stomp, shove, escape = false, false, false, false, false, false
    local maxRangeMelee, maxRangePistol, maxRangeRifle
    local friendlies, friendliesBwd, enemies, enemiesBwd = 0, 0, 0, 0
    local sx, sy = 0, 0

    if not BanditBrain.HasActionTask(brain) then
        local health = bandit:getHealth()    
        if health < 0.4 then healing = true end

        local wp = weapons.primary
        if wp and wp.name then
            if (wp.type == "mag" and wp.bulletsLeft <= 0 and wp.magCount > 0) or
               (wp.type == "nomag" and wp.bulletsLeft < wp.ammoSize and wp.ammoCount > 0) or
               wp.racked == false then
                if bandit:isPrimaryEquipped(wp.name) then reload = true end
            end
        end

        local ws = weapons.secondary
        if not reload and ws and ws.name then
            if (ws.type == "mag" and ws.bulletsLeft <= 0 and ws.magCount > 0) or
               (ws.type == "nomag" and ws.bulletsLeft < ws.ammoSize and ws.ammoCount > 0) or
               ws.racked == false then
                if bandit:isPrimaryEquipped(ws.name) then reload = true end
            end
        end

        if isBareHands or isNeedPrimary or isNeedSecondary then resupply = true end
    end

    local meleeDist = isOutside and 2.6 or 1.2
    local meleeDistPlayer = isOutside and 3.5 or 1.2
    local rifleDist = 5.5
    local escapeDist = 10
    local bwdDist = 2.8

    if brain.hostile or brain.hostileP then
        local playerList = BanditPlayer.GetPlayers()
        for i=0, playerList:size()-1 do
            local potentialEnemy = playerList:get(i)
            if potentialEnemy and potentialEnemy:isAlive() and bandit:CanSee(potentialEnemy) and not potentialEnemy:isBehind(bandit) and (instanceof(potentialEnemy, "IsoPlayer") and not BanditPlayer.IsGhost(potentialEnemy)) then
                local px, py, pz = potentialEnemy:getX(), potentialEnemy:getY(), potentialEnemy:getZ()
                local dist = math.sqrt(((zx - px) * (zx - px)) + ((zy - py) * (zy - py)))
                if dist < bestDist and math.abs(zz - pz) < 0.5 then
                    local spottedScore = CalcSpottedScore(potentialEnemy, dist)
                    if not bandit:getSquare():isSomethingTo(potentialEnemy:getSquare()) and spottedScore > 0.49 then
                        bestDist, enemyCharacter = dist, potentialEnemy
                        brain.lastPost = {x=px, y=py, z=pz, id=BanditUtils.GetCharacterID(enemyCharacter), d=enemyCharacter:getDirectionAngle()}
                        combat, switch, firing, shove, escape = false, false, false, false, false

                        if weapons.melee then
                            if not maxRangeMelee then maxRangeMelee = BanditCompatibility.InstanceItem(weapons.melee):getMaxRange() end
                            local prone = potentialEnemy:isProne()
                            if dist <= meleeDistPlayer then 
                                if bandit:isPrimaryEquipped(weapons.melee) then
                                    if dist <= maxRangeMelee then
                                        local asn = enemyCharacter:getActionStateName()
                                        shove = dist < 0.5 and not prone and asn ~= "onground" and asn ~= "sitonground" and asn ~= "climbfence" and asn ~= "bumped"
                                        combat = not shove
                                    end
                                else
                                    switch = true
                                    switchTo = weapons.melee
                                end
                            end
                        end

                        if not isOutOfAmmo and dist > meleeDistPlayer + 1 and not combat and not shove then
                            if weapons.primary.name and weapons.primary.bulletsLeft > 0 then
                                if not maxRangeRifle then
                                    local item = BanditCompatibility.InstanceItem(weapons.primary.name)
                                    item = BanditUtils.ModifyWeapon(item, brain)
                                    maxRangeRifle = BanditCompatibility.GetMaxRange(item)
                                end
                                if dist < maxRangeRifle then
                                    if bandit:isPrimaryEquipped(weapons.primary.name) then
                                        if dist < maxRangeRifle + rifleDist and IsShotClear(bandit, potentialEnemy) then firing = true end
                                    elseif not reload then
                                        Bandit.Say(bandit, "SPOTTED")
                                        switch = true
                                        switchTo = weapons.primary.name
                                    end
                                end
                            elseif weapons.secondary.name and weapons.secondary.bulletsLeft > 0 then
                                if not maxRangePistol then
                                    local item = BanditCompatibility.InstanceItem(weapons.secondary.name)
                                    item = BanditUtils.ModifyWeapon(item, brain)
                                    maxRangePistol = BanditCompatibility.GetMaxRange(item)
                                end
                                if dist < maxRangePistol then
                                    if bandit:isPrimaryEquipped(weapons.secondary.name) then
                                        if dist < maxRangePistol + rifleDist and IsShotClear(bandit, potentialEnemy) then firing = true end
                                    elseif not reload then
                                        Bandit.Say(bandit, "SPOTTED")
                                        switch = true
                                        switchTo = weapons.secondary.name
                                    end
                                end
                            end
                        end
                    end
                end
            end
        end
    end

    local cache, potentialEnemyList = BanditZombie.Cache, BanditZombie.CacheLight
    for id, potentialEnemy in pairs(potentialEnemyList) do
        local maxDistAllowed = 57
        if brain.weaponsHold then maxDistAllowed = 2 end
        local distManhattan = math.abs(potentialEnemy.x - zx) + math.abs(potentialEnemy.y - zy)
        if distManhattan < maxDistAllowed then
            if BanditUtils.AreEnemies(potentialEnemy.brain, brain) then
                local potentialEnemy = cache[id]
                if potentialEnemy:isAlive() and potentialEnemy:getHealth() > 0 and bandit:CanSee(potentialEnemy) then
                    local pesq = potentialEnemy:getSquare()
                    if pesq and pesq:getLightLevel(0) > 0.20 and not bandit:getSquare():isSomethingTo(pesq) then
                        local px, py, pz = potentialEnemy:getX(), potentialEnemy:getY(), potentialEnemy:getZ()
                        local dist = math.sqrt(((zx - px) * (zx - px)) + ((zy - py) * (zy - py)))
                        if dist < escapeDist and potentialEnemy:isAlive() and not potentialEnemy:isProne() then
                            enemies = enemies + 1
                            if dist < bwdDist then enemiesBwd = enemiesBwd + 1 end
                        end
                        if dist < bestDist then
                            bestDist, enemyCharacter = dist, potentialEnemy
                            combat, switch, firing, shove, stomp, escape = false, false, false, false, false, false
                            local asn = enemyCharacter:getActionStateName()
                            if dist <= 1 and math.abs(zz - pz) < 0.8 then
                                if enemyCharacter:isProne() or ans == "onground" then stomp = true else shove = true end
                            elseif not isOutOfAmmo then
                                if weapons.primary.name and weapons.primary.bulletsLeft > 0 then
                                    if not maxRangeRifle then
                                        local item = BanditCompatibility.InstanceItem(weapons.primary.name)
                                        item = BanditUtils.ModifyWeapon(item, brain)
                                        maxRangeRifle = BanditCompatibility.GetMaxRange(item)
                                    end
                                    if dist < maxRangeRifle then
                                        if bandit:isPrimaryEquipped(weapons.primary.name) then
                                            if dist < maxRangeRifle + rifleDist and IsShotClear(bandit, potentialEnemy) then firing = true end
                                        elseif not reload then
                                            Bandit.Say(bandit, "SPOTTED")
                                            switch = true
                                            switchTo = weapons.primary.name
                                        end
                                    end
                                elseif weapons.secondary.name and weapons.secondary.bulletsLeft > 0 then
                                    if not maxRangePistol then
                                        local item = BanditCompatibility.InstanceItem(weapons.secondary.name)
                                        item = BanditUtils.ModifyWeapon(item, brain)
                                        maxRangePistol = BanditCompatibility.GetMaxRange(item)
                                    end
                                    if dist < maxRangePistol then
                                        if bandit:isPrimaryEquipped(weapons.secondary.name) then
                                            if dist < maxRangePistol + rifleDist and IsShotClear(bandit, potentialEnemy) then firing = true end
                                        elseif not reload then
                                            Bandit.Say(bandit, "SPOTTED")
                                            switch = true
                                            switchTo = weapons.secondary.name
                                        end
                                    end
                                end
                            elseif dist <= meleeDist then
                                if bandit:isPrimaryEquipped(weapons.melee) then
                                    if not maxRangeMelee then maxRangeMelee = BanditCompatibility.InstanceItem(weapons.melee):getMaxRange() end
                                    local fix = 0.1
                                    if dist <= maxRangeMelee + fix then combat = true end
                                else
                                    switch = true
                                    switchTo = weapons.melee
                                end
                            end
                        end
                    end
                end
            else
                local distSq = ((zx - potentialEnemy.x) * (zx - potentialEnemy.x)) + ((zy - potentialEnemy.y) * (zy - potentialEnemy.y))
                if distSq < 27.04 then
                    friendlies = friendlies + 1
                    if distSq < 5.76 then friendliesBwd = friendliesBwd + 1 end
                end
            end
        end
    end

    if getWorld():getGameMode() == "Multiplayer" and IsWindowClose(bandit) then
        if bandit:getPrimaryHandItem() then bandit:setPrimaryHandItem(nil) end
        if bandit:getSecondaryHandItem() then bandit:setSecondaryHandItem(nil) end
        switch = false
    end

    if shove then
        if not BanditBrain.HasTaskType(brain, "Push") then
            Bandit.ClearTasks(bandit)
            local veh = enemyCharacter:getVehicle()
            if veh then Bandit.Say(bandit, "CAR") end
            if bandit:isFacingObject(enemyCharacter, 0.1) then
                local eid = BanditUtils.GetCharacterID(enemyCharacter)
                local task = {action="Push", anim="Shove", sound="AttackShove", time=60, endurance=-0.05, eid=eid, x=enemyCharacter:getX(), y=enemyCharacter:getY(), z=enemyCharacter:getZ()}
                table.insert(tasks, task)
            else
                bandit:faceThisObject(enemyCharacter)
            end
        end
    elseif stomp then
        if not BanditBrain.HasTaskTypes(brain, {"Smack"}) then 
            Bandit.ClearTasks(bandit)
            local eid = BanditUtils.GetCharacterID(enemyCharacter)
            local task = {action="Smack", time=65, endurance=-0.03, shm=false, weapon=weapons.melee, eid=eid, x=enemyCharacter:getX(), y=enemyCharacter:getY(), z=enemyCharacter:getZ()}
            table.insert(tasks, task)
        end
    elseif switch then
        if not BanditBrain.HasActionTask(brain) then
            Bandit.ClearTasks(bandit)
            local stasks = BanditPrograms.Weapon.Switch(bandit, switchTo)
            for _, t in pairs(stasks) do table.insert(tasks, t) end
        end
    elseif combat then
        if not BanditBrain.HasTaskTypes(brain, {"Smack", "Push", "Equip", "Unequip"}) then 
            Bandit.ClearTasks(bandit)
            local veh = enemyCharacter:getVehicle()
            if veh then Bandit.Say(bandit, "CAR") end
            if bandit:isFacingObject(enemyCharacter, 0.5) then
                local shouldHitMoving = enemiesBwd >= friendliesBwd + 1
                local eid = BanditUtils.GetCharacterID(enemyCharacter)
                local task = {action="Smack", time=65, endurance=-0.03, shm=shouldHitMoving, weapon=weapons.melee, eid=eid, x=enemyCharacter:getX(), y=enemyCharacter:getY(), z=enemyCharacter:getZ()}
                table.insert(tasks, task)
            else
                bandit:faceThisObject(enemyCharacter)
            end
        end
    elseif enemies >= friendlies + 2 then
        if not BanditBrain.HasMoveTask(brain) then
            local sx, sy = 0, 0
            local closestDistSq = math.huge
            local closestDX, closestDY = 0, 0
            local threatCount = 0
            local escapeDistSq = escapeDist * escapeDist
            for id, enemyLight in pairs(potentialEnemyList) do
                if BanditUtils.AreEnemies(enemyLight.brain, brain) then
                    local dx = zx - enemyLight.x
                    local dy = zy - enemyLight.y
                    local distSq = dx*dx + dy*dy
                    if distSq > 0.01 and distSq < escapeDistSq then
                        threatCount = threatCount + 1
                        if distSq < closestDistSq then
                            closestDistSq = distSq
                            closestDX = dx
                            closestDY = dy
                        end
                        local dist = math.sqrt(distSq)
                        dx = dx / dist
                        dy = dy / dist
                        local weight = 1 / distSq
                        sx = sx + dx * weight
                        sy = sy + dy * weight
                    end
                end
            end
            if threatCount > 0 then
                local perpX = -sy
                local perpY = sx
                sx = sx + perpX * 0.25
                sy = sy + perpY * 0.25
            end
            local mag = math.sqrt(sx*sx + sy*sy)
            if mag < 0.001 then
                if closestDistSq < math.huge then
                    local dist = math.sqrt(closestDistSq)
                    sx = closestDX / dist
                    sy = closestDY / dist
                else
                    local angle = ZombRandFloat(0, math.pi * 2)
                    sx = math.cos(angle)
                    sy = math.sin(angle)
                end
            else
                sx = sx / mag
                sy = sy / mag
            end
            if brain.escapeX and brain.escapeY then
                sx = sx * 0.7 + brain.escapeX * 0.3
                sy = sy * 0.7 + brain.escapeY * 0.3
                local smag = math.sqrt(sx*sx + sy*sy)
                if smag > 0 then
                    sx = sx / smag
                    sy = sy / smag
                end
            end
            brain.escapeX = sx
            brain.escapeY = sy
            local baseDist = 6
            local scale = math.min(threatCount, 5) * 1.5
            local runDist = baseDist + scale
            local nbx = zx + sx * runDist
            local nby = zy + sy * runDist
            local nbz = zz
            Bandit.ClearTasks(bandit)
            local task = BanditUtils.GetMoveTask(0.01, nbx, nby, nbz, "Run", 12, false)
            task.time = 140 + threatCount * 30
            task.backwards = false
            table.insert(tasks, task)
        end
    elseif BanditCompatibility.GetGameVersion() >= 42 and enemiesBwd >= 2 then
        if not Bandit.HasMoveTask(bandit) and not Bandit.HasTaskType(bandit, "Shove") and not Bandit.HasTaskType(bandit, "Hit") then
            Bandit.ClearTasks(bandit)
            local mrad = math.atan2(sy, sx)
            local l = 1
            local nbx = zx + (l * math.cos(mrad))
            local nby = zy + (l * math.sin(mrad))
            local nbz = zz
            local task = BanditUtils.GetMoveTask(0.01, nbx, nby, nbz, "WalkBwdAim", l, false)
            task.backwards = true
            task.lock = false
            table.insert(tasks, task)
        end
    elseif healing then
        if not BanditBrain.HasTaskType(brain, "Bandage") then
            local task = {action="Bandage"}
            table.insert(tasks, task)
        end
    elseif firing then
        if not BanditBrain.HasTaskTypes(brain, {"Shoot", "Turn", "Aim", "Rack", "Equip", "Unequip", "Load", "Unload"}) then 
            Bandit.ClearTasks(bandit)
            if enemyCharacter:isAlive() then
                local veh = enemyCharacter:getVehicle()
                if veh then Bandit.Say(bandit, "CAR") end
                if bandit:isFacingObject(enemyCharacter, 0.1) then
                    for _, slot in pairs({"primary", "secondary"}) do
                        if weapons[slot].name then
                            if weapons[slot].bulletsLeft > 0 then
                                if not weapons[slot].racked then
                                    local stasks = BanditPrograms.Weapon.Rack(bandit, slot)
                                    for _, t in pairs(stasks) do table.insert(tasks, t) end
                                elseif not Bandit.IsAim(bandit) then
                                    local stasks = BanditPrograms.Weapon.Aim(bandit, enemyCharacter, slot)
                                    for _, t in pairs(stasks) do table.insert(tasks, t) end
                                elseif weapons[slot].bulletsLeft > 0 then
                                    local stasks = BanditPrograms.Weapon.Shoot(bandit, enemyCharacter, slot)
                                    for _, t in pairs(stasks) do table.insert(tasks, t) end
                                end
                                break
                            elseif (weapons[slot].type == "mag" and weapons[slot].magCount > 0) or
                                   (weapons[slot].type == "nomag" and weapons[slot].ammoCount > 0) then
                                Bandit.Say(bandit, "RELOADING")
                                local stasks = BanditPrograms.Weapon.Reload(bandit, slot)
                                for _, t in pairs(stasks) do table.insert(tasks, t) end
                                break
                            end
                        end
                    end
                else
                    bandit:faceThisObject(enemyCharacter)
                end
            elseif instanceof(enemyCharacter, "IsoPlayer") then
                local task = {action="Time", anim="Smoke", time=250}
                table.insert(tasks, task)
                Bandit.Say(bandit, "DEATH")
            end
        end
    elseif reload then
        if not BanditBrain.HasActionTask(brain) then
            for _, slot in pairs({"primary", "secondary"}) do
                if weapons[slot].name and bandit:isPrimaryEquipped(weapons[slot].name) then
                    Bandit.ClearTasks(bandit)
                    Bandit.Say(bandit, "RELOADING")
                    local stasks = BanditPrograms.Weapon.Reload(bandit, slot)
                    for _, t in pairs(stasks) do table.insert(tasks, t) end
                end
            end
        end
    elseif resupply then
        if not BanditBrain.HasTask(brain) then
            local stasks = BanditPrograms.Weapon.Resupply(bandit)
            for _, t in pairs(stasks) do table.insert(tasks, t) end
        end
    end

    return tasks
end

local function ManageSocialDistance(bandit)
    local bx, by, bz = bandit:getX(), bandit:getY(), bandit:getZ()
    local brain = BanditBrain.Get(bandit)
    if brain.hostile or brain.hostileP then return end

    local playerList = BanditPlayer.GetPlayers()
    for i = 0, playerList:size() - 1 do
        local player = playerList:get(i)
        if player then
            local px, py, pz = player:getX(), player:getY(), player:getZ()
            local veh = player:getVehicle()
            local asn = bandit:getActionStateName()
            local dist = math.sqrt(((bx - px) * (bx - px)) + ((by - py) * (by - py)))
            if bz == pz and dist < 3 and not veh and asn ~= "onground" then
                bandit:setUseless(true)
            else
                bandit:setUseless(false)
            end
        end
    end
end

local biteTab = {}

local function UpdateZombies(zombie)
    local player = getSpecificPlayer(0)
    if not player then return end

    local target = zombie:getTarget()
    if target and target:getVariableBoolean("Bandit") then
        zombie:setVariable("NoLungeAttack", true)
    else
        zombie:setVariable("NoLungeAttack", false)
    end

    if zombie:getVariableBoolean("Bandit") then return end

    local asn = zombie:getActionStateName()
    local zid = zombie:getModData().zid
    if zid and biteTab[zid] and (zombie:getBumpType() == "Bite" or zombie:getBumpType() == "BiteLow") and asn == "bumped" then
        local tick = biteTab[zid].tick
        if tick == 14 then
            local bandit = biteTab[zid].bandit
            local dist = BanditUtils.DistTo(zombie:getX(), zombie:getY(), bandit:getX(), bandit:getY())
            if dist < 0.8 then 
                if ZombRand(4) == 1 then zombie:playSound("ZombieBite") else zombie:playSound("ZombieScratch") end

                local teeth = BanditCompatibility.InstanceItem("Base.RollingPin")
                BanditCompatibility.Splash(bandit, teeth, zombie)
                bandit:setHitFromBehind(zombie:isBehind(bandit))
        
                if instanceof(bandit, "IsoZombie") then
                    bandit:setPlayerAttackPosition(bandit:testDotSide(zombie))
                end
        
                if not bandit:isOnKillDone() then
                    Bandit.ClearTasks(bandit)
                    bandit:Hit(teeth, zombie, 1.01, false, 1, false)
                    Bandit.UpdateInfection(bandit, 0.001)

                    local h = bandit:getHealth()
                    local id = BanditUtils.GetCharacterID(bandit)
                    local args = {id=id, h=h}
                    sendClientCommand(getSpecificPlayer(0), 'Sync', 'Health', args)
                end
            end
        elseif tick >= 16 then
            biteTab[zid] = nil
            zombie:getModData().zid = nil
            return
        end
        biteTab[zid].tick = tick + 1
        return
    end

    local stuckTime = zombie:getModData().stuckTime or 0

    if asn == "bumped" or asn == "onground" or asn == "climbfence" or asn == "getup" or asn == "turnalerted" then
        return
    end
    if zombie:isProne() then return end

    BanditBrain.Remove(zombie)
    if zombie:isUseless() then zombie:setUseless(false) end

    local phi = zombie:getPrimaryHandItem()
    if phi then zombie:setPrimaryHandItem(nil) end
    local shi = zombie:getSecondaryHandItem()
    if shi then zombie:setSecondaryHandItem(nil) end

    local target = zombie:getTarget()
    if target and instanceof(target, "IsoZombie") then
        zombie:setVariable("ZombieBiteDone", true)
        zombie:setNoTeeth(true)
    else
        zombie:setNoTeeth(false)
    end

    local zx, zy, zz = zombie:getX(), zombie:getY(), zombie:getZ()
    local px, py, pz = player:getX(), player:getY(), player:getZ()
    local distPlayer2 = ((px - zx) * (px - zx)) + ((py - zy) * (py - zy))
    if distPlayer2 < 4 and math.abs(pz - zz) < 0.3 then return end

    local banditList = BanditZombie.CacheLightB
    local dist2max = math.huge
    local banditCached = nil
    for _, bandit in pairs(banditList) do
        local dist2 = ((bandit.x - zx) * (bandit.x - zx)) + ((bandit.y - zy) * (bandit.y - zy))
        if dist2 < dist2max then
            dist2max = dist2
            banditCached = bandit
        end
    end

    if banditCached and dist2max < 400 then
        local bandit = BanditZombie.Cache[banditCached.id]

        if dist2max > 9 then
            if zombie:CanSee(bandit) then zombie:pathToCharacter(bandit) end
        else
            if zombie and bandit then
                -- [LCC POC] Keep Bandits' own pursuit / Bite pipeline without
                -- constructing a vanilla zombie -> Bandit combat relationship.
                zombie:pathToCharacter(bandit)
            end

            if dist2max < 0.64 and math.abs(zz - banditCached.z) < 0.3 then
                local isWallTo = zombie:getSquare():isSomethingTo(bandit:getSquare())
                if not isWallTo then
                    if zombie:isFacingObject(bandit, 0.3) then
                        local attackingZombiesNumber = 0
                        for id, attackingZombie in pairs(BanditZombie.CacheLightZ) do
                            if math.abs(attackingZombie.x - banditCached.x) + math.abs(attackingZombie.y - banditCached.y) < 1 then
                                local dist = math.sqrt(((attackingZombie.x - banditCached.x) * (attackingZombie.x - banditCached.x)) + ((attackingZombie.y - banditCached.y) * (attackingZombie.y - banditCached.y)))
                                if dist < 0.6 then
                                    attackingZombiesNumber = attackingZombiesNumber + 1
                                    if attackingZombiesNumber > 2 then break end
                                end
                            end
                        end

                        if attackingZombiesNumber > 2 then
                            if not Bandit.HasTaskType(bandit, "Die") then
                                Bandit.ClearTasks(bandit)
                                local task = {action="Die", lock=true, anim="Die", time=300}
                                Bandit.AddTask(bandit, task)
                            end
                            return
                        end

                        if zombie:getBumpType() ~= "Bite" and zombie:getBumpType() ~= "BiteLow" and asn ~= "staggerback" then
                            bandit:setZombiesDontAttack(true)
                            if bandit:isProne() or bandit:isCrawling() then
                                zombie:setBumpType("BiteLow")
                            else
                                zombie:setBumpType("Bite")
                            end
                            local zid = BanditUtils.GetCharacterID(zombie)
                            zombie:getModData().zid = zid 
                            biteTab[zid] = {bandit=bandit, tick=0}
                        end
                    else
                        zombie:faceThisObject(bandit)
                    end
                end
            end
        end
    end
end

local function ProcessTask(bandit, task)
    if not task.action then return end
    if not task.state then task.state = "NEW" end

    if task.state == "NEW" then
        if not task.time then task.time = 1000 end
        if task.action ~= "Shoot" and task.action ~= "Aim" and task.action ~= "Rack" and task.action ~= "Load" then
            Bandit.SetAim(bandit, false)
        end
        if task.action ~= "Move" and task.action ~= "GoTo" then
            if Bandit.IsMoving(bandit) then Bandit.SetMoving(bandit, false) end
        end
        if task.sound then
            local play = true
            if task.soundDistMax then
                local player = getSpecificPlayer(0)
                local dist = BanditUtils.DistTo(bandit:getX(), bandit:getY(), player:getX(), player:getY())
                if dist > task.soundDistMax then play = false end
            end
            if play then
                local emitter = bandit:getEmitter()
                if not emitter:isPlaying(task.sound) then emitter:playSound(task.sound) end
            end
        end
        if task.anim then bandit:setBumpType(task.anim) end
        local done = ZombieActions[task.action].onStart(bandit, task)
        if done then task.state = "WORKING" end
    elseif task.state == "WORKING" then
        local decrement = 1 / ((getAverageFPS() + 0.5) * 0.01666667)
        task.time = task.time - decrement
        local done = ZombieActions[task.action].onWorking(bandit, task)
        if done or task.time <= 0 then task.state = "COMPLETED" end
    elseif task.state == "COMPLETED" then
        if task.sound then
            local emitter = bandit:getEmitter()
            if not emitter:isPlaying(task.sound) then bandit:playSound(task.sound) end
        end
        if task.endurance then Bandit.UpdateEndurance(bandit, task.endurance) end
        local done = ZombieActions[task.action].onComplete(bandit, task)
        if done then Bandit.RemoveTask(bandit) end
    end
end

local function GenerateTask(bandit)
    local tasks = {}
    local enduranceTasks = ManageEndurance(bandit)
    if #enduranceTasks > 0 then for _, t in pairs(enduranceTasks) do table.insert(tasks, t) end end

    if #tasks == 0 then
        local healingTasks = ManageHealth(bandit)
        if #healingTasks > 0 then for _, t in pairs(healingTasks) do table.insert(tasks, t) end end
    end

    if #tasks == 0 then
        local combatTasks = ManageCombat(bandit)
        if #combatTasks > 0 then for _, t in pairs(combatTasks) do table.insert(tasks, t) end end
    end

    if #tasks == 0 then
        local colissionTasks = ManageCollisions(bandit)
        if #colissionTasks > 0 then for _, t in pairs(colissionTasks) do table.insert(tasks, t) end end
    end

    if #tasks == 0 and not Bandit.HasTask(bandit) then
        local program = Bandit.GetProgram(bandit)
        if program and program.name and program.stage then
            local res = ZombiePrograms[program.name][program.stage](bandit)
            if res.status and res.next then
                Bandit.SetProgramStage(bandit, res.next)
                for _, task in pairs(res.tasks) do table.insert(tasks, task) end
            else
                local task = {action="Time", anim="Shrug", time=200}
                table.insert(tasks, task)
            end
        end
    end

    if #tasks > 0 then
        local brain = BanditBrain.Get(bandit)
        for _, task in pairs(tasks) do table.insert(brain.tasks, task) end
    end
end

local function OnBanditUpdate(zombie)
    local ts = getTimestampMs()
    if isServer() then return end

    local isMP = getWorld():getGameMode() == "Multiplayer"
    if isMP then
        local i1 = zombie:getPrimaryHandItem()
        local i2 = zombie:getSecondaryHandItem()
        if (i1 or i2) and IsWindowClose(zombie) then
            if i1 then
                zombie:setPrimaryHandItem(nil)
                zombie:setVariable("BanditPrimary", "")
                zombie:setVariable("BanditPrimaryType", "")
            end
            if i2 then zombie:setSecondaryHandItem(nil) end
        end
    end

    if not Bandit.Engine then return end
    if BanditCompatibility.IsReanimatedForGrappleOnly(zombie) then return end
    if BanditCompatibility.IsRagdoll(zombie) then return end

    local target = zombie:getTarget()
    if target and instanceof(target, "IsoPlayer") and not target:getVariableBoolean("Bandit") then
        if zombie:isCrawling() then
            local zx, zy, zz = zombie:getX(), zombie:getY(), zombie:getZ()
            local px, py, pz = target:getX(), target:getY(), target:getZ()
            local dist = math.sqrt(((zx - px) * (zx - px)) + ((zy - py) * (zy - py)))
            if dist < 0.80 and math.abs(zz - pz) < 0.3 and zombie:CanSee(target) then
                local isWallTo = zombie:getSquare():isSomethingTo(target:getSquare())
                if not isWallTo and zombie:isFacingObject(target, 0.3) then
                    zombie:changeState(LungeState.instance())
                    zombie:getPathFindBehavior2():cancel()
                    zombie:setPath2(nil)
                    return
                end
            end
        end
    end

    local id = BanditUtils.GetZombieID(zombie)
    local brain = BanditBrain.Get(zombie)
    local gmd = GetBanditClusterData(id)
    if gmd and gmd[id] then
        if not zombie:getVariableBoolean("Bandit") then
            brain = gmd[id]
            Banditize(zombie, brain)
        end
    else
        if zombie:getVariableBoolean("Bandit") then Zombify(zombie) end
    end

    UpdateZombies(zombie)

    local asn = zombie:getActionStateName()
    if asn == "onground" then
        local h = zombie:getHealth()
        if h <=0 then
            zombie:setAttackedBy(getCell():getFakeZombieForHit())
            zombie:die()
        end
    end

    if not zombie:getVariableBoolean("Bandit") then return end
    if not brain then return end

    if BanditZombie.CacheLightB[id] then 
        zombie:setUseless(false)
    else
        zombie:setUseless(true)
        return
    end

    local bandit = zombie
    if BanditCompatibility.GetGameVersion() >= 42 then bandit:setAnimatingBackwards(false) end
    bandit:setWalkType(bandit:getVariableString("BanditWalkType"))
    bandit:setSpeedMod(1)
    Bandit.SurpressZombieSounds(bandit)
    if not brain.eatBody then bandit:setEatBodyTarget(nil, false) end
    Bandit.ApplyVisuals(bandit, brain)
    ManageTorch(bandit, brain)
    ManageOnFire(bandit)
    ManageSpeechCooldown(brain)

    local continue = ManageActionState(bandit)
    if not continue then return end
    if isMP then ManageSocialDistance(bandit) end
    if bandit:isCrawling() then Bandit.Say(bandit, "DEAD") end

    GenerateTask(bandit)
    local task = Bandit.GetTask(bandit)
    if task then ProcessTask(bandit, task) end

    local elapsed = getTimestampMs() - ts
    if elapsed < 1 then 
        iter1 = iter1 + 1 
        sum1 = sum1 + elapsed
    elseif elapsed < 5 then 
        iter2 = iter2 + 1
        sum2 = sum2 + elapsed
    else
        iter3 = iter3 + 1
        sum3 = sum3 + elapsed
    end
end

local function OnHitZombie(zombie, attacker, bodyPartType, handWeapon)
    if not zombie:getVariableBoolean("Bandit") then return end

    local bandit = zombie
    Bandit.AddVisualDamage(bandit, handWeapon)
    Bandit.ClearTasks(bandit)
    Bandit.Say(bandit, "HIT", true)
    if Bandit.IsSleeping(bandit) then
        local task = {action="Time", lock=true, anim="GetUp", time=150}
        Bandit.ClearTasks(bandit)
        Bandit.AddTask(bandit, task)
        Bandit.SetSleeping(bandit, false)
        Bandit.SetProgramStage(bandit, "Prepare")
    end

    BanditPlayer.CheckFriendlyFire(bandit, attacker)

    if handWeapon:isRanged() and instanceof(attacker, "IsoPlayer") then
        local bodyPartTypes = {
            Foot_R = {}, Foot_L = {}, LowerLeg_R = {}, LowerLeg_L = {}, UpperLeg_R = {}, UpperLeg_L = {},
            Groin = {serious = true}, Neck = {serious = true}, Head = {insta = true},
            Torso_Lower = {serious = true}, Torso_Upper = {serious = true}, UpperArm_R = {}, UpperArm_L = {},
            ForeArm_R = {}, ForeArm_L = {}, Hand_R = {}, Hand_L = {}
        }

        for k, tab in pairs(bodyPartTypes) do
            if BodyPartType[k] == bodyPartType then
                local idx = BodyPartType.ToIndex(bodyPartType)
                local def = bandit:getBodyPartClothingDefense(idx, false, true)
                local rnd = ZombRand(100)
                if rnd > def then
                    if tab.insta then
                        bandit:Kill(nil)
                        return
                    end
                    if tab.serious then
                        local maxDmg = handWeapon:getMaxDamage() or 1
                        local extraDmg = 0.26 * maxDmg
                        local health = bandit:getHealth() - extraDmg
                        if health <=0 then
                            bandit:Kill(nil)
                            return
                        else
                            bandit:setHealth(health)
                        end
                    end
                end
            end
        end
    end
end

local function OnZombieDead(bandit)
    if bandit:getVariableBoolean("Bandit") then 
        local brain = BanditBrain.Get(bandit)
        local inventory = bandit:getInventory()
        local items = ArrayList.new()
        local veh = bandit:getVehicle()
        if veh then veh:exit(bandit) end

        inventory:getAllEvalRecurse(predicateRemovable, items)
        for i=0, items:size()-1 do
            local item = items:get(i)
            inventory:Remove(item)
            inventory:setDrawDirty(true)
        end

        local stuckLocationList = {"MeatCleaver in Back", "Axe Back", "Knife in Back", "Knife Left Leg", "Knife Right Leg", "Knife Shoulder", "Knife Stomach"}
        for _, stuckLocation in pairs(stuckLocationList) do
            local attachedItem = bandit:getAttachedItem(stuckLocation)
            if attachedItem then
                inventory:AddItem(attachedItem)
                inventory:setDrawDirty(true)
            end
        end

        if brain.bag and brain.bag == "Briefcase" then
            local bag = BanditCompatibility.InstanceItem("Base.Briefcase")
            local bagContainer = bag:getItemContainer()
            if bagContainer then
                local rn = ZombRand(3)
                if rn == 0 then
                    for i = 1, 1000 do bagContainer:AddItem(instanceItem("Base.Money")) end
                elseif rn == 1 then
                    bagContainer:AddItem(BanditCompatibility.InstanceItem("Base.Corset_Black"))
                    bagContainer:AddItem(BanditCompatibility.InstanceItem("Base.StockingsBlack"))
                    bagContainer:AddItem(BanditCompatibility.InstanceItem("Base.Hat_PeakedCapArmy"))
                elseif rn == 2 then
                    bagContainer:AddItem(BanditCompatibility.InstanceItem("Base.Machete"))
                    if BanditCompatibility.GetGameVersion() >= 42 then
                        bagContainer:AddItem(BanditCompatibility.InstanceItem("Base.Hat_HalloweenMaskVampire"))
                        bagContainer:AddItem(BanditCompatibility.InstanceItem("Base.BlackRobe"))
                    end
                end
                bandit:getSquare():AddWorldInventoryItem(bag, ZombRandFloat(0.2, 0.8), ZombRandFloat(0.2, 0.8), 0)
            end
        end

        if brain.key and ZombRand(3) == 1 then
            local item = BanditCompatibility.InstanceItem("Base.Key1")
            item:setKeyId(brain.key)
            item:setName("Building Key")
            inventory:AddItem(item)
            Bandit.UpdateItemsToSpawnAtDeath(bandit, brain)
        end

        Bandit.Say(bandit, "DEAD", true)
        local player = getSpecificPlayer(0)
        local killer = bandit:getAttackedBy()
        if killer and killer == player then player:setZombieKills(player:getZombieKills() - 1) end

        bandit:setUseless(false)
        bandit:setReanim(false)
        bandit:setVariable("Bandit", false)
        bandit:setVariable("LimpSpeed", 0.3)
        bandit:setVariable("RunSpeed", 0.3)
        bandit:setVariable("WalkSpeed", 0.3)
        bandit:setPrimaryHandItem(nil)
        bandit:clearAttachedItems()
        bandit:resetEquippedHandsModels()
        bandit:getModData().isDeadBandit = true

        local args = {id = brain.id}
        sendClientCommand(player, 'Commands', 'BanditRemove', args)
        BanditBrain.Remove(bandit)
    end
end

local function OnDeadBodySpawn(body)
    local md = body:getModData()
    if md.isDeadBandit and md.isDeadBandit == true then
        local player = getSpecificPlayer(0)
        md.isDeadBandit = false
        local args = {x = body:getX(), y = body:getY(), z = body:getZ(), id = md.brainId}
        sendClientCommand(player, 'Commands', 'BanditCorpse', args)
    end
end

local function perf()
    print ("BANDIT UPDATE REPORT: invocations: " .. "short: " .. iter1 .. "( " .. sum1.. "), medium: " .. iter2 .. "(" .. sum2 .. "), long: " .. iter3 .. "(" .. sum3.. ")")
    iter1 = 0
    iter2 = 0
    iter3 = 0
    sum1 = 0
    sum2 = 0
    sum3 = 0
end

Events.OnZombieUpdate.Remove(OnBanditUpdate)
Events.OnZombieUpdate.Add(OnBanditUpdate)

Events.OnHitZombie.Remove(OnHitZombie)
Events.OnHitZombie.Add(OnHitZombie)

Events.OnZombieDead.Remove(OnZombieDead)
Events.OnZombieDead.Add(OnZombieDead)

Events.OnDeadBodySpawn.Remove(OnDeadBodySpawn)
Events.OnDeadBodySpawn.Add(OnDeadBodySpawn)

-- Events.EveryOneMinute.Remove(perf)
-- Events.EveryOneMinute.Add(perf)
