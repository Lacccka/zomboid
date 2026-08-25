require "LCCQF/LCCQFConstants"

LCCQF = LCCQF or {}
LCCQF.Dialogues = LCCQF.Dialogues or {}

LCCQF.Dialogues.test_alexey = {
    start = "start",
    nodes = {
        start = {
            text = "Ты не похож на местного. Если пришёл поговорить — говори. Я пока никуда не спешу.",
            choices = {
                { text = "Есть работа?", next = "work" },
                { text = "Кто ты?", next = "who" },
                { text = "Уйти", close = true },
            },
        },
        work = {
            text = "Работа будет. Но сейчас это только проверка нашей системы разговора. Позже отсюда сервер сможет создать настоящее задание.",
            choices = {
                { text = "Понятно.", next = "start" },
                { text = "Уйти", close = true },
            },
        },
        who = {
            text = "Зови меня Алексей. Для сервера я тестовый постоянный NPC, а для тебя — первый человек, с которым эта система умеет разговаривать.",
            choices = {
                { text = "Вернуться к разговору.", next = "start" },
                { text = "Уйти", close = true },
            },
        },
    },
}

function LCCQF.GetDialogue(dialogueId)
    return LCCQF.Dialogues[dialogueId]
end

return LCCQF.Dialogues
