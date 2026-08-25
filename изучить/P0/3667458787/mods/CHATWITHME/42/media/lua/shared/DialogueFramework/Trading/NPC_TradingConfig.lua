local NPC_TradingConfig = {}

NPC_TradingConfig.TRADING_SYSTEM = {
    SIMPLE = 1,
    VALUE_BASED = 2
}

NPC_TradingConfig.SETTINGS = {
    ACTIVE_SYSTEM = NPC_TradingConfig.TRADING_SYSTEM.SIMPLE,

    SCAN_INTERVAL_MINUTES = 10,
    BACKPACK_ITEM_TYPE = "Base.Bag_Military",

    SIMPLE_UI_WIDTH = 500,
    SIMPLE_UI_HEIGHT = 200,
    VALUE_UI_WIDTH = 600,
    VALUE_UI_HEIGHT = 800
}

NPC_TradingConfig.TIMING = {
    GATHER_ITEMS_DURATION = 150,
    RECEIVE_REWARD_DURATION = 100,
    ADD_TO_BACKPACK_DURATION = 50,
    COMPLETE_TRADE_DURATION = 150
}

function NPC_TradingConfig.getActiveSystem()
    return NPC_TradingConfig.SETTINGS.ACTIVE_SYSTEM
end

function NPC_TradingConfig.isSimpleSystemActive()
    return NPC_TradingConfig.SETTINGS.ACTIVE_SYSTEM == NPC_TradingConfig.TRADING_SYSTEM.SIMPLE
end

function NPC_TradingConfig.isValueSystemActive()
    return NPC_TradingConfig.SETTINGS.ACTIVE_SYSTEM == NPC_TradingConfig.TRADING_SYSTEM.VALUE_BASED
end

function NPC_TradingConfig.setActiveSystem(system)
    if system == 1 or system == 2 then
        NPC_TradingConfig.SETTINGS.ACTIVE_SYSTEM = system
        return true
    end
    return false
end

function NPC_TradingConfig.getSimpleUIWidth()
    return NPC_TradingConfig.SETTINGS.SIMPLE_UI_WIDTH
end

function NPC_TradingConfig.getSimpleUIHeight()
    return NPC_TradingConfig.SETTINGS.SIMPLE_UI_HEIGHT
end

function NPC_TradingConfig.getValueUIWidth()
    return NPC_TradingConfig.SETTINGS.VALUE_UI_WIDTH
end

function NPC_TradingConfig.getValueUIHeight()
    return NPC_TradingConfig.SETTINGS.VALUE_UI_HEIGHT
end

function NPC_TradingConfig.getScanIntervalMinutes()
    return NPC_TradingConfig.SETTINGS.SCAN_INTERVAL_MINUTES
end

function NPC_TradingConfig.getBackpackItemType()
    return NPC_TradingConfig.SETTINGS.BACKPACK_ITEM_TYPE
end

return NPC_TradingConfig
