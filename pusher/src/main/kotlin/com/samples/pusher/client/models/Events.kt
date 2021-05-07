package com.samples.pusher.client.models

data class PullRequestEvent(
	var name:String = "",
	var full_name:String = "",

)
data class Repository(
	var name:String = "",
	var full_name:String = "",
	var html_url:String = "",
	var url:String = "",
)
data class Sender(
	var login:String = "",
	var id:Long,
	var url:String = "",
	var html_url:String = "",
	var avatar_url:String = ""
)
data class Pusher(
	var name:String = "",
	var email:String = "",
)
data class PushEvent(
	var ref:String = "",
	var after:String = "",
	var before:String = "",
	var repository:Repository
	var pusher:Pusher = Pusher()
)