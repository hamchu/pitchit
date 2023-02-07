package com.alppano.speakon.domain.recording.dto;

import com.alppano.speakon.domain.recording_timestamp.dto.TimestampWithQuestion;
import lombok.*;

import java.util.List;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecordingDetailInfo {
    private Long recordingId;
    private String recordingUri;
    private List<TimestampWithQuestion> timestamps;
}
