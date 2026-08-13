local ItemsAboutToRemove =

{

"AssaultRifle","AssaultRifle2","HuntingRifle","VarmintRifle","JS14_Rifle","L92_Carbine","L94_Rifle","MSR7T_Rifle","TrapperCarbine",
"Pistol","Pistol2","Pistol3","Revolver","Revolver_Long","Revolver_Short",
"Shotgun","ShotgunSawnoff","DoubleBarrelShotgunSawnoff","DoubleBarrelShotgun","ShotgunShells","JS3T_Shotgun",
"9mmClip","M14Clip","556Clip","45Clip","44Clip","JS14_Clip",
"ShotgunShellsCarton","ShotgunShellsBox","ShotgunShells",
"Bullets9mmCarton","Bullets9mmBox","Bullets9mm",
"Bullets45Carton","Bullets45Box","Bullets45",
"Bullets44Carton","Bullets44Box","Bullets44",
"Bullets38Carton","Bullets38Box","Bullets38",
"Bullets357Carton","Bullets357Box","Bullets357",
"556Carton","556Box","556Bullets",
"308Carton","308Box","308Bullets",
"3030Carton","3030Box","3030Bullets",
}

local ISInventoryPane_RefreshContainer = ISInventoryPane.refreshContainer

function ISInventoryPane:refreshContainer()
    
    for i, name in pairs(ItemsAboutToRemove) do
        
       if not SandboxVars.ModernFirearmsSystemSandboxGun._Pistol3_Spawn then
            if name == "Pistol3" then
               self.inventory:RemoveAll(name)
               self.inventory:setDrawDirty(false)
            end
       end
   
       if not SandboxVars.ModernFirearmsSystemSandboxGun._VarmintRifle_Spawn then
            if name == "VarmintRifle" then
               self.inventory:RemoveAll(name)
               self.inventory:setDrawDirty(false)
            end
       end
   
       if not SandboxVars.ModernFirearmsSystemSandboxGun._Revolver_Spawn then
            if name == "Revolver" then
               self.inventory:RemoveAll(name)
               self.inventory:setDrawDirty(false)
            end
       end
   
       if not SandboxVars.ModernFirearmsSystemSandboxGun._Revolver_Long_Spawn then
            if name == "Revolver_Long" then
               self.inventory:RemoveAll(name)
               self.inventory:setDrawDirty(false)
            end
       end
   
      if not SandboxVars.ModernFirearmsSystemSandboxGun._Pistol2_Spawn then
           if name == "Pistol2" then
              self.inventory:RemoveAll(name)
              self.inventory:setDrawDirty(false)
           end
      end
  
      if not SandboxVars.ModernFirearmsSystemSandboxGun._AssaultRifle_Spawn then
           if name == "AssaultRifle" then
              self.inventory:RemoveAll(name)
              self.inventory:setDrawDirty(false)
           end
      end
  
        if not SandboxVars.ModernFirearmsSystemSandboxGun._AssaultRifle2_Spawn then
             if name == "AssaultRifle2" then
                self.inventory:RemoveAll(name)
                self.inventory:setDrawDirty(false)
             end
        end
    
        if not SandboxVars.ModernFirearmsSystemSandboxGun._HuntingRifle_Spawn then
             if name == "HuntingRifle" then
                self.inventory:RemoveAll(name)
                self.inventory:setDrawDirty(false)
             end
        end
    
        if not SandboxVars.ModernFirearmsSystemSandboxGun._Pistol_Spawn then
             if name == "Pistol" then
                self.inventory:RemoveAll(name)
                self.inventory:setDrawDirty(false)
             end
        end
    
        if not SandboxVars.ModernFirearmsSystemSandboxGun._Shotgun_Spawn then
             if name == "Shotgun" then
                self.inventory:RemoveAll(name)
                self.inventory:setDrawDirty(false)
             end
        end
    
        if not SandboxVars.ModernFirearmsSystemSandboxGun._ShotgunSawnoff_Spawn then
             if name == "ShotgunSawnoff" then
                self.inventory:RemoveAll(name)
                self.inventory:setDrawDirty(false)
             end
        end
    
        if not SandboxVars.ModernFirearmsSystemSandboxGun._DoubleBarrelShotgun_Spawn then
             if name == "DoubleBarrelShotgun" then
                self.inventory:RemoveAll(name)
                self.inventory:setDrawDirty(false)
             end
        end

    end

    ISInventoryPane_RefreshContainer(self)
end