package com.example.eunoiahealth
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private lateinit var dailySteps: TextView
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    private lateinit var logoutButton: MaterialButton

    //Database stuff
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val dateFormat = SimpleDateFormat("YYYYMMDD", Locale.getDefault())
    private val todayDate = dateFormat.format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dailySteps = findViewById(R.id.dailySteps)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        loadStepsFromDB()

        logoutButton = findViewById(R.id.logout_button)

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
    //Look here first if anything goes wrong
    private fun loadStepsFromDB(){
        userId?.let {uid ->
            db.collection("users").document(uid)
                .collection("dailySteps").document(todayDate)
                .get()
                .addOnSuccessListener { document ->
                    if(document.exists()) {
                        val steps = document.getLong("steps")?:0
                        previousTotalSteps = steps.toFloat()
                        dailySteps.text = "Daily Steps: $steps"
                    }
                }
        }
    }
    override fun onResume() {
        super.onResume()
        stepSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        saveSteps()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_STEP_COUNTER){
            totalSteps = event.values[0]
            updateUI()
        }
    }

    //and here
    private fun updateUI(){
        val currentSteps = totalSteps - previousTotalSteps
        dailySteps.text = "Daily Steps: ${currentSteps.toInt()}"
    }
    // and here
    private fun saveSteps(){
        val currentSteps = totalSteps - previousTotalSteps
        userId?.let {uid ->
            db.collection("users").document(uid)
                .collection("dailySteps").document(todayDate)
                .set(mapOf("steps" to currentSteps.toInt()))
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //Not handling changes in accuracy
    }
}
