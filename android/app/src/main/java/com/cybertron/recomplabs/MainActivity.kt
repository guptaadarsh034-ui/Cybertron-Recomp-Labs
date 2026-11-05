package com.cybertron.recomplabs

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private external fun runSelfTest(): String

    companion object {
        init { System.loadLibrary("ppc_runtime") }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = TextView(this).apply {
            text = "CYBERTRON RECOMP LABS"; textSize = 22f; gravity = Gravity.CENTER
        }
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val appVersion = packageInfo.versionName ?: "unknown"
        val appVersionCode = packageInfo.versionCode
        val subtitle = TextView(this).apply {
            text = "Milestone 2 • Android ARM64 runtime prototype\nVersion $appVersion ($appVersionCode)"
            textSize = 14f; gravity = Gravity.CENTER
        }
        val output = TextView(this).apply {
            text = "Ready. Run the synthetic PowerPC test."
            textSize = 16f; setPadding(16, 24, 16, 24)
        }
        val runButton = Button(this).apply {
            text = "RUN POWERPC SELF-TEST"
            setOnClickListener {
                output.text = try { runSelfTest() }
                catch (error: UnsatisfiedLinkError) {
                    "Native runtime failed to load: ${error.message}"
                }
            }
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(24, 48, 24, 24)
            addView(title); addView(subtitle); addView(runButton); addView(output)
        }
        setContentView(layout)
    }
}
