--------------------------------------------------------------------------------------------------------------------------------------------------
--[[

Ceci est un guide de base pour le fichier principal que vous devez ajouter à votre mod,ce que fait chaque type de table et des informations sur la plupart de leurs valeurs.
	
Je vais essayer de l’améliorer au fur et à mesure que je reçois des commentaires des moddeurs.
	
                  --------------------------------------------------------------------
	
Votre fichier doit être placé dans media/lua/client.
Maintenant, commençons.
]]--

--C’est la première ligne dont vous avez besoin :
require 'SFQuest_Database'
                     
--------------------------------------------------------------------------------------------------------------------------------------------------
--[[ SECTION 1 : QUÊTES

Avant qu’une quête puisse être donnée à un personnage, elle doit être créée et inséré dans le système. Voyons comment cela se fait et ce que les valeurs signifient.

Une quête sera une table locale qui inclut plusieurs valeurs :

local yourTable = { guid = « examplequest », 
completesound = « levelup », 
lore = {"IGUI_QuestLore_examplequest"}, 
needsitem = « Base.Nails;5 », 
unlockedsound = « QuestUnlocked », 
text = « IGUI_Quest_examplequest », 
texture = « Item_Nails », 
title = « IGUI_QuestTitle_examplequest », 
awardsitem = « Money;20 », 
awardsrep = « TestFaction;100 » }
	
Voici une liste des valeurs qui vont à l’intérieur de la table de quête et ce qu’elles font.
Ils peuvent être ajoutés à la table dans n’importe quel ordre, j’ajoute généralement guid en premier et ensuite, essayez de les lister par ordre alphabétique.	

awardsitem = (FACULTATIF) Un ou plusieurs types d’objets qui seront donnés au joueur lorsque la quête sera terminée, suivis de la quantité et séparés par un point-virgule.Ceux-ci sont affichés dans le dialogue d’un PNJ pour accepter la quête (Voir plus à ce sujet dans la section dialogue). Il accepte un article « Base.Burger », un 	article et une quantité « Base.Burger;2 » ou plusieurs articles et leurs quantités « Base.Fries;2; Base.Burger;1 ».

awardslore = (FACULTATIF) La quête recevra une page de texte supplémentaire dans son interface utilisateur lor, elle accepte une chaîne contenant une entrée de traduction.Si la quête n’avait pas de pages de texte et n’a pas pu être cliquée, elle sera cliquable lorsque cette nouvelle page sera ajoutée.

awardsrep = (FACULTATIF) Points de réputation qui seront attribués lorsque la quête sera terminée. Accepte le code de faction (voir la section faction) plus le montant de la réputation, par exemple : « TestFaction;50 ».

awardstask = (FACULTATIF) Déverrouille une autre quête Lorsque cette quête est terminée, il accepte une chaîne qui doit être un guid de quête valide et unique (voir GUID dans cette liste).

awardsworld = (FACULTATIF) Déverrouille un dialogue PNJ lorsque cette quête est terminée, Il accepte une chaîne qui doit être un code de dialogue valide et unique (voir la section dialogue).

completesound = (FACULTATIF) Liens vers un ID sonore qui sera joué lorsque la quête sera terminée, j’utilise l’ancien son de niveau supérieur « levelup » pour mes quêtes.

dailycode = (FACULTATIF) Chaîne utilisée pour identifier les quêtes du même pool quotidien. Lorsqu’il est temps de relancer et de donner une nouvelle quête à partir d’un pool, le système vérifie si le personnage en a déjà une.

guid = (OBLIGATOIRE) Il s’agit d’une chaîne unique utilisée pour identifier votre quête parmi toutes les quêtes ajoutées par tous les mods, alors rendez-la vraiment unique comme « YourModName_UniqueQuestName ».

lore = (FACULTATIF) Il s’agit d’un tableau avec des entrées de traduction, ces entrées doivent contenir du texte que les joueurs peuvent lire s’ils cliquent sur la quête, si aucune table de lore n’est incluse, cliquer sur la quête n’ouvrira pas l’interface utilisateur de la tradition.

needsitem = (FACULTATIF) L’ajout de cela signifie que votre quête a besoin d’un ou plusieurs éléments pour progresser, il peut s’agir d’un ID d’élément ou d’une balise d’élément, suivi de la quantité et séparé par un point-virgule. Exemples : « Base.Nails;5 », ou l’une des options de balise :

	Il existe quelques options lors de la vérification des balises :
							Tag# accepte tout élément qui a la balise. Exemple : « tag#Egg;12 ».
							TagPredicateBigFish# accepte le poisson frais au-delà d’une certaine longueur.
							TagPredicateCondition# accepte les armes au-delà d’une certaine condition.
							TagPredicateFreshFood# accepte les aliments qui sont actuellement frais.
							TagPredicateFullDrainable# il n’acceptera que les articles drainables complets.	
							
objectifs = (FACULTATIF) Tableau contenant un tableau pour chaque objectif. Les objectifs affichent leur propre texte et peuvent avoir leur propre état de progression     	distinct. Il est généralement nécessaire de remplir tous les objectifs avant de pouvoir terminer une quête, mais pas nécessairement dans l’ordre dans lequel ils sont présentés. Exemple : Une quête peut nécessiter de parler à 4 autres PNJ.
ondone = (FACULTATIF) Une chaîne contenant une liste de commandes qui seront exécutées lorsque la quête ou l’objectif est marqué comme terminé (voir les section 	commandes )	
onobtained = (FACULTATIF) Chaîne contenant une liste de commandes qui seront exécutées lorsque la quête ou l’objectif sera marqué comme obtenu (voir la section commandes).
text = (OBLIGATOIRE) Le texte qui sera affiché dans la liste des quêtes doit être court et descriptif.	
texture = (FACULTATIF) L’icône qui sera affichée dans la liste des quêtes. Si vous utilisez une icône d’objet du jeu, vous devez ajouter le préfixe « Item_ », s’il s’agit 	d’une icône modifiée, le chemin complet et le fichier doivent être activés. Exemple : « Item_Nails », « médias/textures/Item_MyOwnIcon.png ».
title = (FACULTATIF) Entrée de traduction dont le titre s’affiche dans l’interface utilisateur de l’histoire. Exemple : « IGUI_QuestTitle_examplequest ».
unlockedsound = (FACULTATIF) Liens vers un identifiant sonore qui sera joué lorsque la quête est donnée à un personnage, je fournis un nouveau son « QuestUnlocked » 	que j’utilise pour mes quêtes.
updates = (FACULTATIF) Cela signifie que cette quête est en fait une mise à jour pour une autre quête active et ne sera pas ajoutée à la liste active. Il doit s’agir d’un 	guide de quête valide et unique et cette quête doit figurer dans la liste des quêtes actives au moment du déblocage de cette mise à jour. La plupart des valeurs répertoriées ici peuvent être utilisées pour changer cette quête du personnage. Exemples : texture, texte, titre.

	--------------------------------------------------------------------	

	
	Voici la ligne qui insère une nouvelle quête dans le système :
	table.insert(SFQuest_Database.QuestPool, votreTable);
	
Ou vous pouvez le faire en une seule ligne comme ceci:
table.insert(SFQuest_Database.QuestPool, { guid = « examplequest », completesound = « levelup », lore = {"IGUI_QuestLore_examplequest"}, needsitem = « Base.Nails;5 », unlockedsound = « QuestUnlocked », text = « IGUI_Quest_examplequest », texture = « Item_Nails », title = « IGUI_QuestTitle_examplequest », awardsitem = « Money;20 », awardsrep = « TestFaction;100 » });
	
    --------------------------------------------------------------------
SECTION 1.1 : OBJECTIFS
	
hidden = (FACULTATIF) Si la valeur true est attribuée, l’objectif n’est pas affiché dans l’onglet de quête. Il fonctionne toujours comme n’importe quel autre objectif et 	peut être affiché s’il est mis à jour pour supprimer la valeur masquée.
	oncompleted = (FACULTATIF) Chaîne de commandes qui s’exécute lorsque cet objectif est marqué comme terminé (voir la section commandes).
--------------------------------------------------------------------------------------
 SECTION 2 : COMMANDES

Les quêtes, les minuteries ou d’autres mécanismes de ce système peuvent s’exécuter. Une variété de commandes, celles-ci sont incluses dans les chaînes suivies par leurs paramètres le cas échéant. Une seule chaîne peut contenir plusieurs commandes qui seront toutes exécutées en même temps.
	
Voici la liste des commandes et les paramètres dont elles ont besoin :

actionevent - Ajoute une condition spécifique qui sera vérifiée toutes les 10 minutes, lorsque la condition est remplie, une liste de commandes sera donnée. Paramètres : 	Condition et liste des commandes.
	Conditions possibles : « killzombies », suivi de la quantité, séparée par : « killzombies:25 ».
	Commandes : Toutes les commandes et leurs paramètres, mais avec : au lieu de ;
addmannequin - Ajoute un mannequin au monde. Gardez à l’esprit que le mannequin doit avoir été inséré dans la liste des mannequins pour que cela fonctionne. Paramètre : Une 	balise carrée (chaîne composée des x, y et z du carré).
additem - Ajoute un objet à l’inventaire du joueur. Paramètres : ID de l’article suivi de la quantité.
clickevent - Ajoute un événement de clic droit à un carré spécifique (voir la section événement de clic droit). Paramètres : Un « squaretag » et une chaîne unique pour 	l’événement click séparés par : (Exemple : « 12000x7500x0:MyUniqueClickEvent », des valeurs pour l’action chronométrée séparées par : (exemple : « time:50:anim:loot »), et une liste de commandes qui s’exécuteront lorsque l’action est exécutée. Chaque paramètre séparé par un point-virgule ;
	completequest - Termine une quête. Paramètres : Un guide de quête.
	entrée.
lore - Ajoute une nouvelle entrée lore à une quête. Paramètres : guide de quête suivi d’une entrée de traduction.
	playersay - Affiche une ligne de texte au-dessus de la tête du personnage, ne crée pas de son réel pour attirer les zombies. Paramètres : saisie de traduction.
	randomcodedworldfrompool - Sélectionne un événement/dialogue mondial aléatoire dans un pool et exécute unlockworldevent. Paramètres : Un dailycode (voir la section quêtes), le nom de la table externe, le nom de la table interne.
removeclickevent - Supprime l’événement click du caractère. Paramètre : nom unique d’un événement click (voir la section événements du clic droit).
removemannequin - Supprime un mannequin du monde.
revealobjective - Révèle un objectif qui est actuellement masqué (voir la section quêtes, sous-section objectifs). Paramètres : guid de quête suivi de l’index de l’objectif dans la liste (à partir de 1).
timer - Ajoute une minuterie spécifique à ce caractère. Paramètres : guidage d’une minuterie.
updatequeststatus - Met à jour l’état de la quête, peut utiliser diverses chaînes telles que completed, done, obtain, . Paramètres : un guide de quête suivi d’un statut.
updateobjective - Met à jour l’état objectif d’une quête, tout comme la commande précédente. Paramètres : guid de quête, index et statut de l’objectif.
updateobjectivetext - Modifie le texte affiché pour un certain objectif. Paramters : guide de quête, index de l’objectif et entrée de traduction.
unlockroom - Permet au personnage de recevoir une liste de commandes lorsqu’il entre dans une certaine pièce de la carte. Paramètres : Un squaretag suivi d’une liste de commandes (avec : au lieu de ;).
unlocktimer - Identique à timer.
unlockworldevent - Permet de cliquer avec le bouton droit de la souris sur l’un des « PNJ » préexistants et de démarrer un dialogue spécifique. Paramètres: 
worldevent - Identique à unlockworldevent.

-------------------------------------------------------

SECTION 3 : DIALOGUES

Une méthode courante pour débloquer des quêtes est de parler à un PNJ. Pour cela, nous devons créer des dialogues.

Voici comment insérer un dialogue dans le système : table.insert(SFQuestDatabase.DialoguePool, { dialoguecode = "VotreNomUniqueDeDialogue", context = "ContextMenuWorldEventTalkTo", command = "unlockquest;examplequest", optional = true, text = "IGUIDialogueexamplequestunlock", textaccepted = "IGUIDialogueexamplequestaccepted", textdeclined = "IGUIDialogueexamplequestdeclined"});

dialoguecode = (REQUIS) Une chaîne unique utilisée pour identifier votre dialogue parmi tous les dialogues ajoutés par tous les mods. context = (REQUIS) Une entrée de traduction qui sera affichée lorsque vous ferez un clic droit sur le carré. Pour les "PNJ", je suggère "ContextMenuWorldEventTalkTo". command = (REQUIS) Une chaîne indiquant ce que fait ce dialogue. Les options sont : completequest - Utilisé pour terminer une quête active, suivi de l'identifiant de la quête. "completequest;YourQuestGuid" unlockquest - Utilisé pour débloquer des quêtes, suivi de l'identifiant de la quête. "unlockquest;YourQuestGuid" updateobjectivestatus - Utilisé pour mettre à jour le statut d'un objectif spécifique pour une quête active, suivi de l'identifiant de la quête, de l'index de l'objectif et du statut. "updateobjectivestatus;YourQuestGuid;1;Completed" optional = (OPTIONNEL) Uniquement utilisé si le dialogue débloque une quête. Si cette option est définie sur true, le personnage peut refuser l'offre et revenir plus tard. text = (REQUIS) Une entrée de traduction avec le texte qui sera affiché dans la fenêtre de dialogue. textaccepted = Uniquement utilisé si le dialogue débloque une quête. Le texte sera affiché après avoir accepté l'offre. textdeclined = Uniquement utilisé si le dialogue débloque une quête. Le texte sera affiché après avoir refusé l'offre.

Donc, si l'idée est de débloquer et de terminer une quête en parlant à un PNJ, nous devons ajouter 2 dialogues distincts et donner seulement celui pour la complétion lorsque le joueur remplit les conditions. Par exemple, en acquérant un objet.

-------------------------------------------------------

SECTION 4 : ÉVÉNEMENTS MONDIAUX (AKA PNJ)

Pour implémenter complètement un dialogue, nous avons besoin d'un "PNJ" dans cette case. Celui-ci contiendra des informations nécessaires pour d'autres mécanismes qui ne doivent pas être incluses dans chaque dialogue. Ces informations sont appelées des "événements mondiaux" en interne.

Voici comment insérer un événement mondial :
table.insert(SFQuestDatabase.WorldPool, {identity = "VotreModSurvivant", square = "12000x7000x0", name = "IGUIWorldEventNameSurvivor", faction = "TestFaction", picture = "media/textures/Picture_SurvivorFace.png"});

identity = (OBLIGATOIRE) Une chaîne unique qui l'identifie parmi tous les événements mondiaux ajoutés par tous les mods. Ce serait une bonne pratique d'inclure le nom de votre mod ou un autre préfixe unique.
square = (OBLIGATOIRE) Une chaîne composée des valeurs x, y et z de la case, séparées par un "x". Exemple : "8000x5000x0". Assurez-vous que les joueurs peuvent atteindre cette case.
name = (OBLIGATOIRE) Une chaîne pointant vers une entrée de traduction contenant le nom de l'événement, s'il n'y a aucune entrée fournie pour le niveau de faction actuel atteint par le joueur.
faction = (OPTIONNEL) La faction à laquelle appartient ce PNJ, lors de l'attribution de points de réputation pour les quêtes, c'est la faction qui sera utilisée. En théorie, ce n'est pas requis, mais ne tentez pas d'attribuer des points de réputation si vous n'incluez pas un code de faction valide.
picture = (OPTIONNEL) Un fichier png qui ne doit pas dépasser 100 pixels de largeur et 140 pixels de hauteur. Si non inclus, aucun portrait ne sera affiché dans les fenêtres de dialogue.

-------------------------------------------------------

 SECTION 5 : MANNEQUINS

Les mannequins peuvent être utilisés pour représenter des PNJ statiques, mais ils sont optionnels et la mécanique des événements mondiaux/dialogues fonctionne sans eux. Assurez-vous que leur case correspond à un événement mondial existant (voir section des événements mondiaux).

Insertion de la table des mannequins ici, à utiliser par les événements mondiaux "PNJ"
Un seul peut être placé dans une case par conception.
Obtenez les valeurs x, y et z de la case, séparées par un "x".
SFQuestDatabase.MannequinPool["8000x5000x0"] = {sprite = "locationshopmall01_68", direction = "S", beard = "", hair = "Bald", outfit = "Farmer"};

sprite = (OBLIGATOIRE) Une chaîne avec l'un des sprites de mannequin disponibles, de location_shop_mall_01_65 à 70 et de 73 à 78.
direction = Une chaîne avec la direction vers laquelle ce mannequin fera face, doit être l'un des suivants : "N", "W", "E" ou "S".
beard = (OPTIONNEL) Une chaîne avec l'ID du modèle de barbe, si non inclus, un modèle aléatoire sera utilisé en fonction des définitions de tenue. "" signifie pas de barbe.
beardcolor = (OPTIONNEL) Une chaîne contenant des valeurs r, v, b allant de 0 à 1, séparées par des virgules. Exemple : "0.5,0.1,0.1". Si non inclus, une couleur de cheveux aléatoire basée sur les définitions de tenue sera utilisée.
hair = (OPTIONNEL) Une chaîne avec l'ID du modèle de cheveux, si non inclus, un modèle aléatoire sera utilisé en fonction des définitions de tenue. "Bald" signifie pas de cheveux.
haircolor = (OPTIONNEL) Une chaîne contenant des valeurs r, v, b allant de 0 à 1, séparées par des virgules. Exemple : "0.5,0.1,0.1". Si non inclus, une couleur de cheveux aléatoire basée sur les définitions de tenue sera utilisée.
outfit = (OPTIONNEL) Une chaîne avec une tenue valide provenant de clothing.xml, gardez à l'esprit que les tenues sont soit masculines soit féminines, donc choisissez une tenue qui correspond à votre mannequin choisi. Ce système offre quelques tenues qui incluent une couleur de peau aléatoire, l'utilisation de tenues issues du jeu de base donnera un aspect pâle au mannequin.

-------------------------------------------------------

SECTION 6: FACTIONS

Toutes les factions de tous les mods de quêtes activés sont affichées dans l'onglet Quêtes. Celles-ci peuvent être utilisées par les moddeurs mais n'offrent aucune fonctionnalité en elles-mêmes.

Les quêtes peuvent donner des points de réputation qui débloquent des niveaux.

Voici comment une faction est insérée dans le système :
table.insert(SFQuestDatabase.FactionPool, {factioncode = "ExampleFaction", name = "IGUIFactions_ExampleFaction", startrep = 0, minrep = 0, maxtier = 5, tiers = ExampleTiersTemplate});

factioncode = (REQUIS) Une chaîne unique utilisée pour identifier votre faction parmi toutes les factions ajoutées par tous les mods. Elle est référencée, par exemple, lorsqu'une quête inclut des récompenses de réputation.
name = (REQUIS) Une chaîne pour une entrée de traduction. Exemple : "IGUI_Factions_ExampleFaction".
startrep = (REQUIS) Les points de réputation de départ pour cette faction.
minrep = (REQUIS) Le nombre minimum de points que tout personnage peut avoir, au cas où il serait prévu de supprimer des points.
maxtier = (REQUIS) Le niveau maximum qu'un personnage peut débloquer pour la faction. Aucun point de réputation ne sera accordé une fois ce niveau atteint.
tiers = (REQUIS) Une table contenant 1 table pour chaque niveau. Il est conseillé de créer une table distincte à utiliser comme modèle si toutes les factions vont fonctionner de la même manière.

--------------------------------------------------------------------

Voici un exemple de modèle :
ExampleTiersTemplate = { {tiername = "IGUIFactionsTemplateTier1", minrep = 700, barcolor = "red"}, {tiername = "IGUIFactionsTemplateTier2", minrep = 1400, barcolor = "orange"}, {tiername = "IGUIFactionsTemplateTier3", minrep = 2100, barcolor = "yellow"}, {tiername = "IGUIFactionsTemplateTier4", minrep = 2800, barcolor = "green"} };

La table de chaque niveau comprendra les éléments suivants :

barcolor = Une chaîne avec la couleur qui sera utilisée pour la barre de progression (voir la dernière partie de cette section).
minrep = Les points de réputation qui doivent être atteints pour débloquer le niveau.
tiername = Le nom qui sera affiché pour ce niveau.
unlocks = (OPTIONNEL) Une chaîne qui est une liste de commandes (voir la section des commandes), qui se déclenche lorsque le niveau est atteint.

--------------------------------------------------------------------

Les couleurs utilisées dans la barre de progression d'une faction sont liées à une simple table fournie par le système. Les options standard sont "bleu", "cyan", "vert", "magenta", "orange", "rouge" et "jaune". Mais de nouvelles couleurs peuvent facilement être ajoutées. La table doit inclure 3 valeurs (r, g, b) comprises entre 0.0 et 1.0.
SFQuest_Database.ColorPool.Purple = {0.5, 0.0, 1.0}

--------------------------------------------------------------------------------------------------------------------------------------------------

SECTION 7 : POOLS DE DÉPART

Les pools de départ sont une liste de choses qui seront données aux personnages lors de leur création. Par exemple, si vous souhaitez que le personnage puisse parler à un PNJ dès le départ, cette option doit être ajoutée ici.

Certaines mécaniques posent problème lorsqu'elles sont données directement de cette façon, il est donc également possible de donner un court minuteur qui sera ensuite utilisé pour débloquer la fonction (voir la section des minuteurs).
table.insert(SFQuestDatabase.StartingPool, {condition = "profession;Carpenter", world = "YourModSurvivor;YourMod_UniqueDialogueName;examplequest"});

condition = (OPTIONNEL) La liste de ce pool de départ spécifique ne sera donnée que si le personnage remplit la condition requise. Exemples : "profession;Carpenter" ou des traits comme "trait;Handy".
click = (OPTIONNEL) Une chaîne composée des paramètres pour un événement de clic droit (voir les sections des commandes et des événements de clic droit).
daily = (OPTIONNEL) Un pool de quêtes quotidiennes données par un PNJ spécifique (voir la section des événements quotidiens).
quest = (OPTIONNEL) Un guid de quête. La quête sera ajoutée à la liste des quêtes actives.
timer = (OPTIONNEL) Une chaîne contenant le guid d'un minuteur (voir la section des minuteurs).
world = (OPTIONNEL) Une chaîne composée d'une identité d'événement mondial, d'un code de dialogue et d'un guid de quête, séparés par ;


Ainsi, dans cet exemple, seuls les personnages charpentiers pourraient parler au PNJ et déverrouiller cette quête.

-------------------------------------------------------

SECTION 8 : TIMERS

TRADUCTION EN COURS, MOUVÉE DU PACK STALKER

-- Insertion des timers, qui déclenchent quelque chose lorsqu'ils expirent.
-- Les timers peuvent avoir une durée de compte à rebours aléatoire entre les valeurs timermin et timermax, pour une durée fixe, utilisez la même valeur pour les deux.
-- guid = une chaîne unique parmi tous les timers utilisés pour les identifier, il est recommandé d'inclure le nom de votre mod dedans
-- sound = peut être utilisé pour jouer un son lors de l'expiration.
-- command = une version simplifiée de commandes (voir ci-dessous), elle accepte une chaîne qui doit être une commande valide qui fait généralement quelque chose en relation avec la valeur guid du timer. Exemple : "unlockQuest" débloquera la quête qui partage le même guid que ce timer.
-- commands = une chaîne comprenant tout ce que fait le timer lorsqu'il expire. Chaque commande est séparée de ses paramètres par des points-virgules.
    timermin = Les timers peuvent avoir une durée de compte à rebours aléatoire entre les valeurs timermin et timermax, pour une durée fixe, utilisez la même valeur pour les deux. Mesuré en heures.
    timermax =

table.insert(SFQuestDatabase.TimerPool, {guid = "DucksQuestTimerExample", command = "unlockQuest", timermin = 1, timermax = 2, sound = "doublebeep"}); table.insert(SFQuestDatabase.TimerPool, {guid = "PondStalkerBanditsInit", commands = "randomcodedworldfrompool;PondStalkerBandits;ThePondStalker;Bandits", timermin = 0.15, timermax = 0.15}); ]]--

--------------------------------------------------------------------

--[[ SECTION 9 : ÉVÉNEMENTS DE CLIQUE DROIT

Pour éviter d'avoir besoin d'un million d'actions chronométrées différentes pour chaque besoin spécifique,
le système de quêtes inclut une action chronométrée qui peut recevoir des valeurs des mods
ajoutant des quêtes, permettant ainsi d'effectuer des actions personnalisées qui ont des résultats uniques
lorsqu'elles sont effectuées.

Chaque événement de clic droit est lié à une case spécifique sur la carte, mais plusieurs événements
peuvent être actifs pour une case en même temps.

Les événements de clic droit peuvent être attribués à un personnage en utilisant la commande "clickevent"
(voir la section des commandes) ou via un pool de départ (voir la section des pools de départ).
Chaque paramètre est séparé du suivant par un point-virgule ;

--------------------------------------------------------------------
Le premier paramètre inclut l'étiquette de la case et un nom unique pour cet événement de clic,
afin de pouvoir l'identifier ultérieurement, par exemple lorsque vous souhaitez le supprimer pour un joueur.

Exemple: "8000x6000x0:MonPropreÉvénement"


--------------------------------------------------------------------
Le deuxième paramètre comprend les données de l'action chronométrée, séparées par :

anim: (OPTIONNEL) Définit l'animation utilisée par l'action, les animations ont des noms internes (exemple : anim:Loot).
animvar: (OPTIONNEL) Définit une variable pour certaines animations pouvant être utilisée par l'action. Paramètres : 2 chaînes (exemple : animvar:LootPosition:Low pour l'animation Loot).
            LootPosition:Low (animation Loot) Utilisé lorsqu'un personnage ramasse des objets par terre.
time: (REQUIS) Définit la durée de l'action (exemple : time:50).

Cela donnerait "anim:Loot:time:50"

--------------------------------------------------------------------
Le troisième paramètre est une liste de commandes qui s'exécuteront lorsque l'action
sera entièrement effectuée. Comme elle est incluse dans une chaîne plus grande,
le point-virgule ; habituel qui sépare les commandes et leurs paramètres
est remplacé par : Ne vous inquiétez pas, le système le reconvertira en
point-virgule en interne et cela fonctionnera comme une liste standard de commandes.

--------------------------------------------------------------------
Section 10 : PISCINE ÉVÉNEMENTS ALEATOIRES

Ce sont des tables utilisées pour randomiser les événements mondiaux (voir la section des événements mondiaux). Ils incluent des chaînes avec le format "ÉvénementMonde;CodeDialogue,GuideQuête".

Lors de l'utilisation de la commande randomcodedworldfrompool (voir la section des commandes), les 2 derniers paramètres seront ExamplePool et Pool1 ou Pool2 dans cet exemple :

ExamplePool = { Pool1 = { "PondStalkerBandits;ThePondStalkerAmmoBoxBandits;ThePondStalkerAmmoBoxBandits", "PondStalkerBandits;ThePondStalkerAnimalBandits;ThePondStalkerAnimalBandits", "PondStalkerBandits;ThePondStalkerArtifactBandits;ThePondStalkerArtifactBandits", "PondStalkerBandits;ThePondStalkerBigFishBandits;ThePondStalkerBigFishBandits", "PondStalkerBandits;ThePondStalkerCannedFoodBandits;ThePondStalkerCannedFoodBandits", "PondStalkerBandits;ThePondStalkerFishBandits;ThePondStalkerFishBandits", "PondStalkerBandits;ThePondStalkerForagedFoodBandits;ThePondStalkerForagedFoodBandits", "PondStalkerBandits;ThePondStalkerMedicineBandits;ThePondStalkerMedicineBandits", "PondStalkerBandits;ThePondStalkerPatchesBandits;ThePondStalkerPatchesBandits", "PondStalkerBandits;ThePondStalkerPistolBandits;ThePondStalkerPistolBandits", "PondStalkerBandits;ThePondStalkerRifleBandits;ThePondStalkerRifleBandits", "PondStalkerBandits;ThePondStalkerShotgunBandits;ThePondStalkerShotgunBandits", "PondStalkerBandits;ThePondStalkerSkillBookBandits;ThePondStalkerSkillBookBandits", "PondStalkerBandits;ThePondStalkerZombiesBandits;ThePondStalkerZombiesBandits", }, Pool2 = { "PondStalkerClearSky;ThePondStalkerAmmoBoxClearSky;ThePondStalkerAmmoBoxClearSky", "PondStalkerClearSky;ThePondStalkerAnimalClearSky;ThePondStalkerAnimalClearSky", "PondStalkerClearSky;ThePondStalkerArtifactClearSky;ThePondStalkerArtifactClearSky", "PondStalkerClearSky;ThePondStalkerBigFishClearSky;ThePondStalkerBigFishClearSky", "PondStalkerClearSky;ThePondStalkerCannedFoodClearSky;ThePondStalkerCannedFoodClearSky", "PondStalkerClearSky;ThePondStalkerFishClearSky;ThePondStalkerFishClearSky", "PondStalkerClearSky;ThePondStalkerForagedFoodClearSky;ThePondStalkerForagedFoodClearSky", "PondStalkerClearSky;ThePondStalkerMedicineClearSky;ThePondStalkerMedicineClearSky", "PondStalkerClearSky;ThePondStalkerPistolClearSky;ThePondStalkerPistolClearSky", "PondStalkerClearSky;ThePondStalkerRifleClearSky;ThePondStalkerRifleClearSky", "PondStalkerClearSky;ThePondStalkerShotgunClearSky;ThePondStalkerShotgunClearSky", "PondStalkerClearSky;ThePondStalkerSkillBookClearSky;ThePondStalkerSkillBookClearSky", "PondStalkerClearSky;ThePondStalkerZombiesClearSky;ThePondStalkerZombies_ClearSky", }, }

SFQuest_Database.RandomEventPool.ExamplePool = ExamplePool;

]]--

--[[ SECTION : ÉVÉNEMENTS QUOTIDIENS

EN COURS DE DÉVELOPPEMENT

Voilà comment insérer un événement quotidien :
table.insert(SFQuestDatabase.DailyEventPool, { dailycode = "ExempleCodeQuotidien", condition = "notmaxedwithcode;QuêteCodeQuotidienPourCePool;1", commands = "randomcodedworldfrompool;PondStalker_Bandits;ThePondStalker;Bandits", days = 0, frequency = 12});
]]--