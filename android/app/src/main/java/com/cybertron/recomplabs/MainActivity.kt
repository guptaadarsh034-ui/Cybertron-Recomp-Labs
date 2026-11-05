package com.cybertron.recomplabs

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private external fun runSelfTest(): String

    companion object {
        private const val PICK_GAME_FILE = 5106
        private const val BUILD_ID = "CRL-BOOT-PREFLIGHT-001"
        private const val BG = 0xFF080D16.toInt()
        private const val SURFACE = 0xFF121D2A.toInt()
        private const val TEXT = 0xFFEAF1F8.toInt()
        private const val MUTED = 0xFF9AAABD.toInt()
        private const val CYAN = 0xFF55D9D0.toInt()
        private const val ORANGE = 0xFFFFBD69.toInt()

        init { System.loadLibrary("ppc_runtime") }
    }

    private lateinit var console: TextView
    private lateinit var state: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = BG
        window.navigationBarColor = BG
        window.decorView.setBackgroundColor(BG)

        val version = packageManager.getPackageInfo(packageName, 0).versionName ?: "unknown"
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(28))
            setBackgroundColor(BG)
        }
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(BG)
            addView(page)
        }

        page.addView(text("CYBERTRON RECOMP LABS", 25f, TEXT, true), margin(5))
        page.addView(text("XBOX 360 → ANDROID ARM64", 12f, CYAN, true), margin(18))

        val identity = card()
        identity.addView(text("FIRST BOOT-FEASIBILITY BUILD", 12f, CYAN, true), margin(9))
        identity.addView(text("Version $version", 18f, TEXT, true), margin(4))
        identity.addView(text("Build ID: $BUILD_ID", 11f, MUTED, true), margin(4))
        identity.addView(text("Device ABI: ${android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"}", 12f, MUTED))
        page.addView(identity, margin(16))

        page.addView(text("01  /  GAME FILE CHECK", 12f, MUTED, true), margin(8))
        val select = actionButton("SELECT XEX AND CHECK BOOT READINESS", ORANGE)
        select.setOnClickListener {
            val picker = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            startActivityForResult(picker, PICK_GAME_FILE)
        }
        page.addView(select, margin(8))
        page.addView(text("Reads the XEX2 header and declared PE signature locally. The file is not uploaded or executed.", 12f, MUTED), margin(16))

        page.addView(text("02  /  NATIVE CPU SELF-TEST", 12f, MUTED, true), margin(8))
        val test = actionButton("RUN SYNTHETIC POWERPC TEST", CYAN)
        test.setOnClickListener {
            state.text = "SYNTHETIC TEST FINISHED"
            state.setTextColor(CYAN)
            console.text = try {
                runSelfTest()
            } catch (e: Throwable) {
                "NATIVE TEST FAILED\n${e.javaClass.simpleName}: ${e.message ?: "No details"}"
            }
        }
        page.addView(test, margin(16))

        val outputCard = card()
        outputCard.addView(text("DIAGNOSTIC REPORT", 12f, CYAN, true), margin(10))
        state = text("WAITING FOR INPUT", 11f, MUTED, true)
        outputCard.addView(state, margin(10))
        console = text(
            "Choose a local XEX file to run the boot-readiness preflight, or run the synthetic CPU test.",
            13f, TEXT
        ).apply {
            typeface = Typeface.MONOSPACE
            setLineSpacing(dp(3).toFloat(), 1f)
        }
        outputCard.addView(console)
        page.addView(outputCard, margin(16))

        val reality = card()
        reality.addView(text("HONEST STATUS", 12f, CYAN, true), margin(8))
        reality.addView(text("This APK cannot boot the game yet.", 15f, ORANGE, true), margin(7))
        reality.addView(text("The preflight can identify a basic XEX2 container structure. It does not decrypt or decompress game sections, map the executable, resolve Xbox imports, emulate the Xbox kernel, or render Xenos graphics/audio.", 12f, TEXT), margin(8))
        reality.addView(text("A passing file check means only that selected header checks passed. It is not proof that the game can run on this phone.", 12f, MUTED))
        page.addView(reality, margin(16))

        page.addView(text("OFFLINE LOCAL INSPECTION  •  NO GAME DATA UPLOAD", 10f, MUTED, true))
        setContentView(scroll)
    }

    @Deprecated("Legacy document-picker callback kept to avoid extra dependencies")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PICK_GAME_FILE || resultCode != RESULT_OK) return
        val uri: Uri = data?.data ?: return
        state.text = "XEX STRUCTURAL PREFLIGHT FINISHED"
        state.setTextColor(ORANGE)
        console.text = XexInspector.inspect(contentResolver, uri)
    }

    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(16), dp(16), dp(16))
        background = rounded(SURFACE, 14)
    }

    private fun text(value: String, size: Float, color: Int, bold: Boolean = false) =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        }

    private fun actionButton(value: String, color: Int) = Button(this).apply {
        text = value
        textSize = 12f
        isAllCaps = false
        setTextColor(BG)
        setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        background = rounded(color, 10)
        minHeight = dp(56)
        gravity = Gravity.CENTER
        elevation = 0f
        stateListAnimator = null
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun margin(bottom: Int = 0) = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply { if (bottom > 0) bottomMargin = dp(bottom) }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density + 0.5f).toInt()
}
