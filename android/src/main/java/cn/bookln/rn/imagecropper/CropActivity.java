package cn.bookln.rn.imagecropper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageOptions;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class CropActivity extends AppCompatActivity implements CropImageView.OnSetImageUriCompleteListener,
        CropImageView.OnCropImageCompleteListener {

    private CropImageView mCropImageView;

    private TextView IvStartCropper;

    private RelativeLayout IvCloseCropper;

    private ImageView ivRotateLeft;

    private ImageView ivRotateRight;

    private Uri mCropImageUri;

    private CropImageOptions mOptions;

    private int rotateDegree = 0;

    public static String CROP_PERCENT_DATA = "croppedCoordsPercent";
    public static String CROPPED_WIDTH = "croppedWidth";
    public static String CROPPED_HEIGHT = "croppedHeight";

    @Override
    @SuppressLint("NewApi")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);
        mCropImageView = findViewById(R.id.cropImageView);
        IvStartCropper = findViewById(R.id.btn_start_cropper);
        IvCloseCropper = findViewById(R.id.btn_close_cropper);

        ivRotateLeft = findViewById(R.id.btn_rotate_left);
        ivRotateRight = findViewById(R.id.btn_rotate_right);

        ivRotateLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateDegree -= 90;
                mCropImageView.setRotatedDegrees(rotateDegree);
            }
        });

        ivRotateRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateDegree += 90;
                mCropImageView.setRotatedDegrees(rotateDegree);
            }
        });

        IvCloseCropper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResultCancel();
            }
        });

        IvStartCropper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImage();
            }
        });


        Bundle bundle = getIntent().getBundleExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE);
        mCropImageUri = bundle.getParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE);
        mOptions = bundle.getParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS);

        if (savedInstanceState == null) {
            if (CropImage.isReadExternalStoragePermissionsRequired(this, mCropImageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already grunted, can start crop image activity
                mCropImageView.setImageUriAsync(mCropImageUri);
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mCropImageView.setOnSetImageUriCompleteListener(this);
        mCropImageView.setOnCropImageCompleteListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCropImageView.setOnSetImageUriCompleteListener(null);
        mCropImageView.setOnCropImageCompleteListener(null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResultCancel();
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                // User cancelled the picker. We don't have anything to crop
                setResultCancel();
            }

            if (resultCode == Activity.RESULT_OK) {
                mCropImageUri = CropImage.getPickImageResultUri(this, data);

                // For API >= 23 we need to check specifically that we have permissions to read external
                // storage.
                if (CropImage.isReadExternalStoragePermissionsRequired(this, mCropImageUri)) {
                    // request permissions and handle the result in onRequestPermissionsResult()
                    requestPermissions(
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
                } else {
                    // no permissions required or already grunted, can start crop image activity
                    mCropImageView.setImageUriAsync(mCropImageUri);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null
                    && grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                mCropImageView.setImageUriAsync(mCropImageUri);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
                setResultCancel();
            }
        }
    }

    @Override
    public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {

        Rect cropRect = result.getCropRect();
        int cropLeft = cropRect.left;
        int cropRight = cropRect.right;
        int cropTop = cropRect.top;
        int cropBottom = cropRect.bottom;
        Rect wholeImageRect = result.getWholeImageRect();
        int wholeLeft = wholeImageRect.left;
        int wholeRight = wholeImageRect.right;
        int wholeTop = wholeImageRect.top;
        int wholeBottom = wholeImageRect.bottom;

        float leftPercent = cropLeft * 1f / (wholeRight - wholeLeft);
        float rightPercent = cropRight * 1f / (wholeRight - wholeLeft);

        float topPercent = cropTop * 1f / (wholeBottom - wholeTop);
        float bottomPercent = cropBottom * 1f / (wholeBottom - wholeTop);
        float[] cropPercent = {leftPercent, topPercent, rightPercent, bottomPercent};

        int cropWidth =cropRight-cropLeft;
        int cropHeight =cropBottom-cropTop;
        setResult(result.getUri(), result.getError(), result.getSampleSize(),cropPercent,cropWidth,cropHeight);
    }

    @Override
    public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {
        if (error == null) {
            if (mOptions.initialCropWindowRectangle != null) {
                mCropImageView.setCropRect(mOptions.initialCropWindowRectangle);
            }
            if (mOptions.initialRotation > -1) {
                mCropImageView.setRotatedDegrees(mOptions.initialRotation);
            }
            if (mOptions.aspectRatioY > 1 && mOptions.aspectRatioX > 1) {
                Rect cropRect = mCropImageView.getCropRect();
                int width = cropRect.right - cropRect.left;
                int height = cropRect.bottom - cropRect.top;
                int halfHeight = height / 2;
                int centerY = cropRect.top + halfHeight;
                float aspect = mOptions.aspectRatioX * 1f / mOptions.aspectRatioY;
                int newHeight = (int) (width / aspect);

                mCropImageView.setCropRect(new Rect(cropRect.left, centerY - newHeight / 2, cropRect.right, centerY + newHeight / 2));
            }
        } else {
            setResult(null, error, 1,null,0,0);
        }
    }

    protected void cropImage() {
        if (mOptions.noOutputImage) {
            setResult(null, null, 1,null,0,0);
        } else {
            Uri outputUri = getOutputUri();
            mCropImageView.saveCroppedImageAsync(
                    outputUri,
                    mOptions.outputCompressFormat,
                    mOptions.outputCompressQuality,
                    mOptions.outputRequestWidth,
                    mOptions.outputRequestHeight,
                    mOptions.outputRequestSizeOptions);
        }
    }

    protected void rotateImage(int degrees) {
        mCropImageView.rotateImage(degrees);
    }

    protected Uri getOutputUri() {
        Uri outputUri = mOptions.outputUri;
        if (outputUri == null || outputUri.equals(Uri.EMPTY)) {
            try {
                String ext =
                        mOptions.outputCompressFormat == Bitmap.CompressFormat.JPEG
                                ? ".jpg"
                                : mOptions.outputCompressFormat == Bitmap.CompressFormat.PNG ? ".png" : ".webp";
                outputUri = Uri.fromFile(File.createTempFile("cropped", ext, getCacheDir()));
            } catch (IOException e) {
                throw new RuntimeException("Failed to create temp file for output image", e);
            }
        }
        return outputUri;
    }

    /**
     * Result with cropped image data or error if failed.
     */
    protected void setResult(Uri uri, Exception error, int sampleSize, float[] cropData,int cropWidth,int cropHeight) {
        int resultCode = error == null ? RESULT_OK : CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE;
        setResult(resultCode, getResultIntent(uri, error, sampleSize, cropData,cropWidth,cropHeight));
        finish();
    }

    /**
     * Cancel of cropping activity.
     */
    protected void setResultCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * Get intent instance to be used for the result of this activity.
     */
    protected Intent getResultIntent(Uri uri, Exception error, int sampleSize, float[] cropData,int cropWidth,int cropHeight) {
        CropImage.ActivityResult result =
                new CropImage.ActivityResult(
                        mCropImageView.getImageUri(),
                        uri,
                        error,
                        mCropImageView.getCropPoints(),
                        mCropImageView.getCropRect(),
                        mCropImageView.getRotatedDegrees(),
                        mCropImageView.getWholeImageRect(),
                        sampleSize);
        Intent intent = new Intent();
        intent.putExtras(getIntent());
        intent.putExtra(CropImage.CROP_IMAGE_EXTRA_RESULT, result);
        intent.putExtra(CropActivity.CROP_PERCENT_DATA, cropData);
        intent.putExtra(CropActivity.CROPPED_HEIGHT, cropHeight);
        intent.putExtra(CropActivity.CROPPED_WIDTH, cropWidth);
        return intent;
    }
}