package com.movesense.showcaseapp.section_00_mainView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.movesense.showcaseapp.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SettingsActivity extends AppCompatActivity {

    private TextInputEditText fullNameEt, emailEt, passwordEt, ageEt, heightEt, weightEt;
    private Spinner genderSp;
    private Button saveBtn;

    private static final String BASE_URL = "https://pulsepal.store/";
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        fullNameEt = findViewById(R.id.fullNameEt);
        emailEt    = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        ageEt      = findViewById(R.id.ageEt);
        heightEt   = findViewById(R.id.heightEt);
        weightEt   = findViewById(R.id.weightEt);
        genderSp   = findViewById(R.id.genderSp);
        saveBtn    = findViewById(R.id.saveBtn);

        // load existing values
        SharedPreferences prefs = getSharedPreferences("PulsePalPrefs", MODE_PRIVATE);
        fullNameEt.setText(prefs.getString("fullName",""));
        emailEt   .setText(prefs.getString("userEmail",""));
        ageEt     .setText(String.valueOf(prefs.getInt("age",0)));
        heightEt  .setText(String.valueOf(prefs.getFloat("height",0)));
        weightEt  .setText(String.valueOf(prefs.getFloat("weight",0)));
        String g = prefs.getString("gender","");
        int pos = ((android.widget.ArrayAdapter)genderSp.getAdapter()).getPosition(g);
        if(pos>=0) genderSp.setSelection(pos);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        saveBtn.setOnClickListener(v -> {
            String fullName = fullNameEt.getText().toString().trim();
            String email    = emailEt.getText().toString().trim();
            String password = passwordEt.getText().toString().trim();
            String ageStr   = ageEt.getText().toString().trim();
            String heightStr= heightEt.getText().toString().trim();
            String weightStr= weightEt.getText().toString().trim();
            String gender   = genderSp.getSelectedItem().toString();

            if(fullName.isEmpty()||email.isEmpty()||ageStr.isEmpty()||
                    heightStr.isEmpty()||weightStr.isEmpty()) {
                Toast.makeText(this,"Please fill in all fields",Toast.LENGTH_SHORT).show();
                return;
            }

            int age;
            float height, weight;
            try {
                age    = Integer.parseInt(ageStr);
                height = Float.parseFloat(heightStr);
                weight = Float.parseFloat(weightStr);
            } catch(NumberFormatException e){
                Toast.makeText(this,"Enter valid numbers for age/height/weight",Toast.LENGTH_SHORT).show();
                return;
            }

            UpdateUserRequest req = new UpdateUserRequest(
                    fullName,email,password,age,gender,height,weight
            );
            apiService.updateUser(req).enqueue(new Callback<ResponseModel>() {
                @Override
                public void onResponse(Call<ResponseModel> call, Response<ResponseModel> r) {
                    if(r.isSuccessful()&&r.body()!=null&&r.body().success){
                        Toast.makeText(SettingsActivity.this,
                                r.body().getMessage(),Toast.LENGTH_SHORT).show();
                        // save locally
                        prefs.edit()
                                .putString("fullName",fullName)
                                .putString("userEmail",email)
                                .putInt("age",age)
                                .putString("gender",gender)
                                .putFloat("height",height)
                                .putFloat("weight",weight)
                                .apply();
                        finish();
                    } else {
                        String err="Update failed";
                        try{ err=r.errorBody().string(); }catch(Exception ignored){}
                        Toast.makeText(SettingsActivity.this,
                                err,Toast.LENGTH_SHORT).show();
                        Log.e("Settings",err);
                    }
                }
                @Override
                public void onFailure(Call<ResponseModel> call, Throwable t) {
                    Toast.makeText(SettingsActivity.this,
                            "Network error: "+t.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
