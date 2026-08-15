require "BodyLocations"

local group = BodyLocations.getGroup("Human")

----НОГИ: ----- 														- ВСЕ РЕНДЕРИТСЯ, ВСЕ ИМЕЕТ АЛЬТ МОДЕЛЬКИ
	-- THIGH_LEFT (base:thigh_left)		THIGH_RIGHT (base:thigh_right)	- ляшка
	-- KNEE_LEFT 						KNEE_RIGHT						- коленко
	-- CALF_LEFT (base:calf_left) 		CALF_RIGHT (base:calf_right)	- голень


---- РУКИ: -----
	-- SPORT_SHOULDERPAD							-- Бесполезное ванильное гавно
	-- SPORT_SHOULDERPAD_ON_TOP						-- Это все спортивные регби наплечники
	-- SHOULDERPAD_LEFT	(base:shoulderpadleft)		-- Заменяют рюкзак и большую часть верхних вещей!
	-- SHOULDERPAD_RIGHT (base:shoulderpadright)	-- Меняем на свое:
	group:getOrCreateLocation(zReBodyLocation.ZOMBIEREENGINE_SHOULDER_LEFT); 	-- ПЛЕЧО L		-- zombieREengine:shoulder_left
	group:getOrCreateLocation(zReBodyLocation.ZOMBIEREENGINE_SHOULDER_RIGHT); 	-- ПЛЕЧО R		-- zombieREengine:shoulder_right

	-- ELBOW_LEFT (base:elbow_left) 		ELBOW_RIGHT (base:elbow_right) 		-- НАЛОКОТНИКИ  -- Рендерится, имеют альт модельки, не конфликтуют.

	-- FORE_ARM_LEFT (base:forearm_left) 	FORE_ARM_RIGHT (base:forearm_right) -- ПРЕДПЛЕЧЬЯ, не рендерится под курткой, меняем на свое:
	group:getOrCreateLocation(zReBodyLocation.ZOMBIEREENGINE_FOREARM_LEFT); 	-- ПРЕДПЛЕЧЬЕ, c рендером	-- zombieREengine:forearm_left
	group:getOrCreateLocation(zReBodyLocation.ZOMBIEREENGINE_FOREARM_RIGHT); 	-- ПРЕДПЛЕЧЬЕ, c рендером	-- zombieREengine:forearm_right


---- ПРОЧЕЕ: -----	
	group:getOrCreateLocation(zReBodyLocation.ZOMBIEREENGINE_FANNYPACKBACK);	-- СУМКА НА ЖОПЕ, с рендером	-- zombieREengine:fannypack_back

	group:getOrCreateLocation(zReBodyLocation.ZOMBIEREENGINE_HIPBAG);			-- НАБЕДРЕННАЯ СУМКА 	-- zombieREengine:hipbag
	
	group:getOrCreateLocation(zReBodyLocation.ZOMBIEREENGINE_MASK);				-- БАЛАКЛАВА			-- zombieREengine:mask
	
	group:getOrCreateLocation(zReBodyLocation.ZOMBIEREENGINE_EXOSKELET);		-- ЭКЗОСКЕЛЕТ			-- zombieREengine:exoskelet


