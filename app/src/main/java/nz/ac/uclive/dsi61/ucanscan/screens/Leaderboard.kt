package nz.ac.uclive.dsi61.ucanscan.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import nz.ac.uclive.dsi61.ucanscan.entity.Landmark
import nz.ac.uclive.dsi61.ucanscan.navigation.BottomNavigationBar
import nz.ac.uclive.dsi61.ucanscan.navigation.TopNavigationBar
import nz.ac.uclive.dsi61.ucanscan.viewmodel.IsRaceStartedModel
import nz.ac.uclive.dsi61.ucanscan.viewmodel.LandmarkViewModel
import nz.ac.uclive.dsi61.ucanscan.viewmodel.StopwatchViewModel

// This is a temporary Screen for a Leaderboard (for navigation purposes)
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedMutableState")
@Composable
fun LeaderboardScreen(context: Context,
                    navController: NavController, stopwatchViewModel : StopwatchViewModel, isRaceStartedModel : IsRaceStartedModel,
                      landmarkViewModel: LandmarkViewModel
) {

    val landmarks by landmarkViewModel.landmarks.collectAsState(initial = emptyList<Landmark>())
    // Here is how we can get stuff from our DB to display on a screen - ideally we have
    // different viewModels depending on the logic we are working with. I made the LandmarkViewModel for now

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
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {

                Button(
                    onClick = {
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = landmarkViewModel.landmarks.toString()
                    )
                }
            }

            BackToRaceOrHomeButtonContainer(navController, innerPadding, isRaceStartedModel.isRaceStarted)

        }
    )

    BackHandler {
        // user has a back to race button so doesn't need going back with system
    }

}
