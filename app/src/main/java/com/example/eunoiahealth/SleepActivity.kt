package com.example.eunoiahealth

import android.os.Bundle
import android.widget.Button
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class SleepActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep)

        val startTimePicker: TimePicker = findViewById(R.id.startTimePicker)
        val endTimePicker: TimePicker = findViewById(R.id.endTimePicker)
        val btnSaveSleep: Button = findViewById(R.id.btnSaveSleep)

        btnSaveSleep.setOnClickListener {
            saveSleepData(startTimePicker, endTimePicker)
        }
    }

    private fun saveSleepData(startTimePicker: TimePicker, endTimePicker: TimePicker) {
        val startHour = startTimePicker.hour
        val startMinute = startTimePicker.minute
        val endHour = endTimePicker.hour
        val endMinute = endTimePicker.minute

        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val startTime = sdf.format(Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
        }.time)
        val endTime = sdf.format(Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, endHour)
            set(Calendar.MINUTE, endMinute)
        }.time)

        val sleepData = hashMapOf(
            "start_time" to startTime,
            "end_time" to endTime,
            "date" to SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        )

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .collection("sleepData")
                .add(sleepData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Sleep data saved successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save sleep data", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
