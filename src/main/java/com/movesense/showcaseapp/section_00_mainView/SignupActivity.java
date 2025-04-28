// Prepared By: Kirubel Temesgen
// Student ID: C00260396
// Institution: SETU Carlow
// Supervisor: Joeseph Kehoe
// Purpose: User signup.

package com.movesense.showcaseapp.section_00_mainView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.movesense.showcaseapp.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SignupActivity extends AppCompatActivity {

    private EditText fullNameEditText, emailEditText, passwordEditText,
            ageEditText, heightEditText, weightEditText;
    private Spinner genderSpinner;
    private Button signupButton;
    private TextView alreadyHaveAccount;

    private static final String BASE_URL = "https://pulsepal.store/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailEditText    = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        ageEditText      = findViewById(R.id.ageEditText);
        heightEditText   = findViewById(R.id.heightEditText);
        weightEditText   = findViewById(R.id.weightEditText);
        genderSpinner    = findViewById(R.id.genderSpinner);
        signupButton     = findViewById(R.id.signupButton);
        alreadyHaveAccount = findViewById(R.id.alreadyHaveAccount);

        signupButton.setOnClickListener(view -> {
            String fullName  = fullNameEditText.getText().toString().trim();
            String email     = emailEditText   .getText().toString().trim();
            String password  = passwordEditText.getText().toString().trim();
            String ageStr    = ageEditText     .getText().toString().trim();
            String heightStr = heightEditText  .getText().toString().trim();
            String weightStr = weightEditText  .getText().toString().trim();
            String gender    = genderSpinner.getSelectedItem().toString();

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() ||
                    ageStr.isEmpty()  || heightStr.isEmpty() || weightStr.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int age;
            float height, weight;
            try {
                age    = Integer.parseInt(ageStr);
                height = Float.parseFloat(heightStr);
                weight = Float.parseFloat(weightStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this,
                        "Enter valid numbers for Age, Height, and Weight",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (age <= 0 || height <= 0 || weight <= 0) {
                Toast.makeText(this,
                        "Please enter valid Age, Height, and Weight values",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(new User(fullName, email, password, age, gender, height, weight));
        });

        alreadyHaveAccount.setOnClickListener(view -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser(User user) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<ResponseModel> call = apiService.signup(user);

        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(
                    Call<ResponseModel> call,
                    Response<ResponseModel> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("SignupResponse", "Success: " + response.body().getMessage());
                    Toast.makeText(SignupActivity.this,
                            response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    // After successful signup, go to login
                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                    finish();
                } else {
                    String err = "Signup failed";
                    try { err = response.errorBody().string(); }
                    catch (Exception ignored) {}
                    Log.e("SignupResponse", err);
                    Toast.makeText(SignupActivity.this,
                            err, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.e("SignupResponse", "Network error: " + t.getMessage());
                Toast.makeText(SignupActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
