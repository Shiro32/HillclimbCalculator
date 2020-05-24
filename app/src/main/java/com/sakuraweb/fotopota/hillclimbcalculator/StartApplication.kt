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

        // Realm全体の初期化処理
        Realm.init(this)

        createCourseData()
        createTireData()
    }
}

// 黒いToast画面を出すだけ
public fun blackToast(c: Context, s: String) {
    val toast = Toast.makeText(c, s, Toast.LENGTH_SHORT)
    val view: View = toast.view

//    view.background.setColorFilter(Color.rgb(0,0,0), PorterDuff.Mode.SRC_IN)
    view.background.colorFilter = PorterDuffColorFilter(Color.rgb(0,0,0), PorterDuff.Mode.SRC_IN)
    view.findViewById<TextView>(android.R.id.message).setTextColor(Color.rgb(255,255,255))

    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
    toast.show()
}


// コース選択用ＤＢ構築
// DBがゼロ件かどうかチェックして、無いなら一から作る
private fun createCourseData() {

    // Configurationを作る
    val config = RealmConfiguration.Builder()
        .deleteRealmIfMigrationNeeded()
        .name("course.realm")
//            .modules(CourseData())
        .build()

    // importしたRealmクラスのsetDefaultConfigurationメソッドを呼び出し、インスタンスも作る
    Realm.setDefaultConfiguration(config)

    // 全部のコースデータをrealmResults配列に読み込む
    val realm = Realm.getDefaultInstance()
    val realmResults: RealmResults<CourseData> = realm.where(CourseData::class.java).findAll().sort("id", Sort.DESCENDING)

    // データゼロなら作る
    if( realmResults.size == 0 ) {

        // オリジナルデータを作る
        val courseList = listOf<CourseDataInit>(
            CourseDataInit("ヤビツ峠（表）", "神奈川", "名古木交差点", 11800, 676),
            CourseDataInit("湘南平", "神奈川", "ようこそ看板", 1600, 140),
            CourseDataInit( "湘南国際村（山）", "神奈川", "麓～横須賀市看板", 2700, 135),
            CourseDataInit( "湘南国際村（海）", "神奈川", "麓～横須賀市看板", 3000, 170),
            CourseDataInit("足柄峠", "神奈川県", "竜福寺交差点", 11800, 707),
            CourseDataInit("富士ヒルクライム", "山梨県", "料金所", 25000, 1270),
            CourseDataInit("富士国際（あざみライン）", "山梨県", "須走", 11400, 1200),
            CourseDataInit("椿ライン", "神奈川", "湯河原", 17200, 941),
            CourseDataInit("ラルプ・デュエズ", "フランス", "", 15500, 1130),
            CourseDataInit("ガリビエ峠", "フランス", "", 34800, 2120),
            CourseDataInit("モン・ヴァントゥ", "フランス", "", 21100, 1617),
            CourseDataInit("蔵王ヒルクライム", "宮城県", "", 18700, 1334),
            CourseDataInit("榛名山ヒルクライム", "群馬県", "", 14700, 907),
            CourseDataInit("渋峠", "群馬県", "天狗山レスト", 18300, 1004),
            CourseDataInit("嬬恋ヒルクライム", "群馬県", "", 19800, 1010),
            CourseDataInit("ツール・ド・草津", "群馬県", "", 12300, 768),
            CourseDataInit("赤城山ヒルクライム", "群馬県", "", 20800, 1313),
            CourseDataInit("乗鞍ヒルクライム", "長野県", "", 20500, 1260),
            CourseDataInit("ツール・ド・八ヶ岳", "長野県", "", 25000, 1300),
            CourseDataInit("ツール・ド・美ヶ原", "長野県", "", 21600, 1270),
            CourseDataInit("麦草峠", "長野県", "八千穂駅", 25400, 1336),
            CourseDataInit("青木峠", "長野県", "松本市中川", 19400, 390),
            CourseDataInit("白石峠", "埼玉県", "ときがわ", 6200, 539),
            CourseDataInit("根の権現", "埼玉県", "", 3900, 340),
            CourseDataInit("風張峠", "東京都", "武蔵五日市駅", 58300, 967),
            CourseDataInit("ジャイアンツ坂", "東京都", "", 600, 61),
            CourseDataInit("明神・三国峠", "静岡・山梨", "駿河小山駅", 17700, 910),
            CourseDataInit("大弛峠", "山梨県", "塩山駅", 34500, 1963),
            CourseDataInit("二之瀬峠", "岐阜県", "庭田交差点", 6500, 408),
            CourseDataInit("雨沢峠", "愛知県", "上品野交差点", 8000, 335),
            CourseDataInit("十三峠", "大阪府", "大竹7丁目交差点", 4100, 375),
            CourseDataInit("清滝峠", "大阪府", "中野ランプ南交差点", 6000, 221),
            CourseDataInit("鍋谷峠", "大阪府", "和泉市父鬼町", 5900, 434),
            CourseDataInit("暗峠", "大阪府", "", 2400, 400),
            CourseDataInit("六甲越", "兵庫県", "逆瀬川駅", 11400, 828),
            CourseDataInit("灰ヶ峰", "広島県", "西畑交差点", 11100, 674)
        )

        // DBに書き込む
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


// タイヤ転がり抵抗用ＤＢの構築
private fun createTireData() {

    // Configurationを作る
    val config2 = RealmConfiguration.Builder()
        .deleteRealmIfMigrationNeeded()
        .name("tire.realm")
        .build()

    // importしたRealmクラスのsetDefaultConfigurationメソッドを呼び出し、インスタンスも作る
    Realm.setDefaultConfiguration(config2)

    // 全部のタイヤ転がり抵抗データをrealmResults配列に読み込む
    val realm2 = Realm.getDefaultInstance()
    val realmResults2: RealmResults<TireData> =
        realm2.where(TireData::class.java).findAll().sort("id", Sort.DESCENDING)

    // データゼロなら作る
    if (realmResults2.size == 0) {

        // オリジナルデータを作る
        val crrList = listOf<TireDataInit>(
            TireDataInit("CL", "Vittoria", "Corsa G+ 2.0",0.0039, 8.3),
            TireDataInit("CL", "Vittoria", "Corsa G+ 1.0",0.00366, 8.3),
            TireDataInit("CL", "Vittoria", "Corsa Control G+ 2.0",0.00423, 8.3),
            TireDataInit("CL", "Vittoria", "Corsa Control G+ 1.0",0.00468, 8.3),
            TireDataInit("TL", "Vittoria", "Corsa Speed G+ 2.0",0.0021, 8.3),
            TireDataInit("TL", "Vittoria", "Corsa Speed G+ 1.0",0.00231, 8.3),
            TireDataInit("TU", "Vittoria", "Corsa Speed",0.00264, 9.7),
            TireDataInit("TU", "Vittoria", "Corsa Speed",0.00273, 8.3),
            TireDataInit("CL", "Vittoria", "Zaffiro Slick",0.00471, 8.3),
            TireDataInit("CL", "Vittoria", "Zaffiro Pro G+ 2.0",0.00489, 8.3),
            TireDataInit("CL", "Vittoria", "Rubino Pro Speed",0.00357, 8.3),
            TireDataInit("CL", "Vittoria", "Rubino Pro III",0.00402, 8.3),
            TireDataInit("CL", "Vittoria", "Rubino Pro G+ 2.0",0.00456, 8.3),
            TireDataInit("CL", "Vittoria", "Rubino Pro G+ 1.0",0.00444, 8.3),
            TireDataInit("CL", "Vittoria", "Rubino Pro Control",0.00507, 8.3),
            TireDataInit("CL", "Vittoria", "Open Corsa CX III",0.0039, 8.3),
            TireDataInit("TU", "Vittoria", "Corsa G+",0.00369, 8.3),
            TireDataInit("TU", "Vittoria", "Corsa Elite",0.00387, 8.3),
            TireDataInit("CL", "Continental", "Grand Prix TT",0.00297, 8.3),
            TireDataInit("CL", "Continental", "Grand Prix SuperSonic",0.00306, 8.3),
            TireDataInit("CL", "Continental", "Grand Prix Force II",0.0033, 8.3),
            TireDataInit("CL", "Continental", "Grand Prix 5000",0.003, 8.3),
            TireDataInit("CL", "Continental", "Grand Prix 4000S II",0.00366, 8.3),
            TireDataInit("CL", "Continental", "Grand Prix 4000 RS",0.00315, 8.3),
            TireDataInit("CL", "Continental", "Grand Prix 4 Season",0.00516, 8.3),
            TireDataInit("TL", "Continental", "Grand Prix Attack II",0.00351, 8.3),
            TireDataInit("TL", "Continental", "Grand Prix 5000 TL",0.00249, 8.3),
            TireDataInit("TU", "Continental", "Competition",0.00426, 8.3),
            TireDataInit("CL", "Continental", "Ultra Sport III",0.00423, 8.3),
            TireDataInit("CL", "Continental", "Ultra Sport II",0.00429, 8.3),
            TireDataInit("CL", "Schwalbe", "Ultremo ZX",0.00423, 8.3),
            TireDataInit("CL", "Schwalbe", "Pro One Addix",0.00384, 8.3),
            TireDataInit("CL", "Schwalbe", "One V-Guard",0.00369, 8.3),
            TireDataInit("CL", "Schwalbe", "One Performance Addix",0.00462, 8.3),
            TireDataInit("CL", "Schwalbe", "Durano Plus Addix",0.00492, 8.3),
            TireDataInit("CL", "Schwalbe", "Durano",0.0054, 8.3),
            TireDataInit("TL", "Schwalbe", "Pro One Tubeless",0.0033, 8.3),
            TireDataInit("TL", "Schwalbe", "Pro One TT TLE Addix",0.00306, 8.3),
            TireDataInit("TL", "Schwalbe", "One Tubeless",0.00354, 8.3),
            TireDataInit("TL", "Schwalbe", "One TLE Performance Addix",0.00474, 8.3),
            TireDataInit("TL", "Schwalbe", "Ironman Tubeless",0.00387, 8.3),
            TireDataInit("TU", "Schwalbe", "Pro One HT",0.00474, 9.7),
            TireDataInit("TU", "Schwalbe", "Pro One HT",0.00483, 8.3),
            TireDataInit("TL", "Hutchinson", "Fusion 5 Performance TL",0.00393, 8.3),
            TireDataInit("TL", "Hutchinson", "Fusion 5 Galactik TL",0.00303, 8.3),
            TireDataInit("TL", "Hutchinson", "Fusion 5 All Season TL",0.00429, 8.3),
            TireDataInit("CL", "Michelin", "Pro 4 Service Sourse",0.00447, 8.3),
            TireDataInit("CL", "Michelin", "Pro 4 Endurance v2",0.00426, 8.3),
            TireDataInit("CL", "Michelin", "Power Time Trial",0.00258, 8.6),
            TireDataInit("CL", "Michelin", "Power Road",0.00372, 8.3),
            TireDataInit("CL", "Michelin", "Power Endurance",0.00435, 8.3),
            TireDataInit("CL", "Michelin", "Power Competition",0.00327, 8.3),
            TireDataInit("CL", "Michelin", "Lithion 2",0.00477, 8.3),
            TireDataInit("CL", "Michelin", "Krylion 2 Endurance",0.00462, 8.3),
            TireDataInit("TL", "Michelin", "Power Road TLR",0.00303, 8.3),
            TireDataInit("CL", "Mavic", "Yksion Elite",0.0045, 8.3),
            TireDataInit("TL", "Mavic", "Yksion Pro UST Tubeless",0.00351, 8.3),
            TireDataInit("CL", "Maxxis", "Re-Fuse",0.00677, 8.3),
            TireDataInit("CL", "Maxxis", "High Road",0.00396, 8.3),
            TireDataInit("TL", "Maxxis", "Padrone Tubeless Ready",0.00393, 8.3),
            TireDataInit("CL", "Panaracer", "Race L Evo 3",0.00417, 8.3),
            TireDataInit("CL", "Panaracer", "Race D Evo 3",0.00522, 8.3),
            TireDataInit("CL", "Panaracer", "Race A Evo 3",0.00492, 8.3),
            TireDataInit("CL", "Pirelli", "P Zero Velo TT",0.00306, 8.3),
            TireDataInit("CL", "Pirelli", "P Zero Velo 4S",0.00441, 8.3),
            TireDataInit("TL", "Pirelli", "Cinturato Velo TLR",0.00468, 8.3),
            TireDataInit("CL", "Specialized", "Turbo Cotton",0.00303, 8.3),
            TireDataInit("CL", "Specialized", "S-Works Turbo",0.00402, 8.3),
            TireDataInit("CL", "Veloflex", "Corsa",0.00402, 8.3),
            TireDataInit("CL", "Bontrager", "R4 320",0.00345, 8.3),
            TireDataInit("CL", "Zipp", "Tangente Speed",0.00348, 8.3),
            TireDataInit("CL", "Zipp", "Tangente Course",0.00459, 8.3)

        )

        // DBに書き込む
        realm2.beginTransaction()

        var id = 1
        for (crr in crrList.reversed()) {
            val c = realm2.createObject<TireData>(id++)
            c.type = crr.type
            c.maker = crr.maker
            c.name = crr.name
            c.crr = crr.crr
            c.press = crr.press
        }

        realm2.commitTransaction()
    }

    realm2.close()
}
