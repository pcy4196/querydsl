package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
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
    }

    @Test
    public void startJPQL() {
        // member1 을 search
        String qlString = "select m from Member m where m.username = :username";
        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {
        QMember m = new QMember("m");

        Member findMember = queryFactory
                        .select(m)
                        .from(m)
                        .where(m.username.eq("member1")) // 파라미터 바인딩 처리
                        .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydslBasic() {
        Member findMember = queryFactory
                .select(member) // static import
                .from(member)
                .where(member.username.eq("member1")) // 파라미터 바인딩 처리
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");

        // 같은 테이블을 JOIN 할 경우에만 사용
        QMember m1 = new QMember("m1");

        Member findMember1 = queryFactory
                .select(m1) // static import
                .from(m1)
                .where(m1.username.eq("member1")) // 파라미터 바인딩 처리
                .fetchOne();

        assertThat(findMember1.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.between(10, 30)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10)
                )
                .fetchOne();

        /*
        select
            member0_.member_id as member_i1_1_,
            member0_.age as age2_1_,
            member0_.team_id as team_id4_1_,
            member0_.username as username3_1_
        from
            member member0_
        where
            member0_.username=?
            and member0_.age=?
         */

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch() {
//        List<Member> fetch = queryFactory
//                .selectFrom(member)
//                .fetch();
//
//        Member fetchOne = queryFactory
//                .selectFrom(QMember.member)
//                .fetchOne();
//
//        Member fetchFirst = queryFactory
//                .selectFrom(QMember.member)
//                .fetchFirst();

        QueryResults<Member> result = queryFactory
                .selectFrom(member)
                .fetchResults();

        result.getTotal();
        /*
        select
            count(member0_.member_id) as col_0_0_
        from
            member member0_
         */

        List<Member> content = result.getResults();
        /*
        select
            member0_.member_id as member_i1_1_,
            member0_.age as age2_1_,
            member0_.team_id as team_id4_1_,
            member0_.username as username3_1_
        from
            member member0_
         */

        long total = queryFactory
                .selectFrom(member)
                .fetchCount();
        /*
        - Querydsl -
        select
        count(member1)
        from
        Member member1
        */
        /*
        - SQL -
        select
        count(member0_.member_id) as col_0_0_
        from
        member member0_
         */
    }
}
