package com.example.questions_intro.ui.compose_views

import android.text.TextUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.Dimension

import com.example.ads.Constants.languageCode
import com.example.questions_intro.R
import com.example.questions_intro.ui.model.Questions
import com.example.questions_intro.ui.model.QuestionsChoices



//@Preview
@Composable
fun CreateToolBar(
    currentPage: String = "Test",
    showSelectedNext: MutableState<Boolean>,
    onSkipClick:() ->Unit,
    onBackClick: () -> Unit
) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .height(30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Image(
            painter = painterResource(id = com.project.common.R.drawable.back_logo),
            contentDescription = "backLogo",
            modifier = Modifier
                .fillMaxHeight()
                .padding(8.dp, 0.dp, 0.dp, 0.dp)
                .alpha(if (currentPage.toInt() != 1) 1f else 0f)
                .clickable(enabled = currentPage.toInt() != 1, onClick = {
                    onBackClick.invoke()
                })
        )

        Text(
            text = currentPage.plus("/4"),
            Modifier
                .padding(24.dp, 0.dp, 0.dp, 0.dp),
            maxLines = 1,
            style = MaterialTheme.typography.h2,
            textAlign = TextAlign.Center,
            color = colorResource(com.project.common.R.color.tab_txt_clr_question)
        )


        Text(
            text = stringResource(com.project.common.R.string.skip),
            Modifier
                .padding( end= 16.dp)
                .clickable {
                    onSkipClick.invoke()
                },
            maxLines = 1,
            style = MaterialTheme.typography.overline,
            textAlign = TextAlign.Center,
            color = colorResource(com.project.common.R.color.tab_txt_clr_question)
        )

    }
}

//@Preview
@Composable
fun FragmentView(
    screen: Int = 0,
    question: Questions = Questions("sample", ""),
    choicesList: List<QuestionsChoices> = emptyList(),
    showSelectedNext: MutableState<Boolean>,
    selectedItems: SnapshotStateMap<Int, Boolean>,
    onNextClick: () -> Unit,
    onSkipClick: () -> Unit,
    onBackClick: () -> Unit,
    onItemClick: (Int, Boolean) -> Unit,
    scrollState: LazyGridState,
    fromSurvey:Boolean = false,
) {
    FixedTextSize {
        MyAppTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorResource(com.project.common.R.color.container_clr_activity_questions))
            ) {
                CreateToolBar(screen.toString(), showSelectedNext,onSkipClick, onBackClick)
                if(fromSurvey){
                    CreateContentSurvey(
                        question,
                        choicesList,
                        showSelectedNext,
                        selectedItems,
                        onNextClick,
                        onItemClick,
                        scrollState
                    )
                }else {
                    CreateContent(
                        question,
                        choicesList,
                        showSelectedNext,
                        selectedItems,
                        onNextClick,
                        onItemClick,
                        scrollState
                    )
                }
            }
        }
    }
}

var latest: Long = 0

private fun singleClick(onClick: () -> Unit): () -> Unit {
    return {
        val now = System.currentTimeMillis()
        if (now - latest >= 300) {
            onClick()
            latest = now
        }
    }
}

