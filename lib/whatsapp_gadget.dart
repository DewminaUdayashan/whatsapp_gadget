import 'dart:async';

import 'package:flutter/services.dart';

class WhatsappGadget {
  static const MethodChannel _channel = const MethodChannel('whatsapp_gadget');

  static Future<bool?> shareToWhatsApp({
    required List<String> paths,
    String type = 'image/jpg',
    String packageName = 'com.whatsapp',
  }) async {
    var payload = <String, dynamic>{
      'data': paths,
      'settings': [
        packageName,
        type,
      ],
    };
    final bool? version =
        await _channel.invokeMethod('shareToWhatsApp', payload);
    return version;
  }
}
