package site.addzero.configcenter.ktor.routes

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import site.addzero.configcenter.runtime.ConfigCenterBootstrap
import site.addzero.configcenter.spec.ConfigCenterGateway
import site.addzero.configcenter.spec.ConfigDomain
import site.addzero.configcenter.spec.ConfigMutationRequest
import site.addzero.configcenter.spec.ConfigQuery
import site.addzero.configcenter.spec.ConfigSnapshotDto
import site.addzero.configcenter.spec.ConfigTargetDto
import site.addzero.configcenter.spec.ConfigTargetMutationRequest
import site.addzero.configcenter.spec.ConfigValueResponse
import site.addzero.configcenter.spec.RenderedConfig

/**
 * 配置中心路由处理器。
 */

/**
 * 读取最终解析后的单个配置值。
 */
@GetMapping("/api/config-center/env")
fun getConfigCenterEnv(
    @RequestParam("key") key: String,
    @RequestParam("namespace") namespace: String?,
    @RequestParam("profile") profile: String?,
    @RequestParam("domain") domain: String?,
): ConfigValueResponse {
    val query = ConfigQuery(
        namespace = namespace,
        profile = profile?.ifBlank { "default" } ?: "default",
        domain = domain?.toConfigDomainOrNull(),
    )
    val value = kotlinx.coroutines.runBlocking {
        gateway().getEnv(key, query)
    }
    return ConfigValueResponse(key = key, value = value)
}

/**
 * 读取指定命名空间的最终快照。
 */
@GetMapping("/api/config-center/snapshot")
fun getConfigCenterSnapshot(
    @RequestParam("namespace") namespace: String?,
    @RequestParam("profile") profile: String?,
): ConfigSnapshotDto {
    val snapshot = kotlinx.coroutines.runBlocking {
        gateway().getSnapshot(
            namespace = namespace,
            profile = profile?.ifBlank { "default" } ?: "default",
        )
    }
    return ConfigSnapshotDto(snapshot)
}

/**
 * 列出配置项物理记录。
 */
@GetMapping("/api/config-center/entries")
fun listConfigCenterEntries(
    @RequestParam("namespace") namespace: String?,
    @RequestParam("domain") domain: String?,
    @RequestParam("profile") profile: String?,
    @RequestParam("keyword") keyword: String?,
    @RequestParam("includeDisabled") includeDisabled: String?,
): List<site.addzero.configcenter.spec.ConfigEntryDto> {
    return kotlinx.coroutines.runBlocking {
        gateway().listEntries(
            ConfigQuery(
                namespace = namespace,
                domain = domain?.toConfigDomainOrNull(),
                profile = profile?.ifBlank { "default" } ?: "default",
                keyword = keyword,
                includeDisabled = includeDisabled?.toBoolean() ?: false,
            ),
        )
    }
}

/**
 * 按 id 读取配置项。
 */
@GetMapping("/api/config-center/entries/{id}")
fun getConfigCenterEntry(
    @PathVariable("id") id: String,
): site.addzero.configcenter.spec.ConfigEntryDto {
    return requireNotNull(
        kotlinx.coroutines.runBlocking {
            gateway().getEntry(id)
        },
    ) {
        "未找到配置项：$id"
    }
}

/**
 * 新增配置项。
 */
@PostMapping("/api/config-center/entries")
fun createConfigCenterEntry(
    @RequestBody request: ConfigMutationRequest,
): site.addzero.configcenter.spec.ConfigEntryDto {
    return kotlinx.coroutines.runBlocking {
        gateway().addEnv(request)
    }
}

/**
 * 更新配置项。
 */
@PutMapping("/api/config-center/entries/{id}")
fun updateConfigCenterEntry(
    @PathVariable("id") id: String,
    @RequestBody request: ConfigMutationRequest,
): site.addzero.configcenter.spec.ConfigEntryDto {
    return kotlinx.coroutines.runBlocking {
        gateway().updateEnv(id, request)
    }
}

/**
 * 删除配置项。
 */
@DeleteMapping("/api/config-center/entries/{id}")
fun deleteConfigCenterEntry(
    @PathVariable("id") id: String,
): ConfigValueResponse {
    kotlinx.coroutines.runBlocking {
        gateway().deleteEnv(id)
    }
    return ConfigValueResponse(key = id, value = "deleted")
}

/**
 * 列出渲染目标。
 */
@GetMapping("/api/config-center/targets")
fun listConfigCenterTargets(): List<ConfigTargetDto> {
    return kotlinx.coroutines.runBlocking {
        gateway().listTargets()
    }
}

/**
 * 按 id 读取渲染目标。
 */
@GetMapping("/api/config-center/targets/{id}")
fun getConfigCenterTarget(
    @PathVariable("id") id: String,
): ConfigTargetDto {
    return requireNotNull(
        kotlinx.coroutines.runBlocking {
            gateway().getTarget(id)
        },
    ) {
        "未找到渲染目标：$id"
    }
}

/**
 * 新增或更新渲染目标。
 */
@PostMapping("/api/config-center/targets")
fun saveConfigCenterTarget(
    @RequestBody request: ConfigTargetMutationRequest,
): ConfigTargetDto {
    return kotlinx.coroutines.runBlocking {
        gateway().saveTarget(request)
    }
}

/**
 * 更新渲染目标。
 */
@PutMapping("/api/config-center/targets/{id}")
fun updateConfigCenterTarget(
    @PathVariable("id") id: String,
    @RequestBody request: ConfigTargetMutationRequest,
): ConfigTargetDto {
    return kotlinx.coroutines.runBlocking {
        gateway().saveTarget(request.copy(id = id))
    }
}

/**
 * 删除渲染目标。
 */
@DeleteMapping("/api/config-center/targets/{id}")
fun deleteConfigCenterTarget(
    @PathVariable("id") id: String,
): ConfigValueResponse {
    kotlinx.coroutines.runBlocking {
        gateway().deleteTarget(id)
    }
    return ConfigValueResponse(key = id, value = "deleted")
}

/**
 * 预览渲染目标输出。
 */
@PostMapping("/api/config-center/render/{targetId}/preview")
fun previewConfigCenterTarget(
    @PathVariable("targetId") targetId: String,
): RenderedConfig {
    return kotlinx.coroutines.runBlocking {
        gateway().renderTarget(targetId)
    }
}

/**
 * 导出渲染目标到文件。
 */
@PostMapping("/api/config-center/render/{targetId}/export")
fun exportConfigCenterTarget(
    @PathVariable("targetId") targetId: String,
): RenderedConfig {
    return kotlinx.coroutines.runBlocking {
        gateway().exportTarget(targetId)
    }
}

/**
 * 暴露启动期只读 bootstrap 值。
 */
@GetMapping("/api/config-center/bootstrap/{key}")
fun getBootstrapValue(
    @PathVariable("key") key: String,
): ConfigValueResponse {
    return ConfigValueResponse(
        key = key,
        value = bootstrap().readBootstrapValue(key),
    )
}

private fun gateway(): ConfigCenterGateway {
    return KoinPlatform.getKoin().get()
}

private fun bootstrap(): ConfigCenterBootstrap {
    return KoinPlatform.getKoin().get()
}

private fun String.toConfigDomainOrNull(): ConfigDomain? {
    return runCatching { ConfigDomain.valueOf(this) }.getOrNull()
}
