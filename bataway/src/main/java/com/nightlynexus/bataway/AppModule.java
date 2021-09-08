package com.nightlynexus.bataway;

import android.app.Application;
import android.app.PendingIntent;
import com.squareup.sqldelight.android.AndroidSqliteDriver;
import com.squareup.sqldelight.db.SqlDriver;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import java.util.LinkedHashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

@Module
abstract class AppModule {
  @AppScope @Provides static EnabledPreference provideEnabledPreference(Application application) {
    return new EnabledPreference(
        application.getSharedPreferences("EnabledPreference", MODE_PRIVATE));
  }

  @AppScope @Provides static Database provideDatabase(Application application) {
    SqlDriver driver =
        new AndroidSqliteDriver(Database.Companion.getSchema(), application, "database.db");
    return Database.Companion.invoke(driver);
  }

  @Provides static AdNotificationQueries provideAdNotificationQueries(Database database) {
    return database.getAdNotificationQueries();
  }

  @AppScope @Provides static @AdNotificationContentIntents
  Map<String, PendingIntent> provideAdNotificationContentIntents() {
    return new LinkedHashMap<>();
  }

  @ContributesAndroidInjector
  abstract BatAwayListenerService contributeBatAwayListenerService();

  @ContributesAndroidInjector abstract BatAwayActivity contributeBatAwayActivity();
}
