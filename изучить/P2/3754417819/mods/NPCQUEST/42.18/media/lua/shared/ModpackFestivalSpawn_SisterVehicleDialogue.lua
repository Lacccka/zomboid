-- Alyssa vehicle dialogue data for future sister rebuild work.

ModpackFestivalSister = ModpackFestivalSister or {}

ModpackFestivalSister.VEHICLE_DIALOGUE_LINES = ModpackFestivalSister.VEHICLE_DIALOGUE_LINES or {
    "I think my leg's asleep. Both legs.",
    "My butt hurts.",
    "I'm gonna chew through my own hand if I sit still any longer.",
    "Tell me when something happens. I'm closing my eyes.",
    "Would you rather fight one zombie horse or a hundred zombie ducks. Think about it.",
    "Why don't scientists trust atoms. ...because they make up everything.",
    "Stop drumming on the wheel. I'm gonna lose it.",
    "Thanks for driving.",
    "I'm sorry I'm being annoying. I just have to fill the silence or I'll think.",
    "You're really not as bad at this as I thought you'd be.",
    "I'm glad we're not walking.",
    "Don't fall asleep at the wheel. I will absolutely scream.",
    "Wake me up if anything happens. ...or don't. Yeah, don't.",
    "I'll keep watch. You drive.",
    "Why don't skeletons fight each other. They don't have the guts.",
    "What did the grape say when it got stepped on. Nothing, it just let out a little wine.",
}

function ModpackFestivalSister.getVehicleDialogueLines()
    return ModpackFestivalSister.VEHICLE_DIALOGUE_LINES or {}
end

function ModpackFestivalSister.pickVehicleDialogueLine()
    local lines = ModpackFestivalSister.getVehicleDialogueLines()
    if not lines or #lines == 0 then
        return nil
    end
    if ZombRand then
        return lines[ZombRand(#lines) + 1]
    end
    local idx = 1
    if getTimestampMs then
        idx = (getTimestampMs() % #lines) + 1
    end
    return lines[idx]
end
