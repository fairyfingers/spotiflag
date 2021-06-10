package com.example.spotiflag.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class Root {
    var items: ArrayList<Item>? = null
    get() { return field }
    set(value) {  field = value }

    var avatarUrl: String? = null
    get() { return field }
    set(value) { field = value }

    fun computeAverage(): Int {
        var average = 0
        var divisor = 0

        for (item in items!!) {
            average = (average + item.track?.popularity!!)
            divisor++
        }

        average /= divisor
        return average
    }
}