//@Preview
@Composable
fun CreateContent(
    question: Questions = Questions("sample", ""),
    choicesList: List<QuestionsChoices> = emptyList(),
    showSelectedNext: MutableState<Boolean>,
    selectedItems: SnapshotStateMap<Int, Boolean>,
    onNextClick: () -> Unit,
    onItemClick: (Int, Boolean) -> Unit,
    scrollState: LazyGridState,
) {

    val interactionSource = remember { MutableInteractionSource() }

//    val scrollState = rememberLazyGridState()

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        val (grid, skipText, heading, subheading) = createRefs()

//        val subheadingText = HtmlCompat.fromHtml(
//            question.subHeading,
//            HtmlCompat.FROM_HTML_MODE_LEGACY
//        )
        Text(
            text = question.heading,
            Modifier
                .padding(0.dp, 10.dp, 0.dp, 0.dp)
                .constrainAs(heading) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .wrapContentSize(),
            style = MaterialTheme.typography.h1,
            color = colorResource(com.project.common.R.color.tab_txt_clr_question),
            textAlign = TextAlign.Center,
        )

        Text(
            text = question.subHeading,
            Modifier
                .padding(4.dp, 10.dp, 4.dp, 0.dp)
                .constrainAs(subheading) {
                    top.linkTo(heading.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.preferredWrapContent
                }
                .wrapContentHeight(),
            style = MaterialTheme.typography.subtitle1,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            color = colorResource(com.project.common.R.color.tab_txt_clr_question)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = scrollState,
            modifier = Modifier
                .constrainAs(grid) {
                    verticalBias = 0f
                    top.linkTo(subheading.bottom, margin = 16.dp)//24.dp
                    bottom.linkTo(skipText.top, margin = 16.dp)
                    height =
                        Dimension.preferredWrapContent // Wraps content height but respects constraints
                }
                .padding(horizontal = 0.dp),
            contentPadding = PaddingValues(0.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(choicesList) { index, choice ->
                val isSelected = selectedItems[index] == true
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(
                            colorResource(com.project.common.R.color.drawer_card_clr),
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            if (!isSelected) 1.dp else 2.dp,
                            color = if (!isSelected) Color.Black.copy(alpha = 0.2f)
                            else colorResource(com.project.common.R.color.selected_color),
                            RoundedCornerShape(10.dp)
                        )
                        .aspectRatio(1.5f)
                      //  .aspectRatio(1.73f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = singleClick {
                                selectedItems[index] = !(selectedItems[index] ?: false)
                                selectedItems[index]?.let {
                                    if (it) {
                                        onItemClick.invoke(index, true)
                                    } else {
                                        onItemClick.invoke(index, false)
                                    }
                                }
                                showSelectedNext.value = selectedItems.containsValue(true)
                            }
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp, 0.dp)
                    ) {
                        Image(
                            painter = painterResource(choice.icon),
                            contentDescription = "icon",
                            modifier = Modifier.size(28.dp)
                        )
                        /*Icon(
                            painter = painterResource(choice.icon),
                            tint = colorResource(com.project.common.R.color.tab_txt_clr_icon),
                            contentDescription = "icon"
                        )*/
                        Text(
                            text = choice.title,
                            Modifier
                                .padding((12.5).dp, 0.dp, 0.dp, 0.dp)
                                .fillMaxWidth(),
                            style = MaterialTheme.typography.body1,
                            textAlign = TextAlign.Start,
                            lineHeight = 20.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            color = colorResource(com.project.common.R.color.tab_txt_clr_question)
                        )
                    }

                    if (isSelected) {
                        Image(
                            painter = painterResource(
                                if (languageCode == "ar" || languageCode == "ur")
                                    R.drawable.selected_icon_new_ur_ar
                                else
                                    R.drawable.selected_icon_new
                            ),
                            contentDescription = "icon",
                            contentScale = ContentScale.FillHeight,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .height(24.dp)
                        )
                    }
                }
            }
        }

        val originalColor = colorResource(id = com.project.common.R.color.selected_color)
        val colorUnSelected = colorResource(id = com.project.common.R.color.question_next_btn_clr)

        val originalColorTxt = colorResource(id = com.project.common.R.color.white)
        val colorUnSelectedTxt = colorResource(id = com.project.common.R.color.question_next_txt_clr)

        Box(
            modifier = Modifier
                .padding(0.dp, 0.dp, 8.dp, 0.dp)
                .constrainAs(skipText) {
                    bottom.linkTo(parent.bottom, margin = 14.dp)//margin = 24.dp
                    // top.linkTo(grid.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    verticalBias = 0f
                }
                .background(
                    if (showSelectedNext.value) originalColor  else colorUnSelected,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(enabled = showSelectedNext.value) {
                    onNextClick.invoke()
                }
        ) {
            Text(
                text = stringResource(com.project.common.R.string.next),
                Modifier
                    .padding(horizontal = 25.dp, vertical = 5.dp)//horizontal 10
                    //.padding(horizontal = 55.dp, vertical = 17.dp)//horizontal 10
                    .wrapContentSize(),
                maxLines = 1,
                style = MaterialTheme.typography.h2,
                color = if(showSelectedNext.value)originalColorTxt else colorUnSelectedTxt//colorResource(com.project.common.R.color.selected_color)
            )
        }

    /*    Box(
            modifier = Modifier
                .padding(0.dp, 0.dp, 8.dp, 0.dp)
                .constrainAs(skipText) {
                    bottom.linkTo(parent.bottom, margin = 14.dp)//margin = 24.dp
                    // top.linkTo(grid.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    verticalBias = 0f
                }
                .background(
                    if (showSelectedNext.value) colorResource(com.project.common.R.color.question_next_btn_clr) else colorWithAlpha,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(enabled = showSelectedNext.value) {
                    onNextClick.invoke()
                }
        ) {
            Text(
                text = stringResource(com.project.common.R.string.next),
                Modifier
                    //.padding(horizontal = 45.dp, vertical = 12.dp)
                    .padding(horizontal = 25.dp, vertical = 5.dp)
                  //  .padding(horizontal = 50.dp, vertical = 15.dp)//horizontal 10
                    .wrapContentSize(),
                maxLines = 1,
                style = MaterialTheme.typography.h2,
                color = colorResource(com.project.common.R.color.selected_color)
            )
        }*/

    }
}

