package com.project.common.states

import com.project.common.db_table.FrameChildImageStickerModel
import com.project.common.db_table.FrameChildImagesModel
import com.project.common.db_table.FrameChildTextStickerModel
import com.project.common.db_table.FrameParentModel
import com.project.common.model.ImagesModel

sealed class MultiFitStitchViewState {
    object Idle : MultiFitStitchViewState()
    object Loading : MultiFitStitchViewState()
    object SaveLoading : MultiFitStitchViewState()
    object SaveComplete : MultiFitStitchViewState()
    object Success : MultiFitStitchViewState()
    data class  BackgroundTick(val backgroundState: BackgroundState?) : MultiFitStitchViewState()
    object Tick : MultiFitStitchViewState()
    object Back : MultiFitStitchViewState()
    class UpdateCollageTemplate(var collageDraftFrame: FrameParentModel) : MultiFitStitchViewState()
    class UpdateMultiFitStitch(
        var drawable: Any,
        var isShape: Boolean = false,
        var isDraft: Boolean = false,
        var ratio: String = ""
    ) : MultiFitStitchViewState()

    class SetDimensionRatio(var ratio: String) : MultiFitStitchViewState()


    class UpdateImage(
        var x: Float = 0f,
        var y: Float = 0f,
        var width: Int = 0,
        var height: Int = 0,
        var rotation: Float = 0f,
        var imageIndex: Int = 0,
        var isLast: Boolean = false,
        var percentX: Float = 0f,
        var percentY: Float = 0f,
        var percentHeight: Float = 0f,
        var percentWidth: Float = 0f,
        var mask: Any = ""
    ) : MultiFitStitchViewState()

    class UpdateImageFromDraft(
        var obj: FrameChildImagesModel, var isLast: Boolean = false
    ) : MultiFitStitchViewState()

    class UpdateImageStickersDraft(
        var obj: FrameChildImageStickerModel
    ) : MultiFitStitchViewState()

    class UpdateTextStickersDraft(
        var obj: FrameChildTextStickerModel
    ) : MultiFitStitchViewState()

    class UpdateImagePathsWithEnhancement(
        var index: Int,
        var path: String,
        var isLast: Boolean,
        var mask: Any? = "",
        var fromCrop: Boolean = false,
    ) : MultiFitStitchViewState()

    class UpdateImageBgWithPath(
        var index: Int,
        var path: String,
        var isLast: Boolean,
        var mask: Any? = "",
        var fromCrop: Boolean = false,
    ) : MultiFitStitchViewState()

    class UpdateMatrixOfImage(
        var obj: ImagesModel,
        var index: Int,
    ) : MultiFitStitchViewState()
    class Error(val message: String) : MultiFitStitchViewState()

}