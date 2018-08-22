package peru.proj1;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Color;
import android.graphics.drawable.LayerDrawable;
import android.graphics.BitmapRegionDecoder;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.Utils;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import android.util.Log;
import android.widget.Toast;
import org.opencv.core.TermCriteria;
import java.util.Map;
import org.opencv.core.Rect;
import java.nio.ByteBuffer;


//import android.widget.TextView;


public class MainActivity extends Activity {


//    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
//        @Override
//        public void onManagerConnected(int status) {
//            switch (status) {
//                case LoaderCallbackInterface.SUCCESS: {
//                    Log.i("MainActivity", "OpenCV loaded successfully");
//                }
//                break;
//                default: {
//                    super.onManagerConnected(status);
//                }
//                break;
//            }
//        }
//    };
   private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private Button btnSelect,send_t;
    private ImageView ivImage;
   //private TextView tv_data,tv_data1,tv_data2;
    private String userChoosenTask;
    public static double red=0,green=0,blue=0;
    static double variance=0.0,IDM = 0.0,contrast=0.0,energy = 0.0,entropy = 0.0,homogeneity = 0.0;
    static double correlation=0.0,sum = 0.0,prominence=0.0,shade=0.0,inertia=0.0,mean=0.0;
    static  double stdevx=0.0,gbyr=0,gbyb=0;
    static  double stdevy=0.0;

