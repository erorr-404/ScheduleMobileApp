package com.erorr404.timetable

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

/**
 * A simple [Fragment] subclass.
 * Use the [ScheduleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ScheduleFragment : Fragment() {

    private var schedule: Schedule? = null
    private var currentDay = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            val scheduleRecyclerView: RecyclerView = view.findViewById(R.id.scheduleRecyclerView)
            scheduleRecyclerView.layoutManager = LinearLayoutManager(this.context)
            scheduleRecyclerView.adapter = ScheduleRecyclerAdapter(this.requireContext(), schedule!!)

        } catch (e: java.lang.NullPointerException) {
            Log.d("FRAGMENT ERROR", "$e")
            return
        }

    }

    fun setSchedule(newSchedule: Schedule?, curDay: Int): ScheduleFragment {
        schedule = if ((schedule == null) or ((schedule != newSchedule) and (schedule != null))) {
            newSchedule
        } else {
            null
        }
        currentDay = curDay
        return this
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ScheduleFragment()
    }
}