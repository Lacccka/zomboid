require "LCCQF/Quest/LCCQFQuestClientState"
require "LCCQF/Knowledge/LCCQFKnownPeopleClientState"
require "LCCQF/Quest/LCCQFCharacterProjectionLifecycle"
require "LCCQF/Knowledge/LCCQFCharacterKnowledgeLifecycle"
require "LCCQF/Quest/LCCQFQuestMarkerService"
require "LCCQF/Runtime/LCCQFBanditsClientPresentation"
require "LCCQF/UI/LCCQFHub"
require "LCCQF/UI/LCCQFRPGJournalPages"
require "LCCQF/Relationship/LCCQFKnownPeopleRelationshipPresentation"

print("[LCCQF][CLIENT] RPG hub bootstrap loaded people=true journal=true portrait=ISUI3DModel relationships=true")

return LCCQFHub
