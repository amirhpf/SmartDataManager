package com.smartdatamanager

import android.app.Activity
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.Calendar

class MainActivity : Activity() {

    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createScreen()
    }

    override fun onResume() {
        super.onResume()
        if (::content.isInitialized) {
            showUsage()
        }
    }

    private fun hasUsagePermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE)
                as AppOpsManager

        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )

        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun createScreen() {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(24, 24, 24, 24)
        root.setBackgroundColor(Color.rgb(245, 247, 250))

        val title = TextView(this)
        title.text = "مدیریت هوشمند اینترنت"
        title.textSize = 25f
        title.setTextColor(Color.BLACK)
        title.gravity = Gravity.CENTER
        title.setPadding(8, 24, 8, 24)

        val button = Button(this)
        button.text = "فعال‌سازی دسترسی آمار استفاده"
        button.setOnClickListener {
            startActivity(
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            )
        }

        content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL

        val scroll = ScrollView(this)
        scroll.addView(content)

        root.addView(title)
        root.addView(button)
        root.addView(scroll)

        setContentView(root)
    }

    private fun showUsage() {
        content.removeAllViews()

        if (!hasUsagePermission()) {
            addText(
                "برای نمایش مدت استفاده برنامه‌ها، " +
                "ابتدا دسترسی آمار استفاده را فعال کن.",
                18f
            )
            return
        }

        addText("گزارش استفاده برنامه‌ها - امروز", 21f)

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val start = calendar.timeInMillis
        val end = System.currentTimeMillis()

        val manager = getSystemService(
            Context.USAGE_STATS_SERVICE
        ) as UsageStatsManager

        val stats = manager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            start,
            end
        )

        if (stats.isNullOrEmpty()) {
            addText("هنوز اطلاعاتی برای نمایش وجود ندارد.", 16f)
            return
        }

        val sorted = stats
            .filter { it.totalTimeInForeground > 0 }
            .sortedByDescending { it.totalTimeInForeground }

        for (item in sorted) {
            val name = try {
                val info = packageManager.getApplicationInfo(
                    item.packageName, 0
                )
                packageManager.getApplicationLabel(info).toString()
            } catch (e: Exception) {
                item.packageName
            }

            val minutes = item.totalTimeInForeground / 60000
            val hours = minutes / 60
            val remaining = minutes % 60

            val time = if (hours > 0) {
                "${hours} ساعت و ${remaining} دقیقه"
            } else {
                "${minutes} دقیقه"
            }

            addText("$name\nزمان استفاده: $time", 16f)
        }
    }

    private fun addText(text: String, size: Float) {
        val view = TextView(this)
        view.text = text
        view.textSize = size
        view.setTextColor(Color.DKGRAY)
        view.setPadding(12, 18, 12, 18)
        content.addView(view)
    }
}
