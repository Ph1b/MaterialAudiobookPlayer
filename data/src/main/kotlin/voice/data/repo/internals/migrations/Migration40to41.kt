package voice.data.repo.internals.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.squareup.anvil.annotations.ContributesMultibinding
import javax.inject.Inject
import voice.common.AppScope

@ContributesMultibinding(
  scope = AppScope::class,
  boundType = Migration::class,
)
class Migration40to41
@Inject constructor() : IncrementalMigration(40) {

  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE tableBooks ADD loudnessGain INTEGER")
  }
}
