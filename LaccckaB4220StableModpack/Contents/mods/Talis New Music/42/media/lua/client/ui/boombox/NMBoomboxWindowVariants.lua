local env = _G.NMBoomboxWindowEnv
setfenv(1, env)

function normalizeBoomboxVariantToken(rawToken)
    local text = tostring(rawToken or "")
    local token = text:match("Boombox([A-Za-z]+)$")
        or text:match("NM_Boombox([A-Za-z]+)$")
        or text:match("World_NM_Boombox([A-Za-z]+)$")
        or text:match("^([A-Za-z]+)$")
    token = tostring(token or "")
    if token == "" then
        return nil
    end
    return token
end

function resolveBoomboxVariantFromItem(item)
    if not item then
        return BOOMBOX_UI_VARIANT_FALLBACK
    end
    if item.getScriptItem then
        local okScript, scriptItem = pcall(item.getScriptItem, item)
        if okScript and scriptItem then
            if scriptItem.getWorldStaticModel then
                local okWorld, token = pcall(scriptItem.getWorldStaticModel, scriptItem)
                local variant = okWorld and normalizeBoomboxVariantToken(token) or nil
                if variant then return variant end
            end
            if scriptItem.getStaticModel then
                local okStatic, token = pcall(scriptItem.getStaticModel, scriptItem)
                local variant = okStatic and normalizeBoomboxVariantToken(token) or nil
                if variant then return variant end
            end
        end
    end
    if item.getWorldStaticModel then
        local okWorld, token = pcall(item.getWorldStaticModel, item)
        local variant = okWorld and normalizeBoomboxVariantToken(token) or nil
        if variant then return variant end
    end
    if item.getStaticModel then
        local okStatic, token = pcall(item.getStaticModel, item)
        local variant = okStatic and normalizeBoomboxVariantToken(token) or nil
        if variant then return variant end
    end
    if item.getFullType then
        local variant = normalizeBoomboxVariantToken(item:getFullType())
        if variant then return variant end
    end
    if item.getType then
        local variant = normalizeBoomboxVariantToken(item:getType())
        if variant then return variant end
    end
    return BOOMBOX_UI_VARIANT_FALLBACK
end

