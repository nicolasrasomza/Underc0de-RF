package org.underc0de.underc0derf.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.underc0de.underc0derf.R

/*
 * Identidad visual según el Manual de Marca de Underc0de.
 *
 * Paleta principal: azul #212349 y celestes #3CB7EA / #3DA4DD / #4594D1.
 * Acentos: #2EF2E8 y #7B5CFF. Underc0de Day: magenta #F9226C y violeta #914EF5.
 */

// ---- Paleta principal ----
/** Azul institucional. Fondo general de la app (uso predominante). */
val BrandBlue = Color(0xFF212349)
val BrandCeleste = Color(0xFF3CB7EA)
val BrandCeleste2 = Color(0xFF3DA4DD)
val BrandCeleste3 = Color(0xFF4594D1)

// ---- Acentos ----
val BrandAqua = Color(0xFF2EF2E8)
val BrandIndigo = Color(0xFF7B5CFF)
val BrandMagenta = Color(0xFFF9226C)
val BrandViolet = Color(0xFF914EF5)

// ---- Neutros ----
val BrandWhite = Color(0xFFFFFFFF)
/** Fondo general: negro puro (estética terminal). */
val BrandBlack = Color(0xFF000000)
/** Paneles/tarjetas: azul institucional muy oscurecido. */
val BrandPanel = Color(0xFF0C0E1D)
/** Borde sutil de paneles y botones secundarios deshabilitados. */
val BrandBorder = Color(0xFF162A3D)
/** Texto secundario / deshabilitado (blanco atenuado sobre azul). */
val BrandMuted = Color(0xFFA9ADD3)

// ---- Roles semánticos usados por las pantallas ----
val BrandBackground = BrandBlack
val BrandSurface = BrandPanel
val BrandPrimary = BrandCeleste      // acciones principales, títulos de pantalla
val BrandSuccess = BrandAqua         // transmitiendo / escuchando / OK
val BrandAlert = BrandMagenta        // SOS, errores, llamados a la acción críticos
val BrandWarning = BrandViolet       // avisos (bordes y etiquetas)
val BrandText = BrandWhite

/**
 * Tipografías del manual:
 *  - Usuzi Regular (principal): logotipo, títulos y encabezados. Nunca textos extensos.
 *  - Univia Pro (secundaria): subtítulos, bajadas, frases destacadas y botones.
 *    Es de Adobe Fonts y su licencia no permite empaquetarla en una app, así que es
 *    OPCIONAL: si se agregan `res/font/univia_pro_medium.ttf` y `univia_pro_bold.ttf`
 *    (con una licencia que lo permita) se usan solas; si no, se usa Roboto.
 *  - Roboto (secundaria): cuerpos de texto.
 *  - Pixelify Sans Medium (secundaria): SOLO acentos gráficos puntuales.
 *
 * Ver FUENTES.md en la raíz del proyecto.
 */
object BrandFonts {
    val Body = FontFamily(
        Font(R.font.roboto_regular, FontWeight.Normal),
        Font(R.font.roboto_medium, FontWeight.Medium),
        Font(R.font.roboto_bold, FontWeight.Bold)
    )

    val Accent = FontFamily(
        Font(R.font.pixelify_sans_medium, FontWeight.Medium)
    )

    /** Usuzi Regular: logo, títulos y encabezados. */
    val Title = FontFamily(
        Font(R.font.usuzi, FontWeight.Normal)
    )

    /** Subtítulos y botones: Univia Pro si está disponible, si no Roboto. Se resuelve en [init]. */
    var Subtitle: FontFamily = Body
        private set

    /** true si Univia Pro está empaquetada en res/font. */
    var hasUnivia: Boolean = false
        private set

    @SuppressLint("DiscouragedApi")
    fun init(context: Context) {
        val res = context.resources
        val medium = res.getIdentifier("univia_pro_medium", "font", context.packageName)
        val bold = res.getIdentifier("univia_pro_bold", "font", context.packageName)
        if (medium != 0 || bold != 0) {
            val fonts = buildList {
                if (medium != 0) add(Font(medium, FontWeight.Medium))
                if (bold != 0) add(Font(bold, FontWeight.Bold))
            }
            Subtitle = FontFamily(fonts)
            hasUnivia = true
        }
    }
}

private fun brandTypography(): Typography {
    val base = Typography()
    fun TextStyle.title() = copy(fontFamily = BrandFonts.Title, fontWeight = FontWeight.Normal)
    fun TextStyle.body() = copy(fontFamily = BrandFonts.Body)
    fun TextStyle.subtitle() = copy(fontFamily = BrandFonts.Subtitle)
    return Typography(
        displayLarge = base.displayLarge.title(),
        displayMedium = base.displayMedium.title(),
        displaySmall = base.displaySmall.title(),
        headlineLarge = base.headlineLarge.title(),
        headlineMedium = base.headlineMedium.title(),
        headlineSmall = base.headlineSmall.title(),
        titleLarge = base.titleLarge.subtitle(),
        titleMedium = base.titleMedium.subtitle(),
        titleSmall = base.titleSmall.subtitle(),
        bodyLarge = base.bodyLarge.body(),
        bodyMedium = base.bodyMedium.body(),
        bodySmall = base.bodySmall.body(),
        labelLarge = base.labelLarge.subtitle(),
        labelMedium = base.labelMedium.body(),
        labelSmall = base.labelSmall.body()
    )
}

private val BrandColors = darkColorScheme(
    primary = BrandPrimary,
    onPrimary = BrandBackground,
    secondary = BrandCeleste3,
    onSecondary = BrandWhite,
    tertiary = BrandAqua,
    onTertiary = BrandBackground,
    background = BrandBackground,
    onBackground = BrandText,
    surface = BrandSurface,
    onSurface = BrandText,
    surfaceVariant = BrandSurface,
    onSurfaceVariant = BrandMuted,
    error = BrandAlert,
    onError = BrandWhite,
    outline = BrandBorder
)

/**
 * Forma de los controles: esquinas superior-izquierda e inferior-derecha cortadas en diagonal
 * (bloques/contenedores del sistema gráfico de la marca).
 */
val BrandButtonShape = CutCornerShape(topStart = 9.dp, bottomEnd = 9.dp)
val BrandCardShape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp)

@Composable
fun Underc0deTheme(content: @Composable () -> Unit) {
    val typography = remember { brandTypography() }
    MaterialTheme(colorScheme = BrandColors, typography = typography, content = content)
}
