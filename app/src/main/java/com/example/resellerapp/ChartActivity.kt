package com.example.resellerapp

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.database.*
import java.util.*


class ChartActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private lateinit var pieChart: PieChart
    private lateinit var noDataText: TextView
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        // Initialize views
        barChart = findViewById(R.id.barChart)
        pieChart = findViewById(R.id.pieChart)
        noDataText = findViewById(R.id.noDataText)
        database = FirebaseDatabase.getInstance().getReference("saved")

        loadBarChartData()
        loadPieChartData()
    }

    // Fungsi untuk Bar Chart
    private fun loadBarChartData() {
        val currentWeek = getWeekOfYear(Date())
        val lastWeek = currentWeek - 1

        var thisWeekCount = 0
        var lastWeekCount = 0

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                thisWeekCount = 0
                lastWeekCount = 0

                for (resellerSnapshot in snapshot.children) {
                    for (data in resellerSnapshot.children) {
                        val timestamp = data.child("timestamp").getValue(Long::class.java) ?: continue
                        val date = Date(timestamp)
                        val week = getWeekOfYear(date)

                        if (week == currentWeek) {
                            thisWeekCount++
                        } else if (week == lastWeek) {
                            lastWeekCount++
                        }
                    }
                }

                if (thisWeekCount == 0 && lastWeekCount == 0) {
                    noDataText.visibility = View.VISIBLE
                    barChart.visibility = View.GONE
                } else {
                    noDataText.visibility = View.GONE
                    barChart.visibility = View.VISIBLE
                    displayBarChart(thisWeekCount, lastWeekCount)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChartActivity, "Failed to load data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun displayBarChart(thisWeekCount: Int, lastWeekCount: Int) {
        val entries = listOf(
            BarEntry(0f, thisWeekCount.toFloat()),
            BarEntry(1f, lastWeekCount.toFloat())
        )

        val dataSet = BarDataSet(entries, "Pengajuan yang diterima")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        dataSet.valueTextSize = 14f
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }

        val barData = BarData(dataSet)
        barChart.data = barData

        barChart.description.text = "Perbandingan Pengajuan: Minggu ini vs Minggu lalu"
        barChart.description.textSize = 14f

        val labels = listOf("Minggu ini", "Minggu lalu")
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.xAxis.textSize = 12f
        barChart.xAxis.granularity = 1f
        barChart.xAxis.setLabelCount(2, true)

        barChart.axisLeft.textSize = 14f
        barChart.axisRight.textSize = 14f

        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisRight.setDrawGridLines(false)
        barChart.xAxis.setDrawGridLines(false)

        barChart.animateY(1000)
        barChart.invalidate()
    }

    // Fungsi untuk Pie Chart
    private fun loadPieChartData() {
        val itemCountMap = mutableMapOf<String, Int>()

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemCountMap.clear()

                for (resellerSnapshot in snapshot.children) {
                    for (data in resellerSnapshot.children) {
                        val item = data.child("item").getValue(String::class.java) ?: continue
                        itemCountMap[item] = itemCountMap.getOrDefault(item, 0) + 1
                    }
                }

                if (itemCountMap.isEmpty()) {
                    pieChart.visibility = View.GONE
                } else {
                    pieChart.visibility = View.VISIBLE
                    displayPieChart(itemCountMap)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChartActivity, "Failed to load data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun displayPieChart(itemCounts: Map<String, Int>) {
        val totalItems = itemCounts.values.sum()
        val entries = itemCounts.map { (key, value) ->
            PieEntry(value.toFloat(), key)
        }

        val pieDataSet = PieDataSet(entries, "Distribusi Item")
        pieDataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        pieDataSet.valueTextSize = 14f
        pieDataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val percentage = (value / totalItems * 100).toInt()
                return "$percentage%"
            }
        }

        val pieData = PieData(pieDataSet)
        pieChart.data = pieData

        pieChart.description.text = "Distribusi Item"
        pieChart.description.textSize = 14f

        pieChart.setDrawEntryLabels(true)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setUsePercentValues(false) // Menampilkan persentase secara manual
        pieChart.setHoleColor(android.R.color.transparent) // Transparan untuk gaya Donut Chart
        pieChart.animateY(1000)

        pieChart.invalidate()
    }

    private fun getWeekOfYear(date: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.WEEK_OF_YEAR)
    }
}
