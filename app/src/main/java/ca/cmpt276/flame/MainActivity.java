package ca.cmpt276.flame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import ca.cmpt276.flame.model.BGMusicPlayer;
import ca.cmpt276.flame.model.PrefsManager;
import ca.cmpt276.flame.model.TimeoutManager;

import static ca.cmpt276.flame.model.BGMusicPlayer.playBgMusic;

/**
 * Main Activity: displays a menu to the user, allowing them to open
 * the FlipCoinActivity, the TimeoutActivity, the ChildrenActivity and
 * the AboutActivity
 *
 * onTrimMemory() and listenForScreenTurningOff() code inspired from:
 * http://www.developerphil.com/no-you-can-not-override-the-home-button-but-you-dont-have-to/
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initModel();
        setupButtons();
        listenForScreenTurningOff();

        if (BGMusicPlayer.isMusicEnabled()) {
            playBgMusic();
        }
    }

    private void initModel() {
        SharedPreferences sharedPrefs = getPreferences(MODE_PRIVATE);
        PrefsManager.init(sharedPrefs);
        BGMusicPlayer.init(getApplicationContext());
    }

    private void setupButtons() {
        Button taskBtn = findViewById(R.id.main_btnTask);
        Button flipCoinBtn = findViewById(R.id.main_btnFlipCoin);
        Button takeBreathBtn = findViewById(R.id.main_btnTakeBreath);
        Button timeoutBtn = findViewById(R.id.main_btnTimeout);
        Button childrenBtn = findViewById(R.id.main_btnChildren);
        Button helpBtn = findViewById(R.id.main_btnAbout);
        ImageButton musicOnOffBtn = findViewById(R.id.main_btnMusicOnOff);

        if(BGMusicPlayer.isMusicEnabled()) {
            musicOnOffBtn.setImageResource(R.drawable.music_on);
        } else {
            musicOnOffBtn.setImageResource(R.drawable.music_off);
        }

        taskBtn.setOnClickListener(view -> startActivity(TaskActivity.makeIntent(this)));
        flipCoinBtn.setOnClickListener(view -> startActivity(FlipCoinActivity.makeIntent(this)));
        takeBreathBtn.setOnClickListener(view -> startActivity(BreathsActivity.makeIntent(this)));
        childrenBtn.setOnClickListener(view -> startActivity(ChildrenActivity.makeIntent(this)));
        helpBtn.setOnClickListener(view -> startActivity(HelpActivity.makeIntent(this)));

        timeoutBtn.setOnClickListener(view -> {
            if(TimeoutManager.getInstance().getTimerState() == TimeoutManager.TimerState.STOPPED) {
                startActivity(ChooseTimeActivity.makeIntent(this));
            } else {
                startActivity(TimeoutActivity.makeIntent(this));
            }
        });

        musicOnOffBtn.setOnClickListener(v -> {
            boolean isMusicEnabled = !BGMusicPlayer.isMusicEnabled();
            BGMusicPlayer.setIsMusicEnabled(isMusicEnabled);

            if (isMusicEnabled) {
                BGMusicPlayer.playBgMusic();
                musicOnOffBtn.setImageResource(R.drawable.music_on);
            } else {
                BGMusicPlayer.pauseBgMusic();
                musicOnOffBtn.setImageResource(R.drawable.music_off);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BGMusicPlayer.resumeBgMusic();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // turns off music if back button pressed from main activity
        BGMusicPlayer.pauseBgMusic();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            // turns off music if overview/home buttons pressed
            BGMusicPlayer.pauseBgMusic();
        }
    }

    private void listenForScreenTurningOff() {
        IntentFilter screenStateFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // turns off music if screen is turned off
                BGMusicPlayer.pauseBgMusic();
            }
        }, screenStateFilter);
    }
}