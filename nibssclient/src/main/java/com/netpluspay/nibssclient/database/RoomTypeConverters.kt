package com.netpluspay.nibssclient.database

import androidx.room.TypeConverter
import com.danbamitale.epmslib.entities.TransactionType
import com.danbamitale.epmslib.utils.IsoAccountType
import com.google.gson.Gson
import com.netpluspay.nibssclient.work.ModelObjects

class RoomTypeConverters {
    companion object {
        @JvmStatic
        @TypeConverter
        fun convertIsoAccountEnumToString(isoAccountType: IsoAccountType) = isoAccountType.name

        @JvmStatic
        @TypeConverter
        fun convertIsoAccountStringToEnum(value: String) = IsoAccountType.valueOf(value)

        @JvmStatic
        @TypeConverter
        fun convertTransactionResponseEnumToString(transactionType: TransactionType) =
            transactionType.name

        @JvmStatic
        @TypeConverter
        fun convertTransactionResponseToEnum(value: String) = TransactionType.valueOf(value)

        @JvmStatic
        @TypeConverter
        fun fromTransactionResponseX(transX: ModelObjects.TransactionResponseX) =
            Gson().toJson(transX)

        @JvmStatic
        @TypeConverter
        fun toTransactionResponseX(transXasString: String) =
            Gson().fromJson(
                transXasString,
                ModelObjects.TransactionResponseX::class.java
            )
    }
}
