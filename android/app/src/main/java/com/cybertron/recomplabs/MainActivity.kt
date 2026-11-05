package com.cybertron.recomplabs

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private external fun runSelfTest(): String
    companion object {
        private const val PICK_FILE = 4103
        private const val BUILD = "CRL-ANDROID-004"
        private val BG = 0xFF0A0F18.toInt()
        private val PANEL = 0xFF141E2B.toInt()
        private val FG = 0xFFEAF2FA.toInt()
        private val MUTED = 0xFF9AAABD.toInt()
        private val TEAL = 0xFF45D6C5.toInt()
        private val GOLD = 0xFFFFC46B.toInt()
        init { System.loadLibrary("ppc_runtime") }
    }
    private lateinit var output: TextView
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = BG
        window.navigationBarColor = BG
        val version = packageManager.getPackageInfo(packageName, 0).versionName ?: "unknown"
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(24))
            setBackgroundColor(BG)
        }
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(BG)
            addView(root)
        }
        root.addView(text("CYBERTRON", 13f, TEAL, true), lp(3))
        root.addView(text("RECOMP LABS", 30f, FG, true), lp(5))
        root.addView(text("ANDROID ARM64 RESEARCH BUILD", 12f, MUTED, true), lp(18))

        val build = panel()
        build.addView(text("MILESTONE 3  /  XEX PREFLIGHT", 13f, TEAL, true), lp(8))
        build.addView(text("Version $version  •  $BUILD", 14f, FG, true), lp(5))
        build.addView(text("Target: Android ARM64", 12f, MUTED))
        root.addView(build, lp(16))

        root.addView(text("RUNTIME TESTS", 12f, MUTED, true), lp(8))
        val test = action("RUN POWERPC SELF-TEST", TEAL)
        test.setOnClickListener {
            status.text = "TEST COMPLETE"
            status.setTextColor(TEAL)
            output.text = try { runSelfTest() }
            catch (e: Exception) { "RUNTIME ERROR\n${e.javaClass.simpleName}: ${e.message}" }
        }
        root.addView(test, lp(10))
        val inspect = action("INSPECT LOCAL EXECUTABLE", GOLD)
        inspect.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            startActivityForResult(intent, PICK_FILE)
        }
        root.addView(inspect, lp(16))

        val console = panel()
        console.addView(text("OUTPUT CONSOLE", 12f, TEAL, true), lp(8))
        status = text("READY  •  WAITING FOR TEST", 11f, MUTED, true)
        console.addView(status, lp(10))
        output = text("No test has been run. Choose an action above.", 13f, FG)
        output.typeface = Typeface.MONOSPACE
        output.setLineSpacing(dp(3).toFloat(), 1f)
        console.addView(output)
        root.addView(console, lp(16))

        val limits = panel()
        limits.addView(text("CURRENT CAPABILITIES", 12f, TEAL, true), lp(8))
        limits.addView(text("✓ Synthetic PowerPC instruction test", 13f, FG), lp(5))
        limits.addView(text("✓ Local file metadata and 4-byte signature check", 13f, FG), lp(5))
        limits.addView(text("— No XEX parsing or recompilation yet", 13f, MUTED), lp(5))
        limits.addView(text("— No game boot, graphics, audio or playable FPS yet", 13f, MUTED))
        root.addView(limits, lp(16))
        root.addView(text("RESEARCH PROTOTYPE  •  NO GAME DATA UPLOADED", 10f, MUTED, true))
        setContentView(scroll)
    }

    private fun panel() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(15), dp(16), dp(15))
        background = shape(PANEL, 14)
    }
    private fun text(value: String, size: Float, color: Int, bold: Boolean = false) =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        }
    private fun action(value: String, color: Int) = Button(this).apply {
        text = value
        textSize = 13f
        isAllCaps = false
        setTextColor(BG)
        setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        background = shape(color, 10)
        minHeight = dp(54)
        gravity = Gravity.CENTER
        elevation = 0f
        stateListAnimator = null
    }
    private fun shape(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }
    private fun lp(bottom: Int = 0) = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply { if (bottom > 0) bottomMargin = dp(bottom) }
    private fun dp(v: Int) = (v * resources.displayMetrics.density + 0.5f).toInt()

    @Deprecated("Legacy result callback retained for compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PICK_FILE || resultCode != RESULT_OK) return
        val uri: Uri = data?.data ?: return
        status.text = "FILE INSPECTION COMPLETE"
        status.setTextColor(GOLD)
        output.text = inspect(uri)
    }

    private fun inspect(uri: Uri): String = try {
        val metadata = contentResolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null
        )?.use { c ->
            if (c.moveToFirst()) {
                val ni = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val si = c.getColumnIndex(OpenableColumns.SIZE)
                (if (ni >= 0) c.getString(ni) else "Selected file") to
                    (if (si >= 0 && !c.isNull(si)) c.getLong(si) else -1L)
            } else null
        } ?: ("Selected file" to -1L)
        val sig = contentResolver.openInputStream(uri)?.use { stream ->
            val b = ByteArray(4)
            var n = 0
            while (n < 4) {
                val r = stream.read(b, n, 4 - n)
                if (r <= 0) break
                n += r
            }
            if (n == 4) b.toString(Charsets.US_ASCII) else "SHORT"
        } ?: "UNREADABLE"
        val detail = when (sig) {
            "XEX2" -> "XEX2 signature recognized."
            "SHORT" -> "File is shorter than four bytes."
            "UNREADABLE" -> "Could not read selected file."
            else -> "Unknown signature bytes: " +
                sig.toByteArray(Charsets.US_ASCII).joinToString(" ") { "%02X".format(it) }
        }
        "LOCAL FILE PREFLIGHT (NO UPLOAD)\nName: ${metadata.first}\nSize: ${if (metadata.second >= 0) "${metadata.second} bytes" else "unavailable"}\nSignature: $sig\n\n$detail\n\nMetadata and four-byte check only. This does NOT parse, decrypt, recompile or boot the game."
    } catch (e: Exception) {
        "INSPECTION ERROR\n${e.javaClass.simpleName}: ${e.message}"
    }
}
