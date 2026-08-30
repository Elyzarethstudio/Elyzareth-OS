package com.example.engine

import android.content.Context
import com.example.model.ArchiveFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Local Disk Persistence Manager for Elyzareth OS Space Archive (App 05).
 * Persists all archive artifacts deterministically so they survive app process kills and reboots.
 */
object ArchivePersistenceManager {

    private const val ARCHIVE_FILE_NAME = "elyzareth_space_archive.json"

    fun saveArchiveFiles(context: Context, files: List<ArchiveFile>) {
        try {
            val rootArray = JSONArray()
            for (file in files) {
                val obj = JSONObject()
                obj.put("id", file.id)
                obj.put("fileName", file.fileName)
                obj.put("category", file.category)
                obj.put("originTenant", file.originTenant)
                obj.put("previewText", file.previewText)
                obj.put("fullText", file.fullText)
                obj.put("g3SealHash", file.g3SealHash)
                obj.put("sizeKb", file.sizeKb.toDouble())
                obj.put("timestamp", file.timestamp)
                rootArray.put(obj)
            }
            val targetFile = File(context.filesDir, ARCHIVE_FILE_NAME)
            targetFile.writeText(rootArray.toString(2), Charsets.UTF_8)
        } catch (_: Exception) {
            // Silently fail or log in non-fatal conditions
        }
    }

    fun loadSavedArchiveFiles(context: Context): List<ArchiveFile>? {
        return try {
            val targetFile = File(context.filesDir, ARCHIVE_FILE_NAME)
            if (!targetFile.exists() || !targetFile.canRead()) return null
            val content = targetFile.readText(Charsets.UTF_8)
            if (content.isBlank()) return null

            val rootArray = JSONArray(content)
            val filesList = mutableListOf<ArchiveFile>()
            for (i in 0 until rootArray.length()) {
                val obj = rootArray.getJSONObject(i)
                filesList.add(
                    ArchiveFile(
                        id = obj.optString("id", "ARC-RESTORED-$i"),
                        fileName = obj.optString("fileName", "artifact_$i.txt"),
                        category = obj.optString("category", "ALL"),
                        originTenant = obj.optString("originTenant", "Archive Persistence"),
                        previewText = obj.optString("previewText", ""),
                        fullText = obj.optString("fullText", ""),
                        g3SealHash = obj.optString("g3SealHash", "G3-SEAL-RESTORED"),
                        sizeKb = obj.optDouble("sizeKb", 2.5).toFloat(),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            if (filesList.isNotEmpty()) filesList else null
        } catch (_: Exception) {
            null
        }
    }
}
