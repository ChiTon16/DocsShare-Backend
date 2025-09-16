// FollowService.java
package com.tonz.tonzdocs.service;

import com.tonz.tonzdocs.dto.SubjectDTO;
import com.tonz.tonzdocs.model.*;
import com.tonz.tonzdocs.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final SubjectFollowRepository followRepo;
    private final SubjectRepository subjectRepo;
    private final UserRepository userRepo;

    @Transactional(readOnly = true)
    public List<SubjectDTO> listFollowed(Integer userId) {
        return followRepo.findFollowedDTOByUser(userId);
    }

    @Transactional
    public boolean follow(Integer userId, Integer subjectId) {
        if (followRepo.existsByUser_UserIdAndSubject_SubjectId(userId, subjectId)) return false; // idempotent
        User u = userRepo.findById(userId).orElseThrow();
        Subject s = subjectRepo.findById(subjectId).orElseThrow();
        SubjectFollow f = new SubjectFollow(new SubjectFollowId(userId, subjectId), u, s, null);
        followRepo.save(f);
        return true;
    }

    @Transactional
    public boolean unfollow(Integer userId, Integer subjectId) {
        if (!followRepo.existsByUser_UserIdAndSubject_SubjectId(userId, subjectId)) return false;
        followRepo.deleteByUser_UserIdAndSubject_SubjectId(userId, subjectId);
        return true;
    }

    @Transactional(readOnly = true)
    public long countFollowers(Integer subjectId) {
        return followRepo.countFollowersBySubject(subjectId);
    }
}
