package com.example.foody.data

import android.content.Context
import androidx.datastore.DataStore
import androidx.datastore.preferences.*
import com.example.foody.util.Constants.Companion.DEFAULT_DIET_TYPE
import com.example.foody.util.Constants.Companion.DEFAULT_MEAL_TYPE
import com.example.foody.util.Constants.Companion.PREFERENCES_BACK_ONLINE
import com.example.foody.util.Constants.Companion.PREFERENCES_DIET_TYPE
import com.example.foody.util.Constants.Companion.PREFERENCES_DIET_TYPE_ID
import com.example.foody.util.Constants.Companion.PREFERENCES_MEAL_TYPE
import com.example.foody.util.Constants.Companion.PREFERENCES_MEAL_TYPE_ID
import com.example.foody.util.Constants.Companion.PREFERENCES_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

//DataStoreRepository will be used in the RecipesViewModel so annotation for hilt added
@ActivityRetainedScoped
class DataStoreRepository
@Inject constructor(
    @ApplicationContext
    private val context: Context) {

    private object PreferenceKeys{
        val selectedMealType = preferencesKey<String>(PREFERENCES_MEAL_TYPE)
        val selectedMealTypeId = preferencesKey<Int>(PREFERENCES_MEAL_TYPE_ID)
        val selectedDietType = preferencesKey<String>(PREFERENCES_DIET_TYPE)
        val selectedDietTypeId = preferencesKey<Int>(PREFERENCES_DIET_TYPE_ID)
        val backOnline  = preferencesKey<Boolean>(PREFERENCES_BACK_ONLINE)
    }

    private val dataStore : DataStore<Preferences> = context.createDataStore(
        name = PREFERENCES_NAME)

    //suspend (as it works in background thread) function for saving values from chips and store to Data Store preferences
    suspend fun saveMealAndDietType(
        mealType:String,
        mealTypeId: Int,
        dietType:String,
        dietTypeId:Int){
        dataStore.edit { preferences ->
            //specifying key: preferences[PreferenceKeys.selectedMealType] and value: = mealType
            preferences[PreferenceKeys.selectedMealType] = mealType
            preferences[PreferenceKeys.selectedMealTypeId] = mealTypeId
            preferences[PreferenceKeys.selectedDietType] = dietType
            preferences[PreferenceKeys.selectedDietTypeId] = dietTypeId
        }
    }

    //fun edit called to store value of backOnline from fun param inside the key
    suspend fun saveBackOnline(backOnline: Boolean){
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.backOnline] = backOnline
        }
    }

    //variable which read values that we have already saved
    val readMealAndDietType: Flow<MealAndDietType> = dataStore.data
        .catch { exception->
            if (exception is IOException){
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
            //get data from DataStore preferences and if default chips was selected, read mentioned values
        .map { preferences ->
            //store value for selected key PreferenceKeys.selectedMealType, if no any values saved, emit "main course"
            // Elvis operator means - if the expression to the left of ?: is not null, the elvis operator returns it, otherwise it returns the expression to the right.
            val selectedMealType = preferences[PreferenceKeys.selectedMealType]?: DEFAULT_MEAL_TYPE
            val selectedMealTypeId = preferences[PreferenceKeys.selectedMealTypeId]?: 0
            val selectedDietType = preferences[PreferenceKeys.selectedDietType]?: DEFAULT_DIET_TYPE
            val selectedDietTypeId = preferences[PreferenceKeys.selectedDietTypeId]?:0

            MealAndDietType(
                selectedMealType,
                selectedMealTypeId,
                selectedDietType,
                selectedDietTypeId
            )
        }

    val readBackOnline: Flow<Boolean> = dataStore.data
        .catch { exception->
            if (exception is IOException){
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            //return the value of backOnline variable, if no vale return default "false"
            val backOnline = preferences[PreferenceKeys.backOnline]?: false
            backOnline
        }
}

//class used to pass values - Strings represent chop text Ints represent chip IDs
data class MealAndDietType(
    val selectedMealType: String,
    val selectedMealTypeId: Int,
    val selectedDietType: String,
    val selectedDietTypeId: Int
    )