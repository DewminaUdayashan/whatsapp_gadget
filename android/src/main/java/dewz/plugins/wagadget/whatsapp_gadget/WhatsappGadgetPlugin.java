package dewz.plugins.wagadget.whatsapp_gadget;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
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
        for (Uri uri : arr) {
            File f = new File(uri.getPath());
            if (f.exists()) Log.d(TAG, "shareToWhatsApp: FILE EXIST");
            else
                Log.d(TAG, "shareToWhatsApp: FILE NOT FOUND");
        }
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setPackage(PACKAGE); //com.whatsapp
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, (java.util.ArrayList<? extends android.os.Parcelable>) arr);
        shareIntent.setType(TYPE);//"image/jpeg"
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            activity.startActivity(shareIntent);
            result.success("true");
        } catch (Exception ex) {
            Log.d(TAG, "shareToWhatsApp: Error on Start Activity => " + ex);
            result.success(ex);
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
