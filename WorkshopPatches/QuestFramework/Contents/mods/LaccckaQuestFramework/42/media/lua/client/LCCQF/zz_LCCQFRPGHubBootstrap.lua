require "LCCQF/Quest/LCCQFQuestClientState"
require "LCCQF/Knowledge/LCCQFKnownPeopleClientState"
require "LCCQF/Faction/LCCQFKnownFactionsClientState"
require "LCCQF/Quest/LCCQFCharacterProjectionLifecycle"
require "LCCQF/Knowledge/LCCQFCharacterKnowledgeLifecycle"
require "LCCQF/Faction/LCCQFCharacterFactionKnowledgeLifecycle"
require "LCCQF/Quest/LCCQFQuestMarkerService"
require "LCCQF/Runtime/LCCQFBanditsClientPresentation"
require "LCCQF/UI/LCCQFHub"
require "LCCQF/UI/LCCQFRPGJournalPages"
require "LCCQF/Relationship/LCCQFKnownPeopleRelationshipPresentation"
require "LCCQF/UI/LCCQFFactionPage"
require "LCCQF/Faction/LCCQFKnownPeopleFactionPresentation"
require "LCCQF/Faction/LCCQFFactionKnownMembersPresentation"
require "LCCQF/Faction/LCCQFFactionSiteDebugClient"

print("[LCCQF][CLIENT] RPG hub bootstrap loaded people=true factions=true journal=true portrait=ISUI3DModel relationships=true crossNav=true factionSiteDebug=privileged")

return LCCQFHub
