package com.khw.ciod

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.White)
            ) {
                BackgroundScreen()

                var email: String by remember { mutableStateOf("") }
                var password: String by remember { mutableStateOf("") }
                Row(modifier = Modifier.align(Alignment.Center)) {
                    Spacer(modifier = Modifier.weight(1f))
                    Column(
                        modifier = Modifier
                            .weight(5f)
                            .padding(bottom = 40.dp)
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(text = "email") },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedLabelColor = Color.Black,
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(text = "password") },
                            visualTransformation = PasswordVisualTransformation(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedLabelColor = Color.Black,
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
                SignUpButton(Modifier.align(Alignment.BottomCenter), email, password)
            }

        }
    }

    @Composable
    private fun SignUpButton(modifier: Modifier,email:String, password: String) {
        auth = Firebase.auth
        val context = LocalContext.current
        Button(
            onClick = {
                if (email.isEmpty() || password.isEmpty()) {

                    Toast.makeText(
                        baseContext,
                        "이메일 / 비밀번호를 입력하세요",
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Toast.makeText(
                                    context,
                                    "회원가입 완료",
                                    Toast.LENGTH_SHORT,
                                ).show()
                                Log.d(TAG, "createUserWithEmail:success")
                                finish()
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.exception)
                                Toast.makeText(
                                    context,
                                    task.exception.toString(),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                }
            },
            modifier = modifier
                .padding(40.dp)
                .size(280.dp, 50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black
            )
        ) {
            Text(text = "회원가입", fontSize = 16.sp)
        }

    }
}