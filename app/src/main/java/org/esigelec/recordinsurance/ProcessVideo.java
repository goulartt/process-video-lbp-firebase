package org.esigelec.recordinsurance;

import android.app.Activity;
import android.graphics.Bitmap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ProcessVideo {

    private FileOutputStream fos;
    private ServiceFirebase serviceFirebase;
    private Activity activity;

    public ProcessVideo(Activity activity) {
        serviceFirebase = new ServiceFirebase(activity);
        this.activity = activity;
    }


    public File processLbp(String path, Boolean save) throws IOException {

        String fileName = Util.getFileName(path);
        ArrayList<Bitmap> framesBitmap = Util.getFramesBitmap(activity.getBaseContext(), path);


        File videoLbp = new File(activity.getFilesDir() + "video-lbp.txt");
        videoLbp.delete();
        fos = new FileOutputStream(videoLbp, true);

        for (Bitmap frame : framesBitmap) {
            String sign = getLbpSign(frame);
            fos.write(sign.getBytes());
        }

        fos.close();

        if (save)
            serviceFirebase.putFile(videoLbp, fileName);

        return videoLbp;
    }



    private String getLbpSign(Bitmap frame) {

        StringBuilder sb = new StringBuilder();
        int[][] imageArray = new int[frame.getWidth()][frame.getHeight()];
        int currentPixelValue, newPixelValue;

        for(int row=0; row < frame.getWidth(); row++){
            for(int col=0; col < frame.getHeight(); col++){

                imageArray[row][col]= frame.getPixel(row,col);
            }
        }

        for(int row=1; row<frame.getWidth()-1; row++){
            for(int col=1; col<frame.getHeight()-1; col++){
                currentPixelValue=imageArray[row][col];
                newPixelValue=0;
                if(imageArray[row-1][col-1]>currentPixelValue) newPixelValue=newPixelValue+1;
                if(imageArray[row-1][col]>currentPixelValue) newPixelValue=newPixelValue+2;
                if(imageArray[row-1][col+1]>currentPixelValue) newPixelValue=newPixelValue+4;
                if(imageArray[row][col+1]>currentPixelValue) newPixelValue=newPixelValue+8;
                if(imageArray[row+1][col+1]>currentPixelValue) newPixelValue=newPixelValue+16;
                if(imageArray[row+1][col]>currentPixelValue) newPixelValue=newPixelValue+32;
                if(imageArray[row+1][col-1]>currentPixelValue) newPixelValue=newPixelValue+64;
                if(imageArray[row][col-1]>currentPixelValue) newPixelValue=newPixelValue+128;
                sb.append(newPixelValue);
            }
        }

        sb.append(System.lineSeparator());
        return sb.toString();
    }

    public Boolean compareFile(String selectedVideoPath, File fileFirebase) throws IOException {

        if (fileFirebase.length() == 0)
            return false;

        File localFile = processLbp(selectedVideoPath, false);


        return isEqualFiles(localFile, fileFirebase);

    }

    private Boolean isEqualFiles(File localFile, File fileFirebase) throws IOException {

        BufferedReader local = new BufferedReader(new FileReader(localFile));

        BufferedReader firebase = new BufferedReader(new FileReader(fileFirebase));

        String line1 = local.readLine();

        String line2 = firebase.readLine();

        boolean areEqual = true;

        int lineNum = 1;

        while (line1 != null || line2 != null)
        {
            if(line1 == null || line2 == null)
            {
                areEqual = false;

                break;
            }
            else if(! line1.equalsIgnoreCase(line2))
            {
                areEqual = false;

                break;
            }

            line1 = local.readLine();

            line2 = firebase.readLine();

            lineNum++;
        }



        local.close();

        firebase.close();
        fileFirebase.delete();
        localFile.delete();

        return areEqual;

    }

    public void processFileLbpContinuasly(Activity activity, String path, int start) throws IOException {
        String fileName = Util.getFileName(path);
        ArrayList<Bitmap> framesBitmap = Util.getFramesBitmapWithStart(activity.getBaseContext(), path, start);


        File videoLbp = new File(activity.getFilesDir() + "video-lbp.txt");
        videoLbp.delete();
        fos = new FileOutputStream(videoLbp, true);

        for (Bitmap frame : framesBitmap) {
            String sign = getLbpSign(frame);
            fos.write(sign.getBytes());
        }

        fos.close();

        serviceFirebase.putFile(videoLbp, fileName+"-"+start);

    }
}
