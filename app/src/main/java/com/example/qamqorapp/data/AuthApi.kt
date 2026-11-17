package com.example.qamqorapp.data
import android.content.Context
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
    val expiresInMins: Int = 60,
)
@Serializable
data class LoginResponse(
    val firstName: String,
    val lastName: String,
    val accessToken: String,
    val refreshToken: String,
)
@Serializable
data class UserDto(
    val phone: String? = null,
)
interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("auth/me")
    suspend fun me(@Header("Authorization") bearer: String): UserDto
}

interface AuthClient {

    val isLoggedIn: Boolean
    suspend fun login(username: String, password: String)
    suspend fun logout()
}
class DummyJsonAuthClient(
    private val api: AuthApi,
    context: Context,
    private val posts: PostRepository,
) : AuthClient {
    private val prefs = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
    override val isLoggedIn: Boolean
        get() = prefs.getString(KEY_ACCESS, null) != null
    override suspend fun login(username: String, password: String) {
        val tokens = api.login(LoginRequest(username.trim(), password))
        val user = api.me("Bearer ${tokens.accessToken}")
        prefs.edit()
            .putString(KEY_ACCESS, tokens.accessToken)
            .putString(KEY_REFRESH, tokens.refreshToken)
            .apply()
        posts.saveProfile("${tokens.firstName} ${tokens.lastName}".trim(), user.phone.orEmpty())
    }
    override suspend fun logout() {
        prefs.edit().clear().apply()
        posts.clearProfile()
    }

    private companion object {
        const val KEY_ACCESS = "access"
        const val KEY_REFRESH = "refresh"
    }
}
