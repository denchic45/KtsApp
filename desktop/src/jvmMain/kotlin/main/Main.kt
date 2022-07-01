import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import main.MainContent
import main.theme.KtsTheme

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = WindowState(size = DpSize(1296.dp, 878.dp))
    ) {
        KtsTheme {
            MainContent()
        }
    }
}