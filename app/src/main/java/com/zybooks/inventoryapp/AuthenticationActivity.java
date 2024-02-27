package com.zybooks.inventoryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zybooks.inventoryapp.userviewmodel.UserHandler;

public class AuthenticationActivity extends AppCompatActivity {

    private UserHandler userHandler;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        userHandler = new UserHandler(this);
        usernameEditText = findViewById(R.id.username_entry);
        passwordEditText = findViewById(R.id.password_entry);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(userHandler.validateUser(usernameEditText.getText().toString(), passwordEditText.getText().toString())) {
                    Toast.makeText(AuthenticationActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(AuthenticationActivity.this, DatabaseDisplayActivity.class);
                    intent.putExtra("username", usernameEditText.getText().toString());
                    startActivity(intent);
                } else {
                  Toast.makeText(AuthenticationActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long userId = userHandler.addUser(usernameEditText.getText().toString(), passwordEditText.getText().toString());
                if (userId > 0) {
                    Toast.makeText(AuthenticationActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(AuthenticationActivity.this, DatabaseDisplayActivity.class);
                    intent.putExtra("username", usernameEditText.getText().toString());
                    startActivity(intent);
                } else {
                    Toast.makeText(AuthenticationActivity.this, "Registration Failed\n" +
                            "Username may already exist", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}