    int pixel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(OpenCVLoader.initDebug())
        {
            Toast.makeText(getApplicationContext(),"openCv loaded successfully",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"Not Loaded",Toast.LENGTH_SHORT).show();

        }
        send_t = (Button) findViewById(R.id.send_text);
        send_t.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent myIntent = new Intent(MainActivity.this,
                        Main2Activity.class);
                startActivity(myIntent);
            }
        });
        btnSelect = (Button) findViewById(R.id.btnSelectPhoto);
        btnSelect.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        ivImage = (ImageView) findViewById(R.id.ivImage);

        Thread myThread = new Thread(new MyServer());
        myThread.start();
        /* tv_data = (TextView) findViewById(R.id.text_view);
        tv_data1 = (TextView) findViewById(R.id.text_view_green);
        tv_data2 = (TextView) findViewById(R.id.text_view_blue);
*/    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
      //  OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }



    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result=Utility.checkPermission(MainActivity.this);

                if (items[item].equals("Take Photo")) {
                    userChoosenTask ="Take Photo";
                    if(result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask ="Choose from Library";
                    if(result)
                        galleryIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {


        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        Bitmap new_img = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //assign your bitmap here
        int redColors = 0;
        int greenColors = 0;
        int blueColors = 0;
        int pCount = 0;

        for (int y = 0; y < thumbnail.getHeight(); y++)
        {
            for (int x = 0; x < thumbnail.getWidth(); x++)
            {
                int c = thumbnail.getPixel(x, y);
                pCount++;
                redColors += Color.red(c);
                greenColors += Color.green(c);
                blueColors += Color.blue(c);
            }
        }
        // calculate average of bitmap r,g,b values
        red = (redColors/pCount);
        green = (greenColors/pCount);
        blue = (blueColors/pCount);

        gbyr=green/red;
        gbyb=green/blue;





        Mat mat=new Mat(thumbnail.getHeight(),thumbnail.getWidth(), CvType.CV_8UC3);
        Bitmap bmp32 = thumbnail.copy(Bitmap.Config.RGB_565, true);
        Utils.bitmapToMat(bmp32, mat);
        Mat mat1 =new Mat(thumbnail.getHeight(),thumbnail.getWidth(), CvType.CV_8UC3);

       Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY,3);

        Rect roi = new Rect(0, 0, mat.cols() - 1, mat.rows() - 1);
        mat.submat(roi);
        Mat imgroi = new Mat();
        mat.copyTo(imgroi);
        Utils.matToBitmap(imgroi, new_img);
        boolean symmetry;
        int phi;
        // get byte arrays for the image pixels and mask pixels
        int width = thumbnail.getWidth();
        int height = thumbnail.getHeight();



//        byte [] mask = thumbnail.getMaskArray();

        int size = thumbnail.getRowBytes() * thumbnail.getHeight();
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        thumbnail.copyPixelsToBuffer(byteBuffer);
        byteBuffer.rewind();
        byte[] pixels = byteBuffer.array();
        int size1 = new_img.getRowBytes() * new_img.getHeight();
        ByteBuffer byteBuffer1 = ByteBuffer.allocate(size1);
        new_img.copyPixelsToBuffer(byteBuffer1);
        byteBuffer1.rewind();
        byte[] mask = byteBuffer1.array();
        // value = value at pixel of interest; dValue = value of pixel at offset
        int value;
        int dValue;
        double totalPixels = roi.height*roi.width;
        totalPixels = totalPixels * 2;
        double pixelProgress = 0;
        double pixelCount = 0;

        //====================================================================================================
        // compute the Gray Level Correlation Matrix

        int offsetX = 1;
        int offsetY = 0;
        double[][] glcm = new double [256][256];
        int d = 1;
        phi = 0;
        symmetry = true;
        double rad = Math.toRadians(-1.0 * phi);
        offsetX = (int) ((int) d* Math.round(Math.cos(rad)));
        offsetY = (int) ((int) d* Math.round(Math.sin(rad)));


        // loop through the pixels in the ROI b
        //ounding rectangle
        for (int y=roi.y; y<(roi.y + roi.height); y++) 	{
            for (int x=roi.x; x<(roi.x + roi.width); x++)	 {
                // check to see if the pixel is in the mask (if it exists)
                if ((mask == null) || ((0xff & mask[(((y-roi.y)*roi.width)+(x-roi.x))]) > 0) ) {
                    // check to see if the offset pixel is in the roi
                    int dx = x + offsetX;
                    int dy = y + offsetY;
                    if ( ((dx >= roi.x) && (dx < (roi.x+roi.width))) && ((dy >= roi.y) && (dy < (roi.y+roi.height))) ) {
                        // check to see if the offset pixel is in the mask (if it exists)
                        if ((mask == null) || ((0xff & mask[(((dy-roi.y)*roi.width)+(dx-roi.x))]) > 0) ) {
                            value = 0xff & pixels[(y*width)+x];
                            dValue = 0xff & pixels[(dy*width) + dx];
                            glcm [value][dValue]++;
                            pixelCount++;
                        }
                        // if symmetry is selected, invert the offsets and go through the process again
                        if (symmetry) {
                            dx = x - offsetX;
                            dy = y - offsetY;
                            if ( ((dx >= roi.x) && (dx < (roi.x+roi.width))) && ((dy >= roi.y) && (dy < (roi.y+roi.height))) ) {
                                // check to see if the offset pixel is in the mask (if it exists)
                                if ((mask == null) || ((0xff & mask[(((dy-roi.y)*roi.width)+(dx-roi.x))]) > 0) ) {
                                    value = 0xff & pixels[(y*width)+x];
                                    dValue = 0xff & pixels[(dy*width) + dx];
                                    glcm [dValue][value]++;
                                    pixelCount++;
                                }
                            }
                        }
                    }
                }
                pixelProgress++;
                //IJ.showProgress(pixelProgress/totalPixels);
            }
        }

        // convert the GLCM from absolute counts to probabilities
        for (int i=0; i<256; i++) {
            for (int j = 0; j < 256; j++) {
                glcm[i][j] = (glcm[i][j]) / (pixelCount);
            }
        }

        double [] px = new double [256];
        double [] py = new double [256];
        double meanx=0.0;
        double meany=0.0;


        // Px(i) and Py(j) are the marginal-probability matrix; sum rows (px) or columns (py)
        // First, initialize the arrays to 0
        for (int i=0;  i<256; i++){
            px[i] = 0.0;
            py[i] = 0.0;
        }

        // sum the glcm rows to Px(i)
        for (int i=0;  i<256; i++) {
            for (int j=0; j<256; j++) {
                px[i] += glcm [i][j];
            }
        }

        // sum the glcm rows to Py(j)
        for (int j=0;  j<256; j++) {
            for (int i=0; i<256; i++) {
                py[j] += glcm [i][j];
            }
        }

        // calculate meanx and meany
        for (int i=0;  i<256; i++) {
            meanx += (i*px[i]);
            meany += (i*py[i]);
        }

        // calculate stdevx and stdevy
        for (int i=0;  i<256; i++) {
            stdevx += ((Math.pow((i-meanx),2))*px[i]);
            stdevy += ((Math.pow((i-meany),2))*py[i]);
        }


    //IDM
        for (int i=0;  i<256; i++)  {
            for (int j=0; j<256; j++) {
                IDM += ((1/(1+(Math.pow(i-j,2))))*glcm[i][j]);
            }
        }

    //Contrast
        for (int i=0;  i<256; i++)  {
            for (int j=0; j<256; j++) {
                //contrast += Math.pow(Math.abs(i-j),2)*(glcm[i][j]);
                contrast += Math.pow(i-j,2)*(glcm[i][j]); // 20110530
            }
        }


     //Energy
        for (int i=0;  i<256; i++)  {
            for (int j=0; j<256; j++) {
                energy += Math.pow(glcm[i][j],2);
            }
        }

    //Entropy


        for (int i=0;  i<256; i++)  {
            for (int j=0; j<256; j++) {
                if (glcm[i][j] != 0) {
                    entropy = entropy-(glcm[i][j]*(Math.log(glcm[i][j])));

                }
            }
        }

    //homogenity

        for (int i=0;  i<256; i++) {
            for (int j=0; j<256; j++) {
                homogeneity += glcm[i][j]/(1.0+Math.abs(i-j));
            }
        }

     //Variance
        mean = (meanx + meany)/2;


        for (int i=0;  i<256; i++)  {
            for (int j=0; j<256; j++) {
                variance += (Math.pow((i-mean),2)* glcm[i][j]);
            }
        }

     //correlation

        for (int i=0;  i<256; i++) {
            for (int j=0; j<256; j++) {
                //Walker, et al. 1995 (matches Xite)
                //correlation += ((((i-meanx)*(j-meany))/Math.sqrt(stdevx*stdevy))*glcm[i][j]);
                //Haralick, et al. 1973 (continued below outside loop; matches original GLCM_Texture)
                //correlation += (i*j)*glcm[i][j];
                //matlab's rephrasing of Haralick 1973; produces the same result as Haralick 1973
                correlation += ((((i-meanx)*(j-meany))/( stdevx*stdevy))*glcm[i][j]);
            }
        }

        //sum

        for (int i=0; i<256; i++)  {
            for (int j=0; j<256; j++) {
                sum = sum + glcm[i][j];
            }
        }

        //prominence


        for (int i=0;  i<256; i++) {
            for (int j=0; j<256; j++) {
                prominence += (Math.pow((i+j-meanx-meany),4)*glcm[i][j]);
            }
        }

        //shade

        for (int i=0;  i<256; i++) {
            for (int j=0; j<256; j++) {
                shade += (Math.pow((i+j-meanx-meany),3)*glcm[i][j]);
            }
        }

        //inertia

        for (int i=0;  i<256; i++)  {
            for (int j=0; j<256; j++) {
                if (glcm[i][j] != 0) {
                    inertia += (Math.pow((i-j),2)*glcm[i][j]);
                }
            }
        }

        ivImage.setImageBitmap(thumbnail);

    }





    private void onSelectFromGalleryResult(Intent data) {
        int x=20,y=20,r=0,g=0,b=0;

        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();

            }
        }



        int redColors = 0;
        int greenColors = 0;
        int blueColors = 0;
        int pCount = 0;

        for (int m = 0; y < bm.getHeight(); y++)
        {
            for (int l = 0; x < bm.getWidth(); x++)
            {
                int c = bm.getPixel(x, y);
                pCount++;
                redColors += Color.red(c);
                greenColors += Color.green(c);
                blueColors += Color.blue(c);
            }
        }
        // calculate average of bitmap r,g,b values
        red = (redColors/pCount);
        green = (greenColors/pCount);
        blue = (blueColors/pCount);

        gbyr=green/red;
        gbyb=green/blue;


        Mat mat=new Mat(bm.getHeight(),bm.getWidth(), CvType.CV_8UC3);
        Bitmap bmp32 = bm.copy(Bitmap.Config.RGB_565, true);
        Utils.bitmapToMat(bmp32, mat);
        Mat mat1 =new Mat(bm.getHeight(),bm.getWidth(), CvType.CV_8UC3);

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY,3);
        Bitmap new_img = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        Rect roi = new Rect(0, 0, mat.cols() - 1, mat.rows() - 1);
        mat.submat(roi);
        Mat imgroi = new Mat();
        mat.copyTo(imgroi);
        Utils.matToBitmap(imgroi, new_img);
        boolean symmetry;
        int phi;
        // get byte arrays for the image pixels and mask pixels
        int width = bm.getWidth();
        int height = bm.getHeight();



