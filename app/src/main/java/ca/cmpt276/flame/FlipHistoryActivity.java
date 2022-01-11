package ca.cmpt276.flame;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import ca.cmpt276.flame.model.BGMusicPlayer;
import ca.cmpt276.flame.model.Child;
import ca.cmpt276.flame.model.ChildrenManager;
import ca.cmpt276.flame.model.FlipHistoryEntry;
import ca.cmpt276.flame.model.FlipManager;

/**
 * FlipHistoryActivity allows the user to see the history of flip coin, which included child's name,
 * the result of flip coin, the state of win or lose, and the date teh child flip the coin.
 * If user click switch button, user could change state between only shows the turn child's history
 * and shows all children's history
 */
public class FlipHistoryActivity extends AppCompatActivity {
    private final FlipManager flipManager = FlipManager.getInstance();
    private final ChildrenManager childrenManager = ChildrenManager.getInstance();
    private final Child turnChild = flipManager.getTurnChild();
    private final ArrayList<FlipHistoryEntry> historyList = new ArrayList<>();
    private final HashMap<Long, Bitmap> childBitmaps = new HashMap<>();
    private static final float SCREEN_SIZE_BASE_RATIO = 5.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flip_history);
        setupToolbar();
        setupSwitchButton();
        populateList();
        setupListView();
        resizeToggleSwitch();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_flipHistory);
        toolbar.setTitle(R.string.flip_history);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }

    private void setupSwitchButton() {
        SwitchCompat switchCompat = findViewById(R.id.flipHistory_switch);
        //check if there is no child has been added
        if (turnChild != null) {
            switchCompat.setText(getString(R.string.shows_only_person_name, turnChild.getName()));
        } else {
            switchCompat.setVisibility(View.GONE);
        }

        switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            historyList.clear();
            if (isChecked) {
                //show only turn child's history
                for (FlipHistoryEntry history : flipManager) {
                    if (history.getChildId() == turnChild.getId()) {
                        historyList.add(0, history);
                    }
                }
            } else {
                //show all history
                for (FlipHistoryEntry history : flipManager) {
                    historyList.add(0, history);
                }
            }
            setupListView();
        });
    }

    private void populateList() {
        for (FlipHistoryEntry history : flipManager) {
            historyList.add(0, history);
        }
    }

    private void setupListView() {
        TextView noCoinsFlipped = findViewById(R.id.flipHistory_txtNoCoinsFlipped);

        if (historyList.isEmpty()) {
            noCoinsFlipped.setVisibility(View.VISIBLE);
        } else {
            noCoinsFlipped.setVisibility(View.GONE);
        }

        ArrayAdapter<FlipHistoryEntry> adapter = new HistoryListAdapter();
        ListView list = findViewById(R.id.flipHistory_listView);
        list.setAdapter(adapter);
    }


    private void resizeToggleSwitch() {
        SwitchCompat switchCompat = findViewById(R.id.flipHistory_switch);
        switchCompat.setScaleX(getScreenSizeInInches() / SCREEN_SIZE_BASE_RATIO);
        switchCompat.setScaleY(getScreenSizeInInches() / SCREEN_SIZE_BASE_RATIO);
    }

    private float getScreenSizeInInches() {
        // returns the usable screen size, which is slightly less than the actual screen size but works fine for screen size ratio calculations
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        double widthInch = (double) displayMetrics.widthPixels / (double)displayMetrics.xdpi;
        double heightInch = (double) displayMetrics.heightPixels / (double)displayMetrics.ydpi;

        return (float) Math.sqrt(Math.pow(widthInch, 2) + Math.pow(heightInch, 2));
    }

    private class HistoryListAdapter extends ArrayAdapter<FlipHistoryEntry> {
        HistoryListAdapter() {
            super(FlipHistoryActivity.this, R.layout.list_view_flip_history, historyList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            //make sure we have a view to work with (may have been given null)
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.list_view_flip_history, parent, false);
            }

            FlipHistoryEntry currentHistory = historyList.get(position);

            String coinSideResult;
            if (currentHistory.getResult() == FlipManager.CoinSide.HEADS) {
                coinSideResult = getString(R.string.heads);
            } else {
                coinSideResult = getString(R.string.tails);
            }

            String flipResult;
            Child child = childrenManager.getChild(currentHistory.getChildId());

            if (child != null) {
                String wonOrLost;

                if (currentHistory.wasWon()) {
                    wonOrLost = getString(R.string.won_green);
                } else {
                    wonOrLost = getString(R.string.lost_red);
                }

                flipResult = getString(R.string.flip_result_child, child.getName(), coinSideResult, wonOrLost);
            } else {
                flipResult = getString(R.string.flip_result, coinSideResult);
            }

            TextView txtMain = itemView.findViewById(R.id.flipHistory_txtMain);
            txtMain.setText(getTextFromHtml(flipResult));

            ImageView profileImg = itemView.findViewById(R.id.flipHistory_imgProfile);
            profileImg.setImageBitmap(getChildBitmap(child));

            SimpleDateFormat format = new SimpleDateFormat("MMM dd HH:mm", Locale.getDefault());
            String time = format.format(currentHistory.getDate());

            TextView txtTime = itemView.findViewById(R.id.flipHistory_txtTime);
            txtTime.setText(time);

            return itemView;
        }
    }

    // reduce memory usage by only storing one bitmap in memory for each child
    private Bitmap getChildBitmap(Child child) {
        if(child == null || !child.hasImage()) {
            if(!childBitmaps.containsKey(Child.NONE)) {
                childBitmaps.put(Child.NONE, Child.getDefaultImageBitmap(this));
            }

            return childBitmaps.get(Child.NONE);
        }

        if(!childBitmaps.containsKey(child.getId())) {
            childBitmaps.put(child.getId(), child.getImageBitmap(this));
        }

        return childBitmaps.get(child.getId());
    }

    @Override
    protected void onResume() {
        super.onResume();
        BGMusicPlayer.resumeBgMusic();
    }

    protected static Intent makeIntent(Context context) {
        return new Intent(context, FlipHistoryActivity.class);
    }

    // https://stackoverflow.com/questions/7130619/bold-words-in-a-string-of-strings-xml-in-android
    private Spanned getTextFromHtml(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(text);
        }
    }
}