package com.syndicate.carsharing

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.syndicate.carsharing.database.models.Rate
import com.syndicate.carsharing.database.models.User
import com.syndicate.carsharing.viewmodels.MainViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.nefilim.kjwt.JWT
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserStore @Inject constructor(@ApplicationContext private val context: Context) {

    private val mapper = jacksonObjectMapper()

    companion object {
        val ID = intPreferencesKey("id")
        val EMAIL = stringPreferencesKey("email")
        val PASSWORD = stringPreferencesKey("password")
        val USER_ROLE = stringPreferencesKey("user_role")
        val PASSPORT_ID = intPreferencesKey("passport_id")
        val DRIVER_LICENSE_ID = intPreferencesKey("driver_license_id")
        val BALANCE = doublePreferencesKey("balance")
        val IS_VERIFIED = booleanPreferencesKey("is_verified")
        val SELFIE_ID = intPreferencesKey("selfie_id")
        val TOKEN = stringPreferencesKey("token")
        val RESERVING = booleanPreferencesKey("is_reserving")
        val CHECKING = booleanPreferencesKey("is_checking")
        val RENTING = booleanPreferencesKey("is_renting")
        val LAST_RATE = stringPreferencesKey("last_rate")
    }

    fun getToken(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN] ?: ""
        }
    }

    fun getLastSelectedRate(): Flow<Rate> {
        val mapper = jacksonObjectMapper()
        return context.dataStore.data.map { preferences ->
            mapper.readValue<Rate>(preferences[LAST_RATE] ?: mapper.writeValueAsString(Rate()))
        }
    }

    suspend fun setLastSelectedRate(rate: Rate) {
        val mapper = jacksonObjectMapper()
        context.dataStore.edit { preferences ->
            preferences[LAST_RATE] = mapper.writeValueAsString(rate)
        }
    }

    suspend fun clearCarStates() {
        context.dataStore.edit { preferences ->
            preferences[RENTING] = false
            preferences[RESERVING] = false
            preferences[CHECKING] = false
        }
    }

    fun getReserving(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[RESERVING] ?: false
        }
    }

    suspend fun setReserving(isReserving: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[RESERVING] = isReserving
        }
    }

    fun getRenting(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[RENTING] ?: false
        }
    }

    suspend fun setRenting(isRenting: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[RENTING] = isRenting
        }
    }

    fun getChecking(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[CHECKING] ?: false
        }
    }

    suspend fun setChecking(isChecking: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CHECKING] = isChecking
        }
    }

    fun getUser(): Flow<User> {
        return context.dataStore.data.map { preferences ->
            User(
                id = preferences[ID] ?: 0,
                email = preferences[EMAIL] ?: "",
                password = preferences[PASSWORD] ?: "",
                userRole = preferences[USER_ROLE] ?: "",
                passportId = preferences[PASSPORT_ID] ?: 0,
                driverLicenseId = preferences[DRIVER_LICENSE_ID] ?: 0,
                balance = preferences[BALANCE] ?: 0.0,
                isVerified = preferences[IS_VERIFIED] ?: false,
                selfieId = preferences[SELFIE_ID] ?: 0
            )
        }
    }

    suspend fun saveUser(user: User) {
        context.dataStore.edit {preferences ->
            preferences[ID] = user.id
            preferences[EMAIL] = user.email
            preferences[PASSWORD] = user.password
            preferences[USER_ROLE] = user.userRole
            preferences[PASSPORT_ID] = user.passportId ?: 0
            preferences[DRIVER_LICENSE_ID] = user.driverLicenseId ?: 0
            preferences[BALANCE] = user.balance
            preferences[IS_VERIFIED] = user.isVerified
            preferences[SELFIE_ID] = user.selfieId ?: 0
        }
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN] = token
        }
    }

    fun decryptToken(token: String): User {
        var user: User? = null
        JWT.decode(token).tap {
            val algorithm = "AES/CBC/PKCS5Padding"
            val key = SecretKeySpec(Base64.getDecoder().decode("etfpbiaI/tdXSTl36Os6Q3hufDpcSxVwXZYY7lx4Z7g="), "AES")
            val iv = IvParameterSpec(Base64.getDecoder().decode("autI78dTryrVFHHivDxr5g=="))

            val cipher = Cipher.getInstance(algorithm)
            cipher.init(Cipher.DECRYPT_MODE, key, iv)
            val plainText = cipher.doFinal(Base64.getDecoder().decode(it.claimValue("user").toList().joinToString()))
            val result = String(plainText)
            user = mapper.readValue(result)
        }
        return user as User
    }
}