---- АЛЬТ МОДЕЛИ: -----	
	group:setAltModel(ItemBodyLocation.JACKET, zReBodyLocation.ZOMBIEREENGINE_SHOULDER_LEFT)
	group:setAltModel(ItemBodyLocation.JACKET_SUIT, zReBodyLocation.ZOMBIEREENGINE_SHOULDER_LEFT)
	group:setAltModel(ItemBodyLocation.JACKET_BULKY, zReBodyLocation.ZOMBIEREENGINE_SHOULDER_LEFT)
	group:setAltModel(ItemBodyLocation.JACKET_HAT_BULKY, zReBodyLocation.ZOMBIEREENGINE_SHOULDER_LEFT)
	group:setAltModel(ItemBodyLocation.BOILERSUIT, zReBodyLocation.ZOMBIEREENGINE_SHOULDER_LEFT)
	group:setAltModel(ItemBodyLocation.FULL_SUIT, zReBodyLocation.ZOMBIEREENGINE_SHOULDER_LEFT)
	group:setAltModel(ItemBodyLocation.FULL_SUIT_HEAD, zReBodyLocation.ZOMBIEREENGINE_SHOULDER_LEFT)
	group:setAltModel(ItemBodyLocation.BATH_ROBE, zReBodyLocation.ZOMBIEREENGINE_SHOULDER_LEFT)
	group:setAltModel(ItemBodyLocation.SWEATER, zReBodyLocation.ZOMBIEREENGINE_SHOULDER_LEFT)
	group:setAltModel(ItemBodyLocation.SWEATER_HAT, zReBodyLocation.ZOMBIEREENGINE_SHOULDER_LEFT)
	group:setAltModel(ItemBodyLocation.JERSEY, zReBodyLocation.ZOMBIEREENGINE_SHOULDER_LEFT)

	group:setAltModel(ItemBodyLocation.JACKET, zReBodyLocation.ZOMBIEREENGINE_SHOULDER_RIGHT)
	group:setAltModel(ItemBodyLocation.JACKET_SUIT, zReBodyLocation.ZOMBIEREENGINE_SHOULDER_RIGHT)
	group:setAltModel(ItemBodyLocation.JACKET_BULKY, zReBodyLocation.ZOMBIEREENGINE_SHOULDER_RIGHT)
	group:setAltModel(ItemBodyLocation.JACKET_HAT_BULKY, zReBodyLocation.ZOMBIEREENGINE_SHOULDER_RIGHT)
	group:setAltModel(ItemBodyLocation.BOILERSUIT, zReBodyLocation.ZOMBIEREENGINE_SHOULDER_RIGHT)
	group:setAltModel(ItemBodyLocation.FULL_SUIT, zReBodyLocation.ZOMBIEREENGINE_SHOULDER_RIGHT)
	group:setAltModel(ItemBodyLocation.FULL_SUIT_HEAD, zReBodyLocation.ZOMBIEREENGINE_SHOULDER_RIGHT)
	group:setAltModel(ItemBodyLocation.BATH_ROBE, zReBodyLocation.ZOMBIEREENGINE_SHOULDER_RIGHT)
	group:setAltModel(ItemBodyLocation.SWEATER, zReBodyLocation.ZOMBIEREENGINE_SHOULDER_RIGHT)
	group:setAltModel(ItemBodyLocation.SWEATER_HAT, zReBodyLocation.ZOMBIEREENGINE_SHOULDER_RIGHT)
	group:setAltModel(ItemBodyLocation.JERSEY, zReBodyLocation.ZOMBIEREENGINE_SHOULDER_RIGHT)

	group:setAltModel(ItemBodyLocation.JACKET, zReBodyLocation.ZOMBIEREENGINE_FOREARM_LEFT)
	group:setAltModel(ItemBodyLocation.JACKET_SUIT, zReBodyLocation.ZOMBIEREENGINE_FOREARM_LEFT)
	group:setAltModel(ItemBodyLocation.JACKET_BULKY, zReBodyLocation.ZOMBIEREENGINE_FOREARM_LEFT)
	group:setAltModel(ItemBodyLocation.JACKET_HAT_BULKY, zReBodyLocation.ZOMBIEREENGINE_FOREARM_LEFT)
	group:setAltModel(ItemBodyLocation.BOILERSUIT, zReBodyLocation.ZOMBIEREENGINE_FOREARM_LEFT)
	group:setAltModel(ItemBodyLocation.FULL_SUIT, zReBodyLocation.ZOMBIEREENGINE_FOREARM_LEFT)
	group:setAltModel(ItemBodyLocation.FULL_SUIT_HEAD, zReBodyLocation.ZOMBIEREENGINE_FOREARM_LEFT)
	group:setAltModel(ItemBodyLocation.BATH_ROBE, zReBodyLocation.ZOMBIEREENGINE_FOREARM_LEFT)
	group:setAltModel(ItemBodyLocation.SWEATER, zReBodyLocation.ZOMBIEREENGINE_FOREARM_LEFT)
	group:setAltModel(ItemBodyLocation.SWEATER_HAT, zReBodyLocation.ZOMBIEREENGINE_FOREARM_LEFT)
	group:setAltModel(ItemBodyLocation.JERSEY, zReBodyLocation.ZOMBIEREENGINE_FOREARM_LEFT)

	group:setAltModel(ItemBodyLocation.JACKET, zReBodyLocation.ZOMBIEREENGINE_FOREARM_RIGHT)
	group:setAltModel(ItemBodyLocation.JACKET_SUIT, zReBodyLocation.ZOMBIEREENGINE_FOREARM_RIGHT)
	group:setAltModel(ItemBodyLocation.JACKET_BULKY, zReBodyLocation.ZOMBIEREENGINE_FOREARM_RIGHT)
	group:setAltModel(ItemBodyLocation.JACKET_HAT_BULKY, zReBodyLocation.ZOMBIEREENGINE_FOREARM_RIGHT)
	group:setAltModel(ItemBodyLocation.BOILERSUIT, zReBodyLocation.ZOMBIEREENGINE_FOREARM_RIGHT)
	group:setAltModel(ItemBodyLocation.FULL_SUIT, zReBodyLocation.ZOMBIEREENGINE_FOREARM_RIGHT)
	group:setAltModel(ItemBodyLocation.FULL_SUIT_HEAD, zReBodyLocation.ZOMBIEREENGINE_FOREARM_RIGHT)
	group:setAltModel(ItemBodyLocation.BATH_ROBE, zReBodyLocation.ZOMBIEREENGINE_FOREARM_RIGHT)
	group:setAltModel(ItemBodyLocation.SWEATER, zReBodyLocation.ZOMBIEREENGINE_FOREARM_RIGHT)
	group:setAltModel(ItemBodyLocation.SWEATER_HAT, zReBodyLocation.ZOMBIEREENGINE_FOREARM_RIGHT)
	group:setAltModel(ItemBodyLocation.JERSEY, zReBodyLocation.ZOMBIEREENGINE_FOREARM_RIGHT)


