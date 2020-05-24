package com.sakuraweb.fotopota.hillclimbcalculator

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.realm.RealmResults

// RecyclerView上に載せたOKボタンのリスナ
// 実装は別クラスにやらせるとして、とりあえず抽象インターフェースを設定
interface SetTireListener {
    fun tireOkBtnTapped(ret: TireData?)
}

// アダプター本体
// 難しくて何がんにゃら自分でもよくわからん
class TireListRecyclerViewAdapter(realmResults: RealmResults<TireData>, private val listener: SetTireListener):
    RecyclerView.Adapter<TireMyViewHolder>() {

    private val rResults: RealmResults<TireData> = realmResults

    // 新しく1行分のViewをXMLから生成し、1行分のViewHolderを生成してViewをセットする
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TireMyViewHolder {

        // 新しいView（1行）を生成する　レイアウト画面で作った、one_result（1行）
        val view = LayoutInflater.from(parent.context).inflate(R.layout.one_tire, parent, false)

        // 1行ビューをもとに、ViewHolder（←自分で作ったヤツ）インスタンスを生成
        // 今作ったView（LinearLayout）を渡す
        // ビューホルダは、内部のローカル変数に1行分のデータを保持（日付、血圧、脈拍）
        val viewholder = TireMyViewHolder(view)
        return viewholder
    }

    // ViewHolderの表示内容を更新する
    // 渡されたビューホルダにデータを書き込む
    override fun onBindViewHolder(holder: TireMyViewHolder, position: Int) {
        val bp = rResults[position]

        if( bp!=null ) {
            holder.type?.text = "【"+bp.type+"】"
            holder.nameText?.text = bp.name
            holder.makerText?.text = bp.maker
            holder.crrText?.text = bp.crr.toString()
            holder.pressText?.text = "（＠"+bp.press.toString()+"気圧）"

            holder.editBtn?.setOnClickListener {
                val intent = Intent(it.context, TireEditActivity::class.java)
                intent.putExtra("id", bp.id)
                it.context.startActivity(intent)
            }

            holder.itemView.setOnClickListener {
                listener.tireOkBtnTapped(bp)
            }
        }
    }

    // アダプターの必須昨日の、サイズを返すメソッド
    override fun getItemCount(): Int {
        return rResults.size
    }
}