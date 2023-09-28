package com.example.conexionbluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.core.app.ActivityCompat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    var btnEscuchar: Button? = null
    var btnListar: Button? = null
    var btnEnviar: Button? = null
    var listView: ListView? = null
    var txtMensaje: TextView? = null
    var txtStatus: TextView? = null
    var txtMsg: EditText? = null
    var socketAux: BluetoothServerSocket? = null
    var SocketCliente: BluetoothSocket? = null
    private var myBluetoothAdapter: BluetoothAdapter? = null
    lateinit var n_pairedDevice: Set<BluetoothDevice>
    val APP_NAME = "BTChat"
    var myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00B05F9B34FB")
    var conectarCliente: SocketClienteBluetooth? = null
    var REQUEST_ENABLE_BLUETOOTH = 1
    private val PERMISSIONS_REQUEST_CODE = 1001


    var servidor: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        relacionarObjetos()

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (!hasAllPermissions()) {
            requestNeededPermissions()
        }


        if (ActivityCompat.checkSelfPermission(
                this,
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
            return
        }
        socketAux = myBluetoothAdapter!!.listenUsingRfcommWithServiceRecord(APP_NAME, myUUID)

        //Habilitar bluetooth
        if (!myBluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
            Toast.makeText(this, "Bluetooth Habilitado", Toast.LENGTH_LONG).show()
        }

        implementarListeners()

    }

    private fun implementarListeners() {
        btnListar!!.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
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
            n_pairedDevice = myBluetoothAdapter!!.bondedDevices
            val list: ArrayList<BluetoothDevice> = ArrayList()

            if (!n_pairedDevice.isEmpty()) {
                for (device: BluetoothDevice in n_pairedDevice) {
                    list.add(device)
                    Log.i("device ", "" + device)
                }
            } else {
                Toast.makeText(this, "no paired bluetooth device found", Toast.LENGTH_LONG).show()

            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)

            listView!!.adapter = adapter
            listView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                val device: BluetoothDevice = list[position]
                SocketCliente = device.createRfcommSocketToServiceRecord(myUUID)

                conectarCliente =
                    SocketClienteBluetooth(this, txtStatus!!, device, SocketCliente!!, txtMensaje!!)
                conectarCliente!!.execute()

                txtStatus!!.text = "Conectando..."
            }

        }
        var escuchar =SocketServidorBluetooth(this, txtStatus!!,myBluetoothAdapter!!,socketAux!!,txtMsg!!,txtMensaje!!)

        btnEscuchar!!.setOnClickListener {

            escuchar.execute()
            servidor = true
        }
        btnEnviar!!.setOnClickListener {
            if(servidor){
                escuchar!!.enviar(txtMsg!!.text.toString())
            }else{
                conectarCliente!!.enviar(txtMsg!!.text.toString())
            }
        }
    }


    private fun relacionarObjetos() {
        btnEscuchar=findViewById(R.id.btnEscuchar)
        btnListar=findViewById(R.id.btnListaDispositivos)
        btnEnviar=findViewById(R.id.btnEnviar)
        listView=findViewById(R.id.IsView)
        txtMensaje=findViewById(R.id.txtMensaje)
        txtStatus=findViewById(R.id.txtStatus)
        txtMsg=findViewById(R.id.txtMsg)
    }

    private fun hasAllPermissions(): Boolean {
        return checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestNeededPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSIONS_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Todos los permisos han sido concedidos
                    // Puedes continuar con las operaciones de Bluetooth
                } else {
                    // Al menos un permiso ha sido denegado
                    Toast.makeText(this, "Todos los permisos son necesarios para usar Bluetooth", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


}