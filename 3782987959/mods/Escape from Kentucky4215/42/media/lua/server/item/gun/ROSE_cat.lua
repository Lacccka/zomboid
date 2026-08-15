require 'Items/ProceduralDistributions'
require "Items/ItemPicker"

if not SandboxVars.ModernFirearmsSystemSandboxGun._ROSE_cat_Spawn then return end

table.insert(ProceduralDistributions["list"]["GunStoreShelf"].items, "Base.ROSE_cat");
table.insert(ProceduralDistributions["list"]["GunStoreShelf"].items, 0.3);
