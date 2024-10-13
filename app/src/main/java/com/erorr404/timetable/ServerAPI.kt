package com.erorr404.timetable

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.Scanner
import kotlin.math.roundToInt

class ServerAPI {
    private val linksURL = "https://vl4-timetable.pp.ua/data/links.json"
    private val timetableURL = "https://vl4-timetable.pp.ua/data/timetable.json"
    private val timeURL = "https://vl4-timetable.pp.ua/data/time.json"
    private val booksURL = "https://vl4-timetable.pp.ua/data/books.json"
    private val importanceURL = "https://vl4-timetable.pp.ua/data/importance.json"

    suspend fun fetchAllData(): Schedule? {
        val linksString = fetchDataFromUrl(linksURL)
        val timetableString = fetchDataFromUrl(timetableURL)
        val timeString = fetchDataFromUrl(timeURL)
        val importanceString = fetchDataFromUrl(importanceURL)

        if ((linksString == null) or (timetableString == null) or (timeString == null) or (importanceString == null)) {
            return null
        } else {
            val schedule = parseSchedule(timetableString!!, timeString!!, linksString!!, importanceString!!)
            Log.d("SCHEDULE", "Schedule: $schedule")
            return schedule
        }
    }

    private fun parseSchedule(
        scheduleJson: String,
        timeJson: String,
        linksJson: String,
        importanceJson: String
    ): Schedule {
        val gson = Gson()

        // Parse Schedule JSON (subject names for each day)
        val scheduleMap: Map<String, List<String>> = gson.fromJson(scheduleJson.replace("\n", ""), object : TypeToken<Map<String, List<String>>>() {}.type)

        // Parse Time JSON (start and end times for each lesson slot)
        val timeMap: Map<String, Map<String, Map<String, Any>>> = gson.fromJson(timeJson.replace("\n", ""), object : TypeToken<Map<String, Map<String, Map<String, Any>>>>() {}.type)

        // Parse Links JSON (subject to meeting link)
        val linksMap: Map<String, String> = gson.fromJson(linksJson.replace("\n", ""), object : TypeToken<Map<String, String>>() {}.type)

        // Parse Importance JSON (subject to importance level)
        val importanceMap: Map<String, Int> = gson.fromJson(importanceJson.replace("\n", ""), object : TypeToken<Map<String, Int>>() {}.type)

        // Create TimeSlot objects from timeMap
        fun createTimeSlot(time: Map<String, Any>): TimeSlot {
            val hour = "${time["hour"]}".toDouble().roundToInt()
            val minute = "${time["minute"]}".toDouble().roundToInt()
            return TimeSlot(hour, minute)
        }

        // Build the final schedule by iterating over each day in scheduleMap
        fun buildLessons(daySchedule: List<String>): List<Lesson> {
            return daySchedule.mapIndexed { index, subjectName ->
                val start = createTimeSlot(timeMap[(index + 1).toString()]!!["start"]!!)
                val end = createTimeSlot(timeMap[(index + 1).toString()]!!["end"]!!)
                val link = linksMap[subjectName] ?: "No link available"
                val importance = importanceMap[subjectName] ?: 0

                Lesson(
                    subject = Subject(subjectName, importance, link),
                    start = start,
                    end = end
                )
            }
        }

        // Build the weekly schedule
        return Schedule(
            monday = buildLessons(scheduleMap["monday"] ?: listOf()),
            tuesday = buildLessons(scheduleMap["tuesday"] ?: listOf()),
            wednesday = buildLessons(scheduleMap["wednesday"] ?: listOf()),
            thursday = buildLessons(scheduleMap["thursday"] ?: listOf()),
            friday = buildLessons(scheduleMap["friday"] ?: listOf()),
        )
    }

    private suspend fun fetchDataFromUrl(url: String): String? {
        return withContext(Dispatchers.IO) {
            fetchData(url)
        }
    }

    private fun fetchData(urlString: String): String? {
        try {
            val url = URL(urlString)
            val connection = url.openConnection()
            connection.connect() // Establish connection
            return Scanner(connection.inputStream, "UTF-8").useDelimiter("\\A").next() // Read data as String
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}