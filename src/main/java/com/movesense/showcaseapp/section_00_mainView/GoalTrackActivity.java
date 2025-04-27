package com.movesense.showcaseapp.section_00_mainView;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonObject;
import com.movesense.showcaseapp.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GoalTrackActivity extends AppCompatActivity {

    private Spinner metricSpinner, operatorSpinner;
    private EditText targetValueEt, durationEt;
    private Button saveGoalBtn;
    private ApiService apiService;
    private int sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_track);

        // initialise API client
        apiService = new Retrofit.Builder()
                .baseUrl("https://pulsepal.store/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);

        // retrieve session ID from intent
        sessionId = getIntent().getIntExtra("session_id", 0);

        // bind UI elements
        metricSpinner   = findViewById(R.id.metricSpinner);
        operatorSpinner = findViewById(R.id.operatorSpinner);
        targetValueEt   = findViewById(R.id.targetValueEt);
        durationEt      = findViewById(R.id.durationEt);
        saveGoalBtn     = findViewById(R.id.saveGoalBtn);

        // populate metric options
        ArrayAdapter<CharSequence> metricAdapter = ArrayAdapter.createFromResource(
                this, R.array.metric_options,
                android.R.layout.simple_spinner_item
        );
        metricAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        metricSpinner.setAdapter(metricAdapter);

        // populate operator options
        ArrayAdapter<CharSequence> operatorAdapter = ArrayAdapter.createFromResource(
                this, R.array.operator_options,
                android.R.layout.simple_spinner_item
        );
        operatorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        operatorSpinner.setAdapter(operatorAdapter);

        // show duration field only for heart rate metric
        metricSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = metricSpinner.getSelectedItem().toString();
                durationEt.setVisibility(
                        selected.equals("Heart Rate(bpm)")
                                ? View.VISIBLE : View.GONE
                );
            }

            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        fetchSessionGoals(); // load existing goals

        saveGoalBtn.setOnClickListener(v -> {
            if (sessionId <= 0) {
                Toast.makeText(this,
                        "No active session to assign a goal",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // read inputs
            String rawMetric   = metricSpinner.getSelectedItem().toString();
            String rawOperator = operatorSpinner.getSelectedItem().toString();
            String targetStr   = targetValueEt.getText().toString().trim();
            String durStr      = durationEt.getText().toString().trim();

            if (targetStr.isEmpty()) {
                Toast.makeText(this,
                        "Please enter a target value",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            float targetValue;
            int durationMin = 0;
            try {
                targetValue = Float.parseFloat(targetStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this,
                        "Enter valid number for target",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // parse duration if required
            if (rawMetric.equals("Heart Rate(bpm)")) {
                if (durStr.isEmpty()) {
                    Toast.makeText(this,
                            "Please enter duration for heart rate goal",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    durationMin = Integer.parseInt(durStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(this,
                            "Invalid duration",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // map UI selection to request values
            String metric;
            switch (rawMetric) {
                case "Heart Rate(bpm)":   metric = "heart_rate"; break;
                case "Pace(min/km)":      metric = "pace";      break;
                case "Distance(meters)":  metric = "distance";  break;
                case "Steps":             metric = "steps";     break;
                case "Calories(kcal)":    metric = "calories";  break;
                default:
                    Toast.makeText(this,
                            "Unknown metric",
                            Toast.LENGTH_SHORT).show();
                    return;
            }

            String operator;
            switch (rawOperator) {
                case "Below": operator = "<="; break;
                case "Above": operator = ">="; break;
                default:
                    Toast.makeText(this,
                            "Unknown operator",
                            Toast.LENGTH_SHORT).show();
                    return;
            }

            int durationSec = durationMin * 60;

            // build and send request
            CreateGoalRequest req = new CreateGoalRequest(
                    sessionId,
                    metric,
                    operator,
                    targetValue,
                    durationSec
            );

            apiService.createGoal(req).enqueue(new Callback<ResponseModel>() {
                @Override
                public void onResponse(Call<ResponseModel> call,
                                       Response<ResponseModel> response) {
                    if (response.isSuccessful()
                            && response.body()!=null
                            && response.body().success) {
                        Toast.makeText(GoalTrackActivity.this,
                                "Goal saved!",
                                Toast.LENGTH_SHORT).show();
                        fetchSessionGoals();
                    } else {
                        String msg = response.body()!=null
                                ? response.body().message
                                : "Unknown server error";
                        Toast.makeText(GoalTrackActivity.this,
                                "Error: " + msg,
                                Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(Call<ResponseModel> call, Throwable t) {
                    Toast.makeText(GoalTrackActivity.this,
                            "Network error: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // load existing session goals (no UI feedback here)
    private void fetchSessionGoals() {
        if (sessionId <= 0) return;

        JsonObject body = new JsonObject();
        body.addProperty("session_id", sessionId);

        apiService.getSessionGoals(body).enqueue(new Callback<GoalsResponse>() {
            @Override
            public void onResponse(Call<GoalsResponse> call,
                                   Response<GoalsResponse> resp) {
                if (resp.isSuccessful()
                        && resp.body()!=null
                        && resp.body().success) {

                }
            }
            @Override public void onFailure(Call<GoalsResponse> call, Throwable t) { }
        });
    }
}
