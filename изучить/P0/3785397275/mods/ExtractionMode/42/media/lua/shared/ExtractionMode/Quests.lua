require "ExtractionMode/Upgrades"
require "ExtractionMode/Localization"

ExtractionMode = ExtractionMode or {}

local Upgrades = ExtractionMode.Upgrades
local Localization = ExtractionMode.Localization
local Quests = {}

local CANNED_FOOD_REWARDS = {
    "Base.TinnedBeans", "Base.CannedBolognese", "Base.CannedCarrots2",
    "Base.CannedChili", "Base.CannedCornedBeef", "Base.CannedCorn",
    "Base.CannedFruitCocktail", "Base.CannedFruitBeverage", "Base.CannedMilk",
    "Base.CannedMushroomSoup", "Base.CannedPeaches", "Base.CannedPeas",
    "Base.CannedPineapple", "Base.CannedPotato2", "Base.CannedSardines",
    "Base.TinnedSoup", "Base.CannedTomato2", "Base.TunaTin", "Base.Dogfood",
}

local MILITARY_MEDICAL_REWARDS = {
    "Base.AlcoholWipes", "Base.Antibiotics", "Base.Bandage", "Base.Gloves_Surgical",
    "Base.Pills", "Base.Scalpel", "Base.ScissorsBluntMedical", "Base.SutureNeedle",
    "Base.SutureNeedleHolder", "Base.Tweezers",
}

local contacts = {
    pilot_friend = { id = "pilot_friend", name = "Your Pilot Friend" },
    dr_layne = {
        id = "dr_layne",
        name = "Dr. Judith Layne",
        description = "The sole doctor in an underground bunker filled with survivors, putting on a brave face while tending to the community's wounds and injuries.",
        focus = "Medical supplies",
    },
    franklin_porch = {
        id = "franklin_porch",
        name = "Franklin Porch",
        description = "Knox Country's foremost zombie exterminator. His brother Dean hosted Exposure Survival, but Franklin believes the best defense is a good offense.",
        focus = "Zombie hunting and survival gear",
    },
    sgt_major_graves = {
        id = "sgt_major_graves",
        name = "Sgt. Major Graves",
        description = "A remnant of the Knox Evacuation forces who refused to abandon her unit. She leads a small military survivor group and maintains what order she can.",
        focus = "Food, firearms, and logistics",
    },
    silas_mercer = {
        id = "silas_mercer",
        name = "Silas \"The Rat\" Mercer",
        description = "A smuggler who thrived in the Exclusion Zone's chaos and intends to live like a king when society rebuilds.",
        focus = "Luxury goods, valuables, and stashed items",
    },
    elias_vance = {
        id = "elias_vance",
        name = "Elias Vance",
        description = "A paranoid former scientist from the classified military facility, hiding in a fortified safehouse with knowledge needed to begin sequencing a vaccine.",
        focus = "Specialized research items",
    },
}

