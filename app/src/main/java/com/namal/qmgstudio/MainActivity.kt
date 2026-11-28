package com.namal.qmgstudio

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.foundation.layout.offset
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.runtime.SideEffect
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxHeight
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/* ================= palette ================= */

/** One complete look. The whole app reads its colours, fonts and shapes from the current Pal. */
class Pal(
    val id: String, val name: String, val dark: Boolean, val glass: Boolean,
    val bgTop: Color, val bgMid: Color, val bgBot: Color,
    val text: Color, val onAcc: Color, val card: Color, val stroke: Color, val stage: Color, val nav: Color,
    val c1: Color, val c2: Color, val c3: Color, val c4: Color, val c5: Color, val c6: Color,
    val orbs: List<Color>, val ocean: Boolean, val flatNav: Boolean, val tabs: Boolean,
    val body: FontFamily, val head: FontFamily
)

object Th {
    private val fYoungSerif = FontFamily(Font(R.font.young_serif))
    private val fInstrumentSans = FontFamily(Font(R.font.instrument_sans))
    private val fBricolage = FontFamily(Font(R.font.bricolage_grotesque))
    private val fJakarta = FontFamily(Font(R.font.plus_jakarta_sans))
    private val fGloock = FontFamily(Font(R.font.gloock))
    private val fFigtree = FontFamily(Font(R.font.figtree))
    private val fUnbounded = FontFamily(Font(R.font.unbounded))
    private val fOutfit = FontFamily(Font(R.font.outfit))
    private val fPlayfair = FontFamily(Font(R.font.playfair_display))
    private val fSora = FontFamily(Font(R.font.sora))

    private val ink = Color(0xFF151515)
    private val paperText = Color(0xFF1B1612)
    private val cream = Color(0xFFF3EFE6)

    /** Five Paper looks. All are paper-first: warm neutrals, one hot accent. */
    val all: List<Pal> = listOf(
        Pal(
            "paper", "Paper", dark = false, glass = false,
            bgTop = Color(0xFFF6F3EE), bgMid = Color(0xFFF6F3EE), bgBot = Color(0xFFF6F3EE),
            text = ink, onAcc = ink, card = Color(0xFFFBF9F4), stroke = ink,
            stage = Color(0xFFFBF9F4), nav = Color(0xFFF6F3EE),
            c1 = Color(0xFFFF5A1F), c2 = Color(0xFFFF7A3D), c3 = Color(0xFFFF9A66),
            c4 = Color(0xFF1A9D55), c5 = Color(0xFFFF8A57), c6 = Color(0xFF8A4A1C),
            orbs = emptyList(), ocean = false, flatNav = true, tabs = false,
            body = fInstrumentSans, head = fYoungSerif
        ),
        Pal(
            "paperglass", "Paper glass", dark = false, glass = true,
            bgTop = Color(0xFFF6EFE4), bgMid = Color(0xFFF2DCC7), bgBot = Color(0xFFE9D6C4),
            text = paperText, onAcc = paperText, card = Color(0x38FFFFFF), stroke = Color(0xE6FFFFFF),
            stage = Color(0x80FFFFFF), nav = Color(0x66FFFFFF),
            c1 = Color(0xFFFF5A1F), c2 = Color(0xFFFF7A3D), c3 = Color(0xFFFFA070),
            c4 = Color(0xFF3F7D58), c5 = Color(0xFFFFC9A0), c6 = Color(0xFF8A4A1C),
            orbs = listOf(Color(0xCCFF8A4C), Color(0xCCFFC9A0), Color(0xCC8FC4A0)), ocean = false, flatNav = false, tabs = true,
            body = fJakarta, head = fBricolage
        ),
        Pal(
            "paperdark", "Paper dark", dark = true, glass = false,
            bgTop = Color(0xFF121110), bgMid = Color(0xFF121110), bgBot = Color(0xFF121110),
            text = cream, onAcc = Color(0xFF121110), card = Color(0xFF1C1A18), stroke = Color(0xFF2E2A27),
            stage = Color(0xFF1C1A18), nav = Color(0xFF121110),
            c1 = Color(0xFFFF6A2B), c2 = Color(0xFFFF8A57), c3 = Color(0xFFFFB08A),
            c4 = Color(0xFF34D27A), c5 = Color(0xFFFFB08A), c6 = Color(0xFFA8A198),
            orbs = emptyList(), ocean = false, flatNav = true, tabs = true,
            body = fFigtree, head = fGloock
        ),
        Pal(
            "inkglass", "Paper ink glass", dark = true, glass = true,
            bgTop = Color(0xFF0E0D0C), bgMid = Color(0xFF14110F), bgBot = Color(0xFF0E0D0C),
            text = cream, onAcc = Color(0xFF121110), card = Color(0x20FFFFFF), stroke = Color(0xAAFFFFFF),
            stage = Color(0x66121110), nav = Color(0x66181614),
            c1 = Color(0xFFFF6A2B), c2 = Color(0xFFFF8350), c3 = Color(0xFF9D7DFF),
            c4 = Color(0xFF16C7A0), c5 = Color(0xFFFFB08A), c6 = Color(0xFFC9B8FF),
            orbs = listOf(Color(0xCCFF6A2B), Color(0xCC7A4DFF), Color(0xCC16C7A0)), ocean = false, flatNav = false, tabs = true,
            body = fOutfit, head = fUnbounded
        ),
        Pal(
            "sakuraglass", "Paper sakura glass", dark = false, glass = true,
            bgTop = Color(0xFFFBEFF0), bgMid = Color(0xFFF7DCE3), bgBot = Color(0xFFF1D3DD),
            text = Color(0xFF2A1620), onAcc = Color(0xFFFFFFFF), card = Color(0x3AFFFFFF), stroke = Color(0xE6FFFFFF),
            stage = Color(0x80FFFFFF), nav = Color(0x70FFFFFF),
            c1 = Color(0xFFE5486F), c2 = Color(0xFFF0709A), c3 = Color(0xFFF59AB8),
            c4 = Color(0xFF3F8F78), c5 = Color(0xFFFFB3C8), c6 = Color(0xFF8A3A55),
            orbs = listOf(Color(0xCCF0709A), Color(0xCCFFC6D6), Color(0xCCB9A6F0)), ocean = false, flatNav = false, tabs = true,
            body = fSora, head = fPlayfair
        )
    )

    var id by mutableStateOf("paperglass")
    var tabsOverride by mutableIntStateOf(-1)      // -1 = theme default, 0 = all in one, 1 = tabs

    val cur: Pal get() = all.firstOrNull { it.id == id } ?: all[1]
    val tabs: Boolean get() = false   // the Edit card is always one list (the tabs layout is gone)

    fun load(ctx: Context) {
        val sp = ctx.getSharedPreferences("qmg_ui", Context.MODE_PRIVATE)
        val saved = sp.getString("theme", "paperglass") ?: "paperglass"
        id = if (all.any { it.id == saved }) saved else "paperglass"   // the old Ocean look is gone
        tabsOverride = sp.getInt("tabs", -1)
    }

    fun save(ctx: Context) {
        ctx.getSharedPreferences("qmg_ui", Context.MODE_PRIVATE).edit().putString("theme", id).putInt("tabs", tabsOverride).apply()
    }
}

// the old colour names now follow the chosen theme
private val Ink: Color get() = Th.cur.bgTop
private val Cyan: Color get() = Th.cur.c1
private val Violet: Color get() = Th.cur.c2
private val Pink: Color get() = Th.cur.c3
private val Mint: Color get() = Th.cur.c4
private val Sakura: Color get() = Th.cur.c5
private val Peach: Color get() = Th.cur.c6
private val Danger = Color(0xFFFF6B6B)
private val Tx: Color get() = Th.cur.text

private fun tint(a: Float): Color = Th.cur.text.copy(alpha = a)

private fun cardBorder(): Brush {
    val p = Th.cur
    val c = p.stroke
    return if (p.glass) Brush.linearGradient(listOf(c, c.copy(alpha = c.alpha * 0.08f), c.copy(alpha = c.alpha * 0.4f)))
    else SolidColor(c)
}

private fun scheme() = if (Th.cur.dark) darkColorScheme(
    primary = Violet,
    onPrimary = Th.cur.onAcc,
    secondary = Cyan,
    background = Th.cur.bgTop,
    surface = Th.cur.stage,
    onSurface = Th.cur.text,
    onBackground = Th.cur.text
) else lightColorScheme(
    primary = Violet,
    onPrimary = Th.cur.onAcc,
    secondary = Cyan,
    background = Th.cur.bgTop,
    surface = Th.cur.stage,
    onSurface = Th.cur.text,
    onBackground = Th.cur.text
)

class MainActivity : ComponentActivity() {
    companion object {
        /** true while the app is on screen (used so "animation ready" notifications only show when you are away) */
        @Volatile var visible = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Diag.silentEnabled(this)) Diag.schedule(this)   // keeps the quiet background check scheduled (no-op if already queued)
        Th.load(this)
        setContent {
            MaterialTheme(colorScheme = scheme()) {
                CompositionLocalProvider(
                    LocalTextStyle provides LocalTextStyle.current.merge(TextStyle(fontFamily = Th.cur.body, color = Th.cur.text))
                ) { App() }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        visible = true
    }

    override fun onStop() {
        visible = false
        super.onStop()
    }
}

/* ================= liquid ocean backdrop ================= */

@Composable
private fun NightBackdrop(animate: Boolean) {
    val inf = rememberInfiniteTransition(label = "bg")
    val t = inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(40000, easing = LinearEasing), RepeatMode.Restart),
        label = "t"
    )
    val bubbles = remember {
        val r = Random(11)
        // x, phase, size, speed, wobble
        List(26) { floatArrayOf(r.nextFloat(), r.nextFloat(), 0.6f + r.nextFloat() * 1.8f, (1 + r.nextInt(3)).toFloat(), r.nextFloat()) }
    }
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val tt = if (animate && Th.cur.ocean) t.value else 0.3f
        val twoPi = (2.0 * PI).toFloat()
        val ang = tt * twoPi

        drawRect(
            Brush.verticalGradient(
                0f to Th.cur.bgTop,
                0.5f to Th.cur.bgMid,
                1f to Th.cur.bgBot,
                startY = 0f,
                endY = h
            )
        )
        // slow drifting liquid orbs
        fun orb(cx: Float, cy: Float, r: Float, c: Color) {
            drawCircle(Brush.radialGradient(listOf(c, Color.Transparent), center = Offset(cx, cy), radius = r), radius = r, center = Offset(cx, cy))
        }
        if (!Th.cur.ocean) {
            // calm themes: a few slow colour blobs (or nothing) and no bubbles
            for ((k, oc) in Th.cur.orbs.withIndex()) {
                orb(w * (0.15f + 0.35f * k + 0.04f * sin(ang + k)), h * (0.12f + 0.32f * k + 0.03f * cos(ang)), w * 0.75f, oc)
            }
            if (Th.cur.glass) {
                // solid shapes under the glass: sharp outside the cards, soft and frosted behind them
                val xs = floatArrayOf(0.84f, 0.12f, 0.70f, 0.30f)
                val ys = floatArrayOf(0.15f, 0.40f, 0.72f, 0.92f)
                val rs = floatArrayOf(0.17f, 0.13f, 0.20f, 0.11f)
                val cs = Th.cur.orbs
                for (k in xs.indices) {
                    val c0 = cs[k % cs.size]
                    val cx = w * xs[k]; val cy = h * ys[k]; val rr = w * rs[k] * 1.25f
                    drawPath(
                        blobPath(cx, cy, rr, k * 1.9f),
                        Brush.linearGradient(listOf(c0, c0.copy(alpha = c0.alpha * 0.30f)), start = Offset(cx - rr, cy - rr), end = Offset(cx + rr, cy + rr))
                    )
                    // a small bright droplet next to each blob
                    drawPath(blobPath(cx + rr * 0.9f, cy - rr * 0.8f, rr * 0.26f, k * 0.7f), Th.cur.c5.copy(alpha = 0.9f))
                }
                drawCircle(Th.cur.c1.copy(alpha = 0.35f), radius = w * 0.36f, center = Offset(w * 0.28f, h * 0.24f), style = Stroke(w * 0.018f))
                drawCircle(Th.cur.c3.copy(alpha = 0.30f), radius = w * 0.30f, center = Offset(w * 0.9f, h * 0.58f), style = Stroke(w * 0.014f))
            }
            return@Canvas
        }
        orb(w * (0.20f + 0.14f * sin(ang)), h * (0.20f + 0.06f * cos(ang)), w * 0.85f, Color(0x663D7BFF))
        orb(w * (0.85f - 0.12f * cos(ang)), h * (0.45f + 0.07f * sin(ang)), w * 0.80f, Color(0x4437D5D6))
        orb(w * (0.40f + 0.18f * cos(ang * 1f + 1f)), h * (0.78f - 0.06f * sin(ang)), w * 0.90f, Color(0x553355FF))
        orb(w * (0.75f + 0.08f * sin(ang + 2f)), h * (0.10f + 0.04f * cos(ang)), w * 0.55f, Color(0x33A5E8FF))

        // two liquid waves at the bottom
        fun wave(base: Float, amp: Float, speed: Float, phase: Float, c: Color) {
            val path = Path()
            path.moveTo(0f, h)
            val steps = 28
            for (i in 0..steps) {
                val x = w * i / steps
                val y = h * base + amp * h * sin(twoPi * (i.toFloat() / steps * 1.3f + tt * speed + phase))
                path.lineTo(x, y)
            }
            path.lineTo(w, h)
            path.close()
            drawPath(path, c)
        }
        wave(0.90f, 0.012f, 2f, 0.0f, Color(0x331E5FFF))
        wave(0.94f, 0.010f, -3f, 0.4f, Color(0x4412A4D8))

        // rising bubbles
        for (b in bubbles) {
            val rise = 1f - ((tt * b[3] + b[1]) % 1f)
            val y = rise * (h + 40f) - 20f
            val x = b[0] * w + sin(twoPi * (tt * b[3] * 2f + b[4])) * 14f
            val rr = b[2] * density * 3.2f
            drawCircle(Color(0x14BFE6FF), radius = rr, center = Offset(x, y))
            drawCircle(Color(0x55DDF3FF), radius = rr, center = Offset(x, y), style = Stroke(width = 1f * density))
            drawCircle(Color(0x99FFFFFF), radius = rr * 0.25f, center = Offset(x - rr * 0.35f, y - rr * 0.35f))
        }
    }
}

