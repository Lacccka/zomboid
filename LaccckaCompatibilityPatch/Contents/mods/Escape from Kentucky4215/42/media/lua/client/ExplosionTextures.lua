local textureName = "theMH_MkII_SFX"
local currentFrame = 1 --From 1 to 12
local duration, toNextFrameTime = 0, 0.04
local lastFrame = nil
local start = false
local occurSquare = nil

local function OnThrowableExplode(throwable, square)
    if square == nil then return end
    occurSquare = square
    start = true
end

Events.OnThrowableExplode.Add(OnThrowableExplode)

local function OnTick()
    if not start then return end
    if occurSquare == nil  then return end

    if currentFrame >= 13 then
        start = false
        occurSquare = nil
        currentFrame = 1
        return
    end

    if lastFrame == nil then
       lastFrame = occurSquare:AddWorldInventoryItem("Base." .. textureName .. tostring(currentFrame), 0.1, 0.1, 0.1, true)
    end

    duration = duration + getGameTime():getTimeDelta()
    if duration >= toNextFrameTime then
        currentFrame = currentFrame + 1
        duration = 0
        lastFrame:getWorldItem():removeFromSquare()
        lastFrame:getWorldItem():removeFromWorld()
        lastFrame = nil
    end
end

Events.OnTick.Add(OnTick)