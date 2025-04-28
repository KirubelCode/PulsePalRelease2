// Prepared By: Kirubel Temesgen
// Student ID: C00260396
// Institution: SETU Carlow
// Supervisor: Joeseph Kehoe
// Purpose: Visually display session data points on a graph and
// allows a user to select the session they wish to analyse and the variables they wish to see.
// Also this notes any goals which may have been defined previously in the exercise session.

package com.movesense.showcaseapp.section_00_mainView;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;                // <-- added
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.JsonObject;               // <-- added
import com.movesense.showcaseapp.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WorkoutResultsActivity extends AppCompatActivity {
    private static final int MAX_POINTS = 200;

    private Spinner sessionSpinner;
    private Spinner presetSpinner;
    private Spinner markerIntervalSpinner;
    private TextView resultsTitle;
    private LinearLayout summaryContainer;        // <-- added

    private ChipGroup metricChipGroup;
    private Chip chipHR, chipPace, chipDist, chipSteps, chipCals, chipToggleMarkers;

    private LineChart lineChart;
    private ApiService apiService;

    private List<SessionSummary> sessionList;
    private List<SessionDataPoint> dataPoints;
    private long baseTimestamp;

    // how many seconds between drawing each marker
    private int markerIntervalSec = 10;

    // final values passed from ExerciseSessionActivity
    private int finalSteps;                      // <-- added
    private float finalDist, finalPace, finalCals; // <-- added
    private int finalZoneSec;                    // <-- added

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_results);

        // bind summary container
        summaryContainer = findViewById(R.id.summaryContainer); // <-- added

        // read final values
        finalSteps   = getIntent().getIntExtra("final_steps", 0);
        finalDist    = getIntent().getFloatExtra("final_distance", 0f);
        finalPace    = getIntent().getFloatExtra("final_pace", 0f);
        finalCals    = getIntent().getFloatExtra("final_calories", 0f);
        finalZoneSec = getIntent().getIntExtra("final_timeInZone", 0);

        // load user email from prefs
        SharedPreferences prefs = getSharedPreferences("PulsePalPrefs", MODE_PRIVATE);
        String userEmail = prefs.getString("userEmail", null);

        // UI references
        sessionSpinner        = findViewById(R.id.sessionSpinner);
        presetSpinner         = findViewById(R.id.presetSpinner);
        markerIntervalSpinner = findViewById(R.id.markerIntervalSpinner);

        metricChipGroup       = findViewById(R.id.metricChipGroup);
        chipHR                = findViewById(R.id.chipHR);
        chipPace              = findViewById(R.id.chipPace);
        chipDist              = findViewById(R.id.chipDist);
        chipSteps             = findViewById(R.id.chipSteps);
        chipCals              = findViewById(R.id.chipCals);
        chipToggleMarkers     = findViewById(R.id.chipToggleMarkers);

        lineChart             = findViewById(R.id.lineChart);

        // Retrofit setup
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://pulsepal.store/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Marker‐interval spinner: 5s,10s,15s,30s,60s
        String[] intervals = {"5s","10s","15s","30s","60s"};
        ArrayAdapter<String> markerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, intervals
        );
        markerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        markerIntervalSpinner.setAdapter(markerAdapter);
        markerIntervalSpinner.setSelection(1); // default to "10s"
        markerIntervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                markerIntervalSec = Integer.parseInt(intervals[pos].replace("s",""));
                updateChart();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        setupPresetSpinner();

        // Fetch session list
        apiService.getSessions(new UserRequest(userEmail))
                .enqueue(new Callback<SessionListResponse>() {
                    @Override public void onResponse(
                            Call<SessionListResponse> call,
                            Response<SessionListResponse> response
                    ) {
                        if (response.isSuccessful() && response.body().success) {
                            sessionList = response.body().sessions;
                            setupSessionSpinner();
                        } else {
                            Toast.makeText(
                                    WorkoutResultsActivity.this,
                                    "Failed to load sessions",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                    @Override public void onFailure(Call<SessionListResponse> call, Throwable t) {}
                });

        // Immediately show goal summary for this session_id if passed
        int sid = getIntent().getIntExtra("session_id", -1);
        if (sid != -1) {
            fetchAndShowGoals(sid);
        }
    }

    private void setupSessionSpinner() {
        List<String> labels = new ArrayList<>();
        SimpleDateFormat inFmt  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        SimpleDateFormat outFmt = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US);

        for (SessionSummary s : sessionList) {
            try {
                Date d = inFmt.parse(s.date);
                labels.add(outFmt.format(d)
                        + " (" + String.format(Locale.US, "%.1f min", s.duration_minutes) + ")");
            } catch (Exception e) {
                labels.add(s.date);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, labels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sessionSpinner.setAdapter(adapter);

        sessionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(
                    AdapterView<?> parent, View view, int pos, long id
            ) {
                int sessionId = sessionList.get(pos).session_id;
                fetchDataPointsForSession(sessionId);
                fetchAndShowGoals(sessionId); // <-- added
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void fetchAndShowGoals(int sessionId) {
        // 1) Always load the persisted summary for this session
        SharedPreferences prefs = getSharedPreferences("PulsePalPrefs", MODE_PRIVATE);
        finalSteps   = prefs.getInt   ("summary_" + sessionId + "_steps",    finalSteps);
        finalDist    = prefs.getFloat ("summary_" + sessionId + "_distance", finalDist);
        finalPace    = prefs.getFloat ("summary_" + sessionId + "_pace",     finalPace);
        finalCals    = prefs.getFloat ("summary_" + sessionId + "_calories", finalCals);
        finalZoneSec = prefs.getInt   ("summary_" + sessionId + "_zone",     finalZoneSec);

        // 2) Fetch the goals and display checkmarks
        JsonObject body = new JsonObject();
        body.addProperty("session_id", sessionId);
        apiService.getSessionGoals(body).enqueue(new Callback<GoalsResponse>() {
            @Override public void onResponse(Call<GoalsResponse> call, Response<GoalsResponse> r) {
                if (!r.isSuccessful() || r.body()==null || !r.body().success) return;
                summaryContainer.removeAllViews();

                for (GoalsResponse.Goal g : r.body().goals) {
                    String metric = g.metric;
                    float target  = "heart_rate".equals(metric)
                            ? g.duration_seconds
                            : g.target_value;

                    float achieved;
                    switch (metric) {
                        case "steps":      achieved = finalSteps;   break;
                        case "distance":   achieved = finalDist;    break;
                        case "pace":       achieved = finalPace;    break;
                        case "calories":   achieved = finalCals;    break;
                        case "heart_rate": achieved = finalZoneSec; break;
                        default:           achieved = 0;            break;
                    }

                    boolean passed;
                    if ("pace".equals(metric)) {
                        passed = "<=".equals(g.operator)
                                ? achieved <= target
                                : achieved >= target;
                    } else {
                        passed = achieved >= target;
                    }

                    TextView row = new TextView(WorkoutResultsActivity.this);
                    row.setTextSize(16f);
                    row.setPadding(0,8,0,8);
                    row.setTextColor(passed
                            ? Color.parseColor("#2E7D32")
                            : Color.parseColor("#C62828"));

                    String label;
                    switch (metric) {
                        case "steps":      label = "Steps";          break;
                        case "distance":   label = "Distance";       break;
                        case "pace":       label = "Pace";           break;
                        case "calories":   label = "Calories";       break;
                        case "heart_rate": label = "Time in HR zone";break;
                        default:           label = metric;           break;
                    }

                    String unit = metric.equals("distance")   ? "m"
                            : metric.equals("calories")   ? "kcal"
                            : metric.equals("pace")       ? "min/km"
                            : metric.equals("heart_rate") ? "sec"
                            : "steps";

                    String doneStr = metric.equals("heart_rate")
                            ? String.format(Locale.US, "%d:%02d", (int)achieved/60, (int)achieved%60)
                            : String.format(Locale.US, "%.2f", achieved);
                    String tgtStr  = metric.equals("heart_rate")
                            ? String.format(Locale.US, "%d:%02d", (int)target/60, (int)target%60)
                            : String.format(Locale.US, "%.2f", target);

                    row.setText((passed ? "✔ " : "✘ ")
                            + label + ": " + doneStr + " / " + tgtStr + " " + unit);
                    summaryContainer.addView(row);
                }
            }
            @Override public void onFailure(Call<GoalsResponse> c, Throwable t) {}
        });
    }
                                               // <-- end added method

    private void fetchDataPointsForSession(int sessionId) {
        apiService.getSessionDataPoints(new SessionDataRequest(sessionId))
                .enqueue(new Callback<SessionDataResponse>() {
                    @Override public void onResponse(
                            Call<SessionDataResponse> call,
                            Response<SessionDataResponse> resp
                    ) {
                        if (resp.isSuccessful()
                                && resp.body() != null
                                && resp.body().success
                        ) {
                            dataPoints    = resp.body().data_points;
                            baseTimestamp = dataPoints.isEmpty()
                                    ? 0
                                    : dataPoints.get(0).timestamp;
                            updateChart();
                        } else {
                            Toast.makeText(
                                    WorkoutResultsActivity.this,
                                    "Failed to load data points",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                    @Override public void onFailure(
                            Call<SessionDataResponse> call, Throwable t
                    ) {
                        Toast.makeText(
                                WorkoutResultsActivity.this,
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void setupPresetSpinner() {
        String[] presets = {"Default","Cardio","Progress"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, presets
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        presetSpinner.setAdapter(adapter);

        presetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(
                    AdapterView<?> parent, View view, int pos, long id
            ) {
                switch (pos) {
                    case 0: setChips(false,true,false,false,false); break;
                    case 1: setChips(true,true,false,false,true);  break;
                    case 2: setChips(false,false,true,true,false); break;
                }
                updateChart();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        metricChipGroup.setOnCheckedStateChangeListener((g, c) -> updateChart());
        chipToggleMarkers.setOnCheckedChangeListener((b, checked) -> updateChart());

        lineChart.getDescription().setEnabled(true);
        lineChart.getDescription().setText("Time (mm:ss)");
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.getLegend().setWordWrapEnabled(true);

        XAxis x = lineChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawLabels(true);
        x.setLabelRotationAngle(-45);
        x.setGranularity(10f);
        x.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float value) {
                int sec = (int) value;
                return String.format(Locale.US, "%02d:%02d", sec/60, sec%60);
            }
        });
    }

    private void setChips(
            boolean hr,
            boolean pace,
            boolean dist,
            boolean steps,
            boolean cals
    ) {
        chipHR.setChecked(hr);
        chipPace.setChecked(pace);
        chipDist.setChecked(dist);
        chipSteps.setChecked(steps);
        chipCals.setChecked(cals);
    }

    private void updateChart() {
        if (dataPoints == null) return;

        // determine sample period (s) between raw points
        float sampleSec = 1f;
        if (dataPoints.size()>1) {
            sampleSec = (dataPoints.get(1).timestamp
                    - dataPoints.get(0).timestamp) / 1000f;
        }
        // how many raw points to skip to achieve ~ markerIntervalSec
        int step = Math.max(1, Math.round(markerIntervalSec / sampleSec));

        boolean showMarkers = chipToggleMarkers.isChecked();

        LineData data = new LineData();
        if (chipHR.isChecked()) {
            LineDataSet ds = build("Heart Rate (bpm)", dp->dp.heart_rate, step, showMarkers);
            ds.setColor(Color.RED);
            ds.setCircleColor(Color.RED);
            data.addDataSet(ds);
        }
        if (chipPace.isChecked()) {
            LineDataSet ds = build("Pace (min/km)", dp->dp.pace, step, showMarkers);
            ds.setColor(Color.GREEN);
            ds.setCircleColor(Color.GREEN);
            data.addDataSet(ds);
        }
        if (chipDist.isChecked()) {
            LineDataSet ds = build("Distance (m)", dp->dp.distance, step, showMarkers);
            ds.setColor(Color.BLUE);
            ds.setCircleColor(Color.BLUE);
            data.addDataSet(ds);
        }
        if (chipSteps.isChecked()) {
            LineDataSet ds = build("Steps", dp->dp.steps, step, showMarkers);
            ds.setColor(Color.MAGENTA);
            ds.setCircleColor(Color.MAGENTA);
            data.addDataSet(ds);
        }
        if (chipCals.isChecked()) {
            LineDataSet ds = build("Calories (kcal)", dp->dp.calories, step, showMarkers);
            ds.setColor(Color.rgb(255,165,0)); // orange
            ds.setCircleColor(Color.rgb(255,165,0));
            data.addDataSet(ds);
        }

        lineChart.setData(data);
        lineChart.invalidate();
    }

    private LineDataSet build(
            String label,
            ValueExtractor ext,
            int step,
            boolean drawMarkers
    ) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < dataPoints.size(); i += step) {
            SessionDataPoint dp = dataPoints.get(i);
            float x = (dp.timestamp - baseTimestamp) / 1000f;
            entries.add(new Entry(x, ext.extract(dp)));
        }
        // include last point
        if ((dataPoints.size()-1) % step != 0) {
            SessionDataPoint dp = dataPoints.get(dataPoints.size()-1);
            float x = (dp.timestamp - baseTimestamp) / 1000f;
            entries.add(new Entry(x, ext.extract(dp)));
        }

        LineDataSet set = new LineDataSet(entries, label);
        set.setLineWidth(2f);
        set.setDrawCircles(drawMarkers);
        set.setCircleRadius(drawMarkers ? 3f : 0f);
        set.setDrawValues(drawMarkers);
        return set;
    }

    interface ValueExtractor { float extract(SessionDataPoint dp); }
}
