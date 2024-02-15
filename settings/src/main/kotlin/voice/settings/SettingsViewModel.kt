package voice.settings

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import android.content.Intent
import java.io.File
import java.io.FileOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import de.paulwoitaschek.flowpref.Pref
import voice.common.AppInfoProvider
import voice.common.DARK_THEME_SETTABLE
import voice.common.grid.GridCount
import voice.common.grid.GridMode
import voice.common.navigation.Destination
import voice.common.navigation.Navigator
import voice.common.pref.PrefKeys
import voice.data.repo.internals.AppDb
import javax.inject.Inject
import javax.inject.Named
import androidx.activity.ComponentActivity
import android.content.ContextWrapper
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import android.net.Uri
import androidx.compose.runtime.MutableState
import voice.logging.core.Logger


const val CSV_NEWLINE = "\n"
const val CSV_INDICATOR_START = "{START}"
const val CSV_INDICATOR_END = "{END}"
const val CSV_INDICATOR_TABLE = "{TABLE}"
const val CSV_COMMA_REPLACE = "{COMMA}"

fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> this.baseContext.getActivity()
    else -> null
}

class SettingsViewModel
@Inject constructor(
  @Named(PrefKeys.DARK_THEME)
  private val useDarkTheme: Pref<Boolean>,
  @Named(PrefKeys.AUTO_REWIND_AMOUNT)
  private val autoRewindAmountPref: Pref<Int>,
  @Named(PrefKeys.SEEK_TIME)
  private val seekTimePref: Pref<Int>,
  private val navigator: Navigator,
  private val appInfoProvider: AppInfoProvider,
  @Named(PrefKeys.GRID_MODE)
  private val gridModePref: Pref<GridMode>,
  private val gridCount: GridCount,
  private val appDb: AppDb,
  private val context: Context,
) : SettingsListener {

  private val dialog = mutableStateOf<SettingsViewState.Dialog?>(null)

  // val getContent = registerForActivityResult(GetContent()) { uri: Uri? ->
  //     // Handle the returned Uri
  // }

  @Composable
  fun viewState(): SettingsViewState {
    val useDarkTheme by remember { useDarkTheme.flow }.collectAsState(initial = false)
    val autoRewindAmount by remember { autoRewindAmountPref.flow }.collectAsState(initial = 0)
    val seekTime by remember { seekTimePref.flow }.collectAsState(initial = 0)
    val gridMode by remember { gridModePref.flow }.collectAsState(initial = GridMode.GRID)
    return SettingsViewState(
      useDarkTheme = useDarkTheme,
      showDarkThemePref = DARK_THEME_SETTABLE,
      seekTimeInSeconds = seekTime,
      autoRewindInSeconds = autoRewindAmount,
      dialog = dialog.value,
      appVersion = appInfoProvider.versionName,
      useGrid = when (gridMode) {
        GridMode.LIST -> false
        GridMode.GRID -> true
        GridMode.FOLLOW_DEVICE -> gridCount.useGridAsDefault()
      },
    )
  }

  override fun close() {
    navigator.goBack()
  }

  override fun toggleDarkTheme() {
    useDarkTheme.value = !useDarkTheme.value
  }

  override fun toggleGrid() {
    gridModePref.value = when (gridModePref.value) {
      GridMode.LIST -> GridMode.GRID
      GridMode.GRID -> GridMode.LIST
      GridMode.FOLLOW_DEVICE -> if (gridCount.useGridAsDefault()) {
        GridMode.LIST
      } else {
        GridMode.GRID
      }
    }
  }

  override fun seekAmountChanged(seconds: Int) {
    seekTimePref.value = seconds
  }

  override fun onSeekAmountRowClicked() {
    dialog.value = SettingsViewState.Dialog.SeekTime
  }

  override fun autoRewindAmountChanged(seconds: Int) {
    autoRewindAmountPref.value = seconds
  }

  override fun onAutoRewindRowClicked() {
    dialog.value = SettingsViewState.Dialog.AutoRewindAmount
  }

  override fun dismissDialog() {
    dialog.value = null
  }

  override fun getSupport() {
    navigator.goTo(Destination.Website("https://github.com/PaulWoitaschek/Voice/discussions/categories/q-a"))
  }

  override fun suggestIdea() {
    navigator.goTo(Destination.Website("https://github.com/PaulWoitaschek/Voice/discussions/categories/ideas"))
  }

  override fun export(saveFile: () -> MutableState<Uri?>) {
    // navigator.goTo(Destination.Website("https://github.com/PaulWoitaschek/Voice/discussions/categories/ideas"))
    // I need the  `appDb().getOpenHelper().getReadableDatabase()`
    val suppDb = appDb.openHelper.readableDatabase
    val replaceCommaInData = CSV_COMMA_REPLACE /* commas in the data will be replaced by this */
    val rv = StringBuilder().append(CSV_INDICATOR_START)
    val sql = StringBuilder()
    var afterFirstTable = false
    var afterFirstColumn: Boolean
    var afterFirstRow: Boolean
    var currentTableName: String
    val csr = appDb.query(
      "SELECT name FROM sqlite_master " +
              "WHERE type='table' " +
              "AND name NOT LIKE('sqlite_%') " +
              "AND name NOT LIKE('room_%') " +
              "AND name NOT LIKE('android_%')",
      arrayOf<Any>()
    )

    while (csr.moveToNext()) {
        sql.clear()
        sql.append("SELECT ")
        currentTableName = csr.getString(0)
        if (afterFirstTable) rv.append("$CSV_NEWLINE")
        afterFirstTable = true
        afterFirstColumn = false
        rv.append(CSV_NEWLINE).append("$CSV_INDICATOR_TABLE,$currentTableName")
        for (columnName in getTableColumnNames(currentTableName,suppDb)) {
          if (afterFirstColumn) sql.append("||','||")
          afterFirstColumn = true
          sql.append("replace(`$columnName`,',','$replaceCommaInData')")
        }
        sql.append(" FROM `${currentTableName}`")
        val csr2 = appDb.query(sql.toString(),null)
        afterFirstRow = false
        while (csr2.moveToNext()) {
          if (!afterFirstRow) rv.append("$CSV_NEWLINE")
          afterFirstRow = true
          rv.append(CSV_NEWLINE).append(csr2.getString(0))
        }
    }

    rv.append(CSV_NEWLINE).append("$CSV_INDICATOR_END")
    val csv = rv.toString()

    // val path = context.getFilesDir()
    // val file = File(path, "export.txt")
    // file.writeText(csv)

    // AHGG how do I get the activity my folks

    Logger.w("Calling save file")
    saveFile().value?.let { uri ->
    Logger.w("Saving file $uri")
    val stream = context.contentResolver.openOutputStream(uri)!!
    stream.write(csv.toByteArray())
    stream.close()
      // try {
      //     context.contentResolver.openFileDescriptor(uri, "w")?.use {
      //         FileOutputStream(it.fileDescriptor).use {
      //             it.write(csv.toByteArray())
      //         }
      //     }
      // } catch (e: FileNotFoundException) {
      //     e.printStackTrace()
      // } catch (e: IOException) {
      //     e.printStackTrace()
      // }
    }

    // val getContent = activity.registerForActivityResult(CreateDocument("text/plain")) { uri: Uri? ->
    //     // Handle the returned Uri
    //     // File(uri).writeText(csv)
    //     try {
    //         context.contentResolver.openFileDescriptor(uri!!, "w")?.use {
    //             FileOutputStream(it.fileDescriptor).use {
    //                 it.write(csv.toByteArray())
    //             }
    //         }
    //     } catch (e: FileNotFoundException) {
    //         e.printStackTrace()
    //     } catch (e: IOException) {
    //         e.printStackTrace()
    //     }
    // }

    // getContent.launch("")

      // val uri = FileProvider.getUriForFile(
      //     context,
      //     // BuildConfig.APPLICATION_ID +
      //     "de.ph1b.audiobook.coverprovider",
      //     file
      // )
      // val intent = Intent(Intent.ACTION_SEND)
      // intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      // intent.setType("text/plain")
      // intent.putExtra(Intent.EXTRA_STREAM, uri)
      // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      // context.startActivity(intent)

    //   val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
    //     addCategory(Intent.CATEGORY_OPENABLE)
    //     type = "text/plain"
    //     putExtra(Intent.EXTRA_TITLE, "export.txt")

    //     // Optionally, specify a URI for the directory that should be opened in
    //     // the system file picker before your app creates the document.
    //     // putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
    // }
    // startActivityForResult(intent, CREATE_FILE)

    // fun backupAsCSV() {
    //     val csvFile = File(instance!!.getOpenHelper().writableDatabase.path + THEDATBASE_DATABASE_BACKUP_CSV_SUFFIX)
    //     csvFile.delete()
    //     csvFile.writeText(createAutoCSV())
    // }
  }


  private fun getTableColumnNames(tableName: String, suppDB: SupportSQLiteDatabase): List<String> {
    val rv = arrayListOf<String>()
    val csr = suppDB.query("SELECT name FROM pragma_table_info('${tableName}')",arrayOf<Any>())
    while (csr.moveToNext()) {
        rv.add(csr.getString(0))
    }
    csr.close()
    return rv.toList()
  }

  override fun openBugReport() {
    val url = "https://github.com/PaulWoitaschek/Voice/issues/new".toUri()
      .buildUpon()
      .appendQueryParameter("template", "bug.yml")
      .appendQueryParameter("version", appInfoProvider.versionName)
      .appendQueryParameter("androidversion", Build.VERSION.SDK_INT.toString())
      .appendQueryParameter("device", Build.MODEL)
      .toString()
    navigator.goTo(Destination.Website(url))
  }

  override fun openTranslations() {
    dismissDialog()
    navigator.goTo(Destination.Website("https://hosted.weblate.org/engage/voice/"))
  }
}
