require "ExtractionMode/Config"
require "ExtractionMode/Infection"
require "TimedActions/ISReadABook"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local Infection = ExtractionMode.Infection
local Reading = {}

-- Reading actions are constructed independently on the client and server in
-- multiplayer, so adjust the shared duration calculation instead of trying to
-- advance only the local progress bar. This covers skill books and ordinary
-- literature while preserving all vanilla trait, glasses, and sitting bonuses.
if ISReadABook and ISReadABook.ExtractionModeOriginalGetDuration == nil then
    ISReadABook.ExtractionModeOriginalGetDuration = ISReadABook.getDuration

    function ISReadABook:getDuration()
        local duration = ISReadABook.ExtractionModeOriginalGetDuration(self)
        if self.character and Infection.playerInsideHideout(self.character) then
            local reduction = math.max(0, math.min(95,
                tonumber(Config.value("HideoutReadingDurationReductionPercent")) or 25))
            duration = duration * (1 - reduction / 100)
        end
        return math.max(duration, 1)
    end
end

ExtractionMode.HideoutReading = Reading
return Reading
