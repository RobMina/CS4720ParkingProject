package cs4720.ram2aq.yx4qu.uvaparking.cs4720parkingproject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

/**
 * Created by Robert on 4/28/2016.
 */
public class PictureActivity extends Activity {

    private TextView imageExistsTextView = null;
    private ImageView theImageView = null;
    private static final String imageFilename = "UVAParkingPermitImage";
    private File imageDir = null;
    private static final int REQUEST_TAKE_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture);

        imageExistsTextView = (TextView) findViewById(R.id.permit_image_exists_textview);
        theImageView = (ImageView) findViewById(R.id.permit_image_view);

        imageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (imageDir != null) {
            File image = new File(imageDir.getAbsolutePath() + imageFilename);
            if (image.exists()) {
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                //bitmap = Bitmap.createScaledBitmap(bitmap,theImageView.getMaxWidth(),theImageView.getMaxHeight(),true);
                theImageView.setImageBitmap(bitmap);
                imageExistsTextView.setText("");
            }
            else {
                imageExistsTextView.setText("No permit image exists.");
            }
        }
        else {
            imageExistsTextView.setText("No permit image exists.");
        }
    }

    public void captureButtonClicked(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = new File(imageDir.getAbsolutePath() + imageFilename);
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            File image = new File(imageDir.getAbsolutePath() + imageFilename);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
            //bitmap = Bitmap.createScaledBitmap(bitmap,theImageView.getWidth(),theImageView.getHeight(),true);
            theImageView.setImageBitmap(bitmap);
            imageExistsTextView.setText("");
        }
    }
}
