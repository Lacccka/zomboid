require "TimedActions/ISBaseTimedAction"

-- 下挂喷火器 (Flamethrower) underbarrel ability.
-- 手持安装了 Gunpart.Flamethrower 的枪械，瞄准后按 G (LauchGrenadelauncherat)
-- 会朝瞄准方向喷射火焰：击倒 MinAngle=0.96、MaxRange=10 范围内的僵尸，使其起火并小幅击退。
-- 一次装入 5 升汽油 (Fluid.Petrol) 可开火 5 次，装填时间与下挂榴弹发射器一致 (time=100)。
-- 开火时会产生一束向前延伸 10 格的枪口火焰，并在枪口处产生照明光晕 (屏幕空间，约 0.2 秒)。

local AWCWF_Stool_Flamethrower = {
    ["Flamethrower"] = {
        LaunchSound = "Flamethrower",
        LoadSound = "LauncherReload",
        MinAngle = 0.94,
        MaxRange = 11,
        KnockBack = 0.6,    -- 命中僵尸时沿远离玩家方向击退的距离（格）
        FuelLiters = 5.0,   -- 一次装入 5 升汽油
        MaxShots = 5,       -- 装满后可开火 5 次
    },
}

-- 枪口火焰贴图路径（相对 media/ 目录）。
--   火焰喷射锥体：muzzle-flash-side.png
--   枪口照明光晕：muzzle-flash-star.png
local FlameJetTexture  = "media/textures/Flamethrower.png"
local FlameGlowTexture = "media/textures/"
local FlashDurationMs  = 200   -- 枪口火焰/照明的持续时长（毫秒）

local FuelKey = "FlamethrowerShots"

-- 返回 (MainGun, config)，若当前手持枪械的 Stool 槽安装了喷火器；否则返回 nil。
local function GetFlamethrower(playerObj)
    local MainGun = playerObj and playerObj:getPrimaryHandItem()
    if not MainGun then return nil end
    if not (MainGun:IsWeapon() and MainGun:isRanged()) then return nil end
    local part = MainGun:getWeaponPart("Stool")
    if not part then return nil end
    local cfg = AWCWF_Stool_Flamethrower[part:getType()]
    if not cfg then return nil end
    return MainGun, cfg
end

-- 剩余可开火次数（0 = 未装填）。
local function GetShotsLeft(MainGun)
    local n = MainGun:getModData()[FuelKey]
    if type(n) == "number" then return n end
    return 0
end

local function SetShotsLeft(MainGun, n)
    if n <= 0 then
        MainGun:getModData()[FuelKey] = nil
    else
        MainGun:getModData()[FuelKey] = n
    end
end

-- 在玩家背包中寻找装有不少于 liters 升汽油的容器。
local function FindPetrolContainer(playerObj, liters)
    local inv = playerObj:getInventory()
    if not inv then return nil end
    local items = inv:getItems()
    if not items then return nil end
    for i = 0, items:size() - 1 do
        local item = items:get(i)
        local fc = item and item:getFluidContainer()
        if fc and fc:contains(Fluid.Petrol) and fc:getAmount() >= liters then
            return item
        end
    end
    return nil
end

-- ---------------------------------------------------------------------------
-- 枪口火焰 / 照明（屏幕空间，纯视觉，开火后约 0.2 秒内淡出）。
-- ---------------------------------------------------------------------------
local Flash = { active = false, startMs = 0, dirX = 0, dirY = -1 }
local FlameJetTex  = getTexture(FlameJetTexture)
local FlameGlowTex = getTexture(FlameGlowTexture)

local function TriggerFlash(fx, fy)
    Flash.active = true
    Flash.startMs = getTimestampMs()
    Flash.dirX = fx
    Flash.dirY = fy
end

local function OnPostRender()
    if not Flash.active then return end

    local player = getPlayer()
    if not player then
        Flash.active = false
        return
    end

    local elapsed = getTimestampMs() - Flash.startMs
    if elapsed >= FlashDurationMs then
        Flash.active = false
        return
    end
    local fade = 1 - elapsed / FlashDurationMs

    local renderer = getRenderer()
    local zoom = getCore():getZoom(0)
    if zoom <= 0 then zoom = 1 end

    local px, py, pz = player:getX(), player:getY(), player:getZ()
    local fx, fy = Flash.dirX, Flash.dirY

    -- 枪口起点：玩家前方一点点、胸口高度。
    local mx = px + fx * 0.5
    local my = py + fy * 0.75
    local mz = pz + 0.42

    -- 终点：前方 MaxRange (10) 格。
    local tx = px + fx * 10
    local ty = py + fy * 10

    local playerNum = player:getPlayerNum()
    local sx0 = isoToScreenX(playerNum, mx, my, mz) * zoom
    local sy0 = isoToScreenY(playerNum, mx, my, mz) * zoom
    local sx1 = isoToScreenX(playerNum, tx, ty, mz) * zoom
    local sy1 = isoToScreenY(playerNum, tx, ty, mz) * zoom

    local dx = sx1 - sx0
    local dy = sy1 - sy0
    local len = math.sqrt(dx * dx + dy * dy)
    if len < 1 then
        Flash.active = false
        return
    end
    dx = dx / len
    dy = dy / len
    local nx = -dy
    local ny = dx

    -- 火焰喷射锥体：从枪口到前方 10 格，逐渐变宽。
    if FlameJetTex then
        local wNear = 14 * zoom
        local wFar = 70 * zoom
        local nearLx = sx0 - nx * wNear
        local nearLy = sy0 - ny * wNear
        local nearRx = sx0 + nx * wNear
        local nearRy = sy0 + ny * wNear
        local farRx  = sx1 + nx * wFar
        local farRy  = sy1 + ny * wFar
        local farLx  = sx1 - nx * wFar
        local farLy  = sy1 - ny * wFar
        renderer:renderPoly(FlameJetTex, nearLx, nearLy, nearRx, nearRy, farRx, farRy, farLx, farLy,
            1.0, 0.65, 0.1, fade * 0.9)
    end

    -- 枪口照明光晕：多层同心叠加，形成向四周扩散的照明效果。
    if FlameGlowTex then
        local g1 = 40 * zoom
        renderer:renderPoly(FlameGlowTex, sx0 - g1, sy0 - g1, sx0 + g1, sy0 - g1, sx0 + g1, sy0 + g1, sx0 - g1, sy0 + g1,
            1.0, 0.8, 0.3, fade)
        local g2 = 90 * zoom
        renderer:renderPoly(FlameGlowTex, sx0 - g2, sy0 - g2, sx0 + g2, sy0 - g2, sx0 + g2, sy0 + g2, sx0 - g2, sy0 + g2,
            1.0, 0.7, 0.2, fade * 0.5)
        local g3 = 160 * zoom
        renderer:renderPoly(FlameGlowTex, sx0 - g3, sy0 - g3, sx0 + g3, sy0 - g3, sx0 + g3, sy0 + g3, sx0 - g3, sy0 + g3,
            1.0, 0.6, 0.15, fade * 0.25)
    end
