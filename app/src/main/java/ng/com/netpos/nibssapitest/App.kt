package ng.com.netpos.nibssapitest // ktlint-disable filename

import android.app.Application
import android.content.ContextWrapper
import com.netpluspay.netpossdk.NetPosSdk
import com.netpluspay.netpossdk.NetPosSdk.loadEmvParams
import com.netpluspay.netpossdk.NetPosSdk.loadProvidedCapksAndAids
import com.netpluspay.netpossdk.utils.TerminalParameters
import com.pixplicity.easyprefs.library.Prefs
import timber.log.Timber

class AppClass : Application() {
    override fun onCreate() {
        super.onCreate()
        Prefs.Builder()
            .setContext(this)
            .setMode(ContextWrapper.MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()
        Timber.plant(Timber.DebugTree())
        NetPosSdk.init()
        val terminalParameters = TerminalParameters()
        terminalParameters.merchantCode = "3099"
        terminalParameters.merchantName = "NetPlus"
        terminalParameters.merchantId = "2033LAGPOOO7885"
        terminalParameters.terminalId = "2044EZVH"
        loadEmvParams(terminalParameters)
        loadProvidedCapksAndAids()
    }
}
