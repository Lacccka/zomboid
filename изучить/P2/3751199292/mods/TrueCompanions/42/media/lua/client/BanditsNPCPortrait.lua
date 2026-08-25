--
-- True Companions - NPC portrait (real head-and-shoulders 3D render)
--
-- Renders the actual character's face/upper body using the engine's ISUI3DModel -- the
-- same widget the vanilla Character screen / character-creation avatar use. To avoid the
-- FBO accumulation that crashed an earlier attempt, we keep ONE shared model instance for
-- the whole mod and re-parent it (as a child) onto whichever window is showing it; only a
-- single GPU buffer ever exists.
--
-- IMPORTANT (matches CharacterCreationAvatar): the model must be added as a CHILD of the
-- owning window (not a top-level UIManager element), and the zoom/offset use small values
-- (zoom 14, yOffset -0.85) to frame head-and-shoulders -- big offsets push the figure off
-- the panel and leave an empty box.
--
-- Gated by the sandbox option "Show 3D NPC portrait" (default OFF, experimental).
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.Portrait = {}
local Pt = BanditsNPC.Portrait

function Pt.Enabled()
    return (BanditsNPC.Opt and BanditsNPC.Opt("NPCPortrait3D", false)) == true
end

-- head-and-shoulders framing (tunable). Same scale as the character-creation avatar's
-- zoomed view: zoom 14, yOffset -0.85.
Pt.ZOOM = 14
Pt.YOFFSET = -0.85

local model = nil
local curChar = nil

local function ensure()
    if model then return model end
    if not rawget(_G, "ISUI3DModel") then return nil end
    local ok = pcall(function()
        model = ISUI3DModel:new(0, 0, 100, 150)
        model:initialise()
        model:instantiate()
        model.backgroundColor = { r = 0.08, g = 0.08, b = 0.10, a = 0.85 }
        model.borderColor = { r = 0.5, g = 0.5, b = 0.55, a = 0.85 }
        model:setState("idle")
        model:setDirection(IsoDirections.S)
        if model.setIsometric then model:setIsometric(false) end   -- straight-on portrait, not the iso 3/4 "looking down-left" angle
        model:setAnimateWhilePaused(true)
    end)
    if not ok then model = nil end
    return model
end

-- Show the head-and-shoulders portrait of `npc` as a CHILD of `owner` at the owner-relative
-- rect (x,y,w,h). Call every render while it should be visible. Returns true if shown.
function Pt.Show(owner, npc, x, y, w, h)
    if not (Pt.Enabled() and owner and npc) then return false end
    local m = ensure()
    if not m then return false end
    local ok = pcall(function()
        -- re-add if not parented to this window OR silently detached (removeChild leaves
        -- .parent set, so also verify we're really in the owner's children map)
        local attached = (m.parent == owner) and (not owner.children or (m.ID and owner.children[m.ID] ~= nil))
        if not attached then
            if m.parent and m.parent.removeChild then m.parent:removeChild(m) end
            m.parent = nil
            owner:addChild(m)
        end
        m:setX(x); m:setY(y)
        m:setWidth(w); m:setHeight(h)
        if m.javaObject then m.javaObject:setWidth(w); m.javaObject:setHeight(h) end
        m:setVisible(true)
        if curChar ~= npc then
            curChar = npc
            m:setCharacter(npc)
            m:setZoom(Pt.ZOOM)
            m:setYOffset(Pt.YOFFSET)
        end
    end)
    return ok and true or false
end

function Pt.Hide()
    if model then
        pcall(function()
            if model.parent and model.parent.removeChild then model.parent:removeChild(model) end
            -- ISUIElement:removeChild does NOT clear the child's .parent field -- without
            -- this, Show()'s `m.parent ~= owner` check thinks we're still attached and never
            -- re-adds the portrait (the "gone until you close and reopen V" bug)
            model.parent = nil
            model:setVisible(false)
        end)
    end
    curChar = nil
end