@Composable
private fun LogoMark(sizeDp: Int, fast: Boolean) {
    val tr = rememberInfiniteTransition(label = "logo")
    val deg by tr.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(if (fast) 1600 else 9000, easing = LinearEasing), RepeatMode.Restart),
        label = "deg"
    )
    Canvas(Modifier.size(sizeDp.dp)) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension * 0.40f
        rotate(deg, c) {
            drawCircle(
                brush = Brush.sweepGradient(listOf(Cyan, Violet, Pink, Cyan), c),
                radius = r,
                center = c,
                style = Stroke(width = size.minDimension * 0.13f)
            )
        }
        val p = Path()
        p.moveTo(c.x - r * 0.30f, c.y - r * 0.45f)
        p.lineTo(c.x + r * 0.52f, c.y)
        p.lineTo(c.x - r * 0.30f, c.y + r * 0.45f)
        p.close()
        drawPath(p, Color.White)
    }
}

private fun blobPath(cx: Float, cy: Float, r: Float, seed: Float): Path {
    val p = Path()
    val n = 56
    for (i in 0..n) {
        val a = (2.0 * PI).toFloat() * i / n
        val rr = r * (1f + 0.16f * sin(3f * a + seed) + 0.09f * sin(5f * a + seed * 1.7f))
        val x = cx + rr * cos(a)
        val y = cy + rr * sin(a)
        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
    }
    p.close()
    return p
}

/* ================= real glass: frosted backdrop, rim light, jelly nav, icon set ================= */

/** A blurred copy of the app background. Glass surfaces draw their own slice of it, so they look frosted. */
private var glassLayerRef: GraphicsLayer? by mutableStateOf(null)

@Composable
private fun Modifier.glassBack(): Modifier {
    val layer = glassLayerRef
    if (layer == null || !Th.cur.glass) return this
    var off by remember { mutableStateOf(Offset.Zero) }
    return this
        .onGloballyPositioned { off = it.positionInWindow() }
        .drawBehind { translate(-off.x, -off.y) { drawLayer(layer) } }
}

/** Light catching the edge of the glass: bright top-left rim, soft inner glow, a thin shade along the bottom. */
private fun Modifier.glassRim(radiusDp: Float): Modifier = this.drawBehind {
    val rad = radiusDp.dp.toPx()
    val r = androidx.compose.ui.geometry.CornerRadius(rad, rad)
    val k = if (Th.cur.dark) 0.55f else 0.95f
    drawRoundRect(
        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.34f * k), Color.Transparent, Color.White.copy(alpha = 0.10f * k)), start = Offset.Zero, end = Offset(size.width, size.height)),
        cornerRadius = r
    )
    drawRoundRect(
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.85f * k), Color.White.copy(alpha = 0.05f), Color.Transparent), endY = size.height * 0.5f),
        topLeft = Offset(1.5f, 1.5f), size = Size(size.width - 3f, size.height - 3f), cornerRadius = r, style = Stroke(2.5f)
    )
    // water-drop highlight (top left) and a coloured caustic glow along the bottom right
    drawOval(
        Brush.radialGradient(listOf(Color.White.copy(alpha = 0.75f * k), Color.Transparent), center = Offset(rad * 1.1f, rad * 0.55f), radius = rad * 0.9f),
        topLeft = Offset(rad * 0.45f, rad * 0.2f), size = Size(rad * 1.3f, rad * 0.7f)
    )
    drawOval(
        Brush.radialGradient(listOf(Th.cur.c1.copy(alpha = 0.30f), Color.Transparent), center = Offset(size.width * 0.82f, size.height), radius = size.width * 0.45f),
        topLeft = Offset(size.width * 0.35f, size.height * 0.55f), size = Size(size.width * 0.65f, size.height * 0.45f)
    )
    drawRoundRect(
        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = if (Th.cur.dark) 0.35f else 0.10f)), startY = size.height * 0.5f, endY = size.height),
        topLeft = Offset(1f, 1f), size = Size(size.width - 2f, size.height - 2f), cornerRadius = r, style = Stroke(2f)
    )
}

private val ICONS: Map<String, String> = mapOf(
    "create" to "M12 3l1.9 5.1L19 10l-5.1 1.9L12 17l-1.9-5.1L5 10l5.1-1.9z M18.5 15.5l.7 1.8 1.8.7-1.8.7-.7 1.8-.7-1.8-1.8-.7 1.8-.7z",
    "play" to "M8 5v14l11-7z",
    "pause" to "M8 5v14M16 5v14",
    "save" to "M12 4v11M7 11l5 5 5-5M5 20h14",
    "tools" to "M14.5 6.5a4 4 0 0 0 4.9 4.9L21 13l-8 8-3-3 8-8zM3 21l4-4",
    "gallery" to "M6 4h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z M15 4h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2h-3a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z M6 13h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2z M15 13h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2h-3a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2z",
    "bolt" to "M13 2L4 14h7l-1 8 9-12h-7z",
    "film" to "M4 5h16v14H4zM8 5v14M16 5v14M4 10h4M4 14h4M16 10h4M16 14h4",
    "moon" to "M20 14A8 8 0 1 1 10 4a6.5 6.5 0 0 0 10 10z",
    "crop" to "M6 3v15h15M3 6h15v15",
    "balance" to "M12 4v16M6 20h12M5 8h14M5 8l-3 7h6zM19 8l-3 7h6z",
    "box" to "M3 7l9-4 9 4v10l-9 4-9-4zM3 7l9 4 9-4M12 11v10",
    "search" to "M11 4a7 7 0 1 0 0 14 7 7 0 0 0 0-14zM16 16l5 5",
    "refresh" to "M20 11a8 8 0 1 0-2.3 5.7M20 4v7h-7",
    "shield" to "M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6z",
    "pulse" to "M3 12h4l2-5 4 10 2-5h6",
    "folder" to "M3 6h6l2 2h10v11H3z",
    "star" to "M12 3l2.7 5.6 6.1.9-4.4 4.3 1 6.1L12 17l-5.4 2.9 1-6.1L3.2 9.5l6.1-.9z",
    "bug" to "M9 9h6v7a3 3 0 0 1-6 0zM9 6a3 3 0 0 1 6 0M4 12h5M15 12h5M5 19l4-3M19 19l-4-3M5 6l4 3M19 6l-4 3",
    "share" to "M12 16V4M7 8l5-4 5 4M5 20h14",
    "lock" to "M7 11V8a5 5 0 0 1 10 0v3M5 11h14v10H5zM12 15v2",
    "warn" to "M12 3l10 18H2zM12 10v5M12 18v.5",
    "check" to "M5 12.5l4.5 4.5L19 7",
    "x" to "M6 6l12 12M18 6L6 18",
    "clip" to "M9 4h6v3H9zM6 5.5h3M15 5.5h3V21H6V5.5",
    "phone" to "M8 3h8a1 1 0 0 1 1 1v16a1 1 0 0 1-1 1H8a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1zM11 18h2",
    "smile" to "M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18zM8 14a5 5 0 0 0 8 0M9 9.5v.5M15 9.5v.5",
    "confused" to "M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18zM8.5 15.5c2-1.3 5-1.3 7 0M9 9.5v.5M15 9.5v.5"
)

private val LEAD: List<Pair<String, String>> = listOf(
    "\uD83C\uDFAC" to "film", "\u25B6\uFE0F" to "play", "\u25B6" to "play", "\uD83D\uDCBE" to "save", "\uD83E\uDDF0" to "tools", "\uD83D\uDDBC\uFE0F" to "gallery",
    "\u26A1" to "bolt", "\uD83D\uDE80" to "bolt", "\uD83C\uDF19" to "moon", "\u2702\uFE0F" to "crop", "\u2728" to "create", "\u2696\uFE0F" to "balance",
    "\uD83D\uDCE6" to "box", "\uD83D\uDD0E" to "search", "\uD83D\uDD2C" to "search", "\uD83C\uDF9E\uFE0F" to "film", "\u23F8\uFE0F" to "pause", "\u23F8" to "pause",
    "\uD83D\uDD04" to "refresh", "\u21A9\uFE0F" to "refresh", "\uD83D\uDEDF" to "shield", "\uD83E\uDE7A" to "pulse", "\uD83D\uDCC2" to "folder", "\u2B50" to "star",
    "\u2B07\uFE0F" to "save", "\uD83D\uDC1E" to "bug", "\uD83D\uDCE4" to "share", "\uD83D\uDD10" to "lock", "\u26A0\uFE0F" to "warn", "\u2705" to "check",
    "\u274C" to "x", "\uD83D\uDE42" to "smile", "\uD83D\uDE15" to "confused", "\uD83D\uDCCB" to "clip", "\uD83D\uDCF1" to "phone"
).sortedByDescending { it.first.length }

/** Removes any emoji that is still in a text (the line icons replace them). Plain symbols such as the check mark stay. */
private fun stripEmoji(t: String): String {
    val sb = StringBuilder()
    var i = 0
    while (i < t.length) {
        val cp = t.codePointAt(i)
        i += Character.charCount(cp)
        val emoji = cp in 0x1F000..0x1FAFF || (cp in 0x2600..0x27BF && cp != 0x2713 && cp != 0x2714 && cp != 0x2717) ||
            cp in 0x2B00..0x2BFF || cp in 0x23E9..0x23FF || cp == 0xFE0F || cp == 0x200D
        if (!emoji) sb.appendCodePoint(cp)
    }
    return sb.toString().trim()
}

/** Splits a leading emoji off a label: ("refresh icon  Refresh") -> ("refresh", "Refresh"). */
private fun splitLead(text: String): Pair<String?, String> {
    for ((e, n) in LEAD) if (text.startsWith(e)) return n to text.substring(e.length).trimStart()
    return null to text
}

@Composable
private fun Ic(name: String, color: Color, size: Dp = 22.dp, stroke: Float = 1.8f) {
    val path = remember(name) { PathParser().parsePathString(ICONS[name] ?: ICONS.getValue("bolt")).toPath() }
    Canvas(Modifier.size(size)) {
        val k = this.size.minDimension / 24f
        scale(k, k, pivot = Offset.Zero) {
            drawPath(path, color, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

/** Text that turns a leading emoji into a drawn line icon. */
@Composable
private fun IcText(text: String, color: Color, size: Float, weight: FontWeight, modifier: Modifier = Modifier, lineH: Float = 0f) {
    val parts = remember(text) { splitLead(text).let { it.first to stripEmoji(it.second) } }
    val ic = parts.first
    val rest = parts.second
    if (ic == null) {
        if (lineH > 0f) Text(rest, color = color, fontSize = size.sp, fontWeight = weight, lineHeight = lineH.sp, modifier = modifier)
        else Text(rest, color = color, fontSize = size.sp, fontWeight = weight, modifier = modifier)
    } else {
        Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Ic(ic, color, (size + 6f).dp)
            if (lineH > 0f) Text(rest, color = color, fontSize = size.sp, fontWeight = weight, lineHeight = lineH.sp, modifier = Modifier.weight(1f, fill = false))
            else Text(rest, color = color, fontSize = size.sp, fontWeight = weight, modifier = Modifier.weight(1f, fill = false))
        }
    }
}

/* ================= building blocks ================= */

@Composable
private fun GlassCard(title: String, accent: Color = Cyan, badge: String? = null, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(26.dp)
    val glassK = if (Th.cur.glass) 1f else 0f
    var pressed by remember { mutableStateOf(false) }
    val pop by animateFloatAsState(if (pressed) 0.97f else 1f, spring(), label = "pop")
    Column(
        Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = pop; scaleY = pop }
            .clip(shape)
            .glassBack()
            .background(Th.cur.card)
            .then(if (Th.cur.glass) Modifier.glassRim(26f) else Modifier)
            .border(1.dp, cardBorder(), shape)
            .then(
                if (onClick != null) Modifier.pointerInput(Unit) {
                    detectTapGestures(onPress = { pressed = true; tryAwaitRelease(); pressed = false }, onTap = { onClick() })
                } else Modifier
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (badge != null) {
                Box(
                    Modifier.size(24.dp).clip(CircleShape).background(Brush.linearGradient(listOf(accent, Violet))),
                    contentAlignment = Alignment.Center
                ) { Text(badge, color = Th.cur.onAcc, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold) }
            } else {
                Box(Modifier.size(8.dp).clip(CircleShape).background(accent))
            }
            Spacer(Modifier.width(10.dp))
            Text(
                title.uppercase(),
                color = Tx.copy(alpha = 0.9f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
        content()
    }
}

@Composable
private fun GlowButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    val tr = rememberInfiniteTransition(label = "glow")
    val shift by tr.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(tween(3600, easing = LinearEasing), RepeatMode.Restart),
        label = "shift"
    )
    val shape = RoundedCornerShape(18.dp)
    val brush = if (enabled) {
        Brush.linearGradient(
            listOf(Cyan, Violet, Pink),
            start = Offset(shift, 0f),
            end = Offset(shift + 600f, 0f),
            tileMode = TileMode.Mirror
        )
    } else {
        Brush.linearGradient(listOf(Color(0xFF2A2D3A), Color(0xFF2A2D3A)))
    }
    val base = if (enabled) Modifier.shadow(14.dp, shape, ambientColor = Violet, spotColor = Cyan) else Modifier
    Box(
        base
            .fillMaxWidth()
            .height(54.dp)
            .clip(shape)
            .background(brush)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        IcText(text, if (enabled) Th.cur.onAcc else Color(0xFF7C8094), 16f, FontWeight.ExtraBold)
    }
}

@Composable
private fun GhostButton(text: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(shape)
            .background(tint(0.08f))
            .border(1.dp, Brush.horizontalGradient(listOf(Cyan.copy(alpha = 0.7f), Violet.copy(alpha = 0.7f))), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        IcText(text, Tx, 14f, FontWeight.SemiBold)
    }
}

@Composable
private fun MiniButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier
            .height(40.dp)
            .clip(shape)
            .background(tint(0.10f))
            .border(1.dp, Tx.copy(alpha = 0.25f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        IcText(text, Tx, 13f, FontWeight.SemiBold)
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    val fill = if (selected) Brush.horizontalGradient(listOf(Cyan.copy(alpha = 0.32f), Violet.copy(alpha = 0.55f)))
    else Brush.horizontalGradient(listOf(tint(0.08f), tint(0.08f)))
    IcText(
        label,
        if (selected) Tx else Tx.copy(alpha = 0.75f),
        13f,
        FontWeight.SemiBold,
        modifier = Modifier
            .clip(shape)
            .background(fill)
            .border(1.dp, if (selected) Cyan else Tx.copy(alpha = 0.2f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp)
    )
}

@Composable
private fun Pill(text: String, color: Color, onClick: (() -> Unit)? = null) {
    val shape = RoundedCornerShape(50)
    val tap = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Text(
        text,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        modifier = Modifier
            .clip(shape)
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.5f), shape)
            .then(tap)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    )
}

@Composable
private fun Label(text: String) {
    IcText(text, Tx, 13f, FontWeight.SemiBold)
}

/** Friendly loading animation: three dots that hop one after the other. */
@Composable
private fun BounceDots(color: Color = Cyan, dot: Dp = 10.dp) {
    val t = rememberInfiniteTransition(label = "dots")
    Row(horizontalArrangement = Arrangement.spacedBy(dot * 0.7f), verticalAlignment = Alignment.CenterVertically) {
        for (i in 0..2) {
            val v by t.animateFloat(
                0f, 1f,
                infiniteRepeatable(tween(480, easing = FastOutSlowInEasing), RepeatMode.Reverse, initialStartOffset = StartOffset(i * 150)),
                label = "dot" + i
            )
            Box(
                Modifier
                    .size(dot)
                    .graphicsLayer { translationY = -dot.toPx() * 0.9f * v; alpha = 0.4f + 0.6f * v }
                    .background(color, CircleShape)
            )
        }
    }
}

/** Pulse ring: two rings grow and fade one after the other. */
@Composable
private fun PulseRing(color: Color = Cyan, size: Dp = 44.dp) {
    val t = rememberInfiniteTransition(label = "pulse")
    val p by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1300, easing = LinearEasing)), label = "p")
    Canvas(Modifier.size(size)) {
        for (k in 0..1) {
            val q = (p + k * 0.5f) % 1f
            drawCircle(color.copy(alpha = 1f - q), radius = this.size.minDimension / 2f * (0.25f + 0.75f * q), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()))
        }
    }
}

