-- Copyright (c) 2022-2025 Oneline/D.Borovsky
-- All rights reserved
QuestLogger = {}
QuestLogger.verbose = false;
QuestLogger.mute = false;
QuestLogger.error = false;

function QuestLogger.print(text)
    if QuestLogger.verbose then
        if QuestLogger.mute then return end
        print(text);
    end
end

function QuestLogger.onStateChanged(result, id_1, id_2)
    if QuestLogger.verbose then
        if id_2 then
            print(string.format("[QSystem*] Task '%s' has been %s (quest=%s).", QuestManager.instance.quests[id_1].tasks[id_2].internal, result, QuestManager.instance.quests[id_1].internal));
        else
            print(string.format("[QSystem*] Quest '%s' has been %s.", QuestManager.instance.quests[id_1].internal, result));
        end
    end
end

function QuestLogger.report(message)
    addLineInChat(message, 0);
end