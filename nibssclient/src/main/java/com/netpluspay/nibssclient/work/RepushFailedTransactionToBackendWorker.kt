package com.netpluspay.nibssclient.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.netpluspay.nibssclient.database.AppDatabase
import com.netpluspay.nibssclient.models.DataToLogAfterConnectingToNibss
import com.netpluspay.nibssclient.models.mapTransactionResponseToTransactionWithRemark
import com.netpluspay.nibssclient.network.StormApiClient
import com.netpluspay.nibssclient.work.ModelObjects.disposeWith
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class RepushFailedTransactionToBackendWorker(
    context: Context,
    workParams: WorkerParameters
) : Worker(context, workParams) {
    private val compositeDisposable = CompositeDisposable()
    private val stormApiService = StormApiClient.getStormApiLoginInstance()
    private val transactionTrackingTableDao =
        AppDatabase.getDatabaseInstance(context).transactionTrackingTableDao()

    override fun doWork(): Result {
        val yetToBeUpdatedTransactions =
            transactionTrackingTableDao.getAllYetToBeUpdatedTransactions()
        var counter = yetToBeUpdatedTransactions.size
        yetToBeUpdatedTransactions.forEach {
            repushTransactionTransaction(it) {
                --counter
            }
        }

        return if (transactionTrackingTableDao.getAllYetToBeUpdatedTransactions()
            .isEmpty() && counter == 0
        ) {
            Result.success()
        } else Result.retry()
    }

    private fun repushTransactionTransaction(
        transactionToRepush: ModelObjects.TransactionResponseXForTracking,
        decrementCounter: () -> Unit
    ) {
        val transactionResponse = DataToLogAfterConnectingToNibss(
            transactionToRepush.status,
            mapTransactionResponseToTransactionWithRemark(
                ModelObjects.mapToTransactionResponse(
                    transactionToRepush.transRespX
                )
            ),
            transactionToRepush.temporalRRN
        )
        stormApiService.updateLogAfterConnectingToNibss2(
            transactionToRepush.temporalRRN,
            transactionResponse
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { t1, t2 ->
                t1?.let {
                    Timber.d("GOT_HERE_A")
                    if (it.code() in 200..299 || it.code() == 409 || it.message()
                        .contains("There is an error")
                    ) {
                        Timber.d("CONDITION_FULFILLED")
                        transactionTrackingTableDao.deleteTransactionAfterSuccessfulUpdateAtBackend(
                            transactionToRepush
                        ).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { numberOfAffectedRows, error ->
                                numberOfAffectedRows?.let {
                                    Timber.d("NUMBER_OF_DELETED_ITEMS=====>%s", it.toString())
                                }
                                error?.let {
                                    Timber.d("ERROR_DELETING_ITEMS=====>%s", it.localizedMessage)
                                }
                            }.disposeWith(compositeDisposable)
                        decrementCounter()
                    }
                }
                t2?.let {
                    Timber.d("SEND_TRANS_TO_BACKEND_ERROR%s", it.localizedMessage)
                }
            }.disposeWith(compositeDisposable)
    }

    override fun onStopped() {
        super.onStopped()
        compositeDisposable.clear()
    }
}
