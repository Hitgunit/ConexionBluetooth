package com.example.conexionbluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.AsyncTask
import android.os.Message
import android.widget.TextView
import java.io.IOException

class SocketServidorBluetooth(var context: Context,
                              var tvStatus:TextView,
                              var bluetoothAdapter: BluetoothAdapter,
                              var socketSer:BluetoothServerSocket,
                              var txtMSG:TextView,
                              var txtMensaje:TextView
):AsyncTask<String,Int,String>() {

    val STATE_LISTENING=1
    val STATE_CONNECTING=2
    val STATE_CONNECTED=3
    val STATE_CONNECTION_FAILED=4
    val STATE_MESSAGE_RECEIVED=5

    //Variable para el mensaje
    var buffer: String?=null


    var btServerSocket:BluetoothServerSocket?=null
    public var socket:BluetoothSocket?=null

    override fun onPreExecute() {

        try {
            btServerSocket=socketSer
        }catch (e:IOException){
            e.printStackTrace()
        }

        super.onPreExecute()
    }

    override fun doInBackground(vararg params: String?): String {
        var mensaje: Message

        while (socket==null){
            try {
                mensaje= Message.obtain()//obtener el mensaje cuando intente conectar con el  cliente
                mensaje.what=STATE_CONNECTING
                publishProgress(mensaje.what)
                socket=btServerSocket!!.accept()
            }catch (e:IOException){
                e.printStackTrace()

                mensaje= Message.obtain()
                mensaje.what=STATE_CONNECTION_FAILED
                publishProgress(mensaje.what)
            }

            if(socket!=null){
                mensaje=Message.obtain()
                mensaje.what=STATE_CONNECTED
                publishProgress(mensaje.what)

                //Iniciar Enviar y Recibir

                while (socket!=null){
                    try {
                        var a:Char?=null //Almacena letra por letra en ASCII y despues en alfabeto
                        a=socket!!.inputStream.read().toChar()

                        if (a=='!'){
                            buffer=""
                        }else{
                            Thread.sleep(3)
                            buffer="$buffer$a"
                            publishProgress(0)
                        }
                    }catch (e:IOException){
                        e.printStackTrace()
                    }
                }
            }
        }

        return ""
    }

    override fun onProgressUpdate(vararg values: Int?) {

        when(values[0]){
            STATE_LISTENING -> tvStatus.text="Esperando conexion"
            STATE_CONNECTING -> tvStatus.text="Conectando..."
            STATE_CONNECTED -> tvStatus.text="Conectado"
            STATE_CONNECTION_FAILED -> tvStatus.text="Conexion fallida"
            STATE_MESSAGE_RECEIVED -> tvStatus.text="..."
        }

        if(values[0]==0){
            txtMensaje.text=buffer!!.toString()
        }

        super.onProgressUpdate(*values)
    }

    //Metodo enviar

    public fun enviar(n:String){
        var r="!$n"
        socket!!.outputStream.write(r.toByteArray())
    }

}
