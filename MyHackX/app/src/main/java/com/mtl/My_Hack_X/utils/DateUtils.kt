package com.mtl.My_Hack_X.utils

import com.google.firebase.Timestamp
import java.util.Date

fun Timestamp.toDate(): Date = Date(seconds * 1000 + nanoseconds / 1000000)

fun Date.toTimestamp(): Timestamp = Timestamp(time / 1000, ((time % 1000) * 1000000).toInt()) 