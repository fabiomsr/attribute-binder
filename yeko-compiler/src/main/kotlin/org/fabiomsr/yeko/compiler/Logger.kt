package org.fabiomsr.yeko.compiler

import javax.annotation.processing.Messager
import javax.tools.Diagnostic

/**
 * Created by Fabiomsr on 10/9/16.
 */
class Logger(val messager: Messager){
    fun logNote(message: String) {
        messager.printMessage(Diagnostic.Kind.NOTE, message)
    }

    fun logError(message: String) {
        messager.printMessage(Diagnostic.Kind.ERROR, message)
    }
}