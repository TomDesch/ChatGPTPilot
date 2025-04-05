package org.stealingdapenta.git

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

object ProjectFileWriter {
    fun writeFile(project: Project, relativePath: String, content: String, onComplete: () -> Unit = {}) {
        val basePath = project.basePath ?: return
        val targetFile = File(basePath, relativePath)

        WriteCommandAction.runWriteCommandAction(project) {
            targetFile.parentFile.mkdirs()
            targetFile.writeText(content)

            // Refresh IntelliJ's view
            val virtualFile: VirtualFile = VfsUtil.findFileByIoFile(targetFile, true) ?: return@runWriteCommandAction
            ApplicationManager.getApplication().invokeLater {
                virtualFile.refresh(true, false)
                onComplete()
            }
        }
    }
}
