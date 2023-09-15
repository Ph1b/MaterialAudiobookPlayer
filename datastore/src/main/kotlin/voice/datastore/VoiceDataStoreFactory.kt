package voice.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import javax.inject.Inject
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class VoiceDataStoreFactory
@Inject constructor(
  private val json: Json,
  private val context: Context,
) {

  fun <T> create(
    serializer: KSerializer<T>,
    defaultValue: T,
    fileName: String,
  ): DataStore<T> {
    return DataStoreFactory.create(
      serializer = KotlinxDataStoreSerializer(
        defaultValue = defaultValue,
        json = json,
        serializer = serializer,
      ),
    ) {
      context.dataStoreFile(fileName)
    }
  }

  fun int(
    fileName: String,
    defaultValue: Int,
  ): DataStore<Int> {
    return create(
      serializer = Int.serializer(),
      defaultValue = defaultValue,
      fileName = fileName,
    )
  }

  fun boolean(
    fileName: String,
    defaultValue: Boolean,
  ): DataStore<Boolean> {
    return create(
      serializer = Boolean.serializer(),
      defaultValue = defaultValue,
      fileName = fileName,
    )
  }
}
