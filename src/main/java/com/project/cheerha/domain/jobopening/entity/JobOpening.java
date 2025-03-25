package com.project.cheerha.domain.jobopening.entity;

import com.project.cheerha.domain.keyword.entity.JobOpeningKeyword;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.ZonedDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "job_opening")
@EntityListeners(AuditingEntityListener.class)
public class JobOpening  implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    private String title;

    @Column(length = 255, nullable = false)
    private String company;

    @Column(length = 255, nullable = false)
    private String location;

    private int salary;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private EducationLevel educationLevel;

    @Column(length = 255, nullable = false)
    private String jobOpeningUrl;

    //Integer을 사용한 이유는 경력에 경력없는 신입(0년)일 경우도 있지만, 경력무관(null)인 경우도 있다.
    //이로 인해 int를 사용하면 null(경력무관)을 쓸 수 없기 때문에 Integer 사용
    @Column(nullable = true)
    private Integer minExperienceYears;

    @Column(nullable = true)
    private Integer maxExperienceYears;

    @Column(length = 255, nullable = false)
    private String position;  // 포지션 (직무명 예시: SW개발)

    //해외 사이트 적용을 생각해서 ZonedDateTime 사용
    private ZonedDateTime hiringStartAt;
    private ZonedDateTime hiringEndAt;

    // 채용 공고가 생성된, 즉 올라온 날짜
    private ZonedDateTime createdAt = ZonedDateTime.now();

    private int viewCount;

    @OneToMany(mappedBy = "jobOpening", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobOpeningKeyword> jobOpeningKeywordList = new ArrayList<>();

    /**
     * JobOpening 엔티티를 생성하는 유틸리티 메서드입니다.
     *
     * 이 메서드는 제공된 파라미터들을 기반으로 새로운 JobOpening 객체를 생성합니다.
     * 이 메서드를 사용하여 새로운 JobOpening 객체를 편리하게 생성할 수 있습니다.
     */
    public static JobOpening toEntity(
            String title,
            String company,
            String location,
            int salary,
            EmploymentType employmentType,
            EducationLevel educationLevel,
            String jobOpeningUrl,
            Integer minExperienceYears,
            Integer masExperienceYears,
            String position,
            ZonedDateTime hiringStartAt,
            ZonedDateTime hiringEndAt
    ){
        JobOpening jobOpening = new JobOpening();
        jobOpening.title = title;
        jobOpening.company = company;
        jobOpening.location = location;
        jobOpening.salary = salary;
        jobOpening.employmentType = employmentType;
        jobOpening.educationLevel = educationLevel;
        jobOpening.jobOpeningUrl = jobOpeningUrl;
        jobOpening.minExperienceYears = minExperienceYears;
        jobOpening.maxExperienceYears = masExperienceYears;
        jobOpening.position = position;
        jobOpening.hiringStartAt = hiringStartAt;
        jobOpening.hiringEndAt = hiringEndAt;
        return jobOpening;
    }

    /**
     * JobOpening의 id를 Integer로 변환하여 반환하는 메서드입니다.
     *
     * 이 메서드는 JobOpening의 id 값을 Integer로 변환하여 반환합니다.
     * 이 값을 사용하여 다른 객체나 시스템에서 id를 활용할 수 있도록 합니다.
     *
     * @return JobOpening의 id 값 (Integer 형)
     */
    public Integer getJobOpeningId() {
        return this.id != null ? this.id.intValue() : null;
    }
}
