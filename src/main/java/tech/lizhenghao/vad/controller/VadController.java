package tech.lizhenghao.vad.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jitsi.webrtcvadwrapper.WebRTCVad;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
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
        byte[] audioData;
        try (InputStream inputStream = file.getInputStream()) {
            audioData = toByteArray(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("unable to find required files");
        }
        /**
         * 采样率为16000hz,分片为160，即10ms
         */
        WebRTCVad vad = new WebRTCVad(16000, 1);
        int binSize = 160;
        double[] audioSample = new double[160];
        int binIdx = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < audioData.length; i++) {
            double currentSample = audioData[i];
            binIdx = i % binSize;

            //we have filled a bin, let's see if there's speech in it
            if (binIdx == 0 && i > 0) {
                try {
                    boolean isSpeech = vad.isSpeech(audioSample);
                    if (isSpeech) {
                        log.info("speech detect, time:{}ms", i * 10);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            audioSample[binIdx] = currentSample;
        }
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();}
}
