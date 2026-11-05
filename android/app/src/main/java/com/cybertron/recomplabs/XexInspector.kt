package com.cybertron.recomplabs

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import java.io.BufferedInputStream
import java.io.EOFException
import java.io.InputStream

/**
 * Read-only XEX2 structural preflight.
 *
 * This intentionally does not decrypt/decompress XEX sections or execute code.
 * It reads the XEX header, optional-header directory and PE signature only.
 */
object XexInspector {
    private data class FileInfo(val name: String, val size: Long)

    fun inspect(resolver: ContentResolver, uri: Uri): String {
        return try {
            val info = queryFileInfo(resolver, uri)
            if (info.size < HEADER_SIZE) {
                return fail("FILE TOO SMALL", "The selected file is smaller than the 24-byte XEX header.")
            }

            val stream = resolver.openInputStream(uri)
                ?: return fail("FILE ACCESS FAILED", "Android could not open the selected document.")

            stream.use { raw ->
                val input = BufferedInputStream(raw, 64 * 1024)
                val header = readExact(input, HEADER_SIZE)
                val magic = header.copyOfRange(0, 4).toString(Charsets.US_ASCII)

                if (magic != "XEX2") {
                    return fail(
                        "NOT A RECOGNIZED XEX2 FILE",
                        "The first four bytes are ${hex(header.copyOfRange(0, 4))}, not XEX2."
                    )
                }

                val moduleFlags = u32(header, 4)
                val peOffset = u32(header, 8)
                val reserved = u32(header, 12)
                val securityInfoOffset = u32(header, 16)
                val optionalCount = u32(header, 20)

                if (optionalCount > MAX_OPTIONAL_HEADERS) {
                    return fail("INVALID HEADER", "Optional-header count $optionalCount exceeds the safety limit.")
                }

                val directoryBytes = optionalCount * OPTIONAL_ENTRY_SIZE
                val directoryEnd = HEADER_SIZE.toLong() + directoryBytes
                if (directoryEnd > info.size) {
                    return fail("TRUNCATED HEADER", "Optional-header directory extends beyond the reported file size.")
                }

                val optionalHeaders = ArrayList<String>(optionalCount.toInt())
                repeat(optionalCount.toInt()) {
                    val entry = readExact(input, OPTIONAL_ENTRY_SIZE)
                    val key = u32(entry, 0)
                    val value = u32(entry, 4)
                    optionalHeaders.add("0x${key.toString(16).padStart(8, '0')} → 0x${value.toString(16).padStart(8, '0')}")
                }

                if (peOffset < directoryEnd || peOffset + 4L > info.size) {
                    return fail(
                        "INVALID PE OFFSET",
                        "PE data offset 0x${peOffset.toString(16)} is outside the valid file range."
                    )
                }

                val skipBytes = peOffset - directoryEnd
                if (!skipFully(input, skipBytes)) {
                    return fail("TRUNCATED FILE", "Could not reach the PE data offset.")
                }
                val peSignature = readExact(input, 4)
                val peValid = peSignature.contentEquals(byteArrayOf(0x50, 0x45, 0x00, 0x00))

                val report = StringBuilder()
                report.appendLine("CYBERTRON RECOMP LABS — BOOT PREFLIGHT")
                report.appendLine("File: ${info.name}")
                report.appendLine("Size: ${info.size} bytes")
                report.appendLine("Container magic: $magic")
                report.appendLine("Module flags: 0x${moduleFlags.toString(16)}")
                report.appendLine("PE data offset: 0x${peOffset.toString(16)}")
                report.appendLine("Security info offset: 0x${securityInfoOffset.toString(16)}")
                report.appendLine("Reserved field: 0x${reserved.toString(16)}")
                report.appendLine("Optional headers: $optionalCount")
                report.appendLine("PE signature at declared offset: ${if (peValid) "PASS" else "FAIL (${hex(peSignature)})"}")
                if (optionalHeaders.isNotEmpty()) {
                    report.appendLine("Optional-header keys:")
                    optionalHeaders.take(MAX_HEADERS_TO_DISPLAY).forEach { report.appendLine("  $it") }
                    if (optionalHeaders.size > MAX_HEADERS_TO_DISPLAY) {
                        report.appendLine("  … ${optionalHeaders.size - MAX_HEADERS_TO_DISPLAY} more")
                    }
                }
                report.appendLine()
                report.appendLine("BOOT VERDICT: CANNOT BOOT WITH THIS BUILD")
                report.appendLine(
                    if (peValid) {
                        "The basic XEX2 header and PE signature checks passed. This is a container-format result only, not proof the game is loadable."
                    } else {
                        "The declared PE signature check failed. The file may be truncated, malformed, or use a layout this preflight does not support."
                    }
                )
                report.appendLine()
                report.appendLine("Missing before a real boot attempt: XEX security/decompression handling, PE image mapping and relocations, Xbox import/kernel compatibility, complete PowerPC + VMX128 execution, memory/MMIO, and graphics/audio services.")
                report.appendLine("No game data was uploaded or executed.")
                report.toString()
            }
        } catch (e: EOFException) {
            fail("TRUNCATED FILE", "The selected file ended before the required header data was read.")
        } catch (e: SecurityException) {
            fail("PERMISSION ERROR", "Android did not grant read access to this document.")
        } catch (e: Exception) {
            fail("PREFLIGHT ERROR", "${e.javaClass.simpleName}: ${e.message ?: "Unknown error"}")
        }
    }

