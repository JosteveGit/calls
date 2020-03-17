package com.example.callssmsreader

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.provider.CallLog.Calls
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

class MonitorCallLogs : Service() {

    override fun onStart(intent: Intent?, startId: Int) {

        val handler = Handler()
        val callLog = mutableListOf<StringBuffer?>(StringBuffer())
        val smsLog = mutableListOf<StringBuffer?>(StringBuffer())
        val runnable = object : Runnable{
            override fun run() {
                if(callLog[0]!=getCallDetails()){
                    val folder = File(
                        Environment.getExternalStorageDirectory().toString() +
                            File.separator + "Josteve/Calls")
                    if (!folder.exists()) {
                        val made = folder.mkdirs()
                        Log.d("Made", "$made")
                    }

                    var file: File? = null
                    file = File(Environment.getExternalStorageDirectory().toString()+ File.separator+"Josteve/Calls"+File.separator+"Calls.txt")
                    val data = getCallDetails().toString().toByteArray()

                    file.createNewFile()
                    val fo: OutputStream = FileOutputStream(file)
                    fo.write(data)
                    fo.close()

                    callLog[0] = getCallDetails()
                }
                if(smsLog[0] != getSms()){
                    val folder = File(
                        Environment.getExternalStorageDirectory().toString() +
                                File.separator + "Josteve/SMS")
                    if (!folder.exists()) {
                        val made = folder.mkdirs()
                        Log.d("Made", "$made")
                    }
                    var file: File? = null
                    file = File(Environment.getExternalStorageDirectory().toString()+ File.separator+"Josteve/SMS"+File.separator+"Sms.txt")
                    val data = getSms().toString().toByteArray()

                    file.createNewFile()
                    val fo: OutputStream = FileOutputStream(file)
                    fo.write(data)
                    fo.close()

                    smsLog[0] = getSms()
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }



    private fun getSms(): StringBuffer?{
        val sb = StringBuffer()
        sb.append("Sms log: ")

        val uriSMSURI = Uri.parse("content://sms")
        val cursor = contentResolver.query(uriSMSURI, null, null, null, null)

        while (cursor!=null && cursor.moveToNext()){
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
        cursor?.close()

        return sb
    }


    private fun getCallDetails(): StringBuffer?{
        val sb = StringBuffer()
         if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_DENIED
        ) {
             Log.d("Permission","Granted")
             val managedCursor = applicationContext.contentResolver.query(Calls.CONTENT_URI, null,null,null,null)
             val number = managedCursor!!.getColumnIndex(Calls.NUMBER)
             val type = managedCursor.getColumnIndex(Calls.TYPE)
             val date = managedCursor.getColumnIndex(Calls.DATE)
             val duration = managedCursor.getColumnIndex(Calls.DURATION)
             sb.append("Call Log: ")
             while (managedCursor.moveToNext()){
                 val phoneNumber = managedCursor.getString(number)
                 val callType = managedCursor.getString(type)
                 val callDate = managedCursor.getString(date)
                 val callDayTime = Date(callDate.toLong() as Long)
                 val callDuration = managedCursor.getString(duration)
                 var dir : String?= null
                 val dircode: Int = callType.toInt()
                 when(dircode){
                     Calls.OUTGOING_TYPE -> dir = "OUTGOING"
                     Calls.INCOMING_TYPE -> dir = "INCOMING"
                     Calls.MISSED_TYPE -> dir = "MISSED"
                 }
                 sb.append("\nPhone Number:--- $phoneNumber \nCall Type:--- $dir \nCall Date:--- $callDayTime \nCall duration in sec :--- $callDuration");
                 sb.append("\n----------------------------------");
             }

             return sb
        }

        Log.d("Permission","Denied")

        return null

    }


    override fun onBind(intent: Intent?): IBinder? = null
}
