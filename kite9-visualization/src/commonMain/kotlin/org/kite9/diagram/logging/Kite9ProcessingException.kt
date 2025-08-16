package org.kite9.diagram.logging

/**
 * Thrown whenever there is a problem creating the kite9 item.
 * This is to do with input data not matching expectations, and
 * should lead the user back to the understanding of what the issue is.
 *
 * @author moffatr
 */
open class Kite9ProcessingException : RuntimeException
{
    protected constructor(arg0: String, arg1: Throwable?) : super(correctMessage(arg0, arg1), correctThrowable(arg1)) {}
    constructor(arg0: String?) : super(arg0) {}

    companion object {
        /**
         * These prevent us from creating traces with multiple [Kite9ProcessingException]s in them.
         */
        private fun correctMessage(arg0: String, arg1: Throwable?): String {
            return if (arg1 is Kite9ProcessingException) """
     $arg0
     ${arg1.message}
     """.trimIndent() else arg0
        }

        protected fun correctThrowable(arg1: Throwable?): Throwable? {
            return if ((arg1 is Kite9ProcessingException) && (arg1.cause != null)) arg1.cause else arg1
        }
    }
}