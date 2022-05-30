package com.cs.ge.services;

import com.cs.ge.entites.Guest;
import com.cs.ge.repositories.InvitesRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InvitesService {
    private final InvitesRepository invitesRepository;

    public InvitesService(final InvitesRepository invitesRepository) {
        this.invitesRepository = invitesRepository;
    }

    public List<Guest> Search() {
        return this.invitesRepository.findAll();
    }

    public void add(final Guest guest) {
        this.invitesRepository.save(guest);
    }

    public void deleteInvite(final String id) {
        this.invitesRepository.deleteById(id);
    }

    public void UpdateInvite(final String id, final Guest guest) {
        final Optional<Guest> current = this.invitesRepository.findById(id);
        if (current.isPresent()) {
            final Guest foundGuest = current.get();
            foundGuest.setId(id);
            foundGuest.setLastName(guest.getLastName());
            foundGuest.setFirstName(guest.getFirstName());
            foundGuest.setClasse(guest.getClasse());
            foundGuest.setPlace(guest.getPlace());
            foundGuest.setTypeBillet(guest.getTypeBillet());
            this.invitesRepository.save(foundGuest);
        }
    }

}
