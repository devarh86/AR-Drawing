package com.example.analytics

object Events {

    object ApiKeys {
        val AUTH_TOKEN = "auth_token_api"
        val FRAME = "frame_api"
        val MAIN = "main_api"
        val HOME = "home_api"
        val FEATURE = "feature_api"
        val BACKGROUNDS = "backgrounds_api"
        val FILTERS = "filters_api"
        val EFFECTS = "effects_api"
        val STICKERS = "stickers_api"
        val SEARCH_FRAME = "search_frame"
    }

    object ApiParams {
        val STATE = "state"
        val TIME = "time"
        val TOTAL_TIME_CONSUMED = "total_time_consumed"
        val INTERNET_STATE = "internet_state"
        val ERROR_MESSAGE = "error_message"
    }

    object ApiStates {
        val LOADING = "loading"
        val SUCCESS = "success"
        val EXCEPTION = "exception"
        val FAILED = "failed"
    }

    object NetworkKeys {
        val INTERNET = "internet"
    }

    object NetworkParams {
        val INTERNET_STATE = "internet_state"
    }

    object ApplicationKeys {
        val APPLICATION = "application"
    }

    object ApplicationParams {
        val APPLICATION_STATE = "application_state"
    }

    object ApplicationState {
        val RESUME = "resume"
        val PAUSE = "pause"
        val CREATE = "create"
        val DESTROY = "destroy"
    }

    object Screens {
        val MAIN = "main"
        val SETTING = "setting"
        val SPLASH = "splash"
        val HOME = "home"
        val PREMIUM_NEW = "premium_new"
        val CATEGORIES = "categories"
        val PROCESSING = "processing"
        val FEATURE = "feature"
        val STYLES = "styles"
        val TEMPLATE = "template"
        val SEARCH = "search"
        val SEARCH_FRAME = "search_frame"
        val TEMPLATE_BASE = "template_base"
        val MY_WORK = "mywork"
        val FAVOURITE = "favourite"
        val PREMIUM = "premium"
        val PREMIUM_SPLASH = "premium_splash"
        val PREMIUM_OFFER = "premium_offer"
        val PREMIUM_SHARE = "premium_share"
        val GALLERY = "gallery"
        val PRE_EDITOR = "pre_editor"
        val PHOTO_EDITOR = "photo_editor"
        val FRAME_EDITOR = "frame_editor"
        val PIP_EDITOR = "pip_editor"
        val BLEND_EDITOR = "blend_editor"
        val BLEND_EDITOR_CROPPER = "blend_editor_cropper"
        val COLLAGE = "collage"
        val SAVE_AND_SHARE = "save_and_share"
    }

    object SubScreens {
        val FOR_YOU = "new"
        val TODAY_SPECIAL = "top"
        val MOST_USED = "trending"
        val SEARCH = "search"
        val PRO = "pro"
        val SLIDER_MENU = "slider_menu"
        val RECENTLY_USED = "recently_used"
        val DRAFT = "draft"
        val SAVED = "saved"
        val LAYOUTS = "layouts"
        val BG = "bg"
        val BORDER_SIZE = "border_size"
        val BORDER_COLOR = "border_color"
    }

    object DIALOGS {
        val DOWNLOAD_FRAME = "download_frame"
        val DISCARD_DIALOG = "discard_dialog"
        val REWARDED_INTERSITIAL_FRAME = "rewarded_interstitial_frame"
        val REWARDED_REMOVE_WATERMARK = "rewarded_remove_watermark"
        val QUALITY = "quality"
        val REWARDED_FRAME = "rewarded_frame"
    }

    object ParamsKeys {
        val OPENING_SCREEN = "opening_screen"
        val SCREEN = "screen"
        val SUB_SCREEN = "sub_screen"
        val PARENT_SCREEN = "parent_screen"
        val DIALOG = "dialog"
        val ACTION = "action"
        val MESSAGE = "message"
        val FROM = "from"
        val BUTTON = "button"
        val SELECTED_IMAGES = "selected_images"
        val IS_FAVOURITE = "is_favourite"
        val FRAME_ID = "frame_id"
        val CATNAME = "category_name"
        val FRAME_NAME = "frame_name"
        val TAG_NAME = "tag_name"
        val IS_TAG_SELECTED = "is_tag_selected"
        val CROSS_PROMO_AD_TITLE = "cross_promo_ad_title"
        val CROSS_PROMO_AD_PLACEMENT = "cross_promo_ad_placement"
        val CROSS_PROMO_AD_TYPE = "cross_promo_ad_type"
        val COLLAGE_POSITION = "collage_position"
        val COLOR_ALPHA = "color_alpha"
        val CORNER_RADIUS = "corner_radius"
        val PADDING = "padding"
        val BLUR = "blur"
        val BORDER_SIZE = "border_size"
        val CATEGORY_NAME = "category_name"
        val IS_APPLY_ALL_SELECTED = "is_apply_all_selected"
        val FILTER_ID = "filter_id"
        val FILTER_NAME = "filter_name"
        val FONT_ID = "font_id"
        val FONT_NAME = "font_name"
        val STICKER_ID = "sticker_id"
        val STICKER_NAME = "sticker_name"
        val ROTATION = "rotation"
        val OBSERVER_STATE = "observer_state"
    }

