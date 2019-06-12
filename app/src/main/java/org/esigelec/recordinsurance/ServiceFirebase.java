package org.esigelec.recordinsurance;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

class ServiceFirebase {

    private FirebaseFirestore db;
    private StorageReference storageRef;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private Activity activity;
    private static final String TAG = "ServiceFirebase";
    private static final String FOLDER = "insurance/";

    public  ServiceFirebase( Activity activity) {

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        this.activity = activity;
        signInAnonymously();

    }

    public void put(String document, String filePath) {
        File file = new File(filePath);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        String hash = Util.SHAsum(bytes);

        Map<String, Object> fileDocument = new HashMap<>();
        fileDocument.put("hash", hash);
        fileDocument.put("size", size / 1024);
        fileDocument.put("filename", file.getName());
        fileDocument.put("path", file.getAbsolutePath());
        fileDocument.put("timestamp", FieldValue.serverTimestamp());



        // Add a new document with a generated ID
        db.collection(document)
                .add(fileDocument)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

    }

    public void putFile(File fileLbp, String name) {
        Uri file = Uri.fromFile(fileLbp);


        StorageReference riversRef = storageRef.child(FOLDER+name+".txt");

        riversRef.putFile(file);
    }

    public Task<QuerySnapshot> checkIntegrity(String document, File file) {

        deletePreviousDate();

        int size = (int) file.length();
        byte[] bytes = new byte[size];
        String hash = Util.SHAsum(bytes);
        Log.d(TAG, "Hash: "+hash);
        boolean isEmpty;

        return db.collection(document)
                .whereEqualTo("hash", hash)
                .get();

    }

    private void deletePreviousDate() {


        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1);

        Timestamp time = new Timestamp(cal.getTime());

        db.collection("records").whereLessThan("timestamp", time)
                .get()
                .addOnCompleteListener(deleteOnComplete());

        db.collection("completeRecords").whereLessThan("timestamp", time)
                .get()
                .addOnCompleteListener(deleteOnComplete());


    }

    private OnCompleteListener<QuerySnapshot> deleteOnComplete() {
        return new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot result = task.getResult();
                for(DocumentSnapshot doc : result.getDocuments()) {
                    doc.getReference().delete();
                }

            }
        };
    }

    public FileDownloadTask getFile(File tempFile, String selectedVideoPath) throws IOException {
        String fileName = Util.getFileName(selectedVideoPath);

        String child =FOLDER+fileName+".txt";

        return storageRef.child(child).getFile(tempFile);


    }

    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnSuccessListener(activity, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

            }
        });
    }
}
