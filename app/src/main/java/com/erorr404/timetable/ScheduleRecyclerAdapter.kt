package com.erorr404.timetable

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScheduleRecyclerAdapter(private val context: Context, private val schedule: Schedule) : RecyclerView.Adapter<ScheduleRecyclerAdapter.AdapterViewHolder>() {
    private lateinit var daysList: List<List<Lesson>>
    // ViewHolder for the parent item
    class AdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayTitleTextView: TextView = itemView.findViewById(R.id.dayName)
        val childListView: ListView = itemView.findViewById(R.id.lessonListView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.schedule_day, parent, false)

        daysList = listOf(schedule.monday, schedule.tuesday, schedule.wednesday, schedule.thursday, schedule.friday)

        return AdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdapterViewHolder, position: Int) {
        var dayName = ""
        val lessonList: MutableList<ScheduleListItem> = listOf<ScheduleListItem>().toMutableList()
        when (position) {
            0 -> dayName = "Monday"
            1 -> dayName = "Tuesday"
            2 -> dayName = "Wednesday"
            3 -> dayName = "Thursday"
            4 -> dayName = "Friday"
        }
        val parentItem = daysList[position]

        parentItem.forEach {
            lessonList.add(ScheduleListItem(it.subject!!.name, it.subject!!.link, it.start!!))
        }

        holder.dayTitleTextView.text = dayName

        // Set up the ListView inside the parent item
        val childAdapter = ScheduleListAdapter(context, lessonList)
        holder.childListView.adapter = childAdapter

        // Set ListView height based on number of children
        setListViewHeightBasedOnChildren(holder.childListView)
    }

    // Returns the total number of items in the list
    override fun getItemCount(): Int {
        return 5
    }

    // Helper method to fix ListView height inside RecyclerView
    private fun setListViewHeightBasedOnChildren(listView: ListView) {
        val listAdapter = listView.adapter ?: return
        var totalHeight = 0
        for (i in 0 until listAdapter.count) {
            val listItem = listAdapter.getView(i, null, listView)
            listItem.measure(
                View.MeasureSpec.makeMeasureSpec(listView.width, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            totalHeight += listItem.measuredHeight
        }

        val params = listView.layoutParams
        params.height = totalHeight + (listView.dividerHeight * (listAdapter.count - 1))
        listView.layoutParams = params
        listView.requestLayout()
    }
}