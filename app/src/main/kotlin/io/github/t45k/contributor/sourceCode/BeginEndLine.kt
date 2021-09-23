package io.github.t45k.contributor.sourceCode

data class BeginEndLine(val beginLine: Int, val endLine: Int) {
    fun overlapWith(beginEndLine: BeginEndLine): Boolean =
        beginEndLine.endLine >= this.beginLine && this.endLine >= beginEndLine.beginLine
}
