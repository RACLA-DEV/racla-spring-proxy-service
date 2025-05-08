package app.racla.raclaspringproxyservice.Config;

import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ActuatorTokenFilter extends OncePerRequestFilter {

    private final String token;

    public ActuatorTokenFilter(String token) {
        this.token = token;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/actuator")) {
            String requestToken = request.getHeader("Authorization");

            if (requestToken != null && requestToken.startsWith("Bearer ")) {
                String tokenValue = requestToken.substring(7);

                if (token.equals(tokenValue)) {
                    Authentication authentication = new PreAuthenticatedAuthenticationToken(
                            "actuator", null, AuthorityUtils.createAuthorityList("ROLE_ACTUATOR"));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