---- ВЗАИМОИСКЛЮЧЕНИЯ: -----
	group:setExclusive(zReBodyLocation.ZOMBIEREENGINE_SHOULDER_LEFT, ItemBodyLocation.SHOULDERPAD_LEFT);	-- Ванильные наплечники против наших.
	group:setExclusive(zReBodyLocation.ZOMBIEREENGINE_SHOULDER_LEFT, ItemBodyLocation.SPORT_SHOULDERPAD);	-- Ванильные наплечники против наших.
	group:setExclusive(zReBodyLocation.ZOMBIEREENGINE_SHOULDER_LEFT, ItemBodyLocation.SPORT_SHOULDERPAD_ON_TOP);	-- Ванильные наплечники против наших.
	
	group:setExclusive(zReBodyLocation.ZOMBIEREENGINE_SHOULDER_RIGHT, ItemBodyLocation.SHOULDERPAD_RIGHT);	-- Ванильные наплечники против наших.
	group:setExclusive(zReBodyLocation.ZOMBIEREENGINE_SHOULDER_RIGHT, ItemBodyLocation.SPORT_SHOULDERPAD);	-- Ванильные наплечники против наших.
	group:setExclusive(zReBodyLocation.ZOMBIEREENGINE_SHOULDER_RIGHT, ItemBodyLocation.SPORT_SHOULDERPAD_ON_TOP);	-- Ванильные наплечники против наших.
	
	group:setExclusive(zReBodyLocation.ZOMBIEREENGINE_FOREARM_LEFT, ItemBodyLocation.FORE_ARM_LEFT);	-- Ванильные предплечья против наших.
	group:setExclusive(zReBodyLocation.ZOMBIEREENGINE_FOREARM_RIGHT, ItemBodyLocation.FORE_ARM_RIGHT);	-- Ванильные предплечья против наших.
	
	group:setExclusive(zReBodyLocation.ZOMBIEREENGINE_FANNYPACKBACK, ItemBodyLocation.FANNY_PACK_BACK);	-- Ванильная нажопная сумка против нашей.