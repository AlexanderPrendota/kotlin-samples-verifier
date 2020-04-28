package com.samples.verifier.model

class ParseConfiguration {
  var snippetFlags: HashSet<String> = hashSetOf()
  var ignoreAttributes: HashSet<Attribute>? = null
  var parseDirectory: Regex? = null
  var ignoreDirectory: Regex? = null
  var parseTags: HashSet<String>? = null
}

data class Attribute(val name: String, val value: String)