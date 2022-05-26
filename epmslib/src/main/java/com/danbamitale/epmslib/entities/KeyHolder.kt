package com.danbamitale.epmslib.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.danbamitale.epmslib.extensions.xor
import com.danbamitale.epmslib.utils.TripleDES
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class KeyHolder
/**
 *
 * @param masterKey
 * @param sessionKey
 * @param pinKey
 * @param track2Key
 * @param bdk
 * @param isTest
 */(
    var masterKey: String = "",
    var sessionKey: String = "",
    var pinKey: String = "",
    var track2Key: String = "",
    var bdk: String = ""
) : Parcelable {

    @PrimaryKey
    var id = 1

    init {
        println("initialized keyholder")
    }

    private var baseKey: String = "DB99F003EB17F590E8DBCCA66CDD7E72"
    var posMode = PosMode.EPMS
        private set

    companion object {
        @Ignore
        private var baseKey: String = "0101010101010101"

        fun setHostKeyComponents(firstComponent: String, secondComponent: String) {
            if (firstComponent.isNotBlank() && secondComponent.isNotBlank()) {
                baseKey = firstComponent.xor(secondComponent)
            }
        }

        fun getClearMasterKey(encryptedMasterKey: String) =
            TripleDES.decrypt(encryptedMasterKey, baseKey)
    }
}

val KeyHolder.nibssClearMasterKey: String
    get() = KeyHolder.getClearMasterKey(masterKey)

val KeyHolder.clearSessionKey: String
    get() = TripleDES.decrypt(this.sessionKey, nibssClearMasterKey)

val KeyHolder.clearPinKey: String
    get() = TripleDES.decrypt(this.pinKey, nibssClearMasterKey)

enum class PosMode {
    EPMS,
    POSVAS,
    ISW,
}
