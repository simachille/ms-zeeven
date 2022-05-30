package com.cs.ge.services;

import com.cs.ge.entites.JwtRequest;
import com.cs.ge.entites.Utilisateur;
import com.cs.ge.entites.Verification;
import com.cs.ge.exception.ApplicationException;
import com.cs.ge.repositories.UtilisateurRepository;
import com.cs.ge.security.JwtTokenUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.cs.ge.enums.Role.CUSTOMER;
import static com.cs.ge.utils.UtilitaireService.valEmail;
import static com.cs.ge.utils.UtilitaireService.valNumber;
import static com.cs.ge.utils.UtilitaireService.validationChaine;

@Service
public class UtilisateursService {

    private static final String ACCOUNT_NOT_EXISTS = "Aucun compte ne correspond à %s %s.";
    private final UtilisateurRepository utilisateurRepository;
    private final VerificationService verificationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    public UtilisateursService(final UtilisateurRepository utilisateurRepository, final VerificationService verificationService, final PasswordEncoder passwordEncoder, final JwtTokenUtil jwtTokenUtil) {
        this.utilisateurRepository = utilisateurRepository;
        this.verificationService = verificationService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
    }


    public void activate(final String code) {
        final Verification verification = this.verificationService.getByCode(code);
        Utilisateur utilisateur = verification.getUtilisateur();
        utilisateur = this.utilisateurRepository.findById(utilisateur.getId()).orElseThrow(() -> new ApplicationException("aucun utilisateur pour ce code"));
        utilisateur.setEnabled(true);
        final LocalDateTime localDateTime = verification.getDateExpiration();
        //if (localDateTime=)
        this.utilisateurRepository.save(utilisateur);
    }

    public void validationUsername(final String username) {
        final Optional<Utilisateur> exist = this.utilisateurRepository.findById(username);
        if (exist.isPresent()) {
            throw new ApplicationException("Username existe déjà");
        }
    }

    public void add(final Utilisateur utilisateur) { // en entrée je dois avoir quelque chose sous la forme d'un Utilisateur de type utilisateur
        this.validationUsername(utilisateur.getUsername());
        String lastName = utilisateur.getLastName();
        lastName = lastName.toUpperCase();
        utilisateur.setLastName(lastName);
        this.utilisateurRepository.save(utilisateur);
    }

    public List<Utilisateur> search() {
        return this.utilisateurRepository.findAll();
    }

    public void deleteUtilisateur(final String id) {
        this.utilisateurRepository.deleteById(id);
    }

    public void updateUtilisateur(final String id, final Utilisateur utilisateur) {
        final Optional<Utilisateur> current = this.utilisateurRepository.findById(id);
        if (current.isPresent()) {
            final Utilisateur foundUser = current.get();
            foundUser.setId(id);
            foundUser.setFirstName(utilisateur.getFirstName());
            foundUser.setLastName(utilisateur.getLastName());
            foundUser.setEmail(utilisateur.getEmail());
            foundUser.setEmail(utilisateur.getPhone());
            this.utilisateurRepository.save(foundUser);
        }
    }


    public void inscription(final Utilisateur utilisateur) throws MessagingException, IOException {
        this.checkAccount(utilisateur);
        utilisateur.setRole(CUSTOMER);
        utilisateur.setPassword(this.passwordEncoder.encode(utilisateur.getPassword()));
        final String encodedPassword = this.passwordEncoder.encode(utilisateur.getPassword());
        utilisateur.setPassword(encodedPassword);
        valEmail(utilisateur.getUsername());
        valNumber(utilisateur.getUsername());
        this.utilisateurRepository.save(utilisateur);
        final Verification verification = this.verificationService.createCode(utilisateur);
        if (utilisateur.getEmail() != null) {
            this.verificationService.sendEmail(utilisateur, verification.getCode());
        }
    }

    private void checkAccount(final Utilisateur utilisateur) {
        if (
                (utilisateur.getEmail() == null || utilisateur.getEmail().trim().isEmpty())
                        && (utilisateur.getPhone() == null || utilisateur.getPhone().trim().isEmpty())
        ) {
            throw new ApplicationException("Veuillez sair l'email ou votre téléphone");
        }

        validationChaine(utilisateur.getFirstName());
        validationChaine(utilisateur.getLastName());
        valEmail(utilisateur.getEmail());
        valNumber(utilisateur.getPhone());
        final Optional<Utilisateur> userByEmail = this.utilisateurRepository.findByUsername(utilisateur.getEmail());
        if (userByEmail.isPresent()) {
            throw new IllegalArgumentException("Cet email est déjà utilsé");
        }

        final Optional<Utilisateur> userByPhone = this.utilisateurRepository.findByUsername(utilisateur.getPhone());
        if (userByPhone.isPresent()) {
            throw new IllegalArgumentException("Ce téléphone est déjà utilsé");
        }
    }

    public String connexion(final JwtRequest authenticationRequest) {
        final String usermame = authenticationRequest.getUsername();
        final String password = authenticationRequest.getPassword();

        final Optional<Utilisateur> optionalProfile = this.utilisateurRepository.findByUsername(usermame);
        if (optionalProfile.isEmpty()) {
            throw new IllegalArgumentException(String.format(ACCOUNT_NOT_EXISTS, "l'email", usermame));
        }

        return this.jwtTokenUtil.generateToken(optionalProfile.get());
    }
}
