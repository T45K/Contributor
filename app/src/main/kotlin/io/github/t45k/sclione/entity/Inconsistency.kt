package io.github.t45k.sclione.entity

data class Inconsistency(
    val modifiedCode: CodeBlock,
    val unModifiedCodeList: List<CodeBlock>
)
