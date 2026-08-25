LCCQF = LCCQF or {}

local DialogueContent = LCCQF.DialogueContent or {}

local dialogues = {
    test_alexey = {
        start = "start",
        nodes = {
            start = {
                text = "Ты не похож на местного. Если пришёл поговорить — говори. Я пока никуда не спешу.",
                choices = {
                    { id = "ask_work", text = "Есть работа?", next = "work" },
                    { id = "ask_identity", text = "Кто ты?", next = "who" },
                    { id = "leave", text = "Уйти", close = true },
                },
            },
            work = {
                text = "Работа будет. Но сейчас это только проверка нашей системы разговора. Позже отсюда сервер сможет создать настоящее задание.",
                choices = {
                    { id = "back", text = "Понятно.", next = "start" },
                    { id = "leave", text = "Уйти", close = true },
                },
            },
            who = {
                text = "Зови меня Алексей. Для сервера я тестовый постоянный NPC, а для тебя — первый человек, с которым эта система умеет разговаривать.",
                choices = {
                    { id = "back", text = "Вернуться к разговору.", next = "start" },
                    { id = "leave", text = "Уйти", close = true },
                },
            },
        },
    },
}

function DialogueContent.Get(dialogueId)
    if type(dialogueId) ~= "string" then return nil end
    return dialogues[dialogueId]
end

LCCQF.DialogueContent = DialogueContent

return DialogueContent
