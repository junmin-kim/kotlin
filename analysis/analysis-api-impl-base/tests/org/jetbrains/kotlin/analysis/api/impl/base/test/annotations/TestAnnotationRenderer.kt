/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.annotations

import org.jetbrains.kotlin.analysis.api.annotations.KtAnnotationsList
import org.jetbrains.kotlin.analysis.api.impl.base.test.indented
import org.jetbrains.kotlin.analysis.api.symbols.DebugSymbolRenderer

object TestAnnotationRenderer {
    fun renderAnnotations(annotations: KtAnnotationsList) = buildString {
        appendLine("annotations: [")
        for (annotation in annotations.annotations) {
            appendLine(DebugSymbolRenderer.renderAnnotationApplication(annotation).indented(indent = 2))
        }
        appendLine("]")
    }
}