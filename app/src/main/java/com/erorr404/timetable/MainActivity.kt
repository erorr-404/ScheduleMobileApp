package com.erorr404.timetable

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.erorr404.timetable.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.LocalTime
import android.content.Intent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity() {

    private var serverAPI = ServerAPI()
    private var schedule: Schedule? = null
    private lateinit var b: ActivityMainBinding
    private var currentLesson: Lesson? = null
    private var curDayPosition = 1
    private var nextLessons: List<Lesson>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        fetchDataAndProceed()

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            val intent = Intent(this, OnErrorActivity::class.java)
            val args: ArrayList<String?> = arrayListOf(throwable.cause.toString(), throwable.message, throwable.localizedMessage, throwable.stackTrace.toString())
            intent.putStringArrayListExtra("error_info", args)
            startActivity(intent)
        }

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        enableEdgeToEdge()

        b.bottomNavigationView.selectedItemId = R.id.homeMenuItem
    }

    private fun fetchDataAndProceed() {
        // Use Coroutines to fetch data in the background
        lifecycleScope.launch {
            // get schedule
            val newSchedule = serverAPI.fetchAllData()
            if (newSchedule != null) {
                schedule = newSchedule
                Log.d("Data", "Data: $schedule")
            } else {
                Log.d("Data","Failed to fetch data")
            }

            val currentDay = LocalDate.now().dayOfWeek
            val dayPosition = currentDay.value
            curDayPosition = dayPosition
            Log.d("CURRENT dayPosition", "$dayPosition")

            try {
                when (dayPosition) {
                    1 -> currentLesson = getCurrentLesson(schedule!!.monday)
                    2 -> currentLesson = getCurrentLesson(schedule!!.tuesday)
                    3 -> currentLesson = getCurrentLesson(schedule!!.wednesday)
                    4 -> currentLesson = getCurrentLesson(schedule!!.thursday)
                    5 -> currentLesson = getCurrentLesson(schedule!!.friday)
                }

                when (dayPosition) {
                    1 -> nextLessons = getUpcomingLessons(schedule!!.monday)
                    2 -> nextLessons = getUpcomingLessons(schedule!!.tuesday)
                    3 -> nextLessons = getUpcomingLessons(schedule!!.wednesday)
                    4 -> nextLessons = getUpcomingLessons(schedule!!.thursday)
                    5 -> nextLessons = getUpcomingLessons(schedule!!.friday)
                }

            } catch (e: java.lang.NullPointerException) {
                currentLesson = null
                Log.d("CURRENT LESSON", "NullPointerException") // не викликається
            }

            Log.d("SCHEDULE", "Got this: $newSchedule")
            Log.d("CURRENT LESSON", "Got this: $currentLesson")

            withContext(Dispatchers.Main) {
                // Proceed to the main content after fetching the data
                startMainActivity()
            }
        }
    }

    private fun startMainActivity() {
        // Continue with your normal logic, loading the main UI
        // Data fetching is complete at this point

        val homeFragment = HomeFragment()
        replaceFragment(homeFragment)
        supportFragmentManager.executePendingTransactions()
        homeFragment.setSchedule(schedule)
        homeFragment.setCurrentLesson(currentLesson, nextLessons)

        b.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.homeMenuItem -> {
                    replaceFragment(homeFragment)
                    supportFragmentManager.executePendingTransactions()
                    homeFragment.setSchedule(schedule)
                    homeFragment.setCurrentLesson(currentLesson, nextLessons)
                }

                R.id.scheduleMenuItem -> {
                    if (schedule === null) {
                        replaceFragment(NoInternetFragment.newInstance("Включи інтернет, придурок"))
                        Log.d("FRAGMENT ERROR", "Schedule is null")
                        Toast.makeText(
                            applicationContext,
                            "Німа доступу до інтернету",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        replaceFragment(ScheduleFragment().setSchedule(schedule, curDayPosition))
                    }
                }

                R.id.linksMenuItem -> replaceFragment(LinksFragment())

                else -> {}
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment)
        fragmentTransaction.commit()
    }

    private fun getCurrentLesson(todaySchedule: List<Lesson>): Lesson? {
        val currentTime = LocalTime.now()
        val currentTimeInMinutes = currentTime.hour * 60 + currentTime.minute
        for (lesson in todaySchedule) {
            val startTime = (lesson.start!!.hour * 60) + lesson.start!!.minute
            val endTime = (lesson.end!!.hour * 60) + lesson.end!!.minute
            if (currentTimeInMinutes in startTime - 5..endTime) {
                return lesson
            } else {
                continue
            }
        }
        return null
    }

    private fun getUpcomingLessons(todaySchedule: List<Lesson>): List<Lesson> {
        // Get the current time
        val currentTime = LocalTime.now()
        val currentTimeInMinutes = currentTime.hour * 60 + currentTime.minute

        // Filter the lessons that start after the current time
        val upcoming = todaySchedule.filter { lesson ->
            val lessonStartTimeInMinutes = lesson.start!!.hour * 60 + lesson.start!!.minute
            // Return a Boolean condition for filtering
            lessonStartTimeInMinutes > currentTimeInMinutes
        }

        return upcoming
    }

