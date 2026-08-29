package com.example.engine

import android.content.Context
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object CorpusPersistenceManager {

    private const val INVENTORY_FILE_NAME = "elyzareth_corpus_inventory.json"

    fun saveReport(context: Context, report: CorpusInventoryReport) {
        try {
            val rootJson = JSONObject()
            rootJson.put("scanTimestamp", report.scanTimestamp)
            rootJson.put("sourceRootUri", report.sourceRootUri)
            rootJson.put("sourceRootDisplayName", report.sourceRootDisplayName)
            rootJson.put("scanStatus", report.scanStatus.name)
            rootJson.put("scanStatusMessage", report.scanStatusMessage)
            rootJson.put("totalFilesDiscovered", report.totalFilesDiscovered)
            rootJson.put("baseTitlesDiscovered", report.baseTitlesDiscovered)
            rootJson.put("versionsDiscovered", report.versionsDiscovered)
            rootJson.put("successfullyParsed", report.successfullyParsed)
            rootJson.put("unparsedCount", report.unparsedCount)
            rootJson.put("duplicateCandidatesCount", report.duplicateCandidatesCount)
            rootJson.put("orphanArtifactsCount", report.orphanArtifactsCount)
            rootJson.put("missingExpectedComponentsCount", report.missingExpectedComponentsCount)

            // Language Stats
            val langObj = JSONObject()
            report.languageStats.forEach { (k, v) -> langObj.put(k, v) }
            rootJson.put("languageStats", langObj)

            // Evidence Stats
            val evidenceObj = JSONObject()
            report.evidenceStats.forEach { (k, v) -> evidenceObj.put(k, v) }
            rootJson.put("evidenceStats", evidenceObj)

            // Artifacts
            val artifactsArr = JSONArray()
            for (art in report.allArtifacts) {
                val artObj = JSONObject()
                artObj.put("id", art.id)
                artObj.put("documentUri", art.documentUri)
                artObj.put("fileName", art.fileName)
                artObj.put("relativePath", art.relativePath)
                artObj.put("fileExtension", art.fileExtension)
                artObj.put("fileSizeBytes", art.fileSizeBytes)
                artObj.put("lastModified", art.lastModified)
                artObj.put("mimeType", art.mimeType)
                artObj.put("sha256Hash", art.sha256Hash)
                artObj.put("category", art.category.name)
                artObj.put("discoveryState", art.discoveryState.name)
                artObj.put("isParsedSuccessfully", art.isParsedSuccessfully)
                artObj.put("detectedBaseTitle", art.detectedBaseTitle ?: "")
                artObj.put("detectedVersionLabel", art.detectedVersionLabel ?: "")
                artObj.put("detectedLanguage", art.detectedLanguage)
                artObj.put("lineCount", art.lineCount)
                artObj.put("wordCount", art.wordCount)
                artObj.put("characterCount", art.characterCount)
                artObj.put("snippetText", art.snippetText ?: "")
                artObj.put("parseErrorMessage", art.parseErrorMessage ?: "")
                artObj.put("audioFormat", art.audioFormat ?: "")
                artObj.put("isOrphan", art.isOrphan)
                artObj.put("isDuplicateCandidate", art.isDuplicateCandidate)
                artObj.put("requiresHumanReview", art.requiresHumanReview)
                artObj.put("humanReviewReason", art.humanReviewReason ?: "")
                artifactsArr.put(artObj)
            }
            rootJson.put("allArtifacts", artifactsArr)

            // Base Title Groups
            val groupsArr = JSONArray()
            for (group in report.baseTitleGroups) {
                val grpObj = JSONObject()
                grpObj.put("baseId", group.baseId)
                grpObj.put("title", group.title)
                grpObj.put("relativeFolder", group.relativeFolder)
                grpObj.put("lyricCount", group.lyricCount)
                grpObj.put("audioCount", group.audioCount)
                grpObj.put("schemaCount", group.schemaCount)
                grpObj.put("primaryLanguage", group.primaryLanguage)
                grpObj.put("isCompletePackage", group.isCompletePackage)
                grpObj.put("requiresHumanReview", group.requiresHumanReview)
                grpObj.put("humanReviewReason", group.humanReviewReason ?: "")

                val missingArr = JSONArray()
                group.missingComponents.forEach { missingArr.put(it) }
                grpObj.put("missingComponents", missingArr)

                val artIdsArr = JSONArray()
                group.artifacts.forEach { artIdsArr.put(it.id) }
                grpObj.put("artifactIds", artIdsArr)

                groupsArr.put(grpObj)
            }
            rootJson.put("baseTitleGroups", groupsArr)

            val file = File(context.filesDir, INVENTORY_FILE_NAME)
            file.writeText(rootJson.toString(2), Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadSavedReport(context: Context): CorpusInventoryReport? {
        val file = File(context.filesDir, INVENTORY_FILE_NAME)
        if (!file.exists()) return null

        return try {
            val text = file.readText(Charsets.UTF_8)
            val root = JSONObject(text)

            val artifactsMap = mutableMapOf<String, DiscoveredArtifactRecord>()
            val allArtifacts = mutableListOf<DiscoveredArtifactRecord>()
            val artArr = root.optJSONArray("allArtifacts") ?: JSONArray()

            for (i in 0 until artArr.length()) {
                val obj = artArr.getJSONObject(i)
                val cat = try { ArtifactCategory.valueOf(obj.optString("category", "UNKNOWN")) } catch (e: Exception) { ArtifactCategory.UNKNOWN }
                val state = try { IngestionDiscoveryState.valueOf(obj.optString("discoveryState", "DISCOVERED")) } catch (e: Exception) { IngestionDiscoveryState.DISCOVERED }

                val record = DiscoveredArtifactRecord(
                    id = obj.optString("id"),
                    documentUri = obj.optString("documentUri"),
                    fileName = obj.optString("fileName"),
                    relativePath = obj.optString("relativePath"),
                    fileExtension = obj.optString("fileExtension"),
                    fileSizeBytes = obj.optLong("fileSizeBytes"),
                    lastModified = obj.optLong("lastModified"),
                    mimeType = obj.optString("mimeType"),
                    sha256Hash = obj.optString("sha256Hash"),
                    category = cat,
                    discoveryState = state,
                    isParsedSuccessfully = obj.optBoolean("isParsedSuccessfully"),
                    detectedBaseTitle = obj.optString("detectedBaseTitle").ifBlank { null },
                    detectedVersionLabel = obj.optString("detectedVersionLabel").ifBlank { null },
                    detectedLanguage = obj.optString("detectedLanguage", "Uncertain"),
                    lineCount = obj.optInt("lineCount"),
                    wordCount = obj.optInt("wordCount"),
                    characterCount = obj.optInt("characterCount"),
                    snippetText = obj.optString("snippetText").ifBlank { null },
                    parseErrorMessage = obj.optString("parseErrorMessage").ifBlank { null },
                    audioFormat = obj.optString("audioFormat").ifBlank { null },
                    isOrphan = obj.optBoolean("isOrphan"),
                    isDuplicateCandidate = obj.optBoolean("isDuplicateCandidate"),
                    requiresHumanReview = obj.optBoolean("requiresHumanReview"),
                    humanReviewReason = obj.optString("humanReviewReason").ifBlank { null }
                )
                artifactsMap[record.id] = record
                allArtifacts.add(record)
            }

            val groupsArr = root.optJSONArray("baseTitleGroups") ?: JSONArray()
            val baseTitleGroups = mutableListOf<DiscoveredBaseTitleGroup>()

            for (i in 0 until groupsArr.length()) {
                val grpObj = groupsArr.getJSONObject(i)
                val artIds = grpObj.optJSONArray("artifactIds") ?: JSONArray()
                val groupArts = mutableListOf<DiscoveredArtifactRecord>()
                for (j in 0 until artIds.length()) {
                    val aId = artIds.getString(j)
                    artifactsMap[aId]?.let { groupArts.add(it) }
                }

                val missingArr = grpObj.optJSONArray("missingComponents") ?: JSONArray()
                val missingList = mutableListOf<String>()
                for (m in 0 until missingArr.length()) {
                    missingList.add(missingArr.getString(m))
                }

                baseTitleGroups.add(
                    DiscoveredBaseTitleGroup(
                        baseId = grpObj.optString("baseId"),
                        title = grpObj.optString("title"),
                        relativeFolder = grpObj.optString("relativeFolder"),
                        artifacts = groupArts,
                        lyricCount = grpObj.optInt("lyricCount"),
                        audioCount = grpObj.optInt("audioCount"),
                        schemaCount = grpObj.optInt("schemaCount"),
                        primaryLanguage = grpObj.optString("primaryLanguage"),
                        isCompletePackage = grpObj.optBoolean("isCompletePackage"),
                        missingComponents = missingList,
                        duplicateCandidates = emptyList(),
                        requiresHumanReview = grpObj.optBoolean("requiresHumanReview"),
                        humanReviewReason = grpObj.optString("humanReviewReason").ifBlank { null }
                    )
                )
            }

            val langMap = mutableMapOf<String, Int>()
            val langObj = root.optJSONObject("languageStats")
            langObj?.keys()?.forEach { k -> langMap[k] = langObj.optInt(k) }

            val evidMap = mutableMapOf<String, Int>()
            val evidObj = root.optJSONObject("evidenceStats")
            evidObj?.keys()?.forEach { k -> evidMap[k] = evidObj.optInt(k) }

            val status = try { IngestionScanStatus.valueOf(root.optString("scanStatus", "COMPLETED")) } catch (e: Exception) { IngestionScanStatus.COMPLETED }

            CorpusInventoryReport(
                scanTimestamp = root.optLong("scanTimestamp"),
                sourceRootUri = root.optString("sourceRootUri"),
                sourceRootDisplayName = root.optString("sourceRootDisplayName"),
                scanStatus = status,
                scanStatusMessage = root.optString("scanStatusMessage"),
                totalFilesDiscovered = root.optInt("totalFilesDiscovered"),
                baseTitlesDiscovered = root.optInt("baseTitlesDiscovered"),
                versionsDiscovered = root.optInt("versionsDiscovered"),
                successfullyParsed = root.optInt("successfullyParsed"),
                unparsedCount = root.optInt("unparsedCount"),
                duplicateCandidatesCount = root.optInt("duplicateCandidatesCount"),
                orphanArtifactsCount = root.optInt("orphanArtifactsCount"),
                missingExpectedComponentsCount = root.optInt("missingExpectedComponentsCount"),
                languageStats = langMap,
                evidenceStats = evidMap,
                baseTitleGroups = baseTitleGroups,
                allArtifacts = allArtifacts
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
