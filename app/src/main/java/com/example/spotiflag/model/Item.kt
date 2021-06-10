package com.example.spotiflag.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class Item {
    var track: Track? = null
    get() { return field }
    set(value) {  field = value }
}