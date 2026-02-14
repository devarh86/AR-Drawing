package com.project.core.customView.stickerView

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.InputType
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.project.sticker.stickerView.AutoResizeTextView

/**
 * Created by cheungchingai on 6/15/15.
 */
class StickerTextView : StickerView {

    private var tv_main: AutoResizeTextView? = null

    var colorWithoutTrans = Color.WHITE

    var myBackgroundColor = Color.TRANSPARENT

    override val mainView: View?
        get() = if (tv_main != null) {
            tv_main
        } else {
            tv_main = AutoResizeTextView(context)
            //tv_main.setTextSize(22);
            tv_main?.setTextColor(Color.WHITE)
            tv_main?.setBackgroundColor(Color.parseColor("#B3000000"))
//            tv_main?.setBackgroundColor(Color.TRANSPARENT)
            tv_main?.gravity = Gravity.CENTER
            tv_main?.textSize = 400f
            tv_main?.setShadowLayer(4f, 0f, 0f, Color.BLACK)
            tv_main?.maxLines = 0
            val params = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            params.gravity = Gravity.CENTER
            tv_main?.layoutParams = params
            if (imageViewFlip != null) imageViewFlip?.visibility = GONE
            (tv_main as EditText).imeOptions = EditorInfo.IME_ACTION_DONE
            (tv_main as EditText).setImeActionLabel("Done", EditorInfo.IME_ACTION_DONE)
            (tv_main as EditText).inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
            (tv_main as EditText).setSelectAllOnFocus(true)
            (tv_main as EditText).setLines(50)
            (tv_main as EditText).isSingleLine = false
            tv_main
        }

    constructor(context: Context) : super(context) {

        tv_main?.let {
            it.setOnEditorActionListener(object : TextView.OnEditorActionListener {
                override fun onEditorAction(
                    v: TextView?,
                    actionId: Int,
                    event: KeyEvent?
                ): Boolean {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {

                        disableEditText()
                        return true;
                    }
                    return false;
                }
            })
        }
    }

    constructor(context: Context, attrs: AttributeSet?) : super(
        context, attrs
    ) {
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context, attrs, defStyle
    ) {
    }

    fun EditText.setReadOnly(value: Boolean, inputType: Int = InputType.TYPE_NULL) {
        isFocusable = !value
        isFocusableInTouchMode = !value
        this.inputType = inputType
    }

    var text: String?
        get() = if (tv_main != null) tv_main?.text.toString() else null
        set(text) {
            if (tv_main != null) tv_main?.setText(text)
        }


    fun pixelsToSp(context: Context, px: Float): Float {
        val scaledDensity = context.resources.displayMetrics.scaledDensity
        return px / scaledDensity
    }

    fun setTypeFace(typeface: Typeface) {

        tv_main?.let {
            it.typeface = typeface
        }
    }

    fun enableEditText() {

        tv_main?.setReadOnly(false, InputType.TYPE_TEXT_FLAG_MULTI_LINE)
        tv_main?.requestFocus()
        tv_main?.setSelectAllOnFocus(false)
        tv_main?.showKeyboard()
    }

    fun disableEditText() {

        tv_main?.let {
            it.setReadOnly(true)
            forTouch?.visibility = View.VISIBLE
            it.hideKeyboard()
            (it as EditText).isSingleLine = false
            it.reAdjust()
        }
    }

    private fun EditText.showKeyboard() {
        val imm =
            this@StickerTextView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    fun EditText.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.windowToken, 0)
    }

    fun setTextColor(color: Int) {

        tv_main?.setTextColor(color)
    }

    fun setBackGround(color: Int) {

        tv_main?.setBackgroundColor(color)
        myBackgroundColor = color
    }

    fun setTextColorWithoutTrans(color: Int) {

        colorWithoutTrans = color
    }

    fun getTextColor(): Int? {
        return tv_main?.currentTextColor
    }
}