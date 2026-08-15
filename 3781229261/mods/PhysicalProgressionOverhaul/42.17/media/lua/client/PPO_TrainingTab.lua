require "PPO_TrainingPanel"

PPO = PPO or {}
PPO.TrainingTab = PPO.TrainingTab or { Installed = false }

local TrainingTab = PPO.TrainingTab

-- The vanilla window builds its own five views first; this only appends a
-- sixth. A failure inside the addition leaves exactly what vanilla built.
local function addView(window)
    if type(window) ~= "table" then return false end
    local panel = window.panel
    if type(panel) ~= "table" or type(panel.addView) ~= "function" then
        return false
    end
    local view = PPO.TrainingPanel:new(0, 8, window.width or 0,
        (window.height or 0) - 8, window.playerNum)
    if type(view.initialise) == "function" then view:initialise() end
    panel:addView(getText("IGUI_PPO_TrainingTab"), view)
    window.ppoTrainingView = view
    return true
end

function TrainingTab.install()
    if TrainingTab.Installed then return false end
    if ISCharacterInfoWindow == nil
            or ISCharacterInfoWindow.createChildren == nil then
        return false
    end

    local original = ISCharacterInfoWindow.createChildren
    TrainingTab.original = original
    ISCharacterInfoWindow.createChildren = function(self)
        original(self)
        pcall(addView, self)
    end
    TrainingTab.Installed = true
    return true
end

TrainingTab.install()