-- A quest with no prerequisites is available immediately. Future quests can
-- name one or more prerequisite quest IDs; they remain completely hidden from
-- the quest screen until every prerequisite has been completed.
local definitions = {
    {
        number = 1,
        id = "contact_the_outside",
        contactId = "pilot_friend",
        name = "Contact the Outside",
        description = "Build a radio repeater and reach someone beyond the Exclusion Zone.",
        flavorText = "You and your ever silent pilot buddy found this unused military bunker and agreed it could become something more than a place to hide. The walls are solid, the old infrastructure still has promise, and the flights give you a way to move-but isolation will eventually kill you. Build a radio repeater so you can push a signal beyond the Exclusion Zone and find out who else is still listening.",
        completedFlavorText = "The repeater's signal is weak, but it reaches far enough. Messages begin slipping through the static—fragmented voices, cautious introductions, proof that the world outside your bunker is not entirely silent. Two individuals catch your attention: Dr. Layne, a physician struggling to keep survivors supplied, and Franklin Porch, a relentless zombie hunter with work to offer. Contact has been made. Now you have to decide what those connections are worth.",
        requirements = {
            { label = "Radio Transmitter", amount = 1, types = { "Base.RadioTransmitter" } },
            { label = "Electrical Wire", amount = 3, types = { "Base.ElectricWire" } },
        },
        skillRequirements = {},
    },
    {
        number = 2,
        id = "emergency_supplies",
        contactId = "dr_layne",
        name = "Emergency Supplies",
        description = "Help Dr. Layne replenish her dwindling medical supplies.",
        flavorText = "I didn't know anyone else was still alive inside the Exclusion Zone. That's the best news I've heard in a long time. I'm treating more injuries than I can keep supplied for, though. If you can spare five bandages and two bottles of painkillers, it would really help us out. -Dr. Layne",
        completedFlavorText = "These supplies arrived when we needed them most. Thank you. A few people are going to make it through the night because you answered. I wish I could say that settles things, but it doesn't—our situation is still fragile, and I am going to need more help soon. I'll contact you when I know what comes next.",
        prerequisites = { "contact_the_outside" },
        requirements = {
            { label = "Bandages", amount = 5, types = { "Base.Bandage" } },
            { label = "Painkillers", amount = 2, types = { "Base.Pills" } },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "dr_layne", amount = 10 },
        },
    },
    {
        number = 3,
        id = "thinning_the_herd",
        contactId = "franklin_porch",
        name = "Thinning the Herd",
        description = "Kill 20 zombies for Franklin Porch.",
        flavorText = "My brother Dean used to teach people how to stay alive on Life and Living's Exposure Survival. Good advice, sure, but surviving isn't enough anymore. There are a ton of those things out there, and every one left standing is another problem waiting to find somebody. Put down twenty of them. Every kill counts. -Franklin Porch",
        completedFlavorText = "Twenty fewer dead are wandering around out there tonight. That means more people can finally rest without wondering if those same twenty are about to come through the door. You did good—but don't mistake a first step for the finish line. We're only getting started.",
        prerequisites = { "contact_the_outside" },
        requirements = {},
        skillRequirements = {},
        objectives = {
            { id = "zombie_kills", type = "zombie_kills", label = "Zombies killed", amount = 20 },
        },
        rewards = {
            { type = "trust", contactId = "franklin_porch", amount = 10 },
        },
    },
    {
        number = 4,
        id = "first_do_no_harm",
        contactId = "dr_layne",
        name = "First Do No Harm",
        description = "Hand in 2 bottles of disinfectant and 3 alcohol wipes.",
        flavorText = "I am doing my best to patch up the people who make it to the bunker, but my basic supplies are completely gone. We are losing people to minor fevers and secondary infections just because I cannot sanitize their wounds. I need cleaning agents. Disinfectant, alcohol wipes, just get me something so I can stop these infections before they spread.",
        completedFlavorText = "Thank you. This buys us time. I can finally clean up the triage cots. Keep your radio on, I will let you know when I need you again.",
        prerequisites = { "emergency_supplies" },
        requirements = {
            { label = "Disinfectant", amount = 2, types = { "Base.Disinfectant" } },
            { label = "Alcohol Wipes", amount = 3, types = { "Base.AlcoholWipes" } },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "dr_layne", amount = 10 },
            { type = "item", fullType = "Base.BookFirstAid1", label = "First Aid for Beginners", amount = 1 },
        },
    },
    {
        number = 5,
        id = "calorie_deficit",
        contactId = "sgt_major_graves",
        name = "Calorie Deficit",
        description = "Hand in 5 cans of food, 1 can opener, and 2 water bottles.",
        flavorText = "This is Sgt. Major Graves. I do not know who you are, but you are operating on my grid. I am establishing a strict daily ration scale for my squad. Before I trust you with tactical operations, I need to know you can reliably secure basic sustenance. Bring me canned goods, an opener, and fresh water. -Sgt. Major Graves",
        completedFlavorText = "Good. You follow instructions and you know how to scavenge without drawing a crowd. These calories will keep my perimeter guards alert for another 48 hours. Consider yourself a provisional asset.",
        prerequisites = { "first_do_no_harm", "clearing_the_neighborhood" },
        requirements = {
            { label = "Cans of Food", amount = 5, typePrefixes = { "Base.Canned" }, excludedTypeFragments = { "Open", "_Box" }, types = { "Base.TunaTin", "Base.Dogfood" } },
            { label = "Can Opener", amount = 1, types = { "Base.TinOpener" } },
            { label = "Water Bottles", amount = 2, types = { "Base.WaterBottle" }, requiresWater = true },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "sgt_major_graves", amount = 10 },
            { type = "item", fullType = "Base.Bullets9mmBox", label = "Box of 9mm Rounds", amount = 2 },
        },
    },
    {
        number = 6,
        id = "clearing_the_neighborhood",
        contactId = "franklin_porch",
        name = "Clearing the Neighborhood",
        description = "Kill 30 zombies in Rosewood.",
        flavorText = "I have been watching the hordes move through the tree lines. They are pooling up around Rosewood. If we do not thin them out now, they are going to overrun our southern routes. Get into the town limits and put thirty of them in the dirt. I do not care how you take them down. Just make sure they stay down.",
        completedFlavorText = "That is what I like to see. Thirty less of those freaks means thirty less problems for our scavengers to trip over. Take this blade, keep it sharp, and stay ready.",
        prerequisites = { "thinning_the_herd" },
        requirements = {},
        skillRequirements = {},
        objectives = {
            { id = "rosewood_zombie_kills", type = "zombie_kills", townKey = "rosewood", label = "Zombies killed in Rosewood", amount = 30 },
        },
        rewards = {
            { type = "trust", contactId = "franklin_porch", amount = 10 },
            { type = "item", fullType = "Base.Machete", label = "Machete", amount = 1 },
        },
    },
    {
        number = 7,
        id = "opportunistic_value",
        contactId = "silas_mercer",
        name = "Opportunistic Value",
        description = "Hand in 5 digital watches and 1 piece of jewelry.",
        flavorText = "You are fighting over canned peas while the real wealth of Knox Country is sitting on the wrists of the dead. Society will bounce back, and the people holding the hard assets will be the ones making the rules. Bring me some digital watches and a piece of jewelry. Prove to me you understand how things work now. -Silas Mercer",
        completedFlavorText = "Beautiful. You see the big picture. When the military eventually rolls back in and sets up the green zones, we are going to be living like kings. I will be in touch when I find a buyer for the next batch.",
        prerequisites = { "first_do_no_harm", "clearing_the_neighborhood" },
        requirements = {
            { label = "Digital Watches", amount = 5, types = { "Base.WristWatch_Right_DigitalBlack", "Base.WristWatch_Left_DigitalBlack", "Base.WristWatch_Right_DigitalRed", "Base.WristWatch_Left_DigitalRed", "Base.WristWatch_Right_DigitalDress", "Base.WristWatch_Left_DigitalDress" } },
            { label = "Jewelry", amount = 1, typePrefixes = { "Base.Necklace", "Base.Ring_", "Base.Earring_", "Base.Bracelet_", "Base.NoseRing_", "Base.BellyButton_" } },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "silas_mercer", amount = 10 },
            { type = "item", fullType = "Base.Bullets38Box", label = "Box of .38 Special Rounds", amount = 2 },
            { type = "item", fullType = "Base.Revolver_Short", label = "SN38 Revolver", amount = 1 },
        },
    },
    {
        number = 8,
        id = "perimeter_defense",
        contactId = "franklin_porch",
        name = "Perimeter Defense",
        description = "Hand in 2 boxes of nails, 1 hammer, and 1 hand axe.",
        flavorText = "My hunting parties need safehouses to fall back to when the streets get too crowded. We have the muscle to build the barricades, but we are short on hardware. Bring us nails, a hammer, and an axe.",
        completedFlavorText = "The hardware is solid. We are already boarding up the safehouse windows. Having a place to sleep with one eye open instead of two makes a world of difference out here.",
        prerequisites = { "clearing_the_neighborhood" },
        requirements = {
            { label = "Boxes of Nails", amount = 2, types = { "Base.NailsBox" } },
            { label = "Hammer", amount = 1, types = { "Base.Hammer" } },
            { label = "Hand Axe", amount = 1, types = { "Base.HandAxe" } },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "franklin_porch", amount = 10 },
            { type = "item", fullType = "Base.DoubleBarrelShotgun", label = "Double-Barreled Shotgun", amount = 1 },
            { type = "item", fullType = "Base.ShotgunShellsBox", label = "Box of Shotgun Shells", amount = 2 },
        },
    },
    {
        number = 9,
        id = "arming_the_remnants",
        contactId = "sgt_major_graves",
        name = "Arming the Remnants",
        description = "Hand in 1 functioning handgun.",
        flavorText = "My unit's sidearms are in complete disrepair. Finding pristine military hardware is impossible right now, so I am standardizing with whatever civilian firearms we can scavenge. Find me a functioning handgun. I need my perimeter guards armed.",
        completedFlavorText = "Weapon is clear, action cycles fine. It is not standard issue, but it will put a hole in a target just the same. You are proving to be a reliable logistical runner.",
        prerequisites = { "calorie_deficit" },
        requirements = {
            { label = "Handgun", amount = 1, types = { "Base.Pistol", "Base.Pistol2", "Base.Pistol3", "Base.Revolver", "Base.Revolver_Long", "Base.Revolver_Short" }, requiresUsable = true },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "sgt_major_graves", amount = 10 },
            { type = "item", fullType = "Base.FirstAidKit_Military", label = "First Aid Kit - Military", amount = 1 },
        },
    },
    {
        number = 10,
        id = "advanced_triage",
        contactId = "dr_layne",
        name = "Advanced Triage",
        description = "Hand in 3 packs of sleeping pills.",
        flavorText = "The damp down here is taking a terrible toll. The survivors are terrified and haven't slept in days. Exhaustion is going to kill them faster than the dead out there. Raid the local bathroom cabinets and bring me some over-the-counter sleeping pills. I need to give these people some peace.",
        completedFlavorText = "Thank you. They are finally resting. One of the survivors who made it in yesterday dropped this revolver and said they would not need it anymore. Take it. It is the least I can do.",
        prerequisites = { "first_do_no_harm" },
        requirements = {
            { label = "Sleeping Pills", amount = 3, types = { "Base.PillsSleepingTablets" } },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "dr_layne", amount = 10 },
            { type = "item", fullType = "Base.Revolver", label = "Patrol Revolver", amount = 1 },
            { type = "item", fullType = "Base.Bullets357Box", label = "Box of .357 Magnum Rounds", amount = 1 },
        },
    },
    {
        number = 11,
        id = "wealth_of_kentucky",
        contactId = "silas_mercer",
        name = "The Wealth of Kentucky",
        description = "Hand in 1 bottle of wine and 3 packs of cigarettes.",
        flavorText = "I have a buyer who is losing his mind from the stress. He is willing to trade high-tier gear for some simple vices to take the edge off. Hit the local bars or gas stations, grab a bottle of wine and some smokes. Bring them to me and we can do some real business.",
        completedFlavorText = "Excellent vintage. Well, good enough for the end of the world anyway. My buyer is happy, which means I am happy. Enjoy the hardware.",
        prerequisites = { "opportunistic_value" },
        requirements = {
            { label = "Bottle of Wine", amount = 1, types = { "Base.Wine", "Base.Wine2", "Base.WineAged", "Base.WineScrewtop" } },
            { label = "Cigarette Packs", amount = 3, types = { "Base.CigarettePack" } },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "silas_mercer", amount = 10 },
            { type = "item", fullType = "Base.GunLight", label = "Gun Light", amount = 1 },
            { type = "item", fullType = "Base.TritiumSights", label = "Tritium Pistol Sights", amount = 1 },
        },
    },
    {
        number = 12,
        id = "reclaiming_the_ridge",
        contactId = "sgt_major_graves",
        name = "Reclaiming the Ridge",
        description = "Kill 25 zombies in March Ridge.",
        flavorText = "March Ridge was intended to be a secure military housing sector, but it fell during the initial panic. I want that foothold cleared. I need to send recovery teams in to retrieve military gear, but I cannot risk them until you pacify the sector. Eliminate twenty-five hostiles in the area.",
        completedFlavorText = "Perimeter reports indicate a significant drop in hostile movement around the Ridge. My teams are moving in to secure the assets now. Excellent work, soldier.",
        prerequisites = { "perimeter_defense", "arming_the_remnants" },
        requirements = {},
        skillRequirements = {},
        objectives = {
            { id = "march_ridge_zombie_kills", type = "zombie_kills", townKey = "march_ridge", label = "Zombies killed in March Ridge", amount = 25 },
        },
        rewards = {
            { type = "trust", contactId = "sgt_major_graves", amount = 10 },
            { type = "item", fullType = "Base.Hat_Army", label = "Military Helmet", amount = 1 },
        },
    },
    {
        number = 13,
        id = "tools_of_the_trade",
        contactId = "dr_layne",
        name = "Tools of the Trade",
        description = "Hand in 1 scalpel, 2 suture needles, and 2 tweezers.",
        flavorText = "We need to expand. I want to set up a secondary triage center closer to the city limits so we do not have to drag the wounded all the way back to the bunker. I need specialized trauma supplies. Ripped sheets are not cutting it anymore. Hit the Rosewood Clinic or Cortman Medical and bring me a scalpel, needles, and tweezers.",
        completedFlavorText = "These tools are pristine. You have no idea how much of a difference this makes. We can actually close deep wounds properly now instead of just packing them with gauze.",
        prerequisites = { "advanced_triage" },
        requirements = {
            { label = "Scalpel", amount = 1, types = { "Base.Scalpel" } },
            { label = "Suture Needles", amount = 2, types = { "Base.SutureNeedle" } },
            { label = "Tweezers", amount = 2, types = { "Base.Tweezers" } },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "dr_layne", amount = 10 },
            { type = "item", fullType = "Base.PillsBeta", label = "Beta Blockers", amount = 2 },
        },
    },
    {
        number = 14,
        id = "cleanup",
        contactId = "franklin_porch",
        name = "Cleanup",
        description = "Kill 50 zombies anywhere in the Exclusion Zone.",
        flavorText = "The hordes are gathering again. They do not sleep, they just walk and merge into bigger masses. I want you to make a real dent in their numbers before they reach the safe zones. Drop fifty of them. Anywhere, anytime. Just get it done.",
        completedFlavorText = "Fifty bodies. That is a good day's work. It won't stop the tide entirely, but it sure as hell slows it down. Reload and rest up, we aren't done yet.",
        prerequisites = { "perimeter_defense" },
        requirements = {},
        skillRequirements = {},
        objectives = {
            { id = "zombie_kills", type = "zombie_kills", label = "Zombies killed", amount = 50 },
        },
        rewards = {
            { type = "trust", contactId = "franklin_porch", amount = 10 },
            { type = "item", fullType = "Base.Shotgun", label = "JS-2000 Shotgun", amount = 1 },
            { type = "item", fullType = "Base.ShotgunShellsBox", label = "Box of 12-Gauge Shells", amount = 2 },
        },
    },
    {
        number = 15,
        id = "liquidating_assets",
        contactId = "silas_mercer",
        name = "Liquidating Assets",
        description = "Hand in 50 paper money and 5 credit cards.",
        flavorText = "Graves and Franklin think cash is only good for starting fires. They lack vision. Paper money and plastic are status symbols among my buyers. It reminds them of the world they used to control. Check the wallets of the dead and the cash registers in the commercial districts. Bring me the cash and cards.",
        completedFlavorText = "Look at that. A stack of hundreds and a handful of platinum cards. Completely worthless out there, but in here? It buys you favors. Good doing business with you.",
        prerequisites = { "wealth_of_kentucky" },
        requirements = {
            { label = "Paper Money", amount = 50, types = { "Base.Money" } },
            { label = "Credit Cards", amount = 5, types = { "Base.CreditCard" } },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "silas_mercer", amount = 10 },
            { type = "item", fullType = "Base.RedDot", label = "Red Dot Sight", amount = 1 },
            { type = "item", fullType = "Base.x2Scope", label = "2x Scope", amount = 1 },
        },
    },
    {
        number = 16,
        id = "logistical_support",
        contactId = "sgt_major_graves",
        name = "Logistical Support",
        description = "Hand in 1 duffel bag, 2 flashlights, and 2 batteries.",
        flavorText = "I am expanding our night patrols, but my scouts are operating blind and cannot carry enough gear. A civilian school bag will not cut it for military operations. I need reliable load-bearing equipment and proper illumination. Bring me a duffel bag, flashlights, and batteries.",
        completedFlavorText = "The gear is satisfactory. My night patrols are fully equipped and ready to deploy. Logistics win wars, and right now, you are the best quartermaster I've got.",
        prerequisites = { "reclaiming_the_ridge" },
        requirements = {
            { label = "Duffel Bag", amount = 1, types = { "Base.Bag_DuffelBag" } },
            { label = "Flashlights", amount = 2, types = { "Base.HandTorch", "Base.Torch", "Base.FlashLight_AngleHead", "Base.FlashLight_AngleHead_Army" } },
            { label = "Batteries", amount = 2, types = { "Base.Battery" } },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "sgt_major_graves", amount = 10 },
            { type = "item", fullType = "Base.Vest_BulletArmy", label = "Bulletproof Vest - Military", amount = 1 },
        },
    },
    {
        number = 17,
        id = "muldraugh_strip",
        contactId = "franklin_porch",
        name = "The Muldraugh Strip",
        description = "Kill 30 zombies in Muldraugh.",
        flavorText = "The Muldraugh highway is completely choked with the dead. It is a literal wall of bodies. I am planning a heavy supply run right down the main strip, but I need a path cleared before my convoy moves in. Get to Muldraugh and thin the herd by thirty. Pull them off the asphalt.",
        completedFlavorText = "The convoy pushed through with minimal resistance. You cleared the choke point exactly as requested. We secured the supplies thanks to you.",
        prerequisites = { "cleanup" },
        requirements = {},
        skillRequirements = {},
        objectives = {
            { id = "muldraugh_zombie_kills", type = "zombie_kills", townKey = "muldraugh", label = "Zombies killed in Muldraugh", amount = 30 },
        },
        rewards = {
            { type = "trust", contactId = "franklin_porch", amount = 10 },
            { type = "item", fullType = "Base.308Box", label = "Box of 7.62x51mm Rounds", amount = 2 },
            { type = "item", fullType = "Base.HuntingRifle", label = "MSR788 Rifle", amount = 1 },
        },
    },
    {
        number = 18,
        id = "mess_of_life",
        contactId = "dr_layne",
        name = "The Mess of Life",
        description = "Hand in 3 bath towels, 2 bars of soap, and 2 buckets.",
        flavorText = "Living in an underground bunker with a dozen injured survivors is a nightmare. We do not have proper drainage or running water. The sweat and the damp make it a breeding ground for bacteria. I need clean water, buckets, soap, and thick towels to scrub this ward down before typhoid takes us all. Please help me sanitize the area.",
        completedFlavorText = "The floors are finally clean. It is hard to stay optimistic when you are surrounded by filth, so this does more for morale than you might think. Thank you for doing the dirty work.",
        prerequisites = { "tools_of_the_trade" },
        requirements = {
            { label = "Bath Towels", amount = 3, types = { "Base.BathTowel" } },
            { label = "Bars of Soap", amount = 2, types = { "Base.Soap2" } },
            { label = "Buckets", amount = 2, types = { "Base.BucketEmpty", "Base.Bucket", "Base.BucketWood", "Base.BucketCarved", "Base.BucketForged" } },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "dr_layne", amount = 10 },
            { type = "item", fullType = "Base.Antibiotics", label = "Antibiotics", amount = 3 },
        },
    },
    {
        number = 19,
        id = "heavy_rations",
        contactId = "sgt_major_graves",
        name = "Heavy Rations",
        description = "Hand in 5 cans of food and 2 jars of peanut butter.",
        flavorText = "The physical demands of constant combat are catching up to my unit. The baseline rations are not enough anymore. My soldiers are burning too much energy clearing sectors. I need high-density foods to keep them effective in the field. Bring me canned goods and peanut butter.",
        completedFlavorText = "Excellent. I have adjusted the ration distributions. The men are fed and morale is stabilizing. Maintaining this calorie intake is the only way we hold the line.",
        prerequisites = { "logistical_support" },
        requirements = {
            { label = "Cans of Food", amount = 5, typePrefixes = { "Base.Canned" }, excludedTypeFragments = { "Open", "_Box" }, types = { "Base.TunaTin", "Base.Dogfood" } },
            { label = "Peanut Butter", amount = 2, types = { "Base.PeanutButter" } },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "sgt_major_graves", amount = 10 },
            { type = "item", fullType = "Base.556Box", label = "Box of 5.56x45mm Rounds", amount = 5 },
            { type = "item", fullType = "Base.JS14_Rifle", label = "JS-14 Rifle", amount = 1 },
            { type = "item", fullType = "Base.JS14_Clip", label = "JS-14 Magazine", amount = 2 },
        },
    },
    {
        number = 20,
        id = "ghost_in_the_static",
        contactId = "elias_vance",
        name = "The Ghost in the Static",
        description = "Hand in 1 gas mask or respirator, 2 empty notebooks, and 2 pens.",
        flavorText = "You. The one doing the runner jobs for Graves. Stop transmitting your coordinates in the clear. Call me Vance. I have theoretical data on the Knox infection, but my safehouse is compromised. Before I even consider speaking to you about my research, I need respiratory protection to move, and notebooks to physically back up my hard drives. Do not ask questions. Just bring the gear.",
        completedFlavorText = "Are you sure these are safe? Nevermind, you would not know. I will sterilize them myself. Stand by for your next set of instructions. The real work is about to begin.",
        prerequisites = { "liquidating_assets", "muldraugh_strip", "mess_of_life", "heavy_rations" },
        requirements = {
            { label = "Gas Mask or Respirator", amount = 1, types = { "Base.Hat_GasMask", "Base.Hat_BuildersRespirator" } },
            { label = "Empty Notebooks", amount = 2, types = { "Base.Notebook" } },
            { label = "Pens", amount = 2, types = { "Base.Pen" } },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "elias_vance", amount = 10 },
        },
    },
    {
        number = 21,
        id = "paper_trail",
        contactId = "elias_vance",
        name = "Paper Trail",
        description = "Recover the Special Documents from Rosewood Police Department.",
        flavorText = "One of my colleagues tried to get out of Rosewood before the quarantine locked everything down. The police picked him up after he started talking about restricted research and contaminated samples. If they processed him normally, whatever he was carrying should still be in the evidence room at Rosewood PD. Find those documents and bring them to me. I need to know what he managed to copy.",
        completedFlavorText = "These are his. Personnel lists, transfer dates, sample numbers... he copied more than I expected. The important part is the transfers. My team was told the work had stopped, but these records say otherwise. Someone kept moving research material after the program was shut down. I need to find out where it went.",
        prerequisites = { "ghost_in_the_static" },
        requirements = {
            { label = "Special Documents", amount = 1, types = { "ExtractionMode.SpecialDocuments" },
                raidSpawnPoint = { x = 8061, y = 11723, z = 0 }, locationTownKey = "rosewood" },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "elias_vance", amount = 10 },
        },
    },
    {
        number = 22,
        id = "steady_hands",
        contactId = "dr_layne",
        name = "Steady Hands",
        description = "Reach First Aid 2 and hand in suturing and disinfecting supplies.",
        flavorText = "The supplies you have brought me have helped, but I am treating injuries that take more than clean bandages to fix. Deep cuts, broken glass, gunshots, all of it. I cannot be the only person around here who knows how to close a wound. Get some practice treating injuries and bring me more suturing supplies and disinfectant. You need to be able to take care of yourself when nobody else can.",
        completedFlavorText = "Good. You know enough now to clean a wound, stop the bleeding, and close it without making things worse. These supplies will help with the more serious injuries coming through here. Keep practicing. The next time you get hurt out there, knowing what to do may be what gets you home.",
        prerequisites = { "ghost_in_the_static" },
        requirements = {
            { label = "Suture Needles", amount = 2, types = { "Base.SutureNeedle" } },
            { label = "Disinfectant", amount = 2, types = { "Base.Disinfectant" } },
        },
        skillRequirements = {
            { label = "First Aid", perk = Perks.Doctor, level = 2 },
        },
        rewards = {
            { type = "trust", contactId = "dr_layne", amount = 10 },
            { type = "item", fullType = "Base.Antibiotics", label = "Antibiotics", amount = 1 },
            { type = "item", fullType = "Base.PillsBeta", label = "Beta Blockers", amount = 1 },
        },
    },
    {
        number = 23,
        id = "keeping_them_running",
        contactId = "sgt_major_graves",
        name = "Keeping Them Running",
        description = "Reach Mechanics 2 and hand in roadside repair equipment.",
        flavorText = "I have vehicles, but no motor pool worth the name. Tires are going flat, parts are wearing out, and every vehicle we lose cuts down how far my people can operate. Learn enough basic mechanics to handle routine repairs and bring me a jack, lug wrench, and tire pump. If we intend to keep expanding our supply routes, I need those vehicles running.",
        completedFlavorText = "Good. That gives us enough equipment to handle the most common roadside problems. I have people checking the vehicles now and pulling anything that is likely to fail. Keeping them running means longer supply runs and fewer soldiers walking home. We are going to need that range soon.",
        prerequisites = { "ghost_in_the_static" },
        requirements = {
            { label = "Jack", amount = 1, types = { "Base.Jack" } },
            { label = "Lug Wrench", amount = 1, types = { "Base.LugWrench" } },
            { label = "Tire Pump", amount = 1, types = { "Base.TirePump" } },
        },
        skillRequirements = {
            { label = "Mechanics", perk = Perks.Mechanics, level = 2 },
        },
        rewards = {
            { type = "trust", contactId = "sgt_major_graves", amount = 10 },
            { type = "item", fullType = "Base.PetrolCan", label = "Full Gas Can", amount = 2, fillPetrol = true },
        },
    },
    {
        number = 24,
        id = "riverside_patrol",
        contactId = "franklin_porch",
        name = "Riverside Patrol",
        description = "Kill 25 zombies in Riverside.",
        flavorText = "Riverside is filling up with the dead. My scavengers are trying to work the commercial district, but every trip gets harder as more of them wander in from the neighborhoods. Get into town and put down twenty-five. That should give my people enough room to work without fighting their way through every block.",
        completedFlavorText = "That helped. My people made it into the commercial district and came back with a decent haul. There are still plenty of dead in Riverside, but twenty-five fewer makes the streets a little easier to move through. Keep chipping away at them.",
        prerequisites = { "ghost_in_the_static" },
        requirements = {},
        skillRequirements = {},
        objectives = {
            { id = "riverside_zombie_kills", type = "zombie_kills", townKey = "riverside", label = "Zombies killed in Riverside", amount = 25 },
        },
        rewards = {
            { type = "trust", contactId = "franklin_porch", amount = 10 },
            { type = "item", fullType = "Base.ShotgunShellsBox", label = "Box of Shotgun Shells", amount = 3 },
            { type = "item", fullType = "Base.Shotgun", label = "JS-2000 Shotgun", amount = 1 },
            { type = "item", fullType = "Base.Gloves_LeatherGloves", label = "Leather Gloves", amount = 1 },
        },
    },
    {
        number = 25,
        id = "dead_drop",
        contactId = "silas_mercer",
        name = "The Dead Drop",
        description = "Recover the Courier's Ledger from Knox Bank in Muldraugh.",
        flavorText = "I had a guy working Muldraugh before everything fell apart. He kept a paper ledger with names, deliveries, payments, all the things sensible people normally make sure never get written down. His last message said he left it in his office before things got bad. Do you know the bank in Muldraugh? He worked in an office there. Bring me the ledger and I will make it worth the trip.",
        completedFlavorText = "There it is. Names, payments, safehouses... a lot of useful information in here. This entry is different, though. Military buyer, civilian courier, sealed medical cargo, paid entirely in cash. It happened only a few days before the outbreak. I think Vance is going to want to hear about this one.",
        prerequisites = { "ghost_in_the_static" },
        requirements = {
            { label = "Courier's Ledger", amount = 1, types = { "ExtractionMode.CouriersLedger" },
                raidSpawnPoint = { x = 10623, y = 9698, z = 1 }, locationTownKey = "muldraugh" },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "silas_mercer", amount = 10 },
            { type = "item", fullType = "Base.Revolver", label = "Patrol Revolver", amount = 1 },
            { type = "item", fullType = "Base.Bullets357Box", label = "Box of .357 Magnum Rounds", amount = 2 },
            { type = "item", fullType = "Base.Necklace_Gold", label = "Gold Necklace", amount = 1 },
        },
    },
    {
        number = 26,
        id = "cold_storage",
        contactId = "dr_layne",
        name = "Cold Storage",
        description = "Recover a Medical Transport Cooler from Riverside.",
        flavorText = "Vance asked how we would transport a biological sample without ruining it. For once, he asked a sensible question. We need a proper medical transport cooler, something meant for blood, tissue, or temperature-sensitive medication. The children's medical facility in Riverside should have one. Find me one that is still in usable condition.",
        completedFlavorText = "This will work. The seal is intact and the insulation has not been damaged. I can clean it out and have it ready if Vance actually finds something worth transporting. I am starting to understand what he is working toward. I just hope he knows what he is doing.",
        prerequisites = { "steady_hands", "riverside_patrol" },
        requirements = {
            { label = "Medical Transport Cooler", amount = 1, types = { "ExtractionMode.MedicalTransportCooler" },
                raidSpawnPoint = { x = 6669, y = 5454, z = 0 }, locationTownKey = "riverside" },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "dr_layne", amount = 10 },
            { type = "item", fullType = "Base.Antibiotics", label = "Antibiotics", amount = 3 },
            { type = "item", fullType = "Base.Bag_MedicalBag", label = "Trauma Bag", amount = 1 },
            { type = "item", fullType = "Base.Pills", label = "Painkillers", amount = 2 },
        },
    },
    {
        number = 27,
        id = "cross_reference",
        contactId = "elias_vance",
        name = "Cross Reference",
        description = "Recover the Ekron Transfer Manifest and bring writing supplies.",
        flavorText = "Mercer's ledger confirms that research material was being moved through civilian channels. One of the shipments was supposed to go to someone in Ekron, and there should have been a transfer manifest with it. If everything collapsed before it was delivered, there is a good chance the paperwork is still sitting in the post office. Check there and bring me the manifest. Bring a couple of notebooks and pens too. I want a paper copy of whatever I can reconstruct.",
        completedFlavorText = "There. The shipment numbers match the records from Rosewood. It passed through Ekron and was assigned to Aaron Keller, one of the logistics staff attached to the containment program. The delivery address listed here is in Rosewood. If Keller kept copies of anything he was working on, his residence is the next place to look.",
        prerequisites = { "paper_trail", "dead_drop" },
        requirements = {
            { label = "Transfer Manifest", amount = 1, types = { "ExtractionMode.TransferManifest" },
                raidSpawnPoint = { x = 685, y = 9858, z = 0 }, locationTownKey = "ekron" },
            { label = "Empty Notebooks", amount = 2, types = { "Base.Notebook" } },
            { label = "Pens", amount = 2, types = { "Base.Pen" } },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "elias_vance", amount = 10 },
        },
    },
    {
        number = 28,
        id = "court_with_death",
        contactId = "elias_vance",
        name = "Court with Death",
        description = "Reach Sneaking and Lightfooted 2, then recover the Researcher's Notes.",
        flavorText = "The name on that manifest is Aaron Keller, I know that name... He was investigating transport records and patient transfers during the containment operation. He worked at the Court of Justice in Rosewood. That area was packed with people before the outbreak and it will be worse now. Learn to move quietly before you go in. I need whatever notes Keller left behind, not a trail of dead leading straight back to you.",
        completedFlavorText = "Keller kept copies of almost everything. Transfer orders, patient numbers, internal messages. Most of it is routine, but this is not. During the first days of the quarantine, several infected patients were transferred to Knox Penitentiary under military supervision. They used the prison infirmary as an isolation ward. If the medical records are still there, they may contain observations from some of the earliest known cases.",
        prerequisites = { "cross_reference" },
        requirements = {
            { label = "Researcher's Notes", amount = 1, types = { "ExtractionMode.ResearchersNotes" },
                raidSpawnPoint = { x = 8079, y = 11670, z = 0 }, locationTownKey = "rosewood" },
        },
        skillRequirements = {
            { label = "Sneaking", perk = Perks.Sneak, level = 2 },
            { label = "Lightfooted", perk = Perks.Lightfoot, level = 2 },
        },
        rewards = {
            { type = "trust", contactId = "elias_vance", amount = 10 },
            { type = "location", townKey = "knox_penitentiary", label = "Unlock Raid Location: Knox Penitentiary" },
        },
    },
    {
        number = 29,
        id = "observation_ward",
        contactId = "elias_vance",
        name = "The Observation Ward",
        description = "Recover the Quarantine Medical Records from Knox Penitentiary.",
        flavorText = "Keller's notes confirm that the military used Knox Penitentiary to hold infected patients during the early quarantine. The prison already had walls, cells, guards, and an infirmary, so it made an effective isolation site. I need the medical records from that ward. Do not waste time clearing the whole prison. Get into the medical or administrative area, find the quarantine records, and get out.",
        completedFlavorText = "These are the records. Most of the patients followed the same pattern, but not all of them. Three were exposed at roughly the same time. Two deteriorated quickly. The third remained coherent much longer than expected. The doctors noticed it too. They took repeated blood samples and sent them somewhere else for testing. The samples are gone, but the records tell us where they went. We have another lead.",
        prerequisites = { "court_with_death" },
        requirements = {
            { label = "Quarantine Medical Records", amount = 1, types = { "ExtractionMode.QuarantineMedicalRecords" },
                raidSpawnPoint = { x = 7522, y = 11864, z = 0 }, locationTownKey = "knox_penitentiary" },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "elias_vance", amount = 10 },
        },
    },
    {
        number = 30,
        id = "containment",
        contactId = "franklin_porch",
        name = "Containment",
        description = "Kill 50 zombies anywhere in the Exclusion Zone.",
        flavorText = "Heard you went poking around the penitentiary. Trips like that stir up more dead than people realize. Engines, gunshots, doors opening, people moving through places that have been quiet for weeks. My scouts are seeing groups drifting onto our usual routes. Take fifty of them down before those groups start joining together. Better to deal with them now than wait for a herd.",
        completedFlavorText = "That's better. Scouts are seeing fewer groups on the roads and the larger packs are starting to break apart. Whatever Vance found in those prison records has Graves and Layne interested, so I figure it must be important. If his research is going somewhere, I will keep the dead from getting in the way.",
        prerequisites = { "observation_ward","cold_storage" },
        requirements = {},
        skillRequirements = {},
        objectives = {
            { id = "zombie_kills", type = "zombie_kills", label = "Zombies killed", amount = 50 },
        },
        rewards = {
            { type = "trust", contactId = "franklin_porch", amount = 10 },
            { type = "item", fullType = "Base.HuntingRifle", label = "MSR788 Rifle", amount = 1 },
            { type = "item", fullType = "Base.308Box", label = "Box of 7.62x51mm Rounds", amount = 4 },
            { type = "item", fullType = "Base.x4Scope", label = "4x Scope", amount = 1 },
            { type = "item", fullType = "Base.HuntingKnife", label = "Military Hunting Knife", amount = 1 },
        },
    },
    {
        number = 31,
        id = "eyes_on_west_point",
        contactId = "sgt_major_graves",
        name = "Eyes on West Point",
        description = "Visit Thunder Gas, Spiffo's, and the West Point School Office during one West Point raid.",
        flavorText = "I am not sending people into West Point blind. I need a basic survey of the town before I commit vehicles and scavenging teams. Check Thunder Gas, Spiffo's, and the school. I want to know what is still standing, how bad the streets are, and whether any of those locations are worth coming back to. You do not need to bring anything home. Just get eyes on all three and make it back.",
        completedFlavorText = "Good. That gives me enough to start planning proper runs into town. Fuel access is still possible, there are supplies worth recovering, and the school may be useful as a landmark or staging point. I will start putting routes together.",
        prerequisites = { "containment" },
        requirements = {},
        skillRequirements = {},
        objectives = {
            { id = "visit_thunder_gas", type = "raid_visit", townKey = "west_point",
                label = "Thunder Gas visited", amount = 1, x = 11824, y = 6871, radius = 10 },
            { id = "visit_spiffos", type = "raid_visit", townKey = "west_point",
                label = "Spiffo's visited", amount = 1, x = 11976, y = 6804, radius = 10 },
            { id = "visit_school_office", type = "raid_visit", townKey = "west_point",
                label = "West Point School Office visited", amount = 1, x = 11338, y = 6774, radius = 10 },
        },
        rewards = {
            { type = "trust", contactId = "sgt_major_graves", amount = 10 },
            { type = "item", fullType = "Base.Bag_ALICEpack_Army", label = "Military Backpack", amount = 1 },
        },
    },
    {
        number = 32,
        id = "field_medicine",
        contactId = "dr_layne",
        name = "Field Medicine",
        description = "Reach First Aid 3 and hand in antibiotics, suture needles, and disinfectant.",
        flavorText = "You are traveling farther from the bunker now, which means I cannot help you every time something goes wrong. I need you to know how to handle more serious injuries on your own. Keep practicing your first aid, and bring me antibiotics, sutures, and disinfectant. Those are the supplies I am burning through fastest here.",
        completedFlavorText = "That is better. You are not a doctor, but you know enough now to keep a bad injury from becoming a fatal one. I can put these supplies to use immediately. Take the manual and instruments. You are going to need them if these longer runs continue.",
        prerequisites = { "containment" },
        requirements = {
            { label = "Antibiotics", amount = 2, types = { "Base.Antibiotics" } },
            { label = "Suture Needles", amount = 3, types = { "Base.SutureNeedle" } },
            { label = "Disinfectant", amount = 2, types = { "Base.Disinfectant" } },
        },
        skillRequirements = {
            { label = "First Aid", perk = Perks.Doctor, level = 3 },
        },
        rewards = {
            { type = "trust", contactId = "dr_layne", amount = 10 },
            { type = "item", fullType = "Base.BookFirstAid2", label = "First Aid for Intermediates", amount = 1 },
            { type = "item", fullType = "Base.SutureNeedleHolder", label = "Suture Needle Holder", amount = 1 },
            { type = "item", fullType = "Base.Pills", label = "Painkillers", amount = 2 },
            { type = "item", fullType = "Base.PillsBeta", label = "Beta Blockers", amount = 2 },
        },
    },
    {
        number = 33,
        id = "roadworthy",
        contactId = "sgt_major_graves",
        name = "Roadworthy",
        description = "Reach Mechanics 3 and Electrical 2, then hand in a battery, wrench, and two full gas cans.",
        flavorText = "The vehicles are holding together, but local repairs are not enough anymore. We are starting to push farther north, and a breakdown twenty miles from the bunker is a different problem than one down the road. Improve your mechanical and electrical skills and bring me a battery, fuel, and a wrench. I want enough spare equipment on hand to keep a vehicle moving when something fails in the field.",
        completedFlavorText = "Good. The battery is already assigned to one of the trucks and the fuel is going into the reserve. This gives us a little more confidence sending vehicles beyond our usual routes. Take the ammunition. If you are helping keep my people moving, I can spare the gear to keep you alive.",
        prerequisites = { "containment" },
        requirements = {
            { label = "Car Battery", amount = 1, types = { "Base.CarBattery1", "Base.CarBattery2", "Base.CarBattery3" } },
            { label = "Wrench", amount = 1, types = { "Base.Wrench" } },
            { label = "Full Gas Cans", amount = 2, types = { "Base.PetrolCan" }, requiresFullPetrol = true },
        },
        skillRequirements = {
            { label = "Mechanics", perk = Perks.Mechanics, level = 3 },
            { label = "Electrical", perk = Perks.Electricity, level = 2 },
        },
        rewards = {
            { type = "trust", contactId = "sgt_major_graves", amount = 10 },
            { type = "item", fullType = "Base.556Box", label = "Box of 5.56x45mm Rounds", amount = 5 },
        },
    },
    {
        number = 34,
        id = "west_point_sweep",
        contactId = "franklin_porch",
        name = "West Point Sweep",
        description = "Kill 50 zombies in West Point.",
        flavorText = "Graves is starting to send people into West Point, and that means I need some of the dead taken off the streets first. Get into town and put down fifty. Focus on the areas people actually need to move through and do not get yourself boxed in trying to chase one more kill.",
        completedFlavorText = "That opened things up. Scouts are already reporting easier movement through town, and Graves should have fewer problems getting her people where they need to go. Take the rifle. You are working farther from home now, so having something with reach will not hurt.",
        prerequisites = { "containment" },
        requirements = {},
        skillRequirements = {},
        objectives = {
            { id = "west_point_zombie_kills", type = "zombie_kills", townKey = "west_point",
                label = "Zombies killed in West Point", amount = 50 },
        },
        rewards = {
            { type = "trust", contactId = "franklin_porch", amount = 10 },
            { type = "item", fullType = "Base.AssaultRifle2", label = "M14 Rifle", amount = 1 },
            { type = "item", fullType = "Base.M14Clip", label = "M14 Magazine", amount = 1 },
            { type = "item", fullType = "Base.308Box", label = "Box of 7.62x51mm Rounds", amount = 3 },
        },
    },
    {
        number = 35,
        id = "creature_comforts",
        contactId = "silas_mercer",
        name = "Creature Comforts",
        description = "Hand in bourbon, cigarette packs, a gold watch, and jewelry.",
        flavorText = "People will trade a surprising amount for something that makes the world feel normal for ten minutes. Booze, cigarettes, a nice watch, a little jewelry. I have buyers sitting on useful equipment and nothing enjoyable to spend it on. Bring me a decent bundle and I will make sure you get something practical in return.",
        completedFlavorText = "Perfect. This is exactly the kind of thing people convince themselves they cannot live without. I already know where most of it is going. Take the bag and the food in it. We've got plenty and I want to keep useful people like you healthy!",
        prerequisites = { "containment" },
        requirements = {
            { label = "Bourbon", amount = 2, types = { "Base.Whiskey" } },
            { label = "Cigarette Packs", amount = 3, types = { "Base.CigarettePack" } },
            { label = "Gold Watch", amount = 1, types = { "Base.WristWatch_Right_ClassicGold", "Base.WristWatch_Left_ClassicGold" } },
            { label = "Jewelry", amount = 3, typePrefixes = { "Base.Necklace", "Base.Ring_", "Base.Earring_", "Base.Bracelet_", "Base.NoseRing_", "Base.BellyButton_" } },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "silas_mercer", amount = 10 },
            { type = "item", fullType = "Base.Bag_DuffelBag", label = "Duffel Bag with 5 Random Cans of Food", amount = 1,
                randomContents = { amount = 5, types = CANNED_FOOD_REWARDS } },
        },
    },
    {
        number = 36,
        id = "opportunity_knocks",
        contactId = "silas_mercer",
        name = "Opportunity Knocks",
        description = "Visit the West Point Post Office, Police Department, and Pharmacy during one raid.",
        flavorText = "I have enough people asking for goods that I need to know what is still worth hitting in West Point. I do not need you hauling anything back yet. Check the Post Office, Police Department, and the Pharmacy. I want to know which places are intact, which ones are picked clean, and which ones still look worth sending people into.",
        completedFlavorText = "That saves me a lot of wasted trips. Two of those locations sound worth the trouble, and I already know who will pay for what is left inside. Take the sledgehammer. It has been sitting in one of my stashes for weeks, and you will get more use out of it than I will.",
        prerequisites = { "creature_comforts" },
        requirements = {},
        skillRequirements = {},
        objectives = {
            { id = "visit_post_office", type = "raid_visit", townKey = "west_point",
                label = "West Point Post Office visited", amount = 1, x = 11958, y = 6912, radius = 10 },
            { id = "visit_police_department", type = "raid_visit", townKey = "west_point",
                label = "West Point Police Department visited", amount = 1, x = 11812, y = 6810, radius = 10 },
            { id = "visit_pharmacy", type = "raid_visit", townKey = "west_point",
                label = "West Point Pharmacy visited", amount = 1, x = 11933, y = 6801, radius = 10 },
        },
        rewards = {
            { type = "trust", contactId = "silas_mercer", amount = 10 },
            { type = "item", fullType = "Base.Sledgehammer", label = "Sledgehammer", amount = 1 },
        },
    },
    {
        number = 37,
        id = "forward_supplies",
        contactId = "sgt_major_graves",
        name = "Forward Supplies",
        description = "Reach Carpentry 4 and hand in construction tools, hardware, and flashlights.",
        flavorText = "We are operating too far from the bunker to keep treating every run like a day trip. I want a forward supply position set up where patrols can rest, make repairs, and store emergency gear. I need someone who knows enough carpentry to make it hold together, plus the tools and hardware to build it. Bring me what I need and I will handle the manpower.",
        completedFlavorText = "The position is going up now. It is not much to look at, but it gives my people somewhere to regroup without driving all the way back here. You supplied most of what made it possible, so take the vest and ammunition. I would rather see good equipment used than gathering dust in storage.",
        prerequisites = { "roadworthy" },
        requirements = {
            { label = "Boxes of Nails", amount = 3, types = { "Base.NailsBox" } },
            { label = "Hammer", amount = 1, types = { "Base.Hammer" } },
            { label = "Saw", amount = 1, types = { "Base.Saw" } },
            { label = "Wood Glue", amount = 2, types = { "Base.Woodglue" } },
            { label = "Flashlights", amount = 2, types = { "Base.HandTorch", "Base.Torch", "Base.FlashLight_AngleHead", "Base.FlashLight_AngleHead_Army" } },
        },
        skillRequirements = {
            { label = "Carpentry", perk = Perks.Woodwork, level = 4 },
        },
        rewards = {
            { type = "trust", contactId = "sgt_major_graves", amount = 10 },
            { type = "item", fullType = "Base.Vest_BulletArmy", label = "Military Bulletproof Vest", amount = 1 },
            { type = "item", fullType = "Base.556Box", label = "Box of 5.56x45mm Rounds", amount = 3 },
        },
    },
    {
        number = 38,
        id = "evacuation_triage",
        contactId = "dr_layne",
        name = "Evacuation Triage",
        description = "Reach First Aid 4 and recover the Evacuation Records from the West Point School Office.",
        flavorText = "I have been comparing what survivors remember about the evacuation, and several stories mention the West Point school being used as a temporary staging point. The school administration or whoever took over certainly had a record of how they routed evacuees. If we can see where the wounded and suspected infected were being sent, we may learn what the military was doing before the routes collapsed.",
        completedFlavorText = "This explains a lot. The sick and injured were being screened locally, then sent north to a military checkpoint outside Louisville. Some were evacuated, some were quarantined, and some simply disappear from the paperwork after reaching the checkpoint. If we want to know what happened to them, that is the next place to look.",
        prerequisites = { "field_medicine" },
        requirements = {
            { label = "Evacuation Records", amount = 1, types = { "ExtractionMode.EvacuationRecords" },
                raidSpawnPoint = { x = 11355, y = 6769, z = 0 }, locationTownKey = "west_point" },
        },
        skillRequirements = {
            { label = "First Aid", perk = Perks.Doctor, level = 4 },
        },
        rewards = {
            { type = "trust", contactId = "dr_layne", amount = 10 },
            { type = "item", fullType = "Base.FirstAidKit_Military", label = "Military First Aid Kit with Random Supplies", amount = 1,
                randomContents = { amount = 3, types = MILITARY_MEDICAL_REWARDS } },
            { type = "location", townKey = "louisville_checkpoint", label = "Unlock Raid Location: Louisville Military Checkpoint" },
        },
    },
    {
        number = 39,
        id = "break_the_blockade",
        contactId = "franklin_porch",
        name = "Break the Blockade",
        description = "Kill 100 zombies at the Louisville Military Checkpoint.",
        flavorText = "Layne found your checkpoint. Problem is, the place is crawling. I have seen enough military positions after they fall to know what that means. Tight spaces, fences, abandoned vehicles, and a whole lot of dead packed into one place. If anybody is going to search it properly, we need room to move. Go in there and put down a hundred. This time I do mean a hundred.",
        completedFlavorText = "Now that made a difference. My people got inside after you cleared the worst of them out. Place was picked over, but the military left plenty behind when they pulled out. We kept what we needed. This pile is yours. The checkpoint is still dangerous, but at least now someone can search it without fighting through a wall of dead first.",
        prerequisites = { "evacuation_triage" },
        requirements = {},
        skillRequirements = {},
        objectives = {
            { id = "checkpoint_zombie_kills", type = "zombie_kills", townKey = "louisville_checkpoint",
                label = "Zombies killed at the Military Checkpoint", amount = 100 },
        },
        rewards = {
            { type = "trust", contactId = "franklin_porch", amount = 10 },
            { type = "item", fullType = "Base.AssaultRifle", label = "M16 Assault Rifle", amount = 1 },
            { type = "item", fullType = "Base.556Clip", label = "M16 Magazine", amount = 2 },
            { type = "item", fullType = "Base.556Box", label = "Box of 5.56x45mm Rounds", amount = 4 },
        },
    },
    {
        number = 40,
        id = "the_transfer_point",
        contactId = "elias_vance",
        name = "The Transfer Point",
        description = "Recover the Medical Transfer Records from the Louisville Military Checkpoint.",
        flavorText = "Layne told me what she found. More importantly, Franklin has made the checkpoint searchable. I need the medical transfer records from inside. The prison files said blood samples from one of the unusual cases were sent out for testing. If those samples passed through this checkpoint, there should be a record of where they went next. Find it, probably in a medical tent.",
        completedFlavorText = "Here it is. The patient number matches the records from the penitentiary. The blood samples reached the checkpoint and were transferred onward before the evacuation route failed. They were sent into Louisville. I do not have the final destination yet, but this proves the samples made it that far. The trail is still there. Take this containment gear. If the trail leads into Louisville, you are going to need it.",
        prerequisites = { "break_the_blockade" },
        requirements = {
            { label = "Medical Transfer Records", amount = 1, types = { "ExtractionMode.MedicalTransferRecords" },
                raidSpawnPoint = { x = 12542, y = 4336, z = 0 }, locationTownKey = "louisville_checkpoint" },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "elias_vance", amount = 10 },
            { type = "item", fullType = "Base.HazmatSuit", label = "Hazmat Suit", amount = 1 },
            { type = "item", fullType = "Base.Hat_NBCmask", label = "NBC Mask", amount = 1 },
            { type = "item", fullType = "Base.GasmaskFilter", label = "Gas Mask Filters", amount = 2 },
        },
    },
    {
        number = 41,
        id = "reconnaissance_in_force",
        contactId = "sgt_major_graves",
        name = "Reconnaissance in Force",
        description = "Scout the Louisville Community Center.",
        flavorText = "I am not sending people into Louisville blind. We were told the Community Center was going to be stockpiled with supplies and shipped out around the area. I want to know how badly the roads are blocked, where the dead are concentrating, and whether the center actually has the supplies. You do not need to bring anything back, just get me the information and get out alive.",
        completedFlavorText = "Empty? What a load of bullshit. Of course they lied about having aid supplies stockpiled... At least you have information on the way in to the city. There are routes we can use and places my people can fall back to if things go wrong. You have done more for my unit than I expected when you first came over the radio. Take the rifle and ammunition. If you are going deeper into Louisville, I want you carrying something I know can keep you alive.",
        prerequisites = { "the_transfer_point" },
        requirements = {},
        skillRequirements = {},
        objectives = {
            { id = "visit_community_center", type = "raid_visit", townKey = "louisville",
                label = "Louisville Community Center visited", amount = 1,
                x = 12848, y = 1697, z = 0, radius = 10 },
        },
        rewards = {
            { type = "trust", contactId = "sgt_major_graves", amount = 10 },
            { type = "item", fullType = "Base.AssaultRifle", label = "M16 Assault Rifle", amount = 1 },
            { type = "item", fullType = "Base.556Clip", label = "M16 Magazine", amount = 3 },
            { type = "item", fullType = "Base.556Box", label = "Box of 5.56x45mm Rounds", amount = 6 },
        },
    },
    {
        number = 42,
        id = "the_original_case",
        contactId = "dr_layne",
        name = "The Original Case",
        description = "Reach First Aid 3 and recover the Patient Zero Sample from St. Peregrin Hospital.",
        flavorText = "Vance has been focused on the prisoner whose infection progressed unusually slowly, but we still need something to compare that case against. The evacuation records reference preserved material from one of the earliest confirmed patients being sent to St. Peregrin Hospital. If that specimen survived, it may show us what the infection looked like before anyone started experimenting with treatments. I bet the sample is in the morgue area. You know enough medicine now to recognize what you are looking for. Find the sample, keep it sealed, and bring it directly to me.",
        completedFlavorText = "The labeling matches the early admission records and the container is still intact. This is the sample we were looking for. I am not opening it here. Vance has the equipment and the research notes, so I am sending it directly to him. If there is anything left in this sample that can help us understand the infection, he has the best chance of finding it. You have done everything I could reasonably ask of you. Thank you.",
        prerequisites = { "the_transfer_point" },
        requirements = {
            { label = "Patient Zero Sample", amount = 1, types = { "ExtractionMode.PatientZeroSample" },
                raidSpawnPoint = { x = 12953, y = 2000, z = 0 }, locationTownKey = "louisville" },
        },
        skillRequirements = {
            { label = "First Aid", perk = Perks.Doctor, level = 3 },
        },
        rewards = {
            { type = "trust", contactId = "dr_layne", amount = 10 },
            { type = "item", fullType = "Base.FirstAidKit_Military",
                label = "Military First Aid Kit with 8 Random Medical Supplies", amount = 1,
                randomContents = { amount = 8, types = MILITARY_MEDICAL_REWARDS } },
        },
    },
    {
        number = 43,
        id = "city_of_opportunity",
        contactId = "silas_mercer",
        name = "City of Opportunity",
        description = "Scout Boxpop Brewery, Knox Distillery, and the central pharmacy during one Louisville raid.",
        flavorText = "Everybody else hears Louisville and starts talking about hospitals, soldiers, and evacuation routes. I hear Louisville and think about everything people left behind. Good drinks, good drugs, and jewelry, whole warehouses nobody has touched in months. Check the two easy to access distilleries and the central pharmacy. I don't need you carrying anything back yet. I just want to know which places still look worth the trouble.",
        completedFlavorText = "Now that is useful information. Some of it sounds picked over, but there is still enough sitting in that city to keep my buyers happy for a very long time. Take the tools and the bag. I have plenty of merchandise and not enough people I trust to go after it. I would rather keep you properly equipped.",
        prerequisites = { "the_transfer_point" },
        requirements = {},
        skillRequirements = {},
        objectives = {
            { id = "visit_boxpop", type = "raid_visit", townKey = "louisville",
                label = "Boxpop Brewery visited", amount = 1, x = 13174, y = 1593, z = 0, radius = 10 },
            { id = "visit_knox_distillery", type = "raid_visit", townKey = "louisville",
                label = "Knox Distillery visited", amount = 1, x = 12914, y = 1383, z = 0, radius = 10 },
            { id = "visit_central_pharmacy", type = "raid_visit", townKey = "louisville",
                label = "Pharmacy visited", amount = 1, x = 12951, y = 1483, z = 0, radius = 10 },
        },
        rewards = {
            { type = "trust", contactId = "silas_mercer", amount = 10 },
            { type = "item", fullType = "Base.Bag_NormalHikingBag",
                label = "Hiking Bag with Welding Equipment", amount = 1,
                fixedContents = {
                    { fullType = "Base.BlowTorch", amount = 2 },
                    { fullType = "Base.WeldingMask", amount = 1 },
                } },
        },
    },
    {
        number = 44,
        id = "off_the_books",
        contactId = "silas_mercer",
        name = "Off the Books",
        description = "Recover the Commercial Property Records from the Louisville County Clerk and Records office.",
        flavorText = "Scouting storefronts is useful, but the real money is in knowing what is behind them. The County Clerk keeps property records, permits, ownership changes, renovations, all the boring paperwork that tells you who had money and where they spent it. I want the commercial records. Bring me anything covering the larger properties in the city. If I know who owned what, I can work out which buildings are actually worth risking people for.",
        completedFlavorText = "This is better than I expected. Renovations, storage expansions, private loading areas... there are a lot of places in here nobody walking through the front door would ever know existed. One file keeps coming up: the Grand Ohio Mall. Major renovation work, new service access, rooftop modifications. The detailed building plans were filed with City Hall. Those plans could be worth more than anything sitting in the stores themselves.",
        prerequisites = { "city_of_opportunity" },
        requirements = {
            { label = "Commercial Property Records", amount = 1,
                types = { "ExtractionMode.CommercialPropertyRecords" },
                raidSpawnPoint = { x = 12635, y = 1453, z = 0 }, locationTownKey = "louisville" },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "silas_mercer", amount = 10 },
            { type = "item", fullType = "Base.Katana", label = "Katana", amount = 1 },
        },
    },
    {
        number = 45,
        id = "desperate_measures",
        contactId = "elias_vance",
        name = "Desperate Measures",
        description = "Bring Vance three samples of the Knox Infection Cure for analysis.",
        flavorText = "Layne sent me the early sample. It is useful, but it only tells me what the infection looked like before anyone tried to interfere with it. I keep hearing reports of so-called cures circulating inside the Zone. I do not trust the name, and neither should you. If they actually worked reliably, we would know by now. Bring me three of them. I need enough material to compare what people are using against the original infection sample.",
        completedFlavorText = "Interesting. These aren't identical, but they aren't random either. Whoever made them was working from incomplete information, just like I am, but there are common elements in all three. Something here is suppressing part of the infection's response. Layne's sample gives me the infection before treatment. These give me three attempts at interfering with it. That's enough to start comparing them properly. The problem is I can't run half my equipment long enough to do it.",
        prerequisites = { "the_original_case" },
        requirements = {
            { label = "Knox Infection Cure", amount = 3, types = { "ExtractionMode.InfectionCure" } },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "elias_vance", amount = 10 },
        },
    },
    {
        number = 46,
        id = "the_grand_prize",
        contactId = "silas_mercer",
        name = "The Grand Prize",
        description = "Recover the Grand Ohio Mall Building Plans from Louisville City Hall.",
        flavorText = "The county records were right. City Hall should have the full plans for the Grand Ohio Mall: service corridors, loading docks, maintenance rooms, storage areas, everything the customers were never supposed to see. That place is enormous. If we are going to get anything worthwhile out of it, I want to know exactly how it was built before anyone goes wandering inside. Find the plans and bring them to me.",
        completedFlavorText = "Now this is a prize. Loading bays, service halls, storerooms, roof access... there are enough hidden routes in this building to keep scavengers busy for weeks. Here is something that matters to your friends, though. The roof has a helipad. Obviously that is not the only helipad in Louisville, but look at where the mall sits compared to the edge of the Exclusion Zone. A pilot could cross the restricted airspace, land, and turn back almost immediately. Anywhere deeper in the city and they would be in the air long enough for somebody outside to notice. Could be useful, eh? See you around, kid.",
        prerequisites = { "off_the_books" },
        requirements = {
            { label = "Grand Ohio Mall Building Plans", amount = 1,
                types = { "ExtractionMode.GrandOhioMallPlans" },
                raidSpawnPoint = { x = 12559, y = 1520, z = 0 }, locationTownKey = "louisville" },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "silas_mercer", amount = 10 },
            { type = "location", townKey = "grand_ohio_mall", label = "Unlock Raid Location: Grand Ohio Mall" },
        },
    },
    {
        number = 47,
        id = "controlled_conditions",
        contactId = "elias_vance",
        name = "Controlled Conditions",
        description = "Reach Electrical 4 and provide stable power and replacement electrical components.",
        flavorText = "I've learned everything I can from the samples without running proper tests. Layne's specimen gives me a baseline, and those cures show that someone has already found ways to interfere with the infection, even if they don't fully understand how. The problem now is my equipment. I can run pieces of it for a few minutes at a time, but that's not enough. I need stable power, replacement wiring, and enough electrical components to keep the instruments running without risking the entire setup every time I switch something on. Get me a generator, a couple of usable batteries, wire, and whatever electronic components you can salvage. If I lose this equipment now, we're finished.",
        completedFlavorText = "Good. Power is stable and the damaged circuits are holding. I can finally run the equipment long enough to compare the original sample against the cure compounds properly. There are patterns here I couldn't see before. I'm not ready to call it a vaccine. Not yet. But I know what I need to reproduce now.",
        prerequisites = { "desperate_measures" },
        requirements = {
            { label = "Generator", amount = 1,
                types = { "Base.Generator", "Base.Generator_Blue", "Base.Generator_Old", "Base.Generator_Yellow" } },
            { label = "Car Batteries", amount = 2,
                types = { "Base.CarBattery1", "Base.CarBattery2", "Base.CarBattery3" } },
            { label = "Electronic Scrap", amount = 10, types = { "Base.ElectronicsScrap" } },
            { label = "Electrical Wire", amount = 5, types = { "Base.ElectricWire" } },
        },
        skillRequirements = {
            { label = "Electrical", perk = Perks.Electricity, level = 4 },
        },
        rewards = {
            { type = "trust", contactId = "elias_vance", amount = 10 },
        },
    },
    {
        number = 48,
        id = "the_candidate",
        contactId = "elias_vance",
        name = "The Candidate",
        description = "Provide the sterile medical supplies Vance needs to prepare an experimental vaccine candidate.",
        flavorText = "I have finished the comparison. The early infection sample, the resistant patient's blood, and the cure compounds all point toward the same response. I cannot manufacture this properly with what I have here, but I can prepare a small candidate sample cleanly enough for a real laboratory to continue the work. Bring me sterile gloves, alcohol wipes, disinfectant, and antibiotics. After that, there is nothing else for you to find. It is on me.",
        completedFlavorText = "This is the best I can do here. I cannot tell you it works, and I cannot test it properly without equipment that no longer exists inside the Zone. What I can tell you is that the resistant patient's response was real, and I have reproduced enough of it to give someone outside a place to start. The sample has to leave Knox Country. If it stays here, everything we did ends with us.",
        prerequisites = { "controlled_conditions" },
        requirements = {
            { label = "Surgical Gloves", amount = 2, types = { "Base.Gloves_Surgical" } },
            { label = "Alcohol Wipes", amount = 5, types = { "Base.AlcoholWipes" } },
            { label = "Disinfectant", amount = 2, types = { "Base.Disinfectant" } },
            { label = "Antibiotics", amount = 2, types = { "Base.Antibiotics" } },
        },
        skillRequirements = {},
        rewards = {
            { type = "trust", contactId = "elias_vance", amount = 10 },
            { type = "item", fullType = "ExtractionMode.VaccineSample", label = "Vaccine Sample", amount = 1 },
        },
    },
    {
        number = 49,
        id = "clear_the_landing_zone",
        contactId = "franklin_porch",
        name = "Clear the Landing Zone",
        description = "Kill 100 zombies at and around the Grand Ohio Mall.",
        flavorText = "Silas showed me the mall plans. Helipad might work, but nobody is landing there with that place packed full of dead. Your pilot says he gets one quick pass across the boundary and that is it. Fine. Then we make sure the one place he has to touch down is as clear as we can get it. Get into the Grand Ohio Mall and put down a hundred. I want enough bodies off the ground that nobody has to fight through a crowd just to reach the roof.",
        completedFlavorText = "A hundred down. That is the kind of clearing job I can work with. My people pushed into the mall after you broke the worst of the crowd and pulled what ammunition and optics they could find. Your share is waiting for you. The place is still dangerous, but the route to the roof is a hell of a lot better than it was. If your pilot is serious about making that run, we have done what we can for him.",
        prerequisites = { "the_grand_prize" },
        requirements = {},
        skillRequirements = {},
        objectives = {
            { id = "mall_zombie_kills", type = "zombie_kills", townKey = "grand_ohio_mall",
                label = "Zombies killed at and around the Grand Ohio Mall", amount = 100 },
        },
        rewards = {
            { type = "trust", contactId = "franklin_porch", amount = 10 },
            { type = "item", fullType = "Base.556Box", label = "Box of 5.56x45mm Rounds", amount = 5 },
            { type = "item", fullType = "Base.308Box", label = "Box of 7.62x51mm Rounds", amount = 4 },
            { type = "item", fullType = "Base.ShotgunShellsBox", label = "Box of Shotgun Shells", amount = 4 },
            { type = "item", fullType = "Base.RedDot", label = "Red Dot Sight", amount = 1 },
            { type = "item", fullType = "Base.x8Scope", label = "8x Scope", amount = 1 },
        },
    },
    {
        number = 50,
        id = "one_last_flight",
        contactId = "elias_vance",
        name = "One Last Flight",
        description = "Bring the Vaccine Candidate to the Grand Ohio Mall rooftop helipad and make the handoff.",
        flavorText = "My people answered. They have a pilot willing to make the run, but understand what that means. Nobody outside authorized this. The government is not coming in for us, and they are not opening the Exclusion Zone because we claim to have something important. The airspace is still being watched. If he spends too long inside the perimeter, somebody will detect him, and the jets enforcing the quarantine may shoot him down before anyone bothers asking questions. The mall is our only realistic option. It is close enough to the boundary that he can cross, land, take the sample, and get back out quickly. He will not wait on the roof, don't even try to board. Get the Vaccine Candidate there and make the handoff.",
        completedFlavorText = "He made it. The sample is out. That is all we know for certain. No rescue team followed him in, no announcement came over the radio, and nobody outside suddenly changed their mind about the people still trapped here. Maybe the sample reaches a laboratory. Maybe somebody decides it is worth studying. Maybe one day it becomes something more than the work of a few people hiding inside a dead state. Whatever happens next is outside our hands. We got it across the line. That was the job.",
        prerequisites = { "the_candidate", "clear_the_landing_zone" },
        requirements = {
            { label = "Vaccine Sample", amount = 1, types = { "ExtractionMode.VaccineSample" },
                raidGrantOnInsertion = true, locationTownKey = "grand_ohio_mall" },
        },
        skillRequirements = {},
        objectives = {
            { id = "vaccine_handoff", type = "campaign_handoff",
                label = "Vaccine Candidate handed off at the rooftop helipad", amount = 1 },
        },
        rewards = {
            { type = "trust", contactId = "elias_vance", amount = 10 },
            { type = "campaign", label = "Complete the campaign" },
        },
    },
}

