package tech.lizhenghao.vad.controller;

import com.orctom.vad4j.VAD;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Array;
import java.util.Arrays;

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

    private static int AUDIO_MAX_SEGMENT_LENGTH = 2 * 16 * 1600;

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
             VAD vad = new VAD()) {
            /**
             * 采样率为16000hz float,分片为640，即10ms
             */
            byte currentSample;

            int binSize = 640;
            byte[] audioSample = inputStream.readAllBytes();
            byte[] audioBuffer = new byte[AUDIO_MAX_SEGMENT_LENGTH];
            int binIdx;
            boolean isSpeechNow = false;
            for (int i = 0, j = 0; i < audioSample.length; i++) {
                currentSample = audioSample[i];
                binIdx = i % binSize;
                //we have filled a bin, let's see if there's speech in it
                if (binIdx == 0 && i > 0) {
                    try {
                        float isSpeech = vad.speechProbability(audioBuffer);
                        log.info("speech detect, time:{}ms, probability:{}, audio length:{}", i / 64, isSpeech, i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // audioSample[binIdx] = currentSample;
                audioBuffer[j] = currentSample;
            }
            log.info("i:{}", audioSample.length);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("unable to find required files");
        }

    }
}
