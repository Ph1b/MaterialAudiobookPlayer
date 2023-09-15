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
class Migration27to28
@Inject constructor() : IncrementalMigration(27) {

  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("DROP TABLE IF EXISTS TABLE_BOOK_COPY")
  }
}
