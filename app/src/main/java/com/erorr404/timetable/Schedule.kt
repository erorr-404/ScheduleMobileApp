package com.erorr404.timetable

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TSubject(
    var name: String,
    var importance: Int,
    var link: String
) : Parcelable

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
