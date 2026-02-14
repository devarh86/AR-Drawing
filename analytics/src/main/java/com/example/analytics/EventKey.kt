package com.example.analytics

enum class EventKey(val eventName: String) {

    BTM_NAV_FEATURED_SCREEN("ftrd_btn"),
    BTM_NAV_HOME_SCREEN("home_btn"),
    BTM_NAV_MY_FRAMES_SCREEN("Myframes_btn"),
    BTM_NAV_PRO_SCREEN("pro_btn"),
    HOME_SOLO_BUTTON("home_solo_btn"),
    HOME_DUAL_BUTTON("home_dual_btn"),
    HOME_MULTI_BUTTON("home_multi_btn"),
    HOME_COLLAGE_BUTTON("home_colg_btn"),
    HOME_PIP_BUTTON("home_pip_btn"),

    HOME_SLIDER("main_slider"),
    SOLO_SLIDER("solo_slider_btn"),
    DUAL_SLIDER("dual_slider_btn"),
    PIP_SLIDER("pip_slider_btn"),
    MULTI_SLIDER("multi_slider_btn"),

    DRAWER_SHARE("main_slider"),
    DRAWER_RATE_US("slider_rate_us"),
    SETTING_PRO_VERSION("in_app_set"),
    DRAWER_PRIVACY("slider_privacy"),

    FEATURED_SCREEN("ftrd_default"),
    FEATURED_FRAME_SELECT("(catg)_ftrd_frm_select"),
    FEATURED_FRAME_DISCARD("(catg)_ftrd_frm_discard"),
    FEATURED_FRAME_SELECT_ASSET_ID("(catg)_ftrd_frm_select_(id)"),
    FEATURED_FRAME_PRE_EDIT("(catg)_ftrd_frm_pre_edit_scrn"),
    FEATURED_FRAME_PRE_EDIT_SAVE("(catg)_ftrd_frm_pre_edit_save"),
    FEATURED_FRAME_PRE_EDIT_SAVE_ID("(catg)_ftrd_frm_pre_edit_save_(id)"),
    FEATURED_EDITOR("(catg)_ftrd_frm_editor"),
    FEATURED_EDITOR_SAVE("(catg)_ftrd_frm_editor_save"),
    FEATURED_EDITOR_SAVE_ASSET_ID("(catg)_ftrd_frm_editor_save_(id)"),
    FEATURE_PRE_EDIT_SHARE_SCREEN("(catg)_ftrd_frm_pre_edit_share_scrn"),
    FEATURE_EDITOR_SHARE_SCREEN("(catg)_ftrd_frm_editor_share_scrn"),

    IMAGE_FEATURED_SELECT("(catg)_ftrd_img_sel"),
    IMAGE_HOME_SELECT("(catg)_home_img_sel"),
    IMAGE_SELECT("(catg)_home_img_sel"),
    SEE_ALL_IMAGE_SELECT("home_(catg)_list_img_sel"),


    HOME_FRAME_SELECT("(catg)_home_frm_select"),
    HOME_FRAME_SELECT_ASSET_ID("(catg)_home_frm_select_(id)"),
    HOME_FRAME_PRE_EDIT_SCREEN("(catg)_home_frm_pre_edit_scrn"),
    HOME_PRE_EDIT_SAVE("(catg)_home_frm_pre_edit_save"),
    HOME_PRE_EDIT_DISCARD("(catg)_home_frm_pre_edit_discard"),
    HOME_EDIT_DISCARD("(catg)_home_frm_edit_discard"),
    HOME_PRE_EDIT_SAVE_ID("(catg)_home_frm_pre_edit_save_(id)"),
    HOME_EDITOR("(catg)_home_frm_editor"),
    HOME_EDITOR_SAVE("(catg)_home_frm_editor_save"),
    HOME_EDITOR_SAVE_ASSET_ID("(catg)_home_frm_editor_save_(id)"),
    HOME_PRE_EDIT_SHARE_SCREEN("(catg)_home_frm_pre_edit_share_scrn"),
    HOME_EDITOR_SHARE_SCREEN("(catg)_home_frm_editor_share_scrn"),

    SOLO_SEE_ALL("solo_list"),
    DUAL_SEE_ALL("dual_list"),
    PIP_SEE_ALL("pip_list"),
    M_PLEX_SEE_ALL("mltplx_list"),
    Collage_ALL("collage_list"),

    LIST_FRAME_SELECT("(catg)_list_frm_select"),
    LIST_FRAME_SELECT_ASSET_ID("(catg)_list_frm_select_(id)"),
    LIST_PRE_EDIT_SCREEN("(catg)_list_frm_pre_edit_scrn"),
    LIST_PRE_EDIT_SAVE("(catg)_list_frm_pre_edit_save"),
    LIST_EDITOR("(catg)_list_frm_editor"),
    LIST_EDITOR_SAVE("(catg)_list_frm_editor_save"),
    LIST_EDITOR_SAVE_ASSET_ID("(catg)_list_frm_editor_save_(id)"),
    LIST_PRE_EDIT_SHARE_SCREEN("(catg)_list_frm_pre_edit_share_scrn"),
    LIST_EDITOR_SHARE_SCREEN("(catg)_list_frm_editor_share_scrn"),

    ONLINE_FRAME_SELECT("home_(catg)_frm_select"),
    ONLINE_LIST_FRAME_SELECT("home_(catg)_list_frm_select"),
    ONLINE_FRAME_SELECT_ASSET_ID("home_(catg)_frm_select_(id)"),
    ONLINE_LIST_FRAME_SELECT_ASSET_ID("home_(catg)_list_frm_select_(id)"),

