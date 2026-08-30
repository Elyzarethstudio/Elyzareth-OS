package com.example.ui.tenants

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ArchiveFile
import com.example.ui.theme.*

@Composable
fun SpaceArchiveApp(
    files: List<ArchiveFile>,
    selectedFile: ArchiveFile?,
    categoryFilter: String,
    onCategorySelect: (String) -> Unit,
    onSelectFile: (ArchiveFile) -> Unit,
    onCopyContent: (ArchiveFile) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val categories = listOf("ALL", "LYRICS", "CORPUS", "PIPELINE_BUNDLE")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                ElyIndigo.copy(alpha = 0.15f),
                                ElyCyan.copy(alpha = 0.10f)
                            )
                        )
                    )
                    .border(1.dp, ElyIndigo.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "APP 05 // SPACE ARCHIVE & FILE EXPLORER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = ElyIndigo
                    )
                    Text(
                        text = "Immutable Cryptographic Repository // One Space",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElyTextPrimary
                    )
                }
            }
        }

        // Categories Pills
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories) { cat ->
                    val isSelected = categoryFilter == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) ElyIndigo.copy(alpha = 0.25f) else ElySurfaceCard)
                            .border(1.dp, if (isSelected) ElyIndigo else Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                            .clickable { onCategorySelect(cat) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) ElyIndigo else ElyTextSecondary
                        )
                    }
                }
            }
        }

        // File List
        val filtered = files.filter {
            if (categoryFilter == "ALL") true else it.category == categoryFilter
        }

        items(filtered) { file ->
            val isSelected = selectedFile?.id == file.id
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) ElyIndigo.copy(alpha = 0.15f) else ElySurfaceCard)
                    .border(1.dp, if (isSelected) ElyIndigo else Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .clickable { onSelectFile(file) }
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = when (file.category) {
                            "LYRICS" -> Icons.Default.MusicNote
                            "CORPUS" -> Icons.Default.MenuBook
                            "PIPELINE_BUNDLE" -> Icons.Default.Cable
                            else -> Icons.Default.InsertDriveFile
                        },
                        contentDescription = null,
                        tint = ElyCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = file.fileName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ElyTextPrimary)
                        Text(text = "${file.originTenant} • ${file.sizeKb} KB", fontSize = 9.sp, color = ElyTextTertiary)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(text = file.g3SealHash.take(16), fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = ElyG3Axiom)
                    }
                }
            }
        }

        // Selected File Inspector
        if (selectedFile != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF090D16))
                        .border(1.dp, ElyIndigo.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "FILE CONTENT VIEWER // ${selectedFile.fileName}", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = ElyIndigo, fontWeight = FontWeight.Bold)
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // 📋 Real Android Clipboard Copy Button
                                Button(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(selectedFile.fullText))
                                        val sysClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        sysClipboard?.setPrimaryClip(ClipData.newPlainText(selectedFile.fileName, selectedFile.fullText))
                                        Toast.makeText(context, "Copied '${selectedFile.fileName}' to clipboard", Toast.LENGTH_SHORT).show()
                                        onCopyContent(selectedFile)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElyHeaderGlass),
                                    border = BorderStroke(0.5.dp, ElyCyan),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp).testTag("btn_archive_copy")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = ElyCyan, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", fontSize = 9.sp, color = ElyTextPrimary)
                                }

                                // 📤 Real Android External Share Button
                                Button(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, selectedFile.fileName)
                                            putExtra(Intent.EXTRA_TEXT, selectedFile.fullText)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share '${selectedFile.fileName}'"))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElyHeaderGlass),
                                    border = BorderStroke(0.5.dp, ElyIndigo),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp).testTag("btn_archive_share")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share", tint = ElyIndigo, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share", fontSize = 9.sp, color = ElyTextPrimary)
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF06090F),
                            border = BorderStroke(0.5.dp, ElyWindowBorderInactive),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SelectionContainer {
                                Text(
                                    text = selectedFile.fullContent,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = ElyTextPrimary,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
