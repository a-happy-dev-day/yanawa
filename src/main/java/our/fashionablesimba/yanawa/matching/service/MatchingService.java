package our.fashionablesimba.yanawa.matching.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import our.fashionablesimba.yanawa.matching.domain.MatchingReviewClient;
import our.fashionablesimba.yanawa.matching.domain.matching.*;
import our.fashionablesimba.yanawa.matching.domain.usermatching.UserMatching;
import our.fashionablesimba.yanawa.matching.domain.usermatching.UserMatchingRepository;
import our.fashionablesimba.yanawa.matching.error.NotFoundDataException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

@Service
public class MatchingService {

    Logger log = LoggerFactory.getLogger(this.getClass());

    private final MatchingRepository matchingRepository;
    private final UserMatchingRepository userMatchingRepository;

    private final MatchingReviewClient matchingReviewClient;

    public MatchingService(MatchingRepository matchingRepository, UserMatchingRepository userMatchingRepository, MatchingReviewClient matchingReviewClient) {
        this.matchingRepository = matchingRepository;
        this.userMatchingRepository = userMatchingRepository;
        this.matchingReviewClient = matchingReviewClient;
    }

    @Transactional
    public Matching recruit(Long userId, String tennisCourtName, LocalDateTime matchingData,
                            RecruitmentAge age, RatingLevel minimumLevel, RatingLevel maximumLevel, String content,
                            Long recruitmentAnnual, PreferenceTeamGame teamGame, BigDecimal rentalCost
    ) {
        log.debug("[{}][{}] recruit Matching", this.getClass(), this.getClass().getSimpleName());
        matchingRepository.findAllByUserId(userId).stream().anyMatch(matching ->
        {
            if (matching.getMatchingDate().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
                    .equals(matchingData.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)))) {
                throw new IllegalStateException("겹치는 시간이 존재합니다.");
            }
            return false;
        });

        Matching matching = new Matching(userId, matchingData, minimumLevel, maximumLevel,
                age, teamGame, rentalCost, content, tennisCourtName, recruitmentAnnual);
        return matchingRepository.save(matching);
    }

    @Transactional
    public UserMatching apply(Long userId, Long matchingId) {
        log.debug("[{}][{}] apply Matching", this.getClass(), this.getClass().getSimpleName());
        Matching matching = matchingRepository.findById(matchingId).orElseThrow(() -> new NotFoundDataException("매칭 정보가 존재하지 않습니다."));
        if (!matching.getStatus().equals(MatchingStatus.RECRUITING)) {
            throw new IllegalStateException("현재 매칭이 모집중이 아닙니다.");
        }

        UserMatching userMatching = userMatchingRepository.save(new UserMatching(userId, matchingId));

        if (matching.getPreferenceTeamGame().equals(PreferenceTeamGame.SINGLES)
                || userMatchingRepository.findAllByMatchingId(matchingId).stream().count() == 4) {
            completeRecruitment(matchingId);
        }
        return userMatching;
    }

    @Scheduled(fixedDelayString = "${fixed.delay.seconds:60}000")
    public void CallToCompleteMatching() {
        log.debug("[{}][{}] Call to complete matching", this.getClass(), this.getClass().getSimpleName());
        matchingRepository.findAll().forEach(matching -> {
            if (matching.getStatus().equals(MatchingStatus.MATCHING_PROGRESS) && matching.getMatchingDate().plusHours(4).isAfter(LocalDateTime.now())) {
                completeMatch(matching.getMatchingId());
            }
        });
    }

    @Transactional
    public void completeRecruitment(Long matchingId) {
        Matching matching = matchingRepository.findById(matchingId).orElseThrow(() -> new NotFoundDataException("매칭 정보가 존재하지 않습니다."));
        if (!matching.getStatus().equals(MatchingStatus.RECRUITING)) {
            throw new IllegalStateException("현재 매칭이 모집중이 아닙니다.");
        }

        matching.updateStatus(MatchingStatus.RECRUITMENT_COMPLETED);
        matchingRepository.save(matching);
    }

    @Transactional
    public void proceedMatch(Long matchingId) {
        Matching matching = matchingRepository.findById(matchingId).orElseThrow(() -> new NotFoundDataException("매칭 정보가 존재하지 않습니다."));
        if (!matching.getStatus().equals(MatchingStatus.RECRUITMENT_COMPLETED)) {
            throw new IllegalStateException("현재 매칭이 모집이 완료되지 않았습니다.");
        }

        matching.updateStatus(MatchingStatus.MATCHING_PROGRESS);
        matchingRepository.save(matching);
    }

    @Transactional
    public void completeMatch(Long matchingId) {
        Matching matching = matchingRepository.findById(matchingId).orElseThrow(() -> new NotFoundDataException("매칭 정보가 존재하지 않습니다."));
        if (!matching.getStatus().equals(MatchingStatus.MATCHING_PROGRESS)) {
            throw new IllegalStateException("현재 매칭이 진행중이지 않습니다.");
        }

        matching.updateStatus(MatchingStatus.MATCHING_COMPLETED);
        matchingRepository.save(matching);

    }

    @Transactional
    public void completeReview(Long matchingId) {
        Matching matching = matchingRepository.findById(matchingId).orElseThrow(() -> new NotFoundDataException("매칭 정보가 존재하지 않습니다."));
        if (!matching.getStatus().equals(MatchingStatus.MATCHING_COMPLETED)) {
            throw new IllegalStateException("현재 매칭이 끝나지 않습니다.");
        }

        matching.updateStatus(MatchingStatus.REVIEW_COMPLETED);
        matchingRepository.save(matching);

        userMatchingRepository.findAllByMatchingId(matchingId).forEach(userMatching -> {
            matchingReviewClient.writeMatchingReview(matchingId, userMatching.getUserId());
        });
    }

    @Transactional(readOnly = true)
    public List<Matching> findAll() {
        return matchingRepository.findAll();
    }

}
