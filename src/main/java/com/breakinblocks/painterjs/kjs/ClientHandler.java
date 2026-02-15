package com.breakinblocks.painterjs.kjs;

import com.breakinblocks.painterjs.client.ClientPainter;
import com.breakinblocks.painterjs.objects.PaintObject;

import java.util.Map;

public class ClientHandler {
    public static void paint(Map<String, PaintObject> paintObjects) {
        ClientPainter.update(paintObjects);
    }
}
