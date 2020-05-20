package com.sakuraweb.fotopota.hillclimbcalculator

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import io.realm.Sort

import kotlinx.android.synthetic.main.activity_course_list.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_course_list.*

// コース選択画面（リスト形式から選択や追加）
// SetCourseListnerはRecyolerViewの決定用で使うため、マルチ継承（何が何だか・・・）
class CourseListActivity : AppCompatActivity(), SetCourseListener {

    // とりあえず、Realmのインスタンスを作る
    private lateinit var realm: Realm

    // アダプタのインスタンス
    private lateinit var adapter: CourseListRecyclerViewAdapter

    // レイアウトマネージャーのインスタンス
    private lateinit var layoutManager: RecyclerView.LayoutManager

    // コース選択画面の構築
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_list)
        setSupportActionBar(toolbar)

        // realmのインスタンスを作る
        realm = Realm.getDefaultInstance()

        // 追加ボタン（fab）のリスナを設定する（EditActivity画面を呼び出す）
        // 第2引数が投げる先のActivity。KOTLINじゃなくJAVAクラスで渡すため、::class.javaにする

        fab.setOnClickListener {
            val intent = Intent(this, CourseEditActivity::class.java)
            startActivity(intent)
        }

/*
		fab.setOnClickListener { view ->
			Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
				.setAction("Action", null).show()
		}
*/
    }


    // いよいよここでリスト表示
    // RecyclerViewerのレイアウトマネージャーとアダプターを設定してあげれば、あとは自動
    override fun onStart() {
        super.onStart()

        // 全部のコースデータをrealmResults配列に読み込む
        val realmResults = realm.where(CourseData::class.java).findAll().sort("id", Sort.DESCENDING)

        // 1行のViewを表示するレイアウトマネージャーを設定する
        // LinearLayout、GridLayout、独自も選べるが無難にLinearLayoutManagerにする
        layoutManager = LinearLayoutManager(this)
        courseView.layoutManager = layoutManager

        // アダプターを設定する
        adapter = CourseListRecyclerViewAdapter(realmResults, this)
        courseView.adapter = this.adapter
    }

    // 終了処理
    // たぶん、何も選択しないで「戻る」などしたときは、これだけ呼ばれるのかな？

    override fun onDestroy() {
        super.onDestroy()
        realm.close()

//        Toast.makeText(applicationContext, "リスト画面消します！", Toast.LENGTH_LONG).show()
    }

    // 選択画面（RecyclerViewの決定ボタン）で確定した際に呼ばれるリスナ
    // Intentを作ってメイン画面に戻っているだけ
    // この画面自体を殺さなくていいのか、finishやrealm.closeは要らないのか、後で確認しましょう
    override fun okBtnTapped(ret: CourseData?) {
        val l:String = ret?.length.toString()
        val h:String = ret?.height.toString()
        val n:String = ret?.name.toString()

//        val intent = Intent(applicationContext, MainActivity::class.java)

        val intent = Intent()
        intent.putExtra("length", l)
        intent.putExtra("height", h)
        intent.putExtra("name", n)
        setResult(RESULT_OK, intent)
//        startActivity(intent)
//		Toast.makeText(applicationContext, "WRITE!{$l}/{$h}", Toast.LENGTH_LONG).show()

        // この画面自体を消し去ろう
        finish()
    }
}
