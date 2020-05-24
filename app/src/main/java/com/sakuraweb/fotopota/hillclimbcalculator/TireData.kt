package com.sakuraweb.fotopota.hillclimbcalculator

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

// タイヤの転がり抵抗データベース用のCRRDataクラス

open class TireData : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    
    var type: String = ""
    var maker: String  = ""
    var name: String = ""
    var crr: Double = 0.0
    var press: Double = 0.0
}