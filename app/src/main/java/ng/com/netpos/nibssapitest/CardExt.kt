@file:Suppress("DEPRECATION")

package ng.com.netpos.nibssapitest

import android.app.Activity
import android.app.ProgressDialog
import android.content.*
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

import com.netpluspay.netpossdk.emv.CardReadResult
import com.netpluspay.netpossdk.emv.CardReaderEvent
import com.netpluspay.netpossdk.emv.CardReaderService
import com.netpluspay.nibssclient.models.CardData
import com.netpluspay.nibssclient.models.IsoAccountType
import com.pos.sdk.emvcore.POIEmvCoreManager.DEV_ICC
import com.pos.sdk.emvcore.POIEmvCoreManager.DEV_PICC
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

data class ICCCardHelper(
    val cardReadResult: CardReadResult? = null,
    val customerName: String? = null,
    val cardScheme: String? = null,
    var accountType: IsoAccountType? = null,
    val cardData: CardData? = null,
    val error: Throwable? = null
)


fun showCardDialog(
    context: Activity,
    amount: Long,
    cashBackAmount: Long
):  LiveData<ICCCardHelper> {
    val liveData: MutableLiveData<ICCCardHelper> = MutableLiveData()
    val dialog = ProgressDialog(context)
        .apply {
            setMessage("Waiting for card")
            setCancelable(false)
        }
    var iccCardHelper: ICCCardHelper? = null
    val cardService = CardReaderService(context, listOf(DEV_ICC, DEV_PICC))
    val c = cardService.initiateICCCardPayment(
        amount,
        cashBackAmount
    )
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
            when (it) {
                is CardReaderEvent.CardRead -> {
                    val cardResult = it.data
//                    Log.e("CardExt", cardResult.iccDataString)
//                    Log.e("CardExt", cardResult.nibssIccSubset)
//                    Timber.e(cardResult.toString())
//                    Timber.e(cardResult.iccDataString)
//                    Timber.e(cardResult.nibssIccSubset)
                    val card = CardData(
                        track2Data = cardResult.track2Data!!,
                        nibssIccSubset = cardResult.nibssIccSubset,
                        panSequenceNumber = cardResult.applicationPANSequenceNumber!!,
                        posEntryMode = "051"
                    )
                    if (cardResult.encryptedPinBlock.isNullOrEmpty().not()){
                        card.apply {
                            pinBlock = cardResult.encryptedPinBlock
                        }
                    }
                    iccCardHelper = ICCCardHelper(
                        cardReadResult = cardResult,
                        customerName = cardResult.cardHolderName,
                        cardScheme = cardResult.cardScheme,
                        cardData = card
                    )

                }
                is CardReaderEvent.CardDetected -> {
                    val mode = when (it.mode) {
                        DEV_ICC -> "EMV"
                        DEV_PICC -> "EMV Contactless"
                        else -> "MAGNETIC STRIPE"
                    }
                    dialog.setMessage("Reading Card with $mode Please Wait")
                 Timber.e("Card Detected")
                }
            }
        }, {
            it?.let {
                Timber.e(it)
                it.printStackTrace()
                dialog.dismiss()
                Timber.e( "error: ${it.localizedMessage}")
                liveData.value = ICCCardHelper(error = it)
            }

        }, {
            dialog.dismiss()
            liveData.value = iccCardHelper
        })

    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Stop") { d, _ ->
        cardService.transEnd(message = "Stopped")
        d.dismiss()
    }
    dialog.show()
    return liveData
}

