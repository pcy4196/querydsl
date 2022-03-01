package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJpaRepository;
import study.querydsl.repository.MemberRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
        return memberJpaRepository.searchByWhere(condition);
    }

    @GetMapping("/v2/members")
    public Page<MemberTeamDto> searchMemberV2(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPagingSimple(condition, pageable);
    }

    @GetMapping("/v3/members")
    public Page<MemberTeamDto> searchMemberV3(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPagingComplexTwo(condition, pageable);
    }

    // searchMemberV3
    // count 쿼리
    /*
    select
            count(member0_.member_id) as col_0_0_
        from
            member member0_
     */

    // content 쿼리
    /*
    select
            member0_.member_id as col_0_0_,
            member0_.username as col_1_0_,
            member0_.age as col_2_0_,
            team1_.team_id as col_3_0_,
            team1_.name as col_4_0_
        from
            member member0_
        left outer join
            team team1_
                on member0_.team_id=team1_.team_id limit ? offset ?
     */
}
