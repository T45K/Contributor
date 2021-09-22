package io.github.t45k.contributor.entity

import org.eclipse.jgit.diff.DiffEntry
import java.nio.file.Path

class TrackedJavaFiles(val path: Path, private val diff: List<DiffEntry>) {

    fun extractCodeBlocks() {

    }
}
