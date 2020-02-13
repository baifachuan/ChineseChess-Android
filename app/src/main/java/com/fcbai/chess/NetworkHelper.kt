package com.fcbai.chess

import android.os.AsyncTask
import android.os.Handler
import android.os.Message
import android.util.Log
import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.Fuel
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence


object NetworkHelper {

    private const val sendTopicName = "receive_topic_name"
    private const val receiveTopicName = "send_topic_name"
    private const val serverURI = "tcp://10.0.2.2:1883"

    fun receive(mHandler: Handler) {
        try {
            val client =  MqttClient(serverURI, MqttAsyncClient.generateClientId(), MemoryPersistence())
            val conOpt = MqttConnectOptions()
            conOpt.connectionTimeout = 3000
            conOpt.keepAliveInterval = 300
            client.setCallback(MQttCallbackHandler())
            client.connect(conOpt)
            client.subscribe(receiveTopicName, 0)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun send(data: String) {
        try {
            val client =  MqttClient(serverURI, MqttAsyncClient.generateClientId(), MemoryPersistence())
            val conOpt = MqttConnectOptions();
            conOpt.connectionTimeout = 3000
            conOpt.keepAliveInterval = 300
            client.setCallback(MQttCallbackHandler())
            client.connect(conOpt)
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


class AIInvokeAsyncTask(mHandler: Handler): AsyncTask<Void, Void, String>() {
    val mHandler = mHandler
    override fun doInBackground(vararg params: Void?): String? {
//        NetworkHelper.receive(mHandler)
        while (true) {
            if (StatusModel.stepMessage.step == Step.ROBOT) {
                Log.d("ROBOT", "is robot")
                val myChessPieces = Model.getDefaultChessBoard()
                    .flatMap { f -> f.filter { f1 -> !f1.isDeath && f1.group == Group.BLACK } }
                val soldier05 = myChessPieces.first { f -> f.name == "soldier05" }
                val to = Model.getPosition(soldier05.position.biasX, soldier05.position.biasY + 0.1F)
                to?.let {
                    val msg = Message()
                    msg.what = GameActivity.AI_UPDATE
                    msg.obj = Klaxon().toJsonString(NotificationMessage(soldier05.position, it))
                    mHandler.sendMessage(msg)
                }

                StatusModel.stepMessage = StatusModel.stepMessage.copy(step = Step.HUMAN)
            } else {
                Log.d("HUMAN", "is human")
            }

            Thread.sleep(3 * 1000)
        }
        return null
    }
}

class LoginAsyncTask : AsyncTask<Void, Void, String>() {
    override fun doInBackground(vararg params: Void?): String? {
        val bodyJson = """{"id": ${StatusModel.gameInfo.id}}"""
        val (request, response, result) = Fuel.post("http://10.0.2.2:5000/start")
            .body(bodyJson)
            .response()
        Log.d("response", String(response.data))
        return null
    }
}

class MQttCallbackHandler: MqttCallback {
    override fun messageArrived(topic: String?, message: MqttMessage?) {
        Log.d("MQttCallbackHandler", "MQttCallbackHandler/messageArrived=$topic")
        if (message != null) {
            Log.d("MQttCallbackHandler", "message=" + String(message.getPayload()))
        }
    }

    override fun connectionLost(cause: Throwable?) {
        Log.d("MQttCallbackHandler", "MQttCallbackHandler/connectionLost")
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        Log.d("MQttCallbackHandler", "MQttCallbackHandler/deliveryComplete")
    }

}