@Composable
fun CreateContentSurvey(
    question: Questions = Questions("sample", ""),
    choicesList: List<QuestionsChoices> = emptyList(),
    showSelectedNext: MutableState<Boolean>,
    selectedItems: SnapshotStateMap<Int, Boolean>,
    onNextClick: () -> Unit,
    onItemClick: (Int, Boolean) -> Unit,
    scrollState: LazyGridState,
) {

    val interactionSource = remember { MutableInteractionSource() }

//    val scrollState = rememberLazyGridState()

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        val (grid, skipText, heading, subheading) = createRefs()
        Text(
            text = question.heading,
            Modifier
                .padding(0.dp, 16.dp, 0.dp, 0.dp)
                .constrainAs(heading) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .wrapContentSize(),
            style = MaterialTheme.typography.h1,
            color = colorResource(com.project.common.R.color.tab_txt_clr_question),
            textAlign = TextAlign.Center,
        )

        Text(
            text = question.subHeading,
            Modifier
                .padding(4.dp, 16.dp, 4.dp, 0.dp)
                .constrainAs(subheading) {
                    top.linkTo(heading.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.preferredWrapContent
                }
                .wrapContentHeight(),
            style = MaterialTheme.typography.subtitle1,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            color = colorResource(com.project.common.R.color.tab_txt_clr_question)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = scrollState,
            modifier = Modifier
                .constrainAs(grid) {
                    verticalBias = 0f
                    top.linkTo(subheading.bottom, margin =16 .dp)//24
                    bottom.linkTo(skipText.top, margin = 16.dp)//16
                    height = Dimension.fillToConstraints//Dimension.fillToConstraints
                        //Dimension.preferredWrapContent // Wraps content height but respects constraints
                }
                .padding(horizontal = 0.dp),
            contentPadding = PaddingValues(0.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(choicesList) { index, choice ->
                val isSelected = selectedItems[index] == true
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(
                            colorResource(com.project.common.R.color.drawer_card_clr),
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            if (!isSelected) 1.dp else 2.dp,
                            color = if (!isSelected) Color.Black.copy(alpha = 0.2f)
                            else colorResource(com.project.common.R.color.selected_color),
                            RoundedCornerShape(10.dp)
                        )
                        .wrapContentHeight()
                        //.aspectRatio(1.1f)
//                        .aspectRatio(1.73f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = singleClick {
                                selectedItems[index] = !(selectedItems[index] ?: false)
                                selectedItems[index]?.let {
                                    if (it) {
                                        onItemClick.invoke(index, true)
                                    } else {
                                        onItemClick.invoke(index, false)
                                    }
                                }
                                showSelectedNext.value = selectedItems.containsValue(true)
                            }
                        )
                ) {
                    Column(
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier.padding(6.dp,6.dp)
                    ) {
                        Image(
                            painter = painterResource(choice.icon),
                            contentDescription = "icon",
                            modifier = Modifier
                                .padding( (12.5).dp, (12.5).dp, (12.5).dp, 0.dp)
                                .fillMaxWidth()
                               // .height(110.dp)
                                .fillMaxSize(0.7f) // Dynamic height instead of fixed dp
                        )
                        Text(
                            text = choice.title,
                            Modifier
                                .padding(0.dp, (12.5).dp, 0.dp,(12.5).dp)
                                .fillMaxWidth(),
                            style = MaterialTheme.typography.body1,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = colorResource(com.project.common.R.color.tab_txt_clr_question)
                        )
                    }

                    if (isSelected) {
                        Image(
                            painter = painterResource(
                                if (languageCode == "ar" || languageCode == "ur")
                                    R.drawable.selected_icon_new_ur_ar
                                else
                                    R.drawable.selected_icon_new
                            ),
                            contentDescription = "icon",
                            contentScale = ContentScale.FillHeight,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .height(24.dp)
                        )
                    }
                }
            }
        }
        val originalColor = colorResource(id = com.project.common.R.color.selected_color)
        val colorUnSelected = colorResource(id = com.project.common.R.color.question_next_btn_clr)

        val originalColorTxt = colorResource(id = com.project.common.R.color.white)
        val colorUnSelectedTxt = colorResource(id = com.project.common.R.color.question_next_txt_clr)


        Box(
        modifier = Modifier
           .padding(0.dp, 0.dp, 8.dp, 0.dp)
           .constrainAs(skipText) {
               bottom.linkTo(parent.bottom, margin = 14.dp)//margin = 24.dp
               // top.linkTo(grid.bottom, margin = 16.dp)
               start.linkTo(parent.start)
               end.linkTo(parent.end)
               verticalBias = 0f
           }
           .background(
               if (showSelectedNext.value) originalColor  else colorUnSelected,
               shape = RoundedCornerShape(8.dp)
           )
           .clickable(enabled = showSelectedNext.value) {
               onNextClick.invoke()
           }
   ) {
       Text(
           text = stringResource(com.project.common.R.string.next),
           Modifier
               .padding(horizontal = 25.dp, vertical = 5.dp)//horizontal 10
               //.padding(horizontal = 55.dp, vertical = 17.dp)//horizontal 10
               .wrapContentSize(),
           maxLines = 1,
           style = MaterialTheme.typography.h2,
           color = if(showSelectedNext.value)originalColorTxt else colorUnSelectedTxt//colorResource(com.project.common.R.color.selected_color)
       )
   }

    }
}