/** Wave bars: five bars that rise and fall in a wave. */
@Composable
private fun WaveBars(color: Color = Cyan, h: Dp = 30.dp) {
    val t = rememberInfiniteTransition(label = "wave")
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(h)) {
        for (i in 0..4) {
            val v by t.animateFloat(0.25f, 1f, infiniteRepeatable(tween(520, easing = FastOutSlowInEasing), RepeatMode.Reverse, initialStartOffset = StartOffset(i * 110)), label = "bar" + i)
            Box(Modifier.width(5.dp).fillMaxHeight(v).clip(RoundedCornerShape(3.dp)).background(color))
        }
    }
}

/** Orbit, 5 versions: 0 three dots, 1 dot with a fading tail, 2 planet and moon, 3 two rings turning opposite ways, 4 four dots breathing in and out. */
@Composable
private fun OrbitDots(variant: Int, color: Color = Cyan, size: Dp = 44.dp) {
    val t = rememberInfiniteTransition(label = "orbit")
    val a by t.animateFloat(0f, 360f, infiniteRepeatable(tween(1200, easing = LinearEasing)), label = "a")
    val br by t.animateFloat(0.45f, 1f, infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "br")
    Canvas(Modifier.size(size)) {
        val full = this.size.minDimension / 2f
        val r = full - 5.dp.toPx()
        fun dot(deg: Float, rad: Float, dr: Float, al: Float) {
            val ang = Math.toRadians(deg.toDouble())
            drawCircle(color.copy(alpha = al.coerceIn(0f, 1f)), radius = dr, center = androidx.compose.ui.geometry.Offset(center.x + (rad * Math.cos(ang)).toFloat(), center.y + (rad * Math.sin(ang)).toFloat()))
        }
        when (variant % 5) {
            0 -> for (k in 0..2) dot(a + k * 120f, r, 4.5.dp.toPx() - k * 0.8.dp.toPx(), 1f - k * 0.3f)
            1 -> for (k in 0..5) dot(a - k * 16f, r, 5.dp.toPx() - k * 0.7.dp.toPx(), 1f - k * 0.17f)
            2 -> { drawCircle(color, radius = 6.dp.toPx(), center = center); dot(a, r, 3.5.dp.toPx(), 1f) }
            3 -> {
                drawCircle(color.copy(alpha = 0.2f), radius = r, style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
                drawCircle(color.copy(alpha = 0.2f), radius = r * 0.5f, style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
                dot(a, r, 4.dp.toPx(), 1f); dot(-a * 1.6f, r * 0.5f, 3.dp.toPx(), 0.8f)
            }
            else -> for (k in 0..3) dot(a / 2f + k * 90f, r * br, 4.dp.toPx(), 0.5f + 0.5f * br)
        }
    }
}

/** Arc spinner: a ring segment that grows and shrinks while it turns. */
@Composable
private fun ArcSpinner(color: Color = Cyan, size: Dp = 38.dp) {
    val t = rememberInfiniteTransition(label = "arc")
    val rot by t.animateFloat(0f, 360f, infiniteRepeatable(tween(1000, easing = LinearEasing)), label = "rot")
    val sweep by t.animateFloat(40f, 250f, infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "sweep")
    Canvas(Modifier.size(size)) {
        val sw = 4.dp.toPx()
        drawArc(
            color, rot, sweep, false,
            topLeft = androidx.compose.ui.geometry.Offset(sw / 2f, sw / 2f),
            size = androidx.compose.ui.geometry.Size(this.size.width - sw, this.size.height - sw),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = sw, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}

/** The loading animation of the app: two rings turning opposite ways. */
@Composable
private fun Loader(color: Color = Cyan, size: Dp = 44.dp) {
    OrbitDots(3, color, size)
}

@Composable
private fun Hint(text: String) {
    IcText(text, Tx.copy(alpha = 0.6f), 12f, FontWeight.Normal, lineH = 17f)
}

@Composable
private fun Toggle(title: String, hint: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Tx, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Hint(hint)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = Violet,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = tint(0.13f),
                uncheckedThumbColor = Tx.copy(alpha = 0.7f)
            )
        )
    }
}

@Composable
private fun sliderColors() = SliderDefaults.colors(
    thumbColor = Cyan,
    activeTrackColor = Violet,
    inactiveTrackColor = Tx.copy(alpha = 0.14f)
)

@Composable
private fun PhoneMock(aspect: Float, height: Dp, content: @Composable BoxScope.() -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        Modifier
            .height(height)
            .aspectRatio(aspect)
            .shadow(18.dp, shape, ambientColor = Violet, spotColor = Cyan)
            .clip(shape)
            .background(Color.Black)
            .border(3.dp, Brush.verticalGradient(listOf(Color(0xFF454A63), Color(0xFF15171F))), shape)
    ) {
        content()
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF0B0B10))
                .border(1.dp, Color(0xFF2B2E3B), CircleShape)
        )
    }
}

