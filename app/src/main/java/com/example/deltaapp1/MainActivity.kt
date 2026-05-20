package com.example.deltaapp1
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.grid.items
import android.content.ContentValues
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.foundation.border
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation.width
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

import com.example.deltaapp1.ui.theme.DeltaApp1Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import java.io.File
import java.io.FileOutputStream
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.apply
import kotlin.collections.plusAssign
import kotlin.text.toInt

enum class LayoutT {TWO_GRID,THREE_GRID,FOUR_GRID,FIVE_GRID}
data class TextOverlayStuffs(
    val text: String,
    val relativeX: Float=0.5f,
    val relativeY: Float=0.5f,
    val textColor:Color = Color.White,
    val textFont: FontFamily = FontFamily.Default
)
data class ScrapPicStuffs(
    val id: Uri,
    val x:Float,
    val y:Float,
    val zoom:Float,
    val rotate:Float
)
class EditorViewModel : ViewModel() { //inheriting from ViewModel class
    var spacing by mutableStateOf(4.dp)
    var rounding by mutableStateOf(10.dp)
    //setContent
    var selectedLayout by mutableStateOf<LayoutT?>(null)
    var viewMode by mutableStateOf(true) //true is dark mode(default) and false is bright mode btw
    var bgColour by mutableStateOf(Color.White)

    //secondscreen function
    var currentBox by mutableStateOf<Int?>(null)
    var selectedPic by mutableStateOf<Int?>(null)
    var currentPics by mutableStateOf(listOf<Int?>(null,null,null,null,null)) //drawable resources are integers generally
    var textOverlay by mutableStateOf<String>("")
    var listOfTextOvers by mutableStateOf(listOf<TextOverlayStuffs>())
    var displayOverlay by mutableStateOf(false)
    var showDone by mutableStateOf(false)
    var selectedStyle by mutableStateOf(FontFamily.Default)
    var selectedColor by mutableStateOf(Color.White)
    //scrapbook
    var scrapRounding by mutableStateOf(10.dp)
    var scrapBg by mutableStateOf(Color.White)
    var scrapPics by mutableStateOf(listOf<ScrapPicStuffs>())

}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeltaApp1Theme {
                val navController =
                    rememberNavController() //an object to control navigation backstack, current route, etc.
                val editorViewModel: EditorViewModel = viewModel() //created an object but also don't want duplicates using viewModel() which searches for one and equalizes to that
                //searches for instance in ViewModels store and uses that if existing and if not creates one. So that the instance of viewmodel states can be shared independent of the composable
                NavHost( //manages the screens and which screens are visible
                    navController = navController,
                    startDestination = "home"
                ) { //call required function under each composable(route=" ")
                    composable("home") {
                        ScreenHome(
                            navController=navController, selectedLayout=editorViewModel.selectedLayout, layoutSelection = {layout -> editorViewModel.selectedLayout = layout}, viewMode = editorViewModel.viewMode, changeView = { editorViewModel.viewMode = !editorViewModel.viewMode},bgColour = editorViewModel.bgColour,
                            colorChange = {newcolor ->
                                editorViewModel.bgColour = newcolor
                                editorViewModel.scrapBg = newcolor
                            }
                        )
                    }
                    composable("editor") {
                        SecondScreen(
                            selectedLayoutT =
                                editorViewModel.selectedLayout!!, //!! guarantees that it is not null
                            navController =
                                navController,
                            viewMode = editorViewModel.viewMode,
                            bgColour = editorViewModel.bgColour, colorChange = {newcolor -> editorViewModel.bgColour = newcolor}
                        )
                    }
                    composable("scrapbook"){
                        ScrapbookScreen(editorViewModel.viewMode, navController)
                    }
                    composable("savedpics"){
                        SavedPicsScreen(editorViewModel.viewMode,navController)
                    }
                }
            }
        }
    }
}
@Composable
fun AdaptiveSp(base: Float, max: Float = base * 1.4f): TextUnit {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val scale = (screenWidth / 360f).coerceIn(0.75f, 1.4f)
    return (base * scale).coerceAtMost(max).sp
}

