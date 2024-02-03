package com.example.skycast.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.domain.models.WeatherModel
import com.example.skycast.R
import com.example.skycast.presentation.adapters.DailyAdapter
import com.example.skycast.presentation.adapters.HourlyAdapter
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale

class WeatherFragment: Fragment() {

    private lateinit var hourAdapter: HourlyAdapter
    private lateinit var dayAdapter: DailyAdapter
    private lateinit var rvHourWeather: RecyclerView
    private lateinit var rvDayWeather: RecyclerView
    private var button: Button? = null
    private var editText: EditText? = null
    private var tvCity: TextView? = null
    private var tvTemp: TextView? = null
    private var tvCondition: TextView? = null
    private var ivIcon: ImageView? = null

    private val weatherViewModel: WeatherViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.weather_fragment, container, false)
        rvHourWeather = view.findViewById(R.id.rv_hour)
        rvDayWeather = view.findViewById(R.id.rv_day)
        hourAdapter = HourlyAdapter()
        dayAdapter = DailyAdapter()

        rvHourWeather.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
        rvHourWeather.adapter = hourAdapter

        rvDayWeather.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
        rvDayWeather.adapter = dayAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)

        button?.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val cityName: String = editText?.text.toString()
                    weatherViewModel.updateCityName(cityName)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        dayAdapter.setClickListener { selectedDay ->
            weatherViewModel.updateSelectedDay(selectedDay)
        }

        weatherViewModel.weatherDataLive.observe(viewLifecycleOwner) { weatherModel ->
            showWeatherData(weatherModel)
        }

        weatherViewModel.selectedWeatherDayLive.observe(viewLifecycleOwner) { selectedDay ->
            updateSelectedDay(selectedDay)
        }
    }

    private fun showWeatherData(weatherModel: WeatherModel?) {

        requireActivity().runOnUiThread {
            ivIcon?.let {
                Glide.with(this)
                    .load("https:${weatherModel?.currentWeather?.icon}")
                    .into(it)
            }

            tvCity?.text = context?.getString(R.string.current_weather_city, weatherModel?.currentWeather?.city)
            tvTemp?.text = context?.getString(R.string.current_weather_temp, weatherModel?.currentWeather?.avgTemp)
            tvCondition?.text = context?.getString(
                R.string.current_weather_condition,
                weatherModel?.currentWeather?.condition,
                weatherModel?.currentWeather?.maxTemp,
                weatherModel?.currentWeather?.minTemp,
                LocalDateTime.parse(
                    weatherModel?.currentWeather?.time,
                    DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .append(DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm"))
                        .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        .toFormatter()
                ).format(DateTimeFormatter.ofPattern("EEE, HH:mm", Locale("ru"))).toString()
            )

            hourAdapter.setHourlyData(weatherModel?.dailyWeather?.get(0)?.hourlyWeather!!)
            dayAdapter.setDailyData(weatherModel.dailyWeather)

            val selectedDay = weatherViewModel.selectedWeatherDayLive.value
            if (selectedDay != null) {
                hourAdapter.setHourlyData(selectedDay.hourlyWeather)
            }

        }

    }

    private fun updateSelectedDay(selectedDay: WeatherModel.DailyWeather?) {

        requireActivity().runOnUiThread {
            ivIcon?.let {
                Glide.with(this)
                    .load("https:${selectedDay?.icon}")
                    .into(it)
            }

            //tvCity?.text = context?.getString(R.string.current_weather_city, weatherModel?.currentWeather?.city)
            tvTemp?.text = context?.getString(R.string.current_weather_temp, selectedDay?.avgTemp)
            tvCondition?.text = context?.getString(
                R.string.current_weather_condition,
                selectedDay?.condition,
                selectedDay?.maxTemp,
                selectedDay?.minTemp,
                // Поправленный код для парсинга даты
                LocalDate.parse(
                    selectedDay?.day,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
                ).atStartOfDay().format(
                    DateTimeFormatter.ofPattern("EEE, HH:mm", Locale("ru"))
                )
            )

            hourAdapter.setHourlyData(selectedDay?.hourlyWeather!!)

        }

    }

    private fun init(view: View) {
        button = view.findViewById(R.id.button)
        editText = view.findViewById(R.id.editText)

        tvCity = view.findViewById(R.id.tv_city)
        tvTemp = view.findViewById(R.id.tv_temp)
        tvCondition = view.findViewById(R.id.tv_condition)
        ivIcon = view.findViewById(R.id.iv_icon)
    }
}