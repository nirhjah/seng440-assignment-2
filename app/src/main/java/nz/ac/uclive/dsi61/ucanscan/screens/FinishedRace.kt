package nz.ac.uclive.dsi61.ucanscan.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import nz.ac.uclive.dsi61.ucanscan.Constants
import nz.ac.uclive.dsi61.ucanscan.R
import nz.ac.uclive.dsi61.ucanscan.navigation.BottomNavigationBar
import nz.ac.uclive.dsi61.ucanscan.navigation.Screens
import nz.ac.uclive.dsi61.ucanscan.navigation.TopNavigationBar
import nz.ac.uclive.dsi61.ucanscan.viewmodel.IsRaceStartedModel
import nz.ac.uclive.dsi61.ucanscan.viewmodel.LandmarkViewModel
import nz.ac.uclive.dsi61.ucanscan.viewmodel.StopwatchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedMutableState")
@Composable
fun FinishedRaceScreen(context: Context, navController: NavController,
                       stopwatchViewModel: StopwatchViewModel, isRaceStartedModel: IsRaceStartedModel,
                       landmarkViewModel: LandmarkViewModel
) {
    val configuration = LocalConfiguration.current
    val IS_LANDSCAPE = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    stopwatchViewModel.isRunning = false
    isRaceStartedModel.setRaceStarted(false)

    // Media player should play sound when screen is created
    val mMediaPlayer = remember { MediaPlayer.create(context, R.raw.finish_race_sound) }
    val soundPlayed = remember { mutableStateOf(false) }

    if (!soundPlayed.value) {
        mMediaPlayer.start()
        soundPlayed.value = true
    }

    DisposableEffect(Unit) {

        onDispose {mMediaPlayer.release()}
    }

    stopwatchViewModel.startTime = 0L



    Scaffold(
        containerColor = colorResource(R.color.light_green),
        bottomBar = {
            BottomNavigationBar(navController)
        }, content = {
            innerPadding ->

            val isGiveUpDialogOpen = remember { mutableStateOf(false) }
            val isShareDialogOpen = remember { mutableStateOf(false) }


            TopNavigationBar(
                navController = navController,
                stopwatchViewModel = stopwatchViewModel,
                onGiveUpClick = {
                    isGiveUpDialogOpen.value = true
                },
                isRaceStartedModel = isRaceStartedModel,
                landmarkViewModel = landmarkViewModel
            )

            if(IS_LANDSCAPE) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = Constants.TOP_NAVBAR_HEIGHT)
                        .padding(bottom = Constants.BOTTOM_NAVBAR_HEIGHT),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        modifier = Modifier
                            .padding(32.dp)
                            .weight(0.33f)
                    ) {
                        FinishedRaceTitle(IS_LANDSCAPE)
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.33f)
                    ) {
                        FinishedRaceCircle(stopwatchViewModel, IS_LANDSCAPE)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(0.33f),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FinishedRaceButtons(context, navController, stopwatchViewModel, landmarkViewModel, isShareDialogOpen)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = Constants.TOP_NAVBAR_HEIGHT)
                        .padding(bottom = Constants.BOTTOM_NAVBAR_HEIGHT),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FinishedRaceTitle(IS_LANDSCAPE)

                    FinishedRaceCircle(stopwatchViewModel, IS_LANDSCAPE)

                    Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FinishedRaceButtons(context, navController, stopwatchViewModel, landmarkViewModel, isShareDialogOpen)
                    }
                }
            }
        }
    )
}


