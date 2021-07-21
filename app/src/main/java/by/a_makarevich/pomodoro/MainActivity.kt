package by.a_makarevich.pomodoro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import by.a_makarevich.pomodoro.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), LifecycleObserver, StopwatchListener {

    private lateinit var binding: ActivityMainBinding
    private val LOG = "MyLog"

    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0

    var startTimeForService = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.recycler.apply {
            adapter = stopwatchAdapter
        }


        binding.addNewStopwatchButton.setOnClickListener {

            val minutes = binding.editTextMinutes.text.toString()

            if (minutes == "") {
                Toast.makeText(
                    this,
                    "Please, input minutes",
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                stopwatches.add(Stopwatch(nextId++, minutes.toLong() * 1000L * 60L, false))
                stopwatchAdapter.submitList(stopwatches.toList())
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        Log.d(LOG, "@OnLifecycleEvent(Lifecycle.Event.ON_STOP)")



        stopwatches.forEach {
            if (it.isStarted) {
                startTimeForService = it.currentMs
                val startIntent = Intent(this, ForegroundService::class.java)
                startIntent.putExtra(COMMAND_ID, COMMAND_START)
                startIntent.putExtra(STARTED_TIMER_TIME_MS, startTimeForService)
                startService(startIntent)
            }
        }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        Log.d(LOG, "@OnLifecycleEvent(Lifecycle.Event.ON_START)")
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)

    }


    override fun start(id: Int) {
        changeStopwatch(id, null, true)
    }

    override fun stop(id: Int, currentMs: Long) {
        changeStopwatch(id, currentMs, false)
    }

    override fun delete(id: Int) {
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<Stopwatch>()
        stopwatches.forEach {
            if (it.id == id) {
                newTimers.add(Stopwatch(it.id, currentMs ?: it.currentMs, isStarted))
            } else {
                newTimers.add(Stopwatch(it.id, it.currentMs, false))
            }
        }
        stopwatchAdapter.apply {
            submitList(newTimers)
        }
        stopwatches.clear()
        stopwatches.addAll(newTimers)
    }
}
