package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppId(
    val title: String,
    val shortName: String,
    val subtitle: String,
    val tenantNumber: String,
    val defaultIcon: ImageVector,
    val defaultWidth: Float = 360f,
    val defaultHeight: Float = 520f
) {
    LYRIC_GENERATOR(
        title = "Lyric Studio",
        shortName = "Lyric Studio",
        subtitle = "Songwriting & Meter Generator",
        tenantNumber = "TENANT 01",
        defaultIcon = Icons.Filled.AutoAwesome,
        defaultWidth = 360f,
        defaultHeight = 520f
    ),
    CORPUS_CURATOR(
        title = "Sitting Room",
        shortName = "Sitting Room",
        subtitle = "Lyric Curator & Review Studio",
        tenantNumber = "TENANT 02",
        defaultIcon = Icons.Filled.MenuBook,
        defaultWidth = 400f,
        defaultHeight = 540f
    ),
    INTEGRATOR(
        title = "Axiom Integrator",
        shortName = "Integrator",
        subtitle = "Cross-Song Pipeline & Studio Bridge",
        tenantNumber = "TENANT 03",
        defaultIcon = Icons.Filled.Cable,
        defaultWidth = 370f,
        defaultHeight = 530f
    ),
    ENGINE_TERMINAL(
        title = "Governance Matrix",
        shortName = "Governance",
        subtitle = "Quality Standards & Rule Verifier",
        tenantNumber = "TENANT 04",
        defaultIcon = Icons.Filled.Terminal,
        defaultWidth = 360f,
        defaultHeight = 510f
    ),
    SPACE_ARCHIVE(
        title = "Space Archive",
        shortName = "Archive",
        subtitle = "Song Library & Sound Vault",
        tenantNumber = "TENANT 05",
        defaultIcon = Icons.Filled.FolderSpecial,
        defaultWidth = 360f,
        defaultHeight = 500f
    )
}

enum class VerificationState {
    PENDING,
    ACTIVE,
    VERIFIED,
    WARNING,
    FAILED
}

enum class TenantLifecycleState(val label: String) {
    DORMANT("Dormant // Released"),
    INITIALIZING("Initializing Space"),
    ACTIVE_FOREGROUND("Active // Foreground"),
    ACTIVE_BACKGROUND("Active // Background"),
    MINIMIZED("Minimized // Suspended"),
    TERMINATING("Terminating // Freeing Memory")
}

data class TenantResourceMetrics(
    val appId: AppId,
    val lifecycleState: TenantLifecycleState = TenantLifecycleState.DORMANT,
    val allocatedMemoryMb: Float = 0f,
    val activeThreads: Int = 0,
    val openHandles: Int = 0,
    val launchCount: Int = 0,
    val processId: Int = 1000 + (appId.ordinal * 23),
    val lastStateChange: Long = System.currentTimeMillis()
)

data class WindowData(
    val appId: AppId,
    val isMinimized: Boolean = false,
    val isMaximized: Boolean = false,
    val isClosed: Boolean = false,
    val offsetX: Float = 16f,
    val offsetY: Float = 32f,
    val width: Float = 360f,
    val height: Float = 520f,
    val zIndex: Float = 1f,
    val lifecycleState: TenantLifecycleState = TenantLifecycleState.ACTIVE_FOREGROUND,
    val allocatedMemoryMb: Float = 14.5f,
    val g1Status: VerificationState = VerificationState.VERIFIED,
    val g2Status: VerificationState = VerificationState.VERIFIED,
    val g3Status: VerificationState = VerificationState.VERIFIED,
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)
