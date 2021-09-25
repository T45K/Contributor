package io.github.t45k.contributor.util

import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

fun Path.walk(): List<Path> = Files.walk(this).toList()
