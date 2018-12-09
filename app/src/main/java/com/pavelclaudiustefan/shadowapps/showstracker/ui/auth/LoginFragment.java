package com.pavelclaudiustefan.shadowapps.showstracker.ui.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.data.LocalDataSyncService;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.movies.MoviesActivity;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.Utils;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginFragment extends Fragment {

    private static final String TAG = "ShadowDebug";

    public static final int SIGNUP_PAGE = 1;

    // Google activity request code
    public static final int RC_SIGN_IN = 0;

    @BindView(R.id.input_email)         EditText emailText;
    @BindView(R.id.input_password)      EditText passwordText;
    @BindView(R.id.button_login)        Button loginButton;
    @BindView(R.id.button_google)       RelativeLayout googleButton;
    @BindView(R.id.section_changer)     TextView sectionChangerTextView;
    @BindView(R.id.loading_indicator)   ProgressBar loadingIndicator;

    // Listener implemented in the parent activity
    private OnFragmentInteractionListener listener;

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    private boolean isNewUser = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initAuth();
    }

    private void initAuth() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        if (getContext() != null) {
            googleSignInClient = GoogleSignIn.getClient(getContext(), gso);
        } else {
            Log.e(TAG, "initAuth: getContext() is null", new Exception("context is null"));
        }

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login, container, false);

        ButterKnife.bind(this, root);

        setUpTextValidators();
        setUpLoginButton();
        setUpGoogleSignIn();
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
                isValidPassword(password);
            }
        });
    }

    private boolean isValidEmail(String email) {
        boolean isValid = Utils.isValidEmail(email);
        if (!isValid)
            emailText.setError("Not a valid email");
        return isValid;
    }

    private boolean isValidPassword(String password) {
        boolean isValid = password.length() >= 5;
        if (!isValid)
            passwordText.setError("Must have at least 6 characters");
        return isValid;
    }

    private void setUpLoginButton() {
        loginButton.setOnClickListener(view -> {
            hideKeyboard();

            setIsLoading(true);

            String email = emailText.getText().toString();
            String password = passwordText.getText().toString();

            if (isValidEmail(email) && isValidPassword(password)) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                onLoginSucces();
                            } else {
                                onLoginFailed(task.getException());
                            }
                        });
            } else {
                setIsLoading(false);
            }
        });
    }

    private void onLoginSucces() {
        if (getActivity() != null) {
            if (!isNewUser) {
                syncLocalData();
            }

            getActivity().finish();
            startActivity(new Intent(getContext(), MoviesActivity.class));
        } else {
            setIsLoading(false);
            Log.e(TAG, "onLoginSucces: getActivity() is null");
        }
    }

    private void onLoginFailed(Exception firebaseAuthException) {
        setIsLoading(false);

        if (firebaseAuthException == null) {
            Toast.makeText(getContext(), "Login failed", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), firebaseAuthException.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void syncLocalData() {
        Context context = getContext();
        assert context != null;

        Intent i = new Intent(context, LocalDataSyncService.class);
        i.putExtra(LocalDataSyncService.OPTION_TITLE, LocalDataSyncService.TITLE_SYNC);
        context.startService(i);
    }

    private void setUpGoogleSignIn() {
        googleButton.setOnClickListener(view -> {
            setIsLoading(true);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null) {
                        firebaseAuthWithGoogle(account);
                    } else {
                        setIsLoading(false);
                        Log.e(TAG, "onActivityResult: ", new Exception("Google account is null"));
                    }
                } catch (ApiException e) {
                    setIsLoading(false);

                    // Google Sign In failed, update UI appropriately
                    //Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Google sign in failed", e);
                }
            } else {
                // TODO - Check if the task was canceled or an error appeared
                setIsLoading(false);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        //Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        AuthResult authResult = task.getResult();
                        if (authResult != null && authResult.getAdditionalUserInfo().isNewUser()) {
                            isNewUser = true;
                            saveUserInFirestore();
                        }
                        onLoginSucces();
                    } else {
                        onLoginFailed(task.getException());
                    }
                });
    }

    private void saveUserInFirestore() {
        // New user is authenticated - Add user to firestore
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {

            Map<String, Object> user = new HashMap<>();
            if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                user.put("displayName", currentUser.getDisplayName());
            } else {
                user.put("displayName", currentUser.getEmail());
            }
            user.put("email", currentUser.getEmail());
            user.put("createdAt", Timestamp.now());
            user.put("updatedAt", Timestamp.now());

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
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

    private void setUpSectionChanger() {
        String text = "<font color=#263238>Don't have an account? </font> <font color=#FFFFFF>Sign Up</font>";
        sectionChangerTextView.setText(Html.fromHtml(text));

        sectionChangerTextView.setOnClickListener(view -> {
            hideKeyboard();

            if (listener != null) {
                listener.changeToSection(SIGNUP_PAGE);
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

        // Disable buttons while loading to avoid button click spam
        loginButton.setEnabled(!isLoading);
        googleButton.setEnabled(!isLoading);
    }
}
