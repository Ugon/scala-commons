package com.avsystem.commons
package redis

/**
  * Author: ghik
  * Created: 06/10/16.
  */
object SeparateThreadExecutionContext extends ExecutionContext {
  def execute(runnable: Runnable) = new Thread(runnable).start()
  def reportFailure(cause: Throwable) = cause.printStackTrace()
  def submit[T](code: => T): Future[T] = Future(code)(this)
}
