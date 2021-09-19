package io.github.t45k.sclione.service

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffAlgorithm
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.Edit
import org.eclipse.jgit.diff.RawText
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.AbbreviatedObjectId
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.filter.RevFilter
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.util.io.DisabledOutputStream
import java.io.File
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.exists

@ExperimentalPathApi
class GitService(private val repositoryName: String) {
    private val git = cloneIfNotExists()
    private val repositoryPath: Path = Path.of("./storage/$repositoryName")

    private fun cloneIfNotExists(): Git =
        if (repositoryPath.exists()) {
            Git(FileRepository(repositoryPath.resolve(".git").toFile()))
                .apply { this.pull().call() }
        } else {
            Git.cloneRepository()
                .setURI("https://github.com/$repositoryName.git")
                .setDirectory(File("./storage/$repositoryName"))
                .call()
        }

    fun checkout(commitHash: String) {
        git.checkout()
            .setName(commitHash)
            .call()
    }

    fun calcFileDiff(path: Path, baseCommitSha: String, mergedCommitSha: String): List<Edit> {
        val fileName = repositoryPath.relativize(path).toString()
        val entry: DiffEntry = executeDiffCommand(baseCommitSha, mergedCommitSha)
            .first { it.oldPath == fileName }
            .takeIf { it.changeType == DiffEntry.ChangeType.MODIFY }
            ?: return emptyList()

        val oldText: RawText = readBlob(entry.oldId)
        val newText: RawText = readBlob(entry.newId)
        return DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.MYERS)
            .diff(RawTextComparator.DEFAULT, oldText, newText)
            ?: emptyList()
    }

    private fun executeDiffCommand(oldCommitHash: String, newCommitHash: String): List<DiffEntry> {
        val oldTreeParser = prepareTreeParser(ObjectId.fromString(oldCommitHash))
        val newTreeParser = prepareTreeParser(ObjectId.fromString(newCommitHash))
        return DiffFormatter(DisabledOutputStream.INSTANCE)
            .apply { this.setRepository(git.repository) }
            .apply { this.setDiffComparator(RawTextComparator.DEFAULT) }
            .apply { this.isDetectRenames = true }
            .scan(oldTreeParser, newTreeParser)
    }

    private fun readBlob(blobId: AbbreviatedObjectId): RawText =
        git.repository.newObjectReader()
            .open(blobId.toObjectId(), Constants.OBJ_BLOB)
            .run { RawText(this.cachedBytes) }

    fun getMergeBasedParentCommitSha(commitSha: String): String {
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
