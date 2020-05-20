package com.sakuraweb.fotopota.hillclimbcalculator

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.realm.RealmResults

/*
アダプタクラスを作る
アダプタとは、ビューに表示するすべてのリストデータを管理し、ビューの各行に当てはめていくオブジェクト

〇表示用ViewHolderインスタンスを生成（onCreateViewHolder）
　レイアウトに従って1行分のViewを生成
　ViewHolderを生成して、上記Viewをセットして完了

〇表示内容の更新（onBindViewHolder）　
　ViewHolderのインスタンスを更新してやる（holder.name = "hoge"）

 ListViewでは、ArrayAdapterやSimpleAdapterなどの既存アダプタが利用できた（データ種別による）
 RecyclerViewでは、アダプがが無いので自分で作ってあげる必要がある
 でたらめなアダプタではいけないので、RecyclerView.adapterでインターフェースを定義されているので、
 必要な3メソッドを実装してやり、のちにメインプログラムでインスタンスを生成、RecycleViewのadapterにセットする
 ３つのメソッドは（onCreateViewHolder, getItemCount, onBindViewHolder）

 なんでも格納できるRealmResultsクラスに対して、BloodPressクラスに限定している
 結局、「RealmResults<BloodPress>」型になる。もう、何が何だか。

 RecyclerViewのサブクラス、Adapterクラス（型引数でViewHolder）を継承する
 */

// RecyclerView上に載せたOKボタンのリスナ
// 実装は別クラスにやらせるとして、とりあえず抽象インターフェースを設定
interface SetCourseListener {
    fun okBtnTapped(ret: CourseData?)
}

// アダプター本体
// 難しくて何がんにゃら自分でもよくわからん
class CourseListRecyclerViewAdapter(realmResults: RealmResults<CourseData>, private val listener: SetCourseListener):
    RecyclerView.Adapter<MyViewHolder>() {

    private val rResults: RealmResults<CourseData> = realmResults

    // 新しく1行分のViewをXMLから生成し、1行分のViewHolderを生成してViewをセットする
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        // 新しいView（1行）を生成する　レイアウト画面で作った、one_result（1行）
        val view = LayoutInflater.from(parent.context).inflate(R.layout.one_course, parent, false)

        // 1行ビューをもとに、ViewHolder（←自分で作ったヤツ）インスタンスを生成
        // 今作ったView（LinearLayout）を渡す
        // ビューホルダは、内部のローカル変数に1行分のデータを保持（日付、血圧、脈拍）
        val viewholder = MyViewHolder(view)
        return viewholder
    }

    // ViewHolderの表示内容を更新する
    // 渡されたビューホルダにデータを書き込む
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val bp = rResults[position]

        if( bp!=null ) {
            val l: Double = bp.length.toDouble()
            val h: Double = bp.height.toDouble()

            holder.nameText?.text = bp.name
            holder.prefText?.text = bp.pref
            holder.startText?.text = bp.start
            holder.lengthText?.text = bp.length.toString()
            holder.heightText?.text = bp.height.toString()
            holder.gradeText?.text = ((h / l * 100*10).toInt().toDouble()/10).toString()

            //        holder.itemView.setBackgroundColor(if (position % 2 == 0) Color.LTGRAY else Color.WHITE)

            holder.editBtn?.setOnClickListener {
                val intent = Intent(it.context, CourseEditActivity::class.java)
                intent.putExtra("id", bp.id)
                it.context.startActivity(intent)
            }

            /*
            // クリックリスナをセットする（onCreateでもいいような気がするけど）
            // これはRecyclerView本体をクリックしたときの処理（ボタンじゃなくて） これもアリかも？
            holder.itemView.setOnClickListener {
                val intent = Intent(it.context, EditActivity::class.java)
                intent.putExtra("id", bp?.id)
                it.context.startActivity(intent)
            }
            */

            holder.itemView.setOnClickListener {
                listener.okBtnTapped(bp)
            }
        }
    }

    // アダプターの必須昨日の、サイズを返すメソッド
    override fun getItemCount(): Int {
        return rResults.size
    }
}