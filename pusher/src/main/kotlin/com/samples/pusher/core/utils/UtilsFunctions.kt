package com.samples.pusher.core.utils


fun String.isHttpUrl() = this.indexOf("http://", 0, true) == 0 ||
  this.indexOf("https://", 0, true) == 0