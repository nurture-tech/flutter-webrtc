package org.webrtc.video;

import androidx.annotation.Nullable;

import org.webrtc.EglBase;
import org.webrtc.SoftwareVideoDecoderFactory;
import org.webrtc.VideoCodecInfo;
import org.webrtc.VideoDecoder;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.WrappedVideoDecoderFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomVideoDecoderFactory implements VideoDecoderFactory {
    private SoftwareVideoDecoderFactory softwareVideoDecoderFactory = new SoftwareVideoDecoderFactory();
    private WrappedVideoDecoderFactory wrappedVideoDecoderFactory;
    private boolean forceSWCodec  = false;

    private List<String> forceSWCodecs = new ArrayList<>();

    private List<String> priorities;

    public  CustomVideoDecoderFactory(EglBase.Context sharedContext) {
        priorities = Arrays.asList("H265", "H264", "AV1", "VP9", "VP8");
        this.wrappedVideoDecoderFactory = new WrappedVideoDecoderFactory(sharedContext);
    }

    public void setForceSWCodec(boolean forceSWCodec) {
        this.forceSWCodec = forceSWCodec;
    }

    public void setForceSWCodecList(List<String> forceSWCodecs) {
        this.forceSWCodecs = forceSWCodecs;
    }

    @Nullable
    @Override
    public VideoDecoder createDecoder(VideoCodecInfo videoCodecInfo) {
        if(forceSWCodec) {
            return softwareVideoDecoderFactory.createDecoder(videoCodecInfo);
        }
        if(!forceSWCodecs.isEmpty()) {
            if(forceSWCodecs.contains(videoCodecInfo.name)) {
                return softwareVideoDecoderFactory.createDecoder(videoCodecInfo);
            }
        }
        return wrappedVideoDecoderFactory.createDecoder(videoCodecInfo);
    }

    @Override
    public VideoCodecInfo[] getSupportedCodecs() {
//        if(forceSWCodec && forceSWCodecs.isEmpty()) {
//            return softwareVideoDecoderFactory.getSupportedCodecs();
//        }
//        return wrappedVideoDecoderFactory.getSupportedCodecs();

        final List<VideoCodecInfo> supported = new ArrayList<>(Arrays.asList(wrappedVideoDecoderFactory.getSupportedCodecs()));
        final VideoCodecInfo[] sorted = new VideoCodecInfo[supported.size()];
        int i = 0;
        for (String codec : priorities) {
            int j = 0;
            while (j < supported.size() && !codec.equals(supported.get(j).name))
                j++;
            if (j < supported.size()) {
                sorted[i++] = supported.get(j);
                supported.remove(j);
            }
        }
        while (i < sorted.length && !supported.isEmpty()) {
            final VideoCodecInfo codecInfo = supported.get(0);
            supported.remove(0);
            sorted[i++] = codecInfo;
        }

        return sorted;
    }
}