//        byte [] mask = bm.getMaskArray();

        int size = bm.getRowBytes() * bm.getHeight();
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        bm.copyPixelsToBuffer(byteBuffer);
        byteBuffer.rewind();
        byte[] pixels = byteBuffer.array();

        int size1 = new_img.getRowBytes() * new_img.getHeight();
        ByteBuffer byteBuffer1 = ByteBuffer.allocate(size1);
        new_img.copyPixelsToBuffer(byteBuffer1);
        byteBuffer1.rewind();
        byte[] mask = byteBuffer1.array();
        // value = value at pixel of interest; dValue = value of pixel at offset
        int value;
        int dValue;
        double totalPixels = roi.height*roi.width;
        totalPixels = totalPixels * 2;
        double pixelProgress = 0;
        double pixelCount = 0;

        //====================================================================================================
        // compute the Gray Level Correlation Matrix

        int offsetX = 1;
        int offsetY = 0;
        double[][] glcm = new double [256][256];
        int d = 1;
        phi = 0;
        symmetry = true;
        double rad = Math.toRadians(-1.0 * phi);
        offsetX = (int) ((int) d* Math.round(Math.cos(rad)));
        offsetY = (int) ((int) d* Math.round(Math.sin(rad)));


        // loop through the pixels in the ROI bounding rectangle
        for (y=roi.y; y<(roi.y + roi.height); y++) 	{
            for (x=roi.x; x<(roi.x + roi.width); x++)	 {
                // check to see if the pixel is in the mask (if it exists)
                if ((mask == null) || ((0xff & mask[(((y-roi.y)*roi.width)+(x-roi.x))]) > 0) ) {
                    // check to see if the offset pixel is in the roi
                    int dx = x + offsetX;
                    int dy = y + offsetY;
                    if ( ((dx >= roi.x) && (dx < (roi.x+roi.width))) && ((dy >= roi.y) && (dy < (roi.y+roi.height))) ) {
                        // check to see if the offset pixel is in the mask (if it exists)
                        if ((mask == null) || ((0xff & mask[(((dy-roi.y)*roi.width)+(dx-roi.x))]) > 0) ) {
                            value = 0xff & pixels[(y*width)+x];
                            dValue = 0xff & pixels[(dy*width) + dx];
                            glcm [value][dValue]++;
                            pixelCount++;
                        }
                        // if symmetry is selected, invert the offsets and go through the process again
                        if (symmetry) {
                            dx = x - offsetX;
                            dy = y - offsetY;
                            if ( ((dx >= roi.x) && (dx < (roi.x+roi.width))) && ((dy >= roi.y) && (dy < (roi.y+roi.height))) ) {
                                // check to see if the offset pixel is in the mask (if it exists)
                                if ((mask == null) || ((0xff & mask[(((dy-roi.y)*roi.width)+(dx-roi.x))]) > 0) ) {
                                    value = 0xff & pixels[(y*width)+x];
                                    dValue = 0xff & pixels[(dy*width) + dx];
                                    glcm [dValue][value]++;
                                    pixelCount++;
                                }
                            }
                        }
                    }
                }
                pixelProgress++;
                //IJ.showProgress(pixelProgress/totalPixels);
            }
        }

        // convert the GLCM from absolute counts to probabilities
        for (int i=0; i<256; i++) {
            for (int j = 0; j < 256; j++) {
                glcm[i][j] = (glcm[i][j]) / (pixelCount);
            }
        }

        double [] px = new double [256];
        double [] py = new double [256];
        double meanx=0.0;
        double meany=0.0;


        // Px(i) and Py(j) are the marginal-probability matrix; sum rows (px) or columns (py)
        // First, initialize the arrays to 0
        for (int i=0;  i<256; i++){
            px[i] = 0.0;
            py[i] = 0.0;
        }

        // sum the glcm rows to Px(i)
        for (int i=0;  i<256; i++) {
            for (int j=0; j<256; j++) {
                px[i] += glcm [i][j];
            }
        }

        // sum the glcm rows to Py(j)
        for (int j=0;  j<256; j++) {
            for (int i=0; i<256; i++) {
                py[j] += glcm [i][j];
            }
        }

        // calculate meanx and meany
        for (int i=0;  i<256; i++) {
            meanx += (i*px[i]);
            meany += (i*py[i]);
        }

        // calculate stdevx and stdevy
        for (int i=0;  i<256; i++) {
            stdevx += ((Math.pow((i-meanx),2))*px[i]);
            stdevy += ((Math.pow((i-meany),2))*py[i]);
        }


        //IDM
        for (int i=0;  i<256; i++)  {
            for (int j=0; j<256; j++) {
                IDM += ((1/(1+(Math.pow(i-j,2))))*glcm[i][j]);
            }
        }

        //Contrast
        for (int i=0;  i<256; i++)  {
            for (int j=0; j<256; j++) {
                //contrast += Math.pow(Math.abs(i-j),2)*(glcm[i][j]);
                contrast += Math.pow(i-j,2)*(glcm[i][j]); // 20110530
            }
        }


        //Energy
        for (int i=0;  i<256; i++)  {
            for (int j=0; j<256; j++) {
                energy += Math.pow(glcm[i][j],2);
            }
        }

        //Entropy


        for (int i=0;  i<256; i++)  {
            for (int j=0; j<256; j++) {
                if (glcm[i][j] != 0) {
                    entropy = entropy-(glcm[i][j]*(Math.log(glcm[i][j])));

                }
            }
        }

        //homogenity

        for (int i=0;  i<256; i++) {
            for (int j=0; j<256; j++) {
                homogeneity += glcm[i][j]/(1.0+Math.abs(i-j));
            }
        }

        //Variance
        mean = (meanx + meany)/2;


        for (int i=0;  i<256; i++)  {
            for (int j=0; j<256; j++) {
                variance += (Math.pow((i-mean),2)* glcm[i][j]);
            }
        }

        //correlation

        for (int i=0;  i<256; i++) {
            for (int j=0; j<256; j++) {
                //Walker, et al. 1995 (matches Xite)
                //correlation += ((((i-meanx)*(j-meany))/Math.sqrt(stdevx*stdevy))*glcm[i][j]);
                //Haralick, et al. 1973 (continued below outside loop; matches original GLCM_Texture)
                //correlation += (i*j)*glcm[i][j];
                //matlab's rephrasing of Haralick 1973; produces the same result as Haralick 1973
                correlation += ((((i-meanx)*(j-meany))/( stdevx*stdevy))*glcm[i][j]);
            }
        }

        //sum

        for (int i=0; i<256; i++)  {
            for (int j=0; j<256; j++) {
                sum = sum + glcm[i][j];
            }
        }

        //prominence


        for (int i=0;  i<256; i++) {
            for (int j=0; j<256; j++) {
                prominence += (Math.pow((i+j-meanx-meany),4)*glcm[i][j]);
            }
        }

        //shade

        for (int i=0;  i<256; i++) {
            for (int j=0; j<256; j++) {
                shade += (Math.pow((i+j-meanx-meany),3)*glcm[i][j]);
            }
        }

        //inertia

        for (int i=0;  i<256; i++)  {
            for (int j=0; j<256; j++) {
                if (glcm[i][j] != 0) {
                    inertia += (Math.pow((i-j),2)*glcm[i][j]);
                }
            }
        }

        ivImage.setImageBitmap(bm);


    }

}




