package com.yannick.momentum

import android.os.Build.VERSION.SDK_INT
import androidx.multidex.MultiDexApplication
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.request.crossfade
import com.yannick.HomeModules
import com.yannick.core.CoreModules
import com.yannick.data.DataModules
import com.yannick.domain.DomainModules
import com.yannick.featureauth.presentation.AuthPresentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import timber.log.Timber

class MomentumApplication : MultiDexApplication(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        initKoin()
        initTimber()
    }

    private fun initKoin() {
        GlobalContext.startKoin {
            androidLogger()
            androidContext(this@MomentumApplication)
            modules(AuthPresentationModule)
            modules(appModules)
            modules(CoreModules)
            modules(DataModules)
            modules(DomainModules)
            modules(HomeModules)
        }
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .crossfade(true)
            .build()
    }
}
