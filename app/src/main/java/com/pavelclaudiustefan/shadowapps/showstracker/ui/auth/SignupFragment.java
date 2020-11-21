package com.pavelclaudiustefan.shadowapps.showstracker.ui.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.pavelclaudiustefan.shadowapps.showstracker.ui.movies.MoviesActivity;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.Utils;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignupFragment extends Fragment {

    private static final String TAG = "ShadowDebug";

    public static final int LOGIN_PAGE = 0;

    @BindView(R.id.input_email)             EditText emailText;
    @BindView(R.id.input_password)          EditText passwordText;
    @BindView(R.id.input_password_confirm)  EditText passwordConfirmText;
    @BindView(R.id.button_signup)           Button signupButton;
    @BindView(R.id.section_changer)         TextView sectionChangerTextView;
    @BindView(R.id.loading_indicator)       ProgressBar loadingIndicator;

    // Listener implemented in the parent activity
    private OnFragmentInteractionListener listener;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        // Init firebase and firestore
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_signup, container, false);

        ButterKnife.bind(this, root);

        setUpTextValidators();
        setUpSignupButton();
        setUpSectionChanger();

        return root;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private void setUpTextValidators() {
        emailText.setOnFocusChangeListener((view, isFocused) -> {
            String email = emailText.getText().toString();
            if (!isFocused && !email.isEmpty()) {
                isValidEmail(email);
            }
        });

        passwordText.setOnFocusChangeListener((view, isFocused) -> {
            String password = passwordText.getText().toString();
            if (!isFocused && !password.isEmpty()) {
                if (isValidPassword(password)) {
                    // The password is valid -> Check if it's confirmed too (only if passwordConfirmText is not empty)
                    String passwordConfirm = passwordConfirmText.getText().toString();
                    if (!passwordConfirm.isEmpty())
                        isValidPasswordConfirm(password, passwordConfirm);
                }
            } else {
                passwordConfirmText.setError(null);
            }
        });

        passwordConfirmText.setOnFocusChangeListener((view, isFocused) -> {
            String password = passwordText.getText().toString();
            String passwordConfirm = passwordConfirmText.getText().toString();
            if (!isFocused && !passwordConfirm.isEmpty()) {
                isValidPasswordConfirm(password, passwordConfirm);
            }
        });
    }

    private boolean isValidEmail(String email) {
        boolean isValid = Utils.isValidEmail(email);
        if (!isValid) {
            emailText.setError("Not a valid email");
        }
        return isValid;
    }

    private boolean isValidPassword(String password) {
        boolean isValid = password.length() >= 5;
        if (!isValid) {
            passwordText.setError("Must have at least 6 characters");
        }
        return isValid;
    }

    private boolean isValidPasswordConfirm(String password, String passwordConfirm) {
        boolean isValid = password.equals(passwordConfirm);
        if (!isValid) {
            passwordConfirmText.setError("Passwords don't match");
        }
        return isValid;
    }

    private void setUpSignupButton() {
        signupButton.setOnClickListener(view -> {
            setIsLoading(true);
            hideKeyboard();

            String email = emailText.getText().toString();
            String password = passwordText.getText().toString();
            String passwordConfirm = passwordConfirmText.getText().toString();

            if (isValidEmail(email) && isValidPassword(password) && isValidPasswordConfirm(password, passwordConfirm)) {
                trySignup(email, password);
            } else {
                setIsLoading(false);
            }
        });
    }

    private void trySignup(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        onSignupSucces();
                    } else {
                        onSignupFailed(task.getException());
                    }
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
            setIsLoading(false);
            Log.e(TAG, "getCurrentUser is null");
        }

        if (getActivity() != null) {
            getActivity().finish();
            startActivity(new Intent(getContext(), MoviesActivity.class));
        } else {
            setIsLoading(false);
            Log.e(TAG, "onSignupSucces: getActivity() is null");
        }
    }

    public void onSignupFailed(Exception firebaseAuthException) {
        setIsLoading(false);

        // TODO: inform user if he has no internet connection
        if (firebaseAuthException == null) {
            Toast.makeText(getContext(), "Signing up failed", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), firebaseAuthException.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setUpSectionChanger() {
        String text = "<font color=#263238>Already have an account? </font> <font color=#EEEEEE>Log In</font>";
        sectionChangerTextView.setText(Html.fromHtml(text));

        sectionChangerTextView.setOnClickListener(view -> {
            hideKeyboard();
            if (listener != null) {
                listener.changeToSection(LOGIN_PAGE);
            }
        });
    }

    public void hideKeyboard() {
        Context context = getContext();
        View view = getView();
        if (context != null && view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setIsLoading(boolean isLoading) {
        if (isLoading) {
            loadingIndicator.setVisibility(View.VISIBLE);
        } else {
            loadingIndicator.setVisibility(View.GONE);
        }

        // Disable button while loading to avoid button click spam
        signupButton.setEnabled(!isLoading);
    }

}
