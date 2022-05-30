package com.cs.ge.entites;

import com.cs.ge.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Document("EVENEMENTS")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Event {
    @Id
    private String id;
    private String name;
    private EventStatus status;
    private Categorie categorie;
    private Set<Instant> days;
    private List<Guest> contacts;
    private List<Guest> guests;
}
