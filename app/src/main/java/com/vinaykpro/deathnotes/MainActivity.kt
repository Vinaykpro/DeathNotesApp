package com.vinaykpro.deathnotes

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.os.IResultReceiver2.Default
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.deathnotes.ui.theme.DeathNotesTheme
import com.vinaykpro.deathnotes.ui.theme.RyukExtra
import dev.romainguy.kotlin.math.Quaternion
import io.github.sceneview.Scene
import io.github.sceneview.SceneView
import io.github.sceneview.animation.Transition.animatePosition
import io.github.sceneview.animation.Transition.animateRotation
import io.github.sceneview.gesture.MoveGestureDetector
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNode
import io.github.sceneview.utils.readBuffer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.viewinterop.AndroidView
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "userprefs")
val stringKey = stringPreferencesKey("promises")

private val termsAgreedKey = booleanPreferencesKey("termsagreed")
private lateinit var appOpenAdManager: AppOpenAdManager
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)
        appOpenAdManager = AppOpenAdManager()
        setContent {
            DeathNotesTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current;
                    val termsAgreed= remember { mutableStateOf(true) }
                    LaunchedEffect(Unit) {
                    val initialValue = context.dataStore.data
                        .map { preferences -> preferences[termsAgreedKey] ?: false }
                        .first()
                    termsAgreed.value = initialValue
                    }

                    if(termsAgreed.value)
                        Display3DModel()
                    else
                        DisplayTerms(context, termsAgreed)

                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i("AppOpenAd", "onPause")
        appOpenAdManager.loadAd(this)
    }

    override fun onRestart() {
        super.onRestart()
        Log.i("AppOpenAd", "onRestart")
        appOpenAdManager.showAd(this)
    }
}
val rules = listOf<String>(
    "The human who writes in this note is eternally bound to obey its contents; failure to comply shall result in death.",
    "Once inscribed, a rule cannot be altered, erased, or undone.",
    "If a promise conflicts with one already inscribed or is written with intent to disregard others, such a promise shall be nullified and hold no power within this note.",
    "The cause of death for the human will be decided based on the failed promise ensuring no other lives would be effected.",
    "The book must be opened at least once every day. Failure to do so will invoke a curse, and an unseen force will relentlessly seek to bring harm upon you."
)


