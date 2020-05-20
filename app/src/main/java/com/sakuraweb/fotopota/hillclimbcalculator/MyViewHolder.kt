package com.sakuraweb.fotopota.hillclimbcalculator

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.one_course.view.*

// ViewHolderを作る
// 要するに1行分のViewを保持する（データではない）、描画用のインスタンス。
// 保持するView自体はアダプタ側で作って渡されてくる
// 画面に入ってる範囲のViewが用意され、外に出ると再利用されて別要素に切り替わったりする（Recycler）
// RecyclerViewのサブクラス、ViewHolderクラス（引数itemView）を継承して、アプリ用のViewを保持できるようにする
// 継承元のクラス（今回だとRecyclerView.ViewHolder）の引数の型は気にしないのね（変えられないから？）

class MyViewHolder(iv: View) : RecyclerView.ViewHolder(iv) {
    var nameText: TextView? = null
    var prefText: TextView? = null
    var startText: TextView? = null
    var lengthText: TextView? = null // こういうのって、lateinitじゃダメなんだっけ？
    var heightText: TextView? = null
    var gradeText: TextView? = null
    var editBtn: Button? = null


    // 初期化処理
    // KOTLINではプライマリコンストラクタは、引数をローカル変数にコピーするだけ （ivに貰っている）
    // より具体的な初期化処理はinitブロックに記載する模様
    // プライマリコンストラクタ（引数代入）→イニシャライザ（init）→セカンダリコンストラクタ
    // ３つのローカル変数に、３つのTextViewの参照のみを保持している （＝1行で3ポインターだけと効率的）
    // のちに、アダプタの方から、holder.dateText = "1" みたいに設定する
    init {
        // ローカル変数のdateTextに、1行レイアウトのTextViewのビューそのもの
        nameText 	= iv.courseNameText
        startText   = iv.courseStartText
        lengthText 	= iv.courseLengthText
        heightText 	= iv.courseHeightText
        prefText 	= iv.coursePrefText
        gradeText   = iv.courseGradeText
        editBtn		= iv.courseEditBtn
    }
}