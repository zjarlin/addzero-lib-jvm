package site.addzero.kcp.transformoverload.idea

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsListener
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.psi.PsiManager
import com.intellij.util.Alarm
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.pathString

@Service(Service.Level.PROJECT)
class TransformOverloadStubService(
    private val project: Project,
) {

    private val logger = Logger.getInstance(TransformOverloadStubService::class.java)
    private val refreshAlarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, project)

    @Volatile
    private var sourceRoot: VirtualFile? = null

    init {
        val connection = project.messageBus.connect(project)
        connection.subscribe(
            VirtualFileManager.VFS_CHANGES,
            object : BulkFileListener {
                override fun after(events: List<VFileEvent>) {
                    if (events.any(::shouldRefreshFor)) {
                        scheduleRefresh()
                    }
                }
            },
        )
    }

    fun getSourceRoots(): Collection<VirtualFile> = listOfNotNull(sourceRoot)

    fun getRootsToWatch(): Collection<VirtualFile> = listOfNotNull(sourceRoot)

    fun scheduleRefresh() {
        if (project.isDisposed) {
            return
        }
        refreshAlarm.cancelAllRequests()
        refreshAlarm.addRequest(
            {
                if (project.isDisposed) {
                    return@addRequest
                }
                if (DumbService.getInstance(project).isDumb) {
                    scheduleRefresh()
                    return@addRequest
                }
                refreshNow()
            },
            400,
        )
    }

    private fun notifyRootsChanged(
        oldRoots: List<VirtualFile>,
    ) {
        ApplicationManager.getApplication().invokeLater {
            if (project.isDisposed) {
                return@invokeLater
            }
            ApplicationManager.getApplication().runWriteAction {
                AdditionalLibraryRootsListener.fireAdditionalLibraryChanged(
                    project,
                    javaClass.name,
                    oldRoots,
                    getSourceRoots(),
                    "transform overload IDE stubs refreshed",
                )
            }
        }
    }

    private fun refreshNow() {
        val generatedFiles = ApplicationManager.getApplication().runReadAction<List<IdeGeneratedFile>> {
            if (project.isDisposed) {
                emptyList()
            } else {
                TransformOverloadStubGenerator(project).generate()
            }
        }

        val outputRoot = resolveOutputRoot()
        val oldRoots = getSourceRoots().toList()

        try {
            syncOutputRoot(outputRoot, generatedFiles)
            val refreshedRoot = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(outputRoot)
            sourceRoot = refreshedRoot?.takeIf { generatedFiles.isNotEmpty() }
            logger.info(
                "Transform overload IDEA stubs refreshed: ${generatedFiles.size} file(s) in $outputRoot",
            )
        } catch (ex: Exception) {
            logger.warn("Failed to refresh transform-overload IDE stubs", ex)
            return
        }

        notifyRootsChanged(oldRoots)
        restartAnalysis()
    }

    private fun resolveOutputRoot(): Path {
        val basePath = project.basePath ?: error("Project base path is unavailable")
        val outputRoot = Paths.get(basePath, TransformOverloadIdeaConstants.stubRootRelativePath)
        Files.createDirectories(outputRoot)
        return outputRoot
    }

    private fun syncOutputRoot(
        outputRoot: Path,
        generatedFiles: List<IdeGeneratedFile>,
    ) {
        Files.createDirectories(outputRoot)
        val expectedPaths = generatedFiles.associateBy { file ->
            outputRoot.resolve(file.relativePath)
        }

        if (Files.exists(outputRoot)) {
            Files.walk(outputRoot).use { paths ->
                paths.filter { path -> Files.isRegularFile(path) }
                    .sorted(Comparator.reverseOrder())
                    .forEach { path ->
                        if (path !in expectedPaths.keys) {
                            Files.deleteIfExists(path)
                        }
                    }
            }
        }

        expectedPaths.forEach { (path, file) ->
            Files.createDirectories(path.parent)
            path.toFile().writeText(file.content)
        }
    }

    private fun shouldRefreshFor(event: VFileEvent): Boolean {
        if (!event.path.endsWith(".kt") && !event.path.endsWith(".kts")) {
            return false
        }
        val outputRoot = project.basePath?.let { basePath ->
            Paths.get(basePath, TransformOverloadIdeaConstants.stubRootRelativePath).pathString
        } ?: return true
        return !event.path.startsWith(outputRoot)
    }

    private fun restartAnalysis() {
        ApplicationManager.getApplication().invokeLater {
            if (project.isDisposed) {
                return@invokeLater
            }
            PsiManager.getInstance(project).dropPsiCaches()
            DaemonCodeAnalyzer.getInstance(project).restart()
        }
    }
}
