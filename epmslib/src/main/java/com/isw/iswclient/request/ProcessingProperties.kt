package com.isw.iswclient.request

const val NIBSS_THEN_ISW_STRATEGY = "NIBSS_THEN_ISW"
const val ISW_ONLY_STRATEGY = "ISW_ONLY"

data class ProcessingProperties(
    var processingStrategy: String = NIBSS_THEN_ISW_STRATEGY
)
