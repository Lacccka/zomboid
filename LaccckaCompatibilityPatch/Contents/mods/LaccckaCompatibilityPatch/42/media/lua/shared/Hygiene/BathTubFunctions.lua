-- Lifestyle's shared timed action requires a client-only helper during the
-- shared/server phase. The real client file replaces this table later.
BathTubFunctions = BathTubFunctions or {}
BathTubFunctions.DoAction = BathTubFunctions.DoAction or function() end
BathTubFunctions.DoActionDisturbed = BathTubFunctions.DoActionDisturbed or function() end
return BathTubFunctions

