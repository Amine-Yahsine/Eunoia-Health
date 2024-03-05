package com.example.eunoiahealth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MoodActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood)

        firestore = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        setupMoodButtons()
    }

    private fun setupMoodButtons() {
        findViewById<ImageButton>(R.id.btnMoodHappy).setOnClickListener {
            recordMood(3)
        }
        findViewById<ImageButton>(R.id.btnMoodNeutral).setOnClickListener {
            recordMood(2)
        }
        findViewById<ImageButton>(R.id.btnMoodSad).setOnClickListener {
            recordMood(1)
        }
    }

    private fun recordMood(moodValue: Int) {
        val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val moodDocRef = firestore.collection("users").document(userId)
            .collection("Moods").document(currentDate)

        moodDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                Toast.makeText(this, "You've already entered your mood today.", Toast.LENGTH_SHORT).show()
            } else {
                val moodData = hashMapOf(
                    "mood" to moodValue,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                moodDocRef.set(moodData).addOnSuccessListener {
                    Toast.makeText(this, "Mood recorded successfully.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to record mood.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
