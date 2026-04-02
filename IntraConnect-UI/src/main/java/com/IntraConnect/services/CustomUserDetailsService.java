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
    public UserDetails loadUserByUsername(String _email) throws UsernameNotFoundException {

        log.info("inside loadUserByUsername{}",_email);

        String query = "SELECT " +
                "APPUSERS.USERNAME, " +
                "APPUSERS.EMAIL, " +
                "APPUSERS.PASSWORD, " +
                "ROLE.ROLE " +
                "FROM APPUSERS " +
                "LEFT JOIN ROLE  ON ROLE.id = APPUSERS.role_id " +
                "WHERE APPUSERS.EMAIL = '" + _email + "'";

        try(Transaction transaction = Transaction.create()){
            ResultSet rs = transaction.select(query);
            if (!rs.next()) {
                throw new UsernameNotFoundException("User not found: " + _email);
            }
            String name = rs.getString("USERNAME");
            String email = rs.getString("EMAIL");
            String password = rs.getString("PASSWORD");
            String role = rs.getString("ROLE"); // "ADMIN" oder "USER"

            user = new com.IntraConnect.user.User(name,email, password, role);

            return User.withUsername(email)
                    .password(password)
                    .roles(role)  // Spring macht ROLE_ automatisch
                    .build();

        }catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public com.IntraConnect.user.User getUser() {
        return user;
    }
}
