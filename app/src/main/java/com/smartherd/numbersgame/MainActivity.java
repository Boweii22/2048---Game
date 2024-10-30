package com.smartherd.numbersgame;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.CountDownTimer;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;

import com.smartherd.xno.R;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private int[][] gameBoard = new int[4][4];
    private Random random = new Random();
    private GestureDetector gestureDetector;
    private int score = 0;
    private TextView scoreTextView;
    private TextView bestScoreTextView;
    private CountDownTimer gameTimer;
    private long remainingTime = 60000; // Example: 60 seconds
    private int currentScore = 0;
    private static final String BEST_SCORE_KEY = "best_score";
    private SharedPreferences sharedPreferences;
    private TextView timerTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);


        // Set the status bar color
        setStatusBarColor(getResources().getColor(R.color.main));

        gridLayout = findViewById(R.id.gridLayout);
        FrameLayout resetButton = findViewById(R.id.resetButton);

        scoreTextView = findViewById(R.id.score);
        bestScoreTextView = findViewById(R.id.best_score_value);
        updateScore(0); // Initialize score display

        timerTextView = findViewById(R.id.timerTextView);

        resetButton.setOnClickListener(v -> resetGame());
        resetGame();

        sharedPreferences = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        updateBestScoreDisplay();
        startGameTimer();

        gestureDetector = new GestureDetector(this, new GestureListener());
    }


    private void startGameTimer() {
        gameTimer = new CountDownTimer(60000, 1000) { // 60 seconds timer
            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                timerTextView.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                checkAndSaveBestScore();
                showTimeUpDialog();
            }
        };
        gameTimer.start();
    }


    private void showTimeUpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_time_is_up, null);

        TextView dialogBestScoreText = dialogView.findViewById(R.id.dialogBestScoreTextView);
        dialogBestScoreText.setText("Best Score: " + sharedPreferences.getInt("best_score", Integer.parseInt(scoreTextView.getText().toString())));

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        dialogView.findViewById(R.id.playAgainButton).setOnClickListener(v -> {
            dialog.dismiss();
            resetGame();
            startGameTimer();
        });

        dialog.show();
    }



    private void updateBestScoreDisplay() {
        int bestScore = sharedPreferences.getInt("best_score", 0);
        bestScoreTextView.setText(String.valueOf(bestScore));
    }


    private void checkAndSaveBestScore() {
        int bestScore = sharedPreferences.getInt("best_score", 0);
        if (score > bestScore) {
            sharedPreferences.edit().putInt("best_score", score).apply();
            updateBestScoreDisplay();
        }
    }





    private void updateScore(int points) {
        score += points;
        scoreTextView.setText(String.valueOf(score)); // Ensures score is treated as text
    }

    private void setStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(color);
    }

    private void resetGame() {
        score = 0;
        updateScore(score); //to reset the score display
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                gameBoard[i][j] = 0;
            }
        }
        addRandomTile();
        addRandomTile();
        updateUI();
    }

    private void addRandomTile() {
        int x, y;
        do {
            x = random.nextInt(4);
            y = random.nextInt(4);
        } while (gameBoard[x][y] != 0);
        gameBoard[x][y] = random.nextBoolean() ? 2 : 4;
    }

    private void updateUI() {
        gridLayout.removeAllViews();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                TextView tile = new TextView(this);
                tile.setTextColor(Color.parseColor("#000000"));
                tile.setTextSize(32);
                tile.setGravity(Gravity.CENTER);
                tile.setBackgroundResource(R.drawable.rounded_tile); //to set the rounded background
//                tile.setBackgroundColor(getColorForValue(gameBoard[i][j]));
                // Apply dynamic color to the drawable
                GradientDrawable background = (GradientDrawable) tile.getBackground();
                background.setColor(getColorForValue(gameBoard[i][j]));
                tile.setText(gameBoard[i][j] == 0 ? "" : String.valueOf(gameBoard[i][j]));
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 0;
                params.columnSpec = GridLayout.spec(j, 1f);
                params.rowSpec = GridLayout.spec(i, 1f);
                params.setMargins(8, 8, 8, 8);
                tile.setLayoutParams(params);
                gridLayout.addView(tile);

                // Add animations for new tiles
                tile.setAlpha(0f);
                tile.animate().alpha(1f).setDuration(300).start();
            }
        }
    }

    private int getColorForValue(int value) {
        switch (value) {
            case 0: return 0xFFEFEFEF; // Empty
            case 2: return 0xFFEEE4DA; // 2
            case 4: return 0xFFEDE0C8; // 4
            case 8: return 0xFFF2B179; // 8
            case 16: return 0xFFF59563; // 16
            case 32: return 0xFFF67C5F; // 32
            case 64: return 0xFFF67C5F; // 64
            case 128: return 0xFFEDCF72; // 128
            case 256: return 0xFFEDCC61; // 256
            case 512: return 0xFFEDC850; // 512
            case 1024: return 0xFFEDC53F; // 1024
            case 2048: return 0xFFEDC22E; // 2048
            default: return 0xFF3C3A32; // Higher values
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        moveRight();
                    } else {
                        moveLeft();
                    }
                    return true;
                }
            } else {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        moveDown();
                    } else {
                        moveUp();
                    }
                    return true;
                }
            }
            return false;
        }
    }

    private void moveUp() {
        for (int j = 0; j < 4; j++) {
            for (int i = 1; i < 4; i++) {
                if (gameBoard[i][j] != 0) {
                    int k = i;
                    while (k > 0 && gameBoard[k - 1][j] == 0) {
                        gameBoard[k - 1][j] = gameBoard[k][j];
                        gameBoard[k][j] = 0;
                        k--;
                    }
                    if (k > 0 && gameBoard[k - 1][j] == gameBoard[k][j]) {
                        gameBoard[k - 1][j] *= 2;
                        gameBoard[k][j] = 0;

                        // Update score with the value of the merged tile
                        updateScore(gameBoard[k - 1][j]);
                    } else if (k == 0 && gameBoard[k][j] != 0) {
                        // Update score for the tile at the top if it hasn't been merged
                        updateScore(gameBoard[k][j]);
                    }
                }
            }
        }
        if (checkGameOver()) {
            showGameOverDialog();
        }
        addRandomTile();
        updateUI();
    }


    private void moveDown() {
        for (int j = 0; j < 4; j++) {
            for (int i = 2; i >= 0; i--) {
                if (gameBoard[i][j] != 0) {
                    int k = i;
                    while (k < 3 && gameBoard[k + 1][j] == 0) {
                        gameBoard[k + 1][j] = gameBoard[k][j];
                        gameBoard[k][j] = 0;
                        k++;
                    }
                    if (k < 3 && gameBoard[k + 1][j] == gameBoard[k][j]) {
                        gameBoard[k + 1][j] *= 2;
                        gameBoard[k][j] = 0;

                        // Update score with the value of the merged tile
                        updateScore(gameBoard[k + 1][j]);

                    }
                }
            }
        }
        if (checkGameOver()) {
            showGameOverDialog();
        }
        addRandomTile();
        updateUI();
    }

    private void moveLeft() {
        for (int i = 0; i < 4; i++) {
            for (int j = 1; j < 4; j++) {
                if (gameBoard[i][j] != 0) {
                    int k = j;
                    while (k > 0 && gameBoard[i][k - 1] == 0) {
                        gameBoard[i][k - 1] = gameBoard[i][k];
                        gameBoard[i][k] = 0;
                        k--;
                    }
                    if (k > 0 && gameBoard[i][k - 1] == gameBoard[i][k]) {
                        gameBoard[i][k - 1] *= 2;
                        gameBoard[i][k] = 0;

                        // Update score with the value of the merged tile
                        updateScore(gameBoard[k - 1][j]);
                    }
                }
            }
        }
        if (checkGameOver()) {
            showGameOverDialog();
        }
        addRandomTile();
        updateUI();
    }

    private void moveRight() {
        for (int i = 0; i < 4; i++) {
            for (int j = 2; j >= 0; j--) {
                if (gameBoard[i][j] != 0) {
                    int k = j;
                    while (k < 3 && gameBoard[i][k + 1] == 0) {
                        gameBoard[i][k + 1] = gameBoard[i][k];
                        gameBoard[i][k] = 0;
                        k++;
                    }
                    if (k < 3 && gameBoard[i][k + 1] == gameBoard[i][k]) {
                        gameBoard[i][k + 1] *= 2;
                        gameBoard[i][k] = 0;

                        // Update score with the merged value
                        updateScore(gameBoard[i][k + 1]);
                    }
                }
            }
        }
        if (checkGameOver()) {
            showGameOverDialog();
        }
        addRandomTile();
        updateUI();
    }

    private void showGameOverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_game_over, null);

        TextView dialogBestScoreText = dialogView.findViewById(R.id.dialogBestScoreTextView);
        dialogBestScoreText.setText("Best Score: " + sharedPreferences.getInt(BEST_SCORE_KEY, 0));

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        dialogView.findViewById(R.id.playAgainButton).setOnClickListener(v -> {
            dialog.dismiss();
            resetGame();
            startGameTimer();
        });

        dialog.show();
    }


    private boolean checkGameOver() {
        // Check for empty spaces
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (gameBoard[i][j] == 0) {
                    return false; // Empty space found, game is not over
                }
            }
        }

        // Check for possible merges in rows
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                if (gameBoard[i][j] == gameBoard[i][j + 1]) {
                    return false; // Adjacent tiles in the row can be merged
                }
            }
        }

        // Check for possible merges in columns
        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 3; i++) {
                if (gameBoard[i][j] == gameBoard[i + 1][j]) {
                    return false; // Adjacent tiles in the column can be merged
                }
            }
        }

        return true; // No moves available, game is over
    }

}