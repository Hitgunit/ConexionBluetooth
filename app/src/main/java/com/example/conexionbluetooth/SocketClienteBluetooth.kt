package com.example.conexionbluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Message
import android.widget.TextView
import androidx.core.app.ActivityCompat
import java.io.IOException

class SocketClienteBluetooth(var context:Context,
                             var tvStatus:TextView,
                             var dispositivo:BluetoothDevice,
                             var socketCliente:BluetoothSocket,
                             var mensaje:TextView
):AsyncTask<String,Int,String>() {

    val STATE_LISTENING=1
    val STATE_CONNECTING=2
    val STATE_CONNECTED=3
    val STATE_CONNECTION_FAILED=4
    val STATE_MESSAGE_RECEIVED=5

    //Variable para el mensaje
    var buffer: String?=null

    var btSocketCliente:BluetoothSocket?=null

    val movimientoLocal = (context as MainActivity).eleccionJugador


    override fun onPreExecute() {
        try {
            btSocketCliente=socketCliente
        }catch (e:IOException){
            e.printStackTrace()
        }
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: String?): String {
        var mensaje:Message?=null
        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }
            btSocketCliente!!.connect()

            mensaje=Message.obtain()//obtener el mensaje cuando intente conectar con el  cliente
            mensaje.what=STATE_CONNECTED
            publishProgress(mensaje.what)

            //Iniciar Enviar y Recibir

            while (btSocketCliente!=null){
                try {
                    var a:Char?=null //Almacena letra por letra en ASCII y despues en alfabeto
                    a=btSocketCliente!!.inputStream.read().toChar()

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

        }catch (e:IOException){
            e.printStackTrace()

            mensaje=Message.obtain()
            mensaje.what=STATE_CONNECTION_FAILED
            publishProgress(mensaje.what)
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
            val movimientoRemoto = buffer!!.toString()
            if (movimientoRemoto in listOf("piedra", "papel", "tijera")) {
                val resultado = (context as MainActivity).determinarGanador(movimientoLocal, movimientoRemoto)
                mensaje.text = resultado.toString()
            } else {
                mensaje.text = movimientoRemoto
            }
        }


        super.onProgressUpdate(*values)
    }

    //Metodo enviar

    public fun enviar(n:String){
        var r="!$n"
        btSocketCliente!!.outputStream.write(r.toByteArray())
    }

}