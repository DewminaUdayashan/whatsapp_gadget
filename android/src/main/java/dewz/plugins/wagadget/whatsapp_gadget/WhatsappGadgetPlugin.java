package dewz.plugins.wagadget.whatsapp_gadget;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/**
 * WhatsappGadgetPlugin
 */
public class WhatsappGadgetPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {
    private static final String TAG = "WhatsApp Gadget => ";

    private MethodChannel channel;
    private Activity activity;
    private Result result;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "whatsapp_gadget");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        this.result = result;


        if (call.method.equals("shareToWhatsApp")) {
            final Map<String, ArrayList<String>> arg = call.arguments();
            Log.d(TAG, "onMethodCall: " + arg);
            ArrayList<String> dataList = arg.get("data");
            ArrayList<String> settings = arg.get("settings");
            ArrayList<Uri> dataUriArr = new ArrayList<Uri>();
            assert dataList != null;
            for (String data : dataList) {
                Log.d(TAG, "onMethodCall: " + data);
                dataUriArr.add(Uri.parse(data));
            }
            assert settings != null;
            shareToWhatsApp(dataUriArr, settings);
        } else {
            result.notImplemented();
        }
    }


    private void shareToWhatsAppApi30() {

    }


    public boolean save(Activity activity, ArrayList<byte[]> bytes, String mimeType) {
        String extention;
        boolean saved = false;
        String name;
        final String IMAGES_FOLDER_NAME = "DewzStatus";
        OutputStream fos;
        if (mimeType.contains("image")) extention = ".jpg";
        else extention = ".mp4";
        for (int i = 0; i < bytes.size(); i++) {
            Log.d(TAG, "save: EXTENSION ==============> " + extention);
            Log.d(TAG, "save: MIME TYPE ==============> " + mimeType);
            name = String.valueOf(System.currentTimeMillis()) + i;
            byte[] aByte = bytes.get(i);
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentResolver resolver = activity.getContentResolver();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);//"image/jpeg"
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + IMAGES_FOLDER_NAME);
                    Uri imageUri;
                    if ((mimeType.contains("image")))
                        imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                    else
                        imageUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
                    fos = resolver.openOutputStream(imageUri);
                } else {
                    String imagesDir = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DCIM).toString() + File.separator + IMAGES_FOLDER_NAME;
                    File file = new File(imagesDir);
                    if (!file.exists()) {
                        if (file.mkdir()) {
                            Log.d(TAG, "save: DIR CREATED");
                        }
                    }
                    File image = new File(imagesDir, name + extention);
                    fos = new FileOutputStream(image);
//                    scanMedia(activity, image.getPath());
                }
                fos.write(aByte);
                fos.flush();
                fos.close();
                saved = true;
            } catch (Exception e) {
                saved = false;
                e.printStackTrace();
            }
        }
        return saved;
    }


    private void shareToWhatsApp(ArrayList<Uri> arr, ArrayList<String> settings) {
        String PACKAGE = settings.get(0);
        String TYPE = settings.get(1);
        Log.d(TAG, "shareToWhatsApp: RECIVED TYPE " + TYPE);
        File f = new File(arr.get(0).getPath());
        Uri uri =
                FileProvider.getUriForFile(activity, activity.getPackageName(), f);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setPackage(PACKAGE); //com.whatsapp
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setType(TYPE);//
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        try {
            activity.startActivity(shareIntent);
            result.success("true");
        } catch (Exception ex) {
            Log.d(TAG, "shareToWhatsApp: Error on Start Activity => " + ex);
            result.success(ex);
        }

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File copyToShareCacheFolder(File file) throws IOException {
        File folder = getShareCacheFolder();
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File newFile = new File(folder, file.getName());
        copy(file, newFile);
        return newFile;
    }

    @NonNull
    private File getShareCacheFolder() {
        return new File(activity.getCacheDir(), "whatsapp_gadget");
    }

    private static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {

            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }


    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        binding.addActivityResultListener(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        activity = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: Code => " + requestCode + " Data => " + data.getData());
        return false;
    }
}
