// package com.example.demo.util;

// import java.io.IOException;

// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;
// @Component
// public class JwtFilter extends OncePerRequestFilter {

//     @Autowired
//     private JwtUtil jwtUtil;

//     @Autowired
//     private UserDetailsService userDetailsService;

//     @Override
//     protected void doFilterInternal(
//             HttpServletRequest request,
//             HttpServletResponse response,
//             FilterChain chain)
//             throws IOException, ServletException {
//                 String header = request.getHeader("Authorization");
//                 System.out.println("hiii");

//         if (header == null || !header.startsWith("Bearer ")) {
//             chain.doFilter(request, response);
//             return;
//         }

//         String token = header.substring(7);
//                 System.out.println(token);

//         try {
//             if (!jwtUtil.validate(token)) {
//                 chain.doFilter(request, response);
//                 return;
//             }
// String email = jwtUtil.extractAllClaims(token).get("email", String.class);
//                 System.out.println(email);

//             UserDetails userDetails =
//                     userDetailsService.loadUserByUsername(email);

//             UsernamePasswordAuthenticationToken auth =
//                     new UsernamePasswordAuthenticationToken(
//                             userDetails,
//                             null,
//                             userDetails.getAuthorities());

//             SecurityContextHolder.getContext().setAuthentication(auth);

//         } catch (Exception e) {
//             SecurityContextHolder.clearContext();
//         }

//         chain.doFilter(request, response);
//     }
// }