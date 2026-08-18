package com.github.v2raygg.dto

data class OutboundTrafficStat(
    val tag: String,
    val direction: String,
    val value: Long,
)