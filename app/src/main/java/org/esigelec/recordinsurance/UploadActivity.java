package org.esigelec.recordinsurance;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;

import java.io.File;
import java.io.IOException;

public class UploadActivity extends AppCompatActivity {

    private static final int SELECT_VIDEO = 1;
    private static final String TAG = "UploadActivity" ;

    private String selectedVideoPath;
    private ProgressBar spinner;
    private FloatingActionButton fab;
    private ServiceFirebase serviceFirebase;
    private ProcessVideo processVideo;
    private TextView message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        message = (TextView) findViewById(R.id.message);
        serviceFirebase = new ServiceFirebase(this);
        processVideo = new ProcessVideo(this);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, SELECT_VIDEO);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        spinner.setVisibility(View.VISIBLE);
        fab.setEnabled(false);
        message.setText("");
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_VIDEO) {
                selectedVideoPath = Util.getRealPathFromUri(this, data.getData());

                try {
                    File localFile = File.createTempFile("video-lbp-firebase", "txt");
                    FileDownloadTask task = serviceFirebase.getFile(localFile, selectedVideoPath);

                    task.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            try {
                                if(processVideo.compareFile(selectedVideoPath, localFile)) {
                                    message.setText("The integrity has been checked and validated! This video belong to the Driver");
                                } else {
                                    message.setText("This video doesn't belong to the driver");
                                }
                                spinner.setVisibility(View.GONE);
                                fab.setEnabled(true);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            message.setText("This video doesn't belong to the driver");
                            spinner.setVisibility(View.GONE);
                            fab.setEnabled(true);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else {
                spinner.setVisibility(View.GONE);
                fab.setEnabled(true);
            }
        } else {
            spinner.setVisibility(View.GONE);
            fab.setEnabled(true);
        }

        //finish();
    }



    private OnCompleteListener<QuerySnapshot> processComplete() {
        return new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                spinner.setVisibility(View.GONE);
                fab.setEnabled(true);

                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty())
                        Toast.makeText(getApplicationContext(), "The integrity has been checked and validated! This video belong to the Driver", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplicationContext(), "This video doesn't belong to the driver", Toast.LENGTH_SHORT).show();

                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        };
    }
}
