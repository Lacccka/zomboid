require "TimedActions/ISBaseTimedAction"

local old_ISReadABook_getDuration = ISReadABook.getDuration

function ISReadABook:getDuration()
    local time = old_ISReadABook_getDuration(self)
    
    local zreitem = self.item
    if zreitem then
        local fullType = zreitem:getFullType()
        local slowBooks = {
            ["zReLabItems.bkNotebookVaccineStart"] = 6,
            ["zReLabItems.bkNotebookVaccine2"] = 6,
            ["zReLabItems.bkNotebookVaccine3"] = 6,
            ["zReLabItems.bkNotebookVaccine4"] = 6,
        }
        
        local multiplier = slowBooks[fullType] or 1.0
        
        -- Применяем множитель
        time = time * multiplier
    end
    
    return time
end