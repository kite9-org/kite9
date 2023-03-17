package com.kite9.pipeline.command

/**
 * This is thrown when something goes wrong in command processing.
 */
class CommandException : Exception {

    private var c: List<Command>
    private var status: Int

    constructor(status: Int, message: String, c: Command, cause: Throwable? = null) : super(message, cause) {
        this.status = status
        this.c = listOf(c)
    }

    constructor(status: Int, message: String, cause: Throwable? = null, c: List<Command>) : super(message, cause) {
        this.status = status
        this.c = c
    }
}