    ONLINE_FRAME_PRE_EDIT("home_(catg)_pre_edtr"),
    ONLINE_FRAME_PRE_EDIT_SAVE("home_(catg)_pre_save"),
    ONLINE_FRAME_EDIT_DISCARD("home_(catg)_edit_discard"),
    ONLINE_FRAME_PRE_EDIT_DISCARD("home_(catg)_pre_edit_discard"),
    ONLINE_FRAME_PRE_EDIT_SAVE_ID("home_(catg)_pre_save_(id)"),
    ONLINE_FRAME_PRE_EDIT_EDITOR("home_(catg)_edtr_scrn"),
    ONLINE_FRAME_EDITOR_SAVE("home_(catg)_editor_save"),
    ONLINE_FRAME_EDITOR_SAVE_ASSET_ID("home_(catg)_editor_save_(id)"),

    ONLINE_PRE_EDIT_SHARE_SCREEN("home_(catg)_pre_share_scrn"),
    ONLINE_EDITOR_SHARE_SCREEN("home_(catg)_share_scrn"),

    ACTIVITY_SAVE_SHARE_SCREEN("(catg)_share_scrn"),
    SAVE_SHARE_HOME("(catg)_share_scrn_home"),
    SAVE_SHARE_BACK_EDITOR("(catg)_share_scrn_back"),
    SAVE_SHARE_F_BOOK("(catg)_share_scrn_fb"),
    SAVE_SHARE_W_APP("(catg)_share_scrn_wa"),
    SAVE_SHARE_T_WITTER("(catg)_share_scrn_twt"),
    SAVE_SHARE_MAIL("(catg)_share_scrn_mail"),

    MY_WORK_SCREEN("my_work_scrn"),
    MY_WORK_BACK("my_work_scrn_back"),
    MY_WORK_DELETE("my_work_scrn_dell"),
    MY_WORK_SCREEN_VIEW("my_work_scrn_view"),

    TEXT("(catg)_editor_text"),
    TEXT_CANCEL_CROSS("(catg)_editor_text_keyboard"),
    TEXT_DONE("(catg)_editor_text_keyboard_done"),
    TEXT_ACTIVITY_CLOSE("(catg)_editor_text_keyboard_close"),

    REPLACE_PHOTO("(catg)_editor_replace_photo"),
    REPLACE_PHOTO_NEXT("(catg)_editor_replace_photo_done"),
    REPLACE_PHOTO_BACK("(catg)_editor_replace_photo_back"),

    FRAMES("(catg)_editor_frames"),
    FRAME_REPLACED("(catg)_editor_frame_replaced"),
    FRAME_REPLACED_ASSET_ID("(catg)_editor_frame_(id)"),
    FRAME_REPLACED_BACK("(catg)_editor_frame_back"),

    FILTER_EVENT("(catg)_editor_fltr"),
    FILTER_SELECTION("(catg)_editor_fltr_slct"),
    FILTER_SELECTION_ASSET_ID("(catg)_editor_fltr_slct_(id)"),
    FILTER_SELECTION_TICK("(catg)_editor_fltr_done"),
    FILTER_SELECTION_CROSS("(catg)_editor_fltr_cancel"),


    STICKER_EVENT("(catg)_editor_stckr"),
    STICKER_SELECTION("(catg)_editor_stckr_slct"),
    STICKER_SELECTION_ASSET_ID("(catg)_editor_stckr_slct_(id)"),
    STICKER_SELECTION_TICK("(catg)_editor_stckr_done"),
    STICKER_SELECTION_CROSS("(catg)_editor_stckr_cancel"),

    BLUR("(catg)_editor_blur"),

    TOOLS("(catg)_editor_tool"),
    TOOLS_APPLY("(catg)_editor_tool_apply"),
    TOOLS_BACK("(catg)_editor_tool_back"),

    TOOLS_CROP("(catg)_editor_tool_crop"),
    TOOLS_CROP_APPLY("(catg)_editor_tool_crop_apply"),
    TOOLS_CROP_BACK("(catg)_editor_tool_crop_back"),

    TOOLS_ROTATE("(catg)_editor_tool_rotate"),
    TOOLS_ROTATE_APPLY("(catg)_editor_tool_rotate_apply"),
    TOOLS_ROTATE_BACK("(catg)_editor_tool_rotate_back"),

    TOOLS_FLIP("(catg)_editor_tool_flip"),
    TOOLS_FLIP_APPLY("(catg)_editor_tool_flip_apply"),
    TOOLS_FLIP_BACK("(catg)_editor_tool_flip_back"),

    SPLASH_LOADING("splash"),
    MAIN_SCREEN_PREVIEW("main_screen_preview"),

    ALLOW_PERM("allow_perm"),
    DENY_PERM("deny_perm"),


    EVENT_BANNER_PRO_OFFER_DISPLAY("_pro_banner_dsply"),
    EVENT_BANNER_PRO_OFFER_CLICK("_pro_banner_go_pro"),
    EVENT_BANNER_PRO_OFFER_CANCEL("_pro_banner_cancl"),
    EVENT_BUMPER_PRO_OFFER_DISPLAY("pro_bumper_dsply"),
    EVENT_BUMPER_PRO_OFFER_CLICK("pro_bumper_go_pro"),

    EVENT_MONTHLY_PANEL_OPEN("monthly_panel_open"),
    EVENT_WEEKLY_PANEL_OPEN("weekly_panel_open"),
    EVENT_PRO_SCREEN_WEEKLY("weekly_btn_click"),
    EVENT_PRO_SCREEN_MONTHLY("monthly_btn_click"),





}