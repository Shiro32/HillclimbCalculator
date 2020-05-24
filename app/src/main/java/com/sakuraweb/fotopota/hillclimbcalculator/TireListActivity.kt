package com.sakuraweb.fotopota.hillclimbcalculator

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.Sort

import kotlinx.android.synthetic.main.activity_tire_list.*
import kotlinx.android.synthetic.main.content_course_list.*
import kotlinx.android.synthetic.main.content_tire_list.*

// 転がり抵抗決定のためのタイヤ選択
// やってることは、コースリスト選択と同じ
class TireListActivity : AppCompatActivity(), SetTireListener {

    // とりあえず、Realmのインスタンスを作る
    private lateinit var realm: Realm

    // アダプタのインスタンス
    private lateinit var adapter: TireListRecyclerViewAdapter

    // レイアウトマネージャーのインスタンス
    private lateinit var layoutManager: RecyclerView.LayoutManager

    // タイヤ選択画面の構築
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tire_list)
        setSupportActionBar(tireToolbar)


        // realmのインスタンスを作る
        val config2 = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .name("tire.realm")
            .build()

        // importしたRealmクラスのsetDefaultConfigurationメソッドを呼び出し、インスタンスも作る
        Realm.setDefaultConfiguration(config2)
        realm = Realm.getDefaultInstance()

        // 追加ボタン（fab）のリスナを設定する（EditActivity画面を呼び出す）
        // 第2引数が投げる先のActivity。KOTLINじゃなくJAVAクラスで渡すため、::class.javaにする

        fab.setOnClickListener {
            val intent = Intent(this, TireEditActivity::class.java)
            startActivity(intent)
        }
    }


    // いよいよここでリスト表示
    // RecyclerViewerのレイアウトマネージャーとアダプターを設定してあげれば、あとは自動
    override fun onStart() {
        super.onStart()

        // 全部のコースデータをrealmResults配列に読み込む
        val realmResults = realm.where(TireData::class.java).findAll().sort("id", Sort.DESCENDING)

        // 1行のViewを表示するレイアウトマネージャーを設定する
        // LinearLayout、GridLayout、独自も選べるが無難にLinearLayoutManagerにする
        layoutManager = LinearLayoutManager(this)
        tireView.layoutManager = layoutManager

        // アダプターを設定する
        adapter = TireListRecyclerViewAdapter(realmResults, this)
        tireView.adapter = this.adapter
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
    override fun tireOkBtnTapped(ret: TireData?) {
        val n:String = ret?.name.toString()
        val c:String = ret?.crr.toString()

        val intent = Intent()
        intent.putExtra("name", n)
        intent.putExtra("crr", c)
        setResult(RESULT_OK, intent)

        finish()
    }
}