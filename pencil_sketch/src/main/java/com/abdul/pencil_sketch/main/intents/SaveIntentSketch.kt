package com.abdul.pencil_sketch.main.intents

import android.content.Context
import com.project.common.enum_classes.SaveQuality
import com.project.common.model.SavingModel

sealed class SaveIntentSketch {
    class SaveClick(var resolution: SaveQuality) : SaveIntentSketch()
    class Saving(
        var context: Context,
        var savingModelList: MutableList<SavingModel>
    ) : SaveIntentSketch()

}