/*val MyCustomFont = FontFamily(
    Font(com.project.common.R.font.roboto_flex_regular, FontWeight.Normal),
    Font(com.project.common.R.font.roboto_flex_regular, FontWeight.Light),
    Font(com.project.common.R.font.roboto_flex_regular, FontWeight.Thin),
    Font(com.project.common.R.font.roboto_flex_regular, FontWeight.Bold),
    Font(com.project.common.R.font.roboto_bold, FontWeight.W600),
    Font(com.project.common.R.font.roboto_flex_regular, FontWeight.W700),
)*/

val MyCustomFont = FontFamily(
    Font(com.project.common.R.font.robotom, FontWeight.Normal),
    Font(com.project.common.R.font.robotom, FontWeight.Light),
    Font(com.project.common.R.font.robotom, FontWeight.Thin),
    Font(com.project.common.R.font.robotom, FontWeight.Bold),
    Font(com.project.common.R.font.robotom, FontWeight.W600),
    Font(com.project.common.R.font.robotom, FontWeight.W700),
)

val AppTypography = Typography(
    h1 = TextStyle(
        fontFamily = MyCustomFont,
        fontWeight = FontWeight.W600,
        fontSize = 22.sp
    ),
    h2 = TextStyle(
        fontFamily = MyCustomFont,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    ),
    subtitle1 = TextStyle(
        fontFamily = MyCustomFont,
        fontWeight = FontWeight.W700,
        fontSize = 13.sp
    ),
    subtitle2 = TextStyle(
        fontFamily = MyCustomFont,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp
    ),
    body1 = TextStyle(
        fontFamily = MyCustomFont,
        fontWeight = FontWeight.W700,
        fontSize = 13.sp
    ),
    overline = TextStyle(
        fontFamily = MyCustomFont,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        textDecoration = TextDecoration.Underline
    )
)

@Composable
fun MyAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = AppTypography,
        content = content
    )
}

@Composable
fun FixedTextSize(content: @Composable () -> Unit) {
    val fixedDensity = Density(density = LocalDensity.current.density, fontScale = 1f)

    CompositionLocalProvider(LocalDensity provides fixedDensity) {
        content()
    }
}

@Composable
fun CreateChoices(index: Int = 0, choices: QuestionsChoices, weightModifier: Modifier) {
    Row(
        modifier = weightModifier
            .fillMaxWidth()
            .padding(0.dp, if (index == 0) 24.dp else 16.dp, 0.dp, 0.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(0.dp, 0.dp, 16.dp, 0.dp)
                .background(
                    colorResource(com.project.common.R.color.drawer_card_clr),
                    RoundedCornerShape(8.dp)
                )
                .weight(1f)
                .fillMaxHeight()
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp, 4.dp)
            )
            {
                Icon(
                    painter = painterResource(choices.icon),
                    contentDescription = "icon"
                )
                Text(
                    text = choices.title,
                    Modifier
                        .padding(4.dp, 0.dp, 0.dp, 0.dp),
                    style = MaterialTheme.typography.subtitle1,
                    textAlign = TextAlign.Center
                )
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(
                    colorResource(com.project.common.R.color.drawer_card_clr),
                    RoundedCornerShape(8.dp)
                )
                .weight(1f)
                .fillMaxHeight()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp, 4.dp)
            )
            {
                Icon(
                    painter = painterResource(R.drawable.student_icon),
                    contentDescription = "icon"
                )
                Text(
                    text = stringResource(com.project.common.R.string.help_us_bring_experiences),
                    Modifier
                        .padding(0.dp, 8.dp, 0.dp, 0.dp),
                    style = MaterialTheme.typography.subtitle1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
