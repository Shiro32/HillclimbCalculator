package com.sakuraweb.fotopota.hillclimbcalculator

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.black_toast_layout.*
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sqrt

// メイン画面クラス
// 実際にはRealmデータベースの初期化処理などがあるため、これが最初の実行コードではない
// 最初に実行されるのはStartApplicationクラス
class MainActivity : AppCompatActivity() {

    // 計算用に各種要素はローカル変数で持つ
    // TextEdit入力途中の人に対応すべく、結局は、各計算時にTextEdit→ローカル変数への読み込みをやる
    private var bodyWeight: Double = 0.0
    private var bodyHeight: Double = 0.0
    private var bikeWeight: Double = 0.0
    private var avePower: Double = 0.0
    private var goalTimeHour: Int = 0
    private var goalTimeMin: Int = 0
    private var goalTimeSec: Int = 0
    private var blacketAdjust: Double = 0.0
    private var highAdjust: Double = 0.0
    private var rollingAdjust: Double = 0.0
    private var courseHeight: Double = 635.0
    private var courseLength: Double = 11800.0
    private var courseName: String = " "

    // 以下の２つは計算上の定数
    private val fujii: Double = 0.00007638
    private val weightRatio: Double = 0.839827861

    // 入力時のキーボード制御（隠したりするやつ）のため
    private lateinit var defaultBack: Drawable
    private lateinit var inputMethodManager: InputMethodManager


    // 起動処理 （実質的にMainActivityのコンストラクタ）
    // これ以上早く起動したいのであれば、Applicationクラスでインスタンスを作って、manifest.xmlで指定
    // 今回は、StartApplicationクラスの方を先に実行する
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // メイン画面をinfrateする
        setContentView(R.layout.activity_main)

        // プリファレンスからデータを読み込む
        loadDataFromPref()

        // 背景点滅用
        defaultBack = bodyWeightEdit.background

        // 画面フォーカス用に
        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager        // 入力制御用に入れておく

        // 計算用ボタンのリスナを設定する（このクラス内なので簡単）
        recalcPowerBtn.setOnClickListener() {recalcPower()}
        recalcTimeBtn.setOnClickListener() {recalcGoalTime()}
        recalcBodyWeightBtn.setOnClickListener() {recalcBodyWeight()}
        recalcBikeWeightBtn.setOnClickListener() {recalcBikeWeight()}