local byId = {}
for id, contact in pairs(contacts) do
    local prefix = "IGUI_ExtractionMode_Contact_" .. id .. "_"
    contact.nameKey = prefix .. "Name"
    if contact.description then contact.descriptionKey = prefix .. "Description" end
    if contact.focus then contact.focusKey = prefix .. "Focus" end
end
for _, definition in ipairs(definitions) do
    local prefix = "IGUI_ExtractionMode_Quest_" .. definition.id .. "_"
    definition.nameKey = prefix .. "Name"
    definition.descriptionKey = prefix .. "Description"
    definition.flavorTextKey = prefix .. "Flavor"
    definition.completedFlavorTextKey = prefix .. "CompletedFlavor"
    for index, requirement in ipairs(definition.requirements or {}) do
        requirement.labelKey = prefix .. "Requirement_" .. tostring(index)
    end
    for index, requirement in ipairs(definition.skillRequirements or {}) do
        requirement.labelKey = prefix .. "Skill_" .. tostring(index)
    end
    for index, objective in ipairs(definition.objectives or {}) do
        objective.labelKey = prefix .. "Objective_" .. tostring(index)
    end
    for index, reward in ipairs(definition.rewards or {}) do
        if reward.label then reward.labelKey = prefix .. "Reward_" .. tostring(index) end
    end
    byId[definition.id] = definition
