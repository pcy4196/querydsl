package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
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
//                .where(usernameEq(usernameP), ageEq(ageP))
                .where(allEq(usernameP, ageP))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameP) {
        if (usernameP != null) {
            return member.username.eq(usernameP);
        } else {
            return null;
        }
    }

    private BooleanExpression ageEq(Integer ageP) {
        return ageP != null ? member.age.eq(ageP) : null;
    }

    // null 체크는 주의해서 처리해야함
    private BooleanExpression allEq(String usernameP, Integer ageP) {
        return usernameEq(usernameP).and(ageEq(ageP));
    }

    @Test
    public void bulkUpdate() {

        // member1 = 10 --> 비회원
        // member2 = 20 --> 비회원
        // member3 = 30 --> 유지
        // member4 = 40 --> 유지

        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();

        /*
        update member set username='비회원' where age<28;
        */

        // DB랑 영속성 콘텍스트의 같이 다르다.(bulk연산 단점)

        em.flush();
        em.clear();
        // DB랑 영속성 콘텍스트의 값이 같게 설정

        List<Member> result = queryFactory
                .selectFrom(member)
                .fetch();

        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void bulkAdd() {
        long count = queryFactory
                .update(member)
                .set(member.age, member.age.add((1)))
                .execute();

        /*
        update member set age=age+1
         */
    }

    @Test
    public void bulkDel() {
        long count = queryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();
        /*
        delete from member where age>18;
         */
    }

    @Test
    public void sqlFunction() {
        List<String> result = queryFactory
                .select(Expressions.stringTemplate(
                        "function('replace', {0}, {1}, {2})",
                        member.username, "member", "M"))
                .from(member)
                .fetch();

        /*
        select
            replace(member0_.username,
            ?,
            ?) as col_0_0_
        from
            member member0_
         */
        for (String s : result) {
            System.out.println("s = " + s);
        }
        /*
        s = M1
        s = M2
        s = M3
        s = M4
         */
    }

    @Test
    public void sqlFunction2() {
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .where(member.username.eq(
                        Expressions.stringTemplate("function('lower', {0})", member.username)))
                .fetch();

        /*
        select
            member0_.username as col_0_0_
        from
            member member0_
        where
            member0_.username=lower(member0_.username)
         */
        for (String s : result) {
            System.out.println("s = " + s);
        }
        /*
        s = member1
        s = member2
        s = member3
        s = member4
        */

        List<String> result2 = queryFactory
                .select(member.username)
                .from(member)
                .where(member.username.eq(member.username.lower()))
                .fetch();

        /*
        select
            member0_.username as col_0_0_
        from
            member member0_
        where
            member0_.username=lower(member0_.username)
         */
        for (String s : result2) {
            System.out.println("s = " + s);
        }
        /*
        s = member1
        s = member2
        s = member3
        s = member4
         */
    }
}
