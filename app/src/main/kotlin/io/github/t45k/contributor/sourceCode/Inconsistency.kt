package io.github.t45k.contributor.sourceCode

data class Inconsistency(
    val modifiedCode: CodeBlock,
    val unmodifiedCodeList: List<CodeBlock>
) {
    override fun toString(): String = """
        |mod
        |$modifiedCode
        |unmod
        |${unmodifiedCodeList.joinToString("\n")}
    """.trimMargin()
}
