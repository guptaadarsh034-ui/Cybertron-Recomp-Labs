package com.cybertron.recomplabs

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private external fun runSelfTest(): String
    companion object {
        private const val REQUEST_PICK_EXECUTABLE = 4103
        init { System.loadLibrary("ppc_runtime") }
    }
    private lateinit var output: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = TextView(this).apply {
            text = "CYBERTRON RECOMP LABS"; textSize = 22f; gravity = Gravity.CENTER
        }
        val info = packageManager.getPackageInfo(packageName, 0)
        val subtitle = TextView(this).apply {
            text = "Milestone 3 • XEX preflight + ARM64 runtime\nVersion \${info.versionName} (\${info.versionCode})"
            textSize = 14f; gravity = Gravity.CENTER
        }
        output = TextView(this).apply {
            text = "Ready. Run the synthetic PowerPC test or inspect a local executable's file signature."
            textSize = 15f; setPadding(16, 24, 16, 24)
        }
        val runButton = Button(this).apply {
            text = "RUN POWERPC SELF-TEST"
            setOnClickListener {
                output.text = try { runSelfTest() }
                catch (error: UnsatisfiedLinkError) { "Native runtime failed to load: \${error.message}" }
            }
        }
        val inspectButton = Button(this).apply {
            text = "INSPECT LOCAL EXECUTABLE"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                startActivityForResult(intent, REQUEST_PICK_EXECUTABLE)
            }
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(24, 48, 24, 24)
            addView(title); addView(subtitle); addView(runButton); addView(inspectButton); addView(output)
        }
        setContentView(layout)
    }

    @Deprecated("Deprecated in Android API; retained for broad platform compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_PICK_EXECUTABLE || resultCode != RESULT_OK) return
        val uri: Uri = data?.data ?: return
        output.text = inspectLocalFile(uri)
    }

    private fun inspectLocalFile(uri: Uri): String = try {
        val metadata = contentResolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val ni = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val si = cursor.getColumnIndex(OpenableColumns.SIZE)
                val name = if (ni >= 0) cursor.getString(ni) else "Selected file"
                val size = if (si >= 0 && !cursor.isNull(si)) cursor.getLong(si) else -1L
                name to size
            } else null
        } ?: ("Selected file" to -1L)

        val signature = contentResolver.openInputStream(uri)?.use { stream ->
            val bytes = ByteArray(4)
            var count = 0
            while (count < bytes.size) {
                val n = stream.read(bytes, count, bytes.size - count)
                if (n <= 0) break
                count += n
            }
            if (count == 4) bytes.toString(Charsets.US_ASCII) else "SHORT"
        } ?: "UNREADABLE"
        val sizeText = if (metadata.second >= 0) "\${metadata.second} bytes" else "size unavailable"
        val identification = when (signature) {
            "XEX2" -> "XEX2 signature recognized."
            "SHORT" -> "File is shorter than a 4-byte signature."
            "UNREADABLE" -> "Could not read the selected file."
            else -> "Unknown 4-byte signature: \${signature.toByteArray(Charsets.US_ASCII).joinToString(" ") { "%02X".format(it) }}"
        }
        "Local file inspection (no upload)\nName: \${metadata.first}\nSize: $sizeText\nSignature: $signature\n\n$identification\n\nPreflight only: this does NOT parse, decrypt, recompile, or boot the game."
    } catch (error: Exception) {
        "Could not inspect file: \${error.javaClass.simpleName}: \${error.message}"
    }
}
