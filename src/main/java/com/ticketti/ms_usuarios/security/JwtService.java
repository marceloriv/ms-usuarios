package com.ticketti.ms_usuarios.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio JWT para ms-usuarios.
 * Compatible con tokens emitidos por el BFF:
 * - subject : correo del usuario (String)
 * - claim   : "rol"
 * - claim   : "usuarioId"
 * Sin issuer ni audience estrictos. Algoritmo: HS256.
 */

/**
 * Servicio para validacion y extraccion de claims JWT.
 * Compatible con tokens emitidos por el BFF:
 * - subject : correo del usuario (String)
 * - claim   : "rol"
 * - sin issuer ni audience estrictos
 * Algoritmo: HS256.
 */
@Service
@Slf4j
public class JwtService {

	@Value("${jwt.secret}")
	private String secretKey;

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
	}

	private static final long EXPIRATION = 1000L * 60 * 60 * 24; // 24 horas

	/** Genera un token JWT con el mismo formato que el BFF. */
	public String generarToken(String correo, String rol, Long usuarioId) {
		return Jwts.builder()
				.subject(correo)
				.claim("rol", rol)
				.claim("usuarioId", usuarioId)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + EXPIRATION))
				.signWith(getSigningKey())
				.compact();
	}

	// ── Métodos de contexto ────────────────────────────────────────────────

	/**
	 * Devuelve el correo del usuario autenticado desde el SecurityContext.
	 * El subject del token BFF es el correo, no un ID numérico.
	 */
	public String getCorreoFromContext() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new SecurityException("Usuario no autenticado");
		}
		Object principal = authentication.getPrincipal();
		if (principal instanceof String correo) {
			return correo;
		}
		throw new SecurityException("Formato de usuario invalido");
	}

	/**
	 * Devuelve el rol del usuario autenticado desde el SecurityContext.
	 */
	public String getRoleFromContext() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new SecurityException("Usuario no autenticado");
		}
		return authentication.getAuthorities().stream()
				.findFirst()
				.map(ga -> ga.getAuthority().replace("ROLE_", ""))
				.orElseThrow(() -> new SecurityException("Rol no encontrado"));
	}

	// ── Extracción de claims ───────────────────────────────────────────────

	/** Devuelve el correo del usuario (subject del token BFF). */
	public String extractCorreo(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	/** Devuelve el rol desde el claim "rol" (formato BFF). */
	public String extractRole(String token) {
		return extractClaim(token, claims -> claims.get("rol", String.class));
	}

	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		return claimsResolver.apply(extractAllClaims(token));
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	// ── Validación ─────────────────────────────────────────────────────────

	/**
	 * Valida que el token no haya expirado y tenga subject y rol presentes.
	 * No valida issuer ni audience — el BFF no los incluye.
	 */
	public boolean isTokenValid(String token) {
		try {
			return extractCorreo(token) != null && !isTokenExpired(token);
		} catch (Exception e) {
			log.error("Error validando token: {}", e.getMessage());
			return false;
		}
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	public boolean validateTokenClaims(String token) {
		try {
			Claims claims = extractAllClaims(token);
			return claims.getSubject() != null
					&& claims.get("rol") != null
					&& claims.getExpiration() != null;
		} catch (Exception e) {
			log.error("Token invalido: {}", e.getMessage());
			return false;
		}
	}
}
