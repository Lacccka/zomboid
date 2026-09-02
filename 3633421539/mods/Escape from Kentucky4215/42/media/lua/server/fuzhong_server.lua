VacMod27 = VacMod27 or {}

    local function func_8c6911()
        print("load lua/server/ItemCode.lua in ModernFirearmsSystem")
        function ItemCodeOnCreate.fuzhong(item)
            item:setHungChange(item:getHungChange() * -10)
        end
    end
    pcall(func_8c6911)