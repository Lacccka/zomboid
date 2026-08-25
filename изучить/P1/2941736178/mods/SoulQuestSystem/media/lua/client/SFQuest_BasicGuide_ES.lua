--------------------------------------------------------------------------------------------------------------------------------------------------
--[[

	Esta es una guía básica para el archivo principal que tienes que añadir a tu mod,
	qué hace cada tipo de tabla e información sobre la mayoría de sus valores.
	
	Intentaré mejorarla a medida que reciba comentarios de los modders.
	
	--------------------------------------------------------------------
	
	Su archivo debe ser colocado dentro de media/lua/client.
	Ahora vamos a empezar.

]]--

-- Esta es la primera línea que necesitas:
require 'SFQuest_Database'


--------------------------------------------------------------------------------------------------------------------------------------------------
--[[ SECTION 1 : QUESTS

	Antes de poder dar una búsqueda a un personaje es necesario crearla
	e introducirla en el sistema. Veamos cómo se hace y qué
	significan los valores.

	Una búsqueda será una tabla local que incluirá varios valores:
	local yourTable = { guid = "examplequest", completesound = "levelup", lore = {"IGUI_QuestLore_examplequest"}, needsitem = "Base.Nails;5", unlockedsound = "QuestUnlocked", text = "IGUI_Quest_examplequest", texture = "Item_Nails", title = "IGUI_QuestTitle_examplequest", awardsitem = "Money;20", awardsrep = "TestFaction;100" }
	
	Aquí hay una lista de valores que van dentro de la tabla de búsqueda y lo que hacen.
	Se pueden añadir a la tabla en cualquier orden, por lo general añadir guid primero y
	y luego trato de enumerarlos alfabéticamente.
	
	awardsitem = (OPTIONAL) Uno o más tipos de objetos que se entregarán al jugador cuando complete la misión, seguidos de la cantidad y separados por punto y coma. Estos se muestran en el diálogo de un PNJ para aceptar la quest (Ver más sobre esto en la sección de diálogos). Acepta un objeto "Base.Burger", artículo y cantidad "Base.Burger;2" o varios artículos y sus cantidades "Base.Fries;2;Base.Burger;1".
	awardslore = (OPTIONAL) La búsqueda recibirá una página de texto más en su interfaz de lore, que acepta una cadena que contiene una entrada de traducción. Si la búsqueda no tenía páginas de texto y no se podía hacer clic en ella, se podrá hacer clic cuando se añada esta nueva página.
	awardsrep = (OPTIONAL) Puntos de reputación que se obtendrán al completar la misión. Acepta el código de facción (ver la sección de facción) más la cantidad de reputación, por ejemplo: "TestFaction;50".
	awardstask = (OPTIONAL) Desbloquea otra quest cuando esta quest es completada, acepta una cadena que debe ser un guid de quest valido y unico (ver guid en esta lista).
	awardsworld = (OPTIONAL) Desbloquea un dialogo NPC cuando esta quest es completada, Acepta una cadena que debe ser un dialoguecode valido y unico (ver la seccion de dialogo).
	completesound = (OPTIONAL) Enlaza con un ID de sonido que se reproducirá cuando se complete la quest, yo uso el antiguo sonido de subir de nivel "levelup" para mis quests.
	dailycode = (OPTIONAL) Cadena utilizada para identificar las misiones del mismo pool diario. Cuando llega el momento de repetir la tirada y dar una nueva misión de un grupo, el sistema comprueba si el personaje ya tiene una.
	guid = (REQUIRED) Esta es una cadena única utilizada para identificar tu búsqueda entre todas las búsquedas añadidas por todos los mods, así que hazla realmente única como "YourModName_UniqueQuestName".
	lore = (OPTIONAL) Esta es una tabla con entradas de traducción, estas entradas deben contener texto que los jugadores puedan leer si hacen clic en la búsqueda, si no se incluye una tabla de lore al hacer clic en la búsqueda no se abrirá la interfaz de usuario de lore.
	needsitem = (OPTIONAL) Añadir esto significa que tu quest necesita uno o más items para progresar, puede ser un ID de item, o una etiqueta de item, seguido de la cantidad y separado por punto y coma. Ejemplos: "Base.Nails;5", o una de las opciones de etiqueta:
							Existen varias opciones a la hora de buscar etiquetas:
							Tag# acepta cualquier elemento que tenga la etiqueta. Ejemplo: "tag#Egg;12".
							TagPredicateBigFish# acepta pescado fresco a partir de cierta longitud.
							TagPredicateCondition# acepta armas por encima de una determinada condición.
							TagPredicateFreshFood# acepta alimentos que estén frescos en ese momento.
							TagPredicateFullDrainable# sólo aceptará artículos completamente drenables.
	objectives = (OPTIONAL) Una tabla que contiene una tabla para cada objetivo. Los objetivos muestran su propio texto y pueden tener su propio estado de progreso. Normalmente es necesario completar todos los objetivos antes de poder completar una misión, pero no necesariamente en el orden en que se presentan. Por ejemplo: Una búsqueda puede requerir hablar con otros 4 PNJs.
	ondone = (OPTIONAL)  Una cadena que contiene una lista de comandos que serán ejecutados cuando la misión u objetivo sea marcado como realizado (ver la sección de comandos).
	onobtained = (OPTIONAL) Una cadena que contiene una lista de comandos que serán ejecutados cuando la misión u objetivo sea marcado como obtenido (ver la sección de comandos).
	text = (REQUIRED) El texto que se mostrará en la lista de misiones, debe ser corto y descriptivo.
	texture = (OPTIONAL) El icono que se mostrará en la lista de misiones. Si utilizas un icono de objeto del juego debes añadir el icono "Item_" preffix, si se trata de un icono modded la ruta completa y el archivo deben ser provived. Ejemplo:  "Item_Nails", "media/textures/Item_MyOwnIcon.png".
	title = (OPTIONAL) Una entrada de traducción con el título que se muestra en el lore UI. Ejemplo: "IGUI_QuestTitle_examplequest".
	unlockedsound = (OPTIONAL) Enlaces a un ID de sonido que se reproducirá cuando la búsqueda se da a un personaje, proporciono un nuevo sonido "QuestUnlocked" que uso para mis búsquedas.
	updates = (OPTIONAL) Significa que esta quest es en realidad una actualización de otra quest activa y no se añadirá a la lista de activas. Debe ser una guia de quest valida y unica y esa quest debe estar en la lista de quest activas en el momento de desbloquear esta actualizacion. La mayoría de los valores listados aquí pueden ser usados para cambiar esa quest para el personaje. Ejemplos: textura, texto, título.
	
	--------------------------------------------------------------------	
	
	Esta es la línea que inserta una nueva búsqueda en el sistema:
	table.insert(SFQuest_Database.QuestPool, yourTable);
	
	O puedes hacerlo en una sola línea como esta:
	table.insert(SFQuest_Database.QuestPool, { guid = "examplequest", completesound = "levelup", lore = {"IGUI_QuestLore_examplequest"}, needsitem = "Base.Nails;5", unlockedsound = "QuestUnlocked", text = "IGUI_Quest_examplequest", texture = "Item_Nails", title = "IGUI_QuestTitle_examplequest", awardsitem = "Money;20", awardsrep = "TestFaction;100" });
	
	--------------------------------------------------------------------
	SECTION 1.1 : OBJETIVOS
	
	hidden = (OPTIONAL) Si se establece en true el objetivo no se muestra en la pestaña de búsqueda. Sigue funcionando como cualquier otro objetivo y puede mostrarse si se actualiza para eliminar el valor oculto.
	oncompleted = (OPTIONAL) Una cadena de comandos que se ejecutarán cuando ese objetivo se marque como completado (ver la sección de comandos).
	
]]--


