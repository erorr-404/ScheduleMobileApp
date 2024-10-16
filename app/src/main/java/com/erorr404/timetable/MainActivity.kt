package com.erorr404.timetable

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.erorr404.timetable.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.LocalTime
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private var serverAPI = ServerAPI()
    private var schedule: Schedule? = null
    private lateinit var b: ActivityMainBinding
    private var currentLesson: Lesson? = null
    private var curDayPosition = 1
    private var nextLessons: List<Lesson>? = null
    private lateinit var notificationSwitch: com.google.android.material.materialswitch.MaterialSwitch
    private lateinit var scheduler: AndroidAlarmScheduler
    private var notificationList: MutableList<AlarmItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        createNotificationChannel()
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            val intent = Intent(this, OnErrorActivity::class.java)
            val args: ArrayList<String?> = arrayListOf(throwable.cause.toString(), throwable.message, throwable.localizedMessage, throwable.stackTrace.toString())
            intent.putStringArrayListExtra("error_info", args)
            startActivity(intent)
        }

        fetchDataAndProceed()

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
        scheduler = AndroidAlarmScheduler(this@MainActivity)
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
                        replaceFragment(NoInternetFragment.newInstance("Немає доступу до інтернету"))
                        Log.d("FRAGMENT ERROR", "Schedule is null")
                        Toast.makeText(
                            applicationContext,
                            "Немає доступу до інтернету",
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

        notificationSwitch = findViewById(R.id.notificationSwitch)
        // Load switch state from SharedPreferences when the activity is created
        val sharedPref = getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        val isNotificationsEnabled = sharedPref.getBoolean("notificationsEnabled", false)
        notificationSwitch.isChecked = isNotificationsEnabled
        val notificationsDisabled = sharedPref.getBoolean("NotificationsDenied", false)
        notificationSwitch.isEnabled = !notificationsDisabled

        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Enable notifications for all lessons
                lifecycleScope.launch {
                    scheduleLessonNotifications(schedule!!)
                }
            } else {
                // Cancel all scheduled notifications
                lifecycleScope.launch {
                    cancelAllLessonNotifications()
                }
            }

            // Save switch state in SharedPreferences
            lifecycleScope.launch {
                with(sharedPref.edit()) {
                    putBoolean("notificationsEnabled", isChecked)
                    apply()
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            )  {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
    }

    private fun scheduleLessonNotifications(schedule: Schedule) {
//        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val daysOfWeek = listOf(
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY
        )
        val lessonsPerDay = listOf(
            schedule.monday, schedule.tuesday, schedule.wednesday, schedule.thursday, schedule.friday
        )

        for ((dayIndex, dayLessons) in lessonsPerDay.withIndex()) {
            for (lesson in dayLessons) {

                val calendar = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    set(Calendar.DAY_OF_WEEK, daysOfWeek[dayIndex])
                    set(Calendar.HOUR_OF_DAY, lesson.start!!.hour)
                    set(Calendar.MINUTE, lesson.start!!.minute)
                    set(Calendar.SECOND, 0)
                    add(Calendar.MINUTE, -3)
                }

                val alarmItem = AlarmItem(
                    calendar,
                    lesson.subject!!.name,
                    "${lesson.subject!!.name} починається о ${lesson.start!!.hour}:${lesson.start!!.minute}",
                    lesson.subject!!.link
                )

                scheduler.schedule(alarmItem)
                notificationList.add(alarmItem)
            }
        }
        Log.d("NOTIFICATIONS", "Scheduled notifications for all Lessons")
    }

    private fun cancelAllLessonNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            scheduler.cancelAll()
        } else {
            for (notification in notificationList) {
                scheduler.cancel(notification)
                notificationList.remove(notification)
            }
            Log.i("NOTIFICATIONS", "SDK version is lower 34. Canceled all Alarms manually.")
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

    private fun createNotificationChannel() {
        val soundUri: Uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.raw.alarm)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val channelId = "lesson_new_channel"
        val channelName = "Lesson Notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = "Channel for lesson start notifications"
            setSound(soundUri, audioAttributes)
        }

        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createTestSchedule(): List<Lesson> {
        return listOf(
                Lesson(
                    subject = Subject(name="Math", importance = 1, link = "https://example.com/math"),
                    start = TimeSlot(hour = 8, minute = 0),
                    end = TimeSlot(hour = 8, minute = 45)
                ),
                Lesson(
                    subject = Subject(name="English", importance = 1, link = "https://example.com/english"),
                    start = TimeSlot(hour = 9, minute = 0),
                    end = TimeSlot(hour = 9, minute = 45)
                ),
                Lesson(
                    subject = Subject(name="History", importance = 2, link = "https://example.com/history"),
                    start = TimeSlot(hour = 10, minute = 0),
                    end = TimeSlot(hour = 10, minute = 45)
                ),
                Lesson(
                    subject = Subject(name="Physics", importance = 2, link = "https://example.com/physics"),
                    start = TimeSlot(hour = 11, minute = 0),
                    end = TimeSlot(hour = 11, minute = 45)
                ),
                Lesson(
                    subject = Subject(name="Biology", importance = 3, link = "https://example.com/biology"),
                    start = TimeSlot(hour = 12, minute = 0),
                    end = TimeSlot(hour = 12, minute = 45)
                ),
                Lesson(
                    subject = Subject(name="Art", importance = 3, link = "https://example.com/art"),
                    start = TimeSlot(hour = 13, minute = 0),
                    end = TimeSlot(hour = 13, minute = 45)
                ),
                Lesson(
                    subject = Subject(name="Geography", importance = 2, link = "https://example.com/geography"),
                    start = TimeSlot(hour = 14, minute = 0),
                    end = TimeSlot(hour = 14, minute = 45)
                ),
                Lesson(
                    subject = Subject(name="Chemistry", importance = 2, link = "https://example.com/chemistry"),
                    start = TimeSlot(hour = 15, minute = 0),
                    end = TimeSlot(hour = 23, minute = 59)
                )
            )
    }
}