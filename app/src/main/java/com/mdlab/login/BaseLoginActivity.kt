package com.mdlab.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse

abstract class  BaseLoginActivity : AppCompatActivity() {

    /**
     * Override this if you want to set your own theme or flags
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_login)
        login()
    }

    private lateinit var auth: FirebaseAuth

    /**
     * Override this if you want to use other providers then Google, Email and Anonymous
     */
    protected open fun providersList(): List<AuthUI.IdpConfig>? = arrayListOf(
        AuthUI.IdpConfig.GoogleBuilder().build(),
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.AnonymousBuilder().build()
    )

    private fun login() {
        auth = Firebase.auth
        if (auth.currentUser != null) {
            //signed in
            startSignedInActivity(this, null)
            finish()
        } else {
            // sign in
            startActivityForResult(
                providersList()?.let {
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false, true)
                        .setAvailableProviders(
                            it
                        )
                        .build()
                },
                RC_SIGN_IN
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val response: IdpResponse? = IdpResponse.fromResultIntent(data)
            if (resultCode == RESULT_OK) {
                startSignedInActivity(this, response)
                finish()
            } else {
                // sign in failed
                if (response == null) {
                    Log.d(TAG, "activity result: cancelled")
                }
                val errCode =  response?.error?.errorCode ?: 0
                if (ErrorCodes.NO_NETWORK == errCode) {
                    Log.d(TAG, "activity result: no network")
                    return
                }
                Log.d(TAG, "activity result: unknown error")
            }
        }
    }

    protected open fun startSignedInActivity(context: Context, response: IdpResponse?) {
        if (response != null) {
            Log.d(TAG, "Login successfull")
        } else {
            Log.d(TAG, "Login failed")
        }
    }

    companion object {
        const val RC_SIGN_IN = 123
        const val TAG = "LoginActivity"
    }
}