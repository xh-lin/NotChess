package edu.umb.cs.notchess;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class GameOverDialogActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog_game_over);

        Intent intent = getIntent();
        String winnerText = intent.getStringExtra(getString(R.string.winner_text));
        ((TextView) findViewById(R.id.WinnerTextView)).setText(winnerText);
    }

    public void backToNavigation(View view) {
        Intent intent = new Intent(this, NavigationActivity.class);
        startActivity(intent);
    }

    public void levelReset(View view) {
        Intent intent = new Intent(this, NavigationActivity.class);
        startActivity(intent);
        intent = new Intent(this, LevelActivity.class);
        startActivity(intent);
    }
}
