package jp.hotdrop.compl.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class AppModule(app: Application) {

    private val context: Context = app

    @Provides
    fun provideContext(): Context {
        return context
    }
}