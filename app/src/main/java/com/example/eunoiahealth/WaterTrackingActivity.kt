package com.example.eunoiahealth

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class WaterTrackingActivity : AppCompatActivity() {
    private lateinit var tvWaterIntakeTotal: TextView
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    private val todayDate = sdf.format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_water_tracking)

        tvWaterIntakeTotal = findViewById(R.id.tvWaterIntakeTotal)
        setupButtons()
        loadWaterIntake()

        val backToMainButton: Button = findViewById(R.id.btnBackToMain)
        backToMainButton.setOnClickListener {
            finish()
        }

    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnAdd100ml).setOnClickListener { adjustWaterIntake(100) }
        findViewById<Button>(R.id.btnAdd250ml).setOnClickListener { adjustWaterIntake(250) }
        findViewById<Button>(R.id.btnAdd500ml).setOnClickListener { adjustWaterIntake(500) }
    }

    private fun adjustWaterIntake(amount: Int) {
        val docRef = db.collection("users").document(userId ?: return)
            .collection("WaterIntake").document(todayDate)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val currentIntake = snapshot.getDouble("intake") ?: 0.0
            val newIntake = currentIntake + amount
            transaction.set(docRef, mapOf("intake" to newIntake))
            newIntake
        }.addOnSuccessListener { newIntake ->
            updateWaterIntakeDisplay(newIntake.toInt())
        }.addOnFailureListener { e ->
            e.printStackTrace()
            Toast.makeText(this, "Failed to update intake", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadWaterIntake() {
        val docRef = db.collection("users").document(userId ?: return)
            .collection("WaterIntake").document(todayDate)

        docRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val currentIntake = document.getDouble("intake") ?: 0.0
                updateWaterIntakeDisplay(currentIntake.toInt())
            } else {
                updateWaterIntakeDisplay(0)
            }
        }
    }

    private fun updateWaterIntakeDisplay(intake: Int) {
        tvWaterIntakeTotal.text = "Total Intake: $intake ml"
    }
}
