import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.gpt.R

class TemplateHelper(private val context: Context) {
    var lastApi: String = ""

    private lateinit var radioGroup: RadioGroup
    private lateinit var editText: EditText
    private lateinit var addButton: Button
    private lateinit var saveButton: Button
    val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

    private var tempSet: MutableSet<String> = mutableSetOf()

    fun showCustomTempAlert() {
        val alertDialogBuilder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.custom_temp_alert, null)

        alertDialogBuilder.setView(dialogView)

        val alertDialog = alertDialogBuilder.create()

        radioGroup = dialogView.findViewById(R.id.radioGroup_temp)
        editText = dialogView.findViewById(R.id.editText_temp)
        addButton = dialogView.findViewById(R.id.addButton_temp)
        saveButton = dialogView.findViewById(R.id.saveButton_temp)

        addButton.setOnClickListener {
            val value = editText.text.toString().trim()
            if (value.isNotEmpty()) {
                addRadioButton(value)
                editText.setText("")

            }
        }

        saveButton.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putStringSet("tempSet", tempSet.toSet())
            editor.apply()
            alertDialog.dismiss()
        }

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = group.findViewById<RadioButton>(checkedId)
            lastApi = radioButton.text.toString()

            val editor = sharedPreferences.edit()
            editor.putString("currTemp", lastApi)
            editor.apply()


            radioGroup.setOnLongClickListener {
                removeSelectedRadioButton()
                true
            }

            Log.e("chkd", "checkedRadioButton?.text is $checkedId")

        }

        initRadioButtons()
        addRadioButton("")
        alertDialog.show()
    }

    private fun addRadioButton(value: String) {
        val radioButton = RadioButton(context)
        radioButton.text = value
        radioGroup.addView(radioButton)

        watcher(tempSet, "before add")

        tempSet.add(value)
        watcher(tempSet, "after add")
    }


    private fun initRadioButtons() {
        val savedTempSet = sharedPreferences.getStringSet("tempSet", emptySet())?.toMutableSet()

        if (savedTempSet != null) {
            tempSet = savedTempSet
            radioGroup.removeAllViews()

            for (temp in savedTempSet) {
                val radioButton = RadioButton(context)
                radioButton.text = temp
                radioGroup.addView(radioButton)
            }
        }

    }

    private fun removeSelectedRadioButton() {
        val checkedButtonId = radioGroup.checkedRadioButtonId
        if (checkedButtonId != -1) {
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedButtonId)
            val value = checkedRadioButton.text.toString()

            watcher(tempSet, "before remove")

            radioGroup.removeView(checkedRadioButton)
            tempSet.remove(value)
            watcher(tempSet, "after remove ")

            Toast.makeText(context, "Удалено $value", Toast.LENGTH_SHORT).show()
            Log.e("chkd", "checkedRadioButton?.text is $value")
        }
    }

    fun watcher(tempSet: MutableSet<String>, wheen: String) {
        Log.e("watcher", "--------------- $wheen")

        Log.e("watcher", "tempSet size is ${tempSet.size}")

        for (temp in tempSet) {
            Log.e("watcher", "temp is $temp")
        }
    }
}
