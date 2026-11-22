package org.kite9.diagram.logging

interface Logable {
    val prefix: String?
    val isLoggingEnabled: Boolean
}