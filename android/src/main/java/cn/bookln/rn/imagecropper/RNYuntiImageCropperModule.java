package cn.bookln.rn.imagecropper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageOptions;

import androidx.annotation.NonNull;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_EXTRA_OPTIONS;
import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_EXTRA_SOURCE;

public class RNYuntiImageCropperModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private final ReactApplicationContext reactContext;
    private Uri mSource;
    private CropImageOptions mOptions;
    private Promise mPromise;

    public RNYuntiImageCropperModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(this);
        mOptions = new CropImageOptions();
    }

    @Override
    public String getName() {
        return "RNYuntiImageCropper";
    }

    @ReactMethod
    public void cropWithUri(final String uriStr, final Promise promise) {
        cropWithUriWithAspectRatio(uriStr, null, promise);
    }

    /**
     * 以Uri打开截图
     *
     * @param uriStr  必须是本地Uri
     * @param options
     * @param promise
     */
    @ReactMethod
    public void cropWithUriWithAspectRatio(final String uriStr, final ReadableMap options, final Promise promise) {
        final Activity activity = getCurrentActivity();

        if (activity == null) {
            promise.reject("400", "Activity doesn't exist");
            return;
        }

        if (options != null) {
            mOptions.aspectRatioX = options.getInt("aspectRatioX");
            mOptions.aspectRatioY = options.getInt("aspectRatioY");
            mOptions.initialCropWindowPaddingRatio = 0.05f;
        }

        mPromise = promise;
//        setConfiguration(options);
//        mSource = Uri.parse("file:///storage/emulated/0/DCIM/Camera/IMG_20181012_154422.jpg");
        mSource = Uri.parse(uriStr);
        activity.startActivityForResult(getIntent(activity), CROP_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public Intent getIntent(@NonNull Context context) {
        mOptions.validate();

        Intent intent = new Intent();
        intent.setClass(context, CropActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(CROP_IMAGE_EXTRA_SOURCE, mSource);
        bundle.putParcelable(CROP_IMAGE_EXTRA_OPTIONS, mOptions);
        intent.putExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE, bundle);
        return intent;
    }

    //TODO 以base64打开截图
    @ReactMethod
    public void cropWithBase64(final String base64, final ReadableMap options, final Promise promise) {

    }


    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();
                WritableMap writableMap = new WritableNativeMap();
                float[] cropPercentData = data.getFloatArrayExtra(CropActivity.CROP_PERCENT_DATA);
                if (data != null && cropPercentData != null) {
                    WritableMap coordsMap = new WritableNativeMap();
                    coordsMap.putDouble("left",cropPercentData[0]);
                    coordsMap.putDouble("top",cropPercentData[1]);
                    coordsMap.putDouble("right",cropPercentData[2]);
                    coordsMap.putDouble("bottom",cropPercentData[3]);
                    writableMap.putMap(CropActivity.CROP_PERCENT_DATA,coordsMap);
                }
                int cropWidth = data.getIntExtra(CropActivity.CROPPED_WIDTH, 0);
                int cropHeight = data.getIntExtra(CropActivity.CROPPED_HEIGHT, 0);
                writableMap.putInt(CropActivity.CROPPED_WIDTH,cropWidth);
                writableMap.putInt(CropActivity.CROPPED_HEIGHT,cropHeight);

                writableMap.putString("uri", resultUri.toString());
                mPromise.resolve(writableMap);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                mPromise.reject("500", "文件路径为空加载图片失败");
            } else if (resultCode == RESULT_CANCELED) {
                mPromise.reject("501", "取消操作");
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {

    }
}