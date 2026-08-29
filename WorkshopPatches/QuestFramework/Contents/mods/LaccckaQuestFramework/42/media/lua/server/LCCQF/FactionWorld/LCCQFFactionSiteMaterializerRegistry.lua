-- Provider-neutral registry. Faction world services depend on this interface, while
-- concrete physical runtimes register themselves behind the adapter boundary.
if isClient and isClient() and not (isServer and isServer()) then
    return {}
end

LCCQF = LCCQF or {}

local Registry = LCCQF.FactionSiteMaterializerRegistry or {}
local adapters = Registry.adapters or {}

function Registry.Register(adapterId, adapter)
    if type(adapterId) ~= "string" or adapterId == "" then return false, "invalid materializer id" end
    if type(adapter) ~= "table" or type(adapter.Materialize) ~= "function" then
        return false, "invalid materializer adapter"
    end
    adapters[adapterId] = adapter
    return true
end

function Registry.Get(adapterId)
    if type(adapterId) ~= "string" then return nil end
    return adapters[adapterId]
end

function Registry.IsRegistered(adapterId)
    return Registry.Get(adapterId) ~= nil
end

Registry.adapters = adapters
LCCQF.FactionSiteMaterializerRegistry = Registry
return Registry
