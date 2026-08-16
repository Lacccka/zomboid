require "Hotbar/ISHotbarAttachDefinition"
if not ISHotbarAttachDefinition then
    return
end

local GrenadeBandolierSlot1 = {
    type = "GrenadeBandolierSlot1",
    name = "Grenade Slot 1",
    animset = "holster left",
    attachments = {
        GrenadeSlot = "Grenade Bandolier Slot 1",
    },
}
table.insert(ISHotbarAttachDefinition, GrenadeBandolierSlot1)

local GrenadeBandolierSlot2 = {
    type = "GrenadeBandolierSlot2",
    name = "Grenade Slot 2",
    animset = "holster left",
    attachments = {
        GrenadeSlot = "Grenade Bandolier Slot 2",
    },
}
table.insert(ISHotbarAttachDefinition, GrenadeBandolierSlot2)

local GrenadeBandolierSlot3 = {
    type = "GrenadeBandolierSlot3",
    name = "Grenade Slot 3",
    animset = "holster right",
    attachments = {
        GrenadeSlot = "Grenade Bandolier Slot 3",
    },
}
table.insert(ISHotbarAttachDefinition, GrenadeBandolierSlot3)

local GrenadeBandolierSlot4 = {
    type = "GrenadeBandolierSlot4",
    name = "Grenade Slot 4",
    animset = "holster right",
    attachments = {
        GrenadeSlot = "Grenade Bandolier Slot 4",
    },
}
table.insert(ISHotbarAttachDefinition, GrenadeBandolierSlot4)

for _, def in ipairs(ISHotbarAttachDefinition) do
    if def.type == "WebbingLeft" then
        def.attachments["GrenadeSlot"] = "Webbing Left Walkie"
    elseif def.type == "WebbingRight" then
        def.attachments["GrenadeSlot"] = "Webbing Right Walkie"
    end
end