package com.example.spotiflag.model.userData

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UserData {
    var images: ArrayList<ImageInfo>? = null
    get() { return field }
    set(value) {  field = value }
}