package dewz.plugins.wagadget.whatsapp_gadget;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
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


    private void shareToWhatsApp(ArrayList<Uri> arr, ArrayList<String> settings) {
        String PACKAGE = settings.get(0);
        String TYPE = settings.get(1);
        ArrayList<Uri> uris = new ArrayList<>(arr.size());
        for (Uri uri : arr) {
            File f = new File(uri.getPath());
            Log.d(TAG, "shareToWhatsApp: " + uri.getPath());
            Log.d(TAG, "shareToWhatsApp: " + activity.getCacheDir().getPath());
            Log.d(TAG, "shareToWhatsApp: " + activity.getExternalCacheDir().getPath());
        }
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setPackage(PACKAGE); //com.whatsapp
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setType("image/*");//
        shareIntent.putExtra(Intent.EXTRA_STREAM, arr);
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
