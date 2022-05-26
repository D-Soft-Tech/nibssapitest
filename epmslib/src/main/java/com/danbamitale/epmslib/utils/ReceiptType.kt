package com.danbamitale.epmslib.utils

enum class ReceiptType {
    CUSTOMER_COPY, MERCHANT_COPY;

    override fun toString(): String {
        return name.replace('_', ' ')
    }
}