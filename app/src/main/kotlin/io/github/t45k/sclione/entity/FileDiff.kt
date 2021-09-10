package io.github.t45k.sclione.entity

import java.nio.file.Path

data class FileDiff(
    val oldPath: Path?,
    val newPath: Path?,
    val addedLines: List<Int>,
    val deletedLines: List<Int>,
)
