package com.movesense.showcaseapp.section_00_mainView;

import android.content.Intent;
import android.os.Bundle;
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

    private EditText fullNameEditText, emailEditText, passwordEditText, ageEditText, heightEditText, weightEditText;
    private Spinner genderSpinner;
    private Button signupButton;
    private TextView alreadyHaveAccount;

    private static final String BASE_URL = "https://your-server.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        ageEditText = findViewById(R.id.ageEditText);
        genderSpinner = findViewById(R.id.genderSpinner);
        heightEditText = findViewById(R.id.heightEditText);
        weightEditText = findViewById(R.id.weightEditText);
        signupButton = findViewById(R.id.signupButton);
        alreadyHaveAccount = findViewById(R.id.alreadyHaveAccount);

        signupButton.setOnClickListener(view -> {
            String fullName = fullNameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String age = ageEditText.getText().toString().trim();
            String gender = genderSpinner.getSelectedItem().toString();
            String height = heightEditText.getText().toString().trim();
            String weight = weightEditText.getText().toString().trim();

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || age.isEmpty() || height.isEmpty() || weight.isEmpty()) {
                Toast.makeText(SignupActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
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
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if (response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(SignupActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignupActivity.this, MainViewActivity.class));
                    finish();
                } else {
                    Toast.makeText(SignupActivity.this, "Signup Failed: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Toast.makeText(SignupActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
