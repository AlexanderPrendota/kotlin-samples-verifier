package com.samples.pusher.core.utils


fun String.isHttpUrl() = this.startsWith("http://", 0, true) ||
  this.startsWith("https://", 0, true)