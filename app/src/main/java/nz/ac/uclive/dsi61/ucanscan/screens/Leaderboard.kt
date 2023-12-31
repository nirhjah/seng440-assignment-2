package nz.ac.uclive.dsi61.ucanscan.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import nz.ac.uclive.dsi61.ucanscan.Constants
import nz.ac.uclive.dsi61.ucanscan.LandmarkSaver
import nz.ac.uclive.dsi61.ucanscan.R
import nz.ac.uclive.dsi61.ucanscan.UCanScanApplication
import nz.ac.uclive.dsi61.ucanscan.entity.Landmark
import nz.ac.uclive.dsi61.ucanscan.entity.Times
import nz.ac.uclive.dsi61.ucanscan.navigation.BottomNavigationBar
import nz.ac.uclive.dsi61.ucanscan.navigation.TopNavigationBar
import nz.ac.uclive.dsi61.ucanscan.viewmodel.FinishedRaceViewModel
import nz.ac.uclive.dsi61.ucanscan.viewmodel.IsRaceStartedModel
import nz.ac.uclive.dsi61.ucanscan.viewmodel.LandmarkViewModel
import nz.ac.uclive.dsi61.ucanscan.viewmodel.StopwatchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedMutableState")
@Composable
fun LeaderboardScreen(context: Context,
                    navController: NavController, stopwatchViewModel : StopwatchViewModel, isRaceStartedModel : IsRaceStartedModel,
                      landmarkViewModel: LandmarkViewModel
) {
    val configuration = LocalConfiguration.current
    val IS_LANDSCAPE = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val application = context.applicationContext as UCanScanApplication
    val finishedRaceViewModel: FinishedRaceViewModel = remember {
        FinishedRaceViewModel(repository = application.repository)
    }

    val isRaceStarted by isRaceStartedModel.isRaceStarted
    val allTimes by finishedRaceViewModel.allTimes.collectAsState(emptyList())

    val currentLandmark = rememberSaveable(saver = LandmarkSaver()) {
        landmarkViewModel.currentLandmark ?: Landmark("", "", 0.0, 0.0, false)
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }, content = {

                innerPadding ->

            val openDialog = remember { mutableStateOf(false)  }

            TopNavigationBar(
                navController = navController,
                stopwatchViewModel = stopwatchViewModel,
                onGiveUpClick = {
                    openDialog.value = true
                },
                isRaceStartedModel = isRaceStartedModel,
                landmarkViewModel = landmarkViewModel
            )

            StopwatchIncrementFunctionality(stopwatchViewModel)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize().padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = if (isRaceStarted) 70.dp else 30.dp,
                    bottom = 16.dp)
            ) {
                Text(text = stringResource(id = R.string.personal_bests),
                    fontSize = 24.sp)


                TimesDisplay(allTimes = allTimes, context, stopwatchViewModel, IS_LANDSCAPE)


            }

            BackToRaceOrHomeButtonContainer(navController, innerPadding, isRaceStartedModel.isRaceStarted, landmarkViewModel, IS_LANDSCAPE, currentLandmark)

        }
    )

    BackHandler {
        // user has a back to race button so doesn't need going back with system
    }

}


@Composable
fun TimesDisplay(allTimes: List<Times>, context:Context, stopwatchViewModel: StopwatchViewModel, isLandscape: Boolean) {
    val isShareDialogOpen = remember { mutableStateOf(false) }

    LazyColumn (
        modifier = Modifier
            .padding(top = 19.dp)
            .padding(bottom = if(isLandscape) {0.dp} else {Constants.MEDIUM_BTN_HEIGHT} + Constants.BOTTOM_NAVBAR_HEIGHT)
    ) {
        itemsIndexed(allTimes) { index, time ->

            val medalImage = when (index) {
                0 -> R.drawable.medal_first
                1 -> R.drawable.medal_second
                2 -> R.drawable.medal_third
                else -> R.drawable.medal_all
            }

            Row(modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    painter = painterResource(id = medalImage),
                    contentDescription = "Camera",
                    modifier = Modifier
                        .size(50.dp)
                )

                Text(text = "${time.dateAchieved}          ${convertTimeLongToMinutes(time.endTime)}",
                    modifier = Modifier.padding(top = 14.dp))

                IconButton(onClick = { isShareDialogOpen.value = true
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.share),
                        contentDescription = "Share"
                    )
                }

                if (isShareDialogOpen.value) {
                    AlertDialog(
                        title = {
                            Text(
                                fontWeight = FontWeight.Bold,
                                text = stringResource(R.string.share_dialog_title)
                            )
                        },
                        text = {
                            val options = listOf(stringResource(R.string.share_via_email), stringResource(R.string.share_via_text), stringResource(R.string.share_via_phonecall))
                            LazyColumn {
                                items(options) { option ->
                                    Text(
                                        modifier = Modifier
                                            .clickable {
                                                isShareDialogOpen.value = false
                                                dispatchAction(context, option, convertTimeLongToMinutes(time.endTime) + " on " + time.dateAchieved, "leaderboard")
                                            }
                                            .padding(vertical = 16.dp),
                                        style = TextStyle(fontSize = 18.sp),
                                        text = option
                                    )
                                }
                            }
                        },
                        onDismissRequest = { isShareDialogOpen.value = false },
                        confirmButton  = {},
                        dismissButton = {}
                    )
                }

            }
        }
    }
}


fun convertTimeLongToMinutes(time: Long): String {
    val seconds = time / 1000
    val minutes = seconds / 60
    val actualSeconds = seconds % 60
    val hours = minutes / 60
    val actualMinutes = minutes % 60

    return "%02d:%02d:%02d".format(hours, actualMinutes, actualSeconds)
}