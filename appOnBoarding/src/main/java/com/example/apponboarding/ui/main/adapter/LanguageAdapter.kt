package com.example.apponboarding.ui.main.adapter

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.apponboarding.R
import com.example.apponboarding.domain.Language

class LanguageAdapter(
    private var languages: List<Language>,
    private var selectedLanguageCode: String,
    private val onLanguageSelected: (Language) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    var isSelectedStart = 0
    var previousPosition = 0

    inner class LanguageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val animationTap: LottieAnimationView = itemView.findViewById(R.id.animationTap)
        private val flagLanguageItem: ImageView = itemView.findViewById(R.id.flagLanguageItem)
        private val titleLanguageItem: TextView = itemView.findViewById(R.id.titleLanguageItem)
        private val checkboxLanguageItemChecked: ImageView = itemView.findViewById(R.id.check_box_checked)
        private val layout: LinearLayout = itemView.findViewById(R.id.ll)

        fun bind(language: Language, position: Int) {
            // Set flag image and language name
            flagLanguageItem.setImageResource(language.flagResId)
            titleLanguageItem.text = language.name

            // Show animationTap only for the initially selected language
            Log.d(TAG, "selectedLanguageCode: $selectedLanguageCode")
            Log.d(TAG, "language.languageCode: ${language.languageCode}")

            if (isSelectedStart == 0) {
                selectedLanguageCode =
                    "en"
            }

            val isSelected = language.languageCode == selectedLanguageCode
            language.isSelected = isSelected
            Log.d(TAG, "isSelected: $isSelected")

            if (isSelected) {
                previousPosition = position
            }

            // Show the checkbox if the item is selected
            checkboxLanguageItemChecked.visibility = if (isSelected && isSelectedStart != 0) View.VISIBLE else View.GONE
            layout.setBackgroundResource(
                if (isSelected && isSelectedStart != 0) com.project.common.R.drawable.border_selected_lan
                else com.project.common.R.drawable.border_unselected_lan
            )

            animationTap.visibility = if (isSelected && isSelectedStart == 0) View.VISIBLE else View.GONE

            // Handle item click
            itemView.setOnClickListener {
                if (isSelectedStart == 0) {
                    isSelectedStart = 1
                    animationTap.visibility = View.GONE
                    checkboxLanguageItemChecked.visibility = View.VISIBLE
                    layout.setBackgroundResource(com.project.common.R.drawable.border_selected_lan)
                    onLanguageSelected(language)
                    selectedLanguageCode = language.languageCode
                    if (previousPosition >= 0)
                        notifyItemChanged(previousPosition)
                } else {
                    if (!isSelected) {

                        isSelectedStart = 1
                        animationTap.visibility = View.GONE
                        checkboxLanguageItemChecked.visibility = View.VISIBLE
                        layout.setBackgroundResource(com.project.common.R.drawable.border_selected_lan)
                        onLanguageSelected(language)
                        selectedLanguageCode = language.languageCode
                        if (previousPosition >= 0)
                            notifyItemChanged(previousPosition)
                    }
                }
                previousPosition = position
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_language, parent, false)
        return LanguageViewHolder(view)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(languages[position], position)
    }

    override fun getItemCount(): Int = languages.size

    fun updateData(newLanguages: List<Language>, newSelectedLanguageCode: String) {
        languages = newLanguages
        Log.d(TAG, "newSelectedLanguageCode: $newSelectedLanguageCode")
        selectedLanguageCode = newSelectedLanguageCode
        Log.d(TAG, "selectedLanguageCodeAfterNewValue: $selectedLanguageCode")
        notifyDataSetChanged()
    }
}