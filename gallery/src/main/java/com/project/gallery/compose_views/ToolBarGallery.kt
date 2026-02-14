package com.project.gallery.compose_views

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.ripple
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.example.ads.Constants.galleryButtonNewFlow
import com.project.gallery.R
import com.project.gallery.data.model.GalleryChildModel
import com.project.gallery.ui.adapters.GalleryAdapterForCompose

class ToolBarGallery {
    @Preview(showBackground = true,  uiMode = Configuration.UI_MODE_NIGHT_YES)
    @Composable
    fun PreviewCreateToolBarCollage() {
        val name = remember { mutableStateOf("My Collage") }
        val loading = remember { mutableStateOf(false) }
        val showFolders = remember { mutableStateOf(false) }
        val showPrimaryTick = remember { mutableStateOf(true) }

        CreateToolBarCollage(
            name = name,
            loading = loading,
            showFolders = showFolders,
            showPrimaryTick = showPrimaryTick,
            alignStart = false,
            fromCollage = false,
        ) { action ->
            // For preview we can just log or do nothing
            println("Callback: $action")
        }
    }
    @Composable
    fun CreateToolBarCollage(
        name: MutableState<String>,
        loading: MutableState<Boolean>,
        showFolders: MutableState<Boolean>,
        showPrimaryTick: MutableState<Boolean>,
        alignStart: Boolean,
        fromCollage: Boolean,
        myCallback: (value: String) -> Unit,
    ) {
        val nameState by rememberUpdatedState(name.value)
        val loadingState by rememberUpdatedState(loading.value)
        val showFoldersState by rememberUpdatedState(showFolders.value)
        val showPrimaryTickState by rememberUpdatedState(showPrimaryTick.value)

        Row(
            modifier = Modifier
                .padding(8.dp, 4.dp, 8.dp, 4.dp)
                .fillMaxWidth()
                .height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row (verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier

                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = true
                    ) {
                        myCallback("cancel")
                    }
            ) {
                Image(
                    painter = painterResource(
                        id = com.project.common.R.drawable.ic_back_arrow
                    ),
                    contentDescription = "back_logo",
                    contentScale = ContentScale.Inside
                )
                Text(
                    text = "Select Photo",
                    color = colorResource(id = com.project.common.R.color.text_color),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (alignStart) 2.6f else 2f)
                    .padding(0.dp,0.dp, if(alignStart) 8.dp else 0.dp, 0.dp)
                ,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (!alignStart) Arrangement.Center else Arrangement.Start
            ) {
//                Text(
//                    text = nameState,
//                    Modifier
//                        .padding(4.dp, 0.dp, 4.dp, 0.dp)
//                        .wrapContentWidth(unbounded = false)
//                        .clickable(
//                            interactionSource = remember { MutableInteractionSource() },
//                            indication = ripple(
//                                color = colorResource(id = com.project.common.R.color.primary_50)
//                            ),
//                            enabled = true
//                        ) {
//                            myCallback("folder")
//                        },
//                    maxLines = 1,
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Medium,
//                    overflow = TextOverflow.Ellipsis,
//                    color = colorResource(id = com.project.common.R.color.tab_txt_clr)
//                )
//
//                Image(
//                    painter = painterResource(id = R.drawable.arrow_down_logo),
//                    contentDescription = "dropDownIcon",
//                    modifier = Modifier
//                        .wrapContentHeight()
//                        .wrapContentWidth(unbounded = true)
//                        .rotate(if (showFoldersState) 180f else 0f)
//                        .padding(4.dp, 0.dp, 4.dp, 0.dp)
//                        .clickable(
//                            interactionSource = remember { MutableInteractionSource() },
//                            indication = ripple(
//                                color = colorResource(id = com.project.common.R.color.primary_50)
//                            ),
//                            enabled = true
//                        ) {
//                            myCallback("folder")
//                        },
//                    contentScale = ContentScale.Inside
//                )

                if (loadingState) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(15.dp),
                        strokeWidth = 2.dp,
                        color = colorResource(id = com.project.common.R.color.primary_50)
                    )
                }
            }

            Icon(
                painter = painterResource(
                    id =
                        if (showPrimaryTickState) com.project.common.R.drawable.next_button_selected else com.project.common.R.drawable.next_button_unselected
                ),
                tint = Color.Unspecified,
                contentDescription = "tick_logo",
                modifier = Modifier
                    .weight(1f)
                    .alpha(if(galleryButtonNewFlow && fromCollage) 0f else 1f)
                    .clickable(enabled = !(galleryButtonNewFlow && fromCollage),
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (showPrimaryTickState) {
                            myCallback("tick")
                        }
                    }
            ) 

           /* if (!alignStart) {
                Icon(
                    painter = painterResource(
                        id =
                        if (showPrimaryTickState) com.project.common.R.drawable.next_button_selected else com.project.common.R.drawable.next_button_unselected
                    ),
                    tint = Color.Unspecified,
                    contentDescription = "tick_logo",
                    modifier = Modifier
                        .weight(1f)
                        .alpha(if(galleryButtonNewFlow && fromCollage) 0f else 1f)
                        .clickable(enabled = !(galleryButtonNewFlow && fromCollage),
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (showPrimaryTickState) {
                                myCallback("tick")
                            }
                        }
                )
            } else {
                Box(modifier = Modifier
                    .weight(1f)
                    .padding(4.dp, 0.dp)
                    .alpha(if(galleryButtonNewFlow && fromCollage) 0f else 1f)
                    .clickable(enabled = !(galleryButtonNewFlow && fromCollage),
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (showPrimaryTickState) {
                            myCallback("tick")
                        }
                    }
                    .background(
                        color =
                        colorResource(
                            if (showPrimaryTickState) com.project.common.R.color.selected_color
                            else com.project.common.R.color.btn_bg_clr
                        ), shape = RoundedCornerShape(30.dp)
                    ),
                    contentAlignment = Alignment.Center) {
                    Text(
                        modifier = Modifier.padding(0.dp, 10.dp),
                        text = stringResource(com.project.common.R.string.next),
                        fontSize = 15.sp,
                        color = if (showPrimaryTickState) Color.Black else Color.White,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }*/
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewCreateToolBar_Default() {
        val name = remember { mutableStateOf("My Toolbar") }
        val loading = remember { mutableStateOf(false) }
        val showDivider = remember { mutableStateOf(false) }
        val showFolders = remember { mutableStateOf(false) }
        val showPrimaryTick = remember { mutableStateOf(true) }
        val showCounterImages = remember { mutableStateOf(0) }

        CreateToolBar(
            name = name,
            loading = loading,
            showDivider = showDivider,
            showFolders = showFolders,
            showPrimaryTick = showPrimaryTick,
            selectedCount = showCounterImages,
        ) { action ->
            println("Action: $action")
        }
    }

    @Composable
    fun CreateToolBar(
        name: MutableState<String>,
        loading: MutableState<Boolean>,
        showDivider: MutableState<Boolean>,
        showFolders: MutableState<Boolean>,
        showPrimaryTick: MutableState<Boolean>,
        selectedCount: MutableState<Int>,
        myCallback: (value: String) -> Unit
    ) {
        val nameState by rememberUpdatedState(name.value)
        val loadingState by rememberUpdatedState(loading.value)
        val showDividerState by rememberUpdatedState(showDivider.value)
        val showFoldersState by rememberUpdatedState(showFolders.value)
        val showPrimaryTickState by rememberUpdatedState(showPrimaryTick.value)
        val selectedCountState by rememberUpdatedState(selectedCount.value)
//   .background(color = colorResource(id = com.project.common.R.color.btn_bg_clr))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(0.dp, 4.dp, 0.dp, 0.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (showDividerState) {
               /* Divider(
                    color = colorResource(id = com.project.common.R.color.camera_back_color),
                    thickness = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.15f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .align(alignment = Alignment.CenterHorizontally)
                )*/
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    //.height(44.dp)
                    .wrapContentHeight()
                    .padding(4.dp, if (showDividerState) 4.dp else 8.dp, 4.dp, 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    painter = painterResource(
                        id =  com.project.common.R.drawable.back_logo
//                            if (showDividerState) {
//                            com.project.common.R.drawable.next_button_unselected
//                        } else {
//                            com.project.common.R.drawable.back_logo
//                        }
                    ),
                    tint = Color.White,
                    contentDescription = "back_logo",
                    modifier = Modifier
                       // .alpha(if (showDividerState) 0f else 1f)
                     //   .weight(if (showDividerState) 1f else 0.40f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(
                                color = colorResource(id = com.project.common.R.color.primary_50)
                            ),
                            enabled = true
                        ) {
                            myCallback("cancel")
                        }
                )

                   /* Text(
                        text = "Select Photo",
                        color = colorResource(id = com.project.common.R.color.normal_txt_clr),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp)
                    )*/

                Column(
                    modifier = Modifier.padding(start = 8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Select Photo",
                        color = colorResource(id = com.project.common.R.color.normal_txt_clr),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Counter text below
                    if (selectedCountState > 0) {
                        Text(
                            text = "Select $selectedCountState image",
                            color = colorResource(id = com.project.common.R.color.normal_txt_clr),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {

                    if (loadingState) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(15.dp),
                            strokeWidth = 2.dp,
                            color = colorResource(id = com.project.common.R.color.primary_50)
                        )
                    }
                }

                Icon(
                    painter = painterResource(
                        id = if (!showDividerState) R.drawable.tick_logo else {
                            if (showPrimaryTickState) com.project.common.R.drawable.next_button_selected else com.project.common.R.drawable.next_button_unselected
                        }
                    ),
                    tint = Color.Unspecified,
                    contentDescription = "tick_logo",
                    modifier = Modifier
                        .weight(if (showDividerState) 1f else 0.45f)
                        .alpha(if (showDividerState) 1f else 0f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (showDividerState) {
                                myCallback("tick")
                            }
                        }
                )
            }
        }
    }

    @Composable
    fun CreateToolBarCollage(
        name: MutableState<String>,
        loading: MutableState<Boolean>,
        showFolders: MutableState<Boolean>,
        showPrimaryTick: MutableState<Boolean>,
        myCallback: (value: String) -> Unit
    ) {
        val nameState by rememberUpdatedState(name.value)
        val loadingState by rememberUpdatedState(loading.value)
        val showFoldersState by rememberUpdatedState(showFolders.value)
        val showPrimaryTickState by rememberUpdatedState(showPrimaryTick.value)

        Row(
            modifier = Modifier
                .padding(8.dp, 4.dp, 8.dp, 4.dp)
                .fillMaxWidth()
                .height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = true
                    ) {
                        myCallback("cancel")
                    }
            ) {
                Image(
                    painter = painterResource(
                        id = com.project.common.R.drawable.back_logo
                    ),
                    contentDescription = "back_logo",
                    contentScale = ContentScale.Inside
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = nameState,
                    Modifier
                        .padding(4.dp, 0.dp, 4.dp, 0.dp)
                        .wrapContentWidth(unbounded = false)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(
                                color = colorResource(id = com.project.common.R.color.primary_50)
                            ),
                            enabled = true
                        ) {
                            myCallback("folder")
                        },
                    maxLines = 1,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    overflow = TextOverflow.Ellipsis,
                    color = colorResource(id = com.project.common.R.color.tab_txt_clr)
                )

                Image(
                    painter = painterResource(id = R.drawable.arrow_down_logo),
                    contentDescription = "dropDownIcon",
                    modifier = Modifier
                        .wrapContentHeight()
                        .wrapContentWidth(unbounded = true)
                        .rotate(if (showFoldersState) 180f else 0f)
                        .padding(4.dp, 0.dp, 4.dp, 0.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(
                                color = colorResource(id = com.project.common.R.color.primary_50)
                            ),
                            enabled = true
                        ) {
                            myCallback("folder")
                        },
                    contentScale = ContentScale.Inside
                )

                if (loadingState) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(15.dp),
                        strokeWidth = 2.dp,
                        color = colorResource(id = com.project.common.R.color.primary_50)
                    )
                }
            }

            Icon(
                painter = painterResource(
                    id =
                    if (showPrimaryTickState) com.project.common.R.drawable.next_button_selected else com.project.common.R.drawable.next_button_unselected
                ),
                tint = Color.Unspecified,
                contentDescription = "tick_logo",
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (showPrimaryTickState) {
                            myCallback("tick")
                        }
                    }
            )
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Preview
    @Composable
    fun CreateGalleryView(
        galleryAdapter: GalleryAdapterForCompose,
        list: MutableState<List<GalleryChildModel>>,
        selectedList: MutableState<List<String>>,
        showImage: MutableState<Boolean>,
        currentPath: MutableState<String>,
        recyclerViewCallBack: (view: RecyclerView) -> Unit
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    RecyclerView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                        layoutManager = GridLayoutManager(context, 4)
                        overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                        adapter = galleryAdapter
                        recyclerViewCallBack.invoke(this)
                    }
                },
                update = { recyclerView ->
                    //Callback that runs on each recomposition.
                }
            )


            if (showImage.value) {
                GlideImage(
                    model = currentPath.value, contentDescription = "large_image",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .wrapContentSize(unbounded = false),
                    contentScale = ContentScale.Fit
                )
            }


        }
    }



    @Composable
    fun loadPicture(url: String, placeholder: Painter? = null): Painter? {

        var state by remember {
            mutableStateOf(placeholder)
        }

        val options: RequestOptions = RequestOptions().override(200, 200)
        val context = LocalContext.current
        val result = object : CustomTarget<Bitmap>() {
            override fun onResourceReady(
                resource: Bitmap,
                transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
            ) {
                state = BitmapPainter(resource.asImageBitmap())
            }

            override fun onLoadCleared(p: Drawable?) {
                state = placeholder
            }
        }
        try {
            Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(options)
                .into(result)
        } catch (e: Exception) {
            // Can't use LocalContext in Compose Preview
        }
        return state
    }
}

