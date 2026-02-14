package com.project.gallery.compose_views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class ToolBar {

    @Preview
    @Composable
    fun CreateToolBar(name: String = "Test") {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = com.project.common.R.drawable.back_logo),
                contentDescription = "backLogo",
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(8.dp, 0.dp, 0.dp, 0.dp)
            )

            Text(
                text = name,
                Modifier
                    .padding(8.dp, 0.dp, 0.dp, 0.dp),
                maxLines = 1,
            )
        }
    }
}