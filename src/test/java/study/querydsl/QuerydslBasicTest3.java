package study.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
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

import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
public class QuerydslBasicTest3 {

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

    /**
     * 나이가 가장 많은 회원 조회
     */
    @Test
    public void subQuery() {

        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        /*
        select
            member0_.member_id as member_i1_1_,
            member0_.age as age2_1_,
            member0_.team_id as team_id4_1_,
            member0_.username as username3_1_
        from
            member member0_
        where
            member0_.age=(
                select
                    max(member1_.age)
                from
                    member member1_
            )
         */
        assertThat(result).extracting("age")
                .containsExactly(40);
    }

    /**
     * 나이가 평균 이상인 회원
     */
    @Test
    public void subQueryGoe() {

        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        /*
        select
            member0_.member_id as member_i1_1_,
            member0_.age as age2_1_,
            member0_.team_id as team_id4_1_,
            member0_.username as username3_1_
        from
            member member0_
        where
            member0_.age>=(
                select
                    avg(cast(member1_.age as double))
                from
                    member member1_
            )
         */

        assertThat(result).extracting("age")
                .contains(40, 30);
    }

    /**
     * 나이가 10 초과인 회원
     */
    @Test
    public void subQueryIn() {

        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        /*
        select
            member0_.member_id as member_i1_1_,
            member0_.age as age2_1_,
            member0_.team_id as team_id4_1_,
            member0_.username as username3_1_
        from
            member member0_
        where
            member0_.age in (
                select
                    member1_.age
                from
                    member member1_
                where
                    member1_.age>?
            )
         */

        assertThat(result).extracting("age")
                .contains(40, 30, 20);
    }

    @Test
    public void selectSubquery() {

        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = queryFactory
                .select(member.username,
                        select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();
        /*
        select
            member0_.username as col_0_0_,
            (select
                avg(cast(member1_.age as double))
            from
                member member1_) as col_1_0_
        from
            member member0_
         */
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /*
        from 절의 서브쿼리 해결방안(inline 서브쿼리 불가 --> JPA 단점)
        1. 서브쿼리를 join으로 변경한다. (가능한 상황도 있고, 불가능한 상황도 있다.)
        2. 애플리케이션에서 쿼리를 2번 분리해서 실행한다.
        3. nativeSQL을 사용한다.
    */


    @Test
    public void basicCase() {
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        /*
        select
            case
                when member0_.age=? then ?
                when member0_.age=? then ?
                else '기타'
            end as col_0_0_
        from
            member member0_
         */

        for (String s : result) {
            System.out.println("s = " + s);
        }
        /*
        s = 열살
        s = 스무살
        s = 기타
        s = 기타
        */
    }


    @Test
    public void cplexCase() {
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타").as("ageStr"))
                .from(member)
                .fetch();

        /*
        select
            case
                when member0_.age between ? and ? then ?
                when member0_.age between ? and ? then ?
                else '기타'
            end as col_0_0_
        from
            member member0_
         */

        for (String s : result) {
            System.out.println("s = " + s);
            assertThat(s).isIn("0~20살", "21~30살", "기타");
        }

        /*
        s = 0~20살
        s = 0~20살
        s = 21~30살
        s = 기타
        */
    }

    /**
     * 다음과 같은 임의의 순서로 회원을 출력하고 싶다면?
     * 1. 0 ~ 30살이 아닌 회원을 가장 먼저 출력
     * 2. 0 ~ 20살 회원 출력
     * 3. 21 ~ 30살 회원 출력
     */
    @Test
    public void rankPath() {
        NumberExpression<Integer> rankPath = new CaseBuilder()
                .when(member.age.gt(30)).then(3)
                .when(member.age.between(0, 20)).then(2)
                .otherwise(1);

        List<Tuple> result = queryFactory
                .select(member.age
                        , member.username
                        , rankPath
                )
                .from(member)
                .orderBy(rankPath.desc())
                .fetch();
        /*
        select
            member0_.age as col_0_0_,
            member0_.username as col_1_0_,
            case
                when member0_.age>? then ?
                when member0_.age between ? and ? then ?
                else 1
            end as col_2_0_
        from
            member member0_
        inner join
            team team1_
                on member0_.team_id=team1_.team_id
        order by
            case
                when member0_.age>?30 then ?3
                when member0_.age between ?0 and ?20 then ?2
                else 1
            end desc
         */
        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            Integer rank = tuple.get(rankPath);
            System.out.println("username = " + username + " age = " + age + " rank = " + rank);
        }
        /*
        username = member4 age = 40 rank = 3
        username = member1 age = 10 rank = 2
        username = member2 age = 20 rank = 2
        username = member3 age = 30 rank = 1
        */
    }

    @Test
    public void constant() {
        List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        /*
        select
            member0_.username as col_0_0_
        from
            member member0_
         */
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void concat() {
        List<String> result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                // stringValue enum 처리시 많이 사용
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();

        /*
        select
            ((member0_.username||?)||cast(member0_.age as char)) as col_0_0_
        from
            member member0_
        where
            member0_.username=?
         */
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }
}
