package com.goofy.boilerplate.extension

import java.net.URLDecoder
import java.net.URLEncoder

fun String.encodeURL(type: String = "UTF-8"): String = URLEncoder.encode(this, type)
fun String.decodeURL(type: String = "UTF-8"): String = URLDecoder.decode(this, type)
