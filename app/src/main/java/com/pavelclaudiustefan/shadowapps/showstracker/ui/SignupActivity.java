package com.pavelclaudiustefan.shadowapps.showstracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.Utils;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignupActivity extends AppCompatActivity {

    @BindView(R.id.input_email) EditText emailText;
    @BindView(R.id.input_password) EditText passwordText;
    @BindView(R.id.input_password_confirm) EditText passwordConfirmText;
    @BindView(R.id.button_signup) Button signupButton;
    @BindView(R.id.link_login) TextView loginLink;
    @BindView(R.id.loadingProgress) ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signup();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
            }
        });
    }

    public void signup() {
        if (!validate()) {
            onSignupFailed();
            return;
        }

        signupButton.setEnabled(false);

        progressBar.setVisibility(View.VISIBLE);

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        // TODO - Signup logic
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            onSignupSucces();
                        } else {
                            onSignupFailed();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });

    }

    public void onSignupSucces() {
        signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Signing up failed", Toast.LENGTH_LONG).show();
        signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        String passwordConfirm = passwordConfirmText.getText().toString();

        if (email.isEmpty() || !Utils.isEmailValid(email)) {
            emailText.setError("Not a valid email adress");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 25) {
            passwordText.setError("Between 4 and 25 alphanumeric characters.");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        if (!Objects.equals(password, passwordConfirm)) {
            passwordConfirmText.setError("Passwords didn't match. Try again.");
            valid = false;
        } else {
            passwordConfirmText.setError(null);
        }

        return valid;
    }
}
