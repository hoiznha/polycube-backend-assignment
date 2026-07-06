package com.polycube.payment.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberGrade grade;

    public Member(MemberGrade grade) {
        if (grade == null) {
            throw new IllegalArgumentException("회원 등급은 필수입니다.");
        }
        this.grade = grade;
    }

    public void changeGrade(MemberGrade grade) {
        if (grade == null) {
            throw new IllegalArgumentException("회원 등급은 필수입니다.");
        }
        this.grade = grade;
    }
}
