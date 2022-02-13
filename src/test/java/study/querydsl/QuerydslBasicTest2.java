package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest2 {

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
    public void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        /*
        select member0_.member_id as member_i1_1_,
               member0_.age as age2_1_,
               member0_.team_id as team_id4_1_,
               member0_.username as username3_1_
          from member member0_
         order by member0_.username desc
         limit 2 offset 1;
        */
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void paging2() {
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }

    @Test
    public void aggregation() {
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

       /*
       select
            count(member1),
            sum(member1.age),
            avg(member1.age),
            max(member1.age),
            min(member1.age)
        from
            Member member1
        */


        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구하라.
     */
    @Test
    public void group() {
        List<Tuple> result = queryFactory
                .select(
                        team.name,
                        member.age.avg()
                )
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        /* select
        team.name,
        avg(member1.age)
    from
        Member member1
    inner join
        member1.team as team
    group by
        team.name */
        /*
        select
            team1_.name as col_0_0_,
            avg(cast(member0_.age as double)) as col_1_0_
        from
            member member0_
        inner join
            team team1_
                on member0_.team_id=team1_.team_id
        group by
            team1_.name
         */

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);


        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15); // (10 + 20) / 2

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35); // (30 + 40) / 2
    }

    /**
     * 팀 A에 소속된 모든 회원
     */
    @Test
    public void join() {
        List<Member> result = queryFactory
                .select(member)
                .from(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        /*
        select
            member1
        from
            Member member1
        inner join
            member1.team as team
        where
            team.name = ?1
        */
        /*
        select
        member0_.member_id as member_i1_1_,
                member0_.age as age2_1_,
        member0_.team_id as team_id4_1_,
                member0_.username as username3_1_
        from
        member member0_
        inner join
        team team1_
        on member0_.team_id=team1_.team_id
        where
        team1_.name=?
        */

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    @Test
    public void join1() {
        List<Member> result = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        /*
        select
            member1
        from
            Member member1
        left join
            member1.team as team
        where
            team.name = ?1
        */
        /*
        select
        member0_.member_id as member_i1_1_,
                member0_.age as age2_1_,
        member0_.team_id as team_id4_1_,
                member0_.username as username3_1_
        from
        member member0_
        left outer join
        team team1_
        on member0_.team_id=team1_.team_id
        where
        team1_.name=?
        */

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인
     * 회원이 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        /*
        select
            member1
        from
            Member member1,
            Team team
        where
            member1.username = team.name
        */
        /*
        select
        member0_.member_id as member_i1_1_,
                member0_.age as age2_1_,
        member0_.team_id as team_id4_1_,
                member0_.username as username3_1_
        from
        member member0_ cross
                join
        team team1_
        where
        member0_.username=team1_.name
         */
        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");

    }

    /**
     * 예1) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인,회원은 모두 조회
     * JPQL : select m, t from Member m left join m.team t on t.name = 'teamA'
     * 예2~3) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인 (inner join)
     * JPQL : select m, t from Member m join m.team t on t.name = 'teamA'
     */
    @Test
    public void joinOnFiltering() {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        /*
        select member0_.member_id as member_i1_1_0_
        , team1_.team_id as team_id1_2_1_
        , member0_.age as age2_1_0_
        , member0_.team_id as team_id4_1_0_
        , member0_.username as username3_1_0_
        , team1_.name as name2_2_1_
        from member member0_
        left outer join team team1_
        on member0_.team_id=team1_.team_id
            and (team1_.name=?)
         */
        for (Tuple tuple : result) {
            System.out.println("tuple : " + tuple);
        }

        List<Tuple> result1 = queryFactory
                .select(member, team)
                .from(member)
                .join(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        /*
        select
            member0_.member_id as member_i1_1_0_,
            team1_.team_id as team_id1_2_1_,
            member0_.age as age2_1_0_,
            member0_.team_id as team_id4_1_0_,
            member0_.username as username3_1_0_,
            team1_.name as name2_2_1_
        from
            member member0_
        inner join
            team team1_
                on member0_.team_id=team1_.team_id
                and (
                    team1_.name=?
                )
         */
        for (Tuple tuple : result1) {
            System.out.println("tuple : " + tuple);
        }

        // result1 보다 더많이 사용하는 방법 - 결과는 같다
        List<Tuple> result2 = queryFactory
                .select(member, team)
                .from(member)
                .join(member.team, team)
//                .on(team.name.eq("teamA"))
                .where(team.name.eq("teamA"))
                .fetch();
        /*
        select
            member0_.member_id as member_i1_1_0_,
            team1_.team_id as team_id1_2_1_,
            member0_.age as age2_1_0_,
            member0_.team_id as team_id4_1_0_,
            member0_.username as username3_1_0_,
            team1_.name as name2_2_1_
        from
            member member0_
        inner join
            team team1_
                on member0_.team_id=team1_.team_id
        where
            team1_.name=?
         */
        for (Tuple tuple : result2) {
            System.out.println("tuple : " + tuple);
        }
    }

    /**
     * 연관관계가 없는 엔티티 외부조인
     * 회원이 이름이 팀 이름과 같은 대상 외부조인
     */
    @Test
    public void joinOnNoRelation() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
//                .leftJoin(member.team, team)
                .leftJoin(team)
                .on(member.username.eq(team.name))
                .fetch();

        /*
        select
            member1,
            team
        from
            Member member1
        left join
            Team team with member1.username = team.name
        */
        /*
        select
            member0_.member_id as member_i1_1_0_,
            team1_.team_id as team_id1_2_1_,
            member0_.age as age2_1_0_,
            member0_.team_id as team_id4_1_0_,
            member0_.username as username3_1_0_,
            team1_.name as name2_2_1_
        from
            member member0_
        left outer join
            team team1_
                on (
                    member0_.username=team1_.name
                )
         */

        for (Tuple tuple : result) {
            System.out.println("tuple : " + tuple);
        }

        /*
        tuple : [Member(id=3, username=member1, age=10), null]
        tuple : [Member(id=4, username=member2, age=20), null]
        tuple : [Member(id=5, username=member3, age=30), null]
        tuple : [Member(id=6, username=member4, age=40), null]
        tuple : [Member(id=7, username=teamA, age=0), Team(id=1, name=teamA)]
        tuple : [Member(id=8, username=teamB, age=0), Team(id=2, name=teamB)]
        tuple : [Member(id=9, username=teamC, age=0), null]
         */
//        assertThat(result)
//                .extracting("username")
//                .containsExactly("teamA", "teamB");

    }
}
