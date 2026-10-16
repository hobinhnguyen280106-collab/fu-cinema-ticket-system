package com.fu.fu_cinama_ticket_system.entity;

import com.fu.fu_cinama_ticket_system.enums.AgeRating;
import com.fu.fu_cinama_ticket_system.enums.MovieStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long movieId;

    // Tiêu đề phim tiếng Việt: NVARCHAR(200)
    @Column(nullable = false, columnDefinition = "NVARCHAR(200)")
    private String title;

    // Đạo diễn: NVARCHAR(100)
    @Column(nullable = false, columnDefinition = "NVARCHAR(100)")
    private String director;

    @Column(nullable = false)
    private Integer duration; // phút

    @Column(nullable = false)
    private LocalDate releaseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AgeRating ageRating;

    // Mô tả chi tiết phim: NVARCHAR(2000)
    @Column(nullable = false, columnDefinition = "NVARCHAR(2000)")
    private String description;

    // URL ảnh poster thì không dấu nên giữ VARCHAR(500) là đủ
    @Column(nullable = false, length = 500)
    private String posterUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovieStatus status;
}
