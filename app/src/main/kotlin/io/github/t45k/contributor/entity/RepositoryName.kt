package io.github.t45k.contributor.entity

import java.nio.file.Path

data class RepositoryName(val name: String) {
    init {
        name.takeIf(Regex(".+/.+")::matches) ?: throw InvalidRepositoryNameException(name)
    }

    val localPath: Path by lazy { Path.of(".", "storage", name) }
    val gitHubUrl: String by lazy { "https://github.com/$name" }
}

class InvalidRepositoryNameException(name: String) : RuntimeException() {
    override val message: String = "$name is invalid repository name"
}
