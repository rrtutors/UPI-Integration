package com.rrtutors.upiintegration

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import android.net.ConnectivityManager
import android.util.Log


class MainActivity : AppCompatActivity() {
val  UPI_PAYMENT=100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var amount_ed=findViewById<EditText>(R.id.amount_et)
        var upi_idEd=findViewById<EditText>(R.id.upi_id)
        var nameEd=findViewById<EditText>(R.id.name)
        var noteEd=findViewById<EditText>(R.id.note)
        var send=findViewById<Button>(R.id.send)
        send.setOnClickListener(View.OnClickListener {

            var amount=amount_ed.text.toString().trim();
            var id=upi_idEd.text.toString().trim();
            var name=nameEd.text.toString().trim();
            var note=noteEd.text.toString().trim();

            if(amount.length>0) {
                var uri = Uri.parse("upi://pay").buildUpon()
                    .appendQueryParameter("pa", id)
                    .appendQueryParameter("pn", name)
                    .appendQueryParameter("tn", note)
                    .appendQueryParameter("am", amount)
                    .appendQueryParameter("cu", "INR")
                    .build();

                var intent = Intent(Intent.ACTION_VIEW);
                intent.data = uri
                var intentChooser = Intent.createChooser(intent, "Pay with")
                if (null != intentChooser.resolveActivity(getPackageManager())) {
                    startActivityForResult(intentChooser, UPI_PAYMENT);
                } else {
                    Toast.makeText(
                        MainActivity@ this,
                        "No UPI app found, please install one to continue",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            }else
            {
                Toast.makeText(MainActivity@this,"Please enter amount",Toast.LENGTH_SHORT).show();

            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode)
        {
            UPI_PAYMENT->{
                if(resultCode== Activity.RESULT_OK||resultCode==1)
                {
                    var list:ArrayList<String>
                    if(data!=null)
                    {
                        list= ArrayList()
                        data.getStringExtra("resposne")?.let { list.add(it) }

                    }else
                    {
                        list= ArrayList()
                        list.add("No Reponse")
                    }
                }else
                {
                    var list:ArrayList<String>
                    list= ArrayList()
                    list.add("Error")

                    checkPaymentStatus(list)
                }
            }
        }
    }

    private fun checkPaymentStatus(list: java.util.ArrayList<String>) {

        if(isConnectionAvailable(applicationContext))
        {
            var str=list.get(0);
            Log.d("UPIPAY", "upiPaymentDataOperation: "+str);
            if(str==null)
                str="discard"
            var status: String? =null;
            var txtnNo:String? =null;
            var paymentCancel:String? =null;

            var response=str.split("&")
            for (i in 0 until response.size) {
                val equalStr = response[i].split("=")
                if (equalStr.size >= 2) {
                    if (equalStr[0].toLowerCase() == "Status".toLowerCase()) {
                        status = equalStr[1].toLowerCase()
                    } else if (equalStr[0].toLowerCase() == "ApprovalRefNo".toLowerCase() || equalStr[0].toLowerCase() == "txnRef".toLowerCase()) {
                        txtnNo = equalStr[1]
                    }
                } else {
                    paymentCancel = "Payment cancelled by user."
                }
            }
            if (status.equals("success")) {
                //Code to handle successful transaction here.
                Toast.makeText(MainActivity@this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                Log.d("UPI", "responseStr: "+txtnNo);
            }
            else if("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(MainActivity@this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MainActivity@this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
            }

        }else
        {
            Toast.makeText(MainActivity@this,"Please check your network",Toast.LENGTH_SHORT).show();

        }

    }

    fun isConnectionAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val netInfo = connectivityManager.activeNetworkInfo
            if (netInfo != null && netInfo.isConnected
                && netInfo.isConnectedOrConnecting
                && netInfo.isAvailable
            ) {
                return true
            }
        }
        return false
    }
}