@Composable
fun DisplayTerms(context : Context, termsAgreed : MutableState<Boolean>) {
    val skipText = remember { mutableStateOf("SkIP >>") }
    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val ruleIndex = remember { mutableStateOf(0) }
    val nextIndication = remember { mutableStateOf("") }
    val indicationAlpha = remember { mutableStateOf(0f) }
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {
        Box(modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(20.dp)
            .clickable(
                indication = null,
                interactionSource = interactionSource,
                onClick = {
                    if (skipText.value.length == 7) {
                        coroutineScope.launch {
                            delay(1500L)
                            skipText.value = "SkIP >>"
                        }
                    }
                    skipText.value = "You Cannot Skip"
                }
            )) {
                Text(
                    text = skipText.value,
                    style = TextStyle(
                        fontFamily = RyukExtra,
                        color = Color.Red,
                        fontSize = 20.sp
                    ), /*modifier = Modifier.clickable { onClick = {} }*/
                )
            }
        AnimatedTextDirect(fullText = rules[ruleIndex.value], nextIndication, ruleIndex)

        Box(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomStart)
            .padding(20.dp)
            .padding(bottom = 50.dp)
            .clickable(
                indication = null,
                interactionSource = interactionSource,
                onClick = {
                    if (nextIndication.value != "" && ruleIndex.value < 4) {
                        ruleIndex.value += 1
                        nextIndication.value = ""
                        indicationAlpha.value = 0f
                    } else if (ruleIndex.value == 4) {
                        termsAgreed.value = true
                        CoroutineScope(Dispatchers.Default).launch {
                            context.dataStore.edit { it[termsAgreedKey] = true }
                        }
                    }
                }
            )
        ) {
            LaunchedEffect(nextIndication.value) {
                if(nextIndication.value.isNotEmpty()) {
                    indicationAlpha.value = 0f
                    while (indicationAlpha.value < 1f) {
                        indicationAlpha.value += 0.1f
                        delay(50L)
                    }
                }
            }
            Text(
                text = nextIndication.value,
                style = TextStyle(
                    fontFamily = RyukExtra,
                    color = Color(0xFFFADA65),
                    fontSize = 20.sp,
                ), modifier = Modifier
                    .align(Alignment.Center)
                    .alpha(indicationAlpha.value)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Composable
fun Display3DModel() {
    Box(modifier = Modifier.fillMaxSize()) {
        val context = LocalContext.current
        val promises = remember { mutableStateOf<List<String>>(emptyList()) }
        LaunchedEffect(Unit) {
            promises.value = getStringListFromJson(context).toMutableList()
        }

        val engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine)

        val reset = remember { mutableStateOf(false) }
        val isBookClicked = remember { mutableStateOf(false) }
        val isBookCloseClicked = remember { mutableStateOf(false) }
        val isBookOpened = remember { mutableStateOf(false) }
        val isSettingsOpen = remember { mutableStateOf(false) }
        val isRulesSelected = remember { mutableStateOf(true) }
        val uriHandler = LocalUriHandler.current

        val currentPromise = remember { mutableStateOf(0) }
        val promiseChange = remember { mutableStateOf(-2) }

        val flipPageSound = FlipPageSound(context)

        var cameraNode = rememberCameraNode(engine).apply {
            position = Position(z = 2f, x = 0f, y = 3f)
        }

        val centerNode = rememberNode(engine)
            .addChildNode(cameraNode)

        val noteInput = remember { mutableStateOf("") }

        val interactionSource = remember { MutableInteractionSource() }

        val dnote: ModelNode = rememberNode {
            ModelNode(
                modelInstance = modelLoader.createModelInstance(
                    assetFileLocation = "dnote.glb"
                ),
                scaleToUnits = 1.5f,
            )
        }
        dnote.stopAnimation(0)
        cameraNode.isPositionEditable = true;
        cameraNode.isRotationEditable = true;
        cameraNode.isEditable = true;

        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            cameraNode = cameraNode,
            childNodes = listOf(centerNode, dnote),
            onFrame = {
                if(reset.value) {
                    cameraNode = cameraNode.apply {
                        position = Position(z = 0.01f, x = 0f, y = 3f)
                    }
                }
                cameraNode.lookAt(centerNode)
            }
        )
        dnote.onSingleTapConfirmed = {
            true.also {
                reset.value = true;
                if(!isBookClicked.value) {
                    promiseChange.value = -2
                    dnote.playAnimation(1, 1f, false)
                    CoroutineScope(Dispatchers.Default).launch {
                        delay(650L)
                        dnote.stopAnimation(1)
                        isBookOpened.value = true
                        isBookCloseClicked.value = false
                    }
                    isBookClicked.value = true
                }
            }
        }
        //Text(text = reset.value.toString(), modifier = Modifier.align(Alignment.TopStart))
        if(isBookOpened.value && promiseChange.value == 0 && !isBookCloseClicked.value) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(30.dp)
                    .clickable(
                        indication = null,
                        interactionSource = interactionSource,
                        onClick = {
                            if (isBookOpened.value && promiseChange.value == 0) {
                                dnote.playAnimation(3, 1f, false)
                                dnote.playAnimation(0, 1f, false)
                                CoroutineScope(Dispatchers.Default).launch {
                                    delay(300L)
                                    dnote.stopAnimation(3)
                                    delay(362L)
                                    dnote.stopAnimation(0)
                                    isBookOpened.value = false
                                    isBookClicked.value = false
                                    reset.value = false;
                                }
                            }
                        }
                    ), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tap here to close your death notes",
                    color = Color.Yellow,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(30.dp)
                )
            }
        }
        Icon(painter = painterResource(id = R.drawable.settings), contentDescription = "Settings", modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(15.dp)
            .clickable { isSettingsOpen.value = true })

        val promiseAlpha = remember { mutableStateOf(1f) }

        if(isBookClicked.value && !isBookCloseClicked.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0f))
            ) {
                LaunchedEffect(promiseChange.value) {
                    if(promiseChange.value == -2) {
                        promiseAlpha.value = 0f
                        while (promiseAlpha.value < 1f) {
                            promiseAlpha.value += 0.1f
                            delay(120L)
                        }
                        promiseChange.value = 0
                    } else if (promiseChange.value != 0) {
                        promiseAlpha.value = 1f
                        while (promiseAlpha.value > 0f) {
                            promiseAlpha.value -= 0.1f
                            delay(105L)
                        }
                        currentPromise.value += promiseChange.value
                        while (promiseAlpha.value < 1f) {
                            promiseAlpha.value += 0.1f
                            delay(40L)
                        }
                        promiseChange.value = 0
                    }
                }

                TextField(
                    value = if (currentPromise.value < promises.value.size) promises.value[currentPromise.value] else noteInput.value,
                    onValueChange = { updatedValue -> noteInput.value = updatedValue },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                        .alpha(promiseAlpha.value),
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontFamily = RyukExtra,
                        textAlign = TextAlign.Center,
                        fontSize = 22.sp
                    ),
                    enabled = currentPromise.value == promises.value.size,
                    placeholder = {
                        Text(
                            text = "Tap here to start writing and press enter to save the promise",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    singleLine = false,
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        containerColor = Color.Transparent,
                        cursorColor = Color.Black,
                        selectionColors = TextSelectionColors(
                            handleColor = Color.Black,
                            backgroundColor = Color.LightGray
                        ),
                        disabledTextColor = Color.Black
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (noteInput.value.isNotEmpty()) {
                                val newList = promises.value.toMutableList()
                                newList.add(noteInput.value)
                                promises.value = newList
                                CoroutineScope(Dispatchers.IO).launch {
                                    saveStringListAsJson(context, promises.value)
                                }
                                noteInput.value = ""
                            }
                        }
                    )
                )
            }
        }

        Column(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter), horizontalAlignment = Alignment.CenterHorizontally) {
            if(!isBookOpened.value) {
                Text(text = "Tap on it to open or hover to move around and inspect your death notes", textAlign = TextAlign.Center, fontSize = 18.sp, modifier = Modifier
                    .padding(50.dp)
                    .alpha(if (isBookClicked.value) 0f else 1f))
            } else if(!isBookCloseClicked.value) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier) {
                    Text(text = "<", color = Color.White, fontSize = 30.sp, modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = interactionSource,
                            onClick = {
                                if (currentPromise.value > 0 && promiseChange.value == 0) {
                                    dnote.playAnimation(2, 1f, false)
                                    GlobalScope.launch {
                                        delay(1210L)
                                        dnote.stopAnimation(2)
                                    }
                                    promiseChange.value = -1
                                    flipPageSound.play()
                                }
                                noteInput.value = ""
                            })
                        .padding(30.dp)
                        .alpha(if (currentPromise.value > 0 && promiseChange.value == 0) 1f else 0.3f))
                    Text(
                        text = "page ${currentPromise.value + 1}/∞",
                        color = Color.White,
                        fontSize = 20.sp
                    )
                    Text(text = ">", color = Color.White, fontSize = 30.sp, modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = interactionSource,
                            onClick = {
                                if (currentPromise.value < promises.value.size && promiseChange.value == 0) {
                                    dnote.playAnimation(3, 1f, false)
                                    GlobalScope.launch {
                                        delay(1100L)
                                        dnote.stopAnimation(3)
                                    }
                                    promiseChange.value = 1
                                    flipPageSound.play()
                                }
                            })
                        .padding(30.dp)
                        .alpha(if (currentPromise.value < promises.value.size && promiseChange.value == 0) 1f else 0.3f))
                }
            }
            bannerAd("ca-app-pub-2813592783630195/2460008389")
        }

        if(isSettingsOpen.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(
                        indication = null,
                        interactionSource = interactionSource,
                        onClick = { isSettingsOpen.value = false }
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                        .background(Color.Black)
                        .border(
                            width = 1.dp,
                            color = Color.Gray.copy(0.3f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(10.dp)
                        .border(width = 1.dp, color = Color(0xFFBBBBBB))
                        .padding(3.dp)
                        .align(Alignment.Center)
                        .clickable(enabled = false, onClick = {})
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.corndesign),
                        contentDescription = "d1",
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .size(20.dp)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.corndesign),
                        contentDescription = "d2",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                            .rotate(90f)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.corndesign),
                        contentDescription = "d3",
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .size(20.dp)
                            .rotate(270f)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.corndesign),
                        contentDescription = "d4",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(20.dp)
                            .rotate(180f)
                    )
                    Column {
                        Row(modifier = Modifier.padding(top = 20.dp)) {
                            Column(modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    indication = null,
                                    interactionSource = interactionSource,
                                    onClick = { isRulesSelected.value = true }), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Rules")
                                Icon(painterResource(id = R.drawable.headingborder), contentDescription = null, modifier = Modifier
                                    .height(10.dp)
                                    .width(24.dp)
                                    .alpha(if (isRulesSelected.value) 1f else 0f))
                            }
                            Column(modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    indication = null,
                                    interactionSource = interactionSource,
                                    onClick = { isRulesSelected.value = false }), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Credits")
                                Icon(painterResource(id = R.drawable.headingborder), contentDescription = null, modifier = Modifier
                                    .height(10.dp)
                                    .width(24.dp)
                                    .alpha(if (isRulesSelected.value) 0f else 1f))
                            }
                        }
                        Column(modifier = Modifier
                            .padding(vertical = 15.dp, horizontal = 20.dp)
                            .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) { //Rules column
                            if(isRulesSelected.value) {
                                rules.forEachIndexed { ind, value ->
                                    Text(text = "${ind + 1}. $value", modifier = Modifier.padding(5.dp))
                                }
                            } else {
                                Text(text = "Developer:", fontSize = 24.sp, color = Color.Yellow, modifier = Modifier.padding(top = 15.dp, bottom = 5.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                                    uriHandler.openUri("https://vinaykpro.github.io/portfolio/")
                                }) {
                                    Text(text = "Vinaykpro", fontSize = 18.sp)
                                    Icon(painter = painterResource(id = R.drawable.openicon), contentDescription = "open", modifier = Modifier.size(20.dp))
                                }

                                Text(text = "3d model inspiration", fontSize = 24.sp, color = Color.Yellow, modifier = Modifier.padding(top = 35.dp, bottom = 5.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                                    uriHandler.openUri("https://sketchfab.com/3d-models/death-note-38e9f0d0c6944557b6ecf2003f5aa4bb")
                                }) {
                                    Text(text = "CG.oum (sketchfab)", fontSize = 18.sp)
                                    Icon(painter = painterResource(id = R.drawable.openicon), contentDescription = "open", modifier = Modifier.size(20.dp))
                                }

                                Text(text = "Font (Ryuk Extra)", fontSize = 24.sp, color = Color.Yellow, modifier = Modifier.padding(top = 35.dp, bottom = 5.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                                    .padding(bottom = 25.dp)
                                    .clickable {
                                        uriHandler.openUri("https://www.onlinewebfonts.com/download/c5d9ca934221f82dc295c25078e361ae")
                                    }) {
                                    Text(text = "onlinewebfonts.com", fontSize = 18.sp)
                                    Icon(painter = painterResource(id = R.drawable.openicon), contentDescription = "open", modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedTextDirect(fullText: String, nextIndication: MutableState<String>, ruleIndex : MutableState<Int>, delayMillis: Long = 40L) {
    val stringBuilder = remember { StringBuilder() }
    val text = remember { mutableStateOf("") }
    LaunchedEffect(fullText) {
        stringBuilder.clear()
        for (char in fullText) {
            stringBuilder.append(char)
            text.value = stringBuilder.toString()
            if(char == ' ') delay(delayMillis+70L)
            else delay(delayMillis)
        }

        nextIndication.value = if(ruleIndex.value < 4) "TAP HERE TO CONTINUE" else "Proceed (At your own risk)"
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center ) {
        Text(text = text.value, textAlign = TextAlign.Center, style = TextStyle(fontFamily = RyukExtra, color = Color.White, fontSize = 30.sp), modifier =  Modifier.padding(30.dp))
    }
}

suspend fun saveStringListAsJson(context: Context, stringList: List<String>) {
    val json = Json.encodeToString(stringList)
    context.dataStore.edit { preferences ->
        preferences[stringKey] = json
    }
}

suspend fun getStringListFromJson(context: Context): List<String> {
    val preferences = context.dataStore.data.first()
    val json = preferences[stringKey] ?: return emptyList()
    return Json.decodeFromString(json)
}

class FlipPageSound(context: Context) {
    private val soundPool = SoundPool.Builder().setMaxStreams(5).build()
    private val soundId: Int = soundPool.load(context, R.raw.turnpage, 1)
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false

    private val playRunnable = Runnable {
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        isPlaying = false
    }

    fun play() {
        if (isPlaying) return
        isPlaying = true
        handler.postDelayed(playRunnable, 450L)
    }

    fun release() {
        handler.removeCallbacksAndMessages(null)
        soundPool.release()
    }
}

@Composable
fun bannerAd(adId : String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = {context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = adId
                    loadAd(AdRequest.Builder().build())
                }
            })
    }
}