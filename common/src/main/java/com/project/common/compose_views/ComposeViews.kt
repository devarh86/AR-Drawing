package com.project.common.compose_views

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inapp.helpers.Constants.isProVersion
import com.project.common.R


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ToolBarComposePreview() {
    ToolbarCompose("Multi Fit", onBackClick = {

    }, onSaveClick = {

    })
}

@Composable
fun ToolbarCompose(titleText: String,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        tonalElevation = 3.dp,
        color = colorResource(id = com.project.common.R.color.container_clr_activity)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .width(30.dp)
                        .height(24.dp)
                        .padding(end = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = com.project.common.R.drawable.back_logo),
                        contentDescription = "Back",
                        tint = colorResource(id = com.project.common.R.color.tab_txt_clr)
                    )
                }

                Text(
//                    text = stringResource(id = com.project.common.R.string.stitch),
                    text = titleText,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily(Font(com.project.common.R.font.robotom))
                    ),
                    color = colorResource(id = com.project.common.R.color.tab_txt_clr)
                )

                Spacer(modifier = Modifier.weight(1f))
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .clickable { onSaveClick() }
//                        .padding(8.dp) // makes it easier to tap
//                ) {
//                    Text(
//                        text = stringResource(id = com.project.common.R.string.save).uppercase(),
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.SemiBold,
//                        color = Color.White // adjust based on toolbar bg
//                    )
//
//                    Spacer(modifier = Modifier.width(4.dp)) // space between text and icon
//
//                    Icon(
//                        painter = painterResource(id = R.drawable.save_ic), // <-- your custom icon
//                        contentDescription = null,
//                        tint = Color.White, // match text color
//                        modifier = Modifier.size(18.dp) // adjust size as needed
//                    )
//                }
//
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(color = colorResource(com.project.common.R.color.selected_color))
                        .clickable { onSaveClick() }
                        .padding(horizontal = 17.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Left side -> Save + Watch Ad in Column
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(id = com.project.common.R.string.save),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        if (!isProVersion()) {
                            Text(
                                text = stringResource(id = com.project.common.R.string.with_ad),
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Black
                            )
                        }
                    }

                    // Right side -> Icon aligned to center (spans height of Save + Watch Ad)
                    Icon(
                        painter = painterResource(id = R.drawable.save_ic),// replace with painterResource for custom drawable
                        contentDescription = "Download",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

            }
        }
    }
}