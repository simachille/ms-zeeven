package com.cs.ge.entites;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Schedule {
    private String id;
    private String title;
    private String note;
    private String location;
    private String end;
    private String start;
    private Instant date;
}