end

function Quests.definitions()
    return definitions
end

function Quests.definition(id)
    return byId[tostring(id or "")]
end

function Quests.name(definition)
    return Localization.field(definition, "name")
end

function Quests.description(definition)
    return Localization.field(definition, "description")
end

function Quests.flavorText(definition, completed)
    return Localization.field(definition, completed and "completedFlavorText" or "flavorText")
end

function Quests.label(entry)
    return Localization.field(entry, "label")
end

function Quests.contact(id)
    return contacts[tostring(id or "")]
end

function Quests.contactName(id)
    local contact = Quests.contact(id)
    return contact and contact.name or tostring(id or "Unknown")
end

function Quests.contactDisplayName(id)
    local contact = Quests.contact(id)
    return contact and Localization.field(contact, "name")
        or Localization.get("IGUI_ExtractionMode_Unknown", "Unknown")
end

function Quests.contactDescription(id)
    return Localization.field(Quests.contact(id), "description")
end

function Quests.contactFocus(id)
    return Localization.field(Quests.contact(id), "focus")
end

function Quests.isCompleted(completed, id)
    return completed ~= nil and completed[tostring(id or "")] == true
end

function Quests.isAcquired(completed, definition)
    if definition == nil then return false end
    if Quests.isCompleted(completed, definition.id) then return true end
    for _, id in ipairs(definition.prerequisites or {}) do
        if not Quests.isCompleted(completed, id) then return false end
    end
    return true
