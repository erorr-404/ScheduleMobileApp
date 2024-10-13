package com.erorr404.timetable

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity

class ScheduleListAdapter(context: Context, childItemList: List<ScheduleListItem>) : ArrayAdapter<ScheduleListItem>(context, 0, childItemList) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.schedule_lesson, parent, false)

        val childItem = getItem(position)
        val childTitleTextView = view.findViewById<TextView>(R.id.lessonName)
        val joinButton = view.findViewById<Button>(R.id.joinButton)
        val lessonStartTimeText = view.findViewById<TextView>(R.id.lessonStartTimeText)
        joinButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(childItem?.link))
            // Check if there is no error
            try {
                startActivity(context, intent, null)
            } catch (e: Exception)  {
                // Optionally handle if no app is available (e.g., show a message to the user)
                Toast.makeText(context, "Exception: $e", Toast.LENGTH_SHORT).show()
            }
        }

        val stringBuilder = StringBuilder()
        stringBuilder.append(childItem?.start?.hour!!)

        stringBuilder.append(":")

        if (childItem.start.minute == 0) {
            stringBuilder.append("00")
        } else {
            stringBuilder.append(childItem.start.minute)
        }

        lessonStartTimeText.text = stringBuilder.toString()
//        lessonStartTimeText.text = "00:00"
        childTitleTextView.text = childItem.lesson

        return view
    }
}