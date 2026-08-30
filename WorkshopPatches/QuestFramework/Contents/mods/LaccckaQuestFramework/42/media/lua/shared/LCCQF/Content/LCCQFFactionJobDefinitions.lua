require "LCCQF/Core/LCCQFFactionJobRegistry"

local Registry = LCCQF.FactionJobRegistry

local jobs = {
    { jobId = "guard", dutyMode = "guard", targetKind = "home" },
    { jobId = "command", dutyMode = "command", targetKind = "home" },
    { jobId = "rest", dutyMode = "rest", targetKind = "bed" },
    { jobId = "work", dutyMode = "work", targetKind = "storage" },
}

for _, definition in ipairs(jobs) do
    local ok, errorText = Registry.Register(definition)
    if not ok and errorText ~= "duplicate jobId" then
        print("[LCCQF][FACTION:JOBS] registration failed jobId="
            .. tostring(definition.jobId) .. " error=" .. tostring(errorText))
    end
end

return Registry
