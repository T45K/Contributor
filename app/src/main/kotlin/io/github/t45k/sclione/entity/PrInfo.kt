package io.github.t45k.sclione.entity

/**
 * マージ済みのコミットを扱うので，headとbaseだとその間の関係ない変更も含んでしまう
 * マージコミットとそれの親コミットを使う
 */
data class PrInfo(
    val number: Int,
    val mergeCommitSha: String,
)
