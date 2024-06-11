package com.khw.ciod

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.White)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.mainactivitybackgroundicon),
                    contentDescription = "",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 56.dp)
                        .size(240.dp, 400.dp)
                )

                Image(
                    painter = painterResource(id = R.drawable.mainactivitybackgroundicon2),
                    contentDescription = "",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 120.dp)
                        .size(176.dp, 240.dp)
                )

                val context = LocalContext.current
                var email: String by remember { mutableStateOf("altmen77@naver.com") }
                var password: String by remember { mutableStateOf("hahaha") }
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
                            value = "*".repeat(password.length),
                            onValueChange = { password = it },
                            label = { Text(text = "password") },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedLabelColor = Color.Black,
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )


                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(onClick = {
                                context.startActivity(Intent(context, SignUpActivity::class.java))
                            }) {
                                Text(
                                    text = "회원가입",
                                    fontSize = 16.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                auth = Firebase.auth

                Button(
                    onClick = {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success")
                                    val user = auth.currentUser
                                    val userIntent = Intent(context, ClosetActivity::class.java)
                                    if (user != null) {
                                        userIntent.putExtra("user", email)
                                    }
                                    password = ""
                                    context.startActivity(userIntent)
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                                    Toast.makeText(
                                        baseContext,
                                        task.exception.toString(),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(40.dp)
                        .size(280.dp, 50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    )
                ) {
                    Text(text = "로그인", fontSize = 16.sp)
                }
            }

        }
    }
}