package com.example.engine

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.example.model.*
import java.io.InputStream
import java.security.MessageDigest
import java.util.*

object CorpusDiscoveryEngine {

    private const val BUFFER_SIZE = 8192

    /**
     * Recursively walks a SAF Document Tree and builds a non-destructive, read-only
     * inventory of all discovered song corpora and artifacts.
     */
    fun performDiscoveryDryRun(
        context: Context,
        rootTreeUri: Uri,
        rootDisplayName: String,
        onProgress: (currentFileCount: Int, currentPath: String) -> Unit = { _, _ -> }
    ): CorpusInventoryReport {
        val discoveredRecords = mutableListOf<DiscoveredArtifactRecord>()
        val contentResolver = context.contentResolver

        try {
            val rootDocId = DocumentsContract.getTreeDocumentId(rootTreeUri)
            val rootChildrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(rootTreeUri, rootDocId)

            traverseDirectoryRecursive(
                context = context,
                rootTreeUri = rootTreeUri,
                currentFolderUri = rootChildrenUri,
                currentRelativePath = "",
                discoveredRecords = discoveredRecords,
                onProgress = onProgress
            )
        } catch (e: Exception) {
            return CorpusInventoryReport(
                sourceRootUri = rootTreeUri.toString(),
                sourceRootDisplayName = rootDisplayName,
                scanStatus = IngestionScanStatus.FAILED,
                scanStatusMessage = "Discovery Dry Run encountered an error: ${e.localizedMessage ?: e.message}"
            )
        }

        // Run deterministic grouping and classification
        return buildInventoryReportFromRecords(
            sourceRootUri = rootTreeUri.toString(),
            sourceRootDisplayName = rootDisplayName,
            records = discoveredRecords
        )
    }

    private fun traverseDirectoryRecursive(
        context: Context,
        rootTreeUri: Uri,
        currentFolderUri: Uri,
        currentRelativePath: String,
        discoveredRecords: MutableList<DiscoveredArtifactRecord>,
        onProgress: (Int, String) -> Unit
    ) {
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED
        )

        val childFolders = mutableListOf<Pair<String, String>>() // docId, displayName

        context.contentResolver.query(currentFolderUri, projection, null, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
            val sizeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
            val modifiedCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

            while (cursor.moveToNext()) {
                val docId = if (idCol >= 0) cursor.getString(idCol) else null ?: continue
                val displayName = if (nameCol >= 0) cursor.getString(nameCol) else ""
                val mimeType = if (mimeCol >= 0) cursor.getString(mimeCol) ?: "" else ""
                val size = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
                val lastModified = if (modifiedCol >= 0) cursor.getLong(modifiedCol) else 0L

                val isDirectory = mimeType == DocumentsContract.Document.MIME_TYPE_DIR ||
                        mimeType == "vnd.android.document/directory"

                if (isDirectory) {
                    childFolders.add(Pair(docId, displayName))
                } else {
                    val docUri = DocumentsContract.buildDocumentUriUsingTree(rootTreeUri, docId)
                    val record = processDiscoveredArtifact(
                        context = context,
                        docUri = docUri,
                        fileName = displayName,
                        relativePath = if (currentRelativePath.isEmpty()) displayName else "$currentRelativePath/$displayName",
                        fileSizeBytes = size,
                        lastModified = lastModified,
                        mimeType = mimeType
                    )
                    discoveredRecords.add(record)
                    onProgress(discoveredRecords.size, record.relativePath)
                }
            }
        }