function getBoomboxUITexturesForVariant(variant)
    local token = normalizeBoomboxVariantToken(variant) or BOOMBOX_UI_VARIANT_FALLBACK
    local cached = BOOMBOX_UI_TEXTURES_BY_VARIANT[token]
    if cached and cached.front and cached.lid then
        return cached
    end
    local fallbackToken = BOOMBOX_UI_VARIANT_FALLBACK
    local resolveVariantTexture = function(prefix, variantToken)
        return getTexture and getTexture(prefix .. variantToken .. ".png") or nil
    end
    local resolveOddboltTexture = function(path, fallbackPath)
        local tex = getTexture and getTexture(path) or nil
        if tex or not fallbackPath then
            return tex
        end
        return getTexture and getTexture(fallbackPath) or nil
    end
    local front = resolveVariantTexture(FRONT_TEXTURE_PREFIX, token)
    local lid = resolveVariantTexture(LID_TEXTURE_PREFIX, token)
    if token ~= fallbackToken then
        front = front or resolveVariantTexture(FRONT_TEXTURE_PREFIX, fallbackToken)
        lid = lid or resolveVariantTexture(LID_TEXTURE_PREFIX, fallbackToken)
    end
    local textures = {
        variant = token,
        base = getTexture and getTexture(BASE_TEXTURE_PATH) or nil,
        front = front,
        lid = lid,
        lidEdge = getTexture and getTexture(LID_EDGE_TEXTURE_PATH) or nil,
        close = getTexture and getTexture(CLOSE_TEXTURE_PATH) or nil,
        powerBg = getTexture and getTexture(POWER_BG_TEXTURE_PATH) or nil,
        powerSlide = getTexture and getTexture(POWER_SLIDE_TEXTURE_PATH) or nil,
        buttonTop = getTexture and getTexture(BUTTON_TOP_TEXTURE_PATH) or nil,
        buttonBottom = getTexture and getTexture(BUTTON_BOTTOM_TEXTURE_PATH) or nil,
        buttonTopPrev = token == "Oddbolt"
            and resolveOddboltTexture("media/textures/UI/Boombox/NM_UI_Boombox_Button_Top_Oddbolt.png", BUTTON_TOP_TEXTURE_PATH)
            or (getTexture and getTexture(BUTTON_TOP_TEXTURE_PATH) or nil),
        buttonBottomPrev = token == "Oddbolt"
            and resolveOddboltTexture("media/textures/UI/Boombox/NM_UI_Boombox_Button_Bottom_Oddbolt.png", BUTTON_BOTTOM_TEXTURE_PATH)
            or (getTexture and getTexture(BUTTON_BOTTOM_TEXTURE_PATH) or nil),
        play = getTexture and getTexture(BUTTON_PLAY_TEXTURE_PATH) or nil,
        stop = getTexture and getTexture(BUTTON_STOP_TEXTURE_PATH) or nil,
        prev = token == "Oddbolt"
            and resolveOddboltTexture("media/textures/UI/Boombox/NM_UI_Boombox_Button_Prev_Oddbolt.png", BUTTON_PREV_TEXTURE_PATH)
            or (getTexture and getTexture(BUTTON_PREV_TEXTURE_PATH) or nil),
        next = getTexture and getTexture(BUTTON_NEXT_TEXTURE_PATH) or nil,
        eject = getTexture and getTexture(BUTTON_EJECT_TEXTURE_PATH) or nil,
        topPlay = getTexture and getTexture(BUTTON_TOP_PLAY_TEXTURE_PATH) or nil,
        topStop = getTexture and getTexture(BUTTON_TOP_STOP_TEXTURE_PATH) or nil,
        topPrev = token == "Oddbolt"
            and resolveOddboltTexture("media/textures/UI/Boombox/NM_UI_Boombox_Button_Top_Prev_Oddbolt.png", BUTTON_TOP_PREV_TEXTURE_PATH)
            or (getTexture and getTexture(BUTTON_TOP_PREV_TEXTURE_PATH) or nil),
        topNext = getTexture and getTexture(BUTTON_TOP_NEXT_TEXTURE_PATH) or nil,
        volumeBg = getTexture and getTexture(VOLUME_BG_TEXTURE_PATH) or nil,
        volumeKnob = token == "Oddbolt"
            and resolveOddboltTexture("media/textures/UI/Boombox/NM_UI_Boombox_Volume_Knob_Oddbolt.png", VOLUME_KNOB_TEXTURE_PATH)
            or (getTexture and getTexture(VOLUME_KNOB_TEXTURE_PATH) or nil),
        modeBg = getTexture and getTexture(MODE_BG_TEXTURE_PATH) or nil,
        modeButton = getTexture and getTexture(MODE_BUTTON_TEXTURE_PATH) or nil,
        spool = getTexture and getTexture(SPOOL_TEXTURE_PATH) or nil,
        modeIcons = {
            getTexture and getTexture(MODE_ICON_REPEAT_SONG) or getTexture and getTexture("UI_NM_RepeatSong") or nil,
            getTexture and getTexture(MODE_ICON_REPEAT_ALBUM) or getTexture and getTexture("UI_NM_RepeatAlbum") or nil,
            getTexture and getTexture(MODE_ICON_SHUFFLE) or getTexture and getTexture("UI_NM_Shuffle") or nil,
        }
    }
    BOOMBOX_UI_TEXTURES_BY_VARIANT[token] = textures
    return textures
end

function BoomboxWindow:getBoomboxVariant(resolved)
    local ctx = resolved or self:resolveContextCached()
    return resolveBoomboxVariantFromItem(ctx and ctx.item or nil)
end

function BoomboxWindow:resolveBoomboxUITextures(resolvedOrVariant)
    local variant = type(resolvedOrVariant) == "string" and resolvedOrVariant or self:getBoomboxVariant(resolvedOrVariant)
    return getBoomboxUITexturesForVariant(variant)
end
