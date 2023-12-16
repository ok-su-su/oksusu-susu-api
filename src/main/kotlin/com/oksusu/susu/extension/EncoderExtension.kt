package com.oksusu.susu.extension

import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*

fun String.encodeURL(type: String = "UTF-8"): String = URLEncoder.encode(this, type)
fun String.decodeURL(type: String = "UTF-8"): String = URLDecoder.decode(this, type)

fun String.decodeBase64(): String = String(Base64.getDecoder().decode(this))
