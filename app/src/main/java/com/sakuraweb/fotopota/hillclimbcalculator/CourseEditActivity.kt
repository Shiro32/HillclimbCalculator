package com.sakuraweb.fotopota.hillclimbcalculator

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_course_edit.*

// 入力画面（CourseEditActivity）の処理
// 新規追加：　コースリストの「＋」ボタンからIntentで呼び出される
//　既存の編集・削除：　コースリストの「編集」ボタンからIntentで呼び出される
class CourseEditActivity : AppCompatActivity() {
    private val tag = "CourseData"
    private lateinit var realm: Realm
    private lateinit var inputMethodManager: InputMethodManager

    // Edit画面の開始
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_edit)

        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // Realmのインスタンスを作る
        // この画面が消される時まで使い続ける
        // 消される時（onDestroy）でcloseしてやる
        realm = Realm.getDefaultInstance()

        // 画面遷移（Intent）によって、新規追加と更新を見分ける
        // idが0以上なら既存（なので削除もあり）
        // idが0なら新規作成
        val bpId = intent.getLongExtra("id", 0L)
        if (bpId > 0L) {
            val bp = realm.where<CourseData>().equalTo("id", bpId).findFirst()
            courseEditNameEdit.setText(bp?.name.toString())
            courseEditPrefEdit.setText(bp?.pref.toString())
            courseEditStartEdit.setText(bp?.start.toString())
            courseEditLengthEdit.setText(bp?.length.toString())
            courseEditHeightEdit.setText(bp?.height.toString())
            courseEditDeleteBtn.visibility = View.VISIBLE
        } else {
            courseEditDeleteBtn.visibility = View.INVISIBLE
        }


        // saveボタンのリスナ （強引にここに全部書く）
        courseEditSaveBtn.setOnClickListener {
            var length: Long = 0
            var height: Long = 0
            var name: String = ""
            var pref: String = ""
            var start: String = ""

            // 各テキストボックスから変数に読込み（text化して、Long化する）
            if (!courseEditLengthEdit.text.isNullOrEmpty()) {
                length = courseEditLengthEdit.text.toString().toLong()
            }
            if (!courseEditHeightEdit.text.isNullOrEmpty()) {
                height = courseEditHeightEdit.text.toString().toLong()
            }
            if( !courseEditNameEdit.text.isNullOrEmpty()) {
                name = courseEditNameEdit.text.toString()
            }
            if( !courseEditPrefEdit.text.isNullOrEmpty()) {
                pref = courseEditPrefEdit.text.toString()
            }
            if( !courseEditStartEdit.text.isNullOrEmpty()) {
                start = courseEditStartEdit.text.toString()
            }



            when (bpId) {
                0L -> {
                    // Realmに登録する
                    // トランザクションを1行のメソッド（executeTransaction）で実行する
                    realm.executeTransaction {

                        // whereクエリでidの最大値（今まで書かれた最後の部分）をゲットする
                        // whereクエリの戻り値はRealmQueryオブジェクトであり、続けざまにmaxメソッドができる
                        // で、出来上がったmaxIDは、たぶん、RealmQueryオブジェクト
                        val maxID = realm.where<CourseData>().max("id")

                        // エルビス演算子（?:）でチェック。 要するに+1で次の要素にする（相変わらず、RealmQuery）
                        val nextID = (maxID?.toLong() ?: 0L) + 1L

                        // ようやく、１レコードを生成する
                        // CourseDataは自分で設計したクラス。そのインスタンスとして作る
                        // で、そのフィールドに直接書き込む（なんかすごいが）
                        // createObjectメソッドは型パラメータと、idが必要
                        // 型パラメータにより任意のレコード型を作れる。すごいねぇ・・・。
                        val bp: CourseData = realm.createObject<CourseData>(nextID)
                        bp.name = name
                        bp.length = length
                        bp.pref = pref
                        bp.start = start
                        bp.height = height

                    } // トランザクションを1行で書いているので、これで書き込み終了（簡単すぎ）
                }
                else -> { // 修正作業
                    realm.executeTransaction {
                        val bp = realm.where<CourseData>().equalTo("id", bpId).findFirst()
                        bp?.name = name
                        bp?.pref = pref
                        bp?.start = start
                        bp?.length = length
                        bp?.height = height
                        }
                }
            }

            blackToast(applicationContext, "保存しましたよ")

            // アクティビティを閉じる（破棄する）
            finish()
        }

        courseEditDeleteBtn.setOnClickListener {
            realm.executeTransaction {
                realm.where<CourseData>().equalTo("id", bpId)?.findFirst()?.deleteFromRealm()
            }

            blackToast(applicationContext, "削除しました")
            finish()
        }

        courseEditCancelBtn.setOnClickListener() {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }


    // 入力箇所（EditText）以外をタップしたときに、フォーカスをオフにする
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        // キーボードを隠す
        inputMethodManager.hideSoftInputFromWindow(editLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)
        // 背景にフォーカスを移す
        editLayout.requestFocus()

        return super.dispatchTouchEvent(event)
    }
}
