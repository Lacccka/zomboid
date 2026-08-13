--***********************************************************
--**                    Ranger Operator                    **
--***********************************************************

-- Anxiety simulator 9000

local group = AttachedLocations.getGroup("Human")
-- The Group coordinate for the thing we want to Merge shit to.  Aka. The Zomboid DuctTape.
group:getOrCreateLocation("TacOpChestHolster"):setAttachmentName("TacOp_Chest_Holster") 
group:getOrCreateLocation("TacOpChestHolsterM"):setAttachmentName("TacOp_Chest_HolsterM")
group:getOrCreateLocation("OperatorSlingMain"):setAttachmentName("Operator_Sling_Main")
group:getOrCreateLocation("OperatorVestMain"):setAttachmentName("Operator_Vest_Main")
group:getOrCreateLocation("OperatorHeadGearMount"):setAttachmentName("Operator_HeadGear_Mount")


group:getOrCreateLocation("OperatorHeadGearMountAlt"):setAttachmentName("Operator_HeadGear_Mount_Alt")



group:getOrCreateLocation("OperatorGLXHeadGearMount"):setAttachmentName("Operator_GLXHeadGear_Mount")
-- we take this ^ PLACE NAME.                         AND GIVE IT COORDINATES / ID.
--
--
group:getOrCreateLocation("OperatorBeltLeft"):setAttachmentName("Operator_belt_left")
group:getOrCreateLocation("CRYETan2"):setAttachmentName("FR_Firearm_Vest")
--
--
--
--
group:getOrCreateLocation("Tac Op Flashlight"):setAttachmentName("Tac_Op_Flashlight")
group:getOrCreateLocation("Flashlight Mount"):setAttachmentName("Flashlight_Mount")
-- AttachmentType = TacOpFlashlight, from the Scriptblock, --> Calls for -->     Tac_Op_Flashlight  ID. -->
-- Code Here Creates it when Game Loads. --> This in turn Links to --> Attachment Tac_Op_Flashlight -->   (in FR_Operator_Attachments.txt)
-- This AGAIN gets linked Back to, TacOpFlashlight       whacky attachment recall.

-- here we have two new ID's and location names.  Both are bad. and while they can work, they WILL inevitably confuse you.
-- group:getOrCreateLocation("Belt Left Screwdriver"):setAttachmentName("belt_left_screwdriver")   PZ uses this format.
-- group:getOrCreateLocation("Tac Op Flashlight"):setAttachmentName("Tac_Op_Flashlight")

--[[

group:getOrCreateLocation("Ash Holster Side"):setAttachmentName("AshHolster_Side")
group:getOrCreateLocation("Operator Holster Side"):setAttachmentName("OperatorHolster_Side")
group:getOrCreateLocation("Operator Holster Side"):setAttachmentName("OperatorHolsterSide")

--]]

--group:getOrCreateLocation("HolsterMountDeltaR"):setAttachmentName("Holster_Mount_Delta_R")
--group:getOrCreateLocation("HolsterMountDeltaL"):setAttachmentName("Holster_Mount_Delta_L")


group:getOrCreateLocation("HolsterChimeraDeltaR"):setAttachmentName("Holster_Chimera_Delta_R")
group:getOrCreateLocation("HolsterChimeraDeltaL"):setAttachmentName("Holster_Chimera_Delta_L")



group:getOrCreateLocation("StanagOperatorMolleStorage"):setAttachmentName("Stanag_Operator_Molle_StorageLeft")
group:getOrCreateLocation("StanagOperatorMolleStorageRight"):setAttachmentName("Stanag_Operator_Molle_StorageRight")
group:getOrCreateLocation("StanagOperatorMolleStorageMiddle"):setAttachmentName("Stanag_Operator_Molle_StorageMiddle")

group:getOrCreateLocation("ARsetOperatorMolleStorage"):setAttachmentName("ARset_Operator_Molle_Storage")
group:getOrCreateLocation("ARsetOperatorMolleStorageRight"):setAttachmentName("ARset_Operator_Molle_Storage_Right")
group:getOrCreateLocation("ARsetOperatorMolleStorageMiddle"):setAttachmentName("ARset_Operator_Molle_Storage_Middle")
-- MolleStorage









group:getOrCreateLocation("ChestMountDT"):setAttachmentName("DevTac_Chest_Holster")
group:getOrCreateLocation("KydexSlingMain"):setAttachmentName("Kydex_Sling_Main")
group:getOrCreateLocation("KydexVestMain"):setAttachmentName("Kydex_Vest_Main")
group:getOrCreateLocation("KydexBeltLeft"):setAttachmentName("Kydex_belt_left")
group:getOrCreateLocation("CRYETan2"):setAttachmentName("FR_Firearm_Vest")
group:getOrCreateLocation("Ash Holster Side"):setAttachmentName("AshHolster_Side")
group:getOrCreateLocation("Kydex Holster Side"):setAttachmentName("KydexHolster_Side")
group:getOrCreateLocation("Kydex Holster Side"):setAttachmentName("KydexHolsterSide")
group:getOrCreateLocation("Visible Holster Right"):setAttachmentName("visholster_right")
group:getOrCreateLocation("Visible Holster Left"):setAttachmentName("visholster_left")
group:getOrCreateLocation("StanagMolleStorage"):setAttachmentName("Stanag_Molle_StorageLeft")
group:getOrCreateLocation("StanagMolleStorageRight"):setAttachmentName("Stanag_Molle_StorageRight")
group:getOrCreateLocation("StanagMolleStorageMiddle"):setAttachmentName("Stanag_Molle_StorageMiddle")