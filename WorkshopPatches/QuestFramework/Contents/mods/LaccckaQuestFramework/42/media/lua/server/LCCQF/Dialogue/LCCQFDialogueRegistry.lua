-- Server-side dialogue overlay. Authored static dialogue remains in DialogueContent;
-- generated faction/world NPCs register reusable server-owned dialogue definitions here.
require "LCCQF/LCCQFConstants"
require "LCCQF/Content/LCCQFDialogueContent"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Static = LCCQF.DialogueContent
local Registry = LCCQF.DialogueRegistry or {}
local generated = Registry.generated or {}

local function validId(value)
    return type(value) == "string" and value ~= "" and #value <= C.MAX_IDENTIFIER_LENGTH
end

local function validate(dialogueId, dialogue)
    if not validId(dialogueId) or type(dialogue) ~= "table" then return false, "invalid dialogue" end
    if not validId(dialogue.start) or type(dialogue.nodes) ~= "table" then return false, "invalid dialogue graph" end
    if type(dialogue.nodes[dialogue.start]) ~= "table" then return false, "dialogue start node missing" end
    for nodeId, node in pairs(dialogue.nodes) do
        if not validId(nodeId) or type(node) ~= "table" or not validId(node.textKey) then
            return false, "invalid dialogue node"
        end
        if node.choices ~= nil and type(node.choices) ~= "table" then return false, "invalid dialogue choices" end
    end
    return true
end

function Registry.Register(dialogueId, dialogue)
    local ok, errorText = validate(dialogueId, dialogue)
    if not ok then return false, errorText end
    generated[dialogueId] = dialogue
    return true
end

function Registry.Get(dialogueId)
    if not validId(dialogueId) then return nil end
    return generated[dialogueId] or Static.Get(dialogueId)
end

Registry.generated = generated
LCCQF.DialogueRegistry = Registry
return Registry
