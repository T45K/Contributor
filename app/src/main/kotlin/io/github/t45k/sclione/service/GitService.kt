package io.github.t45k.sclione.service

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.filter.RevFilter
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import java.io.File
import java.nio.file.Path

class GitService(private val repositoryName: String) {
    private val git = cloneIfNotExists()
    private val repositoryPath: Path = Path.of("./storage/$repositoryName")

    private

    fun cloneIfNotExists(): Git =
        Git.cloneRepository()
            .setURI("https://github.com/$repositoryName.git")
            .setDirectory(File("./storage/$repositoryName"))
            .setCloneAllBranches(true)
            .call()

    fun checkout(commitHash: String) {
        git.checkout()
            .setName(commitHash)
            .call()
    }

    fun calcFileDiff(path: Path, mergedCommitSha: String) {
        val fileName = repositoryPath.relativize(path).toString()

    }

    private fun executeDiffCommand(oldCommitHash: String, newCommitHash: String) {
        val newTreeParser = prepareTreeParser(ObjectId.fromString(newCommitHash))

    }

    private fun getMergeBasedParent(commitSha: String): String {
        val commitId: ObjectId = ObjectId.fromString(commitSha)
        val parentCommitId: ObjectId = git.repository
            .parseCommit(commitId)
            .parents[0]
        return RevWalk(git.repository)
            .apply { this.revFilter = RevFilter.MERGE_BASE }
            .apply { this.markStart(this.parseCommit(parentCommitId)) }
            .apply { this.markStart(this.parseCommit(commitId)) }
            .next()
            .name
    }

    private fun getCommonAncestorCommitSha(oldCommitHash: String, newCommitHash: String): String =
        RevWalk(git.repository)
            .apply { this.revFilter = RevFilter.MERGE_BASE }
            .apply { this.markStart(this.parseCommit(ObjectId.fromString(oldCommitHash))) }
            .apply { this.markStart(this.parseCommit(ObjectId.fromString(newCommitHash))) }
            .next()
            .name

    private fun prepareTreeParser(objectId: ObjectId): AbstractTreeIterator {
        val revWalk = RevWalk(git.repository).also { it.revFilter = RevFilter.MERGE_BASE }
        val commit = revWalk.parseCommit(objectId)
        val tree = revWalk.parseTree(commit.tree.id)
        val treeParser = CanonicalTreeParser()
            .also { it.reset(git.repository.newObjectReader(), tree) }
        revWalk.dispose()
        return treeParser
    }
}
