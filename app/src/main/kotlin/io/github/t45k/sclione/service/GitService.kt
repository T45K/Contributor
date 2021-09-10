package io.github.t45k.sclione.service

import org.eclipse.jgit.api.Git
import java.io.File

class GitService(private val repositoryName: String) {
    private fun cloneIfNotExists(): Git =
        Git.cloneRepository()
            .setURI("https://github.com/$repositoryName.git")
            .setDirectory(File("./strage/$repositoryName"))
            .setCloneAllBranches(true)
            .call()
}
