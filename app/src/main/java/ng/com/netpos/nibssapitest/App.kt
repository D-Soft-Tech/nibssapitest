package ng.com.netpos.nibssapitest

import android.app.Application
import com.netpluspay.netpossdk.NetPosSdk
import com.netpluspay.netpossdk.utils.TerminalParameters
import timber.log.Timber

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        //NetPosSdk.init(this)
        Timber.plant(Timber.DebugTree())
//        NetPosSdk.loadEmvParams(TerminalParameters().apply {
//            terminalCapability = "E068C8"
//        })
    }
}