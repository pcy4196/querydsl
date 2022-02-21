package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
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
import static study.querydsl.entity.QMember.*;

@SpringBootTest
@Transactional
public class QuerydslAdvancedTest2 {

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
    public void dynamicQueryBooleanBuilder() {
        String usernameP = "member1";
        Integer ageP = 10;

        List<Member> result = searchMember1(usernameP, ageP);

        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameP, Integer ageP) {

        BooleanBuilder builder = new BooleanBuilder();
        if (usernameP != null) {
            builder.and(member.username.eq(usernameP));
        }
        if (ageP != null) {
            builder.and(member.age.eq(ageP));
        }

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
        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }


    @Test
    public void dynamicQueryWhereParam() {
        String usernameP = "member1";
        Integer ageP = 10;

        List<Member> result = searchMember2(usernameP, ageP);
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

        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameP, Integer ageP) {
        return queryFactory
                .selectFrom(member)
                .where(usernameEq(usernameP), ageEq(ageP))
                .fetch();
    }

    private Predicate usernameEq(String usernameP) {
        if (usernameP != null) {
            return member.username.eq(usernameP);
        } else {
            return null;
        }
    }

    private Predicate ageEq(Integer ageP) {
        return ageP != null ? member.age.eq(ageP) : null;
    }
}
