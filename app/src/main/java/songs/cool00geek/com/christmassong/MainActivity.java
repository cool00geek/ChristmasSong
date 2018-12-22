package songs.cool00geek.com.christmassong;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final int SONG_DIR_REQUEST_CODE = 42;
    private static final int TRAIN_DIR_REQUEST_CODE = 43;

    private static String SongDir;
    private static String TrainDir;

    private SharedPreferences mPreferences;
    private String sharedPrefFile = "com.cool00geek.songsPrefs";

    private final MediaPlayer SongPlayer = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        SongDir = mPreferences.getString("songs", "");
        TrainDir = mPreferences.getString("trains", "");

        setContentView(R.layout.activity_main);

        TextView songDirText = findViewById(R.id.mainSongDirText);
        songDirText.setText("Using Song Directory: " + SongDir);
        TextView trainDirText = findViewById(R.id.chooDirText);
        trainDirText.setText("Using Train Sound Directory: " + TrainDir);

    }

    public void playSongs(View v) throws IOException {
        String extStore = System.getenv("EXTERNAL_STORAGE");
        File songDir = new File(extStore + "/" + SongDir);
        File trainDir = new File(extStore + "/" + TrainDir);

        if (songDir.exists() && trainDir.exists() && songDir.isDirectory() && trainDir.isDirectory()){
            File[] songArr = songDir.listFiles();
            File[] trainArr = trainDir.listFiles();

            final ArrayList<File> songList = new ArrayList<>(Arrays.asList(songArr));
            final ArrayList<File> trainList = new ArrayList<>(Arrays.asList(trainArr));

            int startSongIndex = (int)(Math.random() * songList.size());
            SongPlayer.setDataSource(songList.get(startSongIndex).getAbsolutePath());
            SongPlayer.prepare();
            SongPlayer.start();

            SongPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    try {
                        MediaPlayer trainPlayer = new MediaPlayer();
                        int random = (int)(Math.random() * trainList.size());
                        trainPlayer.setDataSource(trainList.get(random).getAbsolutePath());
                        trainPlayer.prepare();
                        trainPlayer.start();
                        trainPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                SongPlayer.reset();
                                int rand = (int)(Math.random() * songList.size());
                                try {
                                    SongPlayer.setDataSource(songList.get(rand).getAbsolutePath());
                                    SongPlayer.prepare();
                                } catch (IOException e) {
                                    try {
                                        SongPlayer.setDataSource(songList.get(rand).getAbsolutePath());
                                        SongPlayer.prepare();
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                                SongPlayer.start();
                                playBackgroundTrain(trainList);
                            }
                        });


                    } catch (IOException e) {
                        Log.e("Broke", e.getMessage());
                        e.printStackTrace();
                        finish();
                    }
                }
            });

            /*try {
                if (SongPlayer.isPlaying() && trainList.size() != 0){
                    playBackgroundTrain(trainList);

                }
            } catch (Exception e){
                finish();
            }*/
        }
    }

    private void playBackgroundTrain(final ArrayList<File> trainList){
        int secondsBeforePlaying = (int)(Math.random()*30 + 60);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MediaPlayer trainPlayer = new MediaPlayer();
                int random = (int)(Math.random() * trainList.size());
                try {
                    if (SongPlayer.isPlaying()) {
                        trainPlayer.setDataSource(trainList.get(random).getAbsolutePath());

                        if ((int)(Math.random() * 3) == 1){
                            SongPlayer.setVolume((float) 0, (float) 0);
                        } else {
                            SongPlayer.setVolume((float) 0.5, (float) 0.5);
                        }

                        trainPlayer.prepare();
                        trainPlayer.start();
                        trainPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                SongPlayer.setVolume(1, 1);
                                //playBackgroundTrain(trainList);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, secondsBeforePlaying * 1000);

    }

    public void choseSongDir(View v) {
        chooseDir(SONG_DIR_REQUEST_CODE);
    }

    public void choseTrainDir(View v) {
        chooseDir(TRAIN_DIR_REQUEST_CODE);
    }

    private void chooseDir(int requestCode) {
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        // intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        //intent.setType("image/*");

        startActivityForResult(intent, requestCode);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();

        Uri uri = null;

        if (requestCode == SONG_DIR_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().


            if (resultData != null) {
                uri = resultData.getData();
                String path = uri.getPath();
                SongDir = path.split(":")[1];

                Log.i("Got Dir:", "Uri: " + SongDir);
                TextView songDirText = findViewById(R.id.mainSongDirText);
                songDirText.setText("Using Song Directory: " + SongDir);

                preferencesEditor.putString("songs", SongDir);
            }
        } else if (requestCode == TRAIN_DIR_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                uri = resultData.getData();
                String path = uri.getPath();
                TrainDir = path.split(":")[1];

                Log.i("Got Dir:", "Uri: " + TrainDir);
                TextView trainDirText = findViewById(R.id.chooDirText);
                trainDirText.setText("Using Train Sound Directory: " + TrainDir);

                preferencesEditor.putString("trains", TrainDir);
            }
        }
        preferencesEditor.apply();


        String extStore = System.getenv("EXTERNAL_STORAGE");

        File file = new File(extStore + "/" + SongDir);
        if (file.exists() && file.isDirectory()) {
            File[] listOfFiles = file.listFiles();
            for (File dirFile : listOfFiles) {
                Log.i("Files in folder", dirFile.getAbsolutePath());
            }
        }
    }

    public void stopMusic(View v){
        try {
            if (SongPlayer.isPlaying()) {
                SongPlayer.stop();
                SongPlayer.reset();
            }
        } catch (Exception e){
            // It is already stopped and released
        }
    }

    public void skipSong(View v){
        try {
            if (SongPlayer.isPlaying()){
                stopMusic(v);
                playSongs(v);
            }
        } catch (Exception e){
            // Player isn't playing or has been released
        }
    }

}
