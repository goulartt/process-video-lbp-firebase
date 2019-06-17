package org.esigelec.recordinsurance;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;

public class Util {

    public static String SHAsum(byte[] convertme) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return byteArray2Hex(md.digest(convertme));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;

    }

    private static String byteArray2Hex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static ArrayList<Bitmap> getFramesBitmap(Context context, String path) {

        File videoFile = new File(path);
        Uri videoFileUri = Uri.parse(videoFile.toString());


        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoFile.toString());
        ArrayList<Bitmap> rev=new ArrayList<Bitmap>();

        //Create a new Media Player
        MediaPlayer mp = MediaPlayer.create(context, videoFileUri);

        int millis = mp.getDuration();

        for(int i=1000000;i<millis*1000;i+=5000000)
        {
            Bitmap bitmap=retriever.getFrameAtTime(i,MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            rev.add(Bitmap.createScaledBitmap(bitmap, 240, 240, false));
        }

        return rev;
    }
    public static String getFileName(String path) {
        String[] split = path.split("/");
        path = split[split.length-1];
        return path;
    }

    public static ArrayList<Bitmap> getFramesBitmapWithStart(Context baseContext, String path, int start) {

        File videoFile = new File(path);
        Uri videoFileUri = Uri.parse(videoFile.toString());


        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoFile.toString());
        ArrayList<Bitmap> rev=new ArrayList<Bitmap>();

        //Create a new Media Player
        MediaPlayer mp = MediaPlayer.create(baseContext, videoFileUri);

        int millis = mp.getDuration();

        for(int i=start;i<millis*1000;i+=5000000)
        {
            Bitmap bitmap=retriever.getFrameAtTime(i,MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            rev.add(bitmap);
        }

        return rev;
    }
}
