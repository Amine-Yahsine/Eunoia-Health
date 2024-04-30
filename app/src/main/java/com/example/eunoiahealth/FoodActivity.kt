package com.example.eunoiahealth

import FoodResponse  // Adjust package name as needed
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FoodActivity : AppCompatActivity() {
    private lateinit var foodInput: EditText
    private lateinit var calorieCount: TextView
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Initialize Retrofit
    private val edamamService: EdamamService = RetrofitClient.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food)

        foodInput = findViewById(R.id.foodInput)
        val addFoodButton: Button = findViewById(R.id.addFoodButton)
        calorieCount = findViewById(R.id.calorieCount)

        addFoodButton.setOnClickListener {
            val foodItem = foodInput.text.toString()
            if (foodItem.isNotEmpty()) {
                getCaloriesForFood(foodItem)
            } else {
                Toast.makeText(this, "Please enter a food item", Toast.LENGTH_SHORT).show()
            }
        }

        updateCalorieDisplay()
    }

    private fun getCaloriesForFood(food: String) {
        edamamService.getFoodInfo(food, "a8e414d9", "a8571884cfd2352f8f309618f61b5d73").enqueue(object : Callback<FoodResponse> {
            override fun onResponse(call: Call<FoodResponse>, response: Response<FoodResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val calories = it.parsed.firstOrNull()?.food?.nutrients?.ENERC_KCAL ?: 0.0
                        if (calories == 0.0) {
                            Toast.makeText(applicationContext, "No calories data found for this item.", Toast.LENGTH_LONG).show()
                        } else {
                            addCaloriesToFirestore(calories)
                        }
                    }
                } else {
                    Toast.makeText(applicationContext, "Failed to retrieve data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FoodResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun addCaloriesToFirestore(calories: Double) {
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val docRef = db.collection("users").document(userId).collection("calories")
            .document("current")

        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            if (!snapshot.exists()) {
                transaction.set(docRef, hashMapOf("calorieCount" to 0.0)) // Initialize if doesn't exist
            }
            val currentCalories = snapshot.getDouble("calorieCount") ?: 0.0
            val newCalorieCount = currentCalories + calories
            transaction.update(docRef, "calorieCount", newCalorieCount)
            null // Return null to indicate success in the transaction
        }.addOnSuccessListener {
            updateCalorieDisplay()
            finish()  // Finish this activity and return to MainActivity
        }.addOnFailureListener { e ->
            Log.e("FirestoreError", "Failed to update calories: ${e.message}", e)
            Toast.makeText(this, "Failed to update calories: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }




    private fun updateCalorieDisplay() {
        if (userId != null) {
            val docRef = db.collection("users").document(userId).collection("calories")
                .document("current")

            docRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val currentCalories = document.getDouble("calorieCount") ?: 0.0
                    calorieCount.text = "Total Calories: $currentCalories"
                } else {
                    calorieCount.text = "Total Calories: 0"
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load calories", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
