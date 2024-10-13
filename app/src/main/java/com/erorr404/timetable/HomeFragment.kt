package com.erorr404.timetable

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalTime

/**
 * A simple [Fragment] subclass.
 */

const val SCHEDULE_KEY = "schedule"
const val CUR_LESSON_KEY = "current_lesson"

class HomeFragment() : Fragment() {

    private var schedule: Schedule? = null
    private var currentLesson: Lesson? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun setSchedule(newSchedule: Schedule?): HomeFragment {
        schedule = if ((schedule == null) or ((schedule != newSchedule) and (schedule != null))) {
            newSchedule
        } else {
            null
        }
        return this
    }

    fun setCurrentLesson(newCurrentLesson: Lesson?, nextLessons: List<Lesson>?): HomeFragment {
        currentLesson = newCurrentLesson

        val currentLessonView = requireView().findViewById<TextView>(R.id.currentLessonView)
        val connectToCurrentLessonButton = requireView().findViewById<Button>(R.id.connectToCurrentLesson)

        if (currentLesson == null) {
            currentLessonView.text = "Нащастя, уроку зараз немає"
            connectToCurrentLessonButton.text = "Надрукувати сраку на принтері"
            connectToCurrentLessonButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/PDaPqQd1Mns?si=G8IaSqdi-Jtcka7Ihttps://youtu.be/PDaPqQd1Mns?si=G8IaSqdi-Jtcka7I"))

                try {
                    ContextCompat.startActivity(requireContext(), intent, null)
                } catch (e: Exception) {
                    Toast.makeText(context, "Exception: $e", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            currentLessonView.text = currentLesson?.subject?.name
            connectToCurrentLessonButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentLesson?.subject?.link))

                try {
                    ContextCompat.startActivity(requireContext(), intent, null)
                } catch (e: Exception) {
                    Toast.makeText(context, "Exception: $e", Toast.LENGTH_SHORT).show()
                }
            }
        }

        if (nextLessons != null) {
            val listView = requireView().findViewById<ListView>(R.id.nextLessonsListView)
            val lessonList: MutableList<ScheduleListItem> = listOf<ScheduleListItem>().toMutableList()
            nextLessons.forEach {
                lessonList.add(ScheduleListItem(it.subject!!.name, it.subject!!.link, it.start!!))
            }
            val childAdapter = ScheduleListAdapter(requireContext(), lessonList)
            listView.adapter = childAdapter
        }

        return this
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        schedule = arguments?.getParcelable(SCHEDULE_KEY)
//        currentLesson = arguments?.getParcelable(CUR_LESSON_KEY)
//        setCurrentLesson(currentLesson)
    }

    companion object {
        fun newInstance(schedule: Schedule?, currentLesson: Lesson?): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle().apply {
                putParcelable(SCHEDULE_KEY, schedule)
                putParcelable(CUR_LESSON_KEY, currentLesson)
            }
            fragment.arguments = args
            return fragment
        }
    }
}