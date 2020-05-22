package com.sakuraweb.fotopota.hillclimbcalculator

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RadioButton
import kotlinx.android.synthetic.main.activity_pos_select.*

class PosSelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pos_select)

        posSaveBtn.isEnabled = false

        posGroup.setOnCheckedChangeListener() { _, _ ->
            posSaveBtn.isEnabled = true
        }

        posSaveBtn.setOnClickListener() {
            var value = 1.0
            var name = ""

            val id = posGroup.checkedRadioButtonId
            if (id > 0) {
                val sel = findViewById<RadioButton>(id).text

                when (sel) {
                    getString(R.string.posUnderBtnText) -> {
                        name = "下ハンドル"
                        value = 1.0
                    }
                    getString(R.string.posBlacketBtnText) -> {
                        name = "ブラケットポジション"
                        value = 1.25
                    }
                    getString(R.string.posTopBtnText) -> {
                        name = "上ハンドル"
                        value = 1.38
                    }
                    getString(R.string.posDancingBtnText) -> {
                        name = "ダンシング"
                        value = 1.73
                    }
                    getString(R.string.posAeroBtnText) -> {
                        name = "エアロポジション"
                        value = 0.94
                    }
                    else -> { finish() }
                }
            } else { finish() }

            val intent = Intent()
            intent.putExtra("name", name)
            intent.putExtra("value", value.toString())
            setResult(RESULT_OK, intent)
            finish()
        }

        posCancelBtn.setOnClickListener() {
            finish()
        }
    }
}
