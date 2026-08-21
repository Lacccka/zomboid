/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

public enum AiConfigOptions {
    PP_SBBC_MAX_BONES("PP_SBBC_MAX_BONES"),
    PP_CT_MAX_SMOOTHING_ANGLE("PP_CT_MAX_SMOOTHING_ANGLE"),
    PP_CT_TEXTURE_CHANNEL_INDEX("PP_CT_TEXTURE_CHANNEL_INDEX"),
    PP_GSN_MAX_SMOOTHING_ANGLE("PP_GSN_MAX_SMOOTHING_ANGLE"),
    IMPORT_MDL_COLORMAP("IMPORT_MDL_COLORMAP"),
    PP_RRM_EXCLUDE_LIST("PP_RRM_EXCLUDE_LIST"),
    PP_PTV_KEEP_HIERARCHY("PP_PTV_KEEP_HIERARCHY"),
    PP_PTV_NORMALIZE("PP_PTV_NORMALIZE"),
    PP_FD_REMOVE("PP_FD_REMOVE");

    private final String m_name;

    private AiConfigOptions(String string2) {
        this.m_name = string2;
    }
}