//    private fun afterSplashScreen() {
//        setTheme(R.style.Theme_Timetable)
//
//        replaceFragment(HomeFragment().setSchedule(schedule))
//
//        b.bottomNavigationView.selectedItemId = R.id.homeMenuItem
//
//        val testSchedule = createTestSchedule()
//        currentLesson = getCurrentLesson(testSchedule)
//
//        b.bottomNavigationView.setOnItemSelectedListener {
//            when (it.itemId) {
//                R.id.homeMenuItem -> {
//                    val homeFragment = HomeFragment()
//                    replaceFragment(homeFragment)
//                    supportFragmentManager.executePendingTransactions()
//                    homeFragment.setSchedule(schedule)
//                    homeFragment.setCurrentLesson(currentLesson)
//                }
//
//                R.id.scheduleMenuItem -> {
//                    if (schedule === null) {
//                        replaceFragment(NoInternetFragment.newInstance("Включи інтернет, придурок"))
//                        Log.d("FRAGMENT ERROR", "Schedule is null")
//                        Toast.makeText(
//                            applicationContext,
//                            "Німа доступу до інтернету",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    } else {
//                        replaceFragment(ScheduleFragment().setSchedule(schedule))
//                    }
//                }
//
//                R.id.linksMenuItem -> replaceFragment(LinksFragment())
//
//                else -> {}
//            }
//            true
//        }
//
//        replaceFragment(HomeFragment().setSchedule(schedule))
//    }

//    private fun whileSplashScreen() {
//        lifecycleScope.launch {
//            val newSchedule = serverAPI.fetchAllData()
//            if (newSchedule != null) {
//                schedule = newSchedule
//                Log.d("Data", "Data: $schedule")
//            } else {
//                Log.d("Data","Failed to fetch data")
//            }
//
//            currentLesson = getFastCurrentLesson(newSchedule)
//
//            Log.d("SCHEDULE", "Got this: $newSchedule")
//            Log.d("CURRENT LESSON", "Got this: $currentLesson")
//
//            afterSplashScreen()
//
//        }
//    }

//    private fun getFastCurrentLesson(schedule: Schedule?): Lesson? {
//        val currentDay = LocalDate.now().dayOfWeek
//        val dayPosition = currentDay.value
//        Log.d("CURRENT dayPosition", "$dayPosition")
//
//        try {
//            when (dayPosition) {
//                1 -> currentLesson = getCurrentLesson(schedule!!.monday)!!
//                2 -> currentLesson = getCurrentLesson(schedule!!.tuesday)!!
//                3 -> currentLesson = getCurrentLesson(schedule!!.wednesday)!!
//                4 -> currentLesson = getCurrentLesson(schedule!!.thursday)!!
//                5 -> currentLesson = getCurrentLesson(schedule!!.friday)!!
//            }
//        } catch (e: java.lang.NullPointerException) {
//            currentLesson = null
//            Log.d("CURRENT LESSON", "NullPointerException") // не викликається
//        }
//
//        Log.d("CURRENT LESSON", "$currentLesson")
//        return currentLesson
//    }



//    private fun getSchedule(snackbarApply: Boolean, view: View?) {
//        lifecycleScope.launch {
//            val newSchedule = serverAPI.fetchAllData()
//            if (newSchedule != null) {
//                schedule = newSchedule
//                Log.d("Data", "Data: $schedule")
//            } else {
//                Log.d("Data","Failed to fetch data")
//                if (snackbarApply) {
//                    Snackbar.make(view!!, "Failed to fetch data", Snackbar.LENGTH_LONG).show()
//                }
//            }
//        }
//    }

    private fun createTestSchedule(): List<Lesson> {
        return listOf(
                Lesson(
                    subject = Subject("Math", importance = 1, link = "https://example.com/math"),
                    start = TimeSlot(hour = 8, minute = 0),
                    end = TimeSlot(hour = 8, minute = 45)
                ),
                Lesson(
                    subject = Subject("English", importance = 1, link = "https://example.com/english"),
                    start = TimeSlot(hour = 9, minute = 0),
                    end = TimeSlot(hour = 9, minute = 45)
                ),
                Lesson(
                    subject = Subject("History", importance = 2, link = "https://example.com/history"),
                    start = TimeSlot(hour = 10, minute = 0),
                    end = TimeSlot(hour = 10, minute = 45)
                ),
                Lesson(
                    subject = Subject("Physics", importance = 2, link = "https://example.com/physics"),
                    start = TimeSlot(hour = 11, minute = 0),
                    end = TimeSlot(hour = 11, minute = 45)
                ),
                Lesson(
                    subject = Subject("Biology", importance = 3, link = "https://example.com/biology"),
                    start = TimeSlot(hour = 12, minute = 0),
                    end = TimeSlot(hour = 12, minute = 45)
                ),
                Lesson(
                    subject = Subject("Art", importance = 3, link = "https://example.com/art"),
                    start = TimeSlot(hour = 13, minute = 0),
                    end = TimeSlot(hour = 13, minute = 45)
                ),
                Lesson(
                    subject = Subject("Geography", importance = 2, link = "https://example.com/geography"),
                    start = TimeSlot(hour = 14, minute = 0),
                    end = TimeSlot(hour = 14, minute = 45)
                ),
                Lesson(
                    subject = Subject("Chemistry", importance = 2, link = "https://example.com/chemistry"),
                    start = TimeSlot(hour = 15, minute = 0),
                    end = TimeSlot(hour = 23, minute = 59)
                )
            )
    }
}