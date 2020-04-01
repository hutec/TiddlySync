package com.example.tiddlysync

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private val url:String = ""
    private val credentials:String = ""
    private var queue:RequestQueue? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        queue = Volley.newRequestQueue(this)

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
        // Read content of existing Tiddler and append the received content
        val receivedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        txtMain.text = receivedText
        updateTiddler(receivedText)
    }

    private fun updateTiddler(newText: String) {
        val request = JsonObjectRequestBasicAuth(Request.Method.GET, url, null,
            Response.Listener{ response->
                try {
                    // Parse the json object here
                    val oldText = response.get("text").toString()
                    txtMain.text = "Response : $response"
                    writeTiddler(oldText, newText)

                }catch (e:Exception){
                    e.printStackTrace()
                    txtMain.text = "Parse exception : $e"
                }
            }, Response.ErrorListener{
                txtMain.text = "Volley error: $it"
            }, credentials
        )

        queue?.add(request)
    }

    private fun writeTiddler(oldtext: String, newText: String) {
        val jsonObj = JSONObject()
        jsonObj.put("title", "AndroidTiddler")
        jsonObj.put("text", "* %s \n %s".format(newText, oldtext))

        val request = JsonObjectRequestBasicAuth(Request.Method.PUT, url, jsonObj,
            Response.Listener{ response->
                try {
                    txtMain.text = "Response : $response"
                }catch (e:Exception){
                    e.printStackTrace()
                    txtMain.text = "Parse exception : $e"
                }
            }, Response.ErrorListener{
                txtMain.text = "Volley error: $it"
            }, credentials
        )
        queue?.add(request)
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
