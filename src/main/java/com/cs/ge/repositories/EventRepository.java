package com.cs.ge.repositories;

import com.cs.ge.entites.Event;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventRepository extends MongoRepository<Event, String> {

}
