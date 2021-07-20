package by.a_makarevich.pomodoro

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import by.a_makarevich.pomodoro.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), StopwatchListener {

    private lateinit var binding: ActivityMainBinding
    private val LOG = "MyLog"

    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            }        }
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