package com.eduvista.service;

import com.eduvista.entity.Role;
import com.eduvista.entity.User;
import com.eduvista.repository.RoleRepository;
import com.eduvista.repository.UserRepository;
import com.eduvista.security.JwtTokenUtil;
import com.eduvista.util.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Transactional
    public CommonResponse<Map<String, Object>> login(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsername(username).orElseThrow();
            
            String token = jwtTokenUtil.generateToken(userDetails);
            String role = user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName())
                .orElse("USER");
            
            String redisKey = "token:" + username;
            redisTemplate.opsForValue().set(redisKey, token, 24, TimeUnit.HOURS);
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("username", username);
            data.put("role", role);
            data.put("avatar", user.getAvatar());
            
            return CommonResponse.success(data);
        } catch (Exception e) {
            return CommonResponse.error("用户名或密码错误");
        }
    }
    
    @Transactional
    public CommonResponse<Map<String, Object>> register(String username, String password, String email) {
        if (userRepository.existsByUsername(username)) {
            return CommonResponse.error("用户名已存在");
        }
        if (userRepository.existsByEmail(email)) {
            return CommonResponse.error("邮箱已存在");
        }
        
        User user = User.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .email(email)
            .nickname(username)
            .build();
        
        Role studentRole = roleRepository.findByName("STUDENT")
            .orElseGet(() -> roleRepository.save(Role.builder().name("STUDENT").description("学生").build()));
        
        user.setRoles(Set.of(studentRole));
        user = userRepository.save(user);
        
        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        
        return CommonResponse.success(data);
    }
}