        // Recurse into child directories
        for ((subDocId, subName) in childFolders) {
            val subChildrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(rootTreeUri, subDocId)
            val subRelativePath = if (currentRelativePath.isEmpty()) subName else "$currentRelativePath/$subName"
            traverseDirectoryRecursive(
                context = context,
                rootTreeUri = rootTreeUri,
                currentFolderUri = subChildrenUri,
                currentRelativePath = subRelativePath,
                discoveredRecords = discoveredRecords,
                onProgress = onProgress
            )
        }
    }

    private fun processDiscoveredArtifact(
        context: Context,
        docUri: Uri,
        fileName: String,
        relativePath: String,
        fileSizeBytes: Long,
        lastModified: Long,
        mimeType: String
    ): DiscoveredArtifactRecord {
        val ext = fileName.substringAfterLast('.', "").lowercase(Locale.US)
        val category = when {
            ext in listOf("txt", "lrc", "md", "lyric") || mimeType.startsWith("text/") -> ArtifactCategory.LYRIC_TEXT
            ext in listOf("json", "schema", "witness") || mimeType.contains("json") -> ArtifactCategory.STRUCTURED_SCHEMA
            ext in listOf("wav", "pcm", "mp3", "m4a", "flac", "ogg", "aac") || mimeType.startsWith("audio/") -> ArtifactCategory.AUDIO_STREAM
            ext in listOf("xml", "csv", "yaml", "yml", "ini") -> ArtifactCategory.METADATA_RECORD
            else -> ArtifactCategory.UNKNOWN
        }

        var sha256 = "UNCOMPUTED"
        var isParsed = false
        var lineCount = 0
        var wordCount = 0
        var charCount = 0
        var snippet: String? = null
        var detectedLang = "Uncertain"
        var parseError: String? = null

        try {
            // Deterministic SHA-256 computation over raw byte stream
            context.contentResolver.openInputStream(docUri)?.use { inputStream ->
                val digest = MessageDigest.getInstance("SHA-256")
                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
                sha256 = "sha256:" + digest.digest().joinToString("") { "%02x".format(it) }
            }

            // Non-destructive parsing based on category
            when (category) {
                ArtifactCategory.LYRIC_TEXT -> {
                    context.contentResolver.openInputStream(docUri)?.use { stream ->
                        val text = stream.bufferedReader(Charsets.UTF_8).readText()
                        lineCount = text.lines().size
                        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
                        wordCount = words.size
                        charCount = text.length
                        snippet = text.take(240).trim()
                        detectedLang = detectLyricLanguage(text)
                        isParsed = true
                    }
                }
                ArtifactCategory.STRUCTURED_SCHEMA -> {
                    context.contentResolver.openInputStream(docUri)?.use { stream ->
                        val json = stream.bufferedReader(Charsets.UTF_8).readText()
                        charCount = json.length
                        snippet = json.take(240).trim()
                        isParsed = json.trimStart().startsWith("{") || json.trimStart().startsWith("[")
                        if (!isParsed) {
                            parseError = "File has .json extension but is not valid JSON root."
                        }
                    }
                }
                ArtifactCategory.AUDIO_STREAM -> {
                    // Audio acoustic measurements are strictly marked NOT_MEASURED until full PCM decoder pass
                    isParsed = true
                    snippet = "Binary Audio Stream [$ext, ${formatBytes(fileSizeBytes)}]"
                }
                ArtifactCategory.METADATA_RECORD, ArtifactCategory.UNKNOWN -> {
                    isParsed = true
                }
            }
        } catch (e: Exception) {
            isParsed = false
            parseError = "Read/Parse error: ${e.localizedMessage ?: e.message}"
        }

        // Deterministically extract title and version candidates from path & filename
        val (detectedTitle, detectedVer) = extractTitleAndVersion(fileName, relativePath)

        val discoveryState = when {
            !isParsed -> IngestionDiscoveryState.FAILED
            category == ArtifactCategory.AUDIO_STREAM -> IngestionDiscoveryState.NOT_MEASURED
            else -> IngestionDiscoveryState.PARSED
        }

        return DiscoveredArtifactRecord(
            id = "ART-${UUID.randomUUID().toString().take(8).uppercase(Locale.US)}",
            documentUri = docUri.toString(),
            fileName = fileName,
            relativePath = relativePath,
            fileExtension = ext,
            fileSizeBytes = fileSizeBytes,
            lastModified = lastModified,
            mimeType = mimeType,
            sha256Hash = sha256,
            category = category,
            discoveryState = discoveryState,
            isParsedSuccessfully = isParsed,
            detectedBaseTitle = detectedTitle,
            detectedVersionLabel = detectedVer,
            detectedLanguage = detectedLang,
            lineCount = lineCount,
            wordCount = wordCount,
            characterCount = charCount,
            snippetText = snippet,
            parseErrorMessage = parseError,
            audioFormat = if (category == ArtifactCategory.AUDIO_STREAM) ext else null
        )
    }

    private fun detectLyricLanguage(text: String): String {
        var devanagariCount = 0
        var latinCount = 0
        for (ch in text) {
            val code = ch.code
            if (code in 0x0900..0x097F) {
                devanagariCount++
            } else if (ch in 'A'..'Z' || ch in 'a'..'z') {
                latinCount++
            }
        }

        val total = devanagariCount + latinCount
        if (total == 0) return "Unknown / Non-Textual"

        val devanagariRatio = devanagariCount.toFloat() / total
        return when {
            devanagariRatio > 0.40f -> "Hindi (Devanagari)"
            devanagariRatio in 0.05f..0.40f -> "Hindi / English Mixed"
            containsRomanizedHindiKeywords(text) -> "Hindi (Romanized)"
            else -> "English / Latin"
        }
    }

    private fun containsRomanizedHindiKeywords(text: String): Boolean {
        val lower = text.lowercase(Locale.US)
        val hindiKeywords = listOf(
            "tera", "meri", "dil", "pyar", "ishq", "kya", "zindagi", "raat", "chaand", "aankhon",
            "khoya", "tujhe", "hum", "tum", "saath", "mohabbat", "khwaab", "tere", "mere", "jahaan"
        )
        var matches = 0
        for (kw in hindiKeywords) {
            if (lower.contains("\\b$kw\\b".toRegex())) {
                matches++
                if (matches >= 2) return true
            }
        }
        return false
    }

    private fun extractTitleAndVersion(fileName: String, relativePath: String): Pair<String, String> {
        val pathSegments = relativePath.split('/').filter { it.isNotBlank() }
        val nameWithoutExt = fileName.substringBeforeLast(".")

        // If located in a distinct subfolder (e.g. "Song_Title/lyric_v1.txt"), subfolder is base title
        val baseTitle = if (pathSegments.size > 1) {
            pathSegments[pathSegments.size - 2]
                .replace('_', ' ')
                .replace('-', ' ')
                .trim()
        } else {
            // Flat structure: strip version suffix if exists
            nameWithoutExt
                .replace(Regex("(?i)[_\\-\\s]+(v\\d+|take\\d+|ver\\d+|demo|final|master|mix).*$"), "")
                .replace('_', ' ')
                .replace('-', ' ')
                .trim()
                .ifBlank { nameWithoutExt }
        }

        // Version extractor
        val verMatch = Regex("(?i)(v\\d+|ver\\d+|take\\d+|demo|draft|final)").find(fileName)
        val versionLabel = verMatch?.value?.uppercase(Locale.US) ?: "v01"

        return Pair(baseTitle, versionLabel)
    }

    private fun buildInventoryReportFromRecords(
        sourceRootUri: String,
        sourceRootDisplayName: String,
        records: List<DiscoveredArtifactRecord>
    ): CorpusInventoryReport {
        // Group by detected Base Title deterministically
        val baseGroupsMap = mutableMapOf<String, MutableList<DiscoveredArtifactRecord>>()
        val duplicatesByHash = mutableMapOf<String, MutableList<DiscoveredArtifactRecord>>()

        for (rec in records) {
            // Track duplicates by SHA-256 (if computed)
            if (rec.sha256Hash.startsWith("sha256:") && rec.sha256Hash != "sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855") {
                duplicatesByHash.getOrPut(rec.sha256Hash) { mutableListOf() }.add(rec)
            }

            val titleKey = rec.detectedBaseTitle?.lowercase(Locale.US)?.trim() ?: "uncategorized_orphans"
            baseGroupsMap.getOrPut(titleKey) { mutableListOf() }.add(rec)
        }

        val duplicateRecordsList = mutableListOf<DiscoveredArtifactRecord>()
        for ((_, hashGroup) in duplicatesByHash) {
            if (hashGroup.size > 1) {
                duplicateRecordsList.addAll(hashGroup)
            }
        }

        val baseTitleGroups = mutableListOf<DiscoveredBaseTitleGroup>()
        val orphanArtifacts = mutableListOf<DiscoveredArtifactRecord>()
        val humanReviewItems = mutableListOf<CorpusReviewItem>()

        val languageCounter = mutableMapOf<String, Int>()
        val evidenceCounter = mutableMapOf(
            "Text Witness Available" to 0,
            "Audio Stream Present" to 0,
            "Schema / Metadata Present" to 0,
            "Audio Acoustic (NOT MEASURED)" to 0,
            "Parse Errors / Malformed" to 0
        )

        var versionsCount = 0
        var missingComponentsCount = 0

        for ((_, groupRecords) in baseGroupsMap) {
            val sampleTitle = groupRecords.firstOrNull { it.detectedBaseTitle != null }?.detectedBaseTitle ?: "Uncategorized"
            val lyrics = groupRecords.filter { it.category == ArtifactCategory.LYRIC_TEXT }
            val audio = groupRecords.filter { it.category == ArtifactCategory.AUDIO_STREAM }
            val schema = groupRecords.filter { it.category == ArtifactCategory.STRUCTURED_SCHEMA }

            lyrics.forEach {
                languageCounter[it.detectedLanguage] = (languageCounter[it.detectedLanguage] ?: 0) + 1
                evidenceCounter["Text Witness Available"] = (evidenceCounter["Text Witness Available"] ?: 0) + 1
            }
            audio.forEach {
                evidenceCounter["Audio Stream Present"] = (evidenceCounter["Audio Stream Present"] ?: 0) + 1
                evidenceCounter["Audio Acoustic (NOT MEASURED)"] = (evidenceCounter["Audio Acoustic (NOT MEASURED)"] ?: 0) + 1
            }
            schema.forEach {
                evidenceCounter["Schema / Metadata Present"] = (evidenceCounter["Schema / Metadata Present"] ?: 0) + 1
            }

            val isOrphanGroup = groupRecords.size == 1 && lyrics.isEmpty() && schema.isEmpty()
            if (isOrphanGroup) {
                orphanArtifacts.addAll(groupRecords)
                humanReviewItems.add(
                    CorpusReviewItem(
                        id = "REV-${UUID.randomUUID().toString().take(6)}",
                        title = sampleTitle,
                        artifactFileName = groupRecords.first().fileName,
                        reason = "Orphan artifact with no companion lyric or schema witness.",
                        severity = ReviewSeverity.WARNING,
                        recommendedAction = "Verify if this file belongs to an existing base title."
                    )
                )
                continue
            }

            val missingList = mutableListOf<String>()
            if (lyrics.isEmpty()) missingList.add("Missing Lyric Witness (.txt/.lrc)")
            if (audio.isEmpty()) missingList.add("Missing Audio Stream (.wav/.pcm/.mp3)")
            if (schema.isEmpty()) missingList.add("Missing Dual-Witness Schema (.json)")

            if (missingList.isNotEmpty()) {
                missingComponentsCount++
            }

            val duplicateWarnings = mutableListOf<String>()
            val versionsInGroup = groupRecords.mapNotNull { it.detectedVersionLabel }.distinct()
            versionsCount += versionsInGroup.size.coerceAtLeast(1)

            val primaryLang = lyrics.firstOrNull()?.detectedLanguage ?: "Unspecified"
            val requiresReview = missingList.isNotEmpty() || duplicateWarnings.isNotEmpty() || groupRecords.any { !it.isParsedSuccessfully }
            val reviewReason = when {
                groupRecords.any { !it.isParsedSuccessfully } -> "One or more artifacts failed non-destructive parsing."
                missingList.isNotEmpty() -> "Incomplete artifact package: ${missingList.joinToString(", ")}"
                else -> null
            }

            if (requiresReview && reviewReason != null) {
                humanReviewItems.add(
                    CorpusReviewItem(
                        id = "REV-${UUID.randomUUID().toString().take(6)}",
                        title = sampleTitle,
                        artifactFileName = groupRecords.first().fileName,
                        reason = reviewReason,
                        severity = if (groupRecords.any { !it.isParsedSuccessfully }) ReviewSeverity.CRITICAL else ReviewSeverity.INFO,
                        recommendedAction = "Inspect raw artifacts in Corpus Curator before curation."
                    )
                )
            }

            val groupFolder = groupRecords.first().relativePath.substringBeforeLast('/', "")

            baseTitleGroups.add(
                DiscoveredBaseTitleGroup(
                    baseId = "BASE-DISC-${UUID.randomUUID().toString().take(6).uppercase(Locale.US)}",
                    title = sampleTitle,
                    relativeFolder = groupFolder.ifBlank { "Root Directory" },
                    artifacts = groupRecords,
                    lyricCount = lyrics.size,
                    audioCount = audio.size,
                    schemaCount = schema.size,
                    primaryLanguage = primaryLang,
                    isCompletePackage = missingList.isEmpty(),
                    missingComponents = missingList,
                    duplicateCandidates = duplicateWarnings,
                    requiresHumanReview = requiresReview,
                    humanReviewReason = reviewReason
                )
            )
        }

        val successfullyParsed = records.count { it.isParsedSuccessfully }
        val unparsedCount = records.count { !it.isParsedSuccessfully }
        evidenceCounter["Parse Errors / Malformed"] = unparsedCount

        return CorpusInventoryReport(
            scanTimestamp = System.currentTimeMillis(),
            sourceRootUri = sourceRootUri,
            sourceRootDisplayName = sourceRootDisplayName,
            scanStatus = IngestionScanStatus.COMPLETED,
            scanStatusMessage = "Read-Only Discovery Complete. Discovered ${records.size} artifacts across ${baseTitleGroups.size} base titles.",
            totalFilesDiscovered = records.size,
            baseTitlesDiscovered = baseTitleGroups.size,
            versionsDiscovered = versionsCount,
            successfullyParsed = successfullyParsed,
            unparsedCount = unparsedCount,
            duplicateCandidatesCount = duplicateRecordsList.size,
            orphanArtifactsCount = orphanArtifacts.size,
            missingExpectedComponentsCount = missingComponentsCount,
            languageStats = languageCounter,
            evidenceStats = evidenceCounter,
            humanReviewItems = humanReviewItems,
            baseTitleGroups = baseTitleGroups.sortedBy { it.title.lowercase(Locale.US) },
            orphanArtifacts = orphanArtifacts,
            duplicateArtifacts = duplicateRecordsList,
            allArtifacts = records
        )
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
        return "%.1f %s".format(bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
}
