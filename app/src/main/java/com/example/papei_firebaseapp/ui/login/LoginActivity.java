package com.example.papei_firebaseapp.ui.login;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.papei_firebaseapp.R;
import com.example.papei_firebaseapp.databinding.ActivityLoginBinding;
import com.example.papei_firebaseapp.ui.forgotpassword.PasswordForgot;
import com.example.papei_firebaseapp.ui.main.MainActivity;
import com.example.papei_firebaseapp.ui.register.RegisterForm;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private LoginViewModel vm;
    private ActivityLoginBinding binding;
    private TextView register,forgotPassword;
    private FirebaseAuth mAuth;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        vm = new LoginViewModel(LoginActivity.this);

        binding.setVm(vm);

        mAuth = FirebaseAuth.getInstance();

        //set views
        setViews();
        EditText usernameEditText = binding.username;
        EditText passwordEditText = binding.password;
        Button loginButton = binding.login;
        ProgressBar loadingProgressBar = binding.loading;
        register = binding.register;
        forgotPassword = binding.forgotPassword;

        vm.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        vm.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                vm.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //todo perform login in LoginViewModel
                /*   loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());*/

                    login();
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                //todo perform login in LoginViewModel
                /*   loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());*/
                login();
            }
        });

        binding.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterForm.class);
                registerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(registerIntent);
                LoginActivity.this.finish();
            }
        });

        binding.forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent forgotPassword = new Intent(LoginActivity.this, PasswordForgot.class);
                forgotPassword.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(forgotPassword);
                LoginActivity.this.finish();
            }
        });


    }

    private void setViews() {
    }


    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.login:
                login();
                
            default:

        }
    }

    private void login() {
        //Validate User Login
        String email = binding.username.getText().toString().trim();
        String password = binding.password.getText().toString().trim();

        if(email.isEmpty())
        {
            binding.username.setError(LoginActivity.this.getString(R.string.require_email));
            binding.username.requestFocus();
            return;
        }
        //email verification
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            binding.username.setError(LoginActivity.this.getString(R.string.enter_valid_email));
            binding.username.requestFocus();
            return;
        }
        //valida password input
        if(password.isEmpty())
        {
            binding.username.setError(LoginActivity.this.getString(R.string.require_password));
            binding.username.requestFocus();
            return;
        }
        //email verification
        if(password.length()<6)
        {
            binding.username.setError(LoginActivity.this.getString(R.string.min_password));
            binding.username.requestFocus();
            return;
        }

        //todo show progress bar here

        //try a login
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    //check email on firebase
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user.isEmailVerified())
                    {
                        //redirect to Main Activity
                        Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(loginIntent);
                        finish();
                    }
                    else
                    {
                        user.sendEmailVerification();
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setMessage(getString(R.string.check_email))
                                .setCancelable(true)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //do things
                                        dialog.dismiss();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();

                    }

                }
                else
                {
                    Toast.makeText(LoginActivity.this, LoginActivity.this.getString(R.string.login_failed),
                            Toast.LENGTH_LONG).show();

                }
            }
        });

    }


}