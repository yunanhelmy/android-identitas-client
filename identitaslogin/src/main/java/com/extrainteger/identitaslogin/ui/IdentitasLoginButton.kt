package com.extrainteger.identitaslogin.ui

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.util.Log
import android.widget.Button
import android.util.TypedValue
import android.view.View
import com.extrainteger.identitaslogin.*
import com.extrainteger.identitaslogin.models.AuthToken
import android.util.DisplayMetrics
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.widget.Toast
import com.extrainteger.identitaslogin.utils.ConnectionState


/**
 * Created by ali on 04/12/17.
 */
class IdentitasLoginButton: Button{
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int): super(context, attrs, defStyle)

    private var config: IdentitasConfig? = null
    private var mCallback: Callback<AuthToken>? = null
    private val TAG = "Login Button"

    init {
        setupButton()
    }

    private fun setupButton() {
        setPadding(getSizeInDp(20f), 0,
                getSizeInDp(20f), 0)
        setOnClickListener(LoginClickListener())
    }

    private fun getSizeInDp(value: Float): Int{
        val r = resources
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, r.displayMetrics))
    }

    private inner class LoginClickListener : OnClickListener {
        override fun onClick(v: View?) {
            gotoOAuthActivity()
        }
    }

    private fun gotoOAuthActivity(){
        if (config!=null) {
            if (config?.activity!=null){
                if (config?.CLIENT_ID!="" && config?.CLIENT_SCRET!="" && config?.REDIRECT_URI!=""){
                    if (config?.CLIENT_ID!=null && config?.CLIENT_SCRET!=null && config?.REDIRECT_URI!=null){
                        if (ConnectionState(config?.activity).isConnected()){
                            val intent = Intent(config?.activity, OauthActivity::class.java)
                            intent.putExtra(IdentitasConstants.BASE_URL_FIELD, config?.BASE_URL)
                            intent.putExtra(IdentitasConstants.CLIENT_ID_FIELD, config?.CLIENT_ID)
                            intent.putExtra(IdentitasConstants.CLIENT_SECRET_FIELD, config?.CLIENT_SCRET)
                            intent.putExtra(IdentitasConstants.REDIRECT_URI_FIELD, config?.REDIRECT_URI)
                            intent.putExtra(IdentitasConstants.SCOPE_FIELD, getScope(config?.SCOPES))
                            intent.putExtra(IdentitasConstants.REFERER_FIELD, config?.REFERER)
                            config?.activity?.startActivityForResult(intent, IdentitasConstants.LOGIN_ACTIVITY_REQUEST_CODE)
                        }
                        else{
                            Toast.makeText(context, "Please check your internet connection", Toast.LENGTH_SHORT).show()
                            Log.e(TAG, "Not connected to internet !")
                        }
                    }
                    else{
                        Log.e(TAG, "Incorrect config !")
                    }
                }
                else{
                    Log.e(TAG, "Incorrect config !")
                }
            }
            else{
                Log.e(TAG, "Context/activity can't be blank !")
            }
        }
        else{
            Log.e(TAG, "Null config detected !")
        }
    }

    private fun getScope(scopes: List<String>?): String? {
        val joined = scopes?.joinToString("+")
        return joined
    }

    fun configure(config: IdentitasConfig){
        this.config = config
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IdentitasConstants.LOGIN_ACTIVITY_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                mCallback?.success(Result(AuthToken(
                        data?.getStringExtra(IdentitasConstants.ACCESS_TOKEN_FIELD),
                        data?.getStringExtra(IdentitasConstants.REFRESH_TOKEN_FIELD),
                        data?.getStringExtra(IdentitasConstants.TOKEN_TYPE_FIELD)), null)
                )
            }
            else if (data?.getIntExtra(IdentitasConstants.GET_TOKEN_ERROR_CODE_FIELD, 0)==400){
                mCallback?.failure(IdentitasException("Unauthorized"))
            }
            else if(data?.getStringExtra(IdentitasConstants.GET_TOKEN_EXCEPTION_FIELD)!=null){
                mCallback?.failure(IdentitasException(data.getStringExtra(IdentitasConstants.GET_TOKEN_EXCEPTION_FIELD)))
            }
        }
    }

    fun setCallback(callback: Callback<AuthToken>){
        this.mCallback = callback
    }
}