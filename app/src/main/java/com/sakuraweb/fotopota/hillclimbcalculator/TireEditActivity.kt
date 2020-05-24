package com.sakuraweb.fotopota.hillclimbcalculator

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_course_edit.*
import kotlinx.android.synthetic.main.activity_tire_edit.*

class TireEditActivity : AppCompatActivity() {
    private val tag = "TireData"
    private lateinit var realm: Realm
    private lateinit var inputMethodManager: InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tire_edit)

        inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager


        realm = Realm.getDefaultInstance()

        // 画面遷移（Intent）によって、新規追加と更新を見分ける
        // idが0以上なら既存（なので削除もあり）
        // idが0なら新規作成
        val bpId = intent.getLongExtra("id", 0L)
        if (bpId > 0L) {
            val bp = realm.where<TireData>().equalTo("id", bpId).findFirst()
            tireNameEdit.setText(bp?.name.toString())
            tireMakerEdit.setText(bp?.maker.toString())
            tireCRREdit.setText(bp?.crr.toString())
            tirePressEdit.setText(bp?.press.toString())

            when( bp?.type.toString() ) {
                getString(R.string.tireTU) -> { tireTURadioBtn.isChecked = true }
                getString(R.string.tireCL) -> { tireCLRadioBtn.isChecked = true }
                getString(R.string.tireTL) -> { tireTLRadioBtn.isChecked = true }
                else -> { }
             }
            tireDeleteBtn.visibility = View.VISIBLE
        } else {
            tireDeleteBtn.visibility = View.INVISIBLE
        }

        // 画面フォーカス用に
        inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager


        // saveボタンのリスナ （強引にここに全部書く）
        tireSaveBtn.setOnClickListener {
            var crr: Double = 0.0
            var press: Double = 0.0
            var name: String = ""
            var maker: String = ""
            var type: String = ""

            // 各テキストボックスから変数に読込み（text化して、Long化する）
            if (!tireMakerEdit.text.isNullOrEmpty()) { maker = tireMakerEdit.text.toString() }
            if (!tireNameEdit.text.isNullOrEmpty()) {  name = tireNameEdit.text.toString() }
            if (!tireCRREdit.text.isNullOrEmpty()) { crr = tireCRREdit.text.toString().toDouble() }
            if (!tirePressEdit.text.isNullOrEmpty()) { press = tirePressEdit.text.toString().toDouble() }

            val id = tireRadioGroup.checkedRadioButtonId
            if( id>0 ) {
                val sel = findViewById<RadioButton>(id).text
                when(sel) {
                    getString(R.string.tireTU) -> {
                        type = "TU"
                    }
                    getString(R.string.tireCL) -> {
                        type = "CL"
                    }
                    getString(R.string.tireTL) -> {
                        type = "TL"
                    }
                    else -> {
                        type = "?"
                    }
                }
            }


            when (bpId) {
                0L -> {
                    // Realmに登録する
                    // トランザクションを1行のメソッド（executeTransaction）で実行する
                    realm.executeTransaction {

                        // whereクエリでidの最大値（今まで書かれた最後の部分）をゲットする
                        // whereクエリの戻り値はRealmQueryオブジェクトであり、続けざまにmaxメソッドができる
                        // で、出来上がったmaxIDは、たぶん、RealmQueryオブジェクト
                        val maxID = realm.where<TireData>().max("id")

                        // エルビス演算子（?:）でチェック。 要するに+1で次の要素にする（相変わらず、RealmQuery）
                        val nextID = (maxID?.toLong() ?: 0L) + 1L

                        // ようやく、１レコードを生成する
                        // CourseDataは自分で設計したクラス。そのインスタンスとして作る
                        // で、そのフィールドに直接書き込む（なんかすごいが）
                        // createObjectメソッドは型パラメータと、idが必要
                        // 型パラメータにより任意のレコード型を作れる。すごいねぇ・・・。
                        val bp: TireData = realm.createObject<TireData>(nextID)
                        bp.name = name
                        bp.maker = maker
                        bp.press = press
                        bp.crr = crr
                        bp.type = type

                    } // トランザクションを1行で書いているので、これで書き込み終了（簡単すぎ）
                }
                else -> { // 修正作業
                    realm.executeTransaction {
                        val bp = realm.where<TireData>().equalTo("id", bpId).findFirst()
                        bp?.name = name
                        bp?.maker = maker
                        bp?.type = type
                        bp?.press = press
                        bp?.crr = crr
                    }
                }
            }

            blackToast(applicationContext, "保存しましたよ")

            // アクティビティを閉じる（破棄する）
            finish()
        }

        tireDeleteBtn.setOnClickListener {
            realm.executeTransaction {
                realm.where<TireData>().equalTo("id", bpId)?.findFirst()?.deleteFromRealm()
            }

            blackToast(applicationContext, "削除しました")
            finish()
        }

        tireCancelBtn.setOnClickListener() {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }


    // 入力箇所（EditText）以外をタップしたときに、フォーカスをオフにする
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // キーボードを隠す
        inputMethodManager.hideSoftInputFromWindow(
            tireEditLayout.getWindowToken(),
            InputMethodManager.HIDE_NOT_ALWAYS
        )
        // 背景にフォーカスを移す
        tireEditLayout.requestFocus()

        return super.onTouchEvent(event)
    }
}