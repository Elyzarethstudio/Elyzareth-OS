package com.example.engine

import com.example.model.AppId
import com.example.model.TenantLifecycleState
import com.example.model.TenantResourceMetrics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * TenantLifecycleManager enforces the lifecycle states of applications within One Space.
 * It manages memory allocation, process tracking, thread handles, and ensures that when
 * a tenant window is closed, its memory, coroutines, and resources are properly released.
 */
class TenantLifecycleManager(
    private val scope: CoroutineScope,
    private val onAuditLog: (layer: String, message: String, hashStamp: String) -> Unit
) {

    private val tenantJobs = mutableMapOf<AppId, MutableList<Job>>()

    private val _tenantMetrics = MutableStateFlow<Map<AppId, TenantResourceMetrics>>(
        AppId.values().associateWith { appId ->
            TenantResourceMetrics(
                appId = appId,
                lifecycleState = TenantLifecycleState.DORMANT,
                allocatedMemoryMb = 0f,
                activeThreads = 0,
                openHandles = 0,
                launchCount = 0
            )
        }
    )
    val tenantMetrics: StateFlow<Map<AppId, TenantResourceMetrics>> = _tenantMetrics.asStateFlow()

    private fun getDefaultMemoryForApp(appId: AppId): Float {
        return when (appId) {
            AppId.LYRIC_GENERATOR -> 16.5f
            AppId.CORPUS_CURATOR -> 18.2f
            AppId.INTEGRATOR -> 28.4f
            AppId.ENGINE_TERMINAL -> 14.1f
            AppId.SPACE_ARCHIVE -> 12.0f
        }
    }

    private fun getDefaultThreadsForApp(appId: AppId): Int {
        return when (appId) {
            AppId.LYRIC_GENERATOR -> 2
            AppId.CORPUS_CURATOR -> 2
            AppId.INTEGRATOR -> 4
            AppId.ENGINE_TERMINAL -> 3
            AppId.SPACE_ARCHIVE -> 1
        }
    }

    /**
     * Allocates resources and transitions the tenant from DORMANT -> INITIALIZING -> ACTIVE_FOREGROUND.
     */
    fun allocateAndLaunch(appId: AppId, previouslyFocused: AppId? = null): TenantResourceMetrics {
        val currentMap = _tenantMetrics.value.toMutableMap()
        
        // Put previous focused app in background if it is running
        if (previouslyFocused != null && previouslyFocused != appId) {
            val prev = currentMap[previouslyFocused]
            if (prev != null && prev.lifecycleState == TenantLifecycleState.ACTIVE_FOREGROUND) {
                currentMap[previouslyFocused] = prev.copy(lifecycleState = TenantLifecycleState.ACTIVE_BACKGROUND)
            }
        }

        val existing = currentMap[appId] ?: TenantResourceMetrics(appId = appId)
        val initialMem = getDefaultMemoryForApp(appId)
        val initialThreads = getDefaultThreadsForApp(appId)
        
        val updated = existing.copy(
            lifecycleState = TenantLifecycleState.ACTIVE_FOREGROUND,
            allocatedMemoryMb = initialMem,
            activeThreads = initialThreads,
            openHandles = (2..5).random(),
            launchCount = existing.launchCount + 1,
            lastStateChange = System.currentTimeMillis()
        )
        
        currentMap[appId] = updated
        _tenantMetrics.value = currentMap

        val hash = ElyzarethGovernanceEngine.generateHash("TENANT_ALLOC::${appId.name}::${System.currentTimeMillis()}")
        onAuditLog(
            "TENANT_MGR",
            "Tenant '${appId.shortName}' (PID ${updated.processId}) allocated ${"%.1f".format(initialMem)} MB RAM.",
            hash
        )

        return updated
    }

    /**
     * Transitions a tenant to focused foreground, moving others to background.
     */
    fun setForeground(appId: AppId) {
        val currentMap = _tenantMetrics.value.toMutableMap()
        currentMap.forEach { (id, metrics) ->
            if (id == appId) {
                if (metrics.lifecycleState != TenantLifecycleState.DORMANT) {
                    currentMap[id] = metrics.copy(
                        lifecycleState = TenantLifecycleState.ACTIVE_FOREGROUND,
                        lastStateChange = System.currentTimeMillis()
                    )
                }
            } else if (metrics.lifecycleState == TenantLifecycleState.ACTIVE_FOREGROUND) {
                currentMap[id] = metrics.copy(
                    lifecycleState = TenantLifecycleState.ACTIVE_BACKGROUND,
                    lastStateChange = System.currentTimeMillis()
                )
            }
        }
        _tenantMetrics.value = currentMap
    }

    /**
     * Transitions a tenant to minimized state.
     */
    fun setMinimized(appId: AppId) {
        val currentMap = _tenantMetrics.value.toMutableMap()
        val metrics = currentMap[appId] ?: return
        if (metrics.lifecycleState != TenantLifecycleState.DORMANT) {
            currentMap[appId] = metrics.copy(
                lifecycleState = TenantLifecycleState.MINIMIZED,
                allocatedMemoryMb = (metrics.allocatedMemoryMb * 0.75f).coerceAtLeast(4f), // Compact memory
                lastStateChange = System.currentTimeMillis()
            )
            _tenantMetrics.value = currentMap
        }
    }

    /**
     * Terminates the tenant, cancels all active coroutine jobs, releases memory buffers,
     * resets handles, and transitions state to DORMANT.
     */
    fun terminateAndRelease(
        appId: AppId,
        onCleanup: () -> Unit = {}
    ): Float {
        val currentMap = _tenantMetrics.value.toMutableMap()
        val metrics = currentMap[appId] ?: return 0f
        val freedMemory = metrics.allocatedMemoryMb

        // Cancel any registered coroutines for this tenant
        tenantJobs[appId]?.forEach { job ->
            if (job.isActive) job.cancel()
        }
        tenantJobs[appId]?.clear()

        // Execute tenant-specific cleanup callback
        onCleanup()

        // Reset metrics to DORMANT / 0 resources
        currentMap[appId] = metrics.copy(
            lifecycleState = TenantLifecycleState.DORMANT,
            allocatedMemoryMb = 0f,
            activeThreads = 0,
            openHandles = 0,
            lastStateChange = System.currentTimeMillis()
        )
        _tenantMetrics.value = currentMap

        val hash = ElyzarethGovernanceEngine.generateHash("TENANT_RELEASE::${appId.name}::${System.currentTimeMillis()}")
        onAuditLog(
            "TENANT_MGR",
            "Tenant '${appId.shortName}' (PID ${metrics.processId}) terminated. Memory freed: ${"%.1f".format(freedMemory)} MB. Handles released.",
            hash
        )

        return freedMemory
    }

    /**
     * Purges background caches and dormant buffers across all tenants.
     */
    fun purgeDormantMemory(): Float {
        var totalFreed = 0f
        val currentMap = _tenantMetrics.value.toMutableMap()
        currentMap.forEach { (id, metrics) ->
            if (metrics.lifecycleState == TenantLifecycleState.DORMANT && metrics.allocatedMemoryMb > 0f) {
                totalFreed += metrics.allocatedMemoryMb
                currentMap[id] = metrics.copy(allocatedMemoryMb = 0f)
            } else if (metrics.lifecycleState == TenantLifecycleState.MINIMIZED) {
                val freed = metrics.allocatedMemoryMb * 0.2f
                totalFreed += freed
                currentMap[id] = metrics.copy(allocatedMemoryMb = metrics.allocatedMemoryMb - freed)
            }
        }
        _tenantMetrics.value = currentMap
        val hash = ElyzarethGovernanceEngine.generateHash("MEMORY_PURGE::${System.currentTimeMillis()}")
        onAuditLog("OS_KERNEL", "One Space Memory Garbage Collection. Reclaimed ${"%.1f".format(totalFreed)} MB.", hash)
        return totalFreed
    }

    /**
     * Registers a coroutine job to a specific tenant so it can be safely cancelled upon close.
     */
    fun registerTenantJob(appId: AppId, job: Job) {
        val list = tenantJobs.getOrPut(appId) { mutableListOf() }
        list.add(job)
    }

    /**
     * Total allocated memory across all active tenants.
     */
    fun getTotalAllocatedMemoryMb(): Float {
        return _tenantMetrics.value.values.sumOf { it.allocatedMemoryMb.toDouble() }.toFloat()
    }

    /**
     * Total active threads across all active tenants.
     */
    fun getTotalActiveThreads(): Int {
        return _tenantMetrics.value.values.sumOf { it.activeThreads }
    }
}
