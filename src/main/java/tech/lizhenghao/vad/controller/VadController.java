package tech.lizhenghao.vad.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jitsi.webrtcvadwrapper.WebRTCVad;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * @author Lizhenghao
 * @version 1.0
 * @description: TODO
 * @date 2020/12/23 16:32
 */
@RestController
@RequestMapping
@Slf4j
public class VadController {

    private static int AUDIO_MAX_SEGMENT_LENGTH = 4 * 10 * 16000;

    @GetMapping("/version")
    public String version() {
        return "version:1.0.0";
    }

    /**
     * @description: 上传录音文件
     * @author Lizhenghao
     * @date 2020/12/23 16:47
     * @version 1.0
     */
    @SneakyThrows
    @PostMapping("/upload")
    public void upload(@RequestParam("file") MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             DataInputStream dataInputStream = new DataInputStream(inputStream)) {
            WebRTCVad vad = new WebRTCVad(16000, 1);
            /**
             * 采样率为16000hz float,分片为640，即10ms
             */
            int binIdx;
            int binSize = 480;
            int currentSample;
            int i=0;
            int[] audioSample = new int[binSize];
            while (-1 != (currentSample = dataInputStream.readInt())) {
                binIdx = i % binSize;
                //we have filled a bin, let's see if there's speech in it
                if (binIdx == 0 && i > 0) {
                    try {
                        boolean isSpeaking = vad.isSpeech(audioSample);
                        log.info("speech detect, time:{}ms,isSpeech:{}, audio length:{}", i / 32, isSpeaking, i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                audioSample[binIdx] = currentSample;
                i++;
            }
            log.info("i:{}", i);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("unable to find required files");
        }

    }
}
