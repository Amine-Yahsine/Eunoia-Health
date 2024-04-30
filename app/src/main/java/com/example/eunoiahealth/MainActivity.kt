package com.example.eunoiahealth
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
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
    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    private val todayDate = dateFormat.format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dailySteps = findViewById(R.id.dailySteps)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        loadStepsFromDB()
        loadUserProfile()

        logoutButton = findViewById(R.id.logout_button)

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val recordMoodButton: MaterialButton = findViewById(R.id.recordMoodButton)
        recordMoodButton.setOnClickListener {
            val intent = Intent(this, MoodActivity::class.java)
            startActivity(intent)
        }
        findViewById<MaterialButton>(R.id.viewGraphsButton).setOnClickListener {
            val intent = Intent(this, GraphsActivity::class.java)
            startActivity(intent)
        }
        val profileImage: ImageView = findViewById(R.id.profile_image)
        profileImage.setOnClickListener {
            val intent = Intent(this, ProfileCustomizationActivity::class.java)
            startActivity(intent)
        }
        val sleepTrackingButton: MaterialButton = findViewById(R.id.sleepTrackingButton)
        sleepTrackingButton.setOnClickListener {
            val intent = Intent(this, SleepActivity::class.java)
            startActivity(intent)
        }
        val foodTrackingButton: Button = findViewById(R.id.foodTrackingButton)
        foodTrackingButton.setOnClickListener {
            val intent = Intent(this, FoodActivity::class.java)
            startActivity(intent)
        }
        val waterTrackingButton: Button = findViewById(R.id.btnWaterTracking)
        waterTrackingButton.setOnClickListener {
            val intent = Intent(this, WaterTrackingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .get(Source.SERVER)
                .addOnSuccessListener { documentSnapshot ->
                    val imageUrl = documentSnapshot.getString("photoUrl")
                    val imageView: ImageView = findViewById(R.id.profile_image)
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .skipMemoryCache(true) // Skip memory cache
                            .diskCacheStrategy(DiskCacheStrategy.NONE) // Skip disk cache
                            .into(imageView)
                    }
                    val username = documentSnapshot.getString("username") ?: "Anonymous"
                    val usernameTextView: TextView = findViewById(R.id.usernameTextView)
                    usernameTextView.text = username
                }
                .addOnFailureListener { e ->

                }
        }
    }
    //Look here first if anything goes wrong
    private fun loadStepsFromDB(){
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val todayDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            db.collection("users").document(uid)
                .collection("dailySteps").document(todayDate)
                .get()
                .addOnSuccessListener { document ->
                    if(document.exists()) {
                        val steps = document.getLong("steps") ?: 0
                        previousTotalSteps = steps.toFloat()
                    } else {
                        previousTotalSteps = totalSteps // Initialize if no entry for today
                    }
                    updateUI() // Ensure UI is updated regardless
                }
        }
    }
    override fun onResume() {
        super.onResume()
        stepSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        loadUserProfile()
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
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val todayDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            db.collection("users").document(uid)
                .collection("dailySteps").document(todayDate)
                .set(mapOf("steps" to currentSteps.toInt(), "timestamp" to FieldValue.serverTimestamp()))
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //Not handling changes in accuracy
    }

}
