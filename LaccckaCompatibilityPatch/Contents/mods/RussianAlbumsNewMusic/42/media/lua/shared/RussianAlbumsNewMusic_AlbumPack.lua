require "NMAlbumPackBuilder"

local function build(label, sound)
    return { label = label, sound = sound }
end

local albumPack = {
    module = "RussianAlbumsNewMusic",
    albums = {
        {
            module = "RussianAlbumsNewMusic",
            id = "ClassicDorogaVibe",
            title = "Classic Doroga Vibe",
            trackSource = {
                explicit = {
                    full = {
                        build("Vinyl 7B Molodye Vetra", "RANM_ClassicDorogaVibe_01"),
                        build("Vinyl Agas Chetyre Tatarina", "RANM_ClassicDorogaVibe_02"),
                        build("Vinyl Agas Aziya Evraziya", "RANM_ClassicDorogaVibe_03"),
                        build("Vinyl Agas Vorovskaya Dolya", "RANM_ClassicDorogaVibe_04"),
                        build("Vinyl Aleksandr Rozenbaum Izvozchik", "RANM_ClassicDorogaVibe_05"),
                        build("Vinyl Ariya Igra S Ognyem 1989", "RANM_ClassicDorogaVibe_06"),
                        build("Vinyl Ariya Torero 1988", "RANM_ClassicDorogaVibe_07"),
                        build("Vinyl Basta Rostov Don", "RANM_ClassicDorogaVibe_08"),
                        build("Vinyl Ariya Ulitsa Roz 1988", "RANM_ClassicDorogaVibe_09"),
                        build("Vinyl Bumer Moskva Magadan", "RANM_ClassicDorogaVibe_10"),
                        build("Vinyl Butyrka Devchonka S Tsentra", "RANM_ClassicDorogaVibe_11"),
                        build("Vinyl Chernikovskaya Hata Nazhmi Na Knopku", "RANM_ClassicDorogaVibe_12"),
                        build("Vinyl Butyrka Za Rostovskuyu Bratvu", "RANM_ClassicDorogaVibe_13"),
                        build("Vinyl Chernikovskaya Hata Stavlyu Na Zero", "RANM_ClassicDorogaVibe_14"),
                        build("Vinyl Chicherina Tu Lu La", "RANM_ClassicDorogaVibe_15"),
                        build("Vinyl Conec Solnetnihy Dnei Eudu Daleko", "RANM_ClassicDorogaVibe_16"),
                        build("Vinyl Ddt Prosvistela", "RANM_ClassicDorogaVibe_17"),
                        build("Vinyl Eolika Karavana 1985", "RANM_ClassicDorogaVibe_18"),
                        build("Vinyl Gde Fantom Relsi", "RANM_ClassicDorogaVibe_19"),
                        build("Vinyl Kipelov Ya Svoboden", "RANM_ClassicDorogaVibe_20"),
                        build("Vinyl Gruppa Butyrka A Dlya Vas Ya Nikto", "RANM_ClassicDorogaVibe_21"),
                        build("Vinyl Komissar Nashe Vremya Prishlo 1991", "RANM_ClassicDorogaVibe_22"),
                        build("Vinyl Kosmos Na Potolke Relsy", "RANM_ClassicDorogaVibe_23"),
                        build("Vinyl Leningrad Moskva", "RANM_ClassicDorogaVibe_24"),
                        build("Vinyl Krug Mihail Devochka Pay", "RANM_ClassicDorogaVibe_25"),
                        build("Vinyl Lyube Atas", "RANM_ClassicDorogaVibe_26"),
                        build("Vinyl Krug Mihail Kolshchik", "RANM_ClassicDorogaVibe_27"),
                        build("Vinyl Lyube Kombat 1992", "RANM_ClassicDorogaVibe_28"),
                        build("Vinyl Lyube Soldat", "RANM_ClassicDorogaVibe_29"),
                    },
                },
            },
            media = {
                cassette = {
                    mode = "full",
                    items = {
                        full = "CassetteAlbumClassicDorogaVibe",
                    },
                },
                vinyl = {
                    mode = "full",
                    items = {
                        full = "VinylAlbumClassicDorogaVibe",
                    },
                },
                cd = {
                    mode = "full",
                    items = {
                        full = "CDAlbumClassicDorogaVibe",
                    },
                },
            },
            coverGroups = {
                {
                    mode = "fallback",
                    texture = "RANM/Covers/classic_doroga_vibe",
                    includePlayable = { "cassette", "vinyl", "cd" },
                },
            },
        },
        {
            module = "RussianAlbumsNewMusic",
            id = "ClassicGoldWind",
            title = "Classic Gold Wind",
            trackSource = {
                explicit = {
                    full = {
                        build("Vinyl Abba Gimme", "RANM_ClassicGoldWind_01"),
                        build("Vinyl Aida Vedishcheva Pesenka O Medvedyah", "RANM_ClassicGoldWind_02"),
                        build("Vinyl Aleksey Lebedinskiy Za Chto Gerasim Utopil Mumu", "RANM_ClassicGoldWind_03"),
                        build("Vinyl Baccara I Can Boogie", "RANM_ClassicGoldWind_04"),
                        build("Vinyl Boney M Daddy Cool", "RANM_ClassicGoldWind_05"),
                        build("Vinyl Bravo Chernyy Kot", "RANM_ClassicGoldWind_06"),
                        build("Vinyl Bravo Lyubite Devushki", "RANM_ClassicGoldWind_07"),
                        build("Vinyl Chernikovskaya Hata Ty Ne Ver Slezam", "RANM_ClassicGoldWind_08"),
                        build("Vinyl Dschinghis Khan Moskau", "RANM_ClassicGoldWind_09"),
                        build("Vinyl Durnojj Vkus Svetomuzyka", "RANM_ClassicGoldWind_10"),
                        build("Vinyl E Rotic Max Dont Have Sex With Your Ex", "RANM_ClassicGoldWind_11"),
                        build("Vinyl Elektroklub Igrushka 1990", "RANM_ClassicGoldWind_12"),
                        build("Vinyl Forum Davaite Sosvinimsya 1987", "RANM_ClassicGoldWind_13"),
                        build("Vinyl Glukoza Yura", "RANM_ClassicGoldWind_14"),
                        build("Vinyl Gruppa Na Na Faina", "RANM_ClassicGoldWind_15"),
                        build("Vinyl Ice Mc Think About The Way", "RANM_ClassicGoldWind_16"),
                        build("Vinyl Igorek Podozhdem Tvoyu Mat", "RANM_ClassicGoldWind_17"),
                        build("Vinyl Kar Men Chio Chio San 1990", "RANM_ClassicGoldWind_18"),
                        build("Vinyl Kosmos Na Potolke Limonady", "RANM_ClassicGoldWind_19"),
                        build("Vinyl Kosmos Na Potolke Vecherinka", "RANM_ClassicGoldWind_20"),
                        build("Vinyl Larisa Mondrus Prosnis I Poy", "RANM_ClassicGoldWind_21"),
                        build("Vinyl Laura Braningan Selfcontrol", "RANM_ClassicGoldWind_22"),
                    },
                },
            },
            media = {
                cassette = {
                    mode = "full",
                    items = {
                        full = "CassetteAlbumClassicGoldWind",
                    },
                },
                vinyl = {
                    mode = "full",
                    items = {
                        full = "VinylAlbumClassicGoldWind",
                    },
                },
                cd = {
                    mode = "full",
                    items = {
                        full = "CDAlbumClassicGoldWind",
                    },
                },
            },
            coverGroups = {
                {
                    mode = "fallback",
                    texture = "RANM/Covers/classic_gold_wind",
                    includePlayable = { "cassette", "vinyl", "cd" },
                },
            },
        },
        {
            module = "RussianAlbumsNewMusic",
            id = "ClassicItWillBeGood",
            title = "Classic It Will Be Good",
            trackSource = {
                explicit = {
                    full = {
                        build("Vinyl Abba Sos", "RANM_ClassicItWillBeGood_01"),
                        build("Vinyl Apofeoz Toski Doroga", "RANM_ClassicItWillBeGood_02"),
                        build("Vinyl Apofeoz Toski Lift", "RANM_ClassicItWillBeGood_03"),
                        build("Vinyl Apoforez Toski Oblomki", "RANM_ClassicItWillBeGood_04"),
                        build("Vinyl Ariya Poteryannyy Ray", "RANM_ClassicItWillBeGood_05"),
                        build("Vinyl Buerak Grustno S Toboi", "RANM_ClassicItWillBeGood_06"),
                        build("Vinyl Buerak Porazhenie", "RANM_ClassicItWillBeGood_07"),
                        build("Vinyl Chernikovskaya Hata Spektakl Okonchen", "RANM_ClassicItWillBeGood_08"),
                        build("Vinyl Ddt Chto Takoe Osen", "RANM_ClassicItWillBeGood_09"),
                        build("Vinyl Durnojj Vkus Ne Ukhodi", "RANM_ClassicItWillBeGood_10"),
                        build("Vinyl Electropticy Vso Zakonchitsa", "RANM_ClassicItWillBeGood_11"),
                        build("Vinyl Elektroklub Posledniye Svidaniye 1989", "RANM_ClassicItWillBeGood_12"),
                        build("Vinyl Elektroklub Proshalnyi Den 1987", "RANM_ClassicItWillBeGood_13"),
                        build("Vinyl Gde Fantom Chuzhaya Zhizn", "RANM_ClassicItWillBeGood_14"),
                        build("Vinyl Hi Fi A Dozhd Na Oknah Risuet", "RANM_ClassicItWillBeGood_15"),
                        build("Vinyl Igor Kornelyuk Dozhdi", "RANM_ClassicItWillBeGood_16"),
                        build("Vinyl Igor Talkov Letnii Dozhd 1995", "RANM_ClassicItWillBeGood_17"),
                        build("Vinyl Komissar Ty Uidyesh 1991", "RANM_ClassicItWillBeGood_18"),
                        build("Zarqon - Мужчины", "RANM_ClassicItWillBeGood_19"),
                    },
                },
            },
            media = {
                cassette = {
                    mode = "full",
                    items = {
                        full = "CassetteAlbumClassicItWillBeGood",
                    },
                },
                vinyl = {
                    mode = "full",
                    items = {
                        full = "VinylAlbumClassicItWillBeGood",
                    },
                },
                cd = {
                    mode = "full",
                    items = {
                        full = "CDAlbumClassicItWillBeGood",
                    },
                },
            },
            coverGroups = {
                {
                    mode = "fallback",
                    texture = "RANM/Covers/classic_it_will_be_good",
                    includePlayable = { "cassette", "vinyl", "cd" },
                },
            },
        },
        {
            module = "RussianAlbumsNewMusic",
            id = "ClassicKino",
            title = "Classic Kino",
            trackSource = {
                explicit = {
                    full = {
                        build("Kino Bezdelnik", "RANM_ClassicKino_01"),
                        build("Kino Gruppa Krovi 1987", "RANM_ClassicKino_02"),
                        build("Kino Konchitsya Leto", "RANM_ClassicKino_03"),
                        build("Kino Mama Anarhiya", "RANM_ClassicKino_04"),
                        build("Kino Nam S Toboi 1990", "RANM_ClassicKino_05"),
                        build("Kino Sledi Za Soboy", "RANM_ClassicKino_06"),
                        build("Kino Spokoinya Notch", "RANM_ClassicKino_07"),
                        build("Kino Trolleybus", "RANM_ClassicKino_08"),
                        build("Kino Xochu Peremen 1989", "RANM_ClassicKino_09"),
                        build("Kino Zvezda Po Imeni Solnste 1988", "RANM_ClassicKino_10"),
                        build("Kino V Tsoy Gruppa Krovi", "RANM_ClassicKino_11"),
                        build("Kino V Tsoy Peremen", "RANM_ClassicKino_12"),
                    },
                },
            },
            media = {
                cassette = {
                    mode = "full",
                    items = {
                        full = "CassetteAlbumClassicKino",
                    },
                },
                vinyl = {
                    mode = "full",
                    items = {
                        full = "VinylAlbumClassicKino",
                    },
                },
                cd = {
                    mode = "full",
                    items = {
                        full = "CDAlbumClassicKino",
                    },
                },
            },
            coverGroups = {
                {
                    mode = "fallback",
                    texture = "RANM/Covers/classic_kino",
                    includePlayable = { "cassette", "vinyl", "cd" },
                },
            },
        },
        {
            module = "RussianAlbumsNewMusic",
            id = "ClassicLifeEnd",
            title = "Classic Life End",
            trackSource = {
                explicit = {
                    full = {
                        build("Vinyl Agas Romashka Belaya", "RANM_ClassicLifeEnd_01"),
                        build("Vinyl Anna German Nadezhda", "RANM_ClassicLifeEnd_02"),
                        build("Vinyl Akula Malo", "RANM_ClassicLifeEnd_03"),
                        build("Vinyl Ariel V Krayu Magnoliy", "RANM_ClassicLifeEnd_04"),
                        build("Vinyl Angliya Davai Poka", "RANM_ClassicLifeEnd_05"),
                        build("Vinyl Buerak Na Starykh Sideniyakh Kinoteatra", "RANM_ClassicLifeEnd_06"),
                        build("Vinyl Best Glukoza Nevesta", "RANM_ClassicLifeEnd_07"),
                        build("Vinyl Durnoy Vkus Starye Plastinki", "RANM_ClassicLifeEnd_08"),
                        build("Vinyl Bi 2 Schaste Moe Gde Ty", "RANM_ClassicLifeEnd_09"),
                        build("Vinyl Elektroklub Ty Pomnish Moskvu 1987", "RANM_ClassicLifeEnd_10"),
                        build("Vinyl Bravo Feat Zhanna Aguzarova Kak Byt Bonus Track", "RANM_ClassicLifeEnd_11"),
                        build("Vinyl Forum Ostrovok 1987", "RANM_ClassicLifeEnd_12"),
                        build("Vinyl Chay Vdvoem Den Rozhdenya", "RANM_ClassicLifeEnd_13"),
                        build("Vinyl Gde Fantom Starik", "RANM_ClassicLifeEnd_14"),
                        build("Vinyl Elektroklub Ne Begi 1988", "RANM_ClassicLifeEnd_15"),
                        build("Vinyl Igor Kornelyuk Gorod Kotorogo Net", "RANM_ClassicLifeEnd_16"),
                        build("Vinyl Elektroklub Polchasa 1990", "RANM_ClassicLifeEnd_17"),
                        build("Vinyl Igor Sklyar Komarovo", "RANM_ClassicLifeEnd_18"),
                        build("Vinyl Eolika Prostiye Slova 1982", "RANM_ClassicLifeEnd_19"),
                        build("Vinyl Igor Talkov Chistviye Prudy 1989", "RANM_ClassicLifeEnd_20"),
                        build("Vinyl Igor Talkov Ya Vernus 1991", "RANM_ClassicLifeEnd_21"),
                        build("Vinyl Irina Saltykova Serye Glaza", "RANM_ClassicLifeEnd_22"),
                        build("Vinyl Komissar Eti Glaza 1991", "RANM_ClassicLifeEnd_23"),
                        build("Vinyl Korotkaya Lyubov Kursk", "RANM_ClassicLifeEnd_24"),
                        build("Vinyl Lera Masskva 7 Etazh", "RANM_ClassicLifeEnd_25"),
                    },
                },
            },
            media = {
                cassette = {
                    mode = "full",
                    items = {
                        full = "CassetteAlbumClassicLifeEnd",
                    },
                },
                vinyl = {
                    mode = "full",
                    items = {
                        full = "VinylAlbumClassicLifeEnd",
                    },
                },
                cd = {
                    mode = "full",
                    items = {
                        full = "CDAlbumClassicLifeEnd",
                    },
                },
            },
            coverGroups = {
                {
                    mode = "fallback",
                    texture = "RANM/Covers/classic_life_end",
                    includePlayable = { "cassette", "vinyl", "cd" },
                },
            },
        },
        {
            module = "RussianAlbumsNewMusic",
            id = "ClassicMistyroad",
            title = "Classic Mistyroad",
            trackSource = {
                explicit = {
                    full = {
                        build("Vinyl 7B Nekreshchenaya Luna", "RANM_ClassicMistyroad_01"),
                        build("Vinyl Alliyance Na Zare 1984", "RANM_ClassicMistyroad_02"),
                        build("Vinyl Alyans Na Zare", "RANM_ClassicMistyroad_03"),
                        build("Vinyl Angliya Skazochnyi Mir", "RANM_ClassicMistyroad_04"),
                        build("Vinyl Bi 2 Polkovniku Nikto Ne Pishet", "RANM_ClassicMistyroad_05"),
                        build("Vinyl Bishkek Krysha", "RANM_ClassicMistyroad_06"),
                        build("Vinyl Brain Storm Veter", "RANM_ClassicMistyroad_07"),
                        build("Vinyl Buerak Puls Stuchit", "RANM_ClassicMistyroad_08"),
                        build("Vinyl Bumazhnye Tigry Henhina V Peskah", "RANM_ClassicMistyroad_09"),
                        build("Vinyl Chernikovskaya Hata Belaya Noch", "RANM_ClassicMistyroad_10"),
                        build("Vinyl Cultodinochestva Ya Ehoghu", "RANM_ClassicMistyroad_11"),
                        build("Vinyl Durnojj Vkus Navsegda", "RANM_ClassicMistyroad_12"),
                        build("Vinyl Electropticy Morskya", "RANM_ClassicMistyroad_13"),
                        build("Vinyl Eolika Nokitirne 1983", "RANM_ClassicMistyroad_14"),
                        build("Vinyl Forum Belaya Noch 1987", "RANM_ClassicMistyroad_15"),
                        build("Vinyl Kvartira Dhina Belya Vedma", "RANM_ClassicMistyroad_16"),
                        build("Vinyl Kvartira Dzhina Mnogojetazhki", "RANM_ClassicMistyroad_17"),
                    },
                },
            },
            media = {
                cassette = {
                    mode = "full",
                    items = {
                        full = "CassetteAlbumClassicMistyroad",
                    },
                },
                vinyl = {
                    mode = "full",
                    items = {
                        full = "VinylAlbumClassicMistyroad",
                    },
                },
                cd = {
                    mode = "full",
                    items = {
                        full = "CDAlbumClassicMistyroad",
                    },
                },
            },
            coverGroups = {
                {
                    mode = "fallback",
                    texture = "RANM/Covers/classic_mistyroad",
                    includePlayable = { "cassette", "vinyl", "cd" },
                },
            },
        },
        {
            module = "RussianAlbumsNewMusic",
            id = "ClassicPankRock",
            title = "Classic Pank Rock",
            trackSource = {
                explicit = {
                    full = {
                        build("Vinyl Agata Kristi Kak Na Voyne", "RANM_ClassicPankRock_01"),
                        build("Vinyl Bespokojjnik Snova", "RANM_ClassicPankRock_02"),
                        build("Vinyl Chenki Nenavist", "RANM_ClassicPankRock_03"),
                        build("Vinyl Conec Solnetchih Dney Deti Nemoi Strany", "RANM_ClassicPankRock_04"),
                        build("Vinyl Disciplina Bezbolnoi Bity Plenka", "RANM_ClassicPankRock_05"),
                        build("Vinyl Divergetia Mama Ya Ne Hochu V Armiu", "RANM_ClassicPankRock_06"),
                        build("Vinyl Egor Letov I Kommunizm Fantom", "RANM_ClassicPankRock_07"),
                        build("Vinyl Eho Pos Pank", "RANM_ClassicPankRock_08"),
                        build("Vinyl Grazhdanskaya Oborona Vse Idet Po Planu", "RANM_ClassicPankRock_09"),
                        build("Vinyl Leningrad Nikogo Ne Zhalko", "RANM_ClassicPankRock_10"),
                        build("Vinyl Leningrad Ryba", "RANM_ClassicPankRock_11"),
                        build("Нервы - А А А", "RANM_ClassicPankRock_12"),
                        build("Нервы - Бей Моё Сердце", "RANM_ClassicPankRock_13"),
                        build("Нервы - Муза", "RANM_ClassicPankRock_14"),
                        build("Нервы - Нервы", "RANM_ClassicPankRock_15"),
                    },
                },
            },
            media = {
                cassette = {
                    mode = "full",
                    items = {
                        full = "CassetteAlbumClassicPankRock",
                    },
                },
                vinyl = {
                    mode = "full",
                    items = {
                        full = "VinylAlbumClassicPankRock",
                    },
                },
                cd = {
                    mode = "full",
                    items = {
                        full = "CDAlbumClassicPankRock",
                    },
                },
            },
            coverGroups = {
                {
                    mode = "fallback",
                    texture = "RANM/Covers/classic_pank_rock",
                    includePlayable = { "cassette", "vinyl", "cd" },
                },
            },
        },
        {
            module = "RussianAlbumsNewMusic",
            id = "Deadinsidesoft",
            title = "Deadinsidesoft",
            trackSource = {
                explicit = {
                    full = {
                        build("‘ ♪ 𝐬𝐩𝐝𝐮𝐩𝐬𝐨𝐧𝐠 ♪ ‘ - Dxrk-Do Or Die (Speed Up)", "RANM_Deadinsidesoft_01"),
                        build("Atl - На Щите", "RANM_Deadinsidesoft_02"),
                        build("Big Baby Tape - Trap Luv", "RANM_Deadinsidesoft_03"),
                        build("Fem Love - Фотографирую Закат", "RANM_Deadinsidesoft_04"),
                        build("Kavinsky - Nightcall", "RANM_Deadinsidesoft_05"),
                        build("Lil Peep X Bones - Five Degrees, Cut (Mushup)", "RANM_Deadinsidesoft_06"),
                        build("Lizer - Пачка Сигарет", "RANM_Deadinsidesoft_07"),
                        build("Oliver Tree - Bounce", "RANM_Deadinsidesoft_08"),
                        build("Oliver Tree - Jerk (Almost Phonk Remix By Da Hata)", "RANM_Deadinsidesoft_09"),
                        build("Pharaoh - Мой Кайф", "RANM_Deadinsidesoft_10"),
                        build("Plenka - When You Find Me", "RANM_Deadinsidesoft_11"),
                        build("Pussykiller (Remix Tik Tok By Tep Edits) - Папа", "RANM_Deadinsidesoft_12"),
                        build("Shadowraze - Astral Step", "RANM_Deadinsidesoft_13"),
                        build("Soska 69 - Басы Долбят", "RANM_Deadinsidesoft_14"),
                        build("T A T U - Покажи Мне Любовь (Tik Tok Remix)", "RANM_Deadinsidesoft_15"),
                        build("Twinky - Беды С Башкой (Prod By Shvrpness)", "RANM_Deadinsidesoft_16"),
                        build("Twinky-Ja-Tebja-Obmanu", "RANM_Deadinsidesoft_17"),
                        build("Wenaro, Lxner - Лёд", "RANM_Deadinsidesoft_18"),
                        build("Willv - Forever", "RANM_Deadinsidesoft_19"),
                        build("Xxxtentacion - Sad!", "RANM_Deadinsidesoft_20"),
                        build("Zhanulka - Не Я", "RANM_Deadinsidesoft_21"),
                        build("Zhanulka - Ток", "RANM_Deadinsidesoft_22"),
                        build("Джизус - Ты Меня Не Ищи (Remix)", "RANM_Deadinsidesoft_23"),
                        build("Кишлак - 11 11", "RANM_Deadinsidesoft_24"),
                        build("Кишлак - Грязь", "RANM_Deadinsidesoft_25"),
                        build("Кишлак - Эмо", "RANM_Deadinsidesoft_26"),
                        build("Мёртвые Сны - Не Хочу Жить", "RANM_Deadinsidesoft_27"),
                        build("Неизвестен - Мы Умрем Где То Посреди Ночи Speed Up", "RANM_Deadinsidesoft_28"),
                        build("Семьсот Семь - Пустота", "RANM_Deadinsidesoft_29"),
                        build("Семьсот Семь, Кишлак - Внутри", "RANM_Deadinsidesoft_30"),
                        build("Юно - Hikikomori", "RANM_Deadinsidesoft_31"),
                    },
                },
            },
            media = {
                cassette = {
                    mode = "full",
                    items = {
                        full = "CassetteAlbumDeadinsidesoft",
                    },
                },
                vinyl = {
                    mode = "full",
                    items = {
                        full = "VinylAlbumDeadinsidesoft",
                    },
                },
                cd = {
                    mode = "full",
                    items = {
                        full = "CDAlbumDeadinsidesoft",
                    },
                },
            },
            coverGroups = {
                {
                    mode = "fallback",
                    texture = "RANM/Covers/deadinsidesoft",
                    includePlayable = { "cassette", "vinyl", "cd" },
                },
            },
        },
        {
            module = "RussianAlbumsNewMusic",
            id = "MaybeDora",
            title = "Maybe Dora",
            trackSource = {
                explicit = {
                    full = {
                        build("Dora-Bolshe", "RANM_MaybeDora_01"),
                        build("Dora-Byt-S-Tobojj", "RANM_MaybeDora_02"),
                        build("Dora-Doradura", "RANM_MaybeDora_03"),
                        build("Dora-Esli-Khochesh", "RANM_MaybeDora_04"),
                        build("Dora-Loverboy", "RANM_MaybeDora_05"),
                        build("Dora-Mjejjbi-Bjejjbi-Barbisajjz", "RANM_MaybeDora_06"),
                        build("Dora-Mjejjbi-Bjejjbi-Ne-Ispravljus (1)", "RANM_MaybeDora_07"),
                        build("Dora-Mjejjbi-Bjejjbi-Ne-Ispravljus", "RANM_MaybeDora_08"),
                        build("Dora-Mladshaja-Sestra", "RANM_MaybeDora_09"),
                        build("Dora-Stop-Slovo", "RANM_MaybeDora_10"),
                        build("Dora-Vtjurilas", "RANM_MaybeDora_11"),
                        build("Mejbi Bejbi - Zajka", "RANM_MaybeDora_12"),
                        build("Mejbi Bejbi Lsp - Klub", "RANM_MaybeDora_13"),
                        build("Mjejjbi-Bjejjbi-Akhegao", "RANM_MaybeDora_14"),
                        build("Mjejjbi-Bjejjbi-Bratik", "RANM_MaybeDora_15"),
                        build("Mjejjbi-Bjejjbi-Dakimakura", "RANM_MaybeDora_16"),
                        build("Mjejjbi-Bjejjbi-Feat -Quiizzzmeow-Bol", "RANM_MaybeDora_17"),
                        build("Mjejjbi-Bjejjbi-Intimki", "RANM_MaybeDora_18"),
                        build("Mjejjbi-Bjejjbi-Mjejjbiljend", "RANM_MaybeDora_19"),
                        build("Mjejjbi-Bjejjbi-Planeta-M", "RANM_MaybeDora_20"),
                        build("Mjejjbi-Bjejjbi-Pokhrjukajj", "RANM_MaybeDora_21"),
                        build("Mjejjbi-Bjejjbi-Superporosenok", "RANM_MaybeDora_22"),
                        build("Mjejjbi-Bjejjbi-Treepside-Vysshaja-Shkola-Fejj", "RANM_MaybeDora_23"),
                        build("Platina-Feat -Dora-San-Laran", "RANM_MaybeDora_24"),
                    },
                },
            },
            media = {
                cassette = {
                    mode = "full",
                    items = {
                        full = "CassetteAlbumMaybeDora",
                    },
                },
                vinyl = {
                    mode = "full",
                    items = {
                        full = "VinylAlbumMaybeDora",
                    },
                },
                cd = {
                    mode = "full",
                    items = {
                        full = "CDAlbumMaybeDora",
                    },
                },
            },
            coverGroups = {
                {
                    mode = "fallback",
                    texture = "RANM/Covers/maybe_dora",
                    includePlayable = { "cassette", "vinyl", "cd" },
                },
            },
        },
        {
            module = "RussianAlbumsNewMusic",
            id = "MixtapeByNekit",
            title = "Mixtape By Nekit",
            trackSource = {
                explicit = {
                    full = {
                        build("Anguish, Amb Vsh, Ily - Bubblegum Bitch (Slowed)", "RANM_MixtapeByNekit_01"),
                        build("Anime Kei - Fighting Spirit", "RANM_MixtapeByNekit_02"),
                        build("Anime Kei - Strong And Strike", "RANM_MixtapeByNekit_03"),
                        build("Artemas - I Like The Way You Kiss Me", "RANM_MixtapeByNekit_04"),
                        build("Bbno$ - Antidepressants", "RANM_MixtapeByNekit_05"),
                        build("Bbno$ - It Boy", "RANM_MixtapeByNekit_06"),
                        build("Bbno$ - Two", "RANM_MixtapeByNekit_07"),
                        build("Bbno$, Ironmouse - 1-800", "RANM_MixtapeByNekit_08"),
                        build("Bbno$, Lentra - Imma", "RANM_MixtapeByNekit_09"),
                        build("Cjbeards - Maestro", "RANM_MixtapeByNekit_10"),
                        build("Convolk - Cat Scratch", "RANM_MixtapeByNekit_11"),
                        build("Convolk - Lonewolf", "RANM_MixtapeByNekit_12"),
                        build("Damage - Black Catcher Phonk", "RANM_MixtapeByNekit_13"),
                        build("Danger Twins - Show Of Hands", "RANM_MixtapeByNekit_14"),
                        build("Dk, Alrt - Nerves", "RANM_MixtapeByNekit_15"),
                        build("Lida - Фото Со Звездой", "RANM_MixtapeByNekit_16"),
                        build("Lida - Я Люблю Бухать", "RANM_MixtapeByNekit_17"),
                        build("Lonely Blaze - Брось Меня", "RANM_MixtapeByNekit_18"),
                        build("Offl1 Nx - Misted", "RANM_MixtapeByNekit_19"),
                        build("Oliver Tree - Hurt", "RANM_MixtapeByNekit_20"),
                        build("Pyatno - Trash", "RANM_MixtapeByNekit_21"),
                        build("Rat Boy & Ibdy - Who's Ready For Tomorrow", "RANM_MixtapeByNekit_22"),
                        build("Soska 69 - Чёрная Машина", "RANM_MixtapeByNekit_23"),
                        build("Sunmi - Cynical", "RANM_MixtapeByNekit_24"),
                        build("The Weeknd - False Alarm", "RANM_MixtapeByNekit_25"),
                        build("Twinky-Dyshi", "RANM_MixtapeByNekit_26"),
                        build("Бредишь - В Ахуе", "RANM_MixtapeByNekit_27"),
                        build("Кишлак - Дорогу Молодым", "RANM_MixtapeByNekit_28"),
                        build("Кишлак - Самый Лучший День", "RANM_MixtapeByNekit_29"),
                        build("Обнимаю - Тачка", "RANM_MixtapeByNekit_30"),
                        build("Плм - Голод", "RANM_MixtapeByNekit_31"),
                        build("Плм - Чел Замолчи Пж", "RANM_MixtapeByNekit_32"),
                        build("Свит Шот - Зеркала", "RANM_MixtapeByNekit_33"),
                    },
                },
            },
            media = {
                cassette = {
                    mode = "full",
                    items = {
                        full = "CassetteAlbumMixtapeByNekit",
                    },
                },
                vinyl = {
                    mode = "full",
                    items = {
                        full = "VinylAlbumMixtapeByNekit",
                    },
                },
                cd = {
                    mode = "full",
                    items = {
                        full = "CDAlbumMixtapeByNekit",
                    },
                },
            },
            coverGroups = {
                {
                    mode = "fallback",
                    texture = "RANM/Covers/mixtape_by_nekit",
                    includePlayable = { "cassette", "vinyl", "cd" },
                },
            },
        },
        {
            module = "RussianAlbumsNewMusic",
            id = "MixtapeByRicardo",
            title = "Mixtape By Ricardo",
            trackSource = {
                explicit = {
                    full = {
                        build("Gachibasser Pharaoh Diko Naprimer Right Version Gachi Remix Gachibass", "RANM_MixtapeByRicardo_01"),
                        build("Gachimuchi Valim Na Gelike Gachi Remix", "RANM_MixtapeByRicardo_02"),
                        build("Gayazov Brother Malinovaya Lada Right Version Gachi Remix", "RANM_MixtapeByRicardo_03"),
                        build("Iowa Ulybajsya Gachi Remix", "RANM_MixtapeByRicardo_04"),
                        build("Korol I Shut Horoshij Pirat Mertvyj Pirat Right Version Gachi Remix", "RANM_MixtapeByRicardo_05"),
                        build("Razor Shot Yunost V Sapogah Gachi Version Gachimuchi Remiks", "RANM_MixtapeByRicardo_06"),
                        build("Ruki Vverh Dumala Right Version Gachi Remix", "RANM_MixtapeByRicardo_07"),
                        build("Slava Marlow Snova Ya Napivayus Right Version", "RANM_MixtapeByRicardo_08"),
                        build("Via Gra Pocelui Right Version Gachi Remix", "RANM_MixtapeByRicardo_09"),
                        build("Yurij Shatunov Belye Rozy Right Version Gachi Remix Gachi Shedevr Belye Rozy", "RANM_MixtapeByRicardo_10"),
                        build("Benny Benassi - Satisfaction (Right Version) Gachimuchi", "RANM_MixtapeByRicardo_11"),
                        build("Danzel - You Spin Me Round (Right Version♂)", "RANM_MixtapeByRicardo_12"),
                        build("Shadowraze - Astral Step【Right Version】♂ Gachi Remix", "RANM_MixtapeByRicardo_13"),
                        build("Tred Cat - Заплатите Ведьмаку 300$", "RANM_MixtapeByRicardo_14"),
                        build("Валентин Стрыкало - Наш Dungeon Master (Right Version) ♂ Gachi Remix", "RANM_MixtapeByRicardo_15"),
                        build("Клава Кока, Niletto - Краш (♂Right Version♂)", "RANM_MixtapeByRicardo_16"),
                    },
                },
            },
            media = {
                cassette = {
                    mode = "full",
                    items = {
                        full = "CassetteAlbumMixtapeByRicardo",
                    },
                },
                vinyl = {
                    mode = "full",
                    items = {
                        full = "VinylAlbumMixtapeByRicardo",
                    },
                },
                cd = {
                    mode = "full",
                    items = {
                        full = "CDAlbumMixtapeByRicardo",
                    },
                },
            },
            coverGroups = {
                {
                    mode = "fallback",
                    texture = "RANM/Covers/mixtape_by_ricardo",
                    includePlayable = { "cassette", "vinyl", "cd" },
                },
            },
        },
        {
            module = "RussianAlbumsNewMusic",
            id = "MixtapeByVan",
            title = "Mixtape By Van",
            trackSource = {
                explicit = {
                    full = {
                        build("Alla Pugacheva Kuda Uhodit Detstvo Gachi Version Gachimuchi Remiks", "RANM_MixtapeByVan_01"),
                        build("Anzhelika Varum Gorodok Right Version Gachi Remix Gachi Right Gachi Gachi Gorodok Right Ver", "RANM_MixtapeByVan_02"),
                        build("Artur Pirozhkov Zacepila Right Version Gachi Remix", "RANM_MixtapeByVan_03"),
                        build("Asiya Assiya Nu Che Ty Takoj No Homo Gachi Remix By Tredcat Right Version Nu Che Ty Takoj", "RANM_MixtapeByVan_04"),
                        build("Bi 2 Varvara Gachi Version Gachimuchi Remiks", "RANM_MixtapeByVan_05"),
                        build("Datezrealboi 25 17 Golova Chtoby Dumat Right Version Gachi Remix", "RANM_MixtapeByVan_06"),
                        build("Dead Blonde Malchik Na Devyatke Right Version Gachi Remix By Deltamix", "RANM_MixtapeByVan_07"),
                        build("Edikboi Rasa Pchelovod Gachi Version Ty Pchela Ya Boy Next Door Gachimuchi Remiks", "RANM_MixtapeByVan_08"),
                        build("Eldzhej Feduk Rozovoe Vino Right Version Gachi Remix By Rat Tv", "RANM_MixtapeByVan_09"),
                        build("Evgeniya Otradnaya Uhodi I Dver Zakroj Gachiremix Right Version", "RANM_MixtapeByVan_10"),
                        build("Gachi - Кто Пчёлок Уважает", "RANM_MixtapeByVan_11"),
                        build("Pyatakoff - Люблю Больших Люблю Попастых Мото Мото Right Version Gachi Remix", "RANM_MixtapeByVan_12"),
                        build("Uncle Flexxx - Camry 3 5 ♂【Right Version】♂ Gachi Remix", "RANM_MixtapeByVan_13"),
                        build("А4 - Kids (Right Version)", "RANM_MixtapeByVan_14"),
                        build("Верка Сердючка - Гуляночка (Right Version) G Man", "RANM_MixtapeByVan_15"),
                        build("Мэйби Бэйби - Ахегао (Right Version)", "RANM_MixtapeByVan_16"),
                    },
                },
            },
            media = {
                cassette = {
                    mode = "full",
                    items = {
                        full = "CassetteAlbumMixtapeByVan",
                    },
                },
                vinyl = {
                    mode = "full",
                    items = {
                        full = "VinylAlbumMixtapeByVan",
                    },
                },
                cd = {
                    mode = "full",
                    items = {
                        full = "CDAlbumMixtapeByVan",
                    },
                },
            },
            coverGroups = {
                {
                    mode = "fallback",
                    texture = "RANM/Covers/mixtape_by_van",
                    includePlayable = { "cassette", "vinyl", "cd" },
                },
            },
        },
        {
            module = "RussianAlbumsNewMusic",
            id = "MixtapeToBilly",
            title = "Mixtape To Billy",
            trackSource = {
                explicit = {
                    full = {
                        build("(Gachi Remix) Big Baby Tape - Gimme The Ass ♂", "RANM_MixtapeToBilly_01"),
                        build("♂Семён Слепаков♂ - ♂Каждую Пятницу♂ (Gachi)", "RANM_MixtapeToBilly_02"),
                        build("Korol I Shut Motocikl Right Version Gachi Remix", "RANM_MixtapeToBilly_03"),
                        build("Linkin Park Numb Right Version Gachi Remix", "RANM_MixtapeToBilly_04"),
                        build("Maks Korzh Zhit V Kajf Gachi Version", "RANM_MixtapeToBilly_05"),
                        build("Mihail Shufutinskij 3 Sentyabrya No Shufutinskij Dumaet Chto On Ven Darkholm Right Version Gachi Re", "RANM_MixtapeToBilly_06"),
                        build("Neizvesten Dima Bilan Molniya Gachi Version Gachimuchi Remiks", "RANM_MixtapeToBilly_07"),
                        build("Niletto Lyubimka Right Version", "RANM_MixtapeToBilly_08"),
                        build("Papiny Dochki Gachi Version Gachimuchi Remiks", "RANM_MixtapeToBilly_09"),
                        build("Shoshon Elegant Dora Poshlyu Ego Na Gachi Remix", "RANM_MixtapeToBilly_10"),
                        build("V Yacheslav Kukoba Kabanchik Right Version Gachi Remix", "RANM_MixtapeToBilly_11"),
                        build("Vintazh Roman Right Version Gachi Remix", "RANM_MixtapeToBilly_12"),
                        build("Gachimuchi - Бутылочка", "RANM_MixtapeToBilly_13"),
                        build("Kl - Фотографирую Закат - Gachi Remix", "RANM_MixtapeToBilly_14"),
                        build("Евгения Отрадная - Уходи И Дверь Закрой (Right Version)", "RANM_MixtapeToBilly_15"),
                        build("Нурминский - Black Guard (Right Version♂) Gachi Remix", "RANM_MixtapeToBilly_16"),
                    },
                },
            },
            media = {
                cassette = {
                    mode = "full",
                    items = {
                        full = "CassetteAlbumMixtapeToBilly",
                    },
                },
                vinyl = {
                    mode = "full",
                    items = {
                        full = "VinylAlbumMixtapeToBilly",
                    },
                },
                cd = {
                    mode = "full",
                    items = {
                        full = "CDAlbumMixtapeToBilly",
                    },
                },
            },
            coverGroups = {
                {
                    mode = "fallback",
                    texture = "RANM/Covers/mixtape_to_billy",
                    includePlayable = { "cassette", "vinyl", "cd" },
                },
            },
        },
        {
            module = "RussianAlbumsNewMusic",
            id = "PeregaSirat",
            title = "Perega Sirat",
            trackSource = {
                explicit = {
                    full = {
                        build("Lida-Feat -Serega-Pirat-Chsv", "RANM_PeregaSirat_01"),
                        build("Mzlff-Feat -Serega-Pirat-Ja-Ne-Bojus-Oshibatsja", "RANM_PeregaSirat_02"),
                        build("Serega-Pirat-Apelsin", "RANM_PeregaSirat_03"),
                        build("Serega-Pirat-Dengimenjajut", "RANM_PeregaSirat_04"),
                        build("Serega-Pirat-Feat -Barikader-Kak-Zhe-On-Silen", "RANM_PeregaSirat_05"),
                        build("Serega-Pirat-Feat -Qeqoqeq-Zombi-Apokalipsis", "RANM_PeregaSirat_06"),
                        build("Serega-Pirat-Gimn-Dakhaka", "RANM_PeregaSirat_07"),
                        build("Serega-Pirat-Izvini-Segodnja-Prazdnik", "RANM_PeregaSirat_08"),
                        build("Serega-Pirat-Ja-Podnimaju-Svoju-Golovu-Vverkh", "RANM_PeregaSirat_09"),
                        build("Serega-Pirat-Ja-Vzletaju-Vverkh", "RANM_PeregaSirat_10"),
                        build("Serega-Pirat-Kachalka", "RANM_PeregaSirat_11"),
                        build("Serega-Pirat-Khejjtery", "RANM_PeregaSirat_12"),
                        build("Serega-Pirat-Masha", "RANM_PeregaSirat_13"),
                        build("Serega-Pirat-Mojj-Bajjk", "RANM_PeregaSirat_14"),
                        build("Serega-Pirat-Mojj-Topor", "RANM_PeregaSirat_15"),
                        build("Serega-Pirat-Natalija", "RANM_PeregaSirat_16"),
                        build("Serega-Pirat-Nu-I-Chto-Chto-Ja-Vor", "RANM_PeregaSirat_17"),
                        build("Serega-Pirat-Pochemu-Ty-Eshhe-Ne-Fanat", "RANM_PeregaSirat_18"),
                        build("Serega-Pirat-Rassel-Krou", "RANM_PeregaSirat_19"),
                        build("Serega-Pirat-Shizoid", "RANM_PeregaSirat_20"),
                        build("Serega-Pirat-Tp-Na-Ame", "RANM_PeregaSirat_21"),
                        build("Serega-Pirat-Vajjbmen", "RANM_PeregaSirat_22"),
                        build("Serega-Pirat-V-Jetojj-Trave", "RANM_PeregaSirat_23"),
                    },
                },
            },
            media = {
                cassette = {
                    mode = "full",
                    items = {
                        full = "CassetteAlbumPeregaSirat",
                    },
                },
                vinyl = {
                    mode = "full",
                    items = {
                        full = "VinylAlbumPeregaSirat",
                    },
                },
                cd = {
                    mode = "full",
                    items = {
                        full = "CDAlbumPeregaSirat",
                    },
                },
            },
            coverGroups = {
                {
                    mode = "fallback",
                    texture = "RANM/Covers/perega_sirat",
                    includePlayable = { "cassette", "vinyl", "cd" },
                },
            },
        },
        {
            module = "RussianAlbumsNewMusic",
            id = "PhonkVer1",
            title = "Phonk Ver1",
            trackSource = {
                explicit = {
                    full = {
                        build("$Mokez, Towa, D1s6ix - Evil Clique", "RANM_PhonkVer1_01"),
                        build("42 Path - Backpack", "RANM_PhonkVer1_02"),
                        build("42 Path - Freddie Race", "RANM_PhonkVer1_03"),
                        build("Ace $Nows X Kyanite X Styx - Hated Soulz", "RANM_PhonkVer1_04"),
                        build("Da Menace X Dj Akoza - Price On Your Head", "RANM_PhonkVer1_05"),
                        build("Dead Welder - Shit On Is", "RANM_PhonkVer1_06"),
                        build("Dragonmane - Buck Playa Buck", "RANM_PhonkVer1_07"),
                        build("Evil - Prend Une Sip", "RANM_PhonkVer1_08"),
                        build("Extemple - Chorus", "RANM_PhonkVer1_09"),
                        build("Hpshawty - I Need That Cap", "RANM_PhonkVer1_10"),
                        build("Junior Ferrari - Evil Evil", "RANM_PhonkVer1_11"),
                        build("Ke Playa - Downstairs", "RANM_PhonkVer1_12"),
                        build("Lil Kaine, Kingpin Skinny Pimp, Lil Sko - Came 2 Rob", "RANM_PhonkVer1_13"),
                        build("Nissan Playa - Evil Inside", "RANM_PhonkVer1_14"),
                        build("Nissan Playa - Tennessee", "RANM_PhonkVer1_15"),
                        build("Nissan Playa - Tokyo", "RANM_PhonkVer1_16"),
                        build("Nissan Playa - Twin Turbo", "RANM_PhonkVer1_17"),
                        build("Nissan Playa - Useless", "RANM_PhonkVer1_18"),
                        build("Rxdxvil - Steelman", "RANM_PhonkVer1_19"),
                        build("Ryznbeatz - Sxreen!", "RANM_PhonkVer1_20"),
                        build("Scriptz, Jon T - Black Pearl Paint", "RANM_PhonkVer1_21"),
                        build("Shinigami Tenshi - Paranoid Streets", "RANM_PhonkVer1_22"),
                        build("Towa, Token - Most Wanted", "RANM_PhonkVer1_23"),
                        build("Valode R (Feat Smxkydog) - Laid Back", "RANM_PhonkVer1_24"),
                        build("Whiteye$ - Runnin Up [ Umbasa X Cxxlion ]", "RANM_PhonkVer1_25"),
                    },
                },
            },
            media = {
                cassette = {
                    mode = "full",
                    items = {
                        full = "CassetteAlbumPhonkVer1",
                    },
                },
                vinyl = {
                    mode = "full",
                    items = {
                        full = "VinylAlbumPhonkVer1",
                    },
                },
                cd = {
                    mode = "full",
                    items = {
                        full = "CDAlbumPhonkVer1",
                    },
                },
            },
            coverGroups = {
                {
                    mode = "fallback",
                    texture = "RANM/Covers/phonk_ver1",
                    includePlayable = { "cassette", "vinyl", "cd" },
                },
            },
        },
        {
            module = "RussianAlbumsNewMusic",
            id = "PhonkVer2",
            title = "Phonk Ver2",
            trackSource = {
                explicit = {
                    full = {
                        build("!Yunx - How Much She Love Me", "RANM_PhonkVer2_01"),
                        build("Archez - Berserk Moves", "RANM_PhonkVer2_02"),
                        build("Crypt1 K - Necrotic", "RANM_PhonkVer2_03"),
                        build("Crypt1 K - Reanimate", "RANM_PhonkVer2_04"),
                        build("Dj Playastation X Londy - Murderer", "RANM_PhonkVer2_05"),
                        build("Fi Shbonez - Dont Sleep (Prod Bxgdvn)", "RANM_PhonkVer2_06"),
                        build("Forgottenage - Warfare", "RANM_PhonkVer2_07"),
                        build("Hageshi Jigoku - Moonlight Illuminates", "RANM_PhonkVer2_08"),
                        build("Hayabusa Mitsubishi - 4 All", "RANM_PhonkVer2_09"),
                        build("Hayabusa Mitsubishi - Septum", "RANM_PhonkVer2_10"),
                        build("Hayabusa Mitsubishi - Spirit 2", "RANM_PhonkVer2_11"),
                        build("Kreiiin, Wheezzys - Hell Race", "RANM_PhonkVer2_12"),
                        build("Kxtsu - Crusaders", "RANM_PhonkVer2_13"),
                        build("Ldrr, Las Canciones De Alfredo Larin - Cuando Se Te Moja La Tarea", "RANM_PhonkVer2_14"),
                        build("Mista Playa - Armor Breaking", "RANM_PhonkVer2_15"),
                        build("Mista Playa - Sakura Illusion", "RANM_PhonkVer2_16"),
                        build("Prxsxnt Fxture - Helltaker", "RANM_PhonkVer2_17"),
                        build("Prxsxnt Fxture - House", "RANM_PhonkVer2_18"),
                        build("Relyct, Arkea - A-What (Ultra Slowed)", "RANM_PhonkVer2_19"),
                        build("Shinki21, Green Orxnge - Typical Chill Song", "RANM_PhonkVer2_20"),
                        build("Slim Guerilla - Outlaw (Prod Genshin)", "RANM_PhonkVer2_21"),
                        build("Tadakatsu - Descent On Irohazaka", "RANM_PhonkVer2_22"),
                        build("Trxshrelvx - Madness", "RANM_PhonkVer2_23"),
                        build("Trxshrelvx, Ghxstblvde - Dance Dance", "RANM_PhonkVer2_24"),
                        build("Umbasa - Shit Storm", "RANM_PhonkVer2_25"),
                    },
                },
            },
            media = {
                cassette = {
                    mode = "full",
                    items = {
                        full = "CassetteAlbumPhonkVer2",
                    },
                },
                vinyl = {
                    mode = "full",
                    items = {
                        full = "VinylAlbumPhonkVer2",
                    },
                },
                cd = {
                    mode = "full",
                    items = {
                        full = "CDAlbumPhonkVer2",
                    },
                },
            },
            coverGroups = {
                {
                    mode = "fallback",
                    texture = "RANM/Covers/phonk_ver2",
                    includePlayable = { "cassette", "vinyl", "cd" },
                },
            },
        },
        {
            module = "RussianAlbumsNewMusic",
            id = "RedzedBohemianPsycho",
            title = "Redzed Bohemian Psycho",
            trackSource = {
                explicit = {
                    full = {
                        build("Redzed - Dead Bodies Everywhere", "RANM_RedzedBohemianPsycho_01"),
                        build("Redzed - Antichrist", "RANM_RedzedBohemianPsycho_02"),
                        build("Redzed - Sinister", "RANM_RedzedBohemianPsycho_03"),
                        build("Redzed - Bleed For Me Drumin' Hard", "RANM_RedzedBohemianPsycho_04"),
                        build("Redzed - Eyes Wide Open", "RANM_RedzedBohemianPsycho_05"),
                        build("Redzed - Straight Outta Flames", "RANM_RedzedBohemianPsycho_06"),
                        build("Redzed - Sippin' Blood", "RANM_RedzedBohemianPsycho_07"),
                        build("Redzed - Rave In The Grave", "RANM_RedzedBohemianPsycho_08"),
                        build("Redzed - Doom", "RANM_RedzedBohemianPsycho_09"),
                        build("Redzed - Chopper Swing", "RANM_RedzedBohemianPsycho_10"),
                        build("Redzed - Blind", "RANM_RedzedBohemianPsycho_11"),
                        build("Redzed Feat Gizmo - Ghoul", "RANM_RedzedBohemianPsycho_12"),
                    },
                },
            },
            media = {
                cassette = {
                    mode = "full",
                    items = {
                        full = "CassetteAlbumRedzedBohemianPsycho",
                    },
                },
                vinyl = {
                    mode = "full",
                    items = {
                        full = "VinylAlbumRedzedBohemianPsycho",
                    },
                },
                cd = {
                    mode = "full",
                    items = {
                        full = "CDAlbumRedzedBohemianPsycho",
                    },
                },
            },
            coverGroups = {
                {
                    mode = "fallback",
                    texture = "RANM/Covers/redzed_bohemian_psycho",
                    includePlayable = { "cassette", "vinyl", "cd" },
                },
            },
        },
        {
            module = "RussianAlbumsNewMusic",
            id = "Sqwozbab",
            title = "Sqwozbab",
            trackSource = {
                explicit = {
                    full = {
                        build("Sqwoz Bab - Кусь", "RANM_Sqwozbab_01"),
                        build("Sqwoz Bab - Сбербанк", "RANM_Sqwozbab_02"),
                        build("Sqwoz Bab - Kia Rio", "RANM_Sqwozbab_03"),
                        build("Sqwoz Bab, Джарахов - Booty", "RANM_Sqwozbab_04"),
                        build("Sqwoz Bab, Aum Raa - Qlitorock", "RANM_Sqwozbab_05"),
                        build("Sqwoz Bab - Арбуз", "RANM_Sqwozbab_06"),
                        build("Sqwoz Bab - Ограбь Мои Яйца", "RANM_Sqwozbab_07"),
                        build("Sqwoz Bab - Не Бабушка", "RANM_Sqwozbab_08"),
                        build("Sqwoz Bab - Ой", "RANM_Sqwozbab_09"),
                        build("Sqwoz Bab - Thai Trap", "RANM_Sqwozbab_10"),
                        build("Sqwoz Bab, Roulanges, Ролан - Guap", "RANM_Sqwozbab_11"),
                        build("Sqwoz Bab - Бойскаут", "RANM_Sqwozbab_12"),
                        build("Sqwoz Bab, Дети Rave - Zidane", "RANM_Sqwozbab_13"),
                        build("Sqwoz Bab - Адлер", "RANM_Sqwozbab_14"),
                        build("Sqwoz Bab, Aum Raa - Мезим", "RANM_Sqwozbab_15"),
                        build("Sqwoz Bab - Ебеня", "RANM_Sqwozbab_16"),
                        build("Sqwoz Bab - Озеро В Лесу", "RANM_Sqwozbab_17"),
                        build("Sqwoz Bab - Рахат Лукум", "RANM_Sqwozbab_18"),
                        build("Sqwoz Bab - Татарский Богатырь", "RANM_Sqwozbab_19"),
                        build("Sqwoz Bab, Хлеб - Бывший", "RANM_Sqwozbab_20"),
                        build("Ролан, Sqwoz Bab - Ламба", "RANM_Sqwozbab_21"),
                    },
                },
            },
            media = {
                cassette = {
                    mode = "full",
                    items = {
                        full = "CassetteAlbumSqwozbab",
                    },
                },
                vinyl = {
                    mode = "full",
                    items = {
                        full = "VinylAlbumSqwozbab",
                    },
                },
                cd = {
                    mode = "full",
                    items = {
                        full = "CDAlbumSqwozbab",
                    },
                },
            },
            coverGroups = {
                {
                    mode = "fallback",
                    texture = "RANM/Covers/sqwozbab",
                    includePlayable = { "cassette", "vinyl", "cd" },
                },
            },
        },
    },
}

NMAlbumPackBuilder.registerAlbumPack(albumPack)
