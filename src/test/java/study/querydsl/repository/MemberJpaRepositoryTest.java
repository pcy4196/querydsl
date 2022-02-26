package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired MemberJpaRepository memberJpaRepository;

    @Test
    public void basicTest() {
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberJpaRepository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberJpaRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }

    @Test
    public void basicQuerydslTest() {
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        // Querydsl 작성
        List<Member> result1 = memberJpaRepository.findAll_Querydsl();
        assertThat(result1).containsExactly(member);

        // Querydsl 작성
        List<Member> result2 = memberJpaRepository.findByUsername_Querydsl("member1");
        assertThat(result2).containsExactly(member);
    }

    @Test
    public void searchTest() {
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

        List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(cond);

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
        List<MemberTeamDto> result1 = memberJpaRepository.searchByBuilder(cond1);
        assertThat(result1).extracting("username").containsExactly("member3", "member4");

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

        List<MemberTeamDto> result = memberJpaRepository.searchByWhere(cond);

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
        List<MemberTeamDto> result1 = memberJpaRepository.searchByWhere(cond1);
        assertThat(result1).extracting("username").containsExactly("member3", "member4");

    }
}