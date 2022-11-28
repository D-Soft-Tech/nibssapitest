package com.netpluspay.nibssclient.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.danbamitale.epmslib.entities.TransactionResponse
import com.netpluspay.nibssclient.dao.TransactionResponseDao
import com.netpluspay.nibssclient.dao.TransactionTrackingTableDao

@Database(
    entities = [TransactionResponse::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(
    RoomTypeConverters::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionTrackingTableDao(): TransactionTrackingTableDao
    abstract fun transactionResponseDao(): TransactionResponseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabaseInstance(context: Context) = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, AppDatabase::class.java, "netpos-db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
