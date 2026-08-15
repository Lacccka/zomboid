Grenade_Tajectory = Grenade_Tajectory or {}
local TickTable = {}

function Grenade_Tajectory.ShootGrenade(character, handWeapon, GrenadeTypeInfo)

    local DirectSquare = Grenade_Tajectory.aimcursorsq
    local Distance = Grenade_Tajectory.aimtexdistance
    table.insert(TickTable, {
        DirectSquare = DirectSquare,
        Distance = Distance,
        GrenadeTypeInfo = GrenadeTypeInfo
    })

end

function Grenade_Tajectory.ShootGrenadeTick()
    for i, v in pairs(TickTable) do
        if v.Distance > 0 then
            v.Distance = v.Distance - 1
        else
            Grenade_Tajectory.Boom(v.DirectSquare, v.GrenadeTypeInfo)
            table.remove(TickTable, i)
        end
    end
end

Events.OnTick.Add(Grenade_Tajectory.ShootGrenadeTick)
