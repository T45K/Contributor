package io.github.t45k.contributor.entity

import java.nio.file.Path

data class RepositoryName(val name: String) {
    init {
        name.takeIf { Regex(".+/.+").matches(it) } ?: throw InvalidRepositoryNameException(name)
    }

    fun toLocalPath(): Path = Path.of("./storage/${name}")
    fun toGitHubUrl(): String = "https://github.com/$name"
}

class InvalidRepositoryNameException(name: String) : RuntimeException() {
    override val message: String = "$name is invalid repository name"
}
