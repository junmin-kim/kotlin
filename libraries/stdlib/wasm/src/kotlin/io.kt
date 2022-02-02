/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.io

import kotlin.wasm.internal.*

/** Prints the line separator to the standard output stream. */
public actual fun println() {
    println("")
}

/** Prints the given [message] and the line separator to the standard output stream. */
public actual fun println(message: Any?) {
    printlnImpl(exportString(message.toString()))
}

/** Prints the given [message] to the standard output stream. */
public actual fun print(message: Any?) {
    // TODO: Support print without newline
    println(message)
}

@SinceKotlin("1.6")
public actual fun readln(): String = throw UnsupportedOperationException("readln is not supported in Kotlin/WASM")

@SinceKotlin("1.6")
public actual fun readlnOrNull(): String? = throw UnsupportedOperationException("readlnOrNull is not supported in Kotlin/WASM")

internal actual interface Serializable

@WasmImport("runtime", "println")
private fun printlnImpl(messageAddr: Int): Unit =
    implementedAsIntrinsic

internal fun printError(error: Any?) {
    printErrorImpl(exportString(error.toString()))
}

@WasmImport("runtime", "printError")
private fun printErrorImpl(errorAddr: Int): Unit =
    implementedAsIntrinsic