        // About画面
        imageTitleBtn.setOnClickListener() {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        // コース選択ボタンのリスナを設定
        // CourseListActivityへ遷移する。特段Intentなしで。
        courseSelectBtn.setOnClickListener() {
            val intent = Intent(this, CourseListActivity::class.java)

            // 結果を戻すことを前提に別Activityを呼び出す。処理は、onActivityResultメソッドで！
            startActivityForResult(intent, 1)
        }

        // copyrightメッセージにURLを埋め込む
        copyRightText.setText(Html.fromHtml("v1.0 Copyright ©2020 Shiro, <a href=\"http://fotopota.sakuraweb.com\">フォトポタ日記2.0</a>"))
        copyRightText.movementMethod = LinkMovementMethod.getInstance()

        // privacy policyにURLを埋め込む
        ppText.setText(Html.fromHtml("<a href=\"http://fotopota.sakuraweb.com/privacy-hhc.html\">プライバシーポリシー</a>"))
        ppText.movementMethod = LinkMovementMethod.getInstance()

//      Toast.makeText(applicationContext, "MainのOnCreate", Toast.LENGTH_LONG).show()
    }
    // onCreate

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 選択画面からの戻り時はIntentでコースデータを伝えてくるので反映する
        // ★コース名なども引っ張ってこよう
        if( resultCode == RESULT_OK) {
            val l = data?.getStringExtra("length")
            val h = data?.getStringExtra("height")
            val n = data?.getStringExtra("name")

            if( l!=null && h!=null && n!=null ) {
                courseLengthEdit.setText(l.toString())
                courseHeightEdit.setText(h.toString())
                courseNameText.setText(n.toString())
                setGradeText()
                BlackToast(applicationContext, "$n をセットしました")
            }
        }
    }

    private fun setGradeText() {
        val l:Double = courseLengthEdit.text.toString().toDouble()
        val h:Double = courseHeightEdit.text.toString().toDouble()

        courseGrade.setText(
            ((h/l*1000).toInt().toDouble()/10).toString()
        )
    }

    // よその画面から戻ってきたとき（初回起動、コース選択画面からの戻り）
    override fun onResume() {
        super.onResume()
    }


    // アプリの実質的な終了処理
    // プリファレンスにデータを書き込む
    override fun onPause() {
        super.onPause()
        saveDataToPref()
    }


    // 入力箇所（EditText）以外をタップしたときに、フォーカスをオフにする
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        clearFocus()

        return super.dispatchTouchEvent(ev)

    }



     private fun clearFocus() {
        setGradeText()

         // キーボードを隠す
        inputMethodManager.hideSoftInputFromWindow(contentLayoutBase.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)
        // 背景にフォーカスを移す
        contentLayoutBase.requestFocus()
    }

    // ★★★　ここから、タイム計算の具体的なアプリケーション　★★★
    // EditText達から数字を読み込む
    // 空欄でも呼ばれることがあるので、その対策が必要（面倒くせ～）
    private fun loadDataFromEdit() {
        // 驚いたことに、editTextのtextプロパティは、Editable型
        // なので、一度、toStringしなくてはならぬ
        // さらに、空欄だった際には0.0を入れるように全部やる。超面倒くせえ
        if(!bodyWeightEdit.text.toString().equals("")) bodyWeight = bodyWeightEdit.text.toString().toDouble() else bodyWeight = 0.0
        if(!bodyHeightEdit.text.toString().equals("")) bodyHeight = bodyHeightEdit.text.toString().toDouble() else bodyHeight = 0.0
        if(!bikeWeightEdit.text.toString().equals("")) bikeWeight = bikeWeightEdit.text.toString().toDouble() else bikeWeight = 0.0
        if(!avePowerEdit.text.toString().equals("")) avePower = avePowerEdit.text.toString().toDouble() else avePower = 0.0
        if(!goalTimeHourEdit.text.toString().equals("")) goalTimeHour = goalTimeHourEdit.text.toString().toInt() else goalTimeHour = 0
        if(!goalTimeMinEdit.text.toString().equals("")) goalTimeMin = goalTimeMinEdit.text.toString().toInt() else goalTimeMin = 0
        if(!goalTimeSecEdit.text.toString().equals("")) goalTimeSec = goalTimeSecEdit.text.toString().toInt() else goalTimeSec = 0
        if(!blacketAdjustEdit.text.toString().equals("")) blacketAdjust = blacketAdjustEdit.text.toString().toDouble() else blacketAdjust = 0.0
        if(!highAdjustEdit.text.toString().equals("")) highAdjust = highAdjustEdit.text.toString().toDouble() else highAdjust = 0.0
        if(!rollingAdjustEdit.text.toString().equals("")) rollingAdjust = rollingAdjustEdit.text.toString().toDouble() else rollingAdjust = 0.0
        if(!courseLengthEdit.text.toString().equals("")) courseLength = courseLengthEdit.text.toString().toDouble() else courseLength = 0.0
        if(!courseHeightEdit.text.toString().equals("")) courseHeight = courseHeightEdit.text.toString().toDouble() else courseHeight = 0.0
    }

    // 共用プリファレンスから諸設定データを読み込む
    // 読み込めない場合（＝初回だけだと思うけど）のために初期設定値も入れておく
    private fun loadDataFromPref() {
        // プリファレンスからローカル変数に入れる
        PreferenceManager.getDefaultSharedPreferences(this).apply {
            bodyWeight      = getString("bodyWeight", "51")!!.toDouble()
            bodyHeight      = getString("bodyHeight","175")!!.toDouble()
            bikeWeight      = getString("bikeWeight", "8.5")!!.toDouble()
            avePower        = getString("avePower", "215")!!.toDouble()
            goalTimeHour    = getString("goalTimeHour", "0")!!.toInt()
            goalTimeMin     = getString("goalTimeMin", "40")!!.toInt()
            goalTimeSec     = getString("goalTimeSec", "0")!!.toInt()
            blacketAdjust   = getString("blacketAdjust", "1.25")!!.toDouble()
            highAdjust      = getString("highAdjust", "0.752")!!.toDouble()
            rollingAdjust   = getString("rollingAdjust", "0.00421")!!.toDouble()
            courseLength    = getString("courseLength", "11800")!!.toDouble()
            courseHeight    = getString("courseHeight", "635")!!.toDouble()
            courseName      = getString("courseName", " ").toString()

            // 各TextEditにも設定してやる
            bodyWeightEdit.setText(bodyWeight.toString())
            bodyHeightEdit.setText(bodyHeight.toString())
            bikeWeightEdit.setText(bikeWeight.toString())
            avePowerEdit.setText(avePower.toString())
            goalTimeHourEdit.setText(goalTimeHour.toString())
            goalTimeMinEdit.setText(goalTimeMin.toString())
            goalTimeSecEdit.setText(goalTimeSec.toString())
            blacketAdjustEdit.setText(blacketAdjust.toString())
            highAdjustEdit.setText(highAdjust.toString())
            rollingAdjustEdit.setText(rollingAdjust.toString())
            courseLengthEdit.setText(courseLength.toString())
            courseHeightEdit.setText(courseHeight.toString())
            courseNameText.setText(courseName)

            setGradeText()
            // Toast.makeText(applicationContext, "load from PREF:{$courseName}", Toast.LENGTH_LONG).show()
        }
    }


    // 共用プリファレンスに諸設定データを書き出す
    // なんだかんだ言って、EditTextがその時点での最新値なので、EditTextから読んで書き出す
    private fun saveDataToPref() {

        // textEditから数字を読みだす。もしかすると、これを経由しないで、直接、TEditTextから書いていいかも
        loadDataFromEdit()

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = pref.edit()

        editor.putString( "bodyWeight", bodyWeight.toString())
            .putString( "bodyHeight", bodyHeight.toString())
            .putString( "bikeWeight", bikeWeight.toString())
            .putString( "avePower", avePower.toString())
            .putString( "goalTimeHour", goalTimeHour.toString())
            .putString( "goalTimeMin", goalTimeMin.toString())
            .putString( "goalTimeSec", goalTimeSec.toString())
            .putString( "blacketAdjust", blacketAdjust.toString())
            .putString( "highAdjust", highAdjust.toString())
            .putString( "rollingAdjust", rollingAdjust.toString())
            .putString( "courseLength", courseLength.toString())
            .putString( "courseHeight", courseHeight.toString())
            .putString( "courseName", courseNameText.text.toString())
            .apply()

        // Toast.makeText(applicationContext, "WRITE:{$courseName}", Toast.LENGTH_LONG).show()
    }


    // 指定したEditTextをハイライト表示する。それ以外はオフにする
    // １秒後にはオフに戻す
    private fun highlightEdit(vararg sels: EditText) {
        // フォーカスを消す
        clearFocus()

        // 最初のころは、ここで全要素をクリアしたりしていたが、単一要素だけにした
        // ハイライトする

        for(s in sels) {
            s.setBackgroundResource(R.color.colorChange)
        }

        // １秒後にハイライト消す
        Handler().postDelayed( Runnable {
            for(s in sels) {
                s.setBackground(defaultBack)
                s.background.clearColorFilter()
            }
        }, 1000)
    }


    // ようやくアプリケーション本体
    // パワー計算・Edit更新
    private fun recalcPower() {
        val w: Double
        val p1: Double
        val p2: Double
        val p3: Double
        var p : Double
        val goalTime: Int

        // まずEditTextからローカル変数に
        loadDataFromEdit()

        goalTime = goalTimeHour*3600 + goalTimeMin*60 + goalTimeSec
        w = bodyWeight + bikeWeight

        p1 = blacketAdjust*highAdjust*fujii* Math.pow(bodyWeight, 0.425) * Math.pow(
            bodyHeight,
            0.725
        ) * Math.pow(courseLength / goalTime, 2.0)
        p2 = w* cos(asin(courseHeight/courseLength)) *rollingAdjust
        p3 = w*courseHeight/courseLength

        p = (p1+p2+p3) * courseLength/goalTime * 9.81

        p = round(p*10) /10

        // 書き込み ＆ ハイライト
        avePowerEdit.setText(p.toString())
        highlightEdit (avePowerEdit)
    }

    // 体重計算・Edit更新
    private fun recalcBodyWeight() {
        var w: Double
        val goalTime: Int

        // まずEditTextからローカル変数に
        loadDataFromEdit()
        goalTime = goalTimeHour*3600 + goalTimeMin*60 + goalTimeSec
        w = avePower * goalTime / (courseHeight*9.81) * weightRatio - bikeWeight

        w = round(w*10) /10

        // 書き込み ＆ ハイライト
        bodyWeightEdit.setText(w.toString())
        highlightEdit (bodyWeightEdit)

        // 体重がマイナスになってしまった場合の処置
        if (w <= 0) {
            BlackToast(applicationContext, getString(R.string.bodyWeightCaution_msg))
        }
    }

    // Bike重量計算・Edit更新
    private fun recalcBikeWeight() {
        var w: Double
        val goalTime: Int

        // まずEditTextからローカル変数に
        loadDataFromEdit()
        goalTime = goalTimeHour*3600 + goalTimeMin*60 + goalTimeSec
        w = avePower * goalTime / (courseHeight*9.81) * weightRatio - bodyWeight

        w = round( w*10 ) /10

        // 書き込み ＆ ハイライト
        bikeWeightEdit.setText(w.toString())
        highlightEdit (bikeWeightEdit)

        // bike重量ががマイナスになってしまった場合の処置
        if (w <= 0) {
            BlackToast(applicationContext,getString(R.string.bikeWeightCaution_msg))
        }
    }


    // ゴールタイム計算・Edit更新
    private fun recalcGoalTime() {
        val w: Double

        val a: Double
        val c: Double
        val c27: Double
        val b3: Double
        val t: Double

        val h: Int
        val m: Int
        val s: Int

        // まずEditTextからローカル変数に
        loadDataFromEdit()

        w = bodyWeight + bikeWeight
        a = -9.81 * courseLength / avePower * (w * cos(asin(courseHeight / courseLength)) * rollingAdjust + w * courseHeight / courseLength)
        c = -9.81 * Math.pow(
            courseLength,
            3.0
        ) / avePower * blacketAdjust * highAdjust * fujii * Math.pow(
            bodyWeight,
            0.425
        ) * Math.pow(bodyHeight, 0.725)

        c27 = (27 * c + 2 * Math.pow(a, 3.0)) / 54
        b3 = -Math.pow(a, 2.0) / 9
        t = Math.pow(
            -c27 + sqrt(c27 * c27 + b3 * b3 * b3),
            1.0 / 3.0
        ) + Math.pow(-c27 - sqrt(c27 * c27 + b3 * b3 * b3), 1.0 / 3.0) - a / 3

        // timeを時分秒に分けて整数化
        h = (t / 3600).toInt()
        m = ((t - h * 3600) / 60).toInt()
        s = (t - (h * 3600 + m * 60)).toInt()

        // 書き込み ＆ ハイライト
        goalTimeHourEdit.setText(h.toString())
        goalTimeMinEdit.setText(m.toString())
        goalTimeSecEdit.setText(s.toString())

        highlightEdit(goalTimeHourEdit, goalTimeMinEdit,goalTimeSecEdit)
    }

}
