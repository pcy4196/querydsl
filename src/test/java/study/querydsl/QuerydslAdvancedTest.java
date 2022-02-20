package study.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.hibernate.criterion.Projection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static study.querydsl.entity.QMember.*;

@SpringBootTest
@Transactional
public class QuerydslAdvancedTest {

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
    public void simpleProjecion() {
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void tupleProjection() {

        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        /*
        select
            member0_.username as col_0_0_,
            member0_.age as col_1_0_
        from
            member member0_
         */

        /*
        username = member1
        age = 10
        username = member2
        age = 20
        username = member3
        age = 30
        username = member4
        age = 40
         */
        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            int age = tuple.get(member.age);
            System.out.println("username = " + username);
            System.out.println("age = " + age);
        }
    }

    @Test
    public void findDtoByJPQL() {
        List<MemberDto> result = em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age)" +
                        " from Member m", MemberDto.class)
                .getResultList();

        /*
        select
            member0_.username as col_0_0_,
            member0_.age as col_1_0_
        from
            member member0_
         */
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }
    
    @Test
    public void findDtoBySetter() {
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        /*
        select
            member0_.username as col_0_0_,
            member0_.age as col_1_0_
        from
            member member0_
         */

        /*
        memberDto = MemberDto(username=member1, age=10)
        memberDto = MemberDto(username=member2, age=20)
        memberDto = MemberDto(username=member3, age=30)
        memberDto = MemberDto(username=member4, age=40)
         */
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoByField() {
        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        /*
        select
            member0_.username as col_0_0_,
            member0_.age as col_1_0_
        from
            member member0_
         */

        /*
        memberDto = MemberDto(username=member1, age=10)
        memberDto = MemberDto(username=member2, age=20)
        memberDto = MemberDto(username=member3, age=30)
        memberDto = MemberDto(username=member4, age=40)
         */
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoByConstructor() {
        // 생성자
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        /*
        select
            member0_.username as col_0_0_,
            member0_.age as col_1_0_
        from
            member member0_
         */

        /*
        memberDto = MemberDto(username=member1, age=10)
        memberDto = MemberDto(username=member2, age=20)
        memberDto = MemberDto(username=member3, age=30)
        memberDto = MemberDto(username=member4, age=40)
         */
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findUserDtoByConstructor() {
        QMember memberSub = new QMember("memberSub");
        List<UserDto> result = queryFactory
                .select(Projections.constructor(UserDto.class,
                        member.username.as("name"),

                        ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub), "age")
                ))
                .from(member)
                .fetch();

        /*
        select
            member0_.username as col_0_0_,
            (select
                max(member1_.age)
            from
                member member1_) as col_1_0_
        from
            member member0_
         */
        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
        /*
        userDto = UserDto(name=member1, age=40)
        userDto = UserDto(name=member2, age=40)
        userDto = UserDto(name=member3, age=40)
        userDto = UserDto(name=member4, age=40)
         */
    }

    @Test
    public void findUserDtoByField() {
        QMember memberSub = new QMember("memberSub");
        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),

                        ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub), "age")
                ))
                .from(member)
                .fetch();

        /*
        select
            member0_.username as col_0_0_,
            (select
                max(member1_.age)
            from
                member member1_) as col_1_0_
        from
            member member0_
         */
        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
        /*
        userDto = UserDto(name=member1, age=40)
        userDto = UserDto(name=member2, age=40)
        userDto = UserDto(name=member3, age=40)
        userDto = UserDto(name=member4, age=40)
         */
    }

    @Test
    public void findDtoByQueryProjection() {
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        /*
        select
            member0_.username as col_0_0_,
            member0_.age as col_1_0_
        from
            member member0_
         */
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + result);
        }

        // 단점 : 1. Q파일 생성 2. DTO가   Querydsl 의존성을 가진다.
    }
}
