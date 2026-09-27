package com.smashedtatoes.stepcount.data.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferenceKeys {
        val DAILY_GOAL = intPreferencesKey("daily_goal")
        val ACTIVE_TRACKING_DATE = stringPreferencesKey("active_tracking_date")
        val BASELINE_SENSOR_READING = intPreferencesKey("baseline_sensor_reading")
        val STORED_STEPS_BASE = intPreferencesKey("stored_steps_base")
        val USE_MILES = booleanPreferencesKey("use_miles")
    }

    val dailyGoalFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.DAILY_GOAL] ?: 10000
    }

    val activeTrackingDateFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.ACTIVE_TRACKING_DATE] ?: ""
    }

    val baselineSensorReadingFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.BASELINE_SENSOR_READING] ?: -1
    }

    val storedStepsBaseFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.STORED_STEPS_BASE] ?: 0
    }

    val useMilesFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.USE_MILES] ?: false
    }

    suspend fun updateDailyGoal(goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.DAILY_GOAL] = goal
        }
    }

    suspend fun updateTrackingState(date: String, baselineSensor: Int, storedBase: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.ACTIVE_TRACKING_DATE] = date
            preferences[PreferenceKeys.BASELINE_SENSOR_READING] = baselineSensor
            preferences[PreferenceKeys.STORED_STEPS_BASE] = storedBase
        }
    }

    suspend fun updateUseMiles(useMiles: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.USE_MILES] = useMiles
        }
    }
}
