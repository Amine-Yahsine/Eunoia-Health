package com.example.eunoiahealth.ui.theme

data class FoodResponse(
    val calories: Int, // Ensure these fields match the structure of the JSON response
    val label: String,
    val nutrients: Nutrients
)

data class Nutrients(
    val fat: Double,
    val protein: Double,
    val carbs: Double
)