@Composable
fun FinishedRaceTitle(isLandscape: Boolean) {
    Text(
        text = stringResource(R.string.finished_the_race),
        style = TextStyle(
            fontSize = 28.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        ),
        modifier = Modifier
            .fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(R.string.final_time),
        style = TextStyle(
            fontSize = 28.sp,
            textAlign = TextAlign.Center,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(if(!isLandscape) {30.dp} else {0.dp})
    )
}


@Composable
fun FinishedRaceCircle(stopwatchViewModel: StopwatchViewModel, isLandscape: Boolean) {
    // when in landscape, the size of the circle shrinks, so the position of the text must change
    val textPadding = if(isLandscape) {80.dp} else {120.dp}
    Box(
        modifier = Modifier
            .size(300.dp)
            .background(colorResource(R.color.light_grey), shape = CircleShape)
    ) {
        Text(
            text = convertTimeLongToMinutes(stopwatchViewModel.time),
            fontSize = 48.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = textPadding)
                .align(Alignment.Center),
            style = TextStyle(fontWeight = FontWeight.Bold)
        )
    }
}


@Composable
fun FinishedRaceButtons(context: Context, navController: NavController,
                        stopwatchViewModel: StopwatchViewModel, landmarkViewModel: LandmarkViewModel,
                        isShareDialogOpen: MutableState<Boolean>) {
    Button(
        onClick = {
            landmarkViewModel.resetLandmarks()
            navController.navigate(Screens.MainMenu.route)
        },
        modifier = Modifier.size(width = 200.dp, height = Constants.MEDIUM_BTN_HEIGHT)
    ) {
        Text(
            text = stringResource(R.string.back_to_home),
            fontSize = 20.sp
        )
    }

    Button(
        modifier = Modifier
            .size(Constants.MEDIUM_BTN_HEIGHT),
        shape = RoundedCornerShape(16.dp),
        onClick = {
            isShareDialogOpen.value = true
        },
    ) {
        Icon(
            painter = painterResource(id = R.drawable.share),
            contentDescription = "Share",
            modifier = Modifier
                .size(100.dp)
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
                                    dispatchAction(context, option, convertTimeLongToMinutes(stopwatchViewModel.time),
                                        "finished-race")
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


fun dispatchAction(context: Context, option: String, timeOrLandmark: String, case: String) {
    // we manually get the strings from the string resource IDs because
    // using stringResource() to do it would require this function to be composable
    val email = context.resources.getString(R.string.share_via_email) // get string value given resource id
    val text = context.resources.getString(R.string.share_via_text)
    val call = context.resources.getString(R.string.share_via_phonecall)
    var shareTitleString = ""
    var shareBodyPt1String = ""
    var shareBodyPt2String = ""

    when (case) {
        "finished-race" -> {
            shareTitleString = context.resources.getString(R.string.share_finished_race_email_subject)
            shareBodyPt1String = context.resources.getString(R.string.share_finished_race_msg_pt1)
            shareBodyPt2String = context . resources . getString (R.string.share_finished_race_msg_pt2)
        }
        "leaderboard" -> {
            shareTitleString = context.resources.getString(R.string.share_leaderboard_stat_email_subject)
            shareBodyPt1String = context.resources.getString(R.string.share_leaderboard_stat_pt1)
            shareBodyPt2String = context . resources . getString (R.string.share_leaderboard_stat_pt2)
        }
        "landmark" -> {
            shareTitleString = context.resources.getString(R.string.share_found_landmark_email_subject)
            shareBodyPt1String = context.resources.getString(R.string.share_found_landmark_msg_pt1)
            shareBodyPt2String = context.resources.getString(R.string.share_found_landmark_msg_pt2)
        }
    }

    when (option) {
        email -> {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, shareTitleString)
            intent.putExtra(Intent.EXTRA_TEXT, shareBodyPt1String + timeOrLandmark + shareBodyPt2String)
            ContextCompat.startActivity(context, intent, null)
        }

        text -> {
            val uri = Uri.parse("smsto:")
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            intent.putExtra("sms_body", shareBodyPt1String + timeOrLandmark + shareBodyPt2String)
            ContextCompat.startActivity(context, intent, null)
        }

        call -> {
            val uri = Uri.parse("tel:")
            val intent = Intent(Intent.ACTION_DIAL, uri)
            ContextCompat.startActivity(context, intent, null)
        }
    }
}