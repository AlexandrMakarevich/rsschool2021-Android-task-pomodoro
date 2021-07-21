package by.a_makarevich.pomodoro

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import by.a_makarevich.pomodoro.databinding.StopwatchItemBinding
import kotlinx.coroutines.*

class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener,
    private val resources: Resources
) : RecyclerView.ViewHolder(binding.root) {

    private val LOG = "MyLog"

    private var timer: CountDownTimer? = null

    private var scope = CoroutineScope(Dispatchers.Main)

    private var globalPeriod = 0L


    fun bind(stopwatch: Stopwatch) {
        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()

        if (stopwatch.isStarted) {
            startTimer(stopwatch)
        } else {
            stopTimer(stopwatch)
        }

        Log.d(LOG, "bind!!! ${stopwatch.id}")

        initButtonsListeners(stopwatch)

        if (globalPeriod == 0L) {
            globalPeriod = stopwatch.currentMs
            binding.customView.setPeriod(globalPeriod)
        }

    }


    private fun initButtonsListeners(stopwatch: Stopwatch) {

        binding.startPauseButton.setOnClickListener {


            if (stopwatch.isStarted) {
                listener.stop(stopwatch.id, stopwatch.currentMs)
            } else {
                listener.start(stopwatch.id)
            }
        }

        binding.deleteButton.setOnClickListener {
            scope.cancel()
            listener.delete(stopwatch.id)
        }
    }


    private fun startTimer(stopwatch: Stopwatch) {

        Log.d(LOG, "fun startTimer")

        val drawable = resources.getDrawable(R.drawable.ic_baseline_pause_24)
        binding.startPauseButton.setImageDrawable(drawable)

        timer?.cancel()
        timer = getCountDownTimer(stopwatch)
        timer?.start()


        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()


        // start customView
        val scope = CoroutineScope(Dispatchers.Main)

        scope.launch {
            while (stopwatch.currentMs > 0) {
                stopwatch.currentMs -= INTERVAL
                binding.customView.setCurrent(globalPeriod - stopwatch.currentMs)
                delay(INTERVAL)
            }
        }
        this.scope = scope
    }


    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        return object : CountDownTimer(stopwatch.currentMs, UNIT_TEN_MS) {

            var period = stopwatch.currentMs

            override fun onTick(millisUntilFinished: Long) {
                period -= INTERVAL
                binding.stopwatchTimer.text = period.displayTime()
                stopwatch.currentMs = period
            }

            override fun onFinish() {

                binding.stopwatchTimer.text = START_TIME

                binding.blinkingIndicator.isInvisible = true
                (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
                
                scope.cancel()

                stopwatch.isStarted = false
                val drawable = resources.getDrawable(R.drawable.ic_baseline_cancel_24)
                binding.startPauseButton.setImageDrawable(drawable)
                binding.startPauseButton.isEnabled = false

                Toast.makeText(
                    binding.deleteButton.context,
                    "Timer № ${stopwatch.id+1}  stoped!!!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    private fun stopTimer(stopwatch: Stopwatch) {

        Log.d(LOG, "fun stopTimer")

        val drawable = resources.getDrawable(R.drawable.ic_baseline_play_arrow_24)
        binding.startPauseButton.setImageDrawable(drawable)

        timer?.cancel()
        scope.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()

    }

    private companion object {


        private const val UNIT_TEN_MS = 100L
        //  private const val PERIOD = 1000L * 60L * 60L * 24L // Day

        // CustomView
        private const val INTERVAL = 100L
        private const val PERIOD_CUSTOM_VIEW = 1000L * 30 // 30 sec
        private const val REPEAT = 10 // 10 times


    }
}