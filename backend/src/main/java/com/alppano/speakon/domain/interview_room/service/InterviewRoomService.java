package com.alppano.speakon.domain.interview_room.service;

import com.alppano.speakon.common.dto.PagedResult;
import com.alppano.speakon.common.exception.ResourceForbiddenException;
import com.alppano.speakon.common.exception.ResourceNotFoundException;
import com.alppano.speakon.domain.interview_join.entity.InterviewJoin;
import com.alppano.speakon.domain.interview_join.entity.Participant;
import com.alppano.speakon.domain.interview_join.repository.InterviewJoinRepository;
import com.alppano.speakon.domain.interview_room.dto.InterviewRoomDetailInfo;
import com.alppano.speakon.domain.interview_room.dto.InterviewRoomInfo;
import com.alppano.speakon.domain.interview_room.dto.InterviewRoomRequest;
import com.alppano.speakon.domain.interview_room.entity.InterviewRoom;
import com.alppano.speakon.domain.interview_room.repository.InterviewRoomRepository;
import com.alppano.speakon.domain.user.entity.User;
import com.alppano.speakon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InterviewRoomService {

    private final InterviewRoomRepository interviewRoomRepository;
    private final InterviewJoinRepository interviewJoinRepository;
    private final UserRepository userRepository;

    @Transactional
    public InterviewRoomInfo createInterviewRoom(InterviewRoomRequest dto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 회원입니다."));

        InterviewRoom interviewRoom = InterviewRoom.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .manager(user)
                .maxPersonCount(dto.getMaxPersonCount())
                .password(dto.getPassword())
                .startDate(dto.getStartDate())
                .finished(0)
                .build();

        interviewRoomRepository.save(interviewRoom);

        InterviewJoin interviewJoin = InterviewJoin.builder()
                .user(user)
                .finished(0)
                .build();

        // TODO: 면접방 생성 후, 응답 데이터로 현재 인원 정보를 줄 필요가 없다면 builder로 생성할 때 interviewRoom을 같이 넣어주면 됨
        interviewJoin.setInterviewRoom(interviewRoom);

        interviewJoinRepository.save(interviewJoin);

        return new InterviewRoomInfo(interviewRoom);
    }

    @Transactional
    public void deleteInterviewRoom(Long interviewRoomId, Long userId) {
        InterviewRoom interviewRoom = interviewRoomRepository.findById(interviewRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 면접방입니다."));

        if (!interviewRoom.getManager().getId().equals(userId)) {
            throw new ResourceForbiddenException("방장만 면접방을 삭제할 수 있습니다.");
        }

        interviewRoomRepository.delete(interviewRoom);
    }

    public InterviewRoomDetailInfo getInterviewRoomDetailInfo(Long interviewRoomId, Long userId) {
        InterviewRoom interviewRoom = interviewRoomRepository.findById(interviewRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 면접방입니다."));

        interviewJoinRepository.findByUserIdAndInterviewRoomId(userId, interviewRoomId)
                .orElseThrow(() -> new ResourceForbiddenException("참여 중인 면접방만 접근할 수 있습니다."));

        InterviewRoomDetailInfo interviewRoomDetailInfo = new InterviewRoomDetailInfo(interviewRoom);

        // 참가자 정보
        List<Participant> participants = new ArrayList<>();
        for (InterviewJoin join : interviewRoom.getInterviewJoins()) {
            participants.add(new Participant(join.getUser(), join.getId()));
        }
        interviewRoomDetailInfo.setParticipants(participants);

        return interviewRoomDetailInfo;
    }

    public PagedResult<InterviewRoomInfo> searchInterviewRooms(Pageable pageable, String title, Integer finished) {
        Page<InterviewRoom> queryResult = null;

        if (title != null && finished != null) {
            queryResult = interviewRoomRepository.findAllByTitleContainingAndFinished(pageable, title, finished);
        } else if (title != null) {
            queryResult = interviewRoomRepository.findAllByTitleContaining(pageable, title);
        } else if (finished != null) {
            queryResult = interviewRoomRepository.findAllByFinished(pageable, finished);
        } else {
            queryResult = interviewRoomRepository.findAll(pageable);
        }

        Page<InterviewRoomInfo> list = queryResult.map(interviewRoom -> new InterviewRoomInfo(interviewRoom));

        return new PagedResult<>(list);
    }

    @Transactional
    public void setInterviewRoomFinishedStatus(Long interviewRoomId, Integer finished, Long userId) {
        InterviewRoom interviewRoom = interviewRoomRepository.findById(interviewRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 면접방입니다."));

        if (!interviewRoom.getManager().getId().equals(userId)) {
            throw new ResourceForbiddenException("방장만 면접방 상태를 수정할 수 있습니다.");
        }

        interviewRoom.setFinished(finished);
    }

    @Transactional
    public void updateInterviewRoom(InterviewRoomRequest dto, Long interviewRoomId, Long userId) {
        InterviewRoom interviewRoom = interviewRoomRepository.findById(interviewRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 면접방입니다."));

        if (!interviewRoom.getManager().getId().equals(userId)) {
            throw new ResourceForbiddenException("방장만 면접방 상태를 수정할 수 있습니다.");
        }

        if (dto.getTitle() != null) {
            interviewRoom.setTitle(dto.getTitle());
        }

        if (dto.getDescription() != null) {
            interviewRoom.setDescription(dto.getDescription());
        }

        if (dto.getContactWay() != null) {
            interviewRoom.setContactWay(dto.getContactWay());
        }

        if (dto.getMaxPersonCount() != null) {
            if (interviewRoom.getInterviewJoins().size() > dto.getMaxPersonCount()) {
                throw new ResourceForbiddenException("수정하려는 최대 인원 수가 현재 인원 수보다 작습니다.");
            }
            interviewRoom.setMaxPersonCount(dto.getMaxPersonCount());
        }

        if (dto.getStartDate() != null) {
            interviewRoom.setStartDate(dto.getStartDate());
        }

        String password = null;
        if (dto.getPassword() != null && dto.getPassword().trim().length() != 0) {
            password = dto.getPassword().trim();
        }
        interviewRoom.setPassword(password);
    }
}
