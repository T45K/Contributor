package io.github.t45k.contributor.entity

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.filter.RevFilter
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.util.io.DisabledOutputStream

class GitDiff(private val git: Git, private val commit: GitCommit) {
    private val comparison: GitCommit = getMergeBaseCommitSha(commit)

    fun execute(): List<DiffEntry> {
        val oldTreeParser = prepareTreeParser(git.repository.resolve(commit.sha))
        val newTreeParser = prepareTreeParser(git.repository.resolve(comparison.sha))
        return DiffFormatter(DisabledOutputStream.INSTANCE)
            .apply { this.setRepository(git.repository) }
            .apply { this.setDiffComparator(RawTextComparator.DEFAULT) }
            .apply { this.isDetectRenames = true }
            .scan(oldTreeParser, newTreeParser)
    }

    fun getModifiedJavaFiles(): List<DiffEntry> =
        execute().filter { it.changeType == DiffEntry.ChangeType.MODIFY && it.oldPath.endsWith(".java") }

    private fun prepareTreeParser(objectId: ObjectId): AbstractTreeIterator {
        val revWalk = RevWalk(git.repository).also { it.revFilter = RevFilter.MERGE_BASE }
        val commit = revWalk.parseCommit(objectId)
        val tree = revWalk.parseTree(commit.tree.id)
        val treeParser = CanonicalTreeParser()
            .also { it.reset(git.repository.newObjectReader(), tree) }
        revWalk.dispose()
        return treeParser
    }

    private fun getMergeBaseCommitSha(commit: GitCommit): GitCommit {
        val commitId: ObjectId = git.repository.resolve(commit.sha)
        val parentCommitId: ObjectId = git.repository
            .parseCommit(commitId)
            .parents[0]
        return RevWalk(git.repository)
            .apply { this.revFilter = RevFilter.MERGE_BASE }
            .apply { this.markStart(this.parseCommit(parentCommitId)) }
            .apply { this.markStart(this.parseCommit(commitId)) }
            .next()
            .name
            .let { GitCommit(it) }
    }
}
