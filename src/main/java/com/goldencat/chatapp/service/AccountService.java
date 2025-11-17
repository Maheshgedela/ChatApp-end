package com.goldencat.chatapp.service;

import com.goldencat.chatapp.model.Account;
import com.goldencat.chatapp.model.Status;
import com.goldencat.chatapp.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

        private final PasswordEncoder passwordEncoder;
        private final AccountRepository accountRepository;

        // ✅ Find account by username (throws if not found)
        public Account findAccountByUsername(String username) {
            return accountRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
        }

        // ✅ Used by Spring Security during login
        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            Account account = findAccountByUsername(username);
            return new Account(
                    account.getUsername(),
                    account.getPassword(),
                    authorities()
            );
        }

        // ✅ Default authorities for every user
        public Collection<? extends GrantedAuthority> authorities() {
            return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // ✅ Register a new account
        public Account registerAccount(String username, String password) {
            if (accountRepository.findByUsername(username).isPresent()) {
                throw new RuntimeException("Username already exists");
            }

            Account account = new Account();
            account.setUsername(username);
            account.setPassword(passwordEncoder.encode(password));
            account.setStatus(Status.OFFLINE); // Added
            return accountRepository.save(account);
        }

        // ✅ Get all online users
        public List<Account> getConnectedUsers() {
            return accountRepository.findAllByStatus(Status.ONLINE);
        }

        // ✅ Update user to online when connected
        public void saveUser(Account user) {
            var existingUser = accountRepository.findByUsername(user.getUsername()).orElse(null);
            if (existingUser != null) {
                existingUser.setStatus(Status.ONLINE);
                accountRepository.save(existingUser);
            }
        }

        // ✅ Set user offline when disconnected
        public void disconnect(Account user) {
            var existingUser = accountRepository.findByUsername(user.getUsername()).orElse(null);
            if (existingUser != null) {
                existingUser.setStatus(Status.OFFLINE);
                accountRepository.save(existingUser);
            }
        }
    }



