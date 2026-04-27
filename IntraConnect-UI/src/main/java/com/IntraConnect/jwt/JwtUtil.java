package com.IntraConnect.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil {
	
	private final Key key;
	// Spring sucht nach einer Umgebungsvariable namens JWT_SECRET
	// oder einem Eintrag jwt.secret in den Properties.
	public JwtUtil(@Value("${jwt.secret}") String secret) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes());
	}

    public  String extractUsername(String token){
        return extractClaims(token, Claims::getSubject);
    }

    public Date extractExpiration(String token){
        return extractClaims(token, Claims::getExpiration);
    }

    public <T> T extractClaims(String token, Function<Claims, T> claimsTResolver){
        final Claims claims = extractAllClaims(token);

        return claimsTResolver.apply(claims);
    }
	
	public Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith((SecretKey) key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

    public String generateToken(String username, String role){
        Map<String, Object> claims = new HashMap<>();
        claims.put("role",role);
        return createToken(claims, username);
    }

    public boolean validateToken(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    private String createToken(Map<String, Object> claims, String username){
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+1000*60*60*10))
                .signWith(SignatureAlgorithm.HS256, key).compact();
    }
}
