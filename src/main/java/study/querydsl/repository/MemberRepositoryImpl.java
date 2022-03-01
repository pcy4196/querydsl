package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition cond) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(cond.getUsername()),
                        teamnameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe())

                )
                .fetch();
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamnameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

    @Override
    public Page<MemberTeamDto> searchPagingSimple(MemberSearchCondition cond, Pageable pageable) {
        QueryResults<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(cond.getUsername()),
                        teamnameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe())

                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<MemberTeamDto> searchPagingComplex(MemberSearchCondition cond, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(cond.getUsername()),
                        teamnameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe())

                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // count 쿼리 조회 시 left 조인 불필요시 사용
        long total = queryFactory
                .select(member)
                .from(member)
//                .leftJoin(member.team, team)
                .where(
                        usernameEq(cond.getUsername()),
                        teamnameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe())
                )
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<MemberTeamDto> searchPagingComplexTwo(MemberSearchCondition cond, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(cond.getUsername()),
                        teamnameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe())

                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // count 쿼리 조회 시 left 조인 불필요시 사용
        JPAQuery<Member> countQuery = queryFactory
                .select(member)
                .from(member)
//                .leftJoin(member.team, team)
                .where(
                        usernameEq(cond.getUsername()),
                        teamnameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe())
                );

//        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchCount());
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }

}
