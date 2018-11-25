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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.movies.MoviesActivity;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.Utils;

import java.util.Arrays;

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
    @BindView(R.id.button_facebook)     RelativeLayout facebookButton;
    @BindView(R.id.section_changer)     TextView sectionChangerTextView;
    @BindView(R.id.loading_indicator)   ProgressBar loadingIndicator;

    // Listener implemented in the parent activity
    private OnFragmentInteractionListener listener;

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private CallbackManager callbackManager;

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

        callbackManager = CallbackManager.Factory.create();

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login, container, false);

        ButterKnife.bind(this, root);

        setUpTextValidators();
        setUpLoginButton();
        setUpGoogleSignIn();
        //setUpFacebookSignIn();
        setUpDisabledFacebookSignIn();
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
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Google sign in failed", e);
            }
        } else if (FacebookSdk.isFacebookRequestCode(requestCode)){
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        onLoginSucces();
                    } else {
                        onLoginFailed(task.getException());
                    }
                });
    }

    private void setUpFacebookSignIn() {
        facebookButton.setOnClickListener(view -> {
            setIsLoading(true);


            LoginManager.getInstance().registerCallback(callbackManager,
                    new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            Log.d(TAG, "facebook:onSuccess:" + loginResult);
                            handleFacebookAccessToken(loginResult.getAccessToken());
                        }

                        @Override
                        public void onCancel() {
                            setIsLoading(false);
                            Log.d(TAG, "facebook:onCancel");
                        }

                        @Override
                        public void onError(FacebookException exception) {
                            setIsLoading(false);
                            Toast.makeText(getContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "facebook:onError", exception);
                        }
                    });
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("user_photos", "email", "public_profile", "user_posts"));
        });
    }

    private void setUpDisabledFacebookSignIn() {
        facebookButton.setOnClickListener(view -> Toast.makeText(getContext(), "Disabled", Toast.LENGTH_SHORT).show());
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        onLoginSucces();
                        Log.d(TAG, "signInWithCredential:success");
                    } else {
                        onLoginFailed(task.getException());
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                    }
                });
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
        facebookButton.setEnabled(!isLoading);
    }
}
