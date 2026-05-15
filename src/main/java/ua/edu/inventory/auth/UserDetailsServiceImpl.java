package ua.edu.inventory.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ua.edu.inventory.site.SiteRepository;
import ua.edu.inventory.user.UserRepository;

import java.util.UUID;

/** Завантаження UserPrincipal з БД для Spring Security (form-login та JWT). */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final SiteRepository siteRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Користувач з логіном '" + username + "' не знайдений"));
        String siteName = user.getSiteId() != null
                ? siteRepository.findById(user.getSiteId()).map(s -> s.getName()).orElse(null)
                : null;
        return UserPrincipal.from(user, siteName);
    }

    public UserDetails loadUserById(UUID id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Користувач з id=" + id + " не знайдений"));
        String siteName = user.getSiteId() != null
                ? siteRepository.findById(user.getSiteId()).map(s -> s.getName()).orElse(null)
                : null;
        return UserPrincipal.from(user, siteName);
    }
}
