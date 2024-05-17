package com.fibelatti.photowidget.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fibelatti.photowidget.di.PhotoWidgetEntryPoint
import com.fibelatti.photowidget.di.entryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

class PhotoWidgetRescheduleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val isBoot = Intent.ACTION_BOOT_COMPLETED == intent?.action
        val isUpdate = Intent.ACTION_MY_PACKAGE_REPLACED == intent?.action ||
            (Intent.ACTION_PACKAGE_REPLACED == intent?.action &&
                intent.data?.schemeSpecificPart == context?.packageName)

        if (context != null && (isBoot || isUpdate)) {
            Timber.d("isBoot: $isBoot, isUpdate: $isUpdate")

            val ids = PhotoWidgetProvider.ids(context).ifEmpty { return }

            val entryPoint = entryPoint<PhotoWidgetEntryPoint>(context)
            val coroutineScope = entryPoint.coroutineScope()
            val photoWidgetStorage = entryPoint.photoWidgetStorage()
            val photoWidgetAlarmManager = entryPoint.photoWidgetAlarmManager()

            coroutineScope.launch {
                for (id in ids) {
                    val enabled = photoWidgetStorage.getWidgetIntervalEnabled(appWidgetId = id)
                    val interval = photoWidgetStorage.getWidgetInterval(appWidgetId = id)
                    if (enabled) {
                        photoWidgetAlarmManager.setup(
                            appWidgetId = id,
                            repeatInterval = interval.repeatInterval,
                            timeUnit = interval.timeUnit,
                        )
                    }

                    PhotoWidgetProvider.update(context = context, appWidgetId = id)
                }
            }
        }
    }
}
