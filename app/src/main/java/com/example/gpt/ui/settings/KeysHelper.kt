import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.gpt.R

class KeysHelper(private val context: Context) {
    var lastApi: String = ""

    private lateinit var radioGroup: RadioGroup
    private lateinit var editText: EditText
    private lateinit var addButton: Button
    private lateinit var saveButton: Button
    val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

    private var keysSet: MutableSet<String> = mutableSetOf()


    fun showCustomKeysAlert() {
        val alertDialogBuilder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.custom_keys_alert, null)

        alertDialogBuilder.setView(dialogView)

        val alertDialog = alertDialogBuilder.create()

        radioGroup = dialogView.findViewById(R.id.radioGroup)
        editText = dialogView.findViewById(R.id.editText)
        addButton = dialogView.findViewById(R.id.addButton)
        saveButton = dialogView.findViewById(R.id.saveButton)


        addButton.setOnClickListener {
            val value = editText.text.toString().trim()
            if (value.isNotEmpty()) {
                addRadioButton(value)
                editText.setText("")
            }
        }

        saveButton.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putStringSet("keysSet", keysSet.toSet())
            editor.apply()
            alertDialog.dismiss()
        }


        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = group.findViewById<RadioButton>(checkedId)
            lastApi = radioButton.text.toString()
            Log.e("watcher", "lastApi is $lastApi")


            radioGroup.setOnLongClickListener {
                removeSelectedRadioButton()
                true
            }

            Log.e("chkd", "checkedRadioButton?.text is $checkedId")

        }

        initRadioButtons()

        alertDialog.show()
    }

    private fun addRadioButton(value: String) {
        val radioButton = RadioButton(context)
        radioButton.text = value
        radioGroup.addView(radioButton)

        watcher(keysSet, "before add")

        keysSet.add(value)
        watcher(keysSet, "after add")
    }


    private fun initRadioButtons() {
        val savedKeysSet = sharedPreferences.getStringSet("keysSet", emptySet())?.toMutableSet()

        if (savedKeysSet != null) {
            keysSet = savedKeysSet
            radioGroup.removeAllViews()

            for (key in savedKeysSet) {
                val radioButton = RadioButton(context)
                radioButton.text = key
                radioGroup.addView(radioButton)
            }
        }

    }

    private fun removeSelectedRadioButton() {
        val checkedButtonId = radioGroup.checkedRadioButtonId
        if (checkedButtonId != -1) {
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedButtonId)
            val value = checkedRadioButton.text.toString()

            watcher(keysSet, "before remove")

            radioGroup.removeView(checkedRadioButton)
            keysSet.remove(value)
            watcher(keysSet, "after remove ")

            Toast.makeText(context, "Удалено $value", Toast.LENGTH_SHORT).show()
            Log.e("chkd", "checkedRadioButton?.text is $value")
        }
    }

    fun watcher(keysSet: MutableSet<String>, wheen: String ){
        Log.e("watcher", "--------------- $wheen")

        Log.e("watcher", "keysSet size is ${keysSet.size}")

        for(key in keysSet){
            Log.e("watcher", "key is $key")
        }
    }
}
