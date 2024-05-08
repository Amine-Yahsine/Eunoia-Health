package com.example.eunoiahealth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class GraphsActivity : AppCompatActivity() {

    private lateinit var lineChartSteps: LineChart
    private lateinit var barChartMood: BarChart
    private val firestore = FirebaseFirestore.getInstance()
    private val userId: String by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graphs)

        lineChartSteps = findViewById(R.id.lineChartSteps)
        barChartMood = findViewById(R.id.barChartMood)

        if (userId.isNotBlank()) {
            setupCharts()
        }
    }

    private fun setupCharts() {
        lifecycleScope.launch {
            val dates = getLast7DaysDates()
            val stepsEntries = fetchStepsData(dates)
            val moodEntries = fetchMoodData(dates)

            withContext(Dispatchers.Main) {
                updateStepsChart(stepsEntries)
                updateMoodChart(moodEntries)
            }
        }
    }

    private fun getLast7DaysDates(): List<String> {
        val calendar = Calendar.getInstance()
        return List(7) { index ->
            calendar.add(Calendar.DATE, if (index == 0) 0 else -1)
            dateFormat.format(calendar.time)
        }.reversed()
    }

    private suspend fun fetchStepsData(dates: List<String>): List<Entry> {
        val entries = ArrayList<Entry>()
        dates.forEachIndexed { index, date ->
            try {
                val document = firestore.collection("users").document(userId)
                    .collection("dailySteps").document(date).get().await()
                val steps = document.getLong("steps")?.toFloat() ?: 0f
                if (steps > 0) {
                    entries.add(Entry(index.toFloat(), steps))
                }
            } catch (e: Exception) {
                showError("Failed to fetch step data for $date")
            }
        }
        return entries
    }

    private suspend fun fetchMoodData(dates: List<String>): List<BarEntry> {
        val entries = ArrayList<BarEntry>()
        dates.forEachIndexed { index, date ->
            try {
                val document = firestore.collection("users").document(userId)
                    .collection("Moods").document(date).get().await()
                val mood = document.getLong("mood")?.toFloat() ?: 2f
                entries.add(BarEntry(index.toFloat(), mood))
            } catch (e: Exception) {
                showError("Failed to fetch mood data for $date")
            }
        }
        return entries
    }

    private fun updateStepsChart(entries: List<Entry>) {
        val dataSet = LineDataSet(entries, "Daily Steps")
        val lineData = LineData(dataSet)
        lineChartSteps.data = lineData
        lineChartSteps.invalidate() // Refresh chart
    }

    private fun updateMoodChart(entries: List<BarEntry>) {
        val dataSet = BarDataSet(entries, "Daily Mood")
        val barData = BarData(dataSet)
        barChartMood.data = barData
        barChartMood.invalidate() // Refresh chart
    }

    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}
