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

    private var startTimeForService = 0L

    override fun onStop() {
        Log.d(LOG, "override fun onStop()")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(LOG, "override fun onDestroy()")
        super.onDestroy()
    }

    override fun onPause() {
        Log.d(LOG, "override fun onPause()")
        super.onPause()
    }

    override fun onStart() {
        Log.d(LOG, "override fun onStart()")
        super.onStart()
    }

    override fun onResume() {
        Log.d(LOG, "override fun onResume()")
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(LOG, "override fun onCreate")

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {

            adapter = stopwatchAdapter
        }

        binding.addNewStopwatchButton.setOnClickListener {

            when (val minutes = binding.editTextMinutes.text.toString()) {
                "" -> {
                    Toast.makeText(
                        this,
                        "Please, input minutes",
                        Toast.LENGTH_SHORT
                    ).show()

                }
                "0" -> {
                    Toast.makeText(
                        this,
                        "Please, input more, than 0",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    stopwatches.add(
                        Stopwatch(
                            nextId++,
                            minutes.toLong() * 1000L * 60L,
                            minutes.toLong() * 1000L * 60L,
                            false,
                            false
                        )
                    )
                    stopwatchAdapter.submitList(stopwatches.toList())
                }
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
                newTimers.add(Stopwatch(it.id, currentMs ?: it.currentMs, it.maxMs, isStarted, it.isFinished))
            } else {
                newTimers.add(Stopwatch(it.id, it.currentMs, it.maxMs, false, it.isFinished))
            }
        }
        stopwatchAdapter.apply {
            submitList(newTimers)
        }
        stopwatches.clear()
        stopwatches.addAll(newTimers)
    }
}
