package com.nightlynexus.bataway

internal interface CrashReporter {
  fun report(cause: Throwable)
}
