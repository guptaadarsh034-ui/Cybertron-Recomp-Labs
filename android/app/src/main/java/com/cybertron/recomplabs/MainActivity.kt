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
    companion object {
        private const val PICK_EXE = 7101
        private const val PICK_FOLDER = 7102
        private const val BG = 0xFF080D16.toInt()
        private const val SURFACE = 0xFF121D2A.toInt()
        private const val TEXT = 0xFFEAF1F8.toInt()
        private const val MUTED = 0xFF9AAABD.toInt()
        private const val CYAN = 0xFF55D9D0.toInt()
        private const val ORANGE = 0xFFFFBD69.toInt()
    }

    private lateinit var status: TextView
    private lateinit var exeInfo: TextView
    private lateinit var folderInfo: TextView

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
        page.addView(text("CYBERTRON RECOMP LABS", 24f, TEXT, true), margin(6))
        page.addView(text("WINDOWS PC → ANDROID ARM64", 12f, CYAN, true), margin(18))

        val intro = card()
        intro.addView(text("PC COMPATIBILITY FOUNDATION", 12f, CYAN, true), margin(8))
        intro.addView(text("Version " + version, 17f, TEXT, true), margin(5))
        intro.addView(text("Import your own legally obtained Windows game files. Nothing is uploaded.", 13f, MUTED))
        page.addView(intro, margin(16))

        page.addView(text("01 / SELECT GAME EXECUTABLE", 12f, MUTED, true), margin(8))
        page.addView(actionButton("SELECT WINDOWS .EXE", CYAN).apply {
            setOnClickListener {
                val picker = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                startActivityForResult(picker, PICK_EXE)
            }
        }, margin(8))
        exeInfo = text("No executable selected", 12f, MUTED)
        page.addView(exeInfo, margin(12))

        page.addView(text("02 / SELECT GAME INSTALLATION FOLDER", 12f, MUTED, true), margin(8))
        page.addView(actionButton("SELECT GAME FOLDER", ORANGE).apply {
            setOnClickListener {
                startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), PICK_FOLDER)
            }
        }, margin(8))
        folderInfo = text("No game folder selected", 12f, MUTED)
        page.addView(folderInfo, margin(12))

        val state = card()
        state.addView(text("DEVELOPMENT STATUS", 12f, CYAN, true), margin(8))
        status = text("IMPORT SHELL READY", 13f, TEXT, true)
        state.addView(status, margin(8))
        state.addView(text("This build does not include Wine, x86 CPU translation, DirectX-to-Vulkan translation, or the game runtime. Selecting files does not launch the game. These components must be integrated and tested in later milestones.", 12f, MUTED))
        page.addView(state, margin(16))
        page.addView(text("Independent fan research • No game files bundled • No affiliation with rights holders", 10f, MUTED), margin(8))
        setContentView(scroll)
    }

    @Deprecated("Uses Android's system document picker")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        val flags = data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        try {
            contentResolver.takePersistableUriPermission(uri, flags)
        } catch (_: SecurityException) {
            // Some document providers do not offer persistable permissions.
        }
        when (requestCode) {
            PICK_EXE -> {
                val name = displayName(uri)
                exeInfo.text = "Selected: " + name + "\nURI: " + uri.toString()
                status.text = if (name.endsWith(".exe", ignoreCase = true)) "EXECUTABLE SELECTED — NOT YET TESTED" else "FILE SELECTED — PLEASE CHOOSE THE GAME .EXE"
            }
            PICK_FOLDER -> {
                folderInfo.text = "Selected game folder:\n" + uri.toString()
                status.text = "GAME FOLDER SELECTED — RUNTIME NOT INTEGRATED"
            }
        }
    }

    private fun displayName(uri: Uri): String {
        contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index >= 0) return cursor.getString(index) ?: "Selected file"
            }
        }
        return uri.lastPathSegment ?: "Selected file"
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
        minHeight = dp(54)
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

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()
}
