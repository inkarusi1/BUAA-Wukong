package com.example.navbar.util;

import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.voice.VoicePool;

public class Speaker {
    private final VoicePool voicePool = VoicePool.get();

    public void speak(String content) {
        voicePool.playTTs(content, Priority.NORMAL, null);
    }

    public void stop() {
        voicePool.stopTTs(Priority.NORMAL, null);
    }
}
