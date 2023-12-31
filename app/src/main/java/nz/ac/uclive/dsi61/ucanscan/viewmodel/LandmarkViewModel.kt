package nz.ac.uclive.dsi61.ucanscan.viewmodel
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import nz.ac.uclive.dsi61.ucanscan.entity.Landmark
import nz.ac.uclive.dsi61.ucanscan.repository.UCanScanRepository

class LandmarkViewModel(private val repository: UCanScanRepository) : ViewModel() {

    // Current index of the list of landmarks - this is how we're tracking at
    // what point of the race we are
    var currentIndex by mutableIntStateOf(0)

    // List of found landmarks
    var foundLandmarks by mutableStateOf<List<Landmark>>(emptyList())

    // Flow of list of landmarks (from RoomDB)
    private val landmarksFlow = repository.landmarks

    // Actual list of landmarks that I can index
    private val _landmarks = MutableStateFlow<List<Landmark>>(emptyList())
    val landmarks: StateFlow<List<Landmark>> = _landmarks

    // The past landmark - used for rendering in the FoundLandmark screen so we can update landmarks there
    var pastLandmark by mutableStateOf<Landmark?>(null)

    // Current landmark (whatever is being currently searched for)
    var currentLandmark by mutableStateOf<Landmark?>(null)

    init {
        // Update list
        viewModelScope.launch {
            landmarksFlow.collect { landmarksList ->
                _landmarks.value = landmarksList
            }
        }
        updateLandmarks()
    }

    // Mark landmark as found, put it in the foundLandmarks list
    fun markLandmarkAsFound(landmark: Landmark) {
        val updatedList = foundLandmarks.toMutableList()
        updatedList.add(landmark)
        foundLandmarks = updatedList
    }

    // Updates current index
    fun updateCurrentIndex(newIndex: Int) {
        currentIndex = newIndex
    }

    // Updates past and current landmarks
    fun updateLandmarks() {
        val landmarkList = landmarks.value
        Log.d("LANDMARK LIST", landmarkList.toString())
        pastLandmark = landmarkList.getOrNull(currentIndex - 1)
        currentLandmark = landmarkList.getOrNull(currentIndex)
    }

    // If the user gives up or for some reason returns to the main menu, indices and landmarks reset
    fun resetLandmarks() {
        currentIndex = 0
        val landmarkList = landmarks.value
        for (landmark in landmarkList) {
            landmark.isFound = false
            CoroutineScope(Dispatchers.IO).launch {
            repository.updateLandmark(landmark)
            }
        }
        pastLandmark = null
        currentLandmark = landmarkList.getOrNull(currentIndex)
        foundLandmarks = emptyList()
    }

}