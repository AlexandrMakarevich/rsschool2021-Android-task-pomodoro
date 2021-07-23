package by.a_makarevich.pomodoro

data class Stopwatch(
    val id: Int,
    var currentMs: Long,
    val maxMs: Long,
    var isStarted: Boolean,
    var isFinished: Boolean
)