@Composable
fun AdaptiveDp(base: Float, max: Float = base * 1.5f): Dp {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val scale = (screenWidth / 360f).coerceIn(0.75f, 1.5f)
    return (base * scale).coerceAtMost(max).dp
}
data class InternPhoto(
    val name:String,
    val bmp:Bitmap
)
private suspend fun loadSavedImages(context:Context): List<InternPhoto>{
    return withContext(Dispatchers.IO){
        val files = context.filesDir?.listFiles()
        files?.filter{ it.canRead() && it.isFile && it.name.endsWith(".png")}?.map{
            val bytes = it.readBytes()
            val bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.size)
            InternPhoto(it.name, bmp)
        } ?: listOf()
    }
}
private fun saveImage(bitmap:Bitmap,context: Context){
    try{
        context.openFileOutput("Collage_${System.currentTimeMillis()}.png", MODE_PRIVATE).use{
            stream -> if (!bitmap.compress(Bitmap.CompressFormat.PNG,100,stream)){
                throw IOException ("Couldn't save Bitmap")
        }
        }
    } catch(e: IOException){
        e.printStackTrace()
    }
}
fun exportImage(bitmap: Bitmap, context: Context){
    val resolver = context.contentResolver //a bridge between the app and photos gallery like API handler
    val contentValues = ContentValues().apply{ //to store metadata(key-value container) about the collage to save.Also "apply" refers to applying these attributes to this object of ContentValues()
        put(MediaStore.MediaColumns.DISPLAY_NAME, "Collage_${System.currentTimeMillis()}.png")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/DeltaImages")
    }
    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    //tells Android new media entry using this metadata and android returns location of newly created img
    imageUri?.let{
        val outputStream = resolver.openOutputStream(it)
        outputStream?.use{
            stream -> bitmap.compress(Bitmap.CompressFormat.PNG,100,stream)
        }
    }

}
@Composable
fun GridCell(indexnum:Int, onClick: () -> Unit, modifier:Modifier=Modifier //Modifier=Modifier makes passing Modifier optional during fx call
             ,currentPics: List<Int?>,viewMode: Boolean,bgColouri:Color){ //to manage each grid cell
    var X by remember{   //without remember values reset after every recomposition
        mutableStateOf(0f)} //with mutableStateOf UI recomposes automatically everytime the value changes
    var Y by remember{
        mutableStateOf(0f)} //all gesture values are handled with float values
    var zoomscale by remember{
        mutableStateOf(1f)} //default zoom=1 like nothing
    var rotation by remember{
        mutableStateOf(0f)} //in degree
    Box(
        modifier = modifier.clip(RoundedCornerShape(2.dp)).background(color = bgColouri).clickable{ onClick()}
    ){
        currentPics[indexnum]?.let { pic -> //if Image exists, render it as pic. Otherwise, nothing.
            Image(
                painter = painterResource(pic),
                contentDescription = null,
                contentScale = ContentScale.Crop, //zooms image and fits it to the cell, removes extra parts if any
                modifier = Modifier.fillMaxSize()
                    .graphicsLayer{ //applies transformations to composable(zoom,rotate)
                    scaleX= zoomscale
                    scaleY= zoomscale //together zooms by same scale in both directions
                    rotationZ=rotation //rotates around Z axis
                    }
                    .offset{ //(drag)
                        IntOffset((X).toInt(),Y.toInt()) //moves image position
                    }
                    .pointerInput(Unit){ //handles drag,pinches,rotations and other touch events
                    detectTransformGestures { _, pan, zoom, rotate ->
                        X+=pan.x
                        Y+=pan.y
                        zoomscale*=zoom  //as these get changed, states get updated, graphicslayer and offset value changes, img moves
                        rotation+=rotate
                        }
                    }
                        .pointerInput(Unit){ //hmm there can be multiple pointerInputs
                        detectTapGestures(
                            onDoubleTap= {
                                X=0f
                                Y=0f
                                zoomscale=1f
                                rotation=0f
                            }
                        )
                    }

            )
        }
    }
}
@Composable
fun ShowPic(details:Uri){
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current //dp for layouting and px for gestures
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val containerHeightPx = with(density) { maxHeight.toPx() }
        val imageSizeDp = maxWidth * 0.3f

        var x by remember {
            mutableStateOf<Float>((maxWidth.value-imageSizeDp.value)/2f)
        }

        var y by remember {
            mutableStateOf<Float>((maxHeight.value-imageSizeDp.value)/2f)
        }

        var zoomi by remember {
            mutableStateOf(1f)
        }

        var rotate by remember {
            mutableStateOf(0f)
        }
        Image(
            painter = rememberAsyncImagePainter(model = details),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(imageSizeDp).graphicsLayer {
                translationX = x
                translationY = y
                scaleX = zoomi
                scaleY = zoomi
                rotationZ = rotate
            }.pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, rotation ->
                    x += pan.x
                    y += pan.y
                    zoomi *= zoom
                    rotate += rotation
                }
            }
        )
    }
}
@Composable
fun SavedPicsScreen(viewMode:Boolean,navController: NavController){
    val context = LocalContext.current

        var photos by remember{
            mutableStateOf(listOf<InternPhoto>())
        }
    LaunchedEffect(Unit) { //imp for launching coroutine only once can't be safely kept under composable without this
        photos = loadSavedImages(context)
    }
    Box(modifier = Modifier.fillMaxSize().background(if (viewMode)Color.Black else Color.White ).padding(vertical=AdaptiveDp(40f),horizontal = AdaptiveDp(16f))) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().height(AdaptiveDp(40f)).clip(RoundedCornerShape(12.dp))
                    .background(color = if (viewMode) Color.White else Color.Black).padding(AdaptiveDp(5f))
            ) {
                Text(
                    text = "SAVED IMAGES",
                    color = if (viewMode) Color.Black else Color.White,
                    fontSize = AdaptiveSp(20f),
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = {
                        navController.navigate("home")
                    }) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = if (viewMode) Color.Black else Color.White,
                        modifier = Modifier.size(AdaptiveDp(20f))
                    )
                }
            }
            Spacer(modifier=Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = AdaptiveDp(128f)),
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(AdaptiveDp(8f)),
                horizontalArrangement = Arrangement.spacedBy(AdaptiveDp(8f))
            ) {
                items(photos) {
                    photo ->
                    Image(
                        bitmap = photo.bmp.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
@Composable
fun ScrapbookScreen(viewMode:Boolean, navController: NavController){
    val editorViewModel: EditorViewModel = viewModel()
    var extend4 by remember{ mutableStateOf(false)}
    var extend5 by remember{ mutableStateOf(false)}
    val scope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val context = LocalContext.current
    val bgColors = listOf("White","Black","Red","Blue","Green","Yellow")
    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                imageUri = it
                editorViewModel.scrapPics = editorViewModel.scrapPics + ScrapPicStuffs(it,50f,80f,1f,0f)
            }
        }
    )
    Box(modifier = Modifier.fillMaxSize().background(color = if(viewMode) Color.Black else Color.White)){
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top=AdaptiveDp(40f),bottom=AdaptiveDp(40f),start=AdaptiveDp(20f),end=AdaptiveDp(20f))){
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(AdaptiveDp(50f)).clip(shape = RoundedCornerShape(10.dp)).background(color = if (viewMode) Color.White else Color.Black)){
                Text(
                    text = "  SCRAPBOOK FREE-MODE",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Cursive,
                    fontSize = AdaptiveSp(20f),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if(viewMode)Color.Black else Color.White
                )
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,

                    tint = if (viewMode) Color.Black else Color.White,
                    modifier = Modifier.padding(end=12.dp).size(AdaptiveDp(30f)).clickable{navController.popBackStack()}
                )
            }
            Spacer(modifier = Modifier.height(15.dp))
            BoxWithConstraints(modifier=Modifier.fillMaxWidth()) {
                var newHeight = maxWidth*1.2f
                Box(modifier = Modifier.drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    drawLayer(graphicsLayer)
                }) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(newHeight).clip(
                            RoundedCornerShape(
                                editorViewModel.scrapRounding
                            )
                        ).background(color = editorViewModel.scrapBg)
                            .border(width = 5.dp, color = Color.Black)
                    ) {
                        editorViewModel.scrapPics.forEach { stuffs ->
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.TopStart
                            ) {
                                ShowPic(stuffs.id)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(15.dp))
            var val3 = editorViewModel.scrapRounding.value.toInt()
            Row(horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.wrapContentWidth().height(30.dp).clip(shape = RoundedCornerShape(10.dp)).background(color = if (viewMode) Color.White else Color.Black).align(Alignment.Start)){
                Text(
                    text = "   RADIUS: $val3  ",
                    fontWeight = FontWeight.Bold,
                    fontSize = AdaptiveSp(16f),
                    color = if (viewMode) Color.Black else Color.White
                )
            }
            Spacer(Modifier.height(8.dp))
            Slider(
                value = editorViewModel.scrapRounding.value,
                onValueChange = {
                    editorViewModel.scrapRounding = it.dp
                },
                valueRange = (0.dp).value..(100.dp).value
            )
            Spacer(Modifier.height(5.dp))
            Column {
                imageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(model = imageUri),
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(50.dp)
                            .align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Button(
                        onClick = {
                            galleryLauncher.launch("image/*")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewMode) Color.White else Color.Black,
                            contentColor = if (viewMode) Color.Black else Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = "ADD PICS",
                            fontWeight = FontWeight.Bold,
                            fontSize = AdaptiveSp(16f)
                        )
                    }

                    Box(
                        modifier = Modifier.weight(1f)
                    ) {

                        Button(
                            onClick = { extend4 = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (viewMode) Color.White else Color.Black,
                                contentColor = if (viewMode) Color.Black else Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Text(
                                text = "BG COLOR",
                                fontWeight = FontWeight.Bold,
                                fontSize = AdaptiveSp(16f)
                            )
                        }

                        DropdownMenu(
                            expanded = extend4,
                            onDismissRequest = { extend4 = false }
                        ) {

                            bgColors.forEach { colori ->

                                DropdownMenuItem(
                                    text = { Text(colori) },

                                    onClick = {

                                        when (colori) {
                                            "White" -> editorViewModel.scrapBg = Color.White
                                            "Black" -> editorViewModel.scrapBg = Color.Black
                                            "Red" -> editorViewModel.scrapBg = Color.Red
                                            "Green" -> editorViewModel.scrapBg = Color.Green
                                            "Yellow" -> editorViewModel.scrapBg = Color.Yellow
                                            "Blue" -> editorViewModel.scrapBg = Color.Blue
                                        }

                                        extend4 = false
                                    }
                                )
                            }
                        }
                    }
                }

            }
            Row(horizontalArrangement = Arrangement.Absolute.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically){
                Button(onClick = { scope.launch{
                    val imageBitmap = graphicsLayer.toImageBitmap()
                    val bitmap = imageBitmap.asAndroidBitmap()
                    saveImage(bitmap, context)
                    Toast.makeText(context, "Saved successfully", Toast.LENGTH_LONG).show()
                }
                }, colors= ButtonDefaults.buttonColors(containerColor = if (viewMode){Color.White} else {Color.Black}, contentColor = if (viewMode){Color.Black} else {Color.White}), modifier = Modifier.weight(1f)
                ){
                    Text(
                        text = "   SAVE   ",
                        fontWeight = FontWeight.Bold,
                        fontSize = AdaptiveSp(16f)
                    )
                }
                Button(onClick = { scope.launch{
                    val imageBitmap = graphicsLayer.toImageBitmap()
                    val bitmap = imageBitmap.asAndroidBitmap()
                    exportImage(bitmap, context)
                    Toast.makeText(context, "Exported successfully", Toast.LENGTH_LONG).show()
                }
                }, colors= ButtonDefaults.buttonColors(containerColor = if (viewMode){Color.White} else {Color.Black}, contentColor = if (viewMode){Color.Black} else {Color.White}), modifier = Modifier.weight(1f)
                ){
                    Text(
                        text = "   EXPORT  ",
                        fontWeight = FontWeight.Bold,
                        fontSize = AdaptiveSp(16f)
                    )
                }
            }


        }
    }
}

