package pro.horovodovodo4ka.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import pro.horovodovodo4ka.astaroth.ConsoleLogger
import pro.horovodovodo4ka.astaroth.Log
import pro.horovodovodo4ka.astaroth.LogType
import pro.horovodovodo4ka.astaroth.e
import pro.horovodovodo4ka.astaroth.plusAssign

object TestTag: LogType

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log += ConsoleLogger()
        val f = { Log.e(TestTag) { 1 } }
        f()
    }
}