    private fun queryFileInfo(resolver: ContentResolver, uri: Uri): FileInfo {
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    val name = if (nameIndex >= 0) cursor.getString(nameIndex) ?: "Selected file" else "Selected file"
                    val size = if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) cursor.getLong(sizeIndex) else -1L
                    if (size >= 0L) return FileInfo(name, size)
                }
            }
        throw IllegalArgumentException("The document provider did not report a reliable file size.")
    }

    private fun readExact(input: InputStream, count: Int): ByteArray {
        val bytes = ByteArray(count)
        var offset = 0
        while (offset < count) {
            val read = input.read(bytes, offset, count - offset)
            if (read < 0) throw EOFException()
            if (read == 0) continue
            offset += read
        }
        return bytes
    }

    private fun skipFully(input: InputStream, count: Long): Boolean {
        var remaining = count
        val scratch = ByteArray(8192)
        while (remaining > 0L) {
            val skipped = input.skip(remaining)
            if (skipped > 0L) {
                remaining -= skipped
            } else {
                val read = input.read(scratch, 0, minOf(scratch.size.toLong(), remaining).toInt())
                if (read < 0) return false
                if (read == 0) continue
                remaining -= read.toLong()
            }
        }
        return true
    }

    private fun u32(bytes: ByteArray, offset: Int): Long =
        ((bytes[offset].toLong() and 0xffL) shl 24) or
        ((bytes[offset + 1].toLong() and 0xffL) shl 16) or
        ((bytes[offset + 2].toLong() and 0xffL) shl 8) or
        (bytes[offset + 3].toLong() and 0xffL)

    private fun hex(bytes: ByteArray): String =
        bytes.joinToString(" ") { "%02X".format(it.toInt() and 0xff) }

    private fun fail(title: String, detail: String): String =
        "CYBERTRON RECOMP LABS — BOOT PREFLIGHT\n$title\n\n$detail\n\nBOOT VERDICT: CANNOT BOOT WITH THIS BUILD. No game data was uploaded or executed."

    private const val HEADER_SIZE = 24
    private const val OPTIONAL_ENTRY_SIZE = 8
    private const val MAX_OPTIONAL_HEADERS = 4096L
    private const val MAX_HEADERS_TO_DISPLAY = 24
}