@Composable
fun OverlayText(
    details: TextOverlayStuffs,
    onDragging: (Float, Float) -> Unit
) {

    BoxWithConstraints {

        val density = LocalDensity.current

        val maxWidthPx =
            with(density) {
                maxWidth.toPx()
            }

        val maxHeightPx =
            with(density) {
                maxHeight.toPx()
            }

        val actualX =
            details.relativeX * maxWidthPx

        val actualY =
            details.relativeY * maxHeightPx

        Text(
            text = details.text,
            color = details.textColor,
            fontSize = AdaptiveSp(30f),
            fontFamily = details.textFont,
            fontWeight = FontWeight.Bold,

            modifier = Modifier
                .offset {
                    IntOffset(
                        actualX.toInt(),
                        actualY.toInt()
                    )
                }
                .pointerInput(Unit) {

                    detectDragGestures { _, dragAmount ->

                        val deltaRelativeX =
                            dragAmount.x / maxWidthPx

                        val deltaRelativeY =
                            dragAmount.y / maxHeightPx

                        onDragging(
                            deltaRelativeX,
                            deltaRelativeY
                        )
                    }
                }
        )
    }
}


@Composable
fun SecondScreen(selectedLayoutT: LayoutT,modifier: Modifier=Modifier, navController: NavController,viewMode:Boolean,bgColour: Color,colorChange:(Color)->Unit){
    val editorViewModel: EditorViewModel = viewModel() //this function creates ViewModel if not existing and reuses old one if existing
    val picList = listOf<Int>(
        R.drawable.pic1,
        R.drawable.pic2,
        R.drawable.pic3,
        R.drawable.pic4,
        R.drawable.pic5
    )
    var extend1 by remember{
        mutableStateOf(false)
    }

    val textStyles = listOf("Normal","Serif","SansSerif","Monospace","Cursive")

    var extend2 by remember{
        mutableStateOf(false)
    }
    val textColors = listOf("White","Black","Red","Blue","Green","Yellow")
    var extend3 by remember{
        mutableStateOf(false)
    }

    val context = LocalContext.current//to access Android APIs too
    //for Android Environment access for storage, files, toasts, permissions,database,system services etc
    val graphicsLayer = rememberGraphicsLayer() //graphics recording layer which later -> image object -> bitmap
    val scope = rememberCoroutineScope() //for asynchronous coroutine tasks to create scope for running suspend functions
    Box(modifier= Modifier.background(if (viewMode){Color.Black} else {Color.White}).fillMaxSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(start=AdaptiveDp(16f),end=AdaptiveDp(16f),top=AdaptiveDp(40f),bottom=AdaptiveDp(40f)).verticalScroll(rememberScrollState())){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (viewMode){Color.White} else {Color.Black},
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp),

                horizontalArrangement = Arrangement.SpaceBetween,

                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "GALLERY",
                    color = if (viewMode){Color.Black} else {Color.White},
                    fontSize = AdaptiveSp(30f),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Icon(
                    imageVector = Icons.Default.Home, //selecting icon from Default Icon style pack of Compose Material icon collection
                    contentDescription = null, //needed for both images and icons...some accessibility thing
                    tint = if (viewMode){Color.Black} else {Color.White}, //color of icon
                    modifier = Modifier
                        .size(AdaptiveDp(30f))
                        .clickable {
                            navController.navigate("home")
                        }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "CUSTOM EDITOR",
                color = if (viewMode){Color.White} else {Color.Black},
                fontSize = AdaptiveSp(25f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(16.dp))
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                var newHeight = maxWidth*0.7f
                Box(
                    modifier = Modifier
                        .height(newHeight)
                        .fillMaxWidth()
                ) {
                    when (selectedLayoutT) {

                        LayoutT.TWO_GRID -> {

                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(editorViewModel.rounding)) //without clip children can go outside boundaries of box
                                    .background(
                                        if (viewMode) {
                                            Color.Black
                                        } else {
                                            Color.White
                                        }
                                    )
                                    .drawWithContent { //to get temp. control on collage rendering over composable
                                        graphicsLayer.record { //record everything here to graphicsLayer canvas created earlier
                                            this@drawWithContent.drawContent() //to draw normal UI too along with recording
                                        }
                                        drawLayer(graphicsLayer) //to display the drawn/recorded collage content

                                    }
                            ) {

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(editorViewModel.spacing),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    GridCell(
                                        indexnum = 0,
                                        currentPics = editorViewModel.currentPics,
                                        onClick = { editorViewModel.currentBox = 0 },
                                        modifier = Modifier.weight(1f).fillMaxHeight(),
                                        viewMode = viewMode,
                                        bgColouri = bgColour
                                    )
                                    GridCell(
                                        indexnum = 1,
                                        currentPics = editorViewModel.currentPics,
                                        onClick = { editorViewModel.currentBox = 1 },
                                        modifier = Modifier.weight(1f).fillMaxHeight(),
                                        viewMode = viewMode,
                                        bgColouri = bgColour
                                    )
                                }
                                editorViewModel.listOfTextOvers.forEachIndexed { index, item ->
                                    OverlayText(
                                        details = item,
                                        onDragging = { dxPercent, dyPercent ->
                                            editorViewModel.listOfTextOvers =
                                                editorViewModel.listOfTextOvers.toMutableList()
                                                    .also {
                                                        it[index] = it[index].copy(
                                                            relativeX = (it[index].relativeX + dxPercent).coerceIn(
                                                                0f,
                                                                1f
                                                            ),
                                                            relativeY = (it[index].relativeY + dyPercent).coerceIn(
                                                                0f,
                                                                1f
                                                            )
                                                        )
                                                    }
                                        })
                                }
                            }
                        }


                        LayoutT.THREE_GRID -> {

                            Box(
                                modifier = Modifier
                                    .height(newHeight)
                                    .clip(RoundedCornerShape(editorViewModel.rounding))
                                    .background(
                                        if (viewMode) {
                                            Color.Black
                                        } else {
                                            Color.White
                                        }
                                    ).drawWithContent {
                                        graphicsLayer.record {
                                            this@drawWithContent.drawContent()
                                        }
                                        drawLayer(graphicsLayer)
                                    }
                            ) {


                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(editorViewModel.spacing),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    GridCell(
                                        indexnum = 0,
                                        currentPics = editorViewModel.currentPics,
                                        onClick = { editorViewModel.currentBox = 0 },
                                        modifier = Modifier.weight(1f).fillMaxHeight(),
                                        viewMode = viewMode,
                                        bgColouri = bgColour
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (viewMode) {
                                                    Color.Black
                                                } else {
                                                    Color.White
                                                }
                                            )
                                            .fillMaxHeight()
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(
                                                editorViewModel.spacing
                                            )
                                        ) {
                                            GridCell(
                                                indexnum = 1,
                                                currentPics = editorViewModel.currentPics,
                                                onClick = { editorViewModel.currentBox = 1 },
                                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                                viewMode = viewMode,
                                                bgColouri = bgColour
                                            )
                                            GridCell(
                                                indexnum = 2,
                                                currentPics = editorViewModel.currentPics,
                                                onClick = { editorViewModel.currentBox = 2 },
                                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                                viewMode = viewMode,
                                                bgColouri = bgColour
                                            )
                                        }
                                    }
                                }
                            }
                            editorViewModel.listOfTextOvers.forEachIndexed { index, item ->
                                OverlayText(details = item, onDragging = { dxPercent, dyPercent ->
                                    editorViewModel.listOfTextOvers =
                                        editorViewModel.listOfTextOvers.toMutableList().also {
                                            it[index] = it[index].copy(
                                                relativeX = (it[index].relativeX + dxPercent).coerceIn(
                                                    0f,
                                                    1f
                                                ),
                                                relativeY = (it[index].relativeY + dyPercent).coerceIn(
                                                    0f,
                                                    1f
                                                )
                                            )
                                        }
                                })
                            }
                        }

                        LayoutT.FOUR_GRID -> {

                            Box(
                                modifier = Modifier
                                    .height(newHeight)
                                    .clip(RoundedCornerShape(editorViewModel.rounding))
                                    .background(
                                        if (viewMode) {
                                            Color.Black
                                        } else {
                                            Color.White
                                        }
                                    ).drawWithContent {
                                        graphicsLayer.record {
                                            this@drawWithContent.drawContent()
                                        }
                                        drawLayer(graphicsLayer)
                                    }
                            ) {

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(editorViewModel.spacing),
                                    modifier = Modifier.fillMaxSize()
                                ) {

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (viewMode) {
                                                    Color.Black
                                                } else {
                                                    Color.White
                                                }
                                            )
                                            .fillMaxHeight()
                                    ) {

                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(
                                                editorViewModel.spacing
                                            )
                                        ) {
                                            GridCell(
                                                indexnum = 0,
                                                currentPics = editorViewModel.currentPics,
                                                onClick = { editorViewModel.currentBox = 0 },
                                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                                viewMode = viewMode,
                                                bgColouri = bgColour
                                            )
                                            GridCell(
                                                indexnum = 1,
                                                currentPics = editorViewModel.currentPics,
                                                onClick = { editorViewModel.currentBox = 1 },
                                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                                viewMode = viewMode,
                                                bgColouri = bgColour
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (viewMode) {
                                                    Color.Black
                                                } else {
                                                    Color.White
                                                }
                                            )
                                            .fillMaxHeight()
                                    ) {

                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(
                                                editorViewModel.spacing
                                            )
                                        ) {
                                            GridCell(
                                                indexnum = 2,
                                                currentPics = editorViewModel.currentPics,
                                                onClick = { editorViewModel.currentBox = 2 },
                                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                                viewMode = viewMode,
                                                bgColouri = bgColour
                                            )
                                            GridCell(
                                                indexnum = 3,
                                                currentPics = editorViewModel.currentPics,
                                                onClick = { editorViewModel.currentBox = 3 },
                                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                                viewMode = viewMode,
                                                bgColouri = bgColour
                                            )
                                        }
                                    }
                                }
                                editorViewModel.listOfTextOvers.forEachIndexed { index, item ->
                                    OverlayText(
                                        details = item,
                                        onDragging = { dxPercent, dyPercent ->
                                            editorViewModel.listOfTextOvers =
                                                editorViewModel.listOfTextOvers.toMutableList()
                                                    .also {
                                                        it[index] = it[index].copy(
                                                            relativeX = (it[index].relativeX + dxPercent).coerceIn(
                                                                0f,
                                                                1f
                                                            ),
                                                            relativeY = (it[index].relativeY + dyPercent).coerceIn(
                                                                0f,
                                                                1f
                                                            )
                                                        )
                                                    }
                                        })
                                }

                            }
                        }

                        LayoutT.FIVE_GRID -> {

                            Box(
                                modifier = Modifier
                                    .height(newHeight)
                                    .clip(RoundedCornerShape(editorViewModel.rounding))
                                    .background(
                                        if (viewMode) {
                                            Color.Black
                                        } else {
                                            Color.White
                                        }
                                    ).drawWithContent {
                                        graphicsLayer.record {
                                            this@drawWithContent.drawContent()
                                        }
                                        drawLayer(graphicsLayer)
                                    }
                            ) {

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(editorViewModel.spacing),
                                    modifier = Modifier.fillMaxSize()
                                ) {

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (viewMode) {
                                                    Color.Black
                                                } else {
                                                    Color.White
                                                }
                                            )
                                            .fillMaxHeight()
                                    ) {

                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(
                                                editorViewModel.spacing
                                            )
                                        ) {
                                            GridCell(
                                                indexnum = 0,
                                                currentPics = editorViewModel.currentPics,
                                                onClick = { editorViewModel.currentBox = 0 },
                                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                                viewMode = viewMode,
                                                bgColouri = bgColour
                                            )
                                            GridCell(
                                                indexnum = 1,
                                                currentPics = editorViewModel.currentPics,
                                                onClick = { editorViewModel.currentBox = 1 },
                                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                                viewMode = viewMode,
                                                bgColouri = bgColour
                                            )
                                            GridCell(
                                                indexnum = 2,
                                                currentPics = editorViewModel.currentPics,
                                                onClick = { editorViewModel.currentBox = 2 },
                                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                                viewMode = viewMode,
                                                bgColouri = bgColour
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (viewMode) {
                                                    Color.Black
                                                } else {
                                                    Color.White
                                                }
                                            )
                                            .fillMaxHeight()
                                    ) {

                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(
                                                editorViewModel.spacing
                                            )
                                        ) {
                                            GridCell(
                                                indexnum = 3,
                                                currentPics = editorViewModel.currentPics,
                                                onClick = { editorViewModel.currentBox = 3 },
                                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                                viewMode = viewMode,
                                                bgColouri = bgColour
                                            )
                                            GridCell(
                                                indexnum = 4,
                                                currentPics = editorViewModel.currentPics,
                                                onClick = { editorViewModel.currentBox = 4 },
                                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                                viewMode = viewMode,
                                                bgColouri = bgColour
                                            )
                                        }
                                    }
                                }
                                editorViewModel.listOfTextOvers.forEachIndexed { index, item ->
                                    OverlayText(
                                        details = item,
                                        onDragging = { dxPercent, dyPercent ->
                                            editorViewModel.listOfTextOvers =
                                                editorViewModel.listOfTextOvers.toMutableList()
                                                    .also {
                                                        it[index] = it[index].copy(
                                                            relativeX = (it[index].relativeX + dxPercent).coerceIn(
                                                                0f,
                                                                1f
                                                            ),
                                                            relativeY = (it[index].relativeY + dyPercent).coerceIn(
                                                                0f,
                                                                1f
                                                            )
                                                        )
                                                    }
                                        })
                                }

                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            var intVal1 = editorViewModel.rounding.value.toInt()
            var intVal2 = editorViewModel.spacing.value.toInt()
            Text(
                text = "RADIUS:$intVal1  ",
                modifier = Modifier.background(if (viewMode){Color.White} else {Color.Black},shape = RoundedCornerShape(10.dp)).align(Alignment.Start).padding(start=5.dp),
                fontSize = AdaptiveSp(25f),
                color = if (viewMode){Color.Black} else {Color.White},
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = editorViewModel.rounding.value,
                onValueChange = {
                    editorViewModel.rounding = it.dp
                },
                valueRange = (0.dp).value..(100.dp).value

            )

            Spacer(modifier = Modifier.height(15.dp))
            Text(
                text = "SPACING:$intVal2 ",
                modifier = Modifier.background(if (viewMode){Color.White} else {Color.Black},shape = RoundedCornerShape(10.dp)).align(Alignment.Start).padding(start=5.dp),
                fontSize = AdaptiveSp(25f),
                color = if (viewMode){Color.Black} else {Color.White},
                fontWeight = FontWeight.Bold

            )
            Slider(
                value = editorViewModel.spacing.value,
                onValueChange = {
                    editorViewModel.spacing = it.dp
                },
                valueRange = (0.dp).value..(100.dp).value

            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(AdaptiveDp(40f)).clip(RoundedCornerShape(12.dp)).background(color=if(viewMode)Color.White else Color.Black)){
                Spacer(modifier=Modifier.height(2.dp))
                Button( modifier=Modifier.weight(1f).height(AdaptiveDp(30f)),onClick={ scope.launch { //to start a coroutine/asynchronous task without freezing UI
                    Toast.makeText(context, "Saved successfully", Toast.LENGTH_LONG).show()
                    val imageBitmap = graphicsLayer.toImageBitmap() //a suspend function to render image object
                    val bitmap = imageBitmap.asAndroidBitmap()
                    saveImage(bitmap,context)
                } },
                    colors= ButtonDefaults.buttonColors(contentColor = if(viewMode) {Color.White} else {Color.Black}, containerColor = if(viewMode) {Color.Black} else {Color.White} )){
                    Text(
                        text = "SAVE",
                        fontWeight = FontWeight.Bold,
                        fontSize = AdaptiveSp(15f),
                        textAlign = TextAlign.Center
                    )
                }
                Button( modifier=Modifier.weight(1f).height(AdaptiveDp(30f)), onClick={ scope.launch{
                    Toast.makeText(context,"Exported successfully",Toast.LENGTH_LONG).show()
                    val imageBitmap = graphicsLayer.toImageBitmap()
                    val bitmap = imageBitmap.asAndroidBitmap()
                    exportImage(bitmap,context)
                } },
                    colors= ButtonDefaults.buttonColors(contentColor = if(viewMode) {Color.White} else {Color.Black}, containerColor = if(viewMode) {Color.Black} else {Color.White} )){
                    Text(
                        text = "EXPORT",
                        fontWeight = FontWeight.Bold,
                        fontSize = AdaptiveSp(15f),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier=Modifier.height(2.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box {
                Button(
                    onClick = {extend3=true}, colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewMode) {
                            Color.White
                        } else {
                            Color.Black
                        },
                        contentColor = if (viewMode) {
                            Color.Black
                        } else {
                            Color.White
                        }
                    )
                ) {
                    Text(
                        text = "BACKGROUND COLOR",
                        fontSize = AdaptiveSp(18f),
                        fontWeight = FontWeight.Bold
                    )
                }
                DropdownMenu(expanded=extend3, onDismissRequest = {extend3=false}) {
                    textColors.forEach { item ->
                        DropdownMenuItem(
                            text = {
                                Text(item)},
                            onClick = {
                                when(item){
                                    "White"->colorChange(Color.White)
                                    "Black" -> colorChange(Color.Black)
                                    "Red" -> colorChange(Color.Red)
                                    "Blue" -> colorChange(Color.Blue)
                                    "Green" -> colorChange(Color.Green)
                                    "Yellow" -> colorChange(Color.Yellow)
                                }
                                extend3=false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        editorViewModel.displayOverlay = true
                        editorViewModel.showDone = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewMode){Color.White} else {Color.Black},
                        contentColor = if (viewMode){Color.Black} else {Color.White}
                    )
                ) {
                    Text(
                        text = "ADD TEXT",
                        fontSize = AdaptiveSp(18f),
                        fontWeight = FontWeight.Bold
                    )
                }
                if (editorViewModel.displayOverlay) {
                    TextField(
                        value = editorViewModel.textOverlay,
                        onValueChange = {
                            editorViewModel.textOverlay = it
                        },
                        modifier = Modifier.weight(1f)
                    )
                }


                if (editorViewModel.showDone) {
                    Button(
                        onClick = { editorViewModel.listOfTextOvers =
                            editorViewModel.listOfTextOvers + TextOverlayStuffs(text = editorViewModel.textOverlay, relativeX = 0f, relativeY = 0f, textColor = editorViewModel.selectedColor, textFont = editorViewModel.selectedStyle)
//creating new list for recomposing UI (oldlist + new item)
                            editorViewModel.displayOverlay = false
                            editorViewModel.showDone = false
                            editorViewModel.textOverlay = "" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewMode){Color.White} else {Color.Black},
                            contentColor = if (viewMode){Color.Black} else {Color.White}
                        )
                    ) {
                        Text(
                            text = "DONE",
                            fontSize = AdaptiveSp(18f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically){
                if (editorViewModel.displayOverlay){
                    Box{
                        Button(onClick = {extend1=true}, colors = ButtonDefaults.buttonColors(containerColor = if (viewMode){Color.White} else {Color.Black}, contentColor = if (viewMode){Color.Black} else {Color.White})){
                            Text(text="TEXT STYLE",
                                fontSize=AdaptiveSp(18f))
                        }
                        DropdownMenu(expanded=extend1, onDismissRequest = {extend1=false}) {
                            textStyles.forEach {
                                    item ->
                                DropdownMenuItem(
                                    text = {
                                        Text(item)},
                                    onClick = {
                                        when(item){
                                            "Normal"->editorViewModel.selectedStyle= FontFamily.Default
                                            "Serif" -> editorViewModel.selectedStyle = FontFamily.Serif
                                            "SansSerif" -> editorViewModel.selectedStyle = FontFamily.SansSerif
                                            "Monospace" -> editorViewModel.selectedStyle = FontFamily.Monospace
                                            "Cursive" -> editorViewModel.selectedStyle = FontFamily.Cursive
                                        }
                                        extend1=false
                                    }
                                )
                            }
                        }
                    }
                }
                if (editorViewModel.displayOverlay){
                    Box{
                        Button(onClick = {extend2=true}, colors = ButtonDefaults.buttonColors(containerColor = if (viewMode){Color.White} else {Color.Black}, contentColor = if (viewMode){Color.Black} else {Color.White})){
                            Text(text="TEXT COLOR",
                                fontSize = AdaptiveSp(18f))
                        }
                        DropdownMenu(expanded=extend2, onDismissRequest = {extend2=false}) {
                            textColors.forEach {
                                    item ->
                                DropdownMenuItem(
                                    text = {
                                        Text(item)},
                                    onClick = {
                                        when(item){
                                            "White"->editorViewModel.selectedColor= Color.White
                                            "Black" -> editorViewModel.selectedColor= Color.Black
                                            "Red" -> editorViewModel.selectedColor= Color.Red
                                            "Blue" -> editorViewModel.selectedColor= Color.Blue
                                            "Green" -> editorViewModel.selectedColor= Color.Green
                                            "Yellow" -> editorViewModel.selectedColor= Color.Yellow
                                        }
                                        extend2=false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Spacer( modifier = Modifier.height(15.dp))
            if (editorViewModel.currentBox != null) {

                Column(
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                ) {

                    Text(
                        text = "SELECT IMAGE",
                        modifier = Modifier
                            .background(
                                color = if (viewMode){Color.Black} else {Color.White},
                                shape = RoundedCornerShape(12.dp)
                            ).align(Alignment.Start),
                        fontSize = AdaptiveSp(18f),
                        fontWeight = FontWeight.Bold,
                        color = if (viewMode){Color.White} else {Color.Black}
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    LazyRow(
                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {

                        items(picList) { pic ->

                            Image(
                                painter = painterResource(pic),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,

                                modifier = Modifier
                                    .size(AdaptiveDp(80f))

                                    .border(
                                        width =
                                            if (editorViewModel.selectedPic == pic){ 4.dp}
                                            else{ 0.dp},
                                        color =
                                            if (editorViewModel.selectedPic == pic) {Color.Blue}
                                            else {Color.Transparent},

                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    .clip(RoundedCornerShape(12.dp))

                                    .clickable {
                                        editorViewModel.selectedPic = pic
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    if (editorViewModel.selectedPic != null) {

                        Button(
                            onClick = {

                                editorViewModel.currentPics =
                                    editorViewModel.currentPics.toMutableList().also {

                                        it[editorViewModel.currentBox!!] = editorViewModel.selectedPic
                                    }

                                editorViewModel.selectedPic = null

                                editorViewModel.currentBox = null
                            },
                            colors = ButtonDefaults.buttonColors(contentColor = if (viewMode){Color.White} else {Color.Black}, containerColor = if (viewMode){Color.Black} else {Color.White})
                        ) {

                            Text(
                                text = "DONE",
                                fontSize = AdaptiveSp(18f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

        }

    }

}
@Composable
fun ScreenHome(navController: NavController, selectedLayout:LayoutT?, layoutSelection:(LayoutT) -> Unit,viewMode:Boolean, changeView: ()->Unit,bgColour: Color,colorChange:(Color)->Unit) {

        Box(modifier = Modifier.background(color=if (viewMode){Color.Black} else {Color.White}).fillMaxSize()) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(start=AdaptiveDp(16f),end=AdaptiveDp(16f),top=AdaptiveDp(40f),bottom=AdaptiveDp(40f)).verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier=Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(color= if (viewMode){Color.White} else {Color.Black}).height(45.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "GALLERY",
                        color = if (viewMode){Color.Black} else {Color.White},
                        fontSize = AdaptiveSp(30f),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Button(onClick={changeView()
                        val newie = !viewMode
                        colorChange(if (newie) Color.White else Color.Black)

                                   }, colors = ButtonDefaults.buttonColors(containerColor = if (viewMode){Color.Black} else {Color.White}, contentColor = if (viewMode){Color.White} else {Color.Black})){
                        Text(
                            text = if (viewMode) "LIGHT MODE" else "DARK MODE",
                            fontSize =AdaptiveSp(10f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                        horizontalArrangement = Arrangement.Absolute.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().height(AdaptiveDp(40f)).clip(RoundedCornerShape(12.dp))
                    .background(color = if (viewMode) Color.White else Color.Black).padding(AdaptiveDp(5f)).clickable{
                        navController.navigate("savedpics")
                    }
                ) {
                Text(
                    text = "SAVED IMAGES",
                    color = if (viewMode) Color.Black else Color.White,
                    fontSize = AdaptiveSp(20f),
                    fontWeight = FontWeight.Bold
                )}


                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "CHOOSE LAYOUT",
                    color = if (viewMode){Color.White} else {Color.Black},
                    fontSize = AdaptiveSp(30f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(horizontalArrangement = Arrangement.Absolute.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(40.dp).clip(shape = RoundedCornerShape(10.dp)).background(color = if (viewMode) Color.White else Color.Black).
                clickable{
                    navController.navigate("scrapbook")
                }){
                    Text(
                        text = "SCRAPBOOK MODE",
                        fontSize = AdaptiveSp(24f),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Cursive,
                        color = if(viewMode)Color.Black else Color.White

                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "2 GRID",
                        color = if (viewMode){Color.Black} else {Color.White},
                        fontSize = AdaptiveSp(24f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).background(
                            color = if (viewMode){Color.White} else {Color.Black},
                            shape = RoundedCornerShape(12.dp)
                        ).padding(vertical=4.dp)
                    )
                    Text(
                        text = "3 GRID",
                        color = if (viewMode){Color.Black} else {Color.White},
                        fontSize = AdaptiveSp(24f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).background(
                            color = if (viewMode){Color.White} else {Color.Black},
                            shape = RoundedCornerShape(12.dp)
                        ).padding(vertical=4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val newHeight = (maxWidth*0.6f)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth() //fill maximum width of parent column
                    ) {
                        Box(
                            modifier = Modifier.weight(1f).height(newHeight)
                                .border( //divide width equally cuz of two 1f
                                    width = if (selectedLayout == LayoutT.TWO_GRID) {
                                        5.dp
                                    } else {
                                        0.dp
                                    }, color = if (selectedLayout == LayoutT.TWO_GRID) {
                                        Color.Blue
                                    } else {
                                        Color.Transparent
                                    }, shape = RoundedCornerShape(7.dp)
                                ).clip(RoundedCornerShape(10.dp)).background(
                                color = if (viewMode) {
                                    Color.Black
                                } else {
                                    Color.White
                                }
                            )
                                .clickable {
                                    layoutSelection(
                                        LayoutT.TWO_GRID
                                    )
                                }) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.fillMaxSize() //fill maximum size of parent box...height, width taken care by parent
                            ) {
                                Box(
                                    Modifier.weight(1f).background(
                                        color = if (viewMode) {
                                            Color.White
                                        } else {
                                            Color.Black
                                        }
                                    ).fillMaxHeight()
                                ) {

                                }
                                Box(
                                    Modifier.weight(1f).background(
                                        color = if (viewMode) {
                                            Color.White
                                        } else {
                                            Color.Black
                                        }
                                    ).fillMaxHeight()
                                ) {

                                }
                            }
                        }
                        Box(
                            modifier = Modifier.weight(1f).height(newHeight).border(
                                width = if (selectedLayout == LayoutT.THREE_GRID) {
                                    5.dp
                                } else {
                                    0.dp
                                }, color = if (selectedLayout == LayoutT.THREE_GRID) {
                                    Color.Blue
                                } else {
                                    Color.Transparent
                                }, shape = RoundedCornerShape(7.dp)
                            ).clip(RoundedCornerShape(10.dp)).background(
                                color = if (viewMode) {
                                    Color.Black
                                } else {
                                    Color.White
                                }
                            )
                                .clickable {
                                    layoutSelection(
                                        LayoutT.THREE_GRID
                                    )
                                }) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(
                                    Modifier.weight(1f).background(
                                        color = if (viewMode) {
                                            Color.White
                                        } else {
                                            Color.Black
                                        }
                                    ).height(newHeight)

                                ) {

                                }
                                Box(
                                    Modifier.weight(1f).background(
                                        color = if (viewMode) {
                                            Color.Black
                                        } else {
                                            Color.White
                                        }
                                    ).height(newHeight)

                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(5.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Box(
                                            Modifier.weight(1f).background(
                                                color = if (viewMode) {
                                                    Color.White
                                                } else {
                                                    Color.Black
                                                }
                                            ).fillMaxWidth()

                                        ) {

                                        }
                                        Box(
                                            Modifier.weight(1f).background(
                                                color = if (viewMode) {
                                                    Color.White
                                                } else {
                                                    Color.Black
                                                }
                                            ).fillMaxWidth()
                                        ) {

                                        }
                                    }

                                }
                            }

                        }

                    }
                }
                Spacer(modifier = Modifier.height(15.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "4 GRID",
                        color = if (viewMode){Color.Black} else {Color.White},
                        fontSize = AdaptiveSp(24f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).background(
                            color = if (viewMode){Color.White} else {Color.Black},
                            shape = RoundedCornerShape(12.dp)
                        ).padding(vertical=4.dp)
                    )
                    Text(
                        text = "5 GRID",
                        color = if (viewMode){Color.Black} else {Color.White},
                        fontSize = AdaptiveSp(24f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).background(
                            color = if (viewMode){Color.White} else {Color.Black},
                            shape = RoundedCornerShape(12.dp)
                        ).padding(vertical=4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val newHeight = maxWidth*0.6f
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.weight(1f).height(newHeight).border(
                                width = if (selectedLayout == LayoutT.FOUR_GRID) {
                                    5.dp
                                } else {
                                    0.dp
                                }, color = if (selectedLayout == LayoutT.FOUR_GRID) {
                                    Color.Blue
                                } else {
                                    Color.Transparent
                                }, shape = RoundedCornerShape(7.dp)
                            ).clip(RoundedCornerShape(10.dp)).background(
                                color = if (viewMode) {
                                    Color.Black
                                } else {
                                    Color.White
                                }
                            )
                                .clickable {
                                    layoutSelection(
                                        LayoutT.FOUR_GRID
                                    )
                                }) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(
                                    Modifier.weight(1f).background(
                                        color = if (viewMode) {
                                            Color.Black
                                        } else {
                                            Color.White
                                        }
                                    ).height(newHeight)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Box(
                                            Modifier.weight(1f).background(
                                                color = if (viewMode) {
                                                    Color.White
                                                } else {
                                                    Color.Black
                                                }
                                            )
                                                .height(newHeight/2).fillMaxWidth()
                                        ) {

                                        }
                                        Box(
                                            Modifier.weight(1f).background(
                                                color = if (viewMode) {
                                                    Color.White
                                                } else {
                                                    Color.Black
                                                }
                                            )
                                                .height(newHeight/2).fillMaxWidth()
                                        ) {

                                        }
                                    }
                                }
                                Box(
                                    Modifier.weight(1f).background(
                                        color = if (viewMode) {
                                            Color.Black
                                        } else {
                                            Color.White
                                        }
                                    ).fillMaxHeight()
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Box(
                                            Modifier.weight(1f).background(
                                                color = if (viewMode) {
                                                    Color.White
                                                } else {
                                                    Color.Black
                                                }
                                            )
                                                .height(newHeight/2).fillMaxWidth()
                                        ) {

                                        }
                                        Box(
                                            Modifier.weight(1f).background(
                                                color = if (viewMode) {
                                                    Color.White
                                                } else {
                                                    Color.Black
                                                }
                                            )
                                                .height(newHeight/2).fillMaxWidth()
                                        ) {

                                        }
                                    }
                                }
                            }
                        }
                        Box(
                            modifier = Modifier.weight(1f).height(newHeight).border(
                                width = if (selectedLayout == LayoutT.FIVE_GRID) {
                                    5.dp
                                } else {
                                    0.dp
                                }, color = if (selectedLayout == LayoutT.FIVE_GRID) {
                                    Color.Blue
                                } else {
                                    Color.Transparent
                                }, shape = RoundedCornerShape(7.dp)
                            ).clip(RoundedCornerShape(10.dp)).background(
                                color = if (viewMode) {
                                    Color.Black
                                } else {
                                    Color.White
                                }
                            )
                                .clickable {
                                    layoutSelection(
                                        LayoutT.FIVE_GRID
                                    )
                                }) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(
                                    Modifier.weight(1f).background(
                                        color = if (viewMode) {
                                            Color.Black
                                        } else {
                                            Color.White
                                        }
                                    ).height(newHeight)
                                        .fillMaxWidth()
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(
                                            Modifier.weight(1f).background(
                                                color = if (viewMode) {
                                                    Color.White
                                                } else {
                                                    Color.Black
                                                }
                                            )
                                                .height(newHeight/2).fillMaxWidth()
                                        ) {

                                        }
                                        Box(
                                            Modifier.weight(1f).background(
                                                color = if (viewMode) {
                                                    Color.White
                                                } else {
                                                    Color.Black
                                                }
                                            )
                                                .height(newHeight/2).fillMaxWidth()
                                        ) {

                                        }
                                        Box(
                                            Modifier.weight(1f).background(
                                                color = if (viewMode) {
                                                    Color.White
                                                } else {
                                                    Color.Black
                                                }
                                            )
                                                .height(newHeight/2).fillMaxWidth()
                                        ) {

                                        }
                                    }
                                }
                                Box(
                                    Modifier.weight(1f).background(
                                        color = if (viewMode) {
                                            Color.Black
                                        } else {
                                            Color.White
                                        }
                                    ).fillMaxHeight()
                                        .fillMaxWidth()
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(
                                            Modifier.weight(1f).background(
                                                color = if (viewMode) {
                                                    Color.White
                                                } else {
                                                    Color.Black
                                                }
                                            )
                                                .height(newHeight/2).fillMaxWidth()
                                        ) {

                                        }
                                        Box(
                                            Modifier.weight(1f).background(
                                                color = if (viewMode) {
                                                    Color.White
                                                } else {
                                                    Color.Black
                                                }
                                            )
                                                .height(newHeight/2).fillMaxWidth()
                                        ) {

                                        }
                                    }

                                }
                            }

                        }

                    }
                }
                Spacer( modifier = Modifier.height(15.dp))
                if (selectedLayout != null){
                    Button(onClick = {navController.navigate("editor")},modifier = Modifier.padding(bottom=20.dp),colors= ButtonDefaults.buttonColors(containerColor = if (viewMode){Color.White} else {Color.Black}, contentColor = if (viewMode){Color.Black} else {Color.White})){
                        Text(
                            text = "NEXT",
                            fontSize = AdaptiveSp(25f),
                            modifier = Modifier.background(color = if (viewMode){Color.White} else {Color.Black}, shape = RoundedCornerShape(12.dp)),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

            }
        }

    }


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DeltaApp1Theme {
        val navController =
            rememberNavController()
        var selectedLayout by remember {
            mutableStateOf<LayoutT?>(LayoutT.TWO_GRID)
        }
        var viewMode by remember{
            mutableStateOf(true)
        }
        var bgColour by remember{
            mutableStateOf(if(viewMode)Color.White else Color.Black)
        }
        ScreenHome(
            navController=navController, selectedLayout=selectedLayout, layoutSelection = {layout -> selectedLayout = layout}, viewMode = viewMode, changeView = { viewMode = !viewMode},bgColour = bgColour,
            colorChange = {newcolor ->
               bgColour = newcolor
            }
        )
    }
}