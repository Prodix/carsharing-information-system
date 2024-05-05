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
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.DefaultResponse
import com.syndicate.carsharing.database.models.Rate
import com.syndicate.carsharing.database.models.User
import com.syndicate.carsharing.viewmodels.MainViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.nefilim.kjwt.JWT
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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
        val IS_EMAIL_VERIFIED = booleanPreferencesKey("is_email_verified")
        val SELFIE_ID = intPreferencesKey("selfie_id")
        val TOKEN = stringPreferencesKey("token")
        val RESERVING = booleanPreferencesKey("is_reserving")
        val CHECKING = booleanPreferencesKey("is_checking")
        val RATING = intPreferencesKey("rating")
        val CHECKING_TIME = intPreferencesKey("checking_time")
        val RENTING = booleanPreferencesKey("is_renting")
        val LAST_RATE = stringPreferencesKey("last_rate")
        val RENT_HOURS = intPreferencesKey("rent_hours")
        val IS_CAR_LOCKED = booleanPreferencesKey("is_car_locked")
    }

    fun getToken(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN] ?: ""
        }
    }

    fun getCheckingTime(): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[CHECKING_TIME] ?: 0
        }
    }

    suspend fun saveCheckingTime(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[CHECKING_TIME] = seconds
        }
    }

    fun getIsLocked(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[IS_CAR_LOCKED] ?: false
        }
    }

    suspend fun saveIsCarLocked(isLocked: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_CAR_LOCKED] = isLocked
        }
    }

    fun getRentHours(): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[RENT_HOURS] ?: 0
        }
    }

    suspend fun saveRentHours(hours: Int) {
        context.dataStore.edit { preferences ->
            preferences[RENT_HOURS] = hours
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
        updateToken()
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
        updateToken()
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
        updateToken()
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
                isEmailVerified = preferences[IS_EMAIL_VERIFIED] ?: false,
                selfieId = preferences[SELFIE_ID] ?: 0,
                rating = preferences[RATING] ?: 0,
            )
        }
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN] = token
        }
        updateToken()
    }

    suspend fun updateToken() {
        val token = getToken().first()
        var user = getUser().first()
        val response = HttpClient.client.post(
            "${HttpClient.url}/account/signin"
        ) {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("email", user.email)
                    }
                ))
            headers["Authorization"] = "Bearer $token"
        }.body<DefaultResponse>()
        if (response.status_code == 200) {
            user = decryptToken(response.token as String)
            context.dataStore.edit { preferences ->
                preferences[TOKEN] = response.token as String
                preferences[ID] = user.id
                preferences[EMAIL] = user.email
                preferences[PASSWORD] = user.password
                preferences[USER_ROLE] = user.userRole
                preferences[PASSPORT_ID] = user.passportId ?: 0
                preferences[DRIVER_LICENSE_ID] = user.driverLicenseId ?: 0
                preferences[BALANCE] = user.balance
                preferences[IS_VERIFIED] = user.isVerified
                preferences[IS_EMAIL_VERIFIED] = user.isEmailVerified
                preferences[SELFIE_ID] = user.selfieId ?: 0
                preferences[RATING] = user.rating ?: 0
            }
        }
    }

    private fun decryptToken(token: String): User {
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