package com.isw.gateway

import DukptHelper
import android.content.Context
import android.util.Log
import com.danbamitale.epmslib.di.AppModule.providesDataEc
import com.danbamitale.epmslib.entities.*
import com.danbamitale.epmslib.processors.TransactionProcessor
import com.danbamitale.epmslib.tlv.TLVList
import com.danbamitale.epmslib.utils.*
import com.danbamitale.epmslib.utils.Utility.POS_VAS_NIBSS_DEFAULT_IP
import com.danbamitale.epmslib.utils.Utility.POS_VAS_NIBSS_DEFAULT_PORT
import com.google.gson.Gson
import com.isw.iswclient.iswapiclient.iswTransactionClient
import com.isw.iswclient.request.ISW_ONLY_STRATEGY
import com.isw.iswclient.request.TransferRequest
import com.isw.iswclient.response.CashOutResponnse
import com.isw.iswclient.response.toTransactionResponse
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*

class TransactionProcessorWrapper @JvmOverloads constructor(
    private val merchId: String,
    private val terminalId: String,
    private val amount: Long,
    private val otherAmount: Long = 0,
    var transactionRequestData: TransactionRequestData? = null,
    var keyHolder: KeyHolder? = null,
    var configData: ConfigData? = null,
) {
    init {
        System.loadLibrary("api-keys")
    }

    private val ec = providesDataEc()

    private val posVasIp: String = ec.decryptData(getIp())
    private val posVasPort = ec.decryptData(getPort()).toInt()
    private lateinit var transactionRoute: TransactionRoute
    private lateinit var transactionProcessor: TransactionProcessor
    private var attempt = 0

    private external fun getIp(): String
    private external fun getPort(): String

    fun processIswTransaction(
        cardData: CardData,
    ): Single<TransactionResponse> =
        Single.fromCallable {
            attempt++
            if (transactionRequestData!!.iswParameters == null) {
                println("interswitch parameter is null")
                throw Exception("iswParameters is null")
            }

            if (transactionRequestData!!.iswParameters!!.token.isNullOrEmpty()) {
                if (attempt < 2 &&
                    configData != null && transactionRequestData!!.processingProperties!!.processingStrategy != ISW_ONLY_STRATEGY
                ) {
                    println("isw token is null, re-route transaction to nibss")
                }
                println("isw token is null")
                throw Exception("isw token is null")
            }

            transactionRequestData!!.iswParameters!!.terminalId = terminalId
            transactionRequestData!!.iswParameters!!.remark =
                transactionRequestData!!.echoData ?: ""
            val transactionTime = System.currentTimeMillis()
            val timeMgr = IsoTimeManager()
            val transferRequest = TransferRequest().apply {
                val tlvList = TLVList.fromBinary(cardData.nibssIccSubset)
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.getDefault())
                batteryInformation = 100
                currencyCode = "566"
                languageInfo = "EN"
                merchantId = transactionRequestData!!.iswParameters!!.merchantId
                merchantLocation = transactionRequestData!!.iswParameters!!.merchantNameLocation
                posConditionCode = "00"
                posDataCode = "510101511344101"
                posEntryMode = "051"
                posGeoCode = "00234000000000566"
                printerStatus = 1
                terminalId = transactionRequestData!!.iswParameters!!.terminalId
                terminalType = "22"
                transmissionDate = sdf.format(Date(transactionTime))
                uniqueId = transactionRequestData!!.iswParameters!!.terminalSerial
                cardSequenceNumber =
                    cardData.panSequenceNumber.let { csn ->
                        if (csn.length == 3) {
                            csn.takeLast(2)
                        } else {
                            csn
                        }
                    }
                amountAuthorized = tlvList.getTLV("9F02").value
                amountOther = tlvList.getTLV("9F03").value
                applicationInterchangeProfile = tlvList.getTLV("82").value
                atc = tlvList.getTLV("9F36").value
                cryptogram = tlvList.getTLV("9F26").value
                cryptogramInformationData =
                    tlvList.getTLV("9F27").value.toInt()
                cvmResults = tlvList.getTLV("9F34").value
                iad = tlvList.getTLV("9F10").value
                transactionCurrencyCode = "566"
                terminalVerficationResult = tlvList.getTLV("95").value
                terminalCountryCode = "566"
                terminalType2 = tlvList.getTLV("9F35").value
                terminalCapabilities = "E0F0C8"
                transationDate = tlvList.getTLV("9A").value
                transactionType = tlvList.getTLV("9C").value
                unpredictableNumber = tlvList.getTLV("9F37").value
                dedicatedFileName = try {
                    tlvList.getTLV("84").value
                } catch (e: Exception) {
                    "00000000000000"
                }
                pan = cardData.pan.replace("F", "")
                expiryMonth = cardData.expiryDate.takeLast(2)
                expiryYear = cardData.expiryDate.take(2)
                track2 = cardData.track2Data
                originalTransmissionDate = sdf.format(Date(transactionTime))
                stan = transactionRequestData!!.STAN ?: timeMgr.time
                rrn = transactionRequestData!!.RRN ?: timeMgr.fullDate.substring(2, 14)
                fromAccount = transactionRequestData?.accountType?.name ?: "default"
                toAccount = ""
                minorAmount = amount.minus(1075).toString()
                receivingInstitutionId =
                    transactionRequestData!!.iswParameters!!.receivingInstitutionId
                surcharge = "1075"
                ksnd = "605"
                ksn = if (getCVMMethod(tlvList.getTLV("9F34").value) == CVMETHOD.ONLINE_PIN) {
                    "000002DDDDE00001"
                } else {
                    ""
                }
                pinType = "Dukpt"

                pinBlock = if (getCVMMethod(tlvList.getTLV("9F34").value) == CVMETHOD.ONLINE_PIN) {
                    DukptHelper.DesEncryptDukpt(
                        DukptHelper.getSessionKey(),
                        TripleDES.decrypt(
                            cardData.pinBlock!!,
                            transactionRequestData!!.iswParameters!!.pinKey ?: "",
                        ).toUpperCase(
                            Locale.getDefault(),
                        ),
                    )
                } else {
                    ""
                }
                keyLabel = "000002"
                destinationAccountNumber =
                    transactionRequestData!!.iswParameters!!.destinationAccountNumber
                extendedTransactionType = "6103"
                header = transactionRequestData!!.iswParameters!!.token ?: ""
            }
            Log.d("TRANS_DATA", Gson().toJson(transferRequest))

            iswTransactionClient.performTransaction(
                "Bearer ${transactionRequestData!!.iswParameters!!.token}",
                transferRequest,
            ).flatMap {
                Single.just(
                    it.toTransactionResponse(
                        transactionRequestData!!.STAN ?: timeMgr.time,
                        cardData,
                        transactionTime,
                        transactionRequestData!!.iswParameters!!,
                        amount,
                        interswitchThreshold = transactionRequestData!!.iswParameters!!.interSwitchThreshold,
                        reqRRN = transactionRequestData!!.RRN ?: timeMgr.fullDate.substring(2, 14),
                    ),
                )
            }.onErrorResumeNext {
                println("interswitch error: " + it.localizedMessage)
                (it as? HttpException)?.let { httpException ->
                    return@onErrorResumeNext Single.just(
                        CashOutResponnse().apply {
                            amountAuthorized = this@TransactionProcessorWrapper.amount
                            AmountOther = this@TransactionProcessorWrapper.otherAmount
                            field39 = "A3"
                        }.toTransactionResponse(
                            transactionRequestData!!.STAN ?: timeMgr.time,
                            cardData,
                            transactionTime,
                            transactionRequestData!!.iswParameters!!,
                            amount,
                            interswitchThreshold = transactionRequestData!!.iswParameters!!.interSwitchThreshold,
                            errorMessage = httpException.localizedMessage,
                            reqRRN = transactionRequestData!!.RRN ?: timeMgr.fullDate.substring(
                                2,
                                14,
                            ),
                        ),
                    )
                }
                Single.error(it)
            }.flatMap {
                println("attempt $attempt")
                println("failed on isw but can't re-route to nibss")
                Single.just(it)
            }.blockingGet()
        }.subscribeOn(Schedulers.io())

    private fun getCVMMethod(cvmResult: String) = when (cvmResult[1]) {
        '1' -> CVMETHOD.OFFLINE_PLAINTEXT_PIN
        '2' -> CVMETHOD.ONLINE_PIN
        '3' -> CVMETHOD.OFFLINE_PLAINTEXT_PIN_AND_SIGNATURE
        '4' -> CVMETHOD.OFFLINE_ENCIPHERED_PIN
        '5' -> CVMETHOD.OFFLINE_ENCIPHERED_PIN_AND_SIGNATURE
        'E' -> CVMETHOD.SIGNATURE
        'F' -> CVMETHOD.NO_CVM_PERFORMED
        else -> CVMETHOD.NO_CVM_PERFORMED
    }

    @JvmOverloads
    fun rollback(
        context: Context,
        reversalReasonCode: MessageReasonCode = MessageReasonCode.Timeout,
    ): Single<TransactionResponse> {
        return if (transactionRoute == TransactionRoute.NIBSS) {
            transactionProcessor.rollback(context, reversalReasonCode)
        } else {
            Single.error(Exception("Cannot rollback"))
        }
    }

    private fun getConnectionData(posMode: PosMode): ConnectionData {
        return if (posMode === PosMode.POSVAS) {
            ConnectionData(
                posVasIp,
                posVasPort,
                true,
            )
        } else {
            ConnectionData(
                POS_VAS_NIBSS_DEFAULT_IP,
                POS_VAS_NIBSS_DEFAULT_PORT,
                true,
            )
        }
    }
}
