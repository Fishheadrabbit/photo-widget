package com.fibelatti.photowidget.widget

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fibelatti.photowidget.R
import com.fibelatti.photowidget.configure.appWidgetId
import com.fibelatti.photowidget.model.PhotoWidgetSource
import com.fibelatti.photowidget.platform.KeepAliveService
import com.fibelatti.photowidget.widget.data.PhotoWidgetStorage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * This could have been a `BroadcastReceiver` but the action would be quite confusing without any
 * sort of user feedback. This transparent activity does the work and shows a toast at the end to
 * confirm.
 */
@AndroidEntryPoint
class ToggleCyclingFeedbackActivity : AppCompatActivity() {

    @Inject
    lateinit var photoWidgetStorage: PhotoWidgetStorage

    @Inject
    lateinit var photoWidgetAlarmManager: PhotoWidgetAlarmManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId: Int = intent.appWidgetId

        if (photoWidgetStorage.getWidgetLockedInApp(appWidgetId = appWidgetId)) {
            finish()
            return
        }

        val paused: Boolean = photoWidgetStorage.getWidgetCyclePaused(appWidgetId = appWidgetId)
        val source: PhotoWidgetSource = photoWidgetStorage.getWidgetSource(appWidgetId = appWidgetId)

        when {
            source == PhotoWidgetSource.GIF && paused -> {
                KeepAliveService.sendResumeGifBroadcast(context = this, appWidgetId = appWidgetId)
            }

            source == PhotoWidgetSource.GIF -> {
                KeepAliveService.sendPauseGifBroadcast(context = this, appWidgetId = appWidgetId)
            }

            paused -> {
                photoWidgetAlarmManager.setup(appWidgetId = appWidgetId)
            }

            else -> {
                photoWidgetAlarmManager.cancel(appWidgetId = appWidgetId)
            }
        }

        photoWidgetStorage.saveWidgetCyclePaused(appWidgetId = appWidgetId, value = !paused)

        PhotoWidgetProvider.update(
            context = this,
            appWidgetId = appWidgetId,
        )

        Toast.makeText(
            /* context = */ this,
            /* resId = */
            if (paused) {
                R.string.photo_widget_cycling_feedback_resumed
            } else {
                R.string.photo_widget_cycling_feedback_paused
            },
            /* duration = */ Toast.LENGTH_SHORT,
        ).show()

        finish()
    }
}
