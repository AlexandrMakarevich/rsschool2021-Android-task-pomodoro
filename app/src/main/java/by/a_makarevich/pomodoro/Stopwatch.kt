package by.a_makarevich.pomodoro

data class Stopwatch(
    val id: Int,
    var currentMs: Long,
    var isStarted: Boolean
)
