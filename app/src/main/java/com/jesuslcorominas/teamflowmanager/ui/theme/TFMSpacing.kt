import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object TFMSpacing { // Design tokens Spacing in dp
    val spacing01: Dp = 4.dp

    val spacing02: Dp = 8.dp

    val spacing03: Dp = 12.dp

    val spacing04: Dp = 16.dp

    val spacing05: Dp = 20.dp

    val spacing06: Dp = 24.dp

    val spacing07: Dp = 32.dp

    val spacing08: Dp = 40.dp

    val spacing09: Dp = 48.dp

    val spacing10: Dp = 56.dp

    val spacing11: Dp = 64.dp

    val spacing12: Dp = 72.dp

    val spacing13: Dp = 80.dp

    val spacing14: Dp = 96.dp

    val spacing15: Dp = 104.dp

    val spacing16: Dp = 112.dp
}

val LocalSpacing = staticCompositionLocalOf { TFMSpacing }
