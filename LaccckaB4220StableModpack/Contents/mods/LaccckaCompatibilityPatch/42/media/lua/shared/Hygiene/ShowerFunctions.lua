-- Lifestyle's shared timed action requires a client-only helper during the
-- shared/server phase. The real client file replaces this table later.
ShowerFunctions = ShowerFunctions or {}
ShowerFunctions.DoAction = ShowerFunctions.DoAction or function() end
ShowerFunctions.DoActionDisturbed = ShowerFunctions.DoActionDisturbed or function() end
return ShowerFunctions

