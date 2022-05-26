package com.danbamitale.epmslib.utils

enum class MessageReasonCode(val code: String) {

    CustomerCancellation("4000"), UnSpecified("4001"), CompletedPartially("4004"), Timeout("4021");
}