    object ParamsValues {
        val CLICKED = "clicked"
        val ERROR = "error"
        val DISPLAYED = "displayed"
        val PROCESSING_COMPLETE = "processing_complete"
        val FAVOURITE = "favourite"
        val RECYCLER_VIEW = "rv_list"
        val TAG_RECYCLER_VIEW = "rv_tag_list"
        val STYLE_TAG_RECYCLER_VIEW = "rv_style_tag_list"
        val SEARCH_TAG_RECYCLER_VIEW = "rv_search_tag_list"
        val SEARCH_TAG_RECENT = "search_recent_tag_list"
        val ALBUMS_RECYCLER_VIEW = "rv_albums_list"
        val DISMISSED = "dismissed"
        val PRO = "pro"
        val GIFT = "gift"
        val ROBO_OPEN = "robo_open"
        val PRIVACY_POLICY = "privacy_policy"
        val TERM_OF_USE = "term_of_use"
        val BACK = "back"
        val NEXT = "next"
        val DELETE = "delete"
        val SEEALL = "see_all"
        val SHARE = "share"
        val REMOVE = "remove"
        val CLOSE = "close"
        val DISCARD = "discard"
        val DRAFT = "draft"
        val TICK = "tick"
        val BOTTOM_MENU = "bottom_menu"
        val SAVE = "save"
        val CONTINUE = "continue"

        object FrameEditorScreen {
            val REPLACE_IMAGE = "replace_image"
            val FRAMES = "frames"
            val EFFECTS = "effects"
            val TEXT = "text"
            val FILTERS = "filters"
            val STICKERS = "stickers"
            val USER_GUIDE = "user_guide"
        }

        object PreEditorScreen {
            val EDIT = "edit"
        }

        object AdjustmentScreen {
            val RESET = "reset"
        }

        object RotateScreen {
            val RESET = "reset"
            val ROTATE_90 = "rotate_90"
        }

        object BottomMenu {
            val REPLACE_IMAGE = "replace_image"
            val FILTERS = "filters"
            val ADJUSTMENT = "adjustment"
            val ROTATE = "rotate"
            val VERTICAL = "vertical"
            val HORIZONTAL = "horizontal"
            val CROP = "crop"
        }

        object SaveAndShareScreen {
            val SHARE = "share"
            val HOME = "home"
        }

        object SaveScreen {
            val LOW = "low"
            val MEDIUM = "medium"
            val HIGH = "high"
            val UNLOCK = "unlock"
            val SAVE_AND_CONTINUE = "save_and_continue"
        }

        object TextScreen {
            val EDIT_TEXT = "edit_text"
            val FONTS = "fonts"
            val FONTS_COLOR = "fonts_color"
            val FONTS_BG = "fonts_bg"
        }

        object FilterScreen {
            val APPLY_ALL = "apply_all"
        }

        object CollageScreen {
            val LAYOUTS = "layouts"
            val BACKGROUNDS = "backgrounds"
            val TEXT = "text"
            val FILTERS = "filters"
            val STICKERS = "stickers"
            val NEXT = "next"
            val USER_GUIDE = "user_guide"
        }

        object GalleryScreen {
            val CAMERA = "camera"
        }

        object FavouriteScreen {
            val TRY_NOW = "try_now"
        }

        object MyWorkScreen {
            val TRY_NOW = "try_now"
        }

        object FeatureScreen {
            val SEARCH = "search"
        }

        object HomeScreen {

            val SEAMLESS = "seamless"
            val DRAWING = "ar_drawing"
            val SKETCH = "photo_sketch"
            val IMPORT_GALLERY = "import_gallery"
            val TAPE = "tape"
            val PAPER = "paper"
            val REWIND = "rewind"
            val FILM = "film"
            val PLASTIC = "plastic"
            val STORIES = "stories"
            val Templates = "templates"
            val SLIDER_MENU = "slider_menu"
            val SOLO = "solo"
            val SCRL = "scrl"
            val COLLAGE_FRAMES = "collage_frames"
            val DUAL = "dual"
            val MULTIPLEX = "multiplex"
            val PIP = "pip"
            val NEON = "neon"
            val BG_ART = "bg_art"
            val DOUBLE_EXPOSURE = "double_exposure"
            val Overlay = "overlay"
            val SPIRAL = "spiral"
            val DRIP_ART = "drip_art"
            val BLEND = "blend"
            val EFFECT = "effects"
            val PROFILE_PICTURE = "profile_picture"
            val TOP_PICK = "top_pick"
            val COLLAGE = "collage"
            val PHOTO_EDITOR = "photo_editor"
            val AI_ENHANCER = "ai_enhancer"
            val MULTI_FIT = "multi_fit"
            val STITCH = "stitch"
        }

        object ProScreen {
            val CLOSE = "close"
            val CANCEL_PRO = "cancel_pro"
            val CONTINUE_PURCHASE = "continue_purchase"
            val GOOGLE_PLAY_STORE = "google_play_store"
            val MONTHLY_PLAN = "monthly_plan"
            val QUATERLY_PLAN = "quaterly_plan"
            val YEARLY_PLAN = "yearly_plan"
        }

        object MainScreen {
            val SHARE = "share"
            val RATE_US = "rate_us"
            val LANGUAGE = "language"
        }


    }
}