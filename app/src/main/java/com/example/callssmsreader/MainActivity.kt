package com.example.callssmsreader

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CallLog
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    val bools = mutableListOf<Boolean>()

    var readlog = false
    var readSms = false
    var readExternalStorage = false
    var writeExternalStorage = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_SMS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                0
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            startService(Intent(this, MonitorCallLogs::class.java))
        } else {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_LONG).show()
        }
    }

    private fun getCallDetails() {
        val sb = StringBuffer()
        val managedCursor = managedQuery(CallLog.Calls.CONTENT_URI, null, null, null, null)
        val number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER)
        val type = managedCursor.getColumnIndex(CallLog.Calls.TYPE)
        val date = managedCursor.getColumnIndex(CallLog.Calls.DATE)
        val duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION)
        sb.append("Call Log: ")
        while (managedCursor.moveToNext()) {
            val phoneNumber = managedCursor.getString(number)
            val callType = managedCursor.getString(type)
            val callDate = managedCursor.getString(date)
            val callDayTime = Date(callDate.toLong())
            val callDuration = managedCursor.getString(duration)
            var dir: String? = null
            val dircode: Int = callType.toInt()
            when (dircode) {
                CallLog.Calls.OUTGOING_TYPE -> dir = "OUTGOING"
                CallLog.Calls.INCOMING_TYPE -> dir = "INCOMING"
                CallLog.Calls.MISSED_TYPE -> dir = "MISSED"
            }
            sb.append("\nPhone Number:--- $phoneNumber \nCall Type:--- $dir \nCall Date:--- $callDayTime \nCall duration in sec :--- $callDuration");
            sb.append("\n----------------------------------");
        }
        myText.text = sb
    }

    private fun getSms() {
        val sb = StringBuffer()
        sb.append("Sms log: ")

        val uriSMSURI = Uri.parse("content://sms")
        val cursor = contentResolver.query(uriSMSURI, null, null, null, null)

        while (cursor != null && cursor.moveToNext()) {
            val address = cursor.getString(cursor.getColumnIndex("address"))
            val body = cursor.getString(cursor.getColumnIndexOrThrow("body"))
            val date = Date(cursor.getString(cursor.getColumnIndexOrThrow("date")).toLong())
            val type = when {
                cursor.getString(cursor.getColumnIndexOrThrow("type")).contains("1") -> {
                    "inbox"
                }
                else -> {
                    "sent"
                }
            }
            sb.append("\nNumber:--- $address \nMessage:--- $body \nDate:--- $date \nType:--- $type")
            sb.append("\n----------------------------------");

        }

        myText.text = sb

        cursor?.close()
    }
}
