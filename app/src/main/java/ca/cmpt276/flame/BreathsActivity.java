package ca.cmpt276.flame;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import ca.cmpt276.flame.model.BGMusicPlayer;
import ca.cmpt276.flame.model.BreathsManager;

/**
 * BreathActivity allow user to change number of breaths if desired
 * One big breath button which does most of the interactions
 * show help message to indicate the user to go through the process of breath.
 */
public class BreathsActivity extends AppCompatActivity {
    private static final int INHALE_HOLD_TIME = 3000;
    private static final int EXHALE_WAIT_TIME = 3000;
    private static final int MAX_TIME = 10000;
    private static final float ALPHA_OPAQUE = 1.0f;
    private static final float ALPHA_LIGHTER = 0.7f;

    private final BreathsManager breathsManager = BreathsManager.getInstance();
    private int numBreathsLeft = breathsManager.getNumBreaths();

    private ImageButton settingsButton;
    private TextView headingText;
    private TextView buttonText;
    private ImageButton breathButton;
    private Animation growAnim;
    private TextView numLeftText;

    private final State idleState = new IdleState();
    private final State beginState = new BeginState();
    private final State inhaleState = new InhaleState();
    private final State exhaleState = new ExhaleState();
    private final State finishState = new FinishState();
    private State currentState = idleState;

    //***************************************************************
    //State code start here
    //***************************************************************

    /**
     * This State class represent three different State of Breath which is begin,inhale and exhale.
     * Each State have their own setting of Breath button.
     * Also do actions when exit and enter a new State.
     */
    private abstract static class State {
        void handleEnter() { }
        void handleExit() { }
        boolean handleTouch(View view, MotionEvent motionEvent) {
            return false;
        }
        void handleClick(View view) { }
    }

    private static class IdleState extends State { }

    private class BeginState extends State {
        @Override
        void handleEnter() {
            settingsButton.setVisibility(View.VISIBLE);
            numLeftText.setVisibility(View.INVISIBLE);

            numBreathsLeft = breathsManager.getNumBreaths();
            headingText.setText(getResources().getQuantityString(R.plurals.breaths_heading_begin, numBreathsLeft, numBreathsLeft));
            buttonText.setText(R.string.begin);
            breathButton.setImageResource(R.drawable.breaths_btn_begin);
        }

        @Override
        void handleClick(View view) {
            setState(inhaleState);
        }

        @Override
        void handleExit() {
            settingsButton.setVisibility(View.INVISIBLE);
            numLeftText.setVisibility(View.VISIBLE);
        }
    }

    private class InhaleState extends State {
        MediaPlayer soundPlayer;
        Handler handler = new Handler();
        boolean heldLongEnough;

        Runnable holdLongEnough = () -> {
            heldLongEnough = true;
            buttonText.setText(R.string.out);
            breathButton.setAlpha(ALPHA_LIGHTER);
        };

        Runnable holdTooLong = () -> {
            headingText.setText(R.string.breaths_hold_too_long);
        };

        @Override
        void handleEnter() {
            soundPlayer = MediaPlayer.create(BreathsActivity.this, R.raw.inhale);
            heldLongEnough = false;

            headingText.setText(R.string.breath_before_inhale);
            buttonText.setText(R.string.in);
            numLeftText.setText(getResources().getQuantityString(R.plurals.breaths_num_left, numBreathsLeft, numBreathsLeft));
            breathButton.setImageResource(R.drawable.breaths_btn_in);
        }

        @Override
        boolean handleTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startAnimation(false);
                    soundPlayer.seekTo(0);
                    soundPlayer.start();

                    headingText.setText(R.string.keep_inhaling);
                    handler.postDelayed(holdLongEnough, INHALE_HOLD_TIME);
                    handler.postDelayed(holdTooLong, MAX_TIME);
                    break;

