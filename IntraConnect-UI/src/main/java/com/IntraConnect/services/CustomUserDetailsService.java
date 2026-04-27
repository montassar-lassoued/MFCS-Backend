package com.IntraConnect.services;

import com.IntraConnect.queryExec.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;

@Service
public class CustomUserDetailsService implements UserDetailsService {


    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private com.IntraConnect.user.User user;
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		log.info("Lade User Details für: {}", email);
		
		String query = "SELECT " +
				"APPUSERS.USERNAME, " +
				"APPUSERS.EMAIL, " +
				"APPUSERS.PASSWORD, " +
				"ROLE.ROLE " +
				"FROM APPUSERS " +
				"LEFT JOIN ROLE ON ROLE.id = APPUSERS.role_id " +
				"WHERE APPUSERS.EMAIL = ?";
		
		try (Transaction transaction = Transaction.create()) {
			ResultSet rs = transaction.select(query, email);
			
			if (!rs.next()) {
				throw new UsernameNotFoundException("User nicht gefunden: " + email);
			}
			
			// Wir geben ein sauberes Spring Security User-Objekt zurück
			return User.withUsername(rs.getString("EMAIL"))
					.password(rs.getString("PASSWORD"))
					.roles(rs.getString("ROLE"))
					.build();
			
		} catch (Exception e) {
			log.error("Datenbankfehler in CustomUserDetailsService: ", e);
			throw new UsernameNotFoundException("Fehler beim Laden des Users", e);
		}
	}
	
}
