package com.example.spotiflag

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.example.spotiflag.model.Root
import com.example.spotiflag.model.userData.ImageInfo
import com.example.spotiflag.model.userData.UserData
import de.hdodenhof.circleimageview.CircleImageView
import com.fasterxml.jackson.databind.ObjectMapper
import com.squareup.picasso.Picasso
import okhttp3.*
import java.io.File
import java.io.IOException

class ShowcaseActivity : AppCompatActivity() {
    private var data: Root? = null
    private var popularity = 0
    private val avatar: CircleImageView by lazy {
        findViewById(R.id.avatar)
    }
    private val levelText: TextView by lazy {
        findViewById(R.id.level_text)
    }
    private val reachedText: TextView by lazy {
        findViewById(R.id.reachedText)
    }
    private val levelBar: ProgressBar by lazy {
        findViewById(R.id.levelBar)
    }
    private val logoutButton: Button by lazy {
        findViewById(R.id.logout_button)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_showcase)
    }

    override fun onStart() {
        super.onStart()

        try {
            data = rootmapDataIntoFile()
            popularity = data!!.computeAverage()

            if (popularity == 0) {
                popularity = 1
            } else {
                if (popularity <= 20) {
                    levelText.setText(R.string.level_1);
                }
                if (popularity in 20..39) {
                    levelText.setText(R.string.level_2);
                }

                if (popularity in 40..59) {
                    levelText.setText(R.string.level_3);
                }

                if (popularity in 60..79) {
                    levelText.setText(R.string.level_4);
                }

                if (popularity in 80..100) {
                    levelText.setText(R.string.level_5);
                }
            }

            reachedText.text = "You've reached $popularity% popularity !"
            levelBar.progress = popularity

        } catch (e: Error) { throw Error(e) }

        loadUserInfo()

        logoutButton.setOnClickListener {
            val dir = filesDir.absolutePath
            val file = File("$dir/dataInfo.txt")

            var deletedDataInfo = false
            if (file.exists()) {
                deletedDataInfo = file.delete()
            }

            if (getSharedPreferences("Spotiflag", MODE_PRIVATE).contains("AddDate")
                && getSharedPreferences("Spotiflag", MODE_PRIVATE).contains("Token")) {
                getSharedPreferences("Spotiflag", MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()
            }

            if (deletedDataInfo) {
                val newActivity = Intent(this@ShowcaseActivity, HomeActivity::class.java)
                startActivity(newActivity)
                finish()
            }
        }
    }

    private fun rootmapDataIntoFile(): Root {
        val mapper = ObjectMapper()
        return mapper.readValue(File(filesDir, "dataInfo.txt"), Root::class.java)
    }

    private fun userMapDataInfoResponse(content: String): UserData {
        val mapper = ObjectMapper()
        return mapper.readValue(content, UserData::class.java)
    }

    private fun loadUserInfo() {
        sendRequest("https://api.spotify.com/v1/me")
    }

    private fun sendRequest(url: String) {
        val token = getSharedPreferences("Spotiflag", MODE_PRIVATE)
            .getString("Token", null)
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .build()

        client
            .newCall(request)
            .enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    throw Error(e.message)
                }

                override fun onResponse(call: Call, response: Response) {
                        var images: ArrayList<ImageInfo>?
                        try {
                            images = userMapDataInfoResponse(
                                response.body()!!.string()
                            ).images
                        } catch (e: Error) { throw Error(e) }

                        if (images != null && images.size < 1) {
                            data?.avatarUrl = "https://www.babelio.com/users/AVT_Linus-Torvald_1898.jpeg"
                        }
                        else data?.avatarUrl = images?.get(0)?.url

                        val handler = Handler(Looper.getMainLooper())
                        handler.post {
                            val result = Picasso.get().load(data?.avatarUrl)
                            result.into(avatar)
                        }
                    }
            })
    }
}