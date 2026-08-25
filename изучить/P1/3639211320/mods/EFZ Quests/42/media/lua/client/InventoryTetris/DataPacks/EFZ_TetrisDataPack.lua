local function registerEFZTetrisPack()
    local mods = getActivatedMods()
    if not mods or not mods:contains("INVENTORY_TETRIS") then
        return
    end

    require "InventoryTetris/TetrisItemData"

    local itemPack = {
        ["EFZ.SupplyTicket"] = { ["width"] = 1, ["height"] = 1, ["maxStackSize"] = 100 },
        ["EFZ.ArmyCrateMQ03"] = { ["width"] = 3, ["height"] = 3, ["maxStackSize"] = 1 },
        ["EFZ.RoadControlPlan"] = { ["width"] = 1, ["height"] = 2, ["maxStackSize"] = 1 },
        ["EFZ.OperationCleanSlatePlan"] = { ["width"] = 1, ["height"] = 2, ["maxStackSize"] = 1 },
        ["EFZ.NBCEquipmentCrate"] = { ["width"] = 3, ["height"] = 3, ["maxStackSize"] = 1 },
        ["EFZ.DetaineeVisitationVoiceLog"] = { ["width"] = 1, ["height"] = 1, ["maxStackSize"] = 1 },
        ["EFZ.PatientFile"] = { ["width"] = 1, ["height"] = 1, ["maxStackSize"] = 1 },
        ["EFZ.DeltaFacilityDocument"] = { ["width"] = 1, ["height"] = 2, ["maxStackSize"] = 1 },
        ["EFZ.InfectionSuppressorBox"] = { ["width"] = 2, ["height"] = 2, ["maxStackSize"] = 3 },
        ["EFZ.LastLetterToDoctor"] = { ["width"] = 1, ["height"] = 1, ["maxStackSize"] = 1 },
        ["EFZ.RareAlcohol"] = { ["width"] = 1, ["height"] = 2, ["maxStackSize"] = 1 },
        ["EFZ.PrototypeWeaponBlueprint"] = { ["width"] = 2, ["height"] = 2, ["maxStackSize"] = 1 },
        ["EFZ.LivingSpaceFloorPlanUpper"] = { ["width"] = 1, ["height"] = 1, ["maxStackSize"] = 1 },
        ["EFZ.LivingSpaceFloorPlanLower"] = { ["width"] = 1, ["height"] = 1, ["maxStackSize"] = 1 },
    }

    local alwaysStack = {
        "EFZ.SupplyTicket",
    }

    TetrisItemData.registerItemDefinitions(itemPack)
    TetrisItemData.registerAlwaysStackOnSpawnItems(alwaysStack)
end

Events.OnGameBoot.Add(registerEFZTetrisPack)
