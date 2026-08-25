-- Alyssa idle dialogue data for future sister rebuild work.

ModpackFestivalSister = ModpackFestivalSister or {}

ModpackFestivalSister.IDLE_DIALOGUE_LINES = ModpackFestivalSister.IDLE_DIALOGUE_LINES or {
    "It's too quiet out here.",
    "The birds are still around. That's something.",
    "Do you think it's like this everywhere?",
    "Somebody should've stopped this. Somebody whose job it was.",
    "One day I'm gonna fall apart about all this. Just not today.",
    "You're humming again. You always hum when you're nervous.",
    "Bet I could survive longer than you.",
    "If you die first I'm looting your body. Just so we're clear.",
    "This is the most time we've spent together in years.",
    "You're a terrible conversationalist, you know that?",
    "I want a normal day. Just one. Just a boring normal day.",
    "I'd give anything for a hot shower.",
    "I'm bored.",
    "I'm still bored.",
    "You walk slow.",
    "Did you know spiders can survive underwater for like an hour? I think about that a lot.",
    "There's a kind of jellyfish that's basically immortal. We learned that in school.",
    "Cows have best friends. Real ones. That's a fact.",
    "I read that crows can hold grudges for years. Crows are terrifying.",
    "Octopuses taste with their arms. Just FYI.",
    "I always wanted to be in a horror movie. This sucks though.",
    "If we die can I haunt you specifically. Just you.",
    "I always knew the world was gonna end. I just thought it'd be cooler.",
    "My biggest fear used to be talking on the phone. Imagine.",
    "Wish I could just sleep for a year.",
    "Bet I could do this without you. Probably.",
    "I'm not scared. You're scared.",
    "Do you think I'll ever stop flinching at every sound?",
    "I keep finding pennies. Why are there still so many pennies.",
    "...don't be weird about it but I'm glad you're not dead.",
    "If anyone's gonna survive this it's probably us. Probably.",
    "You breathe so loud.",
    "I'd tell you a construction joke but I'm still working on it.",
    "I'm reading a book about anti-gravity. It's impossible to put down.",
    "What do you call a fish with no eyes? A fsh.",
    "I would tell you a chemistry joke but I know I wouldn't get a reaction.",
    "I'm friends with 25 letters of the alphabet. I don't know Y.",
    "I lost my mood ring and I don't know how to feel about it.",
}

ModpackFestivalSister.FLEE_DIALOGUE_LINES = ModpackFestivalSister.FLEE_DIALOGUE_LINES or {
    "RUN. We are running. Keep up!",
    "There are too many — we need to go RIGHT NOW.",
    "I'm not dying here. Move!",
    "Okay that is way too many. We are LEAVING.",
    "Don't be a hero. Follow me or die alone!",
    "We can't fight all of them — MOVE YOUR FEET.",
    "I am running and you should seriously be doing the same!",
    "Nope. Hard no. Let's go!",
    "If you stay I will be very sad and also still running!",
    "This is the part where we don't die. Go go go!",
    "Fall back! Now! I am not asking!",
    "I did not come this far to get eaten — RUN!",
    "Too many. Way too many. We go. NOW.",
    "Leave them! There's too many — just RUN!",
}

-- Combat speech lines keyed by Bandits2 phrase type.
-- SPOTTED = sees an enemy | DEAD/DEATH = kills one | HIT = takes a hit
-- RELOADING = reloading weapon | BREACH = entering building | CAR = near vehicle
ModpackFestivalSister.COMBAT_DIALOGUE_LINES = ModpackFestivalSister.COMBAT_DIALOGUE_LINES or {
    SPOTTED = {
        "Got one.",
        "Heads up.",
        "There.",
        "On your left.",
        "Watch out!",
        "I see it.",
        "One incoming.",
        "Behind you!",
    },
    DEAD = {
        "Stay down.",
        "Done.",
        "Next.",
        "One less.",
        "Got it.",
        "Clear.",
        "Good.",
        "Down.",
    },
    DEATH = {
        "Stay down.",
        "Done.",
        "Next.",
        "One less.",
        "Got it.",
        "Clear.",
        "Good.",
        "Down.",
    },
    HIT = {
        "Hey!",
        "Ow.",
        "That hurt!",
        "Watch it!",
        "Not the face!",
        "I felt that.",
    },
    RELOADING = {
        "Reloading!",
        "Cover me!",
        "Out — one sec!",
        "Need a second!",
    },
    BREACH = {
        "Going in.",
        "Clearing.",
        "Move.",
        "Stay close.",
    },
    CAR = {
        "Vehicle!",
        "Car!",
        "Over there!",
    },
}

-- Kill milestone lines — one per milestone, spoken exactly once per session.
ModpackFestivalSister.MILESTONE_DIALOGUE_LINES = {
    [10]  = "Ten. Not that I'm counting.",
    [25]  = "Twenty-five. I'm definitely counting.",
    [50]  = "Fifty. We've gotten good at this.",
    [100] = "A hundred. Don't think about it too hard.",
}

-- Night-specific idle lines — used instead of generic pool after dark.
ModpackFestivalSister.NIGHT_DIALOGUE_LINES = {
    "Keep your eyes open. You can't see anything out here.",
    "It's worse at night. Everything's worse at night.",
    "Don't wander off. I'm serious.",
    "Something about the dark makes it feel like more of them.",
    "I hate this. I hate the night especially.",
    "Stay close. I mean it.",
    "I keep thinking I see things moving.",
    "The quiet is the worst part.",
}

-- Rain-specific idle lines.
ModpackFestivalSister.RAIN_DIALOGUE_LINES = {
    "I hate this. I hate all of this.",
    "It's raining. Of course it's raining.",
    "My socks are wet. My socks are wet and I might die today.",
    "You know what I miss? Being dry.",
    "The dead don't care about the rain. Lucky them.",
    "I feel like we're in a bad movie.",
    "At least it keeps them slow.",
}

-- Burst-hit lines — spoken when sister takes several hits in a short window.
ModpackFestivalSister.BURST_HIT_LINES = {
    "Okay that HURT.",
    "They're getting through — help!",
    "A little busy here!",
    "I need help over here!",
    "I'm getting swarmed!",
    "Too many — watch my back!",
}

function ModpackFestivalSister.pickCombatLine(phrase)
    local lines = ModpackFestivalSister.COMBAT_DIALOGUE_LINES
        and ModpackFestivalSister.COMBAT_DIALOGUE_LINES[phrase]
    if not lines or #lines == 0 then return nil end
    if ZombRand then return lines[ZombRand(#lines) + 1] end
    return lines[1]
end

function ModpackFestivalSister.getIdleDialogueLines()
    return ModpackFestivalSister.IDLE_DIALOGUE_LINES or {}
end

function ModpackFestivalSister.getFleeDialogueLines()
    return ModpackFestivalSister.FLEE_DIALOGUE_LINES or {}
end

function ModpackFestivalSister.pickIdleDialogueLine()
    local lines = ModpackFestivalSister.getIdleDialogueLines()
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

function ModpackFestivalSister.pickFleeDialogueLine()
    local lines = ModpackFestivalSister.getFleeDialogueLines()
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
