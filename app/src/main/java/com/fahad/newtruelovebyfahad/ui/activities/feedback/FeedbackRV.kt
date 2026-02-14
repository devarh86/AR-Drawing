package com.fahad.newtruelovebyfahad.ui.activities.feedback

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.FeedbackRowItemBinding
import com.project.common.utils.setOnSingleClickListener

class FeedbackRV(
    private val dataList: List<Suggestions>,
    private val onClick: (feedback: Suggestions) -> Unit
) :
    RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        FeedbackViewHolder(
            FeedbackRowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position >= 0 && position < dataList.size) {

            with((holder as FeedbackViewHolder).binding) {
                if (dataList.isNotEmpty() && dataList.size > position) {
                    dataList[position].let { data ->
                        if (!data.selected) {
                            kotlin.runCatching {
                                text.setTextColor(
                                    ContextCompat.getColor(
                                        root.context,
                                        com.project.common.R.color.btn_txt_clr
                                    )
                                )
                                text.background = ContextCompat.getDrawable(
                                    root.context,
                                    if (!data.selected) com.project.common.R.drawable.corner_radius_bg_5dp_with_stroke else R.drawable.corner_radius_bg_5dp_with_solid
                                )
                            }
                        } else {
                            runCatching {
                                text.setTextColor(
                                    ContextCompat.getColor(
                                        root.context,
                                        com.project.common.R.color.black
                                    )
                                )
                                text.background = ContextCompat.getDrawable(
                                    root.context,
                                    R.drawable.corner_radius_bg_5dp_with_solid
                                )
                            }
                        }
                        text.text = data.text
                        holder.itemView.setOnSingleClickListener {
                            runCatching {
                                data.selected = !data.selected
                                notifyItemChanged(position)
                                onClick.invoke(data)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = dataList.size

    inner class FeedbackViewHolder(val binding: FeedbackRowItemBinding) : ViewHolder(binding.root)
}

@Keep
data class Suggestions(val text: String, var selected: Boolean = false)