package com.zybooks.quickdraw.model

/**
 * represents player
 */
data class Player(
    val name: String,
    var score: Int = 0,
    var isActive: Boolean = false
)

/**
 * represents category of words
 */
data class Category(
    val name: String,
    val words: List<String> = emptyList(),
    val difficulty: Difficulty = Difficulty.MEDIUM
)

/**
 * difficulty levels (may not get to but here if we do!)
 */
enum class Difficulty {
    EASY, MEDIUM, HARD
}

/**
 * settings configuration
 */
data class GameSettings(
    val timerDurationSeconds: Int = 15,
    val minPlayers: Int = 2,
    val maxPlayers: Int = 8,
    val enableSoundEffects: Boolean = true,
    val enableVibration: Boolean = true
)

/**
 * sample categories for the game
 */
object SampleCategories {
    val categories = listOf(
        Category(
            name = "Fruits",
            words = listOf("Apple", "Banana", "Orange", "Grape", "Strawberry", "Pineapple",
                "Watermelon", "Kiwi", "Mango", "Peach", "Pear", "Plum", "Cherry")
        ),
        Category(
            name = "Animals",
            words = listOf("Dog", "Cat", "Elephant", "Lion", "Tiger", "Bear", "Giraffe",
                "Monkey", "Zebra", "Snake", "Dolphin", "Horse", "Cow", "Sheep")
        ),
        Category(
            name = "Countries",
            words = listOf("USA", "Canada", "Japan", "Australia", "Brazil", "France", "Germany",
                "Italy", "Spain", "China", "India", "Russia", "Mexico", "Egypt")
        ),
        Category(
            name = "Sports",
            words = listOf("Soccer", "Basketball", "Tennis", "Golf", "Swimming", "Baseball",
                "Volleyball", "Hockey", "Football", "Rugby", "Cricket", "Boxing")
        ),
        Category(
            name = "Colors",
            words = listOf("Red", "Blue", "Green", "Yellow", "Purple", "Orange", "Pink",
                "Brown", "Black", "White", "Gray", "Gold", "Silver", "Teal")
        )
    )

    /**
     * get a random category from samples
     */
    fun getRandomCategory(): Category {
        return categories.random()
    }
}