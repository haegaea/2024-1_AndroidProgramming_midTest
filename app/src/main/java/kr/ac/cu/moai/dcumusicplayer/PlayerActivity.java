package kr.ac.cu.moai.dcumusicplayer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.util.Objects;

public class PlayerActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private Button playBtn;
    private Button gobackBtn;
    private SeekBar seekBar;
    private TextView musicTime;

    int playPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        playBtn = (Button) findViewById(R.id.playBtn);
        gobackBtn = (Button) findViewById(R.id.goback);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        musicTime = (TextView) findViewById(R.id.tvDurationPos);

        Intent intent = getIntent();
        String mp3file = intent.getStringExtra("mp3");
        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            ImageView ivCover = findViewById(R.id.ivCover);
            retriever.setDataSource(mp3file);

            byte[] b = retriever.getEmbeddedPicture();
            Bitmap cover = BitmapFactory.decodeByteArray(b, 0, b.length);
            ivCover.setImageBitmap(cover);

            TextView tvTitle = findViewById(R.id.tvTitle);
            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            tvTitle.setText(title);

            TextView tvDuration = findViewById(R.id.tvDuration);
            tvDuration.setText(ListViewMP3Adapter.getDuration(retriever));

            TextView tvArtist = findViewById(R.id.tvArtist);
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            tvArtist.setText(artist);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(mp3file);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else if (mediaPlayer != null && mediaPlayer.isPlaying() == false) {
                    mediaPlayer.seekTo(playPos);
                    mediaPlayer.start();
                } else if (mediaPlayer != null && mediaPlayer.isPlaying() == true) {
                    mediaPlayer.pause();
                    playPos = mediaPlayer.getCurrentPosition();
                    mediaPlayer.seekTo(playPos);
                }
                seekBar.setMax(mediaPlayer.getDuration());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(mediaPlayer.isPlaying()) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            int TimePos = mediaPlayer.getCurrentPosition() / 1000;
                            int TimeMax = mediaPlayer.getDuration() / 1000;
                            seekBar.setProgress(mediaPlayer.getCurrentPosition());
                            if(TimePos > 60) {
                                if((TimePos % 60) < 10) {
                                    musicTime.setText(TimePos / 60 + ":" + 0 + TimePos % 60 + "/" + TimeMax / 60 + ":" + TimeMax % 60);
                                }
                                else
                                    musicTime.setText(TimePos / 60 + ":" + TimePos % 60 + "/" + TimeMax / 60 + ":" + TimeMax % 60);
                            }
                            else if(TimePos < 10)
                                musicTime.setText(0 + ":" + 0 + TimePos + "/" + TimeMax / 60 + ":" + TimeMax % 60);
                            else
                                musicTime.setText(0 + ":" + TimePos + "/" + TimeMax / 60 + ":" + TimeMax % 60);
                        }
                    }
                }).start();
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { // 시크바 이벤트 처리
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(fromUser && mediaPlayer != null) { // 시크바를 터치하고, mediaplayer 객체가 존재하면,
                            mediaPlayer.seekTo(progress); // 원하는 위치부터 음악 시작
                        }
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
            }
        });
        gobackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying() == true) {
                    mediaPlayer.stop();
                    finish();
                }
            }
        });
    }
}