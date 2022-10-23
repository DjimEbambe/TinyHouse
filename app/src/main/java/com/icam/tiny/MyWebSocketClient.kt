package com.icam.tiny

import android.app.Activity
import android.util.Log
import android.widget.TextView
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI


/**
 * Created by naoikotaro on 2018/03/27.
 */
class MyWebSocketClient(val activity: Activity, uri: URI) : WebSocketClient(uri) {


    private val breakLine = System.lineSeparator()


    override fun onOpen(handshakedata: ServerHandshake?) {
        Log.i(javaClass.simpleName, "Connected to WS server.")
        Log.i(javaClass.simpleName, "Thread:\"${Thread.currentThread().name} \"Running on")

    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Log.i(javaClass.simpleName, "Disconnected from WS server. reason:${reason}")
        Log.i(javaClass.simpleName, "Thread:${Thread.currentThread().name}Running on")
    }

    override fun onMessage(message: String?) {
        Log.i(javaClass.simpleName, "Message received.")
        Log.i(javaClass.simpleName, "Thread: \"${Thread.currentThread().name} \"Running with")
        activity.runOnUiThread {

            Log.i(javaClass.simpleName, "Message added to TextView.")
            Log.i(javaClass.simpleName, "Thread: \"${Thread.currentThread().name} \"Running on")

        }
    }

    override fun onError(ex: Exception?) {
        Log.i(javaClass.simpleName, "An error has occurred.", ex)
        Log.i(javaClass.simpleName, "Thread: \"${Thread.currentThread().name} \"Running on")
    }
}