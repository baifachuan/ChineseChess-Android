package com.fcbai.chess

import android.os.AsyncTask
import android.os.Handler
import android.os.Message
import android.util.Log
import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.*


object NetworkHelper {

    private const val sendTopicName = "receive_topic_name"
    private const val receiveTopicName = "send_topic_name"
    private const val serverURI = "tcp://10.0.2.2:1883"

    fun receive(mHandler: Handler, client: MqttAndroidClient) {
        try {
            val conOpt = MqttConnectOptions()
            conOpt.connectionTimeout = 3000
            conOpt.keepAliveInterval = 300
            client.setCallback(MQttCallbackHandler(mHandler))
            if (!client.isConnected) {
                client.connect(conOpt)
            }
            client.subscribe(receiveTopicName, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun send(data: String, client: MqttAndroidClient) {
        try {
            val conOpt = MqttConnectOptions()
            conOpt.connectionTimeout = 3000
            conOpt.keepAliveInterval = 300
            if (!client.isConnected) {
                client.connect(conOpt)
            }
            val msg = MqttMessage()
            msg.payload = data.toByteArray()
            msg.qos = 2
            msg.isRetained = false
            client.publish(sendTopicName, msg)
        } catch (e: MqttException) {
            e.printStackTrace()
        }

    }
}


class AIInvokeAsyncTask(mHandler: Handler, client: MqttAndroidClient): AsyncTask<Void, Void, String>() {
    private val mHandler = mHandler
    private val client = client
    override fun doInBackground(vararg params: Void?): String? {
        while (true) {
            NetworkHelper.receive(mHandler, client)
            if (StatusModel.stepMessage.step == Step.AI) {
                NetworkHelper.send(Klaxon().toJsonString(Model.notification2Action(StatusModel.stepMessage)), client)
                StatusModel.stepMessage = StatusModel.stepMessage.copy(step = Step.WATTING)
                Log.d("AI", "is robot")
            } else {
                Log.d("HUMAN", "is human")
            }

            Thread.sleep(3 * 1000)
        }
        return null
    }
}

class LoginAsyncTask(mHandler: Handler) : AsyncTask<Void, String, String>() {
    private val mHandler = mHandler

    private fun register(): User {
        val registerData = """{"user_name": "${UUID.randomUUID()}", "password": "fcbai"}"""
        val (request, response, result) = Fuel.post("http://10.0.2.2:5000/register")
            .body(registerData)
            .response()
        Log.d("response", String(response.data))
        return Gson().fromJson(String(response.data), User::class.java)
    }

    override fun doInBackground(vararg params: Void?): String? {
        val user = register()
        val bodyJson = """{"id": ${user.id}, "user_name": "${user.user_name}", "token": "${user.token}"}"""
        val (request, response, result) = Fuel.post("http://10.0.2.2:5000/start")
            .body(bodyJson)
            .response()
        Log.d("response", String(response.data))
        return String(response.data)
    }

    override fun onProgressUpdate(vararg values: String?) {
        super.onProgressUpdate(*values)
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        mHandler.sendEmptyMessage(GameActivity.GAME_INIT_SUCCESS)
    }
}

class MQttCallbackHandler(mHandler: Handler): MqttCallback {

    val mHandler = mHandler

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        Log.d("MQttCallbackHandler", "MQttCallbackHandler/messageArrived=$topic")
        message?.let {
//            val networkProtocol= Klaxon().parse<NetworkProtocol1>(String(it.payload))
            val networkProtocol= Gson().fromJson(String(it.payload), NetworkProtocol::class.java)

            StatusModel.stepMessage = StatusModel.stepMessage.copy(step = Step.HUMAN)
            val msg = Message()
            msg.what = GameActivity.AI_UPDATE
            msg.obj = Klaxon().toJsonString(Model.action2Notification(networkProtocol!!.action.toString()))
            mHandler.sendMessage(msg)
            Log.d("MQttCallbackHandler", "message=" + String(it.payload))
        }

    }

    override fun connectionLost(cause: Throwable?) {
        Log.d("MQttCallbackHandler", "MQttCallbackHandler/connectionLost")
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        Log.d("MQttCallbackHandler", "MQttCallbackHandler/deliveryComplete")
    }

}

class MQTTIMqttActionListener: IMqttActionListener {
    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
        Log.e("onFailure", "onFailure")
    }

    override fun onSuccess(asyncActionToken: IMqttToken?) {
        Log.e("onSuccess", "onSuccess")
    }

}