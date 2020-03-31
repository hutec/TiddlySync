package com.example.tiddlysync

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        when {
            intent?.action == Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    handleSendText(intent) // Handle text being sent
                }
            }
            else -> {
                // Handle other intents, such as being started from the home screen
            }
        }
    }

    private fun handleSendText(intent: Intent) {
        val receivedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        txtMain.text = receivedText

        val url = ""
        val credentials = ""

        val queue = Volley.newRequestQueue(this)

        // New tiddler
        val jsonObj = JSONObject()
        jsonObj.put("title", "AndroidTiddler")
        jsonObj.put("text", receivedText)
        txtMain.text = "$jsonObj"

        // API call
        val request = JsonObjectRequestBasicAuth(Request.Method.PUT, url, jsonObj,
            Response.Listener{ response->
                try {
                    // Parse the json object here
                    txtMain.text = "Response : $response"
                }catch (e:Exception){
                    e.printStackTrace()
                    txtMain.text = "Parse exception : $e"
                }
            }, Response.ErrorListener{
                txtMain.text = "Volley error: $it"
            }, credentials
        )
        queue.add(request)
    }
}

class JsonObjectRequestBasicAuth(
    method: Int,
    url: String,
    jsonObject: JSONObject?,
    listener: Response.Listener<JSONObject>,
    errorListener: Response.ErrorListener,
    credentials: String
) : JsonObjectRequest(method,url, jsonObject, listener, errorListener) {

    private var mCredentials:String = credentials

    @Throws(AuthFailureError::class)
    override fun getHeaders(): Map<String, String> {
        val headers = HashMap<String, String>()

        val auth = "Basic " + Base64.encodeToString(mCredentials.toByteArray(), Base64.NO_WRAP)
        headers["Host"] = ""
        headers["Referer"] = ""
        headers["X-Requested-With"] =  "TiddlyWiki"
        headers["Origin"] = ""
        headers["Content-type"] = "application/json"
        headers["Authorization"] = auth

        return headers
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<JSONObject> {
        // 204 is expected, return empty JSON object
        if (response.statusCode == 204) {
            return Response.success(JSONObject(), HttpHeaderParser.parseCacheHeaders(response))
        }
        return super.parseNetworkResponse(response)
    }
}
