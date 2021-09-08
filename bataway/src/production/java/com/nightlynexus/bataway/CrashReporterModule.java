package com.nightlynexus.bataway;

import dagger.Module;
import dagger.Provides;

@Module
abstract class CrashReporterModule {
  @Provides static CrashReporter providesCrashReporter() {
    return cause -> {
      // No-op.
    };
  }
}
