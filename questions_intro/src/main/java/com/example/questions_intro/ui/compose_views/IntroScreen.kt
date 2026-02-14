package com.example.questions_intro.ui.compose_views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.questions_intro.R
import com.example.questions_intro.ui.model.IntroScreenContent

@Preview(widthDp = 200, heightDp = 200)
@Composable
fun IntroView(
    screenContent: IntroScreenContent = IntroScreenContent(),
) {

//    val composition by rememberLottieComposition(
//        LottieCompositionSpec.RawRes(
//            screenContent.animation
//        )
//    )

//    val progress by animateLottieCompositionAsState(
//        composition,
//        iterations = LottieConstants.IterateForever
//    )

    FixedTextSize {
        MyIntroTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorResource(com.project.common.R.color.container_clr_activity_questions)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

//                LottieAnimation(
//                    composition, contentScale = ContentScale.Fit,
//                    progress = {
//                        progress
//                    }
//                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 8.dp)
                    ,
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(screenContent.icon),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = "icon"
                    )

                    Image(
                        painter = painterResource(R.drawable.overlay_intro),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = "icon"
                    )
                }

                screenContent.heading?.let {
                    Text(
                        text = it,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.h1,
                        color = colorResource(
                            com.project.common.R.color.tab_txt_clr
                        ),
                        modifier = Modifier
                            .padding(top = 24.dp, bottom = 0.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )

                    Text(
                        text = screenContent.subHeading,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.subtitle1,
                        color = colorResource(
                            com.project.common.R.color.tab_txt_clr
                        ),
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                }
            }
        }
    }
}

val MyCustomFontIntro = FontFamily(
    Font(com.project.common.R.font.roboto_regular, FontWeight.Normal),
    Font(com.project.common.R.font.roboto_light, FontWeight.Light),
    Font(com.project.common.R.font.roboto_thin, FontWeight.Thin),
    Font(com.project.common.R.font.roboto_bold, FontWeight.Bold),
    Font(com.project.common.R.font.roboto_bold, FontWeight.W700),
)

val AppTypographyIntro = Typography(
    h1 = TextStyle(
        fontFamily = MyCustomFontIntro,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),

    subtitle1 = TextStyle(
        fontFamily = MyCustomFontIntro,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    )
)

@Composable
fun MyIntroTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = AppTypographyIntro,
        content = content
    )
}