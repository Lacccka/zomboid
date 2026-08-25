# Разбор Workshop-модов из `изучить/P0-P2`

## Что сделано

- Статически разобраны все 35 Workshop-пакетов из трёх тиров.
- Для каждого пакета создан отдельный отчёт с CLIENT/SERVER/SHARED, network, persistence/identity, рисками, полезными механиками и точными путями.
- Создан сводный индекс с практической переоценкой тиров и порядком последующего runtime-аудита.
- Исходный код модов и `LaccckaQuestFramework` не изменялись.

## Файлы

- `docs/mod-research/README.md`
- `docs/mod-research/P0/*.md` — 13 отчётов
- `docs/mod-research/P1/*.md` — 13 отчётов
- `docs/mod-research/P2/*.md` — 9 отчётов

## Главные выводы

- `Interactive NPCs` фактически P0: ближайший reference для server-validated NPC dialogue flow.
- `QP Survivor Contracts` + `Bclan` + `Extraction Mode` вместе закрывают лучшие найденные паттерны group objectives, invite handshake и durable session lifecycle.
- `Share Map Notes` — лучший компактный сетевой protocol/marker reference.
- `Chat with Me` полезен для UI/proximity, но его `AwardItems`/`AwardGift` — критический DS anti-pattern.
- `True Companions`, `NPC&QUEST` и `Reactive Sound Events` практически повышены из P2 в P1.

## Проверки

- 35 отчётов, 35 уникальных Workshop ID, пропусков и лишних ID нет.
- Все относительные ссылки индекса разрешаются.
- Все указанные пути к исходникам существуют.
- Выполнен `git diff --check`.
- Runtime/DS-тесты не выполнялись: это отдельная следующая фаза.

## Ограничения

- Наличие server-файлов не считается доказательством безопасной authority: каждый handler оценивался отдельно, но полный adversarial runtime-аудит ещё нужен.
- При отсутствии явной лицензии исходники рассматриваются только как материал для изучения, не как разрешение на копирование.
