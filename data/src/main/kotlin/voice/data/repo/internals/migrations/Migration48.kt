package voice.data.repo.internals.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.squareup.anvil.annotations.ContributesMultibinding
import javax.inject.Inject
import voice.common.AppScope

@ContributesMultibinding(
  scope = AppScope::class,
  boundType = Migration::class,
)
class Migration48
@Inject constructor() : IncrementalMigration(48) {

  override fun migrate(db: SupportSQLiteDatabase) {
    // there was a bug a in the chapter parsing, trigger a scan.
    val lastModifiedCv = ContentValues().apply {
      put("fileLastModified", 0)
    }
    db.update("chapters", SQLiteDatabase.CONFLICT_FAIL, lastModifiedCv, null, null)
  }
}
