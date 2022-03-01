package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired MemberRepository memberRepository;

    @Test
    public void basicTest() {
        Member member = new Member("member1", 10);
        memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberRepository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }

    @Test
    public void searchWhereTest() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        // 1
        MemberSearchCondition cond = new MemberSearchCondition();
        cond.setAgeGoe(35);
        cond.setAgeLoe(45);
        cond.setTeamName("teamB");

        List<MemberTeamDto> result = memberRepository.search(cond);

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
                on member0_.team_id=team1_.team_id
        where
            team1_.name=?
            and member0_.age>=?
            and member0_.age<=?
         */
        assertThat(result).extracting("username").containsExactly("member4");

        // 2
        MemberSearchCondition cond1 = new MemberSearchCondition();
        cond1.setTeamName("teamB");

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
                on member0_.team_id=team1_.team_id
        where
            team1_.name=?
         */
        List<MemberTeamDto> result1 = memberRepository.search(cond1);
        assertThat(result1).extracting("username").containsExactly("member3", "member4");

    }

    @Test
    public void searchPageingSimpleTest() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition cond = new MemberSearchCondition();
//        cond.setAgeGoe(35);
//        cond.setAgeLoe(45);
//        cond.setTeamName("teamB");

        PageRequest pageRequest = PageRequest.of(0, 3);

        Page<MemberTeamDto> result = memberRepository.searchPagingSimple(cond, pageRequest);
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
                on member0_.team_id=team1_.team_id limit ?
         */
        /*
        select
            count(member0_.member_id) as col_0_0_
        from
            member member0_
        left outer join
            team team1_
                on member0_.team_id=team1_.team_id
         */
        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("username").containsExactly("member1", "member2", "member3");
    }

    @Test
    public void searchPageingComplexTest() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition cond = new MemberSearchCondition();
//        cond.setAgeGoe(35);
//        cond.setAgeLoe(45);
//        cond.setTeamName("teamB");

        PageRequest pageRequest = PageRequest.of(0, 3);

        Page<MemberTeamDto> result = memberRepository.searchPagingComplex(cond, pageRequest);
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
                on member0_.team_id=team1_.team_id limit ?
         */
        /*
        select
            count(member0_.member_id) as col_0_0_
        from
            member member0_
         */
        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("username").containsExactly("member1", "member2", "member3");
    }

    @Test
    public void searchPageingComplexTwoTest() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition cond = new MemberSearchCondition();

        PageRequest pageRequest = PageRequest.of(0, 6);

        Page<MemberTeamDto> result = memberRepository.searchPagingComplexTwo(cond, pageRequest);

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
                on member0_.team_id=team1_.team_id limit ?
         */
        // count 쿼리 수행 X
        System.out.println("result.getSize() : " + result.getSize());
        for (MemberTeamDto content : result.getContent()) {
            System.out.println("content : " + content);
        }
    }

    @Test
    public void QuerydslPredicateExecutorTest() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        QMember member = QMember.member;
        Iterable<Member> result = memberRepository.findAll(
                member.age.between(10, 40)
                .and(member.username.eq("member1")));

        /*
        select
            member0_.member_id as member_i1_1_,
            member0_.age as age2_1_,
            member0_.team_id as team_id4_1_,
            member0_.username as username3_1_
        from
            member member0_
        where
            (
                member0_.age between ? and ?
            )
            and member0_.username=?
         */
        /*
        [한계]
        1. 조인X (묵시적 조인은 가능하지만 left join이 불가능하다.)
        2. 클라이언트가 Querydsl에 의존해야 한다. 서비스 클래스가 Querydsl이라는 구현 기술에 의존해야 한다.
        3. 복잡한 실무환경에서 사용하기에는 한계가 명확하다.
         */
        for (Member findMember : result) {
            System.out.println("findMember : " + findMember);
        }
    }
}