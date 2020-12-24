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
        try (VAD vad = new VAD()) {
            int binSize = 320;
            // byte[] audioSample = new byte[320];
            byte[] audioBuffer = new byte[AUDIO_MAX_SEGMENT_LENGTH];
            int binIdx = 0;
            boolean isSpeechNow = false;
            StringBuilder sb = new StringBuilder();
            for (int i = 0,j=0; i < audioData.length; i++,j++) {
                byte currentSample = audioData[i];
                binIdx = i % binSize;
                //we have filled a bin, let's see if there's speech in it
                if (binIdx == 0 && i > 0) {
                    try {
                        boolean isSpeech = vad.isSpeech(audioBuffer);
                        if(isSpeech != isSpeechNow){
                            isSpeechNow = isSpeech;
                            log.info("speech detect, time:{}ms, isSpeech:{}, audio length:{}", i / 160 * 10, isSpeech, audioBuffer.length);
                            // 检测到了声音，则把audioBuffer清空
                            audioBuffer = new byte[AUDIO_MAX_SEGMENT_LENGTH];
                            j = 0;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // audioSample[binIdx] = currentSample;
                audioBuffer[j] = currentSample;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        // 跳过wav格式的前44个字节
        input.skip(44);
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }
}
