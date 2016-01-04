package io.koff.generator

/**
 * Wrapper for throwables in order to send it as actor message
 * @param throwable
 */
case class ExceptionMessage(throwable: Throwable)
