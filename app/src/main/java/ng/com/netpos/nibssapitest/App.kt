package ng.com.netpos.nibssapitest // ktlint-disable filename

import android.app.Application
import android.content.ContextWrapper
import com.dsofttech.dprefs.utils.DPrefs
import com.pixplicity.easyprefs.library.Prefs
import timber.log.Timber

class AppClass : Application() {
    override fun onCreate() {
        super.onCreate()
        DPrefs.initializeDPrefs(this)
        Prefs.Builder()
            .setContext(this)
            .setMode(ContextWrapper.MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()
        Timber.plant(Timber.DebugTree())
//        NetPosSdk.init()
//        val terminalParameters = TerminalParameters()
//        terminalParameters.merchantCode = "3099"
//        terminalParameters.merchantName = "NetPlus"
//        terminalParameters.merchantId = "2033LAGPOOO7885"
//        terminalParameters.terminalId = "2033ALZP"
//        loadEmvParams(terminalParameters)
//        loadProvidedCapksAndAids()
    }
}
