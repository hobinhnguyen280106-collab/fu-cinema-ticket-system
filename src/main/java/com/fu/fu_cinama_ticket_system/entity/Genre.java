package com.fu.fu_cinama_ticket_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "genres")
@Getter
@Setter
@NoArgsConstructor
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long genreId;

    // Đổi sang NVARCHAR(50) để lưu tên thể loại tiếng Việt (Hành động, Tình cảm,...)
    @Column(nullable = false, unique = true, columnDefinition = "NVARCHAR(50)")
    private String genreName;

    // Đổi sang NVARCHAR(500) để lưu mô tả tiếng Việt
    @Column(nullable = false, columnDefinition = "NVARCHAR(500)")
    private String description;

    @OneToMany(mappedBy = "genre")
    private List<Movie> movies = new ArrayList<>();

    public Genre(String genreName, String description) {
        this.genreName = genreName;
        this.description = description;
    }
}
