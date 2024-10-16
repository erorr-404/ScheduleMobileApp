package com.erorr404.timetable

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar

@Parcelize
data class Subject(
    var name: String,
    var importance: Int,
    var link: String
): Parcelable

@Parcelize
data class TimeSlot(
    var hour: Int,
    var minute: Int
): Parcelable

@Parcelize
data class Lesson(
    var subject: Subject?,
    var start: TimeSlot?,
    var end: TimeSlot?
): Parcelable

@Parcelize
data class Schedule(
    var monday: List<Lesson> = listOf(),
    var tuesday: List<Lesson> = listOf(),
    var wednesday: List<Lesson> = listOf(),
    var thursday: List<Lesson> = listOf(),
    var friday: List<Lesson> = listOf(),
): Parcelable

data class ScheduleListItem(
    val lesson: String,
    val link: String,
    val start: TimeSlot
)

data class AlarmItem(
    val time: Calendar,
    val title: String,
    val content: String,
    val intentLink: String
)
