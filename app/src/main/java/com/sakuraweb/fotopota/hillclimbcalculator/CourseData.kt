package com.sakuraweb.fotopota.hillclimbcalculator

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

// realmで保存単位（レコード）とするCourseDataクラス
// RealmObjectを継承して作る
// RealmObjectはコンストラクタに引数は無いので、そのまま継承
// Javaは標準で継承可能だけど、Kotlinは継承不可能なので、明示的にopenする

open class CourseData : RealmObject() {
    // よくわからんが、PrimaryKeyというアノテーションで、DBの検索キーにするらしい
    @PrimaryKey
    var id: Long = 0

    var name: String = "name"
    var pref: String = "pref"
    var start: String = "start"
    var length: Long = 0
    var height: Long = 0
}
