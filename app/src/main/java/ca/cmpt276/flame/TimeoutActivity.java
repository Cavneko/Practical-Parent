package ca.cmpt276.flame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Locale;

import ca.cmpt276.flame.model.BGMusicPlayer;
import ca.cmpt276.flame.model.TimeoutManager;

/**
 * TimeoutActivity shows the currently running timer, and allows the user
 * to pause, reset, resume or cancel the timer
 */
public class TimeoutActivity extends AppCompatActivity {
    private static final int MILLIS_IN_MIN = 60000;
    private static final int MILLIS_IN_SEC = 1000;
    private static final int PROGRESS_BAR_STEPS = 1000;
    private static final int COUNTDOWN_INTERVAL_MILLIS = 40;
    public static final int TIMER_SPEED_MIN_VALUE = 25;
    public static final int TIMER_SPEED_MAX_VALUE = 400;
    public static final int TIMER_SPEED_INCREMENT = 25;
    private final TimeoutManager timeoutManager = TimeoutManager.getInstance();
    private TextView timeSpeedTxt;
    private CountDownTimer countDownTimer;
    private Button pauseTimerBtn;
    private Button resetBtn;
    private TextView countdownTimeTxt;
    private ProgressBar circularProgressBar;
    private ImageButton settingImageBtn;
    private float millisEntered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeout);

        millisEntered = timeoutManager.getMinutesEntered() * MILLIS_IN_MIN;
        circularProgressBar = findViewById(R.id.timeout_progressBar);
        circularProgressBar.setMax(PROGRESS_BAR_STEPS);
        settingImageBtn = findViewById(R.id.timeoutActivity_settingsImageButton);
        timeSpeedTxt = findViewById(R.id.timeoutActivity_timeSpeedView);
        setupToolbar();
        setUpSettingsBtn();
        setupPauseButton();
        setUpResetButton();
        setupTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        countDownTimer.cancel();
    }

    private void updateButtons() {
        switch(timeoutManager.getTimerState()) {
            case RUNNING:
                pauseTimerBtn.setText(R.string.pause);
                resetBtn.setText(R.string.reset);
                break;
            case PAUSED:
                pauseTimerBtn.setText(R.string.resume);
                resetBtn.setText(R.string.reset);
                break;
            case STOPPED:
                pauseTimerBtn.setText(R.string.start);
                resetBtn.setText(R.string.cancel);
                break;
        }
    }

    private void updateTimerProgress() {
        if(countdownTimeTxt == null) {
            countdownTimeTxt = findViewById(R.id.timeout_txtTimeRemaining);
        }

        long millisRemaining = timeoutManager.getMillisRemaining();
        int progressBarStepsLeft = (int) (PROGRESS_BAR_STEPS * millisRemaining / millisEntered);

        circularProgressBar.setProgress(progressBarStepsLeft);

        long minRemaining = millisRemaining / MILLIS_IN_MIN;
        long secRemaining = (millisRemaining % MILLIS_IN_MIN) / MILLIS_IN_SEC;
        String timeStr = String.format(Locale.getDefault(), "%d:%02d", minRemaining, secRemaining);

        countdownTimeTxt.setText(timeStr);

        if(timeoutManager.getMillisRemaining() == 0) {
            countDownTimer.cancel();
            countdownTimeTxt.setText(R.string.finished);
            updateButtons();
            timeSpeedTxt.setVisibility(TextView.INVISIBLE);
            settingImageBtn.setVisibility(TextView.INVISIBLE);
        }
    }

    private void updateTimerSpeedTxt() {
        timeSpeedTxt.setText(getString(R.string.timer_speed, timeoutManager.getSpeedPercentage()));
    }

    private void setUpSettingsBtn() {
        settingImageBtn.setBackgroundColor(Color.TRANSPARENT);
        settingImageBtn.setOnClickListener(view -> chooseSpeedDialog());
    }

    private void setupPauseButton() {
        pauseTimerBtn = findViewById(R.id.timeout_btnPause);

        pauseTimerBtn.setOnClickListener(view -> {
            switch (timeoutManager.getTimerState()) {
                case RUNNING:
                    // "Pause" button
                    countDownTimer.cancel();
                    timeoutManager.pause(getApplicationContext());
                    break;
                case PAUSED:
                    // "Resume" button if timer is paused, fall through
                case STOPPED:
                    // "Start" button if timer is stopped
                    settingImageBtn.setVisibility(TextView.VISIBLE);
                    timeSpeedTxt.setVisibility(TextView.VISIBLE);
                    countDownTimer.start();
                    timeoutManager.start(getApplicationContext());
                    updateTimerSpeedTxt();
                    break;
            }

            updateButtons();
        });
    }

    private void setUpResetButton() {
        resetBtn = findViewById(R.id.timeout_btnReset);

        // reset button cancels the previous timer and sets remaining time to the starting time
        resetBtn.setOnClickListener(view -> {
            if (timeoutManager.getTimerState() == TimeoutManager.TimerState.STOPPED) {
                timeoutManager.cancelAlarm(getApplicationContext());
                finish();
            } else {
                countDownTimer.cancel();
                timeoutManager.reset(getApplicationContext());
                updateTimerProgress();
                updateButtons();
                updateTimerSpeedTxt();
            }
            settingImageBtn.setVisibility(TextView.INVISIBLE);
        });
    }

    private void setupTimer() {
        countDownTimer = new CountDownTimer(Long.MAX_VALUE, COUNTDOWN_INTERVAL_MILLIS) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateTimerProgress();
            }

            @Override
            public void onFinish() {
                // method is required but nothing needs to happen here
                // instead, the TimeoutManager manages what happens when the timer finishes
            }
        };
    }

    private void chooseSpeedDialog() {
        String[] numberStrings = getSpeedOptions();

        NumberPicker speedPicker = new NumberPicker(this);
        speedPicker.setWrapSelectorWheel(true);
        speedPicker.setMinValue(0);
        speedPicker.setMaxValue(numberStrings.length - 1);
        speedPicker.setDisplayedValues(numberStrings);
        speedPicker.setValue((timeoutManager.getSpeedPercentage() / TIMER_SPEED_INCREMENT) - 1);
        
        LinearLayout numberLayout = setLinearNumberLayout(speedPicker);
        
        new AlertDialog.Builder(TimeoutActivity.this)
                .setTitle(R.string.choose_time_speed)
                .setView(numberLayout)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    int speed = Integer.parseInt(numberStrings[speedPicker.getValue()]);
                    timeoutManager.setSpeedPercentage(this, speed);
                    updateTimerSpeedTxt();
                })
                .setNegativeButton(R.string.cancel, null).show();
    }

    private String[] getSpeedOptions() {
        ArrayList<String> options = new ArrayList<>();

        for(int i = TIMER_SPEED_MIN_VALUE; i <= TIMER_SPEED_MAX_VALUE; i += TIMER_SPEED_INCREMENT) {
            options.add(String.valueOf(i));
        }

        return options.toArray(new String[0]);
    }

    private LinearLayout setLinearNumberLayout(NumberPicker numberPicker) {
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.addView(numberPicker);
        layout.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
        return layout;
    }


    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.timeout);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateTimerProgress();
        updateButtons();
        updateTimerSpeedTxt();
        if(timeoutManager.getTimerState() == TimeoutManager.TimerState.RUNNING) {
            countDownTimer.start();
        }

        BGMusicPlayer.resumeBgMusic();
    }

    protected static Intent makeIntent(Context context) {
        return new Intent(context, TimeoutActivity.class);
    }

}