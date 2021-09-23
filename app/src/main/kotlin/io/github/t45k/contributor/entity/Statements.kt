package io.github.t45k.contributor.entity

data class Statements(val value: List<Int>) {
    fun hasSufficientSize(): Boolean = value.size >= 5
}
