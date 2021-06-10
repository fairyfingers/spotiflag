package com.example.spotiflag.model.userData

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class ImageInfo {
    var url: String? = null
    get() { return field }
    set(value) {  field = value }
}