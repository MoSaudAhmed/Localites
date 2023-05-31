package com.example.localites.helpers

import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class TextChanges {
    public fun capitalize(capString: String): String? {
        val capBuffer = StringBuffer()
        val capMatcher: Matcher =
            Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString)
        while (capMatcher.find()) {
            capMatcher.appendReplacement(
                capBuffer,
                capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase()
            )
        }
        return capMatcher.appendTail(capBuffer).toString()
    }

    public fun convertTimeStampToDate(): String {
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEE MMM dd hh:mm aa 'GMT'Z yyyy")
        val date = dateFormat.format(cal.time)
        return date
    }

    public fun convertDateToText(date: String): String {
        try {
            val dateFormat = SimpleDateFormat("EEE MMM dd hh:mm aa 'GMT'Z yyyy")
            val date = dateFormat.parse(date)
            var am_pm = ""
            var hour = 0
            if (date.hours > 12) {
                am_pm = "pm"
                hour = date.hours - 12
            } else {
                am_pm = "am"
                hour = date.hours
            }
            var yearFormat = SimpleDateFormat("yyyy")
            return "Posted On: ${date.date}-${date.month + 1}-${yearFormat.format(date)} - ${hour}:${date.minutes} $am_pm"
        } catch (e: Exception) {
            return "Posted on: ${date}"
        }
    }
}