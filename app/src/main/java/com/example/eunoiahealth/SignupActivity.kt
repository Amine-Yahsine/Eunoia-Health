package com.example.eunoiahealth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {
    private lateinit var buttonReg : MaterialButton
    private lateinit var emailText : TextInputEditText
    private lateinit var passwordText : TextInputEditText
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        buttonReg = findViewById(R.id.btn_register)
        emailText = findViewById(R.id.email)
        passwordText = findViewById(R.id.password)
        auth = FirebaseAuth.getInstance()

        buttonReg.setOnClickListener{
            val mail = emailText.text.toString().trim()
            val pass = passwordText.text.toString().trim()

            if (!isEmailValid(mail)){
                Toast.makeText(this@SignupActivity, "Please input a valid email address.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(pass.length <= 6){
                Toast.makeText(this@SignupActivity, "Your password needs to contain 6 characters or more.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(mail, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@SignupActivity,
                            "Account created successfully!",
                            Toast.LENGTH_SHORT,
                        ).show()
                        val intent = Intent(this@SignupActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {

                        Toast.makeText(
                            this@SignupActivity,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()

                    }
                }
        }

    }

    private fun isEmailValid(mail: CharSequence?): Boolean{
        if(mail == null){
            return false
        }
        else{
            return android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()
        }
    }
}