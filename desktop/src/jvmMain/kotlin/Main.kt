import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.denchic45.kts.UserEntity
import com.denchic45.kts.data.local.db.DbHelper
import com.denchic45.kts.data.local.db.DriverFactory
import com.denchic45.kts.data.pref.core.FilePreferencesFactory
import com.denchic45.kts.main.MainContent
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.denchic45.kts.main.theme.KtsTheme

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = WindowState(size = DpSize(1296.dp, 878.dp))
    ) {

        var users by remember { mutableStateOf<List<String>>(emptyList()) }

        KtsTheme {
            MainContent()
//            users.forEach {
//                Text(it)
//            }
        }

        GlobalScope.launch {
            delay(500)
            FilePreferencesFactory("AppPrefs").systemRoot().putFloat("Float",2F)
            DriverFactory().driver.apply {
                DbHelper(this).database.apply {
                    userEntityQueries.upsert(
                        UserEntity(
                            user_id = "1",
                            first_name = "Ivan",
                            surname = "Ivanoov",
                            patronymic = null,
                            user_group_id = "",
                            role = "STUDENT",
                            email = null,
                            photo_url = "",
                            gender = 1,
                            admin = false,
                            generated_avatar = true,
                            timestamp = 0
                        )
                    )
                    userEntityQueries
                        .getAll()
                        .asFlow()
                        .mapToList()
                        .collect { users = it.map { "User: ${it.first_name + it.surname}" } }
                }
            }
        }
    }
}