                case MotionEvent.ACTION_UP:
                    if(heldLongEnough) {
                        setState(exhaleState);
                    } else {
                        stopRunning();
                        headingText.setText(R.string.breath_before_inhale);
                    }
                    break;
            }
            return true;
        }

        void stopRunning() {
            handler.removeCallbacksAndMessages(null);
            breathButton.setAlpha(ALPHA_OPAQUE);
            stopAnimation();

            if(soundPlayer.isPlaying()) {
                soundPlayer.pause();
            }
        }

        @Override
        void handleExit() {
            stopRunning();
            soundPlayer.release();
            soundPlayer = null;
        }
    }

    private class ExhaleState extends State {
        MediaPlayer soundPlayer;
        Handler handler = new Handler();

        Runnable waitLongEnough = () -> {
            numBreathsLeft--;
            numLeftText.setText(getResources().getQuantityString(R.plurals.breaths_num_left, numBreathsLeft, numBreathsLeft));

            if(numBreathsLeft > 0) {
                buttonText.setText(R.string.in);
            } else {
                buttonText.setText(R.string.good_job);
            }

            breathButton.setEnabled(true);
            breathButton.setAlpha(ALPHA_LIGHTER);
        };

        Runnable waitTooLong = this::setNextState;

        @Override
        void handleEnter() {
            soundPlayer = MediaPlayer.create(BreathsActivity.this, R.raw.exhale);

            handler.postDelayed(waitLongEnough, EXHALE_WAIT_TIME);
            handler.postDelayed(waitTooLong, MAX_TIME);

            breathButton.setEnabled(false);
            startAnimation(true);
            soundPlayer.seekTo(0);
            soundPlayer.start();

            headingText.setText(R.string.keep_exhaling);
            buttonText.setText(R.string.out);
            breathButton.setImageResource(R.drawable.breaths_btn_out);
        }

        @Override
        void handleClick(View view) {
            // handleClick is only possible after the EXHALE_WAIT_TIME
            setNextState();
        }

        void setNextState() {
            if(numBreathsLeft > 0) {
                setState(inhaleState);
            } else {
                setState(finishState);
            }
        }

        @Override
        void handleExit() {
            breathButton.setAlpha(ALPHA_OPAQUE);
            handler.removeCallbacksAndMessages(null);
            breathButton.setEnabled(true);
            stopAnimation();

            if(soundPlayer.isPlaying()) {
                soundPlayer.pause();
            }

            soundPlayer.release();
            soundPlayer = null;
        }
    }

    private class FinishState extends State {
        @Override
        void handleEnter() {
            headingText.setText(R.string.all_done);
            breathButton.setImageResource(R.drawable.breaths_btn_begin);
        }

        @Override
        void handleClick(View view) {
            setState(beginState);
        }
    }

    private void setState(State newState) {
        currentState.handleExit();
        currentState = newState;
        currentState.handleEnter();
    }

    //***************************************************************
    //State code end here
    //***************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breaths);
        setupToolbar();
        setupViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BGMusicPlayer.pauseBgMusic();
        setState(beginState);
    }

    @Override
    public void onPause() {
        super.onPause();
        setState(idleState);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupViews() {
        buttonText = findViewById(R.id.breaths_txtBtn);
        headingText = findViewById(R.id.breaths_txtHeading);
        numLeftText = findViewById(R.id.breaths_txtNumLeft);
        breathButton = findViewById(R.id.breathes_imageButton);
        settingsButton = findViewById(R.id.breaths_btnSettings);

        breathButton.setOnTouchListener((view,  motionEvent) -> {
            return currentState.handleTouch(view, motionEvent);
        });

        breathButton.setOnClickListener((view) -> {
            currentState.handleClick(view);
        });

        settingsButton.setOnClickListener((view) -> {
            showSettingsDialog();
        });
    }

    private float getAnimationScale() {
        ConstraintLayout rootLayout = findViewById(R.id.breaths_layoutContent);

        // Pythagorean theorem
        float parentDim = (float) Math.sqrt(Math.pow(rootLayout.getWidth(), 2) + Math.pow(rootLayout.getHeight(), 2));
        float btnDim = breathButton.getWidth();

        return parentDim / btnDim;
    }

    private void startAnimation(boolean isShrink) {
        final float HALF_LAYOUT = 0.5f;

        float bigScale = getAnimationScale();
        float fromScale = 1f;
        float toScale = bigScale;

        if(isShrink) {
            fromScale = bigScale;
            toScale = 1f;
        }

        growAnim = new ScaleAnimation(fromScale, toScale, fromScale, toScale, Animation.RELATIVE_TO_SELF, HALF_LAYOUT, Animation.RELATIVE_TO_SELF, HALF_LAYOUT);
        growAnim.setDuration(MAX_TIME);
        growAnim.setFillAfter(true);
        breathButton.startAnimation(growAnim);
    }

    private void stopAnimation() {
        if(growAnim != null) {
            growAnim.cancel();
            breathButton.clearAnimation();
        }
    }

    private void showSettingsDialog() {
        NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setMinValue(BreathsManager.MIN_BREATHS);
        numberPicker.setMaxValue(BreathsManager.MAX_BREATHS);
        numberPicker.setValue(breathsManager.getNumBreaths());

        LinearLayout numberLayout = setSettingDialogLayout(numberPicker);
        new AlertDialog.Builder(BreathsActivity.this)
                .setTitle(R.string.number_of_breaths)
                .setView(numberLayout)
                .setPositiveButton(R.string.save, (dialogInterface, i) -> {
                    breathsManager.setNumBreaths(numberPicker.getValue());
                    currentState.handleEnter();
                })
                .setNegativeButton(R.string.cancel, null).show();

    }

    private LinearLayout setSettingDialogLayout(NumberPicker numberPicker) {
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.addView(numberPicker);
        layout.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
        return layout;
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_Breaths);
        toolbar.setTitle(R.string.take_a_breath);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }

    protected static Intent makeIntent(Context context) {
        return new Intent(context, BreathsActivity.class);
    }

}