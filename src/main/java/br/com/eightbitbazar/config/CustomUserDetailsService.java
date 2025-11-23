package br.com.eightbitbazar.config;

import br.com.eightbitbazar.application.port.out.UserRepository;
import br.com.eightbitbazar.domain.user.User;
import br.com.eightbitbazar.domain.user.UserId;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService, UserDetailsPasswordService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .filter(u -> !u.isDeleted())
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return new org.springframework.security.core.userdetails.User(
            user.id().value().toString(),
            user.password(),
            List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name()))
        );
    }

    @Override
    @Transactional
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        Long userId = Long.parseLong(user.getUsername());
        userRepository.updatePassword(new UserId(userId), newPassword);

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            newPassword,
            user.getAuthorities()
        );
    }
}
