package com.samples.pusher.core.utils

import java.math.BigInteger
import java.security.MessageDigest


fun String.isHttpUrl() = this.startsWith("http://", 0, true) ||
  this.startsWith("https://", 0, true)

fun md5(input: String): String {
  val md = MessageDigest.getInstance("MD5")
  return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
}

fun getFilenameFromPath(path: String) =
  path.substringAfterLast('/').substringBeforeLast('.')