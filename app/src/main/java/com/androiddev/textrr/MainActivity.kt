package com.androiddev.textrr

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import java.util.*


class MainActivity : AppCompatActivity(),TextToSpeech.OnInitListener {
    lateinit var imageView: ImageView
    lateinit var editText: EditText
    lateinit var captureImage : Button
    lateinit var detectText : Button
    lateinit var buttonSpeak : Button
    lateinit var tts : TextToSpeech

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.imageView)
        editText = findViewById(R.id.editText)
        captureImage = findViewById(R.id.captureImage)
        detectText = findViewById(R.id.detectText)
        buttonSpeak = findViewById(R.id.buttonSpeak)
        captureImage.setOnClickListener(){
            selectImage()
        }
        detectText.setOnClickListener(){
            startRecognizing(imageView)
        }

        buttonSpeak.isEnabled = false
        tts = TextToSpeech(this,this)
        buttonSpeak.setOnClickListener{speakOut()}
    }


        private fun selectImage() {
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, 1)
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 ) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
        }
    }

    fun startRecognizing(v: View) {
        if (imageView.drawable != null) {
            editText.setText("")
            v.isEnabled = false
            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
            val image = InputImage.fromBitmap(bitmap, 0)
            val detector = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            detector.process(image).addOnSuccessListener { firebaseVisionText ->
                v.isEnabled = true
                processResultText(firebaseVisionText)
            }
                .addOnFailureListener {
                    v.isEnabled = true
                    editText.setText("Failed")
                }
        } else {
            Toast.makeText(this, "Select an Image", Toast.LENGTH_SHORT).show()

        }
    }
    private fun Any.processResultText(detector: Text) {
        val detectorText = detector.text
        if(detector.textBlocks.size == 0){
            editText.setText("No text found")
            return
        }
        for(block in detector.textBlocks){
            val blockText = block.text
            editText.append(blockText + "\n")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.UK)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language Detected is not supported")
            } else {
                buttonSpeak.isEnabled = true
            }
        } else {
            Log.e("TTS", "Initialization Failed!")
        }
    }

    private fun speakOut(){
        val text = editText.text.toString().trim()

        if (text.isNotEmpty()) {
                // Lollipop and above requires an additional ID to be passed.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Call Lollipop+ function
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
                } else {
                    // Call Legacy function
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null)
                }
            } else {
                Toast.makeText(this, "Text cannot be empty", Toast.LENGTH_LONG).show()
            }
    }

    public override fun onDestroy() {
        if(tts != null){
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

    override fun onPause() {
        tts.stop()
        super.onPause()
    }
}