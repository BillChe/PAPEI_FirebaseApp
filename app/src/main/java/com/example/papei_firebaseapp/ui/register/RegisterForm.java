package com.example.papei_firebaseapp.ui.register;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.papei_firebaseapp.R;
import com.example.papei_firebaseapp.data.User;
import com.example.papei_firebaseapp.ui.login.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;


public class RegisterForm extends Activity implements View.OnClickListener{
    private Button registerFormButton;
    private Context context;
    private EditText usernameEditText;
    private EditText passwordEditText, emailEt;
    boolean registered;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.registerform);

        usernameEditText = findViewById(R.id.usernameET);
        passwordEditText = findViewById(R.id.passwordET);
        emailEt = findViewById(R.id.emailEt);

        context = this;
        registerFormButton = (Button) findViewById(R.id.registerFormButton);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(registered)
        {
            Intent loginIntent = new Intent(this,LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
            this.finish();
        }

    }

    @Override
    public void onClick(View view) {
        if((usernameEditText.getText()!=null) && (passwordEditText.getText()!=null)
                && !usernameEditText.getText().toString().isEmpty()
                && !passwordEditText.getText().toString().isEmpty())
        {
        String username = String.valueOf(usernameEditText.getText());
        String password = String.valueOf(passwordEditText.getText());
        String email = String.valueOf(emailEt.getText());

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    //create a user object
                    User user = new User(username, email);
                    //put user in real time database
                    //create a firebase database object here
                    //create a collection named Users
                    FirebaseDatabase.getInstance().getReference("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(user)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(context, context.getString(R.string.register_success),
                                                Toast.LENGTH_LONG).show();
                                        //todo redirect to Login Screen !!!!
                                    }
                                    else
                                    {
                                        Toast.makeText(context, context.getString(R.string.register_fail),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                }
                else
                {
                    Toast.makeText(context, context.getString(R.string.register_success),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        new AlertDialog.Builder(context)
                .setTitle("Completed entry")
                .setMessage("You Are Now Registered")
                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                        dialog.dismiss();
                        Intent loginIntent = new Intent(RegisterForm.this, LoginActivity.class);
                        startActivity(loginIntent);
                    }
                })
                // A null listener allows the button to dismiss the dialog and take no further action.
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
        }
        else
        {
            Toast.makeText(context, context.getString(R.string.please_check_data),
                    Toast.LENGTH_LONG).show();
        }
            registered = true;
    }

}
