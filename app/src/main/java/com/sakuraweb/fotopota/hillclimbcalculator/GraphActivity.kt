package com.sakuraweb.fotopota.hillclimbcalculator

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.android.synthetic.main.activity_graph.*
import kotlin.math.asin
import kotlin.math.cos

// 各パワー成分はグローバル変数で受け渡し
private var gPower: Int = 0
private var rPower: Int = 0
private var aPower: Int = 0
private var tPower: Int = 0

class GraphActivity : AppCompatActivity() {

    // スタートアップ
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        // 体重バー
        // 10～100kgにするため、0～90を+10オフセットして使う
        graphBodyWeightBar.max = 90
        graphBodyWeightBar.progress = (bodyWeight-10).toInt()
        graphBodyWeightText.setText(bodyWeight.toString())

        graphBodyWeightBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                graphBodyWeightText.setText((progress+10).toString())
                drawGraph()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })

        // バイク重量バー
        // 2～20kgにするため、0～18を+2オフセットして使う
        graphBikeWeightBar.max = 20
        graphBikeWeightBar.progress = (bikeWeight-2).toInt()
        graphBikeWeightText.setText(bikeWeight.toString())

        graphBikeWeightBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                graphBikeWeightText.setText((progress+2).toString())
                drawGraph()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })

        // 転がり抵抗バー
        // 0.001～0.005にするため、0～40にして、+10　÷10000して使う
        graphRollingBar.max = 40
        graphRollingBar.progress = (rollingAdjust*10000-10).toInt()
        graphRollingText.setText(rollingAdjust.toString())

        graphRollingBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                graphRollingText.setText(((progress+10).toDouble()/10000).toString())
                drawGraph()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })

        // タイムバー
        // 5～180分にするため、+5オフセットして使う
        graphTimeBar.max = 175
        graphTimeBar.progress = goalTimeHour*60+ goalTimeMin - 5
        graphTimeText.setText((goalTimeHour*60+ goalTimeMin).toString())

        graphTimeBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                graphTimeText.setText((progress+5).toString())
                drawGraph()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })

        // 標高差バー
        graphHeightBar.max = 3000
        graphHeightBar.progress = courseHeight.toInt()
        graphHeightText.setText(courseHeight.toInt().toString())

        graphHeightBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                graphHeightText.setText(progress.toString())
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

    // 一応作ったけど、誰も呼び出してない
    fun setSeekBar() {
        graphBikeWeightBar.progress = graphBikeWeightText.text.toString().toInt()
        graphBodyWeightBar.progress = graphBodyWeightText.text.toString().toInt()
        graphRollingBar.progress    = (graphRollingText.text.toString().toDouble()*10000).toInt()
        graphTimeBar.progress       = graphTimeText.text.toString().toInt()
    }

    // 円グラフを描くための準備
    // 穴をあけたり、凡例を描いたり
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
        chart.centerText = "サンプルテキストです！"
        chart.setCenterTextSize(25f)

    }


    // 元データとseekBarの値からパワーを算出する
    // 転がり、空気、重力の３つのグローバル変数に格納する
    private fun calcPowers() {
        // バーの数字を読む
        bodyWeight = graphBodyWeightText.text.toString().toDouble()
        bikeWeight = graphBikeWeightText.text.toString().toDouble()
        rollingAdjust = graphRollingText.text.toString().toDouble()
        courseHeight = graphHeightText.text.toString().toDouble()
        val goalTime = graphTimeText.text.toString().toLong()*60 // 分で登録してあるため


        val w: Double = bodyWeight + bikeWeight
        var p1: Double
        val p2: Double
        val p3: Double
        val r : Double = courseLength / goalTime * 9.81

        p1 = (blacketAdjust*highAdjust*fujii* Math.pow(bodyWeight, 0.425) * Math.pow(
            bodyHeight,
            0.725) * Math.pow(courseLength / goalTime, 2.0))*r

        p2 = (w* cos(asin(courseHeight/courseLength)) *rollingAdjust) * r
        p3 = (w*courseHeight/courseLength) * r

        if( p1<0.0 ) {p1=0.0}
        aPower = p1.toInt()
        rPower = p2.toInt()
        gPower = p3.toInt()
        tPower = aPower + rPower + gPower
    }


    fun drawGraph() {
        val xVals = mutableListOf<String>()
        val yVals = mutableListOf<Entry>()
        val colors = mutableListOf<Int>()

        calcPowers()

        xVals.add("空気抵抗")
        xVals.add("転がり抵抗")
        xVals.add("重力抵抗")

        // 転がり抵抗が小さすぎるとバグるみたいなので、zeroサプレス
        if (rPower*1000 < 3* tPower) {rPower = 0}
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
        val speed = ((courseLength/graphTimeText.text.toString().toDouble())*60/1000).toInt()
        chart.centerText = "${tPower.toInt().toString()}W\n${speed}km/h"

        // 数値表示？
        dataSet.setDrawValues(true)

        val data = PieData(xVals, dataSet)
//  data.setValueFormatter(PercentFormatter())

        // テキスト設定
        data.setValueTextSize(14f)
        data.setValueTextColor(Color.BLACK)

        chart.data = data
        chart.invalidate()

//       graphHeaderText.setText("${aPower}\n${rPower}\n${gPower}")
    }


//    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
//        clearFocus()
//        return super.dispatchTouchEvent(ev)
//    }
//
//    private fun clearFocus() {
//        // 画面更新があるはずなので、ここで勾配データも再表示しておく
//        setSeekBar()
//        drawGraph()
//        // キーボードを隠す
//        inputMethodManager.hideSoftInputFromWindow(graphBase.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)
//        // 背景にフォーカスを移す
//        graphBase.requestFocus()
//    }


}



