package com.netpluspay.nibssclient.dao

import androidx.room.* // ktlint-disable no-wildcard-imports
import com.netpluspay.nibssclient.work.ModelObjects.TransactionResponseXForTracking
import io.reactivex.Single

@Dao
interface TransactionTrackingTableDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTransactionForTracking(transResponse: TransactionResponseXForTracking): Single<Long>

    @Delete
    fun deleteTransactionAfterSuccessfulUpdateAtBackend(updatedTrans: TransactionResponseXForTracking): Single<Int>

    @Query("SELECT * FROM transactionTrackingTable")
    fun getAllYetToBeUpdatedTransactions(): List<TransactionResponseXForTracking>
}
