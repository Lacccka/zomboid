--
-- Bandits NPC - Companions Overhaul - Native inventory access
--
-- Opens the companion's inventory using the game's REAL loot window
-- (getPlayerLoot) by injecting the NPC's container as a loot tab. This gives
-- the authentic inventory experience -- icons, drag-and-drop, right-click
-- "Grab/Transfer", tooltips -- identical to looting any container.
--
-- The loot window rebuilds its container list constantly (it scans the world),
-- so we hook its refreshBackpacks to re-add our NPC container every rebuild,
-- and remove the hook when the companion dies / moves out of range / a new
-- session starts. Everything is pcall-guarded; on any failure we fall back to
-- the simple two-pane window.
--

require "ISUI/BanditsNPCInventoryWindow"

BanditsNPC = BanditsNPC or {}
BanditsNPC.Inventory = BanditsNPC.Inventory or {}

local RANGE = 3.5

function BanditsNPC.Inventory.EndNative()
    local s = BanditsNPC.Inventory._session
    if not s then return end
    BanditsNPC.Inventory._session = nil
    pcall(function()
        if s.loot and s.orig then
            -- = nil, NOT = s.orig (v0.75.19). refreshBackpacks is a CLASS method
            -- (ISInventoryPage.lua:1537) and is never an instance field in vanilla, so
            -- our hook was an instance field SHADOWING it. Re-assigning s.orig left that
            -- shadow in place, pinned to a snapshot of the class method taken when our
            -- session started -- silently defeating any mod that patches the class
            -- afterwards. Removing the field lets lookups fall through to whatever the
            -- class currently holds. Only remove OUR hook: if someone wrapped us since,
            -- theirs is the live value and must be left alone.
            if s.loot.refreshBackpacks == s.hook then
                s.loot.refreshBackpacks = nil
            end
            s.loot:refreshBackpacks()
        end
    end)
    pcall(function()
        if s.loot and s.oldX then
            s.loot:setX(s.oldX)
            s.loot:setY(s.oldY)
        end
    end)
end

function BanditsNPC.Inventory.OpenNative(zombie)
    local player = getSpecificPlayer(0)
    if not (player and zombie) then return end

    BanditsNPC.Inventory.EndNative()

    local ok = pcall(function()
        local pnum = player:getPlayerNum()
        local loot = getPlayerLoot(pnum)
        if not loot then error("no loot window") end

        local npcInv = zombie:getInventory()
        pcall(function() npcInv:setExplored(true) end)

        local brain = BanditBrain.Get(zombie)
        local label = (brain and brain.fullname) or "Companion"

        -- Re-add our container tab every time the window rebuilds its list.
        local orig = loot.refreshBackpacks
        local hook = function(selfPage)
            orig(selfPage)
            pcall(function()
                selfPage:addContainerButton(npcInv, nil, label, label)
            end)
        end
        loot.refreshBackpacks = hook

        BanditsNPC.Inventory._session = { loot = loot, orig = orig, hook = hook, zombie = zombie, npcInv = npcInv, player = player }

        -- show both windows so drag-and-drop has a target, then select the NPC tab
        pcall(function() getPlayerInventory(pnum):setVisible(true) end)
        loot:setVisible(true)
        loot:refreshBackpacks()
        loot:selectButtonForContainer(npcInv)

        -- bring the loot window to the centre so it's obvious; remember the
        -- original spot so EndNative can put it back.
        local s = BanditsNPC.Inventory._session
        s.oldX, s.oldY = loot:getX(), loot:getY()
        loot:setX((getCore():getScreenWidth() - loot:getWidth()) / 2)
        loot:setY((getCore():getScreenHeight() - loot:getHeight()) / 2)
    end)

    if not ok then
        print("[BanditsNPC] Native inventory failed; using simple window.")
        BanditsNPCInventoryWindow.OpenFor(zombie)
    end
end

-- Close the session when the companion dies or the player walks away.
local function monitor()
    local s = BanditsNPC.Inventory._session
    if not s then return end

    local z, p = s.zombie, s.player
    if not z or not z:isAlive() or not p then
        BanditsNPC.Inventory.EndNative()
        return
    end
    local dist = BanditUtils.DistTo(p:getX(), p:getY(), z:getX(), z:getY())
    if dist > RANGE then
        BanditsNPC.Inventory.EndNative()
    end
end

Events.OnPlayerUpdate.Add(monitor)
