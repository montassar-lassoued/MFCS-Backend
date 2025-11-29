package com.pilot.services;

import com.pilot.queryExec.QueryExecutor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

//@Service
public class CustomUserDetailsService implements UserDetailsService {


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        QueryExecutor executor = QueryExecutor.Create();
        ResultSet rs = executor.submitSelect(
                "SELECT" +
                        "    APPUSERS.NAME," +
                        "    APPUSERS.EMAIL," +
                        "    ROLE.ROLE" +
                        "FROM APPUSERS " +
                        "JOIN ROLE  ON ROLE.id = APPUSERS.role_id " +
                        "WHERE USERNAME = '" + username + "'");

        try {
            if (!rs.next()) {
                throw new UsernameNotFoundException("User not found: " + username);
            }

            String dbUsername = rs.getString("USERNAME");
            String dbPassword = rs.getString("PASSWORD");
            String Role = rs.getString("ROLE"); // "ADMIN" oder "USER"


            return User.withUsername(dbUsername)
                    .password(dbPassword)
                    .roles(Role)  // Spring macht ROLE_ automatisch
                    .build();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
