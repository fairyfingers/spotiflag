package com.example.spotiflag

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.example.spotiflag.consts.APP_CLIENT_ID
import com.example.spotiflag.consts.REDIRECT_URI
import com.example.spotiflag.consts.REQUEST_CODE
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import okhttp3.*
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class HomeActivity : AppCompatActivity() {
    private val signInButton: Button by lazy {
        findViewById(R.id.connect_button)
    }
    private val loadingCircle: ProgressBar by lazy {
        findViewById(R.id.progress_bar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_home)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        val dateFromPrefs = getSharedPreferences("Spotiflag", MODE_PRIVATE)
            .getString("AddDate", null)
        val tokenFromPrefs = getSharedPreferences("Spotiflag", MODE_PRIVATE)
            .getString("Token", null)

        if (dateFromPrefs != null && tokenFromPrefs != null) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE)
                val now = sdf.parse(LocalDate.now().plusDays(1).toString())
                val parsedDateFromPrefs = sdf.parse(dateFromPrefs)

                if (parsedDateFromPrefs!! < now) {
                    sendRequest(
                        "https://api.spotify.com/v1/me",
                        object: TreatResponse {
                            override fun treatResponse(response: Response) {
                                val newActivity = if (response.code() == 400) {
                                    Intent(this@HomeActivity, ErrorActivity::class.java)
                                } else {
                                    Intent(this@HomeActivity, ShowcaseActivity::class.java)
                                }
                                startActivity(newActivity)
                                finish()
                            }
                        }
                    )
                }
            } catch (e: Error) { throw Error(e) }
        }

        signInButton.setOnClickListener {
            loadingCircle.isVisible = true
            signInButton.isEnabled = false
            signInButton.text = resources.getText(
                R.string.button_state_being_connected
            )
            startSignInProcess()
        }
    }

    override fun onStop() {
        super.onStop()
        getSharedPreferences("Spotiflag", MODE_PRIVATE).edit().clear().apply()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != REQUEST_CODE) throw Error("Request codes don't match")
        val response = AuthorizationClient.getResponse(resultCode, data)

        when (response.type) {
            AuthorizationResponse.Type.TOKEN -> {
                println("obtained token")
                val date = LocalDate.now()
                getSharedPreferences("Spotiflag", MODE_PRIVATE)
                    .edit()
                    .putString("Token", response.accessToken)
                    .putInt("Expires", response.expiresIn)
                    .putString("AddDate", date.toString())
                    .apply()

                sendRequest(
                    "https://api.spotify.com/v1/me/player/recently-played?limit=50",
                    object: TreatResponse {
                        override fun treatResponse(response: Response) {
                            val body = response.body()?.string() ?: throw Error("Retrieved body is null")
                            writeFile(body, "dataInfo.txt")

                            val newActivity = Intent(this@HomeActivity, ShowcaseActivity::class.java)
                            startActivity(newActivity)
                            finish()
                        }
                    }
                )
            }
            AuthorizationResponse.Type.ERROR -> throw Error(response.error)
            else -> throw Error("Most likely auth flow was cancelled")
        }
    }

    /**
     * Starts Spotify SDK's embedded sign in process.
     */
    private fun startSignInProcess() {
        println("entered sign in process")

        val builder = AuthorizationRequest
            .Builder(
                APP_CLIENT_ID,
                AuthorizationResponse.Type.TOKEN,
                REDIRECT_URI
            )
        builder.setScopes(
            arrayOf(
                "user-read-email",
                "user-read-recently-played",
                "playlist-read-private"
            )
        )

        AuthorizationClient
            .openLoginActivity(
                this,
                REQUEST_CODE,
                builder.build()
            )
    }

    /**
     * Sends an HTTP request using OKHttp3 package.
     */
    private fun sendRequest(url: String, callback: TreatResponse) {
        val token = this.getSharedPreferences("Spotiflag", Context.MODE_PRIVATE)
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
                    callback.treatResponse(response)
                }
            })
    }

    /**
     * OKHttp3 response treatment contract.
     */
    private interface TreatResponse {
        fun treatResponse(response: Response)
    }

    /**
     * Writes a file containing all received data from Spotify API.
     */
    private fun writeFile(body: String, filename: String) {
        val file = File(this.filesDir, filename)
        val fileWriter = FileWriter(file)
        val printWriter = PrintWriter(fileWriter)
        printWriter.print(body)
        fileWriter.close()
    }
}