end

-- 喷射火焰：击倒并点燃瞄准方向锥形范围内的僵尸，消耗一次装填，并触发枪口火焰/照明。
local function FireFlamethrower(playerObj, MainGun, cfg)
    playerObj:getEmitter():playSound(cfg.LaunchSound)

    local px = playerObj:getX()
    local py = playerObj:getY()
    local pz = playerObj:getZ()

    local fwd = playerObj:getForwardDirection()
    local fx, fy = 0, -1
    if fwd then
        fx = fwd:getX()
        fy = fwd:getY()
    end
    local flen = math.sqrt(fx * fx + fy * fy)
    if flen < 0.0001 then
        fx, fy = 0, -1
        flen = 1
    end
    fx = fx / flen
    fy = fy / flen

    local range = cfg.MaxRange
    local cell = getCell()
    for dy = -range, range do
        for dx = -range, range do
            local dist = math.sqrt(dx * dx + dy * dy)
            if dist <= range and dist > 0.0001 then
                local vx = dx / dist
                local vy = dy / dist
                if fx * vx + fy * vy >= cfg.MinAngle then
                    local sq = cell:getGridSquare(px + dx, py + dy, pz)
                    if sq then
                        local zombies = sq:getMovingObjects()
                        for i = 0, zombies:size() - 1 do
                            local zombie = zombies:get(i)
                            if instanceof(zombie, "IsoZombie") then
                                zombie:knockDown(false)
                                zombie:SetOnFire()
                                -- 小幅击退：把僵尸沿远离玩家的方向推开一小段。
                                zombie:setX(zombie:getX() + vx * cfg.KnockBack)
                                zombie:setY(zombie:getY() + vy * cfg.KnockBack)
                            end
                        end
                    end
                end
            end
        end
    end

    SetShotsLeft(MainGun, GetShotsLeft(MainGun) - 1)
    TriggerFlash(fx, fy)
end

-- 装填定时动作：与下挂榴弹发射器相同的时间 (time=100)，完成后消耗 5 升汽油并装满 5 发。
ISFlamethrowerReload = ISBaseTimedAction:derive("ISFlamethrowerReload")

function ISFlamethrowerReload:isValid()
    if not self.weapon then return false end
    local part = self.weapon:getWeaponPart("Stool")
    if not part then return false end
    if AWCWF_Stool_Flamethrower[part:getType()] == nil then return false end
    if GetShotsLeft(self.weapon) > 0 then return false end
    return true
end

function ISFlamethrowerReload:start()
    self.character:playSound(self.cfg.LoadSound)
end

function ISFlamethrowerReload:stop()
    ISBaseTimedAction.stop(self)
end

function ISFlamethrowerReload:perform()
    ISBaseTimedAction.perform(self)
    local fuel = FindPetrolContainer(self.character, self.cfg.FuelLiters)
    if fuel then
        local fc = fuel:getFluidContainer()
        local newAmount = fc:getAmount() - self.cfg.FuelLiters
        if newAmount < 0 then newAmount = 0 end
        fc:adjustAmount(newAmount)
        SetShotsLeft(self.weapon, self.cfg.MaxShots)
    end
end

function ISFlamethrowerReload:new(character, time, weapon, cfg)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.character = character
    o.weapon = weapon
    o.cfg = cfg
    o.stopOnWalk = false
    o.stopOnRun = true
    o.maxTime = time
    return o
end

local function OnKeyPressed(_key)
    if _key ~= getCore():getKey("LauchGrenadelauncherat") then
        return
    end
    local playerObj = getPlayer()
    if not playerObj then return end
    local MainGun, cfg = GetFlamethrower(playerObj)
    if not MainGun then return end
    if not playerObj:isAiming() then return end

    if GetShotsLeft(MainGun) > 0 then
        FireFlamethrower(playerObj, MainGun, cfg)
    else
        if FindPetrolContainer(playerObj, cfg.FuelLiters) then
            ISTimedActionQueue.add(ISFlamethrowerReload:new(playerObj, 100, MainGun, cfg))
        end
    end
end

local function Check()
    Events.OnKeyPressed.Add(OnKeyPressed)
end

local function OnGameStartCheckModinfo()
    Events.OnGameStart.Add(Check)
end
Events.OnGameStart.Add(OnGameStartCheckModinfo)

Events.OnPostRender.Add(OnPostRender)
