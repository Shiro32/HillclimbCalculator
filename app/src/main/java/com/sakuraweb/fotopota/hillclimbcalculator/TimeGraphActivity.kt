package com.sakuraweb.fotopota.hillclimbcalculator

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.android.synthetic.main.activity_graph.*
import kotlinx.android.synthetic.main.activity_time_graph.*
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sqrt
import kotlinx.android.synthetic.main.activity_time_graph.chart as chart1

// 各パワー成分はグローバル変数で受け渡し
private var gPower: Int = 0
private var rPower: Int = 0
private var aPower: Int = 0
private var gTime: Double = 0.0

class TimeGraphActivity : AppCompatActivity() {

    // 毎度おなじみスタートアップ
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_graph)

        // 体重バー
        // 10～100kgにするため、0～90を+10オフセットして使う
        timeGraphBodyWeightBar.max = 90
        timeGraphBodyWeightBar.progress = (bodyWeight-10).toInt()
        timeGraphBodyWeightText.setText(bodyWeight.toString())

        timeGraphBodyWeightBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                timeGraphBodyWeightText.setText((progress+10).toString())
                drawGraph()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })

        // バイク重量バー
        // 2～20kgにするため、0～18を+2オフセットして使う
        timeGraphBikeWeightBar.max = 20
        timeGraphBikeWeightBar.progress = (bikeWeight-2).toInt()
        timeGraphBikeWeightText.setText(bikeWeight.toString())

        timeGraphBikeWeightBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                timeGraphBikeWeightText.setText((progress+2).toString())
                drawGraph()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })

        // 転がり抵抗バー
        // 0.001～0.005にするため、0～40にして、+10　÷10000して使う
        timeGraphRollingBar.max = 40
        timeGraphRollingBar.progress = (rollingAdjust*10000-10).toInt()
        timeGraphRollingText.setText(rollingAdjust.toString())

        timeGraphRollingBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                timeGraphRollingText.setText(((progress+10).toDouble()/10000).toString())
                drawGraph()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })

        // パワーバー
        // 100～400Wにするため、0～300を+100オフセットして使う
        timeGraphPowerBar.max = 300
        timeGraphPowerBar.progress = (avePower-100).toInt()
        timeGraphPowerText.setText(avePower.toInt().toString())

        timeGraphPowerBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                timeGraphPowerText.setText((progress+100).toString())
                drawGraph()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })





        // 各種バーの初期設定完了
        // グラフの初期化＆描画
        initGraph()
        drawGraph()

    }


    // 円グラフを描くための準備
    // 穴をあけたり、凡例を描いたり。グラフ自体の描画はdrawGraphで
    private fun initGraph() {

        // 真ん中の穴
        chart.isDrawHoleEnabled = true
        chart.holeRadius = 50f
        chart.transparentCircleRadius = 55f;

        // 開始位置
        chart.rotationAngle = 270f

        // 回転可能？
        chart.isRotationEnabled = false

        // レジェンド
        chart.legend.isEnabled = true
        chart.legend.textSize = 12F
        chart.setDescription("")

        // 真ん中の文字
        chart.setDrawCenterText(true)
        chart.centerText = "２００Ｗ"
        chart.setCenterTextSize(25f)

    }


    // 円グラフを描画する
    // ついでにタイムも記載する
    fun drawGraph() {
        val xVals = mutableListOf<String>()
        val yVals = mutableListOf<Entry>()
        val colors = mutableListOf<Int>()

        // 数字をテキストボックスから読み込む
        loadFromEdit()

        // ゴールタイムとパワー内訳を再計算
        calcTime()
        calcPowers()

        val h = (gTime/3600).toInt()
        val m = ((gTime-h*3600)/60).toInt()
        val s = (gTime-(h*3600+m*60)).toInt()

        timeGraphTimeText.setText("${h}時間${m}分${s}秒")

        xVals.add("空気抵抗")
        xVals.add("転がり抵抗")
        xVals.add("重力抵抗")

        yVals.add(Entry(aPower.toFloat(),0))
        yVals.add(Entry(rPower.toFloat(),1))
        yVals.add(Entry(gPower.toFloat(),2))

        val dataSet: PieDataSet = PieDataSet(yVals, "【単位：W】")
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 1f

        // 色の設定
        colors.add(ColorTemplate.COLORFUL_COLORS[0])
        colors.add(ColorTemplate.COLORFUL_COLORS[1])
        colors.add(ColorTemplate.COLORFUL_COLORS[2])
        dataSet.setColors(colors)

        // 真ん中の文字
        val speed = (courseLength/gTime*3600/1000).toInt()
        chart.centerText = "${timeGraphPowerText.text}W\n${speed}km/h"

        // 数値表示？
        dataSet.setDrawValues(true)

        val data = PieData(xVals, dataSet)
//  data.setValueFormatter(PercentFormatter())

        // テキスト設定
        data.setValueTextSize(14f)
        data.setValueTextColor(Color.BLACK)

        chart.data = data
        chart.invalidate()

    }


    private fun loadFromEdit() {
        // バーの数字を読む
        bodyWeight = timeGraphBodyWeightText.text.toString().toDouble()
        bikeWeight = timeGraphBikeWeightText.text.toString().toDouble()
        rollingAdjust = timeGraphRollingText.text.toString().toDouble()
        avePower = timeGraphPowerText.text.toString().toDouble()
    }

    private fun calcTime() {
        val w: Double = bodyWeight + bikeWeight
        val a: Double
        val c: Double
        val c27: Double
        val b3: Double
        val t: Double

        a = -9.81 * courseLength / avePower.toDouble() * (w * cos(asin(courseHeight / courseLength)) * rollingAdjust + w * courseHeight / courseLength)
        c = -9.81 * Math.pow(
            courseLength,
            3.0
        ) / avePower.toDouble() * blacketAdjust * highAdjust * fujii * Math.pow(
            bodyWeight, 0.425
        ) * Math.pow(bodyHeight, 0.725)

        c27 = (27 * c + 2 * Math.pow(a, 3.0)) / 54
        b3 = -Math.pow(a, 2.0) / 9
        t = Math.pow(
            -c27 + sqrt(c27 * c27 + b3 * b3 * b3),
            1.0 / 3.0
        ) + Math.pow(-c27 - sqrt(c27 * c27 + b3 * b3 * b3), 1.0 / 3.0) - a / 3

        gTime = t
    }

    // powerは算出済みだけど、その内訳を作り出す
    // 元データとseekBarの値からパワーを算出する
    // 転がり、空気、重力の３つのグローバル変数に格納する
    private fun calcPowers() {
        val w: Double = bodyWeight + bikeWeight
        var p1: Double
        val p2: Double
        val p3: Double
        val r : Double = courseLength / gTime * 9.81

        p1 = (blacketAdjust*highAdjust*fujii* Math.pow(bodyWeight, 0.425) * Math.pow(
            bodyHeight,
            0.725) * Math.pow(courseLength / gTime, 2.0))*r

        p2 = (w* cos(asin(courseHeight/courseLength)) *rollingAdjust) * r
        p3 = (w*courseHeight/courseLength) * r

        if( p1<0.0 ) {p1=0.0}
        aPower = p1.toInt()
        rPower = p2.toInt()
        gPower = p3.toInt()
    }
}


