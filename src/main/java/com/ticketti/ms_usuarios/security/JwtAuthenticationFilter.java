package com.ticketti.ms_usuarios.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro de autenticación JWT.
 * Extrae token del header Authorization y valida claims.
 * Compatible con tokens del BFF: subject=correo, claim "rol".
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;


	@Override
	protected void doFilterInternal(
			@Nonnull HttpServletRequest request,
			@Nonnull HttpServletResponse response,
			@Nonnull FilterChain filterChain
	) throws ServletException, IOException {

		final String authHeader = request.getHeader("Authorization");

		// Si no hay token, continúa sin establecer auth.
		// Spring Security decidirá según las reglas de la ruta (permitAll / authenticated).
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			log.debug("Sin header Authorization en: {}", request.getRequestURI());
			filterChain.doFilter(request, response);
			return;
		}

		final String jwt = authHeader.substring(7);

		try {
			if (jwtService.isTokenValid(jwt) && jwtService.validateTokenClaims(jwt)) {
				// JWT válido: poblar el SecurityContext
				String correo = jwtService.extractCorreo(jwt);
				String role   = jwtService.extractRole(jwt);

				if (SecurityContextHolder.getContext().getAuthentication() == null) {
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
							correo,
							null,
							Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
					);
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
					log.debug("Autenticacion exitosa para: {}, rol: {}", correo, role);
				}
			} else {
				// JWT presente pero inválido: NO bloqueamos aquí.
				// Rutas con permitAll() pasarán; rutas authenticated() serán bloqueadas por Spring Security.
				log.warn("Token JWT invalido o claims incompletos para: {}", request.getRequestURI());
			}
		} catch (JwtException | IllegalArgumentException e) {
			// Token malformado: igual dejamos que Spring Security decida.
			log.warn("Error procesando JWT en {}: {}", request.getRequestURI(), e.getMessage());
		}

		filterChain.doFilter(request, response);
	}

}
