data class FoodResponse(
    val text: String,
    val parsed: List<ParsedItem>
)

data class ParsedItem(
    val food: Food
)

data class Food(
    val foodId: String,
    val label: String,
    val nutrients: Nutrients,
    val category: String,
    val categoryLabel: String,
    val image: String?
)

data class Nutrients(
    val ENERC_KCAL: Double,
    val PROCNT: Double,
    val FAT: Double,
    val CHOCDF: Double,
    val FIBTG: Double
)
