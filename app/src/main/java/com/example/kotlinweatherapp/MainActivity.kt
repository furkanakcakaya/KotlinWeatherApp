package com.example.kotlinweatherapp

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope()  {
    var city: String = "istanbul"
    var api_key: String = "c3d33bf86ebc9ed36ba75b21848b16af"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CoroutineScope(Dispatchers.Main + Job()).launch {
            weatherTask()
        }
    }

    private suspend fun weatherTask(){
        findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
        findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
        findViewById<TextView>(R.id.errorText).visibility = View.GONE
        withContext(Dispatchers.IO){
            var response: String?
            try{
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$api_key").readText(
                    Charsets.UTF_8
                )
            }catch (e: Exception){
                println(e)
                response = null
            }
            withContext(Dispatchers.Main){
                updateUI(response)
            }
        }
    }

    private fun updateUI(response: String?) {
        if (response.isNullOrBlank()){
            findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
            return
        }
        val jsonObj = JSONObject(response)
        val main = jsonObj.getJSONObject("main")
        val sys = jsonObj.getJSONObject("sys")
        val wind = jsonObj.getJSONObject("wind")
        val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
        val updatedAt:Long = jsonObj.getLong("dt")
        val updatedAtText = "Updated at: "+ SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(updatedAt*1000))
        val temp = main.getString("temp")+"°C"
        val tempMin = "Min Temp: " + main.getString("temp_min")+"°C"
        val tempMax = "Max Temp: " + main.getString("temp_max")+"°C"
        val pressure = main.getString("pressure")
        val humidity = main.getString("humidity")

        val sunrise:Long = sys.getLong("sunrise")
        val sunset:Long = sys.getLong("sunset")
        val windSpeed = wind.getString("speed")
        val weatherDescription = weather.getString("description")

        val address = jsonObj.getString("name")+", "+sys.getString("country")

        /* Populating extracted data into our views */
        findViewById<TextView>(R.id.address).text = address
        findViewById<TextView>(R.id.updated_at).text =  updatedAtText
        findViewById<TextView>(R.id.status).text = weatherDescription.uppercase()
        findViewById<TextView>(R.id.temp).text = temp
        findViewById<TextView>(R.id.temp_min).text = tempMin
        findViewById<TextView>(R.id.temp_max).text = tempMax
        findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(
            Date(sunrise*1000)
        )
        findViewById<TextView>(R.id.sunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset*1000))
        findViewById<TextView>(R.id.wind).text = windSpeed
        findViewById<TextView>(R.id.pressure).text = pressure
        findViewById<TextView>(R.id.humidity).text = humidity
        /* Views populated, Hiding the loader, Showing the main design */
        findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
        findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE



    }

}