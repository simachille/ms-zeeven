package com.cs.ge.services;

import com.cs.ge.entites.Event;
import com.cs.ge.entites.Guest;
import com.cs.ge.enums.EventStatus;
import com.cs.ge.exception.ApplicationException;
import com.cs.ge.repositories.AdresseRepository;
import com.cs.ge.repositories.EventRepository;
import com.cs.ge.repositories.UtilisateurRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EventService {
    private final EventRepository eventsRepository;
    //private final AdresseRepository adresseRepository;
    //private final UtilisateurRepository utilisateurRepository;

    public EventService(final EventRepository eventsRepository, final AdresseRepository adresseRepository, final UtilisateurRepository utilisateurRepository) {
        this.eventsRepository = eventsRepository;
        //this.adresseRepository = adresseRepository;
        //this.utilisateurRepository = utilisateurRepository;
    }

    public List<Event> search() {
        return this.eventsRepository.findAll();
    }

    public void add(final Event event) {

        if (event.getName() == null || event.getName().trim().isEmpty()) {
            throw new ApplicationException("Champs obligatoire");
        }
        //final EventStatus status = EventService.eventStatus(event.getDateDebut(), event.getDateFin());
        //event.setStatut(status);

        //Adresse adresse = adresseRepository.save(event.getAdresse());
        //event.	setAdresse(adresse);

        //Utilisateur utilisateur= utilisateurRepository.save(event.getUtilisateur());
        //event.setUtilisateur(utilisateur);
        this.eventsRepository.save(event);
    }

    private static EventStatus eventStatus(final Date dateDebut, final Date dateFin) {
        final Date date = new Date();
        EventStatus status = EventStatus.DISABLED;

        if (dateDebut.before(date)) {
            throw new ApplicationException("La date de votre évènement est invalide");
        }

        if (dateDebut.equals(date)) {
            status = EventStatus.ACTIVE;
        }

        if (dateFin.after(date)) {
            status = EventStatus.INCOMMING;
        }

        return status;
    }

    public void delete(final String id) {
        this.eventsRepository.deleteById(id);
    }

    public void update(final String id, final Event event) {
        final Optional<Event> current = this.eventsRepository.findById(id);
        if (current.isPresent()) {
            final Event foundEvents = current.get();
            foundEvents.setId(id);
            foundEvents.setName(event.getName());
            foundEvents.setStatus(event.getStatus());
            this.eventsRepository.save(foundEvents);
        }
    }

    public Event read(final String id) {
        return this.eventsRepository.findById(id).orElseThrow(
                () -> new ApplicationException("Aucune enttité ne correspond au critères fournis")
        );
    }

    public void addInvites(final String id, final Guest guest) {
        final UUID uuid = UUID.randomUUID();
        final String uuidAsString = uuid.toString();
        guest.setId(uuidAsString);
        final Event event = this.read(id);
        List<Guest> guests = event.getGuests();
        if (guests == null) {
            guests = new ArrayList<>();
        }
        guests.add(guest);
        event.setGuests(guests);
        this.eventsRepository.save(event);
    }

    public List<Guest> guests(final String id) {
        final Event event = this.read(id);
        return event.getGuests();
    }
}
