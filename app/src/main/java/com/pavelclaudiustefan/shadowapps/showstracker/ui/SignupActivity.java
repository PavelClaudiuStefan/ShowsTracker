package com.pavelclaudiustefan.shadowapps.showstracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.Utils;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.movies.MoviesActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignupActivity extends AppCompatActivity {

    public static final String TAG = "SignupActivity";

    @BindView(R.id.input_email) EditText emailText;
    @BindView(R.id.input_password) EditText passwordText;
    @BindView(R.id.input_password_confirm) EditText passwordConfirmText;
    @BindView(R.id.button_signup) Button signupButton;
    @BindView(R.id.link_login) TextView loginLink;
    @BindView(R.id.loadingProgress) ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        signupButton.setOnClickListener(view -> signup());

        loginLink.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.animator.push_right_in, R.animator.push_right_out);
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
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        onSignupSucces();
                    } else {
                        onSignupFailed();
                    }
                    progressBar.setVisibility(View.GONE);
                });

    }

    public void onSignupSucces() {
        // New user is authenticated - Add user to firestore
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {

            Map<String, Object> user = new HashMap<>();
            user.put("email", currentUser.getEmail());
            user.put("createdAt", Timestamp.now());
            user.put("updatedAt", Timestamp.now());

            firestore.collection("users")
                    .document(currentUser.getUid())
                    .set(user)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Added a new document"))
                    .addOnFailureListener(error -> Log.e(TAG, "Error adding document", error));
        } else {
            Log.e(TAG, "getCurrentUser is null");
        }

        signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
        startActivity(new Intent(this, MoviesActivity.class));
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

        if (password.isEmpty() || password.length() <= 5 || password.length() >= 50) {
            passwordText.setError("Between 5 and 50 alphanumeric characters.");
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
