package com.example.mikki.paypal

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.paypal.android.sdk.payments.*
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import java.math.BigDecimal


class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



    }

    fun displayResult(result:String){
        tv_receipt.text = result
        //toast(result)
        Toast.makeText(
                applicationContext,
                result, Toast.LENGTH_LONG).show()

    }

    fun onBuyPress(view: View){
        val thingToBuy = getThingToBuy(PayPalPayment.PAYMENT_INTENT_SALE)

        /*
         * See getStuffToBuy(..) for examples of some available payment options.
         */

        val intent = Intent(this@MainActivity,
                PaymentActivity::class.java)

        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config)

        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy)

        startActivityForResult(intent, REQUEST_CODE_PAYMENT)
    }


    private  fun getThingToBuy(paymentIntent: String): PayPalPayment{

        return PayPalPayment(BigDecimal("200"), "USD"
                , "Name of Item", paymentIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                val confirm = data?.getParcelableExtra<PaymentConfirmation>(PaymentActivity.EXTRA_RESULT_CONFIRMATION)
                if (confirm != null) {
                    try {
                        Log.i(TAG, confirm.toJSONObject().toString(4))
                        Log.i(TAG, confirm.payment.toJSONObject().toString(4))
                        /**
                         * TODO: send 'confirm' (and possibly confirm.getPayment() to your server for verification
                         * or consent completion.
                         * See https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                         * for more details.
                         * For sample mobile backend interactions, see
                         * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
                         */
                        displayResult("PaymentConfirmation info received from PayPal")


                    } catch (e: JSONException) {
                        Log.e(TAG, "an extremely unlikely failure occurred: ", e)
                    }

                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "The user canceled.")
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(
                        TAG,
                        "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.")
            }
        }
    }

    /**
     * companion obj for use of singleton classes
     */
    companion object {
        private val TAG = "PAYPAL"
        private val REQUEST_CODE_PAYMENT = 1

        //no_network == no need for client ID
        private val CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_NO_NETWORK
        //private val CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX
        // note that these credentials will differ between live & sandbox environments.
        private val CONFIG_CLIENT_ID = "credentials from developer.paypal.com"

        private val config = PayPalConfiguration()
                .environment(CONFIG_ENVIRONMENT)
                .clientId(CONFIG_CLIENT_ID)
                .merchantName("Example Merchant")
                .merchantPrivacyPolicyUri(Uri.parse("https://www.example.com/privacy"))
                .merchantUserAgreementUri(Uri.parse("https://www.example.com/legal"))



    }

    public override fun onDestroy() {
        // Stop service when done
        stopService(Intent(this, PayPalService::class.java))
        super.onDestroy()
    }

}


