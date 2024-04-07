package com.example.findmyfriends.data.model

class FMFUser (var username: String, var email: String = "", var id: String ) {
    var latitude = ""
    var longitude = ""
    var lastPosTime = ""
    var groups = mutableListOf<String>()

}
