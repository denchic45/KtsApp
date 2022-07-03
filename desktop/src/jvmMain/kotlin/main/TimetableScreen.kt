package main

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import main.theme.Blue
import main.theme.Typography

@Preview
@OptIn(ExperimentalUnitApi::class)
@Composable
fun TimetableScreen() {
    MediumTopAppBar(
        title = { Text("Июль", fontSize = TextUnit(32F, TextUnitType.Sp)) },
        modifier = Modifier.heightIn(min = 112.dp),
        actions = {
            IconButton(
                modifier = Modifier,
                onClick = {}
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "back arrow icon"
                )
            }
        }
    )
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxSize().padding(end = 24.dp),
        elevation = 0.dp
    ) {
        Timetable()
    }
}

@OptIn(ExperimentalUnitApi::class)
@Composable
@Preview
fun LessonCell() {
    Column(
        modifier = Modifier.widthIn(min = 196.dp).padding(18.dp)
    ) {
        Icon(
            painterResource("icons/ic_basketball.xml"),
            null,
            modifier = Modifier.size(28.dp),
            tint = Blue
        )
        Text(
            "Физкультура",
            modifier = Modifier.padding(top = 8.dp),
            style = Typography.titleLarge
        )
        Text(
            "2-й корпус",
            modifier = Modifier.padding(top = 4.dp),
            fontSize = TextUnit(18F, TextUnitType.Sp),
            fontFamily = FontFamily(
                Font(
                    resource = "fonts/Gilroy-Medium.ttf"
                )
            )
        )
    }
}

@Composable
@Preview
fun DayOfWeekCell() {
    Column(
        modifier = Modifier.widthIn(min = 196.dp).height(112.dp).padding(top = 24.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "ПН",
            style = Typography.bodyLarge
        )
        Text(
            "27",
            modifier = Modifier.padding(top = 18.dp),
            style = Typography.headlineMedium
        )
    }
}

@Composable
@Preview
fun RowScope.LessonsOfDay(modifier: Modifier = Modifier) {
    Column(modifier = Modifier.fillMaxWidth().weight(1F)) {
        DayOfWeekCell()
        repeat(8) {
            LessonCell()
        }
    }
}

@Composable
@Preview
fun Timetable() {
    Row(Modifier.fillMaxWidth()) {
        repeat(6) {
            LessonsOfDay(modifier = Modifier.weight(1f))
        }
    }
}