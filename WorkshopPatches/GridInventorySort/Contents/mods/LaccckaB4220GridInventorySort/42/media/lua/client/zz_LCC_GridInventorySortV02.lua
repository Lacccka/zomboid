local okPages, pageErr = pcall(require, "LCC/GridMultiPage")
if not okPages then
    print("[LCC GridSort] multi-page bootstrap failed: " .. tostring(pageErr))
end

local okNetwork, networkErr = pcall(require, "LCC/GridSortNetwork")
if not okNetwork then
    print("[LCC GridSort] MP network bootstrap failed: " .. tostring(networkErr))
end
