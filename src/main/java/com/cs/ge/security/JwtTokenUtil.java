package com.cs.ge.security;

import com.cs.ge.entites.Utilisateur;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Clock;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    private static final long serialVersionUID = -3301605591108950415L;
    private final Clock clock = DefaultClock.INSTANCE;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String getUsernameFromToken(final String token) {
        return this.getClaimFromToken(token, Claims::getSubject);
    }

    public Date getIssuedAtDateFromToken(final String token) {
        return this.getClaimFromToken(token, Claims::getIssuedAt);
    }

    public Date getExpirationDateFromToken(final String token) {
        return this.getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(final String token, final Function<Claims, T> claimsResolver) {
        final Claims claims = this.getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(final String token) {
        return Jwts.parser()
                .setSigningKey(this.secret)
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(final String token) {
        final Date expiration = this.getExpirationDateFromToken(token);
        return expiration.before(this.clock.now());
    }

    private static Boolean isCreatedBeforeLastPasswordReset(final Date created, final Date lastPasswordReset) {
        return (lastPasswordReset != null && created.before(lastPasswordReset));
    }

    private static Boolean ignoreTokenExpiration(final String token) {
        // here you specify tokens, for that the expiration is ignored
        return false;
    }

    public String generateToken(final Utilisateur profile) {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("role", profile.getRole());
        claims.put("lastName", profile.getLastName());
        claims.put("firstName", profile.getFirstName());
        claims.put("name", profile.getFirstName() + " " + profile.getFirstName());
        claims.put("email", profile.getUsername());
        return this.doGenerateToken(claims, profile.getUsername());
    }

    private String doGenerateToken(final Map<String, Object> claims, final String subject) {
        final Date createdDate = this.clock.now();
        final Date expirationDate = JwtTokenUtil.calculateExpirationDate(createdDate);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, this.secret)
                .compact();
    }

    public Boolean canTokenBeRefreshed(final String token, final Date lastPasswordReset) {
        final Date created = this.getIssuedAtDateFromToken(token);
        return !JwtTokenUtil.isCreatedBeforeLastPasswordReset(created, lastPasswordReset)
                && (!this.isTokenExpired(token) || JwtTokenUtil.ignoreTokenExpiration(token));
    }

    public String refreshToken(final String token) {
        final Date createdDate = this.clock.now();
        final Date expirationDate = JwtTokenUtil.calculateExpirationDate(createdDate);

        final Claims claims = this.getAllClaimsFromToken(token);
        claims.setIssuedAt(createdDate);
        claims.setExpiration(expirationDate);

        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, this.secret)
                .compact();
    }

    public Boolean validateToken(final String token, final UserDetails userDetails) {
        final Utilisateur user = (Utilisateur) userDetails;
        final String username = this.getUsernameFromToken(token);
        final Date created = this.getIssuedAtDateFromToken(token);
        // final Date expiration = getExpirationDateFromToken(token);
        return (
                username.equals(user.getUsername())
                        && !this.isTokenExpired(token)
                //&& !isCreatedBeforeLastPasswordReset(created, user.getLastPasswordResetDate())
        );
    }

    private static Date calculateExpirationDate(final Date createdDate) {
        final Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR, 6);
        return now.getTime();
    }
}
