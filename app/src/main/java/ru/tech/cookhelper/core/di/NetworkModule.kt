package ru.tech.cookhelper.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import ru.tech.cookhelper.core.di.RetrofitModule.provideRetrofit
import ru.tech.cookhelper.data.remote.api.auth.AuthService
import ru.tech.cookhelper.data.remote.api.chat.ChatApi
import ru.tech.cookhelper.data.remote.api.ingredients.FridgeApi
import ru.tech.cookhelper.data.remote.api.user.UserApi
import ru.tech.cookhelper.data.remote.dto.RecipeDto
import ru.tech.cookhelper.data.remote.utils.Response
import ru.tech.cookhelper.data.remote.web_socket.WebSocketState
import ru.tech.cookhelper.data.remote.web_socket.feed.FeedService
import ru.tech.cookhelper.data.remote.web_socket.message.MessageService
import ru.tech.cookhelper.data.remote.web_socket.message.MessageServiceImpl
import ru.tech.cookhelper.data.remote.web_socket.user.UserService
import ru.tech.cookhelper.data.remote.web_socket.user.UserServiceImpl
import ru.tech.cookhelper.data.utils.JsonParser
import javax.inject.Singleton
import kotlin.random.Random

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAuthService(
        retrofit: Retrofit
    ): AuthService = retrofit.create(AuthService::class.java)

    @Provides
    @Singleton
    fun provideChatApi(
        retrofit: Retrofit
    ): ChatApi = retrofit.create(ChatApi::class.java)

    @Provides
    @Singleton
    fun provideUserApi(
        retrofit: Retrofit
    ): UserApi = retrofit.create(UserApi::class.java)

    @Provides
    @Singleton
    fun provideFridgeApi(
        retrofit: Retrofit
    ): FridgeApi = retrofit.create(FridgeApi::class.java)

    @Provides
    @Singleton
    fun provideMessageService(
        jsonParser: JsonParser
    ): MessageService = MessageServiceImpl(jsonParser)

    private interface Govno {
        @GET("api/recipe/get/")
        suspend fun ebatsaVAnal(@Query("id") hui: Int) : Response<RecipeDto>
    }

    private fun GovnoImpl(retrofit: Retrofit = provideRetrofit()): Govno {
        return retrofit.create(Govno::class.java)
    }

    @Provides
    @Singleton
    fun provideFeedService(
        jsonParser: JsonParser
    ): FeedService = object : FeedService {
        override fun invoke(token: String): Flow<WebSocketState<List<RecipeDto>>> = flow {
            emit(WebSocketState.Opening())
            delay(1000)
            while(true) {
                GovnoImpl().ebatsaVAnal(hui = Random.nextInt(0, 145)).data?.let { pizda ->
                    emit(
                        WebSocketState.Message(
                            listOf(pizda)
                        )
                    )
                    delay(5000)
                }
            }
        }

        override fun sendMessage(data: String) {

        }

        override fun closeService() {

        }

    }

    @Provides
    @Singleton
    fun provideUserService(
        jsonParser: JsonParser
    ): UserService = UserServiceImpl(jsonParser)

}