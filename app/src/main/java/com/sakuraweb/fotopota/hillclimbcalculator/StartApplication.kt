package com.sakuraweb.fotopota.hillclimbcalculator

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.createObject

// 一番最初に実行されるApplicationクラス
// いつもの、AppCompatActivity（MainActivity）は、manifest.xmlで最初の画面（Acitivity）として実行される
// Application（CustomApplication）も、manifest.xmlで最初のクラスとして実行される
// で、その実行順位が、Application ＞ AppCompatActivityとなっているので、こっちの方が先
// 今回は、データベース作成のために最初にここで起動させる

class StartApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)

        // オリジナルデータを作る
        val courseList = listOf<CourseDataInit>(

            CourseDataInit("ヤビツ峠（表）", "神奈川", "名古木交差点",11800, 676),
            CourseDataInit("湘南平", "神奈川", "ようこそ看板",1600, 140),
            CourseDataInit("足柄峠", "神奈川県", "竜福寺交差点",11800, 707),
            CourseDataInit("富士ヒルクライム", "山梨県", "料金所",25000, 1270),
            CourseDataInit("富士国際（あざみライン）", "山梨県", "須走",11400, 1200),
            CourseDataInit("椿ライン", "神奈川", "湯河原",17200, 941),
            CourseDataInit("ラルプ・デュエズ", "フランス", "",15500, 1130),
            CourseDataInit("ガリビエ峠", "フランス", "",34800, 2120),
            CourseDataInit("モン・ヴァントゥ", "フランス", "",21100, 1617),
            CourseDataInit("蔵王ヒルクライム", "宮城県", "",18700, 1334),
            CourseDataInit("榛名山ヒルクライム", "群馬県", "",14700, 907),
            CourseDataInit("渋峠", "群馬県", "天狗山レスト",18300, 1004),
            CourseDataInit("嬬恋ヒルクライム", "群馬県", "",19800, 1010),
            CourseDataInit("ツール・ド・草津", "群馬県", "",12300, 768),
            CourseDataInit("赤城山ヒルクライム", "群馬県", "",20800, 1313),
            CourseDataInit("乗鞍ヒルクライム", "長野県", "",20500, 1260),
            CourseDataInit("ツール・ド・八ヶ岳", "長野県", "",25000, 1300),
            CourseDataInit("ツール・ド・美ヶ原", "長野県", "",21600, 1270),
            CourseDataInit("麦草峠", "長野県", "八千穂駅",25400, 1336),
            CourseDataInit("青木峠", "長野県", "松本市中川",19400, 390),
            CourseDataInit("白石峠", "埼玉県", "ときがわ",6200, 539),
            CourseDataInit("根の権現", "埼玉県", "",3900, 340),
            CourseDataInit("風張峠", "東京都", "武蔵五日市駅",58300, 967),
            CourseDataInit("明神・三国峠", "静岡・山梨", "駿河小山駅",17700, 910),
            CourseDataInit("大弛峠", "山梨県", "塩山駅",34500, 1963),
            CourseDataInit("二之瀬峠", "岐阜県", "庭田交差点",6500, 408),
            CourseDataInit("雨沢峠", "愛知県", "上品野交差点",8000, 335),
            CourseDataInit("十三峠", "大阪府", "大竹7丁目交差点",4100, 375),
            CourseDataInit("清滝峠", "大阪府", "中野ランプ南交差点",6000, 221),
            CourseDataInit("鍋谷峠", "大阪府", "和泉市父鬼町",5900, 434),
            CourseDataInit("暗峠", "大阪府", "",2400, 400),
            CourseDataInit("六甲越", "兵庫県", "逆瀬川駅",11400, 828),
            CourseDataInit("灰ヶ峰", "広島県", "西畑交差点",11100, 674)

            )

        // Configuretionを作る
        val config = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()

        // importしたRealmクラスのsetDefaultConfigurationメソッドを呼び出してる
        Realm.setDefaultConfiguration(config)


        // ここからが初期データ読込み作業
        val realm :Realm = Realm.getDefaultInstance()

        // 全部のコースデータをrealmResults配列に読み込む
        val realmResults: RealmResults<CourseData> = realm.where(CourseData::class.java).findAll().sort("id", Sort.DESCENDING)


        // データ数がゼロなら初期データを読み込む。
        // これだけなら、もっと簡単な判定で開始できないのかな・・・？
        // 全部読み込んでおいて、使わないんで・・・。
        if( realmResults.size == 0) {
            realm.beginTransaction()

                var id = 1
                for (course in courseList.reversed()) {
                    val c = realm.createObject<CourseData>(id++)
                    c.name = course.name
                    c.pref = course.pref
                    c.start = course.start
                    c.length = course.length
                    c.height = course.height
                }

            realm.commitTransaction()
        }

        realm.close()
    }
}

public fun BlackToast(c: Context, s: String) {
    val toast = Toast.makeText(c, s, Toast.LENGTH_SHORT)
    val view: View = toast.view

//    view.background.setColorFilter(Color.rgb(0,0,0), PorterDuff.Mode.SRC_IN)
    view.background.colorFilter = PorterDuffColorFilter(Color.rgb(0,0,0), PorterDuff.Mode.SRC_IN)
    view.findViewById<TextView>(android.R.id.message).setTextColor(Color.rgb(255,255,255))

    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
    toast.show()


}