end

function Quests.acquiredDefinitions(completed)
    local result = {}
    for _, definition in ipairs(definitions) do
        if Quests.isAcquired(completed, definition) then result[#result + 1] = definition end
    end
    return result
end

function Quests.completionSnapshot(completed)
    local result = {}
    for _, definition in ipairs(definitions) do
        result[definition.id] = Quests.isCompleted(completed, definition.id)
    end
    return result
end

function Quests.objectiveCount(progress, definition, objective)
    if progress == nil or definition == nil or objective == nil then return 0 end
    local questProgress = progress[definition.id]
    return math.max(0, math.floor(tonumber(questProgress and questProgress[objective.id]) or 0))
end

function Quests.objectivesMet(progress, definition)
    if definition == nil then return false end
    for _, objective in ipairs(definition.objectives or {}) do
        if Quests.objectiveCount(progress, definition, objective)
            < math.max(0, math.floor(tonumber(objective.amount) or 0)) then
            return false
        end
    end
    return true
end

function Quests.incrementObjective(progress, definition, objective, amount)
    if progress == nil or definition == nil or objective == nil then return 0 end
    progress[definition.id] = progress[definition.id] or {}
    local maximum = math.max(0, math.floor(tonumber(objective.amount) or 0))
    local current = Quests.objectiveCount(progress, definition, objective)
    local updated = math.min(maximum, current + math.max(0, math.floor(tonumber(amount) or 0)))
    progress[definition.id][objective.id] = updated
    return updated
end

function Quests.resetRaidVisitObjectives(progress, completed)
    if progress == nil then return false end
    local changed = false
    for _, definition in ipairs(definitions) do
        if Quests.isAcquired(completed, definition)
            and not Quests.isCompleted(completed, definition.id) then
            for _, objective in ipairs(definition.objectives or {}) do
                if objective.type == "raid_visit"
                    and Quests.objectiveCount(progress, definition, objective) > 0 then
                    progress[definition.id] = progress[definition.id] or {}
                    progress[definition.id][objective.id] = 0
                    changed = true
                end
            end
        end
    end
    return changed
end

function Quests.objectiveSnapshot(progress)
    local result = {}
    for _, definition in ipairs(definitions) do
        local questProgress = {}
        for _, objective in ipairs(definition.objectives or {}) do
            questProgress[objective.id] = Quests.objectiveCount(progress, definition, objective)
        end
        result[definition.id] = questProgress
    end
    return result
end

function Quests.trustSnapshot(trust)
    local result = {}
    for id in pairs(contacts) do result[id] = math.max(0, tonumber(trust and trust[id]) or 0) end
    return result
end

function Quests.applyRewards(trust, definition)
    if trust == nil or definition == nil then return end
    for _, reward in ipairs(definition.rewards or {}) do
        if reward.type == "trust" and contacts[reward.contactId] ~= nil then
            trust[reward.contactId] = math.max(0, tonumber(trust[reward.contactId]) or 0)
                + math.max(0, tonumber(reward.amount) or 0)
        end
    end
end

-- Quest and upgrade definitions deliberately share the same requirement
-- schema. Keeping the inventory and skill checks in one place ensures nested
-- bags, alternative item types, and server-side consumption behave identically.
function Quests.skillLevel(player, requirement)
    return Upgrades.skillLevel(player, requirement)
end

function Quests.skillRequirementsMet(player, definition)
    return Upgrades.skillRequirementsMet(player, definition)
end

function Quests.missingSkillNames(player, definition)
    return Upgrades.missingSkillNames(player, definition)
end

function Quests.requirementCount(inventory, requirement)
    return Upgrades.requirementCount(inventory, requirement)
end

function Quests.requirementsMet(inventory, definition)
    return Upgrades.requirementsMet(inventory, definition)
end

function Quests.consumeRequirements(inventory, definition)
    return Upgrades.consumeRequirements(inventory, definition)
end

ExtractionMode.Quests = Quests
return Quests
