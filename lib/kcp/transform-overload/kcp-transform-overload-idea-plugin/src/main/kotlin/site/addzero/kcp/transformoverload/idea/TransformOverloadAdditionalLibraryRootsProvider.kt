package site.addzero.kcp.transformoverload.idea

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.roots.SyntheticLibrary
import com.intellij.openapi.vfs.VirtualFile

class TransformOverloadAdditionalLibraryRootsProvider : AdditionalLibraryRootsProvider() {

    override fun getAdditionalProjectLibraries(
        project: Project,
    ): Collection<SyntheticLibrary> {
        val roots = project.service<TransformOverloadStubService>().getSourceRoots().toList()
        return if (roots.isEmpty()) {
            emptyList()
        } else {
            listOf(
                SyntheticLibrary.newImmutableLibrary(
                    javaClass.name,
                    roots,
                    emptyList(),
                    emptySet(),
                    null,
                ),
            )
        }
    }

    override fun getRootsToWatch(
        project: Project,
    ): Collection<VirtualFile> {
        return project.service<TransformOverloadStubService>().getRootsToWatch()
    }
}
