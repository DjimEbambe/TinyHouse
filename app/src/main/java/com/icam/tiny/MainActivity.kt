package com.icam.tiny

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.lzyzsd.circleprogress.ArcProgress
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI


class MainActivity : AppCompatActivity() {
    private var ip = "192.168.202.222"
    private val uri = URI("ws://${ip}/ws")
    private  var valuesGet: List<String> =listOf (" ")
    private val client_ = MyWebSocketClient(this, uri)
    //fan
    var fan_state_get: Int? = null
    var fan_speed_get: Int? = null
    var temperature_get: String? = null
    var fan_mode_get: Boolean? = null
    //led
    private val client = HttpClient(OkHttp) {
        install(WebSockets) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var speedSet =""
        val fan_state = findViewById<TextView>(R.id.fan_state)
        val fan_mode = findViewById<ToggleButton>(R.id.fan_mode)
        val fan_speed = findViewById<SeekBar>(R.id.fan_speed)
        val fan_loading: ProgressBar = findViewById(R.id.fan_loading)
        val fan_toggle = findViewById<Button>(R.id.fan_toggle)
        val fan_image_inim = findViewById<ImageView>(R.id.image_fan)
        val fan_speed_value = findViewById<TextView>(R.id.fan_speed_value)
        val temperature_value = findViewById<TextView>(R.id.temperature)
        //led
        val led1 = findViewById<ImageView>(R.id.led1)
        val led2 = findViewById<ImageView>(R.id.led2)
        val led3 = findViewById<ImageView>(R.id.led3)
        val led_mode = findViewById<ToggleButton>(R.id.led_mode)
        val led_level = findViewById<SeekBar>(R.id.led_level)
        val led_level_value = findViewById<TextView>(R.id.led_level_value)
        val led_state1 = findViewById<ToggleButton>(R.id.ledState1)
        val led_state2 = findViewById<ToggleButton>(R.id.ledState2)
        val led_state3 = findViewById<ToggleButton>(R.id.ledState3)

        //
        val progress1 = findViewById<ArcProgress>(R.id.progress1)
        val progress2 = findViewById<ArcProgress>(R.id.progress2)
        val progress3 = findViewById<ArcProgress>(R.id.progress3)
        progress1.progress=90

        val btnConnect = findViewById<Button>(R.id.btnConnect)
        btnConnect.visibility = View.GONE

        //Animation set up
        val anim = RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        anim.interpolator = LinearInterpolator()
        anim.repeatCount = Animation.INFINITE
        anim.duration = 3700
        //client_2
        CoroutineScope(Dispatchers.Main).launch {
            try {
                client_.connect()
                btnConnect.text="connected"
            } catch (e: Exception) {
                btnConnect.text="connecting..."
                btnConnect.isEnabled=true
                Toast.makeText(this@MainActivity, "$e", Toast.LENGTH_SHORT).show()
            }
        }
        //client_1
        CoroutineScope(Dispatchers.Main).launch {
            try {
                client.webSocket(method = HttpMethod.Get, host = ip, port = 80, path = "/ws") {
                    while(true) {
                        val message = incoming.receive() as? Frame.Text ?: continue
                        var value:String  = message.readText()
                        valuesGet = value.split("v")
                        //fan
                        fan_state_get = (valuesGet.elementAt(0)).toInt()
                        CoroutineScope(Dispatchers.Main).launch {
                            if (fan_state_get ==1){
                                fan_image_inim.startAnimation(anim)
                                fan_state.text = "On"
                            } else if(fan_state_get==0){
                                fan_state.text = "Off"
                                fan_image_inim.animation = null
                            }
                        }
                        fan_speed_get = (valuesGet.elementAt(1)).toInt()
                        fan_speed_get = convert(fan_speed_get!!, 52..255, 0..100)
                        fan_speed_value.text= fan_speed_get.toString() +" %"
                        val fanAuto = (valuesGet.elementAt(2)).toInt()==1
                        fan_mode.isChecked = fanAuto
                        if (fanAuto){
                            fan_toggle.isEnabled=false
                            fan_speed.isEnabled=false
                        }else{
                            fan_toggle.isEnabled=true
                            fan_speed.isEnabled=true
                        }
                        //Temperature
                        temperature_get = (valuesGet.elementAt(3)).toString()
                        temperature_value.text= temperature_get +"°C"
                        //Led
                        val ledAuto = (valuesGet.elementAt(4)).toInt()==1
                        val led1_state_get = (valuesGet.elementAt(5)).toInt()==1
                        val led2_state_get = (valuesGet.elementAt(6)).toInt()==1
                        val led3_state_get = (valuesGet.elementAt(7)).toInt()==1
                        val ledLevel = (valuesGet.elementAt(8)).toInt()
                        led_state1.isChecked = led1_state_get
                        led_state2.isChecked = led2_state_get
                        led_state3.isChecked = led3_state_get
                        led_level_value.text = convert(ledLevel, 0..255, 0..100).toString()+" %"
                        if (led1_state_get){
                            led1.setImageResource(R.drawable.ledon)
                        }else{
                            led1.setImageResource(R.drawable.ledoff)
                        }
                        if (led2_state_get){
                            led2.setImageResource(R.drawable.ledon)
                        }else{
                            led2.setImageResource(R.drawable.ledoff)
                        }
                        if (led3_state_get){
                            led3.setImageResource(R.drawable.ledon)
                        }else{
                            led3.setImageResource(R.drawable.ledoff)
                        }
                        led_mode.isChecked = ledAuto

                        if (ledAuto){
                            led_state1.isEnabled=false
                            led_state2.isEnabled=false
                            led_state3.isEnabled=false
                            led_level.isEnabled=false
                        }else{
                            led_state1.isEnabled=true
                            led_state2.isEnabled=true
                            led_state3.isEnabled=true
                            led_level.isEnabled=true
                        }
                    }
                }
            } catch (e: Exception) {
                btnConnect.text="connecting..."
                btnConnect.isEnabled=true
                Toast.makeText(this@MainActivity, "$e", Toast.LENGTH_SHORT).show()
            }
        }


        btnConnect.isEnabled=false
        // Reload App if there is not connection
        btnConnect.setOnClickListener {
            val mIntent = intent
            finish()
            startActivity(mIntent)
            it.isEnabled = false
        }

        //FAN
        fan_toggle.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    println(client_)
                    client_.send("fan_toggle")
                    client_.send(" ")
                } catch (e: Exception) {
                    btnConnect.text="connecting..."
                    btnConnect.isEnabled=true
                    Toast.makeText(this@MainActivity, "$e", Toast.LENGTH_SHORT).show()
                }
            }
        }
        fan_speed?.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seek: SeekBar) {}
            override fun onStopTrackingTouch(seek: SeekBar) {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        fan_loading.visibility= View.VISIBLE
                        fan_speed.isEnabled=false
                        client_.send("speed_${seek.progress}")
                        client_.send(" ")
                        fan_speed.isEnabled=true
                        fan_loading.visibility= View.GONE
                    } catch (e: Exception) {
                        btnConnect.text="connecting..."
                        btnConnect.isEnabled=true
                        Toast.makeText(this@MainActivity, "$e", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        fan_mode.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    client_.send("fanAuto")
                    client_.send(" ")
                } catch (e: Exception) {
                    btnConnect.text="connecting..."
                    btnConnect.isEnabled=true
                    Toast.makeText(this@MainActivity, "$e", Toast.LENGTH_SHORT).show()
                }
            }
        }
        //LED
        led_mode.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    client_.send("ledsAuto")
                    client_.send(" ")
                } catch (e: Exception) {
                    btnConnect.text="connecting..."
                    btnConnect.isEnabled=true
                    Toast.makeText(this@MainActivity, "$e", Toast.LENGTH_SHORT).show()
                }
            }
        }
        led_state1.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    client_.send("ledA")
                    client_.send(" ")
                } catch (e: Exception) {
                    btnConnect.text="connecting..."
                    btnConnect.isEnabled=true
                    Toast.makeText(this@MainActivity, "$e", Toast.LENGTH_SHORT).show()
                }
            }
        }
        led_state2.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    client_.send("ledB")
                    client_.send(" ")
                } catch (e: Exception) {
                    btnConnect.text="connecting..."
                    btnConnect.isEnabled=true
                    Toast.makeText(this@MainActivity, "$e", Toast.LENGTH_SHORT).show()
                }
            }
        }
        led_state3.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    client_.send("ledC")
                    client_.send(" ")
                } catch (e: Exception) {
                    btnConnect.text="connecting..."
                    btnConnect.isEnabled=true
                    Toast.makeText(this@MainActivity, "$e", Toast.LENGTH_SHORT).show()
                }
            }
        }
        led_level?.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seek: SeekBar) {}
            override fun onStopTrackingTouch(seek: SeekBar) {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        fan_loading.visibility= View.VISIBLE
                        fan_speed.isEnabled=false
                        client_.send("ledLevel_${seek.progress}")
                        client_.send(" ")
                        fan_speed.isEnabled=true
                        fan_loading.visibility= View.GONE
                    } catch (e: Exception) {
                        btnConnect.text="connecting..."
                        btnConnect.isEnabled=true
                        Toast.makeText(this@MainActivity, "$e", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
    private fun convert(number: Int, original: IntRange, target: IntRange): Int {
        val ratio = (number - original.start).toFloat() / (original.endInclusive - original.start)
        return (ratio * (target.endInclusive - target.start)).toInt()
    }
}