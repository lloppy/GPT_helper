package com.example.gpt.ui.home

import AsteriskPasswordTransformationMethod
import KeysHelper
import android.Manifest
import android.animation.ObjectAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Editable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gpt.R
import com.example.gpt.api.OpenAiAPI
import com.example.gpt.api.model.ChatCompletionRequest
import com.example.gpt.api.model.ChatCompletionResponse
import com.example.gpt.api.model.Message
import com.example.gpt.api.model.TranscriptResponse
import com.example.gpt.databinding.FragmentHomeBinding
import com.example.gpt.firebase.FirebaseHelper
import com.example.gpt.popup.LoadingScreen
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.util.*

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var loading: LoadingScreen
    private lateinit var tts: TextToSpeech
    private var isRecording = false
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var audioFile: File

    private lateinit var messages: MutableList<Message>

    private lateinit var sendPrompt: ImageView
    private lateinit var sendAudio: ImageView
    private lateinit var deleteButton: ImageView
    private lateinit var deleteTempButton: ImageView
    private lateinit var inputPrompt: EditText
    private lateinit var tokenApi: EditText
    private lateinit var finalResultContainer: LinearLayout
    private lateinit var record: ImageView

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        sendPrompt = root.findViewById(R.id.sendPropmt)
        sendAudio = root.findViewById(R.id.sendAudio)
        deleteButton = root.findViewById(R.id.deleteAll)
        deleteTempButton = root.findViewById(R.id.delete_template)
        inputPrompt = root.findViewById(R.id.inputPrompt)
        tokenApi = root.findViewById(R.id.apiToken)
        tokenApi.transformationMethod = AsteriskPasswordTransformationMethod()
        finalResultContainer = root.findViewById(R.id.finalResultContainer)
        record = root.findViewById(R.id.recording_indicator)

        record.setOnClickListener { stopRecordingAndSendAudio(tokenApi) }
        deleteTempButton.setOnClickListener { inputPrompt.text.clear() }

        val bot = Message("system", "Голосовой ассистент")
        messages = mutableListOf(bot)

        checkRecordAudioPermission()
        initTextToSpeech()
        initViews()

        val firebaseHelper = FirebaseHelper()
        firebaseHelper.loadUser()

        checker()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkRecordAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                0
            )
        }
    }

    private fun initTextToSpeech() {
        tts = TextToSpeech(requireContext()) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale("zh")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initViews() {
        configureInputPrompt(inputPrompt)
        setupSendPromptClickListener(sendPrompt, inputPrompt, tokenApi, finalResultContainer)
        setupSendAudioClickListener(sendAudio, tokenApi)
        setupDeleteButtonClickListener(deleteButton, inputPrompt, finalResultContainer)
    }

    private fun configureInputPrompt(inputPrompt: EditText) {
        inputPrompt.setHorizontallyScrolling(false)
    }

    private fun setupSendPromptClickListener(
        sendPrompt: ImageView,
        inputPrompt: EditText,
        tokenApi: EditText,
        finalResultContainer: LinearLayout
    ) {

        sendPrompt.setOnClickListener {
            if (inputPrompt.text?.isEmpty() == true) checker()

            if (inputPrompt.text?.isEmpty() == true) {
                showToastMessage("Введите запрос")
            } else if (tokenApi.text.isEmpty()) {
                showToastMessage("Введите свой токен, чтобы отправить боту запрос")
            } else {
                sendTextPrompt(inputPrompt, tokenApi, finalResultContainer, messages)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        checker()
    }

    fun checker() {
        var newKey = ""
        val sharedPreferences = context!!.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val savedKeysSet = sharedPreferences.getStringSet("keysSet", emptySet())?.toMutableSet()
        val currKey = sharedPreferences.getString("currKey", String())

        if (currKey!!.isNotEmpty()){
            newKey = currKey
            val editableText: Editable = Editable.Factory.getInstance().newEditable(newKey)
            tokenApi.text = editableText
        }
        else {
            if(savedKeysSet!!.isNotEmpty()){
                newKey = savedKeysSet!!.first()
                val editableText: Editable = Editable.Factory.getInstance().newEditable(newKey)
                tokenApi.text = editableText
            }
        }


    }

    private fun setupSendAudioClickListener(sendAudio: ImageView, tokenApi: EditText) {
        sendAudio.setOnClickListener {
            if (tokenApi.text.isEmpty()) {
                showToastMessage("Введите свой токен, чтобы отправить боту запрос")
            } else {
                if (isRecording) {
                    stopRecordingAndSendAudio(tokenApi)
                } else {
                    startRecording()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setupDeleteButtonClickListener(
        deleteButton: ImageView,
        inputPrompt: EditText,
        finalResultContainer: LinearLayout
    ) {
        deleteButton.setOnClickListener {
            val alertDialog = AlertDialog.Builder(requireContext())
                .setTitle("Подтверждение удаления")
                .setMessage("Вы уверены, что хотите удалить всю историю сообщений?")
                .setPositiveButton("Удалить") { dialog, _ ->

                    inputPrompt.text.clear()
                    finalResultContainer.removeAllViews()
                    messages.removeIf { message -> message.role != "system" }
                    dialog.dismiss()
                }
                .setNegativeButton("Отмена") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

            alertDialog.setOnShowListener {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.RED)
            }
            alertDialog.show()
        }
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun addMessage(container: LinearLayout, message: String, isQuestion: Boolean) {
        val paddingInDp = 20
        val paddingInPx = (paddingInDp * resources.displayMetrics.density).toInt()

        val textView = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = if (isQuestion) Gravity.END else Gravity.START
            }
            text = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY)
            setTextColor(Color.WHITE)
            textSize = 20f
            if (isQuestion) {
                setPadding(paddingInPx, 0, 0, 0)
            } else {
                setPadding(0, 0, paddingInPx, 0)
            }
        }
        container.addView(textView)
    }


    private fun sendTextPrompt(
        inputPrompt: EditText,
        tokenApi: EditText,
        finalResultContainer: LinearLayout,
        messages: MutableList<Message>
    ) {
        loading = LoadingScreen(requireActivity())
        loading.execute()

        val question = "<b>我:</b> ${inputPrompt.text}<br>"
        addMessage(finalResultContainer, question, true)

        val message = Message("user", inputPrompt.text.toString())
        messages.add(message)
        val request = ChatCompletionRequest("gpt-3.5-turbo", messages)


        OpenAiAPI(tokenApi.text.toString()).chatCompletion(request)
            .enqueue(object : Callback<ChatCompletionResponse> {
                override fun onFailure(call: Call<ChatCompletionResponse>, t: Throwable) {
                    showToastMessage("Ошибка при вызове службы ChatCompletion")
                    loading.isDismiss()
                }

                override fun onResponse(
                    call: Call<ChatCompletionResponse>,
                    response: Response<ChatCompletionResponse>
                ) {
                    val chatGpt =
                        "<b>ChatGPT:</b> ${response.body()?.choices?.get(0)?.message?.content}<br>"
                    addMessage(finalResultContainer, chatGpt, false)

                    val chatGptPlainText = response.body()?.choices?.get(0)?.message?.content ?: ""
                    speakText(chatGptPlainText)

                    loading.isDismiss()
                }
            })
    }

    private fun stopRecordingAndSendAudio(tokenApi: EditText) {
        toggleRecordingIndicator(false)
        loading = LoadingScreen(requireActivity())
        loading.execute()
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null
        isRecording = false

        sendAudio(tokenApi)
    }

    private fun sendAudio(tokenApi: EditText) {
        val audioRequestBody = audioFile.asRequestBody("audio/m4a".toMediaTypeOrNull())
        val audioPart = MultipartBody.Part.createFormData("file", audioFile.name, audioRequestBody)
        val modelRequestBody = "whisper-1".toRequestBody("whisper-1".toMediaTypeOrNull())

        if (audioFile.length() == 0L) {
            showToastMessage("Аудиофайл пуст")
            loading.isDismiss()
        } else {
            OpenAiAPI(tokenApi.text.toString()).sendAudio(
                audio = audioPart,
                model = modelRequestBody
            ).enqueue(object : Callback<TranscriptResponse> {
                override fun onFailure(call: Call<TranscriptResponse>, t: Throwable) {
                    showToastMessage("Ошибка при вызове службы SpeechToText")
                    loading.isDismiss()
                }

                override fun onResponse(
                    call: Call<TranscriptResponse>,
                    response: Response<TranscriptResponse>
                ) {
                    if (!response.isSuccessful) {
                        val errorMessage = response.errorBody()?.string() ?: "Неизвестная ошибка"
                        showToastMessage(
                            "Ошибка при вызове службы SpeechToText. Код ошибки: ${response.code()}, Сообщение: $errorMessage"
                        )
                        loading.isDismiss()
                        return
                    }
                    val transcribedText = response.body()?.text ?: ""
                    inputPrompt.setText(transcribedText)
                    loading.isDismiss()
                }
            })
        }
    }

    private fun startRecording() {
        audioFile = File(requireContext().externalCacheDir?.absolutePath + "/audio.m4a")

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(audioFile.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        }

        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            toggleRecordingIndicator(true)
            isRecording = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun speakText(text: String) {
        val utteranceId = this.hashCode().toString() + System.currentTimeMillis()
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onDone(utteranceId: String?) {}

            override fun onError(utteranceId: String?) {}
        })

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    override fun onDestroy() {
        if (this::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

    private fun toggleRecordingIndicator(visible: Boolean) {
        val recordingIndicator: ImageView = _binding!!.recordingIndicator
        if (visible) {
            val blinkAnimation = ObjectAnimator.ofInt(recordingIndicator, "alpha", 0, 255).apply {
                duration = 1000
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = ObjectAnimator.INFINITE
            }
            recordingIndicator.visibility = View.VISIBLE
            blinkAnimation.start()
        } else {
            recordingIndicator.visibility = View.GONE
        }
    }
}