@Composable
private fun NavBar(tab: Int, onTab: (Int) -> Unit) {
    val items = listOf("create" to "Create", "play" to "Preview", "save" to "Save", "tools" to "Tools")
    val none = remember { MutableInteractionSource() }
    if (Th.cur.flatNav) {
        // flat tab bar: a line on top and an accent bar that slides (with a little bounce) to the chosen tab
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .background(Th.cur.nav)
                .drawBehind { drawLine(Th.cur.stroke.copy(alpha = 0.5f), Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx()) }
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            val iw = maxWidth / items.size
            val x by animateDpAsState(iw * tab, spring(0.55f, 420f), label = "navx")
            Box(Modifier.offset(x = x + iw * 0.2f).width(iw * 0.6f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Cyan))
            Row(Modifier.fillMaxWidth()) {
                for (i in items.indices) {
                    val sel = tab == i
                    val sc by animateFloatAsState(if (sel) 1.12f else 1f, spring(0.45f, 500f), label = "navsc")
                    Column(
                        Modifier.weight(1f).clickable(interactionSource = none, indication = null) { onTab(i) }.padding(top = 9.dp, bottom = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(Modifier.graphicsLayer { scaleX = sc; scaleY = sc }) { Ic(items[i].first, if (sel) Tx else Tx.copy(alpha = 0.55f), 22.dp) }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            items[i].second.uppercase(),
                            fontSize = 10.sp,
                            letterSpacing = 0.6.sp,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                            color = if (sel) Tx else Tx.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
        return
    }
    // glass bar: frosted pill with a jelly bubble. The edge the bubble moves towards runs ahead, so it stretches and then snaps back.
    val shape = RoundedCornerShape(32.dp)
    val pad = 6.dp
    val hold = remember { intArrayOf(tab, 1) }
    if (tab != hold[0]) { hold[1] = if (tab > hold[0]) 1 else 0; hold[0] = tab }
    val toRight = hold[1] == 1
    BoxWithConstraints(Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 8.dp)) {
        val iw = (maxWidth - pad * 2) / items.size
        val lead by animateFloatAsState(if (toRight) tab + 1f else tab.toFloat(), spring(0.5f, 620f), label = "lead")
        val trail by animateFloatAsState(if (toRight) tab.toFloat() else tab + 1f, spring(0.62f, 230f), label = "trail")
        val a = minOf(lead, trail)
        val wBub = (maxOf(lead, trail) - a).coerceAtLeast(0.7f)
        Box(
            Modifier
                .fillMaxWidth()
                .shadow(20.dp, shape, ambientColor = Cyan.copy(alpha = 0.4f), spotColor = Violet.copy(alpha = 0.5f))
                .clip(shape)
                .glassBack()
                .background(Th.cur.nav)
                .glassRim(32f)
                .border(1.dp, cardBorder(), shape)
        ) {
            Box(Modifier.padding(pad).fillMaxWidth().height(54.dp)) {
                Box(
                    Modifier
                        .offset(x = iw * a)
                        .width(iw * wBub)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(26.dp))
                        .background(Brush.linearGradient(listOf(Cyan.copy(alpha = 0.55f), Violet.copy(alpha = 0.35f))))
                        .glassRim(26f)
                        .border(1.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(26.dp))
                )
            }
            Row(Modifier.padding(pad).fillMaxWidth().height(54.dp)) {
                for (i in items.indices) {
                    val sel = tab == i
                    val sc by animateFloatAsState(if (sel) 1.14f else 1f, spring(0.4f, 520f), label = "navsc")
                    Column(
                        Modifier.weight(1f).fillMaxHeight().clickable(interactionSource = none, indication = null) { onTab(i) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(Modifier.graphicsLayer { scaleX = sc; scaleY = sc }) { Ic(items[i].first, if (sel) Th.cur.onAcc else Tx.copy(alpha = 0.7f), 22.dp) }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            items[i].second,
                            fontSize = 11.sp,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                            color = if (sel) Th.cur.onAcc else Tx.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.Toast(text: String, onDismiss: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        Modifier
            .align(Alignment.BottomCenter)
            .systemBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 84.dp)
            .fillMaxWidth()
            .shadow(14.dp, shape, ambientColor = Cyan, spotColor = Violet)
            .clip(shape)
            .background(Color(0xE60A1830))
            .background(Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x0AFFFFFF))))
            .border(1.dp, Brush.linearGradient(listOf(Color(0xAAFFFFFF), Color(0x22FFFFFF), Color(0x66FFFFFF))), shape)
            .clickable(onClick = onDismiss)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        IcText(text, Color.White, 14f, FontWeight.Normal)
    }
}

private fun sizeText(bytes: Long): String =
    if (bytes >= 1024L * 1024L) String.format("%.1f MB", bytes / 1048576.0) else (bytes / 1024).toString() + " KB"

private fun mmss(s: Float): String {
    val t = s.coerceAtLeast(0f)
    val m = (t / 60f).toInt()
    return m.toString() + ":" + String.format("%04.1f", t - m * 60f)
}

private fun fitName(f: Fit): String = when (f) {
    Fit.FILL -> "Fill"
    Fit.FIT -> "Fit"
    Fit.STRETCH -> "Stretch"
    Fit.GLOW -> "Full screen"
}

/* ---------- live preview drawing (reads the frame index only while drawing, so playing never recomposes the screen) ---------- */

@Composable
private fun LiveCanvas(images: List<ImageBitmap>, fit: Fit, crop: Float, fadeFrames: Int, totalFrames: Int, ed: Edits, idx: () -> Int) {
    val edges = remember(images) { arrayOfNulls<IntArray>(images.size) }
    // brightness / contrast, same maths as the encoder: out = k * c + (128 * (1 - k) + brightness)
    val cf = remember(ed.brightness, ed.contrast) {
        if (ed.brightness == 0 && ed.contrast == 100) null
        else {
            val k = ed.contrast / 100f
            val t = 128f * (1f - k) + ed.brightness
            ColorFilter.colorMatrix(
                ColorMatrix(
                    floatArrayOf(
                        k, 0f, 0f, 0f, t,
                        0f, k, 0f, 0f, t,
                        0f, 0f, k, 0f, t,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
        }
    }
    Box(Modifier.fillMaxSize()) {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(Color(ed.bg))
        if (images.isEmpty()) return@Canvas
        val i = idx().coerceIn(0, images.size - 1)
        val img = images[i]
        val dw = size.width
        val dh = size.height
        // the user's crop box: only this part of the picture is fitted to the screen
        val cr = Engine.cropRect(img.width, img.height, ed)
        val cx = cr[0]
        val cy = cr[1]
        val cw = cr[2]
        val ch = cr[3]
        val fw = cw.toFloat()
        val fh = ch.toFloat()
        when (fit) {
            Fit.FILL -> {
                val s = maxOf(dw / fw, dh / fh)
                val sw = (dw / s).toInt().coerceIn(1, cw)
                val sh = (dh / s).toInt().coerceIn(1, ch)
                drawImage(
                    img,
                    srcOffset = IntOffset(cx + ((cw - sw) * crop).toInt(), cy + ((ch - sh) * crop).toInt()),
                    srcSize = IntSize(sw, sh),
                    dstOffset = IntOffset(0, 0),
                    dstSize = IntSize(dw.toInt(), dh.toInt()),
                    colorFilter = cf
                )
            }
            Fit.FIT -> {
                val s = minOf(dw / fw, dh / fh)
                val ow = (fw * s).toInt().coerceAtLeast(1)
                val oh = (fh * s).toInt().coerceAtLeast(1)
                drawImage(
                    img,
                    srcOffset = IntOffset(cx, cy),
                    srcSize = IntSize(cw, ch),
                    dstOffset = IntOffset(((dw - ow) / 2f).toInt(), ((dh - oh) / 2f).toInt()),
                    dstSize = IntSize(ow, oh),
                    colorFilter = cf
                )
            }
            Fit.STRETCH -> drawImage(
                img,
                srcOffset = IntOffset(cx, cy),
                srcSize = IntSize(cw, ch),
                dstOffset = IntOffset(0, 0),
                dstSize = IntSize(dw.toInt(), dh.toInt()),
                colorFilter = cf
            )
            Fit.GLOW -> {
                val stK = ed.stretch / 100f
                val s0 = minOf(dw / fw, dh / fh) * ed.zoom / 100f
                val ow = (fw * (s0 + (dw / fw - s0) * stK)).toInt().coerceAtLeast(1)
                val oh = (fh * (s0 + (dh / fh - s0) * stK)).toInt().coerceAtLeast(1)
                val ox = (dw - ow) / 2f + dw * ed.moveX / 100f
                val oy = (dh - oh) / 2f + dh * ed.moveY / 100f
                val gr = dw - ox - ow
                val gb = dh - oy - oh
                drawImage(
                    img,
                    srcOffset = IntOffset(cx, cy),
                    srcSize = IntSize(cw, ch),
                    dstOffset = IntOffset(ox.toInt(), oy.toInt()),
                    dstSize = IntSize(ow, oh),
                    colorFilter = cf
                )
            }
        }
        if (ed.text.isNotBlank()) Engine.drawOverlay(drawContext.canvas.nativeCanvas, dw, dh, ed)
        if (fadeFrames > 0) {
            val n = totalFrames.coerceAtLeast(1)
            var gain = 256
            if (i < fadeFrames) gain = 256 * (i + 1) / (fadeFrames + 1)
            if (n - 1 - i < fadeFrames) gain = minOf(gain, 256 * (n - i) / (fadeFrames + 1))
            drawRect(Color.Black.copy(alpha = (1f - gain / 256f).coerceIn(0f, 1f)))
        }
    }
    }
}

@Composable
private fun TextBox(value: String, hint: String, onChange: (String) -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Tx.copy(alpha = 0.07f))
            .border(1.dp, Tx.copy(alpha = 0.16f), shape)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        if (value.isEmpty()) Text(hint, color = Tx.copy(alpha = 0.4f), fontSize = 14.sp)
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = TextStyle(color = Tx, fontSize = 14.sp),
            cursorBrush = SolidColor(Cyan),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Timeline(durSec: Float, startSec: Float, spanSec: Float) {
    Canvas(Modifier.fillMaxWidth().height(30.dp)) {
        val pad = 9.dp.toPx()
        val w = size.width - pad * 2f
        val barH = 8.dp.toPx()
        val y = (size.height - barH) / 2f
        val d = durSec.coerceAtLeast(0.1f)
        drawRoundRect(Tx.copy(alpha = 0.13f), Offset(pad, y), Size(w, barH), CornerRadius(barH / 2f))
        val x0 = pad + (startSec / d).coerceIn(0f, 1f) * w
        val x1 = pad + ((startSec + spanSec) / d).coerceIn(0f, 1f) * w
        val sw = maxOf(x1 - x0, 6f)
        drawRoundRect(
            Brush.horizontalGradient(listOf(Cyan, Violet, Pink), startX = x0, endX = x0 + sw),
            Offset(x0, y), Size(sw, barH), CornerRadius(barH / 2f)
        )
        drawCircle(Cyan, 7.dp.toPx(), Offset(x0, size.height / 2f))
        drawCircle(Cyan, 7.dp.toPx(), Offset(x0 + sw, size.height / 2f))
    }
}

/* ================= main screen ================= */

@Composable
private fun App() {
    val ctx = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var tab by remember { mutableIntStateOf(0) }
    // changing tab: the old page slides out, the new one slides in
    val pageAnim = remember { androidx.compose.animation.core.Animatable(1f) }
    var slideDir by remember { mutableIntStateOf(1) }
    var pageLeaving by remember { mutableStateOf(false) }
    var navJob by remember { mutableStateOf<Job?>(null) }
    fun goTab(n: Int) {
        if (n == tab) return
        navJob?.cancel()
        navJob = scope.launch {
            slideDir = if (n > tab) 1 else -1
            pageLeaving = true
            pageAnim.animateTo(0f, tween(130))
            tab = n
            pageLeaving = false
            pageAnim.snapTo(0f)
            pageAnim.animateTo(1f, spring(0.8f, 320f))
        }
    }
    var rootState by remember { mutableIntStateOf(Engine.rootState) }
    var makeBoot by remember { mutableStateOf(true) }
    var makeShutdown by remember { mutableStateOf(false) }
    var libOk by remember { mutableStateOf(Engine.findLib(ctx) != null) }
    var target by remember { mutableStateOf(Engine.loadTarget(ctx) ?: Engine.detectTarget(ctx, false)) }

    // source
    var srcUri by remember { mutableStateOf<Uri?>(null) }
    var srcName by remember { mutableStateOf("") }
    var srcDurMs by remember { mutableLongStateOf(0L) }
    var scrubJob by remember { mutableStateOf<Job?>(null) }

    // options
    var startSec by remember { mutableFloatStateOf(0f) }
    var lenSec by remember { mutableFloatStateOf(6f) }
    var speed by remember { mutableFloatStateOf(1f) }
    var fit by remember { mutableStateOf(Fit.GLOW) }   // one mode only: the whole video, nothing cut off
    var crop by remember { mutableStateOf(0.5f) }
    var fadeFrames by remember { mutableIntStateOf(0) }
    var quality by remember { mutableIntStateOf(1) }
    var dither by remember { mutableStateOf(false) }
    var useTarget by remember { mutableStateOf(false) }
    var targetMb by remember { mutableFloatStateOf(2f) }

    // extra editing
    var brightness by remember { mutableIntStateOf(0) }
    var contrast by remember { mutableIntStateOf(100) }
    var overlayText by remember { mutableStateOf("") }
    var textPos by remember { mutableIntStateOf(2) }
    var textSize by remember { mutableIntStateOf(1) }
    // crop: a box of any size / shape, which is then fitted to the screen
    var cropW by remember { mutableIntStateOf(100) }
    var cropH by remember { mutableIntStateOf(100) }
    var panX by remember { mutableIntStateOf(50) }
    var panY by remember { mutableIntStateOf(50) }
    // size: the whole video can be made bigger or smaller and moved, so it fits the screen the way you want
    var zoom by remember { mutableIntStateOf(100) }
    var moveX by remember { mutableIntStateOf(0) }
    var moveY by remember { mutableIntStateOf(0) }
    var bg by remember { mutableIntStateOf(0xFF000000.toInt()) }
    var zoomAcc by remember { mutableStateOf(100f) }   // exact value while pinching
    var stretch by remember { mutableIntStateOf(0) }
    val curEdits = Edits(brightness, contrast, overlayText, textPos, textSize, cropW, cropH, panX, panY, zoom, moveX, moveY, bg, stretch)

    // the root manager has no metamodule (found by the silent root check); shown as a warning at the top
    var metaWarn by remember { mutableStateOf<String?>(null) }
    var metaSkipped by remember { mutableStateOf(false) }   // "Skip" on the full-screen warning (until the app is opened again)
    var metaBusy by remember { mutableStateOf(false) }
    var editTab by remember { mutableIntStateOf(0) }   // Trim / Fit / Crop / Look when the Edit card shows tabs

    // community gallery (Gallery tab)
    var galItems by remember { mutableStateOf<List<Gallery.Item>?>(null) }
    var galLoading by remember { mutableStateOf(false) }
    var galError by remember { mutableStateOf<String?>(null) }
    var galNotSetUp by remember { mutableStateOf(false) }
    val galThumbs = remember { mutableStateMapOf<String, ImageBitmap>() }
    var galPick by remember { mutableStateOf<Gallery.Item?>(null) }      // item waiting for the "Install?" answer
    var galBusyName by remember { mutableStateOf<String?>(null) }         // item being downloaded / installed
    var galPct by remember { mutableIntStateOf(0) }

    // crop shape typed by the user (width : height), applied with a button
    var shapeW by remember { mutableStateOf("") }
    var shapeH by remember { mutableStateOf("") }
    val notifPerm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    // live stage (plays the chosen part of your clip with the current settings)
    var stageImages by remember { mutableStateOf<List<ImageBitmap>>(emptyList()) }
    val coveredFor = remember { arrayOfNulls<Any>(1) }
    // zoom (percent) that makes the picture cover the whole screen, null while no frame is loaded
    fun coverZoom(): Int? {
        val im = stageImages.firstOrNull() ?: return null
        val rx = target.w.toFloat() / im.width
        val ry = target.h.toFloat() / im.height
        return (100f * maxOf(rx, ry) / minOf(rx, ry)).toInt().coerceIn(100, 800)
    }
    LaunchedEffect(srcUri, stageImages.isNotEmpty()) {
        // a newly picked video starts as "full screen": the picture covers the whole screen, nothing blurred, no bars
        val cz = coverZoom()
        if (cz != null && srcUri != null && coveredFor[0] != srcUri) {
            coveredFor[0] = srcUri
            zoom = cz
            moveX = 0; moveY = 0
        }
    }
    var stageIdx by remember { mutableIntStateOf(0) }
    var stagePlaying by remember { mutableStateOf(true) }
    var clipLoading by remember { mutableStateOf(false) }
    var clipPct by remember { mutableIntStateOf(0) }
    var exactBmp by remember { mutableStateOf<Bitmap?>(null) }
    var exactBytes by remember { mutableIntStateOf(0) }
    var exactBusy by remember { mutableStateOf(false) }

    // work
    var busy by remember { mutableStateOf(false) }
    var done by remember { mutableIntStateOf(0) }
    var total by remember { mutableIntStateOf(0) }
    var convStart by remember { mutableLongStateOf(0L) }
    var liveFrame by remember { mutableStateOf<Bitmap?>(null) }
    var liveBytes by remember { mutableLongStateOf(0L) }
    var job by remember { mutableStateOf<Job?>(null) }
    val built = remember { mutableStateMapOf<Int, Built>() }
    var status by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf<String?>(null) }

    // app update (checked on open, or from the About card)
    var update by remember { mutableStateOf<Updater.Info?>(null) }
    var updBusy by remember { mutableStateOf(false) }
    var updProgress by remember { mutableFloatStateOf(0f) }
    var updChecking by remember { mutableStateOf(false) }

    fun checkUpdate(manual: Boolean) {
        if (updChecking) return
        updChecking = true
        scope.launch {
            val r = withContext(Dispatchers.IO) { runCatching { Updater.check(ctx) } }
            updChecking = false
            val info = r.getOrNull()
            if (info != null) update = info
            else if (manual) status = if (r.isFailure) "Could not check for updates. Check your internet." else "You are on the latest version ✓"
        }
    }

    fun startUpdate(info: Updater.Info) {
        if (!Updater.canInstall(ctx)) {
            status = "Allow installs from QMG Studio, come back and tap Update again."
            Updater.openInstallPermission(ctx)
            return
        }
        updBusy = true
        updProgress = 0f
        scope.launch {
            val r = withContext(Dispatchers.IO) { runCatching { Updater.download(ctx, info) { updProgress = it } } }
            updBusy = false
            val f = r.getOrNull()
            if (f != null) {
                update = null
                Updater.install(ctx, f)
            } else {
                status = "Update failed: " + (r.exceptionOrNull()?.message ?: "unknown error")
            }
        }
    }

    // result player
    var previewFile by remember { mutableStateOf<File?>(null) }
    var previewVersion by remember { mutableIntStateOf(0) }
    var previewBmp by remember { mutableStateOf<Bitmap?>(null) }
    var playing by remember { mutableStateOf(true) }
    var frameNo by remember { mutableIntStateOf(0) }
    var frameTotal by remember { mutableIntStateOf(0) }
    var verifyText by remember { mutableStateOf("") }
    var verifying by remember { mutableStateOf(false) }
    var gifBusy by remember { mutableStateOf(false) }
    var backingUp by remember { mutableStateOf(false) }

    // derived values
    val durSec = if (srcDurMs > 0) srcDurMs / 1000f else 3f
    val maxLen = minOf(Engine.MAX_SECONDS.toFloat(), ((durSec - startSec) / speed)).coerceAtLeast(1f)
    val len = lenSec.coerceIn(1f, maxLen)
    val maxStart = (durSec - len * speed).coerceAtLeast(0f)
    val frames = (len * Engine.FPS).toInt()
    val aspect = (if (target.h > 0) target.w.toFloat() / target.h else 0.45f).coerceIn(0.25f, 2f)

    /* ----- pickers ----- */

    val sourcePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            srcUri = uri
            scope.launch {
                val info = withContext(Dispatchers.IO) {
                    Triple(Engine.displayName(ctx, uri), Engine.durationMs(ctx, uri), Engine.thumbnail(ctx, uri, 0L))
                }
                srcName = info.first
                srcDurMs = info.second
                startSec = 0f
                lenSec = minOf(6f, if (info.second > 0) info.second / 1000f else 3f).coerceAtLeast(1f)
                exactBmp = null
                val th = info.third
                stageImages = if (th != null) listOf(th.asImageBitmap()) else emptyList()
                stageIdx = 0
                status = ""
            }
        }
    }
    val libPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val ok = Engine.importLib(ctx, uri) && Engine.findLib(ctx) != null
            libOk = ok
            status = if (ok) "✅ Library imported" else "❌ That file did not work"
        }
    }
    val qmgOpener = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val f = File(ctx.cacheDir, "opened.qmg")
                try {
                    ctx.contentResolver.openInputStream(uri)?.use { i -> f.outputStream().use { o -> i.copyTo(o) } }
                    withContext(Dispatchers.Main) {
                        previewFile = f; previewVersion++; playing = true; tab = 1
                        status = "Opened " + Engine.displayName(ctx, uri)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { status = "❌ Cannot open that file" }
                }
            }
        }
    }
    val saverBoot = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri: Uri? ->
        val f = built[0]?.file
        if (uri != null && f != null) {
            scope.launch(Dispatchers.IO) {
                val ok = Engine.writeTo(ctx, uri) { o -> f.inputStream().use { it.copyTo(o) } }
                withContext(Dispatchers.Main) { status = if (ok) "💾 bootsamsung.qmg saved ✔" else "❌ Save failed: " + Engine.lastError }
            }
        }
    }
    val saverShut = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri: Uri? ->
        val f = built[1]?.file
        if (uri != null && f != null) {
            scope.launch(Dispatchers.IO) {
                val ok = Engine.writeTo(ctx, uri) { o -> f.inputStream().use { it.copyTo(o) } }
                withContext(Dispatchers.Main) { status = if (ok) "💾 shutdown.qmg saved ✔" else "❌ Save failed: " + Engine.lastError }
            }
        }
    }
    val zipSaver = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri: Uri? ->
        if (uri != null) {
            val boot = built[0]?.file
            val shut = built[1]?.file
            val dir = target.stockDir
            scope.launch(Dispatchers.IO) {
                val ok = Engine.writeTo(ctx, uri) { o -> Engine.writeModuleZip(boot, boot, shut, dir, o) }
                withContext(Dispatchers.Main) { status = if (ok) "📦 Module zip saved ✔" else "❌ Save failed: " + Engine.lastError }
            }
        }
    }
    val gifSaver = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/gif")) { uri: Uri? ->
        val f = previewFile
        val lib = Engine.findLib(ctx)
        if (uri != null && f != null && lib != null) {
            gifBusy = true
            status = "🎞️ Making GIF…"
            scope.launch(Dispatchers.IO) {
                var err: String? = null
                val ok = Engine.writeTo(ctx, uri) { o ->
                    err = Gif.export(f, lib.absolutePath, o) { d, t -> status = "🎞️ GIF frame $d / $t" }
                }
                withContext(Dispatchers.Main) {
                    gifBusy = false
                    status = if (err != null) "❌ " + err else if (ok) "🎞️ GIF saved ✔" else "❌ Save failed: " + Engine.lastError
                }
            }
        }
    }

    fun clipBase(): String = Engine.baseName(srcName)

    fun loadGallery() {
        if (galLoading) return
        galLoading = true
        galError = null
        galNotSetUp = false
        scope.launch(Dispatchers.IO) {
            try {
                val list = Gallery.list()
                withContext(Dispatchers.Main) { galItems = list; galLoading = false }
                for (gi in list) {
                    val u = gi.thumbUrl ?: continue
                    if (galThumbs.containsKey(gi.name)) continue
                    val b = Gallery.thumb(u) ?: continue
                    withContext(Dispatchers.Main) { galThumbs[gi.name] = b.asImageBitmap() }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    galLoading = false
                    galNotSetUp = e is Gallery.NotSetUp
                    galError = if (e is Gallery.NotSetUp) null else "Could not load Explore: " + (e.message ?: "no internet?")
                }
            }
        }
    }

    fun installFromGallery(item: Gallery.Item) {
        if (galBusyName != null) return
        galBusyName = item.name
        galPct = 0
        scope.launch(Dispatchers.IO) {
            var text = ""
            try {
                val zip = Gallery.download(ctx, item) { d, t -> if (t > 0) galPct = (d * 100 / t).toInt() }
                val fl = Gallery.extract(ctx, zip)
                if (fl.boot == null && fl.shutdown == null) {
                    text = "❌ This file has no animation inside"
                } else {
                    val ok = Engine.installModuleRoot(ctx, fl.boot, fl.loop, fl.shutdown, target.stockDir)
                    text = if (ok) "⚡ Installed " + item.title + ". Reboot to see it." else "❌ " + Engine.lastError
                }
            } catch (e: Exception) {
                text = "❌ " + (e.message ?: "Download failed")
            }
            withContext(Dispatchers.Main) { galBusyName = null; status = text }
        }
    }

    fun shareToGallery() {
        val boot = built[0]?.file
        val shut = built[1]?.file
        if (boot == null && shut == null) { status = "Make an animation first"; return }
        val dir = target.stockDir
        val name = (clipBase() + "_" + target.w + "x" + target.h).replace(Regex("[^A-Za-z0-9._-]"), "_")
        val thumb: Bitmap? = stageImages.firstOrNull()?.asAndroidBitmap()
        status = "💾 Preparing your animation…"
        scope.launch(Dispatchers.IO) {
            val where = Engine.saveMain(ctx, name + ".zip", "application/zip") { o -> Engine.writeModuleZip(boot, boot, shut, dir, o) }
            if (where != null && thumb != null) {
                try {
                    val jpg = Gallery.thumbJpeg(thumb)
                    Engine.saveMain(ctx, name + ".jpg", "image/jpeg") { o -> o.write(jpg) }
                } catch (e: Exception) { }
            }
            withContext(Dispatchers.Main) {
                if (where != null) {
                    status = "📤 Saved as " + name + ".zip. Now upload it on the page that opens."
                    uriHandler.openUri(Gallery.UPLOAD_URL)
                } else status = "❌ Save failed: " + Engine.lastError
            }
        }
    }
    fun saveDirect(fileName: String, mime: String, okText: String, write: (java.io.OutputStream) -> Unit) {
        status = "💾 Saving…"
        scope.launch(Dispatchers.IO) {
            val where = Engine.saveMain(ctx, fileName, mime, write)
            withContext(Dispatchers.Main) {
                status = if (where != null) "$okText → $where ✔" else "❌ Save failed: " + Engine.lastError
            }
        }
    }

    /* ----- actions ----- */

    fun toggleBoot() { if (makeBoot && !makeShutdown) return; makeBoot = !makeBoot }
    fun toggleShutdown() { if (makeShutdown && !makeBoot) return; makeShutdown = !makeShutdown }

    // instant single frame while the start slider moves (the full clip reloads right after)
    fun scrubTo() {
        val u = srcUri ?: return
        scrubJob?.cancel()
        scrubJob = scope.launch {
            delay(120)
            val b = withContext(Dispatchers.IO) { Engine.thumbnail(ctx, u, (startSec * 1000).toLong()) }
            if (b != null) { stageImages = listOf(b.asImageBitmap()); stageIdx = 0 }
        }
    }

    fun runExact() {
        val u = srcUri ?: return
        if (!libOk || exactBusy || busy) return
        exactBusy = true
        val pn = stageImages.size
        // the preview may hold fewer frames than the final animation: find the real output frame
        val i = if (pn > 1 && pn < frames) (stageIdx.toLong() * (frames - 1) / (pn - 1)).toInt() else stageIdx
        scope.launch {
            val tMs = (startSec * 1000).toLong() + (i * 1000f / Engine.FPS * speed).toLong()
            val src = withContext(Dispatchers.IO) { Engine.frameAt(ctx, u, tMs) }
            val r = if (src != null) Engine.previewFrame(ctx, src, target, fit, dither, quality, curEdits) else null
            if (r != null) {
                exactBmp = r.first
                exactBytes = r.second
            } else {
                status = "Exact view is busy, try again in a moment"
            }
            exactBusy = false
        }
    }

    fun startConvert() {
        val uri = srcUri ?: return
        busy = true
        status = ""
        done = 0
        total = 0
        convStart = System.currentTimeMillis()
        liveFrame = null
        liveBytes = 0L
        exactBmp = null
        val wantBoot = makeBoot
        val wantShut = makeShutdown
        val bootFile = File(ctx.filesDir, "boot.qmg")
        val shutFile = File(ctx.filesDir, "shutdown.qmg")
        val first = if (wantBoot) bootFile else shutFile
        val opts = Opts(
            startMs = (startSec * 1000).toLong(), lenSec = len, quality = quality, fit = fit,
            dither = dither, fadeFrames = fadeFrames, speed = speed,
            targetKb = if (useTarget) (targetMb * 1024).toInt() else 0,
            ed = curEdits
        )
        // keeps the encode alive (with a progress notification) while the app is in the background
        if (Build.VERSION.SDK_INT >= 33 &&
            ctx.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) notifPerm.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        EncodeService.start(ctx)
        job = scope.launch {
            try {
                val b = Engine.convert(ctx, uri, target, opts, first, { d, t -> done = d; total = t; EncodeService.progress(ctx, d, t) }) { bm, bytes ->
                    liveFrame = bm
                    liveBytes = bytes
                }
                built.clear()   // never keep results of an older convert
                if (wantBoot) built[0] = b
                if (wantShut) {
                    if (first != shutFile) first.copyTo(shutFile, overwrite = true)
                    built[1] = b.copy(file = shutFile)
                }
                previewFile = first
                previewVersion++
                playing = true
                verifyText = ""
                val secs = (System.currentTimeMillis() - convStart) / 1000
                status = "✅ Done in " + secs + " s: " + b.frames + " frames, " + sizeText(b.bytes)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                EncodeService.done(ctx, "✅ Animation ready · " + sizeText(b.bytes))
                tab = 1
            } catch (e: CancellationException) {
                status = "Cancelled"
                throw e
            } catch (e: OutOfMemoryError) {
                status = "❌ Out of memory. Try a shorter clip or turn off the size target."
            } catch (e: Exception) {
                val at = e.stackTrace.firstOrNull { it.className.startsWith("com.namal") }
                val where = if (at != null) " @ " + at.fileName + ":" + at.lineNumber else ""
                status = "❌ " + e.javaClass.simpleName + ": " + (e.message ?: "no message") + where
            } finally {
                busy = false
                liveFrame = null
                EncodeService.stop(ctx)
            }
        }
    }

    // crop box with the given shape (width / height), as large as it can be inside the video
    fun setCropAspect(a: Float) {
        val img = stageImages.firstOrNull()
        val sa = if (img != null && img.height > 0) img.width.toFloat() / img.height else 16f / 9f
        if (a <= sa) {
            cropH = 100
            cropW = (a / sa * 100f).toInt().coerceIn(10, 100)
        } else {
            cropW = 100
            cropH = (sa / a * 100f).toInt().coerceIn(10, 100)
        }
        panX = 50
        panY = 50
    }

    // the metamodule check needs root, so it only runs once root is granted
    fun recheckMeta() {
        if (metaBusy) return
        metaBusy = true
        scope.launch(Dispatchers.IO) {
            val r = Diag.run(ctx)
            withContext(Dispatchers.Main) {
                metaWarn = r.metaWarn
                metaBusy = false
                status = if (r.metaWarn == null) "✅ Metamodule check passed" else "⚠️ Still no metamodule found"
            }
        }
    }

    // copies a full diagnostics report to the clipboard (the checks themselves run silently in the background)
    fun copyDiag() {
        status = "🩺 Collecting the report…"
        scope.launch(Dispatchers.IO) {
            val r = Diag.run(ctx)
            withContext(Dispatchers.Main) {
                val cm = ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                cm.setPrimaryClip(android.content.ClipData.newPlainText("QMG Studio report", r.text))
                status = if (r.rootOk) "📋 Report copied" else "📋 Report copied (root was not available)"
            }
        }
    }

    // crop box of ANY shape typed by the user (for example 400 : 100); the phone itself never changes shape
    fun applyCropShape() {
        val w = shapeW.toIntOrNull() ?: 0
        val h = shapeH.toIntOrNull() ?: 0
        if (w <= 0 || h <= 0) {
            status = "❌ Type a width and a height, for example 400 and 100"
            return
        }
        setCropAspect(w.toFloat() / h)
        status = "✂️ Crop box set to " + w + " : " + h
    }

    fun requestRootUi() {
        status = "🔐 Asking for root… (allow it in your root manager)"
        scope.launch(Dispatchers.IO) {
            val ok = Engine.requestRoot()
            withContext(Dispatchers.Main) {
                rootState = if (ok) 1 else 2
                if (ok) Diag.setRootOk(ctx, true)
                status = if (ok) "✅ Root granted" else "❌ Root not available or denied. QMG Studio needs root."
            }
            if (ok) {
                val r = Diag.run(ctx)
                withContext(Dispatchers.Main) { metaWarn = r.metaWarn }
                if (Engine.loadTarget(ctx) == null) {
                    val t = Engine.detectTarget(ctx, true)
                    Engine.saveTarget(ctx, t)
                    withContext(Dispatchers.Main) { target = t }
                }
            }
        }
    }

    fun detectWithRoot() {
        status = "🔎 Looking for your stock animation… (allow root if asked)"
        scope.launch(Dispatchers.IO) {
            val t = Engine.detectTarget(ctx, true)
            Engine.saveTarget(ctx, t)
            withContext(Dispatchers.Main) {
                target = t
                status = "📱 Using " + t.w + "×" + t.h + " (" + t.source + ")"
            }
        }
    }

    fun backup() {
        if (backingUp) return
        backingUp = true
        status = "🛟 Backing up… (allow root if asked)"
        scope.launch(Dispatchers.IO) {
            val n = Engine.backupStock(ctx)
            val t = Engine.detectTarget(ctx, true)
            withContext(Dispatchers.Main) {
                if (t.stockDir != null) { target = t; Engine.saveTarget(ctx, t) }
                status = if (n > 0) "🛟 Backed up $n file(s) → " + Engine.lastBackupDir
                else "❌ No stock .qmg found / root denied. " + Engine.lastError
                backingUp = false
            }
        }
    }

    fun runVerify() {
        val f = previewFile ?: return
        verifying = true
        verifyText = "Checking…"
        scope.launch(Dispatchers.IO) {
            val r = Engine.verify(ctx, f)
            withContext(Dispatchers.Main) { verifyText = r; verifying = false }
        }
    }

    /* ----- effects ----- */

    // the exact (real encoder) picture is only valid for the settings it was made with
    LaunchedEffect(fit, dither, quality, startSec, lenSec, speed, srcUri, target, curEdits) { exactBmp = null }

    // loads the chosen part of the clip as small frames (debounced) so the stage can play it live
    val lastLoaded = remember { arrayOfNulls<Any>(1) }
    val loadedInfo = remember { DoubleArray(3) }         // start, end (seconds) and speed of the frames in loadedFrames
    val loadedFrames = remember { arrayOfNulls<Any>(1) } // all frames of the last finished load
    LaunchedEffect(srcUri, startSec, lenSec, speed) {
        val u = srcUri ?: return@LaunchedEffect
        // already loaded? A shorter / moved range inside it only needs a slice of those frames, no new loading
        @Suppress("UNCHECKED_CAST")
        val have = loadedFrames[0] as? List<ImageBitmap>
        val s0 = startSec.toDouble()
        val e0 = s0 + len.toDouble()
        if (have != null && have.size > 3 && lastLoaded[0] == u && loadedInfo[2] == speed.toDouble() && s0 >= loadedInfo[0] - 0.02 && e0 <= loadedInfo[1] + 0.02) {
            val span = loadedInfo[1] - loadedInfo[0]
            val lo = (((s0 - loadedInfo[0]) / span) * (have.size - 1)).toInt().coerceIn(0, have.size - 2)
            val hi = (((e0 - loadedInfo[0]) / span) * (have.size - 1)).toInt().coerceIn(lo + 1, have.size - 1)
            stageImages = have.subList(lo, hi + 1).toList()
            stageIdx = 0
            clipLoading = false
            return@LaunchedEffect
        }
        if (lastLoaded[0] != null && lastLoaded[0] == u) delay(400)   // a new video starts loading (and playing) at once
        lastLoaded[0] = u
        clipLoading = true
        clipPct = 0
        val c = try {
            Engine.previewClip(
                ctx, u, (startSec * 1000).toLong(), len, speed,
                onPass = { list ->
                    // every round already covers the WHOLE clip (coarse first), so it can play at once and gets smoother
                    val old = stageImages.size
                    stageImages = list.map { it.asImageBitmap() }
                    stageIdx = if (old > 1) (stageIdx.toLong() * list.size / old).toInt().coerceIn(0, list.size - 1) else 0
                }
            ) { d, t -> clipPct = d * 100 / maxOf(1, t) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        if (c != null) {
            stageImages = c.frames.map { it.asImageBitmap() }
            stageIdx = 0
            loadedFrames[0] = stageImages
            loadedInfo[0] = startSec.toDouble(); loadedInfo[1] = startSec.toDouble() + len.toDouble(); loadedInfo[2] = speed.toDouble()
        }
        clipLoading = false
    }

    // stage player: just steps an index at the animation's own frame rate
    LaunchedEffect(stageImages, stagePlaying, tab, busy, exactBmp) {
        if (tab != 0 || !stagePlaying || busy || stageImages.size < 2 || exactBmp != null) return@LaunchedEffect
        // a long clip is shown with fewer frames, so every step lasts longer and the whole clip still plays in its real length
        val stepMs = (len * 1000f / stageImages.size).toLong().coerceIn(1000L / Engine.FPS, 700L)
        while (isActive) {
            delay(stepMs)
            stageIdx = (stageIdx + 1) % stageImages.size
        }
    }

    // result player: decodes the .qmg frame by frame, the same way the phone will
    LaunchedEffect(previewFile, previewVersion, tab, playing) {
        val f = previewFile
        val lib = Engine.findLib(ctx)
        if (tab != 1 || !playing || f == null || lib == null) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            try {
                while (isActive) {
                    val n = Reader.open(f.absolutePath, lib.absolutePath)
                    if (n <= 0) {
                        status = "⚠️ Cannot open this .qmg for preview"
                        break
                    }
                    frameTotal = n
                    var i = 0
                    while (isActive) {
                        val t0 = System.nanoTime()
                        val b = Reader.next(true) ?: break
                        previewBmp = b
                        i++
                        frameNo = i
                        val spent = (System.nanoTime() - t0) / 1000000L
                        delay(maxOf(1L, 1000L / Engine.FPS - spent))
                    }
                    val rc = Reader.lastRc
                    Reader.close()
                    if (rc < 0) {
                        status = if (rc == -5) "⚠️ Preview not available: this file uses features the built-in decoder does not support yet"
                        else "⚠️ Preview error ($rc)"
                        break
                    }
                }
            } finally {
                Reader.close()
            }
        }
    }

    val scroll = rememberScrollState()
    // ask for root when the app opens, then learn the size of this phone's stock animation
    LaunchedEffect(Unit) {
        // the check gets 500 ms: if su answers in time the app opens at once, otherwise the main screen opens anyway
        // and the root pop-up of the root manager keeps waiting in the background (root turns on when it is allowed)
        val job = async(Dispatchers.IO) { Engine.requestRoot() }
        val quick = withTimeoutOrNull(500L) { job.await() }
        if (quick == null) rootState = 2
        val ok = quick ?: job.await()
        rootState = if (ok) 1 else 2
        if (ok) {
            Diag.setRootOk(ctx, true)
            // metamodule check first (needs root, takes about a second): a full-screen warning appears if it is missing
            val r = withContext(Dispatchers.IO) { Diag.run(ctx) }
            metaWarn = r.metaWarn
            val problem = r.problem ?: r.metaWarn
            if (problem != null) Diag.notifyProblem(ctx, problem)
            if (Engine.loadTarget(ctx) == null) {
                val t = withContext(Dispatchers.IO) { Engine.detectTarget(ctx, true) }
                Engine.saveTarget(ctx, t)
                target = t
            }
        } else {
            status = "Root is needed to use QMG Studio"
        }
    }

    LaunchedEffect(tab) { scroll.scrollTo(0) }
    LaunchedEffect(rootState) { if (rootState == 1) tab = 0 }   // root granted: the main screen opens on Create

    // look for a newer release every time the app opens
    LaunchedEffect(Unit) { checkUpdate(false) }

    /* ----- layout ----- */

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val stageH = (maxHeight * 0.27f).coerceIn(170.dp, 260.dp)
        val bigPhoneH = (maxHeight * 0.5f).coerceIn(260.dp, 430.dp)

        val glassLayer = rememberGraphicsLayer()
        SideEffect {
            glassLayer.renderEffect = if (Build.VERSION.SDK_INT >= 31) BlurEffect(48f, 48f, TileMode.Clamp) else null
            glassLayerRef = glassLayer
        }
        Box(
            Modifier.fillMaxSize().drawWithContent {
                if (Th.cur.glass) glassLayer.record { this@drawWithContent.drawContent() }
                drawContent()
            }
        ) { NightBackdrop(!busy) }

        Column(Modifier.fillMaxSize().systemBarsPadding()) {

            // header
            Row(
                Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LogoMark(42, busy)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        "QMG Studio",
                        style = TextStyle(
                            brush = Brush.horizontalGradient(listOf(Cyan, Violet, Pink)),
                            fontFamily = Th.cur.head,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Pill(Engine.deviceName(), Cyan)
                        Pill("Android " + Build.VERSION.RELEASE, Violet)
                        Pill(target.w.toString() + "×" + target.h, Violet)
                        Pill(if (libOk) "Decoder ✓" else "Decoder ✗", if (libOk) Mint else Danger)
                        Pill(
                            when (rootState) { 1 -> "Root ✓"; 2 -> "Root ✗ (tap)"; else -> "Root …" },
                            when (rootState) { 1 -> Mint; 2 -> Danger; else -> Peach }
                        ) { requestRootUi() }
                    }
                }
            }

            // pinned stage: live preview while editing, progress while converting
            if (tab == 0 && busy) {
                val shape = RoundedCornerShape(24.dp)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .clip(shape)
                        .background(Th.cur.stage)
                        .border(1.dp, Brush.horizontalGradient(listOf(Pink.copy(alpha = 0.6f), Violet.copy(alpha = 0.6f))), shape)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PhoneMock(aspect, stageH) {
                        val lf = liveFrame
                        if (lf != null) {
                            Image(
                                bitmap = lf.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.FillBounds
                            )
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("CONVERTING", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { if (total > 0) done / total.toFloat() else 0f },
                                    modifier = Modifier.size(70.dp),
                                    strokeWidth = 6.dp,
                                    color = Cyan,
                                    trackColor = Tx.copy(alpha = 0.1f)
                                )
                                Text(
                                    (if (total > 0) (done * 100 / total) else 0).toString() + "%",
                                    color = Tx, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Label("Frame $done / $total")
                                Hint("So far " + sizeText(liveBytes))
                                val eta = if (done > 0 && total > done)
                                    ((System.currentTimeMillis() - convStart) / 1000f / done * (total - done)).toInt() else -1
                                if (eta >= 0) Hint("about $eta s left")
                            }
                        }
                        MiniButton("Cancel", Modifier.fillMaxWidth()) { job?.cancel() }
                    }
                }
            } else if (tab == 0 && srcUri != null) {
                val shape = RoundedCornerShape(24.dp)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .clip(shape)
                        .background(Th.cur.stage)
                        .border(1.dp, Brush.horizontalGradient(listOf(Cyan.copy(alpha = 0.55f), Violet.copy(alpha = 0.55f))), shape)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PhoneMock(aspect, stageH) {
                        val ex = exactBmp
                        if (ex != null) {
                            Image(
                                bitmap = ex.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.FillBounds
                            )
                        } else {
                            // the preview may hold fewer frames than the final animation: scale the fade to match
                            val pn = stageImages.size
                            val fadeP = if (fadeFrames > 0 && pn > 1 && pn < frames) maxOf(1, fadeFrames * pn / frames) else fadeFrames
                            LiveCanvas(stageImages, fit, crop, fadeP, if (pn > 1 && pn < frames) pn else frames, curEdits) { stageIdx }
                        }
                        // tap the phone to pause / play
                        Box(
                            Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    // like a video editor: drag with one finger to move the video, pinch with two to make it bigger or smaller
                                    detectTransformGestures { _, pan, zc, _ ->
                                        if (exactBmp == null) {
                                            val base = if (kotlin.math.abs(zoomAcc - zoom) > 1.5f) zoom.toFloat() else zoomAcc
                                            zoomAcc = (base * zc).coerceIn(10f, 800f)
                                            zoom = zoomAcc.toInt()
                                            moveX = (moveX + pan.x / size.width * 100f).toInt().coerceIn(-100, 100)
                                            moveY = (moveY + pan.y / size.height * 100f).toInt().coerceIn(-100, 100)
                                        }
                                    }
                                }
                                .clickable {
                                    if (exactBmp != null) { exactBmp = null; stagePlaying = true }
                                    else stagePlaying = !stagePlaying
                                }
                        )
                        if (clipLoading) {
                            // always shown while the preview loads, even if it only takes a few seconds
                            Column(
                                Modifier.align(Alignment.Center).clip(RoundedCornerShape(16.dp)).background(Color(0xAA000000)).padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Loader(Cyan, 44.dp)
                                Text("Loading preview… " + clipPct + "%", color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text("LIVE PREVIEW", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text(
                            fitName(fit) + (if (fadeFrames > 0) " · fade" else "") + (if (speed != 1f) " · " + speed + "×" else ""),
                            color = Tx, fontSize = 15.sp, fontWeight = FontWeight.Bold
                        )
                        Hint(frames.toString() + " frames · " + String.format("%.1f", len) + " s")
                        Hint(
                            if (clipLoading) "Loading your clip… " + clipPct + "%"
                            else if (exactBmp != null) "Real encoder view · " + (exactBytes / 1024) + " KB for this frame"
                            else if (stagePlaying) "Playing live · drag to move, pinch to resize"
                            else "Paused · tap the phone to play"
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MiniButton(if (stagePlaying) "⏸" else "▶") { stagePlaying = !stagePlaying }
                            MiniButton(if (exactBusy) "…" else "🔬 Exact", Modifier.weight(1f)) { runExact() }
                        }
                        GlowButton("⚡  Convert", enabled = libOk) { startConvert() }
                    }
                }
            }

            // scrolling content (slides up and fades in every time you change tab)
            Column(
                Modifier
                    .weight(1f)
                    .graphicsLayer {
                        val v = pageAnim.value.coerceIn(0f, 1f)
                        alpha = v
                        translationX = (if (pageLeaving) -slideDir else slideDir) * (1f - v) * 56.dp.toPx()
                        translationY = (1f - v) * 12.dp.toPx()
                    }
                    .verticalScroll(scroll)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (!libOk) {
                    GlassCard("Decoder library", Pink) {
                        Hint("libQmageDecoder.so was not found on this phone. Pick a copy of it (from /system/lib64 of a Samsung device).")
                        GhostButton("Pick libQmageDecoder.so") { libPicker.launch("*/*") }
                    }
                }

                /* ================= CREATE ================= */
                if (tab == 0) {
                    if (!Engine.isSamsung()) {
                        GlassCard("Not a Samsung phone", Danger) {
                            Hint("QMG (.qmg) boot animations only run on Samsung phones (One UI). You can still make an animation here and use it on a Samsung phone.")
                        }
                    }
                    GlassCard("Video or GIF", Cyan, "1") {
                        if (srcName.isNotEmpty()) {
                            Text(srcName, color = Tx, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Hint("Whole clip: " + String.format("%.1f", durSec) + " s")
                        } else {
                            Hint("Pick any video or GIF. It plays on the phone above while you edit, and every change shows up there instantly.")
                        }
                        GlowButton(if (srcUri == null) "🎬  Pick a video or GIF" else "🎬  Change") {
                            sourcePicker.launch(arrayOf("video/*", "image/gif"))
                        }
                    }

                    GlassCard("What are you making?", Pink, "2") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Chip("🚀 Boot", makeBoot) { toggleBoot() }
                            Chip("🌙 Shutdown", makeShutdown) { toggleShutdown() }
                        }
                        Hint(
                            if (makeBoot && makeShutdown) "Both: the same animation is used for starting and shutting down."
                            else if (makeBoot) "Plays while the phone starts (also used for the loop)."
                            else "Plays when the phone shuts down."
                        )
                        val hb = built[0]
                        val hs = built[1]
                        if (hb != null || hs != null) {
                            Hint(
                                "Already made: " +
                                    (if (hb != null) "boot " + sizeText(hb.bytes) else "") +
                                    (if (hb != null && hs != null) ", " else "") +
                                    (if (hs != null) "shutdown " + sizeText(hs.bytes) else "")
                            )
                        }
                    }

                    AnimatedVisibility(visible = srcUri != null) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            GlassCard("Edit", Violet, "3") {
                                if (Th.tabs) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Chip("Trim", editTab == 0) { editTab = 0 }
                                        Chip("Size", editTab == 1) { editTab = 1 }
                                        Chip("Look", editTab == 3) { editTab = 3 }
                                    }
                                }
                                if (!Th.tabs || editTab == 0) {
                                Timeline(durSec, startSec, len * speed)
                                Label(
                                    mmss(startSec) + "  →  " + mmss(startSec + len * speed) +
                                        "   ·   " + String.format("%.1f", len) + " s output"
                                )
                                if (maxLen > 1.05f) {
                                    Hint("Length")
                                    Slider(value = len, onValueChange = { lenSec = it }, valueRange = 1f..maxLen, colors = sliderColors())
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Chip("Whole clip", false) { lenSec = maxLen }
                                        Chip("3 s", false) { lenSec = minOf(3f, maxLen) }
                                        Chip("6 s", false) { lenSec = minOf(6f, maxLen) }
                                    }
                                }
                                if (maxStart > 0.2f) {
                                    Hint("Start at")
                                    Slider(
                                        value = startSec.coerceIn(0f, maxStart),
                                        onValueChange = { startSec = it; scrubTo() },
                                        valueRange = 0f..maxStart,
                                        colors = sliderColors()
                                    )
                                }
                                Label("Speed")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Chip("0.5×", speed == 0.5f) { speed = 0.5f }
                                    Chip("1×", speed == 1f) { speed = 1f }
                                    Chip("1.5×", speed == 1.5f) { speed = 1.5f }
                                    Chip("2×", speed == 2f) { speed = 2f }
                                }
                                }
                                if (!Th.tabs || editTab == 1) {
                                Label("Size " + zoom + "%")
                                Slider(value = zoom.toFloat(), onValueChange = { zoom = it.toInt() }, valueRange = 10f..800f, colors = sliderColors())
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Chip("Whole video", zoom == 100 && moveX == 0 && moveY == 0) { zoom = 100; moveX = 0; moveY = 0; stretch = 0 }
                                    Chip("Full screen", false) {
                                        // zoom just enough that the video covers the whole screen; Left / Right then picks which part shows
                                        val cz = coverZoom()
                                        if (cz != null) { zoom = cz; moveX = 0; moveY = 0 }
                                    }
                                    Chip("Bigger", false) { zoom = (zoom + 10).coerceAtMost(800) }
                                    Chip("Smaller", false) { zoom = (zoom - 10).coerceAtLeast(10) }
                                }
                                Hint("Background colour")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    listOf(0xFF000000, 0xFFFFFFFF, 0xFF1A1A2E, 0xFFB71C1C, 0xFF0D47A1, 0xFF1B5E20, 0xFFF9A825).forEach { c ->
                                        val ci = c.toInt()
                                        Box(
                                            Modifier.size(32.dp).clip(CircleShape).background(Color(ci))
                                                .border(if (bg == ci) 3.dp else 1.dp, if (bg == ci) Cyan else Color.Gray, CircleShape)
                                                .clickable { bg = ci }
                                        )
                                    }
                                }
                                Chip("Match video", false) {
                                    try {
                                        val b0 = stageImages.firstOrNull()?.asAndroidBitmap()
                                        if (b0 != null) {
                                            val sw = b0.copy(Bitmap.Config.ARGB_8888, false)
                                            val px = Bitmap.createScaledBitmap(sw, 1, 1, true).getPixel(0, 0)
                                            bg = px or 0xFF000000.toInt()
                                        }
                                    } catch (_: Throwable) {}
                                }
                                Hint("Left / right")
                                Slider(value = moveX.toFloat(), onValueChange = { moveX = it.toInt() }, valueRange = -100f..100f, colors = sliderColors())
                                Hint("Up / down")
                                Slider(value = moveY.toFloat(), onValueChange = { moveY = it.toInt() }, valueRange = -100f..100f, colors = sliderColors())
                                Hint("Like a video editor: on the phone above, drag with one finger to move the video and pinch with two fingers to make it bigger or smaller. Nothing is cut or stretched, it just sits where you put it (black where it does not reach). \"Full screen\" covers the whole screen, \"Whole video\" shows everything.")
                                }
                                if (false) {   // old fit choices removed: always fitted to the screen
                                Label("Fit to screen")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Chip("Fill", fit == Fit.FILL) { fit = Fit.FILL }
                                    Chip("Fit (bars)", fit == Fit.FIT) { fit = Fit.FIT }
                                    Chip("Stretch", fit == Fit.STRETCH) { fit = Fit.STRETCH }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Chip("Fit + glow (no bars, no crop)", fit == Fit.GLOW) { fit = Fit.GLOW }
                                }
                                if (fit == Fit.FILL) {
                                    Label("Fill: which part to keep")
                                    Slider(
                                        value = crop,
                                        onValueChange = { crop = it; Engine.crop = it },
                                        valueRange = 0f..1f,
                                        colors = sliderColors()
                                    )
                                }
                                Label("Fade from / to black")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Chip("Off", fadeFrames == 0) { fadeFrames = 0 }
                                    Chip("Short", fadeFrames == 4) { fadeFrames = 4 }
                                    Chip("Long", fadeFrames == 8) { fadeFrames = 8 }
                                }
                                Hint("Fit and fade change the phone above right away.")
                                }
                                if (false) {   // crop removed
                                Label("Crop: any size you like")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Chip("Full", cropW == 100 && cropH == 100) { cropW = 100; cropH = 100; panX = 50; panY = 50 }
                                    Chip("Phone shape", false) { setCropAspect(target.w.toFloat() / maxOf(1, target.h)) }
                                    Chip("Square", false) { setCropAspect(1f) }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Chip("16:9", false) { setCropAspect(16f / 9f) }
                                    Chip("9:16", false) { setCropAspect(9f / 16f) }
                                    Chip("4:3", false) { setCropAspect(4f / 3f) }
                                }
                                Label("Or type any shape")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.weight(1f)) { TextBox(shapeW, "Width, e.g. 400") { shapeW = it.filter { c -> c.isDigit() }.take(5) } }
                                    Text(":", color = Tx.copy(alpha = 0.7f), fontSize = 18.sp)
                                    Box(Modifier.weight(1f)) { TextBox(shapeH, "Height, e.g. 100") { shapeH = it.filter { c -> c.isDigit() }.take(5) } }
                                }
                                GhostButton("✂️  Use this crop shape") { applyCropShape() }
                                Label("Crop width " + cropW + "%")
                                Slider(
                                    value = cropW.toFloat(),
                                    onValueChange = { cropW = it.toInt().coerceIn(10, 100) },
                                    valueRange = 10f..100f,
                                    colors = sliderColors()
                                )
                                Label("Crop height " + cropH + "%")
                                Slider(
                                    value = cropH.toFloat(),
                                    onValueChange = { cropH = it.toInt().coerceIn(10, 100) },
                                    valueRange = 10f..100f,
                                    colors = sliderColors()
                                )
                                if (cropW < 100) {
                                    Label("Move the box left / right")
                                    Slider(
                                        value = panX.toFloat(),
                                        onValueChange = { panX = it.toInt() },
                                        valueRange = 0f..100f,
                                        colors = sliderColors()
                                    )
                                }
                                if (cropH < 100) {
                                    Label("Move the box up / down")
                                    Slider(
                                        value = panY.toFloat(),
                                        onValueChange = { panY = it.toInt() },
                                        valueRange = 0f..100f,
                                        colors = sliderColors()
                                    )
                                }
                                Hint("The part inside the box is what gets fitted to your phone's screen. Pick Fill, Fit or Stretch under \"Fit to screen\" above to choose how.")
                                }
                                if (!Th.tabs || editTab == 3) {
                                Label("Brightness " + (if (brightness > 0) "+" else "") + brightness)
                                Slider(
                                    value = brightness.toFloat(),
                                    onValueChange = { brightness = it.toInt() },
                                    valueRange = -60f..60f,
                                    colors = sliderColors()
                                )
                                Label("Contrast " + contrast + "%")
                                Slider(
                                    value = contrast.toFloat(),
                                    onValueChange = { contrast = it.toInt() },
                                    valueRange = 70f..140f,
                                    colors = sliderColors()
                                )
                                Label("Text on the animation")
                                TextBox(overlayText, "Your name or a short text") { overlayText = it.take(40) }
                                if (overlayText.isNotBlank()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Chip("Top", textPos == 0) { textPos = 0 }
                                        Chip("Middle", textPos == 1) { textPos = 1 }
                                        Chip("Bottom", textPos == 2) { textPos = 2 }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Chip("Small", textSize == 0) { textSize = 0 }
                                        Chip("Medium", textSize == 1) { textSize = 1 }
                                        Chip("Large", textSize == 2) { textSize = 2 }
                                    }
                                }
                                }
                                GhostButton("↺  Reset edits") {
                                    cropW = 100; cropH = 100; panX = 50; panY = 50
                                    brightness = 0; contrast = 100
                                    overlayText = ""; textPos = 2; textSize = 1
                                    coverZoom()?.let { zoom = it }; moveX = 0; moveY = 0; bg = 0xFF000000.toInt()
                                }
                                Hint("Everything here shows on the phone above and goes into the final .qmg.")
                            }

                            GlassCard("Quality", Peach, "4") {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Chip("✨ Best", quality == 0) { quality = 0 }
                                    Chip("⚖️ Balanced", quality == 1) { quality = 1 }
                                    Chip("📦 Small", quality == 2) { quality = 2 }
                                }
                                Toggle("Dithering", "Smoother gradients (adds a little noise, bigger file)", dither) { dither = it }
                                Toggle("Target file size", "Squeeze the animation to about this size", useTarget) { useTarget = it }
                                if (useTarget) {
                                    Label("About " + String.format("%.1f", targetMb) + " MB")
                                    Slider(value = targetMb, onValueChange = { targetMb = it }, valueRange = 0.3f..8f, colors = sliderColors())
                                }
                                Hint("Quality and dithering show up with the Exact button (it runs the real encoder on one frame). " + target.w + "×" + target.h + " · " + Engine.FPS + " fps")
                            }

                            if (!busy) GlowButton("⚡  Convert", enabled = libOk) { startConvert() }
                        }
                    }

                    GlassCard("Your phone", Violet) {
                        Hint("Size used: " + target.w + "×" + target.h + " (" + target.source + "). QMG Studio copies the size of your stock animation so it fits your Samsung exactly.")
                        GhostButton("🔎  Match my phone (uses root)") { detectWithRoot() }
                    }
                }

                /* ================= PREVIEW ================= */
                if (tab == 1) {
                    GlassCard("Result preview", Cyan) {
                        val pf = previewFile
                        if (pf == null) {
                            Hint("Nothing to show yet. Convert a video, or open any .qmg file from the Tools tab.")
                        } else {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                PhoneMock(aspect, bigPhoneH) {
                                    val pb = previewBmp
                                    if (pb != null) {
                                        Image(
                                            bitmap = pb.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                            LinearProgressIndicator(
                                progress = { if (frameTotal > 0) frameNo / frameTotal.toFloat() else 0f },
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)),
                                color = Cyan,
                                trackColor = Tx.copy(alpha = 0.12f)
                            )
                            Label("Frame " + frameNo + " / " + frameTotal + "   ·   " + sizeText(pf.length()))
                            Hint("This is decoded from the .qmg file itself, so it is what the phone will show.")
                            GhostButton(if (playing) "⏸  Pause" else "▶️  Play") { playing = !playing }
                        }
                    }

                    if (previewFile != null) {
                        GlassCard("Check on this phone", Mint) {
                            Hint("Runs this phone's own Samsung decoder on the file and compares the pictures. Experimental: if the app closes, your file is already saved.")
                            if (verifyText.isNotEmpty()) Label(verifyText)
                            GhostButton(if (verifying) "Checking…" else "🔬  Verify with system decoder") { if (!verifying) runVerify() }
                        }
                        GlassCard("Share as GIF", Pink) {
                            Hint("Turns the animation into a GIF (360 px wide) you can post or send.")
                            GhostButton(if (gifBusy) "Working…" else "🎞️  Export GIF") {
                                if (!gifBusy) {
                                    val f = previewFile
                                    val lib = Engine.findLib(ctx)
                                    if (f != null && lib != null) {
                                        gifBusy = true
                                        status = "🎞️ Making GIF…"
                                        scope.launch(Dispatchers.IO) {
                                            var err: String? = null
                                            val where = Engine.saveMain(ctx, clipBase() + "_QmgStudio.gif", "image/gif") { o ->
                                                err = Gif.export(f, lib.absolutePath, o) { d, t -> status = "🎞️ GIF frame $d / $t" }
                                            }
                                            withContext(Dispatchers.Main) {
                                                gifBusy = false
                                                status = if (err != null) "❌ " + err else if (where != null) "🎞️ GIF saved → $where ✔" else "❌ Save failed: " + Engine.lastError
                                            }
                                        }
                                    } else status = "Make an animation first"
                                }
                            }
                        }
                    }
                }

                /* ================= INSTALL ================= */
                if (tab == 2) {
                    GlassCard("Your animations", Cyan, onClick = {
                        if (built.isNotEmpty()) {
                            val boot = built[0]?.file; val shut = built[1]?.file; val dir = target.stockDir
                            saveDirect(clipBase() + "_QmgStudio-module.zip", "application/zip", "📦 Module zip") { o -> Engine.writeModuleZip(boot, boot, shut, dir, o) }
                        } else status = "Make an animation first"
                    }) {
                        for (s in 0..1) {
                            val b = built[s]
                            val name = if (s == 0) "Boot animation" else "Shutdown animation"
                            if (b != null) Label("✔ " + name + " · " + b.frames + " frames · " + sizeText(b.bytes))
                            else Hint("— " + name + ": not made yet")
                        }
                        Hint("Make them in the Create tab. Boot also covers the loop file. Tap this card to save the module .zip at once.")
                    }

                    val any = built.isNotEmpty()
                    GlassCard("Save", Violet) {
                        Hint("Tap a button and the file is saved at once into Download/QmgStudio. Then flash the module .zip in KernelSU / Magisk Manager → Modules → Install from storage. Back up your stock files first (Tools tab).")
                        GhostButton("📦  Save as module (.zip)") {
                            if (any) {
                                val boot = built[0]?.file; val shut = built[1]?.file; val dir = target.stockDir
                                saveDirect(clipBase() + "_QmgStudio-module.zip", "application/zip", "📦 Module zip") { o -> Engine.writeModuleZip(boot, boot, shut, dir, o) }
                            } else status = "Make an animation first"
                        }
                        if (built[0] != null) GhostButton("💾  Save bootsamsung.qmg") {
                            val f = built[0]?.file
                            if (f != null) saveDirect(clipBase() + "_bootsamsung.qmg", "application/octet-stream", "💾 Boot animation") { o -> f.inputStream().use { it.copyTo(o) } }
                        }
                        if (built[1] != null) GhostButton("💾  Save shutdown.qmg") {
                            val f = built[1]?.file
                            if (f != null) saveDirect(clipBase() + "_shutdown.qmg", "application/octet-stream", "💾 Shutdown animation") { o -> f.inputStream().use { it.copyTo(o) } }
                        }
                    }

                    GlassCard("Undo", Pink) {
                        Hint("Puts your phone's original boot animation back. Reboot afterwards.")
                        GhostButton("↩️  Restore original animation") { if (rootState == 1) confirm = "uninstall" else status = "Root is needed: tap the Root chip at the top to ask again" }
                        GhostButton("🔄  Reboot now") { if (rootState == 1) confirm = "reboot" else status = "Root is needed: tap the Root chip at the top to ask again" }
                    }
                }

                /* ================= TOOLS ================= */
                if (tab == 3) {
                    GlassCard("Customize", Pink) {
                        Hint("Pick a look. The whole app changes at once and your choice is remembered.")
                        for (row in Th.all.chunked(2)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                for (pl in row) Chip(pl.name, Th.id == pl.id) { Th.id = pl.id; Th.save(ctx) }
                            }
                        }
                    }

                    GlassCard("Safety", Mint) {
                        Hint("Copies your stock animations (bootsamsung.qmg, shutdown.qmg …) to QmgStudio/backup in your main storage. Uses root if needed.")
                        GhostButton(if (backingUp) "Working…" else "🛟  Backup stock animations") { backup() }
                        Hint("A silent background check watches your module and only notifies you if it is not mounted. Need help? Copy a report and send it.")
                    }

                    GlassCard("Open a .qmg", Cyan) {
                        Hint("Preview any .qmg file, then export it as a GIF.")
                        GhostButton("📂  Choose a .qmg file") { qmgOpener.launch(arrayOf("*/*")) }
                        val bf = Engine.backupFiles(ctx)
                        if (bf.isNotEmpty()) {
                            Label("Your backups")
                            for (f in bf) {
                                GhostButton("▶️  " + f.name + "  (" + sizeText(f.length()) + ")") {
                                    previewFile = f; previewVersion++; playing = true; tab = 1
                                }
                            }
                        }
                    }

                    GlassCard("Decoder library", Violet) {
                        Hint(
                            if (libOk) "Found. The delta table is read from your phone's own Samsung library, nothing from Samsung is bundled."
                            else "Not found on this phone."
                        )
                        GhostButton("Pick libQmageDecoder.so manually") { libPicker.launch("*/*") }
                    }

                    GlassCard("About", Pink) {
                        Text("QMG Studio " + Engine.appVersion(ctx), color = Tx, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Hint("Made by " + Engine.AUTHOR + " · open source (MIT).")
                        Hint("Samsung .qmg boot animations: any size that is a multiple of 4, keyframe + delta frames, verified bit-exact against Samsung's own decoder.")
                        Hint("github.com/xBooM-SL/Qmg-studio")
                        GhostButton("⭐  Open on GitHub") { uriHandler.openUri(Engine.REPO_URL) }
                        GhostButton(if (updChecking) "Checking…" else "🔄  Check for updates") { checkUpdate(true) }
                        GhostButton("⬇️  Latest releases") { uriHandler.openUri(Engine.RELEASES_URL) }
                        GhostButton("🐞  Report a problem") { uriHandler.openUri(Engine.ISSUES_URL) }
                    }
                }

            }

            NavBar(tab) { goTab(it) }
        }

        if (status.isNotEmpty()) Toast(status) { status = "" }

        // a new version: a full-screen page right after opening, with Install now and Skip
        val up = update
        AnimatedVisibility(
            visible = up != null,
            enter = fadeIn(tween(250)) + slideInVertically(tween(350)) { it / 4 },
            exit = fadeOut(tween(250))
        ) {
            if (up != null) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFF070D1A))
                        .pointerInput(Unit) { detectTapGestures { } }
                        .systemBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                ) {
                    Column(
                        Modifier.align(Alignment.Center).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Ic("save", Cyan, 56.dp, 1.4f)
                        Text("New version", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                        val mb = if (up.size > 0) "  ·  " + String.format("%.1f MB", up.size / 1048576f) else ""
                        Text("v" + up.version + " is ready (you have " + Updater.installedVersion(ctx) + ")" + mb, color = Color(0xFFD5D8E6), textAlign = TextAlign.Center)
                        if (up.notes.isNotEmpty() && !updBusy) {
                            Text(up.notes, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.verticalScroll(rememberScrollState()).height(140.dp))
                        }
                        if (updBusy) {
                            LinearProgressIndicator(progress = { updProgress }, modifier = Modifier.fillMaxWidth(), color = Cyan)
                            Text("Downloading… " + (updProgress * 100).toInt() + "%", fontSize = 12.sp, color = Color.White)
                        }
                        GlowButton(if (updBusy) "Downloading…" else "\uD83D\uDE42  Install now", enabled = !updBusy) { startUpdate(up) }
                        if (!updBusy) TextButton(onClick = { update = null }) { IcText("\uD83D\uDE15  Skip", Color.White.copy(alpha = 0.85f), 15f, FontWeight.SemiBold) }
                    }
                }
            }
        }

        // the root check gets at most 500 ms; this full-screen page only covers that moment
        // the root screen comes FIRST. While root is being checked it shows "Checking root"; if root is missing it stays
        // (Try again); as soon as root is granted it fades away and the main screen is there.
        AnimatedVisibility(
            visible = rootState == 0,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(350)) + slideOutVertically(tween(350)) { -it / 4 }
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFF070D1A))
                    .pointerInput(Unit) { detectTapGestures { } }
                    .systemBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 40.dp)
            ) {
                    Column(
                        Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Ic("lock", Cyan, 56.dp, 1.4f)
                        Text(
                            if (rootState == 0) "Getting things ready…" else "Root is needed",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            if (rootState == 0) "Just a moment." else "QMG Studio works only on a rooted phone (KernelSU or Magisk). Open your root manager, allow QMG Studio, then tap the button below.",
                            color = Color(0xFFD5D8E6), fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center
                        )
                        if (rootState == 0) Loader(Cyan, 56.dp)
                        else GlowButton("🔄  Try again") { requestRootUi() }
                    }
            }
        }

        val gp = galPick
        if (gp != null) {
            AlertDialog(
                onDismissRequest = { galPick = null },
                containerColor = Color(0xFF0B1629),
                titleContentColor = Color.White,
                textContentColor = Color(0xFFD5D8E6),
                title = { Text("Install " + gp.title + "?") },
                text = {
                    Text(
                        (if (metaWarn != null) "No metamodule found in KernelSU, so the animation will probably not show until you install one (for example meta-overlayfs).\n\n" else "") +
                            "It replaces the animation made by this app and is used after the next reboot. Make sure you backed up your stock animations (Tools tab)."
                    )
                },
                confirmButton = { TextButton(onClick = { galPick = null; installFromGallery(gp) }) { Text("Install", color = Cyan) } },
                dismissButton = { TextButton(onClick = { galPick = null }) { Text("Cancel", color = Color.White) } }
            )
        }

        // full-screen warning when KernelSU has no metamodule (found right after root is granted); "Skip" is at the bottom
        val mwFull = metaWarn
        if (mwFull != null && !metaSkipped) {
            Dialog(
                onDismissRequest = { },
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false)
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFF070D1A))
                        .padding(horizontal = 24.dp, vertical = 40.dp)
                ) {
                    Column(
                        Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Ic("warn", Cyan, 56.dp, 1.4f)
                        Text(
                            "Metamodule not installed",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Text(mwFull, color = Color(0xFFD5D8E6), fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center)
                        GlowButton(if (metaBusy) "Checking…" else "🔄  Check again") { recheckMeta() }
                    }
                    Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                        GhostButton("Skip") { metaSkipped = true }
                    }
                }
            }
        }

        val c = confirm
        if (c != null) {
            val title = when (c) {
                "install" -> "Install now?"
                "uninstall" -> "Restore the original animation?"
                else -> "Reboot now?"
            }
            val msg = when (c) {
                "install" -> (if (metaWarn != null) "No metamodule found in KernelSU, so the animation will probably not show until you install one (for example meta-overlayfs).\n\n" else "") +
                    "The animation is written into a KernelSU/Magisk module and used after the next reboot. Make sure you backed up your stock animations."
                "uninstall" -> "The QMG Studio module is removed, so your phone's own animation comes back after the next reboot."
                else -> "The phone restarts immediately."
            }
            AlertDialog(
                onDismissRequest = { confirm = null },
                containerColor = Color(0xFF0B1629),
                titleContentColor = Color.White,
                textContentColor = Color(0xFFD5D8E6),
                title = { Text(title) },
                text = { Text(msg) },
                confirmButton = {
                    TextButton(onClick = {
                        confirm = null
                        scope.launch(Dispatchers.IO) {
                            val ok: Boolean
                            val text: String
                            if (c == "install") {
                                ok = Engine.installModuleRoot(ctx, built[0]?.file, built[0]?.file, built[1]?.file, target.stockDir)
                                text = if (ok) "⚡ Installed. Reboot to see it." else "❌ " + Engine.lastError
                            } else if (c == "uninstall") {
                                ok = Engine.uninstallModuleRoot()
                                text = if (ok) "↩️ Original animation restored. Reboot to see it." else "❌ " + Engine.lastError
                            } else {
                                ok = Engine.rebootRoot()
                                text = if (ok) "Rebooting…" else "❌ " + Engine.lastError
                            }
                            withContext(Dispatchers.Main) { status = text }
                        }
                    }) { Text("Continue", color = Cyan) }
                },
                dismissButton = { TextButton(onClick = { confirm = null }) { Text("Cancel", color = Color.White) } }
            )
        }
    }
}
