package voice.settings.views

import AutoSleepTimerSetting
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.squareup.anvil.annotations.ContributesTo
import voice.common.AppScope
import voice.common.compose.VoiceTheme
import voice.common.compose.rememberScoped
import voice.common.rootComponentAs
import voice.settings.SettingsListener
import voice.settings.SettingsViewModel
import voice.settings.SettingsViewState
import voice.strings.R as StringsR

@Composable
@Preview
private fun SettingsPreview() {
  val viewState = SettingsViewState(
    useDarkTheme = false,
    showDarkThemePref = true,
    seekTimeInSeconds = 42,
    autoRewindInSeconds = 12,
    dialog = null,
    appVersion = "1.2.3",
    useGrid = true,
    autoSleepTimer = false,
    autoSleepTimeEnd = "",
    autoSleepTimeStart = "",
  )
  VoiceTheme {
    Settings(
      viewState,
      object : SettingsListener {
        override fun close() {}
        override fun toggleDarkTheme() {}
        override fun seekAmountChanged(seconds: Int) {}
        override fun onSeekAmountRowClick() {}
        override fun autoRewindAmountChang(seconds: Int) {}
        override fun onAutoRewindRowClick() {}
        override fun dismissDialog() {}
        override fun openTranslations() {}
        override fun getSupport() {}
        override fun suggestIdea() {}
        override fun openBugReport() {}
        override fun toggleGrid() {}
        override fun toggleAutoSleepTimer(checked: Boolean) {}
        override fun setAutoSleepTimerStart(
          hour: Int,
          minute: Int,
        ) {
        }

        override fun setAutoSleepTimerEnd(
          hour: Int,
          minute: Int,
        ) {
        }
      },
    )
  }
}

@Composable
private fun Settings(
  viewState: SettingsViewState,
  listener: SettingsListener,
) {
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
          Text(stringResource(StringsR.string.action_settings))
        },
        navigationIcon = {
          IconButton(
            onClick = {
              listener.close()
            },
          ) {
            Icon(
              imageVector = Icons.Outlined.Close,
              contentDescription = stringResource(StringsR.string.close),
            )
          }
        },
      )
    },
  ) { contentPadding ->
    Box(Modifier.padding(contentPadding)) {
      Column(
        Modifier
          .verticalScroll(rememberScrollState())
          .nestedScroll(scrollBehavior.nestedScrollConnection)
          .padding(vertical = 8.dp),
      ) {
        if (viewState.showDarkThemePref) {
          DarkThemeRow(viewState.useDarkTheme, listener::toggleDarkTheme)
        }
        ListItem(
          modifier = Modifier.clickable { listener.toggleGrid() },
          leadingContent = {
            val imageVector = if (viewState.useGrid) {
              Icons.Outlined.GridView
            } else {
              Icons.AutoMirrored.Outlined.ViewList
            }
            Icon(imageVector, stringResource(StringsR.string.pref_use_grid))
          },
          headlineContent = { Text(stringResource(StringsR.string.pref_use_grid)) },
          trailingContent = {
            Switch(
              checked = viewState.useGrid,
              onCheckedChange = {
                listener.toggleGrid()
              },
            )
          },
        )
        SeekTimeRow(viewState.seekTimeInSeconds) {
          listener.onSeekAmountRowClick()
        }
        AutoRewindRow(viewState.autoRewindInSeconds) {
          listener.onAutoRewindRowClick()
        }
        AutoSleepTimerRow(
          viewState.autoSleepTimer,
          listener::toggleAutoSleepTimer,
        )
        ListItem(
          leadingContent = { Spacer(Modifier.size(24.dp)) },
          headlineContent = {
            Row(
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              AutoSleepTimerSetting(
                viewState.autoSleepTimer,
                viewState.autoSleepTimeStart,
                stringResource(id = StringsR.string.auto_sleep_timer_start),
                listener::setAutoSleepTimerStart,
              )
              Spacer(Modifier.weight(weight = 1f, fill = true))
              AutoSleepTimerSetting(
                viewState.autoSleepTimer,
                viewState.autoSleepTimeEnd,
                stringResource(id = StringsR.string.auto_sleep_timer_end),
                listener::setAutoSleepTimerEnd,
              )
            }
          },
        )
        ListItem(
          modifier = Modifier.clickable { listener.suggestIdea() },
          leadingContent = { Icon(Icons.Outlined.Lightbulb, stringResource(StringsR.string.pref_suggest_idea)) },
          headlineContent = { Text(stringResource(StringsR.string.pref_suggest_idea)) },
        )
        ListItem(
          modifier = Modifier.clickable { listener.getSupport() },
          leadingContent = { Icon(Icons.AutoMirrored.Outlined.HelpOutline, stringResource(StringsR.string.pref_get_support)) },
          headlineContent = { Text(stringResource(StringsR.string.pref_get_support)) },
        )
        ListItem(
          modifier = Modifier.clickable { listener.openBugReport() },
          leadingContent = { Icon(Icons.Outlined.BugReport, stringResource(StringsR.string.pref_report_issue)) },
          headlineContent = { Text(stringResource(StringsR.string.pref_report_issue)) },
        )
        ListItem(
          modifier = Modifier.clickable { listener.openTranslations() },
          leadingContent = { Icon(Icons.Outlined.Language, stringResource(StringsR.string.pref_help_translating)) },
          headlineContent = { Text(stringResource(StringsR.string.pref_help_translating)) },
        )
        AppVersion(appVersion = viewState.appVersion)
        Dialog(viewState, listener)
      }
    }
  }
}

@ContributesTo(AppScope::class)
interface SettingsComponent {
  val settingsViewModel: SettingsViewModel
}

@Composable
fun Settings() {
  val viewModel = rememberScoped { rootComponentAs<SettingsComponent>().settingsViewModel }
  val viewState = viewModel.viewState()
  Settings(viewState, viewModel)
}

@Composable
private fun Dialog(
  viewState: SettingsViewState,
  listener: SettingsListener,
) {
  val dialog = viewState.dialog ?: return
  when (dialog) {
    SettingsViewState.Dialog.AutoRewindAmount -> {
      AutoRewindAmountDialog(
        currentSeconds = viewState.autoRewindInSeconds,
        onSecondsConfirm = listener::autoRewindAmountChang,
        onDismiss = listener::dismissDialog,
      )
    }
    SettingsViewState.Dialog.SeekTime -> {
      SeekAmountDialog(
        currentSeconds = viewState.seekTimeInSeconds,
        onSecondsConfirm = listener::seekAmountChanged,
        onDismiss = listener::dismissDialog,
      )
    }
  }
}
