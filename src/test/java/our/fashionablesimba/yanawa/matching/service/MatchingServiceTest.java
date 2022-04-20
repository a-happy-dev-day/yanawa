package our.fashionablesimba.yanawa.matching.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import our.fashionablesimba.yanawa.matching.domain.NotificationReviewClient;
import our.fashionablesimba.yanawa.matching.domain.matching.Matching;
import our.fashionablesimba.yanawa.matching.domain.matching.MatchingRepository;
import our.fashionablesimba.yanawa.matching.domain.usermatching.UserMatchingRepository;
import our.fashionablesimba.yanawa.matching.fixture.FakeNotificationReviewClient;
import our.fashionablesimba.yanawa.matching.fixture.MemoryMatchingRepository;
import our.fashionablesimba.yanawa.matching.fixture.MemoryUserMatchingRepository;

import static our.fashionablesimba.yanawa.matching.fixture.MatchingFixture.매칭;

class MatchingServiceTest {

    private MatchingRepository matchingRepository = new MemoryMatchingRepository();
    private UserMatchingRepository userMatchingRepository = new MemoryUserMatchingRepository();
    private NotificationReviewClient notificationReviewClient = new FakeNotificationReviewClient();

    MatchingService matchingService = new MatchingService(matchingRepository, userMatchingRepository, notificationReviewClient);

    @Test
    @DisplayName("매칭을 등록합니다.")
    void test1() {
        Matching matching = 매칭;
        Assertions.assertDoesNotThrow(
                () -> matchingService.recruit(matching.getUserId(), matching.getTennisCourt(),
                        matching.getMatchingDate(), matching.getRecruitmentAge(), matching.getMinimumLevel(),
                        matching.getMaximumLevel(), matching.getMatchingContent(), matching.getRecruitmentAnnual(),
                        matching.getPreferenceTeamGame(), matching.getRentalCost())
        );
    }


    // 매칭을 등록하는 사용자가 해당 시간에 매칭이 존재하면 생성할 수 없습니다.
    // 매칭을 요청합니다.
    // 매칭이 모집 중이 아니라면 예외를 발생합니다.
    // 매칭의 상태를 변경합니다.
    // 각각의 매칭의 상태에 따라 테스트를 진행합니다.
}