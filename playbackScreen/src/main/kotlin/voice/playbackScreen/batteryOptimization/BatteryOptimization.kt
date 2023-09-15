package voice.playbackScreen.batteryOptimization

import android.os.Build
import androidx.datastore.core.DataStore
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class BatteryOptimization
@Inject constructor(
  private val isIgnoringBatteryOptimizations: IsIgnoringBatteryOptimizations,
  private val amountOfBatteryOptimizationsRequested: DataStore<Int>,
) {

  suspend fun shouldRequest(): Boolean = when {
    isIgnoringBatteryOptimizations() -> false
    Build.MANUFACTURER.equals("Google", ignoreCase = true) -> false
    else -> amountOfBatteryOptimizationsRequested.data.first() <= 3
  }

  suspend fun onBatteryOptimizationsRequested() {
    amountOfBatteryOptimizationsRequested.updateData { it + 1 }
  }
}
