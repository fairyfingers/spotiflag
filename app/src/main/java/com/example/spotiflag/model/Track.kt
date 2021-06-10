package com.example.spotiflag.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class Track {
    var name: String? = null
    get() { return field }
    set(value) {  field = value }

    var popularity: Int? = null
    get() { return field }
    set(value) {  field = value }
}