--------------------------------------------------------------------------------------------------------------------------------------------------
--[[ SECTION 2 : COMANDOS

	Las misiones, temporizadores u otras mecánicas de este sistema pueden ejecutar 
	una variedad de comandos, éstos se incluyen en cadenas seguidas
	seguidos de sus parámetros, si los hay. Una sola cadena puede contener muchos
	comandos que se ejecutarán todos al mismo tiempo.
	
	Aquí está la lista de comandos y los parámetros que necesitan:
	
	actionevent - Añade una condición específica que se comprobará cada 10 minutos, cuando se cumpla la condición se dará una lista de comandos. Parámetros: Condición y lista de comandos.
					Posibles condiciones: "killzombies", seguido de la cantidad, separada por : "killzombies:25".
					Commands: Cualquier comando y sus parámetros, pero con : en lugar de ;
	addmannequin - Añade un maniquí al mundo. Ten en cuenta que el maniquí debe haber sido insertado en la lista de maniquíes para que esto funcione. Parámetros: Una etiqueta de cuadrado (cadena compuesta por x, y y z del cuadrado).
	additem - Añade un objeto al inventario del jugador. Parámetros: ID del objeto seguido de la cantidad.
	clickevent - Añade un evento de clic derecho en una casilla específica (ver sección de eventos de clic derecho). Parámetros: A "squaretag" y una cadena única para el evento de clic separada por : (ejemplo: "12000x7500x0:MyUniqueClickEvent", para la acción temporizada separados por : (ejemplo: "time:50:anim:loot"), y una lista de comandos que se ejecutarán cuando se realice la acción. Cada parámetro separado por punto y coma ;
	completequest - Completa una búsqueda. Parámetros: A quest guid.
	lore    - Añade una nueva entrada a una misión. Parámetros: Quest guid seguido de una entrada de traducción.
	playersay - Muestra una línea de texto sobre la cabeza del personaje, no crea sonido real para atraer a los zombies. Parámetros: translation entry.
	randomcodedworldfrompool - Pescoge un evento/diálogo aleatorio de un pool y ejecuta unlockworldevent. Parámetros: A dailycode (ver sección de misiones), nombre de la tabla externa, nombre de la tabla interna.
	removeclickevent - Elimina el evento de clic del personaje. Parámetro: Nombre único de un evento de clic (ver sección de eventos de clic derecho).
	removemannequin - Elimina un maniquí del mundo.
	revealobjective - Revela un objetivo que está actualmente oculto (ver sección de misiones, sub sección de objetivos). Parámetros: Guía de la misión seguida del índice del objetivo en la lista (empezando por 1).
	timer - Añade un temporizador específico a ese personaje. Parámetros: La guía de un temporizador.
	updatequeststatus - Actualiza el estado de la búsqueda, puede utilizar una variedad de cadenas como completado, hecho, obtenido, . Parámetros: Una guía de búsqueda seguida de un estado.
	updateobjective - Actualiza el estado del objetivo de una búsqueda, de forma similar al comando anterior. Parámetros: Guía de la misión, índice y estado del objetivo.
	updateobjectivetext - Cambia el texto mostrado para un determinado objetivo. Parámetros: Guía de la misión, índice del objetivo y una entrada de traducción.
	unlockroom - Permite que el personaje reciba una lista de comandos al entrar en una determinada sala del mapa. Parámetros: Una etiqueta cuadrada seguida de una lista de comandos (con : en lugar de ;).
	unlocktimer -  Igual que el temporizador. 
	unlockworldevent - Permite hacer clic con el botón derecho del ratón en uno de los "PNJs" preexistentes e iniciar un diálogo específico. Parámetros: 
	worldevent - Igual que unlockworldevent.
	
	
	
]]--


--------------------------------------------------------------------------------------------------------------------------------------------------
--[[ SECTION 3 : DIALOGOS

	Un método común de desbloquear misiones es hablando con un PNJ.
	Para eso necesitamos crear diálogos.

	Aquí insertamos todos los diálogos necesarios.
	en lo que hacen por lo que es posible que un mod necesite muchos de ellos.
	
	Así es como se inserta un diálogo en el sistema:
table.insert(SFQuest_Database.DialoguePool, { dialoguecode = "YourMod_UniqueDialogueName", context = "ContextMenu_WorldEvent_TalkTo", command = "unlockquest;examplequest", optional = true, text = "IGUI_Dialogue_examplequest_unlock", textaccepted = "IGUI_Dialogue_examplequest_accepted", textdeclined = "IGUI_Dialogue_examplequest_declined"});

	dialoguecode = (REQUIRED) Una cadena única utilizada para identificar tu diálogo entre todos los diálogos añadidos por todos los mods.
	context = (REQUIRED) Una entrada de traducción que se mostrará al hacer clic con el botón derecho del ratón en el cuadrado. Para "NPCs" sugiero "ContextMenu_WorldEvent_TalkTo".
	command = (REUIRED) Una cadena con lo que hace este diálogo. Las opciones son:
						completequest - Utilizado para completar una búsqueda activa, seguido de una guía de búsqueda. "completequest;YourQuestGuid"
						unlockquest - Se utiliza para desbloquear misiones, seguido de la guía de la misión. "unlockquest;YourQuestGuid"
						updateobjectivestatus - Se utiliza para actualizar el estado de un objetivo específico para una búsqueda activa, seguido de la guía de la búsqueda, el índice del objetivo y el estado. "updateobjectivestatus;YourQuestGuid;1;Completed"
	optional = (OPTIONAL) Solo se usa si el dialogo desbloquea una quest. Si se establece en true el personaje puede rechazar el trato y volver más tarde.
	text = (REQUIRED) Una entrada de traducción con el texto que se mostrará en la ventana de diálogo.
	textaccepted = Sólo se utiliza si el diálogo desbloquea una misión. El texto se mostrará después de aceptar el trato.
	textdeclined = Sólo se utiliza si el diálogo desbloquea una misión. El texto se mostrará después de rechazar el trato.
	
	So si la idea es desbloquear y completar una quest hablando no un NPCs
	tenemos que añadir 2 diálogos separados y sólo dar el uno para su finalización
	cuando el jugador haya cumplido los requisitos. Por ejemplo, adquirir un objeto.
	
]]--


-------------------------------------------------------------------------------------------------------------------------------------------------- 
--[[ SECTION 4 : EVENTOS MUNDIALES (NPC)

	Para implementar completamente un diálogo necesitamos
	un "NPC" en esa plaza. Éste
	contendrá información necesaria para
	otras mecánicas que no necesitan ser
	en cada diálogo. Estos son
	llamados 'Eventos Mundiales' internamente.
	
	Así se inserta un acontecimiento mundial:
table.insert(SFQuest_Database.WorldPool, {identity = "YourMod_Survivor", square = "12000x7000x0", name = "IGUI_WorldEventName_Survivor", faction = "TestFaction", picture = "media/textures/Picture_SurvivorFace.png"});

	identity = (REQUIRED) Una cadena única que lo identifica entre todos los eventos mundiales añadidos por todos los mods. Sería una buena práctica incluir en ella el nombre de tu mod o algún otro tipo de prefijo único.
	square = (REQUIRED) Cadena compuesta por los valores x, y y z del cuadrado, separados por una "x". Ejemplo: "8000x5000x0". Asegúrate de que los jugadores pueden llegar a ese cuadrado.
	name = (REQUIRED) Una cadena que apunta a una entrada de traducción que contiene el nombre del evento, si no hay ninguna entrada para el nivel de la facción actual alcanzado por el jugador.
	faction = (OPTIONAL) La facción a la que pertenece este PNJ, cuando se otorgan puntos de reputación para las misiones esta es la facción que se utilizará. En teoría no es necesario, pero no intentes otorgar puntos de reputación si no incluyes un código de facción válido.
	picture = (OPTIONAL) Un archivo png que no debe tener más de 100 píxeles de ancho y 140 píxeles de alto. Si no se incluye, no se mostrará ningún retrato en las ventanas de diálogo.

]]--


--------------------------------------------------------------------------------------------------------------------------------------------------
--[[ SECCION 5 : MANIQUIS

	Se pueden utilizar maniquíes para representar a los PNJ estáticos, pero son opcionales
	y la mecánica de eventos/diálogos del mundo funciona sin ellos. Asegúrate de que
	su casilla coincide con un evento mundial existente (ver sección de eventos mundiales).

	Insertar la casilla del maniquí aquí, para ser utilizada por el evento mundial "NPCs".
	Sólo uno puede ser colocado en un cuadrado por diseño.
	Obtén los valores x, y y z del cuadrado, separados por una "x".
	
SFQuest_Database.MannequinPool["8000x5000x0"] = {sprite = "location_shop_mall_01_68", direction = "S", beard = "", hair = "Bald", outfit = "Farmer"};

	sprite = (REQUIRED) Una cadena con uno de los sprites del maniquí disponible, location_shop_mall_01_65 to 70 and from 73 to 78.
	direction =  A string with one of the mannequin sprites available, "N", "W", "E" or "S".
	beard = (OPTIONAL) Una cadena con el ID del modelo de barba, si no se incluye se utilizará un modelo aleatorio según las definiciones de atuendo. "" significa sin barba.
	beardcolor = (OPTIONAL) Una cadena que contiene valores r, g, b que van de 0 a 1, separados por coma. Ejemplo: "0.5,0.1,0.1". Si no se incluye, utilizará un color de pelo aleatorio basado en las definiciones del atuendo.
	hair = (OPTIONAL) Una cadena con el ID del modelo de pelo, si no se incluye se utilizará un modelo aleatorio de acuerdo con las definiciones de atuendo. "Bald" significa que no hay pelo.
	haircolor = (OPTIONAL) Una cadena que contiene valores r, g, b que van de 0 a 1, separados por coma. Ejemplo: "0.5,0.1,0.1". Si no se incluye, utilizará un color de pelo aleatorio basado en las definiciones del atuendo.
	outfit = (OPTIONAL) Una cadena con cualquier atuendo válido de clothing.xml, ten en cuenta que los atuendos son masculinos o femeninos, así que elige uno que coincida con tu maniquí elegido. Este sistema proporciona algunos trajes que incluyen el color de la piel al azar, el uso de trajes del juego base se traducirá en maniquí apariencia blanca en su lugar.

]]--


--------------------------------------------------------------------------------------------------------------------------------------------------
--[[ SECTION 6 : FACCIONES

Todas las facciones de todos los mods de misiones activados se muestran
	en la pestaña Misiones. Estos pueden ser utilizados por modders pero
	pero no proporcionan ninguna funcionalidad por sí mismas.
	
	Las misiones pueden dar puntos de reputación que desbloquean niveles.

	Así es como se inserta una facción en el sistema:
table.insert(SFQuest_Database.FactionPool, {factioncode = "ExampleFaction", name = "IGUI_Factions_ExampleFaction", startrep = 0, minrep = 0, maxtier = 5, tiers = ExampleTiersTemplate});

	factioncode = (REQUIRED) Una cadena única utilizada para identificar tu facción entre todas las facciones añadidas por todos los mods. Se utiliza, por ejemplo, cuando una misión incluye recompensas de reputación. 
	name = (REQUIRED) Una cadena para una entrada de traducción. Ejemplo: "IGUI_Factions_ExampleFaction".
	startrep = (REQUIRED) Los puntos de reputación iniciales de esa facción.
	minrep = (REQUIRED) Los puntos mínimos que puede tener cualquier personaje, en caso de que haya planes para eliminar puntos.
	maxtier = (REQUIRED) El nivel máximo que un personaje puede desbloquear para la facción. No se concederán puntos de reputación una vez desbloqueado ese nivel.
	tiers = (REQUIRED) Una tabla que contiene 1 tabla para cada nivel. Se sugiere crear una tabla separada para usarla como plantilla si todas las facciones van a funcionar de la misma manera.
		
	--------------------------------------------------------------------
	
	Este es un ejemplo de plantilla:
ExampleTiersTemplate = { {tiername = "IGUI_Factions_Template_Tier1", minrep = 700, barcolor = "red"}, {tiername = "IGUI_Factions_Template_Tier2", minrep = 1400, barcolor = "orange"}, {tiername = "IGUI_Factions_Template_Tier3", minrep = 2100, barcolor = "yellow"}, {tiername = "IGUI_Factions_Template_Tier4", minrep = 2800, barcolor = "green"} };

	La tabla de cada grada incluirá lo siguiente:
	
	barcolor = Una cadena con el color que se utilizará para la barra de progreso (véase la última parte de esta sección).
	minrep = Los puntos de reputación que hay que alcanzar para desbloquear el nivel
	tiername = El nombre que se mostrará para ese nivel.
	unlocks = (OPTIONAL) Una cadena que es una lista de comandos (ver sección comandos), se dispara cuando se alcanza el nivel.
	
	--------------------------------------------------------------------
	
	Los colores utilizados en la barra de progreso de una facción enlazan con una sencilla tabla proporcionada
	por el sistema. Las opciones estándar son "blue", "cyan", "green", "magenta",
	"orange", "red" Y yellow". Pero se pueden añadir fácilmente nuevos colores.
	La tabla debe incluir 3 valores (r, g, b) que oscilen entre 0,0 y 1,0
	
SFQuest_Database.ColorPool.Purple = {0.5, 0.0, 1.0}
	
	]]--


	--------------------------------------------------------------------------------------------------------------------------------------------------
--[[ SECTION 7 : PISCINAS DE INICIO

	Los fondos iniciales son una lista de cosas que se darán a los personajes
	cuando se crean. Por ejemplo, si quieres que el personaje pueda
	hablar con un NPC desde el principio, esa opción debe ser dada aquí.
	
	Algunas mecánicas tienen algunos problemas cuando se dan directamente así, por lo que es
	también es posible dar un temporizador corto que luego se utilizará para desbloquear 
	la característica (ver sección de temporizadores).

table.insert(SFQuest_Database.StartingPool, {condition = "profession;Carpenter", world = "YourMod_Survivor;YourMod_UniqueDialogueName;examplequest"});

	condition = (OPTIONAL) La lista de esta reserva inicial específica sólo se otorgará si el personaje cumple el requisito. Ejemplos: "profession;Carpenter" o rasgos como "trait;Handy".
	click = (OPTIONAL) Cadena compuesta por los parámetros de un evento de clic derecho (ver secciones comandos y eventos de clic derecho).
	daily = (OPTIONAL) Un conjunto de misiones diarias que se dan desde un NPC específico (ver sección de eventos diarios).
	quest = (OPTIONAL) Una guía de búsqueda. La búsqueda se añadirá a la lista de búsquedas activas.
	timer = (OPTIONAL) Una cadena que contiene el guid único de un temporizador (ver sección temporizadores).
	world = (OPTIONAL) Una cadena compuesta por la identidad de un evento mundial, un código de diálogo y una guía de búsqueda, separados por ;
	
	
	Así que en ese ejemplo sólo los personajes carpintero sería capaz de hablar con
	el NPC y desbloquear esa búsqueda.
	
]]--


--------------------------------------------------------------------------------------------------------------------------------------------------
--[[ SECTION 8 : temporizadores

	TRABAJO EN PROCESO, MOVIDO DEL STALKER PACK

--Insertar temporizadores, estos hacen que algo suceda cuando expiran.
--Los temporizadores pueden tener una duración de cuenta atrás aleatoria entre los valores timermin y timermax, para una duración determinada utilizar el mismo valor para ambos.
-- guid = una cadena única entre todos los temporizadores utilizada para identificarlos, sería una buena práctica incluir el nombre de su mod en ella
-- sound = se puede utilizar para reproducir un sonido al expirar.
-- command = Una versión más simple de commans (ver más abajo), acepta una cadena que debe ser un comando válido que normalmente hace algo relacionado con el valor guid del temporizador. Ejemplo: "unlockQuest" desbloqueará la quest que comparte el mismo guid de este temporizador.
-- commands = una cadena que incluye todo lo que hace el temporizador cuando expira. Cada comando está separado de sus parámetros por punto y coma.
	timermin = Los temporizadores pueden tener una duración de cuenta atrás aleatoria entre los valores timermin y timermax, para una duración determinada utilice el mismo valor para ambos. Se mide en horas.
	timermax =

table.insert(SFQuest_Database.TimerPool, {guid = "DucksQuestTimerExample", command = "unlockQuest", timermin = 1, timermax = 2, sound = "doublebeep"});
table.insert(SFQuest_Database.TimerPool, {guid = "PondStalker_BanditsInit", commands = "randomcodedworldfrompool;PondStalker_Bandits;ThePondStalker;Bandits", timermin = 0.15, timermax = 0.15});
]]--


--------------------------------------------------------------------------------------------------------------------------------------------------
--[[ SECTION 9 : EVENTOS DE CLIC DERECHO

	Para evitar la necesidad de un millón de acciones temporizadas diferentes para cada necesidad específica,
	el sistema de misiones incluye una acción cronometrada que puede recibir valores de los mods
	que añaden misiones, lo que permite realizar acciones personalizadas que tienen resultados
	resultados únicos cuando se realizan.
	
	Cada evento de clic derecho está vinculado a una casilla específica del mapa, pero más de un evento puede estar activo para una casilla a la vez.
	evento puede estar activo para una casilla al mismo tiempo. 
	
	Los eventos de clic derecho se pueden dar a un personaje usando el comando "clickevent"
	(ver la sección de comandos) o a través de un pool inicial (ver la sección de pool inicial).
	Cada parámetro está separado del siguiente por un punto y coma ;
	
	--------------------------------------------------------------------
	El primer parámetro incluye la etiqueta cuadrada y un nombre único para ese evento de clic
	para poder identificarlo más tarde, por ejemplo, cuando desee eliminarlo para un jugador.
	
	Ejemplo: "8000x6000x0:MyOwnEvent"
	
	
	--------------------------------------------------------------------
	El segundo parámetro incluye los datos de la acción temporizada, separados por :
	
	anim: (OPTIONAL) Establece la animación utilizada por la acción, las animaciones tienen nombres internos (ejemplo: anim:Loot).
	animvar: (OPTIONAL) Establece una variable para ciertas animaciones que pueden ser usadas por la acción. Parámetros: 2 cadenas (ejemplo: animvar:LootPosition:Low para la animación Loot).
				LootPosition:Low (Animación de botín) Se utiliza cuando un personaje coge objetos del suelo.
	time: (REQUIRED) Establece el tiempo de duración de la acción (ejemplo: time:50).

	Así que eso sería "anim:Loot:time:50"
		
	--------------------------------------------------------------------
	El tercer parámetro es una lista de comandos que se ejecutarán cuando la acción
	se ejecute por completo. Dado que se incluye como parte de una cadena mayor
	aquí el habitual punto y coma ; que separa los comandos y sus parámetros
	se sustituye por : No se preocupe, el sistema lo revertirá internamente a un punto y coma. 
	y funcionará como una lista estándar de comandos.

]]--


--------------------------------------------------------------------------------------------------------------------------------------------------
--[[ SECTION 10 : GRUPOS DE EVENTOS ALEATORIOS

	Son tablas que se utilizan para aleatorizar los eventos del mundo (ver sección eventos del mundo
	sección). Incluyen cadenas con el formato "WorldEvent;Dialoguecode,Questguid".
	formato.
	
	Cuando se usa el comando randomcodedworldfrompool (ver sección comandos) los 2 últimos parámetros serán ExamplePool y Pool1 o Pool2.
	2 parámetros serán ExamplePool y Pool1 o Pool2 en este ejemplo:

ExamplePool = {
	Pool1 = {
	"PondStalker_Bandits;ThePondStalker_AmmoBox_Bandits;ThePondStalker_AmmoBox_Bandits",
	"PondStalker_Bandits;ThePondStalker_Animal_Bandits;ThePondStalker_Animal_Bandits",
	"PondStalker_Bandits;ThePondStalker_Artifact_Bandits;ThePondStalker_Artifact_Bandits",
	"PondStalker_Bandits;ThePondStalker_BigFish_Bandits;ThePondStalker_BigFish_Bandits",
	"PondStalker_Bandits;ThePondStalker_CannedFood_Bandits;ThePondStalker_CannedFood_Bandits",
	"PondStalker_Bandits;ThePondStalker_Fish_Bandits;ThePondStalker_Fish_Bandits",
	"PondStalker_Bandits;ThePondStalker_ForagedFood_Bandits;ThePondStalker_ForagedFood_Bandits",
	"PondStalker_Bandits;ThePondStalker_Medicine_Bandits;ThePondStalker_Medicine_Bandits",
	"PondStalker_Bandits;ThePondStalker_Patches_Bandits;ThePondStalker_Patches_Bandits",
	"PondStalker_Bandits;ThePondStalker_Pistol_Bandits;ThePondStalker_Pistol_Bandits",
	"PondStalker_Bandits;ThePondStalker_Rifle_Bandits;ThePondStalker_Rifle_Bandits",
	"PondStalker_Bandits;ThePondStalker_Shotgun_Bandits;ThePondStalker_Shotgun_Bandits",
	"PondStalker_Bandits;ThePondStalker_SkillBook_Bandits;ThePondStalker_SkillBook_Bandits",
	"PondStalker_Bandits;ThePondStalker_Zombies_Bandits;ThePondStalker_Zombies_Bandits",
	},
	Pool2 = {
	"PondStalker_ClearSky;ThePondStalker_AmmoBox_ClearSky;ThePondStalker_AmmoBox_ClearSky",
	"PondStalker_ClearSky;ThePondStalker_Animal_ClearSky;ThePondStalker_Animal_ClearSky",
	"PondStalker_ClearSky;ThePondStalker_Artifact_ClearSky;ThePondStalker_Artifact_ClearSky",
	"PondStalker_ClearSky;ThePondStalker_BigFish_ClearSky;ThePondStalker_BigFish_ClearSky",
	"PondStalker_ClearSky;ThePondStalker_CannedFood_ClearSky;ThePondStalker_CannedFood_ClearSky",
	"PondStalker_ClearSky;ThePondStalker_Fish_ClearSky;ThePondStalker_Fish_ClearSky",
	"PondStalker_ClearSky;ThePondStalker_ForagedFood_ClearSky;ThePondStalker_ForagedFood_ClearSky",
	"PondStalker_ClearSky;ThePondStalker_Medicine_ClearSky;ThePondStalker_Medicine_ClearSky",
	"PondStalker_ClearSky;ThePondStalker_Pistol_ClearSky;ThePondStalker_Pistol_ClearSky",
	"PondStalker_ClearSky;ThePondStalker_Rifle_ClearSky;ThePondStalker_Rifle_ClearSky",
	"PondStalker_ClearSky;ThePondStalker_Shotgun_ClearSky;ThePondStalker_Shotgun_ClearSky",
	"PondStalker_ClearSky;ThePondStalker_SkillBook_ClearSky;ThePondStalker_SkillBook_ClearSky",
	"PondStalker_ClearSky;ThePondStalker_Zombies_ClearSky;ThePondStalker_Zombies_ClearSky",
	},
}

SFQuest_Database.RandomEventPool.ExamplePool = ExamplePool;

]]--


--------------------------------------------------------------------------------------------------------------------------------------------------
--[[ SECTION : EVENTOS DIARIOS

	TRABAJO EN CURSO

	Así se inserta un evento diario:
table.insert(SFQuest_Database.DailyEventPool, { dailycode = "Example_DailyCode", condition = "notmaxedwithcode;QuestDailyCodeForThisPool;1", commands = "randomcodedworldfrompool;PondStalker_Bandits;ThePondStalker;Bandits", days = 0, frequency = 12});

]]--